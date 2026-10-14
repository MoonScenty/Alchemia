package me.moonscenty.alchemia.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The Crimson Cult's blade. Whatever it cuts is left weak for three seconds and hungry for six, it mends itself a
 * point a second as void metal does, and it is uneasy to hold: two points of warp while it is in hand.
 * <p>
 * The original's sword was three and a half over a plain sword's base. Swords hit a point softer now than they did
 * then, and so does this one; it is still three and a half over the plain sword of today.
 */
public class CrimsonBladeItem extends SwordItem implements WarpingGear {
    private static final int WARP = 2;
    private static final int MENDS_EVERY = 20;
    private static final int WEAKENS_FOR = 60;
    private static final int STARVES_FOR = 120;

    public CrimsonBladeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public int warp(ItemStack stack, Player holder) {
        return WARP;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide && !(target instanceof Player && attacker instanceof Player hitter
                && !hitter.canHarmPlayer((Player) target))) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKENS_FOR), attacker);
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, STARVES_FOR), attacker);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        super.inventoryTick(stack, level, holder, slot, selected);
        if (!level.isClientSide && stack.isDamaged() && holder.tickCount % MENDS_EVERY == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.alchemia.crimson_blade.sapping").withStyle(ChatFormatting.GOLD));
    }
}
