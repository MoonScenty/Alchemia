package me.moonscenty.alchemia.aspect;

import java.util.HashMap;
import java.util.Map;

import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

// Aspects earned from the item's kind and enchantments; merged by max, not added.
public final class AspectBonuses {
    private static final Map<ResourceKey<Enchantment>, Holder<Aspect>> FROM_ENCHANTMENT = new HashMap<>();

    static {
        FROM_ENCHANTMENT.put(Enchantments.AQUA_AFFINITY, ModAspects.WATER);
        FROM_ENCHANTMENT.put(Enchantments.DEPTH_STRIDER, ModAspects.WATER);
        FROM_ENCHANTMENT.put(Enchantments.BANE_OF_ARTHROPODS, ModAspects.BEAST);
        FROM_ENCHANTMENT.put(Enchantments.LURE, ModAspects.BEAST);
        FROM_ENCHANTMENT.put(Enchantments.PROTECTION, ModAspects.PROTECT);
        FROM_ENCHANTMENT.put(Enchantments.BLAST_PROTECTION, ModAspects.PROTECT);
        FROM_ENCHANTMENT.put(Enchantments.FIRE_PROTECTION, ModAspects.PROTECT);
        FROM_ENCHANTMENT.put(Enchantments.PROJECTILE_PROTECTION, ModAspects.PROTECT);
        FROM_ENCHANTMENT.put(Enchantments.EFFICIENCY, ModAspects.TOOL);
        FROM_ENCHANTMENT.put(Enchantments.MENDING, ModAspects.TOOL);
        FROM_ENCHANTMENT.put(Enchantments.FEATHER_FALLING, ModAspects.FLIGHT);
        FROM_ENCHANTMENT.put(Enchantments.FIRE_ASPECT, ModAspects.FIRE);
        FROM_ENCHANTMENT.put(Enchantments.FLAME, ModAspects.FIRE);
        FROM_ENCHANTMENT.put(Enchantments.FORTUNE, ModAspects.DESIRE);
        FROM_ENCHANTMENT.put(Enchantments.LOOTING, ModAspects.DESIRE);
        FROM_ENCHANTMENT.put(Enchantments.LUCK_OF_THE_SEA, ModAspects.DESIRE);
        FROM_ENCHANTMENT.put(Enchantments.INFINITY, ModAspects.CRAFT);
        FROM_ENCHANTMENT.put(Enchantments.KNOCKBACK, ModAspects.AIR);
        FROM_ENCHANTMENT.put(Enchantments.PUNCH, ModAspects.AIR);
        FROM_ENCHANTMENT.put(Enchantments.RESPIRATION, ModAspects.AIR);
        FROM_ENCHANTMENT.put(Enchantments.POWER, ModAspects.AVERSION);
        FROM_ENCHANTMENT.put(Enchantments.SHARPNESS, ModAspects.AVERSION);
        FROM_ENCHANTMENT.put(Enchantments.THORNS, ModAspects.AVERSION);
        FROM_ENCHANTMENT.put(Enchantments.SILK_TOUCH, ModAspects.EXCHANGE);
        FROM_ENCHANTMENT.put(Enchantments.SMITE, ModAspects.ENTROPY);
        FROM_ENCHANTMENT.put(Enchantments.UNBREAKING, ModAspects.EARTH);
        FROM_ENCHANTMENT.put(Enchantments.SOUL_SPEED, ModAspects.SOUL);
        FROM_ENCHANTMENT.put(Enchantments.SWIFT_SNEAK, ModAspects.MOTION);
        FROM_ENCHANTMENT.put(Enchantments.RIPTIDE, ModAspects.MOTION);
        FROM_ENCHANTMENT.put(Enchantments.CHANNELING, ModAspects.ENERGY);
        FROM_ENCHANTMENT.put(Enchantments.MULTISHOT, ModAspects.CRAFT);
        FROM_ENCHANTMENT.put(Enchantments.QUICK_CHARGE, ModAspects.MOTION);
        FROM_ENCHANTMENT.put(Enchantments.PIERCING, ModAspects.AVERSION);
        FROM_ENCHANTMENT.put(Enchantments.IMPALING, ModAspects.AVERSION);
        FROM_ENCHANTMENT.put(Enchantments.LOYALTY, ModAspects.MOTION);
        FROM_ENCHANTMENT.put(Enchantments.BREACH, ModAspects.AVERSION);
        FROM_ENCHANTMENT.put(Enchantments.DENSITY, ModAspects.AVERSION);
        FROM_ENCHANTMENT.put(Enchantments.WIND_BURST, ModAspects.AIR);
        FROM_ENCHANTMENT.put(me.moonscenty.alchemia.enchantment.ModEnchantments.HASTE, ModAspects.MOTION);
        FROM_ENCHANTMENT.put(me.moonscenty.alchemia.enchantment.ModEnchantments.REPAIR, ModAspects.TOOL);
    }

    // original's potion table, worth three a level
    private static final Map<Holder<MobEffect>, Holder<Aspect>> FROM_EFFECT = Map.ofEntries(
            Map.entry(MobEffects.BLINDNESS, ModAspects.DARKNESS),
            Map.entry(MobEffects.CONFUSION, ModAspects.ELDRITCH),
            Map.entry(MobEffects.DAMAGE_BOOST, ModAspects.AVERSION),
            Map.entry(MobEffects.DIG_SLOWDOWN, ModAspects.TRAP),
            Map.entry(MobEffects.DIG_SPEED, ModAspects.TOOL),
            Map.entry(MobEffects.HARM, ModAspects.DEATH),
            Map.entry(MobEffects.HEAL, ModAspects.LIFE),
            Map.entry(MobEffects.HUNGER, ModAspects.DEATH),
            Map.entry(MobEffects.INVISIBILITY, ModAspects.SENSES),
            Map.entry(MobEffects.JUMP, ModAspects.FLIGHT),
            Map.entry(MobEffects.MOVEMENT_SLOWDOWN, ModAspects.TRAP),
            Map.entry(MobEffects.MOVEMENT_SPEED, ModAspects.MOTION),
            Map.entry(MobEffects.NIGHT_VISION, ModAspects.SENSES),
            Map.entry(MobEffects.POISON, ModAspects.DEATH),
            Map.entry(MobEffects.REGENERATION, ModAspects.LIFE),
            Map.entry(MobEffects.DAMAGE_RESISTANCE, ModAspects.PROTECT),
            Map.entry(MobEffects.WATER_BREATHING, ModAspects.AIR),
            Map.entry(MobEffects.WEAKNESS, ModAspects.DEATH));

    private AspectBonuses() {
    }

    public static AspectList ofItem(Item item, AspectList base) {
        if (item instanceof ArmorItem armor) {
            return base.mergeMax(ModAspects.PROTECT, armor.getDefense());
        }
        if (item instanceof SwordItem sword) {
            return base.mergeMax(ModAspects.AVERSION, (int) (sword.getTier().getAttackDamageBonus() + 1));
        }
        if (item instanceof BowItem || item instanceof CrossbowItem) {
            return base.mergeMax(ModAspects.AVERSION, 3).mergeMax(ModAspects.FLIGHT, 1);
        }
        if (item instanceof DiggerItem digger) {
            return base.mergeMax(ModAspects.TOOL, harvestRank(digger.getTier()) + 1);
        }
        if (item instanceof HoeItem || item instanceof ShearsItem) {
            return base.mergeMax(ModAspects.TOOL, byDurability(new ItemStack(item).getMaxDamage()));
        }
        return base;
    }

    // original's: shears and hoes are ranked by how long they last
    private static int byDurability(int lasts) {
        if (lasts <= Tiers.WOOD.getUses()) {
            return 1;
        }
        if (lasts <= Tiers.STONE.getUses()) {
            return 2;
        }
        return lasts <= Tiers.IRON.getUses() ? 3 : 4;
    }

    private static AspectList ofWand(ItemStack stack, AspectList base) {
        if (!(stack.getItem() instanceof me.moonscenty.alchemia.item.WandItem)) {
            return base;
        }
        int cost = me.moonscenty.alchemia.item.WandItem.rod(stack).craftCost()
                + me.moonscenty.alchemia.item.WandItem.cap(stack).craftCost();
        return base.mergeMax(ModAspects.ENERGY, cost / 2).mergeMax(ModAspects.TOOL, cost / 3);
    }

    private static AspectList ofPotion(ItemStack stack, AspectList base) {
        if (!(stack.getItem() instanceof PotionItem)) {
            return base;
        }
        AspectList result = base.mergeMax(ModAspects.WATER, 1).mergeMax(ModAspects.CRYSTAL, 1);
        if (stack.getItem() instanceof SplashPotionItem || stack.getItem() instanceof LingeringPotionItem) {
            result = result.mergeMax(ModAspects.ENTROPY, 2);
        }
        for (MobEffectInstance effect : stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                .getAllEffects()) {
            int level = effect.getAmplifier() + 1;
            result = result.mergeMax(ModAspects.ENERGY, level * 2);
            Holder<Aspect> aspect = FROM_EFFECT.get(effect.getEffect());
            if (aspect != null) {
                result = result.mergeMax(aspect, level * 3);
            } else if (effect.getEffect().equals(MobEffects.FIRE_RESISTANCE)) {
                result = result.mergeMax(ModAspects.PROTECT, level).mergeMax(ModAspects.FIRE, level * 2);
            } else if (effect.getEffect().equals(MobEffects.ABSORPTION)) {
                result = result.mergeMax(ModAspects.PROTECT, level).mergeMax(ModAspects.LIFE, level * 2);
            }
        }
        return result;
    }

    public static AspectList ofStack(ItemStack stack, AspectList base) {
        AspectList result = ofPotion(stack, ofWand(stack, base));
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (enchantments.isEmpty()) {
            return result;
        }

        int levels = 0;
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            int level = enchantments.getLevel(enchantment);
            levels += level;
            Holder<Aspect> aspect = enchantment.unwrapKey().map(FROM_ENCHANTMENT::get).orElse(null);
            if (aspect != null) {
                result = result.mergeMax(aspect, level);
            }
        }

        return levels > 0 ? result.mergeMax(ModAspects.ENERGY, levels) : result;
    }

    // original's scale: wood/gold 0 .. diamond 3, alchemium with diamond; anything stronger is 4
    private static int harvestRank(Tier tier) {
        if (tier == Tiers.WOOD || tier == Tiers.GOLD) {
            return 0;
        }
        if (tier == Tiers.STONE) {
            return 1;
        }
        if (tier == Tiers.IRON) {
            return 2;
        }
        if (tier == Tiers.DIAMOND || tier == me.moonscenty.alchemia.item.ModTiers.ALCHEMIUM
                || tier == me.moonscenty.alchemia.item.ModTiers.ELEMENTAL) {
            return 3;
        }
        return 4;
    }
}
