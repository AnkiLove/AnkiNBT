package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.util.UiSound;
import com.ankinbt.util.TextEditBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class ThemeCustomizeScreen extends Screen {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 190;

    private final Screen parent;
    private final TextEditBuffer accentValue;
    private final TextEditBuffer backgroundValue;
    private int activeField;
    private boolean draggingField;
    private String error = "";
    private float openAnim;
    private float cancelHover;
    private float applyHover;

    ThemeCustomizeScreen(Screen parent) {
        super(Component.translatable("ankinbt.config.ui_custom_colors"));
        this.parent = parent;
        this.accentValue = new TextEditBuffer(AnkiConfig.getUiAccentHex());
        this.backgroundValue = new TextEditBuffer(AnkiConfig.getUiBackgroundHex());
    }

    @Override
    protected void init() {
        super.init();
        activeField = 0;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.08f, AnkiConfig.getUiAnimationSpeed()) : 1f;
        openAnim = UiTheme.approach(openAnim, 1f, speed);
        int px = (width - PANEL_W) / 2;
        int baseY = (height - PANEL_H) / 2;
        int py = baseY + Math.round((1f - openAnim) * 14f);
        int accent = previewColor(accentValue.value(), UiTheme.accent(AnkiConfig.getUiAccentPreset()));
        float opacity = AnkiConfig.getUiOpacity();

        g.fill(0, 0, width, height, UiTheme.scrim(opacity, openAnim));
        if (AnkiConfig.isUiShadowEnabled()) {
            g.fill(px + 4, py + 5, px + PANEL_W + 4, py + PANEL_H + 5,
                    UiTheme.shadow(opacity, openAnim, true));
        }
        g.fill(px, py, px + PANEL_W, py + PANEL_H, UiTheme.surface(opacity, openAnim));
        border(g, px, py, PANEL_W, PANEL_H, UiTheme.themedBorder(opacity, openAnim));
        g.fill(px + 1, py + 1, px + PANEL_W - 1, py + 34, UiTheme.toolbar(opacity, openAnim));
        g.fill(px + 1, py + 34, px + PANEL_W - 1, py + 35, accent);

        g.drawString(font, title, px + 12, py + 12, UiTheme.textMain(), false);
        renderField(g, px + 16, py + 52, PANEL_W - 32, 26, 0,
                Component.translatable("ankinbt.config.ui_custom_accent").getString(), accentValue, accent);
        int background = previewColor(backgroundValue.value(), 0xFF080B10);
        renderField(g, px + 16, py + 92, PANEL_W - 32, 26, 1,
                Component.translatable("ankinbt.config.ui_custom_background").getString(), backgroundValue, background);

        if (!error.isEmpty()) g.drawString(font, error, px + 16, py + 124, UiTheme.TXT_ERR, false);
        else g.drawString(font, Component.translatable("ankinbt.config.ui_hex_hint"), px + 16, py + 124, UiTheme.textDim(), false);

        int buttonY = py + PANEL_H - 34;
        int buttonW = 92;
        int cancelX = px + PANEL_W / 2 - buttonW - 7;
        int applyX = px + PANEL_W / 2 + 7;
        float hoverSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.18f, speed * 2.4f) : 1f;
        cancelHover = UiTheme.approach(cancelHover, inside(mouseX, mouseY, cancelX, buttonY, buttonW, 22) ? 1f : 0f, hoverSpeed);
        applyHover = UiTheme.approach(applyHover, inside(mouseX, mouseY, applyX, buttonY, buttonW, 22) ? 1f : 0f, hoverSpeed);
        renderButton(g, cancelX, buttonY, buttonW, Component.translatable("ankinbt.edit.cancel").getString(), cancelHover, false, accent);
        renderButton(g, applyX, buttonY, buttonW, Component.translatable("ankinbt.edit.apply").getString(), applyHover, true, accent);
    }

    private void renderField(GuiGraphics g, int x, int y, int w, int h, int index, String label,
                             TextEditBuffer buffer, int swatch) {
        String value = buffer.value();
        g.drawString(font, label, x, y - 11, UiTheme.textDim(), false);
        int borderColor = activeField == index ? swatch : UiTheme.themedBorder(1f, 1f);
        g.fill(x, y, x + w, y + h, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
        border(g, x, y, w, h, borderColor);
        g.fill(x + 7, y + 7, x + 19, y + h - 7, swatch);
        g.drawString(font, value, x + 27, y + 9, UiTheme.textMain(), false);
        if (activeField == index && buffer.hasSelection()) {
            int sx = x + 27 + font.width(value.substring(0, buffer.selectionStart()));
            int ex = x + 27 + font.width(value.substring(0, buffer.selectionEnd()));
            g.fill(sx, y + 5, ex, y + h - 5, 0x663B82F6);
            g.drawString(font, value.substring(buffer.selectionStart(), buffer.selectionEnd()),
                    sx, y + 9, UiTheme.textMain(), false);
        }
        if (activeField == index && System.currentTimeMillis() % 1000L < 500L) {
            int cursorX = x + 27 + font.width(value.substring(0, buffer.cursor()));
            g.fill(cursorX, y + 6, cursorX + 1, y + h - 6, UiTheme.textMain());
        }
    }

    private void renderButton(GuiGraphics g, int x, int y, int w, String label, float hover, boolean primary, int accent) {
        int rest = primary ? UiTheme.withAlpha(accent & 0x00FFFFFF, 178) : UiTheme.withAlpha(0xFFFFFF, 36);
        int hot = primary ? accent : UiTheme.withAlpha(0xFFFFFF, 72);
        g.fill(x, y, x + w, y + 22, UiTheme.mix(rest, hot, hover));
        border(g, x, y, w, 22, primary ? accent : UiTheme.themedBorder(1f, 1f));
        g.drawString(font, label, x + (w - font.width(label)) / 2, y + 7,
                primary ? 0xFFFFFFFF : UiTheme.textMain(), false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2 + Math.round((1f - openAnim) * 14f);
        draggingField = false;
        if (inside(event.x(), event.y(), px + 16, py + 52, PANEL_W - 32, 26)) {
            activeField = 0;
            focusField(accentValue, event.x() - (px + 43), isDoubleClick);
            return true;
        }
        if (inside(event.x(), event.y(), px + 16, py + 92, PANEL_W - 32, 26)) {
            activeField = 1;
            focusField(backgroundValue, event.x() - (px + 43), isDoubleClick);
            return true;
        }
        int buttonY = py + PANEL_H - 34;
        int buttonW = 92;
        if (inside(event.x(), event.y(), px + PANEL_W / 2 - buttonW - 7, buttonY, buttonW, 22)) {
            UiSound.playClick();
            closeToParent();
            return true;
        }
        if (inside(event.x(), event.y(), px + PANEL_W / 2 + 7, buttonY, buttonW, 22)) {
            UiSound.playClick();
            applyColors();
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!draggingField || event.button() != 0) return super.mouseDragged(event, dragX, dragY);
        TextEditBuffer buffer = activeField == 0 ? accentValue : backgroundValue;
        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2 + Math.round((1f - openAnim) * 14f);
        int fieldY = activeField == 0 ? py + 52 : py + 92;
        if (event.y() >= fieldY - 6 && event.y() <= fieldY + 32) {
            buffer.moveTo(cursorFromMouse(buffer, (int) Math.round(event.x()) - (px + 43)), true);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = draggingField;
        draggingField = false;
        return handled || super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            closeToParent();
            return true;
        }
        if (event.key() == 258) {
            activeField = 1 - activeField;
            return true;
        }
        if (event.key() == 257 || event.key() == 335) {
            applyColors();
            return true;
        }
        TextEditBuffer buffer = activeField == 0 ? accentValue : backgroundValue;
        if (buffer.keyPressed(event.key(), event.modifiers())) { error = ""; return true; }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char value = Character.toUpperCase((char) event.codepoint());
        if (!(value == '#' || value >= '0' && value <= '9' || value >= 'A' && value <= 'F')) return true;
        TextEditBuffer buffer = activeField == 0 ? accentValue : backgroundValue;
        if (buffer.value().length() >= 7 && !buffer.hasSelection()) return true;
        buffer.charTyped(value);
        error = "";
        return true;
    }

    private void applyColors() {
        String accent = normalize(accentValue.value());
        String background = normalize(backgroundValue.value());
        if (accent == null || background == null) {
            error = Component.translatable("ankinbt.config.ui_hex_error").getString();
            return;
        }
        AnkiConfig.setUiAccentHex(accent);
        AnkiConfig.setUiBackgroundHex(background);
        AnkiConfig.setUiAccentPreset(4);
        AnkiConfig.setUiBackgroundPreset(4);
        closeToParent();
    }

    private void focusField(TextEditBuffer buffer, double localX, boolean selectAll) {
        if (selectAll) buffer.selectAll();
        else buffer.moveTo(cursorFromMouse(buffer, (int) Math.round(localX)), false);
        draggingField = true;
    }

    private int cursorFromMouse(TextEditBuffer buffer, int localX) {
        String value = buffer.value();
        int best = 0;
        int distance = Integer.MAX_VALUE;
        for (int i = 0; i <= value.length(); i++) {
            int current = Math.abs(font.width(value.substring(0, i)) - Math.max(0, localX));
            if (current < distance) {
                distance = current;
                best = i;
            }
        }
        return best;
    }

    private static String normalize(String value) {
        String result = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        if (!result.startsWith("#")) result = "#" + result;
        return result.matches("#[0-9A-F]{6}") ? result : null;
    }

    private static String appendHex(String current, char value) {
        String result = current == null ? "" : current;
        if (value == '#') return result.isEmpty() ? "#" : result;
        if (result.isEmpty()) result = "#";
        return result.length() < 7 ? result + value : result;
    }

    private static int previewColor(String value, int fallback) {
        String normalized = normalize(value);
        if (normalized == null) return fallback;
        return 0xFF000000 | Integer.parseInt(normalized.substring(1), 16);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void border(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void closeToParent() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
