package io.sc3.plethora.gameplay

import net.minecraft.item.Item
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import io.sc3.library.Tooltips.addDescLines
import io.sc3.plethora.Plethora.MOD_ID

abstract class BaseItem(protected val itemName: String, settings: Settings) : Item(settings) {
  override fun getTranslationKey() = "item.$MOD_ID.$itemName"

  override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
    super.appendTooltip(stack, context, tooltip, type)
    addDescLines(tooltip, getTranslationKey(stack))
  }
}
