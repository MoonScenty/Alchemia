package me.moonscenty.alchemia.entity.cult;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * The cult's soldier: 30 health, the crimson plate head to foot, and an iron sword. Now and then -- one in twenty
 * on hard, one in a hundred otherwise -- something better: one in five of those a void sword under a hood, the rest
 * an alchemium sword, half of them bareheaded. A quarter of the time, as the region gets harder, the sword is
 * enchanted. It swings twice as often as anything else that swings.
 */
public class CultistKnight extends Cultist {
    public CultistKnight(EntityType<? extends CultistKnight> type, Level level) {
        super(type, level);
    }

    public CultistKnight(Level level) {
        this(ModEntities.CULTIST_KNIGHT.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Cultist.attributes().add(Attributes.MAX_HEALTH, 30.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new QuickMelee(this));
        goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new CallForHelp(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void dress(ServerLevelAccessor level, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.CRIMSON.get("crimson_plate_helm").get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.CRIMSON.get("crimson_plate_chest").get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.CRIMSON.get("crimson_plate_legs").get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.CRIMSON.get("crimson_boots").get()));
        float special = level.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F;
        if (random.nextFloat() < special) {
            if (random.nextInt(5) == 0) {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.METAL_TOOLS.get("void_sword").get()));
                setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.CRIMSON.get("crimson_robe_helm").get()));
            } else {
                setItemSlot(EquipmentSlot.MAINHAND,
                        new ItemStack(ModItems.METAL_TOOLS.get("alchemium_sword").get()));
                if (random.nextBoolean()) {
                    setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }
        } else {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
        float hardness = difficulty.getSpecialMultiplier();
        ItemStack held = getMainHandItem();
        if (!held.isEmpty() && random.nextFloat() < 0.25F * hardness) {
            setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(random, held,
                    (int) (5.0F + hardness * random.nextInt(18)), level.registryAccess(),
                    level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                            .getTag(net.minecraft.tags.EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)));
        }
    }

    /** A melee goal that swings every half second, as the original's did, where the game's swings every second. */
    public static class QuickMelee extends MeleeAttackGoal {
        public QuickMelee(net.minecraft.world.entity.PathfinderMob mob) {
            super(mob, 1.0, false);
        }

        @Override
        protected int getAttackInterval() {
            return adjustedTickDelay(10);
        }
    }
}
