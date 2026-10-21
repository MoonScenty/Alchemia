package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.AlchemiaConfig;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Some kinds are likelier champions than others, as the original's whitelist weighted them; and a pech may be one. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class ChampionRollTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static int offset(GameTestHelper helper, EntityType<? extends Monster> type) {
        Monster mob = type.create(helper.getLevel());
        BlockPos at = helper.absolutePos(new BlockPos(4, 2, 4));
        mob.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        return Champions.offset(mob);
    }

    @GameTest(template = TEMPLATE)
    public static void someKindsAreLikelierChampions(GameTestHelper helper) {
        int zombie = offset(helper, EntityType.ZOMBIE);
        int taintacle = offset(helper, ModEntities.TAINTACLE.get());
        int inhabited = offset(helper, ModEntities.INHABITED_ZOMBIE.get());
        if (AlchemiaConfig.CHAMPION_MOBS.get()) {
            helper.assertValueEqual(taintacle, zombie - 1, "a taintacle has one more");
            helper.assertValueEqual(inhabited, zombie - 2, "an inhabited zombie two more");
        } else {
            helper.assertValueEqual(taintacle, zombie, "with champions off, the kinds count nothing");
        }
        helper.assertTrue(ModEntities.PECH.get().is(ModTags.Entities.CHAMPION_CANDIDATES), "a pech may be one");
        helper.succeed();
    }
}
