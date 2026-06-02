package io.sc3.plethora.integration.computercraft.meta.item

import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.shared.turtle.items.TurtleItem
import io.sc3.plethora.api.meta.ItemStackMetaProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack

class TurtleItemMeta : ItemStackMetaProvider<TurtleItem>(TurtleItem::class.java, "turtle") {
  override fun getMeta(stack: ItemStack, item: TurtleItem): Map<String, *> {
    val out: MutableMap<String, Any?> = HashMap()

    stack.get(DataComponentTypes.DYED_COLOR)?.let {
      out["color"] = it.rgb()
      out["colour"] = it.rgb()
    }

    out["fuel"] = TurtleItem.getFuelLevel(stack)

    out["left"] = getUpgrade(TurtleItem.getUpgradeWithData(stack, TurtleSide.LEFT))
    out["right"] = getUpgrade(TurtleItem.getUpgradeWithData(stack, TurtleSide.RIGHT))

    return out
  }

  companion object {
    fun getUpgrade(upgrade: UpgradeData<ITurtleUpgrade>?): Map<String, String>? {
      if (upgrade == null) return null
      val u = upgrade.upgrade()
      return mapOf(
        "id"        to upgrade.holder().registryKey().value.toString(),
        "adjective" to u.adjective.string,
        "type"      to u.upgradeType.toString()
      )
    }
  }
}
