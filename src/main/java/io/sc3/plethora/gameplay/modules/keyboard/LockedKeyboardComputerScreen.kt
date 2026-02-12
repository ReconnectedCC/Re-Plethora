package io.sc3.plethora.gameplay.modules.keyboard

import dan200.computercraft.client.gui.NoTermComputerScreen
import dan200.computercraft.client.network.ClientNetworking
import dan200.computercraft.shared.computer.inventory.AbstractComputerMenu
import dan200.computercraft.shared.network.server.ComputerActionServerMessage
import dan200.computercraft.shared.network.server.MouseEventServerMessage
import io.sc3.plethora.Plethora
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.MultilineText
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.text.Text.translatable
import kotlin.math.abs

class LockedKeyboardComputerScreen<T : AbstractComputerMenu>(
  private val screen: T,
  player: PlayerInventory,
  title: Text
) : NoTermComputerScreen<T>(screen, player, title) {
  private val tr by lazy { MinecraftClient.getInstance().textRenderer }

  private var lines: MultilineText = MultilineText.EMPTY
  override fun init() {
    val sosMode = Plethora.config.keyboard.sosMode
    if (sosMode) {
      client!!.setScreen(null)
      // Shut down the computer so it can't softlock the player.
      ClientNetworking.sendToServer(ComputerActionServerMessage(screen,ComputerActionServerMessage.Action.SHUTDOWN))
      return
    }
    super.init()

   client!!.mouse.unlockCursor() // Get back cursor control
    lines = MultilineText.create(tr, translatable("item.plethora.module.module_keyboard.close"), (width * 0.8).toInt())
  }



  override fun getScreenHandler(): T = screen

  override fun render(ctx: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
    // Don't call super.render (will render the pocket computer overlay text)
    // This means that Screen.drawables don't get drawn, but since we have none anyway, that's fine
    lines.drawCenterWithShadow(ctx, width / 2, 10, textRenderer.fontHeight, 0xFFFFFF)
  }

  override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
    val scaleFactorX = 512.0/this.width
    val scaleFactorY = 288.0/this.height
    val newX = (mouseX * scaleFactorX).toInt()
    val newY = (mouseY * scaleFactorY).toInt()
    ClientNetworking.sendToServer(MouseEventServerMessage(screen, MouseEventServerMessage.Action.CLICK,button,
      newX, newY
    ))
    return super.mouseClicked(mouseX, mouseY, button)
  }

  override fun mouseScrolled(mouseX: Double, mouseY: Double, pDelta: Double): Boolean {
    // Send mouse scroll events to the computer
    val scaleFactorX = 512.0/this.width
    val scaleFactorY = 288.0/this.height
    val newX = (mouseX * scaleFactorX).toInt()
    val newY = (mouseY * scaleFactorY).toInt()
    val direction = (pDelta/ abs(pDelta)).toInt() // either -1 or 1, 0 shouldn't be possible?
    ClientNetworking.sendToServer(MouseEventServerMessage(screen, MouseEventServerMessage.Action.SCROLL,direction,newX,newY))
    return false
  }
}
