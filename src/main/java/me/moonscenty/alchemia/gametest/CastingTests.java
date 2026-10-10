package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.Casting;
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
}
