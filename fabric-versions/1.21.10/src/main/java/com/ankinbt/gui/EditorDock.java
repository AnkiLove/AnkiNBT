package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.compat.VersionCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import java.lang.reflect.Field;

final class EditorDock {
    static final int MENU_BAR_HEIGHT = 30;
    private static final int DRAWER_GAP = 3;
    private static final int STANDALONE_BAR_Y = 48;
    private static final int MIN_PANEL_WIDTH = 300;
    private static final int COMPACT_MIN_PANEL_WIDTH = 220;
    private static final int MIN_DRAWER_HEIGHT = 120;
    private static final int COMPACT_MIN_DRAWER_HEIGHT = 84;
    private static final int DEFAULT_MAX_PANEL_WIDTH = 720;
    private static final int EDGE_MAX_PANEL_WIDTH = 500;
    private static final int SIZE_CONTROL_RESERVED = 36;
    private static final int SIZE_TRACK_WIDTH = 128;
    private static final int SIZE_TRACK_HEIGHT = 4;
    private static final int SIZE_THUMB_WIDTH = 12;
    private static final int SIZE_THUMB_HEIGHT = 10;
    private static final int SIZE_LABEL_WIDTH = 32;
    private static final int SIZE_CONTROL_HEIGHT = 24;
    private static final int SIZE_CONTROL_BOTTOM_MARGIN = 5;
    private static final int SIZE_CONTROL_PADDING = 6;
    private static final int SIZE_RESET_GAP = 6;
    private static final int SIZE_RESET_SIZE = 16;
    private static final int SIZE_ADJUST_GAP = 4;
    private static final int SIZE_ADJUST_SIZE = 18;
    // Fallback used before a stored profile is loaded; the reset action follows the
    // current item-editor profile for entity and villager screens.
    static final float DEFAULT_EDITOR_SCALE = 0.40f;
    static final float DEFAULT_AXIS_ADJUSTMENT = 0.0f;
    static final float AXIS_ADJUSTMENT_STEP = 0.05f;
    private static Field hoveredSlotField;
    private static boolean hoveredSlotResolved;

    private EditorDock() {
    }

    static Bounds calculate(int width, int height, boolean inventoryOverlay) {
        int margin = inventoryOverlay ? 8 : 12;
        int maxWidth = Math.max(240, width - margin * 2);
        int maxHeight = Math.max(220, height - margin * 2);
        if (!inventoryOverlay) {
            int panelWidth = Math.min(maxWidth, 620);
            int panelHeight = Math.min(maxHeight, 420);
            return new Bounds((width - panelWidth) / 2, (height - panelHeight) / 2, panelWidth, panelHeight);
        }

        String position = AnkiConfig.getItemEditorPosition();
        boolean horizontal = "top".equals(position) || "bottom".equals(position);
        int panelWidth = horizontal
                ? Math.min(maxWidth, 620)
                : Math.min(maxWidth, Math.max(Math.min(340, maxWidth), Math.min(500, Math.round(width * 0.52f))));
        int panelHeight = horizontal
                ? Math.min(maxHeight, Math.max(Math.min(280, maxHeight), Math.min(350, Math.round(height * 0.68f))))
                : Math.min(maxHeight, 420);

        int x = switch (position) {
            case "left" -> margin;
            case "right" -> width - panelWidth - margin;
            default -> (width - panelWidth) / 2;
        };
        int y = switch (position) {
            case "top" -> margin;
            case "bottom" -> height - panelHeight - margin;
            default -> (height - panelHeight) / 2;
        };
        return new Bounds(x, y, panelWidth, panelHeight);
    }

    static MenuLayout menuLayout(int width, int height, int requestedDrawerHeight, boolean inventoryOverlay) {
        return menuLayout(width, height, requestedDrawerHeight, inventoryOverlay,
                AnkiConfig.getItemEditorScale(),
                AnkiConfig.getItemEditorWidthAdjustment(),
                AnkiConfig.getItemEditorHeightAdjustment());
    }

    static MenuLayout menuLayout(int width, int height, int requestedDrawerHeight,
                                 boolean inventoryOverlay, float scale) {
        return menuLayout(width, height, requestedDrawerHeight, inventoryOverlay, scale,
                DEFAULT_AXIS_ADJUSTMENT, DEFAULT_AXIS_ADJUSTMENT);
    }

    static MenuLayout menuLayout(int width, int height, int requestedDrawerHeight,
                                 boolean inventoryOverlay, float scale,
                                 float widthAdjustment, float heightAdjustment) {
        return menuLayoutInternal(width, height, requestedDrawerHeight, inventoryOverlay, scale,
                widthAdjustment, heightAdjustment);
    }

    private static MenuLayout menuLayoutInternal(int width, int height, int requestedDrawerHeight,
                                                 boolean inventoryOverlay, float scale,
                                                 float widthAdjustment, float heightAdjustment) {
        int margin = inventoryOverlay ? 8 : 12;
        int bottomMargin = margin + SIZE_CONTROL_RESERVED;
        int barWidth = resolvePanelWidth(width, inventoryOverlay, scale, widthAdjustment);
        int safeTop = inventoryOverlay ? margin : standaloneSafeTop(height, margin);
        int availableDrawerHeight = Math.max(1,
                height - safeTop - bottomMargin - MENU_BAR_HEIGHT - DRAWER_GAP);
        int drawerHeight = resolveDrawerHeight(requestedDrawerHeight, availableDrawerHeight, scale,
                heightAdjustment);
        String position = inventoryOverlay ? AnkiConfig.getItemEditorPosition() : "center";

        boolean above = "bottom".equals(position);
        int barX = switch (position) {
            case "left" -> margin;
            case "right" -> width - barWidth - margin;
            default -> (width - barWidth) / 2;
        };
        int barY = switch (position) {
            case "bottom" -> height - MENU_BAR_HEIGHT - bottomMargin;
            case "top" -> margin;
            default -> safeTop + Math.max(0,
                    (height - safeTop - bottomMargin - MENU_BAR_HEIGHT - DRAWER_GAP - drawerHeight) / 2);
        };
        if (inventoryOverlay && AnkiConfig.getItemEditorCustomX() >= 0 && AnkiConfig.getItemEditorCustomY() >= 0) {
            barX = AnkiConfig.getItemEditorCustomX();
            barY = AnkiConfig.getItemEditorCustomY();
            above = barY > height / 2;
        }
        return menuLayoutAtSize(width, height, barWidth, drawerHeight, inventoryOverlay, barX, barY, above);
    }

    static MenuLayout menuLayoutAt(int width, int height, int drawerHeight, boolean inventoryOverlay,
                                   int requestedBarX, int requestedBarY) {
        return menuLayoutAt(width, height, drawerHeight, inventoryOverlay,
                requestedBarX, requestedBarY, AnkiConfig.getItemEditorScale(),
                AnkiConfig.getItemEditorWidthAdjustment(),
                AnkiConfig.getItemEditorHeightAdjustment());
    }

    static MenuLayout menuLayoutAt(int width, int height, int drawerHeight, boolean inventoryOverlay,
                                   int requestedBarX, int requestedBarY, float scale) {
        return menuLayoutAt(width, height, drawerHeight, inventoryOverlay, requestedBarX,
                requestedBarY, scale, DEFAULT_AXIS_ADJUSTMENT, DEFAULT_AXIS_ADJUSTMENT);
    }

    static MenuLayout menuLayoutAt(int width, int height, int drawerHeight, boolean inventoryOverlay,
                                   int requestedBarX, int requestedBarY, float scale,
                                   float widthAdjustment, float heightAdjustment) {
        int availableDrawerHeight = Math.max(1,
                height - MENU_BAR_HEIGHT - (inventoryOverlay ? 16 : 24)
                        - SIZE_CONTROL_RESERVED - DRAWER_GAP);
        return menuLayoutAtSize(width, height,
                resolvePanelWidth(width, inventoryOverlay, scale, widthAdjustment),
                resolveDrawerHeight(drawerHeight, availableDrawerHeight, scale, heightAdjustment), inventoryOverlay,
                requestedBarX, requestedBarY, requestedBarY > height / 2);
    }

    static MenuLayout resizeLayout(int width, int height, boolean inventoryOverlay, MenuLayout current,
                                   int preferredDrawerHeight, float scale) {
        return resizeLayout(width, height, inventoryOverlay, current, preferredDrawerHeight, scale,
                DEFAULT_AXIS_ADJUSTMENT, DEFAULT_AXIS_ADJUSTMENT);
    }

    static MenuLayout resizeLayout(int width, int height, boolean inventoryOverlay, MenuLayout current,
                                   int preferredDrawerHeight, float scale,
                                   float widthAdjustment, float heightAdjustment) {
        if (current == null) {
            return menuLayout(width, height, preferredDrawerHeight, inventoryOverlay, scale,
                    widthAdjustment, heightAdjustment);
        }
        int margin = inventoryOverlay ? 8 : 12;
        int availableDrawerHeight = Math.max(1,
                height - MENU_BAR_HEIGHT - margin * 2 - SIZE_CONTROL_RESERVED - DRAWER_GAP);
        int requestedWidth = resolvePanelWidth(width, inventoryOverlay, scale, widthAdjustment);
        int requestedDrawerHeight = resolveDrawerHeight(preferredDrawerHeight,
                availableDrawerHeight, scale, heightAdjustment);
        int widthDelta = requestedWidth - current.bar().width();
        int heightDelta = requestedDrawerHeight - current.drawer().height();
        int nextX = current.bar().x() - widthDelta / 2;
        int nextY = current.bar().y() + (current.drawerAbove() ? heightDelta / 2 : -heightDelta / 2);
        return menuLayoutAtSize(width, height, requestedWidth, requestedDrawerHeight, inventoryOverlay,
                nextX, nextY, current.drawerAbove());
    }

    static SizeControl sizeControl(int width, int height, float scale) {
        int trackWidth = Math.min(SIZE_TRACK_WIDTH, Math.max(72, width - 48));
        int groupWidth = SIZE_LABEL_WIDTH + trackWidth + SIZE_RESET_GAP + SIZE_RESET_SIZE
                + SIZE_ADJUST_GAP + SIZE_ADJUST_SIZE * 2 + SIZE_ADJUST_GAP;
        int groupX = Math.max(4, (width - groupWidth) / 2);
        int panelY = Math.max(2, height - SIZE_CONTROL_BOTTOM_MARGIN - SIZE_CONTROL_HEIGHT);
        int trackX = groupX + SIZE_LABEL_WIDTH;
        int trackY = panelY + (SIZE_CONTROL_HEIGHT - SIZE_TRACK_HEIGHT) / 2;
        int thumbTravel = Math.max(1, trackWidth - SIZE_THUMB_WIDTH);
        int thumbX = trackX + Math.round(clampScale(scale) * thumbTravel);
        Bounds track = new Bounds(trackX, trackY, trackWidth, SIZE_TRACK_HEIGHT);
        Bounds thumb = new Bounds(thumbX, trackY - 3, SIZE_THUMB_WIDTH, SIZE_THUMB_HEIGHT);
        Bounds backdrop = new Bounds(groupX - SIZE_CONTROL_PADDING, panelY,
                groupWidth + SIZE_CONTROL_PADDING * 2, SIZE_CONTROL_HEIGHT);
        // Only the track is draggable. The surrounding label and action buttons
        // retain their own behavior instead of changing scale on a stray click.
        Bounds hit = new Bounds(trackX - 5, trackY - 5, trackWidth + 10, 14);
        Bounds reset = new Bounds(trackX + trackWidth + SIZE_RESET_GAP, trackY - 6,
                SIZE_RESET_SIZE, SIZE_RESET_SIZE);
        int adjustX = reset.x() + reset.width() + SIZE_ADJUST_GAP;
        Bounds horizontal = new Bounds(adjustX, trackY - 7, SIZE_ADJUST_SIZE, SIZE_ADJUST_SIZE);
        Bounds vertical = new Bounds(adjustX + SIZE_ADJUST_SIZE + SIZE_ADJUST_GAP,
                trackY - 7, SIZE_ADJUST_SIZE, SIZE_ADJUST_SIZE);
        return new SizeControl(backdrop, hit, track, thumb, reset, horizontal, vertical);
    }

    static float sizeScaleFromMouse(int width, double mouseX) {
        SizeControl control = sizeControl(width, 40, 0.5f);
        double local = mouseX - control.track().x() - SIZE_THUMB_WIDTH / 2.0;
        double travel = Math.max(1.0, control.track().width() - SIZE_THUMB_WIDTH);
        return clampScale((float) (local / travel));
    }

    static float renderSizeControl(GuiGraphics graphics, Font font, int width, int height,
                                   int mouseX, int mouseY, float scale, float hoverAnimation,
                                   boolean active, int accentColor) {
        SizeControl control = sizeControl(width, height, scale);
        boolean hovered = control.backdrop().contains(mouseX, mouseY);
        float speed = AnkiConfig.isUiAnimationEnabled()
                ? Math.max(0.18f, AnkiConfig.getUiAnimationSpeed() * 2.4f) : 1.0f;
        float nextHover = UiTheme.approach(hoverAnimation, hovered ? 1.0f : 0.0f, speed);
        Bounds backdrop = control.backdrop();
        Bounds track = control.track();
        Bounds thumb = control.thumb();
        Bounds reset = control.reset();
        float configuredOpacity = Math.max(0.3f, Math.min(1.0f, AnkiConfig.getUiOpacity()));
        int backdropFill = UiTheme.withAlpha(UiTheme.baseRgb(),
                Math.round(255f * (0.44f + configuredOpacity * 0.42f)
                        * (0.88f + nextHover * 0.12f)));
        int backdropBorder = UiTheme.mix(UiTheme.themedBorder(1.0f, 1.0f), accentColor,
                nextHover * 0.46f);
        graphics.fill(backdrop.x(), backdrop.y(), backdrop.x() + backdrop.width(),
                backdrop.y() + backdrop.height(), backdropFill);
        drawBorder(graphics, backdrop, backdropBorder);
        graphics.fill(backdrop.x(), backdrop.y(), backdrop.x() + 2, backdrop.y() + backdrop.height(),
                UiTheme.withAlpha(accentColor & 0x00FFFFFF, 130 + Math.round(nextHover * 100)));
        String scaleLabel = Math.round(clampScale(scale) * 100.0f) + "%";
        graphics.drawString(font, scaleLabel, backdrop.x() + SIZE_CONTROL_PADDING,
                backdrop.y() + (backdrop.height() - font.lineHeight) / 2,
                hovered ? UiTheme.textMain() : UiTheme.textDim(), false);
        int trackColor = UiTheme.mix(UiTheme.themedBorder(1.0f, 1.0f), accentColor,
                nextHover * 0.55f);
        graphics.fill(track.x(), track.y(), track.x() + track.width(),
                track.y() + track.height(), trackColor);
        int filledEnd = thumb.x() + thumb.width() / 2;
        graphics.fill(track.x(), track.y(), filledEnd, track.y() + track.height(), accentColor);
        graphics.fill(thumb.x(), thumb.y(), thumb.x() + thumb.width(),
                thumb.y() + thumb.height(), active ? accentColor : UiTheme.textDim());
        boolean resetHovered = reset.contains(mouseX, mouseY);
        int resetFill = resetHovered
                ? UiTheme.mix(UiTheme.panel(0.88f, 1.0f), accentColor, 0.32f)
                : UiTheme.panel(0.72f, 1.0f);
        int resetBorder = resetHovered ? accentColor : UiTheme.themedBorder(0.9f, 0.8f);
        graphics.fill(reset.x(), reset.y(), reset.x() + reset.width(), reset.y() + reset.height(), resetFill);
        graphics.fill(reset.x(), reset.y(), reset.x() + reset.width(), reset.y() + 1, resetBorder);
        graphics.fill(reset.x(), reset.y() + reset.height() - 1,
                reset.x() + reset.width(), reset.y() + reset.height(), resetBorder);
        graphics.fill(reset.x(), reset.y(), reset.x() + 1, reset.y() + reset.height(), resetBorder);
        graphics.fill(reset.x() + reset.width() - 1, reset.y(),
                reset.x() + reset.width(), reset.y() + reset.height(), resetBorder);
        String resetIcon = UiIcons.RESET;
        graphics.drawString(font, UiIcons.component(resetIcon),
                reset.x() + (reset.width() - font.width(UiIcons.component(resetIcon))) / 2,
                reset.y() + 4, resetHovered ? UiTheme.textMain() : UiTheme.textDim(), false);
        renderAdjustButton(graphics, font, control.horizontal(), mouseX, mouseY, accentColor,
                Component.literal("H"), Component.translatable("ankinbt.ui.resize_width"));
        renderAdjustButton(graphics, font, control.vertical(), mouseX, mouseY, accentColor,
                Component.literal("V"), Component.translatable("ankinbt.ui.resize_height"));
        boolean trackHovered = track.contains(mouseX, mouseY) || thumb.contains(mouseX, mouseY);
        if (trackHovered) {
            VersionCompat.get().renderTooltip(graphics, font,
                    Component.translatable("ankinbt.ui.resize_editor"), mouseX, mouseY);
        } else if (resetHovered) {
            VersionCompat.get().renderTooltip(graphics, font,
                    Component.translatable("ankinbt.ui.resize_reset"), mouseX, mouseY);
        }
        return nextHover;
    }

    private static void renderAdjustButton(GuiGraphics graphics, Font font, Bounds bounds,
                                           int mouseX, int mouseY, int accentColor,
                                           Component icon, Component tooltip) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        int fill = hovered ? UiTheme.withAlpha(accentColor & 0x00FFFFFF, 100)
                : UiTheme.panel(0.72f, 1.0f);
        int border = hovered ? accentColor : UiTheme.themedBorder(0.9f, 0.8f);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), fill);
        drawBorder(graphics, bounds, border);
        graphics.drawString(font, icon,
                bounds.x() + (bounds.width() - font.width(icon)) / 2,
                bounds.y() + (bounds.height() - font.lineHeight) / 2,
                hovered ? UiTheme.textMain() : UiTheme.textDim(), false);
        if (hovered) VersionCompat.get().renderTooltip(graphics, font, tooltip, mouseX, mouseY);
    }

    private static void drawBorder(GuiGraphics graphics, Bounds bounds, int color) {
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + 1, color);
        graphics.fill(bounds.x(), bounds.y() + bounds.height() - 1,
                bounds.x() + bounds.width(), bounds.y() + bounds.height(), color);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + 1, bounds.y() + bounds.height(), color);
        graphics.fill(bounds.x() + bounds.width() - 1, bounds.y(),
                bounds.x() + bounds.width(), bounds.y() + bounds.height(), color);
    }

    private static MenuLayout menuLayoutAtSize(int width, int height, int requestedPanelWidth,
                                               int drawerHeight, boolean inventoryOverlay,
                                               int requestedBarX, int requestedBarY, boolean preferAbove) {
        int margin = inventoryOverlay ? 8 : 12;
        int bottomMargin = margin + SIZE_CONTROL_RESERVED;
        int maxPanelWidth = Math.max(1, width - margin * 2);
        int minPanelWidth = minimumPanelWidth(width, maxPanelWidth);
        int barWidth = clamp(requestedPanelWidth, minPanelWidth, maxPanelWidth);
        int barX = Math.max(margin, Math.min(requestedBarX, width - barWidth - margin));
        int barY = Math.max(margin, Math.min(requestedBarY, height - MENU_BAR_HEIGHT - bottomMargin));
        int availableDrawerHeight = Math.max(1,
                height - MENU_BAR_HEIGHT - margin - bottomMargin - DRAWER_GAP);
        int minDrawerHeight = minimumDrawerHeight(availableDrawerHeight);
        drawerHeight = clamp(drawerHeight, minDrawerHeight, availableDrawerHeight);
        int aboveSpace = Math.max(0, barY - DRAWER_GAP - margin);
        int belowSpace = Math.max(0, height - bottomMargin - barY - MENU_BAR_HEIGHT - DRAWER_GAP);
        boolean canOpenAbove = aboveSpace >= drawerHeight;
        boolean canOpenBelow = belowSpace >= drawerHeight;
        boolean above = preferAbove ? canOpenAbove || !canOpenBelow : !canOpenBelow && canOpenAbove;
        int directionalSpace = above ? aboveSpace : belowSpace;
        if (directionalSpace == 0 && Math.max(aboveSpace, belowSpace) > 0) {
            above = aboveSpace > belowSpace;
            directionalSpace = Math.max(aboveSpace, belowSpace);
        }
        drawerHeight = Math.max(1, Math.min(drawerHeight, directionalSpace));
        int drawerY = above
                ? Math.max(margin, barY - drawerHeight - DRAWER_GAP)
                : Math.min(height - drawerHeight - bottomMargin, barY + MENU_BAR_HEIGHT + DRAWER_GAP);
        return new MenuLayout(
                new Bounds(barX, barY, barWidth, MENU_BAR_HEIGHT),
                new Bounds(barX, drawerY, barWidth, drawerHeight),
                above);
    }

    private static int resolvePanelWidth(int width, boolean inventoryOverlay, float scale,
                                         float widthAdjustment) {
        int margin = inventoryOverlay ? 8 : 12;
        int maxWidth = Math.max(1, width - margin * 2);
        int minWidth = minimumPanelWidth(width, maxWidth);
        String position = inventoryOverlay ? AnkiConfig.getItemEditorPosition() : "center";
        boolean edge = "left".equals(position) || "right".equals(position);
        int cap = configuredPanelCap(edge, maxWidth);
        int maximum = Math.max(minWidth, Math.min(cap, maxWidth));
        int preferred = clamp(configuredPreferredPanelWidth(width, edge), minWidth, maximum);
        int resolved = interpolateScale(minWidth, preferred, maximum, scale);
        float viewportRatio = Math.min(1.0f, 0.45f + clampScale(scale) * 0.55f);
        int responsiveCap = Math.max(minWidth, Math.round(width * viewportRatio));
        int finalMaximum = Math.max(minWidth, Math.min(maximum, responsiveCap));
        return applyAxisAdjustment(Math.min(resolved, finalMaximum), minWidth, finalMaximum,
                widthAdjustment);
    }

    private static int resolveDrawerHeight(int requestedDrawerHeight, int availableDrawerHeight, float scale,
                                           float heightAdjustment) {
        int minHeight = minimumDrawerHeight(availableDrawerHeight);
        int preferred = configuredPreferredDrawerHeight(requestedDrawerHeight,
                minHeight, availableDrawerHeight);
        int resolved = interpolateScale(minHeight, preferred, availableDrawerHeight, scale);
        float availableRatio = Math.min(1.0f, 0.52f + clampScale(scale) * 0.55f);
        int responsiveCap = Math.max(minHeight, Math.round(availableDrawerHeight * availableRatio));
        int finalMaximum = Math.max(minHeight, Math.min(availableDrawerHeight, responsiveCap));
        return applyAxisAdjustment(Math.min(resolved, finalMaximum), minHeight, finalMaximum,
                heightAdjustment);
    }

    private static int minimumPanelWidth(int viewportWidth, int availableWidth) {
        int responsiveMinimum = Math.max(COMPACT_MIN_PANEL_WIDTH, Math.round(viewportWidth * 0.50f));
        return Math.min(Math.min(MIN_PANEL_WIDTH, responsiveMinimum), availableWidth);
    }

    private static int minimumDrawerHeight(int availableHeight) {
        int responsiveMinimum = Math.max(COMPACT_MIN_DRAWER_HEIGHT,
                Math.round(availableHeight * 0.45f));
        return Math.min(Math.min(MIN_DRAWER_HEIGHT, responsiveMinimum), availableHeight);
    }

    private static int configuredPanelCap(boolean edge, int availableWidth) {
        int configured = switch (AnkiConfig.getEditorResolutionPreset()) {
            case "auto" -> availableWidth;
            case "960x540" -> edge ? 460 : 520;
            case "1280x720" -> edge ? 560 : 680;
            case "1600x900" -> edge ? 680 : 860;
            default -> edge ? EDGE_MAX_PANEL_WIDTH : DEFAULT_MAX_PANEL_WIDTH;
        };
        return Math.max(MIN_PANEL_WIDTH, configured);
    }

    private static int configuredPreferredPanelWidth(int viewportWidth, boolean edge) {
        return switch (AnkiConfig.getEditorResolutionPreset()) {
            case "auto" -> Math.round(viewportWidth * (edge ? 0.58f : 0.76f));
            case "960x540" -> edge ? 430 : 500;
            case "1280x720" -> edge ? 520 : 640;
            case "1600x900" -> edge ? 620 : 780;
            // Standalone editors need enough room for their tab labels and a three-column
            // item grid.  Edge overlays remain compact so inventory slots stay usable.
            default -> Math.round(viewportWidth * (edge ? 0.54f : 0.72f));
        };
    }

    private static int configuredPreferredDrawerHeight(int requestedHeight,
                                                        int minHeight, int availableHeight) {
        int adaptive = clamp(requestedHeight, minHeight,
                Math.max(minHeight, Math.round(availableHeight * 0.78f)));
        int configured = switch (AnkiConfig.getEditorResolutionPreset()) {
            case "auto" -> Math.round(availableHeight * 0.82f);
            case "960x540" -> Math.max(requestedHeight, Math.round(availableHeight * 0.60f));
            case "1280x720" -> Math.max(requestedHeight, Math.round(availableHeight * 0.72f));
            case "1600x900" -> Math.max(requestedHeight, Math.round(availableHeight * 0.84f));
            default -> adaptive;
        };
        return clamp(configured, minHeight, availableHeight);
    }

    private static int standaloneSafeTop(int height, int margin) {
        return Math.max(margin, Math.min(STANDALONE_BAR_Y, Math.max(margin, height / 6)));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private static float clampScale(float value) {
        return Math.max(0.0f, Math.min(value, 1.0f));
    }

    static float clampAxisAdjustment(float value) {
        return Math.max(-1.0f, Math.min(value, 1.0f));
    }

    static float adjustAxis(float current, float delta) {
        return clampAxisAdjustment(current + delta);
    }

    static int applyAxisAdjustment(int base, int minimum, int maximum, float adjustment) {
        int span = Math.max(0, maximum - minimum);
        return clamp(base + Math.round(span * clampAxisAdjustment(adjustment)), minimum, maximum);
    }

    static int interpolateScale(int minimum, int preferred, int maximum, float scale) {
        float clamped = clampScale(scale);
        if (clamped <= 0.5f) {
            return Math.round(minimum + (preferred - minimum) * (clamped * 2.0f));
        }
        return Math.round(preferred + (maximum - preferred) * ((clamped - 0.5f) * 2.0f));
    }

    static Slot hoveredSlot(AbstractContainerScreen<?> screen) {
        if (screen == null) return null;
        resolveHoveredSlotField();
        if (hoveredSlotField == null) return null;
        try {
            return (Slot) hoveredSlotField.get(screen);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    static boolean isPlayerInventorySlot(Slot slot) {
        Minecraft minecraft = Minecraft.getInstance();
        return slot != null
                && minecraft.player != null
                && slot.container == minecraft.player.getInventory()
                && slot.getContainerSlot() >= 0
                && (slot.getContainerSlot() < 36 || slot.getContainerSlot() == 40);
    }

    private static synchronized void resolveHoveredSlotField() {
        if (hoveredSlotResolved) return;
        hoveredSlotResolved = true;
        try {
            hoveredSlotField = AbstractContainerScreen.class.getDeclaredField("hoveredSlot");
            hoveredSlotField.setAccessible(true);
            return;
        } catch (ReflectiveOperationException ignored) {
        }
        for (Field field : AbstractContainerScreen.class.getDeclaredFields()) {
            if (field.getType() == Slot.class) {
                field.setAccessible(true);
                hoveredSlotField = field;
                break;
            }
        }
    }

    record Bounds(int x, int y, int width, int height) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    record MenuLayout(Bounds bar, Bounds drawer, boolean drawerAbove) {
    }

    record SizeControl(Bounds backdrop, Bounds hit, Bounds track, Bounds thumb, Bounds reset,
                       Bounds horizontal, Bounds vertical) {
    }

}
