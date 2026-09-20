package me.moonscenty.alchemia.network;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.research.NoteRequests;
import net.minecraft.server.level.ServerPlayer;
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
    }

    private static void onRequestNote(RequestNote payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                NoteRequests.give(player, payload.research());
            }
        });
    }
}
