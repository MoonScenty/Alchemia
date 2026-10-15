package me.moonscenty.alchemia.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.OwnAspects;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.item.WispyEssenceItem;
import me.moonscenty.alchemia.menu.PechMenu;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.spell.PechBlast;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * A pech: a little hunched forager with a pack on its back, who picks up whatever it thinks is worth having.
 *
 * <p>There are three. A forager fights with whatever it has in hand, a mage with a wand that throws curses, and a
 * stalker with a bow. Untamed, a pech keeps clear of players; hit one, and every pech for thirty-two blocks about
 * comes for whoever did it, for twenty to forty seconds.
 *
 * <p>Drop it something it prizes -- gold, a golden apple, a pearl, a diamond, an emerald, or anything full enough of
 * desire -- and it may take to you. A pech that has taken to you will trade: it rolls the bones over what is offered
 * and pays out its worth in things off its own list, now and then something out of its pack, now and then something
 * out of a dungeon chest. It may tire of you over a good trade and go back to being wild.
 *
 * <p>Anything else it finds lying about goes in its pack, nine stacks of it, and comes out when it dies -- a third of
 * the time; otherwise a gold coin. It drops a wisp's essence or two besides, and now and then a fragment of
 * knowledge.
 */
public class Pech extends Monster implements RangedAttackMob, OwnAspects {
    private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(Pech.class,
            EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(Pech.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(Pech.class,
            EntityDataSerializers.BOOLEAN);

    /** The three kinds. */
    public static final int FORAGER = 0;
    public static final int MAGE = 1;
    public static final int STALKER = 2;

    /** The original's numbers. */
    private static final double LIVES = 30.0;
    private static final double HITS = 6.0;
    private static final double SPEED = 0.5;
    private static final double ARMOUR = 2.0;
    private static final int PACK = 9;
    /** How far a struck pech's fellows hear of it, across and up. */
    private static final double FELLOWS_ACROSS = 32.0;
    private static final double FELLOWS_UP = 16.0;
    /** How long a pech stays angry, at least and at most besides. */
    private static final int ANGRY_FOR = 400;
    /** Entity events: two kinds of mutter, pleased, and angry. */
    private static final byte MUTTERS = 16;
    private static final byte CHATTERS = 17;
    private static final byte PLEASED = 18;
    private static final byte ANGERED = 19;

    /** A tag on what a pech hands back after a trade, so that it does not go straight after it again. */
    public static final String HANDED_BACK = "alchemia.pech_drop";

    private final NonNullList<ItemStack> pack = NonNullList.withSize(PACK, ItemStack.EMPTY);
    /** Being traded with, which holds it still. */
    public boolean trading;
    private boolean rethink;
    /** How far its jaw is working, client-side; it closes by a quarter a tick. */
    public float mumble;
    private int growls;

    private final RangedAttackGoal shooting = new RangedAttackGoal(this, 0.6, 20, 50, 15.0F);
    private final RangedAttackGoal blasting = new RangedAttackGoal(this, 0.6, 20, 30, 15.0F);
    private final MeleeAttackGoal hitting = new MeleeAttackGoal(this, 0.6, false);
    private final AvoidEntityGoal<Player> shying = new AvoidEntityGoal<>(this, Player.class, 8.0F, 0.5, 0.6);

    public Pech(EntityType<? extends Pech> type, Level level) {
        super(type, level);
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        setDropChance(EquipmentSlot.MAINHAND, 0.2F);
        if (!level.isClientSide) {
            fight();
        }
    }

    public Pech(Level level) {
        this(ModEntities.PECH.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.ATTACK_DAMAGE, HITS)
                .add(Attributes.MOVEMENT_SPEED, SPEED)
                .add(Attributes.ARMOR, ARMOUR);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new HoldStillToTrade(this));
        goalSelector.addGoal(3, new FetchItems(this));
        goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.5));
        goalSelector.addGoal(9, new RandomStrollGoal(this, 0.6));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, net.minecraft.world.entity.Mob.class, 8.0F));
        goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return getAnger() > 0 && super.canUse();
            }
        });
    }

    /** How it fights, by what it holds, and whether it keeps clear of players, by whether it has taken to them. */
    private void fight() {
        goalSelector.removeGoal(hitting);
        goalSelector.removeGoal(shooting);
        goalSelector.removeGoal(blasting);
        ItemStack held = getMainHandItem();
        if (held.is(Items.BOW)) {
            goalSelector.addGoal(2, shooting);
        } else if (held.getItem() instanceof WandItem) {
            goalSelector.addGoal(2, blasting);
        } else {
            goalSelector.addGoal(2, hitting);
        }
        goalSelector.removeGoal(shying);
        if (!isTamed()) {
            goalSelector.addGoal(4, shying);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KIND, (byte) FORAGER);
        builder.define(ANGER, 0);
        builder.define(TAMED, false);
    }

    public int getKind() {
        return entityData.get(KIND);
    }

    public void setKind(int kind) {
        entityData.set(KIND, (byte) kind);
    }

    public int getAnger() {
        return entityData.get(ANGER);
    }

    public void setAnger(int anger) {
        entityData.set(ANGER, anger);
    }

    public boolean isTamed() {
        return entityData.get(TAMED);
    }

    public void setTamed(boolean tamed) {
        entityData.set(TAMED, tamed);
        rethink = true;
    }

    public NonNullList<ItemStack> pack() {
        return pack;
    }

    /** Each kind is called after what it does. */
    @Override
    protected Component getTypeName() {
        return switch (getKind()) {
            case MAGE -> Component.translatable("entity.alchemia.pech.mage");
            case STALKER -> Component.translatable("entity.alchemia.pech.stalker");
            default -> super.getTypeName();
        };
    }

    /** What a pech is besides being one: a forager wants, a mage wards off, a stalker is all aura. */
    @Override
    public AspectList ownAspects() {
        return switch (getKind()) {
            case MAGE -> AspectList.of(ModAspects.AVERSION, 2);
            case STALKER -> AspectList.of(ModAspects.AURA, 2);
            default -> AspectList.of(ModAspects.DESIRE, 2);
        };
    }

    // --- coming into the world ---------------------------------------------------------------------------

    /** What a new pech has in hand: a wand one time in ten, a bow one in four, a tool or a blade now and then. */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        ItemStack held = switch (random.nextInt(20)) {
            case 0, 12 -> wand(random);
            case 1 -> new ItemStack(Items.STONE_SWORD);
            case 2, 4, 10, 11, 13 -> new ItemStack(Items.BOW);
            case 3 -> new ItemStack(Items.STONE_AXE);
            case 5 -> new ItemStack(Items.IRON_SWORD);
            case 6 -> new ItemStack(Items.IRON_AXE);
            case 7 -> new ItemStack(Items.FISHING_ROD);
            case 8 -> new ItemStack(Items.STONE_PICKAXE);
            case 9 -> new ItemStack(Items.IRON_PICKAXE);
            default -> ItemStack.EMPTY;
        };
        setItemSlot(EquipmentSlot.MAINHAND, held);
    }

    /** A plain wand with the pech's own focus in it, and a little vis, mostly of earth, entropy and water. */
    private static ItemStack wand(RandomSource random) {
        ItemStack wand = WandItem.of(ModWandParts.WOOD, ModWandParts.IRON);
        WandItem.setFocus(wand, new ItemStack(ModItems.FOCI.get("pech").get()));
        WandItem.addVis(wand, ModAspects.EARTH, 2 + random.nextInt(6));
        WandItem.addVis(wand, ModAspects.ENTROPY, 2 + random.nextInt(6));
        WandItem.addVis(wand, ModAspects.WATER, 2 + random.nextInt(6));
        WandItem.addVis(wand, ModAspects.AIR, random.nextInt(4));
        WandItem.addVis(wand, ModAspects.FIRE, random.nextInt(4));
        WandItem.addVis(wand, ModAspects.ORDER, random.nextInt(4));
        return wand;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData data) {
        populateDefaultEquipmentSlots(random, difficulty);
        ItemStack held = getMainHandItem();
        if (held.getItem() instanceof WandItem) {
            setKind(MAGE);
            setDropChance(EquipmentSlot.MAINHAND, 0.1F);
        } else if (!held.isEmpty()) {
            if (held.is(Items.BOW)) {
                setKind(STALKER);
            }
            populateDefaultEquipmentEnchantments(level, random, difficulty);
        }
        setCanPickUpLoot(random.nextFloat() < 0.75F * difficulty.getSpecialMultiplier());
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    /** Never where too many already are. */
    public static boolean checkSpawnRules(EntityType<Pech> type, ServerLevelAccessor level, MobSpawnType reason,
            net.minecraft.core.BlockPos pos, RandomSource random) {
        return level.getEntitiesOfClass(Pech.class, new AABB(pos).inflate(16.0)).size() < 4
                && checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (!level().isClientSide && slot == EquipmentSlot.MAINHAND) {
            rethink = true;
        }
    }

    // --- fighting ----------------------------------------------------------------------------------------

    /** A stalker looses an arrow; a mage throws a curse. */
    @Override
    public void performRangedAttack(LivingEntity target, float pull) {
        if (getKind() == STALKER) {
            ItemStack bow = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
            AbstractArrow arrow = ProjectileUtil.getMobArrow(this, getProjectile(bow), pull, bow);
            double dx = target.getX() - getX();
            double dy = target.getY(1.0 / 3.0) - arrow.getY();
            double dz = target.getZ() - getZ();
            double across = Math.sqrt(dx * dx + dz * dz);
            arrow.shoot(dx, dy + across * 0.2, dz, 1.6F, 14 - level().getDifficulty().getId() * 4);
            playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
            level().addFreshEntity(arrow);
        } else if (getKind() == MAGE) {
            PechBlast blast = new PechBlast(level(), this).cursing(1, 0, random.nextFloat() < 0.1F);
            double dx = target.getX() + target.getDeltaMovement().x - getX();
            double dy = target.getY() + target.getEyeHeight() - 1.5 - getY();
            double dz = target.getZ() + target.getDeltaMovement().z - getZ();
            double across = Math.sqrt(dx * dx + dz * dz);
            blast.shoot(dx, dy + across * 0.1, dz, 1.5F, 4.0F);
            playSound(ModSounds.ICE.get(), 0.4F, 1.0F + random.nextFloat() * 0.1F);
            level().addFreshEntity(blast);
        }
        swing(InteractionHand.MAIN_HAND);
    }

    /** Hit by a player, it and every pech about it goes for them. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        if (source.getEntity() instanceof Player player) {
            for (Pech fellow : level().getEntitiesOfClass(Pech.class,
                    getBoundingBox().inflate(FELLOWS_ACROSS, FELLOWS_UP, FELLOWS_ACROSS))) {
                fellow.angerAt(player);
            }
            angerAt(player);
        }
        return super.hurt(source, amount);
    }

    private void angerAt(LivingEntity enemy) {
        if (getAnger() <= 0) {
            level().broadcastEntityEvent(this, ANGERED);
            playSound(ModSounds.PECH_CHARGE.get(), getSoundVolume(), getVoicePitch());
        }
        setTarget(enemy);
        setAnger(ANGRY_FOR + random.nextInt(ANGRY_FOR));
        setTamed(false);
    }

    // --- ticking -----------------------------------------------------------------------------------------

    @Override
    public void tick() {
        if (mumble > 0.0F) {
            mumble *= 0.75F;
        }
        if (!level().isClientSide) {
            if (getAnger() > 0) {
                setAnger(getAnger() - 1);
            }
            if (getAnger() > 0 && getTarget() != null) {
                if (growls > 0) {
                    growls--;
                }
                if (growls == 0) {
                    growls = 100;
                    playSound(ModSounds.PECH_CHARGE.get(), getSoundVolume(), getVoicePitch());
                }
                level().broadcastEntityEvent(this, CHATTERS);
            }
        } else {
            if (random.nextInt(15) == 0 && getAnger() > 0) {
                puff(ParticleTypes.ANGRY_VILLAGER);
            }
            if (random.nextInt(25) == 0 && isTamed()) {
                puff(ParticleTypes.HAPPY_VILLAGER);
            }
        }
        super.tick();
    }

    @Override
    protected void customServerAiStep() {
        if (rethink) {
            rethink = false;
            fight();
        }
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            heal(1.0F);
        }
    }

    private void puff(ParticleOptions particle) {
        level().addParticle(particle, getX() + random.nextFloat() * getBbWidth() * 2.0F - getBbWidth(),
                getY() + 0.5 + random.nextFloat() * getBbHeight(),
                getZ() + random.nextFloat() * getBbWidth() * 2.0F - getBbWidth(),
                random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
    }

    @Override
    public void handleEntityEvent(byte event) {
        switch (event) {
            case MUTTERS -> mumble = Mth.PI;
            case CHATTERS -> mumble = Mth.TWO_PI;
            case PLEASED -> {
                for (int puff = 0; puff < 5; puff++) {
                    puff(ParticleTypes.HAPPY_VILLAGER);
                }
            }
            case ANGERED -> {
                for (int puff = 0; puff < 5; puff++) {
                    puff(ParticleTypes.ANGRY_VILLAGER);
                }
                mumble = Mth.TWO_PI;
            }
            default -> super.handleEntityEvent(event);
        }
    }

    /** It mutters as it goes, and now and then chatters with another pech near it. */
    @Override
    public void playAmbientSound() {
        if (!level().isClientSide) {
            if (random.nextInt(3) == 0 && !level().getEntitiesOfClass(Pech.class,
                    getBoundingBox().inflate(4.0, 2.0, 4.0), other -> other != this).isEmpty()) {
                level().broadcastEntityEvent(this, CHATTERS);
                playSound(ModSounds.PECH_TRADE.get(), getSoundVolume(), getVoicePitch());
                return;
            }
            level().broadcastEntityEvent(this, MUTTERS);
        }
        super.playAmbientSound();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PECH_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PECH_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PECH_DEATH.get();
    }

    // --- picking things up -------------------------------------------------------------------------------

    /** Whether it would pick this up: something it prizes while it is still wild, or anything with room for it. */
    public boolean wants(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!isTamed() && PechTrades.valued(stack)) {
            return true;
        }
        for (ItemStack held : pack) {
            if (held.isEmpty() || ItemStack.isSameItemSameComponents(held, stack)
                    && held.getCount() + stack.getCount() <= held.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Takes what it can of this. Something it prizes, while it is still wild, it eats, and may take to whoever left it
     * for it; anything else goes in its pack.
     *
     * @return what it left
     */
    public ItemStack take(ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        if (!isTamed() && PechTrades.valued(stack)) {
            if (random.nextInt(10) < PechTrades.value(stack)) {
                setTamed(true);
                level().broadcastEntityEvent(this, PLEASED);
            }
            stack.shrink(1);
            return stack;
        }
        for (ItemStack held : pack) {
            if (!held.isEmpty() && ItemStack.isSameItemSameComponents(held, stack)) {
                int moved = Math.min(stack.getCount(), held.getMaxStackSize() - held.getCount());
                held.grow(moved);
                stack.shrink(moved);
            }
        }
        for (int slot = 0; slot < PACK && !stack.isEmpty(); slot++) {
            if (pack.get(slot).isEmpty()) {
                pack.set(slot, stack.copy());
                stack.setCount(0);
            }
        }
        return stack;
    }

    /** One thing out of its pack, at random, if it has anything. */
    public ItemStack fromPack() {
        List<Integer> full = new ArrayList<>();
        for (int slot = 0; slot < PACK; slot++) {
            if (!pack.get(slot).isEmpty()) {
                full.add(slot);
            }
        }
        return full.isEmpty() ? ItemStack.EMPTY : pack.get(full.get(random.nextInt(full.size()))).split(1);
    }

    // --- trading -----------------------------------------------------------------------------------------

    /** A pech that has taken to whoever it is trades with them; a wild one is any other mob to them. */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() || player.getItemInHand(hand).getItem() instanceof NameTagItem) {
            return InteractionResult.PASS;
        }
        if (!isTamed()) {
            return super.mobInteract(player, hand);
        }
        if (!level().isClientSide) {
            player.openMenu(new SimpleMenuProvider((id, inventory, who) -> new PechMenu(id, inventory, this),
                    getDisplayName()), buffer -> buffer.writeVarInt(getId()));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /** It gets bored of a trader now and then, over a good enough trade. */
    public void tireOfTrading() {
        setTamed(false);
        playSound(ModSounds.PECH_TRADE.get(), 0.4F, 1.0F);
    }

    // --- going and dying ---------------------------------------------------------------------------------

    /** Not while it has much in its pack. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return pack.stream().filter(stack -> !stack.isEmpty()).count() < 5;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        int looting = source.getEntity() instanceof LivingEntity killer
                ? EnchantmentHelper.getEnchantmentLevel(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.LOOTING), killer)
                : 0;
        for (int slot = 0; slot < PACK; slot++) {
            ItemStack held = pack.get(slot);
            if (!held.isEmpty() && random.nextFloat() < 0.33F) {
                spawnAtLocation(held.copy(), 1.5F);
            } else if (random.nextFloat() > 0.33F) {
                spawnAtLocation(new ItemStack(ModItems.COIN.get()), 1.5F);
            }
        }
        for (int tries = 0; tries < 1 + looting; tries++) {
            if (random.nextBoolean()) {
                spawnAtLocation(WispyEssenceItem.of(ModAspects.randomPrimal(random)), 1.5F);
            }
        }
        if (random.nextInt(50) < 1 + looting) {
            spawnAtLocation(new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get()), 1.5F);
        }
        super.dropCustomDeathLoot(level, source, recentlyHit);
    }

    // --- saving ------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("PechType", (byte) getKind());
        tag.putInt("Anger", getAnger());
        tag.putBoolean("Tamed", isTamed());
        ListTag loot = new ListTag();
        for (ItemStack held : pack) {
            loot.add(held.isEmpty() ? new CompoundTag() : held.save(registryAccess()));
        }
        tag.put("Loot", loot);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setKind(tag.getByte("PechType"));
        setAnger(tag.getInt("Anger"));
        setTamed(tag.getBoolean("Tamed"));
        ListTag loot = tag.getList("Loot", Tag.TAG_COMPOUND);
        for (int slot = 0; slot < PACK; slot++) {
            pack.set(slot, slot < loot.size()
                    ? ItemStack.parseOptional(registryAccess(), loot.getCompound(slot)) : ItemStack.EMPTY);
        }
        rethink = true;
    }

    // --- its own ways --------------------------------------------------------------------------------------

    /** Stands still while somebody is trading with it, so long as it is on firm, dry ground. */
    private static final class HoldStillToTrade extends Goal {
        private final Pech pech;

        HoldStillToTrade(Pech pech) {
            this.pech = pech;
            setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return pech.isAlive() && !pech.isInWater() && pech.isTamed() && pech.onGround() && !pech.hurtMarked
                    && pech.trading;
        }

        @Override
        public void start() {
            pech.getNavigation().stop();
        }

        @Override
        public void stop() {
            pech.trading = false;
        }
    }

    /** Goes after whatever is lying about within sixteen blocks that it would pick up, and picks it up. */
    private static final class FetchItems extends Goal {
        private static final double LOOKS = 16.0;
        private final Pech pech;
        @Nullable
        private ItemEntity after;
        private int count;
        private int penalty;

        FetchItems(Pech pech) {
            this.pech = pech;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (--count > 0) {
                return false;
            }
            double nearest = Double.MAX_VALUE;
            after = null;
            for (ItemEntity item : pech.level().getEntitiesOfClass(ItemEntity.class,
                    pech.getBoundingBox().inflate(LOOKS))) {
                if (!pech.wants(item.getItem()) || item.getTags().contains(HANDED_BACK)) {
                    continue;
                }
                double distance = item.distanceToSqr(pech);
                if (distance < nearest && distance <= LOOKS * LOOKS) {
                    nearest = distance;
                    after = item;
                }
            }
            return after != null;
        }

        @Override
        public boolean canContinueToUse() {
            return after != null && after.isAlive() && !pech.getNavigation().isDone()
                    && after.distanceToSqr(pech) < LOOKS * LOOKS;
        }

        @Override
        public void start() {
            pech.getNavigation().moveTo(after, pech.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5);
            count = 0;
        }

        @Override
        public void stop() {
            after = null;
        }

        @Override
        public void tick() {
            if (after == null) {
                return;
            }
            pech.getLookControl().setLookAt(after, 30.0F, 30.0F);
            if (pech.getSensing().hasLineOfSight(after) && --count <= 0) {
                count = penalty + 4 + pech.getRandom().nextInt(4);
                pech.getNavigation().moveTo(after, pech.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5);
                var path = pech.getNavigation().getPath();
                if (path != null && path.getEndNode() != null
                        && after.distanceToSqr(path.getEndNode().x, path.getEndNode().y, path.getEndNode().z) < 1.0) {
                    penalty = 0;
                } else {
                    penalty += 10;
                }
            }
            if (pech.distanceToSqr(after.getX(), after.getBoundingBox().minY, after.getZ()) <= 1.5) {
                count = 0;
                int before = after.getItem().getCount();
                ItemStack left = pech.take(after.getItem().copy());
                if (left.isEmpty()) {
                    after.discard();
                } else {
                    after.setItem(left);
                }
                if (left.getCount() != before) {
                    after.playSound(SoundEvents.ITEM_PICKUP, 0.2F,
                            ((pech.getRandom().nextFloat() - pech.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }
            }
        }
    }
}
