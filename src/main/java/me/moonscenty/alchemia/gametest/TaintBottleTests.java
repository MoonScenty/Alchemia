package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.ThrownTaintBottle;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** A thrown bottle of taint poisons the living about where it breaks, but not the undead. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TaintBottleTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void bottleOfTaintPoisons(GameTestHelper helper) {
        for (int x = 2; x <= 8; x++) {
            for (int z = 2; z <= 8; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(6, 2, 5));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 2, 5));
        ThrownTaintBottle bottle = new ThrownTaintBottle(ModEntities.TAINT_BOTTLE.get(), helper.getLevel());
        Vec3 at = helper.absoluteVec(new Vec3(5.5, 5.0, 5.5));
        bottle.moveTo(at.x, at.y, at.z);
        bottle.setDeltaMovement(0.0, -0.5, 0.0);
        helper.getLevel().addFreshEntity(bottle);
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.hasEffect(ModEffects.TAINT_POISON), "the pig is poisoned");
            helper.assertFalse(zombie.hasEffect(ModEffects.TAINT_POISON), "the zombie is not");
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(Alchemia.id("bottle_taint")).isPresent(),
                    "and it is made");
        });
    }
}
