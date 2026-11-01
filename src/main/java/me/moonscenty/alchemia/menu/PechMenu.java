package me.moonscenty.alchemia.menu;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.entity.Pech;
import me.moonscenty.alchemia.entity.PechTrades;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class PechMenu extends AlchemiaMenu {
    public static final int ROLL = 0;
    public static final int OFFER = 0;
    public static final int GIVEN = 4;
    private static final double REACH = 8.0;

    private static final int OFFER_X = 36;
    private static final int OFFER_Y = 29;
    private static final int GIVEN_X = 106;
    private static final int GIVEN_Y = 20;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;
    private static final int PITCH = 18;

    @Nullable
    private final Pech pech;
    private final Player player;
    private final SimpleContainer trade = new SimpleContainer(1 + GIVEN);

    public PechMenu(int id, Inventory inventory, RegistryFriendlyByteBuf extra) {
        this(id, inventory, inventory.player.level().getEntity(extra.readVarInt()) instanceof Pech found
                ? found : null);
    }

    public PechMenu(int id, Inventory inventory, @Nullable Pech pech) {
        super(ModMenus.PECH.get(), id);
        this.pech = pech;
        this.player = inventory.player;
        if (pech != null) {
            pech.trading = true;
        }
        trade.addListener(container -> slotsChanged(container));

        addSlot(new Slot(trade, OFFER, OFFER_X, OFFER_Y));
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                addSlot(new Slot(trade, 1 + column + row * 2, GIVEN_X + PITCH * column, GIVEN_Y + PITCH * row) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        addPlayerInventory(inventory, PACK_X, PACK_Y);
    }

    @Nullable
    public Pech pech() {
        return pech;
    }

    public boolean canRoll() {
        if (!PechTrades.valued(trade.getItem(OFFER))) {
            return false;
        }
        for (int slot = 1; slot <= GIVEN; slot++) {
            if (!trade.getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clickMenuButton(Player who, int which) {
        if (which != ROLL) {
            return false;
        }
        roll();
        return true;
    }

    private void roll() {
        if (pech == null || !(player.level() instanceof ServerLevel level) || !canRoll()) {
            return;
        }
        RandomSource random = level.random;
        ItemStack offered = trade.getItem(OFFER);
        int value = PechTrades.value(offered);
        if (random.nextInt(100) <= value / 2) {
            pech.tireOfTrading();
        }
        if (random.nextInt(5) == 0) {
            value += random.nextInt(3);
        } else if (random.nextBoolean()) {
            value -= random.nextInt(3);
        }
        while (value > 0) {
            int lot = Math.min(5, Math.max((value + 1) / 2, random.nextInt(value) + 1));
            value -= lot;
            ItemStack given = ItemStack.EMPTY;
            if (lot == 1 && random.nextBoolean()) {
                given = pech.fromPack();
            } else if (lot >= 4 && random.nextBoolean()) {
                given = fromDungeon(level, random);
            }
            if (given.isEmpty()) {
                given = PechTrades.lot(level, pech.getKind(), lot, random);
            }
            give(given);
        }
        offered.shrink(1);
        trade.setChanged();
    }

    private void give(ItemStack given) {
        for (int slot = 1; slot <= GIVEN && !given.isEmpty(); slot++) {
            ItemStack there = trade.getItem(slot);
            if (!there.isEmpty() && ItemStack.isSameItemSameComponents(there, given)) {
                int moved = Math.min(given.getCount(), there.getMaxStackSize() - there.getCount());
                there.grow(moved);
                given.shrink(moved);
            }
        }
        for (int slot = 1; slot <= GIVEN && !given.isEmpty(); slot++) {
            if (trade.getItem(slot).isEmpty()) {
                trade.setItem(slot, given.copy());
                given.setCount(0);
            }
        }
    }

    private ItemStack fromDungeon(ServerLevel level, RandomSource random) {
        var table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        var rolled = table.getRandomItems(new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.CHEST));
        return rolled.isEmpty() ? ItemStack.EMPTY : rolled.get(random.nextInt(rolled.size()));
    }

    @Override
    public boolean stillValid(Player who) {
        return pech != null && pech.isAlive() && pech.isTamed() && who.distanceToSqr(pech) <= REACH * REACH;
    }

    @Override
    public void removed(Player who) {
        super.removed(who);
        if (pech != null) {
            pech.trading = false;
        }
        if (who.level().isClientSide) {
            return;
        }
        for (int slot = 0; slot <= GIVEN; slot++) {
            ItemStack left = trade.removeItemNoUpdate(slot);
            if (!left.isEmpty()) {
                ItemEntity dropped = who.drop(left, false);
                if (dropped != null) {
                    dropped.addTag(Pech.HANDED_BACK);
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player who, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack held = slot.getItem();
        ItemStack copy = held.copy();
        int traded = 1 + GIVEN;
        if (index < traded) {
            if (!moveItemStackTo(held, traded, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(held, OFFER, OFFER + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (held.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
