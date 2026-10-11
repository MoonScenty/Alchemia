package me.moonscenty.alchemia.item.tool;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The shovel of the earthmover: an alchemium shovel with earth bound into it.
 *
 * <p>It digs three by three, a working it comes with. Used against the side of a block it does the other half of
 * the trade: it lays a three-by-three of that same block out from the face, out of the bag, one per square.
 * Grass it cannot copy is laid as dirt. Which way the square stands -- flat against the face, or out of it one
 * way or the other -- is turned with the same key that sets an architect's focus.
 *
 * <p>Crouching puts it back to being a shovel, and a shovel used on grass makes a path.
 */
public class ElementalShovelItem extends ShovelItem {
    /** How many ways the square can stand. */
    public static final int LAYINGS = 3;

    public ElementalShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (level.getBlockEntity(clicked) != null) {
            return InteractionResult.PASS;
        }
        BlockState laying = level.getBlockState(clicked);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean any = false;
        for (BlockPos at : square(context.getItemInHand(), clicked, context.getClickedFace(), player.getYRot())) {
            if (!level.getBlockState(at).canBeReplaced()) {
                continue;
            }
            BlockState put = laid(player, laying);
            if (put == null) {
                break;
            }
            level.setBlock(at, put, 3);
            level.playSound(null, at, put.getSoundType().getPlaceSound(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.6F, 0.9F + level.random.nextFloat() * 0.2F);
            context.getItemInHand().hurtAndBreak(1, player,
                    net.minecraft.world.entity.LivingEntity.getSlotForHand(context.getHand()));
            any = true;
        }
        return any ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    /**
     * What goes in one square, taken out of the bag: the block itself, or dirt for grass that cannot be copied.
     * Nothing, when the bag has run out of both.
     */
    private static BlockState laid(Player player, BlockState laying) {
        if (player.isCreative() || takeOne(player, new ItemStack(laying.getBlock()))) {
            return laying;
        }
        if (laying.is(Blocks.GRASS_BLOCK) && takeOne(player, new ItemStack(Blocks.DIRT))) {
            return Blocks.DIRT.defaultBlockState();
        }
        return null;
    }

    private static boolean takeOne(Player player, ItemStack wanted) {
        if (wanted.isEmpty()) {
            return false;
        }
        int slot = player.getInventory().findSlotMatchingItem(wanted);
        if (slot < 0) {
            return false;
        }
        player.getInventory().removeItem(slot, 1);
        return true;
    }

    /**
     * The nine squares in front of the face, standing whichever way the shovel is set to.
     * <p>
     * Flat lies against the face. The other two stand out of it, upright and across; on a floor or a ceiling
     * which way is across is settled by which way the player is facing. The arithmetic is the original's.
     */
    public static List<BlockPos> square(ItemStack shovel, BlockPos clicked, Direction face, float yaw) {
        int laying = laying(shovel);
        boolean level = face.getAxis() == Direction.Axis.Y;
        boolean alongZ = face.getAxis() == Direction.Axis.Z;
        int quarter = Mth.floor(yaw * 4.0F / 360.0F + 0.5) & 3;
        boolean facingX = quarter == 1 || quarter == 3;

        List<BlockPos> squares = new ArrayList<>();
        BlockPos front = clicked.relative(face);
        for (int one = -1; one <= 1; one++) {
            for (int other = -1; other <= 1; other++) {
                int x = 0;
                int y = 0;
                int z = 0;
                if (laying == 1) {
                    y = other;
                    if (level) {
                        if (facingX) {
                            z = one;
                        } else {
                            x = one;
                        }
                    } else if (alongZ) {
                        z = one;
                    } else {
                        x = one;
                    }
                } else if (laying == 2) {
                    if (level) {
                        y = other;
                        if (facingX) {
                            z = one;
                        } else {
                            x = one;
                        }
                    } else {
                        z = other;
                        x = one;
                    }
                } else if (level) {
                    x = one;
                    z = other;
                } else if (alongZ) {
                    x = one;
                    y = other;
                } else {
                    z = one;
                    y = other;
                }
                squares.add(front.offset(x, y, z));
            }
        }
        return squares;
    }

    /** Which way it is set to lay. */
    public static int laying(ItemStack shovel) {
        return shovel.getOrDefault(ModDataComponents.SHOVEL_LAYING.get(), 0);
    }

    /** Turns it to the next way of laying, and says which that is. */
    public static void turn(Player player, ItemStack shovel) {
        int next = (laying(shovel) + 1) % LAYINGS;
        shovel.set(ModDataComponents.SHOVEL_LAYING.get(), next);
        player.displayClientMessage(Component.translatable("item.alchemia.elemental_shovel.laying." + next), true);
    }
}
