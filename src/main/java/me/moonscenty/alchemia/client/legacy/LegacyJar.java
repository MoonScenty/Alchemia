package me.moonscenty.alchemia.client.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class LegacyJar implements AutoCloseable {
    private static final Pattern MC_VERSION = Pattern.compile("\"mcversion\"\\s*:\\s*\"([^\"]+)\"");
    private static final String ASSETS = "assets/thaumcraft/";

    final Path path;
    final LegacyEdition edition;
    private final ZipFile zip;

    private LegacyJar(Path path, LegacyEdition edition, ZipFile zip) {
        this.path = path;
        this.edition = edition;
        this.zip = zip;
    }

    static Optional<LegacyJar> open(Path path) throws IOException {
        ZipFile zip = new ZipFile(path.toFile());
        try {
            ZipEntry info = zip.getEntry("mcmod.info");
            if (info == null) {
                zip.close();
                return Optional.empty();
            }
            String text;
            try (InputStream in = zip.getInputStream(info)) {
                text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            Matcher matcher = MC_VERSION.matcher(text);
            Optional<LegacyEdition> edition = text.contains("\"Thaumcraft\"") && matcher.find()
                    ? LegacyEdition.byMinecraftVersion(matcher.group(1))
                    : Optional.empty();
            if (edition.isEmpty()) {
                zip.close();
                return Optional.empty();
            }
            return Optional.of(new LegacyJar(path, edition.get(), zip));
        } catch (IOException | RuntimeException e) {
            zip.close();
            throw e;
        }
    }

    Optional<byte[]> read(String path) throws IOException {
        ZipEntry entry = zip.getEntry(ASSETS + path);
        if (entry == null || entry.isDirectory()) {
            return Optional.empty();
        }
        try (InputStream in = zip.getInputStream(entry)) {
            return Optional.of(in.readAllBytes());
        }
    }

    Optional<byte[]> readClass(String internalName) throws IOException {
        ZipEntry entry = zip.getEntry(internalName + ".class");
        if (entry == null) {
            return Optional.empty();
        }
        try (InputStream in = zip.getInputStream(entry)) {
            return Optional.of(in.readAllBytes());
        }
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }
}
