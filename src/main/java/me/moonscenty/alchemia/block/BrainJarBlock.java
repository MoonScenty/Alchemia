package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.BrainJarBlockEntity;
import me.moonscenty.alchemia.client.BrainJarEffects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A brain in a jar: a warded jar with a zombie's brain floating in brine, which eats experience and gives some back
 * when shaken.
 * <p>
 * Shaking it is any click at all. It counts as two bookshelves to an enchanting table near it, and a comparator
 * reads how full it is. When it is full it lets off wisps, so that it can be seen to be.
 */
public class BrainJarBlock extends BaseEntityBlock {
    public static final MapCodec<BrainJarBlock> CODEC = simpleCodec(BrainJarBlock::new);
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);

    public BrainJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BrainJarBlock> codec() {
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
        return new BrainJarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BRAIN_JAR.get(), BrainJarBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar) {
            jar.shake(level);
            if (level.isClientSide) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.GLASS_HIT, SoundSource.BLOCKS, 0.2F, 1.0F, false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** A full one lets off a wisp now and then, in the colour of death. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar && jar.xp() >= BrainJarBlockEntity.HOLDS) {
            BrainJarEffects.full(level, pos);
        }
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 2.0F;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar)) {
            return 0;
        }
        return (int) Math.floor(jar.xp() / (float) BrainJarBlockEntity.HOLDS * 14.0F) + (jar.xp() > 0 ? 1 : 0);
    }
}
