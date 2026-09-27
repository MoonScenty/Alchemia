package me.moonscenty.alchemia.block.entity;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One vessel of essentia, caught as it rises out of a smelter.
 * <p>
 * An alembic holds one kind and one kind only. That is the whole of the sorting: stack several over a smelter and
 * each takes the first thing it is offered, so what comes out separated is separated by how many pots are there
 * rather than by any setting.
 */
public class AlembicBlockEntity extends BlockEntity {
    /** How much one holds. */
    public static final int CAPACITY = 32;

    private Holder<Aspect> holding;
    private int amount;

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

    /** Whether one more of this would fit. An empty vessel will take anything; a full one takes nothing. */
    public boolean wants(Holder<Aspect> aspect) {
        return amount < CAPACITY && (holding == null || holding.value() == aspect.value());
    }

    /** Takes one. Says so, since the smelter only lets go of what was actually caught. */
    public boolean accept(Holder<Aspect> aspect) {
        if (!wants(aspect)) {
            return false;
        }
        holding = aspect;
        amount++;
        changed();
        return true;
    }

    /** What a broken vessel lets go: all of it, into the air. */
    public void spill() {
        if (level instanceof ServerLevel served && amount > 0) {
            AuraHandler.add(served, worldPosition, ModAspects.FLUX, amount);
        }
        holding = null;
        amount = 0;
    }

    /** The colour of what is caught, for whatever wants to show it. */
    public int colour() {
        return holding == null ? 0xFFFFFF : holding.value().color();
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    /**
     * Walks up from a spot and gives one aspect to the first vessel that will take it.
     * <p>
     * A vessel already holding this aspect is preferred over an empty one, so a stack fills up rather than spreading
     * a little of everything across every pot. Only once none of them holds it does an empty one open.
     */
    public static boolean pourInto(Level level, BlockPos from, Holder<Aspect> aspect) {
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
        holding = tag.contains("aspect")
                ? ModAspects.REGISTRY.getHolder(ResourceLocation.parse(tag.getString("aspect")))
                        .map(found -> (Holder<Aspect>) found).orElse(null)
                : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("amount", amount);
        if (holding != null) {
            tag.putString("aspect", holding.value().id().toString());
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
