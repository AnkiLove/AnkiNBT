package com.ankinbt.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class MultiLineTextEditBuffer {
    private static final int KEY_BACKSPACE = 259;
    private static final int KEY_DELETE = 261;
    private static final int KEY_RIGHT = 262;
    private static final int KEY_LEFT = 263;
    private static final int KEY_DOWN = 264;
    private static final int KEY_UP = 265;
    private static final int KEY_HOME = 268;
    private static final int KEY_END = 269;
    private static final int KEY_A = 65;
    private static final int KEY_C = 67;
    private static final int KEY_V = 86;
    private static final int KEY_X = 88;
    private static final int MOD_SHIFT = 1;
    private static final int MOD_CTRL = 2;

    private final List<String> lines = new ArrayList<>();
    private int cursorLine;
    private int cursorCol;
    private int anchorLine;
    private int anchorCol;
    private int preferredCol = -1;

    public MultiLineTextEditBuffer(List<String> initialLines) {
        setLines(initialLines);
        moveTo(this.lines.size() - 1, this.lines.get(this.lines.size() - 1).length(), false);
    }

    public void setLines(List<String> initialLines) {
        this.lines.clear();
        if (initialLines != null) {
            for (String line : initialLines) {
                this.lines.add(line == null ? "" : line);
            }
        }
        if (this.lines.isEmpty()) {
            this.lines.add("");
        }
        moveTo(Math.min(this.cursorLine, this.lines.size() - 1), this.cursorCol, false);
    }

    public List<String> lines() {
        return this.lines;
    }

    public int cursorLine() {
        return this.cursorLine;
    }

    public int cursorCol() {
        return this.cursorCol;
    }

    public void moveTo(int line, int col, boolean selecting) {
        int oldLine = this.cursorLine;
        int oldCol = this.cursorCol;
        this.cursorLine = clampLine(line);
        this.cursorCol = clampCol(this.cursorLine, col);
        if (selecting) {
            if (!hasSelection()) {
                this.anchorLine = oldLine;
                this.anchorCol = oldCol;
            }
        } else {
            this.anchorLine = this.cursorLine;
            this.anchorCol = this.cursorCol;
        }
        this.preferredCol = this.cursorCol;
    }

    public boolean hasSelection() {
        return this.cursorLine != this.anchorLine || this.cursorCol != this.anchorCol;
    }

    public boolean lineHasSelection(int line) {
        if (!hasSelection()) {
            return false;
        }
        Position start = selectionStart();
        Position end = selectionEnd();
        return line >= start.line && line <= end.line && selectionStartCol(line) < selectionEndCol(line);
    }

    public int selectionStartCol(int line) {
        Position start = selectionStart();
        Position end = selectionEnd();
        if (line < start.line || line > end.line) {
            return 0;
        }
        return line == start.line ? start.col : 0;
    }

    public int selectionEndCol(int line) {
        Position start = selectionStart();
        Position end = selectionEnd();
        if (line < start.line || line > end.line) {
            return 0;
        }
        return line == end.line ? end.col : this.lines.get(line).length();
    }

    public void selectAll() {
        this.anchorLine = 0;
        this.anchorCol = 0;
        this.cursorLine = this.lines.size() - 1;
        this.cursorCol = this.lines.get(this.cursorLine).length();
        this.preferredCol = this.cursorCol;
    }

    public boolean keyPressed(int key, int modifiers) {
        boolean ctrl = (modifiers & MOD_CTRL) != 0;
        boolean shift = (modifiers & MOD_SHIFT) != 0;
        if (ctrl && key == KEY_A) {
            selectAll();
            return true;
        }
        if (ctrl && key == KEY_C) {
            copySelection();
            return true;
        }
        if (ctrl && key == KEY_X) {
            copySelection();
            deleteSelection();
            return true;
        }
        if (ctrl && key == KEY_V) {
            pasteClipboard();
            return true;
        }
        if (key == KEY_BACKSPACE) {
            backspace(ctrl);
            return true;
        }
        if (key == KEY_DELETE) {
            delete(ctrl);
            return true;
        }
        if (key == KEY_LEFT) {
            moveHorizontal(-1, ctrl, shift);
            return true;
        }
        if (key == KEY_RIGHT) {
            moveHorizontal(1, ctrl, shift);
            return true;
        }
        if (key == KEY_UP) {
            moveVertical(-1, shift);
            return true;
        }
        if (key == KEY_DOWN) {
            moveVertical(1, shift);
            return true;
        }
        if (key == KEY_HOME) {
            moveTo(this.cursorLine, 0, shift);
            return true;
        }
        if (key == KEY_END) {
            moveTo(this.cursorLine, this.lines.get(this.cursorLine).length(), shift);
            return true;
        }
        return false;
    }

    public boolean charTyped(char c) {
        return charTyped((int) c);
    }

    public boolean charTyped(int codePoint) {
        if (!Character.isValidCodePoint(codePoint) || Character.isISOControl(codePoint)) {
            return false;
        }
        insert(new String(Character.toChars(codePoint)));
        return true;
    }

    public void insertNewLine() {
        deleteSelection();
        String line = this.lines.get(this.cursorLine);
        int col = clampCol(this.cursorLine, this.cursorCol);
        String before = line.substring(0, col);
        String after = line.substring(col);
        this.lines.set(this.cursorLine, before);
        this.lines.add(this.cursorLine + 1, after);
        moveTo(this.cursorLine + 1, 0, false);
    }

    public void insert(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        deleteSelection();
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        String[] parts = normalized.split("\n", -1);
        String line = this.lines.get(this.cursorLine);
        int col = clampCol(this.cursorLine, this.cursorCol);
        String before = line.substring(0, col);
        String after = line.substring(col);
        if (parts.length == 1) {
            this.lines.set(this.cursorLine, before + parts[0] + after);
            moveTo(this.cursorLine, col + parts[0].length(), false);
            return;
        }
        this.lines.set(this.cursorLine, before + parts[0]);
        int insertLine = this.cursorLine + 1;
        for (int i = 1; i < parts.length - 1; i++) {
            this.lines.add(insertLine++, parts[i]);
        }
        this.lines.add(insertLine, parts[parts.length - 1] + after);
        moveTo(insertLine, parts[parts.length - 1].length(), false);
    }

    public void wrapSelectionOrInsert(String prefix, String suffix) {
        String safePrefix = prefix == null ? "" : prefix;
        String safeSuffix = suffix == null ? "" : suffix;
        if (!hasSelection()) {
            insert(safePrefix);
            return;
        }
        String selected = selectedText();
        replaceSelection(safePrefix + selected + safeSuffix);
        moveTo(this.cursorLine, this.cursorCol - safeSuffix.length(), false);
    }

    public boolean deleteSelection() {
        if (!hasSelection()) {
            return false;
        }
        replaceSelection("");
        return true;
    }

    public String selectedText() {
        if (!hasSelection()) {
            return "";
        }
        Position start = selectionStart();
        Position end = selectionEnd();
        if (start.line == end.line) {
            return this.lines.get(start.line).substring(start.col, end.col);
        }
        StringBuilder out = new StringBuilder();
        out.append(this.lines.get(start.line).substring(start.col)).append('\n');
        for (int line = start.line + 1; line < end.line; line++) {
            out.append(this.lines.get(line)).append('\n');
        }
        out.append(this.lines.get(end.line), 0, end.col);
        return out.toString();
    }

    private void replaceSelection(String replacement) {
        Position start = selectionStart();
        Position end = selectionEnd();
        moveTo(start.line, start.col, false);
        if (start.line == end.line) {
            String line = this.lines.get(start.line);
            this.lines.set(start.line, line.substring(0, start.col) + line.substring(end.col));
        } else {
            String merged = this.lines.get(start.line).substring(0, start.col) + this.lines.get(end.line).substring(end.col);
            for (int line = end.line; line > start.line; line--) {
                this.lines.remove(line);
            }
            this.lines.set(start.line, merged);
        }
        insert(replacement);
    }

    private void backspace(boolean ctrl) {
        if (deleteSelection()) {
            return;
        }
        if (ctrl) {
            moveHorizontalTo(previousWord(), true);
            deleteSelection();
            return;
        }
        if (this.cursorCol > 0) {
            String line = this.lines.get(this.cursorLine);
            this.lines.set(this.cursorLine, line.substring(0, this.cursorCol - 1) + line.substring(this.cursorCol));
            moveTo(this.cursorLine, this.cursorCol - 1, false);
        } else if (this.cursorLine > 0) {
            String current = this.lines.remove(this.cursorLine);
            int prevLen = this.lines.get(this.cursorLine - 1).length();
            this.lines.set(this.cursorLine - 1, this.lines.get(this.cursorLine - 1) + current);
            moveTo(this.cursorLine - 1, prevLen, false);
        }
    }

    private void delete(boolean ctrl) {
        if (deleteSelection()) {
            return;
        }
        if (ctrl) {
            moveHorizontalTo(nextWord(), true);
            deleteSelection();
            return;
        }
        String line = this.lines.get(this.cursorLine);
        if (this.cursorCol < line.length()) {
            this.lines.set(this.cursorLine, line.substring(0, this.cursorCol) + line.substring(this.cursorCol + 1));
        } else if (this.cursorLine < this.lines.size() - 1) {
            this.lines.set(this.cursorLine, line + this.lines.remove(this.cursorLine + 1));
        }
    }

    private void moveHorizontal(int delta, boolean ctrl, boolean selecting) {
        if (hasSelection() && !selecting && !ctrl) {
            Position edge = delta < 0 ? selectionStart() : selectionEnd();
            moveTo(edge.line, edge.col, false);
            return;
        }
        Position next = ctrl ? (delta < 0 ? previousWord() : nextWord()) : adjacent(delta);
        moveTo(next.line, next.col, selecting);
    }

    private void moveHorizontalTo(Position next, boolean selecting) {
        moveTo(next.line, next.col, selecting);
    }

    private void moveVertical(int delta, boolean selecting) {
        if (this.preferredCol < 0) {
            this.preferredCol = this.cursorCol;
        }
        int nextLine = clampLine(this.cursorLine + delta);
        moveTo(nextLine, this.preferredCol, selecting);
    }

    private Position adjacent(int delta) {
        if (delta < 0) {
            if (this.cursorCol > 0) {
                return new Position(this.cursorLine, this.cursorCol - 1);
            }
            if (this.cursorLine > 0) {
                int line = this.cursorLine - 1;
                return new Position(line, this.lines.get(line).length());
            }
        } else {
            String line = this.lines.get(this.cursorLine);
            if (this.cursorCol < line.length()) {
                return new Position(this.cursorLine, this.cursorCol + 1);
            }
            if (this.cursorLine < this.lines.size() - 1) {
                return new Position(this.cursorLine + 1, 0);
            }
        }
        return new Position(this.cursorLine, this.cursorCol);
    }

    private Position previousWord() {
        int line = this.cursorLine;
        int col = this.cursorCol;
        while (line > 0 && col == 0) {
            line--;
            col = this.lines.get(line).length();
        }
        String text = this.lines.get(line);
        while (col > 0 && Character.isWhitespace(text.charAt(col - 1))) {
            col--;
        }
        while (col > 0 && isWordChar(text.charAt(col - 1))) {
            col--;
        }
        if (col == this.cursorCol && line == this.cursorLine && col > 0) {
            col--;
        }
        return new Position(line, col);
    }

    private Position nextWord() {
        int line = this.cursorLine;
        int col = this.cursorCol;
        while (line < this.lines.size() - 1 && col >= this.lines.get(line).length()) {
            line++;
            col = 0;
        }
        String text = this.lines.get(line);
        while (col < text.length() && Character.isWhitespace(text.charAt(col))) {
            col++;
        }
        while (col < text.length() && isWordChar(text.charAt(col))) {
            col++;
        }
        if (col == this.cursorCol && line == this.cursorLine && col < text.length()) {
            col++;
        }
        return new Position(line, col);
    }

    private Position selectionStart() {
        if (this.anchorLine < this.cursorLine || this.anchorLine == this.cursorLine && this.anchorCol <= this.cursorCol) {
            return new Position(this.anchorLine, this.anchorCol);
        }
        return new Position(this.cursorLine, this.cursorCol);
    }

    private Position selectionEnd() {
        if (this.anchorLine < this.cursorLine || this.anchorLine == this.cursorLine && this.anchorCol <= this.cursorCol) {
            return new Position(this.cursorLine, this.cursorCol);
        }
        return new Position(this.anchorLine, this.anchorCol);
    }

    private void copySelection() {
        if (hasSelection()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(selectedText());
        }
    }

    private void pasteClipboard() {
        String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
        if (clip != null && !clip.isEmpty()) {
            insert(clip);
        }
    }

    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '.' || c == '-';
    }

    private int clampLine(int line) {
        return Math.max(0, Math.min(line, this.lines.size() - 1));
    }

    private int clampCol(int line, int col) {
        return Math.max(0, Math.min(col, this.lines.get(clampLine(line)).length()));
    }

    private record Position(int line, int col) {
    }
}
