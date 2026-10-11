package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.block.LampBlock;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * An arcane lamp at work: setting down points of light in the dark places round it, one at a time.
 *
 * <p>Every quarter of a second it picks somewhere within fifteen blocks and, if that place is empty, dark, and in
 * plain sight of the lamp, leaves a light there. Over a few minutes a cave fills with them. None is set more
 * than four blocks above the ground, so a lamp in the open does not light the sky.
 */
public class ArcaneLampBlockEntity extends BlockEntity {
    /** How often it sets a light down, how far out it reaches, and how dark a place has to be. All the original's. */
    private static final int EVERY = 5;
    private static final int REACHES = 15;
    private static final int DARKER_THAN = 11;
    /** How far above the ground a light may hang, and how near the bottom of the world it may go. */
    private static final int ABOVE_GROUND = 4;
    private static final int OFF_THE_BOTTOM = 5;

    public ArcaneLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_LAMP.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArcaneLampBlockEntity lamp) {
        if (level.getGameTime() % EVERY != 0 || !state.getValue(LampBlock.ENABLED)) {
            return;
        }
        BlockPos spot = pos.offset(spread(level), spread(level), spread(level));
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, spot.getX(), spot.getZ());
        if (spot.getY() > ground + ABOVE_GROUND) {
            spot = new BlockPos(spot.getX(), ground + ABOVE_GROUND, spot.getZ());
        }
        if (spot.getY() < level.getMinBuildHeight() + OFF_THE_BOTTOM) {
            spot = new BlockPos(spot.getX(), level.getMinBuildHeight() + OFF_THE_BOTTOM, spot.getZ());
        }
        if (level.isEmptyBlock(spot) && level.getBrightness(LightLayer.BLOCK, spot) < DARKER_THAN
                && inSight(level, pos, spot)) {
            level.setBlock(spot, ModBlocks.LAMP_LIGHT.get().defaultBlockState(), 3);
        }
    }

    /** As the original drew it: two throws of fifteen, one taken from the other, so the middle is likelier. */
    private static int spread(Level level) {
        return level.random.nextInt(16) - level.random.nextInt(16);
    }

    /** Whether nothing stands between the lamp and that place. */
    private static boolean inSight(Level level, BlockPos lamp, BlockPos spot) {
        Vec3 from = Vec3.atCenterOf(lamp);
        Vec3 to = Vec3.atCenterOf(spot);
        var seen = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                net.minecraft.world.phys.shapes.CollisionContext.empty()));
        return seen.getType() == HitResult.Type.MISS || seen.getBlockPos().equals(lamp) || seen.getBlockPos().equals(spot);
    }

    /** Puts out every light it set down. Called when the lamp is put out or taken away. */
    public void takeBackLights() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (BlockPos at : BlockPos.betweenClosed(worldPosition.offset(-REACHES, -REACHES, -REACHES),
                worldPosition.offset(REACHES, REACHES, REACHES))) {
            if (level.getBlockState(at).is(ModBlocks.LAMP_LIGHT.get())) {
                level.removeBlock(at, false);
            }
        }
    }
}
