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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

// Planted silverwoods get the same node chance as wild ones, since they grow from the same feature.
public class NodeInTreeDecorator extends TreeDecorator {
    public static final MapCodec<NodeInTreeDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(decorator -> decorator.chance),
            // not "type": the dispatcher already writes that key
            NodeType.CODEC.fieldOf("node_type").forGetter(decorator -> decorator.type),
            Codec.floatRange(0.0F, 1.0F).fieldOf("size").forGetter(decorator -> decorator.size))
            .apply(instance, NodeInTreeDecorator::new));

    private final float chance;
    private final NodeType type;
    // relative to a node drawn up in the open
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
        // only a reader is handed in, but during worldgen it is a WorldGenLevel
        if (!(context.level() instanceof WorldGenLevel level) || context.logs().isEmpty()) {
            return;
        }
        RandomSource random = context.random();
        if (random.nextFloat() >= chance) {
            return;
        }

        List<BlockPos> heart = heartwood(level, context.logs());
        int up = heart.size() / 2 - 1 + random.nextInt(3);
        BlockPos at = heart.get(Math.clamp(up, 0, heart.size() - 1));

        AuraNode node = new AuraNode(level.getLevel());
        node.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        node.drawUp(random);
        node.setType(type);
        node.setSize(Math.max(1, (int) (node.getSize() * size)));
        level.addFreshEntity(node);
    }

    // the trunk is the column running the full height; roots and knots are logs too
    private static List<BlockPos> heartwood(WorldGenLevel level, List<BlockPos> logs) {
        Map<Long, List<BlockPos>> columns = new HashMap<>();
        for (BlockPos log : logs) {
            // the dirt put under the trunk is listed among the logs
            if (!level.getBlockState(log).is(BlockTags.LOGS)) {
                continue;
            }
            columns.computeIfAbsent(BlockPos.asLong(log.getX(), 0, log.getZ()), key -> new ArrayList<>()).add(log);
        }
        List<BlockPos> tallest = List.of();
        for (List<BlockPos> column : columns.values()) {
            if (column.size() > tallest.size()) {
                tallest = column;
            }
        }
        // still sorted by height
        return tallest;
    }
}
