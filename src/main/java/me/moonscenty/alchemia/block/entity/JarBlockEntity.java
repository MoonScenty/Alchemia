package me.moonscenty.alchemia.block.entity;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.JarBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A jar of one essentia, with a lid on it.
 * <p>
 * It fills and empties through the lid and nowhere else. A jar is a vessel, not a junction: a pipe run has to come
 * down onto it, which is what makes a wall of jars something you lay out rather than something you scatter.
 */
public class JarBlockEntity extends BlockEntity implements EssentiaHolder {
    /** How much one holds. */
    public static final int CAPACITY = 64;
    /** How many heights the liquid is drawn at. */
    public static final int STEPS = 4;

    private Holder<Aspect> holding;
    private int amount;

    public JarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JAR.get(), pos, state);
    }

    public Optional<Holder<Aspect>> holding() {
        return Optional.ofNullable(holding);
    }

    public int amount() {
        return amount;
    }

    @Override
    public AspectList held() {
        return holding == null ? AspectList.EMPTY : AspectList.of(holding, amount);
    }

    @Override
    public boolean wants(Holder<Aspect> aspect) {
        return amount < CAPACITY && (holding == null || holding.value() == aspect.value());
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        if (!wants(aspect)) {
            return false;
        }
        holding = aspect;
        amount++;
        changed();
        return true;
    }

    @Override
    public boolean release(Holder<Aspect> aspect) {
        if (holding == null || holding.value() != aspect.value() || amount <= 0) {
            return false;
        }
        amount--;
        if (amount == 0) {
            holding = null;
        }
        changed();
        return true;
    }

    /** The lid is the only way in or out. Glass does not take a pipe through its side. */
    @Override
    public boolean reachableFrom(Direction side) {
        return side == Direction.UP;
    }

    /** What a broken jar lets go: all of it, into the air. */
    public void spill() {
        if (level instanceof ServerLevel served && amount > 0) {
            AuraHandler.add(served, worldPosition, ModAspects.FLUX, amount);
        }
        holding = null;
        amount = 0;
    }

    /** The colour the liquid is drawn in. */
    public int colour() {
        return holding == null ? 0xFFFFFF : holding.value().color();
    }

    /**
     * Keeps the block state in step with how full it looks.
     * <p>
     * The state carries the height of the liquid and nothing else, since that is all the model needs; what the
     * essentia actually is lives here, and the colour is asked for separately.
     */
    private void changed() {
        setChanged();
        if (level == null || level.isClientSide) {
            return;
        }
        int shown = amount <= 0 ? 0 : Math.max(1, amount * STEPS / CAPACITY);
        BlockState updated = getBlockState().setValue(JarBlock.FILL, Math.min(STEPS, shown));
        if (updated != getBlockState()) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        } else {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
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

    /** The colour of what is standing in it is drawn from this, so it has to reach the client on its own. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
