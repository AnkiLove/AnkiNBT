package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.editor.SpawnEggEditorHelper;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final ItemStack sourceStack;
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
    private boolean confirmClose = false;
    private boolean confirmReset = false;
    private final List<StateSnapshot> undoStack = new ArrayList<>();

    private int px, py, pw, ph;
    private float openAnim = 0f;
    private static final int MAX_UNDO = 40;

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
        recalcBounds();
        nameBox = new EditBox(font, nameFieldX(), nameFieldY(), 192, 16, Component.empty());
        styleBox(nameBox);
        nameBox.setValue(currentCustomNameInput());
        nameBox.setResponder(v -> dirty = true);
        addRenderableWidget(nameBox);
        healthBox = new EditBox(font, healthFieldX(), healthFieldY(), 88, 16, Component.empty());
        styleBox(healthBox);
        healthBox.setValue(currentHealthNumeric());
        healthBox.setResponder(v -> dirty = true);
        addRenderableWidget(healthBox);
        rebuildButtons();
        undoStack.clear();
        undoStack.add(captureState());
    }

    private void styleBox(EditBox box) {
        if (box == null) return;
        try {
            box.setBordered(false);
        } catch (Throwable ignored) {}
        try {
            box.setTextColor(TXT_MAIN);
        } catch (Throwable ignored) {}
        try {
            box.setTextColorUneditable(TXT_DIM);
        } catch (Throwable ignored) {}
    }

    private void recalcBounds() {
        pw = Math.min(740, width - 20);
        ph = Math.min(420, height - 20);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
    }

    private int nameFieldX() {
        return px + 102;
    }

    private int nameFieldY() {
        return py + 70;
    }

    private int healthFieldX() {
        return px + 102;
    }

    private int healthFieldY() {
        return py + 118;
    }

    private void rebuildButtons() {
        buttons.clear();

        int left = px + 18;
        int right = px + pw - 18;
        int mid = px + pw / 2;

        int rx = mid + 8;
        int rw = right - rx;
        int rowH = AnkiConfig.isUiCompactLayout() ? 20 : 22;
        int gap = AnkiConfig.isUiCompactLayout() ? 4 : 6;
        int y = py + 72;

        buttons.add(stateBtn(rx, y, rw, rowH, "ankinbt.entity.flag.no_ai", () -> stNoAi, v -> stNoAi = v));
        y += rowH + gap;
        buttons.add(stateBtn(rx, y, rw, rowH, "ankinbt.entity.flag.invulnerable", () -> stInvulnerable, v -> stInvulnerable = v));
        y += rowH + gap;
        buttons.add(stateBtn(rx, y, rw, rowH, "ankinbt.entity.flag.no_gravity", () -> stNoGravity, v -> stNoGravity = v));
        y += rowH + gap;
        buttons.add(stateBtn(rx, y, rw, rowH, "ankinbt.entity.flag.silent", () -> stSilent, v -> stSilent = v));
        y += rowH + gap;
        buttons.add(stateBtn(rx, y, rw, rowH, "ankinbt.entity.flag.baby", () -> stBaby, v -> stBaby = v));
        y += rowH + gap;

        if (hasVillagerTradeContext()) {
            buttons.add(new UiBtn(rx, y, rw, rowH,
                    () -> Component.translatable("ankinbt.entity.open_villager").getString(),
                    this::openVillagerTradeEditor, true, null, 0));
            y += rowH + gap;
        }

        if (!sourceStack.isEmpty()) {
            buttons.add(new UiBtn(rx, y, rw, rowH,
                    () -> Component.translatable("ankinbt.entity.open_spawn_egg_nbt").getString(),
                    () -> Minecraft.getInstance().setScreen(new NbtEditorScreen(sourceStack)), true, null, 0));
            y += rowH + gap;
        }

        int bottomY = py + ph - 30;
        int areaW = pw - 36;
        int actionBarW = (areaW - 16) / 3;
        buttons.add(new UiBtn(px + 18, bottomY, actionBarW, 20,
                () -> Component.translatable("ankinbt.entity.apply_patch").getString(),
                this::applyPatch, true, null, 1));
        buttons.add(new UiBtn(px + 18 + actionBarW + 8, bottomY, actionBarW, 20,
                () -> Component.translatable("ankinbt.entity.reset_changes").getString(),
                () -> confirmReset = true, true, null, -1));

        buttons.add(new UiBtn(px + 18 + (actionBarW + 8) * 2, bottomY, actionBarW, 20,
                () -> Component.translatable("ankinbt.edit.cancel").getString(),
                this::tryClose, true, null, 0));
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
                Minecraft.getInstance().setScreen(VillagerTradeEditorScreen.forEntity(targetEntity, this));
                return;
            }
        }
        if (SpawnEggEditorHelper.isVillagerSpawnEgg(sourceStack)) {
            Minecraft.getInstance().setScreen(VillagerTradeEditorScreen.forSpawnEgg(sourceStack, inventorySlot, this));
        }
    }

    private UiBtn stateBtn(int x, int y, int w, int h, String key, Supplier<Integer> getter, java.util.function.IntConsumer setter) {
        return new UiBtn(x, y, w, h,
                () -> Component.translatable(key, stateText(getter.get())).getString(),
                () -> {
                    pushUndo();
                    setter.accept(nextState(getter.get()));
                    dirty = true;
                }, true, null, 0);
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
        if (nameBox != null) nameBox.setValue(currentCustomNameInput());
        if (healthBox != null) healthBox.setValue(currentHealthNumeric());
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
        net.minecraft.nbt.ListTag attrs = new net.minecraft.nbt.ListTag();
        CompoundTag attr = new CompoundTag();
        attr.putString("id", "minecraft:generic.max_health");
        attr.putDouble("base", health);
        attrs.add(attr);
        patch.put("attributes", attrs);

        net.minecraft.nbt.ListTag legacy = new net.minecraft.nbt.ListTag();
        CompoundTag legacyAttr = new CompoundTag();
        legacyAttr.putString("Name", "minecraft:generic.max_health");
        legacyAttr.putDouble("Base", health);
        legacy.add(legacyAttr);
        patch.put("Attributes", legacy);
    }

    private void putTriState(CompoundTag patch, String key, int state) {
        if (state == -1) return;
        patch.putBoolean(key, state == 1);
    }

    private void applyPatch() {
        Minecraft mc = Minecraft.getInstance();
        CompoundTag patch = buildPatch();
        String customName = normalizeCustomNameInput(nameBox == null ? "" : nameBox.getValue()).trim();
        Float healthInput = parsePositiveFloat(healthBox == null ? "" : healthBox.getValue());
        Float currentMaxHealth = currentMaxHealth();
        Float healthToApply = resolveHealthForApply(healthInput, currentMaxHealth);
        if (patch.isEmpty()) {
            setStatus(Component.translatable("ankinbt.entity.preview_empty"), TXT_DIM);
            return;
        }

        if (targetEntity != null) {
            if (mc.player == null) return;
            boolean ok = applyPatchToIntegratedServer(mc, customName, healthInput, currentMaxHealth, healthToApply);
            if (!ok && !EditorCommandHelper.canUseEntityCommand(mc)) {
                setStatus(Component.translatable("ankinbt.entity.admin_required"), TXT_ERR);
                return;
            }
            if (!ok) {
                ok = EditorCommandHelper.applyMergeToEntity(mc, targetEntity, patch);
            }
            setStatus(ok ? Component.translatable("ankinbt.entity.applied") : Component.translatable("ankinbt.status.save_error"), ok ? TXT_OK : TXT_ERR);
            if (ok) {
                applyLocalPreview(patch);
                if (healthInput != null && (currentMaxHealth == null || healthInput > currentMaxHealth + 0.01f)) {
                    EditorCommandHelper.setEntityMaxHealth(mc, targetEntity, healthInput);
                    setLocalMaxHealth(targetEntity, healthInput);
                    currentMaxHealth = healthInput;
                }
                if (healthToApply != null) {
                    if (targetEntity instanceof LivingEntity living) {
                        living.setHealth(healthToApply);
                    }
                    healthBox.setValue(String.format(Locale.ROOT, "%.1f", healthToApply));
                }
                dirty = false;
                undoStack.clear();
                undoStack.add(captureState());
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
            return clickConfirm((int) mx, (int) my);
        }
        if (button == 0 && hitHealToggle((int) mx, (int) my)) {
            pushUndo();
            healToFullOnApply = !healToFullOnApply;
            dirty = true;
            return true;
        }
        if (button == 0 && clickHealthAdjuster((int) mx, (int) my)) {
            return true;
        }
        if (button != 0) return false;
        if (handleTextFieldClick(mx, my, button)) {
            return true;
        }
        for (UiBtn btn : buttons) {
            if (btn.click((int) mx, (int) my)) {
                rebuildButtons();
                return true;
            }
        }
        if (nameBox != null) nameBox.setFocused(false);
        if (healthBox != null) healthBox.setFocused(false);
        return false;
    }

    public boolean keyPressed(int key, int scan, int mod) {
        if (nameBox != null && nameBox.isFocused()) {
            if (pressEditBox(nameBox, key, scan, mod)) return true;
        }
        if (healthBox != null && healthBox.isFocused()) {
            if (pressEditBox(healthBox, key, scan, mod)) return true;
        }
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
        if (nameBox != null && nameBox.isFocused()) {
            if (typeEditBox(nameBox, codePoint, modifiers)) return true;
        }
        if (healthBox != null && healthBox.isFocused()) {
            return typeEditBox(healthBox, codePoint, modifiers);
        }
        return false;
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        render(new com.ankinbt.compat.GuiGraphics(g), mx, my, partialTick);
    }

    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        recalcBounds();
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
        g.fill(px + 1, py + 48, px + pw - 1, py + ph - 40, card);

        com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, px + 12, py + 12, TXT_TITLE, false);

        String mode = targetEntity != null
                ? Component.translatable("ankinbt.entity.mode.entity").getString()
                : Component.translatable("ankinbt.entity.mode.spawn_egg").getString();
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, mode, px + 170, py + 13, TXT_DIM, false);

        int left = px + 18;
        int mid = px + pw / 2;
        int nameY = py + 74;
        int typeY = nameY + 16;
        int posY = typeY + 16;
        int healthY = posY + 16;
        int flagY = canEditHealth() ? (healthAdjustBaseY() + 22) : (healthY + 16);

        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.entity.section.current"), left, py + 64, accent, false);
        drawInlineLabel(g, left, nameY, Component.translatable("ankinbt.entity.info.name").getString());
        renderInlineField(g, nameBox, currentName(), mx, my, accent);
        drawInfo(g, left, typeY, Component.translatable("ankinbt.entity.info.type").getString(), currentType());
        drawInfo(g, left, posY, Component.translatable("ankinbt.entity.info.pos").getString(), currentPos());
        drawInlineLabel(g, left, healthY, Component.translatable("ankinbt.entity.info.health").getString());
        if (canEditHealth()) {
            renderInlineHealthField(g, mx, my, accent);
            renderHealthAdjusters(g, mx, my, accent);
        } else {
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, currentHealth(), left + 84, healthY, TXT_MAIN, false);
        }
        drawInfo(g, left, flagY, Component.translatable("ankinbt.entity.info.flags").getString(), currentFlags());

        renderHealToggle(g, accent);

        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.entity.section.actions"), mid + 8, py + 64, accent, false);

        if (AnkiConfig.isEntityLivePreview()) {
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.entity.section.preview"), left, previewSectionY(), accent, false);
            String preview = buildPatch().toString();
            if (preview.length() > 78) preview = preview.substring(0, 75) + "...";
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, preview, left, previewSectionY() + 16, TXT_MAIN, false);
        }

        for (UiBtn btn : buttons) {
            btn.render(g, font, mx, my, accent);
        }

        if (confirmReset) {
            renderConfirm(g, mx, my,
                    Component.translatable("ankinbt.entity.reset_changes").getString(),
                    Component.translatable("ankinbt.confirm.discard_hint").getString(),
                    0xFFEF4444);
        } else if (confirmClose) {
            renderUnsavedConfirmLikeSimple(g, mx, my);
        }

        if (status != null && !status.getString().isEmpty() && System.currentTimeMillis() - statusTime < 2600) {
            int statusY = py + ph - 44;
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, status, left, statusY, statusColor, false);
        }
    }

    private boolean pressEditBox(EditBox box, int key, int scan, int mod) {
        return box != null && box.keyPressed(new KeyEvent(key, scan, mod));
    }
    private boolean typeEditBox(EditBox box, char codePoint, int modifiers) {
        return box != null && box.charTyped(new CharacterEvent(codePoint));
    }    private void drawInfo(GuiGraphics g, int x, int y, String key, String value) {
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, key + ":", x, y, TXT_DIM, false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, value, x + 84, y, TXT_MAIN, false);
    }

    private void drawInlineLabel(GuiGraphics g, int x, int y, String key) {
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, key + ":", x, y, TXT_DIM, false);
    }

    private void renderInlineField(GuiGraphics g, EditBox box, String placeholder, int mx, int my, int accent) {
        if (box == null) return;
        boolean focused = box.isFocused();
        boolean hover = mx >= box.getX() && mx < box.getX() + box.getWidth() && my >= box.getY() && my < box.getY() + box.getHeight();
        String raw = box.getValue();
        boolean placeholderMode = (raw == null || raw.isBlank()) && !focused && placeholder != null && !placeholder.isBlank();
        String shown = placeholderMode ? placeholder : (raw == null ? "" : raw);
        int color = placeholderMode ? TXT_DIM : TXT_MAIN;
        int textY = box.getY() + 2;
        int maxWidth = Math.max(12, box.getWidth() - 4);
        if (font.width(shown) > maxWidth) {
            shown = font.plainSubstrByWidth(shown, maxWidth);
        }
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, shown, box.getX(), textY, color, false);
        int underline = focused ? accent : (hover ? 0xFF415A86 : 0xFF2C3B5C);
        g.fill(box.getX(), box.getY() + box.getHeight() - 1, box.getX() + box.getWidth(), box.getY() + box.getHeight(), underline);
        if (focused && ((System.currentTimeMillis() / 500L) & 1L) == 0L) {
            int cursorX = Math.min(box.getX() + font.width(shown) + 1, box.getX() + box.getWidth() - 1);
            g.fill(cursorX, box.getY() + 1, cursorX + 1, box.getY() + box.getHeight() - 2, TXT_MAIN);
        }
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
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, tail, tailX, healthBox.getY() + 2, TXT_DIM, false);
        }
    }

    private void renderHealthAdjusters(GuiGraphics g, int mx, int my, int accent) {
        if (!canEditHealth() || healthBox == null) return;
        String[] labels = {"-10", "-1", "+1", "+10", "+100"};
        int x = healthAdjustBaseX();
        int y = healthAdjustBaseY();
        int h = 16;
        for (int i = 0; i < labels.length; i++) {
            int w = healthAdjustWidth(labels[i]);
            int bx = healthAdjustButtonX(labels, i);
            boolean hover = mx >= bx && mx < bx + w && my >= y && my < y + h;
            g.fill(bx, y, bx + w, y + h, hover ? 0x4A273752 : 0x32141C2B);
            border(g, bx, y, w, h, hover ? accent : 0xFF2C3B5C);
            String label = labels[i];
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, label, bx + (w - font.width(label)) / 2, y + 4, TXT_MAIN, false);
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
        return healthBox == null ? px + 102 : healthBox.getX();
    }

    private int healthAdjustBaseY() {
        return healthBox == null ? py + 144 : healthBox.getY() + 22;
    }

    private boolean clickHealthAdjuster(int mx, int my) {
        if (!canEditHealth() || healthBox == null) return false;
        int y = healthAdjustBaseY();
        int h = 16;
        String[] labels = {"-10", "-1", "+1", "+10", "+100"};
        float[] deltas = {-10.0f, -1.0f, 1.0f, 10.0f, 100.0f};
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

    private void adjustHealthBy(float delta) {
        Float current = parsePositiveFloat(healthBox == null ? "" : healthBox.getValue());
        if (current == null && targetEntity instanceof LivingEntity living) {
            current = living.getHealth();
        }
        if (current == null) current = 1.0f;
        float next = Math.max(0.0f, current + delta);
        if (healthBox != null) {
            healthBox.setValue(formatEditableHealth(next));
        }
        dirty = true;
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

    private int previewSectionY() {
        int base = healToggleY() + 26;
        return Math.min(base, py + ph - 92);
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
        return px + 18;
    }

    private int healToggleY() {
        if (canEditHealth() && healthBox != null) {
            return healthAdjustBaseY() + 44;
        }
        return py + 168;
    }

    private boolean hitHealToggle(int mx, int my) {
        int x = healToggleX();
        int y = healToggleY();
        int size = 12;
        int textW = font.width(HEAL_FULL_LABEL);
        return mx >= x && mx < x + size + 6 + textW && my >= y && my < y + size;
    }

    private void renderHealToggle(GuiGraphics g, int accent) {
        int x = healToggleX();
        int y = healToggleY();
        int size = 12;
        g.fill(x, y, x + size, y + size, 0x4A1B2638);
        border(g, x, y, size, size, healToFullOnApply ? accent : 0xFF2C3B5C);
        if (healToFullOnApply) g.fill(x + 3, y + 3, x + size - 3, y + size - 3, 0xFF22C55E);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, HEAL_FULL_LABEL, x + size + 6, y + 2, TXT_MAIN, false);
    }

    private boolean handleTextFieldClick(double mx, double my, int button) {
        if (focusInlineBox(nameBox, mx, my, currentName())) {
            if (healthBox != null) healthBox.setFocused(false);
            return true;
        }
        if (nameBox != null) nameBox.setFocused(false);

        if (canEditHealth() && focusInlineBox(healthBox, mx, my, currentHealthNumeric())) {
            if (nameBox != null) nameBox.setFocused(false);
            return true;
        }
        if (healthBox != null) healthBox.setFocused(false);
        return false;
    }

    private boolean focusInlineBox(EditBox box, double mx, double my, String fallback) {
        if (box == null || !hitInlineField(box, mx, my)) return false;
        if ((box.getValue() == null || box.getValue().isBlank()) && fallback != null && !fallback.isBlank()) {
            setInlineBoxValue(box, fallback);
        }
        box.setFocused(true);
        return true;
    }

    private boolean hitInlineField(EditBox box, double mx, double my) {
        if (box == null) return false;
        return mx >= box.getX() - 2 && mx < box.getX() + box.getWidth() + 2
                && my >= box.getY() - 2 && my < box.getY() + box.getHeight() + 2;
    }

    private void setInlineBoxValue(EditBox box, String value) {
        if (box == null) return;
        boolean wasDirty = dirty;
        String next = box == nameBox ? normalizeCustomNameInput(value) : (value == null ? "" : value);
        box.setValue(next);
        dirty = wasDirty;
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

    private boolean applyPatchToIntegratedServer(Minecraft mc, String customName, Float healthInput, Float currentMaxHealth, Float healthToApply) {
        if (mc == null || targetEntity == null) return false;
        var server = mc.getSingleplayerServer();
        if (server == null) return false;

        AtomicBoolean success = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        server.execute(() -> {
            try {
                Entity serverEntity = EditorCommandHelper.findIntegratedServerEntity(server, targetEntity.getId(), targetEntity.getUUID());
                if (serverEntity == null) return;

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
                    if (healthInput != null && (currentMaxHealth == null || healthInput > currentMaxHealth + 0.01f)) {
                        var attr = living.getAttribute(Attributes.MAX_HEALTH);
                        if (attr != null) attr.setBaseValue(healthInput);
                    }
                    if (healthToApply != null) {
                        living.setHealth(Math.max(0.0f, healthToApply));
                    }
                }
                success.set(true);
            } catch (Throwable ignored) {
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(3, TimeUnit.SECONDS)) return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return success.get();
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
        if (keyPressed(event.key(), event.scancode(), 0)) return true;
        return super.keyPressed(event);
    }
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (charTyped((char) event.codepoint(), 0)) return true;
        return super.charTyped(event);
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, isDoubleClick);
    }    static class UiBtn {
        final int x;
        final int y;
        final int w;
        final int h;
        final Supplier<String> label;
        final Runnable action;
        final boolean enabled;
        final Supplier<Boolean> selected;
        final int style;

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
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, x + 6, y + 7, color, false);
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
        if (nameBox != null) nameBox.setValue(s.name == null ? "" : s.name);
        if (healthBox != null) healthBox.setValue(s.health == null ? "" : s.health);
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
        if (dirty) {
            confirmClose = true;
            return;
        }
        Minecraft.getInstance().setScreen(parent);
    }

    private void renderConfirm(GuiGraphics g, int mx, int my, String title, String desc, int color) {
        int w = 320, h = 110;
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        g.fill(0, 0, width, height, 0x88000000);
        g.fill(x, y, x + w, y + h, 0xF0111726);
        border(g, x, y, w, h, 0xFF2C3B5C);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, x + 12, y + 12, color, false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, desc, x + 12, y + 30, TXT_MAIN, false);

        int by = y + h - 30;
        g.fill(x + 12, by, x + 102, by + 20, 0x6A273752);
        border(g, x + 12, by, 90, 20, 0xFF2C3B5C);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.edit.cancel"), x + 20, by + 6, TXT_MAIN, false);

        g.fill(x + w - 102, by, x + w - 12, by + 20, color == 0xFFEF4444 ? 0x8A991B1B : 0x8A166534);
        border(g, x + w - 102, by, 90, 20, color);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.edit.apply"), x + w - 72, by + 6, TXT_MAIN, false);
    }

    private void renderUnsavedConfirmLikeSimple(GuiGraphics g, int mx, int my) {
        int dw = 260, dh = 110;
        int dx = (width - dw) / 2, dy = (height - dh) / 2;
        g.fill(dx, dy, dx + dw, dy + dh, 0xF0080810);
        border(g, dx, dy, dw, dh, SIMPLE_ERROR);

        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.confirm.title"), dx + 10, dy + 10, SIMPLE_C1, false);
        g.fill(dx + 1, dy + 24, dx + dw - 1, dy + 25, SIMPLE_BORDER);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.confirm.unsaved"), dx + 10, dy + 32, SIMPLE_C2, false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.confirm.discard_hint"), dx + 10, dy + 46, SIMPLE_C3, false);

        int by = dy + dh - 32;
        int bw2 = 70, bh2 = 22;

        int saveX = dx + 10;
        boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
        g.fill(saveX, by, saveX + bw2, by + bh2, sh ? 0xFF16A34A : SIMPLE_SUCCESS);
        String saveLabel = Component.translatable("ankinbt.confirm.save_close").getString();
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, saveLabel, saveX + (bw2 - font.width(saveLabel)) / 2, by + 7, SIMPLE_C1, false);

        int discardX = dx + dw / 2 - bw2 / 2;
        boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
        g.fill(discardX, by, discardX + bw2, by + bh2, dh2 ? 0x80EF4444 : 0x40EF4444);
        String discardLabel = Component.translatable("ankinbt.confirm.discard").getString();
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, discardLabel, discardX + (bw2 - font.width(discardLabel)) / 2, by + 7, SIMPLE_C1, false);

        int cancelX = dx + dw - bw2 - 10;
        boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
        g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? SIMPLE_BTN_HOVER : SIMPLE_BTN_BG);
        String cancelLabel = Component.translatable("ankinbt.edit.cancel").getString();
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, cancelLabel, cancelX + (bw2 - font.width(cancelLabel)) / 2, by + 7, SIMPLE_C2, false);
    }

    private boolean clickConfirm(int mx, int my) {
        if (confirmClose) return clickUnsavedConfirmLikeSimple(mx, my);

        int w = 320, h = 110;
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        int by = y + h - 30;
        if (mx >= x + 12 && mx < x + 102 && my >= by && my < by + 20) {
            confirmClose = false;
            confirmReset = false;
            return true;
        }
        if (mx >= x + w - 102 && mx < x + w - 12 && my >= by && my < by + 20) {
            if (confirmReset) {
                confirmReset = false;
                resetStates();
            } else if (confirmClose) {
                confirmClose = false;
                dirty = false;
                Minecraft.getInstance().setScreen(parent);
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
            applyPatch();
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
}


