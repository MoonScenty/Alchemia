package me.moonscenty.alchemia.item.tool;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

/**
 * The pickaxe of the core: an alchemium pickaxe with fire bound into it.
 *
 * <p>It runs at forge heat. Ore it breaks sometimes comes up already refined into a cluster, it can sound out the
 * stone it is tapped against, and whatever it strikes catches. The first two are workings it carries from the
 * start; the burning is its own.
 */
public class ElementalPickaxeItem extends PickaxeItem {
    /** How long a struck thing burns for, in seconds. The original's number. */
    private static final int BURNS = 2;

    public ElementalPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    /** Whatever is struck catches. Another player only when the world lets players hurt one another. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity struck, LivingEntity striker) {
        if (!(struck instanceof Player other) || !(striker instanceof Player self) || self.canHarmPlayer(other)) {
            struck.igniteForSeconds(BURNS);
        }
        return super.hurtEnemy(stack, struck, striker);
    }
}
