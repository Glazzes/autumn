package com.glaze.autumn.model;

import com.glaze.autumn.shared.enums.EnvironmentType;

public class Environment {
    private final String path;
    private final EnvironmentType type;

    public Environment(String path, EnvironmentType type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public EnvironmentType getType() {
        return type;
    }
}
