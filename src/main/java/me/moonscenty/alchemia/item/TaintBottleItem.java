package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.entity.ThrownTaintBottle;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A bottle of taint, the original's {@code ItemBottleTaint}: thrown, gently and a little upward, to break and taint
 * the ground where it lands.
 */
public class TaintBottleItem extends Item {
    /** How hard it is thrown, and how far above the line of sight. The original's. */
    private static final float SPEED = 0.5F;
    private static final float LOFT = -20.0F;

    public TaintBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            ThrownTaintBottle bottle = new ThrownTaintBottle(level, player);
            bottle.setItem(held);
            bottle.shootFromRotation(player, player.getXRot() + LOFT, player.getYRot(), 0.0F, SPEED, 1.0F);
            level.addFreshEntity(bottle);
        }
        held.consume(1, player);
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }
}
