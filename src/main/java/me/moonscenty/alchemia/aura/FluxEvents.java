package me.moonscenty.alchemia.aura;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.AlchemiaConfig;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * What flux does when a chunk has too much of it.
 * <p>
 * Every so often a chunk that is thick with flux lets some of it out as trouble: warp on whoever is nearby, a
 * sickness, a node knocked into a different kind. Each costs the chunk a share of its flux, which is the one way
 * flux leaves the world on its own.
 * <p>
 * The original also called up wisps and crawlers; those come with their step.
 */
public final class FluxEvents {
    /** A chunk holding more than this share of its base in flux is fair game. */
    private static final float TROUBLE_ABOVE = 0.75F;
    /** How far a trouble reaches from where it lands. */
    private static final double REACH = 16.0;

    private static final Component TWISTED = Component.translatable("flux_event.alchemia.warp")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
    private static final Component UNSTABLE = Component.translatable("flux_event.alchemia.sickness")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);

    /** One thing flux can do, how likely it is against the others, and what it costs the chunk. */
    public record Trouble(String name, int weight, int cost, Performer performer) {
        /**
         * @return whether it came to anything, since a trouble with nobody there to feel it costs nothing
         */
        public boolean perform(ServerLevel level, BlockPos at, RandomSource random) {
            return performer.perform(level, at, random);
        }
    }

    @FunctionalInterface
    public interface Performer {
        boolean perform(ServerLevel level, BlockPos at, RandomSource random);
    }

    private static final List<Trouble> TROUBLES = new ArrayList<>(List.of(
            new Trouble("lightning", 5, 25, FluxEvents::strikeLightning),
            new Trouble("cloud", 1, 40, FluxEvents::gatherCloud),
            new Trouble("warp", 5, 20, FluxEvents::twistMinds),
            new Trouble("sickness", 5, 15, FluxEvents::sicken),
            new Trouble("node_shift", 3, 20, FluxEvents::shiftNode)));

    /** How far a cloud already overhead keeps the weather from piling up. */
    private static final double CLOUD_ELBOW_ROOM = 32.0;
    /** How far a bolt reaches around where it lands, and what it does to whatever it finds there. */
    private static final double BOLT_REACH = 3.0;
    private static final float BOLT_HARM = 3.0F;
    private static final int BOLT_FLU = 1200;
    /** How high a cloud gathers, and how long it stays, in ticks. */
    private static final int CLOUD_HEIGHT = 20;
    private static final int CLOUD_LASTS = 30 * 20;
    private static final int CLOUD_LASTS_MORE = 10 * 20;

    private FluxEvents() {
    }

    /** Something a later step can add to the table, such as a creature that only exists once that step is in. */
    public static void register(Trouble trouble) {
        TROUBLES.add(trouble);
    }

    /** Whether this chunk's flux is enough, and it is its unlucky turn. */
    public static boolean brewing(AuraChunk aura, RandomSource random) {
        int flux = aura.get(ModAspects.FLUX);
        return flux > aura.base() * TROUBLE_ABOVE && random.nextFloat() < flux / (float) (AuraGeneration.BASE * 100);
    }

    /**
     * Lets one trouble out somewhere on the surface of the chunk.
     *
     * @return the trouble that happened, or null if nothing came of it
     */
    public static Trouble strike(ServerLevel level, ChunkPos chunk, RandomSource random) {
        int x = chunk.getMinBlockX() + random.nextInt(16);
        int z = chunk.getMinBlockZ() + random.nextInt(16);
        return strikeAt(level, new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z), random);
    }

    /** Lets one trouble out at a particular spot, paid for by the chunk that spot is in. */
    public static Trouble strikeAt(ServerLevel level, BlockPos at, RandomSource random) {
        if (!AlchemiaConfig.TAINT_SPREADS.get() || TROUBLES.isEmpty()) {
            return null;
        }
        Trouble trouble = pick(random);
        if (AuraHandler.get(level, at, ModAspects.FLUX) < trouble.cost() || !trouble.perform(level, at, random)) {
            return null;
        }
        AuraHandler.drain(level, at, ModAspects.FLUX, trouble.cost());
        return trouble;
    }

    private static Trouble pick(RandomSource random) {
        int total = TROUBLES.stream().mapToInt(Trouble::weight).sum();
        int roll = random.nextInt(total);
        for (Trouble trouble : TROUBLES) {
            roll -= trouble.weight();
            if (roll < 0) {
                return trouble;
            }
        }
        return TROUBLES.getLast();
    }

    private static AABB around(BlockPos at) {
        return new AABB(at).inflate(REACH);
    }

    // --- the troubles themselves ---------------------------------------------

    /** Weather does not pile up: neither trouble happens with a cloud already overhead. */
    private static boolean underACloud(ServerLevel level, BlockPos at) {
        return !level.getEntitiesOfClass(TaintCloud.class, new AABB(at).inflate(CLOUD_ELBOW_ROOM)).isEmpty();
    }

    /**
     * A bolt out of a clear sky, drawn to whoever is standing in the open, that leaves a full puddle of goo where it
     * lands. It is drawn as ordinary lightning; the harm and the flu are the mod's own.
     */
    private static boolean strikeLightning(ServerLevel level, BlockPos at, RandomSource random) {
        if (underACloud(level, at)) {
            return false;
        }
        BlockPos struck = at;
        List<LivingEntity> exposed = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(at.getX(), at.getY(), at.getZ(), at.getX() + 1, level.getMaxBuildHeight(), at.getZ() + 1).inflate(4.0),
                living -> living.isAlive() && level.canSeeSky(living.blockPosition()));
        if (!exposed.isEmpty()) {
            struck = exposed.get(random.nextInt(exposed.size())).blockPosition();
        }
        if (!level.canSeeSky(struck)) {
            return false;
        }

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) {
            return false;
        }
        bolt.moveTo(Vec3.atBottomCenterOf(struck));
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);

        for (LivingEntity hit : level.getEntitiesOfClass(LivingEntity.class, new AABB(struck).inflate(BOLT_REACH, BOLT_REACH + 6.0, BOLT_REACH))) {
            hit.hurt(level.damageSources().lightningBolt(), BOLT_HARM);
            hit.addEffect(new MobEffectInstance(ModEffects.FLUX_FLU, BOLT_FLU, 0, false, true));
        }
        if (level.getBlockState(struck).canBeReplaced() && level.getBlockState(struck).getFluidState().isEmpty()) {
            level.setBlock(struck, FluxGooBlock.of(ModBlocks.FLUX_GOO.get(), FluxGooBlock.FULL), Block.UPDATE_ALL);
        }
        return true;
    }

    /** A cloud gathers overhead and rains goo for a while. */
    private static boolean gatherCloud(ServerLevel level, BlockPos at, RandomSource random) {
        if (underACloud(level, at)) {
            return false;
        }
        BlockPos up = at.above(CLOUD_HEIGHT);
        if (up.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        return level.addFreshEntity(new TaintCloud(level, up, CLOUD_LASTS + random.nextInt(CLOUD_LASTS_MORE)));
    }

    /** Everyone nearby takes warp: usually a passing dose, now and then one that stays. */
    private static boolean twistMinds(ServerLevel level, BlockPos at, RandomSource random) {
        List<Player> players = level.getEntitiesOfClass(Player.class, around(at));
        for (Player player : players) {
            player.sendSystemMessage(TWISTED);
            if (random.nextFloat() < 0.25F) {
                WarpHandler.add(player, WarpData.Kind.STICKY, 1);
            } else {
                WarpHandler.add(player, WarpData.Kind.TEMPORARY, 2 + random.nextInt(4));
            }
        }
        return !players.isEmpty();
    }

    /** Everything living nearby catches the phage, and the players among them are told so. */
    private static boolean sicken(ServerLevel level, BlockPos at, RandomSource random) {
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, around(at));
        for (LivingEntity victim : victims) {
            if (victim instanceof Player player) {
                player.sendSystemMessage(UNSTABLE);
            }
            victim.addEffect(new MobEffectInstance(ModEffects.FLUX_PHAGE, 3000, 2));
        }
        return !victims.isEmpty();
    }

    /** A node nearby is knocked into another kind: half the time unstable, otherwise anything at all. */
    private static boolean shiftNode(ServerLevel level, BlockPos at, RandomSource random) {
        List<AuraNode> nodes = level.getEntitiesOfClass(AuraNode.class, around(at));
        if (nodes.isEmpty()) {
            return false;
        }
        AuraNode node = nodes.get(random.nextInt(nodes.size()));
        NodeType[] kinds = NodeType.values();
        node.setType(random.nextBoolean() ? NodeType.UNSTABLE : kinds[random.nextInt(kinds.length)]);
        return true;
    }
}
