package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.util.DebugLog;
import com.ankinbt.util.ItemRegistryHelper;
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
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.ItemLore;
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
    private float openAnim = 0f;
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
        recalcBounds();

        int inputX = tradeFieldInputX();
        int inputW = tradeFieldInputWidth();
        int row = tradeFieldRowGap();
        int fieldY = tradeFieldStartY();

        buyId = box(inputX, fieldY, inputW, "minecraft:emerald");
        buyCount = box(inputX, fieldY + row, inputW, "1");

        buy2Id = box(inputX, fieldY + row * 2, inputW, "");
        buy2Count = box(inputX, fieldY + row * 3, inputW, "1");

        sellId = box(inputX, fieldY + row * 4, inputW, "minecraft:bread");
        sellCount = box(inputX, fieldY + row * 5, inputW, "6");

        maxUses = box(inputX, fieldY + row * 6, inputW, "9999999");
        xp = box(inputX, fieldY + row * 7, inputW, "1");
        updateTradeFieldLayout();

        if (!initializedFromContext) {
            readContextDefaults();
            ensureTrades();
            initializedFromContext = true;
            dirty = false;
        } else {
            ensureTrades();
        }
        loadTradeToForm(tradeIndex);
        rebuildButtons();
        if (undoStack.isEmpty()) {
            undoStack.add(captureState());
        }
    }

    private void recalcBounds() {
        pw = Math.min(860, width - 20);
        ph = Math.min(480, height - 20);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
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
        int inputX = tradeFieldInputX();
        int inputW = tradeFieldInputWidth();
        int row = tradeFieldRowGap();
        int baseY = tradeFieldStartY();
        int contentBottom = baseY + row * (TRADE_FIELD_COUNT - 1) + TRADE_FIELD_BOX_HEIGHT;
        tradeScrollMax = Math.max(0, contentBottom - tradeFieldClipBottom());
        tradeScroll = Math.max(0, Math.min(tradeScroll, tradeScrollMax));

        int y = baseY - tradeScroll;
        setBoxBounds(buyId, inputX, y, inputW);
        setBoxBounds(buyCount, inputX, y + row, inputW);
        setBoxBounds(buy2Id, inputX, y + row * 2, inputW);
        setBoxBounds(buy2Count, inputX, y + row * 3, inputW);
        setBoxBounds(sellId, inputX, y + row * 4, inputW);
        setBoxBounds(sellCount, inputX, y + row * 5, inputW);
        setBoxBounds(maxUses, inputX, y + row * 6, inputW);
        setBoxBounds(xp, inputX, y + row * 7, inputW);
    }

    private void setBoxBounds(EditBox box, int x, int y, int w) {
        if (box == null) return;
        box.setX(x);
        box.setY(y);
        box.setWidth(w);
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
            if (!suppressDirtySync) dirty = true;
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

        int leftCard = px + 28;
        int mid = px + pw / 2;
        int leftCardRight = mid - 12;
        int rightCardLeft = mid + 10;
        int rightCardRight = px + pw - 22;

        int rowH = AnkiConfig.isUiCompactLayout() ? 20 : 22;
        int gap = AnkiConfig.isUiCompactLayout() ? 5 : 7;

        buttons.add(new UiBtn(leftCardRight - 38, py + 57, 16, 16,
                () -> "<", this::prevTrade, true, null));
        buttons.add(new UiBtn(leftCardRight - 20, py + 57, 16, 16,
                () -> ">", this::nextTrade, true, null));
        if (targetEntity != null) {
            int headerBtnW = 112;
            buttons.add(new UiBtn(px + pw - headerBtnW - 12, py + 8, headerBtnW, 18,
                    () -> tr("key.ankinbt.open_entity_editor"),
                    this::openEntityEditor, true, null));
        }

        int rowW = rightCardRight - rightCardLeft - 12;
        int labelW = 76;
        int actionW = Math.max(120, rowW - labelW);
        int halfW = (actionW - 6) / 2;
        int y = py + 82;
        buttons.add(new UiBtn(rightCardLeft + 8, y, (rowW - 6) / 2, rowH,
                () -> tr("ankinbt.villager.section.trade"),
                () -> rightPage = RightPage.TRADE, true, () -> rightPage == RightPage.TRADE));
        buttons.add(new UiBtn(rightCardLeft + 8 + (rowW - 6) / 2 + 6, y, (rowW - 6) / 2, rowH,
                () -> tr("ankinbt.villager.section.meta"),
                () -> rightPage = RightPage.META, true, () -> rightPage == RightPage.META));
        y += rowH + gap;

        rightLabelX = rightCardLeft + 6;
        rightActionLeft = rightCardLeft + labelW;
        rightTradeOpsY = y;
        rightBuyY = y;
        rightBuy2Y = y;
        rightSellY = y;

        if (rightPage == RightPage.TRADE) {
            rightTradeOpsY = y;
            buttons.add(new UiBtn(rightActionLeft, y, halfW, rowH,
                    () -> tr("ankinbt.villager.add"),
                    this::addTrade, true, null));
            buttons.add(new UiBtn(rightActionLeft + halfW + 6, y, halfW, rowH,
                    () -> tr("ankinbt.villager.remove"),
                    this::removeTrade, trades.size() > 1, null));
            y += rowH + gap;

            rightBuyY = y;
        buttons.add(new UiBtn(rightActionLeft, y, halfW, rowH,
                () -> tr("ankinbt.villager.edit"),
                () -> openPickerFor(InvPickTarget.BUY), true, null));
            buttons.add(new UiBtn(rightActionLeft + halfW + 6, y, halfW, rowH,
                    () -> tr("ankinbt.villager.pick.inv"),
                    () -> fillFromMainHand(buyId), true, null));
            y += rowH + gap;

            rightBuy2Y = y;
        buttons.add(new UiBtn(rightActionLeft, y, halfW, rowH,
                () -> tr("ankinbt.villager.edit"),
                () -> openPickerFor(InvPickTarget.BUY2), true, null));
            buttons.add(new UiBtn(rightActionLeft + halfW + 6, y, halfW, rowH,
                    () -> tr("ankinbt.villager.pick.inv"),
                    () -> fillFromMainHand(buy2Id), true, null));
            y += rowH + gap;

            rightSellY = y;
        buttons.add(new UiBtn(rightActionLeft, y, halfW, rowH,
                () -> tr("ankinbt.villager.edit"),
                () -> openPickerFor(InvPickTarget.SELL), true, null));
            buttons.add(new UiBtn(rightActionLeft + halfW + 6, y, halfW, rowH,
                    () -> tr("ankinbt.villager.pick.inv"),
                    () -> fillFromMainHand(sellId), true, null));
            y += rowH + gap;
        } else {
            buttons.add(new UiBtn(rightActionLeft, y, actionW, rowH,
                    () -> Component.translatable("ankinbt.villager.profession", professionLabel()).getString(),
                    this::cycleProfession, true, null));
            y += rowH + gap;

            buttons.add(new UiBtn(rightActionLeft, y, halfW, rowH,
                    () -> Component.translatable("ankinbt.villager.level", String.valueOf(villagerLevel)).getString(),
                    this::cycleLevel, true, null));
            buttons.add(new UiBtn(rightActionLeft + halfW + 6, y, halfW, rowH,
                    () -> Component.translatable("ankinbt.villager.reward_exp", rewardExp ? tr("ankinbt.simple.on") : tr("ankinbt.simple.off")).getString(),
                    () -> rewardExp = !rewardExp, true, null));
            y += rowH + gap;

            buttons.add(new UiBtn(rightActionLeft, y, actionW, rowH,
                    () -> Component.translatable("ankinbt.villager.require_prof", onOff(AnkiConfig.isVillagerRequireProfession())).getString(),
                    () -> AnkiConfig.setVillagerRequireProfession(!AnkiConfig.isVillagerRequireProfession()), true, null));
            y += rowH + gap;

            if (!sourceStack.isEmpty()) {
                buttons.add(new UiBtn(rightActionLeft, y, actionW, rowH,
                        () -> Component.translatable("ankinbt.villager.open_spawn_egg_nbt").getString(),
                        () -> Minecraft.getInstance().setScreen(new NbtEditorScreen(sourceStack)), true, null));
                y += rowH + gap;
            }
        }

        if (rightPage == RightPage.TRADE && !sourceStack.isEmpty()) {
            buttons.add(new UiBtn(rightActionLeft, y, actionW, rowH,
                    () -> Component.translatable("ankinbt.villager.open_spawn_egg_nbt").getString(),
                    () -> Minecraft.getInstance().setScreen(new NbtEditorScreen(sourceStack)), true, null));
            y += rowH + gap;
        }

        int bottomY = py + ph - 30;
        int areaW = pw - 36;
        int actionBarW = (areaW - 16) / 3;
        buttons.add(new UiBtn(px + 18, bottomY, actionBarW, 20,
                () -> Component.translatable("ankinbt.entity.apply_patch").getString(),
                this::applyTrade, true, null, 1));
        buttons.add(new UiBtn(px + 18 + actionBarW + 8, bottomY, actionBarW, 20,
                () -> Component.translatable("ankinbt.entity.reset_changes").getString(),
                () -> confirmReset = true, true, null, -1));
        buttons.add(new UiBtn(px + 18 + (actionBarW + 8) * 2, bottomY, actionBarW, 20,
                () -> Component.translatable("ankinbt.edit.cancel").getString(),
                this::tryClose, true, null));
    }

    private void openPickerFor(InvPickTarget target) {
        Minecraft.getInstance().setScreen(new ItemPickerScreen(this, id -> {
            pushUndo();
            EditBox box = boxForTarget(target);
            setBoxValue(box, id);
            syncCurrentTrade(false);
            dirty = true;
        }));
    }

    private void openInventoryPicker(InvPickTarget target) {
        invPickTarget = target == InvPickTarget.NONE ? InvPickTarget.BUY : target;
    }

    private void openEntityEditor() {
        if (targetEntity == null) return;
        Minecraft.getInstance().setScreen(EntityEditorScreen.forEntity(targetEntity, this));
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
        trades.clear();
        trades.add(TradeData.defaults());
        tradeIndex = 0;
        professionIndex = defaultProfessionIndex();
        villagerLevel = 1;
        rewardExp = true;
        villagerType = "minecraft:plains";
        loadTradeToForm(tradeIndex);
        dirty = false;
        undoStack.clear();
        undoStack.add(captureState());
        setStatus(Component.translatable("ankinbt.entity.reset_done"), TXT_OK);
        rebuildButtons();
    }

    private void readContextDefaults() {
        if (isWanderingTraderContext()) {
            professionIndex = 0;
            villagerLevel = 1;
            return;
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
        Boolean parsedRewardExp = null;
        for (int i = 0; i < recipes.size(); i++) {
            Object entry = unwrapOptional(recipes.get(i));
            if (!(entry instanceof CompoundTag recipe)) continue;
            TradeData t = tradeFromRecipe(recipe);
            trades.add(t);
            Object re = readTag(recipe, "rewardExp");
            if (re != null) {
                try {
                    Object b = re.getClass().getMethod("getAsBoolean").invoke(re);
                    if (b instanceof Boolean bb) parsedRewardExp = bb;
                } catch (Throwable ignored) {}
            }
        }
        if (parsedRewardExp != null) rewardExp = parsedRewardExp;
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
        return t;
    }

    private TradeData tradeFromRecipe(CompoundTag recipe) {
        TradeData t = TradeData.defaults();
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
        for (TradeData t : trades) {
            if (!isLikelyItemId(t.buyId) || !isLikelyItemId(t.sellId) || (!t.buy2Id.isBlank() && !isLikelyItemId(t.buy2Id))) {
                setStatus(Component.translatable("ankinbt.villager.invalid_item"), TXT_ERR);
                return;
            }
            CompoundTag buyTag = buildTradeStackTag(t.buyId, t.buyCount, t.buyComponents);
            CompoundTag sellTag = buildTradeStackTag(t.sellId, t.sellCount, t.sellComponents);

            CompoundTag recipe = new CompoundTag();
            recipe.put("buy", buyTag);
            recipe.put("base_cost_a", copyCompound(buyTag));
            if (!t.buy2Id.isEmpty()) {
                CompoundTag buyB = buildTradeStackTag(t.buy2Id, t.buy2Count, t.buy2Components);
                recipe.put("buyB", buyB);
                recipe.put("cost_b", copyCompound(buyB));
            }
            recipe.put("sell", sellTag);
            recipe.put("result", copyCompound(sellTag));
            recipe.putInt("maxUses", Math.max(1, t.maxUses));
            recipe.putInt("uses", 0);
            recipe.putInt("xp", Math.max(0, t.xp));
            recipe.putInt("specialPrice", 0);
            recipe.putInt("demand", 0);
            recipe.putFloat("priceMultiplier", 0.0f);
            recipe.putBoolean("rewardExp", rewardExp);
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
                dirty = false;
                undoStack.clear();
                undoStack.add(captureState());
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
                dirty = false;
                undoStack.clear();
                undoStack.add(captureState());
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
        dirty = false;
        undoStack.clear();
        undoStack.add(captureState());
    }

    private boolean isLikelyItemId(String id) {
        return !id.isBlank() && id.contains(":") && id.indexOf(':') > 0 && id.indexOf(':') < id.length() - 1;
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
        updateTradeFieldLayout();
        if (confirmClose || confirmReset) return clickConfirm((int) mx, (int) my);
        if (invPickTarget != InvPickTarget.NONE) return clickInventoryOverlay((int) mx, (int) my, button);
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
        return false;
    }

    public boolean keyPressed(int key, int scan, int mod) {
        if (handleEditBoxKey(key, scan, mod)) return true;
        boolean ctrl = (mod & 2) != 0;
        if (ctrl && key == 90) {
            undo();
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
        updateTradeFieldLayout();
        int left = px + 28;
        int right = px + pw / 2 - 12;
        int top = tradeFieldClipTop();
        int bottom = tradeFieldClipBottom();
        if (tradeScrollMax > 0 && mx >= left && mx < right && my >= top && my < bottom) {
            int step = (int) Math.signum(sy);
            if (step != 0) {
                int delta = Math.max(12, tradeFieldRowGap() / 2);
                tradeScroll = Math.max(0, Math.min(tradeScrollMax, tradeScroll - step * delta));
            }
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        recalcBounds();
        updateTradeFieldLayout();
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);

        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        float opacity = AnkiConfig.getUiOpacity();
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

        int left = px + 28;
        int mid = px + pw / 2;
        int leftRight = mid - 12;
        int rightLeft = mid + 10;
        int right = px + pw - 22;
        int fieldLeft = tradeFieldLeft();
        int inputX = tradeFieldInputX();
        int row = tradeFieldRowGap();
        int fieldY = tradeFieldStartY() - tradeScroll;
        int cardBottom = tradeCardBottomY();

        g.fill(left, py + 74, leftRight, cardBottom, card);
        g.fill(rightLeft, py + 74, right, cardBottom, card);
        border(g, left, py + 74, leftRight - left, cardBottom - (py + 74), border);
        border(g, rightLeft, py + 74, right - rightLeft, cardBottom - (py + 74), border);

        com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, px + 12, py + 12, TXT_TITLE, false);
        String target = targetEntity != null ? targetEntity.getDisplayName().getString() : sourceStack.getHoverName().getString();
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, target, px + 170, py + 13, TXT_DIM, false);

        renderTradeIcons(g, mx, my, fieldLeft + 10, py + 94, accent);

        String tradeLabel = (tradeIndex + 1) + " / " + Math.max(1, trades.size());
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.villager.section.trade") + " " + tradeLabel, left + 8, py + 62, accent, false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, rightPage == RightPage.TRADE ? tr("ankinbt.villager.section.trade") : tr("ankinbt.villager.section.meta"),
                rightLeft + 8, py + 62, accent, false);
        if (rightPage == RightPage.TRADE) {
            drawRightLabel(g, tr("ankinbt.villager.section.trade"), rightLabelX, rightTradeOpsY + 6, rightActionLeft - rightLabelX - 6);
            drawRightLabel(g, tr("ankinbt.villager.buy_item"), rightLabelX, rightBuyY + 6, rightActionLeft - rightLabelX - 6);
            drawRightLabel(g, tr("ankinbt.villager.buy2_item"), rightLabelX, rightBuy2Y + 6, rightActionLeft - rightLabelX - 6);
            drawRightLabel(g, tr("ankinbt.villager.sell_item"), rightLabelX, rightSellY + 6, rightActionLeft - rightLabelX - 6);
        }

        g.enableScissor(left + 2, tradeFieldClipTop(), leftRight - 8, tradeFieldClipBottom());
        renderTradeFieldLabel(g, Component.translatable("ankinbt.villager.buy_item"), fieldLeft, fieldY);
        renderTradeFieldLabel(g, Component.translatable("ankinbt.villager.buy_count"), fieldLeft, fieldY + row);
        renderTradeFieldLabel(g, Component.translatable("ankinbt.villager.buy2_item"), fieldLeft, fieldY + row * 2);
        renderTradeFieldLabel(g, Component.translatable("ankinbt.villager.buy2_count"), fieldLeft, fieldY + row * 3);
        renderTradeFieldLabel(g, Component.translatable("ankinbt.villager.sell_item"), fieldLeft, fieldY + row * 4);
        renderTradeFieldLabel(g, Component.translatable("ankinbt.villager.sell_count"), fieldLeft, fieldY + row * 5);
        renderTradeFieldLabel(g, Component.translatable("ankinbt.villager.max_uses"), fieldLeft, fieldY + row * 6);
        renderTradeFieldLabel(g, Component.translatable("ankinbt.villager.xp"), fieldLeft, fieldY + row * 7);

        renderInlineEditBox(g, buyId, mx, my, accent);
        renderInlineEditBox(g, buyCount, mx, my, accent);
        renderInlineEditBox(g, buy2Id, mx, my, accent);
        renderInlineEditBox(g, buy2Count, mx, my, accent);
        renderInlineEditBox(g, sellId, mx, my, accent);
        renderInlineEditBox(g, sellCount, mx, my, accent);
        renderInlineEditBox(g, maxUses, mx, my, accent);
        renderInlineEditBox(g, xp, mx, my, accent);
        g.disableScissor();
        renderTradeScrollBar(g, leftRight - 7, tradeFieldClipTop(), tradeFieldClipBottom() - tradeFieldClipTop(), accent);

        for (UiBtn btn : buttons) {
            btn.render(g, font, mx, my, accent);
        }

        renderInventoryOverlay(g, mx, my, accent);

        if (confirmReset) {
            renderConfirm(g, mx, my, true);
        } else if (confirmClose) {
            renderUnsavedConfirmLikeSimple(g, mx, my);
        }

        if (status != null && !status.getString().isEmpty() && System.currentTimeMillis() - statusTime < 2600) {
            int statusY = tradeStatusY();
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, status, left, statusY, statusColor, false);
        }
    }

    private void renderTradeFieldLabel(GuiGraphics g, Component label, int x, int y) {
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, x + 2, y + 5, TXT_DIM, false);
    }

    private void renderTradeScrollBar(GuiGraphics g, int x, int y, int h, int accent) {
        if (tradeScrollMax <= 0 || h <= 20) return;
        g.fill(x, y, x + 4, y + h, 0x35192738);
        int thumbH = Math.max(24, (int) Math.round((double) h * h / (h + tradeScrollMax)));
        int travel = Math.max(0, h - thumbH);
        int thumbY = y + (tradeScrollMax == 0 ? 0 : (int) Math.round((double) tradeScroll / tradeScrollMax * travel));
        g.fill(x, thumbY, x + 4, thumbY + thumbH, accent);
    }

    private String safeValue(String in, String def) {
        String t = in == null ? "" : in.trim();
        return t.isEmpty() ? def : t;
    }

    private void renderInlineEditBox(GuiGraphics g, EditBox box, int mx, int my, int accent) {
        if (box == null) return;
        boolean focused = box.isFocused();
        boolean hover = mx >= box.getX() && mx < box.getX() + box.getWidth() && my >= box.getY() && my < box.getY() + box.getHeight();
        String shown = box.getValue() == null ? "" : box.getValue();
        int textY = box.getY() + 2;
        int maxWidth = Math.max(12, box.getWidth() - 4);
        if (font.width(shown) > maxWidth) {
            shown = font.plainSubstrByWidth(shown, maxWidth);
        }
        int color = shown.isBlank() ? TXT_DIM : TXT_MAIN;
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, shown, box.getX() + 2, textY, color, false);
        int lineColor = focused ? accent : (hover ? 0xFF415A86 : 0xFF2C3B5C);
        g.fill(box.getX(), box.getY() + box.getHeight() - 1, box.getX() + box.getWidth(), box.getY() + box.getHeight(), lineColor);
        if (focused && ((System.currentTimeMillis() / 500L) & 1L) == 0L) {
            int cursorX = Math.min(box.getX() + 2 + font.width(shown), box.getX() + box.getWidth() - 1);
            g.fill(cursorX, box.getY() + 3, cursorX + 1, box.getY() + box.getHeight() - 2, TXT_MAIN);
        }
    }

    private boolean handleEditBoxClick(double mx, double my, int button) {
        boolean hit = false;
        for (EditBox box : allBoxes()) {
            if (box != null && isTradeBoxVisible(box) && clickEditBox(box, mx, my, button)) {
                focusBox(box);
                hit = true;
                break;
            }
        }
        if (!hit && button == 0) unfocusEditBoxes();
        return hit;
    }

    private boolean isTradeBoxVisible(EditBox box) {
        if (box == null) return false;
        return box.getY() + box.getHeight() > tradeFieldClipTop() && box.getY() < tradeFieldClipBottom();
    }

    private boolean handleEditBoxKey(int key, int scan, int mod) {
        for (EditBox box : allBoxes()) {
            if (box != null && box.isFocused() && pressEditBox(box, key, scan, mod)) return true;
        }
        return false;
    }

    private boolean handleEditBoxChar(char codePoint, int modifiers) {
        for (EditBox box : allBoxes()) {
            if (box != null && box.isFocused() && typeEditBox(box, codePoint, modifiers)) return true;
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
        return box != null && box.charTyped(new CharacterEvent(codePoint, modifiers));
    }    private List<EditBox> allBoxes() {
        return List.of(buyId, buyCount, buy2Id, buy2Count, sellId, sellCount, maxUses, xp);
    }

    private void focusBox(EditBox target) {
        for (EditBox box : allBoxes()) {
            if (box != null) box.setFocused(box == target);
        }
    }

    private void unfocusEditBoxes() {
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
        TradeData live = readTradeFromForm(trades.get(tradeIndex));
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
                VersionCompat.get().renderTooltip(g, font, Component.literal(hint + ": " + text), mx, my);
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
            VersionCompat.get().renderTooltip(g, font, Component.literal(hint + ": " + itemId), mx, my);
            return;
        }
        if (tryRenderVanillaTooltip(g, stack, mx, my)) return;
        Component fallback = Component.literal(stack.getHoverName().getString() + " (" + itemId + ")");
        VersionCompat.get().renderTooltip(g, font, fallback, mx, my);
    }

    private boolean tryRenderVanillaTooltip(GuiGraphics g, ItemStack stack, int mx, int my) {
        try {
            Method m = g.getClass().getMethod("renderTooltip", net.minecraft.client.gui.Font.class, ItemStack.class, int.class, int.class);
            m.invoke(g, font, stack, mx, my);
            return true;
        } catch (Throwable ignored) {}

        for (Method m : g.getClass().getMethods()) {
            if (!"renderTooltip".equals(m.getName())) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 4
                    && p[0].isAssignableFrom(font.getClass())
                    && ItemStack.class.isAssignableFrom(p[1])
                    && p[2] == int.class
                    && p[3] == int.class) {
                try {
                    m.invoke(g, font, stack, mx, my);
                    return true;
                } catch (Throwable ignored) {}
            }
        }
        return false;
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
        TradeData t = TradeData.defaults();
        if (prev != null) {
            t.buyComponents = copyCompound(prev.buyComponents);
            t.buy2Components = copyCompound(prev.buy2Components);
            t.sellComponents = copyCompound(prev.sellComponents);
        }
        t.buyId = buyId.getValue().trim().isEmpty() ? t.buyId : buyId.getValue().trim();
        t.buy2Id = buy2Id.getValue().trim();
        t.sellId = sellId.getValue().trim().isEmpty() ? t.sellId : sellId.getValue().trim();
        Integer buy = parseInt(buyCount.getValue(), t.buyCount);
        Integer buy2 = parseInt(buy2Count.getValue(), t.buy2Count);
        Integer sell = parseInt(sellCount.getValue(), t.sellCount);
        Integer uses = parseInt(maxUses.getValue(), t.maxUses);
        Integer xpVal = parseInt(xp.getValue(), t.xp);
        if (buy != null) t.buyCount = Math.max(1, buy);
        if (buy2 != null) t.buy2Count = Math.max(1, buy2);
        if (sell != null) t.sellCount = Math.max(1, sell);
        if (uses != null) t.maxUses = Math.max(1, uses);
        if (xpVal != null) t.xp = Math.max(0, xpVal);
        if (prev != null) {
            if (!Objects.equals(t.buyId, prev.buyId)) t.buyComponents = null;
            if (!Objects.equals(t.buy2Id, prev.buy2Id)) t.buy2Components = null;
            if (!Objects.equals(t.sellId, prev.sellId)) t.sellComponents = null;
        }
        return t;
    }

    private boolean syncCurrentTrade(boolean strict) {
        ensureTrades();
        TradeData prev = trades.get(tradeIndex);
        TradeData t = readTradeFromForm(prev);
        boolean buy2Valid = t.buy2Id.isEmpty() || isLikelyItemId(t.buy2Id);
        boolean valid = isLikelyItemId(t.buyId) && isLikelyItemId(t.sellId) && buy2Valid
                && parseInt(buyCount.getValue(), 1) != null
                && parseInt(buy2Count.getValue(), 1) != null
                && parseInt(sellCount.getValue(), 1) != null
                && parseInt(maxUses.getValue(), 12) != null
                && parseInt(xp.getValue(), 1) != null;
        if (strict && !valid) return false;
        trades.set(tradeIndex, t);
        return true;
    }

    private void loadTradeToForm(int idx) {
        ensureTrades();
        TradeData t = trades.get(idx);
        setBoxValue(buyId, t.buyId);
        setBoxValue(buyCount, String.valueOf(t.buyCount));
        setBoxValue(buy2Id, t.buy2Id);
        setBoxValue(buy2Count, String.valueOf(t.buy2Count));
        setBoxValue(sellId, t.sellId);
        setBoxValue(sellCount, String.valueOf(t.sellCount));
        setBoxValue(maxUses, String.valueOf(t.maxUses));
        setBoxValue(xp, String.valueOf(t.xp));
    }

    private void prevTrade() {
        ensureTrades();
        syncCurrentTrade(false);
        tradeIndex--;
        if (tradeIndex < 0) tradeIndex = trades.size() - 1;
        loadTradeToForm(tradeIndex);
    }

    private void nextTrade() {
        ensureTrades();
        syncCurrentTrade(false);
        tradeIndex++;
        if (tradeIndex >= trades.size()) tradeIndex = 0;
        loadTradeToForm(tradeIndex);
    }

    private void addTrade() {
        ensureTrades();
        pushUndo();
        syncCurrentTrade(false);
        trades.add(tradeIndex + 1, trades.get(tradeIndex).copy());
        tradeIndex++;
        loadTradeToForm(tradeIndex);
        dirty = true;
    }

    private void removeTrade() {
        ensureTrades();
        if (trades.size() <= 1) return;
        pushUndo();
        trades.remove(tradeIndex);
        if (tradeIndex >= trades.size()) tradeIndex = trades.size() - 1;
        loadTradeToForm(tradeIndex);
        dirty = true;
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
        if (undoStack.size() <= 1) return;
        undoStack.remove(undoStack.size() - 1);
        applyState(undoStack.get(undoStack.size() - 1));
        dirty = true;
        setStatus(Component.translatable("ankinbt.status.edited"), TXT_DIM);
    }

    private void tryClose() {
        syncCurrentTrade(false);
        if (dirty && AnkiConfig.isConfirmOnClose()) {
            confirmClose = true;
            return;
        }
        Minecraft.getInstance().setScreen(parent);
    }

    private void renderConfirm(GuiGraphics g, int mx, int my, boolean resetMode) {
        int w = 320, h = 118;
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        g.fill(0, 0, width, height, 0x88000000);
        g.fill(x, y, x + w, y + h, 0xF0080810);
        border(g, x, y, w, h, 0xFF2C3B5C);

        String title = resetMode ? tr("ankinbt.entity.reset_changes") : tr("ankinbt.confirm.title");
        int titleColor = resetMode ? TXT_ERR : TXT_TITLE;
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, x + 10, y + 10, titleColor, false);
        g.fill(x + 1, y + 24, x + w - 1, y + 25, 0xFF2C3B5C);
        if (resetMode) {
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.confirm.discard_hint"), x + 10, y + 33, TXT_MAIN, false);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.confirm.unsaved"), x + 10, y + 47, TXT_DIM, false);
        } else {
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.confirm.unsaved"), x + 10, y + 33, TXT_MAIN, false);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.confirm.discard_hint"), x + 10, y + 47, TXT_DIM, false);
        }

        int by = y + h - 32;
        int bw = 84;
        int bh = 22;
        if (resetMode) {
            int cancelX = x + 12;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
            g.fill(cancelX, by, cancelX + bw, by + bh, ch ? 0x6A273752 : 0x4A1B2638);
            border(g, cancelX, by, bw, bh, 0xFF2C3B5C);
            drawBtnText(g, tr("ankinbt.edit.cancel"), cancelX, by, bw);

            int applyX = x + w - bw - 12;
            boolean ah = mx >= applyX && mx < applyX + bw && my >= by && my < by + bh;
            g.fill(applyX, by, applyX + bw, by + bh, ah ? 0xAA7F1D1D : 0x8A991B1B);
            border(g, applyX, by, bw, bh, 0xFFEF4444);
            drawBtnText(g, tr("ankinbt.edit.apply"), applyX, by, bw);
            return;
        }
    }

    private void renderUnsavedConfirmLikeSimple(GuiGraphics g, int mx, int my) {
        int dw = 260, dh = 110;
        int dx = (width - dw) / 2, dy = (height - dh) / 2;
        g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
        border(g, dx, dy, dw, dh, 0xFFEF4444);

        com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.confirm.title"), dx + 10, dy + 10, SIMPLE_C1, false);
        g.fill(dx + 1, dy + 24, dx + dw - 1, dy + 25, SIMPLE_BORDER);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.confirm.unsaved"), dx + 10, dy + 32, SIMPLE_C2, false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, tr("ankinbt.confirm.discard_hint"), dx + 10, dy + 46, SIMPLE_C3, false);

        int by = dy + dh - 32;
        int bw2 = 70, bh2 = 22;

        int saveX = dx + 10;
        boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
        g.fill(saveX, by, saveX + bw2, by + bh2, sh ? 0xFF16A34A : SIMPLE_SUCCESS);
        String saveLabel = tr("ankinbt.confirm.save_close");
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, saveLabel, saveX + (bw2 - font.width(saveLabel)) / 2, by + 7, SIMPLE_C1, false);

        int discardX = dx + dw / 2 - bw2 / 2;
        boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
        g.fill(discardX, by, discardX + bw2, by + bh2, dh2 ? 0x80EF4444 : 0x40EF4444);
        String discardLabel = tr("ankinbt.confirm.discard");
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, discardLabel, discardX + (bw2 - font.width(discardLabel)) / 2, by + 7, SIMPLE_C1, false);

        int cancelX = dx + dw - bw2 - 10;
        boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
        g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? SIMPLE_BTN_HOVER : SIMPLE_BTN_BG);
        String cancelLabel = tr("ankinbt.edit.cancel");
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, cancelLabel, cancelX + (bw2 - font.width(cancelLabel)) / 2, by + 7, SIMPLE_C2, false);
    }

    private void drawBtnText(GuiGraphics g, String text, int x, int y, int w) {
        String out = text;
        if (font.width(out) > w - 8) out = font.plainSubstrByWidth(out, w - 12) + "..";
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, out, x + (w - font.width(out)) / 2, y + 7, TXT_MAIN, false);
    }

    private boolean clickConfirm(int mx, int my) {
        if (confirmClose) return clickUnsavedConfirmLikeSimple(mx, my);

        int w = 320, h = 118;
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        int by = y + h - 32;
        int bw = 84;
        int bh = 22;
        if (confirmReset) {
            int cancelX = x + 12;
            if (mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh) {
                confirmReset = false;
                return true;
            }
            int applyX = x + w - bw - 12;
            if (mx >= applyX && mx < applyX + bw && my >= by && my < by + bh) {
                confirmReset = false;
                resetForm();
                return true;
            }
            return true;
        }

        return true;
    }

    private boolean clickUnsavedConfirmLikeSimple(int mx, int my) {
        int dw = 260, dh = 110;
        int dx = (width - dw) / 2, dy = (height - dh) / 2;
        int by = dy + dh - 32;
        int bw2 = 70, bh2 = 22;

        int saveX = dx + 10;
        if (mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2) {
            applyTrade();
            if (!dirty) {
                confirmClose = false;
                Minecraft.getInstance().setScreen(parent);
            }
            return true;
        }
        int discardX = dx + dw / 2 - bw2 / 2;
        if (mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2) {
            confirmClose = false;
            dirty = false;
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        int cancelX = dx + dw - bw2 - 10;
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
        if (box == buyId) t.buyComponents = copyCompound(components);
        else if (box == buy2Id) t.buy2Components = copyCompound(components);
        else if (box == sellId) t.sellComponents = copyCompound(components);
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

    private static CompoundTag demoComponents(Component name, int color) {
        ItemStack preview = new ItemStack(Items.PAPER);
        Component title = name.copy().withStyle(Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(color)));
        Component lore = Component.literal("婵帗绋掗…鍫ヮ敇閼姐倗鐭嗛弶鐐村娴?Default example").withStyle(Style.EMPTY.withItalic(false));
        preview.set(DataComponents.CUSTOM_NAME, title);
        preview.set(DataComponents.LORE, new ItemLore(List.of(lore)));
        try {
            Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(preview);
            if (fullOpt.isEmpty()) return null;
            CompoundTag full = fullOpt.get();
            CompoundTag components = readStaticCompound(full, "components");
            if (components != null && !components.isEmpty()) return components;
        } catch (Throwable ignored) {}
        return null;
    }

    private static CompoundTag readStaticCompound(CompoundTag parent, String key) {
        if (parent == null || key == null || key.isBlank()) return null;
        try {
            Object out = parent.getClass().getMethod("getCompound", String.class).invoke(parent, key);
            if (out instanceof CompoundTag ct) return ct;
            if (out instanceof Optional<?> opt && opt.orElse(null) instanceof CompoundTag ct) return ct;
        } catch (Throwable ignored) {}
        try {
            Object raw = parent.getClass().getMethod("get", String.class).invoke(parent, key);
            if (raw instanceof Optional<?> opt) raw = opt.orElse(null);
            if (raw instanceof CompoundTag ct) return ct;
        } catch (Throwable ignored) {}
        return null;
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

                CompoundTag mergedTag = readEntityTag(serverEntity);
                boolean loadedFromTag = false;
                if (mergedTag != null && patch != null && !patch.isEmpty()) {
                    mergedTag.merge(copyCompound(patch));
                    if (loadEntityTag(serverEntity, mergedTag)) {
                        loadedFromTag = true;
                    }
                }

                MerchantOffers offers = buildMerchantOffers();
                MerchantOffers live = serverVillager.getOffers();
                live.clear();
                for (MerchantOffer offer : offers) {
                    live.add(offer.copy());
                }

                if (serverEntity instanceof Villager villager && !isWanderingTraderContext()) {
                    ServerLevel serverLevel = serverEntity.level() instanceof ServerLevel level ? level : null;
                    applyVillagerData(villager, serverLevel);
                }
                success.set(loadedFromTag || !offers.isEmpty() || serverEntity instanceof Villager || serverVillager != null);
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
            if (buy.isEmpty() || sell.isEmpty()) continue;

            ItemCost firstCost = toItemCost(buy);
            if (firstCost == null) continue;

            Optional<ItemCost> secondCost = Optional.empty();
            if (!t.buy2Id.isBlank()) {
                ItemStack buy2 = buildPreviewStack(t.buy2Id, t.buy2Components, Math.max(1, t.buy2Count));
                ItemCost extraCost = toItemCost(buy2);
                if (extraCost == null) continue;
                secondCost = Optional.of(extraCost);
            }

            offers.add(new MerchantOffer(
                    firstCost,
                    secondCost,
                    sell.copy(),
                    0,
                    Math.max(1, t.maxUses),
                    Math.max(0, t.xp),
                    0.0f,
                    0
            ));
        }
        return offers;
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
        if (next != null) updated = next;
        next = invokeCompatibleCandidates(updated, "withProfession", professionCandidates);
        if (next != null) updated = next;
        next = invokeCompatible(updated, "withLevel", Integer.valueOf(level));
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
        String actualType = extractNamespacedId(invokeAny(data, "type", "getType"));
        String actualProfession = extractNamespacedId(invokeAny(data, "profession", "getProfession"));
        Integer actualLevel = invokeInt(data, "getLevel");
        return Objects.equals(expectedType, actualType)
                && Objects.equals(expectedProfession, actualProfession)
                && actualLevel != null
                && actualLevel == expectedLevel;
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

        static TradeData defaults() {
            TradeData t = new TradeData();
            t.buyId = "minecraft:emerald";
            t.buyCount = 1;
            t.buyComponents = demoComponents(Component.literal("AnkiNBT INT"), 0x3B82F6);
            t.buy2Id = "";
            t.buy2Count = 1;
            t.buy2Components = null;
            t.sellId = "minecraft:bread";
            t.sellCount = 6;
            t.sellComponents = demoComponents(Component.literal("AnkiNBT OUT"), 0xFACC15);
            t.maxUses = 12;
            t.xp = 1;
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
                    && Objects.equals(buyId, other.buyId)
                    && Objects.equals(compKey(buyComponents), compKey(other.buyComponents))
                    && Objects.equals(buy2Id, other.buy2Id)
                    && Objects.equals(compKey(buy2Components), compKey(other.buy2Components))
                    && Objects.equals(sellId, other.sellId)
                    && Objects.equals(compKey(sellComponents), compKey(other.sellComponents));
        }

        @Override
        public int hashCode() {
            return Objects.hash(buyId, buyCount, compKey(buyComponents), buy2Id, buy2Count, compKey(buy2Components),
                    sellId, sellCount, compKey(sellComponents), maxUses, xp);
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

        @Override
    public boolean keyPressed(KeyEvent event) {
        if (keyPressed(event.key(), event.scancode(), event.modifiers())) return true;
        return super.keyPressed(event);
    }
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (charTyped((char) event.codepoint(), event.modifiers())) return true;
        return super.charTyped(event);
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
    }    private record StateSnapshot(
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

            int bg;
            int edge;
            if (!enabled) {
                bg = 0x2A101827;
                edge = 0xFF2C3B5C;
            } else if (style == 1) {
                bg = hover ? 0xAA14532D : 0x8A166534;
                edge = 0xFF22C55E;
            } else if (style == -1) {
                bg = hover ? 0xAA7F1D1D : 0x8A991B1B;
                edge = 0xFFEF4444;
            } else {
                bg = chosen ? (0xAA000000 | (accent & 0x00FFFFFF)) : hover ? 0x6A273752 : 0x4A1B2638;
                edge = chosen ? accent : 0xFF2C3B5C;
            }
            int color = enabled ? TXT_MAIN : TXT_DIM;

            g.fill(x, y, x + w, y + h, bg);
            g.fill(x, y, x + w, y + 1, edge);
            g.fill(x, y + h - 1, x + w, y + h, edge);
            g.fill(x, y, x + 1, y + h, edge);
            g.fill(x + w - 1, y, x + w, y + h, edge);

            String text = label.get();
            if (font.width(text) > w - 10) text = font.plainSubstrByWidth(text, w - 14) + "..";
            int tx = w <= 24 ? x + (w - font.width(text)) / 2 : x + 6;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, tx, y + 7, color, false);
        }
    }
}
