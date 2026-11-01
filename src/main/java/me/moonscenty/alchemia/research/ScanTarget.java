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

public sealed interface ScanTarget {
    AspectList aspects();

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
            // read via the block's item, which is what aspects are keyed on
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
            return Aspects.of(entity);
        }

        @Override
        public Component displayName() {
            return entity.getDisplayName();
        }
    }

    // no data-map entry: read off the node itself, the amount scaling with its size
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

    // a dropped item is read as its stack
    static ScanTarget of(Entity entity) {
        if (entity instanceof ItemEntity item) {
            return new OfItem(item.getItem());
        }
        return entity instanceof AuraNode node ? new OfNode(node) : new OfEntity(entity);
    }
}
