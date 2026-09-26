package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchChargerBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A frame of four posts with a crystal hanging between them, meant to stand on top of an arcane workbench.
 * <p>
 * It only ever fills the wand on the bench below it. That is the whole of it: it does nothing on its own and
 * nothing for a wand carried past, which is what makes a workbench somewhere worth coming back to.
 */
public class ArcaneWorkbenchChargerBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneWorkbenchChargerBlock> CODEC =
            simpleCodec(ArcaneWorkbenchChargerBlock::new);

    // the four posts stand at the corners and lean in to meet above the middle, where the crystal hangs
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 5, 9, 5),
            Block.box(11, 0, 0, 16, 9, 5),
            Block.box(0, 0, 11, 5, 9, 16),
            Block.box(11, 0, 11, 16, 9, 16),
            Block.box(3, 9, 3, 13, 16, 13));

    public ArcaneWorkbenchChargerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ArcaneWorkbenchChargerBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneWorkbenchChargerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.ARCANE_WORKBENCH_CHARGER.get(),
                        ArcaneWorkbenchChargerBlockEntity::tick);
    }
}
