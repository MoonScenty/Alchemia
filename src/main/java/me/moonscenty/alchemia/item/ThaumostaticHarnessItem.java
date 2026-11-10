package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModArmorMaterials;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public class ThaumostaticHarnessItem extends ArmorItem implements VisDiscount, Rechargeable {
    public static final int FUEL_MAX = 6000;
    public static final int PER_CHARGE = 20;

    public ThaumostaticHarnessItem(Properties properties) {
        super(ModArmorMaterials.HARNESS, Type.CHESTPLATE, properties.durability(400).rarity(Rarity.RARE));
    }

    public static int fuel(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.HARNESS_FUEL.get(), 0);
    }

    public static void setFuel(ItemStack stack, int fuel) {
        stack.set(ModDataComponents.HARNESS_FUEL.get(), Math.max(0, Math.min(FUEL_MAX, fuel)));
    }

    public static boolean hovering(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.HOVERING.get(), false);
    }

    public static boolean recharge(ItemStack stack, ServerLevel level, BlockPos at, int amount) {
        if (AuraHandler.shouldSpare(level, at, ModAspects.AIR) && AuraHandler.shouldSpare(level, at, ModAspects.FIRE)) {
            return false;
        }
        int fuel = fuel(stack);
        boolean took = false;
        for (int i = 0; i < amount && fuel < FUEL_MAX; i++) {
            if (!AuraHandler.drain(level, at, AspectList.of(ModAspects.AIR, 1).add(ModAspects.FIRE, 1))) {
                break;
            }
            fuel += PER_CHARGE;
            took = true;
        }
        if (took) {
            setFuel(stack, fuel);
        }
        return took;
    }

    @Override
    public boolean charge(ItemStack stack, ServerLevel level, BlockPos at, int amount) {
        return recharge(stack, level, at, amount);
    }

    @Override
    public float chargeLevel(ItemStack stack) {
        return fuel(stack) / (float) FUEL_MAX;
    }

    @Override
    public int visDiscount(ItemStack stack, Player wearer) {
        return 1;
    }

    @Override
    public int visDiscount(ItemStack stack, Player wearer, Holder<Aspect> aspect) {
        return aspect.value() == ModAspects.AIR.value() ? 2 : 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * fuel(stack) / FUEL_MAX);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xC8A0FF;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.alchemia.thaumostatic_harness.fuel",
                fuel(stack) / PER_CHARGE).withStyle(ChatFormatting.YELLOW));
        lines.add(discountLine(stack));
    }
}
