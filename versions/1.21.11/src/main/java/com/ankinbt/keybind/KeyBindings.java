package com.ankinbt.keybind;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.gui.AnkiConfigScreen;
import com.ankinbt.gui.EntityEditorScreen;
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

import java.util.Locale;

public class KeyBindings {

    private static KeyMapping openItemEditorKey;
    private static KeyMapping openEntityEditorKey;
    private static KeyMapping openConfigMenuKey;

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(KeyBindings::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onClientTick);
        NeoForge.EVENT_BUS.addListener(KeyBindings::onScreenKeyPress);
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

        if (openConfigMenuKey != null && openConfigMenuKey.consumeClick()) {
            mc.setScreen(new AnkiConfigScreen(mc.screen));
            return;
        }

        if (mc.screen != null) return;

        if (openItemEditorKey != null && openItemEditorKey.consumeClick()) {
            ItemStack held = getHeldOrOffhand(mc);
            if (held.isEmpty()) {
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.translatable("ankinbt.message.no_item"), true);
                return;
            }
            openItemEditor(held, -1);
            return;
        }

        if (openEntityEditorKey != null && openEntityEditorKey.consumeClick()) {
            Entity looked = getLookedEntity(mc);
            if (looked != null) {
                openSmartEntityEditor(mc, looked, ItemStack.EMPTY, -1, mc.screen);
                return;
            }

            ItemStack held = getHeldOrOffhand(mc);
            if (SpawnEggEditorHelper.isSpawnEgg(held)) {
                openSmartEntityEditor(mc, null, held, -1, mc.screen);
                return;
            }

            mc.player.displayClientMessage(net.minecraft.network.chat.Component.translatable("ankinbt.entity.target_hint"), true);
        }
    }

    private static void onScreenKeyPress(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        if (openConfigMenuKey != null && openConfigMenuKey.matches(event.getKeyEvent())) {
            event.setCanceled(true);
            Minecraft.getInstance().setScreen(new AnkiConfigScreen(event.getScreen()));
            return;
        }

        Slot hoveredSlot = containerScreen.getSlotUnderMouse();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();
        int slotIndex = hoveredSlot.getContainerSlot();

        if (openItemEditorKey != null && openItemEditorKey.matches(event.getKeyEvent())) {
            event.setCanceled(true);
            openItemEditor(stack, slotIndex);
            return;
        }

        if (openEntityEditorKey != null && openEntityEditorKey.matches(event.getKeyEvent())) {
            if (!SpawnEggEditorHelper.isSpawnEgg(stack)) return;
            event.setCanceled(true);
            openSmartEntityEditor(Minecraft.getInstance(), null, stack, slotIndex, event.getScreen());
        }
    }

    private static void openSmartEntityEditor(Minecraft mc, Entity looked, ItemStack spawnEgg, int slot, net.minecraft.client.gui.screens.Screen parent) {
        boolean smart = AnkiConfig.isSmartEntityEditorKey();

        if (looked != null) {
            if (smart && isVillagerEntity(looked)) {
                mc.setScreen(VillagerTradeEditorScreen.forEntity(looked, parent));
            } else {
                mc.setScreen(EntityEditorScreen.forEntity(looked, parent));
            }
            return;
        }

        if (!spawnEgg.isEmpty()) {
            if (smart && SpawnEggEditorHelper.isVillagerSpawnEgg(spawnEgg)) {
                mc.setScreen(VillagerTradeEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
            } else {
                mc.setScreen(EntityEditorScreen.forSpawnEgg(spawnEgg, slot, parent));
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
