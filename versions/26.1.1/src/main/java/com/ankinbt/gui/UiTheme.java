package com.ankinbt.gui;

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
            case 2 -> 0xFFF97316;
            case 3 -> 0xFFE11D48;
            default -> 0xFF6366F1;
        };
    }

    public static int scrim(float opacity, float anim) {
        return withAlpha(0x000000, (int) ((90 + 90 * opacity) * anim));
    }

    public static int panel(float opacity, float anim) {
        return withAlpha(0x080810, (int) ((175 + 65 * opacity) * anim));
    }

    public static int card(float opacity, float anim) {
        return withAlpha(0x0C0C18, (int) ((160 + 70 * opacity) * anim));
    }

    public static int header(float opacity, float anim) {
        return withAlpha(0x101020, (int) ((180 + 65 * opacity) * anim));
    }

    public static int border(float opacity, float anim) {
        return withAlpha(0x222236, (int) ((180 + 75 * opacity) * anim));
    }

    public static int shadow(float opacity, float anim, boolean enabled) {
        if (!enabled) return 0;
        return withAlpha(0x000000, (int) ((95 + 95 * opacity) * anim));
    }

    public static float approach(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    public static int withAlpha(int rgb, int alpha) {
        int a = clamp(alpha);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    public static int clamp(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }
}
