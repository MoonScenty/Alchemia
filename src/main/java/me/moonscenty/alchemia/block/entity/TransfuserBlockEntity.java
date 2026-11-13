package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
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

public class TransfuserBlockEntity extends BlockEntity implements EssentiaHolder {
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

    private void thread(BlockPos from, BlockPos to, Holder<Aspect> aspect) {
        if (level instanceof ServerLevel server) {
            EssentiaReach.thread(server, from, to, aspect.value().color());
        }
    }

    private List<EssentiaHolder> jars() {
        List<EssentiaHolder> found = new ArrayList<>();
        if (level == null) {
            return found;
        }
        Direction facing = facing();
        for (BlockEntity near : EssentiaReach.holders(level, worldPosition, REACH * 2)) {
            if (!(near instanceof JarBlockEntity || near instanceof MirrorBlockEntity mirror && mirror.essentia())
                    || !inFront(worldPosition, facing, near.getBlockPos(), REACH)) {
                continue;
            }
            found.add((EssentiaHolder) near);
        }
        return found;
    }

    public static boolean inFront(BlockPos from, Direction facing, BlockPos at) {
        return inFront(from, facing, at, REACH);
    }

    public static boolean inFront(BlockPos from, Direction facing, BlockPos at, int reach) {
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
        return out >= 0 && out < reach && Math.abs(a) <= reach && Math.abs(b) <= reach && (out | a | b) != 0;
    }
}
