package com.pedrobneto

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class SonatypePublish implements Plugin<Project> {

    private static String releasesRepositoryUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
    private static String snapshotsRepositoryUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

    private static void hideTasks(Project project, String... taskList) {
        taskList.each {
            project.tasks.findByName(it).group = null
        }
    }

    private static void hidePublishingTasks(Project project) {
        hideTasks(
                project,
                "generateMetadataFileForLibraryPublication",
                "generatePomFileForLibraryPublication",
                "publish",
                "publishAllPublicationsToMavenRepository",
                "publishLibraryPublicationToMavenLocal",
                "publishLibraryPublicationToMavenRepository",
                "publishToMavenLocal"
        )
    }

    private static void configureRepositories(Project project, SonatypePublishExtension extension) {
        project.repositories.maven {
            url = extension.versionName.endsWith("SNAPSHOT") ? snapshotsRepositoryUrl : releasesRepositoryUrl
            name = "maven"
            credentials {
                username = extension.credentials.username
                password = extension.credentials.password
            }
        }
    }

    private static void configurePublications(Project project, SonatypePublishExtension extension) {
        project.publications {
            library(MavenPublication) {
                artifactId = extension.artifactId

                pom {
                    name = extension.libName
                    description = extension.libDescription
                    url = extension.repositoryUrl

                    scm {
                        url = extension.repositoryUrl
                        connection = extension.repositoryConnectionUrl
                        developerConnection = extension.repositoryConnectionUrl
                    }

                    licenses {
                        license {
                            name = extension.licenseName
                            url = extension.licenseUrl
                        }
                    }

                    developers {
                        developer {
                            id = extension.developerId
                            name = extension.developerName
                            email = extension.developerEmail
                        }
                    }
                }
            }
        }
    }

    @Override
    void apply(Project project) {
        def extension = project.extensions.create("sonatypePublishing", SonatypePublishExtension)

        configureRepositories(project, extension)
        configurePublications(project, extension)

        project.setGroup(extension.groupId)
        project.setVersion(extension.versionName)

        project.task("publishToSonatype") {
            group = "publishing"
            dependsOn "publish"
        }

        hidePublishingTasks(project)
    }
}
