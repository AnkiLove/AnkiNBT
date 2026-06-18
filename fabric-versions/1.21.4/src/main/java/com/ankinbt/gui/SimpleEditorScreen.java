/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.class_1320
 *  net.minecraft.class_1322
 *  net.minecraft.class_1322$class_1323
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1814
 *  net.minecraft.class_1887
 *  net.minecraft.class_1890
 *  net.minecraft.class_1935
 *  net.minecraft.class_2487
 *  net.minecraft.class_2499
 *  net.minecraft.class_2520
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_5250
 *  net.minecraft.class_5251
 *  net.minecraft.class_5348
 *  net.minecraft.class_6880
 *  net.minecraft.class_6880$class_6883
 *  net.minecraft.class_9274
 *  net.minecraft.class_9282
 *  net.minecraft.class_9285
 *  net.minecraft.class_9285$class_9287
 *  net.minecraft.class_9290
 *  net.minecraft.class_9304
 *  net.minecraft.class_9304$class_9305
 *  net.minecraft.class_9331
 *  net.minecraft.class_9334
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.gui.ItemPickerScreen;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.nbt.NbtFileIO;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.util.FlatEditBox;
import com.ankinbt.util.ItemRegistryHelper;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.class_1320;
import net.minecraft.class_1322;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1814;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1935;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import net.minecraft.class_5348;
import net.minecraft.class_6880;
import net.minecraft.class_9274;
import net.minecraft.class_9282;
import net.minecraft.class_9285;
import net.minecraft.class_9290;
import net.minecraft.class_9304;
import net.minecraft.class_9331;
import net.minecraft.class_9334;

public class SimpleEditorScreen
extends class_437 {
    private static final int HEADER_H = 32;
    private static final int FOOTER_H = 20;
    private static final int MARGIN = 16;
    private static final int SIDEBAR_W = 140;
    private static final int SCROLLBAR_W = 6;
    private static final int ROW_H = 24;
    private static final int CAT_H = 28;
    private static final int BG = -670562288;
    private static final int SIDEBAR_BG = -670299112;
    private static final int HEADER_BG = -670035936;
    private static final int BORDER = -14540234;
    private static final int HOVER = 0x30FFFFFF;
    private static final int SELECT_BG = 677603057;
    private static final int ACCENT = -10262799;
    private static final int C1 = -1906448;
    private static final int C2 = -7035976;
    private static final int C3 = -10193781;
    private static final int SB_TRACK = 0x30FFFFFF;
    private static final int SB_THUMB = 0x70FFFFFF;
    private static final int BTN_BG = 0x30FFFFFF;
    private static final int BTN_HOVER = 0x50FFFFFF;
    private static final int SUCCESS = -14498466;
    private static final int ERROR_C = -1096636;
    private static final int CAT_BG = 0x20FFFFFF;
    private static final char SECTION = '\u00a7';
    private static final String[] MC_COLOR_CODES = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
    private static final String[] MC_COLOR_NAMES_ZH = new String[]{"\u9ed1\u8272", "\u6df1\u84dd", "\u6df1\u7eff", "\u6df1\u9752", "\u6df1\u7ea2", "\u7d2b\u8272", "\u91d1\u8272", "\u7070\u8272", "\u6df1\u7070", "\u84dd\u8272", "\u7eff\u8272", "\u9752\u8272", "\u7ea2\u8272", "\u7c89\u7ea2", "\u9ec4\u8272", "\u767d\u8272"};
    private static final int[] MC_COLORS = new int[]{-16777216, -16777046, -16733696, -16733526, -5636096, -5635926, -22016, -5592406, -11184811, -11184641, -11141291, -11141121, -43691, -43521, -171, -1};
    private static final String[] MC_FORMAT_CODES = new String[]{"k", "l", "m", "n", "o", "r"};
    private static final String[] MC_FORMAT_NAMES_ZH = new String[]{"\u968f\u673a", "\u7c97\u4f53", "\u5220\u9664\u7ebf", "\u4e0b\u5212\u7ebf", "\u659c\u4f53", "\u91cd\u7f6e"};
    private static final String[] MC_FORMAT_NAMES_EN = new String[]{"Obfuscated", "Bold", "Strikethrough", "Underline", "Italic", "Reset"};
    private static final String[] POTION_IDS = new String[]{"minecraft:water", "minecraft:awkward", "minecraft:mundane", "minecraft:thick", "minecraft:healing", "minecraft:strong_healing", "minecraft:harming", "minecraft:strong_harming", "minecraft:regeneration", "minecraft:long_regeneration", "minecraft:strong_regeneration", "minecraft:swiftness", "minecraft:long_swiftness", "minecraft:strong_swiftness", "minecraft:slowness", "minecraft:long_slowness", "minecraft:strong_slowness", "minecraft:strength", "minecraft:long_strength", "minecraft:strong_strength", "minecraft:leaping", "minecraft:long_leaping", "minecraft:strong_leaping", "minecraft:poison", "minecraft:long_poison", "minecraft:strong_poison", "minecraft:fire_resistance", "minecraft:long_fire_resistance", "minecraft:water_breathing", "minecraft:long_water_breathing", "minecraft:night_vision", "minecraft:long_night_vision", "minecraft:invisibility", "minecraft:long_invisibility", "minecraft:slow_falling", "minecraft:long_slow_falling", "minecraft:turtle_master", "minecraft:long_turtle_master", "minecraft:strong_turtle_master", "minecraft:weakness", "minecraft:long_weakness", "minecraft:luck"};
    private static final String[] EFFECT_IDS = new String[]{"minecraft:speed", "minecraft:slowness", "minecraft:haste", "minecraft:mining_fatigue", "minecraft:strength", "minecraft:instant_health", "minecraft:instant_damage", "minecraft:jump_boost", "minecraft:nausea", "minecraft:regeneration", "minecraft:resistance", "minecraft:fire_resistance", "minecraft:water_breathing", "minecraft:invisibility", "minecraft:blindness", "minecraft:night_vision", "minecraft:hunger", "minecraft:weakness", "minecraft:poison", "minecraft:wither", "minecraft:health_boost", "minecraft:absorption", "minecraft:saturation", "minecraft:glowing", "minecraft:levitation", "minecraft:luck", "minecraft:unluck", "minecraft:slow_falling", "minecraft:conduit_power", "minecraft:dolphins_grace", "minecraft:bad_omen", "minecraft:hero_of_the_village", "minecraft:darkness", "minecraft:trial_omen", "minecraft:raid_omen", "minecraft:wind_charged", "minecraft:weaving", "minecraft:oozing", "minecraft:infested"};
    private static final Map<String, String> ENCHANT_ZH = new LinkedHashMap<String, String>();
    private static final Map<String, String> ATTR_ZH;
    private static final Map<String, String> ATTR_NOTES_ZH;
    private static final Map<String, String> ATTR_NOTES_EN;
    private static final Map<String, String> SLOT_ZH;
    private static final String[] OP_NAMES_ZH;
    private static final String[] OP_NAMES_EN;
    private class_1799 editStack;
    private final class_1799 originalStack;
    private final int inventorySlot;
    private int px;
    private int py;
    private int pw;
    private int ph;
    private int sideX;
    private int sideY;
    private int sideW;
    private int sideH;
    private int contentX;
    private int contentY;
    private int contentW;
    private int contentH;
    private Category activeCat = Category.GENERAL;
    private int scrollOff = 0;
    private int maxRows;
    private int hoverRow = -1;
    private int sideScrollOff = 0;
    private String statusMsg = null;
    private long statusTime = 0L;
    private int statusColor = -10193781;
    private boolean dirty = false;
    private float openAnim = 0.0f;
    private SubEditor activeSubEditor = null;
    private final List<Btn> headerBtns = new ArrayList<Btn>();
    private static final Method ITEMSTACK_GET_COMPONENT_METHOD;
    private static final Method ITEMSTACK_HAS_COMPONENT_METHOD;
    private static final Method ITEMSTACK_REMOVE_COMPONENT_METHOD;
    private static final Method ITEMSTACK_SET_COMPONENT_METHOD;

    private static void putAttrNote(String id, String zh, String en) {
        for (String key : SimpleEditorScreen.attrNoteKeys(id)) {
            ATTR_NOTES_ZH.put(key, zh);
            ATTR_NOTES_EN.put(key, en);
        }
    }

    private static List<String> attrNoteKeys(String id) {
        return List.of("minecraft:" + id, "minecraft:generic." + id, "minecraft:player." + id, "minecraft:horse." + id, "minecraft:zombie." + id);
    }

    public SimpleEditorScreen(class_1799 stack) {
        this(stack, -1);
    }

    public SimpleEditorScreen(class_1799 stack, int inventorySlot) {
        super((class_2561)class_2561.method_43471((String)"ankinbt.simple.title"));
        this.originalStack = stack;
        this.editStack = stack.method_7972();
        this.inventorySlot = inventorySlot;
    }

    protected void method_25426() {
        super.method_25426();
        int edge = this.field_22789 < 480 ? 6 : 16;
        int availableW = Math.max(240, this.field_22789 - edge * 2);
        int availableH = Math.max(220, this.field_22790 - edge * 2);
        this.pw = Math.min(availableW, 760);
        this.ph = Math.min(availableH, 460);
        this.px = (this.field_22789 - this.pw) / 2;
        this.py = (this.field_22790 - this.ph) / 2;
        this.sideX = this.px + 1;
        this.sideY = this.py + 32 + 1;
        this.sideW = Math.min(140, Math.max(96, this.pw / 4));
        this.sideH = this.ph - 32 - 20 - 2;
        this.contentX = this.px + this.sideW + 2;
        this.contentY = this.py + 32 + 1;
        this.contentW = this.pw - this.sideW - 6 - 6;
        this.contentH = this.ph - 32 - 20 - 2;
        this.maxRows = this.contentH / 24;
        this.buildHeaderButtons();
    }

    private void buildHeaderButtons() {
        this.headerBtns.clear();
        int bw = 22;
        int gap = 3;
        int by = this.py + 6;
        int bx = this.px + this.pw - 16 - 2;
        this.headerBtns.add(new Btn(bx -= bw, by, bw, bw, "X", (class_2561)class_2561.method_43471((String)"ankinbt.btn.close"), this::tryClose));
        bx -= bw + gap;
        int saveW = 40;
        this.headerBtns.add(new Btn(bx -= saveW + gap, by, saveW, bw, class_2561.method_43471((String)"ankinbt.btn.save").getString(), (class_2561)class_2561.method_43471((String)"ankinbt.btn.save.tip"), this::saveToItem));
        int invW = 42;
        this.headerBtns.add(new Btn(bx -= invW + gap, by, invW, bw, class_2561.method_43471((String)"ankinbt.btn.inventory").getString(), (class_2561)class_2561.method_43471((String)"ankinbt.btn.switch_inventory"), this::openInventorySwitch));
        int modeW = 50;
        this.headerBtns.add(new Btn(bx -= modeW + gap + 4, by, modeW, bw, class_2561.method_43471((String)"ankinbt.btn.advanced").getString(), (class_2561)class_2561.method_43471((String)"ankinbt.btn.switch_advanced"), this::switchToAdvanced));
    }

    public void method_25394(class_332 g, int mx, int my, float pt) {
        float cfgSpeed = AnkiConfig.getUiAnimationSpeed();
        float speed = Math.max(0.06f, Math.min(0.16f, cfgSpeed));
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        int scrimAlpha = Math.max(70, Math.min(130, Math.round(112.0f * this.openAnim)));
        int panel = this.fadeColor(-670562288, this.openAnim);
        int header = this.fadeColor(-670035936, this.openAnim);
        int border = this.fadeColor(-14540234, this.openAnim);
        g.method_25294(0, 0, this.field_22789, this.field_22790, scrimAlpha << 24);
        g.method_25294(this.px, this.py, this.px + this.pw, this.py + this.ph, panel);
        this.drawBorder(g, this.px, this.py, this.pw, this.ph, border);
        g.method_25294(this.px + 1, this.py + 1, this.px + this.pw - 1, this.py + 32, header);
        g.method_25294(this.px + 1, this.py + 32, this.px + this.pw - 1, this.py + 32 + 1, border);
        VersionCompat.get().drawString(g, this.field_22793, "AnkiNBT", this.px + 16, this.py + 11, 0xFFE2E8F0, false);
        VersionCompat.get().drawString(g, this.field_22793, "简单模式", this.px + 64, this.py + 11, 0xFF38BDF8, false);
        if (this.dirty) {
            VersionCompat.get().drawString(g, this.field_22793, "*", this.px + 116, this.py + 12, -1096636, false);
        }
        for (Btn b : this.headerBtns) {
            b.render(g, this.field_22793, mx, my);
        }
        this.renderSidebar(g, mx, my);
        g.method_25294(this.px + this.sideW + 1, this.py + 32 + 1, this.px + this.sideW + 2, this.py + this.ph - 20, border);
        if (this.activeSubEditor != null) {
            this.activeSubEditor.render(g, this.field_22793, mx, my, this.contentX, this.contentY, this.contentW, this.contentH);
        } else {
            this.renderCategoryContent(g, mx, my);
        }
        g.method_25294(this.px + 1, this.py + this.ph - 20, this.px + this.pw - 1, this.py + this.ph - 20 + 1, border);
        this.renderFooter(g);
    }

    private int fadeColor(int color, float factor) {
        int a = color >>> 24 & 0xFF;
        int alpha = Math.max(0, Math.min(255, Math.round((float)a * factor)));
        return alpha << 24 | color & 0xFFFFFF;
    }

    private void renderSidebar(class_332 g, int mx, int my) {
        g.method_25294(this.sideX, this.sideY, this.sideX + this.sideW, this.sideY + this.sideH, -670299112);
        int lx = this.sideX + 8;
        int headerY = this.sideY + 8;
        g.method_51427(this.editStack, lx + (this.sideW - 32) / 2, headerY);
        headerY += 24;
        Object name = this.editStack.method_7964().getString();
        if (this.field_22793.method_1727((String)name) > this.sideW - 16) {
            name = this.field_22793.method_27523((String)name, this.sideW - 22) + "...";
        }
        VersionCompat.get().drawString(g, this.field_22793, (String)name, lx, headerY, -1906448, false);
        g.method_25294(lx, headerY += 14, this.sideX + this.sideW - 8, headerY + 1, -14540234);
        int catAreaY = headerY += 8;
        int catAreaH = this.sideY + this.sideH - catAreaY;
        Category[] cats = Category.values();
        String[] catNames = new String[]{class_2561.method_43471((String)"ankinbt.cat.general").getString(), class_2561.method_43471((String)"ankinbt.cat.enchant").getString(), class_2561.method_43471((String)"ankinbt.cat.lore").getString(), class_2561.method_43471((String)"ankinbt.cat.attribute").getString(), class_2561.method_43471((String)"ankinbt.cat.visual").getString(), class_2561.method_43471((String)"ankinbt.cat.misc").getString()};
        int totalCatH = cats.length * 30;
        int maxSideScroll = Math.max(0, totalCatH - catAreaH);
        this.sideScrollOff = Math.max(0, Math.min(this.sideScrollOff, maxSideScroll));
        g.method_44379(this.sideX, catAreaY, this.sideX + this.sideW, this.sideY + this.sideH);
        for (int i = 0; i < cats.length; ++i) {
            boolean active;
            int cy = catAreaY + i * 30 - this.sideScrollOff;
            if (cy + 28 < catAreaY || cy > this.sideY + this.sideH) continue;
            int cw = this.sideW - 16;
            boolean hover = mx >= lx && mx < lx + cw && my >= cy && my < cy + 28 && my >= catAreaY && my < this.sideY + this.sideH;
            boolean bl = active = cats[i] == this.activeCat;
            g.method_25294(lx, cy, lx + cw, cy + 28, active ? -10262799 : (hover ? 0x50FFFFFF : 0x20FFFFFF));
            if (active) {
                g.method_25294(lx, cy, lx + 2, cy + 28, -1);
            }
            VersionCompat.get().drawString(g, this.field_22793, catNames[i], lx + 8, cy + 10, active ? -1906448 : -7035976, false);
        }
        g.method_44380();
        if (totalCatH > catAreaH) {
            int sbx = this.sideX + this.sideW - 5;
            g.method_25294(sbx, catAreaY, sbx + 4, this.sideY + this.sideH, 0x30FFFFFF);
            float ratio = (float)catAreaH / (float)totalCatH;
            int thumbH = Math.max(12, (int)((float)catAreaH * ratio));
            float sr = (float)this.sideScrollOff / (float)Math.max(1, maxSideScroll);
            int thumbY = catAreaY + (int)((float)(catAreaH - thumbH) * sr);
            g.method_25294(sbx, thumbY, sbx + 4, thumbY + thumbH, 0x70FFFFFF);
        }
    }

    private void renderCategoryContent(class_332 g, int mx, int my) {
        List<ActionRow> rows = this.getRowsForCategory(this.activeCat);
        this.hoverRow = -1;
        int end = Math.min(this.scrollOff + this.maxRows, rows.size());
        for (int i = this.scrollOff; i < end; ++i) {
            boolean hovered;
            int ry = this.contentY + (i - this.scrollOff) * 24;
            ActionRow row = rows.get(i);
            boolean bl = hovered = mx >= this.contentX && mx < this.contentX + this.contentW && my >= ry && my < ry + 24;
            if (hovered) {
                this.hoverRow = i;
                g.method_25294(this.contentX, ry, this.contentX + this.contentW, ry + 24, 0x30FFFFFF);
            }
            g.method_25294(this.contentX, ry + 24 - 1, this.contentX + this.contentW, ry + 24, 0x10FFFFFF);
            VersionCompat.get().drawString(g, this.field_22793, row.label, this.contentX + 8, ry + 8, row.labelColor, false);
            int rightX = this.contentX + this.contentW - 8;
            if (row.moveUp != null || row.moveDown != null) {
                int btnW = 16;
                int btnH = 16;
                int btnY = ry + (24 - btnH) / 2;
                if (row.moveDown != null) {
                    boolean dHover = mx >= (rightX -= btnW + 2) && mx < rightX + btnW && my >= btnY && my < btnY + btnH;
                    g.method_25294(rightX, btnY, rightX + btnW, btnY + btnH, dHover ? 0x50FFFFFF : 0x30FFFFFF);
                    VersionCompat.get().drawString(g, this.field_22793, "v", rightX + (btnW - this.field_22793.method_1727("v")) / 2, btnY + 4, dHover ? -1906448 : -10193781, false);
                }
                if (row.moveUp != null) {
                    boolean uHover = mx >= (rightX -= btnW + 2) && mx < rightX + btnW && my >= btnY && my < btnY + btnH;
                    g.method_25294(rightX, btnY, rightX + btnW, btnY + btnH, uHover ? 0x50FFFFFF : 0x30FFFFFF);
                    VersionCompat.get().drawString(g, this.field_22793, "^", rightX + (btnW - this.field_22793.method_1727("^")) / 2, btnY + 4, uHover ? -1906448 : -10193781, false);
                }
                rightX -= 4;
            }
            if (row.currentValue == null) continue;
            Object val = row.currentValue;
            int maxValW = rightX - (this.contentX + this.contentW / 2);
            if (this.field_22793.method_1727((String)val) > maxValW) {
                val = this.field_22793.method_27523((String)val, maxValW - 10) + "..";
            }
            VersionCompat.get().drawString(g, this.field_22793, (String)val, rightX - this.field_22793.method_1727((String)val), ry + 8, -7035976, false);
        }
        if (rows.size() > this.maxRows) {
            int sbx = this.px + this.pw - 6 - 3;
            g.method_25294(sbx, this.contentY, sbx + 6, this.contentY + this.contentH, 0x30FFFFFF);
            float ratio = (float)this.maxRows / (float)rows.size();
            int thumbH = Math.max(16, (int)((float)this.contentH * ratio));
            float sr = (float)this.scrollOff / (float)Math.max(1, rows.size() - this.maxRows);
            int thumbY = this.contentY + (int)((float)(this.contentH - thumbH) * sr);
            g.method_25294(sbx, thumbY, sbx + 6, thumbY + thumbH, 0x70FFFFFF);
        }
    }

    private void renderFooter(class_332 g) {
        int fy = this.py + this.ph - 20 + 5;
        if (this.statusMsg != null && System.currentTimeMillis() - this.statusTime < 3000L) {
            VersionCompat.get().drawString(g, this.field_22793, this.statusMsg, this.px + this.sideW + 8, fy, this.statusColor, false);
        } else {
            this.statusMsg = null;
            VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.simple.hint"), this.px + this.sideW + 8, fy, -10193781, false);
        }
    }

    public boolean method_25402(double mx, double my, int btn) {
        List<ActionRow> rows;
        for (Btn b : this.headerBtns) {
            if (!b.isHover((int)mx, (int)my)) continue;
            b.action.run();
            return true;
        }
        if (this.activeSubEditor != null) {
            return this.activeSubEditor.mouseClicked(mx, my, btn, this.contentX, this.contentY, this.contentW, this.contentH);
        }
        int lx = this.sideX + 8;
        int catStartY = this.sideY + 8 + 24 + 14 + 1 + 8;
        int catAreaBottom = this.sideY + this.sideH;
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; ++i) {
            int cy = catStartY + i * 30 - this.sideScrollOff;
            int cw = this.sideW - 16;
            if (cy + 28 < catStartY || cy > catAreaBottom || !(mx >= (double)lx) || !(mx < (double)(lx + cw)) || !(my >= (double)cy) || !(my < (double)(cy + 28)) || !(my >= (double)catStartY) || !(my < (double)catAreaBottom)) continue;
            this.activeCat = cats[i];
            this.scrollOff = 0;
            return true;
        }
        if (this.hoverRow >= 0 && this.hoverRow < (rows = this.getRowsForCategory(this.activeCat)).size()) {
            ActionRow row = rows.get(this.hoverRow);
            if (row.moveUp != null || row.moveDown != null) {
                int ry = this.contentY + (this.hoverRow - this.scrollOff) * 24;
                int btnW = 16;
                int btnH = 16;
                int btnY = ry + (24 - btnH) / 2;
                int rightX = this.contentX + this.contentW - 8;
                if (row.moveDown != null && mx >= (double)(rightX -= btnW + 2) && mx < (double)(rightX + btnW) && my >= (double)btnY && my < (double)(btnY + btnH)) {
                    row.moveDown.run();
                    return true;
                }
                if (row.moveUp != null && mx >= (double)(rightX -= btnW + 2) && mx < (double)(rightX + btnW) && my >= (double)btnY && my < (double)(btnY + btnH)) {
                    row.moveUp.run();
                    return true;
                }
            }
            row.action.run();
            return true;
        }
        return super.method_25402(mx, my, btn);
    }

    public boolean method_25401(double mx, double my, double sx, double sy) {
        if (this.activeSubEditor != null) {
            return this.activeSubEditor.mouseScrolled(sx, sy);
        }
        if (mx >= (double)this.sideX && mx < (double)(this.sideX + this.sideW) && my >= (double)this.sideY && my < (double)(this.sideY + this.sideH)) {
            this.sideScrollOff -= (int)sy * 10;
            Category[] cats = Category.values();
            int catAreaY = this.sideY + 8 + 24 + 14 + 1 + 8;
            int catAreaH = this.sideY + this.sideH - catAreaY;
            int totalCatH = cats.length * 30;
            int maxSideScroll = Math.max(0, totalCatH - catAreaH);
            this.sideScrollOff = Math.max(0, Math.min(this.sideScrollOff, maxSideScroll));
            return true;
        }
        List<ActionRow> rows = this.getRowsForCategory(this.activeCat);
        this.scrollOff -= (int)sy * 3;
        this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, rows.size() - this.maxRows)));
        return true;
    }

    public boolean method_25404(int key, int scan, int mod) {
        if (this.activeSubEditor != null) {
            if (key == 256) {
                this.activeSubEditor = null;
                return true;
            }
            return this.activeSubEditor.keyPressed(key, scan, mod);
        }
        if (key == 256) {
            this.tryClose();
            return true;
        }
        if (key == 83 && (mod & 2) != 0) {
            this.saveToItem();
            return true;
        }
        return super.method_25404(key, scan, mod);
    }

    private void tryClose() {
        if (this.dirty && AnkiConfig.isConfirmOnClose()) {
            this.activeSubEditor = new ConfirmCloseSubEditor();
        } else {
            this.method_25419();
        }
    }

    public boolean method_25400(char c, int mod) {
        if (this.activeSubEditor != null) {
            return this.activeSubEditor.charTyped(c, mod);
        }
        return super.method_25400(c, mod);
    }

    private List<ActionRow> getRowsForCategory(Category cat) {
        return switch (cat.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.getGeneralRows();
            case 1 -> this.getEnchantRows();
            case 2 -> this.getLoreRows();
            case 3 -> this.getAttributeRows();
            case 4 -> this.getVisualRows();
            case 5 -> this.getMiscRows();
        };
    }

    private List<ActionRow> getGeneralRows() {
        class_1814 rarity;
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        String nameVal = this.editStack.method_7964().getString();
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.rename"), nameVal, () -> this.openInlineEditor("rename", nameVal)));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.count"), String.valueOf(this.editStack.method_7947()), () -> this.openInlineEditor("count", String.valueOf(this.editStack.method_7947()))));
        int maxDmg = this.editStack.method_7936();
        if (maxDmg > 0) {
            int dmg = this.editStack.method_7919();
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.damage"), dmg + " / " + maxDmg, () -> this.openInlineEditor("damage", String.valueOf(this.editStack.method_7919()))));
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.max_damage"), String.valueOf(maxDmg), () -> this.openInlineEditor("max_damage", String.valueOf(maxDmg))));
        }
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.unbreakable"), this.isUnbreakable() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleUnbreakable));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.max_stack"), String.valueOf(this.editStack.method_7914()), () -> this.openInlineEditor("max_stack", String.valueOf(this.editStack.method_7914()))));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.repair_cost"), String.valueOf(this.getRepairCost()), () -> this.openInlineEditor("repair_cost", String.valueOf(this.getRepairCost()))));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.fire_resistant"), this.isFireResistant() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleFireResistant));
        if (VersionCompat.get().hasFood(this.editStack)) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.food_nutrition"), String.valueOf(VersionCompat.get().getFoodNutrition(this.editStack)), () -> this.openInlineEditor("food_nutrition", String.valueOf(VersionCompat.get().getFoodNutrition(this.editStack)))));
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.food_saturation"), String.valueOf(VersionCompat.get().getFoodSaturation(this.editStack)), () -> this.openInlineEditor("food_saturation", String.valueOf(VersionCompat.get().getFoodSaturation(this.editStack)))));
        }
        if ((rarity = (class_1814)this.getComponent(class_9334.field_50073)) != null) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.rarity"), this.getRarityDisplayName(rarity), () -> this.cycleRarity()));
        }
        if (this.isPotionLike()) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.potion_base"), this.potionDisplayName(this.getPotionId()), () -> {
                this.activeSubEditor = new PotionPickerSubEditor();
            }, -10262799));
            int color = this.getPotionCustomColor();
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.potion_custom_color"), color >= 0 ? String.format("#%06X", color & 0xFFFFFF) : SimpleEditorScreen.tr("ankinbt.simple.none"), () -> {
                this.activeSubEditor = new ColorPickerSubEditor(-3);
            }, -10262799));
            if (color >= 0) {
                rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.potion_clear_color"), null, this::clearPotionCustomColor, -1096636));
            }
            int effects = this.getPotionCustomEffectCount();
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.potion_effects"), String.valueOf(effects), () -> {
                this.activeSubEditor = new PotionEffectSubEditor();
            }, -10262799));
            if (effects > 0) {
                rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.potion_clear_effects"), null, this::clearPotionCustomEffects, -1096636));
            }
        }
        return rows;
    }

    private List<ActionRow> getEnchantRows() {
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        class_9304 enchants = class_1890.method_57532((class_1799)this.editStack);
        enchants.method_57539().forEach(entry -> {
            class_6880 ench = (class_6880)entry.getKey();
            int level = entry.getIntValue();
            String eId = ench.method_40230().map(k -> k.method_29177().toString()).orElse("?");
            String displayName = this.getEnchantDisplayName(eId);
            rows.add(new ActionRow(displayName, SimpleEditorScreen.tr("ankinbt.simple.level") + level, () -> this.openInlineEditor("ench_level:" + eId, String.valueOf(level))));
        });
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.add_enchant"), null, () -> {
            this.activeSubEditor = new EnchantPickerSubEditor();
        }, -10262799));
        if (!enchants.method_57543()) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.clear_enchants"), null, this::clearEnchantments, -1096636));
        }
        return rows;
    }

    private List<ActionRow> getLoreRows() {
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.lore_color_hint"), null, () -> {
            this.activeSubEditor = new ColorPickerSubEditor(-1);
        }, -10193781));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.lore_text_editor"), null, () -> {
            this.activeSubEditor = new LoreTextEditorSubEditor();
        }, -10262799));
        List<class_2561> lore = this.getLore();
        for (int i = 0; i < lore.size(); ++i) {
            int idx = i;
            Object text = lore.get(i).getString();
            if (((String)text).length() > 30) {
                text = ((String)text).substring(0, 27) + "...";
            }
            String prefix = i + 1 + ". " + (String)text;
            String moveHint = "";
            if (lore.size() > 1) {
                if (i > 0 && i < lore.size() - 1) {
                    moveHint = "^ v";
                } else if (i > 0) {
                    moveHint = "^";
                } else if (i < lore.size() - 1) {
                    moveHint = "v";
                }
            }
            int fi = i;
            int loreSize = lore.size();
            rows.add(new ActionRow(prefix, moveHint.isEmpty() ? null : moveHint, () -> this.openLoreEditor("lore:" + fi, this.getLoreRawText(fi)), -1906448, fi > 0 ? () -> this.moveLore(fi, fi - 1) : null, fi < loreSize - 1 ? () -> this.moveLore(fi, fi + 1) : null));
        }
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.add_lore"), null, () -> this.openLoreEditor("lore_add", ""), -10262799));
        if (!lore.isEmpty()) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.remove_last_lore"), null, this::removeLastLore));
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.clear_lore"), null, this::clearLore, -1096636));
        }
        return rows;
    }

    private List<ActionRow> getAttributeRows() {
        String displayName;
        String attrId;
        class_9285.class_9287 entry;
        int idx;
        int i;
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        class_9285 attrComp = this.getComponentOrDefault(class_9334.field_49636, class_9285.field_49326);
        List entries = attrComp.comp_2393();
        for (i = 0; i < entries.size(); ++i) {
            String note;
            idx = i;
            entry = (class_9285.class_9287)entries.get(i);
            attrId = entry.comp_2395().method_40230().map(k -> k.method_29177().toString()).orElse("?");
            displayName = this.getAttrDisplayName(attrId);
            double amount = entry.comp_2396().comp_2449();
            String opName = this.getOpName(entry.comp_2396().comp_2450());
            String slotName = this.getSlotDisplayName(entry.comp_2397());
            String valueStr = String.format("%.2f %s [%s]", amount, opName, slotName);
            rows.add(new ActionRow(displayName, valueStr, () -> this.openInlineEditor("attr_amount:" + idx, String.valueOf(amount))));
            if (!AnkiConfig.isAttributeNotesEnabled() || (note = this.getAttrNote(attrId)) == null || note.isBlank()) continue;
            rows.add(new ActionRow("i  " + note, null, () -> {}, -10193781));
        }
        if (!entries.isEmpty()) {
            for (i = 0; i < entries.size(); ++i) {
                idx = i;
                entry = (class_9285.class_9287)entries.get(i);
                attrId = entry.comp_2395().method_40230().map(k -> k.method_29177().toString()).orElse("?");
                displayName = this.getAttrDisplayName(attrId);
                rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.remove_attr") + " " + displayName, null, () -> this.removeAttribute(idx), -1096636));
            }
        }
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.add_attr"), null, () -> {
            this.activeSubEditor = new AttributePickerSubEditor();
        }, -10262799));
        if (!entries.isEmpty()) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.clear_attrs"), null, this::clearAttributes, -1096636));
        }
        return rows;
    }

    private String getAttrDisplayName(String attrId) {
        String zh;
        String translated = this.resolveAttributeDisplayName(attrId);
        if (translated != null) {
            return translated;
        }
        if (this.isZhLanguage() && (zh = this.findAttrText(ATTR_ZH, attrId)) != null) {
            return zh;
        }
        return this.prettifyRegistryId(attrId);
    }

    private String getAttrNote(String attrId) {
        Map<String, String> map = this.isZhLanguage() ? ATTR_NOTES_ZH : ATTR_NOTES_EN;
        return this.findAttrText(map, attrId);
    }

    private boolean isZhLanguage() {
        String lang = class_310.method_1551().field_1690.field_1883;
        return lang != null && lang.startsWith("zh");
    }

    private String findAttrText(Map<String, String> map, String attrId) {
        if (map == null || attrId == null) {
            return null;
        }
        String direct = map.get(attrId);
        if (direct != null) {
            return direct;
        }
        String normalized = this.normalizeAttrId(attrId);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        String exact = map.get("minecraft:" + normalized);
        if (exact != null) {
            return exact;
        }
        for (String prefix : new String[]{"generic.", "player.", "horse.", "zombie."}) {
            String value = map.get("minecraft:" + prefix + normalized);
            if (value == null) continue;
            return value;
        }
        return null;
    }

    private String normalizeAttrId(String attrId) {
        String id = attrId;
        int colon = id.indexOf(58);
        if (colon >= 0 && colon + 1 < id.length()) {
            id = id.substring(colon + 1);
        }
        for (String prefix : new String[]{"generic.", "player.", "horse.", "zombie."}) {
            if (!id.startsWith(prefix)) continue;
            return id.substring(prefix.length());
        }
        return id;
    }

    private String getOpName(class_1322.class_1323 op) {
        String lang = class_310.method_1551().field_1690.field_1883;
        boolean zh = lang != null && lang.startsWith("zh");
        return switch (op) {
            default -> throw new MatchException(null, null);
            case class_1322.class_1323.field_6328 -> {
                if (zh) {
                    yield OP_NAMES_ZH[0];
                }
                yield OP_NAMES_EN[0];
            }
            case class_1322.class_1323.field_6330 -> {
                if (zh) {
                    yield OP_NAMES_ZH[1];
                }
                yield OP_NAMES_EN[1];
            }
            case class_1322.class_1323.field_6331 -> zh ? OP_NAMES_ZH[2] : OP_NAMES_EN[2];
        };
    }

    private String getSlotDisplayName(class_9274 slot) {
        String zh;
        String name = slot.method_15434();
        String lang = class_310.method_1551().field_1690.field_1883;
        if (lang != null && lang.startsWith("zh") && (zh = SLOT_ZH.get(name)) != null) {
            return zh;
        }
        return name;
    }

    private String getRarityDisplayName(class_1814 rarity) {
        boolean zh;
        if (rarity == null) {
            return "";
        }
        String lang = class_310.method_1551().field_1690.field_1883;
        boolean bl = zh = lang != null && lang.startsWith("zh");
        if (zh) {
            return switch (rarity) {
                default -> throw new MatchException(null, null);
                case class_1814.field_8906 -> "\u666e\u901a";
                case class_1814.field_8907 -> "\u7f55\u89c1";
                case class_1814.field_8903 -> "\u7a00\u6709";
                case class_1814.field_8904 -> "\u53f2\u8bd7";
            };
        }
        return switch (rarity) {
            default -> throw new MatchException(null, null);
            case class_1814.field_8906 -> "Common";
            case class_1814.field_8907 -> "Uncommon";
            case class_1814.field_8903 -> "Rare";
            case class_1814.field_8904 -> "Epic";
        };
    }

    private boolean isPotionLike() {
        String id = this.resolveStackRegistryId(this.editStack);
        return id.contains("potion") || id.contains("tipped_arrow") || this.getPotionContentsTag() != null;
    }

    private class_2487 getPotionContentsTag() {
        Optional<class_2487> opt = NbtHelper.serializeItemStack(this.editStack);
        if (opt.isEmpty()) {
            return null;
        }
        class_2487 components = this.getCompoundTag(opt.get(), "components");
        return components == null ? null : this.getCompoundTag(components, "minecraft:potion_contents");
    }

    private String getPotionId() {
        class_2487 potion = this.getPotionContentsTag();
        String id = this.readStringTag(potion, "potion", "");
        return id.isBlank() ? "minecraft:water" : id;
    }

    private int getPotionCustomColor() {
        class_2487 potion = this.getPotionContentsTag();
        return this.readIntTag(potion, "custom_color", -1);
    }

    private int getPotionCustomEffectCount() {
        class_2487 potion = this.getPotionContentsTag();
        class_2499 effects = this.getListTag(potion, "custom_effects");
        return effects == null ? 0 : effects.size();
    }

    private void setPotionBase(String potionId) {
        if (potionId == null || potionId.isBlank()) {
            return;
        }
        this.updatePotionContents(potion -> potion.method_10582("potion", potionId));
    }

    private void setPotionCustomColor(int rgb) {
        this.updatePotionContents(potion -> potion.method_10569("custom_color", rgb & 0xFFFFFF));
    }

    private void clearPotionCustomColor() {
        this.updatePotionContents(potion -> this.removeTagKey((class_2487)potion, "custom_color"));
    }

    private void clearPotionCustomEffects() {
        this.updatePotionContents(potion -> this.removeTagKey((class_2487)potion, "custom_effects"));
    }

    private void addPotionCustomEffect(String effectId, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        if (effectId == null || effectId.isBlank()) {
            return;
        }
        this.updatePotionContents(potion -> {
            class_2499 list = this.getListTag((class_2487)potion, "custom_effects");
            if (list == null) {
                list = new class_2499();
                potion.method_10566("custom_effects", (class_2520)list);
            }
            class_2487 effect = new class_2487();
            effect.method_10582("id", effectId);
            effect.method_10569("duration", Math.max(1, duration));
            effect.method_10569("amplifier", Math.max(0, amplifier));
            effect.method_10556("ambient", ambient);
            effect.method_10556("show_particles", particles);
            effect.method_10556("show_icon", icon);
            list.add((Object)effect);
        });
    }

    private void updatePotionContents(Consumer<class_2487> updater) {
        try {
            Optional<class_2487> opt = NbtHelper.serializeItemStack(this.editStack);
            if (opt.isEmpty()) {
                this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.potion_edit_failed"), -1096636);
                return;
            }
            class_2487 full = this.copyCompoundTag(opt.get());
            class_2487 components = this.getOrCreateCompoundTag(full, "components");
            class_2487 potion = this.getCompoundTag(components, "minecraft:potion_contents");
            if (potion == null) {
                potion = new class_2487();
            }
            updater.accept(potion);
            components.method_10566("minecraft:potion_contents", (class_2520)potion);
            Optional<class_1799> out = NbtHelper.deserializeItemStack(full);
            if (out.isPresent() && !out.get().method_7960()) {
                this.editStack = out.get();
                this.markDirty();
            } else {
                this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.potion_edit_failed"), -1096636);
            }
        }
        catch (Throwable t) {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.potion_edit_failed"), -1096636);
        }
    }

    private String potionDisplayName(String id) {
        String name = id == null || id.isBlank() ? "minecraft:water" : id;
        String key = "item.minecraft." + name.replace("minecraft:", "").replace(':', '.');
        String translated = class_2561.method_43471((String)key).getString();
        if (!translated.equals(key)) {
            return translated;
        }
        return this.prettifyRegistryId(name);
    }

    private String effectDisplayName(String id) {
        String name = id == null || id.isBlank() ? "minecraft:speed" : id;
        String key = "effect.minecraft." + name.replace("minecraft:", "").replace(':', '.');
        String translated = class_2561.method_43471((String)key).getString();
        if (!translated.equals(key)) {
            return translated;
        }
        return this.prettifyRegistryId(name);
    }

    private class_2487 copyCompoundTag(class_2487 source) {
        if (source == null) {
            return new class_2487();
        }
        class_2487 copy = new class_2487();
        copy.method_10543(source);
        return copy;
    }

    private class_2487 getCompoundTag(class_2487 parent, String key) {
        class_2487 tag;
        Object raw = this.getTagValue(parent, key);
        return raw instanceof class_2487 ? (tag = (class_2487)raw) : null;
    }

    private class_2487 getOrCreateCompoundTag(class_2487 parent, String key) {
        class_2487 tag = this.getCompoundTag(parent, key);
        if (tag == null) {
            tag = new class_2487();
            parent.method_10566(key, (class_2520)tag);
        }
        return tag;
    }

    private class_2499 getListTag(class_2487 parent, String key) {
        class_2499 list;
        Object raw = this.getTagValue(parent, key);
        return raw instanceof class_2499 ? (list = (class_2499)raw) : null;
    }

    private Object getTagValue(class_2487 parent, String key) {
        if (parent == null || key == null || key.isBlank()) {
            return null;
        }
        try {
            Object out = parent.getClass().getMethod("get", String.class).invoke((Object)parent, key);
            if (out instanceof Optional) {
                Optional opt = (Optional)out;
                return opt.orElse(null);
            }
            return out;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private String readStringTag(class_2487 parent, String key, String def) {
        if (parent == null) {
            return def;
        }
        try {
            Optional opt;
            Object var7_9;
            Object out = parent.getClass().getMethod("getString", String.class).invoke((Object)parent, key);
            if (out instanceof String) {
                String s = (String)out;
                return s;
            }
            if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof String) {
                String s = var7_9;
                return s;
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        Object raw = this.getTagValue(parent, key);
        if (raw != null) {
            try {
                Object v = raw.getClass().getMethod("getAsString", new Class[0]).invoke(raw, new Object[0]);
                if (v instanceof String) {
                    String s = (String)v;
                    return s;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return def;
    }

    private int readIntTag(class_2487 parent, String key, int def) {
        if (parent == null) {
            return def;
        }
        try {
            Optional opt;
            Object var7_9;
            Object out = parent.getClass().getMethod("getInt", String.class).invoke((Object)parent, key);
            if (out instanceof Number) {
                Number n = (Number)out;
                return n.intValue();
            }
            if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof Number) {
                Number n = var7_9;
                return n.intValue();
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        Object raw = this.getTagValue(parent, key);
        if (raw != null) {
            try {
                Object v = raw.getClass().getMethod("getAsInt", new Class[0]).invoke(raw, new Object[0]);
                if (v instanceof Number) {
                    Number n = (Number)v;
                    return n.intValue();
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return def;
    }

    private void removeTagKey(class_2487 parent, String key) {
        if (parent == null || key == null || key.isBlank()) {
            return;
        }
        try {
            parent.getClass().getMethod("remove", String.class).invoke((Object)parent, key);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void removeAttribute(int index) {
        class_9285 attrComp = this.getComponentOrDefault(class_9334.field_49636, class_9285.field_49326);
        ArrayList<class_9285.class_9287> entries = new ArrayList<class_9285.class_9287>(attrComp.comp_2393());
        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
            this.setComponent(class_9334.field_49636, VersionCompat.get().withEntries(entries, attrComp));
            this.markDirty();
        }
    }

    private void clearAttributes() {
        this.setComponent(class_9334.field_49636, class_9285.field_49326);
        this.markDirty();
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.attrs_cleared"), -7035976);
    }

    private void addAttribute(String attrId, double amount, class_1322.class_1323 op, class_9274 slot) {
        Optional<class_6880.class_6883<class_1320>> holder = VersionCompat.get().getAttributeHolder(attrId);
        if (holder.isEmpty()) {
            return;
        }
        class_9285 attrComp = this.getComponentOrDefault(class_9334.field_49636, class_9285.field_49326);
        ArrayList<class_9285.class_9287> entries = new ArrayList<class_9285.class_9287>(attrComp.comp_2393());
        class_2960 modId = class_2960.method_60655((String)"ankinbt", (String)("custom_" + System.currentTimeMillis()));
        entries.add(new class_9285.class_9287((class_6880)holder.get(), new class_1322(modId, amount, op), slot));
        this.setComponent(class_9334.field_49636, VersionCompat.get().withEntries(entries, attrComp));
        this.dirty = true;
        this.activeSubEditor = null;
        this.setStatus(class_2561.method_43469((String)"ankinbt.status.added", (Object[])new Object[]{this.getAttrDisplayName(attrId)}).getString(), -14498466);
    }

    private List<ActionRow> getVisualRows() {
        class_9282 dyeColor;
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.custom_model_data"), String.valueOf(this.getCustomModelData()), () -> this.openInlineEditor("custom_model_data", String.valueOf(this.getCustomModelData()))));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.enchant_glint"), this.hasEnchantGlint() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleEnchantGlint));
        if (VersionCompat.get().hasHideTooltipFeature()) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.hide_tooltip"), this.isHideTooltip() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleHideTooltip));
        }
        if (VersionCompat.get().hasHideAdditionalFeature()) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.hide_additional"), this.isHideAdditional() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleHideAdditional));
        }
        if ((dyeColor = (class_9282)this.getComponent(class_9334.field_49644)) != null || this.isLeatherArmor()) {
            int color = dyeColor != null ? dyeColor.comp_2384() : 10511680;
            String hex = String.format("#%06X", color & 0xFFFFFF);
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.dye_color"), hex, () -> this.openInlineEditor("dye_color", hex)));
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.dye_color_picker"), null, () -> {
                this.activeSubEditor = new ColorPickerSubEditor(color);
            }, -10262799));
        }
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.name_color"), null, () -> {
            this.activeSubEditor = new ColorPickerSubEditor(-2);
        }, -10262799));
        return rows;
    }

    private List<ActionRow> getMiscRows() {
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.copy_nbt"), null, this::copyNbtToClipboard));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.copy_give_cmd"), null, this::copyGiveCommand));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.export_nbt"), null, () -> {
            this.activeSubEditor = new NbtExportSubEditor();
        }, -10262799));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.import_nbt"), null, () -> {
            this.activeSubEditor = new NbtImportSubEditor();
        }, -10262799));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.container_preview"), null, () -> {
            this.activeSubEditor = new ContainerPreviewSubEditor();
        }, -10262799));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.reset"), null, this::resetItem, -1096636));
        return rows;
    }

    private <T> T getComponent(class_9331<T> type) {
        if (ITEMSTACK_GET_COMPONENT_METHOD == null) {
            return null;
        }
        try {
            return (T)ITEMSTACK_GET_COMPONENT_METHOD.invoke((Object)this.editStack, type);
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private <T> T getComponentOrDefault(class_9331<T> type, T fallback) {
        T value = this.getComponent(type);
        return value != null ? value : fallback;
    }

    private boolean hasComponent(class_9331<?> type) {
        if (ITEMSTACK_HAS_COMPONENT_METHOD == null) {
            return false;
        }
        try {
            Boolean b;
            Object out = ITEMSTACK_HAS_COMPONENT_METHOD.invoke((Object)this.editStack, type);
            return out instanceof Boolean && (b = (Boolean)out) != false;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private <T> void setComponent(class_9331<T> type, T value) {
        if (ITEMSTACK_SET_COMPONENT_METHOD == null) {
            return;
        }
        try {
            ITEMSTACK_SET_COMPONENT_METHOD.invoke((Object)this.editStack, type, value);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void removeComponent(class_9331<?> type) {
        if (ITEMSTACK_REMOVE_COMPONENT_METHOD == null) {
            return;
        }
        try {
            ITEMSTACK_REMOVE_COMPONENT_METHOD.invoke((Object)this.editStack, type);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Method findItemStackComponentMethod(String ... names) {
        for (String name : names) {
            try {
                return class_1799.class.getMethod(name, class_9331.class);
            }
            catch (Throwable throwable) {
            }
        }
        return null;
    }

    private static Method findItemStackSetComponentMethod(String ... names) {
        HashSet<String> nameSet = new HashSet<String>(Arrays.asList(names));
        for (Method method : class_1799.class.getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (!nameSet.contains(method.getName()) || parameterTypes.length != 2 || parameterTypes[0] != class_9331.class) continue;
            return method;
        }
        return null;
    }

    private boolean isUnbreakable() {
        return this.hasComponent(class_9334.field_49630);
    }

    private void toggleUnbreakable() {
        VersionCompat.get().setUnbreakable(this.editStack, !this.isUnbreakable());
        this.markDirty();
    }

    private boolean isFireResistant() {
        return VersionCompat.get().isFireResistant(this.editStack);
    }

    private void toggleFireResistant() {
        VersionCompat.get().setFireResistant(this.editStack, !this.isFireResistant());
        this.markDirty();
    }

    private int getRepairCost() {
        Integer c = (Integer)this.getComponent(class_9334.field_49639);
        return c != null ? c : 0;
    }

    private int getCustomModelData() {
        return VersionCompat.get().getCustomModelData(this.editStack);
    }

    private boolean hasEnchantGlint() {
        Boolean g = (Boolean)this.getComponent(class_9334.field_49641);
        return g != null && g != false;
    }

    private void toggleEnchantGlint() {
        Boolean cur = (Boolean)this.getComponent(class_9334.field_49641);
        if (cur != null && cur.booleanValue()) {
            this.removeComponent(class_9334.field_49641);
        } else {
            this.setComponent(class_9334.field_49641, true);
        }
        this.markDirty();
    }

    private boolean isHideTooltip() {
        return VersionCompat.get().isHideTooltip(this.editStack);
    }

    private void toggleHideTooltip() {
        VersionCompat.get().setHideTooltip(this.editStack, !this.isHideTooltip());
        this.markDirty();
    }

    private boolean isHideAdditional() {
        return VersionCompat.get().isHideAdditional(this.editStack);
    }

    private void toggleHideAdditional() {
        VersionCompat.get().setHideAdditional(this.editStack, !this.isHideAdditional());
        this.markDirty();
    }

    private boolean isLeatherArmor() {
        String id = this.editStack.method_7909().method_40131().method_40237().method_29177().toString();
        return id.contains("leather_");
    }

    private void cycleRarity() {
        class_1814 cur = (class_1814)this.getComponent(class_9334.field_50073);
        if (cur == null) {
            cur = class_1814.field_8906;
        }
        class_1814 next = switch (cur) {
            default -> throw new MatchException(null, null);
            case class_1814.field_8906 -> class_1814.field_8907;
            case class_1814.field_8907 -> class_1814.field_8903;
            case class_1814.field_8903 -> class_1814.field_8904;
            case class_1814.field_8904 -> class_1814.field_8906;
        };
        this.setComponent(class_9334.field_50073, next);
        this.markDirty();
    }

    private List<class_2561> getLore() {
        class_9290 lc = (class_9290)this.getComponent(class_9334.field_49632);
        return lc == null ? List.of() : lc.comp_2400();
    }

    private String getLoreRawText(int idx) {
        List<class_2561> lore = this.getLore();
        if (idx < 0 || idx >= lore.size()) {
            return "";
        }
        return this.componentToColorCoded(lore.get(idx));
    }

    private String componentToColorCoded(class_2561 comp) {
        StringBuilder sb = new StringBuilder();
        comp.method_27658((style, text) -> {
            class_5251 color = style.method_10973();
            if (color != null) {
                int rgb = color.method_27716();
                boolean found = false;
                for (int i = 0; i < MC_COLORS.length; ++i) {
                    if ((MC_COLORS[i] & 0xFFFFFF) != (rgb & 0xFFFFFF)) continue;
                    sb.append('&').append(MC_COLOR_CODES[i]);
                    found = true;
                    break;
                }
                if (!found) {
                    sb.append("&#").append(String.format("%06x", rgb & 0xFFFFFF));
                }
            }
            if (style.method_10984()) {
                sb.append("&l");
            }
            if (style.method_10966()) {
                sb.append("&o");
            }
            if (style.method_10965()) {
                sb.append("&n");
            }
            if (style.method_10986()) {
                sb.append("&m");
            }
            if (style.method_10987()) {
                sb.append("&k");
            }
            sb.append(text);
            return Optional.empty();
        }, class_2583.field_24360);
        return sb.toString();
    }

    static class_2561 colorCodedToComponent(String input) {
        String processed = input;
        class_5250 result = class_2561.method_43473();
        int i = 0;
        class_2583 currentStyle = class_2583.field_24360.method_10978(Boolean.valueOf(false));
        while (i < processed.length()) {
            class_2583 newStyle;
            char code;
            if (processed.charAt(i) == '&' && i + 1 < processed.length()) {
                code = processed.charAt(i + 1);
                if (code == '#' && i + 8 <= processed.length()) {
                    try {
                        String hex = processed.substring(i + 2, i + 8);
                        int rgb = Integer.parseInt(hex, 16);
                        currentStyle = class_2583.field_24360.method_27703(class_5251.method_27717((int)rgb));
                        i += 8;
                        continue;
                    }
                    catch (Exception hex) {
                        // empty catch block
                    }
                }
                if ((newStyle = SimpleEditorScreen.applyColorCode(currentStyle, code)) != null) {
                    currentStyle = newStyle;
                    i += 2;
                    continue;
                }
            }
            if (processed.charAt(i) == '\u00a7' && i + 1 < processed.length() && (newStyle = SimpleEditorScreen.applyColorCode(currentStyle, code = processed.charAt(i + 1))) != null) {
                currentStyle = newStyle;
                i += 2;
                continue;
            }
            int start = i++;
            while (i < processed.length() && processed.charAt(i) != '&' && processed.charAt(i) != '\u00a7') {
                ++i;
            }
            result.method_10852((class_2561)class_2561.method_43470((String)processed.substring(start, i)).method_27696(currentStyle));
        }
        return result;
    }

    private static class_2583 applyColorCode(class_2583 style, char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0));
            case '1' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)170));
            case '2' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)43520));
            case '3' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)43690));
            case '4' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0xAA0000));
            case '5' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0xAA00AA));
            case '6' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0xFFAA00));
            case '7' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0xAAAAAA));
            case '8' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0x555555));
            case '9' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0x5555FF));
            case 'a' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0x55FF55));
            case 'b' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0x55FFFF));
            case 'c' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0xFF5555));
            case 'd' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0xFF55FF));
            case 'e' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0xFFFF55));
            case 'f' -> class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)0xFFFFFF));
            case 'k' -> style.method_36141(Boolean.valueOf(true));
            case 'l' -> style.method_10982(Boolean.valueOf(true));
            case 'm' -> style.method_36140(Boolean.valueOf(true));
            case 'n' -> style.method_30938(Boolean.valueOf(true));
            case 'o' -> style.method_10978(Boolean.valueOf(true));
            case 'r' -> class_2583.field_24360.method_10978(Boolean.valueOf(false));
            default -> null;
        };
    }

    private void setLore(List<class_2561> lines) {
        this.setComponent(class_9334.field_49632, new class_9290(lines));
        this.dirty = true;
    }

    private void moveLore(int from, int to) {
        ArrayList<class_2561> lore = new ArrayList<class_2561>(this.getLore());
        if (from < 0 || from >= lore.size() || to < 0 || to >= lore.size()) {
            return;
        }
        class_2561 moved = (class_2561)lore.remove(from);
        lore.add(to, moved);
        this.setLore(lore);
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.lore_moved"), -7035976);
    }

    private void removeLastLore() {
        ArrayList<class_2561> lore = new ArrayList<class_2561>(this.getLore());
        if (!lore.isEmpty()) {
            lore.remove(lore.size() - 1);
            this.setLore(lore);
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.deleted"), -7035976);
        }
    }

    private void clearLore() {
        this.removeComponent(class_9334.field_49632);
        this.dirty = true;
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.lore_cleared"), -7035976);
    }

    private void clearEnchantments() {
        this.setComponent(class_9334.field_49633, class_9304.field_49385);
        this.dirty = true;
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.enchants_cleared"), -7035976);
    }

    private void copyNbtToClipboard() {
        Optional<class_2487> opt = NbtHelper.serializeItemStack(this.editStack);
        if (opt.isPresent()) {
            class_310.method_1551().field_1774.method_1455(opt.get().toString());
            this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.nbt_copied"), -14498466);
        }
    }

    private void copyGiveCommand() {
        Optional<class_2487> opt = NbtHelper.serializeItemStack(this.editStack);
        if (opt.isPresent()) {
            String id = this.editStack.method_7909().method_40131().method_40237().method_29177().toString();
            String cmd = "/give @s " + id + " " + this.editStack.method_7947();
            class_310.method_1551().field_1774.method_1455(cmd);
            this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.cmd_copied"), -14498466);
        }
    }

    private void resetItem() {
        this.editStack = this.originalStack.method_7972();
        this.dirty = false;
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.reset_done"), -7035976);
    }

    private String getEnchantDisplayName(String enchId) {
        String zh;
        String translated = this.resolveEnchantDisplayName(enchId);
        if (translated != null) {
            return translated;
        }
        String lang = class_310.method_1551().field_1690.field_1883;
        if (lang != null && lang.startsWith("zh") && (zh = ENCHANT_ZH.get(enchId)) != null) {
            return zh;
        }
        return this.prettifyRegistryId(enchId);
    }

    private String resolveAttributeDisplayName(String attrId) {
        try {
            Optional<class_6880.class_6883<class_1320>> holder = VersionCompat.get().getAttributeHolder(attrId);
            if (holder.isEmpty()) {
                return null;
            }
            Object attribute = holder.get().comp_349();
            for (String methodName : new String[]{"getDescriptionId", "descriptionId"}) {
                try {
                    String translated;
                    String key;
                    Object out = attribute.getClass().getMethod(methodName, new Class[0]).invoke(attribute, new Object[0]);
                    if (!(out instanceof String) || (key = (String)out).isBlank() || (translated = class_2561.method_43471((String)key).getString()).isBlank() || translated.equals(key)) continue;
                    return translated;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private String resolveEnchantDisplayName(String enchId) {
        try {
            String translated;
            Optional<class_6880.class_6883<class_1887>> holder = VersionCompat.get().getEnchantHolder(enchId);
            if (holder.isEmpty()) {
                return null;
            }
            class_2561 description = this.invokeComponent(holder.get().comp_349(), "description", "getDescription");
            if (description != null && !(translated = description.getString()).isBlank()) {
                return translated;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private class_2561 invokeComponent(Object target, String ... methodNames) {
        if (target == null || methodNames == null) {
            return null;
        }
        for (String methodName : methodNames) {
            try {
                Object out = target.getClass().getMethod(methodName, new Class[0]).invoke(target, new Object[0]);
                if (!(out instanceof class_2561)) continue;
                class_2561 component = (class_2561)out;
                return component;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private String prettifyRegistryId(String id) {
        String name = id == null ? "" : id;
        int idx = name.indexOf(58);
        if (idx >= 0 && idx + 1 < name.length()) {
            name = name.substring(idx + 1);
        }
        return name.replace("generic.", "").replace('_', ' ');
    }

    private void openInlineEditor(String field, String currentValue) {
        this.activeSubEditor = new InlineFieldEditor(field, currentValue, false);
    }

    private void openLoreEditor(String field, String currentValue) {
        this.activeSubEditor = new InlineFieldEditor(field, currentValue, true);
    }

    private void applyInlineEdit(String field, String value, boolean isLore) {
        try {
            if (field.equals("rename")) {
                if (value.contains("&") || value.contains(String.valueOf('\u00a7'))) {
                    class_2561 comp = SimpleEditorScreen.colorCodedToComponent(value);
                    class_5250 result = class_2561.method_43473().method_27696(class_2583.field_24360.method_10978(Boolean.valueOf(false)));
                    result.method_10852(comp);
                    this.setComponent(class_9334.field_49631, result);
                } else {
                    this.setComponent(class_9334.field_49631, class_2561.method_43470((String)value).method_27696(class_2583.field_24360.method_10978(Boolean.valueOf(false))));
                }
            } else if (field.equals("count")) {
                this.editStack.method_7939(Math.max(1, Math.min(99, Integer.parseInt(value))));
            } else if (field.equals("damage")) {
                this.editStack.method_7974(Math.max(0, Integer.parseInt(value)));
            } else if (field.equals("max_damage")) {
                this.setComponent(class_9334.field_50072, Math.max(1, Integer.parseInt(value)));
            } else if (field.equals("max_stack")) {
                this.setComponent(class_9334.field_50071, Math.max(1, Math.min(99, Integer.parseInt(value))));
            } else if (field.equals("repair_cost")) {
                this.setComponent(class_9334.field_49639, Math.max(0, Integer.parseInt(value)));
            } else if (field.equals("custom_model_data")) {
                VersionCompat.get().setCustomModelData(this.editStack, Integer.parseInt(value));
            } else if (field.equals("dye_color")) {
                String hex = value.startsWith("#") ? value.substring(1) : value;
                int rgb = Integer.parseInt(hex, 16);
                VersionCompat.get().setDyedColor(this.editStack, rgb);
            } else if (field.equals("food_nutrition")) {
                VersionCompat.get().setFoodNutrition(this.editStack, Integer.parseInt(value));
            } else if (field.equals("food_saturation")) {
                VersionCompat.get().setFoodSaturation(this.editStack, Float.parseFloat(value));
            } else if (field.startsWith("lore:")) {
                int idx = Integer.parseInt(field.substring(5));
                ArrayList<class_2561> lore = new ArrayList<class_2561>(this.getLore());
                if (idx >= 0 && idx < lore.size()) {
                    lore.set(idx, (class_2561)(isLore ? SimpleEditorScreen.colorCodedToComponent(value) : class_2561.method_43470((String)value)));
                    this.setLore(lore);
                }
            } else if (field.equals("lore_add")) {
                ArrayList<class_2561> lore = new ArrayList<class_2561>(this.getLore());
                lore.add((class_2561)(isLore ? SimpleEditorScreen.colorCodedToComponent(value) : class_2561.method_43470((String)value)));
                this.setLore(lore);
            } else if (field.startsWith("ench_level:")) {
                String enchId = field.substring(11);
                this.applyEnchantLevel(enchId, Integer.parseInt(value));
            } else if (field.startsWith("attr_amount:")) {
                int idx = Integer.parseInt(field.substring(12));
                class_9285 attrComp = this.getComponentOrDefault(class_9334.field_49636, class_9285.field_49326);
                ArrayList<class_9285.class_9287> entries = new ArrayList<class_9285.class_9287>(attrComp.comp_2393());
                if (idx >= 0 && idx < entries.size()) {
                    class_9285.class_9287 old = (class_9285.class_9287)entries.get(idx);
                    double newAmount = Double.parseDouble(value);
                    entries.set(idx, new class_9285.class_9287(old.comp_2395(), new class_1322(old.comp_2396().comp_2447(), newAmount, old.comp_2396().comp_2450()), old.comp_2397()));
                    this.setComponent(class_9334.field_49636, VersionCompat.get().withEntries(entries, attrComp));
                }
            }
            this.dirty = true;
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.edited"), -7035976);
        }
        catch (NumberFormatException e) {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.invalid_number"), -1096636);
        }
        this.activeSubEditor = null;
    }

    private void applyEnchantLevel(String enchId, int level) {
        class_2960 loc = class_2960.method_12829((String)enchId);
        if (loc == null) {
            return;
        }
        Optional<class_6880.class_6883<class_1887>> holder = VersionCompat.get().getEnchantHolder(enchId);
        if (holder.isEmpty()) {
            return;
        }
        class_9304.class_9305 mutable = new class_9304.class_9305(class_1890.method_57532((class_1799)this.editStack));
        if (level <= 0) {
            mutable.method_57548(h -> h.method_40230().map(k -> k.method_29177().equals((Object)loc)).orElse(false));
        } else {
            mutable.method_57547((class_6880)holder.get(), level);
        }
        this.setComponent(class_9334.field_49633, mutable.method_57549());
    }

    private void addEnchantment(String enchId, int level) {
        this.applyEnchantLevel(enchId, level);
        this.dirty = true;
        this.activeSubEditor = null;
        this.setStatus(class_2561.method_43469((String)"ankinbt.status.added", (Object[])new Object[]{this.getEnchantDisplayName(enchId)}).getString(), -14498466);
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
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        if (!mc.field_1724.method_7337()) {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.creative_only"), -1096636);
            return;
        }
        VersionCompat.get().sanitizeForCreativeSave(this.editStack);
        if (this.inventorySlot >= 0) {
            int creativeSlot = SimpleEditorScreen.creativePacketSlotFromEditedSlot(this.inventorySlot);
            if (creativeSlot < 0) {
                this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
                return;
            }
            int playerSlot = SimpleEditorScreen.playerInventoryIndexFromCreativeSlot(creativeSlot);
            if (playerSlot >= 0) {
                mc.field_1724.method_31548().method_5447(playerSlot, this.editStack.method_7972());
            }
            mc.field_1761.method_2909(this.editStack.method_7972(), creativeSlot);
        } else {
            int slot = VersionCompat.get().getSelectedSlot(mc.field_1724.method_31548());
            mc.field_1724.method_31548().method_5447(slot, this.editStack.method_7972());
            mc.field_1761.method_2909(this.editStack.method_7972(), 36 + slot);
        }
        this.dirty = false;
        this.setStatus(SimpleEditorScreen.tr("ankinbt.status.saved"), -14498466);
    }

    private void switchToAdvanced() {
        class_310.method_1551().method_1507((class_437)new NbtEditorScreen(this.editStack, this.inventorySlot));
    }

    private void openInventorySwitch() {
        this.activeSubEditor = new InventorySwitchSubEditor();
    }

    private void markDirty() {
        this.dirty = true;
        this.setStatus(SimpleEditorScreen.tr("ankinbt.status.edited"), -7035976);
    }

    private void setStatus(String msg, int color) {
        this.statusMsg = msg;
        this.statusColor = color;
        this.statusTime = System.currentTimeMillis();
    }

    private static String tr(String key) {
        return class_2561.method_43471((String)key).getString();
    }

    private int currentEditedSlot() {
        if (this.inventorySlot >= 0) {
            int creativeSlot = SimpleEditorScreen.creativePacketSlotFromEditedSlot(this.inventorySlot);
            int playerSlot = SimpleEditorScreen.playerInventoryIndexFromCreativeSlot(creativeSlot);
            return playerSlot >= 0 ? playerSlot : this.inventorySlot;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return -1;
        }
        return VersionCompat.get().getSelectedSlot(mc.field_1724.method_31548());
    }

    private void switchToInventorySlot(int slot) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.message.no_item"), -1096636);
            this.activeSubEditor = null;
            return;
        }
        if (slot < 0) {
            this.activeSubEditor = null;
            return;
        }
        class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
        if (stack == null || stack.method_7960()) {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.inventory_empty"), -1096636);
            this.activeSubEditor = null;
            return;
        }
        class_310.method_1551().method_1507((class_437)new SimpleEditorScreen(stack.method_7972(), slot));
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
        return null;
    }

    private Object[] tinyFdArgs(Class<?>[] parameterTypes, String defaultPath, boolean isOpen) {
        Object[] args = new Object[parameterTypes.length];
        int stringIndex = 0;
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> pt = parameterTypes[i];
            if (CharSequence.class.isAssignableFrom(pt) || pt == String.class) {
                args[i] = stringIndex == 0 ? (isOpen ? SimpleEditorScreen.tr("ankinbt.simple.import_nbt") : SimpleEditorScreen.tr("ankinbt.simple.export_nbt")) : (stringIndex == 1 ? defaultPath : "NBT files (*.nbt)");
                ++stringIndex;
                continue;
            }
            args[i] = pt == String[].class ? new String[]{"*.nbt"} : (pt == Boolean.TYPE || pt == Boolean.class ? (Comparable<Boolean>)Boolean.valueOf(false) : (Comparable<Boolean>)(pt == Integer.TYPE || pt == Integer.class ? Integer.valueOf(1) : (pt.getName().equals("org.lwjgl.PointerBuffer") ? null : null)));
        }
        return args;
    }

    private void drawBorder(class_332 g, int x, int y, int w, int h, int c) {
        g.method_25294(x, y, x + w, y + 1, c);
        g.method_25294(x, y + h - 1, x + w, y + h, c);
        g.method_25294(x, y, x + 1, y + h, c);
        g.method_25294(x + w - 1, y, x + w, y + h, c);
    }

    private int textViewStart(class_327 font, String value, int cursor, int maxWidth) {
        int start;
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int clampedCursor = Math.max(0, Math.min(cursor, value.length()));
        for (start = 0; start < clampedCursor && font.method_1727(value.substring(start, clampedCursor)) > maxWidth; ++start) {
        }
        return start;
    }

    private String visibleText(class_327 font, String value, int start, int maxWidth) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int safeStart = Math.max(0, Math.min(start, value.length()));
        String text = value.substring(safeStart);
        return font.method_1727(text) <= maxWidth ? text : font.method_27523(text, maxWidth);
    }

    private int plainCursorFromMouse(class_327 font, String value, int start, int relX, int maxWidth) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int safeStart = Math.max(0, Math.min(start, value.length()));
        String shown = this.visibleText(font, value, safeStart, maxWidth);
        int best = safeStart;
        for (int i = 0; i <= shown.length(); ++i) {
            int charW;
            String before = shown.substring(0, i);
            int n = charW = i < shown.length() ? font.method_1727(String.valueOf(shown.charAt(i))) : 8;
            if (font.method_1727(before) + Math.max(1, charW / 2) >= relX) {
                return safeStart + i;
            }
            best = safeStart + i;
        }
        return Math.max(0, Math.min(best, value.length()));
    }

    private int renderedCursorFromMouse(class_327 font, String raw, int relX, boolean rawMode) {
        if (raw == null || raw.isEmpty()) {
            return 0;
        }
        int visible = 0;
        for (int i = 0; i < raw.length(); ++i) {
            int codeLen;
            int n = codeLen = rawMode ? 0 : this.colorCodeLengthAt(raw, i);
            if (codeLen > 0) {
                i += codeLen - 1;
                continue;
            }
            int charW = font.method_1727(String.valueOf(raw.charAt(i)));
            if (visible + Math.max(1, charW / 2) >= relX) {
                return i;
            }
            visible += charW;
        }
        return raw.length();
    }

    private int renderedWidthBeforeCursor(class_327 font, String raw, int cursor, boolean rawMode) {
        if (raw == null || raw.isEmpty()) {
            return 0;
        }
        int width = 0;
        int end = Math.max(0, Math.min(cursor, raw.length()));
        for (int i = 0; i < end; ++i) {
            int codeLen;
            int n = codeLen = rawMode ? 0 : this.colorCodeLengthAt(raw, i);
            if (codeLen > 0) {
                i += codeLen - 1;
                continue;
            }
            width += font.method_1727(String.valueOf(raw.charAt(i)));
        }
        return width;
    }

    private int colorCodeLengthAt(String raw, int i) {
        if (raw == null || i + 1 >= raw.length()) {
            return 0;
        }
        char mark = raw.charAt(i);
        if (mark != '&' && mark != '\u00a7') {
            return 0;
        }
        char code = raw.charAt(i + 1);
        if (code == '#' && i + 7 < raw.length()) {
            return 8;
        }
        return "0123456789abcdefklmnorABCDEFKLMNOR".indexOf(code) >= 0 ? 2 : 0;
    }

    private String resolveStackRegistryId(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return "minecraft:air";
        }
        try {
            class_6880.class_6883 holder = stack.method_7909().method_40131();
            Object key = holder.getClass().getMethod("key", new Class[0]).invoke((Object)holder, new Object[0]);
            for (String method : new String[]{"location", "identifier"}) {
                try {
                    Object out = key.getClass().getMethod(method, new Class[0]).invoke(key, new Object[0]);
                    if (out == null) continue;
                    return out.toString();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return stack.method_7909().toString();
    }

    private String resolveStackRegistryPath(class_1799 stack) {
        String id = this.resolveStackRegistryId(stack);
        int idx = id.indexOf(58);
        return idx >= 0 && idx + 1 < id.length() ? id.substring(idx + 1) : id;
    }

    private void clearLoreComponent() {
        this.removeStackComponent(class_9334.field_49632);
    }

    private void setCustomNameComponent(class_2561 name) {
        this.setStackComponent(class_9334.field_49631, name);
    }

    private void setStackComponent(Object type, Object value) {
        if (this.invokeOuterComponentMethod("setComponent", type, value)) {
            return;
        }
        this.invokeStackComponentMethod("set", type, value);
    }

    private void removeStackComponent(Object type) {
        if (this.invokeOuterComponentMethod("removeComponent", type)) {
            return;
        }
        this.invokeStackComponentMethod("remove", type);
    }

    private boolean invokeOuterComponentMethod(String methodName, Object ... args) {
        for (Method method : ((Object)((Object)this)).getClass().getDeclaredMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) continue;
            try {
                method.setAccessible(true);
                method.invoke((Object)this, args);
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    private boolean invokeStackComponentMethod(String methodName, Object ... args) {
        for (Method method : this.editStack.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) continue;
            try {
                method.invoke((Object)this.editStack, args);
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    public boolean method_25421() {
        return false;
    }

    static {
        ENCHANT_ZH.put("minecraft:protection", "\u4fdd\u62a4");
        ENCHANT_ZH.put("minecraft:fire_protection", "\u706b\u7130\u4fdd\u62a4");
        ENCHANT_ZH.put("minecraft:feather_falling", "\u6454\u843d\u4fdd\u62a4");
        ENCHANT_ZH.put("minecraft:blast_protection", "\u7206\u70b8\u4fdd\u62a4");
        ENCHANT_ZH.put("minecraft:projectile_protection", "\u5f39\u5c04\u7269\u4fdd\u62a4");
        ENCHANT_ZH.put("minecraft:respiration", "\u6c34\u4e0b\u547c\u5438");
        ENCHANT_ZH.put("minecraft:aqua_affinity", "\u6c34\u4e0b\u901f\u6398");
        ENCHANT_ZH.put("minecraft:thorns", "\u8346\u68d8");
        ENCHANT_ZH.put("minecraft:depth_strider", "\u6df1\u6d77\u63a2\u7d22\u8005");
        ENCHANT_ZH.put("minecraft:frost_walker", "\u51b0\u971c\u884c\u8005");
        ENCHANT_ZH.put("minecraft:binding_curse", "\u7ed1\u5b9a\u8bc5\u5492");
        ENCHANT_ZH.put("minecraft:soul_speed", "\u7075\u9b42\u75be\u884c");
        ENCHANT_ZH.put("minecraft:swift_sneak", "\u8fc5\u6377\u6f5c\u884c");
        ENCHANT_ZH.put("minecraft:sharpness", "\u950b\u5229");
        ENCHANT_ZH.put("minecraft:smite", "\u4ea1\u7075\u6740\u624b");
        ENCHANT_ZH.put("minecraft:bane_of_arthropods", "\u8282\u80a2\u6740\u624b");
        ENCHANT_ZH.put("minecraft:knockback", "\u51fb\u9000");
        ENCHANT_ZH.put("minecraft:fire_aspect", "\u706b\u7130\u9644\u52a0");
        ENCHANT_ZH.put("minecraft:looting", "\u62a2\u593a");
        ENCHANT_ZH.put("minecraft:sweeping_edge", "\u6a2a\u626b\u4e4b\u5203");
        ENCHANT_ZH.put("minecraft:efficiency", "\u6548\u7387");
        ENCHANT_ZH.put("minecraft:silk_touch", "\u7cbe\u51c6\u91c7\u96c6");
        ENCHANT_ZH.put("minecraft:unbreaking", "\u8010\u4e45");
        ENCHANT_ZH.put("minecraft:fortune", "\u65f6\u8fd0");
        ENCHANT_ZH.put("minecraft:power", "\u529b\u91cf");
        ENCHANT_ZH.put("minecraft:punch", "\u51b2\u51fb");
        ENCHANT_ZH.put("minecraft:flame", "\u706b\u77e2");
        ENCHANT_ZH.put("minecraft:infinity", "\u65e0\u9650");
        ENCHANT_ZH.put("minecraft:luck_of_the_sea", "\u6d77\u4e4b\u7737\u987e");
        ENCHANT_ZH.put("minecraft:lure", "\u9975\u9493");
        ENCHANT_ZH.put("minecraft:loyalty", "\u5fe0\u8bda");
        ENCHANT_ZH.put("minecraft:impaling", "\u7a7f\u523a");
        ENCHANT_ZH.put("minecraft:riptide", "\u6fc0\u6d41");
        ENCHANT_ZH.put("minecraft:channeling", "\u5f15\u96f7");
        ENCHANT_ZH.put("minecraft:multishot", "\u591a\u91cd\u5c04\u51fb");
        ENCHANT_ZH.put("minecraft:quick_charge", "\u5feb\u901f\u88c5\u586b");
        ENCHANT_ZH.put("minecraft:piercing", "\u7a7f\u900f");
        ENCHANT_ZH.put("minecraft:density", "\u5bc6\u5ea6");
        ENCHANT_ZH.put("minecraft:breach", "\u7834\u7532");
        ENCHANT_ZH.put("minecraft:wind_burst", "\u98ce\u7206");
        ENCHANT_ZH.put("minecraft:mending", "\u7ecf\u9a8c\u4fee\u8865");
        ENCHANT_ZH.put("minecraft:vanishing_curse", "\u6d88\u5931\u8bc5\u5492");
        ATTR_ZH = new LinkedHashMap<String, String>();
        ATTR_ZH.put("minecraft:generic.max_health", "\u6700\u5927\u751f\u547d\u503c");
        ATTR_ZH.put("minecraft:max_health", "\u6700\u5927\u751f\u547d\u503c");
        ATTR_ZH.put("minecraft:generic.follow_range", "\u8ddf\u968f\u8303\u56f4");
        ATTR_ZH.put("minecraft:follow_range", "\u8ddf\u968f\u8303\u56f4");
        ATTR_ZH.put("minecraft:generic.knockback_resistance", "\u51fb\u9000\u6297\u6027");
        ATTR_ZH.put("minecraft:knockback_resistance", "\u51fb\u9000\u6297\u6027");
        ATTR_ZH.put("minecraft:generic.movement_speed", "\u79fb\u52a8\u901f\u5ea6");
        ATTR_ZH.put("minecraft:movement_speed", "\u79fb\u52a8\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.flying_speed", "\u98de\u884c\u901f\u5ea6");
        ATTR_ZH.put("minecraft:flying_speed", "\u98de\u884c\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.attack_damage", "\u653b\u51fb\u4f24\u5bb3");
        ATTR_ZH.put("minecraft:attack_damage", "\u653b\u51fb\u4f24\u5bb3");
        ATTR_ZH.put("minecraft:generic.attack_knockback", "\u653b\u51fb\u51fb\u9000");
        ATTR_ZH.put("minecraft:attack_knockback", "\u653b\u51fb\u51fb\u9000");
        ATTR_ZH.put("minecraft:generic.attack_speed", "\u653b\u51fb\u901f\u5ea6");
        ATTR_ZH.put("minecraft:attack_speed", "\u653b\u51fb\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.armor", "\u62a4\u7532\u503c");
        ATTR_ZH.put("minecraft:armor", "\u62a4\u7532\u503c");
        ATTR_ZH.put("minecraft:generic.armor_toughness", "\u62a4\u7532\u97e7\u6027");
        ATTR_ZH.put("minecraft:armor_toughness", "\u62a4\u7532\u97e7\u6027");
        ATTR_ZH.put("minecraft:generic.luck", "\u5e78\u8fd0\u503c");
        ATTR_ZH.put("minecraft:luck", "\u5e78\u8fd0\u503c");
        ATTR_ZH.put("minecraft:generic.max_absorption", "\u6700\u5927\u5438\u6536");
        ATTR_ZH.put("minecraft:max_absorption", "\u6700\u5927\u5438\u6536");
        ATTR_ZH.put("minecraft:generic.scale", "\u7f29\u653e");
        ATTR_ZH.put("minecraft:scale", "\u7f29\u653e");
        ATTR_ZH.put("minecraft:generic.step_height", "\u53f0\u9636\u9ad8\u5ea6");
        ATTR_ZH.put("minecraft:step_height", "\u53f0\u9636\u9ad8\u5ea6");
        ATTR_ZH.put("minecraft:generic.gravity", "\u91cd\u529b");
        ATTR_ZH.put("minecraft:gravity", "\u91cd\u529b");
        ATTR_ZH.put("minecraft:generic.safe_fall_distance", "\u5b89\u5168\u5760\u843d\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:safe_fall_distance", "\u5b89\u5168\u5760\u843d\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:generic.fall_damage_multiplier", "\u5760\u843d\u4f24\u5bb3\u500d\u7387");
        ATTR_ZH.put("minecraft:fall_damage_multiplier", "\u5760\u843d\u4f24\u5bb3\u500d\u7387");
        ATTR_ZH.put("minecraft:generic.jump_strength", "\u8df3\u8dc3\u529b\u91cf");
        ATTR_ZH.put("minecraft:horse.jump_strength", "\u8df3\u8dc3\u529b\u91cf");
        ATTR_ZH.put("minecraft:jump_strength", "\u8df3\u8dc3\u529b\u91cf");
        ATTR_ZH.put("minecraft:generic.block_interaction_range", "\u65b9\u5757\u4ea4\u4e92\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:player.block_interaction_range", "\u65b9\u5757\u4ea4\u4e92\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:block_interaction_range", "\u65b9\u5757\u4ea4\u4e92\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:generic.entity_interaction_range", "\u5b9e\u4f53\u4ea4\u4e92\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:player.entity_interaction_range", "\u5b9e\u4f53\u4ea4\u4e92\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:entity_interaction_range", "\u5b9e\u4f53\u4ea4\u4e92\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:generic.block_break_speed", "\u65b9\u5757\u7834\u574f\u901f\u5ea6");
        ATTR_ZH.put("minecraft:player.block_break_speed", "\u65b9\u5757\u7834\u574f\u901f\u5ea6");
        ATTR_ZH.put("minecraft:block_break_speed", "\u65b9\u5757\u7834\u574f\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.mining_efficiency", "\u6316\u6398\u6548\u7387");
        ATTR_ZH.put("minecraft:player.mining_efficiency", "\u6316\u6398\u6548\u7387");
        ATTR_ZH.put("minecraft:mining_efficiency", "\u6316\u6398\u6548\u7387");
        ATTR_ZH.put("minecraft:generic.sneaking_speed", "\u6f5c\u884c\u901f\u5ea6");
        ATTR_ZH.put("minecraft:player.sneaking_speed", "\u6f5c\u884c\u901f\u5ea6");
        ATTR_ZH.put("minecraft:sneaking_speed", "\u6f5c\u884c\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.submerged_mining_speed", "\u6c34\u4e0b\u6316\u6398\u901f\u5ea6");
        ATTR_ZH.put("minecraft:player.submerged_mining_speed", "\u6c34\u4e0b\u6316\u6398\u901f\u5ea6");
        ATTR_ZH.put("minecraft:submerged_mining_speed", "\u6c34\u4e0b\u6316\u6398\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.sweeping_damage_ratio", "\u6a2a\u626b\u4f24\u5bb3\u6bd4");
        ATTR_ZH.put("minecraft:player.sweeping_damage_ratio", "\u6a2a\u626b\u4f24\u5bb3\u6bd4");
        ATTR_ZH.put("minecraft:sweeping_damage_ratio", "\u6a2a\u626b\u4f24\u5bb3\u6bd4");
        ATTR_ZH.put("minecraft:burning_time", "\u71c3\u70e7\u65f6\u95f4");
        ATTR_ZH.put("minecraft:explosion_knockback_resistance", "\u7206\u70b8\u51fb\u9000\u6297\u6027");
        ATTR_ZH.put("minecraft:movement_efficiency", "\u79fb\u52a8\u6548\u7387");
        ATTR_ZH.put("minecraft:oxygen_bonus", "\u6c27\u6c14\u52a0\u6210");
        ATTR_ZH.put("minecraft:water_movement_efficiency", "\u6c34\u4e2d\u79fb\u52a8\u6548\u7387");
        ATTR_ZH.put("minecraft:tempt_range", "\u5f15\u8bf1\u8303\u56f4");
        ATTR_ZH.put("minecraft:zombie.spawn_reinforcements", "\u50f5\u5c38\u589e\u63f4\u6982\u7387");
        ATTR_NOTES_ZH = new LinkedHashMap<String, String>();
        ATTR_NOTES_EN = new LinkedHashMap<String, String>();
        SimpleEditorScreen.putAttrNote("max_health", "\u51b3\u5b9a\u5b9e\u4f53\u6700\u5927\u751f\u547d\u503c\u3002", "Controls maximum health.");
        SimpleEditorScreen.putAttrNote("movement_speed", "\u51b3\u5b9a\u5730\u9762\u79fb\u52a8\u901f\u5ea6\u3002", "Controls ground movement speed.");
        SimpleEditorScreen.putAttrNote("attack_damage", "\u51b3\u5b9a\u8fd1\u6218\u57fa\u7840\u4f24\u5bb3\u3002", "Controls base melee damage.");
        SimpleEditorScreen.putAttrNote("attack_speed", "\u51b3\u5b9a\u653b\u51fb\u51b7\u5374\u6062\u590d\u901f\u5ea6\u3002", "Controls attack cooldown speed.");
        SimpleEditorScreen.putAttrNote("armor", "\u63d0\u9ad8\u62a4\u7532\u51cf\u4f24\u3002", "Adds armor damage reduction.");
        SimpleEditorScreen.putAttrNote("armor_toughness", "\u964d\u4f4e\u9ad8\u4f24\u5bb3\u653b\u51fb\u5bf9\u62a4\u7532\u7684\u7a7f\u900f\u3002", "Reduces armor penetration from high damage.");
        SimpleEditorScreen.putAttrNote("luck", "\u5f71\u54cd\u5e26 quality \u6216 bonus_rolls \u7684\u6218\u5229\u54c1\u8868\uff1b\u539f\u7248\u6700\u660e\u663e\u662f\u9493\u9c7c\uff0c\u503c\u8d8a\u9ad8\u8d8a\u5bb9\u6613\u51fa\u9ad8\u8d28\u91cf\u7ed3\u679c\u3002", "Affects loot tables using quality or bonus_rolls. In vanilla it is most visible in fishing.");
        SimpleEditorScreen.putAttrNote("block_interaction_range", "\u73a9\u5bb6\u53ef\u4ea4\u4e92\u65b9\u5757\u7684\u8ddd\u79bb\u3002", "Player block interaction reach.");
        SimpleEditorScreen.putAttrNote("entity_interaction_range", "\u73a9\u5bb6\u53ef\u4ea4\u4e92\u5b9e\u4f53\u7684\u8ddd\u79bb\u3002", "Player entity interaction reach.");
        SimpleEditorScreen.putAttrNote("block_break_speed", "\u73a9\u5bb6\u7834\u574f\u65b9\u5757\u7684\u57fa\u7840\u901f\u5ea6\u3002", "Player base block breaking speed.");
        SimpleEditorScreen.putAttrNote("sneaking_speed", "\u73a9\u5bb6\u6f5c\u884c\u65f6\u79fb\u52a8\u901f\u5ea6\u3002", "Player movement speed while sneaking.");
        SimpleEditorScreen.putAttrNote("submerged_mining_speed", "\u73a9\u5bb6\u5728\u6c34\u4e0b\u6316\u6398\u901f\u5ea6\u3002", "Player mining speed while submerged.");
        SimpleEditorScreen.putAttrNote("scale", "\u6539\u53d8\u5b9e\u4f53\u663e\u793a\u5c3a\u5bf8\u3002", "Changes entity display scale.");
        SimpleEditorScreen.putAttrNote("gravity", "\u6539\u53d8\u5b9e\u4f53\u53d7\u5230\u7684\u91cd\u529b\u3002", "Changes gravity applied to the entity.");
        SimpleEditorScreen.putAttrNote("safe_fall_distance", "\u5f00\u59cb\u8ba1\u7b97\u5760\u843d\u4f24\u5bb3\u524d\u7684\u5b89\u5168\u8ddd\u79bb\u3002", "Safe distance before fall damage starts.");
        SimpleEditorScreen.putAttrNote("fall_damage_multiplier", "\u6539\u53d8\u5760\u843d\u4f24\u5bb3\u500d\u7387\u3002", "Changes fall damage multiplier.");
        SimpleEditorScreen.putAttrNote("jump_strength", "\u6539\u53d8\u8df3\u8dc3\u529b\u5ea6\u3002", "Changes jump strength.");
        SimpleEditorScreen.putAttrNote("knockback_resistance", "\u964d\u4f4e\u53d7\u5230\u51fb\u9000\u7684\u5e45\u5ea6\u3002", "Reduces received knockback.");
        SLOT_ZH = new LinkedHashMap<String, String>();
        SLOT_ZH.put("any", "\u4efb\u610f");
        SLOT_ZH.put("mainhand", "\u4e3b\u624b");
        SLOT_ZH.put("offhand", "\u526f\u624b");
        SLOT_ZH.put("head", "\u5934\u90e8");
        SLOT_ZH.put("chest", "\u80f8\u90e8");
        SLOT_ZH.put("legs", "\u817f\u90e8");
        SLOT_ZH.put("feet", "\u811a\u90e8");
        SLOT_ZH.put("hand", "\u624b\u6301");
        SLOT_ZH.put("armor", "\u62a4\u7532");
        OP_NAMES_ZH = new String[]{"\u589e\u52a0", "\u500d\u7387\u589e\u52a0", "\u500d\u7387\u4e58\u7b97"};
        OP_NAMES_EN = new String[]{"Add", "Multiply Base", "Multiply Total"};
        ITEMSTACK_GET_COMPONENT_METHOD = SimpleEditorScreen.findItemStackComponentMethod("get", "method_57824", "method_58694");
        ITEMSTACK_HAS_COMPONENT_METHOD = SimpleEditorScreen.findItemStackComponentMethod("has", "method_57826");
        ITEMSTACK_REMOVE_COMPONENT_METHOD = SimpleEditorScreen.findItemStackComponentMethod("remove", "method_57381");
        ITEMSTACK_SET_COMPONENT_METHOD = SimpleEditorScreen.findItemStackSetComponentMethod("set", "method_57379");
    }

    private static enum Category {
        GENERAL,
        ENCHANT,
        LORE,
        ATTRIBUTE,
        VISUAL,
        MISC;

    }

    static interface SubEditor {
        public void render(class_332 var1, class_327 var2, int var3, int var4, int var5, int var6, int var7, int var8);

        public boolean mouseClicked(double var1, double var3, int var5, int var6, int var7, int var8, int var9);

        public boolean keyPressed(int var1, int var2, int var3);

        public boolean charTyped(char var1, int var2);

        default public boolean mouseScrolled(double sx, double sy) {
            return false;
        }
    }

    static class Btn {
        final int x;
        final int y;
        final int w;
        final int h;
        final String label;
        final class_2561 tooltip;
        final Runnable action;

        Btn(int x, int y, int w, int h, String label, class_2561 tooltip, Runnable action) {
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

        void render(class_332 g, class_327 f, int mx, int my) {
            boolean hv = this.isHover(mx, my);
            g.method_25294(this.x, this.y, this.x + this.w, this.y + this.h, hv ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, f, this.label, this.x + (this.w - f.method_1727(this.label)) / 2, this.y + (this.h - 8) / 2, -1906448, false);
            if (hv && this.tooltip != null) {
                VersionCompat.get().renderTooltip(g, f, this.tooltip, mx, my);
            }
        }
    }

    static class ActionRow {
        final String label;
        final String currentValue;
        final Runnable action;
        final int labelColor;
        final Runnable moveUp;
        final Runnable moveDown;

        ActionRow(String label, String currentValue, Runnable action) {
            this(label, currentValue, action, -1906448, null, null);
        }

        ActionRow(String label, String currentValue, Runnable action, int labelColor) {
            this(label, currentValue, action, labelColor, null, null);
        }

        ActionRow(String label, String currentValue, Runnable action, int labelColor, Runnable moveUp, Runnable moveDown) {
            this.label = label;
            this.currentValue = currentValue;
            this.action = action;
            this.labelColor = labelColor;
            this.moveUp = moveUp;
            this.moveDown = moveDown;
        }
    }

    class ConfirmCloseSubEditor
    implements SubEditor {
        ConfirmCloseSubEditor() {
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int dw = 260;
            int dh = 110;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -1096636);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.confirm.title"), dx + 10, dy + 10, -1906448, false);
            g.method_25294(dx + 1, dy + 24, dx + dw - 1, dy + 25, -14540234);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.confirm.unsaved"), dx + 10, dy + 32, -7035976, false);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.confirm.discard_hint"), dx + 10, dy + 46, -10193781, false);
            int by = dy + dh - 32;
            int bw2 = 70;
            int bh2 = 22;
            int saveX = dx + 10;
            boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
            g.method_25294(saveX, by, saveX + bw2, by + bh2, sh ? -15293622 : -14498466);
            String saveLabel = SimpleEditorScreen.tr("ankinbt.confirm.save_close");
            VersionCompat.get().drawString(g, font, saveLabel, saveX + (bw2 - font.method_1727(saveLabel)) / 2, by + 7, -1906448, false);
            int discardX = dx + dw / 2 - bw2 / 2;
            boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
            g.method_25294(discardX, by, discardX + bw2, by + bh2, dh2 ? -2131803068 : 1089422404);
            String discardLabel = SimpleEditorScreen.tr("ankinbt.confirm.discard");
            VersionCompat.get().drawString(g, font, discardLabel, discardX + (bw2 - font.method_1727(discardLabel)) / 2, by + 7, -1906448, false);
            int cancelX = dx + dw - bw2 - 10;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.method_25294(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 7, -7035976, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = 260;
            int dh = 110;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int by = dy + dh - 32;
            int bw2 = 70;
            int bh2 = 22;
            int saveX = dx + 10;
            if (mx >= (double)saveX && mx < (double)(saveX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.saveToItem();
                SimpleEditorScreen.this.method_25419();
                return true;
            }
            int discardX = dx + dw / 2 - bw2 / 2;
            if (mx >= (double)discardX && mx < (double)(discardX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.dirty = false;
                SimpleEditorScreen.this.method_25419();
                return true;
            }
            int cancelX = dx + dw - bw2 - 10;
            if (mx >= (double)cancelX && mx < (double)(cancelX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 256) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            return false;
        }
    }

    class InlineFieldEditor
    implements SubEditor {
        final String field;
        final FlatEditBox inputBox;
        final String initialValue;
        String error = null;
        final boolean isLore;
        boolean initialCursorSynced = false;

        InlineFieldEditor(String field, String currentValue, boolean isLore) {
            this.field = field;
            this.isLore = isLore;
            this.initialValue = currentValue == null ? "" : currentValue;
            this.inputBox = new FlatEditBox(SimpleEditorScreen.this.field_22793, 0, 0, 1, 22, (class_2561)class_2561.method_43473());
            this.inputBox.method_1852(2048);
            this.inputBox.method_1852(this.initialValue);
            this.inputBox.method_1863(value -> this.error = null);
            this.inputBox.method_25365(true);
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            boolean colorEditable = this.isLore || this.field.equals("rename");
            int dw = Math.min(w - 20, 360);
            int dh = colorEditable ? 148 : 104;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            String title = this.getFieldLabel(this.field);
            VersionCompat.get().drawString(g, font, title, dx + 10, dy + 8, -1906448, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int ix = dx + 10;
            int iy = dy + 30;
            int iw = dw - 20;
            int ih = 22;
            this.inputBox.method_46421(ix);
            this.inputBox.method_46419(iy);
            this.inputBox.method_25358(iw);
            if (!this.initialCursorSynced) {
                this.inputBox.method_1852(this.initialValue);
                this.inputBox.method_1883(this.inputBox.method_1882().length(), false);
                this.inputBox.method_25365(true);
                this.initialCursorSynced = true;
            }
            this.inputBox.method_25365(true);
            this.inputBox.method_25394(g, mx, my, 0.0f);
            if (colorEditable && !this.inputBox.method_1882().isEmpty()) {
                class_2561 preview = SimpleEditorScreen.colorCodedToComponent(this.inputBox.method_1882());
                VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.preview") + ": ", ix, iy + ih + 4, -10193781, false);
                int previewX = ix + font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.preview") + ": ");
                VersionCompat.get().drawString(g, font, preview, previewX, iy + ih + 4, -1906448, false);
            }
            if (this.error != null) {
                VersionCompat.get().drawString(g, font, this.error, ix, iy + ih + (colorEditable ? 16 : 4), -1096636, false);
            }
            if (colorEditable) {
                int palX = dx + dw - 80;
                int palY = dy + 6;
                boolean palHover = mx >= palX && mx < palX + 70 && my >= palY && my < palY + 16;
                g.method_25294(palX, palY, palX + 70, palY + 16, palHover ? 0x50FFFFFF : 0x30FFFFFF);
                String palLabel = SimpleEditorScreen.tr("ankinbt.simple.color_palette");
                VersionCompat.get().drawString(g, font, palLabel, palX + (70 - font.method_1727(palLabel)) / 2, palY + 4, -7035976, false);
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
            g.method_25294(cancelX, by, cancelX + bw, by + bh, ch ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh;
            g.method_25294(okX, by, okX + bw, by + bh, oh ? -10262799 : -11581723);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.apply"), okX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.apply"))) / 2, by + 6, -1906448, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            boolean colorEditable = this.isLore || this.field.equals("rename");
            int dw = Math.min(w - 20, 360);
            int dh = colorEditable ? 148 : 104;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            if (colorEditable) {
                int palX = dx + dw - 80;
                int palY = dy + 6;
                if (mx >= (double)palX && mx < (double)(palX + 70) && my >= (double)palY && my < (double)(palY + 16)) {
                    SimpleEditorScreen.this.activeSubEditor = new LoreColorInsertEditor(this);
                    return true;
                }
            }
            int ix = dx + 10;
            int iy = dy + 30;
            int iw = dw - 20;
            this.inputBox.method_46421(ix);
            this.inputBox.method_46419(iy);
            this.inputBox.method_25358(iw);
            if (this.inputBox.method_25402(mx, my, btn)) {
                this.inputBox.method_25365(true);
                return true;
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            if (mx >= (double)cancelX && mx < (double)(cancelX + bw) && my >= (double)by && my < (double)(by + bh)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            int okX = dx + dw / 2 + 6;
            if (mx >= (double)okX && mx < (double)(okX + bw) && my >= (double)by && my < (double)(by + bh)) {
                this.apply();
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) {
                this.apply();
                return true;
            }
            if (this.inputBox.method_25404(key, scan, mod)) {
                this.error = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (this.inputBox.method_25400(c, mod)) {
                this.error = null;
                return true;
            }
            return true;
        }

        void insertAtCursor(String text) {
            this.inputBox.method_1853(text == null ? "" : text);
        }

        private void apply() {
            if (this.inputBox.method_1882().isEmpty() && !this.field.equals("rename") && !this.field.equals("lore_add") && !this.field.startsWith("lore:")) {
                this.error = SimpleEditorScreen.tr("ankinbt.simple.invalid_number");
                return;
            }
            SimpleEditorScreen.this.applyInlineEdit(this.field, this.inputBox.method_1882(), this.isLore);
        }

        private String getFieldLabel(String f) {
            if (f.equals("rename")) {
                return SimpleEditorScreen.tr("ankinbt.simple.rename");
            }
            if (f.equals("count")) {
                return SimpleEditorScreen.tr("ankinbt.simple.count");
            }
            if (f.equals("damage")) {
                return SimpleEditorScreen.tr("ankinbt.simple.damage");
            }
            if (f.equals("max_damage")) {
                return SimpleEditorScreen.tr("ankinbt.simple.max_damage");
            }
            if (f.equals("max_stack")) {
                return SimpleEditorScreen.tr("ankinbt.simple.max_stack");
            }
            if (f.equals("repair_cost")) {
                return SimpleEditorScreen.tr("ankinbt.simple.repair_cost");
            }
            if (f.equals("custom_model_data")) {
                return SimpleEditorScreen.tr("ankinbt.simple.custom_model_data");
            }
            if (f.equals("dye_color")) {
                return SimpleEditorScreen.tr("ankinbt.simple.dye_color");
            }
            if (f.equals("lore_add")) {
                return SimpleEditorScreen.tr("ankinbt.simple.add_lore");
            }
            if (f.startsWith("lore:")) {
                return SimpleEditorScreen.tr("ankinbt.simple.edit_lore");
            }
            if (f.startsWith("ench_level:")) {
                return SimpleEditorScreen.tr("ankinbt.simple.ench_level");
            }
            if (f.startsWith("attr_amount:")) {
                return SimpleEditorScreen.tr("ankinbt.simple.attr_amount");
            }
            if (f.equals("food_nutrition")) {
                return SimpleEditorScreen.tr("ankinbt.simple.food_nutrition");
            }
            if (f.equals("food_saturation")) {
                return SimpleEditorScreen.tr("ankinbt.simple.food_saturation");
            }
            return f;
        }
    }

    class InventorySwitchSubEditor
    implements SubEditor {
        private static final int COLS = 9;
        private static final int ROWS = 4;

        InventorySwitchSubEditor() {
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 16, 246);
            int dh = Math.min(h - 16, 150);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.inventory_pick"), dx + 10, dy + 8, -1906448, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.inventory_hint"), dx + 10, dy + 30, -10193781, false);
            class_310 mc = class_310.method_1551();
            if (mc.field_1724 == null) {
                VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.message.no_item"), dx + 10, dy + 48, -1096636, false);
                return;
            }
            int currentSlot = SimpleEditorScreen.this.currentEditedSlot();
            int gridX = dx + 10;
            int gridY = dy + 46;
            int cell = 20;
            int gap = 4;
            for (int r = 0; r < 4; ++r) {
                for (int c = 0; c < 9; ++c) {
                    boolean active;
                    int logical = r < 3 ? 9 + r * 9 + c : c;
                    class_1799 stack = mc.field_1724.method_31548().method_5438(logical);
                    int sx = gridX + c * (cell + gap);
                    int sy = gridY + r * (cell + gap);
                    boolean hover = mx >= sx && mx < sx + 18 && my >= sy && my < sy + 18;
                    boolean bl = active = logical == currentSlot;
                    int bg = active ? 677603057 : (hover ? 0x50FFFFFF : 0x30FFFFFF);
                    int edge = active ? -10262799 : -14540234;
                    g.method_25294(sx, sy, sx + 18, sy + 18, bg);
                    SimpleEditorScreen.this.drawBorder(g, sx, sy, 18, 18, edge);
                    if (stack == null || stack.method_7960()) continue;
                    g.method_51427(stack, sx + 1, sy + 1);
                    if (!hover) continue;
                    VersionCompat.get().renderTooltip(g, font, stack.method_7964(), mx, my);
                }
            }
            int by = dy + dh - 26;
            int bw = 58;
            int bh = 18;
            int bx = dx + dw - bw - 10;
            boolean hover = mx >= bx && mx < bx + bw && my >= by && my < by + bh;
            g.method_25294(bx, by, bx + bw, by + bh, hover ? 0x50FFFFFF : 0x30FFFFFF);
            SimpleEditorScreen.this.drawBorder(g, bx, by, bw, bh, -14540234);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), bx + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 5, -1906448, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 16, 246);
            int dh = Math.min(h - 16, 150);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int gridX = dx + 10;
            int gridY = dy + 46;
            int cell = 20;
            int gap = 4;
            for (int r = 0; r < 4; ++r) {
                for (int c = 0; c < 9; ++c) {
                    int logical = r < 3 ? 9 + r * 9 + c : c;
                    int sx = gridX + c * (cell + gap);
                    int sy = gridY + r * (cell + gap);
                    if (!(mx >= (double)sx) || !(mx < (double)(sx + 18)) || !(my >= (double)sy) || !(my < (double)(sy + 18))) continue;
                    SimpleEditorScreen.this.switchToInventorySlot(logical);
                    return true;
                }
            }
            int by = dy + dh - 26;
            int bw = 58;
            int bh = 18;
            int bx = dx + dw - bw - 10;
            if (mx >= (double)bx && mx < (double)(bx + bw) && my >= (double)by && my < (double)(by + bh)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 256) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            return false;
        }
    }

    class ContainerPreviewSubEditor
    implements SubEditor {
        private static final int COLS = 9;
        private static final int ROWS = 3;
        private static final int PAGE_SIZE = 27;
        private final List<class_2487> slotTags = new ArrayList<class_2487>();
        private final List<class_1799> slotStacks = new ArrayList<class_1799>();
        private int selectedSlot = 0;
        private int page = 0;
        private StorageMode mode = StorageMode.CONTAINER;
        private String message = "";
        private int msgColor = -7035976;

        ContainerPreviewSubEditor() {
            this.loadFromItem();
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 470);
            int dh = Math.min(h - 10, 300);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.container.title"), dx + 10, dy + 8, -1906448, false);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.container.mode") + ": " + this.modeName(), dx + 190, dy + 8, -7035976, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int gridX = dx + 14;
            int gridY = dy + 36;
            int cell = 20;
            int gap = 4;
            int start = this.page * 27;
            int hoveredGlobal = -1;
            class_1799 hoveredStack = class_1799.field_8037;
            for (int i = 0; i < 27; ++i) {
                boolean hover;
                int col = i % 9;
                int row = i / 9;
                int sx = gridX + col * (cell + gap);
                int sy = gridY + row * (cell + gap);
                int global = start + i;
                boolean sel = global == this.selectedSlot;
                boolean bl = hover = mx >= sx && mx < sx + cell && my >= sy && my < sy + cell;
                if (hover) {
                    hoveredGlobal = global;
                }
                int bg = sel ? -2141241916 : (hover ? 1615808229 : 1075915587);
                g.method_25294(sx, sy, sx + cell, sy + cell, bg);
                SimpleEditorScreen.this.drawBorder(g, sx, sy, cell, cell, sel ? -10262799 : -14540234);
                class_1799 st = this.stackAt(global);
                if (st.method_7960()) continue;
                g.method_51427(st, sx + 2, sy + 2);
                if (!hover) continue;
                hoveredStack = st;
            }
            String slotLabel = SimpleEditorScreen.tr("ankinbt.container.slot") + " " + this.selectedSlot;
            VersionCompat.get().drawString(g, font, slotLabel, dx + 14, dy + 112, -7035976, false);
            String pageText = this.page + 1 + " / " + Math.max(1, (this.slotTags.size() + 27 - 1) / 27);
            VersionCompat.get().drawString(g, font, pageText, dx + 14 + font.method_1727(slotLabel) + 18, dy + 112, -10193781, false);
            int by = dy + dh - 30;
            int bw = 66;
            int bh = 20;
            int bx = dx + 10;
            this.renderSmallBtn(g, font, mx, my, bx, by, 20, bh, "<");
            this.renderSmallBtn(g, font, mx, my, bx += 24, by, 20, bh, ">");
            this.renderSmallBtn(g, font, mx, my, bx += 28, by, bw, bh, SimpleEditorScreen.tr("ankinbt.container.from_hand"));
            this.renderSmallBtn(g, font, mx, my, bx += bw + 6, by, bw, bh, SimpleEditorScreen.tr("ankinbt.container.pick_item"));
            this.renderSmallBtn(g, font, mx, my, bx += bw + 6, by, bw, bh, SimpleEditorScreen.tr("ankinbt.container.clear_slot"));
            this.renderSmallBtn(g, font, mx, my, bx += bw + 6, by, 56, bh, this.modeName());
            this.renderSmallBtn(g, font, mx, my, bx += 62, by, 58, bh, SimpleEditorScreen.tr("ankinbt.edit.apply"));
            this.renderSmallBtn(g, font, mx, my, bx += 64, by, 58, bh, SimpleEditorScreen.tr("ankinbt.edit.cancel"));
            if (!this.message.isEmpty()) {
                VersionCompat.get().drawString(g, font, this.message, dx + 10, by - 12, this.msgColor, false);
            }
            if (hoveredGlobal >= 0 && !hoveredStack.method_7960()) {
                VersionCompat.get().renderTooltip(g, font, hoveredStack.method_7964(), mx, my);
            }
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 470);
            int dh = Math.min(h - 10, 300);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int gridX = dx + 14;
            int gridY = dy + 36;
            int cell = 20;
            int gap = 4;
            for (int i = 0; i < 27; ++i) {
                int col = i % 9;
                int row = i / 9;
                int sx = gridX + col * (cell + gap);
                int sy = gridY + row * (cell + gap);
                if (!(mx >= (double)sx) || !(mx < (double)(sx + cell)) || !(my >= (double)sy) || !(my < (double)(sy + cell))) continue;
                this.selectedSlot = this.page * 27 + i;
                this.ensureSlots(this.selectedSlot + 1);
                return true;
            }
            int by = dy + dh - 30;
            int bw = 66;
            int bx = dx + 10;
            int bh = 20;
            if (this.hit(mx, my, bx, by, 20, bh)) {
                this.prevPage();
                return true;
            }
            if (this.hit(mx, my, bx += 24, by, 20, bh)) {
                this.nextPage();
                return true;
            }
            if (this.hit(mx, my, bx += 28, by, bw, bh)) {
                this.fillFromMainHand();
                return true;
            }
            if (this.hit(mx, my, bx += bw + 6, by, bw, bh)) {
                this.openPicker();
                return true;
            }
            if (this.hit(mx, my, bx += bw + 6, by, bw, bh)) {
                this.clearSelected();
                return true;
            }
            if (this.hit(mx, my, bx += bw + 6, by, 56, bh)) {
                this.cycleMode();
                return true;
            }
            if (this.hit(mx, my, bx += 62, by, 58, bh)) {
                this.applyToItem();
                return true;
            }
            if (this.hit(mx, my, bx += 64, by, 58, bh)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 256) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            if (key == 261) {
                this.clearSelected();
                return true;
            }
            if (key == 257 || key == 335) {
                this.applyToItem();
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            if (sy > 0.0) {
                this.prevPage();
            }
            if (sy < 0.0) {
                this.nextPage();
            }
            return true;
        }

        private void prevPage() {
            if (this.page > 0) {
                --this.page;
            }
        }

        private void nextPage() {
            int maxPage = Math.max(0, (this.slotTags.size() - 1) / 27);
            if (this.page < maxPage) {
                ++this.page;
            }
        }

        private void cycleMode() {
            this.mode = switch (this.mode.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> StorageMode.BUNDLE;
                case 1 -> StorageMode.LEGACY;
                case 2 -> StorageMode.CONTAINER;
            };
            this.message = SimpleEditorScreen.tr("ankinbt.container.mode") + ": " + this.modeName();
            this.msgColor = -7035976;
        }

        private String modeName() {
            return switch (this.mode.ordinal()) {
                case 1 -> SimpleEditorScreen.tr("ankinbt.container.mode.bundle");
                case 2 -> SimpleEditorScreen.tr("ankinbt.container.mode.legacy");
                default -> SimpleEditorScreen.tr("ankinbt.container.mode.container");
            };
        }

        private class_1799 stackAt(int global) {
            if (global < 0 || global >= this.slotStacks.size()) {
                return class_1799.field_8037;
            }
            class_1799 st = this.slotStacks.get(global);
            return st == null ? class_1799.field_8037 : st;
        }

        private void fillFromMainHand() {
            class_310 mc = class_310.method_1551();
            if (mc.field_1724 == null) {
                return;
            }
            class_1799 hand = mc.field_1724.method_6047();
            if (hand == null || hand.method_7960()) {
                this.message = SimpleEditorScreen.tr("ankinbt.container.empty_hand");
                this.msgColor = -1096636;
                return;
            }
            this.ensureSlots(this.selectedSlot + 1);
            Optional<class_2487> tag = NbtHelper.serializeItemStack(hand);
            if (tag.isEmpty()) {
                this.message = SimpleEditorScreen.tr("ankinbt.status.save_error");
                this.msgColor = -1096636;
                return;
            }
            this.slotTags.set(this.selectedSlot, this.copyTag(tag.get()));
            this.slotStacks.set(this.selectedSlot, hand.method_7972());
            this.message = SimpleEditorScreen.tr("ankinbt.status.edited");
            this.msgColor = -7035976;
        }

        private void openPicker() {
            int slot = this.selectedSlot;
            class_310.method_1551().method_1507((class_437)new ItemPickerScreen(SimpleEditorScreen.this, id -> {
                this.ensureSlots(slot + 1);
                class_1792 item = ItemRegistryHelper.resolveItem(id);
                if (item == null) {
                    return;
                }
                class_1799 stack = new class_1799((class_1935)item, 1);
                Optional<class_2487> tag = NbtHelper.serializeItemStack(stack);
                if (tag.isPresent()) {
                    this.slotTags.set(slot, this.copyTag(tag.get()));
                    this.slotStacks.set(slot, stack.method_7972());
                    this.message = SimpleEditorScreen.tr("ankinbt.status.edited");
                    this.msgColor = -7035976;
                }
            }));
        }

        private void clearSelected() {
            this.ensureSlots(this.selectedSlot + 1);
            this.slotTags.set(this.selectedSlot, null);
            this.slotStacks.set(this.selectedSlot, class_1799.field_8037);
            this.message = SimpleEditorScreen.tr("ankinbt.status.deleted");
            this.msgColor = -7035976;
        }

        private void loadFromItem() {
            class_2499 legacy;
            class_2499 bundle;
            class_2499 list;
            this.slotTags.clear();
            this.slotStacks.clear();
            this.selectedSlot = 0;
            this.page = 0;
            this.mode = StorageMode.CONTAINER;
            Optional<class_2487> fullOpt = NbtHelper.serializeItemStack(SimpleEditorScreen.this.editStack);
            if (fullOpt.isEmpty()) {
                this.ensureSlots(27);
                return;
            }
            class_2487 full = fullOpt.get();
            class_2487 components = this.getCompound(full, "components");
            class_2499 class_24992 = list = components == null ? null : this.getList(components, "minecraft:container");
            if (list != null && !list.isEmpty()) {
                this.mode = StorageMode.CONTAINER;
                this.readContainerList(list);
                return;
            }
            class_2499 class_24993 = bundle = components == null ? null : this.getList(components, "minecraft:bundle_contents");
            if (bundle != null && !bundle.isEmpty()) {
                this.mode = StorageMode.BUNDLE;
                this.readBundleList(bundle);
                return;
            }
            class_2487 tag = this.getCompound(full, "tag");
            class_2487 block = tag == null ? null : this.getCompound(tag, "BlockEntityTag");
            class_2499 class_24994 = legacy = block == null ? null : this.getList(block, "Items");
            if (legacy != null && !legacy.isEmpty()) {
                this.mode = StorageMode.LEGACY;
                this.readLegacyList(legacy);
                return;
            }
            this.ensureSlots(27);
        }

        private void readContainerList(class_2499 list) {
            class_2487 ct;
            class_2520 entry;
            int i;
            int max = 27;
            for (i = 0; i < list.size(); ++i) {
                entry = list.method_10534(i);
                if (!(entry instanceof class_2487)) continue;
                ct = (class_2487)entry;
                max = Math.max(max, this.readInt(ct, "slot", 0) + 1);
            }
            this.ensureSlots(max);
            for (i = 0; i < list.size(); ++i) {
                int slot;
                entry = list.method_10534(i);
                if (!(entry instanceof class_2487) || (slot = this.readInt(ct = (class_2487)entry, "slot", -1)) < 0) continue;
                class_2487 item = this.getCompound(ct, "item");
                if (item == null) {
                    item = this.getCompound(ct, "stack");
                }
                if (item == null) continue;
                this.setSlot(slot, item);
            }
        }

        private void readBundleList(class_2499 list) {
            this.ensureSlots(Math.max(27, list.size()));
            for (int i = 0; i < list.size(); ++i) {
                class_2520 entry = list.method_10534(i);
                if (!(entry instanceof class_2487)) continue;
                class_2487 ct = (class_2487)entry;
                this.setSlot(i, ct);
            }
        }

        private void readLegacyList(class_2499 list) {
            class_2487 ct;
            class_2520 entry;
            int i;
            int max = 27;
            for (i = 0; i < list.size(); ++i) {
                entry = list.method_10534(i);
                if (!(entry instanceof class_2487)) continue;
                ct = (class_2487)entry;
                max = Math.max(max, this.readInt(ct, "Slot", 0) + 1);
            }
            this.ensureSlots(max);
            for (i = 0; i < list.size(); ++i) {
                int slot;
                entry = list.method_10534(i);
                if (!(entry instanceof class_2487) || (slot = this.readInt(ct = (class_2487)entry, "Slot", -1)) < 0) continue;
                class_2487 stack = new class_2487();
                stack.method_10582("id", this.readString(ct, "id", "minecraft:air"));
                stack.method_10569("count", Math.max(1, this.readInt(ct, "Count", 1)));
                class_2487 legacyTag = this.getCompound(ct, "tag");
                if (legacyTag != null && !legacyTag.method_33133()) {
                    class_2487 components = new class_2487();
                    components.method_10566("minecraft:custom_data", (class_2520)this.copyTag(legacyTag));
                    stack.method_10566("components", (class_2520)components);
                }
                this.setSlot(slot, stack);
            }
        }

        private void setSlot(int slot, class_2487 stackTag) {
            this.ensureSlots(slot + 1);
            this.slotTags.set(slot, this.copyTag(stackTag));
            this.slotStacks.set(slot, this.decodeStack(stackTag));
        }

        private void ensureSlots(int size) {
            int target = Math.max(27, size);
            while (this.slotTags.size() < target) {
                this.slotTags.add(null);
                this.slotStacks.add(class_1799.field_8037);
            }
        }

        private class_1799 decodeStack(class_2487 stackTag) {
            if (stackTag == null || stackTag.method_33133()) {
                return class_1799.field_8037;
            }
            Optional<class_1799> opt = NbtHelper.deserializeItemStack(stackTag);
            if (opt.isPresent()) {
                return opt.get();
            }
            String id = this.readString(stackTag, "id", "");
            if (id.isBlank()) {
                return class_1799.field_8037;
            }
            class_1792 item = ItemRegistryHelper.resolveItem(id);
            if (item == null) {
                return class_1799.field_8037;
            }
            return new class_1799((class_1935)item, Math.max(1, this.readInt(stackTag, "count", 1)));
        }

        private void applyToItem() {
            Optional<class_2487> fullOpt = NbtHelper.serializeItemStack(SimpleEditorScreen.this.editStack);
            if (fullOpt.isEmpty()) {
                this.message = SimpleEditorScreen.tr("ankinbt.status.save_error");
                this.msgColor = -1096636;
                return;
            }
            class_2487 full = fullOpt.get();
            class_2487 components = this.getOrCreateCompound(full, "components");
            if (this.mode == StorageMode.CONTAINER) {
                out = new class_2499();
                for (int i = 0; i < this.slotTags.size(); ++i) {
                    class_2487 stack = this.slotTags.get(i);
                    if (stack == null || this.isAir(stack)) continue;
                    class_2487 entry = new class_2487();
                    entry.method_10569("slot", i);
                    entry.method_10566("item", (class_2520)this.copyTag(stack));
                    out.add((Object)entry);
                }
                components.method_10566("minecraft:container", (class_2520)out);
                this.removeKey(components, "minecraft:bundle_contents");
            } else if (this.mode == StorageMode.BUNDLE) {
                out = new class_2499();
                for (class_2487 stack : this.slotTags) {
                    if (stack == null || this.isAir(stack)) continue;
                    out.add((Object)this.copyTag(stack));
                }
                components.method_10566("minecraft:bundle_contents", (class_2520)out);
                this.removeKey(components, "minecraft:container");
            } else {
                class_2487 tag = this.getOrCreateCompound(full, "tag");
                class_2487 block = this.getOrCreateCompound(tag, "BlockEntityTag");
                class_2499 items = new class_2499();
                for (int i = 0; i < this.slotTags.size(); ++i) {
                    class_2487 stack = this.slotTags.get(i);
                    if (stack == null || this.isAir(stack)) continue;
                    items.add((Object)this.toLegacyStack(i, stack));
                }
                block.method_10566("Items", (class_2520)items);
                tag.method_10566("BlockEntityTag", (class_2520)block);
                full.method_10566("tag", (class_2520)tag);
            }
            full.method_10566("components", (class_2520)components);
            Optional<class_1799> outStack = NbtHelper.deserializeItemStack(full);
            if (outStack.isEmpty()) {
                this.message = SimpleEditorScreen.tr("ankinbt.status.save_error");
                this.msgColor = -1096636;
                return;
            }
            SimpleEditorScreen.this.editStack = outStack.get();
            SimpleEditorScreen.this.markDirty();
            this.loadFromItem();
            this.message = SimpleEditorScreen.tr("ankinbt.container.applied");
            this.msgColor = -14498466;
        }

        private class_2487 toLegacyStack(int slot, class_2487 stack) {
            class_2487 custom;
            class_2487 out = new class_2487();
            out.method_10567("Slot", (byte)(slot & 0xFF));
            out.method_10582("id", this.readString(stack, "id", "minecraft:air"));
            out.method_10567("Count", (byte)Math.max(1, Math.min(127, this.readInt(stack, "count", 1))));
            class_2487 components = this.getCompound(stack, "components");
            if (components != null && (custom = this.getCompound(components, "minecraft:custom_data")) != null && !custom.method_33133()) {
                out.method_10566("tag", (class_2520)this.copyTag(custom));
            }
            return out;
        }

        private boolean isAir(class_2487 stack) {
            return "minecraft:air".equals(this.readString(stack, "id", "minecraft:air"));
        }

        private void renderSmallBtn(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h, String text) {
            boolean hover = this.hit(mx, my, x, y, w, h);
            g.method_25294(x, y, x + w, y + h, hover ? 0x50FFFFFF : 0x30FFFFFF);
            Object draw = text;
            if (font.method_1727((String)draw) > w - 8) {
                draw = font.method_27523((String)draw, w - 12) + "..";
            }
            VersionCompat.get().drawString(g, font, (String)draw, x + (w - font.method_1727((String)draw)) / 2, y + 6, -7035976, false);
        }

        private boolean hit(double mx, double my, int x, int y, int w, int h) {
            return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
        }

        private class_2487 copyTag(class_2487 source) {
            if (source == null) {
                return null;
            }
            class_2487 out = new class_2487();
            out.method_10543(source);
            return out;
        }

        private class_2487 getCompound(class_2487 parent, String key) {
            class_2487 ct;
            if (parent == null || key == null || key.isBlank()) {
                return null;
            }
            Object raw = this.getTag(parent, key);
            return raw instanceof class_2487 ? (ct = (class_2487)raw) : null;
        }

        private class_2499 getList(class_2487 parent, String key) {
            class_2499 lt;
            if (parent == null || key == null || key.isBlank()) {
                return null;
            }
            Object raw = this.getTag(parent, key);
            return raw instanceof class_2499 ? (lt = (class_2499)raw) : null;
        }

        private class_2487 getOrCreateCompound(class_2487 parent, String key) {
            class_2487 out = this.getCompound(parent, key);
            if (out == null) {
                out = new class_2487();
                parent.method_10566(key, (class_2520)out);
            }
            return out;
        }

        private void removeKey(class_2487 parent, String key) {
            if (parent == null || key == null || key.isBlank()) {
                return;
            }
            try {
                parent.getClass().getMethod("remove", String.class).invoke((Object)parent, key);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }

        private Object getTag(class_2487 parent, String key) {
            try {
                Object out = parent.getClass().getMethod("get", String.class).invoke((Object)parent, key);
                if (out instanceof Optional) {
                    Optional opt = (Optional)out;
                    return opt.orElse(null);
                }
                return out;
            }
            catch (Throwable ignored) {
                return null;
            }
        }

        private int readInt(class_2487 parent, String key, int def) {
            if (parent == null) {
                return def;
            }
            try {
                Optional opt;
                Object var7_9;
                Object out = parent.getClass().getMethod("getInt", String.class).invoke((Object)parent, key);
                if (out instanceof Number) {
                    Number n = (Number)out;
                    return n.intValue();
                }
                if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof Number) {
                    Number n = var7_9;
                    return n.intValue();
                }
            }
            catch (Throwable out) {
                // empty catch block
            }
            Object raw = this.getTag(parent, key);
            if (raw != null) {
                try {
                    Object v = raw.getClass().getMethod("getAsInt", new Class[0]).invoke(raw, new Object[0]);
                    if (v instanceof Number) {
                        Number n = (Number)v;
                        return n.intValue();
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            return def;
        }

        private String readString(class_2487 parent, String key, String def) {
            if (parent == null) {
                return def;
            }
            try {
                Optional opt;
                Object var7_9;
                Object out = parent.getClass().getMethod("getString", String.class).invoke((Object)parent, key);
                if (out instanceof String) {
                    String s = (String)out;
                    return s;
                }
                if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof String) {
                    String s = var7_9;
                    return s;
                }
            }
            catch (Throwable out) {
                // empty catch block
            }
            Object raw = this.getTag(parent, key);
            if (raw != null) {
                try {
                    Object v = raw.getClass().getMethod("getAsString", new Class[0]).invoke(raw, new Object[0]);
                    if (v instanceof String) {
                        String s = (String)v;
                        return s;
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            return def;
        }

        private static enum StorageMode {
            CONTAINER,
            BUNDLE,
            LEGACY;

        }
    }

    class NbtImportSubEditor
    implements SubEditor {
        private List<NbtFileIO.NbtFileEntry> files;
        private List<String> categories;
        private String currentCategory = "";
        private int scrollOff = 0;
        private int hoverIdx = -1;
        private int selectedIdx = -1;
        private class_2487 previewTag = null;
        private String previewInfo = null;
        private final Map<String, class_1799> iconCache = new HashMap<String, class_1799>();

        NbtImportSubEditor() {
            this.categories = AnkiConfig.listExportCategories();
            this.files = NbtFileIO.listNbtFiles(null);
        }

        private void refreshFiles() {
            this.files = NbtFileIO.listNbtFiles(this.currentCategory.isEmpty() ? null : this.currentCategory);
            this.selectedIdx = -1;
            this.previewTag = null;
            this.previewInfo = null;
            this.scrollOff = 0;
            this.iconCache.clear();
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            String cat;
            int cw;
            int dw = Math.min(w - 10, 440);
            int dh = Math.min(h - 10, 320);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.import.title"), dx + 10, dy + 8, -1906448, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int tabY = dy + 26;
            int tabX = dx + 8;
            String allLabel = SimpleEditorScreen.tr("ankinbt.import.all");
            int allW = font.method_1727(allLabel) + 10;
            boolean allHover = mx >= tabX && mx < tabX + allW && my >= tabY && my < tabY + 16;
            boolean allActive = this.currentCategory.isEmpty();
            g.method_25294(tabX, tabY, tabX + allW, tabY + 16, allActive ? -10262799 : (allHover ? 0x50FFFFFF : 0x30FFFFFF));
            VersionCompat.get().drawString(g, font, allLabel, tabX + 5, tabY + 4, allActive ? -1906448 : -7035976, false);
            tabX += allW + 4;
            Iterator<String> iterator = this.categories.iterator();
            while (iterator.hasNext() && tabX + (cw = font.method_1727(cat = iterator.next()) + 10) <= dx + dw - 8) {
                boolean hover = mx >= tabX && mx < tabX + cw && my >= tabY && my < tabY + 16;
                boolean active = cat.equals(this.currentCategory);
                g.method_25294(tabX, tabY, tabX + cw, tabY + 16, active ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
                VersionCompat.get().drawString(g, font, cat, tabX + 5, tabY + 4, active ? -1906448 : -7035976, false);
                tabX += cw + 4;
            }
            int listX = dx + 8;
            int listY = tabY + 22;
            int listW = dw / 2 - 12;
            int listH = dh - 110;
            g.method_25294(listX, listY, listX + listW, listY + listH, -16119276);
            SimpleEditorScreen.this.drawBorder(g, listX, listY, listW, listH, -14540234);
            int rowH = 20;
            int maxItems = listH / rowH;
            this.hoverIdx = -1;
            if (this.files.isEmpty()) {
                VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.import.no_files"), listX + 8, listY + 8, -10193781, false);
            } else {
                int end = Math.min(this.scrollOff + maxItems, this.files.size());
                for (int i = this.scrollOff; i < end; ++i) {
                    Object name;
                    boolean sel;
                    boolean hovered;
                    int ry = listY + (i - this.scrollOff) * rowH;
                    boolean bl = hovered = mx >= listX && mx < listX + listW && my >= ry && my < ry + rowH;
                    if (hovered) {
                        this.hoverIdx = i;
                    }
                    boolean bl2 = sel = i == this.selectedIdx;
                    if (sel) {
                        g.method_25294(listX + 1, ry, listX + listW - 1, ry + rowH, 677603057);
                    } else if (hovered) {
                        g.method_25294(listX + 1, ry, listX + listW - 1, ry + rowH, 0x30FFFFFF);
                    }
                    NbtFileIO.NbtFileEntry entry = this.files.get(i);
                    class_1799 icon = this.iconFor(entry);
                    if (!icon.method_7960()) {
                        g.method_51427(icon, listX + 3, ry + 2);
                    }
                    if (font.method_1727((String)(name = entry.displayName())) > listW - 34) {
                        name = font.method_27523((String)name, listW - 40) + "..";
                    }
                    VersionCompat.get().drawString(g, font, (String)name, listX + 22, ry + 6, sel ? -1906448 : -7035976, false);
                }
            }
            int prevX = dx + dw / 2 + 4;
            int prevY = listY;
            int prevW = dw / 2 - 12;
            int prevH = listH;
            g.method_25294(prevX, prevY, prevX + prevW, prevY + prevH, -16119276);
            SimpleEditorScreen.this.drawBorder(g, prevX, prevY, prevW, prevH, -14540234);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.import.preview"), prevX + 6, prevY + 4, -7035976, false);
            if (this.selectedIdx >= 0 && this.selectedIdx < this.files.size()) {
                int infoStartY;
                NbtFileIO.NbtFileEntry entry = this.files.get(this.selectedIdx);
                VersionCompat.get().drawString(g, font, entry.name(), prevX + 6, prevY + 18, -1906448, false);
                if (entry.alias() != null) {
                    VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.export.alias") + " " + entry.alias(), prevX + 6, prevY + 30, -10262799, false);
                    VersionCompat.get().drawString(g, font, entry.sizeDisplay(), prevX + 6, prevY + 42, -10193781, false);
                } else {
                    VersionCompat.get().drawString(g, font, entry.sizeDisplay(), prevX + 6, prevY + 30, -10193781, false);
                }
                int n = infoStartY = entry.alias() != null ? prevY + 56 : prevY + 44;
                if (this.previewInfo != null) {
                    String[] infoLines = this.previewInfo.split("\n");
                    for (int i = 0; i < Math.min(infoLines.length, (prevH - 60) / 11); ++i) {
                        Object line = infoLines[i];
                        if (font.method_1727((String)line) > prevW - 12) {
                            line = font.method_27523((String)line, prevW - 18) + "..";
                        }
                        VersionCompat.get().drawString(g, font, (String)line, prevX + 6, infoStartY + i * 11, -7035976, false);
                    }
                }
            } else {
                VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.import.select_file"), prevX + 6, prevY + 20, -10193781, false);
            }
            int by = dy + dh - 32;
            int bw2 = 70;
            int bh2 = 22;
            int refX = dx + 10;
            boolean rh = mx >= refX && mx < refX + 50 && my >= by && my < by + bh2;
            g.method_25294(refX, by, refX + 50, by + bh2, rh ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.import.refresh"), refX + (50 - font.method_1727(SimpleEditorScreen.tr("ankinbt.import.refresh"))) / 2, by + 7, -7035976, false);
            int openW = 76;
            int openX = refX + 56;
            boolean fh = mx >= openX && mx < openX + openW && my >= by && my < by + bh2;
            g.method_25294(openX, by, openX + openW, by + bh2, fh ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.import.open_file"), openX + (openW - font.method_1727(SimpleEditorScreen.tr("ankinbt.import.open_file"))) / 2, by + 7, -7035976, false);
            int cancelX = dx + dw / 2 - bw2 - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.method_25294(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 7, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw2 && my >= by && my < by + bh2;
            g.method_25294(okX, by, okX + bw2, by + bh2, oh ? -10262799 : -11581723);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.import.do_import"), okX + (bw2 - font.method_1727(SimpleEditorScreen.tr("ankinbt.import.do_import"))) / 2, by + 7, -1906448, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 440);
            int dh = Math.min(h - 10, 320);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int tabY = dy + 26;
            int tabX = dx + 8;
            String allLabel = SimpleEditorScreen.tr("ankinbt.import.all");
            int allW = SimpleEditorScreen.this.field_22793.method_1727(allLabel) + 10;
            if (mx >= (double)tabX && mx < (double)(tabX + allW) && my >= (double)tabY && my < (double)(tabY + 16)) {
                this.currentCategory = "";
                this.refreshFiles();
                return true;
            }
            tabX += allW + 4;
            for (String cat : this.categories) {
                int cw = SimpleEditorScreen.this.field_22793.method_1727(cat) + 10;
                if (tabX + cw > dx + dw - 8) break;
                if (mx >= (double)tabX && mx < (double)(tabX + cw) && my >= (double)tabY && my < (double)(tabY + 16)) {
                    this.currentCategory = cat;
                    this.refreshFiles();
                    return true;
                }
                tabX += cw + 4;
            }
            int listX = dx + 8;
            int listY = tabY + 22;
            int listW = dw / 2 - 12;
            if (this.hoverIdx >= 0 && this.hoverIdx < this.files.size() && mx >= (double)listX && mx < (double)(listX + listW)) {
                this.selectedIdx = this.hoverIdx;
                this.loadPreview();
                return true;
            }
            int by = dy + dh - 32;
            int bw2 = 70;
            int bh2 = 22;
            int refX = dx + 10;
            if (mx >= (double)refX && mx < (double)(refX + 50) && my >= (double)by && my < (double)(by + bh2)) {
                this.categories = AnkiConfig.listExportCategories();
                this.refreshFiles();
                return true;
            }
            int openW = 76;
            int openX = refX + 56;
            if (mx >= (double)openX && mx < (double)(openX + openW) && my >= (double)by && my < (double)(by + bh2)) {
                this.importFromDialog();
                return true;
            }
            int cancelX = dx + dw / 2 - bw2 - 6;
            if (mx >= (double)cancelX && mx < (double)(cancelX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            int okX = dx + dw / 2 + 6;
            if (mx >= (double)okX && mx < (double)(okX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                this.doImport();
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) {
                this.doImport();
                return true;
            }
            if (key == 265 && this.selectedIdx > 0) {
                --this.selectedIdx;
                this.loadPreview();
                return true;
            }
            if (key == 264 && this.selectedIdx < this.files.size() - 1) {
                ++this.selectedIdx;
                this.loadPreview();
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            this.scrollOff -= (int)sy * 3;
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.files.size() - 5)));
            return true;
        }

        private void loadPreview() {
            if (this.selectedIdx < 0 || this.selectedIdx >= this.files.size()) {
                return;
            }
            NbtFileIO.NbtFileEntry entry = this.files.get(this.selectedIdx);
            this.previewTag = NbtFileIO.importNbt(entry.path());
            if (this.previewTag != null) {
                class_2520 comp;
                StringBuilder sb = new StringBuilder();
                if (this.previewTag.method_10545("id")) {
                    sb.append(SimpleEditorScreen.tr("ankinbt.side.id")).append(VersionCompat.get().compoundGetString(this.previewTag, "id")).append("\n");
                }
                if (this.previewTag.method_10545("count")) {
                    sb.append(SimpleEditorScreen.tr("ankinbt.side.count")).append(VersionCompat.get().compoundGetInt(this.previewTag, "count")).append("\n");
                }
                if (this.previewTag.method_10545("components") && (comp = this.previewTag.method_10580("components")) instanceof class_2487) {
                    class_2487 ct = (class_2487)comp;
                    sb.append(SimpleEditorScreen.tr("ankinbt.side.components")).append(": ").append(ct.method_10546()).append("\n");
                    for (String key : VersionCompat.get().getCompoundKeys(ct)) {
                        sb.append("  ").append(key).append("\n");
                    }
                }
                this.previewInfo = sb.toString();
            } else {
                this.previewInfo = SimpleEditorScreen.tr("ankinbt.import.load_failed");
            }
        }

        private void doImport() {
            if (this.selectedIdx < 0 || this.selectedIdx >= this.files.size()) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.select_file"), -1096636);
                return;
            }
            if (this.previewTag == null) {
                this.loadPreview();
            }
            if (this.previewTag == null) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.load_failed"), -1096636);
                return;
            }
            Optional<class_1799> opt = NbtHelper.deserializeItemStack(this.previewTag);
            if (opt.isEmpty()) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.invalid_nbt"), -1096636);
                return;
            }
            SimpleEditorScreen.this.editStack = opt.get();
            SimpleEditorScreen.this.markDirty();
            SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.success"), -14498466);
            SimpleEditorScreen.this.activeSubEditor = null;
        }

        private void importFromDialog() {
            if (!SimpleEditorScreen.this.hasTinyFd()) {
                return;
            }
            String picked = SimpleEditorScreen.this.tinyFdOpenPath(AnkiConfig.getExportPath().toString());
            if (picked == null || picked.isBlank()) {
                return;
            }
            class_2487 tag = NbtFileIO.importNbt(Path.of(picked, new String[0]));
            if (tag == null) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.load_failed"), -1096636);
                return;
            }
            Optional<class_1799> opt = NbtHelper.deserializeItemStack(tag);
            if (opt.isEmpty()) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.invalid_nbt"), -1096636);
                return;
            }
            SimpleEditorScreen.this.editStack = opt.get();
            SimpleEditorScreen.this.markDirty();
            SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.success"), -14498466);
            SimpleEditorScreen.this.activeSubEditor = null;
        }

        private class_1799 iconFor(NbtFileIO.NbtFileEntry entry) {
            Optional<class_1799> opt;
            if (entry == null || entry.path() == null) {
                return class_1799.field_8037;
            }
            String key = entry.path().toString();
            class_1799 cached = this.iconCache.get(key);
            if (cached != null) {
                return cached;
            }
            class_1799 icon = class_1799.field_8037;
            class_2487 tag = NbtFileIO.importNbt(entry.path());
            if (tag != null && (opt = NbtHelper.deserializeItemStack(tag)).isPresent()) {
                icon = opt.get();
            }
            this.iconCache.put(key, icon);
            return icon;
        }
    }

    class NbtExportSubEditor
    implements SubEditor {
        String fileName;
        String category;
        String alias;
        int cursor;
        int focusField = 0;
        String message = null;
        int msgColor = -7035976;
        List<String> existingCats;

        NbtExportSubEditor() {
            String itemId = SimpleEditorScreen.this.resolveStackRegistryPath(SimpleEditorScreen.this.editStack);
            long ts = System.currentTimeMillis() / 1000L;
            this.fileName = itemId + "_" + ts;
            this.category = AnkiConfig.getLastExportCategory();
            this.alias = "";
            this.cursor = this.fileName.length();
            this.existingCats = AnkiConfig.listExportCategories();
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 380);
            int dh = 240;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.export_nbt"), dx + 10, dy + 8, -1906448, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int fieldX = dx + 10;
            int fieldW = dw - 20;
            int fieldH = 20;
            int curY = dy + 28;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.export.category"), fieldX, curY, -7035976, false);
            g.method_25294(fieldX, curY += 12, fieldX + fieldW, curY + fieldH, -15592930);
            SimpleEditorScreen.this.drawBorder(g, fieldX, curY, fieldW, fieldH, this.focusField == 1 ? -10262799 : -14540234);
            String catDisp = this.category.isEmpty() ? SimpleEditorScreen.tr("ankinbt.export.no_category") : this.category;
            VersionCompat.get().drawString(g, font, catDisp + (this.focusField == 1 && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), fieldX + 4, curY + 6, this.category.isEmpty() ? -10193781 : -1906448, false);
            curY += fieldH + 4;
            if (!this.existingCats.isEmpty()) {
                int tagX = fieldX;
                for (String cat : this.existingCats) {
                    int tw = font.method_1727(cat) + 10;
                    if (tagX + tw > fieldX + fieldW) {
                        tagX = fieldX;
                        curY += 16;
                    }
                    boolean hover = mx >= tagX && mx < tagX + tw && my >= curY && my < curY + 14;
                    boolean active = cat.equals(this.category);
                    g.method_25294(tagX, curY, tagX + tw, curY + 14, active ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
                    VersionCompat.get().drawString(g, font, cat, tagX + 5, curY + 3, active ? -1906448 : -7035976, false);
                    tagX += tw + 4;
                }
                curY += 18;
            }
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.export.filename"), fieldX, curY, -7035976, false);
            g.method_25294(fieldX, curY += 12, fieldX + fieldW, curY + fieldH, -15592930);
            SimpleEditorScreen.this.drawBorder(g, fieldX, curY, fieldW, fieldH, this.focusField == 0 ? -10262799 : -14540234);
            VersionCompat.get().drawString(g, font, this.fileName + ".nbt" + (this.focusField == 0 && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), fieldX + 4, curY + 6, -1906448, false);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.export.alias"), fieldX, curY += fieldH + 4, -7035976, false);
            g.method_25294(fieldX, curY += 12, fieldX + fieldW, curY + fieldH, -15592930);
            SimpleEditorScreen.this.drawBorder(g, fieldX, curY, fieldW, fieldH, this.focusField == 2 ? -10262799 : -14540234);
            String aliasDisp = this.alias.isEmpty() ? SimpleEditorScreen.tr("ankinbt.export.alias_hint") : this.alias;
            VersionCompat.get().drawString(g, font, aliasDisp + (this.focusField == 2 && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), fieldX + 4, curY + 6, this.alias.isEmpty() ? -10193781 : -1906448, false);
            curY += fieldH + 6;
            Object pathPreview = AnkiConfig.getNbtExportDir();
            if (!this.category.isEmpty()) {
                pathPreview = (String)pathPreview + "/" + this.category;
            }
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.export.dir") + ": " + (String)pathPreview, fieldX, curY, -10193781, false);
            VersionCompat.get().drawString(g, font, AnkiConfig.isNativeFileDialogEnabled() ? SimpleEditorScreen.tr("ankinbt.export.native_dialog_on") : SimpleEditorScreen.tr("ankinbt.export.native_dialog_off"), fieldX, curY += 12, -10193781, false);
            curY += 12;
            if (this.message != null) {
                VersionCompat.get().drawString(g, font, this.message, fieldX, curY, this.msgColor, false);
            }
            int by = dy + dh - 28;
            int bw2 = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw2 - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.method_25294(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw2 && my >= by && my < by + bh2;
            g.method_25294(okX, by, okX + bw2, by + bh2, oh ? -10262799 : -11581723);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.export.do_export"), okX + (bw2 - font.method_1727(SimpleEditorScreen.tr("ankinbt.export.do_export"))) / 2, by + 6, -1906448, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 380);
            int dh = 240;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int fieldX = dx + 10;
            int fieldW = dw - 20;
            int fieldH = 20;
            int curY = dy + 28 + 12;
            if (mx >= (double)fieldX && mx < (double)(fieldX + fieldW) && my >= (double)curY && my < (double)(curY + fieldH)) {
                this.focusField = 1;
                return true;
            }
            curY += fieldH + 4;
            if (!this.existingCats.isEmpty()) {
                int tagX = fieldX;
                for (String cat : this.existingCats) {
                    int tw = SimpleEditorScreen.this.field_22793.method_1727(cat) + 10;
                    if (tagX + tw > fieldX + fieldW) {
                        tagX = fieldX;
                        curY += 16;
                    }
                    if (mx >= (double)tagX && mx < (double)(tagX + tw) && my >= (double)curY && my < (double)(curY + 14)) {
                        this.category = cat.equals(this.category) ? "" : cat;
                        return true;
                    }
                    tagX += tw + 4;
                }
                curY += 18;
            }
            curY += 12;
            if (mx >= (double)fieldX && mx < (double)(fieldX + fieldW) && my >= (double)curY && my < (double)(curY + fieldH)) {
                this.focusField = 0;
                return true;
            }
            curY += fieldH + 4;
            curY += 12;
            if (mx >= (double)fieldX && mx < (double)(fieldX + fieldW) && my >= (double)curY && my < (double)(curY + fieldH)) {
                this.focusField = 2;
                return true;
            }
            int by = dy + dh - 28;
            int bw2 = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw2 - 6;
            if (mx >= (double)cancelX && mx < (double)(cancelX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            int okX = dx + dw / 2 + 6;
            if (mx >= (double)okX && mx < (double)(okX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                this.doExport();
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            int cur;
            Object target;
            if (key == 257 || key == 335) {
                this.doExport();
                return true;
            }
            if (key == 258) {
                this.focusField = (this.focusField + 1) % 3;
                return true;
            }
            Object object = this.focusField == 0 ? this.fileName : (target = this.focusField == 1 ? this.category : this.alias);
            int n = this.focusField == 0 ? this.cursor : (cur = this.focusField == 1 ? this.category.length() : this.alias.length());
            if (key == 259 && cur > 0) {
                target = ((String)target).substring(0, cur - 1) + ((String)target).substring(cur);
                this.applyField((String)target, --cur);
                return true;
            }
            if (key == 261 && cur < ((String)target).length()) {
                target = ((String)target).substring(0, cur) + ((String)target).substring(cur + 1);
                this.applyField((String)target, cur);
                return true;
            }
            if (key == 263 && cur > 0) {
                --cur;
                if (this.focusField == 0) {
                    this.cursor = cur;
                }
                return true;
            }
            if (key == 262 && cur < ((String)target).length()) {
                ++cur;
                if (this.focusField == 0) {
                    this.cursor = cur;
                }
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c < ' ') {
                return false;
            }
            if (this.focusField == 0) {
                if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '\"' || c == '<' || c == '>' || c == '|') {
                    return false;
                }
                this.fileName = this.fileName.substring(0, this.cursor) + c + this.fileName.substring(this.cursor);
                ++this.cursor;
            } else if (this.focusField == 1) {
                if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '\"' || c == '<' || c == '>' || c == '|' || c == '.') {
                    return false;
                }
                this.category = this.category + c;
            } else {
                this.alias = this.alias + c;
            }
            return true;
        }

        private void applyField(String val, int cur) {
            if (this.focusField == 0) {
                this.fileName = val;
                this.cursor = cur;
            } else if (this.focusField == 1) {
                this.category = val;
            } else {
                this.alias = val;
            }
        }

        private void doExport() {
            if (this.fileName.isEmpty()) {
                this.message = SimpleEditorScreen.tr("ankinbt.export.empty_name");
                this.msgColor = -1096636;
                return;
            }
            Optional<class_2487> opt = NbtHelper.serializeItemStack(SimpleEditorScreen.this.editStack);
            if (opt.isEmpty()) {
                this.message = SimpleEditorScreen.tr("ankinbt.export.failed");
                this.msgColor = -1096636;
                return;
            }
            Path path = null;
            String safeCategory = this.category.isBlank() ? null : this.category.trim();
            AnkiConfig.setLastExportCategory(safeCategory == null ? "" : safeCategory);
            if (SimpleEditorScreen.this.hasTinyFd()) {
                Path base = AnkiConfig.getExportPath(safeCategory);
                String picked = SimpleEditorScreen.this.tinyFdSavePath(base.resolve(this.fileName + ".nbt").toString());
                if (picked == null || picked.isBlank()) {
                    return;
                }
                path = NbtFileIO.exportNbtToPath(opt.get(), Path.of(picked, new String[0]), this.alias.isBlank() ? null : this.alias);
            } else {
                path = NbtFileIO.exportNbt(opt.get(), this.fileName, safeCategory, this.alias.isBlank() ? null : this.alias);
            }
            if (path != null) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.export.success"), -14498466);
                SimpleEditorScreen.this.activeSubEditor = null;
            } else {
                this.message = SimpleEditorScreen.tr("ankinbt.export.failed");
                this.msgColor = -1096636;
            }
        }
    }

    class ColorPickerSubEditor
    implements SubEditor {
        int mode;
        int selectedColor;

        ColorPickerSubEditor(int initialColor) {
            this.mode = initialColor;
            this.selectedColor = initialColor >= 0 ? initialColor : 0xFFFFFF;
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 280);
            int dh = 160;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            String title = this.mode == -2 ? SimpleEditorScreen.tr("ankinbt.simple.name_color") : (this.mode == -3 ? SimpleEditorScreen.tr("ankinbt.simple.potion_custom_color") : SimpleEditorScreen.tr("ankinbt.simple.dye_color_picker"));
            VersionCompat.get().drawString(g, font, title, dx + 10, dy + 8, -1906448, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int gridX = dx + 10;
            int gridY = dy + 28;
            int cellW = (dw - 20) / 8;
            int cellH = 20;
            for (int i = 0; i < 16; ++i) {
                int col = i % 8;
                int row = i / 8;
                int cx = gridX + col * cellW;
                int cy = gridY + row * (cellH + 2);
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.method_25294(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[i] | 0xFF000000);
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
            }
            int prevY = gridY + 2 * (cellH + 2) + 8;
            g.method_25294(dx + 10, prevY, dx + 10 + 30, prevY + 20, this.selectedColor & 0xFFFFFF | 0xFF000000);
            SimpleEditorScreen.this.drawBorder(g, dx + 10, prevY, 30, 20, -14540234);
            VersionCompat.get().drawString(g, font, String.format("#%06X", this.selectedColor & 0xFFFFFF), dx + 46, prevY + 6, -1906448, false);
            if (this.mode == -2) {
                Object name = SimpleEditorScreen.this.editStack.method_7964().getString();
                if (font.method_1727((String)name) > dw - 86) {
                    name = font.method_27523((String)name, dw - 96) + "..";
                }
                class_5250 previewName = class_2561.method_43470((String)name).method_27696(class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)this.selectedColor)));
                VersionCompat.get().drawString(g, font, (class_2561)previewName, dx + 46, prevY + 20, -1906448, false);
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.method_25294(cancelX, by, cancelX + bw, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.method_25294(okX, by, okX + bw, by + bh2, oh ? -10262799 : -11581723);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.apply"), okX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.apply"))) / 2, by + 6, -1906448, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 280);
            int dh = 160;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int gridX = dx + 10;
            int gridY = dy + 28;
            int cellW = (dw - 20) / 8;
            int cellH = 20;
            for (int i = 0; i < 16; ++i) {
                int col = i % 8;
                int row = i / 8;
                int cx = gridX + col * cellW;
                int cy = gridY + row * (cellH + 2);
                if (!(mx >= (double)cx) || !(mx < (double)(cx + cellW - 2)) || !(my >= (double)cy) || !(my < (double)(cy + cellH))) continue;
                this.selectedColor = MC_COLORS[i] & 0xFFFFFF;
                return true;
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            if (mx >= (double)cancelX && mx < (double)(cancelX + bw) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            int okX = dx + dw / 2 + 6;
            if (mx >= (double)okX && mx < (double)(okX + bw) && my >= (double)by && my < (double)(by + bh2)) {
                this.applyColor();
                return true;
            }
            return true;
        }

        private void applyColor() {
            if (this.mode >= 0) {
                VersionCompat.get().setDyedColor(SimpleEditorScreen.this.editStack, this.selectedColor);
                SimpleEditorScreen.this.dirty = true;
            } else if (this.mode == -2) {
                String name = SimpleEditorScreen.this.editStack.method_7964().getString();
                SimpleEditorScreen.this.setCustomNameComponent((class_2561)class_2561.method_43470((String)name).method_27696(class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)this.selectedColor))));
                SimpleEditorScreen.this.dirty = true;
            } else if (this.mode == -3) {
                SimpleEditorScreen.this.setPotionCustomColor(this.selectedColor);
            }
            SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.status.edited"), -7035976);
            SimpleEditorScreen.this.activeSubEditor = null;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            return false;
        }
    }

    class AttributePickerSubEditor
    implements SubEditor {
        private final List<String> allAttrs = new ArrayList<String>();
        private List<String> filtered = new ArrayList<String>();
        private String searchQ = "";
        private final FlatEditBox searchBox;
        private int scrollOff = 0;
        private int hoverIdx = -1;
        private int selectedIdx = -1;
        private String amountInput = "1.0";
        private int amountCursor = 3;
        private int focusField = 0;
        private int selectedOp = 0;
        private int selectedSlot = 0;
        private static final String[] SLOT_KEYS = new String[]{"any", "mainhand", "offhand", "head", "chest", "legs", "feet", "hand", "armor"};

        AttributePickerSubEditor() {
            this.searchBox = new FlatEditBox(SimpleEditorScreen.this.field_22793, 0, 0, 1, 18, (class_2561)class_2561.method_43473());
            this.searchBox.method_1852(128);
            this.searchBox.method_47404((class_2561)class_2561.method_43471((String)"ankinbt.search.hint"));
            this.searchBox.method_1863(value -> {
                this.searchQ = value == null ? "" : value;
                this.filter();
            });
            this.searchBox.method_25365(true);
            try {
                this.allAttrs.addAll(VersionCompat.get().getAllAttributeIds());
            } catch (Throwable ignored) {
            }
            if (this.allAttrs.isEmpty()) {
                this.allAttrs.addAll(ATTR_ZH.keySet());
            }
            Collections.sort(this.allAttrs);
            this.filtered = new ArrayList<String>(this.allAttrs);
        }

        private void filter() {
            if (this.searchQ.isEmpty()) {
                this.filtered = new ArrayList<String>(this.allAttrs);
            } else {
                String q = this.searchQ.toLowerCase();
                this.filtered = this.allAttrs.stream().filter(s -> {
                    if (s.toLowerCase().contains(q)) {
                        return true;
                    }
                    String zh = SimpleEditorScreen.this.findAttrText(ATTR_ZH, (String)s);
                    return zh != null && zh.contains(q);
                }).collect(Collectors.toList());
            }
            this.scrollOff = 0;
            this.selectedIdx = -1;
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.pick_attr"), x + 8, y + 4, -1906448, false);
            int sx = x + 8;
            int sy = y + 18;
            int sw = w - 16;
            int sh = 18;
            g.method_25294(sx, sy, sx + sw, sy + sh, -15592930);
            this.searchBox.method_46421(sx);
            this.searchBox.method_46419(sy);
            this.searchBox.method_25358(sw);
            this.searchBox.method_25365(this.focusField == 0);
            this.searchBox.method_25394(g, mx, my, 0.0f);
            int ly = sy + sh + 4;
            boolean showNotes = AnkiConfig.isAttributeNotesEnabled();
            int listH = h - (showNotes ? 162 : 140);
            int maxItems = listH / 16;
            this.hoverIdx = -1;
            int end = Math.min(this.scrollOff + maxItems, this.filtered.size());
            for (int i = this.scrollOff; i < end; ++i) {
                boolean sel;
                boolean hovered;
                int ry = ly + (i - this.scrollOff) * 16;
                boolean bl = hovered = mx >= x + 8 && mx < x + w - 8 && my >= ry && my < ry + 16;
                if (hovered) {
                    this.hoverIdx = i;
                }
                boolean bl2 = sel = i == this.selectedIdx;
                if (sel) {
                    g.method_25294(x + 8, ry, x + w - 8, ry + 16, 677603057);
                } else if (hovered) {
                    g.method_25294(x + 8, ry, x + w - 8, ry + 16, 0x30FFFFFF);
                }
                String displayName = SimpleEditorScreen.this.getAttrDisplayName(this.filtered.get(i));
                VersionCompat.get().drawString(g, font, displayName, x + 12, ry + 4, sel ? -1906448 : -7035976, false);
                if (!showNotes || SimpleEditorScreen.this.getAttrNote(this.filtered.get(i)) == null) continue;
                int infoX = x + w - 24;
                g.method_25294(infoX, ry + 3, infoX + 10, ry + 13, hovered ? -10262799 : 0x30FFFFFF);
                VersionCompat.get().drawString(g, font, "i", infoX + 4, ry + 5, hovered ? -1906448 : -10193781, false);
            }
            if (showNotes) {
                int noteIdx = this.hoverIdx >= 0 ? this.hoverIdx : this.selectedIdx;
                String note = noteIdx >= 0 && noteIdx < this.filtered.size() ? SimpleEditorScreen.this.getAttrNote(this.filtered.get(noteIdx)) : null;
                int noteY = ly + listH + 4;
                if (note != null && !note.isBlank()) {
                    String shown = font.method_1727(note) > w - 24 ? font.method_27523(note, w - 34) + ".." : note;
                    g.method_25294(x + 8, noteY, x + w - 8, noteY + 18, 538228902);
                    SimpleEditorScreen.this.drawBorder(g, x + 8, noteY, w - 16, 18, 1427421350);
                    VersionCompat.get().drawString(g, font, "i", x + 14, noteY + 5, -10262799, false);
                    VersionCompat.get().drawString(g, font, shown, x + 26, noteY + 5, -7035976, false);
                } else {
                    VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.attr_note_hint"), x + 10, noteY + 5, -10193781, false);
                }
            }
            int bottomY = y + h - 90;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.attr_amount"), x + 8, bottomY + 4, -7035976, false);
            int ax = x + 8 + font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.attr_amount")) + 4;
            int aw = 80;
            g.method_25294(ax, bottomY, ax + aw, bottomY + 18, -15592930);
            SimpleEditorScreen.this.drawBorder(g, ax, bottomY, aw, 18, this.focusField == 1 ? -10262799 : -14540234);
            VersionCompat.get().drawString(g, font, this.amountInput + (this.focusField == 1 && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), ax + 4, bottomY + 5, -1906448, false);
            int opY = bottomY + 22;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.attr_operation"), x + 8, opY + 4, -7035976, false);
            int opX = x + 8 + font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.attr_operation")) + 4;
            String[] opLabels = this.isZh() ? OP_NAMES_ZH : OP_NAMES_EN;
            for (int i = 0; i < 3; ++i) {
                boolean active;
                int bw = font.method_1727(opLabels[i]) + 10;
                boolean hover = mx >= opX && mx < opX + bw && my >= opY && my < opY + 18;
                boolean bl = active = i == this.selectedOp;
                g.method_25294(opX, opY, opX + bw, opY + 18, active ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
                VersionCompat.get().drawString(g, font, opLabels[i], opX + 5, opY + 5, active ? -1906448 : -7035976, false);
                opX += bw + 4;
            }
            int slotY = opY + 22;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.attr_slot"), x + 8, slotY + 4, -7035976, false);
            int slotX = x + 8 + font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.attr_slot")) + 4;
            for (int i = 0; i < SLOT_KEYS.length; ++i) {
                boolean active;
                String slotLabel = this.isZh() ? SLOT_ZH.getOrDefault(SLOT_KEYS[i], SLOT_KEYS[i]) : SLOT_KEYS[i];
                int bw = font.method_1727(slotLabel) + 8;
                boolean hover = mx >= slotX && mx < slotX + bw && my >= slotY && my < slotY + 18;
                boolean bl = active = i == this.selectedSlot;
                g.method_25294(slotX, slotY, slotX + bw, slotY + 18, active ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
                VersionCompat.get().drawString(g, font, slotLabel, slotX + 4, slotY + 5, active ? -1906448 : -7035976, false);
                if ((slotX += bw + 3) <= x + w - 40 || i >= SLOT_KEYS.length - 1) continue;
                slotX = x + 8 + font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.attr_slot")) + 4;
                slotY += 20;
            }
            int confirmY = y + h - 24;
            int confirmX = x + w - 78;
            boolean ch = mx >= confirmX && mx < confirmX + 70 && my >= confirmY && my < confirmY + 20;
            g.method_25294(confirmX, confirmY, confirmX + 70, confirmY + 20, ch ? -10262799 : -11581723);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.add.confirm"), confirmX + (70 - font.method_1727(SimpleEditorScreen.tr("ankinbt.add.confirm"))) / 2, confirmY + 6, -1906448, false);
        }

        private boolean isZh() {
            String lang = class_310.method_1551().field_1690.field_1883;
            return lang != null && lang.startsWith("zh");
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int sx = x + 8;
            int sy = y + 18;
            int sw = w - 16;
            int sh = 18;
            this.searchBox.method_46421(sx);
            this.searchBox.method_46419(sy);
            this.searchBox.method_25358(sw);
            if (this.searchBox.method_25402(mx, my, btn)) {
                this.focusField = 0;
                this.searchBox.method_25365(true);
                return true;
            }
            int bottomY = y + h - 90;
            int ax = x + 8 + SimpleEditorScreen.this.field_22793.method_1727(SimpleEditorScreen.tr("ankinbt.simple.attr_amount")) + 4;
            if (mx >= (double)ax && mx < (double)(ax + 80) && my >= (double)bottomY && my < (double)(bottomY + 18)) {
                this.focusField = 1;
                return true;
            }
            int opY = bottomY + 22;
            int opX = x + 8 + SimpleEditorScreen.this.field_22793.method_1727(SimpleEditorScreen.tr("ankinbt.simple.attr_operation")) + 4;
            String[] opLabels = this.isZh() ? OP_NAMES_ZH : OP_NAMES_EN;
            for (int i = 0; i < 3; ++i) {
                int bw = SimpleEditorScreen.this.field_22793.method_1727(opLabels[i]) + 10;
                if (mx >= (double)opX && mx < (double)(opX + bw) && my >= (double)opY && my < (double)(opY + 18)) {
                    this.selectedOp = i;
                    return true;
                }
                opX += bw + 4;
            }
            int slotY = opY + 22;
            int slotX = x + 8 + SimpleEditorScreen.this.field_22793.method_1727(SimpleEditorScreen.tr("ankinbt.simple.attr_slot")) + 4;
            for (int i = 0; i < SLOT_KEYS.length; ++i) {
                String slotLabel = this.isZh() ? SLOT_ZH.getOrDefault(SLOT_KEYS[i], SLOT_KEYS[i]) : SLOT_KEYS[i];
                int bw = SimpleEditorScreen.this.field_22793.method_1727(slotLabel) + 8;
                if (mx >= (double)slotX && mx < (double)(slotX + bw) && my >= (double)slotY && my < (double)(slotY + 18)) {
                    this.selectedSlot = i;
                    return true;
                }
                if ((slotX += bw + 3) <= x + w - 40 || i >= SLOT_KEYS.length - 1) continue;
                slotX = x + 8 + SimpleEditorScreen.this.field_22793.method_1727(SimpleEditorScreen.tr("ankinbt.simple.attr_slot")) + 4;
                slotY += 20;
            }
            int confirmY = y + h - 24;
            int confirmX = x + w - 78;
            if (mx >= (double)confirmX && mx < (double)(confirmX + 70) && my >= (double)confirmY && my < (double)(confirmY + 20)) {
                this.confirm();
                return true;
            }
            if (this.hoverIdx >= 0 && this.hoverIdx < this.filtered.size()) {
                this.selectedIdx = this.hoverIdx;
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) {
                this.confirm();
                return true;
            }
            if (key == 258) {
                this.focusField = (this.focusField + 1) % 2;
                return true;
            }
            if (this.focusField == 1) {
                if (key == 259 && this.amountCursor > 0 && !this.amountInput.isEmpty()) {
                    this.amountInput = this.amountInput.substring(0, this.amountCursor - 1) + this.amountInput.substring(this.amountCursor);
                    --this.amountCursor;
                    return true;
                }
                if (key == 263 && this.amountCursor > 0) {
                    --this.amountCursor;
                    return true;
                }
                if (key == 262 && this.amountCursor < this.amountInput.length()) {
                    ++this.amountCursor;
                    return true;
                }
            } else {
                if (this.searchBox.method_25404(key, scan, mod)) {
                    this.searchQ = this.searchBox.method_1882();
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c >= ' ') {
                if (this.focusField == 1) {
                    if (c >= '0' && c <= '9' || c == '.' || c == '-') {
                        this.amountInput = this.amountInput.substring(0, this.amountCursor) + c + this.amountInput.substring(this.amountCursor);
                        ++this.amountCursor;
                    }
                } else {
                    this.searchBox.method_25400(c, mod);
                    this.searchQ = this.searchBox.method_1882();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            this.scrollOff -= (int)sy * 3;
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.filtered.size() - 10)));
            return true;
        }

        private class_9274 slotFromKey(String key) {
            return switch (key) {
                case "mainhand" -> class_9274.field_49217;
                case "offhand" -> class_9274.field_49218;
                case "head" -> class_9274.field_49223;
                case "chest" -> class_9274.field_49222;
                case "legs" -> class_9274.field_49221;
                case "feet" -> class_9274.field_49220;
                case "hand" -> class_9274.field_49219;
                case "armor" -> class_9274.field_49224;
                default -> class_9274.field_49216;
            };
        }

        private class_1322.class_1323 opFromIndex(int idx) {
            return switch (idx) {
                case 1 -> class_1322.class_1323.field_6330;
                case 2 -> class_1322.class_1323.field_6331;
                default -> class_1322.class_1323.field_6328;
            };
        }

        private void confirm() {
            if (this.selectedIdx < 0 || this.selectedIdx >= this.filtered.size()) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.select_attr_first"), -1096636);
                return;
            }
            try {
                double amount = Double.parseDouble(this.amountInput);
                SimpleEditorScreen.this.addAttribute(this.filtered.get(this.selectedIdx), amount, this.opFromIndex(this.selectedOp), this.slotFromKey(SLOT_KEYS[this.selectedSlot]));
            }
            catch (NumberFormatException e) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.invalid_number"), -1096636);
            }
        }
    }

    class LoreTextEditorSubEditor
    implements SubEditor {
        private final List<String> lines = new ArrayList<String>();
        private final List<FlatEditBox> lineBoxes = new ArrayList<FlatEditBox>();
        private int activeLine = 0;
        private int scrollOff = 0;
        private boolean showRawCodes = true;

        LoreTextEditorSubEditor() {
            List<class_2561> lore = SimpleEditorScreen.this.getLore();
            if (lore.isEmpty()) {
                this.lines.add("");
            } else {
                for (int i = 0; i < lore.size(); ++i) {
                    this.lines.add(SimpleEditorScreen.this.getLoreRawText(i));
                }
            }
            this.activeLine = Math.max(0, this.lines.size() - 1);
            this.rebuildLineBoxes();
            this.activeBox().method_1883(this.activeBox().method_1882().length(), false);
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 420);
            int dh = Math.min(h - 10, 320);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.lore_text_editor"), dx + 10, dy + 8, -1906448, false);
            String rawLabel = this.showRawCodes ? SimpleEditorScreen.tr("ankinbt.simple.lore_show_preview") : SimpleEditorScreen.tr("ankinbt.simple.lore_show_raw");
            int rawBtnW = font.method_1727(rawLabel) + 10;
            int rawBtnX = dx + dw - rawBtnW - 10;
            boolean rawBtnHover = mx >= rawBtnX && mx < rawBtnX + rawBtnW && my >= dy + 4 && my < dy + 18;
            g.method_25294(rawBtnX, dy + 4, rawBtnX + rawBtnW, dy + 18, rawBtnHover ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, rawLabel, rawBtnX + 5, dy + 8, rawBtnHover ? -1906448 : -7035976, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);

            int textX = dx + 10;
            int textY = dy + 28;
            int textW = dw - 20;
            int textH = dh - 90;
            int contentX = textX + 24;
            int lineH = 14;
            int maxVisLines = Math.max(1, textH / lineH);
            this.scrollToCursor(maxVisLines);
            g.method_25294(textX - 2, textY - 2, textX + textW + 2, textY + textH + 2, -15592930);
            SimpleEditorScreen.this.drawBorder(g, textX - 2, textY - 2, textW + 4, textH + 4, -14540234);
            g.method_44379(textX, textY, textX + textW, textY + textH);
            int end = Math.min(this.scrollOff + maxVisLines, this.lines.size());
            for (int i = this.scrollOff; i < end; ++i) {
                int ly = textY + (i - this.scrollOff) * lineH;
                String line = this.lines.get(i);
                FlatEditBox box = this.lineBoxes.get(i);
                VersionCompat.get().drawString(g, font, String.valueOf(i + 1), textX, ly + 2, -10193781, false);
                if (i == this.activeLine) {
                    g.method_25294(contentX - 2, ly, textX + textW, ly + lineH, 0x18FFFFFF);
                }
                this.configureLineBox(box, contentX, ly, textW - 28, i == this.activeLine);
                if (this.showRawCodes) {
                    box.method_25394(g, mx, my, 0.0f);
                } else {
                    VersionCompat.get().drawString(g, font, SimpleEditorScreen.colorCodedToComponent(line), contentX, ly + 2, -1906448, false);
                }
            }
            g.method_44380();
            if (this.lines.size() > maxVisLines) {
                int sbx = textX + textW - 4;
                g.method_25294(sbx, textY, sbx + 4, textY + textH, 0x30FFFFFF);
                float ratio = (float)maxVisLines / (float)this.lines.size();
                int thumbH = Math.max(8, (int)((float)textH * ratio));
                float sr = (float)this.scrollOff / (float)Math.max(1, this.lines.size() - maxVisLines);
                int thumbY = textY + (int)((float)(textH - thumbH) * sr);
                g.method_25294(sbx, thumbY, sbx + 4, thumbY + thumbH, 0x70FFFFFF);
            }

            int prevY = textY + textH + 8;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.preview") + ":", dx + 10, prevY, -7035976, false);
            prevY += 12;
            int previewLines = Math.min(this.lines.size(), 3);
            for (int i = 0; i < previewLines; ++i) {
                class_2561 preview = SimpleEditorScreen.colorCodedToComponent(this.lines.get(i));
                VersionCompat.get().drawString(g, font, preview, dx + 14, prevY + i * 11, -1906448, false);
            }
            if (this.lines.size() > 3) {
                VersionCompat.get().drawString(g, font, "... +" + (this.lines.size() - 3), dx + 14, prevY + 33, -10193781, false);
            }

            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int palX = dx + 10;
            boolean palH = mx >= palX && mx < palX + 50 && my >= by && my < by + bh2;
            g.method_25294(palX, by, palX + 50, by + bh2, palH ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.color_palette"), palX + (50 - font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.color_palette"))) / 2, by + 6, -7035976, false);
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.method_25294(cancelX, by, cancelX + bw, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.method_25294(okX, by, okX + bw, by + bh2, oh ? -10262799 : -11581723);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.apply"), okX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.apply"))) / 2, by + 6, -1906448, false);
            String state = this.activeLine + 1 + ":" + this.cursorCol() + " | " + this.lines.size() + SimpleEditorScreen.tr("ankinbt.simple.lore_lines_suffix");
            VersionCompat.get().drawString(g, font, state, dx + dw - font.method_1727(state) - 10, by + 6, -10193781, false);
        }

        private void scrollToCursor(int maxVisLines) {
            if (this.activeLine < this.scrollOff) {
                this.scrollOff = this.activeLine;
            }
            if (this.activeLine >= this.scrollOff + maxVisLines) {
                this.scrollOff = this.activeLine - maxVisLines + 1;
            }
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.lines.size() - maxVisLines)));
        }

        private FlatEditBox activeBox() {
            this.ensureLineBoxes();
            return this.lineBoxes.get(Math.max(0, Math.min(this.activeLine, this.lineBoxes.size() - 1)));
        }

        private void ensureLineBoxes() {
            while (this.lineBoxes.size() < this.lines.size()) {
                this.lineBoxes.add(this.newLineBox(this.lines.get(this.lineBoxes.size())));
            }
            while (this.lineBoxes.size() > this.lines.size()) {
                this.lineBoxes.remove(this.lineBoxes.size() - 1);
            }
        }

        private FlatEditBox newLineBox(String value) {
            FlatEditBox box = new FlatEditBox(SimpleEditorScreen.this.field_22793, 0, 0, 1, 14, (class_2561)class_2561.method_43473());
            box.method_1852(2048);
            box.method_1852(value == null ? "" : value);
            return box;
        }

        private void rebuildLineBoxes() {
            this.lineBoxes.clear();
            for (String line : this.lines) {
                this.lineBoxes.add(this.newLineBox(line));
            }
            this.focusActiveBox();
        }

        private void configureLineBox(FlatEditBox box, int x, int y, int w, boolean focused) {
            box.method_46421(x);
            box.method_46419(y);
            box.method_25358(Math.max(1, w));
            box.method_25365(focused);
        }

        private void focusActiveBox() {
            this.ensureLineBoxes();
            for (int i = 0; i < this.lineBoxes.size(); ++i) {
                this.lineBoxes.get(i).method_25365(i == this.activeLine);
            }
        }

        private int cursorCol() {
            FlatEditBox box = this.activeBox();
            return Math.max(0, Math.min(box.method_1881(), box.method_1882().length()));
        }

        private void syncLine(int index) {
            if (index >= 0 && index < this.lines.size() && index < this.lineBoxes.size()) {
                this.lines.set(index, this.lineBoxes.get(index).method_1882());
            }
        }

        private void syncAllLines() {
            this.ensureLineBoxes();
            for (int i = 0; i < this.lines.size(); ++i) {
                this.syncLine(i);
            }
        }

        private void setActiveLine(int line) {
            this.syncLine(this.activeLine);
            this.activeLine = Math.max(0, Math.min(line, this.lines.size() - 1));
            this.focusActiveBox();
        }

        private boolean hasLineSelection(FlatEditBox box) {
            String selected = box.method_1866();
            return selected != null && !selected.isEmpty();
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 420);
            int dh = Math.min(h - 10, 320);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            String rawLabel = this.showRawCodes ? SimpleEditorScreen.tr("ankinbt.simple.lore_show_preview") : SimpleEditorScreen.tr("ankinbt.simple.lore_show_raw");
            int rawBtnW = SimpleEditorScreen.this.field_22793.method_1727(rawLabel) + 10;
            int rawBtnX = dx + dw - rawBtnW - 10;
            if (mx >= (double)rawBtnX && mx < (double)(rawBtnX + rawBtnW) && my >= (double)(dy + 4) && my < (double)(dy + 18)) {
                this.showRawCodes = !this.showRawCodes;
                return true;
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int palX = dx + 10;
            if (mx >= (double)palX && mx < (double)(palX + 50) && my >= (double)by && my < (double)(by + bh2)) {
                InlineFieldEditor tempEditor = new InlineFieldEditor("lore_text_temp", this.activeBox().method_1882(), true);
                SimpleEditorScreen.this.activeSubEditor = new LoreColorInsertEditorForText(this, tempEditor);
                return true;
            }
            int cancelX = dx + dw / 2 - bw - 6;
            if (mx >= (double)cancelX && mx < (double)(cancelX + bw) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            int okX = dx + dw / 2 + 6;
            if (mx >= (double)okX && mx < (double)(okX + bw) && my >= (double)by && my < (double)(by + bh2)) {
                this.applyAll();
                return true;
            }
            int textX = dx + 10;
            int textY = dy + 28;
            int textW = dw - 20;
            int textH = dh - 90;
            if (mx >= (double)textX && mx < (double)(textX + textW) && my >= (double)textY && my < (double)(textY + textH)) {
                int lineH = 14;
                int clickedLine = (int)((my - (double)textY) / (double)lineH) + this.scrollOff;
                if (clickedLine >= 0 && clickedLine < this.lines.size()) {
                    int contentX = textX + 24;
                    int ly = textY + (clickedLine - this.scrollOff) * lineH;
                    FlatEditBox box = this.lineBoxes.get(clickedLine);
                    this.configureLineBox(box, contentX, ly, textW - 28, true);
                    this.setActiveLine(clickedLine);
                    if (this.showRawCodes) {
                        box.method_25402(mx, my, btn);
                    } else {
                        box.method_1883(box.method_1882().length(), false);
                    }
                }
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY, int x, int y, int w, int h) {
            return false;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            FlatEditBox box = this.activeBox();
            if (key == 257 || key == 335) {
                this.syncLine(this.activeLine);
                int col = this.cursorCol();
                String line = this.lines.get(this.activeLine);
                this.lines.set(this.activeLine, line.substring(0, col));
                this.lines.add(this.activeLine + 1, line.substring(col));
                this.rebuildLineBoxes();
                this.setActiveLine(this.activeLine + 1);
                this.activeBox().method_1883(0, false);
                return true;
            }
            if (key == 259 && this.cursorCol() == 0 && !this.hasLineSelection(box) && this.activeLine > 0) {
                this.syncLine(this.activeLine);
                String line = this.lines.remove(this.activeLine);
                int prev = this.activeLine - 1;
                int col = this.lines.get(prev).length();
                this.lines.set(prev, this.lines.get(prev) + line);
                this.rebuildLineBoxes();
                this.setActiveLine(prev);
                this.activeBox().method_1883(col, false);
                return true;
            }
            if (key == 261 && this.cursorCol() == box.method_1882().length() && !this.hasLineSelection(box) && this.activeLine < this.lines.size() - 1) {
                this.syncLine(this.activeLine);
                this.lines.set(this.activeLine, this.lines.get(this.activeLine) + this.lines.remove(this.activeLine + 1));
                this.rebuildLineBoxes();
                this.setActiveLine(this.activeLine);
                return true;
            }
            if (key == 265 && this.activeLine > 0) {
                int col = this.cursorCol();
                this.setActiveLine(this.activeLine - 1);
                this.activeBox().method_1883(Math.min(col, this.activeBox().method_1882().length()), false);
                return true;
            }
            if (key == 264 && this.activeLine < this.lines.size() - 1) {
                int col = this.cursorCol();
                this.setActiveLine(this.activeLine + 1);
                this.activeBox().method_1883(Math.min(col, this.activeBox().method_1882().length()), false);
                return true;
            }
            if (key == 263 && this.cursorCol() == 0 && this.activeLine > 0 && !this.hasLineSelection(box)) {
                this.setActiveLine(this.activeLine - 1);
                this.activeBox().method_1883(this.activeBox().method_1882().length(), false);
                return true;
            }
            if (key == 262 && this.cursorCol() == box.method_1882().length() && this.activeLine < this.lines.size() - 1 && !this.hasLineSelection(box)) {
                this.setActiveLine(this.activeLine + 1);
                this.activeBox().method_1883(0, false);
                return true;
            }
            if (box.method_25404(key, scan, mod)) {
                this.syncLine(this.activeLine);
                return true;
            }
            this.syncLine(this.activeLine);
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            FlatEditBox box = this.activeBox();
            if (box.method_25400(c, mod)) {
                this.syncLine(this.activeLine);
                return true;
            }
            this.syncLine(this.activeLine);
            return true;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            this.scrollOff -= (int)sy * 3;
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.lines.size() - 5)));
            return true;
        }

        void insertAtCursor(String text) {
            String suffix = text != null && text.length() == 2 && text.charAt(0) == '&' && "0123456789abcdefABCDEF".indexOf(text.charAt(1)) >= 0 ? "&r" : "";
            FlatEditBox box = this.activeBox();
            String selected = box.method_1866();
            if (selected != null && !selected.isEmpty() && !suffix.isEmpty()) {
                box.method_1853((text == null ? "" : text) + selected + suffix);
            } else {
                box.method_1853(text == null ? "" : text);
            }
            this.syncLine(this.activeLine);
        }

        private void applyAll() {
            this.syncAllLines();
            while (this.lines.size() > 1 && this.lines.get(this.lines.size() - 1).isEmpty()) {
                this.lines.remove(this.lines.size() - 1);
            }
            ArrayList<class_2561> loreComponents = new ArrayList<class_2561>();
            for (String line : this.lines) {
                if (!line.isEmpty() || this.lines.size() == 1) {
                    loreComponents.add(SimpleEditorScreen.colorCodedToComponent(line));
                    continue;
                }
                loreComponents.add((class_2561)class_2561.method_43473());
            }
            if (loreComponents.size() == 1 && this.lines.get(0).isEmpty()) {
                SimpleEditorScreen.this.clearLoreComponent();
            } else {
                SimpleEditorScreen.this.setLore(loreComponents);
            }
            SimpleEditorScreen.this.markDirty();
            SimpleEditorScreen.this.activeSubEditor = null;
        }
    }

    class EnchantPickerSubEditor
    implements SubEditor {
        private final List<String> allEnchants = new ArrayList<String>();
        private List<String> filtered = new ArrayList<String>();
        private String searchQ = "";
        private final FlatEditBox searchBox;
        private int scrollOff = 0;
        private int hoverIdx = -1;
        private int selectedIdx = -1;
        private String levelInput = "1";
        private int levelCursor = 1;
        private boolean focusLevel = false;

        EnchantPickerSubEditor() {
            this.searchBox = new FlatEditBox(SimpleEditorScreen.this.field_22793, 0, 0, 1, 18, (class_2561)class_2561.method_43473());
            this.searchBox.method_1852(128);
            this.searchBox.method_47404((class_2561)class_2561.method_43471((String)"ankinbt.search.hint"));
            this.searchBox.method_1863(value -> {
                this.searchQ = value == null ? "" : value;
                this.filter();
            });
            this.searchBox.method_25365(true);
            try {
                this.allEnchants.addAll(VersionCompat.get().getAllEnchantIds());
            } catch (Throwable ignored) {
            }
            if (this.allEnchants.isEmpty()) {
                this.allEnchants.addAll(ENCHANT_ZH.keySet());
            }
            Collections.sort(this.allEnchants);
            this.filtered = new ArrayList<String>(this.allEnchants);
        }

        private void filter() {
            if (this.searchQ.isEmpty()) {
                this.filtered = new ArrayList<String>(this.allEnchants);
            } else {
                String q = this.searchQ.toLowerCase();
                this.filtered = this.allEnchants.stream().filter(s -> {
                    if (s.toLowerCase().contains(q)) {
                        return true;
                    }
                    String zh = ENCHANT_ZH.get(s);
                    return zh != null && zh.contains(q);
                }).collect(Collectors.toList());
            }
            this.scrollOff = 0;
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.pick_enchant"), x + 8, y + 4, -1906448, false);
            int sx = x + 8;
            int sy = y + 18;
            int sw = w - 16;
            int sh = 18;
            g.method_25294(sx, sy, sx + sw, sy + sh, -15592930);
            this.searchBox.method_46421(sx);
            this.searchBox.method_46419(sy);
            this.searchBox.method_25358(sw);
            this.searchBox.method_25365(!this.focusLevel);
            this.searchBox.method_25394(g, mx, my, 0.0f);
            int ly = sy + sh + 4;
            int listH = h - 80;
            int maxItems = listH / 16;
            this.hoverIdx = -1;
            int end = Math.min(this.scrollOff + maxItems, this.filtered.size());
            for (int i = this.scrollOff; i < end; ++i) {
                boolean sel;
                boolean hovered;
                int ry = ly + (i - this.scrollOff) * 16;
                boolean bl = hovered = mx >= x + 8 && mx < x + w - 8 && my >= ry && my < ry + 16;
                if (hovered) {
                    this.hoverIdx = i;
                }
                boolean bl2 = sel = i == this.selectedIdx;
                if (sel) {
                    g.method_25294(x + 8, ry, x + w - 8, ry + 16, 677603057);
                } else if (hovered) {
                    g.method_25294(x + 8, ry, x + w - 8, ry + 16, 0x30FFFFFF);
                }
                String displayName = SimpleEditorScreen.this.getEnchantDisplayName(this.filtered.get(i));
                VersionCompat.get().drawString(g, font, displayName, x + 12, ry + 4, sel ? -1906448 : -7035976, false);
            }
            int by = y + h - 30;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.level"), x + 8, by + 6, -7035976, false);
            int lx = x + 8 + font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.level")) + 4;
            g.method_25294(lx, by + 2, lx + 40, by + 20, -15592930);
            SimpleEditorScreen.this.drawBorder(g, lx, by + 2, 40, 18, this.focusLevel ? -10262799 : -14540234);
            VersionCompat.get().drawString(g, font, this.levelInput + (this.focusLevel && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), lx + 4, by + 7, -1906448, false);
            int confirmX = x + w - 78;
            boolean ch = mx >= confirmX && mx < confirmX + 70 && my >= by + 1 && my < by + 21;
            g.method_25294(confirmX, by + 1, confirmX + 70, by + 21, ch ? -10262799 : -11581723);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.add.confirm"), confirmX + (70 - font.method_1727(SimpleEditorScreen.tr("ankinbt.add.confirm"))) / 2, by + 7, -1906448, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int sx = x + 8;
            int sy = y + 18;
            int sw = w - 16;
            int sh = 18;
            this.searchBox.method_46421(sx);
            this.searchBox.method_46419(sy);
            this.searchBox.method_25358(sw);
            if (this.searchBox.method_25402(mx, my, btn)) {
                this.focusLevel = false;
                this.searchBox.method_25365(true);
                return true;
            }
            int by = y + h - 30;
            int lx = x + 8 + SimpleEditorScreen.this.field_22793.method_1727(SimpleEditorScreen.tr("ankinbt.simple.level")) + 4;
            if (mx >= (double)lx && mx < (double)(lx + 40) && my >= (double)(by + 2) && my < (double)(by + 20)) {
                this.focusLevel = true;
                return true;
            }
            int confirmX = x + w - 78;
            if (mx >= (double)confirmX && mx < (double)(confirmX + 70) && my >= (double)(by + 1) && my < (double)(by + 21)) {
                this.confirm();
                return true;
            }
            if (this.hoverIdx >= 0 && this.hoverIdx < this.filtered.size()) {
                this.selectedIdx = this.hoverIdx;
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) {
                this.confirm();
                return true;
            }
            if (key == 258) {
                this.focusLevel = !this.focusLevel;
                return true;
            }
            if (this.focusLevel) {
                if (key == 259 && this.levelCursor > 0 && !this.levelInput.isEmpty()) {
                    this.levelInput = this.levelInput.substring(0, this.levelCursor - 1) + this.levelInput.substring(this.levelCursor);
                    --this.levelCursor;
                    return true;
                }
                if (key == 263 && this.levelCursor > 0) {
                    --this.levelCursor;
                    return true;
                }
                if (key == 262 && this.levelCursor < this.levelInput.length()) {
                    ++this.levelCursor;
                    return true;
                }
            } else {
                if (this.searchBox.method_25404(key, scan, mod)) {
                    this.searchQ = this.searchBox.method_1882();
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c >= ' ') {
                if (this.focusLevel) {
                    if (c >= '0' && c <= '9') {
                        this.levelInput = this.levelInput.substring(0, this.levelCursor) + c + this.levelInput.substring(this.levelCursor);
                        ++this.levelCursor;
                    }
                } else {
                    this.searchBox.method_25400(c, mod);
                    this.searchQ = this.searchBox.method_1882();
                }
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            this.scrollOff -= (int)sy * 3;
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.filtered.size() - 10)));
            return true;
        }

        private void confirm() {
            if (this.selectedIdx < 0 || this.selectedIdx >= this.filtered.size()) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.select_enchant_first"), -1096636);
                return;
            }
            try {
                int level = Integer.parseInt(this.levelInput);
                if (level < 1) {
                    level = 1;
                }
                SimpleEditorScreen.this.addEnchantment(this.filtered.get(this.selectedIdx), level);
            }
            catch (NumberFormatException e) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.invalid_number"), -1096636);
            }
        }
    }

    class PotionEffectSubEditor
    implements SubEditor {
        private final FlatEditBox searchBox;
        private final FlatEditBox durationBox;
        private final FlatEditBox amplifierBox;
        private final List<String> filtered = new ArrayList<String>();
        private int focusField = 0;
        private int hoverIdx = -1;
        private int selectedIdx = 0;
        private int scrollOff = 0;
        private int visibleRows = 7;
        private boolean ambient = false;
        private boolean particles = true;
        private boolean icon = true;

        PotionEffectSubEditor() {
            this.searchBox = new FlatEditBox(SimpleEditorScreen.this.field_22793, 0, 0, 1, 18, (class_2561)class_2561.method_43473());
            this.searchBox.method_1852(128);
            this.searchBox.method_47404((class_2561)class_2561.method_43471((String)"ankinbt.search.hint"));
            this.searchBox.method_1863(value -> this.filter());
            this.durationBox = this.numericBox("600");
            this.amplifierBox = this.numericBox("0");
            this.filter();
        }

        private FlatEditBox numericBox(String value) {
            FlatEditBox box = new FlatEditBox(SimpleEditorScreen.this.field_22793, 0, 0, 1, 18, (class_2561)class_2561.method_43473());
            box.method_1852(16);
            box.method_1852(value);
            return box;
        }

        private void filter() {
            String q = this.searchBox.method_1882().toLowerCase(Locale.ROOT);
            this.filtered.clear();
            for (String id : EFFECT_IDS) {
                if (!q.isEmpty() && !id.toLowerCase(Locale.ROOT).contains(q) && !SimpleEditorScreen.this.effectDisplayName(id).toLowerCase(Locale.ROOT).contains(q)) continue;
                this.filtered.add(id);
            }
            this.selectedIdx = Math.min(this.selectedIdx, Math.max(0, this.filtered.size() - 1));
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.filtered.size() - this.visibleRows)));
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int maxItems;
            int dw = Math.max(260, Math.min(w - 12, 620));
            int dh = Math.max(220, Math.min(h - 12, 340));
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.pick_effect"), dx + 10, dy + 8, -1906448, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int sx = dx + 10;
            int sy = dy + 30;
            int sw = dw - 20;
            int sh = 18;
            this.renderSmallFlatEditBox(g, this.searchBox, sx, sy, sw, sh, this.focusField == 0, mx, my);
            int listY = sy + sh + 6;
            int rowH = 18;
            this.visibleRows = maxItems = Math.max(4, Math.min(10, (dh - 160) / rowH));
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.filtered.size() - maxItems)));
            this.hoverIdx = -1;
            int end = Math.min(this.filtered.size(), this.scrollOff + maxItems);
            for (int i = this.scrollOff; i < end; ++i) {
                boolean selected;
                boolean hover;
                int ry = listY + (i - this.scrollOff) * rowH;
                boolean bl = hover = mx >= dx + 10 && mx < dx + dw - 10 && my >= ry && my < ry + rowH;
                if (hover) {
                    this.hoverIdx = i;
                }
                boolean bl2 = selected = i == this.selectedIdx;
                g.method_25294(dx + 10, ry, dx + dw - 10, ry + rowH - 1, selected ? 677603057 : (hover ? 0x30FFFFFF : 0));
                Object idText = this.filtered.get(i).replace("minecraft:", "");
                int idW = Math.min(150, Math.max(70, sw / 3));
                if (font.method_1727((String)idText) > idW) {
                    idText = font.method_27523((String)idText, idW - 6) + "..";
                }
                Object label = SimpleEditorScreen.this.effectDisplayName(this.filtered.get(i));
                int labelW = sw - idW - 18;
                if (font.method_1727((String)label) > labelW) {
                    label = font.method_27523((String)label, labelW - 6) + "..";
                }
                VersionCompat.get().drawString(g, font, (String)label, dx + 14, ry + 5, selected ? -1906448 : -7035976, false);
                VersionCompat.get().drawString(g, font, (String)idText, dx + dw - 14 - font.method_1727((String)idText), ry + 5, -10193781, false);
            }
            int formY = listY + maxItems * rowH + 8;
            String durationLabel = SimpleEditorScreen.tr("ankinbt.simple.effect_duration");
            String amplifierLabel = SimpleEditorScreen.tr("ankinbt.simple.effect_amplifier");
            int durationBoxX = dx + 10 + font.method_1727(durationLabel) + 8;
            int amplifierLabelX = durationBoxX + 60 + 12;
            int amplifierBoxX = amplifierLabelX + font.method_1727(amplifierLabel) + 8;
            boolean compactForm = amplifierBoxX + 46 > dx + dw - 10;
            VersionCompat.get().drawString(g, font, durationLabel, dx + 10, formY + 5, -7035976, false);
            this.renderSmallFlatEditBox(g, this.durationBox, durationBoxX, formY, 54, 18, this.focusField == 1, mx, my);
            int ampY = compactForm ? formY + 24 : formY;
            int ampLabelX = compactForm ? dx + 10 : amplifierLabelX;
            int ampBoxX = compactForm ? dx + 10 + font.method_1727(amplifierLabel) + 8 : amplifierBoxX;
            VersionCompat.get().drawString(g, font, amplifierLabel, ampLabelX, ampY + 5, -7035976, false);
            this.renderSmallFlatEditBox(g, this.amplifierBox, ampBoxX, ampY, 42, 18, this.focusField == 2, mx, my);
            int toggleY = compactForm ? formY + 48 : formY + 24;
            int tx = dx + 10;
            tx = this.renderToggle(g, font, mx, my, tx, toggleY, SimpleEditorScreen.tr("ankinbt.simple.effect_ambient"), this.ambient);
            tx = this.renderToggle(g, font, mx, my, tx + 4, toggleY, SimpleEditorScreen.tr("ankinbt.simple.effect_particles"), this.particles);
            if (tx + font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.effect_icon")) + 18 > dx + dw - 10) {
                tx = dx + 10;
                toggleY += 22;
            }
            this.renderToggle(g, font, mx, my, tx + 4, toggleY, SimpleEditorScreen.tr("ankinbt.simple.effect_icon"), this.icon);
            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.method_25294(cancelX, by, cancelX + bw, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.method_25294(okX, by, okX + bw, by + bh2, oh ? -10262799 : -11581723);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.add.confirm"), okX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.add.confirm"))) / 2, by + 6, -1906448, false);
        }

        private void renderSmallFlatEditBox(class_332 g, FlatEditBox box, int x, int y, int w, int h, boolean focused, int mx, int my) {
            box.method_46421(x);
            box.method_46419(y);
            box.method_25358(w);
            box.method_25365(focused);
            box.method_25394(g, mx, my, 0.0f);
        }

        private int renderToggle(class_332 g, class_327 font, int mx, int my, int x, int y, String label, boolean on) {
            boolean hover;
            int bw = font.method_1727(label) + 14;
            boolean bl = hover = mx >= x && mx < x + bw && my >= y && my < y + 18;
            g.method_25294(x, y, x + bw, y + 18, on ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
            VersionCompat.get().drawString(g, font, label, x + 7, y + 5, on ? -1906448 : -7035976, false);
            return x + bw;
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int ampBoxX;
            boolean compactForm;
            int dw = Math.max(260, Math.min(w - 12, 620));
            int dh = Math.max(220, Math.min(h - 12, 340));
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int sx = dx + 10;
            int sy = dy + 30;
            int sw = dw - 20;
            int sh = 18;
            this.searchBox.method_46421(sx);
            this.searchBox.method_46419(sy);
            this.searchBox.method_25358(sw);
            if (this.searchBox.method_25402(mx, my, btn)) { this.focusField = 0; this.searchBox.method_25365(true); return true; }
            if (this.hoverIdx >= 0 && this.hoverIdx < this.filtered.size()) {
                this.selectedIdx = this.hoverIdx;
                return true;
            }
            int rowH = 18;
            int maxItems = Math.max(4, Math.min(10, (dh - 160) / rowH));
            int formY = sy + sh + 6 + maxItems * rowH + 8;
            String durationLabel = SimpleEditorScreen.tr("ankinbt.simple.effect_duration");
            String amplifierLabel = SimpleEditorScreen.tr("ankinbt.simple.effect_amplifier");
            int durationBoxX = dx + 10 + SimpleEditorScreen.this.field_22793.method_1727(durationLabel) + 8;
            int amplifierLabelX = durationBoxX + 60 + 12;
            int amplifierBoxX = amplifierLabelX + SimpleEditorScreen.this.field_22793.method_1727(amplifierLabel) + 8;
            boolean bl = compactForm = amplifierBoxX + 46 > dx + dw - 10;
            this.durationBox.method_46421(durationBoxX);
            this.durationBox.method_46419(formY);
            this.durationBox.method_25358(54);
            if (this.durationBox.method_25402(mx, my, btn)) { this.focusField = 1; this.durationBox.method_25365(true); return true; }
            int ampY = compactForm ? formY + 24 : formY;
            int n = ampBoxX = compactForm ? dx + 10 + SimpleEditorScreen.this.field_22793.method_1727(amplifierLabel) + 8 : amplifierBoxX;
            this.amplifierBox.method_46421(ampBoxX);
            this.amplifierBox.method_46419(ampY);
            this.amplifierBox.method_25358(42);
            if (this.amplifierBox.method_25402(mx, my, btn)) { this.focusField = 2; this.amplifierBox.method_25365(true); return true; }
            int ambientX = dx + 10;
            int toggleY = compactForm ? formY + 48 : formY + 24;
            if (this.clickToggle(mx, my, ambientX, toggleY, SimpleEditorScreen.tr("ankinbt.simple.effect_ambient"))) {
                this.ambient = !this.ambient;
                return true;
            }
            int particlesX = ambientX + SimpleEditorScreen.this.field_22793.method_1727(SimpleEditorScreen.tr("ankinbt.simple.effect_ambient")) + 18;
            if (this.clickToggle(mx, my, particlesX, toggleY, SimpleEditorScreen.tr("ankinbt.simple.effect_particles"))) {
                this.particles = !this.particles;
                return true;
            }
            int iconX = particlesX + SimpleEditorScreen.this.field_22793.method_1727(SimpleEditorScreen.tr("ankinbt.simple.effect_particles")) + 18;
            if (iconX + SimpleEditorScreen.this.field_22793.method_1727(SimpleEditorScreen.tr("ankinbt.simple.effect_icon")) + 18 > dx + dw - 10) {
                iconX = dx + 14;
                toggleY += 22;
            }
            if (this.clickToggle(mx, my, iconX, toggleY, SimpleEditorScreen.tr("ankinbt.simple.effect_icon"))) {
                this.icon = !this.icon;
                return true;
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            if (mx >= (double)cancelX && mx < (double)(cancelX + bw) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            int okX = dx + dw / 2 + 6;
            if (mx >= (double)okX && mx < (double)(okX + bw) && my >= (double)by && my < (double)(by + bh2)) {
                this.confirm();
                return true;
            }
            return true;
        }

        private boolean clickToggle(double mx, double my, int x, int y, String label) {
            int bw = SimpleEditorScreen.this.field_22793.method_1727(label) + 14;
            return mx >= (double)x && mx < (double)(x + bw) && my >= (double)y && my < (double)(y + 18);
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) {
                this.confirm();
                return true;
            }
            if (key == 258) {
                this.focusField = (this.focusField + 1) % 3;
                return true;
            }
            if (this.focusField == 0) {
                if (key == 264 && this.selectedIdx < this.filtered.size() - 1) {
                    ++this.selectedIdx;
                    if (this.selectedIdx >= this.scrollOff + this.visibleRows) {
                        ++this.scrollOff;
                    }
                    return true;
                }
                if (key == 265 && this.selectedIdx > 0) {
                    --this.selectedIdx;
                    if (this.selectedIdx < this.scrollOff) {
                        this.scrollOff = this.selectedIdx;
                    }
                    return true;
                }
                if (this.searchBox.method_25404(key, scan, mod)) {
                    return true;
                }
            } else {
                FlatEditBox box = this.focusField == 1 ? this.durationBox : this.amplifierBox;
                if (box.method_25404(key, scan, mod)) {
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (this.focusField == 0) {
                if (this.searchBox.method_25400(c, mod)) return true;
            } else if (c >= '0' && c <= '9' || c == '-') {
                (this.focusField == 1 ? this.durationBox : this.amplifierBox).method_25400(c, mod);
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            this.scrollOff -= (int)sy * 3;
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.filtered.size() - this.visibleRows)));
            return true;
        }

        private void confirm() {
            if (this.selectedIdx < 0 || this.selectedIdx >= this.filtered.size()) {
                return;
            }
            int dur = this.parsePotionInt(this.durationBox.method_1882(), 600);
            int amp = this.parsePotionInt(this.amplifierBox.method_1882(), 0);
            SimpleEditorScreen.this.addPotionCustomEffect(this.filtered.get(this.selectedIdx), dur, amp, this.ambient, this.particles, this.icon);
            SimpleEditorScreen.this.activeSubEditor = null;
        }

        private int parsePotionInt(String value, int fallback) {
            try {
                return Integer.parseInt(value == null || value.isBlank() ? String.valueOf(fallback) : value.trim());
            }
            catch (NumberFormatException ignored) {
                return fallback;
            }
        }
    }

    class PotionPickerSubEditor
    implements SubEditor {
        private final FlatEditBox searchBox;
        private final List<String> filtered = new ArrayList<String>();
        private int hoverIdx = -1;
        private int selectedIdx = 0;
        private int scrollOff = 0;

        PotionPickerSubEditor() {
            this.searchBox = new FlatEditBox(SimpleEditorScreen.this.field_22793, 0, 0, 1, 18, (class_2561)class_2561.method_43473());
            this.searchBox.method_1852(128);
            this.searchBox.method_47404((class_2561)class_2561.method_43471((String)"ankinbt.search.hint"));
            this.searchBox.method_1863(value -> this.filter());
            this.searchBox.method_25365(true);
            this.filter();
            String current = SimpleEditorScreen.this.getPotionId();
            for (int i = 0; i < this.filtered.size(); ++i) {
                if (!this.filtered.get(i).equals(current)) continue;
                this.selectedIdx = i;
                break;
            }
        }

        private void filter() {
            String q = this.searchBox.method_1882().toLowerCase(Locale.ROOT);
            this.filtered.clear();
            for (String id : POTION_IDS) {
                if (!q.isEmpty() && !id.toLowerCase(Locale.ROOT).contains(q) && !SimpleEditorScreen.this.potionDisplayName(id).toLowerCase(Locale.ROOT).contains(q)) continue;
                this.filtered.add(id);
            }
            this.selectedIdx = Math.min(this.selectedIdx, Math.max(0, this.filtered.size() - 1));
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.filtered.size() - 8)));
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 380);
            int dh = Math.min(h - 20, 250);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.pick_potion"), dx + 10, dy + 8, -1906448, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int sx = dx + 10;
            int sy = dy + 30;
            int sw = dw - 20;
            int sh = 18;
            this.searchBox.method_46421(sx);
            this.searchBox.method_46419(sy);
            this.searchBox.method_25358(sw);
            this.searchBox.method_25365(true);
            this.searchBox.method_25394(g, mx, my, 0.0f);
            int listY = sy + sh + 6;
            int maxItems = Math.max(1, (dh - 92) / 18);
            this.hoverIdx = -1;
            int end = Math.min(this.filtered.size(), this.scrollOff + maxItems);
            for (int i = this.scrollOff; i < end; ++i) {
                boolean selected;
                boolean hover;
                int ry = listY + (i - this.scrollOff) * 18;
                boolean bl = hover = mx >= dx + 10 && mx < dx + dw - 10 && my >= ry && my < ry + 18;
                if (hover) {
                    this.hoverIdx = i;
                }
                boolean bl2 = selected = i == this.selectedIdx;
                g.method_25294(dx + 10, ry, dx + dw - 10, ry + 17, selected ? 677603057 : (hover ? 0x30FFFFFF : 0));
                Object label = SimpleEditorScreen.this.potionDisplayName(this.filtered.get(i));
                String id = this.filtered.get(i);
                if (font.method_1727((String)label) > dw - 160) {
                    label = font.method_27523((String)label, dw - 164) + "..";
                }
                VersionCompat.get().drawString(g, font, (String)label, dx + 14, ry + 5, selected ? -1906448 : -7035976, false);
                Object shortId = id.replace("minecraft:", "");
                if (font.method_1727((String)shortId) > 110) {
                    shortId = font.method_27523((String)shortId, 106) + "..";
                }
                VersionCompat.get().drawString(g, font, (String)shortId, dx + dw - 124, ry + 5, -10193781, false);
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.method_25294(cancelX, by, cancelX + bw, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.method_25294(okX, by, okX + bw, by + bh2, oh ? -10262799 : -11581723);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.edit.apply"), okX + (bw - font.method_1727(SimpleEditorScreen.tr("ankinbt.edit.apply"))) / 2, by + 6, -1906448, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 380);
            int dh = Math.min(h - 20, 250);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int sx = dx + 10;
            int sy = dy + 30;
            int sw = dw - 20;
            this.searchBox.method_46421(sx);
            this.searchBox.method_46419(sy);
            this.searchBox.method_25358(sw);
            if (this.searchBox.method_25402(mx, my, btn)) {
                this.searchBox.method_25365(true);
                return true;
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            if (mx >= (double)cancelX && mx < (double)(cancelX + bw) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.activeSubEditor = null;
                return true;
            }
            int okX = dx + dw / 2 + 6;
            if (mx >= (double)okX && mx < (double)(okX + bw) && my >= (double)by && my < (double)(by + bh2)) {
                this.confirm();
                return true;
            }
            if (this.hoverIdx >= 0 && this.hoverIdx < this.filtered.size()) {
                this.selectedIdx = this.hoverIdx;
                if (btn == 0) {
                    this.confirm();
                }
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) {
                this.confirm();
                return true;
            }
            if (key == 264 && this.selectedIdx < this.filtered.size() - 1) {
                ++this.selectedIdx;
                if (this.selectedIdx >= this.scrollOff + 8) {
                    ++this.scrollOff;
                }
                return true;
            }
            if (key == 265 && this.selectedIdx > 0) {
                --this.selectedIdx;
                if (this.selectedIdx < this.scrollOff) {
                    this.scrollOff = this.selectedIdx;
                }
                return true;
            }
            if (this.searchBox.method_25404(key, scan, mod)) {
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (this.searchBox.method_25400(c, mod)) {
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            this.scrollOff -= (int)sy * 3;
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.filtered.size() - 8)));
            return true;
        }

        private void confirm() {
            if (this.selectedIdx >= 0 && this.selectedIdx < this.filtered.size()) {
                SimpleEditorScreen.this.setPotionBase(this.filtered.get(this.selectedIdx));
                SimpleEditorScreen.this.activeSubEditor = null;
            }
        }
    }

    class LoreColorInsertEditor
    implements SubEditor {
        final InlineFieldEditor parent;

        LoreColorInsertEditor(InlineFieldEditor parent) {
            this.parent = parent;
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340);
            int dh = 260;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.color_palette"), dx + 10, dy + 8, -1906448, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int gridX = dx + 12;
            int gridY = dy + 28;
            int cellW = (dw - 24) / 8;
            int cellH = 28;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.palette_bright"), dx + 10, gridY - 1, -10193781, false);
            int[] brightOrder = new int[]{6, 14, 10, 11, 9, 13, 12, 15};
            for (int i = 0; i < 8; ++i) {
                int ci = brightOrder[i];
                int cx = gridX + i * cellW;
                int cy = gridY + 8;
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.method_25294(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
            }
            int darkY = gridY + cellH + 14;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.palette_dark"), dx + 10, darkY - 1, -10193781, false);
            int[] darkOrder = new int[]{0, 8, 4, 5, 1, 3, 2, 7};
            for (int i = 0; i < 8; ++i) {
                int ci = darkOrder[i];
                int cx = gridX + i * cellW;
                int cy = darkY + 8;
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.method_25294(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (ci == 0 || ci == 8) {
                    SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, 0x40FFFFFF);
                }
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
            }
            int fmtY = darkY + cellH + 20;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.format_codes"), dx + 10, fmtY, -7035976, false);
            fmtY += 14;
            int fmtX = gridX;
            String lang = class_310.method_1551().field_1690.field_1883;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; ++i) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.method_1727(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) {
                    fmtX = gridX;
                    fmtY += 22;
                }
                boolean hover = mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18;
                g.method_25294(fmtX, fmtY, fmtX + pillW, fmtY + 18, hover ? -10262799 : 0x30FFFFFF);
                SimpleEditorScreen.this.drawBorder(g, fmtX, fmtY, pillW, 18, hover ? -10262799 : 0x20FFFFFF);
                VersionCompat.get().drawString(g, font, fLabel, fmtX + 7, fmtY + 5, hover ? -1906448 : -7035976, false);
                fmtX += pillW + 6;
            }
            int backY = dy + dh - 26;
            int backW = 70;
            int backX = dx + (dw - backW) / 2;
            boolean bh2 = mx >= backX && mx < backX + backW && my >= backY && my < backY + 20;
            g.method_25294(backX, backY, backX + backW, backY + 20, bh2 ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.back"), backX + (backW - font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.back"))) / 2, backY + 6, -7035976, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340);
            int dh = 260;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int gridX = dx + 12;
            int gridY = dy + 28;
            int cellW = (dw - 24) / 8;
            int cellH = 28;
            int[] brightOrder = new int[]{6, 14, 10, 11, 9, 13, 12, 15};
            for (int i = 0; i < 8; ++i) {
                int ci = brightOrder[i];
                int cx = gridX + i * cellW;
                int cy = gridY + 8;
                if (!(mx >= (double)cx) || !(mx < (double)(cx + cellW - 2)) || !(my >= (double)cy) || !(my < (double)(cy + cellH))) continue;
                this.parent.insertAtCursor("&" + MC_COLOR_CODES[ci]);
                SimpleEditorScreen.this.activeSubEditor = this.parent;
                return true;
            }
            int darkY = gridY + cellH + 14;
            int[] darkOrder = new int[]{0, 8, 4, 5, 1, 3, 2, 7};
            for (int i = 0; i < 8; ++i) {
                int ci = darkOrder[i];
                int cx = gridX + i * cellW;
                int cy = darkY + 8;
                if (!(mx >= (double)cx) || !(mx < (double)(cx + cellW - 2)) || !(my >= (double)cy) || !(my < (double)(cy + cellH))) continue;
                this.parent.insertAtCursor("&" + MC_COLOR_CODES[ci]);
                SimpleEditorScreen.this.activeSubEditor = this.parent;
                return true;
            }
            int fmtY = darkY + cellH + 20 + 14;
            int fmtX = gridX;
            String lang = class_310.method_1551().field_1690.field_1883;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; ++i) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = SimpleEditorScreen.this.field_22793.method_1727(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) {
                    fmtX = gridX;
                    fmtY += 22;
                }
                if (mx >= (double)fmtX && mx < (double)(fmtX + pillW) && my >= (double)fmtY && my < (double)(fmtY + 18)) {
                    this.parent.insertAtCursor("&" + MC_FORMAT_CODES[i]);
                    SimpleEditorScreen.this.activeSubEditor = this.parent;
                    return true;
                }
                fmtX += pillW + 6;
            }
            int backY = dy + dh - 26;
            int backW = 70;
            int backX = dx + (dw - backW) / 2;
            if (mx >= (double)backX && mx < (double)(backX + backW) && my >= (double)backY && my < (double)(backY + 20)) {
                SimpleEditorScreen.this.activeSubEditor = this.parent;
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            return false;
        }
    }

    class LoreColorInsertEditorForText
    implements SubEditor {
        final LoreTextEditorSubEditor textEditor;
        final InlineFieldEditor tempParent;
        private int hoveredColor = -1;

        LoreColorInsertEditorForText(LoreTextEditorSubEditor textEditor, InlineFieldEditor tempParent) {
            this.textEditor = textEditor;
            this.tempParent = tempParent;
        }

        @Override
        public void render(class_332 g, class_327 font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340);
            int dh = 260;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.color_palette"), dx + 10, dy + 8, -1906448, false);
            g.method_25294(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int gridX = dx + 12;
            int gridY = dy + 28;
            int cellW = (dw - 24) / 8;
            int cellH = 28;
            this.hoveredColor = -1;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.palette_bright"), dx + 10, gridY - 1, -10193781, false);
            int[] brightOrder = new int[]{6, 14, 10, 11, 9, 13, 12, 15};
            for (int i = 0; i < 8; ++i) {
                boolean hover;
                int ci = brightOrder[i];
                int cx = gridX + i * cellW;
                int cy = gridY + 8;
                boolean bl = hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                if (hover) {
                    this.hoveredColor = ci;
                }
                g.method_25294(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
                String lang = class_310.method_1551().field_1690.field_1883;
                String tip = "&" + MC_COLOR_CODES[ci] + " " + (lang != null && lang.startsWith("zh") ? MC_COLOR_NAMES_ZH[ci] : MC_COLOR_CODES[ci]);
                int tipW = font.method_1727(tip) + 8;
                g.method_25294(mx + 8, my - 14, mx + 8 + tipW, my - 1, -267382752);
                VersionCompat.get().drawString(g, font, tip, mx + 12, my - 12, -1906448, false);
            }
            int darkY = gridY + cellH + 14;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.palette_dark"), dx + 10, darkY - 1, -10193781, false);
            int[] darkOrder = new int[]{0, 8, 4, 5, 1, 3, 2, 7};
            for (int i = 0; i < 8; ++i) {
                boolean hover;
                int ci = darkOrder[i];
                int cx = gridX + i * cellW;
                int cy = darkY + 8;
                boolean bl = hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                if (hover) {
                    this.hoveredColor = ci;
                }
                int bgColor = MC_COLORS[ci] | 0xFF000000;
                g.method_25294(cx, cy, cx + cellW - 2, cy + cellH, bgColor);
                if (ci == 0 || ci == 8) {
                    SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, 0x40FFFFFF);
                }
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
                String lang = class_310.method_1551().field_1690.field_1883;
                String tip = "&" + MC_COLOR_CODES[ci] + " " + (lang != null && lang.startsWith("zh") ? MC_COLOR_NAMES_ZH[ci] : MC_COLOR_CODES[ci]);
                int tipW = font.method_1727(tip) + 8;
                g.method_25294(mx + 8, my - 14, mx + 8 + tipW, my - 1, -267382752);
                VersionCompat.get().drawString(g, font, tip, mx + 12, my - 12, -1906448, false);
            }
            int fmtY = darkY + cellH + 20;
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.format_codes"), dx + 10, fmtY, -7035976, false);
            fmtY += 14;
            int fmtX = gridX;
            String lang = class_310.method_1551().field_1690.field_1883;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; ++i) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.method_1727(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) {
                    fmtX = gridX;
                    fmtY += 22;
                }
                boolean hover = mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18;
                g.method_25294(fmtX, fmtY, fmtX + pillW, fmtY + 18, hover ? -10262799 : 0x30FFFFFF);
                SimpleEditorScreen.this.drawBorder(g, fmtX, fmtY, pillW, 18, hover ? -10262799 : 0x20FFFFFF);
                VersionCompat.get().drawString(g, font, fLabel, fmtX + 7, fmtY + 5, hover ? -1906448 : -7035976, false);
                fmtX += pillW + 6;
            }
            int backY = dy + dh - 26;
            int backW = 70;
            int backX = dx + (dw - backW) / 2;
            boolean bh2 = mx >= backX && mx < backX + backW && my >= backY && my < backY + 20;
            g.method_25294(backX, backY, backX + backW, backY + 20, bh2 ? 0x50FFFFFF : 0x30FFFFFF);
            VersionCompat.get().drawString(g, font, SimpleEditorScreen.tr("ankinbt.simple.back"), backX + (backW - font.method_1727(SimpleEditorScreen.tr("ankinbt.simple.back"))) / 2, backY + 6, -7035976, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340);
            int dh = 260;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            int gridX = dx + 12;
            int gridY = dy + 28;
            int cellW = (dw - 24) / 8;
            int cellH = 28;
            int[] brightOrder = new int[]{6, 14, 10, 11, 9, 13, 12, 15};
            for (int i = 0; i < 8; ++i) {
                int ci = brightOrder[i];
                int cx = gridX + i * cellW;
                int cy = gridY + 8;
                if (!(mx >= (double)cx) || !(mx < (double)(cx + cellW - 2)) || !(my >= (double)cy) || !(my < (double)(cy + cellH))) continue;
                this.textEditor.insertAtCursor("&" + MC_COLOR_CODES[ci]);
                SimpleEditorScreen.this.activeSubEditor = this.textEditor;
                return true;
            }
            int darkY = gridY + cellH + 14;
            int[] darkOrder = new int[]{0, 8, 4, 5, 1, 3, 2, 7};
            for (int i = 0; i < 8; ++i) {
                int ci = darkOrder[i];
                int cx = gridX + i * cellW;
                int cy = darkY + 8;
                if (!(mx >= (double)cx) || !(mx < (double)(cx + cellW - 2)) || !(my >= (double)cy) || !(my < (double)(cy + cellH))) continue;
                this.textEditor.insertAtCursor("&" + MC_COLOR_CODES[ci]);
                SimpleEditorScreen.this.activeSubEditor = this.textEditor;
                return true;
            }
            int fmtY = darkY + cellH + 20 + 14;
            int fmtX = gridX;
            String lang = class_310.method_1551().field_1690.field_1883;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; ++i) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = SimpleEditorScreen.this.field_22793.method_1727(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) {
                    fmtX = gridX;
                    fmtY += 22;
                }
                if (mx >= (double)fmtX && mx < (double)(fmtX + pillW) && my >= (double)fmtY && my < (double)(fmtY + 18)) {
                    this.textEditor.insertAtCursor("&" + MC_FORMAT_CODES[i]);
                    SimpleEditorScreen.this.activeSubEditor = this.textEditor;
                    return true;
                }
                fmtX += pillW + 6;
            }
            int backY = dy + dh - 26;
            int backW = 70;
            int backX = dx + (dw - backW) / 2;
            if (mx >= (double)backX && mx < (double)(backX + backW) && my >= (double)backY && my < (double)(backY + 20)) {
                SimpleEditorScreen.this.activeSubEditor = this.textEditor;
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            return false;
        }
    }
}


