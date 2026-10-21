package me.moonscenty.alchemia.gametest;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.menu.ResearchTableMenu;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.HexGrid;
import me.moonscenty.alchemia.research.ResearchNote;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The original's research expertise and mastery: points given back on rubbing out, free points on laying down, and
 * a knowledge fragment spent for a point of each primal.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class ResearcherTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final int TRIES = 400;
    private static final int PLENTY = 1000;
    private static final HexGrid.Hex WRITTEN = HexGrid.CENTRE;

    /** Two earths pinned apart, with air written between them, which joins nothing. */
    private static ResearchNote note(boolean written) {
        return new ResearchNote(ResourceLocation.parse("alchemia:test"), AspectList.of(ModAspects.AIR, PLENTY),
                List.of(ResearchNote.Cell.pinned(new HexGrid.Hex(-1, 0), ModAspects.EARTH),
                        written ? new ResearchNote.Cell(WRITTEN, Optional.of(ModAspects.AIR), false)
                                : ResearchNote.Cell.blank(WRITTEN),
                        ResearchNote.Cell.pinned(new HexGrid.Hex(1, 0), ModAspects.EARTH)),
                false);
    }

    private static ResearchTableMenu table(Player player, ResearchNote note) {
        SimpleContainer desk = new SimpleContainer(2);
        desk.setItem(ResearchTableMenu.SLOT_TOOLS, new ItemStack(ModItems.SCRIBING_TOOLS.get()));
        ItemStack sheet = new ItemStack(ModItems.RESEARCH_NOTES.get());
        sheet.set(ModDataComponents.RESEARCH_NOTE.get(), note);
        desk.setItem(ResearchTableMenu.SLOT_NOTES, sheet);
        return new ResearchTableMenu(0, player.getInventory(), desk, ContainerLevelAccess.NULL);
    }

    private static Player researcher(GameTestHelper helper, ResourceLocation... research) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        for (ResourceLocation done : research) {
            knowledge = knowledge.withResearch(done);
        }
        player.setData(ModAttachments.KNOWLEDGE, knowledge);
        return player;
    }

    /** How many of so many rubbings out gave the point back. */
    private static int givenBack(Player player) {
        int back = 0;
        for (int tried = 0; tried < TRIES; tried++) {
            ResearchTableMenu table = table(player, note(true));
            table.place(player, WRITTEN, Optional.empty());
            back += table.note().budget().get(ModAspects.AIR) - PLENTY;
        }
        return back;
    }

    /** How many of so many layings down cost nothing. */
    private static int free(Player player) {
        int free = 0;
        for (int tried = 0; tried < TRIES; tried++) {
            ResearchTableMenu table = table(player, note(false));
            table.place(player, WRITTEN, Optional.of(ModAspects.AIR));
            free += table.note().budget().get(ModAspects.AIR) - (PLENTY - 1);
        }
        return free;
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void rubbingOutGivesBackForAResearcher(GameTestHelper helper) {
        helper.assertValueEqual(givenBack(researcher(helper)), 0, "nothing comes back to a novice");
        int expert = givenBack(researcher(helper, ResearchTableMenu.RESEARCHER_1));
        helper.assertTrue(expert > TRIES / 8 && expert < TRIES * 3 / 8, "a quarter or so to an expert: " + expert);
        int master = givenBack(researcher(helper, ResearchTableMenu.RESEARCHER_1, ResearchTableMenu.RESEARCHER_2));
        helper.assertTrue(master > TRIES * 3 / 8 && master < TRIES * 5 / 8, "a half or so to a master: " + master);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aMasterSometimesWritesForFree(GameTestHelper helper) {
        helper.assertValueEqual(free(researcher(helper, ResearchTableMenu.RESEARCHER_1)), 0,
                "an expert pays every time");
        int master = free(researcher(helper, ResearchTableMenu.RESEARCHER_1, ResearchTableMenu.RESEARCHER_2));
        helper.assertTrue(master > TRIES / 40 && master < TRIES * 3 / 20, "a tenth or so to a master: " + master);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anExpertSpendsAFragment(GameTestHelper helper) {
        Player novice = researcher(helper);
        novice.getInventory().add(new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get()));
        ResearchTableMenu table = table(novice, note(false));
        table.spendFragment(novice);
        helper.assertValueEqual(table.note().budget().get(ModAspects.FIRE), 0, "a novice cannot");

        Player expert = researcher(helper, ResearchTableMenu.RESEARCHER_1);
        expert.getInventory().add(new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get()));
        table = table(expert, note(false));
        table.spendFragment(expert);
        for (var primal : ModAspects.primals()) {
            helper.assertValueEqual(table.note().budget().get(primal), primal.value() == ModAspects.AIR.value()
                    ? PLENTY + 1 : 1, "a point of each primal");
        }
        helper.assertFalse(expert.getInventory().contains(new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get())),
                "for the fragment");
        helper.succeed();
    }
}
