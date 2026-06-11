/*
 * Decompiled with CFR 0.152.
 */
package com.ankinbt.gui;

public final class UiTheme {
    public static final int TXT_TITLE = -1906448;
    public static final int TXT_MAIN = -1906448;
    public static final int TXT_DIM = -7035976;
    public static final int TXT_OK = -14498466;
    public static final int TXT_ERR = -1096636;

    private UiTheme() {
    }

    public static int accent(int preset) {
        return switch (preset) {
            case 1 -> -15681151;
            case 2 -> -429290;
            case 3 -> -2024120;
            default -> -10262799;
        };
    }

    public static int scrim(float opacity, float anim) {
        return UiTheme.withAlpha(0, (int)((90.0f + 90.0f * opacity) * anim));
    }

    public static int panel(float opacity, float anim) {
        return UiTheme.withAlpha(526352, (int)((175.0f + 65.0f * opacity) * anim));
    }

    public static int card(float opacity, float anim) {
        return UiTheme.withAlpha(789528, (int)((160.0f + 70.0f * opacity) * anim));
    }

    public static int header(float opacity, float anim) {
        return UiTheme.withAlpha(0x101020, (int)((180.0f + 65.0f * opacity) * anim));
    }

    public static int border(float opacity, float anim) {
        return UiTheme.withAlpha(0x222236, (int)((180.0f + 75.0f * opacity) * anim));
    }

    public static int shadow(float opacity, float anim, boolean enabled) {
        if (!enabled) {
            return 0;
        }
        return UiTheme.withAlpha(0, (int)((95.0f + 95.0f * opacity) * anim));
    }

    public static float approach(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    public static int withAlpha(int rgb, int alpha) {
        int a = UiTheme.clamp(alpha);
        return a << 24 | rgb & 0xFFFFFF;
    }

    public static int clamp(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }
}

