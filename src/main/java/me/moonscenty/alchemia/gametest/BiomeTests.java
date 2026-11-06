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

    @GameTest(template = TEMPLATE)
    public static void darkNodesMakeTheLandEerie(GameTestHelper helper) {
        Holder<Biome> into = BiomePaint.oneOf(helper.getLevel(), ModTags.Biomes.NODE_DARKENS_INTO,
                RandomSource.create(1L)).orElseThrow();
        helper.assertTrue(into.is(ModBiomes.EERIE), "a dark node should make eerie land, not " + into);
        helper.succeed();
    }

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
        helper.assertBlockPresent(ModBlocks.TAINT_DUST.get(), HERE);
        helper.assertTrue(healed, "the land should no longer be tainted once the taint on it has died");
        helper.succeed();
    }

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

    // only this chunk, so neighbouring tests' aura is left alone
    private static void flux(GameTestHelper helper, int amount) {
        helper.getLevel().getChunkAt(helper.absolutePos(HERE)).setData(ModAuraAttachment.AURA,
                new AuraChunk(AuraGeneration.BASE, AspectList.EMPTY.add(ModAspects.FLUX, amount)));
    }

    private static Holder<Biome> any(GameTestHelper helper, ResourceKey<Biome> key) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(key);
    }

    @GameTest(template = TEMPLATE)
    public static void theWildGrowsWhereTheOriginalPutsIt(GameTestHelper helper) {
        Holder<Biome> forest = any(helper, ModBiomes.MAGICAL_FOREST);
        helper.assertTrue(forest.is(ModTags.Biomes.HAS_GREATWOOD), "greatwood grows in the magical forest too");
        helper.assertTrue(forest.is(ModTags.Biomes.HAS_ANGRY_ZOMBIES), "and angry zombies walk there");
        helper.assertFalse(forest.is(ModTags.Biomes.HAS_SILVERWOOD), "the forest grows its own silverwood");
        helper.assertTrue(any(helper, net.minecraft.world.level.biome.Biomes.FOREST).is(ModTags.Biomes.HAS_SILVERWOOD),
                "silverwood stands in plain forest");
        helper.assertTrue(any(helper, net.minecraft.world.level.biome.Biomes.TAIGA).is(ModTags.Biomes.HAS_LITTLE_GREATWOOD),
                "a little greatwood in the taiga");
        helper.assertFalse(any(helper, ModBiomes.TAINTED_LAND).is(ModTags.Biomes.HAS_PECHS), "no pechs in tainted land");
        helper.assertTrue(any(helper, ModBiomes.EERIE).is(ModTags.Biomes.HAS_PECHS), "but in the eerie land");
        Holder<Biome> nether = any(helper, net.minecraft.world.level.biome.Biomes.NETHER_WASTES);
        helper.assertTrue(nether.is(ModTags.Biomes.HAS_NODES), "nodes form in the nether");
        helper.assertFalse(nether.is(ModTags.Biomes.HAS_ORES), "ores and crystals do not");
        helper.assertTrue(any(helper, net.minecraft.world.level.biome.Biomes.THE_END).is(ModTags.Biomes.HAS_ORES),
                "but they do in the end");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theLandsSpawnWhatTheOriginalsDid(GameTestHelper helper) {
        var forest = any(helper, ModBiomes.MAGICAL_FOREST).value().getMobSettings()
                .getMobs(net.minecraft.world.entity.MobCategory.MONSTER).unwrap();
        int pechWeight = 0;
        for (var data : forest) {
            if (data.type == me.moonscenty.alchemia.registry.ModEntities.PECH.get()) {
                pechWeight += data.getWeight().asInt();
                helper.assertValueEqual(data.maxCount, 1, "pechs come one at a time");
            }
        }
        helper.assertValueEqual(pechWeight, 20, "pech weight in the magical forest");

        var eerie = any(helper, ModBiomes.EERIE).value().getMobSettings()
                .getMobs(net.minecraft.world.entity.MobCategory.CREATURE).unwrap();
        helper.assertTrue(!eerie.isEmpty() && eerie.stream().allMatch(data -> data.type == net.minecraft.world.entity.EntityType.BAT),
                "the eerie land keeps only bats, had " + eerie);

        Biome tainted = any(helper, ModBiomes.TAINTED_LAND).value();
        helper.assertTrue(tainted.getMobSettings().getMobs(net.minecraft.world.entity.MobCategory.AMBIENT).unwrap().stream()
                .anyMatch(data -> data.type == net.minecraft.world.entity.EntityType.BAT), "bats still haunt tainted caves");
        helper.assertValueEqual(tainted.getBaseTemperature(), 0.5F, "tainted land temperature");
        helper.succeed();
    }
}
