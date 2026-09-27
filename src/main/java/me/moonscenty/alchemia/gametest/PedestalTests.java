package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.ArcanePedestalBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The stand an infusion is laid out on.
 * <p>
 * All it has to do is take one thing, give it back, and not swallow anything when it is broken. That is the whole
 * of it, and all three are worth holding to: a ring of twelve of these holding somebody's materials is not a thing
 * to be casual about.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class PedestalTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos WHERE = new BlockPos(2, 1, 2);

    private static ArcanePedestalBlockEntity stand(GameTestHelper helper) {
        helper.setBlock(WHERE, ModBlocks.ARCANE_PEDESTAL.get());
        return (ArcanePedestalBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(WHERE));
    }

    /** Clicks the top of the pedestal with whatever is in hand, the way a person would. */
    private static Player clicks(GameTestHelper helper, ItemStack held) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockPos at = helper.absolutePos(WHERE);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false);
        helper.getLevel().getBlockState(at).useItemOn(player.getMainHandItem(), helper.getLevel(), player,
                InteractionHand.MAIN_HAND, hit);
        return player;
    }

    /** One thing goes on and one thing comes off, and a stack does not go on whole. */
    @GameTest(template = TEMPLATE)
    public static void itTakesOneThing(GameTestHelper helper) {
        ArcanePedestalBlockEntity stand = stand(helper);
        Player player = clicks(helper, new ItemStack(Items.IRON_INGOT, 5));

        helper.assertTrue(stand.held().is(Items.IRON_INGOT), "one ingot went up");
        helper.assertValueEqual(stand.held().getCount(), 1, "one, not the handful");
        helper.assertValueEqual(player.getMainHandItem().getCount(), 4, "and the rest stayed in hand");

        BlockPos at = helper.absolutePos(WHERE);
        helper.getLevel().getBlockState(at).useWithoutItem(helper.getLevel(),
                helper.makeMockPlayer(GameType.SURVIVAL),
                new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false));
        helper.assertTrue(stand.held().isEmpty(), "and an empty hand takes it back");
        helper.succeed();
    }

    /** A pedestal that is holding something will not take another thing on top of it. */
    @GameTest(template = TEMPLATE)
    public static void itHoldsOneThingAtATime(GameTestHelper helper) {
        ArcanePedestalBlockEntity stand = stand(helper);
        clicks(helper, new ItemStack(Items.IRON_INGOT));
        Player second = clicks(helper, new ItemStack(Items.GOLD_INGOT));

        helper.assertTrue(stand.held().is(Items.IRON_INGOT), "the first thing is still the one up there");
        helper.assertValueEqual(second.getMainHandItem().getCount(), 1, "and the second was not taken");
        helper.succeed();
    }

    /** Breaking it drops what was standing on it rather than losing it. */
    @GameTest(template = TEMPLATE)
    public static void breakingItDropsWhatIsOnIt(GameTestHelper helper) {
        stand(helper);
        clicks(helper, new ItemStack(Items.DIAMOND));

        helper.setBlock(WHERE, Blocks.AIR);
        helper.assertItemEntityPresent(Items.DIAMOND, WHERE, 2.0);
        helper.succeed();
    }
}
