package com.shadowslave.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Tide of the Forgotten Shore.
 *
 * The sea swells with the dark and withdraws with the light: water rises as night falls and recedes
 * by day, tracking the sun. To stay affordable, only a disc around each player is reconciled, only a
 * few times a second, and only air&lt;-&gt;water within the tidal band is ever changed — solid terrain,
 * statues and coral are never touched.
 */
public final class TideManager {

    public static final int LOW_TIDE_Y = 60;    // daytime low-water mark
    public static final int HIGH_TIDE_Y = 70;   // midnight high-water mark
    private static final int RADIUS = 26;
    private static final int RADIUS_SQ = RADIUS * RADIUS;
    private static final long INTERVAL = 20L;    // reconcile ~once per second

    private TideManager() {}

    public static void tick(ServerLevel level) {
        if (level.getGameTime() % INTERVAL != 0L) return;
        if (level.players().isEmpty()) return;

        int tideY = currentTideHeight(level);
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

        for (ServerPlayer player : level.players()) {
            int px = player.getBlockX();
            int pz = player.getBlockZ();

            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (dx * dx + dz * dz > RADIUS_SQ) continue;
                    int x = px + dx;
                    int z = pz + dz;

                    for (int y = LOW_TIDE_Y; y <= HIGH_TIDE_Y; y++) {
                        m.set(x, y, z);
                        BlockState cur = level.getBlockState(m);
                        if (y <= tideY) {
                            // below the waterline: flood empty space
                            if (cur.isAir()) {
                                level.setBlock(m, water, 2);
                            }
                        } else {
                            // above the waterline: drain only the tide's own water
                            if (cur.is(Blocks.WATER)) {
                                level.setBlock(m, air, 2);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Smoothly map the time of day to a waterline: lowest at noon, highest at midnight. */
    public static int currentTideHeight(ServerLevel level) {
        long dayTime = level.getDayTime() % 24000L;
        double theta = 2.0 * Math.PI * ((dayTime - 6000.0) / 24000.0);
        double factor = (1.0 - Math.cos(theta)) / 2.0; // 0 at noon -> 1 at midnight
        return (int) Math.round(LOW_TIDE_Y + (HIGH_TIDE_Y - LOW_TIDE_Y) * factor);
    }
}
