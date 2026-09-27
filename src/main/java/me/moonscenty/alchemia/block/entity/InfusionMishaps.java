package me.moonscenty.alchemia.block.entity;

import java.util.List;

import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.player.WarpData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * What happens when an infusion goes wrong.
 * <p>
 * Five things, none of them fatal on its own and all of them worse the longer you let the work run. The point is
 * not the damage: it is that an unstable working makes a scene, so that standing and watching one is a useful thing
 * to do rather than a waste of an afternoon.
 * <p>
 * Every mishap also makes the work a little less stable, so a bad start has a way of getting worse. That is the
 * whole argument for laying the altar out properly before starting on anything dear.
 */
public final class InfusionMishaps {
    /** How far a mishap reaches for somebody to trouble. */
    private static final double NEAR = 6.0;
    /** How hard the blast is, and how much it hurts. Neither breaks anything: the works is not the enemy. */
    private static final float BLAST = 1.8F;
    private static final float HURT = 3.0F;

    private InfusionMishaps() {
    }

    /** One thing goes wrong, chosen at random, and the work gets shakier for it. */
    public static void strike(ServerLevel level, BlockPos pos, InfusionMatrixBlockEntity matrix,
            List<BlockPos> ring) {
        switch (level.random.nextInt(5)) {
            case 0 -> throwOff(level, ring);
            case 1 -> zap(level, pos);
            case 2 -> harm(level, pos);
            case 3 -> blast(level, pos);
            default -> twist(level, pos);
        }
        matrix.worsen();
    }

    /** Something is shaken off a pedestal and thrown across the room. */
    private static void throwOff(ServerLevel level, List<BlockPos> ring) {
        List<BlockPos> standing = ring.stream()
                .filter(at -> level.getBlockEntity(at) instanceof ArcanePedestalBlockEntity stand
                        && !stand.held().isEmpty())
                .toList();
        if (standing.isEmpty()) {
            return;
        }
        BlockPos at = standing.get(level.random.nextInt(standing.size()));
        ArcanePedestalBlockEntity stand = (ArcanePedestalBlockEntity) level.getBlockEntity(at);
        ItemStack thrown = stand.held();
        stand.hold(ItemStack.EMPTY);

        ItemEntity loose = new ItemEntity(level, at.getX() + 0.5, at.getY() + 1.2, at.getZ() + 0.5, thrown);
        loose.setDeltaMovement((level.random.nextDouble() - 0.5) * 0.6, 0.4, (level.random.nextDouble() - 0.5) * 0.6);
        level.addFreshEntity(loose);
        level.playSound(null, at, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 0.6F);
    }

    /** Lightning, without the fire. A works that burns its own roof down teaches nothing. */
    private static void zap(ServerLevel level, BlockPos pos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) {
            return;
        }
        bolt.moveTo(Vec3.atBottomCenterOf(nearby(level, pos)));
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
        harm(level, pos);
    }

    /** Whoever is standing too close gets some of it. */
    private static void harm(ServerLevel level, BlockPos pos) {
        for (Player player : level.getEntitiesOfClass(Player.class, around(pos))) {
            player.hurt(level.damageSources().source(DamageTypes.MAGIC), HURT);
        }
    }

    /** A bang that rattles the windows and breaks nothing. */
    private static void blast(ServerLevel level, BlockPos pos) {
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, BLAST,
                Level.ExplosionInteraction.NONE);
    }

    /** Something looks back, and whoever was watching carries it away with them. */
    private static void twist(ServerLevel level, BlockPos pos) {
        for (Player player : level.getEntitiesOfClass(Player.class, around(pos))) {
            WarpHandler.add(player, WarpData.Kind.TEMPORARY, 1 + level.random.nextInt(2));
        }
    }

    private static AABB around(BlockPos pos) {
        return new AABB(pos).inflate(NEAR);
    }

    private static BlockPos nearby(ServerLevel level, BlockPos pos) {
        return pos.offset(level.random.nextInt(9) - 4, 0, level.random.nextInt(9) - 4);
    }
}
