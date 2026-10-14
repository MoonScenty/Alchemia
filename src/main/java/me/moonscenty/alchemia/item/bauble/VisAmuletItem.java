package me.moonscenty.alchemia.item.bauble;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

/**
 * An amulet that charges what its wearer carries out of the aura where they stand: one drop at a time into the first
 * thing that will take it, looking along the hotbar, then the baubles, then the armour. The vis stone that is found
 * does it every two seconds; the amulet of vis that is made, four times a second.
 */
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
        CuriosApi.getCuriosInventory(player).ifPresent(curios -> {
            var worn = curios.getEquippedCurios();
            for (int i = 0; i < worn.getSlots(); i++) {
                carried.add(worn.getStackInSlot(i));
            }
        });
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
