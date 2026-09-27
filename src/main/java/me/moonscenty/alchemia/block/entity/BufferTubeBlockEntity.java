package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A tube that keeps a few points of essentia of its own.
 * <p>
 * It is a vessel as well as a length of pipe, so the pull it does every turn fills itself before it fills anything
 * beside it, and anything further along the run can draw the lot back out again. What it will not do is take from
 * another buffer: two of them side by side would otherwise pass one point back and forth for ever without a thing
 * being moved anywhere.
 */
public class BufferTubeBlockEntity extends TubeBlockEntity implements EssentiaHolder {
    /** How much it keeps, counted across everything in it. */
    public static final int CAPACITY = 8;

    private AspectList held = AspectList.EMPTY;

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

    /** What a broken buffer lets go: the little it was keeping, into the air. */
    public void spill() {
        if (level instanceof ServerLevel served && !held.isEmpty()) {
            AuraHandler.add(served, worldPosition, ModAspects.FLUX, held.total());
        }
        held = AspectList.EMPTY;
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        held = AspectList.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("held"))
                .result().orElse(AspectList.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        AspectList.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), held)
                .result().ifPresent(written -> tag.put("held", written));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
