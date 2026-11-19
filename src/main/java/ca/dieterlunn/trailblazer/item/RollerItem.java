package ca.dieterlunn.trailblazer.item;

import ca.dieterlunn.trailblazer.TrailblazerConfig;
import ca.dieterlunn.trailblazer.paving.PathPaver;
import ca.dieterlunn.trailblazer.paving.PaveDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Hold use and walk to roll a path along the way. Sneak + use cycles the width.
 * The path runs in the direction the player faces, snapped to the nearest 45 degrees
 * (or 90 degrees when diagonals are disabled in the config).
 */
public class RollerItem extends Item {
    private static final int USE_DURATION = 72000;
    private static final String WIDTH_TAG = "RollerWidth";

    public RollerItem(Properties properties) {
        super(properties);
    }

    public static int getWidth(ItemStack stack) {
        TrailblazerConfig config = TrailblazerConfig.get();
        CompoundTag tag = stack.getTag();
        int width = tag != null && tag.contains(WIDTH_TAG) ? tag.getInt(WIDTH_TAG) : config.defaultWidth;
        return Mth.clamp(width, 1, config.maxWidth);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.trailblazer.roller.tooltip.width", getWidth(stack))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.trailblazer.roller.tooltip.usage")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                int width = getWidth(stack) % TrailblazerConfig.get().maxWidth + 1;
                stack.getOrCreateTag().putInt(WIDTH_TAG, width);
                player.displayClientMessage(Component.translatable("item.trailblazer.roller.width", width)
                        .withStyle(ChatFormatting.GOLD), true);
                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.4F, 1.2F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        // Durability lives in NBT on 1.20.1; losing some while rolling must not interrupt the roll.
        return ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // Durability ticking down while rolling shouldn't make the item bob in the player's hand.
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player) || !player.onGround()) {
            return;
        }

        TrailblazerConfig config = TrailblazerConfig.get();
        PaveDirection direction = PaveDirection.fromYaw(player.getYRot(), config.allowDiagonals);
        // Slightly below the feet so the block stood on is found both on full blocks and on (shorter) paths.
        BlockPos footing = BlockPos.containing(player.getX(), player.getY() - 0.2, player.getZ());

        int converted = PathPaver.pave(serverLevel, player, stack, footing, direction, getWidth(stack));
        if (converted > 0) {
            level.playSound(null, footing, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 0.6F,
                    0.9F + level.getRandom().nextFloat() * 0.2F);
            if (stack.isDamageableItem()) {
                InteractionHand hand = player.getUsedItemHand();
                stack.hurtAndBreak(converted, player, p -> p.broadcastBreakEvent(hand));
            }
        }
    }
}
