package com.ankinbt.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class EnchantmentTooltipHelper {
    private static final String TOOLTIP_DISPLAY_ID = "minecraft:tooltip_display";

    private EnchantmentTooltipHelper() {
    }

    public static boolean isHidden(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        DataComponentType<?> tooltipDisplay = findComponentType(TOOLTIP_DISPLAY_ID);
        if (tooltipDisplay != null) {
            Object display = getComponent(stack, tooltipDisplay);
            Collection<?> hidden = hiddenComponents(display);
            return hidden != null
                    && (hidden.contains(DataComponents.ENCHANTMENTS)
                    || hidden.contains(DataComponents.STORED_ENCHANTMENTS));
        }
        return legacyHidden(stack.get(DataComponents.ENCHANTMENTS))
                || legacyHidden(stack.get(DataComponents.STORED_ENCHANTMENTS));
    }

    public static boolean setHidden(ItemStack stack, boolean hidden) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        DataComponentType<?> tooltipDisplay = findComponentType(TOOLTIP_DISPLAY_ID);
        if (tooltipDisplay != null) {
            return setModernHidden(stack, tooltipDisplay, hidden);
        }
        return setLegacyHidden(stack, hidden);
    }

    private static boolean setModernHidden(ItemStack stack, DataComponentType<?> tooltipDisplay, boolean hidden) {
        Object display = getComponent(stack, tooltipDisplay);
        if (display == null) {
            display = defaultTooltipDisplay();
        }
        if (display == null) {
            return false;
        }
        Object updated = withHidden(display, DataComponents.ENCHANTMENTS, hidden);
        updated = withHidden(updated, DataComponents.STORED_ENCHANTMENTS, hidden);
        if (updated == null) {
            return false;
        }
        setComponent(stack, tooltipDisplay, updated);
        return true;
    }

    private static boolean setLegacyHidden(ItemStack stack, boolean hidden) {
        ItemEnchantments normal = stack.get(DataComponents.ENCHANTMENTS);
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        boolean changed = false;
        if (normal != null) {
            ItemEnchantments updated = withLegacyTooltip(normal, !hidden);
            if (updated == null) {
                return false;
            }
            stack.set(DataComponents.ENCHANTMENTS, updated);
            changed = true;
        }
        if (stored != null) {
            ItemEnchantments updated = withLegacyTooltip(stored, !hidden);
            if (updated == null) {
                return false;
            }
            stack.set(DataComponents.STORED_ENCHANTMENTS, updated);
            changed = true;
        }
        if (!changed) {
            ItemEnchantments updated = withLegacyTooltip(ItemEnchantments.EMPTY, !hidden);
            if (updated == null) {
                return false;
            }
            stack.set(DataComponents.ENCHANTMENTS, updated);
        }
        return true;
    }

    private static boolean legacyHidden(ItemEnchantments enchantments) {
        if (enchantments == null) {
            return false;
        }
        ItemEnchantments hidden = withLegacyTooltip(enchantments, false);
        return hidden != null && hidden.equals(enchantments);
    }

    private static ItemEnchantments withLegacyTooltip(ItemEnchantments enchantments, boolean visible) {
        for (Method method : enchantments.getClass().getMethods()) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || params[0] != Boolean.TYPE
                    || !ItemEnchantments.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            try {
                return (ItemEnchantments)method.invoke(enchantments, visible);
            }
            catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static DataComponentType<?> findComponentType(String id) {
        try {
            for (DataComponentType<?> type : BuiltInRegistries.DATA_COMPONENT_TYPE) {
                Object key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
                if (id.equals(String.valueOf(key))) {
                    return type;
                }
            }
        }
        catch (Throwable ignored) {
        }
        return null;
    }

    private static Object defaultTooltipDisplay() {
        for (String className : new String[]{
                "net.minecraft.world.item.component.TooltipDisplay",
                "net.minecraft.class_10712"}) {
            try {
                Class<?> type = Class.forName(className);
                for (Field field : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) || !type.isAssignableFrom(field.getType())) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value != null) {
                        return value;
                    }
                }
            }
            catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static Collection<?> hiddenComponents(Object display) {
        if (display == null) {
            return null;
        }
        for (Method method : display.getClass().getMethods()) {
            if (method.getParameterCount() != 0 || !Collection.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            try {
                Object value = method.invoke(display);
                if (value instanceof Collection<?>) {
                    return (Collection<?>)value;
                }
            }
            catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static Object withHidden(Object display, DataComponentType<?> component, boolean hidden) {
        if (display == null) {
            return null;
        }
        for (Method method : display.getClass().getMethods()) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 2 || params[1] != Boolean.TYPE
                    || !params[0].isAssignableFrom(component.getClass())
                    || !display.getClass().isAssignableFrom(method.getReturnType())) {
                continue;
            }
            try {
                return method.invoke(display, component, hidden);
            }
            catch (Throwable ignored) {
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object getComponent(ItemStack stack, DataComponentType<?> type) {
        return stack.get((DataComponentType)type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setComponent(ItemStack stack, DataComponentType<?> type, Object value) {
        stack.set((DataComponentType)type, value);
    }
}
