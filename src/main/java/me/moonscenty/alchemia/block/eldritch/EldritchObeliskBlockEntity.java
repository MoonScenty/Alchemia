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
            // 40 ticks of regeneration never heals (original's quirk); it only marks who was already buffed
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
