package me.moonscenty.alchemia.enchantment;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

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
