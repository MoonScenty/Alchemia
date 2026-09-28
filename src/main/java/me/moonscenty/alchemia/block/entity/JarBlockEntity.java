package me.moonscenty.alchemia.block.entity;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.JarBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
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
    /** The aspect written on the label stuck to it, if there is one. */
    private Holder<Aspect> label;
    /** Whether a brace has been fitted, which stops anything being drawn back out. */
    private boolean braced;

    public JarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JAR.get(), pos, state);
    }

    public Optional<Holder<Aspect>> holding() {
        return Optional.ofNullable(holding);
    }

    public int amount() {
        return amount;
    }

    public Optional<Holder<Aspect>> label() {
        return Optional.ofNullable(label);
    }

    public boolean braced() {
        return braced;
    }

    /**
     * Sticks a label on, or takes it off again.
     * <p>
     * A labelled jar keeps the aspect whether or not there is any of it left, which is the whole point: an empty jar
     * on a shelf still says what belongs in it, and a tube that would otherwise drop something else in cannot.
     */
    public void label(Holder<Aspect> aspect) {
        label = aspect;
        if (aspect != null) {
            holding = aspect;
        } else if (amount == 0) {
            holding = null;
        }
        changed();
    }

    public void brace(boolean fitted) {
        braced = fitted;
        changed();
    }

    @Override
    public AspectList held() {
        return holding == null ? AspectList.EMPTY : AspectList.of(holding, amount);
    }

    /** A labelled jar takes what its label says and nothing else, full or empty. */
    @Override
    public boolean wants(Holder<Aspect> aspect) {
        if (amount >= CAPACITY) {
            return false;
        }
        return label != null ? label.value() == aspect.value() : holding == null || holding.value() == aspect.value();
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

    /** A braced jar is a jar you fill and leave. Nothing comes back out of one until the brace comes off. */
    @Override
    public boolean release(Holder<Aspect> aspect) {
        if (braced || holding == null || holding.value() != aspect.value() || amount <= 0) {
            return false;
        }
        amount--;
        if (amount == 0 && label == null) {
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

    /**
     * What a jar taken up off the floor carries with it: what is in it, and what is written on it.
     * <p>
     * A warded jar is warded whether or not it is standing on anything. Breaking one and finding the essentia
     * gone into the air would make a jar a thing you dare not move, and a shelf of jars something you build once
     * and never touch again.
     */
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (holding != null && amount > 0) {
            components.set(ModDataComponents.CONTENTS.get(), AspectList.of(holding, amount));
        }
        if (label != null) {
            components.set(ModDataComponents.ESSENTIA.get(), label);
        }
    }

    /** And what it carried back out when it is set down again. */
    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        AspectList inside = components.get(ModDataComponents.CONTENTS.get());
        if (inside != null && !inside.isEmpty()) {
            holding = inside.sortedByAmount().getFirst();
            amount = Math.min(CAPACITY, inside.get(holding));
        }
        label = components.get(ModDataComponents.ESSENTIA.get());
        if (label != null && holding == null) {
            holding = label;
        }
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
        BlockState updated = getBlockState()
                .setValue(JarBlock.FILL, Math.min(STEPS, shown))
                .setValue(JarBlock.LABELLED, label != null);
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
        holding = named(tag, "aspect");
        label = named(tag, "label");
        braced = tag.getBoolean("braced");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("amount", amount);
        if (holding != null) {
            tag.putString("aspect", holding.value().id().toString());
        }
        if (label != null) {
            tag.putString("label", label.value().id().toString());
        }
        if (braced) {
            tag.putBoolean("braced", true);
        }
    }

    /** The colour of what is standing in it is drawn from this, so it has to reach the client on its own. */
    /** An aspect written down under some name in the tag, if it is there and still exists. */
    @SuppressWarnings("unchecked")
    private static Holder<Aspect> named(CompoundTag tag, String key) {
        return tag.contains(key)
                ? ModAspects.REGISTRY.getHolder(ResourceLocation.parse(tag.getString(key)))
                        .map(found -> (Holder<Aspect>) found).orElse(null)
                : null;
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
