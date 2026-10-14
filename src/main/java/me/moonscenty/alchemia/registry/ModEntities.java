package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.TaintCloud;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.entity.AngryZombie;
import me.moonscenty.alchemia.entity.FireBat;
import me.moonscenty.alchemia.entity.FuriousZombie;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import me.moonscenty.alchemia.wand.spell.Ember;
import me.moonscenty.alchemia.wand.spell.FireOrb;
import me.moonscenty.alchemia.wand.spell.ShockOrb;
import me.moonscenty.alchemia.wand.spell.FrostShard;
import me.moonscenty.alchemia.wand.spell.Grapple;
import me.moonscenty.alchemia.wand.spell.PrimalOrb;
import me.moonscenty.alchemia.wand.spell.VisShard;
import me.moonscenty.alchemia.wand.spell.PechBlast;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Alchemia.MODID);

    /** Small, kept loaded well past the usual distance, and never saved as part of a mob cap. */
    public static final DeferredHolder<EntityType<?>, EntityType<AuraNode>> AURA_NODE =
            ENTITIES.register("aura_node", () -> EntityType.Builder
                    .<AuraNode>of(AuraNode::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(20)
                    .fireImmune()
                    .noSummon()
                    .build("aura_node"));

    /** A spot in the air with no body to speak of; it only has to be seen from far enough to see its rain. */
    public static final DeferredHolder<EntityType<?>, EntityType<TaintCloud>> TAINT_CLOUD =
            ENTITIES.register("taint_cloud", () -> EntityType.Builder
                    .<TaintCloud>of(TaintCloud::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .fireImmune()
                    .noSummon()
                    .build("taint_cloud"));

    /**
     * A thrown spite that bursts where it lands.
     * <p>
     * Small, short-lived and never saved: a curse in the air when the world closes is a curse that never landed.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<PechBlast>> PECH_BLAST =
            ENTITIES.register("pech_blast", () -> EntityType.Builder
                    .<PechBlast>of(PechBlast::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .noSummon()
                    .build("pech_blast"));

    /** A hook on no line at all. What holds the climber to it is the pull, not a rope. */
    public static final DeferredHolder<EntityType<?>, EntityType<Grapple>> GRAPPLE =
            ENTITIES.register("grapple", () -> EntityType.Builder
                    .<Grapple>of(Grapple::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
                    .noSummon()
                    .build("grapple"));

    /**
     * The four a wand throws.
     * <p>
     * None is drawn: each is its own trail of particles and a burst where it lands, which is most of
     * what each of them looked like in the first place.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<Ember>> EMBER =
            ENTITIES.register("ember", () -> EntityType.Builder
                    .<Ember>of(Ember::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .noSummon()
                    .build("ember"));

    public static final DeferredHolder<EntityType<?>, EntityType<FrostShard>> FROST_SHARD =
            ENTITIES.register("frost_shard", () -> EntityType.Builder
                    .<FrostShard>of(FrostShard::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .noSummon()
                    .build("frost_shard"));

    public static final DeferredHolder<EntityType<?>, EntityType<VisShard>> VIS_SHARD =
            ENTITIES.register("vis_shard", () -> EntityType.Builder
                    .<VisShard>of(VisShard::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
                    .noSummon()
                    .build("vis_shard"));

    public static final DeferredHolder<EntityType<?>, EntityType<PrimalOrb>> PRIMAL_ORB =
            ENTITIES.register("primal_orb", () -> EntityType.Builder
                    .<PrimalOrb>of(PrimalOrb::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(4)
                    .noSummon()
                    .build("primal_orb"));

    /** What the fire focus throws once a fireball is worked into it, and what the shock focus lobs. */
    public static final DeferredHolder<EntityType<?>, EntityType<FireOrb>> FIRE_ORB =
            ENTITIES.register("fire_orb", () -> EntityType.Builder
                    .<FireOrb>of(FireOrb::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(4)
                    .noSummon()
                    .build("fire_orb"));

    public static final DeferredHolder<EntityType<?>, EntityType<ShockOrb>> SHOCK_ORB =
            ENTITIES.register("shock_orb", () -> EntityType.Builder
                    .<ShockOrb>of(ShockOrb::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(4)
                    .noSummon()
                    .build("shock_orb"));

    /**
     * A focus on a bracket. It is a mob rather than a block because the original let one hang off any face, and a
     * block that can be put on six faces and still turn its head is a block fighting the game.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<Autocaster>> AUTOCASTER =
            ENTITIES.register("autocaster", () -> EntityType.Builder
                    .<Autocaster>of(Autocaster::new, MobCategory.MISC)
                    .sized(0.9F, 0.9F)
                    .eyeHeight(0.45F)
                    .clientTrackingRange(10)
                    .noSummon()
                    .build("autocaster"));

    /**
     * A bat that burns. Small, fire-proof, and followed at the original's distance; it moves too fast and too
     * often for anything slower than every third tick to keep up with.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<FireBat>> FIRE_BAT =
            ENTITIES.register("firebat", () -> EntityType.Builder
                    .<FireBat>of(FireBat::new, MobCategory.MONSTER)
                    .sized(0.5F, 0.9F)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build("firebat"));

    /** A wisp: a knot of light the size of most of a block, followed far, since it is seen from far off. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.Wisp>> WISP =
            ENTITIES.register("wisp", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.Wisp>of(me.moonscenty.alchemia.entity.Wisp::new,
                            MobCategory.MONSTER)
                    .sized(0.9F, 0.9F)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build("wisp"));

    /** A pech: a little hunched thing a player's width and height, its eyes low in its face. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.Pech>> PECH =
            ENTITIES.register("pech", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.Pech>of(me.moonscenty.alchemia.entity.Pech::new,
                            MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .eyeHeight(1.188F)
                    .clientTrackingRange(8)
                    .build("pech"));

    /** A taint crawler: smaller than a silverfish, its eyes on the ground. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintCrawler>>
            TAINT_CRAWLER = ENTITIES.register("taint_crawler", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintCrawler>of(me.moonscenty.alchemia.entity.TaintCrawler::new,
                            MobCategory.MONSTER)
                    .sized(0.4F, 0.3F)
                    .eyeHeight(0.1F)
                    .clientTrackingRange(8)
                    .build("taint_crawler"));
    /** A taint swarm: a cloud two blocks across. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintSwarm>>
            TAINT_SWARM = ENTITIES.register("taint_swarm", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintSwarm>of(me.moonscenty.alchemia.entity.TaintSwarm::new,
                            MobCategory.MONSTER)
                    .sized(2.0F, 2.0F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build("taint_swarm"));
    /** A taintacle: three blocks tall, rooted. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.Taintacle>>
            TAINTACLE = ENTITIES.register("taintacle", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.Taintacle>of(me.moonscenty.alchemia.entity.Taintacle::new,
                            MobCategory.MONSTER)
                    .sized(0.8F, 3.0F)
                    .clientTrackingRange(8)
                    .build("taintacle"));
    /** A taint tendril: a taintacle a block tall and a finger thick. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintacleTiny>>
            TAINTACLE_TINY = ENTITIES.register("taintacle_tiny", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintacleTiny>of(
                            me.moonscenty.alchemia.entity.TaintacleTiny::new, MobCategory.MONSTER)
                    .sized(0.22F, 1.0F)
                    .clientTrackingRange(8)
                    .noSummon()
                    .build("taintacle_tiny"));

    /** The animals the taint has made over, each the size of what it was. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintedCow>>
            TAINTED_COW = ENTITIES.register("tainted_cow", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintedCow>of(me.moonscenty.alchemia.entity.TaintedCow::new,
                            MobCategory.MONSTER)
                    .sized(0.9F, 1.3F)
                    .clientTrackingRange(8)
                    .build("tainted_cow"));
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintedPig>>
            TAINTED_PIG = ENTITIES.register("tainted_pig", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintedPig>of(me.moonscenty.alchemia.entity.TaintedPig::new,
                            MobCategory.MONSTER)
                    .sized(0.9F, 0.9F)
                    .clientTrackingRange(8)
                    .build("tainted_pig"));
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintedChicken>>
            TAINTED_CHICKEN = ENTITIES.register("tainted_chicken", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintedChicken>of(me.moonscenty.alchemia.entity.TaintedChicken::new,
                            MobCategory.MONSTER)
                    .sized(0.5F, 0.8F)
                    .clientTrackingRange(8)
                    .build("tainted_chicken"));
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintedSheep>>
            TAINTED_SHEEP = ENTITIES.register("tainted_sheep", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintedSheep>of(me.moonscenty.alchemia.entity.TaintedSheep::new,
                            MobCategory.MONSTER)
                    .sized(0.9F, 1.3F)
                    .clientTrackingRange(8)
                    .build("tainted_sheep"));
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintedCreeper>>
            TAINTED_CREEPER = ENTITIES.register("tainted_creeper", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintedCreeper>of(me.moonscenty.alchemia.entity.TaintedCreeper::new,
                            MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("tainted_creeper"));
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintedVillager>>
            TAINTED_VILLAGER = ENTITIES.register("tainted_villager", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintedVillager>of(
                            me.moonscenty.alchemia.entity.TaintedVillager::new,
                            MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("tainted_villager"));
    /** A made-over rabbit keeps to the world as a rabbit does. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.TaintedRabbit>>
            TAINTED_RABBIT = ENTITIES.register("tainted_rabbit", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.TaintedRabbit>of(me.moonscenty.alchemia.entity.TaintedRabbit::new,
                            MobCategory.CREATURE)
                    .sized(0.6F, 0.7F)
                    .clientTrackingRange(8)
                    .build("tainted_rabbit"));
    /** An alchemic slime: a slime's measure, grown by its size. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.AlchemicSlime>>
            ALCHEMIC_SLIME = ENTITIES.register("alchemic_slime", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.AlchemicSlime>of(me.moonscenty.alchemia.entity.AlchemicSlime::new,
                            MobCategory.MONSTER)
                    .sized(0.52F, 0.52F)
                    .eyeHeight(0.325F)
                    .clientTrackingRange(10)
                    .build("alchemic_slime"));

    /** A zombie's size and a zombie's reach. */
    public static final DeferredHolder<EntityType<?>, EntityType<AngryZombie>> ANGRY_ZOMBIE =
            ENTITIES.register("angry_zombie", () -> EntityType.Builder
                    .<AngryZombie>of(AngryZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .passengerAttachments(2.0125F)
                    .ridingOffset(-0.7F)
                    .clientTrackingRange(8)
                    .build("angry_zombie"));

    /** A zombie's size to begin with; its anger makes it bigger. */
    public static final DeferredHolder<EntityType<?>, EntityType<FuriousZombie>> FURIOUS_ZOMBIE =
            ENTITIES.register("furious_zombie", () -> EntityType.Builder
                    .<FuriousZombie>of(FuriousZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .passengerAttachments(2.0125F)
                    .ridingOffset(-0.7F)
                    .clientTrackingRange(8)
                    .build("furious_zombie"));

    /** A golem: a small made thing, waist high to a player. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.golem.Golem>> GOLEM =
            ENTITIES.register("golem", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.golem.Golem>of(me.moonscenty.alchemia.golem.Golem::new,
                            MobCategory.MISC)
                    .sized(0.4F, 0.9F)
                    .eyeHeight(0.7F)
                    .clientTrackingRange(10)
                    .build("golem"));

    /** A golem's dart: a small arrow. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.golem.GolemDart>> GOLEM_DART =
            ENTITIES.register("golem_dart", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.golem.GolemDart>of(me.moonscenty.alchemia.golem.GolemDart::new,
                            MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("golem_dart"));

    /** The Crimson Cult: a player's size, followed as far as the original followed them. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.cult.CultistKnight>>
            CULTIST_KNIGHT = ENTITIES.register("cultist_knight", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.cult.CultistKnight>of(
                            me.moonscenty.alchemia.entity.cult.CultistKnight::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("cultist_knight"));
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.cult.CultistCleric>>
            CULTIST_CLERIC = ENTITIES.register("cultist_cleric", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.cult.CultistCleric>of(
                            me.moonscenty.alchemia.entity.cult.CultistCleric::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("cultist_cleric"));
    /** A lesser crimson portal: still, fire-proof, and told about seldom, as the original's was. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.cult.CultistPortal>>
            CULTIST_PORTAL = ENTITIES.register("cultist_portal", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.cult.CultistPortal>of(
                            me.moonscenty.alchemia.entity.cult.CultistPortal::new, MobCategory.MONSTER)
                    .sized(1.5F, 3.0F)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .updateInterval(20)
                    .build("cultist_portal"));
    /** The crab, low and broad, and the husk it lives in, a zombie's shape. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.EldritchCrab>>
            ELDRITCH_CRAB = ENTITIES.register("eldritch_crab", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.EldritchCrab>of(
                            me.moonscenty.alchemia.entity.EldritchCrab::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.6F)
                    .clientTrackingRange(8)
                    .build("eldritch_crab"));
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.InhabitedZombie>>
            INHABITED_ZOMBIE = ENTITIES.register("inhabited_zombie", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.InhabitedZombie>of(
                            me.moonscenty.alchemia.entity.InhabitedZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .passengerAttachments(2.0125F)
                    .ridingOffset(-0.7F)
                    .clientTrackingRange(8)
                    .build("inhabited_zombie"));

    /** The guardian, tall and narrow; the mind spider, half a spider; the guardian's orb. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.EldritchGuardian>>
            ELDRITCH_GUARDIAN = ENTITIES.register("eldritch_guardian", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.EldritchGuardian>of(
                            me.moonscenty.alchemia.entity.EldritchGuardian::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.25F)
                    .eyeHeight(2.1F)
                    .clientTrackingRange(8)
                    .build("eldritch_guardian"));
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.MindSpider>>
            MIND_SPIDER = ENTITIES.register("mind_spider", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.MindSpider>of(
                            me.moonscenty.alchemia.entity.MindSpider::new, MobCategory.MONSTER)
                    .sized(0.7F, 0.5F)
                    .eyeHeight(0.45F)
                    .clientTrackingRange(8)
                    .build("mind_spider"));
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.EldritchOrb>>
            ELDRITCH_ORB = ENTITIES.register("eldritch_orb", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.EldritchOrb>of(
                            me.moonscenty.alchemia.entity.EldritchOrb::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .noSummon()
                    .build("eldritch_orb"));

    /** The cleric's orb. */
    public static final DeferredHolder<EntityType<?>, EntityType<me.moonscenty.alchemia.entity.cult.GolemOrb>>
            GOLEM_ORB = ENTITIES.register("golem_orb", () -> EntityType.Builder
                    .<me.moonscenty.alchemia.entity.cult.GolemOrb>of(
                            me.moonscenty.alchemia.entity.cult.GolemOrb::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .noSummon()
                    .build("golem_orb"));

    /** What anything alive needs before the game will make one. */
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(AUTOCASTER.get(), Autocaster.attributes().build());
        event.put(CULTIST_KNIGHT.get(), me.moonscenty.alchemia.entity.cult.CultistKnight.attributes().build());
        event.put(ELDRITCH_CRAB.get(), me.moonscenty.alchemia.entity.EldritchCrab.attributes().build());
        event.put(ELDRITCH_GUARDIAN.get(), me.moonscenty.alchemia.entity.EldritchGuardian.attributes().build());
        event.put(MIND_SPIDER.get(), me.moonscenty.alchemia.entity.MindSpider.attributes().build());
        event.put(INHABITED_ZOMBIE.get(), me.moonscenty.alchemia.entity.InhabitedZombie.attributes().build());
        event.put(CULTIST_CLERIC.get(), me.moonscenty.alchemia.entity.cult.CultistCleric.attributes().build());
        event.put(CULTIST_PORTAL.get(), me.moonscenty.alchemia.entity.cult.CultistPortal.attributes().build());
        event.put(FIRE_BAT.get(), FireBat.attributes().build());
        event.put(WISP.get(), me.moonscenty.alchemia.entity.Wisp.attributes().build());
        event.put(PECH.get(), me.moonscenty.alchemia.entity.Pech.attributes().build());
        event.put(GOLEM.get(), me.moonscenty.alchemia.golem.Golem.attributes().build());
        event.put(TAINT_CRAWLER.get(), me.moonscenty.alchemia.entity.TaintCrawler.attributes().build());
        event.put(TAINT_SWARM.get(), me.moonscenty.alchemia.entity.TaintSwarm.attributes().build());
        event.put(TAINTACLE.get(), me.moonscenty.alchemia.entity.Taintacle.attributes().build());
        event.put(TAINTACLE_TINY.get(), me.moonscenty.alchemia.entity.TaintacleTiny.attributes().build());
        event.put(TAINTED_COW.get(), me.moonscenty.alchemia.entity.TaintedCow.attributes().build());
        event.put(TAINTED_PIG.get(), me.moonscenty.alchemia.entity.TaintedPig.attributes().build());
        event.put(TAINTED_CHICKEN.get(), me.moonscenty.alchemia.entity.TaintedChicken.attributes().build());
        event.put(TAINTED_SHEEP.get(), me.moonscenty.alchemia.entity.TaintedSheep.attributes().build());
        event.put(TAINTED_CREEPER.get(), me.moonscenty.alchemia.entity.TaintedCreeper.attributes().build());
        event.put(TAINTED_VILLAGER.get(), me.moonscenty.alchemia.entity.TaintedVillager.attributes().build());
        event.put(TAINTED_RABBIT.get(), me.moonscenty.alchemia.entity.TaintedRabbit.attributes().build());
        event.put(ALCHEMIC_SLIME.get(), net.minecraft.world.entity.monster.Monster.createMonsterAttributes().build());
        event.put(ANGRY_ZOMBIE.get(), AngryZombie.attributes().build());
        event.put(FURIOUS_ZOMBIE.get(), FuriousZombie.attributes().build());
    }

    /**
     * Where they may come of themselves. The zombies as any monster does, in the dark on solid ground. A firebat
     * wants it darker still: whatever light there is has to be no more than a throw of seven.
     */
    public static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(INHABITED_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, me.moonscenty.alchemia.entity.InhabitedZombie::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ELDRITCH_GUARDIAN.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, me.moonscenty.alchemia.entity.EldritchGuardian::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ELDRITCH_CRAB.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ANGRY_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(FURIOUS_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(FIRE_BAT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FireBat::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                me.moonscenty.alchemia.entity.Wisp::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(PECH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                me.moonscenty.alchemia.entity.Pech::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TAINTACLE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                me.moonscenty.alchemia.entity.Taintacle::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private ModEntities() {
    }
}
