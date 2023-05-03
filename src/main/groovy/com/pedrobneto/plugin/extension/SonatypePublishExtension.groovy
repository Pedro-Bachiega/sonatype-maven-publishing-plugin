package com.pedrobneto.plugin.extension

import org.gradle.api.artifacts.repositories.PasswordCredentials

interface SonatypePublishExtension extends ComponentExtension,
        DeveloperExtension,
        LicenseExtension,
        PasswordCredentials,
        RepositoryExtension {
    String NAME = "sonatypePublishing"
}
