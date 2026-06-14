/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.options.OptionsScreen
 *  net.minecraft.client.gui.screens.options.controls.ControlsScreen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 */
package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.gui.CustomItemGroupsScreen;
import com.ankinbt.gui.UiTheme;
import com.ankinbt.keybind.KeyBindings;
import com.ankinbt.util.DebugLog;
import com.ankinbt.util.UiSound;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class AnkiConfigScreen
extends Screen {
    private final Screen parent;
    private final List<UiBtn> buttons = new ArrayList<UiBtn>();
    private Tab tab = Tab.GENERAL;
    private Component status = Component.empty();
    private int statusColor = -7035976;
    private long statusTime = 0L;
    private int px;
    private int py;
    private int pw;
    private int ph;
    private int contentTop;
    private int contentBottom;
    private int maxScroll = 0;
    private float scroll = 0.0f;
    private float targetScroll = 0.0f;
    private float openAnim = 0.0f;
    private long lastDebugRefresh = 0L;
    private long lastKeySync = 0L;

    public AnkiConfigScreen(Screen parent) {
        super((Component)Component.translatable((String)"ankinbt.config.title"));
        this.parent = parent;
    }

    protected void init() {
        this.recalcBounds();
        KeyBindings.syncConfigFromKeyMappings();
        this.rebuildButtons();
    }

    private void recalcBounds() {
        this.pw = Math.min(680, this.width - 24);
        this.ph = Math.min(460, this.height - 20);
        this.px = (this.width - this.pw) / 2;
        this.py = (this.height - this.ph) / 2;
        this.contentTop = this.py + 74;
        this.contentBottom = this.py + this.ph - 44;
    }

    private void rebuildButtons() {
        int gap;
        this.buttons.clear();
        int tabY = this.py + 38;
        int tabW = 98;
        int tabH = 20;
        int tx = this.px + 18;
        Tab[] tabArray = Tab.values();
        int n = tabArray.length;
        for (int i = 0; i < n; ++i) {
            Tab t;
            Tab target = t = tabArray[i];
            this.buttons.add(new UiBtn(tx, tabY, tabW, tabH, () -> Component.translatable((String)target.key).getString(), () -> {
                this.tab = target;
                this.targetScroll = 0.0f;
                this.scroll = 0.0f;
                this.rebuildButtons();
            }, true, () -> this.tab == target, false));
            tx += tabW + 8;
        }
        int left = this.px + 18;
        int right = this.px + this.pw - 18;
        int rowW = right - left;
        int y = this.contentTop;
        int rowH = AnkiConfig.isUiCompactLayout() ? 20 : 22;
        int n2 = gap = AnkiConfig.isUiCompactLayout() ? 4 : 7;
        if (this.tab == Tab.GENERAL) {
            this.buttons.add(this.toggleBtn(left, y, rowW, rowH, "ankinbt.config.preferred_editor", this::modeName, () -> {
                String current = AnkiConfig.getPreferredItemEditor();
                AnkiConfig.setPreferredItemEditor("advanced".equalsIgnoreCase(current) ? "simple" : "advanced");
            }, true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.smart_entity_key", () -> this.onOff(AnkiConfig.isSmartEntityEditorKey()), () -> AnkiConfig.setSmartEntityEditorKey(!AnkiConfig.isSmartEntityEditorKey()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.entity_live_preview", () -> this.onOff(AnkiConfig.isEntityLivePreview()), () -> AnkiConfig.setEntityLivePreview(!AnkiConfig.isEntityLivePreview()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.villager_require_prof", () -> this.onOff(AnkiConfig.isVillagerRequireProfession()), () -> AnkiConfig.setVillagerRequireProfession(!AnkiConfig.isVillagerRequireProfession()), true));
            this.buttons.add(new UiBtn(left, y += rowH + gap, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.open_group_editor").getString(), () -> Minecraft.getInstance().setScreen((Screen)new CustomItemGroupsScreen(this)), true, null, true));
            y += rowH + gap;
        }
        if (this.tab == Tab.KEYS) {
            this.buttons.add(new UiBtn(left, y, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.open_controls").getString(), this::openControlsMenu, true, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + gap, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.reset_keys").getString(), () -> {
                this.resetDefaultKeys();
                this.setStatus((Component)Component.translatable((String)"ankinbt.config.reset_done"), -14498466);
            }, true, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + 10, rowW, rowH, this::keyInfoLine1, () -> {}, false, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + 4, rowW, rowH, this::keyInfoLine2, () -> {}, false, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + 4, rowW, rowH, this::keyInfoLine3, () -> {}, false, null, true));
            y += rowH + 4;
        }
        if (this.tab == Tab.UI) {
            this.buttons.add(this.toggleBtn(left, y, rowW, rowH, "ankinbt.config.ui_opacity", this::uiOpacityText, this::cycleUiOpacity, true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.ui_accent", this::accentText, this::cycleAccentPreset, true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.ui_shadow", () -> this.onOff(AnkiConfig.isUiShadowEnabled()), () -> AnkiConfig.setUiShadowEnabled(!AnkiConfig.isUiShadowEnabled()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.ui_compact", () -> this.onOff(AnkiConfig.isUiCompactLayout()), () -> {
                AnkiConfig.setUiCompactLayout(!AnkiConfig.isUiCompactLayout());
                this.rebuildButtons();
            }, true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.ui_anim", () -> this.onOff(AnkiConfig.isUiAnimationEnabled()), () -> AnkiConfig.setUiAnimationEnabled(!AnkiConfig.isUiAnimationEnabled()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.ui_anim_speed", this::uiAnimSpeedText, this::cycleUiAnimationSpeed, true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.ui_sound_volume", this::uiSoundVolumeText, this::cycleUiSoundVolume, true));
            this.buttons.add(new UiBtn(left, y += rowH + gap, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.open_group_editor").getString(), () -> Minecraft.getInstance().setScreen((Screen)new CustomItemGroupsScreen(this)), true, null, true));
            y += rowH + gap;
        }
        if (this.tab == Tab.ADVANCED) {
            this.buttons.add(this.toggleBtn(left, y, rowW, rowH, "ankinbt.config.confirm_close", () -> this.onOff(AnkiConfig.isConfirmOnClose()), () -> AnkiConfig.setConfirmOnClose(!AnkiConfig.isConfirmOnClose()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.auto_load", () -> this.onOff(AnkiConfig.isAutoLoadLastNbt()), () -> AnkiConfig.setAutoLoadLastNbt(!AnkiConfig.isAutoLoadLastNbt()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.native_file_dialog", () -> this.onOff(AnkiConfig.isNativeFileDialogEnabled()), () -> AnkiConfig.setNativeFileDialogEnabled(!AnkiConfig.isNativeFileDialogEnabled()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.attribute_notes", () -> this.onOff(AnkiConfig.isAttributeNotesEnabled()), () -> AnkiConfig.setAttributeNotesEnabled(!AnkiConfig.isAttributeNotesEnabled()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.tree_expanded", () -> this.onOff(AnkiConfig.isTreeExpandedByDefault()), () -> AnkiConfig.setTreeExpandedByDefault(!AnkiConfig.isTreeExpandedByDefault()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.advanced_tags", () -> this.onOff(AnkiConfig.showAdvancedTags()), () -> AnkiConfig.setShowAdvancedTags(!AnkiConfig.showAdvancedTags()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.config_show_advanced", () -> this.onOff(AnkiConfig.isConfigShowAdvanced()), () -> AnkiConfig.setConfigShowAdvanced(!AnkiConfig.isConfigShowAdvanced()), true));
            this.buttons.add(new UiBtn(left, y += rowH + gap, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.clear_recent_items").getString(), () -> {
                AnkiConfig.clearRecentItemIds();
                this.setStatus((Component)Component.translatable((String)"ankinbt.config.reset_done"), -14498466);
            }, true, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + gap, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.reset_item_groups").getString(), () -> {
                AnkiConfig.resetCustomItemGroups();
                this.setStatus((Component)Component.translatable((String)"ankinbt.config.reset_done"), -14498466);
            }, true, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + gap, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.open_group_editor").getString(), () -> Minecraft.getInstance().setScreen((Screen)new CustomItemGroupsScreen(this)), true, null, true));
            y += rowH + gap;
        }
        if (this.tab == Tab.DEBUG) {
            this.buttons.add(this.toggleBtn(left, y, rowW, rowH, "ankinbt.config.debug.panel", () -> this.onOff(AnkiConfig.isDebugPanelEnabled()), () -> AnkiConfig.setDebugPanelEnabled(!AnkiConfig.isDebugPanelEnabled()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.debug.log", () -> this.onOff(AnkiConfig.isDebugLogEnabled()), () -> AnkiConfig.setDebugLogEnabled(!AnkiConfig.isDebugLogEnabled()), true));
            this.buttons.add(this.toggleBtn(left, y += rowH + gap, rowW, rowH, "ankinbt.config.debug.file_log", () -> this.onOff(AnkiConfig.isDebugFileSaveEnabled()), () -> AnkiConfig.setDebugFileSaveEnabled(!AnkiConfig.isDebugFileSaveEnabled()), true));
            this.buttons.add(new UiBtn(left, y += rowH + gap, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.debug.permission", (Object[])new Object[]{this.debugPermissionText()}).getString(), () -> {}, false, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + 4, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.debug.gamemode", (Object[])new Object[]{this.debugGamemodeText()}).getString(), () -> {}, false, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + 4, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.debug.server", (Object[])new Object[]{this.debugServerText()}).getString(), () -> {}, false, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + 4, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.debug.connection", (Object[])new Object[]{this.debugConnectionText()}).getString(), () -> {}, false, null, true));
            this.buttons.add(new UiBtn(left, y += rowH + gap, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.debug.clear_logs").getString(), DebugLog::clear, true, null, true));
            List<String> logs = DebugLog.snapshot();
            this.buttons.add(new UiBtn(left, y += rowH + gap, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.debug.logs", (Object[])new Object[]{String.valueOf(logs.size())}).getString(), () -> {}, false, null, true));
            y += rowH + 4;
            if (logs.isEmpty()) {
                this.buttons.add(new UiBtn(left, y, rowW, rowH, () -> Component.translatable((String)"ankinbt.config.debug.empty_logs").getString(), () -> {}, false, null, true));
                y += rowH + 4;
            } else {
                int start;
                int i = start = Math.max(0, logs.size() - 24);
                while (i < logs.size()) {
                    int idx = i++;
                    this.buttons.add(new UiBtn(left, y, rowW, rowH, () -> (String)logs.get(idx), () -> {}, false, null, true));
                    y += rowH + 4;
                }
            }
        }
        int visibleH = this.contentBottom - this.contentTop;
        this.maxScroll = Math.max(0, y - this.contentTop - visibleH + 8);
        this.targetScroll = Math.max(0.0f, Math.min(this.targetScroll, (float)this.maxScroll));
        this.scroll = Math.max(0.0f, Math.min(this.scroll, (float)this.maxScroll));
        int bottomY = this.py + this.ph - 30;
        int half = (rowW - 8) / 2;
        this.buttons.add(new UiBtn(left, bottomY, half, 20, () -> Component.translatable((String)"ankinbt.config.reset_defaults").getString(), () -> {
            this.resetDefaults();
            this.rebuildButtons();
            this.setStatus((Component)Component.translatable((String)"ankinbt.config.reset_done"), -14498466);
        }, true, null, false));
        this.buttons.add(new UiBtn(left + half + 8, bottomY, half, 20, () -> Component.translatable((String)"ankinbt.edit.cancel").getString(), this::onClose, true, null, false));
    }

    private UiBtn toggleBtn(int x, int y, int w, int h, String leftKey, Supplier<String> rightValue, Runnable onClick, boolean scrollable) {
        return new UiBtn(x, y, w, h, () -> Component.translatable((String)leftKey, (Object[])new Object[]{rightValue.get()}).getString(), onClick, true, null, scrollable);
    }

    private String modeName() {
        return "advanced".equalsIgnoreCase(AnkiConfig.getPreferredItemEditor()) ? Component.translatable((String)"ankinbt.config.mode.advanced").getString() : Component.translatable((String)"ankinbt.config.mode.simple").getString();
    }

    private String onOff(boolean v) {
        return v ? Component.translatable((String)"ankinbt.simple.on").getString() : Component.translatable((String)"ankinbt.simple.off").getString();
    }

    private void resetDefaultKeys() {
        AnkiConfig.setOpenItemEditorKeyCode(78);
        AnkiConfig.setOpenEntityEditorKeyCode(44);
        AnkiConfig.setOpenVillagerEditorKeyCode(44);
        AnkiConfig.setOpenConfigMenuKeyCode(79);
    }

    private void resetDefaults() {
        AnkiConfig.setPreferredItemEditor("simple");
        AnkiConfig.setConfirmOnClose(true);
        AnkiConfig.setAutoLoadLastNbt(true);
        AnkiConfig.setNativeFileDialogEnabled(false);
        AnkiConfig.setAttributeNotesEnabled(false);
        AnkiConfig.setTreeExpandedByDefault(false);
        AnkiConfig.setShowAdvancedTags(false);
        AnkiConfig.setSmartEntityEditorKey(true);
        AnkiConfig.setVillagerRequireProfession(true);
        AnkiConfig.setEntityLivePreview(true);
        AnkiConfig.setConfigShowAdvanced(false);
        AnkiConfig.setUiOpacity(0.85f);
        AnkiConfig.setUiAccentPreset(0);
        AnkiConfig.setUiShadowEnabled(true);
        AnkiConfig.setUiCompactLayout(false);
        AnkiConfig.setUiAnimationEnabled(true);
        AnkiConfig.setUiAnimationSpeedLevel(3);
        AnkiConfig.setDebugPanelEnabled(true);
        AnkiConfig.setDebugLogEnabled(false);
        AnkiConfig.setDebugFileSaveEnabled(false);
        this.resetDefaultKeys();
    }

    private String uiOpacityText() {
        return String.valueOf(Math.round(AnkiConfig.getUiOpacity() * 100.0f));
    }

    private String accentText() {
        int idx = AnkiConfig.getUiAccentPreset();
        String key = switch (idx) {
            case 1 -> "ankinbt.config.ui_accent.green";
            case 2 -> "ankinbt.config.ui_accent.orange";
            case 3 -> "ankinbt.config.ui_accent.rose";
            default -> "ankinbt.config.ui_accent.blue";
        };
        return Component.translatable((String)key).getString();
    }

    private void cycleUiOpacity() {
        float current = AnkiConfig.getUiOpacity();
        float next = current >= 0.95f ? 0.35f : current + 0.05f;
        AnkiConfig.setUiOpacity(next);
    }

    private void cycleAccentPreset() {
        AnkiConfig.setUiAccentPreset(AnkiConfig.getUiAccentPreset() + 1);
    }

    private String uiAnimSpeedText() {
        return String.valueOf(AnkiConfig.getUiAnimationSpeedLevel());
    }

    private void cycleUiAnimationSpeed() {
        int next = AnkiConfig.getUiAnimationSpeedLevel() >= 10 ? 1 : AnkiConfig.getUiAnimationSpeedLevel() + 1;
        AnkiConfig.setUiAnimationSpeedLevel(next);
    }

    private String uiSoundVolumeText() {
        return String.valueOf(Math.round(AnkiConfig.getUiSoundVolume() * 100.0f));
    }

    private void cycleUiSoundVolume() {
        float now = AnkiConfig.getUiSoundVolume();
        float next = now >= 0.99f ? 0.0f : now + 0.1f;
        AnkiConfig.setUiSoundVolume(next);
    }

    private void openControlsMenu() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            this.setStatus((Component)Component.translatable((String)"ankinbt.config.controls_open_failed"), -7035976);
            return;
        }
        try {
            mc.setScreen((Screen)new ControlsScreen((Screen)this, mc.options));
            return;
        }
        catch (Throwable directErr) {
            DebugLog.warn("Open ControlsScreen directly failed: {}", directErr.toString());
            try {
                String[] candidates;
                for (String className : candidates = new String[]{"net.minecraft.client.gui.screens.options.controls.ControlsScreen", "net.minecraft.client.gui.screens.options.controls.KeyBindsScreen"}) {
                    Class<?> controlsClass = Class.forName(className);
                    for (Constructor<?> ctor : controlsClass.getConstructors()) {
                        Object screen;
                        Class<?>[] p = ctor.getParameterTypes();
                        if (p.length != 2 || !Screen.class.isAssignableFrom(p[0]) || !p[1].isAssignableFrom(mc.options.getClass()) || !((screen = ctor.newInstance(new Object[]{this, mc.options})) instanceof Screen)) continue;
                        Screen s = (Screen)screen;
                        mc.setScreen(s);
                        return;
                    }
                }
            }
            catch (Throwable reflectErr) {
                DebugLog.warn("Open controls menu via reflection failed: {}", reflectErr.toString());
            }
            try {
                mc.setScreen((Screen)new OptionsScreen((Screen)this, mc.options));
                this.setStatus((Component)Component.translatable((String)"ankinbt.config.controls_open_failed"), -7035976);
                return;
            }
            catch (Throwable fallbackErr) {
                DebugLog.warn("Fallback OptionsScreen open failed: {}", fallbackErr.toString());
                this.setStatus((Component)Component.translatable((String)"ankinbt.config.controls_open_failed"), -7035976);
                return;
            }
        }
    }

    private void setStatus(Component msg, int color) {
        this.status = msg;
        this.statusColor = color;
        this.statusTime = System.currentTimeMillis();
    }

    private String keyInfoLine1() {
        return Component.translatable((String)"ankinbt.config.key.item").getString() + ": " + VersionCompat.get().getKeyDisplayName(AnkiConfig.getOpenItemEditorKeyCode());
    }

    private String keyInfoLine2() {
        String label = Component.translatable((String)"ankinbt.config.key.entity").getString() + " / " + Component.translatable((String)"ankinbt.config.key.villager").getString();
        return label + ": " + VersionCompat.get().getKeyDisplayName(AnkiConfig.getOpenEntityEditorKeyCode());
    }

    private String keyInfoLine3() {
        return Component.translatable((String)"ankinbt.config.key.menu").getString() + ": " + VersionCompat.get().getKeyDisplayName(AnkiConfig.getOpenConfigMenuKeyCode());
    }

    private String debugPermissionText() {
        Minecraft mc = Minecraft.getInstance();
        return this.boolText(EditorCommandHelper.canUseEntityCommand(mc));
    }

    private String debugGamemodeText() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return Component.translatable((String)"ankinbt.config.debug.unknown").getString();
        }
        String creative = this.boolText(mc.player.isCreative());
        String spectator = this.boolText(mc.player.isSpectator());
        return Component.translatable((String)"ankinbt.config.debug.gamemode.detail", (Object[])new Object[]{creative, spectator}).getString();
    }

    private String debugServerText() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return Component.translatable((String)"ankinbt.config.debug.unknown").getString();
        }
        if (this.hasSingleplayerServer(mc)) {
            return Component.translatable((String)"ankinbt.config.debug.server.local").getString();
        }
        String remote = this.currentServerName(mc);
        if (!remote.isBlank()) {
            return remote;
        }
        return Component.translatable((String)"ankinbt.config.debug.unknown").getString();
    }

    private String debugConnectionText() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return Component.translatable((String)"ankinbt.config.debug.unknown").getString();
        }
        boolean online = mc.getConnection() != null;
        String level = mc.level == null ? Component.translatable((String)"ankinbt.config.debug.unknown").getString() : this.dimensionKeyText(mc.level.dimension());
        return Component.translatable((String)"ankinbt.config.debug.connection.detail", (Object[])new Object[]{this.boolText(online), level}).getString();
    }

    private String dimensionKeyText(Object dimensionKey) {
        Object out2;
        if (dimensionKey == null) {
            return Component.translatable((String)"ankinbt.config.debug.unknown").getString();
        }
        try {
            out2 = dimensionKey.getClass().getMethod("location", new Class[0]).invoke(dimensionKey, new Object[0]);
            if (out2 != null) {
                return String.valueOf(out2);
            }
        }
        catch (Throwable t) {
            // empty catch block
        }
        try {
            out2 = dimensionKey.getClass().getMethod("identifier", new Class[0]).invoke(dimensionKey, new Object[0]);
            if (out2 != null) {
                return String.valueOf(out2);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return String.valueOf(dimensionKey);
    }

    private boolean hasSingleplayerServer(Minecraft mc) {
        try {
            Object out = mc.getClass().getMethod("hasSingleplayerServer", new Class[0]).invoke((Object)mc, new Object[0]);
            if (out instanceof Boolean) {
                Boolean b = (Boolean)out;
                return b;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private String currentServerName(Minecraft mc) {
        try {
            Object name2;
            Object server = mc.getClass().getMethod("getCurrentServer", new Class[0]).invoke((Object)mc, new Object[0]);
            if (server == null) {
                return "";
            }
            try {
                name2 = server.getClass().getField("name").get(server);
                if (name2 != null) {
                    return String.valueOf(name2);
                }
            }
            catch (Throwable t) {
                // empty catch block
            }
            name2 = server.getClass().getMethod("name", new Class[0]).invoke(server, new Object[0]);
            return name2 == null ? "" : String.valueOf(name2);
        }
        catch (Throwable ignored) {
            return "";
        }
    }

    private String boolText(boolean value) {
        return value ? Component.translatable((String)"ankinbt.config.debug.yes").getString() : Component.translatable((String)"ankinbt.config.debug.no").getString();
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) {
            return false;
        }
        int offset = -Math.round(this.scroll);
        for (UiBtn btn : this.buttons) {
            if (!btn.click((int)mx, (int)my, offset, this.contentTop, this.contentBottom)) continue;
            if (btn.enabled) {
                this.rebuildButtons();
            }
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (mx >= (double)(this.px + 10) && mx <= (double)(this.px + this.pw - 10) && my >= (double)this.contentTop && my <= (double)this.contentBottom && this.maxScroll > 0) {
            this.targetScroll -= (float)sy * 24.0f;
            this.targetScroll = Math.max(0.0f, Math.min((float)this.maxScroll, this.targetScroll));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        this.recalcBounds();
        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        this.openAnim = UiTheme.approach(this.openAnim, 1.0f, speed);
        this.scroll = UiTheme.approach(this.scroll, this.targetScroll, Math.min(1.0f, speed * 2.4f));
        float opacity = AnkiConfig.getUiOpacity();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());
        int scrim = UiTheme.scrim(opacity, this.openAnim);
        int panel = UiTheme.panel(opacity, this.openAnim);
        int card = UiTheme.card(opacity, this.openAnim);
        int header = UiTheme.header(opacity, this.openAnim);
        int border = UiTheme.border(opacity, this.openAnim);
        int shadow = UiTheme.shadow(opacity, this.openAnim, AnkiConfig.isUiShadowEnabled());
        g.fill(0, 0, this.width, this.height, scrim);
        if (shadow != 0) {
            g.fill(this.px + 4, this.py + 4, this.px + this.pw + 4, this.py + this.ph + 4, shadow);
        }
        g.fill(this.px, this.py, this.px + this.pw, this.py + this.ph, panel);
        this.border(g, this.px, this.py, this.pw, this.ph, border);
        g.fill(this.px + 1, this.py + 1, this.px + this.pw - 1, this.py + 34, header);
        g.fill(this.px + 1, this.py + 34, this.px + this.pw - 1, this.py + 35, border);
        g.fill(this.px + 1, this.py + 60, this.px + this.pw - 1, this.py + this.ph - 40, card);
        VersionCompat.get().drawString(g, this.font, this.title, this.px + 12, this.py + 12, -1906448, false);
        VersionCompat.get().drawString(g, this.font, (Component)Component.translatable((String)"ankinbt.config.tip.controls"), this.px + 200, this.py + 13, -7035976, false);
        String tabTitle = this.tab == Tab.GENERAL ? Component.translatable((String)"ankinbt.config.section.editor").getString() : (this.tab == Tab.KEYS ? Component.translatable((String)"ankinbt.config.section.quick").getString() : (this.tab == Tab.UI ? Component.translatable((String)"ankinbt.config.section.ui").getString() : (this.tab == Tab.DEBUG ? Component.translatable((String)"ankinbt.config.section.debug").getString() : Component.translatable((String)"ankinbt.config.section.behavior").getString())));
        VersionCompat.get().drawString(g, this.font, tabTitle, this.px + 18, this.py + 64, accent, false);
        if (this.tab == Tab.KEYS && System.currentTimeMillis() - this.lastKeySync > 250L) {
            this.lastKeySync = System.currentTimeMillis();
            if (KeyBindings.syncConfigFromKeyMappings()) {
                this.rebuildButtons();
            }
        }
        if (this.tab == Tab.DEBUG && System.currentTimeMillis() - this.lastDebugRefresh > 250L) {
            this.lastDebugRefresh = System.currentTimeMillis();
            this.rebuildButtons();
        }
        int offset = -Math.round(this.scroll);
        for (UiBtn btn : this.buttons) {
            btn.render(g, this.font, mx, my, accent, offset, this.contentTop, this.contentBottom);
        }
        if (this.maxScroll > 0) {
            int trackX = this.px + this.pw - 9;
            int trackY = this.contentTop;
            int trackH = this.contentBottom - this.contentTop;
            g.fill(trackX, trackY, trackX + 4, trackY + trackH, UiTheme.withAlpha(0xFFFFFF, 46));
            float ratio = (float)trackH / (float)(trackH + this.maxScroll);
            int thumbH = Math.max(18, (int)((float)trackH * ratio));
            int thumbY = trackY + (int)((float)(trackH - thumbH) * (this.scroll / Math.max(1.0f, (float)this.maxScroll)));
            g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, UiTheme.withAlpha(accent & 0xFFFFFF, 186));
        }
        if (this.status != null && !this.status.getString().isEmpty() && System.currentTimeMillis() - this.statusTime < 2200L) {
            VersionCompat.get().drawString(g, this.font, this.status, this.px + 18, this.py + this.ph - 12, this.statusColor, false);
        }
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
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
        return super.mouseClicked(mx, my, event.button());
    }

    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }

    static enum Tab {
        GENERAL("ankinbt.config.tab.general"),
        KEYS("ankinbt.config.tab.keys"),
        UI("ankinbt.config.tab.ui"),
        ADVANCED("ankinbt.config.tab.advanced"),
        DEBUG("ankinbt.config.tab.debug");

        final String key;

        private Tab(String key) {
            this.key = key;
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
        final boolean scrollable;

        UiBtn(int x, int y, int w, int h, Supplier<String> label, Runnable action, boolean enabled, Supplier<Boolean> selected, boolean scrollable) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
            this.action = action;
            this.enabled = enabled;
            this.selected = selected;
            this.scrollable = scrollable;
        }

        boolean hover(int mx, int my, int offset, int contentTop, int contentBottom) {
            int yy = this.y + (this.scrollable ? offset : 0);
            if (this.scrollable && (yy + this.h < contentTop || yy > contentBottom)) {
                return false;
            }
            return mx >= this.x && mx < this.x + this.w && my >= yy && my < yy + this.h;
        }

        boolean click(int mx, int my, int offset, int contentTop, int contentBottom) {
            if (!this.enabled || !this.hover(mx, my, offset, contentTop, contentBottom)) {
                return false;
            }
            this.action.run();
            UiSound.playClick();
            return true;
        }

        void render(GuiGraphics g, Font font, int mx, int my, int accent, int offset, int contentTop, int contentBottom) {
            boolean chosen;
            int yy = this.y + (this.scrollable ? offset : 0);
            if (this.scrollable && (yy + this.h < contentTop || yy > contentBottom)) {
                return;
            }
            boolean hover = this.hover(mx, my, offset, contentTop, contentBottom);
            boolean bl = chosen = this.selected != null && Boolean.TRUE.equals(this.selected.get());
            int bg = !this.enabled ? UiTheme.withAlpha(1054759, 80) : (chosen ? 0xAA000000 | accent & 0xFFFFFF : (hover ? UiTheme.withAlpha(0xFFFFFF, 82) : UiTheme.withAlpha(0xFFFFFF, 48)));
            int edge = chosen ? accent : UiTheme.withAlpha(0x222236, 255);
            int color = this.enabled ? -1906448 : -7035976;
            g.fill(this.x, yy, this.x + this.w, yy + this.h, bg);
            g.fill(this.x, yy, this.x + this.w, yy + 1, edge);
            g.fill(this.x, yy + this.h - 1, this.x + this.w, yy + this.h, edge);
            g.fill(this.x, yy, this.x + 1, yy + this.h, edge);
            g.fill(this.x + this.w - 1, yy, this.x + this.w, yy + this.h, edge);
            Object text = this.label.get();
            if (font.width((String)text) > this.w - 10) {
                text = font.plainSubstrByWidth((String)text, this.w - 14) + "..";
            }
            VersionCompat.get().drawString(g, font, (String)text, this.x + 6, yy + 7, color, false);
        }
    }
}
