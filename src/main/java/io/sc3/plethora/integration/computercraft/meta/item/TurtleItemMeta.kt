package io.sc3.plethora.integration.computercraft.meta.item

import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.shared.turtle.items.TurtleItem
import io.sc3.plethora.api.meta.ItemStackMetaProvider
import net.minecraft.item.ItemStack

class TurtleItemMeta : ItemStackMetaProvider<TurtleItem>(TurtleItem::class.java, "turtle") {
  override fun getMeta(stack: ItemStack, item: TurtleItem): Map<String, *> {
    val out: MutableMap<String, Any?> = HashMap()

    out["fuel"] = TurtleItem.getFuelLevel(stack)

    out["left"] = getUpgrade(TurtleItem.getUpgrade(stack, TurtleSide.LEFT))
    out["right"] = getUpgrade(TurtleItem.getUpgrade(stack, TurtleSide.RIGHT))

    return out
  }

  companion object {
    fun getUpgrade(upgrade: ITurtleUpgrade?): Map<String, String>? {
      if (upgrade == null) return null
      return mapOf(
        "id"        to upgrade.toString(),
        "adjective" to upgrade.adjective.string,
        "type"      to upgrade.type.toString()
      )
    }
  }
}
