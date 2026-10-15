package me.moonscenty.alchemia.wand.spell;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
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
 * The original's number. What is worked into the focus can make the bolt harder, make it jump on to whatever is
 * standing beside what it struck, or turn it into a ball lobbed at the floor instead.
 */
public final class Shock {
    /** What a bolt does to whatever it lands on, and what it does once it has learned to jump. */
    private static final float HURTS = 4.0F;
    private static final float HURTS_CHAINED = 6.0F;
    /** How far a jump carries, and how many jumps a rank of chaining or of widening is worth. */
    private static final double JUMPS = 8.0;
    private static final int JUMPS_A_RANK = 2;

    /** What a ball lobbed at the floor reaches and does, and what a rank is worth to each. */
    private static final int BALL_REACHES = 4;
    private static final int WIDER = 2;
    private static final float BALL_HURTS = 5.0F;
    private static final float BALL_HARDER = 1.33F;

    /** How many sparks are thrown off where it lands, and how far they scatter. */
    private static final int SPARKS = 5;
    private static final double SCATTERS = 0.6;

    private Shock() {
    }

    public static boolean cast(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor) {
        int potency = Casting.worked(wand, FocusUpgrade.POTENCY);
        int wider = Casting.worked(wand, FocusUpgrade.ENLARGE);
        if (Casting.has(wand, FocusUpgrade.EARTH_SHOCK)) {
            return lob(level, caster, BALL_REACHES + wider * WIDER, BALL_HURTS + potency * BALL_HARDER);
        }

        if (!(aim instanceof EntityHitResult struck) || !(struck.getEntity() instanceof LivingEntity hit)) {
            return false;
        }
        int chained = Casting.worked(wand, FocusUpgrade.CHAIN_LIGHTNING);
        hit.hurt(level.damageSources().indirectMagic(caster, caster),
                (chained > 0 ? HURTS_CHAINED : HURTS) + potency);
        sparks(level, hit);
        // heard at the wand that let it go, as the original's was
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                ModSounds.SHOCK.get(), SoundSource.PLAYERS, 0.25F, 1.0F);
        if (chained > 0) {
            jumpOn(level, caster, hit, (chained + wider) * JUMPS_A_RANK, HURTS + potency);
        }
        return true;
    }

    /**
     * The bolt carrying on from what it struck to whatever is standing nearest it.
     * <p>
     * Each jump starts from where the last one landed, so a line of things is walked along rather than a circle
     * round the first one being filled in. Nothing is hit twice, and whoever cast it is never hit at all.
     */
    private static void jumpOn(ServerLevel level, LivingEntity caster, LivingEntity from, int jumps, float hurts) {
        List<Entity> already = new ArrayList<>(List.of(from, caster));
        LivingEntity here = from;
        for (int jump = 0; jump < jumps; jump++) {
            LivingEntity next = nearest(level, here, already);
            if (next == null) {
                return;
            }
            arc(level, here, next);
            next.hurt(level.damageSources().indirectMagic(caster, caster), hurts);
            sparks(level, next);
            already.add(next);
            here = next;
        }
    }

    /** The nearest living thing to this one that has not been hit yet. */
    private static LivingEntity nearest(ServerLevel level, LivingEntity from, List<Entity> already) {
        LivingEntity found = null;
        double closest = Double.MAX_VALUE;
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(from.position(), from.position()).inflate(JUMPS))) {
            if (already.contains(other) || !other.isAlive()) {
                continue;
            }
            double away = other.distanceToSqr(from);
            if (away < closest) {
                closest = away;
                found = other;
            }
        }
        return found;
    }

    /** The jump itself, as a line of sparks from the one to the other. */
    private static void arc(ServerLevel level, Entity from, Entity to) {
        Vec3 start = from.position().add(0.0, from.getBbHeight() / 2.0, 0.0);
        Vec3 along = to.position().add(0.0, to.getBbHeight() / 2.0, 0.0).subtract(start);
        int steps = Math.max(1, (int) (along.length() * 4));
        for (int step = 0; step <= steps; step++) {
            Vec3 at = start.add(along.scale((double) step / steps));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** A ball of the same thing, thrown at the floor rather than sent at a face. */
    private static boolean lob(ServerLevel level, LivingEntity caster, int reaches, float hurts) {
        ShockOrb orb = new ShockOrb(level, caster).striking(reaches, hurts);
        orb.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, 1.2F, 0.0F);
        level.addFreshEntity(orb);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                ModSounds.ZAP.get(), SoundSource.PLAYERS, 1.0F,
                1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
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
