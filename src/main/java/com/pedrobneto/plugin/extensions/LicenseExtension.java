package com.pedrobneto.plugin.extensions;

import org.gradle.api.provider.Property;

public interface LicenseExtension {
    Property<String> getLicenseName();
    Property<String> getLicenseUrl();
}
