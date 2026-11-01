package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.block.entity.SyncedBlockEntity;
import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * What a banner is: which way it hangs, whether from a wall or on its own pole, its colour -- none for the Crimson
 * Cult's -- and the aspect painted on it, if any.
 */
public class BannerBlockEntity extends SyncedBlockEntity {
    /** One of sixteen turns standing, or one of four hanging. */
    private byte facing;
    private boolean wall;
    /** Null for the cult's. */
    @Nullable
    private DyeColor colour;
    @Nullable
    private Holder<Aspect> aspect;

    public BannerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANNER.get(), pos, state);
    }

    public int facing() {
        return facing;
    }

    public boolean onWall() {
        return wall;
    }

    @Nullable
    public DyeColor colour() {
        return colour;
    }

    /** The Crimson Cult's banner: no colour of its own, and nothing may be painted on it. */
    public boolean isCult() {
        return colour == null;
    }

    @Nullable
    public Holder<Aspect> aspect() {
        return aspect;
    }

    public void hang(int facing, boolean wall) {
        this.facing = (byte) facing;
        this.wall = wall;
        changed();
    }

    public void paint(@Nullable Holder<Aspect> aspect) {
        this.aspect = aspect;
        changed();
    }

    public void setColour(@Nullable DyeColor colour) {
        this.colour = colour;
        changed();
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        colour = input.get(DataComponents.BASE_COLOR);
        aspect = input.get(ModDataComponents.ESSENTIA.get());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        if (colour != null) {
            builder.set(DataComponents.BASE_COLOR, colour);
        }
        if (aspect != null) {
            builder.set(ModDataComponents.ESSENTIA.get(), aspect);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("facing", facing);
        tag.putBoolean("wall", wall);
        tag.putByte("color", (byte) (colour == null ? -1 : colour.getId()));
        if (aspect != null) {
            tag.putString("aspect", aspect.unwrapKey().orElseThrow().location().toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        facing = tag.getByte("facing");
        wall = tag.getBoolean("wall");
        int id = tag.getByte("color");
        colour = id < 0 ? null : DyeColor.byId(id);
        aspect = tag.contains("aspect") ? ModAspects.byId(tag.getString("aspect")) : null;
    }

    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0.0, 1.0, 0.0).expandTowards(0.0, -1.0, 0.0);
    }
}
