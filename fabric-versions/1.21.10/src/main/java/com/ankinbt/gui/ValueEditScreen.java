package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
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

public class ValueEditScreen extends Screen {

    private static final int PW = 320, PH = 150;
    private static final int BG = 0xE8080810;
    private static final int BORDER = 0xFF222236;
    private static final int ACCENT = 0xFF6366F1;
    private static final int C1 = 0xFFE2E8F0;
    private static final int C2 = 0xFF94A3B8;
    private static final int C3 = 0xFF64748B;
    private static final int INPUT_BG = 0xFF12121E;
    private static final int ERR = 0xFFEF4444;

    private final NbtEditorScreen parent;
    private final NbtTreeNode node;
    private String input;
    private String error = null;
    private int cursor;
    private int px, py;

    public ValueEditScreen(NbtEditorScreen parent, NbtTreeNode node) {
        super(Component.translatable("ankinbt.edit.title"));
        this.parent = parent;
        this.node = node;
        this.input = VersionCompat.get().getTagAsString(node.getTag());
        this.cursor = input.length();
    }

    @Override protected void init() { super.init(); px = (width - PW) / 2; py = (height - PH) / 2; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x80000000);
        g.fill(px, py, px + PW, py + PH, BG);
        border(g, px, py, PW, PH, BORDER);
        g.drawString(font, Component.translatable("ankinbt.edit.editing", node.getKey()), px + 12, py + 10, C1, false);
        String type = node.getTypeName();
        g.drawString(font, type, px + PW - font.width(type) - 12, py + 10, NbtHelper.getTagColor(node.getTag()), false);
        g.fill(px + 1, py + 26, px + PW - 1, py + 27, BORDER);


        int ix = px + 12, iy = py + 36, iw = PW - 24, ih = 24;
        g.fill(ix, iy, ix + iw, iy + ih, INPUT_BG);
        border(g, ix, iy, iw, ih, ACCENT);
        String disp = input;
        if (font.width(disp) > iw - 12) { int start = Math.max(0, cursor - 35); disp = ".." + input.substring(start); }
        g.drawString(font, disp, ix + 4, iy + 8, C1, false);
        if (System.currentTimeMillis() % 1000 < 500) {
            String before = input.substring(0, Math.min(cursor, input.length()));
            if (font.width(before) > iw - 12) before = ".." + before.substring(before.length() - 35);
            int cx = ix + 4 + font.width(before);
            g.fill(cx, iy + 4, cx + 1, iy + ih - 4, C1);
        }
        if (error != null) g.drawString(font, error, ix, iy + ih + 4, ERR, false);

        int by = py + PH - 36; int bw = 80, bh = 22;
        int cancelX = px + PW / 2 - bw - 8;
        boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
        g.fill(cancelX, by, cancelX + bw, by + bh, ch ? 0x50FFFFFF : 0x30FFFFFF);
        border(g, cancelX, by, bw, bh, BORDER);
        String cl = Component.translatable("ankinbt.edit.cancel").getString();
        g.drawString(font, cl, cancelX + (bw - font.width(cl)) / 2, by + 7, C2, false);

        int okX = px + PW / 2 + 8;
        boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh;
        g.fill(okX, by, okX + bw, by + bh, oh ? ACCENT : 0xFF4F46E5);
        border(g, okX, by, bw, bh, ACCENT);
        String ol = Component.translatable("ankinbt.edit.apply").getString();
        g.drawString(font, ol, okX + (bw - font.width(ol)) / 2, by + 7, C1, false);
        g.drawString(font, Component.translatable("ankinbt.edit.hint"), px + 12, py + PH - 12, C3, false);
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c); g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c); g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x(); double my = event.y();
        int by = py + PH - 36; int bw = 80, bh = 22;
        int cancelX = px + PW / 2 - bw - 8;
        if (mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh) { goBack(); return true; }
        int okX = px + PW / 2 + 8;
        if (mx >= okX && mx < okX + bw && my >= by && my < by + bh) { apply(); return true; }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key(); int mod = event.modifiers();
        if (key == 256) { goBack(); return true; }
        if (key == 257 || key == 335) { apply(); return true; }
        if (key == 259 && cursor > 0) { input = input.substring(0, cursor - 1) + input.substring(cursor); cursor--; error = null; return true; }
        if (key == 261 && cursor < input.length()) { input = input.substring(0, cursor) + input.substring(cursor + 1); error = null; return true; }
        if (key == 263 && cursor > 0) { cursor--; return true; }
        if (key == 262 && cursor < input.length()) { cursor++; return true; }
        if (key == 268) { cursor = 0; return true; }
        if (key == 269) { cursor = input.length(); return true; }
        if (key == 86 && (mod & 2) != 0) {
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (clip != null && !clip.isEmpty()) { input = input.substring(0, cursor) + clip + input.substring(cursor); cursor += clip.length(); error = null; }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char c = (char) event.codepoint();
        if (c >= 32) { input = input.substring(0, cursor) + c + input.substring(cursor); cursor++; error = null; return true; }
        return super.charTyped(event);
    }

    private void apply() {
        Tag newTag = NbtHelper.parseValue(input, node.getTag());
        if (newTag == null) { error = Component.translatable("ankinbt.edit.error", node.getTypeName()).getString(); return; }
        node.setTag(newTag); node.applyToParent(); parent.onNodeEdited(); goBack();
    }

    private void goBack() { Minecraft.getInstance().setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}