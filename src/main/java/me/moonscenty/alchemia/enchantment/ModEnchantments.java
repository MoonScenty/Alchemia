package me.moonscenty.alchemia.enchantment;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The original's own vanilla-style enchantments, as against the infusion enchantments worked on the altar.
 * <p>
 * Haste, the original's {@code EnchantmentHaste}: on boots of any kind, or on the thaumostatic harness, three levels,
 * a little more speed on the ground for each; half as much in the air or in water. On the harness it takes some of
 * the drag off flying instead. It comes from the enchanting table and enchanted books, as the original put it in
 * the book list.
 * <p>
 * Repair, the original's {@code EnchantmentRepair}: on the original's own gear (its tools, weapons and armour), two
 * levels, not with unbreaking. What it does is {@link Repair}.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class ModEnchantments {
    public static final ResourceKey<Enchantment> HASTE =
            ResourceKey.create(Registries.ENCHANTMENT, Alchemia.id("haste"));
    public static final ResourceKey<Enchantment> REPAIR =
            ResourceKey.create(Registries.ENCHANTMENT, Alchemia.id("repair"));
    /** Repair's levels, the most it mends at once. */
    public static final int REPAIR_LEVELS = 2;
    /** How much speed each level of haste adds, pushing forward each tick. */
    public static final float HASTE_PER_LEVEL = 0.015F;

    private ModEnchantments() {
    }

    /**
     * Haste: weight three, three levels, the original's costs: 15 + 9 a level, and 50 over the base of 1 + 10 a level.
     * Repair: weight two, two levels, 20 + 10 a level and the same ceiling, never with unbreaking.
     */
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        var items = context.lookup(Registries.ITEM);
        context.register(HASTE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ModTags.Items.HASTE_ENCHANTABLE), 3, 3,
                        Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(61, 10), 4,
                        EquipmentSlotGroup.FEET, EquipmentSlotGroup.CHEST))
                .build(HASTE.location()));
        context.register(REPAIR, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ModTags.Items.REPAIR_ENCHANTABLE), 2, REPAIR_LEVELS,
                        Enchantment.dynamicCost(20, 10), Enchantment.dynamicCost(61, 10), 4, EquipmentSlotGroup.ANY))
                .exclusiveWith(net.minecraft.core.HolderSet.direct(context.lookup(Registries.ENCHANTMENT)
                        .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING)))
                .build(REPAIR.location()));
    }

    /** What level of haste a thing has, or none. */
    public static int haste(Player player, ItemStack stack) {
        return level(player.level(), HASTE, stack);
    }

    /** What level of one of these a thing has, or none. */
    public static int level(net.minecraft.world.level.Level level, ResourceKey<Enchantment> which, ItemStack stack) {
        return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(which)
                .map(found -> EnchantmentHelper.getItemEnchantmentLevel((Holder<Enchantment>) found, stack))
                .orElse(0);
    }

    /**
     * Haste on the boots, walking or running forward, not flying: a push ahead each tick, halved off the ground and
     * halved again in water. Movement is the client's to work out, so this is done there.
     */
    @SubscribeEvent
    public static void hurry(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide || !player.isLocalPlayer() || player.getAbilities().flying
                || player.zza <= 0.0F) {
            return;
        }
        int level = haste(player, player.getItemBySlot(EquipmentSlot.FEET));
        if (level <= 0) {
            return;
        }
        float bonus = level * HASTE_PER_LEVEL;
        if (!player.onGround()) {
            bonus /= 2.0F;
        }
        if (player.isInWater()) {
            bonus /= 2.0F;
        }
        player.moveRelative(bonus, new Vec3(0.0, 0.0, 1.0));
    }
}
