package com.simibubi.create.content.contraptions.minecart.capability;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MinecartControllerUpdatePacket(int entityId, @Nullable CompoundTag nbt)
        implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, MinecartControllerUpdatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    MinecartControllerUpdatePacket::entityId,
                    CatnipStreamCodecBuilders.nullable(ByteBufCodecs.COMPOUND_TAG),
                    MinecartControllerUpdatePacket::nbt,
                    MinecartControllerUpdatePacket::new);

    public MinecartControllerUpdatePacket(
            MinecartController controller, @NotNull HolderLookup.Provider registries) {
        this(
                controller.cart().getId(),
                controller.isEmpty() ? null : controller.serializeNBT(registries));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(LocalPlayer player) {
        Entity entityByID = player.clientLevel.getEntity(entityId);
        // fabric: the controller lives on the minecart itself (AbstractMinecartMixin), not in an
        // attachment;
        // a null payload means "no coupling data", which is what deserializing an empty tag resets
        // it to
        if (!(entityByID instanceof AbstractMinecart cart)) return;
        MinecartController controller = cart.create$getController();
        if (controller == null) return;
        controller.deserializeNBT(player.registryAccess(), nbt == null ? new CompoundTag() : nbt);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return AllPackets.MINECART_CONTROLLER;
    }
}
