package io.sc3.plethora.integration.vanilla.meta.item

import io.sc3.plethora.api.meta.BasicMetaProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries

object ContainerItemMeta : BasicMetaProvider<ItemStack>() {
  override fun getMeta(target: ItemStack): Map<String, *> {
    val container = target.get(DataComponentTypes.CONTAINER) ?: return emptyMap<String, Any>()

    val list = mutableMapOf<Int, Map<String, Any>>()
    container.stream().toList().forEachIndexed { index, stack ->
      if (!stack.isEmpty) {
        list[index + 1] = mapOf(
          "name" to Registries.ITEM.getId(stack.item).toString(),
          "count" to stack.count
        )
      }
    }

    if (list.isEmpty()) return emptyMap<String, Any>()
    return mapOf("list" to list)
  }
}
