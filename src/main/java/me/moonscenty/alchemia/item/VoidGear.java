package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// void tools and plate are vanilla item classes, so their effects are done from events
@EventBusSubscriber(modid = Alchemia.MODID)
public final class VoidGear {
    public static final int MENDS_EVERY = 20;
    public static final int WEAKENS_FOR = 60;
    // original's value
    public static final int TOOLS_WEAKEN_FOR = 80;

    private VoidGear() {
    }

    public static int warp(ItemStack stack) {
        if (stack.getItem() instanceof RobeItem robe) {
            return robe.drab() ? 2 : 0;
        }
        return isVoid(stack) ? 1 : 0;
    }

    public static boolean isVoid(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof RobeItem robe) {
            return robe.drab();
        }
        String name = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return name.startsWith("void_") && (ModItems.METAL_TOOLS.containsKey(name)
                || ModItems.METAL_ARMOUR.containsKey(name));
    }

    @SubscribeEvent
    public static void mend(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.tickCount % MENDS_EVERY == 0) {
            mendCarried(player);
        }
    }

    public static void mendCarried(Player player) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isDamaged() && isVoid(stack)) {
                stack.setDamageValue(stack.getDamageValue() - 1);
            }
        }
    }

    @SubscribeEvent
    public static void weaken(LivingDamageEvent.Post event) {
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity attacker)
                || !attacker.getMainHandItem().is(ModItems.METAL_TOOLS.get("void_sword").get())) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || (target instanceof Player struck && attacker instanceof Player hitter
                && !hitter.canHarmPlayer(struck))) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKENS_FOR), attacker);
    }

    // applied on the swing itself, not the hit, as in the original
    @SubscribeEvent
    public static void swung(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        swungAt(event.getEntity(), event.getTarget());
    }

    public static void swungAt(Player swinger, net.minecraft.world.entity.Entity target) {
        if (swinger.level().isClientSide || !(target instanceof LivingEntity living)) {
            return;
        }
        ItemStack held = swinger.getMainHandItem();
        boolean tool = java.util.stream.Stream.of("void_pickaxe", "void_axe", "void_shovel", "void_hoe")
                .anyMatch(name -> held.is(ModItems.METAL_TOOLS.get(name).get()));
        if (!tool) {
            return;
        }
        if (living instanceof Player && (swinger.getServer() == null || !swinger.getServer().isPvpAllowed())) {
            return;
        }
        living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, TOOLS_WEAKEN_FOR), swinger);
    }
}
