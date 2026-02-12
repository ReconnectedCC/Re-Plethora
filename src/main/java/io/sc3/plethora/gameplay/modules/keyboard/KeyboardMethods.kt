package io.sc3.plethora.gameplay.modules.keyboard

import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.core.apis.ComputerAccess
import dan200.computercraft.core.apis.PeripheralAPI
import dan200.computercraft.shared.computer.core.ServerComputer
import dan200.computercraft.shared.network.container.ComputerContainerData
import dan200.computercraft.shared.platform.PlatformHelper
import io.sc3.plethora.Plethora
import io.sc3.plethora.api.method.ContextKeys
import io.sc3.plethora.api.method.FutureMethodResult
import io.sc3.plethora.api.method.IUnbakedContext
import io.sc3.plethora.api.module.IModuleContainer
import io.sc3.plethora.api.module.SubtargetedModuleMethod
import io.sc3.plethora.gameplay.modules.introspection.IntrospectionContextHelpers
import io.sc3.plethora.gameplay.neural.NeuralPocketAccess
import io.sc3.plethora.gameplay.registry.PlethoraModules.KEYBOARD_M
import io.sc3.plethora.integration.EntityIdentifier
import io.sc3.plethora.mixin.computercraft.ComputerAccessAccessor
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import java.util.concurrent.Callable

object KeyboardMethods {
   val EXAMPLE = SubtargetedModuleMethod.of(
      "example", KEYBOARD_M, EntityIdentifier::class.java,
      "function():string -- Example method. Kept for workaround purposes."
    ) { unbaked, _ -> example(unbaked) }
    private fun example(unbaked: IUnbakedContext<IModuleContainer>): FutureMethodResult {
      return FutureMethodResult.result("Example method result.")
    }

  val CAPTURE_INPUT = SubtargetedModuleMethod.of("captureInput",KEYBOARD_M, EntityIdentifier::class.java,"function() -- Opens the keyboard screen"){unbaked, _ -> captureInput(unbaked)}
  private fun captureInput(unbaked: IUnbakedContext<IModuleContainer>): FutureMethodResult {
    return unbaked.costHandler.await(1.0, FutureMethodResult.nextTick(Callable {
      val ctx = unbaked.bake()
      val computer = ctx.getContext(ContextKeys.COMPUTER, ComputerAccess::class.java)
      computer as ComputerAccessAccessor
      val serverContext = IntrospectionContextHelpers.getServerContext(unbaked)
      val player = serverContext.entity.getEntity(serverContext.server)
      if (player is ServerPlayerEntity) {
        Plethora.log.debug("Tried to open keyboard for {}, on computer {}",player,computer.environment.computerEnvironment)
        PlatformHelper.get().openMenu(player,Text.of("Keyboard"), LockedKeyboardScreenHandlerFactory(computer.environment.computerEnvironment as ServerComputer, Text.of("Keyboard")
        ),
          ComputerContainerData(computer.environment.computerEnvironment as ServerComputer,player.getStackInHand(Hand.MAIN_HAND)))

      }
      FutureMethodResult.empty()
    }))
  }
  }
