package io.sc3.plethora.gameplay.data.recipes

import dan200.computercraft.api.media.IMedia
import dan200.computercraft.shared.ModRegistry
import dan200.computercraft.shared.pocket.items.PocketComputerItem
import dan200.computercraft.shared.recipe.CustomShapedRecipe
import dan200.computercraft.shared.recipe.ShapedRecipeSpec
import io.sc3.plethora.gameplay.neural.NeuralComputerHandler
import io.sc3.plethora.gameplay.neural.NeuralHelpers
import io.sc3.plethora.gameplay.neural.NeuralInterfaceInventory
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.registry.RegistryWrapper
import net.minecraft.text.Text

class NeuralInterfaceRecipe(
  recipe: ShapedRecipeSpec
) : CustomShapedRecipe(recipe) {
  private val output = recipe.result().item

  override fun craft(input: CraftingRecipeInput, registries: RegistryWrapper.WrapperLookup): ItemStack {
    val old = input.stacks.firstOrNull { it.item is IMedia } ?: return super.craft(input, registries)
    val item = old.item as IMedia
    val result = ItemStack(output)
    val id = old.get(ModRegistry.DataComponents.COMPUTER_ID.get())?.id() ?: -1
    val label = item.getLabel(registries, old)

    val nbt = NbtCompound()
    if (!label.isNullOrEmpty()) result.set(DataComponentTypes.CUSTOM_NAME, Text.of(label))
    if (id >= 0) nbt.putInt(NeuralComputerHandler.COMPUTER_ID, id)

    // Forge/1.12.2 Plethora does not check if the source pocket computer has an upgrade, but I feel like it would kinda
    // suck to lose your pocket's ender modem when upgrading it to a neural interface, so let's grab that too.
    val upgrade = PocketComputerItem.getUpgrade(old)
    if (upgrade != null) {
      // Check if the neural will actually accept the item before trying to add it. Add to the BACK slot (2)
      val upgradeStack = upgrade.craftingItem
      if (NeuralHelpers.isItemValid(NeuralHelpers.BACK, upgradeStack)) {
        val neuralInv = NeuralInterfaceInventory(result)
        neuralInv.setStack(NeuralHelpers.BACK, upgradeStack)

        Inventories.writeNbt(nbt, neuralInv.ownStacks, registries)
      }
    }

    if (!nbt.isEmpty) result.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt))
    return result
  }

  override fun getSerializer() = Serializer

  companion object {
    val Serializer: RecipeSerializer<NeuralInterfaceRecipe> = serialiser(::NeuralInterfaceRecipe)
  }
}
