package com.simibubi.create.foundation.fabric;

import io.netty.buffer.Unpooled;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Fabric replacement for NeoForge's {@code Player#openMenu(MenuProvider,
 * Consumer<RegistryFriendlyByteBuf>)}. Create's menus all take a raw {@link
 * RegistryFriendlyByteBuf} as their screen opening data (see {@code MenuBase}), so the transported
 * type is the buffer itself and {@link #BUFFER_CODEC} is the stream codec every Create menu type
 * has to be registered with.
 */
public class MenuUtil {

    /**
     * Copies the whole payload verbatim; the menu constructor reads it like a NeoForge extra-data
     * buffer.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> BUFFER_CODEC =
            new StreamCodec<>() {
                @Override
                public RegistryFriendlyByteBuf decode(RegistryFriendlyByteBuf buf) {
                    RegistryFriendlyByteBuf copy =
                            new RegistryFriendlyByteBuf(Unpooled.buffer(), buf.registryAccess());
                    copy.writeBytes(buf, buf.readableBytes());
                    return copy;
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, RegistryFriendlyByteBuf value) {
                    buf.writeBytes(value, value.readerIndex(), value.readableBytes());
                }
            };

    public static void open(
            Player player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) {
        if (!(player instanceof ServerPlayer sp)) return;
        sp.openMenu(new ExtraDataProvider(provider, extraData));
    }

    public static void open(Player player, MenuProvider provider, BlockPos pos) {
        open(player, provider, buf -> buf.writeBlockPos(pos));
    }

    private record ExtraDataProvider(
            MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData)
            implements ExtendedScreenHandlerFactory<RegistryFriendlyByteBuf> {

        @Override
        public RegistryFriendlyByteBuf getScreenOpeningData(ServerPlayer player) {
            RegistryFriendlyByteBuf buf =
                    new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
            extraData.accept(buf);
            return buf;
        }

        @Override
        public Component getDisplayName() {
            return provider.getDisplayName();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return provider.createMenu(id, inv, player);
        }
    }
}
