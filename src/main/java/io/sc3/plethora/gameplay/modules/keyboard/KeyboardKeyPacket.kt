package io.sc3.plethora.gameplay.modules.keyboard

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.plethora.Plethora.ModId
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.PacketByteBuf.getMaxValidator
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class KeyboardKeyPacket(
  val presses:  List<KeyPressEvent>,
  val chars:    List<CharEvent>,
  val releases: List<Int>
): ScLibraryPacket() {
  override fun getId(): CustomPayload.Id<KeyboardKeyPacket> = id

  fun toBytes(buf: PacketByteBuf) {
    buf.writeCollection(presses) { b, p -> p.toBytes(b) }
    buf.writeCollection(chars) { b, c -> c.toBytes(b) }
    buf.writeCollection(releases, PacketByteBuf::writeVarInt)
  }

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {
    ServerKeyListener.process(ctx.player(), presses, chars, releases)
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {}

  companion object {
    @JvmField
    val id = CustomPayload.Id<KeyboardKeyPacket>(ModId("keyboard_key"))

    @JvmField
    val codec: PacketCodec<RegistryByteBuf, KeyboardKeyPacket> =
      PacketCodec.of({ value, buf -> value.toBytes(buf) }, ::fromBytes)

    @JvmStatic
    fun fromBytes(buf: PacketByteBuf) = KeyboardKeyPacket(
      buf.readCollection(getMaxValidator({ mutableListOf() }, 128)) { KeyPressEvent.fromBytes(it) },
      buf.readCollection(getMaxValidator({ mutableListOf() }, 128)) { CharEvent.fromBytes(it) },
      buf.readCollection(getMaxValidator({ mutableListOf() }, 128)) { it.readVarInt() }
    )
  }
}
