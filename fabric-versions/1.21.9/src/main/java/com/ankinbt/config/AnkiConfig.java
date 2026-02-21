package com.ankinbt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import com.ankinbt.compat.VersionCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnkiConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("AnkiNBT");
    private static Path configPath;
    private static boolean initialized = false;

    private static int openKeyCode = InputConstants.KEY_N;
    private static boolean showAdvancedTags = false;
    private static float uiOpacity = 0.85f;
    private static boolean treeExpandedByDefault = false;
    private static String nbtExportDir = "ankinbt-config/save-nbt";
    private static boolean autoLoadLastNbt = true;
    private static String lastNbtFile = "";
    private static boolean confirmOnClose = true;
    private static String lastExportCategory = "";

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
            if (obj.has("openKeyCode")) openKeyCode = obj.get("openKeyCode").getAsInt();
            if (obj.has("showAdvancedTags")) showAdvancedTags = obj.get("showAdvancedTags").getAsBoolean();
            if (obj.has("uiOpacity")) uiOpacity = obj.get("uiOpacity").getAsFloat();
            if (obj.has("treeExpandedByDefault")) treeExpandedByDefault = obj.get("treeExpandedByDefault").getAsBoolean();
            if (obj.has("nbtExportDir")) nbtExportDir = obj.get("nbtExportDir").getAsString();
            if (obj.has("autoLoadLastNbt")) autoLoadLastNbt = obj.get("autoLoadLastNbt").getAsBoolean();
            if (obj.has("lastNbtFile")) lastNbtFile = obj.get("lastNbtFile").getAsString();
            if (obj.has("confirmOnClose")) confirmOnClose = obj.get("confirmOnClose").getAsBoolean();
            if (obj.has("lastExportCategory")) lastExportCategory = obj.get("lastExportCategory").getAsString();
            LOGGER.info("Config loaded from {}", configPath);
        } catch (Exception e) {
            LOGGER.warn("Failed to load config: {}", e.getMessage());
        }
    }

    public static void save() {
        ensureLoaded();
        if (configPath == null) return;
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("openKeyCode", openKeyCode);
            obj.addProperty("showAdvancedTags", showAdvancedTags);
            obj.addProperty("uiOpacity", uiOpacity);
            obj.addProperty("treeExpandedByDefault", treeExpandedByDefault);
            obj.addProperty("nbtExportDir", nbtExportDir);
            obj.addProperty("autoLoadLastNbt", autoLoadLastNbt);
            obj.addProperty("lastNbtFile", lastNbtFile);
            obj.addProperty("confirmOnClose", confirmOnClose);
            obj.addProperty("lastExportCategory", lastExportCategory);
            Files.writeString(configPath, GSON.toJson(obj));
        } catch (Exception e) {
            LOGGER.warn("Failed to save config: {}", e.getMessage());
        }
    }

    // Getters
    public static int getOpenKeyCode() { ensureLoaded(); return openKeyCode; }
    public static String getKeyName() { return VersionCompat.get().getKeyDisplayName(openKeyCode); }
    public static boolean showAdvancedTags() { ensureLoaded(); return showAdvancedTags; }
    public static float getUiOpacity() { ensureLoaded(); return uiOpacity; }
    public static boolean isTreeExpandedByDefault() { ensureLoaded(); return treeExpandedByDefault; }
    public static String getNbtExportDir() { ensureLoaded(); return nbtExportDir; }
    public static boolean isAutoLoadLastNbt() { ensureLoaded(); return autoLoadLastNbt; }
    public static String getLastNbtFile() { ensureLoaded(); return lastNbtFile; }
    public static boolean isConfirmOnClose() { ensureLoaded(); return confirmOnClose; }
    public static String getLastExportCategory() { ensureLoaded(); return lastExportCategory; }

    // Setters
    public static void setShowAdvancedTags(boolean v) { showAdvancedTags = v; save(); }
    public static void setUiOpacity(float v) { uiOpacity = Math.max(0.3f, Math.min(1.0f, v)); save(); }
    public static void setTreeExpandedByDefault(boolean v) { treeExpandedByDefault = v; save(); }
    public static void setNbtExportDir(String v) { nbtExportDir = v; save(); }
    public static void setAutoLoadLastNbt(boolean v) { autoLoadLastNbt = v; save(); }
    public static void setLastNbtFile(String v) { lastNbtFile = v; save(); }
    public static void setConfirmOnClose(boolean v) { confirmOnClose = v; save(); }
    public static void setLastExportCategory(String v) { lastExportCategory = v; save(); }

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
}
