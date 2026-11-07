package com.shadowslave.world.feature;

import com.mojang.serialization.Codec;
import com.shadowslave.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Places a petrified Sleeper statue. Most stand 2–3 blocks tall on an ashen drift; some have
 * already crumbled to a single fallen stone — the countless dreamers who never woke.
 */
public class SleeperStatueFeature extends Feature<NoneFeatureConfiguration> {

    public SleeperStatueFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos origin = ctx.origin();           // first space above the ocean floor / ground

        BlockPos ground = origin.below();
        if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
            return false;                          // need solid footing
        }

        BlockState body = ModBlocks.PETRIFIED_SLEEPER.get().defaultBlockState();
        BlockState ash = ModBlocks.ASHEN_SAND.get().defaultBlockState();

        // ashen drift around the base
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (rand.nextFloat() < 0.55F) {
                    BlockPos g = ground.offset(dx, 0, dz);
                    if (level.getBlockState(g).isFaceSturdy(level, g, Direction.UP)) {
                        m.set(origin.getX() + dx, origin.getY(), origin.getZ() + dz);
                        if (level.isEmptyBlock(m) || level.getBlockState(m).getFluidState().isSource()) {
                            setBlock(level, m, ash);
                        }
                    }
                }
            }
        }

        // the figure itself
        int height = rand.nextFloat() < 0.4F ? 1 : (2 + rand.nextInt(2)); // 1 (fallen) or 2–3
        for (int i = 0; i < height; i++) {
            m.set(origin.getX(), origin.getY() + i, origin.getZ());
            setBlock(level, m, body);
        }
        return true;
    }
}
