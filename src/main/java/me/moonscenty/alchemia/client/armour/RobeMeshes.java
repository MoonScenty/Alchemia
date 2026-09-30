package me.moonscenty.alchemia.client.armour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.phys.AABB;

/**
 * The eight meshes a robe is made of, and which piece of a robe wears which of them.
 *
 * <p>They are bought meshes, carved rather than boxed, and they are loaded as OBJ models rather than built out of
 * cubes. A model nothing refers to is never baked, so each one has to be asked for by name before the game will
 * read it; {@link me.moonscenty.alchemia.client.AlchemiaClientSetup} does that asking.
 *
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

    /**
     * How much room a piece of a robe takes up, in the space it was drawn in.
     * <p>
     * Read off the baked meshes rather than written down, so that a redrawn robe fits its slot without anybody
     * remembering to change a number. Worked out once for each piece and kept, since a mesh never moves.
     */
    public static AABB extent(ArmorItem.Type type, boolean drab) {
        return MEASURED.computeIfAbsent(type.name() + (drab ? ":void" : ""), key -> measure(type, drab));
    }

    private static final Map<String, AABB> MEASURED = new HashMap<>();
    private static final RandomSource STEADY = RandomSource.create();
    /** A baked face carries eight numbers a corner, and the first three of them are where that corner is. */
    private static final int PER_CORNER = 8;

    private static AABB measure(ArmorItem.Type type, boolean drab) {
        float[] low = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] high = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        for (String part : covering(type)) {
            for (BakedQuad quad : faces(baked(part, drab))) {
                int[] numbers = quad.getVertices();
                for (int corner = 0; corner + PER_CORNER <= numbers.length; corner += PER_CORNER) {
                    for (int axis = 0; axis < 3; axis++) {
                        float at = Float.intBitsToFloat(numbers[corner + axis]);
                        low[axis] = Math.min(low[axis], at);
                        high[axis] = Math.max(high[axis], at);
                    }
                }
            }
        }
        return low[0] > high[0]
                ? new AABB(0, 0, 0, 1, 1, 1)
                : new AABB(low[0], low[1], low[2], high[0], high[1], high[2]);
    }

    /** Every face of a mesh, however it was sorted when it was baked. */
    public static List<BakedQuad> faces(BakedModel mesh) {
        List<BakedQuad> out = new ArrayList<>(mesh.getQuads(null, null, STEADY));
        for (Direction side : Direction.values()) {
            out.addAll(mesh.getQuads(null, side, STEADY));
        }
        return out;
    }
}
