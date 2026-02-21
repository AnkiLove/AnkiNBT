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
 * VersionCompat for MC 1.21.1
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
        return com.mojang.blaze3d.platform.InputConstants.getKey(keyCode, -1).getDisplayName().getString();
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
    public boolean isFireResistant(ItemStack stack) { return stack.has(DataComponents.FIRE_RESISTANT); }
    public void setFireResistant(ItemStack stack, boolean value) {
        if (value) stack.set(DataComponents.FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE);
        else stack.remove(DataComponents.FIRE_RESISTANT);
    }

    // --- Custom model data ---
    public int getCustomModelData(ItemStack stack) {
        var cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return cmd != null ? cmd.value() : 0;
    }
    public void setCustomModelData(ItemStack stack, int value) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(value));
    }

    // --- Food ---
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
                nutrition, food.saturation(), food.canAlwaysEat(), food.eatSeconds(), food.usingConvertsTo(), food.effects()));
    }
    public void setFoodSaturation(ItemStack stack, float saturation) {
        var food = stack.get(DataComponents.FOOD);
        if (food != null) stack.set(DataComponents.FOOD, new net.minecraft.world.food.FoodProperties(
                food.nutrition(), saturation, food.canAlwaysEat(), food.eatSeconds(), food.usingConvertsTo(), food.effects()));
    }

    // --- NBT accessors (1.21.1: standard methods) ---
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

    // --- Unbreakable ---
    public void setUnbreakable(ItemStack stack, boolean value) {
        if (value) stack.set(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true));
        else stack.remove(DataComponents.UNBREAKABLE);
    }

    // --- DyedItemColor ---
    public void setDyedColor(ItemStack stack, int rgb) {
        stack.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(rgb, true));
    }

    // --- AttributeModifiers ---
    public ItemAttributeModifiers withEntries(List<ItemAttributeModifiers.Entry> entries, ItemAttributeModifiers old) {
        return new ItemAttributeModifiers(entries, old.showInTooltip());
    }

    // --- Tooltip rendering ---
    public void renderTooltip(net.minecraft.client.gui.GuiGraphics g, net.minecraft.client.gui.Font f, Component tooltip, int mx, int my) {
        g.renderTooltip(f, tooltip, mx, my);
    }
}
