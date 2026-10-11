package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;

/**
 * Purifying bath salts. Thrown into still water, they dissolve and the water turns to purifying fluid.
 *
 * <p>They are fragile: dropped, they last ten seconds rather than five minutes, and it is when they are gone that the
 * water they lay in is turned. Lying anywhere else they are simply gone.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public class BathSaltsItem extends Item {
    /** How long they last on the ground, in ticks. The original's. */
    private static final int LASTS = 200;

    public BathSaltsItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return LASTS;
    }

    /** Dissolved where they lay: in a still pool of water, that pool turns. */
    @SubscribeEvent
    public static void onExpire(ItemExpireEvent event) {
        ItemEntity salts = event.getEntity();
        if (!salts.getItem().is(ModItems.BATH_SALTS.get())) {
            return;
        }
        Level level = salts.level();
        BlockPos at = salts.blockPosition();
        if (level.getBlockState(at).is(Blocks.WATER) && level.getFluidState(at).isSource()) {
            level.setBlock(at, ModBlocks.PURIFYING_FLUID.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
