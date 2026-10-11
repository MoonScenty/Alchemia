package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A spike of ice, thrown one at a time and meant to be aimed.
 *
 * <p>Where an ember is a spray and worth little apiece, this is one shot and worth something: three damage, and
 * it shatters where it lands. The wait between two of them is what the focus costs more than the ice does.
 */
public class FrostShard extends Bolt {
    private static final float HURTS = 3.0F;
    /** It is a thrown stone of a thing and falls like one. A splinter is lighter and hangs longer. */
    private static final double FALLS = 0.03;
    private static final double DRIFTS = 0.015;
    private static final int MELTS_AFTER = 100;
    /** How long a chill holds, in ticks, and how much of its speed a boulder keeps off a wall. */
    private static final int CHILLS_FOR = 200;
    private static final double KEEPS = 0.8;

    private float hurts = HURTS;
    /** Ranks of alchemist's frost, which is how hard whatever it touches is slowed. */
    private int chills;
    /** A splinter of a volley: light, and it shatters on the first thing it touches. */
    private boolean splinter;
    /** How many walls a boulder may come off before it gives up. */
    private int bounces;

    public FrostShard(EntityType<? extends FrostShard> type, Level level) {
        super(type, level);
    }

    public FrostShard(Level level, LivingEntity thrower) {
        super(ModEntities.FROST_SHARD.get(), level, thrower);
    }

    @Override
    protected double getDefaultGravity() {
        return splinter ? DRIFTS : FALLS;
    }

    /** What one shard is worth, and how cold it is. */
    public FrostShard biting(float hurts, int chills) {
        this.hurts = hurts;
        this.chills = chills;
        return this;
    }

    /** One of a volley: it does little and there are a great many of it. */
    public FrostShard asSplinter() {
        this.splinter = true;
        return this;
    }

    /** A boulder, which comes off walls rather than shattering on them. */
    public FrostShard bouncing(int walls) {
        this.bounces = walls;
        return this;
    }

    @Override
    protected ParticleOptions trail() {
        return ParticleTypes.SNOWFLAKE;
    }

    @Override
    protected int lives() {
        return MELTS_AFTER;
    }

    /**
     * Where it ends up.
     * <p>
     * Everything it touches is hit and chilled. What happens to the shard afterwards is the difference between
     * the three shapes: a splinter shatters on the first thing it meets, a spike shatters on anything, and a
     * boulder comes off a wall and carries on until it has run out of walls.
     */
    @Override
    public void lands(HitResult where) {
        if (!(level() instanceof ServerLevel served)) {
            return;
        }
        if (where instanceof EntityHitResult struck) {
            struck.getEntity().hurt(damageSources().indirectMagic(this, getOwner()), hurts);
            if (chills > 0 && struck.getEntity() instanceof LivingEntity chilled) {
                chilled.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CHILLS_FOR, chills - 1));
            }
        }
        if (splinter || bounces-- <= 0) {
            land(served, where);
            discard();
            return;
        }
        if (where instanceof BlockHitResult off) {
            comeOffTheWall(off);
        }
    }

    /** Off the wall it struck, keeping most of its speed and backed out of the block it was entering. */
    private void comeOffTheWall(BlockHitResult off) {
        Direction face = off.getDirection();
        Vec3 going = getDeltaMovement();
        setDeltaMovement(new Vec3(
                face.getStepX() != 0 ? -going.x : going.x,
                face.getStepY() != 0 ? -going.y : going.y,
                face.getStepZ() != 0 ? -going.z : going.z).scale(KEEPS));
        setPos(position().add(getDeltaMovement().normalize().scale(0.05)));
        playSound(SoundEvents.GLASS_STEP, 0.4F, 1.6F);
    }

    /** What is left of it: a shower of ice and the sound of it going. */
    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.15);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5F, 1.4F);
    }
}
