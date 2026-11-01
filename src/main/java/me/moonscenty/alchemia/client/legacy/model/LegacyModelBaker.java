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

public final class LegacyModelBaker {
    private static final List<String> HUMANOID = List.of("head", "hat", "body", "right_arm", "left_arm",
            "right_leg", "left_leg");

    private LegacyModelBaker() {
    }

    public record Baked(ModelPart root, Map<String, ModelPart> parts) {
        public ModelPart part(String name) {
            ModelPart part = parts.get(name);
            if (part == null) {
                throw new IllegalArgumentException("No part " + name);
            }
            return part;
        }
    }

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

    public static ModelPart[] fields(LegacyModel model, String... names) {
        ModelPart[] parts = new ModelPart[names.length];
        for (int i = 0; i < names.length; i++) {
            parts[i] = field(model, names[i]);
        }
        return parts;
    }

    private static void add(PartDefinition parent, LegacyPart part, String name, LegacyModel model) {
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
        baked.visible = !part.hidden();
        for (LegacyPart child : part.children()) {
            ModelPart found = baked.getChild(child.name());
            into.putIfAbsent(child.name(), found);
            collect(found, child, into);
        }
    }
}
