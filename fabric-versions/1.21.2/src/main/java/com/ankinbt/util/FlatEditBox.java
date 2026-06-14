/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 */
package com.ankinbt.util;

import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;

public class FlatEditBox
extends class_342 {
    private static final int TEXT_PAD_X = 4;
    private int bgColor = 1075059755;
    private int borderColor = -13878436;
    private int focusedBorderColor = -10262799;

    public FlatEditBox(class_327 font, int x, int y, int width, int height, class_2561 message) {
        super(font, x, y, width, height, message);
        try {
            this.method_1858(false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.method_1868(-2497806);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.method_1860(-7429177);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public FlatEditBox setThemeColors(int bgColor, int borderColor, int focusedBorderColor) {
        this.bgColor = bgColor;
        this.borderColor = borderColor;
        this.focusedBorderColor = focusedBorderColor;
        return this;
    }

    public void method_48579(class_332 g, int mx, int my, float partialTick) {
        int x = this.method_46426();
        int y = this.method_46427();
        int w = this.method_25368();
        int h = this.method_25364();
        int edge = this.method_25370() ? this.focusedBorderColor : this.borderColor;
        g.method_25294(x, y, x + w, y + h, this.bgColor);
        g.method_25294(x, y, x + w, y + 1, edge);
        g.method_25294(x, y + h - 1, x + w, y + h, edge);
        g.method_25294(x, y, x + 1, y + h, edge);
        g.method_25294(x + w - 1, y, x + w, y + h, edge);
        int textY = y + Math.max(1, (h - 8) / 2);
        try {
            this.method_46421(x + TEXT_PAD_X);
            this.method_46419(textY);
            this.method_25358(Math.max(1, w - TEXT_PAD_X * 2));
            super.method_48579(g, mx, my, partialTick);
        }
        finally {
            this.method_46421(x);
            this.method_46419(y);
            this.method_25358(w);
        }
    }
}

