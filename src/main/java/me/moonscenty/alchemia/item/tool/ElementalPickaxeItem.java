package me.moonscenty.alchemia.item.tool;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

public class ElementalPickaxeItem extends PickaxeItem {
    // seconds; original's value
    private static final int BURNS = 2;

    public ElementalPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity struck, LivingEntity striker) {
        if (!(struck instanceof Player other) || !(striker instanceof Player self) || self.canHarmPlayer(other)) {
            struck.igniteForSeconds(BURNS);
        }
        return super.hurtEnemy(stack, struck, striker);
    }
}
