package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.essentia.EssentiaReach;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class EssentiaFittingsTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos JAR = new BlockPos(3, 2, 3);
    private static final BlockPos MATRIX = new BlockPos(6, 2, 3);

    @GameTest(template = TEMPLATE)
    public static void aBracedJarIsKeptOnlyFromPipelessDraws(GameTestHelper helper) {
        helper.setBlock(JAR, ModBlocks.JAR.get());
        JarBlockEntity jar = (JarBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(JAR));
        for (int one = 0; one < 5; one++) {
            jar.accept(ModAspects.WATER);
        }
        jar.brace(true);
        helper.assertTrue(EssentiaReach.drainForInfusion(helper.getLevel(), helper.absolutePos(MATRIX),
                ModAspects.WATER, 12) == null, "an infusion cannot reach a braced jar");
        helper.assertTrue(jar.release(ModAspects.WATER), "a tube or a phial still can");

        jar.brace(false);
        helper.assertValueEqual(EssentiaReach.drainForInfusion(helper.getLevel(), helper.absolutePos(MATRIX),
                ModAspects.WATER, 12), helper.absolutePos(JAR), "unbraced, the infusion draws from it");
        helper.assertValueEqual(jar.amount(), 3, "one for the tube and one for the infusion");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aSmelterIsFedFromTheSidesAndFuelledFromBelow(GameTestHelper helper) {
        helper.setBlock(JAR, ModBlocks.ESSENTIA_SMELTER.get());
        EssentiaSmelterBlockEntity smelter = (EssentiaSmelterBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(JAR));
        helper.assertValueEqual(smelter.getSlotsForFace(Direction.UP).length, 0, "nothing goes in at the top");
        helper.assertValueEqual(smelter.getSlotsForFace(Direction.DOWN)[0], EssentiaSmelterBlockEntity.SLOT_FUEL,
                "fuel goes in at the bottom");
        for (Direction side : Direction.Plane.HORIZONTAL) {
            helper.assertValueEqual(smelter.getSlotsForFace(side)[0], EssentiaSmelterBlockEntity.SLOT_INPUT,
                    "what is smelted goes in at the " + side.getName());
        }
        helper.succeed();
    }
}
