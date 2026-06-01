package io.sc3.plethora.integration.vanilla.meta.item

import io.sc3.plethora.api.meta.BasicMetaProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack

object WritableBookItemMeta : BasicMetaProvider<ItemStack>() {
  override fun getMeta(target: ItemStack): Map<String, *> {
    val content = target.get(DataComponentTypes.WRITABLE_BOOK_CONTENT) ?: return emptyMap<String, Any>()

    return mapOf(
      "pages" to content.pages().map { it.raw() }
    )
  }
}
