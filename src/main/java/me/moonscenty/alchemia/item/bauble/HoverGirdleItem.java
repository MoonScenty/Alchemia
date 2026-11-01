package me.moonscenty.alchemia.item.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class HoverGirdleItem extends BaubleItem {
    private static final float LIGHTER = 0.33F;

    public HoverGirdleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slot, ItemStack stack) {
        LivingEntity wearer = slot.entity();
        if (wearer.fallDistance > 0) {
            wearer.fallDistance = Math.max(0.0F, wearer.fallDistance - LIGHTER);
        }
    }
}
