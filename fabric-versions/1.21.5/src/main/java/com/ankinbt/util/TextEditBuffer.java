/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package com.ankinbt.util;

import net.minecraft.class_310;

public class TextEditBuffer {
    private static final int KEY_BACKSPACE = 259;
    private static final int KEY_DELETE = 261;
    private static final int KEY_RIGHT = 262;
    private static final int KEY_LEFT = 263;
    private static final int KEY_HOME = 268;
    private static final int KEY_END = 269;
    private static final int KEY_A = 65;
    private static final int KEY_C = 67;
    private static final int KEY_V = 86;
    private static final int KEY_X = 88;
    private static final int MOD_SHIFT = 1;
    private static final int MOD_CTRL = 2;
    private String value;
    private int cursor;
    private int anchor;

    public TextEditBuffer(String value) {
        this.setValue(value);
        this.moveTo(this.value.length(), false);
    }

    public String value() {
        return this.value;
    }

    public int cursor() {
        return this.cursor;
    }

    public int anchor() {
        return this.anchor;
    }

    public void setValue(String value) {
        this.value = value == null ? "" : value;
        this.cursor = this.clamp(this.cursor);
        this.anchor = this.clamp(this.anchor);
    }

    public void moveTo(int nextCursor, boolean selecting) {
        int oldCursor = this.cursor;
        this.cursor = this.clamp(nextCursor);
        if (selecting) {
            if (!this.hasSelection()) {
                this.anchor = oldCursor;
            }
        } else {
            this.anchor = this.cursor;
        }
    }

    public boolean hasSelection() {
        return this.cursor != this.anchor;
    }

    public int selectionStart() {
        return Math.min(this.cursor, this.anchor);
    }

    public int selectionEnd() {
        return Math.max(this.cursor, this.anchor);
    }

    public String selectedText() {
        if (!this.hasSelection()) {
            return "";
        }
        return this.value.substring(this.selectionStart(), this.selectionEnd());
    }

    public void selectAll() {
        this.anchor = 0;
        this.cursor = this.value.length();
    }

    public void clearSelection() {
        this.anchor = this.cursor;
    }

    public boolean keyPressed(int key, int modifiers) {
        boolean shift;
        boolean ctrl = (modifiers & 2) != 0;
        boolean bl = shift = (modifiers & 1) != 0;
        if (ctrl && key == 65) {
            this.selectAll();
            return true;
        }
        if (ctrl && key == 67) {
            this.copySelection();
            return true;
        }
        if (ctrl && key == 88) {
            this.copySelection();
            this.deleteSelection();
            return true;
        }
        if (ctrl && key == 86) {
            this.pasteClipboard();
            return true;
        }
        if (key == 259) {
            if (!this.deleteSelection() && this.cursor > 0) {
                int next = ctrl ? this.previousWord(this.cursor) : this.cursor - 1;
                this.value = this.value.substring(0, next) + this.value.substring(this.cursor);
                this.moveTo(next, false);
            }
            return true;
        }
        if (key == 261) {
            if (!this.deleteSelection() && this.cursor < this.value.length()) {
                int next = ctrl ? this.nextWord(this.cursor) : this.cursor + 1;
                this.value = this.value.substring(0, this.cursor) + this.value.substring(next);
            }
            return true;
        }
        if (key == 263) {
            if (this.hasSelection() && !shift && !ctrl) {
                this.moveTo(this.selectionStart(), false);
            } else {
                this.moveTo(ctrl ? this.previousWord(this.cursor) : this.cursor - 1, shift);
            }
            return true;
        }
        if (key == 262) {
            if (this.hasSelection() && !shift && !ctrl) {
                this.moveTo(this.selectionEnd(), false);
            } else {
                this.moveTo(ctrl ? this.nextWord(this.cursor) : this.cursor + 1, shift);
            }
            return true;
        }
        if (key == 268) {
            this.moveTo(0, shift);
            return true;
        }
        if (key == 269) {
            this.moveTo(this.value.length(), shift);
            return true;
        }
        return false;
    }

    public boolean charTyped(char c) {
        if (c < ' ') {
            return false;
        }
        this.insert(Character.toString(c));
        return true;
    }

    public void insert(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        this.replaceSelection(text);
    }

    public void wrapSelectionOrInsert(String prefix, String suffix) {
        String safeSuffix;
        String safePrefix = prefix == null ? "" : prefix;
        String string = safeSuffix = suffix == null ? "" : suffix;
        if (this.hasSelection()) {
            int start = this.selectionStart();
            String selected = this.selectedText();
            this.value = this.value.substring(0, start) + safePrefix + selected + safeSuffix + this.value.substring(this.selectionEnd());
            this.moveTo(start + safePrefix.length() + selected.length(), false);
        } else {
            this.insert(safePrefix);
        }
    }

    public void replaceSelection(String text) {
        String replacement = text == null ? "" : text;
        int start = this.selectionStart();
        int end = this.selectionEnd();
        this.value = this.value.substring(0, start) + replacement + this.value.substring(end);
        this.moveTo(start + replacement.length(), false);
    }

    public boolean deleteSelection() {
        if (!this.hasSelection()) {
            return false;
        }
        this.replaceSelection("");
        return true;
    }

    private void copySelection() {
        if (!this.hasSelection()) {
            return;
        }
        class_310.method_1551().field_1774.method_1455(this.selectedText());
    }

    private void pasteClipboard() {
        String clip = class_310.method_1551().field_1774.method_1460();
        if (clip != null && !clip.isEmpty()) {
            this.insert(clip);
        }
    }

    private int previousWord(int from) {
        int i;
        for (i = this.clamp(from); i > 0 && Character.isWhitespace(this.value.charAt(i - 1)); --i) {
        }
        while (i > 0 && this.isWordChar(this.value.charAt(i - 1))) {
            --i;
        }
        if (i == from && i > 0) {
            --i;
        }
        return i;
    }

    private int nextWord(int from) {
        int i;
        for (i = this.clamp(from); i < this.value.length() && Character.isWhitespace(this.value.charAt(i)); ++i) {
        }
        while (i < this.value.length() && this.isWordChar(this.value.charAt(i))) {
            ++i;
        }
        if (i == from && i < this.value.length()) {
            ++i;
        }
        return i;
    }

    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '.' || c == '-';
    }

    private int clamp(int n) {
        return Math.max(0, Math.min(n, this.value == null ? 0 : this.value.length()));
    }
}

