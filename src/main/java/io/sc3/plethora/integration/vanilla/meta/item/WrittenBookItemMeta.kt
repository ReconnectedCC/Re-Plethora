package io.sc3.plethora.integration.vanilla.meta.item

import io.sc3.plethora.api.meta.BasicMetaProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack

object WrittenBookItemMeta : BasicMetaProvider<ItemStack>() {
  override fun getMeta(target: ItemStack): Map<String, *> {
    val content = target.get(DataComponentTypes.WRITTEN_BOOK_CONTENT) ?: return emptyMap<String, Any>()

    return mapOf(
      "generation" to content.generation(),
      "author" to content.author(),
      "pages" to content.getPages(false).map { it.string },
      "opened" to content.resolved()
    )
  }
}
