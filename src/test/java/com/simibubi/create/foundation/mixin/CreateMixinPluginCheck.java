package com.simibubi.create.foundation.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;

public final class CreateMixinPluginCheck {
    public static void main(String[] args) {
        if (!FabricLoader.getInstance().getModContainer("porting_lib_chunk_loading").isEmpty())
            throw new AssertionError("Standalone regression check must not load chunk_loading");
        if (new CreateMixinPlugin()
                .shouldApplyMixin(
                        ServerLevel.class.getName(),
                        "com.simibubi.create.foundation.mixin.fabric.ServerLevelForcedChunksMixin"))
            throw new AssertionError("Forced-chunk compatibility mixin must skip an absent module");
        System.out.println("Mixin applicability: absent Porting Lib chunk loading OK");
    }
}
