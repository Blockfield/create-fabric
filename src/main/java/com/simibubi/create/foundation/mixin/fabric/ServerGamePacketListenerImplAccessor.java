package com.simibubi.create.foundation.mixin.fabric;

import net.minecraft.server.network.ServerGamePacketListenerImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
    @Accessor("aboveGroundTickCount")
    void create$setAboveGroundTickCount(int ticks);

    @Accessor("aboveGroundVehicleTickCount")
    void create$setAboveGroundVehicleTickCount(int ticks);
}
