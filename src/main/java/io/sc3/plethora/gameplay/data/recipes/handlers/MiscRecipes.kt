package io.sc3.plethora.gameplay.data.recipes.handlers

import dan200.computercraft.api.ComputerCraftTags
import dan200.computercraft.shared.ModRegistry
import io.sc3.library.recipe.RecipeHandler
import io.sc3.library.recipe.offerTo
import io.sc3.plethora.Plethora.ModId
import io.sc3.plethora.gameplay.data.recipes.NeuralInterfaceRecipe
import io.sc3.plethora.gameplay.data.recipes.handlers.recipe.ShapedSpecBuilder
import io.sc3.plethora.gameplay.data.recipes.inventoryChange
import io.sc3.plethora.gameplay.registry.Registration.ModItems
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Items
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.Registries.RECIPE_SERIALIZER
import net.minecraft.registry.Registry.register

object MiscRecipes : RecipeHandler {
  override fun registerSerializers() {
    register(RECIPE_SERIALIZER, ModId("neural_interface"), NeuralInterfaceRecipe.Serializer)
  }

  override fun generateRecipes(exporter: RecipeExporter, registries: RegistryWrapper.WrapperLookup) {
    // Manipulator Mark I
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.MANIPULATOR_MARK_1)
      .pattern("GCG")
      .pattern("RMR")
      .pattern("III")
      .input('C', ConventionalItemTags.GOLD_INGOTS)
      .input('G', ConventionalItemTags.GLASS_BLOCKS)
      .input('I', ConventionalItemTags.IRON_INGOTS)
      .input('M', ModRegistry.Items.CABLE.get())
      .input('R', ConventionalItemTags.REDSTONE_DUSTS)
      .hasComputer()
      .offerTo(exporter)

    // Manipulator Mark II
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.MANIPULATOR_MARK_2)
      .pattern("CCC")
      .pattern("RMR")
      .pattern("III")
      .input('C', ConventionalItemTags.GOLD_INGOTS)
      .input('I', ConventionalItemTags.IRON_INGOTS)
      .input('M', ModItems.MANIPULATOR_MARK_1)
      .input('R', ConventionalItemTags.REDSTONE_DUSTS)
      .hasComputer()
      .offerTo(exporter)

    // Neural Connector
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.NEURAL_CONNECTOR)
      .pattern("  R")
      .pattern("IIR")
      .pattern("IEI")
      .input('E', Items.ENDER_PEARL)
      .input('I', ConventionalItemTags.IRON_INGOTS)
      .input('R', ConventionalItemTags.REDSTONE_DUSTS)
      .hasComputer()
      .offerTo(exporter)

    // Neural Interface
    ShapedSpecBuilder
      .shaped(RecipeCategory.MISC, ModItems.NEURAL_INTERFACE)
      .pattern("  G")
      .pattern("IPR")
      .pattern(" GM")
      .define('G', ConventionalItemTags.GOLD_INGOTS)
      .define('I', ConventionalItemTags.IRON_INGOTS)
      .define('R', ConventionalItemTags.REDSTONE_DUSTS)
      .define('M', ComputerCraftTags.Items.WIRED_MODEM)
      .define('P', ModRegistry.Items.POCKET_COMPUTER_ADVANCED.get())
      .hasComputer()
      .build(::NeuralInterfaceRecipe)
      .save(exporter, ModId("neural_interface"))
  }

  private val computerCriteria = lazy {
    mapOf(
      "has_computer_normal" to inventoryChange(ModRegistry.Items.COMPUTER_NORMAL.get()),
      "has_computer_advanced" to inventoryChange(ModRegistry.Items.COMPUTER_ADVANCED.get()),
      "has_turtle_normal" to inventoryChange(ModRegistry.Items.TURTLE_NORMAL.get()),
      "has_turtle_advanced" to inventoryChange(ModRegistry.Items.TURTLE_ADVANCED.get()),
      "has_pocket_normal" to inventoryChange(ModRegistry.Items.POCKET_COMPUTER_NORMAL.get()),
      "has_pocket_advanced" to inventoryChange(ModRegistry.Items.POCKET_COMPUTER_ADVANCED.get()),
    )
  }

  private fun CraftingRecipeJsonBuilder.hasComputer() = apply {
    computerCriteria.value.forEach { criterion(it.key, it.value) }
  }

  private fun ShapedSpecBuilder.hasComputer() = apply {
    computerCriteria.value.forEach { unlockedBy(it.key, it.value) }
  }
}
