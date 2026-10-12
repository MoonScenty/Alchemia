package me.moonscenty.alchemia.block.entity;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.CrystallizerBlock;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.item.CrystallizedEssenceItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * An essentia crystallizer: takes one point of essentia in by the face it was set against, and some while later
 * puts out a crystal of it on the other side, into whatever container is there or else onto the ground.
 * <p>
 * Any aspect will do, the six primals included. It runs on earth drawn out of the air around it, four points at a
 * time, a hundred steps of work for each four; a crystal takes forty. With no earth in the air it simply waits,
 * holding its point. A redstone signal stops it.
 */
public class CrystallizerBlockEntity extends BlockEntity implements EssentiaHolder {
    private static final int WORKS_EVERY = 5;
    /** How much work a crystal is, and how much a step of it does at most. */
    private static final int WORK = 200;
    private static final int STEP = 5;
    /** What it draws from the air when it runs dry, and what each point of that is worth. */
    private static final int DRAWS = 4;
    private static final int FUEL_A_POINT = 25;
    /** The block event that sets the client's steam going. */
    public static final int VENTS = 0;
    private static final int VENTS_FOR = 7;

    @Nullable
    private Holder<Aspect> aspect;
    private int fuel;
    private int progress;
    private int counter;

    /** What the client draws: the crystals' spin and their colour, eased toward the aspect's. */
    private float spin;
    private float spinning;
    private float red = 1.0F;
    private float green = 1.0F;
    private float blue = 1.0F;
    private int venting;

    public CrystallizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTALLIZER.get(), pos, state);
    }

    private Direction facing() {
        return getBlockState().getValue(CrystallizerBlock.FACING);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrystallizerBlockEntity crystallizer) {
        if (++crystallizer.counter % WORKS_EVERY == 0 && !level.hasNeighborSignal(pos)) {
            if (crystallizer.aspect == null) {
                crystallizer.drawFromBehind(level, pos);
                crystallizer.progress = 0;
            } else {
                if (crystallizer.fuel <= 0) {
                    crystallizer.fuel += AuraHandler.drainAvailable(level, pos, ModAspects.EARTH, DRAWS)
                            * FUEL_A_POINT;
                }
                if (crystallizer.fuel > 0) {
                    int step = Math.min(STEP, Math.min(Math.max(1, (WORK - crystallizer.progress) / STEP),
                            crystallizer.fuel));
                    crystallizer.fuel -= step;
                    crystallizer.progress += step * STEP;
                    crystallizer.setChanged();
                }
            }
        }
        if (crystallizer.aspect != null && crystallizer.progress >= WORK) {
            crystallizer.eject(level, pos);
            crystallizer.aspect = null;
            crystallizer.progress = 0;
            crystallizer.changed();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CrystallizerBlockEntity crystallizer) {
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        if (crystallizer.aspect != null) {
            int colour = crystallizer.aspect.value().color();
            r = ((colour >> 16) & 0xFF) / 220.0F;
            g = ((colour >> 8) & 0xFF) / 220.0F;
            b = (colour & 0xFF) / 220.0F;
        }
        crystallizer.red = ease(crystallizer.red, r);
        crystallizer.green = ease(crystallizer.green, g);
        crystallizer.blue = ease(crystallizer.blue, b);

        boolean powered = level.hasNeighborSignal(pos);
        crystallizer.spin += crystallizer.spinning;
        if (crystallizer.spin > 360.0F) {
            crystallizer.spin -= 360.0F;
        }
        if (crystallizer.aspect != null && !powered) {
            crystallizer.spinning = Math.min(20.0F, crystallizer.spinning + 0.1F);
        } else {
            crystallizer.spinning = Math.max(0.0F, crystallizer.spinning - 0.2F);
        }

        if (crystallizer.venting > 0) {
            crystallizer.venting--;
            Direction out = crystallizer.facing().getOpposite();
            var random = level.random;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                    pos.getX() + 0.5 + jitter(random) + out.getStepX() / 2.1,
                    pos.getY() + 0.5 + jitter(random) + out.getStepY() / 2.1,
                    pos.getZ() + 0.5 + jitter(random) + out.getStepZ() / 2.1,
                    out.getStepX() / 4.0 + jitter(random) / 4.0, out.getStepY() / 4.0 + jitter(random) / 4.0,
                    out.getStepZ() / 4.0 + jitter(random) / 4.0);
        }
    }

    private static float ease(float now, float toward) {
        return now < toward ? now + 0.05F : now > toward ? now - 0.05F : now;
    }

    private static double jitter(net.minecraft.util.RandomSource random) {
        return 0.1 - random.nextFloat() * 0.2;
    }

    /** Takes one point straight out of a vessel set against its back. A pipe there fills it on its own. */
    private void drawFromBehind(Level level, BlockPos pos) {
        Direction back = facing();
        BlockPos behind = pos.relative(back);
        if (level.getBlockState(behind).getBlock() instanceof TubeBlock
                || !(level.getBlockEntity(behind) instanceof EssentiaHolder source)
                || !source.reachableFrom(back.getOpposite()) || !source.givesTo(back.getOpposite())) {
            return;
        }
        for (Holder<Aspect> held : source.held().sortedByAmount()) {
            if (source.release(held)) {
                accept(held);
                return;
            }
        }
    }

    /** The crystal, into a container in front if there is one with room, else out onto the ground with a puff. */
    private void eject(Level level, BlockPos pos) {
        ItemStack crystal = CrystallizedEssenceItem.of(aspect);
        Direction out = facing().getOpposite();
        IItemHandler container = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(out), facing());
        if (container != null) {
            crystal = ItemHandlerHelper.insertItemStacked(container, crystal, false);
        }
        if (!crystal.isEmpty()) {
            ItemEntity dropped = new ItemEntity(level, pos.getX() + 0.5 + out.getStepX() * 0.65,
                    pos.getY() + 0.5 + out.getStepY() * 0.65, pos.getZ() + 0.5 + out.getStepZ() * 0.65, crystal);
            dropped.setDeltaMovement(out.getStepX() * 0.04, out.getStepY() * 0.04, out.getStepZ() * 0.04);
            level.blockEvent(pos, getBlockState().getBlock(), VENTS, 0);
            level.addFreshEntity(dropped);
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == VENTS) {
            venting = VENTS_FOR;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    // --- what the renderer reads ---------------------------------------------

    public float spin(float partial) {
        return spin + spinning * partial;
    }

    public int tint() {
        return 0xFF000000 | channel(red) << 16 | channel(green) << 8 | channel(blue);
    }

    private static int channel(float value) {
        return (int) (Math.max(0.0F, Math.min(1.0F, value)) * 255.0F);
    }

    // --- essentia ----------------------------------------------------------

    @Override
    public AspectList held() {
        return aspect == null ? AspectList.EMPTY : AspectList.of(aspect, 1);
    }

    @Override
    public boolean wants(Holder<Aspect> offered) {
        return aspect == null && level != null && !level.hasNeighborSignal(worldPosition);
    }

    @Override
    public boolean accept(Holder<Aspect> offered) {
        if (!wants(offered)) {
            return false;
        }
        aspect = offered;
        changed();
        return true;
    }

    /** Pipes meet it only at its back, the face it was set against. */
    @Override
    public boolean reachableFrom(Direction side) {
        return side == facing();
    }

    @Override
    public boolean givesTo(Direction side) {
        return false;
    }

    /** What a broken one lets go: the point it held goes into the air as flux. */
    public void spill() {
        if (level != null && !level.isClientSide && aspect != null) {
            AuraHandler.add(level, worldPosition, ModAspects.FLUX, 1);
        }
        aspect = null;
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- saving ------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        aspect = tag.contains("Aspect")
                ? ModAspects.REGISTRY.getHolder(ResourceLocation.parse(tag.getString("Aspect")))
                        .map(found -> (Holder<Aspect>) found).orElse(null)
                : null;
        fuel = tag.getInt("fuel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (aspect != null) {
            tag.putString("Aspect", aspect.value().id().toString());
        }
        tag.putInt("fuel", fuel);
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
