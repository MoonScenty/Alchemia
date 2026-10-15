package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.TransfuserBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.essentia.EssentiaReach;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An essentia transfuser, the original's {@code TileEssentiaInput} and {@code TileEssentiaOutput}: a pipe fitting
 * that moves essentia through the air to or from the jars in front of it, as the infusion matrix draws it.
 * <p>
 * Its back takes a pipe. The filling kind is somewhere a pipe can empty into: whatever reaches it goes to a jar in
 * front of it, one already holding something before an empty one, the nearest first. The emptying kind is somewhere
 * a pipe can draw from: it gives whatever the jars in front of it hold, again the nearest first. In front means
 * sixteen blocks out from its face and sixteen to either side. Other pipes and transfusers are not counted as jars.
 */
public class TransfuserBlockEntity extends BlockEntity implements EssentiaHolder {
    /** How far it reaches out in front, and to the sides. */
    public static final int REACH = 16;

    public TransfuserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSFUSER.get(), pos, state);
    }

    private boolean filling() {
        return getBlockState().getBlock() instanceof TransfuserBlock block && block.filling();
    }

    private Direction facing() {
        return getBlockState().getValue(TransfuserBlock.FACING);
    }

    /** Only its back, where the pipe goes. */
    @Override
    public boolean reachableFrom(Direction side) {
        return side == facing().getOpposite();
    }

    @Override
    public boolean takesFrom(Direction side) {
        return filling() && side == facing().getOpposite();
    }

    @Override
    public boolean givesTo(Direction side) {
        return !filling() && side == facing().getOpposite();
    }

    /** The filling kind holds nothing of its own; the emptying kind shows what the jars in front hold. */
    @Override
    public AspectList held() {
        AspectList all = AspectList.EMPTY;
        if (filling() || level == null) {
            return all;
        }
        for (EssentiaHolder jar : jars()) {
            all = all.add(jar.held());
        }
        return all;
    }

    @Override
    public boolean wants(Holder<Aspect> aspect) {
        return filling() && jars().stream().anyMatch(jar -> jar.wants(aspect));
    }

    /** Into a jar that already has something, if one will take it, and only then into an empty one. */
    @Override
    public boolean accept(Holder<Aspect> aspect) {
        if (!filling()) {
            return false;
        }
        List<EssentiaHolder> jars = jars();
        for (boolean holding : new boolean[] {true, false}) {
            for (EssentiaHolder jar : jars) {
                if ((jar.held().total() > 0) == holding && jar.wants(aspect) && jar.accept(aspect)) {
                    thread(worldPosition, ((BlockEntity) jar).getBlockPos(), aspect);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean release(Holder<Aspect> aspect) {
        if (filling()) {
            return false;
        }
        for (EssentiaHolder jar : jars()) {
            if (jar.held().get(aspect) > 0 && jar.release(aspect)) {
                thread(((BlockEntity) jar).getBlockPos(), worldPosition, aspect);
                return true;
            }
        }
        return false;
    }

    /** Drops of the essentia from where it came from to where it went, as an infusion draws them. */
    private void thread(BlockPos from, BlockPos to, Holder<Aspect> aspect) {
        if (level instanceof ServerLevel server) {
            EssentiaReach.thread(server, from, to, aspect.value().color());
        }
    }

    /** Every jar in front, nearest first. */
    private List<EssentiaHolder> jars() {
        List<EssentiaHolder> found = new ArrayList<>();
        if (level == null) {
            return found;
        }
        Direction facing = facing();
        for (BlockEntity near : EssentiaReach.holders(level, worldPosition, REACH * 2)) {
            if (near == this || near instanceof TubeBlockEntity || near instanceof TransfuserBlockEntity
                    || !inFront(worldPosition, facing, near.getBlockPos())) {
                continue;
            }
            found.add((EssentiaHolder) near);
        }
        found.sort(Comparator.comparingDouble(jar -> ((BlockEntity) jar).getBlockPos().distSqr(worldPosition)));
        return found;
    }

    /** Within sixteen across and sixteen out from the face, as the original searched. */
    public static boolean inFront(BlockPos from, Direction facing, BlockPos at) {
        int dx = at.getX() - from.getX();
        int dy = at.getY() - from.getY();
        int dz = at.getZ() - from.getZ();
        int out = dx * facing.getStepX() + dy * facing.getStepY() + dz * facing.getStepZ();
        int a;
        int b;
        switch (facing.getAxis()) {
            case Y -> {
                a = dx;
                b = dz;
            }
            case Z -> {
                a = dx;
                b = dy;
            }
            default -> {
                a = dy;
                b = dz;
            }
        }
        return out >= 0 && out < REACH && Math.abs(a) <= REACH && Math.abs(b) <= REACH && (out | a | b) != 0;
    }
}
