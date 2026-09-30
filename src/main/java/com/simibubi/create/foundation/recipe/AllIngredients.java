package com.simibubi.create.foundation.recipe;

import com.simibubi.create.foundation.data.SimpleDatagenIngredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * fabric: NeoForge's {@code IngredientType} registry maps onto fabric-recipe-api-v1's {@link
 * CustomIngredientSerializer} registry.
 */
public class AllIngredients {

    @Internal
    public static void register() {
        CustomIngredientSerializer.register(SimpleDatagenIngredient.SERIALIZER);
    }
}
