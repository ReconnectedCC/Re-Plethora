package io.sc3.plethora.gameplay.data.recipes.handlers

import io.sc3.plethora.Plethora.ModId
import io.sc3.plethora.gameplay.data.recipes.NeuralInterfaceRecipe
import net.minecraft.registry.Registries.RECIPE_SERIALIZER
import net.minecraft.registry.Registry.register

object MiscRecipes : PlethoraRecipeHandler {
  override fun registerSerializers() {
    register(RECIPE_SERIALIZER, ModId("neural_interface"), NeuralInterfaceRecipe.Serializer)
  }
}
