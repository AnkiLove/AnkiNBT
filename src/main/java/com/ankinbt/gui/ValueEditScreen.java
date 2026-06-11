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

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtTreeNode;
import com.ankinbt.util.TextEditBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    private final TextEditBuffer input;
    private String error = null;
    private int cursor;
    private int px;
    private int py;
    private boolean draggingSelection;

    public ValueEditScreen(NbtEditorScreen parent, NbtTreeNode node) {
        super((Component)Component.translatable((String)"ankinbt.edit.title"));
        this.parent = parent;
        this.node = node;
        this.input = new TextEditBuffer(VersionCompat.get().getTagAsString(node.getTag()));
        this.cursor = this.input.cursor();
    }

    protected void init() {
        super.init();
        this.px = (this.width - 320) / 2;
        this.py = (this.height - 150) / 2;
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
        g.fill(ix, iy, ix + iw, iy + ih, -15592930);
        this.border(g, ix, iy, iw, ih, -10262799);
        String value = this.input.value();
        this.cursor = this.input.cursor();
        int viewStart = this.viewStart(iw - 8);
        String disp = this.visibleValue(viewStart, iw - 8);
        if (this.input.hasSelection()) {
            int selStart = Math.max(this.input.selectionStart(), viewStart);
            int selEnd = Math.min(this.input.selectionEnd(), viewStart + disp.length());
            if (selStart < selEnd) {
                int sx = ix + 4 + this.font.width(value.substring(viewStart, selStart));
                int ex = ix + 4 + this.font.width(value.substring(viewStart, selEnd));
                g.fill(sx, iy + 4, ex, iy + ih - 4, 1715176182);
            }
        }
        g.drawString(this.font, disp, ix + 4, iy + 8, -1906448, false);
        if (!this.input.hasSelection() && System.currentTimeMillis() % 1000L < 500L) {
            int cx = ix + 4 + this.font.width(value.substring(viewStart, Math.min(this.cursor, value.length())));
            g.fill(cx, iy + 4, cx + 1, iy + ih - 4, -1906448);
        }
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

    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x();
        double my = event.y();
        if (isDoubleClick && this.inInput(mx, my)) {
            this.input.selectAll();
            this.draggingSelection = true;
            return true;
        }
        if (this.mouseClicked(mx, my, event.button())) {
            return true;
        }
        return super.mouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseClicked(double mx, double my, int button) {
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
        int ix = this.px + 12;
        int iy = this.py + 36;
        int iw = 296;
        int ih = 24;
        if (mx >= ix && mx < ix + iw && my >= iy && my < iy + ih) {
            this.input.moveTo(this.colFromMouse(mx, ix + 4, iw - 8), (button == 0 && Screen.hasShiftDown()));
            this.draggingSelection = button == 0;
            return true;
        }
        this.draggingSelection = false;
        return super.mouseClicked(mx, my, button);
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingSelection) {
            int ix = this.px + 12;
            int iw = 296;
            this.input.moveTo(this.colFromMouse(event.x(), ix + 4, iw - 8), true);
            return true;
        }
        return super.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
    }

    public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {
        if (this.draggingSelection) {
            int ix = this.px + 12;
            int iw = 296;
            this.input.moveTo(this.colFromMouse(mx, ix + 4, iw - 8), true);
            return true;
        }
        return super.mouseDragged(mx, my, button, dragX, dragY);
    }

    public boolean mouseReleased(double mx, double my, int button) {
        this.draggingSelection = false;
        return super.mouseReleased(mx, my, button);
    }

    public boolean keyPressed(KeyEvent event) {
        if (this.keyPressed(event.key(), event.scancode(), event.modifiers())) {
            return true;
        }
        return super.keyPressed(event.key(), event.scancode(), event.modifiers());
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
        String before = this.input.value();
        if (this.input.keyPressed(key, mod)) {
            this.cursor = this.input.cursor();
            if (!before.equals(this.input.value())) {
                this.error = null;
            }
            return true;
        }
        return super.keyPressed(key, scan, mod);
    }

    public boolean charTyped(CharacterEvent event) {
        if (this.charTyped((char)event.codepoint(), event.modifiers())) {
            return true;
        }
        return super.charTyped((char)event.codepoint(), event.modifiers());
    }

    public boolean charTyped(char c, int modifiers) {
        if (this.input.charTyped(c)) {
            this.cursor = this.input.cursor();
            this.error = null;
            return true;
        }
        return super.charTyped(c, modifiers);
    }

    private void apply() {
        Tag newTag = NbtHelper.parseValue(this.input.value(), this.node.getTag());
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

    private int colFromMouse(double mx, int textX, int maxW) {
        String value = this.input.value();
        int viewStart = this.viewStart(maxW);
        int local = Math.max(0, (int)mx - textX);
        int best = viewStart;
        int bestDist = Integer.MAX_VALUE;
        int viewEnd = Math.min(value.length(), viewStart + this.visibleValue(viewStart, maxW).length());
        for (int i = viewStart; i <= viewEnd; i++) {
            int x = this.font.width(value.substring(viewStart, i));
            int dist = Math.abs(x - local);
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return best;
    }

    private boolean inInput(double mx, double my) {
        int ix = this.px + 12;
        int iy = this.py + 36;
        int iw = 296;
        int ih = 24;
        return mx >= ix && mx < ix + iw && my >= iy && my < iy + ih;
    }

    private int viewStart(int maxW) {
        String value = this.input.value();
        int start = 0;
        int cursorPos = Math.min(this.input.cursor(), value.length());
        while (start < cursorPos && this.font.width(value.substring(start, cursorPos)) > maxW) {
            ++start;
        }
        return start;
    }

    private String visibleValue(int viewStart, int maxW) {
        String value = this.input.value();
        if (viewStart >= value.length()) return "";
        StringBuilder out = new StringBuilder();
        for (int i = viewStart; i < value.length(); ++i) {
            String next = out.toString() + value.charAt(i);
            if (this.font.width(next) > maxW) break;
            out.append(value.charAt(i));
        }
        return out.toString();
    }

    public boolean isPauseScreen() {
        return false;
    }
}
