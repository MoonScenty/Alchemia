package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.menu.ArcaneWorkbenchMenu;
import me.moonscenty.alchemia.menu.AutocasterMenu;
import me.moonscenty.alchemia.menu.EssentiaSmelterMenu;
import me.moonscenty.alchemia.menu.FocusPouchMenu;
import me.moonscenty.alchemia.menu.ResearchTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Alchemia.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ResearchTableMenu>> RESEARCH_TABLE =
            MENUS.register("research_table",
                    () -> new MenuType<>(ResearchTableMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<ArcaneWorkbenchMenu>> ARCANE_WORKBENCH =
            MENUS.register("arcane_workbench",
                    () -> new MenuType<>(ArcaneWorkbenchMenu::new, FeatureFlags.VANILLA_SET));

    /** Carries the block's position across, since the screen reads what is dissolved off the block itself. */
    /** Carries which pech is being traded with. */
    public static final DeferredHolder<MenuType<?>, MenuType<me.moonscenty.alchemia.menu.PechMenu>> PECH =
            MENUS.register("pech", () -> IMenuTypeExtension.create(me.moonscenty.alchemia.menu.PechMenu::new));

    /** Reads how long the shard has left off the readings. */
    public static final DeferredHolder<MenuType<?>, MenuType<me.moonscenty.alchemia.menu.AuraTotemMenu>> AURA_TOTEM =
            MENUS.register("aura_totem", () -> new MenuType<>(me.moonscenty.alchemia.menu.AuraTotemMenu::new,
                    FeatureFlags.VANILLA_SET));

    /** Reads the tank off the readings, so needs nothing sent when it opens. */
    public static final DeferredHolder<MenuType<?>, MenuType<me.moonscenty.alchemia.menu.SpaMenu>> SPA =
            MENUS.register("spa", () -> new MenuType<>(me.moonscenty.alchemia.menu.SpaMenu::new,
                    FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<me.moonscenty.alchemia.menu.ThaumatoriumMenu>>
            THAUMATORIUM = MENUS.register("thaumatorium",
                    () -> IMenuTypeExtension.create(me.moonscenty.alchemia.menu.ThaumatoriumMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<me.moonscenty.alchemia.menu.ArcaneBoreMenu>>
            ARCANE_BORE = MENUS.register("arcane_bore",
                    () -> IMenuTypeExtension.create(me.moonscenty.alchemia.menu.ArcaneBoreMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<me.moonscenty.alchemia.menu.HandMirrorMenu>>
            HAND_MIRROR = MENUS.register("hand_mirror",
                    () -> IMenuTypeExtension.create(me.moonscenty.alchemia.menu.HandMirrorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<EssentiaSmelterMenu>> ESSENTIA_SMELTER =
            MENUS.register("essentia_smelter",
                    () -> IMenuTypeExtension.create(EssentiaSmelterMenu::new));

    /** Carries which hand the pouch was opened from, since the pouch is an item and not a place. */
    public static final DeferredHolder<MenuType<?>, MenuType<FocusPouchMenu>> FOCUS_POUCH =
            MENUS.register("focus_pouch",
                    () -> IMenuTypeExtension.create(FocusPouchMenu::new));

    /** Carries which turret was opened, since a turret is an entity and not a place. */
    public static final DeferredHolder<MenuType<?>, MenuType<AutocasterMenu>> AUTOCASTER =
            MENUS.register("autocaster",
                    () -> IMenuTypeExtension.create(AutocasterMenu::new));

    /** Carries which bench was opened, since the focus stays on the bench rather than in a hand. */
    public static final DeferredHolder<MenuType<?>, MenuType<me.moonscenty.alchemia.menu.FocalManipulatorMenu>>
            FOCAL_MANIPULATOR = MENUS.register("focal_manipulator",
                    () -> IMenuTypeExtension.create(me.moonscenty.alchemia.menu.FocalManipulatorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<me.moonscenty.alchemia.menu.GolemPressMenu>>
            GOLEM_PRESS = MENUS.register("golem_press",
                    () -> new MenuType<>(me.moonscenty.alchemia.menu.GolemPressMenu::new, FeatureFlags.VANILLA_SET));

    /** Carries where the seal is, since the panel belongs to it. */
    public static final DeferredHolder<MenuType<?>, MenuType<me.moonscenty.alchemia.menu.SealMenu>> SEAL =
            MENUS.register("seal", () -> IMenuTypeExtension.create(me.moonscenty.alchemia.menu.SealMenu::new));

    private ModMenus() {
    }
}
