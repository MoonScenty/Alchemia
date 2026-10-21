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

/**
 * What void metal does that other metal does not, as the original's {@code ItemVoid*} classes did it: every tool,
 * every piece of armour and every void robe mends itself a point a second wherever it is carried, and each carries
 * warp while it is held or worn, a point for a tool or a plate, two for a piece of robe. The sword leaves whatever it
 * cuts weak for three seconds.
 * <p>
 * The tools and the plate are the game's own kinds of item, so this is done from events rather than in the items.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class VoidGear {
    /** How often a piece mends a point, in ticks. */
    public static final int MENDS_EVERY = 20;
    /** How long the sword's weakness lasts. */
    public static final int WEAKENS_FOR = 60;
    /** How long the void pickaxe, axe, shovel and hoe weaken what they are swung at, in ticks. The original's. */
    public static final int TOOLS_WEAKEN_FOR = 80;

    private VoidGear() {
    }

    /** The warp a piece of void gear carries, or 0 for anything else. */
    public static int warp(ItemStack stack) {
        if (stack.getItem() instanceof RobeItem robe) {
            return robe.drab() ? 2 : 0;
        }
        return isVoid(stack) ? 1 : 0;
    }

    /** Whether this is a void tool, a piece of void plate or a piece of void robe. */
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

    /** Everything void the player carries mends a point a second, wherever it is. */
    @SubscribeEvent
    public static void mend(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.tickCount % MENDS_EVERY == 0) {
            mendCarried(player);
        }
    }

    /** A point back on every damaged piece of void gear the player has about them. */
    public static void mendCarried(Player player) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isDamaged() && isVoid(stack)) {
                stack.setDamageValue(stack.getDamageValue() - 1);
            }
        }
    }

    /** The void sword leaves what it cuts weak, though not another player where they may not be hurt. */
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

    /**
     * The void pickaxe, axe, shovel and hoe weaken whatever living thing they are swung at, as the original's did on
     * the swing itself rather than on the blow: four seconds of it, and another player only where the server lets
     * players hurt each other.
     */
    @SubscribeEvent
    public static void swung(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        swungAt(event.getEntity(), event.getTarget());
    }

    /** A swing at something, apart from the event so that it can be put to a player directly. */
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
