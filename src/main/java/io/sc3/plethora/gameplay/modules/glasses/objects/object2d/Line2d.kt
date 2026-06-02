package io.sc3.plethora.gameplay.modules.glasses.objects.object2d

import com.google.common.base.Objects
import com.mojang.blaze3d.systems.RenderSystem
import io.sc3.plethora.gameplay.modules.glasses.canvas.CanvasClient
import io.sc3.plethora.gameplay.modules.glasses.objects.ColourableObject
import io.sc3.plethora.gameplay.modules.glasses.objects.ObjectRegistry.LINE_2D
import io.sc3.plethora.gameplay.modules.glasses.objects.Scalable
import io.sc3.plethora.util.ByteBufUtils
import io.sc3.plethora.util.DirtyingProperty
import io.sc3.plethora.util.Vec2d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.network.PacketByteBuf
import javax.annotation.Nonnull

class Line2d(
  id: Int,
  parent: Int
) : ColourableObject(id, parent, LINE_2D), Scalable, MultiPoint2d {
  private var start = Vec2d.ZERO
  private var end = Vec2d.ZERO

  /** Line thickness */
  override var scale by DirtyingProperty(1f)

  @Nonnull
  override fun getPoint(idx: Int): Vec2d = if (idx == 0) start else end

  override fun setVertex(idx: Int, @Nonnull point: Vec2d) {
    if (idx == 0) {
      if (!Objects.equal(start, point)) {
        start = point
        setDirty()
      }
    } else {
      if (!Objects.equal(end, point)) {
        end = point
        setDirty()
      }
    }
  }

  override val vertices: Int
    get() = 2

  override fun writeInitial(@Nonnull buf: PacketByteBuf) {
    super.writeInitial(buf)
    ByteBufUtils.writeVec2d(buf, start)
    ByteBufUtils.writeVec2d(buf, end)
    buf.writeFloat(scale)
  }

  override fun readInitial(@Nonnull buf: PacketByteBuf) {
    super.readInitial(buf)
    start = ByteBufUtils.readVec2d(buf)
    end = ByteBufUtils.readVec2d(buf)
    scale = buf.readFloat()
  }

  @Environment(EnvType.CLIENT)
  override fun draw(canvas: CanvasClient, ctx: DrawContext, consumers: VertexConsumerProvider?) {
    setupFlat()

    val matrices = ctx.matrices
    val buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR)
    val matrix = matrices.peek().positionMatrix
    val normal = matrices.peek().normalMatrix

    // OpenGL has some limitations on normal lines, so we instead will create a quad,
    // and use two triangles to "build" the quad.
    RenderSystem.setShader(GameRenderer::getPositionColorProgram)

    // Calculate thickness and the perpendicular direction to do it in.
    val dx = (end.y - start.y).toFloat()
    val dy = -(end.x - start.x).toFloat()
    val length = kotlin.math.sqrt(dx * dx + dy * dy)
    val thickness = scale / 2f

    // Normalize and scale by thickness
    val offsetX = (dx / length) * thickness
    val offsetY = (dy / length) * thickness

    // Create four corners of the quad
    val x1 = start.x.toFloat() - offsetX
    val y1 = start.y.toFloat() - offsetY
    val x2 = start.x.toFloat() + offsetX
    val y2 = start.y.toFloat() + offsetY
    val x3 = end.x.toFloat() - offsetX
    val y3 = end.y.toFloat() - offsetY
    val x4 = end.x.toFloat() + offsetX
    val y4 = end.y.toFloat() + offsetY

    // First Triangle: x1/y1 -> x2/y2 -> x3/y3
    buffer.vertex(matrix, x1, y1, 0f).color(red, green, blue, alpha).normal(0f, 1f, 0f)
    buffer.vertex(matrix, x2, y2, 0f).color(red, green, blue, alpha).normal(0f, 1f, 0f)
    buffer.vertex(matrix, x3, y3, 0f).color(red, green, blue, alpha).normal(0f, 1f, 0f)

    // Second Triangle: x3/y3 -> x2/y2 -> x4/y4
    buffer.vertex(matrix, x3, y3, 0f).color(red, green, blue, alpha).normal(0f, 1f, 0f)
    buffer.vertex(matrix, x2, y2, 0f).color(red, green, blue, alpha).normal(0f, 1f, 0f)
    buffer.vertex(matrix, x4, y4, 0f).color(red, green, blue, alpha).normal(0f, 1f, 0f)

    BufferRenderer.drawWithGlobalProgram(buffer.end())
  }
}
