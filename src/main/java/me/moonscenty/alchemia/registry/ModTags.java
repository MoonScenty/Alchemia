package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> CRYSTALS = mod("crystals");
        /** Blocks a naturally generated crystal can grow on. */
        public static final TagKey<Block> CRYSTAL_GROWABLE = mod("crystal_growable");

        /** Sand, dirt and terracotta: the dry ground a cinderpearl takes root in. */
        public static final TagKey<Block> CINDERPEARL_PLACEABLE = mod("cinderpearl_placeable");

        public static final TagKey<Block> ORES_AMBER = common("ores/amber");
        public static final TagKey<Block> ORES_CINNABAR = common("ores/cinnabar");

        public static final TagKey<Block> STORAGE_BLOCKS_AMBER = common("storage_blocks/amber");
        public static final TagKey<Block> STORAGE_BLOCKS_ALCHEMIUM = common("storage_blocks/alchemium");
        public static final TagKey<Block> STORAGE_BLOCKS_BRASS = common("storage_blocks/brass");

        private static TagKey<Block> mod(String path) {
            return TagKey.create(Registries.BLOCK, Alchemia.id(path));
        }

        private static TagKey<Block> common(String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }

    public static class Biomes {
        public static final TagKey<Biome> HAS_GREATWOOD = TagKey.create(Registries.BIOME, Alchemia.id("has_greatwood"));
        public static final TagKey<Biome> HAS_SILVERWOOD = TagKey.create(Registries.BIOME, Alchemia.id("has_silverwood"));
        public static final TagKey<Biome> HAS_CINDERPEARL = TagKey.create(Registries.BIOME, Alchemia.id("has_cinderpearl"));

        /**
         * What a dark node turns the land around it into.
         * <p>
         * A tag rather than a named biome, so that when there is somewhere properly eerie to turn it into, that
         * biome joins this tag and nothing in the code has to change. For now it is desert: somewhere gone wrong.
         */
        public static final TagKey<Biome> NODE_DARKENS_INTO = TagKey.create(Registries.BIOME, Alchemia.id("node_darkens_into"));
    }

    public static class Items {
        /**
         * Worn or carried, anything in here shows aura nodes without an instrument in hand.
         * <p>
         * Empty until the goggles of revealing arrive; the sight is written against the tag so they only have to
         * join it.
         */
        public static final TagKey<Item> REVEALS = mod("reveals");

        public static final TagKey<Item> SHARDS = mod("shards");
        public static final TagKey<Item> CLUSTERS = mod("clusters");

        public static final TagKey<Item> ORES_AMBER = common("ores/amber");
        public static final TagKey<Item> ORES_CINNABAR = common("ores/cinnabar");
        public static final TagKey<Item> GEMS_AMBER = common("gems/amber");
        public static final TagKey<Item> GEMS_QUICKSILVER = common("gems/quicksilver");
        public static final TagKey<Item> RAW_MATERIALS_CINNABAR = common("raw_materials/cinnabar");

        public static final TagKey<Item> STORAGE_BLOCKS_AMBER = common("storage_blocks/amber");
        public static final TagKey<Item> STORAGE_BLOCKS_ALCHEMIUM = common("storage_blocks/alchemium");
        public static final TagKey<Item> STORAGE_BLOCKS_BRASS = common("storage_blocks/brass");
        public static final TagKey<Item> INGOTS_ALCHEMIUM = common("ingots/alchemium");
        public static final TagKey<Item> INGOTS_BRASS = common("ingots/brass");
        public static final TagKey<Item> NUGGETS_ALCHEMIUM = common("nuggets/alchemium");
        public static final TagKey<Item> NUGGETS_BRASS = common("nuggets/brass");
        public static final TagKey<Item> NUGGETS_QUICKSILVER = common("nuggets/quicksilver");
        public static final TagKey<Item> GEARS = common("gears");
        public static final TagKey<Item> GEARS_ALCHEMIUM = common("gears/alchemium");
        public static final TagKey<Item> GEARS_BRASS = common("gears/brass");
        public static final TagKey<Item> PLATES = common("plates");
        public static final TagKey<Item> PLATES_ALCHEMIUM = common("plates/alchemium");
        public static final TagKey<Item> PLATES_BRASS = common("plates/brass");
        public static final TagKey<Item> PLATES_IRON = common("plates/iron");

        private static TagKey<Item> mod(String path) {
            return TagKey.create(Registries.ITEM, Alchemia.id(path));
        }

        private static TagKey<Item> common(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }
}
