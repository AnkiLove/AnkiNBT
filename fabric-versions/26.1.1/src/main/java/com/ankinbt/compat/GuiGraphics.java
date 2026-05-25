package com.ankinbt.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Small source-compatibility bridge for AnkiNBT's pre-26.1 screen code.
 */
public final class GuiGraphics {
    private final GuiGraphicsExtractor delegate;

    public GuiGraphics(GuiGraphicsExtractor delegate) {
        this.delegate = delegate;
    }

    public GuiGraphicsExtractor unwrap() {
        return delegate;
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        delegate.fill(x1, y1, x2, y2, color);
    }

    public void enableScissor(int x1, int y1, int x2, int y2) {
        delegate.enableScissor(x1, y1, x2, y2);
    }

    public void disableScissor() {
        delegate.disableScissor();
    }

    public int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        delegate.text(font, text, x, y, color, shadow);
        return font.width(text);
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        delegate.text(font, text, x, y, color, shadow);
        return font.width(text);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        delegate.text(font, text, x, y, color, shadow);
        return font.width(text);
    }

    public void renderItem(ItemStack stack, int x, int y) {
        delegate.item(stack, x, y);
    }

    public void renderItem(ItemStack stack, int x, int y, int seed) {
        delegate.item(stack, x, y, seed);
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        delegate.itemDecorations(font, stack, x, y);
    }

    public void renderTooltip(Font font, Component tooltip, int x, int y) {
        delegate.setTooltipForNextFrame(font, tooltip, x, y);
    }

    public void renderTooltip(Font font, List<ClientTooltipComponent> components, int x, int y,
                              ClientTooltipPositioner positioner, Object focusedTooltip) {
        delegate.tooltip(font, components, x, y, positioner, null);
    }
}

