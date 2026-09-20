package me.moonscenty.alchemia.player.effect;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The ailments warp brings on. All of them are harmful except the ward, which is the one thing that holds warp off.
 */
public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Alchemia.MODID);

    /** Saps the vis a player can draw on. The drain itself lands in step 5, when there is an aura to drain from. */
    public static final DeferredHolder<MobEffect, MobEffect> FLUX_FLU = EFFECTS.register("flux_flu",
            () -> WarpEffect.inert(MobEffectCategory.HARMFUL, 0x8040A0));

    /** The flu, but it spreads to anyone standing too close. */
    public static final DeferredHolder<MobEffect, MobEffect> FLUX_PHAGE = EFFECTS.register("flux_phage",
            () -> new WarpEffect(MobEffectCategory.HARMFUL, 0xA040C0, 100, ModEffects::spread));

    /** Food stops filling the belly the way it should. */
    public static final DeferredHolder<MobEffect, MobEffect> UNNATURAL_HUNGER = EFFECTS.register("unnatural_hunger",
            () -> new WarpEffect(MobEffectCategory.HARMFUL, 0x6A3A1A, 1, (entity, amplifier) -> {
                if (entity instanceof Player player) {
                    player.causeFoodExhaustion(0.025F * (amplifier + 1));
                }
            }));

    /** Sunlight starts to burn. */
    public static final DeferredHolder<MobEffect, MobEffect> SUN_SCORNED = EFFECTS.register("sun_scorned",
            () -> new WarpEffect(MobEffectCategory.HARMFUL, 0xE0A020, 20, ModEffects::scorch));

    public static final DeferredHolder<MobEffect, MobEffect> BLURRED_VISION = EFFECTS.register("blurred_vision",
            () -> WarpEffect.inert(MobEffectCategory.HARMFUL, 0x505060));

    public static final DeferredHolder<MobEffect, MobEffect> DEADLY_GAZE = EFFECTS.register("deadly_gaze",
            () -> WarpEffect.inert(MobEffectCategory.HARMFUL, 0x901020));

    /** Flux seeps out of the sufferer and settles on the ground. Needs flux goo, which arrives in step 11. */
    public static final DeferredHolder<MobEffect, MobEffect> ALCHEDIARRHEA = EFFECTS.register("alchediarrhea",
            () -> WarpEffect.inert(MobEffectCategory.HARMFUL, 0x7030A0));

    /** While this holds, warp cannot bring anything on. */
    public static final DeferredHolder<MobEffect, MobEffect> WARP_WARD = EFFECTS.register("warp_ward",
            () -> WarpEffect.inert(MobEffectCategory.BENEFICIAL, 0xFFD700));

    /** Everything that warp can inflict, for the debug command and for tests. */
    public static List<DeferredHolder<MobEffect, MobEffect>> ailments() {
        return List.of(FLUX_FLU, FLUX_PHAGE, UNNATURAL_HUNGER, SUN_SCORNED, BLURRED_VISION, DEADLY_GAZE, ALCHEDIARRHEA);
    }

    private static void spread(LivingEntity carrier, int amplifier) {
        if (carrier.getRandom().nextInt(5) != 0) {
            return;
        }
        for (Player nearby : carrier.level().getEntitiesOfClass(Player.class, carrier.getBoundingBox().inflate(4))) {
            if (nearby != carrier && !nearby.hasEffect(FLUX_PHAGE)) {
                // it passes on weaker than it was caught
                Holder<MobEffect> passed = amplifier > 0 ? FLUX_PHAGE : FLUX_FLU;
                nearby.addEffect(new MobEffectInstance(passed, 6000, Math.max(0, amplifier - 1)));
            }
        }
    }

    private static void scorch(LivingEntity entity, int amplifier) {
        float brightness = entity.getLightLevelDependentMagicValue();
        if (brightness > 0.5F
                && entity.getRandom().nextFloat() * 30F < (brightness - 0.4F) * 2F
                && entity.level().canSeeSky(entity.blockPosition())) {
            entity.igniteForSeconds(4);
        }
    }
}
