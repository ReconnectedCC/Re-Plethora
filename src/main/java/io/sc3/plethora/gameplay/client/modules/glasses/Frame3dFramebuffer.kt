package io.sc3.plethora.gameplay.client.modules.glasses

import io.sc3.plethora.Plethora
import io.sc3.plethora.gameplay.modules.glasses.canvas.CanvasHandler.HEIGHT
import io.sc3.plethora.gameplay.modules.glasses.canvas.CanvasHandler.WIDTH
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gl.SimpleFramebuffer

@Environment(EnvType.CLIENT)
object Frame3dFramebuffer {
  val framebuffer by lazy {
    Plethora.log.debug("Creating ObjectFrame3d framebuffer with size $WIDTH x $HEIGHT")
    SimpleFramebuffer(WIDTH, HEIGHT, true, true)
  }
}
