package io.sc3.plethora.gameplay.data

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.turtle.ITurtleUpgrade
import io.sc3.plethora.core.PocketUpgradeModule
import io.sc3.plethora.core.TurtleUpgradeModule
import io.sc3.plethora.gameplay.modules.ModuleItem
import io.sc3.plethora.gameplay.modules.kinetic.KineticTurtleUpgrade
import io.sc3.plethora.gameplay.registry.Registration.ModItems
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryKey

private fun adjective(item: ModuleItem) = item.translationKey + ".adjective"

fun bootstrapTurtleUpgrades(registerable: Registerable<ITurtleUpgrade>) {
  fun module(item: ModuleItem) = registerable.register(
    RegistryKey.of(ITurtleUpgrade.REGISTRY, item.module),
    TurtleUpgradeModule(ItemStack(item), item, adjective(item))
  )

  module(ModItems.LASER_MODULE)
  module(ModItems.SCANNER_MODULE)
  module(ModItems.SENSOR_MODULE)
  module(ModItems.INTROSPECTION_MODULE)

  val kinetic = ModItems.KINETIC_MODULE
  registerable.register(
    RegistryKey.of(ITurtleUpgrade.REGISTRY, kinetic.module),
    KineticTurtleUpgrade(ItemStack(kinetic), kinetic, adjective(kinetic))
  )
}

fun bootstrapPocketUpgrades(registerable: Registerable<IPocketUpgrade>) {
  fun module(item: ModuleItem) = registerable.register(
    RegistryKey.of(IPocketUpgrade.REGISTRY, item.module),
    PocketUpgradeModule(ItemStack(item), item, adjective(item))
  )

  module(ModItems.LASER_MODULE)
  module(ModItems.SCANNER_MODULE)
  module(ModItems.SENSOR_MODULE)
  module(ModItems.INTROSPECTION_MODULE)
  module(ModItems.KINETIC_MODULE)
  module(ModItems.KEYBOARD_MODULE)
}
