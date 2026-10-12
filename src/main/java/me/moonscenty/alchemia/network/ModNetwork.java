package me.moonscenty.alchemia.network;

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

/**
 * The one thing a client has to ask the server for.
 * <p>
 * Everything else a player knows travels as an attachment, which syncs itself; only this is an action rather than a
 * fact, so it needs saying out loud.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public class ModNetwork {
    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(RequestNote.TYPE, RequestNote.STREAM_CODEC, ModNetwork::onRequestNote);
        registrar.playToServer(PlaceAspect.TYPE, PlaceAspect.STREAM_CODEC, ModNetwork::onPlaceAspect);
        registrar.playToServer(MixAspects.TYPE, MixAspects.STREAM_CODEC, ModNetwork::onMixAspects);
        registrar.playToServer(ChangeFocus.TYPE, ChangeFocus.STREAM_CODEC, ModNetwork::onChangeFocus);
        registrar.playToServer(ToolSetting.TYPE, ToolSetting.STREAM_CODEC, ModNetwork::onToolSetting);
        registrar.playToServer(ChooseFocus.TYPE, ChooseFocus.STREAM_CODEC, ModNetwork::onChooseFocus);
        registrar.playToServer(StartGolem.TYPE, StartGolem.STREAM_CODEC, ModNetwork::onStartGolem);
        registrar.playToClient(SyncSeal.TYPE, SyncSeal.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                () -> me.moonscenty.alchemia.client.ClientSeals.update(payload.pos(), payload.seal().orElse(null),
                        context.player().registryAccess())));
        registrar.playToClient(SyncSeals.TYPE, SyncSeals.STREAM_CODEC, (payload, context) -> context.enqueueWork(
                () -> me.moonscenty.alchemia.client.ClientSeals.replace(payload.seals(),
                        context.player().registryAccess())));
    }

    /**
     * Pressing a golem. The press is the one whose menu the player has open, and the menu checks they know every
     * part of what they asked for.
     */
    private static void onStartGolem(StartGolem payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof me.moonscenty.alchemia.menu.GolemPressMenu press) {
                press.start(payload.design(), player);
            }
        });
    }

    /**
     * Swapping a focus on a wand.
     * <p>
     * The wand is taken from the hand rather than named in the packet, so a client that is a tick behind about
     * which item is selected cannot put a focus on the wrong thing.
     */
    private static void onChangeFocus(ChangeFocus payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack held = player.getMainHandItem();
            if (!(held.getItem() instanceof WandItem)) {
                return;
            }
            if (payload.remove()) {
                Foci.remove(player, held);
            } else {
                Foci.next(player, held);
            }
        });
    }

    /**
     * Fitting the focus picked off the wheel.
     * <p>
     * The wand comes from the hand rather than the packet, for the same reason the cycle takes it from there.
     */
    private static void onChooseFocus(ChooseFocus payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.getMainHandItem().getItem() instanceof WandItem) {
                Foci.fit(player, player.getMainHandItem(), payload.focus());
            }
        });
    }

    /**
     * Turning the setting of whatever is in hand.
     * <p>
     * A wand: how far an architect's focus works, one less each press and round to the most it can do. The
     * shovel of the earthmover: which way it lays its square. Anything else does not listen.
     */
    private static void onToolSetting(ToolSetting payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof WandItem) {
                me.moonscenty.alchemia.wand.spell.Swapping.setReach(player, held);
            } else if (held.getItem() instanceof me.moonscenty.alchemia.item.tool.ElementalShovelItem) {
                me.moonscenty.alchemia.item.tool.ElementalShovelItem.turn(player, held);
            }
        });
    }

    private static void onPlaceAspect(PlaceAspect payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // the menu is the reader's claim on a particular desk, so nothing else needs saying about where they are
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof ResearchTableMenu table) {
                table.place(player, payload.at(), payload.aspect().flatMap(ModNetwork::aspect));
            }
        });
    }

    private static void onMixAspects(MixAspects payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof ResearchTableMenu table) {
                aspect(payload.one()).ifPresent(one ->
                        aspect(payload.other()).ifPresent(other -> table.mix(player, one, other)));
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
