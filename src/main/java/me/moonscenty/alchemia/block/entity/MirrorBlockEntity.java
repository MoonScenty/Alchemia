package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.MirrorBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.essentia.EssentiaReach;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// an essentia mirror holds nothing; it stands in for essentia held near its partner
public class MirrorBlockEntity extends SyncedBlockEntity implements EssentiaHolder {
    private static final int RETRIES_FIRST = 40;
    private static final int RETRIES_AT_MOST = 600;
    private static final int RETRIES_LATER_BY = 20;
    private static final int ITEMS_A_FLUX = 128;
    private static final int ESSENTIA_A_FLUX = 64;
    private static final int SETTLES_EVERY = 100;
    private static final int REACHES = 8;
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

    public boolean paired() {
        MirrorBlockEntity partner = partner();
        return linked && partner != null && partner.linked && here().equals(partner.link);
    }

    public void remember(GlobalPos partner) {
        link = partner;
        restore();
    }

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

    public void unpair() {
        MirrorBlockEntity partner = partner();
        if (partner != null) {
            partner.linked = false;
            partner.changed();
        }
    }

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

    public void receive(ItemStack stack) {
        if (!stack.isEmpty()) {
            queue.add(stack);
            setChanged();
        }
    }

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

    private List<EssentiaHolder> beyond() {
        MirrorBlockEntity partner = essentia() && paired() ? partner() : null;
        if (partner == null) {
            return List.of();
        }
        BlockPos origin = partner.getBlockPos();
        Direction out = partner.facing();
        List<EssentiaHolder> found = new ArrayList<>();
        for (BlockEntity near : EssentiaReach.holders(partner.getLevel(), origin, REACHES * 2)) {
            if (near instanceof JarBlockEntity
                    && TransfuserBlockEntity.inFront(origin, out, near.getBlockPos(), REACHES)) {
                found.add((EssentiaHolder) near);
            }
        }
        return found;
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
                MirrorBlockEntity partner = partner();
                if (partner != null && holder instanceof BlockEntity jar && jar.getLevel() instanceof ServerLevel there) {
                    EssentiaReach.thread(there, jar.getBlockPos(), partner.getBlockPos(), aspect.value().color());
                }
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

    @Override
    public boolean reachableFrom(Direction side) {
        return false;
    }

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
}
