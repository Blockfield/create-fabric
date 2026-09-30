package com.simibubi.create.infrastructure.data;

import com.simibubi.create.AllDamageTypes;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileTypes;
import com.simibubi.create.infrastructure.worldgen.AllConfiguredFeatures;
import com.simibubi.create.infrastructure.worldgen.AllPlacedFeatures;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

import java.util.concurrent.CompletableFuture;

public class GeneratedEntriesProvider extends FabricDynamicRegistryProvider {
    public GeneratedEntriesProvider(
            FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    // fabric: this must be reused in the entrypoint, moved to a method
    public static RegistrySetBuilder addBootstraps(RegistrySetBuilder builder) {
        return builder.add(Registries.ENCHANTMENT, AllEnchantments::bootstrap)
                .add(Registries.DAMAGE_TYPE, AllDamageTypes::bootstrap)
                .add(Registries.CONFIGURED_FEATURE, AllConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, AllPlacedFeatures::bootstrap)
                .add(CreateRegistries.POTATO_PROJECTILE_TYPE, AllPotatoProjectileTypes::bootstrap);
        // fabric: biome modifiers are not a registry, remove
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        // addAll only picks up entries in Create's namespace
        entries.addAll(registries.lookupOrThrow(Registries.ENCHANTMENT));
        entries.addAll(registries.lookupOrThrow(Registries.DAMAGE_TYPE));
        entries.addAll(registries.lookupOrThrow(Registries.CONFIGURED_FEATURE));
        entries.addAll(registries.lookupOrThrow(Registries.PLACED_FEATURE));
        entries.addAll(registries.lookupOrThrow(CreateRegistries.POTATO_PROJECTILE_TYPE));
    }

    @Override
    public String getName() {
        return "Create's Generated Registry Entries";
    }
}
