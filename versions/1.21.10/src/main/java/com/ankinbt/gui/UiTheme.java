package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;

public final class UiTheme {

    private UiTheme() {}

    public static final int TXT_TITLE = 0xFFE2E8F0;
    public static final int TXT_MAIN = 0xFFE2E8F0;
    public static final int TXT_DIM = 0xFF94A3B8;
    public static final int TXT_OK = 0xFF22C55E;
    public static final int TXT_ERR = 0xFFEF4444;

    public static int accent(int preset) {
        return switch (preset) {
            case 1 -> 0xFF10B981;
            case 2 -> 0xFFF59E0B;
            case 3 -> 0xFFE11D48;
            case 4 -> 0xFF000000 | parseRgb(AnkiConfig.getUiAccentHex(), 0x38BDF8);
            default -> 0xFF38BDF8;
        };
    }

    public static int baseRgb() {
        return switch (AnkiConfig.getUiBackgroundPreset()) {
            case 1 -> 0x020202;
            case 2 -> 0x17191C;
            case 3 -> 0xF0F2F4;
            case 4 -> parseRgb(AnkiConfig.getUiBackgroundHex(), 0x080B10);
            default -> 0x080B10;
        };
    }

    public static boolean isLight() {
        int rgb = baseRgb();
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        return r * 299 + g * 587 + b * 114 > 160000;
    }

    public static int textMain() { return isLight() ? 0xFF17191C : TXT_MAIN; }
    public static int textDim() { return isLight() ? 0xFF5E6670 : TXT_DIM; }

    public static int surface(float opacity, float anim) {
        int alpha = Math.round(255 * clampUnit(opacity) * anim);
        if ("minimal".equals(AnkiConfig.getUiVisualStyle())) alpha = Math.round(alpha * 0.86f);
        return withAlpha(baseRgb(), alpha);
    }

    public static int toolbar(float opacity, float anim) {
        int rgb = isLight() ? mixRgb(baseRgb(), 0xFFFFFF, 0.45f) : mixRgb(baseRgb(), 0xFFFFFF, 0.045f);
        float styleAlpha = "outline".equals(AnkiConfig.getUiVisualStyle()) ? 1.0f : 0.94f;
        return withAlpha(rgb, Math.round(255 * clampUnit(opacity) * styleAlpha * anim));
    }

    public static int themedBorder(float opacity, float anim) {
        int rgb = isLight() ? 0xAEB4BC : mixRgb(baseRgb(), 0xFFFFFF, 0.14f);
        float styleAlpha = "outline".equals(AnkiConfig.getUiVisualStyle()) ? 1.0f : 0.82f;
        if ("minimal".equals(AnkiConfig.getUiVisualStyle())) styleAlpha = 0.58f;
        return withAlpha(rgb, Math.round(255 * clampUnit(opacity) * styleAlpha * anim));
    }

    public static int scrim(float opacity, float anim) {
        return withAlpha(0x000000, Math.round(150 * clampUnit(opacity) * anim));
    }

    public static int panel(float opacity, float anim) {
        return surface(opacity, anim);
    }

    public static int card(float opacity, float anim) {
        int rgb = isLight() ? mixRgb(baseRgb(), 0xFFFFFF, 0.34f) : mixRgb(baseRgb(), 0xFFFFFF, 0.035f);
        int alpha = Math.round(255 * clampUnit(opacity) * anim);
        return withAlpha(rgb, Math.min(255, alpha + Math.round(12 * anim)));
    }

    public static int header(float opacity, float anim) {
        return toolbar(opacity, anim);
    }

    public static int border(float opacity, float anim) {
        return themedBorder(opacity, anim);
    }

    public static int shadow(float opacity, float anim, boolean enabled) {
        if (!enabled) return 0;
        return withAlpha(0x000000, Math.round(135 * clampUnit(opacity) * anim));
    }

    public static float approach(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    public static int mix(int from, int to, float amount) {
        float t = Math.max(0.0f, Math.min(1.0f, amount));
        int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t);
        int r = Math.round(((from >>> 16) & 0xFF) + (((to >>> 16) & 0xFF) - ((from >>> 16) & 0xFF)) * t);
        int g = Math.round(((from >>> 8) & 0xFF) + (((to >>> 8) & 0xFF) - ((from >>> 8) & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int withAlpha(int rgb, int alpha) {
        int a = clamp(alpha);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    public static int clamp(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }

    private static float clampUnit(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static int parseRgb(String value, int fallback) {
        try {
            String raw = value == null ? "" : value.trim().replace("#", "");
            return raw.length() == 6 ? Integer.parseInt(raw, 16) : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int mixRgb(int from, int to, float amount) {
        return mix(0xFF000000 | from, 0xFF000000 | to, amount) & 0x00FFFFFF;
    }
}
