package me.moonscenty.alchemia.entity;

import java.util.List;

import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

// Ownerless maze turret; it passes over the undead, as the original meant to.
public class EldritchAutocaster extends Autocaster implements EldritchMob {
    private static final List<String> FOCI = List.of("shard", "shard", "frost", "shock", "fire");

    public EldritchAutocaster(EntityType<? extends EldritchAutocaster> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Autocaster.attributes().add(Attributes.MAX_HEALTH, 55.0);
    }

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
    protected boolean mindsKind(LivingEntity other) {
        return !(other instanceof EldritchMob);
    }

    @Override
    protected void forgetStrangers() {
    }

    @Override
    public boolean worthShooting(LivingEntity other) {
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
    protected void salvage() {
        if (salvaged(0.3F)) {
            spawnAtLocation(new ItemStack(ModItems.ZOMBIE_BRAIN.get()), 0.5F);
        }
        if (salvaged(0.3F)) {
            spawnAtLocation(new ItemStack(ModItems.VOID_INGOT.get()), 0.5F);
        }
        if (salvaged(0.3F)) {
            spawnAtLocation(new ItemStack(ModItems.VOID_INGOT.get()), 0.5F);
        }
        if (salvagedHalfHelped(0.15F) && !getMainHandItem().isEmpty()) {
            spawnAtLocation(getMainHandItem().copy(), 0.5F);
        }
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }
}
