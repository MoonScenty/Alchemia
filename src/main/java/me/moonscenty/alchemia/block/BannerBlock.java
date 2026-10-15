package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.item.PhialItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A banner: dyed cloth on a pole, or hanging from a wall, with an aspect painted on it if somebody has put a phial to
 * it -- and the Crimson Cult's own, which nobody makes and nobody paints. Nothing walks into a banner, and it stays up
 * when what it was put on is taken away, as the original's did.
 */
public class BannerBlock extends BaseEntityBlock {
    public static final MapCodec<BannerBlock> CODEC = simpleCodec(BannerBlock::new);
    private static final VoxelShape STANDING = Block.box(5.3, 0.0, 5.3, 10.6, 32.0, 10.6);
    private static final VoxelShape[] HANGING = {
            Block.box(0.0, -16.0, 0.0, 16.0, 16.0, 4.0),
            Block.box(12.0, -16.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(0.0, -16.0, 12.0, 16.0, 16.0, 16.0),
            Block.box(0.0, -16.0, 0.0, 4.0, 16.0, 16.0)};

    public BannerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BannerBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BannerBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity banner) {
            return banner.onWall() ? HANGING[(banner.facing() / 4) & 3] : STANDING;
        }
        return Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.empty();
    }

    /** A filled phial paints its aspect on; crouching wipes it off. The cult's banner takes no paint. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BannerBlockEntity banner) || banner.isCult()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                banner.paint(null);
                level.playSound(null, pos, SoundEvents.WOOL_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!(held.getItem() instanceof PhialItem) || PhialItem.inside(held).isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            banner.paint(PhialItem.inside(held).get());
            held.consume(1, player);
            level.playSound(null, pos, SoundEvents.WOOL_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected java.util.List<ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        BlockEntity found = params.getOptionalParameter(
                net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (found instanceof BannerBlockEntity banner) {
            return java.util.List.of(BannerItem.of(banner.colour(), banner.aspect()));
        }
        return java.util.List.of(new ItemStack(me.moonscenty.alchemia.registry.ModItems.BANNER.get()));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
            Player player) {
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity banner) {
            return BannerItem.of(banner.colour(), banner.aspect());
        }
        return super.getCloneItemStack(state, target, level, pos, player);
    }
}
