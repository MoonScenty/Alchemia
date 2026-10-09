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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.phys.AABB;

/**
 * The eight meshes a set of worn armour is made of, and which piece of it wears which of them.
 *
 * <p>They are bought meshes, carved rather than boxed, and they are loaded as OBJ models rather than built out of
 * cubes. A model nothing refers to is never baked, so each one has to be asked for by name before the game will
 * read it; {@link me.moonscenty.alchemia.client.AlchemiaClientSetup} does that asking.
 *
 * <p>A set may be drawn off more than one sheet -- the robes are painted, bleached and drained -- and the meshes
 * are the same either way. Only the model file differs, and a variant is the folder its model files sit in.
 */
public final class ArmourMeshes {
    /** Every mesh, by the name it was drawn under. */
    public static final List<String> PARTS =
            List.of("head", "body", "left_arm", "right_arm", "left_leg", "right_leg", "left_feet", "right_feet");

    /** Every set, and every sheet each is drawn off, so that all of them can be asked for at load. */
    public static final Map<String, List<String>> SETS =
            Map.of("robe", List.of("", "dyed", "void"));

    private static final Map<String, AABB> MEASURED = new HashMap<>();
    private static final RandomSource STEADY = RandomSource.create();
    /** A baked face carries eight numbers a corner, and the first three of them are where that corner is. */
    private static final int PER_CORNER = 8;

    private ArmourMeshes() {
    }

    /** Where a mesh's model lives, for one set and one of its sheets. */
    public static ModelResourceLocation model(String set, String variant, String part) {
        String folder = variant.isEmpty() ? "" : variant + "/";
        return ModelResourceLocation.standalone(Alchemia.id("entity/" + set + "/" + folder + part));
    }

    /** The meshes a piece covers, by which slot it is worn in. */
    public static List<String> covering(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> List.of("head");
            case CHESTPLATE -> List.of("body", "left_arm", "right_arm");
            case LEGGINGS -> List.of("left_leg", "right_leg");
            case BOOTS -> List.of("left_feet", "right_feet");
            default -> List.of();
        };
    }

    public static BakedModel baked(String set, String variant, String part) {
        return Minecraft.getInstance().getModelManager().getModel(model(set, variant, part));
    }

    /**
     * How much room a piece takes up, in the space it was drawn in.
     * <p>
     * Read off the baked meshes rather than written down, so that a redrawn piece fits its slot without anybody
     * remembering to change a number. Worked out once for each piece and kept, since a mesh never moves.
     */
    public static AABB extent(String set, String variant, ArmorItem.Type type) {
        return MEASURED.computeIfAbsent(set + ":" + variant + ":" + type.name(),
                key -> measure(set, variant, type));
    }

    private static AABB measure(String set, String variant, ArmorItem.Type type) {
        float[] low = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] high = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        for (String part : covering(type)) {
            for (BakedQuad quad : faces(baked(set, variant, part))) {
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
