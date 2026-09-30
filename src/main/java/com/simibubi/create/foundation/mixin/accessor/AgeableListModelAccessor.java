package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AgeableListModel.class)
public interface AgeableListModelAccessor {
    @Invoker("headParts")
    Iterable<ModelPart> create$callHeadParts();

    @Invoker("bodyParts")
    Iterable<ModelPart> create$callBodyParts();

    // fabric: additional fields needed

    @Accessor
    boolean getScaleHead();

    @Accessor
    float getBabyHeadScale();

    @Accessor
    float getBabyYHeadOffset();

    @Accessor
    float getBabyZHeadOffset();
}
