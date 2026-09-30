package com.simibubi.create.foundation.mixin.fabric;

import com.simibubi.create.foundation.fabric.CustomDataHolder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityCustomDataMixin implements CustomDataHolder {

    @Unique private CompoundTag create$customData;

    @Override
    public CompoundTag create$getCustomData() {
        if (create$customData == null) create$customData = new CompoundTag();
        return create$customData;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void create$saveCustomData(
            CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (create$customData != null && !create$customData.isEmpty())
            tag.put("CreateData", create$customData);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void create$loadCustomData(
            CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag.contains("CreateData", 10)) create$customData = tag.getCompound("CreateData");
    }
}
