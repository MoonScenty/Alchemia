package me.moonscenty.alchemia.golem.seal;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import me.moonscenty.alchemia.golem.Golem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class Fieldwork {
    private static final GameProfile GOLEM = new GameProfile(UUID.fromString("5e1c9a77-2b4f-4c1e-9a64-0f3d7c1b2a90"),
            "[AlchemiaGolem]");

    private Fieldwork() {
    }

    public static FakePlayer hands(ServerLevel level, Golem golem) {
        FakePlayer player = FakePlayerFactory.get(level, GOLEM);
        player.moveTo(golem.getX(), golem.getY(), golem.getZ(), golem.getYRot(), golem.getXRot());
        player.getInventory().clearContent();
        player.setShiftKeyDown(false);
        return player;
    }

    // the original's ripeness test: stems never count; cane and cactus only above the lowest block
    public static boolean grown(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        Block block = state.getBlock();
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            return true;
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        }
        if (block == Blocks.SUGAR_CANE || block == Blocks.CACTUS) {
            return level.getBlockState(pos.below()).is(block);
        }
        return block instanceof BonemealableBlock growing && !(block instanceof StemBlock)
                && (block instanceof BushBlock || block instanceof CocoaBlock)
                && !growing.isValidBonemealTarget(level, pos, state);
    }

    public static boolean picked(BlockState state) {
        return state.getBlock() instanceof SweetBerryBushBlock;
    }

    public static ItemStack seed(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.MELON || block == Blocks.PUMPKIN || block == Blocks.SUGAR_CANE || block == Blocks.CACTUS) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(block.asItem());
    }

    public static boolean log(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.LOGS);
    }

    // drops fall where the block stood, tool or no tool
    public static boolean harvest(ServerLevel level, BlockPos pos, Golem golem) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) {
            return false;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        level.levelEvent(2001, pos, Block.getId(state));
        Block.dropResources(state, level, pos, entity, golem, ItemStack.EMPTY);
        int experience = state.getExpDrop(level, pos, entity, golem, ItemStack.EMPTY);
        level.removeBlock(pos, false);
        if (experience > 0) {
            state.getBlock().popExperience(level, pos, experience);
        }
        return true;
    }

    // fells from the crown down so the tree never floats
    public static boolean breakFurthest(ServerLevel level, BlockPos pos, Golem golem) {
        BlockState kind = level.getBlockState(pos);
        int reach = log(level, pos) ? 2 : 1;
        BlockPos furthest = pos;
        double distance = 0.0;
        boolean moved = true;
        while (moved) {
            moved = false;
            search:
            for (int x = -reach; x <= reach; x++) {
                for (int y = reach; y >= -reach; y--) {
                    for (int z = -reach; z <= reach; z++) {
                        BlockPos at = furthest.offset(x, y, z);
                        if (Math.abs(at.getX() - pos.getX()) > 24 || Math.abs(at.getY() - pos.getY()) > 48
                                || Math.abs(at.getZ() - pos.getZ()) > 24) {
                            break search;
                        }
                        BlockState there = level.getBlockState(at);
                        if (there.is(kind.getBlock()) && there.getDestroySpeed(level, at) >= 0) {
                            double away = at.distSqr(pos);
                            if (away > distance) {
                                distance = away;
                                furthest = at;
                                moved = true;
                                break search;
                            }
                        }
                    }
                }
            }
        }
        return harvest(level, furthest, golem);
    }
}
