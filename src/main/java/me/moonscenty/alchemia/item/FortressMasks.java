package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * What the masks worked into a fortress helm do, the original's {@code EntityEvents.entityHurt} and
 * {@code WarpEvents.checkWarpEvent}. Only a player's helm counts, and only on the head.
 * <ul>
 * <li>The sipping fiend drinks from what its wearer strikes by hand: a point of health back, a chance in twelve for
 * every point of the blow.</li>
 * <li>The angry ghost answers whatever strikes its wearer by hand: four seconds of wither on it, a chance in ten for
 * every point of the blow.</li>
 * <li>The grinning devil takes two to five off what a warp event would have been; see {@link #easing}.</li>
 * </ul>
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class FortressMasks {
    /** How long the ghost's wither lasts, in ticks. The original's. */
    public static final int WITHERS_FOR = 80;

    private FortressMasks() {
    }

    /** Whether a player is wearing a fortress helm with this mask on it. */
    public static boolean wears(LivingEntity wearer, HelmFitting mask) {
        return wearer instanceof Player
                && FortressArmorItem.fitting(wearer.getItemBySlot(EquipmentSlot.HEAD)).filter(mask::equals).isPresent();
    }

    @SubscribeEvent
    public static void hurt(LivingIncomingDamageEvent event) {
        LivingEntity hurt = event.getEntity();
        if (hurt.level().isClientSide) {
            return;
        }
        float amount = event.getAmount();
        // the blow has to come straight from the hand: the original looked at what struck, not who sent it
        if (event.getSource().getDirectEntity() instanceof Player leecher && wears(leecher, HelmFitting.SIPPING_FIEND)
                && leecher.getRandom().nextFloat() < amount / 12.0F) {
            leecher.heal(1.0F);
        }
        if (wears(hurt, HelmFitting.ANGRY_GHOST) && event.getSource().getDirectEntity() instanceof LivingEntity attacker
                && hurt.getRandom().nextFloat() < amount / 10.0F) {
            attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHERS_FOR));
        }
    }

    /** What the grinning devil takes off a warp event's roll for whoever wears it: two to five, or nothing. */
    public static int easing(Player player) {
        return wears(player, HelmFitting.GRINNING_DEVIL) ? 2 + player.getRandom().nextInt(4) : 0;
    }
}
