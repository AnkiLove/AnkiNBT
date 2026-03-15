package com.ankinbt.compat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Shared compatibility layer for mixed 1.21.x APIs.
 * The root source set uses reflection where mappings differ between patch versions.
 */
public class VersionCompat {

    private static VersionCompat INSTANCE;

    public static VersionCompat get() {
        if (INSTANCE == null) INSTANCE = new VersionCompat();
        return INSTANCE;
    }

    public static void setInstance(VersionCompat compat) {
        INSTANCE = compat;
    }

    public Path getConfigDir() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config");
    }

    public Path getGameDir() {
        return Minecraft.getInstance().gameDirectory.toPath();
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

    private String keyName(InputConstants.Key key) {
        try {
            if (key == null) return "";
            return key.getDisplayName().getString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private boolean isReadableKeyName(String keyName) {
        return keyName != null && !keyName.isBlank() && !keyName.startsWith("#");
    }

    public Registry<Enchantment> getEnchantmentRegistry() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        return mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
    }

    public Registry<Attribute> getAttributeRegistry() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        return mc.level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
    }

    public List<String> getAllEnchantIds() {
        List<String> ids = new ArrayList<>();
        Registry<Enchantment> reg = getEnchantmentRegistry();
        if (reg != null) {
            reg.holders().forEach(h -> ids.add(h.key().location().toString()));
        }
        return ids;
    }

    public List<String> getAllAttributeIds() {
        List<String> ids = new ArrayList<>();
        Registry<Attribute> reg = getAttributeRegistry();
        if (reg != null) {
            reg.holders().forEach(h -> ids.add(h.key().location().toString()));
        }
        return ids;
    }

    public Optional<Holder.Reference<Enchantment>> getEnchantHolder(String id) {
        Registry<Enchantment> reg = getEnchantmentRegistry();
        if (reg == null) return Optional.empty();
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) return Optional.empty();
        return reg.getHolder(loc);
    }

    public Optional<Holder.Reference<Attribute>> getAttributeHolder(String id) {
        Registry<Attribute> reg = getAttributeRegistry();
        if (reg == null) return Optional.empty();
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) return Optional.empty();
        return reg.getHolder(loc);
    }

    public boolean isFireResistant(ItemStack stack) {
        try {
            return stack.has(DataComponents.FIRE_RESISTANT);
        } catch (Throwable ignored) {}
        DataComponentType<?> type = findComponent("DAMAGE_RESISTANT");
        return type != null && stack.has(type);
    }

    public void setFireResistant(ItemStack stack, boolean value) {
        try {
            if (value) stack.set(DataComponents.FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE);
            else stack.remove(DataComponents.FIRE_RESISTANT);
            return;
        } catch (Throwable ignored) {}
        DataComponentType<?> type = findComponent("DAMAGE_RESISTANT");
        if (type == null) return;
        if (!value) {
            stack.remove(type);
            return;
        }
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.item.component.DamageResistant");
            Object obj = clazz.getConstructor(Class.forName("net.minecraft.tags.TagKey"))
                    .newInstance(net.minecraft.tags.DamageTypeTags.IS_FIRE);
            setComponentRaw(stack, type, obj);
        } catch (Throwable ignored) {}
    }

    public int getCustomModelData(ItemStack stack) {
        try {
            var cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (cmd == null) return 0;
            Object floats = cmd.getClass().getMethod("floats").invoke(cmd);
            if (floats instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Number n) {
                return n.intValue();
            }
            return 0;
        } catch (Throwable ignored) {}

        try {
            Object cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (cmd == null) return 0;
            Object v = cmd.getClass().getMethod("value").invoke(cmd);
            if (v instanceof Number n) return n.intValue();
        } catch (Throwable ignored) {}
        return 0;
    }

    public void setCustomModelData(ItemStack stack, int value) {
        try {
            stack.set(DataComponents.CUSTOM_MODEL_DATA,
                    new net.minecraft.world.item.component.CustomModelData(value));
            return;
        } catch (Throwable ignored) {}
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.item.component.CustomModelData");
            Constructor<?> c = clazz.getConstructor(List.class, List.class, List.class, List.class);
            Object cmd = c.newInstance(List.of((float) value), List.of(), List.of(), List.of());
            setComponentRaw(stack, DataComponents.CUSTOM_MODEL_DATA, cmd);
        } catch (Throwable ignored) {}
    }

    public boolean hasFood(ItemStack stack) {
        return stack.get(DataComponents.FOOD) != null;
    }

    public int getFoodNutrition(ItemStack stack) {
        var food = stack.get(DataComponents.FOOD);
        return food != null ? food.nutrition() : 0;
    }

    public float getFoodSaturation(ItemStack stack) {
        var food = stack.get(DataComponents.FOOD);
        return food != null ? food.saturation() : 0f;
    }

    public void setFoodNutrition(ItemStack stack, int nutrition) {
        var food = stack.get(DataComponents.FOOD);
        if (food == null) return;
        try {
            stack.set(DataComponents.FOOD, new net.minecraft.world.food.FoodProperties(
                    nutrition, food.saturation(), food.canAlwaysEat(),
                    food.eatSeconds(), food.usingConvertsTo(), food.effects()));
        } catch (Throwable ignored) {
            try {
                Class<?> clazz = Class.forName("net.minecraft.world.food.FoodProperties");
                Constructor<?> c = clazz.getConstructor(int.class, float.class, boolean.class);
                Object fp = c.newInstance(nutrition, food.saturation(), food.canAlwaysEat());
                setComponentRaw(stack, DataComponents.FOOD, fp);
            } catch (Throwable ignored2) {}
        }
    }

    public void setFoodSaturation(ItemStack stack, float saturation) {
        var food = stack.get(DataComponents.FOOD);
        if (food == null) return;
        try {
            stack.set(DataComponents.FOOD, new net.minecraft.world.food.FoodProperties(
                    food.nutrition(), saturation, food.canAlwaysEat(),
                    food.eatSeconds(), food.usingConvertsTo(), food.effects()));
        } catch (Throwable ignored) {
            try {
                Class<?> clazz = Class.forName("net.minecraft.world.food.FoodProperties");
                Constructor<?> c = clazz.getConstructor(int.class, float.class, boolean.class);
                Object fp = c.newInstance(food.nutrition(), saturation, food.canAlwaysEat());
                setComponentRaw(stack, DataComponents.FOOD, fp);
            } catch (Throwable ignored2) {}
        }
    }

    public Set<String> getCompoundKeys(CompoundTag tag) {
        try {
            @SuppressWarnings("unchecked")
            Set<String> keys = (Set<String>) tag.getClass().getMethod("keySet").invoke(tag);
            return keys;
        } catch (Throwable ignored) {}
        try {
            @SuppressWarnings("unchecked")
            Set<String> keys = (Set<String>) tag.getClass().getMethod("getAllKeys").invoke(tag);
            return keys;
        } catch (Throwable ignored) {}
        return new LinkedHashSet<>();
    }

    public String getTagAsString(Tag tag) {
        return tag == null ? "" : tag.toString();
    }

    public byte getByteValue(ByteTag tag) {
        try {
            return (byte) tag.getClass().getMethod("value").invoke(tag);
        } catch (Throwable ignored) {
            return tag.getAsByte();
        }
    }

    public short getShortValue(ShortTag tag) {
        try {
            return (short) tag.getClass().getMethod("value").invoke(tag);
        } catch (Throwable ignored) {
            return tag.getAsShort();
        }
    }

    public int getIntValue(IntTag tag) {
        try {
            return (int) tag.getClass().getMethod("value").invoke(tag);
        } catch (Throwable ignored) {
            return tag.getAsInt();
        }
    }

    public long getLongValue(LongTag tag) {
        try {
            return (long) tag.getClass().getMethod("value").invoke(tag);
        } catch (Throwable ignored) {
            return tag.getAsLong();
        }
    }

    public float getFloatValue(FloatTag tag) {
        try {
            return (float) tag.getClass().getMethod("value").invoke(tag);
        } catch (Throwable ignored) {
            return tag.getAsFloat();
        }
    }

    public double getDoubleValue(DoubleTag tag) {
        try {
            return (double) tag.getClass().getMethod("value").invoke(tag);
        } catch (Throwable ignored) {
            return tag.getAsDouble();
        }
    }

    public String getStringValue(StringTag tag) {
        try {
            Object out = tag.getClass().getMethod("value").invoke(tag);
            return out instanceof String s ? s : tag.getAsString();
        } catch (Throwable ignored) {
            return tag.getAsString();
        }
    }

    public String compoundGetString(CompoundTag tag, String key) {
        try {
            Object out = tag.getClass().getMethod("getString", String.class).invoke(tag, key);
            if (out instanceof String s) return s;
            if (out instanceof Optional<?> opt && opt.orElse(null) instanceof String s) return s;
        } catch (Throwable ignored) {}
        try {
            Tag raw = tag.get(key);
            return raw == null ? "" : raw.getAsString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    public int compoundGetInt(CompoundTag tag, String key) {
        try {
            Object out = tag.getClass().getMethod("getInt", String.class).invoke(tag, key);
            if (out instanceof Integer i) return i;
            if (out instanceof Optional<?> opt && opt.orElse(null) instanceof Integer i) return i;
        } catch (Throwable ignored) {}
        try {
            Tag raw = tag.get(key);
            if (raw instanceof net.minecraft.nbt.NumericTag num) return num.getAsInt();
        } catch (Throwable ignored) {}
        return 0;
    }

    public int getSelectedSlot(net.minecraft.world.entity.player.Inventory inv) {
        try {
            Object out = inv.getClass().getMethod("getSelectedSlot").invoke(inv);
            if (out instanceof Integer i) return i;
        } catch (Throwable ignored) {}
        try {
            return inv.selected;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public boolean isHideTooltip(ItemStack stack) {
        DataComponentType<?> type = findComponent("HIDE_TOOLTIP");
        return type != null && stack.has(type);
    }

    public void setHideTooltip(ItemStack stack, boolean value) {
        DataComponentType<?> type = findComponent("HIDE_TOOLTIP");
        if (type == null) return;
        if (value) setComponentRaw(stack, type, net.minecraft.util.Unit.INSTANCE);
        else stack.remove(type);
    }

    public boolean isHideAdditional(ItemStack stack) {
        DataComponentType<?> type = findComponent("HIDE_ADDITIONAL_TOOLTIP");
        return type != null && stack.has(type);
    }

    public void setHideAdditional(ItemStack stack, boolean value) {
        DataComponentType<?> type = findComponent("HIDE_ADDITIONAL_TOOLTIP");
        if (type == null) return;
        if (value) setComponentRaw(stack, type, net.minecraft.util.Unit.INSTANCE);
        else stack.remove(type);
    }

    public boolean hasHideTooltipFeature() {
        return findComponent("HIDE_TOOLTIP") != null;
    }

    public boolean hasHideAdditionalFeature() {
        return findComponent("HIDE_ADDITIONAL_TOOLTIP") != null;
    }

    public void setUnbreakable(ItemStack stack, boolean value) {
        if (!value) {
            stack.remove(DataComponents.UNBREAKABLE);
            return;
        }
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.item.component.Unbreakable");
            Constructor<?> c = clazz.getConstructors()[0];
            Object obj = c.getParameterCount() == 0 ? c.newInstance() : c.newInstance(true);
            setComponentRaw(stack, DataComponents.UNBREAKABLE, obj);
        } catch (Throwable ignored) {}
    }

    public void setDyedColor(ItemStack stack, int rgb) {
        try {
            stack.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(rgb, true));
            return;
        } catch (Throwable ignored) {}

        try {
            Class<?> clazz = Class.forName("net.minecraft.world.item.component.DyedItemColor");
            Constructor<?> c1 = clazz.getConstructor(int.class);
            Object obj = c1.newInstance(rgb);
            setComponentRaw(stack, DataComponents.DYED_COLOR, obj);
        } catch (Throwable ignored) {}
    }

    public ItemAttributeModifiers withEntries(List<ItemAttributeModifiers.Entry> entries, ItemAttributeModifiers old) {
        try {
            boolean show = true;
            if (old != null) {
                Method m = old.getClass().getMethod("showInTooltip");
                Object out = m.invoke(old);
                if (out instanceof Boolean b) show = b;
            }
            return new ItemAttributeModifiers(entries, show);
        } catch (Throwable ignored) {}
        try {
            Constructor<ItemAttributeModifiers> c = ItemAttributeModifiers.class.getConstructor(List.class);
            return c.newInstance(entries);
        } catch (Throwable ignored) {
            return old;
        }
    }

    public void renderTooltip(GuiGraphics g, Font f, Component tooltip, int mx, int my) {
        try {
            g.renderTooltip(f, tooltip, mx, my);
            return;
        } catch (Throwable ignored) {}
        try {
            Method m = GuiGraphics.class.getMethod("renderTooltip", Font.class, Component.class, int.class, int.class);
            m.invoke(g, f, tooltip, mx, my);
        } catch (Throwable ignored) {}
    }

    public int drawString(GuiGraphics g, Font font, Component text, int x, int y, int color, boolean shadow) {
        if (text == null) return drawString(g, font, "", x, y, color, shadow);
        try {
            Method m = GuiGraphics.class.getMethod("drawString",
                    Font.class, Component.class, int.class, int.class, int.class, boolean.class);
            Object out = m.invoke(g, font, text, x, y, color, shadow);
            if (out instanceof Number n) return n.intValue();
        } catch (Throwable ignored) {}
        return drawString(g, font, text.getString(), x, y, color, shadow);
    }

    public int drawString(GuiGraphics g, Font font, String text, int x, int y, int color, boolean shadow) {
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

    private DataComponentType<?> findComponent(String fieldName) {
        try {
            Object out = DataComponents.class.getField(fieldName).get(null);
            if (out instanceof DataComponentType<?> t) return t;
        } catch (Throwable ignored) {}
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setComponentRaw(ItemStack stack, DataComponentType<?> type, Object value) {
        if (type == null) return;
        stack.set((DataComponentType) type, value);
    }
}
