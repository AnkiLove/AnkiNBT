package com.ankinbt.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public class VersionCompat {

    private static VersionCompat INSTANCE;
    private static final Method ITEMSTACK_GET_COMPONENT_METHOD =
            findItemStackComponentMethod("get", "method_57824", "method_58694");
    private static final Method ITEMSTACK_HAS_COMPONENT_METHOD =
            findItemStackComponentMethod("has", "method_57826");
    private static final Method ITEMSTACK_REMOVE_COMPONENT_METHOD =
            findItemStackComponentMethod("remove", "method_57381");
    private static final Method ITEMSTACK_SET_COMPONENT_METHOD =
            findItemStackSetComponentMethod("set", "method_57379");

    public static VersionCompat get() {
        if (INSTANCE == null) INSTANCE = new VersionCompat();
        return INSTANCE;
    }

    public java.nio.file.Path getConfigDir() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
    }

    public java.nio.file.Path getGameDir() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir();
    }

    public String getKeyDisplayName(int keyCode) {
        if (keyCode == com.mojang.blaze3d.platform.InputConstants.KEY_COMMA) return ",";
        if (keyCode >= com.mojang.blaze3d.platform.InputConstants.KEY_A && keyCode <= com.mojang.blaze3d.platform.InputConstants.KEY_Z) {
            return Character.toString((char)('A' + (keyCode - com.mojang.blaze3d.platform.InputConstants.KEY_A)));
        }
        if (keyCode >= com.mojang.blaze3d.platform.InputConstants.KEY_0 && keyCode <= com.mojang.blaze3d.platform.InputConstants.KEY_9) {
            return Character.toString((char)('0' + (keyCode - com.mojang.blaze3d.platform.InputConstants.KEY_0)));
        }
        try {
            String name = com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName().getString();
            if (name != null && !name.isBlank() && !name.startsWith("#")) return name;
        } catch (Throwable ignored) {}
        return "KEY(" + keyCode + ")";
    }

    public List<String> getAllEnchantIds() {
        return getAllRegistryIds(Registries.ENCHANTMENT);
    }

    public List<String> getAllAttributeIds() {
        return getAllRegistryIds(Registries.ATTRIBUTE);
    }

    @SuppressWarnings("unchecked")
    public Optional<Holder.Reference<Enchantment>> getEnchantHolder(String id) {
        return (Optional<Holder.Reference<Enchantment>>)(Optional<?>)getHolder(Registries.ENCHANTMENT, id);
    }

    @SuppressWarnings("unchecked")
    public Optional<Holder.Reference<Attribute>> getAttributeHolder(String id) {
        return (Optional<Holder.Reference<Attribute>>)(Optional<?>)getHolder(Registries.ATTRIBUTE, id);
    }

    public boolean isFireResistant(ItemStack stack) {
        DataComponentType<?> type = component("DAMAGE_RESISTANT", "FIRE_RESISTANT");
        return type != null && hasComponent(stack, type);
    }

    public void setFireResistant(ItemStack stack, boolean value) {
        DataComponentType<?> type = component("DAMAGE_RESISTANT", "FIRE_RESISTANT");
        if (type == null) return;
        if (!value) {
            removeComponent(stack, type);
            return;
        }
        Object resistant = constructDamageResistant();
        setComponentUnchecked(stack, type, resistant != null ? resistant : unitInstance());
    }

    public int getCustomModelData(ItemStack stack) {
        Object cmd = getComponent(stack, DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null) return 0;
        Object value = invokeAny(cmd, "value", "comp_2382");
        if (value instanceof Number number) return number.intValue();
        Object floats = invokeAny(cmd, "floats");
        if (floats instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    public void setCustomModelData(ItemStack stack, int value) {
        Object cmd = constructCustomModelData(value);
        if (cmd != null) {
            setComponentUnchecked(stack, DataComponents.CUSTOM_MODEL_DATA, cmd);
        }
    }

    public boolean hasFood(ItemStack stack) {
        return getComponent(stack, DataComponents.FOOD) != null;
    }

    public int getFoodNutrition(ItemStack stack) {
        Object food = getComponent(stack, DataComponents.FOOD);
        Object value = invokeAny(food, "nutrition", "comp_2491");
        return value instanceof Number number ? number.intValue() : 0;
    }

    public float getFoodSaturation(ItemStack stack) {
        Object food = getComponent(stack, DataComponents.FOOD);
        Object value = invokeAny(food, "saturation", "comp_2492");
        return value instanceof Number number ? number.floatValue() : 0.0f;
    }

    public void setFoodNutrition(ItemStack stack, int nutrition) {
        Object food = getComponent(stack, DataComponents.FOOD);
        Object replacement = constructFood(food, nutrition, getFoodSaturation(stack));
        if (replacement != null) {
            setComponentUnchecked(stack, DataComponents.FOOD, replacement);
        }
    }

    public void setFoodSaturation(ItemStack stack, float saturation) {
        Object food = getComponent(stack, DataComponents.FOOD);
        Object replacement = constructFood(food, getFoodNutrition(stack), saturation);
        if (replacement != null) {
            setComponentUnchecked(stack, DataComponents.FOOD, replacement);
        }
    }

    public Set<String> getCompoundKeys(CompoundTag tag) {
        Object keys = invokeAny(tag, "getAllKeys", "keySet", "method_10541");
        if (keys instanceof Set<?> set) {
            LinkedHashSet<String> out = new LinkedHashSet<>();
            for (Object key : set) out.add(String.valueOf(key));
            return out;
        }
        return Collections.emptySet();
    }

    public String getTagAsString(Tag tag) {
        Object value = invokeAny(tag, "getAsString", "method_10714");
        return value != null ? String.valueOf(value) : String.valueOf(tag);
    }

    public byte getByteValue(ByteTag tag) {
        Object value = invokeAny(tag, "getAsByte", "value", "method_10698");
        return value instanceof Number number ? number.byteValue() : 0;
    }

    public short getShortValue(ShortTag tag) {
        Object value = invokeAny(tag, "getAsShort", "value", "method_10696");
        return value instanceof Number number ? number.shortValue() : 0;
    }

    public int getIntValue(IntTag tag) {
        Object value = invokeAny(tag, "getAsInt", "value", "method_10701");
        return value instanceof Number number ? number.intValue() : 0;
    }

    public long getLongValue(LongTag tag) {
        Object value = invokeAny(tag, "getAsLong", "value", "method_10699");
        return value instanceof Number number ? number.longValue() : 0L;
    }

    public float getFloatValue(FloatTag tag) {
        Object value = invokeAny(tag, "getAsFloat", "value", "method_10700");
        return value instanceof Number number ? number.floatValue() : 0.0f;
    }

    public double getDoubleValue(DoubleTag tag) {
        Object value = invokeAny(tag, "getAsDouble", "value", "method_10697");
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    public String getStringValue(StringTag tag) {
        Object value = invokeAny(tag, "getAsString", "value", "method_10714");
        return value != null ? String.valueOf(value) : "";
    }

    public String compoundGetString(CompoundTag tag, String key) {
        Object value = invokeNamed(tag, "getString", key);
        if (value instanceof Optional<?> optional) return optional.map(String::valueOf).orElse("");
        return value != null ? String.valueOf(value) : "";
    }

    public int compoundGetInt(CompoundTag tag, String key) {
        Object value = invokeNamed(tag, "getInt", key);
        if (value instanceof Optional<?> optional) value = optional.isPresent() ? optional.get() : 0;
        return value instanceof Number number ? number.intValue() : 0;
    }

    public int getSelectedSlot(Inventory inv) {
        Object value = invokeAny(inv, "getSelectedSlot");
        if (value instanceof Number number) return number.intValue();
        try {
            Field selected = Inventory.class.getDeclaredField("selected");
            selected.setAccessible(true);
            return selected.getInt(inv);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public boolean isHideTooltip(ItemStack stack) {
        DataComponentType<?> type = component("HIDE_TOOLTIP");
        return type != null && hasComponent(stack, type);
    }

    public void setHideTooltip(ItemStack stack, boolean value) {
        setOptionalUnitComponent(stack, value, "HIDE_TOOLTIP");
    }

    public boolean isHideAdditional(ItemStack stack) {
        DataComponentType<?> type = component("HIDE_ADDITIONAL_TOOLTIP");
        return type != null && hasComponent(stack, type);
    }

    public void setHideAdditional(ItemStack stack, boolean value) {
        setOptionalUnitComponent(stack, value, "HIDE_ADDITIONAL_TOOLTIP");
    }

    public boolean hasHideTooltipFeature() {
        return component("HIDE_TOOLTIP") != null;
    }

    public boolean hasHideAdditionalFeature() {
        return component("HIDE_ADDITIONAL_TOOLTIP") != null;
    }

    public void setUnbreakable(ItemStack stack, boolean value) {
        removeComponent(stack, DataComponents.UNBREAKABLE);
        if (!value) {
            return;
        }
        Object unbreakable = constructFirst("net.minecraft.world.item.component.Unbreakable",
                new Class<?>[]{boolean.class}, true);
        setComponentUnchecked(stack, DataComponents.UNBREAKABLE, unbreakable != null ? unbreakable : unitInstance());
    }

    public void sanitizeForCreativeSave(ItemStack stack) {
        if (stack != null && hasComponent(stack, DataComponents.UNBREAKABLE)) {
            setUnbreakable(stack, true);
        }
    }

    public void setDyedColor(ItemStack stack, int rgb) {
        Object color = constructFirst("net.minecraft.world.item.component.DyedItemColor",
                new Class<?>[]{int.class, boolean.class}, rgb, true);
        if (color == null) {
            color = constructFirst("net.minecraft.world.item.component.DyedItemColor",
                    new Class<?>[]{int.class}, rgb);
        }
        if (color != null) {
            setComponentUnchecked(stack, DataComponents.DYED_COLOR, color);
        }
    }

    public ItemAttributeModifiers withEntries(List<ItemAttributeModifiers.Entry> entries, ItemAttributeModifiers old) {
        Object keepTooltip = invokeAny(old, "showInTooltip");
        Object withTooltip = keepTooltip instanceof Boolean b
                ? constructItemAttributeModifiers(new Class<?>[]{List.class, boolean.class}, entries, b)
                : null;
        if (withTooltip instanceof ItemAttributeModifiers modifiers) return modifiers;
        Object plain = constructItemAttributeModifiers(new Class<?>[]{List.class}, entries);
        return plain instanceof ItemAttributeModifiers modifiers ? modifiers : old;
    }

    public void renderTooltip(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font f, Component tooltip, int mx, int my) {
        if (invokeGui(g, "renderTooltip", f, tooltip, mx, my)) return;
        List<?> lines = f.split(tooltip, 200);
        ArrayList<Object> components = new ArrayList<>();
        for (Object line : lines) {
            Object clientLine = constructFirst("net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip",
                    new Class<?>[]{line.getClass()}, line);
            components.add(clientLine != null ? clientLine : line);
        }
        if (invokeGui(g, "renderTooltip", f, components, mx, my,
                net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null)) {
            return;
        }
        invokeGui(g, "renderComponentTooltip", f, lines, mx, my);
    }

    public int drawString(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font font, Component text, int x, int y, int color, boolean shadow) {
        if (text == null) return drawString(g, font, "", x, y, color, shadow);
        Object out = invokeGuiWithResult(g, "drawString", font, text, x, y, color, shadow);
        if (out instanceof Number number) return number.intValue();
        return drawString(g, font, text.getString(), x, y, color, shadow);
    }

    public int drawString(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font font, String text, int x, int y, int color, boolean shadow) {
        String resolved = text == null ? "" : text;
        Object out = invokeGuiWithResult(g, "drawString", font, resolved, x, y, color, shadow);
        if (out instanceof Number number) return number.intValue();
        try {
            g.drawString(font, resolved, x, y, color, shadow);
        } catch (Throwable ignored) {}
        return font.width(resolved);
    }

    private List<String> getAllRegistryIds(Object registryKey) {
        ArrayList<String> ids = new ArrayList<>();
        Object registry = getRegistry(registryKey);
        if (registry == null) return ids;
        Object holders = invokeAny(registry, "holders");
        if (holders instanceof Stream<?> stream) {
            stream.forEach(holder -> {
                Object key = invokeAny(holder, "key", "unwrapKey");
                if (key instanceof Optional<?> optional) key = optional.orElse(null);
                addId(ids, idFromKey(key));
            });
        }
        Object keys = invokeAny(registry, "listElementIds");
        if (keys instanceof Stream<?> stream) {
            stream.forEach(key -> addId(ids, idFromKey(key)));
        } else if (keys instanceof Iterable<?> iterable) {
            for (Object key : iterable) {
                addId(ids, idFromKey(key));
            }
        }
        Object keySet = invokeAny(registry, "keySet", "method_10235");
        if (keySet instanceof Iterable<?> iterable) {
            for (Object key : iterable) addId(ids, idFromKey(key));
        }
        return ids;
    }

    private Optional<?> getHolder(Object registryKey, String id) {
        Object registry = getRegistry(registryKey);
        Object location = parseResourceId(id);
        if (registry == null || location == null) return Optional.empty();
        Object holder = invokeRegistryLookup(registry, location, "getHolder", "get");
        if (holder == null) {
            holder = invokeRegistryLookup(registry, createElementKey(registryKey, location), "getHolder", "get");
        }
        if (holder instanceof Optional<?> optional) return optional;
        return holder != null ? Optional.of(holder) : Optional.empty();
    }

    private Object getRegistry(Object registryKey) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        Object access = mc.level.registryAccess();
        return invokeRegistryLookup(access, registryKey, "registryOrThrow", "lookupOrThrow", "method_30530");
    }

    private Object invokeRegistryLookup(Object target, Object argument, String... names) {
        if (target == null || argument == null) return null;
        Set<String> allowed = new HashSet<>(Arrays.asList(names));
        for (Method method : target.getClass().getMethods()) {
            if (!allowed.contains(method.getName()) || method.getParameterCount() != 1) continue;
            Class<?> parameter = method.getParameterTypes()[0];
            if (!parameter.isAssignableFrom(argument.getClass())) continue;
            try {
                return method.invoke(target, argument);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private String idFromKey(Object key) {
        if (key == null) return "";
        Object id = invokeAny(key, "location", "identifier", "method_29177");
        return id != null ? String.valueOf(id) : String.valueOf(key);
    }

    private Object parseResourceId(String id) {
        Object loc = parseResourceIdWith("net.minecraft.resources.ResourceLocation", id);
        return loc != null ? loc : parseResourceIdWith("net.minecraft.resources.Identifier", id);
    }

    private Object parseResourceIdWith(String className, String id) {
        try {
            Class<?> cls = Class.forName(className);
            for (String methodName : List.of("tryParse", "parse", "method_12829")) {
                try {
                    Method method = cls.getMethod(methodName, String.class);
                    return method.invoke(null, id);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private Object createElementKey(Object registryKey, Object location) {
        Object key = createElementKeyWith("net.minecraft.resources.ResourceKey", registryKey, location);
        return key != null ? key : createElementKeyWith("net.minecraft.registry.RegistryKey", registryKey, location);
    }

    private Object createElementKeyWith(String className, Object registryKey, Object location) {
        try {
            Class<?> cls = Class.forName(className);
            for (Method method : cls.getMethods()) {
                if (!List.of("create", "of", "method_29179", "method_29180").contains(method.getName()) || method.getParameterCount() != 2) continue;
                Class<?>[] params = method.getParameterTypes();
                if (!params[0].isAssignableFrom(registryKey.getClass()) || !params[1].isAssignableFrom(location.getClass())) continue;
                try {
                    return method.invoke(null, registryKey, location);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private void addId(List<String> ids, String id) {
        if (id != null && !id.isBlank() && !ids.contains(id)) ids.add(id);
    }

    @SuppressWarnings("unchecked")
    private <T> T getComponent(ItemStack stack, DataComponentType<T> type) {
        if (ITEMSTACK_GET_COMPONENT_METHOD == null) return null;
        try {
            return (T) ITEMSTACK_GET_COMPONENT_METHOD.invoke(stack, type);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private boolean hasComponent(ItemStack stack, DataComponentType<?> type) {
        if (ITEMSTACK_HAS_COMPONENT_METHOD == null) return false;
        try {
            Object out = ITEMSTACK_HAS_COMPONENT_METHOD.invoke(stack, type);
            return out instanceof Boolean b && b;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setComponentUnchecked(ItemStack stack, DataComponentType<?> type, Object value) {
        if (ITEMSTACK_SET_COMPONENT_METHOD == null || value == null) return;
        try {
            ITEMSTACK_SET_COMPONENT_METHOD.invoke(stack, (DataComponentType)type, value);
        } catch (Throwable ignored) {}
    }

    private void removeComponent(ItemStack stack, DataComponentType<?> type) {
        if (ITEMSTACK_REMOVE_COMPONENT_METHOD == null || type == null) return;
        try {
            ITEMSTACK_REMOVE_COMPONENT_METHOD.invoke(stack, type);
        } catch (Throwable ignored) {}
    }

    private DataComponentType<?> component(String... names) {
        for (String name : names) {
            try {
                Field field = DataComponents.class.getField(name);
                Object value = field.get(null);
                if (value instanceof DataComponentType<?> type) return type;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private void setOptionalUnitComponent(ItemStack stack, boolean value, String componentName) {
        DataComponentType<?> type = component(componentName);
        if (type == null) return;
        if (value) setComponentUnchecked(stack, type, unitInstance());
        else removeComponent(stack, type);
    }

    private Object unitInstance() {
        try {
            return net.minecraft.util.Unit.INSTANCE;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object constructDamageResistant() {
        try {
            Class<?> cls = Class.forName("net.minecraft.world.item.component.DamageResistant");
            Object tag = Class.forName("net.minecraft.tags.DamageTypeTags").getField("IS_FIRE").get(null);
            for (Constructor<?> ctor : cls.getConstructors()) {
                if (ctor.getParameterCount() == 1 && ctor.getParameterTypes()[0].isAssignableFrom(tag.getClass())) {
                    return ctor.newInstance(tag);
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private Object constructCustomModelData(int value) {
        Object legacy = constructFirst("net.minecraft.world.item.component.CustomModelData", new Class<?>[]{int.class}, value);
        if (legacy != null) return legacy;
        return constructFirst("net.minecraft.world.item.component.CustomModelData",
                new Class<?>[]{List.class, List.class, List.class, List.class},
                List.of((float)value), List.of(), List.of(), List.of());
    }

    private Object constructFood(Object current, int nutrition, float saturation) {
        Object canAlwaysEat = invokeAny(current, "canAlwaysEat", "comp_2493");
        boolean eatAnytime = canAlwaysEat instanceof Boolean b && b;
        Object six = constructFirst("net.minecraft.world.food.FoodProperties",
                new Class<?>[]{int.class, float.class, boolean.class, float.class, Optional.class, List.class},
                nutrition, saturation, eatAnytime,
                numberOrDefault(invokeAny(current, "eatSeconds"), 1.6f),
                optionalOrEmpty(invokeAny(current, "usingConvertsTo")),
                listOrEmpty(invokeAny(current, "effects")));
        if (six != null) return six;
        return constructFirst("net.minecraft.world.food.FoodProperties",
                new Class<?>[]{int.class, float.class, boolean.class}, nutrition, saturation, eatAnytime);
    }

    private float numberOrDefault(Object value, float fallback) {
        return value instanceof Number number ? number.floatValue() : fallback;
    }

    private Optional<?> optionalOrEmpty(Object value) {
        return value instanceof Optional<?> optional ? optional : Optional.empty();
    }

    private List<?> listOrEmpty(Object value) {
        return value instanceof List<?> list ? list : List.of();
    }

    private Object constructItemAttributeModifiers(Class<?>[] types, Object... args) {
        try {
            Constructor<ItemAttributeModifiers> ctor = ItemAttributeModifiers.class.getConstructor(types);
            return ctor.newInstance(args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object constructFirst(String className, Class<?>[] parameterTypes, Object... args) {
        try {
            Class<?> cls = Class.forName(className);
            Constructor<?> ctor = cls.getConstructor(parameterTypes);
            return ctor.newInstance(args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object invokeAny(Object target, String... names) {
        if (target == null) return null;
        Set<String> allowed = new HashSet<>(Arrays.asList(names));
        for (Method method : target.getClass().getMethods()) {
            if (!allowed.contains(method.getName()) || method.getParameterCount() != 0) continue;
            try {
                return method.invoke(target);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private Object invokeNamed(Object target, String name, Object... args) {
        if (target == null) return null;
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            try {
                return method.invoke(target, args);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private boolean invokeGui(Object target, String name, Object... args) {
        return invokeGuiWithResult(target, name, args) != InvokeMiss.INSTANCE;
    }

    private Object invokeGuiWithResult(Object target, String name, Object... args) {
        if (target == null) return InvokeMiss.INSTANCE;
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            if (!parametersMatch(method.getParameterTypes(), args)) continue;
            try {
                Object out = method.invoke(target, args);
                return out == null ? Boolean.TRUE : out;
            } catch (Throwable ignored) {}
        }
        return InvokeMiss.INSTANCE;
    }

    private boolean parametersMatch(Class<?>[] types, Object[] args) {
        for (int i = 0; i < types.length; i++) {
            if (args[i] == null) continue;
            Class<?> type = wrap(types[i]);
            if (!type.isAssignableFrom(args[i].getClass())) return false;
        }
        return true;
    }

    private Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static Method findItemStackComponentMethod(String... names) {
        for (String name : names) {
            try {
                return ItemStack.class.getMethod(name, DataComponentType.class);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static Method findItemStackSetComponentMethod(String... names) {
        Set<String> nameSet = new HashSet<>(Arrays.asList(names));
        for (Method method : ItemStack.class.getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (!nameSet.contains(method.getName())) continue;
            if (parameterTypes.length != 2) continue;
            if (parameterTypes[0] != DataComponentType.class) continue;
            return method;
        }
        return null;
    }

    private enum InvokeMiss {
        INSTANCE
    }
}
