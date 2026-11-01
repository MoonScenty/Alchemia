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

@EventBusSubscriber(modid = Alchemia.MODID)
public final class FortressMasks {
    // original's value
    public static final int WITHERS_FOR = 80;

    private FortressMasks() {
    }

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
        // direct hits only, as in the original
        if (event.getSource().getDirectEntity() instanceof Player leecher && wears(leecher, HelmFitting.SIPPING_FIEND)
                && leecher.getRandom().nextFloat() < amount / 12.0F) {
            leecher.heal(1.0F);
        }
        if (wears(hurt, HelmFitting.ANGRY_GHOST) && event.getSource().getDirectEntity() instanceof LivingEntity attacker
                && hurt.getRandom().nextFloat() < amount / 10.0F) {
            attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHERS_FOR));
        }
    }

    public static int easing(Player player) {
        return wears(player, HelmFitting.GRINNING_DEVIL) ? 2 + player.getRandom().nextInt(4) : 0;
    }
}
