package me.moonscenty.alchemia.block.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.BellowsBlock;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

// the sink end pulls rather than the source pushing, so a run fills whichever end still has room
public class TubeBlockEntity extends SyncedBlockEntity {
    private static final int PULLS_EVERY = 10;
    private static final int REACH = 96;
    // original's odds
    private static final int CREAKS_ONE_IN = 100;

    // shut sides, one bit per Direction ordinal
    public static final ModelProperty<Integer> SHUT = new ModelProperty<>();

    private int counter;
    private int shut;
    private long turns;

    public TubeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TUBE.get(), pos, state);
    }

    protected TubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TubeBlockEntity tube) {
        if (tube.counter++ < PULLS_EVERY) {
            return;
        }
        tube.counter = 0;
        tube.turns++;
        tube.pull(level, pos, state);
    }

    public boolean shut(Direction side) {
        return (shut & 1 << side.ordinal()) != 0;
    }

    public void shut(Direction side, boolean closed) {
        shut = closed ? shut | 1 << side.ordinal() : shut & ~(1 << side.ordinal());
        changed();
    }

    protected boolean drawsThrough(Direction side) {
        return true;
    }

    protected boolean blownThrough(Direction side) {
        return false;
    }

    private void pull(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof TubeBlock)) {
            return;
        }
        // a buffer fills itself before its neighbours
        if (this instanceof EssentiaHolder self && draw(level, pos, self, null, this::drawsThrough)) {
            // each bellows adds a pull (original)
            int bellows = BellowsBlock.blowingOn(level, pos, Direction.values());
            for (int more = 0; more < bellows; more++) {
                draw(level, pos, self, null, this::blownThrough);
            }
            return;
        }
        for (Direction side : Direction.values()) {
            if (shut(side) || state.getValue(TubeBlock.SIDES.get(side)) != TubeBlock.Link.BLOCK) {
                continue;
            }
            if (level.getBlockEntity(pos.relative(side)) instanceof EssentiaHolder sink
                    && sink.reachableFrom(side.getOpposite())
                    && sink.takesFrom(side.getOpposite())
                    && draw(level, pos, sink, side, any -> true)) {
                return;
            }
        }
    }

    private boolean draw(Level level, BlockPos pos, EssentiaHolder sink, Direction into,
            Predicate<Direction> through) {
        for (Route route : upstreamOf(level, pos, into)) {
            if (route.from() == sink || !through.test(route.via())) {
                continue;
            }
            // buffers never feed each other, or two would pass a point back and forth forever
            if (sink instanceof BufferTubeBlockEntity && route.from() instanceof BufferTubeBlockEntity) {
                continue;
            }
            // a void jar does not drain other jars (original's suction was too weak)
            if (sink instanceof JarBlockEntity jar && jar.voids() && route.from() instanceof JarBlockEntity) {
                continue;
            }
            if (turns % (route.waits() + 1) != 0) {
                continue;
            }
            for (Holder<Aspect> aspect : route.from().held().sortedByAmount()) {
                if (route.only() != null && route.only().value() != aspect.value()) {
                    continue;
                }
                if (!sink.wants(aspect) || !route.from().release(aspect)) {
                    continue;
                }
                if (sink.accept(aspect)) {
                    if (level.random.nextInt(CREAKS_ONE_IN) == 0) {
                        level.playSound(null, pos, ModSounds.CREAK.get(), SoundSource.BLOCKS, 1.0F,
                                1.3F + level.random.nextFloat() * 0.2F);
                    }
                    return true;
                }
                // the sink refused after all; give it back
                route.from().accept(aspect);
            }
        }
        return false;
    }

    private record Step(BlockPos at, Direction out, Holder<Aspect> only, int waits, Direction via) {
    }

    private record Seen(BlockPos at, Direction out) {
    }

    private record Route(EssentiaHolder from, Holder<Aspect> only, int waits, Direction via) {
    }

    // walked every pull, not cached: runs are short and a cache would need invalidating on every edit
    private static List<Route> upstreamOf(Level level, BlockPos start, Direction out) {
        Set<Seen> seen = new HashSet<>();
        Deque<Step> todo = new ArrayDeque<>();
        List<Route> found = new ArrayList<>();
        seen.add(new Seen(start, out));
        todo.add(new Step(start, out, null, 0, null));

        while (!todo.isEmpty() && seen.size() <= REACH) {
            Step step = todo.removeFirst();
            BlockState state = level.getBlockState(step.at());
            if (!(state.getBlock() instanceof TubeBlock tube)) {
                continue;
            }

            Holder<Aspect> only = step.only();
            Optional<Holder<Aspect>> insists = tube.insistsOn(level, step.at());
            if (insists.isPresent()) {
                // two filters set to different aspects block the run
                if (only != null && only.value() != insists.get().value()) {
                    continue;
                }
                only = insists.get();
            }
            int waits = step.waits() + tube.holdsUp();

            // a buffer along the run is itself a source
            if (!step.at().equals(start) && level.getBlockEntity(step.at()) instanceof EssentiaHolder holder) {
                found.add(new Route(holder, only, waits, step.via()));
            }

            TubeBlockEntity here = level.getBlockEntity(step.at()) instanceof TubeBlockEntity at ? at : null;
            for (Direction side : Direction.values()) {
                if (side == step.out() || !tube.passes(state, side, step.out())
                        || here != null && here.shut(side)) {
                    continue;
                }
                BlockPos next = step.at().relative(side);
                Direction via = step.via() != null ? step.via() : side;
                switch (state.getValue(TubeBlock.SIDES.get(side))) {
                    case TUBE -> {
                        boolean shutThere = level.getBlockEntity(next) instanceof TubeBlockEntity there
                                && there.shut(side.getOpposite());
                        if (!shutThere && seen.add(new Seen(next, side.getOpposite()))) {
                            todo.add(new Step(next, side.getOpposite(), only, waits, via));
                        }
                    }
                    case BLOCK -> {
                        if (level.getBlockEntity(next) instanceof EssentiaHolder holder
                                && holder.reachableFrom(side.getOpposite())
                                && holder.givesTo(side.getOpposite())) {
                            found.add(new Route(holder, only, waits, via));
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        return found;
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(SHUT, shut).build();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        shut = tag.getInt("shut");
        // shut sides live in model data, which must be requested again before a re-render
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (shut != 0) {
            tag.putInt("shut", shut);
        }
    }
}
