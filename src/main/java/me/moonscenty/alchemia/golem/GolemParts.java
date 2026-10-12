package me.moonscenty.alchemia.golem;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/**
 * Everything a golem can be pressed from: what it is made of, and the head, arms, legs and fitting it is given.
 *
 * <p>The parts, their traits, what they take and the research each waits on are all the original's. A part asks for
 * things outright, or for "the base" and "the mechanism" of whatever the golem is made of, so that a wooden golem's
 * legs take planks and a brass one's take plate.
 */
public final class GolemParts {
    private GolemParts() {
    }

    /** Where on the body a part's mesh is hung, and so what moves it. */
    public enum Attach {
        HEAD,
        ARMS,
        LEGS,
        BODY
    }

    /**
     * How a part looks: one of the original's meshes and the picture laid over it. A part with no picture of its
     * own is drawn wholly in its golem's material, and every part draws its groups named {@code bm...} in it.
     */
    public record Look(String mesh, @Nullable String texture, Attach attach) {
        public String meshPath() {
            return "models/legacy/golem/" + mesh + ".obj";
        }

        @Nullable
        public ResourceLocation textureLocation() {
            return texture == null ? null : Alchemia.id("textures/entity/golem/" + texture + ".png");
        }
    }

    /** One thing a part takes. */
    public sealed interface Need permits Stack, Of {
    }

    /** So many of a given item. */
    public record Stack(Supplier<? extends ItemLike> item, int count) implements Need {
    }

    /** One of whatever the golem is made of. */
    public enum Of implements Need {
        BASE,
        MECHANISM
    }

    /** What every kind of part has. */
    public interface Part {
        String key();

        /** All of these must be known to choose it at the press. */
        List<String> research();

        List<GolemTrait> traits();

        List<Need> needs();

        @Nullable
        Look look();

        /** The family of parts it is one of, for its name: material, head, arm, leg or addon. */
        String family();

        default String translationKey() {
            return "golem." + Alchemia.MODID + "." + family() + "." + key();
        }

        default String descriptionKey() {
            return translationKey() + ".text";
        }

        /** The little picture the press shows it by. */
        default ResourceLocation icon() {
            return Alchemia.id("textures/gui/golem/" + family() + "_" + key() + ".png");
        }

        default boolean known(Player player) {
            PlayerKnowledge knowledge = PlayerKnowledge.of(player);
            return research().stream().allMatch(name -> knowledge.hasResearch(Alchemia.id(name)));
        }
    }

    private static Stack of(Supplier<? extends ItemLike> item) {
        return new Stack(item, 1);
    }

    private static Stack of(Supplier<? extends ItemLike> item, int count) {
        return new Stack(item, count);
    }

    /** What the frame is made of: how much it takes, how hard it hits, and some of its traits. */
    public enum Material implements Part {
        WOOD("golem_wood", 0x4D332A, 6, 2, 1, () -> ModBlocks.GREATWOOD.planks().get(),
                () -> ModItems.BRASS_GEAR.get(), GolemTrait.LIGHT),
        IRON("golem_iron", 0xFFFFFF, 20, 8, 3, () -> ModItems.IRON_PLATE.get(), () -> ModItems.BRASS_GEAR.get(),
                GolemTrait.HEAVY, GolemTrait.FIREPROOF, GolemTrait.BLASTPROOF),
        CLAY("golem_clay", 0xC77457, 10, 4, 2, () -> Blocks.TERRACOTTA, () -> ModItems.BRASS_GEAR.get(),
                GolemTrait.FIREPROOF),
        BRASS("golem_brass", 0xEEA31C, 16, 6, 3, () -> ModItems.BRASS_PLATE.get(), () -> ModItems.BRASS_GEAR.get(),
                GolemTrait.LIGHT),
        ALCHEMIUM("golem_alchemium", 0x503772, 24, 10, 4, () -> ModItems.ALCHEMIUM_PLATE.get(),
                () -> ModItems.ALCHEMIUM_GEAR.get(), GolemTrait.HEAVY, GolemTrait.FIREPROOF, GolemTrait.BLASTPROOF),
        VOID("golem_void", 0x160D29, 20, 6, 4, () -> ModItems.VOID_PLATE.get(), () -> ModItems.VOID_GEAR.get(),
                GolemTrait.REPAIR);

        private final String research;
        private final int colour;
        private final int health;
        private final int armour;
        private final int damage;
        private final Supplier<? extends ItemLike> base;
        private final Supplier<? extends ItemLike> mechanism;
        private final List<GolemTrait> traits;

        Material(String research, int colour, int health, int armour, int damage, Supplier<? extends ItemLike> base,
                Supplier<? extends ItemLike> mechanism, GolemTrait... traits) {
            this.research = research;
            this.colour = colour;
            this.health = health;
            this.armour = armour;
            this.damage = damage;
            this.base = base;
            this.mechanism = mechanism;
            this.traits = List.of(traits);
        }

        @Override
        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public List<String> research() {
            return List.of(research);
        }

        @Override
        public List<GolemTrait> traits() {
            return traits;
        }

        /** A frame takes two of its base and one of its mechanism, before any part asks for more. */
        @Override
        public List<Need> needs() {
            return List.of(Of.BASE, Of.BASE, Of.MECHANISM);
        }

        @Override
        @Nullable
        public Look look() {
            return null;
        }

        @Override
        public String family() {
            return "material";
        }

        /** What the golem item is tinted. */
        public int colour() {
            return colour;
        }

        /** Added to a golem's ten. */
        public int health() {
            return health;
        }

        public int armour() {
            return armour;
        }

        /** How hard it hits, if it fights at all. */
        public int damage() {
            return damage;
        }

        public ItemStack base() {
            return new ItemStack(base.get());
        }

        public ItemStack mechanism() {
            return new ItemStack(mechanism.get());
        }

        /** The picture the whole frame is drawn in. */
        public ResourceLocation texture() {
            return Alchemia.id("textures/entity/golem/mat_" + key() + ".png");
        }
    }

    public enum Head implements Part {
        BASIC(List.of("clockwork_mind"), new Look("golem_head_basic", null, Attach.HEAD),
                List.of(of(() -> ModItems.CLOCKWORK_MIND.get()))),
        SMART(List.of("bioalchemic_mind"), new Look("golem_head_smart", "golem_head_other", Attach.HEAD),
                List.of(of(() -> ModItems.BIOALCHEMIC_MIND.get())), GolemTrait.SMART, GolemTrait.FRAGILE),
        SMART_ARMORED(List.of("bioalchemic_mind", "golem_combat"),
                new Look("golem_head_smart_armor", null, Attach.HEAD),
                List.of(of(() -> ModItems.BIOALCHEMIC_MIND.get()), of(() -> ModItems.BRASS_PLATE.get()), Of.BASE,
                        of(() -> Blocks.WHITE_WOOL)), GolemTrait.SMART),
        SCOUT(List.of("golem_vision"), new Look("golem_head_scout", "golem_head_other", Attach.HEAD),
                List.of(of(() -> ModItems.CLOCKWORK_MIND.get()), of(() -> ModItems.VISION_MODULE.get())),
                GolemTrait.SCOUT, GolemTrait.FRAGILE),
        SMART_SCOUT(List.of("golem_vision", "bioalchemic_mind"),
                new Look("golem_head_scout_smart", "golem_head_other", Attach.HEAD),
                List.of(of(() -> ModItems.BIOALCHEMIC_MIND.get()), of(() -> ModItems.VISION_MODULE.get())),
                GolemTrait.SCOUT, GolemTrait.SMART, GolemTrait.FRAGILE);

        private final List<String> research;
        private final Look look;
        private final List<Need> needs;
        private final List<GolemTrait> traits;

        Head(List<String> research, Look look, List<Need> needs, GolemTrait... traits) {
            this.research = research;
            this.look = look;
            this.needs = needs;
            this.traits = List.of(traits);
        }

        @Override
        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public List<String> research() {
            return research;
        }

        @Override
        public List<GolemTrait> traits() {
            return traits;
        }

        @Override
        public List<Need> needs() {
            return needs;
        }

        @Override
        public Look look() {
            return look;
        }

        @Override
        public String family() {
            return "head";
        }
    }

    public enum Arms implements Part {
        BASIC(List.of("clockwork_mind"), new Look("golem_arms_basic", null, Attach.ARMS), List.of()),
        FINE(List.of("golem_brass"), new Look("golem_arms_fine", null, Attach.ARMS),
                List.of(of(() -> ModItems.BRASS_GEAR.get(), 2), Of.BASE), GolemTrait.DEFT, GolemTrait.FRAGILE),
        CLAWS(List.of("golem_combat"), new Look("golem_arms_claws", "golem_arms_claws", Attach.ARMS),
                List.of(of(() -> ModItems.AGGRESSION_MODULE.get()), of(() -> Items.SHEARS, 2), Of.BASE),
                GolemTrait.FIGHTER, GolemTrait.CLUMSY, GolemTrait.BRUTAL),
        BREAKERS(List.of("golem_breaker"), new Look("golem_arms_breakers", "golem_arms_breakers", Attach.ARMS),
                List.of(of(() -> Items.DIAMOND, 2), Of.BASE, of(() -> Blocks.PISTON, 2)),
                GolemTrait.BREAKER, GolemTrait.CLUMSY, GolemTrait.BRUTAL),
        DARTS(List.of("golem_combat"), new Look("golem_arms_darter", "golem_arms_darter", Attach.ARMS),
                List.of(of(() -> ModItems.AGGRESSION_MODULE.get()), of(() -> Blocks.DISPENSER, 2),
                        of(() -> Items.ARROW, 32), Of.MECHANISM),
                GolemTrait.FIGHTER, GolemTrait.CLUMSY, GolemTrait.RANGED, GolemTrait.FRAGILE);

        private final List<String> research;
        private final Look look;
        private final List<Need> needs;
        private final List<GolemTrait> traits;

        Arms(List<String> research, Look look, List<Need> needs, GolemTrait... traits) {
            this.research = research;
            this.look = look;
            this.needs = needs;
            this.traits = List.of(traits);
        }

        @Override
        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public List<String> research() {
            return research;
        }

        @Override
        public List<GolemTrait> traits() {
            return traits;
        }

        @Override
        public List<Need> needs() {
            return needs;
        }

        @Override
        public Look look() {
            return look;
        }

        @Override
        public String family() {
            return "arm";
        }
    }

    public enum Legs implements Part {
        WALKER(List.of("clockwork_mind"), new Look("golem_legs_walker", null, Attach.LEGS),
                List.of(Of.BASE, Of.MECHANISM)),
        ROLLER(List.of("clockwork_mind"), new Look("golem_legs_wheel", "golem_legs_wheel", Attach.BODY),
                List.of(of(() -> Items.BOWL, 2), of(() -> Items.LEATHER), Of.MECHANISM), GolemTrait.WHEELED),
        CLIMBER(List.of("golem_climber"), new Look("golem_legs_climber", "base_metal", Attach.LEGS),
                List.of(of(() -> Items.FLINT, 6), Of.BASE, Of.MECHANISM, Of.MECHANISM), GolemTrait.CLIMBER),
        FLYER(List.of("golem_flyer"), new Look("golem_legs_floater", "golem_legs_floater", Attach.BODY),
                List.of(of(() -> ModBlocks.LEVITATOR.get()), of(() -> ModItems.BRASS_PLATE.get(), 4),
                        of(() -> Items.SLIME_BALL), Of.MECHANISM), GolemTrait.FLYER, GolemTrait.FRAGILE);

        private final List<String> research;
        private final Look look;
        private final List<Need> needs;
        private final List<GolemTrait> traits;

        Legs(List<String> research, Look look, List<Need> needs, GolemTrait... traits) {
            this.research = research;
            this.look = look;
            this.needs = needs;
            this.traits = List.of(traits);
        }

        @Override
        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public List<String> research() {
            return research;
        }

        @Override
        public List<GolemTrait> traits() {
            return traits;
        }

        @Override
        public List<Need> needs() {
            return needs;
        }

        @Override
        public Look look() {
            return look;
        }

        @Override
        public String family() {
            return "leg";
        }
    }

    public enum Addon implements Part {
        NONE(List.of("clockwork_mind"), null, List.of()),
        ARMORED(List.of("golem_combat"), new Look("golem_armor", null, Attach.BODY),
                List.of(Of.BASE, Of.BASE, Of.BASE, Of.BASE), GolemTrait.ARMORED, GolemTrait.HEAVY),
        FIGHTER(List.of("seal_guard"), null,
                List.of(of(() -> ModItems.AGGRESSION_MODULE.get()), Of.MECHANISM), GolemTrait.FIGHTER),
        HAULER(List.of("clockwork_mind"), new Look("golem_hauler", "golem_hauler", Attach.BODY),
                List.of(of(() -> Items.LEATHER), of(() -> Blocks.CHEST)), GolemTrait.HAULER);

        private final List<String> research;
        @Nullable
        private final Look look;
        private final List<Need> needs;
        private final List<GolemTrait> traits;

        Addon(List<String> research, @Nullable Look look, List<Need> needs, GolemTrait... traits) {
            this.research = research;
            this.look = look;
            this.needs = needs;
            this.traits = List.of(traits);
        }

        @Override
        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public List<String> research() {
            return research;
        }

        @Override
        public List<GolemTrait> traits() {
            return traits;
        }

        @Override
        public List<Need> needs() {
            return needs;
        }

        @Override
        @Nullable
        public Look look() {
            return look;
        }

        @Override
        public String family() {
            return "addon";
        }
    }
}
