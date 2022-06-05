package com.pedrobneto.plugin.extension;

import org.gradle.api.provider.Property;

public interface DeveloperExtension {
    Property<String> getDeveloperId();
    Property<String> getDeveloperName();
    Property<String> getDeveloperEmail();
}
