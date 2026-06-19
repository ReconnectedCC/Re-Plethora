// SPDX-FileCopyrightText: 2023 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package io.sc3.plethora.gameplay.data.recipes.handlers.recipe;

import com.mojang.serialization.DataResult;
import dan200.computercraft.shared.recipe.RecipeProperties;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An abstract base class for creating recipes, in the style of {@link RecipeBuilder}.
 *
 * @param <S> The type of this class.
 * @param <O> The output of this builder.
 * @see ShapelessSpecBuilder
 */
public abstract class AbstractRecipeBuilder<S extends AbstractRecipeBuilder<S, O>, O> {
    private final RecipeCategory category;
    protected final ItemStack result;
    private String group = "";
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

    protected AbstractRecipeBuilder(RecipeCategory category, ItemStack result) {
        this.category = category;
        this.result = result;
    }

    /**
     * Set the group for this recipe.
     *
     * @param group The new group.
     * @return This object, for chaining.
     */
    public final S group(String group) {
        this.group = group;
        return self();
    }

    /**
     * Add a criterion to this recipe.
     *
     * @param name      The name of the criterion.
     * @param criterion The criterion to add.
     * @return This object, for chaining.
     */
    public final S unlockedBy(String name, AdvancementCriterion<?> criterion) {
        criteria.put(name, criterion);
        return self();
    }

    /**
     * Convert this builder into the output ({@link O}) object.
     *
     * @param properties The properties for this recipe.
     * @return The built object.
     */
    protected abstract O build(RecipeProperties properties);

    /**
     * Convert this builder into a concrete recipe.
     *
     * @param factory The recipe's constructor.
     * @return The "built" recipe.
     */
    public final FinishedRecipe build(Function<O, Recipe<?>> factory) {
        var properties = new RecipeProperties(group, CraftingRecipeJsonBuilder.toCraftingCategory(category), true);
        return new FinishedRecipe(factory.apply(build(properties)), result.getItem(), category, criteria);
    }

    /**
     * Convert this builder into a concrete recipe.
     *
     * @param factory The recipe's constructor.
     * @return The "built" recipe.
     */
    public final FinishedRecipe buildOrThrow(Function<O, DataResult<? extends Recipe<?>>> factory) {
        return build(s -> factory.apply(s).getOrThrow());
    }

    @SuppressWarnings("unchecked")
    private S self() {
        return (S) this;
    }

    public static final class FinishedRecipe {
        private final Recipe<?> recipe;
        private final Item result;
        private final RecipeCategory category;
        private final Map<String, AdvancementCriterion<?>> criteria;

        private FinishedRecipe(Recipe<?> recipe, Item result, RecipeCategory category, Map<String, AdvancementCriterion<?>> criteria) {
            this.recipe = recipe;
            this.result = result;
            this.category = category;
            this.criteria = criteria;
        }

        public void save(RecipeExporter exporter, Identifier id) {
            if (criteria.isEmpty()) throw new IllegalStateException("No way of obtaining recipe " + id);
            var advancementBuilder = exporter.getAdvancementBuilder()
              .criterion("has_the_recipe", net.minecraft.advancement.criterion.InventoryChangedCriterion.Conditions.items(result))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
            for (var entry : criteria.entrySet()) {
                advancementBuilder.criterion(entry.getKey(), entry.getValue());
            }

            exporter.accept(id, recipe, advancementBuilder.build(Identifier.of(id.getNamespace(), "recipes/" + category.toString().toLowerCase() + "/" + id.getPath())));
        }

        public void save(RecipeExporter exporter) {
            save(exporter, Identifier.of(result.getDefaultStack().getItem().toString()));
        }
    }
}
