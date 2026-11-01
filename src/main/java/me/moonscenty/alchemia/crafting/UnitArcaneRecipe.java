package me.moonscenty.alchemia.crafting;

/** An arcane recipe read off the grid in code: one shared instance, nothing saved, written by a UnitSerializer. */
public abstract class UnitArcaneRecipe implements ArcaneRecipe {
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    /** Every instance of a kind is equal, since the stream codec only sends the one it was built with. */
    @Override
    public boolean equals(Object other) {
        return other != null && other.getClass() == getClass();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
