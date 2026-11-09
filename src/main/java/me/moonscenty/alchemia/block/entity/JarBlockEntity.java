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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class JarBlockEntity extends SyncedBlockEntity implements EssentiaHolder {
    public static final int CAPACITY = 64;
    public static final int STEPS = 4;
    // original's odds
    public static final int SPILLS_ONE_IN = 100;

    private Holder<Aspect> holding;
    private int amount;
    private Holder<Aspect> label;
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

    // a labelled jar keeps its aspect even when empty
    public void label(Holder<Aspect> aspect) {
        label = aspect;
        if (aspect != null) {
            holding = aspect;
        } else if (amount == 0) {
            holding = null;
        }
        changed();
    }

    public void pourOut() {
        if (level instanceof ServerLevel served && amount > 0) {
            AuraHandler.add(served, worldPosition, ModAspects.FLUX, amount);
        }
        amount = 0;
        if (label == null) {
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

    public boolean voids() {
        return getBlockState().getBlock() instanceof me.moonscenty.alchemia.block.VoidJarBlock;
    }

    @Override
    public boolean wants(Holder<Aspect> aspect) {
        if (amount >= CAPACITY && !voids()) {
            return false;
        }
        return label != null ? label.value() == aspect.value() : holding == null || holding.value() == aspect.value();
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        if (!wants(aspect)) {
            return false;
        }
        if (amount >= CAPACITY) {
            if (level instanceof ServerLevel served && served.random.nextInt(SPILLS_ONE_IN) == 0) {
                AuraHandler.add(served, worldPosition, ModAspects.FLUX, 1);
            }
            return true;
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
        if (amount == 0 && label == null) {
            holding = null;
        }
        changed();
        return true;
    }

    @Override
    public boolean reachableFrom(Direction side) {
        return side == Direction.UP;
    }

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

    public int colour() {
        return holding == null ? 0xFFFFFF : holding.value().color();
    }

    @Override
    protected void changed() {
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
        holding = tag.contains("aspect") ? ModAspects.byId(tag.getString("aspect")) : null;
        label = tag.contains("label") ? ModAspects.byId(tag.getString("label")) : null;
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
}
