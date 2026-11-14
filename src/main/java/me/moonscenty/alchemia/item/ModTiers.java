package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.common.SimpleTier;
import net.minecraft.world.item.crafting.Ingredient;

// original's numbers, not rebalanced
public final class ModTiers {
    public static final Tier THAUMIUM = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 7.0F, 2.5F, 22,
            () -> Ingredient.of(ModItems.THAUMIUM_INGOT));

    public static final Tier VOID = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 150, 8.0F, 3.0F, 10,
            () -> Ingredient.of(ModItems.VOID_INGOT));

    public static final Tier ELEMENTAL = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 9.0F, 3.0F, 18,
            () -> Ingredient.of(ModItems.THAUMIUM_INGOT));

    public static final Tier PRIMAL = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 500, 8.0F, 4.0F, 20,
            () -> Ingredient.of(ModItems.VOID_INGOT));

    public static final Tier CRIMSON = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 200, 8.0F, 3.5F, 20,
            () -> Ingredient.of(ModItems.VOID_INGOT));

    private ModTiers() {
    }
}
