package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.eldritch.VacuumBlock;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Outer Lands' stuff: their bedrock, their void, and liquid death. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class OuterBlocksTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** The void floods the air next to it, a step at a time, each step further from its source. */
    @GameTest(template = TEMPLATE)
    public static void theVoidSpreads(GameTestHelper helper) {
        BlockPos source = new BlockPos(16, 10, 16);
        helper.setBlock(source, ModBlocks.VACUUM.get());
        helper.getLevel().scheduleTick(helper.absolutePos(source), ModBlocks.VACUUM.get(), 1);
        helper.succeedWhen(() -> {
            int found = 0;
            for (BlockPos at : BlockPos.betweenClosed(helper.absolutePos(source.offset(-1, -1, -1)),
                    helper.absolutePos(source.offset(1, 1, 1)))) {
                var state = helper.getLevel().getBlockState(at);
                if (state.is(ModBlocks.VACUUM.get()) && state.getValue(VacuumBlock.SPREAD) == 1) {
                    found++;
                }
            }
            helper.assertTrue(found > 0, "a step of void beside the source");
        });
    }

    /** Ancient bedrock laid bare to the air gives way to the void. */
    @GameTest(template = TEMPLATE)
    public static void bareBedrockOpensIntoTheVoid(GameTestHelper helper) {
        BlockPos rock = new BlockPos(16, 10, 16);
        helper.setBlock(rock.above(), Blocks.STONE);
        helper.setBlock(rock, ModBlocks.ANCIENT_BEDROCK.get());
        helper.setBlock(rock.above(), Blocks.AIR);
        helper.succeedWhen(() -> helper.assertBlockPresent(ModBlocks.VACUUM.get(), rock));
    }

    /** Liquid death dissolves what stands in it. */
    @GameTest(template = TEMPLATE)
    public static void liquidDeathDissolves(GameTestHelper helper) {
        BlockPos at = new BlockPos(16, 2, 16);
        helper.setBlock(at, ModBlocks.LIQUID_DEATH.get());
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, at);
        zombie.setData(me.moonscenty.alchemia.player.ModAttachments.CHAMPION, Champions.ORDINARY);
        float before = zombie.getHealth();
        helper.succeedWhen(() -> helper.assertTrue(zombie.getHealth() < before || !zombie.isAlive(),
                "it is dissolving"));
    }
}
