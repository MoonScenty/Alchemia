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

/**
 * A jug that is never empty: it draws water out of the air and pours it into whatever round it will take it.
 *
 * <p>It holds a bucket's worth at a time and fills itself again from the aura, a point of aqua for every bucket.
 * Anything within two blocks to the sides and one up or down that holds liquid -- a crucible, a cauldron, a tank
 * of somebody else's -- is found in time and topped up, a quarter of a bucket every quarter of a second. And
 * anybody holding an empty bucket or bottle can fill it from the jug by hand.
 *
 * <p>It finds what is round it slowly, one place every quarter of a second, and remembers what it found.
 */
public class WaterJugBlockEntity extends BlockEntity {
    /** How much a fill from the air brings, what it pours at a time, and how often it works. The original's. */
    public static final int FILL = 1000;
    private static final int POURS = 25;
    private static final int EVERY = 5;
    /** How far it looks: five across, three high, five deep, about the jug. */
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

    /** One more place round it, to see whether it holds liquid. */
    private void look(ServerLevel level) {
        looked = (looked + 1) % (ACROSS * ACROSS * HIGH);
        if (!pouringInto.contains(looked) && handler(level, looked) != null) {
            pouringInto.add(looked);
            setChanged();
        }
    }

    /** A quarter of a bucket into the first of what it knows of that will take some. */
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

    /** What holds liquid at one of the places round it, poured into from above. Nothing for the jug itself. */
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

    /**
     * Water taken out by hand. It may go below empty: a jug is not refused because it was low, it only owes the
     * air the more before it fills again.
     */
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
