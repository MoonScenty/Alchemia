package me.moonscenty.alchemia.registry;

import java.util.Map;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.item.HelmFitting;
import me.moonscenty.alchemia.research.ResearchNote;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandRod;
import net.minecraft.core.Holder;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Alchemia.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<FocusUpgrade>>>
            FOCUS_UPGRADES = COMPONENTS.register("focus_upgrades",
                    () -> DataComponentType.<java.util.List<FocusUpgrade>>builder()
                            .persistent(FocusUpgrade.CODEC.listOf())
                            .networkSynchronized(FocusUpgrade.STREAM_CODEC
                                    .apply(ByteBufCodecs.list()).cast())
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.minecraft.core.GlobalPos>>
            MIRROR_LINK = COMPONENTS.register("mirror_link", () -> DataComponentType.<net.minecraft.core.GlobalPos>builder()
                    .persistent(net.minecraft.core.GlobalPos.CODEC)
                    .networkSynchronized(net.minecraft.core.GlobalPos.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>,
            DataComponentType<me.moonscenty.alchemia.golem.GolemDesign>> GOLEM_DESIGN = COMPONENTS.register(
                    "golem_design",
                    () -> DataComponentType.<me.moonscenty.alchemia.golem.GolemDesign>builder()
                            .persistent(me.moonscenty.alchemia.golem.GolemDesign.CODEC)
                            .networkSynchronized(me.moonscenty.alchemia.golem.GolemDesign.STREAM_CODEC)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GOLEM_XP =
            COMPONENTS.register("golem_xp", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> AURA_SHOWN =
            COMPONENTS.register("aura_shown", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> HARNESS_FUEL =
            COMPONENTS.register("harness_fuel", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> HOVERING =
            COMPONENTS.register("hovering", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WARP =
            COMPONENTS.register("warp", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_XP =
            COMPONENTS.register("stored_xp", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FOCUS_REACH =
            COMPONENTS.register("focus_reach", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SHOVEL_LAYING =
            COMPONENTS.register("shovel_laying", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 2))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResearchNote>> RESEARCH_NOTE =
            COMPONENTS.register("research_note", () -> DataComponentType.<ResearchNote>builder()
                    .persistent(ResearchNote.CODEC)
                    .networkSynchronized(ResearchNote.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<WandRod>>> WAND_ROD =
            COMPONENTS.register("wand_rod", () -> DataComponentType.<Holder<WandRod>>builder()
                    .persistent(ModWandParts.RODS.holderByNameCodec())
                    .networkSynchronized(ByteBufCodecs.holderRegistry(ModWandParts.RODS_KEY))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<WandCap>>> WAND_CAP =
            COMPONENTS.register("wand_cap", () -> DataComponentType.<Holder<WandCap>>builder()
                    .persistent(ModWandParts.CAPS.holderByNameCodec())
                    .networkSynchronized(ByteBufCodecs.holderRegistry(ModWandParts.CAPS_KEY))
                    .build());

    // ItemContainerContents, not ItemStack: a component must be immutable with value equality
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> WAND_FOCUS =
            COMPONENTS.register("wand_focus", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.minecraft.util.Unit>> SCEPTRE =
            COMPONENTS.register("sceptre", () -> DataComponentType.<net.minecraft.util.Unit>builder()
                    .persistent(net.minecraft.util.Unit.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.StreamCodec.unit(net.minecraft.util.Unit.INSTANCE))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> POUCH_CONTENTS =
            COMPONENTS.register("pouch_contents", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HelmFitting>> HELM_FITTING =
            COMPONENTS.register("helm_fitting", () -> DataComponentType.<HelmFitting>builder()
                    .persistent(HelmFitting.CODEC)
                    .networkSynchronized(HelmFitting.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Block>>> PICKED_BLOCK =
            COMPONENTS.register("picked_block", () -> DataComponentType.<Holder<Block>>builder()
                    .persistent(BuiltInRegistries.BLOCK.holderByNameCodec())
                    .networkSynchronized(ByteBufCodecs.holderRegistry(Registries.BLOCK))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AspectList>> VIS =
            COMPONENTS.register("vis", () -> DataComponentType.<AspectList>builder()
                    .persistent(AspectList.CODEC)
                    .networkSynchronized(AspectList.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Aspect>>> ESSENTIA =
            COMPONENTS.register("essentia", () -> DataComponentType.<Holder<Aspect>>builder()
                    .persistent(ModAspects.REGISTRY.holderByNameCodec())
                    .networkSynchronized(ByteBufCodecs.holderRegistry(ModAspects.KEY))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AspectList>> CONTENTS =
            COMPONENTS.register("contents", () -> DataComponentType.<AspectList>builder()
                    .persistent(AspectList.CODEC)
                    .networkSynchronized(AspectList.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<InfusionEnchantment, Integer>>>
            INFUSION_ENCHANTMENTS = COMPONENTS.register("infusion_enchantments",
                    () -> DataComponentType.<Map<InfusionEnchantment, Integer>>builder()
                            .persistent(Codec.unboundedMap(InfusionEnchantment.CODEC, Codec.INT))
                            .networkSynchronized(ByteBufCodecs.map(java.util.LinkedHashMap::new,
                                    InfusionEnchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT))
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RUNIC_HARDENING =
            COMPONENTS.register("runic_hardening", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 127)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> VERDANT_KIND =
            COMPONENTS.register("verdant_kind", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 2)).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    private ModDataComponents() {
    }
}
