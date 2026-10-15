package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ArcaneEarBlock;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * An arcane ear listening: the note it is tuned to, and how long it has left to signal for having heard it.
 *
 * <p>Every note block played on the server this tick is written down, and every ear goes over what was written
 * when it ticks. Note blocks play before the ears tick, so an ear hears a note the tick it is played; the list is
 * thrown away at the end of the tick.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public class ArcaneEarBlockEntity extends BlockEntity {
    /** How far an ear hears, squared, and how long it signals for a note. The original's. */
    private static final double HEARS_SQUARED = 64 * 64;
    private static final int SIGNALS_FOR = 10;
    /** The notes a note block runs through: two octaves. */
    private static final int NOTES = 25;

    /** A note played this tick: where, in what voice, at what pitch. */
    private record Heard(BlockPos at, NoteBlockInstrument voice, int note) {
    }

    private static final Map<ResourceKey<Level>, List<Heard>> PLAYED = new HashMap<>();

    private int note;
    private int signalling;

    public ArcaneEarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_EAR.get(), pos, state);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onNotePlayed(NoteBlockEvent.Play event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PLAYED.computeIfAbsent(level.dimension(), key -> new ArrayList<>())
                    .add(new Heard(event.getPos().immutable(), event.getInstrument(), event.getVanillaNoteId()));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        PLAYED.clear();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArcaneEarBlockEntity ear) {
        if (ear.signalling > 0 && --ear.signalling == 0) {
            ear.signal(false);
        }
        List<Heard> played = PLAYED.get(level.dimension());
        if (played == null) {
            return;
        }
        NoteBlockInstrument voice = ear.voice();
        for (Heard heard : played) {
            if (heard.voice() == voice && heard.note() == ear.note
                    && heard.at().getCenter().distanceToSqr(pos.getCenter()) <= HEARS_SQUARED) {
                ear.play(false);
                ear.signalling = SIGNALS_FOR;
                ear.signal(true);
                return;
            }
        }
    }

    /** The voice it hears in: the one a note block would play in, standing on what the ear is fixed to. */
    public NoteBlockInstrument voice() {
        Direction facing = getBlockState().getValue(ArcaneEarBlock.FACING);
        return level.getBlockState(worldPosition.relative(facing.getOpposite())).instrument();
    }

    private void signal(boolean on) {
        BlockState state = getBlockState();
        if (state.getValue(ArcaneEarBlock.ENABLED) != on) {
            // what is round the ear hears of it from the block update; what is round its block, from here
            level.setBlock(worldPosition, state.setValue(ArcaneEarBlock.ENABLED, on), Block.UPDATE_ALL);
            level.updateNeighborsAt(worldPosition.relative(state.getValue(ArcaneEarBlock.FACING).getOpposite()),
                    state.getBlock());
        }
    }

    /** A semitone up, round again to the lowest after the highest. */
    public void raisePitch() {
        note = (note + 1) % NOTES;
        setChanged();
        // goggles show the note it is tuned to, so whoever is watching has to know
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Its note shown over it, and sounded, in its voice, if it is being tuned rather than answering. */
    public void play(boolean aloud) {
        if (!(level instanceof ServerLevel served)) {
            return;
        }
        served.sendParticles(ParticleTypes.NOTE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5, 0, note / 24.0, 0.0, 0.0, 1.0);
        NoteBlockInstrument voice = voice();
        if (aloud && !voice.hasCustomSound()) {
            served.playSound(null, worldPosition, voice.getSoundEvent().value(), SoundSource.RECORDS, 3.0F,
                    voice.isTunable() ? NoteBlock.getPitchFromNote(note) : 1.0F);
        }
    }

    /** The note of what is at a place, for goggles to show: a note block's or an ear's, or -1 if it has none. */
    public static int noteAt(BlockState state, BlockEntity entity) {
        if (state.getBlock() instanceof NoteBlock) {
            return state.getValue(NoteBlock.NOTE);
        }
        return entity instanceof ArcaneEarBlockEntity ear ? ear.note() : -1;
    }

    public int note() {
        return note;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        note = Math.clamp(tag.getByte("note"), 0, NOTES - 1);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("note", (byte) note);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
