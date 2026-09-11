package com.novaclient;

import com.novaclient.module.ModuleManager;
import com.novaclient.ui.NovaHud;
import com.novaclient.ui.NovaScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class NovaClient implements ClientModInitializer {
    public static final String MOD_ID = "novaclient";
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.bootstrap();
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.novaclient.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.novaclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new NovaScreen(null));
            }
            ModuleManager.tick(client);
        });
        NovaHud.register();
    }
}
