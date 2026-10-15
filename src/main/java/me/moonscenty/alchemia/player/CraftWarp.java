package me.moonscenty.alchemia.player;

import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The warp of making something that should not be made, as the original's {@code addWarpToItem}: a point of sticking
 * warp on whoever made it, at the arcane workbench, the crucible or the altar. What is in the tag is the original's
 * list.
 */
public final class CraftWarp {
    private CraftWarp() {
    }

    public static void made(Player maker, ItemStack made) {
        if (maker != null && !maker.level().isClientSide && made.is(ModTags.Items.WARPS_WHEN_MADE)) {
            WarpHandler.add(maker, WarpData.Kind.STICKY, 1);
        }
    }
}
