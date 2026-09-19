package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> CRYSTALS = mod("crystals");
        /** Blocks a naturally generated crystal can grow on. */
        public static final TagKey<Block> CRYSTAL_GROWABLE = mod("crystal_growable");

        public static final TagKey<Block> ORES_AMBER = common("ores/amber");
        public static final TagKey<Block> ORES_CINNABAR = common("ores/cinnabar");

        private static TagKey<Block> mod(String path) {
            return TagKey.create(Registries.BLOCK, Alchemia.id(path));
        }

        private static TagKey<Block> common(String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }

    public static class Items {
        public static final TagKey<Item> SHARDS = mod("shards");
        public static final TagKey<Item> CLUSTERS = mod("clusters");

        public static final TagKey<Item> ORES_AMBER = common("ores/amber");
        public static final TagKey<Item> ORES_CINNABAR = common("ores/cinnabar");
        public static final TagKey<Item> GEMS_AMBER = common("gems/amber");
        public static final TagKey<Item> GEMS_QUICKSILVER = common("gems/quicksilver");
        public static final TagKey<Item> RAW_MATERIALS_CINNABAR = common("raw_materials/cinnabar");

        private static TagKey<Item> mod(String path) {
            return TagKey.create(Registries.ITEM, Alchemia.id(path));
        }

        private static TagKey<Item> common(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }
}
