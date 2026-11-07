package com.shadowslave;

import com.shadowslave.network.ModMessages;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Main entry point for the Shadow Slave mod.
 *
 * The mod adds a "Soul Core" progression system inspired by the novel:
 *  - Killing creatures feeds Essence into your Soul Core.
 *  - A separate, naturally regenerating pool (Aether) powers skills.
 *  - Nether Stars expand both your Soul Core and Aether capacity.
 *  - Sleeping can drag you into the Forgotten Shore, where you Awaken and rise through the ranks.
 *
 * The current focus is the dedicated Soul Core UI ({@link com.shadowslave.client.gui.SoulCoreScreen}).
 */
@Mod(ShadowSlave.MODID)
public class ShadowSlave {
    public static final String MODID = "shadowslave";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ShadowSlave() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::commonSetup);

        // Content registries
        com.shadowslave.registry.ModBlocks.BLOCKS.register(modBus);
        com.shadowslave.registry.ModBlocks.ITEMS.register(modBus);
        com.shadowslave.registry.ModCreativeTabs.TABS.register(modBus);
        com.shadowslave.registry.ModFeatures.FEATURES.register(modBus);

        // Client-only setup (keybinds, screen registration) is registered lazily so the
        // dedicated server never classloads client code.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.shadowslave.client.ClientSetup.init(modBus));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModMessages::register);
        LOGGER.info("[Shadow Slave] Soul Core system initialising...");
    }
}
