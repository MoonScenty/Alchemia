package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.BarrierStoneBlock;
import me.moonscenty.alchemia.block.entity.BarrierStoneBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The barrier stone: a ward two blocks high that stops creatures but not players, and goes off with a signal. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class BarrierStoneTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void theWardStopsCreaturesButNotPlayers(GameTestHelper helper) {
        BlockPos stone = new BlockPos(4, 2, 4);
        helper.setBlock(stone, ModBlocks.BARRIER_STONE.get());
        BlockPos absolute = helper.absolutePos(stone);
        BarrierStoneBlockEntity.raise(helper.getLevel(), absolute);
        helper.assertBlockPresent(ModBlocks.WARDING_AURA.get(), stone.above());
        helper.assertBlockPresent(ModBlocks.WARDING_AURA.get(), stone.above(2));

        BlockPos ward = absolute.above();
        var state = helper.getLevel().getBlockState(ward);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, stone.above(3));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemEntity item = new ItemEntity(helper.getLevel(), 0, 0, 0, new ItemStack(Items.STICK));
        helper.assertFalse(state.getCollisionShape(helper.getLevel(), ward, CollisionContext.of(zombie)).isEmpty(),
                "solid to a zombie");
        helper.assertTrue(state.getCollisionShape(helper.getLevel(), ward, CollisionContext.of(player)).isEmpty(),
                "open to a player");
        helper.assertTrue(state.getCollisionShape(helper.getLevel(), ward, CollisionContext.of(item)).isEmpty(),
                "open to an item");
        helper.assertValueEqual(state.getBlockPathType(helper.getLevel(), ward, zombie), PathType.BLOCKED,
                "not on a creature's path");
        helper.assertValueEqual(helper.getLevel().getBlockState(ward).getShape(helper.getLevel(), ward).isEmpty(),
                true, "cannot be struck");

        // a signal holds the stone off
        helper.setBlock(stone.east(), Blocks.REDSTONE_BLOCK);
        helper.assertBlockProperty(stone, BarrierStoneBlock.POWERED, true);
        helper.assertTrue(state.getCollisionShape(helper.getLevel(), ward, CollisionContext.of(zombie)).isEmpty(),
                "open while powered");

        // and without the stone the ward goes
        helper.setBlock(stone, Blocks.AIR);
        helper.assertBlockNotPresent(ModBlocks.WARDING_AURA.get(), stone.above());
        helper.assertBlockNotPresent(ModBlocks.WARDING_AURA.get(), stone.above(2));
        zombie.discard();
        helper.succeed();
    }
}
