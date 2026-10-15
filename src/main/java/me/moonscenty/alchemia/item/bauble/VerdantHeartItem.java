package me.moonscenty.alchemia.item.bauble;

import java.util.List;

import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;

/**
 * The verdant heart band, that holds two hundred of life and spends it on its wearer once a second -- on the first
 * of these that wants it: twenty to lift withering, ten to lift poison, five to lift the flux flu; a lifegiver's five
 * to mend a point of health; a sustainer's one to give back breath, or else to feed. It is charged as a wand is, and
 * draws earth and water for it, not while either is to be spared.
 */
public class VerdantHeartItem extends BaubleItem {
    public static final int HOLDS = 200;
    public static final int PLAIN = 0;
    public static final int LIFEGIVER = 1;
    public static final int SUSTAINER = 2;

    public VerdantHeartItem(Properties properties) {
        super(properties.durability(HOLDS));
    }

    public static int kind(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.VERDANT_KIND.get(), PLAIN);
    }

    public static ItemStack of(ItemStack ring, int kind) {
        ring.set(ModDataComponents.VERDANT_KIND.get(), kind);
        return ring;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return false;
    }

    /** Spends this much if it has it. */
    private static boolean spend(ItemStack stack, int cost) {
        if (stack.getDamageValue() + cost > HOLDS) {
            return false;
        }
        stack.setDamageValue(stack.getDamageValue() + cost);
        return true;
    }

    @Override
    public void curioTick(SlotContext slot, ItemStack stack) {
        LivingEntity wearer = slot.entity();
        if (wearer.level().isClientSide || wearer.tickCount % 20 != 0) {
            return;
        }
        if (wearer.hasEffect(MobEffects.WITHER) && spend(stack, 20)) {
            wearer.removeEffect(MobEffects.WITHER);
            return;
        }
        if (wearer.hasEffect(MobEffects.POISON) && spend(stack, 10)) {
            wearer.removeEffect(MobEffects.POISON);
            return;
        }
        if (wearer.hasEffect(ModEffects.TAINT_POISON) && spend(stack, 5)) {
            wearer.removeEffect(ModEffects.TAINT_POISON);
            return;
        }
        int kind = kind(stack);
        if (kind == LIFEGIVER && wearer.getHealth() < wearer.getMaxHealth() && spend(stack, 5)) {
            wearer.heal(1.0F);
            return;
        }
        if (kind == SUSTAINER) {
            if (wearer.getAirSupply() < 100 && spend(stack, 1)) {
                wearer.setAirSupply(300);
                return;
            }
            if (wearer instanceof Player player && player.canEat(false) && spend(stack, 1)) {
                player.getFoodData().eat(1, 0.3F);
            }
        }
    }

    /** Draws earth and water out of the aura here for it, the mean of what came. */
    public static boolean recharge(ItemStack stack, ServerLevel level, BlockPos at, int amount) {
        if (AuraHandler.shouldSpare(level, at, ModAspects.EARTH) || AuraHandler.shouldSpare(level, at, ModAspects.WATER)) {
            return false;
        }
        amount = Math.min(amount, stack.getDamageValue());
        if (amount <= 0) {
            return false;
        }
        int drawn = Math.round((AuraHandler.drainAvailable(level, at, ModAspects.EARTH, amount)
                + AuraHandler.drainAvailable(level, at, ModAspects.WATER, amount)) / 2.0F);
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - drawn));
        return drawn > 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int kind = kind(stack);
        if (kind == LIFEGIVER) {
            tooltip.add(Component.translatable("item.alchemia.verdant_heart.lifegiver").withStyle(ChatFormatting.GOLD));
        } else if (kind == SUSTAINER) {
            tooltip.add(Component.translatable("item.alchemia.verdant_heart.sustainer").withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.translatable("item.alchemia.vis_charge",
                Math.round(100.0F * (HOLDS - stack.getDamageValue()) / HOLDS)).withStyle(ChatFormatting.YELLOW));
    }
}
