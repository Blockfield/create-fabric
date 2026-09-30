package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.data.loot.LootTableProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Registrate-Fabric 1.3.77 передаёт в LootTableProvider ванильные сабпровайдеры вместо своих
 * (getTables).
 */
@Mixin(LootTableProvider.class)
public interface LootTableProviderSubProvidersAccessor {
    @Accessor("subProviders")
    @Mutable
    void create$setSubProviders(List<LootTableProvider.SubProviderEntry> subProviders);
}
