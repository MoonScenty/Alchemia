package me.moonscenty.alchemia.block.entity;

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
import net.minecraft.world.level.block.state.BlockState;

public class BufferTubeBlockEntity extends TubeBlockEntity implements EssentiaHolder, Spills {
    public static final int CAPACITY = 8;

    private AspectList held = AspectList.EMPTY;
    // per side: 0 open, 1 choked, 2 shut
    private final byte[] choked = new byte[6];

    public BufferTubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TUBE_BUFFER.get(), pos, state);
    }

    @Override
    public AspectList held() {
        return held;
    }

    @Override
    public boolean wants(Holder<Aspect> aspect) {
        return held.total() < CAPACITY;
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        if (!wants(aspect)) {
            return false;
        }
        held = held.add(aspect, 1);
        changed();
        return true;
    }

    @Override
    public boolean release(Holder<Aspect> aspect) {
        if (held.get(aspect) <= 0) {
            return false;
        }
        held = held.reduce(aspect, 1);
        changed();
        return true;
    }

    public int choked(Direction side) {
        return choked[side.ordinal()];
    }

    public void choke(Direction side) {
        choked[side.ordinal()] = (byte) ((choked[side.ordinal()] + 1) % 3);
        changed();
    }

    @Override
    protected boolean drawsThrough(Direction side) {
        return choked[side.ordinal()] < 2;
    }

    @Override
    protected boolean blownThrough(Direction side) {
        return choked[side.ordinal()] == 0;
    }

    @Override
    public void spill() {
        if (level instanceof ServerLevel served && !held.isEmpty()) {
            AuraHandler.add(served, worldPosition, ModAspects.FLUX, held.total());
        }
        held = AspectList.EMPTY;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        held = AspectList.read(tag, "held", registries);
        byte[] read = tag.getByteArray("choke");
        for (int side = 0; side < choked.length; side++) {
            choked[side] = side < read.length ? read[side] : 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        AspectList.write(tag, "held", held, registries);
        tag.putByteArray("choke", choked.clone());
    }
}
