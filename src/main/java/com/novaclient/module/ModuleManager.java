package com.novaclient.module;

import com.novaclient.compat.Compatibility;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.particle.ParticlesMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Registry, persistence, and the few safe vanilla option hooks used by Nova. */
public final class ModuleManager {
    private static final Map<String, NovaModule> MODULES = new LinkedHashMap<>();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("novaclient.properties");
    private static boolean bootstrapped;
    private static boolean showHud = true;
    private static boolean compactHud;
    private static int tickCounter;

    private static Integer originalViewDistance;
    private static Integer originalSimulationDistance;
    private static Integer originalMaxFps;
    private static Boolean originalEntityShadows;
    private static ParticlesMode originalParticles;
    private static CloudRenderMode originalClouds;
    private static Integer originalBiomeBlend;

    private ModuleManager() {}

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        // Performance: conservative defaults that do not touch server behavior.
        add("dynamic_fps", "Dynamic FPS", "Lowers the frame cap while the window is unfocused.", ModuleCategory.PERFORMANCE, true);
        add("frame_pacing", "Frame Pacing", "Keeps client-side work inside a predictable frame budget.", ModuleCategory.PERFORMANCE, true);
        add("lazy_chunk_loading", "Lazy Chunk Loading", "Stages chunk work instead of spiking a single frame.", ModuleCategory.PERFORMANCE, true);
        add("memory_defrag", "Memory Defrag", "Releases stale client caches during safe idle windows.", ModuleCategory.PERFORMANCE, true);
        add("tick_stabilizer", "Tick Stabilizer", "Avoids optional client work when the frame budget is exceeded.", ModuleCategory.PERFORMANCE, true);
        add("async_uploads", "Async Uploads", "Queues render uploads away from the gameplay path.", ModuleCategory.PERFORMANCE, true);
        add("input_priority", "Input Priority", "Processes input before cosmetic client work.", ModuleCategory.PERFORMANCE, true);
        add("fast_math", "Fast Math", "Uses lean math paths for client-only visual calculations.", ModuleCategory.PERFORMANCE, true);
        add("jvm_profile", "JVM Profile", "Shows a safe launch profile hint without changing JVM flags.", ModuleCategory.PERFORMANCE, false);

        // Render: compatible switches; Sodium remains the renderer when installed.
        add("entity_culling", "Entity Culling", "Skips entities hidden behind opaque geometry where possible.", ModuleCategory.RENDER, true);
        add("block_entity_culling", "Block Entity Culling", "Keeps distant block entities out of the render queue.", ModuleCategory.RENDER, true);
        add("render_distance_budget", "Render Distance Budget", "Caps vanilla view distance at 12 chunks while active.", ModuleCategory.RENDER, false);
        add("motion_blur", "Motion Blur", "Subtle temporal trail for camera movement; off by default for clarity.", ModuleCategory.RENDER, false);
        add("particle_optimizer", "Particle Optimizer", "Reduces decorative particle pressure before it reaches the GPU.", ModuleCategory.RENDER, true);
        add("fast_clouds", "Fast Clouds", "Uses the light cloud path and avoids unnecessary cloud updates.", ModuleCategory.RENDER, true);
        add("fog_control", "Fog Control", "Keeps fog calculations on the lightweight client path.", ModuleCategory.RENDER, true);
        add("animation_smoother", "Animation Smoother", "Batches small animation updates to reduce micro-stutter.", ModuleCategory.RENDER, true);
        add("item_model_optimizer", "Item Model Optimizer", "Reduces redundant transforms for held and dropped items.", ModuleCategory.RENDER, true);
        add("biome_blend_budget", "Biome Blend Budget", "Limits expensive biome color blending around the camera.", ModuleCategory.RENDER, true);
        add("light_engine_cache", "Light Engine Cache", "Reuses stable client light samples.", ModuleCategory.RENDER, true);
        add("shader_guard", "Shader Guard", "Protects frame pacing when an optional shader pipeline is overloaded.", ModuleCategory.RENDER, true);
        add("debug_overlay", "Debug Overlay", "Shows Nova telemetry without enabling Minecraft's heavy debug screen.", ModuleCategory.RENDER, false);

        // World: all local/client-side; no packet or server simulation changes.
        add("simulation_distance_budget", "Simulation Budget", "Caps local simulation distance at 8 chunks while active.", ModuleCategory.WORLD, false);
        add("chunk_preloader", "Chunk Preloader", "Warms the next camera ring during low-pressure frames.", ModuleCategory.WORLD, true);
        add("chunk_cache", "Chunk Cache", "Keeps nearby chunk meshes hot for quick camera turns.", ModuleCategory.WORLD, true);
        add("smart_render_distance", "Smart Render Distance", "Adapts visual distance to the current frame budget.", ModuleCategory.WORLD, false);
        add("spawn_limiter", "Spawn Limiter", "Throttles client-only ambient spawn visuals.", ModuleCategory.WORLD, true);
        add("redstone_visuals", "Redstone Visuals", "Reduces client-only redstone animation overhead.", ModuleCategory.WORLD, true);
        add("mob_ai_throttle", "Mob AI Throttle", "Throttles only presentation-side mob brain visuals.", ModuleCategory.WORLD, false);
        add("physics_budget", "Physics Budget", "Stages cosmetic physics work to avoid frame spikes.", ModuleCategory.WORLD, true);
        add("weather_optimizer", "Weather Optimizer", "Batches rain and snow rendering work.", ModuleCategory.WORLD, true);

        // System / safety.
        add("entity_shadows", "Entity Shadows", "Disables entity shadows for a clean, cheaper render.", ModuleCategory.SYSTEM, true);
        add("crash_recovery", "Crash Recovery", "Writes the last module state before a clean client shutdown.", ModuleCategory.SYSTEM, true);
        add("telemetry_shield", "Telemetry Shield", "Keeps Nova local-only; no analytics or network calls.", ModuleCategory.SYSTEM, true);
        add("compatibility_mode", "Compatibility Mode", "Disables invasive hooks when an unknown renderer is present.", ModuleCategory.SYSTEM, true);
        add("safe_fallbacks", "Safe Fallbacks", "Restores vanilla options when a module is turned off.", ModuleCategory.SYSTEM, true);
        add("config_backup", "Config Backup", "Keeps the previous profile available as a local fallback.", ModuleCategory.SYSTEM, true);

        load();
    }

    private static void add(String id, String name, String description, ModuleCategory category, boolean enabled) {
        MODULES.put(id, new NovaModule(id, name, description, category, enabled, null, null));
    }

    public static Collection<NovaModule> all() {
        bootstrap();
        return MODULES.values();
    }

    public static NovaModule get(String id) {
        bootstrap();
        return MODULES.get(id);
    }

    public static boolean enabled(String id) {
        NovaModule module = get(id);
        return module != null && module.enabled();
    }

    public static int enabledCount() {
        return (int) all().stream().filter(NovaModule::enabled).count();
    }

    public static int totalCount() {
        return all().size();
    }

    public static boolean isShowHud() {
        return showHud;
    }

    public static void setShowHud(boolean value) {
        showHud = value;
        save();
    }

    public static boolean isCompactHud() {
        return compactHud;
    }

    public static void setCompactHud(boolean value) {
        compactHud = value;
        save();
    }

    public static void toggle(String id, MinecraftClient client) {
        NovaModule module = get(id);
        if (module == null) return;
        module.toggle(client);
        save();
    }

    public static void set(String id, boolean value, MinecraftClient client) {
        NovaModule module = get(id);
        if (module == null) return;
        module.setEnabled(value, client);
        save();
    }

    public static void applyPreset(String preset, MinecraftClient client) {
        bootstrap();
        for (NovaModule module : MODULES.values()) {
            boolean value = switch (preset) {
                case "competitive" -> module.category() != ModuleCategory.WORLD
                        && !module.id().equals("motion_blur")
                        && !module.id().equals("debug_overlay");
                case "balanced" -> !module.id().equals("motion_blur")
                        && !module.id().equals("render_distance_budget")
                        && !module.id().equals("simulation_distance_budget")
                        && !module.id().equals("debug_overlay");
                case "cinematic" -> !module.id().equals("render_distance_budget")
                        && !module.id().equals("simulation_distance_budget")
                        && !module.id().equals("entity_shadows")
                        && !module.id().equals("particle_optimizer")
                        && !module.id().equals("fast_clouds");
                default -> module.enabled();
            };
            module.setEnabled(value, client);
        }
        save();
    }

    public static void tick(MinecraftClient client) {
        bootstrap();
        tickCounter++;
        if (tickCounter % 20 == 0) applySafeVanillaOptions(client);
    }

    private static void applySafeVanillaOptions(MinecraftClient client) {
        // These are the only automatic vanilla option changes. Each is restored when disabled.
        if (enabled("render_distance_budget")) {
            if (originalViewDistance == null) originalViewDistance = client.options.getViewDistance().getValue();
            client.options.getViewDistance().setValue(Math.min(originalViewDistance, 12));
        } else if (originalViewDistance != null) {
            client.options.getViewDistance().setValue(originalViewDistance);
            originalViewDistance = null;
        }

        if (enabled("simulation_distance_budget")) {
            if (originalSimulationDistance == null) originalSimulationDistance = client.options.getSimulationDistance().getValue();
            client.options.getSimulationDistance().setValue(Math.min(originalSimulationDistance, 8));
        } else if (originalSimulationDistance != null) {
            client.options.getSimulationDistance().setValue(originalSimulationDistance);
            originalSimulationDistance = null;
        }

        if (enabled("entity_shadows")) {
            if (originalEntityShadows == null) originalEntityShadows = client.options.getEntityShadows().getValue();
            client.options.getEntityShadows().setValue(false);
        } else if (originalEntityShadows != null) {
            client.options.getEntityShadows().setValue(originalEntityShadows);
            originalEntityShadows = null;
        }

        if (enabled("particle_optimizer")) {
            if (originalParticles == null) originalParticles = client.options.getParticles().getValue();
            client.options.getParticles().setValue(ParticlesMode.MINIMAL);
        } else if (originalParticles != null) {
            client.options.getParticles().setValue(originalParticles);
            originalParticles = null;
        }

        if (enabled("fast_clouds")) {
            if (originalClouds == null) originalClouds = client.options.getCloudRenderMode().getValue();
            client.options.getCloudRenderMode().setValue(CloudRenderMode.FAST);
        } else if (originalClouds != null) {
            client.options.getCloudRenderMode().setValue(originalClouds);
            originalClouds = null;
        }

        if (enabled("biome_blend_budget")) {
            if (originalBiomeBlend == null) originalBiomeBlend = client.options.getBiomeBlendRadius().getValue();
            client.options.getBiomeBlendRadius().setValue(Math.min(originalBiomeBlend, 0));
        } else if (originalBiomeBlend != null) {
            client.options.getBiomeBlendRadius().setValue(originalBiomeBlend);
            originalBiomeBlend = null;
        }

        if (enabled("dynamic_fps")) {
            if (originalMaxFps == null) originalMaxFps = client.options.getMaxFps().getValue();
            if (!client.isWindowFocused()) client.options.getMaxFps().setValue(Math.min(originalMaxFps, 30));
            else client.options.getMaxFps().setValue(originalMaxFps);
        } else if (originalMaxFps != null) {
            client.options.getMaxFps().setValue(originalMaxFps);
            originalMaxFps = null;
        }
    }

    private static void load() {
        if (!Files.exists(CONFIG_FILE)) return;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_FILE)) {
            properties.load(input);
            showHud = Boolean.parseBoolean(properties.getProperty("showHud", "true"));
            compactHud = Boolean.parseBoolean(properties.getProperty("compactHud", "false"));
            MODULES.values().forEach(module -> {
                String stored = properties.getProperty("module." + module.id());
                if (stored != null) module.forceState(Boolean.parseBoolean(stored));
            });
        } catch (IOException ignored) {
            // A corrupt or locked config should never prevent the game from launching.
        }
    }

    public static void save() {
        if (!bootstrapped) return;
        Properties properties = new Properties();
        properties.setProperty("showHud", Boolean.toString(showHud));
        properties.setProperty("compactHud", Boolean.toString(compactHud));
        MODULES.values().forEach(module -> properties.setProperty("module." + module.id(), Boolean.toString(module.enabled())));
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_FILE)) {
                properties.store(output, "NovaClient local profile — no telemetry");
            }
        } catch (IOException ignored) {
            // Saving is best-effort; the active session remains usable.
        }
    }

    public static String rendererStatus() {
        return Compatibility.rendererLabel();
    }

    public static String sodiumVersion() {
        return Compatibility.sodiumVersion();
    }
}
