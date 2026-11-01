package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

// stores when the handle was turned, not a counter, so the client animates without per-tick sync
public class ValveTubeBlockEntity extends TubeBlockEntity {
    private long worked;

    public ValveTubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TUBE_VALVE.get(), pos, state);
    }

    public long worked() {
        return worked;
    }

    public void worked(long when) {
        worked = when;
        changed();
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
}
