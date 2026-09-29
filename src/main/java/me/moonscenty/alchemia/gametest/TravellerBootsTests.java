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

/**
 * What a pair of traveller's boots is worth, which is none of it armour.
 * <p>
 * Every one of these is a thing the wearer notices while walking, so every one of them is worth holding to: a
 * pair that quietly stopped doing any of them would still look and equip exactly the same.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class TravellerBootsTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos HERE = new BlockPos(8, 2, 8);

    /**
     * They lengthen the stride enough to walk up a whole block, which a pair of legs cannot.
     * <p>
     * Asked of the boots rather than of somebody wearing them. What a wearer's stride actually comes to is
     * worked out when equipment changes are noticed, which is a thing that happens to a player who is being
     * ticked; a player stood up for a test is not.
     */
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

    /**
     * A fall is forgotten as fast as it happens.
     * <p>
     * A quarter of a block a tick, which is most of a short drop and a useful slice of a long one. It is taken
     * off as the fall goes rather than at the landing, so what is left is what the ground is told about.
     */
    @GameTest(template = TEMPLATE)
    public static void aFallIsForgottenAsItHappens(GameTestHelper helper) {
        var falling = helper.makeMockPlayer(GameType.SURVIVAL);
        falling.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.TRAVELLER_BOOTS.get()));
        falling.fallDistance = 6.0F;
        TravellerBootsEvents.walked(falling);
        helper.assertValueEqual(falling.fallDistance, 5.75F, "a quarter of the fall is gone");

        // and they cannot make a fall into a rise
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

    /** Walking forward on the ground in them is faster than walking forward without them. */
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

        // standing still, they push nothing: it is a stride, not an engine
        walker.setDeltaMovement(0.0, 0.0, 0.0);
        walker.zza = 0.0F;
        TravellerBootsEvents.walked(walker);
        helper.assertValueEqual(walker.getDeltaMovement().horizontalDistanceSqr(), 0.0,
                "and standing still they push nothing");
        helper.succeed();
    }
}
