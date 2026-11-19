package ca.dieterlunn.trailblazer.item;

import ca.dieterlunn.trailblazer.TrailblazerConfig;
import ca.dieterlunn.trailblazer.paving.PathPaver;
import ca.dieterlunn.trailblazer.paving.PaveDirection;
import ca.dieterlunn.trailblazer.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Hold use and walk to roll a path along the way. Sneak + use cycles the width.
 * The path runs in the direction the player faces, snapped to the nearest 45 degrees
 * (or 90 degrees when diagonals are disabled in the config).
 */
public class RollerItem extends Item {
    private static final int USE_DURATION = 72000;

    public RollerItem(Properties properties) {
        super(properties);
    }

    /** Lets the player keep walking at a reasonable pace while holding the roller down. */
    public static Properties withUseEffects(Properties properties) {
        // (canSprint, interactVibrations, speedMultiplier). Vanilla item use slows players to 20%.
        return properties.component(DataComponents.USE_EFFECTS,
                new UseEffects(false, true, TrailblazerConfig.get().rollingSpeed));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.trailblazer.roller.tooltip.width", getWidth(stack))
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.trailblazer.roller.tooltip.usage")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static int getWidth(ItemStack stack) {
        TrailblazerConfig config = TrailblazerConfig.get();
        int width = stack.getOrDefault(ModDataComponents.ROLLER_WIDTH.get(), config.defaultWidth);
        return Mth.clamp(width, 1, config.maxWidth);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                int width = getWidth(stack) % TrailblazerConfig.get().maxWidth + 1;
                stack.set(ModDataComponents.ROLLER_WIDTH.get(), width);
                player.sendOverlayMessage(Component.translatable("item.trailblazer.roller.width", width)
                        .withStyle(ChatFormatting.GOLD));
                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.4F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
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
            level.playSound(null, footing, SoundEvents.SHOVEL_FLATTEN.value(), SoundSource.BLOCKS, 0.6F,
                    0.9F + level.getRandom().nextFloat() * 0.2F);
            if (stack.isDamageableItem()) {
                stack.hurtAndBreak(converted, player, player.getUsedItemHand());
            }
        }
    }
}
