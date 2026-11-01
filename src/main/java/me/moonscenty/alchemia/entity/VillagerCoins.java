package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

// any killer counts, not only a player (original's behaviour)
@EventBusSubscriber(modid = Alchemia.MODID)
public final class VillagerCoins {
    private VillagerCoins() {
    }

    @SubscribeEvent
    public static void died(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Villager villager) || villager.level().isClientSide) {
            return;
        }
        int looting = Looting.of(event.getSource());
        if (villager.getRandom().nextInt(10) - looting < 1) {
            event.getDrops().add(new ItemEntity(villager.level(), villager.getX(), villager.getEyeY(), villager.getZ(),
                    new ItemStack(ModItems.COIN.get())));
        }
    }
}
