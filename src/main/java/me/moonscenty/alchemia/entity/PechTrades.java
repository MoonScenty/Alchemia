package me.moonscenty.alchemia.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

// Each pech kind's trade list is the original's.
public final class PechTrades {
    public record Lot(int worth, Function<Level, ItemStack> makes) {
    }

    private static final List<List<Lot>> LISTS = List.of(forager(), mage(), stalker());

    private PechTrades() {
    }

    public static int value(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(Items.GOLD_INGOT) || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            return 2;
        }
        if (stack.is(Items.ENDER_PEARL)) {
            return 3;
        }
        if (stack.is(Items.DIAMOND)) {
            return 4;
        }
        if (stack.is(Items.EMERALD)) {
            return 5;
        }
        int desire = Aspects.of(stack).get(ModAspects.DESIRE);
        return desire > 1 ? Math.min(32, desire) : 0;
    }

    public static boolean valued(ItemStack stack) {
        return value(stack) > 0;
    }

    public static ItemStack lot(Level level, int kind, int worth, RandomSource random) {
        List<Lot> list = LISTS.get(Math.floorMod(kind, LISTS.size()));
        int nearest = list.getFirst().worth();
        for (Lot lot : list) {
            if (Math.abs(lot.worth() - worth) < Math.abs(nearest - worth)
                    || Math.abs(lot.worth() - worth) == Math.abs(nearest - worth) && lot.worth() < nearest) {
                nearest = lot.worth();
            }
        }
        List<Lot> of = new ArrayList<>();
        for (Lot lot : list) {
            if (lot.worth() == nearest) {
                of.add(lot);
            }
        }
        return of.get(random.nextInt(of.size())).makes().apply(level);
    }

    public static List<Lot> list(int kind) {
        return LISTS.get(Math.floorMod(kind, LISTS.size()));
    }

    private static List<Lot> forager() {
        List<Lot> lots = new ArrayList<>();
        lots.add(item(1, ModItems.IRON_CLUSTER.get()));
        lots.add(item(1, ModItems.GOLD_CLUSTER.get()));
        lots.add(item(1, ModItems.CINNABAR_CLUSTER.get()));
        lots.add(item(1, ModItems.COPPER_CLUSTER.get()));
        lots.add(item(2, Items.BLAZE_ROD));
        lots.add(item(2, ModBlocks.GREATWOOD.sapling().get().asItem()));
        lots.add(potion(2, Potions.STRENGTH));
        lots.add(potion(2, Potions.SWIFTNESS));
        lots.add(item(3, Items.EXPERIENCE_BOTTLE));
        lots.add(item(3, Items.EXPERIENCE_BOTTLE));
        lots.add(item(3, ModItems.KNOWLEDGE_FRAGMENT.get()));
        lots.add(item(3, Items.GOLDEN_APPLE));
        lots.add(potion(3, Potions.LONG_STRENGTH));
        lots.add(potion(3, Potions.LONG_NIGHT_VISION));
        lots.add(item(4, ModItems.METAL_TOOLS.get("thaumium_pickaxe").get()));
        lots.add(item(4, ModItems.METAL_TOOLS.get("thaumium_axe").get()));
        lots.add(item(4, ModItems.METAL_TOOLS.get("thaumium_hoe").get()));
        lots.add(item(5, Items.ENCHANTED_GOLDEN_APPLE));
        lots.add(item(5, ModBlocks.SILVERWOOD.sapling().get().asItem()));
        return List.copyOf(lots);
    }

    private static List<Lot> mage() {
        List<Lot> lots = new ArrayList<>();
        for (CrystalType type : new CrystalType[] {CrystalType.AIR, CrystalType.FIRE, CrystalType.WATER,
                CrystalType.EARTH, CrystalType.ORDER, CrystalType.ENTROPY}) {
            lots.add(item(1, ModItems.SHARDS.get(type).get()));
        }
        lots.add(item(1, ModItems.KNOWLEDGE_FRAGMENT.get()));
        lots.add(item(2, ModItems.KNOWLEDGE_FRAGMENT.get()));
        lots.add(potion(2, Potions.REGENERATION));
        lots.add(potion(2, Potions.HEALING));
        lots.add(item(3, Items.EXPERIENCE_BOTTLE));
        lots.add(item(3, Items.EXPERIENCE_BOTTLE));
        lots.add(book(3, me.moonscenty.alchemia.enchantment.ModEnchantments.HASTE));
        lots.add(item(3, Items.GOLDEN_APPLE));
        lots.add(potion(3, Potions.STRONG_REGENERATION));
        lots.add(potion(3, Potions.STRONG_HEALING));
        lots.add(item(4, ModItems.ROBES.get("cloth_boots").get()));
        lots.add(item(4, ModItems.ROBES.get("cloth_chest").get()));
        lots.add(item(4, ModItems.ROBES.get("cloth_legs").get()));
        lots.add(item(5, Items.ENCHANTED_GOLDEN_APPLE));
        lots.add(book(5, me.moonscenty.alchemia.enchantment.ModEnchantments.REPAIR));
        lots.add(item(5, ModItems.FOCI.get("pech").get()));
        lots.add(item(5, ModItems.VIS_STONE.get()));
        return List.copyOf(lots);
    }

    private static List<Lot> stalker() {
        List<Lot> lots = new ArrayList<>();
        for (net.minecraft.world.item.DyeColor colour : net.minecraft.world.item.DyeColor.values()) {
            if (colour != net.minecraft.world.item.DyeColor.BLACK) {
                lots.add(item(1, me.moonscenty.alchemia.registry.ModBlocks.TALLOW_CANDLES.get(colour).get().asItem()));
            }
        }
        for (var arrow : ModItems.PRIMAL_ARROWS.values()) {
            lots.add(new Lot(2, level -> new ItemStack(arrow.get(), 8)));
        }
        lots.add(item(2, Items.GHAST_TEAR));
        lots.add(potion(2, Potions.SWIFTNESS));
        lots.add(potion(2, Potions.STRENGTH));
        lots.add(book(2, Enchantments.POWER));
        lots.add(item(3, Items.EXPERIENCE_BOTTLE));
        lots.add(item(3, Items.EXPERIENCE_BOTTLE));
        lots.add(item(3, ModItems.KNOWLEDGE_FRAGMENT.get()));
        lots.add(item(4, ModItems.BONE_BOW.get()));
        lots.add(potion(3, Potions.LONG_INVISIBILITY));
        lots.add(potion(3, Potions.STRONG_REGENERATION));
        lots.add(item(3, Items.GOLDEN_APPLE));
        lots.add(item(5, Items.ENCHANTED_GOLDEN_APPLE));
        lots.add(book(5, Enchantments.FLAME));
        lots.add(book(5, Enchantments.INFINITY));
        lots.add(item(5, ModItems.PROTECTION_RING.get()));
        return List.copyOf(lots);
    }

    private static Lot item(int worth, Item item) {
        return new Lot(worth, level -> new ItemStack(item));
    }

    private static Lot potion(int worth, Holder<Potion> potion) {
        return new Lot(worth, level -> PotionContents.createItemStack(Items.POTION, potion));
    }

    private static Lot book(int worth, ResourceKey<Enchantment> enchantment) {
        return new Lot(worth, level -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(enchantment), 1)));
    }
}
