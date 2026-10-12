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

    /** What anything alive needs before the game will make one. */
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(AUTOCASTER.get(), Autocaster.attributes().build());
        event.put(FIRE_BAT.get(), FireBat.attributes().build());
        event.put(WISP.get(), me.moonscenty.alchemia.entity.Wisp.attributes().build());
        event.put(PECH.get(), me.moonscenty.alchemia.entity.Pech.attributes().build());
        event.put(ANGRY_ZOMBIE.get(), AngryZombie.attributes().build());
        event.put(FURIOUS_ZOMBIE.get(), FuriousZombie.attributes().build());
    }

    /**
     * Where they may come of themselves. The zombies as any monster does, in the dark on solid ground. A firebat
     * wants it darker still: whatever light there is has to be no more than a throw of seven.
     */
    public static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
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
    }

    private ModEntities() {
    }
}
