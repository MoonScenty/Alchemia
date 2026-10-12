package me.moonscenty.alchemia.golem.seal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.ClientSeals;
import me.moonscenty.alchemia.golem.task.Tasks;
import me.moonscenty.alchemia.network.SyncSeal;
import me.moonscenty.alchemia.network.SyncSeals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Every seal in a world, kept with the world. Seals tick where their block is loaded, fall off what they can no
 * longer stick to, and are told to every player in the world whenever one is put up, changed or taken down, so that
 * anyone holding a bell can see them.
 */
public final class Seals extends SavedData {
    private static final String NAME = Alchemia.MODID + "_seals";

    private final Map<SealPos, SealEntity> seals = new LinkedHashMap<>();
    private int ticks;

    private Seals() {
    }

    private static Seals of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Seals::new, Seals::load, null), NAME);
    }

    /** The seal here, on either side, or null. */
    @Nullable
    public static SealEntity get(Level level, @Nullable SealPos pos) {
        if (pos == null) {
            return null;
        }
        if (level instanceof ServerLevel server) {
            return of(server).seals.get(pos);
        }
        return ClientSeals.get(pos);
    }

    /** Every seal in a world, on the server. */
    public static Collection<SealEntity> all(ServerLevel level) {
        return List.copyOf(of(level).seals.values());
    }

    /** Sticks a new seal here for a player, unless there is one already. */
    public static boolean add(ServerLevel level, BlockPos pos, Direction face, Seal seal, @Nullable Player player) {
        Seals all = of(level);
        SealPos at = new SealPos(pos, face);
        if (all.seals.containsKey(at)) {
            return false;
        }
        SealEntity entity = new SealEntity(at, seal);
        entity.setOwner(player == null ? null : player.getUUID());
        all.seals.put(at, entity);
        changed(level, entity);
        return true;
    }

    /**
     * Takes a seal down, calling off its tasks. Unless quietly, it drops as its item, a little out from the face it
     * was on.
     */
    public static void remove(ServerLevel level, SealPos pos, boolean quietly) {
        Seals all = of(level);
        SealEntity entity = all.seals.remove(pos);
        if (entity == null) {
            return;
        }
        entity.seal().onRemoval(level, pos.pos(), pos.face());
        if (!quietly) {
            Direction face = pos.face();
            level.addFreshEntity(new ItemEntity(level, pos.pos().getX() + 0.5 + face.getStepX() / 1.7F,
                    pos.pos().getY() + 0.5 + face.getStepY() / 1.7F, pos.pos().getZ() + 0.5 + face.getStepZ() / 1.7F,
                    SealTypes.item(entity.seal().key())));
        }
        Tasks.suspendFor(level, pos);
        all.setDirty();
        PacketDistributor.sendToPlayersInDimension(level, new SyncSeal(pos, Optional.empty()));
    }

    /** Something about a seal has changed: it is saved, and every player in the world is told. */
    public static void changed(ServerLevel level, SealEntity entity) {
        of(level).setDirty();
        PacketDistributor.sendToPlayersInDimension(level,
                new SyncSeal(entity.pos(), Optional.of(entity.save(level.registryAccess()))));
    }

    /** Tells one player of every seal in the world they are in. */
    public static void tell(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<CompoundTag> every = new ArrayList<>();
        for (SealEntity entity : of(level).seals.values()) {
            every.add(entity.save(level.registryAccess()));
        }
        PacketDistributor.sendToPlayer(player, new SyncSeals(every));
    }

    /**
     * Every tick: each seal in a loaded place works, and once a second each is checked it can still stick where it
     * is. One that cannot, or that fails, falls off.
     */
    public static void tick(ServerLevel level) {
        Seals all = of(level);
        all.ticks++;
        for (SealEntity entity : List.copyOf(all.seals.values())) {
            if (!level.isLoaded(entity.pos().pos())) {
                continue;
            }
            try {
                if (all.ticks % 20 == 0
                        && !entity.seal().canPlaceAt(level, entity.pos().pos(), entity.pos().face())) {
                    remove(level, entity.pos(), false);
                    continue;
                }
                entity.tick(level);
            } catch (RuntimeException broken) {
                Alchemia.LOGGER.warn("Removing a seal that failed at {}", entity.pos().pos(), broken);
                remove(level, entity.pos(), false);
            }
        }
    }

    // --- saving ---------------------------------------------------------------------------------------------

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (SealEntity entity : seals.values()) {
            list.add(entity.save(registries));
        }
        tag.put("seals", list);
        return tag;
    }

    private static Seals load(CompoundTag tag, HolderLookup.Provider registries) {
        Seals all = new Seals();
        for (Tag each : tag.getList("seals", Tag.TAG_COMPOUND)) {
            SealEntity entity = SealEntity.load((CompoundTag) each, registries);
            if (entity != null) {
                all.seals.put(entity.pos(), entity);
            }
        }
        return all;
    }
}
