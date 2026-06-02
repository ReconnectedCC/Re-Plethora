package io.sc3.plethora.integration.vanilla.meta.item

import io.sc3.plethora.api.meta.BasicMetaProvider
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterials

object ItemMaterialMeta : BasicMetaProvider<ItemStack>() {
  override fun getMeta(target: ItemStack): Map<String, *> {
    val item = target.item
    val name = when (item) {
      is ToolItem -> when (item.material) {
        ToolMaterials.WOOD      -> "wood"
        ToolMaterials.STONE     -> "stone"
        ToolMaterials.IRON      -> "iron"
        ToolMaterials.GOLD      -> "gold"
        ToolMaterials.DIAMOND   -> "diamond"
        ToolMaterials.NETHERITE -> "netherite"
        else                    -> "unknown"
      }
      is ArmorItem -> item.material.key.orElse(null)?.value?.path
      else -> null
    }

    return if (name != null) mapOf("material" to name) else emptyMap<String, Any>()
  }
}
