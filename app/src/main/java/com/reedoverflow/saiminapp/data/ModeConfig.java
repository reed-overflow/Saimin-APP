package com.reedoverflow.saiminapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ModeConfig {
    public final String id;
    public String name;
    public final List<ParameterConfig> parameters = new ArrayList<>();
    public int animationSizePercent = 90;
    public int animationHeightDp = 280;
    public int animationDurationMs = 5000;
    public String animationColor = "#F06292";
    public String backgroundColor = "#FFF0F5";
    public String animationType = "ripple";
    public boolean fullscreen = false;

    public ModeConfig(String id, String name) {
        this.id = id == null || id.isEmpty() ? UUID.randomUUID().toString() : id;
        this.name = name;
    }

    public ModeConfig(String name) {
        this(null, name);
    }
}
