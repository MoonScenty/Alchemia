package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.TransfuserBlock;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.block.entity.TransfuserBlockEntity;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The essentia transfusers: one sends what a pipe brings to the jars in front, the other draws from them. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TransfuserTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void transfusersReachTheJarsInFront(GameTestHelper helper) {
        BlockPos in = new BlockPos(4, 2, 4);
        BlockPos out = new BlockPos(10, 2, 4);
        BlockPos jarIn = new BlockPos(4, 5, 4);
        BlockPos jarOut = new BlockPos(10, 5, 4);
        BlockPos behind = new BlockPos(4, 2, 12);
        helper.setBlock(in, ModBlocks.ESSENTIA_INPUT.get().defaultBlockState().setValue(TransfuserBlock.FACING,
                Direction.UP));
        helper.setBlock(out, ModBlocks.ESSENTIA_OUTPUT.get().defaultBlockState().setValue(TransfuserBlock.FACING,
                Direction.UP));
        helper.setBlock(jarIn, ModBlocks.JAR.get());
        helper.setBlock(jarOut, ModBlocks.JAR.get());
        helper.setBlock(behind.below(), ModBlocks.JAR.get());
        TransfuserBlockEntity filler = (TransfuserBlockEntity) helper.getBlockEntity(in);
        TransfuserBlockEntity emptier = (TransfuserBlockEntity) helper.getBlockEntity(out);
        JarBlockEntity full = (JarBlockEntity) helper.getBlockEntity(jarOut);
        for (int i = 0; i < 5; i++) {
            full.accept(ModAspects.FIRE);
        }

        helper.assertTrue(filler.reachableFrom(Direction.DOWN) && !filler.reachableFrom(Direction.UP),
                "a pipe goes on its back");
        helper.assertTrue(filler.wants(ModAspects.WATER), "the filler has a jar in front to fill");
        helper.assertTrue(filler.accept(ModAspects.WATER), "and fills it");
        helper.assertValueEqual(((JarBlockEntity) helper.getBlockEntity(jarIn)).held().get(ModAspects.WATER), 1,
                "the jar in front has it");
        helper.assertValueEqual(emptier.held().get(ModAspects.FIRE), 5, "the emptier offers what is in front");
        helper.assertTrue(emptier.release(ModAspects.FIRE), "and gives one up");
        helper.assertValueEqual(full.held().get(ModAspects.FIRE), 4, "out of the jar in front");
        helper.assertFalse(TransfuserBlockEntity.inFront(helper.absolutePos(in), Direction.UP,
                helper.absolutePos(in.below(2))), "nothing behind it counts");
        helper.succeed();
    }
}
