package com.shadowslave.client;

import com.shadowslave.ShadowSlave;
import com.shadowslave.client.gui.SoulCoreScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side Forge-bus handlers:
 *  - Opening the Soul Core screen via the keybind.
 *  - Adding a "Soul Core" button to the vanilla inventory so it can be switched to.
 */
@Mod.EventBusSubscriber(modid = ShadowSlave.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Open directly when the keybind is pressed and no screen is up.
        while (KeyBindings.OPEN_SOUL_CORE.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new SoulCoreScreen());
            }
        }
    }

    /** Adds a button to the inventory screen that switches to the Soul Core UI. */
    @SubscribeEvent
    public static void onInventoryInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen invScreen)) return;

        int x = invScreen.getGuiLeft();
        int y = invScreen.getGuiTop() - 22; // sits just above the inventory window

        Button button = Button.builder(
                        Component.literal("❖ Soul Core"),
                        b -> Minecraft.getInstance().setScreen(new SoulCoreScreen()))
                .bounds(x, y, 90, 18)
                .build();

        event.addListener(button);
    }
}
