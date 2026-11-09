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
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(effective(held, event.getState()) || silky(event.getLevel(), held))) {
            return;
        }
        // refine first so collected or dropped items are already clusters
        if (InfusionEnchantments.has(held, InfusionEnchantment.REFINING)) {
            refine(event, InfusionEnchantments.level(held, InfusionEnchantment.REFINING));
        }
        // the shift key rather than the crouching pose, so a player in a two-block gap still gets one block
        if (player.isShiftKeyDown()) {
            return;
        }
        if (InfusionEnchantments.has(held, InfusionEnchantment.DESTRUCTIVE)) {
            spread(event.getLevel(), event.getPos(), player, held);
        }
        if (InfusionEnchantments.has(held, InfusionEnchantment.COLLECTOR)) {
            gather(event.getDrops(), player);
        }
    }

    private static boolean effective(ItemStack tool, BlockState state) {
        return tool.isCorrectToolForDrops(state) || tool.getDestroySpeed(state) > 1.0F;
    }

    private static boolean silky(net.minecraft.world.level.Level level, ItemStack tool) {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), tool) > 0;
    }

    private static boolean ore(BlockState state) {
        return state.is(Tags.Blocks.ORES) || state.is(me.moonscenty.alchemia.registry.ModTags.Blocks.CRYSTALS);
    }

    // the blocks an enchantment takes beyond the struck one: the tool's own fortune and silk apply, no event fires
    private static void harvest(ServerLevel level, Player player, BlockPos at, ItemStack tool) {
        BlockState state = level.getBlockState(at);
        var entity = level.getBlockEntity(at);
        level.levelEvent(2001, at, Block.getId(state));
        boolean drops = !player.getAbilities().instabuild && state.canHarvestBlock(level, at, player);
        level.removeBlock(at, false);
        if (drops) {
            state.getBlock().playerDestroy(level, player, at, state, entity, tool);
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

    private static void gather(java.util.Collection<ItemEntity> drops, Player player) {
        List<ItemEntity> left = new ArrayList<>();
        for (ItemEntity dropped : drops) {
            ItemStack stack = dropped.getItem();
            if (!player.getInventory().add(stack) && !stack.isEmpty()) {
                left.add(dropped);
            }
        }
        drops.clear();
        drops.addAll(left);
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
                if (there.isAir() || there.getDestroySpeed(server, beside) < 0.0F || !effective(held, there)) {
                    continue;
                }
                held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                harvest(server, player, beside, held);
            }
        } finally {
            spreading = false;
        }
    }

    // how far each step may look; trunks reach further than seams
    private static final int SEAM_REACH = 1;
    private static final int TRUNK_REACH = 2;
    // original's limits on the walk away from the struck block
    private static final int STRAYS_ACROSS = 24;
    private static final int STRAYS_UP = 48;

    // breaks the far end of what is joined to the struck block, whose own break is spent there; costs the tool nothing
    @SubscribeEvent
    public static void onBurrowing(BlockEvent.BreakEvent event) {
        if (event.getPlayer().isShiftKeyDown()) {
            return;
        }
        ItemStack held = event.getPlayer().getMainHandItem();
        BlockState struck = event.getState();
        if (!InfusionEnchantments.has(held, InfusionEnchantment.BURROWING)
                || !effective(held, struck)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        boolean trunk = struck.is(BlockTags.LOGS);
        if (!trunk && !ore(struck)) {
            return;
        }
        event.setCanceled(true);
        harvest(level, event.getPlayer(), furthest(level, event.getPos(), struck.getBlock(),
                trunk ? TRUNK_REACH : SEAM_REACH), held);
    }

    // walks to the first further block it finds, again and again
    private static BlockPos furthest(net.minecraft.world.level.Level level, BlockPos from, Block kind, int reach) {
        BlockPos at = from;
        double furthest = 0.0;
        search:
        while (true) {
            for (int east = -reach; east <= reach; east++) {
                for (int up = reach; up >= -reach; up--) {
                    for (int south = -reach; south <= reach; south++) {
                        BlockPos next = at.offset(east, up, south);
                        if (Math.abs(next.getX() - from.getX()) > STRAYS_ACROSS
                                || Math.abs(next.getY() - from.getY()) > STRAYS_UP
                                || Math.abs(next.getZ() - from.getZ()) > STRAYS_ACROSS) {
                            return at;
                        }
                        BlockState there = level.getBlockState(next);
                        double away = next.distSqr(from);
                        if (there.is(kind) && there.getDestroySpeed(level, next) >= 0.0F && away > furthest) {
                            furthest = away;
                            at = next;
                            continue search;
                        }
                    }
                }
            }
            return at;
        }
    }

    private static final int HEARS_AT_LEAST = 4;
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
        int hears = HEARS_AT_LEAST + rank * HEARS_PER_RANK;
        BlockPos tapped = event.getPos();
        for (BlockPos found : BlockPos.betweenClosed(tapped.offset(-hears, -hears, -hears),
                tapped.offset(hears, hears, hears))) {
            if (!ore(level.getBlockState(found))) {
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
    // original's values
    private static final float ARC_HURTS = 0.5F;
    private static final float ARC_SHOVES = 0.5F;

    // a separate lesser blow on each neighbour, so the swing itself is untouched
    @SubscribeEvent
    public static void onArcing(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        int rank = InfusionEnchantments.level(held, InfusionEnchantment.ARCING);
        if (rank <= 0 || !(player.level() instanceof ServerLevel level) || !event.getTarget().isAlive()) {
            return;
        }

        double out = ARCS_AT_ALL + rank * ARCS_PER_RANK;
        float hurts = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                * ARC_HURTS;
        float facing = player.getYRot() * Mth.DEG_TO_RAD;
        int struck = 0;
        for (net.minecraft.world.entity.Entity beside : level.getEntities(player,
                event.getTarget().getBoundingBox().inflate(out, 1.0 + rank / 2.0, out))) {
            if (struck >= rank) {
                break;
            }
            if (!(beside instanceof net.minecraft.world.entity.Mob) || beside == event.getTarget() || !beside.isAlive()
                    || beside instanceof net.minecraft.world.entity.OwnableEntity owned
                            && player.getUUID().equals(owned.getOwnerUUID())) {
                continue;
            }
            var blow = player.damageSources().playerAttack(player);
            if (beside.hurt(blow, hurts)) {
                net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffectsWithItemSource(level, beside,
                        blow, null);
                beside.push(-Mth.sin(facing) * ARC_SHOVES, 0.1, Mth.cos(facing) * ARC_SHOVES);
                struck++;
            }
        }
        if (struck > 0) {
            net.minecraft.world.entity.Entity target = event.getTarget();
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    me.moonscenty.alchemia.registry.ModSounds.SWING.get(), net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0F, 0.9F + level.random.nextFloat() * 0.2F);
        }
    }

    private static final int ESSENCE_IN_FIVE = 5;

    @SubscribeEvent
    public static void onMobDrops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        int rank = InfusionEnchantments.level(held, InfusionEnchantment.ESSENCE);
        if (rank > 0) {
            essence(event, rank);
        }
        if (InfusionEnchantments.has(held, InfusionEnchantment.COLLECTOR)) {
            gather(event.getDrops(), player);
        }
    }

    // each crystal also has a one-in-rank chance of cutting the run short
    private static void essence(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event, int rank) {
        var level = event.getEntity().level();
        var random = level.random;
        AspectList left = Aspects.of(event.getEntity());
        var where = event.getEntity().position().add(0.0, event.getEntity().getEyeHeight(), 0.0);
        int taken = random.nextInt(ESSENCE_IN_FIVE) < rank ? 0 : rank;
        while (taken < rank && !left.isEmpty()) {
            var aspect = left.sortedByAmount().get(random.nextInt(left.size()));
            left = left.reduce(aspect, 1);
            event.getDrops().add(new ItemEntity(level, where.x, where.y, where.z,
                    me.moonscenty.alchemia.item.CrystallizedEssenceItem.of(aspect)));
            taken++;
            if (random.nextInt(rank) == 0) {
                taken += 1 + random.nextInt(2);
            }
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
