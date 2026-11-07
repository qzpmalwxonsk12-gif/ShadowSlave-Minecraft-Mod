package com.shadowslave.soul;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/** Holds the {@link Capability} token used to attach {@link SoulData} to players. */
public final class SoulCapability {
    public static final Capability<SoulData> SOUL = CapabilityManager.get(new CapabilityToken<>() {});

    private SoulCapability() {}
}
