package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.util.ItemRegistryHelper;
import net.minecraft.client.Minecraft;
import com.ankinbt.compat.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemPickerScreen extends Screen {

    private static final int TXT_TITLE = 0xFFF3F6FF;
    private static final int TXT_MAIN = 0xFFD9E2F2;
    private static final int TXT_DIM = 0xFF8EA3C7;

    private final Screen parent;
    private final Consumer<String> onPick;
    private final Runnable closeAction;

    private final List<String> allItemIds = new ArrayList<>();
    private final Map<String, Item> itemById = new LinkedHashMap<>();
    private final List<String> filteredIds = new ArrayList<>();
    private final List<UiBtn> buttons = new ArrayList<>();
    private final List<Group> groups = new ArrayList<>();

    private EditBox searchBox;
    private String activeGroup = "all";
    private int listScroll = 0;
    private int searchY = 66;
    private int listY = 100;
    private int listH = 280;
    private int px, py, pw, ph;
    private float openAnim = 0f;

    private static class Group {
        final String id;
        final String label;

        Group(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    public ItemPickerScreen(Screen parent, Consumer<String> onPick) {
        this(parent, onPick, () -> Minecraft.getInstance().setScreen(parent));
    }

    public ItemPickerScreen(Screen parent, Consumer<String> onPick, Runnable closeAction) {
        super(Component.translatable("ankinbt.item_picker.title"));
        this.parent = parent;
        this.onPick = onPick;
        this.closeAction = closeAction;
    }

    @Override
    protected void init() {
        recalcBounds();
        loadItems();
        initGroups();
        rebuildButtons();

        searchBox = new EditBox(font, px + 18, searchY, pw - 36, 20, Component.empty());
        searchBox.setHint(Component.translatable("ankinbt.item_picker.search"));
        searchBox.setResponder(v -> {
            listScroll = 0;
            refreshFiltered();
        });
        addRenderableWidget(searchBox);
        focusSearchBox();
        refreshFiltered();
    }

    private void recalcBounds() {
        pw = Math.min(900, width - 20);
        ph = Math.min(520, height - 20);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
    }

    private void loadItems() {
        allItemIds.clear();
        itemById.clear();
        Map<String, Item> all = ItemRegistryHelper.allItemsById();
        allItemIds.addAll(all.keySet());
        itemById.putAll(all);
        allItemIds.sort(Comparator.naturalOrder());
    }

    private void initGroups() {
        groups.clear();
        groups.add(new Group("all", tr("ankinbt.item_picker.group.all")));
        groups.add(new Group("recent", tr("ankinbt.item_picker.group.recent")));
        groups.add(new Group("blocks", tr("ankinbt.item_picker.group.blocks")));
        groups.add(new Group("dyed_blocks", tr("ankinbt.item_picker.group.dyed_blocks")));
        groups.add(new Group("tools", tr("ankinbt.item_picker.group.tools")));
        groups.add(new Group("materials", tr("ankinbt.item_picker.group.materials")));

        Map<String, List<String>> custom = new LinkedHashMap<>(AnkiConfig.getCustomItemGroups());
        for (var e : custom.entrySet()) {
            groups.add(new Group("custom:" + e.getKey(), e.getKey()));
        }
    }

    private void rebuildButtons() {
        buttons.clear();
        int x = px + 18;
        int y = py + 38;
        int btnGap = 6;
        int lineH = 24;
        int maxX = px + pw - 18;

        for (Group group : groups) {
            Group g = group;
            int w = Math.min(118, Math.max(62, font.width(g.label) + 18));
            if (x + w > maxX) {
                x = px + 18;
                y += lineH;
            }
            buttons.add(new UiBtn(x, y, w, 20, () -> g.label, () -> {
                activeGroup = g.id;
                listScroll = 0;
                refreshFiltered();
            }, true, () -> activeGroup.equals(g.id)));
            x += w + btnGap;
            if (x > px + pw - 160) {
                x = px + 18;
                y += lineH;
            }
        }

        if (x + 118 > maxX) {
            x = px + 18;
            y += lineH;
        }

        searchY = y + 28;
        listY = searchY + 28;
        int bottomY = py + ph - 30;

        int right = px + pw - 18;
        int wCancel = 84;
        int wClear = 86;
        int wGroup = 88;
        int gap = 8;

        int xCancel = right - wCancel;
        int xClear = xCancel - gap - wClear;
        int xGroup = xClear - gap - wGroup;
        int altY = bottomY;
        if (xGroup < px + 180) {
            altY = bottomY - 24;
        }
        listH = Math.max(88, altY - listY - 8);

        buttons.add(new UiBtn(xGroup, altY, wGroup, 20,
                () -> tr("ankinbt.config.open_group_editor"),
                () -> Minecraft.getInstance().setScreen(new CustomItemGroupsScreen(this)), true, null));

        buttons.add(new UiBtn(xClear, altY, wClear, 20,
                () -> tr("ankinbt.item_picker.clear_recent"),
                AnkiConfig::clearRecentItemIds, true, null));
        buttons.add(new UiBtn(xCancel, altY, wCancel, 20,
                () -> tr("ankinbt.edit.cancel"),
                this::onClose, true, null));
    }

    private void refreshFiltered() {
        String search = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        filteredIds.clear();
        for (String id : allItemIds) {
            if (!search.isEmpty() && !id.toLowerCase(Locale.ROOT).contains(search)) continue;
            if (matchesGroup(id)) filteredIds.add(id);
        }
        clampListScroll();
    }

    private boolean matchesGroup(String id) {
        if ("all".equals(activeGroup)) return true;
        if ("recent".equals(activeGroup)) return AnkiConfig.getRecentItemIds().contains(id);

        Item item = itemById.get(id);
        if (item == null || item == Items.AIR) return false;

        if ("blocks".equals(activeGroup)) return item instanceof BlockItem;
        if ("dyed_blocks".equals(activeGroup)) return item instanceof BlockItem && isDyedPath(id);
        if ("tools".equals(activeGroup)) return isToolItem(item);
        if ("materials".equals(activeGroup)) return isMaterialItem(item, id);

        if (activeGroup.startsWith("custom:")) {
            String name = activeGroup.substring("custom:".length());
            List<String> custom = AnkiConfig.getCustomItemGroups().getOrDefault(name, List.of());
            return custom.contains(id);
        }

        return true;
    }

    private boolean isToolItem(Item item) {
        String name = item.toString().toLowerCase(Locale.ROOT);
        return name.contains("_sword")
                || name.contains("_axe")
                || name.contains("_pickaxe")
                || name.contains("_shovel")
                || name.contains("_hoe")
                || name.contains("shears")
                || name.contains("bow")
                || name.contains("crossbow")
                || name.contains("fishing_rod")
                || name.contains("shield")
                || name.contains("trident");
    }

    private boolean isMaterialItem(Item item, String id) {
        if (item instanceof BlockItem) return false;
        if (isToolItem(item)) return false;
        String path = id.toLowerCase(Locale.ROOT);
        return path.contains("ingot")
                || path.contains("nugget")
                || path.contains("gem")
                || path.contains("dust")
                || path.contains("shard")
                || path.contains("rod")
                || path.contains("string")
                || path.contains("leather")
                || path.contains("powder");
    }

    private boolean isDyedPath(String id) {
        String p = id.toLowerCase(Locale.ROOT);
        return p.contains("white_") || p.contains("orange_") || p.contains("magenta_") || p.contains("light_blue_")
                || p.contains("yellow_") || p.contains("lime_") || p.contains("pink_") || p.contains("gray_")
                || p.contains("light_gray_") || p.contains("cyan_") || p.contains("purple_") || p.contains("blue_")
                || p.contains("brown_") || p.contains("green_") || p.contains("red_") || p.contains("black_");
    }

    private boolean handleMouseClick(double mx, double my, int button) {
        if (button == 0 && isInSearchBox(mx, my)) {
            focusSearchBox();
            return true;
        }
        for (UiBtn btn : buttons) {
            if (btn.click((int) mx, (int) my)) {
                initGroups();
                rebuildButtons();
                refreshFiltered();
                return true;
            }
        }

        if (button == 0) {
            String id = rowAt((int) mx, (int) my);
            if (id != null) {
                AnkiConfig.addRecentItemId(id);
                onPick.accept(id);
                onClose();
                return true;
            }
        }

        unfocusSearchBox();
        return false;
    }

    private boolean isInSearchBox(double mx, double my) {
        return searchBox != null && mx >= px + 18 && mx < px + pw - 18 && my >= searchY && my < searchY + 20;
    }

    private void focusSearchBox() {
        if (searchBox == null) return;
        searchBox.setFocused(true);
        setFocused(searchBox);
    }

    private void unfocusSearchBox() {
        if (searchBox == null) return;
        searchBox.setFocused(false);
    }

    private boolean handleKeyPressed(KeyEvent event) {
        if (searchBox != null && searchBox.keyPressed(event)) {
            return true;
        }
        if (event.key() == 256) {
            onClose();
            return true;
        }
        return false;
    }

    private boolean handleCharTyped(CharacterEvent event) {
        if (searchBox != null && searchBox.isFocused() && searchBox.charTyped(event)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int x = px + 18;
        int w = pw - 36;
        if (mx >= x && mx < x + w && my >= listY && my < listY + listH) {
            int step = (int) Math.signum(sy);
            if (step != 0) {
                listScroll = Math.max(0, Math.min(maxListScroll(), listScroll - step));
            }
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        render(new com.ankinbt.compat.GuiGraphics(g), mx, my, partialTick);
    }

    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        recalcBounds();
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        float opacity = AnkiConfig.getUiOpacity();

        int scrim = UiTheme.scrim(opacity, openAnim);
        int panel = UiTheme.panel(opacity, openAnim);
        int card = UiTheme.card(opacity, openAnim);
        int border = UiTheme.border(opacity, openAnim);
        int shadow = UiTheme.shadow(opacity, openAnim, AnkiConfig.isUiShadowEnabled());

        g.fill(0, 0, width, height, scrim);
        if (shadow != 0) g.fill(px + 4, py + 4, px + pw + 4, py + ph + 4, shadow);
        g.fill(px, py, px + pw, py + ph, panel);
        border(g, px, py, pw, ph, border);

        g.fill(px + 1, py + 1, px + pw - 1, py + 34, UiTheme.header(opacity, openAnim));
        g.fill(px + 1, py + 34, px + pw - 1, py + 35, border);
        g.fill(px + 1, listY - 6, px + pw - 1, py + ph - 40, card);

        com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, px + 12, py + 12, TXT_TITLE, false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.item_picker.tip"), px + 220, py + 13, TXT_DIM, false);

        if (searchBox != null) searchBox.extractRenderState(g.unwrap(), mx, my, partialTick);

        for (UiBtn btn : buttons) btn.render(g, font, mx, my, accent);
        renderRows(g, mx, my);
    }

    private void renderRows(GuiGraphics g, int mx, int my) {
        int x = px + 18;
        int y = listY;
        int w = pw - 36;
        int rowH = AnkiConfig.isUiCompactLayout() ? 22 : 24;
        int rows = Math.max(1, listH / rowH);
        int start = listScroll;
        int end = Math.min(filteredIds.size(), start + rows);

        g.enableScissor(x, y, x + w - 6, y + listH);
        for (int i = start; i < end; i++) {
            int row = i - start;
            int ry = y + row * rowH;
            boolean hover = mx >= x && mx < x + w && my >= ry && my < ry + rowH;
            int bg = hover ? 0x7A2A3B58 : 0x3A1A2335;
            g.fill(x, ry, x + w, ry + rowH - 2, bg);

            String id = filteredIds.get(i);
            Item item = itemById.get(id);
            if (item != null && item != Items.AIR) {
                g.renderItem(new ItemStack(item), x + 4, ry + 3);
            }

            String itemName = item != null && item != Items.AIR ? new ItemStack(item).getHoverName().getString() : id;
            String idText = "(" + id + ")";
            int textX = x + 26;
            int maxTextW = w - 34;

            String idDraw = idText;
            int idBudget = Math.max(60, maxTextW / 2);
            if (font.width(idDraw) > idBudget) idDraw = font.plainSubstrByWidth(idDraw, Math.max(10, idBudget - 2)) + "..";
            int idW = font.width(idDraw);

            int nameBudget = Math.max(20, maxTextW - idW - 4);
            String nameDraw = itemName;
            if (font.width(nameDraw) > nameBudget) nameDraw = font.plainSubstrByWidth(nameDraw, Math.max(10, nameBudget - 2)) + "..";

            int nameY = ry + (rowH <= 22 ? 7 : 8);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, nameDraw, textX, nameY, TXT_MAIN, false);
            int idX = textX + font.width(nameDraw) + 4;
            if (idX < textX + maxTextW) {
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, idDraw, idX, nameY, TXT_DIM, false);
            }
        }
        g.disableScissor();
        renderScrollBar(g, x + w - 4, y, listH, filteredIds.size(), rows, listScroll);
    }

    private String rowAt(int mx, int my) {
        int x = px + 18;
        int y = listY;
        int w = pw - 36;
        int rowH = AnkiConfig.isUiCompactLayout() ? 22 : 24;
        int rows = Math.max(1, listH / rowH);
        if (mx < x || mx >= x + w || my < y || my >= y + rowH * rows) return null;
        int row = (my - y) / rowH;
        int idx = listScroll + row;
        if (idx < 0 || idx >= filteredIds.size()) return null;
        return filteredIds.get(idx);
    }

    private int maxListScroll() {
        int rowH = AnkiConfig.isUiCompactLayout() ? 22 : 24;
        int rows = Math.max(1, listH / rowH);
        return Math.max(0, filteredIds.size() - rows);
    }

    private void clampListScroll() {
        listScroll = Math.max(0, Math.min(listScroll, maxListScroll()));
    }

    private void renderScrollBar(GuiGraphics g, int x, int y, int h, int size, int rows, int scroll) {
        if (size <= rows) return;
        g.fill(x, y, x + 3, y + h, UiTheme.withAlpha(0xFFFFFF, 42));
        float ratio = rows / (float) size;
        int thumbH = Math.max(18, (int) (h * ratio));
        int max = Math.max(1, size - rows);
        int thumbY = y + (int) ((h - thumbH) * (scroll / (float) max));
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(x, thumbY, x + 3, thumbY + thumbH, UiTheme.withAlpha(accent & 0x00FFFFFF, 188));
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (handleKeyPressed(event)) return true;
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (handleCharTyped(event)) return true;
        return super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x();
        double my = event.y();
        if (searchBox != null && searchBox.mouseClicked(event, isDoubleClick)) {
            focusSearchBox();
            return true;
        }
        if (isInSearchBox(mx, my)) {
            focusSearchBox();
            return true;
        }
        if (handleMouseClick(mx, my, event.button())) return true;
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public void onClose() {
        closeAction.run();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private String tr(String key) {
        return Component.translatable(key).getString();
    }

    static class UiBtn {
        final int x;
        final int y;
        final int w;
        final int h;
        final Supplier<String> label;
        final Runnable action;
        final boolean enabled;
        final Supplier<Boolean> selected;

        UiBtn(int x, int y, int w, int h, Supplier<String> label, Runnable action, boolean enabled, Supplier<Boolean> selected) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
            this.action = action;
            this.enabled = enabled;
            this.selected = selected;
        }

        boolean hover(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }

        boolean click(int mx, int my) {
            if (!enabled || !hover(mx, my)) return false;
            action.run();
            return true;
        }

        void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int accent) {
            boolean hover = hover(mx, my);
            boolean chosen = selected != null && Boolean.TRUE.equals(selected.get());

            int bg = !enabled ? 0x2A101827 : chosen ? (0xAA000000 | (accent & 0x00FFFFFF)) : hover ? 0x6A273752 : 0x4A1B2638;
            int edge = chosen ? accent : 0xFF2C3B5C;
            int color = enabled ? TXT_MAIN : TXT_DIM;

            g.fill(x, y, x + w, y + h, bg);
            g.fill(x, y, x + w, y + 1, edge);
            g.fill(x, y + h - 1, x + w, y + h, edge);
            g.fill(x, y, x + 1, y + h, edge);
            g.fill(x + w - 1, y, x + w, y + h, edge);

            String text = label.get();
            if (font.width(text) > w - 10) text = font.plainSubstrByWidth(text, w - 14) + "..";
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, x + 6, y + 7, color, false);
        }
    }
}


