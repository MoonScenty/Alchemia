package me.moonscenty.alchemia.research;

import java.util.List;
import java.util.function.Predicate;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.entity.AngryZombie;
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

// The original's !/@ keys: either an aspect to know, or a clue stored as research under alchemia:clue/...
public final class Clues {
    public static final ResourceLocation BAT = clue("bat");
    public static final ResourceLocation TELEPORT = clue("teleport");
    public static final ResourceLocation BONE = clue("bone");
    public static final ResourceLocation BOW = clue("bow");
    public static final ResourceLocation ARROW = clue("arrow");
    public static final ResourceLocation BRAIN = clue("brain");
    public static final ResourceLocation TAINT_SHARD = clue("taint_shard");
    public static final ResourceLocation DISPENSER = clue("dispenser");
    public static final ResourceLocation NODE_MAGNET_DANGER = clue("node_magnet_danger");
    public static final ResourceLocation BATH_SALTS = clue("bath_salts");
    public static final ResourceLocation OUTER_MOB = clue("outer_mob");
    public static final ResourceLocation OUTER_BOSS = clue("outer_boss");
    public static final ResourceLocation ANCIENT_STONE = clue("ancient_stone");
    public static final ResourceLocation ELDRITCH_STONE = clue("eldritch_stone");
    public static final ResourceLocation GLYPHED_STONE = clue("glyphed_stone");
    public static final ResourceLocation PRIMORDIAL_PEARL = clue("primordial_pearl");
    public static final ResourceLocation FOCUS_PRIMAL = clue("focus_primal");
    // a research rather than a clue: scanning a golem of any kind teaches it outright
    public static final ResourceLocation BASIC_GOLEMANCY = Alchemia.id("basic_golemancy");

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
            new Found(DISPENSER, target -> is(target, Items.DISPENSER) || isBlock(target, Blocks.DISPENSER)),
            new Found(OUTER_MOB, target -> target instanceof ScanTarget.OfEntity seen
                    && seen.entity() instanceof me.moonscenty.alchemia.entity.EldritchMob),
            new Found(OUTER_BOSS, target -> target instanceof ScanTarget.OfEntity seen
                    && seen.entity() instanceof me.moonscenty.alchemia.entity.boss.ThaumcraftBoss),
            new Found(ANCIENT_STONE, target -> isBlock(target, ModBlocks.ANCIENT_STONE.block().get())),
            new Found(ELDRITCH_STONE, target -> isBlock(target, ModBlocks.ELDRITCH_STONE.block().get())),
            new Found(GLYPHED_STONE, target -> isBlock(target, ModBlocks.GLYPHED_STONE.get())),
            new Found(PRIMORDIAL_PEARL, target -> is(target, ModItems.PRIMORDIAL_PEARL.get())),
            new Found(BASIC_GOLEMANCY, target -> target instanceof ScanTarget.OfEntity seen
                    && (seen.entity() instanceof net.minecraft.world.entity.animal.AbstractGolem
                            || seen.entity() instanceof me.moonscenty.alchemia.entity.Construct
                            || seen.entity() instanceof me.moonscenty.alchemia.golem.Golem)));

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

    public static boolean met(PlayerKnowledge knowledge, ResourceLocation clue) {
        return ModAspects.REGISTRY.getHolder(clue).map(aspect -> knowledge.knows(aspect))
                .orElseGet(() -> knowledge.hasResearch(clue));
    }

    public static List<ResourceLocation> newIn(PlayerKnowledge knowledge, ScanTarget target) {
        return FOUND.stream().filter(found -> found.in().test(target) && !knowledge.hasResearch(found.clue()))
                .map(Found::clue).toList();
    }

    public static boolean give(Player player, ResourceLocation clue) {
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        if (knowledge.hasResearch(clue)) {
            return false;
        }
        if (!clue.getPath().startsWith("clue/")) {
            return ResearchCompletion.complete(player, clue);
        }
        PlayerKnowledge.update(player, known -> known.withResearch(clue));
        player.displayClientMessage(Component.translatable("research.alchemia.clue").withStyle(ChatFormatting.GREEN),
                false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.LEARN.get(),
                SoundSource.PLAYERS, 0.3F, 1.0F + player.getRandom().nextFloat() * 0.1F);
        return true;
    }

}
