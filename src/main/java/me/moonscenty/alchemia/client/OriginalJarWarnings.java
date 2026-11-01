package me.moonscenty.alchemia.client;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssetImporter;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.LoadingErrorScreen;

// the warning screen's lists are private and read by name; if renamed, the screen just shows
public final class OriginalJarWarnings {
    private OriginalJarWarnings() {
    }

    public static void onOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof LoadingErrorScreen screen)) {
            return;
        }
        try {
            List<?> errors = (List<?>) field("modLoadErrors").get(screen);
            List<?> warnings = (List<?>) field("modLoadWarnings").get(screen);
            Runnable next = (Runnable) field("nextScreenTask").get(screen);
            if (!errors.isEmpty() || warnings.isEmpty() || next == null) {
                return;
            }
            for (Object formatted : warnings) {
                var read = formatted.getClass().getDeclaredMethod("issue");
                read.setAccessible(true);
                ModLoadingIssue issue = (ModLoadingIssue) read.invoke(formatted);
                if (!aboutAnOriginal(issue)) {
                    return;
                }
            }
            Alchemia.LOGGER.info("Passing by the loader's warnings about the original Thaumcraft jars");
            event.setCanceled(true);
            Minecraft.getInstance().tell(next);
        } catch (ReflectiveOperationException | ClassCastException e) {
            Alchemia.LOGGER.debug("Could not read the loader's warning screen; leaving it be", e);
        }
    }

    private static boolean aboutAnOriginal(ModLoadingIssue issue) {
        if (issue.severity() != ModLoadingIssue.Severity.WARNING) {
            return false;
        }
        Path path = issue.affectedPath();
        if (path == null) {
            for (Object arg : issue.translationArgs()) {
                if (arg instanceof Path given) {
                    path = given;
                } else if (arg instanceof java.io.File file) {
                    path = file.toPath();
                } else if (arg instanceof String named && named.endsWith(".jar")) {
                    path = Path.of(named);
                }
            }
        }
        return path != null && LegacyAssetImporter.isOriginal(path);
    }

    private static Field field(String name) throws NoSuchFieldException {
        Field field = LoadingErrorScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
