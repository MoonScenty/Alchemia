package me.moonscenty.alchemia.enchantment;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.particle.MarkOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public final class InfusionEnchantmentEvents {
    private static final java.util.Map<java.util.UUID, Direction> STRUCK = new java.util.HashMap<>();
    // re-entrancy guards, so the extra blocks and blows don't trigger the effect again
    private static boolean spreading;
    private static boolean burrowing;
    private static boolean arcing;

    private InfusionEnchantmentEvents() {
    }

    // the drops event doesn't say which face was hit, so remember it from the click
    @SubscribeEvent
    public static void onStruck(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            STRUCK.put(event.getEntity().getUUID(), event.getFace());
        }
    }

    @SubscribeEvent
    public static void onBroken(BlockDropsEvent event) {
        // the shift key rather than the crouching pose, so a player in a two-block gap still gets one block
        if (!(event.getBreaker() instanceof Player player) || player.isShiftKeyDown()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        // refine first so collected or dropped items are already clusters
        if (InfusionEnchantments.has(held, InfusionEnchantment.REFINING)
                && held.isCorrectToolForDrops(event.getState())) {
            refine(event, InfusionEnchantments.level(held, InfusionEnchantment.REFINING));
        }
        if (InfusionEnchantments.has(held, InfusionEnchantment.DESTRUCTIVE)) {
            spread(event.getLevel(), event.getPos(), player, held);
        }
        if (InfusionEnchantments.has(held, InfusionEnchantment.COLLECTOR)) {
            collect(event, player);
        }
    }

    // chance per level; original's value
    private static final float REFINES = 0.125F;

    private static void refine(BlockDropsEvent event, int level) {
        float chance = (1 + level) * REFINES;
        boolean any = false;
        for (ItemEntity dropped : event.getDrops()) {
            ItemStack was = dropped.getItem();
            ItemStack now = me.moonscenty.alchemia.item.Clusters.maybe(was, chance, event.getLevel().random);
            if (now != was) {
                dropped.setItem(now);
                any = true;
            }
        }
        if (any) {
            event.getLevel().playSound(null, event.getPos(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.2F, 0.7F + event.getLevel().random.nextFloat() * 0.2F);
        }
    }

    private static void collect(BlockDropsEvent event, Player player) {
        List<ItemEntity> left = new ArrayList<>();
        for (ItemEntity dropped : event.getDrops()) {
            ItemStack stack = dropped.getItem();
            if (!player.getInventory().add(stack) && !stack.isEmpty()) {
                left.add(dropped);
            }
        }
        event.getDrops().clear();
        event.getDrops().addAll(left);
    }

    private static void spread(net.minecraft.world.level.Level level, BlockPos struck, Player player, ItemStack held) {
        if (spreading || !(level instanceof ServerLevel server)) {
            return;
        }
        Direction face = STRUCK.getOrDefault(player.getUUID(), Direction.UP);
        spreading = true;
        try {
            for (BlockPos beside : around(struck, face)) {
                BlockState there = server.getBlockState(beside);
                if (there.isAir() || there.getDestroySpeed(server, beside) < 0.0F
                        || !held.isCorrectToolForDrops(there)) {
                    continue;
                }
                server.destroyBlock(beside, true, player);
                held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
        } finally {
            spreading = false;
        }
    }

    // how far each step may stray from the struck block; trunks reach further than seams
    private static final int SEAM_REACH = 1;
    private static final int TRUNK_REACH = 2;
    private static final int FOLLOWS_AT_MOST = 128;

    // breaks the furthest joined block of the same kind instead of the struck one
    @SubscribeEvent
    public static void onBurrowing(BlockEvent.BreakEvent event) {
        if (burrowing || event.getPlayer().isShiftKeyDown()) {
            return;
        }
        ItemStack held = event.getPlayer().getMainHandItem();
        BlockState struck = event.getState();
        if (!InfusionEnchantments.has(held, InfusionEnchantment.BURROWING)
                || !held.isCorrectToolForDrops(struck)
                || !(event.getLevel() instanceof net.minecraft.world.level.Level level)) {
            return;
        }
        boolean trunk = struck.is(BlockTags.LOGS);
        if (!trunk && !struck.is(Tags.Blocks.ORES)) {
            return;
        }

        BlockPos furthest = furthest(level, event.getPos(), struck.getBlock(),
                trunk ? TRUNK_REACH : SEAM_REACH);
        if (furthest.equals(event.getPos())) {
            return;
        }
        event.setCanceled(true);
        burrowing = true;
        try {
            level.destroyBlock(furthest, true, event.getPlayer());
            held.hurtAndBreak(1, event.getPlayer(), EquipmentSlot.MAINHAND);
        } finally {
            burrowing = false;
        }
    }

    // joined only; reach limits distance from the start, not the number of steps
    private static BlockPos furthest(net.minecraft.world.level.Level level, BlockPos from, Block kind, int reach) {
        java.util.Set<BlockPos> seen = new java.util.HashSet<>();
        java.util.Deque<BlockPos> todo = new java.util.ArrayDeque<>();
        seen.add(from);
        todo.add(from);
        BlockPos best = from;
        double furthest = 0.0;
        while (!todo.isEmpty() && seen.size() < FOLLOWS_AT_MOST) {
            BlockPos at = todo.removeFirst();
            for (int east = -1; east <= 1; east++) {
                for (int up = -1; up <= 1; up++) {
                    for (int south = -1; south <= 1; south++) {
                        BlockPos beside = at.offset(east, up, south);
                        if (seen.contains(beside) || !level.getBlockState(beside).is(kind)
                                || stepsAway(from, beside) > reach) {
                            continue;
                        }
                        seen.add(beside);
                        todo.add(beside);
                        double away = beside.distSqr(from);
                        if (away > furthest) {
                            furthest = away;
                            best = beside;
                        }
                    }
                }
            }
        }
        return best;
    }

    private static final int HEARS_PER_RANK = 4;
    private static final int TAP_COSTS = 5;

    @SubscribeEvent
    public static void onSounding(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || event.getEntity().isShiftKeyDown()) {
            return;
        }
        ItemStack held = event.getItemStack();
        int rank = InfusionEnchantments.level(held, InfusionEnchantment.SOUNDING);
        if (rank <= 0 || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer listener)) {
            return;
        }
        int hears = rank * HEARS_PER_RANK;
        BlockPos tapped = event.getPos();
        for (BlockPos found : BlockPos.betweenClosed(tapped.offset(-hears, -hears, -hears),
                tapped.offset(hears, hears, hears))) {
            if (!level.getBlockState(found).is(Tags.Blocks.ORES)) {
                continue;
            }
            // only the tapping player sees the marks; they spread one block per tick, as in the original
            int waits = Mth.floor(Math.sqrt(tapped.distSqr(found)));
            level.sendParticles(listener, new MarkOptions(waits), true,
                    found.getX() + 0.5, found.getY() + 0.5, found.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
        }
        held.hurtAndBreak(TAP_COSTS, event.getEntity(), EquipmentSlot.MAINHAND);
        level.playSound(null, tapped.getX() + 0.5, tapped.getY() + 0.5, tapped.getZ() + 0.5,
                me.moonscenty.alchemia.registry.ModSounds.WAND_FAIL.get(), net.minecraft.sounds.SoundSource.PLAYERS,
                0.2F, 0.2F + level.random.nextFloat() * 0.2F);
    }

    private static final double ARCS_PER_RANK = 1.0;
    private static final double ARCS_AT_ALL = 1.5;

    @SubscribeEvent
    public static void onArcing(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        int rank = InfusionEnchantments.level(held, InfusionEnchantment.ARCING);
        if (arcing || rank <= 0 || player.level().isClientSide || !event.getTarget().isAlive()) {
            return;
        }

        double out = ARCS_AT_ALL + rank * ARCS_PER_RANK;
        int struck = 0;
        arcing = true;
        try {
            for (net.minecraft.world.entity.LivingEntity beside : player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    event.getTarget().getBoundingBox().inflate(out, 1.0 + rank / 2.0, out))) {
                if (struck >= rank) {
                    break;
                }
                if (beside == player || beside == event.getTarget() || !beside.isAlive()
                        || beside instanceof Player
                        || beside instanceof net.minecraft.world.entity.OwnableEntity owned
                                && player.getUUID().equals(owned.getOwnerUUID())) {
                    continue;
                }
                player.attack(beside);
                struck++;
            }
        } finally {
            arcing = false;
        }
        if (struck > 0) {
            net.minecraft.world.entity.Entity target = event.getTarget();
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    me.moonscenty.alchemia.registry.ModSounds.SWING.get(), net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0F, 0.9F + player.level().random.nextFloat() * 0.2F);
        }
    }

    private static final int ESSENCE_IN_FIVE = 5;

    @SubscribeEvent
    public static void onEssence(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        int rank = InfusionEnchantments.level(held, InfusionEnchantment.ESSENCE);
        if (rank <= 0) {
            return;
        }
        AspectList made = Aspects.of(event.getEntity());
        if (made.isEmpty() || player.level().random.nextInt(ESSENCE_IN_FIVE) >= rank) {
            return;
        }

        var level = event.getEntity().level();
        var where = event.getEntity().position().add(0.0, event.getEntity().getEyeHeight(), 0.0);
        AspectList left = made;
        for (int taken = 0; taken < rank && !left.isEmpty(); taken++) {
            var aspect = left.sortedByAmount().get(player.level().random.nextInt(left.sortedByAmount().size()));
            left = left.reduce(aspect, 1);
            event.getDrops().add(new ItemEntity(level, where.x, where.y, where.z,
                    me.moonscenty.alchemia.item.CrystallizedEssenceItem.of(aspect)));
        }
    }

    // Chebyshev distance
    private static int stepsAway(BlockPos one, BlockPos other) {
        return Math.max(Math.abs(one.getX() - other.getX()),
                Math.max(Math.abs(one.getY() - other.getY()), Math.abs(one.getZ() - other.getZ())));
    }

    private static List<BlockPos> around(BlockPos struck, Direction face) {
        List<BlockPos> all = new ArrayList<>();
        for (int one = -1; one <= 1; one++) {
            for (int other = -1; other <= 1; other++) {
                if (one == 0 && other == 0) {
                    continue;
                }
                all.add(switch (face.getAxis()) {
                    case Y -> struck.offset(one, 0, other);
                    case Z -> struck.offset(one, other, 0);
                    case X -> struck.offset(0, other, one);
                });
            }
        }
        return all;
    }
}
