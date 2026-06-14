/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.item.ItemStack
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.gui.AddTagScreen;
import com.ankinbt.gui.SimpleEditorScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.gui.ValueEditScreen;
import com.ankinbt.nbt.NbtFileIO;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtTreeNode;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

public class NbtEditorScreen
extends Screen {
    private static final int HEADER_H = 32;
    private static final int ROW_H = 18;
    private static final int INDENT = 14;
    private static final int SIDEBAR_W = 140;
    private static final int SCROLLBAR_W = 6;
    private static final int FOOTER_H = 20;
    private static final int MARGIN = 16;
    private static final int BG = -670562288;
    private static final int SIDEBAR_BG = -670299112;
    private static final int HEADER_BG = -670035936;
    private static final int BORDER = -14540234;
    private static final int HOVER = 0x30FFFFFF;
    private static final int C1 = -1906448;
    private static final int C2 = -7035976;
    private static final int C3 = -10193781;
    private static final int SB_TRACK = 0x30FFFFFF;
    private static final int SB_THUMB = 0x70FFFFFF;
    private static final int BTN_BG = 0x30FFFFFF;
    private static final int BTN_HOVER = 0x50FFFFFF;
    private static final int SUCCESS = -14498466;
    private static final int ERROR_C = -1096636;
    private final ItemStack originalStack;
    private final int inventorySlot;
    private CompoundTag fullItemTag;
    private NbtTreeNode rootNode;
    private List<NbtTreeNode> visibleNodes = new ArrayList<NbtTreeNode>();
    private int scrollOff = 0;
    private int maxRows;
    private int selIdx = -1;
    private int hoverIdx = -1;
    private int lastClickIdx = -1;
    private long lastClickTime = 0L;
    private int px;
    private int py;
    private int pw;
    private int ph;
    private int sideX;
    private int sideY;
    private int sideW;
    private int sideH;
    private int treeX;
    private int treeY;
    private int treeW;
    private int treeH;
    private EditBox searchBox;
    private boolean searching = false;
    private final List<Btn> buttons = new ArrayList<Btn>();
    private String statusMsg = null;
    private long statusTime = 0L;
    private int statusColor = -10193781;
    private boolean dirty = false;
    private boolean confirmClose = false;
    private boolean nativeDialogOpen = false;
    private long lastNativeDialogAt = 0L;
    private float openAnim = 0.0f;

    public NbtEditorScreen(ItemStack stack) {
        this(stack, -1);
    }

    public NbtEditorScreen(ItemStack stack, int inventorySlot) {
        super((Component)Component.translatable((String)"ankinbt.title"));
        this.originalStack = stack;
        this.inventorySlot = inventorySlot;
        Optional<CompoundTag> opt = NbtHelper.serializeItemStack(stack);
        this.fullItemTag = opt.orElseGet(() -> {
            CompoundTag fallback = new CompoundTag();
            fallback.putString("id", this.resolveItemId(stack));
            fallback.putInt("count", stack.getCount());
            return fallback;
        });
        this.rebuildTree();
    }

    private void rebuildTree() {
        this.rootNode = new NbtTreeNode("", (Tag)this.fullItemTag, null, AnkiConfig.isTreeExpandedByDefault());
        this.rootNode.setExpanded(true);
        this.refreshVisible();
    }

    private void refreshVisible() {
        this.visibleNodes.clear();
        if (this.rootNode != null) {
            this.rootNode.collectVisible(this.visibleNodes);
        }
        String search = this.searchValue();
        if (this.searching && !search.isEmpty()) {
            String q = search.toLowerCase();
            this.visibleNodes = this.visibleNodes.stream().filter(n -> n.getKey().toLowerCase().contains(q) || n.getDisplayValue().toLowerCase().contains(q) || n.getTypeName().toLowerCase().contains(q)).collect(Collectors.toList());
        }
        this.clampScroll();
    }

    protected void init() {
        super.init();
        this.pw = Math.min(this.width - 32, 620);
        this.ph = Math.min(this.height - 32, 420);
        this.px = (this.width - this.pw) / 2;
        this.py = (this.height - this.ph) / 2;
        this.sideX = this.px + 1;
        this.sideY = this.py + 32 + 1;
        this.sideW = 140;
        this.sideH = this.ph - 32 - 20 - 2;
        this.treeX = this.px + 140 + 2;
        this.treeY = this.py + 32 + 1;
        this.treeW = this.pw - 140 - 6 - 6;
        this.treeH = this.ph - 32 - 20 - 2;
        this.maxRows = this.treeH / 18;
        this.initSearchBox();
        this.buildButtons();
    }

    private void initSearchBox() {
        String value = this.searchValue();
        this.searchBox = new EditBox(this.font, this.treeX + 2, this.treeY + 1, Math.max(1, this.treeW - 4), 16, (Component)Component.translatable((String)"ankinbt.search.hint"));
        this.searchBox.setMaxLength(256);
        this.searchBox.setValue(value);
        this.searchBox.setResponder(v -> this.refreshVisible());
        this.searchBox.setFocused(this.searching);
    }

    private String searchValue() {
        return this.searchBox == null ? "" : this.searchBox.getValue();
    }

    private void setSearching(boolean value) {
        this.searching = value;
        if (this.searchBox != null) {
            if (!this.searching) {
                this.searchBox.setValue("");
            }
            this.searchBox.setFocused(this.searching);
        }
        this.refreshVisible();
    }

    private void layoutSearchBox() {
        if (this.searchBox == null) {
            this.initSearchBox();
        }
        this.searchBox.setX(this.treeX + 2);
        this.searchBox.setY(this.treeY + 1);
        this.searchBox.setWidth(Math.max(1, this.treeW - 4));
    }

    private void buildButtons() {
        this.buttons.clear();
        int bw = 22;
        int gap = 3;
        int by = this.py + 6;
        int bx = this.px + this.pw - 16 - 2;
        this.buttons.add(new Btn(bx -= bw, by, bw, bw, "X", (Component)Component.translatable((String)"ankinbt.btn.close"), this::tryClose));
        this.buttons.add(new Btn(bx -= bw + gap, by, bw, bw, "-", (Component)Component.translatable((String)"ankinbt.btn.collapse"), () -> {
            this.collapseAll(this.rootNode);
            this.rootNode.setExpanded(true);
            this.refreshVisible();
        }));
        this.buttons.add(new Btn(bx -= bw + gap, by, bw, bw, "+", (Component)Component.translatable((String)"ankinbt.btn.expand"), () -> {
            this.expandAll(this.rootNode);
            this.refreshVisible();
        }));
        this.buttons.add(new Btn(bx -= bw + gap, by, bw, bw, "S", (Component)Component.translatable((String)"ankinbt.btn.search"), () -> {
            this.setSearching(!this.searching);
        }));
        this.buttons.add(new Btn(bx -= bw + gap, by, bw, bw, "N", (Component)Component.translatable((String)"ankinbt.btn.add"), this::addTag));
        int saveW = 40;
        this.buttons.add(new Btn(bx -= saveW + gap + 4, by, saveW, bw, Component.translatable((String)"ankinbt.btn.save").getString(), (Component)Component.translatable((String)"ankinbt.btn.save.tip"), this::saveToItem));
        int modeW = 50;
        this.buttons.add(new Btn(bx -= modeW + gap + 4, by, modeW, bw, Component.translatable((String)"ankinbt.btn.simple").getString(), (Component)Component.translatable((String)"ankinbt.btn.switch_simple"), this::switchToSimple));
        int expW = 30;
        this.buttons.add(new Btn(bx -= expW + gap, by, expW, bw, "Ex", (Component)Component.translatable((String)"ankinbt.simple.export_nbt"), this::exportNbt));
        int impW = 30;
        this.buttons.add(new Btn(bx -= impW + gap, by, impW, bw, "Im", (Component)Component.translatable((String)"ankinbt.simple.import_nbt"), this::importNbt));
    }

    public void render(GuiGraphics g, int mx, int my, float pt) {
        float cfgSpeed = AnkiConfig.getUiAnimationSpeed();
        float speed = Math.max(0.06f, Math.min(0.14f, cfgSpeed));
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        int scrim = UiTheme.scrim(AnkiConfig.getUiOpacity(), this.openAnim);
        int panel = this.fadeColor(-670562288, this.openAnim);
        int header = this.fadeColor(-670035936, this.openAnim);
        int border = this.fadeColor(-14540234, this.openAnim);
        int sidebar = this.fadeColor(-670299112, this.openAnim);
        int hover = this.fadeColor(0x30FFFFFF, this.openAnim);
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        int selected = this.fadeColor(UiTheme.withAlpha(accent & 0xFFFFFF, 40), this.openAnim);
        int accentFade = this.fadeColor(accent, this.openAnim);
        int sbTrack = this.fadeColor(0x30FFFFFF, this.openAnim);
        int sbThumb = this.fadeColor(0x70FFFFFF, this.openAnim);
        g.fill(0, 0, this.width, this.height, scrim);
        g.fill(this.px, this.py, this.px + this.pw, this.py + this.ph, panel);
        this.drawBorder(g, this.px, this.py, this.pw, this.ph, border);
        g.fill(this.px + 1, this.py + 1, this.px + this.pw - 1, this.py + 32, header);
        g.fill(this.px + 1, this.py + 32, this.px + this.pw - 1, this.py + 32 + 1, border);
        g.drawString(this.font, "AnkiNBT", this.px + 16, this.py + 11, 0xFFE2E8F0, false);
        g.drawString(this.font, "高级模式", this.px + 64, this.py + 11, 0xFFFF2D7A, false);
        if (this.dirty) {
            g.drawString(this.font, "*", this.px + 116, this.py + 12, -1096636, false);
        }
        for (Btn b : this.buttons) {
            b.render(g, this.font, mx, my);
        }
        this.renderSidebar(g, sidebar, border);
        g.fill(this.px + 140 + 1, this.py + 32 + 1, this.px + 140 + 2, this.py + this.ph - 20, border);
        int atY = this.treeY;
        int atH = this.treeH;
        if (this.searching) {
            g.fill(this.treeX, this.treeY, this.treeX + this.treeW, this.treeY + 18, 0x40000000);
            this.drawBorder(g, this.treeX, this.treeY, this.treeW, 18, accentFade);
            this.layoutSearchBox();
            this.searchBox.setFocused(true);
            this.searchBox.render(g, mx, my, pt);
            atY += 20;
            this.maxRows = (atH -= 20) / 18;
        } else {
            this.maxRows = this.treeH / 18;
        }
        this.hoverIdx = -1;
        int end = Math.min(this.scrollOff + this.maxRows, this.visibleNodes.size());
        for (int i = this.scrollOff; i < end; ++i) {
            Object val;
            boolean hovered;
            int ry = atY + (i - this.scrollOff) * 18;
            NbtTreeNode node = this.visibleNodes.get(i);
            boolean bl = hovered = mx >= this.treeX && mx < this.treeX + this.treeW && my >= ry && my < ry + 18;
            if (hovered) {
                this.hoverIdx = i;
                g.fill(this.treeX, ry, this.treeX + this.treeW, ry + 18, hover);
            }
            if (i == this.selIdx) {
                g.fill(this.treeX, ry, this.treeX + this.treeW, ry + 18, selected);
                g.fill(this.treeX, ry, this.treeX + 2, ry + 18, accentFade);
            }
            int indent = node.getDepth() * 14;
            int tx = this.treeX + 6 + indent;
            if (!node.isLeaf()) {
                g.drawString(this.font, node.isExpanded() ? "v" : ">", tx, ry + 5, -10193781, false);
                tx += 10;
            }
            int tc = NbtHelper.getTagColor(node.getTag());
            String badge = node.getTypeName();
            if (badge.length() > 3) {
                badge = badge.substring(0, 3);
            }
            g.drawString(this.font, badge, tx, ry + 5, tc, false);
            tx += this.font.width(badge) + 4;
            String key = node.getKey();
            if (!key.isEmpty()) {
                g.drawString(this.font, key, tx, ry + 5, -1906448, false);
                tx += this.font.width(key) + 6;
            }
            if (((String)(val = node.getDisplayValue())).length() > 36) {
                val = ((String)val).substring(0, 33) + "...";
            }
            g.drawString(this.font, (String)val, tx, ry + 5, -7035976, false);
        }
        if (this.visibleNodes.size() > this.maxRows) {
            int sbx = this.px + this.pw - 6 - 3;
            g.fill(sbx, atY, sbx + 6, atY + atH, sbTrack);
            float ratio = (float)this.maxRows / (float)this.visibleNodes.size();
            int thumbH = Math.max(16, (int)((float)atH * ratio));
            float sr = (float)this.scrollOff / (float)Math.max(1, this.visibleNodes.size() - this.maxRows);
            int thumbY = atY + (int)((float)(atH - thumbH) * sr);
            g.fill(sbx, thumbY, sbx + 6, thumbY + thumbH, sbThumb);
        }
        g.fill(this.px + 1, this.py + this.ph - 20, this.px + this.pw - 1, this.py + this.ph - 20 + 1, border);
        this.renderFooter(g);
        if (this.confirmClose) {
            this.renderConfirmClose(g, mx, my);
        }
    }

    private void renderConfirmClose(GuiGraphics g, int mx, int my) {
        g.fill(0, 0, this.width, this.height, 0x60000000);
        int dw = 260;
        int dh = 110;
        int dx = (this.width - dw) / 2;
        int dy = (this.height - dh) / 2;
        g.fill(dx, dy, dx + dw, dy + dh, -267909104);
        this.drawBorder(g, dx, dy, dw, dh, -1096636);
        String title = Component.translatable((String)"ankinbt.confirm.title").getString();
        g.drawString(this.font, title, dx + 10, dy + 10, -1906448, false);
        g.fill(dx + 1, dy + 24, dx + dw - 1, dy + 25, -14540234);
        g.drawString(this.font, Component.translatable((String)"ankinbt.confirm.unsaved").getString(), dx + 10, dy + 32, -7035976, false);
        g.drawString(this.font, Component.translatable((String)"ankinbt.confirm.discard_hint").getString(), dx + 10, dy + 46, -10193781, false);
        int by = dy + dh - 32;
        int bw2 = 70;
        int bh2 = 22;
        int saveX = dx + 10;
        boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(saveX, by, saveX + bw2, by + bh2, sh ? accent : UiTheme.withAlpha(accent & 0xFFFFFF, 196));
        String saveLabel = Component.translatable((String)"ankinbt.confirm.save_close").getString();
        g.drawString(this.font, saveLabel, saveX + (bw2 - this.font.width(saveLabel)) / 2, by + 7, -1906448, false);
        int discardX = dx + dw / 2 - bw2 / 2;
        boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
        g.fill(discardX, by, discardX + bw2, by + bh2, dh2 ? -2131803068 : 1089422404);
        String discardLabel = Component.translatable((String)"ankinbt.confirm.discard").getString();
        g.drawString(this.font, discardLabel, discardX + (bw2 - this.font.width(discardLabel)) / 2, by + 7, -1906448, false);
        int cancelX = dx + dw - bw2 - 10;
        boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
        g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
        String cancelLabel = Component.translatable((String)"ankinbt.edit.cancel").getString();
        g.drawString(this.font, cancelLabel, cancelX + (bw2 - this.font.width(cancelLabel)) / 2, by + 7, -7035976, false);
    }

    private void renderSidebar(GuiGraphics g, int sidebarBg, int border) {
        Tag comp;
        g.fill(this.sideX, this.sideY, this.sideX + this.sideW, this.sideY + this.sideH, sidebarBg);
        int y = this.sideY + 8;
        int lx = this.sideX + 8;
        g.renderItem(this.originalStack, lx + (this.sideW - 32) / 2, y);
        y += 24;
        Object name = this.originalStack.getHoverName().getString();
        if (this.font.width((String)name) > this.sideW - 16) {
            name = this.font.plainSubstrByWidth((String)name, this.sideW - 22) + "...";
        }
        g.drawString(this.font, (String)name, lx, y, -1906448, false);
        g.fill(lx, y += 14, this.sideX + this.sideW - 8, y + 1, border);
        y += 6;
        if (this.fullItemTag.contains("id")) {
            this.sideInfo(g, lx, y, Component.translatable((String)"ankinbt.side.id").getString(), VersionCompat.get().compoundGetString(this.fullItemTag, "id"));
            y += 12;
        }
        if (this.fullItemTag.contains("count")) {
            this.sideInfo(g, lx, y, Component.translatable((String)"ankinbt.side.count").getString(), String.valueOf(VersionCompat.get().compoundGetInt(this.fullItemTag, "count")));
            y += 12;
        }
        if (this.fullItemTag.contains("components") && (comp = this.fullItemTag.get("components")) instanceof CompoundTag) {
            CompoundTag ct = (CompoundTag)comp;
            g.fill(lx, y + 2, this.sideX + this.sideW - 8, y + 3, border);
            g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.side.components"), lx, y += 8, -7035976, false);
            this.sideInfo(g, lx, y += 12, Component.translatable((String)"ankinbt.side.tags").getString(), String.valueOf(ct.size()));
            y += 12;
        }
        g.fill(lx, y + 2, this.sideX + this.sideW - 8, y + 3, border);
        this.sideInfo(g, lx, y += 8, Component.translatable((String)"ankinbt.side.visible").getString(), String.valueOf(this.visibleNodes.size()));
    }

    private int fadeColor(int color, float factor) {
        int alpha = color >>> 24 & 0xFF;
        return UiTheme.withAlpha(color & 0xFFFFFF, Math.round((float)alpha * factor));
    }

    private void sideInfo(GuiGraphics g, int x, int y, String label, String value) {
        g.drawString(this.font, label, x, y, -10193781, false);
        int maxW = this.sideW - 16 - this.font.width(label) - 4;
        if (this.font.width((String)value) > maxW) {
            value = this.font.plainSubstrByWidth((String)value, maxW - 8) + "..";
        }
        g.drawString(this.font, (String)value, x + this.font.width(label) + 4, y, -7035976, false);
    }

    private void renderFooter(GuiGraphics g) {
        int fy = this.py + this.ph - 20 + 5;
        if (this.statusMsg != null && System.currentTimeMillis() - this.statusTime < 3000L) {
            g.drawString(this.font, this.statusMsg, this.px + 140 + 8, fy, this.statusColor, false);
        } else {
            this.statusMsg = null;
            g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.hint"), this.px + 140 + 8, fy, -10193781, false);
        }
        if (this.selIdx >= 0 && this.selIdx < this.visibleNodes.size()) {
            NbtTreeNode sel = this.visibleNodes.get(this.selIdx);
            String info = sel.getKey() + " : " + sel.getTypeName();
            g.drawString(this.font, info, this.px + this.pw - this.font.width(info) - 10, fy, -10193781, false);
        }
    }

    private int textViewStart(String value, int cursor, int maxWidth) {
        int start;
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int clampedCursor = Math.max(0, Math.min(cursor, value.length()));
        for (start = 0; start < clampedCursor && this.font.width(value.substring(start, clampedCursor)) > maxWidth; ++start) {
        }
        return start;
    }

    private String visibleText(String value, int start, int maxWidth) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int safeStart = Math.max(0, Math.min(start, value.length()));
        String text = value.substring(safeStart);
        return this.font.width(text) <= maxWidth ? text : this.font.plainSubstrByWidth(text, maxWidth);
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        if (this.confirmClose) {
            int dw = 260;
            int dh = 110;
            int dx = (this.width - dw) / 2;
            int dy = (this.height - dh) / 2;
            int by = dy + dh - 32;
            int bw2 = 70;
            int bh2 = 22;
            int saveX = dx + 10;
            if (mx >= (double)saveX && mx < (double)(saveX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                this.saveToItem();
                this.onClose();
                return true;
            }
            int discardX = dx + dw / 2 - bw2 / 2;
            if (mx >= (double)discardX && mx < (double)(discardX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                this.dirty = false;
                this.onClose();
                return true;
            }
            int cancelX = dx + dw - bw2 - 10;
            if (mx >= (double)cancelX && mx < (double)(cancelX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                this.confirmClose = false;
                return true;
            }
            return true;
        }
        for (Btn b : this.buttons) {
            if (!b.isHover((int)mx, (int)my)) continue;
            b.action.run();
            return true;
        }
        if (this.searching) {
            this.layoutSearchBox();
            if (this.searchBox.mouseClicked(mx, my, btn)) {
                this.searchBox.setFocused(true);
                return true;
            }
        }
        if (this.hoverIdx >= 0 && this.hoverIdx < this.visibleNodes.size()) {
            long now = System.currentTimeMillis();
            if (this.hoverIdx == this.lastClickIdx && now - this.lastClickTime < 400L) {
                NbtTreeNode node = this.visibleNodes.get(this.hoverIdx);
                if (!node.isLeaf()) {
                    node.toggleExpanded();
                    this.refreshVisible();
                } else {
                    this.openEditor(node);
                }
                this.lastClickIdx = -1;
            } else {
                this.selIdx = this.hoverIdx;
                this.lastClickIdx = this.hoverIdx;
                this.lastClickTime = now;
            }
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        this.scrollOff -= (int)sy * 3;
        this.clampScroll();
        return true;
    }

    public boolean keyPressed(int key, int scan, int mod) {
        if (this.confirmClose) {
            if (key == 256) {
                this.confirmClose = false;
                return true;
            }
            return true;
        }
        if (this.searching) {
            if (key == 256) {
                this.setSearching(false);
                return true;
            }
            this.layoutSearchBox();
            if (this.searchBox.keyPressed(key, scan, mod)) {
                return true;
            }
            return true;
        }
        if (key == 256) {
            this.tryClose();
            return true;
        }
        if (key == 264 && this.selIdx < this.visibleNodes.size() - 1) {
            ++this.selIdx;
            this.ensureVis(this.selIdx);
            return true;
        }
        if (key == 265 && this.selIdx > 0) {
            --this.selIdx;
            this.ensureVis(this.selIdx);
            return true;
        }
        if (!this.searching && this.selIdx >= 0 && this.selIdx < this.visibleNodes.size()) {
            NbtTreeNode node = this.visibleNodes.get(this.selIdx);
            if (key == 69) {
                if (!node.isLeaf()) {
                    node.toggleExpanded();
                    this.refreshVisible();
                }
                return true;
            }
            if (key == 257) {
                if (node.isLeaf()) {
                    this.openEditor(node);
                } else {
                    node.toggleExpanded();
                    this.refreshVisible();
                }
                return true;
            }
            if (key == 261) {
                this.deleteNode();
                return true;
            }
        }
        if (key == 83 && (mod & 2) != 0) {
            this.saveToItem();
            return true;
        }
        return false;
    }

    public boolean charTyped(char c, int mod) {
        if (this.searching) {
            this.layoutSearchBox();
            this.searchBox.charTyped(c, mod);
            return true;
        }
        return false;
    }

    private void openEditor(NbtTreeNode node) {
        Minecraft.getInstance().setScreen((Screen)new ValueEditScreen(this, node));
    }

    private void deleteNode() {
        if (this.selIdx < 0 || this.selIdx >= this.visibleNodes.size()) {
            return;
        }
        NbtTreeNode node = this.visibleNodes.get(this.selIdx);
        NbtTreeNode parent = node.getParent();
        if (parent == null) {
            return;
        }
        parent.removeChild(node);
        this.dirty = true;
        this.refreshVisible();
        if (this.selIdx >= this.visibleNodes.size()) {
            this.selIdx = this.visibleNodes.size() - 1;
        }
        this.setStatus(Component.translatable((String)"ankinbt.status.deleted").getString(), -7035976);
    }

    private void addTag() {
        NbtTreeNode target;
        NbtTreeNode nbtTreeNode = target = this.selIdx >= 0 && this.selIdx < this.visibleNodes.size() ? this.visibleNodes.get(this.selIdx) : this.rootNode;
        if (!target.isCompound() && !target.isList()) {
            target = target.getParent();
        }
        if (target == null) {
            target = this.rootNode;
        }
        Minecraft.getInstance().setScreen((Screen)new AddTagScreen(this, target));
    }

    public void addTagToNode(NbtTreeNode parent, String key, Tag tag) {
        parent.addChild(key, tag, false);
        parent.setExpanded(true);
        this.dirty = true;
        this.refreshVisible();
        this.setStatus(Component.translatable((String)"ankinbt.status.added", (Object[])new Object[]{key}).getString(), -14498466);
    }

    private static int playerInventoryIndexFromCreativeSlot(int creativeSlot) {
        if (creativeSlot >= 36 && creativeSlot < 45) {
            return creativeSlot - 36;
        }
        if (creativeSlot >= 9 && creativeSlot < 36) {
            return creativeSlot;
        }
        return -1;
    }
    private static int creativePacketSlotFromEditedSlot(int editedSlot) {
        if (editedSlot >= 36 && editedSlot < 45) {
            return editedSlot;
        }
        if (editedSlot >= 0 && editedSlot < 9) {
            return 36 + editedSlot;
        }
        if (editedSlot >= 9 && editedSlot < 36) {
            return editedSlot;
        }
        return -1;
    }

    private void saveToItem() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (!mc.player.isCreative()) {
            this.setStatus(Component.translatable((String)"ankinbt.status.creative_only").getString(), -1096636);
            return;
        }
        CompoundTag rebuilt = this.rootNode.toCompoundTag();
        Optional<ItemStack> opt = NbtHelper.deserializeItemStack(rebuilt);
        if (opt.isEmpty()) {
            this.setStatus(Component.translatable((String)"ankinbt.status.save_error").getString(), -1096636);
            return;
        }
        ItemStack newStack = opt.get();
        VersionCompat.get().sanitizeForCreativeSave(newStack);
        if (this.inventorySlot >= 0) {
            int creativeSlot = NbtEditorScreen.creativePacketSlotFromEditedSlot(this.inventorySlot);
            if (creativeSlot < 0) {
                this.setStatus(Component.translatable((String)"ankinbt.status.save_error").getString(), -1096636);
                return;
            }
            int playerSlot = NbtEditorScreen.playerInventoryIndexFromCreativeSlot(creativeSlot);
            if (playerSlot >= 0) {
                mc.player.getInventory().setItem(playerSlot, newStack.copy());
            }
            mc.gameMode.handleCreativeModeItemAdd(newStack.copy(), creativeSlot);
        } else {
            int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
            mc.player.getInventory().setItem(slot, newStack.copy());
            mc.gameMode.handleCreativeModeItemAdd(newStack.copy(), 36 + slot);
        }
        this.dirty = false;
        this.setStatus(Component.translatable((String)"ankinbt.status.saved").getString(), -14498466);
    }

    private void switchToSimple() {
        AnkiConfig.setPreferredItemEditor("simple");
        Minecraft.getInstance().setScreen((Screen)new SimpleEditorScreen(this.originalStack, this.inventorySlot));
    }

    private void exportNbt() {
        Path path;
        String itemId = this.resolveItemPath(this.originalStack);
        long ts = System.currentTimeMillis() / 1000L;
        String fileName = itemId + "_" + ts;
        CompoundTag rebuilt = this.rootNode.toCompoundTag();
        if (this.hasTinyFd()) {
            String picked = this.tinyFdSavePath(AnkiConfig.getExportPath().resolve(fileName + ".nbt").toString());
            if (picked == null || picked.isBlank()) {
                return;
            }
            path = NbtFileIO.exportNbtToPath(rebuilt, Path.of(picked, new String[0]));
        } else {
            path = NbtFileIO.exportNbt(rebuilt, fileName);
        }
        if (path != null) {
            this.setStatus(Component.translatable((String)"ankinbt.export.success").getString(), -14498466);
        } else {
            this.setStatus(Component.translatable((String)"ankinbt.export.failed").getString(), -1096636);
        }
    }

    private String resolveItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "minecraft:air";
        }
        try {
            Holder.Reference holder = stack.getItem().builtInRegistryHolder();
            Object key = holder.getClass().getMethod("key", new Class[0]).invoke(holder, new Object[0]);
            try {
                Object loc = key.getClass().getMethod("location", new Class[0]).invoke(key, new Object[0]);
                if (loc != null) {
                    return loc.toString();
                }
            }
            catch (Throwable loc) {
                // empty catch block
            }
            try {
                Object id = key.getClass().getMethod("identifier", new Class[0]).invoke(key, new Object[0]);
                if (id != null) {
                    return id.toString();
                }
            }
            catch (Throwable throwable) {}
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return stack.getItem().toString();
    }

    private String resolveItemPath(ItemStack stack) {
        String id = this.resolveItemId(stack);
        int idx = id.indexOf(58);
        return idx >= 0 && idx + 1 < id.length() ? id.substring(idx + 1) : id;
    }

    private void importNbt() {
        String loadedName;
        CompoundTag tag;
        if (this.hasTinyFd()) {
            String picked = this.tinyFdOpenPath(AnkiConfig.getExportPath().toString());
            if (picked == null || picked.isBlank()) {
                return;
            }
            tag = NbtFileIO.importNbt(Path.of(picked, new String[0]));
            loadedName = Path.of(picked, new String[0]).getFileName().toString();
        } else {
            List<NbtFileIO.NbtFileEntry> files = NbtFileIO.listNbtFiles();
            if (files.isEmpty()) {
                this.setStatus(Component.translatable((String)"ankinbt.import.no_files").getString(), -1096636);
                return;
            }
            NbtFileIO.NbtFileEntry latest = files.get(0);
            tag = NbtFileIO.importNbt(latest.path());
            loadedName = latest.name();
        }
        if (tag != null) {
            this.fullItemTag = tag;
            this.rebuildTree();
            this.dirty = true;
            this.setStatus(Component.translatable((String)"ankinbt.import.success").getString() + " (" + loadedName + ")", -14498466);
        } else {
            this.setStatus(Component.translatable((String)"ankinbt.import.load_failed").getString(), -1096636);
        }
    }

    private boolean hasTinyFd() {
        if (!AnkiConfig.isNativeFileDialogEnabled()) {
            return false;
        }
        try {
            Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
            return true;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private String tinyFdSavePath(String defaultPath) {
        return this.tinyFdDialog("tinyfd_saveFileDialog", defaultPath, false);
    }

    private String tinyFdOpenPath(String defaultPath) {
        return this.tinyFdDialog("tinyfd_openFileDialog", defaultPath, true);
    }

    private String tinyFdDialog(String methodName, String defaultPath, boolean isOpen) {
        long now = System.currentTimeMillis();
        if (this.nativeDialogOpen || now - this.lastNativeDialogAt < 600L) {
            return null;
        }
        this.nativeDialogOpen = true;
        try {
            Class<?> clazz = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
            for (Method m : clazz.getMethods()) {
                Object out;
                if (!m.getName().equals(methodName) || !((out = m.invoke(null, this.tinyFdArgs(m.getParameterTypes(), defaultPath, isOpen))) instanceof CharSequence)) continue;
                CharSequence cs = (CharSequence)out;
                return cs.toString();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        finally {
            this.lastNativeDialogAt = System.currentTimeMillis();
            this.nativeDialogOpen = false;
        }
        return null;
    }

    private Object[] tinyFdArgs(Class<?>[] parameterTypes, String defaultPath, boolean isOpen) {
        Object[] args = new Object[parameterTypes.length];
        int stringIndex = 0;
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> pt = parameterTypes[i];
            if (CharSequence.class.isAssignableFrom(pt) || pt == String.class) {
                args[i] = stringIndex == 0 ? (isOpen ? Component.translatable((String)"ankinbt.simple.import_nbt").getString() : Component.translatable((String)"ankinbt.simple.export_nbt").getString()) : (stringIndex == 1 ? defaultPath : "NBT files (*.nbt)");
                ++stringIndex;
                continue;
            }
            args[i] = pt == String[].class ? new String[]{"*.nbt"} : (pt == Boolean.TYPE || pt == Boolean.class ? Boolean.FALSE : (pt == Integer.TYPE || pt == Integer.class ? Integer.valueOf(1) : (pt.getName().equals("org.lwjgl.PointerBuffer") ? null : null)));
        }
        return args;
    }

    private void tryClose() {
        if (this.dirty && AnkiConfig.isConfirmOnClose()) {
            this.confirmClose = true;
        } else {
            this.onClose();
        }
    }

    public void onNodeEdited() {
        this.dirty = true;
        this.refreshVisible();
        this.setStatus(Component.translatable((String)"ankinbt.status.edited").getString(), -7035976);
    }

    private void setStatus(String msg, int color) {
        this.statusMsg = msg;
        this.statusColor = color;
        this.statusTime = System.currentTimeMillis();
    }

    private void ensureVis(int idx) {
        if (idx < this.scrollOff) {
            this.scrollOff = idx;
        }
        if (idx >= this.scrollOff + this.maxRows) {
            this.scrollOff = idx - this.maxRows + 1;
        }
        this.clampScroll();
    }

    private void clampScroll() {
        int max = Math.max(0, this.visibleNodes.size() - this.maxRows);
        this.scrollOff = Math.max(0, Math.min(this.scrollOff, max));
    }

    private void expandAll(NbtTreeNode n) {
        n.setExpanded(true);
        for (NbtTreeNode c : n.getChildren()) {
            this.expandAll(c);
        }
    }

    private void collapseAll(NbtTreeNode n) {
        n.setExpanded(false);
        for (NbtTreeNode c : n.getChildren()) {
            this.collapseAll(c);
        }
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public CompoundTag getFullItemTag() {
        return this.fullItemTag;
    }

    static class Btn {
        final int x;
        final int y;
        final int w;
        final int h;
        final String label;
        final Component tooltip;
        final Runnable action;

        Btn(int x, int y, int w, int h, String label, Component tooltip, Runnable action) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
            this.tooltip = tooltip;
            this.action = action;
        }

        boolean isHover(int mx, int my) {
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }

        void render(GuiGraphics g, Font f, int mx, int my) {
            boolean h = this.isHover(mx, my);
            g.fill(this.x, this.y, this.x + this.w, this.y + this.h, h ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(f, this.label, this.x + (this.w - f.width(this.label)) / 2, this.y + (this.h - 8) / 2, -1906448, false);
            if (h && this.tooltip != null) {
                VersionCompat.get().renderTooltip(g, f, this.tooltip, mx, my);
            }
        }
    }
}


