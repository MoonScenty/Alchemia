package me.moonscenty.alchemia.golem.seal;

import java.util.Arrays;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * What a seal does: how it finds work for golems, and what a golem does when it gets there.
 *
 * <p>Every seal keeps its own filter, if it has one, and its own options. Which of the pages of a seal's panel it
 * shows, and in what order, is its own business too: priority and locking, filter, area, options, and what golems
 * it wants or will not have.
 */
public abstract class Seal {
    public static final int PRIORITY = 0;
    public static final int FILTER = 1;
    public static final int AREA = 2;
    public static final int OPTIONS = 3;
    public static final int TRAITS = 4;

    private final String key;
    private final NonNullList<ItemStack> filter;
    /** How many of each whitelisted thing is wanted, nought for no limit, where the seal counts at all. */
    private final int[] limits;
    private boolean blacklist = true;
    private final List<Toggle> toggles;

    /** One of a seal's options, on or off. */
    public static final class Toggle {
        private final String key;
        private boolean value;

        public Toggle(String key, boolean value) {
            this.key = key;
            this.value = value;
        }

        public String key() {
            return key;
        }

        public String translationKey() {
            return "golem." + Alchemia.MODID + ".prop." + key;
        }

        public boolean value() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;
        }
    }

    protected Seal(String key, int filterSize, Toggle... toggles) {
        this.key = key;
        this.filter = NonNullList.withSize(filterSize, ItemStack.EMPTY);
        this.limits = new int[filterSize];
        this.toggles = List.of(toggles);
    }

    /** Its name among seals, which is also its item's. */
    public String key() {
        return key;
    }

    /** The picture it is drawn with where it is stuck, which is its item's. */
    public ResourceLocation icon() {
        return Alchemia.id("item/legacy/seal_" + key);
    }

    /** Whether it can stay stuck here. Checked once a second; one that cannot falls off. */
    public abstract boolean canPlaceAt(Level level, BlockPos pos, Direction face);

    /** Every tick it is in a loaded place and not stopped by redstone. */
    public abstract void tick(ServerLevel level, SealEntity seal);

    /** A golem has taken up one of its tasks. */
    public void onTaskStarted(ServerLevel level, Golem golem, Task task) {
    }

    /** A golem has got to one of its tasks: do it, and say whether it was done. */
    public abstract boolean onTaskCompletion(ServerLevel level, Golem golem, Task task);

    /** One of its tasks has been let go of. */
    public void onTaskSuspension(ServerLevel level, Task task) {
    }

    /** Whether this golem can do this task just now. */
    public abstract boolean canGolemPerform(Golem golem, Task task);

    /** It has been taken off. */
    public void onRemoval(ServerLevel level, BlockPos pos, Direction face) {
    }

    /** Which pages its panel has, in order. */
    public abstract int[] categories();

    /** Golems without all of these will not take its tasks. */
    public List<GolemTrait> required() {
        return List.of();
    }

    /** Golems with any of these will not take its tasks. */
    public List<GolemTrait> forbidden() {
        return List.of();
    }

    /** Whether it works over an area in front of it, rather than on the block it is on. */
    public boolean usesArea() {
        return false;
    }

    /** Whether a whitelisted filter entry also says how many of it, as filling and the like do. */
    public boolean hasStacksizeLimiters() {
        return false;
    }

    public boolean hasCategory(int category) {
        return Arrays.stream(categories()).anyMatch(each -> each == category);
    }

    // --- the filter -----------------------------------------------------------------------------------------

    public int filterSize() {
        return filter.size();
    }

    public List<ItemStack> filter() {
        return filter;
    }

    /** Sets a filter entry, one of a thing, and its limit to how many were put there. */
    public void setFilter(int slot, ItemStack stack) {
        filter.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        limits[slot] = stack.isEmpty() ? 0 : stack.getCount();
    }

    public int limit(int slot) {
        return limits[slot];
    }

    public void setLimit(int slot, int limit) {
        limits[slot] = Math.max(0, limit);
    }

    public boolean blacklist() {
        return blacklist;
    }

    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
    }

    // --- options --------------------------------------------------------------------------------------------

    public List<Toggle> toggles() {
        return toggles;
    }

    protected boolean toggle(int which) {
        return toggles.get(which).value();
    }

    /** How strictly it tells things apart: its first four options, where it has them. */
    public Matching.Rule rule() {
        return toggles.size() < 4 ? new Matching.Rule(true, true, false, false)
                : new Matching.Rule(toggle(0), toggle(1), toggle(2), toggle(3));
    }

    // --- saving ---------------------------------------------------------------------------------------------

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag items = new ListTag();
        for (int slot = 0; slot < filter.size(); slot++) {
            if (!filter.get(slot).isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("slot", (byte) slot);
                entry.put("item", filter.get(slot).save(registries));
                entry.putInt("limit", limits[slot]);
                items.add(entry);
            }
        }
        tag.put("filter", items);
        tag.putBoolean("blacklist", blacklist);
        for (Toggle toggle : toggles) {
            tag.putBoolean(toggle.key(), toggle.value());
        }
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        for (int slot = 0; slot < filter.size(); slot++) {
            filter.set(slot, ItemStack.EMPTY);
            limits[slot] = 0;
        }
        for (Tag each : tag.getList("filter", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) each;
            int slot = entry.getByte("slot");
            if (slot >= 0 && slot < filter.size()) {
                filter.set(slot, ItemStack.parseOptional(registries, entry.getCompound("item")).copyWithCount(1));
                limits[slot] = entry.getInt("limit");
            }
        }
        blacklist = !tag.contains("blacklist") || tag.getBoolean("blacklist");
        for (Toggle toggle : toggles) {
            if (tag.contains(toggle.key())) {
                toggle.set(tag.getBoolean(toggle.key()));
            }
        }
    }
}
