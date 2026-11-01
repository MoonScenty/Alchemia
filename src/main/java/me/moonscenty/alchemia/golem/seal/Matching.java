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

public final class Matching {
    private Matching() {
    }

    public record Rule(boolean damage, boolean components, boolean tags, boolean mod) {
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

    // entry is -1 when the stack passed a blacklist
    public record Match(ItemStack stack, int entry) {
        public static final Match NONE = new Match(ItemStack.EMPTY, -1);

        public boolean found() {
            return !stack.isEmpty();
        }
    }

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

    @Nullable
    public static IItemHandler container(Level level, BlockPos pos, Direction face) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, face);
    }

    // leaveOne: only take from a slot holding at least two
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

    // counts at most the wanted amount per slot, as the original's
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

    public static ItemStack insert(IItemHandler container, ItemStack stack) {
        return ItemHandlerHelper.insertItemStacked(container, stack, false);
    }

    public static boolean hasRoomFor(IItemHandler container, ItemStack stack) {
        return ItemHandlerHelper.insertItemStacked(container, stack.copy(), true).getCount() < stack.getCount();
    }

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
