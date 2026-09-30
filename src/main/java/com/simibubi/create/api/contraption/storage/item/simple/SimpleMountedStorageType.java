package com.simibubi.create.api.contraption.storage.item.simple;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public abstract class SimpleMountedStorageType<T extends SimpleMountedStorage>
        extends MountedItemStorageType<SimpleMountedStorage> {
    protected SimpleMountedStorageType(MapCodec<T> codec) {
        super(codec);
    }

    @Override
    @Nullable
    public SimpleMountedStorage mount(
            Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        SlottedStorage<ItemVariant> storage = this.getStorage(level, state, pos, be);
        return this.createStorage(storage);
    }

    protected SlottedStorage<ItemVariant> getStorage(
            Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, be, null);
        // make sure the storage is slotted so new contents can be moved over on disassembly
        return storage instanceof SlottedStorage<ItemVariant> slotted ? slotted : null;
    }

    @Nullable
    protected SimpleMountedStorage createStorage(SlottedStorage<ItemVariant> storage) {
        return new SimpleMountedStorage(storage);
    }

    public static final class Impl extends SimpleMountedStorageType<SimpleMountedStorage> {
        public Impl() {
            super(SimpleMountedStorage.CODEC);
        }
    }
}
