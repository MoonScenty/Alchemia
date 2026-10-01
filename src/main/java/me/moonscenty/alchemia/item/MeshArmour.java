package me.moonscenty.alchemia.item;

import net.minecraft.world.item.ItemStack;

/**
 * A piece of armour that is worn as a carved mesh rather than as a sheet stretched over the body.
 *
 * <p>Vanilla draws armour by pulling two flat pictures over a copy of the wearer, and that will not show a mesh.
 * So a piece like this carries no sheet on its material at all -- the layer that draws armour then has nothing to
 * draw -- and a layer of ours hangs the meshes off the limbs instead.
 *
 * <p>All it has to say for itself is which set of meshes it belongs to and which sheet of that set it should be
 * drawn off today. A robe answers that differently depending on whether anybody has dyed it; a suit of plate
 * answers the same thing every time.
 */
public interface MeshArmour {
    /** The set the meshes belong to, as the folder they were put in: {@code robe}, {@code fortress}. */
    String meshSet();

    /** Which sheet of that set to draw off. The empty name is the set as it was painted. */
    default String meshVariant(ItemStack stack) {
        return "";
    }

    /** What colour to draw it in. White leaves the sheet as it was painted, which is what most pieces want. */
    default int meshTint(ItemStack stack) {
        return 0xFFFFFF;
    }
}
