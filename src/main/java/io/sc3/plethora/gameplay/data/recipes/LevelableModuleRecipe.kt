package io.sc3.plethora.gameplay.data.recipes

import io.sc3.plethora.gameplay.modules.LevelableModuleItem
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Ingredient.ofItems
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.collection.DefaultedList

abstract class LevelableModuleRecipe(
  category: CraftingRecipeCategory = CraftingRecipeCategory.MISC,
  val module: LevelableModuleItem
) : ShapelessRecipe(
  "", category,
  // Result stack with level 1 for EMI display
  ItemStack(module).also { stack ->
    val tag = NbtCompound().apply { putInt("level", 1) }
    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag))
  },
  DefaultedList.copyOf(
    Ingredient.EMPTY,
    ofItems(module),  // Accepts any level — EMI shows level 0 by default
    ofItems(Items.NETHER_STAR),
    ofItems(Items.NETHERITE_INGOT)
  )
){
  override fun craft(inv: CraftingRecipeInput, manager: RegistryWrapper.WrapperLookup): ItemStack {
    val output = getResult(manager)

    for (i in 0 until inv.size) {
      val stack: ItemStack = inv.getStackInSlot(i)
      if (stack.item !is LevelableModuleItem || stack.item !== output.item) {
        continue
      }

      val result = stack.copyWithCount(1)

      // Only increment the level if the module is not already at the max - i.e. only if the effective radius is
      // different to before
      val oldLevel = LevelableModuleItem.getLevel(stack)
      val oldRange = LevelableModuleItem.getEffectiveRange(stack)
      val newRange = LevelableModuleItem.getEffectiveRange(stack, oldLevel + 1)
      if (oldRange == newRange) return ItemStack.EMPTY

      // Increment the level by updating the NBT of the result item
      val tag = result.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() ?: NbtCompound()
      tag.putInt("level", oldLevel + 1)
      result.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag))
      return result
    }

    return output.copy()
  }

  override fun isIgnoredInRecipeBook() = false
}
