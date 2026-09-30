package com.simibubi.create.foundation.mixin.fabric.infra;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.infrastructure.fabric.HelmetOverlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow
    protected abstract void renderTextureOverlay(
            GuiGraphics guiGraphics, ResourceLocation shaderLocation, float alpha);

    @ModifyExpressionValue(
            method =
                    "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            // ponytail: require = 0 -- накладка шлема косметическая, ронять клиент из-за неё
            // нельзя; вернуть 1, когда инжект подтвердится
            require = 0,
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/player/Inventory;getArmor(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack renderCustomOverlay(
            ItemStack stack, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        HelmetOverlay overlay = HelmetOverlay.REGISTRY.get(stack.getItem());
        if (overlay != null) {
            float opacity = overlay.calculateOpacity(stack, this.minecraft.player, partialTick);
            if (opacity > 0) {
                this.renderTextureOverlay(guiGraphics, overlay.texture, opacity);
            }
        }

        return stack;
    }
}
