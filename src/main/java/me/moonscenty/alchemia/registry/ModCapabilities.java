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

/**
 * What our blocks will say yes to when something else asks them in the game's common terms.
 *
 * <p>So far only the crucible, which takes water from anything that pours it: a water jug, or another mod's pipe.
 * It gives nothing back that way -- what is in a crucible is not water any more once something is dissolved in it,
 * and a bucket is still the way to empty one.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class ModCapabilities {
    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CRUCIBLE.get(),
                (crucible, side) -> new CrucibleWater(crucible));
    }

    /** A crucible as a tank of water that only fills. */
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
}
