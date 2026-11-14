package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import io.netty.buffer.Unpooled;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.item.FocusPouchItem;
import me.moonscenty.alchemia.item.RobeItem;
import me.moonscenty.alchemia.item.VisDiscount;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import me.moonscenty.alchemia.item.WandItem;
import net.minecraft.world.entity.EquipmentSlot;
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

    @GameTest(template = TEMPLATE)
    public static void theRodSaysHowMuchItHolds(GameTestHelper helper) {
        helper.assertValueEqual(WandItem.capacity(WandItem.of(ModWandParts.WOOD, ModWandParts.IRON)),
                100, "a stick holds a hundred");
        helper.assertValueEqual(WandItem.capacity(WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.IRON)),
                500, "silverwood holds five hundred");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theCapChangesWhatItCosts(GameTestHelper helper) {
        AspectList price = AspectList.of(ModAspects.FIRE, 100);
        ItemStack iron = wand(ModWandParts.WOOD, ModWandParts.IRON, 100);
        ItemStack thaumium = wand(ModWandParts.WOOD, ModWandParts.THAUMIUM, 100);

        helper.assertFalse(iron.getItem() instanceof WandItem item && item.holds(iron, price, null, me.moonscenty.alchemia.item.VisHolder.Spending.WORKING),
                "an iron cap asks for more than a full stick holds");
        helper.assertTrue(thaumium.getItem() instanceof WandItem item && item.holds(thaumium, price, null, me.moonscenty.alchemia.item.VisHolder.Spending.WORKING),
                "an thaumium cap asks for less");

        ((WandItem) thaumium.getItem()).take(thaumium, price, null, me.moonscenty.alchemia.item.VisHolder.Spending.WORKING);
        helper.assertValueEqual(((WandItem) thaumium.getItem()).held(thaumium, ModAspects.FIRE),
                10, "the tenth it saved is still in the wand");
        helper.succeed();
    }

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

    @GameTest(template = TEMPLATE)
    public static void thereAreTwelveFociAndEachCostsSomething(GameTestHelper helper) {
        helper.assertValueEqual(ModItems.FOCI.size(), 12, "twelve foci");
        ModItems.FOCI.forEach((name, held) -> {
            FocusItem focus = held.get();
            helper.assertTrue(!focus.base().cost().isEmpty(), name + " costs something to use");
            helper.assertTrue(new ItemStack(focus).is(ModTags.Items.FOCI), name + " is a focus to a slot");
        });
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theFociKeptTheirNumbers(GameTestHelper helper) {
        Focus fire = ModItems.FOCI.get("fire").get().base();
        helper.assertValueEqual(fire.cost().get(ModAspects.FIRE), 2, "fire asks two fire");
        helper.assertTrue(fire.perTick(), "fire is charged by the tick");
        helper.assertTrue(fire.turret() != null, "an autocaster will take fire");

        Focus hole = ModItems.FOCI.get("hole").get().base();
        helper.assertValueEqual(hole.cost().get(ModAspects.EARTH), 25, "a hole asks twenty-five earth");
        helper.assertTrue(!hole.perTick(), "a hole is charged once");
        helper.assertTrue(hole.turret() == null, "no autocaster takes a hole");

        Focus primal = ModItems.FOCI.get("primal").get().base();
        helper.assertValueEqual(primal.cost().size(), 6, "primal asks all six");
        helper.assertValueEqual(primal.cooldown(), 500, "primal waits half a second");
        helper.succeed();
    }

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

    @GameTest(template = TEMPLATE)
    public static void twoOfAKindAreToldApartByWhatIsInThem(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = WandItem.of(ModWandParts.WOOD, ModWandParts.IRON);
        ItemStack plain = new ItemStack(ModItems.FOCI.get("shock").get());
        ItemStack worked = new ItemStack(ModItems.FOCI.get("shock").get());
        me.moonscenty.alchemia.wand.FocusUpgrades.apply(worked,
                me.moonscenty.alchemia.wand.FocusUpgrade.POTENCY);
        // read the names first: inserting a stack into the bag empties the stack passed in
        String plainly = Foci.name(plain);
        String workedly = Foci.name(worked);
        player.getInventory().add(plain);
        player.getInventory().add(worked);

        helper.assertTrue(!plainly.equals(workedly), "they are not called the same thing");
        helper.assertValueEqual(Foci.carried(player).size(), 2, "and the ring would show both");

        Foci.next(player, wand);
        String first = Foci.name(Foci.on(wand));
        Foci.next(player, wand);
        helper.assertTrue(!Foci.name(Foci.on(wand)).equals(first), "the cycle reaches the second one");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theRingFitsWhatItNames(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = WandItem.of(ModWandParts.WOOD, ModWandParts.IRON);
        player.getInventory().add(new ItemStack(ModItems.FOCI.get("frost").get()));
        player.getInventory().add(new ItemStack(ModItems.FOCI.get("shock").get()));

        String shock = Foci.name(new ItemStack(ModItems.FOCI.get("shock").get()));
        helper.assertTrue(Foci.fit(player, wand, shock).is(ModItems.FOCI.get("shock").get()),
                "the one that was named is the one that went on");
        helper.assertTrue(Foci.fit(player, wand, shock).isEmpty(), "and asking for it again does nothing");
        helper.assertTrue(Foci.fit(player, wand, "alchemia:focus_hole").isEmpty(),
                "nor does asking for one nobody is carrying");

        helper.assertTrue(Foci.fit(player, wand, Foci.name(new ItemStack(ModItems.FOCI.get("frost").get())))
                .is(ModItems.FOCI.get("frost").get()), "and the other one still swaps in");
        helper.assertTrue(player.getInventory().countItem(ModItems.FOCI.get("shock").get()) == 1,
                "with the first one back in the bag");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theCycleGoesRoundInOrderAndLosesNothing(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = WandItem.of(ModWandParts.WOOD, ModWandParts.IRON);
        // inserted in reverse to prove the order comes from names, not slots
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

    @GameTest(template = TEMPLATE)
    public static void wornGearTakesSomethingOffThePrice(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertValueEqual(VisDiscount.worn(player), 0, "nothing worn, nothing off");

        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ROBES.get("cloth_chest").get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.ROBES.get("cloth_legs").get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.ROBES.get("cloth_boots").get()));
        helper.assertValueEqual(VisDiscount.worn(player), 5, "a cloth robe is five parts in a hundred");

        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.GOGGLES.get()));
        helper.assertValueEqual(VisDiscount.worn(player), 10, "and the goggles another five");

        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.ROBES.get("void_robe_helm").get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ROBES.get("void_robe_chest").get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.ROBES.get("void_robe_legs").get()));
        helper.assertValueEqual(VisDiscount.worn(player), 16, "a void robe is five a piece, over the robe boots");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aWandNeverSpendsLessThanATenth(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AspectList price = AspectList.of(ModAspects.FIRE, 10);

        helper.assertValueEqual(spent(wand(ModWandParts.WOOD, ModWandParts.GOLD, 100), price, player),
                10, "bare, a gold cap pays the asking price");

        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.ROBES.get("void_robe_helm").get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ROBES.get("void_robe_chest").get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.ROBES.get("void_robe_legs").get()));
        helper.assertValueEqual(spent(wand(ModWandParts.WOOD, ModWandParts.GOLD, 100), price, player),
                9, "in a void robe it pays fifteen parts less");

        helper.assertValueEqual(Math.round(VisDiscount.rate(1.0F, player) * 100), 85, "fifteen off, as worn");
        helper.assertValueEqual(Math.round(VisDiscount.rate(0.0F, null) * 100), 10, "and never below a tenth");
        helper.succeed();
    }

    private static int spent(ItemStack wand, AspectList price, Player player) {
        WandItem item = (WandItem) wand.getItem();
        int before = item.held(wand, ModAspects.FIRE);
        item.take(wand, price, player, me.moonscenty.alchemia.item.VisHolder.Spending.WORKING);
        return before - item.held(wand, ModAspects.FIRE);
    }
    @GameTest(template = TEMPLATE)
    public static void bothRobesTakeDye(GameTestHelper helper) {
        ItemStack cloth = new ItemStack(ModItems.ROBES.get("cloth_chest").get());
        ItemStack drab = new ItemStack(ModItems.ROBES.get("void_robe_chest").get());
        helper.assertTrue(cloth.is(ItemTags.DYEABLE), "a plain robe can be dyed");
        helper.assertTrue(drab.is(ItemTags.DYEABLE), "so can a void robe");

        helper.assertValueEqual(RobeItem.dyed(cloth), RobeItem.UNDYED, "undyed, it is the violet it was woven in");
        ItemStack red = DyedItemColor.applyDyes(cloth, List.of((DyeItem) Items.RED_DYE));
        helper.assertTrue(RobeItem.dyed(red) != RobeItem.UNDYED, "dyed, it is drawn in the dye");
        helper.succeed();
    }
}
