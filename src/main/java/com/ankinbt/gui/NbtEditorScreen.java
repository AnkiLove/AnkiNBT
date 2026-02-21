package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtFileIO;
import com.ankinbt.nbt.NbtTreeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main NBT editor screen. Uses ItemStack.CODEC (like NBTEdit) to serialize
 * the full item to a CompoundTag for editing, then deserializes back on save.
 * Layout: left sidebar (item info) + right tree view.
 */
public class NbtEditorScreen extends Screen {

    // Layout
    private static final int HEADER_H = 32;
    private static final int ROW_H = 18;
    private static final int INDENT = 14;
    private static final int SIDEBAR_W = 140;
    private static final int SCROLLBAR_W = 6;
    private static final int FOOTER_H = 20;
    private static final int MARGIN = 16;

    // Colors
    private static final int BG = 0xD8080810;
    private static final int SIDEBAR_BG = 0xD80C0C18;
    private static final int HEADER_BG = 0xD8101020;
    private static final int BORDER = 0xFF222236;
    private static final int HOVER = 0x30FFFFFF;
    private static final int SELECT_BG = 0x28_63_66_F1;
    private static final int ACCENT = 0xFF6366F1;
    private static final int C1 = 0xFFE2E8F0;
    private static final int C2 = 0xFF94A3B8;
    private static final int C3 = 0xFF64748B;
    private static final int SB_TRACK = 0x30FFFFFF;
    private static final int SB_THUMB = 0x70FFFFFF;
    private static final int BTN_BG = 0x30FFFFFF;
    private static final int BTN_HOVER = 0x50FFFFFF;
    private static final int SUCCESS = 0xFF22C55E;
    private static final int ERROR_C = 0xFFEF4444;

    private final ItemStack originalStack;
    private CompoundTag fullItemTag; // Full item serialized via ItemStack.CODEC
    private NbtTreeNode rootNode;
    private List<NbtTreeNode> visibleNodes = new ArrayList<>();

    private int scrollOff = 0, maxRows;
    private int selIdx = -1, hoverIdx = -1;
    private int lastClickIdx = -1;
    private long lastClickTime = 0;

    private int px, py, pw, ph;
    private int sideX, sideY, sideW, sideH;
    private int treeX, treeY, treeW, treeH;

    private String searchQ = "";
    private boolean searching = false;
    private final List<Btn> buttons = new ArrayList<>();

    private String statusMsg = null;
    private long statusTime = 0;
    private int statusColor = C3;
    private boolean dirty = false;
    private boolean confirmClose = false;

    public NbtEditorScreen(ItemStack stack) {
        super(Component.translatable("ankinbt.title"));
        this.originalStack = stack;

        // Serialize the full ItemStack via CODEC (like NBTEdit)
        var opt = NbtHelper.serializeItemStack(stack);
        this.fullItemTag = opt.orElseGet(() -> {
            // Fallback: just use an empty compound with the item id
            CompoundTag fallback = new CompoundTag();
            fallback.putString("id", stack.getItem().builtInRegistryHolder().key().location().toString());
            fallback.putInt("count", stack.getCount());
            return fallback;
        });

        rebuildTree();
    }

    private void rebuildTree() {
        rootNode = new NbtTreeNode("", fullItemTag, null, AnkiConfig.isTreeExpandedByDefault());
        rootNode.setExpanded(true);
        refreshVisible();
    }

    private void refreshVisible() {
        visibleNodes.clear();
        if (rootNode != null) rootNode.collectVisible(visibleNodes);
        if (searching && !searchQ.isEmpty()) {
            String q = searchQ.toLowerCase();
            visibleNodes = visibleNodes.stream()
                    .filter(n -> n.getKey().toLowerCase().contains(q)
                            || n.getDisplayValue().toLowerCase().contains(q)
                            || n.getTypeName().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }
        clampScroll();
    }

    @Override
    protected void init() {
        super.init();
        pw = Math.min(width - MARGIN * 2, 620);
        ph = Math.min(height - MARGIN * 2, 420);
        px = (width - pw) / 2;
        py = (height - ph) / 2;

        sideX = px + 1; sideY = py + HEADER_H + 1;
        sideW = SIDEBAR_W; sideH = ph - HEADER_H - FOOTER_H - 2;

        treeX = px + SIDEBAR_W + 2; treeY = py + HEADER_H + 1;
        treeW = pw - SIDEBAR_W - SCROLLBAR_W - 6;
        treeH = ph - HEADER_H - FOOTER_H - 2;
        maxRows = treeH / ROW_H;

        buildButtons();
    }

    private void buildButtons() {
        buttons.clear();
        int bw = 22, gap = 3, by = py + 6;
        int bx = px + pw - MARGIN - 2;

        bx -= bw;
        buttons.add(new Btn(bx, by, bw, bw, "X", Component.translatable("ankinbt.btn.close"), this::tryClose));
        bx -= bw + gap;
        buttons.add(new Btn(bx, by, bw, bw, "-", Component.translatable("ankinbt.btn.collapse"), () -> {
            collapseAll(rootNode); rootNode.setExpanded(true); refreshVisible();
        }));
        bx -= bw + gap;
        buttons.add(new Btn(bx, by, bw, bw, "+", Component.translatable("ankinbt.btn.expand"), () -> {
            expandAll(rootNode); refreshVisible();
        }));
        bx -= bw + gap;
        buttons.add(new Btn(bx, by, bw, bw, "S", Component.translatable("ankinbt.btn.search"), () -> {
            searching = !searching; if (!searching) searchQ = ""; refreshVisible();
        }));
        bx -= bw + gap;
        buttons.add(new Btn(bx, by, bw, bw, "N", Component.translatable("ankinbt.btn.add"), this::addTag));

        int saveW = 40;
        bx -= saveW + gap + 4;
        buttons.add(new Btn(bx, by, saveW, bw,
                Component.translatable("ankinbt.btn.save").getString(),
                Component.translatable("ankinbt.btn.save.tip"), this::saveToItem));

        // Mode switch button
        int modeW = 50;
        bx -= modeW + gap + 4;
        buttons.add(new Btn(bx, by, modeW, bw,
                Component.translatable("ankinbt.btn.simple").getString(),
                Component.translatable("ankinbt.btn.switch_simple"), this::switchToSimple));

        // Export button
        int expW = 30;
        bx -= expW + gap;
        buttons.add(new Btn(bx, by, expW, bw, "Ex",
                Component.translatable("ankinbt.simple.export_nbt"), this::exportNbt));

        // Import button
        int impW = 30;
        bx -= impW + gap;
        buttons.add(new Btn(bx, by, impW, bw, "Im",
                Component.translatable("ankinbt.simple.import_nbt"), this::importNbt));
    }

    // ==================== RENDER ====================

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x70000000);

        // Panel
        g.fill(px, py, px + pw, py + ph, BG);
        drawBorder(g, px, py, pw, ph, BORDER);

        // Header
        g.fill(px + 1, py + 1, px + pw - 1, py + HEADER_H, HEADER_BG);
        g.fill(px + 1, py + HEADER_H, px + pw - 1, py + HEADER_H + 1, BORDER);
        g.drawString(font, Component.translatable("ankinbt.title"), px + 10, py + 12, C1, false);
        if (dirty) g.drawString(font, "*", px + 10 + font.width(Component.translatable("ankinbt.title")), py + 12, ERROR_C, false);

        for (Btn b : buttons) b.render(g, font, mx, my);

        // Sidebar
        renderSidebar(g);
        g.fill(px + SIDEBAR_W + 1, py + HEADER_H + 1, px + SIDEBAR_W + 2, py + ph - FOOTER_H, BORDER);

        // Search
        int atY = treeY, atH = treeH;
        if (searching) {
            g.fill(treeX, treeY, treeX + treeW, treeY + ROW_H, 0x40000000);
            drawBorder(g, treeX, treeY, treeW, ROW_H, ACCENT);
            String disp = searchQ.isEmpty() ? Component.translatable("ankinbt.search.hint").getString() : searchQ + "_";
            g.drawString(font, disp, treeX + 4, treeY + 5, searchQ.isEmpty() ? C3 : C1, false);
            atY += ROW_H + 2; atH -= ROW_H + 2;
            maxRows = atH / ROW_H;
        } else {
            maxRows = treeH / ROW_H;
        }

        // Tree
        hoverIdx = -1;
        int end = Math.min(scrollOff + maxRows, visibleNodes.size());
        for (int i = scrollOff; i < end; i++) {
            int ry = atY + (i - scrollOff) * ROW_H;
            NbtTreeNode node = visibleNodes.get(i);
            boolean hovered = mx >= treeX && mx < treeX + treeW && my >= ry && my < ry + ROW_H;
            if (hovered) { hoverIdx = i; g.fill(treeX, ry, treeX + treeW, ry + ROW_H, HOVER); }
            if (i == selIdx) {
                g.fill(treeX, ry, treeX + treeW, ry + ROW_H, SELECT_BG);
                g.fill(treeX, ry, treeX + 2, ry + ROW_H, ACCENT);
            }

            int indent = node.getDepth() * INDENT;
            int tx = treeX + 6 + indent;

            if (!node.isLeaf()) {
                g.drawString(font, node.isExpanded() ? "v" : ">", tx, ry + 5, C3, false);
                tx += 10;
            }

            int tc = NbtHelper.getTagColor(node.getTag());
            String badge = node.getTypeName();
            if (badge.length() > 3) badge = badge.substring(0, 3);
            g.drawString(font, badge, tx, ry + 5, tc, false);
            tx += font.width(badge) + 4;

            String key = node.getKey();
            if (!key.isEmpty()) {
                g.drawString(font, key, tx, ry + 5, C1, false);
                tx += font.width(key) + 6;
            }

            String val = node.getDisplayValue();
            if (val.length() > 36) val = val.substring(0, 33) + "...";
            g.drawString(font, val, tx, ry + 5, C2, false);
        }

        // Scrollbar
        if (visibleNodes.size() > maxRows) {
            int sbx = px + pw - SCROLLBAR_W - 3;
            g.fill(sbx, atY, sbx + SCROLLBAR_W, atY + atH, SB_TRACK);
            float ratio = (float) maxRows / visibleNodes.size();
            int thumbH = Math.max(16, (int) (atH * ratio));
            float sr = (float) scrollOff / Math.max(1, visibleNodes.size() - maxRows);
            int thumbY = atY + (int) ((atH - thumbH) * sr);
            g.fill(sbx, thumbY, sbx + SCROLLBAR_W, thumbY + thumbH, SB_THUMB);
        }

        // Footer
        g.fill(px + 1, py + ph - FOOTER_H, px + pw - 1, py + ph - FOOTER_H + 1, BORDER);
        renderFooter(g);

        // Confirm close dialog
        if (confirmClose) {
            renderConfirmClose(g, mx, my);
        }
    }

    private void renderConfirmClose(GuiGraphics g, int mx, int my) {
        g.fill(0, 0, width, height, 0x60000000);
        int dw = 260, dh = 110;
        int dx = (width - dw) / 2, dy = (height - dh) / 2;
        g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
        drawBorder(g, dx, dy, dw, dh, ERROR_C);

        String title = Component.translatable("ankinbt.confirm.title").getString();
        g.drawString(font, title, dx + 10, dy + 10, C1, false);
        g.fill(dx + 1, dy + 24, dx + dw - 1, dy + 25, BORDER);
        g.drawString(font, Component.translatable("ankinbt.confirm.unsaved").getString(), dx + 10, dy + 32, C2, false);
        g.drawString(font, Component.translatable("ankinbt.confirm.discard_hint").getString(), dx + 10, dy + 46, C3, false);

        int by = dy + dh - 32;
        int bw2 = 70, bh2 = 22;

        int saveX = dx + 10;
        boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
        g.fill(saveX, by, saveX + bw2, by + bh2, sh ? ACCENT : 0xFF4F46E5);
        String saveLabel = Component.translatable("ankinbt.confirm.save_close").getString();
        g.drawString(font, saveLabel, saveX + (bw2 - font.width(saveLabel)) / 2, by + 7, C1, false);

        int discardX = dx + dw / 2 - bw2 / 2;
        boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
        g.fill(discardX, by, discardX + bw2, by + bh2, dh2 ? 0x80EF4444 : 0x40EF4444);
        String discardLabel = Component.translatable("ankinbt.confirm.discard").getString();
        g.drawString(font, discardLabel, discardX + (bw2 - font.width(discardLabel)) / 2, by + 7, C1, false);

        int cancelX = dx + dw - bw2 - 10;
        boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
        g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
        String cancelLabel = Component.translatable("ankinbt.edit.cancel").getString();
        g.drawString(font, cancelLabel, cancelX + (bw2 - font.width(cancelLabel)) / 2, by + 7, C2, false);
    }

    private void renderSidebar(GuiGraphics g) {
        g.fill(sideX, sideY, sideX + sideW, sideY + sideH, SIDEBAR_BG);
        int y = sideY + 8, lx = sideX + 8;

        g.renderItem(originalStack, lx + (sideW - 32) / 2, y);
        y += 24;

        String name = originalStack.getHoverName().getString();
        if (font.width(name) > sideW - 16) name = font.plainSubstrByWidth(name, sideW - 22) + "...";
        g.drawString(font, name, lx, y, C1, false);
        y += 14;

        g.fill(lx, y, sideX + sideW - 8, y + 1, BORDER);
        y += 6;

        // Item info from the serialized tag
        if (fullItemTag.contains("id")) {
            sideInfo(g, lx, y, Component.translatable("ankinbt.side.id").getString(), VersionCompat.get().compoundGetString(fullItemTag, "id"));
            y += 12;
        }
        if (fullItemTag.contains("count")) {
            sideInfo(g, lx, y, Component.translatable("ankinbt.side.count").getString(), String.valueOf(VersionCompat.get().compoundGetInt(fullItemTag, "count")));
            y += 12;
        }

        // Components info
        if (fullItemTag.contains("components")) {
            Tag comp = fullItemTag.get("components");
            if (comp instanceof CompoundTag ct) {
                g.fill(lx, y + 2, sideX + sideW - 8, y + 3, BORDER);
                y += 8;
                g.drawString(font, Component.translatable("ankinbt.side.components"), lx, y, C2, false);
                y += 12;
                sideInfo(g, lx, y, Component.translatable("ankinbt.side.tags").getString(), String.valueOf(ct.size()));
                y += 12;
            }
        }

        g.fill(lx, y + 2, sideX + sideW - 8, y + 3, BORDER);
        y += 8;
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
        if (statusMsg != null && System.currentTimeMillis() - statusTime < 3000) {
            g.drawString(font, statusMsg, px + SIDEBAR_W + 8, fy, statusColor, false);
        } else {
            statusMsg = null;
            g.drawString(font, Component.translatable("ankinbt.hint"), px + SIDEBAR_W + 8, fy, C3, false);
        }
        if (selIdx >= 0 && selIdx < visibleNodes.size()) {
            NbtTreeNode sel = visibleNodes.get(selIdx);
            String info = sel.getKey() + " : " + sel.getTypeName();
            g.drawString(font, info, px + pw - font.width(info) - 10, fy, C3, false);
        }
    }

    // ==================== INPUT ====================

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // Handle confirm close dialog
        if (confirmClose) {
            int dw = 260, dh = 110;
            int dx = (width - dw) / 2, dy = (height - dh) / 2;
            int by = dy + dh - 32;
            int bw2 = 70, bh2 = 22;

            int saveX = dx + 10;
            if (mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2) {
                saveToItem(); onClose(); return true;
            }
            int discardX = dx + dw / 2 - bw2 / 2;
            if (mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2) {
                dirty = false; onClose(); return true;
            }
            int cancelX = dx + dw - bw2 - 10;
            if (mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2) {
                confirmClose = false; return true;
            }
            return true;
        }

        for (Btn b : buttons) if (b.isHover((int) mx, (int) my)) { b.action.run(); return true; }

        if (hoverIdx >= 0 && hoverIdx < visibleNodes.size()) {
            long now = System.currentTimeMillis();
            if (hoverIdx == lastClickIdx && now - lastClickTime < 400) {
                NbtTreeNode node = visibleNodes.get(hoverIdx);
                if (!node.isLeaf()) { node.toggleExpanded(); refreshVisible(); }
                else openEditor(node);
                lastClickIdx = -1;
            } else {
                selIdx = hoverIdx;
                lastClickIdx = hoverIdx;
                lastClickTime = now;
            }
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scrollOff -= (int) sy * 3; clampScroll(); return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mod) {
        if (confirmClose) {
            if (key == 256) { confirmClose = false; return true; }
            return true;
        }
        if (searching) {
            if (key == 259 && !searchQ.isEmpty()) { searchQ = searchQ.substring(0, searchQ.length() - 1); refreshVisible(); return true; }
            if (key == 256) { searching = false; searchQ = ""; refreshVisible(); return true; }
        }
        if (key == 256) { tryClose(); return true; }
        if (key == 264 && selIdx < visibleNodes.size() - 1) { selIdx++; ensureVis(selIdx); return true; }
        if (key == 265 && selIdx > 0) { selIdx--; ensureVis(selIdx); return true; }

        if (!searching && selIdx >= 0 && selIdx < visibleNodes.size()) {
            NbtTreeNode node = visibleNodes.get(selIdx);
            if (key == 69) { if (!node.isLeaf()) { node.toggleExpanded(); refreshVisible(); } return true; }
            if (key == 257) { if (node.isLeaf()) openEditor(node); else { node.toggleExpanded(); refreshVisible(); } return true; }
            if (key == 261) { deleteNode(); return true; }
        }
        if (key == 83 && (mod & 2) != 0) { saveToItem(); return true; }
        return super.keyPressed(key, scan, mod);
    }

    @Override
    public boolean charTyped(char c, int mod) {
        if (searching) { searchQ += c; refreshVisible(); return true; }
        return super.charTyped(c, mod);
    }

    // ==================== ACTIONS ====================

    private void openEditor(NbtTreeNode node) {
        Minecraft.getInstance().setScreen(new ValueEditScreen(this, node));
    }

    private void deleteNode() {
        if (selIdx < 0 || selIdx >= visibleNodes.size()) return;
        NbtTreeNode node = visibleNodes.get(selIdx);
        NbtTreeNode parent = node.getParent();
        if (parent == null) return;
        parent.removeChild(node);
        dirty = true;
        refreshVisible();
        if (selIdx >= visibleNodes.size()) selIdx = visibleNodes.size() - 1;
        setStatus(Component.translatable("ankinbt.status.deleted").getString(), C2);
    }

    private void addTag() {
        NbtTreeNode target = (selIdx >= 0 && selIdx < visibleNodes.size()) ? visibleNodes.get(selIdx) : rootNode;
        // Only add to compound or list nodes
        if (!target.isCompound() && !target.isList()) target = target.getParent();
        if (target == null) target = rootNode;
        Minecraft.getInstance().setScreen(new AddTagScreen(this, target));
    }

    public void addTagToNode(NbtTreeNode parent, String key, Tag tag) {
        parent.addChild(key, tag, false);
        parent.setExpanded(true);
        dirty = true;
        refreshVisible();
        setStatus(Component.translatable("ankinbt.status.added", key).getString(), SUCCESS);
    }

    /**
     * Save: rebuild the CompoundTag from the tree, deserialize back to ItemStack,
     * and set it in the player's hand via creative mode packet.
     */
    private void saveToItem() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!mc.player.isCreative()) {
            setStatus(Component.translatable("ankinbt.status.creative_only").getString(), ERROR_C);
            return;
        }

        // Rebuild the full CompoundTag from the tree (like NBTEdit's tree.toCompound())
        CompoundTag rebuilt = rootNode.toCompoundTag();

        // Deserialize back to ItemStack via CODEC
        var opt = NbtHelper.deserializeItemStack(rebuilt);
        if (opt.isEmpty()) {
            setStatus(Component.translatable("ankinbt.status.save_error").getString(), ERROR_C);
            return;
        }

        ItemStack newStack = opt.get();
        int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
        mc.player.getInventory().setItem(slot, newStack.copy());
        mc.gameMode.handleCreativeModeItemAdd(newStack.copy(), 36 + slot);

        dirty = false;
        setStatus(Component.translatable("ankinbt.status.saved").getString(), SUCCESS);
    }

    private void switchToSimple() {
        Minecraft.getInstance().setScreen(new SimpleEditorScreen(originalStack));
    }

    private void exportNbt() {
        String itemId = originalStack.getItem().builtInRegistryHolder().key().location().getPath();
        long ts = System.currentTimeMillis() / 1000;
        String fileName = itemId + "_" + ts;
        CompoundTag rebuilt = rootNode.toCompoundTag();
        var path = NbtFileIO.exportNbt(rebuilt, fileName);
        if (path != null) {
            setStatus(Component.translatable("ankinbt.export.success").getString(), SUCCESS);
        } else {
            setStatus(Component.translatable("ankinbt.export.failed").getString(), ERROR_C);
        }
    }

    private void importNbt() {
        var files = NbtFileIO.listNbtFiles();
        if (files.isEmpty()) {
            setStatus(Component.translatable("ankinbt.import.no_files").getString(), ERROR_C);
            return;
        }
        // Import the most recent file
        var latest = files.get(0);
        CompoundTag tag = NbtFileIO.importNbt(latest.path());
        if (tag != null) {
            this.fullItemTag = tag;
            rebuildTree();
            dirty = true;
            setStatus(Component.translatable("ankinbt.import.success").getString() + " (" + latest.name() + ")", SUCCESS);
        } else {
            setStatus(Component.translatable("ankinbt.import.load_failed").getString(), ERROR_C);
        }
    }

    private void tryClose() {
        if (dirty && com.ankinbt.config.AnkiConfig.isConfirmOnClose()) { confirmClose = true; } else { onClose(); }
    }

    public void onNodeEdited() {
        dirty = true;
        refreshVisible();
        setStatus(Component.translatable("ankinbt.status.edited").getString(), C2);
    }

    private void setStatus(String msg, int color) {
        statusMsg = msg; statusColor = color; statusTime = System.currentTimeMillis();
    }

    // ==================== UTIL ====================

    private void ensureVis(int idx) {
        if (idx < scrollOff) scrollOff = idx;
        if (idx >= scrollOff + maxRows) scrollOff = idx - maxRows + 1;
        clampScroll();
    }

    private void clampScroll() {
        int max = Math.max(0, visibleNodes.size() - maxRows);
        scrollOff = Math.max(0, Math.min(scrollOff, max));
    }

    private void expandAll(NbtTreeNode n) { n.setExpanded(true); for (var c : n.getChildren()) expandAll(c); }
    private void collapseAll(NbtTreeNode n) { n.setExpanded(false); for (var c : n.getChildren()) collapseAll(c); }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c); g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c); g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override public boolean isPauseScreen() { return false; }

    public CompoundTag getFullItemTag() { return fullItemTag; }

    // ==================== BTN ====================

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
