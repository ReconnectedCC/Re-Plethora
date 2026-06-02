package io.sc3.plethora.integration.computercraft.meta.item

import dan200.computercraft.api.media.IMedia
import io.sc3.plethora.api.meta.BasicMetaProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack

class MediaItemMeta : BasicMetaProvider<ItemStack>() {
  override fun getMeta(target: ItemStack): Map<String, *> {
    if (target.item !is IMedia) return emptyMap<String, Any>()
    val label = target.get(DataComponentTypes.CUSTOM_NAME)?.string

    return mapOf(
      "media" to mapOf(
        "label" to label
      )
    )
  }
}
