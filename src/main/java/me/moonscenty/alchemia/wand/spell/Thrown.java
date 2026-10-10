package me.moonscenty.alchemia.wand.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
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

    private Thrown() {
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
