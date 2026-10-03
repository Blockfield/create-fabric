package com.simibubi.create.foundation.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerLevelForcedChunksMixin {
    // Porting Lib beta.90 redirects isEmpty() to hasForcedChunks(), reversing the idle-world gate.
    @ModifyExpressionValue(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/longs/LongSet;isEmpty()Z",
                            remap = false),
            require = 1,
            allow = 1)
    private static boolean create$forcedChunkSetIsEmpty(boolean hasForcedChunks) {
        return !hasForcedChunks;
    }
}
