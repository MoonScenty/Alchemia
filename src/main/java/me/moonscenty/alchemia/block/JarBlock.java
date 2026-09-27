package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A glass jar for keeping one essentia in, with a wooden lid.
 * <p>
 * The state says only how high the liquid stands, since that is all the model needs. What the essentia is lives in
 * the block entity, and the colour is asked for separately when the liquid is drawn.
 */
public class JarBlock extends BaseEntityBlock {
    /** How high the liquid is drawn. Zero is an empty jar. */
    public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, JarBlockEntity.STEPS);

    public static final MapCodec<JarBlock> CODEC = simpleCodec(JarBlock::new);

    // the glass, and the lid on top of it
    private static final VoxelShape SHAPE = net.minecraft.world.phys.shapes.Shapes.or(
            Block.box(3, 0, 3, 13, 13, 13),
            Block.box(5, 13, 5, 11, 14, 11));

    public JarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FILL, 0));
    }

    @Override
    protected MapCodec<JarBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILL);
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
        return new JarBlockEntity(pos, state);
    }

    /** Says what is in it. A jar is read at a glance by its colour, but not by how much is left. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        player.displayClientMessage(jar.holding()
                .map(aspect -> Component.translatable("block.alchemia.jar.holding",
                        aspect.value().displayName(), jar.amount(), JarBlockEntity.CAPACITY))
                .orElse(Component.translatable("block.alchemia.jar.empty"))
                .copy().withStyle(ChatFormatting.GRAY), true);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof JarBlockEntity jar) {
            jar.spill();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
