package com.shadowslave.registry;

import com.shadowslave.ShadowSlave;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Creative tab gathering the mod's blocks. */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ShadowSlave.MODID);

    public static final RegistryObject<CreativeModeTab> SHADOW_SLAVE = TABS.register("shadow_slave",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ShadowSlave.MODID))
                    .icon(() -> new ItemStack(ModBlocks.CRIMSON_SPIRE_BLOCK.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.ASHEN_SAND.get());
                        output.accept(ModBlocks.PETRIFIED_SLEEPER.get());
                        output.accept(ModBlocks.CRIMSON_SPIRE_BLOCK.get());
                        output.accept(ModBlocks.CRIMSON_CORAL.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}
