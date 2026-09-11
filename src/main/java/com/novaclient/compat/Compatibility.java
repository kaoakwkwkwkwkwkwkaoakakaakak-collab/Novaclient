package com.novaclient.compat;

import net.fabricmc.loader.api.FabricLoader;

/** Optional integrations are discovered, never required. This keeps Nova safe beside other render mods. */
public final class Compatibility {
    private Compatibility() {}

    public static boolean hasSodium() {
        return FabricLoader.getInstance().isModLoaded("sodium");
    }

    public static String rendererLabel() {
        return hasSodium() ? "Sodium detected" : "Vanilla renderer";
    }

    public static String sodiumVersion() {
        return FabricLoader.getInstance().getModContainer("sodium")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("not installed");
    }
}
