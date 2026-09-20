package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds what has been left out on a research table: a set of scribing tools, and the note being worked through.
 * <p>
 * Both are visible on the desk, so whenever they change the block state is updated to match and the model picks up
 * the difference.
 */
public class ResearchTableBlockEntity extends BlockEntity {
    public static final int SLOT_TOOLS = 0;
    public static final int SLOT_NOTES = 1;

    private final NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);

    public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_TABLE.get(), pos, state);
    }

    public ItemStack get(int slot) {
        return contents.get(slot);
    }

    /** Puts something down on the desk, handing back whatever was there before. */
    public ItemStack put(int slot, ItemStack stack) {
        ItemStack previous = contents.get(slot);
        contents.set(slot, stack);
        setChanged();
        showWhatIsOnTheDesk();
        return previous;
    }

    public boolean accepts(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_TOOLS -> stack.is(ModItems.SCRIBING_TOOLS.get());
            case SLOT_NOTES -> stack.is(ModItems.RESEARCH_NOTES.get());
            default -> false;
        };
    }

    /** Spends a point of ink, and clears the tools away once they run dry. */
    public boolean useInk() {
        ItemStack tools = contents.get(SLOT_TOOLS);
        if (tools.isEmpty()) {
            return false;
        }
        if (tools.getDamageValue() + 1 >= tools.getMaxDamage()) {
            contents.set(SLOT_TOOLS, ItemStack.EMPTY);
        } else {
            tools.setDamageValue(tools.getDamageValue() + 1);
        }
        setChanged();
        showWhatIsOnTheDesk();
        return true;
    }

    private void showWhatIsOnTheDesk() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState updated = getBlockState()
                .setValue(ResearchTableBlock.HAS_TOOLS, !contents.get(SLOT_TOOLS).isEmpty())
                .setValue(ResearchTableBlock.HAS_NOTES, !contents.get(SLOT_NOTES).isEmpty());
        if (updated != getBlockState()) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        }
    }

    /** Everything left on the desk, for when the block is broken. */
    public NonNullList<ItemStack> contents() {
        return contents;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents.clear();
        ContainerHelper.loadAllItems(tag, contents, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, contents, registries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void drop(Level level, BlockPos pos, ResearchTableBlockEntity table) {
        net.minecraft.world.Containers.dropContents(level, pos, table.contents());
    }
}
