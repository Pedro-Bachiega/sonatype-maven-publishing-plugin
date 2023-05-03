//file:noinspection GrMethodMayBeStatic
package com.pedrobneto.plugin

import com.pedrobneto.plugin.extension.SonatypePublishExtension
import com.pedrobneto.plugin.model.Artifact
import com.pedrobneto.plugin.task.Task
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.internal.impldep.com.esotericsoftware.minlog.Log
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

class SonatypePublish implements Plugin<Project> {

    private static final String snapshotSuffix = "SNAPSHOT"

    private static final String repositoryName = "sonatypeMaven"
    private static final String releasesRepositoryUrl =
            "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
    private static final String snapshotsRepositoryUrl =
            "https://s01.oss.sonatype.org/content/repositories/snapshots/"

    private void hideTasks(Project project, String... taskList) {
        taskList.each {
            final task = project.tasks.findByName(it)
            if (task) task.group = null
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
        )
    }

    private void configureRepositories(
            Project project,
            SonatypePublishExtension sonatypePublishExtension,
            PublishingExtension publishingExtension
    ) {
        publishingExtension.repositories.maven { MavenArtifactRepository repo ->
            repo.name = repositoryName
            repo.url = project.version.toString().endsWith(snapshotSuffix)
                    ? snapshotsRepositoryUrl : releasesRepositoryUrl

            repo.credentials { passwordCredentials ->
                passwordCredentials.username = sonatypePublishExtension.username
                passwordCredentials.password = sonatypePublishExtension.password
            }
        }
    }

    private void configureDevelopers(MavenPomDeveloperSpec spec, SonatypePublishExtension extension) {
        spec.developer { developer ->
            developer.id.set(extension.developerId.get())
            developer.name.set(extension.developerName.get())
            developer.email.set(extension.developerEmail.get())
        }
    }

    private void configureLicenses(MavenPomLicenseSpec spec, SonatypePublishExtension extension) {
        spec.license { license ->
            license.name.set(extension.licenseName.get())
            license.url.set(extension.licenseUrl.get())
        }
    }

    private void configureScm(MavenPomScm scm, SonatypePublishExtension extension) {
        scm.url.set(extension.repositoryUrl.get())
        scm.connection.set(extension.repositoryConnectionUrl.get())
        scm.developerConnection.set(extension.repositoryConnectionUrl.get())
    }

    private void configurePom(MavenPom pom, Artifact artifact, SonatypePublishExtension extension) {
        pom.name.set(artifact.libName.get())
        pom.description.set(artifact.libDescription.get())
        pom.url.set(extension.repositoryUrl.get())

        pom.developers { configureDevelopers(it, extension) }
        pom.licenses { configureLicenses(it, extension) }
        pom.scm { configureScm(it, extension) }
    }

    private void configurePublication(
            MavenPublication publication,
            Artifact artifact,
            SoftwareComponent component,
            Boolean isSingleComponent,
            SonatypePublishExtension extension
    ) {
        if (artifact.sources.empty) publication.from component
        else artifact.sources.forEach(publication::artifact)

        var publicationId = artifact.artifactId.get()
        if (!isSingleComponent) {
            publicationId += "-" + component.name.replaceAll("([A-Z])", "-\1").toLowerCase()
        }

        publication.artifactId = publicationId
        publication.pom { configurePom(it, artifact, extension) }
    }

    private MavenPublication createPublication(
            Project project,
            SonatypePublishExtension sonatypePublishExtension,
            PublishingExtension publishingExtension,
            List<Artifact> artifacts
    ) {
        return publishingExtension.publications {
            final isSingleComponent = project.components.size() == 1
            project.components.forEach { SoftwareComponent component ->
                artifacts.forEach { artifact ->
                    it.create("${artifact.name}${component.name.capitalize()}", MavenPublication) {
                        configurePublication(it, artifact, component, isSingleComponent, sonatypePublishExtension)
                        sign(project, it)
                    }
                }
            }
        }
    }

    private void sign(Project project, Publication publication) {
        final signing = project.extensions.findByType(SigningExtension)
        if (signing) signing.sign(publication) else Log.warn("Signing extension not found.")
    }

    private void checkAndApplyRequiredPlugins(Project project) {
        if (!project.plugins.hasPlugin(MavenPublishPlugin)) project.plugins.apply(MavenPublishPlugin)
        if (!project.plugins.hasPlugin(SigningPlugin)) project.plugins.apply(SigningPlugin)
    }

    @Override
    void apply(Project project) {
        checkAndApplyRequiredPlugins(project)

        final objectFactory = project.objects
        final artifactContainer = objectFactory.domainObjectContainer(
                Artifact,
                name -> objectFactory.newInstance(Artifact, name)

        )
        project.extensions.add("publications", artifactContainer)

        final publishingExtension = project.extensions.findByType(PublishingExtension)
        if (!publishingExtension) {
            Log.error("Publishing extension not found.")
            return
        }

        final sonatypePublishExtension = project.extensions
                .create(SonatypePublishExtension.NAME, SonatypePublishExtension)

        project.afterEvaluate { Project afterEvaluate ->
            Task.configureDefault(afterEvaluate)
            configureRepositories(afterEvaluate, sonatypePublishExtension, publishingExtension)

            final artifacts = artifactContainer.collect()
            createPublication(afterEvaluate, sonatypePublishExtension, publishingExtension, artifacts)

            hideTasks(
                    afterEvaluate,
                    *artifacts*.trimmedDisplayName*.capitalize().collectMany { name ->
                        [
                                "generateMetadataFileFor${name}Publication",
                                "generatePomFileFor${name}Publication",
                                "publish${name}PublicationToMavenLocal",
                                "publish${name}PublicationToSonatypeMavenRepository"
                        ]
                    }.toArray()
            )

            hidePublishingTasks(afterEvaluate)
        }
    }
}
