package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;

import io.github.fabricators_of_create.porting_lib.tags.Tags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ItemApplicationRecipeGen extends ProcessingRecipeGen {

    GeneratedRecipe BOUND_CARDBOARD_BLOCK =
            create(
                    "bound_cardboard_inworld",
                    b ->
                            b.require(AllBlocks.CARDBOARD_BLOCK.asItem())
                                    .require(Tags.Items.STRINGS)
                                    .output(AllBlocks.BOUND_CARDBOARD_BLOCK.asStack()));

    GeneratedRecipe ANDESITE = woodCasing("andesite", I::andesiteAlloy, I::andesiteCasing);
    GeneratedRecipe COPPER = woodCasingTag("copper", I::copper, I::copperCasing);
    GeneratedRecipe BRASS = woodCasingTag("brass", I::brass, I::brassCasing);
    GeneratedRecipe RAILWAY =
            create(
                    "railway_casing",
                    b ->
                            b.require(I.brassCasing())
                                    .require(I.sturdySheet())
                                    .output(I.railwayCasing()));

    protected GeneratedRecipe woodCasing(
            String type, Supplier<ItemLike> ingredient, Supplier<ItemLike> output) {
        return woodCasingIngredient(type, () -> Ingredient.of(ingredient.get()), output);
    }

    protected GeneratedRecipe woodCasingTag(
            String type, Supplier<TagKey<Item>> ingredient, Supplier<ItemLike> output) {
        return woodCasingIngredient(type, () -> Ingredient.of(ingredient.get()), output);
    }

    protected GeneratedRecipe woodCasingIngredient(
            String type, Supplier<Ingredient> ingredient, Supplier<ItemLike> output) {
        create(
                type + "_casing_from_log",
                b ->
                        b.require(ConventionalItemTags.STRIPPED_LOGS)
                                .require(ingredient.get())
                                .output(output.get()));
        return create(
                type + "_casing_from_wood",
                b ->
                        b.require(ConventionalItemTags.STRIPPED_WOODS)
                                .require(ingredient.get())
                                .output(output.get()));
    }

    public ItemApplicationRecipeGen(
            FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected AllRecipeTypes getRecipeType() {
        return AllRecipeTypes.ITEM_APPLICATION;
    }
}
