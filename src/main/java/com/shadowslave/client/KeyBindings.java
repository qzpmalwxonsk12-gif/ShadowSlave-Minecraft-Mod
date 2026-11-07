package com.shadowslave.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.shadowslave.ShadowSlave;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/** Keybind for opening the Soul Core screen directly (default: K). */
public class KeyBindings {

    public static final String CATEGORY = "key.categories." + ShadowSlave.MODID;

    public static final KeyMapping OPEN_SOUL_CORE = new KeyMapping(
            "key." + ShadowSlave.MODID + ".open_soul_core",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SOUL_CORE);
    }
}
