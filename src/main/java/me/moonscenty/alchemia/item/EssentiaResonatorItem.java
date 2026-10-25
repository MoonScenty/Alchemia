package me.moonscenty.alchemia.item;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Held to a vessel or a length of pipe, says what is in it and what it draws, through the side it is held to. It
 * gets in ahead of whatever the block would do with a click, as the original's did, so a tube or a jar can be
 * sounded without being worked.
 * <p>
 * The original also said how hard the thing drew. The pipes here pull from the far end rather than by suction, so
 * there is no strength to tell; it says which essentia it would take instead, which is the half that was useful.
 */
public class EssentiaResonatorItem extends Item {
    public EssentiaResonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        List<Component> report = report(level, pos, context.getClickedFace());
        if (report.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && context.getPlayer() != null) {
            report.forEach(line -> context.getPlayer().sendSystemMessage(line));
            level.playSound(null, pos, ModSounds.ALEMBIC_KNOCK.get(), SoundSource.BLOCKS, 0.5F,
                    1.9F + level.random.nextFloat() * 0.1F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * What the resonator has to say about a block through one of its sides, a line to a thing: everything it holds,
     * and then what it draws. Nothing at all if the block is neither a vessel nor a pipe.
     */
    public static List<Component> report(Level level, BlockPos pos, Direction side) {
        List<Component> lines = new ArrayList<>();
        BlockState state = level.getBlockState(pos);
        if (level.getBlockEntity(pos) instanceof EssentiaHolder holder) {
            boolean open = holder.reachableFrom(side);
            if (open) {
                for (Holder<Aspect> aspect : holder.held().sortedByName()) {
                    lines.add(Component.translatable("item.alchemia.essentia_resonator.contains",
                            holder.held().get(aspect), aspect.value().displayName())
                            .withStyle(ChatFormatting.BLUE));
                }
            }
            List<Holder<Aspect>> wanted = new ArrayList<>();
            if (open && holder.takesFrom(side)) {
                ModAspects.REGISTRY.holders().filter(holder::wants).forEach(wanted::add);
            }
            lines.add(draws(wanted));
        } else if (state.getBlock() instanceof TubeBlock tube) {
            boolean shut = level.getBlockEntity(pos) instanceof TubeBlockEntity pipe && pipe.shut(side);
            List<Holder<Aspect>> wanted = new ArrayList<>();
            if (!shut) {
                tube.insistsOn(level, pos).ifPresentOrElse(wanted::add,
                        () -> ModAspects.REGISTRY.holders().forEach(wanted::add));
            }
            lines.add(draws(wanted));
        }
        return lines;
    }

    /** The line for what something draws: any essentia, a few kinds by name, or none. */
    private static Component draws(List<Holder<Aspect>> wanted) {
        Component what;
        if (wanted.isEmpty()) {
            what = Component.translatable("item.alchemia.essentia_resonator.nothing");
        } else if (wanted.size() == ModAspects.REGISTRY.size()) {
            what = Component.translatable("item.alchemia.essentia_resonator.anything");
        } else {
            what = ComponentUtils.formatList(wanted, aspect -> aspect.value().displayName());
        }
        return Component.translatable("item.alchemia.essentia_resonator.draws", what)
                .withStyle(ChatFormatting.DARK_PURPLE);
    }
}
