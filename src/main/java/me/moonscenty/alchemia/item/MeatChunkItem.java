package me.moonscenty.alchemia.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A meat chunk, the original's {@code ItemChunksEdible}: a scrap of cooked meat the infernal furnace throws out
 * besides now and then. A mouthful (one hunger, a little saturation) eaten in half a second, and with sugar three
 * different kinds make a triple meat treat.
 */
public class MeatChunkItem extends Item {
    /** How long one takes to eat, in ticks. */
    public static final int EATING = 10;

    public MeatChunkItem() {
        super(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build()));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return EATING;
    }
}
