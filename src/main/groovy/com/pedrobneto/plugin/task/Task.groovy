package com.pedrobneto.plugin.task

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar

class Task {
    static void configureDefault(Project project) {
        final android = project.extensions.findByType(LibraryExtension)
        if (!android) return

        if (!project.tasks.findByName('sourcesJar')) {
            project.tasks.register('sourcesJar', Jar) {
                from android.sourceSets.main.java.srcDirs
                archiveClassifier = 'sources'
            }
        }

        if (!project.tasks.findByName('javadoc')) {
            project.tasks.register('javadoc', Javadoc) {
                excludes = ['**/*.kt']
                source = android.sourceSets.main.java.srcDirs
                classpath += project.files(android.getBootClasspath().join(File.pathSeparator))
            }
        }

        if (!project.tasks.findByName('javadocJar')) {
            project.tasks.register('javadocJar', Jar) {
                dependsOn project.tasks.javadoc
                archiveClassifier = 'javadoc'
                from project.tasks.javadoc.destinationDir
            }
        }

        project.tasks.withType(Javadoc).configureEach {
            options.addStringOption('Xdoclint:none', '-quiet')
            options.addStringOption('encoding', 'UTF-8')
        }

        project.artifacts {
            archives project.tasks.javadocJar, project.tasks.sourcesJar
        }
    }

    static void hidePublishingTasks(Project project) {
        final wantedList = ['publish', 'publishToMavenLocal']
        project.tasks.configureEach {
            if (it.group == 'publishing' && it.name !in wantedList) it.group = null
        }
    }
}
