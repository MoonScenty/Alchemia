package me.moonscenty.alchemia.wand.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The two foci that throw something and let it do the rest.
 *
 * <p>Neither aims. Whatever the wand was pointed at only settles which way the thing goes; what it lands on is
 * whatever happens to be in the way by the time it gets there, which is the whole difference between a thrown
 * thing and a bolt.
 */
public final class Thrown {
    /** How fast each leaves the hand, and how far off true. Both fly straight: these are not arrows. */
    private static final float FLIES = 1.5F;
    private static final float TRUE = 0.0F;

    /** How many embers leave the wand each tick, and how wide the spray is, in degrees. */
    private static final int MOTES = 2;
    private static final float SCATTERS = 15.0F;
    /** How often the roar of it is heard while the button is down, in ticks. */
    private static final int ROARS_EVERY = 10;

    private Thrown() {
    }

    /**
     * A spray of embers, two a tick for as long as the wand is held down.
     * <p>
     * They go out in a cone rather than a line, which is what makes this a doorway-filler rather than a sniper's
     * focus. Fifteen degrees of scatter, as the original had it.
     */
    public static boolean fire(ServerLevel level, Player caster, ItemStack wand, HitResult aim, int heldFor) {
        for (int mote = 0; mote < MOTES; mote++) {
            Ember ember = new Ember(level, caster);
            ember.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, FLIES, SCATTERS);
            level.addFreshEntity(ember);
        }
        if (heldFor % ROARS_EVERY == 0) {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.33F, 1.8F);
        }
        return true;
    }

    /** One spike of ice, aimed. */
    public static boolean frost(ServerLevel level, Player caster, ItemStack wand, HitResult aim, int heldFor) {
        return let(level, caster, new FrostShard(level, caster), 1.4F);
    }

    /**
     * A splinter that follows what the wand was pointed at.
     * <p>
     * It will not go off at all without a mark. That is the whole of what makes it cheap: a focus that cannot
     * miss is a focus that has to be given something to not miss.
     */
    public static boolean visShard(ServerLevel level, Player caster, ItemStack wand, HitResult aim, int heldFor) {
        if (!(aim instanceof EntityHitResult struck) || !(struck.getEntity() instanceof LivingEntity mark)) {
            return false;
        }
        return let(level, caster, new VisShard(level, caster, mark), 1.7F);
    }

    /** All six primals wound into a ball, and a hole where it lands. */
    public static boolean primal(ServerLevel level, Player caster, ItemStack wand, HitResult aim, int heldFor) {
        return let(level, caster, new PrimalOrb(level, caster), 0.8F);
    }

    /** A pech's curse, thrown at nothing in particular. */
    public static boolean pech(ServerLevel level, Player caster, ItemStack wand, HitResult aim, int heldFor) {
        PechBlast blast = new PechBlast(level, caster);
        return let(level, caster, blast, 1.2F);
    }

    /** A hook, thrown at a wall. */
    public static boolean grapple(ServerLevel level, Player caster, ItemStack wand, HitResult aim, int heldFor) {
        Grapple hook = new Grapple(level, caster);
        return let(level, caster, hook, 0.9F);
    }

    /** Sends it off along the line of sight and says that something happened, because it did. */
    private static boolean let(ServerLevel level, Player caster, ThrowableProjectile thrown, float pitch) {
        thrown.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, FLIES, TRUE);
        level.addFreshEntity(thrown);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.4F, pitch);
        return true;
    }
}
