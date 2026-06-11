/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.server.IntegratedServer
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.AgeableMob
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.gui.VillagerTradeEditorScreen;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class EntityEditorScreen
extends Screen {
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
    private static final int SIMPLE_ERROR = -1096636;
    private static final Component HEAL_FULL_LABEL = Component.translatable((String)"ankinbt.entity.heal_full");
    private final Entity targetEntity;
    private final ItemStack sourceStack;
    private final int inventorySlot;
    private final Screen parent;
    private final List<UiBtn> buttons = new ArrayList<UiBtn>();
    private int stNoAi = -1;
    private int stInvulnerable = -1;
    private int stNoGravity = -1;
    private int stSilent = -1;
    private int stBaby = -1;
    private boolean healToFullOnApply = false;
    private EditBox nameBox;
    private EditBox healthBox;
    private Component status = Component.empty();
    private int statusColor = -7429177;
    private long statusTime = 0L;
    private boolean dirty = false;
    private boolean confirmClose = false;
    private boolean confirmReset = false;
    private final List<StateSnapshot> undoStack = new ArrayList<StateSnapshot>();
    private int px;
    private int py;
    private int pw;
    private int ph;
    private float openAnim = 0.0f;
    private static final int MAX_UNDO = 40;

    private EntityEditorScreen(Entity targetEntity, ItemStack sourceStack, int inventorySlot, Screen parent) {
        super((Component)Component.translatable((String)"ankinbt.entity.title"));
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

    protected void init() {
        this.recalcBounds();
        this.nameBox = new EditBox(this.font, this.nameFieldX(), this.nameFieldY(), 192, 16, (Component)Component.empty());
        this.styleBox(this.nameBox);
        this.nameBox.setValue(this.currentCustomNameInput());
        this.nameBox.setResponder(v -> {
            this.dirty = true;
        });
        this.addRenderableWidget(this.nameBox);
        this.healthBox = new EditBox(this.font, this.healthFieldX(), this.healthFieldY(), 88, 16, (Component)Component.empty());
        this.styleBox(this.healthBox);
        this.healthBox.setValue(this.currentHealthNumeric());
        this.healthBox.setResponder(v -> {
            this.dirty = true;
        });
        this.addRenderableWidget(this.healthBox);
        this.rebuildButtons();
        this.undoStack.clear();
        this.undoStack.add(this.captureState());
    }

    private void styleBox(EditBox box) {
        if (box == null) {
            return;
        }
        try {
            box.setBordered(false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            box.setTextColor(-2497806);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            box.setTextColorUneditable(-7429177);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void recalcBounds() {
        this.pw = Math.min(740, this.width - 20);
        this.ph = Math.min(420, this.height - 20);
        this.px = (this.width - this.pw) / 2;
        this.py = (this.height - this.ph) / 2;
    }

    private int nameFieldX() {
        return this.px + 102;
    }

    private int nameFieldY() {
        return this.py + 70;
    }

    private int healthFieldX() {
        return this.px + 102;
    }

    private int healthFieldY() {
        return this.py + 118;
    }

    private void rebuildButtons() {
        this.buttons.clear();
        int left = this.px + 18;
        int right = this.px + this.pw - 18;
        int mid = this.px + this.pw / 2;
        int rx = mid + 8;
        int rw = right - rx;
        int rowH = AnkiConfig.isUiCompactLayout() ? 20 : 22;
        int gap = AnkiConfig.isUiCompactLayout() ? 4 : 6;
        int y = this.py + 72;
        this.buttons.add(this.stateBtn(rx, y, rw, rowH, "ankinbt.entity.flag.no_ai", () -> this.stNoAi, v -> {
            this.stNoAi = v;
        }));
        this.buttons.add(this.stateBtn(rx, y += rowH + gap, rw, rowH, "ankinbt.entity.flag.invulnerable", () -> this.stInvulnerable, v -> {
            this.stInvulnerable = v;
        }));
        this.buttons.add(this.stateBtn(rx, y += rowH + gap, rw, rowH, "ankinbt.entity.flag.no_gravity", () -> this.stNoGravity, v -> {
            this.stNoGravity = v;
        }));
        this.buttons.add(this.stateBtn(rx, y += rowH + gap, rw, rowH, "ankinbt.entity.flag.silent", () -> this.stSilent, v -> {
            this.stSilent = v;
        }));
        this.buttons.add(this.stateBtn(rx, y += rowH + gap, rw, rowH, "ankinbt.entity.flag.baby", () -> this.stBaby, v -> {
            this.stBaby = v;
        }));
        y += rowH + gap;
        if (this.hasVillagerTradeContext()) {
            this.buttons.add(new UiBtn(rx, y, rw, rowH, () -> Component.translatable((String)"ankinbt.entity.open_villager").getString(), this::openVillagerTradeEditor, true, null, 0));
            y += rowH + gap;
        }
        if (!this.sourceStack.isEmpty()) {
            this.buttons.add(new UiBtn(rx, y, rw, rowH, () -> Component.translatable((String)"ankinbt.entity.open_spawn_egg_nbt").getString(), () -> Minecraft.getInstance().setScreen((Screen)new NbtEditorScreen(this.sourceStack)), true, null, 0));
            y += rowH + gap;
        }
        int bottomY = this.py + this.ph - 30;
        int areaW = this.pw - 36;
        int actionBarW = (areaW - 16) / 3;
        this.buttons.add(new UiBtn(this.px + 18, bottomY, actionBarW, 20, () -> Component.translatable((String)"ankinbt.entity.apply_patch").getString(), this::applyPatch, true, null, 1));
        this.buttons.add(new UiBtn(this.px + 18 + actionBarW + 8, bottomY, actionBarW, 20, () -> Component.translatable((String)"ankinbt.entity.reset_changes").getString(), () -> {
            this.confirmReset = true;
        }, true, null, -1));
        this.buttons.add(new UiBtn(this.px + 18 + (actionBarW + 8) * 2, bottomY, actionBarW, 20, () -> Component.translatable((String)"ankinbt.edit.cancel").getString(), this::tryClose, true, null, 0));
    }

    private boolean hasVillagerTradeContext() {
        String type;
        if (this.targetEntity != null && ((type = this.targetEntity.getType().toString().toLowerCase(Locale.ROOT)).contains("villager") || type.contains("wandering_trader"))) {
            return true;
        }
        return !this.sourceStack.isEmpty() && SpawnEggEditorHelper.isVillagerSpawnEgg(this.sourceStack);
    }

    private void openVillagerTradeEditor() {
        String type;
        if (this.targetEntity != null && ((type = this.targetEntity.getType().toString().toLowerCase(Locale.ROOT)).contains("villager") || type.contains("wandering_trader"))) {
            Minecraft.getInstance().setScreen((Screen)VillagerTradeEditorScreen.forEntity(this.targetEntity, this));
            return;
        }
        if (SpawnEggEditorHelper.isVillagerSpawnEgg(this.sourceStack)) {
            Minecraft.getInstance().setScreen((Screen)VillagerTradeEditorScreen.forSpawnEgg(this.sourceStack, this.inventorySlot, this));
        }
    }

    private UiBtn stateBtn(int x, int y, int w, int h, String key, Supplier<Integer> getter, IntConsumer setter) {
        return new UiBtn(x, y, w, h, () -> Component.translatable((String)key, (Object[])new Object[]{this.stateText((Integer)getter.get())}).getString(), () -> {
            this.pushUndo();
            setter.accept(this.nextState((Integer)getter.get()));
            this.dirty = true;
        }, true, null, 0);
    }

    private int nextState(int s) {
        if (s < 0) {
            return 1;
        }
        if (s > 0) {
            return 0;
        }
        return -1;
    }

    private String stateText(int s) {
        if (s < 0) {
            return Component.translatable((String)"ankinbt.entity.state.keep").getString();
        }
        return s > 0 ? Component.translatable((String)"ankinbt.simple.on").getString() : Component.translatable((String)"ankinbt.simple.off").getString();
    }

    private void resetStates() {
        this.pushUndo();
        this.stNoAi = -1;
        this.stInvulnerable = -1;
        this.stNoGravity = -1;
        this.stSilent = -1;
        this.stBaby = -1;
        this.healToFullOnApply = false;
        if (this.nameBox != null) {
            this.nameBox.setValue(this.currentCustomNameInput());
        }
        if (this.healthBox != null) {
            this.healthBox.setValue(this.currentHealthNumeric());
        }
        this.dirty = false;
        this.setStatus((Component)Component.translatable((String)"ankinbt.entity.reset_done"), -13315175);
    }

    private CompoundTag buildPatch() {
        String id;
        Float healthToApply;
        String customName;
        CompoundTag patch = new CompoundTag();
        this.putTriState(patch, "NoAI", this.stNoAi);
        this.putTriState(patch, "Invulnerable", this.stInvulnerable);
        this.putTriState(patch, "NoGravity", this.stNoGravity);
        this.putTriState(patch, "Silent", this.stSilent);
        this.putTriState(patch, "IsBaby", this.stBaby);
        if (this.stBaby == 1) {
            patch.putInt("Age", -24000);
        }
        if (this.stBaby == 0) {
            patch.putInt("Age", 0);
        }
        if (!Objects.equals(customName = this.normalizeCustomNameInput(this.nameBox == null ? "" : this.nameBox.getValue()).trim(), this.normalizeCustomNameInput(this.currentCustomNameInput()).trim())) {
            patch.putString("CustomName", this.toCustomNameJson(customName));
            patch.putBoolean("CustomNameVisible", !customName.isBlank());
        }
        Float healthInput = this.parsePositiveFloat(this.healthBox == null ? "" : this.healthBox.getValue());
        Float currentMaxHealth = this.currentMaxHealth();
        if (healthInput != null && (this.targetEntity == null || currentMaxHealth == null || healthInput.floatValue() > currentMaxHealth.floatValue() + 0.01f)) {
            this.putMaxHealthPatch(patch, healthInput.floatValue());
        }
        if ((healthToApply = this.resolveHealthForApply(healthInput, currentMaxHealth)) != null) {
            patch.putFloat("Health", healthToApply.floatValue());
        }
        if (this.stInvulnerable == 1) {
            patch.putInt("NoDamageTicks", Short.MAX_VALUE);
        }
        if (this.stInvulnerable == 0) {
            patch.putInt("NoDamageTicks", 0);
        }
        if (this.targetEntity == null && SpawnEggEditorHelper.isSpawnEgg(this.sourceStack) && !patch.contains("id") && !(id = SpawnEggEditorHelper.inferEntityIdFromSpawnEgg(this.sourceStack)).isBlank()) {
            patch.putString("id", id);
        }
        return patch;
    }

    private void putMaxHealthPatch(CompoundTag patch, float health) {
        ListTag attrs = new ListTag();
        CompoundTag attr = new CompoundTag();
        attr.putString("id", "minecraft:generic.max_health");
        attr.putDouble("base", (double)health);
        attrs.add(attr);
        patch.put("attributes", (Tag)attrs);
        ListTag legacy = new ListTag();
        CompoundTag legacyAttr = new CompoundTag();
        legacyAttr.putString("Name", "minecraft:generic.max_health");
        legacyAttr.putDouble("Base", (double)health);
        legacy.add(legacyAttr);
        patch.put("Attributes", (Tag)legacy);
    }

    private void putTriState(CompoundTag patch, String key, int state) {
        if (state == -1) {
            return;
        }
        patch.putBoolean(key, state == 1);
    }

    private void applyPatch() {
        Minecraft mc = Minecraft.getInstance();
        CompoundTag patch = this.buildPatch();
        String customName = this.normalizeCustomNameInput(this.nameBox == null ? "" : this.nameBox.getValue()).trim();
        Float healthInput = this.parsePositiveFloat(this.healthBox == null ? "" : this.healthBox.getValue());
        Float currentMaxHealth = this.currentMaxHealth();
        Float healthToApply = this.resolveHealthForApply(healthInput, currentMaxHealth);
        if (patch.isEmpty()) {
            this.setStatus((Component)Component.translatable((String)"ankinbt.entity.preview_empty"), -7429177);
            return;
        }
        if (this.targetEntity != null) {
            if (mc.player == null) {
                return;
            }
            boolean ok = this.applyPatchToIntegratedServer(mc, customName, healthInput, currentMaxHealth, healthToApply);
            if (!ok && !EditorCommandHelper.canUseEntityCommand(mc)) {
                this.setStatus((Component)Component.translatable((String)"ankinbt.entity.admin_required"), -1096636);
                return;
            }
            if (!ok) {
                ok = EditorCommandHelper.applyMergeToEntity(mc, this.targetEntity, patch);
            }
            this.setStatus((Component)(ok ? Component.translatable((String)"ankinbt.entity.applied") : Component.translatable((String)"ankinbt.status.save_error")), ok ? -13315175 : -1096636);
            if (ok) {
                this.applyLocalPreview(patch);
                if (healthInput != null && (currentMaxHealth == null || healthInput.floatValue() > currentMaxHealth.floatValue() + 0.01f)) {
                    EditorCommandHelper.setEntityMaxHealth(mc, this.targetEntity, healthInput.floatValue());
                    this.setLocalMaxHealth(this.targetEntity, healthInput.floatValue());
                    currentMaxHealth = healthInput;
                }
                if (healthToApply != null) {
                    Entity entity = this.targetEntity;
                    if (entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity)entity;
                        living.setHealth(healthToApply.floatValue());
                    }
                    this.healthBox.setValue(String.format(Locale.ROOT, "%.1f", healthToApply));
                }
                this.dirty = false;
                this.undoStack.clear();
                this.undoStack.add(this.captureState());
            }
            return;
        }
        if (!SpawnEggEditorHelper.isSpawnEgg(this.sourceStack)) {
            this.setStatus((Component)Component.translatable((String)"ankinbt.entity.spawn_egg_required"), -1096636);
            return;
        }
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

    private void setStatus(Component message, int color) {
        this.status = message;
        this.statusColor = color;
        this.statusTime = System.currentTimeMillis();
    }

    private Float parsePositiveFloat(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        try {
            float v = Float.parseFloat(t);
            return v >= 0.0f ? Float.valueOf(v) : null;
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Float resolveHealthForApply(Float healthInput, Float currentMaxHealth) {
        if (!this.healToFullOnApply) {
            return healthInput;
        }
        if (healthInput != null) {
            if (currentMaxHealth != null && healthInput.floatValue() < currentMaxHealth.floatValue()) {
                return currentMaxHealth;
            }
            return healthInput;
        }
        return currentMaxHealth;
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (this.confirmClose || this.confirmReset) {
            return this.clickConfirm((int)mx, (int)my);
        }
        if (button == 0 && this.hitHealToggle((int)mx, (int)my)) {
            this.pushUndo();
            this.healToFullOnApply = !this.healToFullOnApply;
            this.dirty = true;
            return true;
        }
        if (button == 0 && this.clickHealthAdjuster((int)mx, (int)my)) {
            return true;
        }
        if (button != 0) {
            return false;
        }
        if (this.handleTextFieldClick(mx, my, button)) {
            return true;
        }
        for (UiBtn btn : this.buttons) {
            if (!btn.click((int)mx, (int)my)) continue;
            this.rebuildButtons();
            return true;
        }
        if (this.nameBox != null) {
            this.nameBox.setFocused(false);
        }
        if (this.healthBox != null) {
            this.healthBox.setFocused(false);
        }
        return false;
    }

    public boolean keyPressed(int key, int scan, int mod) {
        boolean ctrl;
        if (this.nameBox != null && this.nameBox.isFocused() && this.pressEditBox(this.nameBox, key, scan, mod)) {
            return true;
        }
        if (this.healthBox != null && this.healthBox.isFocused() && this.pressEditBox(this.healthBox, key, scan, mod)) {
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
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (this.nameBox != null && this.nameBox.isFocused() && this.typeEditBox(this.nameBox, codePoint, modifiers)) {
            return true;
        }
        if (this.healthBox != null && this.healthBox.isFocused()) {
            return this.typeEditBox(this.healthBox, codePoint, modifiers);
        }
        return false;
    }

    public void render(GuiGraphics g, int mx, int my, float partialTick) {
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
        g.fill(this.px + 1, this.py + 48, this.px + this.pw - 1, this.py + this.ph - 40, card);
        VersionCompat.get().drawString(g, this.font, this.title, this.px + 12, this.py + 12, -788737, false);
        String mode = this.targetEntity != null ? Component.translatable((String)"ankinbt.entity.mode.entity").getString() : Component.translatable((String)"ankinbt.entity.mode.spawn_egg").getString();
        VersionCompat.get().drawString(g, this.font, mode, this.px + 170, this.py + 13, -7429177, false);
        int left = this.px + 18;
        int mid = this.px + this.pw / 2;
        int nameY = this.py + 74;
        int typeY = nameY + 16;
        int posY = typeY + 16;
        int healthY = posY + 16;
        int flagY = this.canEditHealth() ? this.healthAdjustBaseY() + 22 : healthY + 16;
        VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.entity.section.current"), left, this.py + 64, accent, false);
        this.drawInlineLabel(g, left, nameY, Component.translatable((String)"ankinbt.entity.info.name").getString());
        this.renderInlineField(g, this.nameBox, this.currentName(), mx, my, accent);
        this.drawInfo(g, left, typeY, Component.translatable((String)"ankinbt.entity.info.type").getString(), this.currentType());
        this.drawInfo(g, left, posY, Component.translatable((String)"ankinbt.entity.info.pos").getString(), this.currentPos());
        this.drawInlineLabel(g, left, healthY, Component.translatable((String)"ankinbt.entity.info.health").getString());
        if (this.canEditHealth()) {
            this.renderInlineHealthField(g, mx, my, accent);
            this.renderHealthAdjusters(g, mx, my, accent);
        } else {
            VersionCompat.get().drawString(g, this.font, this.currentHealth(), left + 84, healthY, -2497806, false);
        }
        this.drawInfo(g, left, flagY, Component.translatable((String)"ankinbt.entity.info.flags").getString(), this.currentFlags());
        this.renderHealToggle(g, accent);
        VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.entity.section.actions"), mid + 8, this.py + 64, accent, false);
        if (AnkiConfig.isEntityLivePreview()) {
            VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.entity.section.preview"), left, this.previewSectionY(), accent, false);
            Object preview = this.buildPatch().toString();
            if (((String)preview).length() > 78) {
                preview = ((String)preview).substring(0, 75) + "...";
            }
            VersionCompat.get().drawString(g, this.font, (String)preview, left, this.previewSectionY() + 16, -2497806, false);
        }
        for (UiBtn btn : this.buttons) {
            btn.render(g, this.font, mx, my, accent);
        }
        if (this.confirmReset) {
            this.renderConfirm(g, mx, my, Component.translatable((String)"ankinbt.entity.reset_changes").getString(), Component.translatable((String)"ankinbt.confirm.discard_hint").getString(), -1096636);
        } else if (this.confirmClose) {
            this.renderUnsavedConfirmLikeSimple(g, mx, my);
        }
        if (this.status != null && !this.status.getString().isEmpty() && System.currentTimeMillis() - this.statusTime < 2600L) {
            int statusY = this.py + this.ph - 44;
            VersionCompat.get().drawString(g, this.font, this.status, left, statusY, this.statusColor, false);
        }
    }

    private boolean pressEditBox(EditBox box, int key, int scan, int mod) {
        return box != null && box.keyPressed(key, scan, mod);
    }

    private boolean typeEditBox(EditBox box, char codePoint, int modifiers) {
        return box != null && box.charTyped(codePoint, modifiers);
    }

    private void drawInfo(GuiGraphics g, int x, int y, String key, String value) {
        VersionCompat.get().drawString(g, this.font, key + ":", x, y, -7429177, false);
        VersionCompat.get().drawString(g, this.font, value, x + 84, y, -2497806, false);
    }

    private void drawInlineLabel(GuiGraphics g, int x, int y, String key) {
        VersionCompat.get().drawString(g, this.font, key + ":", x, y, -7429177, false);
    }

    private void renderInlineField(GuiGraphics g, EditBox box, String placeholder, int mx, int my, int accent) {
        boolean placeholderMode;
        if (box == null) {
            return;
        }
        boolean focused = box.isFocused();
        boolean hover = mx >= box.getX() && mx < box.getX() + box.getWidth() && my >= box.getY() && my < box.getY() + box.getHeight();
        String raw = box.getValue();
        boolean bl = placeholderMode = (raw == null || raw.isBlank()) && !focused && placeholder != null && !placeholder.isBlank();
        String shown = placeholderMode ? placeholder : (raw == null ? "" : raw);
        int color = placeholderMode ? -7429177 : -2497806;
        int textY = box.getY() + 2;
        int maxWidth = Math.max(12, box.getWidth() - 4);
        if (this.font.width(shown) > maxWidth) {
            shown = this.font.plainSubstrByWidth(shown, maxWidth);
        }
        VersionCompat.get().drawString(g, this.font, shown, box.getX(), textY, color, false);
        int underline = focused ? accent : (hover ? -12494202 : -13878436);
        g.fill(box.getX(), box.getY() + box.getHeight() - 1, box.getX() + box.getWidth(), box.getY() + box.getHeight(), underline);
        if (focused && (System.currentTimeMillis() / 500L & 1L) == 0L) {
            int cursorX = Math.min(box.getX() + this.font.width(shown) + 1, box.getX() + box.getWidth() - 1);
            g.fill(cursorX, box.getY() + 1, cursorX + 1, box.getY() + box.getHeight() - 2, -2497806);
        }
    }

    private void renderInlineHealthField(GuiGraphics g, int mx, int my, int accent) {
        if (this.healthBox == null) {
            return;
        }
        String raw = this.healthBox.getValue();
        if (raw == null || raw.isBlank()) {
            raw = this.currentHealthNumeric();
        }
        String shown = raw == null ? "" : raw;
        this.renderInlineField(g, this.healthBox, shown, mx, my, accent);
        Float max = this.currentMaxHealth();
        if (max != null) {
            String tail = " / " + String.format(Locale.ROOT, "%.1f", max);
            int tailX = Math.min(this.healthBox.getX() + Math.min(this.font.width(shown), this.healthBox.getWidth() - 4) + 8, this.healthBox.getX() + this.healthBox.getWidth() + 12);
            VersionCompat.get().drawString(g, this.font, tail, tailX, this.healthBox.getY() + 2, -7429177, false);
        }
    }

    private void renderHealthAdjusters(GuiGraphics g, int mx, int my, int accent) {
        if (!this.canEditHealth() || this.healthBox == null) {
            return;
        }
        String[] labels = new String[]{"-10", "-1", "+1", "+10", "+100"};
        int x = this.healthAdjustBaseX();
        int y = this.healthAdjustBaseY();
        int h = 16;
        for (int i = 0; i < labels.length; ++i) {
            int w = this.healthAdjustWidth(labels[i]);
            int bx = this.healthAdjustButtonX(labels, i);
            boolean hover = mx >= bx && mx < bx + w && my >= y && my < y + h;
            g.fill(bx, y, bx + w, y + h, hover ? 1244084050 : 840178731);
            this.border(g, bx, y, w, h, hover ? accent : -13878436);
            String label = labels[i];
            VersionCompat.get().drawString(g, this.font, label, bx + (w - this.font.width(label)) / 2, y + 4, -2497806, false);
        }
    }

    private String currentName() {
        if (this.targetEntity != null) {
            String custom = this.currentCustomNameInput();
            if (!custom.isBlank()) {
                return custom;
            }
            return this.normalizeCustomNameInput(this.targetEntity.getDisplayName().getString());
        }
        if (!this.sourceStack.isEmpty()) {
            return this.sourceStack.getHoverName().getString();
        }
        return "-";
    }

    private String currentCustomNameInput() {
        if (this.targetEntity != null) {
            Component custom = this.targetEntity.getCustomName();
            return custom == null ? "" : this.normalizeCustomNameInput(custom.getString());
        }
        return "";
    }

    private String currentType() {
        if (this.targetEntity != null) {
            return this.targetEntity.getType().toString().toLowerCase(Locale.ROOT);
        }
        if (!this.sourceStack.isEmpty()) {
            return SpawnEggEditorHelper.getItemId(this.sourceStack);
        }
        return "-";
    }

    private String currentPos() {
        if (this.targetEntity == null) {
            return "-";
        }
        return String.format(Locale.ROOT, "%.1f, %.1f, %.1f", this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ());
    }

    private String currentHealth() {
        Entity entity = this.targetEntity;
        if (!(entity instanceof LivingEntity)) {
            return "-";
        }
        LivingEntity living = (LivingEntity)entity;
        return String.format(Locale.ROOT, "%.1f / %.1f", Float.valueOf(living.getHealth()), Float.valueOf(living.getMaxHealth()));
    }

    private String currentHealthNumeric() {
        Entity entity = this.targetEntity;
        if (!(entity instanceof LivingEntity)) {
            return "";
        }
        LivingEntity living = (LivingEntity)entity;
        return String.format(Locale.ROOT, "%.1f", Float.valueOf(living.getHealth()));
    }

    private boolean canEditHealth() {
        return this.targetEntity instanceof LivingEntity || !this.sourceStack.isEmpty();
    }

    private int healthAdjustBaseX() {
        return this.healthBox == null ? this.px + 102 : this.healthBox.getX();
    }

    private int healthAdjustBaseY() {
        return this.healthBox == null ? this.py + 144 : this.healthBox.getY() + 22;
    }

    private boolean clickHealthAdjuster(int mx, int my) {
        if (!this.canEditHealth() || this.healthBox == null) {
            return false;
        }
        int y = this.healthAdjustBaseY();
        int h = 16;
        String[] labels = new String[]{"-10", "-1", "+1", "+10", "+100"};
        float[] deltas = new float[]{-10.0f, -1.0f, 1.0f, 10.0f, 100.0f};
        for (int i = 0; i < deltas.length; ++i) {
            int w = this.healthAdjustWidth(labels[i]);
            int bx = this.healthAdjustButtonX(labels, i);
            if (mx < bx || mx >= bx + w || my < y || my >= y + h) continue;
            this.pushUndo();
            this.adjustHealthBy(deltas[i]);
            return true;
        }
        return false;
    }

    private void adjustHealthBy(float delta) {
        Entity entity;
        Float current = this.parsePositiveFloat(this.healthBox == null ? "" : this.healthBox.getValue());
        if (current == null && (entity = this.targetEntity) instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            current = Float.valueOf(living.getHealth());
        }
        if (current == null) {
            current = Float.valueOf(1.0f);
        }
        float next = Math.max(0.0f, current.floatValue() + delta);
        if (this.healthBox != null) {
            this.healthBox.setValue(this.formatEditableHealth(next));
        }
        this.dirty = true;
    }

    private String formatEditableHealth(float value) {
        float rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.001f) {
            return Integer.toString((int)rounded);
        }
        return String.format(Locale.ROOT, "%.1f", Float.valueOf(value));
    }

    private String currentFlags() {
        if (this.targetEntity == null) {
            return "-";
        }
        boolean inv = this.targetEntity.isInvulnerable();
        boolean ng = this.targetEntity.isNoGravity();
        boolean sl = this.targetEntity.isSilent();
        return "Inv=" + inv + ", G=" + !ng + ", S=" + sl;
    }

    private void applyLocalPreview(CompoundTag patch) {
        AgeableMob ageable;
        Entity entity;
        if (this.targetEntity == null || patch == null) {
            return;
        }
        if (patch.contains("Invulnerable")) {
            this.targetEntity.setInvulnerable(this.readBoolTag(patch, "Invulnerable", false));
        }
        if (patch.contains("NoGravity")) {
            this.targetEntity.setNoGravity(this.readBoolTag(patch, "NoGravity", false));
        }
        if (patch.contains("Silent")) {
            this.targetEntity.setSilent(this.readBoolTag(patch, "Silent", false));
        }
        if (patch.contains("NoAI") && (entity = this.targetEntity) instanceof Mob) {
            Mob mob = (Mob)entity;
            mob.setNoAi(this.readBoolTag(patch, "NoAI", false));
        }
        if (patch.contains("IsBaby") && (entity = this.targetEntity) instanceof AgeableMob) {
            ageable = (AgeableMob)entity;
            if (this.readBoolTag(patch, "IsBaby", false)) {
                ageable.setAge(-24000);
            } else {
                ageable.setAge(0);
            }
        }
        if (patch.contains("CustomNameVisible")) {
            this.targetEntity.setCustomNameVisible(this.readBoolTag(patch, "CustomNameVisible", false));
        }
        if (patch.contains("CustomName")) {
            this.applyLocalCustomName(this.nameBox == null ? "" : this.nameBox.getValue());
        }
        if (patch.contains("Age") && (entity = this.targetEntity) instanceof AgeableMob) {
            ageable = (AgeableMob)entity;
            ageable.setAge(this.readIntTag(patch, "Age", 0));
        }
    }

    private void applyLocalCustomName(String name) {
        if (this.targetEntity == null) {
            return;
        }
        String normalized = this.normalizeCustomNameInput(name);
        MutableComponent component = normalized.isBlank() ? null : Component.literal((String)normalized);
        this.targetEntity.setCustomName((Component)component);
        this.targetEntity.setCustomNameVisible(!normalized.isBlank());
    }

    private int previewSectionY() {
        int base = this.healToggleY() + 26;
        return Math.min(base, this.py + this.ph - 92);
    }

    private int readIntTag(CompoundTag patch, String key, int def) {
        Optional opt;
        if (patch == null || key == null || key.isBlank()) {
            return def;
        }
        try {
            Object var7_9;
            Object out = patch.getClass().getMethod("getInt", String.class).invoke(patch, key);
            if (out instanceof Number) {
                Number n = (Number)out;
                return n.intValue();
            }
            if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof Number) {
                Number n = (Number)var7_9;
                return n.intValue();
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        try {
            Object out;
            Object raw = patch.getClass().getMethod("get", String.class).invoke(patch, key);
            if (raw instanceof Optional) {
                opt = (Optional)raw;
                raw = opt.orElse(null);
            }
            if (raw != null && (out = raw.getClass().getMethod("getAsInt", new Class[0]).invoke(raw, new Object[0])) instanceof Number) {
                Number n = (Number)out;
                return n.intValue();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return def;
    }

    private boolean readBoolTag(CompoundTag patch, String key, boolean def) {
        Optional opt;
        if (patch == null || key == null || key.isBlank()) {
            return def;
        }
        try {
            Object var7_9;
            Object out = patch.getClass().getMethod("getBoolean", String.class).invoke(patch, key);
            if (out instanceof Boolean) {
                Boolean b = (Boolean)out;
                return b;
            }
            if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof Boolean) {
                Boolean b = (Boolean)var7_9;
                return b;
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        try {
            Object out;
            Object raw = patch.getClass().getMethod("get", String.class).invoke(patch, key);
            if (raw instanceof Optional) {
                opt = (Optional)raw;
                raw = opt.orElse(null);
            }
            if (raw != null && (out = raw.getClass().getMethod("getAsBoolean", new Class[0]).invoke(raw, new Object[0])) instanceof Boolean) {
                Boolean b = (Boolean)out;
                return b;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return def;
    }

    private void setLocalMaxHealth(Object entity, float value) {
        LivingEntity living;
        block5: {
            block4: {
                if (!(entity instanceof LivingEntity)) break block4;
                living = (LivingEntity)entity;
                if (!(value <= 0.0f)) break block5;
            }
            return;
        }
        AttributeInstance attr = living.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue((double)value);
        }
    }

    private Float currentMaxHealth() {
        Entity entity = this.targetEntity;
        if (!(entity instanceof LivingEntity)) {
            return null;
        }
        LivingEntity living = (LivingEntity)entity;
        return Float.valueOf(living.getMaxHealth());
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private int healToggleX() {
        return this.px + 18;
    }

    private int healToggleY() {
        if (this.canEditHealth() && this.healthBox != null) {
            return this.healthAdjustBaseY() + 44;
        }
        return this.py + 168;
    }

    private boolean hitHealToggle(int mx, int my) {
        int x = this.healToggleX();
        int y = this.healToggleY();
        int size = 12;
        int textW = this.font.width((FormattedText)HEAL_FULL_LABEL);
        return mx >= x && mx < x + size + 6 + textW && my >= y && my < y + size;
    }

    private void renderHealToggle(GuiGraphics g, int accent) {
        int x = this.healToggleX();
        int y = this.healToggleY();
        int size = 12;
        g.fill(x, y, x + size, y + size, 1243293240);
        this.border(g, x, y, size, size, this.healToFullOnApply ? accent : -13878436);
        if (this.healToFullOnApply) {
            g.fill(x + 3, y + 3, x + size - 3, y + size - 3, -14498466);
        }
        VersionCompat.get().drawString(g, this.font, HEAL_FULL_LABEL, x + size + 6, y + 2, -2497806, false);
    }

    private boolean handleTextFieldClick(double mx, double my, int button) {
        if (this.focusInlineBox(this.nameBox, mx, my, this.currentName())) {
            if (this.healthBox != null) {
                this.healthBox.setFocused(false);
            }
            return true;
        }
        if (this.nameBox != null) {
            this.nameBox.setFocused(false);
        }
        if (this.canEditHealth() && this.focusInlineBox(this.healthBox, mx, my, this.currentHealthNumeric())) {
            if (this.nameBox != null) {
                this.nameBox.setFocused(false);
            }
            return true;
        }
        if (this.healthBox != null) {
            this.healthBox.setFocused(false);
        }
        return false;
    }

    private boolean focusInlineBox(EditBox box, double mx, double my, String fallback) {
        if (box == null || !this.hitInlineField(box, mx, my)) {
            return false;
        }
        if ((box.getValue() == null || box.getValue().isBlank()) && fallback != null && !fallback.isBlank()) {
            this.setInlineBoxValue(box, fallback);
        }
        box.setFocused(true);
        return true;
    }

    private boolean hitInlineField(EditBox box, double mx, double my) {
        if (box == null) {
            return false;
        }
        return mx >= (double)(box.getX() - 2) && mx < (double)(box.getX() + box.getWidth() + 2) && my >= (double)(box.getY() - 2) && my < (double)(box.getY() + box.getHeight() + 2);
    }

    private void setInlineBoxValue(EditBox box, String value) {
        if (box == null) {
            return;
        }
        boolean wasDirty = this.dirty;
        String next = box == this.nameBox ? this.normalizeCustomNameInput(value) : (value == null ? "" : value);
        box.setValue(next);
        this.dirty = wasDirty;
    }

    private int healthAdjustWidth(String label) {
        return Math.max(28, this.font.width(label) + 12);
    }

    private int healthAdjustButtonX(String[] labels, int index) {
        int x = this.healthAdjustBaseX();
        for (int i = 0; i < index; ++i) {
            x += this.healthAdjustWidth(labels[i]) + 4;
        }
        return x;
    }

    private String toCustomNameJson(String value) {
        return "{\"text\":" + this.jsonString(this.normalizeCustomNameInput(value)) + "}";
    }

    private String jsonString(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        out.append('\"');
        block7: for (int i = 0; i < value.length(); ++i) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\': {
                    out.append("\\\\");
                    continue block7;
                }
                case '\"': {
                    out.append("\\\"");
                    continue block7;
                }
                case '\n': {
                    out.append("\\n");
                    continue block7;
                }
                case '\r': {
                    out.append("\\r");
                    continue block7;
                }
                case '\t': {
                    out.append("\\t");
                    continue block7;
                }
                default: {
                    out.append(ch);
                }
            }
        }
        out.append('\"');
        return out.toString();
    }

    private String normalizeCustomNameInput(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return value;
        }
        try {
            String decoded = this.extractJsonText(JsonParser.parseString((String)trimmed));
            return decoded == null ? value : decoded;
        }
        catch (Throwable ignored) {
            return value;
        }
    }

    private String extractJsonText(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        if (element.isJsonArray()) {
            StringBuilder out = new StringBuilder();
            for (JsonElement entry : element.getAsJsonArray()) {
                String text = this.extractJsonText(entry);
                if (text == null) continue;
                out.append(text);
            }
            return out.toString();
        }
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        StringBuilder out = new StringBuilder();
        if (object.has("text")) {
            out.append(object.get("text").getAsString());
        }
        if (object.has("extra") && object.get("extra").isJsonArray()) {
            for (JsonElement extra : object.getAsJsonArray("extra")) {
                String text = this.extractJsonText(extra);
                if (text == null) continue;
                out.append(text);
            }
        }
        return out.toString();
    }

    private boolean applyPatchToIntegratedServer(Minecraft mc, String customName, Float healthInput, Float currentMaxHealth, Float healthToApply) {
        if (mc == null || this.targetEntity == null) {
            return false;
        }
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) {
            return false;
        }
        AtomicBoolean success = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        server.execute(() -> {
            try {
                Entity serverEntity = EditorCommandHelper.findIntegratedServerEntity(server, this.targetEntity.getId(), this.targetEntity.getUUID());
                if (serverEntity == null) {
                    return;
                }
                if (this.stInvulnerable != -1) {
                    serverEntity.setInvulnerable(this.stInvulnerable == 1);
                }
                if (this.stNoGravity != -1) {
                    serverEntity.setNoGravity(this.stNoGravity == 1);
                }
                if (this.stSilent != -1) {
                    serverEntity.setSilent(this.stSilent == 1);
                }
                if (this.stNoAi != -1 && serverEntity instanceof Mob) {
                    Mob mob = (Mob)serverEntity;
                    mob.setNoAi(this.stNoAi == 1);
                }
                if (this.stBaby != -1 && serverEntity instanceof AgeableMob) {
                    AgeableMob ageable = (AgeableMob)serverEntity;
                    ageable.setAge(this.stBaby == 1 ? -24000 : 0);
                }
                MutableComponent serverName = customName.isBlank() ? null : Component.literal((String)customName);
                serverEntity.setCustomName((Component)serverName);
                serverEntity.setCustomNameVisible(!customName.isBlank());
                if (serverEntity instanceof LivingEntity) {
                    AttributeInstance attr;
                    LivingEntity living = (LivingEntity)serverEntity;
                    if (healthInput != null && (currentMaxHealth == null || healthInput.floatValue() > currentMaxHealth.floatValue() + 0.01f) && (attr = living.getAttribute(Attributes.MAX_HEALTH)) != null) {
                        attr.setBaseValue((double)healthInput.floatValue());
                    }
                    if (healthToApply != null) {
                        living.setHealth(Math.max(0.0f, healthToApply.floatValue()));
                    }
                }
                success.set(true);
            }
            catch (Throwable throwable) {
            }
            finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(3L, TimeUnit.SECONDS)) {
                return false;
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return success.get();
    }

    public void onClose() {
        this.tryClose();
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        if (this.keyPressed(event.key(), event.scancode(), event.modifiers())) {
            return true;
        }
        return super.keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public boolean charTyped(CharacterEvent event) {
        if (this.charTyped((char)event.codepoint(), event.modifiers())) {
            return true;
        }
        return super.charTyped((char)event.codepoint(), event.modifiers());
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (this.mouseClicked(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseClicked(event.x(), event.y(), event.button());
    }

    private StateSnapshot captureState() {
        return new StateSnapshot(this.stNoAi, this.stInvulnerable, this.stNoGravity, this.stSilent, this.stBaby, this.healToFullOnApply, this.nameBox == null ? "" : this.nameBox.getValue(), this.healthBox == null ? "" : this.healthBox.getValue());
    }

    private void applyState(StateSnapshot s) {
        if (s == null) {
            return;
        }
        this.stNoAi = s.stNoAi;
        this.stInvulnerable = s.stInvulnerable;
        this.stNoGravity = s.stNoGravity;
        this.stSilent = s.stSilent;
        this.stBaby = s.stBaby;
        this.healToFullOnApply = s.healFull;
        if (this.nameBox != null) {
            this.nameBox.setValue(s.name == null ? "" : s.name);
        }
        if (this.healthBox != null) {
            this.healthBox.setValue(s.health == null ? "" : s.health);
        }
    }

    private void pushUndo() {
        StateSnapshot current = this.captureState();
        if (!this.undoStack.isEmpty() && Objects.equals(this.undoStack.get(this.undoStack.size() - 1), current)) {
            return;
        }
        this.undoStack.add(current);
        while (this.undoStack.size() > 40) {
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
        if (this.dirty) {
            this.confirmClose = true;
            return;
        }
        Minecraft.getInstance().setScreen(this.parent);
    }

    private void renderConfirm(GuiGraphics g, int mx, int my, String title, String desc, int color) {
        int w = 320;
        int h = 110;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        g.fill(0, 0, this.width, this.height, -2013265920);
        g.fill(x, y, x + w, y + h, -267315418);
        this.border(g, x, y, w, h, -13878436);
        VersionCompat.get().drawString(g, this.font, title, x + 12, y + 12, color, false);
        VersionCompat.get().drawString(g, this.font, desc, x + 12, y + 30, -2497806, false);
        int by = y + h - 30;
        g.fill(x + 12, by, x + 102, by + 20, 1780954962);
        this.border(g, x + 12, by, 90, 20, -13878436);
        VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.edit.cancel"), x + 20, by + 6, -2497806, false);
        g.fill(x + w - 102, by, x + w - 12, by + 20, color == -1096636 ? -1969677541 : -1978243788);
        this.border(g, x + w - 102, by, 90, 20, color);
        VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.edit.apply"), x + w - 72, by + 6, -2497806, false);
    }

    private void renderUnsavedConfirmLikeSimple(GuiGraphics g, int mx, int my) {
        int dw = 260;
        int dh = 110;
        int dx = (this.width - dw) / 2;
        int dy = (this.height - dh) / 2;
        g.fill(dx, dy, dx + dw, dy + dh, -267909104);
        this.border(g, dx, dy, dw, dh, -1096636);
        VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.confirm.title"), dx + 10, dy + 10, -1906448, false);
        g.fill(dx + 1, dy + 24, dx + dw - 1, dy + 25, -14540234);
        VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.confirm.unsaved"), dx + 10, dy + 32, -7035976, false);
        VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.confirm.discard_hint"), dx + 10, dy + 46, -10193781, false);
        int by = dy + dh - 32;
        int bw2 = 70;
        int bh2 = 22;
        int saveX = dx + 10;
        boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
        g.fill(saveX, by, saveX + bw2, by + bh2, sh ? -15293622 : -14498466);
        String saveLabel = Component.translatable((String)"ankinbt.confirm.save_close").getString();
        VersionCompat.get().drawString(g, this.font, saveLabel, saveX + (bw2 - this.font.width(saveLabel)) / 2, by + 7, -1906448, false);
        int discardX = dx + dw / 2 - bw2 / 2;
        boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
        g.fill(discardX, by, discardX + bw2, by + bh2, dh2 ? -2131803068 : 1089422404);
        String discardLabel = Component.translatable((String)"ankinbt.confirm.discard").getString();
        VersionCompat.get().drawString(g, this.font, discardLabel, discardX + (bw2 - this.font.width(discardLabel)) / 2, by + 7, -1906448, false);
        int cancelX = dx + dw - bw2 - 10;
        boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
        g.fill(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
        String cancelLabel = Component.translatable((String)"ankinbt.edit.cancel").getString();
        VersionCompat.get().drawString(g, this.font, cancelLabel, cancelX + (bw2 - this.font.width(cancelLabel)) / 2, by + 7, -7035976, false);
    }

    private boolean clickConfirm(int mx, int my) {
        if (this.confirmClose) {
            return this.clickUnsavedConfirmLikeSimple(mx, my);
        }
        int w = 320;
        int h = 110;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        int by = y + h - 30;
        if (mx >= x + 12 && mx < x + 102 && my >= by && my < by + 20) {
            this.confirmClose = false;
            this.confirmReset = false;
            return true;
        }
        if (mx >= x + w - 102 && mx < x + w - 12 && my >= by && my < by + 20) {
            if (this.confirmReset) {
                this.confirmReset = false;
                this.resetStates();
            } else if (this.confirmClose) {
                this.confirmClose = false;
                this.dirty = false;
                Minecraft.getInstance().setScreen(this.parent);
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
            this.applyPatch();
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

    private record StateSnapshot(int stNoAi, int stInvulnerable, int stNoGravity, int stSilent, int stBaby, boolean healFull, String name, String health) {
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
            VersionCompat.get().drawString(g, font, (String)text, this.x + 6, this.y + 7, color, false);
        }
    }
}
