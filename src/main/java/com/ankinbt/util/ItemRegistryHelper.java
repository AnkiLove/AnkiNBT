/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ItemRegistryHelper {
    private static final Pattern ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");
    private static final Object CACHE_LOCK = new Object();
    private static volatile Map<String, Item> CACHED_ITEMS = Collections.emptyMap();
    private static volatile int CACHED_SIZE = -1;

    private ItemRegistryHelper() {
    }

    public static List<String> allItemIds() {
        return new ArrayList<String>(ItemRegistryHelper.allItemsById().keySet());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Map<String, Item> allItemsById() {
        int size = ItemRegistryHelper.registrySize();
        Map<String, Item> cache = CACHED_ITEMS;
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
            LinkedHashMap<String, Item> out = new LinkedHashMap<String, Item>();
            try {
                for (Item item : BuiltInRegistries.ITEM) {
                    if (!ItemRegistryHelper.isValid(item) || (id = ItemRegistryHelper.getItemId(item)).isBlank()) continue;
                    out.put(id, item);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (out.isEmpty()) {
                try {
                    for (Object key : BuiltInRegistries.ITEM.keySet()) {
                        id = ItemRegistryHelper.normalizeId(String.valueOf(key));
                        if (id.isBlank()) continue;
                        Item item = ItemRegistryHelper.invokeItem(BuiltInRegistries.ITEM, "getValue", key);
                        if (!ItemRegistryHelper.isValid(item)) {
                            item = ItemRegistryHelper.invokeItem(BuiltInRegistries.ITEM, "get", key);
                        }
                        if (!ItemRegistryHelper.isValid(item)) continue;
                        out.put(id, item);
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            Map<String, Item> immutable = Collections.unmodifiableMap(out);
            CACHED_ITEMS = immutable;
            CACHED_SIZE = size;
            return immutable;
        }
    }

    public static String getItemId(Item item) {
        if (item == null || item == Items.AIR) {
            return "";
        }
        try {
            String id = ItemRegistryHelper.normalizeId(String.valueOf(BuiltInRegistries.ITEM.getKey(item)));
            if (!id.isBlank()) {
                return id;
            }
        }
        catch (Throwable key) {
            // empty catch block
        }
        try {
            String id;
            Holder.Reference holder = item.builtInRegistryHolder();
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

    public static Item resolveItem(String itemId) {
        Item minecraftDefault;
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        String norm = ItemRegistryHelper.normalizeId(itemId);
        Map<String, Item> map = ItemRegistryHelper.allItemsById();
        Item direct = map.get(norm);
        if (ItemRegistryHelper.isValid(direct)) {
            return direct;
        }
        if (!norm.contains(":") && ItemRegistryHelper.isValid(minecraftDefault = map.get("minecraft:" + norm))) {
            return minecraftDefault;
        }
        Object rl = ItemRegistryHelper.parseId(norm);
        if (rl != null) {
            Item byGetValue = ItemRegistryHelper.invokeItem(BuiltInRegistries.ITEM, "getValue", rl);
            if (ItemRegistryHelper.isValid(byGetValue)) {
                return byGetValue;
            }
            Item byGet = ItemRegistryHelper.invokeItem(BuiltInRegistries.ITEM, "get", rl);
            if (ItemRegistryHelper.isValid(byGet)) {
                return byGet;
            }
            Item byOptional = ItemRegistryHelper.invokeOptionalItem(BuiltInRegistries.ITEM, "getOptional", rl);
            if (ItemRegistryHelper.isValid(byOptional)) {
                return byOptional;
            }
        }
        try {
            for (Object key : BuiltInRegistries.ITEM.keySet()) {
                if (!norm.equals(ItemRegistryHelper.normalizeId(String.valueOf(key)))) continue;
                Item found = ItemRegistryHelper.invokeItem(BuiltInRegistries.ITEM, "getValue", key);
                if (ItemRegistryHelper.isValid(found)) {
                    return found;
                }
                found = ItemRegistryHelper.invokeItem(BuiltInRegistries.ITEM, "get", key);
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
            return BuiltInRegistries.ITEM.size();
        }
        catch (Throwable ignored) {
            return -1;
        }
    }

    private static boolean isValid(Item item) {
        return item != null && item != Items.AIR;
    }

    private static Item invokeItem(Object registry, String method, Object arg) {
        try {
            Item i;
            Method m = registry.getClass().getMethod(method, arg.getClass());
            Object out = m.invoke(registry, arg);
            return out instanceof Item ? (i = (Item)out) : null;
        }
        catch (NoSuchMethodException e) {
            for (Method m : registry.getClass().getMethods()) {
                if (!m.getName().equals(method) || m.getParameterCount() != 1 || !m.getParameterTypes()[0].isAssignableFrom(arg.getClass())) continue;
                try {
                    Item i;
                    Object out = m.invoke(registry, arg);
                    return out instanceof Item ? (i = (Item)out) : null;
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

    private static Item invokeOptionalItem(Object registry, String method, Object arg) {
        try {
            Optional opt;
            Object var7_7;
            Method m = registry.getClass().getMethod(method, arg.getClass());
            Object out = m.invoke(registry, arg);
            if (out instanceof Optional && (var7_7 = (opt = (Optional)out).orElse(null)) instanceof Item) {
                Item i = (Item)var7_7;
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
