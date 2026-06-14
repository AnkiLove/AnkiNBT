/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 *  net.neoforged.neoforge.client.event.ScreenEvent$KeyPressed$Pre
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.ankinbt.keybind;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.gui.AnkiConfigScreen;
import com.ankinbt.gui.EntityEditorScreen;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.SimpleEditorScreen;
import com.ankinbt.gui.VillagerTradeEditorScreen;
import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class KeyBindings {
    private static final String CATEGORY = "key.categories.ankinbt";
    private static KeyMapping openItemEditorKey;
    private static KeyMapping openEntityEditorKey;
    private static KeyMapping openConfigMenuKey;

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(KeyBindings::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onClientTick);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenKeyPress);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeyBindings.refreshMappingsFromConfig();
        event.register(openItemEditorKey);
        event.register(openEntityEditorKey);
        event.register(openConfigMenuKey);
    }

    private static void refreshMappingsFromConfig() {
        int entityKeyCode = KeyBindings.normalizeEntityEditorKeyCode(AnkiConfig.getOpenEntityEditorKeyCode());
        if (entityKeyCode != AnkiConfig.getOpenEntityEditorKeyCode()) {
            AnkiConfig.setOpenEntityEditorKeyCode(entityKeyCode);
        }
        if (entityKeyCode != AnkiConfig.getOpenVillagerEditorKeyCode()) {
            AnkiConfig.setOpenVillagerEditorKeyCode(entityKeyCode);
        }
        openItemEditorKey = new KeyMapping("key.ankinbt.open_editor", AnkiConfig.getOpenItemEditorKeyCode(), CATEGORY);
        openEntityEditorKey = new KeyMapping("key.ankinbt.open_entity_editor", entityKeyCode, CATEGORY);
        openConfigMenuKey = new KeyMapping("key.ankinbt.open_config_menu", AnkiConfig.getOpenConfigMenuKeyCode(), CATEGORY);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (openConfigMenuKey != null && openConfigMenuKey.consumeClick()) {
            mc.setScreen((Screen)new AnkiConfigScreen(mc.screen));
            return;
        }
        if (mc.screen != null) {
            return;
        }
        if (openItemEditorKey != null && openItemEditorKey.consumeClick()) {
            ItemStack held = KeyBindings.getHeldOrOffhand(mc);
            if (held.isEmpty()) {
                mc.player.displayClientMessage((Component)Component.translatable((String)"ankinbt.message.no_item"), true);
                return;
            }
            KeyBindings.openItemEditor(held, KeyBindings.getHeldInventorySlot(mc));
            return;
        }
        if (openEntityEditorKey != null && openEntityEditorKey.consumeClick()) {
            Entity looked = KeyBindings.getLookedEntity(mc);
            if (looked != null) {
                KeyBindings.openSmartEntityEditor(mc, looked, ItemStack.EMPTY, -1, mc.screen);
                return;
            }
            ItemStack held = KeyBindings.getHeldOrOffhand(mc);
            if (SpawnEggEditorHelper.isSpawnEgg(held)) {
                KeyBindings.openSmartEntityEditor(mc, null, held, KeyBindings.getHeldInventorySlot(mc), mc.screen);
                return;
            }
            mc.player.displayClientMessage((Component)Component.translatable((String)"ankinbt.entity.target_hint"), true);
        }
    }

    private static void onScreenKeyPress(ScreenEvent.KeyPressed.Pre event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof AbstractContainerScreen)) {
            return;
        }
        AbstractContainerScreen containerScreen = (AbstractContainerScreen)screen;
        if (openConfigMenuKey != null && KeyBindings.matchesEventKey(openConfigMenuKey, event)) {
            event.setCanceled(true);
            Minecraft.getInstance().setScreen((Screen)new AnkiConfigScreen(event.getScreen()));
            return;
        }
        Slot hoveredSlot = containerScreen.getSlotUnderMouse();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            return;
        }
        ItemStack stack = hoveredSlot.getItem();
        int slotIndex = KeyBindings.getMenuSlotIndex(containerScreen, hoveredSlot);
        if (openItemEditorKey != null && KeyBindings.matchesEventKey(openItemEditorKey, event)) {
            event.setCanceled(true);
            KeyBindings.openItemEditor(stack, slotIndex);
            return;
        }
        if (openEntityEditorKey != null && KeyBindings.matchesEventKey(openEntityEditorKey, event)) {
            if (!SpawnEggEditorHelper.isSpawnEgg(stack)) {
                return;
            }
            event.setCanceled(true);
            KeyBindings.openSmartEntityEditor(Minecraft.getInstance(), null, stack, slotIndex, event.getScreen());
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

    private static void openSmartEntityEditor(Minecraft mc, Entity looked, ItemStack spawnEgg, int slot, Screen parent) {
        boolean smart = AnkiConfig.isSmartEntityEditorKey();
        if (looked != null) {
            if (smart && KeyBindings.isVillagerEntity(looked)) {
                mc.setScreen((Screen)VillagerTradeEditorScreen.forEntity(looked, parent));
            } else {
                mc.setScreen((Screen)EntityEditorScreen.forEntity(looked, parent));
            }
            return;
        }
        if (!spawnEgg.isEmpty()) {
            if (smart && SpawnEggEditorHelper.isVillagerSpawnEgg(spawnEgg)) {
                mc.setScreen((Screen)VillagerTradeEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
            } else {
                mc.setScreen((Screen)EntityEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
            }
        }
    }

    private static Entity getLookedEntity(Minecraft mc) {
        HitResult hitResult = mc.hitResult;
        if (hitResult instanceof EntityHitResult) {
            EntityHitResult ehr = (EntityHitResult)hitResult;
            return ehr.getEntity();
        }
        return null;
    }

    private static boolean isVillagerEntity(Entity entity) {
        if (entity == null) {
            return false;
        }
        String type = entity.getType().toString().toLowerCase(Locale.ROOT);
        return type.contains("villager") || type.contains("wandering_trader");
    }

    private static ItemStack getHeldOrOffhand(Minecraft mc) {
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) {
            held = mc.player.getOffhandItem();
        }
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
            mc.setScreen((Screen)new NbtEditorScreen(stack, slot));
        } else {
            mc.setScreen((Screen)new SimpleEditorScreen(stack, slot));
        }
    }

    private static int normalizeEntityEditorKeyCode(int keyCode) {
        if (keyCode == 77 || keyCode == 86) {
            return 44;
        }
        return keyCode;
    }

    public static boolean syncConfigFromKeyMappings() {
        int menuCode;
        int entityCode;
        boolean changed = false;
        int itemCode = KeyBindings.keyCode(openItemEditorKey, AnkiConfig.getOpenItemEditorKeyCode());
        if (itemCode != AnkiConfig.getOpenItemEditorKeyCode()) {
            AnkiConfig.setOpenItemEditorKeyCode(itemCode);
            changed = true;
        }
        if ((entityCode = KeyBindings.keyCode(openEntityEditorKey, AnkiConfig.getOpenEntityEditorKeyCode())) != AnkiConfig.getOpenEntityEditorKeyCode()) {
            AnkiConfig.setOpenEntityEditorKeyCode(entityCode);
            changed = true;
        }
        if (entityCode != AnkiConfig.getOpenVillagerEditorKeyCode()) {
            AnkiConfig.setOpenVillagerEditorKeyCode(entityCode);
            changed = true;
        }
        if ((menuCode = KeyBindings.keyCode(openConfigMenuKey, AnkiConfig.getOpenConfigMenuKeyCode())) != AnkiConfig.getOpenConfigMenuKeyCode()) {
            AnkiConfig.setOpenConfigMenuKeyCode(menuCode);
            changed = true;
        }
        return changed;
    }

    private static int keyCode(KeyMapping mapping, int fallback) {
        if (mapping == null) {
            return fallback;
        }
        try {
            Object value;
            try {
                Object value2;
                Object key = mapping.getClass().getMethod("getKey", new Class[0]).invoke((Object)mapping, new Object[0]);
                if (key != null && (value2 = key.getClass().getMethod("getValue", new Class[0]).invoke(key, new Object[0])) instanceof Number) {
                    Number n = (Number)value2;
                    return n.intValue();
                }
            }
            catch (Throwable key) {
                // empty catch block
            }
            Field keyField = KeyMapping.class.getDeclaredField("key");
            keyField.setAccessible(true);
            Object key = keyField.get(mapping);
            if (key != null && (value = key.getClass().getMethod("getValue", new Class[0]).invoke(key, new Object[0])) instanceof Number) {
                Number n = (Number)value;
                return n.intValue();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return fallback;
    }

    private static boolean matchesEventKey(KeyMapping mapping, Object event) {
        Integer code = KeyBindings.eventKeyCode(event);
        return code != null && KeyBindings.keyCode(mapping, Integer.MIN_VALUE) == code;
    }

    private static Integer eventKeyCode(Object event) {
        if (event == null) {
            return null;
        }
        try {
            Object keyEvent = event.getClass().getMethod("getKeyEvent", new Class[0]).invoke(event, new Object[0]);
            if (keyEvent != null) {
                Object value = keyEvent.getClass().getMethod("getKey", new Class[0]).invoke(keyEvent, new Object[0]);
                if (value instanceof Number) {
                    return ((Number)value).intValue();
                }
                value = keyEvent.getClass().getMethod("getValue", new Class[0]).invoke(keyEvent, new Object[0]);
                if (value instanceof Number) {
                    return ((Number)value).intValue();
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            Object value = event.getClass().getMethod("getKeyCode", new Class[0]).invoke(event, new Object[0]);
            if (value instanceof Number) {
                return ((Number)value).intValue();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            Object value = event.getClass().getMethod("getKey", new Class[0]).invoke(event, new Object[0]);
            if (value instanceof Number) {
                return ((Number)value).intValue();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }
}
