package com.pedrobneto.plugin.model;

import org.gradle.api.provider.Property;

import java.util.ArrayList;
import java.util.List;

abstract public class Artifact {
    public final String name;

    private final List<Object> sources = new ArrayList<>();

    @javax.inject.Inject
    public Artifact(String artifactName) {
        this.name = artifactName;
    }

    abstract public Property<String> getArtifactId();

    abstract public Property<String> getLibName();

    abstract public Property<String> getLibDescription();

    public void artifact(Object source) {
        sources.add(source);
    }

    public List<Object> getSources() {
        return sources;
    }

    public String getTrimmedDisplayName() {
        return getLibName().get().trim().replace(" ", "");
    }
}
