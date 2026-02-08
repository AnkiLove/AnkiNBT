package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtTreeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NbtEditorScreen extends Screen {

    private static final int HEADER_H = 32, ROW_H = 18, INDENT = 14, SIDEBAR_W = 140;
    private static final int SCROLLBAR_W = 6, FOOTER_H = 20, MARGIN = 16;
    private static final int BG = 0xD8080810, SIDEBAR_BG = 0xD80C0C18, HEADER_BG = 0xD8101020;
    private static final int BORDER = 0xFF222236, HOVER = 0x30FFFFFF, SELECT_BG = 0x28_63_66_F1;
    private static final int ACCENT = 0xFF6366F1, C1 = 0xFFE2E8F0, C2 = 0xFF94A3B8, C3 = 0xFF64748B;
    private static final int SB_TRACK = 0x30FFFFFF, SB_THUMB = 0x70FFFFFF;
    private static final int BTN_BG = 0x30FFFFFF, BTN_HOVER = 0x50FFFFFF;
    private static final int SUCCESS = 0xFF22C55E, ERROR_C = 0xFFEF4444;

    private final ItemStack originalStack;
    private CompoundTag fullItemTag;
    private NbtTreeNode rootNode;
    private List<NbtTreeNode> visibleNodes = new ArrayList<>();
    private int scrollOff = 0, maxRows, selIdx = -1, hoverIdx = -1;
    private int lastClickIdx = -1; private long lastClickTime = 0;
    private int px, py, pw, ph, sideX, sideY, sideW, sideH, treeX, treeY, treeW, treeH;
    private String searchQ = ""; private boolean searching = false;
    private final List<Btn> buttons = new ArrayList<>();
    private String statusMsg = null; private long statusTime = 0; private int statusColor = C3;
    private boolean dirty = false;

    public NbtEditorScreen(ItemStack stack) {
        super(Component.translatable("ankinbt.title"));
        this.originalStack = stack;
        var opt = NbtHelper.serializeItemStack(stack);
        this.fullItemTag = opt.orElseGet(() -> {
            CompoundTag fallback = new CompoundTag();
            fallback.putString("id", stack.getItem().builtInRegistryHolder().key().location().toString());
            fallback.putInt("count", stack.getCount());
            return fallback;
        });
        rebuildTree();
    }

    private void rebuildTree() {
        rootNode = new NbtTreeNode("", fullItemTag, null, AnkiConfig.isTreeExpandedByDefault());
        rootNode.setExpanded(true); refreshVisible();
    }
    private void refreshVisible() {
        visibleNodes.clear();
        if (rootNode != null) rootNode.collectVisible(visibleNodes);
        if (searching && !searchQ.isEmpty()) {
            String q = searchQ.toLowerCase();
            visibleNodes = visibleNodes.stream()
                    .filter(n -> n.getKey().toLowerCase().contains(q) || n.getDisplayValue().toLowerCase().contains(q) || n.getTypeName().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }
        clampScroll();
    }

    @Override
    protected void init() {
        super.init();
        pw = Math.min(width - MARGIN * 2, 620); ph = Math.min(height - MARGIN * 2, 420);
        px = (width - pw) / 2; py = (height - ph) / 2;
        sideX = px + 1; sideY = py + HEADER_H + 1; sideW = SIDEBAR_W; sideH = ph - HEADER_H - FOOTER_H - 2;
        treeX = px + SIDEBAR_W + 2; treeY = py + HEADER_H + 1;
        treeW = pw - SIDEBAR_W - SCROLLBAR_W - 6; treeH = ph - HEADER_H - FOOTER_H - 2;
        maxRows = treeH / ROW_H;
        buildButtons();
    }

    private void buildButtons() {
        buttons.clear();
        int bw = 22, gap = 3, by = py + 6, bx = px + pw - MARGIN - 2;
        bx -= bw; buttons.add(new Btn(bx, by, bw, bw, "X", Component.translatable("ankinbt.btn.close"), this::onClose));
        bx -= bw + gap; buttons.add(new Btn(bx, by, bw, bw, "-", Component.translatable("ankinbt.btn.collapse"), () -> { collapseAll(rootNode); rootNode.setExpanded(true); refreshVisible(); }));
        bx -= bw + gap; buttons.add(new Btn(bx, by, bw, bw, "+", Component.translatable("ankinbt.btn.expand"), () -> { expandAll(rootNode); refreshVisible(); }));
        bx -= bw + gap; buttons.add(new Btn(bx, by, bw, bw, "S", Component.translatable("ankinbt.btn.search"), () -> { searching = !searching; if (!searching) searchQ = ""; refreshVisible(); }));
        bx -= bw + gap; buttons.add(new Btn(bx, by, bw, bw, "N", Component.translatable("ankinbt.btn.add"), this::addTag));
        int saveW = 40; bx -= saveW + gap + 4;
        buttons.add(new Btn(bx, by, saveW, bw, Component.translatable("ankinbt.btn.save").getString(), Component.translatable("ankinbt.btn.save.tip"), this::saveToItem));
        int modeW = 50; bx -= modeW + gap + 4;
        buttons.add(new Btn(bx, by, modeW, bw, Component.translatable("ankinbt.btn.simple").getString(), Component.translatable("ankinbt.btn.switch_simple"), this::switchToSimple));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x70000000);
        g.fill(px, py, px + pw, py + ph, BG);
        drawBorder(g, px, py, pw, ph, BORDER);
        g.fill(px + 1, py + 1, px + pw - 1, py + HEADER_H, HEADER_BG);
        g.fill(px + 1, py + HEADER_H, px + pw - 1, py + HEADER_H + 1, BORDER);
        g.drawString(font, Component.translatable("ankinbt.title"), px + 10, py + 12, C1, false);
        if (dirty) g.drawString(font, "*", px + 10 + font.width(Component.translatable("ankinbt.title")), py + 12, ERROR_C, false);
        for (Btn b : buttons) b.render(g, font, mx, my);
        renderSidebar(g);
        g.fill(px + SIDEBAR_W + 1, py + HEADER_H + 1, px + SIDEBAR_W + 2, py + ph - FOOTER_H, BORDER);

        int atY = treeY, atH = treeH;
        if (searching) {
            g.fill(treeX, treeY, treeX + treeW, treeY + ROW_H, 0x40000000);
            drawBorder(g, treeX, treeY, treeW, ROW_H, ACCENT);
            String disp = searchQ.isEmpty() ? Component.translatable("ankinbt.search.hint").getString() : searchQ + "_";
            g.drawString(font, disp, treeX + 4, treeY + 5, searchQ.isEmpty() ? C3 : C1, false);
            atY += ROW_H + 2; atH -= ROW_H + 2; maxRows = atH / ROW_H;
        } else { maxRows = treeH / ROW_H; }

        hoverIdx = -1;
        int end = Math.min(scrollOff + maxRows, visibleNodes.size());
        for (int i = scrollOff; i < end; i++) {
            int ry = atY + (i - scrollOff) * ROW_H;
            NbtTreeNode node = visibleNodes.get(i);
            boolean hovered = mx >= treeX && mx < treeX + treeW && my >= ry && my < ry + ROW_H;
            if (hovered) { hoverIdx = i; g.fill(treeX, ry, treeX + treeW, ry + ROW_H, HOVER); }
            if (i == selIdx) { g.fill(treeX, ry, treeX + treeW, ry + ROW_H, SELECT_BG); g.fill(treeX, ry, treeX + 2, ry + ROW_H, ACCENT); }
            int indent = node.getDepth() * INDENT; int tx = treeX + 6 + indent;
            if (!node.isLeaf()) { g.drawString(font, node.isExpanded() ? "v" : ">", tx, ry + 5, C3, false); tx += 10; }
            int tc = NbtHelper.getTagColor(node.getTag());
            String badge = node.getTypeName(); if (badge.length() > 3) badge = badge.substring(0, 3);
            g.drawString(font, badge, tx, ry + 5, tc, false); tx += font.width(badge) + 4;
            String key = node.getKey();
            if (!key.isEmpty()) { g.drawString(font, key, tx, ry + 5, C1, false); tx += font.width(key) + 6; }
            String val = node.getDisplayValue(); if (val.length() > 36) val = val.substring(0, 33) + "...";
            g.drawString(font, val, tx, ry + 5, C2, false);
        }
        if (visibleNodes.size() > maxRows) {
            int sbx = px + pw - SCROLLBAR_W - 3;
            g.fill(sbx, atY, sbx + SCROLLBAR_W, atY + atH, SB_TRACK);
            float ratio = (float) maxRows / visibleNodes.size();
            int thumbH = Math.max(16, (int) (atH * ratio));
            float sr = (float) scrollOff / Math.max(1, visibleNodes.size() - maxRows);
            int thumbY = atY + (int) ((atH - thumbH) * sr);
            g.fill(sbx, thumbY, sbx + SCROLLBAR_W, thumbY + thumbH, SB_THUMB);
        }
        g.fill(px + 1, py + ph - FOOTER_H, px + pw - 1, py + ph - FOOTER_H + 1, BORDER);
        renderFooter(g);
    }

    private void renderSidebar(GuiGraphics g) {
        g.fill(sideX, sideY, sideX + sideW, sideY + sideH, SIDEBAR_BG);
        int y = sideY + 8, lx = sideX + 8;
        g.renderItem(originalStack, lx + (sideW - 32) / 2, y); y += 24;
        String name = originalStack.getHoverName().getString();
        if (font.width(name) > sideW - 16) name = font.plainSubstrByWidth(name, sideW - 22) + "...";
        g.drawString(font, name, lx, y, C1, false); y += 14;
        g.fill(lx, y, sideX + sideW - 8, y + 1, BORDER); y += 6;
        if (fullItemTag.contains("id")) { sideInfo(g, lx, y, Component.translatable("ankinbt.side.id").getString(), VersionCompat.get().compoundGetString(fullItemTag, "id")); y += 12; }
        if (fullItemTag.contains("count")) { sideInfo(g, lx, y, Component.translatable("ankinbt.side.count").getString(), String.valueOf(VersionCompat.get().compoundGetInt(fullItemTag, "count"))); y += 12; }
        if (fullItemTag.contains("components")) {
            Tag comp = fullItemTag.get("components");
            if (comp instanceof CompoundTag ct) { g.fill(lx, y + 2, sideX + sideW - 8, y + 3, BORDER); y += 8;
                g.drawString(font, Component.translatable("ankinbt.side.components"), lx, y, C2, false); y += 12;
                sideInfo(g, lx, y, Component.translatable("ankinbt.side.tags").getString(), String.valueOf(ct.size())); y += 12; }
        }
        g.fill(lx, y + 2, sideX + sideW - 8, y + 3, BORDER); y += 8;
        sideInfo(g, lx, y, Component.translatable("ankinbt.side.visible").getString(), String.valueOf(visibleNodes.size()));
    }
    private void sideInfo(GuiGraphics g, int x, int y, String label, String value) {
        g.drawString(font, label, x, y, C3, false);
        int maxW = sideW - 16 - font.width(label) - 4;
        if (font.width(value) > maxW) value = font.plainSubstrByWidth(value, maxW - 8) + "..";
        g.drawString(font, value, x + font.width(label) + 4, y, C2, false);
    }
    private void renderFooter(GuiGraphics g) {
        int fy = py + ph - FOOTER_H + 5;
        if (statusMsg != null && System.currentTimeMillis() - statusTime < 3000) { g.drawString(font, statusMsg, px + SIDEBAR_W + 8, fy, statusColor, false); }
        else { statusMsg = null; g.drawString(font, Component.translatable("ankinbt.hint"), px + SIDEBAR_W + 8, fy, C3, false); }
        if (selIdx >= 0 && selIdx < visibleNodes.size()) {
            NbtTreeNode sel = visibleNodes.get(selIdx); String info = sel.getKey() + " : " + sel.getTypeName();
            g.drawString(font, info, px + pw - font.width(info) - 10, fy, C3, false);
        }
    }

    // ==================== INPUT ====================
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x(); double my = event.y();
        for (Btn b : buttons) if (b.isHover((int) mx, (int) my)) { b.action.run(); return true; }
        if (hoverIdx >= 0 && hoverIdx < visibleNodes.size()) {
            long now = System.currentTimeMillis();
            if (hoverIdx == lastClickIdx && now - lastClickTime < 400) {
                NbtTreeNode node = visibleNodes.get(hoverIdx);
                if (!node.isLeaf()) { node.toggleExpanded(); refreshVisible(); } else openEditor(node);
                lastClickIdx = -1;
            } else { selIdx = hoverIdx; lastClickIdx = hoverIdx; lastClickTime = now; }
            return true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scrollOff -= (int) sy * 3; clampScroll(); return true;
    }
    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key(); int mod = event.modifiers();
        if (searching) {
            if (key == 259 && !searchQ.isEmpty()) { searchQ = searchQ.substring(0, searchQ.length() - 1); refreshVisible(); return true; }
            if (key == 256) { searching = false; searchQ = ""; refreshVisible(); return true; }
        }
        if (key == 256) { onClose(); return true; }
        if (key == 264 && selIdx < visibleNodes.size() - 1) { selIdx++; ensureVis(selIdx); return true; }
        if (key == 265 && selIdx > 0) { selIdx--; ensureVis(selIdx); return true; }
        if (!searching && selIdx >= 0 && selIdx < visibleNodes.size()) {
            NbtTreeNode node = visibleNodes.get(selIdx);
            if (key == 69) { if (!node.isLeaf()) { node.toggleExpanded(); refreshVisible(); } return true; }
            if (key == 257) { if (node.isLeaf()) openEditor(node); else { node.toggleExpanded(); refreshVisible(); } return true; }
            if (key == 261) { deleteNode(); return true; }
        }
        if (key == 83 && (mod & 2) != 0) { saveToItem(); return true; }
        return super.keyPressed(event);
    }
    @Override
    public boolean charTyped(CharacterEvent event) {
        char c = (char) event.codepoint();
        if (searching) { searchQ += c; refreshVisible(); return true; }
        return super.charTyped(event);
    }

    // ==================== ACTIONS ====================
    private void openEditor(NbtTreeNode node) { Minecraft.getInstance().setScreen(new ValueEditScreen(this, node)); }
    private void deleteNode() {
        if (selIdx < 0 || selIdx >= visibleNodes.size()) return;
        NbtTreeNode node = visibleNodes.get(selIdx); NbtTreeNode parent = node.getParent();
        if (parent == null) return; parent.removeChild(node); dirty = true; refreshVisible();
        if (selIdx >= visibleNodes.size()) selIdx = visibleNodes.size() - 1;
        setStatus(Component.translatable("ankinbt.status.deleted").getString(), C2);
    }
    private void addTag() {
        NbtTreeNode target = (selIdx >= 0 && selIdx < visibleNodes.size()) ? visibleNodes.get(selIdx) : rootNode;
        if (!target.isCompound() && !target.isList()) target = target.getParent();
        if (target == null) target = rootNode;
        Minecraft.getInstance().setScreen(new AddTagScreen(this, target));
    }
    public void addTagToNode(NbtTreeNode parent, String key, Tag tag) {
        parent.addChild(key, tag, false); parent.setExpanded(true); dirty = true; refreshVisible();
        setStatus(Component.translatable("ankinbt.status.added", key).getString(), SUCCESS);
    }
    private void saveToItem() {
        Minecraft mc = Minecraft.getInstance(); if (mc.player == null) return;
        if (!mc.player.isCreative()) { setStatus(Component.translatable("ankinbt.status.creative_only").getString(), ERROR_C); return; }
        CompoundTag rebuilt = rootNode.toCompoundTag();
        var opt = NbtHelper.deserializeItemStack(rebuilt);
        if (opt.isEmpty()) { setStatus(Component.translatable("ankinbt.status.save_error").getString(), ERROR_C); return; }
        ItemStack newStack = opt.get();
        int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
        mc.player.getInventory().setItem(slot, newStack.copy());
        mc.gameMode.handleCreativeModeItemAdd(newStack.copy(), 36 + slot);
        dirty = false; setStatus(Component.translatable("ankinbt.status.saved").getString(), SUCCESS);
    }
    private void switchToSimple() { Minecraft.getInstance().setScreen(new SimpleEditorScreen(originalStack)); }
    public void onNodeEdited() { dirty = true; refreshVisible(); setStatus(Component.translatable("ankinbt.status.edited").getString(), C2); }
    private void setStatus(String msg, int color) { statusMsg = msg; statusColor = color; statusTime = System.currentTimeMillis(); }

    // ==================== UTIL ====================
    private void ensureVis(int idx) { if (idx < scrollOff) scrollOff = idx; if (idx >= scrollOff + maxRows) scrollOff = idx - maxRows + 1; clampScroll(); }
    private void clampScroll() { int max = Math.max(0, visibleNodes.size() - maxRows); scrollOff = Math.max(0, Math.min(scrollOff, max)); }
    private void expandAll(NbtTreeNode n) { n.setExpanded(true); for (var c : n.getChildren()) expandAll(c); }
    private void collapseAll(NbtTreeNode n) { n.setExpanded(false); for (var c : n.getChildren()) collapseAll(c); }
    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c); g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c); g.fill(x + w - 1, y, x + w, y + h, c);
    }
    @Override public boolean isPauseScreen() { return false; }
    public CompoundTag getFullItemTag() { return fullItemTag; }

    static class Btn {
        final int x, y, w, h; final String label; final Component tooltip; final Runnable action;
        Btn(int x, int y, int w, int h, String label, Component tooltip, Runnable action) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.label = label; this.tooltip = tooltip; this.action = action;
        }
        boolean isHover(int mx, int my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
        void render(GuiGraphics g, net.minecraft.client.gui.Font f, int mx, int my) {
            boolean h = isHover(mx, my);
            g.fill(x, y, x + w, this.y + this.h, h ? BTN_HOVER : BTN_BG);
            g.drawString(f, label, x + (w - f.width(label)) / 2, y + (this.h - 8) / 2, C1, false);
            if (h && tooltip != null) VersionCompat.get().renderTooltip(g, f, tooltip, mx, my);
        }
    }
}
