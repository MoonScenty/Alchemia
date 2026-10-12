package me.moonscenty.alchemia.block.entity;

import java.util.Comparator;
import java.util.List;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A brain in a jar: what is left of a zombie's hunger, fed on the thoughts of whatever dies near it.
 * <p>
 * It draws in the nearest experience orb within six blocks and eats every one that touches the jar, up to two
 * thousand points. Shaken, it gives some back -- anything up to sixty-three, at random -- and then waits two
 * seconds before it eats again, so that what it gave is not eaten straight back. It keeps what it holds when it is
 * broken and put down again.
 */
public class BrainJarBlockEntity extends BlockEntity {
    public static final int HOLDS = 2000;
    private static final double REACHES = 6.0;
    private static final int GIVES_UP_TO = 64;
    private static final int WAITS = 40;

    private int xp;
    private int waiting;

    /** Where the brain looks and how it turns toward it, client side only. */
    private float turned;
    private float turnedBefore;
    private float looking;

    public BrainJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRAIN_JAR.get(), pos, state);
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Mth.clamp(xp, 0, HOLDS);
        changed();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity jar) {
        Vec3 middle = pos.getCenter();
        ExperienceOrb nearest = null;
        if (jar.xp < HOLDS) {
            List<ExperienceOrb> near = level.getEntitiesOfClass(ExperienceOrb.class,
                    new AABB(pos).inflate(REACHES));
            nearest = near.stream().min(Comparator.comparingDouble(orb -> orb.distanceToSqr(middle))).orElse(null);
            if (nearest != null && jar.waiting == 0) {
                // drawn in as the game draws an orb to a player, harder the closer it comes
                Vec3 toward = middle.subtract(nearest.position()).scale(1.0 / 7.0);
                double distance = toward.length();
                double pull = 1.0 - distance;
                if (pull > 0.0 && distance > 0.0) {
                    pull *= pull;
                    nearest.setDeltaMovement(nearest.getDeltaMovement().add(toward.x / distance * pull * 0.15,
                            toward.y / distance * pull * 0.33, toward.z / distance * pull * 0.15));
                }
            }
        }
        if (level.isClientSide) {
            jar.turnedBefore = jar.turned;
            if (nearest != null) {
                jar.looking = (float) Mth.atan2(nearest.getZ() - middle.z, nearest.getX() - middle.x);
            } else {
                jar.looking += 0.01F;
            }
            jar.turned += Mth.wrapDegrees((jar.looking - jar.turned) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD * 0.04F;
        }
        if (jar.waiting > 0) {
            jar.waiting--;
        } else if (jar.xp < HOLDS && !level.isClientSide) {
            List<ExperienceOrb> touching = level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(0.1));
            if (!touching.isEmpty()) {
                for (ExperienceOrb orb : touching) {
                    jar.xp += orb.getValue();
                    level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), SoundEvents.GENERIC_EAT,
                            SoundSource.BLOCKS, 0.1F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
                    orb.discard();
                }
                jar.xp = Math.min(jar.xp, HOLDS);
                jar.changed();
            }
        }
    }

    /** Shaken: lets some of what it holds go, and waits before it eats again. */
    public void shake(Level level) {
        waiting = WAITS;
        if (!(level instanceof ServerLevel served)) {
            return;
        }
        int giving = level.random.nextInt(Math.min(xp + 1, GIVES_UP_TO));
        if (giving > 0) {
            xp -= giving;
            ExperienceOrb.award(served, worldPosition.getCenter(), giving);
            changed();
        }
    }

    public float turned(float partial) {
        return turnedBefore + Mth.wrapDegrees((turned - turnedBefore) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD * partial;
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- what it keeps when broken -------------------------------------------

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        xp = input.getOrDefault(ModDataComponents.STORED_XP.get(), 0);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (xp > 0) {
            components.set(ModDataComponents.STORED_XP.get(), xp);
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("XP");
    }

    // --- saving ------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        xp = tag.getInt("XP");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("XP", xp);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
