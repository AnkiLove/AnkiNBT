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
    private static float uiOpacity = 0.85f;
    private static boolean treeExpandedByDefault = false;
    private static String nbtExportDir = "ankinbt-config/save-nbt";
    private static boolean autoLoadLastNbt = true;
    private static String lastNbtFile = "";
    private static boolean confirmOnClose = true;
    private static String lastExportCategory = "";

    private static boolean smartEntityEditorKey = true;
    private static boolean villagerRequireProfession = true;
    private static boolean entityLivePreview = true;
    private static boolean configShowAdvanced = false;

    private static int uiAccentPreset = 0;
    private static boolean uiShadowEnabled = true;
    private static boolean uiCompactLayout = false;
    private static boolean uiAnimationEnabled = true;
    private static float uiAnimationSpeed = 0.09f;
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
            boolean hasEntityKey = obj.has("openEntityEditorKeyCode");
            boolean hasVillagerKey = obj.has("openVillagerEditorKeyCode");
            if (obj.has("openKeyCode")) openItemEditorKeyCode = obj.get("openKeyCode").getAsInt();
            if (obj.has("openItemEditorKeyCode")) openItemEditorKeyCode = obj.get("openItemEditorKeyCode").getAsInt();
            if (hasEntityKey) openEntityEditorKeyCode = obj.get("openEntityEditorKeyCode").getAsInt();
            if (hasVillagerKey) openVillagerEditorKeyCode = obj.get("openVillagerEditorKeyCode").getAsInt();
            if (obj.has("openConfigMenuKeyCode")) openConfigMenuKeyCode = obj.get("openConfigMenuKeyCode").getAsInt();
            if (obj.has("preferredItemEditor")) preferredItemEditor = obj.get("preferredItemEditor").getAsString();
            if (obj.has("showAdvancedTags")) showAdvancedTags = obj.get("showAdvancedTags").getAsBoolean();
            if (obj.has("uiOpacity")) uiOpacity = obj.get("uiOpacity").getAsFloat();
            if (obj.has("treeExpandedByDefault")) treeExpandedByDefault = obj.get("treeExpandedByDefault").getAsBoolean();
            if (obj.has("nbtExportDir")) nbtExportDir = obj.get("nbtExportDir").getAsString();
            if (obj.has("autoLoadLastNbt")) autoLoadLastNbt = obj.get("autoLoadLastNbt").getAsBoolean();
            if (obj.has("lastNbtFile")) lastNbtFile = obj.get("lastNbtFile").getAsString();
            if (obj.has("confirmOnClose")) confirmOnClose = obj.get("confirmOnClose").getAsBoolean();
            if (obj.has("lastExportCategory")) lastExportCategory = obj.get("lastExportCategory").getAsString();

            if (obj.has("smartEntityEditorKey")) smartEntityEditorKey = obj.get("smartEntityEditorKey").getAsBoolean();
            if (obj.has("villagerRequireProfession")) villagerRequireProfession = obj.get("villagerRequireProfession").getAsBoolean();
            if (obj.has("entityLivePreview")) entityLivePreview = obj.get("entityLivePreview").getAsBoolean();
            if (obj.has("configShowAdvanced")) configShowAdvanced = obj.get("configShowAdvanced").getAsBoolean();
            if (obj.has("uiAccentPreset")) uiAccentPreset = obj.get("uiAccentPreset").getAsInt();
            if (obj.has("uiShadowEnabled")) uiShadowEnabled = obj.get("uiShadowEnabled").getAsBoolean();
            if (obj.has("uiCompactLayout")) uiCompactLayout = obj.get("uiCompactLayout").getAsBoolean();
            if (obj.has("uiAnimationEnabled")) uiAnimationEnabled = obj.get("uiAnimationEnabled").getAsBoolean();
            if (obj.has("uiAnimationSpeed")) uiAnimationSpeed = obj.get("uiAnimationSpeed").getAsFloat();
            if (obj.has("uiSoundVolume")) uiSoundVolume = obj.get("uiSoundVolume").getAsFloat();
            if (obj.has("debugPanelEnabled")) debugPanelEnabled = obj.get("debugPanelEnabled").getAsBoolean();
            if (obj.has("debugLogEnabled")) debugLogEnabled = obj.get("debugLogEnabled").getAsBoolean();
            if (obj.has("debugFileSaveEnabled")) debugFileSaveEnabled = obj.get("debugFileSaveEnabled").getAsBoolean();

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

            if (!hasEntityKey || openEntityEditorKeyCode == InputConstants.KEY_M) {
                openEntityEditorKeyCode = InputConstants.KEY_COMMA;
            }
            if (!hasVillagerKey || openVillagerEditorKeyCode == InputConstants.KEY_V) {
                openVillagerEditorKeyCode = InputConstants.KEY_COMMA;
            }
            normalizeKeyBindings();

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

            obj.addProperty("smartEntityEditorKey", smartEntityEditorKey);
            obj.addProperty("villagerRequireProfession", villagerRequireProfession);
            obj.addProperty("entityLivePreview", entityLivePreview);
            obj.addProperty("configShowAdvanced", configShowAdvanced);
            obj.addProperty("uiAccentPreset", uiAccentPreset);
            obj.addProperty("uiShadowEnabled", uiShadowEnabled);
            obj.addProperty("uiCompactLayout", uiCompactLayout);
            obj.addProperty("uiAnimationEnabled", uiAnimationEnabled);
            obj.addProperty("uiAnimationSpeed", uiAnimationSpeed);
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

    public static boolean isSmartEntityEditorKey() { ensureLoaded(); return smartEntityEditorKey; }
    public static boolean isVillagerRequireProfession() { ensureLoaded(); return villagerRequireProfession; }
    public static boolean isEntityLivePreview() { ensureLoaded(); return entityLivePreview; }
    public static boolean isConfigShowAdvanced() { ensureLoaded(); return configShowAdvanced; }
    public static int getUiAccentPreset() { ensureLoaded(); return uiAccentPreset; }
    public static boolean isUiShadowEnabled() { ensureLoaded(); return uiShadowEnabled; }
    public static boolean isUiCompactLayout() { ensureLoaded(); return uiCompactLayout; }
    public static boolean isUiAnimationEnabled() { ensureLoaded(); return uiAnimationEnabled; }
    public static float getUiAnimationSpeed() { ensureLoaded(); return uiAnimationSpeed; }
    public static int getUiAnimationSpeedLevel() { ensureLoaded(); return uiAnimationLevelFromSpeed(uiAnimationSpeed); }
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

    public static void setSmartEntityEditorKey(boolean v) { smartEntityEditorKey = v; save(); }
    public static void setVillagerRequireProfession(boolean v) { villagerRequireProfession = v; save(); }
    public static void setEntityLivePreview(boolean v) { entityLivePreview = v; save(); }
    public static void setConfigShowAdvanced(boolean v) { configShowAdvanced = v; save(); }
    public static void setUiAccentPreset(int v) { uiAccentPreset = Math.floorMod(v, 4); save(); }
    public static void setUiShadowEnabled(boolean v) { uiShadowEnabled = v; save(); }
    public static void setUiCompactLayout(boolean v) { uiCompactLayout = v; save(); }
    public static void setUiAnimationEnabled(boolean v) { uiAnimationEnabled = v; save(); }
    public static void setUiAnimationSpeed(float v) { uiAnimationSpeed = Math.max(UI_ANIMATION_LEVEL_STEP, Math.min(0.45f, v)); save(); }
    public static void setUiAnimationSpeedLevel(int level) { uiAnimationSpeed = uiAnimationSpeedForLevel(level); save(); }
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
        openItemEditorKeyCode = InputConstants.KEY_N;
        openEntityEditorKeyCode = InputConstants.KEY_COMMA;
        openVillagerEditorKeyCode = openEntityEditorKeyCode;
        openConfigMenuKeyCode = InputConstants.KEY_O;
    }

    private static int normalizeEntityEditorKeyCode(int keyCode) {
        if (keyCode == InputConstants.KEY_M || keyCode == InputConstants.KEY_V) {
            return InputConstants.KEY_COMMA;
        }
        return keyCode;
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

