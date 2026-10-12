package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.entity.Wisp;
import me.moonscenty.alchemia.item.WispyEssenceItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The wisp and the pech of step eleven.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class WispAndPechTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos MIDDLE = new BlockPos(8, 6, 8);

    /** A wisp that has drifted a tick is a wisp of something. */
    @GameTest(template = TEMPLATE)
    public static void aWispTakesAnAspect(GameTestHelper helper) {
        Wisp wisp = helper.spawn(ModEntities.WISP.get(), MIDDLE);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(wisp.aspect() != null, "it has an aspect");
            helper.succeed();
        });
    }

    /** A wisp leaves two points of its aspect as wispy essence, and is its aspect twice over to a scan. */
    @GameTest(template = TEMPLATE)
    public static void aWispLeavesItsEssence(GameTestHelper helper) {
        Wisp wisp = helper.spawn(ModEntities.WISP.get(), MIDDLE);
        wisp.setAspect(ModAspects.FIRE);
        var scanned = Aspects.of(wisp);
        helper.assertValueEqual(scanned.get(ModAspects.FIRE), 2, "two of fire to a scan");
        helper.assertValueEqual(scanned.get(ModAspects.AURA), 1, "and a little auram");
        wisp.kill();
        var left = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(4)).stream()
                .map(ItemEntity::getItem).filter(stack -> stack.is(ModItems.WISPY_ESSENCE.get())).findFirst();
        helper.assertTrue(left.isPresent(), "it left its essence");
        helper.assertTrue(WispyEssenceItem.inside(left.get()).filter(aspect -> aspect.is(ModAspects.FIRE.getKey()))
                .isPresent(), "of fire");
        var worth = Aspects.of(left.get());
        helper.assertValueEqual(worth.get(ModAspects.FIRE), 2, "worth two of fire");
        helper.assertValueEqual(worth.get(ModAspects.AURA), 1, "and a breath of auram");
        helper.succeed();
    }

    /** Taken against something standing still, a wisp zaps it before long. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void aWispZapsWhatItTakesAgainst(GameTestHelper helper) {
        Wisp wisp = helper.spawn(ModEntities.WISP.get(), MIDDLE);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, MIDDLE.offset(3, -4, 0));
        wisp.hurt(helper.getLevel().damageSources().mobAttack(pig), 0.0F);
        float full = pig.getHealth();
        helper.runAfterDelay(200, () -> {
            helper.assertTrue(pig.getHealth() < full, "the pig has been zapped");
            helper.succeed();
        });
    }

    /** A chunk thick with flux can let out a wisp, five blocks over the ground. */
    @GameTest(template = TEMPLATE)
    public static void fluxLetsOutAWisp(GameTestHelper helper) {
        BlockPos ground = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.assertTrue(Wisp.fromFlux(helper.getLevel(), ground, RandomSource.create(1L)), "one came");
        var found = helper.getLevel().getEntitiesOfClass(Wisp.class, helper.getBounds().inflate(4));
        helper.assertTrue(found.size() == 1 && Math.abs(found.getFirst().getY() - (ground.getY() + 5)) < 0.01,
                "five blocks up");
        helper.succeed();
    }
}
