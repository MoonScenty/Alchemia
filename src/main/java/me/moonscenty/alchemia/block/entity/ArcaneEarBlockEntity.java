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

@EventBusSubscriber(modid = Alchemia.MODID)
public class ArcaneEarBlockEntity extends SyncedBlockEntity {
    // original's values
    private static final double HEARS_SQUARED = 64 * 64;
    private static final int SIGNALS_FOR = 10;
    private static final int NOTES = 25;

    private record Heard(BlockPos at, NoteBlockInstrument voice, int note) {
    }

    // note blocks play before ears tick, so ears hear notes the same tick; cleared at server tick end
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

    public NoteBlockInstrument voice() {
        Direction facing = getBlockState().getValue(ArcaneEarBlock.FACING);
        return level.getBlockState(worldPosition.relative(facing.getOpposite())).instrument();
    }

    private void signal(boolean on) {
        BlockState state = getBlockState();
        if (state.getValue(ArcaneEarBlock.ENABLED) != on) {
            level.setBlock(worldPosition, state.setValue(ArcaneEarBlock.ENABLED, on), Block.UPDATE_ALL);
            level.updateNeighborsAt(worldPosition.relative(state.getValue(ArcaneEarBlock.FACING).getOpposite()),
                    state.getBlock());
        }
    }

    public void raisePitch() {
        note = (note + 1) % NOTES;
        // goggles show the note it is tuned to, so whoever is watching has to know
        changed();
    }

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
}
