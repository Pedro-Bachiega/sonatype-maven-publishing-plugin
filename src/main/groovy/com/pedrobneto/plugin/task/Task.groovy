package com.pedrobneto.plugin.task

import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar

class Task {
    static void configureDefault(Project project) {
        if (!project.tasks.named('sourcesJar').getOrNull()) {
            project.tasks.register('sourcesJar', Jar) {
                from android.sourceSets.main.java.srcDirs
                archiveClassifier = 'sources'
            }
        }

        if (!project.tasks.named('javadoc').getOrNull()) {
            project.tasks.register('javadoc', Javadoc) {
                excludes = ['**/*.kt']
                source = android.sourceSets.main.java.srcDirs
                classpath += project.files(android.getBootClasspath().join(File.pathSeparator))
            }
        }

        if (!project.tasks.named('javadocJar').getOrNull()) {
            project.tasks.register('javadocJar', Jar) {
                dependsOn project.tasks.javadoc
                archiveClassifier = 'javadoc'
                from javadoc.destinationDir
            }
        }

        project.tasks.withType(Javadoc).configureEach {
            options.addStringOption('Xdoclint:none', '-quiet')
            options.addStringOption('encoding', 'UTF-8')
        }

        artifacts {
            archives project.tasks.javadocJar, project.tasks.sourcesJar
        }
    }
}
