package com.ankinbt.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.network.chat.Component;

import java.util.*;

/**
 * VersionCompat for Fabric MC 1.21.1 (Mojang mappings).
 * Identical API surface to NeoForge 1.21.1 VersionCompat.
 */
public class VersionCompat {

    private static VersionCompat INSTANCE;
    private static final java.lang.reflect.Method ITEMSTACK_GET_COMPONENT_METHOD =
            findItemStackComponentMethod("get", "method_57824", "method_58694");
    private static final java.lang.reflect.Method ITEMSTACK_HAS_COMPONENT_METHOD =
            findItemStackComponentMethod("has", "method_57826");
    private static final java.lang.reflect.Method ITEMSTACK_REMOVE_COMPONENT_METHOD =
            findItemStackComponentMethod("remove", "method_57381");
    private static final java.lang.reflect.Method ITEMSTACK_SET_COMPONENT_METHOD =
            findItemStackSetComponentMethod("set", "method_57379");

    public static VersionCompat get() {
        if (INSTANCE == null) INSTANCE = new VersionCompat();
        return INSTANCE;
    }


    // --- Platform paths ---
    public java.nio.file.Path getConfigDir() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
    }
    public java.nio.file.Path getGameDir() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
    }
        public String getKeyDisplayName(int keyCode) {
        if (keyCode == com.mojang.blaze3d.platform.InputConstants.KEY_COMMA) return ",";
        if (keyCode >= com.mojang.blaze3d.platform.InputConstants.KEY_A && keyCode <= com.mojang.blaze3d.platform.InputConstants.KEY_Z) {
            return Character.toString((char) ('A' + (keyCode - com.mojang.blaze3d.platform.InputConstants.KEY_A)));
        }
        if (keyCode >= com.mojang.blaze3d.platform.InputConstants.KEY_0 && keyCode <= com.mojang.blaze3d.platform.InputConstants.KEY_9) {
            return Character.toString((char) ('0' + (keyCode - com.mojang.blaze3d.platform.InputConstants.KEY_0)));
        }
        try {
            String name = com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName().getString();
            if (name != null && !name.isBlank() && !name.startsWith("#")) return name;
        } catch (Throwable ignored) {}
        return "KEY(" + keyCode + ")";
    }
    // --- Registry ---
    public List<String> getAllEnchantIds() {
        List<String> ids = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Registry<Enchantment> reg = mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            reg.holders().forEach(h -> ids.add(h.key().location().toString()));
        }
        return ids;
    }
    public List<String> getAllAttributeIds() {
        List<String> ids = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Registry<Attribute> reg = mc.level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
            reg.holders().forEach(h -> ids.add(h.key().location().toString()));
        }
        return ids;
    }
    public Optional<Holder.Reference<Enchantment>> getEnchantHolder(String id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Optional.empty();
        Registry<Enchantment> reg = mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) return Optional.empty();
        return reg.getHolder(loc);
    }
    public Optional<Holder.Reference<Attribute>> getAttributeHolder(String id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Optional.empty();
        Registry<Attribute> reg = mc.level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) return Optional.empty();
        return reg.getHolder(loc);
    }

    // --- Fire resistant ---
    public boolean isFireResistant(ItemStack stack) { return hasComponent(stack, DataComponents.FIRE_RESISTANT); }
    public void setFireResistant(ItemStack stack, boolean value) {
        if (value) setComponent(stack, DataComponents.FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE);
        else removeComponent(stack, DataComponents.FIRE_RESISTANT);
    }

    // --- Custom model data ---
    public int getCustomModelData(ItemStack stack) {
        var cmd = getComponent(stack, DataComponents.CUSTOM_MODEL_DATA);
        return cmd != null ? cmd.value() : 0;
    }
    public void setCustomModelData(ItemStack stack, int value) {
        setComponent(stack, DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(value));
    }

    // --- Food ---
    public boolean hasFood(ItemStack stack) { return getComponent(stack, DataComponents.FOOD) != null; }
    public int getFoodNutrition(ItemStack stack) {
        var food = getComponent(stack, DataComponents.FOOD); return food != null ? food.nutrition() : 0;
    }
    public float getFoodSaturation(ItemStack stack) {
        var food = getComponent(stack, DataComponents.FOOD); return food != null ? food.saturation() : 0f;
    }
    public void setFoodNutrition(ItemStack stack, int nutrition) {
        var food = getComponent(stack, DataComponents.FOOD);
        if (food != null) setComponent(stack, DataComponents.FOOD, new net.minecraft.world.food.FoodProperties(
                nutrition, food.saturation(), food.canAlwaysEat(), food.eatSeconds(), food.usingConvertsTo(), food.effects()));
    }
    public void setFoodSaturation(ItemStack stack, float saturation) {
        var food = getComponent(stack, DataComponents.FOOD);
        if (food != null) setComponent(stack, DataComponents.FOOD, new net.minecraft.world.food.FoodProperties(
                food.nutrition(), saturation, food.canAlwaysEat(), food.eatSeconds(), food.usingConvertsTo(), food.effects()));
    }

    // --- NBT accessors ---
    public Set<String> getCompoundKeys(CompoundTag tag) { return tag.getAllKeys(); }
    public String getTagAsString(Tag tag) { return tag.getAsString(); }
    public byte getByteValue(ByteTag tag) { return tag.getAsByte(); }
    public short getShortValue(ShortTag tag) { return tag.getAsShort(); }
    public int getIntValue(IntTag tag) { return tag.getAsInt(); }
    public long getLongValue(LongTag tag) { return tag.getAsLong(); }
    public float getFloatValue(FloatTag tag) { return tag.getAsFloat(); }
    public double getDoubleValue(DoubleTag tag) { return tag.getAsDouble(); }
    public String getStringValue(StringTag tag) { return tag.getAsString(); }
    public String compoundGetString(CompoundTag tag, String key) { return tag.getString(key); }
    public int compoundGetInt(CompoundTag tag, String key) { return tag.getInt(key); }

    // --- Inventory ---
    public int getSelectedSlot(net.minecraft.world.entity.player.Inventory inv) { return inv.selected; }

    // --- Hide tooltip ---
    public boolean isHideTooltip(ItemStack stack) { return hasComponent(stack, DataComponents.HIDE_TOOLTIP); }
    public void setHideTooltip(ItemStack stack, boolean value) {
        if (value) setComponent(stack, DataComponents.HIDE_TOOLTIP, net.minecraft.util.Unit.INSTANCE);
        else removeComponent(stack, DataComponents.HIDE_TOOLTIP);
    }
    public boolean isHideAdditional(ItemStack stack) { return hasComponent(stack, DataComponents.HIDE_ADDITIONAL_TOOLTIP); }
    public void setHideAdditional(ItemStack stack, boolean value) {
        if (value) setComponent(stack, DataComponents.HIDE_ADDITIONAL_TOOLTIP, net.minecraft.util.Unit.INSTANCE);
        else removeComponent(stack, DataComponents.HIDE_ADDITIONAL_TOOLTIP);
    }
    public boolean hasHideTooltipFeature() { return true; }
    public boolean hasHideAdditionalFeature() { return true; }

    // --- Unbreakable ---
    public void setUnbreakable(ItemStack stack, boolean value) {
        if (value) setComponent(stack, DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true));
        else removeComponent(stack, DataComponents.UNBREAKABLE);
    }

    // --- DyedItemColor ---
    public void setDyedColor(ItemStack stack, int rgb) {
        setComponent(stack, DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(rgb, true));
    }

    // --- AttributeModifiers ---
    public ItemAttributeModifiers withEntries(List<ItemAttributeModifiers.Entry> entries, ItemAttributeModifiers old) {
        return new ItemAttributeModifiers(entries, old.showInTooltip());
    }

    // --- Tooltip rendering ---
    public void renderTooltip(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font f, Component tooltip, int mx, int my) {
        java.lang.reflect.Method simple = findGuiGraphicsMethod(
                net.minecraft.client.gui.Font.class,
                net.minecraft.network.chat.Component.class,
                int.class,
                int.class);
        if (simple != null) {
            try {
                simple.invoke(g, f, tooltip, mx, my);
                return;
            } catch (Throwable ignored) {}
        }

        var lines = f.split(tooltip, 200);
        var components = new java.util.ArrayList<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent>();
        for (var line : lines) {
            components.add(new net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip(line));
        }

        java.lang.reflect.Method expanded = findGuiGraphicsMethod(
                net.minecraft.client.gui.Font.class,
                java.util.List.class,
                int.class,
                int.class,
                net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner.class,
                net.minecraft.resources.ResourceLocation.class);
        if (expanded != null) {
            try {
                expanded.invoke(g, f, components, mx, my,
                        net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null);
            } catch (Throwable ignored) {}
        }
    }
    public int drawString(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font font, net.minecraft.network.chat.Component text, int x, int y, int color, boolean shadow) {
        if (text == null) return drawString(g, font, "", x, y, color, shadow);
        java.lang.reflect.Method m = findGuiGraphicsMethod(
                net.minecraft.client.gui.Font.class,
                net.minecraft.network.chat.Component.class,
                int.class,
                int.class,
                int.class,
                boolean.class);
        if (m != null) {
            try {
                Object out = m.invoke(g, font, text, x, y, color, shadow);
                if (out instanceof Number n) return n.intValue();
                return font.width(text);
            } catch (Throwable ignored) {}
        }
        return drawString(g, font, text.getString(), x, y, color, shadow);
    }

    public int drawString(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font font, String text, int x, int y, int color, boolean shadow) {
        String resolved = text == null ? "" : text;
        java.lang.reflect.Method m = findGuiGraphicsMethod(
                net.minecraft.client.gui.Font.class,
                String.class,
                int.class,
                int.class,
                int.class,
                boolean.class);
        if (m != null) {
            try {
                Object out = m.invoke(g, font, resolved, x, y, color, shadow);
                if (out instanceof Number n) return n.intValue();
            } catch (Throwable ignored) {}
        }
        return font.width(resolved);
    }

    @SuppressWarnings("unchecked")
    private <T> T getComponent(ItemStack stack, net.minecraft.core.component.DataComponentType<T> type) {
        if (ITEMSTACK_GET_COMPONENT_METHOD == null) return null;
        try {
            return (T) ITEMSTACK_GET_COMPONENT_METHOD.invoke(stack, type);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private boolean hasComponent(ItemStack stack, net.minecraft.core.component.DataComponentType<?> type) {
        if (ITEMSTACK_HAS_COMPONENT_METHOD == null) return false;
        try {
            Object out = ITEMSTACK_HAS_COMPONENT_METHOD.invoke(stack, type);
            return out instanceof Boolean b && b;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private <T> void setComponent(ItemStack stack, net.minecraft.core.component.DataComponentType<T> type, T value) {
        if (ITEMSTACK_SET_COMPONENT_METHOD == null) return;
        try {
            ITEMSTACK_SET_COMPONENT_METHOD.invoke(stack, type, value);
        } catch (Throwable ignored) {}
    }

    private void removeComponent(ItemStack stack, net.minecraft.core.component.DataComponentType<?> type) {
        if (ITEMSTACK_REMOVE_COMPONENT_METHOD == null) return;
        try {
            ITEMSTACK_REMOVE_COMPONENT_METHOD.invoke(stack, type);
        } catch (Throwable ignored) {}
    }

    private java.lang.reflect.Method findGuiGraphicsMethod(Class<?>... parameterTypes) {
        for (java.lang.reflect.Method method : net.minecraft.client.gui.GuiGraphics.class.getMethods()) {
            if (java.util.Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }
        return null;
    }

    private static java.lang.reflect.Method findItemStackComponentMethod(String... names) {
        for (String name : names) {
            try {
                return net.minecraft.world.item.ItemStack.class.getMethod(name, net.minecraft.core.component.DataComponentType.class);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static java.lang.reflect.Method findItemStackSetComponentMethod(String... names) {
        java.util.Set<String> nameSet = new java.util.HashSet<>(java.util.Arrays.asList(names));
        for (java.lang.reflect.Method method : net.minecraft.world.item.ItemStack.class.getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (!nameSet.contains(method.getName())) continue;
            if (parameterTypes.length != 2) continue;
            if (parameterTypes[0] != net.minecraft.core.component.DataComponentType.class) continue;
            return method;
        }
        return null;
    }
}
