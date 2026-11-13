package me.moonscenty.alchemia.item;

import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.block.MirrorBlock;
import me.moonscenty.alchemia.block.entity.MirrorBlockEntity;
import me.moonscenty.alchemia.menu.HandMirrorMenu;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class HandMirrorItem extends Item {
    public HandMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof MirrorBlock mirror)
                || mirror.essentia()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && context.getPlayer() != null) {
            context.getItemInHand().set(ModDataComponents.MIRROR_LINK.get(),
                    GlobalPos.of(level.dimension(), context.getClickedPos()));
            level.playSound(null, context.getClickedPos(), ModSounds.JAR.get(), SoundSource.BLOCKS, 1.0F, 2.0F);
            context.getPlayer().displayClientMessage(Component.translatable("item.alchemia.hand_mirror.linked")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.get(ModDataComponents.MIRROR_LINK.get()) == null) {
            return InteractionResultHolder.pass(held);
        }
        if (!level.isClientSide) {
            if (target(level, held) == null) {
                broken(level, player, held);
            } else {
                int slot = player.getInventory().selected;
                player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new HandMirrorMenu(id, inventory,
                        slot), held.getHoverName()), buffer -> buffer.writeVarInt(slot));
            }
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Nullable
    public static MirrorBlockEntity target(Level level, ItemStack mirror) {
        GlobalPos link = mirror.get(ModDataComponents.MIRROR_LINK.get());
        if (link == null || !(level instanceof ServerLevel served)) {
            return null;
        }
        ServerLevel there = served.getServer().getLevel(link.dimension());
        if (there == null || !there.isLoaded(link.pos())
                || !(there.getBlockEntity(link.pos()) instanceof MirrorBlockEntity found) || found.essentia()) {
            return null;
        }
        return found;
    }

    public static void broken(Level level, Player player, ItemStack mirror) {
        mirror.remove(ModDataComponents.MIRROR_LINK.get());
        level.playSound(null, player.blockPosition(), ModSounds.ZAP.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
        player.displayClientMessage(Component.translatable("item.alchemia.hand_mirror.error")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.get(ModDataComponents.MIRROR_LINK.get()) != null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        GlobalPos link = stack.get(ModDataComponents.MIRROR_LINK.get());
        if (link != null) {
            lines.add(Component.translatable("block.alchemia.mirror.linked_to", link.pos().getX(), link.pos().getY(),
                    link.pos().getZ(), where(link.dimension())).withStyle(ChatFormatting.GRAY));
        }
    }

    public static Component where(net.minecraft.resources.ResourceKey<Level> dimension) {
        net.minecraft.resources.ResourceLocation id = dimension.location();
        return Component.translatableWithFallback("dimension." + id.getNamespace() + "." + id.getPath(), id.toString());
    }
}
