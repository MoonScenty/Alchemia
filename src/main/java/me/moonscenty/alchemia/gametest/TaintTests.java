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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The taint is the one thing in the mod that eats the world, so it has to be shown to take ground only where it
 * should, to give it back when its flux runs out, and never to get anything for free.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TaintTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 4, 8);
    private static final int BASE = AuraGeneration.BASE;

    private static void flux(GameTestHelper helper, int amount) {
        helper.getLevel().getChunkAt(helper.absolutePos(HERE)).setData(ModAuraAttachment.AURA,
                new AuraChunk(BASE, AspectList.EMPTY.add(ModAspects.FLUX, amount)));
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

    /** Dirt with taint on most sides is eaten and turned to tainted soil. */
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

    /** Anything too hard for the taint is beyond it, however well surrounded. */
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

    /**
     * Fibres creep across open ground, clinging to whatever solid thing is beside them. They are the front, so the
     * check stops the moment they arrive: left longer, they eat the stone and wither, which is the next test.
     */
    @GameTest(template = TEMPLATE)
    public static void fibresTakeHoldOnOpenGround(GameTestHelper helper) {
        flux(helper, BASE);
        helper.setBlock(HERE, ModBlocks.TAINT_SOIL.get());
        // a stone floor next door, with air above it, is exactly where fibres go
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

    /** With no flux to feed it the taint does not move an inch. */
    @GameTest(template = TEMPLATE)
    public static void nothingSpreadsWithoutFlux(GameTestHelper helper) {
        flux(helper, 0);
        helper.setBlock(HERE, ModBlocks.TAINT_SOIL.get());
        helper.setBlock(HERE.east(), Blocks.STONE);

        spreadHard(helper, HERE, 4L);
        helper.assertBlockPresent(Blocks.AIR, HERE.east().above());
        helper.succeed();
    }

    /** Starved of flux, tainted soil goes back to being dirt. */
    @GameTest(template = TEMPLATE)
    public static void starvedSoilTurnsBackToDirt(GameTestHelper helper) {
        flux(helper, 0);
        helper.setBlock(HERE, ModBlocks.TAINT_SOIL.get());

        RandomSource random = RandomSource.create(5L);
        for (int i = 0; i < 100 && helper.getBlockState(HERE).is(ModBlocks.TAINT_SOIL.get()); i++) {
            helper.getBlockState(HERE).randomTick(helper.getLevel(), helper.absolutePos(HERE), random);
        }
        helper.assertBlockPresent(Blocks.DIRT, HERE);
        helper.succeed();
    }

    /** A plain fibre with only taint to hold on to has done its job and withers. */
    @GameTest(template = TEMPLATE)
    public static void fibresWitherOnceTheGroundUnderThemHasTurned(GameTestHelper helper) {
        flux(helper, BASE);
        // which fibres sprout something is settled by where they stand, so find a spot that grows nothing
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

        // the stone it clings to is eaten from under it
        helper.setBlock(spot.below(), ModBlocks.TAINT_ROCK.get());
        helper.assertBlockPresent(Blocks.AIR, spot);
        helper.succeed();
    }

    /** A chunk thick with flux lets it out as trouble, and pays for it. */
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
        // nobody is about to be warped or sickened, so the only trouble that can come of it is the node
        for (int i = 0; i < 100 && happened == null; i++) {
            happened = FluxEvents.strikeAt(helper.getLevel(), at, random);
        }
        helper.assertTrue(happened != null, "a chunk this thick with flux should have let something out");
        // nobody is here to be warped or sickened, so it has to have been the weather or the node
        helper.assertTrue(!happened.name().equals("warp") && !happened.name().equals("sickness"),
                "with nobody about, neither warp nor sickness should have come of it");
        helper.assertTrue(fluxLeft(helper) == before - happened.cost(), "and the chunk should have paid for it");
        helper.succeed();
    }
}
