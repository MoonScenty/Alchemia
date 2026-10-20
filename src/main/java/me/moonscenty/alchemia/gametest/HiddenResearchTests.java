package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.research.Clues;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NoteRequests;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ScanTarget;
import me.moonscenty.alchemia.research.Scanning;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Hidden research: out of the book until its research and clues are there, and clues found by scanning. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class HiddenResearchTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void hiddenResearchWaitsOnItsClues(GameTestHelper helper) {
        ResourceLocation id = Alchemia.id("bone_bow");
        ResearchEntry bow = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY).get(id);
        helper.assertTrue(bow != null && bow.hidden(), "the bone bow is hidden");

        PlayerKnowledge knowledge = PlayerKnowledge.fresh();
        for (ResourceLocation parent : bow.parents()) {
            knowledge = knowledge.withResearch(parent);
        }
        for (var aspect : bow.requirements().sortedByName()) {
            knowledge = knowledge.withAspect(aspect);
        }
        helper.assertFalse(bow.isShownTo(knowledge, id), "not in the book with no clues");
        helper.assertFalse(NoteRequests.isReady(knowledge, id, bow), "and not to be taken up");
        knowledge = knowledge.withResearch(Clues.BONE);
        helper.assertFalse(bow.isShownTo(knowledge, id), "nor with only one of two");
        knowledge = knowledge.withResearch(Clues.BOW);
        helper.assertTrue(bow.isShownTo(knowledge, id), "in the book with both");
        helper.assertTrue(NoteRequests.isReady(knowledge, id, bow), "and ready to be taken up");

        // an aspect is a clue known once the aspect is
        ResourceLocation lamp = Alchemia.id("arcane_lamp");
        ResearchEntry lampEntry = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY).get(lamp);
        PlayerKnowledge fresh = PlayerKnowledge.fresh();
        helper.assertTrue(lampEntry.isShownTo(fresh, lamp), "a lamp is in the book from the start");
        helper.assertFalse(lampEntry.cluesMet(fresh), "but waits on light");
        helper.assertTrue(lampEntry.cluesMet(fresh.withAspect(ModAspects.LIGHT)), "until light is known");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void scanningABoneFindsItsClue(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.fresh());
        Scanning.scan(player, new ScanTarget.OfItem(new ItemStack(Items.BONE)));
        helper.assertTrue(PlayerKnowledge.of(player).hasResearch(Clues.BONE), "the bone is a clue");
        helper.assertFalse(Scanning.hasAnythingToTeach(player, new ScanTarget.OfItem(new ItemStack(Items.BONE))),
                "and once found, nothing more to learn from it");
        helper.succeed();
    }
}
