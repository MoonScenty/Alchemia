package me.moonscenty.alchemia.client.armour;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;

/**
 * The eight meshes a robe is made of, and which piece of a robe wears which of them.
 *
 * <p>They are bought meshes, carved rather than boxed, and they are loaded as OBJ models rather than built out of
 * cubes. A model nothing refers to is never baked, so each one has to be asked for by name before the game will
 * read it; {@link me.moonscenty.alchemia.client.AlchemiaClientSetup} does that asking.
 *
 * <p>The left and right in the drawn file names are the wearer's as the drawing saw them, which is the mirror of
 * what this game calls left and right. So {@code left_arm} is hung off the right arm, and so on for the rest. The
 * names are left as they were drawn rather than quietly renamed, because the file on disk is the one the artist
 * will open again.
 */
public final class RobeMeshes {
    /** Every mesh, by the name it was drawn under. */
    public static final List<String> PARTS =
            List.of("head", "body", "left_arm", "right_arm", "left_leg", "right_leg", "left_feet", "right_feet");

    private RobeMeshes() {
    }

    /** Where a mesh's model lives, for one robe or the other. */
    public static ModelResourceLocation model(String part, boolean drab) {
        ResourceLocation path = Alchemia.id("entity/robe/" + (drab ? "void/" : "") + part);
        return ModelResourceLocation.standalone(path);
    }

    /** The meshes a piece of a robe covers, drawn-file names. */
    public static List<String> covering(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> List.of("head");
            case CHESTPLATE -> List.of("body", "left_arm", "right_arm");
            case LEGGINGS -> List.of("left_leg", "right_leg");
            case BOOTS -> List.of("left_feet", "right_feet");
            default -> List.of();
        };
    }

    public static BakedModel baked(String part, boolean drab) {
        return Minecraft.getInstance().getModelManager().getModel(model(part, drab));
    }
}
