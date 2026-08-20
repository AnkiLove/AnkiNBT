package com.ankinbt.mixin;

import com.ankinbt.gui.InventoryEditorOverlay;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.PreeditEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow
    private Minecraft minecraft;

    /**
     * Container screens stay active while AnkiNBT draws its editor as an overlay.
     * Let vanilla deliver the event first so GLFW/IME composition is never
     * short-circuited, then mirror the committed codepoint into the overlay.
     */
    @Inject(method = "charTyped", at = @At("TAIL"))
    private void ankinbt$forwardOverlayCharacter(long window, CharacterEvent event, CallbackInfo ci) {
        if (minecraft.screen != null) {
            // The container has completed its normal dispatch; mirror the
            // committed codepoint into the overlay's focused editor field.
            InventoryEditorOverlay.handleCharTyped(minecraft.screen, event);
        }
    }

    /** Forward IME composition text without cancelling the native callback. */
    @Inject(method = "preeditCallback", at = @At("TAIL"))
    private void ankinbt$forwardOverlayPreedit(long window, PreeditEvent event, CallbackInfo ci) {
        if (minecraft.screen != null) {
            InventoryEditorOverlay.handlePreedit(minecraft.screen, event);
        }
    }
}
