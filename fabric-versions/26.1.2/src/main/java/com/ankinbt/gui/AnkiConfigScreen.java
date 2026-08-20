package com.ankinbt.gui;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.EditorCommandHelper;
import com.ankinbt.keybind.KeyBindings;
import com.ankinbt.util.DebugLog;
import com.ankinbt.util.UiSound;
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
    private float tabAnim = 0f;
    private float closeHoverAnim = 0f;
    private boolean draggingPanel;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean resizingEditor;
    private boolean editorSizeFocused;
    private float editorScale = EditorDock.DEFAULT_EDITOR_SCALE;
    private float editorWidthAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
    private float editorHeightAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
    private float sizeControlHoverAnim;
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
        editorScale = AnkiConfig.getConfigScreenScale();
        editorWidthAdjustment = AnkiConfig.getConfigScreenWidthAdjustment();
        editorHeightAdjustment = AnkiConfig.getConfigScreenHeightAdjustment();
        recalcBounds();
        KeyBindings.syncConfigFromKeyMappings();
        rebuildButtons();
    }

    private void recalcBounds() {
        int availableWidth = Math.max(1, width - 16);
        int minWidth = Math.min(220, availableWidth);
        int maxWidth = Math.max(minWidth, Math.min(620, availableWidth));
        int preferredWidth = Math.max(minWidth, Math.min(maxWidth, Math.round(width * 0.72f)));
        pw = EditorDock.applyAxisAdjustment(
                EditorDock.interpolateScale(minWidth, preferredWidth, maxWidth, editorScale),
                minWidth, maxWidth, editorWidthAdjustment);

        int availableHeight = Math.max(1, height - 16);
        int minHeight = Math.min(150, availableHeight);
        int maxHeight = Math.max(minHeight, Math.min(420, availableHeight));
        int preferredHeight = Math.max(minHeight, Math.min(maxHeight, Math.round(height * 0.72f)));
        ph = EditorDock.applyAxisAdjustment(
                EditorDock.interpolateScale(minHeight, preferredHeight, maxHeight, editorScale),
                minHeight, maxHeight, editorHeightAdjustment);
        if (!draggingPanel) {
            int configuredX = AnkiConfig.getConfigScreenCustomX();
            int configuredY = AnkiConfig.getConfigScreenCustomY();
            px = configuredX >= 0 ? configuredX : width - pw - 12;
            py = configuredY >= 0 ? configuredY : Math.min(46, Math.max(10, height - ph - 10));
        }
        px = Math.max(8, Math.min(px, Math.max(8, width - pw - 8)));
        py = Math.max(8, Math.min(py, Math.max(8, height - ph - 8)));
        contentTop = py + 74;
        contentBottom = py + ph - 44;
    }

    private void rebuildButtons() {
        buttons.clear();

        int tabY = py + 38;
        int tabGap = 3;
        int tabW = Math.max(28, (pw - 36 - tabGap * (Tab.values().length - 1)) / Tab.values().length);
        int tabH = 20;
        int tx = px + 18;
        for (Tab t : Tab.values()) {
            Tab target = t;
            buttons.add(new UiBtn(tx, tabY, tabW, tabH,
                    () -> Component.translatable(target.key).getString(),
                    () -> {
                        tab = target;
                        tabAnim = 0f;
                        targetScroll = 0f;
                        scroll = 0f;
                        rebuildButtons();
                    },
                    true,
                    () -> tab == target,
                    false));
            tx += tabW + tabGap;
        }

        int left = px + 18;
        int right = px + pw - 18;
        int rowW = right - left;
        int y = contentTop;
        String optionStyle = AnkiConfig.getUiOptionStyle();
        int rowH = AnkiConfig.isUiCompactLayout() || "compact".equals(optionStyle) ? 20
                : "cards".equals(optionStyle) ? 26 : 22;
        int gap = "rows".equals(optionStyle) ? 1 : AnkiConfig.isUiCompactLayout() ? 4 : 6;

        if (tab == Tab.GENERAL) {
            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.preferred_editor", this::modeName,
                    () -> {
                        String current = AnkiConfig.getPreferredItemEditor();
                        AnkiConfig.setPreferredItemEditor("advanced".equalsIgnoreCase(current) ? "simple" : "advanced");
                    }, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.editor_resolution", this::editorResolutionText,
                    AnkiConfig::cycleEditorResolutionPreset, true));
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
                    () -> Minecraft.getInstance().setScreen(new CustomItemGroupsScreen(this)), true, null, true));
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
                    "ankinbt.config.ui_background", this::backgroundText, this::cycleBackgroundPreset, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_style", this::visualStyleText, this::cycleVisualStyle, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_navigation_style", this::navigationStyleText,
                    this::cycleNavigationStyle, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_option_style", this::optionStyleText,
                    () -> {
                        cycleOptionStyle();
                        rebuildButtons();
                    }, true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.ui_custom_colors").getString(),
                    () -> Minecraft.getInstance().setScreen(new ThemeCustomizeScreen(this)), true, null, true));
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
                    "ankinbt.config.ui_editor_position", this::editorPositionText,
                    this::cycleEditorPosition, true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.ui_reset_editor_position").getString(),
                    () -> {
                        AnkiConfig.clearItemEditorCustomPosition();
                        setStatus(Component.translatable("ankinbt.config.ui_position_reset"), UiTheme.TXT_OK);
                    }, true, null, true));
            y += rowH + gap;

            buttons.add(toggleBtn(left, y, rowW, rowH,
                    "ankinbt.config.ui_sound_volume", this::uiSoundVolumeText,
                    this::cycleUiSoundVolume, true));
            y += rowH + gap;

            buttons.add(new UiBtn(left, y, rowW, rowH,
                    () -> Component.translatable("ankinbt.config.open_group_editor").getString(),
                    () -> Minecraft.getInstance().setScreen(new CustomItemGroupsScreen(this)), true, null, true));
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
                    () -> Minecraft.getInstance().setScreen(new CustomItemGroupsScreen(this)), true, null, true));
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

    private String editorResolutionText() {
        String key = switch (AnkiConfig.getEditorResolutionPreset()) {
            case "auto" -> "ankinbt.config.editor_resolution.auto";
            case "960x540" -> "ankinbt.config.editor_resolution.960x540";
            case "1280x720" -> "ankinbt.config.editor_resolution.1280x720";
            case "1600x900" -> "ankinbt.config.editor_resolution.1600x900";
            default -> "ankinbt.config.editor_resolution.adaptive";
        };
        return Component.translatable(key).getString();
    }

    private String onOff(boolean v) {
        return v ? Component.translatable("ankinbt.simple.on").getString()
                : Component.translatable("ankinbt.simple.off").getString();
    }

    private void resetDefaultKeys() {
        KeyBindings.resetToDefaults();
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

        AnkiConfig.setUiOpacity(0.35f);
        AnkiConfig.setUiAccentPreset(0);
        AnkiConfig.setUiAccentHex("#38BDF8");
        AnkiConfig.setUiBackgroundPreset(0);
        AnkiConfig.setUiBackgroundHex("#080B10");
        AnkiConfig.setUiVisualStyle("flat");
        AnkiConfig.setUiNavigationStyle("underline");
        AnkiConfig.setUiOptionStyle("rows");
        AnkiConfig.clearConfigScreenCustomPosition();
        AnkiConfig.setUiShadowEnabled(true);
        AnkiConfig.setUiCompactLayout(false);
        AnkiConfig.setUiAnimationEnabled(true);
        AnkiConfig.setUiAnimationSpeedLevel(3);
        AnkiConfig.setItemEditorPosition("right");
        AnkiConfig.setItemEditorScale(EditorDock.DEFAULT_EDITOR_SCALE);
        AnkiConfig.setConfigScreenScale(EditorDock.DEFAULT_EDITOR_SCALE);
        AnkiConfig.setItemEditorAxisAdjustments(EditorDock.DEFAULT_AXIS_ADJUSTMENT,
                EditorDock.DEFAULT_AXIS_ADJUSTMENT);
        AnkiConfig.resetEntityEditorSizeToItem();
        AnkiConfig.resetVillagerEditorSizeToItem();
        AnkiConfig.setConfigScreenAxisAdjustments(EditorDock.DEFAULT_AXIS_ADJUSTMENT,
                EditorDock.DEFAULT_AXIS_ADJUSTMENT);
        editorScale = EditorDock.DEFAULT_EDITOR_SCALE;
        editorWidthAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
        editorHeightAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
        AnkiConfig.setEditorResolutionPreset("adaptive");
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
            case 4 -> "ankinbt.config.ui_accent.custom";
            default -> "ankinbt.config.ui_accent.blue";
        };
        String value = Component.translatable(key).getString();
        return idx == 4 ? value + " " + AnkiConfig.getUiAccentHex() : value;
    }

    private void cycleUiOpacity() {
        float current = AnkiConfig.getUiOpacity();
        float next = current >= 0.95f ? 0.35f : (current + 0.05f);
        AnkiConfig.setUiOpacity(next);
    }

    private void cycleAccentPreset() {
        AnkiConfig.setUiAccentPreset(AnkiConfig.getUiAccentPreset() + 1);
    }

    private String backgroundText() {
        String key = switch (AnkiConfig.getUiBackgroundPreset()) {
            case 1 -> "ankinbt.config.ui_background.black";
            case 2 -> "ankinbt.config.ui_background.graphite";
            case 3 -> "ankinbt.config.ui_background.light";
            case 4 -> "ankinbt.config.ui_background.custom";
            default -> "ankinbt.config.ui_background.charcoal";
        };
        String value = Component.translatable(key).getString();
        return AnkiConfig.getUiBackgroundPreset() == 4 ? value + " " + AnkiConfig.getUiBackgroundHex() : value;
    }

    private void cycleBackgroundPreset() {
        AnkiConfig.setUiBackgroundPreset(AnkiConfig.getUiBackgroundPreset() + 1);
    }

    private String visualStyleText() {
        String key = switch (AnkiConfig.getUiVisualStyle()) {
            case "outline" -> "ankinbt.config.ui_style.outline";
            case "minimal" -> "ankinbt.config.ui_style.minimal";
            default -> "ankinbt.config.ui_style.flat";
        };
        return Component.translatable(key).getString();
    }

    private void cycleVisualStyle() {
        String next = switch (AnkiConfig.getUiVisualStyle()) {
            case "flat" -> "outline";
            case "outline" -> "minimal";
            default -> "flat";
        };
        AnkiConfig.setUiVisualStyle(next);
    }

    private String navigationStyleText() {
        String key = switch (AnkiConfig.getUiNavigationStyle()) {
            case "segmented" -> "ankinbt.config.ui_navigation_style.segmented";
            case "compact" -> "ankinbt.config.ui_navigation_style.compact";
            default -> "ankinbt.config.ui_navigation_style.underline";
        };
        return Component.translatable(key).getString();
    }

    private void cycleNavigationStyle() {
        String next = switch (AnkiConfig.getUiNavigationStyle()) {
            case "underline" -> "segmented";
            case "segmented" -> "compact";
            default -> "underline";
        };
        AnkiConfig.setUiNavigationStyle(next);
    }

    private String optionStyleText() {
        String key = switch (AnkiConfig.getUiOptionStyle()) {
            case "rows" -> "ankinbt.config.ui_option_style.rows";
            case "compact" -> "ankinbt.config.ui_option_style.compact";
            default -> "ankinbt.config.ui_option_style.cards";
        };
        return Component.translatable(key).getString();
    }

    private void cycleOptionStyle() {
        String next = switch (AnkiConfig.getUiOptionStyle()) {
            case "cards" -> "rows";
            case "rows" -> "compact";
            default -> "cards";
        };
        AnkiConfig.setUiOptionStyle(next);
    }

    private String uiAnimSpeedText() {
        return String.valueOf(AnkiConfig.getUiAnimationSpeedLevel());
    }

    private void cycleUiAnimationSpeed() {
        int next = AnkiConfig.getUiAnimationSpeedLevel() >= 10 ? 1 : (AnkiConfig.getUiAnimationSpeedLevel() + 1);
        AnkiConfig.setUiAnimationSpeedLevel(next);
    }

    private String editorPositionText() {
        String key = switch (AnkiConfig.getItemEditorPosition()) {
            case "left" -> "ankinbt.config.ui_editor_position.left";
            case "top" -> "ankinbt.config.ui_editor_position.top";
            case "bottom" -> "ankinbt.config.ui_editor_position.bottom";
            case "center" -> "ankinbt.config.ui_editor_position.center";
            default -> "ankinbt.config.ui_editor_position.right";
        };
        return Component.translatable(key).getString();
    }

    private void cycleEditorPosition() {
        String next = switch (AnkiConfig.getItemEditorPosition()) {
            case "right" -> "left";
            case "left" -> "top";
            case "top" -> "bottom";
            case "bottom" -> "center";
            default -> "right";
        };
        AnkiConfig.setItemEditorPosition(next);
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
            mc.setScreen(new net.minecraft.client.gui.screens.options.controls.ControlsScreen(this, mc.options));
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
                            mc.setScreen(s);
                            return;
                        }
                    }
                }
            }
        } catch (Throwable reflectErr) {
            DebugLog.warn("Open controls menu via reflection failed: {}", reflectErr.toString());
        }
        try {
            mc.setScreen(new OptionsScreen(this, mc.options, false));
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
        if (button != 0 && button != 1) return false;
        if (button == 0) editorSizeFocused = false;
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
            updateEditorScale(mx);
            return true;
        }
        int closeX = px + pw - 30;
        if (mx >= closeX && mx < closeX + 22 && my >= py + 6 && my < py + 28) {
            UiSound.playClick();
            onClose();
            return true;
        }
        if (mx >= px && mx < px + pw && my >= py && my < py + 34) {
            draggingPanel = true;
            dragOffsetX = (int) Math.round(mx) - px;
            dragOffsetY = (int) Math.round(my) - py;
            return true;
        }
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
        tabAnim = UiTheme.approach(tabAnim, 1.0f, Math.min(1f, speed * 2.1f));
        scroll = UiTheme.approach(scroll, targetScroll, Math.min(1.0f, speed * 2.4f));

        float opacity = AnkiConfig.getUiOpacity();
        int accent = UiTheme.accent(AnkiConfig.getUiAccentPreset());

        int scrim = UiTheme.scrim(opacity, openAnim);
        int panel = UiTheme.panel(opacity, openAnim);
        int card = UiTheme.card(opacity, openAnim);
        int header = UiTheme.header(opacity, openAnim);
        int border = UiTheme.border(opacity, openAnim);
        int shadow = UiTheme.shadow(opacity, openAnim, AnkiConfig.isUiShadowEnabled());

        EditorBrandLayer.renderBackgroundLogo(g, width, height);
        g.fill(0, 0, width, height, scrim);

        if (shadow != 0) g.fill(px + 4, py + 4, px + pw + 4, py + ph + 4, shadow);
        g.fill(px, py, px + pw, py + ph, panel);
        border(g, px, py, pw, ph, border);

        g.fill(px + 1, py + 1, px + pw - 1, py + 34, header);
        g.fill(px + 1, py + 34, px + pw - 1, py + 35, border);
        g.fill(px + 1, py + 60, px + pw - 1, py + ph - 40, card);

        Component settingsIcon = UiIcons.component(UiIcons.SETTINGS);
        Component moveIcon = UiIcons.component(UiIcons.MOVE);
        Component closeIcon = UiIcons.component(UiIcons.CLOSE);
        int closeX = px + pw - 30;
        boolean closeHovered = mx >= closeX && mx < closeX + 22 && my >= py + 6 && my < py + 28;
        closeHoverAnim = UiTheme.approach(closeHoverAnim, closeHovered ? 1.0f : 0.0f,
                AnkiConfig.isUiAnimationEnabled() ? Math.max(0.16f, speed * 2.2f) : 1.0f);
        g.fill(closeX, py + 6, closeX + 22, py + 28,
                UiTheme.mix(0x00000000, UiTheme.withAlpha(accent & 0x00FFFFFF, 72), closeHoverAnim));
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, settingsIcon,
                px + 12, py + 12, accent, false);
        String titleText = title.getString();
        int titleWidth = Math.max(24, closeX - px - 62);
        if (font.width(titleText) > titleWidth) {
            titleText = font.plainSubstrByWidth(titleText, Math.max(4, titleWidth - font.width(".."))) + "..";
        }
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, titleText,
                px + 30, py + 12, UiTheme.TXT_TITLE, false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, moveIcon,
                closeX - font.width(moveIcon) - 9, py + 12, UiTheme.textDim(), false);
        com.ankinbt.compat.VersionCompat.get().drawString(g, font, closeIcon,
                closeX + (22 - font.width(closeIcon)) / 2, py + 12,
                closeHovered ? UiTheme.textMain() : UiTheme.textDim(), false);

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

        int offset = -(int) Math.round(scroll) + Math.round((1f - tabAnim) * 14f);
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
        sizeControlHoverAnim = EditorDock.renderSizeControl(g, font, width, height, mx, my,
                editorScale, sizeControlHoverAnim, resizingEditor || editorSizeFocused, accent);
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
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (resizingEditor && event.button() == 0) {
            updateEditorScale(event.x());
            return true;
        }
        if (draggingPanel && event.button() == 0) {
            px = Math.max(8, Math.min((int) Math.round(event.x()) - dragOffsetX, width - pw - 8));
            py = Math.max(8, Math.min((int) Math.round(event.y()) - dragOffsetY, height - ph - 8));
            contentTop = py + 74;
            contentBottom = py + ph - 44;
            rebuildButtons();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (resizingEditor) {
            resizingEditor = false;
            AnkiConfig.setConfigScreenScale(editorScale);
            return true;
        }
        if (draggingPanel && event.button() == 0) {
            draggingPanel = false;
            AnkiConfig.setConfigScreenCustomPosition(px, py);
            return true;
        }
        return super.mouseReleased(event);
    }

    private void updateEditorScale(double mouseX) {
        setEditorScale(EditorDock.sizeScaleFromMouse(width, mouseX), false);
    }

    private void setEditorScale(float scale, boolean save) {
        editorScale = Math.max(0.0f, Math.min(1.0f, scale));
        recalcBounds();
        rebuildButtons();
        if (save) AnkiConfig.setConfigScreenScale(editorScale);
    }

    private void adjustEditorAxes(float widthDelta, float heightDelta) {
        editorWidthAdjustment = EditorDock.adjustAxis(editorWidthAdjustment, widthDelta);
        editorHeightAdjustment = EditorDock.adjustAxis(editorHeightAdjustment, heightDelta);
        recalcBounds();
        rebuildButtons();
        AnkiConfig.setConfigScreenAxisAdjustments(editorWidthAdjustment, editorHeightAdjustment);
    }

    private void resetEditorSize() {
        editorScale = EditorDock.DEFAULT_EDITOR_SCALE;
        editorWidthAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
        editorHeightAdjustment = EditorDock.DEFAULT_AXIS_ADJUSTMENT;
        recalcBounds();
        rebuildButtons();
        AnkiConfig.setConfigScreenScale(editorScale);
        AnkiConfig.setConfigScreenAxisAdjustments(editorWidthAdjustment, editorHeightAdjustment);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
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
        float hoverAnim;

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

            float speed = AnkiConfig.isUiAnimationEnabled() ? Math.max(0.16f, AnkiConfig.getUiAnimationSpeed() * 2.2f) : 1.0f;
            hoverAnim = UiTheme.approach(hoverAnim, hover ? 1.0f : 0.0f, speed);
            int rest = UiTheme.withAlpha(0xFFFFFF, 34);
            int hoverColor = UiTheme.withAlpha(0xFFFFFF, 74);
            int color = enabled ? UiTheme.textMain() : UiTheme.textDim();
            if (selected != null) {
                String navStyle = AnkiConfig.getUiNavigationStyle();
                if ("segmented".equals(navStyle)) {
                    int bg = chosen ? UiTheme.withAlpha(accent & 0x00FFFFFF, 178)
                            : UiTheme.mix(rest, hoverColor, hoverAnim);
                    g.fill(x, yy, x + w, yy + h, bg);
                    drawEdge(g, x, yy, w, h, chosen ? accent : UiTheme.themedBorder(1f, 1f));
                } else if ("compact".equals(navStyle)) {
                    g.fill(x + 5, yy + 3, x + w - 5, yy + h - 3,
                            chosen ? UiTheme.withAlpha(accent & 0x00FFFFFF, 84)
                                    : UiTheme.mix(0x00000000, hoverColor, hoverAnim));
                    if (chosen) g.fill(x + 5, yy + 4, x + 7, yy + h - 4, accent);
                } else {
                    g.fill(x + 2, yy + 2, x + w - 2, yy + h - 2,
                            UiTheme.mix(0x00000000, hoverColor, hoverAnim));
                    if (chosen) g.fill(x + 6, yy + h - 2, x + w - 6, yy + h, accent);
                }
            } else {
                String optionStyle = AnkiConfig.getUiOptionStyle();
                if ("rows".equals(optionStyle)) {
                    g.fill(x, yy, x + w, yy + h, UiTheme.mix(0x00000000, hoverColor, hoverAnim));
                    g.fill(x, yy + h - 1, x + w, yy + h, UiTheme.themedBorder(0.7f, 1f));
                } else if ("compact".equals(optionStyle)) {
                    g.fill(x, yy + 1, x + w, yy + h - 1, UiTheme.mix(0x18FFFFFF, 0x48FFFFFF, hoverAnim));
                    if (hover) g.fill(x, yy + 3, x + 2, yy + h - 3, accent);
                } else {
                    g.fill(x, yy, x + w, yy + h, enabled
                            ? UiTheme.mix(rest, hoverColor, hoverAnim) : UiTheme.withAlpha(0x101827, 80));
                    drawEdge(g, x, yy, w, h, hover ? UiTheme.withAlpha(accent & 0x00FFFFFF, 190)
                            : UiTheme.themedBorder(0.8f, 1f));
                }
            }

            String text = label.get();
            if (font.width(text) > w - 10) text = font.plainSubstrByWidth(text, w - 14) + "..";
            com.ankinbt.compat.VersionCompat.get().drawString(g, font, text, x + 7, yy + (h - 8) / 2, color, false);
        }

        private static void drawEdge(GuiGraphics g, int x, int y, int w, int h, int color) {
            g.fill(x, y, x + w, y + 1, color);
            g.fill(x, y + h - 1, x + w, y + h, color);
            g.fill(x, y, x + 1, y + h, color);
            g.fill(x + w - 1, y, x + w, y + h, color);
        }
    }
}


