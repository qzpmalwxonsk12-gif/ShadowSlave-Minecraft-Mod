package com.shadowslave.world;

import com.shadowslave.ShadowSlave;
import com.shadowslave.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Identifies and provides access to the Forgotten Shore — the first Nightmare a Sleeper is dragged
 * into: a desolate, fog-drowned coast under eternal night, ringed by an endless dark sea.
 */
public final class ForgottenShore {

    public static final ResourceKey<Level> FORGOTTEN_SHORE =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(ShadowSlave.MODID, "forgotten_shore"));

    private ForgottenShore() {}

    /**
     * Send a player between the waking world and the Forgotten Shore.
     * From the Shore they claw back to the Overworld; from anywhere else they are pulled in.
     */
    public static void transport(ServerPlayer player) {
        boolean onShore = player.level().dimension().equals(FORGOTTEN_SHORE);
        ResourceKey<Level> destKey = onShore ? Level.OVERWORLD : FORGOTTEN_SHORE;

        ServerLevel destination = player.getServer().getLevel(destKey);
        if (destination == null) {
            ShadowSlave.LOGGER.warn("[Shadow Slave] Forgotten Shore dimension is missing; cannot transport {}",
                    player.getName().getString());
            return;
        }
        player.changeDimension(destination, new ForgottenShoreTeleporter());
    }

    /**
     * Raise a small cluster of petrified Sleeper statues around a player who has just arrived on the
     * Shore — the stone company they wake among.
     */
    public static void buildAwakeningStatue(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        RandomSource rand = player.getRandom();
        BlockState body = ModBlocks.PETRIFIED_SLEEPER.get().defaultBlockState();
        BlockState ash = ModBlocks.ASHEN_SAND.get().defaultBlockState();
        BlockPos feet = player.blockPosition();

        int[][] offsets = {{2, 0}, {-2, 1}, {1, 2}, {-1, -2}, {2, -2}};
        for (int[] off : offsets) {
            if (rand.nextFloat() < 0.30F) continue;       // not every spot is occupied
            int x = feet.getX() + off[0];
            int z = feet.getZ() + off[1];
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos base = new BlockPos(x, y, z);

            level.setBlockAndUpdate(base.below(), ash);
            int height = 2 + rand.nextInt(2);
            for (int i = 0; i < height; i++) {
                level.setBlockAndUpdate(base.above(i), body);
            }
        }
    }
}
