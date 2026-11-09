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
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class Shock {
    // plain and chained damage; original's values
    private static final float HURTS = 4.0F;
    private static final float HURTS_CHAINED = 6.0F;
    // jump range, and jumps per rank of chain lightning or enlarge
    private static final double JUMPS = 8.0;
    private static final int JUMPS_A_RANK = 2;

    // earth shock: radius and damage, and what each rank adds
    private static final int BALL_REACHES = 4;
    private static final int WIDER = 2;
    private static final float BALL_HURTS = 5.0F;
    private static final float BALL_HARDER = 1.33F;

    private static final int SPARKS = 5;
    private static final double SCATTERS = 0.6;

    private Shock() {
    }

    public static boolean cast(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor) {
        int potency = Casting.worked(wand, FocusUpgrade.POTENCY);
        int wider = Casting.worked(wand, FocusUpgrade.ENLARGE);
        if (Casting.has(wand, FocusUpgrade.EARTH_SHOCK)) {
            return lob(level, caster, BALL_REACHES + wider * WIDER, (int) (BALL_HURTS + potency * BALL_HARDER));
        }

        // original: the bolt costs vis whether or not it finds anything
        if (!(aim instanceof EntityHitResult struck) || !(struck.getEntity() instanceof LivingEntity hit)
                || !mayHurt(level, hit)) {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    ModSounds.SHOCK.get(), SoundSource.PLAYERS, 0.25F, 1.0F);
            return true;
        }
        int chained = Casting.worked(wand, FocusUpgrade.CHAIN_LIGHTNING);
        hit.hurt(level.damageSources().indirectMagic(caster, caster),
                (chained > 0 ? HURTS_CHAINED : HURTS) + potency);
        sparks(level, hit);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                ModSounds.SHOCK.get(), SoundSource.PLAYERS, 0.25F, 1.0F);
        if (chained > 0) {
            jumpOn(level, caster, hit, (chained + wider) * JUMPS_A_RANK, HURTS + potency);
        }
        return true;
    }

    // each jump starts from the last target and never hits the same thing twice, or the caster
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

    private static LivingEntity nearest(ServerLevel level, LivingEntity from, List<Entity> already) {
        LivingEntity found = null;
        double closest = Double.MAX_VALUE;
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(from.position(), from.position()).inflate(JUMPS))) {
            if (already.contains(other) || !other.isAlive() || !mayHurt(level, other)
                    || other instanceof OwnableEntity pet && already.get(1).equals(pet.getOwner())) {
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

    private static boolean mayHurt(ServerLevel level, Entity target) {
        return !(target instanceof Player) || level.getServer().isPvpAllowed();
    }

    private static void arc(ServerLevel level, Entity from, Entity to) {
        Vec3 start = from.position().add(0.0, from.getBbHeight() / 2.0, 0.0);
        Vec3 along = to.position().add(0.0, to.getBbHeight() / 2.0, 0.0).subtract(start);
        int steps = Math.max(1, (int) (along.length() * 4));
        for (int step = 0; step <= steps; step++) {
            Vec3 at = start.add(along.scale((double) step / steps));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static boolean lob(ServerLevel level, LivingEntity caster, int reaches, float hurts) {
        ShockOrb orb = new ShockOrb(level, caster).striking(reaches, hurts);
        orb.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(orb);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                ModSounds.ZAP.get(), SoundSource.PLAYERS, 1.0F,
                1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
        return true;
    }

    // the original's wand-to-target arc needs a custom particle; only the impact shower is ported
    private static void sparks(ServerLevel level, Entity hit) {
        Vec3 middle = hit.position().add(0.0, hit.getBbHeight() / 2.0, 0.0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, middle.x, middle.y, middle.z,
                SPARKS, SCATTERS, SCATTERS, SCATTERS, 0.0);
    }
}
