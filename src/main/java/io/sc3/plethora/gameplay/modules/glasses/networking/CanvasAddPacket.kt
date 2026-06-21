package io.sc3.plethora.gameplay.modules.glasses.networking

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.plethora.Plethora.ModId
import io.sc3.plethora.gameplay.modules.glasses.canvas.CanvasClient
import io.sc3.plethora.gameplay.modules.glasses.canvas.CanvasHandler
import io.sc3.plethora.gameplay.modules.glasses.objects.BaseObject
import io.sc3.plethora.gameplay.modules.glasses.objects.ObjectRegistry.read
import io.sc3.plethora.gameplay.modules.glasses.objects.ObjectRegistry.write
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class CanvasAddPacket(
  var canvasId: Int = 0,
  var objects: Collection<BaseObject> = emptyList()
): ScLibraryPacket() {
  override fun getId(): CustomPayload.Id<CanvasAddPacket> = Companion.id

  fun toBytes(buf: PacketByteBuf) {
    buf.writeInt(canvasId)
    buf.writeCollection(objects, ::write)
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {
    val canvas = CanvasClient(canvasId)
    objects.onEach(canvas::updateObject)
    CanvasHandler.addClient(canvas)
  }

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {}

  companion object {
    @JvmField
    val id = CustomPayload.Id<CanvasAddPacket>(ModId("canvas_add"))

    @JvmField
    val codec: PacketCodec<RegistryByteBuf, CanvasAddPacket> =
      PacketCodec.of({ value, buf -> value.toBytes(buf) }, ::fromBytes)

    @JvmStatic
    fun fromBytes(buf: PacketByteBuf): CanvasAddPacket {
      val canvasId = buf.readInt()
      val objects = buf.readCollection({ mutableListOf<BaseObject>() }, ::read)

      objects.sortWith(BaseObject.SORTING_ORDER) // Sort by ID to guarantee parents are loaded before their children

      return CanvasAddPacket(canvasId, objects)
    }
  }
}
