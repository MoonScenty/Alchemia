package me.moonscenty.alchemia.research;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ResearchNote(ResourceLocation research, AspectList budget, List<Cell> cells, boolean complete) {
    public static final Codec<ResearchNote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("research").forGetter(ResearchNote::research),
            AspectList.CODEC.fieldOf("budget").forGetter(ResearchNote::budget),
            Cell.CODEC.listOf().fieldOf("cells").forGetter(ResearchNote::cells),
            Codec.BOOL.optionalFieldOf("complete", false).forGetter(ResearchNote::complete))
            .apply(instance, ResearchNote::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchNote> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ResearchNote::research,
            AspectList.STREAM_CODEC, ResearchNote::budget,
            Cell.STREAM_CODEC.apply(ByteBufCodecs.list()), ResearchNote::cells,
            ByteBufCodecs.BOOL, ResearchNote::complete,
            ResearchNote::new);

    // a hex missing from the list is torn out of the sheet
    public record Cell(HexGrid.Hex at, Optional<Holder<Aspect>> aspect, boolean pinned) {
        public static final Codec<Cell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                HexGrid.Hex.CODEC.fieldOf("at").forGetter(Cell::at),
                ModAspects.REGISTRY.holderByNameCodec().optionalFieldOf("aspect").forGetter(Cell::aspect),
                Codec.BOOL.optionalFieldOf("pinned", false).forGetter(Cell::pinned))
                .apply(instance, Cell::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Cell> STREAM_CODEC = StreamCodec.composite(
                HexGrid.Hex.STREAM_CODEC, Cell::at,
                ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(ModAspects.KEY)), Cell::aspect,
                ByteBufCodecs.BOOL, Cell::pinned,
                Cell::new);

        public static Cell blank(HexGrid.Hex at) {
            return new Cell(at, Optional.empty(), false);
        }

        public static Cell pinned(HexGrid.Hex at, Holder<Aspect> aspect) {
            return new Cell(at, Optional.of(aspect), true);
        }

        public boolean isEmpty() {
            return aspect.isEmpty();
        }
    }

    public List<Cell> pinnedCells() {
        return cells.stream().filter(Cell::pinned).toList();
    }

    public Optional<Cell> cellAt(HexGrid.Hex at) {
        return cells.stream().filter(cell -> cell.at().equals(at)).findFirst();
    }

    public boolean holds(HexGrid.Hex at) {
        return cellAt(at).isPresent();
    }
}
