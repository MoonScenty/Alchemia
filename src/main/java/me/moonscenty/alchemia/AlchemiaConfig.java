package me.moonscenty.alchemia;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AlchemiaConfig {
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue TAINT_SPREADS = COMMON_BUILDER
            .comment("Whether taint creeps across the land and flux events happen at all.",
                    "Turn this off for a quieter world where flux only sits in the aura.")
            .define("taintSpreads", true);

    public static final ModConfigSpec.DoubleValue TAINT_SPREAD_COST = COMMON_BUILDER
            .comment("How often a step of taint spreading takes a point of flux out of the aura, from 0 to 1.",
                    "Higher means taint burns itself out sooner.")
            .defineInRange("taintSpreadCost", 0.05, 0.0, 1.0);

    static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();

    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ALWAYS_SHOW_ASPECTS = CLIENT_BUILDER
            .comment("Item aspects are hidden until you hold sneak. Turn this on to reverse that:",
                    "aspects are always shown, and holding sneak hides them.")
            .define("alwaysShowAspects", false);

    static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
}
