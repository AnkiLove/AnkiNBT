package com.ankinbt.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import java.util.*;

/**
 * VersionCompat for MC 1.21.2
 * Same API as 1.21.3: DAMAGE_RESISTANT, int-based CustomModelData, simplified FoodProperties
 */
public class VersionCompat {

    private static VersionCompat INSTANCE;
    public static VersionCompat get() {
        if (INSTANCE == null) INSTANCE = new VersionCompat();
        return INSTANCE;
    }

    // --- Platform paths ---
    public java.nio.file.Path getConfigDir() {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
    }
    public java.nio.file.Path getGameDir() {
        return net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
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
    public boolean isFireResistant(ItemStack stack) { return stack.has(DataComponents.DAMAGE_RESISTANT); }
    public void setFireResistant(ItemStack stack, boolean value) {
        if (value) stack.set(DataComponents.DAMAGE_RESISTANT,
                new net.minecraft.world.item.component.DamageResistant(net.minecraft.tags.DamageTypeTags.IS_FIRE));
        else stack.remove(DataComponents.DAMAGE_RESISTANT);
    }

    public int getCustomModelData(ItemStack stack) {
        var cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return cmd != null ? cmd.value() : 0;
    }
    public void setCustomModelData(ItemStack stack, int value) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(value));
    }

    public boolean hasFood(ItemStack stack) { return stack.get(DataComponents.FOOD) != null; }
    public int getFoodNutrition(ItemStack stack) {
        var food = stack.get(DataComponents.FOOD); return food != null ? food.nutrition() : 0;
    }
    public float getFoodSaturation(ItemStack stack) {
        var food = stack.get(DataComponents.FOOD); return food != null ? food.saturation() : 0f;
    }
    public void setFoodNutrition(ItemStack stack, int nutrition) {
        var food = stack.get(DataComponents.FOOD);
        if (food != null) stack.set(DataComponents.FOOD, new net.minecraft.world.food.FoodProperties(
                nutrition, food.saturation(), food.canAlwaysEat()));
    }
    public void setFoodSaturation(ItemStack stack, float saturation) {
        var food = stack.get(DataComponents.FOOD);
        if (food != null) stack.set(DataComponents.FOOD, new net.minecraft.world.food.FoodProperties(
                food.nutrition(), saturation, food.canAlwaysEat()));
    }

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

    public int getSelectedSlot(net.minecraft.world.entity.player.Inventory inv) { return inv.selected; }

    public boolean isHideTooltip(ItemStack stack) { return stack.has(DataComponents.HIDE_TOOLTIP); }
    public void setHideTooltip(ItemStack stack, boolean value) {
        if (value) stack.set(DataComponents.HIDE_TOOLTIP, net.minecraft.util.Unit.INSTANCE);
        else stack.remove(DataComponents.HIDE_TOOLTIP);
    }
    public boolean isHideAdditional(ItemStack stack) { return stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP); }
    public void setHideAdditional(ItemStack stack, boolean value) {
        if (value) stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, net.minecraft.util.Unit.INSTANCE);
        else stack.remove(DataComponents.HIDE_ADDITIONAL_TOOLTIP);
    }
    public boolean hasHideTooltipFeature() { return true; }
    public boolean hasHideAdditionalFeature() { return true; }

    public void setUnbreakable(ItemStack stack, boolean value) {
        stack.remove(DataComponents.UNBREAKABLE);
        if (value) stack.set(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true));
    }

    public void sanitizeForCreativeSave(ItemStack stack) {
        if (stack != null && stack.has(DataComponents.UNBREAKABLE)) {
            setUnbreakable(stack, true);
        }
    }

    public void setDyedColor(ItemStack stack, int rgb) {
        stack.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(rgb, true));
    }

    public ItemAttributeModifiers withEntries(List<ItemAttributeModifiers.Entry> entries, ItemAttributeModifiers old) {
        return new ItemAttributeModifiers(entries, old.showInTooltip());
    }

    public void renderTooltip(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font f, Component tooltip, int mx, int my) {
        g.renderTooltip(f, tooltip, mx, my);
    }
    public int drawString(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font font, net.minecraft.network.chat.Component text, int x, int y, int color, boolean shadow) {
        if (text == null) return drawString(g, font, "", x, y, color, shadow);
        try {
            java.lang.reflect.Method m = net.minecraft.client.gui.GuiGraphics.class.getMethod("drawString",
                    net.minecraft.client.gui.Font.class, net.minecraft.network.chat.Component.class, int.class, int.class, int.class, boolean.class);
            Object out = m.invoke(g, font, text, x, y, color, shadow);
            if (out instanceof Number n) return n.intValue();
        } catch (Throwable ignored) {}
        return drawString(g, font, text.getString(), x, y, color, shadow);
    }

    public int drawString(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font font, String text, int x, int y, int color, boolean shadow) {
        String resolved = text == null ? "" : text;
        try {
            java.lang.reflect.Method m = net.minecraft.client.gui.GuiGraphics.class.getMethod("drawString",
                    net.minecraft.client.gui.Font.class, String.class, int.class, int.class, int.class, boolean.class);
            Object out = m.invoke(g, font, resolved, x, y, color, shadow);
            if (out instanceof Number n) return n.intValue();
        } catch (Throwable ignored) {}
        g.drawString(font, resolved, x, y, color, shadow);
        return font.width(resolved);
    }
    private List<String> getAllRegistryIds(Object registryKey) {
        ArrayList<String> ids = new ArrayList<>();
        Object registry = getRegistry(registryKey);
        if (registry == null) return ids;
        Object holders = invokeAny(registry, "holders");
        if (holders instanceof java.util.stream.Stream<?> stream) {
            stream.forEach(holder -> {
                Object key = invokeAny(holder, "key", "unwrapKey");
                if (key instanceof Optional<?> optional) key = optional.orElse(null);
                addId(ids, idFromKey(key));
            });
        }
        Object elementIds = invokeAny(registry, "listElementIds");
        if (elementIds instanceof java.util.stream.Stream<?> stream) {
            stream.forEach(key -> addId(ids, idFromKey(key)));
        } else if (elementIds instanceof Iterable<?> iterable) {
            for (Object key : iterable) addId(ids, idFromKey(key));
        }
        Object keySet = invokeAny(registry, "keySet");
        if (keySet instanceof Iterable<?> iterable) {
            for (Object key : iterable) addId(ids, idFromKey(key));
        }
        return ids;
    }

    private Optional<?> getHolder(Object registryKey, String id) {
        Object registry = getRegistry(registryKey);
        Object location = parseResourceId(id);
        if (registry == null || location == null) return Optional.empty();
        Optional<?> holder = toHolderOptional(invokeRegistryLookup(registry, location,
                "getHolder", "getEntry", "method_55841", "method_10223"));
        if (holder.isEmpty()) {
            holder = toHolderOptional(invokeRegistryLookup(registry, createElementKey(registryKey, location),
                    "getHolder", "getEntry", "method_40264", "method_57095", "method_10223"));
        }
        if (holder.isEmpty()) {
            Object value = invokeRegistryLookup(registry, location, "get", "getValue", "method_10223");
            holder = toHolderOptional(invokeRegistryLookup(registry, value,
                    "wrapAsHolder", "getEntry", "method_47983"));
        }
        return holder;
    }

    private Optional<?> toHolderOptional(Object value) {
        if (value instanceof Optional<?> optional) {
            if (optional.isEmpty()) return Optional.empty();
            Object unwrapped = optional.get();
            return isHolder(unwrapped) ? optional : Optional.empty();
        }
        return isHolder(value) ? Optional.of(value) : Optional.empty();
    }

    private boolean isHolder(Object value) {
        if (value == null) return false;
        if (value instanceof Holder<?>) return true;
        String name = value.getClass().getName();
        return name.contains("Holder") || name.contains("class_6880");
    }
    private Object getRegistry(Object registryKey) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        Object access = mc.level.registryAccess();
        return invokeRegistryLookup(access, registryKey, "registryOrThrow", "lookupOrThrow");
    }

    private Object invokeRegistryLookup(Object target, Object argument, String... names) {
        if (target == null || argument == null) return null;
        Set<String> allowed = new HashSet<>(Arrays.asList(names));
        for (java.lang.reflect.Method method : target.getClass().getMethods()) {
            if (!allowed.contains(method.getName()) || method.getParameterCount() != 1) continue;
            Class<?> parameter = method.getParameterTypes()[0];
            if (!parameter.isAssignableFrom(argument.getClass())) continue;
            try {
                return method.invoke(target, argument);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private Object invokeAny(Object target, String... names) {
        if (target == null) return null;
        Set<String> allowed = new HashSet<>(Arrays.asList(names));
        for (java.lang.reflect.Method method : target.getClass().getMethods()) {
            if (!allowed.contains(method.getName()) || method.getParameterCount() != 0) continue;
            try {
                return method.invoke(target);
            } catch (Throwable ignored) {}
        }
        return null;
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
                    java.lang.reflect.Method method = cls.getMethod(methodName, String.class);
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
            for (java.lang.reflect.Method method : cls.getMethods()) {
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
    private String idFromKey(Object key) {
        if (key == null) return "";
        Object id = invokeAny(key, "location");
        return id != null ? String.valueOf(id) : String.valueOf(key);
    }

    private void addId(List<String> ids, String id) {
        if (id != null && !id.isBlank() && !ids.contains(id)) ids.add(id);
    }
}
