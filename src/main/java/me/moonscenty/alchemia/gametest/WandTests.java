package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import io.netty.buffer.Unpooled;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.item.FocusPouchItem;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.wand.Foci;
import me.moonscenty.alchemia.wand.Focus;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
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

    /**
     * Twelve foci, each with a price, and every one of them in the tag a pouch slot will ask.
     * <p>
     * A focus with no price would be free to use and a focus outside the tag could not be put away, and neither
     * would show up anywhere until the thing that reads it was written.
     */
    @GameTest(template = TEMPLATE)
    public static void thereAreTwelveFociAndEachCostsSomething(GameTestHelper helper) {
        helper.assertValueEqual(ModItems.FOCI.size(), 12, "twelve foci");
        ModItems.FOCI.forEach((name, held) -> {
            FocusItem focus = held.get();
            helper.assertTrue(!focus.focus().cost().isEmpty(), name + " costs something to use");
            helper.assertTrue(new ItemStack(focus).is(ModTags.Items.FOCI), name + " is a focus to a slot");
        });
        helper.succeed();
    }

    /**
     * The numbers the original gave a few of them, spot-checked.
     * <p>
     * These are the two kinds of price there are -- one charged when the wand goes off, one charged every tick it
     * is held down -- and getting the flag the wrong way round would make a portable hole cost its whole bill
     * sixty times a second.
     */
    @GameTest(template = TEMPLATE)
    public static void theFociKeptTheirNumbers(GameTestHelper helper) {
        Focus fire = ModItems.FOCI.get("fire").get().focus();
        helper.assertValueEqual(fire.cost().get(ModAspects.FIRE), 2, "fire asks two fire");
        helper.assertTrue(fire.perTick(), "fire is charged by the tick");
        helper.assertTrue(fire.turret(), "an autocaster will take fire");

        Focus hole = ModItems.FOCI.get("hole").get().focus();
        helper.assertValueEqual(hole.cost().get(ModAspects.EARTH), 25, "a hole asks twenty-five earth");
        helper.assertTrue(!hole.perTick(), "a hole is charged once");
        helper.assertTrue(!hole.turret(), "no autocaster takes a hole");

        Focus primal = ModItems.FOCI.get("primal").get().focus();
        helper.assertValueEqual(primal.cost().size(), 6, "primal asks all six");
        helper.assertValueEqual(primal.cooldown(), 500, "primal waits half a second");
        helper.succeed();
    }

    /**
     * Fitting a focus takes it out of the bag, and taking it off puts it back.
     * <p>
     * Nothing may be made or lost on the way. A swap that forgot to put the old focus somewhere would eat it, and
     * one that forgot to empty the slot it came from would hand out a second copy.
     */
    @GameTest(template = TEMPLATE)
    public static void fittingAFocusMovesItRatherThanCopyingIt(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = WandItem.of(ModWandParts.WOOD, ModWandParts.IRON);
        player.getInventory().add(new ItemStack(ModItems.FOCI.get("fire").get()));

        Foci.next(player, wand);
        helper.assertTrue(WandItem.focus(wand).is(ModItems.FOCI.get("fire").get()), "the fire focus is on the wand");
        helper.assertValueEqual(player.getInventory().countItem(ModItems.FOCI.get("fire").get()),
                0, "and no longer in the bag");

        Foci.remove(player, wand);
        helper.assertTrue(WandItem.focus(wand).isEmpty(), "the wand is bare again");
        helper.assertValueEqual(player.getInventory().countItem(ModItems.FOCI.get("fire").get()),
                1, "and the focus is back in the bag");
        helper.succeed();
    }

    /**
     * The cycle runs by name and comes back round, and never leaves two foci where there was one.
     * <p>
     * Sorting by name rather than by where they sit means tidying a bag does not change which comes next. Coming
     * back round means a player carrying one focus can put it on and take it off with the same key.
     */
    @GameTest(template = TEMPLATE)
    public static void theCycleGoesRoundInOrderAndLosesNothing(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = WandItem.of(ModWandParts.WOOD, ModWandParts.IRON);
        // put in backwards, to prove the order comes from the names and not from the slots
        player.getInventory().add(new ItemStack(ModItems.FOCI.get("frost").get()));
        player.getInventory().add(new ItemStack(ModItems.FOCI.get("fire").get()));
        player.getInventory().add(new ItemStack(ModItems.FOCI.get("builder").get()));

        helper.assertTrue(Foci.next(player, wand).is(ModItems.FOCI.get("builder").get()), "builder comes first");
        helper.assertTrue(Foci.next(player, wand).is(ModItems.FOCI.get("fire").get()), "then fire");
        helper.assertTrue(Foci.next(player, wand).is(ModItems.FOCI.get("frost").get()), "then frost");
        helper.assertTrue(Foci.next(player, wand).is(ModItems.FOCI.get("builder").get()), "and round again");

        int carried = 0;
        for (var focus : ModItems.FOCI.values()) {
            carried += player.getInventory().countItem(focus.get());
        }
        helper.assertValueEqual(carried, 2, "two in the bag and one on the wand, all the way round");
        helper.succeed();
    }

    /** A focus survives being written down and read back, which is what carrying it between sessions is. */
    @GameTest(template = TEMPLATE)
    public static void aFittedFocusSurvivesBeingSaved(GameTestHelper helper) {
        ItemStack wand = WandItem.of(ModWandParts.WOOD, ModWandParts.IRON);
        WandItem.setFocus(wand, new ItemStack(ModItems.FOCI.get("shock").get()));

        var registries = helper.getLevel().registryAccess();
        ItemStack read = ItemStack.parse(registries, (net.minecraft.nbt.CompoundTag)
                        ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), wand)
                                .getOrThrow())
                .orElseThrow();
        helper.assertTrue(WandItem.focus(read).is(ModItems.FOCI.get("shock").get()),
                "the focus came back with the wand");
        helper.succeed();
    }

    /**
     * A pouch carries what is put in it, and an emptied one is the same item as a new one.
     * <p>
     * If an emptied pouch kept a component full of nothing it would no longer stack with or compare equal to a
     * fresh one, which is the sort of difference nobody can see and everybody trips over.
     */
    @GameTest(template = TEMPLATE)
    public static void aPouchCarriesWhatIsPutInIt(GameTestHelper helper) {
        ItemStack pouch = new ItemStack(ModItems.FOCUS_POUCH.get());
        List<ItemStack> inside = new ArrayList<>();
        for (int slot = 0; slot < FocusPouchItem.SIZE; slot++) {
            inside.add(ItemStack.EMPTY);
        }
        inside.set(4, new ItemStack(ModItems.FOCI.get("frost").get()));
        FocusPouchItem.setContents(pouch, inside);

        helper.assertTrue(FocusPouchItem.item(pouch, 4).is(ModItems.FOCI.get("frost").get()),
                "the frost focus is in the fifth square");
        helper.assertTrue(FocusPouchItem.item(pouch, 0).isEmpty(), "and the first is empty");

        inside.set(4, ItemStack.EMPTY);
        FocusPouchItem.setContents(pouch, inside);
        helper.assertTrue(ItemStack.isSameItemSameComponents(pouch, new ItemStack(ModItems.FOCUS_POUCH.get())),
                "an emptied pouch is a plain pouch again");
        helper.succeed();
    }

    /**
     * A focus in a pouch is a focus the player is carrying.
     * <p>
     * That is the whole of what a pouch is worth: every focus on one key without eighteen squares gone out of the
     * bag. Taking one off again goes back into the pouch rather than loose, or the bag fills up anyway.
     */
    @GameTest(template = TEMPLATE)
    public static void theWandReachesIntoAPouch(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = WandItem.of(ModWandParts.WOOD, ModWandParts.IRON);

        ItemStack pouch = new ItemStack(ModItems.FOCUS_POUCH.get());
        List<ItemStack> inside = new ArrayList<>();
        for (int slot = 0; slot < FocusPouchItem.SIZE; slot++) {
            inside.add(ItemStack.EMPTY);
        }
        inside.set(7, new ItemStack(ModItems.FOCI.get("shock").get()));
        FocusPouchItem.setContents(pouch, inside);
        player.getInventory().add(pouch);

        helper.assertTrue(Foci.next(player, wand).is(ModItems.FOCI.get("shock").get()),
                "the focus came out of the pouch");
        helper.assertTrue(FocusPouchItem.item(player.getInventory().getItem(0), 7).isEmpty(),
                "and its square in the pouch is empty");
        helper.assertValueEqual(player.getInventory().countItem(ModItems.FOCI.get("shock").get()),
                0, "it is not loose in the bag either");

        Foci.remove(player, wand);
        helper.assertTrue(FocusPouchItem.item(player.getInventory().getItem(0), 0)
                        .is(ModItems.FOCI.get("shock").get()),
                "taking it off put it back in the pouch");
        helper.succeed();
    }
}
