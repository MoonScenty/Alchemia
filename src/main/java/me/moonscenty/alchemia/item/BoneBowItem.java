package me.moonscenty.alchemia.item;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The bow of bone, the original's {@code ItemBowBone}: bone and string with entropy worked into it. It is fully
 * drawn in half the time a bow takes, looses on its own if held past that, sends its arrows a quarter as fast again
 * and half a point harder -- but never as a critical shot.
 */
public class BoneBowItem extends BowItem {
    /** Ticks to a full draw: a bow's twenty, halved. */
    public static final int FULL_DRAW = 10;
    /** Held this long, it looses by itself. */
    public static final int LOOSES_AFTER = 18;
    /** How hard it sends an arrow at full draw, against a bow's three. */
    public static final float SPEED = 3.75F;
    /** What it adds to an arrow's damage. */
    public static final double HARDER = 0.5;

    public BoneBowItem(Properties properties) {
        super(properties);
    }

    /** How far drawn, out of one, after this many ticks: a bow's curve over half the time. */
    public static float power(int ticks) {
        float f = ticks / (float) FULL_DRAW;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (getUseDuration(stack, user) - remaining > LOOSES_AFTER) {
            user.releaseUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack bow, Level level, LivingEntity user, int remaining) {
        if (!(user instanceof Player player)) {
            return;
        }
        ItemStack ammo = player.getProjectile(bow);
        if (ammo.isEmpty()) {
            return;
        }
        int ticks = net.neoforged.neoforge.event.EventHooks.onArrowLoose(bow, level, player,
                getUseDuration(bow, user) - remaining, true);
        if (ticks < 0) {
            return;
        }
        float power = power(ticks);
        if (power < 0.1F) {
            return;
        }
        List<ItemStack> drawn = draw(bow, ammo, player);
        if (level instanceof ServerLevel server && !drawn.isEmpty()) {
            shoot(server, player, player.getUsedItemHand(), bow, drawn, power * SPEED, 1.0F, false, null);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectile, ItemStack weapon) {
        arrow.setBaseDamage(arrow.getBaseDamage() + HARDER);
        return arrow;
    }

    @Override
    public int getEnchantmentValue() {
        return 3;
    }
}
