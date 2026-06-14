/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.class_1132
 *  net.minecraft.class_1297
 *  net.minecraft.class_1646
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1914
 *  net.minecraft.class_1916
 *  net.minecraft.class_1935
 *  net.minecraft.class_1937
 *  net.minecraft.class_2168
 *  net.minecraft.class_2487
 *  net.minecraft.class_2499
 *  net.minecraft.class_2520
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3218
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_364
 *  net.minecraft.class_3850
 *  net.minecraft.class_3988
 *  net.minecraft.class_437
 *  net.minecraft.class_5250
 *  net.minecraft.class_5251
 *  net.minecraft.class_7923
 *  net.minecraft.class_9290
 *  net.minecraft.class_9306
 *  net.minecraft.class_9334
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.gui.EntityEditorScreen;
import com.ankinbt.gui.ItemPickerScreen;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.util.DebugLog;
import com.ankinbt.util.ItemRegistryHelper;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1132;
import net.minecraft.class_1297;
import net.minecraft.class_1646;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1914;
import net.minecraft.class_1916;
import net.minecraft.class_1935;
import net.minecraft.class_1937;
import net.minecraft.class_2168;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3218;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_3850;
import net.minecraft.class_3988;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import net.minecraft.class_7923;
import net.minecraft.class_9290;
import net.minecraft.class_9306;
import net.minecraft.class_9334;

public class VillagerTradeEditorScreen
extends class_437 {
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");
    private static final int TRADE_FIELD_COUNT = 8;
    private static final int TRADE_FIELD_BOX_HEIGHT = 20;
    private static final String FULL_STACK_KEY = "__ankinbt_full_stack";
    private static final int TXT_TITLE = -788737;
    private static final int TXT_MAIN = -2497806;
    private static final int TXT_DIM = -7429177;
    private static final int TXT_OK = -13315175;
    private static final int TXT_ERR = -1096636;
    private static final int SIMPLE_C1 = -1906448;
    private static final int SIMPLE_C2 = -7035976;
    private static final int SIMPLE_C3 = -10193781;
    private static final int SIMPLE_BORDER = -14540234;
    private static final int SIMPLE_BTN_BG = 0x30FFFFFF;
    private static final int SIMPLE_BTN_HOVER = 0x50FFFFFF;
    private static final int SIMPLE_SUCCESS = -14498466;
    private static final String[] PROFESSIONS = new String[]{"", "minecraft:farmer", "minecraft:librarian", "minecraft:cleric", "minecraft:armorer", "minecraft:toolsmith", "minecraft:weaponsmith", "minecraft:fletcher", "minecraft:cartographer", "minecraft:butcher", "minecraft:leatherworker", "minecraft:mason", "minecraft:shepherd", "minecraft:fisherman", "minecraft:unemployed", "minecraft:nitwit"};
    private final class_1297 targetEntity;
    private final class_1799 sourceStack;
    private final int inventorySlot;
    private final class_437 parent;
    private final List<UiBtn> buttons = new ArrayList<UiBtn>();
    private class_342 buyId;
    private class_342 buyCount;
    private class_342 buy2Id;
    private class_342 buy2Count;
    private class_342 sellId;
    private class_342 sellCount;
    private class_342 maxUses;
    private class_342 xp;
    private final List<TradeData> trades = new ArrayList<TradeData>();
    private int tradeIndex = 0;
    private int professionIndex = 1;
    private int villagerLevel = 1;
    private boolean rewardExp = true;
    private String villagerType = "minecraft:plains";
    private boolean dirty = false;
    private boolean confirmClose = false;
    private boolean confirmReset = false;
    private final List<StateSnapshot> undoStack = new ArrayList<StateSnapshot>();
    private static final int MAX_UNDO = 50;
    private static final Map<UUID, class_2487> ENTITY_PATCH_CACHE = new HashMap<UUID, class_2487>();
    private final List<IconHit> iconHits = new ArrayList<IconHit>();
    private final List<InvSlotHit> invSlotHits = new ArrayList<InvSlotHit>();
    private final Map<String, class_1792> itemCache = new HashMap<String, class_1792>();
    private InvPickTarget invPickTarget = InvPickTarget.NONE;
    private class_2561 status = class_2561.method_43473();
    private int statusColor = -7429177;
    private long statusTime = 0L;
    private int px;
    private int py;
    private int pw;
    private int ph;
    private float openAnim = 0.0f;
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

    private VillagerTradeEditorScreen(class_1297 targetEntity, class_1799 sourceStack, int inventorySlot, class_437 parent) {
        super((class_2561)class_2561.method_43471((String)"ankinbt.villager.title"));
        this.targetEntity = targetEntity;
        this.sourceStack = sourceStack == null ? class_1799.field_8037 : sourceStack.method_7972();
        this.inventorySlot = inventorySlot;
        this.parent = parent;
    }

    public static VillagerTradeEditorScreen forEntity(class_1297 entity) {
        return new VillagerTradeEditorScreen(entity, class_1799.field_8037, -1, null);
    }

    public static VillagerTradeEditorScreen forEntity(class_1297 entity, class_437 parent) {
        return new VillagerTradeEditorScreen(entity, class_1799.field_8037, -1, parent);
    }

    public static VillagerTradeEditorScreen forSpawnEgg(class_1799 stack, int inventorySlot) {
        return new VillagerTradeEditorScreen(null, stack, inventorySlot, null);
    }

    public static VillagerTradeEditorScreen forSpawnEgg(class_1799 stack, int inventorySlot, class_437 parent) {
        return new VillagerTradeEditorScreen(null, stack, inventorySlot, parent);
    }

    protected void method_25426() {
        this.recalcBounds();
        int inputX = this.tradeFieldInputX();
        int inputW = this.tradeFieldInputWidth();
        int row = this.tradeFieldRowGap();
        int fieldY = this.tradeFieldStartY();
        this.buyId = this.box(inputX, fieldY, inputW, "minecraft:emerald");
        this.buyCount = this.box(inputX, fieldY + row, inputW, "1");
        this.buy2Id = this.box(inputX, fieldY + row * 2, inputW, "");
        this.buy2Count = this.box(inputX, fieldY + row * 3, inputW, "1");
        this.sellId = this.box(inputX, fieldY + row * 4, inputW, "minecraft:bread");
        this.sellCount = this.box(inputX, fieldY + row * 5, inputW, "6");
        this.maxUses = this.box(inputX, fieldY + row * 6, inputW, "9999999");
        this.xp = this.box(inputX, fieldY + row * 7, inputW, "1");
        this.updateTradeFieldLayout();
        if (!this.initializedFromContext) {
            this.readContextDefaults();
            this.ensureTrades();
            this.initializedFromContext = true;
            this.dirty = false;
        } else {
            this.ensureTrades();
        }
        this.loadTradeToForm(this.tradeIndex);
        this.rebuildButtons();
        if (this.undoStack.isEmpty()) {
            this.undoStack.add(this.captureState());
        }
    }

    private void recalcBounds() {
        this.pw = Math.min(860, this.field_22789 - 20);
        this.ph = Math.min(480, this.field_22790 - 20);
        this.px = (this.field_22789 - this.pw) / 2;
        this.py = (this.field_22790 - this.ph) / 2;
    }

    private int tradeFieldLeft() {
        return this.px + 48;
    }

    private int tradeFieldRight() {
        int leftCardRight = this.px + this.pw / 2 - 12;
        return leftCardRight - (AnkiConfig.isUiCompactLayout() ? 22 : 28);
    }

    private int tradeFieldLabelWidth() {
        return AnkiConfig.isUiCompactLayout() ? 112 : 122;
    }

    private int tradeFieldInputX() {
        return this.tradeFieldLeft() + this.tradeFieldLabelWidth() + (AnkiConfig.isUiCompactLayout() ? 8 : 12);
    }

    private int tradeFieldInputWidth() {
        return Math.max(104, this.tradeFieldRight() - this.tradeFieldInputX());
    }

    private int tradeFieldRowGap() {
        int minGap = AnkiConfig.isUiCompactLayout() ? 2 : 3;
        int maxGap = AnkiConfig.isUiCompactLayout() ? 5 : 6;
        int usable = this.tradeCardBottomY() - this.tradeFieldStartY() - this.tradeFieldBottomPadding() - 160;
        int gap = usable / Math.max(1, 7);
        return 20 + Math.max(minGap, Math.min(maxGap, gap));
    }

    private int tradeFieldStartY() {
        int minTop = this.py + (AnkiConfig.isUiCompactLayout() ? 120 : 124);
        int iconBottom = this.py + 94 + 18;
        int desiredTop = iconBottom + (AnkiConfig.isUiCompactLayout() ? 12 : 14);
        int minGap = AnkiConfig.isUiCompactLayout() ? 2 : 3;
        int maxTop = this.tradeCardBottomY() - this.tradeFieldBottomPadding() - 160 - 7 * minGap;
        return Math.max(minTop, Math.min(desiredTop, maxTop));
    }

    private int tradeFieldBottomPadding() {
        return AnkiConfig.isUiCompactLayout() ? 10 : 12;
    }

    private int tradeFieldClipTop() {
        return this.tradeFieldStartY() - 6;
    }

    private int tradeFieldClipBottom() {
        return this.tradeCardBottomY() - 8;
    }

    private void updateTradeFieldLayout() {
        int inputX = this.tradeFieldInputX();
        int inputW = this.tradeFieldInputWidth();
        int row = this.tradeFieldRowGap();
        int baseY = this.tradeFieldStartY();
        int contentBottom = baseY + row * 7 + 20;
        this.tradeScrollMax = Math.max(0, contentBottom - this.tradeFieldClipBottom());
        this.tradeScroll = Math.max(0, Math.min(this.tradeScroll, this.tradeScrollMax));
        int y = baseY - this.tradeScroll;
        this.setBoxBounds(this.buyId, inputX, y, inputW);
        this.setBoxBounds(this.buyCount, inputX, y + row, inputW);
        this.setBoxBounds(this.buy2Id, inputX, y + row * 2, inputW);
        this.setBoxBounds(this.buy2Count, inputX, y + row * 3, inputW);
        this.setBoxBounds(this.sellId, inputX, y + row * 4, inputW);
        this.setBoxBounds(this.sellCount, inputX, y + row * 5, inputW);
        this.setBoxBounds(this.maxUses, inputX, y + row * 6, inputW);
        this.setBoxBounds(this.xp, inputX, y + row * 7, inputW);
    }

    private void setBoxBounds(class_342 box, int x, int y, int w) {
        if (box == null) {
            return;
        }
        box.method_46421(x);
        box.method_46419(y);
        box.method_25358(w);
    }

    private int tradeCardBottomY() {
        return this.py + this.ph - 62;
    }

    private int tradeStatusY() {
        return this.py + this.ph - 40;
    }

    private class_342 box(int x, int y, int w, String value) {
        class_342 b = new class_342(this.field_22793, x, y, w, 20, (class_2561)class_2561.method_43473());
        b.method_1852(value);
        b.method_1863(v -> {
            if (!this.suppressDirtySync) {
                this.dirty = true;
            }
        });
        try {
            b.method_1858(false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            b.method_1868(-2497806);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            b.method_1860(-7429177);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.method_37063((class_364)b);
        return b;
    }

    private void rebuildButtons() {
        this.buttons.clear();
        int leftCard = this.px + 28;
        int mid = this.px + this.pw / 2;
        int leftCardRight = mid - 12;
        int rightCardLeft = mid + 10;
        int rightCardRight = this.px + this.pw - 22;
        int rowH = AnkiConfig.isUiCompactLayout() ? 20 : 22;
        int gap = AnkiConfig.isUiCompactLayout() ? 5 : 7;
        this.buttons.add(new UiBtn(leftCardRight - 38, this.py + 57, 16, 16, () -> "<", this::prevTrade, true, null));
        this.buttons.add(new UiBtn(leftCardRight - 20, this.py + 57, 16, 16, () -> ">", this::nextTrade, true, null));
        if (this.targetEntity != null) {
            int headerBtnW = 112;
            this.buttons.add(new UiBtn(this.px + this.pw - headerBtnW - 12, this.py + 8, headerBtnW, 18, () -> this.tr("key.ankinbt.open_entity_editor"), this::openEntityEditor, true, null));
        }
        int rowW = rightCardRight - rightCardLeft - 12;
        int labelW = 76;
        int actionW = Math.max(120, rowW - labelW);
        int halfW = (actionW - 6) / 2;
        int y = this.py + 82;
        this.buttons.add(new UiBtn(rightCardLeft + 8, y, (rowW - 6) / 2, rowH, () -> this.tr("ankinbt.villager.section.trade"), () -> {
            this.rightPage = RightPage.TRADE;
        }, true, () -> this.rightPage == RightPage.TRADE));
        this.buttons.add(new UiBtn(rightCardLeft + 8 + (rowW - 6) / 2 + 6, y, (rowW - 6) / 2, rowH, () -> this.tr("ankinbt.villager.section.meta"), () -> {
            this.rightPage = RightPage.META;
        }, true, () -> this.rightPage == RightPage.META));
        this.rightLabelX = rightCardLeft + 6;
        this.rightActionLeft = rightCardLeft + labelW;
        this.rightTradeOpsY = y += rowH + gap;
        this.rightBuyY = y;
        this.rightBuy2Y = y;
        this.rightSellY = y;
        if (this.rightPage == RightPage.TRADE) {
            this.rightTradeOpsY = y;
            this.buttons.add(new UiBtn(this.rightActionLeft, y, halfW, rowH, () -> this.tr("ankinbt.villager.add"), this::addTrade, true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft + halfW + 6, y, halfW, rowH, () -> this.tr("ankinbt.villager.remove"), this::removeTrade, this.trades.size() > 1, null));
            this.rightBuyY = y += rowH + gap;
            this.buttons.add(new UiBtn(this.rightActionLeft, y, halfW, rowH, () -> this.tr("ankinbt.villager.edit"), () -> this.openPickerFor(InvPickTarget.BUY), true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft + halfW + 6, y, halfW, rowH, () -> this.tr("ankinbt.villager.pick.inv"), () -> this.fillFromMainHand(this.buyId), true, null));
            this.rightBuy2Y = y += rowH + gap;
            this.buttons.add(new UiBtn(this.rightActionLeft, y, halfW, rowH, () -> this.tr("ankinbt.villager.edit"), () -> this.openPickerFor(InvPickTarget.BUY2), true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft + halfW + 6, y, halfW, rowH, () -> this.tr("ankinbt.villager.pick.inv"), () -> this.fillFromMainHand(this.buy2Id), true, null));
            this.rightSellY = y += rowH + gap;
            this.buttons.add(new UiBtn(this.rightActionLeft, y, halfW, rowH, () -> this.tr("ankinbt.villager.edit"), () -> this.openPickerFor(InvPickTarget.SELL), true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft + halfW + 6, y, halfW, rowH, () -> this.tr("ankinbt.villager.pick.inv"), () -> this.fillFromMainHand(this.sellId), true, null));
            y += rowH + gap;
        } else {
            this.buttons.add(new UiBtn(this.rightActionLeft, y, actionW, rowH, () -> class_2561.method_43469((String)"ankinbt.villager.profession", (Object[])new Object[]{this.professionLabel()}).getString(), this::cycleProfession, true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft, y += rowH + gap, halfW, rowH, () -> class_2561.method_43469((String)"ankinbt.villager.level", (Object[])new Object[]{String.valueOf(this.villagerLevel)}).getString(), this::cycleLevel, true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft + halfW + 6, y, halfW, rowH, () -> class_2561.method_43469((String)"ankinbt.villager.reward_exp", (Object[])new Object[]{this.rewardExp ? this.tr("ankinbt.simple.on") : this.tr("ankinbt.simple.off")}).getString(), () -> {
                this.rewardExp = !this.rewardExp;
            }, true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft, y += rowH + gap, actionW, rowH, () -> class_2561.method_43469((String)"ankinbt.villager.require_prof", (Object[])new Object[]{this.onOff(AnkiConfig.isVillagerRequireProfession())}).getString(), () -> AnkiConfig.setVillagerRequireProfession(!AnkiConfig.isVillagerRequireProfession()), true, null));
            y += rowH + gap;
            if (!this.sourceStack.method_7960()) {
                this.buttons.add(new UiBtn(this.rightActionLeft, y, actionW, rowH, () -> class_2561.method_43471((String)"ankinbt.villager.open_spawn_egg_nbt").getString(), () -> class_310.method_1551().method_1507((class_437)new NbtEditorScreen(this.sourceStack)), true, null));
                y += rowH + gap;
            }
        }
        if (this.rightPage == RightPage.TRADE && !this.sourceStack.method_7960()) {
            this.buttons.add(new UiBtn(this.rightActionLeft, y, actionW, rowH, () -> class_2561.method_43471((String)"ankinbt.villager.open_spawn_egg_nbt").getString(), () -> class_310.method_1551().method_1507((class_437)new NbtEditorScreen(this.sourceStack)), true, null));
            y += rowH + gap;
        }
        int bottomY = this.py + this.ph - 30;
        int areaW = this.pw - 36;
        int actionBarW = (areaW - 16) / 3;
        this.buttons.add(new UiBtn(this.px + 18, bottomY, actionBarW, 20, () -> class_2561.method_43471((String)"ankinbt.entity.apply_patch").getString(), this::applyTrade, true, null, 1));
        this.buttons.add(new UiBtn(this.px + 18 + actionBarW + 8, bottomY, actionBarW, 20, () -> class_2561.method_43471((String)"ankinbt.entity.reset_changes").getString(), () -> {
            this.confirmReset = true;
        }, true, null, -1));
        this.buttons.add(new UiBtn(this.px + 18 + (actionBarW + 8) * 2, bottomY, actionBarW, 20, () -> class_2561.method_43471((String)"ankinbt.edit.cancel").getString(), this::tryClose, true, null));
    }

    private void openPickerFor(InvPickTarget target) {
        class_310.method_1551().method_1507((class_437)new ItemPickerScreen(this, id -> {
            this.pushUndo();
            class_342 box = this.boxForTarget(target);
            this.setBoxValue(box, (String)id);
            this.syncCurrentTrade(false);
            this.dirty = true;
        }));
    }

    private void openInventoryPicker(InvPickTarget target) {
        this.invPickTarget = target == InvPickTarget.NONE ? InvPickTarget.BUY : target;
    }

    private void openEntityEditor() {
        if (this.targetEntity == null) {
            return;
        }
        class_310.method_1551().method_1507((class_437)EntityEditorScreen.forEntity(this.targetEntity, this));
    }

    private String inventoryPickButtonLabel() {
        return this.tr("ankinbt.villager.pick.inv") + " [" + this.focusedTargetText() + "]";
    }

    private String focusedTargetText() {
        return switch (this.focusedTarget().ordinal()) {
            case 2 -> this.tr("ankinbt.villager.buy2_item");
            case 3 -> this.tr("ankinbt.villager.sell_item");
            default -> this.tr("ankinbt.villager.buy_item");
        };
    }

    private InvPickTarget focusedTarget() {
        if (this.sellId != null && this.sellId.method_25370()) {
            return InvPickTarget.SELL;
        }
        if (this.buy2Id != null && this.buy2Id.method_25370()) {
            return InvPickTarget.BUY2;
        }
        return InvPickTarget.BUY;
    }

    private void resetForm() {
        this.trades.clear();
        this.trades.add(TradeData.defaults());
        this.tradeIndex = 0;
        this.professionIndex = this.defaultProfessionIndex();
        this.villagerLevel = 1;
        this.rewardExp = true;
        this.villagerType = "minecraft:plains";
        this.loadTradeToForm(this.tradeIndex);
        this.dirty = false;
        this.undoStack.clear();
        this.undoStack.add(this.captureState());
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.entity.reset_done"), -13315175);
        this.rebuildButtons();
    }

    private void readContextDefaults() {
        class_2487 vd;
        LoadedVillagerDefaults liveDefaults;
        if (this.isWanderingTraderContext()) {
            this.professionIndex = 0;
            this.villagerLevel = 1;
            return;
        }
        class_2487 root = null;
        LoadedVillagerDefaults loadedVillagerDefaults = liveDefaults = this.targetEntity == null ? null : this.readDefaultsFromIntegratedServer(this.targetEntity);
        if (liveDefaults != null) {
            this.professionIndex = this.normalizeProfessionIndex(liveDefaults.professionIndex());
            this.villagerLevel = liveDefaults.villagerLevel();
            this.villagerType = liveDefaults.villagerType();
            this.rewardExp = liveDefaults.rewardExp();
            this.trades.clear();
            for (TradeData trade : liveDefaults.trades()) {
                this.trades.add(trade.copy());
            }
            this.normalizeProfessionState();
            if (!this.trades.isEmpty()) {
                return;
            }
        }
        if (this.targetEntity != null) {
            root = this.readEntityTag(this.targetEntity);
        } else if (!this.sourceStack.method_7960()) {
            root = SpawnEggEditorHelper.getEntityData(this.sourceStack).orElse(null);
        }
        if (root == null) {
            if (this.targetEntity != null) {
                root = new class_2487();
                this.injectRuntimeVillagerDataIfMissing(root, this.targetEntity);
                this.injectRuntimeOffersIfMissing(root, this.targetEntity);
            } else {
                this.professionIndex = this.defaultProfessionIndex();
                this.villagerLevel = 1;
                this.villagerType = "minecraft:plains";
                this.trades.clear();
                this.trades.add(TradeData.defaults());
                return;
            }
        }
        if (this.targetEntity != null) {
            class_2487 cached = ENTITY_PATCH_CACHE.get(this.targetEntity.method_5667());
            if (cached != null && !cached.method_33133()) {
                root.method_10543(this.copyCompound(cached));
            }
            this.injectRuntimeVillagerDataIfMissing(root, this.targetEntity);
            this.injectRuntimeOffersIfMissing(root, this.targetEntity);
        }
        if ((vd = this.readCompound(root, "VillagerData")) != null) {
            String p = this.readString(vd, "profession", "");
            int idx = this.professionIndexById(p);
            if (idx >= 0) {
                this.professionIndex = idx;
            }
            this.villagerLevel = Math.max(1, Math.min(5, this.readInt(vd, "level", this.villagerLevel)));
            this.villagerType = this.readString(vd, "type", this.villagerType);
        }
        this.normalizeProfessionState();
        this.trades.clear();
        class_2499 recipes = this.extractOfferRecipes(root);
        if (recipes != null && !recipes.isEmpty()) {
            DebugLog.info("Villager offer recipes detected: {}", recipes.size());
            this.applyRecipesToTrades(recipes);
        }
        if (recipes == null || recipes.isEmpty()) {
            DebugLog.warn("Villager offers missing or incompatible on target: {}", this.targetEntity == null ? "spawn_egg" : this.targetEntity.method_5667());
        }
        if (this.trades.isEmpty()) {
            this.trades.add(TradeData.defaults());
        }
        this.normalizeProfessionState();
    }

    private class_2487 readEntityTag(class_1297 entity) {
        if (entity == null) {
            return null;
        }
        class_2487 saved = this.invokeCompoundArg(entity, "saveWithoutId", new class_2487());
        if (saved != null && !saved.method_33133()) {
            return saved;
        }
        saved = this.invokeCompoundArg(entity, "save", new class_2487());
        if (saved != null && !saved.method_33133()) {
            return saved;
        }
        saved = this.invokeCompoundArg(entity, "saveAsPassenger", new class_2487());
        if (saved != null && !saved.method_33133()) {
            return saved;
        }
        return null;
    }

    private class_2487 readCompound(class_2487 parent, String key) {
        class_2487 ct;
        if (parent == null) {
            return null;
        }
        try {
            Optional opt;
            Object var6_7;
            Object out = parent.getClass().getMethod("getCompound", String.class).invoke((Object)parent, key);
            if (out instanceof class_2487) {
                class_2487 ct2 = (class_2487)out;
                return ct2;
            }
            if (out instanceof Optional && (var6_7 = (opt = (Optional)out).orElse(null)) instanceof class_2487) {
                class_2487 ct3 = var6_7;
                return ct3;
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        Object raw = this.readTag(parent, key);
        return raw instanceof class_2487 ? (ct = (class_2487)raw) : null;
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
        Object raw = this.readTag(parent, key);
        if (raw != null) {
            try {
                Object s = raw.getClass().getMethod("getAsString", new Class[0]).invoke(raw, new Object[0]);
                if (s instanceof String) {
                    String str = (String)s;
                    return str;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return def;
    }

    private int readInt(class_2487 parent, String key, int def) {
        if (parent == null) {
            return def;
        }
        try {
            Optional opt;
            Object var7_9;
            Object out = parent.getClass().getMethod("getInt", String.class).invoke((Object)parent, key);
            if (out instanceof Integer) {
                Integer i = (Integer)out;
                return i;
            }
            if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof Integer) {
                Integer i = var7_9;
                return i;
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        Object raw = this.readTag(parent, key);
        if (raw != null) {
            try {
                Object n = raw.getClass().getMethod("getAsInt", new Class[0]).invoke(raw, new Object[0]);
                if (n instanceof Integer) {
                    Integer i = (Integer)n;
                    return i;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return def;
    }

    private Object readTag(class_2487 parent, String key) {
        try {
            Object out = parent.getClass().getMethod("get", String.class).invoke((Object)parent, key);
            return this.unwrapOptional(out);
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private Object unwrapOptional(Object value) {
        Object out = value;
        while (out instanceof Optional) {
            Optional opt = (Optional)out;
            out = opt.orElse(null);
        }
        return out;
    }

    private class_2487 readRecipeItem(class_2487 recipe, String ... keys) {
        if (recipe == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            class_2487 item = this.readCompound(recipe, key);
            if (item == null || item.method_33133()) continue;
            return item;
        }
        return null;
    }

    private class_2487 readStackComponents(class_2487 stackTag) {
        class_2487 components = this.readCompound(stackTag, "components");
        if (components != null && !components.method_33133()) {
            return this.copyCompound(components);
        }
        class_2487 legacyTag = this.readCompound(stackTag, "tag");
        if (legacyTag != null && !legacyTag.method_33133()) {
            class_2487 wrapped = new class_2487();
            wrapped.method_10566("minecraft:custom_data", (class_2520)this.copyCompound(legacyTag));
            return wrapped;
        }
        return null;
    }

    private void applyRecipesToTrades(class_2499 recipes) {
        if (recipes == null || recipes.isEmpty()) {
            return;
        }
        Boolean parsedRewardExp = null;
        for (int i = 0; i < recipes.size(); ++i) {
            Object entry = this.unwrapOptional(recipes.method_10534(i));
            if (!(entry instanceof class_2487)) continue;
            class_2487 recipe = (class_2487)entry;
            TradeData t = this.tradeFromRecipe(recipe);
            this.trades.add(t);
            Object re = this.readTag(recipe, "rewardExp");
            if (re == null) continue;
            try {
                Boolean bb;
                Object b = re.getClass().getMethod("getAsBoolean", new Class[0]).invoke(re, new Object[0]);
                if (!(b instanceof Boolean)) continue;
                parsedRewardExp = bb = (Boolean)b;
                continue;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (parsedRewardExp != null) {
            this.rewardExp = parsedRewardExp;
        }
    }

    private TradeData tradeFromMerchantOffer(class_1914 offer) {
        TradeData t = TradeData.defaults();
        if (offer == null) {
            return t;
        }
        class_1799 buy = offer.method_8246();
        class_1799 buyB = offer.method_8247();
        class_1799 sell = offer.method_8250();
        if (buy != null && !buy.method_7960()) {
            t.buyId = SpawnEggEditorHelper.getItemId(buy);
            t.buyCount = Math.max(1, buy.method_7947());
            t.buyComponents = this.readItemComponents(buy);
        }
        if (buyB != null && !buyB.method_7960()) {
            t.buy2Id = SpawnEggEditorHelper.getItemId(buyB);
            t.buy2Count = Math.max(1, buyB.method_7947());
            t.buy2Components = this.readItemComponents(buyB);
        } else {
            t.buy2Id = "";
            t.buy2Count = 1;
            t.buy2Components = null;
        }
        if (sell != null && !sell.method_7960()) {
            t.sellId = SpawnEggEditorHelper.getItemId(sell);
            t.sellCount = Math.max(1, sell.method_7947());
            t.sellComponents = this.readItemComponents(sell);
        }
        t.maxUses = Math.max(1, offer.method_8248());
        t.xp = Math.max(0, offer.method_19279());
        return t;
    }

    private TradeData tradeFromRecipe(class_2487 recipe) {
        TradeData t = TradeData.defaults();
        class_2487 buy = this.readRecipeItem(recipe, "buy", "base_cost_a", "itemA", "input", "costA");
        class_2487 buyB = this.readRecipeItem(recipe, "buyB", "cost_b", "itemB", "inputB", "costB");
        class_2487 sell = this.readRecipeItem(recipe, "sell", "result", "output", "itemOut");
        if (buy != null) {
            t.buyId = this.readString(buy, "id", t.buyId);
            t.buyCount = Math.max(1, this.readInt(buy, "count", t.buyCount));
            t.buyComponents = this.readStackComponents(buy);
        }
        if (buyB != null) {
            t.buy2Id = this.readString(buyB, "id", t.buy2Id);
            t.buy2Count = Math.max(1, this.readInt(buyB, "count", t.buy2Count));
            t.buy2Components = this.readStackComponents(buyB);
        }
        if (sell != null) {
            t.sellId = this.readString(sell, "id", t.sellId);
            t.sellCount = Math.max(1, this.readInt(sell, "count", t.sellCount));
            t.sellComponents = this.readStackComponents(sell);
        }
        t.maxUses = Math.max(1, this.readInt(recipe, "maxUses", t.maxUses));
        t.xp = Math.max(0, this.readInt(recipe, "xp", t.xp));
        return t;
    }

    private int professionIndexById(String id) {
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < PROFESSIONS.length; ++i) {
            if (!id.equals(PROFESSIONS[i])) continue;
            return i;
        }
        return -1;
    }

    private void cycleProfession() {
        this.pushUndo();
        this.syncCurrentTrade(false);
        if (this.isWanderingTraderContext()) {
            this.professionIndex = 0;
            this.dirty = true;
            return;
        }
        int start = this.professionIndex;
        do {
            ++this.professionIndex;
            if (this.professionIndex < PROFESSIONS.length) continue;
            this.professionIndex = this.defaultProfessionIndex();
        } while (!this.isTradeableProfession(PROFESSIONS[this.professionIndex]) && this.professionIndex != start);
        if (!this.isTradeableProfession(PROFESSIONS[this.professionIndex])) {
            this.professionIndex = this.defaultProfessionIndex();
        }
        this.dirty = true;
    }

    private void cycleLevel() {
        this.pushUndo();
        this.syncCurrentTrade(false);
        ++this.villagerLevel;
        if (this.villagerLevel > 5) {
            this.villagerLevel = 1;
        }
        this.dirty = true;
    }

    private String professionLabel() {
        String id = this.normalizeProfessionId(PROFESSIONS[this.professionIndex]);
        if (id.isBlank()) {
            return class_2561.method_43471((String)"ankinbt.villager.profession.none").getString();
        }
        int idx = id.indexOf(58);
        return idx >= 0 ? id.substring(idx + 1) : id;
    }

    private boolean isTradeableProfession(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        return !id.endsWith("nitwit") && !id.endsWith("unemployed");
    }

    private boolean isWanderingTraderContext() {
        if (this.targetEntity != null) {
            String type = this.targetEntity.method_5864().toString().toLowerCase(Locale.ROOT);
            return type.contains("wandering_trader");
        }
        if (!this.sourceStack.method_7960()) {
            String id = SpawnEggEditorHelper.getItemId(this.sourceStack).toLowerCase(Locale.ROOT);
            return id.contains("wandering_trader_spawn_egg");
        }
        return false;
    }

    private void applyTrade() {
        if (!this.syncCurrentTrade(true)) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.simple.invalid_number"), -1096636);
            return;
        }
        this.ensureTrades();
        boolean wandering = this.isWanderingTraderContext();
        String profession = this.normalizeProfessionId(PROFESSIONS[this.professionIndex]);
        this.professionIndex = this.normalizeProfessionIndex(this.professionIndex);
        class_2499 recipes = new class_2499();
        for (TradeData t : this.trades) {
            if (!this.isLikelyItemId(t.buyId) || !this.isLikelyItemId(t.sellId) || !t.buy2Id.isBlank() && !this.isLikelyItemId(t.buy2Id)) {
                this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.villager.invalid_item"), -1096636);
                return;
            }
            class_2487 buyTag = this.buildTradeStackTag(t.buyId, t.buyCount, t.buyComponents);
            class_2487 sellTag = this.buildTradeStackTag(t.sellId, t.sellCount, t.sellComponents);
            class_2487 recipe = new class_2487();
            recipe.method_10566("buy", (class_2520)buyTag);
            recipe.method_10566("base_cost_a", (class_2520)this.copyCompound(buyTag));
            if (!t.buy2Id.isEmpty()) {
                class_2487 buyB = this.buildTradeStackTag(t.buy2Id, t.buy2Count, t.buy2Components);
                recipe.method_10566("buyB", (class_2520)buyB);
                recipe.method_10566("cost_b", (class_2520)this.copyCompound(buyB));
            }
            recipe.method_10566("sell", (class_2520)sellTag);
            recipe.method_10566("result", (class_2520)this.copyCompound(sellTag));
            recipe.method_10569("maxUses", Math.max(1, t.maxUses));
            recipe.method_10569("uses", 0);
            recipe.method_10569("xp", Math.max(0, t.xp));
            recipe.method_10569("specialPrice", 0);
            recipe.method_10569("demand", 0);
            recipe.method_10548("priceMultiplier", 0.0f);
            recipe.method_10556("rewardExp", this.rewardExp);
            recipes.add((Object)recipe);
        }
        class_2487 offers = new class_2487();
        offers.method_10566("Recipes", (class_2520)recipes);
        offers.method_10566("recipes", (class_2520)this.copyListTag(recipes));
        class_2487 patch = new class_2487();
        patch.method_10566("Offers", (class_2520)offers);
        if (!wandering) {
            class_2487 villagerData = new class_2487();
            villagerData.method_10582("type", this.villagerType == null || this.villagerType.isBlank() ? "minecraft:plains" : this.villagerType);
            villagerData.method_10582("profession", profession);
            villagerData.method_10569("level", Math.max(1, Math.min(5, this.villagerLevel)));
            patch.method_10566("VillagerData", (class_2520)villagerData);
            patch.method_10569("Xp", Math.max(0, this.villagerLevel * 10));
        }
        class_310 mc = class_310.method_1551();
        if (this.targetEntity != null) {
            if (mc.field_1724 == null) {
                return;
            }
            if (this.applyTradeToIntegratedServer(mc, patch)) {
                ENTITY_PATCH_CACHE.put(this.targetEntity.method_5667(), this.copyCompound(patch));
                this.applyTradePreviewToClient();
                this.dirty = false;
                this.undoStack.clear();
                this.undoStack.add(this.captureState());
                this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.entity.applied"), -13315175);
                return;
            }
            if (!EditorCommandHelper.canUseEntityCommand(mc)) {
                this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.entity.admin_required"), -1096636);
                return;
            }
            boolean ok = EditorCommandHelper.applyMergeToEntity(mc, this.targetEntity, patch);
            this.setStatus((class_2561)(ok ? class_2561.method_43471((String)"ankinbt.entity.applied") : class_2561.method_43471((String)"ankinbt.status.save_error")), ok ? -13315175 : -1096636);
            if (ok) {
                ENTITY_PATCH_CACHE.put(this.targetEntity.method_5667(), this.copyCompound(patch));
                this.applyTradePreviewToClient();
                this.dirty = false;
                this.undoStack.clear();
                this.undoStack.add(this.captureState());
            }
            return;
        }
        if (!SpawnEggEditorHelper.isVillagerSpawnEgg(this.sourceStack)) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.villager.spawn_egg_required"), -1096636);
            return;
        }
        patch.method_10582("id", wandering ? "minecraft:wandering_trader" : "minecraft:villager");
        Optional<class_1799> patched = SpawnEggEditorHelper.withMergedEntityData(this.sourceStack, patch);
        if (patched.isEmpty()) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.status.save_error"), -1096636);
            return;
        }
        if (!SpawnEggEditorHelper.saveToCreativeSlot(mc, patched.get(), this.inventorySlot)) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.status.creative_only"), -1096636);
            return;
        }
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.entity.applied"), -13315175);
        this.dirty = false;
        this.undoStack.clear();
        this.undoStack.add(this.captureState());
    }

    private boolean isLikelyItemId(String id) {
        return !id.isBlank() && id.contains(":") && id.indexOf(58) > 0 && id.indexOf(58) < id.length() - 1;
    }

    private Integer parseInt(String in, int def) {
        String t;
        String string = t = in == null ? "" : in.trim();
        if (t.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(t);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private void setStatus(class_2561 msg, int color) {
        this.status = msg;
        this.statusColor = color;
        this.statusTime = System.currentTimeMillis();
    }

    public boolean method_25402(double mx, double my, int button) {
        this.recalcBounds();
        this.updateTradeFieldLayout();
        if (this.confirmClose || this.confirmReset) {
            return this.clickConfirm((int)mx, (int)my);
        }
        if (this.invPickTarget != InvPickTarget.NONE) {
            return this.clickInventoryOverlay((int)mx, (int)my, button);
        }
        if (this.handleEditBoxClick(mx, my, button)) {
            return true;
        }
        if (button == 0 || button == 1) {
            for (IconHit hit : this.iconHits) {
                if (!hit.hit((int)mx, (int)my)) continue;
                class_342 box = this.boxForTarget(hit.target);
                if (box == null) {
                    return true;
                }
                if (button == 0) {
                    this.openPickerFor(hit.target);
                } else {
                    this.openInventoryPicker(hit.target);
                }
                return true;
            }
        }
        if (button == 0) {
            for (UiBtn btn : this.buttons) {
                if (!btn.click((int)mx, (int)my)) continue;
                this.rebuildButtons();
                return true;
            }
        }
        this.unfocusEditBoxes();
        return super.method_25402(mx, my, button);
    }

    public boolean method_25404(int key, int scan, int mod) {
        boolean ctrl;
        if (this.handleEditBoxKey(key, scan, mod)) {
            return true;
        }
        boolean bl = ctrl = (mod & 2) != 0;
        if (ctrl && key == 90) {
            this.undo();
            return true;
        }
        if (key == 256) {
            this.tryClose();
            return true;
        }
        return super.method_25404(key, scan, mod);
    }

    public boolean method_25400(char codePoint, int modifiers) {
        if (this.handleEditBoxChar(codePoint, modifiers)) {
            return true;
        }
        return super.method_25400(codePoint, modifiers);
    }

    public boolean method_25401(double mx, double my, double sx, double sy) {
        this.recalcBounds();
        this.updateTradeFieldLayout();
        int left = this.px + 28;
        int right = this.px + this.pw / 2 - 12;
        int top = this.tradeFieldClipTop();
        int bottom = this.tradeFieldClipBottom();
        if (this.tradeScrollMax > 0 && mx >= (double)left && mx < (double)right && my >= (double)top && my < (double)bottom) {
            int step = (int)Math.signum(sy);
            if (step != 0) {
                int delta = Math.max(12, this.tradeFieldRowGap() / 2);
                this.tradeScroll = Math.max(0, Math.min(this.tradeScrollMax, this.tradeScroll - step * delta));
            }
            return true;
        }
        return super.method_25401(mx, my, sx, sy);
    }

    public void method_25394(class_332 g, int mx, int my, float partialTick) {
        this.recalcBounds();
        this.updateTradeFieldLayout();
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        float opacity = AnkiConfig.getUiOpacity();
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
        int left = this.px + 28;
        int mid = this.px + this.pw / 2;
        int leftRight = mid - 12;
        int rightLeft = mid + 10;
        int right = this.px + this.pw - 22;
        int fieldLeft = this.tradeFieldLeft();
        int inputX = this.tradeFieldInputX();
        int row = this.tradeFieldRowGap();
        int fieldY = this.tradeFieldStartY() - this.tradeScroll;
        int cardBottom = this.tradeCardBottomY();
        g.method_25294(left, this.py + 74, leftRight, cardBottom, card);
        g.method_25294(rightLeft, this.py + 74, right, cardBottom, card);
        this.border(g, left, this.py + 74, leftRight - left, cardBottom - (this.py + 74), border);
        this.border(g, rightLeft, this.py + 74, right - rightLeft, cardBottom - (this.py + 74), border);
        VersionCompat.get().drawString(g, this.field_22793, this.field_22785, this.px + 12, this.py + 12, -788737, false);
        String target = this.targetEntity != null ? this.targetEntity.method_5476().getString() : this.sourceStack.method_7964().getString();
        VersionCompat.get().drawString(g, this.field_22793, target, this.px + 170, this.py + 13, -7429177, false);
        this.renderTradeIcons(g, mx, my, fieldLeft + 10, this.py + 94, accent);
        String tradeLabel = this.tradeIndex + 1 + " / " + Math.max(1, this.trades.size());
        VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.villager.section.trade") + " " + tradeLabel, left + 8, this.py + 62, accent, false);
        VersionCompat.get().drawString(g, this.field_22793, this.rightPage == RightPage.TRADE ? this.tr("ankinbt.villager.section.trade") : this.tr("ankinbt.villager.section.meta"), rightLeft + 8, this.py + 62, accent, false);
        if (this.rightPage == RightPage.TRADE) {
            this.drawRightLabel(g, this.tr("ankinbt.villager.section.trade"), this.rightLabelX, this.rightTradeOpsY + 6, this.rightActionLeft - this.rightLabelX - 6);
            this.drawRightLabel(g, this.tr("ankinbt.villager.buy_item"), this.rightLabelX, this.rightBuyY + 6, this.rightActionLeft - this.rightLabelX - 6);
            this.drawRightLabel(g, this.tr("ankinbt.villager.buy2_item"), this.rightLabelX, this.rightBuy2Y + 6, this.rightActionLeft - this.rightLabelX - 6);
            this.drawRightLabel(g, this.tr("ankinbt.villager.sell_item"), this.rightLabelX, this.rightSellY + 6, this.rightActionLeft - this.rightLabelX - 6);
        }
        g.method_44379(left + 2, this.tradeFieldClipTop(), leftRight - 8, this.tradeFieldClipBottom());
        this.renderTradeFieldLabel(g, (class_2561)class_2561.method_43471((String)"ankinbt.villager.buy_item"), fieldLeft, fieldY);
        this.renderTradeFieldLabel(g, (class_2561)class_2561.method_43471((String)"ankinbt.villager.buy_count"), fieldLeft, fieldY + row);
        this.renderTradeFieldLabel(g, (class_2561)class_2561.method_43471((String)"ankinbt.villager.buy2_item"), fieldLeft, fieldY + row * 2);
        this.renderTradeFieldLabel(g, (class_2561)class_2561.method_43471((String)"ankinbt.villager.buy2_count"), fieldLeft, fieldY + row * 3);
        this.renderTradeFieldLabel(g, (class_2561)class_2561.method_43471((String)"ankinbt.villager.sell_item"), fieldLeft, fieldY + row * 4);
        this.renderTradeFieldLabel(g, (class_2561)class_2561.method_43471((String)"ankinbt.villager.sell_count"), fieldLeft, fieldY + row * 5);
        this.renderTradeFieldLabel(g, (class_2561)class_2561.method_43471((String)"ankinbt.villager.max_uses"), fieldLeft, fieldY + row * 6);
        this.renderTradeFieldLabel(g, (class_2561)class_2561.method_43471((String)"ankinbt.villager.xp"), fieldLeft, fieldY + row * 7);
        this.renderInlineEditBox(g, this.buyId, mx, my, accent);
        this.renderInlineEditBox(g, this.buyCount, mx, my, accent);
        this.renderInlineEditBox(g, this.buy2Id, mx, my, accent);
        this.renderInlineEditBox(g, this.buy2Count, mx, my, accent);
        this.renderInlineEditBox(g, this.sellId, mx, my, accent);
        this.renderInlineEditBox(g, this.sellCount, mx, my, accent);
        this.renderInlineEditBox(g, this.maxUses, mx, my, accent);
        this.renderInlineEditBox(g, this.xp, mx, my, accent);
        g.method_44380();
        this.renderTradeScrollBar(g, leftRight - 7, this.tradeFieldClipTop(), this.tradeFieldClipBottom() - this.tradeFieldClipTop(), accent);
        for (UiBtn btn : this.buttons) {
            btn.render(g, this.field_22793, mx, my, accent);
        }
        this.renderInventoryOverlay(g, mx, my, accent);
        if (this.confirmReset) {
            this.renderConfirm(g, mx, my, true);
        } else if (this.confirmClose) {
            this.renderUnsavedConfirmLikeSimple(g, mx, my);
        }
        if (this.status != null && !this.status.getString().isEmpty() && System.currentTimeMillis() - this.statusTime < 2600L) {
            int statusY = this.tradeStatusY();
            VersionCompat.get().drawString(g, this.field_22793, this.status, left, statusY, this.statusColor, false);
        }
    }

    private void renderTradeFieldLabel(class_332 g, class_2561 label, int x, int y) {
        VersionCompat.get().drawString(g, this.field_22793, label, x + 2, y + 5, -7429177, false);
    }

    private String safeValue(String in, String def) {
        String t = in == null ? "" : in.trim();
        return t.isEmpty() ? def : t;
    }

    private void renderInlineEditBox(class_332 g, class_342 box, int mx, int my, int accent) {
        if (box == null) {
            return;
        }
        boolean focused = box.method_25370();
        boolean hover = mx >= box.method_46426() && mx < box.method_46426() + box.method_25368() && my >= box.method_46427() && my < box.method_46427() + box.method_25364();
        String shown = box.method_1882() == null ? "" : box.method_1882();
        int textY = box.method_46427() + 2;
        int maxWidth = Math.max(12, box.method_25368() - 4);
        if (this.field_22793.method_1727(shown) > maxWidth) {
            shown = this.field_22793.method_27523(shown, maxWidth);
        }
        int color = shown.isBlank() ? -7429177 : -2497806;
        VersionCompat.get().drawString(g, this.field_22793, shown, box.method_46426() + 2, textY, color, false);
        int lineColor = focused ? accent : (hover ? -12494202 : -13878436);
        g.method_25294(box.method_46426(), box.method_46427() + box.method_25364() - 1, box.method_46426() + box.method_25368(), box.method_46427() + box.method_25364(), lineColor);
        if (focused && (System.currentTimeMillis() / 500L & 1L) == 0L) {
            int cursorX = Math.min(box.method_46426() + 2 + this.field_22793.method_1727(shown), box.method_46426() + box.method_25368() - 1);
            g.method_25294(cursorX, box.method_46427() + 3, cursorX + 1, box.method_46427() + box.method_25364() - 2, -2497806);
        }
    }

    private void renderTradeScrollBar(class_332 g, int x, int y, int h, int accent) {
        if (this.tradeScrollMax <= 0 || h <= 20) {
            return;
        }
        g.method_25294(x, y, x + 4, y + h, 890840888);
        int thumbH = Math.max(24, (int)Math.round((double)h * (double)h / (double)(h + this.tradeScrollMax)));
        int travel = Math.max(0, h - thumbH);
        int thumbY = y + (this.tradeScrollMax == 0 ? 0 : (int)Math.round((double)this.tradeScroll / (double)this.tradeScrollMax * (double)travel));
        g.method_25294(x, thumbY, x + 4, thumbY + thumbH, accent);
    }

    private boolean handleEditBoxClick(double mx, double my, int button) {
        boolean hit = false;
        for (class_342 box : this.allBoxes()) {
            if (box == null || !this.isTradeBoxVisible(box) || !box.method_25402(mx, my, button)) continue;
            this.focusBox(box);
            hit = true;
            break;
        }
        if (!hit && button == 0) {
            this.unfocusEditBoxes();
        }
        return hit;
    }

    private boolean isTradeBoxVisible(class_342 box) {
        if (box == null) {
            return false;
        }
        return box.method_46427() + box.method_25364() > this.tradeFieldClipTop() && box.method_46427() < this.tradeFieldClipBottom();
    }

    private boolean handleEditBoxKey(int key, int scan, int mod) {
        for (class_342 box : this.allBoxes()) {
            if (box == null || !box.method_25370() || !box.method_25404(key, scan, mod)) continue;
            return true;
        }
        return false;
    }

    private boolean handleEditBoxChar(char codePoint, int modifiers) {
        for (class_342 box : this.allBoxes()) {
            if (box == null || !box.method_25370() || !box.method_25400(codePoint, modifiers)) continue;
            return true;
        }
        return false;
    }

    private List<class_342> allBoxes() {
        return List.of(this.buyId, this.buyCount, this.buy2Id, this.buy2Count, this.sellId, this.sellCount, this.maxUses, this.xp);
    }

    private void focusBox(class_342 target) {
        for (class_342 box : this.allBoxes()) {
            if (box == null) continue;
            box.method_25365(box == target);
        }
    }

    private void unfocusEditBoxes() {
        for (class_342 box : this.allBoxes()) {
            if (box == null) continue;
            box.method_25365(false);
        }
    }

    private class_342 boxForTarget(InvPickTarget target) {
        return switch (target.ordinal()) {
            case 2 -> this.buy2Id;
            case 3 -> this.sellId;
            default -> this.buyId;
        };
    }

    private void renderTradeIcons(class_332 g, int mx, int my, int x, int y, int accent) {
        this.iconHits.clear();
        this.ensureTrades();
        TradeData live = this.readTradeFromForm(this.trades.get(this.tradeIndex));
        this.renderIconSlot(g, mx, my, x, y, this.buyId == null ? "" : this.buyId.method_1882(), live.buyComponents, live.buyCount, InvPickTarget.BUY, this.tr("ankinbt.villager.buy_item"), accent);
        VersionCompat.get().drawString(g, this.field_22793, "+", x + 42, y + 5, -7429177, false);
        this.renderIconSlot(g, mx, my, x + 52, y, this.buy2Id == null ? "" : this.buy2Id.method_1882(), live.buy2Components, live.buy2Count, InvPickTarget.BUY2, this.tr("ankinbt.villager.buy2_item"), accent);
        VersionCompat.get().drawString(g, this.field_22793, "->", x + 92, y + 5, -7429177, false);
        this.renderIconSlot(g, mx, my, x + 112, y, this.sellId == null ? "" : this.sellId.method_1882(), live.sellComponents, live.sellCount, InvPickTarget.SELL, this.tr("ankinbt.villager.sell_item"), accent);
    }

    private void renderIconSlot(class_332 g, int mx, int my, int x, int y, String itemId, class_2487 components, int count, InvPickTarget target, String hint, int accent) {
        int w = 18;
        int h = 18;
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        int bg = hover ? -1977141422 : 1713055288;
        int edge = hover ? accent : -13878436;
        g.method_25294(x, y, x + w, y + h, bg);
        this.border(g, x, y, w, h, edge);
        class_1799 preview = this.buildPreviewStack(itemId, components, count);
        if (!preview.method_7960()) {
            g.method_51427(preview, x + 1, y + 1);
        }
        this.iconHits.add(new IconHit(x, y, w, h, target));
        if (hover) {
            Object text;
            Object object = text = itemId == null || itemId.isBlank() ? "<" + this.tr("ankinbt.villager.profession.none") + ">" : itemId;
            if (!preview.method_7960()) {
                this.renderStackTooltip(g, preview, mx, my, hint, (String)text);
            } else {
                VersionCompat.get().renderTooltip(g, this.field_22793, (class_2561)class_2561.method_43470((String)(hint + ": " + (String)text)), mx, my);
            }
        }
    }

    private class_1799 buildPreviewStack(String itemId, class_2487 components, int count) {
        class_2487 componentData;
        class_1792 item = this.resolveItem(itemId);
        if (item == null || item == class_1802.field_8162) {
            return class_1799.field_8037;
        }
        int n = Math.max(1, Math.min(64, count));
        class_2487 fullStack = this.readWrappedFullStack(components);
        if (fullStack != null && !fullStack.method_33133()) {
            try {
                class_2487 full = this.copyCompound(fullStack);
                full.method_10582("id", itemId);
                full.method_10569("count", n);
                Optional<class_1799> out = NbtHelper.deserializeItemStack(full);
                if (out.isPresent() && !out.get().method_7960()) {
                    return out.get();
                }
            }
            catch (Throwable full) {
                // empty catch block
            }
        }
        if ((componentData = this.unwrapTradeComponents(components)) == null || componentData.method_33133()) {
            return new class_1799((class_1935)item, n);
        }
        try {
            class_2487 tag = new class_2487();
            tag.method_10582("id", itemId);
            tag.method_10569("count", n);
            tag.method_10566("components", (class_2520)this.copyCompound(componentData));
            Optional<class_1799> out = NbtHelper.deserializeItemStack(tag);
            if (out.isPresent() && !out.get().method_7960()) {
                return out.get();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return new class_1799((class_1935)item, n);
    }

    private void renderStackTooltip(class_332 g, class_1799 stack, int mx, int my, String hint, String itemId) {
        if (stack == null || stack.method_7960()) {
            VersionCompat.get().renderTooltip(g, this.field_22793, (class_2561)class_2561.method_43470((String)(hint + ": " + itemId)), mx, my);
            return;
        }
        if (this.tryRenderVanillaTooltip(g, stack, mx, my)) {
            return;
        }
        class_5250 fallback = class_2561.method_43470((String)(stack.method_7964().getString() + " (" + itemId + ")"));
        VersionCompat.get().renderTooltip(g, this.field_22793, (class_2561)fallback, mx, my);
    }

    private boolean tryRenderVanillaTooltip(class_332 g, class_1799 stack, int mx, int my) {
        try {
            Method m = g.getClass().getMethod("renderTooltip", class_327.class, class_1799.class, Integer.TYPE, Integer.TYPE);
            m.invoke((Object)g, this.field_22793, stack, mx, my);
            return true;
        }
        catch (Throwable throwable) {
            for (Method m : g.getClass().getMethods()) {
                Class<?>[] p;
                if (!"renderTooltip".equals(m.getName()) || (p = m.getParameterTypes()).length != 4 || !p[0].isAssignableFrom(this.field_22793.getClass()) || !class_1799.class.isAssignableFrom(p[1]) || p[2] != Integer.TYPE || p[3] != Integer.TYPE) continue;
                try {
                    m.invoke((Object)g, this.field_22793, stack, mx, my);
                    return true;
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
            }
            return false;
        }
    }

    private class_1792 resolveItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        if (this.itemCache.containsKey(itemId)) {
            return this.itemCache.get(itemId);
        }
        class_1792 found = ItemRegistryHelper.resolveItem(itemId);
        this.itemCache.put(itemId, found);
        return found;
    }

    private void renderInventoryOverlay(class_332 g, int mx, int my, int accent) {
        this.invSlotHits.clear();
        if (this.invPickTarget == InvPickTarget.NONE) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        int cols = 9;
        int rows = 4;
        int cell = 20;
        int w = cols * cell + 20;
        int h = rows * cell + 44;
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) / 2;
        g.method_25294(0, 0, this.field_22789, this.field_22790, -1728053248);
        g.method_25294(x, y, x + w, y + h, -267315418);
        this.border(g, x, y, w, h, -13878436);
        VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.villager.pick.inv") + " - " + this.focusedTargetText(), x + 10, y + 10, accent, false);
        int startX = x + 10;
        int startY = y + 24;
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                int logical = r < 3 ? 9 + r * 9 + c : c;
                class_1799 stack = mc.field_1724.method_31548().method_5438(logical);
                int sx = startX + c * cell;
                int sy = startY + r * cell;
                g.method_25294(sx, sy, sx + 18, sy + 18, 1243293240);
                this.border(g, sx, sy, 18, 18, -13878436);
                if (stack == null || stack.method_7960()) continue;
                g.method_51427(stack, sx + 1, sy + 1);
                String id = SpawnEggEditorHelper.getItemId(stack);
                this.invSlotHits.add(new InvSlotHit(sx, sy, 18, 18, id, stack.method_7972()));
                if (mx < sx || mx >= sx + 18 || my < sy || my >= sy + 18) continue;
                this.renderStackTooltip(g, stack, mx, my, this.tr("ankinbt.villager.pick.inv"), id);
            }
        }
    }

    private boolean clickInventoryOverlay(int mx, int my, int button) {
        if (button != 0) {
            this.invPickTarget = InvPickTarget.NONE;
            return true;
        }
        for (InvSlotHit hit : this.invSlotHits) {
            if (!hit.hit(mx, my)) continue;
            class_342 box = this.boxForTarget(this.invPickTarget);
            if (box != null) {
                this.pushUndo();
                if (this.applyPickedStack(box, hit.stack)) {
                    this.dirty = true;
                }
            }
            this.invPickTarget = InvPickTarget.NONE;
            return true;
        }
        this.invPickTarget = InvPickTarget.NONE;
        return true;
    }

    private void ensureTrades() {
        if (this.trades.isEmpty()) {
            this.trades.add(TradeData.defaults());
        }
        this.tradeIndex = Math.max(0, Math.min(this.tradeIndex, this.trades.size() - 1));
    }

    private TradeData readTradeFromForm(TradeData prev) {
        TradeData t = TradeData.defaults();
        if (prev != null) {
            t.buyComponents = this.copyCompound(prev.buyComponents);
            t.buy2Components = this.copyCompound(prev.buy2Components);
            t.sellComponents = this.copyCompound(prev.sellComponents);
        }
        t.buyId = this.buyId.method_1882().trim().isEmpty() ? t.buyId : this.buyId.method_1882().trim();
        t.buy2Id = this.buy2Id.method_1882().trim();
        t.sellId = this.sellId.method_1882().trim().isEmpty() ? t.sellId : this.sellId.method_1882().trim();
        Integer buy = this.parseInt(this.buyCount.method_1882(), t.buyCount);
        Integer buy2 = this.parseInt(this.buy2Count.method_1882(), t.buy2Count);
        Integer sell = this.parseInt(this.sellCount.method_1882(), t.sellCount);
        Integer uses = this.parseInt(this.maxUses.method_1882(), t.maxUses);
        Integer xpVal = this.parseInt(this.xp.method_1882(), t.xp);
        if (buy != null) {
            t.buyCount = Math.max(1, buy);
        }
        if (buy2 != null) {
            t.buy2Count = Math.max(1, buy2);
        }
        if (sell != null) {
            t.sellCount = Math.max(1, sell);
        }
        if (uses != null) {
            t.maxUses = Math.max(1, uses);
        }
        if (xpVal != null) {
            t.xp = Math.max(0, xpVal);
        }
        if (prev != null) {
            if (!Objects.equals(t.buyId, prev.buyId)) {
                t.buyComponents = null;
            }
            if (!Objects.equals(t.buy2Id, prev.buy2Id)) {
                t.buy2Components = null;
            }
            if (!Objects.equals(t.sellId, prev.sellId)) {
                t.sellComponents = null;
            }
        }
        return t;
    }

    private boolean syncCurrentTrade(boolean strict) {
        boolean valid;
        this.ensureTrades();
        TradeData prev = this.trades.get(this.tradeIndex);
        TradeData t = this.readTradeFromForm(prev);
        boolean buy2Valid = t.buy2Id.isEmpty() || this.isLikelyItemId(t.buy2Id);
        boolean bl = valid = this.isLikelyItemId(t.buyId) && this.isLikelyItemId(t.sellId) && buy2Valid && this.parseInt(this.buyCount.method_1882(), 1) != null && this.parseInt(this.buy2Count.method_1882(), 1) != null && this.parseInt(this.sellCount.method_1882(), 1) != null && this.parseInt(this.maxUses.method_1882(), 12) != null && this.parseInt(this.xp.method_1882(), 1) != null;
        if (strict && !valid) {
            return false;
        }
        this.trades.set(this.tradeIndex, t);
        return true;
    }

    private void loadTradeToForm(int idx) {
        this.ensureTrades();
        TradeData t = this.trades.get(idx);
        this.setBoxValue(this.buyId, t.buyId);
        this.setBoxValue(this.buyCount, String.valueOf(t.buyCount));
        this.setBoxValue(this.buy2Id, t.buy2Id);
        this.setBoxValue(this.buy2Count, String.valueOf(t.buy2Count));
        this.setBoxValue(this.sellId, t.sellId);
        this.setBoxValue(this.sellCount, String.valueOf(t.sellCount));
        this.setBoxValue(this.maxUses, String.valueOf(t.maxUses));
        this.setBoxValue(this.xp, String.valueOf(t.xp));
    }

    private void prevTrade() {
        this.ensureTrades();
        this.syncCurrentTrade(false);
        --this.tradeIndex;
        if (this.tradeIndex < 0) {
            this.tradeIndex = this.trades.size() - 1;
        }
        this.loadTradeToForm(this.tradeIndex);
    }

    private void nextTrade() {
        this.ensureTrades();
        this.syncCurrentTrade(false);
        ++this.tradeIndex;
        if (this.tradeIndex >= this.trades.size()) {
            this.tradeIndex = 0;
        }
        this.loadTradeToForm(this.tradeIndex);
    }

    private void addTrade() {
        this.ensureTrades();
        this.pushUndo();
        this.syncCurrentTrade(false);
        this.trades.add(this.tradeIndex + 1, this.trades.get(this.tradeIndex).copy());
        ++this.tradeIndex;
        this.loadTradeToForm(this.tradeIndex);
        this.dirty = true;
    }

    private void removeTrade() {
        this.ensureTrades();
        if (this.trades.size() <= 1) {
            return;
        }
        this.pushUndo();
        this.trades.remove(this.tradeIndex);
        if (this.tradeIndex >= this.trades.size()) {
            this.tradeIndex = this.trades.size() - 1;
        }
        this.loadTradeToForm(this.tradeIndex);
        this.dirty = true;
    }

    private void fillFromMainHand(class_342 box) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        int slot = VersionCompat.get().getSelectedSlot(mc.field_1724.method_31548());
        class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
        if (stack == null || stack.method_7960()) {
            stack = mc.field_1724.method_6047();
        }
        if (stack == null || stack.method_7960()) {
            return;
        }
        this.pushUndo();
        if (this.applyPickedStack(box, stack)) {
            this.dirty = true;
        }
    }

    private boolean applyPickedStack(class_342 box, class_1799 stack) {
        if (box == null || stack == null || stack.method_7960()) {
            return false;
        }
        String id = SpawnEggEditorHelper.getItemId(stack);
        if (!this.isLikelyItemId(id)) {
            return false;
        }
        this.setBoxValue(box, id);
        this.syncCurrentTrade(false);
        this.setPickedComponents(box, this.readPickedStackData(stack));
        return true;
    }

    private StateSnapshot captureState() {
        this.syncCurrentTrade(false);
        ArrayList<TradeData> copy = new ArrayList<TradeData>();
        for (TradeData t : this.trades) {
            copy.add(t.copy());
        }
        return new StateSnapshot(copy, this.tradeIndex, this.professionIndex, this.villagerLevel, this.rewardExp, this.villagerType, this.dirty);
    }

    private void applyState(StateSnapshot s) {
        if (s == null) {
            return;
        }
        this.trades.clear();
        for (TradeData t : s.trades) {
            this.trades.add(t.copy());
        }
        this.ensureTrades();
        this.tradeIndex = Math.max(0, Math.min(s.tradeIndex, this.trades.size() - 1));
        this.professionIndex = Math.max(0, Math.min(PROFESSIONS.length - 1, s.professionIndex));
        this.villagerLevel = Math.max(1, Math.min(5, s.villagerLevel));
        this.rewardExp = s.rewardExp;
        this.villagerType = s.villagerType;
        this.dirty = s.dirty;
        this.loadTradeToForm(this.tradeIndex);
    }

    private void pushUndo() {
        StateSnapshot current = this.captureState();
        if (!this.undoStack.isEmpty() && Objects.equals(this.undoStack.get(this.undoStack.size() - 1), current)) {
            return;
        }
        this.undoStack.add(current);
        while (this.undoStack.size() > 50) {
            this.undoStack.remove(0);
        }
    }

    private void undo() {
        if (this.undoStack.size() <= 1) {
            return;
        }
        this.undoStack.remove(this.undoStack.size() - 1);
        this.applyState(this.undoStack.get(this.undoStack.size() - 1));
        this.dirty = true;
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.status.edited"), -7429177);
    }

    private void tryClose() {
        this.syncCurrentTrade(false);
        if (this.dirty && AnkiConfig.isConfirmOnClose()) {
            this.confirmClose = true;
            return;
        }
        class_310.method_1551().method_1507(this.parent);
    }

    private void renderConfirm(class_332 g, int mx, int my, boolean resetMode) {
        int w = 320;
        int h = 118;
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) / 2;
        g.method_25294(0, 0, this.field_22789, this.field_22790, -2013265920);
        g.method_25294(x, y, x + w, y + h, -267909104);
        this.border(g, x, y, w, h, -13878436);
        String title = resetMode ? this.tr("ankinbt.entity.reset_changes") : this.tr("ankinbt.confirm.title");
        int titleColor = resetMode ? -1096636 : -788737;
        VersionCompat.get().drawString(g, this.field_22793, title, x + 10, y + 10, titleColor, false);
        g.method_25294(x + 1, y + 24, x + w - 1, y + 25, -13878436);
        if (resetMode) {
            VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.confirm.discard_hint"), x + 10, y + 33, -2497806, false);
            VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.confirm.unsaved"), x + 10, y + 47, -7429177, false);
        } else {
            VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.confirm.unsaved"), x + 10, y + 33, -2497806, false);
            VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.confirm.discard_hint"), x + 10, y + 47, -7429177, false);
        }
        int by = y + h - 32;
        int bw = 84;
        int bh = 22;
        if (resetMode) {
            int cancelX = x + 12;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
            g.method_25294(cancelX, by, cancelX + bw, by + bh, ch ? 1780954962 : 1243293240);
            this.border(g, cancelX, by, bw, bh, -13878436);
            this.drawBtnText(g, this.tr("ankinbt.edit.cancel"), cancelX, by, bw);
            int applyX = x + w - bw - 12;
            boolean ah = mx >= applyX && mx < applyX + bw && my >= by && my < by + bh;
            g.method_25294(applyX, by, applyX + bw, by + bh, ah ? -1434510051 : -1969677541);
            this.border(g, applyX, by, bw, bh, -1096636);
            this.drawBtnText(g, this.tr("ankinbt.edit.apply"), applyX, by, bw);
            return;
        }
    }

    private void renderUnsavedConfirmLikeSimple(class_332 g, int mx, int my) {
        int dw = 260;
        int dh = 110;
        int dx = (this.field_22789 - dw) / 2;
        int dy = (this.field_22790 - dh) / 2;
        g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
        this.border(g, dx, dy, dw, dh, -1096636);
        VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.confirm.title"), dx + 10, dy + 10, -1906448, false);
        g.method_25294(dx + 1, dy + 24, dx + dw - 1, dy + 25, -14540234);
        VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.confirm.unsaved"), dx + 10, dy + 32, -7035976, false);
        VersionCompat.get().drawString(g, this.field_22793, this.tr("ankinbt.confirm.discard_hint"), dx + 10, dy + 46, -10193781, false);
        int by = dy + dh - 32;
        int bw2 = 70;
        int bh2 = 22;
        int saveX = dx + 10;
        boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
        g.method_25294(saveX, by, saveX + bw2, by + bh2, sh ? -15293622 : -14498466);
        String saveLabel = this.tr("ankinbt.confirm.save_close");
        VersionCompat.get().drawString(g, this.field_22793, saveLabel, saveX + (bw2 - this.field_22793.method_1727(saveLabel)) / 2, by + 7, -1906448, false);
        int discardX = dx + dw / 2 - bw2 / 2;
        boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
        g.method_25294(discardX, by, discardX + bw2, by + bh2, dh2 ? -2131803068 : 1089422404);
        String discardLabel = this.tr("ankinbt.confirm.discard");
        VersionCompat.get().drawString(g, this.field_22793, discardLabel, discardX + (bw2 - this.field_22793.method_1727(discardLabel)) / 2, by + 7, -1906448, false);
        int cancelX = dx + dw - bw2 - 10;
        boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
        g.method_25294(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
        String cancelLabel = this.tr("ankinbt.edit.cancel");
        VersionCompat.get().drawString(g, this.field_22793, cancelLabel, cancelX + (bw2 - this.field_22793.method_1727(cancelLabel)) / 2, by + 7, -7035976, false);
    }

    private void drawBtnText(class_332 g, String text, int x, int y, int w) {
        Object out = text;
        if (this.field_22793.method_1727((String)out) > w - 8) {
            out = this.field_22793.method_27523((String)out, w - 12) + "..";
        }
        VersionCompat.get().drawString(g, this.field_22793, (String)out, x + (w - this.field_22793.method_1727((String)out)) / 2, y + 7, -2497806, false);
    }

    private boolean clickConfirm(int mx, int my) {
        if (this.confirmClose) {
            return this.clickUnsavedConfirmLikeSimple(mx, my);
        }
        int w = 320;
        int h = 118;
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) / 2;
        int by = y + h - 32;
        int bw = 84;
        int bh = 22;
        if (this.confirmReset) {
            int cancelX = x + 12;
            if (mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh) {
                this.confirmReset = false;
                return true;
            }
            int applyX = x + w - bw - 12;
            if (mx >= applyX && mx < applyX + bw && my >= by && my < by + bh) {
                this.confirmReset = false;
                this.resetForm();
                return true;
            }
            return true;
        }
        return true;
    }

    private boolean clickUnsavedConfirmLikeSimple(int mx, int my) {
        int dw = 260;
        int dh = 110;
        int dx = (this.field_22789 - dw) / 2;
        int dy = (this.field_22790 - dh) / 2;
        int by = dy + dh - 32;
        int bw2 = 70;
        int bh2 = 22;
        int saveX = dx + 10;
        if (mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2) {
            this.applyTrade();
            if (!this.dirty) {
                this.confirmClose = false;
                class_310.method_1551().method_1507(this.parent);
            }
            return true;
        }
        int discardX = dx + dw / 2 - bw2 / 2;
        if (mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2) {
            this.confirmClose = false;
            this.dirty = false;
            class_310.method_1551().method_1507(this.parent);
            return true;
        }
        int cancelX = dx + dw - bw2 - 10;
        if (mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2) {
            this.confirmClose = false;
            return true;
        }
        return true;
    }

    private void drawRightLabel(class_332 g, String text, int x, int y, int maxW) {
        Object out;
        if (maxW <= 8) {
            return;
        }
        Object object = out = text == null ? "" : text;
        if (this.field_22793.method_1727((String)out) > maxW) {
            out = this.field_22793.method_27523((String)out, maxW - 4) + "..";
        }
        VersionCompat.get().drawString(g, this.field_22793, (String)out, x, y, -7429177, false);
    }

    private void setBoxValue(class_342 box, String value) {
        if (box == null) {
            return;
        }
        boolean old = this.suppressDirtySync;
        this.suppressDirtySync = true;
        box.method_1852(value == null ? "" : value);
        this.suppressDirtySync = old;
    }

    private void setPickedComponents(class_342 box, class_2487 components) {
        this.ensureTrades();
        TradeData t = this.trades.get(this.tradeIndex);
        if (box == this.buyId) {
            t.buyComponents = this.copyCompound(components);
        } else if (box == this.buy2Id) {
            t.buy2Components = this.copyCompound(components);
        } else if (box == this.sellId) {
            t.sellComponents = this.copyCompound(components);
        }
    }

    private class_2487 readPickedStackData(class_1799 stack) {
        class_2487 components = this.readItemComponents(stack);
        if (components == null || components.method_33133()) {
            return null;
        }
        try {
            Optional<class_2487> fullOpt = NbtHelper.serializeItemStack(stack);
            if (fullOpt.isPresent() && !fullOpt.get().method_33133()) {
                class_2487 wrapped = this.copyCompound(components);
                wrapped.method_10566(FULL_STACK_KEY, (class_2520)this.copyCompound(fullOpt.get()));
                return wrapped;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return components;
    }

    private class_2487 readItemComponents(class_1799 stack) {
        try {
            Optional<class_2487> fullOpt = NbtHelper.serializeItemStack(stack);
            if (fullOpt.isEmpty()) {
                return null;
            }
            class_2487 full = fullOpt.get();
            class_2487 components = this.readCompound(full, "components");
            if (components != null && !components.method_33133()) {
                return this.copyCompound(components);
            }
            class_2487 legacy = this.readCompound(full, "tag");
            if (legacy != null && !legacy.method_33133()) {
                class_2487 wrapped = new class_2487();
                wrapped.method_10566("minecraft:custom_data", (class_2520)this.copyCompound(legacy));
                return wrapped;
            }
            return null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private class_2487 buildTradeStackTag(String itemId, int count, class_2487 components) {
        class_2487 out = new class_2487();
        out.method_10582("id", itemId);
        out.method_10569("count", Math.max(1, count));
        class_2487 fullStack = this.readWrappedFullStack(components);
        if (fullStack != null && !fullStack.method_33133()) {
            class_2487 legacyTag;
            class_2487 full = this.copyCompound(fullStack);
            full.method_10582("id", itemId);
            full.method_10569("count", Math.max(1, count));
            class_2487 fullComponents = this.readCompound(full, "components");
            if (fullComponents != null && !fullComponents.method_33133()) {
                out.method_10566("components", (class_2520)this.copyCompound(fullComponents));
            }
            if ((legacyTag = this.readCompound(full, "tag")) != null && !legacyTag.method_33133()) {
                out.method_10566("tag", (class_2520)this.copyCompound(legacyTag));
            }
            return out;
        }
        class_2487 plainComponents = this.unwrapTradeComponents(components);
        if (plainComponents != null && !plainComponents.method_33133()) {
            out.method_10566("components", (class_2520)this.copyCompound(plainComponents));
        }
        return out;
    }

    private class_2487 readWrappedFullStack(class_2487 components) {
        class_2487 full = this.readCompound(components, FULL_STACK_KEY);
        return full == null || full.method_33133() ? null : this.copyCompound(full);
    }

    private class_2487 unwrapTradeComponents(class_2487 components) {
        if (components == null || components.method_33133()) {
            return null;
        }
        class_2487 plain = this.copyCompound(components);
        plain.method_10551(FULL_STACK_KEY);
        return plain.method_33133() ? null : plain;
    }

    private static class_2487 demoComponents(class_2561 name, int color) {
        class_1799 preview = new class_1799((class_1935)class_1802.field_8407);
        class_5250 title = name.method_27661().method_27696(class_2583.field_24360.method_10978(Boolean.valueOf(false)).method_27703(class_5251.method_27717((int)color)));
        class_5250 lore = class_2561.method_43470((String)"\u9ed8\u8ba4\u793a\u4f8b/Default example").method_27696(class_2583.field_24360.method_10978(Boolean.valueOf(false)));
        preview.method_57379(class_9334.field_49631, (Object)title);
        preview.method_57379(class_9334.field_49632, (Object)new class_9290(List.of(lore)));
        try {
            Optional<class_2487> fullOpt = NbtHelper.serializeItemStack(preview);
            if (fullOpt.isEmpty()) {
                return null;
            }
            class_2487 full = fullOpt.get();
            class_2487 components = VillagerTradeEditorScreen.readStaticCompound(full, "components");
            if (components != null && !components.method_33133()) {
                return components;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static class_2487 readStaticCompound(class_2487 parent, String key) {
        Optional opt;
        if (parent == null || key == null || key.isBlank()) {
            return null;
        }
        try {
            Object var5_7;
            Object out = parent.getClass().getMethod("getCompound", String.class).invoke((Object)parent, key);
            if (out instanceof class_2487) {
                class_2487 ct = (class_2487)out;
                return ct;
            }
            if (out instanceof Optional && (var5_7 = (opt = (Optional)out).orElse(null)) instanceof class_2487) {
                class_2487 ct = var5_7;
                return ct;
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        try {
            Object raw = parent.getClass().getMethod("get", String.class).invoke((Object)parent, key);
            if (raw instanceof Optional) {
                opt = (Optional)raw;
                raw = opt.orElse(null);
            }
            if (raw instanceof class_2487) {
                class_2487 ct = (class_2487)raw;
                return ct;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private class_2487 copyCompound(class_2487 source) {
        if (source == null) {
            return null;
        }
        class_2487 out = new class_2487();
        out.method_10543(source);
        return out;
    }

    private int defaultProfessionIndex() {
        return this.isWanderingTraderContext() ? 0 : 1;
    }

    private int normalizeProfessionIndex(int index) {
        if (this.isWanderingTraderContext()) {
            return 0;
        }
        if (index < 0 || index >= PROFESSIONS.length) {
            return this.defaultProfessionIndex();
        }
        return this.isTradeableProfession(PROFESSIONS[index]) ? index : this.defaultProfessionIndex();
    }

    private String normalizeProfessionId(String professionId) {
        if (this.isWanderingTraderContext()) {
            return "";
        }
        return this.isTradeableProfession(professionId) ? professionId : "minecraft:farmer";
    }

    private void normalizeProfessionState() {
        this.professionIndex = this.normalizeProfessionIndex(this.professionIndex);
        this.villagerLevel = Math.max(1, Math.min(5, this.villagerLevel));
        if (this.villagerType == null || this.villagerType.isBlank()) {
            this.villagerType = "minecraft:plains";
        }
    }

    private class_2499 copyListTag(class_2499 source) {
        class_2499 out = new class_2499();
        if (source == null) {
            return out;
        }
        for (int i = 0; i < source.size(); ++i) {
            Object entry = this.unwrapOptional(source.method_10534(i));
            if (entry instanceof class_2487) {
                class_2487 ct = (class_2487)entry;
                out.add((Object)this.copyCompound(ct));
                continue;
            }
            if (!(entry instanceof class_2520)) continue;
            class_2520 tag = (class_2520)entry;
            out.add((Object)tag.method_10707());
        }
        return out;
    }

    private void injectRuntimeOffersIfMissing(class_2487 root, class_1297 entity) {
        if (root == null || entity == null) {
            return;
        }
        class_2487 offers = this.readCompound(root, "Offers");
        if (offers == null) {
            offers = this.readCompound(root, "offers");
        }
        if (this.hasRecipeList(offers, "Recipes") || this.hasRecipeList(offers, "recipes")) {
            return;
        }
        class_2499 runtime = this.readRuntimeOffers(entity);
        if (runtime == null || runtime.isEmpty()) {
            return;
        }
        class_2487 outOffers = offers == null ? new class_2487() : this.copyCompound(offers);
        outOffers.method_10566("Recipes", (class_2520)runtime);
        outOffers.method_10566("recipes", (class_2520)this.copyListTag(runtime));
        root.method_10566("Offers", (class_2520)outOffers);
        DebugLog.info("Injected runtime villager offers: {} entries", runtime.size());
    }

    private void injectRuntimeVillagerDataIfMissing(class_2487 root, class_1297 entity) {
        class_3850 data;
        if (root == null || entity == null) {
            return;
        }
        class_2487 current = this.readCompound(root, "VillagerData");
        if (current != null && !current.method_33133()) {
            return;
        }
        if (entity instanceof class_1646) {
            class_1646 villager = (class_1646)entity;
            v0 = villager.method_7231();
        } else {
            v0 = data = this.invokeAny((Object)entity, "getVillagerData");
        }
        if (data == null) {
            return;
        }
        String professionId = this.extractNamespacedId(this.invokeAny((Object)data, "getProfession", "profession"));
        String typeId = this.extractNamespacedId(this.invokeAny((Object)data, "getType", "type"));
        Integer level = this.invokeInt(data, "getLevel");
        class_2487 vd = new class_2487();
        vd.method_10582("profession", professionId == null || professionId.isBlank() ? "minecraft:farmer" : professionId);
        vd.method_10582("type", typeId == null || typeId.isBlank() ? "minecraft:plains" : typeId);
        vd.method_10569("level", Math.max(1, Math.min(5, level == null ? 1 : level)));
        root.method_10566("VillagerData", (class_2520)vd);
    }

    private boolean hasRecipeList(class_2487 offers, String key) {
        class_2499 list;
        if (offers == null || key == null || key.isBlank()) {
            return false;
        }
        Object raw = this.readTag(offers, key);
        return raw instanceof class_2499 && !(list = (class_2499)raw).isEmpty();
    }

    private class_2499 readRuntimeOffers(class_1297 entity) {
        class_2499 serverMirror = this.readRuntimeOffersFromIntegratedServer(entity);
        if (serverMirror != null && !serverMirror.isEmpty()) {
            return serverMirror;
        }
        class_2499 reflective = this.readOffersFromEntityObject(entity);
        if (reflective != null && !reflective.isEmpty()) {
            return reflective;
        }
        return null;
    }

    private LoadedVillagerDefaults readDefaultsFromIntegratedServer(class_1297 clientEntity) {
        if (clientEntity == null) {
            return null;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || !mc.method_1496()) {
            return null;
        }
        class_1132 server = mc.method_1576();
        if (server == null) {
            return null;
        }
        AtomicReference ref = new AtomicReference();
        CountDownLatch latch = new CountDownLatch(1);
        server.execute(() -> {
            try {
                class_2499 recipes;
                class_1297 serverEntity = EditorCommandHelper.findIntegratedServerEntity(server, clientEntity.method_5628(), clientEntity.method_5667());
                if (!(serverEntity instanceof class_3988)) {
                    return;
                }
                class_3988 villager = (class_3988)serverEntity;
                int liveProfessionIndex = this.professionIndex;
                int liveLevel = this.villagerLevel;
                String liveType = this.villagerType;
                class_2487 serverTag = this.readEntityTag(serverEntity);
                class_2487 liveVillagerData = this.readCompound(serverTag, "VillagerData");
                if (liveVillagerData != null) {
                    String professionId = this.readString(liveVillagerData, "profession", "");
                    int idx = this.professionIndexById(professionId);
                    if (idx >= 0) {
                        liveProfessionIndex = idx;
                    }
                    liveLevel = Math.max(1, Math.min(5, this.readInt(liveVillagerData, "level", liveLevel)));
                    String typeId = this.readString(liveVillagerData, "type", liveType);
                    if (typeId != null && !typeId.isBlank()) {
                        liveType = typeId;
                    }
                } else if (serverEntity instanceof class_1646) {
                    class_3850 data;
                    class_1646 liveVillager = (class_1646)serverEntity;
                    if (!this.isWanderingTraderContext() && (data = liveVillager.method_7231()) != null) {
                        String typeId;
                        Integer level;
                        String professionId = this.extractNamespacedId(this.invokeAny((Object)data, "getProfession", "profession"));
                        int idx = this.professionIndexById(professionId);
                        if (idx >= 0) {
                            liveProfessionIndex = idx;
                        }
                        if ((level = this.invokeInt(data, "getLevel")) != null) {
                            liveLevel = Math.max(1, Math.min(5, level));
                        }
                        if ((typeId = this.extractNamespacedId(this.invokeAny((Object)data, "getType", "type"))) != null && !typeId.isBlank()) {
                            liveType = typeId;
                        }
                    }
                }
                boolean liveRewardExp = this.rewardExp;
                ArrayList<TradeData> liveTrades = new ArrayList<TradeData>();
                class_1916 offers = villager.method_8264();
                if (offers != null && !offers.isEmpty()) {
                    for (class_1914 offer : offers) {
                        liveTrades.add(this.tradeFromMerchantOffer(offer));
                        liveRewardExp = offer.method_8256();
                    }
                    DebugLog.info("Loaded villager offers from integrated merchant API: {}", liveTrades.size());
                }
                if (liveTrades.isEmpty() && serverTag != null && (recipes = this.extractOfferRecipes(serverTag)) != null && !recipes.isEmpty()) {
                    for (int i = 0; i < recipes.size(); ++i) {
                        Object entry = this.unwrapOptional(recipes.method_10534(i));
                        if (!(entry instanceof class_2487)) continue;
                        class_2487 recipe = (class_2487)entry;
                        liveTrades.add(this.tradeFromRecipe(recipe));
                        Object re = this.readTag(recipe, "rewardExp");
                        if (re == null) continue;
                        try {
                            Object b = re.getClass().getMethod("getAsBoolean", new Class[0]).invoke(re, new Object[0]);
                            if (!(b instanceof Boolean)) continue;
                            Boolean bb = (Boolean)b;
                            liveRewardExp = bb;
                            continue;
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                    DebugLog.info("Loaded villager offers from integrated entity tag: {}", liveTrades.size());
                }
                ref.set(new LoadedVillagerDefaults(liveProfessionIndex, liveLevel, liveType, liveRewardExp, liveTrades));
            }
            catch (Throwable t) {
                DebugLog.warn("Integrated villager defaults read failed: {}", t.toString());
            }
            finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(3L, TimeUnit.SECONDS)) {
                DebugLog.warn("Timed out waiting for integrated villager defaults", new Object[0]);
                return null;
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return (LoadedVillagerDefaults)ref.get();
    }

    private class_2499 readRuntimeOffersFromIntegratedServer(class_1297 clientEntity) {
        if (clientEntity == null) {
            return null;
        }
        try {
            class_310 mc = class_310.method_1551();
            if (mc == null || !mc.method_1496()) {
                return null;
            }
            class_1132 server = mc.method_1576();
            if (server == null) {
                return null;
            }
            int targetId = clientEntity.method_5628();
            UUID targetUuid = clientEntity.method_5667();
            for (class_3218 level : server.method_3738()) {
                class_3988 villager;
                class_1916 offers;
                class_1297 serverEntity = level.method_8469(targetId);
                if (serverEntity == null || !targetUuid.equals(serverEntity.method_5667())) {
                    serverEntity = this.findServerEntityByUuid(level, targetUuid);
                }
                if (serverEntity == null) continue;
                if (serverEntity instanceof class_3988 && (offers = (villager = (class_3988)serverEntity).method_8264()) != null && !offers.isEmpty()) {
                    DebugLog.info("Loaded villager offers from integrated server merchant API: {}", offers.size());
                    return this.merchantOffersToList(offers);
                }
                class_2487 serverTag = this.readEntityTag(serverEntity);
                offers = this.extractOfferRecipes(serverTag);
                if (offers == null || offers.isEmpty()) continue;
                DebugLog.info("Loaded villager offers from integrated server mirror: {}", offers.size());
                return offers;
            }
        }
        catch (Throwable t) {
            DebugLog.warn("Integrated server villager offer mirror read failed: {}", t.toString());
        }
        return null;
    }

    private class_1297 findServerEntityByUuid(class_3218 level, UUID uuid) {
        if (level == null || uuid == null) {
            return null;
        }
        try {
            Object out = level.getClass().getMethod("getEntity", UUID.class).invoke((Object)level, uuid);
            if (out instanceof class_1297) {
                class_1297 entity = (class_1297)out;
                return entity;
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        try {
            Object all = level.getClass().getMethod("getAllEntities", new Class[0]).invoke((Object)level, new Object[0]);
            if (all instanceof Iterable) {
                Iterable iterable = (Iterable)all;
                for (Object value : iterable) {
                    class_1297 entity;
                    if (!(value instanceof class_1297) || !uuid.equals((entity = (class_1297)value).method_5667())) continue;
                    return entity;
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private class_2499 readOffersFromEntityObject(Object entityLike) {
        class_1916 merchantOffers;
        if (entityLike == null) {
            return null;
        }
        Object offersObj = this.invokeAny(entityLike, "getOffers", "getRecipes", "getTrades");
        if (offersObj == null) {
            return null;
        }
        if (offersObj instanceof class_1916 && !(merchantOffers = (class_1916)offersObj).isEmpty()) {
            return this.merchantOffersToList(merchantOffers);
        }
        class_2499 direct = this.invokeListTag(offersObj, "createTag", "toTag", "save");
        if (direct == null || direct.isEmpty()) {
            direct = this.invokeListTagArg(offersObj, "save", new class_2499());
        }
        if (direct != null && !direct.isEmpty()) {
            return this.copyListTag(direct);
        }
        class_2499 out = new class_2499();
        if (offersObj instanceof Iterable) {
            Iterable iterable = (Iterable)offersObj;
            for (Object offer : iterable) {
                class_2487 tag = this.serializeOffer(offer);
                if (tag == null || tag.method_33133()) continue;
                out.add((Object)tag);
            }
            return out;
        }
        if (offersObj instanceof List) {
            List list = (List)offersObj;
            for (Object offer : list) {
                class_2487 tag = this.serializeOffer(offer);
                if (tag == null || tag.method_33133()) continue;
                out.add((Object)tag);
            }
            return out;
        }
        Integer size = this.invokeInt(offersObj, "size");
        if (size == null || size <= 0) {
            return out;
        }
        for (int i = 0; i < size; ++i) {
            Object offer = this.invokeAny(offersObj, "get", i);
            class_2487 tag = this.serializeOffer(offer);
            if (tag == null || tag.method_33133()) continue;
            out.add((Object)tag);
        }
        return out.isEmpty() ? null : out;
    }

    private class_2499 extractOfferRecipes(class_2487 root) {
        class_2499 recipes;
        if (root == null) {
            return null;
        }
        class_2487 offers = this.readCompound(root, "Offers");
        if (offers == null) {
            offers = this.readCompound(root, "offers");
        }
        if (offers == null) {
            return null;
        }
        Object raw = this.readTag(offers, "Recipes");
        if (!(raw instanceof class_2499)) {
            raw = this.readTag(offers, "recipes");
        }
        if (!(raw instanceof class_2499)) {
            raw = this.readTag(offers, "Trades");
        }
        if (!(raw instanceof class_2499)) {
            raw = this.readTag(offers, "trades");
        }
        if (raw instanceof class_2499 && !(recipes = (class_2499)raw).isEmpty()) {
            return this.copyListTag(recipes);
        }
        return null;
    }

    private class_2499 merchantOffersToList(class_1916 offers) {
        class_2499 out = new class_2499();
        for (class_1914 offer : offers) {
            class_2487 tag = this.merchantOfferToTag(offer);
            if (tag == null || tag.method_33133()) continue;
            out.add((Object)tag);
        }
        return out;
    }

    private class_2487 merchantOfferToTag(class_1914 offer) {
        if (offer == null) {
            return null;
        }
        class_2487 buy = this.stackToTag(offer.method_8246());
        class_2487 buyB = this.stackToTag(offer.method_8247());
        class_2487 sell = this.stackToTag(offer.method_8250());
        if (buy == null || sell == null) {
            return null;
        }
        class_2487 recipe = new class_2487();
        recipe.method_10566("buy", (class_2520)buy);
        recipe.method_10566("base_cost_a", (class_2520)this.copyCompound(buy));
        recipe.method_10566("sell", (class_2520)sell);
        recipe.method_10566("result", (class_2520)this.copyCompound(sell));
        if (buyB != null && !buyB.method_33133()) {
            recipe.method_10566("buyB", (class_2520)buyB);
            recipe.method_10566("cost_b", (class_2520)this.copyCompound(buyB));
        }
        recipe.method_10569("maxUses", Math.max(1, offer.method_8248()));
        recipe.method_10569("uses", Math.max(0, offer.method_8249()));
        recipe.method_10569("xp", Math.max(0, offer.method_19279()));
        recipe.method_10548("priceMultiplier", offer.method_19278());
        recipe.method_10556("rewardExp", offer.method_8256());
        recipe.method_10569("specialPrice", offer.method_19277());
        recipe.method_10569("demand", offer.method_21725());
        return recipe;
    }

    private class_2487 serializeOffer(Object offer) {
        Boolean reward;
        Float mul;
        Integer xpVal;
        Integer uses;
        Integer maxUses;
        class_1914 merchantOffer;
        class_2487 direct;
        if (offer == null) {
            return null;
        }
        if (offer instanceof class_1914 && (direct = this.merchantOfferToTag(merchantOffer = (class_1914)offer)) != null && !direct.method_33133()) {
            return direct;
        }
        class_2487 fromApi = this.invokeCompound(offer, "createTag");
        if (fromApi == null) {
            fromApi = this.invokeCompound(offer, "save");
        }
        if (fromApi == null) {
            fromApi = this.invokeCompoundArg(offer, "save", new class_2487());
        }
        if (fromApi == null) {
            fromApi = this.invokeCompound(offer, "toTag");
        }
        if (fromApi != null && !fromApi.method_33133()) {
            return fromApi;
        }
        class_2487 buy = this.itemLikeToStackTag(this.invokeAny(offer, "getBaseCostA", "getCostA", "getBuyItem", "getFirstBuyItem"));
        class_2487 buyB = this.itemLikeToStackTag(this.invokeAny(offer, "getCostB", "getSecondCost", "getSecondBuyItem"));
        class_2487 sell = this.itemLikeToStackTag(this.invokeAny(offer, "getResult", "getSellItem", "getOutput"));
        if (buy == null || sell == null) {
            return null;
        }
        class_2487 recipe = new class_2487();
        recipe.method_10566("buy", (class_2520)buy);
        recipe.method_10566("base_cost_a", (class_2520)this.copyCompound(buy));
        recipe.method_10566("sell", (class_2520)sell);
        recipe.method_10566("result", (class_2520)this.copyCompound(sell));
        if (buyB != null && !buyB.method_33133()) {
            recipe.method_10566("buyB", (class_2520)buyB);
            recipe.method_10566("cost_b", (class_2520)this.copyCompound(buyB));
        }
        if ((maxUses = this.invokeInt(offer, "getMaxUses")) != null) {
            recipe.method_10569("maxUses", Math.max(1, maxUses));
        }
        if ((uses = this.invokeInt(offer, "getUses")) != null) {
            recipe.method_10569("uses", Math.max(0, uses));
        }
        if ((xpVal = this.invokeInt(offer, "getXp")) != null) {
            recipe.method_10569("xp", Math.max(0, xpVal));
        }
        if ((mul = this.invokeFloat(offer, "getPriceMultiplier")) != null) {
            recipe.method_10548("priceMultiplier", mul.floatValue());
        }
        if ((reward = this.invokeBool(offer, "shouldRewardExp")) == null) {
            reward = this.invokeBool(offer, "isRewardExp");
        }
        if (reward != null) {
            recipe.method_10556("rewardExp", reward.booleanValue());
        }
        return recipe;
    }

    private class_2487 itemLikeToStackTag(Object itemLike) {
        Matcher matcher;
        if (itemLike == null) {
            return null;
        }
        if (itemLike instanceof class_1799) {
            class_1799 stack = (class_1799)itemLike;
            return this.stackToTag(stack);
        }
        Object stack = this.invokeAny(itemLike, "itemStack", "stack", "toItemStack", "asStack");
        if (stack instanceof class_1799) {
            class_1799 st = (class_1799)stack;
            return this.stackToTag(st);
        }
        String id = "";
        int count = 1;
        Object itemObj = this.invokeAny(itemLike, "item", "getItem", "value");
        if (itemObj instanceof class_1792) {
            class_1792 item = (class_1792)itemObj;
            id = ItemRegistryHelper.getItemId(item);
        } else if (itemObj != null && (matcher = ITEM_ID_PATTERN.matcher(String.valueOf(itemObj).toLowerCase(Locale.ROOT))).find()) {
            id = matcher.group(1);
        }
        Integer c = this.invokeInt(itemLike, "count");
        if (c == null) {
            c = this.invokeInt(itemLike, "getCount");
        }
        if (c != null) {
            count = Math.max(1, c);
        }
        if (id.isBlank() && (matcher = ITEM_ID_PATTERN.matcher(String.valueOf(itemLike).toLowerCase(Locale.ROOT))).find()) {
            id = matcher.group(1);
        }
        if (id.isBlank()) {
            return null;
        }
        class_2487 out = new class_2487();
        out.method_10582("id", id);
        out.method_10569("count", count);
        return out;
    }

    private String extractNamespacedId(Object value) {
        String id;
        Object out2;
        if (value == null) {
            return null;
        }
        try {
            out2 = value.getClass().getMethod("location", new Class[0]).invoke(value, new Object[0]);
            if (out2 != null) {
                return String.valueOf(out2);
            }
        }
        catch (Throwable out2) {
            // empty catch block
        }
        try {
            out2 = value.getClass().getMethod("key", new Class[0]).invoke(value, new Object[0]);
            id = this.extractNamespacedId(out2);
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        catch (Throwable out3) {
            // empty catch block
        }
        try {
            out2 = value.getClass().getMethod("unwrapKey", new Class[0]).invoke(value, new Object[0]);
            id = this.extractNamespacedId(this.unwrapOptional(out2));
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        catch (Throwable out4) {
            // empty catch block
        }
        String text = String.valueOf(value).toLowerCase(Locale.ROOT);
        Matcher matcher = ITEM_ID_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private class_2487 stackToTag(class_1799 stack) {
        class_2487 legacyTag;
        if (stack == null || stack.method_7960()) {
            return null;
        }
        Optional<class_2487> fullOpt = NbtHelper.serializeItemStack(stack);
        if (fullOpt.isEmpty()) {
            return null;
        }
        class_2487 full = fullOpt.get();
        class_2487 out = new class_2487();
        out.method_10582("id", this.readString(full, "id", SpawnEggEditorHelper.getItemId(stack)));
        out.method_10569("count", Math.max(1, this.readInt(full, "count", stack.method_7947())));
        class_2487 components = this.readCompound(full, "components");
        if (components != null && !components.method_33133()) {
            out.method_10566("components", (class_2520)this.copyCompound(components));
        }
        if ((legacyTag = this.readCompound(full, "tag")) != null && !legacyTag.method_33133()) {
            out.method_10566("tag", (class_2520)this.copyCompound(legacyTag));
        }
        return out;
    }

    private boolean applyTradeToIntegratedServer(class_310 mc, class_2487 patch) {
        class_1132 server;
        if (mc == null || this.targetEntity == null) {
            return false;
        }
        try {
            server = mc.method_1576();
        }
        catch (Throwable ignored) {
            return false;
        }
        if (server == null) {
            return false;
        }
        AtomicBoolean success = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        server.execute(() -> {
            try {
                class_1297 serverEntity = EditorCommandHelper.findIntegratedServerEntity(server, this.targetEntity.method_5628(), this.targetEntity.method_5667());
                if (!(serverEntity instanceof class_3988)) {
                    return;
                }
                class_3988 serverVillager = (class_3988)serverEntity;
                class_2487 mergedTag = this.readEntityTag(serverEntity);
                boolean loadedFromTag = false;
                if (mergedTag != null && patch != null && !patch.method_33133()) {
                    mergedTag.method_10543(this.copyCompound(patch));
                    if (this.loadEntityTag(serverEntity, mergedTag)) {
                        loadedFromTag = true;
                    }
                }
                class_1916 offers = this.buildMerchantOffers();
                class_1916 live = serverVillager.method_8264();
                live.clear();
                for (class_1914 offer : offers) {
                    live.add((Object)offer.method_53881());
                }
                if (serverEntity instanceof class_1646) {
                    class_1646 villager = (class_1646)serverEntity;
                    if (!this.isWanderingTraderContext()) {
                        class_3218 level;
                        class_1937 patt0$temp = serverEntity.method_37908();
                        class_3218 serverLevel = patt0$temp instanceof class_3218 ? (level = (class_3218)patt0$temp) : null;
                        this.applyVillagerData(villager, serverLevel);
                    }
                }
                success.set(loadedFromTag || !offers.isEmpty() || serverEntity instanceof class_1646 || serverVillager != null);
            }
            catch (Throwable t) {
                DebugLog.warn("Integrated villager trade apply failed: {}", t.toString());
            }
            finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(3L, TimeUnit.SECONDS)) {
                DebugLog.warn("Timed out waiting for integrated villager trade apply", new Object[0]);
                return false;
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return success.get();
    }

    private boolean loadEntityTag(class_1297 entity, class_2487 tag) {
        if (entity == null || tag == null || tag.method_33133()) {
            return false;
        }
        for (Class<?> type = entity.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Method method = type.getDeclaredMethod("load", class_2487.class);
                method.setAccessible(true);
                method.invoke((Object)entity, tag);
                return true;
            }
            catch (NoSuchMethodException ignored) {
                continue;
            }
            catch (Throwable ignored) {
                return false;
            }
        }
        return false;
    }

    private void applyTradePreviewToClient() {
        class_1297 class_12972 = this.targetEntity;
        if (class_12972 instanceof class_1646) {
            class_1646 villager = (class_1646)class_12972;
            if (!this.isWanderingTraderContext()) {
                this.applyVillagerData(villager, null);
            }
        }
    }

    private void applyVillagerData(class_1646 villager, class_3218 serverLevel) {
        if (villager == null) {
            return;
        }
        String desiredProfession = this.normalizeProfessionId(PROFESSIONS[this.professionIndex]);
        String desiredType = this.villagerType == null || this.villagerType.isBlank() ? "minecraft:plains" : this.villagerType;
        int desiredLevel = Math.max(1, Math.min(5, this.villagerLevel));
        class_3850 desired = this.buildVillagerData(villager);
        villager.method_7195(desired);
        villager.method_19625(Math.max(0, this.villagerLevel * 10));
        if (serverLevel != null) {
            this.invokeCompatible(villager, "refreshBrain", serverLevel);
            if (!this.matchesVillagerData(villager.method_7231(), desiredType, desiredProfession, desiredLevel)) {
                class_2487 tag = this.readEntityTag((class_1297)villager);
                if (tag != null) {
                    class_2487 villagerDataTag = new class_2487();
                    villagerDataTag.method_10582("type", desiredType);
                    villagerDataTag.method_10582("profession", desiredProfession);
                    villagerDataTag.method_10569("level", desiredLevel);
                    tag.method_10566("VillagerData", (class_2520)villagerDataTag);
                    tag.method_10569("Xp", Math.max(0, desiredLevel * 10));
                    if (this.loadEntityTag((class_1297)villager, tag)) {
                        class_3850 rebuilt;
                        villager.method_19625(Math.max(0, desiredLevel * 10));
                        this.invokeCompatible(villager, "refreshBrain", serverLevel);
                        if (!this.matchesVillagerData(villager.method_7231(), desiredType, desiredProfession, desiredLevel) && this.matchesVillagerData(rebuilt = this.buildVillagerData(villager), desiredType, desiredProfession, desiredLevel)) {
                            villager.method_7195(rebuilt);
                            villager.method_19625(Math.max(0, desiredLevel * 10));
                            this.invokeCompatible(villager, "refreshBrain", serverLevel);
                        }
                    }
                }
                if (!this.matchesVillagerData(villager.method_7231(), desiredType, desiredProfession, desiredLevel) && this.forceVillagerDataByCommand(serverLevel, villager, desiredType, desiredProfession, desiredLevel)) {
                    this.invokeCompatible(villager, "refreshBrain", serverLevel);
                }
            }
        }
    }

    private boolean forceVillagerDataByCommand(class_3218 serverLevel, class_1646 villager, String desiredType, String desiredProfession, int desiredLevel) {
        if (serverLevel == null || villager == null) {
            return false;
        }
        try {
            class_2487 villagerDataTag = new class_2487();
            villagerDataTag.method_10582("type", desiredType);
            villagerDataTag.method_10582("profession", desiredProfession);
            villagerDataTag.method_10569("level", desiredLevel);
            class_2487 patch = new class_2487();
            patch.method_10566("VillagerData", (class_2520)villagerDataTag);
            patch.method_10569("Xp", Math.max(0, desiredLevel * 10));
            String command = "data merge entity " + EditorCommandHelper.selectorByUuid(villager.method_5667()) + " " + String.valueOf(patch);
            class_2168 source = serverLevel.method_8503().method_3739();
            try {
                source = source.method_9206(4);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                source = source.method_9217();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            serverLevel.method_8503().method_3734().method_44252(source, command);
            return this.matchesVillagerData(villager.method_7231(), desiredType, desiredProfession, desiredLevel);
        }
        catch (Throwable t) {
            DebugLog.warn("Villager profession command apply failed: {}", t.toString());
            return false;
        }
    }

    private class_1916 buildMerchantOffers() {
        class_1916 offers = new class_1916();
        for (TradeData t : this.trades) {
            class_9306 firstCost;
            class_1799 buy = this.buildPreviewStack(t.buyId, t.buyComponents, Math.max(1, t.buyCount));
            class_1799 sell = this.buildPreviewStack(t.sellId, t.sellComponents, Math.max(1, t.sellCount));
            if (buy.method_7960() || sell.method_7960() || (firstCost = this.toItemCost(buy)) == null) continue;
            Optional<Object> secondCost = Optional.empty();
            if (!t.buy2Id.isBlank()) {
                class_1799 buy2 = this.buildPreviewStack(t.buy2Id, t.buy2Components, Math.max(1, t.buy2Count));
                class_9306 extraCost = this.toItemCost(buy2);
                if (extraCost == null) continue;
                secondCost = Optional.of(extraCost);
            }
            offers.add((Object)new class_1914(firstCost, secondCost, sell.method_7972(), 0, Math.max(1, t.maxUses), Math.max(0, t.xp), 0.0f, 0));
        }
        return offers;
    }

    private class_9306 toItemCost(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        Object predicate = this.buildItemCostPredicate(stack);
        if (predicate != null) {
            for (Constructor<?> ctor : class_9306.class.getConstructors()) {
                Object itemArg;
                Class<?>[] p = ctor.getParameterTypes();
                Object object = itemArg = p.length >= 1 ? this.resolveItemCostItemArg(p[0], stack) : null;
                if (itemArg == null || p.length < 3 || p[1] != Integer.TYPE || !p[2].isInstance(predicate)) continue;
                try {
                    Object out;
                    if (p.length == 4 && p[3].isInstance(stack)) {
                        out = ctor.newInstance(itemArg, stack.method_7947(), predicate, stack.method_7972());
                        if (!(out instanceof class_9306)) continue;
                        class_9306 itemCost = (class_9306)out;
                        return itemCost;
                    }
                    if (p.length != 3 || !((out = ctor.newInstance(itemArg, stack.method_7947(), predicate)) instanceof class_9306)) continue;
                    class_9306 itemCost = (class_9306)out;
                    return itemCost;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        return new class_9306((class_1935)stack.method_7909(), stack.method_7947());
    }

    private Object buildItemCostPredicate(class_1799 stack) {
        Object predicate = this.tryBuildItemCostPredicate("net.minecraft.core.component.DataComponentExactPredicate", stack);
        if (predicate != null) {
            return predicate;
        }
        return this.tryBuildItemCostPredicate("net.minecraft.core.component.DataComponentPredicate", stack);
    }

    private Object tryBuildItemCostPredicate(String className, class_1799 stack) {
        try {
            Class<?> predicateClass = Class.forName(className);
            if (stack.method_57353().method_57837()) {
                try {
                    return predicateClass.getField("EMPTY").get(null);
                }
                catch (Throwable throwable) {
                    try {
                        Object builder = predicateClass.getMethod("builder", new Class[0]).invoke(null, new Object[0]);
                        return builder.getClass().getMethod("build", new Class[0]).invoke(builder, new Object[0]);
                    }
                    catch (Throwable builder) {
                        return null;
                    }
                }
            }
            Class<?> componentMapClass = Class.forName("net.minecraft.core.component.DataComponentMap");
            return predicateClass.getMethod("allOf", componentMapClass).invoke(null, stack.method_57353());
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private Object resolveItemCostItemArg(Class<?> paramType, class_1799 stack) {
        try {
            if (paramType.isInstance(stack.method_41409())) {
                return stack.method_41409();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return paramType.isInstance(stack.method_7909()) ? stack.method_7909() : null;
    }

    private class_3850 buildVillagerData(class_1646 villager) {
        class_3850 data;
        class_3850 current = villager.method_7231();
        Object currentType = this.invokeAny((Object)current, "type", "getType");
        Object currentProfession = this.invokeAny((Object)current, "profession", "getProfession");
        String desiredType = this.villagerType == null || this.villagerType.isBlank() ? "minecraft:plains" : this.villagerType;
        String desiredProfession = this.normalizeProfessionId(PROFESSIONS[this.professionIndex]);
        Object type = this.resolveRegistryEntry(class_7923.field_41194, desiredType, currentType, "minecraft:plains");
        Object profession = this.resolveRegistryEntry(class_7923.field_41195, desiredProfession, currentProfession, "minecraft:farmer");
        int level = Math.max(1, Math.min(5, this.villagerLevel));
        List<Object> typeCandidates = this.registryCandidates(class_7923.field_41194, type, currentType);
        List<Object> professionCandidates = this.registryCandidates(class_7923.field_41195, profession, currentProfession);
        Object updated = current;
        Object next = this.invokeCompatibleCandidates(updated, "withType", typeCandidates);
        if (next != null) {
            updated = next;
        }
        if ((next = this.invokeCompatibleCandidates(updated, "withProfession", professionCandidates)) != null) {
            updated = next;
        }
        if ((next = this.invokeCompatible(updated, "withLevel", level)) instanceof class_3850 && this.matchesVillagerData(data = (class_3850)next, desiredType, desiredProfession, level)) {
            return data;
        }
        if (updated instanceof class_3850 && this.matchesVillagerData(data = updated, desiredType, desiredProfession, level)) {
            return data;
        }
        try {
            for (Constructor<?> ctor : class_3850.class.getConstructors()) {
                Class<?>[] p = ctor.getParameterTypes();
                if (p.length != 3 || p[2] != Integer.TYPE) continue;
                for (Object typeCandidate : typeCandidates) {
                    if (typeCandidate == null || !p[0].isInstance(typeCandidate)) continue;
                    for (Object professionCandidate : professionCandidates) {
                        class_3850 data2;
                        Object out;
                        if (professionCandidate == null || !p[1].isInstance(professionCandidate) || !((out = ctor.newInstance(typeCandidate, professionCandidate, level)) instanceof class_3850) || !this.matchesVillagerData(data2 = (class_3850)out, desiredType, desiredProfession, level)) continue;
                        return data2;
                    }
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return current;
    }

    private boolean matchesVillagerData(class_3850 data, String expectedType, String expectedProfession, int expectedLevel) {
        if (data == null) {
            return false;
        }
        String actualType = this.extractNamespacedId(this.invokeAny((Object)data, "type", "getType"));
        String actualProfession = this.extractNamespacedId(this.invokeAny((Object)data, "profession", "getProfession"));
        Integer actualLevel = this.invokeInt(data, "getLevel");
        return Objects.equals(expectedType, actualType) && Objects.equals(expectedProfession, actualProfession) && actualLevel != null && actualLevel == expectedLevel;
    }

    private Object resolveRegistryEntry(Object registry, String id, Object fallback, String defaultId) {
        if (registry == null) {
            return fallback;
        }
        String rawId = id == null || id.isBlank() ? defaultId : id;
        class_2960 loc = class_2960.method_12829((String)rawId);
        if (loc == null) {
            return fallback;
        }
        Object value = null;
        try {
            Object holder = registry.getClass().getMethod("getHolder", class_2960.class).invoke(registry, loc);
            holder = this.unwrapOptional(holder);
            if (holder != null) {
                return holder;
            }
        }
        catch (Throwable holder) {
            // empty catch block
        }
        try {
            value = registry.getClass().getMethod("get", class_2960.class).invoke(registry, loc);
            value = this.unwrapOptional(value);
            if (this.isHolderLike(value)) {
                return value;
            }
        }
        catch (Throwable holder) {
            // empty catch block
        }
        if (value == null) {
            try {
                value = registry.getClass().getMethod("getValue", class_2960.class).invoke(registry, loc);
                value = this.unwrapOptional(value);
            }
            catch (Throwable holder) {
                // empty catch block
            }
        }
        if ((holder = this.wrapAsHolder(registry, value)) != null) {
            return holder;
        }
        return value != null ? value : fallback;
    }

    private List<Object> registryCandidates(Object registry, Object primary, Object fallback) {
        ArrayList<Object> candidates = new ArrayList<Object>();
        this.addCandidate(candidates, primary);
        this.addCandidate(candidates, this.unwrapHolderValue(primary));
        this.addCandidate(candidates, this.wrapAsHolder(registry, primary));
        this.addCandidate(candidates, fallback);
        this.addCandidate(candidates, this.unwrapHolderValue(fallback));
        this.addCandidate(candidates, this.wrapAsHolder(registry, fallback));
        return candidates;
    }

    private void addCandidate(List<Object> candidates, Object value) {
        if (value == null || candidates == null) {
            return;
        }
        for (Object candidate : candidates) {
            if (candidate != value && !Objects.equals(candidate, value)) continue;
            return;
        }
        candidates.add(value);
    }

    private Object unwrapHolderValue(Object value) {
        Object out2;
        if (value == null) {
            return null;
        }
        try {
            out2 = value.getClass().getMethod("value", new Class[0]).invoke(value, new Object[0]);
            out2 = this.unwrapOptional(out2);
            if (out2 != null) {
                return out2;
            }
        }
        catch (Throwable out2) {
            // empty catch block
        }
        try {
            out2 = value.getClass().getMethod("getValue", new Class[0]).invoke(value, new Object[0]);
            out2 = this.unwrapOptional(out2);
            if (out2 != null) {
                return out2;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private Object invokeCompatibleCandidates(Object target, String method, List<Object> candidates) {
        if (candidates == null) {
            return null;
        }
        for (Object candidate : candidates) {
            Object out = this.invokeCompatible(target, method, candidate);
            if (out == null) continue;
            return out;
        }
        return null;
    }

    private Object invokeAny(Object target, String ... methodNames) {
        if (target == null || methodNames == null) {
            return null;
        }
        for (String method : methodNames) {
            try {
                return target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
            }
            catch (Throwable throwable) {
            }
        }
        return null;
    }

    private Object invokeAny(Object target, String method, int arg) {
        try {
            return target.getClass().getMethod(method, Integer.TYPE).invoke(target, arg);
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private class_2487 invokeCompound(Object target, String method) {
        try {
            class_2487 ct;
            Object out = target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
            return out instanceof class_2487 ? (ct = (class_2487)out) : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private class_2487 invokeCompoundArg(Object target, String method, class_2487 arg) {
        try {
            class_2487 ct;
            Object out = target.getClass().getMethod(method, class_2487.class).invoke(target, arg);
            return out instanceof class_2487 ? (ct = (class_2487)out) : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private class_2499 invokeListTag(Object target, String ... methods) {
        if (target == null || methods == null) {
            return null;
        }
        for (String method : methods) {
            try {
                Optional opt;
                Object var10_10;
                Object out = target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
                if (out instanceof class_2499) {
                    class_2499 lt = (class_2499)out;
                    return lt;
                }
                if (!(out instanceof Optional) || !((var10_10 = (opt = (Optional)out).orElse(null)) instanceof class_2499)) continue;
                class_2499 lt = var10_10;
                return lt;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private class_2499 invokeListTagArg(Object target, String method, class_2499 arg) {
        if (target == null || method == null || method.isBlank()) {
            return null;
        }
        try {
            Optional opt;
            Object var7_8;
            Object out = target.getClass().getMethod(method, class_2499.class).invoke(target, arg);
            if (out instanceof class_2499) {
                class_2499 lt = (class_2499)out;
                return lt;
            }
            if (out instanceof Optional && (var7_8 = (opt = (Optional)out).orElse(null)) instanceof class_2499) {
                class_2499 lt = var7_8;
                return lt;
            }
            return arg;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private Integer invokeInt(Object target, String method) {
        try {
            Integer n;
            Object out = target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
            if (out instanceof Number) {
                Number n2 = (Number)out;
                n = n2.intValue();
            } else {
                n = null;
            }
            return n;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private Object invokeCompatible(Object target, String method, Object arg) {
        if (target == null || method == null || method.isBlank()) {
            return null;
        }
        for (Method candidate : target.getClass().getMethods()) {
            Class<?> parameter;
            if (!candidate.getName().equals(method) || candidate.getParameterCount() != 1 || !this.isCompatible(parameter = candidate.getParameterTypes()[0], arg)) continue;
            try {
                return candidate.invoke(target, this.coerceArgument(parameter, arg));
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private Object wrapAsHolder(Object registry, Object value) {
        if (registry == null || value == null) {
            return null;
        }
        for (Method candidate : registry.getClass().getMethods()) {
            Class<?> parameter;
            if (!"wrapAsHolder".equals(candidate.getName()) || candidate.getParameterCount() != 1 || !this.isCompatible(parameter = candidate.getParameterTypes()[0], value)) continue;
            try {
                Object out = candidate.invoke(registry, this.coerceArgument(parameter, value));
                out = this.unwrapOptional(out);
                if (out == null) continue;
                return out;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private boolean isHolderLike(Object value) {
        return value != null && value.getClass().getName().contains(".Holder");
    }

    private boolean isCompatible(Class<?> parameter, Object arg) {
        if (parameter == null) {
            return false;
        }
        if (arg == null) {
            return !parameter.isPrimitive();
        }
        if (parameter.isInstance(arg)) {
            return true;
        }
        if (!parameter.isPrimitive()) {
            return false;
        }
        return parameter == Integer.TYPE && arg instanceof Number || parameter == Boolean.TYPE && arg instanceof Boolean || parameter == Float.TYPE && arg instanceof Number || parameter == Double.TYPE && arg instanceof Number || parameter == Long.TYPE && arg instanceof Number || parameter == Short.TYPE && arg instanceof Number || parameter == Byte.TYPE && arg instanceof Number;
    }

    private Object coerceArgument(Class<?> parameter, Object arg) {
        Number n;
        Number number;
        if (!parameter.isPrimitive() || arg == null) {
            return arg;
        }
        Number number2 = number = arg instanceof Number ? (Number)(n = (Number)arg) : (Number)null;
        if (parameter == Integer.TYPE && number != null) {
            return number.intValue();
        }
        if (parameter == Float.TYPE && number != null) {
            return Float.valueOf(number.floatValue());
        }
        if (parameter == Double.TYPE && number != null) {
            return number.doubleValue();
        }
        if (parameter == Long.TYPE && number != null) {
            return number.longValue();
        }
        if (parameter == Short.TYPE && number != null) {
            return number.shortValue();
        }
        if (parameter == Byte.TYPE && number != null) {
            return number.byteValue();
        }
        if (parameter == Boolean.TYPE && arg instanceof Boolean) {
            Boolean bool = (Boolean)arg;
            return bool;
        }
        return arg;
    }

    private Float invokeFloat(Object target, String method) {
        try {
            Float f;
            Object out = target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
            if (out instanceof Number) {
                Number n = (Number)out;
                f = Float.valueOf(n.floatValue());
            } else {
                f = null;
            }
            return f;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private Boolean invokeBool(Object target, String method) {
        try {
            Boolean b;
            Object out = target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
            return out instanceof Boolean ? (b = (Boolean)out) : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private String onOff(boolean v) {
        return v ? this.tr("ankinbt.simple.on") : this.tr("ankinbt.simple.off");
    }

    private String tr(String key) {
        return class_2561.method_43471((String)key).getString();
    }

    private void border(class_332 g, int x, int y, int w, int h, int c) {
        g.method_25294(x, y, x + w, y + 1, c);
        g.method_25294(x, y + h - 1, x + w, y + h, c);
        g.method_25294(x, y, x + 1, y + h, c);
        g.method_25294(x + w - 1, y, x + w, y + h, c);
    }

    public void method_25419() {
        this.tryClose();
    }

    public boolean method_25421() {
        return false;
    }

    private static enum InvPickTarget {
        NONE,
        BUY,
        BUY2,
        SELL;

    }

    private static enum RightPage {
        TRADE,
        META;

    }

    private record StateSnapshot(List<TradeData> trades, int tradeIndex, int professionIndex, int villagerLevel, boolean rewardExp, String villagerType, boolean dirty) {
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
            int edge;
            int bg;
            boolean chosen;
            boolean hover = this.hover(mx, my);
            boolean bl = chosen = this.selected != null && Boolean.TRUE.equals(this.selected.get());
            if (!this.enabled) {
                bg = 705697831;
                edge = -13878436;
            } else if (this.style == 1) {
                bg = hover ? -1441508563 : -1978243788;
                edge = -14498466;
            } else if (this.style == -1) {
                bg = hover ? -1434510051 : -1969677541;
                edge = -1096636;
            } else {
                bg = chosen ? 0xAA000000 | accent & 0xFFFFFF : (hover ? 1780954962 : 1243293240);
                edge = chosen ? accent : -13878436;
            }
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
            int tx = this.w <= 24 ? this.x + (this.w - font.method_1727((String)text)) / 2 : this.x + 6;
            VersionCompat.get().drawString(g, font, (String)text, tx, this.y + 7, color, false);
        }
    }

    private static class TradeData {
        String buyId;
        int buyCount;
        class_2487 buyComponents;
        String buy2Id;
        int buy2Count;
        class_2487 buy2Components;
        String sellId;
        int sellCount;
        class_2487 sellComponents;
        int maxUses;
        int xp;

        private TradeData() {
        }

        static TradeData defaults() {
            TradeData t = new TradeData();
            t.buyId = "minecraft:emerald";
            t.buyCount = 1;
            t.buyComponents = VillagerTradeEditorScreen.demoComponents((class_2561)class_2561.method_43470((String)"AnkiNBT INT"), 3900150);
            t.buy2Id = "";
            t.buy2Count = 1;
            t.buy2Components = null;
            t.sellId = "minecraft:bread";
            t.sellCount = 6;
            t.sellComponents = VillagerTradeEditorScreen.demoComponents((class_2561)class_2561.method_43470((String)"AnkiNBT OUT"), 16436245);
            t.maxUses = 12;
            t.xp = 1;
            return t;
        }

        TradeData copy() {
            TradeData t = new TradeData();
            t.buyId = this.buyId;
            t.buyCount = this.buyCount;
            t.buyComponents = TradeData.copyTag(this.buyComponents);
            t.buy2Id = this.buy2Id;
            t.buy2Count = this.buy2Count;
            t.buy2Components = TradeData.copyTag(this.buy2Components);
            t.sellId = this.sellId;
            t.sellCount = this.sellCount;
            t.sellComponents = TradeData.copyTag(this.sellComponents);
            t.maxUses = this.maxUses;
            t.xp = this.xp;
            return t;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TradeData)) {
                return false;
            }
            TradeData other = (TradeData)obj;
            return this.buyCount == other.buyCount && this.buy2Count == other.buy2Count && this.sellCount == other.sellCount && this.maxUses == other.maxUses && this.xp == other.xp && Objects.equals(this.buyId, other.buyId) && Objects.equals(TradeData.compKey(this.buyComponents), TradeData.compKey(other.buyComponents)) && Objects.equals(this.buy2Id, other.buy2Id) && Objects.equals(TradeData.compKey(this.buy2Components), TradeData.compKey(other.buy2Components)) && Objects.equals(this.sellId, other.sellId) && Objects.equals(TradeData.compKey(this.sellComponents), TradeData.compKey(other.sellComponents));
        }

        public int hashCode() {
            return Objects.hash(this.buyId, this.buyCount, TradeData.compKey(this.buyComponents), this.buy2Id, this.buy2Count, TradeData.compKey(this.buy2Components), this.sellId, this.sellCount, TradeData.compKey(this.sellComponents), this.maxUses, this.xp);
        }

        private static String compKey(class_2487 tag) {
            return tag == null ? "" : tag.toString();
        }

        private static class_2487 copyTag(class_2487 tag) {
            if (tag == null) {
                return null;
            }
            class_2487 out = new class_2487();
            out.method_10543(tag);
            return out;
        }
    }

    private record LoadedVillagerDefaults(int professionIndex, int villagerLevel, String villagerType, boolean rewardExp, List<TradeData> trades) {
    }

    private record IconHit(int x, int y, int w, int h, InvPickTarget target) {
        boolean hit(int mx, int my) {
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }
    }

    private record InvSlotHit(int x, int y, int w, int h, String itemId, class_1799 stack) {
        boolean hit(int mx, int my) {
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }
    }
}

