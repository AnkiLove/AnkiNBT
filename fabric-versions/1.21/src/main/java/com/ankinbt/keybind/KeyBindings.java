/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
 *  net.minecraft.class_1297
 *  net.minecraft.class_1735
 *  net.minecraft.class_1799
 *  net.minecraft.class_239
 *  net.minecraft.class_2561
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_3966
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 */
package com.ankinbt.keybind;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.gui.AnkiConfigScreen;
import com.ankinbt.gui.EntityEditorScreen;
import com.ankinbt.gui.NbtEditorScreen;
import com.ankinbt.gui.SimpleEditorScreen;
import com.ankinbt.gui.VillagerTradeEditorScreen;
import java.lang.reflect.Field;
import java.util.Locale;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.class_1297;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_3966;
import net.minecraft.class_437;
import net.minecraft.class_465;

public class KeyBindings {
    private static final String CATEGORY = "key.categories.ankinbt";
    private static class_304 openItemEditorKey;
    private static class_304 openEntityEditorKey;
    private static class_304 openConfigMenuKey;
    private static Field hoveredSlotField;

    public static void register() {
        int entityKeyCode = KeyBindings.normalizeEntityEditorKeyCode(AnkiConfig.getOpenEntityEditorKeyCode());
        if (entityKeyCode != AnkiConfig.getOpenEntityEditorKeyCode()) {
            AnkiConfig.setOpenEntityEditorKeyCode(entityKeyCode);
        }
        if (entityKeyCode != AnkiConfig.getOpenVillagerEditorKeyCode()) {
            AnkiConfig.setOpenVillagerEditorKeyCode(entityKeyCode);
        }
        openItemEditorKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.ankinbt.open_editor", class_3675.class_307.field_1668, AnkiConfig.getOpenItemEditorKeyCode(), CATEGORY));
        openEntityEditorKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.ankinbt.open_entity_editor", class_3675.class_307.field_1668, entityKeyCode, CATEGORY));
        openConfigMenuKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.ankinbt.open_config_menu", class_3675.class_307.field_1668, AnkiConfig.getOpenConfigMenuKeyCode(), CATEGORY));
        try {
            hoveredSlotField = class_465.class.getDeclaredField("hoveredSlot");
            hoveredSlotField.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            try {
                for (Field f : class_465.class.getDeclaredFields()) {
                    if (f.getType() != class_1735.class) continue;
                    hoveredSlotField = f;
                    hoveredSlotField.setAccessible(true);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigMenuKey.method_1436()) {
                client.method_1507((class_437)new AnkiConfigScreen(client.field_1755));
            }
            if (client.field_1724 == null || client.field_1755 != null) {
                return;
            }
            while (openItemEditorKey.method_1436()) {
                class_1799 held = KeyBindings.getHeldOrOffhand(client);
                if (held.method_7960()) {
                    client.field_1724.method_7353((class_2561)class_2561.method_43471((String)"ankinbt.message.no_item"), true);
                    continue;
                }
                KeyBindings.openItemEditor(held, -1);
            }
            while (openEntityEditorKey.method_1436()) {
                class_1297 looked = KeyBindings.getLookedEntity(client);
                if (looked != null) {
                    KeyBindings.openSmartEntityEditor(client, looked, class_1799.field_8037, -1, client.field_1755);
                    continue;
                }
                class_1799 held = KeyBindings.getHeldOrOffhand(client);
                if (SpawnEggEditorHelper.isSpawnEgg(held)) {
                    KeyBindings.openSmartEntityEditor(client, null, held, -1, client.field_1755);
                    continue;
                }
                client.field_1724.method_7353((class_2561)class_2561.method_43471((String)"ankinbt.entity.target_hint"), true);
            }
        });
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof class_465) {
                class_465 containerScreen = (class_465)screen;
                ScreenKeyboardEvents.beforeKeyPress((class_437)screen).register((scr, key, scancode, modifiers) -> {
                    if (openConfigMenuKey.method_1417(key, scancode)) {
                        client.method_1507((class_437)new AnkiConfigScreen(screen));
                        return;
                    }
                    class_1735 hoveredSlot = KeyBindings.getHoveredSlot(containerScreen);
                    if (hoveredSlot == null || !hoveredSlot.method_7681()) {
                        return;
                    }
                    class_1799 stack = hoveredSlot.method_7677();
                    int slotIndex = hoveredSlot.method_34266();
                    if (openItemEditorKey.method_1417(key, scancode)) {
                        KeyBindings.openItemEditor(stack, slotIndex);
                        return;
                    }
                    if (openEntityEditorKey.method_1417(key, scancode) && SpawnEggEditorHelper.isSpawnEgg(stack)) {
                        KeyBindings.openSmartEntityEditor(client, null, stack, slotIndex, screen);
                    }
                });
            }
        });
    }

    private static void openSmartEntityEditor(class_310 client, class_1297 looked, class_1799 spawnEgg, int slot, class_437 parent) {
        boolean smart = AnkiConfig.isSmartEntityEditorKey();
        if (looked != null) {
            if (smart && KeyBindings.isVillagerEntity(looked)) {
                client.method_1507((class_437)VillagerTradeEditorScreen.forEntity(looked, parent));
            } else {
                client.method_1507((class_437)EntityEditorScreen.forEntity(looked, parent));
            }
            return;
        }
        if (!spawnEgg.method_7960()) {
            if (smart && SpawnEggEditorHelper.isVillagerSpawnEgg(spawnEgg)) {
                client.method_1507((class_437)VillagerTradeEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
            } else {
                client.method_1507((class_437)EntityEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
            }
        }
    }

    private static class_1735 getHoveredSlot(class_465<?> screen) {
        if (hoveredSlotField != null) {
            try {
                return (class_1735)hoveredSlotField.get(screen);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return null;
    }

    private static class_1297 getLookedEntity(class_310 client) {
        class_239 class_2392 = client.field_1765;
        if (class_2392 instanceof class_3966) {
            class_3966 ehr = (class_3966)class_2392;
            return ehr.method_17782();
        }
        return null;
    }

    private static boolean isVillagerEntity(class_1297 entity) {
        if (entity == null) {
            return false;
        }
        String type = entity.method_5864().toString().toLowerCase(Locale.ROOT);
        return type.contains("villager") || type.contains("wandering_trader");
    }

    private static class_1799 getHeldOrOffhand(class_310 client) {
        class_1799 held = client.field_1724.method_6047();
        if (held.method_7960()) {
            held = client.field_1724.method_6079();
        }
        return held;
    }

    private static void openItemEditor(class_1799 stack, int slot) {
        class_310 mc = class_310.method_1551();
        if ("advanced".equalsIgnoreCase(AnkiConfig.getPreferredItemEditor())) {
            mc.method_1507((class_437)new NbtEditorScreen(stack, slot));
        } else {
            mc.method_1507((class_437)new SimpleEditorScreen(stack, slot));
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

    private static int keyCode(class_304 mapping, int fallback) {
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
            Field keyField = class_304.class.getDeclaredField("key");
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
}

