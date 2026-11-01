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

public class AlumentumItem extends Item implements ProjectileItem {
    public static final int BURNS_FOR = 6400;
    // smelter speed multiplier, in fifths
    public static final int HURRIES_BY = 4;
    // original's value
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

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ThrownAlumentum thrown = new ThrownAlumentum(level, pos.x(), pos.y(), pos.z());
        thrown.setItem(stack);
        return thrown;
    }
}
