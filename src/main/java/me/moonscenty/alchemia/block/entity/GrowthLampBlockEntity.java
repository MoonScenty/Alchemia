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

public class GrowthLampBlockEntity extends BlockEntity implements EssentiaHolder {
    // original's values
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

    // grass is skipped: always bonemealable, never finished
    private static boolean growing(ServerLevel level, BlockPos at, BlockState there) {
        if (there.is(Blocks.GRASS_BLOCK)) {
            return false;
        }
        // original: a grown stem still has fruit to set; canes and cacti are tended at the root
        if (there.getBlock() instanceof net.minecraft.world.level.block.StemBlock) {
            return true;
        }
        if (there.is(Blocks.NETHER_WART)) {
            return there.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE)
                    < net.minecraft.world.level.block.NetherWartBlock.MAX_AGE;
        }
        if (there.getBlock() instanceof BonemealableBlock plant) {
            return plant.isValidBonemealTarget(level, at, there);
        }
        return (there.is(Blocks.SUGAR_CANE) || there.is(Blocks.CACTUS)) && !level.getBlockState(at.below()).is(there.getBlock());
    }

    @Override
    public AspectList held() {
        return reserve ? AspectList.of(ModAspects.PLANT, 1) : AspectList.EMPTY;
    }

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

    @Override
    public boolean reachableFrom(Direction side) {
        return side == getBlockState().getValue(LampBlock.FACING);
    }

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
