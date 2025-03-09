package io.sc3.plethora.integration.vanilla.meta.entity

import io.sc3.plethora.api.meta.BaseMetaProvider
import io.sc3.plethora.api.meta.BasicMetaProvider
import io.sc3.plethora.api.method.ContextHelpers
import io.sc3.plethora.api.method.IPartialContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.VillagerEntity
import java.util.*

object VillagerEntityMeta : BaseMetaProvider<VillagerEntity>(
  description = "Provides various data about the villager."
){
  override fun getMeta(context: IPartialContext<VillagerEntity>): Map<String, *> {
    var outTrades = ArrayList<HashMap<String,Any?>>()
    for (offer in context.target.offers) {
      var trade = HashMap<String,Any?>()
      trade["originalFirstBuy"] = ContextHelpers.wrapStack(context,offer.originalFirstBuyItem)
      trade["adjustedFirstBuy"] = ContextHelpers.wrapStack(context,offer.adjustedFirstBuyItem)
      trade["secondBuyItem"] = ContextHelpers.wrapStack(context,offer.secondBuyItem)
      trade["sellItem"] = ContextHelpers.wrapStack(context,offer.sellItem)
      trade["isDisabled"] = offer.isDisabled()
      outTrades.add(trade)
    }

    return with(context) {
      mapOf(
        "trades" to outTrades,
        "profession" to target.villagerData.profession.toString(),
        "type" to target.villagerData.type.toString(),
        "level" to target.villagerData.level
      )
    }
  }
}
