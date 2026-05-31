package io.sc3.plethora.integration.computercraft.meta.item

import dan200.computercraft.shared.media.items.PrintoutItem
import dan200.computercraft.shared.media.items.PrintoutData
import io.sc3.plethora.api.meta.ItemStackMetaProvider
import net.minecraft.item.ItemStack

class PrintoutItemMeta : ItemStackMetaProvider<PrintoutItem>(PrintoutItem::class.java, "printout") {
  override fun getMeta(stack: ItemStack, item: PrintoutItem): Map<String, *> {
    val lines: MutableMap<Int, String> = HashMap()
    val printout = PrintoutData.getOrEmpty(stack)
    val lineArray = printout.lines()
    for (i in lineArray.indices) {
      lines[i + 1] = lineArray[i].text()
    }

    return mapOf(
      "title" to printout.title(),
      "pages" to printout.pages(),
      "lines" to lines
    )
  }
}
