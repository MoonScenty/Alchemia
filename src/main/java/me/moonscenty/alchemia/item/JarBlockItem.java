package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class JarBlockItem extends BlockItem {
    public JarBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        AspectList inside = stack.get(ModDataComponents.CONTENTS.get());
        if (inside != null && !inside.isEmpty()) {
            Holder<Aspect> holding = inside.sortedByAmount().getFirst();
            lines.add(Component.translatable("block.alchemia.jar.holding", holding.value().displayName(),
                    inside.get(holding), JarBlockEntity.CAPACITY).withStyle(ChatFormatting.GRAY));
        }
        Holder<Aspect> label = stack.get(ModDataComponents.ESSENTIA.get());
        if (label != null) {
            lines.add(Component.translatable("item.alchemia.jar.labelled", label.value().displayName())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
