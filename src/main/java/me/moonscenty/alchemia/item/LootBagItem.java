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

public class LootBagItem extends Item {
    private static final float GEAR_PER_RARITY = 0.025F;

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

    public static ItemStack loot(ServerLevel level, int rarity, RandomSource random) {
        if (rarity > 0 && random.nextFloat() < GEAR_PER_RARITY * rarity) {
            return gear(level, rarity, random);
        }
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
        return picked.is(Items.BOOK) ? enchanted(level, picked, rarity, random) : picked;
    }

    private static ItemStack enchanted(ServerLevel level, ItemStack stack, int rarity, RandomSource random) {
        return EnchantmentHelper.enchantItem(random, stack, (int) (5 + rarity * 0.75F * random.nextInt(18)),
                level.registryAccess(), level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getTag(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE));
    }

    // original's odds: a little worn, sometimes enchanted, better the luckier the roll
    private static ItemStack gear(ServerLevel level, int rarity, RandomSource random) {
        int quality = random.nextInt(2);
        for (float odds : new float[] {0.2F, 0.15F, 0.1F, 0.095F, 0.095F}) {
            if (random.nextFloat() < odds) {
                quality++;
            }
        }
        ItemStack made = new ItemStack(gearFor(random.nextInt(5)).get(quality));
        made.setDamageValue(random.nextInt(1 + made.getMaxDamage() / 6));
        return random.nextInt(4) < rarity ? enchanted(level, made, rarity, random) : made;
    }

    private static List<Item> gearFor(int slot) {
        return switch (slot) {
            case 0 -> List.of(Items.IRON_AXE, Items.IRON_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_SWORD,
                    ModItems.METAL_TOOLS.get("thaumium_sword").get(), Items.DIAMOND_SWORD,
                    ModItems.METAL_TOOLS.get("void_sword").get());
            case 1 -> armour(Items.LEATHER_BOOTS, Items.GOLDEN_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS,
                    "boots", Items.DIAMOND_BOOTS);
            case 2 -> armour(Items.LEATHER_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS,
                    "leggings", Items.DIAMOND_LEGGINGS);
            case 3 -> armour(Items.LEATHER_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE,
                    Items.IRON_CHESTPLATE, "chestplate", Items.DIAMOND_CHESTPLATE);
            default -> armour(Items.LEATHER_HELMET, Items.GOLDEN_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET,
                    "helmet", Items.DIAMOND_HELMET);
        };
    }

    private static List<Item> armour(Item leather, Item gold, Item chain, Item iron, String piece, Item diamond) {
        return List.of(leather, gold, chain, iron, ModItems.METAL_ARMOUR.get("thaumium_" + piece).get(), diamond,
                ModItems.METAL_ARMOUR.get("void_" + piece).get());
    }

    private static void add(List<ItemStack> things, List<Integer> weights, ItemStack thing, int weight) {
        things.add(thing);
        weights.add(weight);
    }
}
