package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.common.SimpleTier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * What our two metals are worth as a tool.
 * <p>
 * The numbers are the original's and are not rebalanced. Alchemium is the steady one -- it cuts about as fast as
 * diamond, lasts twice what iron does, and takes enchantments better than anything vanilla has. Void is the other
 * sort of good: it cuts faster and hits harder than either, and it wears out in a hundred and fifty swings. The
 * point of the pair is that neither is simply the better one.
 */
public final class ModTiers {
    /** Steady. Reaches what diamond reaches, and is the easier of the two to keep. */
    public static final Tier ALCHEMIUM = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 7.0F, 2.5F, 22,
            () -> Ingredient.of(ModItems.ALCHEMIUM_INGOT));

    /** Sharp and brittle. Reaches what netherite reaches and wears out faster than iron. */
    public static final Tier VOID = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 150, 8.0F, 3.0F, 10,
            () -> Ingredient.of(ModItems.VOID_INGOT));

    /**
     * Alchemium with a primal bound into it. It reaches what diamond reaches, cuts faster, and lasts three times
     * as long as the alchemium it was made from. It is still mended with alchemium.
     */
    public static final Tier ELEMENTAL = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 9.0F, 3.0F, 18,
            () -> Ingredient.of(ModItems.ALCHEMIUM_INGOT));

    /**
     * The primal crusher's. Past diamond, as void metal is, and short-lived like it -- though it mends itself, so
     * the five hundred is a matter of how hard it is worked in a sitting rather than how long it lasts.
     */
    public static final Tier PRIMAL = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 500, 8.0F, 4.0F, 20,
            () -> Ingredient.of(ModItems.VOID_INGOT));

    private ModTiers() {
    }
}
