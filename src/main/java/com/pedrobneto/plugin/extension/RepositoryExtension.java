package com.pedrobneto.plugin.extension;

import org.gradle.api.provider.Property;

public interface RepositoryExtension {
    Property<String> getRepositoryUrl();
    Property<String> getRepositoryConnectionUrl();
}
