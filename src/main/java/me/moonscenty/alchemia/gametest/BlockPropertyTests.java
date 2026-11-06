package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class BlockPropertyTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static void check(GameTestHelper helper, Block block, float hardness, float resistance) {
        helper.assertValueEqual(block.defaultDestroyTime(), hardness, block.getDescriptionId() + " hardness");
        helper.assertValueEqual(block.getExplosionResistance(), resistance, block.getDescriptionId() + " resistance");
    }

    @GameTest(template = TEMPLATE)
    public static void blocksHaveTheOriginalProperties(GameTestHelper helper) {
        check(helper, ModBlocks.TUBE.get(), 0.5F, 3.0F);
        check(helper, ModBlocks.CRUCIBLE.get(), 2.0F, 12.0F);
        check(helper, ModBlocks.RESEARCH_TABLE.get(), 2.0F, 12.0F);
        check(helper, ModBlocks.INFUSION_MATRIX.get(), 2.0F, 12.0F);
        check(helper, ModBlocks.PATTERN_CRAFTER.get(), 2.0F, 12.0F);
        check(helper, ModBlocks.BELLOWS.get(), 1.0F, 12.0F);
        check(helper, ModBlocks.ARCANE_STONE.block().get(), 2.0F, 6.0F);
        check(helper, ModBlocks.TAINT_SOIL.get(), 10.0F, 60.0F);
        check(helper, ModBlocks.TAINT_LOG.get(), 3.0F, 60.0F);
        check(helper, ModBlocks.JAR.get(), 0.3F, 12.0F);
        check(helper, ModBlocks.AMBER_ORE.get(), 1.25F, 3.0F);
        check(helper, ModBlocks.CINNABAR_ORE.get(), 2.0F, 3.0F);
        check(helper, ModBlocks.NITOR.get(net.minecraft.world.item.DyeColor.WHITE).get(), 0.1F, 0.1F);
        check(helper, ModBlocks.FLESH_BLOCK.get(), 0.25F, 1.2F);

        helper.assertValueEqual(ModBlocks.INFUSION_MATRIX.get().defaultBlockState().getLightEmission(), 0,
                "the matrix gives no light");
        SoundType jar = ModBlocks.JAR.get().defaultBlockState().getSoundType();
        helper.assertValueEqual(jar.getBreakSound(), ModSounds.JAR.get(), "a jar sounds like a jar");
        SoundType crystal = ModBlocks.CRYSTALS.get(CrystalType.AIR).get().defaultBlockState().getSoundType();
        helper.assertValueEqual(crystal.getBreakSound(), ModSounds.CRYSTAL.get(), "a crystal like a crystal");
        helper.succeed();
    }
}
