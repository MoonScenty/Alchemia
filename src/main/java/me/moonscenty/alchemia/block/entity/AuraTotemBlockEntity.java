package me.moonscenty.alchemia.block.entity;

import org.joml.Vector3f;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.ShardTuning;
import me.moonscenty.alchemia.block.AuraTotemBlock;
import me.moonscenty.alchemia.menu.AuraTotemMenu;
import me.moonscenty.alchemia.particle.SparkleOptions;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An aura totem at work.
 *
 * <p>It burns a vis shard for five hundred steps, and the shard says what it moves: the one aspect, or a primal
 * picked at random each step for a balanced shard. A step comes every ten ticks, a tick sooner for every inner or
 * outer pole under it. Each step it takes a point of the aspect from a chunk of one ring and, if there was a point to
 * take, puts it in a chunk of the other -- or, now and then, spoils it into flux in its own chunk instead. The more
 * poles, the likelier that is; stabilising poles make it less so.
 */
public class AuraTotemBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    /** How many steps a shard lasts, and how long a step is at the slowest. The original's. */
    public static final int LASTS = 500;
    private static final int SLOWEST = 10;
    /** How likely a step is to spoil, per pole and to begin with, and how much each stabilising pole takes off. */
    private static final float SPOILS = 0.0125F;
    private static final float PURE_PUSHING = 0.1F;
    private static final float PURE_PULLING = 0.025F;
    /** The most tries at a chunk of the outer ring before giving the step up. */
    private static final int TRIES = 100;

    public static final int READING_TIME = 0;
    public static final int READING_TUNING = 1;
    public static final int READINGS = 2;

    private ItemStack shards = ItemStack.EMPTY;
    private int tuning = ShardTuning.NONE;
    private int time;
    private int count;
    private int inner;
    private int outer;
    private int pure;

    private final ContainerData readings = new ContainerData() {
        @Override
        public int get(int index) {
            return index == READING_TIME ? time : index == READING_TUNING ? tuning : 0;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return READINGS;
        }
    };

    public AuraTotemBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AURA_TOTEM.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AuraTotemBlockEntity totem) {
        totem.count++;
        totem.countPoles();
        if (!level.isClientSide) {
            if (totem.count % totem.every() == 0) {
                totem.step(level);
            }
        } else if (totem.tuning != ShardTuning.NONE && totem.count % 20 == 0) {
            totem.sparkle(level);
        }
    }

    /** How many ticks a step takes, with the poles under it. */
    public int every() {
        return SLOWEST - (inner + outer);
    }

    /** The poles under it, counted down to the first block that is not one, five at most. */
    public void countPoles() {
        inner = 0;
        outer = 0;
        pure = 0;
        BlockPos at = worldPosition.below();
        for (int counted = 0; counted < AuraTotemBlock.MOST_POLES; counted++) {
            if (!(level.getBlockState(at).getBlock() instanceof AuraTotemBlock pole) || !pole.kind().isPole()) {
                return;
            }
            switch (pole.kind()) {
                case POLE_INNER -> inner++;
                case POLE_OUTER -> outer++;
                default -> pure++;
            }
            at = at.below();
        }
    }

    private void step(Level level) {
        if (time > 0) {
            time--;
            move(level);
            if (time == 0) {
                setChanged();
            }
            return;
        }
        int was = tuning;
        tuning = ShardTuning.of(shards);
        if (tuning != ShardTuning.NONE) {
            shards.shrink(1);
            time = LASTS;
        }
        if (tuning != was) {
            changed();
        }
    }

    /** One point of the aspect from one ring to the other. */
    private void move(Level level) {
        RandomSource random = level.random;
        int innerX = Mth.nextInt(random, -inner, inner);
        int innerZ = Mth.nextInt(random, -inner, inner);
        int reach = inner + outer + 1;
        int outerX = 0;
        int outerZ = 0;
        int tries = 0;
        while (tries < TRIES && Math.abs(outerX) <= inner && Math.abs(outerZ) <= inner) {
            outerX = Mth.nextInt(random, -reach, reach);
            outerZ = Mth.nextInt(random, -reach, reach);
            tries++;
        }
        if (tries >= TRIES) {
            return;
        }
        boolean pushing = kind() == AuraTotemBlock.Kind.PUSH;
        BlockPos innerChunk = worldPosition.offset(innerX * 16, 0, innerZ * 16);
        BlockPos outerChunk = worldPosition.offset(outerX * 16, 0, outerZ * 16);
        BlockPos from = pushing ? innerChunk : outerChunk;
        BlockPos into = pushing ? outerChunk : innerChunk;
        Holder<Aspect> aspect = ShardTuning.aspect(tuning);
        if (aspect == null) {
            aspect = ModAspects.primals().get(random.nextInt(ModAspects.primals().size()));
        }
        if (AuraHandler.drain(level, from, aspect, 1)) {
            float spoils = SPOILS * (inner + outer + 2) - (pushing ? PURE_PUSHING : PURE_PULLING) * pure;
            if (random.nextFloat() < spoils) {
                AuraHandler.add(level, worldPosition, ModAspects.FLUX, 1);
            } else {
                AuraHandler.add(level, into, aspect, 1);
            }
        }
    }

    /** A spark somewhere down the stack, and a mote of vis drawn in to it or sent away from it. */
    private void sparkle(Level level) {
        RandomSource random = level.random;
        int down = random.nextInt(inner + outer + 1);
        double x = worldPosition.getX() + random.nextFloat();
        double y = worldPosition.getY() - down + random.nextFloat();
        double z = worldPosition.getZ() + random.nextFloat();
        level.addParticle(new SparkleOptions(0xA6FFFF, new Vector3f((float) x, (float) y + 0.1F, (float) z)),
                x, y, z, 0, 0, 0);

        Holder<Aspect> aspect = ShardTuning.aspect(tuning);
        int colour = aspect != null ? aspect.value().color()
                : Mth.color(0.3F + random.nextFloat() * 0.7F, 0.3F + random.nextFloat() * 0.7F,
                        0.3F + random.nextFloat() * 0.7F);
        float spread = 5 + outer;
        Vector3f away = new Vector3f(worldPosition.getX() + (random.nextFloat() - random.nextFloat()) * spread,
                worldPosition.getY() + random.nextInt(4) - random.nextInt(3) + random.nextFloat(),
                worldPosition.getZ() + (random.nextFloat() - random.nextFloat()) * spread);
        Vector3f here = new Vector3f(worldPosition.getX() + random.nextFloat(),
                worldPosition.getY() + random.nextFloat(), worldPosition.getZ() + random.nextFloat());
        boolean pushing = kind() == AuraTotemBlock.Kind.PUSH;
        Vector3f start = pushing ? here : away;
        Vector3f end = pushing ? away : here;
        level.addParticle(new SparkleOptions(colour, end), start.x, start.y, start.z, 0, 0, 0);
    }

    public AuraTotemBlock.Kind kind() {
        return getBlockState().getBlock() instanceof AuraTotemBlock totem ? totem.kind() : AuraTotemBlock.Kind.PUSH;
    }

    public int tuning() {
        return tuning;
    }

    public int time() {
        return time;
    }

    public int inner() {
        return inner;
    }

    public int outer() {
        return outer;
    }

    public int pure() {
        return pure;
    }

    /** The colour its glow takes: the aspect it moves, white for balance, nearly black when it is idle. */
    public int glow() {
        Holder<Aspect> aspect = ShardTuning.aspect(tuning);
        return aspect != null ? aspect.value().color() : tuning == ShardTuning.BALANCED ? 0xF0F0F0 : 0x101010;
    }

    /** Saved, and told to anyone watching, whose game has to colour it again. */
    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- the shard pocket ------------------------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return shards.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return shards;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = shards.split(amount);
        setChanged();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack taken = shards;
        shards = ItemStack.EMPTY;
        setChanged();
        return taken;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        shards = stack;
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return ShardTuning.isShard(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        shards = ItemStack.EMPTY;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[] {0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    // --- the screen ------------------------------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AuraTotemMenu(id, inventory, this, readings);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        shards = tag.contains("shards")
                ? ItemStack.parse(registries, tag.getCompound("shards")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        tuning = tag.contains("tuning") ? tag.getByte("tuning") : ShardTuning.NONE;
        time = tag.getInt("time");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!shards.isEmpty()) {
            tag.put("shards", shards.save(registries));
        }
        tag.putByte("tuning", (byte) tuning);
        tag.putInt("time", time);
    }

    /** Only what it is tuned to is shown to anyone watching; the rest is the screen's business. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("tuning", (byte) tuning);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        tuning = tag.contains("tuning") ? tag.getByte("tuning") : ShardTuning.NONE;
        recolour();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }

    /** The block drawn again, in its new colour. */
    private void recolour() {
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }
}
