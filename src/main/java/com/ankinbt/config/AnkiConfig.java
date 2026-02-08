package com.ankinbt.config;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * Simple configuration holder for AnkiNBT.
 * Keeps defaults minimal — modpack authors can extend via config files later.
 */
public class AnkiConfig {

    private static int openKeyCode = InputConstants.KEY_N;
    private static boolean showAdvancedTags = false;
    private static float uiOpacity = 0.85f;
    private static boolean treeExpandedByDefault = false;

    public static void init() {
        // Future: load from file. For now, sensible defaults.
    }

    public static int getOpenKeyCode() {
        return openKeyCode;
    }

    public static String getKeyName() {
        return InputConstants.getKey(openKeyCode, -1).getDisplayName().getString();
    }

    public static boolean showAdvancedTags() {
        return showAdvancedTags;
    }

    public static float getUiOpacity() {
        return uiOpacity;
    }

    public static boolean isTreeExpandedByDefault() {
        return treeExpandedByDefault;
    }

    public static void setShowAdvancedTags(boolean value) {
        showAdvancedTags = value;
    }

    public static void setUiOpacity(float value) {
        uiOpacity = Math.max(0.3f, Math.min(1.0f, value));
    }

    public static void setTreeExpandedByDefault(boolean value) {
        treeExpandedByDefault = value;
    }
}
