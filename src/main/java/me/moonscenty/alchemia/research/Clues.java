package me.moonscenty.alchemia.research;

import java.util.List;
import java.util.function.Predicate;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.entity.AngryZombie;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Clues, the original's {@code !}- and {@code @}-keys: things found out in the world rather than at the table, that
 * some research waits on before it shows in the book or can be taken up.
 * <p>
 * A clue is either an aspect, which is met once the player knows that aspect, or a clue of its own, kept among the
 * player's research under {@code alchemia:clue/...}. Most of those come from holding an alchemometer up to the right
 * thing -- a bat, a portal, a bone -- and one, the bath salts', from the headaches warp brings on.
 */
public final class Clues {
    public static final ResourceLocation BAT = clue("bat");
    public static final ResourceLocation TELEPORT = clue("teleport");
    public static final ResourceLocation BONE = clue("bone");
    public static final ResourceLocation BOW = clue("bow");
    public static final ResourceLocation ARROW = clue("arrow");
    public static final ResourceLocation BRAIN = clue("brain");
    public static final ResourceLocation TAINT_SHARD = clue("taint_shard");
    public static final ResourceLocation DISPENSER = clue("dispenser");
    /** The headaches of warp, which want something to soothe them: the original's {@code @BATHSALTS}. */
    public static final ResourceLocation BATH_SALTS = clue("bath_salts");

    /** What scanning finds each clue in, the original's scannable things. */
    private static final List<Found> FOUND = List.of(
            new Found(BAT, target -> target instanceof ScanTarget.OfEntity seen && seen.entity() instanceof Bat),
            new Found(TELEPORT, target -> is(target, Items.ENDER_PEARL)
                    || isBlock(target, Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME,
                            ModBlocks.ELDRITCH_PORTAL.get())
                    || target instanceof ScanTarget.OfEntity seen && seen.entity() instanceof EnderMan),
            new Found(BONE, target -> is(target, Items.BONE)),
            new Found(BOW, target -> is(target, Items.BOW)),
            new Found(ARROW, target -> is(target, Items.ARROW)),
            new Found(BRAIN, target -> is(target, ModItems.ZOMBIE_BRAIN.get())
                    || target instanceof ScanTarget.OfEntity seen && seen.entity() instanceof AngryZombie),
            new Found(TAINT_SHARD, target -> is(target, ModItems.SHARDS.get(CrystalType.FLUX).get())
                    || isBlock(target, ModBlocks.CRYSTALS.get(CrystalType.FLUX).get())),
            new Found(DISPENSER, target -> is(target, Items.DISPENSER) || isBlock(target, Blocks.DISPENSER)));

    private record Found(ResourceLocation clue, Predicate<ScanTarget> in) {
    }

    private Clues() {
    }

    private static ResourceLocation clue(String name) {
        return Alchemia.id("clue/" + name);
    }

    private static boolean is(ScanTarget target, Item item) {
        return target instanceof ScanTarget.OfItem held && held.stack().is(item);
    }

    private static boolean isBlock(ScanTarget target, Block... blocks) {
        if (!(target instanceof ScanTarget.OfBlock seen)) {
            return false;
        }
        for (Block block : blocks) {
            if (seen.state().is(block)) {
                return true;
            }
        }
        return false;
    }

    /** Whether a clue is met: an aspect known, or a clue found. */
    public static boolean met(PlayerKnowledge knowledge, ResourceLocation clue) {
        return ModAspects.REGISTRY.getHolder(clue).map(aspect -> knowledge.knows(aspect))
                .orElseGet(() -> knowledge.hasResearch(clue));
    }

    /** The clues this scan would turn up that the player has not found yet. */
    public static List<ResourceLocation> newIn(PlayerKnowledge knowledge, ScanTarget target) {
        return FOUND.stream().filter(found -> found.in().test(target) && !knowledge.hasResearch(found.clue()))
                .map(Found::clue).toList();
    }

    /**
     * Gives a player a clue, with the original's word that something has been found; whether it was new.
     */
    public static boolean give(Player player, ResourceLocation clue) {
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        if (knowledge.hasResearch(clue)) {
            return false;
        }
        player.setData(ModAttachments.KNOWLEDGE, knowledge.withResearch(clue));
        player.displayClientMessage(Component.translatable("research.alchemia.clue").withStyle(ChatFormatting.GREEN),
                false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LEARN.get(),
                SoundSource.PLAYERS, 0.3F, 1.0F + player.getRandom().nextFloat() * 0.1F);
        return true;
    }

}
