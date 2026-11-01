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

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class ModEnchantments {
    public static final ResourceKey<Enchantment> HASTE =
            ResourceKey.create(Registries.ENCHANTMENT, Alchemia.id("haste"));
    public static final ResourceKey<Enchantment> REPAIR =
            ResourceKey.create(Registries.ENCHANTMENT, Alchemia.id("repair"));
    public static final int REPAIR_LEVELS = 2;
    public static final float HASTE_PER_LEVEL = 0.015F;

    private ModEnchantments() {
    }

    // weights, levels and costs are the original's
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

    public static int haste(Player player, ItemStack stack) {
        return level(player.level(), HASTE, stack);
    }

    public static int level(net.minecraft.world.level.Level level, ResourceKey<Enchantment> which, ItemStack stack) {
        return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(which)
                .map(found -> EnchantmentHelper.getItemEnchantmentLevel((Holder<Enchantment>) found, stack))
                .orElse(0);
    }

    // movement is computed on the client, so haste is applied there
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
