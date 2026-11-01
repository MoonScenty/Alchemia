package me.moonscenty.alchemia.client;

/** The channels of a packed {@code 0xRRGGBB} colour, each from nought to one. */
public final class ClientColours {
    private ClientColours() {
    }

    public static float red(int colour) {
        return (colour >> 16 & 0xFF) / 255.0F;
    }

    public static float green(int colour) {
        return (colour >> 8 & 0xFF) / 255.0F;
    }

    public static float blue(int colour) {
        return (colour & 0xFF) / 255.0F;
    }
}
