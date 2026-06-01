package io.sc3.plethora.integration.vanilla.meta.item

import io.sc3.plethora.api.meta.BasicMetaProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack

object BannerItemMeta : BasicMetaProvider<ItemStack>() {
  override fun getMeta(target: ItemStack): Map<String, *> {
    val patterns = target.get(DataComponentTypes.BANNER_PATTERNS) ?: return emptyMap<String, Any>()

    val banner = patterns.layers().take(6).mapNotNull { layer ->
      val id = layer.pattern().key.orElse(null)?.value?.toString() ?: return@mapNotNull null
      val colour = layer.color().getName()
      mapOf(
        "id" to id,
        "name" to id,
        "colour" to colour,
        "color" to colour
      )
    }

    return mapOf("banner" to banner)
  }
}
