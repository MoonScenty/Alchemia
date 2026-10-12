package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Every other kind of zombie has a brain as well, only less often: one time in ten when a player killed it, and one
 * more in ten for each level of looting. Husks, drowned, zombie villagers and zombified piglins are all zombies to
 * the game, as pigmen were to the original. An angry zombie's brain is in its own loot table.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class ZombieBrains {
    private ZombieBrains() {
    }

    @SubscribeEvent
    public static void died(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie) || zombie instanceof AngryZombie
                || zombie.level().isClientSide || !event.isRecentlyHit()) {
            return;
        }
        int looting = 0;
        if (event.getSource().getEntity() instanceof LivingEntity killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(zombie.level().registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING), killer);
        }
        if (zombie.getRandom().nextInt(10) - looting < 1) {
            event.getDrops().add(new ItemEntity(zombie.level(), zombie.getX(), zombie.getEyeY(), zombie.getZ(),
                    new ItemStack(ModItems.ZOMBIE_BRAIN.get())));
        }
    }
}
