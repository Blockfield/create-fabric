package com.simibubi.create.compat.thresholdSwitch;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ThresholdSwitchCompat {

    boolean isFromThisMod(BlockEntity blockEntity);

    long getSpaceInSlot(StorageView<ItemVariant> inv);
}
