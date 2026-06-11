/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_364
 *  net.minecraft.class_3675
 *  net.minecraft.class_437
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.gui.ItemPickerScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.util.ItemRegistryHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_3675;
import net.minecraft.class_437;

public class CustomItemGroupsScreen
extends class_437 {
    private static final int TXT_TITLE = -788737;
    private static final int TXT_MAIN = -2497806;
    private static final int TXT_DIM = -7429177;
    private static final int TXT_OK = -13315175;
    private static final int TXT_ERR = -1096636;
    private final class_437 parent;
    private final List<UiBtn> buttons = new ArrayList<UiBtn>();
    private Map<String, List<String>> groups = new LinkedHashMap<String, List<String>>();
    private List<String> order = new ArrayList<String>();
    private class_342 groupNameBox;
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
    private float openAnim = 0.0f;
    private final Map<String, class_1792> itemCache = new HashMap<String, class_1792>();
    private int px;
    private int py;
    private int pw;
    private int ph;
    private class_2561 status = class_2561.method_43473();
    private int statusColor = -7429177;
    private long statusTime = 0L;

    public CustomItemGroupsScreen(class_437 parent) {
        super((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.title"));
        this.parent = parent;
    }

    protected void method_25426() {
        this.recalcBounds();
        this.groups = new LinkedHashMap<String, List<String>>(AnkiConfig.getCustomItemGroups());
        this.order = new ArrayList<String>(this.groups.keySet());
        if (!this.order.isEmpty()) {
            this.selectedGroup = 0;
        }
        this.groupScroll = 0;
        this.itemScroll = 0;
        this.groupHScroll = 0;
        this.itemHScroll = 0;
        this.groupNameBox = new class_342(this.field_22793, this.px + 16, this.py + 66, 230, 20, (class_2561)class_2561.method_43473());
        this.groupNameBox.method_47404((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.name_hint"));
        if (this.selectedGroup >= 0) {
            this.groupNameBox.method_1852(this.order.get(this.selectedGroup));
        }
        this.method_37063((class_364)this.groupNameBox);
        this.rebuildButtons();
    }

    private void recalcBounds() {
        this.pw = Math.min(900, this.field_22789 - 20);
        this.ph = Math.min(520, this.field_22790 - 20);
        this.px = (this.field_22789 - this.pw) / 2;
        this.py = (this.field_22790 - this.ph) / 2;
    }

    private void rebuildButtons() {
        this.buttons.clear();
        this.clampScroll();
        int leftX = this.px + 16;
        int rightX = this.px + this.pw / 2 + 8;
        int topY = this.py + 100;
        int rowH = AnkiConfig.isUiCompactLayout() ? 20 : 22;
        int gap = AnkiConfig.isUiCompactLayout() ? 4 : 6;
        this.buttons.add(new UiBtn(leftX, topY, 72, rowH, () -> this.tr("ankinbt.config.group_editor.add_group"), this::addGroup, true, null));
        this.buttons.add(new UiBtn(leftX + 78, topY, 72, rowH, () -> this.tr("ankinbt.config.group_editor.rename_group"), this::renameGroup, true, null));
        this.buttons.add(new UiBtn(leftX + 156, topY, 72, rowH, () -> this.tr("ankinbt.config.group_editor.delete_group"), this::deleteGroup, this.selectedGroup >= 0, null));
        this.buttons.add(new UiBtn(rightX, topY, 88, rowH, () -> this.tr("ankinbt.config.group_editor.add_item"), this::pickItemToGroup, this.selectedGroup >= 0, null));
        this.buttons.add(new UiBtn(rightX + 94, topY, 64, rowH, () -> this.tr("ankinbt.config.group_editor.remove_item"), this::removeItem, this.selectedGroup >= 0 && this.selectedItem >= 0, null));
        this.buttons.add(new UiBtn(rightX + 164, topY, 52, rowH, () -> this.tr("ankinbt.config.group_editor.up"), this::moveItemUp, this.selectedGroup >= 0 && this.selectedItem > 0, null));
        this.buttons.add(new UiBtn(rightX + 222, topY, 52, rowH, () -> this.tr("ankinbt.config.group_editor.down"), this::moveItemDown, this.selectedGroup >= 0 && this.selectedItem >= 0 && this.selectedItem < this.currentItems().size() - 1, null));
        int bottomY = this.py + this.ph - 30;
        int barW = this.pw - 32;
        int actW = (barW - 16) / 3;
        int actX = this.px + 16;
        this.buttons.add(new UiBtn(actX, bottomY, actW, 20, () -> this.tr("ankinbt.config.group_editor.reset"), this::resetGroups, true, null));
        this.buttons.add(new UiBtn(actX + actW + 8, bottomY, actW, 20, () -> this.tr("ankinbt.config.group_editor.close"), this::method_25419, true, null));
        this.buttons.add(new UiBtn(actX + (actW + 8) * 2, bottomY, actW, 20, () -> this.tr("ankinbt.edit.cancel"), this::method_25419, true, null));
    }

    private void addGroup() {
        String name = this.safeName(this.groupNameBox.method_1882());
        if (name.isEmpty()) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.invalid_name"), -1096636);
            return;
        }
        if (this.groups.containsKey(name)) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.name_exists"), -1096636);
            return;
        }
        this.groups.put(name, new ArrayList());
        this.order.add(name);
        this.selectedGroup = this.order.size() - 1;
        this.selectedItem = -1;
        this.persist();
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.saved"), -13315175);
        this.rebuildButtons();
    }

    private void renameGroup() {
        if (this.selectedGroup < 0 || this.selectedGroup >= this.order.size()) {
            return;
        }
        String oldName = this.order.get(this.selectedGroup);
        String newName = this.safeName(this.groupNameBox.method_1882());
        if (newName.isEmpty()) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.invalid_name"), -1096636);
            return;
        }
        if (!oldName.equals(newName) && this.groups.containsKey(newName)) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.name_exists"), -1096636);
            return;
        }
        ArrayList items = this.groups.remove(oldName);
        this.groups.put(newName, items == null ? new ArrayList() : items);
        this.order.set(this.selectedGroup, newName);
        this.persist();
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.saved"), -13315175);
        this.rebuildButtons();
    }

    private void deleteGroup() {
        if (this.selectedGroup < 0 || this.selectedGroup >= this.order.size()) {
            return;
        }
        String name = this.order.remove(this.selectedGroup);
        this.groups.remove(name);
        if (this.order.isEmpty()) {
            this.selectedGroup = -1;
            this.selectedItem = -1;
            this.groupNameBox.method_1852("");
        } else {
            this.selectedGroup = Math.min(this.selectedGroup, this.order.size() - 1);
            this.selectedItem = -1;
            this.groupNameBox.method_1852(this.order.get(this.selectedGroup));
        }
        this.persist();
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.saved"), -13315175);
        this.rebuildButtons();
    }

    private void pickItemToGroup() {
        if (this.selectedGroup < 0 || this.selectedGroup >= this.order.size()) {
            return;
        }
        String group = this.order.get(this.selectedGroup);
        class_310.method_1551().method_1507((class_437)new ItemPickerScreen(this, id -> {
            List items = this.groups.computeIfAbsent(group, k -> new ArrayList());
            items.remove(id);
            items.add(id);
            this.selectedItem = items.size() - 1;
            this.persist();
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.saved"), -13315175);
            this.rebuildButtons();
        }));
    }

    private void removeItem() {
        List<String> items = this.currentItems();
        if (this.selectedItem < 0 || this.selectedItem >= items.size()) {
            return;
        }
        items.remove(this.selectedItem);
        if (this.selectedItem >= items.size()) {
            this.selectedItem = items.size() - 1;
        }
        this.persist();
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.saved"), -13315175);
        this.rebuildButtons();
    }

    private void moveItemUp() {
        List<String> items = this.currentItems();
        if (this.selectedItem <= 0 || this.selectedItem >= items.size()) {
            return;
        }
        String v = items.remove(this.selectedItem);
        items.add(this.selectedItem - 1, v);
        --this.selectedItem;
        this.persist();
        this.rebuildButtons();
    }

    private void moveItemDown() {
        List<String> items = this.currentItems();
        if (this.selectedItem < 0 || this.selectedItem >= items.size() - 1) {
            return;
        }
        String v = items.remove(this.selectedItem);
        items.add(this.selectedItem + 1, v);
        ++this.selectedItem;
        this.persist();
        this.rebuildButtons();
    }

    private void resetGroups() {
        AnkiConfig.resetCustomItemGroups();
        this.groups = new LinkedHashMap<String, List<String>>(AnkiConfig.getCustomItemGroups());
        this.order = new ArrayList<String>(this.groups.keySet());
        this.selectedGroup = this.order.isEmpty() ? -1 : 0;
        this.selectedItem = -1;
        this.groupNameBox.method_1852(this.selectedGroup >= 0 ? this.order.get(this.selectedGroup) : "");
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.saved"), -13315175);
        this.rebuildButtons();
    }

    private void persist() {
        LinkedHashMap<String, List<String>> out = new LinkedHashMap<String, List<String>>();
        for (String name : this.order) {
            out.put(name, new ArrayList(this.groups.getOrDefault(name, new ArrayList())));
        }
        AnkiConfig.setCustomItemGroups(out);
    }

    private List<String> currentItems() {
        if (this.selectedGroup < 0 || this.selectedGroup >= this.order.size()) {
            return new ArrayList<String>();
        }
        return this.groups.computeIfAbsent(this.order.get(this.selectedGroup), k -> new ArrayList());
    }

    private void moveGroup(int from, int to) {
        if (from < 0 || to < 0 || from >= this.order.size() || to >= this.order.size() || from == to) {
            return;
        }
        String value = this.order.remove(from);
        this.order.add(to, value);
        this.selectedGroup = to;
        this.selectedItem = -1;
        this.persist();
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.saved"), -13315175);
        this.clampScroll();
    }

    public boolean method_25402(double mx, double my, int button) {
        if (button == 0) {
            int rowH;
            int idx;
            int rowH2;
            int idx2;
            for (UiBtn btn : this.buttons) {
                if (!btn.click((int)mx, (int)my)) continue;
                return true;
            }
            int gx = this.px + 16;
            int gy = this.py + 132;
            int gw = this.pw / 2 - 28;
            int gh = this.ph - 170;
            int listH = gh - 10;
            if (mx >= (double)gx && mx < (double)(gx + gw) && my >= (double)gy && my < (double)(gy + listH) && (idx2 = this.groupScroll + ((int)my - gy) / (rowH2 = 22)) >= 0 && idx2 < this.order.size()) {
                this.dragGroupFrom = idx2;
                this.dragGroupTo = idx2;
                this.selectedGroup = idx2;
                this.selectedItem = -1;
                this.draggingGroup = true;
                this.groupNameBox.method_1852(this.order.get(this.selectedGroup));
                this.rebuildButtons();
                return true;
            }
            int ix = this.px + this.pw / 2 + 8;
            int iy = this.py + 132;
            int iw = this.pw / 2 - 24;
            int ih = this.ph - 170;
            List<String> items = this.currentItems();
            int itemListH = ih - 10;
            if (mx >= (double)ix && mx < (double)(ix + iw) && my >= (double)iy && my < (double)(iy + itemListH) && (idx = this.itemScroll + ((int)my - iy) / (rowH = 22)) >= 0 && idx < items.size()) {
                this.dragItemFrom = idx;
                this.dragItemTo = idx;
                this.selectedItem = idx;
                this.draggingItem = true;
                this.rebuildButtons();
                return true;
            }
        }
        return false;
    }

    public boolean method_25403(double mx, double my, int button, double dragX, double dragY) {
        if (button != 0) {
            return false;
        }
        int gx = this.px + 16;
        int gy = this.py + 132;
        int gw = this.pw / 2 - 28;
        int gh = this.ph - 170;
        int listH = gh - 10;
        if (this.draggingGroup && mx >= (double)gx && mx < (double)(gx + gw) && my >= (double)gy && my < (double)(gy + listH)) {
            int idx = this.groupScroll + ((int)my - gy) / 22;
            if (idx >= 0 && idx < this.order.size()) {
                this.dragGroupTo = idx;
            }
            return true;
        }
        int ix = this.px + this.pw / 2 + 8;
        int iy = this.py + 132;
        int iw = this.pw / 2 - 24;
        int ih = this.ph - 170;
        List<String> items = this.currentItems();
        int itemListH = ih - 10;
        if (this.draggingItem && mx >= (double)ix && mx < (double)(ix + iw) && my >= (double)iy && my < (double)(iy + itemListH)) {
            int idx = this.itemScroll + ((int)my - iy) / 22;
            if (idx >= 0 && idx < items.size()) {
                this.dragItemTo = idx;
            }
            return true;
        }
        return false;
    }

    public boolean method_25401(double mx, double my, double sx, double sy) {
        int gx = this.px + 16;
        int gy = this.py + 132;
        int gw = this.pw / 2 - 28;
        int gh = this.ph - 170;
        int ix = this.px + this.pw / 2 + 8;
        int iy = this.py + 132;
        int iw = this.pw / 2 - 24;
        int ih = this.ph - 170;
        int listH = gh - 10;
        int itemListH = ih - 10;
        int step = (int)Math.signum(sy);
        if (step == 0) {
            return false;
        }
        boolean shift = false;
        class_310 mc = class_310.method_1551();
        if (mc != null) {
            long handle = mc.method_22683().method_4490();
            boolean bl = shift = class_3675.method_15987((long)handle, (int)340) || class_3675.method_15987((long)handle, (int)344);
        }
        if (mx >= (double)gx && mx < (double)(gx + gw) && my >= (double)gy && my < (double)(gy + listH)) {
            if (shift) {
                this.groupHScroll = Math.max(0, Math.min(this.maxGroupHScroll(gw), this.groupHScroll - step * 16));
            } else {
                this.groupScroll = Math.max(0, Math.min(this.maxGroupScroll(), this.groupScroll - step));
            }
            return true;
        }
        if (mx >= (double)ix && mx < (double)(ix + iw) && my >= (double)iy && my < (double)(iy + itemListH)) {
            if (shift) {
                this.itemHScroll = Math.max(0, Math.min(this.maxItemHScroll(iw), this.itemHScroll - step * 16));
            } else {
                this.itemScroll = Math.max(0, Math.min(this.maxItemScroll(), this.itemScroll - step));
            }
            return true;
        }
        return super.method_25401(mx, my, sx, sy);
    }

    public boolean method_25406(double mx, double my, int button) {
        if (button == 0) {
            if (this.draggingGroup && this.dragGroupFrom >= 0 && this.dragGroupTo >= 0 && this.dragGroupFrom != this.dragGroupTo) {
                this.moveGroup(this.dragGroupFrom, this.dragGroupTo);
                this.dragGroupFrom = this.dragGroupTo;
            }
            if (this.draggingItem && this.dragItemFrom >= 0 && this.dragItemTo >= 0 && this.dragItemFrom != this.dragItemTo) {
                List<String> items = this.currentItems();
                if (this.dragItemFrom >= 0 && this.dragItemFrom < items.size() && this.dragItemTo >= 0 && this.dragItemTo < items.size()) {
                    String value = items.remove(this.dragItemFrom);
                    items.add(this.dragItemTo, value);
                    this.selectedItem = this.dragItemTo;
                    this.dragItemFrom = this.dragItemTo;
                    this.persist();
                    this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.config.group_editor.saved"), -13315175);
                }
            }
            this.draggingGroup = false;
            this.draggingItem = false;
            this.dragGroupFrom = -1;
            this.dragItemFrom = -1;
            this.dragGroupTo = -1;
            this.dragItemTo = -1;
            this.rebuildButtons();
            return true;
        }
        return false;
    }

    public void method_25394(class_332 g, int mx, int my, float partialTick) {
        int ry;
        int ry2;
        this.recalcBounds();
        this.clampScroll();
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        float opacity = AnkiConfig.getUiOpacity();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        int scrim = UiTheme.scrim(opacity, this.openAnim);
        int panel = UiTheme.panel(opacity, this.openAnim);
        int card = UiTheme.card(opacity, this.openAnim);
        int border = UiTheme.border(opacity, this.openAnim);
        int shadow = UiTheme.shadow(opacity, this.openAnim, AnkiConfig.isUiShadowEnabled());
        g.method_25294(0, 0, this.field_22789, this.field_22790, scrim);
        if (shadow != 0) {
            g.method_25294(this.px + 4, this.py + 4, this.px + this.pw + 4, this.py + this.ph + 4, shadow);
        }
        g.method_25294(this.px, this.py, this.px + this.pw, this.py + this.ph, panel);
        this.border(g, this.px, this.py, this.pw, this.ph, border);
        g.method_25294(this.px + 1, this.py + 1, this.px + this.pw - 1, this.py + 34, UiTheme.header(opacity, this.openAnim));
        g.method_25294(this.px + 1, this.py + 34, this.px + this.pw - 1, this.py + 35, border);
        int gx = this.px + 16;
        int gy = this.py + 132;
        int gw = this.pw / 2 - 28;
        int gh = this.ph - 170;
        int listH = gh - 10;
        int ix = this.px + this.pw / 2 + 8;
        int iy = this.py + 132;
        int iw = this.pw / 2 - 24;
        int ih = this.ph - 170;
        int itemListH = ih - 10;
        g.method_25294(gx, gy, gx + gw, gy + gh, card);
        g.method_25294(ix, iy, ix + iw, iy + ih, card);
        this.border(g, gx, gy, gw, gh, border);
        this.border(g, ix, iy, iw, ih, border);
        VersionCompat.get().drawString(g, this.field_22793, this.field_22785, this.px + 12, this.py + 12, -788737, false);
        VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.config.group_editor.desc"), this.px + 180, this.py + 13, -7429177, false);
        VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.config.group_editor.groups"), gx, this.py + 90, accent, false);
        VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.config.group_editor.items"), ix, this.py + 90, accent, false);
        if (this.groupNameBox != null) {
            this.groupNameBox.method_25394(g, mx, my, partialTick);
        }
        for (UiBtn btn : this.buttons) {
            btn.render(g, this.field_22793, mx, my, accent);
        }
        int rowH = 22;
        int rows = Math.max(1, listH / rowH);
        int groupEnd = Math.min(this.order.size(), this.groupScroll + rows);
        for (int i = this.groupScroll; i < groupEnd && (ry2 = gy + (i - this.groupScroll) * rowH) + rowH <= gy + listH; ++i) {
            boolean dragTarget;
            boolean selected = i == this.selectedGroup;
            boolean hover = mx >= gx && mx < gx + gw && my >= ry2 && my < ry2 + rowH;
            boolean bl = dragTarget = this.draggingGroup && i == this.dragGroupTo;
            int bg = dragTarget ? UiTheme.withAlpha(16096779, 140) : (selected ? UiTheme.withAlpha(accent & 0xFFFFFF, 120) : (hover ? 1243557445 : 705895467));
            g.method_25294(gx + 1, ry2 + 1, gx + gw - 1, ry2 + rowH - 1, bg);
            String name = this.order.get(i);
            String shown = this.scrolledText(name, this.groupHScroll, gw - 12);
            VersionCompat.get().drawString(g, this.field_22793, shown, gx + 6, ry2 + 7, -2497806, false);
        }
        List<String> items = this.currentItems();
        int itemEnd = Math.min(items.size(), this.itemScroll + rows);
        for (int i = this.itemScroll; i < itemEnd && (ry = iy + (i - this.itemScroll) * rowH) + rowH <= iy + itemListH; ++i) {
            boolean dragTarget;
            boolean selected = i == this.selectedItem;
            boolean hover = mx >= ix && mx < ix + iw && my >= ry && my < ry + rowH;
            boolean bl = dragTarget = this.draggingItem && i == this.dragItemTo;
            int bg = dragTarget ? UiTheme.withAlpha(16096779, 140) : (selected ? UiTheme.withAlpha(accent & 0xFFFFFF, 120) : (hover ? 1243557445 : 705895467));
            g.method_25294(ix + 1, ry + 1, ix + iw - 1, ry + rowH - 1, bg);
            String id = items.get(i);
            class_1792 item = this.resolveItem(id);
            if (item != null && item != class_1802.field_8162) {
                g.method_51427(new class_1799((class_1935)item), ix + 4, ry + 3);
            }
            String shown = this.scrolledText(id, this.itemHScroll, iw - 30);
            VersionCompat.get().drawString(g, this.field_22793, shown, ix + 24, ry + 7, -2497806, false);
        }
        this.renderScrollBar(g, gx + gw - 4, gy, listH, this.order.size(), rows, this.groupScroll, accent);
        this.renderScrollBar(g, ix + iw - 4, iy, itemListH, items.size(), rows, this.itemScroll, accent);
        this.renderHorizontalBar(g, gx + 1, gy + listH + 2, gw - 6, this.maxGroupHScroll(gw), this.groupHScroll, accent);
        this.renderHorizontalBar(g, ix + 1, iy + itemListH + 2, iw - 6, this.maxItemHScroll(iw), this.itemHScroll, accent);
        if (this.status != null && !this.status.getString().isEmpty() && System.currentTimeMillis() - this.statusTime < 2400L) {
            VersionCompat.get().drawString(g, this.field_22793, this.status, this.px + 16, this.py + this.ph - 44, this.statusColor, false);
        }
    }

    private String safeName(String in) {
        return in == null ? "" : in.trim();
    }

    private void renderScrollBar(class_332 g, int x, int y, int h, int size, int rows, int scroll, int accent) {
        if (size <= rows) {
            return;
        }
        g.method_25294(x, y, x + 3, y + h, UiTheme.withAlpha(0xFFFFFF, 40));
        float ratio = (float)rows / (float)size;
        int thumbH = Math.max(18, (int)((float)h * ratio));
        int max = Math.max(1, size - rows);
        int thumbY = y + (int)((float)(h - thumbH) * ((float)scroll / (float)max));
        g.method_25294(x, thumbY, x + 3, thumbY + thumbH, UiTheme.withAlpha(accent & 0xFFFFFF, 188));
    }

    private int maxGroupScroll() {
        int rows = Math.max(1, (this.ph - 180) / 22);
        return Math.max(0, this.order.size() - rows);
    }

    private int maxItemScroll() {
        int rows = Math.max(1, (this.ph - 180) / 22);
        return Math.max(0, this.currentItems().size() - rows);
    }

    private void clampScroll() {
        this.groupScroll = Math.max(0, Math.min(this.groupScroll, this.maxGroupScroll()));
        this.itemScroll = Math.max(0, Math.min(this.itemScroll, this.maxItemScroll()));
        this.groupHScroll = Math.max(0, Math.min(this.groupHScroll, this.maxGroupHScroll(this.pw / 2 - 28)));
        this.itemHScroll = Math.max(0, Math.min(this.itemHScroll, this.maxItemHScroll(this.pw / 2 - 24)));
    }

    private int maxGroupHScroll(int gw) {
        int maxW = 0;
        for (String name : this.order) {
            maxW = Math.max(maxW, this.field_22793.method_1727(name));
        }
        return Math.max(0, maxW - (gw - 12));
    }

    private int maxItemHScroll(int iw) {
        int maxW = 0;
        for (String id : this.currentItems()) {
            maxW = Math.max(maxW, this.field_22793.method_1727(id));
        }
        return Math.max(0, maxW - (iw - 30));
    }

    private String scrolledText(String text, int scrollPx, int width) {
        if (text == null) {
            return "";
        }
        if (scrollPx <= 0) {
            return this.field_22793.method_1727(text) <= width ? text : this.field_22793.method_27523(text, Math.max(8, width - 4));
        }
        int start = 0;
        for (int consumed = 0; start < text.length() && consumed < scrollPx; consumed += this.field_22793.method_1727(String.valueOf(text.charAt(start))), ++start) {
        }
        String tail = start >= text.length() ? "" : text.substring(start);
        return this.field_22793.method_27523(tail, Math.max(8, width - 4));
    }

    private void renderHorizontalBar(class_332 g, int x, int y, int w, int max, int scroll, int accent) {
        if (max <= 0) {
            return;
        }
        g.method_25294(x, y, x + w, y + 3, UiTheme.withAlpha(0xFFFFFF, 40));
        int thumbW = Math.max(16, (int)((float)w * Math.max(0.12f, (float)w / (float)(w + max))));
        int thumbX = x + (int)((float)(w - thumbW) * ((float)scroll / (float)Math.max(1, max)));
        g.method_25294(thumbX, y, thumbX + thumbW, y + 3, UiTheme.withAlpha(accent & 0xFFFFFF, 188));
    }

    private class_1792 resolveItem(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        class_1792 cached = this.itemCache.get(id);
        if (cached != null) {
            return cached;
        }
        class_1792 found = ItemRegistryHelper.resolveItem(id);
        this.itemCache.put(id, found);
        return found;
    }

    private void border(class_332 g, int x, int y, int w, int h, int c) {
        g.method_25294(x, y, x + w, y + 1, c);
        g.method_25294(x, y + h - 1, x + w, y + h, c);
        g.method_25294(x, y, x + 1, y + h, c);
        g.method_25294(x + w - 1, y, x + w, y + h, c);
    }

    private void setStatus(class_2561 msg, int color) {
        this.status = msg;
        this.statusColor = color;
        this.statusTime = System.currentTimeMillis();
    }

    private String tr(String key) {
        return class_2561.method_43471((String)key).getString();
    }

    public void method_25419() {
        class_310.method_1551().method_1507(this.parent);
    }

    public boolean method_25421() {
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
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }

        boolean click(int mx, int my) {
            if (!this.enabled || !this.hover(mx, my)) {
                return false;
            }
            this.action.run();
            return true;
        }

        void render(class_332 g, class_327 font, int mx, int my, int accent) {
            boolean chosen;
            boolean hover = this.hover(mx, my);
            boolean bl = chosen = this.selected != null && Boolean.TRUE.equals(this.selected.get());
            int bg = !this.enabled ? 705697831 : (chosen ? 0xAA000000 | accent & 0xFFFFFF : (hover ? 1780954962 : 1243293240));
            int edge = chosen ? accent : -13878436;
            int color = this.enabled ? -2497806 : -7429177;
            g.method_25294(this.x, this.y, this.x + this.w, this.y + this.h, bg);
            g.method_25294(this.x, this.y, this.x + this.w, this.y + 1, edge);
            g.method_25294(this.x, this.y + this.h - 1, this.x + this.w, this.y + this.h, edge);
            g.method_25294(this.x, this.y, this.x + 1, this.y + this.h, edge);
            g.method_25294(this.x + this.w - 1, this.y, this.x + this.w, this.y + this.h, edge);
            Object text = this.label.get();
            if (font.method_1727((String)text) > this.w - 10) {
                text = font.method_27523((String)text, this.w - 14) + "..";
            }
            VersionCompat.get().drawString(g, font, (String)text, this.x + 6, this.y + 7, color, false);
        }
    }
}

