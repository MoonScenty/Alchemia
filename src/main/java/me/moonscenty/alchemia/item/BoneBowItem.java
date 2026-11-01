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

public class BoneBowItem extends BowItem {
    public static final int FULL_DRAW = 10;
    public static final int LOOSES_AFTER = 18;
    public static final float SPEED = 3.75F;
    public static final double HARDER = 0.5;

    public BoneBowItem(Properties properties) {
        super(properties);
    }

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
