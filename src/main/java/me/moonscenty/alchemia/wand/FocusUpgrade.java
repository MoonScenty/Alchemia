package me.moonscenty.alchemia.wand;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum FocusUpgrade implements StringRepresentable {
    POTENCY("potency", () -> AspectList.of(ModAspects.AVERSION, 1)),
    FRUGAL("frugal", () -> AspectList.of(ModAspects.DESIRE, 1)),
    TREASURE("treasure", () -> AspectList.of(ModAspects.DESIRE, 1)),
    ENLARGE("enlarge", () -> AspectList.of(ModAspects.MOTION, 1)),
    EXTEND("extend", () -> AspectList.of(ModAspects.EXCHANGE, 1)),
    SILK_TOUCH("silk_touch", () -> AspectList.of(ModAspects.DESIRE, 1)),
    ARCHITECT("architect", () -> AspectList.of(ModAspects.CRAFT, 1)),
    ALCHEMISTS_FIRE("alchemists_fire", () -> AspectList.of(ModAspects.ENERGY, 1).add(ModAspects.WATER, 1)),
    ALCHEMISTS_FROST("alchemists_frost", () -> AspectList.of(ModAspects.COLD, 1).add(ModAspects.TRAP, 1)),

    FIREBALL("fireball", () -> AspectList.of(ModAspects.DARKNESS, 1)),
    FIREBEAM("firebeam", () -> AspectList.of(ModAspects.ENERGY, 1).add(ModAspects.AIR, 1)),
    SCATTERSHOT("scattershot", () -> AspectList.of(ModAspects.COLD, 1).add(ModAspects.AVERSION, 1)),
    ICE_BOULDER("ice_boulder", () -> AspectList.of(ModAspects.COLD, 1).add(ModAspects.CRYSTAL, 1)),
    CHAIN_LIGHTNING("chain_lightning", () -> AspectList.of(ModAspects.ENERGY, 1)),
    EARTH_SHOCK("earth_shock", () -> AspectList.of(ModAspects.ENERGY, 1)),
    BAT_BOMBS("bat_bombs", () -> AspectList.of(ModAspects.ENERGY, 1).add(ModAspects.TRAP, 1)),
    DEVIL_BATS("devil_bats", () -> AspectList.of(ModAspects.PROTECT, 1)),
    VAMPIRE_BATS("vampire_bats", () -> AspectList.of(ModAspects.DESIRE, 1).add(ModAspects.LIFE, 1)),
    NIGHTSHADE("nightshade", () -> AspectList.of(ModAspects.LIFE, 1).add(ModAspects.FLUX, 1)
            .add(ModAspects.ENERGY, 1)),
    SEEKER("seeker", () -> AspectList.of(ModAspects.SENSES, 1).add(ModAspects.MIND, 1)),
    PERSISTENT("persistent", () -> AspectList.of(ModAspects.PROTECT, 1).add(ModAspects.AVERSION, 1)
            .add(ModAspects.ENERGY, 1)),
    DOWSING("dowsing", () -> AspectList.of(ModAspects.CRAFT, 1)),
    STICKY("sticky", () -> AspectList.of(ModAspects.TRAP, 1));

    public static final Codec<FocusUpgrade> CODEC = StringRepresentable.fromEnum(FocusUpgrade::values);
    public static final StreamCodec<io.netty.buffer.ByteBuf, FocusUpgrade> STREAM_CODEC =
            ByteBufCodecs.idMapper(which -> values()[which], FocusUpgrade::ordinal);

    private final String name;
    private final Supplier<AspectList> aspects;

    FocusUpgrade(String name, Supplier<AspectList> aspects) {
        this.name = name;
        this.aspects = aspects;
    }

    // built lazily: aspects aren't registered yet when this enum loads
    public AspectList aspects() {
        return aspects.get();
    }

    public ResourceLocation icon() {
        return Alchemia.id("textures/gui/focus_upgrade/" + name + ".png");
    }

    public String translationKey() {
        return "focus_upgrade." + Alchemia.MODID + "." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
