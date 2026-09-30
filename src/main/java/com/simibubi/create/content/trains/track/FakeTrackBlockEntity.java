package com.simibubi.create.content.trains.track;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeTrackBlockEntity extends SyncedBlockEntity {

    int keepAlive;
    private final Map<String, List<AABB>> collisionSources = new HashMap<>();
    private VoxelShape collision = Shapes.empty();

    public VoxelShape collisionShape() {
        return collision;
    }

    public void setCollision(String source, List<AABB> boxes) {
        if (boxes == null
                ? collisionSources.remove(source) == null
                : boxes.equals(collisionSources.put(source, boxes))) return;
        rebuildCollision();
        notifyUpdate();
    }

    private void rebuildCollision() {
        collision = Shapes.empty();
        for (List<AABB> boxes : collisionSources.values())
            for (AABB box : boxes) collision = Shapes.or(collision, Shapes.create(box));
        collision = collision.optimize();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag sources = new CompoundTag();
        collisionSources.forEach(
                (source, boxes) -> {
                    ListTag list = new ListTag();
                    for (AABB box : boxes) {
                        CompoundTag entry = new CompoundTag();
                        entry.putIntArray(
                                "Box",
                                new int[] {
                                    (int) Math.round(box.minX * 16),
                                    (int) Math.round(box.minY * 16),
                                    (int) Math.round(box.minZ * 16),
                                    (int) Math.round(box.maxX * 16),
                                    (int) Math.round(box.maxY * 16),
                                    (int) Math.round(box.maxZ * 16)
                                });
                        list.add(entry);
                    }
                    sources.put(source, list);
                });
        tag.put("TrackCollision", sources);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        collisionSources.clear();
        CompoundTag sources = tag.getCompound("TrackCollision");
        for (String source : sources.getAllKeys()) {
            java.util.ArrayList<AABB> boxes = new java.util.ArrayList<>();
            for (Tag raw : sources.getList(source, Tag.TAG_COMPOUND)) {
                int[] b = ((CompoundTag) raw).getIntArray("Box");
                if (b.length == 6)
                    boxes.add(
                            new AABB(
                                    b[0] / 16.0,
                                    b[1] / 16.0,
                                    b[2] / 16.0,
                                    b[3] / 16.0,
                                    b[4] / 16.0,
                                    b[5] / 16.0));
            }
            collisionSources.put(source, boxes);
        }
        rebuildCollision();
    }

    public FakeTrackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        keepAlive();
    }

    public void randomTick() {
        keepAlive--;
        if (keepAlive > 0) return;
        level.removeBlock(worldPosition, false);
    }

    public void keepAlive() {
        keepAlive = 3;
    }
}
