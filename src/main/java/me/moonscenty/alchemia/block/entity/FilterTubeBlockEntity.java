package me.moonscenty.alchemia.block.entity;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class FilterTubeBlockEntity extends TubeBlockEntity {
    private Holder<Aspect> only;

    public FilterTubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TUBE_FILTER.get(), pos, state);
    }

    public Optional<Holder<Aspect>> only() {
        return Optional.ofNullable(only);
    }

    public void only(Holder<Aspect> aspect) {
        only = aspect;
        changed();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        only = tag.contains("only") ? ModAspects.byId(tag.getString("only")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (only != null) {
            tag.putString("only", only.value().id().toString());
        }
    }
}
