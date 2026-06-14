/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtTreeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class ValueEditScreen
extends Screen {
    private static final int PW = 320;
    private static final int PH = 150;
    private static final int BG = -402126832;
    private static final int BORDER = -14540234;
    private static final int ACCENT = -10262799;
    private static final int C1 = -1906448;
    private static final int C2 = -7035976;
    private static final int C3 = -10193781;
    private static final int INPUT_BG = -15592930;
    private static final int ERR = -1096636;
    private final NbtEditorScreen parent;
    private final NbtTreeNode node;
    private final String initialValue;
    private EditBox input;
    private String error = null;
    private int px;
    private int py;

    public ValueEditScreen(NbtEditorScreen parent, NbtTreeNode node) {
        super((Component)Component.translatable((String)"ankinbt.edit.title"));
        this.parent = parent;
        this.node = node;
        this.initialValue = VersionCompat.get().getTagAsString(node.getTag());
    }

    protected void init() {
        super.init();
        this.px = (this.width - 320) / 2;
        this.py = (this.height - 150) / 2;
        String value = this.input == null ? this.initialValue : this.input.getValue();
        this.input = new EditBox(this.font, this.px + 12, this.py + 36, 296, 24, Component.empty());
        this.input.setMaxLength(32767);
        this.input.setValue(value);
        this.input.setResponder(v -> this.error = null);
        this.input.setFocused(true);
    }

    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        g.fill(this.px, this.py, this.px + 320, this.py + 150, -402126832);
        this.border(g, this.px, this.py, 320, 150, -14540234);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.edit.editing", (Object[])new Object[]{this.node.getKey()}), this.px + 12, this.py + 10, -1906448, false);
        String type = this.node.getTypeName();
        g.drawString(this.font, type, this.px + 320 - this.font.width(type) - 12, this.py + 10, NbtHelper.getTagColor(this.node.getTag()), false);
        g.fill(this.px + 1, this.py + 26, this.px + 320 - 1, this.py + 27, -14540234);
        int ix = this.px + 12;
        int iy = this.py + 36;
        int iw = 296;
        int ih = 24;
        this.input.setX(ix);
        this.input.setY(iy);
        this.input.setWidth(iw);
        this.input.setFocused(true);
        this.input.render(g, mx, my, pt);
        if (this.error != null) {
            g.drawString(this.font, this.error, ix, iy + ih + 4, -1096636, false);
        }
        int by = this.py + 150 - 36;
        int bw = 80;
        int bh = 22;
        int cancelX = this.px + 160 - bw - 8;
        boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
        g.fill(cancelX, by, cancelX + bw, by + bh, ch ? 0x50FFFFFF : 0x30FFFFFF);
        this.border(g, cancelX, by, bw, bh, -14540234);
        String cl = Component.translatable((String)"ankinbt.edit.cancel").getString();
        g.drawString(this.font, cl, cancelX + (bw - this.font.width(cl)) / 2, by + 7, -7035976, false);
        int okX = this.px + 160 + 8;
        boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh;
        g.fill(okX, by, okX + bw, by + bh, oh ? -10262799 : -11581723);
        this.border(g, okX, by, bw, bh, -10262799);
        String ol = Component.translatable((String)"ankinbt.edit.apply").getString();
        g.drawString(this.font, ol, okX + (bw - this.font.width(ol)) / 2, by + 7, -1906448, false);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.edit.hint"), this.px + 12, this.py + 150 - 12, -10193781, false);
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        int by = this.py + 150 - 36;
        int bw = 80;
        int bh = 22;
        int cancelX = this.px + 160 - bw - 8;
        if (mx >= (double)cancelX && mx < (double)(cancelX + bw) && my >= (double)by && my < (double)(by + bh)) {
            this.goBack();
            return true;
        }
        int okX = this.px + 160 + 8;
        if (mx >= (double)okX && mx < (double)(okX + bw) && my >= (double)by && my < (double)(by + bh)) {
            this.apply();
            return true;
        }
        this.input.setX(this.px + 12);
        this.input.setY(this.py + 36);
        this.input.setWidth(296);
        if (this.input.mouseClicked(mx, my, btn)) {
            this.input.setFocused(true);
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    public boolean keyPressed(int key, int scan, int mod) {
        if (key == 256) {
            this.goBack();
            return true;
        }
        if (key == 257 || key == 335) {
            this.apply();
            return true;
        }
        if (this.input.keyPressed(key, scan, mod)) {
            return true;
        }
        return super.keyPressed(key, scan, mod);
    }

    public boolean charTyped(char c, int mod) {
        if (this.input.charTyped(c, mod)) {
            return true;
        }
        return super.charTyped(c, mod);
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
        Minecraft.getInstance().setScreen((Screen)this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}
