package me.moonscenty.alchemia.wand;

import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Letting a wand off: taking aim, paying for it, and waiting before it can be done again.
 *
 * <p>Everything here is the same for all twelve foci. What each of them actually does is its own {@link Spell},
 * and none of them is reached until the price has been met.
 *
 * <p>The order matters and is the original's. The price is checked before the spell runs and taken after, so a
 * spell that finds nothing to do leaves the wand as full as it was. A wand pointed at the sky is not a wand being
 * emptied into it.
 */
public final class Casting {
    /** How far a wand reaches, in blocks. The original looked twenty out for something to hit. */
    public static final double REACH = 20.0;

    /** A thousandth of a second to a millisecond and twenty ticks to a second, which is how a wait is counted here. */
    private static final int A_SECOND = 1000;
    private static final int TICKS_A_SECOND = 20;

    private Casting() {
    }

    /** The focus on the wand, if there is one and it is a focus. */
    public static FocusItem fittedTo(ItemStack wand) {
        return WandItem.focus(wand).getItem() instanceof FocusItem focus ? focus : null;
    }

    /**
     * Lets the wand off once, if it can be.
     *
     * @return whether anything happened, so that a hand can be swung for it
     */
    public static boolean cast(ServerLevel level, Player caster, ItemStack wand, int heldFor) {
        FocusItem fitted = fittedTo(wand);
        if (fitted == null || caster.getCooldowns().isOnCooldown(wand.getItem())) {
            return false;
        }
        Focus focus = fitted.focus();
        if (!VisHolder.canPay(wand, focus.cost(), caster)) {
            return false;
        }
        if (!focus.spell().cast(level, caster, wand, aim(caster), heldFor)) {
            return false;
        }

        VisHolder holder = VisHolder.of(wand);
        if (holder != null) {
            holder.take(wand, focus.cost(), caster);
        }
        waitAfter(caster, wand, focus);
        return true;
    }

    /**
     * How long before this wand will go off again.
     * <p>
     * The original counted in milliseconds off the wall clock. A cooldown here is counted in ticks, which is what
     * this game counts in and what greys the wand out in the hand while it is waiting. A wait shorter than a tick
     * becomes one tick: nothing in this game happens twice in a tick anyway.
     */
    private static void waitAfter(Player caster, ItemStack wand, Focus focus) {
        if (focus.cooldown() <= 0) {
            return;
        }
        int ticks = Math.max(1, focus.cooldown() * TICKS_A_SECOND / A_SECOND);
        caster.getCooldowns().addCooldown(wand.getItem(), ticks);
    }

    /**
     * What the wand is pointed at: whatever living thing is in the way, or the block behind it, or the air at the
     * end of the caster's reach.
     * <p>
     * An entity wins over the block behind it, which is what anybody pointing a wand at a zombie standing against
     * a wall means by it.
     */
    public static HitResult aim(Player caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 along = caster.getViewVector(1.0F).scale(REACH);
        Vec3 far = eye.add(along);

        BlockHitResult blocks = caster.level().clip(new ClipContext(eye, far,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 stop = blocks.getType() == HitResult.Type.MISS ? far : blocks.getLocation();

        AABB swept = caster.getBoundingBox().expandTowards(along).inflate(1.0);
        EntityHitResult struck = ProjectileUtil.getEntityHitResult(caster.level(), caster, eye, stop, swept,
                Casting::catchable);
        return struck != null ? struck : blocks;
    }

    /** What a wand can be aimed at. Anything that can be hit, except the one holding the wand. */
    private static boolean catchable(Entity entity) {
        return !entity.isSpectator() && entity.isAlive() && entity.isPickable();
    }

    /** Whether a level is one things actually happen in, which is the only one a spell is cast in. */
    public static ServerLevel served(Level level) {
        return level instanceof ServerLevel server ? server : null;
    }
}
