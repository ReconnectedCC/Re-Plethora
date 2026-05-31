package io.sc3.plethora.gameplay.data.recipes

import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import io.sc3.plethora.gameplay.registry.Registration.ModItems.SCANNER_MODULE

class ScannerModuleUpgradeRecipe(
  category: CraftingRecipeCategory = CraftingRecipeCategory.MISC
) : LevelableModuleRecipe(category, SCANNER_MODULE) {
  override fun getSerializer() = recipeSerializer

  companion object {
    val recipeSerializer = SpecialRecipeSerializer(::ScannerModuleUpgradeRecipe)
  }
}
