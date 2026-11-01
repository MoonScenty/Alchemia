package me.moonscenty.alchemia.wand.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.moonscenty.alchemia.registry.ModSounds;
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

// dig progress is per caster and resets when the beam moves off the block
public final class Excavation {
    // per-tick dig rate; soft means stone, earth and sand by map colour
    private static final float EATS = 0.05F;
    private static final float EATS_SOFT = 0.25F;
    // original's obsidian bonus
    private static final float EATS_OBSIDIAN = 3.0F;

    private static final int RUMBLES_EVERY = 24;

    private static final Map<UUID, Progress> DIGGING = new HashMap<>();

    private record Progress(BlockPos at, float eaten, int since) {
    }

    private Excavation() {
    }

    // dowsing cluster chance, and what each rank of treasure adds; original's values
    private static final float DOWSES = 0.2F;
    private static final float DOWSES_BETTER = 0.075F;

    // per rank of potency
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
            int wider = me.moonscenty.alchemia.wand.Casting.worked(wand,
                    me.moonscenty.alchemia.wand.FocusUpgrade.ENLARGE);
            for (int more = 0; more < wider; more++) {
                takeNeighbour(level, caster, wand, at, block);
            }
        }
        return true;
    }

    // drops come from a stand-in pick carrying the focus's fortune or silk touch
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

    private static ItemStack dowsed(ServerLevel level, BlockPos at, BlockState block, ItemStack drop,
            int treasure) {
        ItemStack found = me.moonscenty.alchemia.item.Clusters.maybe(drop, DOWSES + treasure * DOWSES_BETTER,
                level.random);
        if (found != drop) {
            level.playSound(null, at, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F, 0.7F);
        }
        return found;
    }

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

    // diamond so every block counts as properly mined
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

    private static float bites(BlockState block, int potency) {
        MapColor colour = block.getMapColor(null, null);
        boolean soft = colour == MapColor.STONE || colour == MapColor.DIRT || colour == MapColor.SAND
                || colour == MapColor.GRASS || colour == MapColor.COLOR_BROWN;
        float bite = soft ? EATS_SOFT + potency * FASTER_SOFT : EATS + potency * FASTER;
        return block.is(net.minecraft.world.level.block.Blocks.OBSIDIAN) ? bite * EATS_OBSIDIAN : bite;
    }

    private static boolean mayBreak(ServerLevel level, Player caster, BlockPos at) {
        if (level.isOutsideBuildHeight(at) || !caster.mayBuild()) {
            return false;
        }
        return !(caster instanceof ServerPlayer digger) || digger.mayInteract(level, at);
    }

    private static void chips(ServerLevel level, BlockPos at, BlockState block, int since) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, block),
                at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.0);
        if (since % RUMBLES_EVERY == 0) {
            level.playSound(null, at, ModSounds.RUMBLE.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
        }
    }
}
