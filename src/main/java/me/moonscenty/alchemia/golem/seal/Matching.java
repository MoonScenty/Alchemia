package me.moonscenty.alchemia.golem.seal;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * How seals tell one thing from another and move things in and out of containers, as the original's inventory
 * helpers did.
 *
 * <p>Two things are alike if they are the same item; whether their damage and the rest of what is on them must
 * match too is up to the seal. A seal may also count as alike anything sharing a common tag with it, which is what
 * the original's ore dictionary was, or anything at all from the same mod.
 */
public final class Matching {
    private Matching() {
    }

    /** How strictly things are compared: a seal's first four toggles. */
    public record Rule(boolean damage, boolean components, boolean tags, boolean mod) {
        /** Exactly the same thing, damage, data and all. */
        public static final Rule STRICT = new Rule(true, true, false, false);

        public boolean alike(ItemStack one, ItemStack other) {
            if (one.isEmpty() || other.isEmpty()) {
                return one.isEmpty() && other.isEmpty();
            }
            if (mod) {
                return BuiltInRegistries.ITEM.getKey(one.getItem()).getNamespace()
                        .equals(BuiltInRegistries.ITEM.getKey(other.getItem()).getNamespace());
            }
            if (tags && one.getTags().anyMatch(tag -> tag.location().getNamespace().equals("c") && other.is(tag))) {
                return true;
            }
            if (!one.is(other.getItem())) {
                return false;
            }
            if (damage && one.getDamageValue() != other.getDamageValue()) {
                return false;
            }
            if (components) {
                ItemStack a = one.copyWithCount(1);
                ItemStack b = other.copyWithCount(1);
                a.remove(DataComponents.DAMAGE);
                b.remove(DataComponents.DAMAGE);
                return ItemStack.isSameItemSameComponents(a, b);
            }
            return true;
        }
    }

    /**
     * One of the things looked through that the filter lets by, and which filter entry let it, or -1 if it got by a
     * blacklist.
     */
    public record Match(ItemStack stack, int entry) {
        public static final Match NONE = new Match(ItemStack.EMPTY, -1);

        public boolean found() {
            return !stack.isEmpty();
        }
    }

    /**
     * The first of these the filter lets by. A blacklist lets by anything not on it, and an empty one everything; a
     * whitelist only what is on it, and then says which entry it matched.
     */
    public static Match firstAllowed(List<ItemStack> filter, boolean blacklist, Iterable<ItemStack> stacks,
            Rule rule) {
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            int by = -1;
            for (int at = 0; at < filter.size(); at++) {
                if (!filter.get(at).isEmpty() && rule.alike(filter.get(at), stack)) {
                    by = at;
                    break;
                }
            }
            if (blacklist ? by < 0 : by >= 0) {
                return new Match(stack, blacklist ? -1 : by);
            }
        }
        return Match.NONE;
    }

    /** The container behind this face of a block, if it is one. */
    @Nullable
    public static IItemHandler container(Level level, BlockPos pos, Direction face) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, face);
    }

    /** The first stack in a container the filter lets by; with {@code leaveOne}, only if there are two of it. */
    public static ItemStack firstAllowed(List<ItemStack> filter, boolean blacklist, IItemHandler container,
            Rule rule, boolean leaveOne) {
        for (int slot = 0; slot < container.getSlots(); slot++) {
            ItemStack there = container.getStackInSlot(slot);
            if (there.isEmpty() || leaveOne && count(container, there, rule) < 2) {
                continue;
            }
            if (firstAllowed(filter, blacklist, List.of(there), rule).found()) {
                return there;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * How much of a thing could be had out of a container, counting no more than the asked-for amount from any one
     * slot, as the original counted.
     */
    public static int count(IItemHandler container, ItemStack wanted, Rule rule) {
        int count = 0;
        for (int slot = 0; slot < container.getSlots(); slot++) {
            ItemStack there = container.getStackInSlot(slot);
            if (!there.isEmpty() && rule.alike(there, wanted)) {
                count += container.extractItem(slot, wanted.getCount(), true).getCount();
            }
        }
        return count;
    }

    /** Takes up to the asked-for amount out of the first slot holding something like it. */
    public static ItemStack extract(IItemHandler container, ItemStack wanted, Rule rule) {
        for (int slot = 0; slot < container.getSlots(); slot++) {
            ItemStack there = container.getStackInSlot(slot);
            if (!there.isEmpty() && rule.alike(there, wanted)) {
                ItemStack out = container.extractItem(slot, wanted.getCount(), false);
                if (!out.isEmpty()) {
                    return out;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /** Puts as much as will go into a container and gives back the rest. */
    public static ItemStack insert(IItemHandler container, ItemStack stack) {
        return ItemHandlerHelper.insertItemStacked(container, stack, false);
    }

    /** Whether any of it would go in. */
    public static boolean hasRoomFor(IItemHandler container, ItemStack stack) {
        return ItemHandlerHelper.insertItemStacked(container, stack.copy(), true).getCount() < stack.getCount();
    }

    /** How much of a thing is lying about loose within so far of a block's middle. */
    public static int countLoose(Level level, BlockPos pos, ItemStack wanted, double range, Rule rule) {
        int count = 0;
        AABB around = new AABB(pos).inflate(range);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, around)) {
            if (item.position().closerThan(pos.getCenter(), range) && rule.alike(wanted, item.getItem())) {
                count += item.getItem().getCount();
            }
        }
        return count;
    }
}
