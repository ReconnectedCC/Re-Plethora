package io.sc3.plethora.integration.vanilla.meta.item

import io.sc3.plethora.api.meta.ItemStackMetaProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack

/**
 * Meta provider for armour properties. Material is handled in [ItemMaterialMeta].
 */
object ArmorItemMeta : ItemStackMetaProvider<ArmorItem>(ArmorItem::class.java, "armor",
  description = "Provides type and colour of armour.") {
  override fun getMeta(stack: ItemStack, item: ArmorItem): Map<String, *> {
    val data = mutableMapOf<String, Any>(
      "armorType" to item.slotType.getName()
    )

    val dyed = stack.get(DataComponentTypes.DYED_COLOR)
    if (dyed != null) {
      data["color"] = dyed.rgb()
      data["colour"] = dyed.rgb()
    }

    return data
  }
}
