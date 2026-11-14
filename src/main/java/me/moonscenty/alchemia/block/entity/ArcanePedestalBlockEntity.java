package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ArcanePedestalBlockEntity extends SyncedBlockEntity {
    public static final int BURST = 11;
    public static final int DONE = 12;
    public static final int POLLUTED = 13;

    private ItemStack held = ItemStack.EMPTY;

    public ArcanePedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_PEDESTAL.get(), pos, state);
    }

    public ItemStack held() {
        return held;
    }

    public void hold(ItemStack stack) {
        held = stack;
        changed();
    }

    public static int pack(Vec3i offset) {
        return (offset.getX() + 16) << 10 | (offset.getY() + 16) << 5 | (offset.getZ() + 16);
    }

    public static Vec3i unpack(int packed) {
        return new Vec3i((packed >> 10 & 31) - 16, (packed >> 5 & 31) - 16, (packed & 31) - 16);
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (id == BURST || id == DONE || id == POLLUTED) {
            if (level != null && level.isClientSide) {
                me.moonscenty.alchemia.client.InfusionEffects.stand(level, worldPosition, id, data);
            }
            return true;
        }
        return super.triggerEvent(id, data);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        held = tag.contains("held")
                ? ItemStack.parse(registries, tag.getCompound("held")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!held.isEmpty()) {
            tag.put("held", held.save(registries));
        }
    }
}
