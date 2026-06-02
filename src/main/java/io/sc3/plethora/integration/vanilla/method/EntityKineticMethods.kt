package io.sc3.plethora.integration.vanilla.method

import dan200.computercraft.api.lua.IArguments
import io.sc3.plethora.api.method.ArgumentExt.optHand
import io.sc3.plethora.api.method.FutureMethodResult
import io.sc3.plethora.api.method.IUnbakedContext
import io.sc3.plethora.api.module.IModuleContainer
import io.sc3.plethora.api.module.SubtargetedModuleMethod
import io.sc3.plethora.gameplay.modules.kinetic.KineticMethods
import io.sc3.plethora.gameplay.registry.PlethoraModules.KINETIC_M
import io.sc3.plethora.integration.PlayerInteractionHelpers
import io.sc3.plethora.mixin.ServerPlayerInteractionManagerAccessor
import io.sc3.plethora.mixin.ServerPlayerInteractionManagerInvoker
import io.sc3.plethora.util.Helpers.normaliseAngle
import io.sc3.plethora.util.PlayerHelpers
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object EntityKineticMethods {
  private val LOOK_FLAGS = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z)
  private val trackedDigEvents = ConcurrentHashMap<ServerPlayerEntity, BlockPos>();

  val LOOK = SubtargetedModuleMethod.of(
    "look", KINETIC_M, LivingEntity::class.java,
    "function(yaw:number, pitch:number) -- Look in a set direction", ::look
  )
  private fun look(unbaked: IUnbakedContext<IModuleContainer>, args: IArguments): FutureMethodResult {
    val entity = KineticMethods.getContext(unbaked).entity

    val yaw = normaliseAngle(args.getFiniteDouble(0)).toFloat()
    val pitch = MathHelper.clamp(normaliseAngle(args.getFiniteDouble(1)), -90.0, 90.0).toFloat()

    if (entity is ServerPlayerEntity) {
      val pos = entity.getPos()
      entity.networkHandler.requestTeleport(pos.getX(), pos.getY(), pos.getZ(), yaw, pitch, LOOK_FLAGS)
    } else {
      entity.yaw = yaw
      entity.setBodyYaw(yaw)
      entity.setHeadYaw(yaw)
      entity.pitch = pitch
    }

    return FutureMethodResult.empty()
  }

  val USE = SubtargetedModuleMethod.of(
    "use", KINETIC_M, LivingEntity::class.java,
    "function([duration:integer], [hand:string]):boolean, string|nil -- Right click with this item using a " +
      "particular hand (\"main\" or \"off\"). The duration is in ticks, or 1/20th of a second.",
    ::use
  )
  private fun use(unbaked: IUnbakedContext<IModuleContainer>, args: IArguments): FutureMethodResult {
    val ctx = KineticMethods.getContext(unbaked)
    val playerCtx = KineticMethods.getPlayer(ctx)
    val player = playerCtx.player
    val fakePlayer = playerCtx.fakePlayer

    val duration = args.optInt(0, 0)
    val hand = args.optHand(1)

    return try {
      val hit = PlayerHelpers.raycast(player)
      PlayerInteractionHelpers.use(player, hit, hand, duration)
    } finally {
      player.clearActiveItem()
      fakePlayer?.updateCooldown()
    }
  }

  val SWING = SubtargetedModuleMethod.of(
    "swing", KINETIC_M, LivingEntity::class.java,
    "function():boolean, string|nil -- Left click with the item in the main hand. Returns the action taken."
  ) { unbaked, _ -> swing(unbaked) }
  private fun swing(unbaked: IUnbakedContext<IModuleContainer>): FutureMethodResult {
    val ctx = KineticMethods.getContext(unbaked)
    val playerCtx = KineticMethods.getPlayer(ctx)
    val player = playerCtx.player
    val fakePlayer = playerCtx.fakePlayer

    return try {
      val baseHit = PlayerHelpers.raycast(player)
      when (baseHit.type) {
        HitResult.Type.ENTITY -> {
          val hit = baseHit as EntityHitResult
          val result = PlayerInteractionHelpers.attack(player, hit.entity, hit)
          FutureMethodResult.result(result.left, result.right)
        }
        HitResult.Type.BLOCK -> {
          val hit = baseHit as BlockHitResult
          if (fakePlayer != null) {
            val result = fakePlayer.dig(hit.blockPos, hit.side)
            FutureMethodResult.result(result.left, result.right)
          } else {
            if(player.world.canPlayerModifyAt(player, baseHit.blockPos)){
              startDigging(player, hit)
              FutureMethodResult.result(true, "Block")
            }else {
              FutureMethodResult.result(false, "No permission to break here")
            }
          }
        }
        else -> {
          FutureMethodResult.result(false, "Nothing to do here")
        }
      }
    } finally {
      player.clearActiveItem()
      fakePlayer?.updateCooldown()
    }
  }

  val IS_SWINGING = SubtargetedModuleMethod.of(
    "isSwinging", KINETIC_M, LivingEntity::class.java,
    "function():boolean -- Returns true if the player is currently swinging their main hand."
  ) { unbaked, _ -> isSwinging(unbaked) }
  private fun isSwinging(unbaked: IUnbakedContext<IModuleContainer>): FutureMethodResult {
    val ctx = KineticMethods.getContext(unbaked)
    val playerCtx = KineticMethods.getPlayer(ctx)
    val player = playerCtx.player

    return FutureMethodResult.result(trackedDigEvents.containsKey(player))
  }

  val STOP_SWINGING = SubtargetedModuleMethod.of(
    "stopSwinging", KINETIC_M, LivingEntity::class.java,
    "function():boolean -- Stops the player from swinging their main hand."
  ) { unbaked, _ -> stopSwinging(unbaked) }
  private fun stopSwinging(unbaked: IUnbakedContext<IModuleContainer>): FutureMethodResult {
    val ctx = KineticMethods.getContext(unbaked)
    val playerCtx = KineticMethods.getPlayer(ctx)
    val player = playerCtx.player

    if(!trackedDigEvents.containsKey(player)){
      return FutureMethodResult.result(false)
    }

    stopDigging(player)
    return FutureMethodResult.result(true)
  }

  private fun startDigging(player: ServerPlayerEntity, baseHit: BlockHitResult) {
    if(trackedDigEvents.containsKey(player)){
      stopDigging(player)
    }

    player.interactionManager.processBlockBreakingAction(
      baseHit.blockPos,
      PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
      baseHit.side,
      player.world.topY,
      0
    )

    trackedDigEvents[player] = baseHit.blockPos
  }

  private fun stopDigging(player: ServerPlayerEntity) {
    if(trackedDigEvents.containsKey(player)){
      setBlockBreakingInfo(player.server, player.world, player.id, trackedDigEvents[player]!!, -1)
      trackedDigEvents.remove(player)
    }
  }

  private fun setBlockBreakingInfo(server: MinecraftServer, world: World, entityId: Int, pos: BlockPos, progress: Int) {
    val var4: Iterator<*> = server.playerManager.playerList.iterator()
    while (var4.hasNext()) {
      val serverPlayerEntity = var4.next() as ServerPlayerEntity?
      if (serverPlayerEntity != null && serverPlayerEntity.world === world) {
        val d = pos.x.toDouble() - serverPlayerEntity.x
        val e = pos.y.toDouble() - serverPlayerEntity.y
        val f = pos.z.toDouble() - serverPlayerEntity.z
        if (d * d + e * e + f * f < 1024.0) {
          serverPlayerEntity.networkHandler.sendPacket(BlockBreakingProgressS2CPacket(entityId, pos, progress))
        }
      }
    }
  }

  @JvmStatic
  fun initKineticDigTracker(){
    ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { server ->
      for (event in trackedDigEvents) {
        val player = event.key
        val pos = event.value

        if(player.isDisconnected){
          trackedDigEvents.remove(player)
          continue
        }

        if(player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) > 36.0){
          stopDigging(player)
          continue
        }

        val state = player.world.getBlockState(pos)

        //Check if block is still there
        if(state.isAir
          || state.block is FluidBlock
          || state.block === Blocks.BEDROCK
          || state.getHardness(player.world, pos) <= -1)
        {
          stopDigging(player)
          continue
        }

        //Check if player is still looking at the block
        val hit = PlayerHelpers.raycast(player)
        if(hit.type != HitResult.Type.BLOCK || (hit as BlockHitResult).blockPos != pos){
          stopDigging(player)
          continue
        }

        val interactionManager = player.interactionManager as ServerPlayerInteractionManagerAccessor
        val tickStartedMining = interactionManager.startMiningTime
        val currentTick = interactionManager.tickCounter

        if((currentTick - tickStartedMining) % 10 == 0){
          player.swingHand(Hand.MAIN_HAND, true)
        }

        val progress = (interactionManager as ServerPlayerInteractionManagerInvoker).invokeContinueMining(player.world.getBlockState(pos), pos, tickStartedMining)

        //Call custom setBlockBreakingInfo so block update is actually sent to triggering player
        setBlockBreakingInfo(server, player.world, player.id, pos, (progress * 10).toInt())

        if(progress >= 1){
          player.interactionManager.tryBreakBlock(pos)
          trackedDigEvents.remove(player)
        }
      }
    })
  }
}
