package me.moonscenty.alchemia.datagen;

import java.util.Locale;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
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
        add("alchemia.configuration.section.alchemia.client.toml", pick("Display", "표시"));
        add("alchemia.configuration.section.alchemia.client.toml.title", pick("Display", "표시"));
        add("alchemia.configuration.alwaysShowAspects", pick("Always Show Aspects", "상 항상 표시"));
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

        addBlock(ModBlocks.ALCHEMIUM_BLOCK, pick("Block of Alchemium", "알케미움 블록"));
        addBlock(ModBlocks.BRASS_BLOCK, pick("Block of Brass", "황동 블록"));
        addItem(ModItems.ALCHEMIUM_INGOT, pick("Alchemium Ingot", "알케미움 주괴"));
        addItem(ModItems.BRASS_INGOT, pick("Brass Ingot", "황동 주괴"));
        addItem(ModItems.ALCHEMIUM_NUGGET, pick("Alchemium Nugget", "알케미움 조각"));
        addItem(ModItems.BRASS_NUGGET, pick("Brass Nugget", "황동 조각"));
        addItem(ModItems.QUICKSILVER_DROP, pick("Quicksilver Drop", "수은 방울"));
        addItem(ModItems.ALCHEMIUM_GEAR, pick("Alchemium Gear", "알케미움 톱니바퀴"));
        addItem(ModItems.BRASS_GEAR, pick("Brass Gear", "황동 톱니바퀴"));
        addItem(ModItems.ALCHEMIUM_PLATE, pick("Alchemium Plate", "알케미움 판"));
        addItem(ModItems.BRASS_PLATE, pick("Brass Plate", "황동 판"));
        addItem(ModItems.IRON_PLATE, pick("Iron Plate", "철 판"));
        addItem(ModItems.SALIS_MUNDUS, pick("Salis Mundus", "살리스 문두스"));



        addItem(ModItems.ALCHEMOMETER, pick("Alchemometer", "알케모미터"));
        addItem(ModItems.NODE_PLACER, pick("Node Placer", "노드 배치기"));
        add("item.alchemia.creative_only", pick("Creative only", "크리에이티브 전용"));
        addBlock(ModBlocks.RESEARCH_TABLE, pick("Research Table", "연구 탁자"));
        addItem(ModItems.SCRIBING_TOOLS, pick("Scribing Tools", "필기구"));
        addItem(ModItems.RESEARCH_NOTES, pick("Research Notes", "연구 노트"));
        addItem(ModItems.ALCHEMONOMICON, pick("Alchemonomicon", "알케모노미콘"));
        add("research.alchemia.known", pick("Worked out", "알아냄"));
        add("research.alchemia.locked", pick("Something must come first", "먼저 알아내야 할 것이 있다"));
        add("research.alchemia.ready", pick("Ready to be worked on", "연구할 수 있다"));
        add("research.alchemia.needs", pick("Still to be found: %s", "아직 찾지 못함: %s"));
        add("item.alchemia.research_notes.on", pick("Research Notes: %s", "연구 노트: %s"));
        add("item.alchemia.research_notes.unsolved", pick("Not worked out yet", "아직 풀지 못했다"));
        add("item.alchemia.research_notes.solved", pick("Worked out", "풀어냈다"));

        add("note.alchemia.written", pick("You draw up a fresh set of notes.", "새 연구 노트를 그려냈다."));
        add("note.alchemia.already_carried", pick("You are already carrying those notes.", "이미 그 연구 노트를 지니고 있다."));
        add("note.alchemia.not_ready", pick("There is nothing there to work on yet.", "아직 그것을 연구할 수 없다."));
        add("note.alchemia.no_paper", pick("You have no paper to write on.", "쓸 종이가 없다."));
        addBlock(ModBlocks.NODE_STABILIZER, pick("Node Stabilizer", "노드 안정기"));
        addBlock(ModBlocks.TAINT_FIBRE, pick("Fibrous Taint", "오염 섬유"));
        addBlock(ModBlocks.TAINT_SOIL, pick("Tainted Soil", "오염된 흙"));
        addBlock(ModBlocks.TAINT_CRUST, pick("Crusted Taint", "오염 껍질"));
        addBlock(ModBlocks.TAINT_ROCK, pick("Tainted Rock", "오염된 바위"));
        addBlock(ModBlocks.TAINT_LOG, pick("Taintwood Log", "오염된 원목"));
        addBlock(ModBlocks.FLUX_GOO, pick("Flux Goo", "플럭스 구스"));
        add("entity.alchemia.taint_cloud", pick("Taint Cloud", "오염 구름"));
        add("entity.alchemia.aura_node", pick("Aura Node", "오라 노드"));
        add("flux_event.alchemia.warp", pick("The nearby aura suddenly twists and warps, leaving your thoughts in a shambles.",
                "주변의 오라가 갑자기 비틀리며 생각이 헝클어진다."));
        add("flux_event.alchemia.sickness", pick("The local aura momentarily becomes unstable, hampering your magical ability.",
                "이곳의 오라가 잠시 불안정해져 마법이 둔해진다."));
        add("node_type.alchemia.plain", pick("Normal Node", "평범한 노드"));
        add("node_type.alchemia.dark", pick("Sinister Node", "불길한 노드"));
        add("node_type.alchemia.hungry", pick("Hungry Node", "굶주린 노드"));
        add("node_type.alchemia.pure", pick("Pure Node", "순수한 노드"));
        add("node_type.alchemia.tainted", pick("Tainted Node", "오염된 노드"));
        add("node_type.alchemia.unstable", pick("Unstable Node", "불안정한 노드"));
        add("node_type.alchemia.astral", pick("Astral Node", "천상의 노드"));
        add("note.alchemia.no_such_mix", pick("These two make nothing", "이 둘로는 아무것도 만들어지지 않는다"));
        add("note.alchemia.pinned", pick("Pinned by the subject", "주제가 고정한 자리"));
        add("note.alchemia.would_hold", pick("This would hold", "여기라면 이어진다"));
        add("note.alchemia.would_not_hold", pick("Nothing here to hold on to", "이어 붙일 것이 없다"));
        add("note.alchemia.claim", pick("Worked out. Read it back to learn %s.", "풀어냈다. 손에 들고 우클릭하면 %s을(를) 익한다."));
        add("note.alchemia.learned", pick("You work out %s.", "%s을(를) 알아냈다."));
        add("note.alchemia.already_known", pick("You already know that.", "이미 알고 있다."));
        add("note.alchemia.no_ink", pick("Your scribing tools have run dry.", "필기구의 잉크가 말랐다."));

        add("research.alchemia.take_note", pick("Click to draw up notes", "클릭해 연구 노트 그리기"));
        add("research.alchemia.open", pick("Click to read", "클릭해 읽기"));
        add("research.alchemia.crafting", pick("On the bench", "작업대에서"));
        add("research.alchemia.smelting", pick("In the fire", "불에서"));
        add("research.alchemia.recipe_missing", pick("The page has faded.", "지면이 바래 알아볼 수 없다."));
        add("scan.alchemia.nothing_there", pick("There is nothing there to read.", "읽을 것이 없다."));
        add("scan.alchemia.nothing", pick("You learn nothing from the %s.", "%s에서는 아무것도 알아낼 수 없다."));
        add("scan.alchemia.already_known", pick("The %s holds nothing new.", "%s에는 새로운 것이 없다."));
        add("scan.alchemia.learned", pick("From the %s you make out: %s", "%s에서 알아냈다: %s"));
        add("scan.alchemia.hint.unread", pick("%s — not yet read", "%s — 아직 읽지 않음"));
        add("scan.alchemia.hint.read", pick("%s — nothing further", "%s — 더 알아낼 것 없음"));

        // The branches of study. Thaumaturgy is arcana here.
        String[][] categories = {
                {"basics", "Basics", "기초"},
                {"arcana", "Arcana", "비학"},
                {"alchemy", "Alchemy", "연금술"},
                {"artifice", "Artifice", "기교"},
                {"golemancy", "Golemancy", "골렘학"},
                {"eldritch", "Eldritch", "이계"},
        };
        for (String[] category : categories) {
            add("research_category." + Alchemia.MODID + "." + category[0], pick(category[1], category[2]));
        }

        String[][] research = {
                {"aspects", "Aspects", "상",
                 "Everything is made of six primal essences and the things they combine into. An alchemometer will name them for you, one object at a time, and what it names is written down here as you go.",
                 "모든 것은 여섯 가지 원시 정수와 그것들이 결합한 것으로 이루어져 있다. 알케모미터는 한 번에 하나씩 그것들의 이름을 알려주며, 알아낸 이름은 이곳에 차곡차곡 적힌다."},
                {"amber", "Amber", "호박",
                 "Sap that hardened around something long ago, and held it there. It breaks out of the ore whole, and takes a polish that the stone around it never will.",
                 "오래전 무언가를 감싼 채 굳어버린 수액. 그것은 아직 갇혀 있다. 광석에서 통째로 떨어져 나오며, 주변의 돌은 결코 내지 못할 광택을 낸다."},
                {"quicksilver", "Quicksilver", "수은",
                 "A metal that will not keep still, coaxed out of cinnabar by fire. It pools rather than sits, and every alchemist learns early to keep a lid on it.",
                 "가만히 있지 못하는 금속. 불로 진사에서 끌어낸다. 놓아두면 앉지 않고 고이며, 연금술사라면 일찍이 뚜껑을 덮어두는 법부터 배운다."},
                {"vis_crystals", "Vis Crystals", "비스 결정",
                 "Where the aura runs thick, it settles into stone as crystal. Each shade of crystal carries the essence it grew out of, and the stone around it remembers that too.",
                 "오라가 짙게 고인 곳에서는 돌 속에 결정으로 내려앉는다. 결정의 빛깔마다 자라난 근원의 정수를 품고 있으며, 주변의 돌 또한 그것을 기억한다."},
                {"greatwood", "Greatwood", "거대나무",
                 "A tree that grows broader and older than any other. Its trunk thickens where lesser wood would split, and a single one will keep a workshop in timber for a season.",
                 "다른 어떤 나무보다 굵고 오래 자라는 나무. 약한 나무라면 갈라졌을 곳에서 오히려 줄기가 두꺼워진다. 한 그루면 작업장의 한 철 목재를 댈 수 있다."},
                {"silverwood", "Silverwood", "은빛나무",
                 "Pale wood that draws the aura to itself, and glows faintly for it. Little that is unnatural will settle near one, which makes a grove of them a quiet place to work.",
                 "오라를 끌어당기는 창백한 나무. 그 탓에 희미하게 빛난다. 부자연스러운 것들은 그 곁에 좀처럼 자리 잡지 못하니, 은빛나무 숲은 일하기에 조용한 곳이다."},
                {"warp", "Warp", "뒤틀림",
                 "Look too long into what should not be, and it begins looking back. The damage is not to the world but to the one studying it, and it does not undo itself with rest.",
                 "있어서는 안 될 것을 오래 들여다보면, 그것도 당신을 들여다보기 시작한다. 상하는 것은 세계가 아니라 그것을 연구하는 자이며, 쉰다고 해서 되돌아오지 않는다."},
        };
        for (String[] entry : research) {
            add("research." + Alchemia.MODID + "." + entry[0], pick(entry[1], entry[2]));
            add("research." + Alchemia.MODID + "." + entry[0] + ".page", pick(entry[3], entry[4]));
        }

        addEffect(ModEffects.FLUX_FLU, pick("Flux Flu", "플럭스 감기"));
        addEffect(ModEffects.FLUX_PHAGE, pick("Flux Phage", "플럭스 역병"));
        addEffect(ModEffects.UNNATURAL_HUNGER, pick("Unnatural Hunger", "부자연스러운 허기"));
        addEffect(ModEffects.SUN_SCORNED, pick("Sun Scorned", "햇빛 거부"));
        addEffect(ModEffects.BLURRED_VISION, pick("Blurred Vision", "흐릿한 시야"));
        addEffect(ModEffects.DEADLY_GAZE, pick("Deadly Gaze", "죽음의 응시"));
        addEffect(ModEffects.ALCHEDIARRHEA, pick("Alchediarrhea", "알케설사"));
        addEffect(ModEffects.WARP_WARD, pick("Warp Ward", "뒤틀림 방호"));

        // What warp murmurs before it does its work. Numbered to match the severity tiers.
        String[][] whispers = {
                {"You feel like you are being watched.", "누군가 지켜보고 있는 것 같다."},
                {"Something moves at the edge of your sight.", "시야 끝에서 무언가 움직인다."},
                {"For a moment, nothing is where you left it.", "잠깐, 모든 것이 제자리에 있지 않았다."},
                {"The air itself feels thin and wrong.", "공기가 얇고 잘못된 느낌이다."},
                {"Something is seeping out of you.", "무언가가 몸에서 배어 나온다."},
                {"A hunger takes you that food will not answer.", "음식으로는 달랠 수 없는 허기가 덮친다."},
                {"You hear your own name, spoken wrong.", "누군가 당신의 이름을 잘못 부른다."},
                {"The silence has a shape to it.", "침묵에 형태가 있다."},
                {"Your eyes will not settle on anything.", "눈이 어디에도 초점을 맞추지 못한다."},
                {"The sun looks at you and does not approve.", "태양이 당신을 못마땅하게 내려다본다."},
                {"Darkness closes without waiting for night.", "밤을 기다리지 않고 어둠이 닫힌다."},
                {"What ails you is looking for company.", "당신을 괴롭히는 것이 동행을 찾고 있다."},
                {"You are no longer entirely alone in here.", "이 안에 당신만 있는 것이 아니다."},
                {"Whatever you look at, looks back.", "무엇을 보든, 그것도 당신을 본다."},
                {"It has stopped being patient.", "그것이 더는 참지 않는다."},
        };
        for (int line = 0; line < whispers.length; line++) {
            add("warp." + Alchemia.MODID + ".text." + line, pick(whispers[line][0], whispers[line][1]));
        }

        Map<StoneSet, String[]> stoneNames = Map.of(
                ModBlocks.ARCANE_STONE, new String[] {"Arcane Stone", "신비한 돌"},
                ModBlocks.ARCANE_STONE_BRICKS, new String[] {"Arcane Stone Bricks", "신비한 돌 벽돌"});
        stoneNames.forEach((set, names) -> {
            addBlock(set.block(), pick(names[0], names[1]));
            addBlock(set.stairs(), pick(names[0] + " Stairs", names[1] + " 계단"));
            addBlock(set.slab(), pick(names[0] + " Slab", names[1] + " 반 블록"));
        });
        addBlock(ModBlocks.AMBER_BLOCK, pick("Block of Amber", "호박 블록"));
        addBlock(ModBlocks.AMBER_BRICKS, pick("Amber Bricks", "호박 벽돌"));


        // The latin name is the aspect's name in every language; the gloss below it says what it stands for
        String[][] aspects = {
                {"aer", "Air", "공기"},
                {"terra", "Earth", "땅"},
                {"ignis", "Fire", "불"},
                {"aqua", "Water", "물"},
                {"ordo", "Order", "질서"},
                {"perditio", "Entropy", "엔트로피"},
                {"vacuos", "Void, Emptiness", "공허"},
                {"lux", "Light", "빛"},
                {"motus", "Motion", "움직임"},
                {"gelum", "Cold, Ice", "냉기"},
                {"vitreus", "Crystal, Glass", "결정"},
                {"metallum", "Metal", "금속"},
                {"victus", "Life", "생명"},
                {"mortuus", "Death", "죽음"},
                {"potentia", "Energy, Power", "에너지"},
                {"permutatio", "Exchange", "교환"},
                {"auram", "Aura", "오라"},
                {"vitium", "Taint, Corruption", "오염"},
                {"tenebrae", "Darkness", "어둠"},
                {"alienis", "The Alien, The Strange", "이계"},
                {"volatus", "Flight", "비행"},
                {"herba", "Plant", "식물"},
                {"instrumentum", "Tool", "도구"},
                {"fabrico", "Craft", "제작"},
                {"machina", "Machine", "기계"},
                {"vinculum", "Trap, Binding", "속박"},
                {"spiritus", "Soul", "영혼"},
                {"cognitio", "Mind", "정신"},
                {"sensus", "Senses", "감각"},
                {"aversio", "Aversion, Conflict", "반감"},
                {"praemunio", "Protection", "보호"},
                {"desiderium", "Desire", "욕망"},
                {"exanimis", "Undeath", "언데드"},
                {"bestia", "Beast", "야수"},
                {"humanus", "Man", "인간"}
        };
        for (String[] aspect : aspects) {
            String key = "aspect." + Alchemia.MODID + "." + aspect[0];
            add(key, aspect[0].substring(0, 1).toUpperCase(Locale.ROOT) + aspect[0].substring(1));
            add(key + ".description", pick(aspect[1], aspect[2]));
        }

        addBlock(ModBlocks.SHIMMERLEAF, pick("Shimmerleaf", "반짝잎"));
        addBlock(ModBlocks.CINDERPEARL, pick("Cinderpearl", "재진주"));
        addBlock(ModBlocks.VISHROOM, pick("Vishroom", "비스버섯"));

        Map<WoodSet, String[]> woodNames = Map.of(
                ModBlocks.GREATWOOD, new String[] {"Greatwood", "거대나무"},
                ModBlocks.SILVERWOOD, new String[] {"Silverwood", "은빛나무"});
        woodNames.forEach((wood, names) -> {
            addBlock(wood.log(), pick(names[0] + " Log", names[1] + " 원목"));
            addBlock(wood.planks(), pick(names[0] + " Planks", names[1] + " 판자"));
            addBlock(wood.leaves(), pick(names[0] + " Leaves", names[1] + " 잎"));
            addBlock(wood.sapling(), pick(names[0] + " Sapling", names[1] + " 묘목"));
            addBlock(wood.stairs(), pick(names[0] + " Stairs", names[1] + " 계단"));
            addBlock(wood.slab(), pick(names[0] + " Slab", names[1] + " 반 블록"));
        });

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
