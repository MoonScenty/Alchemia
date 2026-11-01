package me.moonscenty.alchemia.block.taint;

import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluxGooBlock extends SettlingLayerBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 7);
    public static final int FULL = 7;

    private static final int FLOW_EVERY = 5;
    private static final int DRY_CHANCE = 4;

    private static final VoxelShape[] SHAPES = new VoxelShape[FULL + 1];

    static {
        for (int level = 0; level <= FULL; level++) {
            SHAPES[level] = Block.box(0, 0, 0, 16, 2 + level * 2, 16);
        }
    }

    public FluxGooBlock(Properties properties) {
        super(properties, FLOW_EVERY);
    }

    @Override
    protected IntegerProperty depthProperty() {
        return LEVEL;
    }

    @Override
    protected int full() {
        return FULL;
    }

    public static BlockState of(Block goo, int level) {
        return goo.defaultBlockState().setValue(LEVEL, Math.clamp(level, 0, FULL));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (congeal(state, level, pos, random) || random.nextInt(DRY_CHANCE) != 0) {
            return;
        }
        int depth = state.getValue(LEVEL);
        if (depth > 0) {
            level.setBlock(pos, of(this, depth - 1), Block.UPDATE_CLIENTS);
            AuraHandler.add(level, pos, ModAspects.FLUX, 1);
        } else if (random.nextBoolean()) {
            AuraHandler.add(level, pos, ModAspects.FLUX, 1);
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, TaintSpread.fibres(level, pos), Block.UPDATE_ALL);
        }
    }

    // original's depths
    private static boolean congeal(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int depth = state.getValue(LEVEL);
        if (depth < 2 || !level.isEmptyBlock(pos.above()) || random.nextInt(50) != 0) {
            return false;
        }
        level.removeBlock(pos, false);
        var slime = new me.moonscenty.alchemia.entity.AlchemicSlime(level);
        slime.setSize(depth >= 6 ? 2 : 1, true);
        slime.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(slime);
        level.playSound(null, pos, me.moonscenty.alchemia.registry.ModSounds.GORE.get(),
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof me.moonscenty.alchemia.entity.AlchemicSlime slime) {
            int depth = state.getValue(LEVEL);
            if (!level.isClientSide && slime.getSize() < depth && level.random.nextBoolean()) {
                slime.setSize(slime.getSize() + 1, true);
                if (depth > 1) {
                    level.setBlock(pos, of(this, depth - 1), Block.UPDATE_ALL);
                } else {
                    level.removeBlock(pos, false);
                }
            }
            return;
        }
        entity.makeStuckInBlock(state, new Vec3(0.5, 0.35, 0.5));
        if (!level.isClientSide && entity instanceof net.minecraft.world.entity.LivingEntity living) {
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    me.moonscenty.alchemia.player.effect.ModEffects.FLUX_FLU, 600, state.getValue(LEVEL) / 3, true,
                    true));
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LEVEL)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }
}
