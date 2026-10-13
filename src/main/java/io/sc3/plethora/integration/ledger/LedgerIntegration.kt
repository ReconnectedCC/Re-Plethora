package io.sc3.plethora.integration.ledger

import com.github.quiltservertools.ledger.Ledger.api
import com.github.quiltservertools.ledger.actionutils.ActionFactory.blockBreakAction
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class LedgerIntegration : BlockBreakLogger {
  override fun logBlockBreak(world: World, position: BlockPos, prevBlockState: BlockState, player: PlayerEntity, prevBlockEntity: BlockEntity?, source: String, didBreak: Boolean) {
    if (!didBreak) return;

    api.logAction(blockBreakAction(world, position, prevBlockState, player, prevBlockEntity, source))
  }
}
