package me.moonscenty.alchemia.aura;

import me.moonscenty.alchemia.Alchemia;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The aura rides on the chunk itself, so it is saved and loaded with the land it belongs to and nothing has to keep
 * a separate map of the world.
 */
public class ModAuraAttachment {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Alchemia.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AuraChunk>> AURA =
            ATTACHMENTS.register("aura", () -> AttachmentType
                    .builder(() -> AuraChunk.NONE)
                    .serialize(AuraChunk.CODEC)
                    .build());

    private ModAuraAttachment() {
    }
}
