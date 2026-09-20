package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.ResearchCategory;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ScanTarget;
import me.moonscenty.alchemia.research.StartingResearch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    /** A reader has to start somewhere, so at least one entry is handed over rather than researched. */
    @GameTest(template = TEMPLATE)
    public static void somethingStartsUnlocked(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        long open = entries.stream().filter(ResearchEntry::autoUnlock).count();
        helper.assertTrue(open > 0, "no entry starts unlocked, so the book would open on nothing readable");
        helper.succeed();
    }

    /** Handing out the starting entries twice must not differ from handing them out once. */
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

    /** Clearing one half of what a player knows must leave the other half alone. */
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

    /** Every entry must point at a branch that exists and at parents that exist, or the book would dead-end. */
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

    /** The first entry has to be reachable from a standing start, or nothing else ever opens up. */
    @GameTest(template = TEMPLATE)
    public static void somethingIsReachableFromTheStart(GameTestHelper helper) {
        Registry<ResearchEntry> entries = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        PlayerKnowledge fresh = PlayerKnowledge.fresh();

        boolean anyOpen = entries.entrySet().stream()
                .anyMatch(entry -> entry.getValue().isAvailableTo(fresh::hasResearch));
        helper.assertTrue(anyOpen, "a brand new player should have at least one entry open to them");
        helper.succeed();
    }

    /** Requirements are what a player has yet to discover, so a fresh player must be short of the harder entries. */
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

    /** A scan reads what a thing is made of, and an item and the block it comes from must agree. */
    @GameTest(template = TEMPLATE)
    public static void scanningReadsWhatAThingIsMadeOf(GameTestHelper helper) {
        ScanTarget iron = new ScanTarget.OfItem(new ItemStack(Items.IRON_INGOT));
        AspectList aspects = iron.aspects();
        helper.assertTrue(aspects.get(ModAspects.METAL) == 4, "an iron ingot should read as metallum 4, read " + aspects);

        // a block is read through the item it drops, so the ore and its item must agree
        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        ScanTarget ore = new ScanTarget.OfBlock(helper.getLevel(), pos, ModBlocks.AMBER_ORE.get().defaultBlockState());
        helper.assertTrue(ore.aspects().get(ModAspects.TRAP) > 0, "amber ore should read as vinculum, read " + ore.aspects());
        helper.succeed();
    }

    /** What is left on the desk has to show up on it, since the inkwell and note are part of the block's model. */
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

    /** The desk only takes the two things that belong on it. */
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

    /** Ink runs down as a note is worked through, and the tools vanish once the bottle is dry. */
    @GameTest(template = TEMPLATE)
    public static void inkRunsOutEventually(GameTestHelper helper) {
        BlockPos pos = new BlockPos(6, 1, 6);
        helper.setBlock(pos, ModBlocks.RESEARCH_TABLE.get());
        ResearchTableBlockEntity table = (ResearchTableBlockEntity) helper.getBlockEntity(pos);

        helper.assertTrue(!table.useInk(), "an empty desk has no ink to spend");

        ItemStack tools = new ItemStack(ModItems.SCRIBING_TOOLS.get());
        int capacity = tools.getMaxDamage();
        table.put(ResearchTableBlockEntity.SLOT_TOOLS, tools);

        // a bottle holding 64 points is good for 64 strokes, the last of which empties it
        for (int used = 1; used <= capacity; used++) {
            helper.assertTrue(table.useInk(), "the ink gave out early, after " + used);
        }
        helper.assertTrue(table.get(ResearchTableBlockEntity.SLOT_TOOLS).isEmpty(),
                "the dry tools should have been cleared away");
        helper.assertTrue(!helper.getBlockState(pos).getValue(ResearchTableBlock.HAS_TOOLS), "and the inkwell with them");
        helper.succeed();
    }
}
