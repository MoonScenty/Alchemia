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
import net.minecraft.world.entity.LivingEntity;
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
 *
 * <p>What is worked into the focus makes it eat faster, take its neighbours with it, give more out of what it
 * breaks, or take the block up whole. The fortune and silk touch of it are the game's own enchantments, laid on
 * a tool nobody holds: the block is asked what it gives up to a pick with them on it.
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

    /** How often dowsing finds twice as much in an ore, and what a rank of treasure adds to that. */
    private static final float DOWSES = 0.2F;
    private static final float DOWSES_BETTER = 0.075F;

    /** What one rank of force adds to how fast it eats, hard going and soft going apiece. */
    private static final float FASTER = 0.1F;
    private static final float FASTER_SOFT = 0.25F;

    public static boolean cast(ServerLevel level, LivingEntity who, ItemStack wand, HitResult aim, int heldFor) {
        if (!(who instanceof Player caster)) {
            return false;
        }
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

        int potency = me.moonscenty.alchemia.wand.Casting.worked(wand,
                me.moonscenty.alchemia.wand.FocusUpgrade.POTENCY);
        Progress was = DIGGING.get(caster.getUUID());
        float eaten = was != null && was.at().equals(at) ? was.eaten() : 0.0F;
        int since = was != null && was.at().equals(at) ? was.since() + 1 : 0;
        eaten += bites(block, potency);

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
        if (take(level, caster, wand, at, block)) {
            // a widened beam pulls its neighbours out with it, one of the same kind per rank
            int wider = me.moonscenty.alchemia.wand.Casting.worked(wand,
                    me.moonscenty.alchemia.wand.FocusUpgrade.ENLARGE);
            for (int more = 0; more < wider; more++) {
                takeNeighbour(level, caster, wand, at, block);
            }
        }
        return true;
    }

    /**
     * Breaking one block and handing over what it gives.
     * <p>
     * The focus is not a tool and nobody is swinging anything, so a stand-in pick is made carrying whatever the
     * focus was given -- fortune, or silk touch -- and the block is asked what it drops for that. It is the one
     * way to get the game's own answer rather than guessing at it block by block.
     */
    private static boolean take(ServerLevel level, Player caster, ItemStack wand, BlockPos at, BlockState block) {
        if (!mayBreak(level, caster, at)) {
            return false;
        }
        var entity = block.hasBlockEntity() ? level.getBlockEntity(at) : null;
        if (!caster.isCreative()) {
            ItemStack pick = digging(level, wand);
            boolean dowsing = me.moonscenty.alchemia.wand.Casting.has(wand,
                    me.moonscenty.alchemia.wand.FocusUpgrade.DOWSING);
            int treasure = me.moonscenty.alchemia.wand.Casting.worked(wand,
                    me.moonscenty.alchemia.wand.FocusUpgrade.TREASURE);
            for (ItemStack drop : net.minecraft.world.level.block.Block.getDrops(block, level, at, entity,
                    caster, pick)) {
                net.minecraft.world.level.block.Block.popResource(level, at,
                        dowsing ? dowsed(level, at, block, drop, treasure) : drop);
            }
            block.spawnAfterBreak(level, at, pick, true);
        }
        level.destroyBlock(at, false, caster);
        return true;
    }

    /**
     * What dowsing does to one drop.
     * <p>
     * <b>Different from the original.</b> The original turned the drop into a native cluster, which smelted to
     * twice the ingots. There is no such item in this mod, so what it does instead is give twice the drop --
     * the same amount of metal, come by without a second item to explain. The chance is the original's, and
     * treasure makes it likelier the same way it did.
     */
    private static ItemStack dowsed(ServerLevel level, BlockPos at, BlockState block, ItemStack drop,
            int treasure) {
        if (!block.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)
                || level.random.nextFloat() > DOWSES + treasure * DOWSES_BETTER) {
            return drop;
        }
        ItemStack richer = drop.copy();
        richer.grow(drop.getCount());
        level.playSound(null, at, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.2F, 0.8F);
        return richer;
    }

    /** One more of the same block beside the one just taken, for a beam that has been widened. */
    private static void takeNeighbour(ServerLevel level, Player caster, ItemStack wand, BlockPos at,
            BlockState block) {
        java.util.List<net.minecraft.core.Direction> sides =
                new java.util.ArrayList<>(java.util.List.of(net.minecraft.core.Direction.values()));
        java.util.Collections.shuffle(sides, new java.util.Random(level.random.nextLong()));
        for (net.minecraft.core.Direction side : sides) {
            BlockPos beside = at.relative(side);
            if (level.getBlockState(beside) == block && take(level, caster, wand, beside, block)) {
                return;
            }
        }
    }

    /**
     * The pick nobody is holding.
     * <p>
     * A diamond pick because it has to be good enough that the block counts it as properly mined; what matters
     * is the enchantments laid on it, which are the focus's own upgrades spoken in the game's words.
     */
    private static ItemStack digging(ServerLevel level, ItemStack wand) {
        ItemStack pick = new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
        var enchantments = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        if (me.moonscenty.alchemia.wand.Casting.has(wand, me.moonscenty.alchemia.wand.FocusUpgrade.SILK_TOUCH)) {
            pick.enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
            return pick;
        }
        int treasure = me.moonscenty.alchemia.wand.Casting.worked(wand,
                me.moonscenty.alchemia.wand.FocusUpgrade.TREASURE);
        if (treasure > 0) {
            pick.enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),
                    treasure);
        }
        return pick;
    }

    /** How much of a block this takes in a tick. Earth and stone give; everything else is slow going. */
    private static float bites(BlockState block, int potency) {
        MapColor colour = block.getMapColor(null, null);
        boolean soft = colour == MapColor.STONE || colour == MapColor.DIRT || colour == MapColor.SAND
                || colour == MapColor.GRASS || colour == MapColor.COLOR_BROWN;
        float bite = soft ? EATS_SOFT + potency * FASTER_SOFT : EATS + potency * FASTER;
        // obsidian goes faster than its hardness would say. The original made a point of it
        return block.is(net.minecraft.world.level.block.Blocks.OBSIDIAN) ? bite * EATS_OBSIDIAN : bite;
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
