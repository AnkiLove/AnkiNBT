package com.ankinbt.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

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

public final class ItemRegistryHelper {
    private static final Pattern ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");
    private static final Object CACHE_LOCK = new Object();
    private static volatile Map<String, Item> CACHED_ITEMS = Collections.emptyMap();
    private static volatile int CACHED_SIZE = -1;

    private ItemRegistryHelper() {}

    public static List<String> allItemIds() {
        return new ArrayList<>(allItemsById().keySet());
    }

    public static Map<String, Item> allItemsById() {
        int size = registrySize();
        Map<String, Item> cache = CACHED_ITEMS;
        if (!cache.isEmpty() && CACHED_SIZE == size) {
            return cache;
        }
        synchronized (CACHE_LOCK) {
            cache = CACHED_ITEMS;
            if (!cache.isEmpty() && CACHED_SIZE == size) {
                return cache;
            }
            LinkedHashMap<String, Item> out = new LinkedHashMap<>();
            try {
                for (Item item : BuiltInRegistries.ITEM) {
                    if (!isValid(item)) continue;
                    String id = getItemId(item);
                    if (!id.isBlank()) out.put(id, item);
                }
            } catch (Throwable ignored) {}

            // Fallback path for edge versions where direct iteration may fail.
            if (out.isEmpty()) {
                try {
                    for (Object key : BuiltInRegistries.ITEM.keySet()) {
                        String id = normalizeId(String.valueOf(key));
                        if (id.isBlank()) continue;
                        Item item = invokeItem(BuiltInRegistries.ITEM, "getValue", key);
                        if (!isValid(item)) item = invokeItem(BuiltInRegistries.ITEM, "get", key);
                        if (isValid(item)) out.put(id, item);
                    }
                } catch (Throwable ignored) {}
            }

            Map<String, Item> immutable = Collections.unmodifiableMap(out);
            CACHED_ITEMS = immutable;
            CACHED_SIZE = size;
            return immutable;
        }
    }

    public static String getItemId(Item item) {
        if (item == null || item == Items.AIR) return "";
        try {
            Object key = BuiltInRegistries.ITEM.getKey(item);
            String id = normalizeId(String.valueOf(key));
            if (!id.isBlank()) return id;
        } catch (Throwable ignored) {}
        try {
            Object holder = item.builtInRegistryHolder();
            Object key = holder.getClass().getMethod("key").invoke(holder);
            try {
                Object location = key.getClass().getMethod("location").invoke(key);
                String id = normalizeId(String.valueOf(location));
                if (!id.isBlank()) return id;
            } catch (Throwable ignored) {}
            try {
                Object identifier = key.getClass().getMethod("identifier").invoke(key);
                String id = normalizeId(String.valueOf(identifier));
                if (!id.isBlank()) return id;
            } catch (Throwable ignored) {}
            return normalizeId(String.valueOf(key));
        } catch (Throwable ignored) {}
        return "";
    }

    public static Item resolveItem(String itemId) {
        if (itemId == null || itemId.isBlank()) return null;
        String norm = normalizeId(itemId);
        Map<String, Item> map = allItemsById();
        Item direct = map.get(norm);
        if (isValid(direct)) return direct;
        if (!norm.contains(":")) {
            Item minecraftDefault = map.get("minecraft:" + norm);
            if (isValid(minecraftDefault)) return minecraftDefault;
        }

        Object rl = parseId(norm);
        if (rl != null) {
            Item byGetValue = invokeItem(BuiltInRegistries.ITEM, "getValue", rl);
            if (isValid(byGetValue)) return byGetValue;
            Item byGet = invokeItem(BuiltInRegistries.ITEM, "get", rl);
            if (isValid(byGet)) return byGet;
            Item byOptional = invokeOptionalItem(BuiltInRegistries.ITEM, "getOptional", rl);
            if (isValid(byOptional)) return byOptional;
        }

        try {
            for (Object key : BuiltInRegistries.ITEM.keySet()) {
                if (!norm.equals(normalizeId(String.valueOf(key)))) continue;
                Item found = invokeItem(BuiltInRegistries.ITEM, "getValue", key);
                if (isValid(found)) return found;
                found = invokeItem(BuiltInRegistries.ITEM, "get", key);
                if (isValid(found)) return found;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static int registrySize() {
        try {
            return BuiltInRegistries.ITEM.size();
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static boolean isValid(Item item) {
        return item != null && item != Items.AIR;
    }

    private static Item invokeItem(Object registry, String method, Object arg) {
        try {
            Method m = registry.getClass().getMethod(method, arg.getClass());
            Object out = m.invoke(registry, arg);
            return out instanceof Item i ? i : null;
        } catch (NoSuchMethodException e) {
            for (Method m : registry.getClass().getMethods()) {
                if (!m.getName().equals(method) || m.getParameterCount() != 1) continue;
                if (!m.getParameterTypes()[0].isAssignableFrom(arg.getClass())) continue;
                try {
                    Object out = m.invoke(registry, arg);
                    return out instanceof Item i ? i : null;
                } catch (Throwable ignored) {}
            }
            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Item invokeOptionalItem(Object registry, String method, Object arg) {
        try {
            Method m = registry.getClass().getMethod(method, arg.getClass());
            Object out = m.invoke(registry, arg);
            if (out instanceof Optional<?> opt && opt.orElse(null) instanceof Item i) return i;
        } catch (Throwable ignored) {}
        return null;
    }

    private static String normalizeId(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return "";
        Matcher matcher = ID_PATTERN.matcher(s);
        return matcher.find() ? matcher.group(1) : s;
    }

    private static Object parseId(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            Class<?> rl = Class.forName("net.minecraft.resources.ResourceLocation");
            try {
                return unwrapOptional(rl.getMethod("tryParse", String.class).invoke(null, id));
            } catch (NoSuchMethodException ignored) {
                return unwrapOptional(rl.getMethod("parse", String.class).invoke(null, id));
            }
        } catch (Throwable ignored) {}
        try {
            Class<?> idCls = Class.forName("net.minecraft.resources.Identifier");
            try {
                return unwrapOptional(idCls.getMethod("tryParse", String.class).invoke(null, id));
            } catch (NoSuchMethodException ignored) {
                return unwrapOptional(idCls.getMethod("of", String.class).invoke(null, id));
            }
        } catch (Throwable ignored) {}
        try {
            Class<?> idCls = Class.forName("net.minecraft.util.Identifier");
            try {
                return unwrapOptional(idCls.getMethod("tryParse", String.class).invoke(null, id));
            } catch (NoSuchMethodException ignored) {
                return unwrapOptional(idCls.getMethod("of", String.class).invoke(null, id));
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static Object unwrapOptional(Object value) {
        if (value instanceof Optional<?> opt) return opt.orElse(null);
        return value;
    }
}
