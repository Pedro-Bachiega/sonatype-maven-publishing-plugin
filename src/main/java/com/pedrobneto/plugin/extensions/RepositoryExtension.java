package com.pedrobneto.plugin.extensions;

import org.gradle.api.provider.Property;

public interface RepositoryExtension {
    Property<String> getRepositoryUrl();
    Property<String> getRepositoryConnectionUrl();
}
