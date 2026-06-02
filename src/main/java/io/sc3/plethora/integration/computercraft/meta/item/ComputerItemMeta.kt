package io.sc3.plethora.integration.computercraft.meta.item

import dan200.computercraft.shared.ModRegistry
import dan200.computercraft.shared.computer.items.AbstractComputerItem
import dan200.computercraft.shared.pocket.items.PocketComputerItem
import dan200.computercraft.shared.turtle.items.TurtleItem
import io.sc3.plethora.api.meta.ItemStackMetaProvider
import io.sc3.plethora.gameplay.neural.NeuralInterfaceItem
import io.sc3.plethora.gameplay.registry.Registration.ModItems
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class ComputerItemMeta : ItemStackMetaProvider<Item>(Item::class.java, "computer") {
  override fun getMeta(stack: ItemStack, item: Item): Map<String, *> {
    if (item !is AbstractComputerItem && item !is PocketComputerItem && item !is NeuralInterfaceItem) {
      return emptyMap<String, Any>()
    }

    val data: MutableMap<String, Any?> = HashMap(3)

    val id = if (item is NeuralInterfaceItem) item.getComputerID(stack) else stack.get(ModRegistry.DataComponents.COMPUTER_ID.get())?.id() ?: -1
    if (id >= 0) data["id"] = id

    val label = stack.get(DataComponentTypes.CUSTOM_NAME)?.string
    if (!label.isNullOrEmpty()) data["label"] = label

    // "family" used to be available on IComputerItem. Instead, we just look it up based on the current item.
    data["family"] = when (item) {
      ModRegistry.Items.COMPUTER_NORMAL.get(), ModRegistry.Items.TURTLE_NORMAL.get(), ModRegistry.Items.POCKET_COMPUTER_NORMAL.get() -> "normal"
      ModRegistry.Items.COMPUTER_ADVANCED.get(), ModRegistry.Items.TURTLE_ADVANCED.get(), ModRegistry.Items.POCKET_COMPUTER_ADVANCED.get(), ModItems.NEURAL_INTERFACE -> "advanced"
      ModRegistry.Items.COMPUTER_COMMAND.get() -> "command"
      is TurtleItem -> "unknown"
      else -> "unknown"
    }

    return data
  }
}
