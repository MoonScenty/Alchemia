package me.moonscenty.alchemia.client.legacy;

import java.util.Optional;

public enum LegacyEdition {
    FOUR("1.7.10"),
    FIVE("1.8.9");

    private final String minecraftVersion;

    LegacyEdition(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public String fileName() {
        return switch (this) {
            case FOUR -> "Thaumcraft-1.7.10-4.2.3.5.jar";
            case FIVE -> "Thaumcraft-1.8.9-5.2.4.jar";
        };
    }

    public String downloadPage() {
        return switch (this) {
            case FOUR -> "https://www.curseforge.com/minecraft/mc-mods/thaumcraft/files/2227552";
            case FIVE -> "https://www.curseforge.com/minecraft/mc-mods/thaumcraft/files/2287496";
        };
    }

    String describe() {
        return switch (this) {
            case FOUR -> "Thaumcraft 4 for Minecraft " + minecraftVersion + " (e.g. Thaumcraft-1.7.10-4.2.3.5.jar)";
            case FIVE -> "Thaumcraft 5 for Minecraft " + minecraftVersion + " (e.g. Thaumcraft-1.8.9-5.2.4.jar)";
        };
    }

    static Optional<LegacyEdition> byMinecraftVersion(String version) {
        for (LegacyEdition edition : values()) {
            if (edition.minecraftVersion.equals(version)) {
                return Optional.of(edition);
            }
        }
        return Optional.empty();
    }
}
