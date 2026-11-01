package me.moonscenty.alchemia.client.legacy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.platform.NativeImage;

public record LegacyAsset(String target, List<Source> sources, Transform transform, @Nullable String model) {
    public record Source(LegacyEdition edition, String path) {
    }

    @FunctionalInterface
    public interface Transform {
        Transform COPY = bytes -> bytes;

        byte[] apply(byte[] bytes) throws IOException;
    }

    public static LegacyAsset of(String target) {
        return new LegacyAsset(target, List.of(), Transform.COPY, null);
    }

    public LegacyAsset from(LegacyEdition edition, String path) {
        List<Source> more = new ArrayList<>(sources);
        more.add(new Source(edition, path));
        return new LegacyAsset(target, List.copyOf(more), transform, model);
    }

    public LegacyAsset five(String path) {
        return from(LegacyEdition.FIVE, path);
    }

    public LegacyAsset four(String path) {
        return from(LegacyEdition.FOUR, path);
    }

    // colour supplied lazily: registries aren't filled when this table is built
    public LegacyAsset tinted(IntSupplier rgb) {
        return new LegacyAsset(target, sources, bytes -> tint(bytes, rgb.getAsInt()), model);
    }

    public LegacyAsset tinted(int rgb) {
        return tinted(() -> rgb);
    }

    // the game reads PNG only, so Java's reader decodes the JPEG
    public LegacyAsset fromJpeg() {
        return new LegacyAsset(target, sources, LegacyAsset::jpegToPng, model);
    }

    private static byte[] jpegToPng(byte[] jpeg) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(jpeg));
        if (image == null) {
            throw new IOException("Not a picture Java can read");
        }
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        return png.toByteArray();
    }

    public LegacyAsset cropped(int x, int y, int width, int height) {
        Transform before = transform;
        return new LegacyAsset(target, sources, bytes -> crop(before.apply(bytes), x, y, width, height), model);
    }

    private static byte[] crop(byte[] png, int x, int y, int width, int height) throws IOException {
        try (NativeImage sheet = NativeImage.read(new ByteArrayInputStream(png));
                NativeImage cut = new NativeImage(width, height, true)) {
            if (x + width > sheet.getWidth() || y + height > sheet.getHeight()) {
                throw new IOException("The sheet is " + sheet.getWidth() + "x" + sheet.getHeight()
                        + ", too small to cut " + width + "x" + height + " at " + x + "," + y);
            }
            sheet.copyRect(cut, x, y, 0, 0, width, height, false, false);
            return cut.asByteArray();
        }
    }

    public LegacyAsset forModel(String key) {
        return new LegacyAsset(target, sources, transform, key);
    }

    public LegacyAsset asModel() {
        return new LegacyAsset(target, sources, LegacyAsset::model, model);
    }

    public LegacyAsset asModel(String renderType) {
        return new LegacyAsset(target, sources, original -> {
            JsonObject read = JsonParser.parseString(new String(model(original), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            read.addProperty("render_type", "minecraft:" + renderType);
            return read.toString().getBytes(StandardCharsets.UTF_8);
        }, model);
    }

    public LegacyAsset asModelOver(String ours) {
        return new LegacyAsset(target, sources, original -> over(original, ours), model);
    }

    private static byte[] over(byte[] original, String ours) throws IOException {
        JsonObject theirs = JsonParser.parseString(new String(model(original), StandardCharsets.UTF_8))
                .getAsJsonObject();
        JsonObject mine;
        try (InputStream in = LegacyAsset.class.getResourceAsStream("/assets/alchemia/" + ours)) {
            if (in == null) {
                throw new IOException("No model " + ours);
            }
            mine = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
        JsonArray elements = new JsonArray();
        theirs.getAsJsonArray("elements").forEach(elements::add);
        mine.getAsJsonArray("elements").forEach(elements::add);
        JsonObject textures = mine.getAsJsonObject("textures").deepCopy();
        theirs.getAsJsonObject("textures").entrySet().forEach(entry -> textures.add(entry.getKey(), entry.getValue()));
        mine.add("textures", textures);
        mine.add("elements", elements);
        return mine.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] model(byte[] json) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(new String(json, StandardCharsets.UTF_8)));
        reader.setLenient(true);
        JsonObject root;
        try {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new IOException("Not a model", e);
        }
        root.remove("display");
        JsonObject out = (JsonObject) retarget(root);
        if (!out.has("parent") && out.has("elements")) {
            out.addProperty("parent", "minecraft:block/block");
        }
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static JsonElement retarget(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject copy = new JsonObject();
            element.getAsJsonObject().entrySet().forEach(entry -> copy.add(entry.getKey(), retarget(entry.getValue())));
            return copy;
        }
        if (element.isJsonArray()) {
            JsonArray copy = new JsonArray();
            element.getAsJsonArray().forEach(item -> copy.add(retarget(item)));
            return copy;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.startsWith("thaumcraft:blocks/")) {
                return new JsonPrimitive("alchemia:block/legacy/" + value.substring("thaumcraft:blocks/".length()));
            }
            if (lower.startsWith("thaumcraft:block/")) {
                return new JsonPrimitive("alchemia:block/" + value.substring("thaumcraft:block/".length()));
            }
            if (lower.startsWith("thaumcraft:items/")) {
                return new JsonPrimitive("alchemia:item/legacy/" + value.substring("thaumcraft:items/".length()));
            }
            // 1.8.9 named the flat item model without a namespace
            if (lower.equals("builtin/generated")) {
                return new JsonPrimitive("minecraft:item/generated");
            }
            if (lower.startsWith("blocks/")) {
                String name = value.substring("blocks/".length());
                return new JsonPrimitive("minecraft:block/" + (name.equals("fire_layer_0") ? "fire_0" : name));
            }
        }
        return element;
    }

    private static byte[] tint(byte[] png, int rgb) throws IOException {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        try (NativeImage image = NativeImage.read(new ByteArrayInputStream(png))) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    // NativeImage pixels are ABGR
                    int abgr = image.getPixelRGBA(x, y);
                    int a = abgr >>> 24;
                    int pb = (abgr >> 16 & 0xFF) * b / 255;
                    int pg = (abgr >> 8 & 0xFF) * g / 255;
                    int pr = (abgr & 0xFF) * r / 255;
                    image.setPixelRGBA(x, y, a << 24 | pb << 16 | pg << 8 | pr);
                }
            }
            return image.asByteArray();
        }
    }
}
