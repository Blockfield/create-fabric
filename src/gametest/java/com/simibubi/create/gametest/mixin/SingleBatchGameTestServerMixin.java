package com.simibubi.create.gametest.mixin;

import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchFactory;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(value = GameTestServer.class, remap = false)
public abstract class SingleBatchGameTestServerMixin {
    // ponytail: pinned 1.21.1 CI stalls world behavior in later batches; restore splitting only after
    // upstream batch transitions pass the complete suite.
    @Redirect(
            method = "initServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/gametest/framework/GameTestBatchFactory;fromTestFunction(Ljava/util/Collection;Lnet/minecraft/server/level/ServerLevel;)Ljava/util/Collection;",
                            remap = false),
            remap = false)
    private Collection<GameTestBatch> create$singleInitialBatch(
            Collection<TestFunction> functions, ServerLevel level) {
        return GameTestBatchFactory.fromGameTestInfo(functions.size())
                .batch(
                        functions.stream()
                                .map(
                                        function ->
                                                GameTestBatchFactory.toGameTestInfo(
                                                        function, 0, level))
                                .toList());
    }
}
