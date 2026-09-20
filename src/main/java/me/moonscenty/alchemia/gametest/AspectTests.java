package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class AspectTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void registryHoldsEveryAspect(GameTestHelper helper) {
        helper.assertTrue(ModAspects.REGISTRY.size() == 35, "expected 35 aspects but found " + ModAspects.REGISTRY.size());
        helper.assertTrue(ModAspects.primals().size() == 6, "expected 6 primals but found " + ModAspects.primals().size());
        helper.assertTrue(ModAspects.compounds().size() == 29, "expected 29 compounds but found " + ModAspects.compounds().size());
        helper.assertTrue(ModAspects.AIR.get().isPrimal(), "aer should be primal");
        helper.assertTrue(!ModAspects.VOID.get().isPrimal(), "vacuos should be a compound");
        helper.succeed();
    }

    /** Every compound must break down into primals; a cycle or a dangling part would hang or throw here. */
    @GameTest(template = TEMPLATE)
    public static void everyCompoundReducesToPrimals(GameTestHelper helper) {
        for (Holder<Aspect> holder : ModAspects.compounds()) {
            int complexity = holder.value().complexity();
            helper.assertTrue(complexity > 0 && complexity < 10,
                    holder.value().tag() + " has an implausible complexity of " + complexity);
        }
        helper.assertTrue(ModAspects.VOID.get().complexity() == 1, "vacuos should sit one step above the primals");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aspectsCombineEitherWayRound(GameTestHelper helper) {
        // the registry hands back its own holder, so compare the aspects themselves rather than the holders
        helper.assertTrue(ModAspects.combine(ModAspects.AIR, ModAspects.ENTROPY).map(Holder::value).orElse(null) == ModAspects.VOID.get(),
                "aer + perditio should make vacuos");
        helper.assertTrue(ModAspects.combine(ModAspects.ENTROPY, ModAspects.AIR).map(Holder::value).orElse(null) == ModAspects.VOID.get(),
                "the order of the two aspects should not matter");
        helper.assertTrue(ModAspects.combine(ModAspects.AIR, ModAspects.AIR).isEmpty(), "aer twice should make nothing");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void listAddsAndTakesAway(GameTestHelper helper) {
        AspectList list = AspectList.EMPTY.add(ModAspects.FIRE, 3).add(ModAspects.FIRE, 2).add(ModAspects.WATER, 4);
        helper.assertTrue(list.get(ModAspects.FIRE) == 5, "ignis should have added up to 5, was " + list.get(ModAspects.FIRE));
        helper.assertTrue(list.total() == 9, "the total should be 9, was " + list.total());
        helper.assertTrue(list.sortedByAmount().getFirst().value() == ModAspects.FIRE.get(),
                "ignis totals 5 against aqua's 4, so it should sort first");

        helper.assertTrue(list.reduce(ModAspects.WATER, 4).contains(ModAspects.WATER) == false, "aqua should be gone once spent");
        helper.assertTrue(list.scale(0.5F).get(ModAspects.FIRE) == 2, "half of 5 should round down to 2");
        helper.assertTrue(list.capAt(4).get(ModAspects.FIRE) == 4, "ignis should have been capped to 4");
        helper.succeed();
    }

    /** The cull keeps the six most telling aspects, favouring intricate ones over plentiful plain ones. */
    @GameTest(template = TEMPLATE)
    public static void cullKeepsTheTellingAspects(GameTestHelper helper) {
        // given the same amount of each, the intricate aspect is the telling one and the plain primal goes
        AspectList tied = AspectList.EMPTY
                .add(ModAspects.AIR, 10).add(ModAspects.EARTH, 10).add(ModAspects.FIRE, 10)
                .add(ModAspects.WATER, 10).add(ModAspects.ORDER, 10).add(ModAspects.ENTROPY, 10)
                .add(ModAspects.MAN, 10);
        helper.assertTrue(tied.size() == 7, "the test list should start with 7 aspects");

        AspectList culled = tied.cull();
        helper.assertTrue(culled.size() == AspectList.MAX_ASPECTS, "the cull should leave 6, left " + culled.size());
        helper.assertTrue(culled.contains(ModAspects.MAN), "humanus outweighs a primal at the same amount");

        // but sheer quantity still wins: a single point of humanus is not worth keeping over twenty of a primal
        AspectList lopsided = AspectList.EMPTY
                .add(ModAspects.AIR, 20).add(ModAspects.EARTH, 20).add(ModAspects.FIRE, 20)
                .add(ModAspects.WATER, 20).add(ModAspects.ORDER, 20).add(ModAspects.ENTROPY, 20)
                .add(ModAspects.MAN, 1);
        helper.assertTrue(!lopsided.cull().contains(ModAspects.MAN), "a lone point of humanus should have been dropped");

        helper.assertTrue(AspectList.EMPTY.add(ModAspects.AIR, 1).cull().size() == 1, "a short list should come through untouched");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void listSurvivesSavingAndLoading(GameTestHelper helper) {
        AspectList original = AspectList.EMPTY.add(ModAspects.SOUL, 7).add(ModAspects.FLUX, 2);

        Tag saved = AspectList.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        AspectList loaded = AspectList.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
        helper.assertTrue(loaded.equals(original), "the list changed on the way through nbt: " + loaded);

        // an empty list has to survive the trip too, since most things carry no aspects at all
        Tag emptySaved = AspectList.CODEC.encodeStart(NbtOps.INSTANCE, AspectList.EMPTY).getOrThrow();
        helper.assertTrue(AspectList.CODEC.parse(NbtOps.INSTANCE, emptySaved).getOrThrow().isEmpty(), "an empty list should stay empty");

        List<Holder<Aspect>> order = original.sortedByName();
        helper.assertTrue(order.getFirst().value() == ModAspects.SOUL.get(), "spiritus sorts before vitium by name");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void writtenDownItemsUseTheirOwnAspects(GameTestHelper helper) {
        AspectList iron = Aspects.of(Items.IRON_INGOT);
        helper.assertTrue(iron.get(ModAspects.METAL) == 4, "an iron ingot should be metallum 4, was " + iron);

        // the entry was written against the tag, so every ingot in it answers the same way
        helper.assertTrue(Aspects.of(Items.APPLE).get(ModAspects.PLANT) == 2, "an apple should be herba 2");
        helper.assertTrue(Aspects.of(Items.STICK).get(ModAspects.PLANT) == 1, "a stick should be herba 1");
        helper.succeed();
    }

    /** Nothing writes down a pickaxe, so its aspects have to come from the iron and sticks that make it. */
    @GameTest(template = TEMPLATE)
    public static void craftedItemsTraceBackThroughTheirRecipe(GameTestHelper helper) {
        AspectList pickaxe = Aspects.of(Items.IRON_PICKAXE);
        Alchemia.LOGGER.info("iron pickaxe traced back to {}", pickaxe);

        // three ingots at metallum 4 and two sticks at herba 1, less the quarter that crafting always loses
        helper.assertTrue(pickaxe.get(ModAspects.METAL) == 9, "the pickaxe should carry metallum 9, was " + pickaxe);
        helper.assertTrue(pickaxe.get(ModAspects.PLANT) == 1, "the pickaxe should carry herba 1, was " + pickaxe);
        helper.succeed();
    }

    /** A recipe making more than one splits its ingredients between them. */
    @GameTest(template = TEMPLATE)
    public static void makingSeveralAtOnceSplitsTheAspects(GameTestHelper helper) {
        AspectList plank = Aspects.of(Items.OAK_PLANKS);
        AspectList stairs = Aspects.of(Items.OAK_STAIRS);
        Alchemia.LOGGER.info("oak planks {} -> oak stairs {}", plank, stairs);

        // six planks make four stairs, so a stair is worth less than the plank it came from
        helper.assertTrue(stairs.total() <= plank.total(), "stairs should not out-weigh the planks they came from");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void ourOwnMaterialsAreWrittenDown(GameTestHelper helper) {
        helper.assertTrue(Aspects.of(ModItems.AMBER.get()).get(ModAspects.TRAP) == 2, "amber should be vinculum 2");
        helper.assertTrue(Aspects.of(ModItems.SHARDS.get(CrystalType.FIRE).get()).get(ModAspects.FIRE) == 2,
                "a fire shard should be ignis 2");
        helper.assertTrue(Aspects.of(ModItems.BALANCED_SHARD.get()).size() == AspectList.MAX_ASPECTS,
                "the balanced shard holds six aspects once culled");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void mobsCarryTheirOwnAspects(GameTestHelper helper) {
        helper.assertTrue(Aspects.of(EntityType.ZOMBIE).get(ModAspects.UNDEAD) == 4, "a zombie should be exanimis 4");
        helper.assertTrue(Aspects.of(EntityType.COW).get(ModAspects.BEAST) == 4, "a cow should be bestia 4");
        // nothing is written down for the ender dragon, and there is no recipe to fall back on
        helper.assertTrue(Aspects.of(EntityType.ENDER_DRAGON).isEmpty(), "an unlisted mob should come back empty");
        helper.succeed();
    }

    /** Bedrock has no entry and no recipe, so it must come back empty rather than blow up. */
    @GameTest(template = TEMPLATE)
    public static void unknownThingsComeBackEmpty(GameTestHelper helper) {
        helper.assertTrue(Aspects.of(Items.BEDROCK).isEmpty(), "bedrock should have no aspects");
        helper.assertTrue(Aspects.of(ItemStack.EMPTY).isEmpty(), "an empty stack should have no aspects");
        helper.succeed();
    }

    /** A tool carries instrumentum for being a tool, on top of whatever it was forged from. */
    @GameTest(template = TEMPLATE)
    public static void toolsAndArmourEarnTheirOwnAspects(GameTestHelper helper) {
        AspectList pickaxe = Aspects.of(Items.IRON_PICKAXE);
        Alchemia.LOGGER.info("iron pickaxe with bonuses: {}", pickaxe);
        helper.assertTrue(pickaxe.get(ModAspects.TOOL) == 3, "an iron pickaxe should be instrumentum 3, was " + pickaxe);

        helper.assertTrue(Aspects.of(Items.WOODEN_PICKAXE).get(ModAspects.TOOL) == 1, "a wooden pickaxe should be instrumentum 1");
        helper.assertTrue(Aspects.of(Items.DIAMOND_PICKAXE).get(ModAspects.TOOL) == 4, "a diamond pickaxe should be instrumentum 4");

        // a sword bristles rather than digs
        AspectList sword = Aspects.of(Items.IRON_SWORD);
        helper.assertTrue(sword.get(ModAspects.AVERSION) == 3, "an iron sword should be aversio 3, was " + sword);
        helper.assertTrue(sword.get(ModAspects.TOOL) == 0, "a sword is no tool");

        AspectList chestplate = Aspects.of(Items.IRON_CHESTPLATE);
        helper.assertTrue(chestplate.get(ModAspects.PROTECT) == 6, "an iron chestplate should be praemunio 6, was " + chestplate);

        AspectList bow = Aspects.of(Items.BOW);
        helper.assertTrue(bow.get(ModAspects.AVERSION) == 3 && bow.get(ModAspects.FLIGHT) == 1, "a bow should be aversio 3 and volatus 1");
        helper.succeed();
    }

    /** Enchantments belong to the one stack, so they must not leak into every other copy of the item. */
    @GameTest(template = TEMPLATE)
    public static void enchantmentsAddToTheOneStack(GameTestHelper helper) {
        Holder<Enchantment> sharpness = helper.getLevel().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SHARPNESS);

        ItemStack plain = new ItemStack(Items.IRON_SWORD);
        ItemStack keen = plain.copy();
        keen.enchant(sharpness, 4);

        AspectList plainAspects = Aspects.of(plain);
        AspectList keenAspects = Aspects.of(keen);
        Alchemia.LOGGER.info("plain sword {} vs sharpness IV {}", plainAspects, keenAspects);

        helper.assertTrue(keenAspects.get(ModAspects.AVERSION) == 4, "sharpness IV should lift aversio to 4, was " + keenAspects);
        helper.assertTrue(keenAspects.get(ModAspects.ENERGY) == 4, "four levels of enchanting should show as potentia 4");
        helper.assertTrue(plainAspects.get(ModAspects.ENERGY) == 0, "an unenchanted sword should carry no potentia");
        helper.succeed();
    }
}
