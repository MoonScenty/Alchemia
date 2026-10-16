package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchBlockEntity;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
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

/** How a thaumaturge starts: the recipes for the first things, and a wooden table made a desk or a workbench. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class StartingToolsTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void theFirstThingsCanBeMade(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (String id : new String[] {"wooden_table", "scribing_tools", "scribing_tools_from_bottle",
                "scribing_tools_refill", "alchemometer", "arcane_workbench_charger", "balanced_shard_from_air",
                "balanced_shard_from_entropy"}) {
            helper.assertTrue(recipes.byKey(Alchemia.id(id)).isPresent(), "no recipe " + id);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void scribingToolsMakeADeskAndAWandABench(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos desk = new BlockPos(2, 2, 2);
        BlockPos bench = new BlockPos(5, 2, 2);
        helper.setBlock(desk, ModBlocks.WOODEN_TABLE.get());
        helper.setBlock(bench, ModBlocks.WOODEN_TABLE.get());

        use(helper, player, desk, new ItemStack(ModItems.SCRIBING_TOOLS.get()));
        helper.assertBlockPresent(ModBlocks.RESEARCH_TABLE.get(), desk);
        if (!(helper.getBlockEntity(desk) instanceof ResearchTableBlockEntity table)
                || !table.get(ResearchTableBlockEntity.SLOT_TOOLS).is(ModItems.SCRIBING_TOOLS.get())) {
            throw new net.minecraft.gametest.framework.GameTestAssertException("the tools should be on the desk");
        }
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the tools left the hand");

        use(helper, player, bench, new ItemStack(ModItems.WAND.get()));
        helper.assertBlockPresent(ModBlocks.ARCANE_WORKBENCH.get(), bench);
        if (!(helper.getBlockEntity(bench) instanceof ArcaneWorkbenchBlockEntity workbench)
                || !workbench.getItem(ArcaneWorkbenchBlockEntity.SLOT_WAND).is(ModItems.WAND.get())) {
            throw new net.minecraft.gametest.framework.GameTestAssertException("the wand should be in its slot");
        }
        helper.succeed();
    }

    private static void use(GameTestHelper helper, Player player, BlockPos at, ItemStack held) {
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockPos absolute = helper.absolutePos(at);
        var state = helper.getLevel().getBlockState(absolute);
        state.useItemOn(held, helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
    }
}
