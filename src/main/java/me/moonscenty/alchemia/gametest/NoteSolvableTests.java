package me.moonscenty.alchemia.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.research.ModResearch;
import me.moonscenty.alchemia.research.NoteGeneration;
import me.moonscenty.alchemia.research.NoteSolving;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ResearchNote;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class NoteSolvableTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final int ROUNDS = 40;

    @GameTest(template = TEMPLATE)
    public static void everyNoteCanBeWorkedOut(GameTestHelper helper) {
        Registry<Aspect> aspects = ModAspects.REGISTRY;
        var research = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        List<String> beaten = new ArrayList<>();

        for (var holder : research.holders().toList()) {
            ResourceLocation id = holder.key().location();
            ResearchEntry entry = holder.value();
            if (entry.requirements().isEmpty()) {
                continue;
            }
            for (int round = 0; round < ROUNDS; round++) {
                RandomSource random = RandomSource.create(id.hashCode() * 31L + round);
                ResearchNote note = NoteGeneration.draw(id, entry, random);
                if (solve(aspects, note).isEmpty()) {
                    String why = NoteSolving.solve(aspects, note).isPresent()
                            ? "the sheet cannot pay for the answer" : "no chain of aspects fits the board";
                    beaten.add(id + " #" + round + " (" + why + ", pinned " + note.pinnedCells().stream()
                            .map(c -> c.aspect().map(a -> a.value().tag()).orElse("?") + "@" + c.at())
                            .toList() + ", budget " + note.budget() + ")");
                }
            }
        }

        helper.assertTrue(beaten.isEmpty(), beaten.size() + " notes could not be worked out at all: " + beaten);
        helper.succeed();
    }

    private static Optional<ResearchNote> solve(Registry<Aspect> aspects, ResearchNote note) {
        List<NoteSolving.Placement> answer = NoteSolving.solve(aspects, note).orElse(null);
        if (answer == null) {
            return Optional.empty();
        }
        ResearchNote playing = note;
        for (NoteSolving.Placement step : answer) {
            playing = afford(aspects, playing, step.aspect());
            if (playing == null || !NoteSolving.check(playing, step.at(), Optional.of(step.aspect())).allowed()) {
                return Optional.empty();
            }
            playing = NoteSolving.place(playing, step.at(), Optional.of(step.aspect()));
        }
        return NoteSolving.isSolved(playing) ? Optional.of(playing) : Optional.empty();
    }

    // --- paying for an aspect ---

    private static ResearchNote afford(Registry<Aspect> aspects, ResearchNote note, Holder<Aspect> want) {
        if (note.budget().get(want) > 0) {
            return note;
        }
        List<Holder<Aspect>> parts = want.value().components().orElse(List.of());
        if (parts.size() != 2) {
            return null;
        }
        ResearchNote playing = note;
        boolean twice = parts.get(0).value() == parts.get(1).value();
        for (Holder<Aspect> part : parts) {
            playing = afford(aspects, playing, part);
            if (playing == null) {
                return null;
            }
        }
        if (twice && playing.budget().get(parts.get(0)) < 2) {
            int had = playing.budget().get(parts.get(0));
            playing = new ResearchNote(playing.research(), playing.budget().withAmount(parts.get(0), had), playing.cells(), playing.complete());
            playing = afford(aspects, spend(playing, parts.get(0)), parts.get(0));
            if (playing == null || playing.budget().get(parts.get(0)) < 2) {
                return null;
            }
        }
        Optional<Holder<Aspect>> made = NoteSolving.mixOf(aspects, parts.get(0), parts.get(1));
        if (made.isEmpty() || !NoteSolving.checkMix(playing, parts.get(0), parts.get(1), made).allowed()) {
            return null;
        }
        return NoteSolving.mix(playing, parts.get(0), parts.get(1), made.get());
    }

    private static ResearchNote spend(ResearchNote note, Holder<Aspect> aspect) {
        return new ResearchNote(note.research(), note.budget().reduce(aspect, 1), note.cells(), note.complete());
    }
}
