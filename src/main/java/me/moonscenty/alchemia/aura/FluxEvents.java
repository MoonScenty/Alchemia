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

// Chunks thick with flux periodically discharge it as events; the only way flux leaves the world on its own.
public final class FluxEvents {
    // fraction of the chunk's base
    private static final float TROUBLE_ABOVE = 0.75F;
    private static final double REACH = 16.0;

    private static final Component TWISTED = Component.translatable("flux_event.alchemia.warp")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
    private static final Component UNSTABLE = Component.translatable("flux_event.alchemia.sickness")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);

    public record Trouble(String name, int weight, int cost, Performer performer) {
        // false when nobody was affected; the chunk then pays nothing
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
            new Trouble("node_shift", 3, 20, FluxEvents::shiftNode),
            new Trouble("wisp", 25, 5, me.moonscenty.alchemia.entity.Wisp::fromFlux),
            new Trouble("crawler", 2, 10, me.moonscenty.alchemia.entity.TaintCrawler::fromFlux)));

    private static final double CLOUD_ELBOW_ROOM = 32.0;
    private static final double BOLT_REACH = 3.0;
    private static final float BOLT_HARM = 3.0F;
    private static final int BOLT_FLU = 1200;
    private static final int CLOUD_HEIGHT = 20;
    private static final int CLOUD_LASTS = 30 * 20;
    private static final int CLOUD_LASTS_MORE = 10 * 20;

    private FluxEvents() {
    }

    public static void register(Trouble trouble) {
        TROUBLES.add(trouble);
    }

    public static boolean brewing(AuraChunk aura, RandomSource random) {
        int flux = aura.get(ModAspects.FLUX);
        return flux > aura.base() * TROUBLE_ABOVE && random.nextFloat() < flux / (float) (AuraGeneration.BASE * 100);
    }

    // null when nothing happened
    public static Trouble strike(ServerLevel level, ChunkPos chunk, RandomSource random) {
        int x = chunk.getMinBlockX() + random.nextInt(16);
        int z = chunk.getMinBlockZ() + random.nextInt(16);
        return strikeAt(level, new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z), random);
    }

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

    // weather doesn't stack: no bolt or cloud under an existing cloud
    private static boolean underACloud(ServerLevel level, BlockPos at) {
        return !level.getEntitiesOfClass(TaintCloud.class, new AABB(at).inflate(CLOUD_ELBOW_ROOM)).isEmpty();
    }

    // the bolt is visual only; damage and flu are applied by hand
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
            hit.addEffect(new MobEffectInstance(ModEffects.TAINT_POISON, BOLT_FLU, 0, false, true));
        }
        if (level.getBlockState(struck).canBeReplaced() && level.getBlockState(struck).getFluidState().isEmpty()) {
            level.setBlock(struck, FluxGooBlock.of(ModBlocks.FLUX_GOO.get(), FluxGooBlock.FULL), Block.UPDATE_ALL);
        }
        return true;
    }

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
