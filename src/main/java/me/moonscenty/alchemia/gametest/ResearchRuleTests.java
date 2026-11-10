package me.moonscenty.alchemia.gametest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.Clues;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NoteGeneration;
import me.moonscenty.alchemia.research.NoteRequests;
import me.moonscenty.alchemia.research.RecipeGates;
import me.moonscenty.alchemia.research.ResearchCategory;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ResearchNote;
import me.moonscenty.alchemia.research.ResearchPurchase;
import me.moonscenty.alchemia.research.Scanning;
import me.moonscenty.alchemia.research.StartingResearch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class ResearchRuleTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static ServerPlayer reader(GameTestHelper helper, String... known) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "reader" + helper.getLevel().random.nextInt(100000)));
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        for (String id : known) {
            knowledge = knowledge.withResearch(id.contains(":") ? ResourceLocation.parse(id) : Alchemia.id(id));
        }
        player.setData(ModAttachments.KNOWLEDGE, knowledge);
        return player;
    }

    private static Registry<ResearchEntry> entries(GameTestHelper helper) {
        return helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
    }

    private static boolean knows(ServerPlayer player, String id) {
        return PlayerKnowledge.of(player).hasResearch(Alchemia.id(id));
    }

    @GameTest(template = TEMPLATE)
    public static void secondaryResearchIsBoughtWithLevels(GameTestHelper helper) {
        ResearchEntry jar = entries(helper).get(Alchemia.id("jar_void"));
        helper.assertTrue(jar.secondary(), "the void jar is secondary research");
        helper.assertValueEqual(jar.experience(), 3, "and costs the root of nine");

        ServerPlayer poor = reader(helper, "jar_label");
        poor.experienceLevel = 2;
        helper.assertFalse(ResearchPurchase.buy(poor, Alchemia.id("jar_void")), "two levels are not enough");
        helper.assertFalse(knows(poor, "jar_void"), "so nothing is learned");

        ServerPlayer rich = reader(helper, "jar_label");
        rich.experienceLevel = 5;
        helper.assertTrue(ResearchPurchase.buy(rich, Alchemia.id("jar_void")), "five are");
        helper.assertTrue(knows(rich, "jar_void"), "and it is learned");
        helper.assertValueEqual(rich.experienceLevel, 2, "for three levels");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void secondaryResearchTakesNoNote(GameTestHelper helper) {
        ServerPlayer player = reader(helper, "jar_label");
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        for (var aspect : entries(helper).get(Alchemia.id("jar_void")).requirements().sortedByName()) {
            knowledge = knowledge.withAspects(me.moonscenty.alchemia.aspect.AspectList.of(aspect, 1));
        }
        helper.assertFalse(NoteRequests.isReady(knowledge, Alchemia.id("jar_void"),
                entries(helper).get(Alchemia.id("jar_void"))), "no note for what is bought");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theOriginalsStartingResearchIsKnownAtOnce(GameTestHelper helper) {
        PlayerKnowledge start = StartingResearch.fold(PlayerKnowledge.of(reader(helper)), entries(helper));
        for (String id : new String[] {"crucible", "warp", "arcane_stone", "enchanted_fabric"}) {
            helper.assertTrue(start.hasResearch(Alchemia.id(id)), id + " should be known from the start");
        }
        helper.assertFalse(start.hasResearch(Alchemia.id("golem_wood")), "golems wait for golemancy");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void golemancyAndEldritchTabsOpenLater(GameTestHelper helper) {
        Registry<ResearchCategory> tabs = helper.getLevel().registryAccess().registryOrThrow(ModResearch.CATEGORY_KEY);
        PlayerKnowledge fresh = PlayerKnowledge.of(reader(helper));
        helper.assertFalse(tabs.get(ModResearch.GOLEMANCY).isOpenTo(fresh), "golemancy is closed at first");
        helper.assertFalse(tabs.get(ModResearch.ELDRITCH).isOpenTo(fresh), "and so is the eldritch");
        helper.assertTrue(tabs.get(ModResearch.ALCHEMY).isOpenTo(fresh), "alchemy is not");
        helper.assertTrue(tabs.get(ModResearch.GOLEMANCY).isOpenTo(fresh.withResearch(Clues.BASIC_GOLEMANCY)),
                "golemancy opens with basic golemancy");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void scanningAGolemTeachesGolemancy(GameTestHelper helper) {
        ServerPlayer player = reader(helper);
        var golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(4, 2, 4));
        Scanning.scan(player, me.moonscenty.alchemia.research.ScanTarget.of(golem));
        helper.assertTrue(knows(player, "basic_golemancy"), "the golem should have taught golemancy");
        helper.assertTrue(knows(player, "golem_wood"), "and wooden golems with it");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theSimplestNotesAreNeverTorn(GameTestHelper helper) {
        ResearchEntry amber = entries(helper).get(Alchemia.id("amber"));
        helper.assertValueEqual(amber.complexity(), 1, "amber is the simplest kind");
        Set<Integer> sizes = new HashSet<>();
        RandomSource random = RandomSource.create(1L);
        for (int i = 0; i < 20; i++) {
            sizes.add(NoteGeneration.draw(Alchemia.id("amber"), amber, random).cells().size());
        }
        helper.assertValueEqual(sizes.size(), 1, "every board should be whole");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aSolvedNoteNeedsItsParentsKnown(GameTestHelper helper) {
        ServerPlayer player = reader(helper);
        ItemStack note = new ItemStack(ModItems.RESEARCH_NOTES.get());
        note.set(ModDataComponents.RESEARCH_NOTE.get(),
                new ResearchNote(Alchemia.id("jar_void"), me.moonscenty.alchemia.aspect.AspectList.EMPTY,
                        java.util.List.of(), true));
        player.setItemInHand(InteractionHand.MAIN_HAND, note);
        note.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertFalse(knows(player, "jar_void"), "without the jar label it cannot be understood");
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "and the note is kept");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void carriedThingsSpareTheAuraOnlyOnceLearned(GameTestHelper helper) {
        helper.assertFalse(AuraHandler.spares(reader(helper)), "not at first");
        helper.assertTrue(AuraHandler.spares(reader(helper, "aura_preserve")), "but after the aura preserver");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theOldPedestalsWantTheirStoneSeen(GameTestHelper helper) {
        var research = java.util.Optional.of(Alchemia.id("infusion"));
        PlayerKnowledge infused = PlayerKnowledge.of(reader(helper, "infusion"));
        helper.assertFalse(RecipeGates.known(infused, Alchemia.id("ancient_pedestal"), research),
                "infusion alone is not enough");
        helper.assertTrue(RecipeGates.known(infused.withResearch(Clues.ANCIENT_STONE), Alchemia.id("ancient_pedestal"),
                research), "with ancient stone seen it is");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theAltarIsDrawnAndItsOldFormsWaitForTheirStone(GameTestHelper helper) {
        ResearchEntry infusion = entries(helper).get(Alchemia.id("infusion"));
        var built = infusion.pages().stream().filter(page -> page instanceof me.moonscenty.alchemia.research.ResearchPage.Construct)
                .map(page -> (me.moonscenty.alchemia.research.ResearchPage.Construct) page).toList();
        helper.assertValueEqual(built.size(), 3, "three altars are drawn");
        helper.assertValueEqual(built.get(0).blocks().size(), 27, "each three blocks a side");
        PlayerKnowledge fresh = PlayerKnowledge.of(reader(helper));
        helper.assertValueEqual(infusion.pagesFor(fresh::hasResearch).stream()
                .filter(page -> page instanceof me.moonscenty.alchemia.research.ResearchPage.Construct).count(), 1L,
                "only the plain altar until the old stones are seen");
        helper.succeed();
    }
}
