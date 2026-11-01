package me.moonscenty.alchemia.client.legacy.model;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Vector3f;

public final class LegacyMesh {
    private final List<List<float[][]>> groups;
    private final List<String> names;

    private LegacyMesh(List<List<float[][]>> groups, List<String> names) {
        this.groups = groups.stream().map(List::copyOf).toList();
        this.names = List.copyOf(names);
    }

    public static LegacyMesh parse(String obj) {
        List<float[]> positions = new ArrayList<>();
        List<float[]> corners = new ArrayList<>();
        List<List<float[][]>> groups = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<float[][]> faces = null;
        int count = 0;
        for (String raw : obj.split("\\R")) {
            String[] words = raw.trim().split("\\s+");
            switch (words[0]) {
                case "v" -> positions.add(new float[] {Float.parseFloat(words[1]), Float.parseFloat(words[2]),
                        Float.parseFloat(words[3])});
                case "vt" -> corners.add(new float[] {Float.parseFloat(words[1]), Float.parseFloat(words[2])});
                case "g", "o" -> {
                    String name = words.length > 1 ? words[1] : "";
                    if (faces == null || !faces.isEmpty()) {
                        faces = new ArrayList<>();
                        groups.add(faces);
                        names.add(name);
                    } else {
                        names.set(names.size() - 1, name);
                    }
                }
                case "f" -> {
                    int corners4 = Math.min(4, words.length - 1);
                    if (corners4 < 3) {
                        continue;
                    }
                    float[][] face = new float[corners4][];
                    for (int i = 0; i < corners4; i++) {
                        String[] refs = words[i + 1].split("/");
                        float[] at = positions.get(index(refs[0], positions.size()));
                        int corner = refs.length > 1 && !refs[1].isEmpty() ? index(refs[1], corners.size()) : -1;
                        float[] uv = corner >= 0 ? corners.get(corner) : new float[2];
                        face[i] = new float[] {at[0], at[1], at[2], uv[0], uv[1], corner};
                    }
                    if (faces == null) {
                        faces = new ArrayList<>();
                        groups.add(faces);
                        names.add("");
                    }
                    faces.add(face);
                    count++;
                }
                default -> {
                }
            }
        }
        if (count == 0) {
            throw new IllegalArgumentException("No faces");
        }
        for (int i = groups.size() - 1; i >= 0; i--) {
            if (groups.get(i).isEmpty()) {
                groups.remove(i);
                names.remove(i);
            }
        }
        return new LegacyMesh(groups, names);
    }

    // OBJ indices count from one, and from the end when negative
    private static int index(String ref, int size) {
        int n = Integer.parseInt(ref);
        return n < 0 ? size + n : n - 1;
    }

    public int groups() {
        return groups.size();
    }

    public List<float[][]> faces(int group) {
        return groups.get(group);
    }

    // the old loader the original's renderers used flipped V; its own OBJ loader did not
    public LegacyMesh flippedV() {
        List<List<float[][]>> flipped = new ArrayList<>();
        for (List<float[][]> group : groups) {
            List<float[][]> faces = new ArrayList<>();
            for (float[][] face : group) {
                float[][] copy = new float[face.length][];
                for (int i = 0; i < face.length; i++) {
                    copy[i] = face[i].clone();
                    copy[i][4] = 1.0F - face[i][4];
                }
                faces.add(copy);
            }
            flipped.add(faces);
        }
        return new LegacyMesh(flipped, names);
    }

    public String name(int group) {
        return names.get(group);
    }

    public int group(String name) {
        return names.indexOf(name);
    }

    public void render(PoseStack.Pose pose, VertexConsumer consumer, int colour, int light, int overlay) {
        for (int group = 0; group < groups.size(); group++) {
            render(group, pose, consumer, colour, light, overlay);
        }
    }

    public void render(int group, PoseStack.Pose pose, VertexConsumer consumer, int colour, int light, int overlay) {
        Vector3f normal = new Vector3f();
        for (float[][] face : groups.get(group)) {
            normal(face, normal);
            for (int i = 0; i < 4; i++) {
                float[] corner = face[Math.min(i, face.length - 1)];
                consumer.addVertex(pose, corner[0], corner[1], corner[2])
                        .setColor(colour)
                        .setUv(corner[3], corner[4])
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(pose, normal.x, normal.y, normal.z);
            }
        }
    }

    public void render(String group, PoseStack.Pose pose, VertexConsumer consumer, int colour, int light,
            int overlay) {
        int found = group(group);
        if (found >= 0) {
            render(found, pose, consumer, colour, light, overlay);
        }
    }

    public static Vector3f normal(float[][] face, Vector3f into) {
        float[] a = face[0];
        float[] b = face[1];
        float[] c = face[2];
        into.set(b[0] - a[0], b[1] - a[1], b[2] - a[2]).cross(c[0] - a[0], c[1] - a[1], c[2] - a[2]);
        if (into.lengthSquared() > 0) {
            into.normalize();
        }
        return into;
    }
}
