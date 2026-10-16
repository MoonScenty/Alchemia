package me.moonscenty.alchemia.enchantment;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Repair, the original's {@code EnchantmentRepair} and {@code PlayerEvents.doRepair}: every two seconds a worn thing
 * with it, or one in the hotbar, mends a point a level, paid for out of the aura. What is paid is one primal of what the
 * thing is made of, or earth, picked at random, as much as the level; where that runs thin, or there is not enough, it
 * waits. Not for anyone in creative. An arcane bore mends its pickaxe the same way.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class Repair {
    /** How often it mends, in ticks. */
    public static final int EVERY = 40;

    private Repair() {
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.tickCount % EVERY != 0 || player.getAbilities().instabuild) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            mend(inventory.items.get(slot), player.level(), player.blockPosition(), player.getRandom());
        }
        for (ItemStack worn : inventory.armor) {
            mend(worn, player.level(), player.blockPosition(), player.getRandom());
        }
    }

    /** Mends this a little if it has repair on it and the aura here will pay; whether it did. */
    public static boolean mend(ItemStack stack, Level level, BlockPos pos, RandomSource random) {
        if (stack.isEmpty() || !stack.isDamaged()) {
            return false;
        }
        int points = Math.min(ModEnchantments.REPAIR_LEVELS, ModEnchantments.level(level, ModEnchantments.REPAIR, stack));
        if (points <= 0) {
            return false;
        }
        AspectList made = Aspects.of(stack);
        if (made.isEmpty()) {
            return false;
        }
        var choices = made.toPrimals().add(ModAspects.EARTH, 1).sortedByName();
        Holder<Aspect> paid = choices.get(random.nextInt(choices.size()));
        if (AuraHandler.shouldSpare(level, pos, paid) || !AuraHandler.drain(level, pos, paid, points)) {
            return false;
        }
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - points));
        return true;
    }
}
