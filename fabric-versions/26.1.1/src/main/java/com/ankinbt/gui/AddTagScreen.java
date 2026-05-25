package com.ankinbt.gui;

import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtTreeNode;
import net.minecraft.client.Minecraft;
import com.ankinbt.compat.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class AddTagScreen extends Screen {

    private static final int PW = 300, PH = 200;
    private static final int BG = 0xE8080810;
    private static final int BORDER = 0xFF222236;
    private static final int ACCENT = 0xFF6366F1;
    private static final int C1 = 0xFFE2E8F0, C2 = 0xFF94A3B8, C3 = 0xFF64748B;
    private static final int INPUT_BG = 0xFF12121E;
    private static final int ERR = 0xFFEF4444;

    private static final String[] TYPE_NAMES = {
            "Byte", "Short", "Int", "Long", "Float", "Double", "String", "Compound", "List"
    };
    private static final byte[] TYPE_IDS = {
            Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT, Tag.TAG_LONG,
            Tag.TAG_FLOAT, Tag.TAG_DOUBLE, Tag.TAG_STRING, Tag.TAG_COMPOUND, Tag.TAG_LIST
    };

    private final NbtEditorScreen parent;
    private final NbtTreeNode targetNode;
    private String keyInput = "";
    private int keyCursor = 0;
    private int selectedType = 6;
    private String error = null;
    private int px, py;

    public AddTagScreen(NbtEditorScreen parent, NbtTreeNode targetNode) {
        super(Component.translatable("ankinbt.add.title"));
        this.parent = parent;
        this.targetNode = targetNode;
    }

    @Override
    protected void init() {
        super.init();
        px = (width - PW) / 2;
        py = (height - PH) / 2;
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mx, int my, float pt) {
        render(new com.ankinbt.compat.GuiGraphics(g), mx, my, pt);
    }

    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x80000000);
        g.fill(px, py, px + PW, py + PH, BG);
        border(g, px, py, PW, PH, BORDER);

        g.drawString(font, Component.translatable("ankinbt.add.title"), px + 12, py + 10, C1, false);
        g.fill(px + 1, py + 26, px + PW - 1, py + 27, BORDER);

        int iy = py + 34;
        g.drawString(font, Component.translatable("ankinbt.add.key"), px + 12, iy + 2, C2, false);
        iy += 14;
        int ix = px + 12, iw = PW - 24, ih = 20;
        g.fill(ix, iy, ix + iw, iy + ih, INPUT_BG);
        border(g, ix, iy, iw, ih, ACCENT);
        String kd = keyInput.isEmpty() ? Component.translatable("ankinbt.add.key.hint").getString() : keyInput;
        g.drawString(font, kd + (System.currentTimeMillis() % 1000 < 500 ? "_" : ""),
                ix + 4, iy + 6, keyInput.isEmpty() ? C3 : C1, false);

        iy += ih + 8;
        g.drawString(font, Component.translatable("ankinbt.add.type"), px + 12, iy, C2, false);
        iy += 12;
        int cols = 3, bw = (PW - 24 - (cols - 1) * 4) / cols, bh = 18;
        for (int i = 0; i < TYPE_NAMES.length; i++) {
            int col = i % cols, row = i / cols;
            int bx = px + 12 + col * (bw + 4), by = iy + row * (bh + 3);
            boolean hover = mx >= bx && mx < bx + bw && my >= by && my < by + bh;
            g.fill(bx, by, bx + bw, by + bh, i == selectedType ? ACCENT : (hover ? 0x50FFFFFF : 0x30FFFFFF));
            g.drawString(font, TYPE_NAMES[i], bx + (bw - font.width(TYPE_NAMES[i])) / 2, by + 5, i == selectedType ? C1 : C2, false);
        }

        if (error != null) g.drawString(font, error, px + 12, py + PH - 38, ERR, false);

        int btnY = py + PH - 30, btnW = 80, btnH = 22;
        int cancelX = px + PW / 2 - btnW - 8;
        boolean ch = mx >= cancelX && mx < cancelX + btnW && my >= btnY && my < btnY + btnH;
        g.fill(cancelX, btnY, cancelX + btnW, btnY + btnH, ch ? 0x50FFFFFF : 0x30FFFFFF);
        border(g, cancelX, btnY, btnW, btnH, BORDER);
        String cl = Component.translatable("ankinbt.edit.cancel").getString();
        g.drawString(font, cl, cancelX + (btnW - font.width(cl)) / 2, btnY + 7, C2, false);

        int okX = px + PW / 2 + 8;
        boolean oh = mx >= okX && mx < okX + btnW && my >= btnY && my < btnY + btnH;
        g.fill(okX, btnY, okX + btnW, btnY + btnH, oh ? ACCENT : 0xFF4F46E5);
        border(g, okX, btnY, btnW, btnH, ACCENT);
        String ol = Component.translatable("ankinbt.add.confirm").getString();
        g.drawString(font, ol, okX + (btnW - font.width(ol)) / 2, btnY + 7, C1, false);
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c); g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c); g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x(); double my = event.y();
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
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key(); int scan = event.scancode(); int mod = 0;
        if (key == 256) { goBack(); return true; }
        if (key == 257 || key == 335) { confirm(); return true; }
        if (key == 259 && keyCursor > 0) {
            keyInput = keyInput.substring(0, keyCursor - 1) + keyInput.substring(keyCursor);
            keyCursor--; error = null; return true;
        }
        if (key == 263 && keyCursor > 0) { keyCursor--; return true; }
        if (key == 262 && keyCursor < keyInput.length()) { keyCursor++; return true; }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char c = (char) event.codepoint();
        if (c >= 32) {
            keyInput = keyInput.substring(0, keyCursor) + c + keyInput.substring(keyCursor);
            keyCursor++; error = null; return true;
        }
        return super.charTyped(event);
    }

    private void confirm() {
        if (targetNode.isList()) {
            Tag tag = NbtHelper.createDefault(TYPE_IDS[selectedType]);
            parent.addTagToNode(targetNode, "", tag);
            goBack(); return;
        }
        if (keyInput.isEmpty()) { error = Component.translatable("ankinbt.add.error.empty").getString(); return; }
        for (var child : targetNode.getChildren()) {
            if (child.getKey().equals(keyInput)) {
                error = Component.translatable("ankinbt.add.error.exists").getString(); return;
            }
        }
        Tag tag = NbtHelper.createDefault(TYPE_IDS[selectedType]);
        parent.addTagToNode(targetNode, keyInput, tag);
        goBack();
    }

    private void goBack() { Minecraft.getInstance().setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}


