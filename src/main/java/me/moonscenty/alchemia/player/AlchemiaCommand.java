package me.moonscenty.alchemia.player;

import java.util.Locale;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.StartingResearch;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Operator commands for poking at a player's knowledge and warp, so both can be exercised before the things that
 * normally drive them exist.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public class AlchemiaCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Alchemia.MODID)
                .requires(source -> source.hasPermission(2))
                .then(aspects())
                .then(research())
                .then(warp()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> aspects() {
        return Commands.literal("aspects")
                .then(Commands.literal("list").executes(context -> listAspects(context.getSource())))
                .then(Commands.literal("discover")
                        .then(Commands.literal("all").executes(context -> discoverAll(context.getSource())))
                        .then(Commands.argument("aspect", ResourceLocationArgument.id())
                                .executes(context -> discover(context.getSource(), ResourceLocationArgument.getId(context, "aspect")))))
                .then(Commands.literal("forget").executes(context -> forget(context.getSource())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> research() {
        return Commands.literal("research")
                .then(Commands.literal("list").executes(context -> listResearch(context.getSource())))
                .then(Commands.literal("grant")
                        .then(Commands.literal("all").executes(context -> grantAll(context.getSource())))
                        .then(Commands.argument("entry", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                        entries(context.getSource()).keySet(), builder))
                                .executes(context -> grant(context.getSource(), ResourceLocationArgument.getId(context, "entry")))))
                .then(Commands.literal("forget").executes(context -> forgetResearch(context.getSource())));
    }

    private static Registry<ResearchEntry> entries(CommandSourceStack source) {
        return source.registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
    }

    private static int listResearch(CommandSourceStack source) throws CommandSyntaxException {
        PlayerKnowledge knowledge = PlayerKnowledge.of(source.getPlayerOrException());
        Registry<ResearchEntry> entries = entries(source);
        String done = knowledge.completedResearch().stream().map(ResourceLocation::toString).sorted()
                .reduce((a, b) -> a + ", " + b).orElse("-");
        source.sendSuccess(() -> Component.literal("Completed " + knowledge.completedResearch().size()
                + " of " + entries.size() + ": " + done), false);
        return knowledge.completedResearch().size();
    }

    private static int grant(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!entries(source).containsKey(id)) {
            source.sendFailure(Component.literal("No such research: " + id));
            return 0;
        }
        player.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.of(player).withResearch(id));
        source.sendSuccess(() -> Component.literal("Completed " + id), false);
        return 1;
    }

    private static int grantAll(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Registry<ResearchEntry> entries = entries(source);
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        for (ResourceLocation id : entries.keySet()) {
            knowledge = knowledge.withResearch(id);
        }
        player.setData(ModAttachments.KNOWLEDGE, knowledge);
        source.sendSuccess(() -> Component.literal("Completed every piece of research"), false);
        return entries.size();
    }

    /** Clearing the research hands back whatever starts unlocked, or the book would have nothing to open. */
    private static int forgetResearch(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        player.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.of(player).withoutResearch());
        StartingResearch.grant(player);
        source.sendSuccess(() -> Component.literal("Back to only what a reader starts with"), false);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> warp() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("warp")
                .then(Commands.literal("get").executes(context -> showWarp(context.getSource())))
                .then(Commands.literal("check").executes(context -> forceCheck(context.getSource())));

        for (WarpData.Kind kind : WarpData.Kind.values()) {
            command = command.then(Commands.literal(kind.name().toLowerCase(Locale.ROOT))
                    .then(Commands.argument("amount", IntegerArgumentType.integer(-1000, 1000))
                            .executes(context -> addWarp(context.getSource(), kind, IntegerArgumentType.getInteger(context, "amount")))));
        }
        return command;
    }

    private static int listAspects(CommandSourceStack source) throws CommandSyntaxException {
        PlayerKnowledge knowledge = PlayerKnowledge.of(source.getPlayerOrException());
        String known = knowledge.discoveredAspects().stream().map(Aspect::tag).sorted().reduce((a, b) -> a + ", " + b).orElse("-");
        source.sendSuccess(() -> Component.literal("Discovered " + knowledge.discoveredAspects().size()
                + " of " + ModAspects.REGISTRY.size() + ": " + known), false);
        return knowledge.discoveredAspects().size();
    }

    private static int discover(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Holder<Aspect> aspect = ModAspects.REGISTRY.getHolder(id).orElse(null);
        if (aspect == null) {
            source.sendFailure(Component.literal("No such aspect: " + id));
            return 0;
        }
        player.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.of(player).withAspect(aspect));
        source.sendSuccess(() -> Component.literal("Discovered " + aspect.value().tag()), false);
        return 1;
    }

    private static int discoverAll(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        for (Holder<Aspect> aspect : ModAspects.REGISTRY.holders().map(holder -> (Holder<Aspect>) holder).toList()) {
            knowledge = knowledge.withAspect(aspect);
        }
        player.setData(ModAttachments.KNOWLEDGE, knowledge);
        source.sendSuccess(() -> Component.literal("Discovered every aspect"), false);
        return ModAspects.REGISTRY.size();
    }

    private static int forget(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        player.setData(ModAttachments.KNOWLEDGE, PlayerKnowledge.of(player).withoutAspects());
        source.sendSuccess(() -> Component.literal("Back to knowing only the primals"), false);
        return 1;
    }

    private static int showWarp(CommandSourceStack source) throws CommandSyntaxException {
        WarpData warp = WarpData.of(source.getPlayerOrException());
        source.sendSuccess(() -> Component.literal("Warp: permanent " + warp.permanent() + ", sticky " + warp.sticky()
                + ", temporary " + warp.temporary() + " (total " + warp.total() + "), counter " + warp.counter()), false);
        return warp.total();
    }

    private static int addWarp(CommandSourceStack source, WarpData.Kind kind, int amount)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        WarpHandler.add(player, kind, amount);
        return showWarp(source);
    }

    /** Rolls the warp check right away instead of waiting out the interval. */
    private static int forceCheck(CommandSourceStack source) throws CommandSyntaxException {
        WarpHandler.check(source.getPlayerOrException());
        return showWarp(source);
    }
}
