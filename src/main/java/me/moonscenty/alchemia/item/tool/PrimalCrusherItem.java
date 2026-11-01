package me.moonscenty.alchemia.item.tool;

import me.moonscenty.alchemia.item.WarpingGear;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

public class PrimalCrusherItem extends DiggerItem implements WarpingGear {
    // original's values
    private static final int WARP = 2;
    private static final int MENDS_EVERY = 20;

    public PrimalCrusherItem(Tier tier, Properties properties) {
        super(tier, ModTags.Blocks.MINEABLE_PRIMAL_CRUSHER, properties);
    }

    @Override
    public int warp(ItemStack stack, Player holder) {
        return WARP;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        super.inventoryTick(stack, level, holder, slot, selected);
        if (!level.isClientSide && stack.isDamaged() && holder.tickCount % MENDS_EVERY == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
}
