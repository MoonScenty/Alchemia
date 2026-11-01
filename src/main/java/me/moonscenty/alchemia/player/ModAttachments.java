package me.moonscenty.alchemia.player;

import me.moonscenty.alchemia.Alchemia;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

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

    // -2 never rolled, -1 rolled and ordinary, else a ChampionModifier index; synced to everyone tracking
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> CHAMPION =
            ATTACHMENTS.register("champion", () -> AttachmentType
                    .builder(() -> me.moonscenty.alchemia.entity.Champions.UNROLLED)
                    .serialize(com.mojang.serialization.Codec.INT)
                    .sync(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                    .build());

    private ModAttachments() {
    }
}
