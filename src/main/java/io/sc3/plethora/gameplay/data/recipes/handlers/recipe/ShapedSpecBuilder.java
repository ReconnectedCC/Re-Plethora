// SPDX-FileCopyrightText: 2023 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package io.sc3.plethora.gameplay.data.recipes.handlers.recipe;

import dan200.computercraft.shared.recipe.RecipeProperties;
import dan200.computercraft.shared.recipe.ShapedRecipeSpec;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.TagKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder for {@link ShapedRecipeSpec}s, much like {@link ShapedRecipeBuilder}.
 */
public final class ShapedSpecBuilder extends AbstractRecipeBuilder<ShapedSpecBuilder, ShapedRecipeSpec> {
    private final List<String> rows = new ArrayList<>();
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();

    private ShapedSpecBuilder(RecipeCategory category, ItemStack result) {
        super(category, result);
    }

    public static ShapedSpecBuilder shaped(RecipeCategory category, ItemStack result) {
        return new ShapedSpecBuilder(category, result);
    }

    public static ShapedSpecBuilder shaped(RecipeCategory category, ItemConvertible result) {
        return new ShapedSpecBuilder(category, new ItemStack(result));
    }

    public ShapedSpecBuilder define(char key, Ingredient ingredient) {
        if (this.key.containsKey(key)) throw new IllegalArgumentException("Symbol '" + key + "' is already defined!");
        if (key == ' ') throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");

        this.key.put(key, ingredient);
        return this;
    }

    public ShapedSpecBuilder define(char key, TagKey<Item> tag) {
        return this.define(key, Ingredient.fromTag(tag));
    }

    public ShapedSpecBuilder define(char key, ItemConvertible item) {
        return this.define(key, Ingredient.ofItems(item));
    }

    public ShapedSpecBuilder pattern(String pattern) {
        if (!this.rows.isEmpty() && pattern.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pattern);
            return this;
        }
    }

    @Override
    protected ShapedRecipeSpec build(RecipeProperties properties) {
        return new ShapedRecipeSpec(properties, RawShapedRecipe.create(key, rows), result);
    }
}
