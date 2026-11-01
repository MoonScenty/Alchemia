package me.moonscenty.alchemia.wand.spell;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModSounds;
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

// both foci charge per block placed and never replace a block entity
public final class Swapping {
    // longest row, and what each rank of enlarge adds
    private static final int LAYS_AT_MOST = 16;
    private static final int LAYS_FURTHER = 8;
    // trade spread in steps, and what each rank of enlarge adds; original's value
    private static final int SPREADS = 3;
    private static final int SPREADS_FURTHER = 2;

    private Swapping() {
    }

    // row length is the caster's distance from the face, measured along its axis
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
        int reaches = Math.min(lays(wand), far(against, out, caster));
        List<BlockPos> row = new ArrayList<>();
        for (int along = 1; along < reaches; along++) {
            row.add(against.relative(out, along));
        }
        return lay(level, caster, wand, laying, row, false);
    }

    // spreads through connected blocks of the same kind, not a box
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
        return lay(level, caster, wand, laying, spreadFrom(level, first, was, spreads(wand)), true);
    }

    // returns false so choosing a block is never charged
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

    private static Block remembered(ItemStack wand) {
        Holder<Block> picked = WandItem.focus(wand).get(ModDataComponents.PICKED_BLOCK.get());
        return picked == null ? null : picked.value();
    }

    private static int far(BlockPos against, Direction out, Player caster) {
        return switch (out.getAxis()) {
            case X -> (int) Math.abs(against.getX() + 0.5 - caster.getX());
            case Y -> (int) Math.abs(against.getY() + 0.5 - caster.getY());
            case Z -> (int) Math.abs(against.getZ() + 0.5 - caster.getZ());
        };
    }

    private static int lays(ItemStack wand) {
        int most = LAYS_AT_MOST + Casting.worked(wand, FocusUpgrade.ENLARGE) * LAYS_FURTHER;
        return set(wand, most);
    }

    private static int spreads(ItemStack wand) {
        int most = SPREADS + Casting.worked(wand, FocusUpgrade.ENLARGE) * SPREADS_FURTHER;
        return set(wand, most);
    }

    private static int set(ItemStack wand, int most) {
        if (!Casting.has(wand, FocusUpgrade.ARCHITECT)) {
            return most;
        }
        Integer reach = WandItem.focus(wand).get(ModDataComponents.FOCUS_REACH.get());
        return reach == null ? most : Math.max(1, Math.min(most, reach));
    }

    // the stored reach is a row length for builder and a step count for trade
    public static void setReach(Player caster, ItemStack wand) {
        ItemStack focus = WandItem.focus(wand);
        if (!Casting.has(wand, FocusUpgrade.ARCHITECT) || focus.isEmpty()) {
            return;
        }
        boolean building = focus.is(me.moonscenty.alchemia.registry.ModItems.FOCI.get("builder").get());
        int most = building
                ? LAYS_AT_MOST + Casting.worked(wand, FocusUpgrade.ENLARGE) * LAYS_FURTHER
                : SPREADS + Casting.worked(wand, FocusUpgrade.ENLARGE) * SPREADS_FURTHER;
        Integer was = focus.get(ModDataComponents.FOCUS_REACH.get());
        int now = was == null || was <= 1 ? most : Math.min(most, was) - 1;

        ItemStack set = focus.copy();
        set.set(ModDataComponents.FOCUS_REACH.get(), now);
        WandItem.setFocus(wand, set);
        caster.displayClientMessage(Component.translatable("item.alchemia.focus.reach", now), true);
    }

    private static List<BlockPos> spreadFrom(ServerLevel level, BlockPos first, Block kind, int reach) {
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
            if (taken >= reach) {
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

    private static boolean lay(ServerLevel level, Player caster, ItemStack wand, Block laying,
            List<BlockPos> where, boolean breaking) {
        var focus = me.moonscenty.alchemia.wand.Casting.fitted(wand);
        if (focus == null || where.isEmpty()) {
            return false;
        }
        var price = focus.cost();
        VisHolder holder = VisHolder.of(wand);
        boolean any = false;

        for (BlockPos at : where) {
            BlockState there = level.getBlockState(at);
            boolean room = breaking ? !there.isAir() && level.getBlockEntity(at) == null
                    : there.canBeReplaced();
            if (!room || there.getDestroySpeed(level, at) < 0.0F || !caster.mayInteract(level, at)) {
                continue;
            }
            // the caller already paid for the first square
            if (any && (holder == null || !holder.holds(wand, price, caster, VisHolder.Spending.WORKING))) {
                break;
            }
            if (!takeOne(caster, laying)) {
                break;
            }
            if (any && holder != null) {
                holder.take(wand, price, caster, VisHolder.Spending.WORKING);
            }

            if (breaking) {
                // drops go through a stand-in tool so fortune and silk touch apply
                if (!caster.isCreative()) {
                    net.minecraft.world.level.block.Block.dropResources(there, level, at,
                            level.getBlockEntity(at), caster, tool(level, wand));
                }
                level.destroyBlock(at, false, caster);
            }
            level.setBlock(at, laying.defaultBlockState(), 3);
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.WAND.get(),
                    SoundSource.PLAYERS, 0.25F, 1.0F);
            level.playSound(null, at, ModSounds.POOF.get(), SoundSource.BLOCKS, 0.4F,
                    1.0F + (float) level.random.nextGaussian() * 0.05F);
            any = true;
        }
        return any;
    }

    private static ItemStack tool(ServerLevel level, ItemStack wand) {
        ItemStack pick = new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
        var enchantments = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        if (Casting.has(wand, FocusUpgrade.SILK_TOUCH)) {
            pick.enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
            return pick;
        }
        int treasure = Casting.worked(wand, FocusUpgrade.TREASURE);
        if (treasure > 0) {
            pick.enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),
                    treasure);
        }
        return pick;
    }

    // isCreative() rather than abilities: the two disagree for GameTest mock players
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
