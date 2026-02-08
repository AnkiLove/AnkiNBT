package com.ankinbt.config;

import com.mojang.blaze3d.platform.InputConstants;

public class AnkiConfig {

    private static int openKeyCode = InputConstants.KEY_N;
    private static boolean showAdvancedTags = false;
    private static float uiOpacity = 0.85f;
    private static boolean treeExpandedByDefault = false;

    public static void init() {}

    public static int getOpenKeyCode() { return openKeyCode; }

    public static String getKeyName() {
        return InputConstants.getKey("key.keyboard.n").getDisplayName().getString();
    }

    public static boolean showAdvancedTags() { return showAdvancedTags; }
    public static float getUiOpacity() { return uiOpacity; }
    public static boolean isTreeExpandedByDefault() { return treeExpandedByDefault; }
    public static void setShowAdvancedTags(boolean value) { showAdvancedTags = value; }
    public static void setUiOpacity(float value) { uiOpacity = Math.max(0.3f, Math.min(1.0f, value)); }
    public static void setTreeExpandedByDefault(boolean value) { treeExpandedByDefault = value; }
}
