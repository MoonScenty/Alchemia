package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.FireBat;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.spell.Hellbat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class HellbatTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static ItemStack wand() {
        ItemStack wand = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        WandItem.setFocus(wand, new ItemStack(ModItems.FOCI.get("hellbat").get()));
        return wand;
    }

    private static List<FireBat> bats(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(FireBat.class, new AABB(helper.absolutePos(BlockPos.ZERO))
                .inflate(40.0));
    }

    @GameTest(template = TEMPLATE)
    public static void aBatIsSetOnWhatTheWandIsPointedAt(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        // a mock player stands where it was created, not in the test plot
        Vec3 hand = helper.absoluteVec(new Vec3(2.5, 2.0, 2.5));
        caster.moveTo(hand.x, hand.y, hand.z);
        LivingEntity target = helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 2, 4));

        helper.assertTrue(Hellbat.cast(helper.getLevel(), caster, wand(), new EntityHitResult(target), 0),
                "a bat was let off");
        List<FireBat> bats = bats(helper);
        helper.assertValueEqual(bats.size(), 1, "one bat to a cast");
        FireBat bat = bats.get(0);
        helper.assertTrue(bat.isSummoned() && !bat.isHanging(), "it is the wand's, and awake");
        helper.assertTrue(bat.getTarget() == target, "and it goes for what the wand was pointed at");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void pointedAtNothingNoBatGoes(GameTestHelper helper) {
        Player caster = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockHitResult air = BlockHitResult.miss(caster.getEyePosition(), Direction.UP, caster.blockPosition());
        helper.assertFalse(Hellbat.cast(helper.getLevel(), caster, wand(), air, 0), "there was nothing to set it on");
        helper.assertTrue(bats(helper).isEmpty(), "so there is no bat");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void aBatWithNothingToChaseWithers(GameTestHelper helper) {
        FireBat bat = helper.spawn(me.moonscenty.alchemia.registry.ModEntities.FIRE_BAT.get(), new BlockPos(4, 4, 4));
        LivingEntity target = helper.spawn(EntityType.PIG, new BlockPos(10, 2, 10));
        bat.summon(target, null, 0, false, false, false);
        target.discard();
        helper.succeedWhen(() -> helper.assertFalse(bat.isAlive(), "the bat is gone"));
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aBombBurstsOnWhatItReachesAndBreaksNothing(GameTestHelper helper) {
        BlockPos floor = new BlockPos(4, 1, 4);
        helper.setBlock(floor, Blocks.STONE);
        LivingEntity target = helper.spawn(EntityType.PIG, floor.above());
        float before = target.getHealth();
        FireBat bat = helper.spawn(me.moonscenty.alchemia.registry.ModEntities.FIRE_BAT.get(), floor.above(2));
        bat.summon(target, null, 0, true, false, false);

        helper.succeedWhen(() -> {
            helper.assertFalse(bat.isAlive(), "the bomb went off");
            helper.assertTrue(target.getHealth() < before, "on the pig");
            helper.assertBlockPresent(Blocks.STONE, floor);
        });
    }

    @GameTest(template = TEMPLATE)
    public static void aDevilIsHarderToKill(GameTestHelper helper) {
        LivingEntity target = helper.spawn(EntityType.PIG, new BlockPos(10, 2, 10));
        FireBat plain = helper.spawn(me.moonscenty.alchemia.registry.ModEntities.FIRE_BAT.get(), new BlockPos(4, 4, 4));
        FireBat devil = helper.spawn(me.moonscenty.alchemia.registry.ModEntities.FIRE_BAT.get(), new BlockPos(6, 4, 4));
        plain.summon(target, null, 0, false, false, false);
        devil.summon(target, null, 0, false, true, false);
        helper.assertTrue(devil.getMaxHealth() > plain.getMaxHealth(), "a devil can take more");
        helper.assertTrue(devil.getHealth() == devil.getMaxHealth(), "and starts whole");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fireAndBurstsDoNotTouchIt(GameTestHelper helper) {
        FireBat bat = helper.spawn(me.moonscenty.alchemia.registry.ModEntities.FIRE_BAT.get(), new BlockPos(4, 4, 4));
        float before = bat.getHealth();
        bat.hurt(helper.getLevel().damageSources().onFire(), 4.0F);
        bat.hurt(helper.getLevel().damageSources().explosion(null, null), 4.0F);
        helper.assertValueEqual(bat.getHealth(), before, "it is untouched");
        helper.succeed();
    }
}
