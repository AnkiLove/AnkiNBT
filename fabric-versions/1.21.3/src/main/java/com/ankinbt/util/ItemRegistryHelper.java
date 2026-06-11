/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 *  net.minecraft.class_1802
 *  net.minecraft.class_2960
 *  net.minecraft.class_6880$class_6883
 *  net.minecraft.class_7923
 */
package com.ankinbt.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_6880;
import net.minecraft.class_7923;

public final class ItemRegistryHelper {
    private static final Pattern ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");
    private static final Object CACHE_LOCK = new Object();
    private static volatile Map<String, class_1792> CACHED_ITEMS = Collections.emptyMap();
    private static volatile int CACHED_SIZE = -1;

    private ItemRegistryHelper() {
    }

    public static List<String> allItemIds() {
        return new ArrayList<String>(ItemRegistryHelper.allItemsById().keySet());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Map<String, class_1792> allItemsById() {
        int size = ItemRegistryHelper.registrySize();
        Map<String, class_1792> cache = CACHED_ITEMS;
        if (!cache.isEmpty() && CACHED_SIZE == size) {
            return cache;
        }
        Object object = CACHE_LOCK;
        synchronized (object) {
            String id;
            cache = CACHED_ITEMS;
            if (!cache.isEmpty() && CACHED_SIZE == size) {
                return cache;
            }
            LinkedHashMap<String, class_1792> out = new LinkedHashMap<String, class_1792>();
            try {
                for (class_1792 item : class_7923.field_41178) {
                    if (!ItemRegistryHelper.isValid(item) || (id = ItemRegistryHelper.getItemId(item)).isBlank()) continue;
                    out.put(id, item);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (out.isEmpty()) {
                try {
                    for (Object key : class_7923.field_41178.method_10235()) {
                        id = ItemRegistryHelper.normalizeId(String.valueOf(key));
                        if (id.isBlank()) continue;
                        class_1792 item = ItemRegistryHelper.invokeItem(class_7923.field_41178, "getValue", key);
                        if (!ItemRegistryHelper.isValid(item)) {
                            item = ItemRegistryHelper.invokeItem(class_7923.field_41178, "get", key);
                        }
                        if (!ItemRegistryHelper.isValid(item)) continue;
                        out.put(id, item);
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            Map<String, class_1792> immutable = Collections.unmodifiableMap(out);
            CACHED_ITEMS = immutable;
            CACHED_SIZE = size;
            return immutable;
        }
    }

    public static String getItemId(class_1792 item) {
        if (item == null || item == class_1802.field_8162) {
            return "";
        }
        try {
            class_2960 key = class_7923.field_41178.method_10221((Object)item);
            String id = ItemRegistryHelper.normalizeId(String.valueOf(key));
            if (!id.isBlank()) {
                return id;
            }
        }
        catch (Throwable key) {
            // empty catch block
        }
        try {
            String id;
            class_6880.class_6883 holder = item.method_40131();
            Object key = holder.getClass().getMethod("key", new Class[0]).invoke((Object)holder, new Object[0]);
            try {
                Object location = key.getClass().getMethod("location", new Class[0]).invoke(key, new Object[0]);
                id = ItemRegistryHelper.normalizeId(String.valueOf(location));
                if (!id.isBlank()) {
                    return id;
                }
            }
            catch (Throwable location) {
                // empty catch block
            }
            try {
                Object identifier = key.getClass().getMethod("identifier", new Class[0]).invoke(key, new Object[0]);
                id = ItemRegistryHelper.normalizeId(String.valueOf(identifier));
                if (!id.isBlank()) {
                    return id;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return ItemRegistryHelper.normalizeId(String.valueOf(key));
        }
        catch (Throwable throwable) {
            return "";
        }
    }

    public static class_1792 resolveItem(String itemId) {
        class_1792 minecraftDefault;
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        String norm = ItemRegistryHelper.normalizeId(itemId);
        Map<String, class_1792> map = ItemRegistryHelper.allItemsById();
        class_1792 direct = map.get(norm);
        if (ItemRegistryHelper.isValid(direct)) {
            return direct;
        }
        if (!norm.contains(":") && ItemRegistryHelper.isValid(minecraftDefault = map.get("minecraft:" + norm))) {
            return minecraftDefault;
        }
        Object rl = ItemRegistryHelper.parseId(norm);
        if (rl != null) {
            class_1792 byGetValue = ItemRegistryHelper.invokeItem(class_7923.field_41178, "getValue", rl);
            if (ItemRegistryHelper.isValid(byGetValue)) {
                return byGetValue;
            }
            class_1792 byGet = ItemRegistryHelper.invokeItem(class_7923.field_41178, "get", rl);
            if (ItemRegistryHelper.isValid(byGet)) {
                return byGet;
            }
            class_1792 byOptional = ItemRegistryHelper.invokeOptionalItem(class_7923.field_41178, "getOptional", rl);
            if (ItemRegistryHelper.isValid(byOptional)) {
                return byOptional;
            }
        }
        try {
            for (Object key : class_7923.field_41178.method_10235()) {
                if (!norm.equals(ItemRegistryHelper.normalizeId(String.valueOf(key)))) continue;
                class_1792 found = ItemRegistryHelper.invokeItem(class_7923.field_41178, "getValue", key);
                if (ItemRegistryHelper.isValid(found)) {
                    return found;
                }
                found = ItemRegistryHelper.invokeItem(class_7923.field_41178, "get", key);
                if (!ItemRegistryHelper.isValid(found)) continue;
                return found;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static int registrySize() {
        try {
            return class_7923.field_41178.method_10204();
        }
        catch (Throwable ignored) {
            return -1;
        }
    }

    private static boolean isValid(class_1792 item) {
        return item != null && item != class_1802.field_8162;
    }

    private static class_1792 invokeItem(Object registry, String method, Object arg) {
        try {
            class_1792 i;
            Method m = registry.getClass().getMethod(method, arg.getClass());
            Object out = m.invoke(registry, arg);
            return out instanceof class_1792 ? (i = (class_1792)out) : null;
        }
        catch (NoSuchMethodException e) {
            for (Method m : registry.getClass().getMethods()) {
                if (!m.getName().equals(method) || m.getParameterCount() != 1 || !m.getParameterTypes()[0].isAssignableFrom(arg.getClass())) continue;
                try {
                    class_1792 i;
                    Object out = m.invoke(registry, arg);
                    return out instanceof class_1792 ? (i = (class_1792)out) : null;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            return null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static class_1792 invokeOptionalItem(Object registry, String method, Object arg) {
        try {
            Optional opt;
            Object var7_7;
            Method m = registry.getClass().getMethod(method, arg.getClass());
            Object out = m.invoke(registry, arg);
            if (out instanceof Optional && (var7_7 = (opt = (Optional)out).orElse(null)) instanceof class_1792) {
                class_1792 i = var7_7;
                return i;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static String normalizeId(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) {
            return "";
        }
        Matcher matcher = ID_PATTERN.matcher(s);
        return matcher.find() ? matcher.group(1) : s;
    }

    private static Object parseId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            Class<?> rl = Class.forName("net.minecraft.resources.ResourceLocation");
            try {
                return ItemRegistryHelper.unwrapOptional(rl.getMethod("tryParse", String.class).invoke(null, id));
            }
            catch (NoSuchMethodException ignored) {
                return ItemRegistryHelper.unwrapOptional(rl.getMethod("parse", String.class).invoke(null, id));
            }
        }
        catch (Throwable rl) {
            try {
                Class<?> idCls = Class.forName("net.minecraft.resources.Identifier");
                try {
                    return ItemRegistryHelper.unwrapOptional(idCls.getMethod("tryParse", String.class).invoke(null, id));
                }
                catch (NoSuchMethodException ignored) {
                    return ItemRegistryHelper.unwrapOptional(idCls.getMethod("of", String.class).invoke(null, id));
                }
            }
            catch (Throwable idCls) {
                try {
                    Class<?> idCls2 = Class.forName("net.minecraft.util.Identifier");
                    try {
                        return ItemRegistryHelper.unwrapOptional(idCls2.getMethod("tryParse", String.class).invoke(null, id));
                    }
                    catch (NoSuchMethodException ignored) {
                        return ItemRegistryHelper.unwrapOptional(idCls2.getMethod("of", String.class).invoke(null, id));
                    }
                }
                catch (Throwable throwable) {
                    return null;
                }
            }
        }
    }

    private static Object unwrapOptional(Object value) {
        if (value instanceof Optional) {
            Optional opt = (Optional)value;
            return opt.orElse(null);
        }
        return value;
    }
}

