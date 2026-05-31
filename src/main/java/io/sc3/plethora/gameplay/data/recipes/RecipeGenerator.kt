package io.sc3.plethora.gameplay.data.recipes

import io.sc3.plethora.gameplay.data.recipes.handlers.RecipeHandlers.RECIPE_HANDLERS
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.advancement.AdvancementCriterion
import net.minecraft.advancement.criterion.InventoryChangedCriterion
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.item.ItemConvertible
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class RecipeGenerator(out: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>) :
  FabricRecipeProvider(out, registriesFuture) {
  override fun generate(exporter: RecipeExporter) {
    RECIPE_HANDLERS.forEach { it.generateRecipes(exporter) }
  }
}

fun inventoryChange(vararg items: ItemConvertible): AdvancementCriterion<InventoryChangedCriterion.Conditions> =
  InventoryChangedCriterion.Conditions.items(*items)
