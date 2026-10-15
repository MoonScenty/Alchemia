package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.block.HoleBlock;
import me.moonscenty.alchemia.block.entity.HoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A hole bored clean through a wall, which closes again six seconds later.
 *
 * <p>Nothing is broken and nothing drops. What is in the way is set aside and handed back, so this is a way
 * through a thing rather than a way of taking it: a vault wall opened with this is a vault wall again before
 * anybody has carried much out of it.
 *
 * <p>How deep it goes is how thick the wall is. The focus looks straight in before it starts and counts the solid
 * run, so a hole never opens into the far side of a mountain and never stops short of the far side of a door.
 * Thirty-three squares is as far as it will look, which is thicker than anything worth walking through. A focus
 * that has been widened looks further in, and one that has been lengthened holds the hole open longer; both are
 * what the original did with those two, and neither makes the tunnel any wider.
 */
public final class Hole {
    /** How far in it will look for the far side. */
    private static final int LOOKS_IN = 33;
    /** How long a hole stays open, in ticks. */
    private static final int STAYS_OPEN = 120;
    /** What a rank of widening adds to how far it looks, and a rank of lengthening to how long it holds. */
    private static final int DEEPER = 8;
    private static final int LONGER = 60;

    private Hole() {
    }

    public static boolean cast(ServerLevel level, LivingEntity who, ItemStack wand, HitResult aim, int heldFor) {
        if (!(who instanceof Player caster)) {
            return false;
        }
        if (!(aim instanceof BlockHitResult struck) || aim.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos face = struck.getBlockPos();
        Direction in = struck.getDirection().getOpposite();
        if (!caster.mayInteract(level, face)) {
            return false;
        }

        int looks = LOOKS_IN + me.moonscenty.alchemia.wand.Casting.worked(wand,
                me.moonscenty.alchemia.wand.FocusUpgrade.ENLARGE) * DEEPER;
        int holds = STAYS_OPEN + me.moonscenty.alchemia.wand.Casting.worked(wand,
                me.moonscenty.alchemia.wand.FocusUpgrade.EXTEND) * LONGER;
        int thick = thickness(level, face, in, looks);
        if (thick == 0) {
            return false;
        }
        if (!HoleBlockEntity.bore(level, face, holds, thick, in)) {
            return false;
        }
        level.playSound(null, face, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    /**
     * How many squares of solid there are before the far side.
     * <p>
     * Counted before a single square is opened, so the whole tunnel is decided on the wall as it stands rather
     * than on a wall that is already full of holes.
     */
    private static int thickness(ServerLevel level, BlockPos face, Direction in, int looks) {
        BlockPos at = face;
        int deep = 0;
        while (deep < looks) {
            BlockState there = level.getBlockState(at);
            if (!HoleBlock.borable(there) || there.getDestroySpeed(level, at) < 0.0F
                    || level.getBlockEntity(at) != null) {
                break;
            }
            deep++;
            at = at.relative(in);
        }
        return deep;
    }
}
