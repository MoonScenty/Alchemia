package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

/**
 * The slots the baubles are worn in, as the original's Baubles gave a player: an amulet's, two rings', a belt's.
 * They are Curios' own slots by those names, so anything else worn in them sits beside these.
 */
public class ModCuriosProvider extends CuriosDataProvider {
    public ModCuriosProvider(PackOutput output, ExistingFileHelper files,
            CompletableFuture<HolderLookup.Provider> registries) {
        super(Alchemia.MODID, output, files, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper files) {
        createSlot("necklace");
        createSlot("ring").size(2);
        createSlot("belt");
        createEntities("baubles").addPlayer().addSlots("necklace", "ring", "belt");
    }
}
