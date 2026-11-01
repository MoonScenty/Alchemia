package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.entity.AdvancedAutomatedCrossbow;
import me.moonscenty.alchemia.entity.AutomatedCrossbow;
import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrossbowMenu extends AlchemiaMenu {
    private static final int AMMO_X = 80;
    private static final int AMMO_X_ADVANCED = 42;
    private static final int AMMO_Y = 29;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;

    private final AutomatedCrossbow turret;

    public CrossbowMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, inventory.player.level().getEntity(buffer.readVarInt()) instanceof AutomatedCrossbow found
                ? found : null);
    }

    public CrossbowMenu(int id, Inventory inventory, AutomatedCrossbow turret) {
        super(ModMenus.AUTOMATED_CROSSBOW.get(), id);
        this.turret = turret;
        int ammoX = turret instanceof AdvancedAutomatedCrossbow ? AMMO_X_ADVANCED : AMMO_X;
        if (turret != null) {
            addSlot(new Slot(turret, 0, ammoX, AMMO_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return AutomatedCrossbow.takes(stack);
                }
            });
        }
        addPlayerInventory(inventory, PACK_X, PACK_Y);
    }

    public AutomatedCrossbow turret() {
        return turret;
    }

    @Override
    public boolean clickMenuButton(Player player, int which) {
        if (!(turret instanceof AdvancedAutomatedCrossbow advanced) || !turret.stillValid(player) || which < 0
                || which > Autocaster.FRIENDS) {
            return false;
        }
        advanced.mind(which, !advanced.minds(which));
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return turret != null && turret.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return turret == null ? ItemStack.EMPTY : quickMoveBetween(index, 1);
    }
}
