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

/**
 * Holds a node still, and draws it in to the spot it is meant to sit.
 * <p>
 * The block does the holding rather than the node doing the looking: it reaches for whatever has drifted near the
 * spot above it, pulls that one in and keeps it, and pushes any others away so two never end up sharing a cradle.
 */
public class NodeStabilizerBlockEntity extends BlockEntity {
    /** Where the node is held: the middle of the space one block up. */
    private static final double ANCHOR_UP = 1.5;
    /**
     * How near a node has to drift before the arms can reach it.
     * <p>
     * The original wanted it within half a block, which is only arrangeable with something to carry nodes about.
     * Until there is, a wider reach is what makes one usable at all.
     */
    private static final double REACH = 2.5;

    /** How hard the held one is drawn in, and how hard the rest are pushed off. */
    private static final double DRAW_IN = 1000.0;
    private static final double PUSH_OFF = 750.0;
    /** Near enough that pulling further would only make it jitter. */
    private static final double SETTLED = 0.001;

    /** How long the arms take to run all the way out, in ticks. */
    public static final int STROKE = 37;

    private int hold;
    private int sinceLooked;

    public NodeStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NODE_STABILIZER.get(), pos, state);
    }

    /** Where a node is held, in world coordinates. */
    public Vec3 anchor() {
        return new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + ANCHOR_UP, worldPosition.getZ() + 0.5);
    }

    /** How far the arms are out, from nothing to all the way. */
    public float reachOut() {
        return hold / (float) STROKE;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NodeStabilizerBlockEntity stabilizer) {
        stabilizer.work(level, pos);
    }

    private void work(Level level, BlockPos pos) {
        boolean letGo = level.hasNeighborSignal(pos);
        // looking for entities is not cheap, and a node cannot arrive from nowhere between one second and the next
        boolean anything = false;
        if (++sinceLooked % 10 == 0 || hold > 0) {
            anything = cradle(level, letGo);
        }

        // the arms run out while something is held and draw back in when it is let go
        hold = Mth.clamp(hold + (anything && !letGo ? 1 : -1), 0, STROKE);
    }

    /**
     * Takes hold of whichever node is nearest and shoves the rest off.
     *
     * @return whether there was one to hold
     */
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

    /** The nearest is drawn towards the cradle; everything else is worked away from it. */
    private static void nudge(AuraNode node, Vec3 anchor, boolean holding) {
        Vec3 away = anchor.subtract(node.position());
        if (away.lengthSqr() <= SETTLED) {
            if (!holding) {
                // sat exactly on top of the held one, so lift it clear rather than dividing by nothing
                node.setDeltaMovement(node.getDeltaMovement().add(0.0, 0.005, 0.0));
            }
            return;
        }
        Vec3 push = away.normalize().scale(1.0 / (holding ? DRAW_IN : -PUSH_OFF));
        node.setDeltaMovement(node.getDeltaMovement().add(push));
    }
}
