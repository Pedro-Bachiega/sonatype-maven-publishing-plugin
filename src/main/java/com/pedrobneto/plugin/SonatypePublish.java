package com.pedrobneto.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.internal.impldep.com.esotericsoftware.minlog.Log;
import org.gradle.plugins.signing.SigningExtension;

public class SonatypePublish implements Plugin<Project> {

    private static final String publicationName = "library";

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

    private void configurePublications(
            Project project,
            SonatypePublishExtension sonatypePublishExtension,
            PublishingExtension publishingExtension
    ) {
        MavenPublication pub = publishingExtension
                .getPublications()
                .create(publicationName, MavenPublication.class, publication -> {
                    publication.setArtifactId(sonatypePublishExtension.getArtifactId().get());
                    publication.pom(mavenPom -> {
                        mavenPom.getName().set(sonatypePublishExtension.getLibName());
                        mavenPom.getDescription().set(sonatypePublishExtension.getLibDescription());
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

        sign(project, pub);
    }

    private void sign(Project project, Publication publication) {
        SigningExtension signing = project.getExtensions().findByType(SigningExtension.class);
        if (signing == null) {
            Log.warn("Plugin \"signing\" not found.\nPossible solution: \"apply plugin: 'signing'\" before sonatype-maven-publish.");
            return;
        }

        signing.sign(publication);
    }

    @Override
    public void apply(Project project) {
        SonatypePublishExtension sonatypePublishExtension = project
                .getExtensions()
                .create(SonatypePublishExtension.NAME, SonatypePublishExtension.class);

        PublishingExtension publishingExtension = project
                .getExtensions()
                .findByType(PublishingExtension.class);

        if (publishingExtension == null) {
            Log.error("Plugin \"maven-publish\" not found.\nPossible solution: \"apply plugin: 'maven-publish'\" before sonatype-maven-publish.");
            return;
        }

        project.afterEvaluate(afterEvaluate -> {
            configureRepositories(afterEvaluate, sonatypePublishExtension, publishingExtension);
            configurePublications(afterEvaluate, sonatypePublishExtension, publishingExtension);
            hidePublishingTasks(afterEvaluate);
        });
    }
}
