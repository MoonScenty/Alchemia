package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.entity.PrimalArrow;
import me.moonscenty.alchemia.registry.ModDamageTypes;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Primal arrows: what each kind is, what it leaves on what it hits, and how a bow takes them. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class PrimalArrowTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static PrimalArrow loose(GameTestHelper helper, Player shooter, CrystalType kind) {
        ItemStack ammo = new ItemStack(ModItems.PRIMAL_ARROWS.get(kind).get());
        return (PrimalArrow) ModItems.PRIMAL_ARROWS.get(kind).get().createArrow(helper.getLevel(), ammo, shooter,
                new ItemStack(Items.BOW));
    }

    /** Each kind hits for its share of an arrow, and none can be picked up. */
    @GameTest(template = TEMPLATE)
    public static void eachKindHitsForItsShare(GameTestHelper helper) {
        Player shooter = helper.makeMockPlayer(GameType.SURVIVAL);
        PrimalArrow earth = loose(helper, shooter, CrystalType.EARTH);
        helper.assertTrue(earth.kind() == CrystalType.EARTH, "an earth arrow");
        helper.assertTrue(Math.abs(earth.getBaseDamage() - PrimalArrow.BASE_DAMAGE * 1.5) < 1.0E-6,
                "earth hits half as hard again");
        helper.assertTrue(Math.abs(loose(helper, shooter, CrystalType.ORDER).getBaseDamage()
                - PrimalArrow.BASE_DAMAGE * 0.8) < 1.0E-6, "order a little less");
        helper.assertTrue(earth.pickup == AbstractArrow.Pickup.DISALLOWED, "and nobody picks one back up");
        var types = helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        helper.assertTrue(types.getHolderOrThrow(ModDamageTypes.AIR_ARROW).is(DamageTypeTags.BYPASSES_ARMOR),
                "air goes through armour");
        helper.assertTrue(types.getHolderOrThrow(ModDamageTypes.FIRE_ARROW).is(DamageTypeTags.IS_FIRE),
                "fire is fire");
        helper.succeed();
    }

    /** A water arrow leaves what it hits slowed. */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void waterArrowSlows(GameTestHelper helper) {
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(4, 2, 8));
        Player shooter = helper.makeMockPlayer(GameType.SURVIVAL);
        PrimalArrow arrow = loose(helper, shooter, CrystalType.WATER);
        Vec3 from = helper.absoluteVec(new Vec3(4.5, 2.5, 3.5));
        arrow.moveTo(from.x, from.y, from.z);
        arrow.shoot(0.0, 0.0, 1.0, 1.5F, 0.0F);
        helper.getLevel().addFreshEntity(arrow);
        helper.succeedWhen(() -> helper.assertTrue(pig.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "slowed"));
    }

    /** A bow reaches for a primal arrow before a plain one. */
    @GameTest(template = TEMPLATE)
    public static void bowPrefersPrimalArrows(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, new ItemStack(Items.ARROW, 16));
        player.getInventory().setItem(20, new ItemStack(ModItems.PRIMAL_ARROWS.get(CrystalType.FIRE).get(), 4));
        helper.assertTrue(player.getProjectile(new ItemStack(Items.BOW)).is(
                ModItems.PRIMAL_ARROWS.get(CrystalType.FIRE).get()), "the bow takes the fire arrow");
        helper.assertTrue(player.getProjectile(new ItemStack(Items.CROSSBOW)).is(Items.ARROW),
                "but a crossbow does not");
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(Alchemia.id("primal_arrow_order")).isPresent(),
                "and they are made");
        helper.succeed();
    }
}
