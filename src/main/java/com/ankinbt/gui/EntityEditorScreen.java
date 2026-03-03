package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.editor.SpawnEggEditorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

public class EntityEditorScreen extends Screen {

    private static final int TXT_TITLE = 0xFFF3F6FF;
    private static final int TXT_MAIN = 0xFFD9E2F2;
    private static final int TXT_DIM = 0xFF8EA3C7;
    private static final int TXT_OK = 0xFF34D399;
    private static final int TXT_ERR = 0xFFEF4444;
    private static final Component HEAL_FULL_LABEL = Component.literal("补满血量");

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
        int left = px + 18;
        healthBox = new EditBox(font, left + 2, py + 178, 138, 20, Component.empty());
        healthBox.setValue(currentHealthNumeric());
        healthBox.setResponder(v -> dirty = true);
        addRenderableWidget(healthBox);
        rebuildButtons();
        undoStack.clear();
        undoStack.add(captureState());
    }

    private void recalcBounds() {
        pw = Math.min(740, width - 20);
        ph = Math.min(420, height - 20);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
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

        Float healthInput = parsePositiveFloat(healthBox == null ? "" : healthBox.getValue());
        Float currentMaxHealth = targetEntity == null ? null : invokeFloat(targetEntity, "getMaxHealth");
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
        if (patch.isEmpty()) {
            setStatus(Component.translatable("ankinbt.entity.preview_empty"), TXT_DIM);
            return;
        }

        if (targetEntity != null) {
            if (mc.player == null) return;
            if (!EditorCommandHelper.canUseEntityCommand(mc)) {
                setStatus(Component.translatable("ankinbt.entity.admin_required"), TXT_ERR);
                return;
            }
            boolean ok = EditorCommandHelper.applyMergeToEntity(mc, targetEntity, patch);
            setStatus(ok ? Component.translatable("ankinbt.entity.applied") : Component.translatable("ankinbt.status.save_error"), ok ? TXT_OK : TXT_ERR);
            if (ok) {
                applyLocalPreview(patch);
                Float healthInput = parsePositiveFloat(healthBox == null ? "" : healthBox.getValue());
                Float currentMaxHealth = invokeFloat(targetEntity, "getMaxHealth");
                if (healthInput != null && (currentMaxHealth == null || healthInput > currentMaxHealth + 0.01f)) {
                    EditorCommandHelper.setEntityMaxHealth(mc, targetEntity, healthInput);
                    setLocalMaxHealth(targetEntity, healthInput);
                    currentMaxHealth = healthInput;
                }
                Float healthToApply = resolveHealthForApply(healthInput, currentMaxHealth);
                if (healthToApply != null) {
                    try {
                        targetEntity.getClass().getMethod("setHealth", float.class).invoke(targetEntity, healthToApply);
                    } catch (Throwable ignored) {}
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
        if (button != 0) return false;
        for (UiBtn btn : buttons) {
            if (btn.click((int) mx, (int) my)) {
                rebuildButtons();
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(int key, int scan, int mod) {
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

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        recalcBounds();
        openAnim = UiTheme.approach(openAnim, 1.0f, 0.14f);

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

        g.drawString(font, title, px + 12, py + 12, TXT_TITLE, false);

        String mode = targetEntity != null
                ? Component.translatable("ankinbt.entity.mode.entity").getString()
                : Component.translatable("ankinbt.entity.mode.spawn_egg").getString();
        g.drawString(font, mode, px + 170, py + 13, TXT_DIM, false);

        int left = px + 18;
        int mid = px + pw / 2;
        int y = py + 74;

        g.drawString(font, Component.translatable("ankinbt.entity.section.current"), left, py + 64, accent, false);
        drawInfo(g, left, y, Component.translatable("ankinbt.entity.info.name").getString(), currentName());
        y += 16;
        drawInfo(g, left, y, Component.translatable("ankinbt.entity.info.type").getString(), currentType());
        y += 16;
        drawInfo(g, left, y, Component.translatable("ankinbt.entity.info.pos").getString(), currentPos());
        y += 16;
        drawInfo(g, left, y, Component.translatable("ankinbt.entity.info.health").getString(), currentHealth());
        y += 16;
        drawInfo(g, left, y, Component.translatable("ankinbt.entity.info.flags").getString(), currentFlags());

        g.drawString(font, Component.translatable("ankinbt.entity.edit.health"), left, py + 166, TXT_DIM, false);
        if (healthBox != null) healthBox.render(g, mx, my, partialTick);
        renderHealToggle(g, accent);

        g.drawString(font, Component.translatable("ankinbt.entity.section.actions"), mid + 8, py + 64, accent, false);

        if (AnkiConfig.isEntityLivePreview()) {
            g.drawString(font, Component.translatable("ankinbt.entity.section.preview"), left, py + 206, accent, false);
            String preview = buildPatch().toString();
            if (preview.length() > 78) preview = preview.substring(0, 75) + "...";
            g.drawString(font, preview, left, py + 222, TXT_MAIN, false);
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
            renderConfirm(g, mx, my,
                    Component.translatable("ankinbt.confirm.title").getString(),
                    Component.translatable("ankinbt.confirm.unsaved").getString(),
                    0xFFF59E0B);
        }

        if (status != null && !status.getString().isEmpty() && System.currentTimeMillis() - statusTime < 2600) {
            g.drawString(font, status, left, py + ph - 12, statusColor, false);
        }
    }

    private void drawInfo(GuiGraphics g, int x, int y, String key, String value) {
        g.drawString(font, key + ":", x, y, TXT_DIM, false);
        g.drawString(font, value, x + 84, y, TXT_MAIN, false);
    }

    private String currentName() {
        if (targetEntity != null) return targetEntity.getDisplayName().getString();
        if (!sourceStack.isEmpty()) return sourceStack.getHoverName().getString();
        return "-";
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
        if (targetEntity == null) return "-";
        Float hp = invokeFloat(targetEntity, "getHealth");
        Float max = invokeFloat(targetEntity, "getMaxHealth");
        if (hp == null) return "-";
        if (max != null) return String.format(Locale.ROOT, "%.1f / %.1f", hp, max);
        return String.format(Locale.ROOT, "%.1f", hp);
    }

    private String currentHealthNumeric() {
        if (targetEntity == null) return "";
        Float hp = invokeFloat(targetEntity, "getHealth");
        if (hp == null) return "";
        return String.format(Locale.ROOT, "%.1f", hp);
    }

    private String currentFlags() {
        if (targetEntity == null) return "-";
        boolean inv = invokeBool(targetEntity, "isInvulnerable", false);
        boolean ng = invokeBool(targetEntity, "isNoGravity", false);
        boolean sl = invokeBool(targetEntity, "isSilent", false);
        return "Inv=" + inv + ", G=" + (!ng) + ", S=" + sl;
    }

    private void applyLocalPreview(CompoundTag patch) {
        if (targetEntity == null || patch == null) return;
        applyLocalBool(patch, "Invulnerable", "setInvulnerable");
        applyLocalBool(patch, "NoGravity", "setNoGravity");
        applyLocalBool(patch, "Silent", "setSilent");
        applyLocalBool(patch, "NoAI", "setNoAi");
        applyLocalBool(patch, "IsBaby", "setBaby");
        if (patch.contains("Age")) {
            int age = readIntTag(patch, "Age", 0);
            try {
                targetEntity.getClass().getMethod("setAge", int.class).invoke(targetEntity, age);
            } catch (Throwable ignored) {}
        }
    }

    private void applyLocalBool(CompoundTag patch, String key, String method) {
        if (!patch.contains(key)) return;
        boolean value = readBoolTag(patch, key, false);
        try {
            targetEntity.getClass().getMethod(method, boolean.class).invoke(targetEntity, value);
        } catch (Throwable ignored) {}
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

    private Float invokeFloat(Object target, String method) {
        try {
            Object out = target.getClass().getMethod(method).invoke(target);
            if (out instanceof Number n) return n.floatValue();
        } catch (Throwable ignored) {}
        return null;
    }

    private boolean invokeBool(Object target, String method, boolean def) {
        try {
            Object out = target.getClass().getMethod(method).invoke(target);
            if (out instanceof Boolean b) return b;
        } catch (Throwable ignored) {}
        return def;
    }

    private void setLocalMaxHealth(Object entity, float value) {
        if (entity == null || value <= 0.0f) return;
        try {
            Object holder = entity.getClass().getMethod("getAttributes").invoke(entity);
            if (holder == null) return;
            Object attr = findMaxHealthAttribute(entity);
            if (attr == null) return;
            Object instance = holder.getClass().getMethod("getInstance", attr.getClass()).invoke(holder, attr);
            if (instance != null) {
                instance.getClass().getMethod("setBaseValue", double.class).invoke(instance, (double) value);
                return;
            }
        } catch (Throwable ignored) {}
        try {
            entity.getClass().getMethod("setMaxHealth", float.class).invoke(entity, value);
        } catch (Throwable ignored) {}
    }

    private Object findMaxHealthAttribute(Object entity) {
        try {
            Class<?> attrs = Class.forName("net.minecraft.world.entity.ai.attributes.Attributes");
            try {
                return attrs.getField("MAX_HEALTH").get(null);
            } catch (Throwable ignored) {}
            for (String name : new String[]{"MAX_HEALTH", "f_22265_"}) {
                try {
                    return attrs.getDeclaredField(name).get(null);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private int healToggleX() {
        return px + 18 + 148;
    }

    private int healToggleY() {
        return py + 182;
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
        g.drawString(font, HEAL_FULL_LABEL, x + size + 6, y + 2, TXT_MAIN, false);
    }

    @Override
    public void onClose() {
        tryClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
            g.drawString(font, text, x + 6, y + 7, color, false);
        }
    }

    private record StateSnapshot(int stNoAi, int stInvulnerable, int stNoGravity, int stSilent, int stBaby, boolean healFull, String health) {}

    private StateSnapshot captureState() {
        return new StateSnapshot(stNoAi, stInvulnerable, stNoGravity, stSilent, stBaby, healToFullOnApply, healthBox == null ? "" : healthBox.getValue());
    }

    private void applyState(StateSnapshot s) {
        if (s == null) return;
        stNoAi = s.stNoAi;
        stInvulnerable = s.stInvulnerable;
        stNoGravity = s.stNoGravity;
        stSilent = s.stSilent;
        stBaby = s.stBaby;
        healToFullOnApply = s.healFull;
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
        g.drawString(font, title, x + 12, y + 12, color, false);
        g.drawString(font, desc, x + 12, y + 30, TXT_MAIN, false);

        int by = y + h - 30;
        g.fill(x + 12, by, x + 102, by + 20, 0x6A273752);
        border(g, x + 12, by, 90, 20, 0xFF2C3B5C);
        g.drawString(font, Component.translatable("ankinbt.edit.cancel"), x + 20, by + 6, TXT_MAIN, false);

        g.fill(x + w - 102, by, x + w - 12, by + 20, color == 0xFFEF4444 ? 0x8A991B1B : 0x8A166534);
        border(g, x + w - 102, by, 90, 20, color);
        g.drawString(font, Component.translatable("ankinbt.edit.apply"), x + w - 72, by + 6, TXT_MAIN, false);
    }

    private boolean clickConfirm(int mx, int my) {
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
}
