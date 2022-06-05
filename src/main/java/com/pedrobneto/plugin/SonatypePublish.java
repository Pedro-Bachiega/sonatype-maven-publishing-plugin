package com.pedrobneto.plugin;

import com.pedrobneto.plugin.model.Artifact;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.internal.impldep.com.esotericsoftware.minlog.Log;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

public class SonatypePublish implements Plugin<Project> {

    private static final String snapshotSuffix = "SNAPSHOT";

    private static final String repositoryName = "sonatypeMaven";
    private static final String releasesRepositoryUrl =
            "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/";
    private static final String snapshotsRepositoryUrl =
            "https://s01.oss.sonatype.org/content/repositories/snapshots/";

    private void hideTasks(Project project, String... taskList) {
        for (String taskName : taskList) {
            Task task = project.getTasks().findByName(taskName);
            if (task != null) task.setGroup(null);
        }
    }

    private void hidePublishingTasks(Project project) {
        hideTasks(
                project,
                "generateMetadataFileForLibraryPublication",
                "generatePomFileForLibraryPublication",
                "publishAllPublicationsToSonatypeMavenRepository",
                "publishLibraryPublicationToMavenLocal",
                "publishLibraryPublicationToSonatypeMavenRepository"
        );
    }

    private void configureRepositories(
            Project project,
            SonatypePublishExtension sonatypePublishExtension,
            PublishingExtension publishingExtension
    ) {
        publishingExtension.getRepositories().maven(mavenArtifactRepository -> {
            mavenArtifactRepository.setName(repositoryName);
            mavenArtifactRepository.setUrl(
                    project.getVersion().toString().endsWith(snapshotSuffix)
                            ? snapshotsRepositoryUrl : releasesRepositoryUrl
            );
            mavenArtifactRepository.credentials(passwordCredentials -> {
                passwordCredentials.setUsername(sonatypePublishExtension.getUsername());
                passwordCredentials.setPassword(sonatypePublishExtension.getPassword());
            });
        });
    }

    private MavenPublication createPublication(
            SonatypePublishExtension sonatypePublishExtension,
            PublishingExtension publishingExtension,
            Artifact artifact
    ) {
        return publishingExtension
                .getPublications()
                .create(artifact.getTrimmedDisplayName(), MavenPublication.class, publication -> {
                    artifact.getSources().forEach(publication::artifact);

                    publication.setArtifactId(artifact.getArtifactId().get());
                    publication.pom(mavenPom -> {
                        mavenPom.getName().set(artifact.getLibName());
                        mavenPom.getDescription().set(artifact.getLibDescription());
                        mavenPom.getUrl().set(sonatypePublishExtension.getRepositoryUrl());

                        mavenPom.scm(scm -> {
                            scm.getUrl().set(sonatypePublishExtension.getRepositoryUrl());
                            scm.getConnection().set(sonatypePublishExtension.getRepositoryConnectionUrl());
                            scm.getDeveloperConnection().set(sonatypePublishExtension.getRepositoryConnectionUrl());
                        });

                        mavenPom.licenses(licenses -> licenses.license(license -> {
                            license.getName().set(sonatypePublishExtension.getLicenseName());
                            license.getUrl().set(sonatypePublishExtension.getLicenseUrl());
                        }));

                        mavenPom.developers(developers -> developers.developer(developer -> {
                            developer.getId().set(sonatypePublishExtension.getDeveloperId());
                            developer.getName().set(sonatypePublishExtension.getDeveloperName());
                            developer.getEmail().set(sonatypePublishExtension.getDeveloperEmail());
                        }));
                    });
                });
    }

    private void sign(Project project, Publication publication) {
        SigningExtension signing = project.getExtensions().findByType(SigningExtension.class);
        if (signing == null) {
            Log.warn("Signing extension not found.");
            return;
        }

        signing.sign(publication);
    }

    private void checkAndApplyRequiredPlugins(Project project) {
        if (!project.getPlugins().hasPlugin(MavenPublishPlugin.class)) {
            project.getPlugins().apply(MavenPublishPlugin.class);
        }

        if (!project.getPlugins().hasPlugin(SigningPlugin.class)) {
            project.getPlugins().apply(SigningPlugin.class);
        }
    }

    @Override
    public void apply(Project project) {
        checkAndApplyRequiredPlugins(project);

        ObjectFactory objectFactory = project.getObjects();
        NamedDomainObjectContainer<Artifact> artifactContainer = objectFactory
                .domainObjectContainer(Artifact.class, name -> objectFactory.newInstance(Artifact.class, name));
        project.getExtensions().add("publications", artifactContainer);

        SonatypePublishExtension sonatypePublishExtension = project
                .getExtensions()
                .create(SonatypePublishExtension.NAME, SonatypePublishExtension.class);

        PublishingExtension publishingExtension = project
                .getExtensions()
                .findByType(PublishingExtension.class);

        if (publishingExtension == null) {
            Log.error("Publishing extension not found.");
            return;
        }

        project.afterEvaluate(afterEvaluate -> {
            configureRepositories(afterEvaluate, sonatypePublishExtension, publishingExtension);

            artifactContainer.all(artifact -> {
                Publication publication = createPublication(sonatypePublishExtension, publishingExtension, artifact);
                sign(project, publication);

                String name = artifact.getTrimmedDisplayName();
                String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);
                hideTasks(
                        project,
                        "generateMetadataFileFor" + capitalizedName + "Publication",
                        "generatePomFileFor" + capitalizedName + "Publication",
                        "publish" + capitalizedName + "PublicationToMavenLocal",
                        "publish" + capitalizedName + "PublicationToSonatypeMavenRepository"
                );
            });

            hidePublishingTasks(afterEvaluate);
        });
    }
}
