package com.ankinbt.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/** Normalizes the pre-26 renderer to the editor's small drawing surface. */
final class LegacyGuiGraphics {
    private final GuiGraphics delegate;

    LegacyGuiGraphics(GuiGraphics delegate) {
        this.delegate = delegate;
    }

    GuiGraphics unwrap() {
        return delegate;
    }

    void fill(int x1, int y1, int x2, int y2, int color) {
        delegate.fill(x1, y1, x2, y2, color);
    }

    void enableScissor(int x1, int y1, int x2, int y2) {
        delegate.enableScissor(x1, y1, x2, y2);
    }

    void disableScissor() {
        delegate.disableScissor();
    }

    /**
     * Submits a legacy immediate-mode GUI frame after the screen has balanced
     * its own clipping calls. This finalizer must never pop the scissor stack:
     * doing so after a balanced screen crashes the client with an underflow.
     */
    void finishFrame() {
        delegate.flush();
    }

    int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        return delegate.drawString(font, text, x, y, color, shadow);
    }

    int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        return delegate.drawString(font, text, x, y, color, shadow);
    }

    int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        return delegate.drawString(font, text, x, y, color, shadow);
    }

    void renderItem(ItemStack stack, int x, int y) {
        delegate.renderItem(stack, x, y);
    }

    void renderItem(ItemStack stack, int x, int y, int seed) {
        delegate.renderItem(stack, x, y, seed);
    }

    void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        delegate.renderItemDecorations(font, stack, x, y);
    }

    void blit(ResourceLocation texture, int x, int y, int width, int height) {
        delegate.blit(texture, x, y, 0.0f, 0.0f, width, height, width, height);
    }

    void renderTooltip(Font font, Component tooltip, int x, int y) {
        delegate.renderTooltip(font, tooltip, x, y);
    }

    void renderTooltip(Font font, ItemStack stack, int x, int y) {
        delegate.renderTooltip(font, stack, x, y);
    }

}
