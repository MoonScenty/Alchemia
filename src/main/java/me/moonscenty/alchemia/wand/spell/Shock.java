package me.moonscenty.alchemia.wand.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A bolt straight down the line of sight to whatever living thing is standing in it.
 *
 * <p>It does not travel: there is nothing to dodge and nothing to shoot down. Whatever the wand was pointed at
 * when the button went down is what gets hit, which is what makes this the plainest of the twelve and the one to
 * build the rest on.
 *
 * <p>Four damage, counted as a working rather than a blow, so armour answers it the way armour answers magic.
 * The original's number.
 */
public final class Shock {
    /** What a bolt does to whatever it lands on. */
    private static final float HURTS = 4.0F;

    /** How many sparks are thrown off where it lands, and how far they scatter. */
    private static final int SPARKS = 5;
    private static final double SCATTERS = 0.6;

    private Shock() {
    }

    public static boolean cast(ServerLevel level, Player caster, ItemStack wand, HitResult aim, int heldFor) {
        if (!(aim instanceof EntityHitResult struck) || !(struck.getEntity() instanceof LivingEntity hit)) {
            return false;
        }
        hit.hurt(level.damageSources().indirectMagic(caster, caster), HURTS);
        sparks(level, hit);
        level.playSound(null, hit.getX(), hit.getY(), hit.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.25F, 1.6F);
        return true;
    }

    /**
     * The flash where it lands.
     * <p>
     * The original drew an arc all the way from the wand to the target and a shower where it struck. The arc was
     * a particle of its own, which this mod has none of yet; the shower is the half that says a bolt landed, so
     * it is the half that is here.
     */
    private static void sparks(ServerLevel level, Entity hit) {
        Vec3 middle = hit.position().add(0.0, hit.getBbHeight() / 2.0, 0.0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, middle.x, middle.y, middle.z,
                SPARKS, SCATTERS, SCATTERS, SCATTERS, 0.0);
    }
}
