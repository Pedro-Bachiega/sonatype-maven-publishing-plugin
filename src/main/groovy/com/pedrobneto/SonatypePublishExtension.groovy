package com.pedrobneto

import org.gradle.api.artifacts.repositories.AuthenticationSupported
import org.gradle.api.provider.Property

interface SonatypePublishExtension extends AuthenticationSupported {
    Property<String> getArtifactId()
    Property<String> getGroupId()
    Property<String> getVersionName()

    Property<String> getLibName()
    Property<String> getLibDescription()
    Property<String> getRepositoryUrl()
    Property<String> getRepositoryConnectionUrl()

    Property<String> getLicenseName()
    Property<String> getLicenseUrl()

    Property<String> getDeveloperId()
    Property<String> getDeveloperName()
    Property<String> getDeveloperEmail()
}