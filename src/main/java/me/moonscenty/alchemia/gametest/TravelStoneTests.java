package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.TravelStoneBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The paving stone of travel: two seconds of speed II and jump boost I for anything living that walks on it. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TravelStoneTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void steppingOnItHastens(GameTestHelper helper) {
        BlockPos stone = new BlockPos(4, 2, 4);
        helper.setBlock(stone, ModBlocks.TRAVEL_STONE.get());
        Pig pig = helper.spawn(EntityType.PIG, stone.above());
        ModBlocks.TRAVEL_STONE.get().stepOn(helper.getLevel(), helper.absolutePos(stone),
                helper.getBlockState(stone), pig);
        var speed = pig.getEffect(MobEffects.MOVEMENT_SPEED);
        var jump = pig.getEffect(MobEffects.JUMP);
        helper.assertTrue(speed != null && speed.getAmplifier() == 1, "speed II");
        helper.assertTrue(jump != null && jump.getAmplifier() == 0, "jump boost I");
        helper.assertValueEqual(speed.getDuration(), TravelStoneBlock.LASTS, "for two seconds");
        helper.assertFalse(speed.isVisible(), "without particles");
        pig.discard();
        helper.succeed();
    }
}
