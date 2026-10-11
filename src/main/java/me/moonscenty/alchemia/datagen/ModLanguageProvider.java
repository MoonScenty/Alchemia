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

    /**
     * The twelve foci and the two lines of price under each of them.
     * <p>
     * The names are the original's. Several of them say nothing about what the focus does -- a pech's curse is
     * named after what taught it rather than what it inflicts -- and renaming them for clarity would be renaming
     * somebody else's spells.
     */
    private void foci() {
        add("item.alchemia.focus.cost", pick("Vis Cost:", "비스 값:"));
        add("item.alchemia.focus.cost_per_tick", pick("Vis Cost (per tick):", "비스 값 (틱당):"));
        add("item.alchemia.focus.cost_line", "  %s x %s");
        add("item.alchemia.focus.copying", pick("Copying: %s", "복사 중: %s"));
        addBlock(ModBlocks.HOLE, pick("Hole", "구멍"));
        addItem(ModItems.FOCUS_POUCH, pick("Focus Pouch", "포커스 주머니"));
        addItem(ModItems.ENCHANTED_FABRIC, pick("Enchanted Fabric", "마법 천"));
        robes();
        add("item.alchemia.focus_pouch.holding", pick("Holding %s of %s", "%s / %s 들었음"));
        add("item.alchemia.focus.upgrade_level", "%s %s");
        add("item.alchemia.focus.reach", pick("Reaching %s", "범위 %s"));
        upgrades();
        autocaster();

        String[][] foci = {
                {"fire", "Fire", "불꽃"},
                {"frost", "Frost", "서리"},
                {"shock", "Shock", "충격"},
                {"excavation", "Excavation", "발굴"},
                {"grapple", "Grappler", "갈고리"},
                {"hellbat", "Nine Hells", "아홉 지옥"},
                {"pech", "Pech's Curse", "펙의 저주"},
                {"hole", "Portable Hole", "이동식 구멍"},
                {"primal", "Primal", "원시"},
                {"shard", "Vis Shard", "비스 조각"},
                {"trade", "Equal Trade", "등가교환"},
                {"builder", "Builder", "건설"},
        };
        for (String[] focus : foci) {
            add("item.alchemia.focus_" + focus[0],
                    pick("Wand Focus: " + focus[1], "완드 포커스: " + focus[2]));
        }
    }

    /**
     * The bench a focus is worked on, and the twenty-three things it can work in.
     * <p>
     * Each gets a name and a line saying what it actually does, since the pictures on the bench are the only
     * place they are ever seen and a picture of a snowflake says nothing about how much colder it is.
     */
    private void upgrades() {
        addBlock(ModBlocks.FOCAL_MANIPULATOR, pick("Focal Manipulator", "포커스 조작기"));

        String[][] upgrades = {
                {"potency", "Potency", "강화",
                 "Harder. About a fifth more per rank, though each focus counts that its own way.",
                 "더 세진다. 등급마다 대략 오분의 일씩이고, 그것을 어떻게 세는지는 포커스마다 다르다."},
                {"frugal", "Frugal", "절약",
                 "A tenth off the bill per rank. It pays for this focus's own working and nothing else.",
                 "등급마다 값의 십분의 일을 깎는다. 이 포커스가 제 일을 할 때만이고, 작업대에서 쓰는 값은 그대로다."},
                {"treasure", "Treasure", "보물",
                 "More out of what is broken, the way fortune and looting give more.",
                 "부순 것에서 더 많이 나온다. 행운과 약탈이 하는 일과 같다."},
                {"enlarge", "Enlarge", "확대",
                 "Wider, or more of them at once, depending on what the focus does to begin with.",
                 "더 넓게, 또는 한 번에 더 많이. 어느 쪽인지는 그 포커스가 본래 하는 일에 달렸다."},
                {"extend", "Extend", "연장",
                 "Whatever the focus leaves behind on what it touches stays longer.",
                 "포커스가 닿은 것에 남기고 가는 것이 더 오래간다."},
                {"silk_touch", "Silk Touch", "섬세한 손길",
                 "The block comes up as it stood rather than as what it breaks into. It is not cheap.",
                 "블록이 부서진 모습이 아니라 선 모습 그대로 나온다. 싸지 않다."},
                {"architect", "Architect", "설계",
                 "The square the focus will work on is shown and can be set, rather than guessed at.",
                 "포커스가 건드릴 자리를 짐작하는 대신 눈으로 보고 정한다."},
                {"alchemists_fire", "Alchemist's Fire", "연금술사의 불",
                 "Whatever it sets alight burns far longer.",
                 "불이 붙은 것이 훨씬 오래 탄다."},
                {"alchemists_frost", "Alchemist's Frost", "연금술사의 서리",
                 "Whatever it touches is chilled as well as hurt, and walks away slowly.",
                 "닿은 것은 다치는 데 더해 얼어붙어 느리게 걷는다."},
                {"fireball", "Fireball", "화염구",
                 "A ball to throw instead of a spray to hold down. It costs eight times as much and waits a second.",
                 "누르고 있는 분사 대신 던지는 공이 된다. 값은 여덟 배고 한 번 쏘면 일 초를 기다린다."},
                {"firebeam", "Fire Beam", "화염 광선",
                 "The spray drawn into a line. Narrower, hotter, and it burns through what stands in it.",
                 "퍼지던 불이 한 줄로 모인다. 좁고 뜨겁고, 선 것을 뚫고 지나간다."},
                {"scattershot", "Scattershot", "산탄",
                 "A volley of splinters instead of one spike. Each is little; together they are not.",
                 "창 하나 대신 조각 여럿이 날아간다. 하나하나는 약하고 한꺼번에는 그렇지 않다."},
                {"ice_boulder", "Ice Boulder", "얼음 바위",
                 "One boulder, which bounces. Where it ends up is partly the thrower's skill.",
                 "바위 하나가 튀며 굴러간다. 어디까지 가는지는 던진 사람 솜씨에 달렸다."},
                {"chain_lightning", "Chain Lightning", "연쇄 번개",
                 "Slower and harder, and the bolt jumps on to whatever is standing near what it struck.",
                 "느려지고 세지며, 맞은 것 옆에 선 것으로 번개가 옮겨붙는다."},
                {"earth_shock", "Earth Shock", "대지 충격",
                 "A ball lobbed at the ground instead of a bolt, which leaves the ground live for a while.",
                 "번개 대신 땅으로 공을 던진다. 떨어진 자리의 땅이 한동안 감전된 채로 남는다."},
                {"bat_bombs", "Bat Bombs", "폭탄 박쥐",
                 "The bats always burst, and burst harder.",
                 "박쥐가 반드시 터지고, 더 세게 터진다."},
                {"devil_bats", "Devil Bats", "악마 박쥐",
                 "Bigger bats that never burst and keep at their mark until one of them is done.",
                 "더 크고 터지지 않는 박쥐다. 둘 중 하나가 끝날 때까지 물고 늘어진다."},
                {"vampire_bats", "Vampire Bats", "흡혈 박쥐",
                 "The bats drink what they bite, and hand some of it back to whoever called them.",
                 "문 것을 마시고, 그 일부를 부른 사람에게 돌려준다."},
                {"nightshade", "Nightshade", "가지독",
                 "All three curses at once rather than whichever one the burst happens to pick.",
                 "터질 때 하나를 고르는 대신 셋을 한꺼번에 건다."},
                {"seeker", "Seeker", "추적",
                 "The orb goes straight at the nearest living thing instead of wandering, and never goes wrong.",
                 "구슬이 헤매지 않고 가장 가까운 산 것에게 곧장 간다. 잘못되는 일도 없다."},
                {"persistent", "Persistent", "집요",
                 "When its mark is gone the splinter finds another of the same kind rather than falling.",
                 "노리던 것이 사라지면 떨어지는 대신 같은 종류를 하나 더 찾아간다."},
                {"dowsing", "Dowsing", "탐광",
                 "Ore sometimes comes up as a cluster, which smelts to twice as much.",
                 "광석이 이따금 덩이째 나온다. 제련하면 두 배가 된다."},
                {"sticky", "Sticky", "끈끈함",
                 "The hook does not let go on arrival. Letting go is the climber's business.",
                 "다 올라가도 갈고리가 놓지 않는다. 놓는 것은 오르는 사람이 할 일이다."},
        };
        for (String[] upgrade : upgrades) {
            add("focus_upgrade." + Alchemia.MODID + "." + upgrade[0], pick(upgrade[1], upgrade[2]));
            add("focus_upgrade." + Alchemia.MODID + "." + upgrade[0] + ".note", pick(upgrade[3], upgrade[4]));
        }
    }

    /** The turret, the four things it may be told to mind, and what it says when it is somebody else's. */
    private void autocaster() {
        addItem(ModItems.AUTOCASTER, pick("Autocaster", "자동 시전기"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.AUTOCASTER, pick("Autocaster", "자동 시전기"));
        add("item.alchemia.autocaster.note",
                pick("Fires a fitted focus on its own", "끼운 포커스를 혼자 쏜다"));
        add("entity.alchemia.autocaster.not_yours", pick("Not yours", "남의 것이다"));
        add("gui.alchemia.autocaster.range", pick("Range %s blocks", "사거리 %s칸"));
        add("gui.alchemia.autocaster.minds.0", pick("Target Animals", "동물을 노림"));
        add("gui.alchemia.autocaster.minds.1", pick("Target Mobs", "몹을 노림"));
        add("gui.alchemia.autocaster.minds.2", pick("Target Players", "플레이어를 노림"));
        add("gui.alchemia.autocaster.minds.3", pick("Target Friendly", "아군을 노림"));
    }

    /**
     * The two robes.
     * <p>
     * Neither is a whole suit: the cloth robe has no hood and the void robe has no boots, as the original had
     * them. The names are the original's too -- "cloth" rather than "robe" for the plain one, because that is
     * what it is made of and what it is called everywhere the recipes mention it.
     */
    private void robes() {
        String[][] pieces = {
                {"cloth_chest", "Robe", "로브"},
                {"cloth_legs", "Robe Leggings", "로브 바지"},
                {"cloth_boots", "Robe Boots", "로브 신"},
                {"void_robe_helm", "Void Robe Hood", "공허 로브 두건"},
                {"void_robe_chest", "Void Robe", "공허 로브"},
                {"void_robe_legs", "Void Robe Leggings", "공허 로브 바지"},
                {"fortress_helm", "Fortress Helm", "요새 투구"},
                {"fortress_chest", "Fortress Cuirass", "요새 흉갑"},
                {"fortress_legs", "Fortress Greaves", "요새 각반"},
        };
        for (String[] piece : pieces) {
            add("item.alchemia." + piece[0], pick(piece[1], piece[2]));
        }
        add("item.alchemia.vis_discount", pick("Wands spend %s%% less", "완드가 %s%% 덜 씀"));
        add("item.alchemia.fortress.fitted", pick("Worked in: %s", "박아 넣음: %s"));
        add("helm_fitting.alchemia.goggles", pick("Goggles of Revealing", "계시의 고글"));
        add("helm_fitting.alchemia.grinning_devil", pick("Mask of the Grinning Devil", "웃는 악귀의 가면"));
        add("helm_fitting.alchemia.angry_ghost", pick("Mask of the Angry Ghost", "성난 망령의 가면"));
        add("helm_fitting.alchemia.sipping_fiend", pick("Mask of the Sipping Fiend", "들이켜는 마귀의 가면"));
        add("item.alchemia.fortress.turns",
                pick("Each piece turns %s%% of a blow, %s%% of fire and blasts, %s%% of a working",
                        "한 점마다 타격 %s%%, 불과 폭발 %s%%, 주술 %s%%를 흘림"));
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
        add("pack.alchemia.legacy.title", pick("Original Thaumcraft Assets", "원본 Thaumcraft 에셋"));
        add("pack.alchemia.legacy.description",
                pick("Read from the jars in legacy/", "legacy/ 안의 jar에서 읽어 옴"));

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
        addItem(ModItems.TRAVELLER_BOOTS, pick("Traveller's Boots", "여행자의 장화"));
        addItem(ModItems.GOGGLES, pick("Goggles of Revealing", "계시의 고글"));
        add("item.alchemia.crystallized_essence.of", pick("Crystallized %s", "결정화된 %s"));
        add("item.alchemia.crystallized_essence.holding", pick("One point of %s", "%s 한 점"));
        addInfusionEnchantments();
        elementalTools();
        addBlock(ModBlocks.INFUSION_SPEED_STONE, pick("Infusion Speed Stone", "주입 속도석"));
        addBlock(ModBlocks.INFUSION_COST_STONE, pick("Infusion Cost Stone", "주입 절약석"));
        // one flame to a dye, and the dye's own name in front of it -- white is simply "nitor"
        ModBlocks.NITOR.forEach((colour, flame) -> addBlock(flame,
                colour == DyeColor.WHITE ? pick("Nitor", "니토르")
                        : pick(DYE_NAMES.get(colour)[0] + " Nitor", DYE_NAMES.get(colour)[1] + " 니토르")));
        addItem(ModItems.VOID_SEED, pick("Void Seed", "공허 씨앗"));
        addItem(ModItems.VOID_INGOT, pick("Void Ingot", "공허 주괴"));
        addItem(ModItems.VOID_NUGGET, pick("Void Nugget", "공허 조각"));
        addItem(ModItems.VOID_PLATE, pick("Void Plate", "공허 판"));



        addItem(ModItems.ALCHEMOMETER, pick("Alchemometer", "알케모미터"));
        addItem(ModItems.NODE_PLACER, pick("Node Placer", "노드 배치기"));
        add("item.alchemia.creative_only", pick("Creative only", "크리에이티브 전용"));
        addBlock(ModBlocks.RESEARCH_TABLE, pick("Research Table", "연구 탁자"));
        addBlock(ModBlocks.CRUCIBLE, pick("Crucible", "도가니"));
        addBlock(ModBlocks.ESSENTIA_SMELTER, pick("Essentia Smelter", "에센시아 제련로"));
        addBlock(ModBlocks.ALEMBIC, pick("Arcane Alembic", "비전 증류기"));
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
        add("item.alchemia.wand.focus", pick("Focus: %s", "포커스: %s"));
        add("item.alchemia.wand.focus_fitted", pick("Fitted %s", "%s 끼움"));
        add("item.alchemia.wand.focus_removed", pick("Took off %s", "%s 뺌"));
        add("key.categories.alchemia", "Alchemia");
        add("key.alchemia.change_focus", pick("Change Wand Focus", "완드 포커스 바꾸기"));
        add("key.alchemia.tool_setting", pick("Change Tool Setting", "도구 설정 바꾸기"));
        foci();
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
                {"goggles", "Goggles of Revealing", "계시의 고글",
                 "Two alchemometers looking through brass. An aura node is invisible to anyone not wearing a pair, which makes these the first thing to build and the last thing to take off: half of what this craft is built on cannot be seen at all until they are on your face.",
                 "놋쇠에 물린 알케모미터 둘. 이것을 쓰지 않은 사람에게 오라 노드는 보이지 않는다. 그래서 가장 먼저 만들고 가장 나중에 벗는 물건이 된다 — 이 기예가 딛고 선 것의 절반이 이걸 쓰기 전에는 아예 보이지 않는다."},
                {"traveller_boots", "Traveller's Boots", "여행자의 장화",
                 "They stop almost nothing. What they are worth is the walking: a step up a whole block without jumping, a push along the ground, a hold on the air that is not quite flying, and a fall that is forgotten as fast as it happens. Somebody who spends the day between a works and a mine will not take them off for a suit that stops arrows.",
                 "막아 주는 것은 거의 없다. 값어치는 걷는 데 있다. 뛰지 않고 한 칸을 오르고, 땅을 밀어 나아가고, 허공에서도 발이 듣고, 떨어지는 동안 떨어진 것이 잊힌다. 하루를 공방과 광산 사이에서 보내는 사람은 화살을 막아 주는 갑옷을 위해 이것을 벗지 않는다."},
                {"infusion_enchantment", "Infusion Enchantment", "주입 마법부여",
                 "An altar will put on a tool what no table will sell. There is no gambling and no book to keep it in: the tool goes under the matrix, the price is paid in essentia, and it comes back with one more thing about it. Collector sends what is broken to the one who broke it. Destructive takes the eight blocks round the one struck, where the tool would have served for them anyway. Burrowing brings a seam or a trunk apart from its far end. Refining makes ore come up now and then as a cluster, which smelts to twice the metal. Sounding taps the stone and shows what is behind it for a moment, at five swings' wear. Arcing carries a blow to whatever is standing beside what was struck. Essence makes what is killed give up a little of what it was made of -- the one way to essentia that wants no smelter, and so the meanest of them. Crouch and the digging ones go quiet.",
                 "작업대가 팔지 않는 것을 제단은 도구에 얹는다. 운에 맡길 것도, 담아 둘 책도 없다. 도구를 결계 아래에 두고 에센시아로 값을 치르면 한 가지를 더 지니고 돌아온다. 수집은 부순 것을 부순 이에게 보낸다. 파괴는 때린 칸 둘레 여덟을 같이 가져가되, 그 도구로 캘 수 있는 것만 가져간다. 굴착은 광맥이나 줄기를 먼 끝에서부터 허문다. 정련은 광석이 이따금 군집으로 나오게 한다. 제련하면 두 배다. 탐지는 돌을 두드려 돌 너머를 잠시 보여 주고, 그 대가로 도구가 다섯 번 닳는다. 전이는 때린 것 옆에 선 것에도 같은 매를 옮긴다. 정수는 죽인 것이 제 만들어진 바를 조금 내놓게 한다 -- 제련로 없이 에센시아를 얻는 유일한 길이고, 그래서 한 번에 주는 것이 적다. 웅크리면 캐는 것들은 조용해진다."},
                {"fortress_armour", "Fortress Armour", "요새 갑옷",
                 "A finished suit of alchemium goes under the matrix and comes back out as plate. It stops what void metal stops and lasts as long as the alchemium it was, which no single metal manages, and no enchanting table will ever be offered better steel. The more of the four is worn the less any one blow gets through, and a blow of fire or a blast is turned worse than a blade is. Falling does not wear it at all -- whatever the ground does to the wearer, it does nothing to the plate.",
                 "완성된 알케미움 한 벌을 결계 아래에 두면 판금이 되어 나온다. 공허 금속이 막는 만큼 막으면서 원래의 알케미움만큼 간다. 어느 금속 하나로도 둘 다 되지 않는다. 마법 탁자가 이보다 나은 쇠를 받아 본 적도 없다. 넷 중 많이 갖춰 입을수록 한 대가 덜 들어오고, 불과 폭발은 칼날보다 더 많이 튕겨 낸다. 떨어지는 것으로는 닳지 않는다. 땅이 입은 사람에게 무슨 짓을 하든 판금에는 아무 짓도 하지 않는다."},
                {"enchanted_fabric", "Enchanted Fabric", "마법 천",
                 "Wool and string are worked together at the bench with a little of all six primals, and what comes off is cloth that will hold a working the way a wand holds vis. Nothing is made of it that could not be made of cloth; what is different is that it does not forget.",
                 "양털과 실을 작업대에서 여섯 원시를 조금씩 먹여 가며 함께 짠다. 나오는 것은 완드가 비스를 머금듯 걸린 것을 머금는 천이다. 이것으로 만드는 물건 가운데 천으로 못 만들 것은 없다. 다른 점은 이 천이 잊지 않는다는 것뿐이다."},
                {"robes", "Robes", "로브",
                 "A robe stops arrows about as well as a shirt does. That is not what it is for. Cloth that remembers a working sits between a wand and the air it draws from, and every price the wand is asked comes down a little. Two parts in a hundred at the chest and at the legs, one at the feet. There is no hood: a head wants something else on it, and the robe was never cut for one.",
                 "로브가 화살을 막는 정도는 셔츠와 비슷하다. 그러자고 입는 것이 아니다. 걸린 것을 기억하는 천은 완드와 완드가 길어 올리는 공기 사이에 놓이고, 완드가 치르는 모든 값이 조금씩 내려간다. 가슴과 다리에서 백분의 이, 발에서 백분의 일. 두건은 없다. 머리에는 쓸 것이 따로 있고, 이 로브는 애초에 머리 몫을 재고 자른 것이 아니다."},
                {"void_robes", "Void Robes", "공허 로브",
                 "A suit of void metal taken apart on the altar and sewn back onto cloth. It stops what the metal stopped and comes apart as fast as the metal did, and in exchange it takes five parts in a hundred off every price, at each of its three pieces. This one has a hood and no boots -- the metal never made a good shoe, and the robe never made a good helm until there was metal to line it with.",
                 "공허 금속 한 벌을 제단에서 뜯어 천에 다시 꿰맨 것이다. 금속이 막던 만큼 막고 금속이 부서지던 만큼 빨리 부서지며, 그 대신 세 점이 저마다 모든 값에서 백분의 오를 깎는다. 이쪽은 두건이 있고 신이 없다. 그 금속으로는 쓸 만한 신이 나온 적이 없고, 천으로는 안을 댈 금속이 생기기 전까지 쓸 만한 투구가 나온 적이 없다."},
                {"focal_manipulator", "Focal Manipulator", "포커스 조작기",
                 "A bench for working one more thing into a focus. Five things will go into any focus, one to a rank, and which are on offer depends on the focus and on how deep the rank is -- the shallow ones give a focus more of what it already does, and the deep ones turn it into something else. The price is levels paid up front and vis drawn out of the air afterwards, a little every few ticks, so the bench wants a thick sky above it and a while to work. Take the focus off the bench part-way and the work is lost.",
                 "포커스에 한 가지를 더 박아 넣는 자리다. 어느 포커스든 다섯까지 들어가고 등급마다 하나씩이며, 어떤 것이 올라오는지는 포커스와 등급의 깊이에 달렸다. 얕은 등급은 본래 하던 일을 더 하게 해 주고, 깊은 등급은 아예 다른 것으로 바꾼다. 값은 먼저 경험치 레벨로 치르고 그 뒤로 몇 틱마다 조금씩 공기에서 비스를 길어 올린다. 그래서 하늘이 두터운 곳에 놓아야 하고 시간이 걸린다. 도중에 포커스를 들어내면 하던 일은 사라진다."},
                {"elemental_tools", "Elemental Tools", "원소 도구",
                 "Alchemium tools with a primal bound into each on the altar, and what that element is in a tool already worked into them. The axe of the stream fells a tree from its top and sends what falls to whoever felled it, and held up it draws every loose thing nearby in. The pickaxe of the core refines ore into clusters now and then, sounds out the stone it is tapped against, and sets alight whatever it strikes. The sword of the zephyr carries a blow on to what is standing about, and held up it raises a whirlwind that lifts its bearer and throws back anything that comes close. The shovel of the earthmover digs three by three, and used against a face it lays three by three of that same block out of the bag; the setting key turns which way the square stands. The hoe of growth tills three by three and makes things grow where there is nothing to till. Crouch with any of them and it is only a tool.",
                 "제단에서 알케미움 도구에 원시를 하나씩 묶고, 그 원소가 도구에서 하는 일을 미리 새겨 둔 것이다. 흐름의 도끼는 나무를 꼭대기부터 베고 떨어진 것을 벤 사람에게 보내며, 들고 있으면 둘레의 떨어진 것을 모두 끌어온다. 핵의 곡괭이는 광석을 이따금 군집으로 정련하고, 두드린 돌 너머를 짚어 보며, 친 것에 불을 붙인다. 미풍의 검은 맞은 것 옆에 선 것까지 베고, 들고 있으면 회오리를 일으켜 든 사람을 띄우고 다가오는 것을 밀쳐 낸다. 흙꾼의 삽은 3×3으로 파고, 면에 대고 쓰면 그 블록을 가방에서 꺼내 3×3으로 깐다 — 설정 키로 네모가 서는 방향을 바꾼다. 성장의 괭이는 3×3으로 갈고, 갈 것이 없으면 자라게 한다. 무엇이든 웅크리고 쓰면 그냥 도구다."},
                {"primal_crusher", "Primal Crusher", "원시 분쇄기",
                 "Both elemental diggers and both of their void-metal counterparts beaten together round something that should not be able to hold all four. What comes out is a pickaxe and a shovel at once that digs three by three and refines ore as it goes. It has lost the building and the sounding; what is left only breaks things. Like void metal it mends itself, a point a second, and unlike anything else it is uneasy in the hand -- it carries two points of warp while it is held.",
                 "원소 굴착 도구 둘과 그 공허 금속 짝 둘을, 넷을 다 품을 수 없어야 마땅한 것 둘레에 두들겨 합친 것이다. 나온 것은 곡괭이이자 삽이고, 3×3으로 파며 파는 대로 광석을 정련한다. 짓는 일과 짚어 보는 일은 잃었고 남은 것은 부수는 일뿐이다. 공허 금속처럼 초마다 한 점씩 스스로 고쳐지고, 다른 무엇과도 달리 손에 들기가 꺼림칙하다 — 들고 있는 동안 뒤틀림 2를 지닌다."},
                {"primal_charm", "Primal Charm", "원시 부적",
                 "Where artifice starts: a shard of every primal and the balanced shard that holds them, worked round a ring of gold. Nothing much is done with a charm on its own; a great deal is built round one. Quicksilver laid on the back of a pane of glass makes a mirror, which is the other thing nearly every device needs.",
                 "인공물이 시작되는 자리다. 원시마다 조각 하나와 그것들을 붙드는 균형 조각을 금 고리에 둘러 엮었다. 부적 하나로 하는 일은 별로 없지만, 이것을 둘러싸고 짓는 것은 많다. 판유리 뒤에 수은을 바르면 거울이 되는데, 장치 거의 모두가 필요로 하는 또 하나다."},
                {"arcane_lamp", "Arcane Lamp", "비전 램프",
                 "A lamp of brass and amber that lights more than itself. Every quarter of a second it sets a point of light down somewhere dark within fifteen blocks, where it can see, so that over a few minutes the cave or cellar round it fills with light from nowhere in particular. Hang it from any face. A redstone signal puts it out, and every light it set down goes out with it.",
                 "놋쇠와 호박으로 만든 램프인데 저 자신보다 넓게 밝힌다. 0.25초마다 열다섯 칸 안의 어두운 곳 중 보이는 곳에 빛 한 점을 놓는다. 몇 분이면 둘레의 동굴이나 지하실이 어디서 오는지 모를 빛으로 찬다. 어느 면에나 매달 수 있다. 레드스톤 신호가 들어오면 꺼지고, 놓아 둔 빛도 모두 함께 꺼진다."},
                {"growth_lamp", "Lamp of Growth", "성장의 램프",
                 "An arcane lamp worked on with gold and bone meal. It burns herba, piped in through the face it hangs from, and every point is a hundred nudges to whatever is still growing within six blocks -- each nudge one more turn of growing, taken or not as the plant would take it anyway. It goes dark when it runs out.",
                 "비전 램프에 금과 뼛가루를 먹인 것이다. 매달린 면으로 관을 대어 식물 에센시아를 태우고, 한 점마다 여섯 칸 안에서 아직 자라는 것에 백 번 손을 댄다. 한 번 손댈 때마다 한 번 더 자랄 기회가 생기고, 자라고 말고는 원래 그 식물이 하던 대로다. 다 떨어지면 꺼진다."},
                {"fertility_lamp", "Lamp of Fertility", "풍요의 램프",
                 "An arcane lamp worked on with gold, wheat and carrots. It burns victus, piped in through the face it hangs from, and every fifteen seconds it sets two grown animals of the same kind within seven blocks to courting, at two points a pair. It leaves a pen of more than seven of a kind alone.",
                 "비전 램프에 금과 밀과 당근을 먹인 것이다. 매달린 면으로 관을 대어 생명 에센시아를 태우고, 15초마다 일곱 칸 안의 다 자란 같은 종 동물 둘을 짝짓게 한다. 한 쌍에 두 점이다. 같은 종이 일곱보다 많은 우리는 건드리지 않는다."},
                {"hungry_chest", "Hungry Chest", "굶주린 상자",
                 "A chest in every way, except that whatever comes to rest on its lid it eats: whole stacks where there is room, part of one where there is only room for part. What will not fit stays where it fell. It never pairs into a double chest.",
                 "어느 모로 보나 상자인데, 뚜껑 위에 떨어져 멎은 것을 먹는다. 자리가 있으면 통째로, 일부만 들어가면 일부만 먹는다. 안 들어가는 것은 떨어진 자리에 남는다. 큰 상자로 이어지지 않는다."},
                {"water_jug", "Everfull Urn", "마르지 않는 항아리",
                 "A jug of baked clay that is never empty. Dip a bucket or a bottle in it and it comes away full. Left alone it tops up anything round it that holds water -- a crucible, a cauldron -- a quarter of a bucket every quarter of a second, and fills itself again from the aqua in the air.",
                 "구운 흙 항아리인데 비는 법이 없다. 양동이나 병을 담그면 가득 차서 나온다. 가만 두면 둘레의 물 담는 것 — 도가니, 가마솥 — 에 0.25초마다 양동이 4분의 1씩 물을 붓고, 공기 속 물 에센시아로 제 몸을 다시 채운다."},
                {"bath_salts", "Purifying Bath Salts", "정화의 목욕 소금",
                 "Warp is a danger to the most careful of thaumaturges, and for those who have dabbled a little too much its effects are more than a nuisance. These salts help. Toss them into still water and when they dissolve the water turns to a silvery purifying fluid. Step into it and it soothes away the day, warding you against warp for a while; it is used up doing so. The deeper warp has set in, the shorter the ward. The salts are fragile, and dissolve within ten seconds of being dropped, wherever they lie.",
                 "뒤틀림은 아무리 조심하는 마법사에게도 위험이고, 조금 지나치게 손댄 이에게는 성가신 정도로 끝나지 않는다. 이 소금이 도움이 된다. 고인 물에 던져 두면 녹으면서 물이 은빛 정화의 액체로 바뀐다. 그 안에 들어가면 하루의 피로가 풀리며 한동안 뒤틀림에서 지켜 준다. 그러고 나면 액체는 다 쓰인다. 뒤틀림이 깊이 박혔을수록 지켜 주는 시간은 짧다. 소금은 약해서, 떨어뜨리면 어디에 있든 10초 안에 녹아 버린다."},
                {"spa", "Arcane Spa", "비전 스파",
                 "Throwing salts into water is a chore, easily done away with. The spa takes water, by bucket or by pipe from any side, and bath salts, through its panel or from a hopper on any side but the top, and every two seconds mixes a bucket of the one with a dose of the other and pumps it into the pool above. The pool spreads a block at a time to five by five, wherever there is floor. Switched over on its panel it pumps whatever is in its tank, unmixed -- nearly any liquid that can stand in the world. A redstone signal stops it.",
                 "소금을 물에 던지는 일은 번거롭고, 쉽게 덜 수 있다. 스파는 물을 양동이로든 관으로든 어느 면으로나 받고, 목욕 소금을 화면으로든 위를 뺀 어느 면의 깔때기로든 받는다. 그리고 2초마다 물 한 양동이에 소금 한 줌을 섞어 위쪽 웅덩이로 퍼 올린다. 웅덩이는 바닥이 있는 곳으로 한 칸씩 퍼져 5×5까지 된다. 화면에서 바꾸면 섞지 않고 통에 담긴 것을 그대로 퍼 올린다. 세상에 놓일 수 있는 액체라면 거의 무엇이든 된다. 레드스톤 신호가 들어오면 멈춘다."},
                {"levitator", "Arcane Levitator", "비전 부양기",
                 "Not flight, but the next best thing. A levitator lifts whatever is in front of it -- things dropped, animals, you -- up to eight blocks, or as far as the first solid block. Crouch over one and you are let down slowly. Put on its side it pushes along instead, holding up what it pushes; put it down facing whichever way you like, crouching to turn it round. It burns aer from the aura while it is carrying anything, more the further it reaches: an empty hand on it steps its range through four, eight, sixteen and thirty-two blocks. A redstone signal stops it.",
                 "나는 것은 아니지만 그 다음쯤은 된다. 부양기는 앞에 있는 것 — 떨어진 물건, 동물, 당신 — 을 여덟 칸까지, 아니면 처음 막히는 블록까지 들어 올린다. 그 위에서 웅크리면 천천히 내려온다. 옆으로 눕히면 대신 밀어 보내고, 미는 동안 떠받친다. 어느 쪽으로든 놓을 수 있고, 웅크리고 놓으면 반대로 놓인다. 무엇을 나르는 동안 오라에서 공기를 태우고, 멀리 닿을수록 더 태운다. 빈손으로 누르면 범위가 넷, 여덟, 열여섯, 서른둘로 바뀐다. 레드스톤 신호가 들어오면 멈춘다."},
                {"redstone_relay", "Redstone Relay", "레드스톤 계전기",
                 "Redstone is known to dull magic; sent through a vis crystal, it can be made to do only as much of that as you like. The relay is put down and wired like a repeater, its output the end with the torch. It has two toggles. The one at the back sets how strong a signal it needs before it answers; the one beside the torch, how strong a signal it gives out when it does. Each steps from one to fifteen.",
                 "레드스톤이 마법을 무디게 한다는 것은 잘 알려져 있다. 비스 결정을 거쳐 보내면 그 무디게 하는 정도를 원하는 만큼만으로 맞출 수 있다. 계전기는 레드스톤 중계기처럼 놓고 잇는다. 횃불이 달린 쪽이 내보내는 쪽이다. 손잡이가 둘 있다. 뒤쪽 것은 얼마나 센 신호가 들어와야 답할지, 횃불 옆 것은 답할 때 얼마나 센 신호를 낼지를 정한다. 둘 다 1부터 15까지 바뀐다."},
                {"arcane_ear", "Arcane Ear", "비전 귀",
                 "An ear that hears note blocks. Tune it as you would a note block, a click a semitone up; it listens in the voice of whatever it is fixed to, the way a note block plays in the voice of what it stands on. When a note block within sixty-four blocks plays its note, it gives a short, full redstone pulse, through what it is fixed to as well. It can be fixed to any face.",
                 "소리 블록을 듣는 귀다. 소리 블록처럼 누를 때마다 반음씩 올려 맞춘다. 소리 블록이 딛고 선 것의 음색으로 울리듯, 귀는 붙어 있는 것의 음색으로 듣는다. 64칸 안의 소리 블록이 그 음을 울리면 짧고 꽉 찬 레드스톤 신호를 낸다. 붙은 블록을 통해서도 낸다. 어느 면에나 붙일 수 있다."},
                {"bellows", "Bellows", "풀무",
                 "Two boards and a hide, with a nozzle. Set against a furnace they hurry what is in it by half again. Set against an essentia smelter from the side or the back they cut the time it takes to boil anything down by an eighth apiece, three at most. They pump for as long as nothing stops them, and a redstone signal does. They blow the way you were looking when you put them down; crouch to turn them round.",
                 "판자 둘과 가죽 하나에 주둥이를 달았다. 화로에 대면 굽는 것을 절반만큼 더 빨리 굽는다. 에센시아 제련로 옆이나 뒤에 대면 하나마다 끓여 내는 시간을 팔분의 일씩 줄인다. 셋까지다. 무엇이 막기 전까지 계속 불고, 레드스톤 신호가 막는다. 놓을 때 바라보던 쪽으로 분다. 웅크리고 놓으면 반대로 놓인다."},
                {"clockwork_mind", "Clockwork Mind", "태엽 두뇌",
                 "Gears and a comparator under glass, plated in brass. It is not a mind in any way that matters, but it will follow one rule without being told twice, which is all a machine needs.",
                 "놋쇠를 댄 유리 속의 톱니와 비교기다. 따질 만한 의미의 두뇌는 아니지만 한 가지 규칙을 두 번 말하지 않아도 따른다. 기계에 필요한 것은 그것뿐이다."},
                {"biothaumic_mind", "Biothaumic Mind", "생체마법 두뇌",
                 "The clockwork mind with something living grown into it on the altar. What comes out can be told to choose rather than only to follow, and the things that have to decide for themselves -- an autocaster picking its mark -- are built round one.",
                 "태엽 두뇌에 제단에서 살아 있는 것을 자라 들게 한 것이다. 나온 것은 따르기만 하는 것이 아니라 고르라고 시킬 수 있다. 스스로 정해야 하는 것들 — 노릴 것을 고르는 자동 시전기 같은 — 은 이것을 둘러싸고 짓는다."},
                {"autocaster", "Autocaster", "자동 시전기",
                 "A focus on a bracket, with enough of a mind in it to pick something and point itself at it. It is not a thaumaturge: it draws its own vis out of the air a drop at a time, holds a hundred of each primal at most, and nothing worn or carried makes a working any cheaper for it. Seven of the twelve foci are worth aiming at a thing, and those are the seven it will take. It goes against any face -- floor, wall or ceiling -- and it is told what to mind: animals, monsters, people, and whatever is already yours. A wand clicked against it takes it back down, focus and all.",
                 "받침대에 얹은 포커스다. 무엇을 고르고 그쪽으로 제 몸을 돌릴 만큼의 생각이 들어 있다. 마법사는 아니다. 제 비스를 공기에서 한 방울씩 길어 올리고, 원시마다 백까지만 담으며, 무엇을 걸쳤든 들었든 값이 싸지는 일은 없다. 열둘 중 일곱만이 무언가를 겨눌 값어치가 있고, 받아 주는 것도 그 일곱이다. 바닥이든 벽이든 천장이든 어느 면에나 붙으며, 무엇을 노릴지 일러 줄 수 있다 — 동물, 몹, 사람, 그리고 이미 제 편인 것. 완드로 치면 포커스째 도로 떼어 낸다."},
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

    /** The five elemental tools and the crusher, and the three ways the shovel lays its square. */
    private void elementalTools() {
        add("item.alchemia.elemental_axe", pick("Axe of the Stream", "흐름의 도끼"));
        add("item.alchemia.elemental_pickaxe", pick("Pickaxe of the Core", "핵의 곡괭이"));
        add("item.alchemia.elemental_sword", pick("Sword of the Zephyr", "미풍의 검"));
        add("item.alchemia.elemental_shovel", pick("Shovel of the Earthmover", "흙꾼의 삽"));
        add("item.alchemia.elemental_hoe", pick("Hoe of Growth", "성장의 괭이"));
        add("item.alchemia.primal_crusher", pick("Primal Crusher", "원시 분쇄기"));
        add("item.alchemia.primal_charm", pick("Primal Charm", "원시 부적"));
        add("item.alchemia.mirrored_glass", pick("Mirrored Glass", "거울 유리"));
        add("item.alchemia.morphic_resonator", pick("Morphic Resonator", "변형 공명기"));
        add("item.alchemia.clockwork_mind", pick("Clockwork Mind", "태엽 두뇌"));
        add("item.alchemia.biothaumic_mind", pick("Biothaumic Mind", "생체마법 두뇌"));
        add("block.alchemia.bellows", pick("Bellows", "풀무"));
        add("block.alchemia.levitator", pick("Arcane Levitator", "비전 부양기"));
        add("block.alchemia.levitator.range", pick("Range set to %s blocks (%s aer a minute for each thing lifted)",
                "범위를 %s칸으로 맞췄다 (들어 올리는 것 하나에 분당 공기 %s점)"));
        add("block.alchemia.redstone_relay", pick("Redstone Relay", "레드스톤 계전기"));
        add("block.alchemia.redstone_relay.in", pick("In: %s", "받는 세기: %s"));
        add("block.alchemia.redstone_relay.out", pick("Out: %s", "내는 세기: %s"));
        add("block.alchemia.arcane_ear", pick("Arcane Ear", "비전 귀"));
        add("block.alchemia.spa", pick("Arcane Spa", "비전 스파"));
        add("block.alchemia.purifying_fluid", pick("Purifying Fluid", "정화의 액체"));
        add("item.alchemia.bath_salts", pick("Purifying Bath Salts", "정화의 목욕 소금"));
        add("item.alchemia.purifying_fluid_bucket", pick("Bucket of Purifying Fluid", "정화의 액체 양동이"));
        add("gui.alchemia.spa.mixing", pick("Mix with ingredient", "소금을 섞어 붓는다"));
        add("gui.alchemia.spa.pouring", pick("Use just the fluid", "담긴 것만 붓는다"));
        add("gui.alchemia.spa.amount", pick("%s mB", "%s mB"));
        add("block.alchemia.arcane_lamp", pick("Arcane Lamp", "비전 램프"));
        add("block.alchemia.growth_lamp", pick("Lamp of Growth", "성장의 램프"));
        add("block.alchemia.fertility_lamp", pick("Lamp of Fertility", "풍요의 램프"));
        add("block.alchemia.lamp_light", pick("Lamp Light", "램프 빛"));
        add("block.alchemia.hungry_chest", pick("Hungry Chest", "굶주린 상자"));
        add("block.alchemia.water_jug", pick("Everfull Urn", "마르지 않는 항아리"));
        add("item.alchemia.elemental_shovel.laying.0", pick("Laying flat against the face", "면에 납작하게 깐다"));
        add("item.alchemia.elemental_shovel.laying.1", pick("Laying upright out of the face", "면에서 세로로 세운다"));
        add("item.alchemia.elemental_shovel.laying.2", pick("Laying across out of the face", "면에서 가로로 뻗는다"));
    }

    /** What an altar puts on a tool, written under the tool's name. */
    private void addInfusionEnchantments() {
        add(InfusionEnchantment.COLLECTOR.key(), pick("Collector", "수집"));
        add(InfusionEnchantment.DESTRUCTIVE.key(), pick("Destructive", "파괴"));
        add(InfusionEnchantment.BURROWING.key(), pick("Burrowing", "굴착"));
        add(InfusionEnchantment.REFINING.key(), pick("Refining", "정련"));
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
