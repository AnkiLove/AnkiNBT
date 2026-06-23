package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.keybind.KeyBindings;
import com.ankinbt.util.DebugLog;
import com.ankinbt.util.UiSound;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import com.ankinbt.compat.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AnkiConfigScreen extends Screen {

    private final Screen parent;
    private final List<UiBtn> buttons = new ArrayList<>();
    private Tab tab = Tab.GENERAL;

    private Component status = Component.empty();
    private int statusColor = UiTheme.TXT_DIM;
    private long statusTime = 0;

    private int px, py, pw, ph;
    private int contentTop;
    private int contentBottom;
    private int maxScroll = 0;
    private float scroll = 0f;
    private float targetScroll = 0f;
    private float openAnim = 0f;
    private long lastDebugRefresh = 0L;
    private long lastKeySync = 0L;

    enum Tab {
        GENERAL("ankinbt.config.tab.general"),
        KEYS("ankinbt.config.tab.keys"),
        UI("ankinbt.config.tab.ui"),
        ADVANCED("ankinbt.config.tab.advanced"),
        DEBUG("ankinbt.config.tab.debug");

        final String key;
        Tab(String key) { this.key = key; }
    }

    public AnkiConfigScreen(Screen parent) {
        super(Component.translatable("ankinbt.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        recalcBounds();
        KeyBindings.syncConfigFromKeyMappings();
        rebuildButtons();
    }

    private void recalcBounds() {
        pw = Math.min(680, width - 24);
        ph = Math.min(460, height - 20);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        contentTop = py + 74;
        contentBottom = py + ph - 44;
    }

    private void rebuildButtons() {
        buttons.clear();

        int tabY = py + 38;
        int tabW = 98;
        int tabH = 20;
        int tx = px + 18;
        for (Tab t : Tab.values()) {
            Tab target = t;
            buttons.add(new UiBtn(tx, tabY, tabW, tabH,
                    () -> Component.translatable(target.key).getString(),
                    () -> {
                        tab = target;
                        targetScroll = 0f;
                        scroll = 0f;
                        rebuildButtons();
                    },
                    true,
                    () -> tab == target,
                    false));
            tx += tabW + 8;
        }

        int left = px + 18;
        int right = px + pw - 18;
        int rowW = right - left;
        int y = contentTop;
        int rowH = AnkiConfig.isUiCompactLayout() ? 20 : 22;
        int gap = AnkiConfig.isUiCompactLayout() ? 4 : 7;

        if (tab == Tab.GENERAL) {
            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.preferred_editor", this::modeName,
                    () -> {
                        String current = AnkiConfig.getPreferredItemEditor();
                        AnkiConfig.setPreferredItemEditor("advanced".equalsIgnoreCase(current) ? "simple" : "advanced");
                    }, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.smart_entity_key", () -> onOff(AnkiConfig.isSmartEntityEditorKey()),
                    () -> AnkiConfig.setSmartEntityEditorKey(!AnkiConfig.isSmartEntityEditorKey()), true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.entity_live_preview", () -> onOff(AnkiConfig.isEntityLivePreview()),
                    () -> AnkiConfig.setEntityLivePreview(!AnkiConfig.isEntityLivePreview()), true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.villager_require_prof", () -> onOff(AnkiConfig.isVillagerRequireProfession()),
                    () -> AnkiConfig.setVillagerRequireProfession(!AnkiConfig.isVillagerRequireProfession()), true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.open_group_editor").getString(),
                    () -> Minecraft.getInstance().setScreenAndShow(new CustomItemGroupsScreen(this)), true, null, true));
            y += rowH + gap;
        }

        if (tab == Tab.KEYS) {
            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.open_controls").getString(),
                    this::openControlsMenu, true, null, true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.reset_keys").getString(),
                    () -> {
                        resetDefaultKeys();
                        setStatus(Component.translatable("ankinbt.config.reset_done"), UiTheme.TXT_OK);
                    }, true, null, true));
            y += rowH + 10;

            buttons.add(new UiBtn(left, y, rowW, rowH, this::keyInfoLine1, () -> {}, false, null, true));
            y += rowH + 4;
            buttons.add(new UiBtn(left, y, rowW, rowH, this::keyInfoLine2, () -> {}, false, null, true));
            y += rowH + 4;
            buttons.add(new UiBtn(left, y, rowW, rowH, this::keyInfoLine3, () -> {}, false, null, true));
            y += rowH + 4;
        }

        if (tab == Tab.UI) {
            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_opacity", this::uiOpacityText, this::cycleUiOpacity, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_accent", this::accentText, this::cycleAccentPreset, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_shadow", () -> onOff(AnkiConfig.isUiShadowEnabled()),
                    () -> AnkiConfig.setUiShadowEnabled(!AnkiConfig.isUiShadowEnabled()), true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_compact", () -> onOff(AnkiConfig.isUiCompactLayout()),
                    () -> {
                        AnkiConfig.setUiCompactLayout(!AnkiConfig.isUiCompactLayout());
                        rebuildButtons();
                    }, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_anim", () -> onOff(AnkiConfig.isUiAnimationEnabled()),
                    () -> AnkiConfig.setUiAnimationEnabled(!AnkiConfig.isUiAnimationEnabled()), true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_anim_speed", this::uiAnimSpeedText,
                    this::cycleUiAnimationSpeed, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_sound_volume", this::uiSoundVolumeText,
                    this::cycleUiSoundVolume, true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.open_group_editor").getString(),
                    () -> Minecraft.getInstance().setScreenAndShow(new CustomItemGroupsScreen(this)), true, null, true));
            y += rowH + gap;
        }

        if (tab == Tab.ADVANCED) {
            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.confirm_close", () -> onOff(AnkiConfig.isConfirmOnClose()),
                    () -> AnkiConfig.setConfirmOnClose(!AnkiConfig.isConfirmOnClose()), true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.auto_load", () -> onOff(AnkiConfig.isAutoLoadLastNbt()),
                    () -> AnkiConfig.setAutoLoadLastNbt(!AnkiConfig.isAutoLoadLastNbt()), true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.tree_expanded", () -> onOff(AnkiConfig.isTreeExpandedByDefault()),
                    () -> AnkiConfig.setTreeExpandedByDefault(!AnkiConfig.isTreeExpandedByDefault()), true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.advanced_tags", () -> onOff(AnkiConfig.showAdvancedTags()),
                    () -> AnkiConfig.setShowAdvancedTags(!AnkiConfig.showAdvancedTags()), true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.config_show_advanced", () -> onOff(AnkiConfig.isConfigShowAdvanced()),
                    () -> AnkiConfig.setConfigShowAdvanced(!AnkiConfig.isConfigShowAdvanced()), true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.clear_recent_items").getString(),
                    () -> {
                        AnkiConfig.clearRecentItemIds();
                        setStatus(Component.translatable("ankinbt.config.reset_done"), UiTheme.TXT_OK);
                    }, true, null, true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.reset_item_groups").getString(),
                    () -> {
                        AnkiConfig.resetCustomItemGroups();
                        setStatus(Component.translatable("ankinbt.config.reset_done"), UiTheme.TXT_OK);
                    }, true, null, true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.open_group_editor").getString(),
                    () -> Minecraft.getInstance().setScreenAndShow(new CustomItemGroupsScreen(this)), true, null, true));
            y += rowH + gap;
        }

        if (tab == Tab.DEBUG) {
            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.debug.panel", () -> onOff(AnkiConfig.isDebugPanelEnabled()),
                    () -> AnkiConfig.setDebugPanelEnabled(!AnkiConfig.isDebugPanelEnabled()), true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.debug.log", () -> onOff(AnkiConfig.isDebugLogEnabled()),
                    () -> AnkiConfig.setDebugLogEnabled(!AnkiConfig.isDebugLogEnabled()), true));
            y += rowH + gap;
            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.debug.file_log", () -> onOff(AnkiConfig.isDebugFileSaveEnabled()),
                    () -> AnkiConfig.setDebugFileSaveEnabled(!AnkiConfig.isDebugFileSaveEnabled()), true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.debug.permission", debugPermissionText()).getString(),
                    () -> {}, false, null, true));
            y += rowH + 4;
            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.debug.gamemode", debugGamemodeText()).getString(),
                    () -> {}, false, null, true));
            y += rowH + 4;
            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.debug.server", debugServerText()).getString(),
                    () -> {}, false, null, true));
            y += rowH + 4;
            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.debug.connection", debugConnectionText()).getString(),
                    () -> {}, false, null, true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.debug.clear_logs").getString(),
                    DebugLog::clear, true, null, true));
            y += rowH + gap;

            var logs = DebugLog.snapshot();
            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.debug.logs", String.valueOf(logs.size())).getString(),
                    () -> {}, false, null, true));
            y += rowH + 4;

            if (logs.isEmpty()) {
                buttons.add(new UiBtn(left, y, rowW, rowH,
                        () -> Component.translatable("ankinbt.config.debug.empty_logs").getString(),
                        () -> {}, false, null, true));
                y += rowH + 4;
            } else {
                int start = Math.max(0, logs.size() - 24);
                for (int i = start; i < logs.size(); i++) {
                    final int idx = i;
                    buttons.add(new UiBtn(left, y, rowW, rowH,
                            () -> logs.get(idx),
                            () -> {}, false, null, true));
                    y += rowH + 4;
                }
            }
        }

        int visibleH = contentBottom - contentTop;
        maxScroll = Math.max(0, y - contentTop - visibleH + 8);
        targetScroll = Math.max(0, Math.min(targetScroll, maxScroll));
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        int bottomY = py + ph - 30;
        int half = (rowW - 8) / 2;

        buttons.add(new UiBtn(left, bottomY, half, 20,
                () -> Component.translatable("ankinbt.config.reset_defaults").getString(),
                () -> {
                    resetDefaults();
                    rebuildButtons();
                    setStatus(Component.translatable("ankinbt.config.reset_done"), UiTheme.TXT_OK);
                }, true, null, false));

        buttons.add(new UiBtn(left + half + 8, bottomY, half, 20,
                () -> Component.translatable("ankinbt.edit.cancel").getString(),
                this::onClose, true, null, false));
    }

    private UiBtn toggleBtn(int x, int y, int w, int h, String leftKey, Supplier<String> rightValue, Runnable onClick, boolean scrollable) {
        return new UiBtn(x, y, w, h,
                () -> Component.translatable(leftKey, rightValue.get()).getString(),
                onClick, true, null, scrollable);
    }

    private String modeName() {
        return "advanced".equalsIgnoreCase(AnkiConfig.getPreferredItemEditor())
                ? Component.translatable("ankinbt.config.mode.advanced").getString()
                : Component.translatable("ankinbt.config.mode.simple").getString();
    }

    private String onOff(boolean v) {
        return v ? Component.translatable("ankinbt.simple.on").getString()
                : Component.translatable("ankinbt.simple.off").getString();
    }

    private void resetDefaultKeys() {
        AnkiConfig.setOpenItemEditorKeyCode(InputConstants.KEY_N);
        AnkiConfig.setOpenEntityEditorKeyCode(InputConstants.KEY_COMMA);
        AnkiConfig.setOpenVillagerEditorKeyCode(InputConstants.KEY_COMMA);
        AnkiConfig.setOpenConfigMenuKeyCode(InputConstants.KEY_O);
    }

    private void resetDefaults() {
        AnkiConfig.setPreferredItemEditor("simple");
        AnkiConfig.setConfirmOnClose(true);
        AnkiConfig.setAutoLoadLastNbt(true);
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

        resetDefaultKeys();
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
        return Component.translatable(key).getString();
    }

    private void cycleUiOpacity() {
        float current = AnkiConfig.getUiOpacity();
        float next = current >= 0.95f ? 0.35f : (current + 0.05f);
        AnkiConfig.setUiOpacity(next);
    }

    private void cycleAccentPreset() {
        AnkiConfig.setUiAccentPreset(AnkiConfig.getUiAccentPreset() + 1);
    }

    private String uiAnimSpeedText() {
        return String.valueOf(AnkiConfig.getUiAnimationSpeedLevel());
    }

    private void cycleUiAnimationSpeed() {
        int next = AnkiConfig.getUiAnimationSpeedLevel() >= 10 ? 1 : (AnkiConfig.getUiAnimationSpeedLevel() + 1);
        AnkiConfig.setUiAnimationSpeedLevel(next);
    }

    private String uiSoundVolumeText() {
        return String.valueOf(Math.round(AnkiConfig.getUiSoundVolume() * 100.0f));
    }

    private void cycleUiSoundVolume() {
        float now = AnkiConfig.getUiSoundVolume();
        float next = now >= 0.99f ? 0.0f : (now + 0.1f);
        AnkiConfig.setUiSoundVolume(next);
    }

    private void openControlsMenu() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            setStatus(Component.translatable("ankinbt.config.controls_open_failed"), UiTheme.TXT_DIM);
            return;
        }
        try {
            mc.setScreenAndShow(new net.minecraft.client.gui.screens.options.controls.ControlsScreen(this, mc.options));
            return;
        } catch (Throwable directErr) {
            DebugLog.warn("Open ControlsScreen directly failed: {}", directErr.toString());
        }
        try {
            String[] candidates = new String[]{
                    "net.minecraft.client.gui.screens.options.controls.ControlsScreen",
                    "net.minecraft.client.gui.screens.options.controls.KeyBindsScreen"
            };
            for (String className : candidates) {
                Class<?> controlsClass = Class.forName(className);
                for (Constructor<?> ctor : controlsClass.getConstructors()) {
                    Class<?>[] p = ctor.getParameterTypes();
                    if (p.length == 2 && Screen.class.isAssignableFrom(p[0]) && p[1].isAssignableFrom(mc.options.getClass())) {
                        Object screen = ctor.newInstance(this, mc.options);
                        if (screen instanceof Screen s) {
                            mc.setScreenAndShow(s);
                            return;
                        }
                    }
                }
            }
        } catch (Throwable reflectErr) {
            DebugLog.warn("Open controls menu via reflection failed: {}", reflectErr.toString());
        }
        try {
            mc.setScreenAndShow(new OptionsScreen(this, mc.options, false));
            setStatus(Component.translatable("ankinbt.config.controls_open_failed"), UiTheme.TXT_DIM);
            return;
        } catch (Throwable fallbackErr) {
            DebugLog.warn("Fallback OptionsScreen open failed: {}", fallbackErr.toString());
        }
        setStatus(Component.translatable("ankinbt.config.controls_open_failed"), UiTheme.TXT_DIM);
    }

    private void setStatus(Component msg, int color) {
        status = msg;
        statusColor = color;
        statusTime = System.currentTimeMillis();
    }

    private String keyInfoLine1() {
        return Component.translatable("ankinbt.config.key.item").getString() + ": " + VersionCompat.get().getKeyDisplayName(AnkiConfig.getOpenItemEditorKeyCode());
    }

    private String keyInfoLine2() {
        String label = Component.translatable("ankinbt.config.key.entity").getString()
                + " / "
                + Component.translatable("ankinbt.config.key.villager").getString();
        return label + ": " + VersionCompat.get().getKeyDisplayName(AnkiConfig.getOpenEntityEditorKeyCode());
    }

    private String keyInfoLine3() {
        return Component.translatable("ankinbt.config.key.menu").getString() + ": " + VersionCompat.get().getKeyDisplayName(AnkiConfig.getOpenConfigMenuKeyCode());
    }

    private String debugPermissionText() {
        Minecraft mc = Minecraft.getInstance();
        return boolText(EditorCommandHelper.canUseEntityCommand(mc));
    }

    private String debugGamemodeText() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return Component.translatable("ankinbt.config.debug.unknown").getString();
        String creative = boolText(mc.player.isCreative());
        String spectator = boolText(mc.player.isSpectator());
        return Component.translatable("ankinbt.config.debug.gamemode.detail", creative, spectator).getString();
    }

    private String debugServerText() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return Component.translatable("ankinbt.config.debug.unknown").getString();
        if (hasSingleplayerServer(mc)) return Component.translatable("ankinbt.config.debug.server.local").getString();
        String remote = currentServerName(mc);
        if (!remote.isBlank()) return remote;
        return Component.translatable("ankinbt.config.debug.unknown").getString();
    }

    private String debugConnectionText() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return Component.translatable("ankinbt.config.debug.unknown").getString();
        boolean online = mc.getConnection() != null;
        String level = mc.level == null
                ? Component.translatable("ankinbt.config.debug.unknown").getString()
                : dimensionKeyText(mc.level.dimension());
        return Component.translatable("ankinbt.config.debug.connection.detail", boolText(online), level).getString();
    }

    private String dimensionKeyText(Object dimensionKey) {
        if (dimensionKey == null) return Component.translatable("ankinbt.config.debug.unknown").getString();
        try {
            Object out = dimensionKey.getClass().getMethod("location").invoke(dimensionKey);
            if (out != null) return String.valueOf(out);
        } catch (Throwable ignored) {}
        try {
            Object out = dimensionKey.getClass().getMethod("identifier").invoke(dimensionKey);
            if (out != null) return String.valueOf(out);
        } catch (Throwable ignored) {}
        return String.valueOf(dimensionKey);
    }

    private boolean hasSingleplayerServer(Minecraft mc) {
        try {
            Object out = mc.getClass().getMethod("hasSingleplayerServer").invoke(mc);
            if (out instanceof Boolean b) return b;
        } catch (Throwable ignored) {}
        return false;
    }

    private String currentServerName(Minecraft mc) {
        try {
            Object server = mc.getClass().getMethod("getCurrentServer").invoke(mc);
            if (server == null) return "";
            try {
                Object name = server.getClass().getField("name").get(server);
                if (name != null) return String.valueOf(name);
            } catch (Throwable ignored) {}
            Object name = server.getClass().getMethod("name").invoke(server);
            return name == null ? "" : String.valueOf(name);
        } catch (Throwable ignored) {
            return "";
        }
    }

    private String boolText(boolean value) {
        return value ? Component.translatable("ankinbt.config.debug.yes").getString()
                : Component.translatable("ankinbt.config.debug.no").getString();
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return false;
        int offset = -(int) Math.round(scroll);
        for (UiBtn btn : buttons) {
            if (btn.click((int) mx, (int) my, offset, contentTop, contentBottom)) {
                if (btn.enabled) rebuildButtons();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (mx >= px + 10 && mx <= px + pw - 10 && my >= contentTop && my <= contentBottom && maxScroll > 0) {
            targetScroll -= (float) sy * 24.0f;
            targetScroll = Math.max(0f, Math.min(maxScroll, targetScroll));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        render(new com.ankinbt.compat.GuiGraphics(g), mx, my, partialTick);
    }

    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        recalcBounds();

        float speed = AnkiConfig.isUiAnimationEnabled() ? AnkiConfig.getUiAnimationSpeed() : 1.0f;
        openAnim = UiTheme.approach(openAnim, 1.0f, speed);
        scroll = UiTheme.approach(scroll, targetScroll, Math.min(1.0f, speed * 2.4f));

        float opacity = AnkiConfig.getUiOpacity();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());

        int scrim = UiTheme.scrim(opacity, openAnim);
        int panel = UiTheme.panel(opacity, openAnim);
        int card = UiTheme.card(opacity, openAnim);
        int header = UiTheme.header(opacity, openAnim);
        int border = UiTheme.border(opacity, openAnim);
        int shadow = UiTheme.shadow(opacity, openAnim, AnkiConfig.isUiShadowEnabled());

        g.fill(0, 0, width, height, scrim);

        if (shadow != 0) g.fill(px + 4, py + 4, px + pw + 4, py + ph + 4, shadow);
        g.fill(px, py, px + pw, py + ph, panel);
        border(g, px, py, pw, ph, border);

        g.fill(px + 1, py + 1, px + pw - 1, py + 34, header);
        g.fill(px + 1, py + 34, px + pw - 1, py + 35, border);
        g.fill(px + 1, py + 60, px + pw - 1, py + ph - 40, card);

        com.ankinbt.compat.VersionCompat.get().drawString(g, font, title, px + 12, py + 12, UiTheme.TXT_TITLE, false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, Component.translatable("ankinbt.config.tip.controls"), px + 200, py + 13, UiTheme.TXT_DIM, false);

        String tabTitle = tab == Tab.GENERAL
                ? Component.translatable("ankinbt.config.section.editor").getString()
                : tab == Tab.KEYS
                ? Component.translatable("ankinbt.config.section.quick").getString()
                : tab == Tab.UI
                ? Component.translatable("ankinbt.config.section.ui").getString()
                : tab == Tab.DEBUG
                ? Component.translatable("ankinbt.config.section.debug").getString()
                : Component.translatable("ankinbt.config.section.behavior").getString();
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, tabTitle, px + 18, py + 64, accent, false);

        if (tab == Tab.KEYS && System.currentTimeMillis() - lastKeySync > 250) {
            lastKeySync = System.currentTimeMillis();
            if (KeyBindings.syncConfigFromKeyMappings()) rebuildButtons();
        }

        if (tab == Tab.DEBUG && System.currentTimeMillis() - lastDebugRefresh > 250) {
            lastDebugRefresh = System.currentTimeMillis();
            rebuildButtons();
        }

        int offset = -(int) Math.round(scroll);
        for (UiBtn btn : buttons) {
            btn.render(g, font, mx, my, accent, offset, contentTop, contentBottom);
        }

        if (maxScroll > 0) {
            int trackX = px + pw - 9;
            int trackY = contentTop;
            int trackH = contentBottom - contentTop;
            g.fill(trackX, trackY, trackX + 4, trackY + trackH, UiTheme.withAlpha(0xFFFFFF, 46));
            float ratio = (float) trackH / (trackH + maxScroll);
            int thumbH = Math.max(18, (int) (trackH * ratio));
            int thumbY = trackY + (int) ((trackH - thumbH) * (scroll / Math.max(1f, (float) maxScroll)));
            g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, UiTheme.withAlpha(accent & 0x00FFFFFF, 186));
        }

        if (status != null && !status.getString().isEmpty() && System.currentTimeMillis() - statusTime < 2200) {
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, status, px + 18, py + ph - 12, statusColor, false);
        }
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
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
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(parent);
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
            int yy = y + (scrollable ? offset : 0);
            if (scrollable && (yy + h < contentTop || yy > contentBottom)) return false;
            return mx >= x && mx < x + w && my >= yy && my < yy + h;
        }

        boolean click(int mx, int my, int offset, int contentTop, int contentBottom) {
            if (!enabled || !hover(mx, my, offset, contentTop, contentBottom)) return false;
            action.run();
            UiSound.playClick();
            return true;
        }

        void render(GuiGraphics g, net.minecraft.client.gui.Font font, int mx, int my, int accent, int offset, int contentTop, int contentBottom) {
            int yy = y + (scrollable ? offset : 0);
            if (scrollable && (yy + h < contentTop || yy > contentBottom)) return;

            boolean hover = hover(mx, my, offset, contentTop, contentBottom);
            boolean chosen = selected != null && Boolean.TRUE.equals(selected.get());

            int bg = !enabled ? UiTheme.withAlpha(0x101827, 80) : chosen ? (0xAA000000 | (accent & 0x00FFFFFF)) : hover ? UiTheme.withAlpha(0xFFFFFF, 82) : UiTheme.withAlpha(0xFFFFFF, 48);
            int edge = chosen ? accent : UiTheme.withAlpha(0x222236, 255);
            int color = enabled ? UiTheme.TXT_MAIN : UiTheme.TXT_DIM;

            g.fill(x, yy, x + w, yy + h, bg);
            g.fill(x, yy, x + w, yy + 1, edge);
            g.fill(x, yy + h - 1, x + w, yy + h, edge);
            g.fill(x, yy, x + 1, yy + h, edge);
            g.fill(x + w - 1, yy, x + w, yy + h, edge);

            String text = label.get();
            if (font.width(text) > w - 10) text = font.plainSubstrByWidth(text, w - 14) + "..";
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, x + 6, yy + 7, color, false);
        }
    }
}



