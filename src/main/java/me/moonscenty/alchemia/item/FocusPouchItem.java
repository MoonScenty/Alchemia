package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.menu.FocusPouchMenu;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

public class FocusPouchItem extends Item implements top.theillusivec4.curios.api.type.capability.ICurioItem {
    public static final int ACROSS = 6;
    public static final int DOWN = 3;
    public static final int SIZE = ACROSS * DOWN;

    public FocusPouchItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static ItemContainerContents contents(ItemStack pouch) {
        ItemContainerContents held = pouch.get(ModDataComponents.POUCH_CONTENTS.get());
        return held == null ? ItemContainerContents.EMPTY : held;
    }

    public static ItemStack item(ItemStack pouch, int slot) {
        ItemContainerContents held = contents(pouch);
        return slot < held.getSlots() ? held.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    // no component when empty, so emptied and untouched pouches stack
    public static void setContents(ItemStack pouch, List<ItemStack> items) {
        if (items.stream().allMatch(ItemStack::isEmpty)) {
            pouch.remove(ModDataComponents.POUCH_CONTENTS.get());
        } else {
            pouch.set(ModDataComponents.POUCH_CONTENTS.get(), ItemContainerContents.fromItems(items));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);
        if (!level.isClientSide) {
            MenuProvider opening = new SimpleMenuProvider(
                    (id, inventory, opener) -> new FocusPouchMenu(id, inventory, hand),
                    pouch.getHoverName());
            player.openMenu(opening, buffer -> buffer.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(pouch, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        long inside = contents(stack).nonEmptyStream().count();
        lines.add(Component.translatable("item.alchemia.focus_pouch.holding", inside, SIZE)
                .withStyle(ChatFormatting.GRAY));
    }
}
