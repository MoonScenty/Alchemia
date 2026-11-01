package me.moonscenty.alchemia.block.entity;

import java.util.List;

import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NodeStabilizerBlockEntity extends BlockEntity {
    private static final double ANCHOR_UP = 1.5;
    // original used 0.5; wider until nodes can be carried into place
    private static final double REACH = 2.5;

    private static final double DRAW_IN = 1000.0;
    private static final double PUSH_OFF = 750.0;
    private static final double SETTLED = 0.001;

    public static final int STROKE = 37;

    private int hold;
    private int sinceLooked;

    public NodeStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NODE_STABILIZER.get(), pos, state);
    }

    public Vec3 anchor() {
        return new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + ANCHOR_UP, worldPosition.getZ() + 0.5);
    }

    public float reachOut() {
        return hold / (float) STROKE;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NodeStabilizerBlockEntity stabilizer) {
        stabilizer.work(level, pos);
    }

    private void work(Level level, BlockPos pos) {
        boolean letGo = level.hasNeighborSignal(pos);
        boolean anything = false;
        if (++sinceLooked % 10 == 0 || hold > 0) {
            anything = cradle(level, letGo);
        }

        hold = Mth.clamp(hold + (anything && !letGo ? 1 : -1), 0, STROKE);
    }

    private boolean cradle(Level level, boolean letGo) {
        Vec3 anchor = anchor();
        List<AuraNode> near = level.getEntitiesOfClass(AuraNode.class, new AABB(anchor, anchor).inflate(REACH),
                AuraNode::isAlive);
        if (near.isEmpty()) {
            return false;
        }

        near.sort((one, other) -> Double.compare(one.position().distanceToSqr(anchor),
                other.position().distanceToSqr(anchor)));

        boolean first = true;
        for (AuraNode node : near) {
            if (letGo) {
                node.setHeld(false);
                continue;
            }
            node.setHeld(first);
            nudge(node, anchor, first);
            first = false;
        }
        return true;
    }

    private static void nudge(AuraNode node, Vec3 anchor, boolean holding) {
        Vec3 away = anchor.subtract(node.position());
        if (away.lengthSqr() <= SETTLED) {
            if (!holding) {
                // exactly on top of the held node: nudge up instead of dividing by zero
                node.setDeltaMovement(node.getDeltaMovement().add(0.0, 0.005, 0.0));
            }
            return;
        }
        Vec3 push = away.normalize().scale(1.0 / (holding ? DRAW_IN : -PUSH_OFF));
        node.setDeltaMovement(node.getDeltaMovement().add(push));
    }
}
