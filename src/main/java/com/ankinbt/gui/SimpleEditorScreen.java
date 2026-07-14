/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EquipmentSlotGroup
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.component.DyedItemColor
 *  net.minecraft.world.item.component.ItemAttributeModifiers
 *  net.minecraft.world.item.component.ItemAttributeModifiers$Entry
 *  net.minecraft.world.item.component.ItemLore
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.item.enchantment.ItemEnchantments$Mutable
 *  net.minecraft.world.level.ItemLike
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.gui.ItemPickerScreen;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.nbt.NbtFileIO;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.util.EnchantmentTooltipHelper;
import com.ankinbt.util.FlatEditBox;
import com.ankinbt.util.ItemEditorVisuals;
import com.ankinbt.util.ItemRegistryHelper;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

public class SimpleEditorScreen
extends Screen {
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
    private static final Map<String, String> ENCHANT_ZH = new LinkedHashMap<String, String>();
    private static final Map<String, String> ATTR_ZH;
    private static final Map<String, String> SLOT_ZH;
    private static final String[] OP_NAMES_ZH;
    private static final String[] OP_NAMES_EN;
    private ItemStack editStack;
    private final ItemStack originalStack;
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
    private boolean nativeDialogOpen = false;
    private long lastNativeDialogAt = 0L;
    private float openAnim = 0.0f;
    private SubEditor activeSubEditor = null;
    private final List<Btn> headerBtns = new ArrayList<Btn>();

    public SimpleEditorScreen(ItemStack stack) {
        this(stack, -1);
    }

    public SimpleEditorScreen(ItemStack stack, int inventorySlot) {
        super((Component)Component.translatable((String)"ankinbt.simple.title"));
        this.originalStack = stack;
        this.editStack = stack.copy();
        this.inventorySlot = inventorySlot;
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
        this.contentX = this.px + 140 + 2;
        this.contentY = this.py + 32 + 1;
        this.contentW = this.pw - 140 - 6 - 6;
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
        this.headerBtns.add(new Btn(bx -= bw, by, bw, bw, "X", (Component)Component.translatable((String)"ankinbt.btn.close"), this::tryClose));
        bx -= bw + gap;
        int saveW = 40;
        this.headerBtns.add(new Btn(bx -= saveW + gap, by, saveW, bw, Component.translatable((String)"ankinbt.btn.save").getString(), (Component)Component.translatable((String)"ankinbt.btn.save.tip"), this::saveToItem));
        int modeW = 50;
        this.headerBtns.add(new Btn(bx -= modeW + gap + 4, by, modeW, bw, Component.translatable((String)"ankinbt.btn.advanced").getString(), (Component)Component.translatable((String)"ankinbt.btn.switch_advanced"), this::switchToAdvanced));
    }

    public void render(GuiGraphics g, int mx, int my, float pt) {
        float cfgSpeed = AnkiConfig.getUiAnimationSpeed();
        float speed = Math.max(0.06f, Math.min(0.16f, cfgSpeed));
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        int scrimAlpha = Math.max(70, Math.min(130, Math.round(112.0f * this.openAnim)));
        int panel = this.fadeColor(-670562288, this.openAnim);
        int header = this.fadeColor(-670035936, this.openAnim);
        int border = this.fadeColor(-14540234, this.openAnim);
        g.fill(0, 0, this.width, this.height, scrimAlpha << 24);
        g.fill(this.px, this.py, this.px + this.pw, this.py + this.ph, panel);
        this.drawBorder(g, this.px, this.py, this.pw, this.ph, border);
        g.fill(this.px + 1, this.py + 1, this.px + this.pw - 1, this.py + 32, header);
        g.fill(this.px + 1, this.py + 32, this.px + this.pw - 1, this.py + 32 + 1, border);
        g.drawString(this.font, "AnkiNBT", this.px + 16, this.py + 11, 0xFFE2E8F0, false);
        g.drawString(this.font, "简单模式", this.px + 64, this.py + 11, 0xFF38BDF8, false);
        if (this.dirty) {
            g.drawString(this.font, "*", this.px + 116, this.py + 12, -1096636, false);
        }
        for (Btn b : this.headerBtns) {
            b.render(g, this.font, mx, my);
        }
        this.renderSidebar(g, mx, my);
        g.fill(this.px + 140 + 1, this.py + 32 + 1, this.px + 140 + 2, this.py + this.ph - 20, border);
        if (this.activeSubEditor != null) {
            this.activeSubEditor.render(g, this.font, mx, my, this.contentX, this.contentY, this.contentW, this.contentH);
        } else {
            this.renderCategoryContent(g, mx, my);
        }
        g.fill(this.px + 1, this.py + this.ph - 20, this.px + this.pw - 1, this.py + this.ph - 20 + 1, border);
        this.renderFooter(g);
    }

    private int fadeColor(int color, float factor) {
        int a = color >>> 24 & 0xFF;
        int alpha = Math.max(0, Math.min(255, Math.round((float)a * factor)));
        return alpha << 24 | color & 0xFFFFFF;
    }

    private void renderSidebar(GuiGraphics g, int mx, int my) {
        g.fill(this.sideX, this.sideY, this.sideX + this.sideW, this.sideY + this.sideH, -670299112);
        int lx = this.sideX + 8;
        int headerY = this.sideY + 8;
        g.renderItem(this.editStack, lx + (this.sideW - 32) / 2, headerY);
        headerY += 24;
        Object name = this.editStack.getHoverName().getString();
        if (this.font.width((String)name) > this.sideW - 16) {
            name = this.font.plainSubstrByWidth((String)name, this.sideW - 22) + "...";
        }
        g.drawString(this.font, (String)name, lx, headerY, -1906448, false);
        g.fill(lx, headerY += 14, this.sideX + this.sideW - 8, headerY + 1, -14540234);
        int catAreaY = headerY += 8;
        int catAreaH = this.sideY + this.sideH - catAreaY;
        Category[] cats = Category.values();
        String[] catNames = new String[]{Component.translatable((String)"ankinbt.cat.general").getString(), Component.translatable((String)"ankinbt.cat.enchant").getString(), Component.translatable((String)"ankinbt.cat.lore").getString(), Component.translatable((String)"ankinbt.cat.attribute").getString(), Component.translatable((String)"ankinbt.cat.visual").getString(), Component.translatable((String)"ankinbt.cat.misc").getString()};
        int totalCatH = cats.length * 30;
        int maxSideScroll = Math.max(0, totalCatH - catAreaH);
        this.sideScrollOff = Math.max(0, Math.min(this.sideScrollOff, maxSideScroll));
        g.enableScissor(this.sideX, catAreaY, this.sideX + this.sideW, this.sideY + this.sideH);
        for (int i = 0; i < cats.length; ++i) {
            boolean active;
            int cy = catAreaY + i * 30 - this.sideScrollOff;
            if (cy + 28 < catAreaY || cy > this.sideY + this.sideH) continue;
            int cw = this.sideW - 16;
            boolean hover = mx >= lx && mx < lx + cw && my >= cy && my < cy + 28 && my >= catAreaY && my < this.sideY + this.sideH;
            boolean bl = active = cats[i] == this.activeCat;
            g.fill(lx, cy, lx + cw, cy + 28, active ? -10262799 : (hover ? 0x50FFFFFF : 0x20FFFFFF));
            if (active) {
                g.fill(lx, cy, lx + 2, cy + 28, -1);
            }
            g.drawString(this.font, catNames[i], lx + 8, cy + 10, active ? -1906448 : -7035976, false);
        }
        g.disableScissor();
        if (totalCatH > catAreaH) {
            int sbx = this.sideX + this.sideW - 5;
            g.fill(sbx, catAreaY, sbx + 4, this.sideY + this.sideH, 0x30FFFFFF);
            float ratio = (float)catAreaH / (float)totalCatH;
            int thumbH = Math.max(12, (int)((float)catAreaH * ratio));
            float sr = (float)this.sideScrollOff / (float)Math.max(1, maxSideScroll);
            int thumbY = catAreaY + (int)((float)(catAreaH - thumbH) * sr);
            g.fill(sbx, thumbY, sbx + 4, thumbY + thumbH, 0x70FFFFFF);
        }
    }

    private void renderCategoryContent(GuiGraphics g, int mx, int my) {
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
                g.fill(this.contentX, ry, this.contentX + this.contentW, ry + 24, 0x30FFFFFF);
            }
            g.fill(this.contentX, ry + 24 - 1, this.contentX + this.contentW, ry + 24, 0x10FFFFFF);
            int labelX = this.contentX + 8;
            if (row.icon != null && !row.icon.isEmpty()) {
                g.renderItem(row.icon, this.contentX + 5, ry + 4);
                labelX += 22;
            }
            String label = row.label;
            int labelBudget = Math.max(40, this.contentW - (labelX - this.contentX) - 118);
            if (this.font.width(label) > labelBudget) {
                label = this.font.plainSubstrByWidth(label, Math.max(10, labelBudget - 10)) + "..";
            }
            g.drawString(this.font, label, labelX, ry + 8, row.labelColor, false);
            int rightX = this.contentX + this.contentW - 8;
            if (row.deleteAction != null) {
                int delW = 24;
                int delH = 16;
                int delY = ry + (24 - delH) / 2;
                rightX -= delW;
                boolean delHover = mx >= rightX && mx < rightX + delW && my >= delY && my < delY + delH;
                g.fill(rightX, delY, rightX + delW, delY + delH, delHover ? 0x50FFFFFF : 0x30FFFFFF);
                g.drawString(this.font, "X", rightX + (delW - this.font.width("X")) / 2, delY + 4, delHover ? -1096636 : -10193781, false);
                rightX -= 4;
            }
            if (row.moveUp != null || row.moveDown != null) {
                int btnW = 16;
                int btnH = 16;
                int btnY = ry + (24 - btnH) / 2;
                if (row.moveDown != null) {
                    boolean dHover = mx >= (rightX -= btnW + 2) && mx < rightX + btnW && my >= btnY && my < btnY + btnH;
                    g.fill(rightX, btnY, rightX + btnW, btnY + btnH, dHover ? 0x50FFFFFF : 0x30FFFFFF);
                    g.drawString(this.font, "v", rightX + (btnW - this.font.width("v")) / 2, btnY + 4, dHover ? -1906448 : -10193781, false);
                }
                if (row.moveUp != null) {
                    boolean uHover = mx >= (rightX -= btnW + 2) && mx < rightX + btnW && my >= btnY && my < btnY + btnH;
                    g.fill(rightX, btnY, rightX + btnW, btnY + btnH, uHover ? 0x50FFFFFF : 0x30FFFFFF);
                    g.drawString(this.font, "^", rightX + (btnW - this.font.width("^")) / 2, btnY + 4, uHover ? -1906448 : -10193781, false);
                }
                rightX -= 4;
            }
            if (row.currentValue == null) continue;
            Object val = row.currentValue;
            int maxValW = rightX - (this.contentX + this.contentW / 2);
            if (this.font.width((String)val) > maxValW) {
                val = this.font.plainSubstrByWidth((String)val, maxValW - 10) + "..";
            }
            g.drawString(this.font, (String)val, rightX - this.font.width((String)val), ry + 8, -7035976, false);
        }
        if (rows.size() > this.maxRows) {
            int sbx = this.px + this.pw - 6 - 3;
            g.fill(sbx, this.contentY, sbx + 6, this.contentY + this.contentH, 0x30FFFFFF);
            float ratio = (float)this.maxRows / (float)rows.size();
            int thumbH = Math.max(16, (int)((float)this.contentH * ratio));
            float sr = (float)this.scrollOff / (float)Math.max(1, rows.size() - this.maxRows);
            int thumbY = this.contentY + (int)((float)(this.contentH - thumbH) * sr);
            g.fill(sbx, thumbY, sbx + 6, thumbY + thumbH, 0x70FFFFFF);
        }
    }

    private void renderFooter(GuiGraphics g) {
        int fy = this.py + this.ph - 20 + 5;
        if (this.statusMsg != null && System.currentTimeMillis() - this.statusTime < 3000L) {
            g.drawString(this.font, this.statusMsg, this.px + 140 + 8, fy, this.statusColor, false);
        } else {
            this.statusMsg = null;
            g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.simple.hint"), this.px + 140 + 8, fy, -10193781, false);
        }
    }

    private boolean handleMouseClicked(double mx, double my, int btn) {
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
            if (row.deleteAction != null) {
                int ry = this.contentY + (this.hoverRow - this.scrollOff) * 24;
                int delW = 24;
                int delH = 16;
                int delY = ry + (24 - delH) / 2;
                int delX = this.contentX + this.contentW - 8 - delW;
                if (mx >= (double)delX && mx < (double)(delX + delW) && my >= (double)delY && my < (double)(delY + delH)) {
                    row.deleteAction.run();
                    return true;
                }
            }
            if (row.moveUp != null || row.moveDown != null) {
                int ry = this.contentY + (this.hoverRow - this.scrollOff) * 24;
                int btnW = 16;
                int btnH = 16;
                int btnY = ry + (24 - btnH) / 2;
                int rightX = this.contentX + this.contentW - 8 - (row.deleteAction != null ? 28 : 0);
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
        return false;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        int btn;
        double my;
        double mx = event.x();
        if (this.mouseClicked(mx, my = event.y(), btn = event.button())) {
            return true;
        }
        if (this.minecraft != null) {
            double sw = this.minecraft.getWindow().getScreenWidth();
            double sh = this.minecraft.getWindow().getScreenHeight();
            if (sw > 0.0 && sh > 0.0) {
                double sx = mx * (double)this.width / sw;
                double sy = my * (double)this.height / sh;
                if ((Math.abs(sx - mx) > 0.5 || Math.abs(sy - my) > 0.5) && this.mouseClicked(sx, sy, btn)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        if (this.handleMouseClicked(mx, my, btn)) {
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {
        if (this.activeSubEditor != null && this.activeSubEditor.mouseDragged(mx, my, button, dragX, dragY, this.contentX, this.contentY, this.contentW, this.contentH)) {
            return true;
        }
        return super.mouseDragged(mx, my, button, dragX, dragY);
    }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
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

    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        int scan = event.scancode();
        int mod = event.modifiers();
        if (this.keyPressed(key, scan, mod)) {
            return true;
        }
        return super.keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public boolean keyPressed(int key, int scan, int mod) {
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
        return super.keyPressed(key, scan, mod);
    }

    private void tryClose() {
        if (this.dirty && AnkiConfig.isConfirmOnClose()) {
            this.activeSubEditor = new ConfirmCloseSubEditor();
        } else {
            this.onClose();
        }
    }

    public boolean charTyped(CharacterEvent event) {
        char c = (char)event.codepoint();
        int mod = event.modifiers();
        if (this.charTyped(c, mod)) {
            return true;
        }
        return super.charTyped((char)event.codepoint(), event.modifiers());
    }

    public boolean charTyped(char c, int mod) {
        if (this.activeSubEditor != null) {
            return this.activeSubEditor.charTyped(c, mod);
        }
        return super.charTyped(c, mod);
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
        Rarity rarity;
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        String nameVal = this.editStack.getHoverName().getString();
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.rename"), nameVal, () -> this.openInlineEditor("rename", nameVal)));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.count"), String.valueOf(this.editStack.getCount()), () -> this.openInlineEditor("count", String.valueOf(this.editStack.getCount()))));
        int maxDmg = this.editStack.getMaxDamage();
        if (maxDmg > 0) {
            int dmg = this.editStack.getDamageValue();
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.damage"), dmg + " / " + maxDmg, () -> this.openInlineEditor("damage", String.valueOf(this.editStack.getDamageValue()))));
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.max_damage"), String.valueOf(maxDmg), () -> this.openInlineEditor("max_damage", String.valueOf(maxDmg))));
        }
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.unbreakable"), this.isUnbreakable() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleUnbreakable));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.max_stack"), String.valueOf(this.editStack.getMaxStackSize()), () -> this.openInlineEditor("max_stack", String.valueOf(this.editStack.getMaxStackSize()))));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.repair_cost"), String.valueOf(this.getRepairCost()), () -> this.openInlineEditor("repair_cost", String.valueOf(this.getRepairCost()))));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.fire_resistant"), this.isFireResistant() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleFireResistant));
        if (VersionCompat.get().hasFood(this.editStack)) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.food_nutrition"), String.valueOf(VersionCompat.get().getFoodNutrition(this.editStack)), () -> this.openInlineEditor("food_nutrition", String.valueOf(VersionCompat.get().getFoodNutrition(this.editStack)))));
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.food_saturation"), String.valueOf(VersionCompat.get().getFoodSaturation(this.editStack)), () -> this.openInlineEditor("food_saturation", String.valueOf(VersionCompat.get().getFoodSaturation(this.editStack)))));
        }
        if ((rarity = (Rarity)this.editStack.get(DataComponents.RARITY)) != null) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.rarity"), this.getRarityDisplayName(rarity), () -> this.cycleRarity()));
        }
        return rows;
    }

    private String getRarityDisplayName(Rarity rarity) {
        boolean zh;
        if (rarity == null) {
            return "";
        }
        String lang = Minecraft.getInstance().options.languageCode;
        boolean bl = zh = lang != null && lang.startsWith("zh");
        if (zh) {
            return switch (rarity) {
                default -> throw new MatchException(null, null);
                case Rarity.COMMON -> "\u666e\u901a";
                case Rarity.UNCOMMON -> "\u7f55\u89c1";
                case Rarity.RARE -> "\u7a00\u6709";
                case Rarity.EPIC -> "\u53f2\u8bd7";
            };
        }
        return switch (rarity) {
            default -> throw new MatchException(null, null);
            case Rarity.COMMON -> "Common";
            case Rarity.UNCOMMON -> "Uncommon";
            case Rarity.RARE -> "Rare";
            case Rarity.EPIC -> "Epic";
        };
    }

    private List<ActionRow> getEnchantRows() {
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting((ItemStack)this.editStack);
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.hide_enchantments"), EnchantmentTooltipHelper.isHidden(this.editStack) ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleEnchantmentsHidden));
        enchants.entrySet().forEach(entry -> {
            Holder ench = (Holder)entry.getKey();
            int level = entry.getIntValue();
            Object eKey = ench.unwrapKey().orElse(null);
            String eId = eKey instanceof net.minecraft.resources.ResourceKey ? ((net.minecraft.resources.ResourceKey<?>)eKey).location().toString() : "?";
            String displayName = this.getEnchantDisplayName(eId);
            rows.add(new ActionRow(displayName, SimpleEditorScreen.tr("ankinbt.simple.level") + level, () -> this.openInlineEditor("ench_level:" + eId, String.valueOf(level)), -1906448, null, null, ItemEditorVisuals.enchantIconStack(eId), () -> this.removeEnchantment(eId)));
        });
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.add_enchant"), null, () -> {
            this.activeSubEditor = new EnchantPickerSubEditor();
        }, -10262799));
        if (!enchants.isEmpty()) {
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
        List<Component> lore = this.getLore();
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
        ItemAttributeModifiers.Entry entry;
        int idx;
        int i;
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        ItemAttributeModifiers attrComp = (ItemAttributeModifiers)this.editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List entries = attrComp.modifiers();
        for (i = 0; i < entries.size(); ++i) {
            final int attrIndex = i;
            entry = (ItemAttributeModifiers.Entry)entries.get(i);
            attrId = entry.attribute().unwrapKey().map(k -> k.location().toString()).orElse("?");
            displayName = this.getAttrDisplayName(attrId);
            double amount = entry.modifier().amount();
            String opName = this.getOpName(entry.modifier().operation());
            String slotName = this.getSlotDisplayName(entry.slot());
            String valueStr = String.format("%.2f %s [%s]", amount, opName, slotName);
            rows.add(new ActionRow(displayName, valueStr, () -> this.openInlineEditor("attr_amount:" + attrIndex, String.valueOf(amount)), -1906448, null, null, ItemEditorVisuals.attributeIconStack(attrId), () -> this.removeAttribute(attrIndex)));
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
        attrId = this.normalizeRegistryDisplayId(attrId);
        String zh;
        String translated = this.resolveAttributeDisplayName(attrId);
        if (translated != null) {
            return this.formatLocalizedId(translated, attrId);
        }
        String lang = Minecraft.getInstance().options.languageCode;
        if (lang != null && lang.startsWith("zh") && (zh = ATTR_ZH.get(attrId)) != null) {
            return this.formatLocalizedId(zh, attrId);
        }
        return this.formatLocalizedId(this.prettifyRegistryId(attrId), attrId);
    }

    private String getOpName(AttributeModifier.Operation op) {
        String lang = Minecraft.getInstance().options.languageCode;
        boolean zh = lang != null && lang.startsWith("zh");
        return switch (op) {
            default -> throw new MatchException(null, null);
            case AttributeModifier.Operation.ADD_VALUE -> {
                if (zh) {
                    yield OP_NAMES_ZH[0];
                }
                yield OP_NAMES_EN[0];
            }
            case AttributeModifier.Operation.ADD_MULTIPLIED_BASE -> {
                if (zh) {
                    yield OP_NAMES_ZH[1];
                }
                yield OP_NAMES_EN[1];
            }
            case AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL -> zh ? OP_NAMES_ZH[2] : OP_NAMES_EN[2];
        };
    }

    private String getSlotDisplayName(EquipmentSlotGroup slot) {
        String zh;
        String name = slot.getSerializedName();
        String lang = Minecraft.getInstance().options.languageCode;
        if (lang != null && lang.startsWith("zh") && (zh = SLOT_ZH.get(name)) != null) {
            return zh;
        }
        return name;
    }

    private void removeAttribute(int index) {
        ItemAttributeModifiers attrComp = (ItemAttributeModifiers)this.editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ArrayList<ItemAttributeModifiers.Entry> entries = new ArrayList<ItemAttributeModifiers.Entry>(attrComp.modifiers());
        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
            ItemAttributeModifiers next = VersionCompat.get().withEntries(entries, attrComp);
            if (next == attrComp && entries.size() != attrComp.modifiers().size()) {
                this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
                return;
            }
            this.editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, next);
            this.markDirty();
        } else {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
        }
    }

    private void clearAttributes() {
        this.editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        this.markDirty();
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.attrs_cleared"), -7035976);
    }

    private void addAttribute(String attrId, double amount, AttributeModifier.Operation op, EquipmentSlotGroup slot) {
        Optional<Holder.Reference<Attribute>> holder = VersionCompat.get().getAttributeHolder(attrId);
        if (holder.isEmpty()) {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
            return;
        }
        ItemAttributeModifiers attrComp = (ItemAttributeModifiers)this.editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ArrayList<ItemAttributeModifiers.Entry> entries = new ArrayList<ItemAttributeModifiers.Entry>(attrComp.modifiers());
        ResourceLocation modId = ResourceLocation.fromNamespaceAndPath((String)"ankinbt", (String)("custom_" + System.currentTimeMillis()));
        entries.add(new ItemAttributeModifiers.Entry((Holder)holder.get(), new AttributeModifier(modId, amount, op), slot));
        ItemAttributeModifiers next = VersionCompat.get().withEntries(entries, attrComp);
        if (next == attrComp && entries.size() != attrComp.modifiers().size()) {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
            return;
        }
        this.editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, next);
        this.dirty = true;
        this.activeSubEditor = null;
        this.setStatus(Component.translatable((String)"ankinbt.status.added", (Object[])new Object[]{this.getAttrDisplayName(attrId)}).getString(), -14498466);
    }

    private List<ActionRow> getVisualRows() {
        DyedItemColor dyeColor;
        ArrayList<ActionRow> rows = new ArrayList<ActionRow>();
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.custom_model_data"), String.valueOf(this.getCustomModelData()), () -> this.openInlineEditor("custom_model_data", String.valueOf(this.getCustomModelData()))));
        rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.enchant_glint"), this.hasEnchantGlint() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleEnchantGlint));
        if (VersionCompat.get().hasHideTooltipFeature()) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.hide_tooltip"), this.isHideTooltip() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleHideTooltip));
        }
        if (VersionCompat.get().hasHideAdditionalFeature()) {
            rows.add(new ActionRow(SimpleEditorScreen.tr("ankinbt.simple.hide_additional"), this.isHideAdditional() ? SimpleEditorScreen.tr("ankinbt.simple.on") : SimpleEditorScreen.tr("ankinbt.simple.off"), this::toggleHideAdditional));
        }
        if ((dyeColor = (DyedItemColor)this.editStack.get(DataComponents.DYED_COLOR)) != null || this.isLeatherArmor()) {
            int color = dyeColor != null ? dyeColor.rgb() : 10511680;
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

    private boolean isUnbreakable() {
        return this.editStack.has(DataComponents.UNBREAKABLE);
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
        Integer c = (Integer)this.editStack.get(DataComponents.REPAIR_COST);
        return c != null ? c : 0;
    }

    private int getCustomModelData() {
        return VersionCompat.get().getCustomModelData(this.editStack);
    }

    private boolean hasEnchantGlint() {
        Boolean g = (Boolean)this.editStack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        return g != null && g != false;
    }

    private void toggleEnchantGlint() {
        Boolean cur = (Boolean)this.editStack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (cur != null && cur.booleanValue()) {
            this.editStack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        } else {
            this.editStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
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
        String id = this.editStack.getItem().builtInRegistryHolder().key().location().toString();
        return id.contains("leather_");
    }

    private void cycleRarity() {
        Rarity cur = (Rarity)this.editStack.get(DataComponents.RARITY);
        if (cur == null) {
            cur = Rarity.COMMON;
        }
        Rarity next = switch (cur) {
            default -> throw new MatchException(null, null);
            case Rarity.COMMON -> Rarity.UNCOMMON;
            case Rarity.UNCOMMON -> Rarity.RARE;
            case Rarity.RARE -> Rarity.EPIC;
            case Rarity.EPIC -> Rarity.COMMON;
        };
        this.editStack.set(DataComponents.RARITY, next);
        this.markDirty();
    }

    private List<Component> getLore() {
        ItemLore lc = (ItemLore)this.editStack.get(DataComponents.LORE);
        return lc == null ? List.of() : lc.lines();
    }

    private String getLoreRawText(int idx) {
        List<Component> lore = this.getLore();
        if (idx < 0 || idx >= lore.size()) {
            return "";
        }
        return this.componentToColorCoded(lore.get(idx));
    }

    private String componentToColorCoded(Component comp) {
        StringBuilder sb = new StringBuilder();
        comp.visit((style, text) -> {
            TextColor color = style.getColor();
            if (color != null) {
                int rgb = color.getValue();
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
            if (style.isBold()) {
                sb.append("&l");
            }
            if (style.isItalic()) {
                sb.append("&o");
            }
            if (style.isUnderlined()) {
                sb.append("&n");
            }
            if (style.isStrikethrough()) {
                sb.append("&m");
            }
            if (style.isObfuscated()) {
                sb.append("&k");
            }
            sb.append(text);
            return Optional.empty();
        }, Style.EMPTY);
        return sb.toString();
    }

    static Component colorCodedToComponent(String input) {
        String processed = input;
        MutableComponent result = Component.empty();
        int i = 0;
        Style currentStyle = Style.EMPTY.withItalic(Boolean.valueOf(false));
        while (i < processed.length()) {
            Style newStyle;
            char code;
            if (processed.charAt(i) == '&' && i + 1 < processed.length()) {
                code = processed.charAt(i + 1);
                if (code == '#' && i + 8 <= processed.length()) {
                    try {
                        String hex = processed.substring(i + 2, i + 8);
                        int rgb = Integer.parseInt(hex, 16);
                        currentStyle = Style.EMPTY.withColor(TextColor.fromRgb((int)rgb));
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
            result.append((Component)Component.literal((String)processed.substring(start, i)).withStyle(currentStyle));
        }
        return result;
    }

    private static Style applyColorCode(Style style, char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0));
            case '1' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)170));
            case '2' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)43520));
            case '3' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)43690));
            case '4' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0xAA0000));
            case '5' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0xAA00AA));
            case '6' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0xFFAA00));
            case '7' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0xAAAAAA));
            case '8' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0x555555));
            case '9' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0x5555FF));
            case 'a' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0x55FF55));
            case 'b' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0x55FFFF));
            case 'c' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0xFF5555));
            case 'd' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0xFF55FF));
            case 'e' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0xFFFF55));
            case 'f' -> Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)0xFFFFFF));
            case 'k' -> style.withObfuscated(Boolean.valueOf(true));
            case 'l' -> style.withBold(Boolean.valueOf(true));
            case 'm' -> style.withStrikethrough(Boolean.valueOf(true));
            case 'n' -> style.withUnderlined(Boolean.valueOf(true));
            case 'o' -> style.withItalic(Boolean.valueOf(true));
            case 'r' -> Style.EMPTY.withItalic(Boolean.valueOf(false));
            default -> null;
        };
    }

    private void setLore(List<Component> lines) {
        this.editStack.set(DataComponents.LORE, new ItemLore(lines));
        this.dirty = true;
    }

    private void moveLore(int from, int to) {
        ArrayList<Component> lore = new ArrayList<Component>(this.getLore());
        if (from < 0 || from >= lore.size() || to < 0 || to >= lore.size()) {
            return;
        }
        Component moved = (Component)lore.remove(from);
        lore.add(to, moved);
        this.setLore(lore);
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.lore_moved"), -7035976);
    }

    private void removeLastLore() {
        ArrayList<Component> lore = new ArrayList<Component>(this.getLore());
        if (!lore.isEmpty()) {
            lore.remove(lore.size() - 1);
            this.setLore(lore);
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.deleted"), -7035976);
        }
    }

    private void clearLore() {
        this.editStack.remove(DataComponents.LORE);
        this.dirty = true;
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.lore_cleared"), -7035976);
    }

    private void toggleEnchantmentsHidden() {
        boolean hidden = !EnchantmentTooltipHelper.isHidden(this.editStack);
        if (EnchantmentTooltipHelper.setHidden(this.editStack, hidden)) {
            this.markDirty();
        } else {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
        }
    }

    private void clearEnchantments() {
        boolean hidden = EnchantmentTooltipHelper.isHidden(this.editStack);
        this.editStack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (hidden) {
            EnchantmentTooltipHelper.setHidden(this.editStack, true);
        }
        this.dirty = true;
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.enchants_cleared"), -7035976);
    }

    private void removeEnchantment(String enchId) {
        if (this.applyEnchantLevel(enchId, 0)) {
            this.dirty = true;
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.deleted"), -7035976);
        } else {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
        }
    }

    private void copyNbtToClipboard() {
        Optional<CompoundTag> opt = NbtHelper.serializeItemStack(this.editStack);
        if (opt.isPresent()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(opt.get().toString());
            this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.nbt_copied"), -14498466);
        }
    }

    private void copyGiveCommand() {
        Optional<CompoundTag> opt = NbtHelper.serializeItemStack(this.editStack);
        if (opt.isPresent()) {
            String id = this.editStack.getItem().builtInRegistryHolder().key().location().toString();
            String cmd = "/give @s " + id + " " + this.editStack.getCount();
            Minecraft.getInstance().keyboardHandler.setClipboard(cmd);
            this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.cmd_copied"), -14498466);
        }
    }

    private void resetItem() {
        this.editStack = this.originalStack.copy();
        this.dirty = false;
        this.setStatus(SimpleEditorScreen.tr("ankinbt.simple.reset_done"), -7035976);
    }

    private String getEnchantDisplayName(String enchId) {
        enchId = this.normalizeRegistryDisplayId(enchId);
        String zh;
        String translated = this.resolveEnchantDisplayName(enchId);
        if (translated != null) {
            return this.formatLocalizedId(translated, enchId);
        }
        String lang = Minecraft.getInstance().options.languageCode;
        if (lang != null && lang.startsWith("zh") && (zh = ENCHANT_ZH.get(enchId)) != null) {
            return this.formatLocalizedId(zh, enchId);
        }
        return this.formatLocalizedId(this.prettifyRegistryId(enchId), enchId);
    }

    private String formatLocalizedId(String label, String id) {
        String cleanId = this.normalizeRegistryDisplayId(id);
        if (cleanId == null || cleanId.isBlank() || "?".equals(cleanId)) cleanId = "minecraft:unknown";
        String cleanLabel = label == null || label.isBlank() ? this.prettifyRegistryId(cleanId) : label;
        cleanLabel = cleanLabel.replace("ResourceKey[", "").replace("]", "");
        return cleanLabel.contains(cleanId) ? cleanLabel : cleanLabel + " (" + cleanId + ")";
    }

    private String normalizeRegistryDisplayId(String raw) {
        if (raw == null) return "?";
        String text = raw.trim();
        if (text.isEmpty()) return "?";
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("[a-z0-9_.-]+:[a-z0-9_./-]+")
                .matcher(text.toLowerCase(java.util.Locale.ROOT));
        String last = null;
        while (matcher.find()) last = matcher.group();
        return last != null ? last : text;
    }

    private String resolveAttributeDisplayName(String attrId) {
        try {
            Optional<Holder.Reference<Attribute>> holder = VersionCompat.get().getAttributeHolder(attrId);
            if (holder.isEmpty()) {
                return null;
            }
            Object attribute = holder.get().value();
            for (String methodName : new String[]{"getDescriptionId", "descriptionId"}) {
                try {
                    String translated;
                    String key;
                    Object out = attribute.getClass().getMethod(methodName, new Class[0]).invoke(attribute, new Object[0]);
                    if (!(out instanceof String) || (key = (String)out).isBlank() || (translated = Component.translatable((String)key).getString()).isBlank() || translated.equals(key)) continue;
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
            Optional<Holder.Reference<Enchantment>> holder = VersionCompat.get().getEnchantHolder(enchId);
            if (holder.isEmpty()) {
                return null;
            }
            Component description = this.invokeComponent(holder.get().value(), "description", "getDescription");
            if (description != null && !(translated = description.getString()).isBlank()) {
                return translated;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private Component invokeComponent(Object target, String ... methodNames) {
        if (target == null || methodNames == null) {
            return null;
        }
        for (String methodName : methodNames) {
            try {
                Object out = target.getClass().getMethod(methodName, new Class[0]).invoke(target, new Object[0]);
                if (!(out instanceof Component)) continue;
                Component component = (Component)out;
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
                    Component comp = SimpleEditorScreen.colorCodedToComponent(value);
                    MutableComponent result = Component.empty().withStyle(Style.EMPTY.withItalic(Boolean.valueOf(false)));
                    result.append(comp);
                    this.editStack.set(DataComponents.CUSTOM_NAME, result);
                } else {
                    this.editStack.set(DataComponents.CUSTOM_NAME, Component.literal((String)value).withStyle(Style.EMPTY.withItalic(Boolean.valueOf(false))));
                }
            } else if (field.equals("count")) {
                this.editStack.setCount(Math.max(1, Math.min(99, Integer.parseInt(value))));
            } else if (field.equals("damage")) {
                this.editStack.setDamageValue(Math.max(0, Integer.parseInt(value)));
            } else if (field.equals("max_damage")) {
                this.editStack.set(DataComponents.MAX_DAMAGE, Math.max(1, Integer.parseInt(value)));
            } else if (field.equals("max_stack")) {
                this.editStack.set(DataComponents.MAX_STACK_SIZE, Math.max(1, Math.min(99, Integer.parseInt(value))));
            } else if (field.equals("repair_cost")) {
                this.editStack.set(DataComponents.REPAIR_COST, Math.max(0, Integer.parseInt(value)));
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
                ArrayList<Component> lore = new ArrayList<Component>(this.getLore());
                if (idx >= 0 && idx < lore.size()) {
                    lore.set(idx, (Component)(isLore ? SimpleEditorScreen.colorCodedToComponent(value) : Component.literal((String)value)));
                    this.setLore(lore);
                }
            } else if (field.equals("lore_add")) {
                ArrayList<Component> lore = new ArrayList<Component>(this.getLore());
                lore.add((Component)(isLore ? SimpleEditorScreen.colorCodedToComponent(value) : Component.literal((String)value)));
                this.setLore(lore);
            } else if (field.startsWith("ench_level:")) {
                String enchId = field.substring(11);
                if (!this.applyEnchantLevel(enchId, Integer.parseInt(value))) {
                    this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
                    this.activeSubEditor = null;
                    return;
                }
            } else if (field.startsWith("attr_amount:")) {
                int idx = Integer.parseInt(field.substring(12));
                ItemAttributeModifiers attrComp = (ItemAttributeModifiers)this.editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                ArrayList<ItemAttributeModifiers.Entry> entries = new ArrayList<ItemAttributeModifiers.Entry>(attrComp.modifiers());
                if (idx >= 0 && idx < entries.size()) {
                    ItemAttributeModifiers.Entry old = (ItemAttributeModifiers.Entry)entries.get(idx);
                    double newAmount = Double.parseDouble(value);
                    entries.set(idx, new ItemAttributeModifiers.Entry(old.attribute(), new AttributeModifier(old.modifier().id(), newAmount, old.modifier().operation()), old.slot()));
                    ItemAttributeModifiers next = VersionCompat.get().withEntries(entries, attrComp);
                    if (next == attrComp) {
                        this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
                        this.activeSubEditor = null;
                        return;
                    }
                    this.editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, next);
                } else {
                    this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
                    this.activeSubEditor = null;
                    return;
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

    private boolean applyEnchantLevel(String enchId, int level) {
        ResourceLocation loc = ResourceLocation.tryParse((String)enchId);
        if (loc == null) {
            return false;
        }
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting((ItemStack)this.editStack));
        if (level <= 0) {
            mutable.removeIf(h -> h.unwrapKey().map(k -> k.location().equals(loc)).orElse(false));
        } else {
            Optional<Holder.Reference<Enchantment>> holder = VersionCompat.get().getEnchantHolder(enchId);
            if (holder.isEmpty()) {
                return false;
            }
            mutable.set((Holder)holder.get(), level);
        }
        this.editStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return true;
    }

    private void addEnchantment(String enchId, int level) {
        if (this.applyEnchantLevel(enchId, level)) {
            this.dirty = true;
            this.activeSubEditor = null;
            this.setStatus(Component.translatable((String)"ankinbt.status.added", (Object[])new Object[]{this.getEnchantDisplayName(enchId)}).getString(), -14498466);
        } else {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.save_error"), -1096636);
        }
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
        if (!mc.player.hasInfiniteMaterials()) {
            this.setStatus(SimpleEditorScreen.tr("ankinbt.status.creative_only"), -1096636);
            return;
        }
        VersionCompat.get().sanitizeForCreativeSave(this.editStack);
        if (this.inventorySlot >= 0) {
            int creativeSlot = SimpleEditorScreen.creativePacketSlotFromEditedSlot(this.inventorySlot);
            if (creativeSlot < 0) {
                this.setStatus(Component.translatable((String)"ankinbt.status.save_error").getString(), -1096636);
                return;
            }
            int playerSlot = SimpleEditorScreen.playerInventoryIndexFromCreativeSlot(creativeSlot);
            if (playerSlot >= 0) {
                mc.player.getInventory().setItem(playerSlot, this.editStack.copy());
            }
            mc.gameMode.handleCreativeModeItemAdd(this.editStack.copy(), creativeSlot);
        } else {
            int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
            mc.player.getInventory().setItem(slot, this.editStack.copy());
            mc.gameMode.handleCreativeModeItemAdd(this.editStack.copy(), 36 + slot);
        }
        this.dirty = false;
        this.setStatus(SimpleEditorScreen.tr("ankinbt.status.saved"), -14498466);
    }

    private void switchToAdvanced() {
        Minecraft.getInstance().setScreen((Screen)new NbtEditorScreen(this.editStack, this.inventorySlot));
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
        return Component.translatable((String)key).getString();
    }

    private boolean hasTinyFd() {
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
            Method method = this.pickTinyFdMethod(clazz, methodName);
            if (method != null) {
                Object out = method.invoke(null, this.tinyFdArgs(method.getParameterTypes(), defaultPath, isOpen));
                if (out instanceof CharSequence) {
                    CharSequence cs = (CharSequence)out;
                    return cs.toString();
                }
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

    private Method pickTinyFdMethod(Class<?> clazz, String methodName) {
        Method fallback = null;
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName)) continue;
            Class<?>[] types = method.getParameterTypes();
            boolean hasStringArray = false;
            boolean hasPointerBuffer = false;
            for (Class<?> type : types) {
                if (type == String[].class) {
                    hasStringArray = true;
                }
                if (type.getName().equals("org.lwjgl.PointerBuffer")) {
                    hasPointerBuffer = true;
                }
            }
            if (hasStringArray) {
                return method;
            }
            if (fallback == null && !hasPointerBuffer) {
                fallback = method;
            }
            if (fallback == null) {
                fallback = method;
            }
        }
        return fallback;
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
            args[i] = pt == String[].class ? new String[]{"*.nbt"} : (pt == Boolean.TYPE || pt == Boolean.class ? Boolean.FALSE : (pt == Integer.TYPE || pt == Integer.class ? Integer.valueOf(1) : (pt.getName().equals("org.lwjgl.PointerBuffer") ? null : null)));
        }
        return args;
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
        ATTR_ZH.put("minecraft:generic.follow_range", "\u8ddf\u968f\u8303\u56f4");
        ATTR_ZH.put("minecraft:generic.knockback_resistance", "\u51fb\u9000\u6297\u6027");
        ATTR_ZH.put("minecraft:generic.movement_speed", "\u79fb\u52a8\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.flying_speed", "\u98de\u884c\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.attack_damage", "\u653b\u51fb\u4f24\u5bb3");
        ATTR_ZH.put("minecraft:generic.attack_knockback", "\u653b\u51fb\u51fb\u9000");
        ATTR_ZH.put("minecraft:generic.attack_speed", "\u653b\u51fb\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.armor", "\u62a4\u7532\u503c");
        ATTR_ZH.put("minecraft:generic.armor_toughness", "\u62a4\u7532\u97e7\u6027");
        ATTR_ZH.put("minecraft:generic.luck", "\u5e78\u8fd0");
        ATTR_ZH.put("minecraft:generic.max_absorption", "\u6700\u5927\u5438\u6536");
        ATTR_ZH.put("minecraft:generic.scale", "\u7f29\u653e");
        ATTR_ZH.put("minecraft:generic.step_height", "\u53f0\u9636\u9ad8\u5ea6");
        ATTR_ZH.put("minecraft:generic.gravity", "\u91cd\u529b");
        ATTR_ZH.put("minecraft:generic.safe_fall_distance", "\u5b89\u5168\u5760\u843d\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:generic.fall_damage_multiplier", "\u5760\u843d\u4f24\u5bb3\u500d\u7387");
        ATTR_ZH.put("minecraft:generic.jump_strength", "\u8df3\u8dc3\u529b\u91cf");
        ATTR_ZH.put("minecraft:generic.block_interaction_range", "\u65b9\u5757\u4ea4\u4e92\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:generic.entity_interaction_range", "\u5b9e\u4f53\u4ea4\u4e92\u8ddd\u79bb");
        ATTR_ZH.put("minecraft:generic.block_break_speed", "\u65b9\u5757\u7834\u574f\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.mining_efficiency", "\u6316\u6398\u6548\u7387");
        ATTR_ZH.put("minecraft:generic.sneaking_speed", "\u6f5c\u884c\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.submerged_mining_speed", "\u6c34\u4e0b\u6316\u6398\u901f\u5ea6");
        ATTR_ZH.put("minecraft:generic.sweeping_damage_ratio", "\u6a2a\u626b\u4f24\u5bb3\u6bd4");
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
        public void render(GuiGraphics var1, Font var2, int var3, int var4, int var5, int var6, int var7, int var8);

        public boolean mouseClicked(double var1, double var3, int var5, int var6, int var7, int var8, int var9);

        public boolean keyPressed(int var1, int var2, int var3);

        public boolean charTyped(char var1, int var2);

        default public boolean mouseScrolled(double sx, double sy) {
            return false;
        }

        default public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY, int x, int y, int w, int h) {
            return false;
        }
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
            boolean hv = this.isHover(mx, my);
            g.fill(this.x, this.y, this.x + this.w, this.y + this.h, hv ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(f, this.label, this.x + (this.w - f.width(this.label)) / 2, this.y + (this.h - 8) / 2, -1906448, false);
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
        final ItemStack icon;
        final Runnable deleteAction;

        ActionRow(String label, String currentValue, Runnable action) {
            this(label, currentValue, action, -1906448, null, null);
        }

        ActionRow(String label, String currentValue, Runnable action, int labelColor) {
            this(label, currentValue, action, labelColor, null, null);
        }

        ActionRow(String label, String currentValue, Runnable action, int labelColor, Runnable moveUp, Runnable moveDown) {
            this(label, currentValue, action, labelColor, moveUp, moveDown, ItemStack.EMPTY);
        }

        ActionRow(String label, String currentValue, Runnable action, int labelColor, Runnable moveUp, Runnable moveDown, ItemStack icon) {
            this(label, currentValue, action, labelColor, moveUp, moveDown, icon, null);
        }

        ActionRow(String label, String currentValue, Runnable action, int labelColor, Runnable moveUp, Runnable moveDown, ItemStack icon, Runnable deleteAction) {
            this.label = label;
            this.currentValue = currentValue;
            this.action = action;
            this.labelColor = labelColor;
            this.moveUp = moveUp;
            this.moveDown = moveDown;
            this.icon = icon == null ? ItemStack.EMPTY : icon;
            this.deleteAction = deleteAction;
        }
    }

    class ConfirmCloseSubEditor
    implements SubEditor {
        ConfirmCloseSubEditor() {
        }

        @Override
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = 260;
            int dh = 110;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -1096636);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.confirm.title"), dx + 10, dy + 10, -1906448, false);
            g.fill(dx + 1, dy + 24, dx + dw - 1, dy + 25, -14540234);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.confirm.unsaved"), dx + 10, dy + 32, -7035976, false);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.confirm.discard_hint"), dx + 10, dy + 46, -10193781, false);
            int by = dy + dh - 32;
            int bw2 = 70;
            int bh2 = 22;
            int saveX = dx + 10;
            boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
            g.fill(saveX, by, saveX + bw2, by + bh2, sh ? -15293622 : -14498466);
            String saveLabel = SimpleEditorScreen.tr("ankinbt.confirm.save_close");
            g.drawString(font, saveLabel, saveX + (bw2 - font.width(saveLabel)) / 2, by + 7, -1906448, false);
            int discardX = dx + dw / 2 - bw2 / 2;
            boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
            g.fill(discardX, by, discardX + bw2, by + bh2, dh2 ? -2131803068 : 1089422404);
            String discardLabel = SimpleEditorScreen.tr("ankinbt.confirm.discard");
            g.drawString(font, discardLabel, discardX + (bw2 - font.width(discardLabel)) / 2, by + 7, -1906448, false);
            int cancelX = dx + dw - bw2 - 10;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.width(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 7, -7035976, false);
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
                SimpleEditorScreen.this.onClose();
                return true;
            }
            int discardX = dx + dw / 2 - bw2 / 2;
            if (mx >= (double)discardX && mx < (double)(discardX + bw2) && my >= (double)by && my < (double)(by + bh2)) {
                SimpleEditorScreen.this.dirty = false;
                SimpleEditorScreen.this.onClose();
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
        String error = null;
        final boolean isLore;
        boolean initialCursorSynced = false;

        InlineFieldEditor(String field, String currentValue, boolean isLore) {
            this.field = field;
            this.isLore = isLore;
            this.inputBox = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 22, Component.empty());
            this.inputBox.setMaxLength(2048);
            this.inputBox.setValue(currentValue != null ? currentValue : "");
            this.inputBox.setResponder(value -> this.error = null);
            this.inputBox.setFocused(true);
        }

        @Override
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 340);
            int dh = this.isLore ? 140 : 100;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            String title = this.getFieldLabel(this.field);
            g.drawString(font, title, dx + 10, dy + 8, -1906448, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int ix = dx + 10;
            int iy = dy + 30;
            int iw = dw - 20;
            int ih = 22;
            this.inputBox.setX(ix);
            this.inputBox.setY(iy);
            this.inputBox.setWidth(iw);
            if (!this.initialCursorSynced) {
                this.inputBox.setCursorPosition(this.inputBox.getValue().length());
                this.initialCursorSynced = true;
            }
            this.inputBox.setFocused(true);
            this.inputBox.render(g, mx, my, 0.0f);
            String input = this.inputBox.getValue();
            if (this.isLore && !input.isEmpty()) {
                Component preview = SimpleEditorScreen.colorCodedToComponent(input);
                g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.preview") + ": ", ix, iy + ih + 4, -10193781, false);
                int previewX = ix + font.width(SimpleEditorScreen.tr("ankinbt.simple.preview") + ": ");
                g.drawString(font, preview, previewX, iy + ih + 4, -1906448, false);
            }
            if (this.error != null) {
                g.drawString(font, this.error, ix, iy + ih + (this.isLore ? 16 : 4), -1096636, false);
            }
            if (this.isLore) {
                int palX = dx + dw - 80;
                int palY = dy + 6;
                boolean palHover = mx >= palX && mx < palX + 70 && my >= palY && my < palY + 16;
                g.fill(palX, palY, palX + 70, palY + 16, palHover ? 0x50FFFFFF : 0x30FFFFFF);
                String palLabel = SimpleEditorScreen.tr("ankinbt.simple.color_palette");
                g.drawString(font, palLabel, palX + (70 - font.width(palLabel)) / 2, palY + 4, -7035976, false);
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
            g.fill(cancelX, by, cancelX + bw, by + bh, ch ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh;
            g.fill(okX, by, okX + bw, by + bh, oh ? -10262799 : -11581723);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.edit.apply"), okX + (bw - font.width(SimpleEditorScreen.tr("ankinbt.edit.apply"))) / 2, by + 6, -1906448, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 340);
            int dh = this.isLore ? 140 : 100;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            if (this.isLore) {
                int palX = dx + dw - 80;
                int palY = dy + 6;
                if (mx >= (double)palX && mx < (double)(palX + 70) && my >= (double)palY && my < (double)(palY + 16)) {
                    SimpleEditorScreen.this.activeSubEditor = new LoreColorInsertEditor(this);
                    return true;
                }
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
            int ix = dx + 10;
            int iy = dy + 30;
            int iw = dw - 20;
            this.inputBox.setX(ix);
            this.inputBox.setY(iy);
            this.inputBox.setWidth(iw);
            if (this.inputBox.mouseClicked(mx, my, btn)) {
                this.inputBox.setFocused(true);
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
            this.inputBox.keyPressed(key, scan, mod);
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            return this.inputBox.charTyped(c, mod);
        }

        void insertAtCursor(String text) {
            this.inputBox.insertText(text);
        }

        private void apply() {
            String input = this.inputBox.getValue();
            if (input.isEmpty() && !this.field.equals("rename") && !this.field.equals("lore_add") && !this.field.startsWith("lore:")) {
                this.error = SimpleEditorScreen.tr("ankinbt.simple.invalid_number");
                return;
            }
            SimpleEditorScreen.this.applyInlineEdit(this.field, input, this.isLore);
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

    class ContainerPreviewSubEditor
    implements SubEditor {
        private static final int COLS = 9;
        private static final int ROWS = 3;
        private static final int PAGE_SIZE = 27;
        private final List<CompoundTag> slotTags = new ArrayList<CompoundTag>();
        private final List<ItemStack> slotStacks = new ArrayList<ItemStack>();
        private int selectedSlot = 0;
        private int page = 0;
        private StorageMode mode = StorageMode.CONTAINER;
        private String message = "";
        private int msgColor = -7035976;

        ContainerPreviewSubEditor() {
            this.loadFromItem();
        }

        @Override
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 470);
            int dh = Math.min(h - 10, 300);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.container.title"), dx + 10, dy + 8, -1906448, false);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.container.mode") + ": " + this.modeName(), dx + 190, dy + 8, -7035976, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int gridX = dx + 14;
            int gridY = dy + 36;
            int cell = 20;
            int gap = 4;
            int start = this.page * 27;
            int hoveredGlobal = -1;
            ItemStack hoveredStack = ItemStack.EMPTY;
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
                g.fill(sx, sy, sx + cell, sy + cell, bg);
                SimpleEditorScreen.this.drawBorder(g, sx, sy, cell, cell, sel ? -10262799 : -14540234);
                ItemStack st = this.stackAt(global);
                if (st.isEmpty()) continue;
                g.renderItem(st, sx + 2, sy + 2);
                if (!hover) continue;
                hoveredStack = st;
            }
            String slotLabel = SimpleEditorScreen.tr("ankinbt.container.slot") + " " + this.selectedSlot;
            g.drawString(font, slotLabel, dx + 14, dy + 112, -7035976, false);
            String pageText = this.page + 1 + " / " + Math.max(1, (this.slotTags.size() + 27 - 1) / 27);
            g.drawString(font, pageText, dx + 14 + font.width(slotLabel) + 18, dy + 112, -10193781, false);
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
                g.drawString(font, this.message, dx + 10, by - 12, this.msgColor, false);
            }
            if (hoveredGlobal >= 0 && !hoveredStack.isEmpty()) {
                VersionCompat.get().renderTooltip(g, font, hoveredStack.getHoverName(), mx, my);
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

        private ItemStack stackAt(int global) {
            if (global < 0 || global >= this.slotStacks.size()) {
                return ItemStack.EMPTY;
            }
            ItemStack st = this.slotStacks.get(global);
            return st == null ? ItemStack.EMPTY : st;
        }

        private void fillFromMainHand() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            ItemStack hand = mc.player.getMainHandItem();
            if (hand == null || hand.isEmpty()) {
                this.message = SimpleEditorScreen.tr("ankinbt.container.empty_hand");
                this.msgColor = -1096636;
                return;
            }
            this.ensureSlots(this.selectedSlot + 1);
            Optional<CompoundTag> tag = NbtHelper.serializeItemStack(hand);
            if (tag.isEmpty()) {
                this.message = SimpleEditorScreen.tr("ankinbt.status.save_error");
                this.msgColor = -1096636;
                return;
            }
            this.slotTags.set(this.selectedSlot, this.copyTag(tag.get()));
            this.slotStacks.set(this.selectedSlot, hand.copy());
            this.message = SimpleEditorScreen.tr("ankinbt.status.edited");
            this.msgColor = -7035976;
        }

        private void openPicker() {
            int slot = this.selectedSlot;
            Minecraft.getInstance().setScreen((Screen)new ItemPickerScreen(SimpleEditorScreen.this, id -> {
                this.ensureSlots(slot + 1);
                Item item = ItemRegistryHelper.resolveItem(id);
                if (item == null) {
                    return;
                }
                ItemStack stack = new ItemStack((ItemLike)item, 1);
                Optional<CompoundTag> tag = NbtHelper.serializeItemStack(stack);
                if (tag.isPresent()) {
                    this.slotTags.set(slot, this.copyTag(tag.get()));
                    this.slotStacks.set(slot, stack.copy());
                    this.message = SimpleEditorScreen.tr("ankinbt.status.edited");
                    this.msgColor = -7035976;
                }
            }));
        }

        private void clearSelected() {
            this.ensureSlots(this.selectedSlot + 1);
            this.slotTags.set(this.selectedSlot, null);
            this.slotStacks.set(this.selectedSlot, ItemStack.EMPTY);
            this.message = SimpleEditorScreen.tr("ankinbt.status.deleted");
            this.msgColor = -7035976;
        }

        private void loadFromItem() {
            ListTag legacy;
            ListTag bundle;
            ListTag list;
            this.slotTags.clear();
            this.slotStacks.clear();
            this.selectedSlot = 0;
            this.page = 0;
            this.mode = StorageMode.CONTAINER;
            Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(SimpleEditorScreen.this.editStack);
            if (fullOpt.isEmpty()) {
                this.ensureSlots(27);
                return;
            }
            CompoundTag full = fullOpt.get();
            CompoundTag components = this.getCompound(full, "components");
            ListTag listTag = list = components == null ? null : this.getList(components, "minecraft:container");
            if (list != null && !list.isEmpty()) {
                this.mode = StorageMode.CONTAINER;
                this.readContainerList(list);
                return;
            }
            ListTag listTag2 = bundle = components == null ? null : this.getList(components, "minecraft:bundle_contents");
            if (bundle != null && !bundle.isEmpty()) {
                this.mode = StorageMode.BUNDLE;
                this.readBundleList(bundle);
                return;
            }
            CompoundTag tag = this.getCompound(full, "tag");
            CompoundTag block = tag == null ? null : this.getCompound(tag, "BlockEntityTag");
            ListTag listTag3 = legacy = block == null ? null : this.getList(block, "Items");
            if (legacy != null && !legacy.isEmpty()) {
                this.mode = StorageMode.LEGACY;
                this.readLegacyList(legacy);
                return;
            }
            this.ensureSlots(27);
        }

        private void readContainerList(ListTag list) {
            CompoundTag ct;
            Tag entry;
            int i;
            int max = 27;
            for (i = 0; i < list.size(); ++i) {
                entry = list.get(i);
                if (!(entry instanceof CompoundTag)) continue;
                ct = (CompoundTag)entry;
                max = Math.max(max, this.readInt(ct, "slot", 0) + 1);
            }
            this.ensureSlots(max);
            for (i = 0; i < list.size(); ++i) {
                int slot;
                entry = list.get(i);
                if (!(entry instanceof CompoundTag) || (slot = this.readInt(ct = (CompoundTag)entry, "slot", -1)) < 0) continue;
                CompoundTag item = this.getCompound(ct, "item");
                if (item == null) {
                    item = this.getCompound(ct, "stack");
                }
                if (item == null) continue;
                this.setSlot(slot, item);
            }
        }

        private void readBundleList(ListTag list) {
            this.ensureSlots(Math.max(27, list.size()));
            for (int i = 0; i < list.size(); ++i) {
                Tag entry = list.get(i);
                if (!(entry instanceof CompoundTag)) continue;
                CompoundTag ct = (CompoundTag)entry;
                this.setSlot(i, ct);
            }
        }

        private void readLegacyList(ListTag list) {
            CompoundTag ct;
            Tag entry;
            int i;
            int max = 27;
            for (i = 0; i < list.size(); ++i) {
                entry = list.get(i);
                if (!(entry instanceof CompoundTag)) continue;
                ct = (CompoundTag)entry;
                max = Math.max(max, this.readInt(ct, "Slot", 0) + 1);
            }
            this.ensureSlots(max);
            for (i = 0; i < list.size(); ++i) {
                int slot;
                entry = list.get(i);
                if (!(entry instanceof CompoundTag) || (slot = this.readInt(ct = (CompoundTag)entry, "Slot", -1)) < 0) continue;
                CompoundTag stack = new CompoundTag();
                stack.putString("id", this.readString(ct, "id", "minecraft:air"));
                stack.putInt("count", Math.max(1, this.readInt(ct, "Count", 1)));
                CompoundTag legacyTag = this.getCompound(ct, "tag");
                if (legacyTag != null && !legacyTag.isEmpty()) {
                    CompoundTag components = new CompoundTag();
                    components.put("minecraft:custom_data", (Tag)this.copyTag(legacyTag));
                    stack.put("components", (Tag)components);
                }
                this.setSlot(slot, stack);
            }
        }

        private void setSlot(int slot, CompoundTag stackTag) {
            this.ensureSlots(slot + 1);
            this.slotTags.set(slot, this.copyTag(stackTag));
            this.slotStacks.set(slot, this.decodeStack(stackTag));
        }

        private void ensureSlots(int size) {
            int target = Math.max(27, size);
            while (this.slotTags.size() < target) {
                this.slotTags.add(null);
                this.slotStacks.add(ItemStack.EMPTY);
            }
        }

        private ItemStack decodeStack(CompoundTag stackTag) {
            if (stackTag == null || stackTag.isEmpty()) {
                return ItemStack.EMPTY;
            }
            Optional<ItemStack> opt = NbtHelper.deserializeItemStack(stackTag);
            if (opt.isPresent()) {
                return opt.get();
            }
            String id = this.readString(stackTag, "id", "");
            if (id.isBlank()) {
                return ItemStack.EMPTY;
            }
            Item item = ItemRegistryHelper.resolveItem(id);
            if (item == null) {
                return ItemStack.EMPTY;
            }
            return new ItemStack((ItemLike)item, Math.max(1, this.readInt(stackTag, "count", 1)));
        }

        private void applyToItem() {
            Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(SimpleEditorScreen.this.editStack);
            if (fullOpt.isEmpty()) {
                this.message = SimpleEditorScreen.tr("ankinbt.status.save_error");
                this.msgColor = -1096636;
                return;
            }
            CompoundTag full = fullOpt.get();
            CompoundTag components = this.getOrCreateCompound(full, "components");
            if (this.mode == StorageMode.CONTAINER) {
                ListTag out = new ListTag();
                for (int i = 0; i < this.slotTags.size(); ++i) {
                    CompoundTag stack = this.slotTags.get(i);
                    if (stack == null || this.isAir(stack)) continue;
                    CompoundTag entry = new CompoundTag();
                    entry.putInt("slot", i);
                    entry.put("item", (Tag)this.copyTag(stack));
                    out.add(entry);
                }
                components.put("minecraft:container", (Tag)out);
                this.removeKey(components, "minecraft:bundle_contents");
            } else if (this.mode == StorageMode.BUNDLE) {
                ListTag out = new ListTag();
                for (CompoundTag stack : this.slotTags) {
                    if (stack == null || this.isAir(stack)) continue;
                    out.add(this.copyTag(stack));
                }
                components.put("minecraft:bundle_contents", (Tag)out);
                this.removeKey(components, "minecraft:container");
            } else {
                CompoundTag tag = this.getOrCreateCompound(full, "tag");
                CompoundTag block = this.getOrCreateCompound(tag, "BlockEntityTag");
                ListTag items = new ListTag();
                for (int i = 0; i < this.slotTags.size(); ++i) {
                    CompoundTag stack = this.slotTags.get(i);
                    if (stack == null || this.isAir(stack)) continue;
                    items.add(this.toLegacyStack(i, stack));
                }
                block.put("Items", (Tag)items);
                tag.put("BlockEntityTag", (Tag)block);
                full.put("tag", (Tag)tag);
            }
            full.put("components", (Tag)components);
            Optional<ItemStack> outStack = NbtHelper.deserializeItemStack(full);
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

        private CompoundTag toLegacyStack(int slot, CompoundTag stack) {
            CompoundTag custom;
            CompoundTag out = new CompoundTag();
            out.putByte("Slot", (byte)(slot & 0xFF));
            out.putString("id", this.readString(stack, "id", "minecraft:air"));
            out.putByte("Count", (byte)Math.max(1, Math.min(127, this.readInt(stack, "count", 1))));
            CompoundTag components = this.getCompound(stack, "components");
            if (components != null && (custom = this.getCompound(components, "minecraft:custom_data")) != null && !custom.isEmpty()) {
                out.put("tag", (Tag)this.copyTag(custom));
            }
            return out;
        }

        private boolean isAir(CompoundTag stack) {
            return "minecraft:air".equals(this.readString(stack, "id", "minecraft:air"));
        }

        private void renderSmallBtn(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h, String text) {
            boolean hover = this.hit(mx, my, x, y, w, h);
            g.fill(x, y, x + w, y + h, hover ? 0x50FFFFFF : 0x30FFFFFF);
            Object draw = text;
            if (font.width((String)draw) > w - 8) {
                draw = font.plainSubstrByWidth((String)draw, w - 12) + "..";
            }
            g.drawString(font, (String)draw, x + (w - font.width((String)draw)) / 2, y + 6, -7035976, false);
        }

        private boolean hit(double mx, double my, int x, int y, int w, int h) {
            return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
        }

        private CompoundTag copyTag(CompoundTag source) {
            if (source == null) {
                return null;
            }
            CompoundTag out = new CompoundTag();
            out.merge(source);
            return out;
        }

        private CompoundTag getCompound(CompoundTag parent, String key) {
            CompoundTag ct;
            if (parent == null || key == null || key.isBlank()) {
                return null;
            }
            Object raw = this.getTag(parent, key);
            return raw instanceof CompoundTag ? (ct = (CompoundTag)raw) : null;
        }

        private ListTag getList(CompoundTag parent, String key) {
            ListTag lt;
            if (parent == null || key == null || key.isBlank()) {
                return null;
            }
            Object raw = this.getTag(parent, key);
            return raw instanceof ListTag ? (lt = (ListTag)raw) : null;
        }

        private CompoundTag getOrCreateCompound(CompoundTag parent, String key) {
            CompoundTag out = this.getCompound(parent, key);
            if (out == null) {
                out = new CompoundTag();
                parent.put(key, (Tag)out);
            }
            return out;
        }

        private void removeKey(CompoundTag parent, String key) {
            if (parent == null || key == null || key.isBlank()) {
                return;
            }
            try {
                parent.getClass().getMethod("remove", String.class).invoke(parent, key);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }

        private Object getTag(CompoundTag parent, String key) {
            try {
                Object out = parent.getClass().getMethod("get", String.class).invoke(parent, key);
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

        private int readInt(CompoundTag parent, String key, int def) {
            if (parent == null) {
                return def;
            }
            try {
                Optional opt;
                Object var7_9;
                Object out = parent.getClass().getMethod("getInt", String.class).invoke(parent, key);
                if (out instanceof Number) {
                    Number n = (Number)out;
                    return n.intValue();
                }
                if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof Number) {
                    Number n = (Number)var7_9;
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

        private String readString(CompoundTag parent, String key, String def) {
            if (parent == null) {
                return def;
            }
            try {
                Optional opt;
                Object var7_9;
                Object out = parent.getClass().getMethod("getString", String.class).invoke(parent, key);
                if (out instanceof String) {
                    String s = (String)out;
                    return s;
                }
                if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof String) {
                    String s = (String)var7_9;
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
        private CompoundTag previewTag = null;
        private String previewInfo = null;
        private final Map<String, ItemStack> iconCache = new HashMap<String, ItemStack>();

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
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            String cat;
            int cw;
            int dw = Math.min(w - 10, 440);
            int dh = Math.min(h - 10, 320);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.import.title"), dx + 10, dy + 8, -1906448, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int tabY = dy + 26;
            int tabX = dx + 8;
            String allLabel = SimpleEditorScreen.tr("ankinbt.import.all");
            int allW = font.width(allLabel) + 10;
            boolean allHover = mx >= tabX && mx < tabX + allW && my >= tabY && my < tabY + 16;
            boolean allActive = this.currentCategory.isEmpty();
            g.fill(tabX, tabY, tabX + allW, tabY + 16, allActive ? -10262799 : (allHover ? 0x50FFFFFF : 0x30FFFFFF));
            g.drawString(font, allLabel, tabX + 5, tabY + 4, allActive ? -1906448 : -7035976, false);
            tabX += allW + 4;
            Iterator<String> iterator = this.categories.iterator();
            while (iterator.hasNext() && tabX + (cw = font.width(cat = iterator.next()) + 10) <= dx + dw - 8) {
                boolean hover = mx >= tabX && mx < tabX + cw && my >= tabY && my < tabY + 16;
                boolean active = cat.equals(this.currentCategory);
                g.fill(tabX, tabY, tabX + cw, tabY + 16, active ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
                g.drawString(font, cat, tabX + 5, tabY + 4, active ? -1906448 : -7035976, false);
                tabX += cw + 4;
            }
            int listX = dx + 8;
            int listY = tabY + 22;
            int listW = dw / 2 - 12;
            int listH = dh - 110;
            g.fill(listX, listY, listX + listW, listY + listH, -16119276);
            SimpleEditorScreen.this.drawBorder(g, listX, listY, listW, listH, -14540234);
            int rowH = 20;
            int maxItems = listH / rowH;
            this.hoverIdx = -1;
            if (this.files.isEmpty()) {
                g.drawString(font, SimpleEditorScreen.tr("ankinbt.import.no_files"), listX + 8, listY + 8, -10193781, false);
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
                        g.fill(listX + 1, ry, listX + listW - 1, ry + rowH, 677603057);
                    } else if (hovered) {
                        g.fill(listX + 1, ry, listX + listW - 1, ry + rowH, 0x30FFFFFF);
                    }
                    NbtFileIO.NbtFileEntry entry = this.files.get(i);
                    ItemStack icon = this.iconFor(entry);
                    if (!icon.isEmpty()) {
                        g.renderItem(icon, listX + 3, ry + 2);
                    }
                    if (font.width((String)(name = entry.displayName())) > listW - 34) {
                        name = font.plainSubstrByWidth((String)name, listW - 40) + "..";
                    }
                    g.drawString(font, (String)name, listX + 22, ry + 6, sel ? -1906448 : -7035976, false);
                }
            }
            int prevX = dx + dw / 2 + 4;
            int prevY = listY;
            int prevW = dw / 2 - 12;
            int prevH = listH;
            g.fill(prevX, prevY, prevX + prevW, prevY + prevH, -16119276);
            SimpleEditorScreen.this.drawBorder(g, prevX, prevY, prevW, prevH, -14540234);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.import.preview"), prevX + 6, prevY + 4, -7035976, false);
            if (this.selectedIdx >= 0 && this.selectedIdx < this.files.size()) {
                int infoStartY;
                NbtFileIO.NbtFileEntry entry = this.files.get(this.selectedIdx);
                g.drawString(font, entry.name(), prevX + 6, prevY + 18, -1906448, false);
                if (entry.alias() != null) {
                    g.drawString(font, SimpleEditorScreen.tr("ankinbt.export.alias") + " " + entry.alias(), prevX + 6, prevY + 30, -10262799, false);
                    g.drawString(font, entry.sizeDisplay(), prevX + 6, prevY + 42, -10193781, false);
                } else {
                    g.drawString(font, entry.sizeDisplay(), prevX + 6, prevY + 30, -10193781, false);
                }
                int n = infoStartY = entry.alias() != null ? prevY + 56 : prevY + 44;
                if (this.previewInfo != null) {
                    String[] infoLines = this.previewInfo.split("\n");
                    for (int i = 0; i < Math.min(infoLines.length, (prevH - 60) / 11); ++i) {
                        Object line = infoLines[i];
                        if (font.width((String)line) > prevW - 12) {
                            line = font.plainSubstrByWidth((String)line, prevW - 18) + "..";
                        }
                        g.drawString(font, (String)line, prevX + 6, infoStartY + i * 11, -7035976, false);
                    }
                }
            } else {
                g.drawString(font, SimpleEditorScreen.tr("ankinbt.import.select_file"), prevX + 6, prevY + 20, -10193781, false);
            }
            int by = dy + dh - 32;
            int bw2 = 70;
            int bh2 = 22;
            int refX = dx + 10;
            boolean rh = mx >= refX && mx < refX + 50 && my >= by && my < by + bh2;
            g.fill(refX, by, refX + 50, by + bh2, rh ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.import.refresh"), refX + (50 - font.width(SimpleEditorScreen.tr("ankinbt.import.refresh"))) / 2, by + 7, -7035976, false);
            int openW = 76;
            int openX = refX + 56;
            boolean fh = mx >= openX && mx < openX + openW && my >= by && my < by + bh2;
            g.fill(openX, by, openX + openW, by + bh2, fh ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.import.open_file"), openX + (openW - font.width(SimpleEditorScreen.tr("ankinbt.import.open_file"))) / 2, by + 7, -7035976, false);
            int cancelX = dx + dw / 2 - bw2 - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.width(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 7, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw2 && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw2, by + bh2, oh ? -10262799 : -11581723);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.import.do_import"), okX + (bw2 - font.width(SimpleEditorScreen.tr("ankinbt.import.do_import"))) / 2, by + 7, -1906448, false);
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
            int allW = SimpleEditorScreen.this.font.width(allLabel) + 10;
            if (mx >= (double)tabX && mx < (double)(tabX + allW) && my >= (double)tabY && my < (double)(tabY + 16)) {
                this.currentCategory = "";
                this.refreshFiles();
                return true;
            }
            tabX += allW + 4;
            for (String cat : this.categories) {
                int cw = SimpleEditorScreen.this.font.width(cat) + 10;
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
                Tag comp;
                StringBuilder sb = new StringBuilder();
                if (this.previewTag.contains("id")) {
                    sb.append("ID: ").append(VersionCompat.get().compoundGetString(this.previewTag, "id")).append("\n");
                }
                if (this.previewTag.contains("count")) {
                    sb.append("Count: ").append(VersionCompat.get().compoundGetInt(this.previewTag, "count")).append("\n");
                }
                if (this.previewTag.contains("components") && (comp = this.previewTag.get("components")) instanceof CompoundTag) {
                    CompoundTag ct = (CompoundTag)comp;
                    sb.append("Components: ").append(ct.size()).append("\n");
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
            Optional<ItemStack> opt = NbtHelper.deserializeItemStack(this.previewTag);
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
            CompoundTag tag = NbtFileIO.importNbt(Path.of(picked, new String[0]));
            if (tag == null) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.load_failed"), -1096636);
                return;
            }
            Optional<ItemStack> opt = NbtHelper.deserializeItemStack(tag);
            if (opt.isEmpty()) {
                SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.invalid_nbt"), -1096636);
                return;
            }
            SimpleEditorScreen.this.editStack = opt.get();
            SimpleEditorScreen.this.markDirty();
            SimpleEditorScreen.this.setStatus(SimpleEditorScreen.tr("ankinbt.import.success"), -14498466);
            SimpleEditorScreen.this.activeSubEditor = null;
        }

        private ItemStack iconFor(NbtFileIO.NbtFileEntry entry) {
            Optional<ItemStack> opt;
            if (entry == null || entry.path() == null) {
                return ItemStack.EMPTY;
            }
            String key = entry.path().toString();
            ItemStack cached = this.iconCache.get(key);
            if (cached != null) {
                return cached;
            }
            ItemStack icon = ItemStack.EMPTY;
            CompoundTag tag = NbtFileIO.importNbt(entry.path());
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
            String itemId = SimpleEditorScreen.this.editStack.getItem().builtInRegistryHolder().key().location().getPath();
            long ts = System.currentTimeMillis() / 1000L;
            this.fileName = itemId + "_" + ts;
            this.category = AnkiConfig.getLastExportCategory();
            this.alias = "";
            this.cursor = this.fileName.length();
            this.existingCats = AnkiConfig.listExportCategories();
        }

        @Override
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 380);
            int dh = 240;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.export_nbt"), dx + 10, dy + 8, -1906448, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int fieldX = dx + 10;
            int fieldW = dw - 20;
            int fieldH = 20;
            int curY = dy + 28;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.export.category"), fieldX, curY, -7035976, false);
            g.fill(fieldX, curY += 12, fieldX + fieldW, curY + fieldH, -15592930);
            SimpleEditorScreen.this.drawBorder(g, fieldX, curY, fieldW, fieldH, this.focusField == 1 ? -10262799 : -14540234);
            String catDisp = this.category.isEmpty() ? SimpleEditorScreen.tr("ankinbt.export.no_category") : this.category;
            g.drawString(font, catDisp + (this.focusField == 1 && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), fieldX + 4, curY + 6, this.category.isEmpty() ? -10193781 : -1906448, false);
            curY += fieldH + 4;
            if (!this.existingCats.isEmpty()) {
                int tagX = fieldX;
                for (String cat : this.existingCats) {
                    int tw = font.width(cat) + 10;
                    if (tagX + tw > fieldX + fieldW) {
                        tagX = fieldX;
                        curY += 16;
                    }
                    boolean hover = mx >= tagX && mx < tagX + tw && my >= curY && my < curY + 14;
                    boolean active = cat.equals(this.category);
                    g.fill(tagX, curY, tagX + tw, curY + 14, active ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
                    g.drawString(font, cat, tagX + 5, curY + 3, active ? -1906448 : -7035976, false);
                    tagX += tw + 4;
                }
                curY += 18;
            }
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.export.filename"), fieldX, curY, -7035976, false);
            g.fill(fieldX, curY += 12, fieldX + fieldW, curY + fieldH, -15592930);
            SimpleEditorScreen.this.drawBorder(g, fieldX, curY, fieldW, fieldH, this.focusField == 0 ? -10262799 : -14540234);
            g.drawString(font, this.fileName + ".nbt" + (this.focusField == 0 && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), fieldX + 4, curY + 6, -1906448, false);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.export.alias"), fieldX, curY += fieldH + 4, -7035976, false);
            g.fill(fieldX, curY += 12, fieldX + fieldW, curY + fieldH, -15592930);
            SimpleEditorScreen.this.drawBorder(g, fieldX, curY, fieldW, fieldH, this.focusField == 2 ? -10262799 : -14540234);
            String aliasDisp = this.alias.isEmpty() ? SimpleEditorScreen.tr("ankinbt.export.alias_hint") : this.alias;
            g.drawString(font, aliasDisp + (this.focusField == 2 && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), fieldX + 4, curY + 6, this.alias.isEmpty() ? -10193781 : -1906448, false);
            curY += fieldH + 6;
            Object pathPreview = AnkiConfig.getNbtExportDir();
            if (!this.category.isEmpty()) {
                pathPreview = (String)pathPreview + "/" + this.category;
            }
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.export.dir") + ": " + (String)pathPreview, fieldX, curY, -10193781, false);
            curY += 12;
            if (this.message != null) {
                g.drawString(font, this.message, fieldX, curY, this.msgColor, false);
            }
            int by = dy + dh - 28;
            int bw2 = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw2 - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.width(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw2 && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw2, by + bh2, oh ? -10262799 : -11581723);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.export.do_export"), okX + (bw2 - font.width(SimpleEditorScreen.tr("ankinbt.export.do_export"))) / 2, by + 6, -1906448, false);
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
                    int tw = SimpleEditorScreen.this.font.width(cat) + 10;
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
            if (key == 257 || key == 335) {
                this.doExport();
                return true;
            }
            if (key == 258) {
                this.focusField = (this.focusField + 1) % 3;
                return true;
            }
            String target = this.focusField == 0 ? this.fileName : (this.focusField == 1 ? this.category : this.alias);
            int cur = this.focusField == 0 ? this.cursor : (this.focusField == 1 ? this.category.length() : this.alias.length());
            if (key == 259 && cur > 0) {
                target = target.substring(0, cur - 1) + target.substring(cur);
                this.applyField(target, --cur);
                return true;
            }
            if (key == 261 && cur < target.length()) {
                target = target.substring(0, cur) + target.substring(cur + 1);
                this.applyField(target, cur);
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
            Optional<CompoundTag> opt = NbtHelper.serializeItemStack(SimpleEditorScreen.this.editStack);
            if (opt.isEmpty()) {
                this.message = SimpleEditorScreen.tr("ankinbt.export.failed");
                this.msgColor = -1096636;
                return;
            }
            Path path = null;
            if (SimpleEditorScreen.this.hasTinyFd()) {
                Path base = AnkiConfig.getExportPath(this.category.isBlank() ? null : this.category);
                String picked = SimpleEditorScreen.this.tinyFdSavePath(base.resolve(this.fileName + ".nbt").toString());
                if (picked == null || picked.isBlank()) {
                    return;
                }
                path = NbtFileIO.exportNbtToPath(opt.get(), Path.of(picked, new String[0]), this.alias.isBlank() ? null : this.alias);
            } else {
                path = NbtFileIO.exportNbt(opt.get(), this.fileName, this.category.isBlank() ? null : this.category, this.alias.isBlank() ? null : this.alias);
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
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 280);
            int dh = 160;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            String title = this.mode == -2 ? SimpleEditorScreen.tr("ankinbt.simple.name_color") : SimpleEditorScreen.tr("ankinbt.simple.dye_color_picker");
            g.drawString(font, title, dx + 10, dy + 8, -1906448, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
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
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[i] | 0xFF000000);
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
            }
            int prevY = gridY + 2 * (cellH + 2) + 8;
            g.fill(dx + 10, prevY, dx + 10 + 30, prevY + 20, this.selectedColor & 0xFFFFFF | 0xFF000000);
            SimpleEditorScreen.this.drawBorder(g, dx + 10, prevY, 30, 20, -14540234);
            g.drawString(font, String.format("#%06X", this.selectedColor & 0xFFFFFF), dx + 46, prevY + 6, -1906448, false);
            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw, by + bh2, oh ? -10262799 : -11581723);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.edit.apply"), okX + (bw - font.width(SimpleEditorScreen.tr("ankinbt.edit.apply"))) / 2, by + 6, -1906448, false);
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
                String name = SimpleEditorScreen.this.editStack.getHoverName().getString();
                SimpleEditorScreen.this.editStack.set(DataComponents.CUSTOM_NAME, Component.literal((String)name).withStyle(Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)this.selectedColor))));
                SimpleEditorScreen.this.dirty = true;
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
            this.searchBox = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 18, Component.empty());
            this.searchBox.setMaxLength(128);
            this.searchBox.setHint(Component.translatable("ankinbt.search.hint"));
            this.searchBox.setResponder(value -> {
                this.searchQ = value == null ? "" : value;
                this.filter();
            });
            this.searchBox.setFocused(true);
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
                    String zh = ATTR_ZH.get(s);
                    return zh != null && zh.contains(q);
                }).collect(Collectors.toList());
            }
            this.scrollOff = 0;
            this.selectedIdx = -1;
        }

        @Override
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.pick_attr"), x + 8, y + 4, -1906448, false);
            int sx = x + 8;
            int sy = y + 18;
            int sw = w - 16;
            int sh = 18;
            this.searchBox.setX(sx);
            this.searchBox.setY(sy);
            this.searchBox.setWidth(sw);
            this.searchBox.setFocused(this.focusField == 0);
            this.searchBox.render(g, mx, my, 0.0f);
            int ly = sy + sh + 4;
            int listH = h - 140;
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
                    g.fill(x + 8, ry, x + w - 8, ry + 16, 677603057);
                } else if (hovered) {
                    g.fill(x + 8, ry, x + w - 8, ry + 16, 0x30FFFFFF);
                }
                String attrId = this.filtered.get(i);
                g.renderItem(ItemEditorVisuals.attributeIconStack(attrId), x + 10, ry);
                String displayName = SimpleEditorScreen.this.getAttrDisplayName(attrId);
                int maxNameW = Math.max(40, w - 56);
                if (font.width(displayName) > maxNameW) {
                    displayName = font.plainSubstrByWidth(displayName, Math.max(10, maxNameW - 10)) + "..";
                }
                g.drawString(font, displayName, x + 30, ry + 4, sel ? -1906448 : -7035976, false);
            }
            int bottomY = y + h - 90;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.attr_amount"), x + 8, bottomY + 4, -7035976, false);
            int ax = x + 8 + font.width(SimpleEditorScreen.tr("ankinbt.simple.attr_amount")) + 4;
            int aw = 80;
            g.fill(ax, bottomY, ax + aw, bottomY + 18, -15592930);
            SimpleEditorScreen.this.drawBorder(g, ax, bottomY, aw, 18, this.focusField == 1 ? -10262799 : -14540234);
            g.drawString(font, this.amountInput + (this.focusField == 1 && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), ax + 4, bottomY + 5, -1906448, false);
            int opY = bottomY + 22;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.attr_operation"), x + 8, opY + 4, -7035976, false);
            int opX = x + 8 + font.width(SimpleEditorScreen.tr("ankinbt.simple.attr_operation")) + 4;
            String[] opLabels = this.isZh() ? OP_NAMES_ZH : OP_NAMES_EN;
            for (int i = 0; i < 3; ++i) {
                boolean active;
                int bw = font.width(opLabels[i]) + 10;
                boolean hover = mx >= opX && mx < opX + bw && my >= opY && my < opY + 18;
                boolean bl = active = i == this.selectedOp;
                g.fill(opX, opY, opX + bw, opY + 18, active ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
                g.drawString(font, opLabels[i], opX + 5, opY + 5, active ? -1906448 : -7035976, false);
                opX += bw + 4;
            }
            int slotY = opY + 22;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.attr_slot"), x + 8, slotY + 4, -7035976, false);
            int slotX = x + 8 + font.width(SimpleEditorScreen.tr("ankinbt.simple.attr_slot")) + 4;
            for (int i = 0; i < SLOT_KEYS.length; ++i) {
                boolean active;
                String slotLabel = this.isZh() ? SLOT_ZH.getOrDefault(SLOT_KEYS[i], SLOT_KEYS[i]) : SLOT_KEYS[i];
                int bw = font.width(slotLabel) + 8;
                boolean hover = mx >= slotX && mx < slotX + bw && my >= slotY && my < slotY + 18;
                boolean bl = active = i == this.selectedSlot;
                g.fill(slotX, slotY, slotX + bw, slotY + 18, active ? -10262799 : (hover ? 0x50FFFFFF : 0x30FFFFFF));
                g.drawString(font, slotLabel, slotX + 4, slotY + 5, active ? -1906448 : -7035976, false);
                if ((slotX += bw + 3) <= x + w - 40 || i >= SLOT_KEYS.length - 1) continue;
                slotX = x + 8 + font.width(SimpleEditorScreen.tr("ankinbt.simple.attr_slot")) + 4;
                slotY += 20;
            }
            int confirmY = y + h - 24;
            int confirmX = x + w - 78;
            boolean ch = mx >= confirmX && mx < confirmX + 70 && my >= confirmY && my < confirmY + 20;
            g.fill(confirmX, confirmY, confirmX + 70, confirmY + 20, ch ? -10262799 : -11581723);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.add.confirm"), confirmX + (70 - font.width(SimpleEditorScreen.tr("ankinbt.add.confirm"))) / 2, confirmY + 6, -1906448, false);
        }

        private boolean isZh() {
            String lang = Minecraft.getInstance().options.languageCode;
            return lang != null && lang.startsWith("zh");
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int sx = x + 8;
            int sy = y + 18;
            int sw = w - 16;
            int sh = 18;
            this.searchBox.setX(sx);
            this.searchBox.setY(sy);
            this.searchBox.setWidth(sw);
            if (this.searchBox.mouseClicked(mx, my, btn)) {
                this.focusField = 0;
                this.searchBox.setFocused(true);
                return true;
            }
            int bottomY = y + h - 90;
            int ax = x + 8 + SimpleEditorScreen.this.font.width(SimpleEditorScreen.tr("ankinbt.simple.attr_amount")) + 4;
            if (mx >= (double)ax && mx < (double)(ax + 80) && my >= (double)bottomY && my < (double)(bottomY + 18)) {
                this.focusField = 1;
                return true;
            }
            int opY = bottomY + 22;
            int opX = x + 8 + SimpleEditorScreen.this.font.width(SimpleEditorScreen.tr("ankinbt.simple.attr_operation")) + 4;
            String[] opLabels = this.isZh() ? OP_NAMES_ZH : OP_NAMES_EN;
            for (int i = 0; i < 3; ++i) {
                int bw = SimpleEditorScreen.this.font.width(opLabels[i]) + 10;
                if (mx >= (double)opX && mx < (double)(opX + bw) && my >= (double)opY && my < (double)(opY + 18)) {
                    this.selectedOp = i;
                    return true;
                }
                opX += bw + 4;
            }
            int slotY = opY + 22;
            int slotX = x + 8 + SimpleEditorScreen.this.font.width(SimpleEditorScreen.tr("ankinbt.simple.attr_slot")) + 4;
            for (int i = 0; i < SLOT_KEYS.length; ++i) {
                String slotLabel = this.isZh() ? SLOT_ZH.getOrDefault(SLOT_KEYS[i], SLOT_KEYS[i]) : SLOT_KEYS[i];
                int bw = SimpleEditorScreen.this.font.width(slotLabel) + 8;
                if (mx >= (double)slotX && mx < (double)(slotX + bw) && my >= (double)slotY && my < (double)(slotY + 18)) {
                    this.selectedSlot = i;
                    return true;
                }
                if ((slotX += bw + 3) <= x + w - 40 || i >= SLOT_KEYS.length - 1) continue;
                slotX = x + 8 + SimpleEditorScreen.this.font.width(SimpleEditorScreen.tr("ankinbt.simple.attr_slot")) + 4;
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
                if (this.searchBox.keyPressed(key, scan, mod)) {
                    this.searchQ = this.searchBox.getValue();
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
                    this.searchBox.charTyped(c, mod);
                    this.searchQ = this.searchBox.getValue();
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

        private EquipmentSlotGroup slotFromKey(String key) {
            return switch (key) {
                case "mainhand" -> EquipmentSlotGroup.MAINHAND;
                case "offhand" -> EquipmentSlotGroup.OFFHAND;
                case "head" -> EquipmentSlotGroup.HEAD;
                case "chest" -> EquipmentSlotGroup.CHEST;
                case "legs" -> EquipmentSlotGroup.LEGS;
                case "feet" -> EquipmentSlotGroup.FEET;
                case "hand" -> EquipmentSlotGroup.HAND;
                case "armor" -> EquipmentSlotGroup.ARMOR;
                default -> EquipmentSlotGroup.ANY;
            };
        }

        private AttributeModifier.Operation opFromIndex(int idx) {
            return switch (idx) {
                case 1 -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                case 2 -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                default -> AttributeModifier.Operation.ADD_VALUE;
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
            List<Component> lore = SimpleEditorScreen.this.getLore();
            if (lore.isEmpty()) {
                this.lines.add("");
            } else {
                for (int i = 0; i < lore.size(); ++i) {
                    this.lines.add(SimpleEditorScreen.this.getLoreRawText(i));
                }
            }
            this.activeLine = Math.max(0, this.lines.size() - 1);
            this.rebuildLineBoxes();
            this.activeBox().setCursorPosition(this.activeBox().getValue().length());
        }

        @Override
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, Math.max(320, Math.min(500, w - 24)));
            int dh = Math.min(h - 10, Math.max(220, Math.min(340, h - 16)));
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.lore_text_editor"), dx + 10, dy + 8, -1906448, false);
            String rawLabel = this.showRawCodes ? SimpleEditorScreen.tr("ankinbt.simple.lore_show_preview") : SimpleEditorScreen.tr("ankinbt.simple.lore_show_raw");
            int rawBtnW = font.width(rawLabel) + 10;
            int rawBtnX = dx + dw - rawBtnW - 10;
            boolean rawBtnHover = mx >= rawBtnX && mx < rawBtnX + rawBtnW && my >= dy + 4 && my < dy + 18;
            g.fill(rawBtnX, dy + 4, rawBtnX + rawBtnW, dy + 18, rawBtnHover ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, rawLabel, rawBtnX + 5, dy + 8, rawBtnHover ? -1906448 : -7035976, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);

            int textX = dx + 10;
            int textY = dy + 30;
            int textW = dw - 20;
            int textH = dh - 74;
            int contentX = textX + 24;
            int lineH = 14;
            int maxVisLines = Math.max(1, textH / lineH);
            this.scrollToCursor(maxVisLines);
            g.fill(textX - 2, textY - 2, textX + textW + 2, textY + textH + 2, -15592930);
            SimpleEditorScreen.this.drawBorder(g, textX - 2, textY - 2, textW + 4, textH + 4, -14540234);
            g.enableScissor(textX, textY, textX + textW, textY + textH);
            int end = Math.min(this.scrollOff + maxVisLines, this.lines.size());
            for (int i = this.scrollOff; i < end; ++i) {
                int ly = textY + (i - this.scrollOff) * lineH;
                String line = this.lines.get(i);
                FlatEditBox box = this.lineBoxes.get(i);
                g.drawString(font, String.valueOf(i + 1), textX, ly + 2, -10193781, false);
                if (i == this.activeLine) {
                    g.fill(contentX - 2, ly, textX + textW, ly + lineH, 0x18FFFFFF);
                }
                this.configureLineBox(box, contentX, ly, textW - 28, lineH, i == this.activeLine);
                if (this.showRawCodes) {
                    box.render(g, mx, my, 0.0f);
                } else {
                    g.drawString(font, SimpleEditorScreen.colorCodedToComponent(line), contentX, ly + 2, -1906448, false);
                }
            }
            g.disableScissor();
            if (this.lines.size() > maxVisLines) {
                int sbx = textX + textW - 4;
                g.fill(sbx, textY, sbx + 4, textY + textH, 0x30FFFFFF);
                float ratio = (float)maxVisLines / (float)this.lines.size();
                int thumbH = Math.max(8, (int)((float)textH * ratio));
                float sr = (float)this.scrollOff / (float)Math.max(1, this.lines.size() - maxVisLines);
                int thumbY = textY + (int)((float)(textH - thumbH) * sr);
                g.fill(sbx, thumbY, sbx + 4, thumbY + thumbH, 0x70FFFFFF);
            }

            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int palX = dx + 10;
            boolean palH = mx >= palX && mx < palX + 62 && my >= by && my < by + bh2;
            g.fill(palX, by, palX + 62, by + bh2, palH ? 0x50FFFFFF : 0x30FFFFFF);
            String palLabel = SimpleEditorScreen.tr("ankinbt.simple.color_palette");
            g.drawString(font, palLabel, palX + (62 - font.width(palLabel)) / 2, by + 6, -7035976, false);
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(SimpleEditorScreen.tr("ankinbt.edit.cancel"))) / 2, by + 6, -7035976, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw, by + bh2, oh ? -10262799 : -11581723);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.edit.apply"), okX + (bw - font.width(SimpleEditorScreen.tr("ankinbt.edit.apply"))) / 2, by + 6, -1906448, false);
            String state = this.activeLine + 1 + ":" + this.cursorCol() + " | " + this.lines.size() + SimpleEditorScreen.tr("ankinbt.simple.lore_lines_suffix");
            g.drawString(font, state, dx + dw - font.width(state) - 10, by + 6, -10193781, false);
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
            FlatEditBox box = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 14, Component.empty());
            box.setMaxLength(2048);
            box.setBordered(false);
            box.setValue(value == null ? "" : value);
            return box;
        }

        private void rebuildLineBoxes() {
            this.lineBoxes.clear();
            for (String line : this.lines) {
                this.lineBoxes.add(this.newLineBox(line));
            }
            this.focusActiveBox();
        }

        private void configureLineBox(FlatEditBox box, int x, int y, int w, int h, boolean focused) {
            box.setX(x);
            box.setY(y);
            box.setWidth(Math.max(1, w));
            box.setFocused(focused);
        }

        private void focusActiveBox() {
            this.ensureLineBoxes();
            for (int i = 0; i < this.lineBoxes.size(); ++i) {
                this.lineBoxes.get(i).setFocused(i == this.activeLine);
            }
        }

        private int cursorCol() {
            FlatEditBox box = this.activeBox();
            return Math.max(0, Math.min(box.getCursorPosition(), box.getValue().length()));
        }

        private void syncLine(int index) {
            if (index >= 0 && index < this.lines.size() && index < this.lineBoxes.size()) {
                this.lines.set(index, this.lineBoxes.get(index).getValue());
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
            String selected = box.getHighlighted();
            return selected != null && !selected.isEmpty();
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, Math.max(320, Math.min(500, w - 24)));
            int dh = Math.min(h - 10, Math.max(220, Math.min(340, h - 16)));
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            String rawLabel = this.showRawCodes ? SimpleEditorScreen.tr("ankinbt.simple.lore_show_preview") : SimpleEditorScreen.tr("ankinbt.simple.lore_show_raw");
            int rawBtnW = SimpleEditorScreen.this.font.width(rawLabel) + 10;
            int rawBtnX = dx + dw - rawBtnW - 10;
            if (mx >= (double)rawBtnX && mx < (double)(rawBtnX + rawBtnW) && my >= (double)(dy + 4) && my < (double)(dy + 18)) {
                this.showRawCodes = !this.showRawCodes;
                return true;
            }
            int by = dy + dh - 28;
            int bw = 70;
            int bh2 = 20;
            int palX = dx + 10;
            if (mx >= (double)palX && mx < (double)(palX + 62) && my >= (double)by && my < (double)(by + bh2)) {
                InlineFieldEditor tempEditor = new InlineFieldEditor("lore_text_temp", this.activeBox().getValue(), true);
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
            int textY = dy + 30;
            int textW = dw - 20;
            int textH = dh - 74;
            if (mx >= (double)textX && mx < (double)(textX + textW) && my >= (double)textY && my < (double)(textY + textH)) {
                int lineH = 14;
                int clickedLine = (int)((my - (double)textY) / (double)lineH) + this.scrollOff;
                if (clickedLine >= 0 && clickedLine < this.lines.size()) {
                    int contentX = textX + 24;
                    int ly = textY + (clickedLine - this.scrollOff) * lineH;
                    FlatEditBox box = this.lineBoxes.get(clickedLine);
                    this.configureLineBox(box, contentX, ly, textW - 28, lineH, true);
                    this.setActiveLine(clickedLine);
                    if (this.showRawCodes) {
                        box.mouseClicked(mx, my, btn);
                    } else {
                        box.setCursorPosition(box.getValue().length());
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
                this.activeBox().setCursorPosition(0);
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
                this.activeBox().setCursorPosition(col);
                return true;
            }
            if (key == 261 && this.cursorCol() == box.getValue().length() && !this.hasLineSelection(box) && this.activeLine < this.lines.size() - 1) {
                this.syncLine(this.activeLine);
                this.lines.set(this.activeLine, this.lines.get(this.activeLine) + this.lines.remove(this.activeLine + 1));
                this.rebuildLineBoxes();
                this.setActiveLine(this.activeLine);
                return true;
            }
            if (key == 265 && this.activeLine > 0) {
                int col = this.cursorCol();
                this.setActiveLine(this.activeLine - 1);
                this.activeBox().setCursorPosition(Math.min(col, this.activeBox().getValue().length()));
                return true;
            }
            if (key == 264 && this.activeLine < this.lines.size() - 1) {
                int col = this.cursorCol();
                this.setActiveLine(this.activeLine + 1);
                this.activeBox().setCursorPosition(Math.min(col, this.activeBox().getValue().length()));
                return true;
            }
            if (key == 263 && this.cursorCol() == 0 && this.activeLine > 0 && !this.hasLineSelection(box)) {
                this.setActiveLine(this.activeLine - 1);
                this.activeBox().setCursorPosition(this.activeBox().getValue().length());
                return true;
            }
            if (key == 262 && this.cursorCol() == box.getValue().length() && this.activeLine < this.lines.size() - 1 && !this.hasLineSelection(box)) {
                this.setActiveLine(this.activeLine + 1);
                this.activeBox().setCursorPosition(0);
                return true;
            }
            if (box.keyPressed(key, scan, mod)) {
                this.syncLine(this.activeLine);
                return true;
            }
            this.syncLine(this.activeLine);
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            FlatEditBox box = this.activeBox();
            if (box.charTyped(c, mod)) {
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
            String selected = box.getHighlighted();
            if (selected != null && !selected.isEmpty() && !suffix.isEmpty()) {
                box.insertText((text == null ? "" : text) + selected + suffix);
            } else {
                box.insertText(text == null ? "" : text);
            }
            this.syncLine(this.activeLine);
        }

        private void applyAll() {
            this.syncAllLines();
            while (this.lines.size() > 1 && this.lines.get(this.lines.size() - 1).isEmpty()) {
                this.lines.remove(this.lines.size() - 1);
            }
            ArrayList<Component> loreComponents = new ArrayList<Component>();
            for (String line : this.lines) {
                if (!line.isEmpty() || this.lines.size() == 1) {
                    loreComponents.add(SimpleEditorScreen.colorCodedToComponent(line));
                    continue;
                }
                loreComponents.add((Component)Component.empty());
            }
            if (loreComponents.size() == 1 && this.lines.get(0).isEmpty()) {
                SimpleEditorScreen.this.editStack.remove(DataComponents.LORE);
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
            this.searchBox = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 18, Component.empty());
            this.searchBox.setMaxLength(128);
            this.searchBox.setHint(Component.translatable("ankinbt.search.hint"));
            this.searchBox.setResponder(value -> {
                this.searchQ = value == null ? "" : value;
                this.filter();
            });
            this.searchBox.setFocused(true);
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
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.pick_enchant"), x + 8, y + 4, -1906448, false);
            int sx = x + 8;
            int sy = y + 18;
            int sw = w - 16;
            int sh = 18;
            this.searchBox.setX(sx);
            this.searchBox.setY(sy);
            this.searchBox.setWidth(sw);
            this.searchBox.setFocused(!this.focusLevel);
            this.searchBox.render(g, mx, my, 0.0f);
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
                    g.fill(x + 8, ry, x + w - 8, ry + 16, 677603057);
                } else if (hovered) {
                    g.fill(x + 8, ry, x + w - 8, ry + 16, 0x30FFFFFF);
                }
                String enchId = this.filtered.get(i);
                g.renderItem(ItemEditorVisuals.enchantIconStack(enchId), x + 10, ry);
                String displayName = SimpleEditorScreen.this.getEnchantDisplayName(enchId);
                int maxNameW = Math.max(40, w - 58);
                if (font.width(displayName) > maxNameW) {
                    displayName = font.plainSubstrByWidth(displayName, Math.max(10, maxNameW - 10)) + "..";
                }
                g.drawString(font, displayName, x + 30, ry + 4, sel ? -1906448 : -7035976, false);
            }
            int by = y + h - 30;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.level"), x + 8, by + 6, -7035976, false);
            int lx = x + 8 + font.width(SimpleEditorScreen.tr("ankinbt.simple.level")) + 4;
            g.fill(lx, by + 2, lx + 40, by + 20, -15592930);
            SimpleEditorScreen.this.drawBorder(g, lx, by + 2, 40, 18, this.focusLevel ? -10262799 : -14540234);
            g.drawString(font, this.levelInput + (this.focusLevel && System.currentTimeMillis() % 1000L < 500L ? "_" : ""), lx + 4, by + 7, -1906448, false);
            int confirmX = x + w - 78;
            boolean ch = mx >= confirmX && mx < confirmX + 70 && my >= by + 1 && my < by + 21;
            g.fill(confirmX, by + 1, confirmX + 70, by + 21, ch ? -10262799 : -11581723);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.add.confirm"), confirmX + (70 - font.width(SimpleEditorScreen.tr("ankinbt.add.confirm"))) / 2, by + 7, -1906448, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int sx = x + 8;
            int sy = y + 18;
            int sw = w - 16;
            int sh = 18;
            this.searchBox.setX(sx);
            this.searchBox.setY(sy);
            this.searchBox.setWidth(sw);
            if (this.searchBox.mouseClicked(mx, my, btn)) {
                this.focusLevel = false;
                this.searchBox.setFocused(true);
                return true;
            }
            int by = y + h - 30;
            int lx = x + 8 + SimpleEditorScreen.this.font.width(SimpleEditorScreen.tr("ankinbt.simple.level")) + 4;
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
                if (this.searchBox.keyPressed(key, scan, mod)) {
                    this.searchQ = this.searchBox.getValue();
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
                    this.searchBox.charTyped(c, mod);
                    this.searchQ = this.searchBox.getValue();
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

    class LoreColorInsertEditor
    implements SubEditor {
        final InlineFieldEditor parent;

        LoreColorInsertEditor(InlineFieldEditor parent) {
            this.parent = parent;
        }

        @Override
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340);
            int dh = 260;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.color_palette"), dx + 10, dy + 8, -1906448, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int gridX = dx + 12;
            int gridY = dy + 28;
            int cellW = (dw - 24) / 8;
            int cellH = 28;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.palette_bright"), dx + 10, gridY - 1, -10193781, false);
            int[] brightOrder = new int[]{6, 14, 10, 11, 9, 13, 12, 15};
            for (int i = 0; i < 8; ++i) {
                int ci = brightOrder[i];
                int cx = gridX + i * cellW;
                int cy = gridY + 8;
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
            }
            int darkY = gridY + cellH + 14;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.palette_dark"), dx + 10, darkY - 1, -10193781, false);
            int[] darkOrder = new int[]{0, 8, 4, 5, 1, 3, 2, 7};
            for (int i = 0; i < 8; ++i) {
                int ci = darkOrder[i];
                int cx = gridX + i * cellW;
                int cy = darkY + 8;
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (ci == 0 || ci == 8) {
                    SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, 0x40FFFFFF);
                }
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
            }
            int fmtY = darkY + cellH + 20;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.format_codes"), dx + 10, fmtY, -7035976, false);
            fmtY += 14;
            int fmtX = gridX;
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; ++i) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.width(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) {
                    fmtX = gridX;
                    fmtY += 22;
                }
                boolean hover = mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18;
                g.fill(fmtX, fmtY, fmtX + pillW, fmtY + 18, hover ? -10262799 : 0x30FFFFFF);
                SimpleEditorScreen.this.drawBorder(g, fmtX, fmtY, pillW, 18, hover ? -10262799 : 0x20FFFFFF);
                g.drawString(font, fLabel, fmtX + 7, fmtY + 5, hover ? -1906448 : -7035976, false);
                fmtX += pillW + 6;
            }
            int backY = dy + dh - 26;
            int backW = 70;
            int backX = dx + (dw - backW) / 2;
            boolean bh2 = mx >= backX && mx < backX + backW && my >= backY && my < backY + 20;
            g.fill(backX, backY, backX + backW, backY + 20, bh2 ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.back"), backX + (backW - font.width(SimpleEditorScreen.tr("ankinbt.simple.back"))) / 2, backY + 6, -7035976, false);
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
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; ++i) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = SimpleEditorScreen.this.font.width(fLabel) + 14;
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
        public void render(GuiGraphics g, Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340);
            int dh = 260;
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, -267909104);
            SimpleEditorScreen.this.drawBorder(g, dx, dy, dw, dh, -10262799);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.color_palette"), dx + 10, dy + 8, -1906448, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, -14540234);
            int gridX = dx + 12;
            int gridY = dy + 28;
            int cellW = (dw - 24) / 8;
            int cellH = 28;
            this.hoveredColor = -1;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.palette_bright"), dx + 10, gridY - 1, -10193781, false);
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
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
                String lang = Minecraft.getInstance().options.languageCode;
                String tip = "&" + MC_COLOR_CODES[ci] + " " + (lang != null && lang.startsWith("zh") ? MC_COLOR_NAMES_ZH[ci] : MC_COLOR_CODES[ci]);
                int tipW = font.width(tip) + 8;
                g.fill(mx + 8, my - 14, mx + 8 + tipW, my - 1, -267382752);
                g.drawString(font, tip, mx + 12, my - 12, -1906448, false);
            }
            int darkY = gridY + cellH + 14;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.palette_dark"), dx + 10, darkY - 1, -10193781, false);
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
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, bgColor);
                if (ci == 0 || ci == 8) {
                    SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, 0x40FFFFFF);
                }
                if (!hover) continue;
                SimpleEditorScreen.this.drawBorder(g, cx, cy, cellW - 2, cellH, -1);
                String lang = Minecraft.getInstance().options.languageCode;
                String tip = "&" + MC_COLOR_CODES[ci] + " " + (lang != null && lang.startsWith("zh") ? MC_COLOR_NAMES_ZH[ci] : MC_COLOR_CODES[ci]);
                int tipW = font.width(tip) + 8;
                g.fill(mx + 8, my - 14, mx + 8 + tipW, my - 1, -267382752);
                g.drawString(font, tip, mx + 12, my - 12, -1906448, false);
            }
            int fmtY = darkY + cellH + 20;
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.format_codes"), dx + 10, fmtY, -7035976, false);
            fmtY += 14;
            int fmtX = gridX;
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; ++i) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.width(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) {
                    fmtX = gridX;
                    fmtY += 22;
                }
                boolean hover = mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18;
                g.fill(fmtX, fmtY, fmtX + pillW, fmtY + 18, hover ? -10262799 : 0x30FFFFFF);
                SimpleEditorScreen.this.drawBorder(g, fmtX, fmtY, pillW, 18, hover ? -10262799 : 0x20FFFFFF);
                g.drawString(font, fLabel, fmtX + 7, fmtY + 5, hover ? -1906448 : -7035976, false);
                fmtX += pillW + 6;
            }
            int backY = dy + dh - 26;
            int backW = 70;
            int backX = dx + (dw - backW) / 2;
            boolean bh2 = mx >= backX && mx < backX + backW && my >= backY && my < backY + 20;
            g.fill(backX, backY, backX + backW, backY + 20, bh2 ? 0x50FFFFFF : 0x30FFFFFF);
            g.drawString(font, SimpleEditorScreen.tr("ankinbt.simple.back"), backX + (backW - font.width(SimpleEditorScreen.tr("ankinbt.simple.back"))) / 2, backY + 6, -7035976, false);
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
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; ++i) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = SimpleEditorScreen.this.font.width(fLabel) + 14;
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
