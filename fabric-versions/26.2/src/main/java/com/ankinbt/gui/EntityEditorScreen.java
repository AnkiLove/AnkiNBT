package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.util.UiSound;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import com.ankinbt.compat.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntityEditorScreen extends Screen {

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
    private static final int SIMPLE_ERROR = 0xFFEF4444;
    private static final Component HEAL_FULL_LABEL = Component.translatable("ankinbt.entity.heal_full");

    private final Entity targetEntity;
    private ItemStack sourceStack;
    private final int inventorySlot;
    private final Screen parent;

    private final List<UiBtn> buttons = new ArrayList<>();

    private int stNoAi = -1;
    private int stInvulnerable = -1;
    private int stNoGravity = -1;
    private int stSilent = -1;
    private int stBaby = -1;
    private boolean healToFullOnApply = false;
    private EditBox nameBox;
    private EditBox healthBox;

    private Component status = Component.empty();
    private int statusColor = TXT_DIM;
    private long statusTime = 0;
    private boolean dirty = false;
    private static final long AUTO_SAVE_DEBOUNCE_MS = 350L;
    private long lastEditAt = 0L;
    private boolean suppressDirtyTracking = false;
    private StateSnapshot autoSaveFailureState;
    private boolean confirmClose = false;
    private boolean confirmReset = false;
    private boolean applyPending = false;
    private final List<StateSnapshot> undoStack = new ArrayList<>();

    private int px, py, pw, ph;
    private EditorDock.Bounds barBounds;
    private EditorDock.Bounds drawerBounds;
    private boolean drawerAbove;
    private boolean drawerOpen = true;
    private float drawerAnim = 0f;
    private float contentAnim = 1f;
    private float activeIndicatorX = -1f;
    private final float[] tabHoverAnim = new float[4];
    private final float[] toolHoverAnim = new float[3];
    private EntityTab activeTab = EntityTab.GENERAL;
    private float openAnim = 0f;
    private float brandAnim = 0f;
    private float settingsHoverAnim = 0f;
    private float modalAnim = 0f;
    private final float[] modalButtonHover = new float[3];
    private boolean draggingPanel = false;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean resizingEditor;
    private boolean editorSizeFocused;
    private float editorScale = EditorDock.DEFAULT_EDITOR_SCALE;
    private float editorWidthAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
    private float editorHeightAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
    private float sizeControlHoverAnim;
    private boolean panelPositioned = false;
    private int layoutWidth = -1;
    private int layoutHeight = -1;
    private int tabScroll = 0;
    private int tabMaxScroll = 0;
    /**
     * Tooltip requests are collected while the clipped drawer/menu is rendered
     * and submitted once, after every editor surface has been drawn.
     */
    private Component pendingTooltip;
    private int pendingTooltipX;
    private int pendingTooltipY;
    private static final int MAX_UNDO = 40;

    private enum EntityTab {
        GENERAL("ankinbt.entity.tab.general", UiIcons.USER),
        STATES("ankinbt.entity.tab.states", UiIcons.ACTIVITY),
        INFO("ankinbt.entity.tab.info", UiIcons.INFO),
        TOOLS("ankinbt.entity.tab.tools", UiIcons.LAYERS);

        final String translationKey;
        final String icon;

        EntityTab(String translationKey, String icon) {
            this.translationKey = translationKey;
            this.icon = icon;
        }
    }

    private EntityEditorScreen(Entity targetEntity, ItemStack sourceStack, int inventorySlot, Screen parent) {
        super(Component.translatable("ankinbt.entity.title"));
        this.targetEntity = targetEntity;
        this.sourceStack = sourceStack == null ? ItemStack.EMPTY : sourceStack.copy();
        this.inventorySlot = inventorySlot;
        this.parent = parent;
    }

    public static EntityEditorScreen forEntity(Entity entity) {
        return new EntityEditorScreen(entity, ItemStack.EMPTY, -1, null);
    }

    public static EntityEditorScreen forEntity(Entity entity, Screen parent) {
        return new EntityEditorScreen(entity, ItemStack.EMPTY, -1, parent);
    }

    public static EntityEditorScreen forSpawnEgg(ItemStack stack, int inventorySlot) {
        return new EntityEditorScreen(null, stack, inventorySlot, null);
    }

    public static EntityEditorScreen forSpawnEgg(ItemStack stack, int inventorySlot, Screen parent) {
        return new EntityEditorScreen(null, stack, inventorySlot, parent);
    }

    @Override
    protected void init() {
        editorScale = AnkiConfig.getEntityEditorScale();
        editorWidthAdjustment = AnkiConfig.getEntityEditorWidthAdjustment();
        editorHeightAdjustment = AnkiConfig.getEntityEditorHeightAdjustment();
        String pendingName = nameBox == null ? currentCustomNameInput() : nameBox.getValue();
        String pendingHealth = healthBox == null ? currentHealthNumeric() : healthBox.getValue();
        boolean resuming = nameBox != null || healthBox != null;
        recalcBounds();
        nameBox = new EditBox(font, nameFieldX(), nameFieldY(), nameFieldWidth(), 16, Component.empty());
        styleBox(nameBox);
        nameBox.setValue(pendingName);
        nameBox.setResponder(v -> markDirty());
        addRenderableWidget(nameBox);
        healthBox = new EditBox(font, healthFieldX(), healthFieldY(), healthFieldWidth(), 16, Component.empty());
        styleBox(healthBox);
        healthBox.setValue(pendingHealth);
        healthBox.setResponder(v -> markDirty());
        addRenderableWidget(healthBox);
        rebuildButtons();
        if (!resuming) {
            undoStack.clear();
            undoStack.add(captureState());
        }
    }

    private void styleBox(EditBox box) {
        if (box == null) return;
        try {
            box.setBordered(false);
        } catch (Throwable ignored) {}
        try {
            box.setTextColor(UiTheme.textMain());
        } catch (Throwable ignored) {}
        try {
            box.setTextColorUneditable(UiTheme.textDim());
        } catch (Throwable ignored) {}
    }

    private void recalcBounds() {
        boolean viewportChanged = layoutWidth != width || layoutHeight != height;
        if (barBounds == null || drawerBounds == null || viewportChanged) {
            applyMenuLayout(EditorDock.menuLayout(width, height, 300, false, editorScale,
                    editorWidthAdjustment, editorHeightAdjustment));
        }
        layoutWidth = width;
        layoutHeight = height;
        layoutWidgets();
        if (viewportChanged && nameBox != null) {
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
        panelPositioned = true;
        layoutWidgets();
    }

    private int nameFieldX() {
        return valueColumnX();
    }

    private int nameFieldY() {
        return nameRowY() - 4;
    }

    private int nameFieldWidth() {
        return Math.max(28, valueColumnRight() - nameFieldX());
    }

    private int healthFieldX() {
        return valueColumnX();
    }

    private int healthFieldY() {
        return healthRowY() - 4;
    }

    private int healthFieldWidth() {
        return Math.max(28, Math.min(86, valueColumnRight() - healthFieldX()));
    }

    private int bodyTop() {
        return py + 34;
    }

    private int bodyBottom() {
        return py + ph - 30;
    }

    private int bodyHeight() {
        return Math.max(0, bodyBottom() - bodyTop());
    }

    private int infoCardX() {
        return px + 12;
    }

    private int infoCardWidth() {
        return Math.max(120, pw - 24);
    }

    private int actionCardX() {
        return infoCardX();
    }

    private int actionCardWidth() {
        return infoCardWidth();
    }

    private int labelColumnWidth() {
        int widest = 0;
        String[] keys = {
                "ankinbt.entity.info.name",
                "ankinbt.entity.info.type",
                "ankinbt.entity.info.pos",
                "ankinbt.entity.info.health",
                "ankinbt.entity.info.flags"
        };
        for (String key : keys) {
            widest = Math.max(widest, font.width(Component.translatable(key)));
        }
        int contentWidth = Math.max(1, infoCardWidth() - 20);
        int maximum = Math.max(52, Math.min(112, contentWidth * 2 / 5));
        return Math.max(52, Math.min(widest + 10, maximum));
    }

    private int valueColumnX() {
        return infoCardX() + 10 + labelColumnWidth();
    }

    private int valueColumnRight() {
        return infoCardX() + infoCardWidth() - 10;
    }

    private int valueColumnWidth() {
        return Math.max(1, valueColumnRight() - valueColumnX());
    }

    private boolean denseGeneralLayout() {
        return bodyHeight() < 152;
    }

    private int generalContentInset() {
        return denseGeneralLayout() ? 4 : 8;
    }

    private int generalCardGap() {
        return denseGeneralLayout() ? 3 : 6;
    }

    private int nameCardY() {
        return bodyTop() + generalContentInset();
    }

    private int nameCardHeight() {
        return denseGeneralLayout() ? 24 : 34;
    }

    private int nameRowY() {
        return nameCardY() + (denseGeneralLayout() ? 7 : 10);
    }

    private int healthCardY() {
        return nameCardY() + nameCardHeight() + generalCardGap();
    }

    private int healthCardHeight() {
        int available = Math.max(0, bodyBottom() - healthCardY());
        int preferred = denseGeneralLayout() ? 62 : 68;
        return Math.min(preferred, available);
    }

    private int healthRowY() {
        return healthCardY() + (denseGeneralLayout() ? 7 : 10);
    }

    private int previewCardY() {
        return healthCardY() + healthCardHeight() + generalCardGap();
    }

    private int previewCardHeight() {
        return Math.max(0, Math.min(30, bodyBottom() - previewCardY()));
    }

    private void layoutWidgets() {
        if (nameBox != null) {
            nameBox.setX(nameFieldX());
            nameBox.setY(nameFieldY());
            nameBox.setWidth(nameFieldWidth());
        }
        if (healthBox != null) {
            healthBox.setX(healthFieldX());
            healthBox.setY(healthFieldY());
            healthBox.setWidth(healthFieldWidth());
        }
        syncFieldAvailability();
    }

    private void syncFieldAvailability() {
        boolean enabled = activeTab == EntityTab.GENERAL
                && drawerOpen
                && drawerAnim >= 0.92f
                && !confirmClose
                && !confirmReset;
        if (nameBox != null) {
            boolean fits = nameBox.getY() >= bodyTop()
                    && nameBox.getY() + nameBox.getHeight() <= bodyBottom();
            nameBox.active = enabled && fits;
            nameBox.visible = enabled && fits;
            if (!nameBox.active) nameBox.setFocused(false);
        }
        if (healthBox != null) {
            boolean fits = healthBox.getY() >= bodyTop()
                    && healthBox.getY() + healthBox.getHeight() <= bodyBottom();
            healthBox.active = enabled && fits && canEditHealth();
            healthBox.visible = enabled && fits && canEditHealth();
            if (!healthBox.active) healthBox.setFocused(false);
        }
    }

    private void rebuildButtons() {
        buttons.clear();
        int x = px + 10;
        int y = bodyTop() + 8;
        int availableWidth = Math.max(120, pw - 20);
        int availableHeight = Math.max(1, bodyBottom() - y);
        int preferredRowHeight = AnkiConfig.isUiCompactLayout() ? 26 : 30;

        if (activeTab == EntityTab.STATES) {
            int preferredGap = 6;
            boolean cardGrid = "cards".equals(AnkiConfig.getUiOptionStyle()) && availableWidth >= 360;
            int singleColumnHeight = 5 * preferredRowHeight + 4 * preferredGap;
            int columns = cardGrid || singleColumnHeight > availableHeight ? 2 : 1;
            int rowCount = (5 + columns - 1) / columns;
            int gap = availableHeight < 84 ? 2 : preferredGap;
            int fittedHeight = Math.max(12,
                    (availableHeight - gap * (rowCount - 1)) / Math.max(1, rowCount));
            int rowH = Math.max(18, Math.min(preferredRowHeight, fittedHeight));
            int cellW = Math.max(56, (availableWidth - gap * (columns - 1)) / columns);
            int index = 0;
            buttons.add(stateBtn(x + (index % columns) * (cellW + gap), y + (index / columns) * (rowH + gap), cellW, rowH,
                    "ankinbt.entity.flag.no_ai", () -> stNoAi, v -> stNoAi = v));
            index++;
            buttons.add(stateBtn(x + (index % columns) * (cellW + gap), y + (index / columns) * (rowH + gap), cellW, rowH,
                    "ankinbt.entity.flag.invulnerable", () -> stInvulnerable, v -> stInvulnerable = v));
            index++;
            buttons.add(stateBtn(x + (index % columns) * (cellW + gap), y + (index / columns) * (rowH + gap), cellW, rowH,
                    "ankinbt.entity.flag.no_gravity", () -> stNoGravity, v -> stNoGravity = v));
            index++;
            buttons.add(stateBtn(x + (index % columns) * (cellW + gap), y + (index / columns) * (rowH + gap), cellW, rowH,
                    "ankinbt.entity.flag.silent", () -> stSilent, v -> stSilent = v));
            index++;
            buttons.add(stateBtn(x + (index % columns) * (cellW + gap), y + (index / columns) * (rowH + gap), cellW, rowH,
                    "ankinbt.entity.flag.baby", () -> stBaby, v -> stBaby = v));
        } else if (activeTab == EntityTab.TOOLS) {
            int toolCount = (hasVillagerTradeContext() ? 1 : 0) + (!sourceStack.isEmpty() ? 1 : 0);
            int gap = availableHeight < 60 ? 2 : 6;
            int fittedHeight = toolCount == 0 ? preferredRowHeight
                    : Math.max(12, (availableHeight - gap * (toolCount - 1)) / toolCount);
            int rowH = Math.max(18, Math.min(preferredRowHeight, fittedHeight));
            int index = 0;
            if (hasVillagerTradeContext()) {
                buttons.add(new UiBtn(x, y + index * (rowH + gap), availableWidth, rowH,
                        () -> Component.translatable("ankinbt.entity.open_villager").getString(),
                        this::openVillagerTradeEditor, true, null, 0));
                index++;
            }
            if (!sourceStack.isEmpty()) {
                buttons.add(new UiBtn(x, y + index * (rowH + gap), availableWidth, rowH,
                        () -> Component.translatable("ankinbt.entity.open_spawn_egg_nbt").getString(),
                        () -> Minecraft.getInstance().setScreenAndShow(new NbtEditorScreen(sourceStack)), true, null, 0));
            }
        }
        updateTabScrollBounds();
    }

    private boolean hasVillagerTradeContext() {
        if (targetEntity != null) {
            String type = targetEntity.getType().toString().toLowerCase(Locale.ROOT);
            if (type.contains("villager") || type.contains("wandering_trader")) return true;
        }
        return !sourceStack.isEmpty() && SpawnEggEditorHelper.isVillagerSpawnEgg(sourceStack);
    }

    private void openVillagerTradeEditor() {
        if (targetEntity != null) {
            String type = targetEntity.getType().toString().toLowerCase(Locale.ROOT);
            if (type.contains("villager") || type.contains("wandering_trader")) {
                Minecraft.getInstance().setScreenAndShow(VillagerTradeEditorScreen.forEntity(targetEntity, this));
                return;
            }
        }
        if (SpawnEggEditorHelper.isVillagerSpawnEgg(sourceStack)) {
            Minecraft.getInstance().setScreenAndShow(VillagerTradeEditorScreen.forSpawnEgg(sourceStack, inventorySlot, this));
        }
    }

    private UiBtn stateBtn(int x, int y, int w, int h, String key, Supplier<Integer> getter, java.util.function.IntConsumer setter) {
        return new UiBtn(x, y, w, h,
                () -> Component.translatable(key, stateText(getter.get())).getString(),
                () -> {
                    pushUndo();
                    setter.accept(nextState(getter.get()));
                    markDirty();
                }, true, () -> getter.get() > 0, 0);
    }

    private void openResetConfirm() {
        modalAnim = 0f;
        confirmClose = false;
        confirmReset = true;
    }

    private int nextState(int s) {
        if (s < 0) return 1;
        if (s > 0) return 0;
        return -1;
    }

    private String stateText(int s) {
        if (s < 0) return Component.translatable("ankinbt.entity.state.keep").getString();
        return s > 0 ? Component.translatable("ankinbt.simple.on").getString()
                : Component.translatable("ankinbt.simple.off").getString();
    }

    private void resetStates() {
        pushUndo();
        stNoAi = -1;
        stInvulnerable = -1;
        stNoGravity = -1;
        stSilent = -1;
        stBaby = -1;
        healToFullOnApply = false;
        setBoxValueSilently(nameBox, currentCustomNameInput());
        setBoxValueSilently(healthBox, currentHealthNumeric());
        dirty = false;
        setStatus(Component.translatable("ankinbt.entity.reset_done"), TXT_OK);
    }

    private CompoundTag buildPatch() {
        CompoundTag patch = new CompoundTag();
        putTriState(patch, "NoAI", stNoAi);
        putTriState(patch, "Invulnerable", stInvulnerable);
        putTriState(patch, "NoGravity", stNoGravity);
        putTriState(patch, "Silent", stSilent);
        putTriState(patch, "IsBaby", stBaby);
        if (stBaby == 1) patch.putInt("Age", -24000);
        if (stBaby == 0) patch.putInt("Age", 0);
        String customName = normalizeCustomNameInput(nameBox == null ? "" : nameBox.getValue()).trim();
        if (!Objects.equals(customName, normalizeCustomNameInput(currentCustomNameInput()).trim())) {
            patch.putString("CustomName", toCustomNameJson(customName));
            patch.putBoolean("CustomNameVisible", !customName.isBlank());
        }

        Float healthInput = parsePositiveFloat(healthBox == null ? "" : healthBox.getValue());
        Float currentMaxHealth = currentMaxHealth();
        if (healthInput != null && (targetEntity == null || currentMaxHealth == null || healthInput > currentMaxHealth + 0.01f)) {
            putMaxHealthPatch(patch, healthInput);
        }
        Float healthToApply = resolveHealthForApply(healthInput, currentMaxHealth);
        if (healthToApply != null) {
            patch.putFloat("Health", healthToApply);
        }

        if (stInvulnerable == 1) patch.putInt("NoDamageTicks", 32767);
        if (stInvulnerable == 0) patch.putInt("NoDamageTicks", 0);
        if (targetEntity == null && SpawnEggEditorHelper.isSpawnEgg(sourceStack) && !patch.contains("id")) {
            String id = SpawnEggEditorHelper.inferEntityIdFromSpawnEgg(sourceStack);
            if (!id.isBlank()) patch.putString("id", id);
        }

        return patch;
    }

    private void putMaxHealthPatch(CompoundTag patch, float health) {
        String attributeId = maxHealthAttributeId();
        net.minecraft.nbt.ListTag attrs = new net.minecraft.nbt.ListTag();
        CompoundTag attr = new CompoundTag();
        attr.putString("id", attributeId);
        attr.putDouble("base", health);
        attrs.add(attr);
        patch.put("attributes", attrs);

        net.minecraft.nbt.ListTag legacy = new net.minecraft.nbt.ListTag();
        CompoundTag legacyAttr = new CompoundTag();
        legacyAttr.putString("Name", attributeId);
        legacyAttr.putDouble("Base", health);
        legacy.add(legacyAttr);
        patch.put("Attributes", legacy);
    }

    private String maxHealthAttributeId() {
        return Attributes.MAX_HEALTH.unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("minecraft:max_health");
    }

    private void putTriState(CompoundTag patch, String key, int state) {
        if (state == -1) return;
        patch.putBoolean(key, state == 1);
    }

    private void applyPatch() {
        applyPatch(captureState(), false);
    }

    private void applyPatch(StateSnapshot savedState, boolean automatic) {
        Minecraft mc = Minecraft.getInstance();
        if (applyPending) return;
        if (!validateHealthInput()) return;
        CompoundTag patch = buildPatch();
        String customName = normalizeCustomNameInput(nameBox == null ? "" : nameBox.getValue()).trim();
        Float healthInput = parsePositiveFloat(healthBox == null ? "" : healthBox.getValue());
        Float currentMaxHealth = currentMaxHealth();
        Float healthToApply = resolveHealthForApply(healthInput, currentMaxHealth);
        if (patch.isEmpty()) {
            if (automatic) {
                settleSavedState(savedState, false);
            } else {
                setStatus(Component.translatable("ankinbt.entity.preview_empty"), TXT_DIM);
            }
            return;
        }

        if (targetEntity != null) {
            if (mc.player == null) return;
            if (mc.getSingleplayerServer() != null) {
                applyPending = true;
                setStatus(Component.translatable("ankinbt.status.applying"), TXT_DIM);
                applyPatchToIntegratedServerAsync(mc, customName, healthInput, currentMaxHealth, healthToApply,
                        ok -> finishEntityApply(mc, ok, patch, healthInput, currentMaxHealth, healthToApply,
                                savedState, automatic));
            } else {
                finishEntityApply(mc, false, patch, healthInput, currentMaxHealth, healthToApply,
                        savedState, automatic);
            }
            return;
        }

        if (!SpawnEggEditorHelper.isSpawnEgg(sourceStack)) {
            setStatus(Component.translatable("ankinbt.entity.spawn_egg_required"), TXT_ERR);
            return;
        }

        var patched = SpawnEggEditorHelper.withMergedEntityData(sourceStack, patch);
        if (patched.isEmpty()) {
            setStatus(Component.translatable("ankinbt.status.save_error"), TXT_ERR);
            if (automatic) autoSaveFailureState = savedState;
            return;
        }
        if (!SpawnEggEditorHelper.saveToCreativeSlot(mc, patched.get(), inventorySlot)) {
            setStatus(Component.translatable("ankinbt.status.creative_only"), TXT_ERR);
            if (automatic) autoSaveFailureState = savedState;
            return;
        }
        // Subsequent live saves must merge on top of the last successful entity-data patch.
        sourceStack = patched.get().copy();
        setStatus(Component.translatable(automatic ? "ankinbt.entity.autosaved" : "ankinbt.entity.applied"), TXT_OK);
        settleSavedState(savedState, !automatic);
    }

    private void finishEntityApply(Minecraft mc, boolean integratedOk, CompoundTag patch,
                                   Float healthInput, Float currentMaxHealth, Float healthToApply,
                                   StateSnapshot savedState, boolean automatic) {
        applyPending = false;
        boolean ok = integratedOk;
        if (!ok && !EditorCommandHelper.canUseEntityCommand(mc)) {
            setStatus(Component.translatable("ankinbt.entity.admin_required"), TXT_ERR);
            if (automatic) autoSaveFailureState = savedState;
            return;
        }
        if (!ok) ok = EditorCommandHelper.applyMergeToEntity(mc, targetEntity, patch);
        setStatus(ok ? Component.translatable(automatic ? "ankinbt.entity.autosaved" : "ankinbt.entity.applied")
                        : Component.translatable("ankinbt.status.save_error"),
                ok ? TXT_OK : TXT_ERR);
        if (!ok) {
            if (automatic) autoSaveFailureState = savedState;
            return;
        }

        applyLocalPreview(patch);
        if (healthInput != null && (currentMaxHealth == null || healthInput > currentMaxHealth + 0.01f)) {
            EditorCommandHelper.setEntityMaxHealth(mc, targetEntity, healthInput);
            setLocalMaxHealth(targetEntity, healthInput);
        }
        if (healthToApply != null) {
            if (targetEntity instanceof LivingEntity living) living.setHealth(healthToApply);
            setBoxValueSilently(healthBox, String.format(Locale.ROOT, "%.1f", healthToApply));
        }
        settleSavedState(savedState, !automatic);
    }

    private void markDirty() {
        if (suppressDirtyTracking) return;
        dirty = true;
        lastEditAt = System.currentTimeMillis();
        autoSaveFailureState = null;
    }

    private void setBoxValueSilently(EditBox box, String value) {
        if (box == null) return;
        boolean previous = suppressDirtyTracking;
        suppressDirtyTracking = true;
        box.setValue(value == null ? "" : value);
        suppressDirtyTracking = previous;
    }

    private void settleSavedState(StateSnapshot savedState, boolean resetUndo) {
        if (savedState == null || matchesSavedState(captureState(), savedState)) {
            dirty = false;
            autoSaveFailureState = null;
            if (resetUndo) {
                undoStack.clear();
                undoStack.add(captureState());
            }
            return;
        }
        dirty = true;
        lastEditAt = System.currentTimeMillis();
    }

    private boolean matchesSavedState(StateSnapshot current, StateSnapshot saved) {
        if (current == null || saved == null) return current == saved;
        return current.stNoAi() == saved.stNoAi()
                && current.stInvulnerable() == saved.stInvulnerable()
                && current.stNoGravity() == saved.stNoGravity()
                && current.stSilent() == saved.stSilent()
                && current.stBaby() == saved.stBaby()
                && current.healFull() == saved.healFull()
                && Objects.equals(current.name(), saved.name())
                && Objects.equals(normalizeHealthState(current.health()), normalizeHealthState(saved.health()));
    }

    private String normalizeHealthState(String value) {
        Float parsed = parsePositiveFloat(value);
        return parsed == null ? (value == null ? "" : value.trim())
                : String.format(Locale.ROOT, "%.3f", parsed);
    }

    private void maybeAutoSave() {
        if (!dirty || applyPending || confirmClose || confirmReset) return;
        long now = System.currentTimeMillis();
        if (now - lastEditAt < AUTO_SAVE_DEBOUNCE_MS) return;
        if (!canAutoSave()) return;
        StateSnapshot state = captureState();
        if (Objects.equals(state, autoSaveFailureState)) return;
        applyPatch(state, true);
    }

    private boolean canAutoSave() {
        if (targetEntity != null && Minecraft.getInstance().player == null) return false;
        if (targetEntity == null && !SpawnEggEditorHelper.isSpawnEgg(sourceStack)) return false;
        if (canEditHealth() && healthBox != null) {
            String raw = healthBox.getValue() == null ? "" : healthBox.getValue().trim();
            if (!raw.isEmpty() && parsePositiveFloat(raw) == null) return false;
        }
        return true;
    }

    private void setStatus(Component message, int color) {
        status = message;
        statusColor = color;
        statusTime = System.currentTimeMillis();
    }

    private Float parsePositiveFloat(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty()) return null;
        try {
            float v = Float.parseFloat(t);
            return v >= 0.0f ? v : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean validateHealthInput() {
        if (!canEditHealth() || healthBox == null) return true;
        String raw = healthBox.getValue() == null ? "" : healthBox.getValue().trim();
        if (raw.isEmpty() || parsePositiveFloat(raw) != null) return true;
        activeTab = EntityTab.GENERAL;
        drawerOpen = true;
        contentAnim = 1f;
        layoutWidgets();
        healthBox.setFocused(true);
        setStatus(Component.translatable("ankinbt.simple.invalid_number"), TXT_ERR);
        return false;
    }

    private Float resolveHealthForApply(Float healthInput, Float currentMaxHealth) {
        if (!healToFullOnApply) return healthInput;
        if (healthInput != null) {
            if (currentMaxHealth != null && healthInput < currentMaxHealth) return currentMaxHealth;
            return healthInput;
        }
        return currentMaxHealth;
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (confirmClose || confirmReset) {
            if (button != 0) return true;
            return clickConfirm((int) mx, (int) my);
        }
        if (button == 0 && EditorBrandLayer.isSettingsButton(mx, my, width)) {
            UiSound.playClick();
            Minecraft.getInstance().setScreenAndShow(new AnkiConfigScreen(this));
            return true;
        }
        if (button == 0) editorSizeFocused = false;
        if (button == 0 || button == 1) {
            EditorDock.SizeControl sizeControl = EditorDock.sizeControl(width, height, editorScale);
            if (button == 0 && sizeControl.reset().contains(mx, my)) {
                resetEditorSize();
                UiSound.playClick();
                return true;
            }
            if (sizeControl.horizontal().contains(mx, my)) {
                adjustEditorAxes(button == 1 ? EditorDock.AXIS_ADJUSTMENT_STEP
                        : -EditorDock.AXIS_ADJUSTMENT_STEP, 0.0f);
                UiSound.playClick();
                return true;
            }
            if (sizeControl.vertical().contains(mx, my)) {
                adjustEditorAxes(0.0f, button == 1 ? EditorDock.AXIS_ADJUSTMENT_STEP
                        : -EditorDock.AXIS_ADJUSTMENT_STEP);
                UiSound.playClick();
                return true;
            }
            if (button == 0 && sizeControl.hit().contains(mx, my)) {
                resizingEditor = true;
                editorSizeFocused = true;
                draggingPanel = false;
                blurFields();
                updateEditorScale(mx);
                return true;
            }
        }
        if (handleMenuBarClick(mx, my, button)) return true;
        if (!drawerOpen || drawerAnim < 0.92f || contentAnim < 0.92f
                || drawerBounds == null || !drawerBounds.contains(mx, my)) {
            if (button == 0) blurFields();
            return true;
        }
        if (activeTab == EntityTab.GENERAL && button == 0 && hitHealToggle((int) mx, (int) my)) {
            pushUndo();
            healToFullOnApply = !healToFullOnApply;
            markDirty();
            return true;
        }
        if (activeTab == EntityTab.GENERAL && button == 0 && clickHealthAdjuster((int) mx, (int) my)) {
            return true;
        }
        if (button != 0) return true;
        if (activeTab == EntityTab.GENERAL && handleTextFieldClick(mx, my, button)) {
            return true;
        }
        for (UiBtn btn : buttons) {
            if (btn.click((int) mx, (int) my, entityContentOffset())) {
                rebuildButtons();
                return true;
            }
        }
        blurFields();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (confirmClose || confirmReset) return true;
        if (!drawerOpen || drawerAnim < 0.20f || drawerBounds == null || !drawerBounds.contains(mx, my)) {
            return false;
        }
        int top = bodyTop();
        int bottom = bodyBottom();
        if (my < top || my >= bottom) return true;
        if (tabMaxScroll > 0) {
            int delta = (int) Math.round(scrollY * 12.0);
            if (delta == 0 && Math.abs(scrollY) > 0.01) delta = scrollY > 0 ? 1 : -1;
            tabScroll = Math.max(0, Math.min(tabMaxScroll, tabScroll - delta));
        }
        return true;
    }

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
        int count = EntityTab.values().length;
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
        int count = EntityTab.values().length;
        for (int i = 0; i < count; i++) {
            int x = menuTabX(i);
            int w = menuTabWidth(i);
            if (mx >= x && mx < x + w) return i;
        }
        return -1;
    }

    private void selectEntityTab(EntityTab selected) {
        if (selected == null) return;
        UiSound.playClick();
        if (selected == activeTab) {
            drawerOpen = !drawerOpen;
            if (!drawerOpen) blurFields();
            return;
        }
        activeTab = selected;
        tabScroll = 0;
        tabMaxScroll = 0;
        drawerOpen = true;
        contentAnim = AnkiConfig.isUiAnimationEnabled() ? 0f : 1f;
        blurFields();
        rebuildButtons();
    }

    private boolean handleMenuBarClick(double mx, double my, int button) {
        EditorDock.Bounds menu = menuBounds();
        if (button != 0 || menu == null || !menu.contains(mx, my)) return false;
        int brandW = menuBrandWidth();
        if (mx < menu.x() + brandW) {
            draggingPanel = true;
            dragOffsetX = (int) Math.round(mx) - (barBounds == null ? menu.x() : barBounds.x());
            dragOffsetY = (int) Math.round(my) - (barBounds == null ? menu.y() : barBounds.y());
            return true;
        }
        int toolW = menuToolWidth();
        int toolsStart = menuToolsStart();
        if (mx >= toolsStart) {
            int tool = Math.max(0, Math.min(2, ((int) mx - toolsStart) / toolW));
            if (tool == 0) openResetConfirm();
            else if (tool == 1) applyPatch();
            else tryClose();
            return true;
        }
        int index = menuTabAt(mx, my);
        if (index >= 0 && index < EntityTab.values().length) {
            selectEntityTab(EntityTab.values()[index]);
            return true;
        }
        return true;
    }

    private void blurFields() {
        if (getFocused() == nameBox || getFocused() == healthBox) {
            setFocused(null);
        }
        setDragging(false);
        if (nameBox != null) nameBox.setFocused(false);
        if (healthBox != null) healthBox.setFocused(false);
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
        boolean ctrl = (mod & 2) != 0;
        if (ctrl && key == 90) {
            undo();
            return true;
        }
        if (activeTab == EntityTab.GENERAL && drawerOpen && nameBox != null && nameBox.isFocused()) {
            if (pressEditBox(nameBox, key, scan, mod)) return true;
        }
        if (activeTab == EntityTab.GENERAL && drawerOpen && healthBox != null && healthBox.isFocused()) {
            if (pressEditBox(healthBox, key, scan, mod)) return true;
        }
        if ((key == 263 || key == 262)
                && (nameBox == null || !nameBox.isFocused())
                && (healthBox == null || !healthBox.isFocused())) {
            EntityTab[] tabs = EntityTab.values();
            int next = Math.floorMod(activeTab.ordinal() + (key == 263 ? -1 : 1), tabs.length);
            selectEntityTab(tabs[next]);
            return true;
        }
        if (key == 256) {
            tryClose();
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (confirmClose || confirmReset) return true;
        if (activeTab == EntityTab.GENERAL && drawerOpen && nameBox != null && nameBox.isFocused()) {
            if (typeEditBox(nameBox, codePoint, modifiers)) return true;
        }
        if (activeTab == EntityTab.GENERAL && drawerOpen && healthBox != null && healthBox.isFocused()) {
            return typeEditBox(healthBox, codePoint, modifiers);
        }
        return false;
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        render(new com.ankinbt.compat.GuiGraphics(g), mx, my, partialTick);
    }

    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        pendingTooltip = null;
        recalcBounds();
        float cfgSpeed = AnkiConfig.getUiAnimationSpeed();
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.06f, Math.min(0.16f, cfgSpeed)) : 1.0f;
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);
        float motionSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.10f, cfgSpeed * 1.7f) : 1.0f;
        drawerAnim = UiTheme.approach(drawerAnim, drawerOpen ? 1.0f : 0.0f, motionSpeed);
        contentAnim = UiTheme.approach(contentAnim, 1.0f, motionSpeed);
        syncFieldAvailability();
        maybeAutoSave();
        brandAnim = EditorBrandLayer.approachOpen(brandAnim);
        boolean settingsHovered = EditorBrandLayer.isSettingsButton(mx, my, width);
        settingsHoverAnim = EditorBrandLayer.approachSettingsHover(settingsHoverAnim, settingsHovered);
        modalAnim = UiTheme.approach(modalAnim, confirmClose || confirmReset ? 1.0f : 0.0f,
                Math.min(1.0f, motionSpeed * 1.8f));

        EditorBrandLayer.renderBackgroundLogo(g, width, height);
        g.fill(0, 0, width, height, UiTheme.scrim(AnkiConfig.getUiOpacity(), openAnim));
        renderEntityDrawer(g, mx, my);
        renderEntityMenuBar(g, mx, my);
        if (!confirmClose && !confirmReset) {
            sizeControlHoverAnim = EditorDock.renderSizeControl(g, font, width, height, mx, my,
                    editorScale, sizeControlHoverAnim, resizingEditor || editorSizeFocused,
                    UiTheme.accent(AnkiConfig.getUiAccentPreset()));
        }
        String mode = targetEntity != null
                ? Component.translatable("ankinbt.entity.mode.entity").getString()
                : Component.translatable("ankinbt.entity.mode.spawn_egg").getString();
        EditorBrandLayer.renderStatus(g, font, width, height, brandAnim, mode, editorScale,
                editorWidthAdjustment, editorHeightAdjustment);
        EditorBrandLayer.renderSettingsButton(g, font, width, mx, my, settingsHoverAnim);

        if (confirmReset) {
            renderConfirm(g, mx, my,
                    Component.translatable("ankinbt.entity.reset_changes").getString(),
                    Component.translatable("ankinbt.confirm.discard_hint").getString(),
                    0xFFEF4444);
        } else if (confirmClose) {
            renderUnsavedConfirmLikeSimple(g, mx, my);
        } else {
            renderPendingTooltip(g);
        }

    }

    private void requestTooltip(Component tooltip, int mx, int my) {
        if (tooltip == null) return;
        pendingTooltip = tooltip;
        pendingTooltipX = mx;
        pendingTooltipY = my;
    }

    private void renderPendingTooltip(GuiGraphics g) {
        if (pendingTooltip == null) return;
        com.ankinbt.compat.VersionCompat.get().renderTooltip(g, font, pendingTooltip,
                pendingTooltipX, pendingTooltipY);
    }

    private void renderEntityMenuBar(GuiGraphics g, int mx, int my) {
        EditorDock.Bounds menu = menuBounds();
        if (menu == null) return;
        int bx = menu.x();
        int by = menu.y();
        int bw = menu.width();
        int bh = menu.height();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(bx, by, bx + bw, by + bh, UiTheme.toolbar(AnkiConfig.getUiOpacity(), openAnim));
        border(g, bx, by, bw, bh, UiTheme.themedBorder(AnkiConfig.getUiOpacity(), openAnim));
        g.fill(bx, by + bh - 1, bx + bw, by + bh, UiTheme.withAlpha(accent & 0x00FFFFFF, Math.round(255 * openAnim)));

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
        float hoverSpeed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.18f, AnkiConfig.getUiAnimationSpeed() * 2.4f) : 1f;
        for (int i = 0; i < EntityTab.values().length; i++) {
            EntityTab tab = EntityTab.values()[i];
            int tx = menuTabX(i);
            int tabW = menuTabWidth(i);
            boolean hover = mx >= tx && mx < tx + tabW && my >= by && my < by + bh;
            boolean active = tab == activeTab;
            tabHoverAnim[i] = UiTheme.approach(tabHoverAnim[i], hover ? 1f : 0f, hoverSpeed);
            if (active && "segmented".equals(AnkiConfig.getUiNavigationStyle())) {
                g.fill(tx + 2, by + 3, tx + tabW - 2, by + bh - 3, UiTheme.withAlpha(accent & 0x00FFFFFF, 86));
            } else if (active && "underline".equals(AnkiConfig.getUiNavigationStyle())) {
                // Keep the active page legible even when the underline is selected.
                g.fill(tx + 2, by + 3, tx + tabW - 2, by + bh - 3,
                        UiTheme.withAlpha(accent & 0x00FFFFFF, 24));
            } else if (tabHoverAnim[i] > 0.01f) {
                g.fill(tx + 1, by + 2, tx + tabW - 1, by + bh - 2, UiTheme.mix(0x00000000, 0x4A334155, tabHoverAnim[i]));
            }
            if (i > 0 && tabW > 4) {
                g.fill(tx, by + 7, tx + 1, by + bh - 7,
                        UiTheme.withAlpha(UiTheme.themedBorder(1f, 1f) & 0x00FFFFFF, 72));
            }
            if (active && "compact".equals(AnkiConfig.getUiNavigationStyle())) {
                g.fill(tx + 3, by + 7, tx + 5, by + bh - 7, accent);
            }
            Component icon = UiIcons.component(tab.icon);
            String label = Component.translatable(tab.translationKey).getString();
            int tabTextColor = active ? UiTheme.textMain()
                    : (hover ? UiTheme.textMain() : UiTheme.textDim());
            int total = font.width(icon) + 4 + font.width(label);
            if (tabW >= total + 8) {
                int start = tx + (tabW - total) / 2;
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, icon, start, by + 10, tabTextColor, false);
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, start + font.width(icon) + 4, by + 10, tabTextColor, false);
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
                if (hover) requestTooltip(Component.literal(label), mx, my);
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

    private void renderMenuTool(GuiGraphics g, int mx, int my, int x, int y, int width, int index,
                                String glyph, Component tooltip) {
        boolean hover = mx >= x && mx < x + width && my >= y && my < y + EditorDock.MENU_BAR_HEIGHT;
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.18f, AnkiConfig.getUiAnimationSpeed() * 2.4f) : 1f;
        toolHoverAnim[index] = UiTheme.approach(toolHoverAnim[index], hover ? 1f : 0f, speed);
        g.fill(x, y + 2, x + width, y + EditorDock.MENU_BAR_HEIGHT - 2,
                UiTheme.mix(0x00000000, 0x4A334155, toolHoverAnim[index]));
        Component icon = UiIcons.component(glyph);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, icon, x + (width - font.width(icon)) / 2,
                y + 10, hover ? UiTheme.textMain() : UiTheme.textDim(), false);
        if (hover) requestTooltip(tooltip, mx, my);
    }

    private void renderEntityDrawer(GuiGraphics g, int mx, int my) {
        if (drawerAnim <= 0.01f || drawerBounds == null) return;
        int reveal = Math.max(1, Math.round(ph * drawerAnim));
        int clipTop = drawerAbove ? py + ph - reveal : py;
        int clipBottom = drawerAbove ? py + ph : py + reveal;
        g.enableScissor(px, clipTop, px + pw, clipBottom);
        g.fill(px, py, px + pw, py + ph, UiTheme.surface(AnkiConfig.getUiOpacity(), openAnim));
        border(g, px, py, pw, ph, UiTheme.themedBorder(AnkiConfig.getUiOpacity(), openAnim));

        g.fill(px + 8, py + 29, px + pw - 8, py + 30, UiTheme.themedBorder(1f, 1f));

        int offset = entityContentOffset();
        if (offset != 0) {
            nameBox.setY(nameFieldY() + offset);
            healthBox.setY(healthFieldY() + offset);
        } else {
            layoutWidgets();
        }
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        int bodyClipTop = Math.max(clipTop, bodyTop());
        int bodyClipBottom = Math.min(clipBottom, bodyBottom());
        if (bodyClipBottom > bodyClipTop) {
            g.enableScissor(px + 1, bodyClipTop, px + pw - 1, bodyClipBottom);
            if (activeTab == EntityTab.GENERAL) renderEntityGeneral(g, mx, my, accent, offset);
            else if (activeTab == EntityTab.INFO) renderEntityInfo(g, accent, offset);
            else {
                for (UiBtn btn : buttons) btn.render(g, font, mx, my, accent, offset);
                if (activeTab == EntityTab.TOOLS && buttons.isEmpty()) {
                    com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                            Component.translatable("ankinbt.entity.tools_empty"), px + 12,
                            bodyTop() + 14 + offset, UiTheme.textDim(), false);
                }
            }
            g.disableScissor();
            renderContentScrollbar(g, bodyClipTop, bodyClipBottom);
        }

        int footerY = py + ph - 26;
        g.fill(px + 1, footerY, px + pw - 1, footerY + 1, UiTheme.themedBorder(1f, 1f));
        Component footer = status != null && !status.getString().isEmpty() && System.currentTimeMillis() - statusTime < 2600
                ? status : Component.translatable("ankinbt.entity.footer_ready");
        int footerColor = footer == status ? statusColor : UiTheme.textDim();
        com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                trimToWidth(footer.getString(), Math.max(40, pw - 20)), px + 10, footerY + 9, footerColor, false);
        g.disableScissor();
    }

    private void renderEntityGeneral(GuiGraphics g, int mx, int my, int accent, int offset) {
        int x = infoCardX();
        int w = infoCardWidth();
        int nameY = nameCardY() + offset;
        renderOptionSurface(g, x, nameY, w, nameCardHeight(), mx, my, false);
        drawInlineLabel(g, x + 10, nameRowY() + offset,
                Component.translatable("ankinbt.entity.info.name").getString());
        renderInlineField(g, nameBox, currentName(), mx, my, accent);

        int healthY = healthCardY() + offset;
        int healthHeight = healthCardHeight();
        if (healthHeight > 0) {
            renderOptionSurface(g, x, healthY, w, healthHeight, mx, my, false);
        }
        drawInlineLabel(g, x + 10, healthRowY() + offset,
                Component.translatable("ankinbt.entity.info.health").getString());
        if (canEditHealth()) {
            renderInlineHealthField(g, mx, my, accent);
            renderHealthAdjusters(g, mx, my, accent);
        } else {
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, currentHealth(),
                    valueColumnX(), healthRowY() + offset, UiTheme.textMain(), false);
        }
        renderHealToggle(g, accent);

        int previewHeight = previewCardHeight();
        if (AnkiConfig.isEntityLivePreview() && previewHeight >= 24) {
            int previewY = previewCardY() + offset;
            renderOptionSurface(g, x, previewY, w, previewHeight, mx, my, false);
            String preview = trimToWidth(buildPatch().toString(), Math.max(40, w - 20));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                    Component.translatable("ankinbt.entity.section.preview"), x + 10, previewY + 7, accent, false);
            if (previewHeight >= 29) {
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, preview,
                        valueColumnX(), previewY + 7, UiTheme.textDim(), false);
            }
        }
    }

    private void renderEntityInfo(GuiGraphics g, int accent, int offset) {
        String[][] rows = {
                {Component.translatable("ankinbt.entity.info.name").getString(), currentName()},
                {Component.translatable("ankinbt.entity.info.type").getString(), currentType()},
                {Component.translatable("ankinbt.entity.info.pos").getString(), currentPos()},
                {Component.translatable("ankinbt.entity.info.health").getString(), currentHealth()},
                {Component.translatable("ankinbt.entity.info.flags").getString(), currentFlags()}
        };
        int x = px + 10;
        int baseY = bodyTop() + 4;
        int y = baseY + offset;
        int w = pw - 20;
        int availableHeight = Math.max(1, bodyBottom() - baseY);
        int preferredRowHeight = AnkiConfig.isUiCompactLayout() ? 28 : 32;
        int preferredGap = 5;
        int singleColumnHeight = rows.length * preferredRowHeight + (rows.length - 1) * preferredGap;
        int columns = singleColumnHeight > availableHeight ? 2 : 1;
        int rowCount = (rows.length + columns - 1) / columns;
        int gap = availableHeight < 84 ? 2 : preferredGap;
        int fittedHeight = Math.max(12,
                (availableHeight - gap * (rowCount - 1)) / Math.max(1, rowCount));
        int rowH = Math.min(preferredRowHeight, fittedHeight);
        int cellW = Math.max(1, (w - gap * (columns - 1)) / columns);
        int labelWidth = Math.min(labelColumnWidth(), Math.max(28, cellW - 38));
        for (int i = 0; i < rows.length; i++) {
            int col = i % columns;
            int row = i / columns;
            int rx = x + col * (cellW + gap);
            int ry = y + row * (rowH + gap);
            renderOptionSurface(g, rx, ry, cellW, rowH, -1, -1, false);
            int textY = ry + Math.max(3, (rowH - 8) / 2);
            String label = trimToWidth(rows[i][0] + ":", Math.max(18, labelWidth - 4));
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, label,
                    rx + 9, textY, UiTheme.textDim(), false);
            int valueX = rx + 10 + labelWidth;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font,
                    trimToWidth(rows[i][1], Math.max(20, rx + cellW - valueX - 8)), valueX, textY,
                    i == 0 ? accent : UiTheme.textMain(), false);
        }
    }

    private void renderOptionSurface(GuiGraphics g, int x, int y, int w, int h, int mx, int my, boolean selected) {
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        String style = AnkiConfig.getUiOptionStyle();
        int fill = "compact".equals(style) ? UiTheme.withAlpha(UiTheme.baseRgb(), hover ? 48 : 22)
                : UiTheme.card(AnkiConfig.getUiOpacity(), openAnim);
        if (selected) fill = UiTheme.withAlpha(UiTheme.accent(AnkiConfig.getUiAccentPreset()) & 0x00FFFFFF, 64);
        g.fill(x, y, x + w, y + h, fill);
        int edge = selected ? UiTheme.accent(AnkiConfig.getUiAccentPreset()) : UiTheme.themedBorder(AnkiConfig.getUiOpacity(), 1f);
        if ("rows".equals(style)) g.fill(x, y + h - 1, x + w, y + h, edge);
        else if ("compact".equals(style)) g.fill(x, y, x + (hover ? 2 : 1), y + h, edge);
        else border(g, x, y, w, h, edge);
    }

    private int entityContentOffset() {
        int revealOffset = Math.round((1f - contentAnim) * (drawerAbove ? -7f : 7f));
        return revealOffset - tabScroll;
    }

    private void updateTabScrollBounds() {
        int contentBottom = bodyBottom();
        if (!buttons.isEmpty()) {
            for (UiBtn button : buttons) {
                contentBottom = Math.max(contentBottom, button.y + button.h + 8);
            }
        }
        tabMaxScroll = Math.max(0, contentBottom - bodyBottom());
        tabScroll = Math.max(0, Math.min(tabScroll, tabMaxScroll));
    }

    private void renderContentScrollbar(GuiGraphics g, int clipTop, int clipBottom) {
        if (tabMaxScroll <= 0 || clipBottom <= clipTop) return;
        int trackTop = Math.max(clipTop + 2, bodyTop());
        int trackBottom = Math.min(clipBottom - 2, bodyBottom() - 2);
        int trackHeight = trackBottom - trackTop;
        if (trackHeight < 10) return;
        int thumbHeight = Math.max(8, Math.round(trackHeight * (trackHeight / (float) (trackHeight + tabMaxScroll))));
        int travel = Math.max(1, trackHeight - thumbHeight);
        int thumbY = trackTop + Math.round(travel * (tabScroll / (float) Math.max(1, tabMaxScroll)));
        int x = px + pw - 5;
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(x, trackTop, x + 2, trackBottom, UiTheme.withAlpha(UiTheme.baseRgb(), 54));
        g.fill(x, thumbY, x + 2, thumbY + thumbHeight,
                UiTheme.withAlpha(accent & 0x00FFFFFF, 190));
    }

    private int closeButtonX() {
        return px + pw - 30;
    }

    private void renderSectionCard(GuiGraphics g, int x, int y, int w, int h, Component heading,
                                   int accent, int card, int edge) {
        g.fill(x, y, x + w, y + h, card);
        border(g, x, y, w, h, edge);
        int marker = Math.max(18, Math.round((w - 2) * openAnim));
        g.fill(x + 1, y + 1, Math.min(x + w - 1, x + 1 + marker), y + 3,
                UiTheme.withAlpha(accent & 0x00FFFFFF, 210));
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, heading, x + 10, y + 10, accent, false);
    }

    private String trimToWidth(String value, int maxWidth) {
        if (value == null) return "";
        if (font.width(value) <= maxWidth) return value;
        return font.plainSubstrByWidth(value, Math.max(4, maxWidth - font.width("..."))) + "...";
    }

    private boolean pressEditBox(EditBox box, int key, int scan, int mod) {
        return box != null && box.keyPressed(new KeyEvent(key, scan, mod));
    }
    private boolean typeEditBox(EditBox box, char codePoint, int modifiers) {
        return box != null && box.charTyped(new CharacterEvent(codePoint));
    }

    private boolean typeEditBox(EditBox box, CharacterEvent event) {
        return box != null && event != null && box.charTyped(event);
    }

    private void drawInlineLabel(GuiGraphics g, int x, int y, String key) {
        String label = trimToWidth(key + ":", Math.max(20, labelColumnWidth() - 4));
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, x, y, UiTheme.textDim(), false);
    }

    private void renderInlineField(GuiGraphics g, EditBox box, String placeholder, int mx, int my, int accent) {
        if (box == null) return;
        boolean focused = box.isFocused();
        boolean hover = mx >= box.getX() && mx < box.getX() + box.getWidth() && my >= box.getY() && my < box.getY() + box.getHeight();
        String raw = box.getValue();
        boolean placeholderMode = (raw == null || raw.isBlank()) && !focused && placeholder != null && !placeholder.isBlank();
        if (placeholderMode) {
            String shown = placeholder;
            int maxWidth = Math.max(12, box.getWidth() - 4);
            if (font.width(shown) > maxWidth) {
                shown = font.plainSubstrByWidth(shown, maxWidth);
            }
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, shown,
                    box.getX(), box.getY() + 2, UiTheme.textDim(), false);
        } else {
            box.extractRenderState(g.unwrap(), mx, my, 0f);
        }
        int underline = focused ? accent : (hover
                ? UiTheme.withAlpha(accent & 0x00FFFFFF, 150)
                : UiTheme.themedBorder(AnkiConfig.getUiOpacity(), 1.0f));
        g.fill(box.getX(), box.getY() + box.getHeight() - 1, box.getX() + box.getWidth(), box.getY() + box.getHeight(), underline);
    }

    private void renderInlineHealthField(GuiGraphics g, int mx, int my, int accent) {
        if (healthBox == null) return;
        String raw = healthBox.getValue();
        if (raw == null || raw.isBlank()) raw = currentHealthNumeric();
        String shown = raw == null ? "" : raw;
        renderInlineField(g, healthBox, shown, mx, my, accent);
        Float max = currentMaxHealth();
        if (max != null) {
            String tail = " / " + String.format(Locale.ROOT, "%.1f", max);
            int tailX = Math.min(healthBox.getX() + Math.min(font.width(shown), healthBox.getWidth() - 4) + 8, healthBox.getX() + healthBox.getWidth() + 12);
            int tailLimit = infoCardX() + infoCardWidth() - 8;
            if (tailX + font.width(tail) <= tailLimit) {
                com.ankinbt.compat.VersionCompat.get().drawString(g, font, tail, tailX, healthBox.getY() + 2, UiTheme.textDim(), false);
            }
        }
    }

    private void renderHealthAdjusters(GuiGraphics g, int mx, int my, int accent) {
        if (!canEditHealth() || healthBox == null || !healthAdjustersVisible()) return;
        String[] labels = healthAdjustLabels();
        int y = healthAdjustBaseY();
        int h = 16;
        for (int i = 0; i < labels.length; i++) {
            int w = healthAdjustWidth(labels[i]);
            int bx = healthAdjustButtonX(labels, i);
            boolean hover = mx >= bx && mx < bx + w && my >= y && my < y + h;
            int fill = hover
                    ? UiTheme.withAlpha(accent & 0x00FFFFFF, 54)
                    : UiTheme.withAlpha(UiTheme.baseRgb(), "compact".equals(AnkiConfig.getUiOptionStyle()) ? 22 : 72);
            g.fill(bx, y, bx + w, y + h, fill);
            border(g, bx, y, w, h, hover ? accent : UiTheme.themedBorder(AnkiConfig.getUiOpacity(), 1.0f));
            String label = labels[i];
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, bx + (w - font.width(label)) / 2, y + 4, UiTheme.textMain(), false);
        }
    }

    private String currentName() {
        if (targetEntity != null) {
            String custom = currentCustomNameInput();
            if (!custom.isBlank()) return custom;
            return normalizeCustomNameInput(targetEntity.getDisplayName().getString());
        }
        if (!sourceStack.isEmpty()) return sourceStack.getHoverName().getString();
        return "-";
    }

    private String currentCustomNameInput() {
        if (targetEntity != null) {
            Component custom = targetEntity.getCustomName();
            return custom == null ? "" : normalizeCustomNameInput(custom.getString());
        }
        return "";
    }

    private String currentType() {
        if (targetEntity != null) return targetEntity.getType().toString().toLowerCase(Locale.ROOT);
        if (!sourceStack.isEmpty()) return SpawnEggEditorHelper.getItemId(sourceStack);
        return "-";
    }

    private String currentPos() {
        if (targetEntity == null) return "-";
        return String.format(Locale.ROOT, "%.1f, %.1f, %.1f", targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
    }

    private String currentHealth() {
        if (!(targetEntity instanceof LivingEntity living)) return "-";
        return String.format(Locale.ROOT, "%.1f / %.1f", living.getHealth(), living.getMaxHealth());
    }

    private String currentHealthNumeric() {
        if (!(targetEntity instanceof LivingEntity living)) return "";
        return String.format(Locale.ROOT, "%.1f", living.getHealth());
    }

    private boolean canEditHealth() {
        return targetEntity instanceof LivingEntity || !sourceStack.isEmpty();
    }

    private int healthAdjustBaseX() {
        return valueColumnX();
    }

    private int healthAdjustBaseY() {
        return healthBox == null ? healthRowY() + 18 : healthBox.getY() + 20;
    }

    private boolean clickHealthAdjuster(int mx, int my) {
        if (!canEditHealth() || healthBox == null || !healthAdjustersVisible()) return false;
        int y = healthAdjustBaseY();
        int h = 16;
        String[] labels = healthAdjustLabels();
        float[] deltas = healthAdjustDeltas();
        for (int i = 0; i < deltas.length; i++) {
            int w = healthAdjustWidth(labels[i]);
            int bx = healthAdjustButtonX(labels, i);
            if (mx >= bx && mx < bx + w && my >= y && my < y + h) {
                pushUndo();
                adjustHealthBy(deltas[i]);
                return true;
            }
        }
        return false;
    }

    private String[] healthAdjustLabels() {
        if (valueColumnWidth() < 94) return new String[]{"-1", "+1"};
        if (valueColumnWidth() < 132) return new String[]{"-1", "+1", "+10"};
        if (valueColumnWidth() < 168) return new String[]{"-10", "-1", "+1", "+10"};
        return new String[]{"-10", "-1", "+1", "+10", "+100"};
    }

    private float[] healthAdjustDeltas() {
        if (valueColumnWidth() < 94) return new float[]{-1.0f, 1.0f};
        if (valueColumnWidth() < 132) return new float[]{-1.0f, 1.0f, 10.0f};
        if (valueColumnWidth() < 168) return new float[]{-10.0f, -1.0f, 1.0f, 10.0f};
        return new float[]{-10.0f, -1.0f, 1.0f, 10.0f, 100.0f};
    }

    private boolean healthAdjustersVisible() {
        int cardBottom = healthCardY() + healthCardHeight() + entityContentOffset();
        return healthAdjustBaseY() + 16 <= cardBottom - 4;
    }

    private void adjustHealthBy(float delta) {
        Float current = parsePositiveFloat(healthBox == null ? "" : healthBox.getValue());
        if (current == null && targetEntity instanceof LivingEntity living) {
            current = living.getHealth();
        }
        if (current == null) current = 1.0f;
        float next = Math.max(0.0f, current + delta);
        if (healthBox != null) {
            setBoxValueSilently(healthBox, formatEditableHealth(next));
        }
        markDirty();
    }

    private String formatEditableHealth(float value) {
        float rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.001f) {
            return Integer.toString((int) rounded);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private String currentFlags() {
        if (targetEntity == null) return "-";
        boolean inv = targetEntity.isInvulnerable();
        boolean ng = targetEntity.isNoGravity();
        boolean sl = targetEntity.isSilent();
        return "Inv=" + inv + ", G=" + (!ng) + ", S=" + sl;
    }

    private void applyLocalPreview(CompoundTag patch) {
        if (targetEntity == null || patch == null) return;
        if (patch.contains("Invulnerable")) targetEntity.setInvulnerable(readBoolTag(patch, "Invulnerable", false));
        if (patch.contains("NoGravity")) targetEntity.setNoGravity(readBoolTag(patch, "NoGravity", false));
        if (patch.contains("Silent")) targetEntity.setSilent(readBoolTag(patch, "Silent", false));
        if (patch.contains("NoAI") && targetEntity instanceof Mob mob) mob.setNoAi(readBoolTag(patch, "NoAI", false));
        if (patch.contains("IsBaby") && targetEntity instanceof AgeableMob ageable) {
            if (readBoolTag(patch, "IsBaby", false)) ageable.setAge(-24000);
            else ageable.setAge(0);
        }
        if (patch.contains("CustomNameVisible")) targetEntity.setCustomNameVisible(readBoolTag(patch, "CustomNameVisible", false));
        if (patch.contains("CustomName")) {
            applyLocalCustomName(nameBox == null ? "" : nameBox.getValue());
        }
        if (patch.contains("Age") && targetEntity instanceof AgeableMob ageable) {
            ageable.setAge(readIntTag(patch, "Age", 0));
        }
    }

    private void applyLocalCustomName(String name) {
        if (targetEntity == null) return;
        String normalized = normalizeCustomNameInput(name);
        Component component = normalized.isBlank() ? null : Component.literal(normalized);
        targetEntity.setCustomName(component);
        targetEntity.setCustomNameVisible(!normalized.isBlank());
    }

    private int readIntTag(CompoundTag patch, String key, int def) {
        if (patch == null || key == null || key.isBlank()) return def;
        try {
            Object out = patch.getClass().getMethod("getInt", String.class).invoke(patch, key);
            if (out instanceof Number n) return n.intValue();
            if (out instanceof java.util.Optional<?> opt && opt.orElse(null) instanceof Number n) return n.intValue();
        } catch (Throwable ignored) {}
        try {
            Object raw = patch.getClass().getMethod("get", String.class).invoke(patch, key);
            if (raw instanceof java.util.Optional<?> opt) raw = opt.orElse(null);
            if (raw != null) {
                Object out = raw.getClass().getMethod("getAsInt").invoke(raw);
                if (out instanceof Number n) return n.intValue();
            }
        } catch (Throwable ignored) {}
        return def;
    }

    private boolean readBoolTag(CompoundTag patch, String key, boolean def) {
        if (patch == null || key == null || key.isBlank()) return def;
        try {
            Object out = patch.getClass().getMethod("getBoolean", String.class).invoke(patch, key);
            if (out instanceof Boolean b) return b;
            if (out instanceof java.util.Optional<?> opt && opt.orElse(null) instanceof Boolean b) return b;
        } catch (Throwable ignored) {}
        try {
            Object raw = patch.getClass().getMethod("get", String.class).invoke(patch, key);
            if (raw instanceof java.util.Optional<?> opt) raw = opt.orElse(null);
            if (raw != null) {
                Object out = raw.getClass().getMethod("getAsBoolean").invoke(raw);
                if (out instanceof Boolean b) return b;
            }
        } catch (Throwable ignored) {}
        return def;
    }

    private void setLocalMaxHealth(Object entity, float value) {
        if (!(entity instanceof LivingEntity living) || value <= 0.0f) return;
        var attr = living.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) attr.setBaseValue(value);
    }

    private Float currentMaxHealth() {
        if (!(targetEntity instanceof LivingEntity living)) return null;
        return living.getMaxHealth();
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private int healToggleX() {
        return valueColumnX();
    }

    private int healToggleY() {
        if (canEditHealth() && healthBox != null) {
            return healthAdjustBaseY() + 20;
        }
        return healthRowY() + entityContentOffset() + 20;
    }

    private boolean hitHealToggle(int mx, int my) {
        int x = healToggleX();
        int y = healToggleY();
        if (!healToggleVisible()) return false;
        int size = 12;
        int textW = font.width(healToggleText());
        return mx >= x && mx < x + size + 6 + textW && my >= y && my < y + size;
    }

    private void renderHealToggle(GuiGraphics g, int accent) {
        int x = healToggleX();
        int y = healToggleY();
        int size = 12;
        if (!healToggleVisible()) return;
        g.fill(x, y, x + size, y + size, UiTheme.withAlpha(UiTheme.baseRgb(), 84));
        border(g, x, y, size, size, healToFullOnApply ? accent : UiTheme.themedBorder(AnkiConfig.getUiOpacity(), 1.0f));
        if (healToFullOnApply) g.fill(x + 3, y + 3, x + size - 3, y + size - 3, accent);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, healToggleText(),
                x + size + 6, y + 2, UiTheme.textMain(), false);
    }

    private boolean healToggleVisible() {
        int cardBottom = healthCardY() + healthCardHeight() + entityContentOffset();
        return healToggleY() + 12 <= cardBottom - 4;
    }

    private String healToggleText() {
        return trimToWidth(HEAL_FULL_LABEL.getString(), Math.max(8, valueColumnWidth() - 18));
    }

    private boolean handleTextFieldClick(double mx, double my, int button) {
        if (focusInlineBox(nameBox, mx, my, null)) {
            if (healthBox != null) healthBox.setFocused(false);
            return true;
        }
        if (nameBox != null) nameBox.setFocused(false);

        if (canEditHealth() && focusInlineBox(healthBox, mx, my, currentHealthNumeric())) {
            if (nameBox != null) nameBox.setFocused(false);
            return true;
        }
        if (healthBox != null) healthBox.setFocused(false);
        this.clearFocus();
        return false;
    }

    private boolean focusInlineBox(EditBox box, double mx, double my, String fallback) {
        if (box == null || !box.active || !box.visible || !hitInlineField(box, mx, my)) return false;
        if (!box.isFocused()) pushUndo();
        if ((box.getValue() == null || box.getValue().isBlank()) && fallback != null && !fallback.isBlank()) {
            setInlineBoxValue(box, fallback);
        }
        box.setFocused(true);
        this.setFocused(box);
        setDragging(true);
        return true;
    }

    private boolean hitInlineField(EditBox box, double mx, double my) {
        if (box == null) return false;
        return mx >= box.getX() - 2 && mx < box.getX() + box.getWidth() + 2
                && my >= box.getY() - 2 && my < box.getY() + box.getHeight() + 2;
    }

    private void setInlineBoxValue(EditBox box, String value) {
        if (box == null) return;
        String next = box == nameBox ? normalizeCustomNameInput(value) : (value == null ? "" : value);
        setBoxValueSilently(box, next);
    }

    private int healthAdjustWidth(String label) {
        return Math.max(28, font.width(label) + 12);
    }

    private int healthAdjustButtonX(String[] labels, int index) {
        int x = healthAdjustBaseX();
        for (int i = 0; i < index; i++) {
            x += healthAdjustWidth(labels[i]) + 4;
        }
        return x;
    }

    private String toCustomNameJson(String value) {
        return "{\"text\":" + jsonString(normalizeCustomNameInput(value)) + "}";
    }

    private String jsonString(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(ch);
            }
        }
        out.append('"');
        return out.toString();
    }

    private String normalizeCustomNameInput(String value) {
        if (value == null || value.isBlank()) return "";
        String trimmed = value.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return value;
        }
        try {
            String decoded = extractJsonText(JsonParser.parseString(trimmed));
            return decoded == null ? value : decoded;
        } catch (Throwable ignored) {
            return value;
        }
    }

    private String extractJsonText(JsonElement element) {
        if (element == null || element.isJsonNull()) return "";
        if (element.isJsonPrimitive()) return element.getAsString();
        if (element.isJsonArray()) {
            StringBuilder out = new StringBuilder();
            for (JsonElement entry : element.getAsJsonArray()) {
                String text = extractJsonText(entry);
                if (text != null) out.append(text);
            }
            return out.toString();
        }
        if (!element.isJsonObject()) return null;
        JsonObject object = element.getAsJsonObject();
        StringBuilder out = new StringBuilder();
        if (object.has("text")) {
            out.append(object.get("text").getAsString());
        }
        if (object.has("extra") && object.get("extra").isJsonArray()) {
            for (JsonElement extra : object.getAsJsonArray("extra")) {
                String text = extractJsonText(extra);
                if (text != null) out.append(text);
            }
        }
        return out.toString();
    }

    private void applyPatchToIntegratedServerAsync(Minecraft mc, String customName, Float healthInput,
                                                   Float currentMaxHealth, Float healthToApply,
                                                   Consumer<Boolean> completion) {
        if (mc == null || targetEntity == null) {
            completion.accept(false);
            return;
        }
        var server = mc.getSingleplayerServer();
        if (server == null) {
            completion.accept(false);
            return;
        }

        try {
            server.execute(() -> {
                boolean success = false;
                try {
                    Entity serverEntity = EditorCommandHelper.findIntegratedServerEntity(
                            server, targetEntity.getId(), targetEntity.getUUID());
                    if (serverEntity != null) {
                        if (stInvulnerable != -1) serverEntity.setInvulnerable(stInvulnerable == 1);
                        if (stNoGravity != -1) serverEntity.setNoGravity(stNoGravity == 1);
                        if (stSilent != -1) serverEntity.setSilent(stSilent == 1);
                        if (stNoAi != -1 && serverEntity instanceof Mob mob) mob.setNoAi(stNoAi == 1);
                        if (stBaby != -1 && serverEntity instanceof AgeableMob ageable) {
                            ageable.setAge(stBaby == 1 ? -24000 : 0);
                        }

                        Component serverName = customName.isBlank() ? null : Component.literal(customName);
                        serverEntity.setCustomName(serverName);
                        serverEntity.setCustomNameVisible(!customName.isBlank());

                        if (serverEntity instanceof LivingEntity living) {
                            if (healthInput != null
                                    && (currentMaxHealth == null || healthInput > currentMaxHealth + 0.01f)) {
                                var attr = living.getAttribute(Attributes.MAX_HEALTH);
                                if (attr != null) attr.setBaseValue(healthInput);
                            }
                            if (healthToApply != null) living.setHealth(Math.max(0.0f, healthToApply));
                        }
                        success = true;
                    }
                } catch (Throwable ignored) {
                }
                boolean result = success;
                mc.execute(() -> completion.accept(result));
            });
        } catch (Throwable ignored) {
            applyPending = false;
            completion.accept(false);
        }
    }

    @Override
    public void onClose() {
        tryClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (keyPressed(event.key(), event.scancode(), event.modifiers())) return true;
        return super.keyPressed(event);
    }
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (confirmClose || confirmReset) return true;
        if (activeTab == EntityTab.GENERAL && drawerOpen && nameBox != null && nameBox.isFocused()
                && typeEditBox(nameBox, event)) return true;
        if (activeTab == EntityTab.GENERAL && drawerOpen && healthBox != null && healthBox.isFocused()
                && typeEditBox(healthBox, event)) return true;
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
            applyMenuLayout(EditorDock.menuLayoutAt(width, height, 300, false,
                    (int) Math.round(event.x()) - dragOffsetX,
                    (int) Math.round(event.y()) - dragOffsetY, editorScale,
                    editorWidthAdjustment, editorHeightAdjustment));
            rebuildButtons();
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
            AnkiConfig.setEntityEditorScale(editorScale);
            return true;
        }
        if (draggingPanel && event.button() == 0) {
            draggingPanel = false;
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
        applyMenuLayout(EditorDock.resizeLayout(width, height, false, current, 300, editorScale,
                editorWidthAdjustment, editorHeightAdjustment));
        rebuildButtons();
        if (save) AnkiConfig.setEntityEditorScale(editorScale);
    }

    private void adjustEditorAxes(float widthDelta, float heightDelta) {
        editorWidthAdjustment = EditorDock.adjustAxis(editorWidthAdjustment, widthDelta);
        editorHeightAdjustment = EditorDock.adjustAxis(editorHeightAdjustment, heightDelta);
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, false, current, 300, editorScale,
                editorWidthAdjustment, editorHeightAdjustment));
        rebuildButtons();
        AnkiConfig.setEntityEditorAxisAdjustments(editorWidthAdjustment, editorHeightAdjustment);
    }

    private void resetEditorSize() {
        AnkiConfig.resetEntityEditorSizeToItem();
        editorScale = AnkiConfig.getEntityEditorScale();
        editorWidthAdjustment = AnkiConfig.getEntityEditorWidthAdjustment();
        editorHeightAdjustment = AnkiConfig.getEntityEditorHeightAdjustment();
        EditorDock.MenuLayout current = barBounds == null || drawerBounds == null
                ? null : new EditorDock.MenuLayout(barBounds, drawerBounds, drawerAbove);
        applyMenuLayout(EditorDock.resizeLayout(width, height, false, current, 300, editorScale,
                editorWidthAdjustment, editorHeightAdjustment));
        rebuildButtons();
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
            return click(mx, my, 0);
        }

        boolean click(int mx, int my, int offsetY) {
            if (!enabled || mx < x || mx >= x + w || my < y + offsetY || my >= y + offsetY + h) return false;
            action.run();
            return true;
        }

        void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int accent) {
            render(g, font, mx, my, accent, 0);
        }

        void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int accent, int offsetY) {
            int drawY = y + offsetY;
            boolean hover = mx >= x && mx < x + w && my >= drawY && my < drawY + h;
            boolean chosen = selected != null && Boolean.TRUE.equals(selected.get());
            float speed = AnkiConfig.isUiAnimationEnabled() ? Math.min(1.0f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1.0f;
            hoverAnim = UiTheme.approach(hoverAnim, hover ? 1.0f : 0.0f, speed);
            String optionStyle = AnkiConfig.getUiOptionStyle();

            int bg;
            int edge;
            if (!enabled) {
                bg = UiTheme.withAlpha(UiTheme.baseRgb(), 30);
                edge = UiTheme.themedBorder(AnkiConfig.getUiOpacity(), 0.55f);
            } else if (style == 1) {
                bg = UiTheme.withAlpha(0x166534, 116 + Math.round(42 * hoverAnim));
                edge = 0xFF22C55E;
            } else if (style == -1) {
                bg = UiTheme.withAlpha(0x991B1B, 104 + Math.round(46 * hoverAnim));
                edge = 0xFFEF4444;
            } else {
                int neutralAlpha = "compact".equals(optionStyle) ? 24 : "rows".equals(optionStyle) ? 38 : 68;
                bg = chosen
                        ? UiTheme.withAlpha(accent & 0x00FFFFFF, 82 + Math.round(36 * hoverAnim))
                        : UiTheme.withAlpha(UiTheme.baseRgb(), neutralAlpha + Math.round(44 * hoverAnim));
                edge = chosen ? accent : UiTheme.themedBorder(AnkiConfig.getUiOpacity(), 1.0f);
            }
            int color = enabled ? UiTheme.textMain() : UiTheme.textDim();

            g.fill(x, drawY, x + w, drawY + h, bg);
            if ("rows".equals(optionStyle)) {
                g.fill(x, drawY + h - 1, x + w, drawY + h, edge);
            } else if ("compact".equals(optionStyle)) {
                g.fill(x, drawY, x + (chosen || hoverAnim > 0.05f ? 2 : 1), drawY + h, edge);
            } else {
                g.fill(x, drawY, x + w, drawY + 1, edge);
                g.fill(x, drawY + h - 1, x + w, drawY + h, edge);
                g.fill(x, drawY, x + 1, drawY + h, edge);
                g.fill(x + w - 1, drawY, x + w, drawY + h, edge);
            }

            String text = label.get();
            if (font.width(text) > w - 10) text = font.plainSubstrByWidth(text, Math.max(4, w - 18)) + "...";
            int textY = drawY + Math.max(1, (h - 8) / 2);
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, x + 6, textY, color, false);
        }
    }

    private record StateSnapshot(int stNoAi, int stInvulnerable, int stNoGravity, int stSilent, int stBaby, boolean healFull, String name, String health) {}

    private StateSnapshot captureState() {
        return new StateSnapshot(stNoAi, stInvulnerable, stNoGravity, stSilent, stBaby, healToFullOnApply,
                nameBox == null ? "" : nameBox.getValue(), healthBox == null ? "" : healthBox.getValue());
    }

    private void applyState(StateSnapshot s) {
        if (s == null) return;
        stNoAi = s.stNoAi;
        stInvulnerable = s.stInvulnerable;
        stNoGravity = s.stNoGravity;
        stSilent = s.stSilent;
        stBaby = s.stBaby;
        healToFullOnApply = s.healFull;
        setBoxValueSilently(nameBox, s.name == null ? "" : s.name);
        setBoxValueSilently(healthBox, s.health == null ? "" : s.health);
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
        StateSnapshot current = captureState();
        if (Objects.equals(previous, current) && !undoStack.isEmpty()) {
            previous = undoStack.remove(undoStack.size() - 1);
        }
        applyState(previous);
        markDirty();
        setStatus(Component.translatable("ankinbt.status.edited"), TXT_DIM);
    }

    private void tryClose() {
        if (dirty && AnkiConfig.isConfirmOnClose()) {
            modalAnim = 0f;
            confirmReset = false;
            confirmClose = true;
            return;
        }
        Minecraft.getInstance().setScreenAndShow(parent);
    }

    private void renderConfirm(GuiGraphics g, int mx, int my, String title, String desc, int color) {
        int w = Math.min(320, width - 24), h = 126;
        int x = (width - w) / 2;
        int y = modalDialogY(h);
        float opacity = AnkiConfig.getUiOpacity();
        int edge = UiTheme.themedBorder(opacity, modalAnim);
        g.fill(0, 0, width, height, UiTheme.withAlpha(0x000000, Math.round(152 * modalAnim)));
        g.fill(x, y, x + w, y + h, UiTheme.panel(Math.max(0.72f, opacity), modalAnim));
        border(g, x, y, w, h, edge);
        g.fill(x + 1, y + 1, x + w - 1, y + 3, UiTheme.withAlpha(color & 0x00FFFFFF, Math.round(220 * modalAnim)));
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, x + 12, y + 12, color, false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, trimToWidth(desc, w - 24), x + 12, y + 34, UiTheme.textMain(), false);

        int by = y + h - 34;
        int gap = 8;
        int bw = (w - 24 - gap) / 2;
        renderModalButton(g, x + 12, by, bw, 22, Component.translatable("ankinbt.edit.cancel").getString(), mx, my, 0, 0);
        renderModalButton(g, x + 12 + bw + gap, by, bw, 22, Component.translatable("ankinbt.edit.apply").getString(), mx, my, 1, -1);
    }

    private void renderUnsavedConfirmLikeSimple(GuiGraphics g, int mx, int my) {
        int dw = Math.min(320, width - 24), dh = 126;
        int dx = (width - dw) / 2, dy = modalDialogY(dh);
        float opacity = AnkiConfig.getUiOpacity();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        g.fill(0, 0, width, height, UiTheme.withAlpha(0x000000, Math.round(152 * modalAnim)));
        g.fill(dx, dy, dx + dw, dy + dh, UiTheme.panel(Math.max(0.72f, opacity), modalAnim));
        border(g, dx, dy, dw, dh, UiTheme.themedBorder(opacity, modalAnim));
        g.fill(dx + 1, dy + 1, dx + dw - 1, dy + 3, UiTheme.withAlpha(accent & 0x00FFFFFF, Math.round(220 * modalAnim)));

        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.confirm.title"), dx + 12, dy + 12, UiTheme.textMain(), false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, trimToWidth(Component.translatable("ankinbt.confirm.unsaved").getString(), dw - 24), dx + 12, dy + 34, UiTheme.textDim(), false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, trimToWidth(Component.translatable("ankinbt.confirm.discard_hint").getString(), dw - 24), dx + 12, dy + 49, UiTheme.textDim(), false);

        int by = dy + dh - 34;
        int gap = 6;
        int bw = (dw - 24 - gap * 2) / 3;
        renderModalButton(g, dx + 12, by, bw, 22, Component.translatable("ankinbt.confirm.save_close").getString(), mx, my, 0, 1);
        renderModalButton(g, dx + 12 + bw + gap, by, bw, 22, Component.translatable("ankinbt.confirm.discard").getString(), mx, my, 1, -1);
        renderModalButton(g, dx + 12 + (bw + gap) * 2, by, bw, 22, Component.translatable("ankinbt.edit.cancel").getString(), mx, my, 2, 0);
    }

    private int modalDialogY(int height) {
        return (this.height - height) / 2 + Math.round((1.0f - modalAnim) * 12.0f);
    }

    private void renderModalButton(GuiGraphics g, int x, int y, int w, int h, String label,
                                   int mx, int my, int index, int tone) {
        boolean hovered = mx >= x && mx < x + w && my >= y && my < y + h;
        float speed = AnkiConfig.isUiAnimationEnabled() ? Math.min(1.0f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1.0f;
        modalButtonHover[index] = UiTheme.approach(modalButtonHover[index], hovered ? 1.0f : 0.0f, speed);
        int color = tone > 0 ? 0xFF22C55E : tone < 0 ? 0xFFEF4444 : UiTheme.accent(AnkiConfig.getUiAccentPreset());
        int fill = UiTheme.withAlpha(color & 0x00FFFFFF, 42 + Math.round(58 * modalButtonHover[index]));
        g.fill(x, y, x + w, y + h, fill);
        border(g, x, y, w, h, UiTheme.withAlpha(color & 0x00FFFFFF, 170 + Math.round(70 * modalButtonHover[index])));
        String shown = trimToWidth(label, w - 12);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, shown,
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
        if (mx >= x + 12 && mx < x + 12 + bw && my >= by && my < by + 22) {
            confirmClose = false;
            confirmReset = false;
            return true;
        }
        int applyX = x + 12 + bw + gap;
        if (mx >= applyX && mx < applyX + bw && my >= by && my < by + 22) {
            if (confirmReset) {
                confirmReset = false;
                resetStates();
            } else if (confirmClose) {
                confirmClose = false;
                dirty = false;
                Minecraft.getInstance().setScreenAndShow(parent);
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
            applyPatch();
            if (!dirty) {
                confirmClose = false;
                Minecraft.getInstance().setScreenAndShow(parent);
            }
            return true;
        }
        int discardX = saveX + bw2 + gap;
        if (mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2) {
            confirmClose = false;
            dirty = false;
            Minecraft.getInstance().setScreenAndShow(parent);
            return true;
        }
        int cancelX = discardX + bw2 + gap;
        if (mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2) {
            confirmClose = false;
            return true;
        }
        return true;
    }
}
