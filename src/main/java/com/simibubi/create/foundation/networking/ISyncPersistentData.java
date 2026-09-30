package com.simibubi.create.foundation.networking;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.fabric.CustomDataHolder;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;

public interface ISyncPersistentData {

    void onPersistentDataUpdated();

    default void syncPersistentDataWithTracking(Entity self) {
        CatnipServices.NETWORK.sendToClientsTrackingEntity(self, new PersistentDataPacket(self));
    }

    record PersistentDataPacket(int entityId, CompoundTag readData)
            implements ClientboundPacketPayload {
        public static final StreamCodec<FriendlyByteBuf, PersistentDataPacket> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        PersistentDataPacket::entityId,
                        ByteBufCodecs.COMPOUND_TAG,
                        PersistentDataPacket::readData,
                        PersistentDataPacket::new);

        public PersistentDataPacket(Entity entity) {
            this(entity.getId(), CustomDataHolder.of(entity));
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void handle(LocalPlayer player) {
            Entity entityByID = player.clientLevel.getEntity(entityId);
            CompoundTag data = CustomDataHolder.of(entityByID);
            new HashSet<>(data.getAllKeys()).forEach(data::remove);
            data.merge(readData);
            if (!(entityByID instanceof ISyncPersistentData)) return;
            ((ISyncPersistentData) entityByID).onPersistentDataUpdated();
        }

        @Override
        public PacketTypeProvider getTypeProvider() {
            return AllPackets.PERSISTENT_DATA;
        }
    }
}
