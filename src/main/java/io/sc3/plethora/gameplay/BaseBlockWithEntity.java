package io.sc3.plethora.gameplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class BaseBlockWithEntity extends BlockWithEntity {
    protected BaseBlockWithEntity(Settings settings) {
        super(settings);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        super.appendTooltip(stack, context, tooltip, options);
        tooltip.add(Text.translatable(getTranslationKey() + ".desc")
            .formatted(Formatting.GRAY));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) return;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BaseBlockEntity base) {
            base.broken();
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nonnull
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof BaseBlockEntity base ? base.onUse(player, Hand.MAIN_HAND, hit) : ActionResult.PASS;
    }

    @Nonnull
    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof BaseBlockEntity base)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ActionResult result = base.onUse(player, hand, hit);
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemActionResult.SUCCESS;
            case CONSUME -> ItemActionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemActionResult.CONSUME_PARTIAL;
            case FAIL -> ItemActionResult.FAIL;
            case PASS -> ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }
}
