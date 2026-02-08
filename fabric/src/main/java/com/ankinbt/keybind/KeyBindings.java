package com.ankinbt.keybind;

import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.SimpleEditorScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;

public class KeyBindings {

    private static final String CATEGORY = "key.categories.ankinbt";
    private static KeyMapping openEditorKey;
    private static boolean useSimpleMode = true;
    private static Field hoveredSlotField;

    public static void setUseSimpleMode(boolean simple) { useSimpleMode = simple; }

    public static void register() {
        openEditorKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.ankinbt.open_editor",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_N,
                CATEGORY
        ));

        // Cache the hoveredSlot field via reflection
        try {
            hoveredSlotField = AbstractContainerScreen.class.getDeclaredField("hoveredSlot");
            hoveredSlotField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // Try intermediary name as fallback
            try {
                for (Field f : AbstractContainerScreen.class.getDeclaredFields()) {
                    if (f.getType() == Slot.class) {
                        hoveredSlotField = f;
                        hoveredSlotField.setAccessible(true);
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Normal gameplay: press N while holding item
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openEditorKey.consumeClick()) {
                if (client.player == null || client.screen != null) continue;
                ItemStack held = client.player.getMainHandItem();
                if (held.isEmpty()) held = client.player.getOffhandItem();
                if (held.isEmpty()) {
                    client.player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("ankinbt.message.no_item"), true);
                    continue;
                }
                openEditor(held, -1);
            }
        });

        // Inventory screen: press N while hovering over a slot
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                ScreenKeyboardEvents.beforeKeyPress(screen).register((scr, key, scancode, modifiers) -> {
                    if (openEditorKey.matches(key, scancode)) {
                        Slot hoveredSlot = getHoveredSlot(containerScreen);
                        if (hoveredSlot != null && hoveredSlot.hasItem()) {
                            ItemStack stack = hoveredSlot.getItem();
                            int slotIndex = hoveredSlot.getContainerSlot();
                            openEditor(stack, slotIndex);
                        }
                    }
                });
            }
        });
    }

    private static Slot getHoveredSlot(AbstractContainerScreen<?> screen) {
        if (hoveredSlotField != null) {
            try {
                return (Slot) hoveredSlotField.get(screen);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static void openEditor(ItemStack stack, int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (useSimpleMode) {
            mc.setScreen(new SimpleEditorScreen(stack, slot));
        } else {
            mc.setScreen(new NbtEditorScreen(stack));
        }
    }
}
