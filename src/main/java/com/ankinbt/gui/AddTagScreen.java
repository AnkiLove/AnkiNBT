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
    private String keyInput = "";
    private int keyCursor = 0;
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
        String kd = this.keyInput.isEmpty() ? Component.translatable((String)"ankinbt.add.key.hint").getString() : this.keyInput;
        g.drawString(this.font, kd + (System.currentTimeMillis() % 1000L < 500L ? "_" : ""), ix + 4, iy + 6, this.keyInput.isEmpty() ? -10193781 : -1906448, false);
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
        double mx = event.x();
        double my = event.y();
        int iy = this.py + 34 + 14 + 20 + 8 + 12;
        int cols = 3;
        int bw = (276 - (cols - 1) * 4) / cols;
        int bh = 18;
        for (int i = 0; i < TYPE_NAMES.length; ++i) {
            int col = i % cols;
            int row = i / cols;
            int bx = this.px + 12 + col * (bw + 4);
            int by = iy + row * (bh + 3);
            if (!(mx >= (double)bx) || !(mx < (double)(bx + bw)) || !(my >= (double)by) || !(my < (double)(by + bh))) continue;
            this.selectedType = i;
            return true;
        }
        int btnY = this.py + 200 - 30;
        int btnW = 80;
        int btnH = 22;
        int cancelX = this.px + 150 - btnW - 8;
        if (mx >= (double)cancelX && mx < (double)(cancelX + btnW) && my >= (double)btnY && my < (double)(btnY + btnH)) {
            this.goBack();
            return true;
        }
        int okX = this.px + 150 + 8;
        if (mx >= (double)okX && mx < (double)(okX + btnW) && my >= (double)btnY && my < (double)(btnY + btnH)) {
            this.confirm();
            return true;
        }
        return super.mouseClicked(mx, my, event.button());
    }

    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        int scan = event.scancode();
        int mod = event.modifiers();
        if (key == 256) {
            this.goBack();
            return true;
        }
        if (key == 257 || key == 335) {
            this.confirm();
            return true;
        }
        if (key == 259 && this.keyCursor > 0) {
            this.keyInput = this.keyInput.substring(0, this.keyCursor - 1) + this.keyInput.substring(this.keyCursor);
            --this.keyCursor;
            this.error = null;
            return true;
        }
        if (key == 263 && this.keyCursor > 0) {
            --this.keyCursor;
            return true;
        }
        if (key == 262 && this.keyCursor < this.keyInput.length()) {
            ++this.keyCursor;
            return true;
        }
        return super.keyPressed(key, scan, mod);
    }

    public boolean charTyped(CharacterEvent event) {
        char c = (char)event.codepoint();
        if (c >= ' ') {
            this.keyInput = this.keyInput.substring(0, this.keyCursor) + c + this.keyInput.substring(this.keyCursor);
            ++this.keyCursor;
            this.error = null;
            return true;
        }
        return super.charTyped(c, event.modifiers());
    }

    private void confirm() {
        if (this.targetNode.isList()) {
            Tag tag = NbtHelper.createDefault(TYPE_IDS[this.selectedType]);
            this.parent.addTagToNode(this.targetNode, "", tag);
            this.goBack();
            return;
        }
        if (this.keyInput.isEmpty()) {
            this.error = Component.translatable((String)"ankinbt.add.error.empty").getString();
            return;
        }
        for (NbtTreeNode child : this.targetNode.getChildren()) {
            if (!child.getKey().equals(this.keyInput)) continue;
            this.error = Component.translatable((String)"ankinbt.add.error.exists").getString();
            return;
        }
        Tag tag = NbtHelper.createDefault(TYPE_IDS[this.selectedType]);
        this.parent.addTagToNode(this.targetNode, this.keyInput, tag);
        this.goBack();
    }

    private void goBack() {
        Minecraft.getInstance().setScreen((Screen)this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}
