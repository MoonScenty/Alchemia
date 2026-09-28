package me.moonscenty.alchemia.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * Coal that has been round again: boiled in a crucible until the fire in it is four times what it was.
 * <p>
 * It burns for as long as four coals in anything that takes fuel, which is most of what it is for. In an essentia
 * smelter it does one thing more -- the work goes a fifth quicker while it lasts -- because a smelter is slow by
 * design and this is the one thing a player can spend to hurry it.
 */
public class AlumentumItem extends Item {
    /** Four coals' worth. Coal is sixteen hundred ticks. */
    public static final int BURNS_FOR = 6400;
    /** What a smelter's work is multiplied by while one of these is in the fire, in fifths. */
    public static final int HURRIES_BY = 4;

    public AlumentumItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipe) {
        return BURNS_FOR;
    }
}
