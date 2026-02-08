package com.ankinbt.keybind;

import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.SimpleEditorScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class KeyBindings {

    private static final String CATEGORY = "key.categories.ankinbt";
    private static KeyMapping openEditorKey;
    private static boolean useSimpleMode = true;

    public static void setUseSimpleMode(boolean simple) { useSimpleMode = simple; }

    public static void register(IEventBus modEventBus) {
        openEditorKey = new KeyMapping(
                "key.ankinbt.open_editor",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_N,
                CATEGORY
        );
        modEventBus.addListener(KeyBindings::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onClientTick);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenKeyPress);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openEditorKey);
    }

    /** Normal gameplay: press N while holding item */
    private static void onClientTick(ClientTickEvent.Post event) {
        if (openEditorKey == null || !openEditorKey.consumeClick()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) held = mc.player.getOffhandItem();
        if (held.isEmpty()) {
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("ankinbt.message.no_item"), true);
            return;
        }
        openEditor(held, -1);
    }

    /** Inventory screen: press N while hovering over a slot */
    private static void onScreenKeyPress(ScreenEvent.KeyPressed.Pre event) {
        if (openEditorKey == null) return;
        if (!openEditorKey.matches(event.getKeyCode(), event.getScanCode())) return;
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        Slot hoveredSlot = containerScreen.getSlotUnderMouse();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();
        int slotIndex = hoveredSlot.getContainerSlot();
        event.setCanceled(true);
        openEditor(stack, slotIndex);
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
