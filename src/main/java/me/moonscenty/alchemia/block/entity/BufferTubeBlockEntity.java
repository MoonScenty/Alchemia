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
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A tube that keeps a few points of essentia of its own.
 * <p>
 * It is a vessel as well as a length of pipe, so the pull it does every turn fills itself before it fills anything
 * beside it, and anything further along the run can draw the lot back out again. What it will not do is take from
 * another buffer: two of them side by side would otherwise pass one point back and forth for ever without a thing
 * being moved anywhere.
 * <p>
 * Each side can be choked with a crouching wand, the original's {@code chokedSides}: open, choked or shut, in that
 * turn. A choked side still fills it but bellows do not hurry it through there; a shut one does not fill it at all,
 * though what is in it can still be drawn back out that way.
 */
public class BufferTubeBlockEntity extends TubeBlockEntity implements EssentiaHolder {
    /** How much it keeps, counted across everything in it. */
    public static final int CAPACITY = 8;

    private AspectList held = AspectList.EMPTY;
    /** How choked each side is: 0 open, 1 choked, 2 shut. */
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

    /** One turn further round: open, choked, shut, open again. */
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

    /** What a broken buffer lets go: the little it was keeping, into the air. */
    public void spill() {
        if (level instanceof ServerLevel served && !held.isEmpty()) {
            AuraHandler.add(served, worldPosition, ModAspects.FLUX, held.total());
        }
        held = AspectList.EMPTY;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        held = AspectList.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("held"))
                .result().orElse(AspectList.EMPTY);
        byte[] read = tag.getByteArray("choke");
        for (int side = 0; side < choked.length; side++) {
            choked[side] = side < read.length ? read[side] : 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        AspectList.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), held)
                .result().ifPresent(written -> tag.put("held", written));
        tag.putByteArray("choke", choked.clone());
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
