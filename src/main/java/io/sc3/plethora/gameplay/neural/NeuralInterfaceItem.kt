package io.sc3.plethora.gameplay.neural

import dan200.computercraft.api.ComputerCraftAPI
import dan200.computercraft.api.filesystem.Mount
import dan200.computercraft.api.media.IMedia
import dan200.computercraft.shared.config.Config
import dan200.computercraft.shared.config.ConfigSpec
import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketItem
import io.sc3.library.Tooltips.addDescLines
import io.sc3.plethora.Plethora.MOD_ID
import io.sc3.plethora.gameplay.neural.NeuralComputerHandler.COMPUTER_ID
import io.sc3.plethora.gameplay.neural.NeuralComputerHandler.DIRTY
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.Text.translatable
import net.minecraft.util.Formatting.GRAY
import javax.annotation.Nonnull

class NeuralInterfaceItem(settings: Settings?) : TrinketItem(settings), IMedia {
  override fun getTranslationKey() = "item.$MOD_ID.neuralInterface"

  override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
    super.appendTooltip(stack, context, tooltip, type)
    addDescLines(tooltip, getTranslationKey(stack))

    if (type.isAdvanced) {
      val nbt = stack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()
      if (nbt != null && nbt.contains(COMPUTER_ID)) {
        tooltip.add(translatable("gui.plethora.tooltip.computer_id", getComputerID(stack))
          .formatted(GRAY))
      }
    }
  }

  fun getComputerID(@Nonnull stack: ItemStack): Int {
    val nbt = stack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()
    return if (nbt != null && nbt.contains(COMPUTER_ID)) nbt.getInt(COMPUTER_ID) else -1
  }

  override fun getLabel(registries: RegistryWrapper.WrapperLookup, @Nonnull stack: ItemStack): String? =
    stack.get(DataComponentTypes.CUSTOM_NAME)?.string

  override fun setLabel(@Nonnull stack: ItemStack, label: String?): Boolean {
    if (label == null) {
      stack.remove(DataComponentTypes.CUSTOM_NAME)
    } else {
      stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(label))
    }
    return true
  }

  override fun createDataMount(@Nonnull stack: ItemStack, @Nonnull world: ServerWorld): Mount? {
    val id = getComputerID(stack)
    return if (id < 0) {
      null
    } else {
      //TODO: Add NBT override as architected
      ComputerCraftAPI.createSaveDirMount(world.server, "computer/$id", ConfigSpec.computerSpaceLimit.get().toLong())
    }
  }

  fun changeItem(stack: ItemStack, newItem: Item): ItemStack =
    if (newItem === this) stack.copy() else ItemStack.EMPTY

  override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
    onUpdate(stack, slot, entity, true)
  }

  companion object {
    private fun onUpdate(stack: ItemStack, slot: SlotReference?, player: LivingEntity, forceActive: Boolean) {
      if (!player.entityWorld.isClient) {
        // Fetch computer
        val neural = if (forceActive) {
          NeuralComputerHandler.getServer(stack, player, slot!!).also { it.keepAlive() }
        } else {
          NeuralComputerHandler.tryGetServer(stack, player) ?: return
        }

        var dirty = false

        // Sync computer ID
        val newId = neural.id
        val nbt = stack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() ?: net.minecraft.nbt.NbtCompound()
        if (!nbt.contains(COMPUTER_ID) || nbt.getInt(COMPUTER_ID) != newId) {
          nbt.putInt(COMPUTER_ID, newId)
          dirty = true
        }

        // Sync Label
        val newLabel = neural.label
        val label = stack.get(DataComponentTypes.CUSTOM_NAME)?.string
        if (newLabel != label) {
          if (newLabel == null || newLabel.isEmpty()) {
            stack.remove(DataComponentTypes.CUSTOM_NAME)
          } else {
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(newLabel))
          }
          dirty = true
        }

        // Sync and update peripherals
        val dirtyStatus = nbt.getShort(DIRTY)
        if (dirtyStatus.toInt() != 0) {
          nbt.putShort(DIRTY, 0.toShort())
          dirty = true
        }

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt))

        if (neural.update(player, stack, dirtyStatus.toInt())) {
          dirty = true
        }

        if (dirty && slot != null) {
          slot.inventory().markDirty()
        }
      }
    }
  }
}
