package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.item.VisHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class Oculus {
    public static final ResourceLocation RESEARCH = Alchemia.id("oculus");
    private static final int EACH = 500;

    private Oculus() {
    }

    public static boolean open(Level level, BlockPos pos, Player player, ItemStack wand) {
        if (!(level.getBlockEntity(pos) instanceof EldritchAltarBlockEntity altar)
                || !(level.getBlockEntity(pos.above()) instanceof EldritchPortalBlockEntity portal)
                || altar.eyes() < EldritchAltarBlockEntity.MOST_EYES || altar.isOpen() || portal.isOpen()
                || !PlayerKnowledge.of(player).hasResearch(RESEARCH)) {
            return false;
        }
        if (!VisHolder.pay(wand, AspectList.eachPrimal(EACH), player, VisHolder.Spending.CRAFTING)) {
            return false;
        }
        if (level instanceof net.minecraft.server.level.ServerLevel served) {
            me.moonscenty.alchemia.outer.Labyrinth.of(served.getServer()).ensureMaze(pos, level.random);
        }
        if (!level.isClientSide) {
            altar.setOpen(true);
            portal.open();
            level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }
}
