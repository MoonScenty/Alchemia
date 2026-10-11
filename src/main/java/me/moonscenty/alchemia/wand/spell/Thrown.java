package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The foci that throw something and let it do the rest.
 *
 * <p>None of them aims. Whatever the wand was pointed at only settles which way the thing goes; what it lands on
 * is whatever happens to be in the way by the time it gets there, which is the whole difference between a thrown
 * thing and a bolt.
 *
 * <p>What is worked into the focus is read here rather than inside each thrown thing, because it is the focus
 * that was upgraded and the thrown thing only carries out what it was sent to do.
 */
public final class Thrown {
    /** How fast each leaves the hand, and how far off true. Both fly straight: these are not arrows. */
    private static final float FLIES = 1.5F;
    private static final float TRUE = 0.0F;

    /** How many embers leave the wand each tick, and how wide the spray is, in degrees. */
    private static final int MOTES = 2;
    private static final float SCATTERS = 15.0F;
    /** A beam is the same fire drawn into a line: barely any spread, and it lasts half again as long. */
    private static final float NARROW = 0.25F;
    private static final float SHARPER = 0.5F;
    private static final float HOTTER = 1.5F;
    private static final int REACHES = 30;
    /** What one mote is worth before anything is worked in, and what a rank of force adds. */
    private static final float EMBER = 2.0F;
    /** How often the roar of it is heard while the button is down, in ticks. */
    private static final int ROARS_EVERY = 10;

    /** A volley is this many splinters plus two a rank, each worth almost nothing. */
    private static final int SPLINTERS = 5;
    private static final float SPLINTER = 1.0F;
    /** A spike, a boulder, and what a rank of force is worth to each. */
    private static final float SPIKE = 3.0F;
    private static final float SHARPENS_SPIKE = 1.5F;
    private static final float BOULDER = 4.0F;
    private static final float SHARPENS_BOULDER = 2.0F;
    /** How many walls a boulder comes off before it gives up. */
    private static final int WALLS = 6;

    /** What a splinter of vis is worth, and what a rank of force adds to it. */
    private static final float SLIVER = 1.0F;
    private static final float SHARPENS_SLIVER = 0.5F;

    private Thrown() {
    }

    /**
     * Fire: a spray of embers held down, or one ball thrown.
     * <p>
     * Which it is depends on what is worked into the focus, and they are not the same tool. The spray fills a
     * doorway for as long as the vis holds out; the ball is one throw across a room with a second's wait after
     * it. A beam is the spray again, drawn into a line that burns through whatever stands in it.
     */
    public static boolean fire(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor) {
        int potency = Casting.worked(wand, FocusUpgrade.POTENCY);
        int alchemists = Casting.worked(wand, FocusUpgrade.ALCHEMISTS_FIRE);
        if (Casting.has(wand, FocusUpgrade.FIREBALL)) {
            FireOrb ball = new FireOrb(level, caster)
                    .bursting(1.0F + potency * FireOrb.HARDER, alchemists > 0);
            return let(level, caster, ball, 0.6F);
        }

        boolean beam = Casting.has(wand, FocusUpgrade.FIREBEAM);
        float hurts = EMBER + potency;
        if (beam) {
            hurts = (hurts + SHARPER) * HOTTER;
        }
        for (int mote = 0; mote < MOTES + potency; mote++) {
            Ember ember = new Ember(level, caster).burning(hurts, alchemists);
            if (beam) {
                ember.lasting(REACHES);
            }
            ember.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, FLIES,
                    beam ? NARROW : SCATTERS);
            level.addFreshEntity(ember);
        }
        if (heldFor % ROARS_EVERY == 0) {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.33F, 1.8F);
        }
        return true;
    }

    /**
     * Frost: one spike, a volley of splinters, or a boulder.
     * <p>
     * The spike is aimed and the volley is not; the boulder is aimed and then stops being, since it comes off
     * walls and ends up where it ends up. Anything the cold touches is slowed, if the focus was built for it.
     */
    public static boolean frost(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor) {
        int potency = Casting.worked(wand, FocusUpgrade.POTENCY);
        int chills = Casting.worked(wand, FocusUpgrade.ALCHEMISTS_FROST);

        if (Casting.has(wand, FocusUpgrade.SCATTERSHOT)) {
            for (int shard = 0; shard < SPLINTERS + potency * 2; shard++) {
                FrostShard splinter = new FrostShard(level, caster)
                        .biting(SPLINTER, chills).asSplinter();
                splinter.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, FLIES, 8.0F);
                level.addFreshEntity(splinter);
            }
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.4F, 1.6F);
            return true;
        }
        if (Casting.has(wand, FocusUpgrade.ICE_BOULDER)) {
            return let(level, caster, new FrostShard(level, caster)
                    .biting(BOULDER + potency * SHARPENS_BOULDER, chills).bouncing(WALLS), 0.9F);
        }
        return let(level, caster, new FrostShard(level, caster)
                .biting(SPIKE + potency * SHARPENS_SPIKE, chills), 1.4F);
    }

    /**
     * A splinter that follows what the wand was pointed at.
     * <p>
     * It will not go off at all without a mark. That is the whole of what makes it cheap: a focus that cannot
     * miss is a focus that has to be given something to not miss.
     */
    public static boolean visShard(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim,
            int heldFor) {
        if (!(aim instanceof EntityHitResult struck) || !(struck.getEntity() instanceof LivingEntity mark)) {
            return false;
        }
        int potency = Casting.worked(wand, FocusUpgrade.POTENCY);
        VisShard shard = new VisShard(level, caster, mark)
                .chasing(SLIVER + potency * SHARPENS_SLIVER, Casting.has(wand, FocusUpgrade.PERSISTENT));
        return let(level, caster, shard, 1.7F);
    }

    /** All six primals wound into a ball, and a hole where it lands. */
    public static boolean primal(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim,
            int heldFor) {
        PrimalOrb orb = new PrimalOrb(level, caster);
        if (Casting.has(wand, FocusUpgrade.SEEKER)) {
            orb.seeking();
        }
        return let(level, caster, orb, 0.8F);
    }

    /** A pech's curse, thrown at nothing in particular. */
    public static boolean pech(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor) {
        PechBlast blast = new PechBlast(level, caster).cursing(
                Casting.worked(wand, FocusUpgrade.POTENCY),
                Casting.worked(wand, FocusUpgrade.EXTEND),
                Casting.has(wand, FocusUpgrade.NIGHTSHADE));
        return let(level, caster, blast, 1.2F);
    }

    /** A hook, thrown at a wall. */
    public static boolean grapple(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim,
            int heldFor) {
        Grapple hook = new Grapple(level, caster).hauling(
                Casting.worked(wand, FocusUpgrade.POTENCY),
                Casting.has(wand, FocusUpgrade.STICKY));
        return let(level, caster, hook, 0.9F);
    }

    /** Sends it off along the line of sight and says that something happened, because it did. */
    private static boolean let(ServerLevel level, LivingEntity caster, ThrowableProjectile thrown, float pitch) {
        thrown.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, FLIES, TRUE);
        level.addFreshEntity(thrown);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.4F, pitch);
        return true;
    }
}
