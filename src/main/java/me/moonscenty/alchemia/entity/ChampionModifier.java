package me.moonscenty.alchemia.entity;

/**
 * What a champion is the champion of, in the original's order. Each does its work at one of four times: never (it
 * is only a better number), every tick, when the champion hurts something, or when something hurts the champion.
 * <p>
 * The colour is what the client shows around one, roughly the original's for each.
 */
public enum ChampionModifier {
    BOLD("bold", When.NEVER, 0x5A00E6),
    SPINED("spine", When.HURT, 0xB33333),
    ARMORED("armor", When.HURT, 0xEEEEFF),
    MIGHTY("mighty", When.NEVER, 0xE6E6E6),
    GRIM("grim", When.HITS, 0x1A1A1A),
    WARDED("warded", When.TICK, 0x66CCFF),
    WARPED("warp", When.HITS, 0x8000B3),
    UNDYING("undying", When.TICK, 0x55DD55),
    FIERY("fiery", When.HITS, 0xFF6600),
    SICKLY("sickly", When.HITS, 0x99AA33),
    VENOMOUS("venomous", When.HITS, 0x4E9331),
    VAMPIRIC("vampiric", When.HITS, 0x800000),
    INFESTED("infested", When.HURT, 0xB34DB3);

    public enum When {
        NEVER,
        TICK,
        HITS,
        HURT
    }

    private final String key;
    private final When when;
    private final int colour;

    ChampionModifier(String key, When when, int colour) {
        this.key = key;
        this.when = when;
        this.colour = colour;
    }

    /** The original's name for it, which is also the last part of its translation key. */
    public String key() {
        return key;
    }

    public String translationKey() {
        return "champion.mod." + key;
    }

    public When when() {
        return when;
    }

    public int colour() {
        return colour;
    }

    /** The modifier at this index, or none for anything else (the unrolled and the ordinary). */
    public static ChampionModifier of(int index) {
        return index >= 0 && index < values().length ? values()[index] : null;
    }
}
