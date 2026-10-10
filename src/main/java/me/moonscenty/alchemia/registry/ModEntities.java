package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.TaintCloud;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.wand.spell.Grapple;
import me.moonscenty.alchemia.wand.spell.PechBlast;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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

    private ModEntities() {
    }
}
