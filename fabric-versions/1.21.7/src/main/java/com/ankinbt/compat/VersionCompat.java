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
 * VersionCompat for Fabric MC 1.21.7
 * Same as 1.21.6: tooltip rendering uses ClientTextTooltip
 */
public class VersionCompat {

    private static VersionCompat INSTANCE;
    public static VersionCompat get() {
        if (INSTANCE == null) INSTANCE = new VersionCompat();
        return INSTANCE;
    }

    // --- Registry ---
    public List<String> getAllEnchantIds() {
        List<String> ids = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Registry<Enchantment> reg = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            reg.listElementIds().forEach(key -> ids.add(key.location().toString()));
        }
        return ids;
    }
    public List<String> getAllAttributeIds() {
        List<String> ids = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Registry<Attribute> reg = mc.level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            reg.listElementIds().forEach(key -> ids.add(key.location().toString()));
        }
        return ids;
    }
    public Optional<Holder.Reference<Enchantment>> getEnchantHolder(String id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Optional.empty();
        Registry<Enchantment> reg = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) return Optional.empty();
        return reg.get(loc);
    }
    public Optional<Holder.Reference<Attribute>> getAttributeHolder(String id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Optional.empty();
        Registry<Attribute> reg = mc.level.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) return Optional.empty();
        return reg.get(loc);
    }

    // --- Fire resistant (DAMAGE_RESISTANT) ---
    public boolean isFireResistant(ItemStack stack) { return stack.has(DataComponents.DAMAGE_RESISTANT); }
    public void setFireResistant(ItemStack stack, boolean value) {
        if (value) stack.set(DataComponents.DAMAGE_RESISTANT,
                new net.minecraft.world.item.component.DamageResistant(net.minecraft.tags.DamageTypeTags.IS_FIRE));
        else stack.remove(DataComponents.DAMAGE_RESISTANT);
    }

    // --- Custom model data (multi-value) ---
    public int getCustomModelData(ItemStack stack) {
        var cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null) return 0;
        var floats = cmd.floats();
        return (floats != null && !floats.isEmpty()) ? floats.get(0).intValue() : 0;
    }
    public void setCustomModelData(ItemStack stack, int value) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new net.minecraft.world.item.component.CustomModelData(
                        List.of((float) value), List.of(), List.of(), List.of()));
    }

    // --- Food (3 params) ---
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

    // --- NBT accessors (1.21.5+: methods renamed) ---
    public Set<String> getCompoundKeys(CompoundTag tag) { return tag.keySet(); }
    public String getTagAsString(Tag tag) { return tag.toString(); }
    public byte getByteValue(ByteTag tag) { return tag.value(); }
    public short getShortValue(ShortTag tag) { return tag.value(); }
    public int getIntValue(IntTag tag) { return tag.value(); }
    public long getLongValue(LongTag tag) { return tag.value(); }
    public float getFloatValue(FloatTag tag) { return tag.value(); }
    public double getDoubleValue(DoubleTag tag) { return tag.value(); }
    public String getStringValue(StringTag tag) { return tag.value(); }
    public String compoundGetString(CompoundTag tag, String key) { return tag.getString(key).orElse(""); }
    public int compoundGetInt(CompoundTag tag, String key) { return tag.getInt(key).orElse(0); }

    // --- Inventory ---
    public int getSelectedSlot(net.minecraft.world.entity.player.Inventory inv) { return inv.getSelectedSlot(); }

    // --- Hide tooltip (removed in 1.21.5) ---
    public boolean isHideTooltip(ItemStack stack) { return false; }
    public void setHideTooltip(ItemStack stack, boolean value) { /* no-op */ }
    public boolean isHideAdditional(ItemStack stack) { return false; }
    public void setHideAdditional(ItemStack stack, boolean value) { /* no-op */ }
    public boolean hasHideTooltipFeature() { return false; }
    public boolean hasHideAdditionalFeature() { return false; }

    // --- Unbreakable ---
    public void setUnbreakable(ItemStack stack, boolean value) {
        if (value) stack.set(DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);
        else stack.remove(DataComponents.UNBREAKABLE);
    }

    // --- DyedItemColor (1.21.5+: single int param) ---
    public void setDyedColor(ItemStack stack, int rgb) {
        stack.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(rgb));
    }

    // --- AttributeModifiers (1.21.5+: no showInTooltip) ---
    public ItemAttributeModifiers withEntries(List<ItemAttributeModifiers.Entry> entries, ItemAttributeModifiers old) {
        return new ItemAttributeModifiers(entries);
    }

    // --- Tooltip rendering (1.21.6+: uses ClientTextTooltip) ---
    public void renderTooltip(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font f, Component tooltip, int mx, int my) {
        var lines = f.split(tooltip, 200);
        var components = new java.util.ArrayList<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent>();
        for (var line : lines) {
            components.add(new net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip(line));
        }
        g.renderTooltip(f, components, mx, my, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null);
    }
}