package com.pedrobneto.plugin.model

import org.gradle.api.provider.Property

import javax.inject.Inject

abstract class Artifact {
    public final String name

    private final List<Object> sources = new ArrayList<>()

    @Inject
    Artifact(String artifactName) {
        this.name = artifactName
    }

    abstract Property<String> getArtifactId()

    abstract Property<String> getLibName()

    abstract Property<String> getLibDescription()

    void artifact(Object source) {
        sources.add(source)
    }

    List<Object> getSources() {
        return sources
    }

    String getTrimmedDisplayName() {
        return libName.get().trim().replace(" ", "")
    }
}
