package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EssentiaSmelterMenu extends AlchemiaMenu {
    private static final int SMELTER_SLOTS = 2;

    private static final int INPUT_X = 80;
    private static final int INPUT_Y = 8;
    private static final int FUEL_X = 80;
    private static final int FUEL_Y = 48;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;

    private final Container smelter;
    private final ContainerData readings;
    private final ContainerLevelAccess access;
    private final BlockPos at;

    public EssentiaSmelterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf extra) {
        this(id, inventory, new SimpleContainer(SMELTER_SLOTS),
                new SimpleContainerData(EssentiaSmelterBlockEntity.READINGS),
                ContainerLevelAccess.NULL, extra.readBlockPos());
    }

    public EssentiaSmelterMenu(int id, Inventory inventory, Container smelter, ContainerData readings,
            ContainerLevelAccess access, BlockPos at) {
        super(ModMenus.ESSENTIA_SMELTER.get(), id);
        this.smelter = smelter;
        this.readings = readings;
        this.access = access;
        this.at = at;
        checkContainerSize(smelter, SMELTER_SLOTS);
        addDataSlots(readings);

        addSlot(new Slot(smelter, EssentiaSmelterBlockEntity.SLOT_INPUT, INPUT_X, INPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return smelter.canPlaceItem(EssentiaSmelterBlockEntity.SLOT_INPUT, stack);
            }
        });
        addSlot(new Slot(smelter, EssentiaSmelterBlockEntity.SLOT_FUEL, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return smelter.canPlaceItem(EssentiaSmelterBlockEntity.SLOT_FUEL, stack);
            }
        });

        addPlayerInventory(inventory, PACK_X, PACK_Y);
    }

    public BlockPos at() {
        return at;
    }

    public float burning() {
        int lasts = readings.get(EssentiaSmelterBlockEntity.READING_BURNS_FOR);
        return lasts <= 0 ? 0.0F : readings.get(EssentiaSmelterBlockEntity.READING_BURNING) / (float) lasts;
    }

    public float cooked() {
        int takes = readings.get(EssentiaSmelterBlockEntity.READING_COOKS_FOR);
        return takes <= 0 ? 0.0F : readings.get(EssentiaSmelterBlockEntity.READING_COOKED) / (float) takes;
    }

    public float filled() {
        return Math.min(1.0F, readings.get(EssentiaSmelterBlockEntity.READING_HELD)
                / (float) EssentiaSmelterBlockEntity.CAPACITY);
    }

    public int held() {
        return readings.get(EssentiaSmelterBlockEntity.READING_HELD);
    }

    public AspectList dissolved(net.minecraft.world.level.Level level) {
        return level != null && level.getBlockEntity(at) instanceof EssentiaSmelterBlockEntity found
                ? found.held()
                : AspectList.EMPTY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, SMELTER_SLOTS);
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> level.getBlockState(pos).getBlock()
                instanceof me.moonscenty.alchemia.block.EssentiaSmelterBlock
                && player.distanceToSqr(pos.getCenter()) <= 64.0, true);
    }
}
