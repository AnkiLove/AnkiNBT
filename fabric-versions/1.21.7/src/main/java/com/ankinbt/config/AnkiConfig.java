/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.ankinbt.config;

import com.ankinbt.compat.VersionCompat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnkiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"AnkiNBT");
    private static Path configPath;
    private static boolean initialized;
    private static int openItemEditorKeyCode;
    private static int openEntityEditorKeyCode;
    private static int openVillagerEditorKeyCode;
    private static int openConfigMenuKeyCode;
    private static String preferredItemEditor;
    private static boolean showAdvancedTags;
    private static float uiOpacity;
    private static boolean treeExpandedByDefault;
    private static String nbtExportDir;
    private static boolean autoLoadLastNbt;
    private static String lastNbtFile;
    private static boolean confirmOnClose;
    private static String lastExportCategory;
    private static boolean nativeFileDialogEnabled;
    private static boolean attributeNotesEnabled;
    private static boolean smartEntityEditorKey;
    private static boolean villagerRequireProfession;
    private static boolean entityLivePreview;
    private static boolean configShowAdvanced;
    private static int uiAccentPreset;
    private static boolean uiShadowEnabled;
    private static boolean uiCompactLayout;
    private static boolean uiAnimationEnabled;
    private static float uiAnimationSpeed;
    private static float uiSoundVolume;
    private static boolean debugPanelEnabled;
    private static boolean debugLogEnabled;
    private static boolean debugFileSaveEnabled;
    private static final int UI_ANIMATION_LEVEL_MIN = 1;
    private static final int UI_ANIMATION_LEVEL_MAX = 10;
    private static final float UI_ANIMATION_LEVEL_STEP = 0.03f;
    private static final int MAX_RECENT_ITEMS = 30;
    private static List<String> recentItemIds;
    private static Map<String, List<String>> customItemGroups;

    public static void init() {
        initialized = true;
    }

    private static void ensureLoaded() {
        if (configPath != null) {
            return;
        }
        if (!initialized) {
            return;
        }
        try {
            Path configDir = VersionCompat.get().getConfigDir();
            Files.createDirectories(configDir, new FileAttribute[0]);
            configPath = configDir.resolve("ankinbt.json");
            AnkiConfig.load();
        }
        catch (Throwable e) {
            LOGGER.warn("Config init deferred: {}", (Object)e.getMessage());
        }
    }

    public static void load() {
        if (configPath == null || !Files.exists(configPath, new LinkOption[0])) {
            return;
        }
        try {
            String json = Files.readString(configPath);
            JsonObject obj = JsonParser.parseString((String)json).getAsJsonObject();
            boolean hasEntityKey = obj.has("openEntityEditorKeyCode");
            boolean hasVillagerKey = obj.has("openVillagerEditorKeyCode");
            if (obj.has("openKeyCode")) {
                openItemEditorKeyCode = obj.get("openKeyCode").getAsInt();
            }
            if (obj.has("openItemEditorKeyCode")) {
                openItemEditorKeyCode = obj.get("openItemEditorKeyCode").getAsInt();
            }
            if (hasEntityKey) {
                openEntityEditorKeyCode = obj.get("openEntityEditorKeyCode").getAsInt();
            }
            if (hasVillagerKey) {
                openVillagerEditorKeyCode = obj.get("openVillagerEditorKeyCode").getAsInt();
            }
            if (obj.has("openConfigMenuKeyCode")) {
                openConfigMenuKeyCode = obj.get("openConfigMenuKeyCode").getAsInt();
            }
            if (obj.has("preferredItemEditor")) {
                preferredItemEditor = obj.get("preferredItemEditor").getAsString();
            }
            if (obj.has("showAdvancedTags")) {
                showAdvancedTags = obj.get("showAdvancedTags").getAsBoolean();
            }
            if (obj.has("uiOpacity")) {
                uiOpacity = obj.get("uiOpacity").getAsFloat();
            }
            if (obj.has("treeExpandedByDefault")) {
                treeExpandedByDefault = obj.get("treeExpandedByDefault").getAsBoolean();
            }
            if (obj.has("nbtExportDir")) {
                nbtExportDir = obj.get("nbtExportDir").getAsString();
            }
            if (obj.has("autoLoadLastNbt")) {
                autoLoadLastNbt = obj.get("autoLoadLastNbt").getAsBoolean();
            }
            if (obj.has("lastNbtFile")) {
                lastNbtFile = obj.get("lastNbtFile").getAsString();
            }
            if (obj.has("confirmOnClose")) {
                confirmOnClose = obj.get("confirmOnClose").getAsBoolean();
            }
            if (obj.has("lastExportCategory")) {
                lastExportCategory = obj.get("lastExportCategory").getAsString();
            }
            if (obj.has("nativeFileDialogEnabled")) {
                nativeFileDialogEnabled = obj.get("nativeFileDialogEnabled").getAsBoolean();
            }
            if (obj.has("attributeNotesEnabled")) {
                attributeNotesEnabled = obj.get("attributeNotesEnabled").getAsBoolean();
            }
            if (obj.has("smartEntityEditorKey")) {
                smartEntityEditorKey = obj.get("smartEntityEditorKey").getAsBoolean();
            }
            if (obj.has("villagerRequireProfession")) {
                villagerRequireProfession = obj.get("villagerRequireProfession").getAsBoolean();
            }
            if (obj.has("entityLivePreview")) {
                entityLivePreview = obj.get("entityLivePreview").getAsBoolean();
            }
            if (obj.has("configShowAdvanced")) {
                configShowAdvanced = obj.get("configShowAdvanced").getAsBoolean();
            }
            if (obj.has("uiAccentPreset")) {
                uiAccentPreset = obj.get("uiAccentPreset").getAsInt();
            }
            if (obj.has("uiShadowEnabled")) {
                uiShadowEnabled = obj.get("uiShadowEnabled").getAsBoolean();
            }
            if (obj.has("uiCompactLayout")) {
                uiCompactLayout = obj.get("uiCompactLayout").getAsBoolean();
            }
            if (obj.has("uiAnimationEnabled")) {
                uiAnimationEnabled = obj.get("uiAnimationEnabled").getAsBoolean();
            }
            if (obj.has("uiAnimationSpeed")) {
                uiAnimationSpeed = obj.get("uiAnimationSpeed").getAsFloat();
            }
            if (obj.has("uiSoundVolume")) {
                uiSoundVolume = obj.get("uiSoundVolume").getAsFloat();
            }
            if (obj.has("debugPanelEnabled")) {
                debugPanelEnabled = obj.get("debugPanelEnabled").getAsBoolean();
            }
            if (obj.has("debugLogEnabled")) {
                debugLogEnabled = obj.get("debugLogEnabled").getAsBoolean();
            }
            if (obj.has("debugFileSaveEnabled")) {
                debugFileSaveEnabled = obj.get("debugFileSaveEnabled").getAsBoolean();
            }
            if (obj.has("recentItemIds") && obj.get("recentItemIds").isJsonArray()) {
                recentItemIds = new ArrayList<String>();
                for (JsonElement e : obj.getAsJsonArray("recentItemIds")) {
                    String id;
                    if (!e.isJsonPrimitive() || (id = e.getAsString()).isBlank()) continue;
                    recentItemIds.add(id);
                }
            }
            if (obj.has("customItemGroups") && obj.get("customItemGroups").isJsonObject()) {
                customItemGroups = AnkiConfig.parseItemGroups(obj.getAsJsonObject("customItemGroups"));
            }
            if (!hasEntityKey || openEntityEditorKeyCode == 77) {
                openEntityEditorKeyCode = 44;
            }
            if (!hasVillagerKey || openVillagerEditorKeyCode == 86) {
                openVillagerEditorKeyCode = 44;
            }
            AnkiConfig.normalizeKeyBindings();
            if (debugLogEnabled) {
                LOGGER.info("Config loaded from {}", (Object)configPath);
            }
        }
        catch (Exception e) {
            LOGGER.warn("Failed to load config: {}", (Object)e.getMessage());
        }
    }

    public static void save() {
        AnkiConfig.ensureLoaded();
        if (configPath == null) {
            return;
        }
        try {
            AnkiConfig.normalizeKeyBindings();
            JsonObject obj = new JsonObject();
            obj.addProperty("openItemEditorKeyCode", (Number)openItemEditorKeyCode);
            obj.addProperty("openEntityEditorKeyCode", (Number)openEntityEditorKeyCode);
            obj.addProperty("openVillagerEditorKeyCode", (Number)openVillagerEditorKeyCode);
            obj.addProperty("openConfigMenuKeyCode", (Number)openConfigMenuKeyCode);
            obj.addProperty("preferredItemEditor", preferredItemEditor);
            obj.addProperty("showAdvancedTags", Boolean.valueOf(showAdvancedTags));
            obj.addProperty("uiOpacity", (Number)Float.valueOf(uiOpacity));
            obj.addProperty("treeExpandedByDefault", Boolean.valueOf(treeExpandedByDefault));
            obj.addProperty("nbtExportDir", nbtExportDir);
            obj.addProperty("autoLoadLastNbt", Boolean.valueOf(autoLoadLastNbt));
            obj.addProperty("lastNbtFile", lastNbtFile);
            obj.addProperty("confirmOnClose", Boolean.valueOf(confirmOnClose));
            obj.addProperty("lastExportCategory", lastExportCategory);
            obj.addProperty("nativeFileDialogEnabled", Boolean.valueOf(nativeFileDialogEnabled));
            obj.addProperty("attributeNotesEnabled", Boolean.valueOf(attributeNotesEnabled));
            obj.addProperty("smartEntityEditorKey", Boolean.valueOf(smartEntityEditorKey));
            obj.addProperty("villagerRequireProfession", Boolean.valueOf(villagerRequireProfession));
            obj.addProperty("entityLivePreview", Boolean.valueOf(entityLivePreview));
            obj.addProperty("configShowAdvanced", Boolean.valueOf(configShowAdvanced));
            obj.addProperty("uiAccentPreset", (Number)uiAccentPreset);
            obj.addProperty("uiShadowEnabled", Boolean.valueOf(uiShadowEnabled));
            obj.addProperty("uiCompactLayout", Boolean.valueOf(uiCompactLayout));
            obj.addProperty("uiAnimationEnabled", Boolean.valueOf(uiAnimationEnabled));
            obj.addProperty("uiAnimationSpeed", (Number)Float.valueOf(uiAnimationSpeed));
            obj.addProperty("uiSoundVolume", (Number)Float.valueOf(uiSoundVolume));
            obj.addProperty("debugPanelEnabled", Boolean.valueOf(debugPanelEnabled));
            obj.addProperty("debugLogEnabled", Boolean.valueOf(debugLogEnabled));
            obj.addProperty("debugFileSaveEnabled", Boolean.valueOf(debugFileSaveEnabled));
            JsonArray recent = new JsonArray();
            for (String id : recentItemIds) {
                recent.add(id);
            }
            obj.add("recentItemIds", (JsonElement)recent);
            obj.add("customItemGroups", (JsonElement)AnkiConfig.writeItemGroups(customItemGroups));
            Files.writeString(configPath, (CharSequence)GSON.toJson((JsonElement)obj), new OpenOption[0]);
        }
        catch (Exception e) {
            LOGGER.warn("Failed to save config: {}", (Object)e.getMessage());
        }
    }

    public static int getOpenItemEditorKeyCode() {
        AnkiConfig.ensureLoaded();
        return openItemEditorKeyCode;
    }

    public static int getOpenEntityEditorKeyCode() {
        AnkiConfig.ensureLoaded();
        return openEntityEditorKeyCode;
    }

    public static int getOpenVillagerEditorKeyCode() {
        AnkiConfig.ensureLoaded();
        return openVillagerEditorKeyCode;
    }

    public static int getOpenConfigMenuKeyCode() {
        AnkiConfig.ensureLoaded();
        return openConfigMenuKeyCode;
    }

    public static String getKeyName() {
        return VersionCompat.get().getKeyDisplayName(openItemEditorKeyCode);
    }

    public static String getPreferredItemEditor() {
        AnkiConfig.ensureLoaded();
        return preferredItemEditor;
    }

    public static boolean showAdvancedTags() {
        AnkiConfig.ensureLoaded();
        return showAdvancedTags;
    }

    public static float getUiOpacity() {
        AnkiConfig.ensureLoaded();
        return uiOpacity;
    }

    public static boolean isTreeExpandedByDefault() {
        AnkiConfig.ensureLoaded();
        return treeExpandedByDefault;
    }

    public static String getNbtExportDir() {
        AnkiConfig.ensureLoaded();
        return nbtExportDir;
    }

    public static boolean isAutoLoadLastNbt() {
        AnkiConfig.ensureLoaded();
        return autoLoadLastNbt;
    }

    public static String getLastNbtFile() {
        AnkiConfig.ensureLoaded();
        return lastNbtFile;
    }

    public static boolean isConfirmOnClose() {
        AnkiConfig.ensureLoaded();
        return confirmOnClose;
    }

    public static String getLastExportCategory() {
        AnkiConfig.ensureLoaded();
        return lastExportCategory;
    }

    public static boolean isNativeFileDialogEnabled() {
        AnkiConfig.ensureLoaded();
        return nativeFileDialogEnabled;
    }

    public static boolean isAttributeNotesEnabled() {
        AnkiConfig.ensureLoaded();
        return attributeNotesEnabled;
    }

    public static boolean isSmartEntityEditorKey() {
        AnkiConfig.ensureLoaded();
        return smartEntityEditorKey;
    }

    public static boolean isVillagerRequireProfession() {
        AnkiConfig.ensureLoaded();
        return villagerRequireProfession;
    }

    public static boolean isEntityLivePreview() {
        AnkiConfig.ensureLoaded();
        return entityLivePreview;
    }

    public static boolean isConfigShowAdvanced() {
        AnkiConfig.ensureLoaded();
        return configShowAdvanced;
    }

    public static int getUiAccentPreset() {
        AnkiConfig.ensureLoaded();
        return uiAccentPreset;
    }

    public static boolean isUiShadowEnabled() {
        AnkiConfig.ensureLoaded();
        return uiShadowEnabled;
    }

    public static boolean isUiCompactLayout() {
        AnkiConfig.ensureLoaded();
        return uiCompactLayout;
    }

    public static boolean isUiAnimationEnabled() {
        AnkiConfig.ensureLoaded();
        return uiAnimationEnabled;
    }

    public static float getUiAnimationSpeed() {
        AnkiConfig.ensureLoaded();
        return uiAnimationSpeed;
    }

    public static int getUiAnimationSpeedLevel() {
        AnkiConfig.ensureLoaded();
        return AnkiConfig.uiAnimationLevelFromSpeed(uiAnimationSpeed);
    }

    public static float getUiSoundVolume() {
        AnkiConfig.ensureLoaded();
        return uiSoundVolume;
    }

    public static boolean isDebugPanelEnabled() {
        AnkiConfig.ensureLoaded();
        return debugPanelEnabled;
    }

    public static boolean isDebugLogEnabled() {
        AnkiConfig.ensureLoaded();
        return debugLogEnabled;
    }

    public static boolean isDebugFileSaveEnabled() {
        AnkiConfig.ensureLoaded();
        return debugFileSaveEnabled;
    }

    public static List<String> getRecentItemIds() {
        AnkiConfig.ensureLoaded();
        return new ArrayList<String>(recentItemIds);
    }

    public static Map<String, List<String>> getCustomItemGroups() {
        AnkiConfig.ensureLoaded();
        LinkedHashMap<String, List<String>> out = new LinkedHashMap<String, List<String>>();
        for (Map.Entry<String, List<String>> e : customItemGroups.entrySet()) {
            out.put(e.getKey(), new ArrayList(e.getValue()));
        }
        return out;
    }

    public static void setShowAdvancedTags(boolean v) {
        showAdvancedTags = v;
        AnkiConfig.save();
    }

    public static void setOpenItemEditorKeyCode(int v) {
        openItemEditorKeyCode = v;
        AnkiConfig.save();
    }

    public static void setOpenEntityEditorKeyCode(int v) {
        openVillagerEditorKeyCode = openEntityEditorKeyCode = AnkiConfig.normalizeEntityEditorKeyCode(v);
        AnkiConfig.save();
    }

    public static void setOpenVillagerEditorKeyCode(int v) {
        openVillagerEditorKeyCode = openEntityEditorKeyCode = AnkiConfig.normalizeEntityEditorKeyCode(v);
        AnkiConfig.save();
    }

    public static void setOpenConfigMenuKeyCode(int v) {
        openConfigMenuKeyCode = v;
        AnkiConfig.save();
    }

    public static void setPreferredItemEditor(String v) {
        preferredItemEditor = "advanced".equalsIgnoreCase(v) ? "advanced" : "simple";
        AnkiConfig.save();
    }

    public static void setUiOpacity(float v) {
        uiOpacity = Math.max(0.3f, Math.min(1.0f, v));
        AnkiConfig.save();
    }

    public static void setTreeExpandedByDefault(boolean v) {
        treeExpandedByDefault = v;
        AnkiConfig.save();
    }

    public static void setNbtExportDir(String v) {
        nbtExportDir = v;
        AnkiConfig.save();
    }

    public static void setAutoLoadLastNbt(boolean v) {
        autoLoadLastNbt = v;
        AnkiConfig.save();
    }

    public static void setLastNbtFile(String v) {
        lastNbtFile = v;
        AnkiConfig.save();
    }

    public static void setConfirmOnClose(boolean v) {
        confirmOnClose = v;
        AnkiConfig.save();
    }

    public static void setLastExportCategory(String v) {
        lastExportCategory = v;
        AnkiConfig.save();
    }

    public static void setNativeFileDialogEnabled(boolean v) {
        nativeFileDialogEnabled = v;
        AnkiConfig.save();
    }

    public static void setAttributeNotesEnabled(boolean v) {
        attributeNotesEnabled = v;
        AnkiConfig.save();
    }

    public static void setSmartEntityEditorKey(boolean v) {
        smartEntityEditorKey = v;
        AnkiConfig.save();
    }

    public static void setVillagerRequireProfession(boolean v) {
        villagerRequireProfession = v;
        AnkiConfig.save();
    }

    public static void setEntityLivePreview(boolean v) {
        entityLivePreview = v;
        AnkiConfig.save();
    }

    public static void setConfigShowAdvanced(boolean v) {
        configShowAdvanced = v;
        AnkiConfig.save();
    }

    public static void setUiAccentPreset(int v) {
        uiAccentPreset = Math.floorMod(v, 4);
        AnkiConfig.save();
    }

    public static void setUiShadowEnabled(boolean v) {
        uiShadowEnabled = v;
        AnkiConfig.save();
    }

    public static void setUiCompactLayout(boolean v) {
        uiCompactLayout = v;
        AnkiConfig.save();
    }

    public static void setUiAnimationEnabled(boolean v) {
        uiAnimationEnabled = v;
        AnkiConfig.save();
    }

    public static void setUiAnimationSpeed(float v) {
        uiAnimationSpeed = Math.max(0.03f, Math.min(0.45f, v));
        AnkiConfig.save();
    }

    public static void setUiAnimationSpeedLevel(int level) {
        uiAnimationSpeed = AnkiConfig.uiAnimationSpeedForLevel(level);
        AnkiConfig.save();
    }

    public static void setUiSoundVolume(float v) {
        uiSoundVolume = Math.max(0.0f, Math.min(1.0f, v));
        AnkiConfig.save();
    }

    public static void setDebugPanelEnabled(boolean v) {
        debugPanelEnabled = v;
        AnkiConfig.save();
    }

    public static void setDebugLogEnabled(boolean v) {
        debugLogEnabled = v;
        AnkiConfig.save();
    }

    public static void setDebugFileSaveEnabled(boolean v) {
        debugFileSaveEnabled = v;
        AnkiConfig.save();
    }

    public static void clearRecentItemIds() {
        recentItemIds = new ArrayList<String>();
        AnkiConfig.save();
    }

    public static void resetCustomItemGroups() {
        customItemGroups = AnkiConfig.defaultItemGroups();
        AnkiConfig.save();
    }

    public static void setCustomItemGroups(Map<String, List<String>> groups) {
        AnkiConfig.ensureLoaded();
        customItemGroups = AnkiConfig.sanitizeGroups(groups);
        if (customItemGroups.isEmpty()) {
            customItemGroups = AnkiConfig.defaultItemGroups();
        }
        AnkiConfig.save();
    }

    public static void putCustomItemGroup(String name, List<String> items) {
        AnkiConfig.ensureLoaded();
        String n = AnkiConfig.normalizeGroupName(name);
        if (n.isEmpty()) {
            return;
        }
        customItemGroups.put(n, AnkiConfig.sanitizeItemIds(items));
        AnkiConfig.save();
    }

    public static void removeCustomItemGroup(String name) {
        AnkiConfig.ensureLoaded();
        customItemGroups.remove(AnkiConfig.normalizeGroupName(name));
        if (customItemGroups.isEmpty()) {
            customItemGroups = AnkiConfig.defaultItemGroups();
        }
        AnkiConfig.save();
    }

    public static void renameCustomItemGroup(String oldName, String newName) {
        AnkiConfig.ensureLoaded();
        String o = AnkiConfig.normalizeGroupName(oldName);
        String n = AnkiConfig.normalizeGroupName(newName);
        if (o.isEmpty() || n.isEmpty() || !customItemGroups.containsKey(o)) {
            return;
        }
        List<String> items = customItemGroups.remove(o);
        customItemGroups.put(n, items == null ? new ArrayList() : new ArrayList<String>(items));
        AnkiConfig.save();
    }

    public static void addItemToCustomGroup(String group, String itemId) {
        String id;
        AnkiConfig.ensureLoaded();
        String g = AnkiConfig.normalizeGroupName(group);
        String string = id = itemId == null ? "" : itemId.trim();
        if (g.isEmpty() || id.isEmpty()) {
            return;
        }
        ArrayList<String> list = new ArrayList<String>(customItemGroups.getOrDefault(g, new ArrayList()));
        list.remove(id);
        list.add(id);
        customItemGroups.put(g, list);
        AnkiConfig.save();
    }

    public static void removeItemFromCustomGroup(String group, int index) {
        AnkiConfig.ensureLoaded();
        String g = AnkiConfig.normalizeGroupName(group);
        List<String> list = customItemGroups.get(g);
        if (list == null || index < 0 || index >= list.size()) {
            return;
        }
        list = new ArrayList<String>(list);
        list.remove(index);
        customItemGroups.put(g, list);
        AnkiConfig.save();
    }

    public static void moveItemInCustomGroup(String group, int from, int to) {
        AnkiConfig.ensureLoaded();
        String g = AnkiConfig.normalizeGroupName(group);
        List<String> list = customItemGroups.get(g);
        if (list == null || from < 0 || to < 0 || from >= list.size() || to >= list.size() || from == to) {
            return;
        }
        list = new ArrayList<String>(list);
        String value = list.remove(from);
        list.add(to, value);
        customItemGroups.put(g, list);
        AnkiConfig.save();
    }

    public static void addRecentItemId(String id) {
        AnkiConfig.ensureLoaded();
        if (id == null || id.isBlank()) {
            return;
        }
        recentItemIds.remove(id);
        recentItemIds.add(0, id);
        while (recentItemIds.size() > 30) {
            recentItemIds.remove(recentItemIds.size() - 1);
        }
        AnkiConfig.save();
    }

    private static int uiAnimationLevelFromSpeed(float speed) {
        return Math.max(1, Math.min(10, Math.round(speed / 0.03f)));
    }

    private static void normalizeKeyBindings() {
        openItemEditorKeyCode = 78;
        openVillagerEditorKeyCode = openEntityEditorKeyCode = 44;
        openConfigMenuKeyCode = 79;
    }

    private static int normalizeEntityEditorKeyCode(int keyCode) {
        if (keyCode == 77 || keyCode == 86) {
            return 44;
        }
        return keyCode;
    }

    private static float uiAnimationSpeedForLevel(int level) {
        int clamped = Math.max(1, Math.min(10, level));
        return Math.max(0.03f, Math.min(0.45f, (float)clamped * 0.03f));
    }

    public static Path getExportPath() {
        Path gameDir = VersionCompat.get().getGameDir();
        Path exportDir = gameDir.resolve(nbtExportDir);
        try {
            Files.createDirectories(exportDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return exportDir;
    }

    public static Path getExportPath(String category) {
        Path base = AnkiConfig.getExportPath();
        if (category != null && !category.isBlank()) {
            Path catDir = base.resolve(category.trim());
            try {
                Files.createDirectories(catDir, new FileAttribute[0]);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return catDir;
        }
        return base;
    }

    public static List<String> listExportCategories() {
        ArrayList<String> cats = new ArrayList<String>();
        Path base = AnkiConfig.getExportPath();
        if (!Files.isDirectory(base, new LinkOption[0])) {
            return cats;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(base);){
            for (Path p : stream) {
                if (!Files.isDirectory(p, new LinkOption[0])) continue;
                cats.add(p.getFileName().toString());
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        Collections.sort(cats);
        return cats;
    }

    private static Map<String, List<String>> defaultItemGroups() {
        LinkedHashMap<String, List<String>> m = new LinkedHashMap<String, List<String>>();
        m.put("\u5e38\u7528\u65b9\u5757", List.of("minecraft:stone", "minecraft:dirt", "minecraft:glass", "minecraft:oak_planks", "minecraft:cobblestone"));
        m.put("\u5e38\u7528\u6750\u6599", List.of("minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:diamond", "minecraft:emerald", "minecraft:redstone"));
        return m;
    }

    private static Map<String, List<String>> parseItemGroups(JsonObject obj) {
        LinkedHashMap<String, List<String>> out = new LinkedHashMap<String, List<String>>();
        for (Map.Entry e : obj.entrySet()) {
            if (!((JsonElement)e.getValue()).isJsonArray()) continue;
            ArrayList<String> ids = new ArrayList<String>();
            for (JsonElement idEl : ((JsonElement)e.getValue()).getAsJsonArray()) {
                String id;
                if (!idEl.isJsonPrimitive() || (id = idEl.getAsString()).isBlank()) continue;
                ids.add(id);
            }
            if (ids.isEmpty()) continue;
            out.put((String)e.getKey(), ids);
        }
        if (out.isEmpty()) {
            return AnkiConfig.defaultItemGroups();
        }
        return out;
    }

    private static JsonObject writeItemGroups(Map<String, List<String>> groups) {
        JsonObject out = new JsonObject();
        for (Map.Entry<String, List<String>> e : groups.entrySet()) {
            JsonArray arr = new JsonArray();
            for (String id : e.getValue()) {
                arr.add(id);
            }
            out.add(e.getKey(), (JsonElement)arr);
        }
        return out;
    }

    private static String normalizeGroupName(String in) {
        return in == null ? "" : in.trim();
    }

    private static Map<String, List<String>> sanitizeGroups(Map<String, List<String>> in) {
        LinkedHashMap<String, List<String>> out = new LinkedHashMap<String, List<String>>();
        if (in == null) {
            return out;
        }
        for (Map.Entry<String, List<String>> e : in.entrySet()) {
            String name = AnkiConfig.normalizeGroupName(e.getKey());
            if (name.isEmpty()) continue;
            out.put(name, AnkiConfig.sanitizeItemIds(e.getValue()));
        }
        return out;
    }

    private static List<String> sanitizeItemIds(List<String> ids) {
        ArrayList<String> out = new ArrayList<String>();
        if (ids == null) {
            return out;
        }
        for (String id : ids) {
            String v = id == null ? "" : id.trim();
            if (v.isEmpty()) continue;
            out.add(v);
        }
        return out;
    }

    static {
        initialized = false;
        openItemEditorKeyCode = 78;
        openEntityEditorKeyCode = 44;
        openVillagerEditorKeyCode = 44;
        openConfigMenuKeyCode = 79;
        preferredItemEditor = "simple";
        showAdvancedTags = false;
        uiOpacity = 0.85f;
        treeExpandedByDefault = false;
        nbtExportDir = "ankinbt-config/save-nbt";
        autoLoadLastNbt = true;
        lastNbtFile = "";
        confirmOnClose = true;
        lastExportCategory = "";
        nativeFileDialogEnabled = false;
        attributeNotesEnabled = false;
        smartEntityEditorKey = true;
        villagerRequireProfession = true;
        entityLivePreview = true;
        configShowAdvanced = false;
        uiAccentPreset = 0;
        uiShadowEnabled = true;
        uiCompactLayout = false;
        uiAnimationEnabled = true;
        uiAnimationSpeed = 0.09f;
        uiSoundVolume = 0.7f;
        debugPanelEnabled = true;
        debugLogEnabled = false;
        debugFileSaveEnabled = false;
        recentItemIds = new ArrayList<String>();
        customItemGroups = AnkiConfig.defaultItemGroups();
    }
}

