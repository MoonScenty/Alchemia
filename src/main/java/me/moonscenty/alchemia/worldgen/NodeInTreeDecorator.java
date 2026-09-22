package me.moonscenty.alchemia.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Hangs a node inside the trunk of a tree, some of the time.
 * <p>
 * A silverwood is where the original put its pure nodes: a small one, partway up, in the heart of the wood. Since
 * the same tree is what a sapling grows into, a planted silverwood has the same chance as a wild one.
 */
public class NodeInTreeDecorator extends TreeDecorator {
    public static final MapCodec<NodeInTreeDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(decorator -> decorator.chance),
            // not "type": the dispatcher already writes the decorator's own type under that name
            NodeType.CODEC.fieldOf("node_type").forGetter(decorator -> decorator.type),
            Codec.floatRange(0.0F, 1.0F).fieldOf("size").forGetter(decorator -> decorator.size))
            .apply(instance, NodeInTreeDecorator::new));

    private final float chance;
    private final NodeType type;
    /** How large the node is next to one drawn up in the open. */
    private final float size;

    public NodeInTreeDecorator(float chance, NodeType type, float size) {
        this.chance = chance;
        this.type = type;
        this.size = size;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModFeatures.NODE_IN_TREE.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        // a decorator is only handed a reader, but the world it reads is one an entity can be put into
        if (!(context.level() instanceof WorldGenLevel level) || context.logs().isEmpty()) {
            return;
        }
        RandomSource random = context.random();
        if (random.nextFloat() >= chance) {
            return;
        }

        List<BlockPos> heart = heartwood(context.logs());
        // partway up, give or take a block
        int up = heart.size() / 2 - 1 + random.nextInt(3);
        BlockPos at = heart.get(Math.clamp(up, 0, heart.size() - 1));

        AuraNode node = new AuraNode(level.getLevel());
        node.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        node.drawUp(random);
        node.setType(type);
        node.setSize(Math.max(1, (int) (node.getSize() * size)));
        level.addFreshEntity(node);
    }

    /**
     * The column with the most wood in it, bottom to top.
     * <p>
     * Roots, buttresses and knots all count as logs, so the trunk is picked out as the one column that runs the
     * whole height rather than assumed to be wherever the first log lies.
     */
    private static List<BlockPos> heartwood(List<BlockPos> logs) {
        Map<Long, List<BlockPos>> columns = new HashMap<>();
        for (BlockPos log : logs) {
            columns.computeIfAbsent(BlockPos.asLong(log.getX(), 0, log.getZ()), key -> new ArrayList<>()).add(log);
        }
        List<BlockPos> tallest = List.of();
        for (List<BlockPos> column : columns.values()) {
            if (column.size() > tallest.size()) {
                tallest = column;
            }
        }
        // the logs came sorted by height, and grouping them kept that order
        return tallest;
    }
}
