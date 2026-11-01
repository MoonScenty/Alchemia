package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.aura.ShardTuning;
import me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AuraTotemMenu extends AlchemiaMenu {
    private static final int SHARDS_X = 80;
    private static final int SHARDS_Y = 24;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;

    private final ContainerData readings;
    private final Container totem;

    public AuraTotemMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1), new SimpleContainerData(AuraTotemBlockEntity.READINGS));
    }

    public AuraTotemMenu(int id, Inventory inventory, Container totem, ContainerData readings) {
        super(ModMenus.AURA_TOTEM.get(), id);
        this.totem = totem;
        this.readings = readings;
        checkContainerSize(totem, 1);
        addDataSlots(readings);

        addSlot(new Slot(totem, 0, SHARDS_X, SHARDS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ShardTuning.isShard(stack);
            }
        });
        addPlayerInventory(inventory, PACK_X, PACK_Y);
    }

    public float left() {
        return readings.get(AuraTotemBlockEntity.READING_TIME) / (float) AuraTotemBlockEntity.LASTS;
    }

    public int tuning() {
        return readings.get(AuraTotemBlockEntity.READING_TUNING);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, 1);
    }

    @Override
    public boolean stillValid(Player player) {
        return totem.stillValid(player);
    }
}
