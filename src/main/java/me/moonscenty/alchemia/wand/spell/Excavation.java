package me.moonscenty.alchemia.wand.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A beam that chews through whatever block it is held on.
 *
 * <p>Nothing happens at once. A block has to be held in the beam until it gives, and how long that takes is its
 * own hardness against how fast this eats it -- quickly through stone and earth, slowly through anything else.
 * Look away and the block starts again from nothing, which is what stops somebody sweeping a wall down by
 * glancing along it.
 *
 * <p>How far along each digger is is kept here rather than on the wand, because it belongs to the holding of the
 * button rather than to the wand: a wand handed to somebody else mid-dig has not dug anything for them.
 */
public final class Excavation {
    /** How fast it eats an ordinary block, and how fast it eats stone, earth and sand. */
    private static final float EATS = 0.05F;
    private static final float EATS_SOFT = 0.25F;
    /** Obsidian goes faster than its hardness would say. The original made a point of it. */
    private static final float EATS_OBSIDIAN = 3.0F;

    /** How often the rumble is heard, in ticks, rather than once a tick for as long as the button is down. */
    private static final int RUMBLES_EVERY = 24;

    private static final Map<UUID, Progress> DIGGING = new HashMap<>();

    /** How far into one block one digger has got. */
    private record Progress(BlockPos at, float eaten, int since) {
    }

    private Excavation() {
    }

    public static boolean cast(ServerLevel level, Player caster, ItemStack wand, HitResult aim, int heldFor) {
        if (!(aim instanceof BlockHitResult struck) || aim.getType() != HitResult.Type.BLOCK) {
            DIGGING.remove(caster.getUUID());
            return false;
        }
        BlockPos at = struck.getBlockPos();
        BlockState block = level.getBlockState(at);
        float hardness = block.getDestroySpeed(level, at);
        // bedrock and its kind answer to nothing, and neither does a block somebody is not allowed to break
        if (hardness < 0.0F || block.isAir() || !mayBreak(level, caster, at)) {
            DIGGING.remove(caster.getUUID());
            return false;
        }

        Progress was = DIGGING.get(caster.getUUID());
        float eaten = was != null && was.at().equals(at) ? was.eaten() : 0.0F;
        int since = was != null && was.at().equals(at) ? was.since() + 1 : 0;
        eaten += bites(block);

        chips(level, at, block, since);
        if (eaten < hardness) {
            DIGGING.put(caster.getUUID(), new Progress(at, eaten, since));
            if (caster instanceof ServerPlayer watcher) {
                level.destroyBlockProgress(watcher.getId(), at, (int) (eaten / hardness * 9.0F));
            }
            return true;
        }

        DIGGING.remove(caster.getUUID());
        if (caster instanceof ServerPlayer watcher) {
            level.destroyBlockProgress(watcher.getId(), at, -1);
        }
        level.destroyBlock(at, true, caster);
        return true;
    }

    /** How much of a block this takes in a tick. Earth and stone give; everything else is slow going. */
    private static float bites(BlockState block) {
        if (block.is(net.minecraft.world.level.block.Blocks.OBSIDIAN)) {
            return EATS * EATS_OBSIDIAN;
        }
        MapColor colour = block.getMapColor(null, null);
        boolean soft = colour == MapColor.STONE || colour == MapColor.DIRT || colour == MapColor.SAND
                || colour == MapColor.GRASS || colour == MapColor.COLOR_BROWN;
        return soft ? EATS_SOFT : EATS;
    }

    /** Whether this digger is allowed to take this block, which a protected world or a spawn may say no to. */
    private static boolean mayBreak(ServerLevel level, Player caster, BlockPos at) {
        if (level.isOutsideBuildHeight(at) || !caster.mayBuild()) {
            return false;
        }
        return !(caster instanceof ServerPlayer digger) || digger.mayInteract(level, at);
    }

    /** Chips off the face being eaten, and a rumble now and then rather than every tick. */
    private static void chips(ServerLevel level, BlockPos at, BlockState block, int since) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, block),
                at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.0);
        if (since % RUMBLES_EVERY == 0) {
            level.playSound(null, at, SoundEvents.STONE_HIT, SoundSource.PLAYERS, 0.3F, 0.6F);
        }
    }
}
