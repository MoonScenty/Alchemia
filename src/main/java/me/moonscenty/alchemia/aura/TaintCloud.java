package me.moonscenty.alchemia.aura;

import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import org.joml.Vector3f;

/**
 * A cloud of flux hanging over the land, raining goo.
 * <p>
 * It has no body: it is a spot in the air that lasts a minute or so, drops a puddle now and then on the ground
 * beneath, and shows itself only as a haze and a purple drizzle. Each puddle costs the chunk a point of flux, and
 * when the chunk cannot pay the cloud thins out sooner.
 */
public class TaintCloud extends Entity {
    /** How wide the drizzle falls, and how far a puddle may land from the middle. */
    private static final int SPREAD = 16;
    /** How often, per tick, it drops a puddle. */
    private static final int POOL_CHANCE = 20;
    /** How much sooner it goes when the land has no flux to give. */
    private static final int THINS_BY = 20;

    private static final DustParticleOptions DRIZZLE = new DustParticleOptions(new Vector3f(0.55F, 0.25F, 0.7F), 0.8F);
    private static final DustParticleOptions HAZE = new DustParticleOptions(new Vector3f(0.35F, 0.15F, 0.45F), 2.0F);

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
            drizzle();
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

    /** Drops a puddle somewhere on the ground under the cloud. */
    private void rain() {
        int x = getBlockX() + random.nextInt(SPREAD) - random.nextInt(SPREAD);
        int z = getBlockZ() + random.nextInt(SPREAD) - random.nextInt(SPREAD);
        BlockPos onto = new BlockPos(x, level().getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
        if (onto.getY() <= getBlockY()) {
            rain(onto);
        }
    }

    /**
     * Drops a puddle on a spot, paid for by the land it lands on.
     *
     * @return whether a puddle was left
     */
    public boolean rain(BlockPos onto) {
        BlockState ground = level().getBlockState(onto.below());
        BlockState there = level().getBlockState(onto);
        boolean dry = !ground.getFluidState().is(Fluids.WATER) && !ground.getFluidState().is(Fluids.LAVA);
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

    private void drizzle() {
        for (int i = 0; i < 3; i++) {
            double x = getX() + random.nextInt(SPREAD) - random.nextInt(SPREAD) + random.nextDouble();
            double z = getZ() + random.nextInt(SPREAD) - random.nextInt(SPREAD) + random.nextDouble();
            level().addParticle(DRIZZLE, x, getY() - random.nextInt(4), z, 0.0, -0.4, 0.0);
        }
        double x = getX() + random.nextInt(SPREAD) - random.nextInt(SPREAD) + random.nextDouble();
        double z = getZ() + random.nextInt(SPREAD) - random.nextInt(SPREAD) + random.nextDouble();
        level().addParticle(HAZE, x, getY() + random.nextDouble() * 2, z, 0.0, 0.0, 0.0);
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
