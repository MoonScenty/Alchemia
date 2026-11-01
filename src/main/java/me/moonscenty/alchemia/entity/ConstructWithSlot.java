package me.moonscenty.alchemia.entity;

import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A construct with one slot its owner fills by opening it. The slot is the main hand, which is where a mob's one held
 * thing lives and where it syncs from.
 */
public abstract class ConstructWithSlot extends Construct implements Container, MenuProvider {
    protected ConstructWithSlot(EntityType<? extends ConstructWithSlot> type, Level level) {
        super(type, level);
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected InteractionResult opened(Player player) {
        player.openMenu(this, buffer -> buffer.writeVarInt(getId()));
        return InteractionResult.CONSUME;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getMainHandItem().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? getMainHandItem() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack taken = getItem(slot).split(count);
        if (!taken.isEmpty()) {
            setChanged();
        }
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack held = getItem(slot);
        setItem(slot, ItemStack.EMPTY);
        return held;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            setItemSlot(EquipmentSlot.MAINHAND, stack);
            setChanged();
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return isAlive() && owns(player) && player.distanceToSqr(this) < 64.0;
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }
}
