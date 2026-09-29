package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * What a pair of traveller's boots does every tick to whoever is walking in them.
 * <p>
 * Three things, and each of them answers to what the wearer is doing rather than merely to the boots being on,
 * which is why none of them is an attribute. The push comes only when walking forward and only on the ground;
 * the hold in the air comes only when there is no ground; and the softened landing comes only while falling.
 * <p>
 * The original hung these on a hook that ran for each piece of armour worn. This version of the game has no
 * such hook for players, so the wearer is asked once a tick instead and the boots are looked for by hand.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class TravellerBootsEvents {
    /** How hard a step pushes along the ground, and how much of that is left when wading. */
    private static final float PUSHES = 0.055F;
    private static final float WADING = 4.0F;
    /** What the wearer steers with in the air. A falling player normally has a fifth of this. */
    private static final float IN_AIR = 0.05F;
    /** How much of a fall is forgotten each tick of it. */
    private static final float FALLS_LESS = 0.25F;

    private TravellerBootsEvents() {
    }

    @SubscribeEvent
    public static void onWalking(PlayerTickEvent.Post event) {
        walked(event.getEntity());
    }

    /** One tick of walking in them, apart from the event so that it can be put to a player directly. */
    public static void walked(Player walker) {
        if (!walker.getItemBySlot(EquipmentSlot.FEET).is(ModItems.TRAVELLER_BOOTS.get())) {
            return;
        }

        // a fall that is being forgotten as it happens: a short drop stops hurting, a long one hurts less
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
