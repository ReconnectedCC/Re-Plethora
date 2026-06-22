package io.sc3.plethora.gameplay.data.recipes

import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import io.sc3.plethora.Plethora.ModId
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier

class LevelableModuleEmiRecipe(
  private val recipe: LevelableModuleRecipe,
  private val level: Int
) : EmiRecipe {

  private val input = ItemStack(recipe.module).also { stack ->
    val tag = NbtCompound().apply { putInt("level", level) }
    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag))
  }

  private val output = ItemStack(recipe.module).also { stack ->
    val tag = NbtCompound().apply { putInt("level", level + 1) }
    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag))
  }

  override fun getCategory(): EmiRecipeCategory = VanillaEmiRecipeCategories.CRAFTING

  override fun getId(): Identifier = Identifier.of(
    ModId("").namespace,
    "/levelable_module/${recipe.module.module.path}/level_${level}_to_${level + 1}"
  )

  override fun getInputs(): List<EmiIngredient> = listOf(
    EmiStack.of(input),
    EmiIngredient.of(Ingredient.ofItems(Items.NETHER_STAR)),
    EmiIngredient.of(Ingredient.ofItems(Items.NETHERITE_INGOT))
  )

  override fun getOutputs(): List<EmiStack> = listOf(EmiStack.of(output))

  override fun getDisplayWidth(): Int = 116
  override fun getDisplayHeight(): Int = 54

  override fun addWidgets(widgets: WidgetHolder) {
    // 3x3 grid, slots at 18px spacing
    val inputs = listOf(
      EmiStack.of(input),
      EmiIngredient.of(Ingredient.ofItems(Items.NETHER_STAR)),
      EmiIngredient.of(Ingredient.ofItems(Items.NETHERITE_INGOT)),
      EmiStack.of(ItemStack.EMPTY),
      EmiStack.of(ItemStack.EMPTY),
      EmiStack.of(ItemStack.EMPTY),
      EmiStack.of(ItemStack.EMPTY),
      EmiStack.of(ItemStack.EMPTY),
      EmiStack.of(ItemStack.EMPTY),
      )

    for ((index, ingredient) in inputs.withIndex()) {
      val x = (index % 3) * 18
      val y = (index / 3) * 18
      widgets.addSlot(ingredient, x, y)
    }

    widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18)
    widgets.addTexture(EmiTexture.SHAPELESS, 97, 0)

    widgets.addSlot(EmiStack.of(output), 92, 18).recipeContext(this)
  }
}
