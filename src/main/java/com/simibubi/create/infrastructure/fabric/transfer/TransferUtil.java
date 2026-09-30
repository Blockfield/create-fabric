package com.simibubi.create.infrastructure.fabric.transfer;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Predicate;

public class TransferUtil {
    public static long insert(Storage<FluidVariant> storage, FluidStack stack) {
        try (Transaction t = Transaction.openOuter()) {
            long inserted = insert(storage, stack, t);
            t.commit();
            return inserted;
        }
    }

    public static long insert(Storage<ItemVariant> storage, ItemStack stack) {
        try (Transaction t = Transaction.openOuter()) {
            long inserted = insert(storage, stack, t);
            t.commit();
            return inserted;
        }
    }

    public static long insert(
            Storage<FluidVariant> storage, FluidStack stack, TransactionContext ctx) {
        return storage.insert(stack.getVariant(), stack.getAmount(), ctx);
    }

    public static long insert(
            Storage<ItemVariant> storage, ItemStack stack, TransactionContext ctx) {
        return storage.insert(ItemVariant.of(stack), stack.getCount(), ctx);
    }

    @Nullable
    public static <T extends TransferVariant<?>> ResourceAmount<T> extractAny(
            Storage<T> storage, long maxAmount) {
        return commit(t -> StorageUtil.extractAny(storage, maxAmount, t));
    }

    @Nullable
    public static <T extends TransferVariant<?>> ResourceAmount<T> extractMatching(
            Storage<T> storage, Predicate<T> predicate, long maxAmount, TransactionContext ctx) {
        T resourceExtracting = null;
        long extracted = 0;

        for (StorageView<T> view : storage.nonEmptyViews()) {
            T resource = view.getResource();

            // see if a resource has already been chosen
            if (resourceExtracting != null && !resourceExtracting.equals(resource)) continue;

            // if one hasn't, see if this one matches
            if (resourceExtracting == null && predicate.test(resource)) {
                resourceExtracting = resource;
            } else {
                // nope, skip
                continue;
            }

            extracted += view.extract(resource, maxAmount - extracted, ctx);
            if (extracted >= maxAmount) {
                return new ResourceAmount<>(resource, extracted);
            }
        }

        return resourceExtracting != null
                ? new ResourceAmount<>(resourceExtracting, extracted)
                : null;
    }

    @Nullable
    public static Storage<ItemVariant> getItemStorage(BlockEntity be) {
        return getItemStorage(be, null);
    }

    @Nullable
    public static Storage<ItemVariant> getItemStorage(BlockEntity be, @Nullable Direction side) {
        return ItemStorage.SIDED.find(
                be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
    }

    @Nullable
    public static Storage<ItemVariant> getItemStorage(Level level, BlockPos pos) {
        return getItemStorage(level, pos, null);
    }

    @Nullable
    public static Storage<ItemVariant> getItemStorage(
            Level level, BlockPos pos, @Nullable Direction side) {
        return ItemStorage.SIDED.find(level, pos, side);
    }

    @Nullable
    public static Storage<FluidVariant> getFluidStorage(BlockEntity be) {
        return getFluidStorage(be, null);
    }

    @Nullable
    public static Storage<FluidVariant> getFluidStorage(BlockEntity be, @Nullable Direction side) {
        return FluidStorage.SIDED.find(
                be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
    }

    @Nullable
    public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos) {
        return getFluidStorage(level, pos, null);
    }

    @Nullable
    public static Storage<FluidVariant> getFluidStorage(
            Level level, BlockPos pos, @Nullable Direction side) {
        return FluidStorage.SIDED.find(level, pos, side);
    }

    @Nullable
    public static Storage<FluidVariant> getFluidStorage(
            Level level, BlockPos pos, @Nullable BlockEntity be, @Nullable Direction side) {
        return FluidStorage.SIDED.find(level, pos, level.getBlockState(pos), be, side);
    }

    public static OptionalLong firstCapacity(Storage<?> storage) {
        for (StorageView<?> view : storage) {
            return OptionalLong.of(view.getCapacity());
        }
        return OptionalLong.empty();
    }

    public static <T> void clear(Storage<T> storage) {
        try (Transaction t = Transaction.openOuter()) {
            for (StorageView<T> view : storage.nonEmptyViews()) {
                view.extract(view.getResource(), view.getAmount(), t);
            }
            t.commit();
        }
    }

    public static <T> T commit(Function<TransactionContext, T> function) {
        try (Transaction t = Transaction.openOuter()) {
            T value = function.apply(t);
            t.commit();
            return value;
        }
    }

    /** Fabric transfer amounts are longs; item/tooltip APIs still want ints. */
    public static int truncateLong(long amount) {
        return (int) Math.min(amount, Integer.MAX_VALUE);
    }

    public static FluidStack extractAnyFluid(Storage<FluidVariant> storage, long maxAmount) {
        ResourceAmount<FluidVariant> extracted = extractAny(storage, maxAmount);
        return extracted == null ? FluidStack.EMPTY : new FluidStack(extracted);
    }

    /** Contents of the first non-empty view, or {@link FluidStack#EMPTY}. */
    public static FluidStack firstOrEmpty(Storage<FluidVariant> storage) {
        for (StorageView<FluidVariant> view : storage.nonEmptyViews()) return new FluidStack(view);
        return FluidStack.EMPTY;
    }

    public static long totalCapacity(Storage<?> storage) {
        long total = 0;
        for (StorageView<?> view : storage) total += view.getCapacity();
        return total;
    }

    /** Simulated extraction of everything the storage will give up, as item stacks. */
    public static List<ItemStack> extractAllAsStacks(Storage<ItemVariant> storage) {
        List<ItemStack> stacks = new ArrayList<>();
        TransactionContext current = Transaction.getCurrentUnsafe();
        // not committed: the transaction is rolled back on close, so this only simulates
        try (Transaction t =
                current == null ? Transaction.openOuter() : Transaction.openNested(current)) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant resource = view.getResource();
                long extracted = view.extract(resource, Long.MAX_VALUE, t);
                while (extracted > 0) {
                    int count =
                            truncateLong(
                                    Math.min(
                                            extracted,
                                            resource.getItem().getDefaultMaxStackSize()));
                    stacks.add(resource.toStack(count));
                    extracted -= count;
                }
            }
        }
        return stacks;
    }

    /** Extracts in its own committed transaction. */
    public static <T> long extract(Storage<T> storage, T resource, long amount) {
        try (Transaction t = Transaction.openOuter()) {
            long extracted = storage.extract(resource, amount, t);
            t.commit();
            return extracted;
        }
    }

    public static ItemStack extractAnyItem(
            Storage<ItemVariant> storage, long maxAmount, TransactionContext ctx) {
        ResourceAmount<ItemVariant> resource = StorageUtil.extractAny(storage, maxAmount, ctx);
        return resource == null
                ? ItemStack.EMPTY
                : resource.resource().toStack(truncateLong(resource.amount()));
    }

    public static <T> T simulate(Function<TransactionContext, T> function) {
        try (Transaction t = Transaction.openOuter()) {
            return function.apply(t);
        }
    }
}
