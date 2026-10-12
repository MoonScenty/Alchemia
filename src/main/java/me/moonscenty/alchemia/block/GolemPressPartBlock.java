package me.moonscenty.alchemia.block;

import java.util.Locale;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * One of the four blocks of a golem press round its heart, standing in for what it was made from: the bars over the
 * piston, the table, the anvil or the cauldron. It draws nothing, since the heart draws the whole press; it opens
 * the press like the heart does, and breaking it takes the press apart and drops what it stood in for.
 */
public class GolemPressPartBlock extends Block {
    public static final MapCodec<GolemPressPartBlock> CODEC = simpleCodec(GolemPressPartBlock::new);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public enum Part implements StringRepresentable {
        BARS,
        ANVIL,
        CAULDRON,
        TABLE;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        /** What it was before the press was made, and is again when the press is taken apart. */
        public BlockState original() {
            return switch (this) {
                case BARS -> Blocks.IRON_BARS.defaultBlockState();
                case ANVIL -> Blocks.ANVIL.defaultBlockState();
                case CAULDRON -> Blocks.CAULDRON.defaultBlockState();
                case TABLE -> ModBlocks.STONE_TABLE.get().defaultBlockState();
            };
        }
    }

    public GolemPressPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, Part.BARS));
    }

    @Override
    protected MapCodec<GolemPressPartBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** Picked in creative, it gives what it stands in for. */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(state.getValue(PART).original().getBlock());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        BlockPos heart = heart(level, pos);
        return heart == null ? InteractionResult.PASS : GolemPressBlock.open(level, heart, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && !GolemPressBlock.undoing()) {
            BlockPos heart = heart(level, pos);
            if (heart != null) {
                GolemPressBlock.undo(level, heart, pos);
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    /** The heart of the press this is part of: within a block of it, and at or below it. */
    private static BlockPos heart(BlockGetter level, BlockPos pos) {
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 0, 1))) {
            if (level.getBlockState(at).is(ModBlocks.GOLEM_PRESS.get())) {
                return at.immutable();
            }
        }
        return null;
    }
}
