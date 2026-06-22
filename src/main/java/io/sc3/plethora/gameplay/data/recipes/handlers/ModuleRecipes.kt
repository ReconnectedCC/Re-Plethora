package io.sc3.plethora.gameplay.data.recipes.handlers

import dan200.computercraft.shared.ModRegistry
import io.sc3.library.recipe.BetterComplexRecipeJsonBuilder
import io.sc3.library.recipe.RecipeHandler
import io.sc3.plethora.Plethora.ModId
import io.sc3.plethora.gameplay.data.recipes.LevelableModuleRecipe
import io.sc3.plethora.gameplay.data.recipes.ScannerModuleUpgradeRecipe
import io.sc3.plethora.gameplay.data.recipes.SensorModuleUpgradeRecipe
import io.sc3.plethora.gameplay.data.recipes.inventoryChange
import io.sc3.plethora.gameplay.registry.Registration.ModItems
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.component.ComponentChanges
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.component.type.PotionContentsComponent
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.RecipeProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.potion.Potion
import net.minecraft.potion.Potions
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.Registries
import net.minecraft.registry.Registries.RECIPE_SERIALIZER
import net.minecraft.registry.Registry
import net.minecraft.registry.Registry.register
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import java.util.Optional


object ModuleRecipes : RecipeHandler {
  override fun registerSerializers() {
    register(RECIPE_SERIALIZER, ModId("scanner_module_upgrade"), ScannerModuleUpgradeRecipe.recipeSerializer)
    register(RECIPE_SERIALIZER, ModId("sensor_module_upgrade"), SensorModuleUpgradeRecipe.recipeSerializer)
  }

  override fun generateRecipes(exporter: RecipeExporter, wrapper: RegistryWrapper.WrapperLookup) {
    // Overlay Glasses
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.GLASSES_MODULE)
      .pattern("MIM")
      .pattern("GGG")
      .pattern("IAI")
      .input('A', Items.IRON_HELMET)
      .input('G', ModRegistry.Items.MONITOR_ADVANCED.get())
      .input('I', ConventionalItemTags.IRON_INGOTS)
      .input('M', ModRegistry.Items.WIRELESS_MODEM_NORMAL.get())
      .hasModuleHandler()
      .offerTo(exporter)

    // Introspection Module
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.INTROSPECTION_MODULE)
      .pattern("GCG")
      .pattern("CHC")
      .pattern("GCG")
      .input('C', Items.ENDER_CHEST)
      .input('G', ConventionalItemTags.GOLD_INGOTS)
      .input('H', Items.DIAMOND_HELMET)
      .hasModuleHandler()
      .offerTo(exporter)

    // Keyboard
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.KEYBOARD_MODULE)
      .pattern("  C")
      .pattern("SSI")
      .pattern("SSS")
      .input('C', ModRegistry.Items.CABLE.get())
      .input('I', ConventionalItemTags.IRON_INGOTS)
      .input('S', Items.STONE)
      .hasModuleHandler()
      .offerTo(exporter)

    // Block Scanner
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.SCANNER_MODULE)
      .pattern("EDE")
      .pattern("IOI")
      .pattern("III")
      .input('D', Items.DIRT)
      .input('E', Items.ENDER_EYE)
      .input('I', ConventionalItemTags.IRON_INGOTS)
      .input('O', Items.OBSERVER)
      .hasModuleHandler()
      .offerTo(exporter)

    // Entity Sensor
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.SENSOR_MODULE)
      .pattern("ERE")
      .pattern("IOI")
      .pattern("III")
      .input('E', Items.ENDER_EYE)
      .input('I', ConventionalItemTags.IRON_INGOTS)
      .input('O', Items.OBSERVER)
      .input('R', Items.ROTTEN_FLESH)
      .hasModuleHandler()
      .offerTo(exporter)

    // Kinetic Augment
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.KINETIC_MODULE)
      .pattern("RGR")
      .pattern("PBP")
      .pattern("RGR")
      .input('R', ConventionalItemTags.REDSTONE_DUSTS)
      .input('G', ConventionalItemTags.GOLD_INGOTS)
      .input('P', Items.PISTON)
      .input('B', leapingPotionIngredient()
      )
      .hasModuleHandler()
      .offerTo(exporter)

    // Frickin' Laser Beam
    ShapedRecipeJsonBuilder
      .create(RecipeCategory.MISC, ModItems.LASER_MODULE)
      .pattern("III")
      .pattern("GDF")
      .pattern("  I")
      .input('I', ConventionalItemTags.IRON_INGOTS)
      .input('G', ConventionalItemTags.GLASS_BLOCKS)
      .input('D', ConventionalItemTags.DIAMOND_GEMS)
      .input(
        'F', flameOrFireAspectBookIngredient(wrapper)
      )
      .hasModuleHandler()
      .offerTo(exporter)

    // Module Upgrades
    BetterComplexRecipeJsonBuilder<ScannerModuleUpgradeRecipe>(ModItems.SCANNER_MODULE, ScannerModuleUpgradeRecipe())
      .criterion("has_scanner", RecipeProvider.conditionsFromItem(ModItems.SCANNER_MODULE))
      .offerTo(exporter, ModId("scanner_module_upgrade"))

    BetterComplexRecipeJsonBuilder<SensorModuleUpgradeRecipe>(ModItems.SENSOR_MODULE, SensorModuleUpgradeRecipe())
      .criterion("has_sensor", RecipeProvider.conditionsFromItem(ModItems.SENSOR_MODULE))
      .offerTo(exporter, ModId("sensor_module_upgrade"))
  }

  private val moduleHandlerCriteria = mapOf(
    "has_manipulator_mark_1" to inventoryChange(ModItems.MANIPULATOR_MARK_1),
    "has_manipulator_mark_2" to inventoryChange(ModItems.MANIPULATOR_MARK_2),
    "has_neural_interface"   to inventoryChange(ModItems.NEURAL_INTERFACE)
  )

  private fun CraftingRecipeJsonBuilder.hasModuleHandler() = apply {
    moduleHandlerCriteria.forEach { criterion(it.key, it.value) }
  }

  private fun BetterComplexRecipeJsonBuilder<*>.hasModuleHandler() = apply {
    moduleHandlerCriteria.forEach { criterion(it.key, it.value) }
  }

  fun leapingPotionIngredient(): Ingredient {
    fun potionIngredient(item: Item, potion: RegistryEntry<Potion>) = DefaultCustomIngredients.components(
      Ingredient.ofItems(item),
      ComponentChanges.builder()
        .add(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent(
          Optional.of(potion),
          Optional.empty(),
          listOf()
        ))
        .build()
    )

    val items = listOf(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION)
    val potions = listOf(Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING)

    return DefaultCustomIngredients.any(
      *items.flatMap { item ->
        potions.map { potion -> potionIngredient(item, potion) }
      }.toTypedArray()
    )
  }
  fun flameOrFireAspectBookIngredient(registries: RegistryWrapper.WrapperLookup): Ingredient {
    val enchantmentRegistry = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)

    fun enchantedBookIngredient(enchantmentKey: RegistryKey<Enchantment>, level: Int): Ingredient {
      val enchantment = enchantmentRegistry.getOrThrow(enchantmentKey)
      return DefaultCustomIngredients.components(
        Ingredient.ofItems(Items.ENCHANTED_BOOK),
        ComponentChanges.builder()
          .add(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT)
            .apply { add(enchantment, level) }
            .build()
          )
          .build()
      )
    }

    return DefaultCustomIngredients.any(
      enchantedBookIngredient(Enchantments.FLAME, 1),
      enchantedBookIngredient(Enchantments.FIRE_ASPECT, 1),
      enchantedBookIngredient(Enchantments.FIRE_ASPECT, 2)
    )
  }
}
