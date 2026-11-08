package me.moonscenty.alchemia.gametest;

import java.util.HashSet;
import java.util.Set;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NoteRequests;
import me.moonscenty.alchemia.research.ResearchCategory;
import me.moonscenty.alchemia.research.ResearchCompletion;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ScanTarget;
import me.moonscenty.alchemia.research.StartingResearch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class ResearchTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void thePackLoadsEveryBranchOfStudy(GameTestHelper helper) {
        Registry<ResearchCategory> categories = helper.getLevel().registryAccess().registryOrThrow(ModResearch.CATEGORY_KEY);
        helper.assertTrue(categories.size() == 6, "there should be six branches of study, loaded " + categories.size());
        helper.assertTrue(categories.containsKey(ModResearch.ARCANA.location()), "arcana should be among them");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void somethingStartsUnlocked(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        long open = entries.stream().filter(ResearchEntry::autoUnlock).count();
        helper.assertTrue(open > 0, "no entry starts unlocked, so the book would open on nothing readable");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void startingResearchIsHandedOutOnceOnly(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        PlayerKnowledge fresh = PlayerKnowledge.fresh();
        helper.assertTrue(fresh.completedResearch().isEmpty(), "a new reader should have finished nothing");

        PlayerKnowledge once = StartingResearch.fold(fresh, entries);
        long expected = entries.stream().filter(ResearchEntry::autoUnlock).count();
        helper.assertTrue(once.completedResearch().size() == expected,
                "expected " + expected + " entries handed over, got " + once.completedResearch().size());

        PlayerKnowledge twice = StartingResearch.fold(once, entries);
        helper.assertTrue(twice == once, "a second login should change nothing, so the same knowledge comes back");
        helper.assertTrue(once.discoveredAspects().equals(fresh.discoveredAspects()),
                "handing over research must not touch which aspects are known");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void forgettingOneHalfKeepsTheOther(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        PlayerKnowledge full = StartingResearch.fold(PlayerKnowledge.fresh(), entries)
                .withAspects(AspectList.of(ModAspects.METAL, 1));

        PlayerKnowledge noResearch = full.withoutResearch();
        helper.assertTrue(noResearch.completedResearch().isEmpty(), "the research should be gone");
        helper.assertTrue(noResearch.knows(ModAspects.METAL), "the aspects should have stayed");

        PlayerKnowledge noAspects = full.withoutAspects();
        helper.assertTrue(!noAspects.knows(ModAspects.METAL), "the learned aspect should be gone");
        helper.assertTrue(noAspects.completedResearch().equals(full.completedResearch()), "the research should have stayed");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyEntryPointsSomewhereReal(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        Registry<ResearchCategory> categories = helper.getLevel().registryAccess().registryOrThrow(ModResearch.CATEGORY_KEY);
        helper.assertTrue(entries.size() > 0, "no research was loaded at all");

        for (ResourceLocation id : entries.keySet()) {
            ResearchEntry entry = entries.get(id);
            helper.assertTrue(categories.containsKey(entry.category().location()),
                    id + " belongs to a branch that does not exist: " + entry.category().location());
            for (ResourceLocation parent : entry.parents()) {
                helper.assertTrue(entries.containsKey(parent), id + " lists a parent that does not exist: " + parent);
                helper.assertTrue(!parent.equals(id), id + " is its own parent");
            }
        }
        Alchemia.LOGGER.info("{} research entries loaded across {} branches", entries.size(), categories.size());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void somethingIsReachableFromTheStart(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        PlayerKnowledge fresh = PlayerKnowledge.fresh();

        boolean anyOpen = entries.entrySet().stream()
                .anyMatch(entry -> entry.getValue().isAvailableTo(fresh));
        helper.assertTrue(anyOpen, "a brand new player should have at least one entry open to them");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void requirementsTrackWhatIsStillUnknown(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        ResearchEntry crystals = entries.get(Alchemia.id("vis_crystals"));
        helper.assertTrue(crystals != null, "the vis crystal entry should have loaded");

        PlayerKnowledge fresh = PlayerKnowledge.fresh();
        helper.assertTrue(crystals.missingFor(fresh::knows).isPresent(), "a fresh player should not yet know vitreus or auram");

        PlayerKnowledge learned = fresh.withAspect(ModAspects.CRYSTAL).withAspect(ModAspects.AURA);
        helper.assertTrue(crystals.missingFor(learned::knows).isEmpty(), "once both are known nothing should be missing");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void scanningReadsWhatAThingIsMadeOf(GameTestHelper helper) {
        ScanTarget iron = new ScanTarget.OfItem(new ItemStack(Items.IRON_INGOT));
        AspectList aspects = iron.aspects();
        helper.assertTrue(aspects.get(ModAspects.METAL) == 4, "an iron ingot should read as metallum 4, read " + aspects);

        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        ScanTarget ore = new ScanTarget.OfBlock(helper.getLevel(), pos, ModBlocks.AMBER_ORE.get().defaultBlockState());
        helper.assertTrue(ore.aspects().get(ModAspects.TRAP) > 0, "amber ore should read as vinculum, read " + ore.aspects());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theDeskShowsWhatIsLeftOnIt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.RESEARCH_TABLE.get());
        ResearchTableBlockEntity table = (ResearchTableBlockEntity) helper.getBlockEntity(pos);

        helper.assertTrue(!helper.getBlockState(pos).getValue(ResearchTableBlock.HAS_TOOLS), "a bare desk shows nothing");

        table.put(ResearchTableBlockEntity.SLOT_TOOLS, new ItemStack(ModItems.SCRIBING_TOOLS.get()));
        helper.assertTrue(helper.getBlockState(pos).getValue(ResearchTableBlock.HAS_TOOLS), "the inkwell should have appeared");
        helper.assertTrue(!helper.getBlockState(pos).getValue(ResearchTableBlock.HAS_NOTES), "but not the note");

        table.put(ResearchTableBlockEntity.SLOT_NOTES, new ItemStack(ModItems.RESEARCH_NOTES.get()));
        helper.assertTrue(helper.getBlockState(pos).getValue(ResearchTableBlock.HAS_NOTES), "the note should have appeared too");

        table.put(ResearchTableBlockEntity.SLOT_TOOLS, ItemStack.EMPTY);
        helper.assertTrue(!helper.getBlockState(pos).getValue(ResearchTableBlock.HAS_TOOLS), "taking the tools back clears the inkwell");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theDeskIsFussyAboutWhatGoesOnIt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 4);
        helper.setBlock(pos, ModBlocks.RESEARCH_TABLE.get());
        ResearchTableBlockEntity table = (ResearchTableBlockEntity) helper.getBlockEntity(pos);

        helper.assertTrue(table.accepts(ResearchTableBlockEntity.SLOT_TOOLS, new ItemStack(ModItems.SCRIBING_TOOLS.get())),
                "scribing tools belong in the tool slot");
        helper.assertTrue(!table.accepts(ResearchTableBlockEntity.SLOT_TOOLS, new ItemStack(Items.DIAMOND)),
                "a diamond does not");
        helper.assertTrue(!table.accepts(ResearchTableBlockEntity.SLOT_NOTES, new ItemStack(ModItems.SCRIBING_TOOLS.get())),
                "and the tools do not go in the note slot either");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void inkRunsOutEventually(GameTestHelper helper) {
        BlockPos pos = new BlockPos(6, 1, 6);
        helper.setBlock(pos, ModBlocks.RESEARCH_TABLE.get());
        ResearchTableBlockEntity table = (ResearchTableBlockEntity) helper.getBlockEntity(pos);

        helper.assertTrue(!table.useInk(), "an empty desk has no ink to spend");

        ItemStack tools = new ItemStack(ModItems.SCRIBING_TOOLS.get());
        int capacity = tools.getMaxDamage();
        table.put(ResearchTableBlockEntity.SLOT_TOOLS, tools);

        for (int used = 1; used <= capacity; used++) {
            helper.assertTrue(table.useInk(), "the ink gave out early, after " + used);
        }
        helper.assertValueEqual(capacity, 100, "a hundred points of ink");
        helper.assertTrue(!table.useInk(), "and not one more");
        helper.assertTrue(table.get(ResearchTableBlockEntity.SLOT_TOOLS).is(ModItems.SCRIBING_TOOLS.get()),
                "the dry tools stay where they are, to be refilled");
        helper.assertTrue(helper.getBlockState(pos).getValue(ResearchTableBlock.HAS_TOOLS), "and so does the inkwell");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void someParentsAreKnownWithoutALine(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        ResearchEntry manipulator = entries.get(Alchemia.id("focal_manipulator"));
        PlayerKnowledge fire = PlayerKnowledge.fresh().withResearch(Alchemia.id("focus_fire"));
        helper.assertFalse(manipulator.isAvailableTo(fire), "the other foci are wanted first");
        PlayerKnowledge all = fire;
        for (String focus : new String[] {"focus_frost", "focus_shock", "focus_trade", "focus_excavation"}) {
            all = all.withResearch(Alchemia.id(focus));
        }
        helper.assertTrue(manipulator.isAvailableTo(all), "and then it opens");
        helper.assertValueEqual(manipulator.parents(), java.util.List.of(Alchemia.id("focus_fire")),
                "with only the fire focus drawn to it");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyEntryCanBeEarned(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        Set<ResourceLocation> earned = new HashSet<>();
        for (ResourceLocation id : entries.keySet()) {
            ResearchEntry entry = entries.get(id);
            if (entry.autoUnlock() || entry.onlyGiven()) {
                earned.add(id);
            }
        }

        boolean grew = true;
        while (grew) {
            grew = false;
            for (ResourceLocation id : entries.keySet()) {
                ResearchEntry entry = entries.get(id);
                if (!earned.contains(id) && !entry.requirements().isEmpty() && waitsOnly(entry, earned)) {
                    grew |= earned.add(id);
                }
                if (earned.contains(id)) {
                    grew |= earned.addAll(entry.siblings());
                }
            }
        }

        Set<ResourceLocation> stranded = new HashSet<>(entries.keySet());
        stranded.removeAll(earned);
        helper.assertTrue(stranded.isEmpty(), "no way to ever finish " + stranded);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aSiblingIsReadyTheMomentItsKinIsLearned(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        for (ResourceLocation id : entries.keySet()) {
            ResearchEntry entry = entries.get(id);
            Set<ResourceLocation> known = new HashSet<>(entry.parents());
            known.addAll(entry.hiddenParents());
            known.add(id);
            for (ResourceLocation sibling : entry.siblings()) {
                helper.assertTrue(entries.containsKey(sibling), id + " lists a sibling that does not exist: " + sibling);
                helper.assertTrue(waitsOnly(entries.get(sibling), known), sibling + " waits on more than " + id);
            }
        }
        helper.succeed();
    }

    private static boolean waitsOnly(ResearchEntry entry, Set<ResourceLocation> known) {
        return known.containsAll(entry.parents()) && known.containsAll(entry.hiddenParents());
    }

    @GameTest(template = TEMPLATE)
    public static void thePrimalCharmIsKnownFromTheStart(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        helper.assertTrue(StartingResearch.fold(PlayerKnowledge.fresh(), entries).hasResearch(Alchemia.id("primal_charm")),
                "the root of artifice comes free");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theSealsFollowTheClockworkMind(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (String parent : new String[] {"golem_wood", "distillation", "tallow", "nitor"}) {
            player.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.of(player).withResearch(Alchemia.id(parent)));
        }
        int experience = player.totalExperience;

        helper.assertTrue(ResearchCompletion.complete(player, Alchemia.id("clockwork_mind")), "learnt");
        helper.assertTrue(PlayerKnowledge.of(player).hasResearch(Alchemia.id("control_seals")), "the seals come with it");
        helper.assertFalse(PlayerKnowledge.of(player).hasResearch(Alchemia.id("seal_collect")), "one step at a time");
        helper.assertValueEqual(player.totalExperience, experience + ResearchCompletion.EXPERIENCE,
                "only the entry that was worked for pays");

        ModItems.ALCHEMONOMICON.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(PlayerKnowledge.of(player).hasResearch(Alchemia.id("seal_collect")), "reading brings the rest");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void readingTheBookPaysSiblingsStillOwed(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.of(player).withResearch(Alchemia.id("clockwork_mind")));

        ModItems.ALCHEMONOMICON.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(PlayerKnowledge.of(player).hasResearch(Alchemia.id("control_seals")),
                "an old reader still gets the seals");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anEntryWithNothingToStudyTakesNoNote(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        ResourceLocation seals = Alchemia.id("control_seals");
        PlayerKnowledge knowledge = PlayerKnowledge.fresh();
        for (String parent : new String[] {"distillation", "tallow", "nitor"}) {
            knowledge = knowledge.withResearch(Alchemia.id(parent));
        }
        helper.assertTrue(entries.get(seals).isAvailableTo(knowledge), "its parents are known");
        helper.assertFalse(NoteRequests.isReady(knowledge, seals, entries.get(seals)), "but no note can be drawn");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theBoreComesBeforeTheAutocaster(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        helper.assertTrue(entries.get(Alchemia.id("autocaster")).parents().contains(Alchemia.id("arcane_bore")),
                "the autocaster follows the bore");
        helper.assertFalse(entries.get(Alchemia.id("arcane_bore")).parents().contains(Alchemia.id("autocaster")),
                "not the other way round");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyArcaneRecipeWantsResearch(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getRecipes();
        for (var holder : recipes) {
            if (holder.value() instanceof me.moonscenty.alchemia.crafting.ArcaneShapedRecipe
                    || holder.value() instanceof me.moonscenty.alchemia.crafting.ArcaneShapelessRecipe) {
                var research = ((me.moonscenty.alchemia.crafting.ArcaneRecipe) holder.value()).research();
                helper.assertTrue(research.isPresent(), holder.id() + " can be made without any research");
            }
        }
        helper.succeed();
    }
}
