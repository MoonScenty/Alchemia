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
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.ELDRITCH_AUTOCASTER,
                pick("Ancient Autocaster", "고대 자동 시전기"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.ELDRITCH_WARDEN, pick("Eldritch Warden", "엘드리치 감시자"));
        add("entity.alchemia.eldritch_warden.titled", pick("%s the %s", "%2$s %1$s"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.ELDRITCH_GOLEM,
                pick("Eldritch Construct", "엘드리치 구조체"));
        add("entity.alchemia.eldritch_golem.titled", pick("%s Eldritch Construct", "%s 엘드리치 구조체"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.CULTIST_LEADER, pick("Crimson Praetor", "진홍 집정관"));
        add("entity.alchemia.cultist_leader.titled", pick("Praetor %s the %s", "%2$s 집정관 %1$s"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.GREATER_CULTIST_PORTAL,
                pick("Greater Crimson Portal", "큰 진홍 차원문"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTACLE_GIANT, pick("Giant Taintacle", "거대 오염 촉수"));
        add("entity.alchemia.boss.enraged", pick("%s is enraged by your powerful blows.",
                "%s(이)가 강력한 일격에 격노한다."));
        add("boss.alchemia.warden", pick("Black energy bubbles on the distant pedestal. Something is coming...",
                "먼 받침대 위에서 검은 기운이 끓어오른다. 무언가가 온다..."));
        add("boss.alchemia.golem", pick("A lone sentinel stands frozen in this room...",
                "이 방에 홀로 선 파수꾼이 굳은 채 서 있다..."));
        add("boss.alchemia.crimson", pick("A ruby portal opens nearby heralding the arrival of the Crimson Cult. You must close the portal quickly!",
                "가까이에 진홍빛 차원문이 열리며 진홍 교단이 온다. 어서 차원문을 닫아야 한다!"));
        add("boss.alchemia.taint", pick("An organic rustling comes from the room beyond...",
                "저 너머 방에서 살아 있는 것이 바스락거린다..."));
        add("subtitles.alchemia.jacobs", pick("Construct crackles", "구조체가 지직거린다"));
        addBlock(ModBlocks.SAPPING, pick("Sapping", "흡취"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.FIRE_BAT, pick("Firebat", "불박쥐"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.WISP, pick("Wisp", "위습"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.PECH, pick("Pech Forager", "페크 채집꾼"));
        add("entity.alchemia.pech.mage", pick("Pech Mage", "페크 마법사"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINT_CRAWLER, pick("Tainted Crawler", "오염 크롤러"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINT_SWARM, pick("Taint Swarm", "오염 떼"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTACLE, pick("Taintacle", "오염 촉수"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTACLE_TINY, pick("Taint Tendril", "오염 덩굴손"));
        add("entity.alchemia.pech.stalker", pick("Pech Stalker", "페크 추적자"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.ANGRY_ZOMBIE, pick("Angry Zombie", "성난 좀비"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.CULTIST_KNIGHT, pick("Crimson Knight", "진홍 기사"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.CULTIST_CLERIC, pick("Crimson Cleric", "진홍 사제"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.CULTIST_PORTAL,
                pick("Lesser Crimson Portal", "작은 진홍 차원문"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.GOLEM_ORB, pick("Golem Orb", "골렘 구체"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.ELDRITCH_CRAB, pick("Eldritch Crab", "엘드리치 게"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.ELDRITCH_GUARDIAN,
                pick("Eldritch Guardian", "엘드리치 수호자"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.MIND_SPIDER, pick("Mind Spider", "정신 거미"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.ELDRITCH_ORB, pick("Eldritch Orb", "엘드리치 구체"));
        addItem(ModItems.ELDRITCH_EYE, pick("Eldritch Eye", "엘드리치의 눈"));
        addItem(ModItems.ELDRITCH_GUARDIAN_SPAWN_EGG, pick("Eldritch Guardian Spawn Egg", "엘드리치 수호자 생성 알"));
        addItem(ModItems.MIND_SPIDER_SPAWN_EGG, pick("Mind Spider Spawn Egg", "정신 거미 생성 알"));
        add("subtitles.alchemia.eldritch_guardian.ambient", pick("Guardian breathes", "수호자가 숨을 쉰다"));
        add("subtitles.alchemia.eldritch_guardian.screech", pick("Guardian screeches", "수호자가 비명을 지른다"));
        add("subtitles.alchemia.eldritch_guardian.death", pick("Guardian dies", "수호자가 죽는다"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.INHABITED_ZOMBIE,
                pick("Shambling Husk", "비틀거리는 껍데기"));
        addItem(ModItems.ELDRITCH_CRAB_SPAWN_EGG, pick("Eldritch Crab Spawn Egg", "엘드리치 게 생성 알"));
        addItem(ModItems.INHABITED_ZOMBIE_SPAWN_EGG, pick("Shambling Husk Spawn Egg", "비틀거리는 껍데기 생성 알"));
        add("subtitles.alchemia.eldritch_crab.ambient", pick("Crab chatters", "게가 딸깍거린다"));
        add("subtitles.alchemia.eldritch_crab.bite", pick("Crab snaps", "게가 집게를 휘두른다"));
        add("subtitles.alchemia.eldritch_crab.death", pick("Crab dies", "게가 죽는다"));
        String[][] crimson = {{"crimson_robe_helm", "Crimson Cult Hood", "진홍 교단 두건"},
                {"crimson_robe_chest", "Crimson Cult Robe", "진홍 교단 로브"},
                {"crimson_robe_legs", "Crimson Cult Leggings", "진홍 교단 바지"},
                {"crimson_plate_helm", "Crimson Cult Helm", "진홍 교단 투구"},
                {"crimson_plate_chest", "Crimson Cult Chestplate", "진홍 교단 흉갑"},
                {"crimson_plate_legs", "Crimson Cult Greaves", "진홍 교단 각반"},
                {"crimson_praetor_helm", "Crimson Praetor Helm", "진홍 집정관 투구"},
                {"crimson_praetor_chest", "Crimson Praetor Chestplate", "진홍 집정관 흉갑"},
                {"crimson_praetor_legs", "Crimson Praetor Greaves", "진홍 집정관 각반"},
                {"crimson_boots", "Crimson Cult Boots", "진홍 교단 장화"}};
        for (String[] piece : crimson) {
            addItem(ModItems.CRIMSON.get(piece[0]), pick(piece[1], piece[2]));
        }
        addItem(ModItems.CRIMSON_BLADE, pick("Crimson Blade", "진홍 검"));
        add("item.alchemia.crimson_blade.sapping", pick("Greater Sapping", "큰 흡취"));
        addItem(ModItems.CRIMSON_RITES, pick("Crimson Rites", "진홍 의식서"));
        add("item.alchemia.crimson_rites.text.0", pick("The book is filled with strange symbols.",
                "낯선 기호로 가득한 책이다."));
        add("item.alchemia.crimson_rites.text.1", pick("Use it to study it.", "써서 읽어 본다."));
        addItem(ModItems.CULTIST_KNIGHT_SPAWN_EGG, pick("Crimson Knight Spawn Egg", "진홍 기사 생성 알"));
        addItem(ModItems.CULTIST_CLERIC_SPAWN_EGG, pick("Crimson Cleric Spawn Egg", "진홍 사제 생성 알"));
        addItem(ModItems.CULTIST_PORTAL_SPAWN_EGG, pick("Lesser Crimson Portal Spawn Egg", "작은 진홍 차원문 생성 알"));
        add("subtitles.alchemia.cultist.chant", pick("Cleric chants", "사제가 읊조린다"));
        add("subtitles.alchemia.cultist_portal.ambient", pick("Portal hums", "차원문이 웅웅거린다"));
        add("subtitles.alchemia.cultist_portal.summon", pick("Cultist steps through", "교단원이 건너온다"));
        add("subtitles.alchemia.cultist.cast", pick("Cleric casts", "사제가 주문을 던진다"));
        add("subtitles.alchemia.golem_orb.burst", pick("Orb bursts", "구체가 터진다"));
        add("subtitles.alchemia.crimson_rites.read", pick("Something learnt", "무언가를 깨친다"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.FURIOUS_ZOMBIE, pick("Furious Zombie", "격노한 좀비"));
        addItem(ModItems.ZOMBIE_BRAIN, pick("Zombie Brain", "좀비 뇌"));
        addItem(ModItems.ANGRY_ZOMBIE_SPAWN_EGG, pick("Angry Zombie Spawn Egg", "성난 좀비 생성 알"));
        addItem(ModItems.FURIOUS_ZOMBIE_SPAWN_EGG, pick("Furious Zombie Spawn Egg", "격노한 좀비 생성 알"));
        addItem(ModItems.FIREBAT_SPAWN_EGG, pick("Firebat Spawn Egg", "불박쥐 생성 알"));
        addItem(ModItems.WISP_SPAWN_EGG, pick("Wisp Spawn Egg", "위습 생성 알"));
        addItem(ModItems.WISPY_ESSENCE, pick("Wispy Essence", "위습의 정수"));
        addItem(ModItems.PECH_SPAWN_EGG, pick("Pech Spawn Egg", "페크 생성 알"));
        addItem(ModItems.COIN, pick("Gold Coin", "금화"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTED_COW, pick("Tainted Cow", "오염된 소"));
        addItem(ModItems.TAINTED_COW_SPAWN_EGG, pick("Tainted Cow Spawn Egg", "오염된 소 생성 알"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTED_PIG, pick("Tainted Pig", "오염된 돼지"));
        addItem(ModItems.TAINTED_PIG_SPAWN_EGG, pick("Tainted Pig Spawn Egg", "오염된 돼지 생성 알"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTED_CHICKEN, pick("Tainted Chicken", "오염된 닭"));
        addItem(ModItems.TAINTED_CHICKEN_SPAWN_EGG, pick("Tainted Chicken Spawn Egg", "오염된 닭 생성 알"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTED_SHEEP, pick("Tainted Sheep", "오염된 양"));
        addItem(ModItems.TAINTED_SHEEP_SPAWN_EGG, pick("Tainted Sheep Spawn Egg", "오염된 양 생성 알"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTED_CREEPER, pick("Tainted Creeper", "오염된 크리퍼"));
        addItem(ModItems.TAINTED_CREEPER_SPAWN_EGG, pick("Tainted Creeper Spawn Egg", "오염된 크리퍼 생성 알"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTED_VILLAGER, pick("Tainted Villager", "오염된 주민"));
        addItem(ModItems.TAINTED_VILLAGER_SPAWN_EGG, pick("Tainted Villager Spawn Egg", "오염된 주민 생성 알"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.TAINTED_RABBIT, pick("Tainted Rabbit", "오염된 토끼"));
        addItem(ModItems.TAINTED_RABBIT_SPAWN_EGG, pick("Tainted Rabbit Spawn Egg", "오염된 토끼 생성 알"));
        addEntityType(me.moonscenty.alchemia.registry.ModEntities.ALCHEMIC_SLIME, pick("Alchemic Slime", "연금 슬라임"));
        addItem(ModItems.ALCHEMIC_SLIME_SPAWN_EGG, pick("Alchemic Slime Spawn Egg", "연금 슬라임 생성 알"));
        addItem(ModItems.TAINTED_GOO, pick("Tainted Goo", "오염된 점액"));
        addItem(ModItems.TAINT_TENDRIL, pick("Taint Tendril", "오염 덩굴손"));
        addItem(ModItems.TAINT_CRAWLER_SPAWN_EGG, pick("Tainted Crawler Spawn Egg", "오염 크롤러 생성 알"));
        addItem(ModItems.TAINT_SWARM_SPAWN_EGG, pick("Taint Swarm Spawn Egg", "오염 떼 생성 알"));
        addItem(ModItems.TAINTACLE_SPAWN_EGG, pick("Taintacle Spawn Egg", "오염 촉수 생성 알"));
        add("subtitles.alchemia.swarm.ambient", pick("Swarm hums", "떼가 웅웅거린다"));
        add("subtitles.alchemia.swarm.attack", pick("Swarm bites", "떼가 문다"));
        add("subtitles.alchemia.swarm.buzz", pick("Fly buzzes", "날벌레가 윙윙거린다"));
        add("subtitles.alchemia.taintacle.ambient", pick("Taintacle creaks", "촉수가 삐걱거린다"));
        add("subtitles.alchemia.taintacle.lash", pick("Taintacle lashes", "촉수가 후려친다"));
        add("subtitles.alchemia.gore", pick("Something bursts", "무언가 터진다"));
        addItem(ModItems.KNOWLEDGE_FRAGMENT, pick("Knowledge Fragment", "지식의 조각"));
        add("item.alchemia.knowledge_fragment.help", pick("Use to gain experience", "사용하면 경험치를 얻는다"));
        add("item.alchemia.wispy_essence.holding", pick("Two points of %s", "%s 두 점"));
        add("subtitles.alchemia.wisp.ambient", pick("Wisp hums", "위습이 웅웅거린다"));
        add("subtitles.alchemia.wisp.death", pick("Wisp goes out", "위습이 꺼진다"));
        add("subtitles.alchemia.wisp.zap", pick("Wisp zaps", "위습이 불꽃을 튀긴다"));
        add("subtitles.alchemia.pech.blast", pick("Frost cracks", "서리가 갈라진다"));
        add("subtitles.alchemia.pech.ambient", pick("Pech mutters", "페크가 중얼거린다"));
        add("subtitles.alchemia.pech.hurt", pick("Pech hurts", "페크가 다친다"));
        add("subtitles.alchemia.pech.death", pick("Pech dies", "페크가 죽는다"));
        add("subtitles.alchemia.pech.charge", pick("Pech growls", "페크가 으르렁댄다"));
        add("subtitles.alchemia.pech.trade", pick("Pech chatters", "페크가 재잘댄다"));
        add("subtitles.alchemia.pech.dice", pick("Bones rattle", "뼈 주사위가 구른다"));
        add("subtitles.alchemia.coins", pick("Coins jingle", "동전이 짤랑거린다"));
        add("subtitles.alchemia.urnbreak", pick("Urn shatters", "항아리가 깨진다"));
        String[][] champions = {{"bold", "Bold", "대담한"}, {"spine", "Spined", "가시 돋친"},
                {"armor", "Armored", "무장한"}, {"mighty", "Mighty", "강력한"}, {"grim", "Grim", "음산한"},
                {"warded", "Warded", "보호받는"}, {"warp", "Warped", "뒤틀린"}, {"undying", "Undying", "죽지 않는"},
                {"fiery", "Fiery", "불타는"}, {"sickly", "Sickly", "병든"}, {"venomous", "Venomous", "독이 있는"},
                {"vampiric", "Vampiric", "흡혈하는"}, {"infested", "Infested", "감염된"}};
        for (String[] champion : champions) {
            add("champion.mod." + champion[0], pick(champion[1], champion[2]));
        }
        add("item.alchemia.autocaster.note",
                pick("Fires a fitted focus on its own", "끼운 포커스를 혼자 쏜다"));
        add("entity.alchemia.autocaster.not_yours", pick("Not yours", "남의 것이다"));
        // golems: what they are called, what they say, and every trait and part the press shows
        add("entity.alchemia.golem", pick("Golem", "골렘"));
        add("entity.alchemia.golem.not_yours", pick("This does not belong to you", "당신의 것이 아니다"));
        add("entity.alchemia.golem.follow", pick("I am coming, Master", "따라가겠습니다, 주인님"));
        add("entity.alchemia.golem.stay", pick("I will stay here, Master", "여기 있겠습니다, 주인님"));
        add("item.alchemia.golem.rank", pick("Rank %s", "랭크 %s"));
        add("subtitles.alchemia.golem.clack", pick("Golem clacks", "골렘이 딸깍거린다"));
        add("subtitles.alchemia.golem.death", pick("Golem falls apart", "골렘이 무너진다"));
        add("subtitles.alchemia.golem.order", pick("Golem takes an order", "골렘이 명령을 받는다"));
        add("subtitles.alchemia.made", pick("Something is made", "무언가가 만들어진다"));
        add("subtitles.alchemia.golem_press.start", pick("Golem press starts", "골렘 프레스가 움직인다"));
        add("golem.alchemia.trait.smart", pick("Smart", "영리함"));
        add("golem.alchemia.trait.smart.text",
                pick("Thinks for itself more than a golem ought to, and learns from what it does, coming on a rank at a time.",
                        "골렘치고는 제 생각이 있어서, 하는 일에서 배우며 랭크가 하나씩 오른다."));
        add("golem.alchemia.trait.deft", pick("Deft", "손재주"));
        add("golem.alchemia.trait.deft.text",
                pick("Fine fingers, for work that wants a light touch. Deft and clumsy cancel each other.",
                        "섬세한 손가락이 있어 가벼운 손길이 필요한 일을 해낸다. 손재주와 서투름은 서로 지운다."));
        add("golem.alchemia.trait.clumsy", pick("Clumsy", "서투름"));
        add("golem.alchemia.trait.clumsy.text",
                pick("Hands good for little more than the plainest handling. Clumsy and deft cancel each other.",
                        "가장 단순한 일 말고는 쓸모가 없는 손이다. 서투름과 손재주는 서로 지운다."));
        add("golem.alchemia.trait.fighter", pick("Fighter", "전투"));
        add("golem.alchemia.trait.fighter.text",
                pick("It will turn on things and strike them.",
                        "무언가에 덤벼들어 칠 줄 안다."));
        add("golem.alchemia.trait.wheeled", pick("Wheeled", "바퀴"));
        add("golem.alchemia.trait.wheeled.text",
                pick("Rolls rather than walks: quicker, but it cannot step up as high.",
                        "걷지 않고 구른다. 빠르지만 그만큼 높이 올라서지 못한다."));
        add("golem.alchemia.trait.flyer", pick("Flyer", "비행"));
        add("golem.alchemia.trait.flyer.text",
                pick("Floats wherever it goes, and is never hurt by a fall, but goes more slowly than it would walk.",
                        "어디든 떠서 다니고 떨어져 다치는 일도 없지만, 걷는 것보다 느리다."));
        add("golem.alchemia.trait.climber", pick("Climber", "등반"));
        add("golem.alchemia.trait.climber.text",
                pick("Goes straight up a wall when it has to, and is never hurt by a fall.",
                        "필요하면 벽을 곧장 타고 오르고, 떨어져 다치는 일이 없다."));
        add("golem.alchemia.trait.heavy", pick("Heavy Frame", "무거운 몸체"));
        add("golem.alchemia.trait.heavy.text",
                pick("Slower for its weight. Heavy and light cancel each other.",
                        "무게만큼 느리다. 무거운 몸체와 가벼운 몸체는 서로 지운다."));
        add("golem.alchemia.trait.light", pick("Light Frame", "가벼운 몸체"));
        add("golem.alchemia.trait.light.text",
                pick("Quicker for being light. Light and heavy cancel each other.",
                        "가벼운 만큼 빠르다. 가벼운 몸체와 무거운 몸체는 서로 지운다."));
        add("golem.alchemia.trait.fragile", pick("Fragile", "연약함"));
        add("golem.alchemia.trait.fragile.text",
                pick("A quarter less health and armour. Fragile and armored cancel each other.",
                        "체력과 방어가 사분의 일 적다. 연약함과 장갑은 서로 지운다."));
        add("golem.alchemia.trait.repair", pick("Improved Self Repair", "향상된 자가 수리"));
        add("golem.alchemia.trait.repair.text",
                pick("Mends itself more than twice as fast as other golems.",
                        "다른 골렘보다 두 배 넘게 빨리 스스로를 고친다."));
        add("golem.alchemia.trait.scout", pick("Scout", "정찰"));
        add("golem.alchemia.trait.scout.text",
                pick("Sees further, and goes twice as far from home about its work: thirty-two blocks rather than sixteen.",
                        "더 멀리 보고, 일하러 집에서 두 배 멀리 간다. 열여섯 칸이 아니라 서른두 칸이다."));
        add("golem.alchemia.trait.armored", pick("Armored", "장갑"));
        add("golem.alchemia.trait.armored.text",
                pick("Half as much armour again, or one more at least. Armored and fragile cancel each other.",
                        "방어가 절반만큼, 적어도 하나는 더 붙는다. 장갑과 연약함은 서로 지운다."));
        add("golem.alchemia.trait.brutal", pick("Brutal", "잔혹함"));
        add("golem.alchemia.trait.brutal.text",
                pick("Hits half as hard again, or one harder at least.",
                        "절반만큼, 적어도 하나는 더 세게 친다."));
        add("golem.alchemia.trait.fireproof", pick("Fireproof", "내화"));
        add("golem.alchemia.trait.fireproof.text",
                pick("Fire and lava do nothing to it.",
                        "불도 용암도 아무것도 하지 못한다."));
        add("golem.alchemia.trait.breaker", pick("Breaker", "파쇄"));
        add("golem.alchemia.trait.breaker.text",
                pick("Can break most blocks.",
                        "대부분의 블록을 부술 수 있다."));
        add("golem.alchemia.trait.hauler", pick("Hauler", "운반"));
        add("golem.alchemia.trait.hauler.text",
                pick("Carries two stacks of things rather than one.",
                        "물건을 한 묶음이 아니라 두 묶음 나른다."));
        add("golem.alchemia.trait.ranged", pick("Ranged", "원거리"));
        add("golem.alchemia.trait.ranged.text",
                pick("Strikes from a distance.",
                        "멀리서 공격한다."));
        add("golem.alchemia.trait.blastproof", pick("Blastproof", "내폭"));
        add("golem.alchemia.trait.blastproof.text",
                pick("Takes a third of what an explosion would do to it, and never more than half its health at once.",
                        "폭발이 주는 피해의 삼분의 일만 받고, 한 번에 체력의 절반 넘게 잃는 일이 없다."));
        add("golem.alchemia.material.wood", pick("Greatwood", "거대나무"));
        add("golem.alchemia.material.wood.text",
                pick("Light and quick, but there is not much to it.",
                        "가볍고 빠르지만 단단하지는 않다."));
        add("golem.alchemia.material.iron", pick("Iron", "철"));
        add("golem.alchemia.material.iron.text",
                pick("Sturdy, fireproof and hard to blow apart, but heavy.",
                        "튼튼하고, 불에 타지 않고, 폭발에도 잘 견디지만 무겁다."));
        add("golem.alchemia.material.clay", pick("Clay", "점토"));
        add("golem.alchemia.material.clay.text",
                pick("Baked clay: not strong, but it does not burn, and it is not heavy.",
                        "구운 점토다. 강하지는 않지만 불에 타지 않고 무겁지도 않다."));
        add("golem.alchemia.material.brass", pick("Brass", "황동"));
        add("golem.alchemia.material.brass.text",
                pick("Not so sturdy as iron, and it burns, but brass lets a frame be built light.",
                        "철만큼 튼튼하지 않고 불에도 타지만, 황동이면 몸체를 가볍게 지을 수 있다."));
        add("golem.alchemia.material.alchemium", pick("Alchemium", "알케미움"));
        add("golem.alchemia.material.alchemium.text",
                pick("Everything iron is and more: it takes more and is better armoured, and is just as heavy.",
                        "철이 가진 것에 더해 더 잘 버티고 방어도 좋다. 무겁기는 마찬가지다."));
        add("golem.alchemia.material.void", pick("Void Metal", "공허 금속"));
        add("golem.alchemia.material.void.text",
                pick("A little softer than iron and nowhere near as heavy, and it mends itself.",
                        "철보다 조금 무르고 훨씬 가벼우며, 스스로를 고친다."));
        add("golem.alchemia.head.basic", pick("Clockwork Head", "태엽 머리"));
        add("golem.alchemia.head.basic.text",
                pick("The plain head, with nothing for or against it.",
                        "평범한 머리다. 좋은 것도 나쁜 것도 없다."));
        add("golem.alchemia.head.smart", pick("Smart Head", "영리한 머리"));
        add("golem.alchemia.head.smart.text",
                pick("A bioalchemic mind in a head: it thinks and learns, but breaks easily.",
                        "생체연금 두뇌를 넣은 머리다. 생각하고 배우지만 쉽게 부서진다."));
        add("golem.alchemia.head.smart_armored", pick("Armored Smart Head", "장갑 두른 영리한 머리"));
        add("golem.alchemia.head.smart_armored.text",
                pick("The smart head, plated and padded so that it is no longer fragile.",
                        "영리한 머리에 판을 대고 속을 채워서 더는 연약하지 않다."));
        add("golem.alchemia.head.scout", pick("Perceptive Head", "눈 밝은 머리"));
        add("golem.alchemia.head.scout.text",
                pick("The clockwork head with a vision module for eyes.",
                        "태엽 머리에 시야 모듈을 눈으로 단 것이다."));
        add("golem.alchemia.head.smart_scout", pick("Bioalchemic Head", "생체연금 머리"));
        add("golem.alchemia.head.smart_scout.text",
                pick("A bioalchemic mind behind a vision module's eyes: the best a golem's head can be.",
                        "시야 모듈 눈 뒤에 생체연금 두뇌를 둔 머리다. 골렘 머리로는 이보다 나은 것이 없다."));
        add("golem.alchemia.arm.basic", pick("Basic Arms", "기본 팔"));
        add("golem.alchemia.arm.basic.text",
                pick("Plain arms and hands, with nothing for or against them.",
                        "평범한 팔과 손이다. 좋은 것도 나쁜 것도 없다."));
        add("golem.alchemia.arm.fine", pick("Fine Manipulators", "정밀 조작기"));
        add("golem.alchemia.arm.fine.text",
                pick("Arms ending in small, careful hands.",
                        "작고 조심스러운 손이 달린 팔이다."));
        add("golem.alchemia.arm.claws", pick("Claw Arms", "집게 팔"));
        add("golem.alchemia.arm.claws.text",
                pick("Arms ending in a pair of sharp metal pincers, with an aggression module built in.",
                        "날카로운 쇠 집게가 달린 팔이다. 공격 모듈이 들어 있다."));
        add("golem.alchemia.arm.breakers", pick("Breaker Arms", "파쇄 팔"));
        add("golem.alchemia.arm.breakers.text",
                pick("Arms ending in a pair of diamond-tipped grinders driven by pistons.",
                        "피스톤으로 돌리는 다이아몬드 끝 분쇄기가 달린 팔이다."));
        add("golem.alchemia.arm.darts", pick("Dart Launchers", "다트 발사기"));
        add("golem.alchemia.arm.darts.text",
                pick("Arms ending in launchers that make their own darts, with an aggression module built in.",
                        "다트를 스스로 지어 쏘는 발사기가 달린 팔이다. 공격 모듈이 들어 있다."));
        add("golem.alchemia.leg.walker", pick("Basic Legs", "기본 다리"));
        add("golem.alchemia.leg.walker.text",
                pick("Plain legs, with nothing for or against them.",
                        "평범한 다리다. 좋은 것도 나쁜 것도 없다."));
        add("golem.alchemia.leg.roller", pick("Uni-wheel", "외바퀴"));
        add("golem.alchemia.leg.roller.text",
                pick("One wheel: quick, but it cannot step up as high.",
                        "바퀴 하나다. 빠르지만 그만큼 높이 올라서지 못한다."));
        add("golem.alchemia.leg.climber", pick("Climbing Legs", "등반 다리"));
        add("golem.alchemia.leg.climber.text",
                pick("Legs fitted with flint grips, for going up walls.",
                        "부싯돌 손잡이를 달아 벽을 오르는 다리다."));
        add("golem.alchemia.leg.flyer", pick("Levitation Module", "부양 모듈"));
        add("golem.alchemia.leg.flyer.text",
                pick("An arcane levitator to float the golem about, a third more slowly.",
                        "골렘을 띄워 다니게 하는 비전 부양기다. 삼분의 일만큼 느려진다."));
        add("golem.alchemia.addon.none", pick("Nothing", "없음"));
        add("golem.alchemia.addon.none.text",
                pick("Nothing fitted.",
                        "아무것도 달지 않았다."));
        add("golem.alchemia.addon.armored", pick("Armor Plating", "장갑판"));
        add("golem.alchemia.addon.armored.text",
                pick("Plates of its own material all over it, as heavy as they are.",
                        "제 재료로 만든 판을 온몸에 덧댔다. 그만큼 무겁다."));
        add("golem.alchemia.addon.fighter", pick("Aggression Module", "공격 모듈"));
        add("golem.alchemia.addon.fighter.text",
                pick("An aggression module, so that it will fight.",
                        "싸우도록 공격 모듈을 달았다."));
        add("golem.alchemia.addon.hauler", pick("Carry Frame", "운반 틀"));
        add("golem.alchemia.addon.hauler.text",
                pick("A frame on its back for a second stack of things.",
                        "등에 둘째 묶음을 얹을 틀을 맸다."));
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
        addItem(ModItems.VOID_GEAR, pick("Void Gear", "공허 톱니바퀴"));
        addItem(ModItems.SEALS.get("guard"), pick("Control Seal: Guard", "제어 봉인: 경비"));
        addItem(ModItems.SEALS.get("guard_advanced"), pick("Advanced Control Seal: Guard", "상급 제어 봉인: 경비"));
        addItem(ModItems.SEALS.get("butcher"), pick("Control Seal: Butcher", "제어 봉인: 도축"));
        addItem(ModItems.SEALS.get("harvest"), pick("Control Seal: Harvest", "제어 봉인: 수확"));
        addItem(ModItems.SEALS.get("lumber"), pick("Control Seal: Lumberjack", "제어 봉인: 벌목"));
        addItem(ModItems.SEALS.get("breaker"), pick("Control Seal: Block Breaker", "제어 봉인: 블록 부수기"));
        addItem(ModItems.SEALS.get("use"), pick("Control Seal: Use", "제어 봉인: 사용"));
        add("golem.alchemia.prop.mob", pick("Target monsters", "괴물을 노림"));
        add("golem.alchemia.prop.animal", pick("Target animals", "동물을 노림"));
        add("golem.alchemia.prop.player", pick("Target players", "플레이어를 노림"));
        add("golem.alchemia.prop.replant", pick("Replant crops", "작물을 다시 심음"));
        add("golem.alchemia.prop.provision", pick("Ask for seed", "씨앗을 청함"));
        add("golem.alchemia.prop.left", pick("Left click", "왼쪽 클릭"));
        add("golem.alchemia.prop.empty", pick("Click where the block has gone", "블록이 없는 곳을 클릭"));
        add("golem.alchemia.prop.emptyhand", pick("May use an empty hand", "빈손으로도 씀"));
        add("golem.alchemia.prop.sneak", pick("Click crouching", "웅크리고 클릭"));
        add("golem.alchemia.prop.provision.wl", pick("Ask for what is whitelisted", "허용 목록의 것을 청함"));
        add("entity.alchemia.golem_dart", pick("Golem Dart", "골렘 다트"));
        addItem(ModItems.TALLOW, pick("Magic Tallow", "마법 수지"));
        addBlock(ModBlocks.FLESH_BLOCK, pick("Block of Flesh", "살덩이 블록"));
        addItem(ModItems.SANITY_SOAP, pick("Sanitizing Soap", "정신 정화 비누"));
        java.util.Map<me.moonscenty.alchemia.block.CrystalType, String[]> arrows = java.util.Map.of(
                me.moonscenty.alchemia.block.CrystalType.AIR, new String[] {"Air Arrow", "공기 화살"},
                me.moonscenty.alchemia.block.CrystalType.FIRE, new String[] {"Fire Arrow", "불 화살"},
                me.moonscenty.alchemia.block.CrystalType.WATER, new String[] {"Water Arrow", "물 화살"},
                me.moonscenty.alchemia.block.CrystalType.EARTH, new String[] {"Earth Arrow", "흙 화살"},
                me.moonscenty.alchemia.block.CrystalType.ORDER, new String[] {"Order Arrow", "질서 화살"},
                me.moonscenty.alchemia.block.CrystalType.ENTROPY, new String[] {"Entropy Arrow", "엔트로피 화살"});
        ModItems.PRIMAL_ARROWS.forEach((type, arrow) -> addItem(arrow, pick(arrows.get(type)[0], arrows.get(type)[1])));
        add("entity.alchemia.primal_arrow", pick("Primal Arrow", "원시 화살"));
        addItem(ModItems.BONE_BOW, pick("Bow of Bone", "뼈 활"));
        addItem(ModItems.BOTTLE_TAINT, pick("Bottle of Taint", "오염 병"));
        addItem(ModItems.THAUMOSTATIC_HARNESS, pick("Thaumostatic Harness", "타우모스태틱 하네스"));
        addBlock(ModBlocks.ESSENTIA_INPUT, pick("Filling Essentia Transfuser", "채우는 에센시아 수혈기"));
        addBlock(ModBlocks.TAINT_DUST, pick("Degraded Taint Dust", "삭은 오염 먼지"));
        addBlock(ModBlocks.ALCHEMICAL_CONSTRUCT, pick("Alchemical Construct", "연금술 구조체"));
        addBlock(ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT, pick("Advanced Alchemical Construct", "고급 연금술 구조체"));
        addBlock(ModBlocks.ESSENTIA_OUTPUT, pick("Emptying Essentia Transfuser", "비우는 에센시아 수혈기"));
        add("item.alchemia.thaumostatic_harness.fuel", pick("Flight left: %s s", "남은 비행: %s초"));
        add("key.alchemia.hover", pick("Thaumostatic harness", "타우모스태틱 하네스"));
        add("screen.alchemia.missing_jars.title", pick("Alchemia needs the original Thaumcraft",
                "Alchemia에는 원본 Thaumcraft가 필요합니다"));
        add("screen.alchemia.missing_jars.message", pick(
                "Alchemia draws with the original Thaumcraft's pictures, models and sounds, read from its jars. Download the missing jars from the author's CurseForge page, put them in %s (any file name), and start the game again.",
                "Alchemia는 원본 Thaumcraft jar에서 그림·모델·소리를 읽어 씁니다. 빠진 jar를 작성자의 CurseForge 페이지에서 받아 %s 폴더에 넣고(파일 이름은 상관없습니다) 게임을 다시 시작하세요."));
        add("screen.alchemia.missing_jars.download", pick("Download %s (CurseForge)", "%s 받기 (CurseForge)"));
        add("screen.alchemia.missing_jars.folder", pick("Open the legacy folder", "legacy 폴더 열기"));
        add("entity.alchemia.bottle_taint", pick("Bottled Taint", "병에 든 오염"));
        for (String kind : new String[] {"air_arrow", "fire_arrow", "order_arrow"}) {
            add("death.attack.alchemia." + kind, pick("%1$s was shot by a primal arrow",
                    "%1$s이(가) 원시 화살에 맞았다"));
            add("death.attack.alchemia." + kind + ".player", pick("%1$s was shot by %2$s",
                    "%1$s이(가) %2$s에게 원시 화살로 맞았다"));
        }
        addItem(ModItems.SINISTER_STONE, pick("Sinister Lodestone", "불길한 자석"));
        addBlock(ModBlocks.PATTERN_CRAFTER, pick("Arcane Pattern Crafter", "비전 문양 제작기"));
        add("subtitles.alchemia.key", pick("Dial clicks", "다이얼이 딸깍인다"));
        java.util.Map<net.minecraft.world.item.DyeColor, String[]> dyes = java.util.Map.ofEntries(
                java.util.Map.entry(net.minecraft.world.item.DyeColor.WHITE, new String[] {"White", "흰색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.ORANGE, new String[] {"Orange", "주황색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.MAGENTA, new String[] {"Magenta", "자홍색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.LIGHT_BLUE, new String[] {"Light Blue", "하늘색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.YELLOW, new String[] {"Yellow", "노란색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.LIME, new String[] {"Lime", "연두색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.PINK, new String[] {"Pink", "분홍색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.GRAY, new String[] {"Gray", "회색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.LIGHT_GRAY, new String[] {"Light Gray", "회백색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.CYAN, new String[] {"Cyan", "청록색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.PURPLE, new String[] {"Purple", "보라색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.BLUE, new String[] {"Blue", "파란색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.BROWN, new String[] {"Brown", "갈색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.GREEN, new String[] {"Green", "초록색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.RED, new String[] {"Red", "빨간색"}),
                java.util.Map.entry(net.minecraft.world.item.DyeColor.BLACK, new String[] {"Black", "검은색"}));
        ModBlocks.TALLOW_CANDLES.forEach((dye, candle) -> addBlock(candle,
                pick(dyes.get(dye)[0] + " Tallow Candle", dyes.get(dye)[1] + " 수지 양초")));
        addItem(ModItems.SEAL_BLANK, pick("Blank Seal", "빈 봉인"));
        addItem(ModItems.SEALS.get("pickup"), pick("Control Seal: Collect", "제어 봉인: 수집"));
        addItem(ModItems.SEALS.get("pickup_advanced"), pick("Advanced Control Seal: Collect", "상급 제어 봉인: 수집"));
        addItem(ModItems.SEALS.get("fill"), pick("Control Seal: Store", "제어 봉인: 보관"));
        addItem(ModItems.SEALS.get("fill_advanced"), pick("Advanced Control Seal: Store", "상급 제어 봉인: 보관"));
        addItem(ModItems.SEALS.get("empty"), pick("Control Seal: Empty", "제어 봉인: 비우기"));
        addItem(ModItems.SEALS.get("empty_advanced"), pick("Advanced Control Seal: Empty", "상급 제어 봉인: 비우기"));
        addItem(ModItems.SEALS.get("provider"), pick("Control Seal: Provide", "제어 봉인: 공급"));
        add("gui.alchemia.seal.category.0", pick("Priority / Locking", "우선순위·잠금"));
        add("gui.alchemia.seal.category.1", pick("Filter", "필터"));
        add("gui.alchemia.seal.category.2", pick("Area", "범위"));
        add("gui.alchemia.seal.category.3", pick("Options", "옵션"));
        add("gui.alchemia.seal.category.4", pick("Requirements", "요구 특성"));
        add("gui.alchemia.seal.blacklist", pick("Blacklist", "차단 목록"));
        add("gui.alchemia.seal.whitelist", pick("Whitelist", "허용 목록"));
        add("gui.alchemia.seal.up_down", pick("Up / Down", "위 / 아래"));
        add("gui.alchemia.seal.east_west", pick("East / West", "동 / 서"));
        add("gui.alchemia.seal.north_south", pick("North / South", "북 / 남"));
        add("gui.alchemia.seal.required", pick("Required", "필요"));
        add("gui.alchemia.seal.forbidden", pick("Forbidden", "금지"));
        add("golem.alchemia.prop.meta", pick("Compare damage", "손상도까지 비교"));
        add("golem.alchemia.prop.nbt", pick("Compare item data", "아이템 데이터까지 비교"));
        add("golem.alchemia.prop.ore", pick("Match by common tags", "공용 태그로 비교"));
        add("golem.alchemia.prop.mod", pick("Anything from the same mod", "같은 모드의 것이면 됨"));
        add("golem.alchemia.prop.exist", pick("Container must already hold it", "이미 들어 있는 것만"));
        add("golem.alchemia.prop.cycle", pick("Cycle through the whitelist", "허용 목록을 차례로"));
        add("golem.alchemia.prop.leave", pick("Always leave at least one", "늘 하나는 남김"));
        add("golem.alchemia.prop.single", pick("One item at a time", "한 번에 하나만"));
        add("golem.alchemia.prop.priority", pick("Task Priority", "작업 우선순위"));
        add("golem.alchemia.prop.color", pick("Set for %s golems", "%s 골렘 전용"));
        add("golem.alchemia.prop.colorall", pick("All golems", "모든 골렘"));
        add("golem.alchemia.prop.owner", pick("You own this seal", "당신의 봉인이다"));
        add("golem.alchemia.prop.lock", pick("Only golems of the seal's owner work it", "봉인 주인의 골렘만 일한다"));
        add("golem.alchemia.prop.unlock", pick("Any golem can work it", "어느 골렘이든 일한다"));
        add("golem.alchemia.prop.redon", pick("Stops while powered by redstone", "레드스톤 신호가 오면 멈춘다"));
        add("golem.alchemia.prop.redoff", pick("Ignores redstone", "레드스톤을 무시한다"));
        addItem(ModItems.GOLEM, pick("Golem", "골렘"));
        addItem(ModItems.GOLEM_BELL, pick("Golemancer's Bell", "골렘술사의 종"));
        addItem(ModItems.VISION_MODULE, pick("Vision Module", "시야 모듈"));
        addItem(ModItems.AGGRESSION_MODULE, pick("Aggression Module", "공격 모듈"));
        addBlock(ModBlocks.STONE_TABLE, pick("Stone Table", "돌 탁자"));
        addBlock(ModBlocks.GOLEM_PRESS, pick("Golem Press", "골렘 프레스"));
        addBlock(ModBlocks.GOLEM_PRESS_PART, pick("Golem Press", "골렘 프레스"));

        // the lands
        add("biome.alchemia.magical_forest", pick("Magical Forest", "마법의 숲"));
        add("biome.alchemia.eerie", pick("Eerie", "으스스한 땅"));
        add("biome.alchemia.tainted_land", pick("Tainted Land", "오염된 땅"));
        addBlock(ModBlocks.AMBIENT_GRASS, pick("Ambient Grass", "은은한 풀"));



        addItem(ModItems.ALCHEMOMETER, pick("Alchemometer", "알케모미터"));
        addItem(ModItems.NODE_PLACER, pick("Node Placer", "노드 배치기"));
        add("item.alchemia.creative_only", pick("Creative only", "크리에이티브 전용"));
        addBlock(ModBlocks.RESEARCH_TABLE, pick("Research Table", "연구 탁자"));
        addBlock(ModBlocks.CRUCIBLE, pick("Crucible", "도가니"));
        addBlock(ModBlocks.ESSENTIA_SMELTER, pick("Essentia Smelter", "에센시아 제련로"));
        addBlock(ModBlocks.ALCHEMIUM_SMELTER, pick("Alchemium Essentia Smelter", "알케미움 에센시아 제련로"));
        addBlock(ModBlocks.VOID_SMELTER, pick("Void Metal Essentia Smelter", "공허 금속 에센시아 제련로"));
        addBlock(ModBlocks.SMELTER_AUX, pick("Auxiliary Slurry Pump", "보조 슬러리 펌프"));
        addBlock(ModBlocks.SMELTER_VENT, pick("Auxiliary Venting Port", "보조 배기구"));
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
        addBlock(ModBlocks.ANCIENT_PEDESTAL, pick("Ancient Pedestal", "고대 받침대"));
        addBlock(ModBlocks.ELDRITCH_ALTAR, pick("Eldritch Altar", "엘드리치 제단"));
        addBlock(ModBlocks.ELDRITCH_CAP, pick("Eldritch Capstone", "엘드리치 갓돌"));
        addBlock(ModBlocks.ELDRITCH_OBELISK, pick("Eldritch Obelisk", "엘드리치 오벨리스크"));
        addBlock(ModBlocks.ELDRITCH_OBELISK_BODY, pick("Eldritch Obelisk", "엘드리치 오벨리스크"));
        addBlock(ModBlocks.ELDRITCH_PORTAL, pick("Eldritch Portal", "엘드리치 차원문"));
        addBlock(ModBlocks.BANNER, pick("Banner", "깃발"));
        addItem(ModItems.CRIMSON_BANNER, pick("Crimson Cult Banner", "진홍 교단 깃발"));
        addItem(ModItems.PRIMORDIAL_PEARL, pick("Primordial Pearl", "원시 진주"));
        addItem(ModItems.LOOT_BAG_COMMON, pick("Bag of Loot", "전리품 주머니"));
        addItem(ModItems.LOOT_BAG_UNCOMMON, pick("Bag of Uncommon Loot", "드문 전리품 주머니"));
        addItem(ModItems.LOOT_BAG_RARE, pick("Bag of Rare Loot", "귀한 전리품 주머니"));
        String[][] banners = {{"white", "White Banner", "흰 깃발"}, {"orange", "Orange Banner", "주황 깃발"},
                {"magenta", "Magenta Banner", "자홍 깃발"}, {"light_blue", "Light Blue Banner", "하늘색 깃발"},
                {"yellow", "Yellow Banner", "노란 깃발"}, {"lime", "Lime Banner", "연두 깃발"},
                {"pink", "Pink Banner", "분홍 깃발"}, {"gray", "Gray Banner", "회색 깃발"},
                {"light_gray", "Light Gray Banner", "연회색 깃발"}, {"cyan", "Cyan Banner", "청록 깃발"},
                {"purple", "Purple Banner", "보라 깃발"}, {"blue", "Blue Banner", "파란 깃발"},
                {"brown", "Brown Banner", "갈색 깃발"}, {"green", "Green Banner", "초록 깃발"},
                {"red", "Red Banner", "빨간 깃발"}, {"black", "Black Banner", "검은 깃발"}};
        for (String[] banner : banners) {
            add("item.alchemia.banner." + banner[0], pick(banner[1], banner[2]));
        }
        addBlock(ModBlocks.ELDRITCH_PEDESTAL, pick("Eldritch Pedestal", "엘드리치 받침대"));
        addBlock(ModBlocks.ANCIENT_STONE.block(), pick("Ancient Stone", "고대 돌"));
        addBlock(ModBlocks.ANCIENT_STONE.stairs(), pick("Ancient Stone Stairs", "고대 돌 계단"));
        addBlock(ModBlocks.ANCIENT_STONE.slab(), pick("Ancient Stone Slab", "고대 돌 반 블록"));
        addBlock(ModBlocks.ELDRITCH_STONE.block(), pick("Eldritch Stone", "엘드리치 돌"));
        addBlock(ModBlocks.ELDRITCH_STONE.stairs(), pick("Eldritch Stone Stairs", "엘드리치 돌 계단"));
        addBlock(ModBlocks.ELDRITCH_STONE.slab(), pick("Eldritch Stone Slab", "엘드리치 돌 반 블록"));
        addBlock(ModBlocks.ANCIENT_ROCK, pick("Ancient Rock", "고대 암석"));
        addBlock(ModBlocks.ANCIENT_TILE, pick("Ancient Stone Tile", "고대 돌 타일"));
        addBlock(ModBlocks.GLYPHED_STONE, pick("Glyphed Stone", "문자 새긴 돌"));
        addBlock(ModBlocks.ANCIENT_DOORWAY, pick("Ancient Doorway", "고대 문간"));
        addBlock(ModBlocks.CRUSTED_STONE, pick("Crusted Stone", "딱지 앉은 돌"));
        addBlock(ModBlocks.GLOWING_CRUSTED_STONE, pick("Glowing Crusted Stone", "빛나는 딱지 앉은 돌"));
        addBlock(ModBlocks.POROUS_STONE, pick("Porous Stone", "구멍 숭숭한 돌"));
        addBlock(ModBlocks.LOOT_URN, pick("Ancient Urn", "오래된 항아리"));
        addBlock(ModBlocks.LOOT_CRATE, pick("Old Crate", "오래된 상자"));
        addBlock(ModBlocks.ANCIENT_BEDROCK, pick("Ancient Bedrock", "고대 기반암"));
        addBlock(ModBlocks.VACUUM, pick("Deadly Void", "죽음의 공허"));
        addBlock(ModBlocks.ELDRITCH_LOCK, pick("Eldritch Lock", "엘드리치 자물쇠"));
        addBlock(ModBlocks.ELDRITCH_DOOR, pick("Void Door", "공허의 문"));
        addBlock(ModBlocks.CRAB_SPAWNER, pick("Crab Vent", "게 구멍"));
        addBlock(ModBlocks.LIQUID_DEATH, pick("Liquid Death", "액체 죽음"));
        addItem(ModItems.RUNED_TABLET, pick("Runed Tablet", "룬 석판"));
        addItem(ModItems.MUNDANE_AMULET, pick("Mundane Amulet", "평범한 부적"));
        addItem(ModItems.MUNDANE_RING, pick("Mundane Ring", "평범한 반지"));
        addItem(ModItems.MUNDANE_BELT, pick("Mundane Belt", "평범한 허리띠"));
        addItem(ModItems.FANCY_AMULET, pick("Fancy Amulet", "멋진 부적"));
        addItem(ModItems.FANCY_RING, pick("Fancy Ring", "멋진 반지"));
        addItem(ModItems.FANCY_BELT, pick("Fancy Belt", "멋진 허리띠"));
        add("item.alchemia.apprentice_ring", pick("Apprentice's Ring of %s", "%s의 견습생 반지"));
        ModItems.APPRENTICE_RINGS.values().forEach(ring -> addItem(ring, pick("Apprentice's Ring", "견습생 반지")));
        addItem(ModItems.RUNIC_AMULET, pick("Amulet of Runic Shielding", "룬 보호막 부적"));
        addItem(ModItems.EMERGENCY_RUNIC_AMULET, pick("Amulet of Emergency Shielding", "비상 보호막 부적"));
        addItem(ModItems.PROTECTION_RING, pick("Ring of Protection", "보호의 반지"));
        addItem(ModItems.RUNIC_RING, pick("Ring of Runic Shielding", "룬 보호막 반지"));
        addItem(ModItems.CHARGED_RUNIC_RING, pick("Charged Ring of Shielding", "충전된 보호막 반지"));
        addItem(ModItems.REVITALIZING_RUNIC_RING, pick("Revitalizing Ring of Shielding", "회복의 보호막 반지"));
        addItem(ModItems.RUNIC_GIRDLE, pick("Girdle of Runic Shielding", "룬 보호막 허리띠"));
        addItem(ModItems.KINETIC_RUNIC_GIRDLE, pick("Kinetic Girdle of Shielding", "반동의 보호막 허리띠"));
        addItem(ModItems.VIS_STONE, pick("Vis Stone", "비스 돌"));
        add("item.alchemia.vis_stone.text", pick("Recharges wands in hotbar", "단축바의 완드를 충전한다"));
        addItem(ModItems.VIS_AMULET, pick("Amulet of Vis", "비스 부적"));
        addItem(ModItems.THAUMOSTATIC_GIRDLE, pick("Thaumostatic Girdle", "알케모스태틱 허리띠"));
        addItem(ModItems.VERDANT_HEART, pick("Verdant Heart Band", "푸른 심장 반지"));
        add("item.alchemia.verdant_heart.lifegiver", pick("Lifegiver", "생명을 주는 것"));
        add("item.alchemia.verdant_heart.sustainer", pick("Sustainer", "지탱하는 것"));
        add("item.alchemia.vis_charge", pick("Vis charge: %s%%", "비스 충전: %s%%"));
        add("item.alchemia.runic_charge", pick("Runic shield +%s", "룬 보호막 +%s"));
        addItem(ModItems.TRIPLE_MEAT_TREAT, pick("Triple Meat Treat", "세 고기 간식"));
        add("jei.alchemia.instability", pick("Instability: %s", "불안정: %s"));
        add("subtitles.alchemia.runicshieldeffect", pick("Runic shield flares", "룬 보호막이 번쩍인다"));
        add("curios.identifier.necklace", pick("Amulet", "부적"));
        add("curios.identifier.ring", pick("Ring", "반지"));
        add("curios.identifier.belt", pick("Belt", "허리띠"));
        add("item.alchemia.runed_tablet.text", pick("It seems to be part of something larger.",
                "더 큰 무언가의 일부인 것 같다."));
        addItem(ModItems.LIQUID_DEATH_BUCKET, pick("Bucket of Liquid Death", "액체 죽음 양동이"));
        add("subtitles.alchemia.wind", pick("Wind howls", "바람이 운다"));
        add("subtitles.alchemia.pump", pick("Lock turns", "자물쇠가 돈다"));
        add("subtitles.alchemia.runicshieldcharge", pick("Runes catch", "룬이 깨어난다"));
        add("subtitles.alchemia.evilportal", pick("Way hums", "길이 웅웅거린다"));
        add("subtitles.alchemia.poof", pick("Something vanishes in a puff", "펑 하고 연기가 터진다"));
        add("subtitles.alchemia.spill", pick("Crucible spills", "도가니가 넘친다"));
        add("subtitles.alchemia.bubble", pick("Crucible bubbles", "도가니가 보글거린다"));
        add("subtitles.alchemia.jar", pick("Glass clinks", "유리가 달그락거린다"));
        add("subtitles.alchemia.page", pick("Page rustles", "종이가 바스락거린다"));
        add("subtitles.alchemia.crystal", pick("Crystal chimes", "수정이 울린다"));
        add("subtitles.alchemia.craftfail", pick("Working fails", "작업이 실패한다"));
        add("subtitles.alchemia.cameraticks", pick("Focus clicks into place", "초점이 딸깍 끼워진다"));
        add("subtitles.alchemia.squeek", pick("Valve squeaks", "밸브가 끼익거린다"));
        add("subtitles.alchemia.creak", pick("Tube creaks", "관이 삐걱거린다"));
        add("subtitles.alchemia.rumble", pick("Ground rumbles", "땅이 우르릉거린다"));
        add("subtitles.alchemia.fireloop", pick("Flames roar", "불길이 으르렁거린다"));
        add("subtitles.alchemia.brain", pick("Brain sighs", "뇌가 한숨을 쉰다"));
        add("subtitles.alchemia.infuserstart", pick("Infusion stirs", "주입이 시작된다"));
        add("subtitles.alchemia.infuser", pick("Infusion hums", "주입 제단이 웅웅거린다"));
        add("subtitles.alchemia.hhon", pick("Mechanism engages", "장치가 맞물린다"));
        add("subtitles.alchemia.hhoff", pick("Mechanism releases", "장치가 풀린다"));
        add("subtitles.alchemia.write", pick("Quill scratches", "깃펜이 사각거린다"));
        add("subtitles.alchemia.erase", pick("Ink is scraped away", "잉크를 긁어낸다"));
        add("subtitles.alchemia.swing", pick("Blade sweeps", "칼날이 휩쓴다"));
        add("subtitles.alchemia.heartbeat", pick("Heart pounds", "심장이 쿵쾅거린다"));
        add("subtitles.alchemia.whispers", pick("Something whispers", "무언가가 속삭인다"));
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
        addBlock(ModBlocks.TAINT_GEYSER, pick("Taint Geyser", "오염 간헐천"));
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
        add("warp.alchemia.gained.permanent", pick("You have gained permanent Warp!", "영구 뒤틀림을 얻었다!"));
        add("warp.alchemia.gained.sticky", pick("You have gained Warp!", "뒤틀림을 얻었다!"));
        add("warp.alchemia.gained.temporary", pick("You have gained temporary Warp!", "일시적 뒤틀림을 얻었다!"));
        add("warp.alchemia.lost.permanent", pick("You have lost permanent Warp!", "영구 뒤틀림을 잃었다!"));
        add("warp.alchemia.lost.sticky", pick("You have lost Warp!", "뒤틀림을 잃었다!"));
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
                {"brain_jar", "Brain in a Jar", "단지 속 두뇌",
                 "A zombie's brain, woken and sealed in a warded jar. It keeps some of its hunger, and though it cannot eat brains any more it enjoys the thoughts and experience of whatever dies nearby, drawing them in from six blocks off, up to two thousand. A good shake makes it give some back. When it is full it lets off wisps. It counts as two bookshelves to an enchanting table.",
                 "깨워서 봉인된 단지에 담은 좀비의 두뇌다. 굶주림을 얼마간 간직하고 있어서, 이제 뇌를 먹을 수는 없지만 가까이에서 죽은 것들의 생각과 경험은 즐겨 먹는다. 여섯 칸 밖에서부터 끌어당겨 이천까지 담는다. 세게 흔들면 얼마를 돌려준다. 가득 차면 위로 빛줄기를 피운다. 마법 부여대에는 책장 두 개로 친다."},
                {"thaumatorium", "Automated Alchemy", "자동 연금술",
                 "A crucible that works on its own. Stand two alchemical constructs on a crucible and work them with a wand, from the side you want it to face: what it makes comes out of that side, into a container there if there is one, onto the ground if not. Put a catalyst in it and choose, from the recipes you know that catalyst finishes, what it is to make; it draws the essentia up the pipes itself, an aspect at a time, and as long as there is catalyst it makes the thing perfectly, every time. The crucible under it needs no water, but it does need heat: lava, fire or a nitor directly below. It keeps one recipe in mind; every mnemonic matrix fixed to its sides or top, facing in, lets it keep two more. A redstone signal stops it.",
                 "스스로 일하는 도가니다. 도가니 위에 연금술 구조체 둘을 세우고, 앞으로 삼을 쪽에서 완드로 다룬다. 만든 것은 그쪽으로 나온다. 거기에 상자가 있으면 상자로, 없으면 바닥으로. 촉매를 넣고, 그 촉매로 마무리되는 알고 있는 레시피 가운데 무엇을 만들지 고른다. 에센시아는 관으로 상 하나씩 스스로 끌어오고, 촉매가 남아 있는 한 언제나 틀림없이 만든다. 아래의 도가니에 물은 필요 없지만 열은 필요하다. 바로 아래에 용암, 불, 또는 니토르. 레시피는 하나만 기억한다. 옆이나 위에 안쪽을 보게 붙인 기억 행렬 하나마다 둘씩 더 기억한다. 레드스톤 신호가 멈춘다."},
                {"centrifuge", "Alchemical Centrifuge", "연금술 원심분리기",
                 "Compound essentia piped in from below is spun until it comes apart, and one of the two things it was made of, either one, is drawn off the top. Only one: the other half is lost in the spinning. It holds one point coming in and one going out, takes nothing while either is full, and will not take the six primals, which have nothing to come apart into. It never pushes; something above has to draw on it. A redstone signal stops it.",
                 "아래에서 관으로 들어온 합성 에센시아를 갈라질 때까지 돌리고, 그것을 이루던 둘 가운데 하나를 위로 뽑아낸다. 하나만이다. 나머지 반은 도는 동안 사라진다. 들어오는 것 하나, 나가는 것 하나만 담고, 어느 쪽이든 차 있으면 받지 않는다. 더 쪼갤 것이 없는 여섯 원시는 받지 않는다. 스스로 밀어내지 않으니 위에서 무언가가 뽑아 가야 한다. 레드스톤 신호가 멈춘다."},
                {"crystallizer", "Essentia Crystallization", "에센시아 결정화",
                 "Essentia piped in at its back is set into crystals one point at a time, which come out of the other end into whatever container stands there, or onto the ground. Any aspect will do. It works on earth drawn out of the air, four points at a time; with none in the air it simply waits. A redstone signal stops it.",
                 "뒤로 관을 통해 들어온 에센시아를 한 점씩 결정으로 굳혀 반대쪽 끝으로 내보낸다. 그쪽에 상자가 있으면 거기에, 없으면 바닥에 떨군다. 어느 상이든 된다. 공기 중의 흙을 네 점씩 끌어다 쓰고, 공기에 흙이 없으면 그저 기다린다. 레드스톤 신호가 멈춘다."},
                {"improved_distillation", "Improved Distillation", "증류 개량",
                 "Two things to set against a smelter's sides or back. An auxiliary slurry pump draws on what the smelter holds and pours it up into alembics stacked on the pump itself, so one fire fills another column for every pump. An auxiliary venting port catches some of what the smelter loses before it gets into the air: a third with one, more with each one after, though less each time.",
                 "제련로 옆이나 뒤에 붙이는 것 둘. 보조 슬러리 펌프는 제련로에 든 것을 끌어다 펌프 위에 쌓은 증류기로 올려 보낸다. 펌프 하나마다 불 하나로 채우는 기둥이 하나씩 늘어난다. 보조 배기구는 제련로가 흘리는 것이 공기로 나가기 전에 일부를 붙잡는다. 하나면 셋에 하나, 더 붙일수록 더 많이 잡지만 늘어나는 몫은 점점 줄어든다."},
                {"alchemium_smelter", "Alchemium Essentia Smelter", "알케미움 에센시아 제련로",
                 "A smelter plated in alchemium. It boils things down no faster, but it loses less of what comes off them, and it pours what it holds up into the alembics above more quickly.",
                 "알케미움을 입힌 제련로다. 끓여 내는 속도는 같지만 흘리는 것이 적고, 담은 것을 위의 증류기로 더 빨리 올려 보낸다."},
                {"void_smelter", "Void Metal Essentia Smelter", "공허 금속 에센시아 제련로",
                 "A smelter plated in void metal. Almost nothing that comes off what it burns is lost.",
                 "공허 금속을 입힌 제련로다. 태워서 나오는 것 가운데 잃는 것이 거의 없다."},
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
                {"eldritch_minor", "Eldritch Epiphany", "엘드리치의 깨달음",
                 "Warp that does not pass has started to teach. In the moments when the world goes wrong you have glimpsed something under it -- a place outside, older than the aura and colder, that leans against this one. The void seed is a little of it: a seed sown in the crucible with darkness and the void worked into it, that grows something the world did not make.",
                 "사라지지 않는 뒤틀림이 무언가를 가르치기 시작했다. 세상이 어긋나는 순간마다 그 밑의 무언가를 언뜻 보았다. 오라보다 오래되고 차가운 바깥의 장소가 이쪽에 기대어 있다. 공허 씨앗은 그것의 한 조각이다. 도가니에 어둠과 공허를 함께 넣어 기른 씨앗은 이 세상이 만들지 않은 것을 키운다."},
                {"eldritch_major", "Eldritch Revelation", "엘드리치의 계시",
                 "You understand now what the stone rings are. Each is a door to the Outer Lands, held shut by the altar at its heart; the obelisk over it is the lock's weight. The Crimson Cult wants them open. What they would let through, you would rather not see.",
                 "이제 돌 고리가 무엇인지 안다. 저마다 바깥 땅으로 가는 문이고, 한가운데 제단이 그것을 닫아 두며, 그 위의 오벨리스크는 자물쇠의 무게다. 진홍 교단은 그것을 열고 싶어 한다. 그리로 무엇이 건너올지는 보고 싶지 않다."},
                {"oculus", "Opening the Eye", "눈 뜨기",
                 "It is simple, and you cannot think why the cult has never seen it. Set four eldritch eyes into the altar at the heart of a stone ring, one to a side, and touch it with a wand holding five hundred of every primal. The way over the altar opens. An eldritch eye is an ender eye worked on the altar with a void seed and gold.",
                 "너무 간단해서 교단이 왜 몰랐는지 모를 일이다. 돌 고리 한가운데 제단에 엘드리치의 눈 넷을 한 면에 하나씩 끼우고, 원시마다 오백씩 담은 완드를 대면 된다. 제단 위의 길이 열린다. 엘드리치의 눈은 엔더의 눈을 공허 씨앗과 금으로 제단에서 주입한 것이다."},
                {"banners", "Banners", "깃발",
                 "Dyed cloth on a pole, or hung from a wall. Put a filled phial to one to paint its aspect on it; crouch and touch it to wipe it off again.",
                 "장대에 매달거나 벽에 거는 물들인 천이다. 채운 유리병을 대면 그 상을 그려 넣고, 웅크리고 대면 지운다."},
                {"runic_shielding", "Runic Shielding", "룬 보호막",
                 "Runes cut into a bauble with a primal charm, amber and enchanted cloth hold up a shield over whoever wears it: a pool of hits that are taken before their own, shown as golden hearts. It fills again by itself, a point every two seconds, drawing a point of air and one of earth out of the aura for each, and waits four seconds after it has been beaten down before it starts. Amulet, ring and girdle hold eight, five and ten.",
                 "부적·호박·마법부여된 천으로 장신구에 새긴 룬이 착용자 위에 보호막을 세운다. 자기 체력보다 먼저 깎이는 몇 번의 타격이고, 황금 하트로 보인다. 스스로 다시 차는데 2초마다 1씩, 그때마다 오라에서 공기와 흙을 1씩 끌어온다. 다 깎이면 4초 기다렸다가 다시 차기 시작한다. 부적·반지·허리띠가 각각 8·5·10을 든다."},
                {"runic_special", "Specialized Runic Baubles", "특수한 룬 장신구",
                 "Each of these gives up a little shield for something it does. The charged ring holds four, and each one worn brings the shield back a quarter faster. The revitalizing ring holds four, and when the shield is beaten down it mends its wearer for twelve seconds, once in twenty. The kinetic girdle holds nine, and when the shield is beaten down it throws everything near away in a blast that breaks nothing, once in twenty seconds. The emergency amulet holds seven, and when the shield is beaten down it puts eight back at once, once a minute.",
                 "각각 보호막을 조금 내주고 그 대신 무언가를 한다. 충전된 반지는 4를 들고, 하나 낄 때마다 보호막이 4분의 1씩 빨리 찬다. 회복의 반지는 4를 들고, 보호막이 깎이면 착용자를 12초 동안 낫게 한다(20초에 한 번). 반동의 허리띠는 9를 들고, 보호막이 깎이면 아무것도 부수지 않는 폭발로 가까운 것을 날려 보낸다(20초에 한 번). 비상 부적은 7을 들고, 보호막이 깎이면 8을 곧바로 되돌린다(1분에 한 번)."},
                {"runic_augmentation", "Runic Shielding Augmentation", "룬 보호막 강화",
                 "Any of the mod's armour and any bauble can be worked on the altar to hold one more point of shield: a diamond and salis mundus round it, one salis mundus more for every point it already holds, and essentia that doubles with every point. It can be done again and again, for as long as the essentia can be found.",
                 "이 모드의 갑옷이나 장신구라면 무엇이든 제단에서 보호막을 1 더 들게 할 수 있다. 둘레에 다이아몬드와 소금 문디를 놓되 이미 든 보호막마다 소금 문디를 하나씩 더 놓고, 에센시아는 1마다 두 배가 된다. 에센시아를 구할 수 있는 한 몇 번이고 거듭할 수 있다."},
                {"verdant_heart", "Verdant Heart Band", "푸른 심장 반지",
                 "A band that holds two hundred of life, and spends it once a second on whatever ails its wearer: twenty to lift withering, ten to lift poison, five to lift taint poison. It cannot mend a wound. It is charged like a wand -- on a recharge pedestal, or by an amulet of vis -- out of earth and water in the aura.",
                 "생명을 이백 담아 두었다가 1초마다 착용자를 괴롭히는 것에 쓰는 반지다. 시듦을 걷는 데 20, 독에 10, 오염 중독에 5. 상처를 낫게 하지는 못한다. 완드처럼 충전 받침대나 비스 부적으로 오라의 흙과 물에서 충전한다."},
                {"verdant_heart_lifegiver", "Lifegiver & Sustainer", "생명을 주는 것과 지탱하는 것",
                 "A verdant heart can be worked once more, into one of two things, not both. A lifegiver spends five to mend a point of its wearer's health. A sustainer spends one to give back breath to someone drowning, or else to feed someone hungry. The sustainer wants a triple meat treat, which is sugar and three meats.",
                 "푸른 심장은 한 번 더, 둘 중 하나로만 다듬을 수 있다. 생명을 주는 것은 5를 써서 착용자의 체력을 1 낫게 한다. 지탱하는 것은 1을 써서 물에 빠진 사람에게 숨을 돌려주거나, 그렇지 않으면 배고픈 사람을 먹인다. 지탱하는 것에는 설탕과 세 가지 고기로 만든 세 고기 간식이 든다."},
                {"thaumostatic_girdle", "Thaumostatic Girdle", "알케모스태틱 허리띠",
                 "A belt worked with air and motion that takes a third of a block off every fall for every moment its wearer falls, which is enough that most falls do no harm at all.",
                 "공기와 움직임을 엮은 허리띠다. 착용자가 떨어지는 동안 매 순간 낙하 거리를 3분의 1블록씩 덜어 주어, 웬만한 낙하는 아무 해도 없다."},
                {"vis_amulet", "Portable Vis Recharging", "들고 다니는 비스 충전",
                 "An amulet that draws vis out of the aura where its wearer stands and puts it into what they carry, four times a second: the wands in the hotbar first, then whatever else is worn that a pedestal could charge. The vis stone sometimes found does the same, slowly.",
                 "착용자가 선 곳의 오라에서 비스를 끌어와 지닌 것에 1초에 네 번 넣는 부적이다. 단축바의 완드부터, 그다음 충전 받침대가 충전할 수 있는 착용한 것. 가끔 발견되는 비스 돌도 같은 일을 느리게 한다."},
                {"enter_outer", "The Outer Lands", "바깥 땅",
                 "You have gone through. Beyond the altar is a maze cut out of a black rock that nothing breaks and that the stars show through, and the fog never lifts. Break through to the rock and the void behind it pours out and pulls at you. Somewhere in the maze a runed tablet lies on a pedestal under guard, and somewhere a great hall is sealed with a lock it fits. The way back is the portal you came in by.",
                 "건너왔다. 제단 너머에는 아무것도 부수지 못하고 별이 비쳐 보이는 검은 바위를 깎아 낸 미로가 있고, 안개는 걷히지 않는다. 그 바위까지 뚫으면 뒤에 있던 공허가 쏟아져 나와 몸을 끌어당긴다. 미로 어딘가에 룬 석판이 받침대 위에 지켜지고 있고, 어딘가에는 그것이 맞는 자물쇠로 잠긴 큰 방이 있다. 돌아가는 길은 들어온 차원문이다."},
                {"crimson", "The Crimson Cult", "진홍 교단",
                 "Most of the book is in a crabbed script you cannot read, but enough of it is plain to say what the cult is after. Nobody knows where they came from. They are working towards a rite they call Apertis Oculis, the Opening of the Eye. What opening it would do, the book does not say; you doubt very much that it is anything good. Knights in crimson plate and clerics in crimson robes come through rips in the air wherever the cult has a mind to be, and fight as one.",
                 "책 대부분은 읽을 수 없는 꼬부랑 글씨지만, 교단이 무엇을 노리는지 알 만큼은 읽힌다. 그들이 어디서 왔는지는 아무도 모른다. 그들은 '아페르티스 오쿨리스', 곧 눈 뜨기라 부르는 의식을 완성하려 한다. 무엇을 여는 의식인지 책은 말하지 않는다. 좋은 것일 리는 없을 것 같다. 진홍 판금의 기사와 진홍 로브의 사제가 교단이 마음먹은 곳마다 허공의 틈으로 건너와 한 몸처럼 싸운다."},
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
                {"aura_totem", "Basic Aura Manipulation", "기초 오라 조작",
                 "It is high time the things you have learned about the aura were put to use. The Totem of Dispersal takes vis from round itself and spreads it over a wider area. Feed it vis shards: each burns for a while, and says what kind of vis it moves -- a balanced shard moves all six primals. Meddling with the aura is not without risk, and now and then what it moves turns to flux instead. A totem works on an inner area, alone its own chunk, and an outer one, alone the eight chunks round that; the totem of dispersal moves vis from the inner out to the outer. Up to five totem poles can stand under it. Each inner pole widens the inner area by a chunk all round, each outer pole the outer, and either makes it work a little faster -- and a little likelier to make flux. A dispersal totem with one inner and two outer poles draws from the three chunks by three about it and spreads into a ring three chunks wide beyond that.",
                 "오라에 대해 알게 된 것을 쓸 때가 되었다. 흩어 놓는 오라 토템은 제 둘레의 비스를 가져다 더 넓은 곳에 펴 놓는다. 비스 조각을 먹이면 조각 하나가 한동안 타며 어떤 비스를 옮길지 정한다. 균형 잡힌 조각은 원시 여섯을 다 옮긴다. 오라를 건드리는 데는 위험이 따라서, 이따금 옮기던 것이 플럭스로 바뀐다. 토템은 안쪽 구역과 바깥 구역에서 일한다. 혼자일 때 안쪽은 제 청크, 바깥은 그 둘레 여덟 청크다. 흩어 놓는 토템은 안쪽에서 바깥으로 비스를 옮긴다. 토템 밑에는 토템 기둥을 다섯까지 세울 수 있다. 안쪽 기둥 하나마다 안쪽 구역이 사방으로 한 청크씩, 바깥쪽 기둥은 바깥 구역이 넓어지고, 어느 쪽이든 조금 빨라지는 대신 플럭스가 날 가능성도 조금 커진다. 안쪽 기둥 하나와 바깥쪽 기둥 둘을 세운 흩어 놓는 토템은 둘레 3×3 청크에서 끌어다 그 바깥 세 청크 너비의 고리에 편다."},
                {"aura_totem_pull", "Aura Totem of Attraction", "끌어모으는 오라 토템",
                 "The Totem of Attraction works much as the Totem of Dispersal does, but the other way: it draws vis in from the outer area and moves it into the inner.",
                 "끌어모으는 오라 토템은 흩어 놓는 토템과 거의 같이 일하지만 방향이 반대다. 바깥 구역에서 비스를 끌어와 안쪽 구역으로 옮긴다."},
                {"aura_totem_pole_pure", "Stabilizing Totem Pole", "안정시키는 토템 기둥",
                 "A totem pole that steadies the totem it holds up, making it less likely to spoil what it moves into flux. It neither widens nor quickens anything.",
                 "떠받친 토템을 안정시키는 토템 기둥이다. 옮기던 것이 플럭스로 상할 가능성을 줄인다. 구역을 넓히지도 빠르게 하지도 않는다."},
                {"recharge_pedestal", "Recharge Pedestal", "재충전 받침대",
                 "Travelling the countryside to fill a wand is all very well, but a pedestal that does it for you is better. Set a wand on it and it draws vis from the aura of its chunk to fill it, as much as a wand held in the hand would, twice a second. Like a wand in the hand, it leaves alone any aura run down below a tenth of what it should be. A hopper can set a wand on it and take one off.",
                 "지팡이를 채우러 들판을 돌아다니는 것도 좋지만, 대신 해 주는 받침대가 더 낫다. 지팡이를 올려 두면 제 청크의 오라에서 비스를 끌어 채운다. 손에 든 지팡이만큼씩, 1초에 두 번이다. 손에 든 지팡이처럼, 원래의 10분의 1 밑으로 떨어진 오라는 건드리지 않는다. 깔때기로 지팡이를 올리고 내릴 수도 있다."},
                {"dioptra", "Alchemic Dioptra", "연금 디옵트라",
                 "A device that shows the aura round it plainly. Set a vis shard in the crystal on its top and it draws, in the air over itself, a map thirteen chunks across of how strong that aspect is in each; a balanced shard shows whichever primal is strongest in each chunk instead. Chunks with a node in them sparkle now and then -- not precise, but a help in finding them. A comparator reads how strong the aura is in its own chunk, and a redstone signal puts it out.",
                 "둘레의 오라를 한눈에 보여 주는 장치다. 위쪽 결정에 비스 조각을 끼우면 13청크 너비의 지도를 제 위 공중에 그려, 각 청크에서 그 상이 얼마나 센지 보여 준다. 균형 잡힌 조각을 끼우면 청크마다 가장 센 원시 상을 보여 준다. 노드가 있는 청크는 이따금 반짝인다. 정확하지는 않아도 노드를 찾는 데 도움이 된다. 비교기를 대면 제 청크의 오라 세기를 읽고, 레드스톤 신호가 들어오면 꺼진다."},
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
                 "Gears and a comparator under glass, plated in brass. It is not a mind in any way that matters, but it will follow one rule without being told twice, which is all a machine needs -- a golem most of all. Golems are made at a golem press. Stand a piston facing up with iron bars on it, a stone table beside it, an anvil beside the table and a cauldron beside the anvil and the piston, the four making a square, and strike any of it with a wand. Pick at the press what the golem is to be made of and the parts it is to have: it takes what they need from your pack, and presses it a measure of machina at a time, drawn up a tube or straight out of a jar set against it.",
                 "놋쇠를 댄 유리 속의 톱니와 비교기다. 따질 만한 의미의 두뇌는 아니지만 한 가지 규칙을 두 번 말하지 않아도 따른다. 기계에 필요한 것은 그것뿐이고, 골렘은 더욱 그렇다. 골렘은 골렘 프레스에서 만든다. 위를 보는 피스톤 위에 철창을 얹고, 그 옆에 돌 탁자, 탁자 옆에 모루, 모루와 피스톤 옆에 가마솥을 놓아 넷이 네모를 이루게 한 뒤, 어디든 완드로 친다. 프레스에서 골렘을 무엇으로 만들고 어떤 부품을 달지 고르면, 필요한 것을 가방에서 가져가 마키나를 한 단위씩 들여 찍어 낸다. 마키나는 관으로 끌어 올리거나 옆에 붙인 단지에서 곧장 빨아들인다."},
                {"bioalchemic_mind", "Bioalchemic Mind", "생체연금 두뇌",
                 "The clockwork mind with something living grown into it on the altar. What comes out can be told to choose rather than only to follow, and the things that have to decide for themselves -- an autocaster picking its mark -- are built round one.",
                 "태엽 두뇌에 제단에서 살아 있는 것을 자라 들게 한 것이다. 나온 것은 따르기만 하는 것이 아니라 고르라고 시킬 수 있다. 스스로 정해야 하는 것들 — 노릴 것을 고르는 자동 시전기 같은 — 은 이것을 둘러싸고 짓는다."},
                {"mirror", "Mirror Magic", "거울 마법",
                 "Two mirrors made a pair pass to each other whatever is thrown or piped into either -- even from one world to another -- but never anything living. Put one up, hold a second against it, then put the second up wherever it should open. A pair is two and only two. A mirror taken down remembers its partner, and either can be put up again elsewhere. Moving things strains the aura, and the more is moved the more flux it lets out.",
                 "짝을 이룬 두 거울은 어느 한쪽에 던지거나 관으로 넣은 것을 서로 건넨다. 다른 세계로도 건넨다. 살아 있는 것은 건네지 않는다. 하나를 걸고, 둘째 거울을 그것에 대었다가, 둘째를 열고 싶은 곳에 건다. 짝은 둘뿐이다. 떼어 낸 거울은 짝을 기억하니 어느 쪽이든 다른 곳에 다시 걸 수 있다. 건네는 일은 오라를 긁고, 많이 건넬수록 플럭스가 더 샌다."},
                {"mirror_hand", "Magical Hand Mirror", "마법 손거울",
                 "A mirror on a handle. Held against a magic mirror it remembers it, even one already paired; opened anywhere, whatever is put in it comes out of that mirror. It only sends, never receives.",
                 "손잡이 달린 거울이다. 마법 거울에 대면 그것을 기억한다. 이미 짝이 있는 거울이어도 된다. 어디서든 열어서 넣은 것은 그 거울로 나온다. 보내기만 하고 받지는 않는다."},
                {"mirror_essentia", "Essentia Mirrors", "에센시아 거울",
                 "Essentia cannot be piped through a mirror. But an essentia mirror stands in for whatever essentia is held within eight blocks in front of its partner, so that an altar or a lamp reaching for essentia near one draws on jars by the other. It strains the aura as the other mirrors do.",
                 "에센시아는 거울로 관을 통해 넣을 수 없다. 하지만 에센시아 거울은 짝 거울 앞 여덟 칸 안에 담긴 에센시아를 대신해 준다. 한쪽 근처에서 에센시아를 찾는 제단이나 램프가 다른 쪽 단지에서 끌어 쓴다. 다른 거울처럼 오라를 긁는다."},
                {"infernal_furnace", "Infernal Furnace", "지옥의 화로",
                 "A furnace of obsidian and nether brick round a heart of lava, which needs no fuel. Build it three blocks every way: nether brick at the corners, obsidian on the edges and the floor, still lava in the middle with air above it, and iron bars in place of one of the four obsidian blocks beside the lava. Work it with a wand. Things dropped in through the top are smelted and spat out where the bars were; anything that will not smelt is burned to nothing. It is slow on its own, and much quicker on fire drawn out of the air; bellows blowing into it from two blocks out make it quicker still. Ores come out with a nugget besides now and then, more often with bellows. A little flux escapes now and again.",
                 "흑요석과 네더 벽돌로 용암의 심장을 감싼 화로로, 연료가 필요 없다. 사방 세 칸으로 쌓는다. 모서리는 네더 벽돌, 변과 바닥은 흑요석, 가운데는 위가 비어 있는 고인 용암, 그리고 용암 옆 흑요석 넷 가운데 하나 자리에 철창. 완드로 다룬다. 위로 떨어뜨린 것은 녹여서 철창이 있던 쪽으로 뱉는다. 녹지 않는 것은 타서 없어진다. 혼자서는 느리지만 공기의 불을 끌어 쓰면 훨씬 빠르고, 두 칸 밖에서 안으로 부는 풀무가 있으면 더 빠르다. 광석은 이따금 덩어리를 하나 더 내놓고, 풀무가 있으면 더 자주 그런다. 이따금 플럭스가 조금 새어 나온다."},
                {"arcane_bore", "Arcane Bore", "비전 굴착기",
                 "A machine that carries an excavation focus further than any wand, and without anyone standing over it. It goes directly above or below its base; point it with a wand and give it a redstone signal, and it eats a tunnel out the way it faces, sixty-four blocks deep, sweeping a spiral as it goes. It wants an excavation focus in its left slot and any pickaxe in its right: whatever is worked into either -- force, efficiency, widening, treasure, fortune, silk touch, dowsing -- goes into what it mines. The pickaxe wears a point a block, and the bore stops while it has one left. Everything mined comes out of the base's nozzle, into a container there if there is one; a wand on the base turns the nozzle. Entropy piped into the base drives it fast, twenty blocks a point; without it, it draws entropy out of the air, a block a point and far slower.",
                 "완드로는 닿지 않는 데까지, 지켜보는 사람 없이도 발굴 포커스를 부리는 기계다. 받침 바로 위나 아래에 놓는다. 완드로 방향을 잡고 레드스톤 신호를 주면 바라보는 쪽으로 나선을 그리며 예순네 칸 깊이의 굴을 판다. 왼쪽 칸에 발굴 포커스를, 오른쪽 칸에 아무 곡괭이나 넣는다. 둘에 깃든 것 — 힘, 효율, 넓히기, 보물, 행운, 섬세한 손길, 탐광 — 이 캐는 것에 그대로 들어간다. 곡괭이는 한 칸마다 한 점씩 닳고, 한 점 남으면 굴착기가 멈춘다. 캔 것은 모두 받침의 주둥이로 나온다. 거기 상자가 있으면 상자로. 받침을 완드로 치면 주둥이가 돈다. 받침에 관으로 혼돈을 넣으면 한 점에 스무 칸을 빠르게 판다. 없으면 공기에서 혼돈을 끌어 쓰는데, 한 점에 한 칸이고 훨씬 느리다."},
                {"autocaster", "Autocaster", "자동 시전기",
                 "A focus on a bracket, with enough of a mind in it to pick something and point itself at it. It is not a thaumaturge: it draws its own vis out of the air a drop at a time, holds a hundred of each primal at most, and nothing worn or carried makes a working any cheaper for it. Seven of the twelve foci are worth aiming at a thing, and those are the seven it will take. It goes against any face -- floor, wall or ceiling -- and it is told what to mind: animals, monsters, people, and whatever is already yours. A wand clicked against it takes it back down, focus and all.",
                 "받침대에 얹은 포커스다. 무엇을 고르고 그쪽으로 제 몸을 돌릴 만큼의 생각이 들어 있다. 마법사는 아니다. 제 비스를 공기에서 한 방울씩 길어 올리고, 원시마다 백까지만 담으며, 무엇을 걸쳤든 들었든 값이 싸지는 일은 없다. 열둘 중 일곱만이 무언가를 겨눌 값어치가 있고, 받아 주는 것도 그 일곱이다. 바닥이든 벽이든 천장이든 어느 면에나 붙으며, 무엇을 노릴지 일러 줄 수 있다 — 동물, 몹, 사람, 그리고 이미 제 편인 것. 완드로 치면 포커스째 도로 떼어 낸다."},
                {"golem_wood", "Wooden Golems", "나무 골렘",
                 "A golem is a made thing, pressed whole out of a frame and the parts fitted to it, and it belongs to whoever sets it down. Everyone starts with greatwood: light and quick, and not much to it. What a golem is made of decides how much it can take, how hard it hits, and something of what it is like.",
                 "골렘은 몸체와 거기 끼울 부품을 한꺼번에 찍어 만드는 것이고, 내려놓은 사람의 것이 된다. 누구나 거대나무로 시작한다. 가볍고 빠르지만 단단하지는 않다. 무엇으로 만드느냐가 얼마나 버티는지, 얼마나 세게 치는지, 그리고 어떤 성질인지를 얼마간 정한다."},
                {"golem_iron", "Iron Golems", "철 골렘",
                 "Iron plate makes a golem sturdy, fireproof and hard to blow apart, at the cost of being heavy and slow. Three iron ingots in a row make three plates.",
                 "철 판으로 만든 골렘은 튼튼하고, 불에 타지 않고, 폭발에도 잘 견딘다. 대신 무겁고 느리다. 철 주괴 셋을 한 줄로 놓으면 판 셋이 된다."},
                {"golem_clay", "Clay Golems", "점토 골렘",
                 "Terracotta is not strong, but a golem baked of it does not burn, and is no heavier for that.",
                 "테라코타는 강하지 않지만, 그것으로 구운 골렘은 불에 타지 않고 그렇다고 무겁지도 않다."},
                {"golem_brass", "Brass Golems", "황동 골렘",
                 "Brass is not as sturdy as iron and it burns, but it lets a frame be built light, and a brass golem is quick. Knowing brass is also how fine manipulators are made.",
                 "황동은 철만큼 튼튼하지 않고 불에도 타지만, 몸체를 가볍게 지을 수 있어 황동 골렘은 빠르다. 정밀 조작기도 황동을 알아야 만든다."},
                {"golem_alchemium", "Alchemium Golems", "알케미움 골렘",
                 "Everything iron is and more of it: an alchemium golem takes more, is better armoured and hits harder, and is just as heavy.",
                 "철이 가진 것에 더해, 알케미움 골렘은 더 잘 버티고 방어도 좋고 더 세게 친다. 무겁기는 마찬가지다."},
                {"golem_void", "Void Metal Golems", "공허 금속 골렘",
                 "Void metal is a little softer than iron and nowhere near as heavy, and a golem made of it mends itself more than twice as fast as any other. Its gears are made as the others are, of nuggets round an iron ingot.",
                 "공허 금속은 철보다 조금 무르고 훨씬 가벼우며, 그것으로 만든 골렘은 어느 골렘보다 두 배 넘게 빨리 스스로를 고친다. 톱니바퀴는 다른 것처럼 철 주괴 둘레에 조각을 둘러 만든다."},
                {"golem_direct", "Directing Golems", "골렘 부리기",
                 "A golem left to itself keeps within sixteen blocks of where it was set down. Ring a golemancer's bell at one of your own and it follows you instead; ring it again and it stays where it is, and that is its home from then on. A golem answers only to its owner. A wand put to it folds it back into an item, with all it has learned, and a dye colours the little flag on its back.",
                 "그냥 둔 골렘은 놓인 곳에서 열여섯 칸 안을 지킨다. 제 골렘에게 골렘술사의 종을 울리면 대신 당신을 따라오고, 한 번 더 울리면 그 자리에 머물며 그때부터 그곳이 집이 된다. 골렘은 주인의 말만 듣는다. 완드를 대면 배운 것까지 그대로 다시 아이템으로 접히고, 염료를 대면 등의 작은 깃발이 그 색으로 물든다."},
                {"golem_vision", "Golem Vision", "골렘의 눈",
                 "Glass bottles and fermented spider eyes set in brass make a vision module, a pair of eyes for a golem's head. A golem that sees further works further from home, thirty-two blocks rather than sixteen, though a head with such eyes in it is fragile.",
                 "유리병과 발효된 거미 눈을 황동에 박으면 시야 모듈, 곧 골렘 머리의 눈 한 쌍이 된다. 더 멀리 보는 골렘은 집에서 더 멀리, 열여섯 칸이 아니라 서른두 칸까지 일하러 간다. 다만 그런 눈을 단 머리는 연약하다."},
                {"golem_climber", "Climbing Golems", "등반 골렘",
                 "Legs fitted with flint grips go straight up whatever is in the way, and a golem on them is never hurt by a fall.",
                 "부싯돌 손잡이를 단 다리는 앞을 막는 것을 곧장 타고 오르고, 그 다리를 단 골렘은 떨어져 다치는 일이 없다."},
                {"golem_flyer", "Flying Golems", "나는 골렘",
                 "An arcane levitator under a golem floats it wherever it goes. It is a third slower for it, and fragile, but nothing on the ground is in its way and it never comes to harm by falling.",
                 "골렘 밑에 비전 부양기를 달면 어디든 떠서 다닌다. 그만큼 삼분의 일 느려지고 연약해지지만, 땅 위의 무엇도 길을 막지 못하고 떨어져 다치는 일도 없다."},
                {"golem_breaker", "Breaker Arms", "파쇄 팔",
                 "Arms ending in diamond-tipped grinders, driven by pistons. A golem with them is clumsy and hits hard, and can break most blocks.",
                 "피스톤으로 돌리는 다이아몬드 끝 분쇄기가 달린 팔이다. 이 팔을 단 골렘은 서투르고 세게 치며, 대부분의 블록을 부술 수 있다."},
                {"tallow", "Magic Tallow", "마법 수지",
                 "Rotten flesh stewed in a crucible with a little fire renders down to a clean, waxy tallow, which holds an arcane charge well. It is what control seals are made with, and a wick of string set in two of it makes three candles, which a dye colours and bone meal bleaches again.",
                 "썩은 살점을 불을 조금 넣은 도가니에서 고면 깨끗한 밀랍 같은 수지가 된다. 비전의 기운을 잘 붙든다. 제어 봉인을 만드는 데 쓰고, 실 심지를 수지 둘에 박으면 양초 셋이 된다. 염료로 물들이고 뼛가루로 다시 하얗게 한다."},
                {"control_seals", "Golem Control Seals", "골렘 제어 봉인",
                 "Golems are given work with seals: clay and tallow worked with a red dye and a nitor, then steeped in a crucible into a seal that does one thing. Stuck to the face of a block, a seal offers tasks to any golem nearby that can do them, the nearest and most urgent first. Hold a seal, a golem or a golemancer's bell to see the seals about you, and crouch to see them through walls. Ring the bell at a seal to set it up: how urgent it is, whether only golems of one colour work it, whether only your own golems may, whether redstone stops it, and whatever else that kind of seal has. Ring it crouching to take the seal down.",
                 "골렘에게는 봉인으로 일을 준다. 점토와 수지에 붉은 염료와 니토르를 섞어 만든 빈 봉인을 도가니에서 우려 한 가지 일을 하는 봉인으로 만든다. 블록 면에 붙인 봉인은 그 일을 할 수 있는 근처 골렘에게 일감을 내놓고, 가깝고 급한 것부터 맡긴다. 봉인이나 골렘이나 골렘술사의 종을 들면 둘레의 봉인이 보이고, 웅크리면 벽 너머로도 보인다. 봉인에 종을 울리면 설정판이 열린다. 얼마나 급한지, 한 색 골렘만 일할지, 내 골렘만 일할지, 레드스톤에 멈출지, 그리고 그 봉인만의 설정을 고른다. 웅크리고 울리면 봉인을 떼어 낸다."},
                {"seal_collect", "Control Seal: Collect", "제어 봉인: 수집",
                 "Golems pick up whatever lies still in the area in front of the seal and its filter lets by, as much as they can carry. The area can be widened up to eight blocks each way. The advanced seal, steeped again with mind and senses, has nine filter entries and its own options, and only smart golems work it. Clumsy golems cannot pick anything up.",
                 "봉인 앞 범위에 가만히 놓인 것 가운데 필터가 들이는 것을, 골렘이 들 수 있는 만큼 줍는다. 범위는 각 방향으로 여덟 칸까지 넓힐 수 있다. 정신과 감각으로 한 번 더 우린 상급 봉인은 필터가 아홉 칸이고 옵션을 고를 수 있으며, 영리한 골렘만 일한다. 서투른 골렘은 줍지 못한다."},
                {"seal_store", "Control Seal: Store", "제어 봉인: 보관",
                 "Golems carrying something the filter lets by bring it here and put it in, through the face the seal is on; on something that is not a container they drop it in front. With a whitelist, how many of each thing are on the filter is how many are wanted here, and no more are brought; click a filter square with nothing in hand to raise it and right-click to lower it. The advanced seal can be set to bring only what is already there, and only smart golems work it.",
                 "필터가 들이는 것을 든 골렘이 그것을 여기로 가져와 봉인이 붙은 면으로 넣는다. 통이 아닌 것에 붙어 있으면 앞에 떨군다. 허용 목록이면 필터에 놓인 개수가 여기 있어야 할 개수이고, 그보다 많이는 가져오지 않는다. 빈손으로 필터 칸을 누르면 늘고 오른쪽을 누르면 준다. 상급 봉인은 이미 들어 있는 것만 가져오게 할 수 있고, 영리한 골렘만 일한다."},
                {"seal_empty", "Control Seal: Empty", "제어 봉인: 비우기",
                 "Stuck on a container, golems take out of it whatever the filter lets by, as much as they can carry, through the face the seal is on. What they then do with it is up to other seals. It can be set to leave one of each thing behind. The advanced seal can work through its whitelist an entry at a time, and only smart golems work it.",
                 "통에 붙이면 골렘이 필터가 들이는 것을 들 수 있는 만큼 봉인이 붙은 면으로 꺼낸다. 그다음 그것을 어떻게 할지는 다른 봉인이 정한다. 종류마다 하나씩은 남기게 할 수 있다. 상급 봉인은 허용 목록을 한 칸씩 차례로 처리할 수 있고, 영리한 골렘만 일한다."},
                {"seal_provide", "Control Seal: Provide", "제어 봉인: 공급",
                 "Stuck on a container, it answers other seals within sixty-four blocks that need something to work with: when one asks for something this seal's filter lets by and the container has it, a golem that works for the asking seal fetches it out. It can hand out one at a time and leave one behind. Only smart golems work it.",
                 "통에 붙이면 64칸 안에서 일할 거리가 필요한 다른 봉인에 답한다. 어떤 봉인이 이 봉인의 필터가 들이는 것을 청하고 통에 그것이 있으면, 청한 봉인을 위해 일하는 골렘이 그것을 꺼내 간다. 한 번에 하나씩 내주거나 하나는 남기게 할 수 있다. 영리한 골렘만 일한다."},
                {"seal_guard", "Control Seal: Guard", "제어 봉인: 경비",
                 "An aggression module -- blaze powder behind glass, on brass -- gives a golem the will to fight, and a guarding seal tells it what at. Fighting golems go for whatever monster comes into the seal's area. The advanced seal, for smart golems, can be set on animals too, and on players where the server allows it.",
                 "공격 모듈 — 황동 위에 유리로 가둔 블레이즈 가루 — 은 골렘에게 싸울 마음을 주고, 경비 봉인은 무엇과 싸울지를 일러 준다. 싸우는 골렘은 봉인의 범위에 들어온 괴물에게 덤벼든다. 영리한 골렘을 위한 상급 봉인은 동물에게도, 서버가 허락하면 플레이어에게도 덤비게 할 수 있다."},
                {"golem_combat", "Golem Combat", "골렘 전투",
                 "With the aggression module worked out, golems can be built for fighting: claw arms that snap shut and hit hard, dart launchers that make their own darts and shoot from up to sixteen blocks, armour plating of the golem's own material, an armoured smart head that is no longer fragile, and an aggression module fitted to any golem at all.",
                 "공격 모듈을 알고 나면 골렘을 싸우도록 지을 수 있다. 세게 치며 닫히는 집게 팔, 다트를 스스로 지어 열여섯 칸까지 쏘는 다트 발사기, 제 재료로 덧댄 장갑판, 더는 연약하지 않은 장갑 두른 영리한 머리, 그리고 어느 골렘에든 다는 공격 모듈."},
                {"seal_butcher", "Control Seal: Butcher", "제어 봉인: 도축",
                 "Every ten seconds, smart fighting golems take one grown animal from any herd of more than two in the seal's area, leaving the young, the tame and the last two alone. A herd is thinned, never ended.",
                 "10초마다 영리한 전투 골렘이 봉인 범위 안에 셋 이상 있는 무리에서 다 자란 동물 하나를 잡는다. 새끼와 길든 것과 마지막 둘은 두고 간다. 무리는 줄어들 뿐 사라지지 않는다."},
                {"seal_use", "Control Seal: Use", "제어 봉인: 사용",
                 "A deft, smart golem uses what it carries on the block the seal is stuck to, through the face it is on, as a player would with a right click, or a left click if set so. It can use an empty hand, click crouching, work only where the block has gone, and ask providing seals for whatever is on its whitelist. What the click leaves in its hand it keeps.",
                 "손재주 있고 영리한 골렘이 든 것을, 봉인이 붙은 블록에 그 면으로, 플레이어가 오른쪽 클릭하듯 쓴다. 설정하면 왼쪽 클릭으로 쓴다. 빈손으로도 쓰게, 웅크리고 클릭하게, 블록이 없어진 곳에서만 일하게, 허용 목록의 것을 공급 봉인에 청하게 할 수 있다. 클릭하고 손에 남은 것은 골렘이 지닌다."},
                {"seal_harvest", "Control Seal: Harvest", "제어 봉인: 수확",
                 "Deft, smart golems take whatever has ripened in the seal's area: grown crops, melons and pumpkins, the upper parts of sugar cane and cactus, ripe nether wart and cocoa. Berries are picked and left on the bush. Set to replant, the golem plants again whatever it cut, tilling the ground if it has gone back to dirt, and can ask providing seals for the seed.",
                 "손재주 있고 영리한 골렘이 봉인 범위에서 익은 것을 거둔다. 다 자란 작물, 수박과 호박, 사탕수수와 선인장의 윗동, 익은 네더 사마귀와 코코아. 열매는 따기만 하고 덤불은 둔다. 다시 심기를 켜면 벤 것을 다시 심고, 흙으로 돌아간 땅은 먼저 갈며, 씨앗을 공급 봉인에 청할 수도 있다."},
                {"seal_break", "Control Seal: Block Breaker", "제어 봉인: 블록 부수기",
                 "Golems with grinder arms break every block in the seal's area that its one filter entry lets by. Each block takes half a second of grinding for every three points of its hardness, and drops what it would have to the right tool.",
                 "분쇄기 팔을 단 골렘이 봉인 범위에서 필터 한 칸이 들이는 블록을 모두 부순다. 블록마다 단단함 3당 0.5초씩 갈아야 하고, 알맞은 도구로 캔 것처럼 떨군다."},
                {"seal_lumber", "Control Seal: Lumberjack", "제어 봉인: 벌목",
                 "Smart golems with grinder arms take down every tree whose logs reach into the seal's area, a log at a time from the crown down, so that nothing is ever left hanging.",
                 "분쇄기 팔을 단 영리한 골렘이 통나무가 봉인 범위에 들어온 나무를 모두 쓰러뜨린다. 우듬지부터 통나무 하나씩 내려오므로 허공에 매달려 남는 것이 없다."},
                {"warp", "Warp", "뒤틀림",
                 "Look too long into what should not be, and it begins looking back. The damage is not to the world but to the one studying it, and it does not undo itself with rest.",
                 "있어서는 안 될 것을 오래 들여다보면, 그것도 당신을 들여다보기 시작한다. 상하는 것은 세계가 아니라 그것을 연구하는 자이며, 쉰다고 해서 되돌아오지 않는다."},
                {"essentia_transport", "Essentia Transfusion", "에센시아 수혈",
                 "Aerial essentia. An infusion draws essentia straight out of jars and through the air, and the matrix keeps it stable enough for the working. You have found how to move essentia the same way without it spoiling, in two fittings for a pipe. The filling transfuser takes whatever a pipe brings it and sends it to the jars within sixteen blocks in front of it, filling those that already hold something first. The emptying transfuser does the opposite, drawing from the jars in front of it whatever the pipe behind it asks for.",
                 "공중의 에센시아. 주입은 단지에서 에센시아를 곧장 공중으로 끌어오고, 매트릭스가 그것을 작업할 만큼 안정시킨다. 같은 방식으로 에센시아를 상하게 하지 않고 옮기는 법을 알아내어, 관에 끼우는 장치 둘을 만들었다. 채우는 수혈기는 관이 가져오는 것을 앞쪽 16칸 안의 단지로 보내는데, 이미 무언가 든 단지부터 채운다. 비우는 수혈기는 그 반대로, 뒤의 관이 원하는 것을 앞쪽 단지에서 끌어온다."},
                {"thaumostatic_harness", "Thaumostatic Harness", "타우모스태틱 하네스",
                 "Is it a bird? You have unlocked the secret of flight: a harness that anchors itself to the aura and lets you defy gravity. It runs on vis and has to be charged, at a recharge pedestal or by an amulet of vis; a point each of air and fire keeps it going a second, and it holds five minutes' worth. Press H while wearing it to switch it on and off; switched on, you fly as you would in creative. Sprinting while flying goes faster but burns three times the vis. Worn with a thaumostatic girdle, it flies at full speed. Even switched off, it softens a fall.",
                 "새인가? 비행의 비밀을 풀었다. 오라에 스스로를 붙들어 매어 중력을 거스르게 해 주는 하네스다. 비스로 움직이므로 충전 받침대나 비스 부적으로 채워야 한다. 공기와 불 한 점씩이면 1초를 날고, 5분 몫을 담아 둔다. 입은 채 H를 누르면 켜고 끈다. 켜면 크리에이티브처럼 날 수 있다. 날면서 달리면 빠르지만 비스를 세 배로 쓴다. 타우모스태틱 허리띠를 함께 차면 제 속도로 난다. 꺼져 있어도 떨어질 때 충격을 줄여 준다."},
                {"bottle_taint", "Bottled Taint", "병에 든 오염",
                 "Why would you want this? Cleaning taint is one thing, but spreading it? A phial of flux steeped in more flux and water concentrates the essence of taint into a bottle that need only be thrown. Where it breaks, everything living nearby is poisoned by the taint, and the ground about it goes over to tainted land with flux goo lying in it. Why you would want to do this is up to you.",
                 "이걸 왜 원하겠는가? 오염을 치우는 것과 퍼뜨리는 것은 다른 일이다. 플럭스 약병을 플럭스와 물에 더 우려내면 오염의 정수가 병 하나에 모이고, 던지기만 하면 된다. 깨진 자리 가까이의 산 것은 모두 오염에 중독되고, 그 주변 땅은 오염된 땅으로 바뀌며 플럭스 끈적이가 고인다. 왜 그러고 싶은지는 당신이 정할 일이다."},
                {"bone_bow", "Bone Bow", "뼈 활",
                 "Sticks and stones... A bow can be made of bone and string with entropy worked into its heart. It is drawn fully in half the time a bow takes, and looses by itself if held too long; the arrows it sends fly faster and hit harder, though never as a critical shot.",
                 "막대기와 돌멩이로... 뼈와 실로 활을 짜고 그 한가운데에 엔트로피를 불어넣을 수 있다. 활의 절반 시간이면 끝까지 당겨지고, 너무 오래 쥐고 있으면 저절로 쏜다. 쏘아 보낸 화살은 더 빠르게 날아 더 세게 맞힌다. 다만 치명타는 나지 않는다."},
                {"primal_arrows", "Primal Arrows", "원시 화살",
                 "Arrows each bound with one primal, laid eight round a shard. Air arrows hurt as an arrow does but as magic, past any armour. Fire arrows burn and set their targets alight. Water arrows chill the target to the bone and slow it badly. Earth arrows simply hit a great deal harder, and knock back further. Order arrows hit a little less but past armour, and leave the target weak. Entropy arrows hit a little less and wither what they strike. A bow reaches for them before any other arrow. They cannot be picked up again, and Infinity spares one only a time in three.",
                 "화살마다 원시 하나를 묶은 것으로, 조각 하나에 화살 여덟을 둘러 만든다. 공기 화살은 화살만큼 다치게 하되 마법이라 어떤 갑옷도 막지 못한다. 불 화살은 불로 다치게 하고 맞은 것에 불을 붙인다. 물 화살은 뼛속까지 얼려 크게 느리게 한다. 흙 화살은 그저 훨씬 세게 맞히고 더 멀리 밀쳐 낸다. 질서 화살은 조금 약하지만 갑옷을 뚫고 맞은 것을 나약하게 한다. 엔트로피 화살은 조금 약하지만 맞은 것을 시들게 한다. 활은 다른 화살보다 이것을 먼저 집는다. 다시 주울 수 없고, 무한은 세 번에 한 번만 아껴 준다."},
                {"liquid_death", "Liquid Death", "액체 죽음",
                 "For external use only. Death, crystal and entropy steeped together in a crucible and caught in a bucket make a liquid so toxic that almost nothing keeps it from dissolving flesh, bone and harder things. Nothing alive, or with any semblance of life, survives it. What essentia a creature held as it dissolved is said to settle out as crystals.",
                 "외용으로만 쓸 것. 죽음·결정·엔트로피를 도가니에서 함께 우려 양동이에 받으면, 살과 뼈와 그보다 단단한 것까지 녹여 버리는 지독한 액체가 된다. 살아 있는 것, 살아 있는 척이라도 하는 것은 아무것도 견디지 못한다. 녹아 버린 것이 지녔던 에센시아는 결정으로 가라앉는다고 한다."},
                {"sanity_soap", "Sanitizing Soap", "정신 정화 비누",
                 "The body can be scrubbed clean, so why not the mind? A block of flesh steeped in a crucible with knowledge, the alien, order and life makes a soap. Hold it and wash for ten seconds: all temporary warp goes, and there is a small chance a point of the warp that sticks goes with it. Warp ward, or standing in purifying fluid while you wash, makes that much likelier. Warp that has been written into you for good is beyond any soap.",
                 "몸을 씻을 수 있다면 정신이라고 못 씻을까. 살덩이 블록을 도가니에서 지식·이질·질서·생명과 함께 우려내면 비누가 된다. 들고 10초 동안 씻으면 일시적 뒤틀림이 모두 씻겨 나가고, 작은 확률로 들러붙은 뒤틀림도 1 줄어든다. 뒤틀림 방호를 받았거나 정화의 액체 속에 서서 씻으면 그 확률이 크게 오른다. 영원히 새겨진 뒤틀림은 어떤 비누로도 지워지지 않는다."},
                {"pattern_crafter", "Arcane Pattern Crafter", "비전 문양 제작기",
                 "Crafts that only ever need one kind of thing laid in a pattern, such as planks into sticks or nuggets into an ingot, can be left to a machine. Put an inventory above it and one below: it takes what it needs from the one above once a second, lays it out in the pattern its window shows, and puts whatever that makes into the one below. Click the brass dial on its face to choose the next pattern. A redstone signal stops it.",
                 "판자를 막대로, 조각을 주괴로 만드는 것처럼 한 가지 재료만 문양대로 놓으면 되는 제작은 기계에 맡길 수 있다. 위아래에 보관함을 하나씩 두면, 1초마다 위 보관함에서 필요한 만큼 가져와 창에 보이는 문양대로 늘어놓고, 만들어진 것을 아래 보관함에 넣는다. 앞면의 황동 다이얼을 누르면 다음 문양으로 바뀐다. 레드스톤 신호를 받으면 멈춘다."},
                {"sinister_stone", "Sinister Lodestone", "불길한 자석",
                 "Studying the rite of the opening eye, you found that the place where the eye opens gives off a distinct energy even while it is shut. Flint infused with the right things lights up whenever it is pointed towards such a place, through stone and all. Its reach is limited, but the ritual stones are hard enough to find that it is worth carrying.",
                 "눈이 열리는 의식을 연구하다가, 눈이 열리는 자리는 닫혀 있을 때도 뚜렷한 기운을 낸다는 것을 알아냈다. 알맞은 것을 주입한 부싯돌은 그런 자리를 향하면 돌벽 너머라도 빛난다. 닿는 거리에는 한계가 있지만, 의식의 돌을 찾기가 워낙 어려워 들고 다닐 만하다."},
                {"focus_fire", "Wand Foci", "완드 포커스",
                 "Vis shards laid round a heart of quartz align into a matrix that shapes vis to one task. Fitted to a wand, such a focus is what the wand does when it is used; press the focus key with a wand in hand to choose among those you carry. The first anybody learns is fire, a cone of elemental flame sprayed for as long as the wand is held.",
                 "석영 심을 둘러 비스 조각을 놓으면 비스를 한 가지 일로 빚는 틀로 정렬된다. 완드에 끼우면 그 포커스가 완드를 쓸 때 하는 일이 된다. 완드를 든 채 포커스 키를 누르면 가진 포커스 중에서 고를 수 있다. 누구나 처음 배우는 것은 불로, 완드를 쥐고 있는 동안 원소의 불길을 원뿔 모양으로 뿜는다."},
                {"focus_shock", "Wand Focus: Shock", "완드 포커스: 전격",
                 "Elemental energy gathered and thrown as a bolt of lightning at whatever you point at.",
                 "원소의 기운을 모아 가리키는 곳에 번개로 던진다."},
                {"focus_frost", "Wand Focus: Frost", "완드 포커스: 서리",
                 "Hurls chunks of ice that bounce off what they hit, so they can be sent round corners or through several foes.",
                 "얼음 덩어리를 던진다. 맞은 곳에서 튕겨 나가므로 모퉁이 너머로 보내거나 여럿을 꿰뚫게 할 수 있다."},
                {"focus_excavation", "Wand Focus: Excavation", "완드 포커스: 굴착",
                 "Point it at earth or stone and it is dug away. Tougher stuff, and anything that is not of the earth, takes a great deal longer.",
                 "흙이나 돌을 가리키면 파여 나간다. 단단한 것이나 흙의 것이 아닌 것은 훨씬 오래 걸린다."},
                {"focus_trade", "Wand Focus: Equal Trade", "완드 포커스: 등가교환",
                 "Crouch and use it on a block to choose what to trade for. Then strike a block to swap one, or use it to swap a whole swathe. You must carry the blocks you are swapping in, and only blocks open to the air can be swapped.",
                 "웅크리고 블록에 쓰면 바꿔 넣을 것을 고른다. 그다음 블록을 치면 하나를, 쓰면 한 무리를 바꾼다. 바꿔 넣을 블록을 가지고 있어야 하고, 공기에 드러난 블록만 바뀐다."},
                {"focus_builder", "Wand Focus: Builder", "완드 포커스: 건축",
                 "Like equal trade, but it places rather than swaps. Crouch and use it to choose the block, then aim at a face up to sixty-four blocks away and it lays a line out from that face towards you, sixteen at a time at first. Whatever the line runs through is taken up into your pack.",
                 "등가교환과 같지만 바꾸는 대신 놓는다. 웅크리고 써서 블록을 고른 뒤 64칸 안의 면을 겨누면, 그 면에서 내 쪽으로 한 줄을 깐다. 처음에는 한 번에 16개까지다. 줄이 지나가는 블록은 가방으로 들어온다."},
                {"focus_hole", "Wand Focus: Portable Hole", "완드 포커스: 이동식 구멍",
                 "Opens a passage through a wall into the space between spaces, for a few seconds, and then the wall is back. Some matter, and some complicated things, will not let it through.",
                 "벽에 공간과 공간 사이로 통하는 통로를 몇 초 동안 연다. 그 뒤 벽은 돌아온다. 어떤 물질과 복잡한 것들은 뚫리지 않는다."},
                {"focus_hellbat", "Wand Focus: Nine Hells", "완드 포커스: 아홉 지옥",
                 "Trips to the Nether have taught you to fear its burning bats. Now you can call them up yourself and send them after your enemies.",
                 "네더에 다녀오면서 불타는 박쥐를 두려워하게 되었다. 이제는 직접 불러내 적에게 보낼 수 있다."},
                {"vampire_bats", "Upgrade: Vampire Bats", "업그레이드: 흡혈 박쥐",
                 "Vicious little bloodsuckers. At the fifth rank a Nine Hells focus may be given bats that drain the life of what they bite and hand some of it back to whoever called them. The focal manipulator will not offer it until this is understood.",
                 "사나운 작은 흡혈귀들. 아홉 지옥 포커스의 5단계에서, 문 것의 생명을 빨아 일부를 부른 이에게 돌려주는 박쥐를 줄 수 있다. 이것을 알기 전에는 초점 조작기가 내놓지 않는다."},
                {"focus_primal", "Wand Focus: Primal", "완드 포커스: 원시",
                 "A dangerous, erratic orb of raw primal energy that does terrible damage where it lands. What it costs is never quite the same twice.",
                 "날것의 원시 기운으로 된 위험하고 변덕스러운 구체로, 떨어진 곳에 끔찍한 피해를 준다. 드는 값이 매번 조금씩 다르다."},
        };
        for (String[] entry : research) {
            add("research." + Alchemia.MODID + "." + entry[0], pick(entry[1], entry[2]));
            add("research." + Alchemia.MODID + "." + entry[0] + ".page", pick(entry[3], entry[4]));
        }

        addEffect(ModEffects.TAINT_POISON, pick("Taint Poison", "오염 중독"));
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
                {"Somewhere close, voices are chanting.", "가까운 어딘가에서 읊조리는 소리가 들린다."},
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
        add("item.alchemia.bioalchemic_mind", pick("Bioalchemic Mind", "생체연금 두뇌"));
        add("block.alchemia.bellows", pick("Bellows", "풀무"));
        add("block.alchemia.centrifuge", pick("Essentia Centrifuge", "에센시아 원심분리기"));
        add("block.alchemia.arcane_bore", pick("Arcane Bore", "비전 굴착기"));
        add("block.alchemia.mirror", pick("Magic Mirror", "마법 거울"));
        add("block.alchemia.mirror_essentia", pick("Essentia Mirror", "에센시아 거울"));
        add("block.alchemia.mirror.linked_to", pick("Linked to %s, %s, %s in %s", "%4$s의 %1$s, %2$s, %3$s에 이어짐"));
        add("block.alchemia.mirror.already_linked", pick("That mirror is already linked to a valid destination.",
                "그 거울은 이미 짝이 있다."));
        addItem(ModItems.HAND_MIRROR, pick("Magic Hand Mirror", "마법 손거울"));
        add("item.alchemia.hand_mirror.linked", pick("Link established.", "이어졌다."));
        add("item.alchemia.hand_mirror.error", pick("Destination mirror is missing or misplaced. Link broken.",
                "이어진 거울이 없거나 자리를 옮겼다. 끊어졌다."));
        add("block.alchemia.infernal_furnace", pick("Infernal Furnace", "지옥의 화로"));
        add("block.alchemia.furnace_shell", pick("Infernal Furnace", "지옥의 화로"));
        add("block.alchemia.arcane_bore_base", pick("Arcane Bore Base", "비전 굴착기 받침"));
        add("gui.alchemia.arcane_bore.width", pick("Width: %s", "너비: %s"));
        add("gui.alchemia.arcane_bore.speed", pick("Speed: +%s", "속도: +%s"));
        add("gui.alchemia.arcane_bore.other", pick("Other properties:", "그 밖의 성질:"));
        add("gui.alchemia.arcane_bore.refining", pick("Refining %s", "정련 %s"));
        add("gui.alchemia.arcane_bore.fortune", pick("Fortune %s", "행운 %s"));
        add("gui.alchemia.arcane_bore.silk", pick("Silk Touch", "섬세한 손길"));
        add("block.alchemia.thaumatorium", pick("Thaumatorium", "타우마토리움"));
        add("block.alchemia.brain_jar", pick("Brain in a Jar", "단지 속 두뇌"));
        add("block.alchemia.brain_jar.xp", pick("%s xp", "경험치 %s"));
        add("block.alchemia.mnemonic_matrix", pick("Mnemonic Matrix", "기억 행렬"));
        add("block.alchemia.crystallizer", pick("Essentia Crystallizer", "에센시아 결정화기"));
        add("block.alchemia.levitator", pick("Arcane Levitator", "비전 부양기"));
        add("block.alchemia.levitator.range", pick("Range set to %s blocks (%s aer a minute for each thing lifted)",
                "범위를 %s칸으로 맞췄다 (들어 올리는 것 하나에 분당 공기 %s점)"));
        add("block.alchemia.redstone_relay", pick("Redstone Relay", "레드스톤 계전기"));
        add("block.alchemia.redstone_relay.in", pick("In: %s", "받는 세기: %s"));
        add("block.alchemia.redstone_relay.out", pick("Out: %s", "내는 세기: %s"));
        add("block.alchemia.arcane_ear", pick("Arcane Ear", "비전 귀"));
        add("block.alchemia.spa", pick("Arcane Spa", "비전 스파"));
        add("block.alchemia.recharge_pedestal", pick("Recharge Pedestal", "재충전 받침대"));
        add("block.alchemia.aura_totem_push", pick("Aura Totem of Dispersal", "흩어 놓는 오라 토템"));
        add("block.alchemia.aura_totem_pull", pick("Aura Totem of Attraction", "끌어모으는 오라 토템"));
        add("block.alchemia.aura_totem_pole_inner", pick("Totem Pole Inner", "안쪽 토템 기둥"));
        add("block.alchemia.aura_totem_pole_outer", pick("Totem Pole Outer", "바깥쪽 토템 기둥"));
        add("block.alchemia.aura_totem_pole_pure", pick("Stabilizing Totem Pole", "안정시키는 토템 기둥"));
        add("block.alchemia.dioptra", pick("Alchemic Dioptra", "연금 디옵트라"));
        add("block.alchemia.dioptra.showing", pick("Showing %s levels in the aura", "오라의 %s 수준을 보여 준다"));
        add("block.alchemia.dioptra.strongest", pick("Showing highest aura levels", "가장 높은 오라 수준을 보여 준다"));
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
