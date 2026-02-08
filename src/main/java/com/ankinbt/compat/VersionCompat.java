package com.ankinbt.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Version compatibility layer.
 * Methods here abstract over API differences between MC 1.21.x versions.
 * For 1.21.1 (base version), these are the default implementations.
 * For 1.21.4+, override in version-specific source sets.
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

    // --- Registry access ---

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

    // --- Fire resistant ---

    public boolean isFireResistant(ItemStack stack) {
        return stack.has(DataComponents.FIRE_RESISTANT);
    }

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
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new net.minecraft.world.item.component.CustomModelData(value));
    }

    // --- Food ---

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
        if (food != null) {
            stack.set(DataComponents.FOOD, new net.minecraft.world.food.FoodProperties(
                    nutrition, food.saturation(), food.canAlwaysEat(),
                    food.eatSeconds(), food.usingConvertsTo(), food.effects()));
        }
    }

    public void setFoodSaturation(ItemStack stack, float saturation) {
        var food = stack.get(DataComponents.FOOD);
        if (food != null) {
            stack.set(DataComponents.FOOD, new net.minecraft.world.food.FoodProperties(
                    food.nutrition(), saturation, food.canAlwaysEat(),
                    food.eatSeconds(), food.usingConvertsTo(), food.effects()));
        }
    }
}
