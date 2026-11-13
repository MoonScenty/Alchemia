package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.ArchitectArea;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import me.moonscenty.alchemia.wand.spell.ShockOrb;
import me.moonscenty.alchemia.wand.spell.Swapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class WandRuleTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static ItemStack trade(Block picked, FocusUpgrade... worked) {
        ItemStack wand = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        ItemStack focus = new ItemStack(ModItems.FOCI.get("trade").get());
        for (FocusUpgrade upgrade : worked) {
            FocusUpgrades.apply(focus, upgrade);
        }
        focus.set(ModDataComponents.PICKED_BLOCK.get(), picked.builtInRegistryHolder());
        WandItem.setFocus(wand, focus);
        AspectList filled = AspectList.EMPTY;
        for (var primal : ModAspects.primals()) {
            filled = filled.add(primal, 100 * WandItem.FINE);
        }
        wand.set(ModDataComponents.VIS.get(), filled);
        return wand;
    }

    private static void floor(GameTestHelper helper, Block block) {
        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 7; z++) {
                helper.setBlock(new BlockPos(x, 2, z), block);
            }
        }
    }

    private static BlockHitResult top(GameTestHelper helper, BlockPos at) {
        return new BlockHitResult(helper.absoluteVec(Vec3.atCenterOf(at)), Direction.UP, helper.absolutePos(at), false);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aTradeWaitsItsTurnOneBlockATick(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = trade(Blocks.STONE);
        caster.setItemInHand(InteractionHand.MAIN_HAND, wand);
        BlockPos first = new BlockPos(2, 2, 2);
        List<BlockPos> row = List.of(first, first.east(), first.east(2));
        row.forEach(at -> helper.setBlock(at, Blocks.COAL_ORE));

        helper.assertTrue(Swapping.trade(helper.getLevel(), caster, wand, top(helper, first), 0), "it went");
        helper.assertBlockPresent(Blocks.COAL_ORE, first);
        helper.succeedWhen(() -> row.forEach(at -> helper.assertBlockPresent(Blocks.STONE, at)));
    }

    @GameTest(template = TEMPLATE)
    public static void theTradeAreaKeepsToTheFaceAndTheChosenWidths(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = trade(Blocks.STONE, FocusUpgrade.ARCHITECT);
        ItemStack focus = WandItem.focus(wand).copy();
        focus.set(ModDataComponents.FOCUS_AREA.get(), new ArchitectArea(1, 0, 0));
        WandItem.setFocus(wand, focus);
        floor(helper, Blocks.DIRT);
        BlockPos centre = new BlockPos(4, 2, 4);
        helper.setBlock(centre.west(), Blocks.GRAVEL);

        var found = Swapping.area(helper.getLevel(), caster, wand, helper.absolutePos(centre), Direction.UP);
        helper.assertValueEqual(found.size(), 2, "other blocks cut the row off");

        helper.setBlock(centre.west(), Blocks.DIRT);
        found = Swapping.area(helper.getLevel(), caster, wand, helper.absolutePos(centre), Direction.UP);
        helper.assertValueEqual(found.size(), 3, "three across and one deep");
        helper.assertTrue(found.contains(helper.absolutePos(centre.east()))
                && found.contains(helper.absolutePos(centre.west())), "either side of the struck block");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void anArchitectTradesTheWholeArea(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = trade(Blocks.STONE, FocusUpgrade.ARCHITECT);
        caster.setItemInHand(InteractionHand.MAIN_HAND, wand);
        ItemStack focus = WandItem.focus(wand).copy();
        focus.set(ModDataComponents.FOCUS_AREA.get(), new ArchitectArea(1, 1, 0));
        WandItem.setFocus(wand, focus);
        floor(helper, Blocks.DIRT);
        BlockPos centre = new BlockPos(4, 2, 4);

        helper.assertTrue(Swapping.trade(helper.getLevel(), caster, wand, top(helper, centre), 0), "it went");
        helper.succeedWhen(() -> {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    helper.assertBlockPresent(Blocks.STONE, centre.offset(x, 0, z));
                }
            }
            helper.assertBlockPresent(Blocks.DIRT, centre.offset(2, 0, 0));
        });
    }

    @GameTest(template = TEMPLATE)
    public static void anEarthShockLeavesLiveGround(GameTestHelper helper) {
        floor(helper, Blocks.STONE);
        ShockOrb orb = new ShockOrb(ModEntities.SHOCK_ORB.get(), helper.getLevel());
        Vec3 at = helper.absoluteVec(new Vec3(4.5, 3.05, 4.5));
        orb.setPos(at);
        orb.lands(new BlockHitResult(at, Direction.UP, helper.absolutePos(new BlockPos(4, 2, 4)), false));

        int live = 0;
        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 7; z++) {
                if (helper.getBlockState(new BlockPos(x, 3, z)).is(ModBlocks.SHOCK.get())) {
                    live++;
                }
            }
        }
        helper.assertTrue(live > 0, "sparks settled on the floor around the blast");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void liveGroundStingsAndSlows(GameTestHelper helper) {
        BlockPos at = new BlockPos(2, 2, 2);
        helper.setBlock(at, ModBlocks.SHOCK.get());
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, at);
        float was = pig.getHealth();
        helper.getBlockState(at).entityInside(helper.getLevel(), helper.absolutePos(at), pig);
        helper.assertTrue(pig.getHealth() < was, "it hurt");
        helper.assertTrue(pig.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "and slowed");
        helper.succeed();
    }
}
