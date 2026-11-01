package me.moonscenty.alchemia.aspect;

import java.util.HashMap;
import java.util.Map;

import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
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
        if (item instanceof HoeItem hoe) {
            return base.mergeMax(ModAspects.TOOL, harvestRank(hoe.getTier()) + 1);
        }
        if (item instanceof ShearsItem) {
            return base.mergeMax(ModAspects.TOOL, 2);
        }
        return base;
    }

    public static AspectList ofStack(ItemStack stack, AspectList base) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (enchantments.isEmpty()) {
            return base;
        }

        AspectList result = base;
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

    // original's scale: wood/gold 0 .. diamond 3; anything stronger, modded tiers included, is 4
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
        if (tier == Tiers.DIAMOND) {
            return 3;
        }
        return 4;
    }
}
