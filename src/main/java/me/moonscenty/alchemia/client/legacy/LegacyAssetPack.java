package me.moonscenty.alchemia.client.legacy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

/**
 * The imported files, held in memory and served as a resource pack. Nothing is written to disk: the pictures are
 * the original's, and the only copy of them stays the jar the player brought.
 */
final class LegacyAssetPack extends AbstractPackResources {
    private final Map<String, byte[]> files;
    private final byte[] meta;

    /** @param files our files by their path under {@code assets/alchemia/} */
    LegacyAssetPack(PackLocationInfo location, Map<String, byte[]> files) {
        super(location);
        this.files = files;
        int format = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
        this.meta = ("{\"pack\":{\"pack_format\":" + format
                + ",\"description\":{\"translate\":\"pack.alchemia.legacy.description\"}}}")
                .getBytes(StandardCharsets.UTF_8);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        if (elements.length == 1 && PACK_META.equals(elements[0])) {
            return () -> new ByteArrayInputStream(meta);
        }
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES || !Alchemia.MODID.equals(location.getNamespace())) {
            return null;
        }
        byte[] bytes = files.get(location.getPath());
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !Alchemia.MODID.equals(namespace)) {
            return;
        }
        String prefix = path.endsWith("/") ? path : path + "/";
        files.forEach((file, bytes) -> {
            if (file.startsWith(prefix)) {
                output.accept(Alchemia.id(file), () -> new ByteArrayInputStream(bytes));
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES && !files.isEmpty() ? Set.of(Alchemia.MODID) : Set.of();
    }

    @Override
    public void close() {
    }
}
