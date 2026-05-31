package io.sc3.plethora.gameplay.data.recipes.handlers

import net.minecraft.data.server.recipe.RecipeExporter

interface PlethoraRecipeHandler {
  fun registerSerializers() {
  }

  fun generateRecipes(exporter: RecipeExporter) {
  }
}

object RecipeHandlers {
  val RECIPE_HANDLERS by lazy { listOf(
    ModuleRecipes,
    MiscRecipes,
  )}

  @JvmStatic
  fun registerSerializers() {
    RECIPE_HANDLERS.forEach(PlethoraRecipeHandler::registerSerializers)
  }
}
