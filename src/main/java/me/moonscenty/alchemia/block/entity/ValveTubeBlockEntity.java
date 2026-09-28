package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * When a valve was last worked, so that the handle can be seen swinging round rather than snapping.
 * <p>
 * A moment by the world clock rather than a counter ticked up here: a counter has to be sent to every client that
 * can see the valve, over and over, and a moment is sent once and stays true. The drawing works out for itself how
 * far round the wheel has got.
 */
public class ValveTubeBlockEntity extends TubeBlockEntity {
    private long worked;

    public ValveTubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TUBE_VALVE.get(), pos, state);
    }

    /** When the handle started moving, by the world clock. */
    public long worked() {
        return worked;
    }

    public void worked(long when) {
        worked = when;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        worked = tag.getLong("worked");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("worked", worked);
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
