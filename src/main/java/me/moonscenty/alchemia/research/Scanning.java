package me.moonscenty.alchemia.research;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Turning an alchemometer on something and seeing what comes of it.
 */
public final class Scanning {
    /** How far an alchemometer reaches. */
    public static final double REACH = 9.0;

    private Scanning() {
    }

    /** Whatever the player is looking at, if it is something that can be read at all. */
    public static Optional<ScanTarget> lookingAt(Player player) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 aim = eye.add(player.getLookAngle().scale(REACH));

        // a creature in the way is read before whatever is behind it
        BlockHitResult block = level.clip(new ClipContext(eye, aim, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        double blockDistance = block.getType() == HitResult.Type.MISS ? REACH * REACH : eye.distanceToSqr(block.getLocation());

        EntityHitResult hit = findEntity(player, eye, aim, blockDistance);
        if (hit != null) {
            return Optional.of(ScanTarget.of(hit.getEntity()));
        }
        if (block.getType() == HitResult.Type.BLOCK) {
            return Optional.of(new ScanTarget.OfBlock(level, block.getBlockPos(), level.getBlockState(block.getBlockPos())));
        }
        return Optional.empty();
    }

    /** Whether there is anything left to learn from this, so the scanner can say so before the player bothers. */
    public static boolean hasAnythingToTeach(Player player, ScanTarget target) {
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        return target.aspects().sortedByName().stream().anyMatch(aspect -> !knowledge.knows(aspect));
    }

    /**
     * Reads the target and adds whatever was new to the player's knowledge.
     *
     * @return the aspects the player did not know before
     */
    public static AspectList scan(Player player, ScanTarget target) {
        PlayerKnowledge before = PlayerKnowledge.of(player);
        AspectList aspects = target.aspects();

        AspectList learned = AspectList.EMPTY;
        for (Holder<Aspect> aspect : aspects.sortedByName()) {
            if (!before.knows(aspect)) {
                learned = learned.add(aspect, aspects.get(aspect));
            }
        }

        if (!learned.isEmpty()) {
            player.setData(ModAttachments.KNOWLEDGE, before.withAspects(learned));
        }
        report(player, target, aspects, learned);
        return learned;
    }

    private static void report(Player player, ScanTarget target, AspectList aspects, AspectList learned) {
        if (aspects.isEmpty()) {
            say(player, Component.translatable("scan.alchemia.nothing", target.displayName()), ChatFormatting.DARK_PURPLE);
        } else if (learned.isEmpty()) {
            say(player, Component.translatable("scan.alchemia.already_known", target.displayName()), ChatFormatting.GRAY);
        } else {
            List<Holder<Aspect>> names = learned.sortedByName();
            Component list = names.stream()
                    .map(aspect -> aspect.value().displayName())
                    .reduce((a, b) -> Component.empty().append(a).append(", ").append(b))
                    .orElse(Component.empty());
            say(player, Component.translatable("scan.alchemia.learned", target.displayName(), list), ChatFormatting.AQUA);
            Alchemia.LOGGER.debug("{} learned {} from {}", player.getName().getString(), learned, target.displayName().getString());
        }
    }

    private static void say(Player player, Component message, ChatFormatting colour) {
        player.sendSystemMessage(message.copy().withStyle(colour));
    }

    private static EntityHitResult findEntity(Player player, Vec3 eye, Vec3 aim, double maxDistanceSqr) {
        AABB search = player.getBoundingBox().expandTowards(player.getLookAngle().scale(REACH)).inflate(1);
        EntityHitResult closest = null;
        double closestDistance = maxDistanceSqr;

        for (Entity candidate : player.level().getEntities(player, search, entity -> !entity.isSpectator() && entity.isPickable())) {
            Optional<Vec3> touch = candidate.getBoundingBox().inflate(0.3).clip(eye, aim);
            if (touch.isEmpty()) {
                continue;
            }
            double distance = eye.distanceToSqr(touch.get());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = new EntityHitResult(candidate, touch.get());
            }
        }
        return closest;
    }
}
