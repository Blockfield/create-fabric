package com.simibubi.create.foundation.fabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fabric replacement for porting lib's removed {@code BlockEvents.POST_PROCESS_PLACE}. Fired by
 * {@code foundation.mixin.BlockItemMixin} after a {@link net.minecraft.world.item.BlockItem}
 * successfully placed its block.
 */
@FunctionalInterface
public interface BlockPlacedCallback {

    Event<BlockPlacedCallback> EVENT =
            EventFactory.createArrayBacked(
                    BlockPlacedCallback.class,
                    callbacks ->
                            (context, pos, state) -> {
                                for (BlockPlacedCallback callback : callbacks)
                                    callback.onBlockPlaced(context, pos, state);
                            });

    void onBlockPlaced(BlockPlaceContext context, BlockPos pos, BlockState state);
}
