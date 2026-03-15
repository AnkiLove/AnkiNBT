package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtFileIO;
import com.ankinbt.nbt.NbtTreeNode;
import com.ankinbt.util.UiSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.nio.file.Path;
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
    private final int inventorySlot;
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
    private float openAnim = 0f;

    public NbtEditorScreen(ItemStack stack) {
        this(stack, -1);
    }

    public NbtEditorScreen(ItemStack stack, int inventorySlot) {
        super(Component.translatable("ankinbt.title"));
        this.originalStack = stack;
        this.inventorySlot = inventorySlot;

        // Serialize the full ItemStack via CODEC (like NBTEdit)
        var opt = NbtHelper.serializeItemStack(stack);
        this.fullItemTag = opt.orElseGet(() -> {
            // Fallback: just use an empty compound with the item id
            CompoundTag fallback = new CompoundTag();
            fallback.putString("id", resolveItemId(stack));
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
        float cfgSpeed = AnkiConfig.getUiAnimationSpeed();
        float speed = Math.max(0.06f, Math.min(0.14f, cfgSpeed));
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);

        int scrim = UiTheme.scrim(AnkiConfig.getUiOpacity(), openAnim);
        int panel = fadeColor(BG, openAnim);
        int header = fadeColor(HEADER_BG, openAnim);
        int border = fadeColor(BORDER, openAnim);
        int sidebar = fadeColor(SIDEBAR_BG, openAnim);
        int hover = fadeColor(HOVER, openAnim);
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        int selected = fadeColor(UiTheme.withAlpha(accent & 0x00FFFFFF, 0x28), openAnim);
        int accentFade = fadeColor(accent, openAnim);
        int sbTrack = fadeColor(SB_TRACK, openAnim);
        int sbThumb = fadeColor(SB_THUMB, openAnim);

        g.fill(0, 0, width, height, scrim);

        // Panel
        g.fill(px, py, px + pw, py + ph, panel);
        drawBorder(g, px, py, pw, ph, border);

        // Header
        g.fill(px + 1, py + 1, px + pw - 1, py + HEADER_H, header);
        g.fill(px + 1, py + HEADER_H, px + pw - 1, py + HEADER_H + 1, border);
        g.drawString(font, Component.translatable("ankinbt.title"), px + 10, py + 12, C1, false);
        if (dirty) g.drawString(font, "*", px + 10 + font.width(Component.translatable("ankinbt.title")), py + 12, ERROR_C, false);

        for (Btn b : buttons) b.render(g, font, mx, my);

        // Sidebar
        renderSidebar(g, sidebar, border);
        g.fill(px + SIDEBAR_W + 1, py + HEADER_H + 1, px + SIDEBAR_W + 2, py + ph - FOOTER_H, border);

        // Search
        int atY = treeY, atH = treeH;
        if (searching) {
            g.fill(treeX, treeY, treeX + treeW, treeY + ROW_H, 0x40000000);
            drawBorder(g, treeX, treeY, treeW, ROW_H, accentFade);
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
            if (hovered) { hoverIdx = i; g.fill(treeX, ry, treeX + treeW, ry + ROW_H, hover); }
            if (i == selIdx) {
                g.fill(treeX, ry, treeX + treeW, ry + ROW_H, selected);
                g.fill(treeX, ry, treeX + 2, ry + ROW_H, accentFade);
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
            g.fill(sbx, atY, sbx + SCROLLBAR_W, atY + atH, sbTrack);
            float ratio = (float) maxRows / visibleNodes.size();
            int thumbH = Math.max(16, (int) (atH * ratio));
            float sr = (float) scrollOff / Math.max(1, visibleNodes.size() - maxRows);
            int thumbY = atY + (int) ((atH - thumbH) * sr);
            g.fill(sbx, thumbY, sbx + SCROLLBAR_W, thumbY + thumbH, sbThumb);
        }

        // Footer
        g.fill(px + 1, py + ph - FOOTER_H, px + pw - 1, py + ph - FOOTER_H + 1, border);
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
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(saveX, by, saveX + bw2, by + bh2, sh ? accent : UiTheme.withAlpha(accent & 0x00FFFFFF, 196));
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

    private void renderSidebar(GuiGraphics g, int sidebarBg, int border) {
        g.fill(sideX, sideY, sideX + sideW, sideY + sideH, sidebarBg);
        int y = sideY + 8, lx = sideX + 8;

        g.renderItem(originalStack, lx + (sideW - 32) / 2, y);
        y += 24;

        String name = originalStack.getHoverName().getString();
        if (font.width(name) > sideW - 16) name = font.plainSubstrByWidth(name, sideW - 22) + "...";
        g.drawString(font, name, lx, y, C1, false);
        y += 14;

        g.fill(lx, y, sideX + sideW - 8, y + 1, border);
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
                g.fill(lx, y + 2, sideX + sideW - 8, y + 3, border);
                y += 8;
                g.drawString(font, Component.translatable("ankinbt.side.components"), lx, y, C2, false);
                y += 12;
                sideInfo(g, lx, y, Component.translatable("ankinbt.side.tags").getString(), String.valueOf(ct.size()));
                y += 12;
            }
        }

        g.fill(lx, y + 2, sideX + sideW - 8, y + 3, border);
        y += 8;
        sideInfo(g, lx, y, Component.translatable("ankinbt.side.visible").getString(), String.valueOf(visibleNodes.size()));
    }

    private int fadeColor(int color, float factor) {
        int alpha = (color >>> 24) & 0xFF;
        return UiTheme.withAlpha(color & 0x00FFFFFF, Math.round(alpha * factor));
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

    public boolean mouseClicked(double mx, double my, int btn) {
        // Handle confirm close dialog
        if (confirmClose) {
            int dw = 260, dh = 110;
            int dx = (width - dw) / 2, dy = (height - dh) / 2;
            int by = dy + dh - 32;
            int bw2 = 70, bh2 = 22;

            int saveX = dx + 10;
            if (mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2) {
                UiSound.playClick();
                saveToItem(); onClose(); return true;
            }
            int discardX = dx + dw / 2 - bw2 / 2;
            if (mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2) {
                UiSound.playClick();
                dirty = false; onClose(); return true;
            }
            int cancelX = dx + dw - bw2 - 10;
            if (mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2) {
                UiSound.playClick();
                confirmClose = false; return true;
            }
            return true;
        }

        for (Btn b : buttons) if (b.isHover((int) mx, (int) my)) { UiSound.playClick(); b.action.run(); return true; }

        if (hoverIdx >= 0 && hoverIdx < visibleNodes.size()) {
            long now = System.currentTimeMillis();
            if (hoverIdx == lastClickIdx && now - lastClickTime < 400) {
                NbtTreeNode node = visibleNodes.get(hoverIdx);
                UiSound.playClick();
                if (!node.isLeaf()) { node.toggleExpanded(); refreshVisible(); }
                else openEditor(node);
                lastClickIdx = -1;
            } else {
                UiSound.playClick();
                selIdx = hoverIdx;
                lastClickIdx = hoverIdx;
                lastClickTime = now;
            }
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scrollOff -= (int) sy * 3; clampScroll(); return true;
    }

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
        return false;
    }

    public boolean charTyped(char c, int mod) {
        if (searching) { searchQ += c; refreshVisible(); return true; }
        return false;
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
        if (inventorySlot >= 0) {
            mc.player.getInventory().setItem(inventorySlot, newStack.copy());
            int packetSlot = inventorySlot < 9 ? 36 + inventorySlot : inventorySlot;
            mc.gameMode.handleCreativeModeItemAdd(newStack.copy(), packetSlot);
        } else {
            int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
            mc.player.getInventory().setItem(slot, newStack.copy());
            mc.gameMode.handleCreativeModeItemAdd(newStack.copy(), 36 + slot);
        }

        dirty = false;
        setStatus(Component.translatable("ankinbt.status.saved").getString(), SUCCESS);
    }

    private void switchToSimple() {
        AnkiConfig.setPreferredItemEditor("simple");
        Minecraft.getInstance().setScreen(new SimpleEditorScreen(originalStack, inventorySlot));
    }

    private void exportNbt() {
        String itemId = resolveItemPath(originalStack);
        long ts = System.currentTimeMillis() / 1000;
        String fileName = itemId + "_" + ts;
        CompoundTag rebuilt = rootNode.toCompoundTag();
        Path path;
        if (hasTinyFd()) {
            String picked = tinyFdSavePath(AnkiConfig.getExportPath().resolve(fileName + ".nbt").toString());
            if (picked == null || picked.isBlank()) return;
            path = NbtFileIO.exportNbtToPath(rebuilt, Path.of(picked));
        } else {
            path = NbtFileIO.exportNbt(rebuilt, fileName);
        }
        if (path != null) {
            setStatus(Component.translatable("ankinbt.export.success").getString(), SUCCESS);
        } else {
            setStatus(Component.translatable("ankinbt.export.failed").getString(), ERROR_C);
        }
    }

    private String resolveItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "minecraft:air";
        try {
            Object holder = stack.getItem().builtInRegistryHolder();
            Object key = holder.getClass().getMethod("key").invoke(holder);
            try {
                Object loc = key.getClass().getMethod("location").invoke(key);
                if (loc != null) return loc.toString();
            } catch (Throwable ignored) {}
            try {
                Object id = key.getClass().getMethod("identifier").invoke(key);
                if (id != null) return id.toString();
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return stack.getItem().toString();
    }

    private String resolveItemPath(ItemStack stack) {
        String id = resolveItemId(stack);
        int idx = id.indexOf(':');
        return idx >= 0 && idx + 1 < id.length() ? id.substring(idx + 1) : id;
    }

    private void importNbt() {
        CompoundTag tag;
        String loadedName;
        if (hasTinyFd()) {
            String picked = tinyFdOpenPath(AnkiConfig.getExportPath().toString());
            if (picked == null || picked.isBlank()) return;
            tag = NbtFileIO.importNbt(Path.of(picked));
            loadedName = Path.of(picked).getFileName().toString();
        } else {
            var files = NbtFileIO.listNbtFiles();
            if (files.isEmpty()) {
                setStatus(Component.translatable("ankinbt.import.no_files").getString(), ERROR_C);
                return;
            }
            var latest = files.get(0);
            tag = NbtFileIO.importNbt(latest.path());
            loadedName = latest.name();
        }
        if (tag != null) {
            this.fullItemTag = tag;
            rebuildTree();
            dirty = true;
            setStatus(Component.translatable("ankinbt.import.success").getString() + " (" + loadedName + ")", SUCCESS);
        } else {
            setStatus(Component.translatable("ankinbt.import.load_failed").getString(), ERROR_C);
        }
    }

    private boolean hasTinyFd() {
        try {
            Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private String tinyFdSavePath(String defaultPath) {
        return tinyFdDialog("tinyfd_saveFileDialog", defaultPath, false);
    }

    private String tinyFdOpenPath(String defaultPath) {
        return tinyFdDialog("tinyfd_openFileDialog", defaultPath, true);
    }

    private String tinyFdDialog(String methodName, String defaultPath, boolean isOpen) {
        try {
            Class<?> clazz = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equals(methodName)) continue;
                Object out = m.invoke(null, tinyFdArgs(m.getParameterTypes(), defaultPath, isOpen));
                if (out instanceof CharSequence cs) return cs.toString();
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private Object[] tinyFdArgs(Class<?>[] parameterTypes, String defaultPath, boolean isOpen) {
        Object[] args = new Object[parameterTypes.length];
        int stringIndex = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> pt = parameterTypes[i];
            if (CharSequence.class.isAssignableFrom(pt) || pt == String.class) {
                if (stringIndex == 0) {
                    args[i] = isOpen
                            ? Component.translatable("ankinbt.simple.import_nbt").getString()
                            : Component.translatable("ankinbt.simple.export_nbt").getString();
                } else if (stringIndex == 1) {
                    args[i] = defaultPath;
                } else {
                    args[i] = "NBT files (*.nbt)";
                }
                stringIndex++;
            } else if (pt == String[].class) {
                args[i] = new String[] { "*.nbt" };
            } else if (pt == boolean.class || pt == Boolean.class) {
                args[i] = false;
            } else if (pt == int.class || pt == Integer.class) {
                args[i] = 1;
            } else if (pt.getName().equals("org.lwjgl.PointerBuffer")) {
                args[i] = null;
            } else {
                args[i] = null;
            }
        }
        return args;
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
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x();
        double my = event.y();
        if (mouseClicked(mx, my, event.button())) return true;
        if (minecraft != null) {
            double sw = minecraft.getWindow().getScreenWidth();
            double sh = minecraft.getWindow().getScreenHeight();
            if (sw > 0.0 && sh > 0.0) {
                double sx = mx * width / sw;
                double sy = my * height / sh;
                if ((Math.abs(sx - mx) > 0.5 || Math.abs(sy - my) > 0.5) && mouseClicked(sx, sy, event.button())) {
                    return true;
                }
            }
        }
        return super.mouseClicked(event, isDoubleClick);
    }

}
