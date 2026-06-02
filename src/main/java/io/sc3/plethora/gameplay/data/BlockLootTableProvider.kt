package io.sc3.plethora.gameplay.data

import io.sc3.plethora.gameplay.registry.Registration.ModBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class BlockLootTableProvider(
  out: FabricDataOutput,
  registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricBlockLootTableProvider(out, registriesFuture) {
  override fun generate() {
    addDrop(ModBlocks.MANIPULATOR_MARK_1)
    addDrop(ModBlocks.MANIPULATOR_MARK_2)
  }
}
