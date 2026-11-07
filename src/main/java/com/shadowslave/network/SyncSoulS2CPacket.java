package com.shadowslave.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: full snapshot of the player's Soul Core state. */
public class SyncSoulS2CPacket {

    private final CompoundTag data;

    public SyncSoulS2CPacket(CompoundTag data) {
        this.data = data;
    }

    public SyncSoulS2CPacket(FriendlyByteBuf buf) {
        this.data = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(data);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() ->
                // Only touch the client-only cache when actually running on a client.
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> com.shadowslave.client.ClientSoulData.set(data)));
        ctx.setPacketHandled(true);
        return true;
    }
}
