package me.moonscenty.alchemia.datagen;

import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Every translation is declared for both languages side by side so neither can fall behind.
 */
public abstract class ModLanguageProvider extends LanguageProvider {
    protected ModLanguageProvider(PackOutput output, String locale) {
        super(output, Alchemia.MODID, locale);
    }

    /** Picks the text for this provider's language. */
    protected abstract String pick(String english, String korean);

    @Override
    protected void addTranslations() {
        add("itemGroup.alchemia", "Alchemia");
        add("alchemia.configuration.title", pick("Alchemia Configs", "Alchemia 설정"));
        add("alchemia.configuration.section.alchemia.common.toml", pick("Alchemia Configs", "Alchemia 설정"));
        add("alchemia.configuration.section.alchemia.common.toml.title", pick("Alchemia Configs", "Alchemia 설정"));

        addBlock(ModBlocks.AMBER_ORE, pick("Amber Ore", "호박 광석"));
        addBlock(ModBlocks.DEEPSLATE_AMBER_ORE, pick("Deepslate Amber Ore", "심층암 호박 광석"));
        addBlock(ModBlocks.CINNABAR_ORE, pick("Cinnabar Ore", "진사 광석"));
        addBlock(ModBlocks.DEEPSLATE_CINNABAR_ORE, pick("Deepslate Cinnabar Ore", "심층암 진사 광석"));

        Map<CrystalType, String[]> aspectNames = Map.of(
                CrystalType.AIR, new String[] {"Air", "공기"},
                CrystalType.FIRE, new String[] {"Fire", "불"},
                CrystalType.WATER, new String[] {"Water", "물"},
                CrystalType.EARTH, new String[] {"Earth", "땅"},
                CrystalType.ORDER, new String[] {"Order", "질서"},
                CrystalType.ENTROPY, new String[] {"Entropy", "엔트로피"},
                CrystalType.FLUX, new String[] {"Flux", "플럭스"});
        aspectNames.forEach((type, names) -> {
            addBlock(ModBlocks.CRYSTALS.get(type), pick(names[0] + " Crystal", names[1] + " 결정"));
            addItem(ModItems.SHARDS.get(type), pick(names[0] + " Shard", names[1] + " 조각"));
        });
        addItem(ModItems.BALANCED_SHARD, pick("Balanced Shard", "균형 조각"));

        addItem(ModItems.AMBER, pick("Amber", "호박"));
        addItem(ModItems.QUICKSILVER, pick("Quicksilver", "수은"));
        addItem(ModItems.RAW_CINNABAR, pick("Raw Cinnabar", "진사 원석"));
        addItem(ModItems.IRON_CLUSTER, pick("Iron Cluster", "철 군집"));
        addItem(ModItems.GOLD_CLUSTER, pick("Gold Cluster", "금 군집"));
        addItem(ModItems.COPPER_CLUSTER, pick("Copper Cluster", "구리 군집"));
        addItem(ModItems.CINNABAR_CLUSTER, pick("Cinnabar Cluster", "진사 군집"));
    }

    public static class English extends ModLanguageProvider {
        public English(PackOutput output) {
            super(output, "en_us");
        }

        @Override
        protected String pick(String english, String korean) {
            return english;
        }
    }

    public static class Korean extends ModLanguageProvider {
        public Korean(PackOutput output) {
            super(output, "ko_kr");
        }

        @Override
        protected String pick(String english, String korean) {
            return korean;
        }
    }
}
