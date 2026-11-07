package com.shadowslave.world.feature;

import com.mojang.serialization.Codec;
import com.shadowslave.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Builds a tall, tapering crimson spire — an ominous landmark looming over the Shore. */
public class CrimsonSpireFeature extends Feature<NoneFeatureConfiguration> {

    public CrimsonSpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos origin = ctx.origin();
        BlockState crimson = ModBlocks.CRIMSON_SPIRE_BLOCK.get().defaultBlockState();

        int height = 22 + rand.nextInt(22);            // 22 – 43 tall
        double baseRadius = 3.0 + rand.nextDouble() * 1.8; // 3.0 – 4.8
        int bx = origin.getX();
        int by = origin.getY() - 2;                    // sink the foot into the ground
        int bz = origin.getZ();

        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy < height; dy++) {
            int y = by + dy;
            if (y >= level.getMaxBuildHeight()) break;

            double t = (double) dy / height;
            double r = baseRadius * (1.0 - t);         // taper to a point
            int ri = (int) Math.ceil(r);
            double r2 = r * r + 0.6;

            for (int dx = -ri; dx <= ri; dx++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    if (dx * dx + dz * dz <= r2) {
                        m.set(bx + dx, y, bz + dz);
                        setBlock(level, m, crimson);
                    }
                }
            }
        }
        // crowning block at the tip
        m.set(bx, Math.min(by + height, level.getMaxBuildHeight() - 1), bz);
        setBlock(level, m, crimson);
        return true;
    }
}
