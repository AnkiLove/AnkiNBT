/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  com.ankinbt.compat.LegacyGuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtTreeNode;
import com.ankinbt.util.FlatEditBox;
import com.ankinbt.util.UiSound;
import net.minecraft.client.Minecraft;
import com.ankinbt.gui.LegacyGuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class ValueEditScreen
extends Screen {
    private static final int PW = 320;
    private static final int PH = 150;
    private static final int ERR = -1096636;
    private final NbtEditorScreen parent;
    private final NbtTreeNode node;
    private final String initialValue;
    private FlatEditBox input;
    private String error = null;
    private int px;
    private int py;
    private int basePy;
    private float openAnim;
    private float cancelHover;
    private float applyHover;

    public ValueEditScreen(NbtEditorScreen parent, NbtTreeNode node) {
        super((Component)Component.translatable((String)"ankinbt.edit.title"));
        this.parent = parent;
        this.node = node;
        this.initialValue = VersionCompat.get().getTagAsString(node.getTag());
    }

    protected void init() {
        super.init();
        this.px = (this.width - 320) / 2;
        this.basePy = (this.height - 150) / 2;
        this.py = this.basePy;
        String value = this.input == null ? this.initialValue : this.input.getValue();
        this.input = new FlatEditBox(this.font, this.px + 12, this.py + 36, 296, 24, Component.empty());
        this.input.setMaxLength(32767);
        this.input.setValue(value);
        this.input.setResponder(v -> this.error = null);
        this.input.setFocused(true);
        this.setFocused(this.input);
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        render(new LegacyGuiGraphics(graphics), mouseX, mouseY, partialTick);
    }

    public void render(LegacyGuiGraphics g, int mx, int my, float pt) {
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.08f, AnkiConfig.getUiAnimationSpeed()) : 1f;
        float hoverSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.18f, speed * 2.4f) : 1f;
        this.openAnim = UiTheme.approach(this.openAnim, 1f, speed);
        this.py = this.basePy + Math.round((1f - this.openAnim) * 14f);
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(0, 0, this.width, this.height, UiTheme.scrim(AnkiConfig.getUiOpacity(), this.openAnim));
        if (AnkiConfig.isUiShadowEnabled()) g.fill(this.px + 4, this.py + 5, this.px + 324, this.py + 155,
                UiTheme.shadow(AnkiConfig.getUiOpacity(), this.openAnim, true));
        g.fill(this.px, this.py, this.px + 320, this.py + 150, UiTheme.surface(AnkiConfig.getUiOpacity(), this.openAnim));
        this.border(g, this.px, this.py, 320, 150, UiTheme.themedBorder(AnkiConfig.getUiOpacity(), this.openAnim));
        g.fill(this.px + 1, this.py + 1, this.px + 319, this.py + 26, UiTheme.toolbar(AnkiConfig.getUiOpacity(), this.openAnim));
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.edit.editing", (Object[])new Object[]{this.node.getKey()}), this.px + 12, this.py + 10, UiTheme.textMain(), false);
        String type = this.node.getTypeName();
        g.drawString(this.font, type, this.px + 320 - this.font.width(type) - 12, this.py + 10, NbtHelper.getTagColor(this.node.getTag()), false);
        g.fill(this.px + 1, this.py + 26, this.px + 320 - 1, this.py + 27, accent);
        int ix = this.px + 12;
        int iy = this.py + 36;
        int iw = 296;
        int ih = 24;
        this.input.setX(ix);
        this.input.setY(iy);
        this.input.setWidth(iw);
        this.input.setThemeColors(UiTheme.withAlpha(UiTheme.baseRgb(), 245),
                UiTheme.themedBorder(1f, 1f), accent);
        this.input.setFocused(true);
        this.input.renderWidget(g.unwrap(), mx, my, pt);
        if (this.error != null) {
            g.drawString(this.font, this.error, ix, iy + ih + 4, -1096636, false);
        }
        int by = this.py + 150 - 36;
        int bw = 80;
        int bh = 22;
        int cancelX = this.px + 160 - bw - 8;
        boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
        this.cancelHover = UiTheme.approach(this.cancelHover, ch ? 1f : 0f, hoverSpeed);
        g.fill(cancelX, by, cancelX + bw, by + bh, UiTheme.mix(0x28FFFFFF, 0x60FFFFFF, this.cancelHover));
        this.border(g, cancelX, by, bw, bh, UiTheme.themedBorder(1f, 1f));
        String cl = Component.translatable((String)"ankinbt.edit.cancel").getString();
        g.drawString(this.font, cl, cancelX + (bw - this.font.width(cl)) / 2, by + 7, UiTheme.textDim(), false);
        int okX = this.px + 160 + 8;
        boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh;
        this.applyHover = UiTheme.approach(this.applyHover, oh ? 1f : 0f, hoverSpeed);
        g.fill(okX, by, okX + bw, by + bh,
                UiTheme.mix(UiTheme.withAlpha(accent & 0x00FFFFFF, 176), accent, this.applyHover));
        this.border(g, okX, by, bw, bh, accent);
        String ol = Component.translatable((String)"ankinbt.edit.apply").getString();
        g.drawString(this.font, ol, okX + (bw - this.font.width(ol)) / 2, by + 7, UiTheme.textMain(), false);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.edit.hint"), this.px + 12,
                this.py + 150 - 12, UiTheme.textDim(), false);
    }

    private void border(LegacyGuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int by = this.py + 150 - 36;
        int bw = 80;
        int bh = 22;
        int cancelX = this.px + 160 - bw - 8;
        if (mx >= (double)cancelX && mx < (double)(cancelX + bw) && my >= (double)by && my < (double)(by + bh)) {
            UiSound.playClick();
            this.goBack();
            return true;
        }
        int okX = this.px + 160 + 8;
        if (mx >= (double)okX && mx < (double)(okX + bw) && my >= (double)by && my < (double)(by + bh)) {
            UiSound.playClick();
            this.apply();
            return true;
        }
        int ix = this.px + 12;
        int iy = this.py + 36;
        int iw = 296;
        this.input.setX(ix);
        this.input.setY(iy);
        this.input.setWidth(iw);
        if (this.input.mouseClicked(mx, my, button)) {
            this.input.setFocused(true);
            this.setFocused(this.input);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) {
            this.goBack();
            return true;
        }
        if (key == 257 || key == 335) {
            this.apply();
            return true;
        }
        if (this.input.keyPressed(key, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.input.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void apply() {
        Tag newTag = NbtHelper.parseValue(this.input.getValue(), this.node.getTag());
        if (newTag == null) {
            this.error = Component.translatable((String)"ankinbt.edit.error", (Object[])new Object[]{this.node.getTypeName()}).getString();
            return;
        }
        this.node.setTag(newTag);
        this.node.applyToParent();
        this.parent.onNodeEdited();
        this.goBack();
    }

    private void goBack() {
        this.parent.returnFromChildScreen();
    }

    public boolean isPauseScreen() {
        return false;
    }
}
