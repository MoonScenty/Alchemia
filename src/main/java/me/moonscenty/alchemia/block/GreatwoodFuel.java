package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

// a data map entry loses to the logs_that_burn tag
@EventBusSubscriber(modid = Alchemia.MODID)
public final class GreatwoodFuel {
    private static final int BURNS_FOR = 400;

    private GreatwoodFuel() {
    }

    @SubscribeEvent
    public static void burnTime(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().is(ModBlocks.GREATWOOD.log().get().asItem())) {
            event.setBurnTime(BURNS_FOR);
        }
    }
}
