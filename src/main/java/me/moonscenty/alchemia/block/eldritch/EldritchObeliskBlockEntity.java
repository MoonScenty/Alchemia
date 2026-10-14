package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.entity.EldritchMob;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * The foot of an obelisk: what makes it more than stone. Whatever from the Outer Lands stands within six blocks of it
 * is kept strong, and wisps of dark run down to it from the shaft.
 * <p>
 * The original meant to heal them as well. It gave them two seconds of regeneration, which is too short for the
 * first point of healing ever to come; so in fact it only ever made them strong, and here it does the same.
 */
public class EldritchObeliskBlockEntity extends BlockEntity {
    private static final double REACH = 6.0;

    public EldritchObeliskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELDRITCH_OBELISK.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EldritchObeliskBlockEntity obelisk) {
        AABB around = new AABB(pos).inflate(REACH);
        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, around,
                living -> living instanceof EldritchMob && living.isAlive()
                        && living.distanceToSqr(pos.getCenter()) < REACH * REACH)) {
            if (level.isClientSide) {
                me.moonscenty.alchemia.client.EldritchEffects.obeliskWisp(level, pos, near);
            } else if (!near.hasEffect(MobEffects.REGENERATION)) {
                near.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, true));
                near.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, true));
            }
        }
    }

    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0.0, 5.0, 0.0).inflate(0.3);
    }
}
