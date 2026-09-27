package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The one thing a pedestal is holding up.
 * <p>
 * It is synced as it changes rather than only on load, because the whole point of a pedestal is that what is on it
 * is visible from where you are standing.
 */
public class ArcanePedestalBlockEntity extends BlockEntity {
    private ItemStack held = ItemStack.EMPTY;

    public ArcanePedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_PEDESTAL.get(), pos, state);
    }

    public ItemStack held() {
        return held;
    }

    public void hold(ItemStack stack) {
        held = stack;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
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

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
