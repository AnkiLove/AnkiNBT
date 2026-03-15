package com.ankinbt.editor;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.util.DebugLog;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpawnEggEditorHelper {
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");

    private SpawnEggEditorHelper() {}

    public static boolean isSpawnEgg(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof SpawnEggItem;
    }

    public static boolean isVillagerSpawnEgg(ItemStack stack) {
        if (!isSpawnEgg(stack)) return false;
        String id = getItemId(stack);
        return id.contains("villager_spawn_egg") || id.contains("wandering_trader_spawn_egg");
    }

    public static Optional<CompoundTag> getEntityData(ItemStack stack) {
        Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(stack);
        if (fullOpt.isEmpty()) return Optional.empty();
        CompoundTag full = fullOpt.get();
        CompoundTag components = getChildCompound(full, "components");
        CompoundTag entityData = components == null ? null : getChildCompound(components, "minecraft:entity_data");
        if (entityData == null) entityData = getChildCompound(full, "EntityTag");
        if (entityData == null) return Optional.of(new CompoundTag());
        DebugLog.info("Read spawn egg entity data from item {}: {}", getItemId(stack), entityData);
        return Optional.of(copyCompound(entityData));
    }

    public static Optional<ItemStack> withMergedEntityData(ItemStack source, CompoundTag patch) {
        Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(source);
        if (fullOpt.isEmpty()) return Optional.empty();
        CompoundTag full = fullOpt.get();
        CompoundTag components = getChildCompound(full, "components");
        if (components == null) components = new CompoundTag();

        CompoundTag entityData = getChildCompound(components, "minecraft:entity_data");
        if (entityData == null) entityData = getChildCompound(full, "EntityTag");
        if (entityData == null) entityData = new CompoundTag();

        entityData.merge(patch);
        components.put("minecraft:entity_data", entityData);
        full.put("components", components);
        // Modern 1.21+ codec expects data components path; legacy EntityTag may break strict decoders.
        removeTag(full, "EntityTag");
        DebugLog.info("Write spawn egg entity data patch for {}: {}", getItemId(source), patch);

        Optional<ItemStack> modern = NbtHelper.deserializeItemStack(full);
        if (modern.isPresent()) return modern;

        // Fallback for edge runtimes that still rely on legacy tag path.
        CompoundTag legacyFull = copyCompound(full);
        legacyFull.put("EntityTag", copyCompound(entityData));
        Optional<ItemStack> legacy = NbtHelper.deserializeItemStack(legacyFull);
        if (legacy.isPresent()) return legacy;

        // Last fallback: keep only legacy EntityTag without components.
        CompoundTag legacyOnly = copyCompound(full);
        removeTag(legacyOnly, "components");
        legacyOnly.put("EntityTag", copyCompound(entityData));
        return NbtHelper.deserializeItemStack(legacyOnly);
    }

    private static CompoundTag getChildCompound(CompoundTag parent, String key) {
        if (parent == null || !parent.contains(key)) return null;
        Object tag = parent.get(key);
        if (tag instanceof Optional<?> opt) tag = opt.orElse(null);
        return tag instanceof CompoundTag ct ? ct : null;
    }

    private static CompoundTag copyCompound(CompoundTag source) {
        CompoundTag out = new CompoundTag();
        out.merge(source);
        return out;
    }

    private static void removeTag(CompoundTag parent, String key) {
        if (parent == null || key == null || key.isBlank()) return;
        try {
            parent.remove(key);
            return;
        } catch (Throwable ignored) {}
        try {
            parent.getClass().getMethod("remove", String.class).invoke(parent, key);
        } catch (Throwable ignored) {}
    }

    public static String getItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        try {
            Object key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key != null) {
                String norm = normalizeItemId(key.toString());
                if (!norm.isEmpty()) return norm;
            }
        } catch (Throwable ignored) {}
        try {
            Object holder = stack.getItem().builtInRegistryHolder();
            Object key = holder.getClass().getMethod("key").invoke(holder);
            try {
                Object location = key.getClass().getMethod("location").invoke(key);
                if (location != null) {
                    String norm = normalizeItemId(location.toString());
                    if (!norm.isEmpty()) return norm;
                }
            } catch (Throwable ignored) {}
            try {
                Object identifier = key.getClass().getMethod("identifier").invoke(key);
                if (identifier != null) {
                    String norm = normalizeItemId(identifier.toString());
                    if (!norm.isEmpty()) return norm;
                }
            } catch (Throwable ignored) {}
            if (key != null) return normalizeItemId(key.toString());
        } catch (Throwable ignored) {}
        return "";
    }

    private static String normalizeItemId(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";
        Matcher m = ITEM_ID_PATTERN.matcher(s.toLowerCase());
        if (m.find()) return m.group(1);
        return s.toLowerCase();
    }

    public static boolean saveToCreativeSlot(Minecraft mc, ItemStack stack, int inventorySlot) {
        if (mc == null || mc.player == null || mc.gameMode == null) return false;
        if (!mc.player.isCreative()) return false;

        if (inventorySlot >= 0) {
            mc.player.getInventory().setItem(inventorySlot, stack.copy());
            // Cross-version fallback: different versions/modloaders may expect different slot bases.
            mc.gameMode.handleCreativeModeItemAdd(stack.copy(), inventorySlot);
            if (inventorySlot < 9) mc.gameMode.handleCreativeModeItemAdd(stack.copy(), 36 + inventorySlot);
            DebugLog.info("Saved spawn egg into creative slot {}", inventorySlot);
            return true;
        }

        int selected = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
        mc.player.getInventory().setItem(selected, stack.copy());
        mc.gameMode.handleCreativeModeItemAdd(stack.copy(), selected);
        mc.gameMode.handleCreativeModeItemAdd(stack.copy(), 36 + selected);
        DebugLog.info("Saved spawn egg into selected slot {}", selected);
        return true;
    }

    public static String inferEntityIdFromSpawnEgg(ItemStack stack) {
        String itemId = getItemId(stack);
        if (itemId == null || itemId.isBlank()) return "";
        int idx = itemId.indexOf(':');
        if (idx < 0) return "";
        String ns = itemId.substring(0, idx);
        String path = itemId.substring(idx + 1);
        if (!path.endsWith("_spawn_egg")) return "";
        String entityPath = path.substring(0, path.length() - "_spawn_egg".length());
        if (entityPath.isBlank()) return "";
        return ns + ":" + entityPath;
    }
}
