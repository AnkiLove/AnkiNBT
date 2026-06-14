package com.ankinbt.keybind;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.gui.AnkiConfigScreen;
import com.ankinbt.gui.EntityEditorScreen;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.SimpleEditorScreen;
import com.ankinbt.gui.VillagerTradeEditorScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

public class KeyBindings {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.parse("ankinbt:keybinds"));
    private static KeyMapping openItemEditorKey;
    private static KeyMapping openEntityEditorKey;
    private static KeyMapping openConfigMenuKey;
    private static Field hoveredSlotField;

    public static void register() {
        int entityKeyCode = normalizeEntityEditorKeyCode(AnkiConfig.getOpenEntityEditorKeyCode());
        if (entityKeyCode != AnkiConfig.getOpenEntityEditorKeyCode()) {
            AnkiConfig.setOpenEntityEditorKeyCode(entityKeyCode);
        }
        if (entityKeyCode != AnkiConfig.getOpenVillagerEditorKeyCode()) {
            AnkiConfig.setOpenVillagerEditorKeyCode(entityKeyCode);
        }
        openItemEditorKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.ankinbt.open_editor",
                InputConstants.Type.KEYSYM,
                AnkiConfig.getOpenItemEditorKeyCode(),
                CATEGORY
        ));
        openEntityEditorKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.ankinbt.open_entity_editor",
                InputConstants.Type.KEYSYM,
                entityKeyCode,
                CATEGORY
        ));
        openConfigMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.ankinbt.open_config_menu",
                InputConstants.Type.KEYSYM,
                AnkiConfig.getOpenConfigMenuKeyCode(),
                CATEGORY
        ));

        try {
            hoveredSlotField = AbstractContainerScreen.class.getDeclaredField("hoveredSlot");
            hoveredSlotField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            try {
                for (Field f : AbstractContainerScreen.class.getDeclaredFields()) {
                    if (f.getType() == Slot.class) {
                        hoveredSlotField = f;
                        hoveredSlotField.setAccessible(true);
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigMenuKey.consumeClick()) {
                client.setScreen(new AnkiConfigScreen(client.screen));
            }

            if (client.player == null || client.screen != null) return;

            while (openItemEditorKey.consumeClick()) {
                ItemStack held = getHeldOrOffhand(client);
                if (held.isEmpty()) {
                    client.player.sendOverlayMessage(
                            net.minecraft.network.chat.Component.translatable("ankinbt.message.no_item"));
                    continue;
                }
                openItemEditor(held, getHeldInventorySlot(client));
            }

            while (openEntityEditorKey.consumeClick()) {
                Entity looked = getLookedEntity(client);
                if (looked != null) {
                    openSmartEntityEditor(client, looked, ItemStack.EMPTY, -1, client.screen);
                    continue;
                }

                ItemStack held = getHeldOrOffhand(client);
                if (SpawnEggEditorHelper.isSpawnEgg(held)) {
                    openSmartEntityEditor(client, null, held, getHeldInventorySlot(client), client.screen);
                    continue;
                }

                client.player.sendOverlayMessage(
                        net.minecraft.network.chat.Component.translatable("ankinbt.entity.target_hint"));
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                ScreenKeyboardEvents.beforeKeyPress(screen).register((scr, keyEvent) -> {
                    if (openConfigMenuKey.matches(keyEvent)) {
                        client.setScreen(new AnkiConfigScreen(screen));
                        return;
                    }

                    Slot hoveredSlot = getHoveredSlot(containerScreen);
                    if (hoveredSlot == null || !hoveredSlot.hasItem()) return;
                    ItemStack stack = hoveredSlot.getItem();
                    int slotIndex = KeyBindings.getMenuSlotIndex(containerScreen, hoveredSlot);

                    if (openItemEditorKey.matches(keyEvent)) {
                        openItemEditor(stack, slotIndex);
                        return;
                    }

                    if (openEntityEditorKey.matches(keyEvent) && SpawnEggEditorHelper.isSpawnEgg(stack)) {
                        openSmartEntityEditor(client, null, stack, slotIndex, screen);
                    }
                });
            }
        });
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

    private static void openSmartEntityEditor(Minecraft client, Entity looked, ItemStack spawnEgg, int slot, net.minecraft.client.gui.screens.Screen parent) {
        boolean smart = AnkiConfig.isSmartEntityEditorKey();

        if (looked != null) {
            if (smart && isVillagerEntity(looked)) {
                client.setScreen(VillagerTradeEditorScreen.forEntity(looked, parent));
            } else {
                client.setScreen(EntityEditorScreen.forEntity(looked, parent));
            }
            return;
        }

        if (!spawnEgg.isEmpty()) {
            if (smart && SpawnEggEditorHelper.isVillagerSpawnEgg(spawnEgg)) {
                client.setScreen(VillagerTradeEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
            } else {
                client.setScreen(EntityEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
            }
        }
    }

    private static Slot getHoveredSlot(AbstractContainerScreen<?> screen) {
        if (hoveredSlotField != null) {
            try {
                return (Slot) hoveredSlotField.get(screen);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static Entity getLookedEntity(Minecraft client) {
        if (client.hitResult instanceof net.minecraft.world.phys.EntityHitResult ehr) {
            return ehr.getEntity();
        }
        return null;
    }

    private static boolean isVillagerEntity(Entity entity) {
        if (entity == null) return false;
        String type = entity.getType().toString().toLowerCase(Locale.ROOT);
        return type.contains("villager") || type.contains("wandering_trader");
    }

    private static ItemStack getHeldOrOffhand(Minecraft client) {
        ItemStack held = client.player.getMainHandItem();
        if (held.isEmpty()) held = client.player.getOffhandItem();
        return held;
    }

    private static int getHeldInventorySlot(Minecraft client) {
        if (client.player == null) {
            return -1;
        }
        ItemStack mainHand = client.player.getMainHandItem();
        if (mainHand.isEmpty()) {
            return -1;
        }
        net.minecraft.world.entity.player.Inventory inventory = client.player.getInventory();
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
            mc.setScreen(new NbtEditorScreen(stack, slot));
        } else {
            mc.setScreen(new SimpleEditorScreen(stack, slot));
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

