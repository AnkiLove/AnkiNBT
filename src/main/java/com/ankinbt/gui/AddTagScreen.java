/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 */
package com.ankinbt.gui;

import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtTreeNode;
import com.ankinbt.util.FlatEditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class AddTagScreen
extends Screen {
    private static final int PW = 300;
    private static final int PH = 200;
    private static final int BG = -402126832;
    private static final int BORDER = -14540234;
    private static final int ACCENT = -10262799;
    private static final int C1 = -1906448;
    private static final int C2 = -7035976;
    private static final int C3 = -10193781;
    private static final int INPUT_BG = -15592930;
    private static final int ERR = -1096636;
    private static final String[] TYPE_NAMES = new String[]{"Byte", "Short", "Int", "Long", "Float", "Double", "String", "Compound", "List"};
    private static final byte[] TYPE_IDS = new byte[]{1, 2, 3, 4, 5, 6, 8, 10, 9};
    private final NbtEditorScreen parent;
    private final NbtTreeNode targetNode;
    private FlatEditBox keyBox;
    private int selectedType = 6;
    private String error = null;
    private int px;
    private int py;

    public AddTagScreen(NbtEditorScreen parent, NbtTreeNode targetNode) {
        super((Component)Component.translatable((String)"ankinbt.add.title"));
        this.parent = parent;
        this.targetNode = targetNode;
    }

    protected void init() {
        super.init();
        this.px = (this.width - 300) / 2;
        this.py = (this.height - 200) / 2;
        this.ensureKeyBox();
    }

    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        g.fill(this.px, this.py, this.px + 300, this.py + 200, -402126832);
        this.border(g, this.px, this.py, 300, 200, -14540234);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.add.title"), this.px + 12, this.py + 10, -1906448, false);
        g.fill(this.px + 1, this.py + 26, this.px + 300 - 1, this.py + 27, -14540234);
        int iy = this.py + 34;
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.add.key"), this.px + 12, iy + 2, -7035976, false);
        int ix = this.px + 12;
        int iw = 276;
        int ih = 20;
        g.fill(ix, iy += 14, ix + iw, iy + ih, -15592930);
        this.border(g, ix, iy, iw, ih, -10262799);
        this.layoutKeyBox(ix, iy, iw, ih);
        this.keyBox.render(g, mx, my, pt);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.add.type"), this.px + 12, iy += ih + 8, -7035976, false);
        iy += 12;
        int cols = 3;
        int bw = (276 - (cols - 1) * 4) / cols;
        int bh = 18;
        for (int i = 0; i < TYPE_NAMES.length; ++i) {
            boolean hover;
            int col = i % cols;
            int row = i / cols;
            int bx = this.px + 12 + col * (bw + 4);
            int by = iy + row * (bh + 3);
            boolean bl = hover = mx >= bx && mx < bx + bw && my >= by && my < by + bh;
            g.fill(bx, by, bx + bw, by + bh, i == this.selectedType ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
            g.drawString(this.font, TYPE_NAMES[i], bx + (bw - this.font.width(TYPE_NAMES[i])) / 2, by + 5, i == this.selectedType ? -1906448 : -7035976, false);
        }
        if (this.error != null) {
            g.drawString(this.font, this.error, this.px + 12, this.py + 200 - 38, -1096636, false);
        }
        int btnY = this.py + 200 - 30;
        int btnW = 80;
        int btnH = 22;
        int cancelX = this.px + 150 - btnW - 8;
        boolean ch = mx >= cancelX && mx < cancelX + btnW && my >= btnY && my < btnY + btnH;
        g.fill(cancelX, btnY, cancelX + btnW, btnY + btnH, ch ? 0x50FFFFFF : 0x30FFFFFF);
        this.border(g, cancelX, btnY, btnW, btnH, -14540234);
        String cl = Component.translatable((String)"ankinbt.edit.cancel").getString();
        g.drawString(this.font, cl, cancelX + (btnW - this.font.width(cl)) / 2, btnY + 7, -7035976, false);
        int okX = this.px + 150 + 8;
        boolean oh = mx >= okX && mx < okX + btnW && my >= btnY && my < btnY + btnH;
        g.fill(okX, btnY, okX + btnW, btnY + btnH, oh ? -10262799 : -11581723);
        this.border(g, okX, btnY, btnW, btnH, -10262799);
        String ol = Component.translatable((String)"ankinbt.add.confirm").getString();
        g.drawString(this.font, ol, okX + (btnW - this.font.width(ol)) / 2, btnY + 7, -1906448, false);
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }


    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (this.tryMouseClicked(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        return this.tryMouseClicked(mx, my, btn);
    }

    private boolean tryMouseClicked(double mx, double my, int btn) {
        if (this.handleMouseClicked(mx, my, btn)) {
            return true;
        }
        if (this.minecraft != null) {
            double sw = this.minecraft.getWindow().getScreenWidth();
            double sh = this.minecraft.getWindow().getScreenHeight();
            if (sw > 0.0 && sh > 0.0) {
                double sx = mx * (double)this.width / sw;
                double sy = my * (double)this.height / sh;
                if ((Math.abs(sx - mx) > 0.5 || Math.abs(sy - my) > 0.5) && this.handleMouseClicked(sx, sy, btn)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleMouseClicked(double mx, double my, int btn) {
        this.ensureKeyBox();
        this.layoutKeyBox(this.px + 12, this.py + 34 + 14, 276, 20);
        if (this.keyBox.mouseClicked(mx, my, btn)) {
            this.keyBox.setFocused(true);
            return true;
        }
        int iy = py + 34 + 14 + 20 + 8 + 12;
        int cols = 3, bw = (PW - 24 - (cols - 1) * 4) / cols, bh = 18;
        for (int i = 0; i < TYPE_NAMES.length; i++) {
            int col = i % cols, row = i / cols;
            int bx = px + 12 + col * (bw + 4), by = iy + row * (bh + 3);
            if (mx >= bx && mx < bx + bw && my >= by && my < by + bh) { selectedType = i; return true; }
        }
        int btnY = py + PH - 30, btnW = 80, btnH = 22;
        int cancelX = px + PW / 2 - btnW - 8;
        if (mx >= cancelX && mx < cancelX + btnW && my >= btnY && my < btnY + btnH) { goBack(); return true; }
        int okX = px + PW / 2 + 8;
        if (mx >= okX && mx < okX + btnW && my >= btnY && my < btnY + btnH) { confirm(); return true; }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        if (this.keyPressed(event.key(), event.scancode(), event.modifiers())) {
            return true;
        }
        return super.keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public boolean keyPressed(int key, int scan, int mod) {
        if (this.handleKeyPressed(key, scan, mod)) {
            return true;
        }
        return super.keyPressed(key, scan, mod);
    }

    private boolean handleKeyPressed(int key, int scan, int mod) {
        if (key == 256) { goBack(); return true; }
        if (key == 257 || key == 335) { confirm(); return true; }
        this.ensureKeyBox();
        if (this.keyBox.keyPressed(key, scan, mod)) {
            this.error = null;
            return true;
        }
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        if (this.charTyped((char)event.codepoint(), event.modifiers())) {
            return true;
        }
        return super.charTyped((char)event.codepoint(), event.modifiers());
    }

    public boolean charTyped(char c, int mod) {
        if (this.handleCharTyped(c, mod)) {
            return true;
        }
        return super.charTyped(c, mod);
    }

    private boolean handleCharTyped(char c, int mod) {
        this.ensureKeyBox();
        if (this.keyBox.charTyped(c, mod)) {
            this.error = null;
            return true;
        }
        return false;
    }

    private void confirm() {
        if (this.targetNode.isList()) {
            Tag tag = NbtHelper.createDefault(TYPE_IDS[this.selectedType]);
            this.parent.addTagToNode(this.targetNode, "", tag);
            this.goBack();
            return;
        }
        String keyInput = this.keyBoxValue();
        if (keyInput.isEmpty()) {
            this.error = Component.translatable((String)"ankinbt.add.error.empty").getString();
            return;
        }
        for (NbtTreeNode child : this.targetNode.getChildren()) {
            if (!child.getKey().equals(keyInput)) continue;
            this.error = Component.translatable((String)"ankinbt.add.error.exists").getString();
            return;
        }
        Tag tag = NbtHelper.createDefault(TYPE_IDS[this.selectedType]);
        this.parent.addTagToNode(this.targetNode, keyInput, tag);
        this.goBack();
    }

    private void ensureKeyBox() {
        if (this.keyBox != null) {
            return;
        }
        this.keyBox = new FlatEditBox(this.font, this.px + 12, this.py + 48, 276, 20, Component.empty());
        this.keyBox.setMaxLength(2048);
        this.keyBox.setHint(Component.translatable((String)"ankinbt.add.key.hint"));
        this.keyBox.setResponder(value -> this.error = null);
        this.keyBox.setFocused(true);
    }

    private void layoutKeyBox(int x, int y, int w, int h) {
        this.ensureKeyBox();
        this.keyBox.setX(x);
        this.keyBox.setY(y);
        this.keyBox.setWidth(w);
        this.keyBox.setHeight(h);
        this.keyBox.setFocused(true);
    }

    private String keyBoxValue() {
        this.ensureKeyBox();
        String value = this.keyBox.getValue();
        return value == null ? "" : value.trim();
    }

    private void goBack() {
        Minecraft.getInstance().setScreen((Screen)this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}
