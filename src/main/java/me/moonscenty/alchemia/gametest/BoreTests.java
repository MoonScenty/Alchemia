package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.ArcaneBoreBaseBlock;
import me.moonscenty.alchemia.block.ArcaneBoreBlock;
import me.moonscenty.alchemia.block.entity.ArcaneBoreBlockEntity;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The arcane bore: it digs the way it faces while it has a signal, and what it digs comes out of its base. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class BoreTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos BASE = new BlockPos(5, 2, 12);
    private static final BlockPos BORE = BASE.above();

    /** A bore on its base, facing north, with a focus and a pickaxe in it and entropy in the air about it. */
    private static ArcaneBoreBlockEntity bore(GameTestHelper helper) {
        helper.setBlock(BASE, ModBlocks.ARCANE_BORE_BASE.get().defaultBlockState()
                .setValue(ArcaneBoreBaseBlock.FACING, Direction.EAST));
        helper.setBlock(BASE.east(), Blocks.CHEST);
        helper.setBlock(BORE, ModBlocks.ARCANE_BORE.get().defaultBlockState()
                .setValue(ArcaneBoreBlock.FACING, Direction.NORTH));
        BlockPos at = helper.absolutePos(BORE);
        AuraHandler.add(helper.getLevel(), at, ModAspects.ENTROPY, 200);
        ArcaneBoreBlockEntity bore = (ArcaneBoreBlockEntity) helper.getLevel().getBlockEntity(at);
        bore.setItem(ArcaneBoreBlockEntity.SLOT_FOCUS, new ItemStack(ModItems.FOCI.get("excavation").get()));
        bore.setItem(ArcaneBoreBlockEntity.SLOT_PICK, new ItemStack(Items.IRON_PICKAXE));
        // the first line it looks along starts three blocks out
        helper.setBlock(BORE.north(3), Blocks.STONE);
        return bore;
    }

    private static void run(GameTestHelper helper, ArcaneBoreBlockEntity bore, int ticks) {
        BlockPos at = helper.absolutePos(BORE);
        for (int tick = 0; tick < ticks; tick++) {
            ArcaneBoreBlockEntity.serverTick(helper.getLevel(), at, helper.getLevel().getBlockState(at), bore);
        }
    }

    /** Given a signal, it digs the stone in front of it, and the cobblestone ends up in the chest by its base. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aBoreDigsWhatIsInFrontAndPutsItInTheChest(GameTestHelper helper) {
        ArcaneBoreBlockEntity bore = bore(helper);
        helper.setBlock(BORE.west(), Blocks.REDSTONE_BLOCK);
        run(helper, bore, 300);

        helper.assertBlockNotPresent(Blocks.STONE, BORE.north(3));
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(BASE.east()));
        helper.assertTrue(chest.getItem(0).is(Items.COBBLESTONE), "what it dug came out into the chest");
        helper.assertTrue(bore.getItem(ArcaneBoreBlockEntity.SLOT_PICK).getDamageValue() > 0,
                "and the pickaxe wore for it");
        helper.succeed();
    }

    /** Without a signal it does nothing at all. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aBoreWithoutASignalStandsIdle(GameTestHelper helper) {
        ArcaneBoreBlockEntity bore = bore(helper);
        run(helper, bore, 300);
        helper.assertBlockPresent(Blocks.STONE, BORE.north(3));
        helper.succeed();
    }
}
