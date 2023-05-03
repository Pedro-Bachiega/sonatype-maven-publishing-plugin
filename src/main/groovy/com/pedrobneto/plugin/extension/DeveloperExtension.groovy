package com.pedrobneto.plugin.extension

import org.gradle.api.provider.Property

interface DeveloperExtension {
    Property<String> getDeveloperId()

    Property<String> getDeveloperName()

    Property<String> getDeveloperEmail()
}
