package com.pedrobneto.plugin.extension

import org.gradle.api.provider.ListProperty

interface ComponentExtension {
    ListProperty<String> getAllowedComponents()
}
