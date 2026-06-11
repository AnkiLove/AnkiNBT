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
import com.ankinbt.util.TextEditBuffer;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class ValueEditScreen
extends class_437 {
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
    private static final int SELECT_BG = 1715176182;
    private final NbtEditorScreen parent;
    private final NbtTreeNode node;
    private final TextEditBuffer input;
    private String error = null;
    private int px;
    private int py;
    private float openAnim = 0.0f;
    private boolean draggingSelection;

    public ValueEditScreen(NbtEditorScreen parent, NbtTreeNode node) {
        super((class_2561)class_2561.method_43471((String)"ankinbt.edit.title"));
        this.parent = parent;
        this.node = node;
        this.input = new TextEditBuffer(VersionCompat.get().getTagAsString(node.getTag()));
    }

    protected void method_25426() {
        super.method_25426();
        this.px = (this.field_22789 - 320) / 2;
        this.py = (this.field_22790 - 150) / 2;
    }

    public void method_25394(class_332 g, int mx, int my, float pt) {
        int selEnd;
        int selStart;
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        int scrim = this.fadeColor(Integer.MIN_VALUE, this.openAnim);
        int bg = this.fadeColor(-402126832, this.openAnim);
        int border = this.fadeColor(-14540234, this.openAnim);
        int accent = this.fadeColor(-10262799, this.openAnim);
        int inputBg = this.fadeColor(-15592930, this.openAnim);
        int err = this.fadeColor(-1096636, this.openAnim);
        g.method_25294(0, 0, this.field_22789, this.field_22790, scrim);
        g.method_25294(this.px, this.py, this.px + 320, this.py + 150, bg);
        this.border(g, this.px, this.py, 320, 150, border);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43469((String)"ankinbt.edit.editing", (Object[])new Object[]{this.node.getKey()}), this.px + 12, this.py + 10, -1906448, false);
        String type = this.node.getTypeName();
        VersionCompat.get().drawString(g, this.field_22793, type, this.px + 320 - this.field_22793.method_1727(type) - 12, this.py + 10, NbtHelper.getTagColor(this.node.getTag()), false);
        g.method_25294(this.px + 1, this.py + 26, this.px + 320 - 1, this.py + 27, border);
        int ix = this.px + 12;
        int iy = this.py + 36;
        int iw = 296;
        int ih = 24;
        g.method_25294(ix, iy, ix + iw, iy + ih, inputBg);
        this.border(g, ix, iy, iw, ih, accent);
        int maxTextW = iw - 12;
        int viewStart = this.textViewStart(this.input.value(), this.input.cursor(), maxTextW);
        String disp = this.visibleText(this.input.value(), viewStart, maxTextW);
        if (this.input.hasSelection() && (selStart = Math.max(this.input.selectionStart(), viewStart)) < (selEnd = Math.min(this.input.selectionEnd(), viewStart + disp.length()))) {
            int sx = ix + 4 + this.field_22793.method_1727(this.input.value().substring(viewStart, selStart));
            int ex = ix + 4 + this.field_22793.method_1727(this.input.value().substring(viewStart, selEnd));
            g.method_25294(sx, iy + 4, ex, iy + ih - 4, 1715176182);
        }
        VersionCompat.get().drawString(g, this.field_22793, disp, ix + 4, iy + 8, -1906448, false);
        if (!this.input.hasSelection() && System.currentTimeMillis() % 1000L < 500L) {
            String before = this.input.value().substring(viewStart, Math.max(viewStart, Math.min(this.input.cursor(), this.input.value().length())));
            int cx = ix + 4 + this.field_22793.method_1727(before);
            g.method_25294(cx, iy + 4, cx + 1, iy + ih - 4, -1906448);
        }
        if (this.error != null) {
            VersionCompat.get().drawString(g, this.field_22793, this.error, ix, iy + ih + 4, err, false);
        }
        int by = this.py + 150 - 36;
        int bw = 80;
        int bh = 22;
        int cancelX = this.px + 160 - bw - 8;
        boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
        g.method_25294(cancelX, by, cancelX + bw, by + bh, this.fadeColor(ch ? 0x50FFFFFF : 0x30FFFFFF, this.openAnim));
        this.border(g, cancelX, by, bw, bh, border);
        String cl = class_2561.method_43471((String)"ankinbt.edit.cancel").getString();
        VersionCompat.get().drawString(g, this.field_22793, cl, cancelX + (bw - this.field_22793.method_1727(cl)) / 2, by + 7, -7035976, false);
        int okX = this.px + 160 + 8;
        boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh;
        g.method_25294(okX, by, okX + bw, by + bh, this.fadeColor(oh ? -10262799 : -11581723, this.openAnim));
        this.border(g, okX, by, bw, bh, accent);
        String ol = class_2561.method_43471((String)"ankinbt.edit.apply").getString();
        VersionCompat.get().drawString(g, this.field_22793, ol, okX + (bw - this.field_22793.method_1727(ol)) / 2, by + 7, -1906448, false);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.edit.hint"), this.px + 12, this.py + 150 - 12, -10193781, false);
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

    private int textViewStart(String value, int cursor, int maxWidth) {
        int start;
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int clampedCursor = Math.max(0, Math.min(cursor, value.length()));
        for (start = 0; start < clampedCursor && this.field_22793.method_1727(value.substring(start, clampedCursor)) > maxWidth; ++start) {
        }
        return start;
    }

    private String visibleText(String value, int start, int maxWidth) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int safeStart = Math.max(0, Math.min(start, value.length()));
        String text = value.substring(safeStart);
        return this.field_22793.method_1727(text) <= maxWidth ? text : this.field_22793.method_27523(text, maxWidth);
    }

    private int cursorFromMouse(String value, int start, int relX, int maxWidth) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int safeStart = Math.max(0, Math.min(start, value.length()));
        String shown = this.visibleText(value, safeStart, maxWidth);
        int best = safeStart;
        for (int i = 0; i <= shown.length(); ++i) {
            int charW;
            String before = shown.substring(0, i);
            int n = charW = i < shown.length() ? this.field_22793.method_1727(String.valueOf(shown.charAt(i))) : 8;
            if (this.field_22793.method_1727(before) + Math.max(1, charW / 2) >= relX) {
                return safeStart + i;
            }
            best = safeStart + i;
        }
        return Math.max(0, Math.min(best, value.length()));
    }

    public boolean method_25402(double mx, double my, int btn) {
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
        if (mx >= (double)ix && mx < (double)(ix + iw) && my >= (double)iy && my < (double)(iy + ih)) {
            int viewStart = this.textViewStart(this.input.value(), this.input.cursor(), iw - 12);
            this.input.moveTo(this.cursorFromMouse(this.input.value(), viewStart, (int)mx - ix - 4, iw - 12), btn == 0 && class_437.method_25442());
            this.draggingSelection = btn == 0;
            return true;
        }
        this.draggingSelection = false;
        return super.method_25402(mx, my, btn);
    }

    public boolean method_25403(double mx, double my, int button, double dragX, double dragY) {
        if (this.draggingSelection) {
            int ix = this.px + 12;
            int iw = 296;
            int viewStart = this.textViewStart(this.input.value(), this.input.cursor(), iw - 12);
            this.input.moveTo(this.cursorFromMouse(this.input.value(), viewStart, (int)mx - ix - 4, iw - 12), true);
            return true;
        }
        return super.method_25403(mx, my, button, dragX, dragY);
    }

    public boolean method_25406(double mx, double my, int button) {
        this.draggingSelection = false;
        return super.method_25406(mx, my, button);
    }

    public boolean method_25404(int key, int scan, int mod) {
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
            if (!before.equals(this.input.value())) {
                this.error = null;
            }
            return true;
        }
        return super.method_25404(key, scan, mod);
    }

    public boolean method_25400(char c, int mod) {
        if (this.input.charTyped(c)) {
            this.error = null;
            return true;
        }
        return super.method_25400(c, mod);
    }

    private void apply() {
        class_2520 newTag = NbtHelper.parseValue(this.input.value(), this.node.getTag());
        if (newTag == null) {
            this.error = class_2561.method_43469((String)"ankinbt.edit.error", (Object[])new Object[]{this.node.getTypeName()}).getString();
            return;
        }
        this.node.setTag(newTag);
        this.node.applyToParent();
        this.parent.onNodeEdited();
        this.goBack();
    }

    private void goBack() {
        class_310.method_1551().method_1507((class_437)this.parent);
    }

    public boolean method_25421() {
        return false;
    }
}
