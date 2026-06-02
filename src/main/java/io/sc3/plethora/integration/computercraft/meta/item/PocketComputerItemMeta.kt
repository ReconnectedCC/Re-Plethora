package io.sc3.plethora.integration.computercraft.meta.item

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.shared.pocket.items.PocketComputerItem
import io.sc3.plethora.api.meta.ItemStackMetaProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack

class PocketComputerItemMeta : ItemStackMetaProvider<PocketComputerItem>(PocketComputerItem::class.java, "pocket") {
  override fun getMeta(stack: ItemStack, item: PocketComputerItem): Map<String, *> {
    val out: MutableMap<String, Any?> = HashMap()

    stack.get(DataComponentTypes.DYED_COLOR)?.let {
      out["color"] = it.rgb()
      out["colour"] = it.rgb()
    }

    out["back"] = getUpgrade(PocketComputerItem.getUpgradeWithData(stack))

    return out
  }

  companion object {
    private fun getUpgrade(upgrade: UpgradeData<IPocketUpgrade>?): Map<String, String>? {
      if (upgrade == null) return null
      return mapOf(
        "id"        to upgrade.holder().registryKey().value.toString(),
        "adjective" to upgrade.upgrade().adjective.string
      )
    }
  }
}
