package me.moonscenty.alchemia.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.CrystalType;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Alchemia.MODID);

    public static final DeferredBlock<Block> AMBER_ORE = register("amber_ore",
            () -> new DropExperienceBlock(UniformInt.of(1, 4), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> DEEPSLATE_AMBER_ORE = register("deepslate_amber_ore",
            () -> new DropExperienceBlock(UniformInt.of(1, 4), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final DeferredBlock<Block> CINNABAR_ORE = register("cinnabar_ore",
            () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> DEEPSLATE_CINNABAR_ORE = register("deepslate_cinnabar_ore",
            () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));

    public static final Map<CrystalType, DeferredBlock<CrystalBlock>> CRYSTALS = registerCrystals();

    private static Map<CrystalType, DeferredBlock<CrystalBlock>> registerCrystals() {
        Map<CrystalType, DeferredBlock<CrystalBlock>> crystals = new EnumMap<>(CrystalType.class);
        for (CrystalType type : CrystalType.values()) {
            crystals.put(type, register(type.getName() + "_crystal", () -> new CrystalBlock(type, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(0.25F)
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(CrystalBlock::getLightLevel)
                    .forceSolidOn()
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY))));
        }
        return Collections.unmodifiableMap(crystals);
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }
}
