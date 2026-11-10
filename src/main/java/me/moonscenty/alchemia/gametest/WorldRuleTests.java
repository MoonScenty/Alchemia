package me.moonscenty.alchemia.gametest;

import java.util.HashSet;
import java.util.Set;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.BiomeLeanings;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.worldgen.MoundFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class WorldRuleTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static Holder<Biome> biome(GameTestHelper helper, net.minecraft.resources.ResourceKey<Biome> key) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(key);
    }

    @GameTest(template = TEMPLATE)
    public static void aBiomeOfManyKindsAveragesThem(GameTestHelper helper) {
        Holder<Biome> swamp = biome(helper, Biomes.SWAMP);
        float sum = 0.0F;
        int count = 0;
        for (BiomeLeanings.Leaning leaning : BiomeLeanings.ALL) {
            if (swamp.is(leaning.where())) {
                sum += leaning.level();
                count++;
            }
        }
        helper.assertTrue(count > 1, "a swamp should be more than one kind of land, was " + count);
        helper.assertTrue(Math.abs(AuraGeneration.strength(swamp) - sum / count) < 0.0001F, "and its strength their mean");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void eachChunkLeansItsOwnWay(GameTestHelper helper) {
        Holder<Biome> swamp = biome(helper, Biomes.SWAMP);
        Set<Aspect> seen = new HashSet<>();
        RandomSource random = RandomSource.create(1L);
        for (int i = 0; i < 200; i++) {
            Holder<Aspect> leaning = AuraGeneration.leaning(swamp, random);
            if (leaning != null) {
                seen.add(leaning.value());
            }
        }
        helper.assertTrue(seen.size() > 1, "a swamp's chunks should not all lean the same way");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void oneNodeInTenIsOdd(GameTestHelper helper) {
        RandomSource random = RandomSource.create(2L);
        int odd = 0;
        int tainted = 0;
        int drawn = 20000;
        for (int i = 0; i < drawn; i++) {
            AuraNode node = new AuraNode(helper.getLevel());
            node.drawUp(random);
            if (node.type() != NodeType.PLAIN) {
                odd++;
            }
            if (node.type() == NodeType.TAINTED) {
                tainted++;
            }
        }
        helper.assertTrue(odd > drawn * 0.08 && odd < drawn * 0.12, "about one in ten, got " + odd);
        int kinds = NodeType.values().length - 1;
        double expected = drawn / 10.0 / kinds * 0.67;
        helper.assertTrue(tainted > expected * 0.7 && tainted < expected * 1.3,
                "a third of the tainted ones turn plain, got " + tainted + " of about " + (int) expected);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aSecondHelpingFillsTheChest(GameTestHelper helper) {
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(at);
        MoundFeature.secondHelping(helper.getLevel(), chest, helper.absolutePos(at), RandomSource.create(3L));
        helper.assertFalse(chest.isEmpty(), "the dungeon's loot should be in the chest");
        helper.succeed();
    }
}
