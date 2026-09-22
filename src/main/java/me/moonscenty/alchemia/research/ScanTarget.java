package me.moonscenty.alchemia.research;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.aura.node.AuraNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Something a player can hold an alchemometer up to.
 */
public sealed interface ScanTarget {
    /** What the thing is made of, which is what a scan actually teaches. */
    AspectList aspects();

    /** What to call it in the message that follows a scan. */
    Component displayName();

    record OfItem(ItemStack stack) implements ScanTarget {
        @Override
        public AspectList aspects() {
            return Aspects.of(stack);
        }

        @Override
        public Component displayName() {
            return stack.getHoverName();
        }
    }

    record OfBlock(Level level, BlockPos pos, BlockState state) implements ScanTarget {
        @Override
        public AspectList aspects() {
            // a block is read through the item it would drop, which is what the aspects are written against
            return Aspects.of(state.getBlock().asItem());
        }

        @Override
        public Component displayName() {
            return state.getBlock().getName();
        }
    }

    record OfEntity(Entity entity) implements ScanTarget {
        @Override
        public AspectList aspects() {
            return Aspects.of(entity.getType());
        }

        @Override
        public Component displayName() {
            return entity.getDisplayName();
        }
    }

    /**
     * A node, which is nothing but the aspect it is made of.
     * <p>
     * There is no entry for it in the data map because no two nodes hold the same thing. What it teaches is read
     * off the node in front of the player, and how much of it there is says how large the node has grown.
     */
    record OfNode(AuraNode node) implements ScanTarget {
        @Override
        public AspectList aspects() {
            Holder<Aspect> aspect = node.aspect();
            return aspect == null ? AspectList.EMPTY : AspectList.EMPTY.add(aspect, node.getSize());
        }

        @Override
        public Component displayName() {
            return node.type().displayName();
        }
    }

    /** Reads whatever is in front of the player, preferring a dropped item's contents over the item entity itself. */
    static ScanTarget of(Entity entity) {
        if (entity instanceof ItemEntity item) {
            return new OfItem(item.getItem());
        }
        return entity instanceof AuraNode node ? new OfNode(node) : new OfEntity(entity);
    }
}
