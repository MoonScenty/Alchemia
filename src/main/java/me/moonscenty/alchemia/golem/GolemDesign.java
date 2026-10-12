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

/**
 * One golem as it was pressed: its material and four parts, and how far a smart one has come on since.
 *
 * <p>It packs into a single number a byte to a slot, as the original's did, which is how it rides on the golem item
 * and on the golem itself without anything more to keep in step.
 */
public record GolemDesign(Material material, Head head, Arms arms, Legs legs, Addon addon, int rank) {
    /** The plainest golem: wood, a clockwork head, plain arms, walking legs, nothing fitted. */
    public static final GolemDesign BASIC = fromLong(0L);
    /** How far a smart golem can come on. */
    public static final int TOP_RANK = 10;
    /** What it takes to come on one rank, times the square of the rank it is reaching for. */
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

    /** A slot that names nothing, from some later version or a damaged item, comes back as the first. */
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

    /** The five things it is made of, in the order their traits are added. */
    public List<Part> parts() {
        return List.of(material, head, arms, legs, addon);
    }

    /**
     * What its parts make of it, a pair cancelling where one part brings what another takes away. The order is the
     * original's, which matters only for which of a cancelled pair is left out.
     */
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

    /** Whether somebody knows enough to press it. */
    public boolean known(Player player) {
        return parts().stream().allMatch(part -> part.known(player));
    }

    /** What the press takes for it, alike things counted together, in the order the original listed them. */
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

    /** How many measures of machina it takes to press: two for every trait, and one for every thing put in. */
    public int cost() {
        int cost = traits().size() * 2;
        for (ItemStack stack : components()) {
            cost += stack.getCount();
        }
        return cost;
    }

    /** Ten, and what its material adds, cut by a quarter if it is fragile, and one for each rank. */
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

    /** How hard it hits: nothing unless it fights, and a quarter more for every rank it has come on. */
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

    /** How fast it goes, against a plain golem's one. */
    public float speed() {
        return 1.0F + rank * 0.025F
                + (has(GolemTrait.LIGHT) ? 0.2F : 0.0F)
                + (has(GolemTrait.HEAVY) ? -0.175F : 0.0F)
                + (has(GolemTrait.FLYER) ? -0.33F : 0.0F)
                + (has(GolemTrait.WHEELED) ? 0.25F : 0.0F);
    }

    /** How far from home it will go about its work. */
    public int reach() {
        return has(GolemTrait.SCOUT) ? 32 : 16;
    }

    /** How much it has to learn before it comes on another rank. */
    public int xpToNextRank() {
        return (rank + 1) * (rank + 1) * RANK_XP;
    }
}
