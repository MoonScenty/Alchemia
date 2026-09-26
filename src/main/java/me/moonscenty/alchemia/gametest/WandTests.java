package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import io.netty.buffer.Unpooled;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandRod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * What a wand is made of has to actually change what it does, and the cap's fraction has to survive being paid out
 * — those are the two places where storing vis in whole points instead of hundredths would quietly go wrong.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class WandTests {
    private static final String TEMPLATE = "empty_32x40x32";

    private static ItemStack wand(Holder<WandRod> rod, Holder<WandCap> cap, int fire) {
        ItemStack stack = WandItem.of(rod, cap);
        stack.set(ModDataComponents.VIS.get(),
                AspectList.of(ModAspects.FIRE, fire * WandItem.FINE));
        return stack;
    }

    /** The rod alone decides how much fits. */
    @GameTest(template = TEMPLATE)
    public static void theRodSaysHowMuchItHolds(GameTestHelper helper) {
        helper.assertValueEqual(WandItem.capacity(WandItem.of(ModWandParts.WOOD, ModWandParts.IRON)),
                100, "a stick holds a hundred");
        helper.assertValueEqual(WandItem.capacity(WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.IRON)),
                500, "silverwood holds five hundred");
        helper.succeed();
    }

    /**
     * An iron cap pays a tenth over the asking price and an alchemium one a tenth under, so the same wand with the
     * same vis in it can afford the work with one and not the other.
     */
    @GameTest(template = TEMPLATE)
    public static void theCapChangesWhatItCosts(GameTestHelper helper) {
        AspectList price = AspectList.of(ModAspects.FIRE, 100);
        ItemStack iron = wand(ModWandParts.WOOD, ModWandParts.IRON, 100);
        ItemStack alchemium = wand(ModWandParts.WOOD, ModWandParts.ALCHEMIUM, 100);

        helper.assertFalse(iron.getItem() instanceof WandItem item && item.holds(iron, price),
                "an iron cap asks for more than a full stick holds");
        helper.assertTrue(alchemium.getItem() instanceof WandItem item && item.holds(alchemium, price),
                "an alchemium cap asks for less");

        ((WandItem) alchemium.getItem()).take(alchemium, price, null);
        // ninety paid out of a hundred, so ten left over and none of it rounded away
        helper.assertValueEqual(((WandItem) alchemium.getItem()).held(alchemium, ModAspects.FIRE),
                10, "the tenth it saved is still in the wand");
        helper.succeed();
    }

    /** A wand fills itself from the aura around it, and stops at what the rod holds. */
    @GameTest(template = TEMPLATE)
    public static void itDrawsFromTheAura(GameTestHelper helper) {
        BlockPos where = helper.absolutePos(new BlockPos(2, 1, 2));
        AuraHandler.add(helper.getLevel(), where, ModAspects.FIRE, 50);
        int before = AuraHandler.get(helper.getLevel(), where, ModAspects.FIRE);
        helper.assertTrue(before >= 50, "the test chunk was given fire to draw on");

        int drawn = AuraHandler.drainAvailable(helper.getLevel(), where, ModAspects.FIRE, 10);
        helper.assertTrue(drawn > 0, "there was fire to be had");
        helper.assertValueEqual(AuraHandler.get(helper.getLevel(), where, ModAspects.FIRE),
                before - drawn, "what the wand took is what the chunk lost");
        helper.succeed();
    }

    /**
     * The recipe that puts a wand together has to survive being sent to a client.
     * <p>
     * It carries nothing, so it is written with a codec that sends no bytes at all — but that codec refuses to
     * write anything that is not the very value it was built around, and the recipe read back out of the file is a
     * different object. Every one of these has to count as the same one, or joining a world throws.
     */
    @GameTest(template = TEMPLATE)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void theWandRecipeSurvivesBeingSent(GameTestHelper helper) {
        RecipeHolder<?> found = helper.getLevel().getRecipeManager()
                .byKey(Alchemia.id("arcane_wand")).orElse(null);
        helper.assertTrue(found != null, "the bench knows how to put a wand together");

        RegistryFriendlyByteBuf buffer =
                new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        ((StreamCodec) found.value().getSerializer().streamCodec()).encode(buffer, found.value());
        helper.succeed();
    }
}
