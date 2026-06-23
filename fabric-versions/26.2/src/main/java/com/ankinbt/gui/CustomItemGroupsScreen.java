package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.util.ItemRegistryHelper;
import com.ankinbt.util.UiSound;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import com.ankinbt.compat.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CustomItemGroupsScreen extends Screen {

    private static final int TXT_TITLE = 0xFFF3F6FF;
    private static final int TXT_MAIN = 0xFFD9E2F2;
    private static final int TXT_DIM = 0xFF8EA3C7;
    private static final int TXT_OK = 0xFF34D399;
    private static final int TXT_ERR = 0xFFEF4444;

    private final Screen parent;
    private final List<UiBtn> buttons = new ArrayList<>();

    private Map<String, List<String>> groups = new LinkedHashMap<>();
    private List<String> order = new ArrayList<>();

    private EditBox groupNameBox;
    private int selectedGroup = -1;
    private int selectedItem = -1;
    private int dragGroupFrom = -1;
    private int dragItemFrom = -1;
    private int dragGroupTo = -1;
    private int dragItemTo = -1;
    private boolean draggingGroup = false;
    private boolean draggingItem = false;
    private int groupScroll = 0;
    private int itemScroll = 0;
    private int groupHScroll = 0;
    private int itemHScroll = 0;
    private float openAnim = 0f;
    private final Map<String, Item> itemCache = new HashMap<>();

    private int px, py, pw, ph;

    private Component status = Component.empty();
    private int statusColor = TXT_DIM;
    private long statusTime = 0;

    public CustomItemGroupsScreen(Screen parent) {
        super(Component.translatable("ankinbt.config.group_editor.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        recalcBounds();
        groups = new LinkedHashMap<>(AnkiConfig.getCustomItemGroups());
        order = new ArrayList<>(groups.keySet());
        if (!order.isEmpty()) selectedGroup = 0;
        groupScroll = 0;
        itemScroll = 0;
        groupHScroll = 0;
        itemHScroll = 0;

        groupNameBox = new EditBox(font, px + 16, py + 66, 230, 20, Component.empty());
        groupNameBox.setHint(Component.translatable("ankinbt.config.group_editor.name_hint"));
        if (selectedGroup >= 0) groupNameBox.setValue(order.get(selectedGroup));
        addRenderableWidget(groupNameBox);

        rebuildButtons();
    }

    private void recalcBounds() {
        pw = Math.min(900, width - 20);
        ph = Math.min(520, height - 20);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
    }

    private void rebuildButtons() {
        buttons.clear();
        clampScroll();

        int leftX = px + 16;
        int rightX = px + pw / 2 + 8;
        int topY = py + 100;
        int rowH = AnkiConfig.isUiCompactLayout() ? 20 : 22;
        int gap = AnkiConfig.isUiCompactLayout() ? 4 : 6;

        buttons.add(new UiBtn(leftX, topY, 72, rowH,
                () -> tr("ankinbt.config.group_editor.add_group"), this::addGroup, true, null));
        buttons.add(new UiBtn(leftX + 78, topY, 72, rowH,
                () -> tr("ankinbt.config.group_editor.rename_group"), this::renameGroup, true, null));
        buttons.add(new UiBtn(leftX + 156, topY, 72, rowH,
                () -> tr("ankinbt.config.group_editor.delete_group"), this::deleteGroup,
                selectedGroup >= 0, null));

        buttons.add(new UiBtn(rightX, topY, 88, rowH,
                () -> tr("ankinbt.config.group_editor.add_item"), this::pickItemToGroup,
                selectedGroup >= 0, null));
        buttons.add(new UiBtn(rightX + 94, topY, 64, rowH,
                () -> tr("ankinbt.config.group_editor.remove_item"), this::removeItem,
                selectedGroup >= 0 && selectedItem >= 0, null));
        buttons.add(new UiBtn(rightX + 164, topY, 52, rowH,
                () -> tr("ankinbt.config.group_editor.up"), this::moveItemUp,
                selectedGroup >= 0 && selectedItem > 0, null));
        buttons.add(new UiBtn(rightX + 222, topY, 52, rowH,
                () -> tr("ankinbt.config.group_editor.down"), this::moveItemDown,
                selectedGroup >= 0 && selectedItem >= 0 && selectedItem < currentItems().size() - 1, null));

        int bottomY = py + ph - 30;
        int barW = pw - 32;
        int actW = (barW - 16) / 3;
        int actX = px + 16;
        buttons.add(new UiBtn(actX, bottomY, actW, 20,
                () -> tr("ankinbt.config.group_editor.reset"), this::resetGroups, true, null));
        buttons.add(new UiBtn(actX + actW + 8, bottomY, actW, 20,
                () -> tr("ankinbt.config.group_editor.close"), this::onClose, true, null));
        buttons.add(new UiBtn(actX + (actW + 8) * 2, bottomY, actW, 20,
                () -> tr("ankinbt.edit.cancel"), this::onClose, true, null));
    }

    private void addGroup() {
        String name = safeName(groupNameBox.getValue());
        if (name.isEmpty()) {
            setStatus(Component.translatable("ankinbt.config.group_editor.invalid_name"), TXT_ERR);
            return;
        }
        if (groups.containsKey(name)) {
            setStatus(Component.translatable("ankinbt.config.group_editor.name_exists"), TXT_ERR);
            return;
        }
        groups.put(name, new ArrayList<>());
        order.add(name);
        selectedGroup = order.size() - 1;
        selectedItem = -1;
        persist();
        setStatus(Component.translatable("ankinbt.config.group_editor.saved"), TXT_OK);
        rebuildButtons();
    }

    private void renameGroup() {
        if (selectedGroup < 0 || selectedGroup >= order.size()) return;
        String oldName = order.get(selectedGroup);
        String newName = safeName(groupNameBox.getValue());
        if (newName.isEmpty()) {
            setStatus(Component.translatable("ankinbt.config.group_editor.invalid_name"), TXT_ERR);
            return;
        }
        if (!oldName.equals(newName) && groups.containsKey(newName)) {
            setStatus(Component.translatable("ankinbt.config.group_editor.name_exists"), TXT_ERR);
            return;
        }
        List<String> items = groups.remove(oldName);
        groups.put(newName, items == null ? new ArrayList<>() : items);
        order.set(selectedGroup, newName);
        persist();
        setStatus(Component.translatable("ankinbt.config.group_editor.saved"), TXT_OK);
        rebuildButtons();
    }

    private void deleteGroup() {
        if (selectedGroup < 0 || selectedGroup >= order.size()) return;
        String name = order.remove(selectedGroup);
        groups.remove(name);
        if (order.isEmpty()) {
            selectedGroup = -1;
            selectedItem = -1;
            groupNameBox.setValue("");
        } else {
            selectedGroup = Math.min(selectedGroup, order.size() - 1);
            selectedItem = -1;
            groupNameBox.setValue(order.get(selectedGroup));
        }
        persist();
        setStatus(Component.translatable("ankinbt.config.group_editor.saved"), TXT_OK);
        rebuildButtons();
    }

    private void pickItemToGroup() {
        if (selectedGroup < 0 || selectedGroup >= order.size()) return;
        String group = order.get(selectedGroup);
        Minecraft.getInstance().setScreenAndShow(new ItemPickerScreen(this, id -> {
            List<String> items = groups.computeIfAbsent(group, k -> new ArrayList<>());
            items.remove(id);
            items.add(id);
            selectedItem = items.size() - 1;
            persist();
            setStatus(Component.translatable("ankinbt.config.group_editor.saved"), TXT_OK);
            rebuildButtons();
        }));
    }

    private void removeItem() {
        List<String> items = currentItems();
        if (selectedItem < 0 || selectedItem >= items.size()) return;
        items.remove(selectedItem);
        if (selectedItem >= items.size()) selectedItem = items.size() - 1;
        persist();
        setStatus(Component.translatable("ankinbt.config.group_editor.saved"), TXT_OK);
        rebuildButtons();
    }

    private void moveItemUp() {
        List<String> items = currentItems();
        if (selectedItem <= 0 || selectedItem >= items.size()) return;
        String v = items.remove(selectedItem);
        items.add(selectedItem - 1, v);
        selectedItem--;
        persist();
        rebuildButtons();
    }

    private void moveItemDown() {
        List<String> items = currentItems();
        if (selectedItem < 0 || selectedItem >= items.size() - 1) return;
        String v = items.remove(selectedItem);
        items.add(selectedItem + 1, v);
        selectedItem++;
        persist();
        rebuildButtons();
    }

    private void resetGroups() {
        AnkiConfig.resetCustomItemGroups();
        groups = new LinkedHashMap<>(AnkiConfig.getCustomItemGroups());
        order = new ArrayList<>(groups.keySet());
        selectedGroup = order.isEmpty() ? -1 : 0;
        selectedItem = -1;
        groupNameBox.setValue(selectedGroup >= 0 ? order.get(selectedGroup) : "");
        setStatus(Component.translatable("ankinbt.config.group_editor.saved"), TXT_OK);
        rebuildButtons();
    }

    private void persist() {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (String name : order) {
            out.put(name, new ArrayList<>(groups.getOrDefault(name, new ArrayList<>())));
        }
        AnkiConfig.setCustomItemGroups(out);
    }

    private List<String> currentItems() {
        if (selectedGroup < 0 || selectedGroup >= order.size()) return new ArrayList<>();
        return groups.computeIfAbsent(order.get(selectedGroup), k -> new ArrayList<>());
    }

    private void moveGroup(int from, int to) {
        if (from < 0 || to < 0 || from >= order.size() || to >= order.size() || from == to) return;
        String value = order.remove(from);
        order.add(to, value);
        selectedGroup = to;
        selectedItem = -1;
        persist();
        setStatus(Component.translatable("ankinbt.config.group_editor.saved"), TXT_OK);
        clampScroll();
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (UiBtn btn : buttons) {
                if (btn.click((int) mx, (int) my)) return true;
            }

            int gx = px + 16;
            int gy = py + 132;
            int gw = pw / 2 - 28;
            int gh = ph - 170;
            int listH = gh - 10;
            if (mx >= gx && mx < gx + gw && my >= gy && my < gy + listH) {
                int rowH = 22;
                int idx = groupScroll + (((int) my - gy) / rowH);
                if (idx >= 0 && idx < order.size()) {
                    dragGroupFrom = idx;
                    dragGroupTo = idx;
                    selectedGroup = idx;
                    selectedItem = -1;
                    draggingGroup = true;
                    groupNameBox.setValue(order.get(selectedGroup));
                    rebuildButtons();
                    return true;
                }
            }

            int ix = px + pw / 2 + 8;
            int iy = py + 132;
            int iw = pw / 2 - 24;
            int ih = ph - 170;
            List<String> items = currentItems();
            int itemListH = ih - 10;
            if (mx >= ix && mx < ix + iw && my >= iy && my < iy + itemListH) {
                int rowH = 22;
                int idx = itemScroll + (((int) my - iy) / rowH);
                if (idx >= 0 && idx < items.size()) {
                    dragItemFrom = idx;
                    dragItemTo = idx;
                    selectedItem = idx;
                    draggingItem = true;
                    rebuildButtons();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {
        if (button != 0) return false;

        int gx = px + 16;
        int gy = py + 132;
        int gw = pw / 2 - 28;
        int gh = ph - 170;
        int listH = gh - 10;
        if (draggingGroup && mx >= gx && mx < gx + gw && my >= gy && my < gy + listH) {
            int idx = groupScroll + (((int) my - gy) / 22);
            if (idx >= 0 && idx < order.size()) dragGroupTo = idx;
            return true;
        }

        int ix = px + pw / 2 + 8;
        int iy = py + 132;
        int iw = pw / 2 - 24;
        int ih = ph - 170;
        List<String> items = currentItems();
        int itemListH = ih - 10;
        if (draggingItem && mx >= ix && mx < ix + iw && my >= iy && my < iy + itemListH) {
            int idx = itemScroll + (((int) my - iy) / 22);
            if (idx >= 0 && idx < items.size()) dragItemTo = idx;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int gx = px + 16;
        int gy = py + 132;
        int gw = pw / 2 - 28;
        int gh = ph - 170;
        int ix = px + pw / 2 + 8;
        int iy = py + 132;
        int iw = pw / 2 - 24;
        int ih = ph - 170;
        int listH = gh - 10;
        int itemListH = ih - 10;

        int step = (int) Math.signum(sy);
        if (step == 0) return false;
        boolean shift = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            var win = mc.getWindow();
            shift = InputConstants.isKeyDown(win, 340) || InputConstants.isKeyDown(win, 344);
        }

        if (mx >= gx && mx < gx + gw && my >= gy && my < gy + listH) {
            if (shift) {
                groupHScroll = Math.max(0, Math.min(maxGroupHScroll(gw), groupHScroll - step * 16));
            } else {
                groupScroll = Math.max(0, Math.min(maxGroupScroll(), groupScroll - step));
            }
            return true;
        }
        if (mx >= ix && mx < ix + iw && my >= iy && my < iy + itemListH) {
            if (shift) {
                itemHScroll = Math.max(0, Math.min(maxItemHScroll(iw), itemHScroll - step * 16));
            } else {
                itemScroll = Math.max(0, Math.min(maxItemScroll(), itemScroll - step));
            }
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            if (draggingGroup && dragGroupFrom >= 0 && dragGroupTo >= 0 && dragGroupFrom != dragGroupTo) {
                moveGroup(dragGroupFrom, dragGroupTo);
                dragGroupFrom = dragGroupTo;
            }
            if (draggingItem && dragItemFrom >= 0 && dragItemTo >= 0 && dragItemFrom != dragItemTo) {
                List<String> items = currentItems();
                if (dragItemFrom >= 0 && dragItemFrom < items.size() && dragItemTo >= 0 && dragItemTo < items.size()) {
                    String value = items.remove(dragItemFrom);
                    items.add(dragItemTo, value);
                    selectedItem = dragItemTo;
                    dragItemFrom = dragItemTo;
                    persist();
                    setStatus(Component.translatable("ankinbt.config.group_editor.saved"), TXT_OK);
                }
            }
            draggingGroup = false;
            draggingItem = false;
            dragGroupFrom = -1;
            dragItemFrom = -1;
            dragGroupTo = -1;
            dragItemTo = -1;
            rebuildButtons();
            return true;
        }
        return false;
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        render(new com.ankinbt.compat.GuiGraphics(g), mx, my, partialTick);
    }

    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        recalcBounds();
        clampScroll();
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);

        float opacity = AnkiConfig.getUiOpacity();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
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

        int gx = px + 16;
        int gy = py + 132;
        int gw = pw / 2 - 28;
        int gh = ph - 170;
        int listH = gh - 10;

        int ix = px + pw / 2 + 8;
        int iy = py + 132;
        int iw = pw / 2 - 24;
        int ih = ph - 170;
        int itemListH = ih - 10;

        g.fill(gx, gy, gx + gw, gy + gh, card);
        g.fill(ix, iy, ix + iw, iy + ih, card);
        border(g, gx, gy, gw, gh, border);
        border(g, ix, iy, iw, ih, border);

        g.drawString(font, title, px + 12, py + 12, TXT_TITLE, false);
        g.drawString(font, tr("ankinbt.config.group_editor.desc"), px + 180, py + 13, TXT_DIM, false);
        g.drawString(font, tr("ankinbt.config.group_editor.groups"), gx, py + 90, accent, false);
        g.drawString(font, tr("ankinbt.config.group_editor.items"), ix, py + 90, accent, false);

        if (groupNameBox != null) groupNameBox.extractRenderState(g.unwrap(), mx, my, partialTick);

        for (UiBtn btn : buttons) {
            btn.render(g, font, mx, my, accent);
        }

        int rowH = 22;
        int rows = Math.max(1, listH / rowH);
        int groupEnd = Math.min(order.size(), groupScroll + rows);
        for (int i = groupScroll; i < groupEnd; i++) {
            int ry = gy + (i - groupScroll) * rowH;
            if (ry + rowH > gy + listH) break;
            boolean selected = i == selectedGroup;
            boolean hover = mx >= gx && mx < gx + gw && my >= ry && my < ry + rowH;
            boolean dragTarget = draggingGroup && i == dragGroupTo;
            int bg = dragTarget ? UiTheme.withAlpha(0xF59E0B, 140) : selected ? UiTheme.withAlpha(accent & 0x00FFFFFF, 120) : hover ? 0x4A1F2E45 : 0x2A131C2B;
            g.fill(gx + 1, ry + 1, gx + gw - 1, ry + rowH - 1, bg);
            String name = order.get(i);
            String shown = scrolledText(name, groupHScroll, gw - 12);
            g.drawString(font, shown, gx + 6, ry + 7, TXT_MAIN, false);
        }

        List<String> items = currentItems();
        int itemEnd = Math.min(items.size(), itemScroll + rows);
        for (int i = itemScroll; i < itemEnd; i++) {
            int ry = iy + (i - itemScroll) * rowH;
            if (ry + rowH > iy + itemListH) break;
            boolean selected = i == selectedItem;
            boolean hover = mx >= ix && mx < ix + iw && my >= ry && my < ry + rowH;
            boolean dragTarget = draggingItem && i == dragItemTo;
            int bg = dragTarget ? UiTheme.withAlpha(0xF59E0B, 140) : selected ? UiTheme.withAlpha(accent & 0x00FFFFFF, 120) : hover ? 0x4A1F2E45 : 0x2A131C2B;
            g.fill(ix + 1, ry + 1, ix + iw - 1, ry + rowH - 1, bg);
            String id = items.get(i);
            Item item = resolveItem(id);
            if (item != null && item != Items.AIR) {
                g.renderItem(new ItemStack(item), ix + 4, ry + 3);
            }
            String shown = scrolledText(id, itemHScroll, iw - 30);
            g.drawString(font, shown, ix + 24, ry + 7, TXT_MAIN, false);
        }

        renderScrollBar(g, gx + gw - 4, gy, listH, order.size(), rows, groupScroll, accent);
        renderScrollBar(g, ix + iw - 4, iy, itemListH, items.size(), rows, itemScroll, accent);
        renderHorizontalBar(g, gx + 1, gy + listH + 2, gw - 6, maxGroupHScroll(gw), groupHScroll, accent);
        renderHorizontalBar(g, ix + 1, iy + itemListH + 2, iw - 6, maxItemHScroll(iw), itemHScroll, accent);

        if (status != null && !status.getString().isEmpty() && System.currentTimeMillis() - statusTime < 2400) {
            g.drawString(font, status, px + 16, py + ph - 44, statusColor, false);
        }
    }

    private String safeName(String in) {
        return in == null ? "" : in.trim();
    }

    private void renderScrollBar(GuiGraphics g, int x, int y, int h, int size, int rows, int scroll, int accent) {
        if (size <= rows) return;
        g.fill(x, y, x + 3, y + h, UiTheme.withAlpha(0xFFFFFF, 40));
        float ratio = rows / (float) size;
        int thumbH = Math.max(18, (int) (h * ratio));
        int max = Math.max(1, size - rows);
        int thumbY = y + (int) ((h - thumbH) * (scroll / (float) max));
        g.fill(x, thumbY, x + 3, thumbY + thumbH, UiTheme.withAlpha(accent & 0x00FFFFFF, 188));
    }

    private int maxGroupScroll() {
        int rows = Math.max(1, (ph - 180) / 22);
        return Math.max(0, order.size() - rows);
    }

    private int maxItemScroll() {
        int rows = Math.max(1, (ph - 180) / 22);
        return Math.max(0, currentItems().size() - rows);
    }

    private void clampScroll() {
        groupScroll = Math.max(0, Math.min(groupScroll, maxGroupScroll()));
        itemScroll = Math.max(0, Math.min(itemScroll, maxItemScroll()));
        groupHScroll = Math.max(0, Math.min(groupHScroll, maxGroupHScroll(pw / 2 - 28)));
        itemHScroll = Math.max(0, Math.min(itemHScroll, maxItemHScroll(pw / 2 - 24)));
    }

    private int maxGroupHScroll(int gw) {
        int maxW = 0;
        for (String name : order) maxW = Math.max(maxW, font.width(name));
        return Math.max(0, maxW - (gw - 12));
    }

    private int maxItemHScroll(int iw) {
        int maxW = 0;
        for (String id : currentItems()) maxW = Math.max(maxW, font.width(id));
        return Math.max(0, maxW - (iw - 30));
    }

    private String scrolledText(String text, int scrollPx, int width) {
        if (text == null) return "";
        if (scrollPx <= 0) {
            return font.width(text) <= width ? text : font.plainSubstrByWidth(text, Math.max(8, width - 4));
        }
        int start = 0;
        int consumed = 0;
        while (start < text.length() && consumed < scrollPx) {
            consumed += font.width(String.valueOf(text.charAt(start)));
            start++;
        }
        String tail = start >= text.length() ? "" : text.substring(start);
        return font.plainSubstrByWidth(tail, Math.max(8, width - 4));
    }

    private void renderHorizontalBar(GuiGraphics g, int x, int y, int w, int max, int scroll, int accent) {
        if (max <= 0) return;
        g.fill(x, y, x + w, y + 3, UiTheme.withAlpha(0xFFFFFF, 40));
        int thumbW = Math.max(16, (int) (w * Math.max(0.12f, (w / (float) (w + max)))));
        int thumbX = x + (int) ((w - thumbW) * (scroll / (float) Math.max(1, max)));
        g.fill(thumbX, y, thumbX + thumbW, y + 3, UiTheme.withAlpha(accent & 0x00FFFFFF, 188));
    }

    private Item resolveItem(String id) {
        if (id == null || id.isBlank()) return null;
        Item cached = itemCache.get(id);
        if (cached != null) return cached;
        Item found = ItemRegistryHelper.resolveItem(id);
        itemCache.put(id, found);
        return found;
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private void setStatus(Component msg, int color) {
        status = msg;
        statusColor = color;
        statusTime = System.currentTimeMillis();
    }

    private String tr(String key) {
        return Component.translatable(key).getString();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
            UiSound.playClick();
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
            g.drawString(font, text, x + 6, y + 7, color, false);
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


