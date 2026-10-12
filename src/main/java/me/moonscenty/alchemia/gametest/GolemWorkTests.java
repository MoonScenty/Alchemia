package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemDesign;
import me.moonscenty.alchemia.golem.GolemParts;
import me.moonscenty.alchemia.golem.seal.SealEntity;
import me.moonscenty.alchemia.golem.seal.SealPos;
import me.moonscenty.alchemia.golem.seal.SealTypes;
import me.moonscenty.alchemia.golem.seal.Seals;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Golems, step twelve finished: golems guarding, farming, felling, breaking and using things, and shooting darts. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class GolemWorkTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final GolemDesign FIGHTER = GolemDesign.BASIC.withArms(GolemParts.Arms.CLAWS);
    private static final GolemDesign SMART_FIGHTER = FIGHTER.withHead(GolemParts.Head.SMART);
    private static final GolemDesign FARMER = GolemDesign.BASIC.withArms(GolemParts.Arms.FINE)
            .withHead(GolemParts.Head.SMART);
    private static final GolemDesign GRINDER = GolemDesign.BASIC.withArms(GolemParts.Arms.BREAKERS);

    private static void floor(GameTestHelper helper) {
        for (int x = 1; x <= 11; x++) {
            for (int z = 1; z <= 11; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
    }

    private static Golem golem(GameTestHelper helper, Player owner, GolemDesign design, BlockPos at) {
        Golem golem = ModEntities.GOLEM.get().create(helper.getLevel());
        BlockPos where = helper.absolutePos(at);
        golem.moveTo(where.getX() + 0.5, where.getY(), where.getZ() + 0.5, 0.0F, 0.0F);
        golem.placed(owner.getUUID(), design, 0, where);
        helper.getLevel().addFreshEntity(golem);
        return golem;
    }

    private static SealEntity stick(GameTestHelper helper, String kind, BlockPos on, Player owner) {
        BlockPos pos = helper.absolutePos(on);
        Seals.add(helper.getLevel(), pos, Direction.UP, SealTypes.make(kind), owner);
        return Seals.get(helper.getLevel(), new SealPos(pos, Direction.UP));
    }

    /** A creature that has had its champion roll and come up ordinary, and stands still. */
    private static <T extends Mob> T still(GameTestHelper helper, EntityType<T> type, BlockPos at) {
        T mob = type.create(helper.getLevel());
        mob.setData(ModAttachments.CHAMPION, Champions.ORDINARY);
        Vec3 where = helper.absoluteVec(at.getBottomCenter());
        mob.moveTo(where.x, where.y, where.z, 0.0F, 0.0F);
        mob.setNoAi(true);
        helper.getLevel().addFreshEntity(mob);
        return mob;
    }

    /** A fighting golem goes for a monster that comes into a guarding seal's area. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aGuardSendsAGolemAtAMonster(GameTestHelper helper) {
        floor(helper);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem golem = golem(helper, owner, FIGHTER, new BlockPos(2, 2, 2));
        stick(helper, "guard", new BlockPos(6, 1, 6), owner);
        var zombie = still(helper, EntityType.ZOMBIE, new BlockPos(6, 2, 6));
        helper.succeedWhen(() -> helper.assertTrue(golem.getTarget() == zombie, "the golem is after the zombie"));
    }

    /** A guarding seal never sets a golem on its own owner. */
    @GameTest(template = TEMPLATE)
    public static void aGuardSparesTheOwner(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.STONE);
        SealEntity seal = stick(helper, "guard_advanced", new BlockPos(5, 2, 5), owner);
        Golem golem = golem(helper, owner, SMART_FIGHTER, new BlockPos(2, 2, 2));
        helper.assertFalse(new Task(seal.pos(), owner).canBeDoneBy(golem), "not at its owner");
        helper.succeed();
    }

    /** A butchering seal sends a golem after one of a herd of three, and not after one of a pair. */
    @GameTest(template = TEMPLATE, timeoutTicks = 500)
    public static void aButcherThinsAHerd(GameTestHelper helper) {
        floor(helper);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem golem = golem(helper, owner, SMART_FIGHTER, new BlockPos(2, 2, 2));
        stick(helper, "butcher", new BlockPos(6, 1, 6), owner);
        for (int cow = 0; cow < 3; cow++) {
            still(helper, EntityType.COW, new BlockPos(5 + cow, 2, 6));
        }
        helper.succeedWhen(() -> helper.assertTrue(golem.getTarget() instanceof net.minecraft.world.entity.animal.Cow,
                "the golem is after a cow"));
    }

    /** A harvesting golem cuts ripe wheat and, carrying seed, plants it again. */
    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void aHarvesterCutsAndReplants(GameTestHelper helper) {
        floor(helper);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos field = new BlockPos(6, 1, 6);
        helper.setBlock(field, Blocks.FARMLAND);
        helper.setBlock(field.above(), Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        Golem golem = golem(helper, owner, FARMER, new BlockPos(2, 2, 2));
        golem.hold(new ItemStack(Items.WHEAT_SEEDS, 4));
        stick(helper, "harvest", field, owner);
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(field.above(), CropBlock.AGE, 0);
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2))
                    .stream().anyMatch(item -> item.getItem().is(Items.WHEAT)), "and the wheat lies there");
        });
    }

    /** A breaking golem grinds away whatever is in a breaking seal's area, and it drops. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aBreakerBreaksWhatIsThere(GameTestHelper helper) {
        floor(helper);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        golem(helper, owner, GRINDER, new BlockPos(2, 2, 2));
        helper.setBlock(new BlockPos(6, 2, 6), Blocks.COBBLESTONE);
        stick(helper, "breaker", new BlockPos(6, 1, 6), owner);
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(Blocks.COBBLESTONE, new BlockPos(6, 2, 6));
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2))
                    .stream().anyMatch(item -> item.getItem().is(Items.COBBLESTONE)), "and the cobblestone drops");
        });
    }

    /** A smart golem with grinders takes a tree down from the top, log by log, until none is left. */
    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void aLumberjackFellsATree(GameTestHelper helper) {
        floor(helper);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        golem(helper, owner, GRINDER.withHead(GolemParts.Head.SMART), new BlockPos(2, 2, 2));
        for (int up = 2; up <= 4; up++) {
            helper.setBlock(new BlockPos(6, up, 6), Blocks.OAK_LOG);
        }
        stick(helper, "lumber", new BlockPos(6, 1, 6), owner);
        helper.succeedWhen(() -> {
            for (int up = 2; up <= 4; up++) {
                helper.assertBlockNotPresent(Blocks.OAK_LOG, new BlockPos(6, up, 6));
            }
        });
    }

    /** A deft, smart golem uses what it carries on the block a using seal is on: here, putting a block on it. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void aUserUsesWhatItCarries(GameTestHelper helper) {
        floor(helper);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pillar = new BlockPos(6, 2, 6);
        helper.setBlock(pillar, Blocks.STONE);
        Golem golem = golem(helper, owner, FARMER, new BlockPos(2, 2, 2));
        golem.hold(new ItemStack(Items.COBBLESTONE, 4));
        stick(helper, "use", pillar, owner);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.COBBLESTONE, pillar.above());
            helper.assertValueEqual(golem.getMainHandItem().getCount(), 3, "and the golem has three left");
        });
    }

    /** A golem with dart launchers shoots at what it is set on, from a distance. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void aDartLauncherShoots(GameTestHelper helper) {
        floor(helper);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Golem golem = golem(helper, owner, GolemDesign.BASIC.withArms(GolemParts.Arms.DARTS), new BlockPos(2, 2, 2));
        var zombie = still(helper, EntityType.ZOMBIE, new BlockPos(10, 2, 10));
        float whole = zombie.getHealth();
        golem.setTarget(zombie);
        helper.succeedWhen(() -> {
            helper.assertTrue(zombie.getHealth() < whole, "the zombie has been shot");
            helper.assertTrue(golem.distanceTo(zombie) > 2.0, "from a distance");
        });
    }
}
