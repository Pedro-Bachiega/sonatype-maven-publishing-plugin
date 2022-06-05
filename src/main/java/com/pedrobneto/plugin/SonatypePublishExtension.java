package com.pedrobneto.plugin;

import com.pedrobneto.plugin.extension.DeveloperExtension;
import com.pedrobneto.plugin.extension.LicenseExtension;
import com.pedrobneto.plugin.extension.RepositoryExtension;
import org.gradle.api.artifacts.repositories.PasswordCredentials;

public interface SonatypePublishExtension extends DeveloperExtension,
        LicenseExtension,
        PasswordCredentials,
        RepositoryExtension {
    String NAME = "sonatypePublishing";
}
