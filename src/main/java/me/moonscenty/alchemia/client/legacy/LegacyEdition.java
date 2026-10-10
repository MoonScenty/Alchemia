package me.moonscenty.alchemia.client.legacy;

import java.util.Optional;

/**
 * Which release of the original a jar in {@code legacy/} is. The two keep their pictures under different
 * names (4 wrote {@code thaumiumingot}, 5 wrote {@code ingot_thaumium}), so a mapping has to say which one it means.
 */
public enum LegacyEdition {
    FOUR("1.7.10"),
    FIVE("1.8.9");

    private final String minecraftVersion;

    LegacyEdition(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    /** Which jar this is, in words a player can go and find it by. */
    String describe() {
        return switch (this) {
            case FOUR -> "Thaumcraft 4 for Minecraft " + minecraftVersion + " (e.g. Thaumcraft-1.7.10-4.2.3.5.jar)";
            case FIVE -> "Thaumcraft 5 for Minecraft " + minecraftVersion + " (e.g. Thaumcraft-1.8.9-5.2.4.jar)";
        };
    }

    /** Reads the {@code mcversion} a jar's {@code mcmod.info} declares. The file name is the player's to choose. */
    static Optional<LegacyEdition> byMinecraftVersion(String version) {
        for (LegacyEdition edition : values()) {
            if (edition.minecraftVersion.equals(version)) {
                return Optional.of(edition);
            }
        }
        return Optional.empty();
    }
}
