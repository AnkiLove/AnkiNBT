package com.ankinbt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import com.ankinbt.compat.VersionCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnkiConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("AnkiNBT");
    private static Path configPath;
    private static boolean initialized = false;

    private static int openItemEditorKeyCode = InputConstants.KEY_N;
    private static int openEntityEditorKeyCode = InputConstants.KEY_COMMA;
    private static int openVillagerEditorKeyCode = InputConstants.KEY_COMMA;
    private static int openConfigMenuKeyCode = InputConstants.KEY_O;

    private static String preferredItemEditor = "simple";
    private static boolean showAdvancedTags = false;
    private static float uiOpacity = 0.35f;
    private static boolean treeExpandedByDefault = false;
    private static String nbtExportDir = "ankinbt-config/save-nbt";
    private static boolean autoLoadLastNbt = true;
    private static String lastNbtFile = "";
    private static boolean confirmOnClose = true;
    private static String lastExportCategory = "";
    private static boolean nativeFileDialogEnabled;
    private static boolean attributeNotesEnabled;

    private static boolean smartEntityEditorKey = true;
    private static boolean villagerRequireProfession = true;
    private static boolean entityLivePreview = true;
    private static boolean configShowAdvanced = false;

    private static int uiAccentPreset = 0;
    private static String uiAccentHex = "#38BDF8";
    private static int uiBackgroundPreset = 0;
    private static String uiBackgroundHex = "#080B10";
    private static String uiVisualStyle = "flat";
    private static String uiNavigationStyle = "underline";
    // Keep the compact, scan-friendly row layout as the default. Cards remain an
    // opt-in appearance choice in the theme settings.
    private static String uiOptionStyle = "rows";
    private static boolean uiShadowEnabled = true;
    private static boolean uiCompactLayout = false;
    private static boolean uiAnimationEnabled = true;
    private static float uiAnimationSpeed = 0.09f;
    private static String itemEditorPosition = "center";
    private static int itemEditorCustomX = -1;
    private static int itemEditorCustomY = -1;
    // A compact starting point leaves the navigation labels and the world visible.
    // Users can still drag the shared scale control up to the full adaptive size.
    private static float itemEditorScale = 0.40f;
    private static float entityEditorScale = 0.40f;
    private static float villagerEditorScale = 0.40f;
    private static float configScreenScale = 0.40f;
    // Independent offsets are applied after the shared continuous scale.  They keep
    // horizontal and vertical resizing stable when the bottom slider is moved.
    private static float itemEditorWidthAdjustment;
    private static float itemEditorHeightAdjustment;
    private static float entityEditorWidthAdjustment;
    private static float entityEditorHeightAdjustment;
    private static float villagerEditorWidthAdjustment;
    private static float villagerEditorHeightAdjustment;
    // Entity and villager editors start from the item editor's size. A direct
    // resize turns the corresponding flag off and preserves an independent size.
    private static boolean entityEditorSizeFollowsItem = true;
    private static boolean villagerEditorSizeFollowsItem = true;
    private static float configScreenWidthAdjustment;
    private static float configScreenHeightAdjustment;
    private static final int EDITOR_SCALE_MIGRATION_VERSION = 4;
    private static int editorScaleMigrationVersion = EDITOR_SCALE_MIGRATION_VERSION;
    private static final String DEFAULT_EDITOR_RESOLUTION_PRESET = "adaptive";
    private static final String[] EDITOR_RESOLUTION_PRESETS = {
            "auto", "adaptive", "960x540", "1280x720", "1600x900"
    };
    private static String editorResolutionPreset = DEFAULT_EDITOR_RESOLUTION_PRESET;
    private static int configScreenCustomX = -1;
    private static int configScreenCustomY = -1;
    private static float uiSoundVolume = 0.7f;
    private static boolean debugPanelEnabled = true;
    private static boolean debugLogEnabled = false;
    private static boolean debugFileSaveEnabled = false;
    private static final int UI_ANIMATION_LEVEL_MIN = 1;
    private static final int UI_ANIMATION_LEVEL_MAX = 10;
    private static final float UI_ANIMATION_LEVEL_STEP = 0.03f;

    private static final int MAX_RECENT_ITEMS = 30;
    private static List<String> recentItemIds = new ArrayList<>();
    private static Map<String, List<String>> customItemGroups = defaultItemGroups();

    public static void init() {
        initialized = true;
    }

    private static void ensureLoaded() {
        if (configPath != null) return;
        if (!initialized) return;
        try {
            Path configDir = VersionCompat.get().getConfigDir();
            Files.createDirectories(configDir);
            configPath = configDir.resolve("ankinbt.json");
            load();
        } catch (Throwable e) {
            LOGGER.warn("Config init deferred: {}", e.getMessage());
        }
    }

    public static void load() {
        if (configPath == null || !Files.exists(configPath)) return;
        try {
            String json = Files.readString(configPath);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            int loadedScaleMigrationVersion = obj.has("editorScaleMigrationVersion")
                    ? Math.max(0, obj.get("editorScaleMigrationVersion").getAsInt()) : 0;
            boolean hasEntityKey = obj.has("openEntityEditorKeyCode");
            boolean hasVillagerKey = obj.has("openVillagerEditorKeyCode");
            boolean hasEntityEditorSizeFollowFlag = obj.has("entityEditorSizeFollowsItem");
            boolean hasVillagerEditorSizeFollowFlag = obj.has("villagerEditorSizeFollowsItem");
            if (obj.has("openKeyCode")) openItemEditorKeyCode = obj.get("openKeyCode").getAsInt();
            if (obj.has("openItemEditorKeyCode")) openItemEditorKeyCode = obj.get("openItemEditorKeyCode").getAsInt();
            if (hasEntityKey) openEntityEditorKeyCode = obj.get("openEntityEditorKeyCode").getAsInt();
            if (hasVillagerKey) openVillagerEditorKeyCode = obj.get("openVillagerEditorKeyCode").getAsInt();
            if (obj.has("openConfigMenuKeyCode")) openConfigMenuKeyCode = obj.get("openConfigMenuKeyCode").getAsInt();
            if (obj.has("preferredItemEditor")) preferredItemEditor = obj.get("preferredItemEditor").getAsString();
            if (obj.has("showAdvancedTags")) showAdvancedTags = obj.get("showAdvancedTags").getAsBoolean();
            if (obj.has("uiOpacity")) uiOpacity = Math.max(0.3f, Math.min(1.0f, obj.get("uiOpacity").getAsFloat()));
            if (obj.has("treeExpandedByDefault")) treeExpandedByDefault = obj.get("treeExpandedByDefault").getAsBoolean();
            if (obj.has("nbtExportDir")) nbtExportDir = obj.get("nbtExportDir").getAsString();
            if (obj.has("autoLoadLastNbt")) autoLoadLastNbt = obj.get("autoLoadLastNbt").getAsBoolean();
            if (obj.has("lastNbtFile")) lastNbtFile = obj.get("lastNbtFile").getAsString();
            if (obj.has("confirmOnClose")) confirmOnClose = obj.get("confirmOnClose").getAsBoolean();
            if (obj.has("lastExportCategory")) lastExportCategory = obj.get("lastExportCategory").getAsString();
            if (obj.has("nativeFileDialogEnabled")) nativeFileDialogEnabled = obj.get("nativeFileDialogEnabled").getAsBoolean();
            if (obj.has("attributeNotesEnabled")) attributeNotesEnabled = obj.get("attributeNotesEnabled").getAsBoolean();

            if (obj.has("smartEntityEditorKey")) smartEntityEditorKey = obj.get("smartEntityEditorKey").getAsBoolean();
            if (obj.has("villagerRequireProfession")) villagerRequireProfession = obj.get("villagerRequireProfession").getAsBoolean();
            if (obj.has("entityLivePreview")) entityLivePreview = obj.get("entityLivePreview").getAsBoolean();
            if (obj.has("configShowAdvanced")) configShowAdvanced = obj.get("configShowAdvanced").getAsBoolean();
            if (obj.has("uiAccentPreset")) uiAccentPreset = obj.get("uiAccentPreset").getAsInt();
            if (obj.has("uiAccentHex")) uiAccentHex = normalizeHexColor(obj.get("uiAccentHex").getAsString(), "#38BDF8");
            if (obj.has("uiBackgroundPreset")) uiBackgroundPreset = Math.floorMod(obj.get("uiBackgroundPreset").getAsInt(), 5);
            if (obj.has("uiBackgroundHex")) uiBackgroundHex = normalizeHexColor(obj.get("uiBackgroundHex").getAsString(), "#080B10");
            if (obj.has("uiVisualStyle")) uiVisualStyle = normalizeVisualStyle(obj.get("uiVisualStyle").getAsString());
            if (obj.has("uiNavigationStyle")) uiNavigationStyle = normalizeNavigationStyle(obj.get("uiNavigationStyle").getAsString());
            if (obj.has("uiOptionStyle")) uiOptionStyle = normalizeOptionStyle(obj.get("uiOptionStyle").getAsString());
            if (obj.has("uiShadowEnabled")) uiShadowEnabled = obj.get("uiShadowEnabled").getAsBoolean();
            if (obj.has("uiCompactLayout")) uiCompactLayout = obj.get("uiCompactLayout").getAsBoolean();
            if (obj.has("uiAnimationEnabled")) uiAnimationEnabled = obj.get("uiAnimationEnabled").getAsBoolean();
            if (obj.has("uiAnimationSpeed")) uiAnimationSpeed = obj.get("uiAnimationSpeed").getAsFloat();
            if (obj.has("itemEditorPosition")) itemEditorPosition = normalizeEditorPosition(obj.get("itemEditorPosition").getAsString());
            if (obj.has("itemEditorCustomX")) itemEditorCustomX = Math.max(-1, obj.get("itemEditorCustomX").getAsInt());
            if (obj.has("itemEditorCustomY")) itemEditorCustomY = Math.max(-1, obj.get("itemEditorCustomY").getAsInt());
            if (obj.has("itemEditorScale")) itemEditorScale = normalizeEditorScale(obj.get("itemEditorScale").getAsFloat());
            if (obj.has("entityEditorScale")) entityEditorScale = normalizeEditorScale(obj.get("entityEditorScale").getAsFloat());
            if (obj.has("villagerEditorScale")) villagerEditorScale = normalizeEditorScale(obj.get("villagerEditorScale").getAsFloat());
            if (obj.has("configScreenScale")) configScreenScale = normalizeEditorScale(obj.get("configScreenScale").getAsFloat());
            if (obj.has("itemEditorWidthAdjustment")) itemEditorWidthAdjustment = normalizeEditorAxisAdjustment(obj.get("itemEditorWidthAdjustment").getAsFloat());
            if (obj.has("itemEditorHeightAdjustment")) itemEditorHeightAdjustment = normalizeEditorAxisAdjustment(obj.get("itemEditorHeightAdjustment").getAsFloat());
            if (obj.has("entityEditorWidthAdjustment")) entityEditorWidthAdjustment = normalizeEditorAxisAdjustment(obj.get("entityEditorWidthAdjustment").getAsFloat());
            if (obj.has("entityEditorHeightAdjustment")) entityEditorHeightAdjustment = normalizeEditorAxisAdjustment(obj.get("entityEditorHeightAdjustment").getAsFloat());
            if (obj.has("villagerEditorWidthAdjustment")) villagerEditorWidthAdjustment = normalizeEditorAxisAdjustment(obj.get("villagerEditorWidthAdjustment").getAsFloat());
            if (obj.has("villagerEditorHeightAdjustment")) villagerEditorHeightAdjustment = normalizeEditorAxisAdjustment(obj.get("villagerEditorHeightAdjustment").getAsFloat());
            if (hasEntityEditorSizeFollowFlag) {
                entityEditorSizeFollowsItem = obj.get("entityEditorSizeFollowsItem").getAsBoolean();
            }
            if (hasVillagerEditorSizeFollowFlag) {
                villagerEditorSizeFollowsItem = obj.get("villagerEditorSizeFollowsItem").getAsBoolean();
            }
            if (obj.has("configScreenWidthAdjustment")) configScreenWidthAdjustment = normalizeEditorAxisAdjustment(obj.get("configScreenWidthAdjustment").getAsFloat());
            if (obj.has("configScreenHeightAdjustment")) configScreenHeightAdjustment = normalizeEditorAxisAdjustment(obj.get("configScreenHeightAdjustment").getAsFloat());
            if (obj.has("editorResolutionPreset")) {
                editorResolutionPreset = normalizeEditorResolutionPreset(obj.get("editorResolutionPreset").getAsString());
            }
            if (obj.has("configScreenCustomX")) configScreenCustomX = Math.max(-1, obj.get("configScreenCustomX").getAsInt());
            if (obj.has("configScreenCustomY")) configScreenCustomY = Math.max(-1, obj.get("configScreenCustomY").getAsInt());
            if (obj.has("uiSoundVolume")) uiSoundVolume = obj.get("uiSoundVolume").getAsFloat();
            if (obj.has("debugPanelEnabled")) debugPanelEnabled = obj.get("debugPanelEnabled").getAsBoolean();
            if (obj.has("debugLogEnabled")) debugLogEnabled = obj.get("debugLogEnabled").getAsBoolean();
            if (obj.has("debugFileSaveEnabled")) debugFileSaveEnabled = obj.get("debugFileSaveEnabled").getAsBoolean();

            // Upgrade only exact legacy defaults. Each migration runs once so a later
            // user-selected preset is never mistaken for an old default.
            if (loadedScaleMigrationVersion < 1) {
                if (Math.abs(itemEditorScale - 0.39655173f) < 0.0005f) itemEditorScale = 0.40f;
                if (Math.abs(entityEditorScale - 0.35f) < 0.0005f) entityEditorScale = 0.40f;
                if (Math.abs(villagerEditorScale - 0.35f) < 0.0005f) villagerEditorScale = 0.40f;
                if ("1280x720".equalsIgnoreCase(editorResolutionPreset)) {
                    editorResolutionPreset = DEFAULT_EDITOR_RESOLUTION_PRESET;
                }
            }
            if (loadedScaleMigrationVersion < 2
                    && Math.abs(itemEditorScale - 0.5948276f) < 0.0005f) {
                itemEditorScale = 0.40f;
            }

            // Configurations written before the linked-size option have separate
            // fields for every editor. Treat their untouched 0.40/0/0 values as
            // defaults, so they begin following the current item editor size.
            // Any legacy scale or axis adjustment outside that exact default keeps
            // its independent behavior and therefore is never overwritten.
            if (!hasEntityEditorSizeFollowFlag) {
                entityEditorSizeFollowsItem = isDefaultEditorSize(entityEditorScale,
                        entityEditorWidthAdjustment, entityEditorHeightAdjustment);
            }
            if (!hasVillagerEditorSizeFollowFlag) {
                villagerEditorSizeFollowsItem = isDefaultEditorSize(villagerEditorScale,
                        villagerEditorWidthAdjustment, villagerEditorHeightAdjustment);
            }
            editorScaleMigrationVersion = Math.max(loadedScaleMigrationVersion,
                    EDITOR_SCALE_MIGRATION_VERSION);

            if (obj.has("recentItemIds") && obj.get("recentItemIds").isJsonArray()) {
                recentItemIds = new ArrayList<>();
                for (var e : obj.getAsJsonArray("recentItemIds")) {
                    if (e.isJsonPrimitive()) {
                        String id = e.getAsString();
                        if (!id.isBlank()) recentItemIds.add(id);
                    }
                }
            }
            if (obj.has("customItemGroups") && obj.get("customItemGroups").isJsonObject()) {
                customItemGroups = parseItemGroups(obj.getAsJsonObject("customItemGroups"));
            }

            if (!hasEntityKey) {
                openEntityEditorKeyCode = InputConstants.KEY_COMMA;
            }
            if (!hasVillagerKey) {
                openVillagerEditorKeyCode = openEntityEditorKeyCode;
            }
            normalizeKeyBindings();

            if (loadedScaleMigrationVersion < EDITOR_SCALE_MIGRATION_VERSION
                    || !hasEntityEditorSizeFollowFlag || !hasVillagerEditorSizeFollowFlag) save();

            if (debugLogEnabled) LOGGER.info("Config loaded from {}", configPath);
        } catch (Exception e) {
            LOGGER.warn("Failed to load config: {}", e.getMessage());
        }
    }

    public static void save() {
        ensureLoaded();
        if (configPath == null) return;
        try {
            normalizeKeyBindings();
            JsonObject obj = new JsonObject();
            obj.addProperty("openItemEditorKeyCode", openItemEditorKeyCode);
            obj.addProperty("openEntityEditorKeyCode", openEntityEditorKeyCode);
            obj.addProperty("openVillagerEditorKeyCode", openVillagerEditorKeyCode);
            obj.addProperty("openConfigMenuKeyCode", openConfigMenuKeyCode);
            obj.addProperty("preferredItemEditor", preferredItemEditor);
            obj.addProperty("showAdvancedTags", showAdvancedTags);
            obj.addProperty("uiOpacity", uiOpacity);
            obj.addProperty("treeExpandedByDefault", treeExpandedByDefault);
            obj.addProperty("nbtExportDir", nbtExportDir);
            obj.addProperty("autoLoadLastNbt", autoLoadLastNbt);
            obj.addProperty("lastNbtFile", lastNbtFile);
            obj.addProperty("confirmOnClose", confirmOnClose);
            obj.addProperty("lastExportCategory", lastExportCategory);
            obj.addProperty("nativeFileDialogEnabled", nativeFileDialogEnabled);
            obj.addProperty("attributeNotesEnabled", attributeNotesEnabled);

            obj.addProperty("smartEntityEditorKey", smartEntityEditorKey);
            obj.addProperty("villagerRequireProfession", villagerRequireProfession);
            obj.addProperty("entityLivePreview", entityLivePreview);
            obj.addProperty("configShowAdvanced", configShowAdvanced);
            obj.addProperty("uiAccentPreset", uiAccentPreset);
            obj.addProperty("uiAccentHex", uiAccentHex);
            obj.addProperty("uiBackgroundPreset", uiBackgroundPreset);
            obj.addProperty("uiBackgroundHex", uiBackgroundHex);
            obj.addProperty("uiVisualStyle", uiVisualStyle);
            obj.addProperty("uiNavigationStyle", uiNavigationStyle);
            obj.addProperty("uiOptionStyle", uiOptionStyle);
            obj.addProperty("uiShadowEnabled", uiShadowEnabled);
            obj.addProperty("uiCompactLayout", uiCompactLayout);
            obj.addProperty("uiAnimationEnabled", uiAnimationEnabled);
            obj.addProperty("uiAnimationSpeed", uiAnimationSpeed);
            obj.addProperty("itemEditorPosition", itemEditorPosition);
            obj.addProperty("itemEditorCustomX", itemEditorCustomX);
            obj.addProperty("itemEditorCustomY", itemEditorCustomY);
            obj.addProperty("itemEditorScale", itemEditorScale);
            obj.addProperty("entityEditorScale", entityEditorScale);
            obj.addProperty("villagerEditorScale", villagerEditorScale);
            obj.addProperty("configScreenScale", configScreenScale);
            obj.addProperty("itemEditorWidthAdjustment", itemEditorWidthAdjustment);
            obj.addProperty("itemEditorHeightAdjustment", itemEditorHeightAdjustment);
            obj.addProperty("entityEditorWidthAdjustment", entityEditorWidthAdjustment);
            obj.addProperty("entityEditorHeightAdjustment", entityEditorHeightAdjustment);
            obj.addProperty("villagerEditorWidthAdjustment", villagerEditorWidthAdjustment);
            obj.addProperty("villagerEditorHeightAdjustment", villagerEditorHeightAdjustment);
            obj.addProperty("entityEditorSizeFollowsItem", entityEditorSizeFollowsItem);
            obj.addProperty("villagerEditorSizeFollowsItem", villagerEditorSizeFollowsItem);
            obj.addProperty("configScreenWidthAdjustment", configScreenWidthAdjustment);
            obj.addProperty("configScreenHeightAdjustment", configScreenHeightAdjustment);
            obj.addProperty("editorScaleMigrationVersion", editorScaleMigrationVersion);
            obj.addProperty("editorResolutionPreset", editorResolutionPreset);
            obj.addProperty("configScreenCustomX", configScreenCustomX);
            obj.addProperty("configScreenCustomY", configScreenCustomY);
            obj.addProperty("uiSoundVolume", uiSoundVolume);
            obj.addProperty("debugPanelEnabled", debugPanelEnabled);
            obj.addProperty("debugLogEnabled", debugLogEnabled);
            obj.addProperty("debugFileSaveEnabled", debugFileSaveEnabled);

            JsonArray recent = new JsonArray();
            for (String id : recentItemIds) recent.add(id);
            obj.add("recentItemIds", recent);

            obj.add("customItemGroups", writeItemGroups(customItemGroups));

            Files.writeString(configPath, GSON.toJson(obj));
        } catch (Exception e) {
            LOGGER.warn("Failed to save config: {}", e.getMessage());
        }
    }

    public static int getOpenItemEditorKeyCode() { ensureLoaded(); return openItemEditorKeyCode; }
    public static int getOpenEntityEditorKeyCode() { ensureLoaded(); return openEntityEditorKeyCode; }
    public static int getOpenVillagerEditorKeyCode() { ensureLoaded(); return openVillagerEditorKeyCode; }
    public static int getOpenConfigMenuKeyCode() { ensureLoaded(); return openConfigMenuKeyCode; }
    public static String getKeyName() { return VersionCompat.get().getKeyDisplayName(openItemEditorKeyCode); }

    public static String getPreferredItemEditor() { ensureLoaded(); return preferredItemEditor; }
    public static boolean showAdvancedTags() { ensureLoaded(); return showAdvancedTags; }
    public static float getUiOpacity() { ensureLoaded(); return uiOpacity; }
    public static boolean isTreeExpandedByDefault() { ensureLoaded(); return treeExpandedByDefault; }
    public static String getNbtExportDir() { ensureLoaded(); return nbtExportDir; }
    public static boolean isAutoLoadLastNbt() { ensureLoaded(); return autoLoadLastNbt; }
    public static String getLastNbtFile() { ensureLoaded(); return lastNbtFile; }
    public static boolean isConfirmOnClose() { ensureLoaded(); return confirmOnClose; }
    public static String getLastExportCategory() { ensureLoaded(); return lastExportCategory; }
    public static boolean isNativeFileDialogEnabled() { ensureLoaded(); return nativeFileDialogEnabled; }
    public static boolean isAttributeNotesEnabled() { ensureLoaded(); return attributeNotesEnabled; }

    public static boolean isSmartEntityEditorKey() { ensureLoaded(); return smartEntityEditorKey; }
    public static boolean isVillagerRequireProfession() { ensureLoaded(); return villagerRequireProfession; }
    public static boolean isEntityLivePreview() { ensureLoaded(); return entityLivePreview; }
    public static boolean isConfigShowAdvanced() { ensureLoaded(); return configShowAdvanced; }
    public static int getUiAccentPreset() { ensureLoaded(); return uiAccentPreset; }
    public static String getUiAccentHex() { ensureLoaded(); return uiAccentHex; }
    public static int getUiBackgroundPreset() { ensureLoaded(); return uiBackgroundPreset; }
    public static String getUiBackgroundHex() { ensureLoaded(); return uiBackgroundHex; }
    public static String getUiVisualStyle() { ensureLoaded(); return uiVisualStyle; }
    public static String getUiNavigationStyle() { ensureLoaded(); return uiNavigationStyle; }
    public static String getUiOptionStyle() { ensureLoaded(); return uiOptionStyle; }
    public static boolean isUiShadowEnabled() { ensureLoaded(); return uiShadowEnabled; }
    public static boolean isUiCompactLayout() { ensureLoaded(); return uiCompactLayout; }
    public static boolean isUiAnimationEnabled() { ensureLoaded(); return uiAnimationEnabled; }
    public static float getUiAnimationSpeed() { ensureLoaded(); return uiAnimationSpeed; }
    public static int getUiAnimationSpeedLevel() { ensureLoaded(); return uiAnimationLevelFromSpeed(uiAnimationSpeed); }
    public static String getItemEditorPosition() { ensureLoaded(); return itemEditorPosition; }
    public static int getItemEditorCustomX() { ensureLoaded(); return itemEditorCustomX; }
    public static int getItemEditorCustomY() { ensureLoaded(); return itemEditorCustomY; }
    public static float getItemEditorScale() { ensureLoaded(); return itemEditorScale; }
    public static float getEntityEditorScale() {
        ensureLoaded();
        return entityEditorSizeFollowsItem ? itemEditorScale : entityEditorScale;
    }
    public static float getVillagerEditorScale() {
        ensureLoaded();
        return villagerEditorSizeFollowsItem ? itemEditorScale : villagerEditorScale;
    }
    public static float getConfigScreenScale() { ensureLoaded(); return configScreenScale; }
    public static float getItemEditorWidthAdjustment() { ensureLoaded(); return itemEditorWidthAdjustment; }
    public static float getItemEditorHeightAdjustment() { ensureLoaded(); return itemEditorHeightAdjustment; }
    public static float getEntityEditorWidthAdjustment() {
        ensureLoaded();
        return entityEditorSizeFollowsItem ? itemEditorWidthAdjustment : entityEditorWidthAdjustment;
    }
    public static float getEntityEditorHeightAdjustment() {
        ensureLoaded();
        return entityEditorSizeFollowsItem ? itemEditorHeightAdjustment : entityEditorHeightAdjustment;
    }
    public static float getVillagerEditorWidthAdjustment() {
        ensureLoaded();
        return villagerEditorSizeFollowsItem ? itemEditorWidthAdjustment : villagerEditorWidthAdjustment;
    }
    public static float getVillagerEditorHeightAdjustment() {
        ensureLoaded();
        return villagerEditorSizeFollowsItem ? itemEditorHeightAdjustment : villagerEditorHeightAdjustment;
    }
    public static float getConfigScreenWidthAdjustment() { ensureLoaded(); return configScreenWidthAdjustment; }
    public static float getConfigScreenHeightAdjustment() { ensureLoaded(); return configScreenHeightAdjustment; }
    public static String getEditorResolutionPreset() { ensureLoaded(); return editorResolutionPreset; }
    public static int getConfigScreenCustomX() { ensureLoaded(); return configScreenCustomX; }
    public static int getConfigScreenCustomY() { ensureLoaded(); return configScreenCustomY; }
    public static float getUiSoundVolume() { ensureLoaded(); return uiSoundVolume; }
    public static boolean isDebugPanelEnabled() { ensureLoaded(); return debugPanelEnabled; }
    public static boolean isDebugLogEnabled() { ensureLoaded(); return debugLogEnabled; }
    public static boolean isDebugFileSaveEnabled() { ensureLoaded(); return debugFileSaveEnabled; }

    public static List<String> getRecentItemIds() { ensureLoaded(); return new ArrayList<>(recentItemIds); }

    public static Map<String, List<String>> getCustomItemGroups() {
        ensureLoaded();
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (var e : customItemGroups.entrySet()) out.put(e.getKey(), new ArrayList<>(e.getValue()));
        return out;
    }

    public static void setShowAdvancedTags(boolean v) { showAdvancedTags = v; save(); }
    public static void setOpenItemEditorKeyCode(int v) { openItemEditorKeyCode = v; save(); }
    public static void setOpenEntityEditorKeyCode(int v) {
        openEntityEditorKeyCode = normalizeEntityEditorKeyCode(v);
        openVillagerEditorKeyCode = openEntityEditorKeyCode;
        save();
    }
    public static void setOpenVillagerEditorKeyCode(int v) {
        openEntityEditorKeyCode = normalizeEntityEditorKeyCode(v);
        openVillagerEditorKeyCode = openEntityEditorKeyCode;
        save();
    }
    public static void setOpenConfigMenuKeyCode(int v) { openConfigMenuKeyCode = v; save(); }

    public static void setPreferredItemEditor(String v) {
        preferredItemEditor = "advanced".equalsIgnoreCase(v) ? "advanced" : "simple";
        save();
    }

    public static void setUiOpacity(float v) { uiOpacity = Math.max(0.3f, Math.min(1.0f, v)); save(); }
    public static void setTreeExpandedByDefault(boolean v) { treeExpandedByDefault = v; save(); }
    public static void setNbtExportDir(String v) { nbtExportDir = v; save(); }
    public static void setAutoLoadLastNbt(boolean v) { autoLoadLastNbt = v; save(); }
    public static void setLastNbtFile(String v) { lastNbtFile = v; save(); }
    public static void setConfirmOnClose(boolean v) { confirmOnClose = v; save(); }
    public static void setLastExportCategory(String v) { lastExportCategory = v; save(); }
    public static void setNativeFileDialogEnabled(boolean v) { nativeFileDialogEnabled = v; save(); }
    public static void setAttributeNotesEnabled(boolean v) { attributeNotesEnabled = v; save(); }

    public static void setSmartEntityEditorKey(boolean v) { smartEntityEditorKey = v; save(); }
    public static void setVillagerRequireProfession(boolean v) { villagerRequireProfession = v; save(); }
    public static void setEntityLivePreview(boolean v) { entityLivePreview = v; save(); }
    public static void setConfigShowAdvanced(boolean v) { configShowAdvanced = v; save(); }
    public static void setUiAccentPreset(int v) { uiAccentPreset = Math.floorMod(v, 5); save(); }
    public static void setUiAccentHex(String v) { uiAccentHex = normalizeHexColor(v, uiAccentHex); save(); }
    public static void setUiBackgroundPreset(int v) { uiBackgroundPreset = Math.floorMod(v, 5); save(); }
    public static void setUiBackgroundHex(String v) { uiBackgroundHex = normalizeHexColor(v, uiBackgroundHex); save(); }
    public static void setUiVisualStyle(String v) { uiVisualStyle = normalizeVisualStyle(v); save(); }
    public static void setUiNavigationStyle(String v) { uiNavigationStyle = normalizeNavigationStyle(v); save(); }
    public static void setUiOptionStyle(String v) { uiOptionStyle = normalizeOptionStyle(v); save(); }
    public static void setUiShadowEnabled(boolean v) { uiShadowEnabled = v; save(); }
    public static void setUiCompactLayout(boolean v) { uiCompactLayout = v; save(); }
    public static void setUiAnimationEnabled(boolean v) { uiAnimationEnabled = v; save(); }
    public static void setUiAnimationSpeed(float v) { uiAnimationSpeed = Math.max(UI_ANIMATION_LEVEL_STEP, Math.min(0.45f, v)); save(); }
    public static void setUiAnimationSpeedLevel(int level) { uiAnimationSpeed = uiAnimationSpeedForLevel(level); save(); }
    public static void setItemEditorPosition(String v) {
        itemEditorPosition = normalizeEditorPosition(v);
        itemEditorCustomX = -1;
        itemEditorCustomY = -1;
        save();
    }
    public static void setItemEditorCustomPosition(int x, int y) {
        itemEditorCustomX = Math.max(0, x);
        itemEditorCustomY = Math.max(0, y);
        save();
    }
    public static void clearItemEditorCustomPosition() {
        itemEditorCustomX = -1;
        itemEditorCustomY = -1;
        save();
    }
    public static void setItemEditorScale(float scale) {
        itemEditorScale = normalizeEditorScale(scale);
        if (entityEditorSizeFollowsItem) entityEditorScale = itemEditorScale;
        if (villagerEditorSizeFollowsItem) villagerEditorScale = itemEditorScale;
        save();
    }
    public static void setEntityEditorScale(float scale) {
        float normalized = normalizeEditorScale(scale);
        if (entityEditorSizeFollowsItem && approximatelyEqual(normalized, itemEditorScale)) return;
        captureItemSizeForEntityOverride();
        entityEditorScale = normalized;
        entityEditorSizeFollowsItem = false;
        save();
    }
    public static void setVillagerEditorScale(float scale) {
        float normalized = normalizeEditorScale(scale);
        if (villagerEditorSizeFollowsItem && approximatelyEqual(normalized, itemEditorScale)) return;
        captureItemSizeForVillagerOverride();
        villagerEditorScale = normalized;
        villagerEditorSizeFollowsItem = false;
        save();
    }
    public static void setConfigScreenScale(float scale) {
        configScreenScale = normalizeEditorScale(scale);
        save();
    }
    public static void setItemEditorAxisAdjustments(float widthAdjustment, float heightAdjustment) {
        itemEditorWidthAdjustment = normalizeEditorAxisAdjustment(widthAdjustment);
        itemEditorHeightAdjustment = normalizeEditorAxisAdjustment(heightAdjustment);
        if (entityEditorSizeFollowsItem) {
            entityEditorWidthAdjustment = itemEditorWidthAdjustment;
            entityEditorHeightAdjustment = itemEditorHeightAdjustment;
        }
        if (villagerEditorSizeFollowsItem) {
            villagerEditorWidthAdjustment = itemEditorWidthAdjustment;
            villagerEditorHeightAdjustment = itemEditorHeightAdjustment;
        }
        save();
    }
    public static void setEntityEditorAxisAdjustments(float widthAdjustment, float heightAdjustment) {
        float normalizedWidth = normalizeEditorAxisAdjustment(widthAdjustment);
        float normalizedHeight = normalizeEditorAxisAdjustment(heightAdjustment);
        if (entityEditorSizeFollowsItem
                && approximatelyEqual(normalizedWidth, itemEditorWidthAdjustment)
                && approximatelyEqual(normalizedHeight, itemEditorHeightAdjustment)) return;
        captureItemSizeForEntityOverride();
        entityEditorWidthAdjustment = normalizedWidth;
        entityEditorHeightAdjustment = normalizedHeight;
        entityEditorSizeFollowsItem = false;
        save();
    }
    public static void setVillagerEditorAxisAdjustments(float widthAdjustment, float heightAdjustment) {
        float normalizedWidth = normalizeEditorAxisAdjustment(widthAdjustment);
        float normalizedHeight = normalizeEditorAxisAdjustment(heightAdjustment);
        if (villagerEditorSizeFollowsItem
                && approximatelyEqual(normalizedWidth, itemEditorWidthAdjustment)
                && approximatelyEqual(normalizedHeight, itemEditorHeightAdjustment)) return;
        captureItemSizeForVillagerOverride();
        villagerEditorWidthAdjustment = normalizedWidth;
        villagerEditorHeightAdjustment = normalizedHeight;
        villagerEditorSizeFollowsItem = false;
        save();
    }

    /** Restore the entity editor to the live item-editor size profile. */
    public static void resetEntityEditorSizeToItem() {
        captureItemSizeForEntityOverride();
        entityEditorSizeFollowsItem = true;
        save();
    }

    /** Restore the villager editor to the live item-editor size profile. */
    public static void resetVillagerEditorSizeToItem() {
        captureItemSizeForVillagerOverride();
        villagerEditorSizeFollowsItem = true;
        save();
    }
    public static void setConfigScreenAxisAdjustments(float widthAdjustment, float heightAdjustment) {
        configScreenWidthAdjustment = normalizeEditorAxisAdjustment(widthAdjustment);
        configScreenHeightAdjustment = normalizeEditorAxisAdjustment(heightAdjustment);
        save();
    }
    public static void setEditorResolutionPreset(String preset) {
        editorResolutionPreset = normalizeEditorResolutionPreset(preset);
        save();
    }
    public static String cycleEditorResolutionPreset() {
        ensureLoaded();
        int current = 0;
        for (int i = 0; i < EDITOR_RESOLUTION_PRESETS.length; i++) {
            if (EDITOR_RESOLUTION_PRESETS[i].equals(editorResolutionPreset)) {
                current = i;
                break;
            }
        }
        editorResolutionPreset = EDITOR_RESOLUTION_PRESETS[(current + 1) % EDITOR_RESOLUTION_PRESETS.length];
        save();
        return editorResolutionPreset;
    }
    public static void setConfigScreenCustomPosition(int x, int y) {
        configScreenCustomX = Math.max(0, x);
        configScreenCustomY = Math.max(0, y);
        save();
    }
    public static void clearConfigScreenCustomPosition() {
        configScreenCustomX = -1;
        configScreenCustomY = -1;
        save();
    }
    public static void setUiSoundVolume(float v) { uiSoundVolume = Math.max(0.0f, Math.min(1.0f, v)); save(); }
    public static void setDebugPanelEnabled(boolean v) { debugPanelEnabled = v; save(); }
    public static void setDebugLogEnabled(boolean v) { debugLogEnabled = v; save(); }
    public static void setDebugFileSaveEnabled(boolean v) { debugFileSaveEnabled = v; save(); }

    public static void clearRecentItemIds() { recentItemIds = new ArrayList<>(); save(); }
    public static void resetCustomItemGroups() { customItemGroups = defaultItemGroups(); save(); }

    public static void setCustomItemGroups(Map<String, List<String>> groups) {
        ensureLoaded();
        customItemGroups = sanitizeGroups(groups);
        if (customItemGroups.isEmpty()) customItemGroups = defaultItemGroups();
        save();
    }

    public static void putCustomItemGroup(String name, List<String> items) {
        ensureLoaded();
        String n = normalizeGroupName(name);
        if (n.isEmpty()) return;
        customItemGroups.put(n, sanitizeItemIds(items));
        save();
    }

    public static void removeCustomItemGroup(String name) {
        ensureLoaded();
        customItemGroups.remove(normalizeGroupName(name));
        if (customItemGroups.isEmpty()) customItemGroups = defaultItemGroups();
        save();
    }

    public static void renameCustomItemGroup(String oldName, String newName) {
        ensureLoaded();
        String o = normalizeGroupName(oldName);
        String n = normalizeGroupName(newName);
        if (o.isEmpty() || n.isEmpty() || !customItemGroups.containsKey(o)) return;
        List<String> items = customItemGroups.remove(o);
        customItemGroups.put(n, items == null ? new ArrayList<>() : new ArrayList<>(items));
        save();
    }

    public static void addItemToCustomGroup(String group, String itemId) {
        ensureLoaded();
        String g = normalizeGroupName(group);
        String id = itemId == null ? "" : itemId.trim();
        if (g.isEmpty() || id.isEmpty()) return;
        List<String> list = new ArrayList<>(customItemGroups.getOrDefault(g, new ArrayList<>()));
        list.remove(id);
        list.add(id);
        customItemGroups.put(g, list);
        save();
    }

    public static void removeItemFromCustomGroup(String group, int index) {
        ensureLoaded();
        String g = normalizeGroupName(group);
        List<String> list = customItemGroups.get(g);
        if (list == null || index < 0 || index >= list.size()) return;
        list = new ArrayList<>(list);
        list.remove(index);
        customItemGroups.put(g, list);
        save();
    }

    public static void moveItemInCustomGroup(String group, int from, int to) {
        ensureLoaded();
        String g = normalizeGroupName(group);
        List<String> list = customItemGroups.get(g);
        if (list == null || from < 0 || to < 0 || from >= list.size() || to >= list.size() || from == to) return;
        list = new ArrayList<>(list);
        String value = list.remove(from);
        list.add(to, value);
        customItemGroups.put(g, list);
        save();
    }

    public static void addRecentItemId(String id) {
        ensureLoaded();
        if (id == null || id.isBlank()) return;
        recentItemIds.remove(id);
        recentItemIds.add(0, id);
        while (recentItemIds.size() > MAX_RECENT_ITEMS) recentItemIds.remove(recentItemIds.size() - 1);
        save();
    }

    private static int uiAnimationLevelFromSpeed(float speed) {
        return Math.max(UI_ANIMATION_LEVEL_MIN,
                Math.min(UI_ANIMATION_LEVEL_MAX, Math.round(speed / UI_ANIMATION_LEVEL_STEP)));
    }

    private static void normalizeKeyBindings() {
        openVillagerEditorKeyCode = openEntityEditorKeyCode;
    }

    private static int normalizeEntityEditorKeyCode(int keyCode) {
        return keyCode;
    }

    private static String normalizeEditorPosition(String value) {
        if (value == null) return "center";
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "left", "right", "top", "bottom", "center" -> value.toLowerCase(java.util.Locale.ROOT);
            default -> "center";
        };
    }

    private static void captureItemSizeForEntityOverride() {
        entityEditorScale = itemEditorScale;
        entityEditorWidthAdjustment = itemEditorWidthAdjustment;
        entityEditorHeightAdjustment = itemEditorHeightAdjustment;
    }

    private static void captureItemSizeForVillagerOverride() {
        villagerEditorScale = itemEditorScale;
        villagerEditorWidthAdjustment = itemEditorWidthAdjustment;
        villagerEditorHeightAdjustment = itemEditorHeightAdjustment;
    }

    private static boolean isDefaultEditorSize(float scale, float widthAdjustment, float heightAdjustment) {
        return approximatelyEqual(scale, 0.40f)
                && approximatelyEqual(widthAdjustment, 0.0f)
                && approximatelyEqual(heightAdjustment, 0.0f);
    }

    private static boolean approximatelyEqual(float first, float second) {
        return Math.abs(first - second) < 0.0005f;
    }

    private static float normalizeEditorScale(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float normalizeEditorAxisAdjustment(float value) {
        return Math.max(-1.0f, Math.min(1.0f, value));
    }

    private static String normalizeEditorResolutionPreset(String value) {
        if (value != null) {
            for (String preset : EDITOR_RESOLUTION_PRESETS) {
                if (preset.equalsIgnoreCase(value.trim())) return preset;
            }
        }
        return DEFAULT_EDITOR_RESOLUTION_PRESET;
    }

    private static String normalizeVisualStyle(String value) {
        if (value == null) return "flat";
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "outline", "minimal" -> value.toLowerCase(java.util.Locale.ROOT);
            default -> "flat";
        };
    }

    private static String normalizeNavigationStyle(String value) {
        if (value == null) return "underline";
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "segmented", "compact" -> value.toLowerCase(java.util.Locale.ROOT);
            default -> "underline";
        };
    }

    private static String normalizeOptionStyle(String value) {
        if (value == null) return "rows";
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "cards", "rows", "compact" -> value.toLowerCase(java.util.Locale.ROOT);
            default -> "rows";
        };
    }

    private static String normalizeHexColor(String value, String fallback) {
        if (value == null) return fallback;
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        if (!normalized.startsWith("#")) normalized = "#" + normalized;
        return normalized.matches("#[0-9A-F]{6}") ? normalized : fallback;
    }

    private static float uiAnimationSpeedForLevel(int level) {
        int clamped = Math.max(UI_ANIMATION_LEVEL_MIN, Math.min(UI_ANIMATION_LEVEL_MAX, level));
        return Math.max(UI_ANIMATION_LEVEL_STEP, Math.min(0.45f, clamped * UI_ANIMATION_LEVEL_STEP));
    }

    public static Path getExportPath() {
        Path gameDir = VersionCompat.get().getGameDir();
        Path exportDir = gameDir.resolve(nbtExportDir);
        try { Files.createDirectories(exportDir); } catch (IOException ignored) {}
        return exportDir;
    }

    public static Path getExportPath(String category) {
        Path base = getExportPath();
        if (category != null && !category.isBlank()) {
            Path catDir = base.resolve(category.trim());
            try { Files.createDirectories(catDir); } catch (IOException ignored) {}
            return catDir;
        }
        return base;
    }

    public static java.util.List<String> listExportCategories() {
        java.util.List<String> cats = new java.util.ArrayList<>();
        Path base = getExportPath();
        if (!Files.isDirectory(base)) return cats;
        try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(base)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) {
                    cats.add(p.getFileName().toString());
                }
            }
        } catch (IOException ignored) {}
        java.util.Collections.sort(cats);
        return cats;
    }

    private static Map<String, List<String>> defaultItemGroups() {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("常用方块", List.of(
                "minecraft:stone", "minecraft:dirt", "minecraft:glass",
                "minecraft:oak_planks", "minecraft:cobblestone"
        ));
        m.put("常用材料", List.of(
                "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:diamond",
                "minecraft:emerald", "minecraft:redstone"
        ));
        return m;
    }

    private static Map<String, List<String>> parseItemGroups(JsonObject obj) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (var e : obj.entrySet()) {
            if (!e.getValue().isJsonArray()) continue;
            List<String> ids = new ArrayList<>();
            for (var idEl : e.getValue().getAsJsonArray()) {
                if (idEl.isJsonPrimitive()) {
                    String id = idEl.getAsString();
                    if (!id.isBlank()) ids.add(id);
                }
            }
            if (!ids.isEmpty()) out.put(e.getKey(), ids);
        }
        if (out.isEmpty()) return defaultItemGroups();
        return out;
    }

    private static JsonObject writeItemGroups(Map<String, List<String>> groups) {
        JsonObject out = new JsonObject();
        for (var e : groups.entrySet()) {
            JsonArray arr = new JsonArray();
            for (String id : e.getValue()) arr.add(id);
            out.add(e.getKey(), arr);
        }
        return out;
    }

    private static String normalizeGroupName(String in) {
        return in == null ? "" : in.trim();
    }

    private static Map<String, List<String>> sanitizeGroups(Map<String, List<String>> in) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        if (in == null) return out;
        for (var e : in.entrySet()) {
            String name = normalizeGroupName(e.getKey());
            if (name.isEmpty()) continue;
            out.put(name, sanitizeItemIds(e.getValue()));
        }
        return out;
    }

    private static List<String> sanitizeItemIds(List<String> ids) {
        List<String> out = new ArrayList<>();
        if (ids == null) return out;
        for (String id : ids) {
            String v = id == null ? "" : id.trim();
            if (!v.isEmpty()) out.add(v);
        }
        return out;
    }
}


