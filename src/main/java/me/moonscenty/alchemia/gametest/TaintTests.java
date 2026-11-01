package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.FluxEvents;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.block.taint.TaintSpread;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TaintTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 4, 8);
    private static final int BASE = AuraGeneration.BASE;

    // fill all nine chunks: the structure may place the test spot in a neighbouring chunk
    private static void flux(GameTestHelper helper, int amount) {
        BlockPos middle = helper.absolutePos(HERE);
        for (int east = -16; east <= 16; east += 16) {
            for (int south = -16; south <= 16; south += 16) {
                helper.getLevel().getChunkAt(middle.offset(east, 0, south)).setData(ModAuraAttachment.AURA,
                        new AuraChunk(BASE, AspectList.EMPTY.add(ModAspects.FLUX, amount)));
            }
        }
    }

    private static int fluxLeft(GameTestHelper helper) {
        return AuraHandler.get(helper.getLevel(), helper.absolutePos(HERE), ModAspects.FLUX);
    }

    private static void spreadHard(GameTestHelper helper, BlockPos from, long seed) {
        RandomSource random = RandomSource.create(seed);
        for (int i = 0; i < 400; i++) {
            TaintSpread.spread(helper.getLevel(), helper.absolutePos(from), random);
        }
    }

    @GameTest(template = TEMPLATE)
    public static void hemmedDirtTurnsToTaintedSoil(GameTestHelper helper) {
        flux(helper, BASE);
        helper.setBlock(HERE, Blocks.DIRT);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            helper.setBlock(HERE.relative(side), ModBlocks.TAINT_SOIL.get());
        }
        helper.setBlock(HERE.below(), ModBlocks.TAINT_ROCK.get());

        spreadHard(helper, HERE.north(), 1L);
        helper.assertBlockPresent(ModBlocks.TAINT_SOIL.get(), HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void obsidianIsBeyondTheTaint(GameTestHelper helper) {
        flux(helper, BASE);
        helper.setBlock(HERE, Blocks.OBSIDIAN);
        for (Direction side : Direction.values()) {
            helper.setBlock(HERE.relative(side), ModBlocks.TAINT_SOIL.get());
        }

        spreadHard(helper, HERE.north(), 2L);
        helper.assertBlockPresent(Blocks.OBSIDIAN, HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fibresTakeHoldOnOpenGround(GameTestHelper helper) {
        flux(helper, BASE);
        helper.setBlock(HERE, ModBlocks.TAINT_SOIL.get());
        helper.setBlock(HERE.east(), Blocks.STONE);
        BlockPos onto = HERE.east().above();

        RandomSource random = RandomSource.create(3L);
        BlockState grown = helper.getBlockState(onto);
        for (int i = 0; i < 400 && !grown.is(ModBlocks.TAINT_FIBRE.get()); i++) {
            TaintSpread.spread(helper.getLevel(), helper.absolutePos(HERE), random);
            grown = helper.getBlockState(onto);
        }
        helper.assertTrue(grown.is(ModBlocks.TAINT_FIBRE.get()), "fibres should have crept onto the stone, found " + grown);
        helper.assertTrue(grown.getValue(TaintFibreBlock.DOWN), "and they should be clinging to the floor under them");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void nothingSpreadsWithoutFlux(GameTestHelper helper) {
        flux(helper, 0);
        helper.setBlock(HERE, ModBlocks.TAINT_SOIL.get());
        helper.setBlock(HERE.east(), Blocks.STONE);

        spreadHard(helper, HERE, 4L);
        helper.assertBlockPresent(Blocks.AIR, HERE.east().above());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void starvedSoilCrumblesToDust(GameTestHelper helper) {
        flux(helper, 0);
        helper.setBlock(HERE, ModBlocks.TAINT_SOIL.get());

        RandomSource random = RandomSource.create(5L);
        for (int i = 0; i < 100 && helper.getBlockState(HERE).is(ModBlocks.TAINT_SOIL.get()); i++) {
            helper.getBlockState(HERE).randomTick(helper.getLevel(), helper.absolutePos(HERE), random);
        }
        helper.assertBlockPresent(ModBlocks.TAINT_DUST.get(), HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fibresWitherOnceTheGroundUnderThemHasTurned(GameTestHelper helper) {
        flux(helper, BASE);
        // find a spot whose fibres grow nothing
        BlockPos spot = null;
        for (int x = 2; x < 30 && spot == null; x++) {
            BlockPos candidate = new BlockPos(x, 4, 8);
            helper.setBlock(candidate.below(), Blocks.STONE);
            BlockState fibres = TaintSpread.fibres(helper.getLevel(), helper.absolutePos(candidate));
            if (fibres.getValue(TaintFibreBlock.GROWTH) == 0) {
                spot = candidate;
                helper.setBlock(candidate, fibres);
            }
        }
        helper.assertTrue(spot != null, "somewhere along a row of thirty there should be a plain fibre");
        helper.assertBlockPresent(ModBlocks.TAINT_FIBRE.get(), spot);

        helper.setBlock(spot.below(), ModBlocks.TAINT_ROCK.get());
        helper.assertBlockPresent(Blocks.AIR, spot);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aChunkThickWithFluxKnocksANodeAbout(GameTestHelper helper) {
        flux(helper, BASE * 3);
        AuraNode node = new AuraNode(helper.getLevel());
        BlockPos at = helper.absolutePos(HERE);
        node.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        node.drawUp(RandomSource.create(6L));
        node.setType(NodeType.PLAIN);
        helper.getLevel().addFreshEntity(node);

        int before = fluxLeft(helper);
        RandomSource random = RandomSource.create(7L);
        FluxEvents.Trouble happened = null;
        for (int i = 0; i < 100 && happened == null; i++) {
            happened = FluxEvents.strikeAt(helper.getLevel(), at, random);
        }
        helper.assertTrue(happened != null, "a chunk this thick with flux should have let something out");
        helper.assertTrue(!happened.name().equals("warp") && !happened.name().equals("sickness"),
                "with nobody about, neither warp nor sickness should have come of it");
        helper.assertTrue(fluxLeft(helper) == before - happened.cost(), "and the chunk should have paid for it");
        helper.succeed();
    }

    private static final BooleanProperty[] SIDES = {
            TaintFibreBlock.NORTH, TaintFibreBlock.EAST, TaintFibreBlock.SOUTH,
            TaintFibreBlock.WEST, TaintFibreBlock.UP, TaintFibreBlock.DOWN,
    };

    @GameTest(template = TEMPLATE)
    public static void fibresTakeHoldOfLeaves(GameTestHelper helper) {
        for (Direction side : Direction.values()) {
            helper.setBlock(HERE.relative(side), Blocks.OAK_LEAVES);
        }
        BlockState fibres = TaintSpread.fibres(helper.getLevel(), helper.absolutePos(HERE));
        for (BooleanProperty side : SIDES) {
            helper.assertTrue(fibres.getValue(side), "a fibre walled in by leaves should hold on at " + side.getName());
        }

        helper.setBlock(HERE, fibres);
        helper.assertTrue(fibres.canSurvive(helper.getLevel(), helper.absolutePos(HERE)), "and it should stay there");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aFibreWithNothingToHoldGoes(GameTestHelper helper) {
        helper.setBlock(HERE.below(), Blocks.STONE);
        helper.setBlock(HERE, TaintSpread.fibres(helper.getLevel(), helper.absolutePos(HERE)));
        helper.assertBlockPresent(ModBlocks.TAINT_FIBRE.get(), HERE);

        helper.setBlock(HERE.below(), Blocks.AIR);
        helper.assertBlockPresent(Blocks.AIR, HERE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void walkingOnTaintedGroundPoisons(GameTestHelper helper) {
        BlockPos ground = new BlockPos(4, 1, 4);
        helper.setBlock(ground, ModBlocks.TAINT_SOIL.get());
        BlockPos at = helper.absolutePos(ground);
        var state = helper.getLevel().getBlockState(at);
        var pig = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.PIG, ground.above());
        var zombie = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, ground.above());
        for (int step = 0; step < 10000; step++) {
            state.getBlock().stepOn(helper.getLevel(), at, state, pig);
            state.getBlock().stepOn(helper.getLevel(), at, state, zombie);
        }
        helper.assertTrue(pig.hasEffect(me.moonscenty.alchemia.player.effect.ModEffects.TAINT_POISON),
                "the pig was poisoned");
        helper.assertFalse(zombie.hasEffect(me.moonscenty.alchemia.player.effect.ModEffects.TAINT_POISON),
                "the zombie was not");
        pig.discard();
        zombie.discard();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void taintPoisonHealsTheTaintsOwn(GameTestHelper helper) {
        var poison = me.moonscenty.alchemia.player.effect.ModEffects.TAINT_POISON;
        var crawler = helper.spawnWithNoFreeWill(me.moonscenty.alchemia.registry.ModEntities.TAINT_CRAWLER.get(),
                new BlockPos(3, 2, 3));
        var pig = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.PIG, new BlockPos(6, 2, 6));
        crawler.setHealth(crawler.getMaxHealth() / 2);
        float crawlerWas = crawler.getHealth();
        float pigWas = pig.getHealth();
        poison.value().applyEffectTick(crawler, 0);
        poison.value().applyEffectTick(pig, 0);
        helper.assertTrue(crawler.getHealth() > crawlerWas, "the crawler was healed");
        helper.assertTrue(pig.getHealth() < pigWas, "the pig was hurt");
        helper.succeed();
    }
}
