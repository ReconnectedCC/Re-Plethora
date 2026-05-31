package io.sc3.plethora.gameplay.modules

import io.sc3.plethora.api.method.IContextBuilder
import io.sc3.plethora.api.module.IModuleAccess
import net.minecraft.nbt.NbtElement
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.text.Text.translatable
import kotlin.math.ceil
import kotlin.math.pow

abstract class LevelableModuleItem(itemName: String, settings: Settings) : ModuleItem(itemName, settings) {
  abstract val baseRange: Int
  abstract val maxRange: Int
  abstract val levelCost: Int

  override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
    super.appendTooltip(stack, context, tooltip, type)

    val level = getLevel(stack)
    if (level < 0) return

    val range = getEffectiveRange(stack)
    tooltip.add(translatable("item.plethora.module.level", level, range))
  }

  override fun hasGlint(stack: ItemStack) =
    super.hasGlint(stack) || getLevel(stack) > 0

  override fun getAdditionalContext(stack: ItemStack, access: IModuleAccess, builder: IContextBuilder) {
    super.getAdditionalContext(stack, access, builder)
    builder.addContext(module.toString(), RangeInfo.of(
      getLevel(stack),
      { it * levelCost },
      { getEffectiveRange(baseRange, maxRange, it) }
    ))
  }

  companion object {
    @JvmStatic
    fun getLevel(stack: ItemStack?): Int {
      if (stack == null || stack.isEmpty) return 0
      val nbt = stack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()
      return if (nbt != null && nbt.contains("level", NbtElement.NUMBER_TYPE.toInt())) {
        nbt.getInt("level")
      } else {
        0
      }
    }

    fun getEffectiveRange(baseRange: Int, maxRange: Int, level: Int): Int {
      return if (maxRange <= baseRange || level <= 0) {
        baseRange
      } else {
        // Each level adds half of the remainder to the maximum level - so effectively the geometric sum.
        baseRange + ceil((1 - 0.5.pow(level.toDouble())) * (maxRange - baseRange)).toInt()
      }
    }

    fun getEffectiveRange(stack: ItemStack?, level: Int = getLevel(stack)): Int {
      val item = stack?.item
      return if (item != null && item is LevelableModuleItem) {
        getEffectiveRange(item.baseRange, item.maxRange, level)
      } else {
        0
      }
    }
  }
}
