package com.shadowslave.client;

import net.minecraftforge.eventbus.api.IEventBus;

/** Entry point for client-only registration, invoked from the main mod constructor. */
public final class ClientSetup {

    private ClientSetup() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(KeyBindings::register);
    }
}
