package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.entity.TaintCrawler;
import me.moonscenty.alchemia.entity.TaintSwarm;
import me.moonscenty.alchemia.entity.Taintacle;
import me.moonscenty.alchemia.entity.TaintacleTiny;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The creatures of the taint, step eleven: the crawler, the swarm and the taintacle, and the geyser.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TaintMobTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** A floor of this under the given spots. */
    private static void floor(GameTestHelper helper, net.minecraft.world.level.block.Block block, BlockPos... over) {
        for (BlockPos at : over) {
            helper.setBlock(at.below(), block);
        }
    }

    private static void flux(GameTestHelper helper, BlockPos at, int flux) {
        helper.getLevel().getChunkAt(helper.absolutePos(at)).setData(ModAuraAttachment.AURA,
                new AuraChunk(AuraGeneration.BASE, AspectList.EMPTY.add(ModAspects.FLUX, flux)));
    }

    /** Wherever a crawler crawls, it leaves fibres behind. */
    @GameTest(template = TEMPLATE)
    public static void aCrawlerLeavesFibres(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        floor(helper, Blocks.STONE, at);
        helper.spawnWithNoFreeWill(ModEntities.TAINT_CRAWLER.get(), at);
        helper.runAfterDelay(10, () -> {
            helper.assertBlockPresent(ModBlocks.TAINT_FIBRE.get(), at);
            helper.succeed();
        });
    }

    /** The creatures of the taint are all on one side. */
    @GameTest(template = TEMPLATE)
    public static void taintedCreaturesKeepTogether(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        floor(helper, ModBlocks.TAINT_SOIL.get(), at, at.east(3));
        TaintCrawler crawler = helper.spawnWithNoFreeWill(ModEntities.TAINT_CRAWLER.get(), at);
        Taintacle tentacle = helper.spawnWithNoFreeWill(ModEntities.TAINTACLE.get(), at.east(3));
        helper.assertFalse(crawler.canAttack(tentacle), "a crawler leaves a taintacle be");
        helper.assertFalse(tentacle.canAttack(crawler), "and the other way round");
        helper.assertTrue(crawler.isAlliedTo(tentacle), "they are on one side");
        helper.succeed();
    }

    /** A taintacle stays where it rose, and withers away off the taint. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aTaintacleIsRooted(GameTestHelper helper) {
        BlockPos rooted = new BlockPos(3, 2, 3);
        BlockPos stranded = new BlockPos(9, 2, 9);
        floor(helper, ModBlocks.TAINT_SOIL.get(), rooted);
        floor(helper, Blocks.STONE, stranded);
        Taintacle onTaint = helper.spawnWithNoFreeWill(ModEntities.TAINTACLE.get(), rooted);
        Taintacle offTaint = helper.spawnWithNoFreeWill(ModEntities.TAINTACLE.get(), stranded);
        Vec3 was = onTaint.position();
        // either may have risen a champion, which is not whole to begin with, so each is held to how it began
        float onAtFirst = onTaint.getHealth();
        float offAtFirst = offTaint.getHealth();
        onTaint.setDeltaMovement(1.0, 0.0, 1.0);
        helper.runAfterDelay(45, () -> {
            helper.assertTrue(Math.abs(onTaint.getX() - was.x) < 0.01 && Math.abs(onTaint.getZ() - was.z) < 0.01,
                    "it does not move");
            helper.assertTrue(onTaint.getHealth() >= onAtFirst, "on the taint it does not wither");
            helper.assertTrue(offTaint.getHealth() < offAtFirst, "off it, it withers");
            helper.succeed();
        });
    }

    /** A taintacle sends a tendril up under something on the taint some way off. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aTaintacleSendsATendril(GameTestHelper helper) {
        BlockPos rooted = new BlockPos(2, 2, 2);
        BlockPos prey = new BlockPos(10, 2, 2);
        floor(helper, ModBlocks.TAINT_SOIL.get(), rooted, prey);
        Taintacle tentacle = helper.spawn(ModEntities.TAINTACLE.get(), rooted);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, prey);
        tentacle.setTarget(pig);
        helper.runAfterDelay(45, () -> {
            var tendrils = helper.getLevel().getEntitiesOfClass(TaintacleTiny.class,
                    pig.getBoundingBox().inflate(2.0));
            helper.assertFalse(tendrils.isEmpty(), "a tendril came up under the pig");
            helper.succeed();
        });
    }

    /** A swarm's bite weakens, and does not knock what it bites about. */
    @GameTest(template = TEMPLATE)
    public static void aSwarmWeakens(GameTestHelper helper) {
        BlockPos at = new BlockPos(5, 2, 5);
        floor(helper, Blocks.STONE, at);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, at);
        TaintSwarm swarm = helper.spawnWithNoFreeWill(ModEntities.TAINT_SWARM.get(), at);
        pig.setDeltaMovement(Vec3.ZERO);
        swarm.bite(pig, swarm.distanceTo(pig));
        helper.assertTrue(pig.hasEffect(MobEffects.WEAKNESS), "the pig is weakened");
        helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "and bitten");
        helper.assertTrue(pig.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6, "but not knocked back");
        helper.succeed();
    }

    /** A growth that goes bursts: a crawler, or a breath of flux. */
    @GameTest(template = TEMPLATE)
    public static void aGrowthBursts(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        floor(helper, ModBlocks.TAINT_SOIL.get(), at);
        flux(helper, at, 10);
        helper.setBlock(at, ModBlocks.TAINT_FIBRE.get().defaultBlockState().setValue(TaintFibreBlock.DOWN, true)
                .setValue(TaintFibreBlock.GROWTH, 1));
        helper.setBlock(at, Blocks.AIR);
        int crawlers = helper.getLevel().getEntitiesOfClass(TaintCrawler.class, helper.getBounds().inflate(2)).size();
        int flux = AuraHandler.get(helper.getLevel(), helper.absolutePos(at), ModAspects.FLUX);
        helper.assertTrue(crawlers == 1 && flux == 10 || crawlers == 0 && flux == 11,
                "a crawler or a point of flux, got " + crawlers + " and " + flux);
        helper.succeed();
    }

    /** A geyser with no flux about it gives up, into a puddle of goo. */
    @GameTest(template = TEMPLATE)
    public static void aStarvedGeyserTurnsToGoo(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        floor(helper, Blocks.STONE, at);
        flux(helper, at, 0);
        helper.setBlock(at, ModBlocks.TAINT_GEYSER.get());
        BlockPos where = helper.absolutePos(at);
        RandomSource random = RandomSource.create(2L);
        for (int turn = 0; turn < 200 && helper.getLevel().getBlockState(where).is(ModBlocks.TAINT_GEYSER.get());
                turn++) {
            // a geyser breathes flux into thin air, which would keep it alive; the air is kept thin
            flux(helper, at, 0);
            helper.getLevel().getBlockState(where).randomTick(helper.getLevel(), where, random);
        }
        helper.assertBlockPresent(ModBlocks.FLUX_GOO.get(), at);
        helper.succeed();
    }

    /** A chunk thick with flux can let out a crawler on the ground. */
    @GameTest(template = TEMPLATE)
    public static void fluxLetsOutACrawler(GameTestHelper helper) {
        BlockPos ground = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.assertTrue(TaintCrawler.fromFlux(helper.getLevel(), ground, RandomSource.create(1L)), "one came");
        var found = helper.getLevel().getEntitiesOfClass(TaintCrawler.class, helper.getBounds().inflate(2));
        helper.assertTrue(found.size() == 1, "and it is there");
        helper.succeed();
    }
}
