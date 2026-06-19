package io.sc3.plethora.gameplay.modules.keyboard

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.plethora.Plethora.ModId
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class KeyboardListenPacket(val listening: Boolean): ScLibraryPacket() {
  override fun getId(): CustomPayload.Id<KeyboardListenPacket> = Companion.id

  fun toBytes(buf: PacketByteBuf) {
    buf.writeBoolean(listening)
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {
    ClientKeyListener.listening = listening
  }

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {}

  companion object {
    @JvmField
    val id = CustomPayload.Id<KeyboardListenPacket>(ModId("keyboard_listen"))

    @JvmField
    val codec: PacketCodec<RegistryByteBuf, KeyboardListenPacket> =
      PacketCodec.of({ value, buf -> value.toBytes(buf) }, ::fromBytes)

    @JvmStatic
    fun fromBytes(buf: PacketByteBuf) =
      KeyboardListenPacket(buf.readBoolean())
  }
}
