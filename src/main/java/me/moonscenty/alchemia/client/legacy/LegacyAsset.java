package me.moonscenty.alchemia.client.legacy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

import javax.annotation.Nullable;

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
     * Only taken when the model of that key was read too. A sheet laid out for the original's model is nonsense on
     * the shape our own fallback draws, so the two come in together or not at all.
     */
    public LegacyAsset forModel(String key) {
        return new LegacyAsset(target, sources, transform, key);
    }

    private static byte[] tint(byte[] png, int rgb) throws IOException {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        try (NativeImage image = NativeImage.read(png)) {
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
