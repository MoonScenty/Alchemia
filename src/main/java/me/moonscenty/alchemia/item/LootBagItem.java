package me.moonscenty.alchemia.item;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * A bag of loot, common, uncommon or rare, as a champion leaves or a chest holds. Opened, it spills eight to twelve
 * things at its owner's feet, picked from the original's tables: coins above all, then gold and ender pearls, gems,
 * fragments of knowledge, enchanted books, potions, golden apples, bottles of experience -- and in the rare bag, once
 * in a long while, a nether star or a primordial pearl.
 * <p>
 * The original's bags also held its trinkets -- amulets, rings and girdles -- which this mod does not have yet.
 */
public class LootBagItem extends Item {
    private final int rarity;

    public LootBagItem(Properties properties, int rarity) {
        super(properties);
        this.rarity = rarity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        if (level instanceof ServerLevel served) {
            int count = 8 + level.random.nextInt(5);
            for (int i = 0; i < count; i++) {
                ItemStack thing = loot(served, rarity, level.random);
                if (!thing.isEmpty()) {
                    level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), thing));
                }
            }
            level.playSound(null, player.blockPosition(), ModSounds.COINS.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
            bag.consume(1, player);
        }
        return InteractionResultHolder.sidedSuccess(bag, level.isClientSide);
    }

    /** The bag a champion leaves: common or uncommon, rare only with looting. */
    public static ItemStack forChampion(RandomSource random, int looting) {
        int rarity = Math.min(2, (random.nextInt(9) + looting) / 5);
        return new ItemStack(bag(rarity));
    }

    public static Item bag(int rarity) {
        return switch (rarity) {
            case 2 -> ModItems.LOOT_BAG_RARE.get();
            case 1 -> ModItems.LOOT_BAG_UNCOMMON.get();
            default -> ModItems.LOOT_BAG_COMMON.get();
        };
    }

    /** One thing out of a bag of this rarity. */
    public static ItemStack loot(ServerLevel level, int rarity, RandomSource random) {
        List<ItemStack> things = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        add(things, weights, new ItemStack(ModItems.COIN.get(), rarity + 1), new int[] {2500, 2250, 2000}[rarity]);
        if (rarity == 2) {
            add(things, weights, new ItemStack(ModItems.PRIMORDIAL_PEARL.get()), 1);
            add(things, weights, new ItemStack(Items.NETHER_STAR), 1);
        }
        add(things, weights, new ItemStack(Items.DIAMOND), rarity == 0 ? 10 : 50);
        add(things, weights, new ItemStack(Items.EMERALD), rarity == 0 ? 15 : 75);
        add(things, weights, new ItemStack(Items.GOLD_INGOT), 100);
        add(things, weights, new ItemStack(Items.ENDER_PEARL), 100);
        add(things, weights, new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get()), 25);
        add(things, weights, new ItemStack(Items.EXPERIENCE_BOTTLE), new int[] {5, 10, 20}[rarity]);
        add(things, weights, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), rarity + 1);
        add(things, weights, new ItemStack(Items.GOLDEN_APPLE), 3 * (rarity + 1));
        add(things, weights, new ItemStack(Items.BOOK), 10);
        if (rarity == 0) {
            add(things, weights, new ItemStack(ModItems.MUNDANE_AMULET.get()), 10);
            add(things, weights, new ItemStack(ModItems.MUNDANE_RING.get()), 10);
            add(things, weights, new ItemStack(ModItems.MUNDANE_BELT.get()), 10);
        } else {
            for (var ring : ModItems.APPRENTICE_RINGS.values()) {
                add(things, weights, new ItemStack(ring.get()), rarity == 1 ? 5 : 7);
            }
            add(things, weights, new ItemStack(ModItems.VIS_STONE.get()), 6);
            add(things, weights, new ItemStack(ModItems.PROTECTION_RING.get()), 5);
        }
        // every drinkable and splash potion there is, the stronger and longer ones weighted higher
        for (Holder.Reference<Potion> potion : BuiltInRegistries.POTION.holders().toList()) {
            if (potion.value().getEffects().isEmpty()) {
                continue;
            }
            String name = potion.key().location().getPath();
            int weight = name.startsWith("strong_") ? 2 : name.startsWith("long_") ? 3 : 1;
            add(things, weights, PotionContents.createItemStack(Items.POTION, potion), weight);
            add(things, weights, PotionContents.createItemStack(Items.SPLASH_POTION, potion), weight);
        }
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(total);
        ItemStack picked = ItemStack.EMPTY;
        for (int i = 0; i < things.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                picked = things.get(i).copy();
                break;
            }
        }
        if (picked.is(Items.BOOK)) {
            picked = EnchantmentHelper.enchantItem(random, picked, (int) (5 + rarity * 0.75F * random.nextInt(18)),
                    level.registryAccess(), level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                            .getTag(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE));
        }
        return picked;
    }

    private static void add(List<ItemStack> things, List<Integer> weights, ItemStack thing, int weight) {
        things.add(thing);
        weights.add(weight);
    }
}
