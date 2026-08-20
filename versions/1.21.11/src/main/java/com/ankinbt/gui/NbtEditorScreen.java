package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtFileIO;
import com.ankinbt.nbt.NbtTreeNode;
import com.ankinbt.util.FlatEditBox;
import com.ankinbt.util.UiSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

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
    private static final Identifier LOGO_TEXTURE = Identifier.fromNamespaceAndPath("ankinbt", "textures/gui/editor-logo.png");

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
    private static final int HOVER = 0x30FFFFFF;
    private static final int SB_TRACK = 0x30FFFFFF;
    private static final int SB_THUMB = 0x70FFFFFF;
    private static final int BTN_BG = 0x30FFFFFF;
    private static final int BTN_HOVER = 0x50FFFFFF;
    private static final int SUCCESS = 0xFF22C55E;
    private static final int ERROR_C = 0xFFEF4444;

    private ItemStack originalStack;
    private int inventorySlot;
    private final AbstractContainerScreen<?> inventoryParent;
    private EditorDock.Bounds dockBounds;
    private EditorDock.Bounds barBounds;
    private EditorDock.Bounds drawerBounds;
    private boolean drawerAbove;
    private boolean drawerOpen = true;
    private float drawerAnim = 0f;
    private float activeIndicatorX = -1f;
    private final float[] navHoverAnim = new float[9];
    private final float[] toolHoverAnim = new float[2];
    private boolean draggingMenuBar;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean resizingEditor;
    private boolean editorSizeFocused;
    private float editorScale = EditorDock.DEFAULT_EDITOR_SCALE;
    private float editorWidthAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
    private float editorHeightAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
    private float sizeControlHoverAnim;
    private CompoundTag fullItemTag; // Full item serialized via ItemStack.CODEC
    private NbtTreeNode rootNode;
    private List<NbtTreeNode> visibleNodes = new ArrayList<>();
    private ItemStack previewStack = ItemStack.EMPTY;
    private boolean previewDirty = true;

    private int scrollOff = 0, maxRows;
    private int selIdx = -1, hoverIdx = -1;
    private int lastClickIdx = -1;
    private long lastClickTime = 0;

    private int px, py, pw, ph;
    private int sideX, sideY, sideW, sideH;
    private int treeX, treeY, treeW, treeH;

    private EditBox searchBox;
    private boolean searching = false;
    private final List<Btn> buttons = new ArrayList<>();

    private String statusMsg = null;
    private long statusTime = 0;
    private int statusColor = UiTheme.textDim();
    private boolean dirty = false;
    private boolean nativeDialogOpen = false;
    private long lastNativeDialogAt = 0L;
    private boolean confirmClose = false;
    private float confirmAnim = 0f;
    private final float[] confirmHover = new float[3];
    private float openAnim = 0f;
    private float brandAnim = 0f;
    private float settingsHoverAnim = 0f;

    public NbtEditorScreen(ItemStack stack) {
        this(stack, -1, null);
    }

    public NbtEditorScreen(ItemStack stack, int inventorySlot) {
        this(stack, inventorySlot, null);
    }

    public NbtEditorScreen(ItemStack stack, int inventorySlot, AbstractContainerScreen<?> inventoryParent) {
        super(Component.translatable("ankinbt.title"));
        this.inventorySlot = inventorySlot;
        this.inventoryParent = inventoryParent;
        loadItem(stack);
    }

    private void loadItem(ItemStack stack) {
        this.originalStack = stack.copy();
        this.previewStack = stack.copy();
        this.previewDirty = true;
        var opt = NbtHelper.serializeItemStack(stack);
        this.fullItemTag = opt.orElseGet(() -> {
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
        previewDirty = true;
        refreshVisible();
    }

    private void refreshVisible() {
        visibleNodes.clear();
        if (rootNode != null) rootNode.collectVisible(visibleNodes);
        String search = searchValue();
        if (searching && !search.isEmpty()) {
            String q = search.toLowerCase();
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
        editorScale = AnkiConfig.getItemEditorScale();
        editorWidthAdjustment = AnkiConfig.getItemEditorWidthAdjustment();
        editorHeightAdjustment = AnkiConfig.getItemEditorHeightAdjustment();
        applyMenuLayout(EditorDock.menuLayout(width, height, 310, inventoryParent != null,
                editorScale, editorWidthAdjustment, editorHeightAdjustment));
        initSearchBox();
        buttons.clear();
    }

    private void applyMenuLayout(EditorDock.MenuLayout layout) {
        barBounds = layout.bar();
        drawerBounds = layout.drawer();
        drawerAbove = layout.drawerAbove();
        dockBounds = drawerBounds;
        pw = drawerBounds.width();
        ph = drawerBounds.height();
        px = drawerBounds.x();
        py = drawerBounds.y();

        treeX = px + 8;
        treeY = py + 34;
        treeW = Math.max(1, pw - 16 - SCROLLBAR_W);
        treeH = Math.max(1, ph - 34 - FOOTER_H);
        maxRows = Math.max(1, treeH / ROW_H);
        clampScroll();
    }

    private void initSearchBox() {
        String value = searchValue();
        searchBox = new FlatEditBox(font, treeX, treeY, Math.max(1, treeW), ROW_H,
                Component.translatable("ankinbt.search.hint"));
        searchBox.setMaxLength(256);
        searchBox.setHint(Component.translatable("ankinbt.search.hint"));
        searchBox.setValue(value);
        searchBox.setResponder(v -> refreshVisible());
        searchBox.setFocused(searching);
    }

    private String searchValue() {
        return searchBox == null ? "" : searchBox.getValue();
    }

    private void setSearching(boolean value) {
        searching = value;
        if (searchBox != null) {
            if (!searching) searchBox.setValue("");
            searchBox.setFocused(searching);
        }
        refreshVisible();
    }

    private void layoutSearchBox(int accent) {
        if (searchBox == null) initSearchBox();
        searchBox.setX(treeX);
        searchBox.setY(treeY);
        searchBox.setWidth(Math.max(1, treeW));
        searchBox.setTextColor(UiTheme.textMain());
        searchBox.setTextColorUneditable(UiTheme.textDim());
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
            setSearching(!searching);
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
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.06f, Math.min(0.14f, cfgSpeed)) : 1.0f;
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);
        float motionSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.10f, cfgSpeed * 1.7f) : 1f;
        drawerAnim = UiTheme.approach(drawerAnim, drawerOpen ? 1f : 0f, motionSpeed);
        if (inventoryParent == null) {
            brandAnim = EditorBrandLayer.approachOpen(brandAnim);
            boolean settingsHovered = EditorBrandLayer.isSettingsButton(mx, my, width);
            settingsHoverAnim = EditorBrandLayer.approachSettingsHover(settingsHoverAnim, settingsHovered);
            EditorBrandLayer.renderBackgroundLogo(g, width, height);
            g.fill(0, 0, width, height, UiTheme.scrim(AnkiConfig.getUiOpacity(), openAnim));
        }
        renderDrawer(g, mx, my);
        renderMenuBar(g, mx, my);
        if (!confirmClose && confirmAnim <= 0.01f) {
            sizeControlHoverAnim = EditorDock.renderSizeControl(g, font, width, height, mx, my,
                    editorScale, sizeControlHoverAnim, resizingEditor || editorSizeFocused,
                    UiTheme.accent(AnkiConfig.getUiAccentPreset()));
        }
        if (confirmClose || confirmAnim > 0.01f) renderConfirmClose(g, mx, my);
        if (inventoryParent == null) {
            EditorBrandLayer.renderItemStatus(g, font, width, height, brandAnim,
                    InventoryEditorOverlay.itemEditorStatusMode(
                            Component.translatable("ankinbt.config.mode.advanced").getString()), editorScale);
            EditorBrandLayer.renderSettingsButton(g, font, width, mx, my, settingsHoverAnim);
        }
    }

    private void renderMenuBar(GuiGraphics g, int mx, int my) {
        int bx = barBounds.x(), by = barBounds.y(), bw = barBounds.width(), bh = barBounds.height();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(bx, by, bx + bw, by + bh, UiTheme.toolbar(AnkiConfig.getUiOpacity(), openAnim));
        drawBorder(g, bx, by, bw, bh, UiTheme.themedBorder(AnkiConfig.getUiOpacity(), openAnim));
        g.fill(bx, by + bh - 1, bx + bw, by + bh, fadeColor(accent, openAnim));

        int brandW = Math.min(72, Math.max(58, bw / 7));
        Component moveIcon = UiIcons.component(UiIcons.MOVE);
        boolean moveHover = mx >= bx && mx < bx + brandW && my >= by && my < by + bh;
        g.drawString(font, moveIcon, bx + (brandW - font.width(moveIcon)) / 2, by + 10,
                moveHover ? UiTheme.textMain() : UiTheme.textDim(), false);
        if (moveHover && !draggingMenuBar) {
            VersionCompat.get().renderTooltip(g, font, Component.translatable("ankinbt.ui.drag_editor"), mx, my);
        }
        if (dirty) g.fill(bx + brandW - 6, by + 7, bx + brandW - 3, by + 10, ERROR_C);

        int toolW = 26;
        int toolsStart = bx + bw - toolW * 2;
        int navStart = bx + brandW;
        int navW = Math.max(18, (toolsStart - navStart) / navHoverAnim.length);
        String[] icons = {
                UiIcons.BOX, UiIcons.CODE, UiIcons.SEARCH, UiIcons.TAG,
                UiIcons.CHEVRON_DOWN, UiIcons.CHEVRON_UP, UiIcons.SAVE, UiIcons.COPY, UiIcons.BOOK
        };
        Component[] tips = {
                Component.translatable("ankinbt.btn.switch_simple"), Component.translatable("ankinbt.config.mode.advanced"),
                Component.translatable("ankinbt.btn.search"), Component.translatable("ankinbt.btn.add"),
                Component.translatable("ankinbt.btn.expand"), Component.translatable("ankinbt.btn.collapse"),
                Component.translatable("ankinbt.export.save_tip"), Component.translatable("ankinbt.export.save_as_tip"),
                Component.translatable("ankinbt.simple.import_nbt")
        };
        float hoverSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.18f, AnkiConfig.getUiAnimationSpeed() * 2.4f) : 1f;
        for (int i = 0; i < navHoverAnim.length; i++) {
            int nx = navStart + i * navW;
            boolean hover = mx >= nx && mx < nx + navW && my >= by && my < by + bh;
            boolean active = i == 1;
            navHoverAnim[i] = UiTheme.approach(navHoverAnim[i], hover ? 1f : 0f, hoverSpeed);
            if ("segmented".equals(AnkiConfig.getUiNavigationStyle()) && active) {
                g.fill(nx + 2, by + 3, nx + navW - 2, by + bh - 3,
                        UiTheme.withAlpha(accent & 0x00FFFFFF, 86));
            } else {
                g.fill(nx + 1, by + 2, nx + navW - 1, by + bh - 2,
                        UiTheme.mix(0x00000000, HOVER, navHoverAnim[i]));
            }
            if ("compact".equals(AnkiConfig.getUiNavigationStyle()) && active) {
                g.fill(nx + 3, by + 7, nx + 5, by + bh - 7, accent);
            }
            Component icon = UiIcons.component(icons[i]);
            g.drawString(font, icon, nx + (navW - font.width(icon)) / 2, by + 10,
                    i == 1 ? UiTheme.textMain() : (hover ? UiTheme.textMain() : UiTheme.textDim()), false);
            if (hover) VersionCompat.get().renderTooltip(g, font, tips[i], mx, my);
        }
        int treeX = navStart + navW;
        if (activeIndicatorX < 0f) activeIndicatorX = treeX;
        activeIndicatorX = UiTheme.approach(activeIndicatorX, treeX, hoverSpeed);
        if ("underline".equals(AnkiConfig.getUiNavigationStyle())) {
            g.fill(Math.round(activeIndicatorX) + 5, by + bh - 3,
                    Math.round(activeIndicatorX) + navW - 5, by + bh - 1, accent);
        }

        renderTool(g, mx, my, toolsStart, by, toolW, 0, UiIcons.SAVE,
                Component.translatable(InventoryEditorOverlay.isItemEditorPreviewMode()
                        ? "ankinbt.btn.save.preview_tip" : "ankinbt.btn.save.tip"));
        renderTool(g, mx, my, toolsStart + toolW, by, toolW, 1, UiIcons.CLOSE,
                Component.translatable("ankinbt.btn.close"));
    }

    private void renderTool(GuiGraphics g, int mx, int my, int x, int y, int width, int index,
                            String iconGlyph, Component tooltip) {
        boolean hover = mx >= x && mx < x + width && my >= y && my < y + EditorDock.MENU_BAR_HEIGHT;
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.18f, AnkiConfig.getUiAnimationSpeed() * 2.4f) : 1f;
        toolHoverAnim[index] = UiTheme.approach(toolHoverAnim[index], hover ? 1f : 0f, speed);
        g.fill(x, y + 2, x + width, y + EditorDock.MENU_BAR_HEIGHT - 2,
                UiTheme.mix(0x00000000, HOVER, toolHoverAnim[index]));
        Component icon = UiIcons.component(iconGlyph);
        g.drawString(font, icon, x + (width - font.width(icon)) / 2, y + 10, hover ? UiTheme.textMain() : UiTheme.textDim(), false);
        if (hover) VersionCompat.get().renderTooltip(g, font, tooltip, mx, my);
    }

    private void renderDrawer(GuiGraphics g, int mx, int my) {
        if (drawerAnim <= 0.01f) return;
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        int reveal = Math.max(1, Math.round(ph * drawerAnim));
        int clipTop = drawerAbove ? py + ph - reveal : py;
        int clipBottom = drawerAbove ? py + ph : py + reveal;
        g.enableScissor(px, clipTop, px + pw, clipBottom);
        g.fill(px, py, px + pw, py + ph, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
        drawBorder(g, px, py, pw, ph, UiTheme.themedBorder(AnkiConfig.getUiOpacity(), openAnim));
        ItemStack previewStack = currentPreviewStack();
        g.renderItem(previewStack, px + 9, py + 8);
        String name = previewStack.getHoverName().getString();
        int nameBudget = Math.max(60, pw - 160);
        if (font.width(name) > nameBudget) name = font.plainSubstrByWidth(name, nameBudget - 10) + "..";
        g.drawString(font, name, px + 31, py + 9, UiTheme.textMain(), false);
        String mode = Component.translatable("ankinbt.config.mode.advanced").getString();
        g.drawString(font, mode, px + pw - font.width(mode) - 10, py + 9, accent, false);
        g.fill(px + 8, py + 29, px + pw - 8, py + 30, UiTheme.themedBorder(1f, 1f));

        int atY = treeY, atH = treeH;
        if (searching) {
            layoutSearchBox(accent);
            searchBox.setFocused(true);
            searchBox.renderWidget(g, mx, my, 0f);
            atY += ROW_H + 2; atH -= ROW_H + 2;
            maxRows = atH / ROW_H;
        } else {
            maxRows = treeH / ROW_H;
        }

        // Tree
        hoverIdx = -1;
        int hover = fadeColor(HOVER, openAnim);
        int selected = fadeColor(UiTheme.withAlpha(accent & 0x00FFFFFF, 0x28), openAnim);
        int end = Math.min(scrollOff + maxRows, visibleNodes.size());
        for (int i = scrollOff; i < end; i++) {
            int ry = atY + (i - scrollOff) * ROW_H;
            NbtTreeNode node = visibleNodes.get(i);
            boolean hovered = mx >= treeX && mx < treeX + treeW && my >= ry && my < ry + ROW_H;
            if (hovered) { hoverIdx = i; g.fill(treeX, ry, treeX + treeW, ry + ROW_H, hover); }
            if (i == selIdx) {
                g.fill(treeX, ry, treeX + treeW, ry + ROW_H, selected);
                g.fill(treeX, ry, treeX + 2, ry + ROW_H, accent);
            }

            int indent = node.getDepth() * INDENT;
            int tx = treeX + 6 + indent;

            if (!node.isLeaf()) {
                g.drawString(font, node.isExpanded() ? "v" : ">", tx, ry + 5, UiTheme.textDim(), false);
                tx += 10;
            }

            int tc = NbtHelper.getTagColor(node.getTag());
            String badge = node.getTypeName();
            if (badge.length() > 3) badge = badge.substring(0, 3);
            g.drawString(font, badge, tx, ry + 5, tc, false);
            tx += font.width(badge) + 4;

            String key = node.getKey();
            if (!key.isEmpty()) {
                g.drawString(font, key, tx, ry + 5, UiTheme.textMain(), false);
                tx += font.width(key) + 6;
            }

            String val = node.getDisplayValue();
            if (val.length() > 36) val = val.substring(0, 33) + "...";
            g.drawString(font, val, tx, ry + 5, UiTheme.textDim(), false);
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
        g.fill(px + 1, py + ph - FOOTER_H, px + pw - 1, py + ph - FOOTER_H + 1, UiTheme.themedBorder(1f, 1f));
        renderFooter(g);
        g.disableScissor();
        if (mx >= px + 9 && mx < px + 25 && my >= py + 8 && my < py + 24) {
            VersionCompat.get().renderItemTooltip(g, font, previewStack, mx, my);
        }
    }

    private ItemStack currentPreviewStack() {
        if (rootNode == null) return originalStack;
        if (previewDirty) {
            previewStack = NbtHelper.deserializeItemStack(rootNode.toCompoundTag()).orElse(originalStack).copy();
            previewDirty = false;
        }
        return previewStack;
    }

    private void renderConfirmClose(GuiGraphics g, int mx, int my) {
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.12f, AnkiConfig.getUiAnimationSpeed() * 1.8f) : 1f;
        confirmAnim = UiTheme.approach(confirmAnim, confirmClose ? 1f : 0f, speed);
        int scrimAlpha = Math.round(112 * confirmAnim);
        g.fill(0, 0, width, height, UiTheme.withAlpha(0x000000, scrimAlpha));
        int dw = Math.min(320, width - 32), dh = 126;
        int dx = (width - dw) / 2, dy = confirmDialogY(dh);
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(Math.max(0.72f, AnkiConfig.getUiOpacity()), confirmAnim));
        drawBorder(g, dx, dy, dw, dh, UiTheme.withAlpha(accent & 0x00FFFFFF, Math.round(230 * confirmAnim)));

        String title = Component.translatable("ankinbt.confirm.title").getString();
        g.drawString(font, title, dx + 10, dy + 10, UiTheme.textMain(), false);
        g.fill(dx + 1, dy + 25, dx + dw - 1, dy + 27, accent);
        g.drawString(font, Component.translatable("ankinbt.confirm.unsaved").getString(), dx + 12, dy + 38, UiTheme.textMain(), false);
        g.drawString(font, Component.translatable("ankinbt.confirm.discard_hint").getString(), dx + 12, dy + 53, UiTheme.textDim(), false);

        int by = dy + dh - 34;
        int gap = 7;
        int bw2 = Math.max(72, (dw - 24 - gap * 2) / 3), bh2 = 24;

        int saveX = dx + 12;
        boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
        confirmHover[0] = UiTheme.approach(confirmHover[0], sh ? 1f : 0f, speed);
        g.fill(saveX, by, saveX + bw2, by + bh2,
                UiTheme.mix(UiTheme.withAlpha(accent & 0x00FFFFFF, 164), accent, confirmHover[0]));
        String saveLabel = Component.translatable("ankinbt.confirm.save_close").getString();
        g.drawString(font, saveLabel, saveX + (bw2 - font.width(saveLabel)) / 2, by + 7, UiTheme.textMain(), false);

        int discardX = saveX + bw2 + gap;
        boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
        confirmHover[1] = UiTheme.approach(confirmHover[1], dh2 ? 1f : 0f, speed);
        g.fill(discardX, by, discardX + bw2, by + bh2,
                UiTheme.mix(0x35EF4444, 0x88EF4444, confirmHover[1]));
        String discardLabel = Component.translatable("ankinbt.confirm.discard").getString();
        g.drawString(font, discardLabel, discardX + (bw2 - font.width(discardLabel)) / 2, by + 7, UiTheme.textMain(), false);

        int cancelX = discardX + bw2 + gap;
        boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
        confirmHover[2] = UiTheme.approach(confirmHover[2], ch ? 1f : 0f, speed);
        g.fill(cancelX, by, cancelX + bw2, by + bh2, UiTheme.mix(0x24FFFFFF, 0x58FFFFFF, confirmHover[2]));
        String cancelLabel = Component.translatable("ankinbt.edit.cancel").getString();
        g.drawString(font, cancelLabel, cancelX + (bw2 - font.width(cancelLabel)) / 2, by + 7, UiTheme.textDim(), false);
    }

    private int confirmDialogY(int dialogHeight) {
        return (height - dialogHeight) / 2 + Math.round((1f - confirmAnim) * 14f);
    }

    private void renderSidebar(GuiGraphics g, int sidebarBg, int border) {
        g.fill(sideX, sideY, sideX + sideW, sideY + sideH, sidebarBg);
        int y = sideY + 8, lx = sideX + 8;

        g.renderItem(originalStack, lx + (sideW - 32) / 2, y);
        y += 24;

        String name = originalStack.getHoverName().getString();
        if (font.width(name) > sideW - 16) name = font.plainSubstrByWidth(name, sideW - 22) + "...";
        g.drawString(font, name, lx, y, UiTheme.textMain(), false);
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
                g.drawString(font, Component.translatable("ankinbt.side.components"), lx, y, UiTheme.textDim(), false);
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
        g.drawString(font, label, x, y, UiTheme.textDim(), false);
        int maxW = sideW - 16 - font.width(label) - 4;
        if (font.width(value) > maxW) value = font.plainSubstrByWidth(value, maxW - 8) + "..";
        g.drawString(font, value, x + font.width(label) + 4, y, UiTheme.textDim(), false);
    }

    private void renderFooter(GuiGraphics g) {
        int fy = py + ph - FOOTER_H + 5;
        if (statusMsg != null && System.currentTimeMillis() - statusTime < 3000) {
            g.drawString(font, statusMsg, px + 9, fy, statusColor, false);
        } else {
            statusMsg = null;
            g.drawString(font, Component.translatable("ankinbt.hint"), px + 9, fy, UiTheme.textDim(), false);
        }
        if (selIdx >= 0 && selIdx < visibleNodes.size()) {
            NbtTreeNode sel = visibleNodes.get(selIdx);
            String info = sel.getKey() + " : " + sel.getTypeName();
            g.drawString(font, info, px + pw - font.width(info) - 10, fy, UiTheme.textDim(), false);
        }
    }

    // ==================== INPUT ====================

    private boolean handleMenuBarClick(double mx, double my) {
        if (barBounds == null || !barBounds.contains(mx, my)) return false;
        int brandW = Math.min(72, Math.max(58, barBounds.width() / 7));
        int toolW = 26;
        int toolsStart = barBounds.x() + barBounds.width() - toolW * 2;
        int navStart = barBounds.x() + brandW;
        int navW = Math.max(18, (toolsStart - navStart) / navHoverAnim.length);
        int index = (int) ((mx - navStart) / navW);
        if (mx >= navStart && mx < navStart + navW * navHoverAnim.length && index >= 0 && index < navHoverAnim.length) {
            UiSound.playClick();
            switch (index) {
                case 0 -> switchToSimple();
                case 1 -> drawerOpen = !drawerOpen;
                case 2 -> {
                    setSearching(!searching);
                    drawerOpen = true;
                }
                case 3 -> {
                    drawerOpen = true;
                    addTag();
                }
                case 4 -> {
                    expandAll(rootNode);
                    drawerOpen = true;
                    refreshVisible();
                }
                case 5 -> {
                    collapseAll(rootNode);
                    rootNode.setExpanded(true);
                    drawerOpen = true;
                    refreshVisible();
                }
                case 6 -> exportNbt();
                case 7 -> exportNbtAs();
                case 8 -> importNbt();
                default -> { }
            }
            return true;
        }
        if (mx >= toolsStart && mx < toolsStart + toolW) {
            UiSound.playClick();
            saveToItem();
            return true;
        }
        if (mx >= toolsStart + toolW) {
            UiSound.playClick();
            tryClose();
            return true;
        }
        return true;
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        // Handle confirm close dialog
        if (confirmClose || confirmAnim > 0.01f) {
            if (!confirmClose || btn != 0) return true;
            int dw = Math.min(320, width - 32), dh = 126;
            int dx = (width - dw) / 2, dy = confirmDialogY(dh);
            int by = dy + dh - 34;
            int gap = 7;
            int bw2 = Math.max(72, (dw - 24 - gap * 2) / 3), bh2 = 24;

            int saveX = dx + 12;
            if (mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2) {
                UiSound.playClick();
                saveToItem();
                if (!dirty) onClose();
                return true;
            }
            int discardX = saveX + bw2 + gap;
            if (mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2) {
                UiSound.playClick();
                dirty = false; onClose(); return true;
            }
            int cancelX = discardX + bw2 + gap;
            if (mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2) {
                UiSound.playClick();
                confirmClose = false; return true;
            }
            return true;
        }

        if (inventoryParent == null && btn == 0 && EditorBrandLayer.isSettingsButton(mx, my, width)) {
            UiSound.playClick();
            openChildScreen(new AnkiConfigScreen(this));
            return true;
        }
        if (btn == 0) editorSizeFocused = false;
        if (btn == 0 || btn == 1) {
            EditorDock.SizeControl sizeControl = EditorDock.sizeControl(width, height, editorScale);
            if (btn == 0 && sizeControl.reset().contains(mx, my)) {
                resetEditorSize();
                UiSound.playClick();
                return true;
            }
            if (sizeControl.horizontal().contains(mx, my)) {
                adjustEditorAxes(btn == 1 ? EditorDock.AXIS_ADJUSTMENT_STEP
                        : -EditorDock.AXIS_ADJUSTMENT_STEP, 0.0f);
                UiSound.playClick();
                return true;
            }
            if (sizeControl.vertical().contains(mx, my)) {
                adjustEditorAxes(0.0f, btn == 1 ? EditorDock.AXIS_ADJUSTMENT_STEP
                        : -EditorDock.AXIS_ADJUSTMENT_STEP);
                UiSound.playClick();
                return true;
            }
            if (btn == 0 && sizeControl.hit().contains(mx, my)) {
                resizingEditor = true;
                editorSizeFocused = true;
                draggingMenuBar = false;
                updateEditorScale(mx);
                return true;
            }
        }

        if (searching && drawerBounds != null && drawerBounds.contains(mx, my)
                && mx >= treeX && mx < treeX + treeW && my >= treeY && my < treeY + ROW_H) {
            layoutSearchBox(UiTheme.accent(AnkiConfig.getUiAccentPreset()));
            searchBox.mouseClicked(new MouseButtonEvent(mx, my,
                    new net.minecraft.client.input.MouseButtonInfo(btn, 0)), false);
            searchBox.setFocused(true);
            this.setFocused(searchBox);
            return true;
        }

        if (barBounds != null && barBounds.contains(mx, my)) {
            int brandW = Math.min(72, Math.max(58, barBounds.width() / 7));
            if (btn == 0 && mx < barBounds.x() + brandW) {
                draggingMenuBar = true;
                dragOffsetX = (int) Math.round(mx) - barBounds.x();
                dragOffsetY = (int) Math.round(my) - barBounds.y();
                return true;
            }
        }
        if (handleMenuBarClick(mx, my)) return true;
        if (!drawerOpen || drawerAnim < 0.2f || drawerBounds == null || !drawerBounds.contains(mx, my)) return false;

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
        if (confirmClose || confirmAnim > 0.01f) return true;
        if (!drawerOpen || drawerAnim < 0.2f) return false;
        scrollOff -= (int) sy * 3; clampScroll(); return true;
    }

    public boolean keyPressed(int key, int scan, int mod) {
        if (confirmClose || confirmAnim > 0.01f) {
            if (key == 256) { confirmClose = false; return true; }
            return true;
        }
        if (editorSizeFocused) {
            if (key == 263) { adjustEditorScale(-0.05f); return true; }
            if (key == 262) { adjustEditorScale(0.05f); return true; }
            if (key == 268) { setEditorScale(0.0f, true); return true; }
            if (key == 269) { setEditorScale(1.0f, true); return true; }
            if (key == 256) { editorSizeFocused = false; return true; }
        }
        if (searching) {
            if (key == 256) { setSearching(false); return true; }
            layoutSearchBox(UiTheme.accent(AnkiConfig.getUiAccentPreset()));
            if (searchBox.keyPressed(new KeyEvent(key, scan, mod))) return true;
            return true;
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
        return charTyped((int) c, mod);
    }

    public boolean charTyped(int codePoint, int mod) {
        if (confirmClose || confirmAnim > 0.01f) return true;
        if (searching) {
            layoutSearchBox(UiTheme.accent(AnkiConfig.getUiAccentPreset()));
            if (searchBox.charTyped(new CharacterEvent(codePoint, 0))) return true;
            return true;
        }
        return false;
    }

    // ==================== ACTIONS ====================

    private void openEditor(NbtTreeNode node) {
        openChildScreen(new ValueEditScreen(this, node));
    }

    private void deleteNode() {
        if (selIdx < 0 || selIdx >= visibleNodes.size()) return;
        NbtTreeNode node = visibleNodes.get(selIdx);
        NbtTreeNode parent = node.getParent();
        if (parent == null) return;
        parent.removeChild(node);
        dirty = true;
        previewDirty = true;
        refreshVisible();
        if (selIdx >= visibleNodes.size()) selIdx = visibleNodes.size() - 1;
        setStatus(Component.translatable("ankinbt.status.deleted").getString(), UiTheme.textDim());
    }

    private void addTag() {
        NbtTreeNode target = (selIdx >= 0 && selIdx < visibleNodes.size()) ? visibleNodes.get(selIdx) : rootNode;
        // Only add to compound or list nodes
        if (!target.isCompound() && !target.isList()) target = target.getParent();
        if (target == null) target = rootNode;
        openChildScreen(new AddTagScreen(this, target));
    }

    public void addTagToNode(NbtTreeNode parent, String key, Tag tag) {
        parent.addChild(key, tag, false);
        parent.setExpanded(true);
        dirty = true;
        previewDirty = true;
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

        if (InventoryEditorOverlay.isItemEditorPreviewMode()) {
            setStatus(Component.translatable("ankinbt.status.preview_only").getString(), ERROR_C);
            return;
        }
        if (!mc.player.hasInfiniteMaterials()) {
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
        VersionCompat.get().sanitizeForCreativeSave(newStack);
        if (inventorySlot >= 0) {
            int creativeSlot = creativePacketSlotFromEditedSlot(inventorySlot);
            if (creativeSlot < 0) {
                setStatus(Component.translatable("ankinbt.status.save_error").getString(), ERROR_C);
                return;
            }
            int playerSlot = playerInventoryIndexFromCreativeSlot(creativeSlot);
            if (playerSlot >= 0) {
                mc.player.getInventory().setItem(playerSlot, newStack.copy());
            }
            mc.gameMode.handleCreativeModeItemAdd(newStack.copy(), creativeSlot);
        } else {
            int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
            mc.player.getInventory().setItem(slot, newStack.copy());
            mc.gameMode.handleCreativeModeItemAdd(newStack.copy(), 36 + slot);
        }

        originalStack = newStack.copy();
        dirty = false;
        setStatus(Component.translatable("ankinbt.status.saved").getString(), SUCCESS);
    }

    private void switchToSimple() {
        ItemStack current = NbtHelper.deserializeItemStack(rootNode.toCompoundTag()).orElse(originalStack);
        if (inventoryParent != null) {
            InventoryEditorOverlay.switchToSimple(inventoryParent, current, originalStack, inventorySlot, dirty);
        } else {
            SimpleEditorScreen next = new SimpleEditorScreen(current, inventorySlot);
            next.restoreEditorState(originalStack, dirty);
            AnkiConfig.setPreferredItemEditor("simple");
            Minecraft.getInstance().setScreen(next);
        }
    }

    private void exportNbt() {
        String itemId = resolveItemPath(originalStack);
        long ts = System.currentTimeMillis() / 1000;
        String fileName = itemId + "_" + ts;
        CompoundTag rebuilt = rootNode.toCompoundTag();
        Path path = NbtFileIO.exportNbt(rebuilt, fileName);
        if (path != null) {
            setStatus(Component.translatable("ankinbt.export.success").getString(), SUCCESS);
        } else {
            setStatus(Component.translatable("ankinbt.export.failed").getString(), ERROR_C);
        }
    }

    private void exportNbtAs() {
        if (!hasTinyFd()) {
            setStatus(Component.translatable("ankinbt.export.dialog_unavailable").getString(), ERROR_C);
            return;
        }
        String itemId = resolveItemPath(originalStack);
        long ts = System.currentTimeMillis() / 1000;
        String fileName = itemId + "_" + ts;
        String picked = tinyFdSavePath(AnkiConfig.getExportPath().resolve(fileName + ".nbt").toString());
        if (picked == null || picked.isBlank()) return;
        Path path = NbtFileIO.exportNbtToPath(rootNode.toCompoundTag(), Path.of(picked));
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
            Class<?> clazz = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
            return pickTinyFdMethod(clazz, "tinyfd_saveFileDialog") != null
                    && pickTinyFdMethod(clazz, "tinyfd_openFileDialog") != null;
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
        long now = System.currentTimeMillis();
        if (nativeDialogOpen || now - lastNativeDialogAt < 600L) return null;
        nativeDialogOpen = true;
        try {
            Class<?> clazz = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
            Method method = pickTinyFdMethod(clazz, methodName);
            if (method != null) {
                Object out = method.invoke(null, tinyFdArgs(method.getParameterTypes(), defaultPath, isOpen));
                if (out instanceof CharSequence cs) return cs.toString();
            }
        } catch (Throwable ignored) {}
        finally {
            lastNativeDialogAt = System.currentTimeMillis();
            nativeDialogOpen = false;
        }
        return null;
    }

    private Method pickTinyFdMethod(Class<?> clazz, String methodName) {
        Method charSequenceMethod = null;
        Method stringArrayMethod = null;
        Method fallback = null;
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName)) continue;
            Class<?>[] types = method.getParameterTypes();
            boolean hasStringArray = false;
            boolean hasPointerBuffer = false;
            boolean hasByteBuffer = false;
            boolean hasUnsupported = false;
            for (Class<?> type : types) {
                if (type == String[].class) hasStringArray = true;
                if (type.getName().equals("org.lwjgl.PointerBuffer")) hasPointerBuffer = true;
                if (type.getName().equals("java.nio.ByteBuffer")) hasByteBuffer = true;
                if (type != String.class && type != CharSequence.class
                        && type != String[].class && type != boolean.class && type != Boolean.class
                        && !type.getName().equals("org.lwjgl.PointerBuffer")
                        && !type.getName().equals("java.nio.ByteBuffer")) {
                    hasUnsupported = true;
                }
            }
            if (hasUnsupported) continue;
            if (hasStringArray && stringArrayMethod == null) stringArrayMethod = method;
            if (!hasByteBuffer && hasPointerBuffer) {
                if (charSequenceMethod == null) charSequenceMethod = method;
            }
            if (!hasByteBuffer && fallback == null) fallback = method;
        }
        return charSequenceMethod != null ? charSequenceMethod
                : (stringArrayMethod != null ? stringArrayMethod : fallback);
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
        if (dirty && com.ankinbt.config.AnkiConfig.isConfirmOnClose()) {
            confirmAnim = 0f;
            confirmClose = true;
        } else {
            onClose();
        }
    }

    public void onNodeEdited() {
        dirty = true;
        previewDirty = true;
        refreshVisible();
        setStatus(Component.translatable("ankinbt.status.edited").getString(), UiTheme.textDim());
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
        float hoverAnim;
        Btn(int x, int y, int w, int h, String label, Component tooltip, Runnable action) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.label = label; this.tooltip = tooltip; this.action = action;
        }
        boolean isHover(int mx, int my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
        void render(GuiGraphics g, net.minecraft.client.gui.Font f, int mx, int my) {
            boolean h = isHover(mx, my);
            float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.16f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1.0f;
            hoverAnim = UiTheme.approach(hoverAnim, h ? 1.0f : 0.0f, speed);
            g.fill(x, y, x + w, this.y + this.h, UiTheme.mix(BTN_BG, BTN_HOVER, hoverAnim));
            g.drawString(f, label, x + (w - f.width(label)) / 2, y + (this.h - 8) / 2, UiTheme.textMain(), false);
            if (h && tooltip != null) VersionCompat.get().renderTooltip(g, f, tooltip, mx, my);
        }
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x();
        double my = event.y();
        if (searching) {
            layoutSearchBox(UiTheme.accent(AnkiConfig.getUiAccentPreset()));
            if (searchBox.mouseClicked(event, isDoubleClick)) {
                searchBox.setFocused(true);
                this.setFocused(searchBox);
                return true;
            }
        }
        if (mouseClicked(mx, my, event.button())) return true;
        if (isInsideEditor(mx, my)) return true;
        if (inventoryParent != null) {
            Slot hovered = EditorDock.hoveredSlot(inventoryParent);
            if (event.button() == 0 && EditorDock.isPlayerInventorySlot(hovered) && hovered.hasItem()) {
                return selectInventoryItem(hovered.getItem(), hovered.getContainerSlot());
            }
            return inventoryParent.mouseClicked(event, isDoubleClick);
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (resizingEditor && event.button() == 0) {
            updateEditorScale(event.x());
            return true;
        }
        if (draggingMenuBar && event.button() == 0) {
            applyMenuLayout(EditorDock.menuLayoutAt(width, height, 310, inventoryParent != null,
                    (int) Math.round(event.x()) - dragOffsetX, (int) Math.round(event.y()) - dragOffsetY,
                    editorScale, editorWidthAdjustment, editorHeightAdjustment));
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (resizingEditor) {
            resizingEditor = false;
            AnkiConfig.setItemEditorScale(editorScale);
            return true;
        }
        if (draggingMenuBar) {
            draggingMenuBar = false;
            if (inventoryParent != null && barBounds != null) {
                AnkiConfig.setItemEditorCustomPosition(barBounds.x(), barBounds.y());
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    private void updateEditorScale(double mouseX) {
        setEditorScale(EditorDock.sizeScaleFromMouse(width, mouseX), false);
    }

    private void adjustEditorScale(float delta) {
        setEditorScale(editorScale + delta, true);
    }

    private void setEditorScale(float scale, boolean save) {
        editorScale = Math.max(0.0f, Math.min(1.0f, scale));
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, inventoryParent != null,
                current, 310, editorScale, editorWidthAdjustment, editorHeightAdjustment));
        buildButtons();
        clampScroll();
        if (save) AnkiConfig.setItemEditorScale(editorScale);
    }

    private void adjustEditorAxes(float widthDelta, float heightDelta) {
        editorWidthAdjustment = EditorDock.adjustAxis(editorWidthAdjustment, widthDelta);
        editorHeightAdjustment = EditorDock.adjustAxis(editorHeightAdjustment, heightDelta);
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, inventoryParent != null,
                current, 310, editorScale, editorWidthAdjustment, editorHeightAdjustment));
        buildButtons();
        clampScroll();
        AnkiConfig.setItemEditorAxisAdjustments(editorWidthAdjustment, editorHeightAdjustment);
    }

    private void resetEditorSize() {
        editorScale = EditorDock.DEFAULT_EDITOR_SCALE;
        editorWidthAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
        editorHeightAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, inventoryParent != null,
                current, 310, editorScale, editorWidthAdjustment, editorHeightAdjustment));
        buildButtons();
        clampScroll();
        AnkiConfig.setItemEditorScale(editorScale);
        AnkiConfig.setItemEditorAxisAdjustments(editorWidthAdjustment, editorHeightAdjustment);
    }

    boolean selectInventoryItem(ItemStack stack, int slot) {
        if (stack == null || stack.isEmpty()) return false;
        if (slot == inventorySlot && ItemStack.isSameItemSameComponents(stack, originalStack)) return true;
        if (dirty) {
            setStatus(Component.translatable("ankinbt.status.save_before_switch").getString(), ERROR_C);
            return true;
        }
        inventorySlot = slot;
        loadItem(stack);
        scrollOff = 0;
        selIdx = -1;
        hoverIdx = -1;
        setSearching(false);
        openAnim = AnkiConfig.isUiAnimationEnabled() ? 0.62f : 1.0f;
        setStatus(Component.translatable("ankinbt.status.item_switched", stack.getHoverName().getString()).getString(), SUCCESS);
        return true;
    }

    @Override
    public void onClose() {
        if (inventoryParent != null) InventoryEditorOverlay.close(inventoryParent);
        else super.onClose();
    }

    void requestOverlayClose() {
        tryClose();
    }

    boolean isInsideEditor(double mouseX, double mouseY) {
        if (confirmClose || confirmAnim > 0.01f) return true;
        EditorDock.SizeControl sizeControl = EditorDock.sizeControl(width, height, editorScale);
        if (sizeControl.hit().contains(mouseX, mouseY)
                || sizeControl.reset().contains(mouseX, mouseY)
                || sizeControl.horizontal().contains(mouseX, mouseY)
                || sizeControl.vertical().contains(mouseX, mouseY)) return true;
        if (barBounds != null && barBounds.contains(mouseX, mouseY)) return true;
        return drawerOpen && drawerAnim > 0.08f && drawerBounds != null && drawerBounds.contains(mouseX, mouseY);
    }

    boolean isDraggingMenuBar() {
        return draggingMenuBar || resizingEditor;
    }

    void restoreEditorState(ItemStack original, boolean wasDirty) {
        originalStack = original.copy();
        dirty = wasDirty;
        drawerOpen = true;
    }

    void returnFromChildScreen() {
        if (inventoryParent != null) InventoryEditorOverlay.returnFromModal(inventoryParent);
        else Minecraft.getInstance().setScreen(this);
    }

    private void openChildScreen(Screen child) {
        if (inventoryParent != null) InventoryEditorOverlay.openModal(inventoryParent, child);
        else Minecraft.getInstance().setScreen(child);
    }

    private static int playerInventoryIndexFromCreativeSlot(int creativeSlot) {
        if (creativeSlot == 45) return 40;
        if (creativeSlot >= 36 && creativeSlot < 45) return creativeSlot - 36;
        if (creativeSlot >= 9 && creativeSlot < 36) return creativeSlot;
        return -1;
    }

    private static int creativePacketSlotFromEditedSlot(int editedSlot) {
        if (editedSlot == 40 || editedSlot == 45) return 45;
        if (editedSlot >= 36 && editedSlot < 45) return editedSlot;
        if (editedSlot >= 0 && editedSlot < 9) return 36 + editedSlot;
        if (editedSlot >= 9 && editedSlot < 36) return editedSlot;
        return -1;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (keyPressed(event.key(), event.scancode(), event.modifiers())) return true;
        return super.keyPressed(event);
    }
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (charTyped(event.codepoint(), 0)) return true;
        return super.charTyped(event);
    }
}



