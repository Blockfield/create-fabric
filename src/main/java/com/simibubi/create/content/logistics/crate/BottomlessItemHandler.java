package com.simibubi.create.content.logistics.crate;

import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottomlessItemHandler extends ItemStackHandler
        implements SingleSlotStorage<
                ItemVariant> { // must extend ItemStackHandler for mounted storages

    private Supplier<ItemStack> suppliedItemStack;

    public BottomlessItemHandler(Supplier<ItemStack> suppliedItemStack) {
        super(0);
        this.suppliedItemStack = suppliedItemStack;
        setSize(1); // create slot after setting supplier
    }

    // fabric: this handler is a single infinite slot; the inherited slot storage is never used
    // directly
    @Override
    public ItemStack getStackInSlot(int slot) {
        return getStack();
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {}

    @Override
    public ItemVariant getVariantInSlot(int slot) {
        return getResource();
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return this;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        return List.of(this);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return maxAmount;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        ItemStack stack = getStack();
        if (!resource.matches(stack)) return 0;
        if (!stack.isEmpty()) return Math.min(stack.getMaxStackSize(), maxAmount);
        return 0;
    }

    protected ItemStack getStack() {
        ItemStack stack = suppliedItemStack.get();
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    @Override
    public boolean isResourceBlank() {
        return getStack().isEmpty();
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(getStack());
    }

    @Override
    public long getAmount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    // shortcuts

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return SingleSlotStorage.super.iterator(); // singleton iterator on this
    }

    @Override
    public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
        return this::nonEmptyIterator;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
        return isResourceBlank() ? Collections.emptyIterator() : iterator();
    }
}
