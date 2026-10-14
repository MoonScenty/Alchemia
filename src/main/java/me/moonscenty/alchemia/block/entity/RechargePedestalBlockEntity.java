package me.moonscenty.alchemia.block.entity;

import org.joml.Vector3f;

import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.particle.SparkleOptions;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The wand on a recharge pedestal, and the filling of it.
 *
 * <p>Twice a second it draws for the wand from the aura of its chunk, as much as a wand in the hand draws -- five
 * points and the cap's bonus -- and a full draw shows as a mote of the last vis drawn, drifting in to the wand from
 * somewhere round about. A hopper can set a wand on it from any side, and take one off.
 */
public class RechargePedestalBlockEntity extends BlockEntity implements WorldlyContainer {
    /** How often it draws, in ticks, and how much it asks for each time. The original's. */
    private static final int EVERY = 10;
    private static final int AMOUNT = 5;
    private static final int[] SLOTS = {0};

    private ItemStack held = ItemStack.EMPTY;
    private int counter;

    public RechargePedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RECHARGE_PEDESTAL.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RechargePedestalBlockEntity pedestal) {
        if (pedestal.counter++ % EVERY != 0 || !(level instanceof ServerLevel served)
                || !me.moonscenty.alchemia.item.bauble.Recharging.chargeable(pedestal.held)) {
            return;
        }
        if (!(pedestal.held.getItem() instanceof WandItem)) {
            if (me.moonscenty.alchemia.item.bauble.Recharging.charge(pedestal.held, served, pos, AMOUNT)) {
                pedestal.changed();
            }
            return;
        }
        WandItem.Charged charged = WandItem.chargeFrom(pedestal.held, served, pos, AMOUNT);
        if (charged == null) {
            return;
        }
        pedestal.changed();
        if (charged.full()) {
            RandomSource random = served.random;
            BlockPos from = pos.offset(random.nextInt(3) - random.nextInt(3), 1 + random.nextInt(3),
                    random.nextInt(3) - random.nextInt(3));
            Vector3f target = new Vector3f(pos.getX() + 0.5F, pos.getY() + 1.5F, pos.getZ() + 0.5F);
            served.sendParticles(new SparkleOptions(charged.last().value().color(), target),
                    from.getX() + random.nextDouble(), from.getY() + random.nextDouble(),
                    from.getZ() + random.nextDouble(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** Saved, and shown to anyone watching, since what is on it and how full it is are there to be seen. */
    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- the one place on top ------------------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return held.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return held;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = held.split(amount);
        changed();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack taken = held;
        held = ItemStack.EMPTY;
        changed();
        return taken;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        held = stack;
        changed();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return me.moonscenty.alchemia.item.bauble.Recharging.chargeable(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        held = ItemStack.EMPTY;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return held.isEmpty() && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        held = tag.contains("held")
                ? ItemStack.parse(registries, tag.getCompound("held")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!held.isEmpty()) {
            tag.put("held", held.save(registries));
        }
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
