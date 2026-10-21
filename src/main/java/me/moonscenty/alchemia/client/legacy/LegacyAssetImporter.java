package me.moonscenty.alchemia.client.legacy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.client.legacy.model.LegacyModel;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelReader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * Reads the original's jars from the game's {@code mods} folder and offers what {@link LegacyAssets} asks for
 * as a resource pack sitting above our own files.
 *
 * <p>The jars go in {@code mods} beside this mod because that is where a player puts mods. The loader sees that they
 * are for an old Forge, says so in the log and passes over them; nothing of them is loaded but what is read here.
 *
 * <p>The original is All Rights Reserved, so its pictures never go into this repository. The player brings the jar;
 * we only know where in it to look. Both releases are required: while {@link #missingJars()} finds one missing, the
 * game shows where to download it instead of its title. Our own pictures stay underneath, for anything the jars do
 * not have.
 *
 * <p>The import runs every time the pack list is rebuilt, so dropping a jar in and pressing F3+T is enough.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class LegacyAssetImporter {
    private static final String PACK_ID = Alchemia.MODID + "/legacy";
    /**
     * Stands in for a missing {@code .mcmeta}. The game looks for a picture's metadata in the pack that served it and
     * then in every pack below, so without this an imported still picture would pick up our own animation strip's
     * frame list.
     */
    private static final byte[] NO_META = "{}".getBytes(StandardCharsets.UTF_8);

    private LegacyAssetImporter() {
    }

    @SubscribeEvent
    public static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        event.addRepositorySource(packs -> {
            Map<String, byte[]> files = importAll(folder());
            if (files.isEmpty()) {
                return;
            }
            PackLocationInfo location = new PackLocationInfo(PACK_ID,
                    Component.translatable("pack.alchemia.legacy.title"), PackSource.BUILT_IN, Optional.empty());
            Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
                @Override
                public PackResources openPrimary(PackLocationInfo info) {
                    return new LegacyAssetPack(info, files);
                }

                @Override
                public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                    return new LegacyAssetPack(info, files);
                }
            };
            // required, so it is on without anyone ticking it; not fixed, so a player's own pack can still go above
            Pack pack = Pack.readMetaAndCreate(location, resources, PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(true, Pack.Position.TOP, false));
            if (pack != null) {
                packs.accept(pack);
            }
        });
    }

    /** Our files by their path under {@code assets/alchemia/}; empty when there is nothing to import. */
    static Map<String, byte[]> importAll(Path folder) {
        Map<String, byte[]> files = new HashMap<>();
        // whatever happens below, last time's models are not this time's
        LegacyModels.replace(Map.of(), Map.of());
        if (!Files.isDirectory(folder)) {
            try {
                // made so a player can see where the jars go
                Files.createDirectories(folder);
            } catch (IOException e) {
                Alchemia.LOGGER.debug("Could not make {}", folder, e);
            }
            return files;
        }
        Map<LegacyEdition, LegacyJar> jars = new EnumMap<>(LegacyEdition.class);
        try {
            openJars(folder, jars);
            if (jars.isEmpty()) {
                Alchemia.LOGGER.info("No jar of the original in {}; using our own pictures", folder);
                return files;
            }
            Map<String, LegacyModel> models = importModels(jars);
            Map<LegacyEdition, Integer> taken = new EnumMap<>(LegacyEdition.class);
            List<String> missing = new ArrayList<>();
            for (LegacyAsset asset : LegacyAssets.ALL) {
                if (asset.model() != null && !models.containsKey(asset.model())) {
                    missing.add(asset.target());
                    continue;
                }
                Optional<LegacyEdition> from = importOne(asset, jars, files);
                if (from.isPresent()) {
                    taken.merge(from.get(), 1, Integer::sum);
                } else {
                    missing.add(asset.target());
                }
            }
            LegacyModels.replace(models, readMeshes(files));
            Alchemia.LOGGER.info("Imported {} of {} files from the original {}", LegacyAssets.ALL.size() - missing.size(),
                    LegacyAssets.ALL.size(), taken);
            if (!missing.isEmpty()) {
                Alchemia.LOGGER.info("Not in the jars we have, so ours stay: {}", missing);
            }
        } finally {
            for (LegacyJar jar : jars.values()) {
                try {
                    jar.close();
                } catch (IOException e) {
                    Alchemia.LOGGER.debug("Could not close {}", jar.path, e);
                }
            }
        }
        return files;
    }

    /** Reads the meshes that came over as OBJ files, so what draws them need not go looking for them again. */
    private static Map<String, LegacyMesh> readMeshes(Map<String, byte[]> files) {
        Map<String, LegacyMesh> meshes = new HashMap<>();
        files.forEach((path, bytes) -> {
            if (!path.endsWith(".obj")) {
                return;
            }
            try {
                meshes.put(path, LegacyMesh.parse(new String(bytes, StandardCharsets.UTF_8)));
            } catch (RuntimeException e) {
                Alchemia.LOGGER.warn("Could not read the mesh {}", path, e);
            }
        });
        return meshes;
    }

    /** Reads every model the original wrote as code; the first release whose jar has the class wins. */
    private static Map<String, LegacyModel> importModels(Map<LegacyEdition, LegacyJar> jars) {
        Map<String, LegacyModel> models = new HashMap<>();
        LegacyAssets.MODELS.forEach((key, sources) -> {
            for (LegacyModelSource source : sources) {
                LegacyJar jar = jars.get(source.edition());
                if (jar == null) {
                    continue;
                }
                try {
                    LegacyModelReader.Result read = LegacyModelReader.read(jar::readClass, source.className(),
                            source.descriptor(), source.arguments());
                    if (!read.notes().isEmpty()) {
                        Alchemia.LOGGER.warn("Read {} from {} with guesses: {}", key, source.edition(), read.notes());
                    }
                    models.put(key, read.model());
                    Alchemia.LOGGER.info("Read the {} model out of the original's {} code", key, source.edition());
                    return;
                } catch (IOException | RuntimeException e) {
                    Alchemia.LOGGER.warn("Could not read the {} model from {}", key, source.edition(), e);
                }
            }
        });
        return models;
    }

    /** Whether a file is one of the original's jars, either release, told by what it says it is inside. */
    public static boolean isOriginal(Path path) {
        try {
            Optional<LegacyJar> opened = LegacyJar.open(path);
            if (opened.isPresent()) {
                opened.get().close();
                return true;
            }
        } catch (IOException e) {
            Alchemia.LOGGER.debug("Could not open {}", path, e);
        }
        return false;
    }

    /** Where the jars go: the mods folder, with every other mod. */
    public static Path folder() {
        return FMLPaths.MODSDIR.get();
    }

    /**
     * Which releases of the original are not in the folder. The mod draws with their pictures and models and is not
     * meant to be played without them; the game shows where to get them instead of its title while any is missing.
     * The folder is made if it is not there, so a player can see where the jars go.
     *
     * <p>A jar is told apart by what it says it is inside, not by its name, as for the import itself.
     */
    public static Set<LegacyEdition> missingJars() {
        Path folder = folder();
        Set<LegacyEdition> found = EnumSet.noneOf(LegacyEdition.class);
        if (Files.isDirectory(folder)) {
            Map<LegacyEdition, LegacyJar> jars = new EnumMap<>(LegacyEdition.class);
            openJars(folder, jars);
            found.addAll(jars.keySet());
            for (LegacyJar jar : jars.values()) {
                try {
                    jar.close();
                } catch (IOException e) {
                    Alchemia.LOGGER.debug("Could not close {}", jar.path, e);
                }
            }
        } else {
            try {
                // made so a player can see where the jars go
                Files.createDirectories(folder);
            } catch (IOException e) {
                Alchemia.LOGGER.debug("Could not make {}", folder, e);
            }
        }
        Set<LegacyEdition> missing = EnumSet.allOf(LegacyEdition.class);
        missing.removeAll(found);
        for (LegacyEdition edition : missing) {
            Alchemia.LOGGER.warn("Missing the original's jar in {}: {}, from {}", folder.toAbsolutePath(),
                    edition.describe(), edition.downloadPage());
        }
        return missing;
    }

    private static void openJars(Path folder, Map<LegacyEdition, LegacyJar> jars) {
        List<Path> paths;
        try (Stream<Path> list = Files.list(folder)) {
            paths = list.filter(path -> path.getFileName().toString().endsWith(".jar")).sorted().toList();
        } catch (IOException e) {
            Alchemia.LOGGER.warn("Could not read {}", folder, e);
            return;
        }
        for (Path path : paths) {
            try {
                Optional<LegacyJar> opened = LegacyJar.open(path);
                if (opened.isEmpty()) {
                    // every other mod in the folder, which is no business of ours
                    Alchemia.LOGGER.debug("{} is not a jar of the original; skipped", path.getFileName());
                    continue;
                }
                LegacyJar jar = opened.get();
                if (jars.containsKey(jar.edition)) {
                    Alchemia.LOGGER.warn("{} is a second jar of {}; skipped", path.getFileName(), jar.edition);
                    jar.close();
                    continue;
                }
                jars.put(jar.edition, jar);
                Alchemia.LOGGER.info("Found the original's {} release: {}", jar.edition, path.getFileName());
            } catch (IOException e) {
                Alchemia.LOGGER.warn("Could not open {}", path.getFileName(), e);
            }
        }
    }

    /** Takes the first source a jar has; empty when none of them does or the file could not be read. */
    private static Optional<LegacyEdition> importOne(LegacyAsset asset, Map<LegacyEdition, LegacyJar> jars,
            Map<String, byte[]> files) {
        for (LegacyAsset.Source source : asset.sources()) {
            LegacyJar jar = jars.get(source.edition());
            if (jar == null) {
                continue;
            }
            try {
                Optional<byte[]> bytes = jar.read(source.path());
                if (bytes.isEmpty()) {
                    continue;
                }
                files.put(asset.target(), asset.transform().apply(bytes.get()));
                if (asset.target().endsWith(".png")) {
                    files.put(asset.target() + ".mcmeta", jar.read(source.path() + ".mcmeta").orElse(NO_META));
                }
                return Optional.of(source.edition());
            } catch (IOException | RuntimeException | OutOfMemoryError e) {
                // a picture too big to take in is left to ours, rather than taking the game down with it
                Alchemia.LOGGER.warn("Could not import {} from {} for {}", source.path(), source.edition(),
                        asset.target(), e);
            }
        }
        return Optional.empty();
    }
}
