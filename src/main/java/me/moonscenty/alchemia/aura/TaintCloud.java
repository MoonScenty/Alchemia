package me.moonscenty.alchemia.aura;

import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.client.AuraEffects;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

// Bodiless; each puddle costs the chunk a point of flux, and it thins out sooner when the chunk can't pay.
public class TaintCloud extends Entity {
    private static final int SPREAD = 16;
    // 1 in N per tick
    private static final int POOL_CHANCE = 20;
    private static final int THINS_BY = 20;


    private int lifespan;

    public TaintCloud(EntityType<? extends TaintCloud> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public TaintCloud(Level level, BlockPos at, int lifespan) {
        this(ModEntities.TAINT_CLOUD.get(), level);
        moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
        this.lifespan = lifespan;
    }

    public int lifespan() {
        return lifespan;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            AuraEffects.taintCloud(this);
            return;
        }
        if (lifespan-- < 0) {
            discard();
            return;
        }
        if (random.nextInt(POOL_CHANCE) == 0) {
            rain();
        }
    }

    private void rain() {
        int x = getBlockX() + random.nextInt(SPREAD) - random.nextInt(SPREAD);
        int z = getBlockZ() + random.nextInt(SPREAD) - random.nextInt(SPREAD);
        BlockPos onto = new BlockPos(x, level().getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
        if (onto.getY() <= getBlockY()) {
            rain(onto);
        }
    }

    public boolean rain(BlockPos onto) {
        BlockState ground = level().getBlockState(onto.below());
        BlockState there = level().getBlockState(onto);
        boolean dry = !ground.getFluidState().is(FluidTags.WATER) && !ground.getFluidState().is(FluidTags.LAVA);
        boolean spoiled = ground.is(ModTags.Blocks.TAINT) || there.is(ModTags.Blocks.TAINT) || there.is(ModBlocks.FLUX_GOO.get());
        if (!dry || spoiled || !there.canBeReplaced()) {
            return false;
        }
        if (!AuraHandler.drain(level(), onto, ModAspects.FLUX, 1)) {
            lifespan -= THINS_BY;
            return false;
        }
        level().setBlock(onto, FluxGooBlock.of(ModBlocks.FLUX_GOO.get(), 0), Block.UPDATE_ALL);
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifespan = tag.getInt("lifespan");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("lifespan", lifespan);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
