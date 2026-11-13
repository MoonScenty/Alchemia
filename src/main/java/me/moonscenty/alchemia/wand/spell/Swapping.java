package me.moonscenty.alchemia.wand.spell;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.wand.ArchitectArea;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

// both foci charge per block placed and never replace a block entity
@EventBusSubscriber(modid = Alchemia.MODID)
public final class Swapping {
    // longest row, and what each rank of enlarge adds
    private static final int LAYS_AT_MOST = 16;
    private static final int LAYS_FURTHER = 8;
    // trade spread in steps, and what each rank of enlarge adds; original's value
    private static final int SPREADS = 3;
    private static final int SPREADS_FURTHER = 1;
    // architect half-width, and what each rank of enlarge adds; original's values
    private static final int AREA = 3;
    private static final int AREA_FURTHER = 2;

    private static final Map<ServerLevel, Deque<Swap>> WAITING = new WeakHashMap<>();

    private record Swap(BlockPos at, BlockState was, Block laying, int spreads, Player caster, int slot) {
    }

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
            return struck.getLocation().distanceTo(caster.getEyePosition()) <= Casting.HAND
                    && shown(level, caster, wand, struck);
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

    // original: the vein spreads from the struck block, or with an architect the chosen area goes, one block a tick
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
        BlockState was = level.getBlockState(first);
        if (level.getBlockEntity(first) != null || was.is(ModTags.Blocks.TAINT)) {
            return false;
        }
        int slot = slotOf(caster, wand);
        if (Casting.has(wand, FocusUpgrade.ARCHITECT)) {
            for (BlockPos at : area(level, caster, wand, first, struck.getDirection())) {
                queue(level, at, level.getBlockState(at), laying, 0, caster, slot);
            }
        } else {
            queue(level, first, was, laying, spreads(wand), caster, slot);
        }
        // original: the cast itself is paid for, then each block swapped
        return true;
    }

    // original: a swing swaps the one block looked at
    public static void swing(ServerLevel level, Player caster, ItemStack wand) {
        Block laying = remembered(wand);
        if (laying == null || !(Casting.aim(caster, Casting.fitted(wand)) instanceof BlockHitResult struck)
                || struck.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos at = struck.getBlockPos();
        BlockState was = level.getBlockState(at);
        if (level.getBlockEntity(at) == null && !was.is(ModTags.Blocks.TAINT)) {
            queue(level, at, was, laying, 0, caster, slotOf(caster, wand));
        }
    }

    private static int slotOf(Player caster, ItemStack wand) {
        return caster.getOffhandItem() == wand ? Inventory.SLOT_OFFHAND : caster.getInventory().selected;
    }

    private static void queue(ServerLevel level, BlockPos at, BlockState was, Block laying, int spreads,
            Player caster, int slot) {
        if (was.isAir() || was.is(laying) || was.getDestroySpeed(level, at) < 0.0F) {
            return;
        }
        WAITING.computeIfAbsent(level, waiting -> new ArrayDeque<>())
                .add(new Swap(at.immutable(), was, laying, spreads, caster, slot));
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.WAND.get(),
                SoundSource.PLAYERS, 0.25F, 1.0F);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            step(level);
        }
    }

    // original: one swap a tick in each world; entries that no longer fit are dropped
    public static void step(ServerLevel level) {
        Deque<Swap> waiting = WAITING.get(level);
        if (waiting == null) {
            return;
        }
        Swap next;
        while ((next = waiting.poll()) != null) {
            if (swap(level, next, waiting)) {
                return;
            }
        }
    }

    private static boolean swap(ServerLevel level, Swap swap, Deque<Swap> waiting) {
        Player caster = swap.caster();
        BlockPos at = swap.at();
        ItemStack wand = caster.getInventory().getItem(swap.slot());
        var focus = wand.getItem() instanceof WandItem ? Casting.fitted(wand) : null;
        VisHolder holder = VisHolder.of(wand);
        BlockState there = level.getBlockState(at);
        if (caster.isRemoved() || caster.level() != level || focus == null || holder == null
                || there != swap.was() || level.getBlockEntity(at) != null
                || there.getDestroySpeed(level, at) < 0.0F || !caster.mayInteract(level, at)
                || !holder.holds(wand, focus.cost(), caster, VisHolder.Spending.WORKING)
                || !takeOne(caster, swap.laying())) {
            return false;
        }
        if (!caster.isCreative()) {
            for (ItemStack drop : Block.getDrops(there, level, at, null, caster, tool(level, wand))) {
                if (!caster.getInventory().add(drop)) {
                    Block.popResource(level, at, drop);
                }
            }
            holder.take(wand, focus.cost(), caster, VisHolder.Spending.WORKING);
        }
        BlockState now = swap.laying().defaultBlockState();
        level.setBlockAndUpdate(at, now);
        swap.laying().setPlacedBy(level, at, now, caster, new ItemStack(swap.laying()));
        level.playSound(null, at, ModSounds.POOF.get(), SoundSource.BLOCKS, 0.4F,
                1.0F + (float) level.random.nextGaussian() * 0.05F);

        if (swap.spreads() > 0) {
            for (BlockPos next : BlockPos.betweenClosed(at.offset(-1, -1, -1), at.offset(1, 1, 1))) {
                if (!next.equals(at) && level.getBlockState(next) == swap.was() && exposed(level, next)) {
                    waiting.add(new Swap(next.immutable(), swap.was(), swap.laying(), swap.spreads() - 1, caster,
                            swap.slot()));
                }
            }
        }
        return true;
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

    public static Block remembered(ItemStack wand) {
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
        return LAYS_AT_MOST + Casting.worked(wand, FocusUpgrade.ENLARGE) * LAYS_FURTHER;
    }

    private static int spreads(ItemStack wand) {
        return SPREADS + Casting.worked(wand, FocusUpgrade.ENLARGE) * SPREADS_FURTHER;
    }

    private static int widest(ItemStack wand) {
        return AREA + Casting.worked(wand, FocusUpgrade.ENLARGE) * AREA_FURTHER;
    }

    public static ArchitectArea chosen(ItemStack wand) {
        int most = widest(wand);
        ArchitectArea set = WandItem.focus(wand).get(ModDataComponents.FOCUS_AREA.get());
        return set == null ? new ArchitectArea(most, most, 0)
                : new ArchitectArea(Math.min(most, set.first()), Math.min(most, set.second()), set.changing());
    }

    public static boolean architect(ItemStack wand) {
        return WandItem.focus(wand).is(ModItems.FOCI.get("trade").get()) && Casting.has(wand, FocusUpgrade.ARCHITECT);
    }

    // original: the key widens the chosen sides by one and wraps to nothing past the most; sneaking picks the sides
    public static void setArea(Player caster, ItemStack wand) {
        if (!architect(wand)) {
            return;
        }
        ArchitectArea was = chosen(wand);
        int most = widest(wand);
        ArchitectArea now;
        if (caster.isShiftKeyDown()) {
            now = new ArchitectArea(was.first(), was.second(), (was.changing() + 1) % 3);
        } else {
            int first = was.changing() == 2 ? was.first() : was.first() + 1;
            int second = was.changing() == 1 ? was.second() : was.second() + 1;
            now = new ArchitectArea(first > most ? 0 : first, second > most ? 0 : second, was.changing());
        }
        ItemStack focus = WandItem.focus(wand).copy();
        focus.set(ModDataComponents.FOCUS_AREA.get(), now);
        WandItem.setFocus(wand, focus);
        caster.displayClientMessage(caster.isShiftKeyDown()
                ? Component.translatable("item.alchemia.focus.changing." + now.changing())
                : Component.translatable("item.alchemia.focus.area", now.first() * 2 + 1, now.second() * 2 + 1), true);
    }

    // original: same blocks joined across the struck face, out to the chosen half-widths
    public static List<BlockPos> area(Level level, Player caster, ItemStack wand, BlockPos centre, Direction face) {
        BlockState kind = level.getBlockState(centre);
        ArchitectArea chosen = chosen(wand);
        Direction.Axis axis = face.getAxis();
        // original quirk: on a north or south face the two widths swap
        int one = axis == Direction.Axis.Z ? chosen.second() : chosen.first();
        int other = axis == Direction.Axis.Z ? chosen.first() : chosen.second();
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> open = new ArrayDeque<>();
        open.push(centre.immutable());
        while (!open.isEmpty()) {
            BlockPos at = open.pop();
            if (!seen.add(at) || !within(centre, at, axis, one, other)) {
                continue;
            }
            BlockState there = level.getBlockState(at);
            if (there != kind || there.isAir() || !exposed(level, at) || there.getDestroySpeed(level, at) < 0.0F
                    || !caster.mayInteract(level, at)) {
                continue;
            }
            found.add(at);
            for (Direction step : Direction.values()) {
                if (step.getAxis() != axis) {
                    open.push(at.relative(step));
                }
            }
        }
        return found;
    }

    private static boolean within(BlockPos centre, BlockPos at, Direction.Axis axis, int one, int other) {
        int x = Math.abs(at.getX() - centre.getX());
        int y = Math.abs(at.getY() - centre.getY());
        int z = Math.abs(at.getZ() - centre.getZ());
        return switch (axis) {
            case Y -> x <= one && z <= other;
            case Z -> x <= one && y <= other;
            case X -> y <= one && z <= other;
        };
    }

    private static boolean exposed(Level level, BlockPos at) {
        for (Direction side : Direction.values()) {
            if (!level.getBlockState(at.relative(side)).canOcclude()) {
                return true;
            }
        }
        return false;
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
