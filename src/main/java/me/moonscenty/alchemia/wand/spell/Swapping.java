package me.moonscenty.alchemia.wand.spell;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The two foci that put blocks where other blocks are, or where nothing is.
 *
 * <p>Both are shown a block first. Crouch and use, and the focus remembers what it was pointed at; after that,
 * using it lays that block out. Showing it something costs nothing -- it is not a working, it is a decision.
 *
 * <p>Both pay by the block rather than by the use. A wand that could wall off a valley for the price of one block
 * would be a wand nobody ever built anything with by hand again, and the original charged the same way.
 *
 * <p>Neither will touch a block with something inside it. A chest swapped for cobble is a chest's worth of
 * somebody's afternoon gone, and no amount of vis puts it back.
 */
public final class Swapping {
    /** How far a builder will lay a row, however far away the wall is. */
    private static final int LAYS_AT_MOST = 16;
    /** How far a trade spreads from the block it was aimed at, in steps. */
    private static final int SPREADS = 3;

    private Swapping() {
    }

    /**
     * Lays a row of the remembered block out from the face that was clicked, towards whoever cast it.
     * <p>
     * How long the row is is how far away they are standing, measured along the face's own axis. Standing close
     * to a wall lays a stub; standing back lays a bridge. It is the only measure that needs no explaining while
     * somebody is holding the wand.
     */
    public static boolean builder(ServerLevel level, LivingEntity who, ItemStack wand, HitResult aim, int heldFor) {
        if (!(who instanceof Player caster)) {
            return false;
        }
        if (!(aim instanceof BlockHitResult struck) || aim.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        if (caster.isShiftKeyDown()) {
            return shown(level, caster, wand, struck);
        }
        Block laying = remembered(wand);
        if (laying == null) {
            return false;
        }

        BlockPos against = struck.getBlockPos();
        Direction out = struck.getDirection();
        int reaches = Math.min(LAYS_AT_MOST, far(against, out, caster));
        List<BlockPos> row = new ArrayList<>();
        for (int along = 1; along < reaches; along++) {
            row.add(against.relative(out, along));
        }
        return lay(level, caster, wand, laying, row, false);
    }

    /**
     * Swaps the block that was aimed at, and everything of the same kind within reach of it, for the remembered
     * one.
     * <p>
     * It spreads rather than filling a box: a vein of ore turns to stone and the stone around it is left alone.
     * Three steps, as the original had it.
     */
    public static boolean trade(ServerLevel level, LivingEntity who, ItemStack wand, HitResult aim, int heldFor) {
        if (!(who instanceof Player caster)) {
            return false;
        }
        if (!(aim instanceof BlockHitResult struck) || aim.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        if (caster.isShiftKeyDown()) {
            return shown(level, caster, wand, struck);
        }
        Block laying = remembered(wand);
        if (laying == null) {
            return false;
        }
        BlockPos first = struck.getBlockPos();
        Block was = level.getBlockState(first).getBlock();
        if (was == laying) {
            return false;
        }
        return lay(level, caster, wand, laying, spreadFrom(level, first, was), true);
    }

    /** Being shown a block. It costs nothing, so it says so by handing back nothing done. */
    private static boolean shown(ServerLevel level, Player caster, ItemStack wand, BlockHitResult struck) {
        BlockState looked = level.getBlockState(struck.getBlockPos());
        if (looked.isAir() || level.getBlockEntity(struck.getBlockPos()) != null) {
            return false;
        }
        ItemStack focus = WandItem.focus(wand).copy();
        focus.set(ModDataComponents.PICKED_BLOCK.get(), looked.getBlock().builtInRegistryHolder());
        WandItem.setFocus(wand, focus);
        caster.displayClientMessage(Component.translatable("item.alchemia.focus.copying",
                looked.getBlock().getName()), true);
        return false;
    }

    /** What this focus was last shown, or nothing. */
    private static Block remembered(ItemStack wand) {
        Holder<Block> picked = WandItem.focus(wand).get(ModDataComponents.PICKED_BLOCK.get());
        return picked == null ? null : picked.value();
    }

    /** How far the caster is standing from a face, measured along that face's own axis. */
    private static int far(BlockPos against, Direction out, Player caster) {
        return switch (out.getAxis()) {
            case X -> (int) Math.abs(against.getX() + 0.5 - caster.getX());
            case Y -> (int) Math.abs(against.getY() + 0.5 - caster.getY());
            case Z -> (int) Math.abs(against.getZ() + 0.5 - caster.getZ());
        };
    }

    /** Everything of one kind reachable from a starting block within {@value #SPREADS} steps of it. */
    private static List<BlockPos> spreadFrom(ServerLevel level, BlockPos first, Block kind) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> edge = new ArrayDeque<>();
        Deque<Integer> steps = new ArrayDeque<>();
        edge.add(first);
        steps.add(0);
        seen.add(first);

        while (!edge.isEmpty()) {
            BlockPos at = edge.poll();
            int taken = steps.poll();
            found.add(at);
            if (taken >= SPREADS) {
                continue;
            }
            for (Direction side : Direction.values()) {
                BlockPos next = at.relative(side);
                if (seen.add(next) && level.getBlockState(next).is(kind)) {
                    edge.add(next);
                    steps.add(taken + 1);
                }
            }
        }
        return found;
    }

    /**
     * Lays the block out, one square at a time, for as long as the wand and the bag can keep up.
     * <p>
     * Each square is paid for on its own and stops the whole thing when it cannot be. Running dry halfway leaves
     * half a wall, which is what running dry ought to look like.
     *
     * @param breaking whether what is already there is taken out and handed back, or only built into
     */
    private static boolean lay(ServerLevel level, Player caster, ItemStack wand, Block laying,
            List<BlockPos> where, boolean breaking) {
        FocusItem focus = me.moonscenty.alchemia.wand.Casting.fittedTo(wand);
        if (focus == null || where.isEmpty()) {
            return false;
        }
        var price = focus.focus().cost();
        VisHolder holder = VisHolder.of(wand);
        boolean any = false;

        for (BlockPos at : where) {
            BlockState there = level.getBlockState(at);
            boolean room = breaking ? !there.isAir() && level.getBlockEntity(at) == null
                    : there.canBeReplaced();
            if (!room || there.getDestroySpeed(level, at) < 0.0F || !caster.mayInteract(level, at)) {
                continue;
            }
            // the first square is paid for by whoever called this; every one after it pays its own way
            if (any && (holder == null || !holder.holds(wand, price, caster))) {
                break;
            }
            if (!takeOne(caster, laying)) {
                break;
            }
            if (any && holder != null) {
                holder.take(wand, price, caster);
            }

            if (breaking) {
                level.destroyBlock(at, true, caster);
            }
            level.setBlock(at, laying.defaultBlockState(), 3);
            any = true;
        }

        if (any) {
            level.playSound(null, where.get(0), laying.defaultBlockState().getSoundType().getPlaceSound(),
                    SoundSource.BLOCKS, 0.6F, 1.0F);
        }
        return any;
    }

    /**
     * Takes one of the block out of the bag, unless the bag is bottomless.
     * <p>
     * Asked of the player rather than of their abilities: the two agree in a game being played, and only the
     * question agrees with a player stood up for a test.
     */
    private static boolean takeOne(Player caster, Block laying) {
        if (caster.isCreative()) {
            return true;
        }
        int slot = caster.getInventory().findSlotMatchingItem(new ItemStack(laying));
        if (slot < 0) {
            return false;
        }
        caster.getInventory().removeItem(slot, 1);
        return true;
    }
}
