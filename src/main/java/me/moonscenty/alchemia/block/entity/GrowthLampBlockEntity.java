package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.LampBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A lamp of growth at work: hurrying along whatever is growing within six blocks of it.
 *
 * <p>It burns herba, one point at a time, drawn through the face it hangs from. A point is a hundred nudges, and a
 * nudge is one more turn of growing for one plant that has not finished. It works through the ground under it a
 * column at a time, top to bottom, so it does not keep on at one plant while its neighbours wait.
 *
 * <p>It keeps one point in hand besides the one it is burning, so it does not go dark between deliveries.
 */
public class GrowthLampBlockEntity extends BlockEntity implements EssentiaHolder {
    /** How many nudges a point of herba is worth, and how far round it reaches. The original's numbers. */
    private static final int NUDGES = 100;
    private static final int REACHES = 6;

    private int charges;
    private boolean reserve;
    private final List<BlockPos> columns = new ArrayList<>();

    public GrowthLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GROWTH_LAMP.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrowthLampBlockEntity lamp) {
        if (lamp.charges <= 0 && lamp.reserve) {
            lamp.charges = NUDGES;
            lamp.reserve = false;
            lamp.setChanged();
        }
        boolean powered = level.hasNeighborSignal(pos);
        boolean lit = lamp.charges > 0 && !powered;
        if (lit != state.getValue(LampBlock.ENABLED)) {
            level.setBlock(pos, state.setValue(LampBlock.ENABLED, lit), Block.UPDATE_ALL);
        }
        if (lit && level instanceof ServerLevel served) {
            lamp.nudge(served);
        }
    }

    /**
     * One more turn of growing for the next unfinished plant, looked for one column at a time.
     * <p>
     * A turn of growing is the game's own: the plant is asked to grow the way it is asked a few times a minute
     * anyway, and takes it or not as it would then. The lamp only asks far more often.
     */
    private void nudge(ServerLevel level) {
        if (columns.isEmpty()) {
            for (int x = -REACHES; x <= REACHES; x++) {
                for (int z = -REACHES; z <= REACHES; z++) {
                    columns.add(worldPosition.offset(x, REACHES, z));
                }
            }
            Collections.shuffle(columns, new java.util.Random(level.random.nextLong()));
        }
        BlockPos top = columns.remove(0);
        for (int y = top.getY(); y >= worldPosition.getY() - REACHES; y--) {
            BlockPos at = new BlockPos(top.getX(), y, top.getZ());
            BlockState there = level.getBlockState(at);
            if (at.distSqr(worldPosition) < REACHES * REACHES && growing(level, at, there)) {
                charges--;
                setChanged();
                there.randomTick(level, at, level.random);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.getX() + 0.5, at.getY() + 0.5,
                        at.getZ() + 0.5, 1, 0.3, 0.3, 0.3, 0.0);
                return;
            }
        }
    }

    /**
     * Whether this is something still growing.
     * <p>
     * Anything bone meal would work on that has not finished, and the two that grow without it -- cane and cactus
     * -- while there is room above them. Grass is passed over: it is always willing, and always finished.
     */
    private static boolean growing(ServerLevel level, BlockPos at, BlockState there) {
        if (there.is(Blocks.GRASS_BLOCK)) {
            return false;
        }
        if (there.getBlock() instanceof BonemealableBlock plant) {
            return plant.isValidBonemealTarget(level, at, there);
        }
        return (there.is(Blocks.SUGAR_CANE) || there.is(Blocks.CACTUS)) && level.isEmptyBlock(at.above());
    }

    // --- being fed ----------------------------------------------------------------------------------------------

    @Override
    public AspectList held() {
        return reserve ? AspectList.of(ModAspects.PLANT, 1) : AspectList.EMPTY;
    }

    /** One point in hand at a time, and only herba. */
    @Override
    public boolean wants(Holder<Aspect> aspect) {
        return !reserve && aspect.value() == ModAspects.PLANT.value();
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        if (!wants(aspect)) {
            return false;
        }
        reserve = true;
        setChanged();
        return true;
    }

    /** Fed only through the face it hangs from. */
    @Override
    public boolean reachableFrom(Direction side) {
        return side == getBlockState().getValue(LampBlock.FACING);
    }

    /** How many nudges it has left in the point it is burning. */
    public int charges() {
        return charges;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        charges = tag.getInt("charges");
        reserve = tag.getBoolean("reserve");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("charges", charges);
        tag.putBoolean("reserve", reserve);
    }
}
