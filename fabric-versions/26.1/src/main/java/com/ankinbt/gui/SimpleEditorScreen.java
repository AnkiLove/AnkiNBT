package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtFileIO;
import com.ankinbt.util.ItemEditorVisuals;
import com.ankinbt.util.UiSound;
import com.ankinbt.util.FlatEditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.ankinbt.compat.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import java.lang.reflect.Method;
import java.nio.file.Path;
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
    private static final int POTION_MAX_DURATION = 1_000_000;
    private static final int POTION_MAX_AMPLIFIER = 255;
    private static final int POTION_MAX_CUSTOM_EFFECTS = 32;

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

    private static final String[] POTION_IDS = {
            "minecraft:water", "minecraft:awkward", "minecraft:mundane", "minecraft:thick",
            "minecraft:healing", "minecraft:strong_healing", "minecraft:harming", "minecraft:strong_harming",
            "minecraft:regeneration", "minecraft:long_regeneration", "minecraft:strong_regeneration",
            "minecraft:swiftness", "minecraft:long_swiftness", "minecraft:strong_swiftness",
            "minecraft:slowness", "minecraft:long_slowness", "minecraft:strong_slowness",
            "minecraft:strength", "minecraft:long_strength", "minecraft:strong_strength",
            "minecraft:leaping", "minecraft:long_leaping", "minecraft:strong_leaping",
            "minecraft:poison", "minecraft:long_poison", "minecraft:strong_poison",
            "minecraft:fire_resistance", "minecraft:long_fire_resistance",
            "minecraft:water_breathing", "minecraft:long_water_breathing",
            "minecraft:night_vision", "minecraft:long_night_vision",
            "minecraft:invisibility", "minecraft:long_invisibility",
            "minecraft:slow_falling", "minecraft:long_slow_falling",
            "minecraft:turtle_master", "minecraft:long_turtle_master", "minecraft:strong_turtle_master",
            "minecraft:weakness", "minecraft:long_weakness", "minecraft:luck"
    };
    private static final String[] EFFECT_IDS = {
            "minecraft:speed", "minecraft:slowness", "minecraft:haste", "minecraft:mining_fatigue",
            "minecraft:strength", "minecraft:instant_health", "minecraft:instant_damage", "minecraft:jump_boost",
            "minecraft:nausea", "minecraft:regeneration", "minecraft:resistance", "minecraft:fire_resistance",
            "minecraft:water_breathing", "minecraft:invisibility", "minecraft:blindness", "minecraft:night_vision",
            "minecraft:hunger", "minecraft:weakness", "minecraft:poison", "minecraft:wither",
            "minecraft:health_boost", "minecraft:absorption", "minecraft:saturation", "minecraft:glowing",
            "minecraft:levitation", "minecraft:luck", "minecraft:unluck", "minecraft:slow_falling",
            "minecraft:conduit_power", "minecraft:dolphins_grace", "minecraft:bad_omen", "minecraft:hero_of_the_village",
            "minecraft:darkness", "minecraft:trial_omen", "minecraft:raid_omen", "minecraft:wind_charged",
            "minecraft:weaving", "minecraft:oozing", "minecraft:infested"
    };

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
        ATTR_ZH.put("minecraft:max_health", "最大生命值");
        ATTR_ZH.put("minecraft:generic.follow_range", "跟随范围");
        ATTR_ZH.put("minecraft:follow_range", "跟随范围");
        ATTR_ZH.put("minecraft:generic.knockback_resistance", "击退抗性");
        ATTR_ZH.put("minecraft:knockback_resistance", "击退抗性");
        ATTR_ZH.put("minecraft:generic.movement_speed", "移动速度");
        ATTR_ZH.put("minecraft:movement_speed", "移动速度");
        ATTR_ZH.put("minecraft:generic.flying_speed", "飞行速度");
        ATTR_ZH.put("minecraft:flying_speed", "飞行速度");
        ATTR_ZH.put("minecraft:generic.attack_damage", "攻击伤害");
        ATTR_ZH.put("minecraft:attack_damage", "攻击伤害");
        ATTR_ZH.put("minecraft:generic.attack_knockback", "攻击击退");
        ATTR_ZH.put("minecraft:attack_knockback", "攻击击退");
        ATTR_ZH.put("minecraft:generic.attack_speed", "攻击速度");
        ATTR_ZH.put("minecraft:attack_speed", "攻击速度");
        ATTR_ZH.put("minecraft:generic.armor", "护甲值");
        ATTR_ZH.put("minecraft:armor", "护甲值");
        ATTR_ZH.put("minecraft:generic.armor_toughness", "护甲韧性");
        ATTR_ZH.put("minecraft:armor_toughness", "护甲韧性");
        ATTR_ZH.put("minecraft:generic.luck", "幸运值");
        ATTR_ZH.put("minecraft:luck", "幸运值");
        ATTR_ZH.put("minecraft:generic.max_absorption", "最大吸收");
        ATTR_ZH.put("minecraft:max_absorption", "最大吸收");
        ATTR_ZH.put("minecraft:generic.scale", "缩放");
        ATTR_ZH.put("minecraft:scale", "缩放");
        ATTR_ZH.put("minecraft:generic.step_height", "台阶高度");
        ATTR_ZH.put("minecraft:step_height", "台阶高度");
        ATTR_ZH.put("minecraft:generic.gravity", "重力");
        ATTR_ZH.put("minecraft:gravity", "重力");
        ATTR_ZH.put("minecraft:generic.safe_fall_distance", "安全坠落距离");
        ATTR_ZH.put("minecraft:safe_fall_distance", "安全坠落距离");
        ATTR_ZH.put("minecraft:generic.fall_damage_multiplier", "坠落伤害倍率");
        ATTR_ZH.put("minecraft:fall_damage_multiplier", "坠落伤害倍率");
        ATTR_ZH.put("minecraft:generic.jump_strength", "跳跃力量");
        ATTR_ZH.put("minecraft:horse.jump_strength", "跳跃力量");
        ATTR_ZH.put("minecraft:jump_strength", "跳跃力量");
        ATTR_ZH.put("minecraft:generic.block_interaction_range", "方块交互距离");
        ATTR_ZH.put("minecraft:player.block_interaction_range", "方块交互距离");
        ATTR_ZH.put("minecraft:block_interaction_range", "方块交互距离");
        ATTR_ZH.put("minecraft:generic.entity_interaction_range", "实体交互距离");
        ATTR_ZH.put("minecraft:player.entity_interaction_range", "实体交互距离");
        ATTR_ZH.put("minecraft:entity_interaction_range", "实体交互距离");
        ATTR_ZH.put("minecraft:generic.block_break_speed", "方块破坏速度");
        ATTR_ZH.put("minecraft:player.block_break_speed", "方块破坏速度");
        ATTR_ZH.put("minecraft:block_break_speed", "方块破坏速度");
        ATTR_ZH.put("minecraft:generic.mining_efficiency", "挖掘效率");
        ATTR_ZH.put("minecraft:player.mining_efficiency", "挖掘效率");
        ATTR_ZH.put("minecraft:mining_efficiency", "挖掘效率");
        ATTR_ZH.put("minecraft:generic.sneaking_speed", "潜行速度");
        ATTR_ZH.put("minecraft:player.sneaking_speed", "潜行速度");
        ATTR_ZH.put("minecraft:sneaking_speed", "潜行速度");
        ATTR_ZH.put("minecraft:generic.submerged_mining_speed", "水下挖掘速度");
        ATTR_ZH.put("minecraft:player.submerged_mining_speed", "水下挖掘速度");
        ATTR_ZH.put("minecraft:submerged_mining_speed", "水下挖掘速度");
        ATTR_ZH.put("minecraft:generic.sweeping_damage_ratio", "横扫伤害比");
        ATTR_ZH.put("minecraft:player.sweeping_damage_ratio", "横扫伤害比");
        ATTR_ZH.put("minecraft:sweeping_damage_ratio", "横扫伤害比");
        ATTR_ZH.put("minecraft:burning_time", "燃烧时间");
        ATTR_ZH.put("minecraft:explosion_knockback_resistance", "爆炸击退抗性");
        ATTR_ZH.put("minecraft:movement_efficiency", "移动效率");
        ATTR_ZH.put("minecraft:oxygen_bonus", "氧气加成");
        ATTR_ZH.put("minecraft:water_movement_efficiency", "水中移动效率");
        ATTR_ZH.put("minecraft:tempt_range", "引诱范围");
        ATTR_ZH.put("minecraft:zombie.spawn_reinforcements", "僵尸增援概率");
    }

    private static final Map<String, String> ATTR_NOTES_ZH = new LinkedHashMap<>();
    private static final Map<String, String> ATTR_NOTES_EN = new LinkedHashMap<>();
    static {
        putAttrNote("max_health", "决定实体最大生命值。", "Controls maximum health.");
        putAttrNote("movement_speed", "决定地面移动速度。", "Controls ground movement speed.");
        putAttrNote("attack_damage", "决定近战基础伤害。", "Controls base melee damage.");
        putAttrNote("attack_speed", "决定攻击冷却恢复速度。", "Controls attack cooldown speed.");
        putAttrNote("armor", "提高护甲减伤。", "Adds armor damage reduction.");
        putAttrNote("armor_toughness", "降低高伤害攻击对护甲的穿透。", "Reduces armor penetration from high damage.");
        putAttrNote("luck", "影响带 quality 或 bonus_rolls 的战利品表；原版最明显是钓鱼，值越高越容易出高质量结果。", "Affects loot tables using quality or bonus_rolls. In vanilla it is most visible in fishing.");
        putAttrNote("block_interaction_range", "玩家可交互方块的距离。", "Player block interaction reach.");
        putAttrNote("entity_interaction_range", "玩家可交互实体的距离。", "Player entity interaction reach.");
        putAttrNote("block_break_speed", "玩家破坏方块的基础速度。", "Player base block breaking speed.");
        putAttrNote("sneaking_speed", "玩家潜行时移动速度。", "Player movement speed while sneaking.");
        putAttrNote("submerged_mining_speed", "玩家在水下挖掘速度。", "Player mining speed while submerged.");
        putAttrNote("scale", "改变实体显示尺寸。", "Changes entity display scale.");
        putAttrNote("gravity", "改变实体受到的重力。", "Changes gravity applied to the entity.");
        putAttrNote("safe_fall_distance", "开始计算坠落伤害前的安全距离。", "Safe distance before fall damage starts.");
        putAttrNote("fall_damage_multiplier", "改变坠落伤害倍率。", "Changes fall damage multiplier.");
        putAttrNote("jump_strength", "改变跳跃力度。", "Changes jump strength.");
        putAttrNote("knockback_resistance", "降低受到击退的幅度。", "Reduces received knockback.");
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

    private static void putAttrNote(String id, String zh, String en) {
        for (String key : attrNoteKeys(id)) {
            ATTR_NOTES_ZH.put(key, zh);
            ATTR_NOTES_EN.put(key, en);
        }
    }

    private static List<String> attrNoteKeys(String id) {
        return List.of(
                "minecraft:" + id,
                "minecraft:generic." + id,
                "minecraft:player." + id,
                "minecraft:horse." + id,
                "minecraft:zombie." + id
        );
    }

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
    private boolean nativeDialogOpen = false;
    private long lastNativeDialogAt = 0L;
    private float openAnim = 0f;

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
                Component.translatable("ankinbt.btn.close"), this::tryClose));
        bx -= bw + gap;

        int saveW = 40;
        bx -= saveW + gap;
        headerBtns.add(new Btn(bx, by, saveW, bw,
                Component.translatable("ankinbt.btn.save").getString(),
                Component.translatable("ankinbt.btn.save.tip"), this::saveToItem));

        int invW = 42;
        bx -= invW + gap;
        headerBtns.add(new Btn(bx, by, invW, bw,
                Component.translatable("ankinbt.btn.inventory").getString(),
                Component.translatable("ankinbt.btn.switch_inventory"), this::openInventorySwitch));

        int modeW = 50;
        bx -= modeW + gap + 4;
        headerBtns.add(new Btn(bx, by, modeW, bw,
                Component.translatable("ankinbt.btn.advanced").getString(),
                Component.translatable("ankinbt.btn.switch_advanced"), this::switchToAdvanced));
    }

    // ==================== RENDER ====================

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mx, int my, float pt) {
        render(new com.ankinbt.compat.GuiGraphics(g), mx, my, pt);
    }

    public void render(GuiGraphics g, int mx, int my, float pt) {
        float cfgSpeed = AnkiConfig.getUiAnimationSpeed();
        float speed = Math.max(0.06f, Math.min(0.16f, cfgSpeed));
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);
        int scrimAlpha = Math.max(70, Math.min(130, Math.round(112f * openAnim)));
        int panel = fadeColor(BG, openAnim);
        int header = fadeColor(HEADER_BG, openAnim);
        int border = fadeColor(BORDER, openAnim);

        g.fill(0, 0, width, height, (scrimAlpha << 24));
        g.fill(px, py, px + pw, py + ph, panel);
        drawBorder(g, px, py, pw, ph, border);

        // Header
        g.fill(px + 1, py + 1, px + pw - 1, py + HEADER_H, header);
        g.fill(px + 1, py + HEADER_H, px + pw - 1, py + HEADER_H + 1, border);
        g.drawString(font, "AnkiNBT", px + 16, py + 11, 0xFFE2E8F0, false);
        g.drawString(font, "简单模式", px + 64, py + 11, 0xFF38BDF8, false);
        if (dirty) g.drawString(font, "*", px + 116, py + 12, ERROR_C, false);

        for (Btn b : headerBtns) b.render(g, font, mx, my);

        // Sidebar
        renderSidebar(g, mx, my);
        g.fill(px + SIDEBAR_W + 1, py + HEADER_H + 1, px + SIDEBAR_W + 2, py + ph - FOOTER_H, border);

        // Content
        if (activeSubEditor != null) {
            activeSubEditor.render(g, font, mx, my, contentX, contentY, contentW, contentH);
        } else {
            renderCategoryContent(g, mx, my);
        }

        // Footer
        g.fill(px + 1, py + ph - FOOTER_H, px + pw - 1, py + ph - FOOTER_H + 1, border);
        renderFooter(g);
    }

    private int fadeColor(int color, float factor) {
        int a = (color >>> 24) & 0xFF;
        int alpha = Math.max(0, Math.min(255, Math.round(a * factor)));
        return (alpha << 24) | (color & 0x00FFFFFF);
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
            g.fill(lx, cy, lx + cw, cy + CAT_H, active ? accentColor() : (hover ? BTN_HOVER : CAT_BG));
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
            int labelX = contentX + 8;
            if (row.icon != null && !row.icon.isEmpty()) {
                g.renderItem(row.icon, contentX + 5, ry + 4);
                labelX += 22;
            }
            String rowLabel = row.label;
            int labelBudget = Math.max(40, contentW - (labelX - contentX) - 118);
            if (font.width(rowLabel) > labelBudget) rowLabel = font.plainSubstrByWidth(rowLabel, Math.max(10, labelBudget - 10)) + "..";
            g.drawString(font, rowLabel, labelX, ry + (ROW_H - 8) / 2, row.labelColor, false);

            int rightX = contentX + contentW - 8;
            if (row.deleteAction != null) {
                int delW = 24, delH = 16, delY = ry + (ROW_H - delH) / 2;
                rightX -= delW;
                boolean delHover = mx >= rightX && mx < rightX + delW && my >= delY && my < delY + delH;
                g.fill(rightX, delY, rightX + delW, delY + delH, delHover ? BTN_HOVER : BTN_BG);
                g.drawString(font, "X", rightX + (delW - font.width("X")) / 2, delY + 4, delHover ? ERROR_C : C3, false);
                rightX -= 4;
            }
            // Inline move buttons (right side, before value)
            if (row.moveUp != null || row.moveDown != null) {
                int btnW = 16, btnH = 16, btnY = ry + (ROW_H - btnH) / 2;
                if (row.moveDown != null) {
                    rightX -= btnW + 2;
                    boolean dHover = mx >= rightX && mx < rightX + btnW && my >= btnY && my < btnY + btnH;
                    g.fill(rightX, btnY, rightX + btnW, btnY + btnH, dHover ? BTN_HOVER : BTN_BG);
                    g.drawString(font, "v", rightX + (btnW - font.width("v")) / 2, btnY + 4, dHover ? C1 : C3, false);
                }
                if (row.moveUp != null) {
                    rightX -= btnW + 2;
                    boolean uHover = mx >= rightX && mx < rightX + btnW && my >= btnY && my < btnY + btnH;
                    g.fill(rightX, btnY, rightX + btnW, btnY + btnH, uHover ? BTN_HOVER : BTN_BG);
                    g.drawString(font, "^", rightX + (btnW - font.width("^")) / 2, btnY + 4, uHover ? C1 : C3, false);
                }
                rightX -= 4;
            }

            if (row.currentValue != null) {
                String val = row.currentValue;
                int maxValW = rightX - (contentX + contentW / 2);
                if (font.width(val) > maxValW) val = font.plainSubstrByWidth(val, maxValW - 10) + "..";
                g.drawString(font, val, rightX - font.width(val), ry + (ROW_H - 8) / 2, C2, false);
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

    private int accentColor() { return UiTheme.accent(AnkiConfig.getUiAccentPreset()); }

    // ==================== INPUT ====================

    private boolean handleMouseClicked(double mx, double my, int btn) {
        for (Btn b : headerBtns) if (b.isHover((int) mx, (int) my)) { UiSound.playClick(); b.action.run(); return true; }
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
                UiSound.playClick();
                activeCat = cats[i]; scrollOff = 0; return true;
            }
        }
        if (hoverRow >= 0) {
            List<ActionRow> rows = getRowsForCategory(activeCat);
            if (hoverRow < rows.size()) {
                ActionRow row = rows.get(hoverRow);
                if (row.deleteAction != null) {
                    int ry = contentY + (hoverRow - scrollOff) * ROW_H;
                    int delW = 24, delH = 16, delY = ry + (ROW_H - delH) / 2;
                    int delX = contentX + contentW - 8 - delW;
                    if (mx >= delX && mx < delX + delW && my >= delY && my < delY + delH) {
                        UiSound.playClick();
                        row.deleteAction.run();
                        return true;
                    }
                }
                if (row.moveUp != null || row.moveDown != null) {
                    int ry = contentY + (hoverRow - scrollOff) * ROW_H;
                    int btnW = 16, btnH = 16, btnY = ry + (ROW_H - btnH) / 2;
                    int rightX = contentX + contentW - 8 - (row.deleteAction != null ? 28 : 0);
                    if (row.moveDown != null) {
                        rightX -= btnW + 2;
                        if (mx >= rightX && mx < rightX + btnW && my >= btnY && my < btnY + btnH) {
                            UiSound.playClick();
                            row.moveDown.run(); return true;
                        }
                    }
                    if (row.moveUp != null) {
                        rightX -= btnW + 2;
                        if (mx >= rightX && mx < rightX + btnW && my >= btnY && my < btnY + btnH) {
                            UiSound.playClick();
                            row.moveUp.run(); return true;
                        }
                    }
                }
                UiSound.playClick();
                row.action.run(); return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x();
        double my = event.y();
        int btn = event.button();
        if (handleMouseClicked(mx, my, btn)) return true;

        if (minecraft != null) {
            double sw = minecraft.getWindow().getScreenWidth();
            double sh = minecraft.getWindow().getScreenHeight();
            if (sw > 0.0 && sh > 0.0) {
                double sx = mx * width / sw;
                double sy = my * height / sh;
                if ((Math.abs(sx - mx) > 0.5 || Math.abs(sy - my) > 0.5) && handleMouseClicked(sx, sy, btn)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(event, isDoubleClick);
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();
        if (this.activeSubEditor != null && this.activeSubEditor.mouseDragged(mx, my, button, dragX, dragY, this.contentX, this.contentY, this.contentW, this.contentH)) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }
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
    public boolean keyPressed(KeyEvent event) {
        int key = event.key(); int scan = event.scancode(); int mod = 0;
        if (activeSubEditor != null) {
            if (key == 256) { activeSubEditor = null; return true; }
            return activeSubEditor.keyPressed(key, scan, mod);
        }
        if (key == 256) { tryClose(); return true; }
        if (key == 83 && (mod & 2) != 0) { saveToItem(); return true; }
        return super.keyPressed(event);
    }

    private void tryClose() {
        if (dirty && com.ankinbt.config.AnkiConfig.isConfirmOnClose()) {
            activeSubEditor = new ConfirmCloseSubEditor();
        } else {
            onClose();
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char c = (char) event.codepoint(); int mod = 0;
        if (activeSubEditor != null) return activeSubEditor.charTyped(c, mod);
        return super.charTyped(event);
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
            rows.add(new ActionRow(tr("ankinbt.simple.rarity"), getRarityDisplayName(rarity), () -> cycleRarity()));
        }

        if (isPotionLike()) {
            rows.add(new ActionRow(tr("ankinbt.simple.potion_base"), potionDisplayName(getPotionId()),
                    () -> activeSubEditor = new PotionPickerSubEditor(), accentColor(), null, null, ItemEditorVisuals.potionRowIcon(editStack.getItem().builtInRegistryHolder().key().identifier().toString())));
            int color = getPotionCustomColor();
            rows.add(new ActionRow(tr("ankinbt.simple.potion_custom_color"),
                    color >= 0 ? String.format("#%06X", color & 0xFFFFFF) : tr("ankinbt.simple.none"),
                    () -> activeSubEditor = new ColorPickerSubEditor(-3), accentColor()));
            if (color >= 0) {
                rows.add(new ActionRow(tr("ankinbt.simple.potion_clear_color"), null, this::clearPotionCustomColor, ERROR_C));
            }
            int effects = getPotionCustomEffectCount();
            rows.add(new ActionRow(tr("ankinbt.simple.potion_effects"), String.valueOf(effects),
                    () -> activeSubEditor = new PotionEffectSubEditor(), accentColor(), null, null, ItemEditorVisuals.effectIconStack("minecraft:speed")));
            if (effects > 0) {
                rows.add(new ActionRow(tr("ankinbt.simple.potion_clear_effects"), null, this::clearPotionCustomEffects, ERROR_C));
            }
        }

        return rows;
    }

    private List<ActionRow> getEnchantRows() {
        List<ActionRow> rows = new ArrayList<>();
        ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(editStack);
        enchants.entrySet().forEach(entry -> {
            Holder<Enchantment> ench = entry.getKey();
            int level = entry.getIntValue();
            String eId = ench.unwrapKey().map(this::registryKeyToString).orElse("?");
            String displayName = getEnchantDisplayName(eId);
            rows.add(new ActionRow(displayName, tr("ankinbt.simple.level") + level,
                    () -> openInlineEditor("ench_level:" + eId, String.valueOf(level)), C1, null, null, ItemEditorVisuals.enchantIconStack(eId),
                    () -> removeEnchantment(eId)));
        });
        rows.add(new ActionRow(tr("ankinbt.simple.add_enchant"), null,
                () -> activeSubEditor = new EnchantPickerSubEditor(), accentColor()));
        if (!enchants.isEmpty()) {
            rows.add(new ActionRow(tr("ankinbt.simple.clear_enchants"), null, this::clearEnchantments, ERROR_C));
        }
        return rows;
    }

    private List<ActionRow> getLoreRows() {
        List<ActionRow> rows = new ArrayList<>();
        // Color code hint
        rows.add(new ActionRow(tr("ankinbt.simple.lore_color_hint"), null, () -> activeSubEditor = new ColorPickerSubEditor(-1), C3));

        // Full text editor for all lore
        rows.add(new ActionRow(tr("ankinbt.simple.lore_text_editor"), null,
                () -> activeSubEditor = new LoreTextEditorSubEditor(), accentColor()));

        List<Component> lore = getLore();
        for (int i = 0; i < lore.size(); i++) {
            int idx = i;
            String text = lore.get(i).getString();
            if (text.length() > 30) text = text.substring(0, 27) + "...";
            String prefix = (i + 1) + ". " + text;
            // Build inline move hint with clickable areas handled in renderCategoryContent
            String moveHint = "";
            if (lore.size() > 1) {
                if (i > 0 && i < lore.size() - 1) moveHint = "^ v";
                else if (i > 0) moveHint = "^";
                else if (i < lore.size() - 1) moveHint = "v";
            }
            final int fi = i;
            final int loreSize = lore.size();
            rows.add(new ActionRow(prefix, moveHint.isEmpty() ? null : moveHint,
                    () -> openLoreEditor("lore:" + fi, getLoreRawText(fi)),
                    C1, fi > 0 ? () -> moveLore(fi, fi - 1) : null,
                    fi < loreSize - 1 ? () -> moveLore(fi, fi + 1) : null));
        }
        rows.add(new ActionRow(tr("ankinbt.simple.add_lore"), null,
                () -> openLoreEditor("lore_add", ""), accentColor()));
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
            String attrId = entry.attribute().unwrapKey().map(this::registryKeyToString).orElse("?");
            String displayName = getAttrDisplayName(attrId);
            double amount = entry.modifier().amount();
            String opName = getOpName(entry.modifier().operation());
            String slotName = getSlotDisplayName(entry.slot());
            String valueStr = String.format("%.2f %s [%s]", amount, opName, slotName);
            rows.add(new ActionRow(displayName, valueStr,
                    () -> openInlineEditor("attr_amount:" + idx, String.valueOf(amount)), C1, null, null, ItemEditorVisuals.attributeIconStack(attrId),
                    () -> removeAttribute(idx)));
            if (AnkiConfig.isAttributeNotesEnabled()) {
                String note = getAttrNote(attrId);
                if (note != null && !note.isBlank()) {
                    rows.add(new ActionRow("i  " + note, null, () -> {}, C3));
                }
            }
        }

        rows.add(new ActionRow(tr("ankinbt.simple.add_attr"), null,
                () -> activeSubEditor = new AttributePickerSubEditor(), accentColor()));

        if (!entries.isEmpty()) {
            rows.add(new ActionRow(tr("ankinbt.simple.clear_attrs"), null, this::clearAttributes, ERROR_C));
        }
        return rows;
    }

    private String getAttrDisplayName(String attrId) {
        attrId = normalizeRegistryDisplayId(attrId);
        String translated = resolveAttributeDisplayName(attrId);
        if (translated != null) return formatLocalizedId(translated, attrId);
        if (isZhLanguage()) {
            String zh = findAttrText(ATTR_ZH, attrId);
            if (zh != null) return formatLocalizedId(zh, attrId);
        }
        return formatLocalizedId(prettifyRegistryId(attrId), attrId);
    }

    private String getAttrNote(String attrId) {
        Map<String, String> map = isZhLanguage() ? ATTR_NOTES_ZH : ATTR_NOTES_EN;
        return findAttrText(map, attrId);
    }

    private boolean isZhLanguage() {
        String lang = Minecraft.getInstance().options.languageCode;
        return lang != null && lang.startsWith("zh");
    }

    private String findAttrText(Map<String, String> map, String attrId) {
        if (map == null || attrId == null) return null;
        String direct = map.get(attrId);
        if (direct != null) return direct;
        String normalized = normalizeAttrId(attrId);
        if (normalized == null || normalized.isBlank()) return null;
        String exact = map.get("minecraft:" + normalized);
        if (exact != null) return exact;
        for (String prefix : new String[] { "generic.", "player.", "horse.", "zombie." }) {
            String value = map.get("minecraft:" + prefix + normalized);
            if (value != null) return value;
        }
        return null;
    }

    private String normalizeAttrId(String attrId) {
        String id = attrId;
        int colon = id.indexOf(':');
        if (colon >= 0 && colon + 1 < id.length()) id = id.substring(colon + 1);
        for (String prefix : new String[] { "generic.", "player.", "horse.", "zombie." }) {
            if (id.startsWith(prefix)) return id.substring(prefix.length());
        }
        return id;
    }

    private String registryKeyToString(Object key) {
        if (key == null) return "?";
        for (String method : new String[] { "location", "identifier" }) {
            try {
                Method m = key.getClass().getMethod(method);
                m.setAccessible(true);
                Object out = m.invoke(key);
                if (out != null) return normalizeRegistryDisplayId(out.toString());
            } catch (Throwable ignored) {}
        }
        return normalizeRegistryDisplayId(key.toString());
    }

    private String normalizeRegistryDisplayId(String raw) {
        if (raw == null) return "?";
        String text = raw.trim();
        if (text.isEmpty()) return "?";
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("[a-z0-9_.-]+:[a-z0-9_./-]+")
                .matcher(text.toLowerCase(Locale.ROOT));
        String last = null;
        while (matcher.find()) last = matcher.group();
        return last != null ? last : text;
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

    private String getRarityDisplayName(net.minecraft.world.item.Rarity rarity) {
        if (rarity == null) return "";
        String lang = Minecraft.getInstance().options.languageCode;
        boolean zh = lang != null && lang.startsWith("zh");
        if (zh) {
            return switch (rarity) {
                case COMMON -> "\u666e\u901a";
                case UNCOMMON -> "\u7f55\u89c1";
                case RARE -> "\u7a00\u6709";
                case EPIC -> "\u53f2\u8bd7";
            };
        }
        return switch (rarity) {
            case COMMON -> "Common";
            case UNCOMMON -> "Uncommon";
            case RARE -> "Rare";
            case EPIC -> "Epic";
        };
    }

    private boolean isPotionLike() {
        String id = resolveStackRegistryId(editStack);
        return id.contains("potion") || id.contains("tipped_arrow") || getPotionContentsTag() != null;
    }

    private CompoundTag getPotionContentsTag() {
        Optional<CompoundTag> opt = NbtHelper.serializeItemStack(editStack);
        if (opt.isEmpty()) return null;
        CompoundTag components = getCompoundTag(opt.get(), "components");
        return components == null ? null : getCompoundTag(components, "minecraft:potion_contents");
    }

    private String getPotionId() {
        CompoundTag potion = getPotionContentsTag();
        String id = readStringTag(potion, "potion", "");
        return id.isBlank() ? "minecraft:water" : id;
    }

    private int getPotionCustomColor() {
        CompoundTag potion = getPotionContentsTag();
        return readIntTag(potion, "custom_color", -1);
    }

    private int getPotionCustomEffectCount() {
        CompoundTag potion = getPotionContentsTag();
        ListTag effects = getListTag(potion, "custom_effects");
        return effects == null ? 0 : effects.size();
    }

    private void setPotionBase(String potionId) {
        if (potionId == null || potionId.isBlank()) return;
        updatePotionContents(potion -> potion.putString("potion", potionId));
    }

    private void setPotionCustomColor(int rgb) {
        updatePotionContents(potion -> potion.putInt("custom_color", rgb & 0xFFFFFF));
    }

    private void clearPotionCustomColor() {
        updatePotionContents(potion -> removeTagKey(potion, "custom_color"));
    }

    private void clearPotionCustomEffects() {
        updatePotionContents(potion -> removeTagKey(potion, "custom_effects"));
    }

    private void addPotionCustomEffect(String effectId, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        if (effectId == null || effectId.isBlank()) return;
        Map<String, EffectDraftData> effects = new LinkedHashMap<>();
        effects.put(effectId, new EffectDraftData(duration, amplifier, ambient, particles, icon));
        setPotionCustomEffects(effects);
    }

    private void setPotionCustomEffects(Map<String, EffectDraftData> effects) {
        updatePotionContents(potion -> {
            ListTag list = new ListTag();
            int count = 0;
            for (Map.Entry<String, EffectDraftData> entry : effects.entrySet()) {
                String id = entry.getKey();
                EffectDraftData data = entry.getValue();
                if (id == null || id.isBlank() || data == null) continue;
                CompoundTag effect = new CompoundTag();
                effect.putString("id", id);
                effect.putInt("duration", Math.max(1, Math.min(POTION_MAX_DURATION, data.duration)));
                effect.putInt("amplifier", Math.max(0, Math.min(POTION_MAX_AMPLIFIER, data.amplifier)));
                effect.putBoolean("ambient", data.ambient);
                effect.putBoolean("show_particles", data.particles);
                effect.putBoolean("show_icon", data.icon);
                list.add(effect);
                count++;
                if (count >= POTION_MAX_CUSTOM_EFFECTS) break;
            }
            potion.put("custom_effects", list);
        });
    }

    private record EffectDraftData(int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {}

    private void updatePotionContents(java.util.function.Consumer<CompoundTag> updater) {
        try {
            Optional<CompoundTag> opt = NbtHelper.serializeItemStack(editStack);
            if (opt.isEmpty()) {
                setStatus(tr("ankinbt.simple.potion_edit_failed"), ERROR_C);
                return;
            }
            CompoundTag full = copyCompoundTag(opt.get());
            CompoundTag components = getOrCreateCompoundTag(full, "components");
            CompoundTag potion = getCompoundTag(components, "minecraft:potion_contents");
            if (potion == null) potion = new CompoundTag();
            updater.accept(potion);
            components.put("minecraft:potion_contents", potion);
            Optional<ItemStack> out = NbtHelper.deserializeItemStack(full);
            if (out.isPresent() && !out.get().isEmpty()) {
                editStack = out.get();
                markDirty();
            } else {
                setStatus(tr("ankinbt.simple.potion_edit_failed"), ERROR_C);
            }
        } catch (Throwable t) {
            setStatus(tr("ankinbt.simple.potion_edit_failed"), ERROR_C);
        }
    }

    private String potionDisplayName(String id) {
        String name = id == null || id.isBlank() ? "minecraft:water" : id;
        String key = "item.minecraft." + name.replace("minecraft:", "").replace(':', '.');
        String translated = Component.translatable(key).getString();
        if (!translated.equals(key)) return formatLocalizedId(translated, name);
        return formatLocalizedId(prettifyRegistryId(name), name);
    }

    private String effectDisplayName(String id) {
        String name = id == null || id.isBlank() ? "minecraft:speed" : id;
        String key = "effect.minecraft." + name.replace("minecraft:", "").replace(':', '.');
        String translated = Component.translatable(key).getString();
        if (!translated.equals(key)) return formatLocalizedId(translated, name);
        return formatLocalizedId(prettifyRegistryId(name), name);
    }

    private CompoundTag copyCompoundTag(CompoundTag source) {
        if (source == null) return new CompoundTag();
        CompoundTag copy = new CompoundTag();
        copy.merge(source);
        return copy;
    }

    private CompoundTag getCompoundTag(CompoundTag parent, String key) {
        Object raw = getTagValue(parent, key);
        return raw instanceof CompoundTag tag ? tag : null;
    }

    private CompoundTag getOrCreateCompoundTag(CompoundTag parent, String key) {
        CompoundTag tag = getCompoundTag(parent, key);
        if (tag == null) {
            tag = new CompoundTag();
            parent.put(key, tag);
        }
        return tag;
    }

    private ListTag getListTag(CompoundTag parent, String key) {
        Object raw = getTagValue(parent, key);
        return raw instanceof ListTag list ? list : null;
    }

    private Object getTagValue(CompoundTag parent, String key) {
        if (parent == null || key == null || key.isBlank()) return null;
        try {
            Object out = parent.getClass().getMethod("get", String.class).invoke(parent, key);
            if (out instanceof Optional<?> opt) return opt.orElse(null);
            return out;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String readStringTag(CompoundTag parent, String key, String def) {
        if (parent == null) return def;
        try {
            Object out = parent.getClass().getMethod("getString", String.class).invoke(parent, key);
            if (out instanceof String s) return s;
            if (out instanceof Optional<?> opt && opt.orElse(null) instanceof String s) return s;
        } catch (Throwable ignored) {}
        Object raw = getTagValue(parent, key);
        if (raw != null) {
            try {
                Object v = raw.getClass().getMethod("getAsString").invoke(raw);
                if (v instanceof String s) return s;
            } catch (Throwable ignored) {}
        }
        return def;
    }

    private int readIntTag(CompoundTag parent, String key, int def) {
        if (parent == null) return def;
        try {
            Object out = parent.getClass().getMethod("getInt", String.class).invoke(parent, key);
            if (out instanceof Number n) return n.intValue();
            if (out instanceof Optional<?> opt && opt.orElse(null) instanceof Number n) return n.intValue();
        } catch (Throwable ignored) {}
        Object raw = getTagValue(parent, key);
        if (raw != null) {
            try {
                Object v = raw.getClass().getMethod("getAsInt").invoke(raw);
                if (v instanceof Number n) return n.intValue();
            } catch (Throwable ignored) {}
        }
        return def;
    }

    private boolean readBooleanTag(CompoundTag parent, String key, boolean def) {
        if (parent == null) return def;
        try {
            Object out = parent.getClass().getMethod("getBoolean", String.class).invoke(parent, key);
            if (out instanceof Boolean b) return b;
            if (out instanceof Optional<?> opt && opt.orElse(null) instanceof Boolean b) return b;
        } catch (Throwable ignored) {}
        Object raw = getTagValue(parent, key);
        if (raw != null) {
            try {
                Object v = raw.getClass().getMethod("getAsByte").invoke(raw);
                if (v instanceof Number n) return n.intValue() != 0;
            } catch (Throwable ignored) {}
            try {
                Object v = raw.getClass().getMethod("getAsBoolean").invoke(raw);
                if (v instanceof Boolean b) return b;
            } catch (Throwable ignored) {}
        }
        return def;
    }

    private void removeTagKey(CompoundTag parent, String key) {
        if (parent == null || key == null || key.isBlank()) return;
        try {
            parent.getClass().getMethod("remove", String.class).invoke(parent, key);
        } catch (Throwable ignored) {}
    }

    private void removeAttribute(int index) {
        var attrComp = editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(attrComp.modifiers());
        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
            ItemAttributeModifiers next = VersionCompat.get().withEntries(entries, attrComp);
            if (next == attrComp && entries.size() != attrComp.modifiers().size()) {
                setStatus(tr("ankinbt.status.save_error"), ERROR_C);
                return;
            }
            editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, next);
            markDirty();
        } else {
            setStatus(tr("ankinbt.status.save_error"), ERROR_C);
        }
    }

    private void clearAttributes() {
        editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        markDirty();
        setStatus(tr("ankinbt.simple.attrs_cleared"), C2);
    }

    private void addAttribute(String attrId, double amount, AttributeModifier.Operation op, EquipmentSlotGroup slot) {
        Optional<Holder.Reference<Attribute>> holder = VersionCompat.get().getAttributeHolder(attrId);
        if (holder.isEmpty()) {
            setStatus(tr("ankinbt.status.save_error"), ERROR_C);
            return;
        }

        var attrComp = editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(attrComp.modifiers());
        Identifier modId = Identifier.fromNamespaceAndPath("ankinbt", "custom_" + System.currentTimeMillis());
        entries.add(new ItemAttributeModifiers.Entry(holder.get(),
                new AttributeModifier(modId, amount, op), slot));
        ItemAttributeModifiers next = VersionCompat.get().withEntries(entries, attrComp);
        if (next == attrComp && entries.size() != attrComp.modifiers().size()) {
            setStatus(tr("ankinbt.status.save_error"), ERROR_C);
            return;
        }
        editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, next);
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
                    () -> activeSubEditor = new ColorPickerSubEditor(color), accentColor()));
        }

        // Custom name color
        rows.add(new ActionRow(tr("ankinbt.simple.name_color"), null,
                () -> activeSubEditor = new ColorPickerSubEditor(-2), accentColor()));

        return rows;
    }

    private List<ActionRow> getMiscRows() {
        List<ActionRow> rows = new ArrayList<>();
        rows.add(new ActionRow(tr("ankinbt.simple.copy_nbt"), null, this::copyNbtToClipboard));
        rows.add(new ActionRow(tr("ankinbt.simple.copy_give_cmd"), null, this::copyGiveCommand));
        rows.add(new ActionRow(tr("ankinbt.simple.export_nbt"), null, () -> activeSubEditor = new NbtExportSubEditor(), accentColor()));
        rows.add(new ActionRow(tr("ankinbt.simple.import_nbt"), null, () -> activeSubEditor = new NbtImportSubEditor(), accentColor()));
        if (supportsContainerPreview(editStack)) {
            rows.add(new ActionRow(tr("ankinbt.simple.container_preview"), null, () -> activeSubEditor = new ContainerPreviewSubEditor(), accentColor()));
        }
        rows.add(new ActionRow(tr("ankinbt.simple.reset"), null, this::resetItem, ERROR_C));
        return rows;
    }

    private boolean supportsContainerPreview(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        String itemId = "";
        try {
            itemId = stack.getItem().builtInRegistryHolder().key().identifier().toString();
        } catch (Throwable ignored) {}
        if (isKnownContainerItem(itemId)) return true;

        Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(stack);
        if (fullOpt.isEmpty()) return false;
        CompoundTag full = fullOpt.get();
        CompoundTag components = readCompoundTagCompat(full, "components");
        if (hasListTagCompat(components, "minecraft:container")) return true;
        if (hasListTagCompat(components, "minecraft:bundle_contents")) return true;
        CompoundTag tag = readCompoundTagCompat(full, "tag");
        CompoundTag block = readCompoundTagCompat(tag, "BlockEntityTag");
        return hasListTagCompat(block, "Items");
    }

    private boolean isKnownContainerItem(String itemId) {
        if (itemId == null || itemId.isBlank()) return false;
        return itemId.endsWith(":bundle")
                || itemId.contains("shulker_box")
                || itemId.endsWith(":chest")
                || itemId.endsWith(":trapped_chest")
                || itemId.endsWith(":barrel")
                || itemId.endsWith(":hopper")
                || itemId.endsWith(":dispenser")
                || itemId.endsWith(":dropper")
                || itemId.endsWith(":furnace")
                || itemId.endsWith(":blast_furnace")
                || itemId.endsWith(":smoker")
                || itemId.endsWith(":chiseled_bookshelf")
                || itemId.endsWith(":crafter")
                || itemId.endsWith(":brewing_stand");
    }

    private CompoundTag readCompoundTagCompat(CompoundTag parent, String key) {
        if (parent == null || key == null || key.isBlank()) return null;
        try {
            Object out = parent.getClass().getMethod("getCompound", String.class).invoke(parent, key);
            Object raw = unwrapOptionalCompat(out);
            if (raw instanceof CompoundTag ct) return ct;
        } catch (Throwable ignored) {}
        Object raw = readTagCompat(parent, key);
        return raw instanceof CompoundTag ct ? ct : null;
    }

    private boolean hasListTagCompat(CompoundTag parent, String key) {
        Object raw = readTagCompat(parent, key);
        return raw instanceof ListTag list && !list.isEmpty();
    }

    private Object readTagCompat(CompoundTag parent, String key) {
        if (parent == null || key == null || key.isBlank()) return null;
        try {
            Object out = parent.getClass().getMethod("get", String.class).invoke(parent, key);
            return unwrapOptionalCompat(out);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object unwrapOptionalCompat(Object value) {
        Object out = value;
        while (out instanceof Optional<?> opt) out = opt.orElse(null);
        return out;
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
        String id = editStack.getItem().builtInRegistryHolder().key().identifier().toString();
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
        String processed = input;
        MutableComponent result = Component.empty();
        int i = 0;
        Style currentStyle = Style.EMPTY.withItalic(false); // Force non-italic by default

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
            // Move at least one character to avoid infinite loop on unrecognized & or section codes
            i++;
            while (i < processed.length() && processed.charAt(i) != '&' && processed.charAt(i) != SECTION) i++;
            result.append(Component.literal(processed.substring(start, i)).withStyle(currentStyle));
        }
        return result;
    }

    private static Style applyColorCode(Style style, char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0x000000));
            case '1' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0x0000AA));
            case '2' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0x00AA00));
            case '3' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0x00AAAA));
            case '4' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0xAA0000));
            case '5' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0xAA00AA));
            case '6' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0xFFAA00));
            case '7' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0xAAAAAA));
            case '8' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0x555555));
            case '9' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0x5555FF));
            case 'a' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0x55FF55));
            case 'b' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0x55FFFF));
            case 'c' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0xFF5555));
            case 'd' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0xFF55FF));
            case 'e' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0xFFFF55));
            case 'f' -> Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(0xFFFFFF));
            case 'k' -> style.withObfuscated(true);
            case 'l' -> style.withBold(true);
            case 'm' -> style.withStrikethrough(true);
            case 'n' -> style.withUnderlined(true);
            case 'o' -> style.withItalic(true);
            case 'r' -> Style.EMPTY.withItalic(false);
            default -> null;
        };
    }

    private void setLore(List<Component> lines) {
        editStack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lines));
        dirty = true;
    }

    private void moveLore(int from, int to) {
        List<Component> lore = new ArrayList<>(getLore());
        if (from < 0 || from >= lore.size() || to < 0 || to >= lore.size()) return;
        Component moved = lore.remove(from);
        lore.add(to, moved);
        setLore(lore);
        setStatus(tr("ankinbt.simple.lore_moved"), C2);
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

    private void removeEnchantment(String enchId) {
        if (applyEnchantLevel(enchId, 0)) {
            dirty = true;
            setStatus(tr("ankinbt.status.deleted"), C2);
        } else {
            setStatus(tr("ankinbt.status.save_error"), ERROR_C);
        }
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
            String id = editStack.getItem().builtInRegistryHolder().key().identifier().toString();
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
        enchId = normalizeRegistryDisplayId(enchId);
        String translated = resolveEnchantDisplayName(enchId);
        if (translated != null) return formatLocalizedId(translated, enchId);
        String lang = Minecraft.getInstance().options.languageCode;
        if (lang != null && lang.startsWith("zh")) {
            String zh = ENCHANT_ZH.get(enchId);
            if (zh != null) return formatLocalizedId(zh, enchId);
        }
        return formatLocalizedId(prettifyRegistryId(enchId), enchId);
    }

    private String formatLocalizedId(String label, String id) {
        String cleanId = normalizeRegistryDisplayId(id);
        if (cleanId == null || cleanId.isBlank() || "?".equals(cleanId)) cleanId = "minecraft:unknown";
        String cleanLabel = label == null || label.isBlank() ? prettifyRegistryId(cleanId) : label;
        cleanLabel = cleanLabel.replace("ResourceKey[", "").replace("]", "");
        return cleanLabel.contains(cleanId) ? cleanLabel : cleanLabel + " (" + cleanId + ")";
    }

    private String resolveAttributeDisplayName(String attrId) {
        try {
            Optional<Holder.Reference<Attribute>> holder = VersionCompat.get().getAttributeHolder(attrId);
            if (holder.isEmpty()) return null;
            Object attribute = holder.get().value();
            for (String methodName : new String[] { "getDescriptionId", "descriptionId" }) {
                try {
                    Object out = attribute.getClass().getMethod(methodName).invoke(attribute);
                    if (out instanceof String key && !key.isBlank()) {
                        String translated = Component.translatable(key).getString();
                        if (!translated.isBlank() && !translated.equals(key)) return translated;
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private String resolveEnchantDisplayName(String enchId) {
        try {
            Optional<Holder.Reference<Enchantment>> holder = VersionCompat.get().getEnchantHolder(enchId);
            if (holder.isEmpty()) return null;
            Component description = invokeComponent(holder.get().value(), "description", "getDescription");
            if (description != null) {
                String translated = description.getString();
                if (!translated.isBlank()) return translated;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private Component invokeComponent(Object target, String... methodNames) {
        if (target == null || methodNames == null) return null;
        for (String methodName : methodNames) {
            try {
                Object out = target.getClass().getMethod(methodName).invoke(target);
                if (out instanceof Component component) return component;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private String prettifyRegistryId(String id) {
        String name = id == null ? "" : id;
        int idx = name.indexOf(':');
        if (idx >= 0 && idx + 1 < name.length()) {
            name = name.substring(idx + 1);
        }
        return name.replace("generic.", "").replace('_', ' ');
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
                    // Preserve color codes, but force non-italic to prevent default italic rendering
                    Component comp = colorCodedToComponent(value);
                    MutableComponent result = Component.empty().withStyle(Style.EMPTY.withItalic(false));
                    result.append(comp);
                    editStack.set(DataComponents.CUSTOM_NAME, result);
                } else {
                    editStack.set(DataComponents.CUSTOM_NAME, Component.literal(value).withStyle(Style.EMPTY.withItalic(false)));
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
                if (!applyEnchantLevel(enchId, Integer.parseInt(value))) {
                    setStatus(tr("ankinbt.status.save_error"), ERROR_C);
                    activeSubEditor = null;
                    return;
                }
            } else if (field.startsWith("attr_amount:")) {
                int idx = Integer.parseInt(field.substring(12));
                var attrComp = editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(attrComp.modifiers());
                if (idx >= 0 && idx < entries.size()) {
                    var old = entries.get(idx);
                    double newAmount = Double.parseDouble(value);
                    entries.set(idx, new ItemAttributeModifiers.Entry(old.attribute(),
                            new AttributeModifier(old.modifier().id(), newAmount, old.modifier().operation()), old.slot()));
                    ItemAttributeModifiers next = VersionCompat.get().withEntries(entries, attrComp);
                    if (next == attrComp) {
                        setStatus(tr("ankinbt.status.save_error"), ERROR_C);
                        activeSubEditor = null;
                        return;
                    }
                    editStack.set(DataComponents.ATTRIBUTE_MODIFIERS, next);
                } else {
                    setStatus(tr("ankinbt.status.save_error"), ERROR_C);
                    activeSubEditor = null;
                    return;
                }
            }
            dirty = true;
            setStatus(tr("ankinbt.status.edited"), C2);
        } catch (NumberFormatException e) {
            setStatus(tr("ankinbt.simple.invalid_number"), ERROR_C);
        }
        activeSubEditor = null;
    }

    private boolean applyEnchantLevel(String enchId, int level) {
        Identifier loc = Identifier.tryParse(enchId);
        if (loc == null) return false;
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(editStack));
        if (level <= 0) mutable.removeIf(h -> h.unwrapKey().map(k -> k.identifier().equals(loc)).orElse(false));
        else {
            Optional<Holder.Reference<Enchantment>> holder = VersionCompat.get().getEnchantHolder(enchId);
            if (holder.isEmpty()) return false;
            mutable.set(holder.get(), level);
        }
        editStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return true;
    }

    private void addEnchantment(String enchId, int level) {
        if (applyEnchantLevel(enchId, level)) {
            dirty = true; activeSubEditor = null;
            setStatus(Component.translatable("ankinbt.status.added", getEnchantDisplayName(enchId)).getString(), SUCCESS);
        } else {
            setStatus(tr("ankinbt.status.save_error"), ERROR_C);
        }
    }

    // ==================== SAVE ====================

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
        if (mc.player == null) return;
        if (!mc.player.isCreative()) { setStatus(tr("ankinbt.status.creative_only"), ERROR_C); return; }

        VersionCompat.get().sanitizeForCreativeSave(editStack);
        if (inventorySlot >= 0) {
            int creativeSlot = SimpleEditorScreen.creativePacketSlotFromEditedSlot(inventorySlot);
            if (creativeSlot < 0) {
                setStatus(tr("ankinbt.status.save_error"), ERROR_C);
                return;
            }
            int playerSlot = SimpleEditorScreen.playerInventoryIndexFromCreativeSlot(creativeSlot);
            if (playerSlot >= 0) {
                mc.player.getInventory().setItem(playerSlot, editStack.copy());
            }
            mc.gameMode.handleCreativeModeItemAdd(editStack.copy(), creativeSlot);
        } else {
            int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
            mc.player.getInventory().setItem(slot, editStack.copy());
            mc.gameMode.handleCreativeModeItemAdd(editStack.copy(), 36 + slot);
        }
        dirty = false;
        setStatus(tr("ankinbt.status.saved"), SUCCESS);
    }

    private void switchToAdvanced() {
        Minecraft.getInstance().setScreen(new NbtEditorScreen(editStack, inventorySlot));
    }

    private void openInventorySwitch() {
        activeSubEditor = new InventorySwitchSubEditor();
    }

    private void markDirty() { dirty = true; setStatus(tr("ankinbt.status.edited"), C2); }

    private void setStatus(String msg, int color) {
        statusMsg = msg; statusColor = color; statusTime = System.currentTimeMillis();
    }

    private static String tr(String key) { return Component.translatable(key).getString(); }

    private int currentEditedSlot() {
        if (inventorySlot >= 0) {
            int creativeSlot = SimpleEditorScreen.creativePacketSlotFromEditedSlot(inventorySlot);
            int playerSlot = SimpleEditorScreen.playerInventoryIndexFromCreativeSlot(creativeSlot);
            return playerSlot >= 0 ? playerSlot : inventorySlot;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return -1;
        return VersionCompat.get().getSelectedSlot(mc.player.getInventory());
    }

    private void switchToInventorySlot(int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            setStatus(tr("ankinbt.message.no_item"), ERROR_C);
            activeSubEditor = null;
            return;
        }
        if (slot < 0) {
            activeSubEditor = null;
            return;
        }
        ItemStack stack = mc.player.getInventory().getItem(slot);
        if (stack == null || stack.isEmpty()) {
            setStatus(tr("ankinbt.simple.inventory_empty"), ERROR_C);
            activeSubEditor = null;
            return;
        }
        Minecraft.getInstance().setScreen(new SimpleEditorScreen(stack.copy(), slot));
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
        Method fallback = null;
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName)) continue;
            Class<?>[] types = method.getParameterTypes();
            boolean hasStringArray = false;
            boolean hasPointerBuffer = false;
            for (Class<?> type : types) {
                if (type == String[].class) hasStringArray = true;
                if (type.getName().equals("org.lwjgl.PointerBuffer")) hasPointerBuffer = true;
            }
            if (hasStringArray) return method;
            if (fallback == null && !hasPointerBuffer) fallback = method;
            if (fallback == null) fallback = method;
        }
        return fallback;
    }

    private Object[] tinyFdArgs(Class<?>[] parameterTypes, String defaultPath, boolean isOpen) {
        Object[] args = new Object[parameterTypes.length];
        int stringIndex = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> pt = parameterTypes[i];
            if (CharSequence.class.isAssignableFrom(pt) || pt == String.class) {
                if (stringIndex == 0) {
                    args[i] = isOpen ? tr("ankinbt.simple.import_nbt") : tr("ankinbt.simple.export_nbt");
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

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c); g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c); g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private int textViewStart(net.minecraft.client.gui.Font font, String value, int cursor, int maxWidth) {
        if (value == null || value.isEmpty()) return 0;
        int clampedCursor = Math.max(0, Math.min(cursor, value.length()));
        int start = 0;
        while (start < clampedCursor && font.width(value.substring(start, clampedCursor)) > maxWidth) {
            start++;
        }
        return start;
    }

    private String visibleText(net.minecraft.client.gui.Font font, String value, int start, int maxWidth) {
        if (value == null || value.isEmpty()) return "";
        int safeStart = Math.max(0, Math.min(start, value.length()));
        String text = value.substring(safeStart);
        return font.width(text) <= maxWidth ? text : font.plainSubstrByWidth(text, maxWidth);
    }

    private int plainCursorFromMouse(net.minecraft.client.gui.Font font, String value, int start, int relX, int maxWidth) {
        if (value == null || value.isEmpty()) return 0;
        int safeStart = Math.max(0, Math.min(start, value.length()));
        String shown = visibleText(font, value, safeStart, maxWidth);
        int best = safeStart;
        for (int i = 0; i <= shown.length(); i++) {
            String before = shown.substring(0, i);
            int charW = i < shown.length() ? font.width(String.valueOf(shown.charAt(i))) : 8;
            if (font.width(before) + Math.max(1, charW / 2) >= relX) return safeStart + i;
            best = safeStart + i;
        }
        return Math.max(0, Math.min(best, value.length()));
    }

    private int renderedCursorFromMouse(net.minecraft.client.gui.Font font, String raw, int relX, boolean rawMode) {
        if (raw == null || raw.isEmpty()) return 0;
        int visible = 0;
        for (int i = 0; i < raw.length(); i++) {
            int codeLen = rawMode ? 0 : colorCodeLengthAt(raw, i);
            if (codeLen > 0) {
                i += codeLen - 1;
                continue;
            }
            int charW = font.width(String.valueOf(raw.charAt(i)));
            if (visible + Math.max(1, charW / 2) >= relX) return i;
            visible += charW;
        }
        return raw.length();
    }

    private int renderedWidthBeforeCursor(net.minecraft.client.gui.Font font, String raw, int cursor, boolean rawMode) {
        if (raw == null || raw.isEmpty()) return 0;
        int width = 0;
        int end = Math.max(0, Math.min(cursor, raw.length()));
        for (int i = 0; i < end; i++) {
            int codeLen = rawMode ? 0 : colorCodeLengthAt(raw, i);
            if (codeLen > 0) {
                i += codeLen - 1;
                continue;
            }
            width += font.width(String.valueOf(raw.charAt(i)));
        }
        return width;
    }

    private int colorCodeLengthAt(String raw, int i) {
        if (raw == null || i + 1 >= raw.length()) return 0;
        char mark = raw.charAt(i);
        if (mark != '&' && mark != SECTION) return 0;
        char code = raw.charAt(i + 1);
        if (code == '#' && i + 7 < raw.length()) return 8;
        return "0123456789abcdefklmnorABCDEFKLMNOR".indexOf(code) >= 0 ? 2 : 0;
    }

    private String resolveStackRegistryId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "minecraft:air";
        try {
            Object holder = stack.getItem().builtInRegistryHolder();
            Object key = holder.getClass().getMethod("key").invoke(holder);
            for (String method : new String[] { "location", "identifier" }) {
                try {
                    Object out = key.getClass().getMethod(method).invoke(key);
                    if (out != null) return out.toString();
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return stack.getItem().toString();
    }

    private String resolveStackRegistryPath(ItemStack stack) {
        String id = resolveStackRegistryId(stack);
        int idx = id.indexOf(':');
        return idx >= 0 && idx + 1 < id.length() ? id.substring(idx + 1) : id;
    }

    private void clearLoreComponent() {
        removeStackComponent(DataComponents.LORE);
    }

    private void setCustomNameComponent(Component name) {
        setStackComponent(DataComponents.CUSTOM_NAME, name);
    }

    private void setStackComponent(Object type, Object value) {
        if (invokeOuterComponentMethod("setComponent", type, value)) return;
        invokeStackComponentMethod("set", type, value);
    }

    private void removeStackComponent(Object type) {
        if (invokeOuterComponentMethod("removeComponent", type)) return;
        invokeStackComponentMethod("remove", type);
    }

    private boolean invokeOuterComponentMethod(String methodName, Object... args) {
        for (Method method : getClass().getDeclaredMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) continue;
            try {
                method.setAccessible(true);
                method.invoke(this, args);
                return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    private boolean invokeStackComponentMethod(String methodName, Object... args) {
        for (Method method : editStack.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) continue;
            try {
                method.invoke(editStack, args);
                return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    @Override public boolean isPauseScreen() { return false; }

    // ==================== CONFIRM CLOSE DIALOG ====================

    class ConfirmCloseSubEditor implements SubEditor {
        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = 260, dh = 110;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ERROR_C);

            g.drawString(font, tr("ankinbt.confirm.title"), dx + 10, dy + 10, C1, false);
            g.fill(dx + 1, dy + 24, dx + dw - 1, dy + 25, BORDER);
            g.drawString(font, tr("ankinbt.confirm.unsaved"), dx + 10, dy + 32, C2, false);
            g.drawString(font, tr("ankinbt.confirm.discard_hint"), dx + 10, dy + 46, C3, false);

            int by = dy + dh - 32;
            int bw2 = 70, bh2 = 22;

            // Save & Close
            int saveX = dx + 10;
            boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
            g.fill(saveX, by, saveX + bw2, by + bh2, sh ? 0xFF16A34A : SUCCESS);
            String saveLabel = tr("ankinbt.confirm.save_close");
            g.drawString(font, saveLabel, saveX + (bw2 - font.width(saveLabel)) / 2, by + 7, C1, false);

            // Discard
            int discardX = dx + dw / 2 - bw2 / 2;
            boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
            g.fill(discardX, by, discardX + bw2, by + bh2, dh2 ? 0x80EF4444 : 0x40EF4444);
            String discardLabel = tr("ankinbt.confirm.discard");
            g.drawString(font, discardLabel, discardX + (bw2 - font.width(discardLabel)) / 2, by + 7, C1, false);

            // Cancel
            int cancelX = dx + dw - bw2 - 10;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 7, C2, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = 260, dh = 110;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
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
                activeSubEditor = null; return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 256) { activeSubEditor = null; return true; }
            return true;
        }

        @Override public boolean charTyped(char c, int mod) { return false; }
    }

    // ==================== NBT EXPORT ====================

    class NbtExportSubEditor implements SubEditor {
        String fileName;
        String category;
        String alias;
        int cursor;
        int focusField = 0; // 0=filename, 1=category, 2=alias
        String message = null;
        int msgColor = C2;
        List<String> existingCats;

        NbtExportSubEditor() {
            String itemId = resolveStackRegistryPath(editStack);
            long ts = System.currentTimeMillis() / 1000;
            fileName = itemId + "_" + ts;
            category = com.ankinbt.config.AnkiConfig.getLastExportCategory();
            alias = "";
            cursor = fileName.length();
            existingCats = com.ankinbt.config.AnkiConfig.listExportCategories();
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 380), dh = 240;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, accentColor());

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.export_nbt"), dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            int fieldX = dx + 10, fieldW = dw - 20, fieldH = 20;
            int curY = dy + 28;

            // Category field
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.export.category"), fieldX, curY, C2, false);
            curY += 12;
            g.fill(fieldX, curY, fieldX + fieldW, curY + fieldH, 0xFF12121E);
            drawBorder(g, fieldX, curY, fieldW, fieldH, focusField == 1 ? ACCENT : BORDER);
            String catDisp = category.isEmpty() ? tr("ankinbt.export.no_category") : category;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, catDisp + (focusField == 1 && System.currentTimeMillis() % 1000 < 500 ? "_" : ""),
                    fieldX + 4, curY + 6, category.isEmpty() ? C3 : C1, false);
            curY += fieldH + 4;

            // Quick category tags
            if (!existingCats.isEmpty()) {
                int tagX = fieldX;
                for (String cat : existingCats) {
                    int tw = font.width(cat) + 10;
                    if (tagX + tw > fieldX + fieldW) { tagX = fieldX; curY += 16; }
                    boolean hover = mx >= tagX && mx < tagX + tw && my >= curY && my < curY + 14;
                    boolean active = cat.equals(category);
                    g.fill(tagX, curY, tagX + tw, curY + 14, active ? ACCENT : (hover ? BTN_HOVER : BTN_BG));
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, cat, tagX + 5, curY + 3, active ? C1 : C2, false);
                    tagX += tw + 4;
                }
                curY += 18;
            }

            // Filename field
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.export.filename"), fieldX, curY, C2, false);
            curY += 12;
            g.fill(fieldX, curY, fieldX + fieldW, curY + fieldH, 0xFF12121E);
            drawBorder(g, fieldX, curY, fieldW, fieldH, focusField == 0 ? ACCENT : BORDER);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, fileName + ".nbt" + (focusField == 0 && System.currentTimeMillis() % 1000 < 500 ? "_" : ""),
                    fieldX + 4, curY + 6, C1, false);
            curY += fieldH + 4;

            // Alias field
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.export.alias"), fieldX, curY, C2, false);
            curY += 12;
            g.fill(fieldX, curY, fieldX + fieldW, curY + fieldH, 0xFF12121E);
            drawBorder(g, fieldX, curY, fieldW, fieldH, focusField == 2 ? ACCENT : BORDER);
            String aliasDisp = alias.isEmpty() ? tr("ankinbt.export.alias_hint") : alias;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, aliasDisp + (focusField == 2 && System.currentTimeMillis() % 1000 < 500 ? "_" : ""),
                    fieldX + 4, curY + 6, alias.isEmpty() ? C3 : C1, false);
            curY += fieldH + 6;

            // Path preview
            String pathPreview = com.ankinbt.config.AnkiConfig.getNbtExportDir();
            if (!category.isEmpty()) pathPreview += "/" + category;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.export.dir") + ": " + pathPreview, fieldX, curY, C3, false);
            curY += 12;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                    AnkiConfig.isNativeFileDialogEnabled() ? tr("ankinbt.export.native_dialog_on") : tr("ankinbt.export.native_dialog_off"),
                    fieldX, curY, C3, false);
            curY += 12;

            if (message != null) { com.ankinbt.compat.VersionCompat.get().drawString(g, font, message, fieldX, curY, msgColor, false); }

            // Buttons
            int by = dy + dh - 28, bw2 = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw2 - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, C2, false);

            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw2 && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw2, by + bh2, oh ? ACCENT : 0xFF4F46E5);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.export.do_export"), okX + (bw2 - font.width(tr("ankinbt.export.do_export"))) / 2, by + 6, C1, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 380), dh = 240;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            int fieldX = dx + 10, fieldW = dw - 20, fieldH = 20;

            int curY = dy + 28 + 12;
            // Category input click
            if (mx >= fieldX && mx < fieldX + fieldW && my >= curY && my < curY + fieldH) { focusField = 1; return true; }
            curY += fieldH + 4;

            // Quick category tag clicks
            if (!existingCats.isEmpty()) {
                int tagX = fieldX;
                for (String cat : existingCats) {
                    int tw = font.width(cat) + 10;
                    if (tagX + tw > fieldX + fieldW) { tagX = fieldX; curY += 16; }
                    if (mx >= tagX && mx < tagX + tw && my >= curY && my < curY + 14) {
                        category = cat.equals(category) ? "" : cat;
                        return true;
                    }
                    tagX += tw + 4;
                }
                curY += 18;
            }

            // Filename input click
            curY += 12;
            if (mx >= fieldX && mx < fieldX + fieldW && my >= curY && my < curY + fieldH) { focusField = 0; return true; }
            curY += fieldH + 4;

            // Alias input click
            curY += 12;
            if (mx >= fieldX && mx < fieldX + fieldW && my >= curY && my < curY + fieldH) { focusField = 2; return true; }

            // Buttons
            int by = dy + dh - 28, bw2 = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw2 - 6;
            if (mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2) { activeSubEditor = null; return true; }
            int okX = dx + dw / 2 + 6;
            if (mx >= okX && mx < okX + bw2 && my >= by && my < by + bh2) { doExport(); return true; }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { doExport(); return true; }
            if (key == 258) { focusField = (focusField + 1) % 3; return true; } // Tab
            String target = focusField == 0 ? fileName : (focusField == 1 ? category : alias);
            int cur = focusField == 0 ? cursor : (focusField == 1 ? category.length() : alias.length());

            if (key == 259 && cur > 0) {
                target = target.substring(0, cur - 1) + target.substring(cur);
                cur--;
                applyField(target, cur);
                return true;
            }
            if (key == 261 && cur < target.length()) {
                target = target.substring(0, cur) + target.substring(cur + 1);
                applyField(target, cur);
                return true;
            }
            if (key == 263 && cur > 0) { cur--; if (focusField == 0) cursor = cur; return true; }
            if (key == 262 && cur < target.length()) { cur++; if (focusField == 0) cursor = cur; return true; }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c < 32) return false;
            if (focusField == 0) {
                if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '"' || c == '<' || c == '>' || c == '|') return false;
                fileName = fileName.substring(0, cursor) + c + fileName.substring(cursor);
                cursor++;
            } else if (focusField == 1) {
                if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '"' || c == '<' || c == '>' || c == '|' || c == '.') return false;
                category = category + c;
            } else {
                alias = alias + c;
            }
            return true;
        }

        private void applyField(String val, int cur) {
            if (focusField == 0) { fileName = val; cursor = cur; }
            else if (focusField == 1) { category = val; }
            else { alias = val; }
        }

        private void doExport() {
            if (fileName.isEmpty()) { message = tr("ankinbt.export.empty_name"); msgColor = ERROR_C; return; }
            var opt = NbtHelper.serializeItemStack(editStack);
            if (opt.isEmpty()) { message = tr("ankinbt.export.failed"); msgColor = ERROR_C; return; }
            var path = (Path) null;
            String safeCategory = category.isBlank() ? null : category.trim();
            AnkiConfig.setLastExportCategory(safeCategory == null ? "" : safeCategory);
            if (hasTinyFd()) {
                Path base = com.ankinbt.config.AnkiConfig.getExportPath(safeCategory);
                String picked = tinyFdSavePath(base.resolve(fileName + ".nbt").toString());
                if (picked == null || picked.isBlank()) return;
                path = NbtFileIO.exportNbtToPath(opt.get(), Path.of(picked), alias.isBlank() ? null : alias);
            } else {
                path = NbtFileIO.exportNbt(opt.get(), fileName, safeCategory, alias.isBlank() ? null : alias);
            }
            if (path != null) {
                setStatus(tr("ankinbt.export.success"), SUCCESS);
                activeSubEditor = null;
            } else {
                message = tr("ankinbt.export.failed"); msgColor = ERROR_C;
            }
        }
    }

    // ==================== NBT IMPORT / FILE BROWSER ====================

    class NbtImportSubEditor implements SubEditor {
        private List<NbtFileIO.NbtFileEntry> files;
        private List<String> categories;
        private String currentCategory = "";
        private int scrollOff = 0;
        private int hoverIdx = -1;
        private int selectedIdx = -1;
        private net.minecraft.nbt.CompoundTag previewTag = null;
        private String previewInfo = null;
        private final Map<String, ItemStack> iconCache = new HashMap<>();

        NbtImportSubEditor() {
            categories = com.ankinbt.config.AnkiConfig.listExportCategories();
            files = NbtFileIO.listNbtFiles(null);
        }

        private void refreshFiles() {
            files = NbtFileIO.listNbtFiles(currentCategory.isEmpty() ? null : currentCategory);
            selectedIdx = -1; previewTag = null; previewInfo = null; scrollOff = 0;
            iconCache.clear();
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 440), dh = Math.min(h - 10, 320);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);

            g.drawString(font, tr("ankinbt.import.title"), dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            // Category tabs
            int tabY = dy + 26;
            int tabX = dx + 8;
            // "All" tab
            String allLabel = tr("ankinbt.import.all");
            int allW = font.width(allLabel) + 10;
            boolean allHover = mx >= tabX && mx < tabX + allW && my >= tabY && my < tabY + 16;
            boolean allActive = currentCategory.isEmpty();
            g.fill(tabX, tabY, tabX + allW, tabY + 16, allActive ? ACCENT : (allHover ? BTN_HOVER : BTN_BG));
            g.drawString(font, allLabel, tabX + 5, tabY + 4, allActive ? C1 : C2, false);
            tabX += allW + 4;

            for (String cat : categories) {
                int cw = font.width(cat) + 10;
                if (tabX + cw > dx + dw - 8) break;
                boolean hover = mx >= tabX && mx < tabX + cw && my >= tabY && my < tabY + 16;
                boolean active = cat.equals(currentCategory);
                g.fill(tabX, tabY, tabX + cw, tabY + 16, active ? ACCENT : (hover ? BTN_HOVER : BTN_BG));
                g.drawString(font, cat, tabX + 5, tabY + 4, active ? C1 : C2, false);
                tabX += cw + 4;
            }

            // File list area
            int listX = dx + 8, listY = tabY + 22;
            int listW = dw / 2 - 12, listH = dh - 110;
            g.fill(listX, listY, listX + listW, listY + listH, 0xFF0A0A14);
            drawBorder(g, listX, listY, listW, listH, BORDER);

            int rowH = 20;
            int maxItems = listH / rowH;
            hoverIdx = -1;
            if (files.isEmpty()) {
                g.drawString(font, tr("ankinbt.import.no_files"), listX + 8, listY + 8, C3, false);
            } else {
                int end = Math.min(scrollOff + maxItems, files.size());
                for (int i = scrollOff; i < end; i++) {
                    int ry = listY + (i - scrollOff) * rowH;
                    boolean hovered = mx >= listX && mx < listX + listW && my >= ry && my < ry + rowH;
                    if (hovered) hoverIdx = i;
                    boolean sel = i == selectedIdx;
                    if (sel) g.fill(listX + 1, ry, listX + listW - 1, ry + rowH, SELECT_BG);
                    else if (hovered) g.fill(listX + 1, ry, listX + listW - 1, ry + rowH, HOVER);

                    var entry = files.get(i);
                    ItemStack icon = iconFor(entry);
                    if (!icon.isEmpty()) g.renderItem(icon, listX + 3, ry + 2);
                    String name = entry.displayName();
                    if (font.width(name) > listW - 34) name = font.plainSubstrByWidth(name, listW - 40) + "..";
                    g.drawString(font, name, listX + 22, ry + 6, sel ? C1 : C2, false);
                }
            }

            // Preview area
            int prevX = dx + dw / 2 + 4, prevY = listY;
            int prevW = dw / 2 - 12, prevH = listH;
            g.fill(prevX, prevY, prevX + prevW, prevY + prevH, 0xFF0A0A14);
            drawBorder(g, prevX, prevY, prevW, prevH, BORDER);

            g.drawString(font, tr("ankinbt.import.preview"), prevX + 6, prevY + 4, C2, false);
            if (selectedIdx >= 0 && selectedIdx < files.size()) {
                var entry = files.get(selectedIdx);
                g.drawString(font, entry.name(), prevX + 6, prevY + 18, C1, false);
                if (entry.alias() != null) {
                    g.drawString(font, tr("ankinbt.export.alias") + " " + entry.alias(), prevX + 6, prevY + 30, ACCENT, false);
                    g.drawString(font, entry.sizeDisplay(), prevX + 6, prevY + 42, C3, false);
                } else {
                    g.drawString(font, entry.sizeDisplay(), prevX + 6, prevY + 30, C3, false);
                }

                int infoStartY = entry.alias() != null ? prevY + 56 : prevY + 44;
                if (previewInfo != null) {
                    String[] infoLines = previewInfo.split("\n");
                    for (int i = 0; i < Math.min(infoLines.length, (prevH - 60) / 11); i++) {
                        String line = infoLines[i];
                        if (font.width(line) > prevW - 12) line = font.plainSubstrByWidth(line, prevW - 18) + "..";
                        g.drawString(font, line, prevX + 6, infoStartY + i * 11, C2, false);
                    }
                }
            } else {
                g.drawString(font, tr("ankinbt.import.select_file"), prevX + 6, prevY + 20, C3, false);
            }

            // Buttons
            int by = dy + dh - 32;
            int bw2 = 70, bh2 = 22;

            int refX = dx + 10;
            boolean rh = mx >= refX && mx < refX + 50 && my >= by && my < by + bh2;
            g.fill(refX, by, refX + 50, by + bh2, rh ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.import.refresh"), refX + (50 - font.width(tr("ankinbt.import.refresh"))) / 2, by + 7, C2, false);
            int openW = 76;
            int openX = refX + 56;
            boolean fh = mx >= openX && mx < openX + openW && my >= by && my < by + bh2;
            g.fill(openX, by, openX + openW, by + bh2, fh ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.import.open_file"), openX + (openW - font.width(tr("ankinbt.import.open_file"))) / 2, by + 7, C2, false);

            int cancelX = dx + dw / 2 - bw2 - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 7, C2, false);

            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw2 && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw2, by + bh2, oh ? ACCENT : 0xFF4F46E5);
            g.drawString(font, tr("ankinbt.import.do_import"), okX + (bw2 - font.width(tr("ankinbt.import.do_import"))) / 2, by + 7, C1, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 440), dh = Math.min(h - 10, 320);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;

            // Category tab clicks
            int tabY = dy + 26;
            int tabX = dx + 8;
            String allLabel = tr("ankinbt.import.all");
            int allW = font.width(allLabel) + 10;
            if (mx >= tabX && mx < tabX + allW && my >= tabY && my < tabY + 16) {
                currentCategory = ""; refreshFiles(); return true;
            }
            tabX += allW + 4;
            for (String cat : categories) {
                int cw = font.width(cat) + 10;
                if (tabX + cw > dx + dw - 8) break;
                if (mx >= tabX && mx < tabX + cw && my >= tabY && my < tabY + 16) {
                    currentCategory = cat; refreshFiles(); return true;
                }
                tabX += cw + 4;
            }

            // File list click
            int listX = dx + 8, listY = tabY + 22;
            int listW = dw / 2 - 12;
            if (hoverIdx >= 0 && hoverIdx < files.size() && mx >= listX && mx < listX + listW) {
                selectedIdx = hoverIdx;
                loadPreview();
                return true;
            }

            int by = dy + dh - 32;
            int bw2 = 70, bh2 = 22;

            int refX = dx + 10;
            if (mx >= refX && mx < refX + 50 && my >= by && my < by + bh2) {
                categories = com.ankinbt.config.AnkiConfig.listExportCategories();
                refreshFiles(); return true;
            }
            int openW = 76;
            int openX = refX + 56;
            if (mx >= openX && mx < openX + openW && my >= by && my < by + bh2) {
                importFromDialog();
                return true;
            }

            int cancelX = dx + dw / 2 - bw2 - 6;
            if (mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2) { activeSubEditor = null; return true; }
            int okX = dx + dw / 2 + 6;
            if (mx >= okX && mx < okX + bw2 && my >= by && my < by + bh2) { doImport(); return true; }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { doImport(); return true; }
            if (key == 265 && selectedIdx > 0) { selectedIdx--; loadPreview(); return true; }
            if (key == 264 && selectedIdx < files.size() - 1) { selectedIdx++; loadPreview(); return true; }
            return true;
        }

        @Override public boolean charTyped(char c, int mod) { return false; }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            scrollOff -= (int) sy * 3;
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, files.size() - 5)));
            return true;
        }

        private void loadPreview() {
            if (selectedIdx < 0 || selectedIdx >= files.size()) return;
            var entry = files.get(selectedIdx);
            previewTag = NbtFileIO.importNbt(entry.path());
            if (previewTag != null) {
                StringBuilder sb = new StringBuilder();
                if (previewTag.contains("id")) sb.append("ID: ").append(VersionCompat.get().compoundGetString(previewTag, "id")).append("\n");
                if (previewTag.contains("count")) sb.append("Count: ").append(VersionCompat.get().compoundGetInt(previewTag, "count")).append("\n");
                if (previewTag.contains("components")) {
                    var comp = previewTag.get("components");
                    if (comp instanceof net.minecraft.nbt.CompoundTag ct) {
                        sb.append("Components: ").append(ct.size()).append("\n");
                        for (String key : VersionCompat.get().getCompoundKeys(ct)) {
                            sb.append("  ").append(key).append("\n");
                        }
                    }
                }
                previewInfo = sb.toString();
            } else {
                previewInfo = tr("ankinbt.import.load_failed");
            }
        }

        private void doImport() {
            if (selectedIdx < 0 || selectedIdx >= files.size()) {
                setStatus(tr("ankinbt.import.select_file"), ERROR_C); return;
            }
            if (previewTag == null) { loadPreview(); }
            if (previewTag == null) { setStatus(tr("ankinbt.import.load_failed"), ERROR_C); return; }

            var opt = NbtHelper.deserializeItemStack(previewTag);
            if (opt.isEmpty()) { setStatus(tr("ankinbt.import.invalid_nbt"), ERROR_C); return; }

            editStack = opt.get();
            markDirty();
            setStatus(tr("ankinbt.import.success"), SUCCESS);
            activeSubEditor = null;
        }

        private void importFromDialog() {
            if (!hasTinyFd()) return;
            String picked = tinyFdOpenPath(com.ankinbt.config.AnkiConfig.getExportPath().toString());
            if (picked == null || picked.isBlank()) return;
            CompoundTag tag = NbtFileIO.importNbt(Path.of(picked));
            if (tag == null) {
                setStatus(tr("ankinbt.import.load_failed"), ERROR_C);
                return;
            }
            var opt = NbtHelper.deserializeItemStack(tag);
            if (opt.isEmpty()) {
                setStatus(tr("ankinbt.import.invalid_nbt"), ERROR_C);
                return;
            }
            editStack = opt.get();
            markDirty();
            setStatus(tr("ankinbt.import.success"), SUCCESS);
            activeSubEditor = null;
        }

        private ItemStack iconFor(NbtFileIO.NbtFileEntry entry) {
            if (entry == null || entry.path() == null) return ItemStack.EMPTY;
            String key = entry.path().toString();
            ItemStack cached = iconCache.get(key);
            if (cached != null) return cached;
            ItemStack icon = ItemStack.EMPTY;
            CompoundTag tag = NbtFileIO.importNbt(entry.path());
            if (tag != null) {
                var opt = NbtHelper.deserializeItemStack(tag);
                if (opt.isPresent()) icon = opt.get();
            }
            iconCache.put(key, icon);
            return icon;
        }
    }

    class ContainerPreviewSubEditor implements SubEditor {
        private static final int COLS = 9;
        private static final int ROWS = 3;
        private static final int PAGE_SIZE = COLS * ROWS;

        private final List<CompoundTag> slotTags = new ArrayList<>();
        private final List<ItemStack> slotStacks = new ArrayList<>();
        private int selectedSlot = 0;
        private int page = 0;
        private StorageMode mode = StorageMode.CONTAINER;
        private String message = "";
        private int msgColor = C2;

        ContainerPreviewSubEditor() {
            loadFromItem();
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 470), dh = Math.min(h - 10, 300);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);

            g.drawString(font, tr("ankinbt.container.title"), dx + 10, dy + 8, C1, false);
            g.drawString(font, tr("ankinbt.container.mode") + ": " + modeName(), dx + 190, dy + 8, C2, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            int gridX = dx + 14;
            int gridY = dy + 36;
            int cell = 20;
            int gap = 4;

            int start = page * PAGE_SIZE;
            int hoveredGlobal = -1;
            ItemStack hoveredStack = ItemStack.EMPTY;

            for (int i = 0; i < PAGE_SIZE; i++) {
                int col = i % COLS;
                int row = i / COLS;
                int sx = gridX + col * (cell + gap);
                int sy = gridY + row * (cell + gap);
                int global = start + i;
                boolean sel = global == selectedSlot;
                boolean hover = mx >= sx && mx < sx + cell && my >= sy && my < sy + cell;
                if (hover) hoveredGlobal = global;

                int bg = sel ? 0x805F3DC4 : hover ? 0x604F46E5 : 0x40212B43;
                g.fill(sx, sy, sx + cell, sy + cell, bg);
                drawBorder(g, sx, sy, cell, cell, sel ? ACCENT : BORDER);

                ItemStack st = stackAt(global);
                if (!st.isEmpty()) {
                    g.renderItem(st, sx + 2, sy + 2);
                    if (hover) hoveredStack = st;
                }
            }

            String slotLabel = tr("ankinbt.container.slot") + " " + selectedSlot;
            g.drawString(font, slotLabel, dx + 14, dy + 112, C2, false);
            String pageText = (page + 1) + " / " + Math.max(1, (slotTags.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            g.drawString(font, pageText, dx + 14 + font.width(slotLabel) + 18, dy + 112, C3, false);

            int by = dy + dh - 30;
            int bw = 66;
            int bh = 20;
            int bx = dx + 10;

            renderSmallBtn(g, font, mx, my, bx, by, 20, bh, "<");
            bx += 24;
            renderSmallBtn(g, font, mx, my, bx, by, 20, bh, ">");
            bx += 28;
            renderSmallBtn(g, font, mx, my, bx, by, bw, bh, tr("ankinbt.container.from_hand"));
            bx += bw + 6;
            renderSmallBtn(g, font, mx, my, bx, by, bw, bh, tr("ankinbt.container.pick_item"));
            bx += bw + 6;
            renderSmallBtn(g, font, mx, my, bx, by, bw, bh, tr("ankinbt.container.clear_slot"));
            bx += bw + 6;
            renderSmallBtn(g, font, mx, my, bx, by, 56, bh, modeName());
            bx += 62;
            renderSmallBtn(g, font, mx, my, bx, by, 58, bh, tr("ankinbt.edit.apply"));
            bx += 64;
            renderSmallBtn(g, font, mx, my, bx, by, 58, bh, tr("ankinbt.edit.cancel"));

            if (!message.isEmpty()) {
                g.drawString(font, message, dx + 10, by - 12, msgColor, false);
            }

            if (hoveredGlobal >= 0 && !hoveredStack.isEmpty()) {
                VersionCompat.get().renderTooltip(g, font, hoveredStack.getHoverName(), mx, my);
            }
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 470), dh = Math.min(h - 10, 300);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            int gridX = dx + 14;
            int gridY = dy + 36;
            int cell = 20;
            int gap = 4;

            for (int i = 0; i < PAGE_SIZE; i++) {
                int col = i % COLS;
                int row = i / COLS;
                int sx = gridX + col * (cell + gap);
                int sy = gridY + row * (cell + gap);
                if (mx >= sx && mx < sx + cell && my >= sy && my < sy + cell) {
                    selectedSlot = page * PAGE_SIZE + i;
                    ensureSlots(selectedSlot + 1);
                    return true;
                }
            }

            int by = dy + dh - 30;
            int bw = 66;
            int bh = 20;
            int bx = dx + 10;

            if (hit(mx, my, bx, by, 20, bh)) { prevPage(); return true; }
            bx += 24;
            if (hit(mx, my, bx, by, 20, bh)) { nextPage(); return true; }
            bx += 28;
            if (hit(mx, my, bx, by, bw, bh)) { fillFromMainHand(); return true; }
            bx += bw + 6;
            if (hit(mx, my, bx, by, bw, bh)) { openPicker(); return true; }
            bx += bw + 6;
            if (hit(mx, my, bx, by, bw, bh)) { clearSelected(); return true; }
            bx += bw + 6;
            if (hit(mx, my, bx, by, 56, bh)) { cycleMode(); return true; }
            bx += 62;
            if (hit(mx, my, bx, by, 58, bh)) { applyToItem(); return true; }
            bx += 64;
            if (hit(mx, my, bx, by, 58, bh)) { activeSubEditor = null; return true; }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 256) { activeSubEditor = null; return true; }
            if (key == 261) { clearSelected(); return true; }
            if (key == 257 || key == 335) { applyToItem(); return true; }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            if (sy > 0) prevPage();
            if (sy < 0) nextPage();
            return true;
        }

        private void prevPage() {
            if (page > 0) page--;
        }

        private void nextPage() {
            int maxPage = Math.max(0, (slotTags.size() - 1) / PAGE_SIZE);
            if (page < maxPage) page++;
        }

        private void cycleMode() {
            mode = switch (mode) {
                case CONTAINER -> StorageMode.BUNDLE;
                case BUNDLE -> StorageMode.LEGACY;
                case LEGACY -> StorageMode.CONTAINER;
            };
            message = tr("ankinbt.container.mode") + ": " + modeName();
            msgColor = C2;
        }

        private String modeName() {
            return switch (mode) {
                case BUNDLE -> tr("ankinbt.container.mode.bundle");
                case LEGACY -> tr("ankinbt.container.mode.legacy");
                default -> tr("ankinbt.container.mode.container");
            };
        }

        private ItemStack stackAt(int global) {
            if (global < 0 || global >= slotStacks.size()) return ItemStack.EMPTY;
            ItemStack st = slotStacks.get(global);
            return st == null ? ItemStack.EMPTY : st;
        }

        private void fillFromMainHand() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            ItemStack hand = mc.player.getMainHandItem();
            if (hand == null || hand.isEmpty()) {
                message = tr("ankinbt.container.empty_hand");
                msgColor = ERROR_C;
                return;
            }
            ensureSlots(selectedSlot + 1);
            Optional<CompoundTag> tag = NbtHelper.serializeItemStack(hand);
            if (tag.isEmpty()) {
                message = tr("ankinbt.status.save_error");
                msgColor = ERROR_C;
                return;
            }
            slotTags.set(selectedSlot, copyTag(tag.get()));
            slotStacks.set(selectedSlot, hand.copy());
            message = tr("ankinbt.status.edited");
            msgColor = C2;
        }

        private void openPicker() {
            final int slot = selectedSlot;
            Minecraft.getInstance().setScreen(new ItemPickerScreen(SimpleEditorScreen.this, id -> {
                ensureSlots(slot + 1);
                Item item = com.ankinbt.util.ItemRegistryHelper.resolveItem(id);
                if (item == null) return;
                ItemStack stack = new ItemStack(item, 1);
                Optional<CompoundTag> tag = NbtHelper.serializeItemStack(stack);
                if (tag.isPresent()) {
                    slotTags.set(slot, copyTag(tag.get()));
                    slotStacks.set(slot, stack.copy());
                    message = tr("ankinbt.status.edited");
                    msgColor = C2;
                }
            }));
        }

        private void clearSelected() {
            ensureSlots(selectedSlot + 1);
            slotTags.set(selectedSlot, null);
            slotStacks.set(selectedSlot, ItemStack.EMPTY);
            message = tr("ankinbt.status.deleted");
            msgColor = C2;
        }

        private void loadFromItem() {
            slotTags.clear();
            slotStacks.clear();
            selectedSlot = 0;
            page = 0;
            mode = StorageMode.CONTAINER;

            Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(editStack);
            if (fullOpt.isEmpty()) {
                ensureSlots(PAGE_SIZE);
                return;
            }
            CompoundTag full = fullOpt.get();
            CompoundTag components = getCompound(full, "components");
            ListTag list = components == null ? null : getList(components, "minecraft:container");
            if (list != null && !list.isEmpty()) {
                mode = StorageMode.CONTAINER;
                readContainerList(list);
                return;
            }

            ListTag bundle = components == null ? null : getList(components, "minecraft:bundle_contents");
            if (bundle != null && !bundle.isEmpty()) {
                mode = StorageMode.BUNDLE;
                readBundleList(bundle);
                return;
            }

            CompoundTag tag = getCompound(full, "tag");
            CompoundTag block = tag == null ? null : getCompound(tag, "BlockEntityTag");
            ListTag legacy = block == null ? null : getList(block, "Items");
            if (legacy != null && !legacy.isEmpty()) {
                mode = StorageMode.LEGACY;
                readLegacyList(legacy);
                return;
            }

            ensureSlots(PAGE_SIZE);
        }

        private void readContainerList(ListTag list) {
            int max = PAGE_SIZE;
            for (int i = 0; i < list.size(); i++) {
                Object entry = list.get(i);
                if (!(entry instanceof CompoundTag ct)) continue;
                max = Math.max(max, readInt(ct, "slot", 0) + 1);
            }
            ensureSlots(max);
            for (int i = 0; i < list.size(); i++) {
                Object entry = list.get(i);
                if (!(entry instanceof CompoundTag ct)) continue;
                int slot = readInt(ct, "slot", -1);
                if (slot < 0) continue;
                CompoundTag item = getCompound(ct, "item");
                if (item == null) item = getCompound(ct, "stack");
                if (item == null) continue;
                setSlot(slot, item);
            }
        }

        private void readBundleList(ListTag list) {
            ensureSlots(Math.max(PAGE_SIZE, list.size()));
            for (int i = 0; i < list.size(); i++) {
                Object entry = list.get(i);
                if (entry instanceof CompoundTag ct) setSlot(i, ct);
            }
        }

        private void readLegacyList(ListTag list) {
            int max = PAGE_SIZE;
            for (int i = 0; i < list.size(); i++) {
                Object entry = list.get(i);
                if (!(entry instanceof CompoundTag ct)) continue;
                max = Math.max(max, readInt(ct, "Slot", 0) + 1);
            }
            ensureSlots(max);
            for (int i = 0; i < list.size(); i++) {
                Object entry = list.get(i);
                if (!(entry instanceof CompoundTag ct)) continue;
                int slot = readInt(ct, "Slot", -1);
                if (slot < 0) continue;
                CompoundTag stack = new CompoundTag();
                stack.putString("id", readString(ct, "id", "minecraft:air"));
                stack.putInt("count", Math.max(1, readInt(ct, "Count", 1)));
                CompoundTag legacyTag = getCompound(ct, "tag");
                if (legacyTag != null && !legacyTag.isEmpty()) {
                    CompoundTag components = new CompoundTag();
                    components.put("minecraft:custom_data", copyTag(legacyTag));
                    stack.put("components", components);
                }
                setSlot(slot, stack);
            }
        }

        private void setSlot(int slot, CompoundTag stackTag) {
            ensureSlots(slot + 1);
            slotTags.set(slot, copyTag(stackTag));
            slotStacks.set(slot, decodeStack(stackTag));
        }

        private void ensureSlots(int size) {
            int target = Math.max(PAGE_SIZE, size);
            while (slotTags.size() < target) {
                slotTags.add(null);
                slotStacks.add(ItemStack.EMPTY);
            }
        }

        private ItemStack decodeStack(CompoundTag stackTag) {
            if (stackTag == null || stackTag.isEmpty()) return ItemStack.EMPTY;
            var opt = NbtHelper.deserializeItemStack(stackTag);
            if (opt.isPresent()) return opt.get();
            String id = readString(stackTag, "id", "");
            if (id.isBlank()) return ItemStack.EMPTY;
            Item item = com.ankinbt.util.ItemRegistryHelper.resolveItem(id);
            if (item == null) return ItemStack.EMPTY;
            return new ItemStack(item, Math.max(1, readInt(stackTag, "count", 1)));
        }

        private void applyToItem() {
            Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(editStack);
            if (fullOpt.isEmpty()) {
                message = tr("ankinbt.status.save_error");
                msgColor = ERROR_C;
                return;
            }
            CompoundTag full = fullOpt.get();
            CompoundTag components = getOrCreateCompound(full, "components");

            if (mode == StorageMode.CONTAINER) {
                ListTag out = new ListTag();
                for (int i = 0; i < slotTags.size(); i++) {
                    CompoundTag stack = slotTags.get(i);
                    if (stack == null || isAir(stack)) continue;
                    CompoundTag entry = new CompoundTag();
                    entry.putInt("slot", i);
                    entry.put("item", copyTag(stack));
                    out.add(entry);
                }
                components.put("minecraft:container", out);
                removeKey(components, "minecraft:bundle_contents");
            } else if (mode == StorageMode.BUNDLE) {
                ListTag out = new ListTag();
                for (CompoundTag stack : slotTags) {
                    if (stack == null || isAir(stack)) continue;
                    out.add(copyTag(stack));
                }
                components.put("minecraft:bundle_contents", out);
                removeKey(components, "minecraft:container");
            } else {
                CompoundTag tag = getOrCreateCompound(full, "tag");
                CompoundTag block = getOrCreateCompound(tag, "BlockEntityTag");
                ListTag items = new ListTag();
                for (int i = 0; i < slotTags.size(); i++) {
                    CompoundTag stack = slotTags.get(i);
                    if (stack == null || isAir(stack)) continue;
                    items.add(toLegacyStack(i, stack));
                }
                block.put("Items", items);
                tag.put("BlockEntityTag", block);
                full.put("tag", tag);
            }

            full.put("components", components);
            var outStack = NbtHelper.deserializeItemStack(full);
            if (outStack.isEmpty()) {
                message = tr("ankinbt.status.save_error");
                msgColor = ERROR_C;
                return;
            }

            editStack = outStack.get();
            markDirty();
            loadFromItem();
            message = tr("ankinbt.container.applied");
            msgColor = SUCCESS;
        }

        private CompoundTag toLegacyStack(int slot, CompoundTag stack) {
            CompoundTag out = new CompoundTag();
            out.putByte("Slot", (byte) (slot & 255));
            out.putString("id", readString(stack, "id", "minecraft:air"));
            out.putByte("Count", (byte) Math.max(1, Math.min(127, readInt(stack, "count", 1))));
            CompoundTag components = getCompound(stack, "components");
            if (components != null) {
                CompoundTag custom = getCompound(components, "minecraft:custom_data");
                if (custom != null && !custom.isEmpty()) out.put("tag", copyTag(custom));
            }
            return out;
        }

        private boolean isAir(CompoundTag stack) {
            return "minecraft:air".equals(readString(stack, "id", "minecraft:air"));
        }

        private void renderSmallBtn(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h, String text) {
            boolean hover = hit(mx, my, x, y, w, h);
            g.fill(x, y, x + w, y + h, hover ? BTN_HOVER : BTN_BG);
            String draw = text;
            if (font.width(draw) > w - 8) draw = font.plainSubstrByWidth(draw, w - 12) + "..";
            g.drawString(font, draw, x + (w - font.width(draw)) / 2, y + 6, C2, false);
        }

        private boolean hit(double mx, double my, int x, int y, int w, int h) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }

        private CompoundTag copyTag(CompoundTag source) {
            if (source == null) return null;
            CompoundTag out = new CompoundTag();
            out.merge(source);
            return out;
        }

        private CompoundTag getCompound(CompoundTag parent, String key) {
            if (parent == null || key == null || key.isBlank()) return null;
            Object raw = getTag(parent, key);
            return raw instanceof CompoundTag ct ? ct : null;
        }

        private ListTag getList(CompoundTag parent, String key) {
            if (parent == null || key == null || key.isBlank()) return null;
            Object raw = getTag(parent, key);
            return raw instanceof ListTag lt ? lt : null;
        }

        private CompoundTag getOrCreateCompound(CompoundTag parent, String key) {
            CompoundTag out = getCompound(parent, key);
            if (out == null) {
                out = new CompoundTag();
                parent.put(key, out);
            }
            return out;
        }

        private void removeKey(CompoundTag parent, String key) {
            if (parent == null || key == null || key.isBlank()) return;
            try {
                parent.getClass().getMethod("remove", String.class).invoke(parent, key);
            } catch (Throwable ignored) {}
        }

        private Object getTag(CompoundTag parent, String key) {
            try {
                Object out = parent.getClass().getMethod("get", String.class).invoke(parent, key);
                if (out instanceof Optional<?> opt) return opt.orElse(null);
                return out;
            } catch (Throwable ignored) {
                return null;
            }
        }

        private int readInt(CompoundTag parent, String key, int def) {
            if (parent == null) return def;
            try {
                Object out = parent.getClass().getMethod("getInt", String.class).invoke(parent, key);
                if (out instanceof Number n) return n.intValue();
                if (out instanceof Optional<?> opt && opt.orElse(null) instanceof Number n) return n.intValue();
            } catch (Throwable ignored) {}
            Object raw = getTag(parent, key);
            if (raw != null) {
                try {
                    Object v = raw.getClass().getMethod("getAsInt").invoke(raw);
                    if (v instanceof Number n) return n.intValue();
                } catch (Throwable ignored) {}
            }
            return def;
        }

        private String readString(CompoundTag parent, String key, String def) {
            if (parent == null) return def;
            try {
                Object out = parent.getClass().getMethod("getString", String.class).invoke(parent, key);
                if (out instanceof String s) return s;
                if (out instanceof Optional<?> opt && opt.orElse(null) instanceof String s) return s;
            } catch (Throwable ignored) {}
            Object raw = getTag(parent, key);
            if (raw != null) {
                try {
                    Object v = raw.getClass().getMethod("getAsString").invoke(raw);
                    if (v instanceof String s) return s;
                } catch (Throwable ignored) {}
            }
            return def;
        }

        private enum StorageMode {
            CONTAINER, BUNDLE, LEGACY
        }
    }

    // ==================== INNER CLASSES ====================

    static class ActionRow {
        final String label; final String currentValue; final Runnable action; final int labelColor;
        final Runnable moveUp; final Runnable moveDown; final ItemStack icon; final Runnable deleteAction;
        ActionRow(String label, String currentValue, Runnable action) { this(label, currentValue, action, C1, null, null, ItemStack.EMPTY); }
        ActionRow(String label, String currentValue, Runnable action, int labelColor) { this(label, currentValue, action, labelColor, null, null, ItemStack.EMPTY); }
        ActionRow(String label, String currentValue, Runnable action, int labelColor, Runnable moveUp, Runnable moveDown) { this(label, currentValue, action, labelColor, moveUp, moveDown, ItemStack.EMPTY); }
        ActionRow(String label, String currentValue, Runnable action, int labelColor, Runnable moveUp, Runnable moveDown, ItemStack icon) {
            this(label, currentValue, action, labelColor, moveUp, moveDown, icon, null);
        }
        ActionRow(String label, String currentValue, Runnable action, int labelColor, Runnable moveUp, Runnable moveDown, ItemStack icon, Runnable deleteAction) {
            this.label = label; this.currentValue = currentValue; this.action = action; this.labelColor = labelColor;
            this.moveUp = moveUp; this.moveDown = moveDown; this.icon = icon == null ? ItemStack.EMPTY : icon;
            this.deleteAction = deleteAction;
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
        default boolean mouseDragged(double mx, double my, int button, double dragX, double dragY, int x, int y, int w, int h) { return false; }
    }

    class InventorySwitchSubEditor implements SubEditor {
        private static final int COLS = 9;
        private static final int ROWS = 4;

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 16, 246);
            int dh = Math.min(h - 16, 150);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.inventory_pick"), dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.inventory_hint"), dx + 10, dy + 30, C3, false);

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.message.no_item"), dx + 10, dy + 48, ERROR_C, false);
                return;
            }

            int currentSlot = currentEditedSlot();
            int gridX = dx + 10;
            int gridY = dy + 46;
            int cell = 20;
            int gap = 4;

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int logical = r < 3 ? (9 + r * 9 + c) : c;
                    ItemStack stack = mc.player.getInventory().getItem(logical);
                    int sx = gridX + c * (cell + gap);
                    int sy = gridY + r * (cell + gap);
                    boolean hover = mx >= sx && mx < sx + 18 && my >= sy && my < sy + 18;
                    boolean active = logical == currentSlot;
                    int bg = active ? SELECT_BG : (hover ? BTN_HOVER : BTN_BG);
                    int edge = active ? ACCENT : BORDER;
                    g.fill(sx, sy, sx + 18, sy + 18, bg);
                    drawBorder(g, sx, sy, 18, 18, edge);
                    if (stack != null && !stack.isEmpty()) {
                        g.renderItem(stack, sx + 1, sy + 1);
                        if (hover) {
                            VersionCompat.get().renderTooltip(g, font, stack.getHoverName(), mx, my);
                        }
                    }
                }
            }

            int by = dy + dh - 26;
            int bw = 58;
            int bh = 18;
            int bx = dx + dw - bw - 10;
            boolean hover = mx >= bx && mx < bx + bw && my >= by && my < by + bh;
            g.fill(bx, by, bx + bw, by + bh, hover ? BTN_HOVER : BTN_BG);
            drawBorder(g, bx, by, bw, bh, BORDER);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), bx + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 5, C1, false);
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

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int logical = r < 3 ? (9 + r * 9 + c) : c;
                    int sx = gridX + c * (cell + gap);
                    int sy = gridY + r * (cell + gap);
                    if (mx >= sx && mx < sx + 18 && my >= sy && my < sy + 18) {
                        switchToInventorySlot(logical);
                        return true;
                    }
                }
            }

            int by = dy + dh - 26;
            int bw = 58;
            int bh = 18;
            int bx = dx + dw - bw - 10;
            if (mx >= bx && mx < bx + bw && my >= by && my < by + bh) {
                activeSubEditor = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 256) {
                activeSubEditor = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) { return false; }
    }

    // ==================== INLINE FIELD EDITOR (with color code support) ====================

    class InlineFieldEditor implements SubEditor {
        final String field;
        final FlatEditBox inputBox;
        final String initialValue;
        String error = null;
        final boolean isLore;
        boolean initialCursorSynced = false;

        InlineFieldEditor(String field, String currentValue, boolean isLore) {
            this.field = field; this.isLore = isLore;
            this.initialValue = currentValue != null ? currentValue : "";
            this.inputBox = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 22, Component.empty());
            this.inputBox.setMaxLength(2048);
            this.inputBox.setResponder(value -> error = null);
            this.inputBox.setFocused(true);
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            boolean colorEditable = isLore || field.equals("rename");
            int dw = Math.min(w - 20, 360), dh = colorEditable ? 148 : 104;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);

            String title = getFieldLabel(field);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            // Input box
            int ix = dx + 10, iy = dy + 30, iw = dw - 20, ih = 22;
            inputBox.setX(ix);
            inputBox.setY(iy);
            inputBox.setWidth(iw);
            if (!initialCursorSynced) {
                inputBox.setValue(initialValue);
                int end = inputBox.getValue().length();
                boolean fits = font.width(inputBox.getValue()) <= Math.max(1, iw - 8);
                inputBox.setCursorPosition(fits ? end : 0);
                inputBox.setHighlightPos(fits ? end : 0);
                initialCursorSynced = true;
            }
            inputBox.setFocused(true);
            inputBox.renderWidget(g, mx, my, 0.0f);

            // Preview for name/lore color codes
            String input = inputBox.getValue();
            if (colorEditable && !input.isEmpty()) {
                Component preview = colorCodedToComponent(input);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.preview") + ": ", ix, iy + ih + 4, C3, false);
                int previewX = ix + font.width(tr("ankinbt.simple.preview") + ": ");
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, preview, previewX, iy + ih + 4, C1, false);
            }

            if (error != null) com.ankinbt.compat.VersionCompat.get().drawString(g, font, error, ix, iy + ih + (colorEditable ? 16 : 4), ERROR_C, false);

            // Color palette button for text fields
            if (colorEditable) {
                int palX = dx + dw - 80, palY = dy + 6;
                boolean palHover = mx >= palX && mx < palX + 70 && my >= palY && my < palY + 16;
                g.fill(palX, palY, palX + 70, palY + 16, palHover ? BTN_HOVER : BTN_BG);
                String palLabel = tr("ankinbt.simple.color_palette");
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, palLabel, palX + (70 - font.width(palLabel)) / 2, palY + 4, C2, false);
            }

            // Buttons
            int by = dy + dh - 28, bw = 70, bh = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
            g.fill(cancelX, by, cancelX + bw, by + bh, ch ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, C2, false);

            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh;
            g.fill(okX, by, okX + bw, by + bh, oh ? ACCENT : 0xFF4F46E5);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.apply"), okX + (bw - font.width(tr("ankinbt.edit.apply"))) / 2, by + 6, C1, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            boolean colorEditable = isLore || field.equals("rename");
            int dw = Math.min(w - 20, 360), dh = colorEditable ? 148 : 104;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;

            // Color palette button
            if (colorEditable) {
                int palX = dx + dw - 80, palY = dy + 6;
                if (mx >= palX && mx < palX + 70 && my >= palY && my < palY + 16) {
                    activeSubEditor = new LoreColorInsertEditor(this);
                    return true;
                }
            }

            int ix = dx + 10, iy = dy + 30, iw = dw - 20, ih = 22;
            inputBox.setX(ix);
            inputBox.setY(iy);
            inputBox.setWidth(iw);
            if (inputBox.mouseClicked(new MouseButtonEvent(mx, my, new net.minecraft.client.input.MouseButtonInfo(btn, 0)), false)) {
                inputBox.setFocused(true);
                return true;
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
            if (inputBox.keyPressed(new KeyEvent(key, scan, mod))) return true;
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (inputBox.charTyped(new CharacterEvent(c))) return true;
            return true;
        }

        void insertAtCursor(String text) {
            String selected = inputBox.getHighlighted();
            if (selected != null && !selected.isEmpty() && (text == null || !text.endsWith("r"))) {
                inputBox.insertText((text == null ? "" : text) + selected + "&r");
            } else {
                inputBox.insertText(text == null ? "" : text);
            }
        }

        private void apply() {
            String input = inputBox.getValue();
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

    // ==================== LORE TEXT EDITOR (multi-line) ====================

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
                    box.renderWidget(g, mx, my, 0.0f);
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
                        box.mouseClicked(new MouseButtonEvent(mx, my, new net.minecraft.client.input.MouseButtonInfo(btn, 0)), false);
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
            if (box.keyPressed(new KeyEvent(key, scan, mod))) {
                this.syncLine(this.activeLine);
                return true;
            }
            this.syncLine(this.activeLine);
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            FlatEditBox box = this.activeBox();
            if (box.charTyped(new CharacterEvent(c))) {
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

    class LoreColorInsertEditorForText implements SubEditor {
        final LoreTextEditorSubEditor textEditor;
        final InlineFieldEditor tempParent;
        private int hoveredColor = -1;

        LoreColorInsertEditorForText(LoreTextEditorSubEditor textEditor, InlineFieldEditor tempParent) {
            this.textEditor = textEditor;
            this.tempParent = tempParent;
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340), dh = 260;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.color_palette"), dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            // Color grid - 8x2 layout with larger cells
            int gridX = dx + 12, gridY = dy + 28;
            int cellW = (dw - 24) / 8, cellH = 28;
            hoveredColor = -1;

            // Row labels
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.palette_bright"), dx + 10, gridY - 1, C3, false);
            // Bright colors (a-f + white)
            int[] brightOrder = {6, 14, 10, 11, 9, 13, 12, 15}; // gold, yellow, green, aqua, blue, pink, red, white
            for (int i = 0; i < 8; i++) {
                int ci = brightOrder[i];
                int cx = gridX + i * cellW, cy = gridY + 8;
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                if (hover) hoveredColor = ci;
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (hover) {
                    drawBorder(g, cx, cy, cellW - 2, cellH, 0xFFFFFFFF);
                    // Tooltip
                    String lang = Minecraft.getInstance().options.languageCode;
                    String tip = "&" + MC_COLOR_CODES[ci] + " " + (lang != null && lang.startsWith("zh") ? MC_COLOR_NAMES_ZH[ci] : MC_COLOR_CODES[ci]);
                    int tipW = font.width(tip) + 8;
                    g.fill(mx + 8, my - 14, mx + 8 + tipW, my - 1, 0xF0101020);
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, tip, mx + 12, my - 12, C1, false);
                }
            }

            // Dark colors
            int darkY = gridY + cellH + 14;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.palette_dark"), dx + 10, darkY - 1, C3, false);
            int[] darkOrder = {0, 8, 4, 5, 1, 3, 2, 7}; // black, dark_gray, dark_red, purple, dark_blue, dark_aqua, dark_green, gray
            for (int i = 0; i < 8; i++) {
                int ci = darkOrder[i];
                int cx = gridX + i * cellW, cy = darkY + 8;
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                if (hover) hoveredColor = ci;
                int bgColor = MC_COLORS[ci] | 0xFF000000;
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, bgColor);
                // Light border for dark colors
                if (ci == 0 || ci == 8) drawBorder(g, cx, cy, cellW - 2, cellH, 0x40FFFFFF);
                if (hover) {
                    drawBorder(g, cx, cy, cellW - 2, cellH, 0xFFFFFFFF);
                    String lang = Minecraft.getInstance().options.languageCode;
                    String tip = "&" + MC_COLOR_CODES[ci] + " " + (lang != null && lang.startsWith("zh") ? MC_COLOR_NAMES_ZH[ci] : MC_COLOR_CODES[ci]);
                    int tipW = font.width(tip) + 8;
                    g.fill(mx + 8, my - 14, mx + 8 + tipW, my - 1, 0xF0101020);
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, tip, mx + 12, my - 12, C1, false);
                }
            }

            // Format codes - pill-style buttons
            int fmtY = darkY + cellH + 20;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.format_codes"), dx + 10, fmtY, C2, false);
            fmtY += 14;
            int fmtX = gridX;
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; i++) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.width(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) { fmtX = gridX; fmtY += 22; }
                boolean hover = mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18;
                g.fill(fmtX, fmtY, fmtX + pillW, fmtY + 18, hover ? ACCENT : 0x30FFFFFF);
                // Rounded feel with border
                drawBorder(g, fmtX, fmtY, pillW, 18, hover ? ACCENT : 0x20FFFFFF);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, fLabel, fmtX + 7, fmtY + 5, hover ? C1 : C2, false);
                fmtX += pillW + 6;
            }

            // Back button
            int backY = dy + dh - 26, backW = 70, backX = dx + (dw - backW) / 2;
            boolean bh2 = mx >= backX && mx < backX + backW && my >= backY && my < backY + 20;
            g.fill(backX, backY, backX + backW, backY + 20, bh2 ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.back"), backX + (backW - font.width(tr("ankinbt.simple.back"))) / 2, backY + 6, C2, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340), dh = 260;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;

            int gridX = dx + 12, gridY = dy + 28;
            int cellW = (dw - 24) / 8, cellH = 28;

            // Bright colors
            int[] brightOrder = {6, 14, 10, 11, 9, 13, 12, 15};
            for (int i = 0; i < 8; i++) {
                int ci = brightOrder[i];
                int cx = gridX + i * cellW, cy = gridY + 8;
                if (mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH) {
                    textEditor.insertAtCursor("&" + MC_COLOR_CODES[ci]);
                    activeSubEditor = textEditor;
                    return true;
                }
            }

            // Dark colors
            int darkY = gridY + cellH + 14;
            int[] darkOrder = {0, 8, 4, 5, 1, 3, 2, 7};
            for (int i = 0; i < 8; i++) {
                int ci = darkOrder[i];
                int cx = gridX + i * cellW, cy = darkY + 8;
                if (mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH) {
                    textEditor.insertAtCursor("&" + MC_COLOR_CODES[ci]);
                    activeSubEditor = textEditor;
                    return true;
                }
            }

            // Format codes
            int fmtY = darkY + cellH + 20 + 14;
            int fmtX = gridX;
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; i++) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.width(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) { fmtX = gridX; fmtY += 22; }
                if (mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18) {
                    textEditor.insertAtCursor("&" + MC_FORMAT_CODES[i]);
                    activeSubEditor = textEditor;
                    return true;
                }
                fmtX += pillW + 6;
            }

            // Back button
            int backY = dy + dh - 26, backW = 70, backX = dx + (dw - backW) / 2;
            if (mx >= backX && mx < backX + backW && my >= backY && my < backY + 20) {
                activeSubEditor = textEditor;
                return true;
            }
            return true;
        }

        @Override public boolean keyPressed(int key, int scan, int mod) { return true; }
        @Override public boolean charTyped(char c, int mod) { return false; }
    }

    // ==================== LORE COLOR INSERT EDITOR ====================

    class LoreColorInsertEditor implements SubEditor {
        final InlineFieldEditor parent;
        LoreColorInsertEditor(InlineFieldEditor parent) { this.parent = parent; }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340), dh = 260;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.color_palette"), dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            int gridX = dx + 12, gridY = dy + 28;
            int cellW = (dw - 24) / 8, cellH = 28;

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.palette_bright"), dx + 10, gridY - 1, C3, false);
            int[] brightOrder = {6, 14, 10, 11, 9, 13, 12, 15};
            for (int i = 0; i < 8; i++) {
                int ci = brightOrder[i];
                int cx = gridX + i * cellW, cy = gridY + 8;
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (hover) drawBorder(g, cx, cy, cellW - 2, cellH, 0xFFFFFFFF);
            }

            int darkY = gridY + cellH + 14;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.palette_dark"), dx + 10, darkY - 1, C3, false);
            int[] darkOrder = {0, 8, 4, 5, 1, 3, 2, 7};
            for (int i = 0; i < 8; i++) {
                int ci = darkOrder[i];
                int cx = gridX + i * cellW, cy = darkY + 8;
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (ci == 0 || ci == 8) drawBorder(g, cx, cy, cellW - 2, cellH, 0x40FFFFFF);
                if (hover) drawBorder(g, cx, cy, cellW - 2, cellH, 0xFFFFFFFF);
            }

            int fmtY = darkY + cellH + 20;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.format_codes"), dx + 10, fmtY, C2, false);
            fmtY += 14;
            int fmtX = gridX;
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; i++) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.width(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) { fmtX = gridX; fmtY += 22; }
                boolean hover = mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18;
                g.fill(fmtX, fmtY, fmtX + pillW, fmtY + 18, hover ? ACCENT : 0x30FFFFFF);
                drawBorder(g, fmtX, fmtY, pillW, 18, hover ? ACCENT : 0x20FFFFFF);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, fLabel, fmtX + 7, fmtY + 5, hover ? C1 : C2, false);
                fmtX += pillW + 6;
            }

            int backY = dy + dh - 26, backW = 70, backX = dx + (dw - backW) / 2;
            boolean bh2 = mx >= backX && mx < backX + backW && my >= backY && my < backY + 20;
            g.fill(backX, backY, backX + backW, backY + 20, bh2 ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.back"), backX + (backW - font.width(tr("ankinbt.simple.back"))) / 2, backY + 6, C2, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 340), dh = 260;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;

            int gridX = dx + 12, gridY = dy + 28;
            int cellW = (dw - 24) / 8, cellH = 28;

            int[] brightOrder = {6, 14, 10, 11, 9, 13, 12, 15};
            for (int i = 0; i < 8; i++) {
                int ci = brightOrder[i];
                int cx = gridX + i * cellW, cy = gridY + 8;
                if (mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH) {
                    parent.insertAtCursor("&" + MC_COLOR_CODES[ci]);
                    activeSubEditor = parent;
                    return true;
                }
            }

            int darkY = gridY + cellH + 14;
            int[] darkOrder = {0, 8, 4, 5, 1, 3, 2, 7};
            for (int i = 0; i < 8; i++) {
                int ci = darkOrder[i];
                int cx = gridX + i * cellW, cy = darkY + 8;
                if (mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH) {
                    parent.insertAtCursor("&" + MC_COLOR_CODES[ci]);
                    activeSubEditor = parent;
                    return true;
                }
            }

            int fmtY = darkY + cellH + 20 + 14;
            int fmtX = gridX;
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; i++) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.width(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) { fmtX = gridX; fmtY += 22; }
                if (mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18) {
                    parent.insertAtCursor("&" + MC_FORMAT_CODES[i]);
                    activeSubEditor = parent;
                    return true;
                }
                fmtX += pillW + 6;
            }

            int backY = dy + dh - 26, backW = 70, backX = dx + (dw - backW) / 2;
            if (mx >= backX && mx < backX + backW && my >= backY && my < backY + 20) {
                activeSubEditor = parent;
                return true;
            }
            return true;
        }

        @Override public boolean keyPressed(int key, int scan, int mod) { return true; }
        @Override public boolean charTyped(char c, int mod) { return false; }
    }

    class PotionPickerSubEditor implements SubEditor {
        private final FlatEditBox searchBox;
        private final List<String> filtered = new ArrayList<>();
        private int hoverIdx = -1;
        private int selectedIdx = 0;
        private int scrollOff = 0;

        PotionPickerSubEditor() {
            searchBox = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 18, Component.empty());
            searchBox.setMaxLength(128);
            searchBox.setHint(Component.translatable("ankinbt.search.hint"));
            searchBox.setResponder(value -> filter());
            searchBox.setFocused(true);
            filter();
            String current = getPotionId();
            for (int i = 0; i < filtered.size(); i++) {
                if (filtered.get(i).equals(current)) {
                    selectedIdx = i;
                    break;
                }
            }
        }

        private void filter() {
            String q = searchBox.getValue().toLowerCase(Locale.ROOT);
            filtered.clear();
            for (String id : POTION_IDS) {
                if (q.isEmpty()
                        || id.toLowerCase(Locale.ROOT).contains(q)
                        || potionDisplayName(id).toLowerCase(Locale.ROOT).contains(q)) {
                    filtered.add(id);
                }
            }
            selectedIdx = Math.min(selectedIdx, Math.max(0, filtered.size() - 1));
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, filtered.size() - 8)));
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 380), dh = Math.min(h - 20, 250);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, ACCENT);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.pick_potion"), dx + 10, dy + 8, C1, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            int sx = dx + 10, sy = dy + 30, sw = dw - 20, sh = 18;
            searchBox.setX(sx);
            searchBox.setY(sy);
            searchBox.setWidth(sw);
            searchBox.setFocused(true);
            searchBox.renderWidget(g, mx, my, 0.0f);

            int listY = sy + sh + 6;
            int maxItems = Math.max(1, (dh - 92) / 18);
            hoverIdx = -1;
            int end = Math.min(filtered.size(), scrollOff + maxItems);
            for (int i = scrollOff; i < end; i++) {
                int ry = listY + (i - scrollOff) * 18;
                boolean hover = mx >= dx + 10 && mx < dx + dw - 10 && my >= ry && my < ry + 18;
                if (hover) hoverIdx = i;
                boolean selected = i == selectedIdx;
                g.fill(dx + 10, ry, dx + dw - 10, ry + 17, selected ? SELECT_BG : (hover ? HOVER : 0x00000000));
                String id = filtered.get(i);
                g.renderItem(ItemEditorVisuals.potionRowIcon(id), dx + 14, ry + 1);
                String label = potionDisplayName(id);
                if (font.width(label) > dw - 64) label = font.plainSubstrByWidth(label, dw - 68) + "..";
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, dx + 36, ry + 5, selected ? C1 : C2, false);
            }

            int by = dy + dh - 28, bw = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw, by + bh2, ch ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, C2, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw, by + bh2, oh ? accentColor() : accentColor());
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.apply"), okX + (bw - font.width(tr("ankinbt.edit.apply"))) / 2, by + 6, C1, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, 380), dh = Math.min(h - 20, 250);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            int sx = dx + 10, sy = dy + 30, sw = dw - 20;
            searchBox.setX(sx);
            searchBox.setY(sy);
            searchBox.setWidth(sw);
            if (searchBox.mouseClicked(new MouseButtonEvent(mx, my, new net.minecraft.client.input.MouseButtonInfo(btn, 0)), false)) {
                searchBox.setFocused(true);
                return true;
            }
            int by = dy + dh - 28, bw = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            if (mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2) { activeSubEditor = null; return true; }
            int okX = dx + dw / 2 + 6;
            if (mx >= okX && mx < okX + bw && my >= by && my < by + bh2) { confirm(); return true; }
            if (hoverIdx >= 0 && hoverIdx < filtered.size()) {
                selectedIdx = hoverIdx;
                if (btn == 0) confirm();
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { confirm(); return true; }
            if (searchBox.keyPressed(new KeyEvent(key, scan, mod))) return true;
            if (key == 264 && selectedIdx < filtered.size() - 1) { selectedIdx++; if (selectedIdx >= scrollOff + 8) scrollOff++; return true; }
            if (key == 265 && selectedIdx > 0) { selectedIdx--; if (selectedIdx < scrollOff) scrollOff = selectedIdx; return true; }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (searchBox.charTyped(new CharacterEvent(c))) return true;
            return true;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            scrollOff -= (int) sy * 3;
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, filtered.size() - 8)));
            return true;
        }

        private void confirm() {
            if (selectedIdx >= 0 && selectedIdx < filtered.size()) {
                setPotionBase(filtered.get(selectedIdx));
                activeSubEditor = null;
            }
        }
    }

    class PotionEffectSubEditor implements SubEditor {
        private final FlatEditBox searchBox;
        private final FlatEditBox durationBox;
        private final FlatEditBox amplifierBox;
        private final List<String> filtered = new ArrayList<>();
        private final Map<String, EffectDraft> selectedEffects = new LinkedHashMap<>();
        private String activeEffectId = null;
        private int focusField = 0;
        private int hoverIdx = -1;
        private int selectedIdx = 0;
        private int scrollOff = 0;
        private int selectedScrollOff = 0;
        private int lastMouseX = 0;
        private int lastMouseY = 0;
        private boolean ambient = false;
        private boolean particles = true;
        private boolean icon = true;

        private class EffectDraft {
            String duration;
            String amplifier;
            boolean ambient;
            boolean particles;
            boolean icon;

            EffectDraft(String duration, String amplifier, boolean ambient, boolean particles, boolean icon) {
                this.duration = duration;
                this.amplifier = amplifier;
                this.ambient = ambient;
                this.particles = particles;
                this.icon = icon;
            }
        }

        PotionEffectSubEditor() {
            searchBox = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 18, Component.empty());
            searchBox.setMaxLength(128);
            searchBox.setHint(Component.translatable("ankinbt.search.hint"));
            searchBox.setResponder(value -> filter());
            searchBox.setFocused(true);
            durationBox = numericBox("600");
            amplifierBox = numericBox("0");
            loadExistingEffects();
            filter();
            if (activeEffectId == null && !selectedEffects.isEmpty()) {
                setActiveEffect(selectedEffects.keySet().iterator().next());
            }
        }

        private FlatEditBox numericBox(String value) {
            FlatEditBox box = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 18, Component.empty());
            box.setMaxLength(8);
            box.setValue(value);
            box.setResponder(v -> syncActiveDraft());
            return box;
        }

        private void loadExistingEffects() {
            CompoundTag potion = getPotionContentsTag();
            ListTag list = getListTag(potion, "custom_effects");
            if (list == null || list.isEmpty()) return;
            for (Object raw : list) {
                if (!(raw instanceof CompoundTag effect)) continue;
                String id = readStringTag(effect, "id", "");
                if (id == null || id.isBlank()) continue;
                int dur = Math.max(1, Math.min(POTION_MAX_DURATION, readIntTag(effect, "duration", 600)));
                int amp = Math.max(0, Math.min(POTION_MAX_AMPLIFIER, readIntTag(effect, "amplifier", 0)));
                boolean amb = readBooleanTag(effect, "ambient", false);
                boolean showParticles = readBooleanTag(effect, "show_particles", true);
                boolean showIcon = readBooleanTag(effect, "show_icon", true);
                selectedEffects.put(id, new EffectDraft(String.valueOf(dur), String.valueOf(amp), amb, showParticles, showIcon));
                if (selectedEffects.size() >= POTION_MAX_CUSTOM_EFFECTS) break;
            }
        }

        private void filter() {
            String q = searchBox.getValue().toLowerCase(Locale.ROOT);
            filtered.clear();
            for (String id : EFFECT_IDS) {
                if (q.isEmpty()
                        || id.toLowerCase(Locale.ROOT).contains(q)
                        || effectDisplayName(id).toLowerCase(Locale.ROOT).contains(q)) {
                    filtered.add(id);
                }
            }
            selectedIdx = Math.min(selectedIdx, Math.max(0, filtered.size() - 1));
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, filtered.size() - 7)));
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            lastMouseX = mx;
            lastMouseY = my;
            int dw = Math.min(w - 20, Math.min(760, Math.max(520, w - 36))), dh = Math.min(h - 20, Math.min(360, Math.max(300, h - 30)));
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
            drawBorder(g, dx, dy, dw, dh, accentColor());
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.pick_effect"), dx + 10, dy + 8, C1, false);
            String count = String.format(tr("ankinbt.simple.potion_selected_count"), selectedEffects.size());
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, count, dx + dw - 10 - font.width(count), dy + 8, C2, false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, BORDER);

            int leftX = dx + 10, leftW = Math.max(230, Math.min(320, (dw - 34) * 45 / 100));
            int rightX = leftX + leftW + 12, rightW = dw - (rightX - dx) - 10;
            int sx = leftX, sy = dy + 30, sw = leftW, sh = 18;
            renderSmallFlatEditBox(g, searchBox, sx, sy, sw, sh, focusField == 0, mx, my);

            int listY = sy + sh + 6;
            int bottomSafeY = dy + dh - 34;
            int maxItems = Math.max(5, Math.min(9, (bottomSafeY - listY) / 18));
            hoverIdx = -1;
            int end = Math.min(filtered.size(), scrollOff + maxItems);
            for (int i = scrollOff; i < end; i++) {
                String id = filtered.get(i);
                int ry = listY + (i - scrollOff) * 18;
                boolean hover = mx >= leftX && mx < leftX + leftW && my >= ry && my < ry + 18;
                if (hover) hoverIdx = i;
                boolean focused = i == selectedIdx;
                boolean checked = selectedEffects.containsKey(id);
                boolean active = id.equals(activeEffectId);
                g.fill(leftX, ry, leftX + leftW, ry + 17, active ? SELECT_BG : (checked ? 0x382A2A42 : (hover ? HOVER : 0x00000000)));
                if (focused && !active) drawBorder(g, leftX, ry, leftW, 17, 0x668EA0FF);
                g.renderItem(ItemEditorVisuals.effectIconStack(id), leftX + 4, ry + 1);
                String label = effectDisplayName(id);
                if (font.width(label) > leftW - 48) label = font.plainSubstrByWidth(label, leftW - 52) + "..";
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, leftX + 26, ry + 5, checked ? C1 : C2, false);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, checked ? "-" : "+", leftX + leftW - 14, ry + 5, checked ? C1 : C3, false);
            }

            g.fill(rightX, sy, rightX + rightW, sy + 18, 0x4012121E);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, count, rightX + 6, sy + 5, C2, false);
            int selectedY = sy + 24;
            int selectedListH = Math.min(92, Math.max(44, dh - 218));
            int selectedRows = Math.max(1, selectedListH / 22);
            selectedScrollOff = Math.max(0, Math.min(selectedScrollOff, Math.max(0, selectedEffects.size() - selectedRows)));
            int idx = 0;
            int drawn = 0;
            for (Map.Entry<String, EffectDraft> entry : selectedEffects.entrySet()) {
                if (idx++ < selectedScrollOff) continue;
                if (drawn >= selectedRows) break;
                String id = entry.getKey();
                EffectDraft draft = entry.getValue();
                int ry = selectedY + drawn * 22;
                boolean active = id.equals(activeEffectId);
                g.fill(rightX, ry, rightX + rightW, ry + 20, active ? SELECT_BG : 0x2412121E);
                g.renderItem(ItemEditorVisuals.effectIconStack(id), rightX + 4, ry + 2);
                String label = effectDisplayName(id);
                int metaW = font.width(draft.duration + " / " + draft.amplifier);
                if (font.width(label) > rightW - 58 - metaW) label = font.plainSubstrByWidth(label, Math.max(40, rightW - 62 - metaW)) + "..";
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, rightX + 26, ry + 6, active ? C1 : C2, false);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, draft.duration + " / " + draft.amplifier, rightX + rightW - 28 - metaW, ry + 6, C3, false);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, "X", rightX + rightW - 14, ry + 6, C2, false);
                drawn++;
            }
            if (selectedEffects.size() > selectedRows) {
                int trackX = rightX + rightW - 4;
                int trackY = selectedY;
                int trackH = selectedRows * 22 - 2;
                g.fill(trackX, trackY, trackX + 3, trackY + trackH, SB_TRACK);
                int thumbH = Math.max(12, trackH * selectedRows / Math.max(selectedRows, selectedEffects.size()));
                int maxScroll = Math.max(1, selectedEffects.size() - selectedRows);
                int thumbY = trackY + (trackH - thumbH) * selectedScrollOff / maxScroll;
                g.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, accentColor());
            }

            int formY = selectedY + selectedListH + 12;
            if (activeEffectId == null || !selectedEffects.containsKey(activeEffectId)) {
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.select_effect_first"), rightX + 6, formY + 8, C3, false);
            } else {
                String activeLabel = effectDisplayName(activeEffectId);
                if (font.width(activeLabel) > rightW - 10) activeLabel = font.plainSubstrByWidth(activeLabel, rightW - 14) + "..";
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, activeLabel, rightX + 6, formY, C1, false);
                int editY = formY + 18;
                int fieldW = Math.max(50, Math.min(82, (rightW - 94) / 2));
                int durLabelW = Math.min(46, Math.max(32, font.width(tr("ankinbt.simple.effect_duration"))));
                int ampLabelW = Math.min(38, Math.max(26, font.width(tr("ankinbt.simple.effect_amplifier"))));
                int durX = rightX + 6 + durLabelW + 4;
                int ampX = Math.min(rightX + rightW - fieldW, durX + fieldW + 42);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.effect_duration"), rightX + 6, editY + 5, C2, false);
                renderSmallFlatEditBox(g, durationBox, durX, editY, fieldW, 18, focusField == 1, mx, my);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.effect_amplifier"), ampX - ampLabelW - 4, editY + 5, C2, false);
                renderSmallFlatEditBox(g, amplifierBox, ampX, editY, fieldW, 18, focusField == 2, mx, my);
                int toggleY = editY + 24;
                int toggleGap = 4;
                int toggleW = Math.max(42, (rightW - 12 - toggleGap * 2) / 3);
                renderToggle(g, font, mx, my, rightX + 6, toggleY, toggleW, tr("ankinbt.simple.effect_ambient"), ambient);
                renderToggle(g, font, mx, my, rightX + 6 + toggleW + toggleGap, toggleY, toggleW, tr("ankinbt.simple.effect_particles"), particles);
                renderToggle(g, font, mx, my, rightX + 6 + (toggleW + toggleGap) * 2, toggleY, toggleW, tr("ankinbt.simple.effect_icon"), icon);
            }

            int by = dy + dh - 28, bw = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw, by + bh2, ch ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, C2, false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw, by + bh2, oh ? accentColor() : accentColor());
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.add.confirm"), okX + (bw - font.width(tr("ankinbt.add.confirm"))) / 2, by + 6, C1, false);
        }

        private void renderSmallFlatEditBox(GuiGraphics g, FlatEditBox box, int x, int y, int w, int h, boolean focused, int mx, int my) {
            box.setX(x);
            box.setY(y);
            box.setWidth(w);
            box.setFocused(focused);
            box.renderWidget(g, mx, my, 0.0f);
        }

        private void renderToggle(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int bw, String label, boolean on) {
            boolean hover = mx >= x && mx < x + bw && my >= y && my < y + 18;
            g.fill(x, y, x + bw, y + 18, on ? accentColor() : (hover ? BTN_HOVER : BTN_BG));
            String text = label;
            if (font.width(text) > bw - 10) text = font.plainSubstrByWidth(text, Math.max(8, bw - 14)) + "..";
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, x + (bw - font.width(text)) / 2, y + 5, on ? C1 : C2, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(w - 20, Math.min(760, Math.max(520, w - 36))), dh = Math.min(h - 20, Math.min(360, Math.max(300, h - 30)));
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            int leftX = dx + 10, leftW = Math.max(230, Math.min(320, (dw - 34) * 45 / 100));
            int rightX = leftX + leftW + 12, rightW = dw - (rightX - dx) - 10;
            int sx = leftX, sy = dy + 30, sw = leftW, sh = 18;
            setBoxBounds(searchBox, sx, sy, sw);
            if (searchBox.mouseClicked(new MouseButtonEvent(mx, my, new net.minecraft.client.input.MouseButtonInfo(btn, 0)), false)) { setFocusField(0); return true; }
            int listY = sy + sh + 6;
            int bottomSafeY = dy + dh - 34;
            int maxItems = Math.max(5, Math.min(9, (bottomSafeY - listY) / 18));
            if (mx >= leftX && mx < leftX + leftW && my >= listY && my < listY + maxItems * 18) {
                int hitIdx = scrollOff + Math.max(0, (int) ((my - listY) / 18));
                if (hitIdx >= 0 && hitIdx < filtered.size()) {
                    selectedIdx = hitIdx;
                    selectEffect(filtered.get(hitIdx), btn);
                    return true;
                }
            }

            int selectedY = sy + 24;
            int selectedListH = Math.min(92, Math.max(44, dh - 218));
            int selectedRows = Math.max(1, selectedListH / 22);
            int selectedIdx = 0;
            for (String id : new ArrayList<>(selectedEffects.keySet())) {
                if (selectedIdx++ < selectedScrollOff) continue;
                int visibleIdx = selectedIdx - selectedScrollOff - 1;
                if (visibleIdx >= selectedRows) break;
                int ry = selectedY + visibleIdx * 22;
                if (mx >= rightX && mx < rightX + rightW && my >= ry && my < ry + 20) {
                    if (mx >= rightX + rightW - 24) removeSelectedEffect(id);
                    else setActiveEffect(id);
                    return true;
                }
            }

            int formY = selectedY + selectedListH + 12;
            int editY = formY + 18;
            int fieldW = Math.max(50, Math.min(82, (rightW - 94) / 2));
            int durLabelW = Math.min(46, Math.max(32, font.width(tr("ankinbt.simple.effect_duration"))));
            int ampLabelW = Math.min(38, Math.max(26, font.width(tr("ankinbt.simple.effect_amplifier"))));
            int durX = rightX + 6 + durLabelW + 4;
            int ampX = Math.min(rightX + rightW - fieldW, durX + fieldW + 42);
            setBoxBounds(durationBox, durX, editY, fieldW);
            if (durationBox.mouseClicked(new MouseButtonEvent(mx, my, new net.minecraft.client.input.MouseButtonInfo(btn, 0)), false)) { setFocusField(1); return true; }
            setBoxBounds(amplifierBox, ampX, editY, fieldW);
            if (amplifierBox.mouseClicked(new MouseButtonEvent(mx, my, new net.minecraft.client.input.MouseButtonInfo(btn, 0)), false)) { setFocusField(2); return true; }
            int toggleY = editY + 24;
            int toggleGap = 4;
            int toggleW = Math.max(42, (rightW - 12 - toggleGap * 2) / 3);
            if (clickToggle(mx, my, rightX + 6, toggleY, toggleW)) { ambient = !ambient; syncActiveDraft(); return true; }
            if (clickToggle(mx, my, rightX + 6 + toggleW + toggleGap, toggleY, toggleW)) { particles = !particles; syncActiveDraft(); return true; }
            if (clickToggle(mx, my, rightX + 6 + (toggleW + toggleGap) * 2, toggleY, toggleW)) { icon = !icon; syncActiveDraft(); return true; }

            int by = dy + dh - 28, bw = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            if (mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2) { activeSubEditor = null; return true; }
            int okX = dx + dw / 2 + 6;
            if (mx >= okX && mx < okX + bw && my >= by && my < by + bh2) { confirm(); return true; }
            return true;
        }

        private boolean clickToggle(double mx, double my, int x, int y, int bw) {
            return mx >= x && mx < x + bw && my >= y && my < y + 18;
        }

        private void setBoxBounds(FlatEditBox box, int x, int y, int w) {
            box.setX(x);
            box.setY(y);
            box.setWidth(w);
        }

        private void setFocusField(int field) {
            focusField = field;
            searchBox.setFocused(field == 0);
            durationBox.setFocused(field == 1);
            amplifierBox.setFocused(field == 2);
        }

        private void selectEffect(String id, int btn) {
            syncActiveDraft();
            if (btn == 1 && selectedEffects.containsKey(id)) {
                removeSelectedEffect(id);
                return;
            }
            if (!selectedEffects.containsKey(id) && selectedEffects.size() >= POTION_MAX_CUSTOM_EFFECTS) {
                setStatus("最多 " + POTION_MAX_CUSTOM_EFFECTS + " 个药水效果", ERROR_C);
                return;
            }
            selectedEffects.computeIfAbsent(id, k -> new EffectDraft("600", "0", false, true, true));
            setActiveEffect(id);
        }

        private void setActiveEffect(String id) {
            activeEffectId = id;
            EffectDraft draft = selectedEffects.get(id);
            if (draft == null) return;
            durationBox.setValue(draft.duration);
            durationBox.setCursorPosition(durationBox.getValue().length());
            amplifierBox.setValue(draft.amplifier);
            amplifierBox.setCursorPosition(amplifierBox.getValue().length());
            ambient = draft.ambient;
            particles = draft.particles;
            icon = draft.icon;
        }

        private void removeSelectedEffect(String id) {
            selectedEffects.remove(id);
            if (id != null && id.equals(activeEffectId)) {
                activeEffectId = selectedEffects.isEmpty() ? null : selectedEffects.keySet().iterator().next();
                if (activeEffectId != null) setActiveEffect(activeEffectId);
            }
        }

        private void syncActiveDraft() {
            if (activeEffectId == null) return;
            EffectDraft draft = selectedEffects.get(activeEffectId);
            if (draft == null) return;
            draft.duration = String.valueOf(clampPotionInt(durationBox.getValue(), 600, 1, POTION_MAX_DURATION));
            draft.amplifier = String.valueOf(clampPotionInt(amplifierBox.getValue(), 0, 0, POTION_MAX_AMPLIFIER));
            draft.ambient = ambient;
            draft.particles = particles;
            draft.icon = icon;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { confirm(); return true; }
            if (key == 32 && focusField == 0 && selectedIdx >= 0 && selectedIdx < filtered.size()) { selectEffect(filtered.get(selectedIdx), 0); return true; }
            if (key == 258) { setFocusField((focusField + 1) % 3); return true; }
            if (focusField == 0) {
                if (key == 264 && selectedIdx < filtered.size() - 1) { selectedIdx++; if (selectedIdx >= scrollOff + 7) scrollOff++; return true; }
                if (key == 265 && selectedIdx > 0) { selectedIdx--; if (selectedIdx < scrollOff) scrollOff = selectedIdx; return true; }
                if (searchBox.keyPressed(new KeyEvent(key, scan, mod))) return true;
            } else {
                FlatEditBox box = focusField == 1 ? durationBox : amplifierBox;
                if (box.keyPressed(new KeyEvent(key, scan, mod))) { syncActiveDraft(); return true; }
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (focusField == 0) {
                if (searchBox.charTyped(new CharacterEvent(c))) return true;
            } else {
                (focusField == 1 ? durationBox : amplifierBox).charTyped(new CharacterEvent(c));
                syncActiveDraft();
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            int dw = Math.min(contentW - 20, Math.min(760, Math.max(520, contentW - 36)));
            int dh = Math.min(contentH - 20, Math.min(360, Math.max(300, contentH - 30)));
            int dx = contentX + (contentW - dw) / 2;
            int dy = contentY + (contentH - dh) / 2;
            int leftX = dx + 10;
            int leftW = Math.max(230, Math.min(320, (dw - 34) * 45 / 100));
            int rightX = leftX + leftW + 12;
            int rightW = dw - (rightX - dx) - 10;
            int syBox = dy + 30;
            int selectedY = syBox + 24;
            int selectedListH = Math.min(92, Math.max(44, dh - 218));
            int selectedRows = Math.max(1, selectedListH / 22);
            if (lastMouseX >= rightX && lastMouseX < rightX + rightW && lastMouseY >= selectedY && lastMouseY < selectedY + selectedRows * 22) {
                selectedScrollOff -= (int) sy;
                selectedScrollOff = Math.max(0, Math.min(selectedScrollOff, Math.max(0, selectedEffects.size() - selectedRows)));
                return true;
            }
            scrollOff -= (int) sy * 3;
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, filtered.size() - 7)));
            return true;
        }

        private void confirm() {
            syncActiveDraft();
            if (selectedEffects.isEmpty()) {
                setStatus(tr("ankinbt.simple.select_effect_first"), ERROR_C);
                return;
            }
            Map<String, EffectDraftData> effects = new LinkedHashMap<>();
            for (Map.Entry<String, EffectDraft> entry : new ArrayList<>(selectedEffects.entrySet())) {
                EffectDraft draft = entry.getValue();
                int dur = clampPotionInt(draft.duration, 600, 1, POTION_MAX_DURATION);
                int amp = clampPotionInt(draft.amplifier, 0, 0, POTION_MAX_AMPLIFIER);
                effects.put(entry.getKey(), new EffectDraftData(dur, amp, draft.ambient, draft.particles, draft.icon));
            }
            setPotionCustomEffects(effects);
            activeSubEditor = null;
        }

        private int clampPotionInt(String value, int fallback, int min, int max) {
            try {
                int parsed = Integer.parseInt(value == null || value.isBlank() ? String.valueOf(fallback) : value.trim());
                return Math.max(min, Math.min(max, parsed));
            } catch (NumberFormatException ignored) {
                return Math.max(min, Math.min(max, fallback));
            }
        }
    }

    // ==================== COLOR PICKER (for dye / name color) ====================

    class ColorPickerSubEditor implements SubEditor {
        int mode; // -1 = lore insert, -2 = name color, -3 = potion color, >= 0 = dye color (initial value)
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

            String title = mode == -2 ? tr("ankinbt.simple.name_color")
                    : (mode == -3 ? tr("ankinbt.simple.potion_custom_color") : tr("ankinbt.simple.dye_color_picker"));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, dx + 10, dy + 8, C1, false);
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
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, String.format("#%06X", selectedColor & 0xFFFFFF), dx + 46, prevY + 6, C1, false);
            if (mode == -2) {
                String name = editStack.getHoverName().getString();
                if (font.width(name) > dw - 86) name = font.plainSubstrByWidth(name, dw - 96) + "..";
                Component previewName = Component.literal(name).withStyle(Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(selectedColor)));
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, previewName, dx + 46, prevY + 20, C1, false);
            }

            // Apply / Cancel
            int by = dy + dh - 28, bw = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw, by + bh2, ch ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, C2, false);

            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw, by + bh2, oh ? ACCENT : 0xFF4F46E5);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.apply"), okX + (bw - font.width(tr("ankinbt.edit.apply"))) / 2, by + 6, C1, false);
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
                // Name color - force non-italic to prevent default italic rendering
                String name = editStack.getHoverName().getString();
                setCustomNameComponent(Component.literal(name).withStyle(Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(selectedColor))));
                dirty = true;
            } else if (mode == -3) {
                setPotionCustomColor(selectedColor);
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
        private final FlatEditBox searchBox;
        private int scrollOff = 0;
        private int hoverIdx = -1;
        private int selectedIdx = -1;
        private String levelInput = "1";
        private int levelCursor = 1;
        private boolean focusLevel = false;

        EnchantPickerSubEditor() {
            searchBox = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 18, Component.empty());
            searchBox.setMaxLength(128);
            searchBox.setHint(Component.translatable("ankinbt.search.hint"));
            searchBox.setResponder(v -> {
                searchQ = v == null ? "" : v;
                filter();
            });
            searchBox.setFocused(true);
            try {                allEnchants.addAll(VersionCompat.get().getAllEnchantIds());
            } catch (Throwable ignored) {
            }
            if (allEnchants.isEmpty()) {
                allEnchants.addAll(ENCHANT_ZH.keySet());
            }
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
            searchBox.setX(sx);
            searchBox.setY(sy);
            searchBox.setWidth(sw);
            searchBox.setFocused(!focusLevel);
            searchBox.renderWidget(g, mx, my, 0.0f);

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
                String enchId = filtered.get(i);
                g.renderItem(ItemEditorVisuals.enchantIconStack(enchId), x + 10, ry);
                String displayName = getEnchantDisplayName(enchId);
                if (font.width(displayName) > w - 48) displayName = font.plainSubstrByWidth(displayName, w - 52) + "..";
                g.drawString(font, displayName, x + 32, ry + 4, sel ? C1 : C2, false);
            }

            int by = y + h - 30;
            g.drawString(font, tr("ankinbt.simple.level"), x + 8, by + 6, C2, false);
            int lx = x + 8 + font.width(tr("ankinbt.simple.level")) + 4;
            g.fill(lx, by + 2, lx + 40, by + 20, 0xFF12121E);
            drawBorder(g, lx, by + 2, 40, 18, focusLevel ? accentColor() : BORDER);
            g.drawString(font, levelInput + (focusLevel && System.currentTimeMillis() % 1000 < 500 ? "_" : ""), lx + 4, by + 7, C1, false);

            int confirmX = x + w - 78;
            boolean ch = mx >= confirmX && mx < confirmX + 70 && my >= by + 1 && my < by + 21;
            g.fill(confirmX, by + 1, confirmX + 70, by + 21, ch ? accentColor() : accentColor());
            g.drawString(font, tr("ankinbt.add.confirm"), confirmX + (70 - font.width(tr("ankinbt.add.confirm"))) / 2, by + 7, C1, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int sx = x + 8, sy = y + 18, sw = w - 16, sh = 18;
            searchBox.setX(sx); searchBox.setY(sy); searchBox.setWidth(sw); if (searchBox.mouseClicked(new MouseButtonEvent(mx, my, new net.minecraft.client.input.MouseButtonInfo(btn, 0)), false)) { focusLevel = false; searchBox.setFocused(true); return true; }

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
                if (searchBox.keyPressed(new KeyEvent(key, scan, mod))) {
                    searchQ = searchBox.getValue();
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c >= 32) {
                if (focusLevel) {
                    if (c >= '0' && c <= '9') { levelInput = levelInput.substring(0, levelCursor) + c + levelInput.substring(levelCursor); levelCursor++; }
                } else {
                    searchBox.charTyped(new CharacterEvent(c)); searchQ = searchBox.getValue();
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
        private final FlatEditBox searchBox;
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
            searchBox = new FlatEditBox(SimpleEditorScreen.this.font, 0, 0, 1, 18, Component.empty());
            searchBox.setMaxLength(128);
            searchBox.setHint(Component.translatable("ankinbt.search.hint"));
            searchBox.setResponder(v -> {
                searchQ = v == null ? "" : v;
                filter();
            });
            searchBox.setFocused(true);
            try {                allAttrs.addAll(VersionCompat.get().getAllAttributeIds());
            } catch (Throwable ignored) {
            }
            if (allAttrs.isEmpty()) {
                allAttrs.addAll(ATTR_ZH.keySet());
            }
            Collections.sort(allAttrs);
            filtered = new ArrayList<>(allAttrs);
        }

        private void filter() {
            if (searchQ.isEmpty()) { filtered = new ArrayList<>(allAttrs); }
            else {
                String q = searchQ.toLowerCase();
                filtered = allAttrs.stream().filter(s -> {
                    if (s.toLowerCase().contains(q)) return true;
                    String zh = findAttrText(ATTR_ZH, s);
                    return zh != null && zh.contains(q);
                }).collect(Collectors.toList());
            }
            scrollOff = 0; selectedIdx = -1;
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            // Title
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.pick_attr"), x + 8, y + 4, C1, false);

            // Search box
            int sx = x + 8, sy = y + 18, sw = w - 16, sh = 18;
            searchBox.setX(sx);
            searchBox.setY(sy);
            searchBox.setWidth(sw);
            searchBox.setFocused(focusField == 0);
            searchBox.renderWidget(g, mx, my, 0.0f);

            // Attribute list
            int ly = sy + sh + 4;
            boolean showNotes = AnkiConfig.isAttributeNotesEnabled();
            int listH = h - (showNotes ? 162 : 140);
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
                String attrId = filtered.get(i);
                g.renderItem(ItemEditorVisuals.attributeIconStack(attrId), x + 10, ry);
                String displayName = getAttrDisplayName(attrId);
                int labelBudget = showNotes ? w - 70 : w - 48;
                if (font.width(displayName) > labelBudget) displayName = font.plainSubstrByWidth(displayName, labelBudget - 4) + "..";
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, displayName, x + 32, ry + 4, sel ? C1 : C2, false);
                if (showNotes && getAttrNote(attrId) != null) {
                    int infoX = x + w - 24;
                    g.fill(infoX, ry + 3, infoX + 10, ry + 13, hovered ? accentColor() : BTN_BG);
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, "i", infoX + 4, ry + 5, hovered ? C1 : C3, false);
                }
            }

            if (showNotes) {
                int noteIdx = hoverIdx >= 0 ? hoverIdx : selectedIdx;
                String note = noteIdx >= 0 && noteIdx < filtered.size() ? getAttrNote(filtered.get(noteIdx)) : null;
                int noteY = ly + listH + 4;
                if (note != null && !note.isBlank()) {
                    String shown = font.width(note) > w - 24 ? font.plainSubstrByWidth(note, w - 34) + ".." : note;
                    g.fill(x + 8, noteY, x + w - 8, noteY + 18, 0x2014B8A6);
                    drawBorder(g, x + 8, noteY, w - 16, 18, 0x5514B8A6);
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, "i", x + 14, noteY + 5, accentColor(), false);
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, shown, x + 26, noteY + 5, C2, false);
                } else {
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.attr_note_hint"), x + 10, noteY + 5, C3, false);
                }
            }

            // Bottom controls area
            int bottomY = y + h - 90;

            // Amount input
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.attr_amount"), x + 8, bottomY + 4, C2, false);
            int ax = x + 8 + font.width(tr("ankinbt.simple.attr_amount")) + 4;
            int aw = 80;
            g.fill(ax, bottomY, ax + aw, bottomY + 18, 0xFF12121E);
            drawBorder(g, ax, bottomY, aw, 18, focusField == 1 ? accentColor() : BORDER);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, amountInput + (focusField == 1 && System.currentTimeMillis() % 1000 < 500 ? "_" : ""),
                    ax + 4, bottomY + 5, C1, false);

            // Operation selector
            int opY = bottomY + 22;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.attr_operation"), x + 8, opY + 4, C2, false);
            int opX = x + 8 + font.width(tr("ankinbt.simple.attr_operation")) + 4;
            String[] opLabels = isZh() ? OP_NAMES_ZH : OP_NAMES_EN;
            for (int i = 0; i < 3; i++) {
                int bw = font.width(opLabels[i]) + 10;
                boolean hover = mx >= opX && mx < opX + bw && my >= opY && my < opY + 18;
                boolean active = i == selectedOp;
                g.fill(opX, opY, opX + bw, opY + 18, active ? accentColor() : (hover ? BTN_HOVER : BTN_BG));
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, opLabels[i], opX + 5, opY + 5, active ? C1 : C2, false);
                opX += bw + 4;
            }

            // Slot selector
            int slotY = opY + 22;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.attr_slot"), x + 8, slotY + 4, C2, false);
            int slotX = x + 8 + font.width(tr("ankinbt.simple.attr_slot")) + 4;
            for (int i = 0; i < SLOT_KEYS.length; i++) {
                String slotLabel = isZh() ? SLOT_ZH.getOrDefault(SLOT_KEYS[i], SLOT_KEYS[i]) : SLOT_KEYS[i];
                int bw = font.width(slotLabel) + 8;
                boolean hover = mx >= slotX && mx < slotX + bw && my >= slotY && my < slotY + 18;
                boolean active = i == selectedSlot;
                g.fill(slotX, slotY, slotX + bw, slotY + 18, active ? ACCENT : (hover ? BTN_HOVER : BTN_BG));
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, slotLabel, slotX + 4, slotY + 5, active ? C1 : C2, false);
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
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.add.confirm"), confirmX + (70 - font.width(tr("ankinbt.add.confirm"))) / 2, confirmY + 6, C1, false);
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
                if (searchBox.keyPressed(new KeyEvent(key, scan, mod))) {
                    searchQ = searchBox.getValue();
                    return true;
                }
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
                    searchBox.charTyped(new CharacterEvent(c)); searchQ = searchBox.getValue();
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
