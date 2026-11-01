package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.entity.Autocaster;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AutocasterMenu extends AlchemiaMenu {
    // original's layout
    private static final int FOCUS_X = 36;
    private static final int FOCUS_Y = 29;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;

    private final Autocaster turret;

    public AutocasterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, found(inventory, buffer.readVarInt()));
    }

    public AutocasterMenu(int id, Inventory inventory, Autocaster turret) {
        super(ModMenus.AUTOCASTER.get(), id);
        this.turret = turret;

        addSlot(new Slot(turret, 0, FOCUS_X, FOCUS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Autocaster.takes(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addPlayerInventory(inventory, PACK_X, PACK_Y);
    }

    public Autocaster turret() {
        return turret;
    }

    private static Autocaster found(Inventory inventory, int id) {
        return inventory.player.level().getEntity(id) instanceof Autocaster turret ? turret : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int which) {
        if (turret == null || !turret.stillValid(player) || which < 0 || which > Autocaster.FRIENDS) {
            return false;
        }
        turret.mind(which, !turret.minds(which));
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return turret != null && turret.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, 1);
    }
}
