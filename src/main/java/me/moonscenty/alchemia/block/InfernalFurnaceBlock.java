package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.InfernalFurnaceBlockEntity;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The molten heart of an infernal furnace, where the lava was. Its model is the whole furnace, three blocks every
 * way; the shell round it is placeholder blocks that draw nothing.
 * <p>
 * It is made with a wand out of a three-by-three-by-three of nether brick at the corners, obsidian on the edges and
 * the floor, still lava in the middle, air over it, and iron bars in place of one of the four middle-layer edges.
 * The bars become the mouth, and the heart faces away from it. Break any of it and the rest goes back to what it
 * was, bars and lava and all.
 * <p>
 * Things dropped through the top land on the lava and are taken in; anything living that touches it is burned.
 */
public class InfernalFurnaceBlock extends BaseEntityBlock {
    public static final MapCodec<InfernalFurnaceBlock> CODEC = simpleCodec(InfernalFurnaceBlock::new);
    /** Away from the mouth. */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** The lava's surface, which is what things land on. */
    private static final VoxelShape SURFACE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    public static final java.util.function.Supplier<AspectList> COSTS =
            () -> AspectList.of(ModAspects.FIRE, 50).add(ModAspects.EARTH, 50);
    /** Set while the furnace is taking itself apart, so that the parts do not each try to take it apart again. */
    private static boolean undoing;

    public InfernalFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<InfernalFurnaceBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SURFACE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfernalFurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.INFERNAL_FURNACE.get(), InfernalFurnaceBlockEntity::tick);
    }

    /** Things that land on it are drawn to the middle and taken in; the living are burned. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        Vec3 moving = entity.getDeltaMovement();
        double dx = entity.getX() < pos.getX() + 0.3 ? 1.0E-4 : entity.getX() > pos.getX() + 0.7 ? -1.0E-4 : 0.0;
        double dz = entity.getZ() < pos.getZ() + 0.3 ? 1.0E-4 : entity.getZ() > pos.getZ() + 0.7 ? -1.0E-4 : 0.0;
        entity.setDeltaMovement(moving.add(dx, 0.0, dz));
        if (entity instanceof ItemEntity item) {
            entity.setDeltaMovement(entity.getDeltaMovement().x, 0.025, entity.getDeltaMovement().z);
            if (!level.isClientSide && item.onGround()
                    && level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity furnace
                    && furnace.take(item.getItem())) {
                item.discard();
            }
        } else if (entity instanceof LivingEntity living && !living.fireImmune()) {
            living.hurt(level.damageSources().lava(), 3.0F);
            living.igniteForSeconds(10);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            if (level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity furnace) {
                Containers.dropContents(level, pos, furnace.contents());
            }
            undo(level, pos, state, replacement.is(Blocks.LAVA));
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    /**
     * Takes the furnace apart round its heart: the shell back to nether brick and obsidian, bars back in the mouth if
     * it is open, and the heart back to lava unless it is already being made so.
     */
    public static void undo(Level level, BlockPos heart, BlockState state, boolean heartDone) {
        if (undoing || level.isClientSide) {
            return;
        }
        undoing = true;
        try {
            for (BlockPos at : BlockPos.betweenClosed(heart.offset(-1, -1, -1), heart.offset(1, 1, 1))) {
                BlockState there = level.getBlockState(at);
                if (there.getBlock() instanceof FurnaceShellBlock) {
                    level.setBlock(at, there.getValue(FurnaceShellBlock.OBSIDIAN)
                            ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.NETHER_BRICKS.defaultBlockState(),
                            Block.UPDATE_ALL);
                }
            }
            BlockPos mouth = heart.relative(state.getValue(FACING).getOpposite());
            if (level.isEmptyBlock(mouth)) {
                level.setBlock(mouth, Blocks.IRON_BARS.defaultBlockState(), Block.UPDATE_ALL);
            }
            if (!heartDone && level.getBlockState(heart).is(ModBlocks.INFERNAL_FURNACE.get())) {
                level.setBlock(heart, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
            }
        } finally {
            undoing = false;
        }
    }

    /**
     * Makes one out of the blocks round what was clicked, if they are laid out right, the wand can pay, and its
     * bearer has worked out how. Any of the solid blocks of it may be clicked.
     */
    public static boolean form(Level level, BlockPos clicked, Player player, ItemStack wand) {
        if (!PlayerKnowledge.of(player).hasResearch(Alchemia.id("infernal_furnace"))) {
            return false;
        }
        for (int dx = -2; dx <= 0; dx++) {
            for (int dy = -2; dy <= 0; dy++) {
                for (int dz = -2; dz <= 0; dz++) {
                    BlockPos base = clicked.offset(dx, dy, dz);
                    if (!fits(level, base)) {
                        continue;
                    }
                    VisHolder holder = VisHolder.of(wand);
                    AspectList cost = COSTS.get();
                    if (holder == null || !holder.holds(wand, cost, player, VisHolder.Spending.CRAFTING)) {
                        return false;
                    }
                    if (!level.isClientSide) {
                        holder.take(wand, cost, player, VisHolder.Spending.CRAFTING);
                        build(level, base);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /** Whether the three-by-three-by-three with this corner is laid out as the original's blueprint, bars and all. */
    public static boolean fits(Level level, BlockPos base) {
        boolean bars = false;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos at = base.offset(x, y, z);
                    BlockState there = level.getBlockState(at);
                    boolean corner = x != 1 && z != 1;
                    boolean centre = x == 1 && z == 1;
                    boolean ok;
                    if (corner) {
                        ok = there.is(Blocks.NETHER_BRICKS);
                    } else if (centre) {
                        ok = switch (y) {
                            case 0 -> there.is(Blocks.OBSIDIAN);
                            case 1 -> there.getFluidState().is(Fluids.LAVA) && there.getFluidState().isSource();
                            default -> there.isAir();
                        };
                    } else {
                        ok = there.is(Blocks.OBSIDIAN);
                        if (!ok && y == 1 && !bars && there.is(Blocks.IRON_BARS)) {
                            bars = true;
                            ok = true;
                        }
                    }
                    if (!ok) {
                        return false;
                    }
                }
            }
        }
        return bars;
    }

    /** Builds the furnace on a corner already known to fit, without asking anyone to pay for it. */
    public static void build(Level level, BlockPos base) {
        BlockPos heart = base.offset(1, 1, 1);
        Direction mouth = Direction.NORTH;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(heart.relative(side)).is(Blocks.IRON_BARS)) {
                mouth = side;
                break;
            }
        }
        undoing = true;
        try {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    for (int z = 0; z < 3; z++) {
                        BlockPos at = base.offset(x, y, z);
                        BlockState there = level.getBlockState(at);
                        if (there.is(Blocks.IRON_BARS)) {
                            level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        } else if (x != 1 && z != 1) {
                            level.setBlock(at, ModBlocks.FURNACE_SHELL.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else if (!(x == 1 && z == 1 && y > 0)) {
                            level.setBlock(at, ModBlocks.FURNACE_SHELL.get().defaultBlockState()
                                    .setValue(FurnaceShellBlock.OBSIDIAN, true), Block.UPDATE_ALL);
                        }
                    }
                }
            }
            level.setBlock(heart, ModBlocks.INFERNAL_FURNACE.get().defaultBlockState()
                    .setValue(FACING, mouth.getOpposite()), Block.UPDATE_ALL);
        } finally {
            undoing = false;
        }
        level.playSound(null, heart, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
