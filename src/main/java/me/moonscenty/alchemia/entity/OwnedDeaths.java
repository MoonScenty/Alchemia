package me.moonscenty.alchemia.entity;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.GameRules;

// A named construct tells its owner how it died, as the original did.
public final class OwnedDeaths {
    private OwnedDeaths() {
    }

    public static <T extends LivingEntity & OwnableEntity> void tell(T construct) {
        ServerPlayer owner = toTell(construct);
        if (owner != null) {
            owner.sendSystemMessage(construct.getCombatTracker().getDeathMessage());
        }
    }

    @Nullable
    public static <T extends LivingEntity & OwnableEntity> ServerPlayer toTell(T construct) {
        return tells(construct) && construct.getOwner() instanceof ServerPlayer owner ? owner : null;
    }

    public static boolean tells(LivingEntity construct) {
        return !construct.level().isClientSide && construct.hasCustomName()
                && construct.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
    }
}
