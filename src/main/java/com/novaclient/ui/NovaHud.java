package com.novaclient.ui;

import com.novaclient.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Minimal in-game telemetry, intentionally quiet and fully local. */
public final class NovaHud {
    private NovaHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> render(context));
    }

    private static void render(DrawContext context) {
        if (!ModuleManager.isShowHud() || !ModuleManager.enabled("debug_overlay")) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int fps = client.getCurrentFps();
        int x = 12;
        int y = 12;
        int width = ModuleManager.isCompactHud() ? 118 : 172;
        context.fill(x, y, x + width, y + 28, 0xE610171D);
        context.fill(x, y, x + 3, y + 28, 0xFF85F2CB);
        context.drawTextWithShadow(client.textRenderer, Text.literal("NOVA"), x + 12, y + 6, 0xFF85F2CB);
        context.drawTextWithShadow(client.textRenderer, Text.literal(fps + " FPS"), x + width - (fps > 99 ? 45 : 39), y + 6, 0xFFF4F7F6);
        if (!ModuleManager.isCompactHud()) {
            context.drawTextWithShadow(client.textRenderer, Text.literal("  /  " + ModuleManager.enabledCount() + " active"), x + 12, y + 17, 0xFF829097);
        }
    }
}
