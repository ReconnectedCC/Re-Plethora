package io.sc3.plethora.gameplay.modules;

import com.mojang.authlib.GameProfile;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class BindableModuleItem extends ModuleItem {
    public BindableModuleItem(String itemName, Settings settings) {
        super(itemName, settings);
    }

    public TypedActionResult<ItemStack> onBindableModuleUse(World world, PlayerEntity player, Hand hand) {
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // TODO: Check module blacklist here

        if (world.isClient) return TypedActionResult.success(stack);

        GameProfile profile = player.getGameProfile();
        if (player.isSneaking() && !profile.getName().startsWith("[") && profile.getId() != null) {
            NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
            NbtCompound nbt = customData == null ? new NbtCompound() : customData.copyNbt();

            if (profile.equals(ModuleContextHelpers.getProfile(stack))) {
                nbt.remove("id_lower");
                nbt.remove("id_upper");
                nbt.remove("bound_name");
                if (nbt.isEmpty()) {
                    stack.remove(DataComponentTypes.CUSTOM_DATA);
                } else {
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                }

                player.sendMessage(Text.translatable(getTranslationKey() + ".cleared", player.getName()), true);
            } else {
                UUID id = profile.getId();
                nbt.putLong("id_lower", id.getLeastSignificantBits());
                nbt.putLong("id_upper", id.getMostSignificantBits());
                nbt.putString("bound_name", profile.getName());
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                player.sendMessage(Text.translatable(getTranslationKey() + ".bound", player.getName()), true);
            }
        } else {
            return onBindableModuleUse(world, player, hand);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        String boundName = ModuleContextHelpers.getEntityName(stack);
        if (boundName != null) {
            tooltip.add(Text.translatable(getTranslationKey() + ".binding", boundName));
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        // New in 1.18: Show a glint on bound modules to help the user differentiate them from unbound ones, at least
        // if/while CC still prevents you from equipping bound modules.
        return ModuleContextHelpers.getUuid(stack) != null;
    }
}
