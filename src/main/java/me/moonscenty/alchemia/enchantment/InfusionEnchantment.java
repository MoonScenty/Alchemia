package me.moonscenty.alchemia.enchantment;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum InfusionEnchantment implements StringRepresentable {
    COLLECTOR("collector", 1, () -> List.of(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS, ItemTags.SWORDS)),
    DESTRUCTIVE("destructive", 1, () -> List.of(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS)),
    BURROWING("burrowing", 1, () -> List.of(ItemTags.PICKAXES, ItemTags.AXES)),
    REFINING("refining", 4, () -> List.of(ItemTags.PICKAXES)),
    SOUNDING("sounding", 4, () -> List.of(ItemTags.PICKAXES)),
    ARCING("arcing", 4, () -> List.of(ItemTags.SWORDS, ItemTags.AXES)),
    ESSENCE("essence", 5, () -> List.of(ItemTags.SWORDS, ItemTags.AXES));

    public static final Codec<InfusionEnchantment> CODEC = StringRepresentable.fromEnum(InfusionEnchantment::values);
    public static final StreamCodec<io.netty.buffer.ByteBuf, InfusionEnchantment> STREAM_CODEC =
            ByteBufCodecs.idMapper(which -> values()[which], InfusionEnchantment::ordinal);

    private final String name;
    private final int most;
    // a supplier because tags can't be resolved while the enum is being built
    private final Supplier<List<TagKey<Item>>> goesOn;

    InfusionEnchantment(String name, int most, Supplier<List<TagKey<Item>>> goesOn) {
        this.name = name;
        this.most = most;
        this.goesOn = goesOn;
    }

    public int most() {
        return most;
    }

    public boolean fits(ItemStack stack) {
        return goesOn.get().stream().anyMatch(stack::is);
    }

    public List<TagKey<Item>> goesOn() {
        return goesOn.get();
    }

    public String key() {
        return "infusion_enchantment.alchemia." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
