package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.entity.ThrownAlumentum;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Coal that has been round again: boiled in a crucible until the fire in it is four times what it was.
 * <p>
 * It burns for as long as four coals in anything that takes fuel, which is most of what it is for. In an essentia
 * smelter it does one thing more -- the work goes a fifth quicker while it lasts -- because a smelter is slow by
 * design and this is the one thing a player can spend to hurry it.
 * <p>
 * Thrown, by hand or from a dispenser, it goes off where it lands, as the original's did.
 */
public class AlumentumItem extends Item implements ProjectileItem {
    /** Four coals' worth. Coal is sixteen hundred ticks. */
    public static final int BURNS_FOR = 6400;
    /** What a smelter's work is multiplied by while one of these is in the fire, in fifths. */
    public static final int HURRIES_BY = 4;
    /** How hard it is thrown by hand. The original's, half a snowball's. */
    private static final float THROWN = 0.75F;

    public AlumentumItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipe) {
        return BURNS_FOR;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 0.3F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            ThrownAlumentum thrown = new ThrownAlumentum(level, player);
            thrown.setItem(held);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, THROWN, 1.0F);
            level.addFreshEntity(thrown);
        }
        held.consume(1, player);
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    /** From a dispenser it is shot like any other thrown thing. */
    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ThrownAlumentum thrown = new ThrownAlumentum(level, pos.x(), pos.y(), pos.z());
        thrown.setItem(stack);
        return thrown;
    }
}
