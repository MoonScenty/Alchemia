package me.moonscenty.alchemia.player;

import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// Original's addWarpToItem: crafting a tagged item gives the crafter a point of sticky warp.
public final class CraftWarp {
    private CraftWarp() {
    }

    public static void made(Player maker, ItemStack made) {
        if (maker != null && !maker.level().isClientSide && made.is(ModTags.Items.WARPS_WHEN_MADE)) {
            WarpHandler.add(maker, WarpData.Kind.STICKY, 1);
        }
    }
}
