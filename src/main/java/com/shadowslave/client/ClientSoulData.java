package com.shadowslave.client;

import com.shadowslave.soul.SoulData;
import net.minecraft.nbt.CompoundTag;

/**
 * Client-side mirror of the local player's {@link SoulData}.
 * Updated whenever the server sends a sync packet; read by the Soul Core UI.
 */
public final class ClientSoulData {

    private static final SoulData DATA = new SoulData();

    private ClientSoulData() {}

    public static void set(CompoundTag tag) {
        if (tag != null) {
            DATA.fromNBT(tag);
        }
    }

    public static SoulData get() {
        return DATA;
    }
}
