package me.moonscenty.alchemia.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.network.ChooseFocus;
import me.moonscenty.alchemia.wand.Foci;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class FocusWheelScreen extends Screen {
    private static final ResourceLocation RING = Alchemia.id("textures/gui/focus_wheel.png");
    private static final ResourceLocation RING_BACK = Alchemia.id("textures/gui/focus_wheel_back.png");

    private static final float ROOM = 16.0F;
    private static final float ROOM_EACH = 2.5F;
    private static final float RING_SHADE = 0.5F;
    private static final float OUTER = 2.75F;
    private static final float INNER = 2.55F;
    private static final float TURNS = 0.5F;

    private static final int HALF = 8;
    private static final double OVER = 10.0;
    private static final float GROWS = 1.3F;
    private static final float GROWS_BY = 0.05F;
    private static final float OPENS_BY = 0.15F;

    private final List<ItemStack> carried;
    private final ItemStack fitted;
    private final int key;

    private float open;
    private float grown;
    private int under = -1;
    private boolean chosen;

    public FocusWheelScreen(List<ItemStack> carried, ItemStack fitted, int key) {
        super(CommonComponents.EMPTY);
        java.util.Map<String, ItemStack> once = new java.util.LinkedHashMap<>();
        carried.forEach(stack -> once.putIfAbsent(Foci.name(stack), stack));
        this.carried = List.copyOf(once.values());
        this.fitted = fitted;
        this.key = key;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (minecraft == null || minecraft.player == null
                || !(minecraft.player.getMainHandItem().getItem() instanceof WandItem)) {
            onClose();
            return;
        }
        // a key release (e.g. a mouse button) may never reach a screen, so poll it too
        if (!WandKeys.CHANGE_FOCUS.isDown()) {
            choose();
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        open = Math.min(1.0F, open + OPENS_BY);
        int middleX = width / 2;
        int middleY = height / 2;
        float room = (ROOM + carried.size() * ROOM_EACH) * open;
        float age = minecraft == null || minecraft.player == null ? 0.0F
                : minecraft.player.tickCount + partial;

        rings(graphics, middleX, middleY, room, age);
        if (!fitted.isEmpty()) {
            graphics.renderItem(fitted, middleX - HALF, middleY - HALF);
        }

        under = -1;
        for (int which = 0; which < carried.size(); which++) {
            int[] at = place(which, middleX, middleY, room);
            if (Mth.square(mouseX - at[0]) + Mth.square(mouseY - at[1]) <= Mth.square(OVER)) {
                under = which;
            }
        }
        grown = under < 0 ? 1.0F : Math.min(GROWS, grown + GROWS_BY);

        for (int which = 0; which < carried.size(); which++) {
            int[] at = place(which, middleX, middleY, room);
            graphics.pose().pushPose();
            graphics.pose().translate(at[0], at[1], 0.0F);
            if (which == under) {
                graphics.pose().scale(grown, grown, 1.0F);
            }
            graphics.pose().translate(-HALF, -HALF, 0.0F);
            graphics.renderItem(carried.get(which), 0, 0);
            graphics.pose().popPose();
        }

        if (under >= 0) {
            graphics.renderTooltip(font, carried.get(under), mouseX, mouseY);
        } else if (!fitted.isEmpty() && Math.abs(mouseX - middleX) <= OVER && Math.abs(mouseY - middleY) <= OVER) {
            graphics.renderTooltip(font, fitted, mouseX, mouseY);
        }
    }

    private int[] place(int which, int middleX, int middleY, float room) {
        float round = -90.0F + 360.0F * which / Math.max(1, carried.size());
        return new int[] {
                middleX + (int) (Mth.cos(round * Mth.DEG_TO_RAD) * room),
                middleY + (int) (Mth.sin(round * Mth.DEG_TO_RAD) * room)};
    }

    private void rings(GuiGraphics graphics, int middleX, int middleY, float room, float age) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(RING_SHADE, RING_SHADE, RING_SHADE, RING_SHADE);
        ring(graphics, RING, middleX, middleY, room * OUTER, age * TURNS);
        ring(graphics, RING_BACK, middleX, middleY, room * INNER, -age * TURNS);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void ring(GuiGraphics graphics, ResourceLocation sheet, int middleX, int middleY, float size,
            float turn) {
        graphics.pose().pushPose();
        graphics.pose().translate(middleX, middleY, 0.0F);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(turn));
        graphics.pose().translate(-size / 2.0F, -size / 2.0F, 0.0F);
        graphics.blit(sheet, 0, 0, 0.0F, 0.0F, (int) size, (int) size, (int) size, (int) size);
        graphics.pose().popPose();
    }

    @Override
    public boolean keyReleased(int pressed, int scancode, int modifiers) {
        if (pressed == key) {
            choose();
            onClose();
            return true;
        }
        return super.keyReleased(pressed, scancode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (under >= 0) {
            choose();
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void choose() {
        if (chosen || under < 0) {
            return;
        }
        chosen = true;
        PacketDistributor.sendToServer(new ChooseFocus(Foci.name(carried.get(under))));
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }
}
