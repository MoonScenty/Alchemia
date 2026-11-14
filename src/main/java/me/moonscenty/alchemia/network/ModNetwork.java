package me.moonscenty.alchemia.network;

import java.util.function.BiConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.menu.ResearchTableMenu;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.research.NoteRequests;
import net.minecraft.core.Holder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.wand.Foci;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Alchemia.MODID)
public class ModNetwork {
    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(RequestNote.TYPE, RequestNote.STREAM_CODEC, ModNetwork::onRequestNote);
        registrar.playToServer(BuyResearch.TYPE, BuyResearch.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                () -> {
                    if (context.player() instanceof ServerPlayer player) {
                        me.moonscenty.alchemia.research.ResearchPurchase.buy(player, payload.research());
                    }
                }));
        registrar.playToServer(PlaceAspect.TYPE, PlaceAspect.STREAM_CODEC, ModNetwork::onPlaceAspect);
        registrar.playToServer(MixAspects.TYPE, MixAspects.STREAM_CODEC, ModNetwork::onMixAspects);
        registrar.playToServer(SpendFragment.TYPE, SpendFragment.STREAM_CODEC, ModNetwork::onSpendFragment);
        registrar.playToServer(ChangeFocus.TYPE, ChangeFocus.STREAM_CODEC, ModNetwork::onChangeFocus);
        registrar.playToServer(ToolSetting.TYPE, ToolSetting.STREAM_CODEC, ModNetwork::onToolSetting);
        registrar.playToServer(ChooseFocus.TYPE, ChooseFocus.STREAM_CODEC, ModNetwork::onChooseFocus);
        registrar.playToServer(StartGolem.TYPE, StartGolem.STREAM_CODEC, ModNetwork::onStartGolem);
        registrar.playToServer(ToggleHover.TYPE, ToggleHover.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                () -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        me.moonscenty.alchemia.item.Hover.toggle(player);
                    }
                }));
        registrar.playToClient(ShieldRunes.TYPE, ShieldRunes.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                () -> me.moonscenty.alchemia.client.ShieldRunesRenderer.flare(payload.shielded(), payload.from())));
        registrar.playToClient(BlockArc.TYPE, BlockArc.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                () -> me.moonscenty.alchemia.client.EldritchEffects.blockArc(payload)));
        registrar.playToClient(ResearchLearned.TYPE, ResearchLearned.STREAM_CODEC, (payload, context) -> context
                .enqueueWork(() -> me.moonscenty.alchemia.client.ResearchToast.show(payload.research())));
        registrar.playToClient(AuraReading.TYPE, AuraReading.STREAM_CODEC, (payload, context) -> context
                .enqueueWork(() -> me.moonscenty.alchemia.client.ThaumometerHud.read(payload)));
        registrar.playToClient(WarpVignette.TYPE, WarpVignette.STREAM_CODEC, (payload, context) -> context
                .enqueueWork(() -> me.moonscenty.alchemia.client.WarpScreenEffects.darken(payload.ticks())));
        registrar.playToClient(Mist.TYPE, Mist.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                () -> me.moonscenty.alchemia.client.WarpFog.bring(payload.ticks(), payload.topUp())));
        registrar.playToClient(SyncSeal.TYPE, SyncSeal.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                () -> me.moonscenty.alchemia.client.ClientSeals.update(payload.pos(), payload.seal().orElse(null),
                        context.player().registryAccess())));
        registrar.playToClient(SyncSeals.TYPE, SyncSeals.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                () -> me.moonscenty.alchemia.client.ClientSeals.replace(payload.seals(),
                        context.player().registryAccess())));
    }

    private static void onStartGolem(StartGolem payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof me.moonscenty.alchemia.menu.GolemPressMenu press) {
                press.start(payload.design(), player);
            }
        });
    }

    // the wand comes from the server-side hand, not the packet, so a lagging client cannot target the wrong item
    private static void onChangeFocus(ChangeFocus payload, IPayloadContext context) {
        withWand(context, (player, wand) -> {
            if (WandItem.isSceptre(wand)) {
                return;
            }
            if (payload.remove()) {
                Foci.remove(player, wand);
            } else {
                Foci.next(player, wand);
            }
        });
    }

    private static void onChooseFocus(ChooseFocus payload, IPayloadContext context) {
        withWand(context, (player, wand) -> {
            if (!WandItem.isSceptre(wand)) {
                Foci.fit(player, wand, payload.focus());
            }
        });
    }

    private static void withWand(IPayloadContext context, BiConsumer<ServerPlayer, ItemStack> work) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.getMainHandItem().getItem() instanceof WandItem) {
                work.accept(player, player.getMainHandItem());
            }
        });
    }

    private static void onToolSetting(ToolSetting payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof me.moonscenty.alchemia.item.ThaumometerItem) {
                me.moonscenty.alchemia.item.ThaumometerItem.cycle(player, held);
            } else if (held.getItem() instanceof WandItem) {
                me.moonscenty.alchemia.wand.spell.Swapping.setArea(player, held);
            } else if (held.getItem() instanceof me.moonscenty.alchemia.item.tool.ElementalShovelItem) {
                me.moonscenty.alchemia.item.tool.ElementalShovelItem.turn(player, held);
            }
        });
    }

    private static void onPlaceAspect(PlaceAspect payload, IPayloadContext context) {
        atTable(context, (player, table) ->
                table.place(player, payload.at(), payload.aspect().flatMap(ModNetwork::aspect)));
    }

    private static void onMixAspects(MixAspects payload, IPayloadContext context) {
        atTable(context, (player, table) -> aspect(payload.one()).ifPresent(one ->
                aspect(payload.other()).ifPresent(other -> table.mix(player, one, other))));
    }

    private static void onSpendFragment(SpendFragment payload, IPayloadContext context) {
        atTable(context, (player, table) -> table.spendFragment(player));
    }

    private static void atTable(IPayloadContext context, BiConsumer<ServerPlayer, ResearchTableMenu> work) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof ResearchTableMenu table) {
                work.accept(player, table);
            }
        });
    }

    private static java.util.Optional<Holder<Aspect>> aspect(net.minecraft.resources.ResourceLocation id) {
        return ModAspects.REGISTRY.getHolder(id).map(holder -> (Holder<Aspect>) holder);
    }

    private static void onRequestNote(RequestNote payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                NoteRequests.give(player, payload.research());
            }
        });
    }
}
