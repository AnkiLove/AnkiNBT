/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SpawnEggItem
 */
package com.ankinbt.editor;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.util.DebugLog;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public final class SpawnEggEditorHelper {
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");

    private SpawnEggEditorHelper() {
    }

    public static boolean isSpawnEgg(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof SpawnEggItem;
    }

    public static boolean isVillagerSpawnEgg(ItemStack stack) {
        if (!SpawnEggEditorHelper.isSpawnEgg(stack)) {
            return false;
        }
        String id = SpawnEggEditorHelper.getItemId(stack);
        return id.contains("villager_spawn_egg") || id.contains("wandering_trader_spawn_egg");
    }

    public static Optional<CompoundTag> getEntityData(ItemStack stack) {
        CompoundTag entityData;
        Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(stack);
        if (fullOpt.isEmpty()) {
            return Optional.empty();
        }
        CompoundTag full = fullOpt.get();
        CompoundTag components = SpawnEggEditorHelper.getChildCompound(full, "components");
        CompoundTag compoundTag = entityData = components == null ? null : SpawnEggEditorHelper.getChildCompound(components, "minecraft:entity_data");
        if (entityData == null) {
            entityData = SpawnEggEditorHelper.getChildCompound(full, "EntityTag");
        }
        if (entityData == null) {
            return Optional.of(new CompoundTag());
        }
        DebugLog.info("Read spawn egg entity data from item {}: {}", SpawnEggEditorHelper.getItemId(stack), entityData);
        return Optional.of(SpawnEggEditorHelper.copyCompound(entityData));
    }

    public static Optional<ItemStack> withMergedEntityData(ItemStack source, CompoundTag patch) {
        CompoundTag entityData;
        Optional<CompoundTag> fullOpt = NbtHelper.serializeItemStack(source);
        if (fullOpt.isEmpty()) {
            return Optional.empty();
        }
        CompoundTag full = fullOpt.get();
        CompoundTag components = SpawnEggEditorHelper.getChildCompound(full, "components");
        if (components == null) {
            components = new CompoundTag();
        }
        if ((entityData = SpawnEggEditorHelper.getChildCompound(components, "minecraft:entity_data")) == null) {
            entityData = SpawnEggEditorHelper.getChildCompound(full, "EntityTag");
        }
        if (entityData == null) {
            entityData = new CompoundTag();
        }
        entityData.merge(patch);
        components.put("minecraft:entity_data", (Tag)entityData);
        full.put("components", (Tag)components);
        SpawnEggEditorHelper.removeTag(full, "EntityTag");
        DebugLog.info("Write spawn egg entity data patch for {}: {}", SpawnEggEditorHelper.getItemId(source), patch);
        Optional<ItemStack> modern = NbtHelper.deserializeItemStack(full);
        if (modern.isPresent()) {
            return modern;
        }
        CompoundTag legacyFull = SpawnEggEditorHelper.copyCompound(full);
        legacyFull.put("EntityTag", (Tag)SpawnEggEditorHelper.copyCompound(entityData));
        Optional<ItemStack> legacy = NbtHelper.deserializeItemStack(legacyFull);
        if (legacy.isPresent()) {
            return legacy;
        }
        CompoundTag legacyOnly = SpawnEggEditorHelper.copyCompound(full);
        SpawnEggEditorHelper.removeTag(legacyOnly, "components");
        legacyOnly.put("EntityTag", (Tag)SpawnEggEditorHelper.copyCompound(entityData));
        return NbtHelper.deserializeItemStack(legacyOnly);
    }

    private static CompoundTag getChildCompound(CompoundTag parent, String key) {
        CompoundTag ct;
        if (parent == null || !parent.contains(key)) {
            return null;
        }
        Tag tag = parent.get(key);
        Object rawTag = tag;
        if (rawTag instanceof Optional) {
            tag = (Tag)((Optional)rawTag).orElse(null);
        }
        return tag instanceof CompoundTag ? (ct = (CompoundTag)tag) : null;
    }

    private static CompoundTag copyCompound(CompoundTag source) {
        CompoundTag out = new CompoundTag();
        out.merge(source);
        return out;
    }

    private static void removeTag(CompoundTag parent, String key) {
        if (parent == null || key == null || key.isBlank()) {
            return;
        }
        try {
            parent.remove(key);
            return;
        }
        catch (Throwable throwable) {
            try {
                parent.getClass().getMethod("remove", String.class).invoke(parent, key);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
            return;
        }
    }

    public static String getItemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        try {
            String norm;
            Object key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key != null && !(norm = SpawnEggEditorHelper.normalizeItemId(key.toString())).isEmpty()) {
                return norm;
            }
        }
        catch (Throwable key) {
            // empty catch block
        }
        try {
            String norm;
            Holder.Reference holder = stack.getItem().builtInRegistryHolder();
            Object key = holder.getClass().getMethod("key", new Class[0]).invoke(holder, new Object[0]);
            try {
                Object location = key.getClass().getMethod("location", new Class[0]).invoke(key, new Object[0]);
                if (location != null && !(norm = SpawnEggEditorHelper.normalizeItemId(location.toString())).isEmpty()) {
                    return norm;
                }
            }
            catch (Throwable location) {
                // empty catch block
            }
            try {
                Object identifier = key.getClass().getMethod("identifier", new Class[0]).invoke(key, new Object[0]);
                if (identifier != null && !(norm = SpawnEggEditorHelper.normalizeItemId(identifier.toString())).isEmpty()) {
                    return norm;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (key != null) {
                return SpawnEggEditorHelper.normalizeItemId(key.toString());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "";
    }

    private static String normalizeItemId(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return "";
        }
        Matcher m = ITEM_ID_PATTERN.matcher(s.toLowerCase());
        if (m.find()) {
            return m.group(1);
        }
        return s.toLowerCase();
    }

    private static int playerInventoryIndexFromCreativeSlot(int creativeSlot) {
        if (creativeSlot >= 36 && creativeSlot < 45) {
            return creativeSlot - 36;
        }
        if (creativeSlot >= 9 && creativeSlot < 36) {
            return creativeSlot;
        }
        return -1;
    }
    private static int creativePacketSlotFromEditedSlot(int editedSlot) {
        if (editedSlot >= 36 && editedSlot < 45) {
            return editedSlot;
        }
        if (editedSlot >= 0 && editedSlot < 9) {
            return 36 + editedSlot;
        }
        if (editedSlot >= 9 && editedSlot < 36) {
            return editedSlot;
        }
        return -1;
    }

    public static boolean saveToCreativeSlot(Minecraft mc, ItemStack stack, int inventorySlot) {
        if (mc == null || mc.player == null || mc.gameMode == null) {
            return false;
        }
        if (!mc.player.hasInfiniteMaterials()) {
            return false;
        }
        if (inventorySlot >= 0) {
            int creativeSlot = SpawnEggEditorHelper.creativePacketSlotFromEditedSlot(inventorySlot);
            if (creativeSlot < 0) {
                DebugLog.info("Skipped creative save for invalid slot {}", inventorySlot);
                return false;
            }
            int playerSlot = SpawnEggEditorHelper.playerInventoryIndexFromCreativeSlot(creativeSlot);
            if (playerSlot >= 0) {
                mc.player.getInventory().setItem(playerSlot, stack.copy());
            }
            mc.gameMode.handleCreativeModeItemAdd(stack.copy(), creativeSlot);
            DebugLog.info("Saved spawn egg into creative slot {}", creativeSlot);
            return true;
        }
        int selected = VersionCompat.get().getSelectedSlot(mc.player.getInventory());
        mc.player.getInventory().setItem(selected, stack.copy());
        mc.gameMode.handleCreativeModeItemAdd(stack.copy(), 36 + selected);
        DebugLog.info("Saved spawn egg into selected slot {}", selected);
        return true;
    }

    public static String inferEntityIdFromSpawnEgg(ItemStack stack) {
        String itemId = SpawnEggEditorHelper.getItemId(stack);
        if (itemId == null || itemId.isBlank()) {
            return "";
        }
        int idx = itemId.indexOf(58);
        if (idx < 0) {
            return "";
        }
        String ns = itemId.substring(0, idx);
        String path = itemId.substring(idx + 1);
        if (!path.endsWith("_spawn_egg")) {
            return "";
        }
        String entityPath = path.substring(0, path.length() - "_spawn_egg".length());
        if (entityPath.isBlank()) {
            return "";
        }
        return ns + ":" + entityPath;
    }
}
