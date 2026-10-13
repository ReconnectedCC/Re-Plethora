package io.sc3.plethora.integration.ledger

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface BlockBreakLogger {
  fun logBlockBreak(
    world: World,
    position: BlockPos,
    prevBlockState: BlockState,
    player: PlayerEntity,
    prevBlockEntity: BlockEntity?,
    source: String,
    didBreak: Boolean
  )
}
