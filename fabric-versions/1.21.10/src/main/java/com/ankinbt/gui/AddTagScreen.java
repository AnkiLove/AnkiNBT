package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtTreeNode;
import com.ankinbt.util.FlatEditBox;
import com.ankinbt.util.UiSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class AddTagScreen extends Screen {

    private static final int PW = 300, PH = 200;
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
    private FlatEditBox keyInput;
    private int selectedType = 6;
    private String error = null;
    private int px, py, basePy;
    private float openAnim;
    private float cancelHover;
    private float confirmHover;
    private final float[] typeHover = new float[TYPE_NAMES.length];

    public AddTagScreen(NbtEditorScreen parent, NbtTreeNode targetNode) {
        super(Component.translatable("ankinbt.add.title"));
        this.parent = parent;
        this.targetNode = targetNode;
    }

    @Override
    protected void init() {
        super.init();
        px = (width - PW) / 2;
        basePy = (height - PH) / 2;
        py = basePy;
        String value = keyInput == null ? "" : keyInput.getValue();
        keyInput = new FlatEditBox(font, px + 12, py + 48, PW - 24, 20,
                Component.translatable("ankinbt.add.key"));
        keyInput.setMaxLength(32767);
        keyInput.setHint(Component.translatable("ankinbt.add.key.hint"));
        keyInput.setValue(value);
        keyInput.setResponder(v -> error = null);
        keyInput.setFocused(true);
        this.setFocused(keyInput);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.08f, AnkiConfig.getUiAnimationSpeed()) : 1f;
        float hoverSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.18f, speed * 2.4f) : 1f;
        openAnim = UiTheme.approach(openAnim, 1f, speed);
        py = basePy + Math.round((1f - openAnim) * 14f);
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(0, 0, width, height, UiTheme.scrim(AnkiConfig.getUiOpacity(), openAnim));
        if (AnkiConfig.isUiShadowEnabled()) g.fill(px + 4, py + 5, px + PW + 4, py + PH + 5,
                UiTheme.shadow(AnkiConfig.getUiOpacity(), openAnim, true));
        g.fill(px, py, px + PW, py + PH, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
        border(g, px, py, PW, PH, UiTheme.themedBorder(AnkiConfig.getUiOpacity(), openAnim));
        g.fill(px + 1, py + 1, px + PW - 1, py + 26, UiTheme.toolbar(AnkiConfig.getUiOpacity(), openAnim));

        g.drawString(font, Component.translatable("ankinbt.add.title"), px + 12, py + 10, UiTheme.textMain(), false);
        g.fill(px + 1, py + 26, px + PW - 1, py + 27, accent);

        int iy = py + 34;
        g.drawString(font, Component.translatable("ankinbt.add.key"), px + 12, iy + 2, UiTheme.textDim(), false);
        iy += 14;
        int ix = px + 12, iw = PW - 24, ih = 20;
        layoutKeyInput(ix, iy, iw, accent);
        keyInput.renderWidget(g, mx, my, pt);

        iy += ih + 8;
        g.drawString(font, Component.translatable("ankinbt.add.type"), px + 12, iy, UiTheme.textDim(), false);
        iy += 12;
        int cols = 3, bw = (PW - 24 - (cols - 1) * 4) / cols, bh = 18;
        for (int i = 0; i < TYPE_NAMES.length; i++) {
            int col = i % cols, row = i / cols;
            int bx = px + 12 + col * (bw + 4), by = iy + row * (bh + 3);
            boolean hover = mx >= bx && mx < bx + bw && my >= by && my < by + bh;
            typeHover[i] = UiTheme.approach(typeHover[i], hover ? 1f : 0f, hoverSpeed);
            g.fill(bx, by, bx + bw, by + bh, i == selectedType ? accent
                    : UiTheme.mix(0x20FFFFFF, 0x58FFFFFF, typeHover[i]));
            g.drawString(font, TYPE_NAMES[i], bx + (bw - font.width(TYPE_NAMES[i])) / 2, by + 5,
                    i == selectedType ? UiTheme.textMain() : UiTheme.textDim(), false);
        }

        if (error != null) g.drawString(font, error, px + 12, py + PH - 38, ERR, false);

        int btnY = py + PH - 30, btnW = 80, btnH = 22;
        int cancelX = px + PW / 2 - btnW - 8;
        boolean ch = mx >= cancelX && mx < cancelX + btnW && my >= btnY && my < btnY + btnH;
        cancelHover = UiTheme.approach(cancelHover, ch ? 1f : 0f, hoverSpeed);
        g.fill(cancelX, btnY, cancelX + btnW, btnY + btnH, UiTheme.mix(0x28FFFFFF, 0x60FFFFFF, cancelHover));
        border(g, cancelX, btnY, btnW, btnH, UiTheme.themedBorder(1f, 1f));
        String cl = Component.translatable("ankinbt.edit.cancel").getString();
        g.drawString(font, cl, cancelX + (btnW - font.width(cl)) / 2, btnY + 7, UiTheme.textDim(), false);

        int okX = px + PW / 2 + 8;
        boolean oh = mx >= okX && mx < okX + btnW && my >= btnY && my < btnY + btnH;
        confirmHover = UiTheme.approach(confirmHover, oh ? 1f : 0f, hoverSpeed);
        g.fill(okX, btnY, okX + btnW, btnY + btnH,
                UiTheme.mix(UiTheme.withAlpha(accent & 0x00FFFFFF, 176), accent, confirmHover));
        border(g, okX, btnY, btnW, btnH, accent);
        String ol = Component.translatable("ankinbt.add.confirm").getString();
        g.drawString(font, ol, okX + (btnW - font.width(ol)) / 2, btnY + 7, UiTheme.textMain(), false);
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c); g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c); g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private void layoutKeyInput(int x, int y, int width, int accent) {
        keyInput.setX(x);
        keyInput.setY(y);
        keyInput.setWidth(width);
        keyInput.setThemeColors(UiTheme.withAlpha(UiTheme.baseRgb(), 245),
                UiTheme.themedBorder(1f, 1f), accent);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        layoutKeyInput(px + 12, py + 48, PW - 24, UiTheme.accent(AnkiConfig.getUiAccentPreset()));
        if (keyInput.mouseClicked(event, isDoubleClick)) {
            keyInput.setFocused(true);
            this.setFocused(keyInput);
            return true;
        }
        return handleMouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        return handleMouseClicked(mx, my, btn);
    }

    private boolean handleMouseClicked(double mx, double my, int btn) {
        int inputY = py + 48;
        if (keyInput != null && mx >= px + 12 && mx < px + PW - 12
                && my >= inputY && my < inputY + 20) {
            keyInput.setFocused(true);
            keyInput.setCursorPosition(keyInput.getValue().length());
            this.setFocused(keyInput);
            return true;
        }
        int iy = py + 34 + 14 + 20 + 8 + 12;
        int cols = 3, bw = (PW - 24 - (cols - 1) * 4) / cols, bh = 18;
        for (int i = 0; i < TYPE_NAMES.length; i++) {
            int col = i % cols, row = i / cols;
            int bx = px + 12 + col * (bw + 4), by = iy + row * (bh + 3);
            if (mx >= bx && mx < bx + bw && my >= by && my < by + bh) { selectedType = i; UiSound.playClick(); return true; }
        }
        int btnY = py + PH - 30, btnW = 80, btnH = 22;
        int cancelX = px + PW / 2 - btnW - 8;
        if (mx >= cancelX && mx < cancelX + btnW && my >= btnY && my < btnY + btnH) { UiSound.playClick(); goBack(); return true; }
        int okX = px + PW / 2 + 8;
        if (mx >= okX && mx < okX + btnW && my >= btnY && my < btnY + btnH) { UiSound.playClick(); confirm(); return true; }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) { goBack(); return true; }
        if (event.key() == 257 || event.key() == 335) { confirm(); return true; }
        if (keyInput.keyPressed(event)) { error = null; return true; }
        return false;
    }

    public boolean keyPressed(int key, int scan, int mod) {
        if (key == 256) { goBack(); return true; }
        if (key == 257 || key == 335) { confirm(); return true; }
        if (keyInput.keyPressed(new KeyEvent(key, scan, mod))) { error = null; return true; }
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        if (keyInput.charTyped(event)) { error = null; return true; }
        return false;
    }

    public boolean charTyped(char c, int mod) {
        if (c >= 32) {
            keyInput.insertText(Character.toString(c));
            error = null;
            return true;
        }
        return false;
    }

    private void confirm() {
        if (targetNode.isList()) {
            Tag tag = NbtHelper.createDefault(TYPE_IDS[selectedType]);
            parent.addTagToNode(targetNode, "", tag);
            goBack(); return;
        }
        String keyValue = keyInput.getValue();
        if (keyValue.isEmpty()) { error = Component.translatable("ankinbt.add.error.empty").getString(); return; }
        for (var child : targetNode.getChildren()) {
            if (child.getKey().equals(keyValue)) {
                error = Component.translatable("ankinbt.add.error.exists").getString(); return;
            }
        }
        Tag tag = NbtHelper.createDefault(TYPE_IDS[selectedType]);
        parent.addTagToNode(targetNode, keyValue, tag);
        goBack();
    }

    private void goBack() { parent.returnFromChildScreen(); }
    @Override public boolean isPauseScreen() { return false; }
}
