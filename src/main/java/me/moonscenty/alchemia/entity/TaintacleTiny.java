package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * A taint tendril: a little taintacle a taintacle sends up under something standing on taint. It lashes as its
 * parent does, close in, and withers away of itself after ten seconds. It leaves nothing.
 */
public class TaintacleTiny extends Taintacle {
    /** The original's numbers. */
    private static final double LIVES = 5.0;
    private static final double LASHES = 2.0;
    private static final int LASTS = 200;

    private int left = LASTS;

    public TaintacleTiny(EntityType<? extends TaintacleTiny> type, Level level) {
        super(type, level);
        xpReward = 0;
    }

    public TaintacleTiny(Level level) {
        this(ModEntities.TAINTACLE_TINY.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Taintacle.attributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.ATTACK_DAMAGE, LASHES);
    }

    /** Close in only. */
    @Override
    protected double reach() {
        return 0.0;
    }

    @Override
    protected boolean sendsTendrils() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (left-- <= 0) {
            hurt(damageSources().magic(), 10.0F);
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }
}
