package com.shadowslave.soul;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Attaches a single {@link SoulData} instance to a player and handles its (de)serialization. */
public class SoulProvider implements ICapabilitySerializable<CompoundTag> {

    private final SoulData data = new SoulData();
    private final LazyOptional<SoulData> optional = LazyOptional.of(() -> data);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == SoulCapability.SOUL ? optional.cast() : LazyOptional.empty();
    }

    public void invalidate() {
        optional.invalidate();
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.toNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.fromNBT(nbt);
    }
}
