package me.moonscenty.alchemia.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MeatChunkItem extends Item {
    public static final int EATING = 10;

    public MeatChunkItem() {
        super(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build()));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return EATING;
    }
}
