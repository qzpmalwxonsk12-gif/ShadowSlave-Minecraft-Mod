package com.shadowslave.registry;

import com.shadowslave.ShadowSlave;
import com.shadowslave.world.feature.CoralLabyrinthFeature;
import com.shadowslave.world.feature.CrimsonSpireFeature;
import com.shadowslave.world.feature.SleeperStatueFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Custom worldgen feature types used by the Forgotten Shore biome. */
public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, ShadowSlave.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CRIMSON_SPIRE =
            FEATURES.register("crimson_spire", () -> new CrimsonSpireFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SLEEPER_STATUE =
            FEATURES.register("sleeper_statue", () -> new SleeperStatueFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CORAL_LABYRINTH =
            FEATURES.register("coral_labyrinth", () -> new CoralLabyrinthFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}
}
