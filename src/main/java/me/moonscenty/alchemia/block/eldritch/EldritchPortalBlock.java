package me.moonscenty.alchemia.block.eldritch;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The way to the Outer Lands, above an eldritch altar: shut and unseen until the altar's eyes are all in and a wand
 * has opened it. Nothing breaks it and nothing bumps into it.
 */
public class EldritchPortalBlock extends BaseEntityBlock {
    public static final MapCodec<EldritchPortalBlock> CODEC = simpleCodec(EldritchPortalBlock::new);

    public EldritchPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<EldritchPortalBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EldritchPortalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ELDRITCH_PORTAL.get(), EldritchPortalBlockEntity::tick);
    }
}
