package me.moonscenty.alchemia.block;

import java.util.function.Supplier;

import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

/**
 * What an altar is built of: arcane stone, the ancient stone of the Outer Lands, or eldritch stone. The corners and
 * the pedestal can each be any of the three, and what they are changes how the altar works.
 * <p>
 * Four ancient corners work a little faster and a tenth cheaper, and shake a little more. Four eldritch corners
 * work faster still, cost a little more, and are a good deal steadier. An ancient pedestal takes a part in a
 * hundred off the bill, and four of them add a point of shaking; an eldritch one adds a quarter part, and one
 * holding something shakes a point more.
 */
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

    /** The stone a corner of this kind is built of, and gives back. */
    public Block stone() {
        return stone.get();
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
