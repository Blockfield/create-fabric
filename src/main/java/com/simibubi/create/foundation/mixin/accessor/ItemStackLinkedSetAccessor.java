package com.simibubi.create.foundation.mixin.accessor;

import it.unimi.dsi.fastutil.Hash.Strategy;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackLinkedSet.class)
public interface ItemStackLinkedSetAccessor {
    @Accessor
    static Strategy<? super ItemStack> getTYPE_AND_TAG() {
        throw new AbstractMethodError();
    }
}
