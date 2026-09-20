package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.research.ResearchNote;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** What an item carries beyond being itself. */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Alchemia.MODID);

    /** The puzzle drawn on a sheet of research notes, and the research it stands for. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResearchNote>> RESEARCH_NOTE =
            COMPONENTS.register("research_note", () -> DataComponentType.<ResearchNote>builder()
                    .persistent(ResearchNote.CODEC)
                    .networkSynchronized(ResearchNote.STREAM_CODEC)
                    .build());

    private ModDataComponents() {
    }
}
