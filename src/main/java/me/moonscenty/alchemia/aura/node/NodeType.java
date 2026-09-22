package me.moonscenty.alchemia.aura.node;

import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.BiomePaint;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;

/**
 * What kind of thing a node is.
 * <p>
 * All of them feed the land their own aspect. What tells them apart is what else they do with it: one eats what is
 * around it, one cleans up after everyone, one makes the mess in the first place.
 */
public enum NodeType implements StringRepresentable {
    /** Feeds the land and nothing else. Most nodes. */
    PLAIN("plain"),
    /** Feeds the land, and turns what is around it into somewhere it is not pleasant to be. */
    DARK("dark"),
    /** Feeds nothing. Drinks what the land has and grows on it. */
    HUNGRY("hungry"),
    /** Feeds the land and takes the flux out of it, wearing itself away in the doing. */
    PURE("pure"),
    /** Feeds the land flux. */
    TAINTED("tainted"),
    /** Feeds the land, but what it is made of keeps changing. */
    UNSTABLE("unstable"),
    /** Feeds the land, and is stronger the brighter it is where it hangs. */
    ASTRAL("astral");

    public static final com.mojang.serialization.Codec<NodeType> CODEC = StringRepresentable.fromEnum(NodeType::values);

    /** How often the out-of-the-ordinary business happens, in ticks. */
    public static final int PERIOD = 200;

    private final String name;

    NodeType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** What this kind is called, for the label on a node and for what a reading of one says. */
    public Component displayName() {
        return Component.translatable("node_type.alchemia." + name);
    }

    /** Everything but a hungry node is worth something to the land around it. */
    public boolean feeds() {
        return this != HUNGRY;
    }

    /**
     * How much a node of this kind gives, or takes, each time round.
     * <p>
     * The moon has a say in it: an ordinary node is strongest at full moon, a dark one at new moon, and an astral
     * one goes by the light where it hangs rather than the sky.
     */
    public int strengthOf(AuraNode node, ServerLevel level) {
        double root = Math.sqrt(node.getSize() / 3.0F);
        float moon = moonSway(level);
        float light = (level.getMaxLocalRawBrightness(node.blockPosition()) / 15.0F - 0.5F) / 3.0F;

        return switch (this) {
            case DARK -> atLeastOne(root * (1.0F - moon + light));
            case ASTRAL -> atLeastOne(root * (1.0F + moon - light));
            case HUNGRY -> Math.max(1, atLeastOne(root * (1.0F + moon)) / 10);
            case TAINTED -> Math.max(1, atLeastOne(root * (1.0F + moon)) / 5);
            default -> atLeastOne(root * (1.0F + moon));
        };
    }

    /** How far the moon pushes a node either way, from a fifth down at new moon to a fifth up at full. */
    private static float moonSway(ServerLevel level) {
        return (Math.abs(level.getMoonPhase() - 4) - 2) / 5.0F;
    }

    private static int atLeastOne(double amount) {
        return (int) Math.max(1.0, amount);
    }

    /**
     * The out-of-the-ordinary business, done once every {@link #PERIOD} ticks. Feeding the land is done by the node
     * itself; this is only what makes each kind different.
     */
    public void doItsThing(AuraNode node, ServerLevel level) {
        BlockPos at = node.blockPosition();
        switch (this) {
            case DARK -> darken(node, level, at);
            case HUNGRY -> feedOnTheLand(node, level, at);
            case PURE -> cleanUp(node, level, at);
            case TAINTED -> spoil(node, level, at);
            case UNSTABLE -> drift(node, level);
            default -> {
            }
        }
    }

    /** Turns one patch of land near the node into whatever a dark node makes, working outwards. */
    private void darken(AuraNode node, ServerLevel level, BlockPos at) {
        Optional<Holder<Biome>> into = BiomePaint.oneOf(level, ModTags.Biomes.NODE_DARKENS_INTO, level.getRandom());
        if (into.isEmpty()) {
            return;
        }
        double angle = level.getRandom().nextDouble() * Math.TAU;
        int reach = (int) (4 + Math.sqrt(node.getSize()));
        for (int step = 0; step < reach; step++) {
            BlockPos target = at.offset((int) (Math.cos(angle) * step), 0, (int) (Math.sin(angle) * step));
            if (BiomePaint.paint(level, target, into.get())) {
                return;
            }
        }
    }

    /** Drinks from the land and grows on what it takes, so a hungry node leaves a hole around itself. */
    private void feedOnTheLand(AuraNode node, ServerLevel level, BlockPos at) {
        Holder<Aspect> aspect = node.aspect();
        int base = AuraHandler.base(level, at);
        if (aspect == null || base <= 0) {
            return;
        }
        float share = AuraHandler.get(level, at, aspect) / (float) base;
        if (level.getRandom().nextFloat() < share
                && AuraHandler.drain(level, at, aspect, strengthOf(node, level))
                && level.getRandom().nextInt(1 + node.getSize() * 2) == 0) {
            node.setSize(node.getSize() + 1);
        }
    }

    /** Takes the flux out of the land, and is spent a little at a time for doing it. */
    private void cleanUp(AuraNode node, ServerLevel level, BlockPos at) {
        if (AuraHandler.drain(level, at, ModAspects.FLUX, 1) && level.getRandom().nextFloat() < 0.025F) {
            node.setSize(node.getSize() - 1);
            if (node.getSize() <= 0) {
                node.discard();
            }
        }
    }

    /** Feeds the land flux, and the less of it there already is the more it gives. */
    private void spoil(AuraNode node, ServerLevel level, BlockPos at) {
        int base = AuraHandler.base(level, at);
        if (base <= 0) {
            return;
        }
        float share = AuraHandler.get(level, at, ModAspects.FLUX) / (float) base;
        if (level.getRandom().nextFloat() > share * 0.8F) {
            AuraHandler.recharge(level, at, ModAspects.FLUX, strengthOf(node, level), level.getRandom());
        }
    }

    /** Every so often, forgets what it was made of and becomes something else. */
    private void drift(AuraNode node, ServerLevel level) {
        if (level.getRandom().nextInt(33) == 0) {
            node.setAspect(ModAspects.randomPrimal(level.getRandom()));
        }
    }
}
