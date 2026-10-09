package me.moonscenty.alchemia.client.legacy.model;

import java.util.Map;

/**
 * A model of the original as its constructor built it.
 *
 * @param textureWidth  the sheet the model was drawn for
 * @param textureHeight the sheet the model was drawn for
 * @param bones         a biped's seven parts by the names this game gives them ({@code head}, {@code hat},
 *                      {@code body}, {@code right_arm}, {@code left_arm}, {@code right_leg}, {@code left_leg}), with
 *                      everything the original hung off them. Empty for a model that is not a biped
 * @param fields        every part the constructor kept in a field of its own, by that field's name, whether or not
 *                      it was hung off anything
 */
public record LegacyModel(int textureWidth, int textureHeight, Map<String, LegacyPart> bones,
        Map<String, LegacyPart> fields) {
}
