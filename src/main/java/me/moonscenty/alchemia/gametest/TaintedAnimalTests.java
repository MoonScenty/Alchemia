package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.entity.AlchemicSlime;
import me.moonscenty.alchemia.entity.TaintCrawler;
import me.moonscenty.alchemia.entity.TaintedCow;
import me.moonscenty.alchemia.entity.TaintedCreeper;
import me.moonscenty.alchemia.entity.TaintedRabbit;
import me.moonscenty.alchemia.entity.TaintedSheep;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * What the taint makes of what dies of it, step eleven: the made-over animals and the alchemic slime.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TaintedAnimalTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos AT = new BlockPos(5, 2, 5);

    private static <T extends net.minecraft.world.entity.Mob> T poisoned(GameTestHelper helper, EntityType<T> type) {
        helper.setBlock(AT.below(), Blocks.STONE);
        T creature = helper.spawnWithNoFreeWill(type, AT);
        creature.addEffect(new MobEffectInstance(ModEffects.FLUX_FLU, 200, 0));
        return creature;
    }

    private static <T extends net.minecraft.world.entity.Entity> java.util.List<T> around(GameTestHelper helper,
            Class<T> kind) {
        return helper.getLevel().getEntitiesOfClass(kind, helper.getBounds().inflate(2)).stream()
                .map(kind::cast).toList();
    }

    /** A cow that dies of taint poison gets up again a tainted cow, and is gone itself. */
    @GameTest(template = TEMPLATE)
    public static void aPoisonedCowIsMadeOver(GameTestHelper helper) {
        var cow = poisoned(helper, EntityType.COW);
        cow.kill();
        helper.assertTrue(cow.isRemoved(), "the cow is gone");
        helper.assertValueEqual(around(helper, TaintedCow.class).size(), 1, "and a tainted cow stands there");
        helper.succeed();
    }

    /** A rabbit is made over as the colour it was. */
    @GameTest(template = TEMPLATE)
    public static void aPoisonedRabbitKeepsItsColour(GameTestHelper helper) {
        var rabbit = poisoned(helper, EntityType.RABBIT);
        rabbit.setVariant(Rabbit.Variant.GOLD);
        rabbit.kill();
        var made = around(helper, TaintedRabbit.class);
        helper.assertTrue(made.size() == 1 && made.getFirst().getVariant() == Rabbit.Variant.GOLD,
                "a gold tainted rabbit");
        helper.succeed();
    }

    /** Another animal bursts into crawlers, more the bigger it was. */
    @GameTest(template = TEMPLATE)
    public static void aPoisonedWolfBurstsIntoCrawlers(GameTestHelper helper) {
        var wolf = poisoned(helper, EntityType.WOLF);
        int expected = (int) Math.max(1.0, Math.sqrt(wolf.getMaxHealth() + 2.0F));
        wolf.kill();
        helper.assertValueEqual(around(helper, TaintCrawler.class).size(), expected, "crawlers for a wolf");
        helper.succeed();
    }

    /** Anything else leaves an alchemic slime the size of what it was. */
    @GameTest(template = TEMPLATE)
    public static void aPoisonedZombieLeavesASlime(GameTestHelper helper) {
        var zombie = poisoned(helper, EntityType.ZOMBIE);
        zombie.kill();
        var slimes = around(helper, AlchemicSlime.class);
        helper.assertTrue(slimes.size() == 1 && slimes.getFirst().getSize() == 3, "a slime of three");
        helper.succeed();
    }

    /** Without the poison in it, a cow dies a cow. */
    @GameTest(template = TEMPLATE)
    public static void anUnpoisonedCowStaysDead(GameTestHelper helper) {
        helper.setBlock(AT.below(), Blocks.STONE);
        var cow = helper.spawnWithNoFreeWill(EntityType.COW, AT);
        cow.kill();
        helper.assertTrue(around(helper, TaintedCow.class).isEmpty(), "nothing gets up");
        helper.succeed();
    }

    /** A tainted creeper going off breaks nothing, poisons what is near, and is gone. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aTaintedCreeperSpreadsPoison(GameTestHelper helper) {
        helper.setBlock(AT.below(), Blocks.STONE);
        helper.setBlock(AT.east(2).below(), Blocks.STONE);
        TaintedCreeper creeper = helper.spawnWithNoFreeWill(ModEntities.TAINTED_CREEPER.get(), AT);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, AT.east(2));
        creeper.setSwelling(1);
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(creeper.isRemoved(), "it went off");
            helper.assertTrue(pig.hasEffect(ModEffects.FLUX_FLU), "the pig is poisoned");
            helper.assertBlockPresent(Blocks.STONE, AT.below());
            helper.succeed();
        });
    }

    /** A tainted sheep shears to purple wool. */
    @GameTest(template = TEMPLATE)
    public static void aTaintedSheepShearsPurple(GameTestHelper helper) {
        helper.setBlock(AT.below(), Blocks.STONE);
        TaintedSheep sheep = helper.spawnWithNoFreeWill(ModEntities.TAINTED_SHEEP.get(), AT);
        helper.assertTrue(sheep.isShearable(null, new ItemStack(Items.SHEARS), helper.getLevel(),
                sheep.blockPosition()),
                "it can be shorn");
        var wool = sheep.onSheared(null, new ItemStack(Items.SHEARS), helper.getLevel(), sheep.blockPosition());
        helper.assertTrue(!wool.isEmpty() && wool.getFirst().is(Items.PURPLE_WOOL), "purple wool");
        helper.assertTrue(sheep.isSheared(), "and it is shorn");
        helper.succeed();
    }

    /** An alchemic slime wading through deep goo soaks some up and grows. */
    @GameTest(template = TEMPLATE)
    public static void aSlimeGrowsInGoo(GameTestHelper helper) {
        helper.setBlock(AT.below(), Blocks.STONE);
        helper.setBlock(AT, ModBlocks.FLUX_GOO.get());
        AlchemicSlime slime = helper.spawnWithNoFreeWill(ModEntities.ALCHEMIC_SLIME.get(), AT);
        slime.setSize(1, true);
        BlockPos where = helper.absolutePos(AT);
        for (int wade = 0; wade < 50 && slime.getSize() == 1; wade++) {
            helper.getLevel().getBlockState(where).entityInside(helper.getLevel(), where, slime);
        }
        helper.assertValueEqual(slime.getSize(), 2, "it grew");
        helper.assertTrue(helper.getLevel().getBlockState(where).getValue(FluxGooBlock.LEVEL) == FluxGooBlock.FULL - 1,
                "out of a depth of the goo");
        helper.succeed();
    }

    /** Killed, an alchemic slime bursts into as many little ones as it was big. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aSlimeBursts(GameTestHelper helper) {
        helper.setBlock(AT.below(), Blocks.STONE);
        AlchemicSlime slime = helper.spawnWithNoFreeWill(ModEntities.ALCHEMIC_SLIME.get(), AT);
        slime.setSize(3, true);
        slime.kill();
        helper.runAfterDelay(30, () -> {
            var little = around(helper, AlchemicSlime.class).stream().filter(s -> s.getSize() == 1).count();
            helper.assertValueEqual((int) little, 3, "three little slimes");
            helper.succeed();
        });
    }

    /** A deep puddle of goo, left alone under open air, sooner or later congeals into a slime. */
    @GameTest(template = TEMPLATE)
    public static void gooCongeals(GameTestHelper helper) {
        helper.setBlock(AT.below(), Blocks.STONE);
        helper.setBlock(AT, ModBlocks.FLUX_GOO.get());
        BlockPos where = helper.absolutePos(AT);
        RandomSource random = RandomSource.create(5L);
        for (int turn = 0; turn < 500 && helper.getLevel().getBlockState(where).is(ModBlocks.FLUX_GOO.get());
                turn++) {
            // kept topped up, so that it does not dry away first
            helper.setBlock(AT, ModBlocks.FLUX_GOO.get());
            helper.getLevel().getBlockState(where).randomTick(helper.getLevel(), where, random);
        }
        helper.assertFalse(around(helper, AlchemicSlime.class).isEmpty(), "a slime came of it");
        helper.succeed();
    }
}
