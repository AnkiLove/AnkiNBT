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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.Locale;

public class KeyBindings {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(ResourceLocation.parse("ankinbt:keybinds"));
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
        openItemEditorKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.ankinbt.open_editor",
                InputConstants.Type.KEYSYM,
                AnkiConfig.getOpenItemEditorKeyCode(),
                CATEGORY
        ));
        openEntityEditorKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.ankinbt.open_entity_editor",
                InputConstants.Type.KEYSYM,
                entityKeyCode,
                CATEGORY
        ));
        openConfigMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
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
                    client.player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("ankinbt.message.no_item"), true);
                    continue;
                }
                openItemEditor(held, -1);
            }

            while (openEntityEditorKey.consumeClick()) {
                Entity looked = getLookedEntity(client);
                if (looked != null) {
                    openSmartEntityEditor(client, looked, ItemStack.EMPTY, -1, client.screen);
                    continue;
                }

                ItemStack held = getHeldOrOffhand(client);
                if (SpawnEggEditorHelper.isSpawnEgg(held)) {
                    openSmartEntityEditor(client, null, held, -1, client.screen);
                    continue;
                }

                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("ankinbt.entity.target_hint"), true);
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
                    int slotIndex = hoveredSlot.getContainerSlot();

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
