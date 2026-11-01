package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// polled per player tick: there is no per-armour-piece tick hook for players
@EventBusSubscriber(modid = Alchemia.MODID)
public final class TravellerBootsEvents {
    // ground push, and the share left when wading
    private static final float PUSHES = 0.055F;
    private static final float WADING = 4.0F;
    // air control; a falling player normally has a fifth of this
    private static final float IN_AIR = 0.05F;
    // original's value
    public static final double JUMPS = 0.275;
    // share of fall distance forgotten per tick
    private static final float FALLS_LESS = 0.25F;

    private TravellerBootsEvents() {
    }

    @SubscribeEvent
    public static void onJump(net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        jumped(event.getEntity());
    }

    public static void jumped(net.minecraft.world.entity.LivingEntity jumper) {
        if (jumper instanceof Player && jumper.getItemBySlot(EquipmentSlot.FEET).is(ModItems.TRAVELLER_BOOTS.get())) {
            jumper.setDeltaMovement(jumper.getDeltaMovement().add(0.0, JUMPS, 0.0));
        }
    }

    @SubscribeEvent
    public static void onWalking(PlayerTickEvent.Post event) {
        walked(event.getEntity());
    }

    public static void walked(Player walker) {
        if (!walker.getItemBySlot(EquipmentSlot.FEET).is(ModItems.TRAVELLER_BOOTS.get())) {
            return;
        }

        if (walker.fallDistance > 0.0F) {
            walker.fallDistance = Math.max(0.0F, walker.fallDistance - FALLS_LESS);
        }

        if (walker.getAbilities().flying || walker.zza <= 0.0F) {
            return;
        }
        if (walker.onGround()) {
            walker.moveRelative(walker.isInWater() ? PUSHES / WADING : PUSHES, new Vec3(0.0, 0.0, 1.0));
        } else {
            walker.setSpeed(IN_AIR);
        }
    }
}
