package me.moonscenty.alchemia;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AlchemiaConfig {
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

    // Common values are added here as the systems that need them get ported.

    static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();

    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ALWAYS_SHOW_ASPECTS = CLIENT_BUILDER
            .comment("Item aspects are hidden until you hold sneak. Turn this on to reverse that:",
                    "aspects are always shown, and holding sneak hides them.")
            .define("alwaysShowAspects", false);

    static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
}
