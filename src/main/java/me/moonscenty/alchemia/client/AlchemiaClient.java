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
        // the game is drawn with the original's own pictures and models, so it does not start without both jars.
        // Writing the data files draws nothing and needs neither
        if (!DatagenModLoader.isRunningDataGen()) {
            LegacyAssetImporter.requireJars();
        }
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
