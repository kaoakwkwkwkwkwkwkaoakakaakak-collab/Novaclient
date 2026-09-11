package com.novaclient.module;

import net.minecraft.text.Text;

public enum ModuleCategory {
    PERFORMANCE("Performance", "Frame pacing and CPU budgets"),
    RENDER("Render", "Only draw what matters"),
    WORLD("World", "Lighter simulation and streaming"),
    SYSTEM("System", "Client-side stability controls");

    private final String label;
    private final String description;

    ModuleCategory(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public Text text() {
        return Text.literal(label);
    }
}
