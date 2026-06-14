/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.server.IntegratedServer
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.npc.AbstractVillager
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.trading.MerchantOffer
 *  net.minecraft.world.item.trading.MerchantOffers
 *  net.minecraft.world.level.ItemLike
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.gui.ItemPickerScreen;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.util.DebugLog;
import com.ankinbt.util.ItemRegistryHelper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;

public class VillagerTradeEditorScreen
extends Screen {
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");
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
    private final Entity targetEntity;
    private final ItemStack sourceStack;
    private final int inventorySlot;
    private final Screen parent;
    private final List<UiBtn> buttons = new ArrayList<UiBtn>();
    private EditBox buyId;
    private EditBox buyCount;
    private EditBox buy2Id;
    private EditBox buy2Count;
    private EditBox sellId;
    private EditBox sellCount;
    private EditBox maxUses;
    private EditBox xp;
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
    private static final Map<UUID, CompoundTag> ENTITY_PATCH_CACHE = new HashMap<UUID, CompoundTag>();
    private final List<IconHit> iconHits = new ArrayList<IconHit>();
    private final List<InvSlotHit> invSlotHits = new ArrayList<InvSlotHit>();
    private final Map<String, Item> itemCache = new HashMap<String, Item>();
    private InvPickTarget invPickTarget = InvPickTarget.NONE;
    private Component status = Component.empty();
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
    private long tradeAddedFlashUntil = 0L;

    private VillagerTradeEditorScreen(Entity targetEntity, ItemStack sourceStack, int inventorySlot, Screen parent) {
        super((Component)Component.translatable((String)"ankinbt.villager.title"));
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

    protected void init() {
        this.recalcBounds();
        int left = this.px + 38;
        int right = this.px + this.pw / 2 - 22;
        int fieldW = right - left;
        int halfW = (fieldW - 6) / 2;
        int row = AnkiConfig.isUiCompactLayout() ? 26 : 30;
        int fieldY = this.py + 136;
        this.buyId = this.box(left, fieldY, halfW, "minecraft:emerald");
        this.buyCount = this.box(left + halfW + 6, fieldY, halfW, "1");
        this.buy2Id = this.box(left, fieldY + row, halfW, "");
        this.buy2Count = this.box(left + halfW + 6, fieldY + row, halfW, "1");
        this.sellId = this.box(left, fieldY + row * 2, halfW, "minecraft:bread");
        this.sellCount = this.box(left + halfW + 6, fieldY + row * 2, halfW, "6");
        this.maxUses = this.box(left, fieldY + row * 3, halfW, "9999999");
        this.xp = this.box(left + halfW + 6, fieldY + row * 3, halfW, "1");
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
        this.pw = Math.min(860, this.width - 20);
        this.ph = Math.min(480, this.height - 20);
        this.px = (this.width - this.pw) / 2;
        this.py = (this.height - this.ph) / 2;
    }

    private EditBox box(int x, int y, int w, String value) {
        EditBox b = new EditBox(this.font, x, y, w, 20, (Component)Component.empty());
        b.setValue(value);
        b.setResponder(v -> {
            if (!this.suppressDirtySync) {
                this.dirty = true;
            }
        });
        try {
            b.setBordered(false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            b.setTextColor(-2497806);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.addRenderableWidget(b);
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
            this.buttons.add(new UiBtn(this.rightActionLeft, y, actionW, rowH, () -> Component.translatable((String)"ankinbt.villager.profession", (Object[])new Object[]{this.professionLabel()}).getString(), this::cycleProfession, true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft, y += rowH + gap, halfW, rowH, () -> Component.translatable((String)"ankinbt.villager.level", (Object[])new Object[]{String.valueOf(this.villagerLevel)}).getString(), this::cycleLevel, true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft + halfW + 6, y, halfW, rowH, () -> Component.translatable((String)"ankinbt.villager.reward_exp", (Object[])new Object[]{this.rewardExp ? this.tr("ankinbt.simple.on") : this.tr("ankinbt.simple.off")}).getString(), () -> {
                this.rewardExp = !this.rewardExp;
            }, true, null));
            this.buttons.add(new UiBtn(this.rightActionLeft, y += rowH + gap, actionW, rowH, () -> Component.translatable((String)"ankinbt.villager.require_prof", (Object[])new Object[]{this.onOff(AnkiConfig.isVillagerRequireProfession())}).getString(), () -> AnkiConfig.setVillagerRequireProfession(!AnkiConfig.isVillagerRequireProfession()), true, null));
            y += rowH + gap;
            if (!this.sourceStack.isEmpty()) {
                this.buttons.add(new UiBtn(this.rightActionLeft, y, actionW, rowH, () -> Component.translatable((String)"ankinbt.villager.open_spawn_egg_nbt").getString(), () -> Minecraft.getInstance().setScreen((Screen)new NbtEditorScreen(this.sourceStack)), true, null));
                y += rowH + gap;
            }
        }
        if (this.rightPage == RightPage.TRADE && !this.sourceStack.isEmpty()) {
            this.buttons.add(new UiBtn(this.rightActionLeft, y, actionW, rowH, () -> Component.translatable((String)"ankinbt.villager.open_spawn_egg_nbt").getString(), () -> Minecraft.getInstance().setScreen((Screen)new NbtEditorScreen(this.sourceStack)), true, null));
            y += rowH + gap;
        }
        int bottomY = this.py + this.ph - 30;
        int areaW = this.pw - 36;
        int actionBarW = (areaW - 16) / 3;
        this.buttons.add(new UiBtn(this.px + 18, bottomY, actionBarW, 20, () -> Component.translatable((String)"ankinbt.entity.apply_patch").getString(), this::applyTrade, true, null, 1));
        this.buttons.add(new UiBtn(this.px + 18 + actionBarW + 8, bottomY, actionBarW, 20, () -> Component.translatable((String)"ankinbt.entity.reset_changes").getString(), () -> {
            this.confirmReset = true;
        }, true, null, -1));
        this.buttons.add(new UiBtn(this.px + 18 + (actionBarW + 8) * 2, bottomY, actionBarW, 20, () -> Component.translatable((String)"ankinbt.edit.cancel").getString(), this::tryClose, true, null));
    }

    private void openPickerFor(InvPickTarget target) {
        Minecraft.getInstance().setScreen((Screen)new ItemPickerScreen(this, id -> {
            this.pushUndo();
            EditBox box = this.boxForTarget(target);
            this.setBoxValue(box, (String)id);
            this.syncCurrentTrade(false);
            this.setPickedComponents(box, null);
            this.dirty = true;
        }));
    }

    private void openInventoryPicker(InvPickTarget target) {
        this.invPickTarget = target == InvPickTarget.NONE ? InvPickTarget.BUY : target;
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
        if (this.sellId != null && this.sellId.isFocused()) {
            return InvPickTarget.SELL;
        }
        if (this.buy2Id != null && this.buy2Id.isFocused()) {
            return InvPickTarget.BUY2;
        }
        return InvPickTarget.BUY;
    }

    private void resetForm() {
        this.setBoxValue(this.buyId, "minecraft:emerald");
        this.setBoxValue(this.buyCount, "1");
        this.setBoxValue(this.buy2Id, "");
        this.setBoxValue(this.buy2Count, "1");
        this.setBoxValue(this.sellId, "minecraft:bread");
        this.setBoxValue(this.sellCount, "6");
        this.setBoxValue(this.maxUses, "12");
        this.setBoxValue(this.xp, "1");
        this.trades.clear();
        this.trades.add(this.readTradeFromForm(null));
        this.tradeIndex = 0;
        this.professionIndex = 1;
        this.villagerLevel = 1;
        this.rewardExp = true;
        this.villagerType = "minecraft:plains";
        this.dirty = false;
        this.undoStack.clear();
        this.undoStack.add(this.captureState());
        this.setStatus((Component)Component.translatable((String)"ankinbt.entity.reset_done"), -13315175);
        this.rebuildButtons();
    }

    private void readContextDefaults() {
        Object raw;
        CompoundTag vd;
        if (this.isWanderingTraderContext()) {
            this.professionIndex = 0;
            this.villagerLevel = 1;
            return;
        }
        CompoundTag root = null;
        if (this.targetEntity != null) {
            root = this.readEntityTag(this.targetEntity);
        } else if (!this.sourceStack.isEmpty()) {
            root = SpawnEggEditorHelper.getEntityData(this.sourceStack).orElse(null);
        }
        if (root == null) {
            this.professionIndex = 1;
            this.villagerLevel = 1;
            this.villagerType = "minecraft:plains";
            this.trades.clear();
            this.trades.add(TradeData.defaults());
            return;
        }
        if (this.targetEntity != null) {
            CompoundTag cached = ENTITY_PATCH_CACHE.get(this.targetEntity.getUUID());
            if (cached != null && !cached.isEmpty()) {
                root.merge(this.copyCompound(cached));
            }
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
        if (AnkiConfig.isVillagerRequireProfession() && !this.isTradeableProfession(PROFESSIONS[this.professionIndex])) {
            this.professionIndex = 1;
        }
        this.trades.clear();
        CompoundTag offers = this.readCompound(root, "Offers");
        if (offers == null) {
            offers = this.readCompound(root, "offers");
        }
        Object object = raw = offers == null ? null : this.readTag(offers, "Recipes");
        if (!(raw instanceof ListTag) && offers != null) {
            raw = this.readTag(offers, "recipes");
        }
        if (!(raw instanceof ListTag) && offers != null) {
            raw = this.readTag(offers, "Trades");
        }
        if (!(raw instanceof ListTag) && offers != null) {
            raw = this.readTag(offers, "trades");
        }
        if (raw instanceof ListTag) {
            ListTag recipes = (ListTag)raw;
            DebugLog.info("Villager offer recipes detected: {}", recipes.size());
            for (int i = 0; i < recipes.size(); ++i) {
                Tag entry = recipes.get(i);
                if (!(entry instanceof CompoundTag)) continue;
                CompoundTag recipe = (CompoundTag)entry;
                TradeData t = TradeData.defaults();
                CompoundTag buy = this.readRecipeItem(recipe, "buy", "base_cost_a", "itemA", "input", "costA");
                CompoundTag buyB = this.readRecipeItem(recipe, "buyB", "cost_b", "itemB", "inputB", "costB");
                CompoundTag sell = this.readRecipeItem(recipe, "sell", "result", "output", "itemOut");
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
                this.trades.add(t);
                Object re = this.readTag(recipe, "rewardExp");
                if (re == null) continue;
                try {
                    Object b = re.getClass().getMethod("getAsBoolean", new Class[0]).invoke(re, new Object[0]);
                    if (!(b instanceof Boolean)) continue;
                    Boolean bb = (Boolean)b;
                    this.rewardExp = bb;
                    continue;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        if (!(raw instanceof ListTag)) {
            DebugLog.warn("Villager offers missing or incompatible on target: {}", this.targetEntity == null ? "spawn_egg" : this.targetEntity.getUUID());
        }
        if (this.trades.isEmpty()) {
            this.trades.add(TradeData.defaults());
        }
    }

    private CompoundTag readEntityTag(Entity entity) {
        CompoundTag ct;
        Optional opt;
        Object var5_8;
        Object out2;
        if (entity == null) {
            return null;
        }
        try {
            out2 = entity.getClass().getMethod("saveWithoutId", CompoundTag.class).invoke(entity, new CompoundTag());
            if (out2 instanceof CompoundTag) {
                CompoundTag ct2 = (CompoundTag)out2;
                return ct2;
            }
            if (out2 instanceof Optional && (var5_8 = (opt = (Optional)out2).orElse(null)) instanceof CompoundTag) {
                CompoundTag ct3 = (CompoundTag)var5_8;
                return ct3;
            }
        }
        catch (Throwable t) {
            // empty catch block
        }
        try {
            out2 = entity.getClass().getMethod("saveAsPassenger", CompoundTag.class).invoke(entity, new CompoundTag());
            if (out2 instanceof CompoundTag) {
                ct = (CompoundTag)out2;
                return ct;
            }
            if (out2 instanceof Optional && (var5_8 = (opt = (Optional)out2).orElse(null)) instanceof CompoundTag) {
                CompoundTag ct4 = (CompoundTag)var5_8;
                return ct4;
            }
        }
        catch (Throwable out3) {
            // empty catch block
        }
        try {
            out2 = entity.getClass().getMethod("save", CompoundTag.class).invoke(entity, new CompoundTag());
            if (out2 instanceof CompoundTag) {
                ct = (CompoundTag)out2;
                return ct;
            }
            if (out2 instanceof Optional && (var5_8 = (opt = (Optional)out2).orElse(null)) instanceof CompoundTag) {
                CompoundTag ct5 = (CompoundTag)var5_8;
                return ct5;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private CompoundTag readCompound(CompoundTag parent, String key) {
        CompoundTag ct;
        if (parent == null) {
            return null;
        }
        try {
            Optional opt;
            Object var6_7;
            Object out = parent.getClass().getMethod("getCompound", String.class).invoke(parent, key);
            if (out instanceof CompoundTag) {
                CompoundTag ct2 = (CompoundTag)out;
                return ct2;
            }
            if (out instanceof Optional && (var6_7 = (opt = (Optional)out).orElse(null)) instanceof CompoundTag) {
                CompoundTag ct3 = (CompoundTag)var6_7;
                return ct3;
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        Object raw = this.readTag(parent, key);
        return raw instanceof CompoundTag ? (ct = (CompoundTag)raw) : null;
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

    private int readInt(CompoundTag parent, String key, int def) {
        if (parent == null) {
            return def;
        }
        try {
            Optional opt;
            Object var7_9;
            Object out = parent.getClass().getMethod("getInt", String.class).invoke(parent, key);
            if (out instanceof Integer) {
                Integer i = (Integer)out;
                return i;
            }
            if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof Integer) {
                Integer i = (Integer)var7_9;
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

    private Object readTag(CompoundTag parent, String key) {
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

    private Object unwrapOptional(Object value) {
        if (value instanceof Optional) {
            Optional opt = (Optional)value;
            return opt.orElse(null);
        }
        return value;
    }

    private CompoundTag readRecipeItem(CompoundTag recipe, String ... keys) {
        if (recipe == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            CompoundTag item = this.readCompound(recipe, key);
            if (item == null || item.isEmpty()) continue;
            return item;
        }
        return null;
    }

    private CompoundTag readStackComponents(CompoundTag stackTag) {
        CompoundTag components = this.readCompound(stackTag, "components");
        if (components != null && !components.isEmpty()) {
            return this.copyCompound(components);
        }
        CompoundTag legacyTag = this.readCompound(stackTag, "tag");
        if (legacyTag != null && !legacyTag.isEmpty()) {
            CompoundTag wrapped = new CompoundTag();
            wrapped.put("minecraft:custom_data", (Tag)this.copyCompound(legacyTag));
            return wrapped;
        }
        return null;
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
        ++this.professionIndex;
        if (this.professionIndex >= PROFESSIONS.length) {
            this.professionIndex = 0;
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
        String id = PROFESSIONS[this.professionIndex];
        if (id.isBlank()) {
            return Component.translatable((String)"ankinbt.villager.profession.none").getString();
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
            String type = this.targetEntity.getType().toString().toLowerCase(Locale.ROOT);
            return type.contains("wandering_trader");
        }
        if (!this.sourceStack.isEmpty()) {
            String id = SpawnEggEditorHelper.getItemId(this.sourceStack).toLowerCase(Locale.ROOT);
            return id.contains("wandering_trader_spawn_egg");
        }
        return false;
    }

    private void applyTrade() {
        if (!this.syncCurrentTrade(true)) {
            this.setStatus((Component)Component.translatable((String)"ankinbt.simple.invalid_number"), -1096636);
            return;
        }
        this.ensureTrades();
        boolean wandering = this.isWanderingTraderContext();
        String profession = PROFESSIONS[this.professionIndex];
        if (!wandering && AnkiConfig.isVillagerRequireProfession() && !this.isTradeableProfession(profession)) {
            this.setStatus((Component)Component.translatable((String)"ankinbt.villager.profession_required"), -1096636);
            return;
        }
        if (!wandering && !this.isTradeableProfession(profession)) {
            profession = "minecraft:farmer";
        }
        ListTag recipes = new ListTag();
        for (TradeData t : this.trades) {
            if (!this.isLikelyItemId(t.buyId) || !this.isLikelyItemId(t.sellId) || !t.buy2Id.isBlank() && !this.isLikelyItemId(t.buy2Id)) {
                this.setStatus((Component)Component.translatable((String)"ankinbt.villager.invalid_item"), -1096636);
                return;
            }
            CompoundTag buyTag = new CompoundTag();
            buyTag.putString("id", t.buyId);
            buyTag.putInt("count", Math.max(1, t.buyCount));
            if (t.buyComponents != null && !t.buyComponents.isEmpty()) {
                buyTag.put("components", (Tag)this.copyCompound(t.buyComponents));
            }
            CompoundTag sellTag = new CompoundTag();
            sellTag.putString("id", t.sellId);
            sellTag.putInt("count", Math.max(1, t.sellCount));
            if (t.sellComponents != null && !t.sellComponents.isEmpty()) {
                sellTag.put("components", (Tag)this.copyCompound(t.sellComponents));
            }
            CompoundTag recipe = new CompoundTag();
            recipe.put("buy", (Tag)buyTag);
            recipe.put("base_cost_a", (Tag)this.copyCompound(buyTag));
            if (!t.buy2Id.isEmpty()) {
                CompoundTag buyB = new CompoundTag();
                buyB.putString("id", t.buy2Id);
                buyB.putInt("count", Math.max(1, t.buy2Count));
                if (t.buy2Components != null && !t.buy2Components.isEmpty()) {
                    buyB.put("components", (Tag)this.copyCompound(t.buy2Components));
                }
                recipe.put("buyB", (Tag)buyB);
                recipe.put("cost_b", (Tag)this.copyCompound(buyB));
            }
            recipe.put("sell", (Tag)sellTag);
            recipe.put("result", (Tag)this.copyCompound(sellTag));
            recipe.putInt("maxUses", Math.max(1, t.maxUses));
            recipe.putInt("uses", 0);
            recipe.putInt("xp", Math.max(0, t.xp));
            recipe.putInt("specialPrice", 0);
            recipe.putInt("demand", 0);
            recipe.putFloat("priceMultiplier", 0.0f);
            recipe.putBoolean("rewardExp", this.rewardExp);
            recipes.add(recipe);
        }
        CompoundTag offers = new CompoundTag();
        offers.put("Recipes", (Tag)recipes);
        offers.put("recipes", (Tag)this.copyListTag(recipes));
        CompoundTag patch = new CompoundTag();
        patch.put("Offers", (Tag)offers);
        if (!wandering) {
            CompoundTag villagerData = new CompoundTag();
            villagerData.putString("type", this.villagerType == null || this.villagerType.isBlank() ? "minecraft:plains" : this.villagerType);
            villagerData.putString("profession", profession);
            villagerData.putInt("level", Math.max(1, Math.min(5, this.villagerLevel)));
            patch.put("VillagerData", (Tag)villagerData);
            patch.putInt("Xp", Math.max(0, this.villagerLevel * 10));
        }
        Minecraft mc = Minecraft.getInstance();
        if (this.targetEntity != null) {
            if (mc.player == null) {
                return;
            }
            if (!EditorCommandHelper.canUseEntityCommand(mc)) {
                this.setStatus((Component)Component.translatable((String)"ankinbt.entity.admin_required"), -1096636);
                return;
            }
            boolean ok = EditorCommandHelper.applyMergeToEntity(mc, this.targetEntity, patch);
            this.setStatus((Component)(ok ? Component.translatable((String)"ankinbt.entity.applied") : Component.translatable((String)"ankinbt.status.save_error")), ok ? -13315175 : -1096636);
            if (ok) {
                ENTITY_PATCH_CACHE.put(this.targetEntity.getUUID(), this.copyCompound(patch));
                this.dirty = false;
                this.undoStack.clear();
                this.undoStack.add(this.captureState());
            }
            return;
        }
        if (!SpawnEggEditorHelper.isVillagerSpawnEgg(this.sourceStack)) {
            this.setStatus((Component)Component.translatable((String)"ankinbt.villager.spawn_egg_required"), -1096636);
            return;
        }
        patch.putString("id", wandering ? "minecraft:wandering_trader" : "minecraft:villager");
        Optional<ItemStack> patched = SpawnEggEditorHelper.withMergedEntityData(this.sourceStack, patch);
        if (patched.isEmpty()) {
            this.setStatus((Component)Component.translatable((String)"ankinbt.status.save_error"), -1096636);
            return;
        }
        if (!SpawnEggEditorHelper.saveToCreativeSlot(mc, patched.get(), this.inventorySlot)) {
            this.setStatus((Component)Component.translatable((String)"ankinbt.status.creative_only"), -1096636);
            return;
        }
        this.setStatus((Component)Component.translatable((String)"ankinbt.entity.applied"), -13315175);
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

    private void setStatus(Component msg, int color) {
        this.status = msg;
        this.statusColor = color;
        this.statusTime = System.currentTimeMillis();
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (this.confirmClose || this.confirmReset) {
            return this.clickConfirm((int)mx, (int)my);
        }
        if (this.invPickTarget != InvPickTarget.NONE) {
            return this.clickInventoryOverlay((int)mx, (int)my, button);
        }
        if (button == 0 || button == 1) {
            for (IconHit hit : this.iconHits) {
                if (!hit.hit((int)mx, (int)my)) continue;
                EditBox box = this.boxForTarget(hit.target);
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
        return false;
    }

    public boolean keyPressed(int key, int scan, int mod) {
        boolean ctrl;
        boolean bl = ctrl = (mod & 2) != 0;
        if (ctrl && key == 90) {
            this.undo();
            return true;
        }
        if (key == 256) {
            this.tryClose();
            return true;
        }
        return false;
    }

    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        String preview;
        this.recalcBounds();
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        float opacity = AnkiConfig.getUiOpacity();
        int scrim = UiTheme.scrim(opacity, this.openAnim);
        int panel = UiTheme.panel(opacity, this.openAnim);
        int card = UiTheme.card(opacity, this.openAnim);
        int border = UiTheme.border(opacity, this.openAnim);
        int shadow = UiTheme.shadow(opacity, this.openAnim, AnkiConfig.isUiShadowEnabled());
        g.fill(0, 0, this.width, this.height, scrim);
        if (shadow != 0) {
            g.fill(this.px + 4, this.py + 4, this.px + this.pw + 4, this.py + this.ph + 4, shadow);
        }
        g.fill(this.px, this.py, this.px + this.pw, this.py + this.ph, panel);
        this.border(g, this.px, this.py, this.pw, this.ph, border);
        g.fill(this.px + 1, this.py + 1, this.px + this.pw - 1, this.py + 34, UiTheme.header(opacity, this.openAnim));
        g.fill(this.px + 1, this.py + 34, this.px + this.pw - 1, this.py + 35, border);
        int left = this.px + 28;
        int mid = this.px + this.pw / 2;
        int leftRight = mid - 12;
        int rightLeft = mid + 10;
        int right = this.px + this.pw - 22;
        int fieldW = leftRight - left;
        int halfW = (fieldW - 6) / 2;
        int col2 = left + halfW + 6;
        int row = AnkiConfig.isUiCompactLayout() ? 26 : 30;
        int fieldY = this.py + 136;
        g.fill(left, this.py + 74, leftRight, this.py + this.ph - 44, card);
        g.fill(rightLeft, this.py + 74, right, this.py + this.ph - 44, card);
        this.border(g, left, this.py + 74, leftRight - left, this.ph - 118, border);
        this.border(g, rightLeft, this.py + 74, right - rightLeft, this.ph - 118, border);
        g.drawString(this.font, this.title, this.px + 12, this.py + 12, -788737, false);
        String target = this.targetEntity != null ? this.targetEntity.getDisplayName().getString() : this.sourceStack.getHoverName().getString();
        g.drawString(this.font, target, this.px + 170, this.py + 13, -7429177, false);
        this.renderTradeIcons(g, mx, my, left + 12, this.py + 97, accent);
        int labelY = fieldY - 16;
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.villager.buy_item"), left + 6, labelY, -7429177, false);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.villager.buy_count"), col2 + 6, labelY, -7429177, false);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.villager.buy2_item"), left + 6, labelY + row, -7429177, false);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.villager.buy2_count"), col2 + 6, labelY + row, -7429177, false);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.villager.sell_item"), left + 6, labelY + row * 2, -7429177, false);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.villager.sell_count"), col2 + 6, labelY + row * 2, -7429177, false);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.villager.max_uses"), left + 6, labelY + row * 3, -7429177, false);
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.villager.xp"), col2 + 6, labelY + row * 3, -7429177, false);
        String tradeLabel = this.tradeIndex + 1 + " / " + Math.max(1, this.trades.size());
        if (System.currentTimeMillis() < this.tradeAddedFlashUntil) {
            g.fill(left + 5, this.py + 59, left + 153, this.py + 73, 1429525401);
        }
        g.drawString(this.font, this.tr("ankinbt.villager.section.trade") + " " + tradeLabel, left + 8, this.py + 62, accent, false);
        g.drawString(this.font, this.rightPage == RightPage.TRADE ? this.tr("ankinbt.villager.section.trade") : this.tr("ankinbt.villager.section.meta"), rightLeft + 8, this.py + 62, accent, false);
        if (this.rightPage == RightPage.TRADE) {
            this.drawRightLabel(g, this.tr("ankinbt.villager.section.trade"), this.rightLabelX, this.rightTradeOpsY + 6, this.rightActionLeft - this.rightLabelX - 6);
            this.drawRightLabel(g, this.tr("ankinbt.villager.buy_item"), this.rightLabelX, this.rightBuyY + 6, this.rightActionLeft - this.rightLabelX - 6);
            this.drawRightLabel(g, this.tr("ankinbt.villager.buy2_item"), this.rightLabelX, this.rightBuy2Y + 6, this.rightActionLeft - this.rightLabelX - 6);
            this.drawRightLabel(g, this.tr("ankinbt.villager.sell_item"), this.rightLabelX, this.rightSellY + 6, this.rightActionLeft - this.rightLabelX - 6);
        }
        if (this.buyId != null) {
            this.buyId.render(g, mx, my, partialTick);
        }
        if (this.buyCount != null) {
            this.buyCount.render(g, mx, my, partialTick);
        }
        if (this.buy2Id != null) {
            this.buy2Id.render(g, mx, my, partialTick);
        }
        if (this.buy2Count != null) {
            this.buy2Count.render(g, mx, my, partialTick);
        }
        if (this.sellId != null) {
            this.sellId.render(g, mx, my, partialTick);
        }
        if (this.sellCount != null) {
            this.sellCount.render(g, mx, my, partialTick);
        }
        if (this.maxUses != null) {
            this.maxUses.render(g, mx, my, partialTick);
        }
        if (this.xp != null) {
            this.xp.render(g, mx, my, partialTick);
        }
        String buyPart = this.buyId.getValue().trim() + " x" + this.safeValue(this.buyCount.getValue(), "1");
        if (!this.buy2Id.getValue().trim().isEmpty()) {
            buyPart = buyPart + " + " + this.buy2Id.getValue().trim() + " x" + this.safeValue(this.buy2Count.getValue(), "1");
        }
        if ((preview = buyPart + " -> " + this.sellId.getValue().trim() + " x" + this.safeValue(this.sellCount.getValue(), "1")).length() > 78) {
            preview = preview.substring(0, 75) + "...";
        }
        g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.entity.section.preview"), this.px + 28 + 8, this.py + this.ph - 94, accent, false);
        g.drawString(this.font, preview, this.px + 28 + 8, this.py + this.ph - 78, -2497806, false);
        if (!this.isWanderingTraderContext() && AnkiConfig.isVillagerRequireProfession() && !this.isTradeableProfession(PROFESSIONS[this.professionIndex])) {
            g.drawString(this.font, (Component)Component.translatable((String)"ankinbt.villager.profession_required"), this.px + 28 + 8, this.py + this.ph - 62, -1096636, false);
        }
        for (UiBtn btn : this.buttons) {
            btn.render(g, this.font, mx, my, accent);
        }
        this.renderInventoryOverlay(g, mx, my, accent);
        if (this.confirmReset) {
            this.renderConfirm(g, mx, my, true);
        } else if (this.confirmClose) {
            this.renderUnsavedConfirmLikeSimple(g, mx, my);
        }
        if (this.status != null && !this.status.getString().isEmpty() && System.currentTimeMillis() - this.statusTime < 2600L) {
            g.drawString(this.font, this.status, left, this.py + this.ph - 12, this.statusColor, false);
        }
    }

    private String safeValue(String in, String def) {
        String t = in == null ? "" : in.trim();
        return t.isEmpty() ? def : t;
    }

    private EditBox boxForTarget(InvPickTarget target) {
        return switch (target.ordinal()) {
            case 2 -> this.buy2Id;
            case 3 -> this.sellId;
            default -> this.buyId;
        };
    }

    private void renderTradeIcons(GuiGraphics g, int mx, int my, int x, int y, int accent) {
        this.iconHits.clear();
        this.ensureTrades();
        TradeData live = this.readTradeFromForm(this.trades.get(this.tradeIndex));
        this.renderIconSlot(g, mx, my, x, y, this.buyId == null ? "" : this.buyId.getValue(), live.buyComponents, live.buyCount, InvPickTarget.BUY, this.tr("ankinbt.villager.buy_item"), accent);
        g.drawString(this.font, "+", x + 42, y + 5, -7429177, false);
        this.renderIconSlot(g, mx, my, x + 52, y, this.buy2Id == null ? "" : this.buy2Id.getValue(), live.buy2Components, live.buy2Count, InvPickTarget.BUY2, this.tr("ankinbt.villager.buy2_item"), accent);
        g.drawString(this.font, "->", x + 92, y + 5, -7429177, false);
        this.renderIconSlot(g, mx, my, x + 112, y, this.sellId == null ? "" : this.sellId.getValue(), live.sellComponents, live.sellCount, InvPickTarget.SELL, this.tr("ankinbt.villager.sell_item"), accent);
    }

    private void renderIconSlot(GuiGraphics g, int mx, int my, int x, int y, String itemId, CompoundTag components, int count, InvPickTarget target, String hint, int accent) {
        int w = 18;
        int h = 18;
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        int bg = hover ? -1977141422 : 1713055288;
        int edge = hover ? accent : -13878436;
        g.fill(x, y, x + w, y + h, bg);
        this.border(g, x, y, w, h, edge);
        ItemStack preview = this.buildPreviewStack(itemId, components, count);
        if (!preview.isEmpty()) {
            g.renderItem(preview, x + 1, y + 1);
        }
        this.iconHits.add(new IconHit(x, y, w, h, target));
        if (hover) {
            Object text;
            Object object = text = itemId == null || itemId.isBlank() ? "<" + this.tr("ankinbt.villager.profession.none") + ">" : itemId;
            if (!preview.isEmpty()) {
                this.renderStackTooltip(g, preview, mx, my, hint, (String)text);
            } else {
                VersionCompat.get().renderTooltip(g, this.font, (Component)Component.literal((String)(hint + ": " + (String)text)), mx, my);
            }
        }
    }

    private ItemStack buildPreviewStack(String itemId, CompoundTag components, int count) {
        Item item = this.resolveItem(itemId);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        int n = Math.max(1, Math.min(64, count));
        if (components == null || components.isEmpty()) {
            return new ItemStack((ItemLike)item, n);
        }
        try {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", itemId);
            tag.putInt("count", n);
            tag.put("components", (Tag)this.copyCompound(components));
            Optional<ItemStack> out = NbtHelper.deserializeItemStack(tag);
            if (out.isPresent() && !out.get().isEmpty()) {
                return out.get();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return new ItemStack((ItemLike)item, n);
    }

    private void renderStackTooltip(GuiGraphics g, ItemStack stack, int mx, int my, String hint, String itemId) {
        if (stack == null || stack.isEmpty()) {
            VersionCompat.get().renderTooltip(g, this.font, (Component)Component.literal((String)(hint + ": " + itemId)), mx, my);
            return;
        }
        if (this.tryRenderVanillaTooltip(g, stack, mx, my)) {
            return;
        }
        MutableComponent fallback = Component.literal((String)(stack.getHoverName().getString() + " (" + itemId + ")"));
        VersionCompat.get().renderTooltip(g, this.font, (Component)fallback, mx, my);
    }

    private boolean tryRenderVanillaTooltip(GuiGraphics g, ItemStack stack, int mx, int my) {
        try {
            Method m = g.getClass().getMethod("renderTooltip", Font.class, ItemStack.class, Integer.TYPE, Integer.TYPE);
            m.invoke(g, this.font, stack, mx, my);
            return true;
        }
        catch (Throwable throwable) {
            for (Method m : g.getClass().getMethods()) {
                Class<?>[] p;
                if (!"renderTooltip".equals(m.getName()) || (p = m.getParameterTypes()).length != 4 || !p[0].isAssignableFrom(this.font.getClass()) || !ItemStack.class.isAssignableFrom(p[1]) || p[2] != Integer.TYPE || p[3] != Integer.TYPE) continue;
                try {
                    m.invoke(g, this.font, stack, mx, my);
                    return true;
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
            }
            return false;
        }
    }

    private Item resolveItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        if (this.itemCache.containsKey(itemId)) {
            return this.itemCache.get(itemId);
        }
        Item found = ItemRegistryHelper.resolveItem(itemId);
        this.itemCache.put(itemId, found);
        return found;
    }

    private void renderInventoryOverlay(GuiGraphics g, int mx, int my, int accent) {
        this.invSlotHits.clear();
        if (this.invPickTarget == InvPickTarget.NONE) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        int cols = 9;
        int rows = 4;
        int cell = 20;
        int w = cols * cell + 20;
        int h = rows * cell + 44;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        g.fill(0, 0, this.width, this.height, -1728053248);
        g.fill(x, y, x + w, y + h, -267315418);
        this.border(g, x, y, w, h, -13878436);
        g.drawString(this.font, this.tr("ankinbt.villager.pick.inv") + " - " + this.focusedTargetText(), x + 10, y + 10, accent, false);
        int startX = x + 10;
        int startY = y + 24;
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                int logical = r < 3 ? 9 + r * 9 + c : c;
                ItemStack stack = mc.player.getInventory().getItem(logical);
                int sx = startX + c * cell;
                int sy = startY + r * cell;
                g.fill(sx, sy, sx + 18, sy + 18, 1243293240);
                this.border(g, sx, sy, 18, 18, -13878436);
                if (stack == null || stack.isEmpty()) continue;
                g.renderItem(stack, sx + 1, sy + 1);
                String id = SpawnEggEditorHelper.getItemId(stack);
                this.invSlotHits.add(new InvSlotHit(sx, sy, 18, 18, id));
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
            EditBox box = this.boxForTarget(this.invPickTarget);
            if (box != null && this.isLikelyItemId(hit.itemId)) {
                this.pushUndo();
                box.setValue(hit.itemId);
                this.dirty = true;
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
        t.buyId = this.buyId.getValue().trim().isEmpty() ? t.buyId : this.buyId.getValue().trim();
        t.buy2Id = this.buy2Id.getValue().trim();
        t.sellId = this.sellId.getValue().trim().isEmpty() ? t.sellId : this.sellId.getValue().trim();
        Integer buy = this.parseInt(this.buyCount.getValue(), t.buyCount);
        Integer buy2 = this.parseInt(this.buy2Count.getValue(), t.buy2Count);
        Integer sell = this.parseInt(this.sellCount.getValue(), t.sellCount);
        Integer uses = this.parseInt(this.maxUses.getValue(), t.maxUses);
        Integer xpVal = this.parseInt(this.xp.getValue(), t.xp);
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
        boolean bl = valid = this.isLikelyItemId(t.buyId) && this.isLikelyItemId(t.sellId) && buy2Valid && this.parseInt(this.buyCount.getValue(), 1) != null && this.parseInt(this.buy2Count.getValue(), 1) != null && this.parseInt(this.sellCount.getValue(), 1) != null && this.parseInt(this.maxUses.getValue(), 12) != null && this.parseInt(this.xp.getValue(), 1) != null;
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
        this.tradeAddedFlashUntil = System.currentTimeMillis() + 900L;
        this.setStatus((Component)Component.translatable((String)"ankinbt.villager.trade_added", (Object[])new Object[]{this.tradeIndex + 1, this.trades.size()}), -13315175);
        this.playUiClickFeedback(1.2f);
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
        this.setStatus((Component)Component.translatable((String)"ankinbt.villager.trade_removed", (Object[])new Object[]{this.trades.size()}), -7429177);
        this.playUiClickFeedback(0.9f);
    }

    private void fillFromMainHand(EditBox box) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        int slot = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
        ItemStack stack = mc.player.getInventory().getItem(slot);
        if (stack == null || stack.isEmpty()) {
            stack = mc.player.getMainHandItem();
        }
        if (stack == null || stack.isEmpty()) {
            return;
        }
        String id = SpawnEggEditorHelper.getItemId(stack);
        if (!this.isLikelyItemId(id)) {
            return;
        }
        this.pushUndo();
        this.setBoxValue(box, id);
        this.syncCurrentTrade(false);
        this.setPickedComponents(box, this.readItemComponents(stack));
        this.dirty = true;
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
        this.setStatus((Component)Component.translatable((String)"ankinbt.status.edited"), -7429177);
    }

    private void tryClose() {
        this.syncCurrentTrade(false);
        if (this.dirty && AnkiConfig.isConfirmOnClose()) {
            this.confirmClose = true;
            return;
        }
        Minecraft.getInstance().setScreen(this.parent);
    }

    private void renderConfirm(GuiGraphics g, int mx, int my, boolean resetMode) {
        int w = 320;
        int h = 118;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        g.fill(0, 0, this.width, this.height, -2013265920);
        g.fill(x, y, x + w, y + h, -267909104);
        this.border(g, x, y, w, h, -13878436);
        String title = resetMode ? this.tr("ankinbt.entity.reset_changes") : this.tr("ankinbt.confirm.title");
        int titleColor = resetMode ? -1096636 : -788737;
        g.drawString(this.font, title, x + 10, y + 10, titleColor, false);
        g.fill(x + 1, y + 24, x + w - 1, y + 25, -13878436);
        if (resetMode) {
            g.drawString(this.font, this.tr("ankinbt.confirm.discard_hint"), x + 10, y + 33, -2497806, false);
            g.drawString(this.font, this.tr("ankinbt.confirm.unsaved"), x + 10, y + 47, -7429177, false);
        } else {
            g.drawString(this.font, this.tr("ankinbt.confirm.unsaved"), x + 10, y + 33, -2497806, false);
            g.drawString(this.font, this.tr("ankinbt.confirm.discard_hint"), x + 10, y + 47, -7429177, false);
        }
        int by = y + h - 32;
        int bw = 84;
        int bh = 22;
        if (resetMode) {
            int cancelX = x + 12;
            boolean ch = mx >= cancelX && mx < cancelX + bw && my >= by && my < by + bh;
            g.fill(cancelX, by, cancelX + bw, by + bh, ch ? 1780954962 : 1243293240);
            this.border(g, cancelX, by, bw, bh, -13878436);
            this.drawBtnText(g, this.tr("ankinbt.edit.cancel"), cancelX, by, bw);
            int applyX = x + w - bw - 12;
            boolean ah = mx >= applyX && mx < applyX + bw && my >= by && my < by + bh;
            g.fill(applyX, by, applyX + bw, by + bh, ah ? -1434510051 : -1969677541);
            this.border(g, applyX, by, bw, bh, -1096636);
            this.drawBtnText(g, this.tr("ankinbt.edit.apply"), applyX, by, bw);
            return;
        }
    }

    private void renderUnsavedConfirmLikeSimple(GuiGraphics g, int mx, int my) {
        int dw = 260;
        int dh = 110;
        int dx = (this.width - dw) / 2;
        int dy = (this.height - dh) / 2;
        g.fill(dx, dy, dx + dw, dy + dh, -267909104);
        this.border(g, dx, dy, dw, dh, -1096636);
        g.drawString(this.font, this.tr("ankinbt.confirm.title"), dx + 10, dy + 10, -1906448, false);
        g.fill(dx + 1, dy + 24, dx + dw - 1, dy + 25, -14540234);
        g.drawString(this.font, this.tr("ankinbt.confirm.unsaved"), dx + 10, dy + 32, -7035976, false);
        g.drawString(this.font, this.tr("ankinbt.confirm.discard_hint"), dx + 10, dy + 46, -10193781, false);
        int by = dy + dh - 32;
        int bw2 = 70;
        int bh2 = 22;
        int saveX = dx + 10;
        boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
        g.fill(saveX, by, saveX + bw2, by + bh2, sh ? -15293622 : -14498466);
        String saveLabel = this.tr("ankinbt.confirm.save_close");
        g.drawString(this.font, saveLabel, saveX + (bw2 - this.font.width(saveLabel)) / 2, by + 7, -1906448, false);
        int discardX = dx + dw / 2 - bw2 / 2;
        boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
        g.fill(discardX, by, discardX + bw2, by + bh2, dh2 ? -2131803068 : 1089422404);
        String discardLabel = this.tr("ankinbt.confirm.discard");
        g.drawString(this.font, discardLabel, discardX + (bw2 - this.font.width(discardLabel)) / 2, by + 7, -1906448, false);
        int cancelX = dx + dw - bw2 - 10;
        boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
        g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
        String cancelLabel = this.tr("ankinbt.edit.cancel");
        g.drawString(this.font, cancelLabel, cancelX + (bw2 - this.font.width(cancelLabel)) / 2, by + 7, -7035976, false);
    }

    private void drawBtnText(GuiGraphics g, String text, int x, int y, int w) {
        Object out = text;
        if (this.font.width((String)out) > w - 8) {
            out = this.font.plainSubstrByWidth((String)out, w - 12) + "..";
        }
        g.drawString(this.font, (String)out, x + (w - this.font.width((String)out)) / 2, y + 7, -2497806, false);
    }

    private boolean clickConfirm(int mx, int my) {
        if (this.confirmClose) {
            return this.clickUnsavedConfirmLikeSimple(mx, my);
        }
        int w = 320;
        int h = 118;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
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
        int dx = (this.width - dw) / 2;
        int dy = (this.height - dh) / 2;
        int by = dy + dh - 32;
        int bw2 = 70;
        int bh2 = 22;
        int saveX = dx + 10;
        if (mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2) {
            this.applyTrade();
            if (!this.dirty) {
                this.confirmClose = false;
                Minecraft.getInstance().setScreen(this.parent);
            }
            return true;
        }
        int discardX = dx + dw / 2 - bw2 / 2;
        if (mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2) {
            this.confirmClose = false;
            this.dirty = false;
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }
        int cancelX = dx + dw - bw2 - 10;
        if (mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2) {
            this.confirmClose = false;
            return true;
        }
        return true;
    }

    private void drawRightLabel(GuiGraphics g, String text, int x, int y, int maxW) {
        Object out;
        if (maxW <= 8) {
            return;
        }
        Object object = out = text == null ? "" : text;
        if (this.font.width((String)out) > maxW) {
            out = this.font.plainSubstrByWidth((String)out, maxW - 4) + "..";
        }
        g.drawString(this.font, (String)out, x, y, -7429177, false);
    }

    private void setBoxValue(EditBox box, String value) {
        if (box == null) {
            return;
        }
        boolean old = this.suppressDirtySync;
        this.suppressDirtySync = true;
        box.setValue(value == null ? "" : value);
        this.suppressDirtySync = old;
    }

    private void setPickedComponents(EditBox box, CompoundTag components) {
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

    private CompoundTag readItemComponents(ItemStack stack) {
        try {
            Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(stack);
            if (fullOpt.isEmpty()) {
                return null;
            }
            CompoundTag full = fullOpt.get();
            CompoundTag components = this.readCompound(full, "components");
            if (components != null && !components.isEmpty()) {
                return this.copyCompound(components);
            }
            CompoundTag legacy = this.readCompound(full, "tag");
            if (legacy != null && !legacy.isEmpty()) {
                CompoundTag wrapped = new CompoundTag();
                wrapped.put("minecraft:custom_data", (Tag)this.copyCompound(legacy));
                return wrapped;
            }
            return null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private CompoundTag copyCompound(CompoundTag source) {
        if (source == null) {
            return null;
        }
        CompoundTag out = new CompoundTag();
        out.merge(source);
        return out;
    }

    private ListTag copyListTag(ListTag source) {
        ListTag out = new ListTag();
        if (source == null) {
            return out;
        }
        for (int i = 0; i < source.size(); ++i) {
            Tag entry = source.get(i);
            if (entry instanceof CompoundTag) {
                CompoundTag ct = (CompoundTag)entry;
                out.add(this.copyCompound(ct));
                continue;
            }
            if (!(entry instanceof Tag)) continue;
            Tag tag = entry;
            out.add(tag.copy());
        }
        return out;
    }

    private void injectRuntimeOffersIfMissing(CompoundTag root, Entity entity) {
        if (root == null || entity == null) {
            return;
        }
        CompoundTag offers = this.readCompound(root, "Offers");
        if (offers == null) {
            offers = this.readCompound(root, "offers");
        }
        if (this.hasRecipeList(offers, "Recipes") || this.hasRecipeList(offers, "recipes")) {
            return;
        }
        ListTag runtime = this.readRuntimeOffers(entity);
        if (runtime == null || runtime.isEmpty()) {
            return;
        }
        CompoundTag outOffers = offers == null ? new CompoundTag() : this.copyCompound(offers);
        outOffers.put("Recipes", (Tag)runtime);
        outOffers.put("recipes", (Tag)this.copyListTag(runtime));
        root.put("Offers", (Tag)outOffers);
        DebugLog.info("Injected runtime villager offers: {} entries", runtime.size());
    }

    private boolean hasRecipeList(CompoundTag offers, String key) {
        ListTag list;
        if (offers == null || key == null || key.isBlank()) {
            return false;
        }
        Object raw = this.unwrapOptional(this.readTag(offers, key));
        return raw instanceof ListTag && !(list = (ListTag)raw).isEmpty();
    }

    private ListTag readRuntimeOffers(Entity entity) {
        ListTag reflective;
        ListTag serverMirror = this.readRuntimeOffersFromIntegratedServer(entity);
        if (serverMirror != null && !serverMirror.isEmpty()) {
            return serverMirror;
        }
        if (entity instanceof AbstractVillager) {
            AbstractVillager villager = (AbstractVillager)entity;
            try {
                MerchantOffers offers = villager.getOffers();
                if (offers != null && !offers.isEmpty()) {
                    return this.merchantOffersToList(offers);
                }
            }
            catch (IllegalStateException ex) {
                DebugLog.warn("Client-side getOffers blocked: {}", ex.getMessage());
            }
            catch (Throwable t) {
                DebugLog.warn("Client-side getOffers failed: {}", t.toString());
            }
        }
        if ((reflective = this.readOffersFromEntityObject(entity)) != null && !reflective.isEmpty()) {
            return reflective;
        }
        return null;
    }

    private ListTag readRuntimeOffersFromIntegratedServer(Entity clientEntity) {
        if (clientEntity == null) {
            return null;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || !mc.hasSingleplayerServer()) {
                return null;
            }
            IntegratedServer server = mc.getSingleplayerServer();
            if (server == null) {
                return null;
            }
            int targetId = clientEntity.getId();
            UUID targetUuid = clientEntity.getUUID();
            for (ServerLevel level : server.getAllLevels()) {
                CompoundTag serverTag;
                ListTag offers;
                Entity serverEntity = level.getEntity(targetId);
                if (serverEntity == null || !targetUuid.equals(serverEntity.getUUID()) || (offers = this.extractOfferRecipes(serverTag = this.readEntityTag(serverEntity))) == null || offers.isEmpty()) continue;
                DebugLog.info("Loaded villager offers from integrated server mirror: {}", offers.size());
                return offers;
            }
        }
        catch (Throwable t) {
            DebugLog.warn("Integrated server villager offer mirror read failed: {}", t.toString());
        }
        return null;
    }

    private ListTag readOffersFromEntityObject(Object entityLike) {
        MerchantOffers merchantOffers;
        if (entityLike == null) {
            return null;
        }
        Object offersObj = this.invokeAny(entityLike, "getOffers", "getRecipes", "getTrades");
        if (offersObj == null) {
            return null;
        }
        if (offersObj instanceof MerchantOffers && !(merchantOffers = (MerchantOffers)offersObj).isEmpty()) {
            return this.merchantOffersToList(merchantOffers);
        }
        ListTag direct = this.invokeListTag(offersObj, "createTag", "toTag", "save");
        if (direct == null || direct.isEmpty()) {
            direct = this.invokeListTagArg(offersObj, "save", new ListTag());
        }
        if (direct != null && !direct.isEmpty()) {
            return this.copyListTag(direct);
        }
        ListTag out = new ListTag();
        if (offersObj instanceof Iterable) {
            Iterable iterable = (Iterable)offersObj;
            for (Object offer : iterable) {
                CompoundTag tag = this.serializeOffer(offer);
                if (tag == null || tag.isEmpty()) continue;
                out.add(tag);
            }
            return out;
        }
        if (offersObj instanceof List) {
            List list = (List)offersObj;
            for (Object offer : list) {
                CompoundTag tag = this.serializeOffer(offer);
                if (tag == null || tag.isEmpty()) continue;
                out.add(tag);
            }
            return out;
        }
        Integer size = this.invokeInt(offersObj, "size");
        if (size == null || size <= 0) {
            return out;
        }
        for (int i = 0; i < size; ++i) {
            Object offer = this.invokeAny(offersObj, "get", i);
            CompoundTag tag = this.serializeOffer(offer);
            if (tag == null || tag.isEmpty()) continue;
            out.add(tag);
        }
        return out.isEmpty() ? null : out;
    }

    private ListTag extractOfferRecipes(CompoundTag root) {
        ListTag recipes;
        if (root == null) {
            return null;
        }
        CompoundTag offers = this.readCompound(root, "Offers");
        if (offers == null) {
            offers = this.readCompound(root, "offers");
        }
        if (offers == null) {
            return null;
        }
        Object raw = this.readTag(offers, "Recipes");
        if (!(raw instanceof ListTag)) {
            raw = this.readTag(offers, "recipes");
        }
        if (!(raw instanceof ListTag)) {
            raw = this.readTag(offers, "Trades");
        }
        if (!(raw instanceof ListTag)) {
            raw = this.readTag(offers, "trades");
        }
        if (raw instanceof ListTag && !(recipes = (ListTag)raw).isEmpty()) {
            return this.copyListTag(recipes);
        }
        return null;
    }

    private ListTag merchantOffersToList(MerchantOffers offers) {
        ListTag out = new ListTag();
        for (MerchantOffer offer : offers) {
            CompoundTag tag = this.merchantOfferToTag(offer);
            if (tag == null || tag.isEmpty()) continue;
            out.add(tag);
        }
        return out;
    }

    private CompoundTag merchantOfferToTag(MerchantOffer offer) {
        if (offer == null) {
            return null;
        }
        CompoundTag buy = this.stackToTag(offer.getBaseCostA());
        CompoundTag buyB = this.stackToTag(offer.getCostB());
        CompoundTag sell = this.stackToTag(offer.getResult());
        if (buy == null || sell == null) {
            return null;
        }
        CompoundTag recipe = new CompoundTag();
        recipe.put("buy", (Tag)buy);
        recipe.put("base_cost_a", (Tag)this.copyCompound(buy));
        recipe.put("sell", (Tag)sell);
        recipe.put("result", (Tag)this.copyCompound(sell));
        if (buyB != null && !buyB.isEmpty()) {
            recipe.put("buyB", (Tag)buyB);
            recipe.put("cost_b", (Tag)this.copyCompound(buyB));
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
        Boolean reward;
        Float mul;
        Integer xpVal;
        Integer uses;
        Integer maxUses;
        if (offer == null) {
            return null;
        }
        CompoundTag fromApi = this.invokeCompound(offer, "createTag");
        if (fromApi == null) {
            fromApi = this.invokeCompound(offer, "save");
        }
        if (fromApi == null) {
            fromApi = this.invokeCompoundArg(offer, "save", new CompoundTag());
        }
        if (fromApi == null) {
            fromApi = this.invokeCompound(offer, "toTag");
        }
        if (fromApi != null && !fromApi.isEmpty()) {
            return fromApi;
        }
        CompoundTag buy = this.itemLikeToStackTag(this.invokeAny(offer, "getBaseCostA", "getCostA", "getBuyItem", "getFirstBuyItem"));
        CompoundTag buyB = this.itemLikeToStackTag(this.invokeAny(offer, "getCostB", "getSecondCost", "getSecondBuyItem"));
        CompoundTag sell = this.itemLikeToStackTag(this.invokeAny(offer, "getResult", "getSellItem", "getOutput"));
        if (buy == null || sell == null) {
            return null;
        }
        CompoundTag recipe = new CompoundTag();
        recipe.put("buy", (Tag)buy);
        recipe.put("base_cost_a", (Tag)this.copyCompound(buy));
        recipe.put("sell", (Tag)sell);
        recipe.put("result", (Tag)this.copyCompound(sell));
        if (buyB != null && !buyB.isEmpty()) {
            recipe.put("buyB", (Tag)buyB);
            recipe.put("cost_b", (Tag)this.copyCompound(buyB));
        }
        if ((maxUses = this.invokeInt(offer, "getMaxUses")) != null) {
            recipe.putInt("maxUses", Math.max(1, maxUses));
        }
        if ((uses = this.invokeInt(offer, "getUses")) != null) {
            recipe.putInt("uses", Math.max(0, uses));
        }
        if ((xpVal = this.invokeInt(offer, "getXp")) != null) {
            recipe.putInt("xp", Math.max(0, xpVal));
        }
        if ((mul = this.invokeFloat(offer, "getPriceMultiplier")) != null) {
            recipe.putFloat("priceMultiplier", mul.floatValue());
        }
        if ((reward = this.invokeBool(offer, "shouldRewardExp")) == null) {
            reward = this.invokeBool(offer, "isRewardExp");
        }
        if (reward != null) {
            recipe.putBoolean("rewardExp", reward.booleanValue());
        }
        return recipe;
    }

    private CompoundTag itemLikeToStackTag(Object itemLike) {
        Matcher matcher;
        if (itemLike == null) {
            return null;
        }
        if (itemLike instanceof ItemStack) {
            ItemStack stack = (ItemStack)itemLike;
            return this.stackToTag(stack);
        }
        Object stack = this.invokeAny(itemLike, "itemStack", "stack", "toItemStack", "asStack");
        if (stack instanceof ItemStack) {
            ItemStack st = (ItemStack)stack;
            return this.stackToTag(st);
        }
        String id = "";
        int count = 1;
        Object itemObj = this.invokeAny(itemLike, "item", "getItem", "value");
        if (itemObj instanceof Item) {
            Item item = (Item)itemObj;
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
        CompoundTag out = new CompoundTag();
        out.putString("id", id);
        out.putInt("count", count);
        return out;
    }

    private CompoundTag stackToTag(ItemStack stack) {
        CompoundTag legacyTag;
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(stack);
        if (fullOpt.isEmpty()) {
            return null;
        }
        CompoundTag full = fullOpt.get();
        CompoundTag out = new CompoundTag();
        out.putString("id", this.readString(full, "id", SpawnEggEditorHelper.getItemId(stack)));
        out.putInt("count", Math.max(1, this.readInt(full, "count", stack.getCount())));
        CompoundTag components = this.readCompound(full, "components");
        if (components != null && !components.isEmpty()) {
            out.put("components", (Tag)this.copyCompound(components));
        }
        if ((legacyTag = this.readCompound(full, "tag")) != null && !legacyTag.isEmpty()) {
            out.put("tag", (Tag)this.copyCompound(legacyTag));
        }
        return out;
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

    private CompoundTag invokeCompound(Object target, String method) {
        try {
            Optional opt;
            Object var6_7;
            Object out = target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
            if (out instanceof CompoundTag) {
                CompoundTag ct = (CompoundTag)out;
                return ct;
            }
            if (out instanceof Optional && (var6_7 = (opt = (Optional)out).orElse(null)) instanceof CompoundTag) {
                CompoundTag ct = (CompoundTag)var6_7;
                return ct;
            }
            return null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private CompoundTag invokeCompoundArg(Object target, String method, CompoundTag arg) {
        try {
            Optional opt;
            Object var7_8;
            Object out = target.getClass().getMethod(method, CompoundTag.class).invoke(target, arg);
            if (out instanceof CompoundTag) {
                CompoundTag ct = (CompoundTag)out;
                return ct;
            }
            if (out instanceof Optional && (var7_8 = (opt = (Optional)out).orElse(null)) instanceof CompoundTag) {
                CompoundTag ct = (CompoundTag)var7_8;
                return ct;
            }
            return null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private ListTag invokeListTag(Object target, String ... methods) {
        if (target == null || methods == null) {
            return null;
        }
        for (String method : methods) {
            try {
                Optional opt;
                Object var10_10;
                Object out = target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
                if (out instanceof ListTag) {
                    ListTag lt = (ListTag)out;
                    return lt;
                }
                if (!(out instanceof Optional) || !((var10_10 = (opt = (Optional)out).orElse(null)) instanceof ListTag)) continue;
                ListTag lt = (ListTag)var10_10;
                return lt;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private ListTag invokeListTagArg(Object target, String method, ListTag arg) {
        try {
            Optional opt;
            Object var7_8;
            Object out = target.getClass().getMethod(method, ListTag.class).invoke(target, arg);
            if (out instanceof ListTag) {
                ListTag lt = (ListTag)out;
                return lt;
            }
            if (out instanceof Optional && (var7_8 = (opt = (Optional)out).orElse(null)) instanceof ListTag) {
                ListTag lt = (ListTag)var7_8;
                return lt;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private void playUiClickFeedback(float pitch) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return;
            }
            Object manager = this.invokeAny(mc, "getSoundManager");
            if (manager == null) {
                return;
            }
            Class<?> eventsClass = Class.forName("net.minecraft.sounds.SoundEvents");
            Object uiClick = eventsClass.getField("UI_BUTTON_CLICK").get(null);
            if (uiClick == null) {
                return;
            }
            Object value = this.invokeAny(uiClick, "value");
            Class<?> simpleClass = Class.forName("net.minecraft.client.resources.sounds.SimpleSoundInstance");
            Object soundInst = null;
            for (Method m : simpleClass.getMethods()) {
                if (!"forUI".equals(m.getName()) || m.getParameterCount() != 2) continue;
                Class<?> p0 = m.getParameterTypes()[0];
                try {
                    if (p0.isInstance(uiClick)) {
                        soundInst = m.invoke(null, uiClick, Float.valueOf(pitch));
                        break;
                    }
                    if (value == null || !p0.isInstance(value)) continue;
                    soundInst = m.invoke(null, value, Float.valueOf(pitch));
                    break;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            if (soundInst == null) {
                return;
            }
            for (Method m : manager.getClass().getMethods()) {
                if (!"play".equals(m.getName()) || m.getParameterCount() != 1 || !m.getParameterTypes()[0].isInstance(soundInst)) continue;
                m.invoke(manager, soundInst);
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
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
        return Component.translatable((String)key).getString();
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    public void onClose() {
        this.tryClose();
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double my;
        double mx = event.x();
        if (this.mouseClicked(mx, my = event.y(), event.button())) {
            return true;
        }
        if (this.minecraft != null) {
            double sw = this.minecraft.getWindow().getScreenWidth();
            double sh = this.minecraft.getWindow().getScreenHeight();
            if (sw > 0.0 && sh > 0.0) {
                double sx = mx * (double)this.width / sw;
                double sy = my * (double)this.height / sh;
                if ((Math.abs(sx - mx) > 0.5 || Math.abs(sy - my) > 0.5) && this.mouseClicked(sx, sy, event.button())) {
                    return true;
                }
            }
        }
        return super.mouseClicked(event.x(), event.y(), event.button());
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

        void render(GuiGraphics g, Font font, int mx, int my, int accent) {
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
            g.fill(this.x, this.y, this.x + this.w, this.y + this.h, bg);
            g.fill(this.x, this.y, this.x + this.w, this.y + 1, edge);
            g.fill(this.x, this.y + this.h - 1, this.x + this.w, this.y + this.h, edge);
            g.fill(this.x, this.y, this.x + 1, this.y + this.h, edge);
            g.fill(this.x + this.w - 1, this.y, this.x + this.w, this.y + this.h, edge);
            Object text = this.label.get();
            if (font.width((String)text) > this.w - 10) {
                text = font.plainSubstrByWidth((String)text, this.w - 14) + "..";
            }
            int tx = this.w <= 24 ? this.x + (this.w - font.width((String)text)) / 2 : this.x + 6;
            g.drawString(font, (String)text, tx, this.y + 7, color, false);
        }
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

        private TradeData() {
        }

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

        private static String compKey(CompoundTag tag) {
            return tag == null ? "" : tag.toString();
        }

        private static CompoundTag copyTag(CompoundTag tag) {
            if (tag == null) {
                return null;
            }
            CompoundTag out = new CompoundTag();
            out.merge(tag);
            return out;
        }
    }

    private record IconHit(int x, int y, int w, int h, InvPickTarget target) {
        boolean hit(int mx, int my) {
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }
    }

    private record InvSlotHit(int x, int y, int w, int h, String itemId) {
        boolean hit(int mx, int my) {
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }
    }
}
