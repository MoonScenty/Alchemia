package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A spike of ice, thrown one at a time and meant to be aimed.
 *
 * <p>Where an ember is a spray and worth little apiece, this is one shot and worth something: three damage, and
 * it shatters where it lands. The wait between two of them is what the focus costs more than the ice does.
 */
public class FrostShard extends Bolt {
    private static final float HURTS = 3.0F;
    /** It is a thrown stone of a thing and falls like one. */
    private static final double FALLS = 0.03;
    private static final int MELTS_AFTER = 100;

    public FrostShard(EntityType<? extends FrostShard> type, Level level) {
        super(type, level);
    }

    public FrostShard(Level level, LivingEntity thrower) {
        super(ModEntities.FROST_SHARD.get(), level, thrower);
    }

    @Override
    protected double getDefaultGravity() {
        return FALLS;
    }

    @Override
    protected ParticleOptions trail() {
        return ParticleTypes.SNOWFLAKE;
    }

    @Override
    protected int lives() {
        return MELTS_AFTER;
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.15);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5F, 1.4F);
        if (where instanceof EntityHitResult struck) {
            struck.getEntity().hurt(damageSources().indirectMagic(this, getOwner()), HURTS);
        }
    }
}
