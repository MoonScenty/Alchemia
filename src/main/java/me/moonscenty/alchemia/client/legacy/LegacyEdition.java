package me.moonscenty.alchemia.client.legacy;

import java.util.Optional;

/**
 * Which release of the original a jar in {@code old_thaumcraft/} is. The two keep their pictures under different
 * names (4 wrote {@code thaumiumingot}, 5 wrote {@code ingot_thaumium}), so a mapping has to say which one it means.
 */
public enum LegacyEdition {
    FOUR("1.7.10"),
    FIVE("1.8.9");

    private final String minecraftVersion;

    LegacyEdition(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
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
