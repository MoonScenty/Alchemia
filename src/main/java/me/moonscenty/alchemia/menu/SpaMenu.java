package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.block.entity.SpaBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class SpaMenu extends AlchemiaMenu {
    public static final int TOGGLE_MIXING = 0;

    private static final int SALTS_X = 65;
    private static final int SALTS_Y = 31;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;

    private final Container spa;
    private final ContainerData readings;
    private final ContainerLevelAccess access;
    private final SpaBlockEntity block;

    public SpaMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1), new SimpleContainerData(SpaBlockEntity.READINGS),
                ContainerLevelAccess.NULL, null);
    }

    public SpaMenu(int id, Inventory inventory, SpaBlockEntity spa, ContainerData readings,
            ContainerLevelAccess access) {
        this(id, inventory, spa, readings, access, spa);
    }

    private SpaMenu(int id, Inventory inventory, Container spa, ContainerData readings, ContainerLevelAccess access,
            SpaBlockEntity block) {
        super(ModMenus.SPA.get(), id);
        this.spa = spa;
        this.readings = readings;
        this.access = access;
        this.block = block;
        checkContainerSize(spa, 1);
        addDataSlots(readings);

        addSlot(new Slot(spa, 0, SALTS_X, SALTS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.BATH_SALTS.get());
            }
        });
        addPlayerInventory(inventory, PACK_X, PACK_Y);
    }

    @Override
    public boolean clickMenuButton(Player player, int which) {
        if (which == TOGGLE_MIXING && block != null) {
            block.toggleMixing();
            return true;
        }
        return false;
    }

    public int amount() {
        return readings.get(SpaBlockEntity.READING_AMOUNT);
    }

    public Fluid fluid() {
        return BuiltInRegistries.FLUID.byId(readings.get(SpaBlockEntity.READING_FLUID));
    }

    public boolean mixing() {
        return readings.get(SpaBlockEntity.READING_MIXING) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, 1);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.SPA.get());
    }
}
