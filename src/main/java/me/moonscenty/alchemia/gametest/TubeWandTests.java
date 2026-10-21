package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.BellowsBlock;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.block.entity.BufferTubeBlockEntity;
import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** A wand on a tube's arm shuts that side; crouching, it chokes a buffer's; bellows make a buffer pull harder. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TubeWandTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos START = new BlockPos(2, 1, 2);

    private static AlembicBlockEntity alembic(GameTestHelper helper, BlockPos at, int water) {
        helper.setBlock(at, ModBlocks.ALEMBIC.get());
        AlembicBlockEntity alembic = (AlembicBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
        for (int one = 0; one < water; one++) {
            alembic.accept(ModAspects.WATER);
        }
        return alembic;
    }

    private static <T> T entity(GameTestHelper helper, BlockPos at) {
        @SuppressWarnings("unchecked")
        T found = (T) helper.getLevel().getBlockEntity(helper.absolutePos(at));
        return found;
    }

    private static void run(GameTestHelper helper, BlockPos at, int times) {
        BlockPos where = helper.absolutePos(at);
        TubeBlockEntity tube = entity(helper, at);
        for (int tick = 0; tick < times; tick++) {
            TubeBlockEntity.tick(helper.getLevel(), where, helper.getLevel().getBlockState(where), tube);
        }
    }

    /** Clicks a wand on one arm of a tube, out towards that side of the middle. */
    private static void wandOn(GameTestHelper helper, Player player, BlockPos at, Direction arm) {
        BlockPos where = helper.absolutePos(at);
        player.setItemInHand(InteractionHand.MAIN_HAND, WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD));
        Vec3 hit = Vec3.atCenterOf(where).add(arm.getStepX() * 0.35, arm.getStepY() * 0.35, arm.getStepZ() * 0.35);
        helper.getLevel().getBlockState(where).useItemOn(player.getMainHandItem(), helper.getLevel(), player,
                InteractionHand.MAIN_HAND, new BlockHitResult(hit, arm, where, false));
    }

    /** A shut joint carries nothing, from either end, until it is opened again. */
    @GameTest(template = TEMPLATE)
    public static void aWandShutsAJoint(GameTestHelper helper) {
        AlembicBlockEntity from = alembic(helper, START, 6);
        helper.setBlock(START.east(), ModBlocks.TUBE.get());
        helper.setBlock(START.east(2), ModBlocks.TUBE.get());
        AlembicBlockEntity to = alembic(helper, START.east(3), 0);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        wandOn(helper, player, START.east(), Direction.EAST);
        helper.assertTrue(isShut(helper, START.east(), Direction.EAST), "the side clicked is shut");
        helper.assertTrue(isShut(helper, START.east(2), Direction.WEST), "and so is the far end of the joint");
        run(helper, START.east(2), 60);
        helper.assertTrue(to.isEmpty(), "nothing gets through a shut joint");

        wandOn(helper, player, START.east(), Direction.EAST);
        helper.assertTrue(!isShut(helper, START.east(2), Direction.WEST), "a second click opens it again");
        run(helper, START.east(2), 60);
        helper.assertTrue(!to.isEmpty(), "and then it carries");
        helper.assertValueEqual(from.amount() + to.amount(), 6, "nothing was made or lost");
        helper.succeed();
    }

    private static boolean isShut(GameTestHelper helper, BlockPos at, Direction side) {
        return helper.<TubeBlockEntity>getBlockEntity(at).shut(side);
    }

    /** A wand on the middle of a tube is not a wand on any of its arms. */
    @GameTest(template = TEMPLATE)
    public static void theMiddleIsNoArm(GameTestHelper helper) {
        alembic(helper, START, 1);
        helper.setBlock(START.east(), ModBlocks.TUBE.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos where = helper.absolutePos(START.east());
        player.setItemInHand(InteractionHand.MAIN_HAND, WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD));
        helper.getLevel().getBlockState(where).useItemOn(player.getMainHandItem(), helper.getLevel(), player,
                InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(where), Direction.UP, where, false));
        for (Direction side : Direction.values()) {
            helper.assertTrue(!isShut(helper, START.east(), side), "nothing was shut");
        }
        helper.succeed();
    }

    /** Crouching, a buffer's arm turns open, choked, shut: a shut one does not fill it. */
    @GameTest(template = TEMPLATE)
    public static void aCrouchingWandChokesABuffer(GameTestHelper helper) {
        alembic(helper, START, 6);
        helper.setBlock(START.east(), ModBlocks.TUBE_BUFFER.get());
        BufferTubeBlockEntity buffer = entity(helper, START.east());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);

        wandOn(helper, player, START.east(), Direction.WEST);
        helper.assertValueEqual(buffer.choked(Direction.WEST), 1, "once is choked");
        helper.assertTrue(!buffer.shut(Direction.WEST), "which is not shut");
        wandOn(helper, player, START.east(), Direction.WEST);
        helper.assertValueEqual(buffer.choked(Direction.WEST), 2, "twice is shut");
        run(helper, START.east(), 60);
        helper.assertTrue(buffer.held().isEmpty(), "and fills nothing that way");

        wandOn(helper, player, START.east(), Direction.WEST);
        helper.assertValueEqual(buffer.choked(Direction.WEST), 0, "three times round is open again");
        run(helper, START.east(), 60);
        helper.assertTrue(!buffer.held().isEmpty(), "and it fills");
        helper.succeed();
    }

    /** A bellows blowing on a buffer makes it pull two to a turn; choked, the bellows count for nothing. */
    @GameTest(template = TEMPLATE)
    public static void bellowsHurryABuffer(GameTestHelper helper) {
        alembic(helper, START, 8);
        helper.setBlock(START.east(), ModBlocks.TUBE_BUFFER.get());
        helper.setBlock(START.east().north(), ModBlocks.BELLOWS.get().defaultBlockState()
                .setValue(BellowsBlock.FACING, Direction.SOUTH));
        BufferTubeBlockEntity buffer = entity(helper, START.east());

        // the eleventh tick is the first turn
        run(helper, START.east(), 11);
        helper.assertValueEqual(buffer.held().total(), 2, "one for the turn and one for the bellows");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        wandOn(helper, player, START.east(), Direction.WEST);
        run(helper, START.east(), 11);
        helper.assertValueEqual(buffer.held().total(), 3, "choked, only the one");
        helper.succeed();
    }
}
