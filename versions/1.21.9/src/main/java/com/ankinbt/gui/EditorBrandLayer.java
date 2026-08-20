package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

final class EditorBrandLayer {
    private static final ResourceLocation LOGO_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "ankinbt", "textures/gui/editor-logo.png");
    private static final int SETTINGS_SIZE = 24;
    private static final int SETTINGS_MARGIN = 10;

    private EditorBrandLayer() {
    }

    static float approachOpen(float current) {
        float speed = AnkiConfig.isUiAnimationEnabled()
                ? Math.max(0.08f, AnkiConfig.getUiAnimationSpeed() * 1.7f)
                : 1.0f;
        return UiTheme.approach(current, 1.0f, speed);
    }

    static float approachSettingsHover(float current, boolean hovered) {
        float speed = AnkiConfig.isUiAnimationEnabled()
                ? Math.max(0.16f, AnkiConfig.getUiAnimationSpeed() * 2.2f)
                : 1.0f;
        return UiTheme.approach(current, hovered ? 1.0f : 0.0f, speed);
    }

    static void renderBackgroundLogo(GuiGraphics graphics, int width, int height) {
        graphics.blit(LOGO_TEXTURE, 9, Math.max(9, height - 41), 32, 32,
                0.0f, 0.0f, 1.0f, 1.0f);
    }

    static void renderStatus(GuiGraphics graphics, Font font, int width, int height,
                             float animation, String editorMode) {
        renderStatus(graphics, font, width, height, animation, editorMode,
                AnkiConfig.getItemEditorScale());
    }

    static void renderStatus(GuiGraphics graphics, Font font, int width, int height,
                             float animation, String editorMode, float editorScale) {
        renderStatus(graphics, font, width, height, animation, editorMode, editorScale,
                EditorDock.DEFAULT_AXIS_ADJUSTMENT, EditorDock.DEFAULT_AXIS_ADJUSTMENT);
    }

    static void renderStatus(GuiGraphics graphics, Font font, int width, int height,
                             float animation, String editorMode, float editorScale,
                             float widthAdjustment, float heightAdjustment) {
        renderStatusInternal(graphics, font, width, height, animation, editorMode,
                editorScale, widthAdjustment, heightAdjustment, true);
    }

    /**
     * Item editors use one overall scale control. Their status line should not
     * expose the entity/villager-only axis adjustments.
     */
    static void renderItemStatus(GuiGraphics graphics, Font font, int width, int height,
                                 float animation, String editorMode) {
        renderItemStatus(graphics, font, width, height, animation, editorMode,
                AnkiConfig.getItemEditorScale());
    }

    static void renderItemStatus(GuiGraphics graphics, Font font, int width, int height,
                                 float animation, String editorMode, float editorScale) {
        renderStatusInternal(graphics, font, width, height, animation, editorMode,
                editorScale, EditorDock.DEFAULT_AXIS_ADJUSTMENT,
                EditorDock.DEFAULT_AXIS_ADJUSTMENT, false);
    }

    private static void renderStatusInternal(GuiGraphics graphics, Font font, int width, int height,
                                             float animation, String editorMode, float editorScale,
                                             float widthAdjustment, float heightAdjustment,
                                             boolean showAxisAdjustments) {
        int alpha = Math.max(0, Math.min(255, Math.round(255f * animation)));
        int x = 10 + Math.round((animation - 1f) * 14f);
        int y = 10;
        int rightLimit = settingsButtonX(width) - 10;
        int maxWidth = Math.max(24, rightLimit - x - 7);
        int accent = UiTheme.withAlpha(UiTheme.accent(AnkiConfig.getUiAccentPreset()) & 0x00FFFFFF, alpha);
        int main = UiTheme.withAlpha(UiTheme.textMain() & 0x00FFFFFF, alpha);
        int dim = UiTheme.withAlpha(UiTheme.textDim() & 0x00FFFFFF, alpha);

        String product = "AnkiNBT " + modVersion("ankinbt", "2.0.0");
        String runtime = "Minecraft " + modVersion("minecraft", "1.21.9")
                + "  |  NeoForge " + modVersion("neoforge", "unknown");
        int scalePercent = Math.round(editorScale * 100.0f);
        int widthOffset = Math.round(widthAdjustment * 100.0f);
        int heightOffset = Math.round(heightAdjustment * 100.0f);
        String state = editorMode + "  |  " + width + "x" + height
                + "  |  DEBUG  |  SCALE " + scalePercent + "%";
        if (showAxisAdjustments) {
            state += "  |  H " + signedPercent(widthOffset)
                    + "  V " + signedPercent(heightOffset);
        }

        graphics.fill(x, y, x + 2, y + 34, accent);
        drawTrimmed(graphics, font, product, x + 7, y, maxWidth, main);
        drawTrimmed(graphics, font, runtime, x + 7, y + 12, maxWidth, dim);
        drawTrimmed(graphics, font, state, x + 7, y + 24, maxWidth, dim);
    }

    static void renderSettingsButton(GuiGraphics graphics, Font font, int width, int mouseX, int mouseY,
                                     float hoverAnimation) {
        int x = settingsButtonX(width);
        int y = SETTINGS_MARGIN;
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        int rest = UiTheme.card(AnkiConfig.getUiOpacity(), 1.0f);
        int hovered = UiTheme.withAlpha(accent & 0x00FFFFFF, 78);
        graphics.fill(x, y, x + SETTINGS_SIZE, y + SETTINGS_SIZE,
                UiTheme.mix(rest, hovered, hoverAnimation));
        border(graphics, x, y, SETTINGS_SIZE, SETTINGS_SIZE,
                UiTheme.mix(UiTheme.themedBorder(1.0f, 1.0f), accent, hoverAnimation));
        Component icon = UiIcons.component(UiIcons.SETTINGS);
        VersionCompat.get().drawString(graphics, font, icon,
                x + (SETTINGS_SIZE - font.width(icon)) / 2,
                y + (SETTINGS_SIZE - font.lineHeight) / 2,
                hoverAnimation > 0.35f ? UiTheme.textMain() : UiTheme.textDim(), false);
        if (isSettingsButton(mouseX, mouseY, width)) {
            VersionCompat.get().renderTooltip(graphics, font,
                    Component.translatable("ankinbt.config.title"), mouseX, mouseY);
        }
    }

    static boolean isSettingsButton(double mouseX, double mouseY, int width) {
        int x = settingsButtonX(width);
        return mouseX >= x && mouseX < x + SETTINGS_SIZE
                && mouseY >= SETTINGS_MARGIN && mouseY < SETTINGS_MARGIN + SETTINGS_SIZE;
    }

    private static int settingsButtonX(int width) {
        return Math.max(2, width - SETTINGS_MARGIN - SETTINGS_SIZE);
    }

    private static void drawTrimmed(GuiGraphics graphics, Font font, String value, int x, int y,
                                    int maxWidth, int color) {
        String output = value;
        if (font.width(output) > maxWidth) {
            int dots = font.width("...");
            output = font.plainSubstrByWidth(output, Math.max(1, maxWidth - dots)) + "...";
        }
        graphics.drawString(font, output, x, y, color, true);
    }

    private static String signedPercent(int value) {
        return (value >= 0 ? "+" : "") + value + "%";
    }

    private static void border(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static String modVersion(String id, String fallback) {
        return ModList.get().getModContainerById(id)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse(fallback);
    }
}
