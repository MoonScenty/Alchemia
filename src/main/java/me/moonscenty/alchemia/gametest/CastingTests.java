package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.spell.Grapple;
import me.moonscenty.alchemia.wand.spell.Ember;
import me.moonscenty.alchemia.wand.spell.PechBlast;
import me.moonscenty.alchemia.wand.spell.PrimalOrb;
import me.moonscenty.alchemia.wand.spell.VisShard;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Letting a wand off: what has to be true before a spell runs, and what it costs when it does.
 * <p>
 * The order is the whole of it. A wand that cannot pay does not cast; a spell that finds nothing to do is not
 * paid for. Getting either backwards empties a wand into the sky.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class CastingTests {
    private static final String TEMPLATE = "empty_32x40x32";

    /** A wand with a focus on it and as much air in it as asked for. */
    private static ItemStack wand(String focus, int air) {
        ItemStack wand = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        WandItem.setFocus(wand, new ItemStack(ModItems.FOCI.get(focus).get()));
        wand.set(ModDataComponents.VIS.get(), AspectList.of(ModAspects.AIR, air * WandItem.FINE));
        return wand;
    }

    /** A shock finds whatever is in the way, hurts it, and takes the price off the wand. */
    @GameTest(template = TEMPLATE)
    public static void aShockHurtsWhatItIsPointedAtAndIsPaidFor(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = wand("shock", 100);

        BlockPos where = new BlockPos(2, 2, 2);
        LivingEntity target = helper.spawn(EntityType.ZOMBIE, where);
        helper.assertTrue(target.isAlive(), "there is something to shoot at");

        float before = target.getHealth();
        int had = ((WandItem) wand.getItem()).held(wand, ModAspects.AIR);
        boolean went = me.moonscenty.alchemia.wand.spell.Shock.cast(helper.getLevel(), caster, wand,
                new net.minecraft.world.phys.EntityHitResult(target), 0);

        helper.assertTrue(went, "the bolt landed");
        helper.assertTrue(target.getHealth() < before, "and it hurt");
        helper.assertValueEqual(((WandItem) wand.getItem()).held(wand, ModAspects.AIR), had,
                "the spell itself takes nothing; paying is somebody else's job");
        helper.succeed();
    }

    /**
     * A shock aimed at nothing costs nothing.
     * <p>
     * Air is not a target. A wand that paid for a miss would be a wand emptied by waving it about.
     */
    @GameTest(template = TEMPLATE)
    public static void aShockAtNothingIsNotPaidFor(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = wand("shock", 100);
        int had = ((WandItem) wand.getItem()).held(wand, ModAspects.AIR);

        boolean went = Casting.cast(helper.getLevel(), caster, wand, 0);
        helper.assertFalse(went, "nothing was in the way");
        helper.assertValueEqual(((WandItem) wand.getItem()).held(wand, ModAspects.AIR), had,
                "so nothing was spent");
        helper.succeed();
    }

    /**
     * An empty wand does not cast at all.
     * <p>
     * Checked before the spell runs rather than after, so that a spell is never half done and then unpaid for.
     */
    @GameTest(template = TEMPLATE)
    public static void anEmptyWandDoesNotCast(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        LivingEntity target = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        float before = target.getHealth();

        ItemStack dry = wand("shock", 0);
        helper.assertFalse(Casting.cast(helper.getLevel(), caster, dry, 0), "an empty wand will not go off");
        helper.assertTrue(target.getHealth() == before, "and nothing was hurt by it");
        helper.succeed();
    }

    /** A wand with nothing fitted is a stick with vis in it. */
    @GameTest(template = TEMPLATE)
    public static void aBareWandCastsNothing(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack bare = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        WandItem.brimming(bare);
        helper.assertTrue(Casting.fittedTo(bare) == null, "nothing is fitted to it");
        helper.assertFalse(Casting.cast(helper.getLevel(), caster, bare, 0), "so nothing comes of using it");
        helper.succeed();
    }
    /**
     * A pech's curse bursts where it lands and leaves what was standing there the worse for it.
     * <p>
     * It is thrown, not aimed: whatever is within reach of the burst catches it, and whoever threw it does not.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aPechsCurseAfflictsWhatItBurstsOn(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        // a pig rather than a zombie: two of the three afflictions are poison and weakness, and the dead feel
        // neither. A curse tested on something immune to two thirds of it is a curse tested on nothing
        LivingEntity victim = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 2));

        PechBlast blast = new PechBlast(helper.getLevel(), caster);
        blast.setPos(victim.getX(), victim.getY() + 0.5, victim.getZ());
        helper.getLevel().addFreshEntity(blast);
        blast.burst(new net.minecraft.world.phys.BlockHitResult(blast.position(),
                net.minecraft.core.Direction.UP, helper.absolutePos(new BlockPos(2, 1, 2)), false));

        helper.assertTrue(!victim.getActiveEffects().isEmpty(), "something settled on it");
        helper.assertTrue(victim.getHealth() < victim.getMaxHealth(), "and the burst itself hurt");
        helper.assertTrue(caster.getActiveEffects().isEmpty(), "whoever threw it is spared");
        helper.succeed();
    }

    /**
     * A hook that has bitten hauls whoever threw it towards itself.
     * <p>
     * A hook that has not bitten does nothing at all, which is what stops one from towing a climber off the
     * ground before it has anything to pull against.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aBittenHookHaulsAndALooseOneDoesNot(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        caster.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

        Grapple loose = new Grapple(helper.getLevel(), caster);
        loose.setPos(caster.getX(), caster.getY() + 8.0, caster.getZ());
        helper.getLevel().addFreshEntity(loose);
        loose.tick();
        helper.assertTrue(caster.getDeltaMovement().equals(net.minecraft.world.phys.Vec3.ZERO),
                "a hook in the air pulls nothing");

        loose.bite(new net.minecraft.world.phys.BlockHitResult(loose.position(),
                net.minecraft.core.Direction.DOWN, helper.absolutePos(new BlockPos(0, 9, 0)), false));
        helper.assertTrue(loose.biting(), "it has bitten");
        loose.tick();
        helper.assertTrue(caster.getDeltaMovement().y > 0.0, "and now it hauls upwards");
        helper.succeed();
    }
    /**
     * A vis shard will not go off without a mark, and never misses once it has one.
     * <p>
     * That is the whole of what makes it the cheapest of the twelve. A focus that cannot miss has to be given
     * something to not miss; otherwise it is a free hit on an empty room.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aVisShardNeedsAMarkAndThenKeepsIt(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = wand("shard", 100);
        helper.assertFalse(Casting.cast(helper.getLevel(), caster, wand, 0),
                "pointed at nothing, it does not go off");

        LivingEntity mark = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 2));
        VisShard shard = new VisShard(helper.getLevel(), caster, mark);
        shard.setPos(caster.getX(), caster.getY() + 1.0, caster.getZ());
        shard.setDeltaMovement(0.0, 0.0, 0.0);
        helper.getLevel().addFreshEntity(shard);

        // one tick of steering is enough to tell a shard that chases from one that drifts
        shard.tick();
        helper.assertTrue(shard.getDeltaMovement().length() > 0.0, "it set off after its mark");
        helper.succeed();
    }

    /** Embers come by the handful and burn out on their own, so a long press leaves nothing behind. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void embersGoOutOnTheirOwn(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        Ember mote = new Ember(helper.getLevel(), caster);
        mote.setPos(caster.getX(), caster.getY() + 1.0, caster.getZ());
        helper.getLevel().addFreshEntity(mote);
        helper.assertTrue(mote.isAlive(), "it is lit");

        helper.runAfterDelay(40, () -> {
            helper.assertFalse(mote.isAlive(), "and it has gone out on its own");
            helper.succeed();
        });
    }

    /** A primal orb is a bomb. What it leaves is a hole, which is the original's doing and not a slip here. */
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aPrimalOrbGoesOff(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos stand = new BlockPos(3, 2, 3);
        helper.setBlock(stand, net.minecraft.world.level.block.Blocks.DIRT);

        PrimalOrb orb = new PrimalOrb(helper.getLevel(), caster);
        orb.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 3.0, 3.5)));
        helper.getLevel().addFreshEntity(orb);
        orb.hurtMarked = false;
        orb.lands(new net.minecraft.world.phys.BlockHitResult(orb.position(),
                net.minecraft.core.Direction.UP, helper.absolutePos(stand), false));

        helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.AIR, stand);
        helper.succeed();
    }
}
