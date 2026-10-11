package me.moonscenty.alchemia.item.tool;

import me.moonscenty.alchemia.item.WarpingGear;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

/**
 * The primal crusher: the pickaxe of the core and the shovel of the earthmover, beaten together with their
 * void-metal counterparts round something that should not hold all of them and does.
 *
 * <p>It is a pickaxe and a shovel at once, digs three by three, and refines ore as the pickaxe of the core did.
 * It has lost what the two of them built and sounded out; what is left is only for breaking things. Like void
 * metal it mends itself, a point a second. Unlike anything else in the mod it is uneasy to hold -- it carries
 * two points of warp in hand.
 */
public class PrimalCrusherItem extends DiggerItem implements WarpingGear {
    /** How much warp it carries, and how often it mends a point of its own edge. The original's numbers. */
    private static final int WARP = 2;
    private static final int MENDS_EVERY = 20;

    public PrimalCrusherItem(Tier tier, Properties properties) {
        super(tier, ModTags.Blocks.MINEABLE_PRIMAL_CRUSHER, properties);
    }

    @Override
    public int warp(ItemStack stack, Player holder) {
        return WARP;
    }

    /** Mends itself wherever it is carried, as void metal does. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        super.inventoryTick(stack, level, holder, slot, selected);
        if (!level.isClientSide && stack.isDamaged() && holder.tickCount % MENDS_EVERY == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
}
