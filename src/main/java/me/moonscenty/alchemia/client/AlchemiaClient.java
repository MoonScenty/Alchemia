package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssetImporter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

@Mod(value = Alchemia.MODID, dist = Dist.CLIENT)
public class AlchemiaClient {
    public AlchemiaClient(ModContainer container) {
        if (!DatagenModLoader.isRunningDataGen()) {
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
