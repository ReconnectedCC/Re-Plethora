package io.sc3.plethora.gameplay.modules.glasses.networking

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.plethora.Plethora.ModId
import io.sc3.plethora.gameplay.modules.glasses.canvas.CanvasHandler.getClient
import io.sc3.plethora.gameplay.modules.glasses.canvas.CanvasHandler.removeClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class CanvasRemovePacket(var canvasId: Int = 0): ScLibraryPacket() {
  override fun getId(): CustomPayload.Id<CanvasRemovePacket> = id

  fun toBytes(buf: PacketByteBuf) {
    buf.writeInt(canvasId)
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {
    val canvas = getClient(canvasId) ?: return
    removeClient(canvas)
  }

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {}

  companion object {
    @JvmField
    val id = CustomPayload.Id<CanvasRemovePacket>(ModId("canvas_remove"))

    @JvmField
    val codec: PacketCodec<RegistryByteBuf, CanvasRemovePacket> =
      PacketCodec.of({ value, buf -> value.toBytes(buf) }, ::fromBytes)

    @JvmStatic
    fun fromBytes(buf: PacketByteBuf) =
      CanvasRemovePacket(buf.readInt())
  }
}
