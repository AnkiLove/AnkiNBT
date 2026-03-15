package com.ankinbt.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class FlatEditBox extends EditBox {
    private int bgColor = 0x40141C2B;
    private int borderColor = 0xFF2C3B5C;
    private int focusedBorderColor = 0xFF6366F1;

    public FlatEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        try {
            setBordered(false);
        } catch (Throwable ignored) {}
        try {
            setTextColor(0xFFD9E2F2);
        } catch (Throwable ignored) {}
        try {
            setTextColorUneditable(0xFF8EA3C7);
        } catch (Throwable ignored) {}
    }

    public FlatEditBox setThemeColors(int bgColor, int borderColor, int focusedBorderColor) {
        this.bgColor = bgColor;
        this.borderColor = borderColor;
        this.focusedBorderColor = focusedBorderColor;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics g, int mx, int my, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        int edge = isFocused() ? focusedBorderColor : borderColor;

        g.fill(x, y, x + w, y + h, bgColor);
        g.fill(x, y, x + w, y + 1, edge);
        g.fill(x, y + h - 1, x + w, y + h, edge);
        g.fill(x, y, x + 1, y + h, edge);
        g.fill(x + w - 1, y, x + w, y + h, edge);
        super.renderWidget(g, mx, my, partialTick);
    }
}
