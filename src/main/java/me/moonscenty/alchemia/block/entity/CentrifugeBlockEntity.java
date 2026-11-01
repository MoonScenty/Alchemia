package me.moonscenty.alchemia.block.entity;

import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An essentia centrifuge: takes one point of a compound essentia in from below, spins it, and gives one of the two
 * things it was made of back out of the top.
 * <p>
 * One of the two, chosen at random, not both: half of what goes in is lost in the spinning, as in the original. It
 * holds one point coming in and one going out, takes nothing while either is full, and refuses the six primals,
 * which have nothing to come apart into. It never pushes what it has made; something above has to draw it off.
 * A redstone signal stops it.
 */
public class CentrifugeBlockEntity extends SyncedBlockEntity implements EssentiaHolder, Spills {
    /** How long a point spins before it comes apart, and how often an empty one looks below for another. */
    private static final int SPINS_FOR = 39;
    private static final int LOOKS_EVERY = 5;
    /** How fast the rotor gets up to speed and runs down again, and its top speed, in degrees a tick. */
    private static final float SPINS_UP = 2.0F;
    private static final float RUNS_DOWN = 0.5F;
    private static final float TOP_SPEED = 20.0F;
    /** How far the rotor goes round between one heave of the pump and the next, in degrees. */
    private static final float HALF_TURN = 180.0F;

    @Nullable
    private Holder<Aspect> in;
    @Nullable
    private Holder<Aspect> out;
    private int spinning;
    private int counter;

    /** The rotor, client side only. */
    private float rotation;
    private float speed;

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity centrifuge) {
        if (level.hasNeighborSignal(pos)) {
            return;
        }
        if (centrifuge.out == null && centrifuge.in == null && ++centrifuge.counter % LOOKS_EVERY == 0) {
            centrifuge.drawFromBelow(level, pos);
        }
        if (centrifuge.spinning > 0) {
            centrifuge.spinning--;
        }
        if (centrifuge.out == null && centrifuge.in != null && centrifuge.spinning == 0) {
            List<Holder<Aspect>> parts = centrifuge.in.value().components().orElse(List.of());
            centrifuge.out = parts.isEmpty() ? null : parts.get(level.random.nextInt(parts.size()));
            centrifuge.in = null;
            centrifuge.changed();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity centrifuge) {
        boolean powered = level.hasNeighborSignal(pos);
        if (centrifuge.in != null && !powered && centrifuge.speed < TOP_SPEED) {
            centrifuge.speed += SPINS_UP;
        }
        if ((centrifuge.in == null || powered) && centrifuge.speed > 0.0F) {
            centrifuge.speed = Math.max(0.0F, centrifuge.speed - RUNS_DOWN);
        }
        float before = centrifuge.rotation;
        centrifuge.rotation += centrifuge.speed;
        // the original's pump, heaving once every half turn for as long as the rotor goes round
        if (centrifuge.speed > 0.0F && (int) (before / HALF_TURN) != (int) (centrifuge.rotation / HALF_TURN)) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.PUMP.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    /**
     * Takes one point straight out of whatever vessel it stands on, as the original drew on whatever was below it.
     * A pipe below fills it on its own; this is for a jar set directly underneath.
     */
    private void drawFromBelow(Level level, BlockPos pos) {
        Holder<Aspect> drawn = EssentiaHolder.drawFrom(level, pos, Direction.DOWN, this::wants);
        if (drawn != null) {
            accept(drawn);
        }
    }

    /** How far round the rotor is, for drawing. */
    public float rotation(float partial) {
        return rotation + speed * partial;
    }

    // --- essentia ----------------------------------------------------------

    @Override
    public AspectList held() {
        return out == null ? AspectList.EMPTY : AspectList.of(out, 1);
    }

    @Override
    public boolean wants(Holder<Aspect> aspect) {
        return in == null && !aspect.value().isPrimal() && level != null
                && !level.hasNeighborSignal(worldPosition);
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        if (!wants(aspect)) {
            return false;
        }
        in = aspect;
        spinning = SPINS_FOR;
        changed();
        return true;
    }

    @Override
    public boolean release(Holder<Aspect> aspect) {
        if (out == null || out.value() != aspect.value()) {
            return false;
        }
        out = null;
        changed();
        return true;
    }

    /** Pipes meet it only above and below. */
    @Override
    public boolean reachableFrom(Direction side) {
        return side.getAxis() == Direction.Axis.Y;
    }

    @Override
    public boolean takesFrom(Direction side) {
        return side == Direction.DOWN;
    }

    @Override
    public boolean givesTo(Direction side) {
        return side == Direction.UP;
    }

    /** What a broken one lets go: what it had split goes into the air as flux. What was still spinning is lost. */
    @Override
    public void spill() {
        if (level != null && !level.isClientSide && out != null) {
            AuraHandler.add(level, worldPosition, ModAspects.FLUX, 1);
        }
        out = null;
        in = null;
    }

    // --- saving ------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        in = tag.contains("aspectIn") ? ModAspects.byId(tag.getString("aspectIn")) : null;
        out = tag.contains("aspectOut") ? ModAspects.byId(tag.getString("aspectOut")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (in != null) {
            tag.putString("aspectIn", in.value().id().toString());
        }
        if (out != null) {
            tag.putString("aspectOut", out.value().id().toString());
        }
    }
}
