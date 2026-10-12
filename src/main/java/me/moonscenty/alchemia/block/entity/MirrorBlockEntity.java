package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.MirrorBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A mirror, and the other mirror it is paired with, which may be in another world.
 * <p>
 * A magic mirror takes in whole any stack of items that comes into its block and lets it out of its partner, a
 * single item a tick; hoppers and pipes can feed it too. An essentia mirror passes nothing on its own: it stands in
 * for whatever essentia is held within eight blocks in front of its partner, so that an altar or a lamp reaching
 * for essentia near one mirror draws on jars by the other.
 * <p>
 * Pairs are exclusive. An unpaired mirror tries now and then to pair again with the one it remembers, more seldom
 * the longer it fails. Moving things strains the aura: every hundred and twenty-eight items through a magic mirror,
 * or sixty-four points of essentia through an essentia one, lets out a point of flux.
 */
public class MirrorBlockEntity extends BlockEntity implements EssentiaHolder {
    private static final int RETRIES_FIRST = 40;
    private static final int RETRIES_AT_MOST = 600;
    private static final int RETRIES_LATER_BY = 20;
    private static final int ITEMS_A_FLUX = 128;
    private static final int ESSENTIA_A_FLUX = 64;
    private static final int SETTLES_EVERY = 100;
    /** How far in front of the partner an essentia mirror reaches, across and deep. */
    private static final int REACHES = 8;
    /** The block event the client's swirl is sent as. */
    public static final int SWIRL = 1;

    private boolean linked;
    @Nullable
    private GlobalPos link;
    private int instability;
    private final List<ItemStack> queue = new ArrayList<>();
    private int count;
    private int retryEvery = RETRIES_FIRST;

    public MirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIRROR.get(), pos, state);
    }

    public boolean essentia() {
        return getBlockState().getBlock() instanceof MirrorBlock mirror && mirror.essentia();
    }

    private Direction facing() {
        return getBlockState().getValue(MirrorBlock.FACING);
    }

    public boolean linked() {
        return linked;
    }

    @Nullable
    public GlobalPos link() {
        return link;
    }

    public GlobalPos here() {
        return GlobalPos.of(level.dimension(), worldPosition);
    }

    // --- the pair ------------------------------------------------------------

    /** The mirror at the other end, if its world is running and its chunk is loaded; nothing is loaded to look. */
    @Nullable
    public MirrorBlockEntity partner() {
        if (link == null || !(level instanceof ServerLevel served)) {
            return null;
        }
        ServerLevel there = served.getServer().getLevel(link.dimension());
        if (there == null || !there.isLoaded(link.pos())
                || !(there.getBlockEntity(link.pos()) instanceof MirrorBlockEntity mirror)
                || mirror.essentia() != essentia()) {
            return null;
        }
        return mirror;
    }

    /** Whether this and its partner point at each other. */
    public boolean paired() {
        MirrorBlockEntity partner = partner();
        return linked && partner != null && partner.linked && here().equals(partner.link);
    }

    /** Remembers where its partner is, as a mirror carried from that partner does when it is put down. */
    public void remember(GlobalPos partner) {
        link = partner;
        restore();
    }

    /** Pairs with the mirror it remembers, if that one is there and not already paired with another. */
    private void restore() {
        MirrorBlockEntity partner = partner();
        if (partner == null || partner.paired()) {
            if (linked && !paired()) {
                linked = false;
                changed();
            }
            return;
        }
        partner.linked = true;
        partner.link = here();
        partner.changed();
        linked = true;
        changed();
    }

    /** Tells the partner it is on its own, as a mirror taken down does. */
    public void unpair() {
        MirrorBlockEntity partner = partner();
        if (partner != null) {
            partner.linked = false;
            partner.changed();
        }
    }

    // --- ticking -----------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, MirrorBlockEntity mirror) {
        mirror.count++;
        if (!mirror.essentia()) {
            mirror.eject((ServerLevel) level);
        }
        int each = mirror.essentia() ? ESSENTIA_A_FLUX : ITEMS_A_FLUX;
        if (mirror.instability > each) {
            AuraHandler.add(level, pos, ModAspects.FLUX, 1);
            mirror.instability -= each;
            mirror.setChanged();
        }
        if (mirror.instability > 0 && mirror.count % SETTLES_EVERY == 0) {
            mirror.instability--;
        }
        if (mirror.count % mirror.retryEvery == 0) {
            if (!mirror.paired()) {
                if (mirror.retryEvery < RETRIES_AT_MOST) {
                    mirror.retryEvery += RETRIES_LATER_BY;
                }
                mirror.restore();
            } else {
                mirror.retryEvery = RETRIES_FIRST;
            }
        }
    }

    // --- items -------------------------------------------------------------

    /** A stack come into the block: sent whole to the partner, if the pair holds. */
    public boolean send(ItemEntity item) {
        if (essentia() || !paired()) {
            return false;
        }
        MirrorBlockEntity partner = partner();
        partner.receive(item.getItem().copy());
        instability += item.getItem().getCount();
        item.discard();
        setChanged();
        level.blockEvent(worldPosition, getBlockState().getBlock(), SWIRL, 0);
        return true;
    }

    /** A stack pushed in by a hopper or a pipe: on to the partner, or back out of this one if it has none. */
    public void insert(ItemStack stack) {
        MirrorBlockEntity partner = paired() ? partner() : null;
        if (partner == null) {
            spit((ServerLevel) level, stack);
            return;
        }
        partner.receive(stack.copy());
        instability += stack.getCount();
        setChanged();
        level.blockEvent(worldPosition, getBlockState().getBlock(), SWIRL, 0);
    }

    /** Something sent this way, whether from the partner or from a hand mirror, to be let out a piece at a time. */
    public void receive(ItemStack stack) {
        if (!stack.isEmpty()) {
            queue.add(stack);
            setChanged();
        }
    }

    /** One item out of what has been sent here, a tick, picked at random. */
    private void eject(ServerLevel level) {
        if (queue.isEmpty() || count <= 20) {
            return;
        }
        int pick = level.random.nextInt(queue.size());
        ItemStack from = queue.get(pick);
        spit(level, from.copyWithCount(1));
        from.shrink(1);
        if (from.isEmpty()) {
            queue.remove(pick);
        }
        instability++;
        setChanged();
        level.blockEvent(worldPosition, getBlockState().getBlock(), SWIRL, 0);
    }

    /** Out into the room, pushed away from the glass, and not to be taken straight back in. */
    private void spit(ServerLevel level, ItemStack stack) {
        Direction f = facing();
        ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5 - f.getStepX() * 0.3,
                worldPosition.getY() + 0.25 - f.getStepY() * 0.3, worldPosition.getZ() + 0.5 - f.getStepZ() * 0.3,
                stack);
        item.setDeltaMovement(f.getStepX() * 0.15, f.getStepY() * 0.15, f.getStepZ() * 0.15);
        item.setPortalCooldown(20);
        level.addFreshEntity(item);
    }

    public List<ItemStack> queued() {
        return queue;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != SWIRL || level == null) {
            return super.triggerEvent(id, param);
        }
        if (level.isClientSide) {
            Direction f = facing();
            for (int i = 0; i < 2; i++) {
                level.addParticle(ParticleTypes.WITCH,
                        worldPosition.getX() + 0.33 + level.random.nextFloat() * 0.33 - f.getStepX() / 2.0,
                        worldPosition.getY() + 0.33 + level.random.nextFloat() * 0.33 - f.getStepY() / 2.0,
                        worldPosition.getZ() + 0.33 + level.random.nextFloat() * 0.33 - f.getStepZ() / 2.0,
                        f.getStepX() * 0.05, f.getStepY() * 0.05, f.getStepZ() * 0.05);
            }
        }
        return true;
    }

    // --- essentia ----------------------------------------------------------

    /** What stands in front of the partner, nearest first, other essentia mirrors left out. */
    private List<EssentiaHolder> beyond() {
        MirrorBlockEntity partner = essentia() && paired() ? partner() : null;
        if (partner == null) {
            return List.of();
        }
        Level there = partner.getLevel();
        BlockPos origin = partner.getBlockPos();
        Direction out = partner.facing();
        List<BlockEntity> found = new ArrayList<>();
        for (int a = -REACHES; a <= REACHES; a++) {
            for (int b = -REACHES; b <= REACHES; b++) {
                for (int c = 0; c < REACHES; c++) {
                    if (a == 0 && b == 0 && c == 0) {
                        continue;
                    }
                    BlockPos at;
                    if (out.getAxis() == Direction.Axis.Y) {
                        at = origin.offset(a, c * out.getStepY(), b);
                    } else if (out.getAxis() == Direction.Axis.Z) {
                        at = origin.offset(a, b, c * out.getStepZ());
                    } else {
                        at = origin.offset(c * out.getStepX(), a, b);
                    }
                    if (there.isLoaded(at) && there.getBlockEntity(at) instanceof EssentiaHolder holder
                            && !(holder instanceof MirrorBlockEntity)) {
                        found.add((BlockEntity) holder);
                    }
                }
            }
        }
        found.sort(Comparator.comparingDouble(entity -> entity.getBlockPos().distSqr(origin)));
        return found.stream().map(entity -> (EssentiaHolder) entity).toList();
    }

    @Override
    public AspectList held() {
        if (!essentia()) {
            return AspectList.EMPTY;
        }
        AspectList all = AspectList.EMPTY;
        for (EssentiaHolder holder : beyond()) {
            all = all.add(holder.held());
        }
        return all;
    }

    @Override
    public boolean release(Holder<Aspect> aspect) {
        for (EssentiaHolder holder : beyond()) {
            if (holder.held().get(aspect) > 0 && holder.release(aspect)) {
                instability++;
                setChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean wants(Holder<Aspect> aspect) {
        return beyond().stream().anyMatch(holder -> holder.wants(aspect));
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        for (EssentiaHolder holder : beyond()) {
            if (holder.wants(aspect) && holder.accept(aspect)) {
                instability++;
                setChanged();
                return true;
            }
        }
        return false;
    }

    /** No pipe meets it: the original's could not be piped into. */
    @Override
    public boolean reachableFrom(Direction side) {
        return false;
    }

    // --- what it keeps when taken down ---------------------------------------

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        GlobalPos remembered = input.get(ModDataComponents.MIRROR_LINK.get());
        if (remembered != null) {
            link = remembered;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (linked && link != null) {
            components.set(ModDataComponents.MIRROR_LINK.get(), link);
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("link");
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
        linked = tag.getBoolean("linked");
        link = tag.contains("link")
                ? GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.get("link")).result().orElse(null) : null;
        instability = tag.getInt("instability");
        queue.clear();
        for (Tag entry : tag.getList("Items", Tag.TAG_COMPOUND)) {
            ItemStack.parse(registries, entry).ifPresent(queue::add);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("linked", linked);
        if (link != null) {
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, link).result().ifPresent(written -> tag.put("link", written));
        }
        tag.putInt("instability", instability);
        ListTag items = new ListTag();
        for (ItemStack stack : queue) {
            if (!stack.isEmpty()) {
                items.add(stack.save(registries));
            }
        }
        tag.put("Items", items);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("linked", linked);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
