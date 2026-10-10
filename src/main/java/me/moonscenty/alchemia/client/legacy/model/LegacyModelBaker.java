package me.moonscenty.alchemia.client.legacy.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Turns a model read out of the original into one this game can draw.
 *
 * <p>Little has to change. A {@code ModelRenderer} and a {@link ModelPart} agree on nearly everything: sixteen units
 * to the block, down is positive y, a box is a corner and three sizes, its texture is folded the same way, a part
 * turns about its pivot by z then y then x, and children ride on their parent. What this does is mostly
 * transcribing one tree into the other.
 */
public final class LegacyModelBaker {
    /** The seven parts a humanoid model looks up by name and will not do without. */
    private static final List<String> HUMANOID = List.of("head", "hat", "body", "right_arm", "left_arm",
            "right_leg", "left_leg");

    private LegacyModelBaker() {
    }

    /** A baked model, and every part in it by the name it had in the original, for showing and hiding. */
    public record Baked(ModelPart root, Map<String, ModelPart> parts) {
        public ModelPart part(String name) {
            ModelPart part = parts.get(name);
            if (part == null) {
                throw new IllegalArgumentException("No part " + name);
            }
            return part;
        }
    }

    /** A biped: the seven parts at the root, each carrying what the original hung on it. */
    public static Baked humanoid(LegacyModel model) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (String name : HUMANOID) {
            LegacyPart bone = model.bones().get(name);
            if (bone == null) {
                root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
            } else {
                add(root, bone, name, model);
            }
        }
        ModelPart baked = LayerDefinition.create(mesh, model.textureWidth(), model.textureHeight()).bakeRoot();
        Map<String, ModelPart> parts = new HashMap<>();
        for (String name : HUMANOID) {
            ModelPart part = baked.getChild(name);
            LegacyPart bone = model.bones().get(name);
            parts.put(name, part);
            if (bone != null) {
                collect(part, bone, parts);
            }
        }
        return new Baked(baked, Map.copyOf(parts));
    }

    /** One part the original kept in a field, on its own, with everything hung off it. */
    public static ModelPart field(LegacyModel model, String name) {
        LegacyPart part = model.fields().get(name);
        if (part == null) {
            throw new IllegalArgumentException("No field " + name);
        }
        MeshDefinition mesh = new MeshDefinition();
        add(mesh.getRoot(), part, name, model);
        ModelPart baked = LayerDefinition.create(mesh, model.textureWidth(), model.textureHeight()).bakeRoot()
                .getChild(name);
        collect(baked, part, new HashMap<>());
        return baked;
    }

    private static void add(PartDefinition parent, LegacyPart part, String name, LegacyModel model) {
        // a part that kept its own sheet size gets its corners scaled to the model's, which is the only size a layer
        // has here
        float scaleU = (float) part.textureWidth() / model.textureWidth();
        float scaleV = (float) part.textureHeight() / model.textureHeight();
        CubeListBuilder cubes = CubeListBuilder.create();
        for (LegacyPart.Box box : part.boxes()) {
            cubes.texOffs(box.u(), box.v()).mirror(box.mirror()).addBox(box.x(), box.y(), box.z(), box.width(),
                    box.height(), box.depth(), new CubeDeformation(box.grow()), scaleU, scaleV);
        }
        PartDefinition added = parent.addOrReplaceChild(name, cubes,
                PartPose.offsetAndRotation(part.x(), part.y(), part.z(), part.xRot(), part.yRot(), part.zRot()));
        for (LegacyPart child : part.children()) {
            add(added, child, child.name(), model);
        }
    }

    private static void collect(ModelPart baked, LegacyPart part, Map<String, ModelPart> into) {
        // the original could keep a part and never draw it
        baked.visible = !part.hidden();
        for (LegacyPart child : part.children()) {
            ModelPart found = baked.getChild(child.name());
            into.putIfAbsent(child.name(), found);
            collect(found, child, into);
        }
    }
}
