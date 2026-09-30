package com.simibubi.create.compat.computercraft;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import java.util.function.Function;

public class ComputerCraftProxy {

    // CC: Tweaked is not in this pack, so the peripheral implementations were dropped and every
    // block entity gets the inert fallback behaviour.
    private static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> fallbackFactory;

    public static void register() {
        fallbackFactory = FallbackComputerBehaviour::new;
    }

    public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
        return fallbackFactory.apply(sbe);
    }
}
