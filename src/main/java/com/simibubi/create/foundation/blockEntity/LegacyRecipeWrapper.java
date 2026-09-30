package com.simibubi.create.foundation.blockEntity;

import com.simibubi.create.infrastructure.fabric.transfer.item.SlottedStackStorage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import org.jetbrains.annotations.ApiStatus;

/**
 * Обёртка хранилища под RecipeInput. Container не реализует намеренно: у Container и RecipeInput
 * isEmpty/getItem маппятся в разные intermediary-имена, tiny-remapper на таком классе падает.
 */
public class LegacyRecipeWrapper implements RecipeInput {

    protected final SlottedStackStorage inv;

    public LegacyRecipeWrapper(SlottedStackStorage inv) {
        this.inv = inv;
    }

    @Override
    @ApiStatus.NonExtendable
    @ApiStatus.Internal
    public int size() {
        return getContainerSize();
    }

    /** Returns the size of this inventory. */
    public int getContainerSize() {
        return inv.getSlotCount();
    }

    /**
     * Returns the stack in this slot. This stack should be a modifiable reference, not a copy of a
     * stack in your inventory.
     */
    @Override
    public ItemStack getItem(int slot) {
        return inv.getStackInSlot(slot);
    }

    /**
     * Attempts to remove n items from the specified slot. Returns the split stack that was removed.
     * Modifies the inventory.
     */
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = inv.getStackInSlot(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.split(count);
    }

    /** Sets the contents of this slot to the provided stack. */
    public void setItem(int slot, ItemStack stack) {
        inv.setStackInSlot(slot, stack);
    }

    /** Removes the stack contained in this slot from the underlying handler, and returns it. */
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack s = getItem(index);
        if (s.isEmpty()) return ItemStack.EMPTY;
        setItem(index, ItemStack.EMPTY);
        return s;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inv.getSlotCount(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    public boolean canPlaceItem(int slot, ItemStack stack) {
        return inv.isItemValid(slot, stack);
    }

    public void clearContent() {
        for (int i = 0; i < inv.getSlotCount(); i++) {
            inv.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    // The following methods are never used by vanilla in crafting.  They are defunct as mods need
    // not override them.
    public int getMaxStackSize() {
        return 0;
    }

    public void setChanged() {}

    public boolean stillValid(Player player) {
        return false;
    }

    public void startOpen(Player player) {}

    public void stopOpen(Player player) {}
}
