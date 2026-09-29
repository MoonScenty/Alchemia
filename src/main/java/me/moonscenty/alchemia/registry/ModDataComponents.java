package me.moonscenty.alchemia.registry;

import java.util.Map;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.research.ResearchNote;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandRod;
import net.minecraft.core.Holder;
import net.minecraft.world.item.component.ItemContainerContents;
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

    /**
     * The focus fitted to a wand, kept as the focus item itself rather than as which kind it is.
     * <p>
     * A focus is a thing a player owns: it came out of their bag, it goes back into their bag, and once upgrades
     * arrive two foci of the same kind will differ. Storing which kind it is would lose all of that, so what is
     * stored is the item. A wand with nothing fitted simply has no component.
     * <p>
     * It is held in a one-slot {@link ItemContainerContents} rather than as a bare {@code ItemStack}, because a
     * component has to be immutable and compare by what it holds, and an item stack is neither. This is the same
     * box vanilla puts a shulker box's contents in, with one thing in it.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> WAND_FOCUS =
            COMPONENTS.register("wand_focus", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .build());

    /** The eighteen squares of a focus pouch, kept on the item so that a pouch in a chest is still full. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> POUCH_CONTENTS =
            COMPONENTS.register("pouch_contents", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .build());

    /** What a wand is carrying, in hundredths of a point so that a cap's discount is not rounded away. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AspectList>> VIS =
            COMPONENTS.register("vis", () -> DataComponentType.<AspectList>builder()
                    .persistent(AspectList.CODEC)
                    .networkSynchronized(AspectList.STREAM_CODEC)
                    .build());

    /**
     * The one aspect a phial or a label names.
     * <p>
     * How much of it is not written down: a phial is always eight and a label is a word rather than a measure.
     * An item without this component is an empty phial or a blank label, which is also what makes the two models
     * easy to pick between.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Aspect>>> ESSENTIA =
            COMPONENTS.register("essentia", () -> DataComponentType.<Holder<Aspect>>builder()
                    .persistent(ModAspects.REGISTRY.holderByNameCodec())
                    .networkSynchronized(ByteBufCodecs.holderRegistry(ModAspects.KEY))
                    .build());

    /**
     * What a vessel taken up off the floor still has in it.
     * <p>
     * A warded jar is warded whether or not it is standing on anything, so breaking one hands back the jar with
     * its essentia still inside rather than letting the lot go into the air.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AspectList>> CONTENTS =
            COMPONENTS.register("contents", () -> DataComponentType.<AspectList>builder()
                    .persistent(AspectList.CODEC)
                    .networkSynchronized(AspectList.STREAM_CODEC)
                    .build());

    /**
     * What an altar has put on a tool, and how far.
     * <p>
     * A map rather than a list because a tool may carry several and never two of the same, and because working
     * the same one again raises what is already there rather than adding beside it.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<InfusionEnchantment, Integer>>>
            INFUSION_ENCHANTMENTS = COMPONENTS.register("infusion_enchantments",
                    () -> DataComponentType.<Map<InfusionEnchantment, Integer>>builder()
                            .persistent(Codec.unboundedMap(InfusionEnchantment.CODEC, Codec.INT))
                            .networkSynchronized(ByteBufCodecs.map(java.util.LinkedHashMap::new,
                                    InfusionEnchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT))
                            .build());

    private ModDataComponents() {
    }
}
