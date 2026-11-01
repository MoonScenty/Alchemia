package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class WaterJugBlockEntity extends BlockEntity {
    // original's values
    public static final int FILL = 1000;
    private static final int POURS = 25;
    private static final int EVERY = 5;
    private static final int ACROSS = 5;
    private static final int HIGH = 3;

    private int charge;
    private int looked;
    private int ticks;
    private final List<Integer> pouringInto = new ArrayList<>();

    public WaterJugBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATER_JUG.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaterJugBlockEntity jug) {
        if (++jug.ticks % EVERY != 0 || !(level instanceof ServerLevel served)) {
            return;
        }
        jug.look(served);
        jug.pour(served);
        if (jug.charge <= 0 && AuraHandler.drain(served, pos, ModAspects.WATER, 1)) {
            jug.charge += FILL;
            jug.setChanged();
        }
    }

    private void look(ServerLevel level) {
        looked = (looked + 1) % (ACROSS * ACROSS * HIGH);
        if (!pouringInto.contains(looked) && handler(level, looked) != null) {
            pouringInto.add(looked);
            setChanged();
        }
    }

    private void pour(ServerLevel level) {
        for (int i = 0; i < pouringInto.size() && charge > 0; i++) {
            int place = pouringInto.get(i);
            IFluidHandler into = handler(level, place);
            if (into == null) {
                pouringInto.remove(i--);
                setChanged();
                continue;
            }
            int poured = into.fill(new FluidStack(Fluids.WATER, Math.min(POURS, charge)),
                    IFluidHandler.FluidAction.EXECUTE);
            if (poured > 0) {
                charge -= poured;
                setChanged();
                BlockPos at = placeOf(place);
                level.sendParticles(ParticleTypes.SPLASH, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5,
                        4, 0.2, 0.0, 0.2, 0.0);
                return;
            }
        }
    }

    private IFluidHandler handler(ServerLevel level, int place) {
        BlockPos at = placeOf(place);
        return at.equals(worldPosition) ? null : level.getCapability(Capabilities.FluidHandler.BLOCK, at, Direction.UP);
    }

    private BlockPos placeOf(int place) {
        int x = place / ACROSS % ACROSS;
        int y = place / ACROSS / ACROSS % HIGH;
        int z = place % ACROSS;
        return worldPosition.offset(x - ACROSS / 2, y - HIGH / 2, z - ACROSS / 2);
    }

    // may go negative; the deficit is refilled from the aura first
    public void drawn(int amount) {
        charge -= amount;
        setChanged();
    }

    public int charge() {
        return charge;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        charge = tag.getInt("charge");
        pouringInto.clear();
        for (int place : tag.getIntArray("pouring_into")) {
            pouringInto.add(place);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("charge", charge);
        tag.put("pouring_into", new IntArrayTag(pouringInto));
    }
}
