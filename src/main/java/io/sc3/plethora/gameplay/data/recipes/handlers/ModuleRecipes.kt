package io.sc3.plethora.gameplay.data.recipes.handlers

import io.sc3.plethora.Plethora.ModId
import io.sc3.plethora.gameplay.data.recipes.ScannerModuleUpgradeRecipe
import io.sc3.plethora.gameplay.data.recipes.SensorModuleUpgradeRecipe
import net.minecraft.registry.Registries.RECIPE_SERIALIZER
import net.minecraft.registry.Registry.register

object ModuleRecipes : PlethoraRecipeHandler {
  override fun registerSerializers() {
    register(RECIPE_SERIALIZER, ModId("scanner_module_upgrade"), ScannerModuleUpgradeRecipe.recipeSerializer)
    register(RECIPE_SERIALIZER, ModId("sensor_module_upgrade"), SensorModuleUpgradeRecipe.recipeSerializer)
  }
}
