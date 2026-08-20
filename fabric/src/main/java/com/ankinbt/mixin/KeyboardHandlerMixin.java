package com.ankinbt.mixin;

import com.ankinbt.gui.InventoryEditorOverlay;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void ankinbt$forwardOverlayText(long windowPointer, int codePoint, int modifiers,
                                            CallbackInfo callback) {
        if (windowPointer == minecraft.getWindow().getWindow()
                && InventoryEditorOverlay.handleCharTyped(minecraft.screen, codePoint, modifiers)) {
            callback.cancel();
        }
    }
}
