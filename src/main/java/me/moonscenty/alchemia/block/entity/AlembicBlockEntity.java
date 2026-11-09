package me.moonscenty.alchemia.block.entity;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AlembicBlockEntity extends SyncedBlockEntity implements EssentiaHolder, Spills {
    public static final int CAPACITY = 32;

    private Holder<Aspect> holding;
    private int amount;
    private Holder<Aspect> filter;
    private Direction labelFace = Direction.DOWN;

    public AlembicBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALEMBIC.get(), pos, state);
    }

    public Optional<Holder<Aspect>> holding() {
        return Optional.ofNullable(holding);
    }

    public int amount() {
        return amount;
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    public Optional<Holder<Aspect>> filter() {
        return Optional.ofNullable(filter);
    }

    public Direction labelFace() {
        return labelFace;
    }

    public void label(Holder<Aspect> aspect, Direction face) {
        filter = aspect;
        labelFace = face;
        changed();
    }

    public void unlabel() {
        filter = null;
        labelFace = Direction.DOWN;
        changed();
    }

    public void draw(int points) {
        amount = Math.max(0, amount - points);
        if (amount == 0) {
            holding = null;
        }
        changed();
    }

    // original: tubes may only draw from it, never through the label or the bottom
    @Override
    public boolean reachableFrom(Direction side) {
        return side != labelFace && side != Direction.DOWN;
    }

    @Override
    public boolean takesFrom(Direction side) {
        return false;
    }

    @Override
    public AspectList held() {
        return holding == null ? AspectList.EMPTY : AspectList.of(holding, amount);
    }

    @Override
    public boolean release(Holder<Aspect> aspect) {
        if (holding == null || holding.value() != aspect.value() || amount <= 0) {
            return false;
        }
        amount--;
        if (amount == 0) {
            holding = null;
        }
        changed();
        return true;
    }

    @Override
    public boolean wants(Holder<Aspect> aspect) {
        return amount < CAPACITY && (filter == null || filter.value() == aspect.value())
                && (holding == null || holding.value() == aspect.value());
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        if (!wants(aspect)) {
            return false;
        }
        holding = aspect;
        amount++;
        changed();
        return true;
    }

    public void pourOut() {
        spill();
        changed();
    }

    @Override
    public void spill() {
        if (level instanceof ServerLevel served && amount > 0) {
            AuraHandler.add(served, worldPosition, ModAspects.FLUX, amount);
        }
        holding = null;
        amount = 0;
    }

    public int colour() {
        return holding == null ? 0xFFFFFF : holding.value().color();
    }

    @Override
    protected void changed() {
        changed(Block.UPDATE_ALL);
    }

    public static boolean pourInto(Level level, BlockPos from, Holder<Aspect> aspect) {
        // first pass prefers a vessel already holding this aspect, second allows an empty one
        for (boolean fussy : new boolean[] {true, false}) {
            for (int up = 1; ; up++) {
                BlockPos at = from.above(up);
                if (!(level.getBlockEntity(at) instanceof AlembicBlockEntity alembic)) {
                    break;
                }
                boolean matching = !alembic.isEmpty() && alembic.wants(aspect);
                if ((fussy ? matching : alembic.wants(aspect)) && alembic.accept(aspect)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        amount = tag.getInt("amount");
        holding = tag.contains("aspect") ? ModAspects.byId(tag.getString("aspect")) : null;
        filter = tag.contains("filter") ? ModAspects.byId(tag.getString("filter")) : null;
        labelFace = Direction.from3DDataValue(tag.getByte("label_face"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("amount", amount);
        if (holding != null) {
            tag.putString("aspect", holding.value().id().toString());
        }
        if (filter != null) {
            tag.putString("filter", filter.value().id().toString());
        }
        tag.putByte("label_face", (byte) labelFace.get3DDataValue());
    }
}
