package me.moonscenty.alchemia.block.entity;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.ArcaneBoreBaseBlock;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An arcane bore's base: what it stands on or hangs from, where what it mines comes out, and where entropy is piped
 * in to drive it. It takes essentia on its three sides that are not the nozzle, one point at a time, while the bore
 * above or below it has room for more.
 */
public class ArcaneBoreBaseBlockEntity extends BlockEntity implements EssentiaHolder {
    public ArcaneBoreBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_BORE_BASE.get(), pos, state);
    }

    /** The bore it serves, above it or else below. */
    @Nullable
    private ArcaneBoreBlockEntity bore() {
        if (level == null) {
            return null;
        }
        if (level.getBlockEntity(worldPosition.above()) instanceof ArcaneBoreBlockEntity above) {
            return above;
        }
        return level.getBlockEntity(worldPosition.below()) instanceof ArcaneBoreBlockEntity below ? below : null;
    }

    private Direction nozzle() {
        return getBlockState().getValue(ArcaneBoreBaseBlock.FACING);
    }

    @Override
    public AspectList held() {
        return AspectList.EMPTY;
    }

    @Override
    public boolean wants(Holder<Aspect> aspect) {
        ArcaneBoreBlockEntity bore = bore();
        return aspect.value() == ModAspects.ENTROPY.value() && bore != null && bore.wantsEssentia();
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        ArcaneBoreBlockEntity bore = bore();
        if (!wants(aspect) || bore == null) {
            return false;
        }
        bore.fuel();
        return true;
    }

    /** Pipes meet it on its three sides that are not the nozzle. */
    @Override
    public boolean reachableFrom(Direction side) {
        return side.getAxis().isHorizontal() && side != nozzle();
    }

    @Override
    public boolean givesTo(Direction side) {
        return false;
    }

    /** One point straight out of a vessel set against one of its sides; a pipe there fills it on its own. */
    void drawFromBeside(ArcaneBoreBlockEntity bore) {
        if (level == null) {
            return;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (!reachableFrom(side)) {
                continue;
            }
            BlockPos beside = worldPosition.relative(side);
            if (level.getBlockState(beside).getBlock() instanceof TubeBlock
                    || !(level.getBlockEntity(beside) instanceof EssentiaHolder source)
                    || !source.reachableFrom(side.getOpposite()) || !source.givesTo(side.getOpposite())) {
                continue;
            }
            if (source.held().get(ModAspects.ENTROPY) > 0 && source.release(ModAspects.ENTROPY)) {
                bore.fuel();
                return;
            }
        }
    }
}
