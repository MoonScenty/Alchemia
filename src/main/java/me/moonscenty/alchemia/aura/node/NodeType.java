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

public enum NodeType implements StringRepresentable {
    PLAIN("plain"),
    DARK("dark"),
    HUNGRY("hungry"),
    PURE("pure"),
    TAINTED("tainted"),
    UNSTABLE("unstable"),
    ASTRAL("astral");

    public static final com.mojang.serialization.Codec<NodeType> CODEC = StringRepresentable.fromEnum(NodeType::values);

    public static final int PERIOD = 200;

    private final String name;

    NodeType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public Component displayName() {
        return Component.translatable("node_type.alchemia." + name);
    }

    public boolean feeds() {
        return this != HUNGRY;
    }

    // moon phase sways the strength; astral nodes go by the light where they hang instead
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

    // -20% at new moon to +20% at full
    private static float moonSway(ServerLevel level) {
        return (Math.abs(level.getMoonPhase() - 4) - 2) / 5.0F;
    }

    private static int atLeastOne(double amount) {
        return (int) Math.max(1.0, amount);
    }

    // type-specific extras; feeding the land is done by the node itself
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

    private void cleanUp(AuraNode node, ServerLevel level, BlockPos at) {
        if (AuraHandler.drain(level, at, ModAspects.FLUX, 1) && level.getRandom().nextFloat() < 0.025F) {
            node.setSize(node.getSize() - 1);
            if (node.getSize() <= 0) {
                node.discard();
            }
        }
    }

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

    private void drift(AuraNode node, ServerLevel level) {
        if (level.getRandom().nextInt(33) == 0) {
            node.setAspect(ModAspects.randomPrimal(level.getRandom()));
        }
    }
}
