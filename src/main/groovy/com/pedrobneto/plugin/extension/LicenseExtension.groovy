package com.pedrobneto.plugin.extension

import org.gradle.api.provider.Property

interface LicenseExtension {
    Property<String> getLicenseName()

    Property<String> getLicenseUrl()
}
