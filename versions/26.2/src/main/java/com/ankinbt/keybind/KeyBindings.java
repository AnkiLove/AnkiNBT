package com.ankinbt.keybind;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.gui.AnkiConfigScreen;
import com.ankinbt.gui.EntityEditorScreen;
import com.ankinbt.gui.ImeSupport;
import com.ankinbt.gui.InventoryEditorOverlay;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.SimpleEditorScreen;
import com.ankinbt.gui.VillagerTradeEditorScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

public class KeyBindings {

    private static KeyMapping openItemEditorKey;
    private static KeyMapping openEntityEditorKey;
    private static KeyMapping openConfigMenuKey;

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(KeyBindings::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onClientTick);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenInit);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenRender);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenClosing);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenKeyPress);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenCharTyped);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenMousePress);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenMouseRelease);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenMouseDrag);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenMouseScroll);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        refreshMappingsFromConfig();
        event.register(openItemEditorKey);
        event.register(openEntityEditorKey);
        event.register(openConfigMenuKey);
    }

    private static void refreshMappingsFromConfig() {
        int entityKeyCode = normalizeEntityEditorKeyCode(AnkiConfig.getOpenEntityEditorKeyCode());
        if (entityKeyCode != AnkiConfig.getOpenEntityEditorKeyCode()) {
            AnkiConfig.setOpenEntityEditorKeyCode(entityKeyCode);
        }
        if (entityKeyCode != AnkiConfig.getOpenVillagerEditorKeyCode()) {
            AnkiConfig.setOpenVillagerEditorKeyCode(entityKeyCode);
        }
        openItemEditorKey = new KeyMapping(
                "key.ankinbt.open_editor",
                InputConstants.Type.KEYSYM,
                AnkiConfig.getOpenItemEditorKeyCode(),
                KeyMapping.Category.MISC
        );
        openEntityEditorKey = new KeyMapping(
                "key.ankinbt.open_entity_editor",
                InputConstants.Type.KEYSYM,
                entityKeyCode,
                KeyMapping.Category.MISC
        );
        openConfigMenuKey = new KeyMapping(
                "key.ankinbt.open_config_menu",
                InputConstants.Type.KEYSYM,
                AnkiConfig.getOpenConfigMenuKeyCode(),
                KeyMapping.Category.MISC
        );
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.gui.screen() != null) {
            InventoryEditorOverlay.tick(mc.gui.screen());
            drainScreenEditorClicks();
            return;
        }

        if (openConfigMenuKey != null && openConfigMenuKey.consumeClick()) {
            mc.setScreenAndShow(new AnkiConfigScreen(null));
            return;
        }

        if (openItemEditorKey != null && openItemEditorKey.consumeClick()) {
            ItemStack held = getHeldOrOffhand(mc);
            if (held.isEmpty()) {
                mc.player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("ankinbt.message.no_item"));
                return;
            }
            openItemEditor(held, getHeldInventorySlot(mc));
            return;
        }

        if (openEntityEditorKey != null && openEntityEditorKey.consumeClick()) {
            Entity looked = getLookedEntity(mc);
            if (looked != null) {
                openSmartEntityEditor(mc, looked, ItemStack.EMPTY, -1, mc.gui.screen());
                return;
            }

            ItemStack held = getHeldOrOffhand(mc);
            if (SpawnEggEditorHelper.isSpawnEgg(held)) {
                openSmartEntityEditor(mc, null, held, getHeldInventorySlot(mc), mc.gui.screen());
                return;
            }

            mc.player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("ankinbt.entity.target_hint"));
        }
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (ImeSupport.isAnkiScreen(event.getScreen())) {
            ImeSupport.screenOpened(event.getScreen());
        }
        if (event.getScreen() instanceof AbstractContainerScreen<?> containerScreen) {
            InventoryEditorOverlay.attach(containerScreen, openItemEditorKey);
        }
    }

    private static void onScreenRender(ScreenEvent.Render.Post event) {
        InventoryEditorOverlay.render(event.getScreen(), event.getGuiGraphics(), event.getMouseX(),
                event.getMouseY(), event.getPartialTick());
    }

    private static void onScreenClosing(ScreenEvent.Closing event) {
        ImeSupport.screenRemoved(event.getScreen());
        InventoryEditorOverlay.screenClosing(event.getScreen());
    }

    private static void onScreenCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (InventoryEditorOverlay.handleCharTyped(event.getScreen(), event.getCharacterEvent())) {
            event.setCanceled(true);
        }
    }

    private static void onScreenMousePress(ScreenEvent.MouseButtonPressed.Pre event) {
        if (InventoryEditorOverlay.handleMouseClick(event.getScreen(), event.getMouseButtonEvent())) {
            event.setCanceled(true);
        }
    }

    private static void onScreenMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        if (InventoryEditorOverlay.handleMouseRelease(event.getScreen(), event.getMouseButtonEvent())) {
            event.setCanceled(true);
        }
    }

    private static void onScreenMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        if (InventoryEditorOverlay.handleMouseDrag(event.getScreen(), event.getMouseButtonEvent(),
                event.getDragX(), event.getDragY())) {
            event.setCanceled(true);
        }
    }

    private static void onScreenMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (InventoryEditorOverlay.handleMouseScroll(event.getScreen(), event.getMouseX(), event.getMouseY(),
                event.getScrollDeltaX(), event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    private static void onScreenKeyPress(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        if (openConfigMenuKey != null && openConfigMenuKey.matches(event.getKeyEvent())) {
            event.setCanceled(true);
            InventoryEditorOverlay.openModal(containerScreen, new AnkiConfigScreen(event.getScreen()));
            return;
        }

        if (InventoryEditorOverlay.handleKeyPressed(event.getScreen(), event.getKeyEvent(), openItemEditorKey)) {
            event.setCanceled(true);
            return;
        }

        Slot hoveredSlot = containerScreen.getSlotUnderMouse();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();
        int slotIndex = KeyBindings.getMenuSlotIndex(containerScreen, hoveredSlot);

        if (openEntityEditorKey != null && openEntityEditorKey.matches(event.getKeyEvent())) {
            if (!SpawnEggEditorHelper.isSpawnEgg(stack)) return;
            event.setCanceled(true);
            openSmartEntityEditor(Minecraft.getInstance(), null, stack, slotIndex, event.getScreen());
        }
    }

    private static void drainScreenEditorClicks() {
        drainClicks(openItemEditorKey);
        drainClicks(openEntityEditorKey);
        drainClicks(openConfigMenuKey);
    }

    private static void drainClicks(KeyMapping mapping) {
        if (mapping == null) return;
        while (mapping.consumeClick()) {
            // The active screen has already received the physical key event.
        }
    }

    private static int getMenuSlotIndex(AbstractContainerScreen<?> screen, Slot slot) {
        if (slot == null) {
            return -1;
        }
        int playerSlot = KeyBindings.getPlayerInventorySlot(slot);
        if (playerSlot >= 0) {
            return KeyBindings.creativePacketSlotFromPlayerInventory(playerSlot);
        }
        int menuIndex = KeyBindings.findSlotIndex(screen, slot);
        int menuPlayerSlot = KeyBindings.playerInventorySlotFromCreativePacket(menuIndex);
        if (menuPlayerSlot >= 0 && KeyBindings.isPlayerInventorySlot(slot, menuPlayerSlot)) {
            return menuIndex;
        }
        return -1;
    }

    private static int getPlayerInventorySlot(Slot slot) {
        int containerSlot = slot.getContainerSlot();
        if (containerSlot < 0 || containerSlot >= 36) {
            return -1;
        }
        return KeyBindings.isPlayerInventorySlot(slot, containerSlot) ? containerSlot : -1;
    }

    private static boolean isPlayerInventorySlot(Slot slot, int playerSlot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || playerSlot < 0 || playerSlot >= 36) {
            return false;
        }
        if (slot.container == mc.player.getInventory()) {
            return true;
        }
        ItemStack playerStack = mc.player.getInventory().getItem(playerSlot);
        return playerStack == slot.getItem();
    }

    private static int playerInventorySlotFromCreativePacket(int creativeSlot) {
        if (creativeSlot >= 36 && creativeSlot < 45) {
            return creativeSlot - 36;
        }
        if (creativeSlot >= 9 && creativeSlot < 36) {
            return creativeSlot;
        }
        return -1;
    }

    private static int creativePacketSlotFromPlayerInventory(int playerSlot) {
        if (playerSlot >= 0 && playerSlot < 9) {
            return 36 + playerSlot;
        }
        if (playerSlot >= 9 && playerSlot < 36) {
            return playerSlot;
        }
        return -1;
    }
    private static int findSlotIndex(Object screen, Object slot) {
        int directIndex = KeyBindings.findSlotIndexInFields(screen, slot);
        if (directIndex >= 0) {
            return directIndex;
        }
        for (Class<?> type = screen == null ? null : screen.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    int nestedIndex = KeyBindings.findSlotIndexInFields(field.get(screen), slot);
                    if (nestedIndex >= 0) {
                        return nestedIndex;
                    }
                } catch (Throwable ignored) {
                    // Try the next field.
                }
            }
        }
        return -1;
    }

    private static int findSlotIndexInFields(Object owner, Object slot) {
        for (Class<?> type = owner == null ? null : owner.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(owner);
                    if (value instanceof List<?> slots) {
                        int index = slots.indexOf(slot);
                        if (index >= 0) {
                            return index;
                        }
                    }
                } catch (Throwable ignored) {
                    // Try the next field.
                }
            }
        }
        return -1;
    }

    private static void openSmartEntityEditor(Minecraft mc, Entity looked, ItemStack spawnEgg, int slot, net.minecraft.client.gui.screens.Screen parent) {
        boolean smart = AnkiConfig.isSmartEntityEditorKey();

        if (looked != null) {
            if (smart && isVillagerEntity(looked)) {
                mc.setScreenAndShow(VillagerTradeEditorScreen.forEntity(looked, parent));
            } else {
                mc.setScreenAndShow(EntityEditorScreen.forEntity(looked, parent));
            }
            return;
        }

        if (!spawnEgg.isEmpty()) {
            if (smart && SpawnEggEditorHelper.isVillagerSpawnEgg(spawnEgg)) {
                mc.setScreenAndShow(VillagerTradeEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
            } else {
                mc.setScreenAndShow(EntityEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
            }
        }
    }

    private static Entity getLookedEntity(Minecraft mc) {
        if (mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult ehr) {
            return ehr.getEntity();
        }
        return null;
    }

    private static boolean isVillagerEntity(Entity entity) {
        if (entity == null) return false;
        String type = entity.getType().toString().toLowerCase(Locale.ROOT);
        return type.contains("villager") || type.contains("wandering_trader");
    }

    private static ItemStack getHeldOrOffhand(Minecraft mc) {
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) held = mc.player.getOffhandItem();
        return held;
    }

    private static int getHeldInventorySlot(Minecraft mc) {
        if (mc.player == null) {
            return -1;
        }
        ItemStack mainHand = mc.player.getMainHandItem();
        if (mainHand.isEmpty()) {
            return -1;
        }
        net.minecraft.world.entity.player.Inventory inventory = mc.player.getInventory();
        for (int i = 0; i < 36; ++i) {
            if (inventory.getItem(i) == mainHand) {
                return i;
            }
        }
        int matchedSlot = -1;
        for (int i = 0; i < 36; ++i) {
            ItemStack candidate = inventory.getItem(i);
            if (candidate.getCount() == mainHand.getCount() && ItemStack.isSameItemSameComponents(candidate, mainHand)) {
                if (matchedSlot >= 0) {
                    matchedSlot = -1;
                    break;
                }
                matchedSlot = i;
            }
        }
        if (matchedSlot >= 0) {
            return matchedSlot;
        }
        int selected = com.ankinbt.compat.VersionCompat.get().getSelectedSlot(inventory);
        if (selected >= 0 && selected < 36) {
            ItemStack selectedStack = inventory.getItem(selected);
            if (selectedStack.getCount() == mainHand.getCount() && ItemStack.isSameItemSameComponents(selectedStack, mainHand)) {
                return selected;
            }
        }
        return -1;
    }

    private static void openItemEditor(ItemStack stack, int slot) {
        Minecraft mc = Minecraft.getInstance();
        if ("advanced".equalsIgnoreCase(AnkiConfig.getPreferredItemEditor())) {
            mc.setScreenAndShow(new NbtEditorScreen(stack, slot));
        } else {
            mc.setScreenAndShow(new SimpleEditorScreen(stack, slot));
        }
    }

    private static int normalizeEntityEditorKeyCode(int keyCode) {
        if (keyCode == InputConstants.KEY_M || keyCode == InputConstants.KEY_V) {
            return InputConstants.KEY_COMMA;
        }
        return keyCode;
    }

    public static boolean syncConfigFromKeyMappings() {
        boolean changed = false;
        int itemCode = keyCode(openItemEditorKey, AnkiConfig.getOpenItemEditorKeyCode());
        if (itemCode != AnkiConfig.getOpenItemEditorKeyCode()) {
            AnkiConfig.setOpenItemEditorKeyCode(itemCode);
            changed = true;
        }

        int entityCode = keyCode(openEntityEditorKey, AnkiConfig.getOpenEntityEditorKeyCode());
        if (entityCode != AnkiConfig.getOpenEntityEditorKeyCode()) {
            AnkiConfig.setOpenEntityEditorKeyCode(entityCode);
            changed = true;
        }
        if (entityCode != AnkiConfig.getOpenVillagerEditorKeyCode()) {
            AnkiConfig.setOpenVillagerEditorKeyCode(entityCode);
            changed = true;
        }

        int menuCode = keyCode(openConfigMenuKey, AnkiConfig.getOpenConfigMenuKeyCode());
        if (menuCode != AnkiConfig.getOpenConfigMenuKeyCode()) {
            AnkiConfig.setOpenConfigMenuKeyCode(menuCode);
            changed = true;
        }
        return changed;
    }

    public static void resetToDefaults() {
        setKey(openItemEditorKey, InputConstants.KEY_N);
        setKey(openEntityEditorKey, InputConstants.KEY_COMMA);
        setKey(openConfigMenuKey, InputConstants.KEY_O);
        KeyMapping.resetMapping();

        AnkiConfig.setOpenItemEditorKeyCode(InputConstants.KEY_N);
        AnkiConfig.setOpenEntityEditorKeyCode(InputConstants.KEY_COMMA);
        AnkiConfig.setOpenVillagerEditorKeyCode(InputConstants.KEY_COMMA);
        AnkiConfig.setOpenConfigMenuKeyCode(InputConstants.KEY_O);

        Minecraft client = Minecraft.getInstance();
        if (client != null && client.options != null) client.options.save();
    }

    private static void setKey(KeyMapping mapping, int keyCode) {
        if (mapping != null) mapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
    }

    private static int keyCode(KeyMapping mapping, int fallback) {
        if (mapping == null) return fallback;
        try {
            try {
                Object key = mapping.getClass().getMethod("getKey").invoke(mapping);
                if (key != null) {
                    Object value = key.getClass().getMethod("getValue").invoke(key);
                    if (value instanceof Number n) return n.intValue();
                }
            } catch (Throwable ignored) {}
            java.lang.reflect.Field keyField = KeyMapping.class.getDeclaredField("key");
            keyField.setAccessible(true);
            Object key = keyField.get(mapping);
            if (key != null) {
                Object value = key.getClass().getMethod("getValue").invoke(key);
                if (value instanceof Number n) return n.intValue();
            }
        } catch (Throwable ignored) {
        }
        return fallback;
    }
}
