/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  com.ankinbt.compat.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.network.chat.Component
 */
package com.ankinbt.util;

import net.minecraft.client.gui.Font;
import com.ankinbt.compat.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class FlatEditBox
extends EditBox {
    private static final int TEXT_PAD_X = 4;
    private int bgColor = 1075059755;
    private int borderColor = -13878436;
    private int focusedBorderColor = -10262799;

    public FlatEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        try {
            this.setBordered(false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.setTextColor(-2497806);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.setTextColorUneditable(-7429177);
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

    @Override
    public void extractWidgetRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        renderWidget(new com.ankinbt.compat.GuiGraphics(g), mx, my, partialTick);
    }

    public void renderWidget(GuiGraphics g, int mx, int my, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        int edge = this.isFocused() ? this.focusedBorderColor : this.borderColor;
        g.fill(x, y, x + w, y + h, this.bgColor);
        g.fill(x, y, x + w, y + 1, edge);
        g.fill(x, y + h - 1, x + w, y + h, edge);
        g.fill(x, y, x + 1, y + h, edge);
        g.fill(x + w - 1, y, x + w, y + h, edge);
        int textY = y + Math.max(1, (h - 8) / 2);
        try {
            this.setX(x + TEXT_PAD_X);
            this.setY(textY);
            this.setWidth(Math.max(1, w - TEXT_PAD_X * 2));
            super.extractWidgetRenderState(g.unwrap(), mx, my, partialTick);
        }
        finally {
            this.setX(x);
            this.setY(y);
            this.setWidth(w);
        }
    }
}





