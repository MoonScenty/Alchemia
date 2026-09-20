package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.ResearchCategory;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ScanTarget;
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
}
