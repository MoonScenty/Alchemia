package me.moonscenty.alchemia.enchantment;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
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

/**
 * What the two workings an altar can put on a tool actually do.
 * <p>
 * Both hang off the moment a block's drops are settled, which is the one place where what was broken, who broke
 * it and what they were holding are all still to hand.
 * <p>
 * Both are off while the one holding the tool is crouching. That is the original's rule and it is the only
 * control either of them has: a tool that always takes nine blocks is a tool that cannot take one.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class InfusionEnchantmentEvents {
    /** Which face of a block a player last struck, so that the eight round it can be worked out. */
    private static final java.util.Map<java.util.UUID, Direction> STRUCK = new java.util.HashMap<>();
    /** Stops the eight blocks a destructive tool takes from each taking eight of their own. */
    private static boolean spreading;
    /** The same, for a tool that follows a seam: the block it reaches for must not reach again. */
    private static boolean burrowing;
    /** And again for a blow that carries: the blows it carries must not each carry blows of their own. */
    private static boolean arcing;

    private InfusionEnchantmentEvents() {
    }

    /**
     * Which face was struck, remembered from the click.
     * <p>
     * By the time the drops are settled the hit is over and nothing in that event says which way the player was
     * facing the block. The original kept the same note for the same reason.
     */
    @SubscribeEvent
    public static void onStruck(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            STRUCK.put(event.getEntity().getUUID(), event.getFace());
        }
    }

    @SubscribeEvent
    public static void onBroken(BlockDropsEvent event) {
        // the sneak key rather than the crouching pose: the original asked the same question, and a player
        // holding shift in a two-block gap is still asking for one block
        if (!(event.getBreaker() instanceof Player player) || player.isShiftKeyDown()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (InfusionEnchantments.has(held, InfusionEnchantment.DESTRUCTIVE)) {
            spread(event.getLevel(), event.getPos(), player, held);
        }
        if (InfusionEnchantments.has(held, InfusionEnchantment.COLLECTOR)) {
            collect(event, player);
        }
    }

    /**
     * What was broken goes to the one who broke it.
     * <p>
     * The original made the drops fly after the player and be picked up on the way. This puts them in hand
     * directly, which comes to the same thing a second sooner; what will not fit falls where it was, because a
     * full bag is not a reason to lose the stone.
     */
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

    /**
     * The eight blocks round the one struck go with it.
     * <p>
     * Eight on the face that was struck, so a wall comes down as a wall and a floor as a floor, and only where
     * the tool would have served for them anyway -- a pickaxe does not fell the log beside the stone.
     */
    private static void spread(net.minecraft.world.level.Level level, BlockPos struck, Player player, ItemStack held) {
        if (spreading || !(level instanceof ServerLevel server)) {
            return;
        }
        Direction face = STRUCK.getOrDefault(player.getUUID(), Direction.UP);
        spreading = true;
        try {
            for (BlockPos beside : around(struck, face)) {
                BlockState there = server.getBlockState(beside);
                // unbreakable stone is unbreakable, and a pickaxe does not fell the log standing in it
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

    // --- burrowing ---------------------------------------------------------

    /** How far a vein is followed, and how far a trunk: a tree is worth reaching further into than a seam. */
    private static final int SEAM_REACH = 1;
    private static final int TRUNK_REACH = 2;
    /** No flood fill runs past this many blocks, however big the thing turns out to be. */
    private static final int FOLLOWS_AT_MOST = 128;

    /**
     * A vein or a trunk comes apart from its far end.
     * <p>
     * Strike the bottom of a trunk and the top log falls; strike it again and the next one down falls. What is
     * being paid for is not speed -- it is the same number of swings -- but that a tree comes down on its own
     * rather than being climbed, and a seam is followed without the stone round it being dug out.
     */
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

    /**
     * The block of the same kind that is joined to this one and stands furthest from it.
     * <p>
     * Joined rather than merely nearby, so a second seam behind a wall of stone is left where it is. The reach
     * is how far each step may wander from the block struck, not how many steps there may be -- a trunk is
     * followed up, a seam only into its own corner.
     */
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

    // --- sounding ----------------------------------------------------------

    /** How far the tapping carries, per step it has been taken to, and what one tap wears off the tool. */
    private static final int HEARS_PER_RANK = 4;
    private static final int TAP_COSTS = 5;

    /**
     * Tapping stone with it shows what is behind the stone.
     * <p>
     * For a moment only, and it wears the tool five times what a swing does, so it is a thing to do at a
     * junction rather than every step of a tunnel. What it shows is drawn where the ore is, not on the face of
     * the wall: a player should come away knowing which way to dig, not merely that there is something.
     */
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
            // shown to the one who tapped and to nobody else: it is their tool that heard it
            level.sendParticles(listener, net.minecraft.core.particles.ParticleTypes.END_ROD, true,
                    found.getX() + 0.5, found.getY() + 0.5, found.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.0);
        }
        held.hurtAndBreak(TAP_COSTS, event.getEntity(), EquipmentSlot.MAINHAND);
        level.playSound(null, tapped, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    // --- arcing ------------------------------------------------------------

    /** How far a blow reaches past what it landed on, per step, and how many others it may reach. */
    private static final double ARCS_PER_RANK = 1.0;
    private static final double ARCS_AT_ALL = 1.5;

    /**
     * A blow lands on what is standing about as well as on what was struck.
     * <p>
     * As many others as the working has been taken steps, and no more, so a crowd is thinned rather than felled.
     * Nothing tame is struck and nobody else is, which is the difference between a weapon and a hazard.
     */
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
    }

    // --- essence -----------------------------------------------------------

    /** How often a working of this takes hold at all, out of five, and how many points it takes when it does. */
    private static final int ESSENCE_IN_FIVE = 5;

    /**
     * What is killed gives up a little of what it was made of.
     * <p>
     * Not all of it and not every time: the chance of getting anything at all rises with how far the working has
     * been taken, and what comes out is drawn from what the creature reads as. A wolf gives beast and flesh; it
     * does not give whatever the holder wanted.
     * <p>
     * This is the one way of getting essentia that does not want a smelter, which is why it is the dearest of
     * these to have put on and gives so little at a time.
     */
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
        AspectList made = Aspects.of(event.getEntity().getType());
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

    /** How far apart two spots are, counting a diagonal step as one step. */
    private static int stepsAway(BlockPos one, BlockPos other) {
        return Math.max(Math.abs(one.getX() - other.getX()),
                Math.max(Math.abs(one.getY() - other.getY()), Math.abs(one.getZ() - other.getZ())));
    }

    /** The eight squares round one, on the plane of the face that was struck. */
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
