package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What is dissolved in a crucible at the moment.
 * <p>
 * The water itself is in the block state, since that is what the model has to know about. What is in the water is
 * here, because a list of aspects is not something a block state can hold.
 */
public class CrucibleBlockEntity extends BlockEntity {
    /** Plain water, for a pot that has had nothing thrown in it yet. */
    private static final int WATER = 0x3F76E4;

    private AspectList dissolved = AspectList.EMPTY;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public AspectList dissolved() {
        return dissolved;
    }

    public void dissolve(AspectList added) {
        dissolved = dissolved.add(added);
        changed();
    }

    public void empty() {
        dissolved = AspectList.EMPTY;
        changed();
    }

    /**
     * What colour the water reads as: whatever is dissolved in it, weighted by how much of each there is.
     * <p>
     * Mixed by amount rather than by picking the largest, so a pot slowly filling with one thing drifts towards its
     * colour instead of snapping to it once that aspect wins.
     */
    public int colour() {
        if (dissolved.isEmpty()) {
            return WATER;
        }
        long red = 0;
        long green = 0;
        long blue = 0;
        long total = 0;
        for (Holder<Aspect> aspect : dissolved.sortedByName()) {
            int amount = dissolved.get(aspect);
            int colour = aspect.value().color();
            red += (long) ((colour >> 16) & 0xFF) * amount;
            green += (long) ((colour >> 8) & 0xFF) * amount;
            blue += (long) (colour & 0xFF) * amount;
            total += amount;
        }
        return (int) (red / total) << 16 | (int) (green / total) << 8 | (int) (blue / total);
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        dissolved = AspectList.CODEC
                .parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag.get("dissolved"))
                .result().orElse(AspectList.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        AspectList.CODEC
                .encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), dissolved)
                .result().ifPresent(written -> tag.put("dissolved", written));
    }

    /** The colour of the water is drawn from this, so it has to reach the client without anyone opening anything. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
