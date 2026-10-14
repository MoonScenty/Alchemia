package me.moonscenty.alchemia.entity;

import java.util.List;

import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * The ancient autocaster: a turret of the Outer Lands, set on a pillar in the floor or hanging from the ceiling of a
 * maze's passages. It belongs to nobody and minds everything alive that is not of the Outer Lands -- except the
 * undead, which the original meant it to pass over -- and shoots it with whatever focus it was built with: the
 * shard four times in ten, frost, shock or fire otherwise.
 * <p>
 * It holds fifty of each primal, starts with ten to nineteen of each, and draws more out of the air as any autocaster
 * does. It is harder than a made one (fifty-five to wear down, eight of plating). Broken, it may give up a zombie
 * brain, a void ingot or two, and its focus.
 */
public class EldritchAutocaster extends Autocaster implements EldritchMob {
    private static final List<String> FOCI = List.of("shard", "shard", "frost", "shock", "fire");

    public EldritchAutocaster(EntityType<? extends EldritchAutocaster> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Autocaster.attributes().add(Attributes.MAX_HEALTH, 55.0);
    }

    /** Puts one in a maze, against this face, armed. */
    public static void place(ServerLevelAccessor level, BlockPos at, Direction face) {
        EldritchAutocaster turret = ModEntities.ELDRITCH_AUTOCASTER.get().create(level.getLevel());
        if (turret == null) {
            return;
        }
        turret.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
        turret.setFacing(face);
        turret.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.STRUCTURE, null);
        turret.arm(level.getRandom());
        level.addFreshEntity(turret);
    }

    /** A focus out of the original's four, and a little of each primal to start on. */
    public void arm(RandomSource random) {
        String focus = FOCI.get(random.nextInt(FOCI.size()));
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.FOCI.get(focus).get()));
        for (var primal : ModAspects.primals()) {
            setVis(primal, 10 + random.nextInt(10));
        }
    }

    @Override
    protected int holdsAtMost() {
        return 50;
    }

    @Override
    protected boolean worthShooting(LivingEntity other) {
        if (other instanceof EldritchMob || other.getType().is(EntityTypeTags.UNDEAD)) {
            return false;
        }
        return !(other instanceof Player player) || !player.isCreative() && !player.isSpectator();
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof EldritchMob || super.isAlliedTo(other);
    }

    @Override
    public int getArmorValue() {
        return 8;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource cause, boolean recentlyHit) {
        int looting = 0;
        if (cause.getEntity() instanceof LivingEntity killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING), killer);
        }
        float bonus = 0.15F * looting;
        if (getRandom().nextFloat() < 0.3F + bonus) {
            spawnAtLocation(new ItemStack(ModItems.ZOMBIE_BRAIN.get()), 0.5F);
        }
        if (getRandom().nextFloat() < 0.3F + bonus) {
            spawnAtLocation(new ItemStack(ModItems.VOID_INGOT.get(), 2), 0.5F);
        }
        if (getRandom().nextFloat() < 0.15F + bonus && !getMainHandItem().isEmpty()) {
            spawnAtLocation(getMainHandItem().copy(), 0.0F);
        }
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }
}
