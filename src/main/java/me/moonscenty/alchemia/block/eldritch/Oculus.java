package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.item.VisHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Opening the Eye: a wand to an eldritch altar with all four eyes in it, by someone who knows the rite, opens the way
 * above it. It costs five hundred of every primal.
 */
public final class Oculus {
    public static final ResourceLocation RESEARCH = Alchemia.id("oculus");
    private static final int EACH = 500;

    private Oculus() {
    }

    /** Whether the wand opened the way. */
    public static boolean open(Level level, BlockPos pos, Player player, ItemStack wand) {
        if (!(level.getBlockEntity(pos) instanceof EldritchAltarBlockEntity altar)
                || !(level.getBlockEntity(pos.above()) instanceof EldritchPortalBlockEntity portal)
                || altar.eyes() < EldritchAltarBlockEntity.MOST_EYES || altar.isOpen() || portal.isOpen()
                || !PlayerKnowledge.of(player).hasResearch(RESEARCH)) {
            return false;
        }
        AspectList cost = AspectList.of(ModAspects.AIR, EACH).add(ModAspects.FIRE, EACH).add(ModAspects.EARTH, EACH)
                .add(ModAspects.WATER, EACH).add(ModAspects.ORDER, EACH).add(ModAspects.ENTROPY, EACH);
        VisHolder holder = VisHolder.of(wand);
        if (holder == null || !holder.holds(wand, cost, player, VisHolder.Spending.CRAFTING)) {
            return false;
        }
        if (!level.isClientSide) {
            holder.take(wand, cost, player, VisHolder.Spending.CRAFTING);
            altar.setOpen(true);
            portal.open();
            level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }
}
