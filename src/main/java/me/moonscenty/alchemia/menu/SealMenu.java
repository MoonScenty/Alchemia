package me.moonscenty.alchemia.menu;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.client.ClientSeals;
import me.moonscenty.alchemia.golem.seal.Seal;
import me.moonscenty.alchemia.golem.seal.SealEntity;
import me.moonscenty.alchemia.golem.seal.SealPos;
import me.moonscenty.alchemia.golem.seal.Seals;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A seal's panel: whichever of its pages is open, its filter while that page is, and the player's pack below.
 *
 * <p>Every change is made to the seal on the server, which then tells every player of it. The filter's squares are
 * ghosts: what is put in them stays in the player's hand. Where a seal counts what it wants, a left click with the
 * same thing in hand adds that many, with nothing in hand adds one, and a right click takes them off again.
 */
public class SealMenu extends AbstractContainerMenu {
    // the buttons, numbered as the original numbered them
    public static final int BLACKLIST = 20;
    public static final int WHITELIST = 21;
    public static final int LOCK = 25;
    public static final int UNLOCK = 26;
    public static final int REDSTONE_ON = 27;
    public static final int REDSTONE_OFF = 28;
    public static final int TOGGLE_ON = 30;
    public static final int TOGGLE_OFF = 60;
    public static final int PRIORITY_DOWN = 80;
    public static final int PRIORITY_UP = 81;
    public static final int COLOUR_DOWN = 82;
    public static final int COLOUR_UP = 83;
    /** Up and down by twos from here: height, width, depth. */
    public static final int AREA = 90;

    public static final int GHOSTS = 9;
    /** Where the middle of the round part of the panel is. */
    public static final int MIDDLE_X = 88;
    public static final int MIDDLE_Y = 72;

    private final SealPos pos;
    private final Supplier<SealEntity> seal;
    @Nullable
    private final ServerLevel level;
    private int category;

    /** What the client builds, from where the seal is. */
    public SealMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, SealPos.STREAM_CODEC.decode(buffer), null);
    }

    public SealMenu(int id, Inventory inventory, SealEntity seal, ServerLevel level) {
        this(id, inventory, seal.pos(), level);
    }

    private SealMenu(int id, Inventory inventory, SealPos pos, @Nullable ServerLevel level) {
        super(ModMenus.SEAL.get(), id);
        this.pos = pos;
        this.level = level;
        this.seal = level == null ? () -> ClientSeals.get(pos) : () -> Seals.get(level, pos);
        SealEntity now = seal.get();
        int size = now == null ? 0 : now.seal().filterSize();
        this.category = now == null ? Seal.PRIORITY : now.seal().categories()[0];

        Container ghosts = new SimpleContainer(GHOSTS) {
            @Override
            public ItemStack getItem(int slot) {
                SealEntity there = seal.get();
                return there == null || slot >= there.seal().filterSize() ? ItemStack.EMPTY
                        : there.seal().filter().get(slot);
            }
        };
        int across = 16 + (size - 1) % 3 * 12;
        int down = 16 + (size - 1) / 3 * 12;
        for (int at = 0; at < GHOSTS; at++) {
            int x = MIDDLE_X + at % 3 * 24 - across + 8;
            int y = MIDDLE_Y + at / 3 * 24 - down + 8;
            int which = at;
            addSlot(new Slot(ghosts, at, x, y) {
                @Override
                public boolean isActive() {
                    return category == Seal.FILTER && which < size;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 150 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 208));
        }
    }

    public SealPos pos() {
        return pos;
    }

    @Nullable
    public SealEntity seal() {
        return seal.get();
    }

    public int category() {
        return category;
    }

    /** The screen has turned to another page, which it also tells the server. */
    public void setCategory(int category) {
        this.category = category;
    }

    private boolean owns(Player player, SealEntity there) {
        return Objects.equals(player.getUUID(), there.owner());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        SealEntity there = seal.get();
        if (there == null || level == null) {
            return false;
        }
        Seal kind = there.seal();
        int[] categories = kind.categories();
        boolean done = true;
        if (id >= 0 && id < categories.length) {
            category = categories[id];
            return true;
        } else if (category == Seal.OPTIONS && id >= TOGGLE_ON && id < TOGGLE_ON + kind.toggles().size()) {
            kind.toggles().get(id - TOGGLE_ON).set(true);
        } else if (category == Seal.OPTIONS && id >= TOGGLE_OFF && id < TOGGLE_OFF + kind.toggles().size()) {
            kind.toggles().get(id - TOGGLE_OFF).set(false);
        } else if (category == Seal.PRIORITY && (id == LOCK || id == UNLOCK) && owns(player, there)) {
            there.setLocked(id == LOCK);
        } else if ((id == REDSTONE_ON || id == REDSTONE_OFF) && owns(player, there)) {
            there.setRedstone(id == REDSTONE_ON);
        } else if (category == Seal.FILTER && (id == BLACKLIST || id == WHITELIST)) {
            kind.setBlacklist(id == BLACKLIST);
        } else if (id == PRIORITY_DOWN || id == PRIORITY_UP) {
            there.setPriority(there.priority() + (id == PRIORITY_UP ? 1 : -1));
        } else if (id == COLOUR_DOWN || id == COLOUR_UP) {
            there.setColour(there.colour() + (id == COLOUR_UP ? 1 : -1));
        } else if (kind.usesArea() && id >= AREA && id < AREA + 6) {
            BlockPos area = there.area();
            int step = (id - AREA) % 2 == 0 ? -1 : 1;
            there.setArea(switch ((id - AREA) / 2) {
                case 0 -> area.offset(0, step, 0);
                case 1 -> area.offset(step, 0, 0);
                default -> area.offset(0, 0, step);
            });
        } else {
            done = false;
        }
        if (done) {
            Seals.changed(level, there);
        }
        return done;
    }

    /**
     * The filter's squares take a picture of what is in hand rather than the thing itself, as the original's did;
     * everything else is clicked as usual.
     */
    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (slotId < 0 || slotId >= GHOSTS) {
            super.clicked(slotId, button, type, player);
            return;
        }
        SealEntity there = seal.get();
        if (level == null || there == null || category != Seal.FILTER || slotId >= there.seal().filterSize()) {
            return;
        }
        Seal kind = there.seal();
        ItemStack held = getCarried();
        ItemStack entry = kind.filter().get(slotId);
        boolean counts = kind.hasStacksizeLimiters();
        int limit = kind.limit(slotId);
        if (button == 1) {
            if (!counts) {
                kind.setFilter(slotId, ItemStack.EMPTY);
            } else if (held.isEmpty()) {
                if (type == ClickType.QUICK_MOVE) {
                    kind.setFilter(slotId, ItemStack.EMPTY);
                } else if (!entry.isEmpty()) {
                    if (limit - 1 < 0) {
                        kind.setFilter(slotId, ItemStack.EMPTY);
                    } else {
                        kind.setLimit(slotId, limit - 1);
                    }
                }
            } else if (!entry.isEmpty() && limit == 0) {
                kind.setFilter(slotId, ItemStack.EMPTY);
            } else if (!entry.isEmpty() && ItemStack.isSameItemSameComponents(held, entry)) {
                kind.setLimit(slotId, limit - held.getCount());
            }
        } else if (held.isEmpty()) {
            if (counts && !entry.isEmpty()) {
                kind.setLimit(slotId, limit + 1);
            }
        } else if (!counts) {
            kind.setFilter(slotId, held.copyWithCount(1));
        } else if (!entry.isEmpty() && ItemStack.isSameItemSameComponents(held, entry)) {
            kind.setLimit(slotId, limit + held.getCount());
        } else {
            kind.setFilter(slotId, held);
            kind.setLimit(slotId, 0);
        }
        Seals.changed(level, there);
        broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return seal.get() != null;
    }
}
