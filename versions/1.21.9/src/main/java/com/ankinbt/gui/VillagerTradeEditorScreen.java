package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.util.DebugLog;
import com.ankinbt.util.ItemRegistryHelper;
import com.ankinbt.util.UiSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VillagerTradeEditorScreen extends Screen {
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");
    private static final int TRADE_FIELD_COUNT = 8;
    private static final int TRADE_FIELD_BOX_HEIGHT = 20;
    private static final int TRADE_CARD_WIDTH = 104;
    private static final int TRADE_CARD_HEIGHT = 28;
    private static final int TRADE_CARD_GAP = 5;
    private static final String FULL_STACK_KEY = "__ankinbt_full_stack";

    private static final int TXT_TITLE = 0xFFF3F6FF;
    private static final int TXT_MAIN = 0xFFD9E2F2;
    private static final int TXT_DIM = 0xFF8EA3C7;
    private static final int TXT_OK = 0xFF34D399;
    private static final int TXT_ERR = 0xFFEF4444;
    private static final int SIMPLE_C1 = 0xFFE2E8F0;
    private static final int SIMPLE_C2 = 0xFF94A3B8;
    private static final int SIMPLE_C3 = 0xFF64748B;
    private static final int SIMPLE_BORDER = 0xFF222236;
    private static final int SIMPLE_BTN_BG = 0x30FFFFFF;
    private static final int SIMPLE_BTN_HOVER = 0x50FFFFFF;
    private static final int SIMPLE_SUCCESS = 0xFF22C55E;

    private static final String[] PROFESSIONS = new String[]{
            "",
            "minecraft:farmer",
            "minecraft:librarian",
            "minecraft:cleric",
            "minecraft:armorer",
            "minecraft:toolsmith",
            "minecraft:weaponsmith",
            "minecraft:fletcher",
            "minecraft:cartographer",
            "minecraft:butcher",
            "minecraft:leatherworker",
            "minecraft:mason",
            "minecraft:shepherd",
            "minecraft:fisherman",
            "minecraft:unemployed",
            "minecraft:nitwit"
    };

    private final Entity targetEntity;
    private final ItemStack sourceStack;
    private final int inventorySlot;
    private final Screen parent;

    private final List<UiBtn> buttons = new ArrayList<>();

    private EditBox buyId;
    private EditBox buyCount;
    private EditBox buy2Id;
    private EditBox buy2Count;
    private EditBox sellId;
    private EditBox sellCount;
    private EditBox maxUses;
    private EditBox xp;
    private EditBox invalidBox;

    private final List<TradeData> trades = new ArrayList<>();
    private int tradeIndex = 0;
    private int professionIndex = 1;
    private int villagerLevel = 1;
    private boolean rewardExp = true;
    private String villagerType = "minecraft:plains";
    private boolean dirty = false;
    private boolean confirmClose = false;
    private boolean confirmReset = false;
    private final List<StateSnapshot> undoStack = new ArrayList<>();
    private StateSnapshot baselineSnapshot;
    private static final int MAX_UNDO = 50;
    private static final Map<UUID, CompoundTag> ENTITY_PATCH_CACHE = new HashMap<>();
    private final List<IconHit> iconHits = new ArrayList<>();
    private final List<InvSlotHit> invSlotHits = new ArrayList<>();
    private final Map<String, Item> itemCache = new HashMap<>();
    private InvPickTarget invPickTarget = InvPickTarget.NONE;

    private Component status = Component.empty();
    private int statusColor = TXT_DIM;
    private long statusTime = 0;

    private int px, py, pw, ph;
    private EditorDock.Bounds barBounds;
    private EditorDock.Bounds drawerBounds;
    private boolean drawerAbove;
    private boolean drawerOpen = true;
    private float drawerAnim = 0f;
    private float contentAnim = 1f;
    private float activeIndicatorX = -1f;
    private final float[] tabHoverAnim = new float[VillagerTab.values().length];
    private final float[] menuToolHoverAnim = new float[3];
    private boolean draggingPanel;
    private int panelDragOffsetX;
    private int panelDragOffsetY;
    private boolean resizingEditor;
    private boolean editorSizeFocused;
    private float editorScale = EditorDock.DEFAULT_EDITOR_SCALE;
    private float editorWidthAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
    private float editorHeightAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
    private float sizeControlHoverAnim;
    private int layoutWidth = -1;
    private int layoutHeight = -1;
    private VillagerTab activeTab = VillagerTab.TRADE;
    private int contentScroll;
    private int contentScrollMax;
    private float openAnim = 0f;
    private float brandAnim = 0f;
    private float settingsHoverAnim = 0f;
    private float modalAnim = 0f;
    private final float[] modalButtonHover = new float[3];
    private int rightLabelX;
    private int rightActionLeft;
    private int rightTradeOpsY;
    private int rightBuyY;
    private int rightBuy2Y;
    private int rightSellY;
    private boolean suppressDirtySync = false;
    private RightPage rightPage = RightPage.TRADE;
    private boolean initializedFromContext = false;
    private int tradeScroll = 0;
    private int tradeScrollMax = 0;
    private int tradeNavScroll = 0;
    private int tradeNavScrollMax = 0;
    private float tradeNavScrollVisual = 0f;
    private float tradeSelectionAnim = 1f;
    private int dragTradeIndex = -1;
    private int tradeDropIndex = -1;
    private double dragTradeStartX;
    private double dragTradeStartY;
    private boolean dragTradeActive = false;
    private long lastTradeAutoScrollAt = 0L;
    private final List<TradeCardHit> tradeCardHits = new ArrayList<>();
    private Component hoveredTradeTip;
    /**
     * Tooltips must be submitted after the drawer, menu bar, and inventory
     * overlay. Submitting them from inside a scissor block lets later surfaces
     * cover the tooltip in the 26.1 extraction renderer.
     */
    private Component pendingTooltip;
    private ItemStack pendingItemTooltip;
    private int pendingTooltipX;
    private int pendingTooltipY;

    private VillagerTradeEditorScreen(Entity targetEntity, ItemStack sourceStack, int inventorySlot, Screen parent) {
        super(Component.translatable("ankinbt.villager.title"));
        this.targetEntity = targetEntity;
        this.sourceStack = sourceStack == null ? ItemStack.EMPTY : sourceStack.copy();
        this.inventorySlot = inventorySlot;
        this.parent = parent;
    }

    public static VillagerTradeEditorScreen forEntity(Entity entity) {
        return new VillagerTradeEditorScreen(entity, ItemStack.EMPTY, -1, null);
    }

    public static VillagerTradeEditorScreen forEntity(Entity entity, Screen parent) {
        return new VillagerTradeEditorScreen(entity, ItemStack.EMPTY, -1, parent);
    }

    public static VillagerTradeEditorScreen forSpawnEgg(ItemStack stack, int inventorySlot) {
        return new VillagerTradeEditorScreen(null, stack, inventorySlot, null);
    }

    public static VillagerTradeEditorScreen forSpawnEgg(ItemStack stack, int inventorySlot, Screen parent) {
        return new VillagerTradeEditorScreen(null, stack, inventorySlot, parent);
    }

    @Override
    protected void init() {
        editorScale = AnkiConfig.getVillagerEditorScale();
        editorWidthAdjustment = AnkiConfig.getVillagerEditorWidthAdjustment();
        editorHeightAdjustment = AnkiConfig.getVillagerEditorHeightAdjustment();
        captureDraftFromBoxes();
        recalcBounds();

        buyId = box(0, -1000, 80, "minecraft:emerald");
        buyCount = box(0, -1000, 48, "1");
        buy2Id = box(0, -1000, 80, "");
        buy2Count = box(0, -1000, 48, "1");
        sellId = box(0, -1000, 80, "minecraft:bread");
        sellCount = box(0, -1000, 48, "6");
        maxUses = box(0, -1000, 80, "12");
        xp = box(0, -1000, 80, "1");

        if (!initializedFromContext) {
            readContextDefaults();
            ensureTrades();
            initializedFromContext = true;
            dirty = false;
        } else {
            ensureTrades();
        }
        loadTradeToForm(tradeIndex);
        layoutTradeFields();
        rebuildButtons();
        if (baselineSnapshot == null) {
            dirty = false;
            baselineSnapshot = captureState();
            undoStack.clear();
        }
    }

    private void recalcBounds() {
        boolean viewportChanged = layoutWidth != width || layoutHeight != height;
        if (barBounds == null || drawerBounds == null) {
            applyMenuLayout(EditorDock.menuLayout(width, height, 306, false, editorScale,
                    editorWidthAdjustment, editorHeightAdjustment));
        } else if (viewportChanged) {
            applyMenuLayout(EditorDock.menuLayoutAt(width, height, 306, false, barBounds.x(), barBounds.y(),
                    editorScale, editorWidthAdjustment, editorHeightAdjustment));
        }
        layoutWidth = width;
        layoutHeight = height;
        layoutTradeFields();
        if (viewportChanged && buyId != null) {
            rebuildButtons();
        }
    }

    private void applyMenuLayout(EditorDock.MenuLayout layout) {
        barBounds = layout.bar();
        drawerBounds = layout.drawer();
        drawerAbove = layout.drawerAbove();
        px = drawerBounds.x();
        py = drawerBounds.y();
        pw = drawerBounds.width();
        ph = drawerBounds.height();
        layoutTradeFields();
    }

    private int tradeFieldLeft() {
        return px + 48;
    }

    private int tradeFieldRight() {
        int leftCardRight = px + pw / 2 - 12;
        return leftCardRight - (AnkiConfig.isUiCompactLayout() ? 22 : 28);
    }

    private int tradeFieldLabelWidth() {
        return AnkiConfig.isUiCompactLayout() ? 112 : 122;
    }

    private int tradeFieldInputX() {
        return tradeFieldLeft() + tradeFieldLabelWidth() + (AnkiConfig.isUiCompactLayout() ? 8 : 12);
    }

    private int tradeFieldInputWidth() {
        return Math.max(104, tradeFieldRight() - tradeFieldInputX());
    }

    private int tradeFieldRowGap() {
        int minGap = AnkiConfig.isUiCompactLayout() ? 2 : 3;
        int maxGap = AnkiConfig.isUiCompactLayout() ? 5 : 6;
        int usable = tradeCardBottomY() - tradeFieldStartY() - tradeFieldBottomPadding() - TRADE_FIELD_COUNT * TRADE_FIELD_BOX_HEIGHT;
        int gap = usable / Math.max(1, TRADE_FIELD_COUNT - 1);
        return TRADE_FIELD_BOX_HEIGHT + Math.max(minGap, Math.min(maxGap, gap));
    }

    private int tradeFieldStartY() {
        int minTop = py + (AnkiConfig.isUiCompactLayout() ? 120 : 124);
        int iconBottom = py + 94 + 18;
        int desiredTop = iconBottom + (AnkiConfig.isUiCompactLayout() ? 12 : 14);
        int minGap = AnkiConfig.isUiCompactLayout() ? 2 : 3;
        int maxTop = tradeCardBottomY() - tradeFieldBottomPadding()
                - TRADE_FIELD_COUNT * TRADE_FIELD_BOX_HEIGHT
                - (TRADE_FIELD_COUNT - 1) * minGap;
        return Math.max(minTop, Math.min(desiredTop, maxTop));
    }

    private int tradeFieldBottomPadding() {
        return AnkiConfig.isUiCompactLayout() ? 10 : 12;
    }

    private int tradeFieldClipTop() {
        return tradeFieldStartY() - 6;
    }

    private int tradeFieldClipBottom() {
        return tradeCardBottomY() - 8;
    }

    private void updateTradeFieldLayout() {
        layoutTradeFields();
    }

    private int bodyTop() {
        return py + 70;
    }

    private int bodyBottom() {
        return py + ph - 28;
    }

    /* Keep the trade editor's navigation geometry identical to the entity editor. */
    private int menuBrandWidth() {
        EditorDock.Bounds menu = menuBounds();
        return menu == null ? 58 : Math.min(72, Math.max(58, menu.width() / 7));
    }

    private int menuToolWidth() {
        return 26;
    }

    private int menuToolsStart() {
        EditorDock.Bounds menu = menuBounds();
        return menu == null ? 0 : menu.x() + menu.width() - menuToolWidth() * 3;
    }

    private int menuTabsStart() {
        EditorDock.Bounds menu = menuBounds();
        return menu == null ? 0 : menu.x() + menuBrandWidth();
    }

    private EditorDock.Bounds menuBounds() {
        if (drawerBounds != null && (drawerOpen || drawerAnim > 0.01f)) {
            return new EditorDock.Bounds(drawerBounds.x(), drawerBounds.y(), drawerBounds.width(),
                    Math.min(EditorDock.MENU_BAR_HEIGHT, drawerBounds.height()));
        }
        return barBounds;
    }

    private int menuTabX(int index) {
        int count = VillagerTab.values().length;
        if (menuBounds() == null || count <= 0) return 0;
        int start = menuTabsStart();
        int end = menuToolsStart();
        int available = Math.max(0, end - start);
        return start + (available * Math.max(0, Math.min(index, count))) / count;
    }

    private int menuTabWidth(int index) {
        return Math.max(1, menuTabX(index + 1) - menuTabX(index));
    }

    private int menuTabAt(double mx, double my) {
        EditorDock.Bounds menu = menuBounds();
        if (menu == null || !menu.contains(mx, my)) return -1;
        for (int i = 0; i < VillagerTab.values().length; i++) {
            int x = menuTabX(i);
            int w = menuTabWidth(i);
            if (mx >= x && mx < x + w) return i;
        }
        return -1;
    }

    private void selectVillagerTab(VillagerTab selected) {
        if (selected == null) return;
        if (selected == VillagerTab.ENTITY) {
            openEntityEditor();
            return;
        }
        if (selected == activeTab) {
            UiSound.playClick();
            captureDraftFromBoxes();
            drawerOpen = !drawerOpen;
            if (!drawerOpen) unfocusEditBoxes();
            return;
        }
        switchTab(selected);
    }

    private int contentLeft() {
        return px + 10;
    }

    private int contentWidth() {
        return Math.max(120, pw - 20);
    }

    private int contentTopPadding() {
        return 4;
    }

    private int contentBottomPadding() {
        return AnkiConfig.isUiCompactLayout() ? 4 : 6;
    }

    private int contentGap() {
        return AnkiConfig.isUiCompactLayout() ? 4 : 6;
    }

    private int contentRowHeight() {
        return AnkiConfig.isUiCompactLayout() ? 24 : 28;
    }

    private int tradeSummaryHeight() {
        return AnkiConfig.isUiCompactLayout() ? 38 : 42;
    }

    private int itemPanelHeight() {
        return AnkiConfig.isUiCompactLayout() ? 50 : 54;
    }

    private int contentBaseY() {
        return bodyTop() + contentTopPadding() - contentScroll + contentAnimationOffset();
    }

    /**
     * Keep tab changes in sync with the item/entity editors: the drawer keeps
     * its full body viewport while the new card stack settles by a few pixels.
     */
    private int contentAnimationOffset() {
        return Math.round((1f - contentAnim) * (drawerAbove ? -7f : 7f));
    }

    private int tradeMaxUsesRowY() {
        return contentBaseY() + tradeSummaryHeight() + contentGap();
    }

    private int tradeXpRowY() {
        return tradeMaxUsesRowY() + contentRowHeight() + contentGap();
    }

    private int tradeRewardRowY() {
        return tradeXpRowY() + contentRowHeight() + contentGap();
    }

    private int secondItemPanelY() {
        return contentBaseY() + itemPanelHeight() + contentGap();
    }

    private int itemFieldY(int panelY) {
        return panelY + itemPanelHeight() - TRADE_FIELD_BOX_HEIGHT - 4;
    }

    private int centeredFieldY(int rowY) {
        return rowY + Math.max(0, (contentRowHeight() - TRADE_FIELD_BOX_HEIGHT) / 2);
    }

    private int centeredTextY(int rowY, int rowHeight) {
        return rowY + Math.max(3, (rowHeight - 8) / 2);
    }

    private int tradeValueColumnX() {
        int x = contentLeft();
        int w = contentWidth();
        int preferred = x + Math.min(146, Math.max(108, w / 3));
        return Math.min(preferred, x + w - 82);
    }

    private int tradeValueColumnWidth() {
        return Math.max(72, contentLeft() + contentWidth() - 10 - tradeValueColumnX());
    }

    private int villagerControlCount() {
        int count = 3;
        if (targetEntity != null) count++;
        if (!sourceStack.isEmpty()) count++;
        return count;
    }

    private int contentHeightForTab() {
        int rowH = contentRowHeight();
        int gap = contentGap();
        return switch (activeTab) {
            case TRADE -> tradeSummaryHeight() + (rowH * 3) + (gap * 3);
            case BUY -> itemPanelHeight() * 2 + gap;
            case SELL -> itemPanelHeight();
            case VILLAGER -> villagerControlCount() * rowH
                    + Math.max(0, villagerControlCount() - 1) * gap;
            case ENTITY -> 0;
        };
    }

    private void updateContentScrollBounds() {
        int viewportHeight = Math.max(1, bodyBottom() - bodyTop());
        int contentExtent = contentTopPadding() + contentHeightForTab() + contentBottomPadding();
        contentScrollMax = Math.max(0, contentExtent - viewportHeight);
        contentScroll = Math.max(0, Math.min(contentScroll, contentScrollMax));
    }

    private void hideBox(EditBox box) {
        setBoxBounds(box, -1000, -1000, 20);
        if (box != null) {
            box.active = false;
            box.visible = false;
            box.setFocused(false);
        }
    }

    private void layoutTradeFields() {
        hideBox(buyId);
        hideBox(buyCount);
        hideBox(buy2Id);
        hideBox(buy2Count);
        hideBox(sellId);
        hideBox(sellCount);
        hideBox(maxUses);
        hideBox(xp);
        if (drawerBounds == null) return;
        updateContentScrollBounds();

        int x = contentLeft();
        int w = contentWidth();
        int fieldX = x + 38;
        int countW = Math.min(52, Math.max(40, w / 7));
        int idW = Math.max(74, w - 58 - countW);
        int firstY = itemFieldY(contentBaseY());
        int secondY = itemFieldY(secondItemPanelY());
        if (activeTab == VillagerTab.BUY) {
            setBoxBounds(buyId, fieldX, firstY, idW);
            setBoxBounds(buyCount, x + w - countW - 10, firstY, countW);
            setBoxBounds(buy2Id, fieldX, secondY, idW);
            setBoxBounds(buy2Count, x + w - countW - 10, secondY, countW);
        } else if (activeTab == VillagerTab.SELL) {
            setBoxBounds(sellId, fieldX, firstY, idW);
            setBoxBounds(sellCount, x + w - countW - 10, firstY, countW);
        } else if (activeTab == VillagerTab.TRADE) {
            int valueX = tradeValueColumnX();
            int valueW = tradeValueColumnWidth();
            setBoxBounds(maxUses, valueX, centeredFieldY(tradeMaxUsesRowY()), valueW);
            setBoxBounds(xp, valueX, centeredFieldY(tradeXpRowY()), valueW);
        }
    }

    private void setBoxBounds(EditBox box, int x, int y, int w) {
        if (box == null) return;
        box.setX(x);
        box.setY(y);
        box.setWidth(w);
        boolean enabled = drawerOpen && drawerAnim >= 0.92f && contentAnim >= 0.92f
                && !confirmClose && !confirmReset && invPickTarget == InvPickTarget.NONE;
        box.active = enabled;
        box.visible = enabled;
    }

    private int tradeCardBottomY() {
        return py + ph - 62;
    }

    private int tradeStatusY() {
        return py + ph - 40;
    }

    private EditBox box(int x, int y, int w, String value) {
        EditBox b = new EditBox(font, x, y, w, 20, Component.empty());
        b.setValue(value);
        b.setResponder(v -> {
            if (!suppressDirtySync) {
                dirty = true;
                invalidBox = null;
                captureDraftFromBoxes();
            }
        });
        try {
            b.setBordered(false);
        } catch (Throwable ignored) {}
        try {
            b.setTextColor(TXT_MAIN);
        } catch (Throwable ignored) {}
        try {
            b.setTextColorUneditable(TXT_DIM);
        } catch (Throwable ignored) {}
        addRenderableWidget(b);
        return b;
    }

    private void rebuildButtons() {
        buttons.clear();
        if (drawerBounds == null) return;
        int x = contentLeft();
        updateContentScrollBounds();
        int y = contentBaseY();
        int w = contentWidth();
        int rowH = contentRowHeight();
        int gap = contentGap();

        if (activeTab == VillagerTab.TRADE) {
            buttons.add(new UiBtn(x, tradeRewardRowY(), w, rowH,
                    () -> Component.translatable("ankinbt.villager.reward_exp",
                            rewardExp ? tr("ankinbt.simple.on") : tr("ankinbt.simple.off")).getString(),
                    this::toggleRewardExp, true, () -> rewardExp));
            return;
        }

        if (activeTab == VillagerTab.BUY || activeTab == VillagerTab.SELL) {
            int actionW = Math.max(58, Math.min(92, (w - 90) / 3));
            int actionX = x + w - actionW * 2 - gap - 10;
            int actionH = 20;
            int actionInsetY = 4;
            if (activeTab == VillagerTab.BUY) {
                buttons.add(new UiBtn(actionX, y + actionInsetY, actionW, actionH,
                        () -> tr("ankinbt.villager.edit"), () -> openPickerFor(InvPickTarget.BUY), true, null));
                buttons.add(new UiBtn(actionX + actionW + gap, y + actionInsetY, actionW, actionH,
                        () -> tr("ankinbt.villager.pick.inv"), () -> openInventoryPicker(InvPickTarget.BUY), true, null));
                int secondY = secondItemPanelY() + actionInsetY;
                buttons.add(new UiBtn(actionX, secondY, actionW, actionH,
                        () -> tr("ankinbt.villager.edit"), () -> openPickerFor(InvPickTarget.BUY2), true, null));
                buttons.add(new UiBtn(actionX + actionW + gap, secondY, actionW, actionH,
                        () -> tr("ankinbt.villager.pick.inv"), () -> openInventoryPicker(InvPickTarget.BUY2), true, null));
            } else {
                buttons.add(new UiBtn(actionX, y + actionInsetY, actionW, actionH,
                        () -> tr("ankinbt.villager.edit"), () -> openPickerFor(InvPickTarget.SELL), true, null));
                buttons.add(new UiBtn(actionX + actionW + gap, y + actionInsetY, actionW, actionH,
                        () -> tr("ankinbt.villager.pick.inv"), () -> openInventoryPicker(InvPickTarget.SELL), true, null));
            }
            return;
        }

        buttons.add(new UiBtn(x, y, w, rowH,
                () -> Component.translatable("ankinbt.villager.profession", professionLabel()).getString(),
                this::cycleProfession, !isWanderingTraderContext(), null));
        y += rowH + gap;
        buttons.add(new UiBtn(x, y, w, rowH,
                () -> Component.translatable("ankinbt.villager.level", String.valueOf(villagerLevel)).getString(),
                this::cycleLevel, !isWanderingTraderContext(), null));
        y += rowH + gap;
        buttons.add(new UiBtn(x, y, w, rowH,
                () -> Component.translatable("ankinbt.villager.require_prof",
                        onOff(AnkiConfig.isVillagerRequireProfession())).getString(),
                () -> AnkiConfig.setVillagerRequireProfession(!AnkiConfig.isVillagerRequireProfession()), true,
                AnkiConfig::isVillagerRequireProfession));
        y += rowH + gap;
        if (targetEntity != null) {
            buttons.add(new UiBtn(x, y, w, rowH,
                    () -> tr("key.ankinbt.open_entity_editor"), this::openEntityEditor, true, null));
            y += rowH + gap;
        }
        if (!sourceStack.isEmpty()) {
            buttons.add(new UiBtn(x, y, w, rowH,
                    () -> tr("ankinbt.villager.open_spawn_egg_nbt"), this::openSpawnEggNbt, true, null));
        }
    }

    private void openPickerFor(InvPickTarget target) {
        captureDraftFromBoxes();
        Minecraft.getInstance().setScreen(new ItemPickerScreen(this, id -> {
            pushUndo();
            EditBox box = boxForTarget(target);
            setBoxValue(box, id);
            captureDraftFromBoxes();
            syncCurrentTrade(false);
            dirty = true;
        }));
    }

    private void openInventoryPicker(InvPickTarget target) {
        captureDraftFromBoxes();
        invPickTarget = target == InvPickTarget.NONE ? InvPickTarget.BUY : target;
    }

    private void openEntityEditor() {
        // Preserve incomplete trade drafts while switching to the paired entity editor.
        captureDraftFromBoxes();
        syncCurrentTrade(false);
        if (targetEntity != null) {
            UiSound.playClick();
            Minecraft.getInstance().setScreen(EntityEditorScreen.forEntity(targetEntity, this));
            return;
        }
        if (!sourceStack.isEmpty()) {
            UiSound.playClick();
            Minecraft.getInstance().setScreen(EntityEditorScreen.forSpawnEgg(sourceStack, inventorySlot, this));
            return;
        }
        setStatus(Component.translatable("ankinbt.villager.target_hint"), TXT_ERR);
    }

    private void openSpawnEggNbt() {
        if (sourceStack.isEmpty() || !syncCurrentTrade(true)) return;
        Minecraft.getInstance().setScreen(new NbtEditorScreen(sourceStack));
    }

    private String inventoryPickButtonLabel() {
        return tr("ankinbt.villager.pick.inv") + " [" + focusedTargetText() + "]";
    }

    private String focusedTargetText() {
        return switch (focusedTarget()) {
            case BUY2 -> tr("ankinbt.villager.buy2_item");
            case SELL -> tr("ankinbt.villager.sell_item");
            default -> tr("ankinbt.villager.buy_item");
        };
    }

    private InvPickTarget focusedTarget() {
        if (sellId != null && sellId.isFocused()) return InvPickTarget.SELL;
        if (buy2Id != null && buy2Id.isFocused()) return InvPickTarget.BUY2;
        return InvPickTarget.BUY;
    }

    private void resetForm() {
        if (baselineSnapshot != null) {
            applyState(baselineSnapshot);
        }
        dirty = false;
        undoStack.clear();
        resetTradeDrag();
        ensureTradeVisible();
        setStatus(Component.translatable("ankinbt.entity.reset_done"), TXT_OK);
        rebuildButtons();
    }

    private void readContextDefaults() {
        if (isWanderingTraderContext()) {
            professionIndex = 0;
            villagerLevel = 1;
        }

        CompoundTag root = null;
        LoadedVillagerDefaults liveDefaults = targetEntity == null ? null : readDefaultsFromIntegratedServer(targetEntity);
        if (liveDefaults != null) {
            professionIndex = normalizeProfessionIndex(liveDefaults.professionIndex());
            villagerLevel = liveDefaults.villagerLevel();
            villagerType = liveDefaults.villagerType();
            rewardExp = liveDefaults.rewardExp();
            trades.clear();
            for (TradeData trade : liveDefaults.trades()) {
                trades.add(trade.copy());
            }
            normalizeProfessionState();
            if (!trades.isEmpty()) return;
        }

        if (targetEntity != null) {
            root = readEntityTag(targetEntity);
        } else if (!sourceStack.isEmpty()) {
            root = SpawnEggEditorHelper.getEntityData(sourceStack).orElse(null);
        }

        if (root == null) {
            if (targetEntity != null) {
                root = new CompoundTag();
                injectRuntimeVillagerDataIfMissing(root, targetEntity);
                injectRuntimeOffersIfMissing(root, targetEntity);
            } else {
                professionIndex = defaultProfessionIndex();
                villagerLevel = 1;
                villagerType = "minecraft:plains";
                trades.clear();
                trades.add(TradeData.defaults());
                return;
            }
        }

        if (targetEntity != null) {
            CompoundTag cached = ENTITY_PATCH_CACHE.get(targetEntity.getUUID());
            if (cached != null && !cached.isEmpty()) {
                root.merge(copyCompound(cached));
            }
            injectRuntimeVillagerDataIfMissing(root, targetEntity);
            injectRuntimeOffersIfMissing(root, targetEntity);
        }

        CompoundTag vd = readCompound(root, "VillagerData");
        if (vd != null) {
            String p = readString(vd, "profession", "");
            int idx = professionIndexById(p);
            if (idx >= 0) professionIndex = idx;
            villagerLevel = Math.max(1, Math.min(5, readInt(vd, "level", villagerLevel)));
            villagerType = readString(vd, "type", villagerType);
        }
        normalizeProfessionState();

        trades.clear();
        ListTag recipes = extractOfferRecipes(root);
        if (recipes != null && !recipes.isEmpty()) {
            DebugLog.info("Villager offer recipes detected: {}", recipes.size());
            applyRecipesToTrades(recipes);
        }
        if (recipes == null || recipes.isEmpty()) {
            DebugLog.warn("Villager offers missing or incompatible on target: {}", targetEntity == null ? "spawn_egg" : targetEntity.getUUID());
        }
        if (trades.isEmpty()) trades.add(TradeData.defaults());
        normalizeProfessionState();
    }

    private CompoundTag readEntityTag(Entity entity) {
        if (entity == null) return null;
        CompoundTag saved = invokeCompoundArg(entity, "saveWithoutId", new CompoundTag());
        if (saved != null && !saved.isEmpty()) return saved;
        saved = invokeCompoundArg(entity, "save", new CompoundTag());
        if (saved != null && !saved.isEmpty()) return saved;
        saved = invokeCompoundArg(entity, "saveAsPassenger", new CompoundTag());
        if (saved != null && !saved.isEmpty()) return saved;
        return null;
    }

    private CompoundTag readCompound(CompoundTag parent, String key) {
        if (parent == null) return null;
        try {
            Object out = parent.getClass().getMethod("getCompound", String.class).invoke(parent, key);
            if (out instanceof CompoundTag ct) return ct;
            if (out instanceof java.util.Optional<?> opt && opt.orElse(null) instanceof CompoundTag ct) return ct;
        } catch (Throwable ignored) {}
        Object raw = readTag(parent, key);
        return raw instanceof CompoundTag ct ? ct : null;
    }

    private String readString(CompoundTag parent, String key, String def) {
        if (parent == null) return def;
        try {
            Object out = parent.getClass().getMethod("getString", String.class).invoke(parent, key);
            if (out instanceof String s) return s;
            if (out instanceof java.util.Optional<?> opt && opt.orElse(null) instanceof String s) return s;
        } catch (Throwable ignored) {}
        Object raw = readTag(parent, key);
        if (raw != null) {
            try {
                Object s = raw.getClass().getMethod("getAsString").invoke(raw);
                if (s instanceof String str) return str;
            } catch (Throwable ignored) {}
        }
        return def;
    }

    private int readInt(CompoundTag parent, String key, int def) {
        if (parent == null) return def;
        try {
            Object out = parent.getClass().getMethod("getInt", String.class).invoke(parent, key);
            if (out instanceof Integer i) return i;
            if (out instanceof java.util.Optional<?> opt && opt.orElse(null) instanceof Integer i) return i;
        } catch (Throwable ignored) {}
        Object raw = readTag(parent, key);
        if (raw != null) {
            try {
                Object n = raw.getClass().getMethod("getAsInt").invoke(raw);
                if (n instanceof Integer i) return i;
            } catch (Throwable ignored) {}
        }
        return def;
    }

    private float readFloat(CompoundTag parent, String key, float def) {
        if (parent == null) return def;
        try {
            Object out = parent.getClass().getMethod("getFloat", String.class).invoke(parent, key);
            if (out instanceof Float f) return f;
            if (out instanceof Number n) return n.floatValue();
            if (out instanceof java.util.Optional<?> opt && opt.orElse(null) instanceof Number n) return n.floatValue();
        } catch (Throwable ignored) {}
        Object raw = readTag(parent, key);
        if (raw != null) {
            try {
                Object n = raw.getClass().getMethod("getAsFloat").invoke(raw);
                if (n instanceof Number number) return number.floatValue();
            } catch (Throwable ignored) {}
        }
        return def;
    }

    private boolean readBoolean(CompoundTag parent, String key, boolean def) {
        if (parent == null) return def;
        try {
            Object out = parent.getClass().getMethod("getBoolean", String.class).invoke(parent, key);
            if (out instanceof Boolean b) return b;
            if (out instanceof java.util.Optional<?> opt && opt.orElse(null) instanceof Boolean b) return b;
        } catch (Throwable ignored) {}
        Object raw = readTag(parent, key);
        if (raw != null) {
            try {
                Object b = raw.getClass().getMethod("getAsBoolean").invoke(raw);
                if (b instanceof Boolean value) return value;
            } catch (Throwable ignored) {}
        }
        return def;
    }

    private Object readTag(CompoundTag parent, String key) {
        try {
            Object out = parent.getClass().getMethod("get", String.class).invoke(parent, key);
            return unwrapOptional(out);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object unwrapOptional(Object value) {
        Object out = value;
        while (out instanceof java.util.Optional<?> opt) {
            out = opt.orElse(null);
        }
        return out;
    }

    private CompoundTag readRecipeItem(CompoundTag recipe, String... keys) {
        if (recipe == null || keys == null) return null;
        for (String key : keys) {
            CompoundTag item = readCompound(recipe, key);
            if (item != null && !item.isEmpty()) return item;
        }
        return null;
    }

    private CompoundTag readStackComponents(CompoundTag stackTag) {
        CompoundTag components = readCompound(stackTag, "components");
        if (components != null && !components.isEmpty()) return copyCompound(components);
        CompoundTag legacyTag = readCompound(stackTag, "tag");
        if (legacyTag != null && !legacyTag.isEmpty()) {
            CompoundTag wrapped = new CompoundTag();
            wrapped.put("minecraft:custom_data", copyCompound(legacyTag));
            return wrapped;
        }
        return null;
    }

    private void applyRecipesToTrades(ListTag recipes) {
        if (recipes == null || recipes.isEmpty()) return;
        for (int i = 0; i < recipes.size(); i++) {
            Object entry = unwrapOptional(recipes.get(i));
            if (!(entry instanceof CompoundTag recipe)) continue;
            TradeData t = tradeFromRecipe(recipe);
            trades.add(t);
        }
        if (!trades.isEmpty()) rewardExp = trades.get(0).rewardExp;
    }

    private TradeData tradeFromMerchantOffer(MerchantOffer offer) {
        TradeData t = TradeData.defaults();
        if (offer == null) return t;

        ItemStack buy = offer.getBaseCostA();
        ItemStack buyB = offer.getCostB();
        ItemStack sell = offer.getResult();

        if (buy != null && !buy.isEmpty()) {
            t.buyId = SpawnEggEditorHelper.getItemId(buy);
            t.buyCount = Math.max(1, buy.getCount());
            t.buyComponents = readItemComponents(buy);
        }
        if (buyB != null && !buyB.isEmpty()) {
            t.buy2Id = SpawnEggEditorHelper.getItemId(buyB);
            t.buy2Count = Math.max(1, buyB.getCount());
            t.buy2Components = readItemComponents(buyB);
        } else {
            t.buy2Id = "";
            t.buy2Count = 1;
            t.buy2Components = null;
        }
        if (sell != null && !sell.isEmpty()) {
            t.sellId = SpawnEggEditorHelper.getItemId(sell);
            t.sellCount = Math.max(1, sell.getCount());
            t.sellComponents = readItemComponents(sell);
        }

        t.maxUses = Math.max(1, offer.getMaxUses());
        t.xp = Math.max(0, offer.getXp());
        t.uses = Math.max(0, offer.getUses());
        t.specialPrice = offer.getSpecialPriceDiff();
        t.demand = offer.getDemand();
        t.priceMultiplier = offer.getPriceMultiplier();
        t.rewardExp = offer.shouldRewardExp();
        t.recipeTemplate = merchantOfferToTag(offer);
        return t;
    }

    private TradeData tradeFromRecipe(CompoundTag recipe) {
        TradeData t = TradeData.defaults();
        t.recipeTemplate = copyCompound(recipe);
        CompoundTag buy = readRecipeItem(recipe, "buy", "base_cost_a", "itemA", "input", "costA");
        CompoundTag buyB = readRecipeItem(recipe, "buyB", "cost_b", "itemB", "inputB", "costB");
        CompoundTag sell = readRecipeItem(recipe, "sell", "result", "output", "itemOut");
        if (buy != null) {
            t.buyId = readString(buy, "id", t.buyId);
            t.buyCount = Math.max(1, readInt(buy, "count", t.buyCount));
            t.buyComponents = readStackComponents(buy);
        }
        if (buyB != null) {
            t.buy2Id = readString(buyB, "id", t.buy2Id);
            t.buy2Count = Math.max(1, readInt(buyB, "count", t.buy2Count));
            t.buy2Components = readStackComponents(buyB);
        }
        if (sell != null) {
            t.sellId = readString(sell, "id", t.sellId);
            t.sellCount = Math.max(1, readInt(sell, "count", t.sellCount));
            t.sellComponents = readStackComponents(sell);
        }
        t.maxUses = Math.max(1, readInt(recipe, "maxUses", t.maxUses));
        t.xp = Math.max(0, readInt(recipe, "xp", t.xp));
        t.uses = Math.max(0, readInt(recipe, "uses", t.uses));
        t.specialPrice = readInt(recipe, "specialPrice", t.specialPrice);
        t.demand = readInt(recipe, "demand", t.demand);
        t.priceMultiplier = readFloat(recipe, "priceMultiplier", t.priceMultiplier);
        t.rewardExp = readBoolean(recipe, "rewardExp", t.rewardExp);
        return t;
    }

    private int professionIndexById(String id) {
        if (id == null) return -1;
        for (int i = 0; i < PROFESSIONS.length; i++) {
            if (id.equals(PROFESSIONS[i])) return i;
        }
        return -1;
    }

    private void cycleProfession() {
        pushUndo();
        syncCurrentTrade(false);
        if (isWanderingTraderContext()) {
            professionIndex = 0;
            dirty = true;
            return;
        }
        int start = professionIndex;
        do {
            professionIndex++;
            if (professionIndex >= PROFESSIONS.length) professionIndex = defaultProfessionIndex();
        } while (!isTradeableProfession(PROFESSIONS[professionIndex]) && professionIndex != start);
        if (!isTradeableProfession(PROFESSIONS[professionIndex])) {
            professionIndex = defaultProfessionIndex();
        }
        dirty = true;
    }

    private void cycleLevel() {
        pushUndo();
        syncCurrentTrade(false);
        villagerLevel++;
        if (villagerLevel > 5) villagerLevel = 1;
        dirty = true;
    }

    private void toggleRewardExp() {
        pushUndo();
        rewardExp = !rewardExp;
        syncCurrentTrade(false);
        dirty = true;
    }

    private String professionLabel() {
        String id = normalizeProfessionId(PROFESSIONS[professionIndex]);
        if (id.isBlank()) return Component.translatable("ankinbt.villager.profession.none").getString();
        int idx = id.indexOf(':');
        return idx >= 0 ? id.substring(idx + 1) : id;
    }

    private boolean isTradeableProfession(String id) {
        if (id == null || id.isBlank()) return false;
        return !id.endsWith("nitwit") && !id.endsWith("unemployed");
    }

    private boolean isWanderingTraderContext() {
        if (targetEntity != null) {
            String type = targetEntity.getType().toString().toLowerCase(Locale.ROOT);
            return type.contains("wandering_trader");
        }
        if (!sourceStack.isEmpty()) {
            String id = SpawnEggEditorHelper.getItemId(sourceStack).toLowerCase(Locale.ROOT);
            return id.contains("wandering_trader_spawn_egg");
        }
        return false;
    }

    private void applyTrade() {
        if (!syncCurrentTrade(true)) {
            setStatus(Component.translatable("ankinbt.simple.invalid_number"), TXT_ERR);
            return;
        }
        ensureTrades();

        boolean wandering = isWanderingTraderContext();
        String profession = normalizeProfessionId(PROFESSIONS[professionIndex]);
        professionIndex = normalizeProfessionIndex(professionIndex);

        ListTag recipes = new ListTag();
        for (int tradeNumber = 0; tradeNumber < trades.size(); tradeNumber++) {
            TradeData t = trades.get(tradeNumber);
            String invalidField = invalidItemField(t);
            if (invalidField != null) {
                tradeIndex = tradeNumber;
                loadTradeToForm(tradeIndex);
                ensureTradeVisible();
                setStatus(Component.translatable("ankinbt.villager.invalid_trade", tradeNumber + 1, tr(invalidField)), TXT_ERR);
                return;
            }
            CompoundTag buyTag = buildTradeStackTag(t.buyId, t.buyCount, t.buyComponents);
            CompoundTag sellTag = buildTradeStackTag(t.sellId, t.sellCount, t.sellComponents);

            CompoundTag recipe = t.recipeTemplate == null ? new CompoundTag() : copyCompound(t.recipeTemplate);
            recipe.put("buy", buyTag);
            recipe.put("base_cost_a", copyCompound(buyTag));
            if (!t.buy2Id.isEmpty()) {
                CompoundTag buyB = buildTradeStackTag(t.buy2Id, t.buy2Count, t.buy2Components);
                recipe.put("buyB", buyB);
                recipe.put("cost_b", copyCompound(buyB));
            } else {
                recipe.remove("buyB");
                recipe.remove("cost_b");
                recipe.remove("itemB");
                recipe.remove("inputB");
                recipe.remove("costB");
            }
            recipe.put("sell", sellTag);
            recipe.put("result", copyCompound(sellTag));
            recipe.putInt("maxUses", Math.max(1, t.maxUses));
            recipe.putInt("uses", Math.max(0, t.uses));
            recipe.putInt("xp", Math.max(0, t.xp));
            recipe.putInt("specialPrice", t.specialPrice);
            recipe.putInt("demand", t.demand);
            recipe.putFloat("priceMultiplier", t.priceMultiplier);
            recipe.putBoolean("rewardExp", t.rewardExp);
            t.recipeTemplate = copyCompound(recipe);
            recipes.add(recipe);
        }

        CompoundTag offers = new CompoundTag();
        offers.put("Recipes", recipes);
        offers.put("recipes", copyListTag(recipes));

        CompoundTag patch = new CompoundTag();
        patch.put("Offers", offers);

        if (!wandering) {
            CompoundTag villagerData = new CompoundTag();
            villagerData.putString("type", villagerType == null || villagerType.isBlank() ? "minecraft:plains" : villagerType);
            villagerData.putString("profession", profession);
            villagerData.putInt("level", Math.max(1, Math.min(5, villagerLevel)));
            patch.put("VillagerData", villagerData);
            patch.putInt("Xp", Math.max(0, villagerLevel * 10));
        }

        Minecraft mc = Minecraft.getInstance();
        if (targetEntity != null) {
            if (mc.player == null) return;
            if (applyTradeToIntegratedServer(mc, patch)) {
                ENTITY_PATCH_CACHE.put(targetEntity.getUUID(), copyCompound(patch));
                applyTradePreviewToClient();
                commitBaseline();
                setStatus(Component.translatable("ankinbt.entity.applied"), TXT_OK);
                return;
            }
            if (!EditorCommandHelper.canUseEntityCommand(mc)) {
                setStatus(Component.translatable("ankinbt.entity.admin_required"), TXT_ERR);
                return;
            }
            boolean ok = EditorCommandHelper.applyMergeToEntity(mc, targetEntity, patch);
            setStatus(ok ? Component.translatable("ankinbt.entity.applied") : Component.translatable("ankinbt.status.save_error"), ok ? TXT_OK : TXT_ERR);
            if (ok) {
                ENTITY_PATCH_CACHE.put(targetEntity.getUUID(), copyCompound(patch));
                applyTradePreviewToClient();
                commitBaseline();
            }
            return;
        }

        if (!SpawnEggEditorHelper.isVillagerSpawnEgg(sourceStack)) {
            setStatus(Component.translatable("ankinbt.villager.spawn_egg_required"), TXT_ERR);
            return;
        }

        patch.putString("id", wandering ? "minecraft:wandering_trader" : "minecraft:villager");
        var patched = SpawnEggEditorHelper.withMergedEntityData(sourceStack, patch);
        if (patched.isEmpty()) {
            setStatus(Component.translatable("ankinbt.status.save_error"), TXT_ERR);
            return;
        }
        if (!SpawnEggEditorHelper.saveToCreativeSlot(mc, patched.get(), inventorySlot)) {
            setStatus(Component.translatable("ankinbt.status.creative_only"), TXT_ERR);
            return;
        }
        setStatus(Component.translatable("ankinbt.entity.applied"), TXT_OK);
        commitBaseline();
    }

    private boolean isLikelyItemId(String id) {
        return !id.isBlank() && id.contains(":") && id.indexOf(':') > 0 && id.indexOf(':') < id.length() - 1;
    }

    private String invalidItemField(TradeData trade) {
        if (trade == null || !isRegisteredItem(trade.buyId)) return "ankinbt.villager.buy_item";
        if (!trade.buy2Id.isBlank() && !isRegisteredItem(trade.buy2Id)) return "ankinbt.villager.buy2_item";
        if (!isRegisteredItem(trade.sellId)) return "ankinbt.villager.sell_item";
        return null;
    }

    private boolean isRegisteredItem(String id) {
        Item item = isLikelyItemId(id) ? resolveItem(id) : null;
        return item != null && item != Items.AIR;
    }

    private void commitBaseline() {
        dirty = false;
        baselineSnapshot = captureState();
        undoStack.clear();
    }

    private Integer parseInt(String in, int def) {
        String t = in == null ? "" : in.trim();
        if (t.isEmpty()) return def;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setStatus(Component msg, int color) {
        status = msg;
        statusColor = color;
        statusTime = System.currentTimeMillis();
    }

    public boolean mouseClicked(double mx, double my, int button) {
        recalcBounds();
        if (confirmClose || confirmReset) return clickConfirm((int) mx, (int) my);
        if (invPickTarget != InvPickTarget.NONE) return clickInventoryOverlay((int) mx, (int) my, button);
        if (button == 0 && EditorBrandLayer.isSettingsButton(mx, my, width)) {
            captureDraftFromBoxes();
            Minecraft.getInstance().setScreen(new AnkiConfigScreen(this));
            return true;
        }
        if (button == 0) editorSizeFocused = false;
        if (button == 0 || button == 1) {
            EditorDock.SizeControl sizeControl = EditorDock.sizeControl(width, height, editorScale);
            if (invPickTarget == InvPickTarget.NONE && button == 0 && sizeControl.reset().contains(mx, my)) {
                resetEditorSize();
                UiSound.playClick();
                return true;
            }
            if (invPickTarget == InvPickTarget.NONE && sizeControl.horizontal().contains(mx, my)) {
                adjustEditorAxes(button == 1 ? EditorDock.AXIS_ADJUSTMENT_STEP
                        : -EditorDock.AXIS_ADJUSTMENT_STEP, 0.0f);
                UiSound.playClick();
                return true;
            }
            if (invPickTarget == InvPickTarget.NONE && sizeControl.vertical().contains(mx, my)) {
                adjustEditorAxes(0.0f, button == 1 ? EditorDock.AXIS_ADJUSTMENT_STEP
                        : -EditorDock.AXIS_ADJUSTMENT_STEP);
                UiSound.playClick();
                return true;
            }
            if (invPickTarget == InvPickTarget.NONE && button == 0 && sizeControl.hit().contains(mx, my)) {
                resizingEditor = true;
                editorSizeFocused = true;
                draggingPanel = false;
                unfocusEditBoxes();
                updateEditorScale(mx);
                return true;
            }
        }
        if (menuBounds() != null && menuBounds().contains(mx, my)) return handleMenuBarClick(mx, my, button);
        if (!drawerOpen || drawerAnim < 0.92f || contentAnim < 0.92f
                || drawerBounds == null || !drawerBounds.contains(mx, my)) {
            unfocusEditBoxes();
            return true;
        }
        if (handleTradeStripClick(mx, my, button)) return true;
        if (my < bodyTop() || my >= bodyBottom() || contentAnim < 0.75f) return true;
        if (handleEditBoxClick(mx, my, button)) return true;

        if (button == 0 || button == 1) {
            for (IconHit hit : iconHits) {
                if (hit.hit((int) mx, (int) my)) {
                    EditBox box = boxForTarget(hit.target);
                    if (box == null) return true;
                    if (button == 0) openPickerFor(hit.target);
                    else openInventoryPicker(hit.target);
                    return true;
                }
            }
        }

        if (button == 0) {
            for (UiBtn btn : buttons) {
                if (btn.click((int) mx, (int) my)) {
                    rebuildButtons();
                    return true;
                }
            }
        }
        unfocusEditBoxes();
        return true;
    }

    private boolean handleMenuBarClick(double mx, double my, int button) {
        EditorDock.Bounds menu = menuBounds();
        if (button != 0 || menu == null || !menu.contains(mx, my)) return false;
        int brandW = menuBrandWidth();
        if (mx < menu.x() + brandW) {
            draggingPanel = true;
            panelDragOffsetX = (int) Math.round(mx) - (barBounds == null ? menu.x() : barBounds.x());
            panelDragOffsetY = (int) Math.round(my) - (barBounds == null ? menu.y() : barBounds.y());
            return true;
        }
        int toolW = menuToolWidth();
        int toolsStart = menuToolsStart();
        if (mx >= toolsStart) {
            if (mx < toolsStart + toolW) {
                captureDraftFromBoxes();
                modalAnim = 0f;
                confirmReset = true;
            } else if (mx < toolsStart + toolW * 2) {
                applyTrade();
            } else {
                tryClose();
            }
            return true;
        }
        int index = menuTabAt(mx, my);
        if (index >= 0 && index < VillagerTab.values().length) {
            selectVillagerTab(VillagerTab.values()[index]);
            return true;
        }
        return true;
    }

    private void switchTab(VillagerTab next) {
        if (next == null) return;
        if (next == VillagerTab.ENTITY) {
            openEntityEditor();
            return;
        }
        if (next == activeTab) return;
        // Keep an unfinished draft when moving between pages. Validation belongs to Apply,
        // otherwise a partially typed field would make the remaining editor pages unreachable.
        captureDraftFromBoxes();
        syncCurrentTrade(false);
        UiSound.playClick();
        activeTab = next;
        drawerOpen = true;
        contentScroll = 0;
        contentAnim = AnkiConfig.isUiAnimationEnabled() ? 0f : 1f;
        unfocusEditBoxes();
        layoutTradeFields();
        rebuildButtons();
    }

    public boolean keyPressed(int key, int scan, int mod) {
        if (confirmClose || confirmReset) {
            if (key == 256) {
                confirmClose = false;
                confirmReset = false;
            }
            return true;
        }
        if (editorSizeFocused) {
            if (key == 263) { adjustEditorScale(-0.05f); return true; }
            if (key == 262) { adjustEditorScale(0.05f); return true; }
            if (key == 268) { setEditorScale(0.0f, true); return true; }
            if (key == 269) { setEditorScale(1.0f, true); return true; }
            if (key == 256) { editorSizeFocused = false; return true; }
        }
        if (dragTradeIndex >= 0 && key == 256) {
            resetTradeDrag();
            return true;
        }
        boolean ctrl = (mod & 2) != 0;
        if (ctrl && key == 90) {
            undo();
            return true;
        }
        if (handleEditBoxKey(key, scan, mod)) return true;
        if ((key == 263 || key == 262) && !hasFocusedTradeBox()) {
            VillagerTab[] tabs = VillagerTab.values();
            int next = Math.floorMod(activeTab.ordinal() + (key == 263 ? -1 : 1), tabs.length);
            switchTab(tabs[next]);
            return true;
        }
        if (key == 256) {
            tryClose();
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (handleEditBoxChar(codePoint, modifiers)) return true;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        recalcBounds();
        if (drawerOpen && drawerAnim > 0.2f && mx >= tradeStripLeft() && mx < tradeStripRight()
                && my >= tradeStripY() && my < tradeStripY() + TRADE_CARD_HEIGHT) {
            int step = (int) Math.signum(sy);
            if (step != 0) {
                tradeNavScroll = Math.max(0, Math.min(tradeNavScrollMax,
                        tradeNavScroll - step * (TRADE_CARD_WIDTH + TRADE_CARD_GAP)));
            }
            return true;
        }
        if (drawerOpen && drawerAnim > 0.2f && drawerBounds != null
                && mx >= contentLeft() && mx < contentLeft() + contentWidth()
                && my >= bodyTop() && my < bodyBottom() && contentScrollMax > 0) {
            int step = (int) Math.signum(sy);
            if (step != 0) {
                contentScroll = Math.max(0, Math.min(contentScrollMax,
                        contentScroll - step * (contentRowHeight() + contentGap())));
                layoutTradeFields();
                rebuildButtons();
            }
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        pendingTooltip = null;
        pendingItemTooltip = null;
        recalcBounds();
        float cfgSpeed = AnkiConfig.getUiAnimationSpeed();
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.06f, Math.min(0.16f, cfgSpeed)) : 1.0f;
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);
        float motionSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.10f, cfgSpeed * 1.7f) : 1.0f;
        drawerAnim = UiTheme.approach(drawerAnim, drawerOpen ? 1f : 0f, motionSpeed);
        contentAnim = UiTheme.approach(contentAnim, 1f, motionSpeed);
        brandAnim = EditorBrandLayer.approachOpen(brandAnim);
        settingsHoverAnim = EditorBrandLayer.approachSettingsHover(settingsHoverAnim,
                EditorBrandLayer.isSettingsButton(mx, my, width));
        tradeNavScrollVisual = UiTheme.approach(tradeNavScrollVisual, tradeNavScroll,
                AnkiConfig.isUiAnimationEnabled() ? Math.min(0.65f, speed * 0.85f) : 1.0f);
        tradeSelectionAnim = UiTheme.approach(tradeSelectionAnim, 1.0f,
                AnkiConfig.isUiAnimationEnabled() ? Math.min(0.55f, speed * 0.75f) : 1.0f);
        modalAnim = UiTheme.approach(modalAnim, confirmClose || confirmReset ? 1.0f : 0.0f,
                Math.min(1.0f, motionSpeed * 1.8f));
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());

        EditorBrandLayer.renderBackgroundLogo(g, width, height);
        g.fill(0, 0, width, height, UiTheme.scrim(AnkiConfig.getUiOpacity(), openAnim));
        renderDrawer(g, mx, my);
        renderMenuBar(g, mx, my);
        if (!confirmClose && !confirmReset && invPickTarget == InvPickTarget.NONE) {
            sizeControlHoverAnim = EditorDock.renderSizeControl(g, font, width, height, mx, my,
                    editorScale, sizeControlHoverAnim, resizingEditor || editorSizeFocused, accent);
        }
        EditorBrandLayer.renderStatus(g, font, width, height, brandAnim,
                tr("ankinbt.villager.status_mode"), editorScale,
                editorWidthAdjustment, editorHeightAdjustment);
        EditorBrandLayer.renderSettingsButton(g, font, width, mx, my, settingsHoverAnim);
        renderInventoryOverlay(g, mx, my, accent);

        if (confirmReset) {
            renderConfirm(g, mx, my, true);
        } else if (confirmClose) {
            renderUnsavedConfirmLikeSimple(g, mx, my);
        } else {
            renderPendingTooltip(g);
        }
    }

    private void requestTooltip(Component tooltip, int mx, int my) {
        if (tooltip == null) return;
        pendingTooltip = tooltip;
        pendingItemTooltip = null;
        pendingTooltipX = mx;
        pendingTooltipY = my;
    }

    private void requestItemTooltip(ItemStack stack, int mx, int my) {
        if (stack == null || stack.isEmpty()) return;
        pendingItemTooltip = stack.copy();
        pendingTooltip = null;
        pendingTooltipX = mx;
        pendingTooltipY = my;
    }

    private void renderPendingTooltip(GuiGraphics g) {
        if (pendingItemTooltip != null && !pendingItemTooltip.isEmpty()) {
            VersionCompat.get().renderItemTooltip(g, font, pendingItemTooltip, pendingTooltipX, pendingTooltipY);
        } else if (pendingTooltip != null) {
            VersionCompat.get().renderTooltip(g, font, pendingTooltip,
                    pendingTooltipX, pendingTooltipY);
        }
    }

    private void renderMenuBar(GuiGraphics g, int mx, int my) {
        EditorDock.Bounds menu = menuBounds();
        if (menu == null) return;
        int bx = menu.x();
        int by = menu.y();
        int bw = menu.width();
        int bh = menu.height();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(bx, by, bx + bw, by + bh, UiTheme.toolbar(AnkiConfig.getUiOpacity(), openAnim));
        border(g, bx, by, bw, bh, UiTheme.themedBorder(AnkiConfig.getUiOpacity(), openAnim));
        g.fill(bx, by + bh - 1, bx + bw, by + bh, UiTheme.withAlpha(accent & 0x00FFFFFF,
                Math.round(255f * openAnim)));

        int brandW = menuBrandWidth();
        boolean moveHover = mx >= bx && mx < bx + brandW && my >= by && my < by + bh;
        Component moveIcon = UiIcons.component(UiIcons.MOVE);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, moveIcon,
                bx + (brandW - font.width(moveIcon)) / 2, by + 10,
                moveHover ? UiTheme.textMain() : UiTheme.textDim(), false);
        if (moveHover && !draggingPanel) {
            requestTooltip(Component.translatable("ankinbt.ui.drag_editor"), mx, my);
        }
        if (dirty) g.fill(bx + brandW - 6, by + 7, bx + brandW - 3, by + 10, TXT_ERR);

        int toolW = menuToolWidth();
        int toolsStart = menuToolsStart();
        float hoverSpeed = AnkiConfig.isUiAnimationEnabled()
                ? Math.max(0.18f, AnkiConfig.getUiAnimationSpeed() * 2.4f) : 1f;
        for (int i = 0; i < VillagerTab.values().length; i++) {
            VillagerTab tab = VillagerTab.values()[i];
            int tx = menuTabX(i);
            int tabW = menuTabWidth(i);
            boolean hover = mx >= tx && mx < tx + tabW && my >= by && my < by + bh;
            boolean active = tab == activeTab;
            int tabTextColor = active || hover ? UiTheme.textMain()
                    : tab == VillagerTab.ENTITY ? accent : UiTheme.textDim();
            tabHoverAnim[i] = UiTheme.approach(tabHoverAnim[i], hover ? 1f : 0f, hoverSpeed);
            if (active && "segmented".equals(AnkiConfig.getUiNavigationStyle())) {
                g.fill(tx + 2, by + 3, tx + tabW - 2, by + bh - 3,
                        UiTheme.withAlpha(accent & 0x00FFFFFF, 86));
            } else if (active && "underline".equals(AnkiConfig.getUiNavigationStyle())) {
                g.fill(tx + 2, by + 3, tx + tabW - 2, by + bh - 3,
                        UiTheme.withAlpha(accent & 0x00FFFFFF, 24));
            } else if (tabHoverAnim[i] > 0.01f) {
                g.fill(tx + 1, by + 2, tx + tabW - 1, by + bh - 2,
                        UiTheme.mix(0x00000000, 0x4A334155, tabHoverAnim[i]));
            }
            if (i > 0 && tabW > 4) {
                g.fill(tx, by + 7, tx + 1, by + bh - 7,
                        UiTheme.withAlpha(UiTheme.themedBorder(1f, 1f) & 0x00FFFFFF, 72));
            }
            if (active && "compact".equals(AnkiConfig.getUiNavigationStyle())) {
                g.fill(tx + 3, by + 7, tx + 5, by + bh - 7, accent);
            }
            Component icon = UiIcons.component(tab.icon);
            String label = tr(tab.translationKey);
            int total = font.width(icon) + 4 + font.width(label);
            if (tabW >= total + 8) {
                int start = tx + (tabW - total) / 2;
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, icon, start, by + 10,
                        tabTextColor, false);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, label,
                        start + font.width(icon) + 4, by + 10,
                        tabTextColor, false);
            } else {
                int labelWidth = Math.max(0, tabW - font.width(icon) - 12);
                if (labelWidth >= font.width("..")) {
                    String compactLabel = trimToWidth(label, labelWidth);
                    int compactTotal = font.width(icon) + 4 + font.width(compactLabel);
                    int start = tx + Math.max(2, (tabW - compactTotal) / 2);
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, icon, start, by + 10,
                            tabTextColor, false);
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, compactLabel,
                            start + font.width(icon) + 4, by + 10,
                            tabTextColor, false);
                } else {
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font, icon,
                            tx + (tabW - font.width(icon)) / 2, by + 10,
                            tabTextColor, false);
                }
                if (hover && tab != VillagerTab.ENTITY) {
                    requestTooltip(Component.literal(label), mx, my);
                }
            }
            if (hover && tab == VillagerTab.ENTITY) {
                requestTooltip(Component.translatable("key.ankinbt.open_entity_editor"), mx, my);
            }
        }
        int activeX = menuTabX(activeTab.ordinal());
        int activeW = menuTabWidth(activeTab.ordinal());
        if (activeIndicatorX < 0f) activeIndicatorX = activeX;
        activeIndicatorX = UiTheme.approach(activeIndicatorX, activeX, hoverSpeed);
        if ("underline".equals(AnkiConfig.getUiNavigationStyle())) {
            int indicatorLeft = Math.round(activeIndicatorX) + (activeW > 10 ? 5 : 0);
            int indicatorRight = Math.round(activeIndicatorX) + (activeW > 10 ? activeW - 5 : activeW);
            if (indicatorRight > indicatorLeft) {
                g.fill(indicatorLeft, by + bh - 3, indicatorRight, by + bh - 1, accent);
            }
        }

        renderMenuTool(g, mx, my, toolsStart, by, toolW, 0, UiIcons.RESET,
                Component.translatable("ankinbt.entity.reset_changes"));
        renderMenuTool(g, mx, my, toolsStart + toolW, by, toolW, 1, UiIcons.SAVE,
                Component.translatable("ankinbt.entity.apply_patch"));
        renderMenuTool(g, mx, my, toolsStart + toolW * 2, by, toolW, 2, UiIcons.CLOSE,
                Component.translatable("ankinbt.btn.close"));
    }

    private void renderMenuTool(GuiGraphics g, int mx, int my, int x, int y, int w, int index,
                                String glyph, Component tooltip) {
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + EditorDock.MENU_BAR_HEIGHT;
        float speed = AnkiConfig.isUiAnimationEnabled()
                ? Math.max(0.18f, AnkiConfig.getUiAnimationSpeed() * 2.4f) : 1f;
        menuToolHoverAnim[index] = UiTheme.approach(menuToolHoverAnim[index], hover ? 1f : 0f, speed);
        g.fill(x, y + 2, x + w, y + EditorDock.MENU_BAR_HEIGHT - 2,
                UiTheme.mix(0x00000000, 0x4A334155, menuToolHoverAnim[index]));
        Component icon = UiIcons.component(glyph);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, icon,
                x + (w - font.width(icon)) / 2, y + 10,
                hover ? UiTheme.textMain() : UiTheme.textDim(), false);
        if (hover) requestTooltip(tooltip, mx, my);
    }

    private void renderDrawer(GuiGraphics g, int mx, int my) {
        if (drawerAnim <= 0.01f || drawerBounds == null) {
            tradeCardHits.clear();
            iconHits.clear();
            return;
        }
        int reveal = Math.max(1, Math.round(ph * drawerAnim));
        int clipTop = drawerAbove ? py + ph - reveal : py;
        int clipBottom = drawerAbove ? py + ph : py + reveal;
        g.enableScissor(px, clipTop, px + pw, clipBottom);
        g.fill(px, py, px + pw, py + ph, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
        border(g, px, py, pw, ph, UiTheme.themedBorder(AnkiConfig.getUiOpacity(), openAnim));

        g.fill(px + 8, py + 29, px + pw - 8, py + 30, UiTheme.themedBorder(1f, 1f));

        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        int card = UiTheme.card(AnkiConfig.getUiOpacity(), openAnim);
        int edge = UiTheme.themedBorder(AnkiConfig.getUiOpacity(), openAnim);
        renderTradeStrip(g, mx, my, accent, card, edge);

        layoutTradeFields();
        iconHits.clear();
        int bodyClipTop = Math.max(clipTop, bodyTop());
        int bodyClipBottom = Math.min(clipBottom, bodyBottom());
        if (bodyClipBottom > bodyClipTop) {
            g.enableScissor(px + 1, bodyClipTop, px + pw - 1, bodyClipBottom);
            renderTabContent(g, mx, my, accent);
            renderContentScrollBar(g, accent);
            g.disableScissor();
        }
        int footerY = py + ph - 26;
        g.fill(px + 1, footerY, px + pw - 1, footerY + 1, UiTheme.themedBorder(1f, 1f));
        Component footer = status != null && !status.getString().isEmpty()
                && System.currentTimeMillis() - statusTime < 2600
                ? status : Component.translatable("ankinbt.villager.footer_ready");
        int footerColor = footer == status ? statusColor : UiTheme.textDim();
        com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                trimToWidth(footer.getString(), Math.max(40, pw - 20)), px + 10, footerY + 9,
                footerColor, false);
        g.disableScissor();
        layoutTradeFields();

    }

    private void renderTabContent(GuiGraphics g, int mx, int my, int accent) {
        int x = contentLeft();
        int y = contentBaseY();
        int w = contentWidth();
        if (activeTab == VillagerTab.TRADE) {
            int summaryH = tradeSummaryHeight();
            int rowH = contentRowHeight();
            renderOptionSurface(g, x, y, w, summaryH, mx, my, true);
            renderTradeIcons(g, mx, my, x + 10, y + (summaryH - 18) / 2, accent);
            String label = tr("ankinbt.villager.trade_detail") + "  " + (tradeIndex + 1) + " / " + trades.size();
            int labelX = x + 150;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                    trimToWidth(label, Math.max(36, x + w - 10 - labelX)), labelX,
                    centeredTextY(y, summaryH), accent, false);

            renderTradeValueRow(g, mx, my, x, tradeMaxUsesRowY(), w, rowH,
                    tr("ankinbt.villager.max_uses"), maxUses, accent);
            renderTradeValueRow(g, mx, my, x, tradeXpRowY(), w, rowH,
                    tr("ankinbt.villager.xp"), xp, accent);
        } else if (activeTab == VillagerTab.BUY) {
            renderItemPanel(g, mx, my, x, y, w, buyId, buyCount, InvPickTarget.BUY,
                    tr("ankinbt.villager.buy_item"), accent);
            renderItemPanel(g, mx, my, x, secondItemPanelY(), w, buy2Id, buy2Count, InvPickTarget.BUY2,
                    tr("ankinbt.villager.buy2_item"), accent);
        } else if (activeTab == VillagerTab.SELL) {
            renderItemPanel(g, mx, my, x, y, w, sellId, sellCount, InvPickTarget.SELL,
                    tr("ankinbt.villager.sell_item"), accent);
        }
        for (UiBtn btn : buttons) btn.render(g, font, mx, my, accent);
    }

    private void renderTradeValueRow(GuiGraphics g, int mx, int my, int x, int y, int w, int h,
                                     String label, EditBox box, int accent) {
        renderOptionSurface(g, x, y, w, h, mx, my, false);
        int labelX = x + 10;
        int labelWidth = Math.max(24, tradeValueColumnX() - labelX - 8);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                trimToWidth(label, labelWidth), labelX, centeredTextY(y, h), UiTheme.textDim(), false);
        renderInlineEditBox(g, box, mx, my, accent);
    }

    private void renderItemPanel(GuiGraphics g, int mx, int my, int x, int y, int w,
                                 EditBox idBox, EditBox countBox, InvPickTarget target,
                                 String heading, int accent) {
        int panelH = itemPanelHeight();
        renderOptionSurface(g, x, y, w, panelH, mx, my, false);
        TradeData live = previewTradeFromDraft(trades.get(tradeIndex));
        CompoundTag components = target == InvPickTarget.BUY ? live.buyComponents
                : target == InvPickTarget.BUY2 ? live.buy2Components : live.sellComponents;
        int count = target == InvPickTarget.BUY ? live.buyCount
                : target == InvPickTarget.BUY2 ? live.buy2Count : live.sellCount;
        int headerY = y + 4;
        int actionW = Math.max(58, Math.min(92, (w - 90) / 3));
        int actionX = x + w - actionW * 2 - contentGap() - 10;
        renderIconSlot(g, mx, my, x + 10, headerY + 1, idBox.getValue(), components, count, target, heading, accent);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                trimToWidth(heading, Math.max(24, actionX - (x + 36) - 6)), x + 36,
                centeredTextY(headerY, 20),
                UiTheme.textMain(), false);
        renderInlineEditBox(g, idBox, mx, my, accent);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, "x", countBox.getX() - 10,
                centeredTextY(countBox.getY(), countBox.getHeight()), UiTheme.textDim(), false);
        renderInlineEditBox(g, countBox, mx, my, accent);
    }

    private void renderOptionSurface(GuiGraphics g, int x, int y, int w, int h,
                                     int mx, int my, boolean selected) {
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        String style = AnkiConfig.getUiOptionStyle();
        int fill = "compact".equals(style)
                ? UiTheme.withAlpha(UiTheme.baseRgb(), hover ? 48 : 22)
                : UiTheme.card(AnkiConfig.getUiOpacity(), openAnim);
        if (selected) fill = UiTheme.withAlpha(UiTheme.accent(AnkiConfig.getUiAccentPreset()) & 0x00FFFFFF, 58);
        int edge = selected ? UiTheme.accent(AnkiConfig.getUiAccentPreset())
                : UiTheme.themedBorder(AnkiConfig.getUiOpacity(), 1f);
        g.fill(x, y, x + w, y + h, fill);
        if ("rows".equals(style)) g.fill(x, y + h - 1, x + w, y + h, edge);
        else if ("compact".equals(style)) g.fill(x, y, x + (hover ? 2 : 1), y + h, edge);
        else border(g, x, y, w, h, edge);
    }

    private String trimToWidth(String value, int maxWidth) {
        if (value == null) return "";
        if (font.width(value) <= maxWidth) return value;
        return font.plainSubstrByWidth(value, Math.max(4, maxWidth - font.width("..."))) + "...";
    }

    private int tradeStripLeft() {
        return px + 10;
    }

    private int tradeStripRight() {
        return Math.max(tradeStripLeft() + 72, px + pw - 151);
    }

    private int tradeStripY() {
        return py + 35;
    }

    private void renderTradeStrip(GuiGraphics g, int mx, int my, int accent, int cardColor, int borderColor) {
        ensureTrades();
        hoveredTradeTip = null;
        tradeCardHits.clear();

        int left = tradeStripLeft();
        int right = tradeStripRight();
        int y = tradeStripY();
        int viewWidth = Math.max(1, right - left);
        int contentWidth = trades.size() * (TRADE_CARD_WIDTH + TRADE_CARD_GAP) - TRADE_CARD_GAP;
        tradeNavScrollMax = Math.max(0, contentWidth - viewWidth);
        tradeNavScroll = Math.max(0, Math.min(tradeNavScroll, tradeNavScrollMax));
        tradeNavScrollVisual = Math.max(0f, Math.min(tradeNavScrollVisual, tradeNavScrollMax));

        g.fill(left, y, right, y + TRADE_CARD_HEIGHT, 0x30101927);
        border(g, left, y, viewWidth, TRADE_CARD_HEIGHT, borderColor);
        g.enableScissor(left + 1, y + 1, right - 1, y + TRADE_CARD_HEIGHT - 1);

        int contentX = left - Math.round(tradeNavScrollVisual);
        for (int i = 0; i < trades.size(); i++) {
            int x = contentX + i * (TRADE_CARD_WIDTH + TRADE_CARD_GAP);
            if (x + TRADE_CARD_WIDTH < left || x > right) continue;
            TradeData trade = i == tradeIndex ? previewTradeFromDraft(trades.get(i)) : trades.get(i);
            boolean selected = i == tradeIndex;
            boolean hover = mx >= x && mx < x + TRADE_CARD_WIDTH && my >= y && my < y + TRADE_CARD_HEIGHT;
            boolean drop = dragTradeActive && i == tradeDropIndex;
            int selectionMix = selected ? Math.round(0.16f * tradeSelectionAnim * 255f) : 0;
            int bg = selected ? UiTheme.mix(cardColor, UiTheme.withAlpha(accent, selectionMix), 0.62f)
                    : hover ? UiTheme.mix(cardColor, 0x6A273752, 0.55f) : cardColor;
            int edge = drop || selected ? accent : hover ? 0xFF415A86 : borderColor;
            g.fill(x, y, x + TRADE_CARD_WIDTH, y + TRADE_CARD_HEIGHT, bg);
            border(g, x, y, TRADE_CARD_WIDTH, TRADE_CARD_HEIGHT, edge);
            if (selected) {
                int selectedWidth = Math.max(2, Math.round(TRADE_CARD_WIDTH * tradeSelectionAnim));
                g.fill(x, y + TRADE_CARD_HEIGHT - 2, x + selectedWidth, y + TRADE_CARD_HEIGHT, accent);
            }

            String number = "#" + (i + 1);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, number, x + 5, y + 10, selected ? accent : TXT_DIM, false);
            renderTradeCardItem(g, trade.buyId, trade.buyComponents, trade.buyCount, x + 25, y + 6);
            if (!trade.buy2Id.isBlank()) {
                renderTradeCardItem(g, trade.buy2Id, trade.buy2Components, trade.buy2Count, x + 45, y + 6);
            } else {
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, "+", x + 49, y + 10, TXT_DIM, false);
            }
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, UiIcons.component(UiIcons.CHEVRON_RIGHT), x + 65, y + 10, TXT_DIM, false);
            renderTradeCardItem(g, trade.sellId, trade.sellComponents, trade.sellCount, x + 82, y + 6);

            tradeCardHits.add(new TradeCardHit(x, y, TRADE_CARD_WIDTH, TRADE_CARD_HEIGHT, i));
            if (hover) {
                hoveredTradeTip = Component.translatable("ankinbt.villager.trade_card_tip", i + 1,
                        compactItemId(trade.buyId), compactItemId(trade.sellId));
                requestTooltip(hoveredTradeTip, mx, my);
            }
        }
        g.disableScissor();

        int toolX = right + 6;
        for (TradeTool tool : TradeTool.values()) {
            boolean enabled = switch (tool) {
                case DELETE -> trades.size() > 1;
                case LEFT -> tradeIndex > 0;
                case RIGHT -> tradeIndex < trades.size() - 1;
                default -> true;
            };
            boolean hover = mx >= toolX && mx < toolX + 24 && my >= y + 2 && my < y + 26;
            int bg = !enabled ? 0x20101827 : hover ? 0x6A273752 : 0x3A1B2638;
            int edge = hover && enabled ? accent : borderColor;
            g.fill(toolX, y + 2, toolX + 24, y + 26, bg);
            border(g, toolX, y + 2, 24, 24, edge);
            Component icon = UiIcons.component(tool.glyph);
            int iconX = toolX + (24 - font.width(icon)) / 2;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, icon, iconX, y + 10, enabled ? TXT_MAIN : TXT_DIM, false);
            if (hover) {
                hoveredTradeTip = Component.translatable(tool.tooltipKey);
                requestTooltip(hoveredTradeTip, mx, my);
            }
            toolX += 27;
        }
    }

    private void renderTradeCardItem(GuiGraphics g, String id, CompoundTag components, int count, int x, int y) {
        ItemStack stack = buildPreviewStack(id, components, count);
        if (!stack.isEmpty()) {
            g.renderItem(stack, x, y);
            if (count > 1) {
                String amount = String.valueOf(count);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, amount,
                        x + 15 - font.width(amount), y + 10, 0xFFFFFFFF, true);
            }
        }
    }

    private String compactItemId(String id) {
        if (id == null || id.isBlank()) return tr("ankinbt.villager.profession.none");
        int colon = id.indexOf(':');
        return colon >= 0 && colon + 1 < id.length() ? id.substring(colon + 1) : id;
    }

    private boolean handleTradeStripClick(double mx, double my, int button) {
        int y = tradeStripY();
        if (my < y || my >= y + TRADE_CARD_HEIGHT) return false;
        if (button == 0) {
            int toolX = tradeStripRight() + 6;
            for (TradeTool tool : TradeTool.values()) {
                if (mx >= toolX && mx < toolX + 24 && my >= y + 2 && my < y + 26) {
                    activateTradeTool(tool);
                    return true;
                }
                toolX += 27;
            }
            for (TradeCardHit hit : tradeCardHits) {
                if (!hit.hit((int) mx, (int) my)) continue;
                if (!selectTrade(hit.index)) return true;
                dragTradeIndex = hit.index;
                tradeDropIndex = hit.index;
                dragTradeStartX = mx;
                dragTradeStartY = my;
                dragTradeActive = false;
                return true;
            }
        }
        return mx >= tradeStripLeft() && mx < px + pw - 18;
    }

    private void activateTradeTool(TradeTool tool) {
        switch (tool) {
            case NEW -> addTrade();
            case COPY -> duplicateTrade();
            case DELETE -> removeTrade();
            case LEFT -> moveCurrentTrade(-1);
            case RIGHT -> moveCurrentTrade(1);
        }
        rebuildButtons();
    }

    private void ensureTradeVisible() {
        ensureTrades();
        int viewWidth = Math.max(1, tradeStripRight() - tradeStripLeft());
        int contentWidth = trades.size() * (TRADE_CARD_WIDTH + TRADE_CARD_GAP) - TRADE_CARD_GAP;
        tradeNavScrollMax = Math.max(0, contentWidth - viewWidth);
        int cardLeft = tradeIndex * (TRADE_CARD_WIDTH + TRADE_CARD_GAP);
        int cardRight = cardLeft + TRADE_CARD_WIDTH;
        if (cardLeft < tradeNavScroll) tradeNavScroll = cardLeft;
        if (cardRight > tradeNavScroll + viewWidth) tradeNavScroll = cardRight - viewWidth;
        tradeNavScroll = Math.max(0, Math.min(tradeNavScroll, tradeNavScrollMax));
    }

    private void updateTradeDropTarget(double mx, double my) {
        if (dragTradeIndex < 0) return;
        if (!dragTradeActive) {
            double dx = mx - dragTradeStartX;
            double dy = my - dragTradeStartY;
            if (dx * dx + dy * dy < 16.0) return;
            dragTradeActive = true;
        }
        int left = tradeStripLeft();
        int right = tradeStripRight();
        long now = System.currentTimeMillis();
        if (now - lastTradeAutoScrollAt >= 90L) {
            if (mx < left + 20 && tradeNavScroll > 0) {
                tradeNavScroll = Math.max(0, tradeNavScroll - 18);
                lastTradeAutoScrollAt = now;
            } else if (mx > right - 20 && tradeNavScroll < tradeNavScrollMax) {
                tradeNavScroll = Math.min(tradeNavScrollMax, tradeNavScroll + 18);
                lastTradeAutoScrollAt = now;
            }
        }
        int relativeX = (int) Math.round(mx - left + tradeNavScrollVisual);
        int index = Math.floorDiv(Math.max(0, relativeX), TRADE_CARD_WIDTH + TRADE_CARD_GAP);
        tradeDropIndex = Math.max(0, Math.min(trades.size() - 1, index));
    }

    private void finishTradeDrag() {
        int from = dragTradeIndex;
        int to = tradeDropIndex;
        boolean move = dragTradeActive && from >= 0 && to >= 0 && from != to;
        resetTradeDrag();
        if (move) moveTradeToIndex(from, to);
    }

    private void resetTradeDrag() {
        dragTradeIndex = -1;
        tradeDropIndex = -1;
        dragTradeActive = false;
    }

    private void renderTradeFieldLabel(GuiGraphics g, Component label, int x, int y) {
        String text = label.getString();
        int maxWidth = Math.max(32, tradeFieldInputX() - x - 8);
        if (font.width(text) > maxWidth) text = font.plainSubstrByWidth(text, Math.max(12, maxWidth - 6)) + "..";
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, x + 2, y + 5, TXT_DIM, false);
    }

    private void renderTradeScrollBar(GuiGraphics g, int x, int y, int h, int accent) {
        if (tradeScrollMax <= 0 || h <= 20) return;
        g.fill(x, y, x + 4, y + h, 0x35192738);
        int thumbH = Math.max(24, (int) Math.round((double) h * h / (h + tradeScrollMax)));
        int travel = Math.max(0, h - thumbH);
        int thumbY = y + (tradeScrollMax == 0 ? 0 : (int) Math.round((double) tradeScroll / tradeScrollMax * travel));
        g.fill(x, thumbY, x + 4, thumbY + thumbH, accent);
    }

    private void renderContentScrollBar(GuiGraphics g, int accent) {
        if (contentScrollMax <= 0) return;
        int y = bodyTop() + 2;
        int h = bodyBottom() - bodyTop() - 4;
        if (h <= 12) return;
        int viewport = Math.max(1, bodyBottom() - bodyTop());
        int contentExtent = viewport + contentScrollMax;
        int thumbH = Math.max(12, (int) Math.round((double) h * viewport / contentExtent));
        thumbH = Math.min(h, thumbH);
        int travel = Math.max(0, h - thumbH);
        int thumbY = y + (int) Math.round((double) contentScroll / contentScrollMax * travel);
        int x = px + pw - 5;
        g.fill(x, y, x + 2, y + h, UiTheme.withAlpha(UiTheme.baseRgb(), 44));
        g.fill(x, thumbY, x + 2, thumbY + thumbH, UiTheme.withAlpha(accent & 0x00FFFFFF, 190));
    }

    private String safeValue(String in, String def) {
        String t = in == null ? "" : in.trim();
        return t.isEmpty() ? def : t;
    }

    private void renderInlineEditBox(GuiGraphics g, EditBox box, int mx, int my, int accent) {
        if (box == null) return;
        boolean focused = box.isFocused();
        boolean hover = mx >= box.getX() && mx < box.getX() + box.getWidth() && my >= box.getY() && my < box.getY() + box.getHeight();
        box.render(g, mx, my, 0f);
        int lineColor = box == invalidBox ? TXT_ERR
                : focused ? accent
                : hover ? UiTheme.withAlpha(accent & 0x00FFFFFF, 150)
                : UiTheme.themedBorder(AnkiConfig.getUiOpacity(), 1f);
        g.fill(box.getX(), box.getY() + box.getHeight() - 1, box.getX() + box.getWidth(), box.getY() + box.getHeight(), lineColor);
    }

    private boolean handleEditBoxClick(double mx, double my, int button) {
        boolean hit = false;
        for (EditBox box : allBoxes()) {
            if (box != null && isTradeBoxVisible(box)
                    && mx >= box.getX() && mx < box.getX() + box.getWidth()
                    && my >= box.getY() && my < box.getY() + box.getHeight()) {
                if (button == 0 && !box.isFocused()) pushUndo();
                if (!clickEditBox(box, mx, my, button)) continue;
                focusBox(box);
                hit = true;
                break;
            }
        }
        if (!hit && button == 0) unfocusEditBoxes();
        return hit;
    }

    private boolean isTradeBoxVisible(EditBox box) {
        if (box == null || !drawerOpen || drawerAnim < 0.92f || contentAnim < 0.92f) return false;
        return visibleBoxes().contains(box)
                && box.getY() + box.getHeight() > bodyTop()
                && box.getY() < bodyBottom();
    }

    private boolean handleEditBoxKey(int key, int scan, int mod) {
        for (EditBox box : visibleBoxes()) {
            if (box != null && box.isFocused() && pressEditBox(box, key, scan, mod)) return true;
        }
        return false;
    }

    private boolean hasFocusedTradeBox() {
        for (EditBox box : allBoxes()) {
            if (box != null && box.isFocused()) return true;
        }
        return false;
    }

    private boolean handleEditBoxChar(char codePoint, int modifiers) {
        for (EditBox box : visibleBoxes()) {
            if (box != null && box.isFocused() && typeEditBox(box, codePoint, modifiers)) return true;
        }
        return false;
    }

    private boolean handleEditBoxChar(CharacterEvent event) {
        if (event == null) return false;
        for (EditBox box : visibleBoxes()) {
            if (box != null && box.isFocused() && box.charTyped(event)) return true;
        }
        return false;
    }

    private boolean clickEditBox(EditBox box, double mx, double my, int button) {
        return box != null && box.mouseClicked(new MouseButtonEvent(mx, my, new net.minecraft.client.input.MouseButtonInfo(button, 0)), false);
    }
    private boolean pressEditBox(EditBox box, int key, int scan, int mod) {
        return box != null && box.keyPressed(new KeyEvent(key, scan, mod));
    }
    private boolean typeEditBox(EditBox box, char codePoint, int modifiers) {
        return box != null && box.charTyped(new CharacterEvent(codePoint, 0));
    }

    private List<EditBox> allBoxes() {
        return java.util.Arrays.asList(buyId, buyCount, buy2Id, buy2Count, sellId, sellCount, maxUses, xp);
    }

    private List<EditBox> visibleBoxes() {
        if (activeTab == VillagerTab.BUY) return java.util.Arrays.asList(buyId, buyCount, buy2Id, buy2Count);
        if (activeTab == VillagerTab.SELL) return java.util.Arrays.asList(sellId, sellCount);
        if (activeTab == VillagerTab.TRADE) return java.util.Arrays.asList(maxUses, xp);
        return List.of();
    }

    private void focusBox(EditBox target) {
        for (EditBox box : allBoxes()) {
            if (box != null) box.setFocused(box == target);
        }
        this.setFocused(target);
        setDragging(target != null);
    }

    private void unfocusEditBoxes() {
        if (getFocused() instanceof EditBox) setFocused(null);
        setDragging(false);
        for (EditBox box : allBoxes()) {
            if (box != null) box.setFocused(false);
        }
    }

    private EditBox boxForTarget(InvPickTarget target) {
        return switch (target) {
            case BUY2 -> buy2Id;
            case SELL -> sellId;
            default -> buyId;
        };
    }

    private void renderTradeIcons(GuiGraphics g, int mx, int my, int x, int y, int accent) {
        iconHits.clear();
        ensureTrades();
        TradeData live = previewTradeFromDraft(trades.get(tradeIndex));
        renderIconSlot(g, mx, my, x, y, buyId == null ? "" : buyId.getValue(), live.buyComponents, live.buyCount,
                InvPickTarget.BUY, tr("ankinbt.villager.buy_item"), accent);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, "+", x + 42, y + 5, TXT_DIM, false);
        renderIconSlot(g, mx, my, x + 52, y, buy2Id == null ? "" : buy2Id.getValue(), live.buy2Components, live.buy2Count,
                InvPickTarget.BUY2, tr("ankinbt.villager.buy2_item"), accent);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, "->", x + 92, y + 5, TXT_DIM, false);
        renderIconSlot(g, mx, my, x + 112, y, sellId == null ? "" : sellId.getValue(), live.sellComponents, live.sellCount,
                InvPickTarget.SELL, tr("ankinbt.villager.sell_item"), accent);
    }

    private void renderIconSlot(GuiGraphics g, int mx, int my, int x, int y, String itemId, CompoundTag components, int count,
                                InvPickTarget target, String hint, int accent) {
        int w = 18;
        int h = 18;
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        int bg = hover ? 0x8A273752 : 0x661B2638;
        int edge = hover ? accent : 0xFF2C3B5C;
        g.fill(x, y, x + w, y + h, bg);
        border(g, x, y, w, h, edge);

        ItemStack preview = buildPreviewStack(itemId, components, count);
        if (!preview.isEmpty()) {
            g.renderItem(preview, x + 1, y + 1);
        }
        iconHits.add(new IconHit(x, y, w, h, target));

        if (hover) {
            String text = itemId == null || itemId.isBlank() ? ("<" + tr("ankinbt.villager.profession.none") + ">") : itemId;
            if (!preview.isEmpty()) {
                renderStackTooltip(g, preview, mx, my, hint, text);
            } else {
                requestTooltip(Component.literal(hint + ": " + text), mx, my);
            }
        }
    }

    private ItemStack buildPreviewStack(String itemId, CompoundTag components, int count) {
        Item item = resolveItem(itemId);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        int n = Math.max(1, Math.min(64, count));
        CompoundTag fullStack = readWrappedFullStack(components);
        if (fullStack != null && !fullStack.isEmpty()) {
            try {
                CompoundTag full = copyCompound(fullStack);
                full.putString("id", itemId);
                full.putInt("count", n);
                Optional<ItemStack> out = NbtHelper.deserializeItemStack(full);
                if (out.isPresent() && !out.get().isEmpty()) return out.get();
            } catch (Throwable ignored) {}
        }
        CompoundTag componentData = unwrapTradeComponents(components);
        if (componentData == null || componentData.isEmpty()) return new ItemStack(item, n);
        try {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", itemId);
            tag.putInt("count", n);
            tag.put("components", copyCompound(componentData));
            Optional<ItemStack> out = NbtHelper.deserializeItemStack(tag);
            if (out.isPresent() && !out.get().isEmpty()) return out.get();
        } catch (Throwable ignored) {}
        return new ItemStack(item, n);
    }

    private void renderStackTooltip(GuiGraphics g, ItemStack stack, int mx, int my, String hint, String itemId) {
        if (stack == null || stack.isEmpty()) {
            requestTooltip(Component.literal(hint + ": " + itemId), mx, my);
            return;
        }
        requestItemTooltip(stack, mx, my);
    }

    private Item resolveItem(String itemId) {
        if (itemId == null || itemId.isBlank()) return null;
        if (itemCache.containsKey(itemId)) return itemCache.get(itemId);
        Item found = ItemRegistryHelper.resolveItem(itemId);
        itemCache.put(itemId, found);
        return found;
    }

    private void renderInventoryOverlay(GuiGraphics g, int mx, int my, int accent) {
        invSlotHits.clear();
        if (invPickTarget == InvPickTarget.NONE) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int cols = 9;
        int rows = 4;
        int cell = 20;
        int w = cols * cell + 20;
        int h = rows * cell + 44;
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        g.fill(0, 0, width, height, 0x99000000);
        g.fill(x, y, x + w, y + h, 0xF0111726);
        border(g, x, y, w, h, 0xFF2C3B5C);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.villager.pick.inv") + " - " + focusedTargetText(), x + 10, y + 10, accent, false);

        int startX = x + 10;
        int startY = y + 24;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int logical = r < 3 ? (9 + r * 9 + c) : c;
                ItemStack stack = mc.player.getInventory().getItem(logical);
                int sx = startX + c * cell;
                int sy = startY + r * cell;
                g.fill(sx, sy, sx + 18, sy + 18, 0x4A1B2638);
                border(g, sx, sy, 18, 18, 0xFF2C3B5C);
                if (stack != null && !stack.isEmpty()) {
                    g.renderItem(stack, sx + 1, sy + 1);
                    String id = SpawnEggEditorHelper.getItemId(stack);
                    invSlotHits.add(new InvSlotHit(sx, sy, 18, 18, id, stack.copy()));
                    if (mx >= sx && mx < sx + 18 && my >= sy && my < sy + 18) {
                        renderStackTooltip(g, stack, mx, my, tr("ankinbt.villager.pick.inv"), id);
                    }
                }
            }
        }
    }

    private boolean clickInventoryOverlay(int mx, int my, int button) {
        if (button != 0) {
            invPickTarget = InvPickTarget.NONE;
            return true;
        }
        for (InvSlotHit hit : invSlotHits) {
            if (hit.hit(mx, my)) {
                EditBox box = boxForTarget(invPickTarget);
                if (box != null) {
                    pushUndo();
                    if (applyPickedStack(box, hit.stack)) dirty = true;
                }
                invPickTarget = InvPickTarget.NONE;
                return true;
            }
        }
        invPickTarget = InvPickTarget.NONE;
        return true;
    }

    private void ensureTrades() {
        if (trades.isEmpty()) trades.add(TradeData.defaults());
        tradeIndex = Math.max(0, Math.min(tradeIndex, trades.size() - 1));
    }

    private TradeData readTradeFromForm(TradeData prev) {
        TradeData source = prev == null ? TradeData.blank() : prev;
        TradeData t = source.copy();
        TradeDraft draft = source.draft == null ? TradeDraft.from(source) : source.draft.copy();
        t.buyId = draft.buyId.trim();
        t.buy2Id = draft.buy2Id.trim();
        t.sellId = draft.sellId.trim();
        t.buyCount = Integer.parseInt(draft.buyCount.trim());
        t.buy2Count = Integer.parseInt(draft.buy2Count.trim());
        t.sellCount = Integer.parseInt(draft.sellCount.trim());
        t.maxUses = Integer.parseInt(draft.maxUses.trim());
        t.xp = Integer.parseInt(draft.xp.trim());
        if (!Objects.equals(t.buyId, source.buyId)) t.buyComponents = null;
        if (!Objects.equals(t.buy2Id, source.buy2Id)) t.buy2Components = null;
        if (!Objects.equals(t.sellId, source.sellId)) t.sellComponents = null;
        t.rewardExp = rewardExp;
        t.draft = draft;
        return t;
    }

    private boolean syncCurrentTrade(boolean strict) {
        ensureTrades();
        captureDraftFromBoxes();
        TradeData prev = trades.get(tradeIndex);
        TradeDraft draft = prev.draft == null ? TradeDraft.from(prev) : prev.draft;
        EditBox bad = invalidDraftBox(draft, strict);
        if (bad != null) {
            invalidBox = bad;
            if (strict) {
                VillagerTab required = tabForBox(bad);
                if (required != activeTab) {
                    activeTab = required;
                    contentAnim = 0f;
                    layoutTradeFields();
                    rebuildButtons();
                }
                focusBox(bad);
            }
            return false;
        }
        try {
            trades.set(tradeIndex, readTradeFromForm(prev));
            invalidBox = null;
            return true;
        } catch (NumberFormatException ignored) {
            invalidBox = firstNumericBox();
            if (strict) setStatus(Component.translatable("ankinbt.simple.invalid_number"), TXT_ERR);
            return false;
        }
    }

    private void captureDraftFromBoxes() {
        if (trades.isEmpty() || tradeIndex < 0 || tradeIndex >= trades.size()
                || buyId == null || buyCount == null || buy2Id == null || buy2Count == null
                || sellId == null || sellCount == null || maxUses == null || xp == null) return;
        trades.get(tradeIndex).draft = new TradeDraft(
                buyId.getValue(), buyCount.getValue(), buy2Id.getValue(), buy2Count.getValue(),
                sellId.getValue(), sellCount.getValue(), maxUses.getValue(), xp.getValue());
    }

    private EditBox invalidDraftBox(TradeDraft draft, boolean showStatus) {
        if (draft == null) return buyId;
        if (!isRegisteredItem(draft.buyId.trim())) {
            if (showStatus) setStatus(Component.translatable("ankinbt.villager.invalid_item"), TXT_ERR);
            return buyId;
        }
        if (!draft.buy2Id.trim().isEmpty() && !isRegisteredItem(draft.buy2Id.trim())) {
            if (showStatus) setStatus(Component.translatable("ankinbt.villager.invalid_item"), TXT_ERR);
            return buy2Id;
        }
        if (!isRegisteredItem(draft.sellId.trim())) {
            if (showStatus) setStatus(Component.translatable("ankinbt.villager.invalid_item"), TXT_ERR);
            return sellId;
        }
        if (!validInt(draft.buyCount, 1) || !validInt(draft.buy2Count, 1)
                || !validInt(draft.sellCount, 1) || !validInt(draft.maxUses, 1)
                || !validInt(draft.xp, 0)) {
            if (showStatus) setStatus(Component.translatable("ankinbt.simple.invalid_number"), TXT_ERR);
            if (!validInt(draft.buyCount, 1)) return buyCount;
            if (!validInt(draft.buy2Count, 1)) return buy2Count;
            if (!validInt(draft.sellCount, 1)) return sellCount;
            if (!validInt(draft.maxUses, 1)) return maxUses;
            return xp;
        }
        return null;
    }

    private boolean validInt(String value, int minimum) {
        if (value == null || value.isBlank()) return false;
        try {
            return Integer.parseInt(value.trim()) >= minimum;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private EditBox firstNumericBox() {
        TradeDraft draft = trades.get(tradeIndex).draft;
        if (draft == null || !validInt(draft.buyCount, 1)) return buyCount;
        if (!validInt(draft.buy2Count, 1)) return buy2Count;
        if (!validInt(draft.sellCount, 1)) return sellCount;
        if (!validInt(draft.maxUses, 1)) return maxUses;
        return xp;
    }

    private VillagerTab tabForBox(EditBox box) {
        if (box == buyId || box == buyCount || box == buy2Id || box == buy2Count) return VillagerTab.BUY;
        if (box == sellId || box == sellCount) return VillagerTab.SELL;
        return VillagerTab.TRADE;
    }

    private TradeData previewTradeFromDraft(TradeData source) {
        TradeData out = source == null ? TradeData.blank() : source.copy();
        TradeDraft draft = source == null || source.draft == null ? TradeDraft.from(out) : source.draft;
        out.buyId = draft.buyId.trim();
        out.buy2Id = draft.buy2Id.trim();
        out.sellId = draft.sellId.trim();
        out.buyCount = parsedOr(draft.buyCount, out.buyCount, 1);
        out.buy2Count = parsedOr(draft.buy2Count, out.buy2Count, 1);
        out.sellCount = parsedOr(draft.sellCount, out.sellCount, 1);
        out.maxUses = parsedOr(draft.maxUses, out.maxUses, 1);
        out.xp = parsedOr(draft.xp, out.xp, 0);
        return out;
    }

    private int parsedOr(String value, int fallback, int minimum) {
        try {
            return Math.max(minimum, Integer.parseInt(value.trim()));
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private void loadTradeToForm(int idx) {
        ensureTrades();
        TradeData t = trades.get(idx);
        if (t.draft == null) t.draft = TradeDraft.from(t);
        TradeDraft draft = t.draft;
        setBoxValue(buyId, draft.buyId);
        setBoxValue(buyCount, draft.buyCount);
        setBoxValue(buy2Id, draft.buy2Id);
        setBoxValue(buy2Count, draft.buy2Count);
        setBoxValue(sellId, draft.sellId);
        setBoxValue(sellCount, draft.sellCount);
        setBoxValue(maxUses, draft.maxUses);
        setBoxValue(xp, draft.xp);
        rewardExp = t.rewardExp;
    }

    private void prevTrade() {
        ensureTrades();
        int next = tradeIndex - 1;
        if (next < 0) next = trades.size() - 1;
        selectTrade(next);
    }

    private void nextTrade() {
        ensureTrades();
        int next = tradeIndex + 1;
        if (next >= trades.size()) next = 0;
        selectTrade(next);
    }

    private void addTrade() {
        ensureTrades();
        if (!syncCurrentTrade(true)) return;
        pushUndo();
        trades.add(tradeIndex + 1, TradeData.blank());
        tradeIndex++;
        loadTradeToForm(tradeIndex);
        dirty = true;
        tradeSelectionAnim = 0f;
        ensureTradeVisible();
        setStatus(Component.translatable("ankinbt.villager.trade_added", tradeIndex + 1, trades.size()), TXT_OK);
    }

    private void duplicateTrade() {
        ensureTrades();
        if (!syncCurrentTrade(true)) return;
        pushUndo();
        trades.add(tradeIndex + 1, trades.get(tradeIndex).copy());
        tradeIndex++;
        loadTradeToForm(tradeIndex);
        dirty = true;
        tradeSelectionAnim = 0f;
        ensureTradeVisible();
        setStatus(Component.translatable("ankinbt.villager.trade_copied", tradeIndex + 1), TXT_OK);
    }

    private void removeTrade() {
        ensureTrades();
        if (trades.size() <= 1) return;
        if (!syncCurrentTrade(true)) return;
        pushUndo();
        trades.remove(tradeIndex);
        if (tradeIndex >= trades.size()) tradeIndex = trades.size() - 1;
        loadTradeToForm(tradeIndex);
        dirty = true;
        tradeSelectionAnim = 0f;
        ensureTradeVisible();
        setStatus(Component.translatable("ankinbt.villager.trade_removed", trades.size()), TXT_DIM);
    }

    private boolean selectTrade(int index) {
        ensureTrades();
        if (index < 0 || index >= trades.size()) return false;
        if (index == tradeIndex) return true;
        if (!syncCurrentTrade(true)) {
            setStatus(Component.translatable("ankinbt.simple.invalid_number"), TXT_ERR);
            return false;
        }
        tradeIndex = index;
        loadTradeToForm(tradeIndex);
        tradeSelectionAnim = 0f;
        ensureTradeVisible();
        rebuildButtons();
        return true;
    }

    private void moveCurrentTrade(int direction) {
        ensureTrades();
        int target = Math.max(0, Math.min(trades.size() - 1, tradeIndex + direction));
        if (target == tradeIndex) return;
        moveTradeToIndex(tradeIndex, target);
    }

    private void moveTradeToIndex(int from, int to) {
        ensureTrades();
        if (from < 0 || from >= trades.size() || to < 0 || to >= trades.size() || from == to) return;
        if (!syncCurrentTrade(true)) return;
        pushUndo();
        TradeData moved = trades.remove(from);
        trades.add(to, moved);
        tradeIndex = to;
        loadTradeToForm(tradeIndex);
        dirty = true;
        tradeSelectionAnim = 0f;
        ensureTradeVisible();
        setStatus(Component.translatable("ankinbt.villager.trade_moved", tradeIndex + 1), TXT_DIM);
        rebuildButtons();
    }

    private void fillFromMainHand(EditBox box) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
        ItemStack stack = mc.player.getInventory().getItem(slot);
        if (stack == null || stack.isEmpty()) stack = mc.player.getMainHandItem();
        if (stack == null || stack.isEmpty()) return;
        pushUndo();
        if (applyPickedStack(box, stack)) dirty = true;
    }

    private boolean applyPickedStack(EditBox box, ItemStack stack) {
        if (box == null || stack == null || stack.isEmpty()) return false;
        String id = SpawnEggEditorHelper.getItemId(stack);
        if (!isLikelyItemId(id)) return false;
        setBoxValue(box, id);
        syncCurrentTrade(false);
        setPickedComponents(box, readPickedStackData(stack));
        return true;
    }

    private StateSnapshot captureState() {
        syncCurrentTrade(false);
        List<TradeData> copy = new ArrayList<>();
        for (TradeData t : trades) copy.add(t.copy());
        return new StateSnapshot(copy, tradeIndex, professionIndex, villagerLevel, rewardExp, villagerType, dirty);
    }

    private void applyState(StateSnapshot s) {
        if (s == null) return;
        trades.clear();
        for (TradeData t : s.trades) trades.add(t.copy());
        ensureTrades();
        tradeIndex = Math.max(0, Math.min(s.tradeIndex, trades.size() - 1));
        professionIndex = Math.max(0, Math.min(PROFESSIONS.length - 1, s.professionIndex));
        villagerLevel = Math.max(1, Math.min(5, s.villagerLevel));
        rewardExp = s.rewardExp;
        villagerType = s.villagerType;
        dirty = s.dirty;
        loadTradeToForm(tradeIndex);
    }

    private void pushUndo() {
        StateSnapshot current = captureState();
        if (!undoStack.isEmpty() && Objects.equals(undoStack.get(undoStack.size() - 1), current)) return;
        undoStack.add(current);
        while (undoStack.size() > MAX_UNDO) undoStack.remove(0);
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        StateSnapshot previous = undoStack.remove(undoStack.size() - 1);
        applyState(previous);
        ensureTradeVisible();
        rebuildButtons();
        setStatus(Component.translatable("ankinbt.status.edited"), TXT_DIM);
    }

    private void tryClose() {
        syncCurrentTrade(false);
        if (dirty && AnkiConfig.isConfirmOnClose()) {
            modalAnim = 0f;
            confirmClose = true;
            return;
        }
        Minecraft.getInstance().setScreen(parent);
    }

    private void renderConfirm(GuiGraphics g, int mx, int my, boolean resetMode) {
        int w = Math.min(320, width - 24), h = 126;
        int x = (width - w) / 2;
        int y = modalDialogY(h);
        float opacity = AnkiConfig.getUiOpacity();
        int danger = 0xFFEF4444;
        g.fill(0, 0, width, height, UiTheme.withAlpha(0x000000, Math.round(152 * modalAnim)));
        g.fill(x, y, x + w, y + h, UiTheme.panel(Math.max(0.72f, opacity), modalAnim));
        border(g, x, y, w, h, UiTheme.themedBorder(opacity, modalAnim));
        g.fill(x + 1, y + 1, x + w - 1, y + 3,
                UiTheme.withAlpha(danger & 0x00FFFFFF, Math.round(220 * modalAnim)));
        VersionCompat.get().drawString(g, font, tr("ankinbt.entity.reset_changes"),
                x + 12, y + 12, danger, false);
        VersionCompat.get().drawString(g, font,
                trimToWidth(tr("ankinbt.confirm.unsaved"), w - 24),
                x + 12, y + 35, UiTheme.textMain(), false);
        VersionCompat.get().drawString(g, font,
                trimToWidth(tr("ankinbt.confirm.discard_hint"), w - 24),
                x + 12, y + 50, UiTheme.textDim(), false);
        int by = y + h - 34;
        int gap = 8;
        int bw = (w - 24 - gap) / 2;
        renderModalButton(g, x + 12, by, bw, 22, tr("ankinbt.edit.cancel"), mx, my, 0, 0);
        renderModalButton(g, x + 12 + bw + gap, by, bw, 22, tr("ankinbt.edit.apply"), mx, my, 1, -1);
    }

    private void renderUnsavedConfirmLikeSimple(GuiGraphics g, int mx, int my) {
        int dw = Math.min(320, width - 24), dh = 126;
        int dx = (width - dw) / 2, dy = modalDialogY(dh);
        float opacity = AnkiConfig.getUiOpacity();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(0, 0, width, height, UiTheme.withAlpha(0x000000, Math.round(152 * modalAnim)));
        g.fill(dx, dy, dx + dw, dy + dh, UiTheme.panel(Math.max(0.72f, opacity), modalAnim));
        border(g, dx, dy, dw, dh, UiTheme.themedBorder(opacity, modalAnim));
        g.fill(dx + 1, dy + 1, dx + dw - 1, dy + 3,
                UiTheme.withAlpha(accent & 0x00FFFFFF, Math.round(220 * modalAnim)));
        VersionCompat.get().drawString(g, font, tr("ankinbt.confirm.title"), dx + 12, dy + 12,
                UiTheme.textMain(), false);
        VersionCompat.get().drawString(g, font, trimToWidth(tr("ankinbt.confirm.unsaved"), dw - 24),
                dx + 12, dy + 35, UiTheme.textDim(), false);
        VersionCompat.get().drawString(g, font, trimToWidth(tr("ankinbt.confirm.discard_hint"), dw - 24),
                dx + 12, dy + 50, UiTheme.textDim(), false);
        int by = dy + dh - 34;
        int gap = 6;
        int bw = (dw - 24 - gap * 2) / 3;
        renderModalButton(g, dx + 12, by, bw, 22, tr("ankinbt.confirm.save_close"), mx, my, 0, 1);
        renderModalButton(g, dx + 12 + bw + gap, by, bw, 22, tr("ankinbt.confirm.discard"), mx, my, 1, -1);
        renderModalButton(g, dx + 12 + (bw + gap) * 2, by, bw, 22, tr("ankinbt.edit.cancel"), mx, my, 2, 0);
    }

    private int modalDialogY(int dialogHeight) {
        return (height - dialogHeight) / 2 + Math.round((1f - modalAnim) * 12f);
    }

    private void renderModalButton(GuiGraphics g, int x, int y, int w, int h, String label,
                                   int mx, int my, int index, int tone) {
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        float speed = AnkiConfig.isUiAnimationEnabled()
                ? Math.min(1f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1f;
        modalButtonHover[index] = UiTheme.approach(modalButtonHover[index], hover ? 1f : 0f, speed);
        int color = tone > 0 ? 0xFF22C55E : tone < 0 ? 0xFFEF4444
                : UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(x, y, x + w, y + h,
                UiTheme.withAlpha(color & 0x00FFFFFF, 42 + Math.round(58 * modalButtonHover[index])));
        border(g, x, y, w, h,
                UiTheme.withAlpha(color & 0x00FFFFFF, 170 + Math.round(70 * modalButtonHover[index])));
        String shown = trimToWidth(label, w - 12);
        VersionCompat.get().drawString(g, font, shown,
                x + (w - font.width(shown)) / 2, y + Math.max(1, (h - 8) / 2), UiTheme.textMain(), false);
    }

    private boolean clickConfirm(int mx, int my) {
        if (confirmClose) return clickUnsavedConfirmLikeSimple(mx, my);

        int w = Math.min(320, width - 24), h = 126;
        int x = (width - w) / 2;
        int y = modalDialogY(h);
        int by = y + h - 34;
        int gap = 8;
        int bw = (w - 24 - gap) / 2;
        if (confirmReset) {
            int cancelX = x + 12;
            if (mx >= cancelX && mx < cancelX + bw && my >= by && my < by + 22) {
                confirmReset = false;
                return true;
            }
            int applyX = cancelX + bw + gap;
            if (mx >= applyX && mx < applyX + bw && my >= by && my < by + 22) {
                confirmReset = false;
                resetForm();
                return true;
            }
            return true;
        }

        return true;
    }

    private boolean clickUnsavedConfirmLikeSimple(int mx, int my) {
        int dw = Math.min(320, width - 24), dh = 126;
        int dx = (width - dw) / 2, dy = modalDialogY(dh);
        int by = dy + dh - 34;
        int gap = 6;
        int bw2 = (dw - 24 - gap * 2) / 3, bh2 = 22;

        int saveX = dx + 12;
        if (mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2) {
            applyTrade();
            if (!dirty) {
                confirmClose = false;
                Minecraft.getInstance().setScreen(parent);
            }
            return true;
        }
        int discardX = saveX + bw2 + gap;
        if (mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2) {
            confirmClose = false;
            dirty = false;
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        int cancelX = discardX + bw2 + gap;
        if (mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2) {
            confirmClose = false;
            return true;
        }
        return true;
    }

    private void drawRightLabel(GuiGraphics g, String text, int x, int y, int maxW) {
        if (maxW <= 8) return;
        String out = text == null ? "" : text;
        if (font.width(out) > maxW) out = font.plainSubstrByWidth(out, maxW - 4) + "..";
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, out, x, y, TXT_DIM, false);
    }

    private void setBoxValue(EditBox box, String value) {
        if (box == null) return;
        boolean old = suppressDirtySync;
        suppressDirtySync = true;
        box.setValue(value == null ? "" : value);
        suppressDirtySync = old;
    }

    private void setPickedComponents(EditBox box, CompoundTag components) {
        ensureTrades();
        TradeData t = trades.get(tradeIndex);
        if (box == buyId) {
            t.buyId = box.getValue().trim();
            t.buyComponents = copyCompound(components);
        } else if (box == buy2Id) {
            t.buy2Id = box.getValue().trim();
            t.buy2Components = copyCompound(components);
        } else if (box == sellId) {
            t.sellId = box.getValue().trim();
            t.sellComponents = copyCompound(components);
        }
    }

    private CompoundTag readPickedStackData(ItemStack stack) {
        CompoundTag components = readItemComponents(stack);
        if (components == null || components.isEmpty()) return null;
        try {
            Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(stack);
            if (fullOpt.isPresent() && !fullOpt.get().isEmpty()) {
                CompoundTag wrapped = copyCompound(components);
                wrapped.put(FULL_STACK_KEY, copyCompound(fullOpt.get()));
                return wrapped;
            }
        } catch (Throwable ignored) {}
        return components;
    }

    private CompoundTag readItemComponents(ItemStack stack) {
        try {
            Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(stack);
            if (fullOpt.isEmpty()) return null;
            CompoundTag full = fullOpt.get();
            CompoundTag components = readCompound(full, "components");
            if (components != null && !components.isEmpty()) return copyCompound(components);
            CompoundTag legacy = readCompound(full, "tag");
            if (legacy != null && !legacy.isEmpty()) {
                CompoundTag wrapped = new CompoundTag();
                wrapped.put("minecraft:custom_data", copyCompound(legacy));
                return wrapped;
            }
            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private CompoundTag buildTradeStackTag(String itemId, int count, CompoundTag components) {
        CompoundTag out = new CompoundTag();
        out.putString("id", itemId);
        out.putInt("count", Math.max(1, count));

        CompoundTag fullStack = readWrappedFullStack(components);
        if (fullStack != null && !fullStack.isEmpty()) {
            CompoundTag full = copyCompound(fullStack);
            full.putString("id", itemId);
            full.putInt("count", Math.max(1, count));
            CompoundTag fullComponents = readCompound(full, "components");
            if (fullComponents != null && !fullComponents.isEmpty()) out.put("components", copyCompound(fullComponents));
            CompoundTag legacyTag = readCompound(full, "tag");
            if (legacyTag != null && !legacyTag.isEmpty()) out.put("tag", copyCompound(legacyTag));
            return out;
        }

        CompoundTag plainComponents = unwrapTradeComponents(components);
        if (plainComponents != null && !plainComponents.isEmpty()) out.put("components", copyCompound(plainComponents));
        return out;
    }

    private CompoundTag readWrappedFullStack(CompoundTag components) {
        CompoundTag full = readCompound(components, FULL_STACK_KEY);
        return full == null || full.isEmpty() ? null : copyCompound(full);
    }

    private CompoundTag unwrapTradeComponents(CompoundTag components) {
        if (components == null || components.isEmpty()) return null;
        CompoundTag plain = copyCompound(components);
        plain.remove(FULL_STACK_KEY);
        return plain.isEmpty() ? null : plain;
    }

    private CompoundTag copyCompound(CompoundTag source) {
        if (source == null) return null;
        CompoundTag out = new CompoundTag();
        out.merge(source);
        return out;
    }

    private int defaultProfessionIndex() {
        return isWanderingTraderContext() ? 0 : 1;
    }

    private int normalizeProfessionIndex(int index) {
        if (isWanderingTraderContext()) return 0;
        if (index < 0 || index >= PROFESSIONS.length) return defaultProfessionIndex();
        return isTradeableProfession(PROFESSIONS[index]) ? index : defaultProfessionIndex();
    }

    private String normalizeProfessionId(String professionId) {
        if (isWanderingTraderContext()) return "";
        return isTradeableProfession(professionId) ? professionId : "minecraft:farmer";
    }

    private void normalizeProfessionState() {
        professionIndex = normalizeProfessionIndex(professionIndex);
        villagerLevel = Math.max(1, Math.min(5, villagerLevel));
        if (villagerType == null || villagerType.isBlank()) {
            villagerType = "minecraft:plains";
        }
    }

    private ListTag copyListTag(ListTag source) {
        ListTag out = new ListTag();
        if (source == null) return out;
        for (int i = 0; i < source.size(); i++) {
            Object entry = unwrapOptional(source.get(i));
            if (entry instanceof CompoundTag ct) out.add(copyCompound(ct));
            else if (entry instanceof net.minecraft.nbt.Tag tag) out.add(tag.copy());
        }
        return out;
    }

    private void injectRuntimeOffersIfMissing(CompoundTag root, Entity entity) {
        if (root == null || entity == null) return;
        CompoundTag offers = readCompound(root, "Offers");
        if (offers == null) offers = readCompound(root, "offers");
        if (hasRecipeList(offers, "Recipes") || hasRecipeList(offers, "recipes")) return;

        ListTag runtime = readRuntimeOffers(entity);
        if (runtime == null || runtime.isEmpty()) return;

        CompoundTag outOffers = offers == null ? new CompoundTag() : copyCompound(offers);
        outOffers.put("Recipes", runtime);
        outOffers.put("recipes", copyListTag(runtime));
        root.put("Offers", outOffers);
        DebugLog.info("Injected runtime villager offers: {} entries", runtime.size());
    }

    private void injectRuntimeVillagerDataIfMissing(CompoundTag root, Entity entity) {
        if (root == null || entity == null) return;
        CompoundTag current = readCompound(root, "VillagerData");
        if (current != null && !current.isEmpty()) return;

        Object data = entity instanceof Villager villager ? villager.getVillagerData() : invokeAny(entity, "getVillagerData");
        if (data == null) return;

        String professionId = extractNamespacedId(invokeAny(data, "getProfession", "profession"));
        String typeId = extractNamespacedId(invokeAny(data, "getType", "type"));
        Integer level = invokeInt(data, "getLevel");

        CompoundTag vd = new CompoundTag();
        vd.putString("profession", professionId == null || professionId.isBlank() ? "minecraft:farmer" : professionId);
        vd.putString("type", typeId == null || typeId.isBlank() ? "minecraft:plains" : typeId);
        vd.putInt("level", Math.max(1, Math.min(5, level == null ? 1 : level)));
        root.put("VillagerData", vd);
    }

    private boolean hasRecipeList(CompoundTag offers, String key) {
        if (offers == null || key == null || key.isBlank()) return false;
        Object raw = readTag(offers, key);
        return raw instanceof ListTag list && !list.isEmpty();
    }

    private ListTag readRuntimeOffers(Entity entity) {
        ListTag serverMirror = readRuntimeOffersFromIntegratedServer(entity);
        if (serverMirror != null && !serverMirror.isEmpty()) return serverMirror;

        ListTag reflective = readOffersFromEntityObject(entity);
        if (reflective != null && !reflective.isEmpty()) return reflective;
        return null;
    }

    private LoadedVillagerDefaults readDefaultsFromIntegratedServer(Entity clientEntity) {
        if (clientEntity == null) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || !mc.hasSingleplayerServer()) return null;
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) return null;

        java.util.concurrent.atomic.AtomicReference<LoadedVillagerDefaults> ref = new java.util.concurrent.atomic.AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        server.execute(() -> {
            try {
                Entity serverEntity = EditorCommandHelper.findIntegratedServerEntity(server, clientEntity.getId(), clientEntity.getUUID());
                if (!(serverEntity instanceof AbstractVillager villager)) return;

                int liveProfessionIndex = professionIndex;
                int liveLevel = villagerLevel;
                String liveType = villagerType;
                CompoundTag serverTag = readEntityTag(serverEntity);
                CompoundTag liveVillagerData = readCompound(serverTag, "VillagerData");
                if (liveVillagerData != null) {
                    String professionId = readString(liveVillagerData, "profession", "");
                    int idx = professionIndexById(professionId);
                    if (idx >= 0) liveProfessionIndex = idx;
                    liveLevel = Math.max(1, Math.min(5, readInt(liveVillagerData, "level", liveLevel)));
                    String typeId = readString(liveVillagerData, "type", liveType);
                    if (typeId != null && !typeId.isBlank()) liveType = typeId;
                } else if (serverEntity instanceof Villager liveVillager && !isWanderingTraderContext()) {
                    Object data = liveVillager.getVillagerData();
                    if (data != null) {
                        String professionId = extractNamespacedId(invokeAny(data, "getProfession", "profession"));
                        int idx = professionIndexById(professionId);
                        if (idx >= 0) liveProfessionIndex = idx;
                        Integer level = invokeInt(data, "getLevel");
                        if (level != null) liveLevel = Math.max(1, Math.min(5, level));
                        String typeId = extractNamespacedId(invokeAny(data, "getType", "type"));
                        if (typeId != null && !typeId.isBlank()) liveType = typeId;
                    }
                }

                boolean liveRewardExp = rewardExp;
                List<TradeData> liveTrades = new ArrayList<>();
                MerchantOffers offers = villager.getOffers();
                if (offers != null && !offers.isEmpty()) {
                    for (MerchantOffer offer : offers) {
                        liveTrades.add(tradeFromMerchantOffer(offer));
                        liveRewardExp = offer.shouldRewardExp();
                    }
                    DebugLog.info("Loaded villager offers from integrated merchant API: {}", liveTrades.size());
                }

                if (liveTrades.isEmpty() && serverTag != null) {
                    ListTag recipes = extractOfferRecipes(serverTag);
                    if (recipes != null && !recipes.isEmpty()) {
                        for (int i = 0; i < recipes.size(); i++) {
                            Object entry = unwrapOptional(recipes.get(i));
                            if (entry instanceof CompoundTag recipe) {
                                liveTrades.add(tradeFromRecipe(recipe));
                                Object re = readTag(recipe, "rewardExp");
                                if (re != null) {
                                    try {
                                        Object b = re.getClass().getMethod("getAsBoolean").invoke(re);
                                        if (b instanceof Boolean bb) liveRewardExp = bb;
                                    } catch (Throwable ignored) {}
                                }
                            }
                        }
                        DebugLog.info("Loaded villager offers from integrated entity tag: {}", liveTrades.size());
                    }
                }

                ref.set(new LoadedVillagerDefaults(liveProfessionIndex, liveLevel, liveType, liveRewardExp, liveTrades));
            } catch (Throwable t) {
                DebugLog.warn("Integrated villager defaults read failed: {}", t.toString());
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(3, TimeUnit.SECONDS)) {
                DebugLog.warn("Timed out waiting for integrated villager defaults");
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return ref.get();
    }

    private ListTag readRuntimeOffersFromIntegratedServer(Entity clientEntity) {
        if (clientEntity == null) return null;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || !mc.hasSingleplayerServer()) return null;
            IntegratedServer server = mc.getSingleplayerServer();
            if (server == null) return null;

            int targetId = clientEntity.getId();
            UUID targetUuid = clientEntity.getUUID();

            for (ServerLevel level : server.getAllLevels()) {
                Entity serverEntity = level.getEntity(targetId);
                if (serverEntity == null || !targetUuid.equals(serverEntity.getUUID())) {
                    serverEntity = findServerEntityByUuid(level, targetUuid);
                }
                if (serverEntity == null) continue;

                if (serverEntity instanceof AbstractVillager villager) {
                    MerchantOffers offers = villager.getOffers();
                    if (offers != null && !offers.isEmpty()) {
                        DebugLog.info("Loaded villager offers from integrated server merchant API: {}", offers.size());
                        return merchantOffersToList(offers);
                    }
                }

                CompoundTag serverTag = readEntityTag(serverEntity);
                ListTag offers = extractOfferRecipes(serverTag);
                if (offers != null && !offers.isEmpty()) {
                    DebugLog.info("Loaded villager offers from integrated server mirror: {}", offers.size());
                    return offers;
                }
            }
        } catch (Throwable t) {
            DebugLog.warn("Integrated server villager offer mirror read failed: {}", t.toString());
        }
        return null;
    }

    private Entity findServerEntityByUuid(ServerLevel level, UUID uuid) {
        if (level == null || uuid == null) return null;
        try {
            Object out = level.getClass().getMethod("getEntity", UUID.class).invoke(level, uuid);
            if (out instanceof Entity entity) return entity;
        } catch (Throwable ignored) {}
        try {
            Object all = level.getClass().getMethod("getAllEntities").invoke(level);
            if (all instanceof Iterable<?> iterable) {
                for (Object value : iterable) {
                    if (value instanceof Entity entity && uuid.equals(entity.getUUID())) return entity;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private ListTag readOffersFromEntityObject(Object entityLike) {
        if (entityLike == null) return null;
        Object offersObj = invokeAny(entityLike, "getOffers", "getRecipes", "getTrades");
        if (offersObj == null) return null;

        if (offersObj instanceof MerchantOffers merchantOffers && !merchantOffers.isEmpty()) {
            return merchantOffersToList(merchantOffers);
        }

        ListTag direct = invokeListTag(offersObj, "createTag", "toTag", "save");
        if (direct == null || direct.isEmpty()) {
            direct = invokeListTagArg(offersObj, "save", new ListTag());
        }
        if (direct != null && !direct.isEmpty()) {
            return copyListTag(direct);
        }

        ListTag out = new ListTag();
        if (offersObj instanceof Iterable<?> iterable) {
            for (Object offer : iterable) {
                CompoundTag tag = serializeOffer(offer);
                if (tag != null && !tag.isEmpty()) out.add(tag);
            }
            return out;
        }
        if (offersObj instanceof java.util.List<?> list) {
            for (Object offer : list) {
                CompoundTag tag = serializeOffer(offer);
                if (tag != null && !tag.isEmpty()) out.add(tag);
            }
            return out;
        }

        Integer size = invokeInt(offersObj, "size");
        if (size == null || size <= 0) return out;
        for (int i = 0; i < size; i++) {
            Object offer = invokeAny(offersObj, "get", i);
            CompoundTag tag = serializeOffer(offer);
            if (tag != null && !tag.isEmpty()) out.add(tag);
        }
        return out.isEmpty() ? null : out;
    }

    private ListTag extractOfferRecipes(CompoundTag root) {
        if (root == null) return null;
        CompoundTag offers = readCompound(root, "Offers");
        if (offers == null) offers = readCompound(root, "offers");
        if (offers == null) return null;

        Object raw = readTag(offers, "Recipes");
        if (!(raw instanceof ListTag)) raw = readTag(offers, "recipes");
        if (!(raw instanceof ListTag)) raw = readTag(offers, "Trades");
        if (!(raw instanceof ListTag)) raw = readTag(offers, "trades");
        if (raw instanceof ListTag recipes && !recipes.isEmpty()) return copyListTag(recipes);
        return null;
    }

    private ListTag merchantOffersToList(MerchantOffers offers) {
        ListTag out = new ListTag();
        for (MerchantOffer offer : offers) {
            CompoundTag tag = merchantOfferToTag(offer);
            if (tag != null && !tag.isEmpty()) out.add(tag);
        }
        return out;
    }

    private CompoundTag merchantOfferToTag(MerchantOffer offer) {
        if (offer == null) return null;
        CompoundTag buy = stackToTag(offer.getBaseCostA());
        CompoundTag buyB = stackToTag(offer.getCostB());
        CompoundTag sell = stackToTag(offer.getResult());
        if (buy == null || sell == null) return null;

        CompoundTag recipe = new CompoundTag();
        recipe.put("buy", buy);
        recipe.put("base_cost_a", copyCompound(buy));
        recipe.put("sell", sell);
        recipe.put("result", copyCompound(sell));
        if (buyB != null && !buyB.isEmpty()) {
            recipe.put("buyB", buyB);
            recipe.put("cost_b", copyCompound(buyB));
        }
        recipe.putInt("maxUses", Math.max(1, offer.getMaxUses()));
        recipe.putInt("uses", Math.max(0, offer.getUses()));
        recipe.putInt("xp", Math.max(0, offer.getXp()));
        recipe.putFloat("priceMultiplier", offer.getPriceMultiplier());
        recipe.putBoolean("rewardExp", offer.shouldRewardExp());
        recipe.putInt("specialPrice", offer.getSpecialPriceDiff());
        recipe.putInt("demand", offer.getDemand());
        return recipe;
    }

    private CompoundTag serializeOffer(Object offer) {
        if (offer == null) return null;
        if (offer instanceof MerchantOffer merchantOffer) {
            CompoundTag direct = merchantOfferToTag(merchantOffer);
            if (direct != null && !direct.isEmpty()) return direct;
        }

        CompoundTag fromApi = invokeCompound(offer, "createTag");
        if (fromApi == null) fromApi = invokeCompound(offer, "save");
        if (fromApi == null) fromApi = invokeCompoundArg(offer, "save", new CompoundTag());
        if (fromApi == null) fromApi = invokeCompound(offer, "toTag");
        if (fromApi != null && !fromApi.isEmpty()) return fromApi;

        CompoundTag buy = itemLikeToStackTag(invokeAny(offer, "getBaseCostA", "getCostA", "getBuyItem", "getFirstBuyItem"));
        CompoundTag buyB = itemLikeToStackTag(invokeAny(offer, "getCostB", "getSecondCost", "getSecondBuyItem"));
        CompoundTag sell = itemLikeToStackTag(invokeAny(offer, "getResult", "getSellItem", "getOutput"));
        if (buy == null || sell == null) return null;

        CompoundTag recipe = new CompoundTag();
        recipe.put("buy", buy);
        recipe.put("base_cost_a", copyCompound(buy));
        recipe.put("sell", sell);
        recipe.put("result", copyCompound(sell));
        if (buyB != null && !buyB.isEmpty()) {
            recipe.put("buyB", buyB);
            recipe.put("cost_b", copyCompound(buyB));
        }

        Integer maxUses = invokeInt(offer, "getMaxUses");
        if (maxUses != null) recipe.putInt("maxUses", Math.max(1, maxUses));
        Integer uses = invokeInt(offer, "getUses");
        if (uses != null) recipe.putInt("uses", Math.max(0, uses));
        Integer xpVal = invokeInt(offer, "getXp");
        if (xpVal != null) recipe.putInt("xp", Math.max(0, xpVal));
        Float mul = invokeFloat(offer, "getPriceMultiplier");
        if (mul != null) recipe.putFloat("priceMultiplier", mul);
        Boolean reward = invokeBool(offer, "shouldRewardExp");
        if (reward == null) reward = invokeBool(offer, "isRewardExp");
        if (reward != null) recipe.putBoolean("rewardExp", reward);
        return recipe;
    }

    private CompoundTag itemLikeToStackTag(Object itemLike) {
        if (itemLike == null) return null;
        if (itemLike instanceof ItemStack stack) return stackToTag(stack);

        Object stack = invokeAny(itemLike, "itemStack", "stack", "toItemStack", "asStack");
        if (stack instanceof ItemStack st) return stackToTag(st);

        String id = "";
        int count = 1;

        Object itemObj = invokeAny(itemLike, "item", "getItem", "value");
        if (itemObj instanceof Item item) {
            id = ItemRegistryHelper.getItemId(item);
        } else if (itemObj != null) {
            Matcher matcher = ITEM_ID_PATTERN.matcher(String.valueOf(itemObj).toLowerCase(Locale.ROOT));
            if (matcher.find()) id = matcher.group(1);
        }

        Integer c = invokeInt(itemLike, "count");
        if (c == null) c = invokeInt(itemLike, "getCount");
        if (c != null) count = Math.max(1, c);

        if (id.isBlank()) {
            Matcher matcher = ITEM_ID_PATTERN.matcher(String.valueOf(itemLike).toLowerCase(Locale.ROOT));
            if (matcher.find()) id = matcher.group(1);
        }
        if (id.isBlank()) return null;

        CompoundTag out = new CompoundTag();
        out.putString("id", id);
        out.putInt("count", count);
        return out;
    }

    private String extractNamespacedId(Object value) {
        if (value == null) return null;
        try {
            Object out = value.getClass().getMethod("location").invoke(value);
            if (out != null) return String.valueOf(out);
        } catch (Throwable ignored) {}
        try {
            Object out = value.getClass().getMethod("key").invoke(value);
            String id = extractNamespacedId(out);
            if (id != null && !id.isBlank()) return id;
        } catch (Throwable ignored) {}
        try {
            Object out = value.getClass().getMethod("unwrapKey").invoke(value);
            String id = extractNamespacedId(unwrapOptional(out));
            if (id != null && !id.isBlank()) return id;
        } catch (Throwable ignored) {}
        String text = String.valueOf(value).toLowerCase(Locale.ROOT);
        Matcher matcher = ITEM_ID_PATTERN.matcher(text);
        if (matcher.find()) return matcher.group(1);
        return null;
    }

    private CompoundTag stackToTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(stack);
        if (fullOpt.isEmpty()) return null;
        CompoundTag full = fullOpt.get();
        CompoundTag out = new CompoundTag();
        out.putString("id", readString(full, "id", SpawnEggEditorHelper.getItemId(stack)));
        out.putInt("count", Math.max(1, readInt(full, "count", stack.getCount())));
        CompoundTag components = readCompound(full, "components");
        if (components != null && !components.isEmpty()) out.put("components", copyCompound(components));
        CompoundTag legacyTag = readCompound(full, "tag");
        if (legacyTag != null && !legacyTag.isEmpty()) out.put("tag", copyCompound(legacyTag));
        return out;
    }

    private boolean applyTradeToIntegratedServer(Minecraft mc, CompoundTag patch) {
        if (mc == null || targetEntity == null) return false;
        IntegratedServer server;
        try {
            server = mc.getSingleplayerServer();
        } catch (Throwable ignored) {
            return false;
        }
        if (server == null) return false;

        AtomicBoolean success = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        server.execute(() -> {
            try {
                Entity serverEntity = EditorCommandHelper.findIntegratedServerEntity(server, targetEntity.getId(), targetEntity.getUUID());
                if (!(serverEntity instanceof AbstractVillager serverVillager)) return;

                if (serverEntity instanceof Villager villager && !isWanderingTraderContext()) {
                    ServerLevel serverLevel = serverEntity.level() instanceof ServerLevel level ? level : null;
                    applyVillagerData(villager, serverLevel);
                }

                CompoundTag mergedTag = readEntityTag(serverEntity);
                if (mergedTag != null && patch != null && !patch.isEmpty()) {
                    mergedTag.merge(copyCompound(patch));
                    loadEntityTag(serverEntity, mergedTag);
                }

                if (serverEntity instanceof Villager villager && !isWanderingTraderContext()) {
                    ServerLevel serverLevel = serverEntity.level() instanceof ServerLevel level ? level : null;
                    applyVillagerData(villager, serverLevel);
                }

                MerchantOffers offers = buildMerchantOffers();
                if (offers == null || offers.size() != trades.size()) return;
                MerchantOffers live = serverVillager.getOffers();
                live.clear();
                for (MerchantOffer offer : offers) {
                    live.add(offer.copy());
                }
                success.set(live.size() == trades.size());
            } catch (Throwable t) {
                DebugLog.warn("Integrated villager trade apply failed: {}", t.toString());
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(3, TimeUnit.SECONDS)) {
                DebugLog.warn("Timed out waiting for integrated villager trade apply");
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        return success.get();
    }

    private boolean loadEntityTag(Entity entity, CompoundTag tag) {
        if (entity == null || tag == null || tag.isEmpty()) return false;
        Class<?> type = entity.getClass();
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod("load", CompoundTag.class);
                method.setAccessible(true);
                method.invoke(entity, tag);
                return true;
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
            } catch (Throwable ignored) {
                return false;
            }
        }
        return false;
    }

    private void applyTradePreviewToClient() {
        if (targetEntity instanceof Villager villager && !isWanderingTraderContext()) {
            applyVillagerData(villager, null);
        }
    }

    private void applyVillagerData(Villager villager, ServerLevel serverLevel) {
        if (villager == null) return;
        String desiredProfession = normalizeProfessionId(PROFESSIONS[professionIndex]);
        String desiredType = villagerType == null || villagerType.isBlank() ? "minecraft:plains" : villagerType;
        int desiredLevel = Math.max(1, Math.min(5, villagerLevel));
        VillagerData desired = buildVillagerData(villager);
        villager.setVillagerData(desired);
        villager.setVillagerXp(Math.max(0, villagerLevel * 10));
        if (serverLevel != null) {
            invokeCompatible(villager, "refreshBrain", serverLevel);
            if (!matchesVillagerData(villager.getVillagerData(), desiredType, desiredProfession, desiredLevel)) {
                CompoundTag tag = readEntityTag(villager);
                if (tag != null) {
                    CompoundTag villagerDataTag = new CompoundTag();
                    villagerDataTag.putString("type", desiredType);
                    villagerDataTag.putString("profession", desiredProfession);
                    villagerDataTag.putInt("level", desiredLevel);
                    tag.put("VillagerData", villagerDataTag);
                    tag.putInt("Xp", Math.max(0, desiredLevel * 10));
                    if (loadEntityTag(villager, tag)) {
                        villager.setVillagerXp(Math.max(0, desiredLevel * 10));
                        invokeCompatible(villager, "refreshBrain", serverLevel);
                        if (!matchesVillagerData(villager.getVillagerData(), desiredType, desiredProfession, desiredLevel)) {
                            VillagerData rebuilt = buildVillagerData(villager);
                            if (matchesVillagerData(rebuilt, desiredType, desiredProfession, desiredLevel)) {
                                villager.setVillagerData(rebuilt);
                                villager.setVillagerXp(Math.max(0, desiredLevel * 10));
                                invokeCompatible(villager, "refreshBrain", serverLevel);
                            }
                        }
                    }
                }
                if (!matchesVillagerData(villager.getVillagerData(), desiredType, desiredProfession, desiredLevel)
                        && forceVillagerDataByCommand(serverLevel, villager, desiredType, desiredProfession, desiredLevel)) {
                    invokeCompatible(villager, "refreshBrain", serverLevel);
                }
            }
        }
    }

    private boolean forceVillagerDataByCommand(ServerLevel serverLevel, Villager villager, String desiredType, String desiredProfession, int desiredLevel) {
        if (serverLevel == null || villager == null) return false;
        try {
            CompoundTag villagerDataTag = new CompoundTag();
            villagerDataTag.putString("type", desiredType);
            villagerDataTag.putString("profession", desiredProfession);
            villagerDataTag.putInt("level", desiredLevel);
            CompoundTag patch = new CompoundTag();
            patch.put("VillagerData", villagerDataTag);
            patch.putInt("Xp", Math.max(0, desiredLevel * 10));

            String command = "data merge entity " + EditorCommandHelper.selectorByUuid(villager.getUUID()) + " " + patch;
            var source = serverLevel.getServer().createCommandSourceStack();
            try {
                source = source.withPermission(4);
            } catch (Throwable ignored) {}
            try {
                source = source.withSuppressedOutput();
            } catch (Throwable ignored) {}
            serverLevel.getServer().getCommands().performPrefixedCommand(source, command);
            return matchesVillagerData(villager.getVillagerData(), desiredType, desiredProfession, desiredLevel);
        } catch (Throwable t) {
            DebugLog.warn("Villager profession command apply failed: {}", t.toString());
            return false;
        }
    }

    private MerchantOffers buildMerchantOffers() {
        MerchantOffers offers = new MerchantOffers();
        for (TradeData t : trades) {
            ItemStack buy = buildPreviewStack(t.buyId, t.buyComponents, Math.max(1, t.buyCount));
            ItemStack sell = buildPreviewStack(t.sellId, t.sellComponents, Math.max(1, t.sellCount));
            if (buy.isEmpty() || sell.isEmpty()) return null;

            ItemCost firstCost = toItemCost(buy);
            if (firstCost == null) return null;

            Optional<ItemCost> secondCost = Optional.empty();
            if (!t.buy2Id.isBlank()) {
                ItemStack buy2 = buildPreviewStack(t.buy2Id, t.buy2Components, Math.max(1, t.buy2Count));
                ItemCost extraCost = toItemCost(buy2);
                if (extraCost == null) return null;
                secondCost = Optional.of(extraCost);
            }

            MerchantOffer offer = createMerchantOffer(firstCost, secondCost, sell.copy(), t);
            if (offer == null) return null;
            offers.add(offer);
        }
        return offers;
    }

    private MerchantOffer createMerchantOffer(ItemCost firstCost, Optional<ItemCost> secondCost, ItemStack sell, TradeData trade) {
        try {
            Constructor<MerchantOffer> ctor = MerchantOffer.class.getDeclaredConstructor(
                    ItemCost.class, Optional.class, ItemStack.class,
                    int.class, int.class, boolean.class, int.class, int.class, float.class, int.class);
            ctor.setAccessible(true);
            return ctor.newInstance(firstCost, secondCost, sell,
                    Math.max(0, trade.uses), Math.max(1, trade.maxUses), trade.rewardExp,
                    trade.specialPrice, trade.demand, trade.priceMultiplier, Math.max(0, trade.xp));
        } catch (Throwable ignored) {
            try {
                MerchantOffer offer = new MerchantOffer(
                        firstCost,
                        secondCost,
                        sell,
                        Math.max(0, trade.uses),
                        Math.max(1, trade.maxUses),
                        Math.max(0, trade.xp),
                        trade.priceMultiplier,
                        trade.demand
                );
                offer.setSpecialPriceDiff(trade.specialPrice);
                return offer;
            } catch (Throwable t) {
                DebugLog.warn("Villager offer reconstruction failed: {}", t.toString());
                return null;
            }
        }
    }

    private ItemCost toItemCost(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Object predicate = buildItemCostPredicate(stack);
        if (predicate != null) {
            for (Constructor<?> ctor : ItemCost.class.getConstructors()) {
                Class<?>[] p = ctor.getParameterTypes();
                Object itemArg = p.length >= 1 ? resolveItemCostItemArg(p[0], stack) : null;
                if (itemArg == null || p.length < 3 || p[1] != int.class || !p[2].isInstance(predicate)) continue;
                try {
                    if (p.length == 4 && p[3].isInstance(stack)) {
                        Object out = ctor.newInstance(itemArg, stack.getCount(), predicate, stack.copy());
                        if (out instanceof ItemCost itemCost) return itemCost;
                    } else if (p.length == 3) {
                        Object out = ctor.newInstance(itemArg, stack.getCount(), predicate);
                        if (out instanceof ItemCost itemCost) return itemCost;
                    }
                } catch (Throwable ignored) {}
            }
        }
        return new ItemCost(stack.getItem(), stack.getCount());
    }

    private Object buildItemCostPredicate(ItemStack stack) {
        Object predicate = tryBuildItemCostPredicate("net.minecraft.core.component.DataComponentExactPredicate", stack);
        if (predicate != null) return predicate;
        return tryBuildItemCostPredicate("net.minecraft.core.component.DataComponentPredicate", stack);
    }

    private Object tryBuildItemCostPredicate(String className, ItemStack stack) {
        try {
            Class<?> predicateClass = Class.forName(className);
            if (stack.getComponents().isEmpty()) {
                try {
                    return predicateClass.getField("EMPTY").get(null);
                } catch (Throwable ignored) {}
                try {
                    Object builder = predicateClass.getMethod("builder").invoke(null);
                    return builder.getClass().getMethod("build").invoke(builder);
                } catch (Throwable ignored) {}
                return null;
            }
            Class<?> componentMapClass = Class.forName("net.minecraft.core.component.DataComponentMap");
            return predicateClass.getMethod("allOf", componentMapClass).invoke(null, stack.getComponents());
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object resolveItemCostItemArg(Class<?> paramType, ItemStack stack) {
        try {
            if (paramType.isInstance(stack.getItemHolder())) return stack.getItemHolder();
        } catch (Throwable ignored) {}
        return paramType.isInstance(stack.getItem()) ? stack.getItem() : null;
    }

    private VillagerData buildVillagerData(Villager villager) {
        VillagerData current = villager.getVillagerData();
        Object currentType = invokeAny(current, "type", "getType");
        Object currentProfession = invokeAny(current, "profession", "getProfession");
        String desiredType = villagerType == null || villagerType.isBlank() ? "minecraft:plains" : villagerType;
        String desiredProfession = normalizeProfessionId(PROFESSIONS[professionIndex]);
        Object type = resolveRegistryEntry(BuiltInRegistries.VILLAGER_TYPE, desiredType, currentType, "minecraft:plains");
        Object profession = resolveRegistryEntry(BuiltInRegistries.VILLAGER_PROFESSION, desiredProfession, currentProfession, "minecraft:farmer");
        int level = Math.max(1, Math.min(5, villagerLevel));
        List<Object> typeCandidates = registryCandidates(BuiltInRegistries.VILLAGER_TYPE, type, currentType);
        List<Object> professionCandidates = registryCandidates(BuiltInRegistries.VILLAGER_PROFESSION, profession, currentProfession);

        Object updated = current;
        Object next = invokeCompatibleCandidates(updated, "withType", typeCandidates);
        if (next == null) next = invokeCompatibleCandidates(updated, "setType", typeCandidates);
        if (next != null) updated = next;
        next = invokeCompatibleCandidates(updated, "withProfession", professionCandidates);
        if (next == null) next = invokeCompatibleCandidates(updated, "setProfession", professionCandidates);
        if (next != null) updated = next;
        next = invokeCompatible(updated, "withLevel", Integer.valueOf(level));
        if (next == null) next = invokeCompatible(updated, "setLevel", Integer.valueOf(level));
        if (next instanceof VillagerData data && matchesVillagerData(data, desiredType, desiredProfession, level)) {
            return data;
        }
        if (updated instanceof VillagerData data && matchesVillagerData(data, desiredType, desiredProfession, level)) {
            return data;
        }
        try {
            for (Constructor<?> ctor : VillagerData.class.getConstructors()) {
                Class<?>[] p = ctor.getParameterTypes();
                if (p.length != 3 || p[2] != int.class) continue;
                for (Object typeCandidate : typeCandidates) {
                    if (typeCandidate == null || !p[0].isInstance(typeCandidate)) continue;
                    for (Object professionCandidate : professionCandidates) {
                        if (professionCandidate == null || !p[1].isInstance(professionCandidate)) continue;
                        Object out = ctor.newInstance(typeCandidate, professionCandidate, level);
                        if (out instanceof VillagerData data && matchesVillagerData(data, desiredType, desiredProfession, level)) {
                            return data;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return current;
    }

    private boolean matchesVillagerData(VillagerData data, String expectedType, String expectedProfession, int expectedLevel) {
        if (data == null) return false;
        String actualType = registryEntryId(BuiltInRegistries.VILLAGER_TYPE, invokeAny(data, "type", "getType"));
        String actualProfession = registryEntryId(BuiltInRegistries.VILLAGER_PROFESSION, invokeAny(data, "profession", "getProfession"));
        Integer actualLevel = invokeInt(data, "getLevel");
        if (actualLevel == null) actualLevel = invokeInt(data, "level");
        return Objects.equals(expectedType, actualType)
                && Objects.equals(expectedProfession, actualProfession)
                && actualLevel != null
                && actualLevel == expectedLevel;
    }

    private String registryEntryId(Object registry, Object entry) {
        if (entry == null) return null;
        Object raw = unwrapHolderValue(entry);
        if (raw == null) raw = entry;
        for (String method : List.of("getKey", "getId")) {
            Object id = invokeCompatible(registry, method, raw);
            String resolved = extractNamespacedId(id);
            if (resolved != null && !resolved.isBlank()) return resolved;
        }
        return extractNamespacedId(entry);
    }

    private Object resolveRegistryEntry(Object registry, String id, Object fallback, String defaultId) {
        if (registry == null) return fallback;
        String rawId = id == null || id.isBlank() ? defaultId : id;
        ResourceLocation loc = ResourceLocation.tryParse(rawId);
        if (loc == null) return fallback;
        Object value = null;
        try {
            Object holder = registry.getClass().getMethod("getHolder", ResourceLocation.class).invoke(registry, loc);
            holder = unwrapOptional(holder);
            if (holder != null) return holder;
        } catch (Throwable ignored) {}
        try {
            value = registry.getClass().getMethod("get", ResourceLocation.class).invoke(registry, loc);
            value = unwrapOptional(value);
            if (isHolderLike(value)) return value;
        } catch (Throwable ignored) {}
        if (value == null) {
            try {
                value = registry.getClass().getMethod("getValue", ResourceLocation.class).invoke(registry, loc);
                value = unwrapOptional(value);
            } catch (Throwable ignored) {}
        }
        Object holder = wrapAsHolder(registry, value);
        if (holder != null) return holder;
        return value != null ? value : fallback;
    }

    private Object invokeAny(Object target, String... methodNames) {
        if (target == null || methodNames == null) return null;
        for (String method : methodNames) {
            try {
                return target.getClass().getMethod(method).invoke(target);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private Object invokeAny(Object target, String method, int arg) {
        try {
            return target.getClass().getMethod(method, int.class).invoke(target, arg);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private CompoundTag invokeCompound(Object target, String method) {
        try {
            Object out = target.getClass().getMethod(method).invoke(target);
            return out instanceof CompoundTag ct ? ct : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private CompoundTag invokeCompoundArg(Object target, String method, CompoundTag arg) {
        try {
            Object out = target.getClass().getMethod(method, CompoundTag.class).invoke(target, arg);
            return out instanceof CompoundTag ct ? ct : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private ListTag invokeListTag(Object target, String... methods) {
        if (target == null || methods == null) return null;
        for (String method : methods) {
            try {
                Object out = target.getClass().getMethod(method).invoke(target);
                if (out instanceof ListTag lt) return lt;
                if (out instanceof Optional<?> opt && opt.orElse(null) instanceof ListTag lt) return lt;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private ListTag invokeListTagArg(Object target, String method, ListTag arg) {
        if (target == null || method == null || method.isBlank()) return null;
        try {
            Object out = target.getClass().getMethod(method, ListTag.class).invoke(target, arg);
            if (out instanceof ListTag lt) return lt;
            if (out instanceof Optional<?> opt && opt.orElse(null) instanceof ListTag lt) return lt;
            return arg;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Integer invokeInt(Object target, String method) {
        try {
            Object out = target.getClass().getMethod(method).invoke(target);
            return out instanceof Number n ? n.intValue() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object invokeCompatible(Object target, String method, Object arg) {
        if (target == null || method == null || method.isBlank()) return null;
        for (Method candidate : target.getClass().getMethods()) {
            if (!candidate.getName().equals(method) || candidate.getParameterCount() != 1) continue;
            Class<?> parameter = candidate.getParameterTypes()[0];
            if (!isCompatible(parameter, arg)) continue;
            try {
                return candidate.invoke(target, coerceArgument(parameter, arg));
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private Object wrapAsHolder(Object registry, Object value) {
        if (registry == null || value == null) return null;
        for (Method candidate : registry.getClass().getMethods()) {
            if (!"wrapAsHolder".equals(candidate.getName()) || candidate.getParameterCount() != 1) continue;
            Class<?> parameter = candidate.getParameterTypes()[0];
            if (!isCompatible(parameter, value)) continue;
            try {
                Object out = candidate.invoke(registry, coerceArgument(parameter, value));
                out = unwrapOptional(out);
                if (out != null) return out;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private boolean isHolderLike(Object value) {
        return value != null && value.getClass().getName().contains(".Holder");
    }

    private boolean isCompatible(Class<?> parameter, Object arg) {
        if (parameter == null) return false;
        if (arg == null) return !parameter.isPrimitive();
        if (parameter.isInstance(arg)) return true;
        if (!parameter.isPrimitive()) return false;
        return (parameter == int.class && arg instanceof Number)
                || (parameter == boolean.class && arg instanceof Boolean)
                || (parameter == float.class && arg instanceof Number)
                || (parameter == double.class && arg instanceof Number)
                || (parameter == long.class && arg instanceof Number)
                || (parameter == short.class && arg instanceof Number)
                || (parameter == byte.class && arg instanceof Number);
    }

    private Object coerceArgument(Class<?> parameter, Object arg) {
        if (!parameter.isPrimitive() || arg == null) return arg;
        Number number = arg instanceof Number n ? n : null;
        if (parameter == int.class && number != null) return number.intValue();
        if (parameter == float.class && number != null) return number.floatValue();
        if (parameter == double.class && number != null) return number.doubleValue();
        if (parameter == long.class && number != null) return number.longValue();
        if (parameter == short.class && number != null) return number.shortValue();
        if (parameter == byte.class && number != null) return number.byteValue();
        if (parameter == boolean.class && arg instanceof Boolean bool) return bool;
        return arg;
    }

    private List<Object> registryCandidates(Object registry, Object primary, Object fallback) {
        List<Object> candidates = new ArrayList<>();
        addCandidate(candidates, primary);
        addCandidate(candidates, unwrapHolderValue(primary));
        addCandidate(candidates, wrapAsHolder(registry, primary));
        addCandidate(candidates, fallback);
        addCandidate(candidates, unwrapHolderValue(fallback));
        addCandidate(candidates, wrapAsHolder(registry, fallback));
        return candidates;
    }

    private void addCandidate(List<Object> candidates, Object value) {
        if (value == null || candidates == null) return;
        for (Object candidate : candidates) {
            if (candidate == value || Objects.equals(candidate, value)) {
                return;
            }
        }
        candidates.add(value);
    }

    private Object unwrapHolderValue(Object value) {
        if (value == null) return null;
        try {
            Object out = value.getClass().getMethod("value").invoke(value);
            out = unwrapOptional(out);
            if (out != null) return out;
        } catch (Throwable ignored) {}
        try {
            Object out = value.getClass().getMethod("getValue").invoke(value);
            out = unwrapOptional(out);
            if (out != null) return out;
        } catch (Throwable ignored) {}
        return null;
    }

    private Object invokeCompatibleCandidates(Object target, String method, List<Object> candidates) {
        if (candidates == null) return null;
        for (Object candidate : candidates) {
            Object out = invokeCompatible(target, method, candidate);
            if (out != null) return out;
        }
        return null;
    }

    private Float invokeFloat(Object target, String method) {
        try {
            Object out = target.getClass().getMethod(method).invoke(target);
            return out instanceof Number n ? n.floatValue() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Boolean invokeBool(Object target, String method) {
        try {
            Object out = target.getClass().getMethod(method).invoke(target);
            return out instanceof Boolean b ? b : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String onOff(boolean v) {
        return v ? tr("ankinbt.simple.on") : tr("ankinbt.simple.off");
    }

    private String tr(String key) {
        return Component.translatable(key).getString();
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override
    public void onClose() {
        tryClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class TradeData {
        String buyId;
        int buyCount;
        CompoundTag buyComponents;
        String buy2Id;
        int buy2Count;
        CompoundTag buy2Components;
        String sellId;
        int sellCount;
        CompoundTag sellComponents;
        int maxUses;
        int xp;
        int uses;
        int specialPrice;
        int demand;
        float priceMultiplier;
        boolean rewardExp;
        CompoundTag recipeTemplate;
        TradeDraft draft;

        static TradeData defaults() {
            TradeData t = new TradeData();
            t.buyId = "minecraft:emerald";
            t.buyCount = 1;
            t.buyComponents = null;
            t.buy2Id = "";
            t.buy2Count = 1;
            t.buy2Components = null;
            t.sellId = "minecraft:bread";
            t.sellCount = 6;
            t.sellComponents = null;
            t.maxUses = 12;
            t.xp = 1;
            t.uses = 0;
            t.specialPrice = 0;
            t.demand = 0;
            t.priceMultiplier = 0.0f;
            t.rewardExp = true;
            t.recipeTemplate = null;
            t.draft = null;
            return t;
        }

        static TradeData blank() {
            TradeData t = defaults();
            t.buyComponents = null;
            t.buy2Components = null;
            t.sellComponents = null;
            return t;
        }

        TradeData copy() {
            TradeData t = new TradeData();
            t.buyId = buyId;
            t.buyCount = buyCount;
            t.buyComponents = copyTag(buyComponents);
            t.buy2Id = buy2Id;
            t.buy2Count = buy2Count;
            t.buy2Components = copyTag(buy2Components);
            t.sellId = sellId;
            t.sellCount = sellCount;
            t.sellComponents = copyTag(sellComponents);
            t.maxUses = maxUses;
            t.xp = xp;
            t.uses = uses;
            t.specialPrice = specialPrice;
            t.demand = demand;
            t.priceMultiplier = priceMultiplier;
            t.rewardExp = rewardExp;
            t.recipeTemplate = copyTag(recipeTemplate);
            t.draft = draft == null ? null : draft.copy();
            return t;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TradeData other)) return false;
            return buyCount == other.buyCount
                    && buy2Count == other.buy2Count
                    && sellCount == other.sellCount
                    && maxUses == other.maxUses
                    && xp == other.xp
                    && uses == other.uses
                    && specialPrice == other.specialPrice
                    && demand == other.demand
                    && Float.compare(priceMultiplier, other.priceMultiplier) == 0
                    && rewardExp == other.rewardExp
                    && Objects.equals(buyId, other.buyId)
                    && Objects.equals(compKey(buyComponents), compKey(other.buyComponents))
                    && Objects.equals(buy2Id, other.buy2Id)
                    && Objects.equals(compKey(buy2Components), compKey(other.buy2Components))
                    && Objects.equals(sellId, other.sellId)
                    && Objects.equals(compKey(sellComponents), compKey(other.sellComponents))
                    && Objects.equals(compKey(recipeTemplate), compKey(other.recipeTemplate))
                    && Objects.equals(draft, other.draft);
        }

        @Override
        public int hashCode() {
            return Objects.hash(buyId, buyCount, compKey(buyComponents), buy2Id, buy2Count, compKey(buy2Components),
                    sellId, sellCount, compKey(sellComponents), maxUses, xp, uses, specialPrice, demand,
                    priceMultiplier, rewardExp, compKey(recipeTemplate), draft);
        }

        private static String compKey(CompoundTag tag) {
            return tag == null ? "" : tag.toString();
        }

        private static CompoundTag copyTag(CompoundTag tag) {
            if (tag == null) return null;
            CompoundTag out = new CompoundTag();
            out.merge(tag);
            return out;
        }
    }

    private static final class TradeDraft {
        final String buyId;
        final String buyCount;
        final String buy2Id;
        final String buy2Count;
        final String sellId;
        final String sellCount;
        final String maxUses;
        final String xp;

        TradeDraft(String buyId, String buyCount, String buy2Id, String buy2Count,
                   String sellId, String sellCount, String maxUses, String xp) {
            this.buyId = buyId == null ? "" : buyId;
            this.buyCount = buyCount == null ? "" : buyCount;
            this.buy2Id = buy2Id == null ? "" : buy2Id;
            this.buy2Count = buy2Count == null ? "" : buy2Count;
            this.sellId = sellId == null ? "" : sellId;
            this.sellCount = sellCount == null ? "" : sellCount;
            this.maxUses = maxUses == null ? "" : maxUses;
            this.xp = xp == null ? "" : xp;
        }

        static TradeDraft from(TradeData trade) {
            TradeData value = trade == null ? TradeData.blank() : trade;
            return new TradeDraft(value.buyId, String.valueOf(value.buyCount),
                    value.buy2Id, String.valueOf(value.buy2Count),
                    value.sellId, String.valueOf(value.sellCount),
                    String.valueOf(value.maxUses), String.valueOf(value.xp));
        }

        TradeDraft copy() {
            return new TradeDraft(buyId, buyCount, buy2Id, buy2Count, sellId, sellCount, maxUses, xp);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TradeDraft other)) return false;
            return Objects.equals(buyId, other.buyId)
                    && Objects.equals(buyCount, other.buyCount)
                    && Objects.equals(buy2Id, other.buy2Id)
                    && Objects.equals(buy2Count, other.buy2Count)
                    && Objects.equals(sellId, other.sellId)
                    && Objects.equals(sellCount, other.sellCount)
                    && Objects.equals(maxUses, other.maxUses)
                    && Objects.equals(xp, other.xp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(buyId, buyCount, buy2Id, buy2Count, sellId, sellCount, maxUses, xp);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (keyPressed(event.key(), event.scancode(), event.modifiers())) return true;
        return super.keyPressed(event);
    }
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (handleEditBoxChar(event)) return true;
        return super.charTyped(event);
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return mouseClicked(event.x(), event.y(), event.button());
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (resizingEditor && event.button() == 0) {
            updateEditorScale(event.x());
            return true;
        }
        if (draggingPanel && event.button() == 0) {
            applyMenuLayout(EditorDock.menuLayoutAt(width, height, 306, false,
                    (int) Math.round(event.x()) - panelDragOffsetX,
                    (int) Math.round(event.y()) - panelDragOffsetY, editorScale,
                    editorWidthAdjustment, editorHeightAdjustment));
            rebuildButtons();
            return true;
        }
        if (dragTradeIndex >= 0 && event.button() == 0) {
            updateTradeDropTarget(event.x(), event.y());
            return true;
        }
        if (event.button() == 0 && getFocused() instanceof EditBox box && box.isFocused()) {
            return box.mouseDragged(event, dragX, dragY);
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (resizingEditor && event.button() == 0) {
            resizingEditor = false;
            AnkiConfig.setVillagerEditorScale(editorScale);
            return true;
        }
        if (draggingPanel && event.button() == 0) {
            draggingPanel = false;
            return true;
        }
        if (dragTradeIndex >= 0) {
            finishTradeDrag();
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
        applyMenuLayout(EditorDock.resizeLayout(width, height, false, current, 306, editorScale,
                editorWidthAdjustment, editorHeightAdjustment));
        tradeScroll = Math.max(0, Math.min(tradeScroll, tradeScrollMax));
        contentScroll = Math.max(0, Math.min(contentScroll, contentScrollMax));
        tradeNavScroll = Math.max(0, Math.min(tradeNavScroll, tradeNavScrollMax));
        rebuildButtons();
        if (save) AnkiConfig.setVillagerEditorScale(editorScale);
    }

    private void adjustEditorAxes(float widthDelta, float heightDelta) {
        editorWidthAdjustment = EditorDock.adjustAxis(editorWidthAdjustment, widthDelta);
        editorHeightAdjustment = EditorDock.adjustAxis(editorHeightAdjustment, heightDelta);
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, false, current, 306, editorScale,
                editorWidthAdjustment, editorHeightAdjustment));
        tradeScroll = Math.max(0, Math.min(tradeScroll, tradeScrollMax));
        contentScroll = Math.max(0, Math.min(contentScroll, contentScrollMax));
        tradeNavScroll = Math.max(0, Math.min(tradeNavScroll, tradeNavScrollMax));
        rebuildButtons();
        AnkiConfig.setVillagerEditorAxisAdjustments(editorWidthAdjustment, editorHeightAdjustment);
    }

    private void resetEditorSize() {
        AnkiConfig.resetVillagerEditorSizeToItem();
        editorScale = AnkiConfig.getVillagerEditorScale();
        editorWidthAdjustment = AnkiConfig.getVillagerEditorWidthAdjustment();
        editorHeightAdjustment = AnkiConfig.getVillagerEditorHeightAdjustment();
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, false, current, 306, editorScale,
                editorWidthAdjustment, editorHeightAdjustment));
        tradeScroll = Math.max(0, Math.min(tradeScroll, tradeScrollMax));
        contentScroll = Math.max(0, Math.min(contentScroll, contentScrollMax));
        tradeNavScroll = Math.max(0, Math.min(tradeNavScroll, tradeNavScrollMax));
        rebuildButtons();
    }

    private record StateSnapshot(
            List<TradeData> trades,
            int tradeIndex,
            int professionIndex,
            int villagerLevel,
            boolean rewardExp,
            String villagerType,
            boolean dirty
    ) {}

    private enum InvPickTarget {
        NONE, BUY, BUY2, SELL
    }

    private enum RightPage {
        TRADE, META
    }

    private enum VillagerTab {
        TRADE("ankinbt.villager.tab.trade", UiIcons.REPEAT),
        BUY("ankinbt.villager.tab.buy", UiIcons.CART),
        SELL("ankinbt.villager.tab.sell", UiIcons.BOX),
        VILLAGER("ankinbt.villager.tab.villager", UiIcons.USER),
        ENTITY("ankinbt.villager.tab.entity", UiIcons.ACTIVITY);

        final String translationKey;
        final String icon;

        VillagerTab(String translationKey, String icon) {
            this.translationKey = translationKey;
            this.icon = icon;
        }
    }

    private enum TradeTool {
        NEW(UiIcons.PLUS, "ankinbt.villager.new_trade"),
        COPY(UiIcons.COPY, "ankinbt.villager.copy_trade"),
        DELETE(UiIcons.TRASH, "ankinbt.villager.remove"),
        LEFT(UiIcons.CHEVRON_LEFT, "ankinbt.villager.move_left"),
        RIGHT(UiIcons.CHEVRON_RIGHT, "ankinbt.villager.move_right");

        final String glyph;
        final String tooltipKey;

        TradeTool(String glyph, String tooltipKey) {
            this.glyph = glyph;
            this.tooltipKey = tooltipKey;
        }
    }

    private record LoadedVillagerDefaults(
            int professionIndex,
            int villagerLevel,
            String villagerType,
            boolean rewardExp,
            List<TradeData> trades
    ) {}

    private record IconHit(int x, int y, int w, int h, InvPickTarget target) {
        boolean hit(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private record TradeCardHit(int x, int y, int w, int h, int index) {
        boolean hit(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private record InvSlotHit(int x, int y, int w, int h, String itemId, ItemStack stack) {
        boolean hit(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
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
        final int style;
        float hoverAnim;

        UiBtn(int x, int y, int w, int h, Supplier<String> label, Runnable action, boolean enabled, Supplier<Boolean> selected) {
            this(x, y, w, h, label, action, enabled, selected, 0);
        }

        UiBtn(int x, int y, int w, int h, Supplier<String> label, Runnable action, boolean enabled, Supplier<Boolean> selected, int style) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
            this.action = action;
            this.enabled = enabled;
            this.selected = selected;
            this.style = style;
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
            float speed = AnkiConfig.isUiAnimationEnabled()
                    ? Math.max(0.18f, AnkiConfig.getUiAnimationSpeed() * 2.4f) : 1f;
            hoverAnim = UiTheme.approach(hoverAnim, hover ? 1f : 0f, speed);
            String optionStyle = AnkiConfig.getUiOptionStyle();

            int bg;
            int edge;
            if (!enabled) {
                bg = UiTheme.withAlpha(UiTheme.baseRgb(), 24);
                edge = UiTheme.themedBorder(0.55f, 1f);
            } else if (style == 1) {
                bg = UiTheme.mix(0x8A166534, 0xAA14532D, hoverAnim);
                edge = 0xFF22C55E;
            } else if (style == -1) {
                bg = UiTheme.mix(0x8A991B1B, 0xAA7F1D1D, hoverAnim);
                edge = 0xFFEF4444;
            } else {
                int base = "compact".equals(optionStyle)
                        ? UiTheme.withAlpha(UiTheme.baseRgb(), 22)
                        : UiTheme.card(AnkiConfig.getUiOpacity(), 1f);
                int hovered = UiTheme.withAlpha(accent & 0x00FFFFFF, 54);
                bg = chosen ? UiTheme.withAlpha(accent & 0x00FFFFFF, 68)
                        : UiTheme.mix(base, hovered, hoverAnim);
                edge = chosen ? accent : UiTheme.themedBorder(AnkiConfig.getUiOpacity(), 1f);
            }
            int color = enabled ? TXT_MAIN : TXT_DIM;

            g.fill(x, y, x + w, y + h, bg);
            if ("rows".equals(optionStyle) && style == 0) {
                g.fill(x, y + h - 1, x + w, y + h, edge);
            } else if ("compact".equals(optionStyle) && style == 0) {
                g.fill(x, y, x + (hover ? 2 : 1), y + h, edge);
            } else {
                g.fill(x, y, x + w, y + 1, edge);
                g.fill(x, y + h - 1, x + w, y + h, edge);
                g.fill(x, y, x + 1, y + h, edge);
                g.fill(x + w - 1, y, x + w, y + h, edge);
            }

            String text = label.get();
            if (font.width(text) > w - 10) text = font.plainSubstrByWidth(text, w - 14) + "..";
            int tx = w <= 24 ? x + (w - font.width(text)) / 2 : x + 6;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, tx,
                    y + Math.max(3, (h - 8) / 2), color, false);
        }
    }
}
