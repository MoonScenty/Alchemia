package me.moonscenty.alchemia.block;

import java.util.Locale;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An aura totem, or one of the poles it stands on.
 *
 * <p>A totem moves vis between two rings of chunks round it, a shard at a time: the totem of dispersal from the inner
 * ring out to the outer, the totem of attraction from the outer in. Alone, the inner ring is its own chunk and the
 * outer the eight round that. Up to five poles stacked under it change that -- an inner pole widens the inner ring by
 * a chunk, an outer pole the outer, and each of either quickens it -- and a stabilising pole makes it less likely to
 * spoil what it moves into flux.
 *
 * <p>A pole is worked from the totem above it: what is put into a pole goes into the totem, and clicking a pole opens
 * the totem.
 */
public class AuraTotemBlock extends BaseEntityBlock {
    public static final MapCodec<AuraTotemBlock> CODEC = RecordCodecBuilder.mapCodec(totem -> totem.group(
            Kind.CODEC.fieldOf("kind").forGetter(AuraTotemBlock::kind), propertiesCodec())
            .apply(totem, AuraTotemBlock::new));
    /** The most poles a totem counts under it. The original's. */
    public static final int MOST_POLES = 5;
    private static final VoxelShape POLE = Block.box(2, 0, 2, 14, 16, 14);

    /** Which of the five it is. */
    public enum Kind implements StringRepresentable {
        PUSH, PULL, POLE_OUTER, POLE_INNER, POLE_PURE;

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        public boolean isPole() {
            return this != PUSH && this != PULL;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private final Kind kind;

    public AuraTotemBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected MapCodec<AuraTotemBlock> codec() {
        return CODEC;
    }

    /** The totem a block of a stack answers to: itself, or the first totem above the poles it is part of. */
    public static BlockPos headOf(BlockGetter level, BlockPos pos) {
        BlockPos at = pos;
        for (int climbed = 0; climbed <= MOST_POLES; climbed++) {
            BlockState there = level.getBlockState(at);
            if (!(there.getBlock() instanceof AuraTotemBlock totem)) {
                return null;
            }
            if (!totem.kind().isPole()) {
                return at;
            }
            at = at.above();
        }
        return null;
    }

    /** Another totem or pole in hand is put down against it, so that a stack can be built by clicking. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof BlockItem held && held.getBlock() instanceof AuraTotemBlock) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Opens the totem, from itself or from any pole under it. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        BlockPos head = headOf(level, pos);
        if (head == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level.getBlockEntity(head) instanceof AuraTotemBlockEntity totem) {
            player.openMenu(totem);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof AuraTotemBlockEntity totem) {
            net.minecraft.world.Containers.dropContents(level, pos, totem);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return kind.isPole() ? POLE : Shapes.block();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return kind.isPole() ? null : new AuraTotemBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return kind.isPole() ? null
                : createTickerHelper(type, ModBlockEntities.AURA_TOTEM.get(), AuraTotemBlockEntity::tick);
    }
}
