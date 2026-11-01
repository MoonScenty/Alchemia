package me.moonscenty.alchemia.block.entity;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.ArcaneBoreBaseBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ArcaneBoreBaseBlockEntity extends BlockEntity implements EssentiaHolder {
    public ArcaneBoreBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_BORE_BASE.get(), pos, state);
    }

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

    @Override
    public boolean reachableFrom(Direction side) {
        return side.getAxis().isHorizontal() && side != nozzle();
    }

    @Override
    public boolean givesTo(Direction side) {
        return false;
    }

    void drawFromBeside(ArcaneBoreBlockEntity bore) {
        if (level == null) {
            return;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (reachableFrom(side) && EssentiaHolder.drawFrom(level, worldPosition, side, ModAspects.ENTROPY)) {
                bore.fuel();
                return;
            }
        }
    }
}
