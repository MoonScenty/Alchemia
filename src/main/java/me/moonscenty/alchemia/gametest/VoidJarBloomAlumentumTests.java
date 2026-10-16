package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.EtherealBloomBlockEntity;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.entity.ThrownAlumentum;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The void jar, the ethereal bloom and thrown alumentum. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class VoidJarBloomAlumentumTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void aVoidJarGoesOnTakingWhenFull(GameTestHelper helper) {
        BlockPos plain = new BlockPos(2, 2, 2);
        BlockPos voided = new BlockPos(4, 2, 2);
        helper.setBlock(plain, ModBlocks.JAR.get());
        helper.setBlock(voided, ModBlocks.VOID_JAR.get());
        JarBlockEntity jar = jar(helper, plain);
        JarBlockEntity voidJar = jar(helper, voided);
        for (int i = 0; i < JarBlockEntity.CAPACITY + 20; i++) {
            jar.accept(ModAspects.FIRE);
            voidJar.accept(ModAspects.FIRE);
        }
        helper.assertValueEqual(jar.amount(), JarBlockEntity.CAPACITY, "a jar holds what it holds");
        helper.assertFalse(jar.wants(ModAspects.FIRE), "and then takes no more");
        helper.assertValueEqual(voidJar.amount(), JarBlockEntity.CAPACITY, "so does a void jar");
        helper.assertTrue(voidJar.wants(ModAspects.FIRE), "but it goes on taking");
        helper.assertTrue(voidJar.accept(ModAspects.FIRE), "and letting the rest go");
        helper.assertFalse(voidJar.wants(ModAspects.WATER), "though only what it holds");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theBloomKillsTaintWhereItsLightFalls(GameTestHelper helper) {
        BlockPos soil = new BlockPos(3, 1, 3);
        helper.setBlock(soil, ModBlocks.TAINT_SOIL.get());
        helper.assertTrue(EtherealBloomBlockEntity.clear(helper.getLevel(), helper.absolutePos(soil)),
                "it cleared something");
        helper.assertBlockNotPresent(ModBlocks.TAINT_SOIL.get(), soil);

        BlockPos bloom = new BlockPos(6, 2, 6);
        helper.setBlock(bloom.below(), Blocks.STONE);
        helper.setBlock(bloom, ModBlocks.ETHEREAL_BLOOM.get());
        helper.assertTrue(helper.getBlockEntity(bloom) instanceof EtherealBloomBlockEntity, "a bloom grows there");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void thrownAlumentumGoesOffWhereItLands(GameTestHelper helper) {
        helper.setBlock(new BlockPos(4, 1, 4), Blocks.STONE);
        Vec3 above = helper.absoluteVec(new Vec3(4.5, 6.0, 4.5));
        ThrownAlumentum thrown = new ThrownAlumentum(helper.getLevel(), above.x, above.y, above.z);
        thrown.setDeltaMovement(0.0, -0.5, 0.0);
        helper.getLevel().addFreshEntity(thrown);
        helper.succeedWhen(() -> {
            if (!thrown.isRemoved()) {
                throw new GameTestAssertException("still in the air");
            }
        });
    }

    private static JarBlockEntity jar(GameTestHelper helper, BlockPos at) {
        if (helper.getBlockEntity(at) instanceof JarBlockEntity jar) {
            return jar;
        }
        throw new GameTestAssertException("no jar at " + at);
    }
}
