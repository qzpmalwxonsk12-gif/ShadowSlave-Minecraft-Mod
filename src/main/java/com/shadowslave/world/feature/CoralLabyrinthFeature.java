package com.shadowslave.world.feature;

import com.mojang.serialization.Codec;
import com.shadowslave.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Tangles the Forgotten Shore's sea floor into a labyrinth of crimson coral walls.
 *
 * Walls follow a world-aligned grid (so they continue between chunks) with random doorways,
 * and are only raised on the submerged floor at or below sea level — so they sit underwater at
 * high tide and become a navigable maze as the tide recedes.
 */
public class CoralLabyrinthFeature extends Feature<NoneFeatureConfiguration> {

    private static final int CELL = 4;            // room size between walls
    private static final float DOORWAY_CHANCE = 0.22F;

    public CoralLabyrinthFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockState coral = ModBlocks.CRIMSON_CORAL.get().defaultBlockState();

        int seaLevel = level.getSeaLevel();
        int chunkMinX = (ctx.origin().getX() >> 4) << 4;
        int chunkMinZ = (ctx.origin().getZ() >> 4) << 4;

        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int x = chunkMinX; x < chunkMinX + 16; x++) {
            for (int z = chunkMinZ; z < chunkMinZ + 16; z++) {
                boolean onGrid = Math.floorMod(x, CELL) == 0 || Math.floorMod(z, CELL) == 0;
                if (!onGrid) continue;
                if (rand.nextFloat() < DOORWAY_CHANCE) continue;   // leave a gap to walk through

                int floorTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z); // first space above floor
                if (floorTop - 1 > seaLevel) continue;             // only on the submerged sea floor

                int wallHeight = 2 + rand.nextInt(2);              // 2–3 tall
                for (int i = 0; i < wallHeight; i++) {
                    m.set(x, floorTop + i, z);
                    setBlock(level, m, coral);
                    placedAny = true;
                }
            }
        }
        return placedAny;
    }
}
