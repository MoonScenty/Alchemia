package me.moonscenty.alchemia.client.legacy;

/**
 * Where one release of the original built a model in code: which class, which constructor, and with what.
 *
 * @param arguments boxed ints and floats, in the constructor's order
 */
public record LegacyModelSource(LegacyEdition edition, String className, String descriptor, Object... arguments) {
}
