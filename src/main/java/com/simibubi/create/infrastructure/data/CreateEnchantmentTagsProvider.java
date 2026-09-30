package com.simibubi.create.infrastructure.data;

import com.simibubi.create.AllEnchantments;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

import java.util.concurrent.CompletableFuture;

public class CreateEnchantmentTagsProvider extends EnchantmentTagsProvider {
    public CreateEnchantmentTagsProvider(
            PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(Provider prov) {
        tag(EnchantmentTags.NON_TREASURE)
                .add(AllEnchantments.CAPACITY, AllEnchantments.POTATO_RECOVERY);
        tag(EnchantmentTags.IN_ENCHANTING_TABLE)
                .add(AllEnchantments.CAPACITY, AllEnchantments.POTATO_RECOVERY);
    }
}
