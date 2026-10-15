package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.PatternCrafterBlock;
import me.moonscenty.alchemia.block.entity.PatternCrafterBlockEntity;
import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.block.entity.ArcaneEarBlockEntity;
import me.moonscenty.alchemia.item.SanitySoapItem;
import me.moonscenty.alchemia.item.SinisterStoneItem;
import me.moonscenty.alchemia.item.VisDiscount;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Step 14C: vis exhaustion, the soap, candles, the pattern crafter, the lodestone, the foci and the rest. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class LeftoverTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** Each level of vis exhaustion, of either kind, takes ten parts back off a discount; the worse kind counts. */
    @GameTest(template = TEMPLATE)
    public static void visExhaustionRaisesWandPrices(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertValueEqual(VisDiscount.worn(player), 0, "nothing worn, nothing ailing");
        player.addEffect(new MobEffectInstance(ModEffects.FLUX_FLU, 600, 1));
        helper.assertValueEqual(VisDiscount.worn(player), -20, "two levels of the flu");
        player.addEffect(new MobEffectInstance(ModEffects.FLUX_PHAGE, 600, 2));
        helper.assertValueEqual(VisDiscount.worn(player), -30, "the phage is worse");
        helper.assertTrue(Math.abs(VisDiscount.rate(1.0F, player) - 1.3F) < 1.0E-4F, "so a wand pays more");
        helper.succeed();
    }

    /** The phage passes on a level weaker, and at its last level only as the plain flu. */
    @GameTest(template = TEMPLATE, timeoutTicks = 120)
    public static void fluxPhageSpreadsWeaker(GameTestHelper helper) {
        Pig carrier = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 2, 4));
        Pig caught = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(5, 2, 4));
        Pig last = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(12, 2, 12));
        Pig after = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(13, 2, 12));
        carrier.addEffect(new MobEffectInstance(ModEffects.FLUX_PHAGE, 6000, 1));
        last.addEffect(new MobEffectInstance(ModEffects.FLUX_PHAGE, 6000, 0));
        helper.succeedWhen(() -> {
            MobEffectInstance got = caught.getEffect(ModEffects.FLUX_PHAGE);
            helper.assertTrue(got != null && got.getAmplifier() == 0, "caught the phage, a level weaker");
            helper.assertTrue(after.hasEffect(ModEffects.FLUX_FLU) && !after.hasEffect(ModEffects.FLUX_PHAGE),
                    "the last level passes on only the flu");
        });
    }

    /** Wading in flux goo drains: vis exhaustion, worse the deeper. */
    @GameTest(template = TEMPLATE, timeoutTicks = 60)
    public static void fluxGooExhausts(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at.below(), Blocks.STONE);
        helper.setBlock(at, FluxGooBlock.of(ModBlocks.FLUX_GOO.get(), FluxGooBlock.FULL));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, at);
        helper.succeedWhen(() -> {
            MobEffectInstance got = pig.getEffect(ModEffects.FLUX_FLU);
            helper.assertTrue(got != null && got.getAmplifier() == FluxGooBlock.FULL / 3, "exhausted, deep goo");
        });
    }

    /** A finished wash takes all temporary warp, and a point of sticky warp if the roll is under the odds. */
    @GameTest(template = TEMPLATE)
    public static void soapWashesWarp(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        WarpHandler.add(player, WarpData.Kind.TEMPORARY, 5);
        WarpHandler.add(player, WarpData.Kind.STICKY, 3);
        WarpHandler.add(player, WarpData.Kind.PERMANENT, 2);
        SanitySoapItem.wash(player, 0.9F);
        helper.assertValueEqual(WarpData.of(player).of(WarpData.Kind.TEMPORARY), 0, "temporary warp washed out");
        helper.assertValueEqual(WarpData.of(player).of(WarpData.Kind.STICKY), 3, "a bad roll leaves sticky warp");
        SanitySoapItem.wash(player, 0.1F);
        helper.assertValueEqual(WarpData.of(player).of(WarpData.Kind.STICKY), 2, "a good roll takes a point");
        player.addEffect(new MobEffectInstance(ModEffects.WARP_WARD, 600, 0));
        SanitySoapItem.wash(player, 0.5F);
        helper.assertValueEqual(WarpData.of(player).of(WarpData.Kind.STICKY), 1, "warp ward makes it likelier");
        helper.assertValueEqual(WarpData.of(player).of(WarpData.Kind.PERMANENT), 2, "never the permanent");
        helper.succeed();
    }

    /** A candle burns bright, steadies an infusion, and falls when what it stands on goes. */
    @GameTest(template = TEMPLATE)
    public static void tallowCandleStandsAndBurns(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at.below(), Blocks.STONE);
        var candle = ModBlocks.TALLOW_CANDLES.get(DyeColor.RED).get();
        helper.setBlock(at, candle);
        helper.assertValueEqual(helper.getBlockState(at).getLightEmission(), 14, "bright enough to read by");
        helper.assertTrue(helper.getBlockState(at).is(ModTags.Blocks.STABILISES_INFUSION), "steadies an infusion");
        helper.setBlock(at.below(), Blocks.AIR);
        helper.assertBlockNotPresent(candle, at);
        helper.succeed();
    }

    /** Nine nuggets the whole grid; two planks stood one on the other make sticks; nothing else is touched. */
    @GameTest(template = TEMPLATE)
    public static void patternCrafterCraftsInItsPattern(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 3, 4);
        helper.setBlock(at.below(), Blocks.CHEST);
        helper.setBlock(at, ModBlocks.PATTERN_CRAFTER.get());
        helper.setBlock(at.above(), Blocks.CHEST);
        ChestBlockEntity above = (ChestBlockEntity) helper.getBlockEntity(at.above());
        ChestBlockEntity below = (ChestBlockEntity) helper.getBlockEntity(at.below());
        PatternCrafterBlockEntity crafter = (PatternCrafterBlockEntity) helper.getBlockEntity(at);
        above.setItem(0, new ItemStack(Items.IRON_NUGGET, 5));
        above.setItem(1, new ItemStack(Items.IRON_NUGGET, 5));
        helper.assertTrue(crafter.craft(helper.getLevel()), "the full grid of nuggets");
        helper.assertValueEqual(below.countItem(Items.IRON_INGOT), 1, "an ingot below");
        helper.assertValueEqual(above.countItem(Items.IRON_NUGGET), 1, "one nugget left");
        for (int turn = 0; turn < 3; turn++) {
            crafter.cycle();
        }
        above.setItem(2, new ItemStack(Items.OAK_PLANKS, 2));
        helper.assertTrue(crafter.craft(helper.getLevel()), "two planks stood on end");
        helper.assertValueEqual(below.countItem(Items.STICK), 4, "four sticks below");
        helper.assertFalse(crafter.craft(helper.getLevel()), "nothing left that makes anything");
        helper.assertTrue(PatternCrafterBlock.onDial(helper.getBlockState(at), new Vec3(0.5, 0.25, 0.8)),
                "the dial of one set facing north is on its south side");
        helper.succeed();
    }

    /** The lodestone feels what is ahead, within its cone and its reach, and nothing behind. */
    @GameTest(template = TEMPLATE)
    public static void sinisterStoneFeelsWhatIsAhead(GameTestHelper helper) {
        Vec3 eye = Vec3.ZERO;
        Vec3 north = new Vec3(0, 0, -1);
        helper.assertTrue(SinisterStoneItem.aimedAt(eye, north, new Vec3(10, 0, -100)), "a little off the line");
        helper.assertFalse(SinisterStoneItem.aimedAt(eye, north, new Vec3(60, 0, -100)), "too far off it");
        helper.assertFalse(SinisterStoneItem.aimedAt(eye, north, new Vec3(0, 0, -300)), "too far along it");
        helper.assertFalse(SinisterStoneItem.aimedAt(eye, north, new Vec3(0, 0, 50)), "behind");
        helper.succeed();
    }

    /** Every focus but the pech's and the shard's is made, and the vampire bats wait on their research. */
    @GameTest(template = TEMPLATE)
    public static void fociAreMadeAndVampiresWait(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (String focus : java.util.List.of("fire", "shock", "frost", "excavation", "trade", "builder", "grapple",
                "primal", "hole", "hellbat")) {
            helper.assertTrue(recipes.byKey(Alchemia.id("focus_" + focus)).isPresent(), "a recipe for " + focus);
        }
        ItemStack hellbat = new ItemStack(ModItems.FOCI.get("hellbat").get());
        helper.assertTrue(FocusUpgrades.offers(hellbat, 5).contains(FocusUpgrade.VAMPIRE_BATS), "rank five offers it");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertFalse(FocusUpgrades.offers(hellbat, 5, player).contains(FocusUpgrade.VAMPIRE_BATS),
                "but not to somebody who has not studied it");
        helper.succeed();
    }

    /** A note block and an arcane ear each tell goggles their note. */
    @GameTest(template = TEMPLATE)
    public static void notesAreReadable(GameTestHelper helper) {
        BlockPos block = new BlockPos(4, 2, 4);
        helper.setBlock(block, Blocks.NOTE_BLOCK.defaultBlockState()
                .setValue(net.minecraft.world.level.block.NoteBlock.NOTE, 7));
        helper.assertValueEqual(ArcaneEarBlockEntity.noteAt(helper.getBlockState(block), null), 7, "the note block's note");
        helper.assertValueEqual(ArcaneEarBlockEntity.noteAt(Blocks.STONE.defaultBlockState(), null), -1, "stone has none");
        helper.succeed();
    }
}
