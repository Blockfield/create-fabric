package com.simibubi.create.content.trains.entity;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

// fabric: carriage data cannot ride a custom entity data serializer here, so it is synced with this
// packet
public record CarriageDataUpdatePacket(int entityId, CarriageSyncData data)
        implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, CarriageDataUpdatePacket>
            STREAM_CODEC =
                    StreamCodec.of(
                            (buf, packet) -> {
                                buf.writeVarInt(packet.entityId());
                                packet.data().write(buf);
                            },
                            buf -> {
                                int entityId = buf.readVarInt();
                                CarriageSyncData data = new CarriageSyncData();
                                data.read(buf);
                                return new CarriageDataUpdatePacket(entityId, data);
                            });

    public CarriageDataUpdatePacket(CarriageContraptionEntity entity) {
        this(entity.getId(), entity.carriageData);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(LocalPlayer player) {
        Entity entity = player.clientLevel.getEntity(entityId);
        if (entity instanceof CarriageContraptionEntity carriage) {
            carriage.onCarriageDataUpdate(data);
        } else {
            Create.LOGGER.error(
                    "Invalid CarriageDataUpdatePacket for non-carriage entity: {}", entity);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return AllPackets.CARRIAGE_DATA_UPDATE;
    }
}
