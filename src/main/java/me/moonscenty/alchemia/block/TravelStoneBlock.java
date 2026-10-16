package me.moonscenty.alchemia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A paving stone of travel, the original's travel paving stone: arcane brick that puts a spring in the step of
 * whatever walks across it. Anything living on it is quicker (speed II) and jumps higher (jump boost I) for two
 * seconds after, with no swirl of potion about it; now and then a pale mote rises where it trod.
 */
public class TravelStoneBlock extends Block {
    /** How long the spring lasts after stepping off, in ticks. */
    public static final int LASTS = 40;
    /** A paving stone stands a sixteenth short of a full block, the way farmland does; it is walked on as one. */
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);

    public TravelStoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof LivingEntity living) {
            if (level.isClientSide) {
                if (level.random.nextBoolean()) {
                    me.moonscenty.alchemia.client.MagicalForestEffects.travelMote(level, pos.above(), level.random);
                }
            } else {
                hasten(living);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    /** Two seconds of speed II and jump boost I, ambient and without particles. */
    public static void hasten(LivingEntity living) {
        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, LASTS, 1, true, false));
        living.addEffect(new MobEffectInstance(MobEffects.JUMP, LASTS, 0, true, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}
