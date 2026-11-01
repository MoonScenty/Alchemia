package me.moonscenty.alchemia.item.bauble;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.item.Worn;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;

public class VisAmuletItem extends BaubleItem {
    private final int every;
    private final boolean found;

    public VisAmuletItem(Properties properties, int every, boolean found) {
        super(properties);
        this.every = every;
        this.found = found;
    }

    @Override
    public void curioTick(SlotContext slot, ItemStack stack) {
        if (!(slot.entity() instanceof Player player) || !(player.level() instanceof ServerLevel level)
                || player.tickCount % every != 0) {
            return;
        }
        List<ItemStack> carried = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            carried.add(player.getInventory().getItem(i));
        }
        carried.addAll(Worn.baubles(player));
        for (EquipmentSlot armour : new EquipmentSlot[] {EquipmentSlot.FEET, EquipmentSlot.LEGS,
                EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            carried.add(player.getItemBySlot(armour));
        }
        for (ItemStack each : carried) {
            if (Recharging.chargeable(each) && Recharging.charge(each, level, player.blockPosition(), 1)) {
                return;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (found) {
            tooltip.add(Component.translatable("item.alchemia.vis_stone.text").withStyle(ChatFormatting.AQUA));
        }
    }
}
