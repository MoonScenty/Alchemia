package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.block.HoleBlock;
import me.moonscenty.alchemia.block.entity.HoleBlockEntity;
import me.moonscenty.alchemia.outer.OuterLands;
import me.moonscenty.alchemia.registry.ModSounds;
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

// blocks are set aside and restored when the hole closes; nothing drops
public final class Hole {
    // original's value
    private static final int LOOKS_IN = 33;
    private static final int STAYS_OPEN = 120;
    // per rank of enlarge (depth) and of extend (duration)
    private static final int DEEPER = 8;
    private static final int LONGER = 60;

    private Hole() {
    }

    public static boolean cast(ServerLevel level, LivingEntity who, ItemStack wand, HitResult aim, int heldFor) {
        if (!(who instanceof Player caster)) {
            return false;
        }
        // original: only the outer lands refuse; any other failure still costs vis
        if (!(aim instanceof BlockHitResult struck) || aim.getType() != HitResult.Type.BLOCK) {
            return true;
        }
        BlockPos face = struck.getBlockPos();
        Direction in = struck.getDirection().getOpposite();
        if (level.dimension() == OuterLands.LEVEL) {
            level.playSound(null, face, ModSounds.WAND_FAIL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return false;
        }
        if (!caster.mayInteract(level, face)) {
            return true;
        }

        int looks = LOOKS_IN + me.moonscenty.alchemia.wand.Casting.worked(wand,
                me.moonscenty.alchemia.wand.FocusUpgrade.ENLARGE) * DEEPER;
        int holds = STAYS_OPEN + me.moonscenty.alchemia.wand.Casting.worked(wand,
                me.moonscenty.alchemia.wand.FocusUpgrade.EXTEND) * LONGER;
        int thick = thickness(level, face, in, looks);
        if (thick > 0) {
            HoleBlockEntity.bore(level, face, holds, thick, in);
        }
        level.playSound(null, face, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    // measured before anything opens, so the tunnel depends on the intact wall
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
