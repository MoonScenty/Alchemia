package me.moonscenty.alchemia.client.legacy.model;

import java.util.Map;

public record LegacyModel(int textureWidth, int textureHeight, Map<String, LegacyPart> bones,
        Map<String, LegacyPart> fields) {
}
