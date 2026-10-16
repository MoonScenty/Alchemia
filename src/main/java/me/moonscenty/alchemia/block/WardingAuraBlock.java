package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A warding aura, the original's barrier block: the invisible wall a barrier stone raises over itself, two blocks
 * high. Players pass through it and so does everything that is not a creature; any other creature finds it solid
 * and will not path through it, unless a redstone signal holds the stone off. It cannot be seen or struck, anything
 * may be built into it, and it goes when nothing holds it up from below.
 */
public class WardingAuraBlock extends Block {
    public WardingAuraBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        if (context instanceof EntityCollisionContext held && held.getEntity() instanceof LivingEntity living
                && !(living instanceof Player) && BarrierStoneBlock.warding(level, pos)) {
            return Shapes.block();
        }
        return Shapes.empty();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, Mob mob) {
        return BarrierStoneBlock.warding(level, pos) ? PathType.BLOCKED : PathType.OPEN;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from,
            boolean moving) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(ModBlocks.BARRIER_STONE.get()) && !below.is(this)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }
}
