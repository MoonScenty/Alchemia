package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.BannerBlockEntity;
import me.moonscenty.alchemia.block.eldritch.EldritchAltarBlockEntity;
import me.moonscenty.alchemia.block.eldritch.EldritchObeliskBlockEntity;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.EldritchCrab;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModFeatures;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The eldritch rings: what is built, the altar's eyes, the obelisk, the banners, and what warp teaches. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class EldritchRingTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** On open stone, a ring is raised: the altar on the stone, the shut way over it, the obelisk over that. */
    @GameTest(template = TEMPLATE)
    public static void aRingIsRaisedOnOpenGround(GameTestHelper helper) {
        for (int x = 4; x <= 20; x++) {
            for (int z = 4; z <= 20; z++) {
                helper.setBlock(new BlockPos(x, 5, z), Blocks.STONE);
            }
        }
        BlockPos above = helper.absolutePos(new BlockPos(12, 6, 12));
        boolean raised = ModFeatures.ELDRITCH_RING.get().place(new FeaturePlaceContext<>(java.util.Optional.empty(),
                helper.getLevel(), helper.getLevel().getChunkSource().getGenerator(), helper.getLevel().random,
                above, NoneFeatureConfiguration.INSTANCE));
        helper.assertTrue(raised, "the ring was raised");
        helper.assertBlockPresent(ModBlocks.ELDRITCH_ALTAR.get(), new BlockPos(12, 6, 12));
        helper.assertBlockPresent(ModBlocks.ELDRITCH_PORTAL.get(), new BlockPos(12, 7, 12));
        helper.assertBlockPresent(ModBlocks.ELDRITCH_OBELISK.get(), new BlockPos(12, 8, 12));
        helper.assertBlockPresent(ModBlocks.ELDRITCH_CAP.get(), new BlockPos(15, 6, 13));
        helper.succeed();
    }

    /** Eyes go into an altar, four at most; the third makes it call up guardians. */
    @GameTest(template = TEMPLATE)
    public static void theAltarTakesFourEyes(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at, ModBlocks.ELDRITCH_ALTAR.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ELDRITCH_EYE.get(), 6));
        BlockPos absolute = helper.absolutePos(at);
        for (int i = 0; i < 6; i++) {
            helper.getLevel().getBlockState(absolute).useItemOn(player.getMainHandItem(), helper.getLevel(), player,
                    InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute),
                            net.minecraft.core.Direction.UP, absolute, false));
        }
        EldritchAltarBlockEntity altar = (EldritchAltarBlockEntity) helper.getLevel().getBlockEntity(absolute);
        helper.assertValueEqual(altar.eyes(), 4, "four eyes went in");
        helper.assertValueEqual(player.getMainHandItem().getCount(), 2, "and the other two stayed in hand");
        helper.assertTrue(altar.isSpawner(), "and it has woken to call up guardians");
        helper.succeed();
    }

    /** What stands near an obelisk from the Outer Lands is kept strong. */
    @GameTest(template = TEMPLATE)
    public static void anObeliskKeepsTheOuterLandsStrong(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at, ModBlocks.ELDRITCH_OBELISK.get());
        EldritchCrab crab = new EldritchCrab(helper.getLevel());
        crab.setData(ModAttachments.CHAMPION, Champions.ORDINARY);
        crab.moveTo(helper.absoluteVec(new BlockPos(6, 2, 4).getBottomCenter()));
        crab.setNoAi(true);
        helper.getLevel().addFreshEntity(crab);
        BlockPos absolute = helper.absolutePos(at);
        EldritchObeliskBlockEntity.tick(helper.getLevel(), absolute, helper.getLevel().getBlockState(absolute),
                (EldritchObeliskBlockEntity) helper.getLevel().getBlockEntity(absolute));
        helper.assertTrue(crab.hasEffect(MobEffects.DAMAGE_BOOST), "the crab is strong");
        helper.succeed();
    }

    /** A phial paints its aspect on a dyed banner, but not on the cult's. */
    @GameTest(template = TEMPLATE)
    public static void aPhialPaintsABanner(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at, ModBlocks.BANNER.get());
        BlockPos absolute = helper.absolutePos(at);
        BannerBlockEntity banner = (BannerBlockEntity) helper.getLevel().getBlockEntity(absolute);
        banner.setColour(DyeColor.RED);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND,
                PhialItem.filled(ModItems.PHIAL.get().getDefaultInstance(), ModAspects.FIRE));
        helper.getLevel().getBlockState(absolute).useItemOn(player.getMainHandItem(), helper.getLevel(), player,
                InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute),
                        net.minecraft.core.Direction.NORTH, absolute, false));
        helper.assertTrue(banner.aspect() != null && banner.aspect().value() == ModAspects.FIRE.value(),
                "fire is painted on it");
        helper.succeed();
    }

    /** Warp that has settled in past twenty-five teaches the minor lore, past fifty the major. */
    @GameTest(template = TEMPLATE)
    public static void settledWarpTeaches(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        WarpHandler.epiphany(player, 30);
        helper.assertTrue(PlayerKnowledge.of(player).hasResearch(Alchemia.id("eldritch_minor")), "the minor lore");
        helper.assertFalse(PlayerKnowledge.of(player).hasResearch(Alchemia.id("eldritch_major")), "not the major");
        WarpHandler.epiphany(player, 51);
        helper.assertTrue(PlayerKnowledge.of(player).hasResearch(Alchemia.id("eldritch_major")), "and then the major");
        helper.succeed();
    }
}
