package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@EventBusSubscriber(modid = Alchemia.MODID)
public final class ModCapabilities {
    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CRUCIBLE.get(),
                (crucible, side) -> new CrucibleWater(crucible));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.RECHARGE_PEDESTAL.get(),
                (pedestal, side) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(pedestal));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.AURA_TOTEM.get(),
                (totem, side) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(totem));
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, found, side) -> {
            net.minecraft.core.BlockPos head = me.moonscenty.alchemia.block.AuraTotemBlock.headOf(level, pos);
            return head != null && level.getBlockEntity(head)
                    instanceof me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity totem
                    ? new IntoTotem(totem) : null;
        }, ModBlocks.AURA_TOTEM_POLE_INNER.get(), ModBlocks.AURA_TOTEM_POLE_OUTER.get(),
                ModBlocks.AURA_TOTEM_POLE_PURE.get());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MIRROR.get(),
                (mirror, side) -> mirror.essentia() ? null : new IntoMirror(mirror));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.SPA.get(),
                (spa, side) -> spa.tank(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.SPA.get(),
                (spa, side) -> side == null ? new net.neoforged.neoforge.items.wrapper.InvWrapper(spa)
                        : new net.neoforged.neoforge.items.wrapper.SidedInvWrapper(spa, side));
    }

    private static final class IntoTotem extends net.neoforged.neoforge.items.wrapper.InvWrapper {
        IntoTotem(net.minecraft.world.Container totem) {
            super(totem);
        }

        @Override
        public net.minecraft.world.item.ItemStack extractItem(int slot, int amount, boolean simulate) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
    }

    private record CrucibleWater(CrucibleBlockEntity crucible) implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return crucible.water() > 0 ? new FluidStack(Fluids.WATER, crucible.water()) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return CrucibleBlockEntity.FULL;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return stack.is(Fluids.WATER);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!resource.is(Fluids.WATER)) {
                return 0;
            }
            return crucible.pour(resource.getAmount(), action.execute());
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    private record IntoMirror(me.moonscenty.alchemia.block.entity.MirrorBlockEntity mirror)
            implements net.neoforged.neoforge.items.IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public net.minecraft.world.item.ItemStack getStackInSlot(int slot) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        @Override
        public net.minecraft.world.item.ItemStack insertItem(int slot, net.minecraft.world.item.ItemStack stack,
                boolean simulate) {
            if (!simulate && !stack.isEmpty()) {
                mirror.insert(stack.copy());
            }
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        @Override
        public net.minecraft.world.item.ItemStack extractItem(int slot, int amount, boolean simulate) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, net.minecraft.world.item.ItemStack stack) {
            return true;
        }
    }
}
