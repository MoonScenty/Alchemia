package me.moonscenty.alchemia.datagen;

import java.util.Locale;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Every translation is declared for both languages side by side so neither can fall behind.
 */
public abstract class ModLanguageProvider extends LanguageProvider {
    protected ModLanguageProvider(PackOutput output, String locale) {
        super(output, Alchemia.MODID, locale);
    }

    /** What each dye is called, for the sixteen flames and anything else that comes in a set of them. */
    private static final Map<DyeColor, String[]> DYE_NAMES = Map.ofEntries(
            Map.entry(DyeColor.WHITE, new String[]{"White", "하얀"}),
            Map.entry(DyeColor.ORANGE, new String[]{"Orange", "주황"}),
            Map.entry(DyeColor.MAGENTA, new String[]{"Magenta", "자홍"}),
            Map.entry(DyeColor.LIGHT_BLUE, new String[]{"Light Blue", "하늘"}),
            Map.entry(DyeColor.YELLOW, new String[]{"Yellow", "노란"}),
            Map.entry(DyeColor.LIME, new String[]{"Lime", "연두"}),
            Map.entry(DyeColor.PINK, new String[]{"Pink", "분홍"}),
            Map.entry(DyeColor.GRAY, new String[]{"Gray", "회색"}),
            Map.entry(DyeColor.LIGHT_GRAY, new String[]{"Light Gray", "밝은 회색"}),
            Map.entry(DyeColor.CYAN, new String[]{"Cyan", "청록"}),
            Map.entry(DyeColor.PURPLE, new String[]{"Purple", "보라"}),
            Map.entry(DyeColor.BLUE, new String[]{"Blue", "파란"}),
            Map.entry(DyeColor.BROWN, new String[]{"Brown", "갈색"}),
            Map.entry(DyeColor.GREEN, new String[]{"Green", "초록"}),
            Map.entry(DyeColor.RED, new String[]{"Red", "빨간"}),
            Map.entry(DyeColor.BLACK, new String[]{"Black", "검은"}));

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
        addItem(ModItems.ALUMENTUM, pick("Alumentum", "알루멘툼"));
        // not "Aer Crystal": that is the crystal growing on a node, and two things under one name is one too many
        addItem(ModItems.CRYSTALLIZED_ESSENCE, pick("Crystallized Essence", "결정화된 에센시아"));
        addMetalGear();
        add("item.alchemia.crystallized_essence.of", pick("Crystallized %s", "결정화된 %s"));
        add("item.alchemia.crystallized_essence.holding", pick("One point of %s", "%s 한 점"));
        addInfusionEnchantments();
        addBlock(ModBlocks.INFUSION_SPEED_STONE, pick("Infusion Speed Stone", "주입 속도석"));
        addBlock(ModBlocks.INFUSION_COST_STONE, pick("Infusion Cost Stone", "주입 절약석"));
        // one flame to a dye, and the dye's own name in front of it -- white is simply "nitor"
        ModBlocks.NITOR.forEach((colour, flame) -> addBlock(flame,
                colour == DyeColor.WHITE ? pick("Nitor", "니토르")
                        : pick(DYE_NAMES.get(colour)[0] + " Nitor", DYE_NAMES.get(colour)[1] + " 니토르")));
        addItem(ModItems.VOID_SEED, pick("Void Seed", "공허 씨앗"));
        addItem(ModItems.VOID_INGOT, pick("Void Ingot", "공허 주괴"));
        addItem(ModItems.VOID_NUGGET, pick("Void Nugget", "공허 조각"));



        addItem(ModItems.ALCHEMOMETER, pick("Alchemometer", "알케모미터"));
        addItem(ModItems.NODE_PLACER, pick("Node Placer", "노드 배치기"));
        add("item.alchemia.creative_only", pick("Creative only", "크리에이티브 전용"));
        addBlock(ModBlocks.RESEARCH_TABLE, pick("Research Table", "연구 탁자"));
        addBlock(ModBlocks.CRUCIBLE, pick("Crucible", "도가니"));
        addBlock(ModBlocks.ESSENTIA_SMELTER, pick("Essentia Smelter", "에센시아 제련로"));
        addBlock(ModBlocks.ALEMBIC, pick("Alembic", "증류기"));
        addBlock(ModBlocks.TUBE, pick("Essentia Tube", "에센시아 관"));
        addBlock(ModBlocks.TUBE_VALVE, pick("Essentia Valve", "에센시아 밸브"));
        addBlock(ModBlocks.TUBE_ONEWAY, pick("One-way Essentia Tube", "역류방지 에센시아 관"));
        addBlock(ModBlocks.TUBE_RESTRICT, pick("Restricted Essentia Tube", "제한 에센시아 관"));
        addBlock(ModBlocks.TUBE_FILTER, pick("Essentia Filter Tube", "여과 에센시아 관"));
        addBlock(ModBlocks.TUBE_BUFFER, pick("Essentia Buffer", "에센시아 완충기"));
        add("block.alchemia.tube_filter.open", pick("Lets anything by", "아무것이나 지나간다"));
        add("block.alchemia.tube_filter.only", pick("Lets only %s by", "%s만 지나간다"));
        add("block.alchemia.tube_buffer.empty", pick("Empty", "비어 있다"));
        add("block.alchemia.tube_buffer.holding", pick("%s — %s / %s", "%s — %s / %s"));
        addBlock(ModBlocks.JAR, pick("Warded Jar", "봉인된 단지"));
        addBlock(ModBlocks.ARCANE_PEDESTAL, pick("Arcane Pedestal", "비전 받침대"));
        addBlock(ModBlocks.INFUSION_MATRIX, pick("Runic Matrix", "룬 결계"));
        addBlock(ModBlocks.ARCANE_PILLAR, pick("Arcane Pillar", "비전 기둥"));
        add("block.alchemia.infusion_matrix.asleep", pick("The stones hang still", "돌이 가만히 떠 있다"));
        add("block.alchemia.infusion_matrix.steady", pick("Awake, and evenly laid out", "깨어 있고, 고르게 놓였다"));
        add("block.alchemia.infusion_matrix.lopsided", pick("Awake, but the ring pulls one way (%s)", "깨어 있지만 고리가 한쪽으로 쏠렸다 (%s)"));
        add("block.alchemia.infusion_matrix.steadied", pick("Awake, and held steady (%s)", "깨어 있고, 단단히 붙들려 있다 (%s)"));
        add("block.alchemia.infusion_matrix.woken", pick("The altar wakes", "제단이 깨어난다"));
        add("block.alchemia.infusion_matrix.unbuilt", pick("The altar is not finished", "제단이 아직 완성되지 않았다"));
        add("block.alchemia.infusion_matrix.busy", pick("Already working", "이미 일하고 있다"));
        add("block.alchemia.infusion_matrix.nothing", pick("What is laid out here makes nothing", "여기 놓인 것으로는 아무것도 되지 않는다"));
        add("block.alchemia.infusion_matrix.gathering", pick("Drawing in what it needs", "필요한 것을 끌어당기고 있다"));
        add("block.alchemia.infusion_matrix.owed", pick("Still thirsty for %s", "아직 %s을(를) 원한다"));
        addItem(ModItems.FILTER, pick("Filter", "여과망"));
        addItem(ModItems.PHIAL, pick("Phial", "유리병"));
        add("item.alchemia.phial.filled", pick("Phial of %s", "%s 유리병"));
        add("item.alchemia.phial.holding", pick("%s — %s points", "%s — %s점"));
        addItem(ModItems.JAR_LABEL, pick("Jar Label", "단지 라벨"));
        add("item.alchemia.jar_label.written", pick("Label: %s", "%s 라벨"));
        add("item.alchemia.jar_label.blank", pick("Nothing written on it yet", "아직 아무것도 적혀 있지 않다"));
        addItem(ModItems.JAR_BRACE, pick("Jar Brace", "단지 고정대"));
        add("block.alchemia.jar.empty", pick("Empty", "비어 있다"));
        add("item.alchemia.jar.labelled", pick("Labelled: %s", "라벨: %s"));
        add("block.alchemia.jar.holding", pick("%s — %s of %s", "%s — %s / %s"));
        add("block.alchemia.alembic.empty", pick("Empty", "비어 있다"));
        add("gui.alchemia.essentia_held", pick("Essentia  %s / %s", "에센시아  %s / %s"));
        add("gui.alchemia.essentia_empty", pick("Nothing in it yet", "아직 아무것도 없다"));
        add("block.alchemia.alembic.holding", pick("%s — %s of %s", "%s — %s / %s"));
        addBlock(ModBlocks.ARCANE_WORKBENCH, pick("Arcane Workbench", "비전 작업대"));
        addBlock(ModBlocks.ARCANE_WORKBENCH_CHARGER, pick("Arcane Workbench Charger", "비전 작업대 충전기"));

        // the wand and the pieces it is put together from
        add("item.alchemia.wand", pick("Wand", "완드"));
        add("item.alchemia.wand.named", pick("%s Capped %s Wand", "%s 씌운 %s 완드"));
        add("item.alchemia.wand.vis", pick("%s %s / %s", "%s %s / %s"));
        add("item.alchemia.wand.charge", pick("Draws %s faster", "%s만큼 빨리 채워짐"));
        add("item.alchemia.wand.discount", pick("Spends %s%% more", "%s%% 더 씀"));
        addWandRod("wood", "Wooden", "나무");
        addWandRod("greatwood", "Greatwood", "거대나무");
        addWandRod("silverwood", "Silverwood", "은빛나무");
        addWandRod("reed", "Reed", "갈대");
        addWandRod("obsidian", "Obsidian", "흑요석");
        addWandRod("blaze", "Blaze", "블레이즈");
        addWandRod("ice", "Ice", "얼음");
        addWandRod("quartz", "Quartz", "석영");
        addWandRod("bone", "Bone", "뼈");
        addWandCap("iron", "Iron", "철");
        addWandCap("gold", "Gold", "금");
        addWandCap("brass", "Brass", "황동");
        addWandCap("alchemium", "Alchemium", "알케미움");
        addWandCap("void", "Void", "공허");
        add("gui.alchemia.vis_cost", pick("%s of %s", "%s / %s"));
        add("gui.alchemia.no_wand", pick("A wand must be laid on the bench", "작업대에 완드를 올려야 한다"));
        add("gui.alchemia.not_enough_vis", pick("The wand does not hold enough", "완드에 든 것이 모자라다"));
        add("gui.alchemia.not_researched", pick("You do not know how this is made", "만드는 법을 아직 모른다"));
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
        add("research.alchemia.arcane_crafting", pick("At the arcane workbench", "비전 작업대에서"));
        add("research.alchemia.in_crucible", pick("In the crucible", "도가니에서"));
        add("research.alchemia.in_matrix", pick("At the matrix", "룬 결계에서"));
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
                {"wand", "Wands", "완드",
                 "A rod with a cap at each end, and nothing else. The caps let vis in and out; the rod decides how much of it will sit still. Held in a hand it fills itself from the air, slowly, wherever the aura has not been worked out.",
                 "양 끝에 캡을 씌운 막대, 그게 전부다. 캡은 비스가 드나드는 문이고, 막대는 얼마나 머무를지를 정한다. 손에 들고 있으면 오라가 마르지 않은 곳에서 천천히 스스로 찬다."},
                {"arcane_workbench", "Arcane Workbench", "비전 작업대",
                 "A bench with a cloth laid over it and a place to set a wand down. What is shaped here costs vis as well as materials, and the vis comes out of the wand — so the bench is only ever as good as what is lying on it.",
                 "천을 덮고 완드를 놓을 자리를 낸 작업대. 여기서 만드는 것은 재료 말고도 비스를 먹으며, 그 비스는 완드에서 나온다. 그러니 작업대는 그 위에 놓인 것만큼만 쓸모가 있다."},
                {"charger", "Charger", "충전기",
                 "Four posts and a crystal, standing over a workbench. It fills the wand left below it and does nothing else — no faster than a hand would, but a hand has other work to do.",
                 "작업대 위에 세우는 기둥 넷과 결정 하나. 아래 놓인 완드를 채우는 것 말고는 아무것도 하지 않는다. 손에 쥐는 것보다 빠르지도 않지만, 손은 다른 일을 해야 한다."},
                {"wand_cap_gold", "Gold Caps", "금 캡",
                 "Iron grips vis badly and lets a tenth of every working slip. Gold does not. It is softer and dearer and worth both, for the first cap that does not charge for the privilege.",
                 "철은 비스를 어설프게 붙들어 일할 때마다 열에 하나를 흘린다. 금은 그러지 않는다. 무르고 비싸지만 둘 다 값한다. 값을 더 받지 않는 첫 캡이다."},
                {"wand_cap_brass", "Brass Caps", "황동 캡",
                 "An alloy that pulls harder at the air than either metal in it. It spends no better than gold, but it fills faster, which over a long afternoon is the same thing.",
                 "섞인 어느 금속보다도 공기를 세게 잡아당기는 합금. 쓰는 값은 금과 다르지 않지만 차는 속도가 빠르다. 긴 오후 동안에는 결국 같은 이야기다."},
                {"wand_cap_alchemium", "Alchemium Caps", "알케미움 캡",
                 "Metal that has been taught what it is for. A tenth of every working comes back, which is the first time a cap has given anything rather than taken it.",
                 "무엇에 쓰이는지를 배운 금속. 일할 때마다 열에 하나가 되돌아온다. 캡이 가져가는 대신 내어준 것은 이번이 처음이다."},
                {"wand_rod_greatwood", "Greatwood Rods", "거대나무 막대",
                 "Cut from a tree that grew slowly and held on to it. A greatwood rod carries two and a half times what a stick will, which is the difference between working and stopping to wait.",
                 "느리게 자라며 그것을 품어온 나무에서 잘라낸다. 거대나무 막대는 막대기의 두 배 반을 담는다. 일을 계속하느냐 기다리느냐의 차이다."},
                {"arcane_stone", "Arcane Stone", "비전 석재",
                 "Common stone with a shard worked into it, and it stops being common. It takes the aura the way a wick takes oil, which is why every worthwhile structure in this craft is built of it.",
                 "흔한 돌에 조각을 섞어 넣으면 더는 흔하지 않게 된다. 심지가 기름을 빨아들이듯 오라를 머금는다. 이 기예에서 값나가는 구조물이 하나같이 이것으로 지어지는 이유다."},
                {"crucible", "Crucible", "도가니",
                 "A pot of water kept over a fire. Anything dropped into it comes apart into what it is made of, and once the right things are floating in the water, one last thing thrown in boils the lot into something new.",
                 "불 위에 올려 둔 물 냄비. 떨어뜨린 것은 무엇이든 제 본바탕으로 풀어지고, 물에 알맞은 것들이 떠 있게 되면 마지막 하나를 던져 넣어 전부를 새것으로 끓여낸다."},
                {"metallurgy", "Metallurgy", "야금",
                 "Iron does not want to be anything else, and a crucible is how you argue with it. Steeped in earth and order it comes out alchemium; in energy and water, brass.",
                 "철은 다른 것이 되기를 원하지 않으며, 도가니는 그와 다투는 방법이다. 땅과 질서에 담그면 알케미움으로, 힘과 물에 담그면 황동으로 나온다."},
                {"distillation", "Distillation", "증류",
                 "Burning a thing is a crude way to ask what it is made of, but it is an honest one. A smelter sends the answer up as smoke, and alembics stacked above it catch that smoke one aspect at a time.",
                 "무엇으로 이루어졌는지 묻는 데에 태우는 것은 거칠지만 정직한 방법이다. 제련로가 그 답을 연기로 올려 보내고, 위에 쌓아 올린 증류기가 연기를 상 하나씩 받아 낸다."},
                {"jar_label", "Jars and Labels", "단지와 라벨",
                 "Essentia will not sit in the open. A warded jar holds sixty-four of one kind and refuses the rest; a label tells it which kind before there is any, so a shelf can be laid out empty and filled later. A brace lets a jar be filled and never emptied.",
                 "에센시아는 열린 데에 머물지 않는다. 봉인된 단지는 한 가지를 예순넷까지 담고 나머지는 받지 않는다. 라벨은 아직 아무것도 없을 때 무엇을 담을지 미리 일러 주니, 선반을 빈 채로 먼저 늘어놓고 나중에 채울 수 있다. 고정대를 물리면 채우기만 되고 비울 수는 없다."},
                {"tubes", "Essentia Tubes", "에센시아 관",
                 "Brass pipe with glass in the middle of it. The far end does the pulling rather than the near one, so a run fills whatever still has room instead of whatever it reached first. A valve on the line shuts it, by hand or by redstone.",
                 "가운데에 유리를 끼운 황동 관. 당기는 쪽은 보내는 끝이 아니라 받는 끝이다. 그래서 한 줄기는 먼저 닿은 곳이 아니라 아직 자리가 남은 곳을 채운다. 줄기에 밸브를 물리면 손으로도 레드스톤으로도 막을 수 있다."},
                {"tube_filter", "Sorting the Flow", "흐름 가려 쓰기",
                 "Four ways of telling a length of pipe what to do with what passes through it: let one thing by and turn the rest back, let it by one way only, let it by half as fast, or keep a little in hand for when the works wants it.",
                 "관에게 지나가는 것을 어찌하라 이를 방법 넷. 하나만 지나가게 하고 나머지는 돌려보내거나, 한쪽으로만 내보내거나, 절반만 흘리거나, 공방이 찾을 때를 대비해 조금 쥐고 있게 하거나."},
                {"void_metal", "Void Metal", "공허 금속",
                 "A seed steeped in darkness stops being a seed, and what it becomes, steeped again in metal, sets as an ingot. It will not set at all without a point of flux in the water: the metal wants something wrong in it. What is drawn from it gives back a third of every working, and the caps drawn from it are the last a wand will ever want.",
                 "어둠에 담근 씨앗은 씨앗이기를 그만두고, 그렇게 된 것을 다시 금속에 담그면 주괴로 굳는다. 물에 플럭스가 한 점 없으면 아예 굳지 않는다. 이 금속은 제 안에 잘못된 것을 원한다. 여기서 뽑아낸 캡은 일할 때마다 셋에 하나를 돌려주며, 완드가 바랄 마지막 캡이다."},
                {"alumentum", "Alumentum", "알루멘툼",
                 "Coal that has been round again. What comes out of the water holds four times the fire it went in with, and in an essentia smelter it does one thing more: the work goes a fifth quicker while it lasts. A slow furnace is the price of a smelter, and this is the one thing that can be spent against it.",
                 "한 번 더 돌린 석탄이다. 물에서 나온 것은 들어갈 때의 네 배를 품고 있고, 에센시아 제련로에서는 한 가지를 더 한다 — 타는 동안 일이 5분의 1만큼 빨라진다. 느린 것이 제련로의 값인데, 그 값에 맞설 수 있는 것이 이것 하나다."},
                {"nitor", "Nitor", "니토르",
                 "Light with nothing burning under it. It never goes out and never wants feeding, which is worth the glowstone it costs, and it hangs wherever it is put -- floor, wall, ceiling, open air. A flame that needed something to stand on would be a torch. A dye put to one changes its colour and nothing else; there are sixteen and none is the original.",
                 "밑에서 타는 것이 없는 불빛이다. 꺼지지 않고 먹일 것도 없으니 발광석 값을 한다. 바닥이든 벽이든 천장이든 허공이든 놓은 자리에 그대로 걸린다. 받칠 것이 있어야 하는 불꽃은 횃불이다. 염료를 대면 색만 바뀐다. 열여섯 가지가 있고 그중 원래 것은 없다."},
                {"infusion_boost", "Infusion Stones", "주입석",
                 "Two stones, laid a course below the altar's corners, and an altar takes whatever is under them without being asked. One hurries a turn along and charges a little more for it; the other waits longer and charges less. Four of a kind is the whole of either -- twelve ticks to a turn, or eight parts in a hundred off the bill -- and nothing takes a working below half price. They can be mixed, which is the only reason there are two of them rather than one with a switch.",
                 "제단 귀퉁이보다 한 켜 아래에 까는 돌 둘이다. 제단은 그 밑에 무엇이 깔렸든 묻지 않고 그대로 받는다. 하나는 한 바퀴를 재촉하는 대신 값을 조금 더 받고, 다른 하나는 더 기다리는 대신 덜 받는다. 넷을 같은 것으로 깔면 그것이 한계다 — 한 바퀴 열두 틱, 또는 값에서 백분의 팔. 무엇을 깔아도 반값 아래로는 내려가지 않는다. 섞어 깔 수도 있으니, 돌이 하나가 아니라 둘인 이유가 그것이다."},
                {"alchemium_gear", "Alchemium Gear", "알케미움 장비",
                 "The metal is worth forging with. A tool of it cuts about as fast as diamond, lasts twice what iron does, and takes an enchantment better than anything that can be dug up. A suit of it stops what iron stops and outlasts diamond by half again. Nothing about the shapes is new -- the sticks go where they always went.",
                 "이 금속은 벼려 볼 값어치가 있다. 도구는 다이아몬드만큼 빠르게 깎고 철의 두 배를 가며, 캐낼 수 있는 어떤 것보다 마법을 잘 받는다. 갑옷은 철이 막는 만큼 막으면서 다이아몬드보다 절반 더 간다. 모양은 새로울 것이 없다 — 막대는 늘 있던 자리에 있다."},
                {"void_gear", "Void Gear", "공허 장비",
                 "The other sort of good. It cuts faster and hits harder than anything, reaches what netherite reaches, and wears out in a hundred and fifty swings. A suit of it stops nearly what diamond stops and goes to pieces faster than leather. It is not the better metal; it is the metal for the one job that has to be done now.",
                 "다른 종류의 좋음이다. 무엇보다 빠르게 깎고 세게 치며 네더라이트가 닿는 데까지 닿는데, 백쉰 번을 휘두르면 닳아 없어진다. 갑옷은 다이아몬드에 가깝게 막으면서 가죽보다 빨리 부서진다. 더 나은 금속이 아니라, 지금 당장 해야 하는 한 가지 일을 위한 금속이다."},
                {"infusion_enchantment", "Infusion Enchantment", "주입 마법부여",
                 "An altar will put on a tool what no table will sell. There is no gambling and no book to keep it in: the tool goes under the matrix, the price is paid in essentia, and it comes back with one more thing about it. Collector sends what is broken to the one who broke it. Destructive takes the eight blocks round the one struck, where the tool would have served for them anyway. Burrowing brings a seam or a trunk apart from its far end. Sounding taps the stone and shows what is behind it for a moment, at five swings' wear. Arcing carries a blow to whatever is standing beside what was struck. Essence makes what is killed give up a little of what it was made of -- the one way to essentia that wants no smelter, and so the meanest of them. Crouch and the digging ones go quiet.",
                 "작업대가 팔지 않는 것을 제단은 도구에 얹는다. 운에 맡길 것도, 담아 둘 책도 없다. 도구를 결계 아래에 두고 에센시아로 값을 치르면 한 가지를 더 지니고 돌아온다. 수집은 부순 것을 부순 이에게 보낸다. 파괴는 때린 칸 둘레 여덟을 같이 가져가되, 그 도구로 캘 수 있는 것만 가져간다. 굴착은 광맥이나 줄기를 먼 끝에서부터 허문다. 탐지는 돌을 두드려 돌 너머를 잠시 보여 주고, 그 대가로 도구가 다섯 번 닳는다. 전이는 때린 것 옆에 선 것에도 같은 매를 옮긴다. 정수는 죽인 것이 제 만들어진 바를 조금 내놓게 한다 -- 제련로 없이 에센시아를 얻는 유일한 길이고, 그래서 한 번에 주는 것이 적다. 웅크리면 캐는 것들은 조용해진다."},
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

    /** A rod, and the cap item made of the same metal, both named after the stuff they are made of. */
    private void addWandRod(String rod, String english, String korean) {
        add("wand_rod.alchemia." + rod, pick(english, korean));
        if (!rod.equals("wood")) {
            add("item.alchemia.wand_rod_" + rod, pick(english + " Wand Rod", korean + " 완드 막대"));
        }
    }

    /** The ten tools and eight pieces our two metals are forged into. */
    private void addMetalGear() {
        String[][] metals = {{"alchemium", "Alchemium", "알케미움"}, {"void", "Void", "공허"}};
        String[][] shapes = {
                {"pickaxe", "Pickaxe", "곡괭이"}, {"axe", "Axe", "도끼"}, {"shovel", "Shovel", "삽"},
                {"sword", "Sword", "검"}, {"hoe", "Hoe", "괭이"},
                {"helmet", "Helmet", "투구"}, {"chestplate", "Chestplate", "흉갑"},
                {"leggings", "Leggings", "각반"}, {"boots", "Boots", "장화"},
        };
        for (String[] metal : metals) {
            for (String[] shape : shapes) {
                add("item.alchemia." + metal[0] + "_" + shape[0],
                        pick(metal[1] + " " + shape[1], metal[2] + " " + shape[2]));
            }
        }
    }

    /** What an altar puts on a tool, written under the tool's name. */
    private void addInfusionEnchantments() {
        add(InfusionEnchantment.COLLECTOR.key(), pick("Collector", "수집"));
        add(InfusionEnchantment.DESTRUCTIVE.key(), pick("Destructive", "파괴"));
        add(InfusionEnchantment.BURROWING.key(), pick("Burrowing", "굴착"));
        add(InfusionEnchantment.SOUNDING.key(), pick("Sounding", "탐지"));
        add(InfusionEnchantment.ARCING.key(), pick("Arcing", "전이"));
        add(InfusionEnchantment.ESSENCE.key(), pick("Essence", "정수"));
    }

    private void addWandCap(String cap, String english, String korean) {
        add("wand_cap.alchemia." + cap, pick(english, korean));
        add("item.alchemia.wand_cap_" + cap, pick(english + " Wand Cap", korean + " 완드 캡"));
        // the two that are cast before they are finished have a name for the casting as well
        if (ModItems.INERT_CAPS.containsKey(cap)) {
            add("item.alchemia.wand_cap_" + cap + "_inert",
                    pick("Inert " + english + " Wand Cap", "불활성 " + korean + " 완드 캡"));
        }
    }
}
