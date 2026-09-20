package me.moonscenty.alchemia.player;

import me.moonscenty.alchemia.Alchemia;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data carried by a player and kept across death.
 * <p>
 * Both of these are the player's own business, so they are only ever sent to the player they belong to.
 */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Alchemia.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerKnowledge>> KNOWLEDGE =
            ATTACHMENTS.register("knowledge", () -> AttachmentType
                    .builder(PlayerKnowledge::fresh)
                    .serialize(PlayerKnowledge.CODEC)
                    .sync((holder, player) -> holder == player, PlayerKnowledge.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WarpData>> WARP =
            ATTACHMENTS.register("warp", () -> AttachmentType
                    .builder(() -> WarpData.NONE)
                    .serialize(WarpData.CODEC)
                    .sync((holder, player) -> holder == player, WarpData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}
