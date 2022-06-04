package com.pedrobneto.plugin;

import com.pedrobneto.plugin.extensions.ArtifactExtension;
import com.pedrobneto.plugin.extensions.DeveloperExtension;
import com.pedrobneto.plugin.extensions.LicenseExtension;
import com.pedrobneto.plugin.extensions.RepositoryExtension;
import org.gradle.api.artifacts.repositories.PasswordCredentials;

public interface SonatypePublishExtension extends ArtifactExtension,
        DeveloperExtension,
        LicenseExtension,
        PasswordCredentials,
        RepositoryExtension {
    String NAME = "sonatypePublishing";
}
