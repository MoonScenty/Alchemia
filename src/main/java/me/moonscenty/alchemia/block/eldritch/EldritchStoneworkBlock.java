package me.moonscenty.alchemia.block.eldritch;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The stonework of an eldritch ring: its altar, the capstones about it, and the two parts of its obelisk. All of it is
 * drawn by its own renderer rather than as a block, gives off a little light, takes a long time to break and leaves
 * nothing behind.
 * <p>
 * Breaking any part of an obelisk brings the whole ring down with it -- every piece of eldritch stonework within three
 * blocks across and two up or down -- in a few blasts that hurt whoever stands near and break nothing else.
 */
public class EldritchStoneworkBlock extends BaseEntityBlock {
    /** What the piece is. */
    public enum Part {
        ALTAR,
        CAP,
        OBELISK,
        OBELISK_BODY
    }

    private final Part part;

    public EldritchStoneworkBlock(Properties properties, Part part) {
        super(properties);
        this.part = part;
    }

    public Part part() {
        return part;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(properties -> new EldritchStoneworkBlock(properties, part));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (part) {
            case ALTAR -> new EldritchAltarBlockEntity(pos, state);
            case CAP -> new EldritchCapBlockEntity(pos, state);
            case OBELISK -> new EldritchObeliskBlockEntity(pos, state);
            case OBELISK_BODY -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return switch (part) {
            case ALTAR -> level.isClientSide ? null
                    : createTickerHelper(type, ModBlockEntities.ELDRITCH_ALTAR.get(), EldritchAltarBlockEntity::tick);
            case OBELISK -> createTickerHelper(type, ModBlockEntities.ELDRITCH_OBELISK.get(),
                    EldritchObeliskBlockEntity::tick);
            default -> null;
        };
    }

    /** An eye goes into the altar. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (part != Part.ALTAR || player.isShiftKeyDown() || !held.is(ModItems.ELDRITCH_EYE.get())
                || !(level.getBlockEntity(pos) instanceof EldritchAltarBlockEntity altar)
                || altar.eyes() >= EldritchAltarBlockEntity.MOST_EYES) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            altar.addEye();
            held.consume(1, player);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        boolean obelisk = part == Part.OBELISK || part == Part.OBELISK_BODY;
        super.onRemove(state, level, pos, replacement, moving);
        if (!obelisk || state.is(replacement.getBlock()) || !(level instanceof ServerLevel served)) {
            return;
        }
        // the whole ring comes down with it
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 2, 3))) {
            if (level.getBlockState(at).is(ModTags.Blocks.ELDRITCH_STONEWORK)) {
                level.removeBlock(at, false);
            }
        }
        served.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0F, Level.ExplosionInteraction.NONE);
    }
}
