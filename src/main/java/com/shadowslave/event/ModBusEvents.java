package com.shadowslave.event;

import com.shadowslave.ShadowSlave;
import com.shadowslave.soul.SoulData;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Mod-bus lifecycle handlers (capability registration). */
@Mod.EventBusSubscriber(modid = ShadowSlave.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(SoulData.class);
    }
}
