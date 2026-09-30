package com.simibubi.create.impl.effect;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MilkEffectHandler implements OpenPipeEffectHandler {
    @Override
    public void apply(Level level, AABB area, FluidStack fluid) {
        if (level.getGameTime() % 5 != 0) return;

        List<LivingEntity> entities =
                level.getEntitiesOfClass(
                        LivingEntity.class, area, LivingEntity::isAffectedByPotions);
        for (LivingEntity entity : entities) {
            entity.removeAllEffects();
        }
    }
}
