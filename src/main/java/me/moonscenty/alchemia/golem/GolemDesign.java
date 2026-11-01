package me.moonscenty.alchemia.golem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import me.moonscenty.alchemia.golem.GolemParts.Addon;
import me.moonscenty.alchemia.golem.GolemParts.Arms;
import me.moonscenty.alchemia.golem.GolemParts.Head;
import me.moonscenty.alchemia.golem.GolemParts.Legs;
import me.moonscenty.alchemia.golem.GolemParts.Material;
import me.moonscenty.alchemia.golem.GolemParts.Need;
import me.moonscenty.alchemia.golem.GolemParts.Of;
import me.moonscenty.alchemia.golem.GolemParts.Part;
import me.moonscenty.alchemia.golem.GolemParts.Stack;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// Packs into one long, a byte per slot, as the original's.
public record GolemDesign(Material material, Head head, Arms arms, Legs legs, Addon addon, int rank) {
    public static final GolemDesign BASIC = fromLong(0L);
    public static final int TOP_RANK = 10;
    // times the square of the rank being reached
    public static final int RANK_XP = 1000;

    public static final Codec<GolemDesign> CODEC = Codec.LONG.xmap(GolemDesign::fromLong, GolemDesign::toLong);
    public static final StreamCodec<ByteBuf, GolemDesign> STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(GolemDesign::fromLong, GolemDesign::toLong);

    public static GolemDesign fromLong(long packed) {
        return new GolemDesign(pick(Material.values(), packed, 0), pick(Head.values(), packed, 1),
                pick(Arms.values(), packed, 2), pick(Legs.values(), packed, 3), pick(Addon.values(), packed, 4),
                Math.clamp(slot(packed, 5), 0, TOP_RANK));
    }

    private static int slot(long packed, int slot) {
        return (int) (packed >>> (slot * 8) & 0xFF);
    }

    // unknown ids fall back to the first value
    private static <T> T pick(T[] values, long packed, int slot) {
        int at = slot(packed, slot);
        return at < values.length ? values[at] : values[0];
    }

    public long toLong() {
        return material.ordinal() | (long) head.ordinal() << 8 | (long) arms.ordinal() << 16
                | (long) legs.ordinal() << 24 | (long) addon.ordinal() << 32 | (long) rank << 40;
    }

    public GolemDesign withMaterial(Material material) {
        return new GolemDesign(material, head, arms, legs, addon, rank);
    }

    public GolemDesign withHead(Head head) {
        return new GolemDesign(material, head, arms, legs, addon, rank);
    }

    public GolemDesign withArms(Arms arms) {
        return new GolemDesign(material, head, arms, legs, addon, rank);
    }

    public GolemDesign withLegs(Legs legs) {
        return new GolemDesign(material, head, arms, legs, addon, rank);
    }

    public GolemDesign withAddon(Addon addon) {
        return new GolemDesign(material, head, arms, legs, addon, rank);
    }

    public GolemDesign withRank(int rank) {
        return new GolemDesign(material, head, arms, legs, addon, Math.clamp(rank, 0, TOP_RANK));
    }

    public List<Part> parts() {
        return List.of(material, head, arms, legs, addon);
    }

    // a trait and its opposite cancel; the original's part order decides which is left out
    public Set<GolemTrait> traits() {
        Set<GolemTrait> traits = EnumSet.noneOf(GolemTrait.class);
        for (Part part : parts()) {
            for (GolemTrait trait : part.traits()) {
                if (trait.opposite() != null && traits.contains(trait.opposite())) {
                    traits.remove(trait.opposite());
                } else {
                    traits.add(trait);
                }
            }
        }
        return Collections.unmodifiableSet(traits);
    }

    public boolean has(GolemTrait trait) {
        return traits().contains(trait);
    }

    public boolean known(Player player) {
        return parts().stream().allMatch(part -> part.known(player));
    }

    // in the original's listing order
    public List<ItemStack> components() {
        List<ItemStack> all = new ArrayList<>();
        for (Part part : List.of(material, arms, legs, head, addon)) {
            for (Need need : part.needs()) {
                ItemStack stack = switch (need) {
                    case Stack wanted -> new ItemStack(wanted.item().get(), wanted.count());
                    case Of of -> of == Of.BASE ? material.base() : material.mechanism();
                };
                add(all, stack);
            }
        }
        return all;
    }

    private static void add(List<ItemStack> all, ItemStack stack) {
        for (ItemStack already : all) {
            if (ItemStack.isSameItemSameComponents(already, stack)) {
                already.grow(stack.getCount());
                return;
            }
        }
        all.add(stack);
    }

    public int cost() {
        int cost = traits().size() * 2;
        for (ItemStack stack : components()) {
            cost += stack.getCount();
        }
        return cost;
    }

    public int maxHealth() {
        int health = 10 + material.health();
        if (has(GolemTrait.FRAGILE)) {
            health = (int) (health * 0.75);
        }
        return health + rank;
    }

    public int armour() {
        int armour = material.armour();
        if (has(GolemTrait.ARMORED)) {
            armour = (int) Math.max(armour * 1.5, armour + 1);
        }
        if (has(GolemTrait.FRAGILE)) {
            armour = (int) (armour * 0.75);
        }
        return armour;
    }

    public double damage() {
        if (!has(GolemTrait.FIGHTER)) {
            return 0.0;
        }
        double damage = material.damage();
        if (has(GolemTrait.BRUTAL)) {
            damage = Math.max(damage * 1.5, damage + 1.0);
        }
        return damage + rank * 0.25;
    }

    public float speed() {
        return 1.0F + rank * 0.025F
                + (has(GolemTrait.LIGHT) ? 0.2F : 0.0F)
                + (has(GolemTrait.HEAVY) ? -0.175F : 0.0F)
                + (has(GolemTrait.FLYER) ? -0.33F : 0.0F)
                + (has(GolemTrait.WHEELED) ? 0.25F : 0.0F);
    }

    public int reach() {
        return has(GolemTrait.SCOUT) ? 32 : 16;
    }

    public int xpToNextRank() {
        return (rank + 1) * (rank + 1) * RANK_XP;
    }
}
