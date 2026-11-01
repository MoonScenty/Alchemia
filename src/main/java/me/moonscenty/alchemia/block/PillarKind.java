package me.moonscenty.alchemia.block;

import java.util.function.Supplier;

import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

public enum PillarKind implements StringRepresentable {
    NORMAL("normal", () -> ModBlocks.ARCANE_STONE.block().get()),
    ANCIENT("ancient", () -> ModBlocks.ANCIENT_STONE.block().get()),
    ELDRITCH("eldritch", () -> ModBlocks.ELDRITCH_STONE.block().get());

    private final String name;
    private final Supplier<Block> stone;

    PillarKind(String name, Supplier<Block> stone) {
        this.name = name;
        this.stone = stone;
    }

    public Block stone() {
        return stone.get();
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
