package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.TravellerBootsEvents;
import me.moonscenty.alchemia.item.TravellerBootsItem;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TravellerBootsTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 2, 8);

    // asks the boots directly: a test player is not ticked, so equipment changes never apply
    @GameTest(template = TEMPLATE)
    public static void theyMakeAStepOfAWholeBlock(GameTestHelper helper) {
        var walker = helper.makeMockPlayer(GameType.SURVIVAL);
        double bare = walker.getAttributeValue(Attributes.STEP_HEIGHT);
        helper.assertTrue(bare < 1.0, "bare legs do not make a step of a whole block: " + bare);

        double[] adds = {0.0};
        new ItemStack(ModItems.TRAVELLER_BOOTS.get()).forEachModifier(EquipmentSlot.FEET, (attribute, modifier) -> {
            if (attribute.value() == Attributes.STEP_HEIGHT.value()) {
                adds[0] += modifier.amount();
            }
        });

        helper.assertValueEqual(adds[0], TravellerBootsItem.STRIDES, "and the boots add their own stride");
        helper.assertTrue(bare + adds[0] >= 1.0,
                "which is enough between them: " + (bare + adds[0]));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aFallIsForgottenAsItHappens(GameTestHelper helper) {
        var falling = helper.makeMockPlayer(GameType.SURVIVAL);
        falling.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.TRAVELLER_BOOTS.get()));
        falling.fallDistance = 6.0F;
        TravellerBootsEvents.walked(falling);
        helper.assertValueEqual(falling.fallDistance, 5.75F, "a quarter of the fall is gone");

        falling.fallDistance = 0.1F;
        TravellerBootsEvents.walked(falling);
        helper.assertValueEqual(falling.fallDistance, 0.0F, "a fall this short is simply gone");

        var bare = helper.makeMockPlayer(GameType.SURVIVAL);
        bare.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        bare.fallDistance = 6.0F;
        TravellerBootsEvents.walked(bare);
        helper.assertValueEqual(bare.fallDistance, 6.0F, "and ordinary boots forget nothing");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theyPushAlongTheGround(GameTestHelper helper) {
        helper.setBlock(HERE.below(), net.minecraft.world.level.block.Blocks.STONE);
        var walker = helper.makeMockPlayer(GameType.SURVIVAL);
        walker.setPos(helper.absoluteVec(HERE.getCenter()));
        walker.setOnGround(true);
        walker.zza = 1.0F;
        walker.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.TRAVELLER_BOOTS.get()));

        var before = walker.getDeltaMovement();
        TravellerBootsEvents.walked(walker);
        var after = walker.getDeltaMovement();

        helper.assertTrue(after.horizontalDistanceSqr() > before.horizontalDistanceSqr(),
                "the step pushed them along");

        walker.setDeltaMovement(0.0, 0.0, 0.0);
        walker.zza = 0.0F;
        TravellerBootsEvents.walked(walker);
        helper.assertValueEqual(walker.getDeltaMovement().horizontalDistanceSqr(), 0.0,
                "and standing still they push nothing");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aJumpInThemGoesHigher(GameTestHelper helper) {
        var jumper = helper.makeMockPlayer(GameType.SURVIVAL);
        jumper.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.TRAVELLER_BOOTS.get()));
        jumper.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        TravellerBootsEvents.jumped(jumper);
        helper.assertValueEqual(jumper.getDeltaMovement().y, TravellerBootsEvents.JUMPS, "lifted the more");

        var bare = helper.makeMockPlayer(GameType.SURVIVAL);
        bare.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        bare.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        TravellerBootsEvents.jumped(bare);
        helper.assertValueEqual(bare.getDeltaMovement().y, 0.0, "other boots do nothing");
        helper.succeed();
    }
}
