package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.HoleBlock;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import me.moonscenty.alchemia.wand.spell.Ember;
import me.moonscenty.alchemia.wand.spell.Excavation;
import me.moonscenty.alchemia.wand.spell.FireOrb;
import me.moonscenty.alchemia.wand.spell.FrostShard;
import me.moonscenty.alchemia.wand.spell.Swapping;
import me.moonscenty.alchemia.wand.spell.VisShard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class SpellTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static ItemStack wand(String focus) {
        ItemStack wand = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        WandItem.setFocus(wand, new ItemStack(ModItems.FOCI.get(focus).get()));
        AspectList filled = AspectList.EMPTY;
        for (var primal : ModAspects.primals()) {
            filled = filled.add(primal, 100 * WandItem.FINE);
        }
        wand.set(ModDataComponents.VIS.get(), filled);
        return wand;
    }

    private static BlockHitResult wall(GameTestHelper helper, BlockPos at, Direction face) {
        return new BlockHitResult(helper.absoluteVec(Vec3.atCenterOf(at)), face, helper.absolutePos(at), false);
    }

    private static ItemStack focus(String kind, FocusUpgrade... fitted) {
        ItemStack focus = new ItemStack(ModItems.FOCI.get(kind).get());
        for (FocusUpgrade upgrade : fitted) {
            FocusUpgrades.apply(focus, upgrade);
        }
        return focus;
    }

    @GameTest(template = TEMPLATE)
    public static void aFireballSparesTheGround(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos stone = new BlockPos(8, 2, 8);
        helper.setBlock(stone, Blocks.STONE);
        FireOrb orb = new FireOrb(helper.getLevel(), caster).bursting(3.0F, false);
        Vec3 at = helper.absoluteVec(Vec3.atCenterOf(stone.above()));
        orb.setPos(at.x, at.y, at.z);
        orb.lands(wall(helper, stone, Direction.UP));
        helper.assertBlockPresent(Blocks.STONE, stone);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void embersSlowInTheAir(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        Ember mote = new Ember(helper.getLevel(), caster);
        Vec3 at = helper.absoluteVec(new Vec3(8.5, 8.0, 8.5));
        mote.setPos(at.x, at.y, at.z);
        mote.setDeltaMovement(1.0, 0.0, 0.0);
        mote.tick();
        helper.assertTrue(mote.getDeltaMovement().length() < 0.95, "it loses a twentieth and more each tick");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void embersLeaveTheFireproofAlone(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        LivingEntity blaze = helper.spawn(EntityType.BLAZE, new BlockPos(8, 2, 8));
        LivingEntity zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 8));
        float blazeHad = blaze.getHealth();
        float zombieHad = zombie.getHealth();

        new Ember(helper.getLevel(), caster).lands(new EntityHitResult(blaze));
        new Ember(helper.getLevel(), caster).lands(new EntityHitResult(zombie));

        helper.assertValueEqual(blaze.getHealth(), blazeHad, "the blaze shrugs it off");
        helper.assertTrue(zombie.getHealth() < zombieHad, "the zombie does not");
        helper.assertTrue(zombie.isOnFire(), "and burns");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void splintersBounceThreeTimesAndThenBreak(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos at = new BlockPos(8, 2, 8);
        helper.setBlock(at, Blocks.STONE);
        FrostShard shard = new FrostShard(helper.getLevel(), caster).biting(1.0F, 0).asSplinter();
        shard.setDeltaMovement(1.0, 0.0, 0.0);

        shard.lands(wall(helper, at, Direction.WEST));
        helper.assertTrue(shard.isAlive(), "it comes off the wall");
        helper.assertTrue(shard.getDeltaMovement().x < 0.0, "going back the way it came");
        helper.assertTrue(Math.abs(shard.getDeltaMovement().x) < 0.51, "at half speed");
        shard.lands(wall(helper, at, Direction.WEST));
        shard.lands(wall(helper, at, Direction.WEST));
        helper.assertTrue(shard.isAlive(), "the third bounce is its last");
        shard.lands(wall(helper, at, Direction.WEST));
        helper.assertFalse(shard.isAlive(), "and then it breaks");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aSplinterGetsPastTheGrace(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        LivingEntity zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        zombie.hurt(zombie.damageSources().generic(), 5.0F);
        float had = zombie.getHealth();
        FrostShard shard = new FrostShard(helper.getLevel(), caster).biting(1.0F, 0).asSplinter();
        shard.tickCount = 10;
        shard.setDeltaMovement(1.0, 0.0, 0.0);

        shard.lands(new EntityHitResult(zombie));
        helper.assertTrue(zombie.getHealth() < had, "it still drew blood");
        helper.assertFalse(shard.isAlive(), "and a splinter dies in the wound");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anIceSpikeCarriesOnThroughABody(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        LivingEntity zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        FrostShard shard = new FrostShard(helper.getLevel(), caster).biting(3.0F, 0);
        shard.setDeltaMovement(1.0, 0.0, 0.0);
        shard.lands(new EntityHitResult(zombie));
        helper.assertTrue(shard.isAlive(), "a spike is not spent by one hit");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aVisShardBouncesAndKeepsSeeking(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        LivingEntity mark = helper.spawn(EntityType.PIG, new BlockPos(8, 2, 8));
        BlockPos at = new BlockPos(2, 2, 2);
        helper.setBlock(at, Blocks.STONE);
        VisShard shard = new VisShard(helper.getLevel(), caster, mark);
        shard.setDeltaMovement(0.5, 0.0, 0.0);
        shard.lands(wall(helper, at, Direction.WEST));
        helper.assertTrue(shard.isAlive(), "a wall does not stop it");
        helper.assertTrue(shard.getDeltaMovement().x < 0.0, "it comes back off");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void eachFocusReachesItsOwnDistance(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        caster.moveTo(helper.absoluteVec(new Vec3(8.5, 2.0, 2.5)).x, helper.absoluteVec(new Vec3(8.5, 2.0, 2.5)).y,
                helper.absoluteVec(new Vec3(8.5, 2.0, 2.5)).z, 0.0F, 0.0F);
        caster.setYHeadRot(0.0F);
        BlockPos stone = new BlockPos(8, 3, 11);
        helper.setBlock(stone, Blocks.STONE);

        HitResult dug = Casting.aim(caster, Casting.fitted(wand("excavation")));
        HitResult bored = Casting.aim(caster, Casting.fitted(wand("hole")));
        helper.assertTrue(dug.getType() == HitResult.Type.BLOCK, "the beam reaches eight blocks");
        helper.assertTrue(bored.getType() == HitResult.Type.MISS, "the hole only reaches arm's length");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aPortableHoleRefusesTheBlacklist(GameTestHelper helper) {
        helper.assertTrue(HoleBlock.borable(Blocks.STONE.defaultBlockState()), "stone opens");
        for (Block refused : new Block[] {Blocks.PISTON, Blocks.OAK_DOOR, Blocks.RED_BED, ModBlocks.INFERNAL_FURNACE.get()}) {
            helper.assertFalse(HoleBlock.borable(refused.defaultBlockState()), refused + " does not");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void someUpgradesHaveConditions(GameTestHelper helper) {
        helper.assertFalse(FocusUpgrades.offers(focus("shock"), 4).contains(FocusUpgrade.ENLARGE),
                "a plain shock cannot be enlarged");
        helper.assertTrue(FocusUpgrades.offers(focus("shock", FocusUpgrade.CHAIN_LIGHTNING), 4).contains(FocusUpgrade.ENLARGE),
                "a chain can");
        helper.assertTrue(FocusUpgrades.offers(focus("fire", FocusUpgrade.ALCHEMISTS_FIRE), 4).contains(FocusUpgrade.ALCHEMISTS_FIRE),
                "embers take a second dose");
        helper.assertFalse(FocusUpgrades.offers(focus("fire", FocusUpgrade.ALCHEMISTS_FIRE, FocusUpgrade.FIREBALL), 4)
                .contains(FocusUpgrade.ALCHEMISTS_FIRE), "a fireball only one");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aTradeSkipsTheBuriedAndFillsThePockets(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = wand("trade");
        WandItem.setFocus(wand, withPicked(WandItem.focus(wand), Blocks.STONE));
        caster.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, wand);
        caster.getInventory().add(new ItemStack(Items.STONE, 64));

        BlockPos first = new BlockPos(4, 4, 4);
        helper.setBlock(first, Blocks.COAL_ORE);
        BlockPos diagonal = first.offset(1, 1, 1);
        helper.setBlock(diagonal, Blocks.COAL_ORE);
        BlockPos buried = first.offset(-1, 0, 0);
        helper.setBlock(buried, Blocks.COAL_ORE);
        for (Direction side : Direction.values()) {
            if (!buried.relative(side).equals(first)) {
                helper.setBlock(buried.relative(side), Blocks.DIAMOND_BLOCK);
            }
        }
        helper.setBlock(first, Blocks.DIAMOND_BLOCK);
        helper.setBlock(first, Blocks.COAL_ORE);

        helper.assertTrue(Swapping.trade(helper.getLevel(), caster, wand, wall(helper, first, Direction.UP), 0), "it went");
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.STONE, first);
            helper.assertBlockPresent(Blocks.STONE, diagonal);
            helper.assertBlockPresent(Blocks.COAL_ORE, buried);
            helper.assertTrue(caster.getInventory().contains(new ItemStack(Items.COAL)), "the coal went into the bag");
        });
    }

    private static ItemStack withPicked(ItemStack focus, Block block) {
        ItemStack picked = focus.copy();
        picked.set(ModDataComponents.PICKED_BLOCK.get(), block.builtInRegistryHolder());
        return picked;
    }

    @GameTest(template = TEMPLATE)
    public static void netherrackCountsAsRock(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = wand("excavation");
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at, Blocks.NETHERRACK);
        BlockHitResult struck = wall(helper, at, Direction.UP);
        Excavation.cast(helper.getLevel(), caster, wand, struck, 0);
        Excavation.cast(helper.getLevel(), caster, wand, struck, 1);
        helper.assertBlockNotPresent(Blocks.NETHERRACK, at);
        helper.succeed();
    }
}
