package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssetImporter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Alchemia.MODID, dist = Dist.CLIENT)
public class AlchemiaClient {
    public AlchemiaClient(ModContainer container) {
        // the game is drawn with the original's own pictures and models, so without both jars it shows where to get
        // them in place of its title, and goes no further. Writing the data files draws nothing and needs neither
        if (!DatagenModLoader.isRunningDataGen()) {
            // the loader warns about the original's jars sitting in the mods folder; that is where they belong
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(OriginalJarWarnings::onOpening);
            java.util.Set<me.moonscenty.alchemia.client.legacy.LegacyEdition> missing =
                    LegacyAssetImporter.missingJars();
            if (!missing.isEmpty()) {
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                        (net.neoforged.neoforge.client.event.ScreenEvent.Opening event) -> {
                            if (event.getNewScreen() instanceof net.minecraft.client.gui.screens.TitleScreen) {
                                event.setNewScreen(new MissingJarsScreen(missing, LegacyAssetImporter.folder()));
                            }
                        });
            }
        }
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
