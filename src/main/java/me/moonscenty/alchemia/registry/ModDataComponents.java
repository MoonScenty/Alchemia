package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.research.ResearchNote;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandRod;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
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

    /** The shaft a wand is built on, which says how much it can hold. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<WandRod>>> WAND_ROD =
            COMPONENTS.register("wand_rod", () -> DataComponentType.<Holder<WandRod>>builder()
                    .persistent(ModWandParts.RODS.holderByNameCodec())
                    .networkSynchronized(ByteBufCodecs.holderRegistry(ModWandParts.RODS_KEY))
                    .build());

    /** The metal at its ends, which says what it charges and how fast it fills. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<WandCap>>> WAND_CAP =
            COMPONENTS.register("wand_cap", () -> DataComponentType.<Holder<WandCap>>builder()
                    .persistent(ModWandParts.CAPS.holderByNameCodec())
                    .networkSynchronized(ByteBufCodecs.holderRegistry(ModWandParts.CAPS_KEY))
                    .build());

    /** What a wand is carrying, in hundredths of a point so that a cap's discount is not rounded away. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AspectList>> VIS =
            COMPONENTS.register("vis", () -> DataComponentType.<AspectList>builder()
                    .persistent(AspectList.CODEC)
                    .networkSynchronized(AspectList.STREAM_CODEC)
                    .build());

    private ModDataComponents() {
    }
}
