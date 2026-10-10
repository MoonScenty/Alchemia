package me.moonscenty.alchemia.client.legacy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

/**
 * One of our files, and where in the original to find it.
 *
 * @param target    the path under {@code assets/alchemia/} this replaces
 * @param sources   where to look, in order: the first release that has the file wins
 * @param transform what to do to it on the way. The original tinted many greys in code; we bake the colour in, since
 *                  our own fallback pictures are already coloured and a tint in the model would colour them twice
 * @param model     the key of a model in {@link LegacyAssets#MODELS} this file is only any good with, or null
 */
public record LegacyAsset(String target, List<Source> sources, Transform transform, @Nullable String model) {
    /** A path under the original's {@code assets/thaumcraft/}. */
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

    /**
     * Multiplies every pixel by an RGB colour, which is what the game does to a tinted quad. The colour is asked for
     * at import time rather than now, so it can come from a registry that is not filled yet when this table is built.
     */
    public LegacyAsset tinted(IntSupplier rgb) {
        return new LegacyAsset(target, sources, bytes -> tint(bytes, rgb.getAsInt()), model);
    }

    public LegacyAsset tinted(int rgb) {
        return tinted(() -> rgb);
    }

    /**
     * Turns a JPEG into a PNG. The original kept its large backgrounds as JPEGs; this game reads nothing but PNG, and
     * its own image reader refuses anything else at the first byte, so Java's reader does the decoding.
     */
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

    /** Cuts one picture out of a sheet the original kept many in, after whatever else was done to it. */
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

    /**
     * Only taken when the model of that key was read too. A sheet laid out for the original's model is nonsense on
     * the shape our own fallback draws, so the two come in together or not at all.
     */
    public LegacyAsset forModel(String key) {
        return new LegacyAsset(target, sources, transform, key);
    }

    /**
     * A model file of the original, made one this game reads. 1.8.9 read its model files loosely, so some have a word
     * left unquoted; they are read loosely here too and written back out properly. Every texture is pointed at where
     * {@link LegacyAssets} imports the original's pictures ({@code block/legacy/}), and a model at our own of the same
     * name. The old display settings meant something else then and are dropped: a block model takes the game's
     * ordinary block ones instead.
     */
    public LegacyAsset asModel() {
        return new LegacyAsset(target, sources, LegacyAsset::model, model);
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
                    // NativeImage keeps its pixels as ABGR
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
