package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ZombieBrainItem extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.2F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.8F)
            .build();

    public ZombieBrainItem(Properties properties) {
        super(properties.food(FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
        if (!level.isClientSide && eater instanceof Player player) {
            if (level.random.nextFloat() < 0.1F) {
                WarpHandler.add(player, WarpData.Kind.STICKY, 1);
            } else {
                WarpHandler.add(player, WarpData.Kind.TEMPORARY, 1 + level.random.nextInt(3));
            }
        }
        return super.finishUsingItem(stack, level, eater);
    }
}
