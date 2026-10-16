package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.ArcaneBoreBaseBlock;
import me.moonscenty.alchemia.block.ArcaneBoreBlock;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.enchantment.InfusionEnchantments;
import me.moonscenty.alchemia.item.Clusters;
import me.moonscenty.alchemia.menu.ArcaneBoreMenu;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * An arcane bore: an excavation focus and a pickaxe on a stand, eating a tunnel out in front of it for as long as
 * it is given a redstone signal and something to run on.
 * <p>
 * It runs on entropy: piped in through its base, each point of essentia does twenty blocks at full speed; without
 * essentia it draws entropy out of the air, a block a point and four times as slow. It sweeps a spiral round the
 * axis it faces, two blocks out and more for every rank of widening on the focus, and digs each line of the spiral
 * as deep as sixty-four blocks before moving on. Everything it mines comes out of the base's nozzle, into a
 * container there if there is one.
 * <p>
 * What is on the focus and the pickaxe is what it mines with: force and efficiency make it faster, treasure and
 * fortune make it luckier, either silk touch takes blocks whole, and dowsing or refining turns some ores into
 * clusters. The pickaxe wears a point a block, and the bore stops while it has one left.
 */
public class ArcaneBoreBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_FOCUS = 0;
    public static final int SLOT_PICK = 1;
    /** The radius it sweeps with nothing worked into the focus. */
    public static final int RADIUS = 2;
    /** How many blocks one point of entropy essentia carries it at full speed. */
    private static final int A_POINT_DIGS = 20;
    /** How much entropy it will hold from the air before it stops drawing more. */
    private static final int DRAWS = 5;
    /** How far it looks along a line of the spiral, and how far in front of it the first block it looks at is. */
    private static final int REACHES = 64;
    /** How much slower it digs on what it draws from the air than on essentia. */
    private static final int SLOW = 4;
    /** The block events the client draws from: a block dug, and nothing more. */
    public static final int DUG = 99;
    /** How long the rumble of the digging takes to come round again, in ticks: the original's 1.2 seconds. */
    private static final int RUMBLES_EVERY = 24;
    private static final GameProfile BORE = new GameProfile(java.util.UUID.fromString(
            "6b0f6a16-5b6b-4d0e-8a10-6f3a6e0b0b0e"), "[Alchemia Bore]");

    private final NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);

    // what is worked into the focus and the pickaxe, worked out when either changes
    private int area;
    private int speed;
    private int fortune;

    // the spiral, and the block it is going for
    private int spiral;
    private float radius;
    private float radiusStep;
    private BlockPos lastColumn = BlockPos.ZERO;
    private int stick;
    private int count;
    private boolean toDig;
    @Nullable
    private BlockPos dig;

    // what it has to run on
    private float speedy;
    private int powered;

    /** Client side: where the beam last went, and how long ago, for the head and the beam. */
    private int sinceDug = 100;
    private float spin;
    @Nullable
    private BlockPos lastDug;
    /** Client side: when the rumble of the digging may next be heard, by the world clock. */
    private long nextRumble;

    public ArcaneBoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_BORE.get(), pos, state);
    }

    // --- where things are --------------------------------------------------

    private Direction facing() {
        return getBlockState().getValue(ArcaneBoreBlock.FACING);
    }

    /** The side the base is on: above if there is one above, else below. */
    private Direction baseSide() {
        return level != null && level.getBlockState(worldPosition.above()).is(ModBlocks.ARCANE_BORE_BASE.get())
                ? Direction.UP : Direction.DOWN;
    }

    @Nullable
    private ArcaneBoreBaseBlockEntity base() {
        return level != null && level.getBlockEntity(worldPosition.relative(baseSide()))
                instanceof ArcaneBoreBaseBlockEntity base ? base : null;
    }

    private boolean powered(Level level) {
        return level.hasNeighborSignal(worldPosition) || level.hasNeighborSignal(worldPosition.relative(baseSide()));
    }

    /** Whether it has a focus to dig with and a pickaxe with wear left in it. */
    public boolean ready() {
        ItemStack pick = contents.get(SLOT_PICK);
        return isFocus(contents.get(SLOT_FOCUS)) && isPick(pick) && pick.isDamageableItem()
                && pick.getDamageValue() + 1 < pick.getMaxDamage();
    }

    public static boolean isFocus(ItemStack stack) {
        return stack.is(ModItems.FOCI.get("excavation").get());
    }

    public static boolean isPick(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES);
    }

    // --- running on --------------------------------------------------------

    /** Whether the base should take a point of entropy from a pipe now. */
    boolean wantsEssentia() {
        return speedy < A_POINT_DIGS;
    }

    /** A point of entropy, come in through the base. */
    void fuel() {
        speedy += A_POINT_DIGS;
        powered += A_POINT_DIGS;
        setChanged();
    }

    // --- ticking -----------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneBoreBlockEntity bore) {
        ArcaneBoreBaseBlockEntity base = bore.base();
        if (base != null && bore.wantsEssentia()) {
            base.drawFromBeside(bore);
        }
        if (bore.speedy < 1 && bore.powered < DRAWS) {
            bore.powered += AuraHandler.drainAvailable(level, pos, ModAspects.ENTROPY, DRAWS);
        }
        // repair on the pickaxe mends it here as it would in a hand
        if (level.getGameTime() % me.moonscenty.alchemia.enchantment.Repair.EVERY == 0
                && me.moonscenty.alchemia.enchantment.Repair.mend(bore.contents.get(SLOT_PICK), level, pos,
                        level.random)) {
            bore.setChanged();
        }
        if (bore.powered(level) && bore.ready() && level instanceof ServerLevel served) {
            bore.dig(served, base);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ArcaneBoreBlockEntity bore) {
        bore.sinceDug++;
        if (bore.sinceDug < 40) {
            bore.spin += 10.0F;
        } else if (bore.spin % 90.0F != 0.0F) {
            bore.spin += Math.min(10.0F, 90.0F - bore.spin % 90.0F);
        }
        // the ground grumbling while it digs, a little over once a second, as the original's did
        if (bore.working() && level.getGameTime() >= bore.nextRumble) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.RUMBLE.get(),
                    SoundSource.BLOCKS, 0.25F, 0.9F + level.random.nextFloat() * 0.2F, false);
            bore.nextRumble = level.getGameTime() + RUMBLES_EVERY + level.random.nextInt(3);
        }
    }

    private void dig(ServerLevel level, @Nullable ArcaneBoreBaseBlockEntity base) {
        if (--count > 0 || powered < 1) {
            return;
        }
        boolean did = false;
        if (toDig && dig != null) {
            toDig = false;
            did = take(level, dig, base);
        }
        nextBlock(level);
        if (did) {
            if (speedy > 0) {
                speedy--;
            }
            if (powered > 0) {
                powered--;
            }
        }
    }

    /** Takes one block: what it drops, and whatever is lying loose around it, goes out of the base. */
    private boolean take(ServerLevel level, BlockPos at, @Nullable ArcaneBoreBaseBlockEntity base) {
        BlockState state = level.getBlockState(at);
        if (state.isAir() || state.getDestroySpeed(level, at) < 0) {
            return false;
        }
        FakePlayer digger = FakePlayerFactory.get(level, BORE);
        if (NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, at, state, digger)).isCanceled()) {
            return false;
        }
        ItemStack tool = tool(level);
        boolean whole = EnchantmentHelper.getItemEnchantmentLevel(enchantment(level, Enchantments.SILK_TOUCH), tool) > 0;
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, level, at, level.getBlockEntity(at), digger,
                tool));
        state.spawnAfterBreak(level, at, tool, false);
        level.levelEvent(2001, at, Block.getId(state));
        level.removeBlock(at, false);
        for (ItemEntity loose : level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(1.0))) {
            drops.add(loose.getItem().copy());
            loose.discard();
        }
        int refining = InfusionEnchantments.level(contents.get(SLOT_PICK), InfusionEnchantment.REFINING)
                + (FocusUpgrades.has(contents.get(SLOT_FOCUS), FocusUpgrade.DOWSING) ? 1 : 0);
        for (ItemStack drop : drops) {
            ItemStack out = !whole && refining > 0
                    ? Clusters.maybe(drop, (refining + 1) * 0.125F, level.random) : drop;
            eject(level, out, base);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), DUG, 0);
        contents.get(SLOT_PICK).hurtAndBreak(1, level, null, item -> {
        });
        lightTunnel(level, base);
        setChanged();
        return true;
    }

    /**
     * The pickaxe as it digs: its own enchantments, with the focus's treasure laid on as fortune where that is the
     * better, and the focus's silk touch where it has it.
     */
    private ItemStack tool(ServerLevel level) {
        ItemStack tool = contents.get(SLOT_PICK).copy();
        ItemStack focus = contents.get(SLOT_FOCUS);
        Holder<Enchantment> luck = enchantment(level, Enchantments.FORTUNE);
        int treasure = FocusUpgrades.level(focus, FocusUpgrade.TREASURE);
        if (treasure > EnchantmentHelper.getItemEnchantmentLevel(luck, tool)) {
            tool.enchant(luck, treasure);
        }
        if (FocusUpgrades.has(focus, FocusUpgrade.SILK_TOUCH)) {
            tool.enchant(enchantment(level, Enchantments.SILK_TOUCH), 1);
        }
        return tool;
    }

    private static Holder<Enchantment> enchantment(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
    }

    /** Out of the base's nozzle, into a container there if it has room; at the bore if the base is gone. */
    private void eject(ServerLevel level, ItemStack stack, @Nullable ArcaneBoreBaseBlockEntity base) {
        if (stack.isEmpty()) {
            return;
        }
        if (base == null) {
            Block.popResource(level, worldPosition, stack);
            return;
        }
        Direction nozzle = base.getBlockState().getValue(ArcaneBoreBaseBlock.FACING);
        BlockPos at = base.getBlockPos();
        IItemHandler container = level.getCapability(Capabilities.ItemHandler.BLOCK, at.relative(nozzle),
                nozzle.getOpposite());
        ItemStack left = container == null ? stack : ItemHandlerHelper.insertItemStacked(container, stack, false);
        if (!left.isEmpty()) {
            ItemEntity out = new ItemEntity(level, at.getX() + 0.5 + nozzle.getStepX() * 0.66, at.getY() + 0.4,
                    at.getZ() + 0.5 + nozzle.getStepZ() * 0.66, left);
            out.setDeltaMovement(0.075 * nozzle.getStepX(), 0.025, 0.075 * nozzle.getStepZ());
            level.addFreshEntity(out);
        }
    }

    /** With an arcane lamp beside its base, it lights the tunnel as it goes, as the original did. */
    private void lightTunnel(ServerLevel level, @Nullable ArcaneBoreBaseBlockEntity base) {
        if (base == null) {
            return;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(base.getBlockPos().relative(side)).is(ModBlocks.ARCANE_LAMP.get())) {
                continue;
            }
            Direction f = facing();
            int d = level.random.nextInt(32) * 2;
            int quarter = (d / 2) % 4;
            int across = quarter == 0 ? 3 : quarter == 2 ? -3 : 0;
            BlockPos spot = worldPosition.relative(f, 1 + d);
            spot = f.getStepX() != 0 ? spot.offset(0, 0, across) : spot.offset(across, 0, 0);
            if (quarter == 3 && f.getAxis().isHorizontal()) {
                spot = spot.below(2);
            }
            if (level.isEmptyBlock(spot) && !level.getBlockState(spot).is(ModBlocks.LAMP_LIGHT.get())
                    && level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, spot) < 11) {
                level.setBlock(spot, ModBlocks.LAMP_LIGHT.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            return;
        }
    }

    /**
     * Finds the next block to go for. While a line of the spiral still has something in it, it keeps to that line
     * and goes deeper; once the line is clear to the end of its reach, it turns the spiral on to the next.
     */
    private void nextBlock(ServerLevel level) {
        if (radiusStep == 0.0F) {
            radiusStep = 1.0F;
        }
        Direction f = facing();
        BlockPos column = lastColumn;
        while (stick == 0 && column.equals(lastColumn)) {
            spiral = (int) (spiral + (5 + (10 - Math.abs(radius)) * 2));
            if (spiral >= 360) {
                spiral -= 360;
                radius += radiusStep;
                int widest = RADIUS + area;
                if (radius >= widest || radius <= -widest) {
                    radiusStep = -radiusStep;
                }
            }
            Vec3 out = turned(new Vec3(0.0, radius, 0.0), spiral * Mth.DEG_TO_RAD, f);
            column = BlockPos.containing(worldPosition.getX() + f.getStepX() + 0.5 + out.x,
                    worldPosition.getY() + f.getStepY() + 0.5 + out.y,
                    worldPosition.getZ() + f.getStepZ() + 0.5 + out.z);
        }
        lastColumn = column;
        BlockPos at = column.relative(f);
        for (int depth = 0; depth < REACHES; depth++) {
            at = at.relative(f);
            if (depth < stick) {
                continue;
            }
            BlockState state = level.getBlockState(at);
            float hardness = state.getDestroySpeed(level, at);
            if (hardness < 0) {
                break;
            }
            if (!state.isAir() && !state.getCollisionShape(level, at).isEmpty()) {
                dig = at;
                stick = depth;
                count = delay(hardness);
                toDig = true;
                // whatever stands between the bore and the block it is going for is what it digs first
                Vec3 front = Vec3.atCenterOf(worldPosition.relative(f));
                BlockHitResult hit = level.clip(new ClipContext(front, Vec3.atCenterOf(at), ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockState inWay = level.getBlockState(hit.getBlockPos());
                    float inWayHardness = inWay.getDestroySpeed(level, hit.getBlockPos());
                    if (inWayHardness > -1 && !inWay.getCollisionShape(level, hit.getBlockPos()).isEmpty()) {
                        count = delay(inWayHardness);
                        if (!hit.getBlockPos().equals(at)) {
                            stick = 0;
                        }
                        dig = hit.getBlockPos();
                    }
                }
                setChanged();
                if (level.getBlockState(worldPosition).is(getBlockState().getBlock())) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
                }
                return;
            }
            stick = 0;
        }
        stick = 0;
    }

    /** How long a block takes: the original's sum, four times over on what is drawn from the air. */
    private int delay(float hardness) {
        int ticks = Math.max(10 - speed, (int) (hardness * 2.0F) - speed * 2);
        return speedy < 1 ? ticks * SLOW : ticks;
    }

    /** A spoke of the spiral laid out round the bore's axis, as the original turned its vector. */
    private static Vec3 turned(Vec3 v, float angle, Direction f) {
        float c = Mth.cos(angle);
        float s = Mth.sin(angle);
        v = new Vec3(v.x * c + v.y * s, v.y * c - v.x * s, v.z);
        float yaw = Mth.HALF_PI * f.getStepX();
        c = Mth.cos(yaw);
        s = Mth.sin(yaw);
        v = new Vec3(v.x * c + v.z * s, v.y, v.z * c - v.x * s);
        float pitch = Mth.HALF_PI * f.getStepY();
        c = Mth.cos(pitch);
        s = Mth.sin(pitch);
        return new Vec3(v.x, v.y * c + v.z * s, v.z * c - v.y * s);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == DUG) {
            sinceDug = 0;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    // --- what the renderer and the screen read -------------------------------

    /** The block it is going for, synced so the client can aim the head and the beam at it. */
    @Nullable
    public BlockPos target() {
        return dig;
    }

    public boolean working() {
        return sinceDug < 40;
    }

    public float spin(float partial) {
        return spin + (working() ? 10.0F * partial : 0.0F);
    }

    public boolean hasFocus() {
        return isFocus(contents.get(SLOT_FOCUS));
    }

    public int width() {
        return 1 + (RADIUS + area) * 2;
    }

    public int speedBonus() {
        return speed;
    }

    public int fortune() {
        return fortune;
    }

    public int refining() {
        return InfusionEnchantments.level(contents.get(SLOT_PICK), InfusionEnchantment.REFINING)
                + (FocusUpgrades.has(contents.get(SLOT_FOCUS), FocusUpgrade.DOWSING) ? 1 : 0);
    }

    public boolean silk() {
        return level != null && (FocusUpgrades.has(contents.get(SLOT_FOCUS), FocusUpgrade.SILK_TOUCH)
                || EnchantmentHelper.getItemEnchantmentLevel(enchantment(level, Enchantments.SILK_TOUCH),
                        contents.get(SLOT_PICK)) > 0);
    }

    /** Works out again what the focus and the pickaxe add, whenever either changes. */
    private void reckon() {
        ItemStack focus = contents.get(SLOT_FOCUS);
        ItemStack pick = contents.get(SLOT_PICK);
        area = 0;
        speed = 0;
        fortune = 0;
        if (isFocus(focus)) {
            area = FocusUpgrades.level(focus, FocusUpgrade.ENLARGE);
            speed = FocusUpgrades.level(focus, FocusUpgrade.POTENCY);
            fortune = FocusUpgrades.level(focus, FocusUpgrade.TREASURE);
        }
        if (isPick(pick) && level != null) {
            fortune = Math.max(fortune,
                    EnchantmentHelper.getItemEnchantmentLevel(enchantment(level, Enchantments.FORTUNE), pick));
            speed += EnchantmentHelper.getItemEnchantmentLevel(enchantment(level, Enchantments.EFFICIENCY), pick);
        }
    }

    /** Turns it to dig the other way: the spiral starts again. */
    public void aim(Direction toward) {
        if (level == null) {
            return;
        }
        level.setBlock(worldPosition, getBlockState().setValue(ArcaneBoreBlock.FACING, toward), Block.UPDATE_ALL);
        lastColumn = BlockPos.ZERO;
        toDig = false;
        radiusStep = 0.0F;
        dig = null;
        setChanged();
    }

    // --- the two slots -----------------------------------------------------

    public NonNullList<ItemStack> contents() {
        return contents;
    }

    @Override
    public int getContainerSize() {
        return contents.size();
    }

    @Override
    public boolean isEmpty() {
        return contents.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return contents.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = ContainerHelper.removeItem(contents, slot, amount);
        changed();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(contents, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        contents.set(slot, stack);
        changed();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_FOCUS ? isFocus(stack) : isPick(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 64.0;
    }

    @Override
    public void clearContent() {
        contents.clear();
    }

    /** What it holds changes what it mines with, and what the client draws; both are told. */
    private void changed() {
        reckon();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.alchemia.arcane_bore");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ArcaneBoreMenu(id, inventory, this);
    }

    // --- saving ------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents.clear();
        ContainerHelper.loadAllItems(tag, contents, registries);
        speedy = tag.getShort("SpeedyTime");
        powered = (int) Math.ceil(speedy);
        dig = tag.contains("dig") ? BlockPos.of(tag.getLong("dig")) : null;
        if (level != null && level.isClientSide && dig != null && !dig.equals(lastDug)) {
            lastDug = dig;
        }
        reckon();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, contents, registries);
        tag.putShort("SpeedyTime", (short) speedy);
        if (dig != null) {
            tag.putLong("dig", dig.asLong());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        reckon();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
