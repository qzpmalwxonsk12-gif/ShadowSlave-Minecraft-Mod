package com.shadowslave.network;

import com.shadowslave.ShadowSlave;
import com.shadowslave.soul.SoulCapability;
import com.shadowslave.soul.SoulData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Registers the mod's network channel and provides helpers for sending packets. */
public class ModMessages {

    private static final String PROTOCOL_VERSION = "1";

    @SuppressWarnings("removal")
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ShadowSlave.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    private static int nextId() {
        return id++;
    }

    public static void register() {
        INSTANCE.messageBuilder(SyncSoulS2CPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncSoulS2CPacket::new)
                .encoder(SyncSoulS2CPacket::toBytes)
                .consumerMainThread(SyncSoulS2CPacket::handle)
                .add();
    }

    /** Push a player's current Soul Core state to their client. */
    public static void syncToClient(ServerPlayer player) {
        player.getCapability(SoulCapability.SOUL).ifPresent(data ->
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncSoulS2CPacket(data.toNBT())));
    }

    /** Convenience: run an action on a player's SoulData, then sync the result to their client. */
    public static void update(ServerPlayer player, java.util.function.Consumer<SoulData> action) {
        player.getCapability(SoulCapability.SOUL).ifPresent(data -> {
            action.accept(data);
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncSoulS2CPacket(data.toNBT()));
        });
    }
}
