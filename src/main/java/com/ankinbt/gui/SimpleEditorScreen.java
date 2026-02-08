package com.ankinbt.gui;

import com.ankinbt.nbt.NbtHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.*;
import java.util.stream.Collectors;

import com.ankinbt.compat.VersionCompat;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Simple Mode editor: visual buttons for common item modifications.
 * Zero NBT knowledge required. Switchable with Advanced Mode.
 */
public class SimpleEditorScreen extends Screen {

    // Layout
    private static final int HEADER_H = 32;
    private static final int FOOTER_H = 20;
    private static final int MARGIN = 16;
    private static final int SIDEBAR_W = 140;
    private static final int SCROLLBAR_W = 6;
    private static final int ROW_H = 24;
    private static final int CAT_H = 28;

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
    private static final int CAT_BG = 0x20FFFFFF;

    // Minecraft color codes
    private static final char SECTION = '\u00A7';
    private static final String[] MC_COLOR_CODES = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"
    };
    private static final String[] MC_COLOR_NAMES_ZH = {
        "黑色", "深蓝", "深绿", "深青", "深红", "紫色", "金色", "灰色",
        "深灰", "蓝色", "绿色", "青色", "红色", "粉红", "黄色", "白色"
    };
    private static final int[] MC_COLORS = {
        0xFF000000, 0xFF0000AA, 0xFF00AA00, 0xFF00AAAA, 0xFFAA0000, 0xFFAA00AA, 0xFFFFAA00, 0xFFAAAAAA,
        0xFF555555, 0xFF5555FF, 0xFF55FF55, 0xFF55FFFF, 0xFFFF5555, 0xFFFF55FF, 0xFFFFFF55, 0xFFFFFFFF
    };
    private static final String[] MC_FORMAT_CODES = { "k", "l", "m", "n", "o", "r" };
    private static final String[] MC_FORMAT_NAMES_ZH = { "随机", "粗体", "删除线", "下划线", "斜体", "重置" };
    private static final String[] MC_FORMAT_NAMES_EN = { "Obfuscated", "Bold", "Strikethrough", "Underline", "Italic", "Reset" };

    private static final Map<String, String> ENCHANT_ZH = new LinkedHashMap<>();
    static {
        ENCHANT_ZH.put("minecraft:protection", "保护");
        ENCHANT_ZH.put("minecraft:fire_protection", "火焰保护");
        ENCHANT_ZH.put("minecraft:feather_falling", "摔落保护");
        ENCHANT_ZH.put("minecraft:blast_protection", "爆炸保护");
        ENCHANT_ZH.put("minecraft:projectile_protection", "弹射物保护");
        ENCHANT_ZH.put("minecraft:respiration", "水下呼吸");
        ENCHANT_ZH.put("minecraft:aqua_affinity", "水下速掘");
        ENCHANT_ZH.put("minecraft:thorns", "荆棘");
        ENCHANT_ZH.put("minecraft:depth_strider", "深海探索者");
        ENCHANT_ZH.put("minecraft:frost_walker", "冰霜行者");
        ENCHANT_ZH.put("minecraft:binding_curse", "绑定诅咒");
        ENCHANT_ZH.put("minecraft:soul_speed", "灵魂疾行");
        ENCHANT_ZH.put("minecraft:swift_sneak", "迅捷潜行");
        ENCHANT_ZH.put("minecraft:sharpness", "锋利");
        ENCHANT_ZH.put("minecraft:smite", "亡灵杀手");
        ENCHANT_ZH.put("minecraft:bane_of_arthropods", "节肢杀手");
        ENCHANT_ZH.put("minecraft:knockback", "击退");
        ENCHANT_ZH.put("minecraft:fire_aspect", "火焰附加");
        ENCHANT_ZH.put("minecraft:looting", "抢夺");
        ENCHANT_ZH.put("minecraft:sweeping_edge", "横扫之刃");
        ENCHANT_ZH.put("minecraft:efficiency", "效率");
        ENCHANT_ZH.put("minecraft:silk_touch", "精准采集");
        ENCHANT_ZH.put("minecraft:unbreaking", "耐久");
        ENCHANT_ZH.put("minecraft:fortune", "时运");
        ENCHANT_ZH.put("minecraft:power", "力量");
        ENCHANT_ZH.put("minecraft:punch", "冲击");
        ENCHANT_ZH.put("minecraft:flame", "火矢");
        ENCHANT_ZH.put("minecraft:infinity", "无限");
        ENCHANT_ZH.put("minecraft:luck_of_the_sea", "海之眷顾");
        ENCHANT_ZH.put("minecraft:lure", "饵钓");
        ENCHANT_ZH.put("minecraft:loyalty", "忠诚");
        ENCHANT_ZH.put("minecraft:impaling", "穿刺");
        ENCHANT_ZH.put("minecraft:riptide", "激流");
        ENCHANT_ZH.put("minecraft:channeling", "引雷");
        ENCHANT_ZH.put("minecraft:multishot", "多重射击");
        ENCHANT_ZH.put("minecraft:quick_charge", "快速装填");
        ENCHANT_ZH.put("minecraft:piercing", "穿透");
        ENCHANT_ZH.put("minecraft:density", "密度");
        ENCHANT_ZH.put("minecraft:breach", "破甲");
        ENCHANT_ZH.put("minecraft:wind_burst", "风爆");
        ENCHANT_ZH.put("minecraft:mending", "经验修补");
        ENCHANT_ZH.put("minecraft:vanishing_curse", "消失诅咒");
    }

    // Attribute Chinese name map
    private static final Map<String, String> ATTR_ZH = new LinkedHashMap<>();
    static {
        ATTR_ZH.put("minecraft:generic.max_health", "最大生命值");
        ATTR_ZH.put("minecraft:generic.follow_range", "跟随范围");
        ATTR_ZH.put("minecraft:generic.knockback_resistance", "击退抗性");
        ATTR_ZH.put("minecraft:generic.movement_speed", "移动速度");
        ATTR_ZH.put("minecraft:generic.flying_speed", "飞行速度");
        ATTR_ZH.put("minecraft:generic.attack_damage", "攻击伤害");
        ATTR_ZH.put("minecraft:generic.attack_knockback", "攻击击退");
        ATTR_ZH.put("minecraft:generic.attack_speed", "攻击速度");
        ATTR_ZH.put("minecraft:generic.armor", "护甲值");
        ATTR_ZH.put("minecraft:generic.armor_toughness", "护甲韧性");
        ATTR_ZH.put("minecraft:generic.luck", "幸运");
        ATTR_ZH.put("minecraft:generic.max_absorption", "最大吸收");
        ATTR_ZH.put("minecraft:generic.scale", "缩放");
        ATTR_ZH.put("minecraft:generic.step_height", "台阶高度");
        ATTR_ZH.put("minecraft:generic.gravity", "重力");
        ATTR_ZH.put("minecraft:generic.safe_fall_distance", "安全坠落距离");
        ATTR_ZH.put("minecraft:generic.fall_damage_multiplier", "坠落伤害倍率");
        ATTR_ZH.put("minecraft:generic.jump_strength", "跳跃力量");
        ATTR_ZH.put("minecraft:generic.block_interaction_range", "方块交互距离");
        ATTR_ZH.put("minecraft:generic.entity_interaction_range", "实体交互距离");
        ATTR_ZH.put("minecraft:generic.block_break_speed", "方块破坏速度");
        ATTR_ZH.put("minecraft:generic.mining_efficiency", "挖掘效率");
        ATTR_ZH.put("minecraft:generic.sneaking_speed", "潜行速度");
        ATTR_ZH.put("minecraft:generic.submerged_mining_speed", "水下挖掘速度");
        ATTR_ZH.put("minecraft:generic.sweeping_damage_ratio", "横扫伤害比");
    }

    // Slot Chinese names
    private static final Map<String, String> SLOT_ZH = new LinkedHashMap<>();
    static {
        SLOT_ZH.put("any", "任意");
        SLOT_ZH.put("mainhand", "主手");
        SLOT_ZH.put("offhand", "副手");
        SLOT_ZH.put("head", "头部");
        SLOT_ZH.put("chest", "胸部");
        SLOT_ZH.put("legs", "腿部");
        SLOT_ZH.put("feet", "脚部");
        SLOT_ZH.put("hand", "手持");
        SLOT_ZH.put("armor", "护甲");
    }

    // Operation Chinese names
    private static final String[] OP_NAMES_ZH = { "增加", "倍率增加", "倍率乘算" };
    private static final String[] OP_NAMES_EN = { "Add", "Multiply Base", "Multiply Total" };

    private ItemStack editStack;
    private final ItemStack originalStack;
    private final int inventorySlot; // -1 = main hand, >= 0 = inventory slot

    // Panel geometry
    private int px, py, pw, ph;
    private int sideX, sideY, sideW, sideH;
    private int contentX, contentY, contentW, contentH;

    // Categories
    private enum Category { GENERAL, ENCHANT, LORE, ATTRIBUTE, VISUAL, MISC }
    private Category activeCat = Category.GENERAL;

    // Scroll
    private int scrollOff = 0, maxRows;
    private int hoverRow = -1;
    private int sideScrollOff = 0; // sidebar scroll offset in pixels

    // Status
    private String statusMsg = null;
    private long statusTime = 0;
    private int statusColor = C3;
    private boolean dirty = false;

    // Sub-editor state
    private SubEditor activeSubEditor = null;

    // Header buttons
    private final List<Btn> headerBtns = new ArrayList<>();

    public SimpleEditorScreen(ItemStack stack) {
        this(stack, -1);
    }

    public SimpleEditorScreen(ItemStack stack, int inventorySlot) {
        super(Component.translatable("ankinbt.simple.title"));
        this.originalStack = stack;
        this.editStack = stack.copy();
        this.inventorySlot = inventorySlot;
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

        contentX = px + SIDEBAR_W + 2; contentY = py + HEADER_H + 1;
        contentW = pw - SIDEBAR_W - SCROLLBAR_W - 6;
        contentH = ph - HEADER_H - FOOTER_H - 2;
        maxRows = contentH / ROW_H;

        buildHeaderButtons();
    }

    private void buildHeaderButtons() {
        headerBtns.clear();
        int bw = 22, gap = 3, by = py + 6;
        int bx = px + pw - MARGIN - 2;

        bx -= bw;
        headerBtns.add(new Btn(bx, by, bw, bw, "X",
                Component.translatable("ankinbt.btn.close"), this::onClose));
        bx -= bw + gap;

        int saveW = 40;
        bx -= saveW + gap;
        headerBtns.add(new Btn(bx, by, saveW, bw,
                Component.translatable("ankinbt.btn.save").getString(),
                Component.translatable("ankinbt.btn.save.tip"), this::saveToItem));

        int modeW = 50;
        bx -= modeW + gap + 4;
        headerBtns.add(new Btn(bx, by, modeW, bw,
                Component.translatable("ankinbt.btn.advanced").getString(),
                Component.translatable("ankinbt.btn.switch_advanced"), this::switchToAdvanced));
    }

    // ==================== RENDER ====================

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x70000000);
        g.fill(px, py, px + pw, py + ph, BG);
        drawBorder(g, px, py, pw, ph, BORDER);

        // Header
        g.fill(px + 1, py + 1, px + pw - 1, py + HEADER_H, HEADER_BG);
        g.fill(px + 1, py + HEADER_H, px + pw - 1, py + HEADER_H + 1, BORDER);
        g.drawString(font, Component.translatable("ankinbt.simple.title"), px + 10, py + 12, C1, false);
        if (dirty) g.drawString(font, "*", px + 10 + font.width(Component.translatable("ankinbt.simple.title")), py + 12, ERROR_C, false);

        for (Btn b : headerBtns) b.render(g, font, mx, my);

        // Sidebar
        renderSidebar(g, mx, my);
        g.fill(px + SIDEBAR_W + 1, py + HEADER_H + 1, px + SIDEBAR_W + 2, py + ph - FOOTER_H, BORDER);

        // Content
        if (activeSubEditor != null) {
            activeSubEditor.render(g, font, mx, my, contentX, contentY, contentW, contentH);
        } else {
            renderCategoryContent(g, mx, my);
        }

        // Footer
        g.fill(px + 1, py + ph - FOOTER_H, px + pw - 1, py + ph - FOOTER_H + 1, BORDER);
        renderFooter(g);
    }

    private void renderSidebar(GuiGraphics g, int mx, int my) {
        g.fill(sideX, sideY, sideX + sideW, sideY + sideH, SIDEBAR_BG);

        // Fixed header area: item icon + name + divider
        int lx = sideX + 8;
        int headerY = sideY + 8;
        g.renderItem(editStack, lx + (sideW - 32) / 2, headerY);
        headerY += 24;
        String name = editStack.getHoverName().getString();
        if (font.width(name) > sideW - 16) name = font.plainSubstrByWidth(name, sideW - 22) + "...";
        g.drawString(font, name, lx, headerY, C1, false);
        headerY += 14;
        g.fill(lx, headerY, sideX + sideW - 8, headerY + 1, BORDER);
        headerY += 8;

        // Scrollable category area
        int catAreaY = headerY;
        int catAreaH = sideY + sideH - catAreaY;

        Category[] cats = Category.values();
        String[] catNames = {
            Component.translatable("ankinbt.cat.general").getString(),
            Component.translatable("ankinbt.cat.enchant").getString(),
            Component.translatable("ankinbt.cat.lore").getString(),
            Component.translatable("ankinbt.cat.attribute").getString(),
            Component.translatable("ankinbt.cat.visual").getString(),
            Component.translatable("ankinbt.cat.misc").getString()
        };

        int totalCatH = cats.length * (CAT_H + 2);
        int maxSideScroll = Math.max(0, totalCatH - catAreaH);
        sideScrollOff = Math.max(0, Math.min(sideScrollOff, maxSideScroll));

        // Clip to category area
        g.enableScissor(sideX, catAreaY, sideX + sideW, sideY + sideH);
        for (int i = 0; i < cats.length; i++) {
            int cy = catAreaY + i * (CAT_H + 2) - sideScrollOff;
            if (cy + CAT_H < catAreaY || cy > sideY + sideH) continue;
            int cw = sideW - 16;
            boolean hover = mx >= lx && mx < lx + cw && my >= cy && my < cy + CAT_H && my >= catAreaY && my < sideY + sideH;
            boolean active = cats[i] == activeCat;
            g.fill(lx, cy, lx + cw, cy + CAT_H, active ? ACCENT : (hover ? BTN_HOVER : CAT_BG));
            if (active) g.fill(lx, cy, lx + 2, cy + CAT_H, 0xFFFFFFFF);
            g.drawString(font, catNames[i], lx + 8, cy + (CAT_H - 8) / 2, active ? C1 : C2, false);
        }
        g.disableScissor();

        // Sidebar scrollbar
        if (totalCatH > catAreaH) {
            int sbx = sideX + sideW - 5;
            g.fill(sbx, catAreaY, sbx + 4, sideY + sideH, SB_TRACK);
            float ratio = (float) catAreaH / totalCatH;
            int thumbH = Math.max(12, (int) (catAreaH * ratio));
            float sr = (float) sideScrollOff / Math.max(1, maxSideScroll);
            int thumbY = catAreaY + (int) ((catAreaH - thumbH) * sr);
            g.fill(sbx, thumbY, sbx + 4, thumbY + thumbH, SB_THUMB);
        }
    }

    private void renderCategoryContent(GuiGraphics g, int mx, int my) {
        List<ActionRow> rows = getRowsForCategory(activeCat);
        hoverRow = -1;
        int end = Math.min(scrollOff + maxRows, rows.size());
        for (int i = scrollOff; i < end; i++) {
            int ry = contentY + (i - scrollOff) * ROW_H;
            ActionRow row = rows.get(i);
            boolean hovered = mx >= contentX && mx < contentX + contentW && my >= ry && my < ry + ROW_H;
            if (hovered) { hoverRow = i; g.fill(contentX, ry, contentX + contentW, ry + ROW_H, HOVER); }
            g.fill(contentX, ry + ROW_H - 1, contentX + contentW, ry + ROW_H, 0x10FFFFFF);
            g.drawString(font, row.label, contentX + 8, ry + (ROW_H - 8) / 2, row.labelColor, false);
            if (row.currentValue != null) {
                String val = row.currentValue;
                if (font.width(val) > contentW / 2) val = font.plainSubstrByWidth(val, contentW / 2 - 10) + "..";
                g.drawString(font, val, contentX + contentW - font.width(val) - 8, ry + (ROW_H - 8) / 2, C2, false);
            }
        }
        if (rows.size() > maxRows) {
            int sbx = px + pw - SCROLLBAR_W - 3;
            g.fill(sbx, contentY, sbx + SCROLLBAR_W, contentY + contentH, SB_TRACK);
            float ratio = (float) maxRows / rows.size();
            int thumbH = Math.max(16, (int) (contentH * ratio));
            float sr = (float) scrollOff / Math.max(1, rows.size() - maxRows);
            int thumbY = contentY + (int) ((contentH - thumbH) * sr);
            g.fill(sbx, thumbY, sbx + SCROLLBAR_W, thumbY + thumbH, SB_THUMB);
        }
    }

    private void renderFooter(GuiGraphics g) {
        int fy = py + ph - FOOTER_H + 5;
        if (statusMsg != null && System.currentTimeMillis() - statusTime < 3000) {
            g.drawString(font, statusMsg, px + SIDEBAR_W + 8, fy, statusColor, false);
        } else {
            statusMsg = null;
            g.drawString(font, Component.translatable("ankinbt.simple.hint"), px + SIDEBAR_W + 8, fy, C3, false);
        }
    }

    // ==================== INPUT ====================

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        for (Btn b : headerBtns) if (b.isHover((int) mx, (int) my)) { b.action.run(); return true; }
        if (activeSubEditor != null) return activeSubEditor.mouseClicked(mx, my, btn, contentX, contentY, contentW, contentH);

        int lx = sideX + 8;
        int catStartY = sideY + 8 + 24 + 14 + 1 + 8;
        int catAreaBottom = sideY + sideH;
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int cy = catStartY + i * (CAT_H + 2) - sideScrollOff;
            int cw = sideW - 16;
            if (cy + CAT_H < catStartY || cy > catAreaBottom) continue;
            if (mx >= lx && mx < lx + cw && my >= cy && my < cy + CAT_H && my >= catStartY && my < catAreaBottom) {
                activeCat = cats[i]; scrollOff = 0; return true;
            }
        }
        if (hoverRow >= 0) {
            List<ActionRow> rows = getRowsForCategory(activeCat);
            if (hoverRow < rows.size()) { rows.get(hoverRow).action.run(); return true; }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (activeSubEditor != null) return activeSubEditor.mouseScrolled(sx, sy);
        // Sidebar scroll
        if (mx >= sideX && mx < sideX + sideW && my >= sideY && my < sideY + sideH) {
            sideScrollOff -= (int) sy * 10;
            Category[] cats = Category.values();
            int catAreaY = sideY + 8 + 24 + 14 + 1 + 8;
            int catAreaH = sideY + sideH - catAreaY;
            int totalCatH = cats.length * (CAT_H + 2);
            int maxSideScroll = Math.max(0, totalCatH - catAreaH);
            sideScrollOff = Math.max(0, Math.min(sideScrollOff, maxSideScroll));
            return true;
        }
        // Content scroll
        List<ActionRow> rows = getRowsForCategory(activeCat);
        scrollOff -= (int) sy * 3;
        scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, rows.size() - maxRows)));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mod) {
        if (activeSubEditor != null) {
            if (key == 256) { activeSubEditor = null; return true; }
            return activeSubEditor.keyPressed(key, scan, mod);
        }
        if (key == 256) { onClose(); return true; }
        if (key == 83 && (mod & 2) != 0) { saveToItem(); return true; }
        return super.keyPressed(key, scan, mod);
    }

    @Override
    public boolean charTyped(char c, int mod) {
        if (activeSubEditor != null) return activeSubEditor.charTyped(c, mod);
        return super.charTyped(c, mod);
    }

    // ==================== CATEGORY ROWS ====================

    private List<ActionRow> getRowsForCategory(Category cat) {
        return switch (cat) {
            case GENERAL -> getGeneralRows();
            case ENCHANT -> getEnchantRows();
            case LORE -> getLoreRows();
            case ATTRIBUTE -> getAttributeRows();
            case VISUAL -> getVisualRows();
            case MISC -> getMiscRows();
        };
    }

    private List<ActionRow> getGeneralRows() {
        List<ActionRow> rows = new ArrayList<>();
        String nameVal = editStack.getHoverName().getString();
        rows.add(new ActionRow(tr("ankinbt.simple.rename"), nameVal, () -> openInlineEditor("rename", nameVal)));
        rows.add(new ActionRow(tr("ankinbt.simple.count"), String.valueOf(editStack.getCount()),
                () -> openInlineEditor("count", String.valueOf(editStack.getCount()))));

        int maxDmg = editStack.getMaxDamage();
        if (maxDmg > 0) {
            int dmg = editStack.getDamageValue();
            rows.add(new ActionRow(tr("ankinbt.simple.damage"), dmg + " / " + maxDmg,
                    () -> openInlineEditor("damage", String.valueOf(editStack.getDamageValue()))));
            rows.add(new ActionRow(tr("ankinbt.simple.max_damage"), String.valueOf(maxDmg),
                    () -> openInlineEditor("max_damage", String.valueOf(maxDmg))));
        }

        rows.add(new ActionRow(tr("ankinbt.simple.unbreakable"),
                isUnbreakable() ? tr("ankinbt.simple.on") : tr("ankinbt.simple.off"),
                this::toggleUnbreakable));
        rows.add(new ActionRow(tr("ankinbt.simple.max_stack"), String.valueOf(editStack.getMaxStackSize()),
                () -> openInlineEditor("max_stack", String.valueOf(editStack.getMaxStackSize()))));
        rows.add(new ActionRow(tr("ankinbt.simple.repair_cost"), String.valueOf(getRepairCost()),
                () -> openInlineEditor("repair_cost", String.valueOf(getRepairCost()))));

        // Fire resistant
        rows.add(new ActionRow(tr("ankinbt.simple.fire_resistant"),
                isFireResistant() ? tr("ankinbt.simple.on") : tr("ankinbt.simple.off"),
                this::toggleFireResistant));

        // Food (saturation/nutrition)
        if (VersionCompat.get().hasFood(editStack)) {
            rows.add(new ActionRow(tr("ankinbt.simple.food_nutrition"), String.valueOf(VersionCompat.get().getFoodNutrition(editStack)),
                    () -> openInlineEditor("food_nutrition", String.valueOf(VersionCompat.get().getFoodNutrition(editStack)))));
            rows.add(new ActionRow(tr("ankinbt.simple.food_saturation"), String.valueOf(VersionCompat.get().getFoodSaturation(editStack)),
                    () -> openInlineEditor("food_saturation", String.valueOf(VersionCompat.get().getFoodSaturation(editStack)))));
        }

        // Rarity
        var rarity = editStack.get(DataComponents.RARITY);
        if (rarity != null) {
            rows.add(new ActionRow(tr("ankinbt.simple.rarity"), rarity.name(), () -> cycleRarity()));
        }

        return rows;
    }

    private List<ActionRow> getEnchantRows() {
        List<ActionRow> rows = new ArrayList<>();
        ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(editStack);
        enchants.entrySet().forEach(entry -> {
            Holder<Enchantment> ench = entry.getKey();
            int level = entry.getIntValue();
            String eId = ench.unwrapKey().map(k -> k.location().toString()).orElse("?");
            String displayName = getEnchantDisplayName(eId);
            rows.add(new ActionRow(displayName, tr("ankinbt.simple.level") + level,
                    () -> openInlineEditor("ench_level:" + eId, String.valueOf(level))));
        });
        rows.add(new ActionRow(tr("ankinbt.simple.add_enchant"), null,
                () -> activeSubEditor = new EnchantPickerSubEditor(), ACCENT));
        if (!enchants.isEmpty()) {
            rows.add(new ActionRow(tr("ankinbt.simple.clear_enchants"), null, this::clearEnchantments, ERROR_C));
        }
        return rows;
    }

    private List<ActionRow> getLoreRows() {
        List<ActionRow> rows = new ArrayList<>();
        // Color code hint
        rows.add(new ActionRow(tr("ankinbt.simple.lore_color_hint"), null, () -> activeSubEditor = new ColorPickerSubEditor(-1), C3));

        List<Component> lore = getLore();
        for (int i = 0; i < lore.size(); i++) {
            int idx = i;
            String text = lore.get(i).getString();
            if (text.length() > 40) text = text.substring(0, 37) + "...";
            rows.add(new ActionRow((i + 1) + ". " + text, null,
                    () -> openLoreEditor("lore:" + idx, getLoreRawText(idx))));
        }
        rows.add(new ActionRow(tr("ankinbt.simple.add_lore"), null,
                () -> openLoreEditor("lore_add", ""), ACCENT));
        if (!lore.isEmpty()) {
            rows.add(new ActionRow(tr("ankinbt.simple.remove_last_lore"), null, this::removeLastLore));
            rows.add(new ActionRow(tr("ankinbt.simple.clear_lore"), null, this::clearLore, ERROR_C));
        }
        return rows;
    }

    private List<ActionRow> getAttributeRows() {
        List<ActionRow> rows = new ArrayList<>();
        var attrComp = editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> entries = attrComp.modifiers();

        for (int i = 0; i < entries.size(); i++) {
            int idx = i;
            var entry = entries.get(i);
            String attrId = entry.attribute().unwrapKey().map(k -> k.location().toString()).orElse("?");
            String displayName = getAttrDisplayName(attrId);
            double amount = entry.modifier().amount();
            String opName = getOpName(entry.modifier().operation());
            String slotName = getSlotDisplayName(entry.slot());
            String valueStr = String.format("%.2f %s [%s]", amount, opName, slotName);
            rows.add(new ActionRow(displayName, valueStr,
                    () -> openInlineEditor("attr_amount:" + idx, String.valueOf(amount))));
        }

        // Remove individual attribute
        if (!entries.isEmpty()) {
            for (int i = 0; i < entries.size(); i++) {
                int idx = i;
                var entry = entries.get(i);
                String attrId = entry.attribute().unwrapKey().map(k -> k.location().toString()).orElse("?");
                String displayName = getAttrDisplayName(attrId);
                rows.add(new ActionRow(tr("ankinbt.simple.remove_attr") + " " + displayName, null,
                        () -> removeAttribute(idx), ERROR_C));
            }
        }

        rows.add(new ActionRow(tr("ankinbt.simple.add_attr"), null,
                () -> activeSubEditor = new AttributePickerSubEditor(), ACCENT));

        if (!entries.isEmpty()) {
            rows.add(new ActionRow(tr("ankinbt.simple.clear_attrs"), null, this::clearAttributes, ERROR_C));
        }
        return rows;
    }

    private String getAttrDisplayName(String attrId) {
        String lang = Minecraft.getInstance().options.languageCode;
        if (lang != null && lang.startsWith("zh")) {
            String zh = ATTR_ZH.get(attrId);
            if (zh != null) return zh;
        }
        // Fallback: strip namespace and format nicely
        String name = attrId.contains(":") ? attrId.substring(attrId.indexOf(':') + 1) : attrId;
        return name.replace("generic.", "").replace("_", " ");
    }

    private String getOpName(AttributeModifier.Operation op) {
        String lang = Minecraft.getInstance().options.languageCode;
        boolean zh = lang != null && lang.startsWith("zh");
        return switch (op) {
            case ADD_VALUE -> zh ? OP_NAMES_ZH[0] : OP_NAMES_EN[0];
            case ADD_MULTIPLIED_BASE -> zh ? OP_NAMES_ZH[1] : OP_NAMES_EN[1];
            case ADD_MULTIPLIED_TOTAL -> zh ? OP_NAMES_ZH[2] : OP_NAMES_EN[2];
        };
    }

    private String getSlotDisplayName(EquipmentSlotGroup slot) {
        String name = slot.getSerializedName();
        String lang = Minecraft.getInstance().options.languageCode;
        if (lang != null && lang.startsWith("zh")) {
            String zh = SLOT_ZH.get(name);
            if (zh != null) return zh;
        }
        return name;
    }

    private void removeAttribute(int index) {
        var attrComp = editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(attrComp.modifiers());
        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
            editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, VersionCompat.get().withEntries(entries, attrComp));
            markDirty();
        }
    }

    private void clearAttributes() {
        editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        markDirty();
        setStatus(tr("ankinbt.simple.attrs_cleared"), C2);
    }

    private void addAttribute(String attrId, double amount, AttributeModifier.Operation op, EquipmentSlotGroup slot) {
        Optional<Holder.Reference<Attribute>> holder = VersionCompat.get().getAttributeHolder(attrId);
        if (holder.isEmpty()) return;

        var attrComp = editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(attrComp.modifiers());
        ResourceLocation modId = ResourceLocation.fromNamespaceAndPath("ankinbt", "custom_" + System.currentTimeMillis());
        entries.add(new ItemAttributeModifiers.Entry(holder.get(),
                new AttributeModifier(modId, amount, op), slot));
        editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, VersionCompat.get().withEntries(entries, attrComp));
        dirty = true;
        activeSubEditor = null;
        setStatus(Component.translatable("ankinbt.status.added", getAttrDisplayName(attrId)).getString(), SUCCESS);
    }

    private List<ActionRow> getVisualRows() {
        List<ActionRow> rows = new ArrayList<>();
        rows.add(new ActionRow(tr("ankinbt.simple.custom_model_data"), String.valueOf(getCustomModelData()),
                () -> openInlineEditor("custom_model_data", String.valueOf(getCustomModelData()))));
        rows.add(new ActionRow(tr("ankinbt.simple.enchant_glint"),
                hasEnchantGlint() ? tr("ankinbt.simple.on") : tr("ankinbt.simple.off"),
                this::toggleEnchantGlint));
        if (VersionCompat.get().hasHideTooltipFeature()) {
            rows.add(new ActionRow(tr("ankinbt.simple.hide_tooltip"),
                    isHideTooltip() ? tr("ankinbt.simple.on") : tr("ankinbt.simple.off"),
                    this::toggleHideTooltip));
        }
        if (VersionCompat.get().hasHideAdditionalFeature()) {
            rows.add(new ActionRow(tr("ankinbt.simple.hide_additional"),
                    isHideAdditional() ? tr("ankinbt.simple.on") : tr("ankinbt.simple.off"),
                    this::toggleHideAdditional));
        }

        // Dye color for leather armor
        var dyeColor = editStack.get(DataComponents.DYED_COLOR);
        if (dyeColor != null || isLeatherArmor()) {
            int color = dyeColor != null ? dyeColor.rgb() : 0xA06540;
            String hex = String.format("#%06X", color & 0xFFFFFF);
            rows.add(new ActionRow(tr("ankinbt.simple.dye_color"), hex,
                    () -> openInlineEditor("dye_color", hex)));
            rows.add(new ActionRow(tr("ankinbt.simple.dye_color_picker"), null,
                    () -> activeSubEditor = new ColorPickerSubEditor(color), ACCENT));
        }

        // Custom name color
        rows.add(new ActionRow(tr("ankinbt.simple.name_color"), null,
                () -> activeSubEditor = new ColorPickerSubEditor(-2), ACCENT));

        return rows;
    }

    private List<ActionRow> getMiscRows() {
        List<ActionRow> rows = new ArrayList<>();
        rows.add(new ActionRow(tr("ankinbt.simple.copy_nbt"), null, this::copyNbtToClipboard));
        rows.add(new ActionRow(tr("ankinbt.simple.copy_give_cmd"), null, this::copyGiveCommand));
        rows.add(new ActionRow(tr("ankinbt.simple.reset"), null, this::resetItem, ERROR_C));
        return rows;
    }

    // ==================== ITEM OPERATIONS ====================

    private boolean isUnbreakable() { return editStack.has(DataComponents.UNBREAKABLE); }

    private void toggleUnbreakable() {
        VersionCompat.get().setUnbreakable(editStack, !isUnbreakable());
        markDirty();
    }

    private boolean isFireResistant() { return VersionCompat.get().isFireResistant(editStack); }

    private void toggleFireResistant() {
        VersionCompat.get().setFireResistant(editStack, !isFireResistant());
        markDirty();
    }

    private int getRepairCost() {
        Integer c = editStack.get(DataComponents.REPAIR_COST);
        return c != null ? c : 0;
    }

    private int getCustomModelData() {
        return VersionCompat.get().getCustomModelData(editStack);
    }

    private boolean hasEnchantGlint() {
        Boolean g = editStack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        return g != null && g;
    }

    private void toggleEnchantGlint() {
        Boolean cur = editStack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (cur != null && cur) editStack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        else editStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        markDirty();
    }

    private boolean isHideTooltip() { return VersionCompat.get().isHideTooltip(editStack); }

    private void toggleHideTooltip() {
        VersionCompat.get().setHideTooltip(editStack, !isHideTooltip());
        markDirty();
    }

    private boolean isHideAdditional() { return VersionCompat.get().isHideAdditional(editStack); }

    private void toggleHideAdditional() {
        VersionCompat.get().setHideAdditional(editStack, !isHideAdditional());
        markDirty();
    }

    private boolean isLeatherArmor() {
        String id = editStack.getItem().builtInRegistryHolder().key().location().toString();
        return id.contains("leather_");
    }

    private void cycleRarity() {
        var cur = editStack.get(DataComponents.RARITY);
        if (cur == null) cur = net.minecraft.world.item.Rarity.COMMON;
        var next = switch (cur) {
            case COMMON -> net.minecraft.world.item.Rarity.UNCOMMON;
            case UNCOMMON -> net.minecraft.world.item.Rarity.RARE;
            case RARE -> net.minecraft.world.item.Rarity.EPIC;
            case EPIC -> net.minecraft.world.item.Rarity.COMMON;
        };
        editStack.set(DataComponents.RARITY, next);
        markDirty();
    }

    private List<Component> getLore() {
        var lc = editStack.get(DataComponents.LORE);
        return lc == null ? List.of() : lc.lines();
    }

    /** Get raw text for lore line, preserving section signs as & for editing */
    private String getLoreRawText(int idx) {
        List<Component> lore = getLore();
        if (idx < 0 || idx >= lore.size()) return "";
        // Try to reconstruct the raw text with & codes from the Component
        return componentToColorCoded(lore.get(idx));
    }

    /** Convert a Component back to &-coded string for editing */
    private String componentToColorCoded(Component comp) {
        StringBuilder sb = new StringBuilder();
        comp.visit((style, text) -> {
            TextColor color = style.getColor();
            if (color != null) {
                // Try to match to MC color code
                int rgb = color.getValue();
                boolean found = false;
                for (int i = 0; i < MC_COLORS.length; i++) {
                    if ((MC_COLORS[i] & 0xFFFFFF) == (rgb & 0xFFFFFF)) {
                        sb.append('&').append(MC_COLOR_CODES[i]);
                        found = true;
                        break;
                    }
                }
                if (!found) sb.append("&#").append(String.format("%06x", rgb & 0xFFFFFF));
            }
            if (style.isBold()) sb.append("&l");
            if (style.isItalic()) sb.append("&o");
            if (style.isUnderlined()) sb.append("&n");
            if (style.isStrikethrough()) sb.append("&m");
            if (style.isObfuscated()) sb.append("&k");
            sb.append(text);
            return Optional.empty();
        }, Style.EMPTY);
        return sb.toString();
    }

    /** Convert &-coded string to Component with proper formatting */
    static Component colorCodedToComponent(String input) {
        // Replace & with section sign, then parse
        String processed = input;
        MutableComponent result = Component.empty();
        int i = 0;
        Style currentStyle = Style.EMPTY;

        while (i < processed.length()) {
            if (processed.charAt(i) == '&' && i + 1 < processed.length()) {
                char code = processed.charAt(i + 1);
                // Hex color: &#RRGGBB
                if (code == '#' && i + 8 <= processed.length()) {
                    try {
                        String hex = processed.substring(i + 2, i + 8);
                        int rgb = Integer.parseInt(hex, 16);
                        currentStyle = Style.EMPTY.withColor(TextColor.fromRgb(rgb));
                        i += 8;
                        continue;
                    } catch (Exception ignored) {}
                }
                Style newStyle = applyColorCode(currentStyle, code);
                if (newStyle != null) {
                    currentStyle = newStyle;
                    i += 2;
                    continue;
                }
            }
            // Also handle actual section sign
            if (processed.charAt(i) == SECTION && i + 1 < processed.length()) {
                char code = processed.charAt(i + 1);
                Style newStyle = applyColorCode(currentStyle, code);
                if (newStyle != null) {
                    currentStyle = newStyle;
                    i += 2;
                    continue;
                }
            }
            // Collect plain text until next code
            int start = i;
            while (i < processed.length() && processed.charAt(i) != '&' && processed.charAt(i) != SECTION) i++;
            if (i > start) {
                result.append(Component.literal(processed.substring(start, i)).withStyle(currentStyle));
            }
        }
        return result;
    }

    private static Style applyColorCode(Style style, char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> Style.EMPTY.withColor(TextColor.fromRgb(0x000000));
            case '1' -> Style.EMPTY.withColor(TextColor.fromRgb(0x0000AA));
            case '2' -> Style.EMPTY.withColor(TextColor.fromRgb(0x00AA00));
            case '3' -> Style.EMPTY.withColor(TextColor.fromRgb(0x00AAAA));
            case '4' -> Style.EMPTY.withColor(TextColor.fromRgb(0xAA0000));
            case '5' -> Style.EMPTY.withColor(TextColor.fromRgb(0xAA00AA));
            case '6' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFFAA00));
            case '7' -> Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA));
            case '8' -> Style.EMPTY.withColor(TextColor.fromRgb(0x555555));
            case '9' -> Style.EMPTY.withColor(TextColor.fromRgb(0x5555FF));
            case 'a' -> Style.EMPTY.withColor(TextColor.fromRgb(0x55FF55));
            case 'b' -> Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF));
            case 'c' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555));
            case 'd' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFF55FF));
            case 'e' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF55));
            case 'f' -> Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF));
            case 'k' -> style.withObfuscated(true);
            case 'l' -> style.withBold(true);
            case 'm' -> style.withStrikethrough(true);
            case 'n' -> style.withUnderlined(true);
            case 'o' -> style.withItalic(true);
            case 'r' -> Style.EMPTY;
            default -> null;
        };
    }

    private void setLore(List<Component> lines) {
        editStack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lines));
        dirty = true;
    }

    private void removeLastLore() {
        List<Component> lore = new ArrayList<>(getLore());
        if (!lore.isEmpty()) { lore.remove(lore.size() - 1); setLore(lore); setStatus(tr("ankinbt.status.deleted"), C2); }
    }

    private void clearLore() {
        editStack.remove(DataComponents.LORE); dirty = true;
        setStatus(tr("ankinbt.simple.lore_cleared"), C2);
    }

    private void clearEnchantments() {
        editStack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY); dirty = true;
        setStatus(tr("ankinbt.simple.enchants_cleared"), C2);
    }

    private void copyNbtToClipboard() {
        var opt = NbtHelper.serializeItemStack(editStack);
        if (opt.isPresent()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(opt.get().toString());
            setStatus(tr("ankinbt.simple.nbt_copied"), SUCCESS);
        }
    }

    private void copyGiveCommand() {
        var opt = NbtHelper.serializeItemStack(editStack);
        if (opt.isPresent()) {
            String id = editStack.getItem().builtInRegistryHolder().key().location().toString();
            String cmd = "/give @s " + id + " " + editStack.getCount();
            Minecraft.getInstance().keyboardHandler.setClipboard(cmd);
            setStatus(tr("ankinbt.simple.cmd_copied"), SUCCESS);
        }
    }

    private void resetItem() {
        editStack = originalStack.copy(); dirty = false;
        setStatus(tr("ankinbt.simple.reset_done"), C2);
    }

    private String getEnchantDisplayName(String enchId) {
        // Check if Chinese locale
        String lang = Minecraft.getInstance().options.languageCode;
        if (lang != null && lang.startsWith("zh")) {
            String zh = ENCHANT_ZH.get(enchId);
            if (zh != null) return zh + " (" + enchId.replace("minecraft:", "") + ")";
        }
        return enchId;
    }

    // ==================== INLINE EDITOR ====================

    private void openInlineEditor(String field, String currentValue) {
        activeSubEditor = new InlineFieldEditor(field, currentValue, false);
    }

    private void openLoreEditor(String field, String currentValue) {
        activeSubEditor = new InlineFieldEditor(field, currentValue, true);
    }

    private void applyInlineEdit(String field, String value, boolean isLore) {
        try {
            if (field.equals("rename")) {
                if (value.contains("&") || value.contains(String.valueOf(SECTION))) {
                    editStack.set(DataComponents.CUSTOM_NAME, colorCodedToComponent(value));
                } else {
                    editStack.set(DataComponents.CUSTOM_NAME, Component.literal(value));
                }
            } else if (field.equals("count")) {
                editStack.setCount(Math.max(1, Math.min(99, Integer.parseInt(value))));
            } else if (field.equals("damage")) {
                editStack.setDamageValue(Math.max(0, Integer.parseInt(value)));
            } else if (field.equals("max_damage")) {
                editStack.set(DataComponents.MAX_DAMAGE, Math.max(1, Integer.parseInt(value)));
            } else if (field.equals("max_stack")) {
                editStack.set(DataComponents.MAX_STACK_SIZE, Math.max(1, Math.min(99, Integer.parseInt(value))));
            } else if (field.equals("repair_cost")) {
                editStack.set(DataComponents.REPAIR_COST, Math.max(0, Integer.parseInt(value)));
            } else if (field.equals("custom_model_data")) {
                VersionCompat.get().setCustomModelData(editStack, Integer.parseInt(value));
            } else if (field.equals("dye_color")) {
                String hex = value.startsWith("#") ? value.substring(1) : value;
                int rgb = Integer.parseInt(hex, 16);
                VersionCompat.get().setDyedColor(editStack, rgb);
            } else if (field.equals("food_nutrition")) {
                VersionCompat.get().setFoodNutrition(editStack, Integer.parseInt(value));
            } else if (field.equals("food_saturation")) {
                VersionCompat.get().setFoodSaturation(editStack, Float.parseFloat(value));
            } else if (field.startsWith("lore:")) {
                int idx = Integer.parseInt(field.substring(5));
                List<Component> lore = new ArrayList<>(getLore());
                if (idx >= 0 && idx < lore.size()) {
                    lore.set(idx, isLore ? colorCodedToComponent(value) : Component.literal(value));
                    setLore(lore);
                }
            } else if (field.equals("lore_add")) {
                List<Component> lore = new ArrayList<>(getLore());
                lore.add(isLore ? colorCodedToComponent(value) : Component.literal(value));
                setLore(lore);
            } else if (field.startsWith("ench_level:")) {
                String enchId = field.substring(11);
                applyEnchantLevel(enchId, Integer.parseInt(value));
            } else if (field.startsWith("attr_amount:")) {
                int idx = Integer.parseInt(field.substring(12));
                var attrComp = editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(attrComp.modifiers());
                if (idx >= 0 && idx < entries.size()) {
                    var old = entries.get(idx);
                    double newAmount = Double.parseDouble(value);
                    entries.set(idx, new ItemAttributeModifiers.Entry(old.attribute(),
                            new AttributeModifier(old.modifier().id(), newAmount, old.modifier().operation()), old.slot()));
                    editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, VersionCompat.get().withEntries(entries, attrComp));
                }
            }
            dirty = true;
            setStatus(tr("ankinbt.status.edited"), C2);
        } catch (NumberFormatException e) {
            setStatus(tr("ankinbt.simple.invalid_number"), ERROR_C);
        }
        activeSubEditor = null;
    }

    private void applyEnchantLevel(String enchId, int level) {
        ResourceLocation loc = ResourceLocation.tryParse(enchId);
        if (loc == null) return;
        Optional<Holder.Reference<Enchantment>> holder = VersionCompat.get().getEnchantHolder(enchId);
        if (holder.isEmpty()) return;
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(editStack));
        if (level <= 0) mutable.removeIf(h -> h.unwrapKey().map(k -> k.location().equals(loc)).orElse(false));
        else mutable.set(holder.get(), level);
        editStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    private void addEnchantment(String enchId, int level) {
        applyEnchantLevel(enchId, level);
        dirty = true; activeSubEditor = null;
        setStatus(Component.translatable("ankinbt.status.added", getEnchantDisplayName(enchId)).getString(), SUCCESS);
    }

    // ==================== SAVE ====================

    private void saveToItem() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.isCreative()) { setStatus(tr("ankinbt.status.creative_only"), ERROR_C); return; }

        if (inventorySlot >= 0) {
            // Save to specific inventory slot
            mc.player.getInventory().setItem(inventorySlot, editStack.copy());
            mc.gameMode.handleCreativeModeItemAdd(editStack.copy(), inventorySlot < 9 ? 36 + inventorySlot : inventorySlot);
        } else {
            int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
            mc.player.getInventory().setItem(slot, editStack.copy());
            mc.gameMode.handleCreativeModeItemAdd(editStack.copy(), 36 + slot);
        }
        dirty = false;
        setStatus(tr("ankinbt.status.saved"), SUCCESS);
    }

    private void switchToAdvanced() {
        Minecraft.getInstance().setScreen(new NbtEditorScreen(editStack));
    }

    private void markDirty() { dirty = true; setStatus(tr("ankinbt.status.edited"), C2); }

    private void setStatus(String msg, int color) {
        statusMsg = msg; statusColor = color; statusTime = System.currentTimeMillis();
    }

    private static String tr(String key) { return Component.translatable(key).getString(); }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c); g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c); g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override public boolean isPauseScreen() { return false; }

    // ==================== INNER CLASSES ====================

    static class ActionRow {
        final String label; final String currentValue; final Runnable action; final int labelColor;
        ActionRow(String label, String currentValue, Runnable action) { this(label, currentValue, action, C1); }
        ActionRow(String label, String currentValue, Runnable action, int labelColor) {
            this.label = label; this.currentValue = currentValue; this.action = action; this.labelColor = labelColor;
        }
    }

    static class Btn {
        final int x, y, w, h; final String label; final Component tooltip; final Runnable action;
        Btn(int x, int y, int w, int h, String label, Component tooltip, Runnable action) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.label = label; this.tooltip = tooltip; this.action = action;
        }
        boolean isHover(int mx, int my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
        void render(GuiGraphics g, net.minecraft.client.gui.Font f, int mx, int my) {
            boolean hv = isHover(mx, my);
            g.fill(x, y, x + w, this.y + this.h, hv ? BTN_HOVER : BTN_BG);
            g.drawString(f, label, x + (w - f.width(label)) / 2, y + (this.h - 8) / 2, C1, false);
            if (hv && tooltip != null) VersionCompat.get().renderTooltip(g, f, tooltip, mx, my);
        }
    }

    interface SubEditor {
        void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h);
        boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h);
        boolean keyPressed(int key, int scan, int mod);
        boolean charTyped(char c, int mod);
        default boolean mouseScrolled(double sx, double sy) { return false; }
    }

    // ==================== INLINE FIELD EDITOR (with color code support) ====================

    class InlineFieldEditor implements SubEditor {
        final String field;
        String input;
        int cursor;
        String error = null;
        final boolean isLore;

        InlineFieldEditor(String field, String currentValue, boolean isLore) {
            this.field = field; this.isLore = isLore;
            this.input = currentValue != null ? currentValue : "";
            this.cursor = input.length();
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 340), dh = isLore ? 140 : 100;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);

            String title = getFieldLabel(field);
            g.drawString(font, title, dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            // Input box
            int ix = dx + 10, iy = dy + 30, iw = dw - 20, ih = 22;
            g.fill(ix, iy, ix + iw, iy + ih, 0xFF12121E);
            drawBorder(g, ix, iy, iw, ih, ACCENT);
            String disp = input;
            if (font.width(disp) > iw - 12) {
                int start = Math.max(0, cursor - 30);
                disp = ".." + input.substring(start);
            }
            g.drawString(font, disp + (System.currentTimeMillis() % 1000 < 500 ? "_" : ""), ix + 4, iy + 7, C1, false);

            // Preview for lore with color codes
            if (isLore && !input.isEmpty()) {
                Component preview = colorCodedToComponent(input);
                g.drawString(font, tr("ankinbt.simple.preview") + ": ", ix, iy + ih + 4, C3, false);
                int previewX = ix + font.width(tr("ankinbt.simple.preview") + ": ");
                g.drawString(font, preview, previewX, iy + ih + 4, C1, false);
            }

            if (error != null) g.drawString(font, error, ix, iy + ih + (isLore ? 16 : 4), ERROR_C, false);

            // Color palette button for lore
            if (isLore) {
                int palX = dx + dw - 80, palY = dy + 6;
                boolean palHover = mx >= palX && mx < palX + 70 && my >= palY && my < palY + 16;
                g.fill(palX, palY, palX + 70, palY + 16, palHover ? BTN_HOVER : BTN_BG);
                String palLabel = tr("ankinbt.simple.color_palette");
                g.drawString(font, palLabel, palX + (70 - font.width(palLabel)) / 2, palY + 4, C2, false);
            }

            // Buttons
            int by = dy + dh - 28, bw = 70, bh = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
            g.fill(cancelX, by, cancelX + bw, by + bh, ch ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, C2, false);

            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh;
            g.fill(okX, by, okX + bw, by + bh, oh ? ACCENT : 0xFF4F46E5);
            g.drawString(font, tr("ankinbt.edit.apply"), okX + (bw - font.width(tr("ankinbt.edit.apply"))) / 2, by + 6, C1, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 340), dh = isLore ? 140 : 100;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;

            // Color palette button
            if (isLore) {
                int palX = dx + dw - 80, palY = dy + 6;
                if (mx >= palX && mx < palX + 70 && my >= palY && my < palY + 16) {
                    activeSubEditor = new LoreColorInsertEditor(this);
                    return true;
                }
            }

            int by = dy + dh - 28, bw = 70, bh = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            if (mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh) { activeSubEditor = null; return true; }
            int okX = dx + dw / 2 + 6;
            if (mx >= okX && mx < okX + bw && my >= by && my < by + bh) { apply(); return true; }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { apply(); return true; }
            if (key == 259 && cursor > 0) { input = input.substring(0, cursor - 1) + input.substring(cursor); cursor--; error = null; return true; }
            if (key == 261 && cursor < input.length()) { input = input.substring(0, cursor) + input.substring(cursor + 1); error = null; return true; }
            if (key == 263 && cursor > 0) { cursor--; return true; }
            if (key == 262 && cursor < input.length()) { cursor++; return true; }
            if (key == 268) { cursor = 0; return true; }
            if (key == 269) { cursor = input.length(); return true; }
            if (key == 86 && (mod & 2) != 0) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (clip != null) { input = input.substring(0, cursor) + clip + input.substring(cursor); cursor += clip.length(); error = null; }
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c >= 32) { input = input.substring(0, cursor) + c + input.substring(cursor); cursor++; error = null; return true; }
            return false;
        }

        void insertAtCursor(String text) {
            input = input.substring(0, cursor) + text + input.substring(cursor);
            cursor += text.length();
        }

        private void apply() {
            if (input.isEmpty() && !field.equals("rename") && !field.equals("lore_add") && !field.startsWith("lore:")) {
                error = tr("ankinbt.simple.invalid_number"); return;
            }
            applyInlineEdit(field, input, isLore);
        }

        private String getFieldLabel(String f) {
            if (f.equals("rename")) return tr("ankinbt.simple.rename");
            if (f.equals("count")) return tr("ankinbt.simple.count");
            if (f.equals("damage")) return tr("ankinbt.simple.damage");
            if (f.equals("max_damage")) return tr("ankinbt.simple.max_damage");
            if (f.equals("max_stack")) return tr("ankinbt.simple.max_stack");
            if (f.equals("repair_cost")) return tr("ankinbt.simple.repair_cost");
            if (f.equals("custom_model_data")) return tr("ankinbt.simple.custom_model_data");
            if (f.equals("dye_color")) return tr("ankinbt.simple.dye_color");
            if (f.equals("lore_add")) return tr("ankinbt.simple.add_lore");
            if (f.startsWith("lore:")) return tr("ankinbt.simple.edit_lore");
            if (f.startsWith("ench_level:")) return tr("ankinbt.simple.ench_level");
            if (f.startsWith("attr_amount:")) return tr("ankinbt.simple.attr_amount");
            if (f.equals("food_nutrition")) return tr("ankinbt.simple.food_nutrition");
            if (f.equals("food_saturation")) return tr("ankinbt.simple.food_saturation");
            return f;
        }
    }

    // ==================== LORE COLOR INSERT EDITOR ====================

    class LoreColorInsertEditor implements SubEditor {
        final InlineFieldEditor parent;
        LoreColorInsertEditor(InlineFieldEditor parent) { this.parent = parent; }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 300), dh = 180;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);

            g.drawString(font, tr("ankinbt.simple.color_palette"), dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            // Color grid (4x4)
            int gridX = dx + 10, gridY = dy + 28;
            int cellW = (dw - 20) / 4, cellH = 22;
            for (int i = 0; i < 16; i++) {
                int col = i % 4, row = i / 4;
                int cx = gridX + col * cellW, cy = gridY + row * (cellH + 2);
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, hover ? (MC_COLORS[i] | 0xFF000000) : ((MC_COLORS[i] & 0x00FFFFFF) | 0xC0000000));
                // Show code and name
                String label = "&" + MC_COLOR_CODES[i];
                String lang = Minecraft.getInstance().options.languageCode;
                if (lang != null && lang.startsWith("zh")) label += " " + MC_COLOR_NAMES_ZH[i];
                g.drawString(font, label, cx + 3, cy + 7, i < 1 ? 0xFFFFFFFF : (i < 8 ? 0xFFFFFFFF : 0xFF000000), false);
            }

            // Format codes
            int fmtY = gridY + 4 * (cellH + 2) + 6;
            g.drawString(font, tr("ankinbt.simple.format_codes"), dx + 10, fmtY, C2, false);
            fmtY += 12;
            int fmtCellW = (dw - 20) / 3;
            for (int i = 0; i < MC_FORMAT_CODES.length; i++) {
                int col = i % 3, row = i / 3;
                int fx = gridX + col * fmtCellW, fy = fmtY + row * 18;
                boolean hover = mx >= fx && mx < fx + fmtCellW - 2 && my >= fy && my < fy + 16;
                g.fill(fx, fy, fx + fmtCellW - 2, fy + 16, hover ? BTN_HOVER : BTN_BG);
                String lang = Minecraft.getInstance().options.languageCode;
                String fLabel = "&" + MC_FORMAT_CODES[i] + " " + (lang != null && lang.startsWith("zh") ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i]);
                g.drawString(font, fLabel, fx + 3, fy + 4, C2, false);
            }

            // Back button
            int backY = dy + dh - 24;
            int backW = 60;
            int backX = dx + (dw - backW) / 2;
            boolean bh2 = mx >= backX && mx < backX + backW && my >= backY && my < backY + 18;
            g.fill(backX, backY, backX + backW, backY + 18, bh2 ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.simple.back"), backX + (backW - font.width(tr("ankinbt.simple.back"))) / 2, backY + 5, C2, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 300), dh = 180;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;

            // Color grid
            int gridX = dx + 10, gridY = dy + 28;
            int cellW = (dw - 20) / 4, cellH = 22;
            for (int i = 0; i < 16; i++) {
                int col = i % 4, row = i / 4;
                int cx = gridX + col * cellW, cy = gridY + row * (cellH + 2);
                if (mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH) {
                    parent.insertAtCursor("&" + MC_COLOR_CODES[i]);
                    activeSubEditor = parent;
                    return true;
                }
            }

            // Format codes
            int fmtY = gridY + 4 * (cellH + 2) + 6 + 12;
            int fmtCellW = (dw - 20) / 3;
            for (int i = 0; i < MC_FORMAT_CODES.length; i++) {
                int col = i % 3, row = i / 3;
                int fx = gridX + col * fmtCellW, fy = fmtY + row * 18;
                if (mx >= fx && mx < fx + fmtCellW - 2 && my >= fy && my < fy + 16) {
                    parent.insertAtCursor("&" + MC_FORMAT_CODES[i]);
                    activeSubEditor = parent;
                    return true;
                }
            }

            // Back button
            int backY = dy + dh - 24, backW = 60, backX = dx + (dw - backW) / 2;
            if (mx >= backX && mx < backX + backW && my >= backY && my < backY + 18) {
                activeSubEditor = parent;
                return true;
            }
            return true;
        }

        @Override public boolean keyPressed(int key, int scan, int mod) { return true; }
        @Override public boolean charTyped(char c, int mod) { return false; }
    }

    // ==================== COLOR PICKER (for dye / name color) ====================

    class ColorPickerSubEditor implements SubEditor {
        int mode; // -1 = lore insert, -2 = name color, >= 0 = dye color (initial value)
        int selectedColor;

        ColorPickerSubEditor(int initialColor) {
            this.mode = initialColor;
            this.selectedColor = initialColor >= 0 ? initialColor : 0xFFFFFF;
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 280), dh = 160;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);

            String title = mode == -2 ? tr("ankinbt.simple.name_color") : tr("ankinbt.simple.dye_color_picker");
            g.drawString(font, title, dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            // MC color grid
            int gridX = dx + 10, gridY = dy + 28;
            int cellW = (dw - 20) / 8, cellH = 20;
            for (int i = 0; i < 16; i++) {
                int col = i % 8, row = i / 8;
                int cx = gridX + col * cellW, cy = gridY + row * (cellH + 2);
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[i] | 0xFF000000);
                if (hover) drawBorder(g, cx, cy, cellW - 2, cellH, 0xFFFFFFFF);
            }

            // Preview
            int prevY = gridY + 2 * (cellH + 2) + 8;
            g.fill(dx + 10, prevY, dx + 10 + 30, prevY + 20, (selectedColor & 0xFFFFFF) | 0xFF000000);
            drawBorder(g, dx + 10, prevY, 30, 20, BORDER);
            g.drawString(font, String.format("#%06X", selectedColor & 0xFFFFFF), dx + 46, prevY + 6, C1, false);

            // Apply / Cancel
            int by = dy + dh - 28, bw = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw, by + bh2, ch ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, C2, false);

            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw, by + bh2, oh ? ACCENT : 0xFF4F46E5);
            g.drawString(font, tr("ankinbt.edit.apply"), okX + (bw - font.width(tr("ankinbt.edit.apply"))) / 2, by + 6, C1, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 280), dh = 160;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;

            int gridX = dx + 10, gridY = dy + 28;
            int cellW = (dw - 20) / 8, cellH = 20;
            for (int i = 0; i < 16; i++) {
                int col = i % 8, row = i / 8;
                int cx = gridX + col * cellW, cy = gridY + row * (cellH + 2);
                if (mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH) {
                    selectedColor = MC_COLORS[i] & 0xFFFFFF;
                    return true;
                }
            }

            int by = dy + dh - 28, bw = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            if (mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2) { activeSubEditor = null; return true; }
            int okX = dx + dw / 2 + 6;
            if (mx >= okX && mx < okX + bw && my >= by && my < by + bh2) { applyColor(); return true; }
            return true;
        }

        private void applyColor() {
            if (mode >= 0) {
                // Dye color
                VersionCompat.get().setDyedColor(editStack, selectedColor);
                dirty = true;
            } else if (mode == -2) {
                // Name color
                String name = editStack.getHoverName().getString();
                editStack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(selectedColor))));
                dirty = true;
            }
            setStatus(tr("ankinbt.status.edited"), C2);
            activeSubEditor = null;
        }

        @Override public boolean keyPressed(int key, int scan, int mod) { return true; }
        @Override public boolean charTyped(char c, int mod) { return false; }
    }

    // ==================== ENCHANT PICKER ====================

    class EnchantPickerSubEditor implements SubEditor {
        private final List<String> allEnchants = new ArrayList<>();
        private List<String> filtered = new ArrayList<>();
        private String searchQ = "";
        private int searchCursor = 0;
        private int scrollOff = 0;
        private int hoverIdx = -1;
        private int selectedIdx = -1;
        private String levelInput = "1";
        private int levelCursor = 1;
        private boolean focusLevel = false;

        EnchantPickerSubEditor() {
            allEnchants.addAll(VersionCompat.get().getAllEnchantIds());
            Collections.sort(allEnchants);
            filtered = new ArrayList<>(allEnchants);
        }

        private void filter() {
            if (searchQ.isEmpty()) { filtered = new ArrayList<>(allEnchants); }
            else {
                String q = searchQ.toLowerCase();
                filtered = allEnchants.stream().filter(s -> {
                    if (s.toLowerCase().contains(q)) return true;
                    // Also search Chinese name
                    String zh = ENCHANT_ZH.get(s);
                    return zh != null && zh.contains(q);
                }).collect(Collectors.toList());
            }
            scrollOff = 0;
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            g.drawString(font, tr("ankinbt.simple.pick_enchant"), x + 8, y + 4, C1, false);

            int sx = x + 8, sy = y + 18, sw = w - 16, sh = 18;
            g.fill(sx, sy, sx + sw, sy + sh, 0xFF12121E);
            drawBorder(g, sx, sy, sw, sh, focusLevel ? BORDER : ACCENT);
            String sd = searchQ.isEmpty() ? tr("ankinbt.search.hint") : searchQ;
            g.drawString(font, sd + (!focusLevel && System.currentTimeMillis() % 1000 < 500 ? "_" : ""),
                    sx + 4, sy + 5, searchQ.isEmpty() ? C3 : C1, false);

            int ly = sy + sh + 4;
            int listH = h - 80;
            int maxItems = listH / 16;
            hoverIdx = -1;
            int end = Math.min(scrollOff + maxItems, filtered.size());
            for (int i = scrollOff; i < end; i++) {
                int ry = ly + (i - scrollOff) * 16;
                boolean hovered = mx >= x + 8 && mx < x + w - 8 && my >= ry && my < ry + 16;
                if (hovered) hoverIdx = i;
                boolean sel = i == selectedIdx;
                if (sel) g.fill(x + 8, ry, x + w - 8, ry + 16, SELECT_BG);
                else if (hovered) g.fill(x + 8, ry, x + w - 8, ry + 16, HOVER);
                String displayName = getEnchantDisplayName(filtered.get(i));
                g.drawString(font, displayName, x + 12, ry + 4, sel ? C1 : C2, false);
            }

            int by = y + h - 30;
            g.drawString(font, tr("ankinbt.simple.level"), x + 8, by + 6, C2, false);
            int lx = x + 8 + font.width(tr("ankinbt.simple.level")) + 4;
            g.fill(lx, by + 2, lx + 40, by + 20, 0xFF12121E);
            drawBorder(g, lx, by + 2, 40, 18, focusLevel ? ACCENT : BORDER);
            g.drawString(font, levelInput + (focusLevel && System.currentTimeMillis() % 1000 < 500 ? "_" : ""), lx + 4, by + 7, C1, false);

            int confirmX = x + w - 78;
            boolean ch = mx >= confirmX && mx < confirmX + 70 && my >= by + 1 && my < by + 21;
            g.fill(confirmX, by + 1, confirmX + 70, by + 21, ch ? ACCENT : 0xFF4F46E5);
            g.drawString(font, tr("ankinbt.add.confirm"), confirmX + (70 - font.width(tr("ankinbt.add.confirm"))) / 2, by + 7, C1, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int sx = x + 8, sy = y + 18, sw = w - 16, sh = 18;
            if (mx >= sx && mx < sx + sw && my >= sy && my < sy + sh) { focusLevel = false; return true; }

            int by = y + h - 30;
            int lx = x + 8 + font.width(tr("ankinbt.simple.level")) + 4;
            if (mx >= lx && mx < lx + 40 && my >= by + 2 && my < by + 20) { focusLevel = true; return true; }

            int confirmX = x + w - 78;
            if (mx >= confirmX && mx < confirmX + 70 && my >= by + 1 && my < by + 21) { confirm(); return true; }

            if (hoverIdx >= 0 && hoverIdx < filtered.size()) { selectedIdx = hoverIdx; return true; }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { confirm(); return true; }
            if (key == 258) { focusLevel = !focusLevel; return true; }
            if (focusLevel) {
                if (key == 259 && levelCursor > 0 && !levelInput.isEmpty()) {
                    levelInput = levelInput.substring(0, levelCursor - 1) + levelInput.substring(levelCursor); levelCursor--; return true;
                }
                if (key == 263 && levelCursor > 0) { levelCursor--; return true; }
                if (key == 262 && levelCursor < levelInput.length()) { levelCursor++; return true; }
            } else {
                if (key == 259 && searchCursor > 0 && !searchQ.isEmpty()) {
                    searchQ = searchQ.substring(0, searchCursor - 1) + searchQ.substring(searchCursor); searchCursor--; filter(); return true;
                }
                if (key == 263 && searchCursor > 0) { searchCursor--; return true; }
                if (key == 262 && searchCursor < searchQ.length()) { searchCursor++; return true; }
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c >= 32) {
                if (focusLevel) {
                    if (c >= '0' && c <= '9') { levelInput = levelInput.substring(0, levelCursor) + c + levelInput.substring(levelCursor); levelCursor++; }
                } else {
                    searchQ = searchQ.substring(0, searchCursor) + c + searchQ.substring(searchCursor); searchCursor++; filter();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            scrollOff -= (int) sy * 3;
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, filtered.size() - 10)));
            return true;
        }

        private void confirm() {
            if (selectedIdx < 0 || selectedIdx >= filtered.size()) {
                setStatus(tr("ankinbt.simple.select_enchant_first"), ERROR_C); return;
            }
            try {
                int level = Integer.parseInt(levelInput);
                if (level < 1) level = 1;
                addEnchantment(filtered.get(selectedIdx), level);
            } catch (NumberFormatException e) {
                setStatus(tr("ankinbt.simple.invalid_number"), ERROR_C);
            }
        }
    }

    // ==================== ATTRIBUTE PICKER ====================

    class AttributePickerSubEditor implements SubEditor {
        private final List<String> allAttrs = new ArrayList<>();
        private List<String> filtered = new ArrayList<>();
        private String searchQ = "";
        private int searchCursor = 0;
        private int scrollOff = 0;
        private int hoverIdx = -1;
        private int selectedIdx = -1;
        private String amountInput = "1.0";
        private int amountCursor = 3;
        private int focusField = 0; // 0=search, 1=amount
        private int selectedOp = 0; // 0=ADD_VALUE, 1=ADD_MULTIPLIED_BASE, 2=ADD_MULTIPLIED_TOTAL
        private int selectedSlot = 0; // index into SLOT_KEYS

        private static final String[] SLOT_KEYS = { "any", "mainhand", "offhand", "head", "chest", "legs", "feet", "hand", "armor" };

        AttributePickerSubEditor() {
            allAttrs.addAll(VersionCompat.get().getAllAttributeIds());
            Collections.sort(allAttrs);
            filtered = new ArrayList<>(allAttrs);
        }

        private void filter() {
            if (searchQ.isEmpty()) { filtered = new ArrayList<>(allAttrs); }
            else {
                String q = searchQ.toLowerCase();
                filtered = allAttrs.stream().filter(s -> {
                    if (s.toLowerCase().contains(q)) return true;
                    String zh = ATTR_ZH.get(s);
                    return zh != null && zh.contains(q);
                }).collect(Collectors.toList());
            }
            scrollOff = 0; selectedIdx = -1;
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            // Title
            g.drawString(font, tr("ankinbt.simple.pick_attr"), x + 8, y + 4, C1, false);

            // Search box
            int sx = x + 8, sy = y + 18, sw = w - 16, sh = 18;
            g.fill(sx, sy, sx + sw, sy + sh, 0xFF12121E);
            drawBorder(g, sx, sy, sw, sh, focusField == 0 ? ACCENT : BORDER);
            String sd = searchQ.isEmpty() ? tr("ankinbt.search.hint") : searchQ;
            g.drawString(font, sd + (focusField == 0 && System.currentTimeMillis() % 1000 < 500 ? "_" : ""),
                    sx + 4, sy + 5, searchQ.isEmpty() ? C3 : C1, false);

            // Attribute list
            int ly = sy + sh + 4;
            int listH = h - 140;
            int maxItems = listH / 16;
            hoverIdx = -1;
            int end = Math.min(scrollOff + maxItems, filtered.size());
            for (int i = scrollOff; i < end; i++) {
                int ry = ly + (i - scrollOff) * 16;
                boolean hovered = mx >= x + 8 && mx < x + w - 8 && my >= ry && my < ry + 16;
                if (hovered) hoverIdx = i;
                boolean sel = i == selectedIdx;
                if (sel) g.fill(x + 8, ry, x + w - 8, ry + 16, SELECT_BG);
                else if (hovered) g.fill(x + 8, ry, x + w - 8, ry + 16, HOVER);
                String displayName = getAttrDisplayName(filtered.get(i));
                g.drawString(font, displayName, x + 12, ry + 4, sel ? C1 : C2, false);
            }

            // Bottom controls area
            int bottomY = y + h - 90;

            // Amount input
            g.drawString(font, tr("ankinbt.simple.attr_amount"), x + 8, bottomY + 4, C2, false);
            int ax = x + 8 + font.width(tr("ankinbt.simple.attr_amount")) + 4;
            int aw = 80;
            g.fill(ax, bottomY, ax + aw, bottomY + 18, 0xFF12121E);
            drawBorder(g, ax, bottomY, aw, 18, focusField == 1 ? ACCENT : BORDER);
            g.drawString(font, amountInput + (focusField == 1 && System.currentTimeMillis() % 1000 < 500 ? "_" : ""),
                    ax + 4, bottomY + 5, C1, false);

            // Operation selector
            int opY = bottomY + 22;
            g.drawString(font, tr("ankinbt.simple.attr_operation"), x + 8, opY + 4, C2, false);
            int opX = x + 8 + font.width(tr("ankinbt.simple.attr_operation")) + 4;
            String[] opLabels = isZh() ? OP_NAMES_ZH : OP_NAMES_EN;
            for (int i = 0; i < 3; i++) {
                int bw = font.width(opLabels[i]) + 10;
                boolean hover = mx >= opX && mx < opX + bw && my >= opY && my < opY + 18;
                boolean active = i == selectedOp;
                g.fill(opX, opY, opX + bw, opY + 18, active ? ACCENT : (hover ? BTN_HOVER : BTN_BG));
                g.drawString(font, opLabels[i], opX + 5, opY + 5, active ? C1 : C2, false);
                opX += bw + 4;
            }

            // Slot selector
            int slotY = opY + 22;
            g.drawString(font, tr("ankinbt.simple.attr_slot"), x + 8, slotY + 4, C2, false);
            int slotX = x + 8 + font.width(tr("ankinbt.simple.attr_slot")) + 4;
            for (int i = 0; i < SLOT_KEYS.length; i++) {
                String slotLabel = isZh() ? SLOT_ZH.getOrDefault(SLOT_KEYS[i], SLOT_KEYS[i]) : SLOT_KEYS[i];
                int bw = font.width(slotLabel) + 8;
                boolean hover = mx >= slotX && mx < slotX + bw && my >= slotY && my < slotY + 18;
                boolean active = i == selectedSlot;
                g.fill(slotX, slotY, slotX + bw, slotY + 18, active ? ACCENT : (hover ? BTN_HOVER : BTN_BG));
                g.drawString(font, slotLabel, slotX + 4, slotY + 5, active ? C1 : C2, false);
                slotX += bw + 3;
                // Wrap to next line if too wide
                if (slotX > x + w - 40 && i < SLOT_KEYS.length - 1) {
                    slotX = x + 8 + font.width(tr("ankinbt.simple.attr_slot")) + 4;
                    slotY += 20;
                }
            }

            // Confirm button
            int confirmY = y + h - 24;
            int confirmX = x + w - 78;
            boolean ch = mx >= confirmX && mx < confirmX + 70 && my >= confirmY && my < confirmY + 20;
            g.fill(confirmX, confirmY, confirmX + 70, confirmY + 20, ch ? ACCENT : 0xFF4F46E5);
            g.drawString(font, tr("ankinbt.add.confirm"), confirmX + (70 - font.width(tr("ankinbt.add.confirm"))) / 2, confirmY + 6, C1, false);
        }

        private boolean isZh() {
            String lang = Minecraft.getInstance().options.languageCode;
            return lang != null && lang.startsWith("zh");
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            // Search box focus
            int sx = x + 8, sy = y + 18, sw = w - 16, sh = 18;
            if (mx >= sx && mx < sx + sw && my >= sy && my < sy + sh) { focusField = 0; return true; }

            // Amount box focus
            int bottomY = y + h - 90;
            int ax = x + 8 + font.width(tr("ankinbt.simple.attr_amount")) + 4;
            if (mx >= ax && mx < ax + 80 && my >= bottomY && my < bottomY + 18) { focusField = 1; return true; }

            // Operation buttons
            int opY = bottomY + 22;
            int opX = x + 8 + font.width(tr("ankinbt.simple.attr_operation")) + 4;
            String[] opLabels = isZh() ? OP_NAMES_ZH : OP_NAMES_EN;
            for (int i = 0; i < 3; i++) {
                int bw = font.width(opLabels[i]) + 10;
                if (mx >= opX && mx < opX + bw && my >= opY && my < opY + 18) { selectedOp = i; return true; }
                opX += bw + 4;
            }

            // Slot buttons
            int slotY = opY + 22;
            int slotX = x + 8 + font.width(tr("ankinbt.simple.attr_slot")) + 4;
            for (int i = 0; i < SLOT_KEYS.length; i++) {
                String slotLabel = isZh() ? SLOT_ZH.getOrDefault(SLOT_KEYS[i], SLOT_KEYS[i]) : SLOT_KEYS[i];
                int bw = font.width(slotLabel) + 8;
                if (mx >= slotX && mx < slotX + bw && my >= slotY && my < slotY + 18) { selectedSlot = i; return true; }
                slotX += bw + 3;
                if (slotX > x + w - 40 && i < SLOT_KEYS.length - 1) {
                    slotX = x + 8 + font.width(tr("ankinbt.simple.attr_slot")) + 4;
                    slotY += 20;
                }
            }

            // Confirm button
            int confirmY = y + h - 24;
            int confirmX = x + w - 78;
            if (mx >= confirmX && mx < confirmX + 70 && my >= confirmY && my < confirmY + 20) { confirm(); return true; }

            // List selection
            if (hoverIdx >= 0 && hoverIdx < filtered.size()) { selectedIdx = hoverIdx; return true; }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { confirm(); return true; }
            if (key == 258) { focusField = (focusField + 1) % 2; return true; } // Tab
            if (focusField == 1) {
                if (key == 259 && amountCursor > 0 && !amountInput.isEmpty()) {
                    amountInput = amountInput.substring(0, amountCursor - 1) + amountInput.substring(amountCursor); amountCursor--; return true;
                }
                if (key == 263 && amountCursor > 0) { amountCursor--; return true; }
                if (key == 262 && amountCursor < amountInput.length()) { amountCursor++; return true; }
            } else {
                if (key == 259 && searchCursor > 0 && !searchQ.isEmpty()) {
                    searchQ = searchQ.substring(0, searchCursor - 1) + searchQ.substring(searchCursor); searchCursor--; filter(); return true;
                }
                if (key == 263 && searchCursor > 0) { searchCursor--; return true; }
                if (key == 262 && searchCursor < searchQ.length()) { searchCursor++; return true; }
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c >= 32) {
                if (focusField == 1) {
                    if ((c >= '0' && c <= '9') || c == '.' || c == '-') {
                        amountInput = amountInput.substring(0, amountCursor) + c + amountInput.substring(amountCursor); amountCursor++;
                    }
                } else {
                    searchQ = searchQ.substring(0, searchCursor) + c + searchQ.substring(searchCursor); searchCursor++; filter();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            scrollOff -= (int) sy * 3;
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, filtered.size() - 10)));
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
            if (selectedIdx < 0 || selectedIdx >= filtered.size()) {
                setStatus(tr("ankinbt.simple.select_attr_first"), ERROR_C); return;
            }
            try {
                double amount = Double.parseDouble(amountInput);
                addAttribute(filtered.get(selectedIdx), amount, opFromIndex(selectedOp), slotFromKey(SLOT_KEYS[selectedSlot]));
            } catch (NumberFormatException e) {
                setStatus(tr("ankinbt.simple.invalid_number"), ERROR_C);
            }
        }
    }
}
