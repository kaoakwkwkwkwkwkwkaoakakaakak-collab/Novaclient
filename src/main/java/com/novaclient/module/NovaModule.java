package com.novaclient.module;

import net.minecraft.client.MinecraftClient;

import java.util.Objects;
import java.util.function.Consumer;

/** A small, deliberately dependency-free feature unit. Modules never assume Sodium is present. */
public final class NovaModule {
    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final Consumer<MinecraftClient> enableAction;
    private final Consumer<MinecraftClient> disableAction;
    private boolean enabled;

    public NovaModule(
            String id,
            String name,
            String description,
            ModuleCategory category,
            boolean enabled,
            Consumer<MinecraftClient> enableAction,
            Consumer<MinecraftClient> disableAction
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.category = Objects.requireNonNull(category);
        this.enabled = enabled;
        this.enableAction = enableAction == null ? client -> {} : enableAction;
        this.disableAction = disableAction == null ? client -> {} : disableAction;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public ModuleCategory category() {
        return category;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled, MinecraftClient client) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            enableAction.accept(client);
        } else {
            disableAction.accept(client);
        }
    }

    public void forceState(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle(MinecraftClient client) {
        setEnabled(!enabled, client);
    }
}
