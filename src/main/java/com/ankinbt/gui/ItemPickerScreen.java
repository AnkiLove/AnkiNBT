/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.input.MouseButtonInfo
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.gui.CustomItemGroupsScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.util.ItemRegistryHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ItemPickerScreen
extends Screen {
    private static final int TXT_TITLE = -788737;
    private static final int TXT_MAIN = -2497806;
    private static final int TXT_DIM = -7429177;
    private final Screen parent;
    private final Consumer<String> onPick;
    private final List<String> allItemIds = new ArrayList<String>();
    private final Map<String, Item> itemById = new LinkedHashMap<String, Item>();
    private final List<String> filteredIds = new ArrayList<String>();
    private final List<UiBtn> buttons = new ArrayList<UiBtn>();
    private final List<Group> groups = new ArrayList<Group>();
    private EditBox searchBox;
    private String activeGroup = "all";
    private int listScroll = 0;
    private int searchY = 66;
    private int listY = 100;
    private int listH = 280;
    private int px;
    private int py;
    private int pw;
    private int ph;
    private float openAnim = 0.0f;

    public ItemPickerScreen(Screen parent, Consumer<String> onPick) {
        super((Component)Component.translatable((String)"ankinbt.item_picker.title"));
        this.parent = parent;
        this.onPick = onPick;
    }

    protected void init() {
        this.recalcBounds();
        this.loadItems();
        this.initGroups();
        this.rebuildButtons();
        this.searchBox = new EditBox(this.font, this.px + 18, this.searchY, this.pw - 36, 20, (Component)Component.empty());
        this.searchBox.setHint((Component)Component.translatable((String)"ankinbt.item_picker.search"));
        this.searchBox.setResponder(v -> {
            this.listScroll = 0;
            this.refreshFiltered();
        });
        this.addRenderableWidget(this.searchBox);
        this.focusSearchBox();
        this.updateSearchBoxBounds();
        this.refreshFiltered();
    }

    private void recalcBounds() {
        this.pw = Math.min(900, this.width - 20);
        this.ph = Math.min(520, this.height - 20);
        this.px = (this.width - this.pw) / 2;
        this.py = (this.height - this.ph) / 2;
    }

    private void loadItems() {
        this.allItemIds.clear();
        this.itemById.clear();
        Map<String, Item> all = ItemRegistryHelper.allItemsById();
        this.allItemIds.addAll(all.keySet());
        this.itemById.putAll(all);
        this.allItemIds.sort(Comparator.naturalOrder());
    }

    private void initGroups() {
        this.groups.clear();
        this.groups.add(new Group("all", this.tr("ankinbt.item_picker.group.all")));
        this.groups.add(new Group("recent", this.tr("ankinbt.item_picker.group.recent")));
        this.groups.add(new Group("blocks", this.tr("ankinbt.item_picker.group.blocks")));
        this.groups.add(new Group("dyed_blocks", this.tr("ankinbt.item_picker.group.dyed_blocks")));
        this.groups.add(new Group("tools", this.tr("ankinbt.item_picker.group.tools")));
        this.groups.add(new Group("materials", this.tr("ankinbt.item_picker.group.materials")));
        LinkedHashMap<String, List<String>> custom = new LinkedHashMap<String, List<String>>(AnkiConfig.getCustomItemGroups());
        for (Map.Entry e : custom.entrySet()) {
            this.groups.add(new Group("custom:" + (String)e.getKey(), (String)e.getKey()));
        }
    }

    private void rebuildButtons() {
        this.buttons.clear();
        int x = this.px + 18;
        int y = this.py + 38;
        int btnGap = 6;
        int lineH = 24;
        int maxX = this.px + this.pw - 18;
        Iterator<Group> iterator = this.groups.iterator();
        while (iterator.hasNext()) {
            Group group;
            Group g = group = iterator.next();
            int w = Math.min(118, Math.max(62, this.font.width(g.label) + 18));
            if (x + w > maxX) {
                x = this.px + 18;
                y += lineH;
            }
            this.buttons.add(new UiBtn(x, y, w, 20, () -> g.label, () -> {
                this.activeGroup = g.id;
                this.listScroll = 0;
                this.refreshFiltered();
            }, true, () -> this.activeGroup.equals(g.id)));
            if ((x += w + btnGap) <= this.px + this.pw - 160) continue;
            x = this.px + 18;
            y += lineH;
        }
        if (x + 118 > maxX) {
            x = this.px + 18;
            y += lineH;
        }
        this.searchY = y + 28;
        this.listY = this.searchY + 28;
        int bottomY = this.py + this.ph - 30;
        int right = this.px + this.pw - 18;
        int wCancel = 84;
        int wClear = 86;
        int wGroup = 88;
        int gap = 8;
        int xCancel = right - wCancel;
        int xClear = xCancel - gap - wClear;
        int xGroup = xClear - gap - wGroup;
        int altY = bottomY;
        if (xGroup < this.px + 180) {
            altY = bottomY - 24;
        }
        this.listH = Math.max(88, altY - this.listY - 8);
        this.buttons.add(new UiBtn(xGroup, altY, wGroup, 20, () -> this.tr("ankinbt.config.open_group_editor"), () -> Minecraft.getInstance().setScreen((Screen)new CustomItemGroupsScreen(this)), true, null));
        this.buttons.add(new UiBtn(xClear, altY, wClear, 20, () -> this.tr("ankinbt.item_picker.clear_recent"), AnkiConfig::clearRecentItemIds, true, null));
        this.buttons.add(new UiBtn(xCancel, altY, wCancel, 20, () -> this.tr("ankinbt.edit.cancel"), this::onClose, true, null));
        this.updateSearchBoxBounds();
    }

    private void updateSearchBoxBounds() {
        if (this.searchBox == null) {
            return;
        }
        this.searchBox.setX(this.px + 18);
        this.searchBox.setY(this.searchY);
        this.searchBox.setWidth(this.pw - 36);
    }

    private void refreshFiltered() {
        String search = this.searchBox == null ? "" : this.searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        this.filteredIds.clear();
        for (String id : this.allItemIds) {
            String itemName;
            Item item = this.itemById.get(id);
            String string = itemName = item == null ? "" : new ItemStack((ItemLike)item).getHoverName().getString().toLowerCase(Locale.ROOT);
            if (!search.isEmpty() && !id.toLowerCase(Locale.ROOT).contains(search) && !itemName.contains(search) || !this.matchesGroup(id)) continue;
            this.filteredIds.add(id);
        }
        this.clampListScroll();
    }

    private boolean matchesGroup(String id) {
        if ("all".equals(this.activeGroup)) {
            return true;
        }
        if ("recent".equals(this.activeGroup)) {
            return AnkiConfig.getRecentItemIds().contains(id);
        }
        Item item = this.itemById.get(id);
        if (item == null || item == Items.AIR) {
            return false;
        }
        if ("blocks".equals(this.activeGroup)) {
            return item instanceof BlockItem;
        }
        if ("dyed_blocks".equals(this.activeGroup)) {
            return item instanceof BlockItem && this.isDyedPath(id);
        }
        if ("tools".equals(this.activeGroup)) {
            return this.isToolItem(item);
        }
        if ("materials".equals(this.activeGroup)) {
            return this.isMaterialItem(item, id);
        }
        if (this.activeGroup.startsWith("custom:")) {
            String name = this.activeGroup.substring("custom:".length());
            List custom = AnkiConfig.getCustomItemGroups().getOrDefault(name, List.of());
            return custom.contains(id);
        }
        return true;
    }

    private boolean isToolItem(Item item) {
        String name = item.toString().toLowerCase(Locale.ROOT);
        return name.contains("_sword") || name.contains("_axe") || name.contains("_pickaxe") || name.contains("_shovel") || name.contains("_hoe") || name.contains("shears") || name.contains("bow") || name.contains("crossbow") || name.contains("fishing_rod") || name.contains("shield") || name.contains("trident");
    }

    private boolean isMaterialItem(Item item, String id) {
        if (item instanceof BlockItem) {
            return false;
        }
        if (this.isToolItem(item)) {
            return false;
        }
        String path = id.toLowerCase(Locale.ROOT);
        return path.contains("ingot") || path.contains("nugget") || path.contains("gem") || path.contains("dust") || path.contains("shard") || path.contains("rod") || path.contains("string") || path.contains("leather") || path.contains("powder");
    }

    private boolean isDyedPath(String id) {
        String p = id.toLowerCase(Locale.ROOT);
        return p.contains("white_") || p.contains("orange_") || p.contains("magenta_") || p.contains("light_blue_") || p.contains("yellow_") || p.contains("lime_") || p.contains("pink_") || p.contains("gray_") || p.contains("light_gray_") || p.contains("cyan_") || p.contains("purple_") || p.contains("blue_") || p.contains("brown_") || p.contains("green_") || p.contains("red_") || p.contains("black_");
    }

    private boolean handleMouseClick(double mx, double my, int button) {
        String id;
        if (button == 0 && this.isInSearchBox(mx, my)) {
            this.focusSearchBox();
            return true;
        }
        for (UiBtn btn : this.buttons) {
            if (!btn.click((int)mx, (int)my)) continue;
            this.initGroups();
            this.rebuildButtons();
            this.refreshFiltered();
            return true;
        }
        if (button == 0 && (id = this.rowAt((int)mx, (int)my)) != null) {
            AnkiConfig.addRecentItemId(id);
            this.onPick.accept(id);
            this.onClose();
            return true;
        }
        if (this.searchBox != null) {
            this.unfocusSearchBox();
        }
        return false;
    }

    private boolean isInSearchBox(double mx, double my) {
        return this.searchBox != null && mx >= (double)(this.px + 18) && mx < (double)(this.px + this.pw - 18) && my >= (double)this.searchY && my < (double)(this.searchY + 20);
    }

    private void focusSearchBox() {
        if (this.searchBox == null) {
            return;
        }
        this.searchBox.setFocused(true);
        this.setFocused((GuiEventListener)this.searchBox);
    }

    private void unfocusSearchBox() {
        if (this.searchBox == null) {
            return;
        }
        this.searchBox.setFocused(false);
    }

    private boolean handleKeyPressed(int key, int scancode, int modifiers) {
        if (this.searchBox != null && this.searchBox.keyPressed(key, scancode, modifiers)) {
            return true;
        }
        if (key == 256) {
            this.onClose();
            return true;
        }
        return false;
    }

    private boolean handleCharTyped(char codePoint, int modifiers) {
        return this.searchBox != null && this.searchBox.charTyped(codePoint, modifiers);
    }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int x = this.px + 18;
        int w = this.pw - 36;
        if (mx >= (double)x && mx < (double)(x + w) && my >= (double)this.listY && my < (double)(this.listY + this.listH)) {
            int step = (int)Math.signum(sy);
            if (step != 0) {
                this.listScroll = Math.max(0, Math.min(this.maxListScroll(), this.listScroll - step));
            }
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        this.recalcBounds();
        this.updateSearchBoxBounds();
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        float opacity = AnkiConfig.getUiOpacity();
        int scrim = UiTheme.scrim(opacity, this.openAnim);
        int panel = UiTheme.panel(opacity, this.openAnim);
        int card = UiTheme.card(opacity, this.openAnim);
        int border = UiTheme.border(opacity, this.openAnim);
        int shadow = UiTheme.shadow(opacity, this.openAnim, AnkiConfig.isUiShadowEnabled());
        g.fill(0, 0, this.width, this.height, scrim);
        if (shadow != 0) {
            g.fill(this.px + 4, this.py + 4, this.px + this.pw + 4, this.py + this.ph + 4, shadow);
        }
        g.fill(this.px, this.py, this.px + this.pw, this.py + this.ph, panel);
        this.border(g, this.px, this.py, this.pw, this.ph, border);
        g.fill(this.px + 1, this.py + 1, this.px + this.pw - 1, this.py + 34, UiTheme.header(opacity, this.openAnim));
        g.fill(this.px + 1, this.py + 34, this.px + this.pw - 1, this.py + 35, border);
        g.fill(this.px + 1, this.listY - 6, this.px + this.pw - 1, this.py + this.ph - 40, card);
        VersionCompat.get().drawString(g, this.font, this.title, this.px + 12, this.py + 12, -788737, false);
        VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.item_picker.tip"), this.px + 220, this.py + 13, -7429177, false);
        if (this.searchBox != null) {
            this.searchBox.render(g, mx, my, partialTick);
        }
        for (UiBtn btn : this.buttons) {
            btn.render(g, this.font, mx, my, accent);
        }
        this.renderRows(g, mx, my);
    }

    private void renderRows(GuiGraphics g, int mx, int my) {
        int x = this.px + 18;
        int y = this.listY;
        int w = this.pw - 36;
        int rowH = AnkiConfig.isUiCompactLayout() ? 22 : 24;
        int rows = Math.max(1, this.listH / rowH);
        int start = this.listScroll;
        int end = Math.min(this.filteredIds.size(), start + rows);
        g.enableScissor(x, y, x + w - 6, y + this.listH);
        for (int i = start; i < end; ++i) {
            int row = i - start;
            int ry = y + row * rowH;
            boolean hover = mx >= x && mx < x + w && my >= ry && my < ry + rowH;
            int bg = hover ? 2049588056 : 974791477;
            g.fill(x, ry, x + w, ry + rowH - 2, bg);
            String id = this.filteredIds.get(i);
            Item item = this.itemById.get(id);
            if (item != null && item != Items.AIR) {
                g.renderItem(new ItemStack((ItemLike)item), x + 4, ry + 3);
            }
            String itemName = item != null && item != Items.AIR ? new ItemStack((ItemLike)item).getHoverName().getString() : id;
            String idText = "(" + id + ")";
            int textX = x + 26;
            int maxTextW = w - 34;
            String idDraw = idText;
            int idBudget = Math.max(60, maxTextW / 2);
            if (this.font.width(idDraw) > idBudget) {
                idDraw = this.font.plainSubstrByWidth(idDraw, Math.max(10, idBudget - 2)) + "..";
            }
            int idW = this.font.width(idDraw);
            int nameBudget = Math.max(20, maxTextW - idW - 4);
            Object nameDraw = itemName;
            if (this.font.width((String)nameDraw) > nameBudget) {
                nameDraw = this.font.plainSubstrByWidth((String)nameDraw, Math.max(10, nameBudget - 2)) + "..";
            }
            int nameY = ry + (rowH <= 22 ? 7 : 8);
            VersionCompat.get().drawString(g, this.font, (String)nameDraw, textX, nameY, -2497806, false);
            int idX = textX + this.font.width((String)nameDraw) + 4;
            if (idX >= textX + maxTextW) continue;
            VersionCompat.get().drawString(g, this.font, idDraw, idX, nameY, -7429177, false);
        }
        g.disableScissor();
        this.renderScrollBar(g, x + w - 4, y, this.listH, this.filteredIds.size(), rows, this.listScroll);
    }

    private String rowAt(int mx, int my) {
        int x = this.px + 18;
        int y = this.listY;
        int w = this.pw - 36;
        int rowH = AnkiConfig.isUiCompactLayout() ? 22 : 24;
        int rows = Math.max(1, this.listH / rowH);
        if (mx < x || mx >= x + w || my < y || my >= y + rowH * rows) {
            return null;
        }
        int row = (my - y) / rowH;
        int idx = this.listScroll + row;
        if (idx < 0 || idx >= this.filteredIds.size()) {
            return null;
        }
        return this.filteredIds.get(idx);
    }

    private int maxListScroll() {
        int rowH = AnkiConfig.isUiCompactLayout() ? 22 : 24;
        int rows = Math.max(1, this.listH / rowH);
        return Math.max(0, this.filteredIds.size() - rows);
    }

    private void clampListScroll() {
        this.listScroll = Math.max(0, Math.min(this.listScroll, this.maxListScroll()));
    }

    private void renderScrollBar(GuiGraphics g, int x, int y, int h, int size, int rows, int scroll) {
        if (size <= rows) {
            return;
        }
        g.fill(x, y, x + 3, y + h, UiTheme.withAlpha(0xFFFFFF, 42));
        float ratio = (float)rows / (float)size;
        int thumbH = Math.max(18, (int)((float)h * ratio));
        int max = Math.max(1, size - rows);
        int thumbY = y + (int)((float)(h - thumbH) * ((float)scroll / (float)max));
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(x, thumbY, x + 3, thumbY + thumbH, UiTheme.withAlpha(accent & 0xFFFFFF, 188));
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    public boolean keyPressed(KeyEvent event) {
        if (this.handleKeyPressed(event.key(), event.scancode(), event.modifiers())) {
            return true;
        }
        return super.keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public boolean charTyped(CharacterEvent event) {
        if (this.handleCharTyped((char)event.codepoint(), event.modifiers())) {
            return true;
        }
        return super.charTyped((char)event.codepoint(), event.modifiers());
    }

    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (this.handleKeyPressed(key, scancode, modifiers)) {
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (this.handleCharTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (this.searchBox != null && this.isInSearchBox(mx, my)) {
            this.searchBox.mouseClicked(mx, my, button);
            this.focusSearchBox();
            return true;
        }
        if (this.handleMouseClick(mx, my, button)) {
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x();
        double my = event.y();
        if (this.mouseClicked(mx, my, event.button())) {
            return true;
        }
        if (this.minecraft != null) {
            double sw = this.minecraft.getWindow().getScreenWidth();
            double sh = this.minecraft.getWindow().getScreenHeight();
            if (sw > 0.0 && sh > 0.0) {
                double sx = mx * (double)this.width / sw;
                double sy = my * (double)this.height / sh;
                if ((Math.abs(sx - mx) > 0.5 || Math.abs(sy - my) > 0.5) && this.mouseClicked(sx, sy, event.button())) {
                    return true;
                }
            }
        }
        return super.mouseClicked(event.x(), event.y(), event.button());
    }

    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private String tr(String key) {
        return Component.translatable((String)key).getString();
    }

    private static class Group {
        final String id;
        final String label;

        Group(String id, String label) {
            this.id = id;
            this.label = label;
        }
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
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }

        boolean click(int mx, int my) {
            if (!this.enabled || !this.hover(mx, my)) {
                return false;
            }
            this.action.run();
            return true;
        }

        void render(GuiGraphics g, Font font, int mx, int my, int accent) {
            boolean chosen;
            boolean hover = this.hover(mx, my);
            boolean bl = chosen = this.selected != null && Boolean.TRUE.equals(this.selected.get());
            int bg = !this.enabled ? 705697831 : (chosen ? 0xAA000000 | accent & 0xFFFFFF : (hover ? 1780954962 : 1243293240));
            int edge = chosen ? accent : -13878436;
            int color = this.enabled ? -2497806 : -7429177;
            g.fill(this.x, this.y, this.x + this.w, this.y + this.h, bg);
            g.fill(this.x, this.y, this.x + this.w, this.y + 1, edge);
            g.fill(this.x, this.y + this.h - 1, this.x + this.w, this.y + this.h, edge);
            g.fill(this.x, this.y, this.x + 1, this.y + this.h, edge);
            g.fill(this.x + this.w - 1, this.y, this.x + this.w, this.y + this.h, edge);
            Object text = this.label.get();
            if (font.width((String)text) > this.w - 10) {
                text = font.plainSubstrByWidth((String)text, this.w - 14) + "..";
            }
            VersionCompat.get().drawString(g, font, (String)text, this.x + 6, this.y + 7, color, false);
        }
    }
}
