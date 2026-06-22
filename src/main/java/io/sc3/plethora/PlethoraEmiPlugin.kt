package io.sc3.plethora

import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import io.sc3.plethora.gameplay.data.recipes.LevelableModuleEmiRecipe
import io.sc3.plethora.gameplay.data.recipes.LevelableModuleRecipe
import io.sc3.plethora.gameplay.modules.LevelableModuleItem
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeType

class PlethoraEmiPlugin : EmiPlugin {
  override fun register(registry: EmiRegistry) {
    registry.recipeManager
      .listAllOfType(RecipeType.CRAFTING)
      .filter { it.value is LevelableModuleRecipe }
      .forEach { entry ->
        registry.removeRecipes { it.id == entry.id }
        val recipe = entry.value as LevelableModuleRecipe
        val moduleStack = ItemStack(recipe.module)

        // Query max level dynamically by simulating level increments
        // until getEffectiveRange stops changing
        var level = 0
        while (true) {
          val currentRange = LevelableModuleItem.getEffectiveRange(moduleStack, level)
          val nextRange = LevelableModuleItem.getEffectiveRange(moduleStack, level + 1)
          if (currentRange == nextRange) break  // hit the cap
          registry.addRecipe(LevelableModuleEmiRecipe(recipe, level))
          level++
        }
      }
  }
}
