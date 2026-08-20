package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.nbt.NbtFileIO;
import com.ankinbt.util.EnchantmentTooltipHelper;
import com.ankinbt.util.TextEditBuffer;
import com.ankinbt.util.MultiLineTextEditBuffer;
import com.ankinbt.util.ItemEditorVisuals;
import com.ankinbt.util.UiSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import java.lang.reflect.Method;
import java.nio.file.Path;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
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
    private static final ResourceLocation LOGO_TEXTURE = ResourceLocation.fromNamespaceAndPath("ankinbt", "textures/gui/editor-logo.png");

    // Layout
    private static final int HEADER_H = 32;
    private static final int FOOTER_H = 20;
    private static final int MARGIN = 16;
    private static final int SIDEBAR_W = 140;
    private static final int SCROLLBAR_W = 6;
    private static final int ROW_H = 24;
    private static final int CARD_H = 42;
    private static final int CARD_GAP = 5;
    private static final int LORE_CARD_GAP = 3;
    private static final int CAT_H = 28;

    // Colors
    private static final int BG = 0xD8080810;
    private static final int SIDEBAR_BG = 0xD80C0C18;
    private static final int HEADER_BG = 0xD8101020;
    private static final int HOVER = 0x30FFFFFF;
    private static final int SELECT_BG = 0x28_38_BD_F8;
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
    private ItemStack originalStack;
    private int inventorySlot; // -1 = main hand, >= 0 = inventory slot
    private final AbstractContainerScreen<?> inventoryParent;
    private EditorDock.Bounds dockBounds;
    private EditorDock.Bounds barBounds;
    private EditorDock.Bounds drawerBounds;
    private boolean drawerAbove;
    private boolean drawerOpen = true;
    private float drawerAnim = 0f;
    private float contentAnim = 1f;
    private float activeIndicatorX = -1f;
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
    private final float[] categoryHoverAnim = new float[Category.values().length];
    private final Map<Integer, Float> cardHoverAnim = new HashMap<>();
    private int draggingLoreIndex = -1;
    private int loreDropIndex = -1;
    private long lastLoreAutoScrollAt = 0L;
    private float loreTipHoverAnim = 0f;
    private float homeTipHoverAnim = 0f;

    // Status
    private String statusMsg = null;
    private long statusTime = 0;
    private int statusColor = UiTheme.textDim();
    private boolean dirty = false;
    private boolean nativeDialogOpen = false;
    private long lastNativeDialogAt = 0L;
    private float openAnim = 0f;
    private float brandAnim = 0f;
    private float settingsHoverAnim = 0f;

    // Sub-editor state
    private SubEditor activeSubEditor = null;
    private SubEditor lastRenderedSubEditor = null;

    // Header buttons
    private final List<Btn> headerBtns = new ArrayList<>();

    public SimpleEditorScreen(ItemStack stack) {
        this(stack, -1, null);
    }

    public SimpleEditorScreen(ItemStack stack, int inventorySlot) {
        this(stack, inventorySlot, null);
    }

    public SimpleEditorScreen(ItemStack stack, int inventorySlot, AbstractContainerScreen<?> inventoryParent) {
        super(Component.translatable("ankinbt.simple.title"));
        this.originalStack = stack.copy();
        this.editStack = stack.copy();
        this.inventorySlot = inventorySlot;
        this.inventoryParent = inventoryParent;
    }

    @Override
    protected void init() {
        super.init();
        editorScale = AnkiConfig.getItemEditorScale();
        editorWidthAdjustment = AnkiConfig.getItemEditorWidthAdjustment();
        editorHeightAdjustment = AnkiConfig.getItemEditorHeightAdjustment();
        applyMenuLayout(EditorDock.menuLayout(width, height, 286, inventoryParent != null,
                editorScale, editorWidthAdjustment, editorHeightAdjustment));
        headerBtns.clear();
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

        contentX = px + 8;
        contentY = py + 34;
        contentW = Math.max(1, pw - 16 - SCROLLBAR_W);
        contentH = Math.max(1, ph - 34 - FOOTER_H);
        maxRows = Math.max(1, contentH / ROW_H);
        scrollOff = Math.max(0, Math.min(scrollOff,
                Math.max(0, getRowsForCategory(activeCat).size() - maxRows)));
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
    public void render(GuiGraphics g, int mx, int my, float pt) {
        float cfgSpeed = AnkiConfig.getUiAnimationSpeed();
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.06f, Math.min(0.16f, cfgSpeed)) : 1.0f;
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);
        float motionSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.10f, cfgSpeed * 1.7f) : 1.0f;
        drawerAnim = UiTheme.approach(drawerAnim, drawerOpen ? 1.0f : 0.0f, motionSpeed);
        contentAnim = UiTheme.approach(contentAnim, 1.0f, motionSpeed);
        if (draggingLoreIndex >= 0 && drawerAnim >= 0.92f && contentAnim >= 0.92f) {
            updateLoreDropTarget(mx, my);
        }

        if (inventoryParent == null) {
            brandAnim = EditorBrandLayer.approachOpen(brandAnim);
            boolean settingsHovered = EditorBrandLayer.isSettingsButton(mx, my, width);
            settingsHoverAnim = EditorBrandLayer.approachSettingsHover(settingsHoverAnim, settingsHovered);
            EditorBrandLayer.renderBackgroundLogo(g, width, height);
            g.fill(0, 0, width, height, UiTheme.scrim(AnkiConfig.getUiOpacity(), openAnim));
        }
        renderDrawer(g, mx, my);
        renderMenuBar(g, mx, my);
        renderSizeControl(g, mx, my);
        if (inventoryParent == null) {
            EditorBrandLayer.renderItemStatus(g, font, width, height, brandAnim,
                    InventoryEditorOverlay.itemEditorStatusMode(
                            Component.translatable("ankinbt.config.mode.simple").getString()), editorScale);
            EditorBrandLayer.renderSettingsButton(g, font, width, mx, my, settingsHoverAnim);
        }
    }

    private void renderMenuBar(GuiGraphics g, int mx, int my) {
        int bx = barBounds.x(), by = barBounds.y(), bw = barBounds.width(), bh = barBounds.height();
        int accent = accentColor();
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
        int tabStart = bx + brandW;
        int tabCount = Category.values().length + 1;
        int tabW = Math.max(22, (toolsStart - tabStart) / tabCount);
        String[] icons = { UiIcons.BOX, UiIcons.SPARKLES, UiIcons.TEXT, UiIcons.SHIELD, UiIcons.EYE, UiIcons.LAYERS };
        String[] labels = {
                Component.translatable("ankinbt.cat.general").getString(),
                Component.translatable("ankinbt.cat.enchant").getString(),
                Component.translatable("ankinbt.cat.lore").getString(),
                Component.translatable("ankinbt.cat.attribute").getString(),
                Component.translatable("ankinbt.cat.visual").getString(),
                Component.translatable("ankinbt.cat.misc").getString()
        };
        float hoverSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.18f, AnkiConfig.getUiAnimationSpeed() * 2.4f) : 1.0f;
        for (int i = 0; i < Category.values().length; i++) {
            int tx = tabStart + i * tabW;
            boolean hover = mx >= tx && mx < tx + tabW && my >= by && my < by + bh;
            boolean active = Category.values()[i] == activeCat;
            categoryHoverAnim[i] = UiTheme.approach(categoryHoverAnim[i], hover ? 1f : 0f, hoverSpeed);
            if ("segmented".equals(AnkiConfig.getUiNavigationStyle()) && active) {
                g.fill(tx + 2, by + 3, tx + tabW - 2, by + bh - 3,
                        UiTheme.withAlpha(accent & 0x00FFFFFF, 86));
            } else if (categoryHoverAnim[i] > 0.01f) {
                g.fill(tx + 1, by + 2, tx + tabW - 1, by + bh - 2,
                        UiTheme.mix(0x00000000, HOVER, categoryHoverAnim[i]));
            }
            if ("compact".equals(AnkiConfig.getUiNavigationStyle()) && active) {
                g.fill(tx + 3, by + 7, tx + 5, by + bh - 7, accent);
            }
            Component icon = UiIcons.component(icons[i]);
            if (tabW >= 46) {
                int totalW = font.width(icon) + 4 + font.width(labels[i]);
                g.drawString(font, icon, tx + Math.max(3, (tabW - totalW) / 2), by + 10, Category.values()[i] == activeCat ? UiTheme.textMain() : UiTheme.textDim(), false);
                g.drawString(font, labels[i], tx + Math.max(3, (tabW - totalW) / 2) + font.width(icon) + 4,
                        by + 10, Category.values()[i] == activeCat ? UiTheme.textMain() : UiTheme.textDim(), false);
            } else {
                g.drawString(font, icon, tx + (tabW - font.width(icon)) / 2, by + 10,
                        Category.values()[i] == activeCat ? UiTheme.textMain() : UiTheme.textDim(), false);
                if (hover) VersionCompat.get().renderTooltip(g, font, Component.literal(labels[i]), mx, my);
            }
        }

        int advancedX = tabStart + Category.values().length * tabW;
        boolean advancedHover = mx >= advancedX && mx < advancedX + tabW && my >= by && my < by + bh;
        Component advancedIcon = UiIcons.component(UiIcons.CODE);
        g.fill(advancedX, by + 4, advancedX + 1, by + bh - 4, UiTheme.themedBorder(1f, 1f));
        g.drawString(font, advancedIcon, advancedX + (tabW - font.width(advancedIcon)) / 2, by + 10,
                advancedHover ? UiTheme.textMain() : UiTheme.textDim(), false);
        if (advancedHover) VersionCompat.get().renderTooltip(g, font,
                Component.translatable("ankinbt.btn.switch_advanced"), mx, my);

        int activeX = tabStart + activeCat.ordinal() * tabW;
        if (activeIndicatorX < 0f) activeIndicatorX = activeX;
        activeIndicatorX = UiTheme.approach(activeIndicatorX, activeX, hoverSpeed);
        if ("underline".equals(AnkiConfig.getUiNavigationStyle())) {
            g.fill(Math.round(activeIndicatorX) + 5, by + bh - 3,
                    Math.round(activeIndicatorX) + tabW - 5, by + bh - 1, accent);
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

    private void renderSizeControl(GuiGraphics g, int mx, int my) {
        if (activeSubEditor != null) return;
        sizeControlHoverAnim = EditorDock.renderSizeControl(g, font, width, height, mx, my,
                editorScale, sizeControlHoverAnim, resizingEditor || editorSizeFocused, accentColor());
    }

    private void renderDrawer(GuiGraphics g, int mx, int my) {
        if (drawerAnim <= 0.01f) return;
        if (activeSubEditor != lastRenderedSubEditor) {
            contentAnim = AnkiConfig.isUiAnimationEnabled() ? 0f : 1f;
            lastRenderedSubEditor = activeSubEditor;
        }
        int reveal = Math.max(1, Math.round(ph * drawerAnim));
        int clipTop = drawerAbove ? py + ph - reveal : py;
        int clipBottom = drawerAbove ? py + ph : py + reveal;
        g.enableScissor(px, clipTop, px + pw, clipBottom);
        g.fill(px, py, px + pw, py + ph, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
        drawBorder(g, px, py, pw, ph, UiTheme.themedBorder(AnkiConfig.getUiOpacity(), openAnim));

        g.renderItem(editStack, px + 9, py + 8);
        String itemName = editStack.getHoverName().getString();
        int nameBudget = Math.max(60, pw - 150);
        if (font.width(itemName) > nameBudget) itemName = font.plainSubstrByWidth(itemName, nameBudget - 10) + "..";
        g.drawString(font, itemName, px + 31, py + 9, UiTheme.textMain(), false);
        String category = Component.translatable(categoryTranslation(activeCat)).getString();
        g.drawString(font, category, px + pw - font.width(category) - 10, py + 9, accentColor(), false);
        g.fill(px + 8, py + 29, px + pw - 8, py + 30, UiTheme.themedBorder(1f, 1f));

        int baseContentY = py + 34;
        contentY = baseContentY + Math.round((1f - contentAnim) * (drawerAbove ? -7f : 7f));
        if (activeSubEditor != null) {
            activeSubEditor.render(g, font, mx, my, contentX, contentY, contentW, contentH);
        } else {
            renderCategoryContent(g, mx, my);
        }
        g.fill(px + 1, py + ph - FOOTER_H, px + pw - 1, py + ph - FOOTER_H + 1, UiTheme.themedBorder(1f, 1f));
        renderFooter(g);
        boolean loreTipHovered = renderLoreTip(g, mx, my);
        boolean homeTipHovered = renderHomeTip(g, mx, my);
        g.disableScissor();
        if (mx >= px + 9 && mx < px + 25 && my >= py + 8 && my < py + 24) {
            VersionCompat.get().renderItemTooltip(g, font, editStack, mx, my);
        }
        if (loreTipHovered) {
            VersionCompat.get().renderTooltip(g, font,
                    Component.translatable("ankinbt.simple.lore_color_hint"), mx, my);
        }
        if (homeTipHovered) {
            VersionCompat.get().renderTooltip(g, font,
                    Component.translatable("ankinbt.simple.layout_tip"), mx, my);
        }
    }

    private String categoryTranslation(Category category) {
        return switch (category) {
            case GENERAL -> "ankinbt.cat.general";
            case ENCHANT -> "ankinbt.cat.enchant";
            case LORE -> "ankinbt.cat.lore";
            case ATTRIBUTE -> "ankinbt.cat.attribute";
            case VISUAL -> "ankinbt.cat.visual";
            case MISC -> "ankinbt.cat.misc";
        };
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
        g.drawString(font, name, lx, headerY, UiTheme.textMain(), false);
        headerY += 14;
        g.fill(lx, headerY, sideX + sideW - 8, headerY + 1, UiTheme.themedBorder(1f, 1f));
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
            float hoverTarget = hover ? 1.0f : 0.0f;
            float hoverSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.16f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1.0f;
            categoryHoverAnim[i] = UiTheme.approach(categoryHoverAnim[i], hoverTarget, hoverSpeed);
            int categoryColor = active ? accentColor() : UiTheme.mix(CAT_BG, BTN_HOVER, categoryHoverAnim[i]);
            g.fill(lx, cy, lx + cw, cy + CAT_H, categoryColor);
            if (active) g.fill(lx, cy, lx + 2, cy + CAT_H, 0xFFFFFFFF);
            g.drawString(font, catNames[i], lx + 8, cy + (CAT_H - 8) / 2, active ? UiTheme.textMain() : UiTheme.textDim(), false);
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
        if ("cards".equals(AnkiConfig.getUiOptionStyle())) {
            renderCategoryCards(g, mx, my, rows);
            return;
        }
        scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, rows.size() - maxRows)));
        hoverRow = -1;
        int end = Math.min(scrollOff + maxRows, rows.size());
        for (int i = scrollOff; i < end; i++) {
            int ry = contentY + (i - scrollOff) * ROW_H;
            ActionRow row = rows.get(i);
            boolean loreLine = row.reorderIndex >= 0;
            boolean hovered = mx >= contentX && mx < contentX + contentW && my >= ry && my < ry + ROW_H;
            if (hovered) { hoverRow = i; g.fill(contentX, ry, contentX + contentW, ry + ROW_H, HOVER); }
            if (row.sectionHeader) {
                int headerFill = UiTheme.withAlpha(accentColor() & 0x00FFFFFF, hovered ? 42 : 24);
                g.fill(contentX, ry, contentX + contentW, ry + ROW_H, headerFill);
                g.fill(contentX, ry + ROW_H - 2, contentX + 28, ry + ROW_H, accentColor());
                g.drawString(font, row.label, contentX + 8, ry + (ROW_H - 8) / 2,
                        hovered ? UiTheme.textMain() : accentColor(), false);
                continue;
            }
            if (draggingLoreIndex >= 0 && row.reorderIndex == loreDropIndex) {
                drawBorder(g, contentX + 1, ry + 1, contentW - 2, ROW_H - 2, accentColor());
            }
            if (draggingLoreIndex == row.reorderIndex) {
                g.fill(contentX + 1, ry + 1, contentX + contentW - 1, ry + ROW_H - 1, 0x48000000);
            }
            g.fill(contentX, ry + ROW_H - 1, contentX + contentW, ry + ROW_H, 0x10FFFFFF);
            int labelX = contentX + 8;
            if (loreLine) {
                Component grip = UiIcons.component(UiIcons.MOVE);
                boolean gripHovered = isLoreDragHandle(i, mx, my);
                g.drawString(font, grip, contentX + 7, ry + 8,
                        gripHovered || draggingLoreIndex == row.reorderIndex ? accentColor() : UiTheme.textDim(), false);
                labelX += 20;
                if (gripHovered && draggingLoreIndex < 0) {
                    VersionCompat.get().renderTooltip(g, font,
                            Component.translatable("ankinbt.simple.lore_drag_tip"), mx, my);
                }
            }
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
                g.drawString(font, "X", rightX + (delW - font.width("X")) / 2, delY + 4, delHover ? ERROR_C : UiTheme.textDim(), false);
                rightX -= 4;
            }
            // Inline move buttons (right side, before value)
            if (row.moveUp != null || row.moveDown != null) {
                int btnW = 16, btnH = 16, btnY = ry + (ROW_H - btnH) / 2;
                if (row.moveDown != null) {
                    rightX -= btnW + 2;
                    boolean dHover = mx >= rightX && mx < rightX + btnW && my >= btnY && my < btnY + btnH;
                    g.fill(rightX, btnY, rightX + btnW, btnY + btnH, dHover ? BTN_HOVER : BTN_BG);
                    g.drawString(font, "v", rightX + (btnW - font.width("v")) / 2, btnY + 4, dHover ? UiTheme.textMain() : UiTheme.textDim(), false);
                }
                if (row.moveUp != null) {
                    rightX -= btnW + 2;
                    boolean uHover = mx >= rightX && mx < rightX + btnW && my >= btnY && my < btnY + btnH;
                    g.fill(rightX, btnY, rightX + btnW, btnY + btnH, uHover ? BTN_HOVER : BTN_BG);
                    g.drawString(font, "^", rightX + (btnW - font.width("^")) / 2, btnY + 4, uHover ? UiTheme.textMain() : UiTheme.textDim(), false);
                }
                rightX -= 4;
            }

            if (row.currentValue != null) {
                String val = row.currentValue;
                int maxValW = rightX - (contentX + contentW / 2);
                if (font.width(val) > maxValW) val = font.plainSubstrByWidth(val, maxValW - 10) + "..";
                g.drawString(font, val, rightX - font.width(val), ry + (ROW_H - 8) / 2, UiTheme.textDim(), false);
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

    private void renderCategoryCards(GuiGraphics g, int mx, int my, List<ActionRow> rows) {
        hoverRow = -1;
        int columns = cardColumns();
        int gap = cardGap();
        int cardHeight = cardHeight();
        int cardWidth = Math.max(88, (contentW - gap * (columns - 1)) / columns);
        int visibleRows = Math.max(1, contentH / (cardHeight + gap));
        int capacity = visibleRows * columns;
        int totalRows = (rows.size() + columns - 1) / columns;
        int maxOffset = Math.max(0, (totalRows - visibleRows) * columns);
        scrollOff = Math.max(0, Math.min(scrollOff, maxOffset));
        int end = Math.min(rows.size(), scrollOff + capacity);
        float speed = AnkiConfig.isUiAnimationEnabled()
                ? Math.max(0.16f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1f;
        for (int i = scrollOff; i < end; i++) {
            int local = i - scrollOff;
            int col = local % columns;
            int gridRow = local / columns;
            int cx = contentX + col * (cardWidth + gap);
            int cy = contentY + gridRow * (cardHeight + gap);
            ActionRow row = rows.get(i);
            boolean loreLine = row.reorderIndex >= 0;
            boolean hovered = mx >= cx && mx < cx + cardWidth && my >= cy && my < cy + cardHeight;
            if (hovered) hoverRow = i;
            float hover = UiTheme.approach(cardHoverAnim.getOrDefault(i, 0f), hovered ? 1f : 0f, speed);
            cardHoverAnim.put(i, hover);
            int cardBg = UiTheme.mix(UiTheme.card(AnkiConfig.getUiOpacity(), openAnim),
                    UiTheme.withAlpha(accentColor() & 0x00FFFFFF, 54), hover);
            g.fill(cx, cy, cx + cardWidth, cy + cardHeight, cardBg);
            drawBorder(g, cx, cy, cardWidth, cardHeight, hovered
                    ? UiTheme.withAlpha(accentColor() & 0x00FFFFFF, 208)
                    : UiTheme.themedBorder(0.8f, openAnim));
            if (row.sectionHeader) {
                g.fill(cx, cy + cardHeight - 2, cx + Math.min(cardWidth, 34), cy + cardHeight, accentColor());
                g.drawString(font, row.label, cx + 8, cy + (cardHeight - 8) / 2,
                        hovered ? UiTheme.textMain() : accentColor(), false);
                continue;
            }
            if (draggingLoreIndex >= 0 && row.reorderIndex == loreDropIndex) {
                drawBorder(g, cx + 1, cy + 1, cardWidth - 2, cardHeight - 2, accentColor());
            }
            if (draggingLoreIndex == row.reorderIndex) {
                g.fill(cx + 1, cy + 1, cx + cardWidth - 1, cy + cardHeight - 1, 0x48000000);
            }

            int labelX = cx + 8;
            if (loreLine) {
                Component grip = UiIcons.component(UiIcons.MOVE);
                boolean gripHovered = isLoreDragHandle(i, mx, my);
                g.drawString(font, grip, cx + 7, cy + 8,
                        gripHovered || draggingLoreIndex == row.reorderIndex ? accentColor() : UiTheme.textDim(), false);
                labelX += 20;
                if (gripHovered && draggingLoreIndex < 0) {
                    VersionCompat.get().renderTooltip(g, font,
                            Component.translatable("ankinbt.simple.lore_drag_tip"), mx, my);
                }
            }
            if (row.icon != null && !row.icon.isEmpty()) {
                g.renderItem(row.icon, cx + 6, cy + 6);
                labelX += 22;
            }
            int actionReserve = row.deleteAction != null ? 24 : 7;
            String label = row.label;
            int labelWidth = Math.max(24, cx + cardWidth - actionReserve - labelX - 4);
            if (font.width(label) > labelWidth) label = font.plainSubstrByWidth(label, Math.max(8, labelWidth - 8)) + "..";
            g.drawString(font, label, labelX, cy + 8, row.labelColor, false);

            if (row.currentValue != null) {
                String value = row.currentValue;
                int valueWidth = cardWidth - 16;
                if (font.width(value) > valueWidth) value = font.plainSubstrByWidth(value, Math.max(8, valueWidth - 8)) + "..";
                g.drawString(font, value, cx + 8, cy + 26, UiTheme.textDim(), false);
            }
            renderCardActions(g, row, mx, my, cx, cy, cardWidth, cardHeight);
        }
        if (rows.size() > capacity) {
            int sbx = px + pw - SCROLLBAR_W - 3;
            g.fill(sbx, contentY, sbx + SCROLLBAR_W, contentY + contentH, SB_TRACK);
            float ratio = (float) capacity / rows.size();
            int thumbH = Math.max(16, (int) (contentH * ratio));
            float sr = (float) scrollOff / Math.max(1, rows.size() - capacity);
            int thumbY = contentY + (int) ((contentH - thumbH) * sr);
            g.fill(sbx, thumbY, sbx + SCROLLBAR_W, thumbY + thumbH, SB_THUMB);
        }
    }

    private void renderCardActions(GuiGraphics g, ActionRow row, int mx, int my,
                                   int x, int y, int width, int height) {
        int right = x + width - 6;
        if (row.deleteAction != null) {
            int bx = right - 18;
            boolean hover = mx >= bx && mx < bx + 18 && my >= y + 5 && my < y + 21;
            g.fill(bx, y + 5, bx + 18, y + 21, hover ? 0x66EF4444 : 0x28EF4444);
            g.drawString(font, "X", bx + (18 - font.width("X")) / 2, y + 9, hover ? ERROR_C : UiTheme.textDim(), false);
            right = bx - 3;
        }
        int buttonY = y + height - 19;
        if (row.moveDown != null) {
            int bx = right - 16;
            boolean hover = mx >= bx && mx < bx + 16 && my >= buttonY && my < buttonY + 14;
            g.fill(bx, buttonY, bx + 16, buttonY + 14, hover ? BTN_HOVER : BTN_BG);
            g.drawString(font, "v", bx + (16 - font.width("v")) / 2, buttonY + 3, UiTheme.textDim(), false);
            right = bx - 2;
        }
        if (row.moveUp != null) {
            int bx = right - 16;
            boolean hover = mx >= bx && mx < bx + 16 && my >= buttonY && my < buttonY + 14;
            g.fill(bx, buttonY, bx + 16, buttonY + 14, hover ? BTN_HOVER : BTN_BG);
            g.drawString(font, "^", bx + (16 - font.width("^")) / 2, buttonY + 3, UiTheme.textDim(), false);
        }
    }

    private int cardColumns() {
        // Lore is an ordered line list even when the global option style uses cards.
        // Keeping it single-column makes the visual order match drag-and-drop order.
        if (activeCat == Category.LORE || activeCat == Category.ENCHANT) return 1;
        // The default 854-wide Minecraft GUI produces about 500 logical pixels of
        // content width.  Use three cards there, while retaining useful fallbacks
        // for narrow editor resolutions.
        if (contentW >= 400) return 3;
        if (contentW >= 270) return 2;
        return 1;
    }

    private int cardHeight() {
        return activeCat == Category.LORE ? ROW_H : CARD_H;
    }

    private int cardGap() {
        return activeCat == Category.LORE ? LORE_CARD_GAP : CARD_GAP;
    }

    private int cardStride() {
        return cardHeight() + cardGap();
    }

    private int cardCapacity() {
        return Math.max(1, contentH / cardStride()) * cardColumns();
    }

    private boolean isLoreDragHandle(int rowIndex, double mx, double my) {
        if (activeCat != Category.LORE || rowIndex < scrollOff) return false;
        if ("cards".equals(AnkiConfig.getUiOptionStyle())) {
            int columns = cardColumns();
            int gap = cardGap();
            int cardHeight = cardHeight();
            int cardWidth = Math.max(88, (contentW - gap * (columns - 1)) / columns);
            int local = rowIndex - scrollOff;
            if (local < 0 || local >= cardCapacity()) return false;
            int x = contentX + local % columns * (cardWidth + gap);
            int y = contentY + local / columns * (cardHeight + gap);
            return mx >= x + 4 && mx < x + 27 && my >= y + 4 && my < y + 25;
        }
        int local = rowIndex - scrollOff;
        if (local < 0 || local >= maxRows) return false;
        int y = contentY + local * ROW_H;
        return mx >= contentX + 3 && mx < contentX + 27 && my >= y && my < y + ROW_H;
    }

    private void updateLoreDropTarget(double mx, double my) {
        if (draggingLoreIndex < 0) return;
        List<ActionRow> rows = getLoreRows();
        if (rows.isEmpty()) return;
        long now = System.currentTimeMillis();
        if (now - lastLoreAutoScrollAt >= 110L) {
            int step = "cards".equals(AnkiConfig.getUiOptionStyle()) ? cardColumns() : 1;
            int maxOffset = "cards".equals(AnkiConfig.getUiOptionStyle())
                    ? Math.max(0, rows.size() - cardCapacity())
                    : Math.max(0, rows.size() - maxRows);
            if (my < contentY + 12 && scrollOff > 0) {
                scrollOff = Math.max(0, scrollOff - step);
                lastLoreAutoScrollAt = now;
            } else if (my > contentY + contentH - 12 && scrollOff < maxOffset) {
                scrollOff = Math.min(maxOffset, scrollOff + step);
                lastLoreAutoScrollAt = now;
            }
        }

        int visualIndex;
        if ("cards".equals(AnkiConfig.getUiOptionStyle())) {
            int columns = cardColumns();
            int gap = cardGap();
            int cardHeight = cardHeight();
            int cardWidth = Math.max(88, (contentW - gap * (columns - 1)) / columns);
            int col = Math.max(0, Math.min(columns - 1,
                    (int) Math.floor((mx - contentX) / Math.max(1.0, cardWidth + gap))));
            int gridRow = Math.max(0, (int) Math.floor((my - contentY) / (double) (cardHeight + gap)));
            visualIndex = scrollOff + gridRow * columns + col;
        } else {
            int localRow = Math.max(0, (int) Math.floor((my - contentY) / (double) ROW_H));
            visualIndex = scrollOff + localRow;
        }
        visualIndex = Math.max(0, Math.min(rows.size() - 1, visualIndex));

        int nearest = -1;
        int nearestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < rows.size(); i++) {
            int reorderIndex = rows.get(i).reorderIndex;
            if (reorderIndex < 0) continue;
            int distance = Math.abs(i - visualIndex);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = reorderIndex;
            }
        }
        if (nearest >= 0) loreDropIndex = nearest;
    }

    private void finishLoreDrag() {
        int from = draggingLoreIndex;
        int to = loreDropIndex;
        resetLoreDrag();
        if (from >= 0 && to >= 0 && from != to) moveLore(from, to);
    }

    private void resetLoreDrag() {
        draggingLoreIndex = -1;
        loreDropIndex = -1;
        lastLoreAutoScrollAt = 0L;
    }

    private void renderFooter(GuiGraphics g) {
        int fy = py + ph - FOOTER_H + 5;
        String footerText;
        int footerColor;
        if (statusMsg != null && System.currentTimeMillis() - statusTime < 3000) {
            footerText = statusMsg;
            footerColor = statusColor;
        } else {
            statusMsg = null;
            footerText = Component.translatable("ankinbt.simple.hint").getString();
            footerColor = UiTheme.textDim();
        }
        int reserve = (activeCat == Category.LORE || activeCat == Category.GENERAL)
                && activeSubEditor == null ? 44 : 18;
        int maxWidth = Math.max(24, pw - reserve);
        if (font.width(footerText) > maxWidth) {
            footerText = font.plainSubstrByWidth(footerText, Math.max(4, maxWidth - font.width(".."))) + "..";
        }
        g.drawString(font, footerText, px + 9, fy, footerColor, false);
    }

    private boolean renderLoreTip(GuiGraphics g, int mx, int my) {
        boolean visible = activeCat == Category.LORE && drawerOpen && activeSubEditor == null;
        if (!visible) {
            loreTipHoverAnim = UiTheme.approach(loreTipHoverAnim, 0f, 0.25f);
            return false;
        }
        int x = px + pw - 30;
        int y = py + ph - FOOTER_H + 2;
        int size = 16;
        boolean hovered = mx >= x && mx < x + size && my >= y && my < y + size;
        float speed = AnkiConfig.isUiAnimationEnabled()
                ? Math.max(0.16f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1f;
        loreTipHoverAnim = UiTheme.approach(loreTipHoverAnim, hovered ? 1f : 0f, speed);
        int accent = accentColor();
        g.fill(x, y, x + size, y + size,
                UiTheme.mix(0x1CFFFFFF, UiTheme.withAlpha(accent & 0x00FFFFFF, 78), loreTipHoverAnim));
        drawBorder(g, x, y, size, size,
                UiTheme.mix(UiTheme.themedBorder(0.8f, 1f), accent, loreTipHoverAnim));
        Component icon = UiIcons.component(UiIcons.HELP);
        g.drawString(font, icon, x + (size - font.width(icon)) / 2, y + 4,
                hovered ? UiTheme.textMain() : UiTheme.textDim(), false);
        return hovered;
    }

    private boolean isLoreTip(double mx, double my) {
        if (activeCat != Category.LORE || !drawerOpen || activeSubEditor != null) return false;
        int x = px + pw - 30;
        int y = py + ph - FOOTER_H + 2;
        return mx >= x && mx < x + 16 && my >= y && my < y + 16;
    }

    private boolean renderHomeTip(GuiGraphics g, int mx, int my) {
        boolean visible = activeCat == Category.GENERAL && drawerOpen && activeSubEditor == null;
        if (!visible) {
            homeTipHoverAnim = UiTheme.approach(homeTipHoverAnim, 0f, 0.25f);
            return false;
        }
        int x = px + pw - 30;
        int y = py + ph - FOOTER_H + 2;
        int size = 16;
        boolean hovered = mx >= x && mx < x + size && my >= y && my < y + size;
        float speed = AnkiConfig.isUiAnimationEnabled()
                ? Math.max(0.16f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1f;
        homeTipHoverAnim = UiTheme.approach(homeTipHoverAnim, hovered ? 1f : 0f, speed);
        int accent = accentColor();
        g.fill(x, y, x + size, y + size,
                UiTheme.mix(0x1CFFFFFF, UiTheme.withAlpha(accent & 0x00FFFFFF, 78), homeTipHoverAnim));
        drawBorder(g, x, y, size, size,
                UiTheme.mix(UiTheme.themedBorder(0.8f, 1f), accent, homeTipHoverAnim));
        Component icon = UiIcons.component(UiIcons.HELP);
        g.drawString(font, icon, x + (size - font.width(icon)) / 2, y + 4,
                hovered ? UiTheme.textMain() : UiTheme.textDim(), false);
        return hovered;
    }

    private boolean isHomeTip(double mx, double my) {
        if (activeCat != Category.GENERAL || !drawerOpen || activeSubEditor != null) return false;
        int x = px + pw - 30;
        int y = py + ph - FOOTER_H + 2;
        return mx >= x && mx < x + 16 && my >= y && my < y + 16;
    }

    private int accentColor() { return UiTheme.accent(AnkiConfig.getUiAccentPreset()); }

    // ==================== INPUT ====================

    private boolean handleMouseClicked(double mx, double my, int btn) {
        if (activeSubEditor instanceof ConfirmCloseSubEditor) {
            if (btn != 0 || !drawerOpen || drawerAnim < 0.92f || contentAnim < 0.92f) return true;
            return activeSubEditor.mouseClicked(mx, my, btn, contentX, contentY, contentW, contentH);
        }
        if (inventoryParent == null && btn == 0 && EditorBrandLayer.isSettingsButton(mx, my, width)) {
            UiSound.playClick();
            openChildScreen(new AnkiConfigScreen(this));
            return true;
        }
        if (btn == 0) editorSizeFocused = false;
        if (activeSubEditor == null && (btn == 0 || btn == 1)) {
            EditorDock.SizeControl sizeControl = EditorDock.sizeControl(width, height, editorScale);
            if (btn == 0 && sizeControl.reset().contains(mx, my)) {
                resetEditorScale();
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
                resetLoreDrag();
                updateEditorScale(mx);
                return true;
            }
        }
        if (barBounds != null && barBounds.contains(mx, my)) {
            int brandW = Math.min(72, Math.max(58, barBounds.width() / 7));
            if (btn == 0 && mx < barBounds.x() + brandW) {
                draggingMenuBar = true;
                dragOffsetX = (int) Math.round(mx) - barBounds.x();
                dragOffsetY = (int) Math.round(my) - barBounds.y();
                return true;
            }
            int toolW = 26;
            int toolsStart = barBounds.x() + barBounds.width() - toolW * 2;
            int tabStart = barBounds.x() + brandW;
            int tabCount = Category.values().length + 1;
            int tabW = Math.max(22, (toolsStart - tabStart) / tabCount);
            for (int i = 0; i < Category.values().length; i++) {
                int tx = tabStart + i * tabW;
                if (mx >= tx && mx < tx + tabW) {
                    UiSound.playClick();
                    resetLoreDrag();
                    if (activeCat == Category.values()[i]) {
                        drawerOpen = !drawerOpen;
                    } else {
                        activeCat = Category.values()[i];
                        drawerOpen = true;
                        contentAnim = 0f;
                        scrollOff = 0;
                        activeSubEditor = null;
                    }
                    return true;
                }
            }
            int advancedX = tabStart + Category.values().length * tabW;
            if (mx >= advancedX && mx < advancedX + tabW) {
                UiSound.playClick();
                switchToAdvanced();
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
        if (!drawerOpen || drawerAnim < 0.92f || contentAnim < 0.92f
                || drawerBounds == null || !drawerBounds.contains(mx, my)) return false;
        if (activeSubEditor != null) return activeSubEditor.mouseClicked(mx, my, btn, contentX, contentY, contentW, contentH);
        if (isLoreTip(mx, my) || isHomeTip(mx, my)) return true;
        if (hoverRow >= 0) {
            List<ActionRow> rows = getRowsForCategory(activeCat);
            if (hoverRow < rows.size()) {
                ActionRow row = rows.get(hoverRow);
                if (row.sectionHeader) {
                    UiSound.playClick();
                    return true;
                }
                if (btn == 0 && row.reorderIndex >= 0 && drawerAnim >= 0.9f && contentAnim >= 0.9f
                        && isLoreDragHandle(hoverRow, mx, my)) {
                    UiSound.playClick();
                    draggingLoreIndex = row.reorderIndex;
                    loreDropIndex = row.reorderIndex;
                    lastLoreAutoScrollAt = System.currentTimeMillis();
                    return true;
                }
                if ("cards".equals(AnkiConfig.getUiOptionStyle())) {
                    if (handleCardActionClick(row, hoverRow, mx, my)) return true;
                    UiSound.playClick();
                    row.action.run();
                    return true;
                }
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

    private boolean handleCardActionClick(ActionRow row, int index, double mx, double my) {
        int columns = cardColumns();
        int gap = cardGap();
        int cardHeight = cardHeight();
        int cardWidth = Math.max(88, (contentW - gap * (columns - 1)) / columns);
        int local = index - scrollOff;
        int x = contentX + local % columns * (cardWidth + gap);
        int y = contentY + local / columns * (cardHeight + gap);
        int right = x + cardWidth - 6;
        if (row.deleteAction != null) {
            int bx = right - 18;
            if (mx >= bx && mx < bx + 18 && my >= y + 5 && my < y + 21) {
                UiSound.playClick();
                row.deleteAction.run();
                return true;
            }
            right = bx - 3;
        }
        int buttonY = y + cardHeight - 19;
        if (row.moveDown != null) {
            int bx = right - 16;
            if (mx >= bx && mx < bx + 16 && my >= buttonY && my < buttonY + 14) {
                UiSound.playClick();
                row.moveDown.run();
                return true;
            }
            right = bx - 2;
        }
        if (row.moveUp != null) {
            int bx = right - 16;
            if (mx >= bx && mx < bx + 16 && my >= buttonY && my < buttonY + 14) {
                UiSound.playClick();
                row.moveUp.run();
                return true;
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
        if (isInsideEditor(mx, my)) return true;

        if (inventoryParent != null) {
            Slot hovered = EditorDock.hoveredSlot(inventoryParent);
            if (btn == 0 && EditorDock.isPlayerInventorySlot(hovered) && hovered.hasItem()) {
                return selectInventoryItem(hovered.getItem(), hovered.getContainerSlot());
            }
            return inventoryParent.mouseClicked(event, isDoubleClick);
        }
        return super.mouseClicked(event, isDoubleClick);
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();
        if (resizingEditor && button == 0) {
            updateEditorScale(mx);
            return true;
        }
        if (draggingLoreIndex >= 0 && button == 0) {
            updateLoreDropTarget(mx, my);
            return true;
        }
        if (draggingMenuBar && button == 0) {
            applyMenuLayout(EditorDock.menuLayoutAt(width, height, 286, inventoryParent != null,
                    (int) Math.round(mx) - dragOffsetX, (int) Math.round(my) - dragOffsetY,
                    editorScale, editorWidthAdjustment, editorHeightAdjustment));
            return true;
        }
        if (this.activeSubEditor != null && this.activeSubEditor.mouseDragged(mx, my, button, dragX, dragY, this.contentX, this.contentY, this.contentW, this.contentH)) {
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
        if (draggingLoreIndex >= 0) {
            finishLoreDrag();
            return true;
        }
        if (draggingMenuBar) {
            draggingMenuBar = false;
            if (inventoryParent != null && barBounds != null) {
                AnkiConfig.setItemEditorCustomPosition(barBounds.x(), barBounds.y());
            }
            return true;
        }
        if (activeSubEditor != null
                && activeSubEditor.mouseReleased(event.x(), event.y(), event.button(),
                contentX, contentY, contentW, contentH)) {
            return true;
        }
        return super.mouseReleased(event);
    }

    private void updateEditorScale(double mouseX) {
        editorScale = EditorDock.sizeScaleFromMouse(width, mouseX);
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, inventoryParent != null,
                current, 286, editorScale, editorWidthAdjustment, editorHeightAdjustment));
    }

    private void adjustEditorScale(float delta) {
        editorScale = Math.max(0.0f, Math.min(1.0f, editorScale + delta));
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, inventoryParent != null,
                current, 286, editorScale, editorWidthAdjustment, editorHeightAdjustment));
        AnkiConfig.setItemEditorScale(editorScale);
    }

    private void adjustEditorAxes(float widthDelta, float heightDelta) {
        editorWidthAdjustment = EditorDock.adjustAxis(editorWidthAdjustment, widthDelta);
        editorHeightAdjustment = EditorDock.adjustAxis(editorHeightAdjustment, heightDelta);
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, inventoryParent != null,
                current, 286, editorScale, editorWidthAdjustment, editorHeightAdjustment));
        AnkiConfig.setItemEditorAxisAdjustments(editorWidthAdjustment, editorHeightAdjustment);
    }

    private void resetEditorScale() {
        editorScale = EditorDock.DEFAULT_EDITOR_SCALE;
        editorWidthAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
        editorHeightAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, inventoryParent != null,
                current, 286, editorScale, editorWidthAdjustment, editorHeightAdjustment));
        AnkiConfig.setItemEditorScale(editorScale);
        AnkiConfig.setItemEditorAxisAdjustments(editorWidthAdjustment, editorHeightAdjustment);
        UiSound.playClick();
    }
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (!drawerOpen || drawerAnim < 0.92f || contentAnim < 0.92f) return false;
        if (draggingLoreIndex >= 0) return true;
        if (activeSubEditor != null) return activeSubEditor.mouseScrolled(sx, sy);
        List<ActionRow> rows = getRowsForCategory(activeCat);
        if ("cards".equals(AnkiConfig.getUiOptionStyle())) {
            int columns = cardColumns();
            scrollOff -= (int) Math.signum(sy) * columns;
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, rows.size() - cardCapacity())));
        } else {
            scrollOff -= (int) sy * 3;
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, rows.size() - maxRows)));
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key(); int scan = event.scancode(); int mod = event.modifiers();
        if (draggingLoreIndex >= 0) {
            if (key == 256) resetLoreDrag();
            return true;
        }
        if (activeSubEditor != null) {
            if (key == 256) { activeSubEditor = null; return true; }
            return activeSubEditor.keyPressed(key, scan, mod);
        }
        if (editorSizeFocused) {
            if (key == 263) { adjustEditorScale(-0.05f); return true; }
            if (key == 262) { adjustEditorScale(0.05f); return true; }
            if (key == 268) { adjustEditorScale(-1.0f); return true; }
            if (key == 269) { adjustEditorScale(1.0f); return true; }
            if (key == 256) { editorSizeFocused = false; return true; }
        }
        if (key == 256) { tryClose(); return true; }
        if (key == 83 && (mod & 2) != 0) { saveToItem(); return true; }
        return super.keyPressed(event);
    }

    private void tryClose() {
        if (dirty && com.ankinbt.config.AnkiConfig.isConfirmOnClose()) {
            drawerOpen = true;
            contentAnim = 0f;
            activeSubEditor = new ConfirmCloseSubEditor();
        } else {
            onClose();
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (activeSubEditor != null) return activeSubEditor.charTyped(event.codepoint(), 0);
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
                    () -> activeSubEditor = new PotionPickerSubEditor(), accentColor(), null, null, ItemEditorVisuals.potionRowIcon(editStack.getItem().builtInRegistryHolder().key().location().toString())));
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
        rows.add(new ActionRow(tr("ankinbt.simple.hide_enchantments"),
                EnchantmentTooltipHelper.isHidden(editStack) ? tr("ankinbt.simple.on") : tr("ankinbt.simple.off"),
                this::toggleEnchantmentsHidden));
        List<EnchantRowData> sorted = new ArrayList<>();
        enchants.entrySet().forEach(entry -> {
            Holder<Enchantment> ench = entry.getKey();
            String eId = ench.unwrapKey().map(this::registryKeyToString).orElse("?");
            sorted.add(new EnchantRowData(eId, entry.getIntValue(), enchantGroup(ench.value())));
        });
        sorted.sort(Comparator.comparingInt(EnchantRowData::group)
                .thenComparing(data -> getEnchantDisplayName(data.id()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(EnchantRowData::id));
        int lastGroup = -1;
        for (EnchantRowData data : sorted) {
            if (data.group() != lastGroup) {
                lastGroup = data.group();
                rows.add(ActionRow.section(tr(enchantGroupKey(lastGroup)), accentColor()));
            }
            String displayName = getEnchantDisplayName(data.id());
            rows.add(new ActionRow(displayName, tr("ankinbt.simple.level") + data.level(),
                    () -> openInlineEditor("ench_level:" + data.id(), String.valueOf(data.level())), UiTheme.textMain(), null, null,
                    ItemEditorVisuals.enchantIconStack(data.id()), () -> removeEnchantment(data.id())));
        }
        rows.add(new ActionRow(tr("ankinbt.simple.add_enchant"), null,
                () -> activeSubEditor = new EnchantPickerSubEditor(), accentColor()));
        if (!enchants.isEmpty()) {
            rows.add(new ActionRow(tr("ankinbt.simple.clear_enchants"), null, this::clearEnchantments, ERROR_C));
        }
        return rows;
    }

    private List<ActionRow> getLoreRows() {
        List<ActionRow> rows = new ArrayList<>();
        List<Component> lore = getLore();
        if (lore.isEmpty()) {
            rows.add(new ActionRow(tr("ankinbt.simple.lore_empty"), null,
                    () -> activeSubEditor = new LoreTextEditorSubEditor(false), UiTheme.textDim()));
        }
        for (int i = 0; i < lore.size(); i++) {
            String text = lore.get(i).getString();
            if (text.length() > 30) text = text.substring(0, 27) + "...";
            String prefix = (i + 1) + ". " + text;
            final int fi = i;
            rows.add(new ActionRow(prefix, null,
                    () -> openLoreEditor("lore:" + fi, getLoreRawText(fi)),
                    UiTheme.textMain(), null, null,
                    ItemStack.EMPTY, () -> removeLore(fi)).reorderable(fi));
        }
        rows.add(new ActionRow(tr("ankinbt.simple.add_lore"), null,
                () -> activeSubEditor = new LoreTextEditorSubEditor(true), accentColor()));
        if (!lore.isEmpty()) {
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
                    () -> openInlineEditor("attr_amount:" + idx, String.valueOf(amount)), UiTheme.textMain(), null, null, ItemEditorVisuals.attributeIconStack(attrId),
                    () -> removeAttribute(idx)));
            if (AnkiConfig.isAttributeNotesEnabled()) {
                String note = getAttrNote(attrId);
                if (note != null && !note.isBlank()) {
                    rows.add(new ActionRow("i  " + note, null, () -> {}, UiTheme.textDim()));
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
        setStatus(tr("ankinbt.simple.attrs_cleared"), UiTheme.textDim());
    }

    private void addAttribute(String attrId, double amount, AttributeModifier.Operation op, EquipmentSlotGroup slot) {
        Optional<Holder.Reference<Attribute>> holder = VersionCompat.get().getAttributeHolder(attrId);
        if (holder.isEmpty()) {
            setStatus(tr("ankinbt.status.save_error"), ERROR_C);
            return;
        }

        var attrComp = editStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(attrComp.modifiers());
        ResourceLocation modId = ResourceLocation.fromNamespaceAndPath("ankinbt", "custom_" + System.currentTimeMillis());
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
        rows.add(new ActionRow(tr("ankinbt.simple.reset"), null, this::openResetConfirm, ERROR_C));
        return rows;
    }

    private boolean supportsContainerPreview(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        String itemId = "";
        try {
            itemId = stack.getItem().builtInRegistryHolder().key().location().toString();
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
        boolean[] firstSegment = {true};
        comp.visit((style, text) -> {
            if (text.isEmpty()) return Optional.empty();
            if (!firstSegment[0]) {
                // Each component segment has a complete style. Reset before reapplying it so
                // disabled color and format flags do not leak into the following segment.
                sb.append("&r");
            }
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
            firstSegment[0] = false;
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
        if (lines == null || lines.isEmpty()) {
            editStack.remove(DataComponents.LORE);
        } else {
            editStack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lines));
        }
        dirty = true;
    }

    private void moveLore(int from, int to) {
        List<Component> lore = new ArrayList<>(getLore());
        if (from < 0 || from >= lore.size() || to < 0 || to >= lore.size()) return;
        Component moved = lore.remove(from);
        lore.add(to, moved);
        setLore(lore);
        setStatus(tr("ankinbt.simple.lore_moved"), UiTheme.textDim());
    }

    private void removeLastLore() {
        List<Component> lore = new ArrayList<>(getLore());
        if (!lore.isEmpty()) { lore.remove(lore.size() - 1); setLore(lore); setStatus(tr("ankinbt.status.deleted"), UiTheme.textDim()); }
    }

    private void removeLore(int index) {
        List<Component> lore = new ArrayList<>(getLore());
        if (index < 0 || index >= lore.size()) return;
        lore.remove(index);
        setLore(lore);
        setStatus(tr("ankinbt.status.deleted"), UiTheme.textDim());
    }

    private void clearLore() {
        editStack.remove(DataComponents.LORE); dirty = true;
        setStatus(tr("ankinbt.simple.lore_cleared"), UiTheme.textDim());
    }

    private void toggleEnchantmentsHidden() {
        boolean hidden = !EnchantmentTooltipHelper.isHidden(editStack);
        if (EnchantmentTooltipHelper.setHidden(editStack, hidden)) {
            markDirty();
        } else {
            setStatus(tr("ankinbt.status.save_error"), ERROR_C);
        }
    }

    private void clearEnchantments() {
        boolean hidden = EnchantmentTooltipHelper.isHidden(editStack);
        EnchantmentHelper.setEnchantments(editStack, ItemEnchantments.EMPTY);
        if (hidden) EnchantmentTooltipHelper.setHidden(editStack, true);
        dirty = true;
        setStatus(tr("ankinbt.simple.enchants_cleared"), UiTheme.textDim());
    }

    private void removeEnchantment(String enchId) {
        if (applyEnchantLevel(enchId, 0)) {
            dirty = true;
            setStatus(tr("ankinbt.status.deleted"), UiTheme.textDim());
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
            String id = editStack.getItem().builtInRegistryHolder().key().location().toString();
            String cmd = "/give @s " + id + " " + editStack.getCount();
            Minecraft.getInstance().keyboardHandler.setClipboard(cmd);
            setStatus(tr("ankinbt.simple.cmd_copied"), SUCCESS);
        }
    }

    private void openResetConfirm() {
        activeSubEditor = new ConfirmResetSubEditor();
    }

    private void resetItem() {
        editStack = originalStack.copy(); dirty = false;
        setStatus(tr("ankinbt.simple.reset_done"), UiTheme.textDim());
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

    /**
     * Enchantment priority follows the item's own Minecraft definition:
     * primary (item-specific) first, supported (general) second, and entries
     * that are not declared for this item last.  Keeping this in one helper
     * makes the picker and the applied-enchantment list use identical rules.
     */
    private int enchantGroup(Enchantment enchantment) {
        if (enchantment == null) return 2;
        try {
            if (enchantment.isPrimaryItem(editStack)) return 0;
            if (enchantment.isSupportedItem(editStack)) return 1;
        } catch (Throwable ignored) {
            // A datapack can provide an incomplete definition; keep it visible
            // in the final group instead of breaking the editor.
        }
        return 2;
    }

    private int enchantGroup(String enchantId) {
        try {
            Optional<Holder.Reference<Enchantment>> holder = VersionCompat.get().getEnchantHolder(enchantId);
            return holder.map(reference -> enchantGroup(reference.value())).orElse(2);
        } catch (Throwable ignored) {
            return 2;
        }
    }

    private Set<String> appliedEnchantIds() {
        Set<String> applied = new HashSet<>();
        try {
            EnchantmentHelper.getEnchantmentsForCrafting(editStack).entrySet().forEach(entry ->
                    entry.getKey().unwrapKey().map(this::registryKeyToString)
                            .map(this::normalizeRegistryDisplayId)
                            .filter(Objects::nonNull)
                            .ifPresent(applied::add));
        } catch (Throwable ignored) {
            // A malformed component must not prevent the picker from opening.
        }
        return applied;
    }

    private String enchantGroupKey(int group) {
        return switch (group) {
            case 0 -> "ankinbt.simple.enchant_group_specific";
            case 1 -> "ankinbt.simple.enchant_group_general";
            default -> "ankinbt.simple.enchant_group_other";
        };
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
                    List<Component> replacement = loreComponentsFromInput(value, isLore);
                    lore.remove(idx);
                    lore.addAll(idx, replacement);
                    setLore(lore);
                }
            } else if (field.equals("lore_add")) {
                List<Component> lore = new ArrayList<>(getLore());
                lore.addAll(loreComponentsFromInput(value, isLore));
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
            setStatus(tr("ankinbt.status.edited"), UiTheme.textDim());
        } catch (NumberFormatException e) {
            setStatus(tr("ankinbt.simple.invalid_number"), ERROR_C);
        }
        activeSubEditor = null;
    }

    private List<Component> loreComponentsFromInput(String value, boolean colorCoded) {
        String[] lines = (value == null ? "" : value).split("\\R", -1);
        List<Component> result = new ArrayList<>(Math.max(1, lines.length));
        for (String line : lines) {
            result.add(colorCoded ? colorCodedToComponent(line) : Component.literal(line));
        }
        return result;
    }

    private boolean applyEnchantLevel(String enchId, int level) {
        ResourceLocation loc = ResourceLocation.tryParse(enchId);
        if (loc == null) return false;
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(editStack));
        if (level <= 0) mutable.removeIf(h -> h.unwrapKey().map(k -> k.location().equals(loc)).orElse(false));
        else {
            Optional<Holder.Reference<Enchantment>> holder = VersionCompat.get().getEnchantHolder(enchId);
            if (holder.isEmpty()) return false;
            mutable.set(holder.get(), level);
        }
        EnchantmentHelper.setEnchantments(editStack, mutable.toImmutable());
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

    private void saveToItem() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (InventoryEditorOverlay.isItemEditorPreviewMode()) {
            setStatus(tr("ankinbt.status.preview_only"), ERROR_C);
            return;
        }
        if (!mc.player.hasInfiniteMaterials()) {
            setStatus(tr("ankinbt.status.creative_only"), ERROR_C);
            return;
        }

        VersionCompat.get().sanitizeForCreativeSave(editStack);
        if (inventorySlot >= 0) {
            // Save to specific inventory slot
            mc.player.getInventory().setItem(inventorySlot, editStack.copy());
            mc.gameMode.handleCreativeModeItemAdd(editStack.copy(), packetSlot(inventorySlot));
        } else {
            int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
            mc.player.getInventory().setItem(slot, editStack.copy());
            mc.gameMode.handleCreativeModeItemAdd(editStack.copy(), 36 + slot);
        }
        originalStack = editStack.copy();
        dirty = false;
        setStatus(tr("ankinbt.status.saved"), SUCCESS);
    }

    private void switchToAdvanced() {
        resetLoreDrag();
        if (inventoryParent != null) {
            InventoryEditorOverlay.switchToAdvanced(inventoryParent, editStack, originalStack, inventorySlot, dirty);
        } else {
            NbtEditorScreen next = new NbtEditorScreen(editStack, inventorySlot);
            next.restoreEditorState(originalStack, dirty);
            AnkiConfig.setPreferredItemEditor("advanced");
            Minecraft.getInstance().setScreen(next);
        }
    }

    private void openInventorySwitch() {
        activeSubEditor = new InventorySwitchSubEditor();
    }

    private void markDirty() { dirty = true; setStatus(tr("ankinbt.status.edited"), UiTheme.textDim()); }

    private void setStatus(String msg, int color) {
        statusMsg = msg; statusColor = color; statusTime = System.currentTimeMillis();
    }

    private static String tr(String key) { return Component.translatable(key).getString(); }

    private int currentEditedSlot() {
        if (inventorySlot >= 0) return inventorySlot;
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
        selectInventoryItem(stack, slot);
    }

    boolean selectInventoryItem(ItemStack stack, int slot) {
        if (stack == null || stack.isEmpty()) return false;
        if (slot == inventorySlot && ItemStack.isSameItemSameComponents(stack, originalStack)) return true;
        if (dirty) {
            setStatus(tr("ankinbt.status.save_before_switch"), ERROR_C);
            return true;
        }
        originalStack = stack.copy();
        editStack = stack.copy();
        resetLoreDrag();
        inventorySlot = slot;
        activeSubEditor = null;
        scrollOff = 0;
        sideScrollOff = 0;
        hoverRow = -1;
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
        if (activeSubEditor instanceof ConfirmCloseSubEditor) return true;
        if (activeSubEditor == null) {
            EditorDock.SizeControl sizeControl = EditorDock.sizeControl(width, height, editorScale);
            if (sizeControl.hit().contains(mouseX, mouseY)
                    || sizeControl.reset().contains(mouseX, mouseY)
                    || sizeControl.horizontal().contains(mouseX, mouseY)
                    || sizeControl.vertical().contains(mouseX, mouseY)) return true;
        }
        if (barBounds != null && barBounds.contains(mouseX, mouseY)) return true;
        return drawerOpen && drawerAnim > 0.08f && drawerBounds != null && drawerBounds.contains(mouseX, mouseY);
    }

    boolean isDraggingMenuBar() {
        return draggingMenuBar || resizingEditor || draggingLoreIndex >= 0;
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

    private static int packetSlot(int inventorySlot) {
        if (inventorySlot == 40) return 45;
        return inventorySlot < 9 ? 36 + inventorySlot : inventorySlot;
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

    private void renderTextBuffer(GuiGraphics g, net.minecraft.client.gui.Font font, TextEditBuffer buffer,
                                  int x, int y, int w, int h, boolean focused, String hint) {
        String value = buffer.value();
        if (value.isEmpty()) {
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, hint == null ? "" : hint,
                    x + 4, y + Math.max(4, (h - 8) / 2), UiTheme.textDim(), false);
            return;
        }
        int maxWidth = Math.max(1, w - 8);
        int start = textViewStart(font, value, buffer.cursor(), maxWidth);
        String shown = visibleText(font, value, start, maxWidth);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, shown, x + 4,
                y + Math.max(4, (h - 8) / 2), UiTheme.textMain(), false);
        if (buffer.hasSelection()) {
            int selectionStart = Math.max(start, buffer.selectionStart());
            int selectionEnd = Math.min(value.length(), Math.max(selectionStart, buffer.selectionEnd()));
            if (selectionStart < selectionEnd) {
                int sx = x + 4 + font.width(value.substring(start, selectionStart));
                int ex = x + 4 + font.width(value.substring(start, selectionEnd));
                g.fill(sx, y + 3, Math.max(sx + 1, ex), y + h - 3, 0x663B82F6);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                        value.substring(selectionStart, selectionEnd), sx,
                        y + Math.max(4, (h - 8) / 2), UiTheme.textMain(), false);
            }
        }
        if (focused && !buffer.hasSelection() && System.currentTimeMillis() % 1000L < 500L) {
            int cursor = Math.max(start, Math.min(buffer.cursor(), value.length()));
            int cx = x + 4 + font.width(value.substring(start, cursor));
            g.fill(cx, y + 3, cx + 1, y + h - 3, UiTheme.textMain());
        }
    }

    private int bufferCursorFromMouse(net.minecraft.client.gui.Font font, TextEditBuffer buffer,
                                      int relX, int maxWidth) {
        return plainCursorFromMouse(font, buffer.value(),
                textViewStart(font, buffer.value(), buffer.cursor(), maxWidth), relX, maxWidth);
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

    class ConfirmResetSubEditor implements SubEditor {
        private float modalAnim;
        private final float[] buttonHover = new float[2];

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my,
                           int x, int y, int w, int h) {
            float speed = AnkiConfig.isUiAnimationEnabled()
                    ? Math.max(0.12f, AnkiConfig.getUiAnimationSpeed() * 1.8f) : 1f;
            modalAnim = UiTheme.approach(modalAnim, 1f, speed);
            g.fill(x, y, x + w, y + h, UiTheme.withAlpha(0x000000, Math.round(92 * modalAnim)));
            int dw = Math.min(320, w - 12), dh = Math.min(118, h - 8);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2 + Math.round((1f - modalAnim) * 12f);
            int accent = accentColor();
            g.fill(dx, dy, dx + dw, dy + dh,
                    UiTheme.surface(Math.max(0.72f, AnkiConfig.getUiOpacity()), modalAnim));
            drawBorder(g, dx, dy, dw, dh,
                    UiTheme.withAlpha(0xEF4444, Math.round(230 * modalAnim)));
            g.fill(dx + 1, dy + 1, dx + dw - 1, dy + 3,
                    UiTheme.withAlpha(0xEF4444, Math.round(220 * modalAnim)));
            g.drawString(font, tr("ankinbt.simple.reset"), dx + 12, dy + 12, UiTheme.textMain(), false);
            g.drawString(font, tr("ankinbt.confirm.unsaved"), dx + 12, dy + 35, UiTheme.textMain(), false);
            g.drawString(font, tr("ankinbt.confirm.discard_hint"), dx + 12, dy + 50, UiTheme.textDim(), false);

            int by = dy + dh - 34;
            int gap = 8;
            int bw = (dw - 24 - gap) / 2;
            renderResetButton(g, font, mx, my, dx + 12, by, bw, 24,
                    tr("ankinbt.edit.cancel"), 0, accent, speed);
            renderResetButton(g, font, mx, my, dx + 12 + bw + gap, by, bw, 24,
                    tr("ankinbt.simple.reset"), 1, 0xFFEF4444, speed);
        }

        private void renderResetButton(GuiGraphics g, net.minecraft.client.gui.Font font,
                                       int mx, int my, int x, int y, int w, int h,
                                       String label, int index, int color, float speed) {
            boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
            buttonHover[index] = UiTheme.approach(buttonHover[index], hover ? 1f : 0f, speed);
            g.fill(x, y, x + w, y + h,
                    UiTheme.withAlpha(color & 0x00FFFFFF, 42 + Math.round(58 * buttonHover[index])));
            drawBorder(g, x, y, w, h,
                    UiTheme.withAlpha(color & 0x00FFFFFF, 170 + Math.round(70 * buttonHover[index])));
            String shown = font.width(label) <= w - 12 ? label : font.plainSubstrByWidth(label, w - 18) + "...";
            g.drawString(font, shown, x + (w - font.width(shown)) / 2, y + 8, UiTheme.textMain(), false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            if (btn != 0) return true;
            int dw = Math.min(320, w - 12), dh = Math.min(118, h - 8);
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2 + Math.round((1f - modalAnim) * 12f);
            int by = dy + dh - 34;
            int gap = 8;
            int bw = (dw - 24 - gap) / 2;
            if (mx >= dx + 12 && mx < dx + 12 + bw && my >= by && my < by + 24) {
                activeSubEditor = null;
                return true;
            }
            int resetX = dx + 12 + bw + gap;
            if (mx >= resetX && mx < resetX + bw && my >= by && my < by + 24) {
                resetItem();
                activeSubEditor = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 256) activeSubEditor = null;
            return true;
        }

        @Override public boolean charTyped(char c, int mod) { return true; }
    }

    class ConfirmCloseSubEditor implements SubEditor {
        private float modalAnim;
        private final float[] buttonHover = new float[3];

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.12f, AnkiConfig.getUiAnimationSpeed() * 1.8f) : 1f;
            modalAnim = UiTheme.approach(modalAnim, 1f, speed);
            g.fill(x, y, x + w, y + h, UiTheme.withAlpha(0x000000, Math.round(92 * modalAnim)));
            int dw = Math.min(320, w - 12), dh = Math.min(126, h - 8);
            int dx = x + (w - dw) / 2, dy = dialogY(y, h, dh);
            int accent = accentColor();
            g.fill(dx, dy, dx + dw, dy + dh,
                    UiTheme.surface(Math.max(0.72f, AnkiConfig.getUiOpacity()), modalAnim));
            drawBorder(g, dx, dy, dw, dh, UiTheme.withAlpha(accent & 0x00FFFFFF, Math.round(230 * modalAnim)));

            g.drawString(font, tr("ankinbt.confirm.title"), dx + 12, dy + 10, UiTheme.textMain(), false);
            g.fill(dx + 1, dy + 25, dx + dw - 1, dy + 27, accent);
            g.drawString(font, tr("ankinbt.confirm.unsaved"), dx + 12, dy + 38, UiTheme.textMain(), false);
            g.drawString(font, tr("ankinbt.confirm.discard_hint"), dx + 12, dy + 53, UiTheme.textDim(), false);

            int by = dy + dh - 34;
            int gap = 7;
            int bw2 = Math.max(66, (dw - 24 - gap * 2) / 3), bh2 = 24;

            // Save & Close
            int saveX = dx + 12;
            boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
            buttonHover[0] = UiTheme.approach(buttonHover[0], sh ? 1f : 0f, speed);
            g.fill(saveX, by, saveX + bw2, by + bh2,
                    UiTheme.mix(UiTheme.withAlpha(accent & 0x00FFFFFF, 164), accent, buttonHover[0]));
            String saveLabel = tr("ankinbt.confirm.save_close");
            g.drawString(font, saveLabel, saveX + (bw2 - font.width(saveLabel)) / 2, by + 7, UiTheme.textMain(), false);

            // Discard
            int discardX = saveX + bw2 + gap;
            boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
            buttonHover[1] = UiTheme.approach(buttonHover[1], dh2 ? 1f : 0f, speed);
            g.fill(discardX, by, discardX + bw2, by + bh2,
                    UiTheme.mix(0x35EF4444, 0x88EF4444, buttonHover[1]));
            String discardLabel = tr("ankinbt.confirm.discard");
            g.drawString(font, discardLabel, discardX + (bw2 - font.width(discardLabel)) / 2, by + 7, UiTheme.textMain(), false);

            // Cancel
            int cancelX = discardX + bw2 + gap;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            buttonHover[2] = UiTheme.approach(buttonHover[2], ch ? 1f : 0f, speed);
            g.fill(cancelX, by, cancelX + bw2, by + bh2,
                    UiTheme.mix(0x24FFFFFF, 0x58FFFFFF, buttonHover[2]));
            g.drawString(font, tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 7, UiTheme.textDim(), false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            int dw = Math.min(320, w - 12), dh = Math.min(126, h - 8);
            int dx = x + (w - dw) / 2, dy = dialogY(y, h, dh);
            int by = dy + dh - 34;
            int gap = 7;
            int bw2 = Math.max(66, (dw - 24 - gap * 2) / 3), bh2 = 24;

            int saveX = dx + 12;
            if (mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2) {
                saveToItem();
                if (!dirty) onClose();
                return true;
            }
            int discardX = saveX + bw2 + gap;
            if (mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2) {
                dirty = false; onClose(); return true;
            }
            int cancelX = discardX + bw2 + gap;
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

        @Override public boolean charTyped(char c, int mod) { return true; }

        private int dialogY(int y, int h, int dialogHeight) {
            return y + (h - dialogHeight) / 2 + Math.round((1f - modalAnim) * 12f);
        }
    }

    // ==================== NBT EXPORT ====================

    class NbtExportSubEditor implements SubEditor {
        final TextEditBuffer fileNameInput;
        final TextEditBuffer categoryInput;
        final TextEditBuffer aliasInput;
        int focusField = 0; // 0=filename, 1=category, 2=alias
        boolean draggingText = false;
        String message = null;
        int msgColor = UiTheme.textDim();

        NbtExportSubEditor() {
            String itemId = resolveStackRegistryPath(editStack);
            long ts = System.currentTimeMillis() / 1000;
            fileNameInput = new TextEditBuffer(itemId + "_" + ts);
            categoryInput = new TextEditBuffer(com.ankinbt.config.AnkiConfig.getLastExportCategory());
            aliasInput = new TextEditBuffer("");
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(Math.max(1, w - 16), 420), dh = Math.min(Math.max(1, h - 10), 210);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, dx, dy, dw, dh, accentColor());

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.export.title"), dx + 12, dy + 9, UiTheme.textMain(), false);
            g.fill(dx + 1, dy + 25, dx + dw - 1, dy + 26, UiTheme.themedBorder(1f, 1f));

            int labelW = Math.max(font.width(tr("ankinbt.export.category")),
                    Math.max(font.width(tr("ankinbt.export.filename")), font.width(tr("ankinbt.export.alias")))) + 12;
            int labelX = dx + 12;
            int fieldX = labelX + labelW;
            int fieldW = Math.max(50, dx + dw - 12 - fieldX);
            int fieldH = 20;
            String category = categoryInput.value();
            int categoryY = dy + 34;
            int fileNameY = categoryY + 30;
            int aliasY = fileNameY + 30;

            renderExportLabel(g, font, tr("ankinbt.export.category"), labelX, categoryY);
            renderExportInput(g, font, categoryInput, fieldX, categoryY, fieldW, fieldH,
                    focusField == 1, tr("ankinbt.export.no_category"));

            renderExportLabel(g, font, tr("ankinbt.export.filename"), labelX, fileNameY);
            g.fill(fieldX, fileNameY, fieldX + fieldW, fileNameY + fieldH, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
            drawBorder(g, fieldX, fileNameY, fieldW, fieldH,
                    focusField == 0 ? accentColor() : UiTheme.themedBorder(1f, 1f));
            int suffixW = font.width(".nbt") + 6;
            renderTextBuffer(g, font, fileNameInput, fieldX, fileNameY,
                    Math.max(12, fieldW - suffixW), fieldH, focusField == 0, "");
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, ".nbt", fieldX + fieldW - font.width(".nbt") - 4,
                    fileNameY + 6, UiTheme.textDim(), false);

            renderExportLabel(g, font, tr("ankinbt.export.alias"), labelX, aliasY);
            renderExportInput(g, font, aliasInput, fieldX, aliasY, fieldW, fieldH,
                    focusField == 2, tr("ankinbt.export.alias_hint"));

            int pathY = aliasY + 30;
            g.fill(labelX, pathY, dx + dw - 12, pathY + 32, UiTheme.card(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, labelX, pathY, dw - 24, 32, UiTheme.themedBorder(1f, 1f));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.export.dir"), labelX + 7, pathY + 5, accentColor(), false);
            String safeCategory = category.isBlank() ? null : category.trim();
            String fileName = fileNameInput.value().isBlank() ? tr("ankinbt.export.empty_name") : fileNameInput.value() + ".nbt";
            String pathPreview = com.ankinbt.config.AnkiConfig.getExportPath(safeCategory).resolve(fileName).toString();
            pathPreview = tailToWidth(pathPreview, Math.max(20, dw - 38));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, pathPreview, labelX + 7, pathY + 18, UiTheme.textDim(), false);

            int by = dy + dh - 27;
            if (message != null) {
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, message, labelX, by - 13, msgColor, false);
            }
            int gap = 6, cancelW = 64, saveAsW = 82, saveW = 68, bh = 20;
            int totalW = cancelW + saveAsW + saveW + gap * 2;
            int cancelX = dx + (dw - totalW) / 2;
            int saveAsX = cancelX + cancelW + gap;
            int saveX = saveAsX + saveAsW + gap;
            renderExportButton(g, font, mx, my, cancelX, by, cancelW, bh,
                    tr("ankinbt.edit.cancel"), false, true);
            renderExportButton(g, font, mx, my, saveAsX, by, saveAsW, bh,
                    tr("ankinbt.export.save_as"), false, hasTinyFd());
            renderExportButton(g, font, mx, my, saveX, by, saveW, bh,
                    tr("ankinbt.export.save"), true, true);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            draggingText = false;
            int dw = Math.min(Math.max(1, w - 16), 420), dh = Math.min(Math.max(1, h - 10), 210);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            int labelW = Math.max(font.width(tr("ankinbt.export.category")),
                    Math.max(font.width(tr("ankinbt.export.filename")), font.width(tr("ankinbt.export.alias")))) + 12;
            int fieldX = dx + 12 + labelW, fieldW = Math.max(50, dx + dw - 12 - fieldX), fieldH = 20;
            int categoryY = dy + 34;
            int fileNameY = categoryY + 30;
            int aliasY = fileNameY + 30;

            if (mx >= fieldX && mx < fieldX + fieldW && my >= categoryY && my < categoryY + fieldH) {
                focusField = 1;
                categoryInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, categoryInput,
                        (int) Math.round(mx) - fieldX - 4, fieldW - 8), false);
                draggingText = btn == 0;
                return true;
            }
            int suffixW = font.width(".nbt") + 6;
            if (mx >= fieldX && mx < fieldX + fieldW && my >= fileNameY && my < fileNameY + fieldH) {
                focusField = 0;
                fileNameInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, fileNameInput,
                        (int) Math.round(mx) - fieldX - 4, Math.max(12, fieldW - suffixW - 8)), false);
                draggingText = btn == 0;
                return true;
            }
            if (mx >= fieldX && mx < fieldX + fieldW && my >= aliasY && my < aliasY + fieldH) {
                focusField = 2;
                aliasInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, aliasInput,
                        (int) Math.round(mx) - fieldX - 4, fieldW - 8), false);
                draggingText = btn == 0;
                return true;
            }

            // Buttons
            int by = dy + dh - 27;
            int gap = 6, cancelW = 64, saveAsW = 82, saveW = 68, bh = 20;
            int totalW = cancelW + saveAsW + saveW + gap * 2;
            int cancelX = dx + (dw - totalW) / 2;
            int saveAsX = cancelX + cancelW + gap;
            int saveX = saveAsX + saveAsW + gap;
            if (hit(mx, my, cancelX, by, cancelW, bh)) { activeSubEditor = null; return true; }
            if (hit(mx, my, saveAsX, by, saveAsW, bh)) { doExport(true); return true; }
            if (hit(mx, my, saveX, by, saveW, bh)) { doExport(false); return true; }
            return true;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY,
                                    int x, int y, int w, int h) {
            if (!draggingText || button != 0) return false;
            int dw = Math.min(Math.max(1, w - 16), 420), dh = Math.min(Math.max(1, h - 10), 210);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            int labelW = Math.max(font.width(tr("ankinbt.export.category")),
                    Math.max(font.width(tr("ankinbt.export.filename")), font.width(tr("ankinbt.export.alias")))) + 12;
            int fieldX = dx + 12 + labelW, fieldW = Math.max(50, dx + dw - 12 - fieldX), fieldH = 20;
            int categoryY = dy + 34;
            int fileNameY = categoryY + 30;
            int aliasY = fileNameY + 30;
            int suffixW = font.width(".nbt") + 6;
            if (focusField == 1 && my >= categoryY - 8 && my <= categoryY + fieldH + 8) {
                categoryInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, categoryInput,
                        (int) Math.round(mx) - fieldX - 4, fieldW - 8), true);
                return true;
            }
            if (focusField == 0 && my >= fileNameY - 8 && my <= fileNameY + fieldH + 8) {
                fileNameInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, fileNameInput,
                        (int) Math.round(mx) - fieldX - 4, Math.max(12, fieldW - suffixW - 8)), true);
                return true;
            }
            if (focusField == 2 && my >= aliasY - 8 && my <= aliasY + fieldH + 8) {
                aliasInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, aliasInput,
                        (int) Math.round(mx) - fieldX - 4, fieldW - 8), true);
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button, int x, int y, int w, int h) {
            boolean handled = draggingText;
            draggingText = false;
            return handled;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { doExport(false); return true; }
            if (key == 83 && (mod & 2) != 0) { doExport((mod & 1) != 0); return true; }
            if (key == 258) { focusField = (focusField + 1) % 3; return true; } // Tab
            TextEditBuffer target = focusField == 0 ? fileNameInput : (focusField == 1 ? categoryInput : aliasInput);
            target.keyPressed(key, mod);
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c < 32) return false;
            if (focusField == 0) {
                if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '"' || c == '<' || c == '>' || c == '|') return false;
                fileNameInput.charTyped(c);
            } else if (focusField == 1) {
                if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '"' || c == '<' || c == '>' || c == '|' || c == '.') return false;
                categoryInput.charTyped(c);
            } else {
                aliasInput.charTyped(c);
            }
            return true;
        }

        private void doExport(boolean saveAs) {
            String fileName = fileNameInput.value().trim();
            String category = categoryInput.value();
            String alias = aliasInput.value();
            if (fileName.isEmpty()) { message = tr("ankinbt.export.empty_name"); msgColor = ERROR_C; return; }
            var opt = NbtHelper.serializeItemStack(editStack);
            if (opt.isEmpty()) { message = tr("ankinbt.export.failed"); msgColor = ERROR_C; return; }
            var path = (Path) null;
            String safeCategory = category.isBlank() ? null : category.trim();
            AnkiConfig.setLastExportCategory(safeCategory == null ? "" : safeCategory);
            if (saveAs) {
                if (!hasTinyFd()) {
                    message = tr("ankinbt.export.dialog_unavailable");
                    msgColor = ERROR_C;
                    return;
                }
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

        private void renderExportLabel(GuiGraphics g, net.minecraft.client.gui.Font font,
                                       String label, int x, int y) {
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, x, y + 6, UiTheme.textDim(), false);
        }

        private void renderExportInput(GuiGraphics g, net.minecraft.client.gui.Font font,
                                       TextEditBuffer buffer, int x, int y, int w, int h,
                                       boolean focused, String hint) {
            g.fill(x, y, x + w, y + h, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
            drawBorder(g, x, y, w, h, focused ? accentColor() : UiTheme.themedBorder(1f, 1f));
            renderTextBuffer(g, font, buffer, x, y, w, h, focused, hint);
        }

        private void renderExportButton(GuiGraphics g, net.minecraft.client.gui.Font font,
                                        int mx, int my, int x, int y, int w, int h,
                                        String label, boolean primary, boolean enabled) {
            boolean hover = enabled && hit(mx, my, x, y, w, h);
            int color = !enabled ? UiTheme.withAlpha(UiTheme.baseRgb(), 90)
                    : primary ? (hover ? accentColor() : UiTheme.withAlpha(accentColor() & 0x00FFFFFF, 190))
                    : (hover ? BTN_HOVER : BTN_BG);
            g.fill(x, y, x + w, y + h, color);
            drawBorder(g, x, y, w, h, primary && enabled ? accentColor() : UiTheme.themedBorder(1f, 1f));
            int textColor = enabled ? (primary ? UiTheme.textMain() : UiTheme.textDim()) : UiTheme.withAlpha(UiTheme.textDim() & 0x00FFFFFF, 110);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, label,
                    x + (w - font.width(label)) / 2, y + 6, textColor, false);
        }

        private String tailToWidth(String value, int maxWidth) {
            if (value == null || font.width(value) <= maxWidth) return value == null ? "" : value;
            String prefix = "...";
            int start = 0;
            while (start < value.length() && font.width(prefix + value.substring(start)) > maxWidth) start++;
            return prefix + value.substring(Math.min(start, value.length()));
        }

        private boolean hit(double mx, double my, int x, int y, int w, int h) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
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
            g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, dx, dy, dw, dh, accentColor());

            g.drawString(font, tr("ankinbt.import.title"), dx + 10, dy + 8, UiTheme.textMain(), false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, UiTheme.themedBorder(1f, 1f));

            // Category tabs
            int tabY = dy + 26;
            int tabX = dx + 8;
            // "All" tab
            String allLabel = tr("ankinbt.import.all");
            int allW = font.width(allLabel) + 10;
            boolean allHover = mx >= tabX && mx < tabX + allW && my >= tabY && my < tabY + 16;
            boolean allActive = currentCategory.isEmpty();
            g.fill(tabX, tabY, tabX + allW, tabY + 16, allActive ? accentColor() : (allHover ? BTN_HOVER : BTN_BG));
            g.drawString(font, allLabel, tabX + 5, tabY + 4, allActive ? UiTheme.textMain() : UiTheme.textDim(), false);
            tabX += allW + 4;

            for (String cat : categories) {
                int cw = font.width(cat) + 10;
                if (tabX + cw > dx + dw - 8) break;
                boolean hover = mx >= tabX && mx < tabX + cw && my >= tabY && my < tabY + 16;
                boolean active = cat.equals(currentCategory);
                g.fill(tabX, tabY, tabX + cw, tabY + 16, active ? accentColor() : (hover ? BTN_HOVER : BTN_BG));
                g.drawString(font, cat, tabX + 5, tabY + 4, active ? UiTheme.textMain() : UiTheme.textDim(), false);
                tabX += cw + 4;
            }

            // File list area
            int listX = dx + 8, listY = tabY + 22;
            int listW = dw / 2 - 12, listH = dh - 110;
            g.fill(listX, listY, listX + listW, listY + listH, 0xFF0A0A14);
            drawBorder(g, listX, listY, listW, listH, UiTheme.themedBorder(1f, 1f));

            int rowH = 20;
            int maxItems = listH / rowH;
            hoverIdx = -1;
            if (files.isEmpty()) {
                g.drawString(font, tr("ankinbt.import.no_files"), listX + 8, listY + 8, UiTheme.textDim(), false);
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
                    g.drawString(font, name, listX + 22, ry + 6, sel ? UiTheme.textMain() : UiTheme.textDim(), false);
                }
            }

            // Preview area
            int prevX = dx + dw / 2 + 4, prevY = listY;
            int prevW = dw / 2 - 12, prevH = listH;
            g.fill(prevX, prevY, prevX + prevW, prevY + prevH, 0xFF0A0A14);
            drawBorder(g, prevX, prevY, prevW, prevH, UiTheme.themedBorder(1f, 1f));

            g.drawString(font, tr("ankinbt.import.preview"), prevX + 6, prevY + 4, UiTheme.textDim(), false);
            if (selectedIdx >= 0 && selectedIdx < files.size()) {
                var entry = files.get(selectedIdx);
                g.drawString(font, entry.name(), prevX + 6, prevY + 18, UiTheme.textMain(), false);
                if (entry.alias() != null) {
                    g.drawString(font, tr("ankinbt.export.alias") + " " + entry.alias(), prevX + 6, prevY + 30, accentColor(), false);
                    g.drawString(font, entry.sizeDisplay(), prevX + 6, prevY + 42, UiTheme.textDim(), false);
                } else {
                    g.drawString(font, entry.sizeDisplay(), prevX + 6, prevY + 30, UiTheme.textDim(), false);
                }

                int infoStartY = entry.alias() != null ? prevY + 56 : prevY + 44;
                if (previewInfo != null) {
                    String[] infoLines = previewInfo.split("\n");
                    for (int i = 0; i < Math.min(infoLines.length, (prevH - 60) / 11); i++) {
                        String line = infoLines[i];
                        if (font.width(line) > prevW - 12) line = font.plainSubstrByWidth(line, prevW - 18) + "..";
                        g.drawString(font, line, prevX + 6, infoStartY + i * 11, UiTheme.textDim(), false);
                    }
                }
            } else {
                g.drawString(font, tr("ankinbt.import.select_file"), prevX + 6, prevY + 20, UiTheme.textDim(), false);
            }

            // Buttons
            int by = dy + dh - 32;
            int bw2 = 70, bh2 = 22;

            int refX = dx + 10;
            boolean rh = mx >= refX && mx < refX + 50 && my >= by && my < by + bh2;
            g.fill(refX, by, refX + 50, by + bh2, rh ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.import.refresh"), refX + (50 - font.width(tr("ankinbt.import.refresh"))) / 2, by + 7, UiTheme.textDim(), false);
            int openW = 76;
            int openX = refX + 56;
            boolean fh = mx >= openX && mx < openX + openW && my >= by && my < by + bh2;
            g.fill(openX, by, openX + openW, by + bh2, fh ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.import.open_file"), openX + (openW - font.width(tr("ankinbt.import.open_file"))) / 2, by + 7, UiTheme.textDim(), false);

            int cancelX = dx + dw / 2 - bw2 - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? BTN_HOVER : BTN_BG);
            g.drawString(font, tr("ankinbt.edit.cancel"), cancelX + (bw2 - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 7, UiTheme.textDim(), false);

            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw2 && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw2, by + bh2, oh ? accentColor() : 0xFF4F46E5);
            g.drawString(font, tr("ankinbt.import.do_import"), okX + (bw2 - font.width(tr("ankinbt.import.do_import"))) / 2, by + 7, UiTheme.textMain(), false);
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
        private int msgColor = UiTheme.textDim();

        ContainerPreviewSubEditor() {
            loadFromItem();
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            int dw = Math.min(w - 10, 470), dh = Math.min(h - 10, 300);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, dx, dy, dw, dh, accentColor());

            g.drawString(font, tr("ankinbt.container.title"), dx + 10, dy + 8, UiTheme.textMain(), false);
            g.drawString(font, tr("ankinbt.container.mode") + ": " + modeName(), dx + 190, dy + 8, UiTheme.textDim(), false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, UiTheme.themedBorder(1f, 1f));

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
                drawBorder(g, sx, sy, cell, cell, sel ? accentColor() : UiTheme.themedBorder(1f, 1f));

                ItemStack st = stackAt(global);
                if (!st.isEmpty()) {
                    g.renderItem(st, sx + 2, sy + 2);
                    if (hover) hoveredStack = st;
                }
            }

            String slotLabel = tr("ankinbt.container.slot") + " " + selectedSlot;
            g.drawString(font, slotLabel, dx + 14, dy + 112, UiTheme.textDim(), false);
            String pageText = (page + 1) + " / " + Math.max(1, (slotTags.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            g.drawString(font, pageText, dx + 14 + font.width(slotLabel) + 18, dy + 112, UiTheme.textDim(), false);

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
            msgColor = UiTheme.textDim();
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
            msgColor = UiTheme.textDim();
        }

        private void openPicker() {
            final int slot = selectedSlot;
            ItemPickerScreen picker = new ItemPickerScreen(SimpleEditorScreen.this, id -> {
                ensureSlots(slot + 1);
                Item item = com.ankinbt.util.ItemRegistryHelper.resolveItem(id);
                if (item == null) return;
                ItemStack stack = new ItemStack(item, 1);
                Optional<CompoundTag> tag = NbtHelper.serializeItemStack(stack);
                if (tag.isPresent()) {
                    slotTags.set(slot, copyTag(tag.get()));
                    slotStacks.set(slot, stack.copy());
                    message = tr("ankinbt.status.edited");
                    msgColor = UiTheme.textDim();
                }
            }, SimpleEditorScreen.this::returnFromChildScreen);
            openChildScreen(picker);
        }

        private void clearSelected() {
            ensureSlots(selectedSlot + 1);
            slotTags.set(selectedSlot, null);
            slotStacks.set(selectedSlot, ItemStack.EMPTY);
            message = tr("ankinbt.status.deleted");
            msgColor = UiTheme.textDim();
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
            g.drawString(font, draw, x + (w - font.width(draw)) / 2, y + 6, UiTheme.textDim(), false);
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

    private record EnchantRowData(String id, int level, int group) {}

    private static final class EnchantPickerRow {
        final boolean header;
        final int enchantIndex;
        final int group;

        private EnchantPickerRow(boolean header, int enchantIndex, int group) {
            this.header = header;
            this.enchantIndex = enchantIndex;
            this.group = group;
        }

        static EnchantPickerRow header(int group) {
            return new EnchantPickerRow(true, -1, group);
        }

        static EnchantPickerRow enchant(int index, int group) {
            return new EnchantPickerRow(false, index, group);
        }
    }

    static class ActionRow {
        final String label; final String currentValue; final Runnable action; final int labelColor;
        final Runnable moveUp; final Runnable moveDown; final ItemStack icon; final Runnable deleteAction;
        boolean sectionHeader;
        int reorderIndex = -1;
        ActionRow(String label, String currentValue, Runnable action) { this(label, currentValue, action, UiTheme.textMain(), null, null, ItemStack.EMPTY); }
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

        static ActionRow section(String label, int color) {
            ActionRow row = new ActionRow(label, null, () -> {}, color);
            row.sectionHeader = true;
            return row;
        }

        ActionRow reorderable(int index) {
            this.reorderIndex = index;
            return this;
        }
    }

    static class Btn {
        final int x, y, w, h; final String label; final Component tooltip; final Runnable action;
        float hoverAnim;
        Btn(int x, int y, int w, int h, String label, Component tooltip, Runnable action) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.label = label; this.tooltip = tooltip; this.action = action;
        }
        boolean isHover(int mx, int my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
        void render(GuiGraphics g, net.minecraft.client.gui.Font f, int mx, int my) {
            boolean hv = isHover(mx, my);
            float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.16f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1.0f;
            hoverAnim = UiTheme.approach(hoverAnim, hv ? 1.0f : 0.0f, speed);
            g.fill(x, y, x + w, this.y + this.h, UiTheme.mix(BTN_BG, BTN_HOVER, hoverAnim));
            g.drawString(f, label, x + (w - f.width(label)) / 2, y + (this.h - 8) / 2, UiTheme.textMain(), false);
            if (hv && tooltip != null) VersionCompat.get().renderTooltip(g, f, tooltip, mx, my);
        }
    }

    interface SubEditor {
        void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h);
        boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h);
        boolean keyPressed(int key, int scan, int mod);
        boolean charTyped(char c, int mod);
        default boolean charTyped(int codePoint, int mod) {
            if (!Character.isValidCodePoint(codePoint) || Character.isISOControl(codePoint)) return false;
            boolean handled = false;
            for (char unit : Character.toChars(codePoint)) handled |= charTyped(unit, mod);
            return handled;
        }
        default void onClosed() {}
        default boolean mouseScrolled(double sx, double sy) { return false; }
        default boolean mouseDragged(double mx, double my, int button, double dragX, double dragY, int x, int y, int w, int h) { return false; }
        default boolean mouseReleased(double mx, double my, int button, int x, int y, int w, int h) { return false; }
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
            g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, dx, dy, dw, dh, accentColor());

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.inventory_pick"), dx + 10, dy + 8, UiTheme.textMain(), false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, UiTheme.themedBorder(1f, 1f));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.inventory_hint"), dx + 10, dy + 30, UiTheme.textDim(), false);

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
                    int edge = active ? accentColor() : UiTheme.themedBorder(1f, 1f);
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
            drawBorder(g, bx, by, bw, bh, UiTheme.themedBorder(1f, 1f));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), bx + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 5, UiTheme.textMain(), false);
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
        final TextEditBuffer input;
        String error = null;
        final boolean isLore;
        boolean draggingText;

        InlineFieldEditor(String field, String currentValue, boolean isLore) {
            this.field = field; this.isLore = isLore;
            this.input = new TextEditBuffer(currentValue);
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            boolean colorEditable = isLore || field.equals("rename");
            int dw = Math.min(w - 20, 360), dh = colorEditable ? 148 : 104;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, dx, dy, dw, dh, accentColor());

            String title = getFieldLabel(field);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, dx + 10, dy + 8, UiTheme.textMain(), false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, UiTheme.themedBorder(1f, 1f));

            // Input box
            int ix = dx + 10, iy = dy + 30, iw = dw - 20, ih = 22;
            g.fill(ix, iy, ix + iw, iy + ih, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
            drawBorder(g, ix, iy, iw, ih, accentColor());
            int maxTextW = iw - 10;
            int viewStart = textViewStart(font, input.value(), input.cursor(), maxTextW);
            String disp = visibleText(font, input.value(), viewStart, maxTextW);
            if (input.hasSelection()) {
                int selStart = Math.max(input.selectionStart(), viewStart);
                int selEnd = Math.min(input.selectionEnd(), viewStart + disp.length());
                if (selStart < selEnd) {
                    int sx = ix + 4 + font.width(input.value().substring(viewStart, selStart));
                    int ex = ix + 4 + font.width(input.value().substring(viewStart, selEnd));
                    g.fill(sx, iy + 4, ex, iy + ih - 4, 0x663B82F6);
                }
            }
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, disp, ix + 4, iy + 7, UiTheme.textMain(), false);
            if (!input.hasSelection() && System.currentTimeMillis() % 1000 < 500) {
                int cursorX = ix + 4 + font.width(input.value().substring(viewStart, Math.max(viewStart, Math.min(input.cursor(), input.value().length()))));
                g.fill(cursorX, iy + 4, cursorX + 1, iy + ih - 4, UiTheme.textMain());
            }

            // Preview for name/lore color codes
            if (colorEditable && !input.value().isEmpty()) {
                Component preview = colorCodedToComponent(input.value());
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.preview") + ": ", ix, iy + ih + 4, UiTheme.textDim(), false);
                int previewX = ix + font.width(tr("ankinbt.simple.preview") + ": ");
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, preview, previewX, iy + ih + 4, UiTheme.textMain(), false);
            }

            if (error != null) com.ankinbt.compat.VersionCompat.get().drawString(g, font, error, ix, iy + ih + (colorEditable ? 16 : 4), ERROR_C, false);

            // Color palette button for text fields
            if (colorEditable) {
                int palX = dx + dw - 80, palY = dy + 6;
                boolean palHover = mx >= palX && mx < palX + 70 && my >= palY && my < palY + 16;
                g.fill(palX, palY, palX + 70, palY + 16, palHover ? BTN_HOVER : BTN_BG);
                String palLabel = tr("ankinbt.simple.color_palette");
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, palLabel, palX + (70 - font.width(palLabel)) / 2, palY + 4, UiTheme.textDim(), false);
            }

            // Buttons
            int by = dy + dh - 28, bw = 70, bh = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
            g.fill(cancelX, by, cancelX + bw, by + bh, ch ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, UiTheme.textDim(), false);

            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh;
            g.fill(okX, by, okX + bw, by + bh, oh ? accentColor() : 0xFF4F46E5);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.apply"), okX + (bw - font.width(tr("ankinbt.edit.apply"))) / 2, by + 6, UiTheme.textMain(), false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            draggingText = false;
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
            if (mx >= ix && mx < ix + iw && my >= iy && my < iy + ih) {
                int maxTextW = iw - 10;
                int viewStart = textViewStart(font, input.value(), input.cursor(), maxTextW);
                input.moveTo(plainCursorFromMouse(font, input.value(), viewStart, (int) mx - ix - 4, maxTextW), false);
                draggingText = btn == 0;
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
        public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY,
                                    int x, int y, int w, int h) {
            if (!draggingText || button != 0) return false;
            boolean colorEditable = isLore || field.equals("rename");
            int dw = Math.min(w - 20, 360), dh = colorEditable ? 148 : 104;
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            int ix = dx + 10, iy = dy + 30, iw = dw - 20, ih = 22;
            if (my < iy - 8 || my > iy + ih + 8) return true;
            int maxTextW = iw - 10;
            int viewStart = textViewStart(SimpleEditorScreen.this.font,
                    input.value(), input.cursor(), maxTextW);
            int next = plainCursorFromMouse(SimpleEditorScreen.this.font, input.value(), viewStart,
                    (int) Math.round(mx) - ix - 4, maxTextW);
            input.moveTo(next, true);
            return true;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button, int x, int y, int w, int h) {
            boolean handled = draggingText;
            draggingText = false;
            return handled;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { apply(); return true; }
            String before = input.value();
            if (input.keyPressed(key, mod)) {
                if (!before.equals(input.value())) error = null;
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (input.charTyped(c)) { error = null; return true; }
            return true;
        }

        void insertAtCursor(String text) {
            boolean wrapsSelection = input.hasSelection() && (text == null || !text.endsWith("r"));
            input.wrapSelectionOrInsert(text, wrapsSelection ? "&r" : "");
        }

        private void apply() {
            if (input.value().isEmpty() && !field.equals("rename") && !field.equals("lore_add") && !field.startsWith("lore:")) {
                error = tr("ankinbt.simple.invalid_number"); return;
            }
            applyInlineEdit(field, input.value(), isLore);
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
        private final MultiLineTextEditBuffer buffer;
        private int scrollOff = 0;
        private boolean showRawCodes = false;
        private boolean draggingText = false;
        private boolean ensureCursorVisible = true;

        LoreTextEditorSubEditor(boolean appendLine) {
            ArrayList<String> initial = new ArrayList<String>();
            List<Component> lore = SimpleEditorScreen.this.getLore();
            if (lore.isEmpty()) {
                initial.add("");
            } else {
                for (int i = 0; i < lore.size(); ++i) {
                    initial.add(SimpleEditorScreen.this.getLoreRawText(i));
                }
                if (appendLine) initial.add("");
            }
            this.buffer = new MultiLineTextEditBuffer(initial);
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
            if (this.ensureCursorVisible) {
                this.scrollToCursor(maxVisLines);
                this.ensureCursorVisible = false;
            }
            g.fill(textX - 2, textY - 2, textX + textW + 2, textY + textH + 2, -15592930);
            SimpleEditorScreen.this.drawBorder(g, textX - 2, textY - 2, textW + 4, textH + 4, -14540234);
            g.enableScissor(textX, textY, textX + textW, textY + textH);
            int end = Math.min(this.scrollOff + maxVisLines, this.buffer.lines().size());
            for (int i = this.scrollOff; i < end; ++i) {
                int ly = textY + (i - this.scrollOff) * lineH;
                String line = this.buffer.lines().get(i);
                g.drawString(font, String.valueOf(i + 1), textX, ly + 2, -10193781, false);
                if (i == this.buffer.cursorLine()) {
                    g.fill(contentX - 2, ly, textX + textW, ly + lineH, 0x18FFFFFF);
                }
                if (this.buffer.lineHasSelection(i)) {
                    int sx = contentX + this.textWidthForColumn(font, line, this.buffer.selectionStartCol(i));
                    int ex = contentX + this.textWidthForColumn(font, line, this.buffer.selectionEndCol(i));
                    g.fill(sx, ly + 1, Math.max(sx + 1, ex), ly + lineH - 1, 1715176182);
                }
                if (this.showRawCodes) {
                    g.drawString(font, line, contentX, ly + 2, -1906448, false);
                } else {
                    g.drawString(font, SimpleEditorScreen.colorCodedToComponent(line), contentX, ly + 2, -1906448, false);
                }
                if (!this.buffer.hasSelection() && i == this.buffer.cursorLine() && System.currentTimeMillis() % 1000L < 500L) {
                    int cx = contentX + this.textWidthForColumn(font, line, this.buffer.cursorCol());
                    g.fill(cx, ly + 1, cx + 1, ly + lineH - 1, -1906448);
                }
            }
            g.disableScissor();
            if (this.buffer.lines().size() > maxVisLines) {
                int sbx = textX + textW - 4;
                g.fill(sbx, textY, sbx + 4, textY + textH, 0x30FFFFFF);
                float ratio = (float)maxVisLines / (float)this.buffer.lines().size();
                int thumbH = Math.max(8, (int)((float)textH * ratio));
                float sr = (float)this.scrollOff / (float)Math.max(1, this.buffer.lines().size() - maxVisLines);
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
            String state = this.buffer.cursorLine() + 1 + ":" + this.buffer.cursorCol() + " | " + this.buffer.lines().size() + SimpleEditorScreen.tr("ankinbt.simple.lore_lines_suffix");
            g.drawString(font, state, dx + dw - font.width(state) - 10, by + 6, -10193781, false);
        }

        private void scrollToCursor(int maxVisLines) {
            if (this.buffer.cursorLine() < this.scrollOff) {
                this.scrollOff = this.buffer.cursorLine();
            }
            if (this.buffer.cursorLine() >= this.scrollOff + maxVisLines) {
                this.scrollOff = this.buffer.cursorLine() - maxVisLines + 1;
            }
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.buffer.lines().size() - maxVisLines)));
        }

        private int textWidthForColumn(Font font, String line, int col) {
            int safeCol = Math.max(0, Math.min(col, line.length()));
            if (this.showRawCodes) {
                return font.width(line.substring(0, safeCol));
            }
            return font.width(this.stripColorCodes(line.substring(0, safeCol)));
        }

        private int columnFromMouse(Font font, String line, double mx, int contentX) {
            int local = Math.max(0, (int)mx - contentX);
            int best = 0;
            int bestDist = Integer.MAX_VALUE;
            for (int i = 0; i <= line.length(); ++i) {
                int x = this.textWidthForColumn(font, line, i);
                int dist = Math.abs(x - local);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = i;
                }
            }
            return best;
        }

        private String stripColorCodes(String s) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); ++i) {
                if (s.charAt(i) == '&' && i + 1 < s.length()) {
                    char next = s.charAt(i + 1);
                    if (next == '#' && i + 7 < s.length()) {
                        i += 7;
                        continue;
                    }
                    if ("0123456789abcdefklmnorABCDEFKLMNOR".indexOf(next) >= 0) {
                        ++i;
                        continue;
                    }
                }
                sb.append(s.charAt(i));
            }
            return sb.toString();
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            this.draggingText = false;
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
                InlineFieldEditor tempEditor = new InlineFieldEditor("lore_text_temp", this.buffer.lines().get(this.buffer.cursorLine()), true);
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
                this.moveCursorFromMouse(mx, my, textX, textY, textW, textH, false);
                this.draggingText = btn == 0;
                return true;
            }
            this.draggingText = false;
            return true;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY, int x, int y, int w, int h) {
            if (!this.draggingText || button != 0) {
                return false;
            }
            int dw = Math.min(w - 10, Math.max(320, Math.min(500, w - 24)));
            int dh = Math.min(h - 10, Math.max(220, Math.min(340, h - 16)));
            int dx = x + (w - dw) / 2;
            int dy = y + (h - dh) / 2;
            this.moveCursorFromMouse(mx, my, dx + 10, dy + 30, dw - 20, dh - 74, true);
            return true;
        }

        private void moveCursorFromMouse(double mx, double my, int textX, int textY, int textW, int textH, boolean selecting) {
            int lineH = 14;
            int maxVisLines = Math.max(1, textH / lineH);
            int clickedLine = (int)((my - (double)textY) / (double)lineH) + this.scrollOff;
            clickedLine = Math.max(0, Math.min(clickedLine, this.buffer.lines().size() - 1));
            int contentX = textX + 24;
            int col = this.columnFromMouse(SimpleEditorScreen.this.font, this.buffer.lines().get(clickedLine), mx, contentX);
            this.buffer.moveTo(clickedLine, col, selecting);
            this.scrollToCursor(maxVisLines);
            this.ensureCursorVisible = false;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) {
                this.buffer.insertNewLine();
                this.ensureCursorVisible = true;
                return true;
            }
            if (this.buffer.keyPressed(key, mod)) {
                this.ensureCursorVisible = true;
                return true;
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            boolean handled = this.buffer.charTyped(c);
            if (handled) this.ensureCursorVisible = true;
            return handled;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            this.scrollOff -= (int)sy * 3;
            this.scrollOff = Math.max(0, Math.min(this.scrollOff, Math.max(0, this.buffer.lines().size() - 5)));
            return true;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button, int x, int y, int w, int h) {
            boolean handled = this.draggingText;
            this.draggingText = false;
            return handled;
        }

        void insertAtCursor(String text) {
            String suffix = text != null && text.length() == 2 && text.charAt(0) == '&' && "0123456789abcdefABCDEF".indexOf(text.charAt(1)) >= 0 ? "&r" : "";
            this.buffer.wrapSelectionOrInsert(text, suffix);
        }

        private void applyAll() {
            List<String> lines = this.buffer.lines();
            while (lines.size() > 1 && lines.get(lines.size() - 1).isEmpty()) {
                lines.remove(lines.size() - 1);
            }
            ArrayList<Component> loreComponents = new ArrayList<Component>();
            for (String line : lines) {
                if (!line.isEmpty() || lines.size() == 1) {
                    loreComponents.add(SimpleEditorScreen.colorCodedToComponent(line));
                    continue;
                }
                loreComponents.add((Component)Component.empty());
            }
            if (loreComponents.size() == 1 && lines.get(0).isEmpty()) {
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
            g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, dx, dy, dw, dh, accentColor());

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.color_palette"), dx + 10, dy + 8, UiTheme.textMain(), false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, UiTheme.themedBorder(1f, 1f));

            // Color grid - 8x2 layout with larger cells
            int gridX = dx + 12, gridY = dy + 28;
            int cellW = (dw - 24) / 8, cellH = 28;
            hoveredColor = -1;

            // Row labels
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.palette_bright"), dx + 10, gridY - 1, UiTheme.textDim(), false);
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
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, tip, mx + 12, my - 12, UiTheme.textMain(), false);
                }
            }

            // Dark colors
            int darkY = gridY + cellH + 14;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.palette_dark"), dx + 10, darkY - 1, UiTheme.textDim(), false);
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
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, tip, mx + 12, my - 12, UiTheme.textMain(), false);
                }
            }

            // Format codes - pill-style buttons
            int fmtY = darkY + cellH + 20;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.format_codes"), dx + 10, fmtY, UiTheme.textDim(), false);
            fmtY += 14;
            int fmtX = gridX;
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; i++) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.width(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) { fmtX = gridX; fmtY += 22; }
                boolean hover = mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18;
                g.fill(fmtX, fmtY, fmtX + pillW, fmtY + 18, hover ? accentColor() : 0x30FFFFFF);
                // Rounded feel with border
                drawBorder(g, fmtX, fmtY, pillW, 18, hover ? accentColor() : 0x20FFFFFF);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, fLabel, fmtX + 7, fmtY + 5, hover ? UiTheme.textMain() : UiTheme.textDim(), false);
                fmtX += pillW + 6;
            }

            // Back button
            int backY = dy + dh - 26, backW = 70, backX = dx + (dw - backW) / 2;
            boolean bh2 = mx >= backX && mx < backX + backW && my >= backY && my < backY + 20;
            g.fill(backX, backY, backX + backW, backY + 20, bh2 ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.back"), backX + (backW - font.width(tr("ankinbt.simple.back"))) / 2, backY + 6, UiTheme.textDim(), false);
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
            g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, dx, dy, dw, dh, accentColor());

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.color_palette"), dx + 10, dy + 8, UiTheme.textMain(), false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, UiTheme.themedBorder(1f, 1f));

            int gridX = dx + 12, gridY = dy + 28;
            int cellW = (dw - 24) / 8, cellH = 28;

            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.palette_bright"), dx + 10, gridY - 1, UiTheme.textDim(), false);
            int[] brightOrder = {6, 14, 10, 11, 9, 13, 12, 15};
            for (int i = 0; i < 8; i++) {
                int ci = brightOrder[i];
                int cx = gridX + i * cellW, cy = gridY + 8;
                boolean hover = mx >= cx && mx < cx + cellW - 2 && my >= cy && my < cy + cellH;
                g.fill(cx, cy, cx + cellW - 2, cy + cellH, MC_COLORS[ci] | 0xFF000000);
                if (hover) drawBorder(g, cx, cy, cellW - 2, cellH, 0xFFFFFFFF);
            }

            int darkY = gridY + cellH + 14;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.palette_dark"), dx + 10, darkY - 1, UiTheme.textDim(), false);
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
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.format_codes"), dx + 10, fmtY, UiTheme.textDim(), false);
            fmtY += 14;
            int fmtX = gridX;
            String lang = Minecraft.getInstance().options.languageCode;
            boolean isZh = lang != null && lang.startsWith("zh");
            for (int i = 0; i < MC_FORMAT_CODES.length; i++) {
                String fLabel = isZh ? MC_FORMAT_NAMES_ZH[i] : MC_FORMAT_NAMES_EN[i];
                int pillW = font.width(fLabel) + 14;
                if (fmtX + pillW > dx + dw - 12) { fmtX = gridX; fmtY += 22; }
                boolean hover = mx >= fmtX && mx < fmtX + pillW && my >= fmtY && my < fmtY + 18;
                g.fill(fmtX, fmtY, fmtX + pillW, fmtY + 18, hover ? accentColor() : 0x30FFFFFF);
                drawBorder(g, fmtX, fmtY, pillW, 18, hover ? accentColor() : 0x20FFFFFF);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, fLabel, fmtX + 7, fmtY + 5, hover ? UiTheme.textMain() : UiTheme.textDim(), false);
                fmtX += pillW + 6;
            }

            int backY = dy + dh - 26, backW = 70, backX = dx + (dw - backW) / 2;
            boolean bh2 = mx >= backX && mx < backX + backW && my >= backY && my < backY + 20;
            g.fill(backX, backY, backX + backW, backY + 20, bh2 ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.back"), backX + (backW - font.width(tr("ankinbt.simple.back"))) / 2, backY + 6, UiTheme.textDim(), false);
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
        private final TextEditBuffer search = new TextEditBuffer("");
        private final List<String> filtered = new ArrayList<>();
        private int hoverIdx = -1;
        private int selectedIdx = 0;
        private int scrollOff = 0;
        private boolean draggingText = false;

        PotionPickerSubEditor() {
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
            String q = search.value().toLowerCase(Locale.ROOT);
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
            g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, dx, dy, dw, dh, accentColor());
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.pick_potion"), dx + 10, dy + 8, UiTheme.textMain(), false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, UiTheme.themedBorder(1f, 1f));

            int sx = dx + 10, sy = dy + 30, sw = dw - 20, sh = 18;
            g.fill(sx, sy, sx + sw, sy + sh, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
            drawBorder(g, sx, sy, sw, sh, accentColor());
            renderTextBuffer(g, font, search, sx, sy, sw, sh, true, tr("ankinbt.search.hint"));

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
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, dx + 36, ry + 5, selected ? UiTheme.textMain() : UiTheme.textDim(), false);
            }

            int by = dy + dh - 28, bw = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw, by + bh2, ch ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, UiTheme.textDim(), false);
            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw, by + bh2, oh ? accentColor() : accentColor());
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.apply"), okX + (bw - font.width(tr("ankinbt.edit.apply"))) / 2, by + 6, UiTheme.textMain(), false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            draggingText = false;
            int dw = Math.min(w - 20, 380), dh = Math.min(h - 20, 250);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            int sx = dx + 10, sy = dy + 30, sw = dw - 20, sh = 18;
            if (mx >= sx && mx < sx + sw && my >= sy && my < sy + sh) {
                search.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, search,
                        (int) Math.round(mx) - sx - 4, sw - 8), false);
                draggingText = btn == 0;
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
        public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY,
                                    int x, int y, int w, int h) {
            if (!draggingText || button != 0) return false;
            int dw = Math.min(w - 20, 380), dh = Math.min(h - 20, 250);
            int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
            int sx = dx + 10, sy = dy + 30, sw = dw - 20, sh = 18;
            if (my >= sy - 8 && my <= sy + sh + 8) {
                search.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, search,
                        (int) Math.round(mx) - sx - 4, sw - 8), true);
                filter();
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button, int x, int y, int w, int h) {
            boolean handled = draggingText;
            draggingText = false;
            return handled;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { confirm(); return true; }
            if (key == 264 && selectedIdx < filtered.size() - 1) { selectedIdx++; if (selectedIdx >= scrollOff + 8) scrollOff++; return true; }
            if (key == 265 && selectedIdx > 0) { selectedIdx--; if (selectedIdx < scrollOff) scrollOff = selectedIdx; return true; }
            String before = search.value();
            if (search.keyPressed(key, mod)) { if (!before.equals(search.value())) filter(); return true; }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (search.charTyped(c)) { filter(); return true; }
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
        private final TextEditBuffer search = new TextEditBuffer("");
        private final TextEditBuffer duration = new TextEditBuffer("600");
        private final TextEditBuffer amplifier = new TextEditBuffer("0");
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
        private boolean draggingText = false;

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

        /**
         * Shared geometry for the effect picker. Keeping one calculation for
         * painting and input prevents the compact layout from drifting or
         * allowing rows to escape below the dialog footer.
         */
        private record EffectLayout(
                int dx, int dy, int dw, int dh,
                int leftX, int leftW, int rightX, int rightW,
                int searchY, int searchH, int listY, int listBottom, int maxItems,
                int selectedHeaderY, int selectedY, int selectedListH, int selectedRows,
                int formY, int durationLabelX, int durationX, int durationY,
                int amplifierLabelX, int amplifierX, int amplifierY,
                int fieldW, int toggleY, int toggleGap, int toggleW,
                int buttonY, int buttonW, int buttonH, int cancelX, int confirmX,
                boolean stackedFields) {
        }

        private EffectLayout layout(int x, int y, int w, int h) {
            int dw = Math.max(1, Math.min(760, Math.max(320, w - 12)));
            dw = Math.min(dw, Math.max(1, w - 4));
            int dh = Math.max(1, Math.min(420, Math.max(190, h - 8)));
            dh = Math.min(dh, Math.max(1, h - 4));
            int dx = x + Math.max(0, (w - dw) / 2);
            int dy = y + Math.max(0, (h - dh) / 2);

            int inner = 10;
            int gap = 10;
            int available = Math.max(2, dw - inner * 2 - gap);
            int leftW = available < 330
                    ? Math.max(120, available * 43 / 100)
                    : Math.max(190, Math.min(340, available * 45 / 100));
            leftW = Math.min(leftW, Math.max(1, available - 120));
            int rightW = Math.max(1, available - leftW);
            int leftX = dx + inner;
            int rightX = leftX + leftW + gap;

            int bodyTop = dy + 30;
            int bodyBottom = Math.max(bodyTop + 1, dy + dh - 32);
            int searchY = bodyTop;
            int searchH = 22;
            int listY = searchY + searchH + 7;
            int listBottom = Math.max(listY + 1, bodyBottom);
            int maxItems = Math.max(1, (listBottom - listY) / 22);

            int selectedHeaderY = bodyTop;
            int selectedY = selectedHeaderY + 29;
            int availableRight = Math.max(36, bodyBottom - selectedY);
            int selectedListH = Math.max(36, Math.min(126, availableRight - 78));
            if (selectedY + selectedListH > bodyBottom - 38) {
                selectedListH = Math.max(24, bodyBottom - selectedY - 38);
            }
            int selectedRows = Math.max(1, selectedListH / 22);
            int formY = selectedY + selectedListH + 8;

            int durationLabelW = Math.min(52, Math.max(28,
                    SimpleEditorScreen.this.font.width(tr("ankinbt.simple.effect_duration"))));
            int amplifierLabelW = Math.min(52, Math.max(28,
                    SimpleEditorScreen.this.font.width(tr("ankinbt.simple.effect_amplifier"))));
            boolean stacked = rightW < 245;
            int fieldW;
            int durationLabelX = rightX + 6;
            int durationX;
            int durationY = formY + 18;
            int amplifierLabelX;
            int amplifierX;
            int amplifierY;
            if (stacked) {
                fieldW = Math.max(40, rightW - 12 - durationLabelW - 4);
                durationX = durationLabelX + durationLabelW + 4;
                amplifierLabelX = rightX + 6;
                amplifierX = amplifierLabelX + amplifierLabelW + 4;
                amplifierY = durationY + 23;
            } else {
                int fieldsGap = 9;
                int availableFields = Math.max(72,
                        rightW - 12 - durationLabelW - amplifierLabelW - fieldsGap - 8);
                fieldW = Math.max(40, Math.min(86, availableFields / 2));
                durationX = durationLabelX + durationLabelW + 4;
                amplifierLabelX = durationX + fieldW + fieldsGap;
                amplifierX = amplifierLabelX + amplifierLabelW + 4;
                amplifierY = durationY;
            }
            int toggleY = stacked ? amplifierY + 23 : durationY + 23;
            int toggleGap = 4;
            int toggleW = Math.max(38, (rightW - 12 - toggleGap * 2) / 3);

            int buttonH = 20;
            int buttonY = dy + dh - buttonH - 7;
            int buttonW = Math.max(62, Math.min(88, (dw - 34) / 2));
            int cancelX = dx + dw / 2 - buttonW - 5;
            int confirmX = dx + dw / 2 + 5;
            return new EffectLayout(dx, dy, dw, dh, leftX, leftW, rightX, rightW,
                    searchY, searchH, listY, listBottom, maxItems,
                    selectedHeaderY, selectedY, selectedListH, selectedRows,
                    formY, durationLabelX, durationX, durationY,
                    amplifierLabelX, amplifierX, amplifierY, fieldW,
                    toggleY, toggleGap, toggleW, buttonY, buttonW, buttonH,
                    cancelX, confirmX, stacked);
        }

        private int visibleEffectRows() {
            // During construction the screen geometry is not initialized yet.
            // Keep the previous keyboard/filter fallback until the first frame.
            if (contentW <= 0 || contentH <= 0) return 7;
            return Math.max(1, layout(contentX, contentY, contentW, contentH).maxItems());
        }

        PotionEffectSubEditor() {
            loadExistingEffects();
            filter();
            if (activeEffectId == null && !selectedEffects.isEmpty()) {
                setActiveEffect(selectedEffects.keySet().iterator().next());
            }
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
            String q = search.value().toLowerCase(Locale.ROOT);
            filtered.clear();
            for (String id : EFFECT_IDS) {
                if (q.isEmpty()
                        || id.toLowerCase(Locale.ROOT).contains(q)
                        || effectDisplayName(id).toLowerCase(Locale.ROOT).contains(q)) {
                    filtered.add(id);
                }
            }
            selectedIdx = Math.min(selectedIdx, Math.max(0, filtered.size() - 1));
            scrollOff = Math.max(0, Math.min(scrollOff,
                    Math.max(0, filtered.size() - visibleEffectRows())));
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            lastMouseX = mx;
            lastMouseY = my;
            EffectLayout l = layout(x, y, w, h);
            g.fill(l.dx(), l.dy(), l.dx() + l.dw(), l.dy() + l.dh(), UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, l.dx(), l.dy(), l.dw(), l.dh(), accentColor());
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.pick_effect"), l.dx() + 10, l.dy() + 8, UiTheme.textMain(), false);
            String count = String.format(tr("ankinbt.simple.potion_selected_count"), selectedEffects.size());
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, count,
                    l.dx() + l.dw() - 10 - font.width(count), l.dy() + 8, UiTheme.textDim(), false);
            g.fill(l.dx() + 1, l.dy() + 25, l.dx() + l.dw() - 1, l.dy() + 26, UiTheme.themedBorder(1f, 1f));

            g.fill(l.leftX(), l.searchY(), l.leftX() + l.leftW(), l.listBottom(), UiTheme.card(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, l.leftX(), l.searchY(), l.leftW(), l.listBottom() - l.searchY(), UiTheme.themedBorder(0.9f, 1f));
            renderSmallTextBox(g, font, l.leftX() + 6, l.searchY() + 5, l.leftW() - 12, l.searchH(),
                    search, focusField == 0, tr("ankinbt.search.hint"));

            int listY = l.listY();
            int maxItems = l.maxItems();
            hoverIdx = -1;
            int end = Math.min(filtered.size(), scrollOff + maxItems);
            g.enableScissor(l.leftX() + 1, listY, l.leftX() + l.leftW() - 1, l.listBottom());
            for (int i = scrollOff; i < end; i++) {
                String id = filtered.get(i);
                int ry = listY + (i - scrollOff) * 22;
                boolean hover = mx >= l.leftX() && mx < l.leftX() + l.leftW() && my >= ry && my < ry + 22;
                if (hover) hoverIdx = i;
                boolean focused = i == selectedIdx;
                boolean checked = selectedEffects.containsKey(id);
                boolean active = id.equals(activeEffectId);
                int rowFill = active ? SELECT_BG : (checked ? 0x382A2A42 : (hover ? HOVER : 0x18000000));
                g.fill(l.leftX() + 5, ry, l.leftX() + l.leftW() - 5, ry + 21, rowFill);
                if (focused && !active) drawBorder(g, l.leftX() + 5, ry, l.leftW() - 10, 21, 0x668EA0FF);
                g.renderItem(ItemEditorVisuals.effectIconStack(id), l.leftX() + 9, ry + 2);
                String label = effectDisplayName(id);
                if (font.width(label) > l.leftW() - 58) label = font.plainSubstrByWidth(label, Math.max(20, l.leftW() - 62)) + "..";
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, l.leftX() + 32, ry + 6,
                        checked ? UiTheme.textMain() : UiTheme.textDim(), false);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, checked ? "-" : "+",
                        l.leftX() + l.leftW() - 18, ry + 6, checked ? UiTheme.textMain() : UiTheme.textDim(), false);
            }
            g.disableScissor();
            if (filtered.size() > maxItems) {
                int trackX = l.leftX() + l.leftW() - 5;
                int trackY = listY;
                int trackH = Math.max(1, l.listBottom() - listY);
                int thumbH = Math.max(14, trackH * maxItems / Math.max(maxItems, filtered.size()));
                int maxScroll = Math.max(1, filtered.size() - maxItems);
                int thumbY = trackY + (trackH - thumbH) * scrollOff / maxScroll;
                g.fill(trackX, trackY, trackX + 2, trackY + trackH, SB_TRACK);
                g.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, accentColor());
            }

            g.fill(l.rightX(), l.selectedHeaderY(), l.rightX() + l.rightW(), l.selectedY() - 6,
                    UiTheme.card(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, l.rightX(), l.selectedHeaderY(), l.rightW(), l.selectedY() - l.selectedHeaderY() - 6,
                    UiTheme.themedBorder(0.9f, 1f));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.potion_selected_list"),
                    l.rightX() + 6, l.selectedHeaderY() + 7, UiTheme.textMain(), false);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, count,
                    l.rightX() + l.rightW() - 6 - font.width(count), l.selectedHeaderY() + 7, UiTheme.textDim(), false);
            selectedScrollOff = Math.max(0, Math.min(selectedScrollOff,
                    Math.max(0, selectedEffects.size() - l.selectedRows())));
            int idx = 0;
            int drawn = 0;
            g.enableScissor(l.rightX() + 1, l.selectedY(), l.rightX() + l.rightW() - 1,
                    l.selectedY() + l.selectedListH());
            for (Map.Entry<String, EffectDraft> entry : selectedEffects.entrySet()) {
                if (idx++ < selectedScrollOff) continue;
                if (drawn >= l.selectedRows()) break;
                String id = entry.getKey();
                EffectDraft draft = entry.getValue();
                int ry = l.selectedY() + drawn * 22;
                boolean active = id.equals(activeEffectId);
                g.fill(l.rightX() + 5, ry, l.rightX() + l.rightW() - 5, ry + 21,
                        active ? SELECT_BG : 0x2412121E);
                g.renderItem(ItemEditorVisuals.effectIconStack(id), l.rightX() + 9, ry + 2);
                String label = effectDisplayName(id);
                int metaW = font.width(draft.duration + " / " + draft.amplifier);
                if (font.width(label) > l.rightW() - 62 - metaW) {
                    label = font.plainSubstrByWidth(label, Math.max(20, l.rightW() - 66 - metaW)) + "..";
                }
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, l.rightX() + 32, ry + 6,
                        active ? UiTheme.textMain() : UiTheme.textDim(), false);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, draft.duration + " / " + draft.amplifier,
                        l.rightX() + l.rightW() - 28 - metaW, ry + 6, UiTheme.textDim(), false);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, "×", l.rightX() + l.rightW() - 17, ry + 6,
                        UiTheme.textDim(), false);
                drawn++;
            }
            g.disableScissor();
            if (selectedEffects.size() > l.selectedRows()) {
                int trackX = l.rightX() + l.rightW() - 4;
                int trackY = l.selectedY();
                int trackH = Math.max(1, l.selectedListH());
                g.fill(trackX, trackY, trackX + 3, trackY + trackH, SB_TRACK);
                int thumbH = Math.max(12, trackH * l.selectedRows() / Math.max(l.selectedRows(), selectedEffects.size()));
                int maxScroll = Math.max(1, selectedEffects.size() - l.selectedRows());
                int thumbY = trackY + (trackH - thumbH) * selectedScrollOff / maxScroll;
                g.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, accentColor());
            }

            int formY = l.formY();
            g.enableScissor(l.rightX(), formY, l.rightX() + l.rightW(), l.buttonY() - 3);
            if (activeEffectId == null || !selectedEffects.containsKey(activeEffectId)) {
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.select_effect_first"),
                        l.rightX() + 6, formY + 8, UiTheme.textDim(), false);
            } else {
                String activeLabel = effectDisplayName(activeEffectId);
                if (font.width(activeLabel) > l.rightW() - 12) activeLabel = font.plainSubstrByWidth(activeLabel, Math.max(20, l.rightW() - 16)) + "..";
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, activeLabel,
                        l.rightX() + 6, formY, UiTheme.textMain(), false);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.effect_duration"),
                        l.durationLabelX(), l.durationY() + 5, UiTheme.textDim(), false);
                renderSmallTextBox(g, font, l.durationX(), l.durationY(), l.fieldW(), 18,
                        duration, focusField == 1, "");
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.effect_amplifier"),
                        l.amplifierLabelX(), l.amplifierY() + 5, UiTheme.textDim(), false);
                renderSmallTextBox(g, font, l.amplifierX(), l.amplifierY(), l.fieldW(), 18,
                        amplifier, focusField == 2, "");
                renderToggle(g, font, mx, my, l.rightX() + 6, l.toggleY(), l.toggleW(),
                        tr("ankinbt.simple.effect_ambient"), ambient);
                renderToggle(g, font, mx, my, l.rightX() + 6 + l.toggleW() + l.toggleGap(), l.toggleY(), l.toggleW(),
                        tr("ankinbt.simple.effect_particles"), particles);
                renderToggle(g, font, mx, my, l.rightX() + 6 + (l.toggleW() + l.toggleGap()) * 2, l.toggleY(), l.toggleW(),
                        tr("ankinbt.simple.effect_icon"), icon);
            }
            g.disableScissor();

            boolean ch = mx >= l.cancelX() && mx < l.cancelX() + l.buttonW() && my >= l.buttonY() && my < l.buttonY() + l.buttonH();
            g.fill(l.cancelX(), l.buttonY(), l.cancelX() + l.buttonW(), l.buttonY() + l.buttonH(), ch ? BTN_HOVER : BTN_BG);
            drawBorder(g, l.cancelX(), l.buttonY(), l.buttonW(), l.buttonH(), UiTheme.themedBorder(0.9f, 1f));
            String cancel = tr("ankinbt.edit.cancel");
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, cancel,
                    l.cancelX() + (l.buttonW() - font.width(cancel)) / 2, l.buttonY() + 6,
                    UiTheme.textDim(), false);
            boolean oh = mx >= l.confirmX() && mx < l.confirmX() + l.buttonW() && my >= l.buttonY() && my < l.buttonY() + l.buttonH();
            g.fill(l.confirmX(), l.buttonY(), l.confirmX() + l.buttonW(), l.buttonY() + l.buttonH(), oh ? accentColor() : UiTheme.withAlpha(accentColor() & 0x00FFFFFF, 210));
            drawBorder(g, l.confirmX(), l.buttonY(), l.buttonW(), l.buttonH(), accentColor());
            String confirm = tr("ankinbt.add.confirm");
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, confirm,
                    l.confirmX() + (l.buttonW() - font.width(confirm)) / 2, l.buttonY() + 6,
                    UiTheme.textMain(), false);
        }

        private void renderSmallTextBox(GuiGraphics g, net.minecraft.client.gui.Font font, int x, int y, int w, int h,
                                        TextEditBuffer buffer, boolean focused, String hint) {
            g.fill(x, y, x + w, y + h, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
            drawBorder(g, x, y, w, h, focused ? accentColor() : UiTheme.themedBorder(1f, 1f));
            renderTextBuffer(g, font, buffer, x, y, w, h, focused, hint);
        }

        private void renderToggle(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int bw, String label, boolean on) {
            boolean hover = mx >= x && mx < x + bw && my >= y && my < y + 18;
            g.fill(x, y, x + bw, y + 18, on ? accentColor() : (hover ? BTN_HOVER : BTN_BG));
            String text = label;
            if (font.width(text) > bw - 10) text = font.plainSubstrByWidth(text, Math.max(8, bw - 14)) + "..";
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, x + (bw - font.width(text)) / 2, y + 5, on ? UiTheme.textMain() : UiTheme.textDim(), false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            draggingText = false;
            EffectLayout l = layout(x, y, w, h);
            int sx = l.leftX() + 6, sy = l.searchY() + 5, sw = l.leftW() - 12, sh = l.searchH();
            if (mx >= sx && mx < sx + sw && my >= sy && my < sy + sh) {
                focusField = 0;
                search.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, search,
                        (int) Math.round(mx) - sx - 4, sw - 8), false);
                draggingText = btn == 0;
                return true;
            }
            int listY = l.listY();
            int maxItems = l.maxItems();
            if (mx >= l.leftX() && mx < l.leftX() + l.leftW()
                    && my >= listY && my < l.listBottom()) {
                int hitIdx = scrollOff + Math.max(0, (int) ((my - listY) / 22));
                if (hitIdx >= 0 && hitIdx < filtered.size()) {
                    selectedIdx = hitIdx;
                    selectEffect(filtered.get(hitIdx), btn);
                    return true;
                }
            }

            int selectedY = l.selectedY();
            int selectedListH = l.selectedListH();
            int selectedRows = l.selectedRows();
            int selectedIdx = 0;
            for (String id : new ArrayList<>(selectedEffects.keySet())) {
                if (selectedIdx++ < selectedScrollOff) continue;
                int visibleIdx = selectedIdx - selectedScrollOff - 1;
                if (visibleIdx >= selectedRows) break;
                int ry = selectedY + visibleIdx * 22;
                if (mx >= l.rightX() && mx < l.rightX() + l.rightW() && my >= ry && my < ry + 21) {
                    if (mx >= l.rightX() + l.rightW() - 26) removeSelectedEffect(id);
                    else setActiveEffect(id);
                    return true;
                }
            }

            if (mx >= l.durationX() && mx < l.durationX() + l.fieldW()
                    && my >= l.durationY() && my < l.durationY() + 18) {
                focusField = 1;
                duration.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, duration,
                        (int) Math.round(mx) - l.durationX() - 4, l.fieldW() - 8), false);
                draggingText = btn == 0;
                return true;
            }
            if (mx >= l.amplifierX() && mx < l.amplifierX() + l.fieldW()
                    && my >= l.amplifierY() && my < l.amplifierY() + 18) {
                focusField = 2;
                amplifier.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, amplifier,
                        (int) Math.round(mx) - l.amplifierX() - 4, l.fieldW() - 8), false);
                draggingText = btn == 0;
                return true;
            }
            if (clickToggle(mx, my, l.rightX() + 6, l.toggleY(), l.toggleW())) { ambient = !ambient; syncActiveDraft(); return true; }
            if (clickToggle(mx, my, l.rightX() + 6 + l.toggleW() + l.toggleGap(), l.toggleY(), l.toggleW())) { particles = !particles; syncActiveDraft(); return true; }
            if (clickToggle(mx, my, l.rightX() + 6 + (l.toggleW() + l.toggleGap()) * 2, l.toggleY(), l.toggleW())) { icon = !icon; syncActiveDraft(); return true; }

            if (mx >= l.cancelX() && mx < l.cancelX() + l.buttonW()
                    && my >= l.buttonY() && my < l.buttonY() + l.buttonH()) { activeSubEditor = null; return true; }
            if (mx >= l.confirmX() && mx < l.confirmX() + l.buttonW()
                    && my >= l.buttonY() && my < l.buttonY() + l.buttonH()) { confirm(); return true; }
            return true;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY,
                                    int x, int y, int w, int h) {
            if (!draggingText || button != 0) return false;
            EffectLayout l = layout(x, y, w, h);
            int sx = l.leftX() + 6, sy = l.searchY() + 5, sw = l.leftW() - 12, sh = l.searchH();
            if (focusField == 0 && my >= sy - 8 && my <= sy + sh + 8) {
                search.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, search,
                        (int) Math.round(mx) - sx - 4, sw - 8), true);
                filter();
                return true;
            }
            if (focusField == 1 && my >= l.durationY() - 8 && my <= l.durationY() + 26
                    && mx >= l.durationX() - 8 && mx <= l.durationX() + l.fieldW() + 8) {
                duration.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, duration,
                        (int) Math.round(mx) - l.durationX() - 4, l.fieldW() - 8), true);
                syncActiveDraft();
                return true;
            }
            if (focusField == 2 && my >= l.amplifierY() - 8 && my <= l.amplifierY() + 26
                    && mx >= l.amplifierX() - 8 && mx <= l.amplifierX() + l.fieldW() + 8) {
                amplifier.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, amplifier,
                        (int) Math.round(mx) - l.amplifierX() - 4, l.fieldW() - 8), true);
                syncActiveDraft();
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button, int x, int y, int w, int h) {
            boolean handled = draggingText;
            draggingText = false;
            return handled;
        }

        private boolean clickToggle(double mx, double my, int x, int y, int bw) {
            return mx >= x && mx < x + bw && my >= y && my < y + 18;
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
            duration.setValue(draft.duration);
            duration.moveTo(duration.value().length(), false);
            amplifier.setValue(draft.amplifier);
            amplifier.moveTo(amplifier.value().length(), false);
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
            // Keep the active text exactly as typed while the field has focus.  Eager
            // normalization made a Ctrl+A replacement briefly become the fallback
            // value, so entering a custom duration or level could not complete.
            // Bounds are enforced once when the effect list is committed.
            draft.duration = duration.value();
            draft.amplifier = amplifier.value();
            draft.ambient = ambient;
            draft.particles = particles;
            draft.icon = icon;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { confirm(); return true; }
            if (key == 32 && focusField == 0 && selectedIdx >= 0 && selectedIdx < filtered.size()) { selectEffect(filtered.get(selectedIdx), 0); return true; }
            if (key == 258) { focusField = (focusField + 1) % 3; return true; }
            if (focusField == 0) {
                int visibleRows = visibleEffectRows();
                if (key == 264 && selectedIdx < filtered.size() - 1) {
                    selectedIdx++;
                    if (selectedIdx >= scrollOff + visibleRows) scrollOff++;
                    return true;
                }
                if (key == 265 && selectedIdx > 0) { selectedIdx--; if (selectedIdx < scrollOff) scrollOff = selectedIdx; return true; }
                String before = search.value();
                if (search.keyPressed(key, mod)) { if (!before.equals(search.value())) filter(); return true; }
            } else {
                TextEditBuffer buffer = focusField == 1 ? duration : amplifier;
                if (buffer.keyPressed(key, mod)) { syncActiveDraft(); return true; }
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (focusField == 0) {
                if (search.charTyped(c)) { filter(); return true; }
            } else if ((c >= '0' && c <= '9') || c == '-') {
                (focusField == 1 ? duration : amplifier).charTyped(c);
                syncActiveDraft();
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            EffectLayout l = layout(contentX, contentY, contentW, contentH);
            if (lastMouseX >= l.rightX() && lastMouseX < l.rightX() + l.rightW()
                    && lastMouseY >= l.selectedY() && lastMouseY < l.selectedY() + l.selectedListH()) {
                selectedScrollOff -= (int) sy;
                selectedScrollOff = Math.max(0, Math.min(selectedScrollOff,
                        Math.max(0, selectedEffects.size() - l.selectedRows())));
                return true;
            }
            scrollOff -= (int) sy * 3;
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, filtered.size() - l.maxItems())));
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
            g.fill(dx, dy, dx + dw, dy + dh, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
            drawBorder(g, dx, dy, dw, dh, accentColor());

            String title = mode == -2 ? tr("ankinbt.simple.name_color")
                    : (mode == -3 ? tr("ankinbt.simple.potion_custom_color") : tr("ankinbt.simple.dye_color_picker"));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, dx + 10, dy + 8, UiTheme.textMain(), false);
            g.fill(dx + 1, dy + 22, dx + dw - 1, dy + 23, UiTheme.themedBorder(1f, 1f));

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
            drawBorder(g, dx + 10, prevY, 30, 20, UiTheme.themedBorder(1f, 1f));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, String.format("#%06X", selectedColor & 0xFFFFFF), dx + 46, prevY + 6, UiTheme.textMain(), false);
            if (mode == -2) {
                String name = editStack.getHoverName().getString();
                if (font.width(name) > dw - 86) name = font.plainSubstrByWidth(name, dw - 96) + "..";
                Component previewName = Component.literal(name).withStyle(Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(selectedColor)));
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, previewName, dx + 46, prevY + 20, UiTheme.textMain(), false);
            }

            // Apply / Cancel
            int by = dy + dh - 28, bw = 70, bh2 = 20;
            int cancelX = dx + dw / 2 - bw - 6;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh2;
            g.fill(cancelX, by, cancelX + bw, by + bh2, ch ? BTN_HOVER : BTN_BG);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.cancel"), cancelX + (bw - font.width(tr("ankinbt.edit.cancel"))) / 2, by + 6, UiTheme.textDim(), false);

            int okX = dx + dw / 2 + 6;
            boolean oh = mx >= okX && mx < okX + bw && my >= by && my < by + bh2;
            g.fill(okX, by, okX + bw, by + bh2, oh ? accentColor() : 0xFF4F46E5);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.edit.apply"), okX + (bw - font.width(tr("ankinbt.edit.apply"))) / 2, by + 6, UiTheme.textMain(), false);
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
            setStatus(tr("ankinbt.status.edited"), UiTheme.textDim());
            activeSubEditor = null;
        }

        @Override public boolean keyPressed(int key, int scan, int mod) { return true; }
        @Override public boolean charTyped(char c, int mod) { return false; }
    }

    // ==================== ENCHANT PICKER ====================

    class EnchantPickerSubEditor implements SubEditor {
        private final List<String> allEnchants = new ArrayList<>();
        private List<String> filtered = new ArrayList<>();
        private final List<EnchantPickerRow> displayRows = new ArrayList<>();
        private final TextEditBuffer searchQ = new TextEditBuffer("");
        private int scrollOff = 0;
        private int hoverIdx = -1;
        private int selectedIdx = -1;
        private int visibleItems = 1;
        private final TextEditBuffer levelInput = new TextEditBuffer("1");
        private boolean focusLevel = false;
        private boolean focusSpecificFilter = false;
        private boolean specificOnly = false;
        private boolean specificFilterVisible = true;
        private float specificFilterHover = 0f;
        private boolean draggingText = false;

        EnchantPickerSubEditor() {
            try {
                allEnchants.addAll(VersionCompat.get().getAllEnchantIds());
            } catch (Throwable ignored) {
            }
            if (allEnchants.isEmpty()) {
                allEnchants.addAll(ENCHANT_ZH.keySet());
            }
            allEnchants.sort(Comparator.comparingInt((String id) -> SimpleEditorScreen.this.enchantGroup(id))
                    .thenComparing(SimpleEditorScreen.this::getEnchantDisplayName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(String::toString));
            filter();
        }

        private void filter() {
            String selectedId = selectedIdx >= 0 && selectedIdx < filtered.size()
                    ? filtered.get(selectedIdx) : null;
            String query = searchQ.value();
            if (query.isEmpty()) { filtered = new ArrayList<>(allEnchants); }
            else {
                String q = query.toLowerCase(Locale.ROOT);
                filtered = allEnchants.stream().filter(s -> {
                    if (s.toLowerCase().contains(q)) return true;
                    // Also search Chinese name
                    String zh = ENCHANT_ZH.get(s);
                    return zh != null && zh.contains(q);
                }).collect(Collectors.toList());
            }
            if (specificOnly) {
                filtered.removeIf(id -> enchantGroup(id) != 0);
            }
            Set<String> applied = appliedEnchantIds();
            if (!applied.isEmpty()) {
                filtered.removeIf(id -> applied.contains(normalizeRegistryDisplayId(id)));
            }
            filtered.sort(Comparator.comparingInt((String id) -> SimpleEditorScreen.this.enchantGroup(id))
                    .thenComparing(SimpleEditorScreen.this::getEnchantDisplayName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(String::toString));
            selectedIdx = selectedId == null ? -1 : filtered.indexOf(selectedId);
            rebuildDisplayRows();
            scrollOff = 0;
        }

        private void rebuildDisplayRows() {
            displayRows.clear();
            int lastGroup = specificOnly ? 0 : 2;
            for (int group = 0; group <= lastGroup; group++) {
                displayRows.add(EnchantPickerRow.header(group));
                for (int i = 0; i < filtered.size(); i++) {
                    if (enchantGroup(filtered.get(i)) == group) {
                        displayRows.add(EnchantPickerRow.enchant(i, group));
                    }
                }
            }
        }

        @Override
        public void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int x, int y, int w, int h) {
            boolean tiny = h < 76;
            boolean compact = h < 100;
            specificFilterVisible = !tiny;
            if (!tiny) {
                int filterH = compact ? 13 : 15;
                String filterLabel = tr("ankinbt.simple.enchant_filter_specific");
                int filterW = Math.min(Math.max(76, font.width(filterLabel) + 26), Math.max(76, w - 32));
                int filterX = x + w - 8 - filterW;
                int filterY = y + (compact ? 1 : 2);
                boolean filterHovered = mx >= filterX && mx < filterX + filterW
                        && my >= filterY && my < filterY + filterH;
                float speed = AnkiConfig.isUiAnimationEnabled()
                        ? Math.max(0.16f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1f;
                specificFilterHover = UiTheme.approach(specificFilterHover, filterHovered ? 1f : 0f, speed);
                int filterFill = specificOnly
                        ? UiTheme.withAlpha(accentColor() & 0x00FFFFFF, 126)
                        : UiTheme.withAlpha(UiTheme.baseRgb(), 220);
                filterFill = UiTheme.mix(filterFill,
                        UiTheme.withAlpha(accentColor() & 0x00FFFFFF, 72), specificFilterHover);
                g.fill(filterX, filterY, filterX + filterW, filterY + filterH, filterFill);
                drawBorder(g, filterX, filterY, filterW, filterH,
                        specificOnly || focusSpecificFilter ? accentColor() : UiTheme.themedBorder(0.9f, 1f));
                Component shield = UiIcons.component(UiIcons.SHIELD);
                g.drawString(font, shield, filterX + 4, filterY + (filterH - 8) / 2,
                        specificOnly || filterHovered ? UiTheme.textMain() : UiTheme.textDim(), false);
                g.drawString(font, filterLabel, filterX + 17, filterY + (filterH - 8) / 2,
                        specificOnly || filterHovered ? UiTheme.textMain() : UiTheme.textDim(), false);

                String title = tr("ankinbt.simple.pick_enchant");
                int titleBudget = Math.max(12, filterX - (x + 8) - 8);
                if (font.width(title) > titleBudget) {
                    title = font.plainSubstrByWidth(title, Math.max(8, titleBudget - 8)) + "..";
                }
                g.drawString(font, title, x + 8, y + (compact ? 2 : 4), UiTheme.textMain(), false);
            }

            int sx = x + 8, sy = tiny ? y : y + (compact ? 12 : 18), sw = w - 16, sh = tiny ? 14 : compact ? 16 : 18;
            g.fill(sx, sy, sx + sw, sy + sh, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
            drawBorder(g, sx, sy, sw, sh, focusLevel ? UiTheme.themedBorder(1f, 1f) : accentColor());
            renderTextBuffer(g, font, searchQ, sx, sy, sw, sh, !focusLevel, tr("ankinbt.search.hint"));

            int ly = sy + sh + (tiny ? 1 : 4);
            int by = y + h - (tiny ? 21 : compact ? 22 : 30);
            int listH = Math.max(16, by - ly - 2);
            visibleItems = Math.max(1, listH / 16);
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, displayRows.size() - visibleItems)));
            hoverIdx = -1;
            int end = Math.min(scrollOff + visibleItems, displayRows.size());
            for (int i = scrollOff; i < end; i++) {
                int ry = ly + (i - scrollOff) * 16;
                boolean hovered = mx >= x + 8 && mx < x + w - 8 && my >= ry && my < ry + 16;
                EnchantPickerRow pickerRow = displayRows.get(i);
                if (pickerRow.header) {
                    if (hovered) g.fill(x + 8, ry, x + w - 8, ry + 16, UiTheme.withAlpha(accentColor() & 0x00FFFFFF, 32));
                    g.fill(x + 8, ry + 14, x + 28, ry + 16, accentColor());
                    g.drawString(font, tr(enchantGroupKey(pickerRow.group)), x + 32, ry + 4,
                            hovered ? UiTheme.textMain() : accentColor(), false);
                    continue;
                }
                if (hovered) hoverIdx = pickerRow.enchantIndex;
                boolean sel = pickerRow.enchantIndex == selectedIdx;
                if (sel) g.fill(x + 8, ry, x + w - 8, ry + 16, SELECT_BG);
                else if (hovered) g.fill(x + 8, ry, x + w - 8, ry + 16, HOVER);
                String enchId = filtered.get(pickerRow.enchantIndex);
                g.renderItem(ItemEditorVisuals.enchantIconStack(enchId), x + 10, ry);
                String displayName = getEnchantDisplayName(enchId);
                if (font.width(displayName) > w - 48) displayName = font.plainSubstrByWidth(displayName, w - 52) + "..";
                g.drawString(font, displayName, x + 32, ry + 4, sel ? UiTheme.textMain() : UiTheme.textDim(), false);
            }

            g.drawString(font, tr("ankinbt.simple.level"), x + 8, by + 6, UiTheme.textDim(), false);
            int lx = x + 8 + font.width(tr("ankinbt.simple.level")) + 4;
            g.fill(lx, by + 2, lx + 40, by + 20, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
            drawBorder(g, lx, by + 2, 40, 18, focusLevel ? accentColor() : UiTheme.themedBorder(1f, 1f));
            renderTextBuffer(g, font, levelInput, lx, by + 2, 40, 18, focusLevel, "");

            int confirmX = x + w - 78;
            boolean ch = mx >= confirmX && mx < confirmX + 70 && my >= by + 1 && my < by + 21;
            g.fill(confirmX, by + 1, confirmX + 70, by + 21, ch ? accentColor() : accentColor());
            g.drawString(font, tr("ankinbt.add.confirm"), confirmX + (70 - font.width(tr("ankinbt.add.confirm"))) / 2, by + 7, UiTheme.textMain(), false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            boolean tiny = h < 76;
            boolean compact = h < 100;
            if (!tiny) {
                int filterH = compact ? 13 : 15;
                String filterLabel = tr("ankinbt.simple.enchant_filter_specific");
                int filterW = Math.min(Math.max(76, font.width(filterLabel) + 26), Math.max(76, w - 32));
                int filterX = x + w - 8 - filterW;
                int filterY = y + (compact ? 1 : 2);
                if (mx >= filterX && mx < filterX + filterW && my >= filterY && my < filterY + filterH) {
                    focusLevel = false;
                    focusSpecificFilter = true;
                    if (btn == 0) {
                        specificOnly = !specificOnly;
                        filter();
                        UiSound.playClick();
                    }
                    return true;
                }
            }
            int sx = x + 8, sy = tiny ? y : y + (compact ? 12 : 18), sw = w - 16, sh = tiny ? 14 : compact ? 16 : 18;
            if (mx >= sx && mx < sx + sw && my >= sy && my < sy + sh) {
                focusLevel = false;
                focusSpecificFilter = false;
                searchQ.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, searchQ,
                        (int) Math.round(mx) - sx - 4, sw - 8), false);
                draggingText = btn == 0;
                return true;
            }

            int by = y + h - (tiny ? 21 : compact ? 22 : 30);
            int lx = x + 8 + font.width(tr("ankinbt.simple.level")) + 4;
            if (mx >= lx && mx < lx + 40 && my >= by + 2 && my < by + 20) {
                focusLevel = true;
                focusSpecificFilter = false;
                levelInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, levelInput,
                        (int) Math.round(mx) - lx - 4, 32), false);
                draggingText = btn == 0;
                return true;
            }

            int confirmX = x + w - 78;
            if (mx >= confirmX && mx < confirmX + 70 && my >= by + 1 && my < by + 21) { confirm(); return true; }

            if (hoverIdx >= 0 && hoverIdx < filtered.size()) {
                focusSpecificFilter = false;
                selectedIdx = hoverIdx;
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY,
                                    int x, int y, int w, int h) {
            if (!draggingText || button != 0) return false;
            boolean tiny = h < 76;
            boolean compact = h < 100;
            int sx = x + 8, sy = tiny ? y : y + (compact ? 12 : 18), sw = w - 16, sh = tiny ? 14 : compact ? 16 : 18;
            int by = y + h - (tiny ? 21 : compact ? 22 : 30);
            if (!focusLevel && my >= sy - 6 && my <= sy + sh + 6) {
                searchQ.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, searchQ,
                        (int) Math.round(mx) - sx - 4, sw - 8), true);
            } else if (focusLevel && my >= by - 4 && my <= by + 24) {
                levelInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, levelInput,
                        (int) Math.round(mx) - (x + 8 + SimpleEditorScreen.this.font.width(tr("ankinbt.simple.level")) + 4) - 4, 32), true);
            }
            return true;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button, int x, int y, int w, int h) {
            boolean handled = draggingText;
            draggingText = false;
            return handled;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 258) {
                if (focusLevel) {
                    focusLevel = false;
                    focusSpecificFilter = false;
                } else if (specificFilterVisible && !focusSpecificFilter) {
                    focusSpecificFilter = true;
                } else {
                    focusSpecificFilter = false;
                    focusLevel = true;
                }
                return true;
            }
            if (focusSpecificFilter) {
                if (key == 32 || key == 257 || key == 335) {
                    specificOnly = !specificOnly;
                    filter();
                    UiSound.playClick();
                }
                return true;
            }
            if (key == 257 || key == 335) { confirm(); return true; }
            if (focusLevel) {
                if (levelInput.keyPressed(key, mod)) return true;
            } else {
                if (searchQ.keyPressed(key, mod)) { filter(); return true; }
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c >= 32) {
                if (focusLevel) {
                    if (c >= '0' && c <= '9') levelInput.charTyped(c);
                } else {
                    searchQ.charTyped(c); filter();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double sx, double sy) {
            scrollOff -= (int) sy * 3;
            scrollOff = Math.max(0, Math.min(scrollOff, Math.max(0, displayRows.size() - visibleItems)));
            return true;
        }

        private void confirm() {
            if (selectedIdx < 0 || selectedIdx >= filtered.size()) {
                setStatus(tr("ankinbt.simple.select_enchant_first"), ERROR_C); return;
            }
            try {
                int level = Integer.parseInt(levelInput.value());
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
        private final TextEditBuffer searchQ = new TextEditBuffer("");
        private int scrollOff = 0;
        private int hoverIdx = -1;
        private int selectedIdx = -1;
        private final TextEditBuffer amountInput = new TextEditBuffer("1.0");
        private int focusField = 0; // 0=search, 1=amount
        private boolean draggingText = false;
        private int selectedOp = 0; // 0=ADD_VALUE, 1=ADD_MULTIPLIED_BASE, 2=ADD_MULTIPLIED_TOTAL
        private int selectedSlot = 0; // index into SLOT_KEYS

        private static final String[] SLOT_KEYS = { "any", "mainhand", "offhand", "head", "chest", "legs", "feet", "hand", "armor" };

        AttributePickerSubEditor() {
            try {
                allAttrs.addAll(VersionCompat.get().getAllAttributeIds());
            } catch (Throwable ignored) {
            }
            if (allAttrs.isEmpty()) {
                allAttrs.addAll(ATTR_ZH.keySet());
            }
            Collections.sort(allAttrs);
            filtered = new ArrayList<>(allAttrs);
        }

        private void filter() {
            String query = searchQ.value();
            if (query.isEmpty()) { filtered = new ArrayList<>(allAttrs); }
            else {
                String q = query.toLowerCase();
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
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.pick_attr"), x + 8, y + 4, UiTheme.textMain(), false);

            // Search box
            int sx = x + 8, sy = y + 18, sw = w - 16, sh = 18;
            g.fill(sx, sy, sx + sw, sy + sh, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
            drawBorder(g, sx, sy, sw, sh, focusField == 0 ? accentColor() : UiTheme.themedBorder(1f, 1f));
            renderTextBuffer(g, font, searchQ, sx, sy, sw, sh, focusField == 0, tr("ankinbt.search.hint"));

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
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, displayName, x + 32, ry + 4, sel ? UiTheme.textMain() : UiTheme.textDim(), false);
                if (showNotes && getAttrNote(attrId) != null) {
                    int infoX = x + w - 24;
                    g.fill(infoX, ry + 3, infoX + 10, ry + 13, hovered ? accentColor() : BTN_BG);
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, "i", infoX + 4, ry + 5, hovered ? UiTheme.textMain() : UiTheme.textDim(), false);
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
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, shown, x + 26, noteY + 5, UiTheme.textDim(), false);
                } else {
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.attr_note_hint"), x + 10, noteY + 5, UiTheme.textDim(), false);
                }
            }

            // Bottom controls area
            int bottomY = y + h - 90;

            // Amount input
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.attr_amount"), x + 8, bottomY + 4, UiTheme.textDim(), false);
            int ax = x + 8 + font.width(tr("ankinbt.simple.attr_amount")) + 4;
            int aw = 80;
            g.fill(ax, bottomY, ax + aw, bottomY + 18, UiTheme.withAlpha(UiTheme.baseRgb(), 245));
            drawBorder(g, ax, bottomY, aw, 18, focusField == 1 ? accentColor() : UiTheme.themedBorder(1f, 1f));
            renderTextBuffer(g, font, amountInput, ax, bottomY, aw, 18, focusField == 1, "");

            // Operation selector
            int opY = bottomY + 22;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.attr_operation"), x + 8, opY + 4, UiTheme.textDim(), false);
            int opX = x + 8 + font.width(tr("ankinbt.simple.attr_operation")) + 4;
            String[] opLabels = isZh() ? OP_NAMES_ZH : OP_NAMES_EN;
            for (int i = 0; i < 3; i++) {
                int bw = font.width(opLabels[i]) + 10;
                boolean hover = mx >= opX && mx < opX + bw && my >= opY && my < opY + 18;
                boolean active = i == selectedOp;
                g.fill(opX, opY, opX + bw, opY + 18, active ? accentColor() : (hover ? BTN_HOVER : BTN_BG));
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, opLabels[i], opX + 5, opY + 5, active ? UiTheme.textMain() : UiTheme.textDim(), false);
                opX += bw + 4;
            }

            // Slot selector
            int slotY = opY + 22;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.simple.attr_slot"), x + 8, slotY + 4, UiTheme.textDim(), false);
            int slotX = x + 8 + font.width(tr("ankinbt.simple.attr_slot")) + 4;
            for (int i = 0; i < SLOT_KEYS.length; i++) {
                String slotLabel = isZh() ? SLOT_ZH.getOrDefault(SLOT_KEYS[i], SLOT_KEYS[i]) : SLOT_KEYS[i];
                int bw = font.width(slotLabel) + 8;
                boolean hover = mx >= slotX && mx < slotX + bw && my >= slotY && my < slotY + 18;
                boolean active = i == selectedSlot;
                g.fill(slotX, slotY, slotX + bw, slotY + 18, active ? accentColor() : (hover ? BTN_HOVER : BTN_BG));
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, slotLabel, slotX + 4, slotY + 5, active ? UiTheme.textMain() : UiTheme.textDim(), false);
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
            g.fill(confirmX, confirmY, confirmX + 70, confirmY + 20, ch ? accentColor() : 0xFF4F46E5);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.add.confirm"), confirmX + (70 - font.width(tr("ankinbt.add.confirm"))) / 2, confirmY + 6, UiTheme.textMain(), false);
        }

        private boolean isZh() {
            String lang = Minecraft.getInstance().options.languageCode;
            return lang != null && lang.startsWith("zh");
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn, int x, int y, int w, int h) {
            draggingText = false;
            // Search box focus
            int sx = x + 8, sy = y + 18, sw = w - 16, sh = 18;
            if (mx >= sx && mx < sx + sw && my >= sy && my < sy + sh) {
                focusField = 0;
                searchQ.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, searchQ,
                        (int) Math.round(mx) - sx - 4, sw - 8), false);
                draggingText = btn == 0;
                return true;
            }

            // Amount box focus
            int bottomY = y + h - 90;
            int ax = x + 8 + font.width(tr("ankinbt.simple.attr_amount")) + 4;
            if (mx >= ax && mx < ax + 80 && my >= bottomY && my < bottomY + 18) {
                focusField = 1;
                amountInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, amountInput,
                        (int) Math.round(mx) - ax - 4, 72), false);
                draggingText = btn == 0;
                return true;
            }

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
        public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY,
                                    int x, int y, int w, int h) {
            if (!draggingText || button != 0) return false;
            int bottomY = y + h - 90;
            int sx = x + 8, sy = y + 18, sw = w - 16, sh = 18;
            int ax = x + 8 + font.width(tr("ankinbt.simple.attr_amount")) + 4;
            if (focusField == 0 && my >= sy - 8 && my <= sy + sh + 8) {
                searchQ.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, searchQ,
                        (int) Math.round(mx) - sx - 4, sw - 8), true);
                filter();
                return true;
            }
            if (focusField == 1 && my >= bottomY - 8 && my <= bottomY + 18 + 8) {
                amountInput.moveTo(bufferCursorFromMouse(SimpleEditorScreen.this.font, amountInput,
                        (int) Math.round(mx) - ax - 4, 72), true);
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button, int x, int y, int w, int h) {
            boolean handled = draggingText;
            draggingText = false;
            return handled;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mod) {
            if (key == 257 || key == 335) { confirm(); return true; }
            if (key == 258) { focusField = (focusField + 1) % 2; return true; } // Tab
            if (focusField == 1) {
                amountInput.keyPressed(key, mod);
            } else {
                if (searchQ.keyPressed(key, mod)) filter();
            }
            return true;
        }

        @Override
        public boolean charTyped(char c, int mod) {
            if (c >= 32) {
                if (focusField == 1) {
                    if ((c >= '0' && c <= '9') || c == '.' || c == '-') {
                        amountInput.charTyped(c);
                    }
                } else {
                    searchQ.charTyped(c); filter();
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
                double amount = Double.parseDouble(amountInput.value());
                addAttribute(filtered.get(selectedIdx), amount, opFromIndex(selectedOp), slotFromKey(SLOT_KEYS[selectedSlot]));
            } catch (NumberFormatException e) {
                setStatus(tr("ankinbt.simple.invalid_number"), ERROR_C);
            }
        }
    }
}





