package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.BiomePaint;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.block.taint.TaintSpread;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.worldgen.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The mod's three lands: that they are there and sorted as the original sorted them, that a place can be turned into
 * one and back, and that the taint and the dark nodes do the turning.
 * <p>
 * Every test that paints puts the land back before it finishes, so nothing it leaves behind can colour another test.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class BiomeTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 4, 8);

    private static Holder<Biome> biome(GameTestHelper helper, ResourceKey<Biome> key) {
        return BiomePaint.named(helper.getLevel(), key).orElseThrow();
    }

    private static boolean in(GameTestHelper helper, ResourceKey<Biome> key, TagKey<Biome> tag) {
        return biome(helper, key).is(tag);
    }

    private static boolean landIs(GameTestHelper helper, BlockPos at, ResourceKey<Biome> key) {
        return BiomePaint.at(helper.getLevel(), helper.absolutePos(at)).is(key);
    }

    private static void putBack(GameTestHelper helper, BlockPos at) {
        ServerLevel level = helper.getLevel();
        BlockPos where = helper.absolutePos(at);
        BiomePaint.paint(level, where, BiomePaint.generated(level, where), false);
    }

    /** All three lands are there, and sorted as the original sorted them. */
    @GameTest(template = TEMPLATE)
    public static void theLandsAreThereAndSorted(GameTestHelper helper) {
        for (ResourceKey<Biome> land : java.util.List.of(ModBiomes.MAGICAL_FOREST, ModBiomes.EERIE,
                ModBiomes.TAINTED_LAND)) {
            helper.assertTrue(helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).containsKey(land),
                    land.location() + " should be a biome");
            helper.assertTrue(in(helper, land, Tags.Biomes.IS_MAGICAL), land.location() + " should be magical");
        }
        helper.assertTrue(in(helper, ModBiomes.MAGICAL_FOREST, Tags.Biomes.IS_FOREST), "the forest is a forest");
        helper.assertTrue(in(helper, ModBiomes.EERIE, Tags.Biomes.IS_SPOOKY), "the eerie land is spooky");
        helper.assertTrue(in(helper, ModBiomes.TAINTED_LAND, Tags.Biomes.IS_WASTELAND), "the tainted land is waste");
        helper.succeed();
    }

    /** A dark node has the eerie land to make. */
    @GameTest(template = TEMPLATE)
    public static void darkNodesMakeTheLandEerie(GameTestHelper helper) {
        Holder<Biome> into = BiomePaint.oneOf(helper.getLevel(), ModTags.Biomes.NODE_DARKENS_INTO,
                RandomSource.create(1L)).orElseThrow();
        helper.assertTrue(into.is(ModBiomes.EERIE), "a dark node should make eerie land, not " + into);
        helper.succeed();
    }

    /** A patch of land painted over is given back what it was made as, and only if it is still what was painted. */
    @GameTest(template = TEMPLATE)
    public static void paintedLandCanBeGivenBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(HERE);
        Holder<Biome> made = BiomePaint.generated(level, at);

        helper.assertTrue(BiomePaint.paint(level, at, biome(helper, ModBiomes.TAINTED_LAND), false),
                "the land should have been painted");
        helper.assertTrue(landIs(helper, HERE, ModBiomes.TAINTED_LAND), "the land should now be tainted");
        helper.assertFalse(BiomePaint.restore(level, at, ModBiomes.EERIE, false),
                "tainted land is not eerie land, so asking to undo eerie land should leave it be");
        helper.assertTrue(BiomePaint.restore(level, at, ModBiomes.TAINTED_LAND, false),
                "the tainted land should have been given back");
        helper.assertTrue(BiomePaint.at(level, at).value() == made.value(), "the land should be what it was made as");
        helper.succeed();
    }

    /** Where the taint is spreading, the land under it turns to tainted land. */
    @GameTest(template = TEMPLATE)
    public static void spreadingTaintSpoilsTheLand(GameTestHelper helper) {
        flux(helper, AuraGeneration.BASE);
        helper.setBlock(HERE, ModBlocks.TAINT_SOIL.get());

        TaintSpread.spread(helper.getLevel(), helper.absolutePos(HERE), RandomSource.create(2L));
        boolean spoiled = landIs(helper, HERE, ModBiomes.TAINTED_LAND);
        putBack(helper, HERE);
        helper.assertTrue(spoiled, "the land under spreading taint should have turned");
        helper.succeed();
    }

    /** When tainted soil starves and dies, the land it had spoiled is given back. */
    @GameTest(template = TEMPLATE)
    public static void starvingTaintGivesTheLandBack(GameTestHelper helper) {
        flux(helper, 0);
        helper.setBlock(HERE, ModBlocks.TAINT_SOIL.get());
        BiomePaint.paint(helper.getLevel(), helper.absolutePos(HERE), biome(helper, ModBiomes.TAINTED_LAND), false);

        RandomSource random = RandomSource.create(5L);
        for (int i = 0; i < 100 && helper.getBlockState(HERE).is(ModBlocks.TAINT_SOIL.get()); i++) {
            helper.getBlockState(HERE).randomTick(helper.getLevel(), helper.absolutePos(HERE), random);
        }
        boolean healed = !landIs(helper, HERE, ModBiomes.TAINTED_LAND);
        putBack(helper, HERE);
        helper.assertBlockPresent(Blocks.DIRT, HERE);
        helper.assertTrue(healed, "the land should no longer be tainted once the taint on it has died");
        helper.succeed();
    }

    /** The glimmering grass is grass: flowers grow on it, and covered over it dies back to dirt. */
    @GameTest(template = TEMPLATE)
    public static void ambientGrassIsGrass(GameTestHelper helper) {
        helper.setBlock(HERE, ModBlocks.AMBIENT_GRASS.get());
        helper.assertTrue(Blocks.POPPY.defaultBlockState().canSurvive(helper.getLevel(),
                helper.absolutePos(HERE.above())), "a flower should be able to grow on it");

        helper.setBlock(HERE.above(), Blocks.STONE);
        helper.getBlockState(HERE).randomTick(helper.getLevel(), helper.absolutePos(HERE), RandomSource.create(6L));
        helper.assertBlockPresent(Blocks.DIRT, HERE);
        helper.succeed();
    }

    /**
     * Flux in the air of the one chunk the taint here asks about. Only that one, so as to leave the aura of any test
     * sharing the chunks round about alone.
     */
    private static void flux(GameTestHelper helper, int amount) {
        helper.getLevel().getChunkAt(helper.absolutePos(HERE)).setData(ModAuraAttachment.AURA,
                new AuraChunk(AuraGeneration.BASE, AspectList.EMPTY.add(ModAspects.FLUX, amount)));
    }
}
