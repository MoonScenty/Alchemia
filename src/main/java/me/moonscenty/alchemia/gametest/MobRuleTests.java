package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.AdvancedAutomatedCrossbow;
import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.entity.ChampionModifier;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.EldritchCrab;
import me.moonscenty.alchemia.entity.EldritchGuardian;
import me.moonscenty.alchemia.entity.Pech;
import me.moonscenty.alchemia.entity.Wisp;
import me.moonscenty.alchemia.entity.cult.GolemOrb;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class MobRuleTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos AT = new BlockPos(8, 2, 8);

    private static AdvancedAutomatedCrossbow crossbow(GameTestHelper helper, Player owner) {
        AdvancedAutomatedCrossbow turret = helper.spawnWithNoFreeWill(ModEntities.ADVANCED_AUTOMATED_CROSSBOW.get(), AT);
        turret.setOwner(owner.getUUID());
        return turret;
    }

    private static Autocaster caster(GameTestHelper helper) {
        Autocaster turret = new Autocaster(helper.getLevel(), helper.absolutePos(AT), Direction.UP);
        helper.getLevel().addFreshEntity(turret);
        return turret;
    }

    @GameTest(template = TEMPLATE)
    public static void aConstructWillNotTurnOnItsOwner(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
        AdvancedAutomatedCrossbow turret = crossbow(helper, owner);
        helper.assertTrue(turret.isAlliedTo(owner), "its owner is a friend");
        helper.assertFalse(turret.isAlliedTo(stranger), "a stranger is not");
        turret.mind(Autocaster.FRIENDS, true);
        helper.assertFalse(turret.isAlliedTo(owner), "minding friends takes that away");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void villagersAreAnimalsButConstructsAreNot(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        AdvancedAutomatedCrossbow turret = crossbow(helper, owner);
        AdvancedAutomatedCrossbow other = helper.spawnWithNoFreeWill(ModEntities.ADVANCED_AUTOMATED_CROSSBOW.get(),
                AT.east(3));
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, AT.west(3));
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, AT.north(3));
        turret.mind(Autocaster.ANIMALS, true);
        turret.mind(Autocaster.MONSTERS, true);
        helper.assertTrue(turret.worthShooting(villager), "a villager counts as an animal, as in the original");
        helper.assertFalse(turret.worthShooting(other), "nor is another construct");
        helper.assertTrue(turret.worthShooting(cow), "a cow is");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void mindingFriendsLeavesTheUnownedAlone(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        AdvancedAutomatedCrossbow turret = crossbow(helper, owner);
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, AT.north(3));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, AT.south(3));
        turret.mind(Autocaster.ANIMALS, true);
        turret.mind(Autocaster.MONSTERS, true);
        turret.mind(Autocaster.FRIENDS, true);
        helper.assertFalse(turret.worthShooting(cow), "a wild cow is nobody's friend");
        helper.assertFalse(turret.worthShooting(zombie), "nor a zombie");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void mindingFriendsMakesTeammatesFairGame(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        AdvancedAutomatedCrossbow turret = crossbow(helper, owner);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, AT.south(3));
        var board = helper.getLevel().getScoreboard();
        var team = board.addPlayerTeam("alchemia_friends_" + helper.absolutePos(AT).asLong());
        board.addPlayerToTeam(turret.getScoreboardName(), team);
        board.addPlayerToTeam(zombie.getScoreboardName(), team);
        try {
            helper.assertTrue(turret.isAlliedTo(zombie), "a teammate is an ally");
            turret.mind(Autocaster.FRIENDS, true);
            helper.assertFalse(turret.isAlliedTo(zombie), "but minding friends, the turret has no allies");
        } finally {
            board.removePlayerTeam(team);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anAutocasterFiresOnlyWhereItIsLooking(GameTestHelper helper) {
        Autocaster turret = caster(helper);
        Pig mark = helper.spawnWithNoFreeWill(EntityType.PIG, AT.south(6));
        turret.setXRot(0.0F);
        turret.setYHeadRot(0.0F);
        turret.xRotO = 0.0F;
        turret.yHeadRotO = 0.0F;
        helper.assertTrue(turret.aimedAt(mark, 16.0F), "straight ahead is in the cone");
        helper.assertFalse(turret.aimedAt(mark, 3.0F), "but not past its range");
        turret.setYHeadRot(90.0F);
        turret.yHeadRotO = 90.0F;
        helper.assertFalse(turret.aimedAt(mark, 16.0F), "and not off to the side");
        turret.discard();
        helper.succeed();
    }

    private static boolean thornsLand(GameTestHelper helper, float shield) {
        EldritchCrab crab = helper.spawnWithNoFreeWill(ModEntities.ELDRITCH_CRAB.get(), AT);
        Zombie biter = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, AT.east(2));
        Champions.make(crab, ChampionModifier.SPINED);
        crab.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(20.0);
        crab.setAbsorptionAmount(shield);
        float whole = biter.getHealth();
        crab.hurt(crab.damageSources().mobAttack(biter), 1.0F);
        boolean landed = biter.getHealth() < whole;
        crab.discard();
        biter.discard();
        return landed;
    }

    @GameTest(template = TEMPLATE)
    public static void aShieldedEldritchMobKeepsItsSpinesIn(GameTestHelper helper) {
        helper.assertFalse(thornsLand(helper, 10.0F), "while the shield holds, nothing else happens");
        helper.assertTrue(thornsLand(helper, 0.0F), "with it down, the spines bite");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBurningGuardianSetsAlight(GameTestHelper helper) {
        EldritchGuardian guardian = helper.spawnWithNoFreeWill(ModEntities.ELDRITCH_GUARDIAN.get(), AT);
        LivingEntity pig = helper.spawnWithNoFreeWill(EntityType.PIG, AT.east());
        guardian.igniteForSeconds(100.0F);
        for (int i = 0; i < 60 && !pig.isOnFire(); i++) {
            pig.invulnerableTime = 0;
            pig.setHealth(pig.getMaxHealth());
            guardian.doHurtTarget(pig);
        }
        helper.assertTrue(pig.isOnFire(), "its blows should set the pig alight");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aWispDoesNotDrown(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(AT.offset(-2, 0, -2), AT.offset(2, 3, 2))) {
            helper.setBlock(pos, Blocks.WATER);
        }
        Wisp wisp = helper.spawnWithNoFreeWill(ModEntities.WISP.get(), AT.above());
        int full = wisp.getMaxAirSupply();
        helper.runAfterDelay(40, () -> {
            helper.assertValueEqual(wisp.getAirSupply(), full, "its air should not run down");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void pechAndTaintedBeastsKeepOutOfWater(GameTestHelper helper) {
        Pech pech = helper.spawnWithNoFreeWill(ModEntities.PECH.get(), AT);
        LivingEntity cow = helper.spawnWithNoFreeWill(ModEntities.TAINTED_COW.get(), AT.east(2));
        LivingEntity chicken = helper.spawnWithNoFreeWill(ModEntities.TAINTED_CHICKEN.get(), AT.west(2));
        helper.assertTrue(pech.getPathfindingMalus(PathType.WATER) < 0.0F, "the pech keeps out");
        helper.assertTrue(((net.minecraft.world.entity.Mob) cow).getPathfindingMalus(PathType.WATER) < 0.0F, "the cow keeps out");
        helper.assertTrue(((net.minecraft.world.entity.Mob) chicken).getPathfindingMalus(PathType.WATER) >= 0.0F,
                "the chicken does not mind");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aGolemOrbCannotBeStruck(GameTestHelper helper) {
        Zombie thrower = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, AT);
        GolemOrb orb = new GolemOrb(helper.getLevel(), thrower, null, true);
        helper.assertFalse(orb.isPickable(), "a swing should pass through it");
        helper.succeed();
    }
}
