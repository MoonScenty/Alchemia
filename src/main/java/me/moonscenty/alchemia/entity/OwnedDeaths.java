package me.moonscenty.alchemia.entity;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.GameRules;

/**
 * A construct with a name tells its owner how it died, the original's {@code EntityOwnedConstruct.onDeath}: only when
 * death messages are shown at all, and only to an owner who is about.
 */
public final class OwnedDeaths {
    private OwnedDeaths() {
    }

    public static <T extends LivingEntity & OwnableEntity> void tell(T construct) {
        ServerPlayer owner = toTell(construct);
        if (owner != null) {
            owner.sendSystemMessage(construct.getCombatTracker().getDeathMessage());
        }
    }

    /** Who hears of it, if anybody does. */
    @Nullable
    public static <T extends LivingEntity & OwnableEntity> ServerPlayer toTell(T construct) {
        return tells(construct) && construct.getOwner() instanceof ServerPlayer owner ? owner : null;
    }

    /** Whether its death is worth telling of at all: it has a name, and the world shows deaths. */
    public static boolean tells(LivingEntity construct) {
        return !construct.level().isClientSide && construct.hasCustomName()
                && construct.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
    }
}
