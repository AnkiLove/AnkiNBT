/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2520
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtTreeNode;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class AddTagScreen
extends class_437 {
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
    private float openAnim = 0.0f;

    public AddTagScreen(NbtEditorScreen parent, NbtTreeNode targetNode) {
        super((class_2561)class_2561.method_43471((String)"ankinbt.add.title"));
        this.parent = parent;
        this.targetNode = targetNode;
    }

    protected void method_25426() {
        super.method_25426();
        this.px = (this.field_22789 - 300) / 2;
        this.py = (this.field_22790 - 200) / 2;
    }

    public void method_25394(class_332 g, int mx, int my, float pt) {
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        int scrim = this.fadeColor(Integer.MIN_VALUE, this.openAnim);
        int bg = this.fadeColor(-402126832, this.openAnim);
        int border = this.fadeColor(-14540234, this.openAnim);
        int accent = this.fadeColor(-10262799, this.openAnim);
        int inputBg = this.fadeColor(-15592930, this.openAnim);
        int err = this.fadeColor(-1096636, this.openAnim);
        g.method_25294(0, 0, this.field_22789, this.field_22790, scrim);
        g.method_25294(this.px, this.py, this.px + 300, this.py + 200, bg);
        this.border(g, this.px, this.py, 300, 200, border);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.add.title"), this.px + 12, this.py + 10, -1906448, false);
        g.method_25294(this.px + 1, this.py + 26, this.px + 300 - 1, this.py + 27, border);
        int iy = this.py + 34;
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.add.key"), this.px + 12, iy + 2, -7035976, false);
        int ix = this.px + 12;
        int iw = 276;
        int ih = 20;
        g.method_25294(ix, iy += 14, ix + iw, iy + ih, inputBg);
        this.border(g, ix, iy, iw, ih, accent);
        String kd = this.keyInput.isEmpty() ? class_2561.method_43471((String)"ankinbt.add.key.hint").getString() : this.keyInput;
        VersionCompat.get().drawString(g, this.field_22793, kd + (System.currentTimeMillis() % 1000L < 500L ? "_" : ""), ix + 4, iy + 6, this.keyInput.isEmpty() ? -10193781 : -1906448, false);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.add.type"), this.px + 12, iy += ih + 8, -7035976, false);
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
            g.method_25294(bx, by, bx + bw, by + bh, i == this.selectedType ? accent : this.fadeColor(hover ? 0x50FFFFFF : 0x30FFFFFF, this.openAnim));
            VersionCompat.get().drawString(g, this.field_22793, TYPE_NAMES[i], bx + (bw - this.field_22793.method_1727(TYPE_NAMES[i])) / 2, by + 5, i == this.selectedType ? -1906448 : -7035976, false);
        }
        if (this.error != null) {
            VersionCompat.get().drawString(g, this.field_22793, this.error, this.px + 12, this.py + 200 - 38, err, false);
        }
        int btnY = this.py + 200 - 30;
        int btnW = 80;
        int btnH = 22;
        int cancelX = this.px + 150 - btnW - 8;
        boolean ch = mx >= cancelX && mx < cancelX + btnW && my >= btnY && my < btnY + btnH;
        g.method_25294(cancelX, btnY, cancelX + btnW, btnY + btnH, this.fadeColor(ch ? 0x50FFFFFF : 0x30FFFFFF, this.openAnim));
        this.border(g, cancelX, btnY, btnW, btnH, border);
        String cl = class_2561.method_43471((String)"ankinbt.edit.cancel").getString();
        VersionCompat.get().drawString(g, this.field_22793, cl, cancelX + (btnW - this.field_22793.method_1727(cl)) / 2, btnY + 7, -7035976, false);
        int okX = this.px + 150 + 8;
        boolean oh = mx >= okX && mx < okX + btnW && my >= btnY && my < btnY + btnH;
        g.method_25294(okX, btnY, okX + btnW, btnY + btnH, this.fadeColor(oh ? -10262799 : -11581723, this.openAnim));
        this.border(g, okX, btnY, btnW, btnH, accent);
        String ol = class_2561.method_43471((String)"ankinbt.add.confirm").getString();
        VersionCompat.get().drawString(g, this.field_22793, ol, okX + (btnW - this.field_22793.method_1727(ol)) / 2, btnY + 7, -1906448, false);
    }

    private int fadeColor(int color, float progress) {
        int alpha = color >>> 24 & 0xFF;
        int faded = Math.max(0, Math.min(255, Math.round((float)alpha * progress)));
        return color & 0xFFFFFF | faded << 24;
    }

    private void border(class_332 g, int x, int y, int w, int h, int c) {
        g.method_25294(x, y, x + w, y + 1, c);
        g.method_25294(x, y + h - 1, x + w, y + h, c);
        g.method_25294(x, y, x + 1, y + h, c);
        g.method_25294(x + w - 1, y, x + w, y + h, c);
    }

    public boolean method_25402(double mx, double my, int btn) {
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
        return super.method_25402(mx, my, btn);
    }

    public boolean method_25404(int key, int scan, int mod) {
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
        return super.method_25404(key, scan, mod);
    }

    public boolean method_25400(char c, int mod) {
        if (c >= ' ') {
            this.keyInput = this.keyInput.substring(0, this.keyCursor) + c + this.keyInput.substring(this.keyCursor);
            ++this.keyCursor;
            this.error = null;
            return true;
        }
        return super.method_25400(c, mod);
    }

    private void confirm() {
        if (this.targetNode.isList()) {
            class_2520 tag = NbtHelper.createDefault(TYPE_IDS[this.selectedType]);
            this.parent.addTagToNode(this.targetNode, "", tag);
            this.goBack();
            return;
        }
        if (this.keyInput.isEmpty()) {
            this.error = class_2561.method_43471((String)"ankinbt.add.error.empty").getString();
            return;
        }
        for (NbtTreeNode child : this.targetNode.getChildren()) {
            if (!child.getKey().equals(this.keyInput)) continue;
            this.error = class_2561.method_43471((String)"ankinbt.add.error.exists").getString();
            return;
        }
        class_2520 tag = NbtHelper.createDefault(TYPE_IDS[this.selectedType]);
        this.parent.addTagToNode(this.targetNode, this.keyInput, tag);
        this.goBack();
    }

    private void goBack() {
        class_310.method_1551().method_1507((class_437)this.parent);
    }

    public boolean method_25421() {
        return false;
    }
}

