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
        public static final TagKey<Block> ELDRITCH_STONEWORK = mod("eldritch_stonework");
        public static final TagKey<Block> CRYSTALS = mod("crystals");
        public static final TagKey<Block> CRYSTAL_GROWABLE = mod("crystal_growable");

        public static final TagKey<Block> CINDERPEARL_PLACEABLE = mod("cinderpearl_placeable");

        public static final TagKey<Block> TAINT = mod("taint");
        public static final TagKey<Block> TAINT_ROTS_TO_SOIL = mod("taint_rots_to_soil");
        public static final TagKey<Block> TAINT_ROTS_TO_ROCK = mod("taint_rots_to_rock");
        public static final TagKey<Block> TAINT_ROTS_TO_CRUST = mod("taint_rots_to_crust");

        public static final TagKey<Block> HEATS_CRUCIBLE = mod("heats_crucible");

        public static final TagKey<Block> STABILISES_INFUSION = mod("stabilises_infusion");

        public static final TagKey<Block> MINEABLE_PRIMAL_CRUSHER = mod("mineable/primal_crusher");

        public static final TagKey<Block> ORES_AMBER = common("ores/amber");
        public static final TagKey<Block> ORES_CINNABAR = common("ores/cinnabar");

        public static final TagKey<Block> STORAGE_BLOCKS_AMBER = common("storage_blocks/amber");
        public static final TagKey<Block> STORAGE_BLOCKS_ALCHEMIUM = common("storage_blocks/alchemium");
        public static final TagKey<Block> STORAGE_BLOCKS_BRASS = common("storage_blocks/brass");
        public static final TagKey<Block> STORAGE_BLOCKS_VOID = common("storage_blocks/void");

        private static TagKey<Block> mod(String path) {
            return TagKey.create(Registries.BLOCK, Alchemia.id(path));
        }

        private static TagKey<Block> common(String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }

    public static class Entities {
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> CHAMPION_CANDIDATES =
                TagKey.create(Registries.ENTITY_TYPE, Alchemia.id("champion_candidates"));
    }

    public static class Biomes {
        public static final TagKey<Biome> HAS_ANGRY_ZOMBIES = TagKey.create(Registries.BIOME, Alchemia.id("has_angry_zombies"));
        public static final TagKey<Biome> HAS_GREATWOOD = TagKey.create(Registries.BIOME, Alchemia.id("has_greatwood"));
        public static final TagKey<Biome> HAS_SILVERWOOD = TagKey.create(Registries.BIOME, Alchemia.id("has_silverwood"));
        public static final TagKey<Biome> HAS_CINDERPEARL = TagKey.create(Registries.BIOME, Alchemia.id("has_cinderpearl"));

        public static final TagKey<Biome> NODE_DARKENS_INTO = TagKey.create(Registries.BIOME, Alchemia.id("node_darkens_into"));
    }

    public static class Items {
        public static final TagKey<Item> REVEALS = mod("reveals");
        public static final TagKey<Item> NITOR = mod("nitor");
        public static final TagKey<Item> RUNIC_AUGMENTABLE = mod("runic_augmentable");

        public static final TagKey<Item> FOCI = mod("foci");
        public static final TagKey<Item> HASTE_ENCHANTABLE = mod("enchantable/haste");
        public static final TagKey<Item> REPAIR_ENCHANTABLE = mod("enchantable/repair");
        public static final TagKey<Item> WARPS_WHEN_MADE = mod("warps_when_made");

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
        public static final TagKey<Item> STORAGE_BLOCKS_VOID = common("storage_blocks/void");
        public static final TagKey<Item> INGOTS_ALCHEMIUM = common("ingots/alchemium");
        public static final TagKey<Item> INGOTS_BRASS = common("ingots/brass");
        public static final TagKey<Item> NUGGETS_ALCHEMIUM = common("nuggets/alchemium");
        public static final TagKey<Item> NUGGETS_BRASS = common("nuggets/brass");
        public static final TagKey<Item> NUGGETS_QUICKSILVER = common("nuggets/quicksilver");
        public static final TagKey<Item> INGOTS_VOID = common("ingots/void");
        public static final TagKey<Item> NUGGETS_VOID = common("nuggets/void");
        public static final TagKey<Item> GEARS = common("gears");
        public static final TagKey<Item> GEARS_ALCHEMIUM = common("gears/alchemium");
        public static final TagKey<Item> GEARS_BRASS = common("gears/brass");
        public static final TagKey<Item> PLATES = common("plates");
        public static final TagKey<Item> PLATES_ALCHEMIUM = common("plates/alchemium");
        public static final TagKey<Item> PLATES_BRASS = common("plates/brass");
        public static final TagKey<Item> PLATES_IRON = common("plates/iron");
        public static final TagKey<Item> PLATES_VOID = common("plates/void");

        private static TagKey<Item> mod(String path) {
            return TagKey.create(Registries.ITEM, Alchemia.id(path));
        }

        private static TagKey<Item> common(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }
}
