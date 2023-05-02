# Setup

To implement this plugin there are just a few simple steps.

---------------------------------------------------------------------------------
### Adding the dependency

If needed, add the plugins repository to the project's root **build.gradle** repositories block:
```
maven {
    url "https://plugins.gradle.org/m2/"
}
```

Then simply add this dependency to the project's root **build.gradle** dependencies block:
```
classpath 'io.github.pedro-bachiega:SonatypeMavenPublishPlugin:0.0.3'
```

---------------------------------------------------------------------------------
### Applying the plugin

To apply the plugin, just add this line to the **build.gradle** file on the module you will be publishing from:
```
apply plugin: 'io.github.pedro-bachiega.sonatype-maven-publish'
```

---------------------------------------------------------------------------------
### Using the plugin

To start configuring, first you need to enter your information in relation to the project.
As a simple example, all the information will be displayed here, but you can put it on your gradle.properties file or wherever you think suits you better.
```
sonatypePublishing {
    repositoryUrl = "https://github.com/your_user/sample_repo" //Url to your source code
    repositoryConnectionUrl = "scm:https://github.com/your_user/sample_repo.git" //Url you use to connect to your source code

    licenseName = "The Apache License, Version 2.0" //Name of the license used by the project
    licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt" //Url of the license used by the project

    developerId = "johnDoe" //Your id
    developerName = "John Doe" //Your name
    developerEmail = "john.doe@sample-mail.com" //Your contact e-mail

    username = "john_doe" //The user you use on https://s01.oss.sonatype.org/
    password = "password" //The password you use on https://s01.oss.sonatype.org/
}
```

You **must** have a **javadocJar** task, a **sourcesJar** task and the **artifacts** configured on the module
you'll be publishing from for your lib to be accepted when published.
For all rules and a complete tutorial on how to publish a lib (you can skip the gradle part if you're using this plugin),
refer to https://central.sonatype.org/publish/publish-guide/#introduction.

The following sample code provides the standard configuration for all of the above.
```
android {
    task sourcesJar(type: Jar) {
        from android.sourceSets.main.java.srcDirs
        classifier = 'sources'
    }

    task javadoc(type: Javadoc) {
        excludes = ['**/*.kt']
        source = android.sourceSets.main.java.srcDirs
        classpath += project.files(android.getBootClasspath().join(File.pathSeparator))
    }

    task javadocJar(type: Jar, dependsOn: javadoc) {
        classifier = 'javadoc'
        from javadoc.destinationDir
    }

    tasks.withType(Javadoc) {
        options.addStringOption('Xdoclint:none', '-quiet')
        options.addStringOption('encoding', 'UTF-8')
    }

    artifacts {
        archives javadocJar, sourcesJar
    }
}
```

After this initial setup, all you need to do is create a new publication for each artifact you want to publish.
```
publications {
    myCoolLib {
        artifactId = "my-cool-lib"
        libName = "My Cool Library"
        libDescription = "Welcome to my cool library"
    }
}
```

If you plan on publishing multiple libs at a time and they use different tasks and file locations,
you **must** provide each of them like so.
```
publications {
    firstLib {
        artifactId = "first-lib"
        libName = "First Library"
        libDescription = "My first awesome library"

        artifact sourcesFirstLibJar
        artifact javadocFirstLibJar
        artifact "$buildDir/outputs/aar/first-lib-release.aar"
    }

    secondLib {
        artifactId = "second-lib"
        libName = "Second Library"
        libDescription = "My second awesome library"

        artifact sourcesSecondLibJar
        artifact javadocSecondLibJar
        artifact "$buildDir/outputs/aar/second-lib-release.aar"
    }
}
```
