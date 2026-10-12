package me.moonscenty.alchemia.item;

import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.golem.seal.SealDisplayer;
import me.moonscenty.alchemia.golem.seal.SealEntity;
import me.moonscenty.alchemia.golem.seal.SealPos;
import me.moonscenty.alchemia.golem.seal.Seals;
import me.moonscenty.alchemia.menu.SealMenu;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The golemancer's bell. Rung at one of your golems, once you have learned how to direct them, it tells the golem
 * to follow you or to stay where it is; the golem hears it and does the rest.
 *
 * <p>Rung at a seal, on the block or from up to five blocks off, it opens the seal's panel; rung at one crouching, it
 * takes the seal down. Held, it shows where the seals are.
 */
public class GolemBellItem extends Item implements SealDisplayer {
    private static final double REACH = 5.0;

    public GolemBellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        player.swing(context.getHand());
        SealPos at = new SealPos(context.getClickedPos(), context.getClickedFace());
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return Seals.get(context.getLevel(), at) != null ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        SealEntity seal = Seals.get(level, at);
        if (seal == null) {
            return InteractionResult.PASS;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.6F, 1.0F + level.random.nextFloat() * 0.1F);
        handle(level, player, seal);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.swing(hand);
        if (level instanceof ServerLevel server) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.6F, 1.0F + level.random.nextFloat() * 0.1F);
            SealEntity seal = lookedAt(server, player);
            if (seal != null) {
                handle(server, player, seal);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    /** Crouching, takes the seal down; otherwise opens it. */
    private static void handle(ServerLevel level, Player player, SealEntity seal) {
        if (player.isShiftKeyDown()) {
            Seals.remove(level, seal.pos(), false);
            level.playSound(null, seal.pos().pos(), ModSounds.ZAP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            player.openMenu(new SimpleMenuProvider((id, inventory, who) -> new SealMenu(id, inventory, seal, level),
                    Component.translatable("item.alchemia.seal_" + seal.seal().key())),
                    buffer -> SealPos.STREAM_CODEC.encode(buffer, seal.pos()));
        }
    }

    /**
     * The first seal along where the player is looking, within five blocks: each block the look passes through is
     * tried on whichever face the look meets it by, as the original did.
     */
    @Nullable
    public static SealEntity lookedAt(Level level, Player player) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getLookAngle().scale(REACH));
        Vec3 step = to.subtract(from).scale(0.1 / REACH);
        Vec3 at = from;
        BlockPos last = null;
        for (int along = 0; along < REACH * 10; along++) {
            at = at.add(step);
            BlockPos pos = BlockPos.containing(at);
            if (pos.equals(last)) {
                continue;
            }
            last = pos;
            BlockHitResult hit = AABB.clip(List.of(new AABB(0, 0, 0, 1, 1, 1)), from, to, pos);
            if (hit != null) {
                SealEntity seal = Seals.get(level, new SealPos(pos, hit.getDirection()));
                if (seal != null) {
                    return seal;
                }
            }
        }
        return null;
    }
}
