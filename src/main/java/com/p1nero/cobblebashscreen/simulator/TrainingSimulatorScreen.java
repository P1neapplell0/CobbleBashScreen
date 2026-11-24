package com.p1nero.cobblebashscreen.simulator;

import com.nore.cobblebash.CobbleBash;
import com.nore.cobblebash.elitefour.EliteFourMember;
import com.nore.cobblebash.gym.GymType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class TrainingSimulatorScreen extends AbstractContainerScreen<TrainingSimulatorMenu> {
    private static final String TRANSLATION_PREFIX = "screen.cobblebash_screen.simulator.";
    private static final GymType[] GYMS = GymType.values();
    private static final int ELITE = TrainingSimulatorMenu.ELITE_FOUR_BUTTON_ID;

    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 222;
    private static final Rect CLOSE = new Rect(288, 9, 22, 22);
    private static final Rect GYM_TAB = new Rect(12, 42, 146, 21);
    private static final Rect ELITE_TAB = new Rect(162, 42, 146, 21);
    private static final Rect CONTENT = new Rect(12, 68, 296, 118);
    private static final Rect GRID = new Rect(16, 71, 282, 112);
    private static final Rect TRACK = new Rect(300, GRID.y(), 5, GRID.height());
    private static final Rect ELITE_ACTION = new Rect(190, 158, 109, 21);
    private static final Rect DIALOG = new Rect(44, 59, 232, 104);
    private static final Rect CANCEL = new Rect(56, 129, 101, 23);
    private static final Rect ENTER = new Rect(163, 129, 101, 23);
    private static final int COLUMNS = 3;
    private static final int CARD_WIDTH = 90;
    private static final int CARD_HEIGHT = 34;
    private static final int CARD_GAP = 6;
    private static final int ROW_HEIGHT = CARD_HEIGHT + 5;
    private static final int TOTAL_ROWS = (GYMS.length + COLUMNS - 1) / COLUMNS;
    private static final int FULL_GRID_HEIGHT = (TOTAL_ROWS - 1) * ROW_HEIGHT + CARD_HEIGHT;
    private static final int MAX_SCROLL = Math.max(0, FULL_GRID_HEIGHT - GRID.height());
    private static final int THUMB_HEIGHT = Math.min(TRACK.height(),
            Math.max(12, TRACK.height() * GRID.height() / FULL_GRID_HEIGHT));
    private static final double WHEEL_SCROLL_PIXELS = 24.0D;
    private static final double SCROLL_RESPONSE = 18.0D;

    // Ore UI surface and action colors, adapted to Minecraft's integer GUI coordinates.
    private static final int BORDER = 0xFF1E1E1F;
    private static final int HEADER = 0xFF313233;
    private static final int WELL = 0xFF242425;
    private static final int SURFACE = 0xFF48494A;
    private static final int DIVIDER = 0xFF58585A;
    private static final int WHITE = 0xFFF4F6F9;
    private static final int MUTED = 0xFFD0D1D4;
    private static final int DIM = 0xFFB1B2B5;
    private static final int GREEN = 0xFFA0E081;
    private static final int GOLD = 0xFFFFD479;
    private static final SoundEvent GUI_CLICK = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("cobblemon", "gui.click"));
    private static final SoundEvent PC_UNLOCK = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath("cobblemon", "pc.wallpaper.unlock"));

    private boolean eliteTab;
    private double scrollOffset;
    private double targetScrollOffset;
    private long lastRenderNanos;
    private int scrollbarGrabOffset = -1;
    private int pendingChallenge = -1;
    private boolean openingSoundPlayed;
    private int observedUnlockRevision;

    public TrainingSimulatorScreen(TrainingSimulatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        observedUnlockRevision = menu.getUnlockRevision();
        scrollbarGrabOffset = -1;
        if (!openingSoundPlayed) {
            openingSoundPlayed = true;
            playGuiClick();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (menu.getUnlockRevision() != observedUnlockRevision) {
            observedUnlockRevision = menu.getUnlockRevision();
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(PC_UNLOCK, 1.0F));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        updateSmoothScroll();
        int x = pendingChallenge < 0 ? mouseX - leftPos : -1;
        int y = pendingChallenge < 0 ? mouseY - topPos : -1;

        drawFrame(graphics, new Rect(0, 0, PANEL_WIDTH, PANEL_HEIGHT));
        fill(graphics, 2, 2, PANEL_WIDTH - 4, 35, HEADER);
        fill(graphics, 2, 36, PANEL_WIDTH - 4, 1, BORDER);
        label(graphics, text("title"), 12, 9, 268, WHITE, true);
        label(graphics, text("subtitle"), 12, 22, 268, MUTED, false);
        drawButtonFace(graphics, CLOSE, ButtonStyle.NEUTRAL, CLOSE.contains(x, y));
        drawCross(graphics, CLOSE.x() + 8, CLOSE.y() + 7);

        drawTab(graphics, GYM_TAB, text("tab.gyms"), !eliteTab, x, y);
        drawTab(graphics, ELITE_TAB, text("tab.elite_four"), eliteTab, x, y);
        drawWell(graphics, CONTENT);
        if (eliteTab) {
            drawEliteFour(graphics, x, y);
        } else {
            drawGymGrid(graphics, x, y);
        }
        drawFooter(graphics);
        if (pendingChallenge >= 0) {
            drawConfirmation(graphics, mouseX - leftPos, mouseY - topPos);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // The title and dialog are part of the surface drawn by renderBg.
    }

    private void drawTab(GuiGraphics graphics, Rect bounds, Component title, boolean selected, int mouseX, int mouseY) {
        if (selected) {
            drawWell(graphics, bounds);
            fill(graphics, bounds.x() + 1, bounds.bottom() - 3, bounds.width() - 2, 2, 0xFF6CC349);
            centeredLabel(graphics, title, bounds, bounds.y() + 6, WHITE, true);
        } else {
            drawButton(graphics, bounds, title, ButtonStyle.NEUTRAL, mouseX, mouseY);
        }
    }

    private Rect cardBounds(int index) {
        return new Rect(GRID.x() + index % COLUMNS * (CARD_WIDTH + CARD_GAP),
                GRID.y() + index / COLUMNS * ROW_HEIGHT - (int) Math.round(scrollOffset), CARD_WIDTH, CARD_HEIGHT);
    }

    private void drawGymGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean pointerInGrid = GRID.contains(mouseX, mouseY) && scrollbarGrabOffset < 0;
        graphics.enableScissor(leftPos + GRID.x(), topPos + GRID.y(), leftPos + GRID.right(), topPos + GRID.bottom());
        for (int index = 0; index < GYMS.length; index++) {
            Rect card = cardBounds(index);
            if (card.bottom() > GRID.y() && card.y() < GRID.bottom()) {
                drawGymCard(graphics, index, card, pointerInGrid && card.contains(mouseX, mouseY));
            }
        }
        graphics.disableScissor();

        drawWell(graphics, TRACK);
        if (MAX_SCROLL > 0) {
            drawButtonFace(graphics, thumbBounds(), ButtonStyle.SECONDARY,
                    scrollbarGrabOffset >= 0 || TRACK.contains(mouseX, mouseY));
        }
    }

    private void drawGymCard(GuiGraphics graphics, int index, Rect card, boolean hovered) {
        GymType type = GYMS[index];
        ChallengeState state = challengeState(index);
        int x = card.x();
        int y = card.y();
        drawButtonFace(graphics, card, state == ChallengeState.LOCKED ? ButtonStyle.DISABLED : ButtonStyle.NEUTRAL,
                hovered);
        drawWell(graphics, new Rect(x + 4, y + 3, 19, 19));
        graphics.renderItem(new ItemStack(CobbleBash.TRAINING_DISKS.get(type).get()), leftPos + x + 5, topPos + y + 4);
        fill(graphics, x + 4, y + 20, 19, 1, typeColor(type));
        label(graphics, text("gym_name." + type.getId()), x + 27, y + 5, card.width() - 32, WHITE, true);

        Component clears = text("clear_count", menu.getGymClearCount(index));
        int clearsWidth = font.width(clears);
        label(graphics, clears, card.right() - 5 - clearsWidth, y + 15, clearsWidth, DIM, false);
        if (menu.hasCompleted(index)) {
            drawCheck(graphics, x + 28, y + 16, GREEN);
        }
        Component action = text(state == ChallengeState.LOCKED ? "no_disc" : state.label);
        label(graphics, action, x + 5, y + 23, card.width() - 10, state.color, false);
    }

    private void drawEliteFour(GuiGraphics graphics, int mouseX, int mouseY) {
        drawWell(graphics, new Rect(20, 75, 24, 24));
        graphics.renderItem(new ItemStack(CobbleBash.ELITE_FOUR_TRAINING_DISK.get()), leftPos + 24, topPos + 79);
        label(graphics, text("elite_title"), 50, 77, 245, WHITE, true);
        label(graphics, text("completed_count", menu.getCompletedCount(), GYMS.length), 50, 89, 245, MUTED, false);
        var members = EliteFourMember.ordered();
        for (int index = 0; index < members.size(); index++) {
            int x = 20 + index % 2 * 142;
            int y = 105 + index / 2 * 22;
            drawButtonFace(graphics, new Rect(x, y, 137, 18), ButtonStyle.NEUTRAL, false);
            label(graphics, text("elite_member." + members.get(index).getId()), x + 6, y + 4, 125, MUTED, false);
        }

        ChallengeState state = challengeState(ELITE);
        boolean gated = menu.getCompletedCount() < GYMS.length;
        Component status = gated ? text("requires_gyms", GYMS.length) : switch (state) {
            case CHALLENGE -> text("unlocked");
            case UNLOCK -> text("disc_ready");
            case LOCKED -> text("no_elite_disc");
        };
        label(graphics, status, 20, 164, ELITE_ACTION.x() - 26, state.color, false);
        drawButton(graphics, ELITE_ACTION, text(state.label), switch (state) {
            case CHALLENGE -> ButtonStyle.PRIMARY;
            case UNLOCK -> ButtonStyle.SECONDARY;
            case LOCKED -> ButtonStyle.DISABLED;
        }, mouseX, mouseY);
    }

    private void drawFooter(GuiGraphics graphics) {
        drawWell(graphics, new Rect(12, 191, 296, 24));
        int completed = menu.getCompletedCount();
        label(graphics, text("completed_count", completed, GYMS.length), 19, 195, 144, WHITE, false);
        drawWell(graphics, new Rect(19, 206, 138, 6));
        int progress = 136 * Mth.clamp(completed, 0, GYMS.length) / GYMS.length;
        if (progress > 0) {
            fill(graphics, 20, 207, progress, 4, 0xFF3C8527);
            fill(graphics, 20, 207, progress, 1, 0xFF6CC349);
        }
        fill(graphics, 165, 195, 1, 16, DIVIDER);
        label(graphics, text("challenge_count", menu.getChallengesStarted()), 173, 195, 129, WHITE, false);
        boolean active = menu.hasActiveChallenge();
        fill(graphics, 173, 207, 4, 4, active ? GOLD : DIM);
        label(graphics, text(active ? "active" : "standby"), 181, 205, 121, active ? GOLD : MUTED, false);
    }

    private void drawConfirmation(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        fill(graphics, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 0xB0101011);
        drawFrame(graphics, DIALOG);
        fill(graphics, DIALOG.x() + 2, DIALOG.y() + 2, DIALOG.width() - 4, 21, HEADER);
        fill(graphics, DIALOG.x() + 2, DIALOG.y() + 22, DIALOG.width() - 4, 1, BORDER);
        centeredLabel(graphics, text("confirm_title"), DIALOG, 68, WHITE, true);
        Component target = pendingChallenge == ELITE ? text("elite_title")
                : text("gym_target", text("gym_name." + GYMS[pendingChallenge].getId()));
        centeredLabel(graphics, target, DIALOG, 94, GREEN, true);
        centeredLabel(graphics, text("confirm_enter"), DIALOG, 108, MUTED, false);
        drawButton(graphics, CANCEL, Component.translatable("gui.cancel"), ButtonStyle.SECONDARY, mouseX, mouseY);
        drawButton(graphics, ENTER, text("enter"), canConfirm() ? ButtonStyle.PRIMARY : ButtonStyle.DISABLED,
                mouseX, mouseY);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = mouseX - leftPos;
        double y = mouseY - topPos;
        if (pendingChallenge >= 0) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (CANCEL.contains(x, y)) {
                    dismissConfirmation();
                } else if (ENTER.contains(x, y)) {
                    confirmChallenge();
                }
            }
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (CLOSE.contains(x, y)) {
            playGuiClick();
            onClose();
            return true;
        }
        if (GYM_TAB.contains(x, y) || ELITE_TAB.contains(x, y)) {
            boolean nextEliteTab = ELITE_TAB.contains(x, y);
            if (nextEliteTab != eliteTab) {
                playGuiClick();
                eliteTab = nextEliteTab;
            }
            return true;
        }
        if (eliteTab) {
            if (ELITE_ACTION.contains(x, y)) {
                activateChallenge(ELITE);
                return true;
            }
        } else if (MAX_SCROLL > 0 && TRACK.contains(x, y)) {
            Rect thumb = thumbBounds();
            scrollbarGrabOffset = thumb.contains(x, y) ? (int) y - thumb.y() : THUMB_HEIGHT / 2;
            targetScrollOffset = scrollOffset;
            if (!thumb.contains(x, y)) {
                dragScrollbar(y);
            }
            return true;
        } else if (GRID.contains(x, y)) {
            for (int index = 0; index < GYMS.length; index++) {
                if (cardBounds(index).contains(x, y)) {
                    activateChallenge(index);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (pendingChallenge >= 0) {
            return true;
        }
        if (!eliteTab && scrollbarGrabOffset < 0 && CONTENT.contains(mouseX - leftPos, mouseY - topPos)) {
            targetScrollOffset = Mth.clamp(targetScrollOffset - scrollY * WHEEL_SCROLL_PIXELS, 0.0D, MAX_SCROLL);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && scrollbarGrabOffset >= 0) {
            dragScrollbar(mouseY - topPos);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && scrollbarGrabOffset >= 0) {
            scrollbarGrabOffset = -1;
            return true;
        }
        return pendingChallenge >= 0 || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (pendingChallenge >= 0) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                dismissConfirmation();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private ChallengeState challengeState(int challenge) {
        if (challenge == ELITE && menu.getCompletedCount() < GYMS.length) {
            return ChallengeState.LOCKED;
        }
        if (menu.isUnlocked(challenge)) {
            return ChallengeState.CHALLENGE;
        }
        return menu.getDiskCount(challenge) > 0 ? ChallengeState.UNLOCK : ChallengeState.LOCKED;
    }

    private void activateChallenge(int challenge) {
        switch (challengeState(challenge)) {
            case CHALLENGE -> {
                playGuiClick();
                pendingChallenge = challenge;
            }
            case UNLOCK -> {
                playGuiClick();
                sendMenuButton(TrainingSimulatorMenu.UNLOCK_BUTTON_OFFSET + challenge);
            }
            case LOCKED -> { }
        }
    }

    private boolean canConfirm() {
        return challengeState(pendingChallenge) == ChallengeState.CHALLENGE;
    }

    private void confirmChallenge() {
        if (canConfirm()) {
            playGuiClick();
            sendMenuButton(pendingChallenge);
            pendingChallenge = -1;
        }
    }

    private void dismissConfirmation() {
        playGuiClick();
        pendingChallenge = -1;
    }

    private void sendMenuButton(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    private void playGuiClick() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(GUI_CLICK, 1.0F));
    }

    private Rect thumbBounds() {
        int travel = TRACK.height() - THUMB_HEIGHT;
        int y = TRACK.y() + (int) Math.round(travel * scrollOffset / MAX_SCROLL);
        return new Rect(TRACK.x(), y, TRACK.width(), THUMB_HEIGHT);
    }

    private void dragScrollbar(double mouseY) {
        int travel = TRACK.height() - THUMB_HEIGHT;
        double fraction = Mth.clamp((mouseY - TRACK.y() - scrollbarGrabOffset) / travel, 0.0D, 1.0D);
        targetScrollOffset = scrollOffset = fraction * MAX_SCROLL;
    }

    private void updateSmoothScroll() {
        long now = System.nanoTime();
        if (lastRenderNanos != 0 && scrollbarGrabOffset < 0) {
            double elapsed = Math.min(0.05D, (now - lastRenderNanos) / 1_000_000_000.0D);
            scrollOffset += (targetScrollOffset - scrollOffset) * (1.0D - Math.exp(-SCROLL_RESPONSE * elapsed));
            if (Math.abs(targetScrollOffset - scrollOffset) < 0.05D) {
                scrollOffset = targetScrollOffset;
            }
        }
        lastRenderNanos = now;
    }

    private void drawFrame(GuiGraphics graphics, Rect rect) {
        fill(graphics, rect.x() + 2, rect.y() + 2, rect.width(), rect.height(), 0x70000000);
        fill(graphics, rect, BORDER);
        fill(graphics, rect.x() + 1, rect.y() + 1, rect.width() - 2, rect.height() - 4, SURFACE);
        fill(graphics, rect.x() + 1, rect.y() + 1, rect.width() - 2, 1, 0xFF777879);
        fill(graphics, rect.x() + 1, rect.bottom() - 3, rect.width() - 2, 2, HEADER);
    }

    private void drawWell(GuiGraphics graphics, Rect rect) {
        fill(graphics, rect, BORDER);
        fill(graphics, rect.x() + 1, rect.y() + 1, rect.width() - 2, rect.height() - 2, WELL);
        fill(graphics, rect.x() + 1, rect.bottom() - 1, rect.width() - 2, 1, DIVIDER);
    }

    private void drawButton(GuiGraphics graphics, Rect rect, Component label, ButtonStyle style, int mouseX, int mouseY) {
        drawButtonFace(graphics, rect, style, rect.contains(mouseX, mouseY));
        centeredLabel(graphics, label, rect, rect.y() + (rect.height() - font.lineHeight - 2) / 2,
                style.textColor, style == ButtonStyle.PRIMARY || style == ButtonStyle.NEUTRAL);
    }

    private void drawButtonFace(GuiGraphics graphics, Rect rect, ButtonStyle style, boolean hovered) {
        boolean highlighted = hovered && style != ButtonStyle.DISABLED;
        fill(graphics, rect, highlighted ? WHITE : BORDER);
        fill(graphics, rect.x() + 1, rect.y() + 1, rect.width() - 2, rect.height() - 2, style.shadow);
        fill(graphics, rect.x() + 1, rect.y() + 1, rect.width() - 2, rect.height() - 4,
                highlighted ? style.hover : style.face);
        fill(graphics, rect.x() + 1, rect.y() + 1, rect.width() - 2, 1, style.highlight);
    }

    private void drawCheck(GuiGraphics graphics, int x, int y, int color) {
        fill(graphics, x, y + 2, 1, 2, color);
        fill(graphics, x + 1, y + 3, 1, 2, color);
        fill(graphics, x + 2, y + 2, 1, 2, color);
        fill(graphics, x + 3, y + 1, 1, 2, color);
        fill(graphics, x + 4, y, 1, 2, color);
    }

    private void drawCross(GuiGraphics graphics, int x, int y) {
        for (int pixel = 0; pixel < 6; pixel++) {
            fill(graphics, x + pixel, y + pixel, 1, 1, WHITE);
            fill(graphics, x + 5 - pixel, y + pixel, 1, 1, WHITE);
        }
    }

    private void label(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color, boolean shadow) {
        int textWidth = font.width(text);
        if (textWidth <= maxWidth) {
            graphics.drawString(font, text, leftPos + x, topPos + y, color, shadow);
            return;
        }

        float scale = (float) maxWidth / textWidth;
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + x, topPos + y + (1 - scale) * font.lineHeight / 2, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, text, 0, 0, color, shadow);
        graphics.pose().popPose();
    }

    private void centeredLabel(GuiGraphics graphics, Component text, Rect rect, int y, int color, boolean shadow) {
        int textWidth = Math.min(font.width(text), rect.width() - 8);
        label(graphics, text, rect.x() + (rect.width() - textWidth) / 2, y, textWidth, color, shadow);
    }

    private void fill(GuiGraphics graphics, Rect rect, int color) {
        fill(graphics, rect.x(), rect.y(), rect.width(), rect.height(), color);
    }

    private void fill(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(leftPos + x, topPos + y, leftPos + x + width, topPos + y + height, color);
    }

    private static Component text(String path, Object... arguments) {
        return Component.translatable(TRANSLATION_PREFIX + path, arguments);
    }

    private static int typeColor(GymType type) {
        return switch (type.getId()) {
            case "fire" -> 0xFFE65B45;
            case "water" -> 0xFF4AA8E8;
            case "grass" -> 0xFF58B85B;
            case "electric" -> 0xFFF2C84B;
            case "psychic", "fairy" -> 0xFFE77DB5;
            case "poison", "ghost" -> 0xFF9B65C7;
            case "ice", "flying" -> 0xFF82D7E8;
            case "rock", "ground" -> 0xFFB78B58;
            case "dragon" -> 0xFF6677D8;
            case "dark" -> 0xFF8C8D90;
            case "steel", "normal" -> 0xFFA7B2B8;
            case "bug" -> 0xFF93B83F;
            case "fighting" -> 0xFFC35B4D;
            default -> MUTED;
        };
    }

    private record Rect(int x, int y, int width, int height) {
        int right() {
            return x + width;
        }

        int bottom() {
            return y + height;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < right() && mouseY >= y && mouseY < bottom();
        }
    }

    private enum ChallengeState {
        CHALLENGE("challenge", GREEN),
        UNLOCK("unlock", GOLD),
        LOCKED("locked", DIM);

        private final String label;
        private final int color;

        ChallengeState(String label, int color) {
            this.label = label;
            this.color = color;
        }
    }

    private enum ButtonStyle {
        PRIMARY(0xFF3C8527, 0xFF52A535, 0xFF639D52, 0xFF1D4D13, WHITE),
        SECONDARY(0xFFD0D1D4, 0xFFF4F6F9, 0xFFE6E8EB, DIVIDER, BORDER),
        NEUTRAL(SURFACE, DIVIDER, 0xFF6D6D6E, HEADER, WHITE),
        DISABLED(HEADER, HEADER, 0xFF48494A, WELL, DIM);

        private final int face;
        private final int hover;
        private final int highlight;
        private final int shadow;
        private final int textColor;

        ButtonStyle(int face, int hover, int highlight, int shadow, int textColor) {
            this.face = face;
            this.hover = hover;
            this.highlight = highlight;
            this.shadow = shadow;
            this.textColor = textColor;
        }
    }
}
