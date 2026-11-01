package me.moonscenty.alchemia.menu;

import me.moonscenty.alchemia.block.entity.FocalManipulatorBlockEntity;
import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FocalManipulatorMenu extends AlchemiaMenu {
    private static final int FOCUS_X = 88;
    private static final int FOCUS_Y = 60;
    private static final int PACK_X = 16;
    private static final int PACK_Y = 151;

    private final FocalManipulatorBlockEntity bench;

    public FocalManipulatorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, found(inventory, buffer.readBlockPos()));
    }

    public FocalManipulatorMenu(int id, Inventory inventory, FocalManipulatorBlockEntity bench) {
        super(ModMenus.FOCAL_MANIPULATOR.get(), id);
        this.bench = bench;

        addSlot(new Slot(bench, 0, FOCUS_X, FOCUS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof FocusItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addPlayerInventory(inventory, PACK_X, PACK_Y);
    }

    public FocalManipulatorBlockEntity bench() {
        return bench;
    }

    private static FocalManipulatorBlockEntity found(Inventory inventory, BlockPos at) {
        BlockEntity there = inventory.player.level().getBlockEntity(at);
        return there instanceof FocalManipulatorBlockEntity bench ? bench : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int which) {
        var upgrades = me.moonscenty.alchemia.wand.FocusUpgrade.values();
        if (bench == null || which < 0 || which >= upgrades.length) {
            return false;
        }
        if (bench.start(upgrades[which], player)) {
            return true;
        }
        if (!player.level().isClientSide) {
            player.level().playSound(null, bench.getBlockPos(),
                    me.moonscenty.alchemia.registry.ModSounds.CRAFT_FAIL.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.33F, 1.0F);
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return bench != null && bench.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, 1);
    }
}
