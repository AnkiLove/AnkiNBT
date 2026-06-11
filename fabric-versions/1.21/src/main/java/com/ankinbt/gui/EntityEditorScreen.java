/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_1132
 *  net.minecraft.class_1296
 *  net.minecraft.class_1297
 *  net.minecraft.class_1308
 *  net.minecraft.class_1309
 *  net.minecraft.class_1324
 *  net.minecraft.class_1799
 *  net.minecraft.class_2487
 *  net.minecraft.class_2499
 *  net.minecraft.class_2520
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_364
 *  net.minecraft.class_437
 *  net.minecraft.class_5134
 *  net.minecraft.class_5250
 *  net.minecraft.class_5348
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
import net.minecraft.class_1132;
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1324;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_5134;
import net.minecraft.class_5250;
import net.minecraft.class_5348;

public class EntityEditorScreen
extends class_437 {
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
    private static final class_2561 HEAL_FULL_LABEL = class_2561.method_43471((String)"ankinbt.entity.heal_full");
    private final class_1297 targetEntity;
    private final class_1799 sourceStack;
    private final int inventorySlot;
    private final class_437 parent;
    private final List<UiBtn> buttons = new ArrayList<UiBtn>();
    private int stNoAi = -1;
    private int stInvulnerable = -1;
    private int stNoGravity = -1;
    private int stSilent = -1;
    private int stBaby = -1;
    private boolean healToFullOnApply = false;
    private class_342 nameBox;
    private class_342 healthBox;
    private class_2561 status = class_2561.method_43473();
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

    private EntityEditorScreen(class_1297 targetEntity, class_1799 sourceStack, int inventorySlot, class_437 parent) {
        super((class_2561)class_2561.method_43471((String)"ankinbt.entity.title"));
        this.targetEntity = targetEntity;
        this.sourceStack = sourceStack == null ? class_1799.field_8037 : sourceStack.method_7972();
        this.inventorySlot = inventorySlot;
        this.parent = parent;
    }

    public static EntityEditorScreen forEntity(class_1297 entity) {
        return new EntityEditorScreen(entity, class_1799.field_8037, -1, null);
    }

    public static EntityEditorScreen forEntity(class_1297 entity, class_437 parent) {
        return new EntityEditorScreen(entity, class_1799.field_8037, -1, parent);
    }

    public static EntityEditorScreen forSpawnEgg(class_1799 stack, int inventorySlot) {
        return new EntityEditorScreen(null, stack, inventorySlot, null);
    }

    public static EntityEditorScreen forSpawnEgg(class_1799 stack, int inventorySlot, class_437 parent) {
        return new EntityEditorScreen(null, stack, inventorySlot, parent);
    }

    protected void method_25426() {
        this.recalcBounds();
        this.nameBox = new class_342(this.field_22793, this.nameFieldX(), this.nameFieldY(), 192, 16, (class_2561)class_2561.method_43473());
        this.styleBox(this.nameBox);
        this.nameBox.method_1852(this.currentCustomNameInput());
        this.nameBox.method_1863(v -> {
            this.dirty = true;
        });
        this.method_37063((class_364)this.nameBox);
        this.healthBox = new class_342(this.field_22793, this.healthFieldX(), this.healthFieldY(), 88, 16, (class_2561)class_2561.method_43473());
        this.styleBox(this.healthBox);
        this.healthBox.method_1852(this.currentHealthNumeric());
        this.healthBox.method_1863(v -> {
            this.dirty = true;
        });
        this.method_37063((class_364)this.healthBox);
        this.rebuildButtons();
        this.undoStack.clear();
        this.undoStack.add(this.captureState());
    }

    private void styleBox(class_342 box) {
        if (box == null) {
            return;
        }
        try {
            box.method_1858(false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            box.method_1868(-2497806);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            box.method_1860(-7429177);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void recalcBounds() {
        this.pw = Math.min(740, this.field_22789 - 20);
        this.ph = Math.min(420, this.field_22790 - 20);
        this.px = (this.field_22789 - this.pw) / 2;
        this.py = (this.field_22790 - this.ph) / 2;
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
            this.buttons.add(new UiBtn(rx, y, rw, rowH, () -> class_2561.method_43471((String)"ankinbt.entity.open_villager").getString(), this::openVillagerTradeEditor, true, null, 0));
            y += rowH + gap;
        }
        if (!this.sourceStack.method_7960()) {
            this.buttons.add(new UiBtn(rx, y, rw, rowH, () -> class_2561.method_43471((String)"ankinbt.entity.open_spawn_egg_nbt").getString(), () -> class_310.method_1551().method_1507((class_437)new NbtEditorScreen(this.sourceStack)), true, null, 0));
            y += rowH + gap;
        }
        int bottomY = this.py + this.ph - 30;
        int areaW = this.pw - 36;
        int actionBarW = (areaW - 16) / 3;
        this.buttons.add(new UiBtn(this.px + 18, bottomY, actionBarW, 20, () -> class_2561.method_43471((String)"ankinbt.entity.apply_patch").getString(), this::applyPatch, true, null, 1));
        this.buttons.add(new UiBtn(this.px + 18 + actionBarW + 8, bottomY, actionBarW, 20, () -> class_2561.method_43471((String)"ankinbt.entity.reset_changes").getString(), () -> {
            this.confirmReset = true;
        }, true, null, -1));
        this.buttons.add(new UiBtn(this.px + 18 + (actionBarW + 8) * 2, bottomY, actionBarW, 20, () -> class_2561.method_43471((String)"ankinbt.edit.cancel").getString(), this::tryClose, true, null, 0));
    }

    private boolean hasVillagerTradeContext() {
        String type;
        if (this.targetEntity != null && ((type = this.targetEntity.method_5864().toString().toLowerCase(Locale.ROOT)).contains("villager") || type.contains("wandering_trader"))) {
            return true;
        }
        return !this.sourceStack.method_7960() && SpawnEggEditorHelper.isVillagerSpawnEgg(this.sourceStack);
    }

    private void openVillagerTradeEditor() {
        String type;
        if (this.targetEntity != null && ((type = this.targetEntity.method_5864().toString().toLowerCase(Locale.ROOT)).contains("villager") || type.contains("wandering_trader"))) {
            class_310.method_1551().method_1507((class_437)VillagerTradeEditorScreen.forEntity(this.targetEntity, this));
            return;
        }
        if (SpawnEggEditorHelper.isVillagerSpawnEgg(this.sourceStack)) {
            class_310.method_1551().method_1507((class_437)VillagerTradeEditorScreen.forSpawnEgg(this.sourceStack, this.inventorySlot, this));
        }
    }

    private UiBtn stateBtn(int x, int y, int w, int h, String key, Supplier<Integer> getter, IntConsumer setter) {
        return new UiBtn(x, y, w, h, () -> class_2561.method_43469((String)key, (Object[])new Object[]{this.stateText((Integer)getter.get())}).getString(), () -> {
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
            return class_2561.method_43471((String)"ankinbt.entity.state.keep").getString();
        }
        return s > 0 ? class_2561.method_43471((String)"ankinbt.simple.on").getString() : class_2561.method_43471((String)"ankinbt.simple.off").getString();
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
            this.nameBox.method_1852(this.currentCustomNameInput());
        }
        if (this.healthBox != null) {
            this.healthBox.method_1852(this.currentHealthNumeric());
        }
        this.dirty = false;
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.entity.reset_done"), -13315175);
    }

    private class_2487 buildPatch() {
        String id;
        Float healthToApply;
        String customName;
        class_2487 patch = new class_2487();
        this.putTriState(patch, "NoAI", this.stNoAi);
        this.putTriState(patch, "Invulnerable", this.stInvulnerable);
        this.putTriState(patch, "NoGravity", this.stNoGravity);
        this.putTriState(patch, "Silent", this.stSilent);
        this.putTriState(patch, "IsBaby", this.stBaby);
        if (this.stBaby == 1) {
            patch.method_10569("Age", -24000);
        }
        if (this.stBaby == 0) {
            patch.method_10569("Age", 0);
        }
        if (!Objects.equals(customName = this.normalizeCustomNameInput(this.nameBox == null ? "" : this.nameBox.method_1882()).trim(), this.normalizeCustomNameInput(this.currentCustomNameInput()).trim())) {
            patch.method_10582("CustomName", this.toCustomNameJson(customName));
            patch.method_10556("CustomNameVisible", !customName.isBlank());
        }
        Float healthInput = this.parsePositiveFloat(this.healthBox == null ? "" : this.healthBox.method_1882());
        Float currentMaxHealth = this.currentMaxHealth();
        if (healthInput != null && (this.targetEntity == null || currentMaxHealth == null || healthInput.floatValue() > currentMaxHealth.floatValue() + 0.01f)) {
            this.putMaxHealthPatch(patch, healthInput.floatValue());
        }
        if ((healthToApply = this.resolveHealthForApply(healthInput, currentMaxHealth)) != null) {
            patch.method_10548("Health", healthToApply.floatValue());
        }
        if (this.stInvulnerable == 1) {
            patch.method_10569("NoDamageTicks", Short.MAX_VALUE);
        }
        if (this.stInvulnerable == 0) {
            patch.method_10569("NoDamageTicks", 0);
        }
        if (this.targetEntity == null && SpawnEggEditorHelper.isSpawnEgg(this.sourceStack) && !patch.method_10545("id") && !(id = SpawnEggEditorHelper.inferEntityIdFromSpawnEgg(this.sourceStack)).isBlank()) {
            patch.method_10582("id", id);
        }
        return patch;
    }

    private void putMaxHealthPatch(class_2487 patch, float health) {
        class_2499 attrs = new class_2499();
        class_2487 attr = new class_2487();
        attr.method_10582("id", "minecraft:generic.max_health");
        attr.method_10549("base", (double)health);
        attrs.add((Object)attr);
        patch.method_10566("attributes", (class_2520)attrs);
        class_2499 legacy = new class_2499();
        class_2487 legacyAttr = new class_2487();
        legacyAttr.method_10582("Name", "minecraft:generic.max_health");
        legacyAttr.method_10549("Base", (double)health);
        legacy.add((Object)legacyAttr);
        patch.method_10566("Attributes", (class_2520)legacy);
    }

    private void putTriState(class_2487 patch, String key, int state) {
        if (state == -1) {
            return;
        }
        patch.method_10556(key, state == 1);
    }

    private void applyPatch() {
        class_310 mc = class_310.method_1551();
        class_2487 patch = this.buildPatch();
        String customName = this.normalizeCustomNameInput(this.nameBox == null ? "" : this.nameBox.method_1882()).trim();
        Float healthInput = this.parsePositiveFloat(this.healthBox == null ? "" : this.healthBox.method_1882());
        Float currentMaxHealth = this.currentMaxHealth();
        Float healthToApply = this.resolveHealthForApply(healthInput, currentMaxHealth);
        if (patch.method_33133()) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.entity.preview_empty"), -7429177);
            return;
        }
        if (this.targetEntity != null) {
            if (mc.field_1724 == null) {
                return;
            }
            boolean ok = this.applyPatchToIntegratedServer(mc, customName, healthInput, currentMaxHealth, healthToApply);
            if (!ok && !EditorCommandHelper.canUseEntityCommand(mc)) {
                this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.entity.admin_required"), -1096636);
                return;
            }
            if (!ok) {
                ok = EditorCommandHelper.applyMergeToEntity(mc, this.targetEntity, patch);
            }
            this.setStatus((class_2561)(ok ? class_2561.method_43471((String)"ankinbt.entity.applied") : class_2561.method_43471((String)"ankinbt.status.save_error")), ok ? -13315175 : -1096636);
            if (ok) {
                this.applyLocalPreview(patch);
                if (healthInput != null && (currentMaxHealth == null || healthInput.floatValue() > currentMaxHealth.floatValue() + 0.01f)) {
                    EditorCommandHelper.setEntityMaxHealth(mc, this.targetEntity, healthInput.floatValue());
                    this.setLocalMaxHealth(this.targetEntity, healthInput.floatValue());
                    currentMaxHealth = healthInput;
                }
                if (healthToApply != null) {
                    class_1297 class_12972 = this.targetEntity;
                    if (class_12972 instanceof class_1309) {
                        class_1309 living = (class_1309)class_12972;
                        living.method_6033(healthToApply.floatValue());
                    }
                    this.healthBox.method_1852(String.format(Locale.ROOT, "%.1f", healthToApply));
                }
                this.dirty = false;
                this.undoStack.clear();
                this.undoStack.add(this.captureState());
            }
            return;
        }
        if (!SpawnEggEditorHelper.isSpawnEgg(this.sourceStack)) {
            this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.entity.spawn_egg_required"), -1096636);
            return;
        }
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

    private void setStatus(class_2561 message, int color) {
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

    public boolean method_25402(double mx, double my, int button) {
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
            this.nameBox.method_25365(false);
        }
        if (this.healthBox != null) {
            this.healthBox.method_25365(false);
        }
        return false;
    }

    public boolean method_25404(int key, int scan, int mod) {
        boolean ctrl;
        if (this.nameBox != null && this.nameBox.method_25370() && this.nameBox.method_25404(key, scan, mod)) {
            return true;
        }
        if (this.healthBox != null && this.healthBox.method_25370() && this.healthBox.method_25404(key, scan, mod)) {
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

    public boolean method_25400(char codePoint, int modifiers) {
        if (this.nameBox != null && this.nameBox.method_25370() && this.nameBox.method_25400(codePoint, modifiers)) {
            return true;
        }
        if (this.healthBox != null && this.healthBox.method_25370()) {
            return this.healthBox.method_25400(codePoint, modifiers);
        }
        return false;
    }

    public void method_25394(class_332 g, int mx, int my, float partialTick) {
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
        g.method_25294(0, 0, this.field_22789, this.field_22790, scrim);
        if (shadow != 0) {
            g.method_25294(this.px + 4, this.py + 4, this.px + this.pw + 4, this.py + this.ph + 4, shadow);
        }
        g.method_25294(this.px, this.py, this.px + this.pw, this.py + this.ph, panel);
        this.border(g, this.px, this.py, this.pw, this.ph, border);
        g.method_25294(this.px + 1, this.py + 1, this.px + this.pw - 1, this.py + 34, UiTheme.header(opacity, this.openAnim));
        g.method_25294(this.px + 1, this.py + 34, this.px + this.pw - 1, this.py + 35, border);
        g.method_25294(this.px + 1, this.py + 48, this.px + this.pw - 1, this.py + this.ph - 40, card);
        VersionCompat.get().drawString(g, this.field_22793, this.field_22785, this.px + 12, this.py + 12, -788737, false);
        String mode = this.targetEntity != null ? class_2561.method_43471((String)"ankinbt.entity.mode.entity").getString() : class_2561.method_43471((String)"ankinbt.entity.mode.spawn_egg").getString();
        VersionCompat.get().drawString(g, this.field_22793, mode, this.px + 170, this.py + 13, -7429177, false);
        int left = this.px + 18;
        int mid = this.px + this.pw / 2;
        int nameY = this.py + 74;
        int typeY = nameY + 16;
        int posY = typeY + 16;
        int healthY = posY + 16;
        int flagY = this.canEditHealth() ? this.healthAdjustBaseY() + 22 : healthY + 16;
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.entity.section.current"), left, this.py + 64, accent, false);
        this.drawInlineLabel(g, left, nameY, class_2561.method_43471((String)"ankinbt.entity.info.name").getString());
        this.renderInlineField(g, this.nameBox, this.currentName(), mx, my, accent);
        this.drawInfo(g, left, typeY, class_2561.method_43471((String)"ankinbt.entity.info.type").getString(), this.currentType());
        this.drawInfo(g, left, posY, class_2561.method_43471((String)"ankinbt.entity.info.pos").getString(), this.currentPos());
        this.drawInlineLabel(g, left, healthY, class_2561.method_43471((String)"ankinbt.entity.info.health").getString());
        if (this.canEditHealth()) {
            this.renderInlineHealthField(g, mx, my, accent);
            this.renderHealthAdjusters(g, mx, my, accent);
        } else {
            VersionCompat.get().drawString(g, this.field_22793, this.currentHealth(), left + 84, healthY, -2497806, false);
        }
        this.drawInfo(g, left, flagY, class_2561.method_43471((String)"ankinbt.entity.info.flags").getString(), this.currentFlags());
        this.renderHealToggle(g, accent);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.entity.section.actions"), mid + 8, this.py + 64, accent, false);
        if (AnkiConfig.isEntityLivePreview()) {
            VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.entity.section.preview"), left, this.previewSectionY(), accent, false);
            Object preview = this.buildPatch().toString();
            if (((String)preview).length() > 78) {
                preview = ((String)preview).substring(0, 75) + "...";
            }
            VersionCompat.get().drawString(g, this.field_22793, (String)preview, left, this.previewSectionY() + 16, -2497806, false);
        }
        for (UiBtn btn : this.buttons) {
            btn.render(g, this.field_22793, mx, my, accent);
        }
        if (this.confirmReset) {
            this.renderConfirm(g, mx, my, class_2561.method_43471((String)"ankinbt.entity.reset_changes").getString(), class_2561.method_43471((String)"ankinbt.confirm.discard_hint").getString(), -1096636);
        } else if (this.confirmClose) {
            this.renderUnsavedConfirmLikeSimple(g, mx, my);
        }
        if (this.status != null && !this.status.getString().isEmpty() && System.currentTimeMillis() - this.statusTime < 2600L) {
            int statusY = this.py + this.ph - 44;
            VersionCompat.get().drawString(g, this.field_22793, this.status, left, statusY, this.statusColor, false);
        }
    }

    private void drawInfo(class_332 g, int x, int y, String key, String value) {
        VersionCompat.get().drawString(g, this.field_22793, key + ":", x, y, -7429177, false);
        VersionCompat.get().drawString(g, this.field_22793, value, x + 84, y, -2497806, false);
    }

    private void drawInlineLabel(class_332 g, int x, int y, String key) {
        VersionCompat.get().drawString(g, this.field_22793, key + ":", x, y, -7429177, false);
    }

    private void renderInlineField(class_332 g, class_342 box, String placeholder, int mx, int my, int accent) {
        boolean placeholderMode;
        if (box == null) {
            return;
        }
        boolean focused = box.method_25370();
        boolean hover = mx >= box.method_46426() && mx < box.method_46426() + box.method_25368() && my >= box.method_46427() && my < box.method_46427() + box.method_25364();
        String raw = box.method_1882();
        boolean bl = placeholderMode = (raw == null || raw.isBlank()) && !focused && placeholder != null && !placeholder.isBlank();
        String shown = placeholderMode ? placeholder : (raw == null ? "" : raw);
        int color = placeholderMode ? -7429177 : -2497806;
        int textY = box.method_46427() + 2;
        int maxWidth = Math.max(12, box.method_25368() - 4);
        if (this.field_22793.method_1727(shown) > maxWidth) {
            shown = this.field_22793.method_27523(shown, maxWidth);
        }
        VersionCompat.get().drawString(g, this.field_22793, shown, box.method_46426(), textY, color, false);
        int underline = focused ? accent : (hover ? -12494202 : -13878436);
        g.method_25294(box.method_46426(), box.method_46427() + box.method_25364() - 1, box.method_46426() + box.method_25368(), box.method_46427() + box.method_25364(), underline);
        if (focused && (System.currentTimeMillis() / 500L & 1L) == 0L) {
            int cursorX = Math.min(box.method_46426() + this.field_22793.method_1727(shown) + 1, box.method_46426() + box.method_25368() - 1);
            g.method_25294(cursorX, box.method_46427() + 1, cursorX + 1, box.method_46427() + box.method_25364() - 2, -2497806);
        }
    }

    private void renderInlineHealthField(class_332 g, int mx, int my, int accent) {
        if (this.healthBox == null) {
            return;
        }
        String raw = this.healthBox.method_1882();
        if (raw == null || raw.isBlank()) {
            raw = this.currentHealthNumeric();
        }
        String shown = raw == null ? "" : raw;
        this.renderInlineField(g, this.healthBox, shown, mx, my, accent);
        Float max = this.currentMaxHealth();
        if (max != null) {
            String tail = " / " + String.format(Locale.ROOT, "%.1f", max);
            int tailX = Math.min(this.healthBox.method_46426() + Math.min(this.field_22793.method_1727(shown), this.healthBox.method_25368() - 4) + 8, this.healthBox.method_46426() + this.healthBox.method_25368() + 12);
            VersionCompat.get().drawString(g, this.field_22793, tail, tailX, this.healthBox.method_46427() + 2, -7429177, false);
        }
    }

    private void renderHealthAdjusters(class_332 g, int mx, int my, int accent) {
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
            g.method_25294(bx, y, bx + w, y + h, hover ? 1244084050 : 840178731);
            this.border(g, bx, y, w, h, hover ? accent : -13878436);
            String label = labels[i];
            VersionCompat.get().drawString(g, this.field_22793, label, bx + (w - this.field_22793.method_1727(label)) / 2, y + 4, -2497806, false);
        }
    }

    private String currentName() {
        if (this.targetEntity != null) {
            String custom = this.currentCustomNameInput();
            if (!custom.isBlank()) {
                return custom;
            }
            return this.normalizeCustomNameInput(this.targetEntity.method_5476().getString());
        }
        if (!this.sourceStack.method_7960()) {
            return this.sourceStack.method_7964().getString();
        }
        return "-";
    }

    private String currentCustomNameInput() {
        if (this.targetEntity != null) {
            class_2561 custom = this.targetEntity.method_5797();
            return custom == null ? "" : this.normalizeCustomNameInput(custom.getString());
        }
        return "";
    }

    private String currentType() {
        if (this.targetEntity != null) {
            return this.targetEntity.method_5864().toString().toLowerCase(Locale.ROOT);
        }
        if (!this.sourceStack.method_7960()) {
            return SpawnEggEditorHelper.getItemId(this.sourceStack);
        }
        return "-";
    }

    private String currentPos() {
        if (this.targetEntity == null) {
            return "-";
        }
        return String.format(Locale.ROOT, "%.1f, %.1f, %.1f", this.targetEntity.method_23317(), this.targetEntity.method_23318(), this.targetEntity.method_23321());
    }

    private String currentHealth() {
        class_1297 class_12972 = this.targetEntity;
        if (!(class_12972 instanceof class_1309)) {
            return "-";
        }
        class_1309 living = (class_1309)class_12972;
        return String.format(Locale.ROOT, "%.1f / %.1f", Float.valueOf(living.method_6032()), Float.valueOf(living.method_6063()));
    }

    private String currentHealthNumeric() {
        class_1297 class_12972 = this.targetEntity;
        if (!(class_12972 instanceof class_1309)) {
            return "";
        }
        class_1309 living = (class_1309)class_12972;
        return String.format(Locale.ROOT, "%.1f", Float.valueOf(living.method_6032()));
    }

    private boolean canEditHealth() {
        return this.targetEntity instanceof class_1309 || !this.sourceStack.method_7960();
    }

    private int healthAdjustBaseX() {
        return this.healthBox == null ? this.px + 102 : this.healthBox.method_46426();
    }

    private int healthAdjustBaseY() {
        return this.healthBox == null ? this.py + 144 : this.healthBox.method_46427() + 22;
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
        class_1297 class_12972;
        Float current = this.parsePositiveFloat(this.healthBox == null ? "" : this.healthBox.method_1882());
        if (current == null && (class_12972 = this.targetEntity) instanceof class_1309) {
            class_1309 living = (class_1309)class_12972;
            current = Float.valueOf(living.method_6032());
        }
        if (current == null) {
            current = Float.valueOf(1.0f);
        }
        float next = Math.max(0.0f, current.floatValue() + delta);
        if (this.healthBox != null) {
            this.healthBox.method_1852(this.formatEditableHealth(next));
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
        boolean inv = this.targetEntity.method_5655();
        boolean ng = this.targetEntity.method_5740();
        boolean sl = this.targetEntity.method_5701();
        return "Inv=" + inv + ", G=" + !ng + ", S=" + sl;
    }

    private void applyLocalPreview(class_2487 patch) {
        class_1296 ageable;
        class_1297 class_12972;
        if (this.targetEntity == null || patch == null) {
            return;
        }
        if (patch.method_10545("Invulnerable")) {
            this.targetEntity.method_5684(this.readBoolTag(patch, "Invulnerable", false));
        }
        if (patch.method_10545("NoGravity")) {
            this.targetEntity.method_5875(this.readBoolTag(patch, "NoGravity", false));
        }
        if (patch.method_10545("Silent")) {
            this.targetEntity.method_5803(this.readBoolTag(patch, "Silent", false));
        }
        if (patch.method_10545("NoAI") && (class_12972 = this.targetEntity) instanceof class_1308) {
            class_1308 mob = (class_1308)class_12972;
            mob.method_5977(this.readBoolTag(patch, "NoAI", false));
        }
        if (patch.method_10545("IsBaby") && (class_12972 = this.targetEntity) instanceof class_1296) {
            ageable = (class_1296)class_12972;
            if (this.readBoolTag(patch, "IsBaby", false)) {
                ageable.method_5614(-24000);
            } else {
                ageable.method_5614(0);
            }
        }
        if (patch.method_10545("CustomNameVisible")) {
            this.targetEntity.method_5880(this.readBoolTag(patch, "CustomNameVisible", false));
        }
        if (patch.method_10545("CustomName")) {
            this.applyLocalCustomName(this.nameBox == null ? "" : this.nameBox.method_1882());
        }
        if (patch.method_10545("Age") && (class_12972 = this.targetEntity) instanceof class_1296) {
            ageable = (class_1296)class_12972;
            ageable.method_5614(this.readIntTag(patch, "Age", 0));
        }
    }

    private void applyLocalCustomName(String name) {
        if (this.targetEntity == null) {
            return;
        }
        String normalized = this.normalizeCustomNameInput(name);
        class_5250 component = normalized.isBlank() ? null : class_2561.method_43470((String)normalized);
        this.targetEntity.method_5665((class_2561)component);
        this.targetEntity.method_5880(!normalized.isBlank());
    }

    private int previewSectionY() {
        int base = this.healToggleY() + 26;
        return Math.min(base, this.py + this.ph - 92);
    }

    private int readIntTag(class_2487 patch, String key, int def) {
        Optional opt;
        if (patch == null || key == null || key.isBlank()) {
            return def;
        }
        try {
            Object var7_9;
            Object out = patch.getClass().getMethod("getInt", String.class).invoke((Object)patch, key);
            if (out instanceof Number) {
                Number n = (Number)out;
                return n.intValue();
            }
            if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof Number) {
                Number n = var7_9;
                return n.intValue();
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        try {
            Object out;
            Object raw = patch.getClass().getMethod("get", String.class).invoke((Object)patch, key);
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

    private boolean readBoolTag(class_2487 patch, String key, boolean def) {
        Optional opt;
        if (patch == null || key == null || key.isBlank()) {
            return def;
        }
        try {
            Object var7_9;
            Object out = patch.getClass().getMethod("getBoolean", String.class).invoke((Object)patch, key);
            if (out instanceof Boolean) {
                Boolean b = (Boolean)out;
                return b;
            }
            if (out instanceof Optional && (var7_9 = (opt = (Optional)out).orElse(null)) instanceof Boolean) {
                Boolean b = var7_9;
                return b;
            }
        }
        catch (Throwable out) {
            // empty catch block
        }
        try {
            Object out;
            Object raw = patch.getClass().getMethod("get", String.class).invoke((Object)patch, key);
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
        class_1309 living;
        block5: {
            block4: {
                if (!(entity instanceof class_1309)) break block4;
                living = (class_1309)entity;
                if (!(value <= 0.0f)) break block5;
            }
            return;
        }
        class_1324 attr = living.method_5996(class_5134.field_23716);
        if (attr != null) {
            attr.method_6192((double)value);
        }
    }

    private Float currentMaxHealth() {
        class_1297 class_12972 = this.targetEntity;
        if (!(class_12972 instanceof class_1309)) {
            return null;
        }
        class_1309 living = (class_1309)class_12972;
        return Float.valueOf(living.method_6063());
    }

    private void border(class_332 g, int x, int y, int w, int h, int c) {
        g.method_25294(x, y, x + w, y + 1, c);
        g.method_25294(x, y + h - 1, x + w, y + h, c);
        g.method_25294(x, y, x + 1, y + h, c);
        g.method_25294(x + w - 1, y, x + w, y + h, c);
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
        int textW = this.field_22793.method_27525((class_5348)HEAL_FULL_LABEL);
        return mx >= x && mx < x + size + 6 + textW && my >= y && my < y + size;
    }

    private void renderHealToggle(class_332 g, int accent) {
        int x = this.healToggleX();
        int y = this.healToggleY();
        int size = 12;
        g.method_25294(x, y, x + size, y + size, 1243293240);
        this.border(g, x, y, size, size, this.healToFullOnApply ? accent : -13878436);
        if (this.healToFullOnApply) {
            g.method_25294(x + 3, y + 3, x + size - 3, y + size - 3, -14498466);
        }
        VersionCompat.get().drawString(g, this.field_22793, HEAL_FULL_LABEL, x + size + 6, y + 2, -2497806, false);
    }

    private boolean handleTextFieldClick(double mx, double my, int button) {
        if (this.focusInlineBox(this.nameBox, mx, my, this.currentName())) {
            if (this.healthBox != null) {
                this.healthBox.method_25365(false);
            }
            return true;
        }
        if (this.nameBox != null) {
            this.nameBox.method_25365(false);
        }
        if (this.canEditHealth() && this.focusInlineBox(this.healthBox, mx, my, this.currentHealthNumeric())) {
            if (this.nameBox != null) {
                this.nameBox.method_25365(false);
            }
            return true;
        }
        if (this.healthBox != null) {
            this.healthBox.method_25365(false);
        }
        return false;
    }

    private boolean focusInlineBox(class_342 box, double mx, double my, String fallback) {
        if (box == null || !this.hitInlineField(box, mx, my)) {
            return false;
        }
        if ((box.method_1882() == null || box.method_1882().isBlank()) && fallback != null && !fallback.isBlank()) {
            this.setInlineBoxValue(box, fallback);
        }
        box.method_25365(true);
        return true;
    }

    private boolean hitInlineField(class_342 box, double mx, double my) {
        if (box == null) {
            return false;
        }
        return mx >= (double)(box.method_46426() - 2) && mx < (double)(box.method_46426() + box.method_25368() + 2) && my >= (double)(box.method_46427() - 2) && my < (double)(box.method_46427() + box.method_25364() + 2);
    }

    private void setInlineBoxValue(class_342 box, String value) {
        if (box == null) {
            return;
        }
        boolean wasDirty = this.dirty;
        String next = box == this.nameBox ? this.normalizeCustomNameInput(value) : (value == null ? "" : value);
        box.method_1852(next);
        this.dirty = wasDirty;
    }

    private int healthAdjustWidth(String label) {
        return Math.max(28, this.field_22793.method_1727(label) + 12);
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

    private boolean applyPatchToIntegratedServer(class_310 mc, String customName, Float healthInput, Float currentMaxHealth, Float healthToApply) {
        if (mc == null || this.targetEntity == null) {
            return false;
        }
        class_1132 server = mc.method_1576();
        if (server == null) {
            return false;
        }
        AtomicBoolean success = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        server.execute(() -> {
            try {
                class_1297 serverEntity = EditorCommandHelper.findIntegratedServerEntity(server, this.targetEntity.method_5628(), this.targetEntity.method_5667());
                if (serverEntity == null) {
                    return;
                }
                if (this.stInvulnerable != -1) {
                    serverEntity.method_5684(this.stInvulnerable == 1);
                }
                if (this.stNoGravity != -1) {
                    serverEntity.method_5875(this.stNoGravity == 1);
                }
                if (this.stSilent != -1) {
                    serverEntity.method_5803(this.stSilent == 1);
                }
                if (this.stNoAi != -1 && serverEntity instanceof class_1308) {
                    class_1308 mob = (class_1308)serverEntity;
                    mob.method_5977(this.stNoAi == 1);
                }
                if (this.stBaby != -1 && serverEntity instanceof class_1296) {
                    class_1296 ageable = (class_1296)serverEntity;
                    ageable.method_5614(this.stBaby == 1 ? -24000 : 0);
                }
                class_5250 serverName = customName.isBlank() ? null : class_2561.method_43470((String)customName);
                serverEntity.method_5665((class_2561)serverName);
                serverEntity.method_5880(!customName.isBlank());
                if (serverEntity instanceof class_1309) {
                    class_1324 attr;
                    class_1309 living = (class_1309)serverEntity;
                    if (healthInput != null && (currentMaxHealth == null || healthInput.floatValue() > currentMaxHealth.floatValue() + 0.01f) && (attr = living.method_5996(class_5134.field_23716)) != null) {
                        attr.method_6192((double)healthInput.floatValue());
                    }
                    if (healthToApply != null) {
                        living.method_6033(Math.max(0.0f, healthToApply.floatValue()));
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

    public void method_25419() {
        this.tryClose();
    }

    public boolean method_25421() {
        return false;
    }

    private StateSnapshot captureState() {
        return new StateSnapshot(this.stNoAi, this.stInvulnerable, this.stNoGravity, this.stSilent, this.stBaby, this.healToFullOnApply, this.nameBox == null ? "" : this.nameBox.method_1882(), this.healthBox == null ? "" : this.healthBox.method_1882());
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
            this.nameBox.method_1852(s.name == null ? "" : s.name);
        }
        if (this.healthBox != null) {
            this.healthBox.method_1852(s.health == null ? "" : s.health);
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
        this.setStatus((class_2561)class_2561.method_43471((String)"ankinbt.status.edited"), -7429177);
    }

    private void tryClose() {
        if (this.dirty) {
            this.confirmClose = true;
            return;
        }
        class_310.method_1551().method_1507(this.parent);
    }

    private void renderConfirm(class_332 g, int mx, int my, String title, String desc, int color) {
        int w = 320;
        int h = 110;
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) / 2;
        g.method_25294(0, 0, this.field_22789, this.field_22790, -2013265920);
        g.method_25294(x, y, x + w, y + h, -267315418);
        this.border(g, x, y, w, h, -13878436);
        VersionCompat.get().drawString(g, this.field_22793, title, x + 12, y + 12, color, false);
        VersionCompat.get().drawString(g, this.field_22793, desc, x + 12, y + 30, -2497806, false);
        int by = y + h - 30;
        g.method_25294(x + 12, by, x + 102, by + 20, 1780954962);
        this.border(g, x + 12, by, 90, 20, -13878436);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.edit.cancel"), x + 20, by + 6, -2497806, false);
        g.method_25294(x + w - 102, by, x + w - 12, by + 20, color == -1096636 ? -1969677541 : -1978243788);
        this.border(g, x + w - 102, by, 90, 20, color);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.edit.apply"), x + w - 72, by + 6, -2497806, false);
    }

    private void renderUnsavedConfirmLikeSimple(class_332 g, int mx, int my) {
        int dw = 260;
        int dh = 110;
        int dx = (this.field_22789 - dw) / 2;
        int dy = (this.field_22790 - dh) / 2;
        g.method_25294(dx, dy, dx + dw, dy + dh, -267909104);
        this.border(g, dx, dy, dw, dh, -1096636);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.confirm.title"), dx + 10, dy + 10, -1906448, false);
        g.method_25294(dx + 1, dy + 24, dx + dw - 1, dy + 25, -14540234);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.confirm.unsaved"), dx + 10, dy + 32, -7035976, false);
        VersionCompat.get().drawString(g, this.field_22793, (class_2561)class_2561.method_43471((String)"ankinbt.confirm.discard_hint"), dx + 10, dy + 46, -10193781, false);
        int by = dy + dh - 32;
        int bw2 = 70;
        int bh2 = 22;
        int saveX = dx + 10;
        boolean sh = mx >= saveX && mx < saveX + bw2 && my >= by && my < by + bh2;
        g.method_25294(saveX, by, saveX + bw2, by + bh2, sh ? -15293622 : -14498466);
        String saveLabel = class_2561.method_43471((String)"ankinbt.confirm.save_close").getString();
        VersionCompat.get().drawString(g, this.field_22793, saveLabel, saveX + (bw2 - this.field_22793.method_1727(saveLabel)) / 2, by + 7, -1906448, false);
        int discardX = dx + dw / 2 - bw2 / 2;
        boolean dh2 = mx >= discardX && mx < discardX + bw2 && my >= by && my < by + bh2;
        g.method_25294(discardX, by, discardX + bw2, by + bh2, dh2 ? -2131803068 : 1089422404);
        String discardLabel = class_2561.method_43471((String)"ankinbt.confirm.discard").getString();
        VersionCompat.get().drawString(g, this.field_22793, discardLabel, discardX + (bw2 - this.field_22793.method_1727(discardLabel)) / 2, by + 7, -1906448, false);
        int cancelX = dx + dw - bw2 - 10;
        boolean ch = mx >= cancelX && mx < cancelX + bw2 && my >= by && my < by + bh2;
        g.method_25294(cancelX, by, cancelX + bw2, by + bh2, ch ? 0x50FFFFFF : 0x30FFFFFF);
        String cancelLabel = class_2561.method_43471((String)"ankinbt.edit.cancel").getString();
        VersionCompat.get().drawString(g, this.field_22793, cancelLabel, cancelX + (bw2 - this.field_22793.method_1727(cancelLabel)) / 2, by + 7, -7035976, false);
    }

    private boolean clickConfirm(int mx, int my) {
        if (this.confirmClose) {
            return this.clickUnsavedConfirmLikeSimple(mx, my);
        }
        int w = 320;
        int h = 110;
        int x = (this.field_22789 - w) / 2;
        int y = (this.field_22790 - h) / 2;
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
                class_310.method_1551().method_1507(this.parent);
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
            this.applyPatch();
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
            VersionCompat.get().drawString(g, font, (String)text, this.x + 6, this.y + 7, color, false);
        }
    }
}

