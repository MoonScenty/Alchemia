package me.moonscenty.alchemia.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The hoe of growth: an alchemium hoe with order and entropy bound into it.
 *
 * <p>It tills three by three. Where there is nothing to till it makes things grow instead, as bone meal does, at
 * three points of its edge a time. Crouching puts it back to being an ordinary hoe.
 *
 * <p>It does not take enchantments as well as the other four. The original had it so, and a hoe is not much of a
 * thing to enchant.
 */
public class ElementalHoeItem extends HoeItem {
    /** What making something grow costs the edge, and how well it takes enchanting. The original's numbers. */
    private static final int GROWING_WEARS = 3;
    private static final int ENCHANTS = 5;

    public ElementalHoeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public int getEnchantmentValue() {
        return ENCHANTS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        boolean tilled = false;
        BlockPos middle = context.getClickedPos();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos at = middle.offset(x, 0, z);
                UseOnContext there = new UseOnContext(context.getLevel(), player, context.getHand(),
                        context.getItemInHand(), new BlockHitResult(context.getClickLocation().add(x, 0, z),
                                context.getClickedFace(), at, context.isInside()));
                if (super.useOn(there).consumesAction()) {
                    tilled = true;
                }
            }
        }
        if (tilled) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return grow(context, player);
    }

    /** Nothing to till, so something to grow: bone meal's own working, paid for out of the edge. */
    private InteractionResult grow(UseOnContext context, Player player) {
        Level level = context.getLevel();
        BlockPos at = context.getClickedPos();
        if (!BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, at)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.levelEvent(1505, at, 15);
            context.getItemInHand().hurtAndBreak(GROWING_WEARS, player,
                    LivingEntity.getSlotForHand(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
