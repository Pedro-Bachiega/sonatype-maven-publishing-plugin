package com.pedrobneto.plugin.extensions;

import org.gradle.api.provider.Property;

public interface DeveloperExtension {
    Property<String> getDeveloperId();
    Property<String> getDeveloperName();
    Property<String> getDeveloperEmail();
}
