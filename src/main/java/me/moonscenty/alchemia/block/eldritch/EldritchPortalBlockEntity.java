package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.block.entity.SyncedBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Whether the way is open, and how far it has opened, for the drawing. In the Outer Lands it is always open. Every
 * quarter second it takes whoever stands in it through, to the Outer Lands or back out of them.
 */
public class EldritchPortalBlockEntity extends SyncedBlockEntity {
    private boolean open;
    /** How long it has stood open, counted on the client up to thirty for the drawing. */
    public int opened = -1;
    private int count;

    public EldritchPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELDRITCH_PORTAL.get(), pos, state);
    }

    /** A portal the client has loaded is one a sinister lodestone can feel, open or shut. */
    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) {
            me.moonscenty.alchemia.item.SinisterStoneItem.PORTALS.add(
                    net.minecraft.core.GlobalPos.of(level.dimension(), worldPosition));
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide) {
            me.moonscenty.alchemia.item.SinisterStoneItem.PORTALS.remove(
                    net.minecraft.core.GlobalPos.of(level.dimension(), worldPosition));
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
        changed();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EldritchPortalBlockEntity portal) {
        portal.count++;
        if (!portal.open && me.moonscenty.alchemia.outer.OuterLands.isOuterLands(level)) {
            portal.open = true;
        }
        if (!portal.open) {
            return;
        }
        if (!level.isClientSide) {
            if (portal.count % 5 == 0) {
                for (net.minecraft.server.level.ServerPlayer player : level.getEntitiesOfClass(
                        net.minecraft.server.level.ServerPlayer.class, new AABB(pos).inflate(0.5, 1.0, 0.5))) {
                    me.moonscenty.alchemia.outer.OuterLands.travel(player, pos);
                }
            }
            return;
        }
        if (portal.count % 250 == 0 || portal.count == 1) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    me.moonscenty.alchemia.registry.ModSounds.EVIL_PORTAL.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        if (portal.opened < 30) {
            portal.opened++;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("open", open);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        open = tag.getBoolean("open");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("open", open);
        return tag;
    }

    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1.0);
    }
}
