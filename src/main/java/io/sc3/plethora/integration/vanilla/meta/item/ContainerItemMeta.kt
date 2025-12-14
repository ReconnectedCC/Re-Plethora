package io.sc3.plethora.integration.vanilla.meta.item

import io.sc3.plethora.api.meta.ItemStackMetaProvider
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound


object ContainerItemMeta : ItemStackMetaProvider<BlockItem>(BlockItem::class.java) {

  override fun getMeta(stack: ItemStack, item: BlockItem): MutableMap<String, *> {
    val data = mutableMapOf<String, Any>()

    val tag = stack.getSubNbt("BlockEntityTag") ?: return data
    if (!tag.contains("Items", NbtCompound.LIST_TYPE.toInt())) return data

    val items = tag.getList("Items", NbtCompound.COMPOUND_TYPE.toInt())


    val list = mutableMapOf<Int, Map<String, Any>>()

    for (element in items) {
      val itemTag = element as NbtCompound
      val slot = itemTag.getByte("Slot").toInt() + 1

      list[slot] = mapOf(
        "name" to itemTag.getString("id"),
        "count" to itemTag.getByte("Count").toInt()
      )
    }

    data["list"] = list
    return data
  }
}

