package me.moonscenty.alchemia.block;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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

    /** Whether a label is stuck on it, which the model draws and the jar takes its orders from. */
    public static final BooleanProperty LABELLED = BooleanProperty.create("labelled");

    /** Which side the label is stuck to: whichever side the person who stuck it on was standing. */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<JarBlock> CODEC = simpleCodec(JarBlock::new);

    // the glass, and the lid on top of it
    private static final VoxelShape SHAPE = net.minecraft.world.phys.shapes.Shapes.or(
            Block.box(3, 0, 3, 13, 13, 13),
            Block.box(5, 13, 5, 11, 14, 11));

    public JarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FILL, 0)
                .setValue(LABELLED, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<JarBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILL, LABELLED, FACING);
    }

    /** Set down facing whoever put it there, so a label stuck on later has a sensible side to start from. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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

    /**
     * Fits a brace, or sticks a label on.
     * <p>
     * A blank label takes its word from whatever the jar is already holding, so labelling a working jar is one
     * click and no thinking. A label already written on can go on an empty jar, or on one holding that same thing;
     * on anything else it would be a lie, so it does not go on at all.
     * <p>
     * Anything else in hand is handed on. A phial is poured by the phial, not by the jar.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(ModItems.JAR_BRACE.get()) && !jar.braced()) {
            if (!level.isClientSide) {
                jar.brace(true);
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5F, 1.4F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(ModItems.JAR_LABEL.get()) && jar.label().isEmpty()) {
            Optional<Holder<Aspect>> word = PhialItem.inside(stack).or(jar::holding);
            // it may say what is in there already, or anything at all when there is nothing in there to contradict
            boolean honest = word.isPresent() && (jar.amount() == 0
                    || jar.holding().orElseThrow().value() == word.get().value());
            if (!honest) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                jar.label(word.get());
                // the label ends up on the side you were standing on, which is the side you will read it from
                level.setBlock(pos, level.getBlockState(pos)
                        .setValue(FACING, player.getDirection().getOpposite()), Block.UPDATE_ALL);
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 0.7F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Says what is in it, or takes the label back off.
     * <p>
     * A jar is read at a glance by its colour, but not by how much is left, so it is worth asking. Crouching with
     * an empty hand peels the label instead: that is the only way to change what a labelled jar will take.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
            return InteractionResult.PASS;
        }
        // a block that only reports what is in it must not swallow the click: Minecraft asks the block with
        // the item first, then the block on its own, and only then the item itself. A jar that answers a phial
        // with a sentence about its contents is a jar the phial can never be used on.
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isSecondaryUseActive() && jar.label().isPresent()) {
            popResource(level, pos, labelFor(jar.label().get()));
            jar.label(null);
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 0.7F, 0.8F);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(jar.holding()
                .map(aspect -> Component.translatable("block.alchemia.jar.holding",
                        aspect.value().displayName(), jar.amount(), JarBlockEntity.CAPACITY))
                .orElse(Component.translatable("block.alchemia.jar.empty"))
                .copy().withStyle(ChatFormatting.GRAY), true);
        return InteractionResult.CONSUME;
    }

    /** A label with an aspect written on it, for handing back. */
    public static ItemStack labelFor(Holder<Aspect> aspect) {
        ItemStack label = new ItemStack(ModItems.JAR_LABEL.get());
        label.set(ModDataComponents.ESSENTIA.get(), aspect);
        return label;
    }

    /**
     * What a broken jar leaves behind: the brace, which comes off, and the jar itself.
     * <p>
     * The essentia and the label go with the jar rather than falling out of it -- the loot table copies them onto
     * the dropped item. The brace is a separate thing that was fitted to the outside of it, so it lands separately.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof JarBlockEntity jar
                && jar.braced()) {
            popResource(level, pos, new ItemStack(ModItems.JAR_BRACE.get()));
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
