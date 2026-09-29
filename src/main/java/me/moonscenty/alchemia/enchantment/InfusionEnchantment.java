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

/**
 * What an altar can put on a tool that an enchanting table cannot.
 * <p>
 * These are not enchantments as the game knows them. They have no levels to buy, no table to gamble at and no
 * book to store them in; the only way to get one is to lay the tool under a matrix and pay for it in essentia.
 * That is the whole of the difference, and it is why they are kept apart rather than registered alongside the
 * vanilla ones: a thing that cannot be bought should not sit in the same list as the things that can.
 * <p>
 * What each will go on is a list of tags rather than a list of items, so that a tool added by anything else is
 * covered by the same rule. The original asked the tool for its tool classes, which is the same question put the
 * way that version of the game could answer it.
 */
public enum InfusionEnchantment implements StringRepresentable {
    /** What is broken goes to the one who broke it, rather than onto the floor. */
    COLLECTOR("collector", 1, () -> List.of(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS, ItemTags.SWORDS)),
    /** The eight blocks round the one struck go with it, where the tool would have served for them. */
    DESTRUCTIVE("destructive", 1, () -> List.of(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS));

    public static final Codec<InfusionEnchantment> CODEC = StringRepresentable.fromEnum(InfusionEnchantment::values);
    public static final StreamCodec<io.netty.buffer.ByteBuf, InfusionEnchantment> STREAM_CODEC =
            ByteBufCodecs.idMapper(which -> values()[which], InfusionEnchantment::ordinal);

    private final String name;
    private final int most;
    /** Held as a supplier because tags cannot be looked at while the enum is still being built. */
    private final Supplier<List<TagKey<Item>>> goesOn;

    InfusionEnchantment(String name, int most, Supplier<List<TagKey<Item>>> goesOn) {
        this.name = name;
        this.most = most;
        this.goesOn = goesOn;
    }

    /** How far this one can be taken. Working the same thing again past this does nothing. */
    public int most() {
        return most;
    }

    /** Whether this will go on that at all, whatever is already on it. */
    public boolean fits(ItemStack stack) {
        return goesOn.get().stream().anyMatch(stack::is);
    }

    /** What it will go on, for a recipe to name the same set of tools the working itself accepts. */
    public List<TagKey<Item>> goesOn() {
        return goesOn.get();
    }

    /** The key its name and its line of description are written under. */
    public String key() {
        return "infusion_enchantment.alchemia." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
