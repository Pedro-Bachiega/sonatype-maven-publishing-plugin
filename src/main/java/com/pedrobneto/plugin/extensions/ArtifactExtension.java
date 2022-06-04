package com.pedrobneto.plugin.extensions;

import org.gradle.api.provider.Property;

public interface ArtifactExtension {
    Property<String> getArtifactId();

    Property<String> getLibName();
    Property<String> getLibDescription();
}
