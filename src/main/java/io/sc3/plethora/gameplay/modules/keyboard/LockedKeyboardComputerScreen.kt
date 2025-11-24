package io.sc3.plethora.gameplay.modules.keyboard

import dan200.computercraft.client.gui.NoTermComputerScreen
import dan200.computercraft.client.network.ClientNetworking
import dan200.computercraft.shared.computer.inventory.AbstractComputerMenu
import dan200.computercraft.shared.network.server.MouseEventServerMessage
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.MultilineText
import net.minecraft.client.gui.AbstractParentElement
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ParentElement
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.text.Text.translatable

class LockedKeyboardComputerScreen<T : AbstractComputerMenu>(
  private val screen: T,
  player: PlayerInventory,
  title: Text
) : NoTermComputerScreen<T>(screen, player, title) {
  private val tr by lazy { MinecraftClient.getInstance().textRenderer }

  private var lines: MultilineText = MultilineText.EMPTY

  override fun init() {
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

  override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
    // Replace this with vanilla implementation since I don't want the hotbar changing when people scroll.
    return (Screen::mouseScrolled)(this,pMouseX,pMouseY,pDelta)
  }
}
