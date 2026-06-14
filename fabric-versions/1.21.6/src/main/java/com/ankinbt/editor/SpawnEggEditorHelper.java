/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_1826
 *  net.minecraft.class_2487
 *  net.minecraft.class_2520
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_6880$class_6883
 *  net.minecraft.class_7923
 */
package com.ankinbt.editor;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.nbt.NbtHelper;
import com.ankinbt.util.DebugLog;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1799;
import net.minecraft.class_1826;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_6880;
import net.minecraft.class_7923;

public final class SpawnEggEditorHelper {
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");

    private SpawnEggEditorHelper() {
    }

    public static boolean isSpawnEgg(class_1799 stack) {
        return !stack.method_7960() && stack.method_7909() instanceof class_1826;
    }

    public static boolean isVillagerSpawnEgg(class_1799 stack) {
        if (!SpawnEggEditorHelper.isSpawnEgg(stack)) {
            return false;
        }
        String id = SpawnEggEditorHelper.getItemId(stack);
        return id.contains("villager_spawn_egg") || id.contains("wandering_trader_spawn_egg");
    }

    public static Optional<class_2487> getEntityData(class_1799 stack) {
        class_2487 entityData;
        Optional<class_2487> fullOpt = NbtHelper.serializeItemStack(stack);
        if (fullOpt.isEmpty()) {
            return Optional.empty();
        }
        class_2487 full = fullOpt.get();
        class_2487 components = SpawnEggEditorHelper.getChildCompound(full, "components");
        class_2487 class_24872 = entityData = components == null ? null : SpawnEggEditorHelper.getChildCompound(components, "minecraft:entity_data");
        if (entityData == null) {
            entityData = SpawnEggEditorHelper.getChildCompound(full, "EntityTag");
        }
        if (entityData == null) {
            return Optional.of(new class_2487());
        }
        DebugLog.info("Read spawn egg entity data from item {}: {}", SpawnEggEditorHelper.getItemId(stack), entityData);
        return Optional.of(SpawnEggEditorHelper.copyCompound(entityData));
    }

    public static Optional<class_1799> withMergedEntityData(class_1799 source, class_2487 patch) {
        class_2487 entityData;
        Optional<class_2487> fullOpt = NbtHelper.serializeItemStack(source);
        if (fullOpt.isEmpty()) {
            return Optional.empty();
        }
        class_2487 full = fullOpt.get();
        class_2487 components = SpawnEggEditorHelper.getChildCompound(full, "components");
        if (components == null) {
            components = new class_2487();
        }
        if ((entityData = SpawnEggEditorHelper.getChildCompound(components, "minecraft:entity_data")) == null) {
            entityData = SpawnEggEditorHelper.getChildCompound(full, "EntityTag");
        }
        if (entityData == null) {
            entityData = new class_2487();
        }
        entityData.method_10543(patch);
        components.method_10566("minecraft:entity_data", (class_2520)entityData);
        full.method_10566("components", (class_2520)components);
        SpawnEggEditorHelper.removeTag(full, "EntityTag");
        DebugLog.info("Write spawn egg entity data patch for {}: {}", SpawnEggEditorHelper.getItemId(source), patch);
        Optional<class_1799> modern = NbtHelper.deserializeItemStack(full);
        if (modern.isPresent()) {
            return modern;
        }
        class_2487 legacyFull = SpawnEggEditorHelper.copyCompound(full);
        legacyFull.method_10566("EntityTag", (class_2520)SpawnEggEditorHelper.copyCompound(entityData));
        Optional<class_1799> legacy = NbtHelper.deserializeItemStack(legacyFull);
        if (legacy.isPresent()) {
            return legacy;
        }
        class_2487 legacyOnly = SpawnEggEditorHelper.copyCompound(full);
        SpawnEggEditorHelper.removeTag(legacyOnly, "components");
        legacyOnly.method_10566("EntityTag", (class_2520)SpawnEggEditorHelper.copyCompound(entityData));
        return NbtHelper.deserializeItemStack(legacyOnly);
    }

    private static class_2487 getChildCompound(class_2487 parent, String key) {
        class_2487 ct;
        if (parent == null || !parent.method_10545(key)) {
            return null;
        }
        class_2520 tag = parent.method_10580(key);
        if (tag instanceof Optional) {
            Optional opt = (Optional)tag;
            tag = opt.orElse(null);
        }
        return tag instanceof class_2487 ? (ct = (class_2487)tag) : null;
    }

    private static class_2487 copyCompound(class_2487 source) {
        class_2487 out = new class_2487();
        out.method_10543(source);
        return out;
    }

    private static void removeTag(class_2487 parent, String key) {
        if (parent == null || key == null || key.isBlank()) {
            return;
        }
        try {
            parent.method_10551(key);
            return;
        }
        catch (Throwable throwable) {
            try {
                parent.getClass().getMethod("remove", String.class).invoke((Object)parent, key);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
            return;
        }
    }

    public static String getItemId(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return "";
        }
        try {
            String norm;
            class_2960 key = class_7923.field_41178.method_10221((Object)stack.method_7909());
            if (key != null && !(norm = SpawnEggEditorHelper.normalizeItemId(key.toString())).isEmpty()) {
                return norm;
            }
        }
        catch (Throwable key) {
            // empty catch block
        }
        try {
            String norm;
            class_6880.class_6883 holder = stack.method_7909().method_40131();
            Object key = holder.getClass().getMethod("key", new Class[0]).invoke((Object)holder, new Object[0]);
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

    public static boolean saveToCreativeSlot(class_310 mc, class_1799 stack, int inventorySlot) {
        if (mc == null || mc.field_1724 == null || mc.field_1761 == null) {
            return false;
        }
        if (!mc.field_1724.method_7337()) {
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
                mc.field_1724.method_31548().method_5447(playerSlot, stack.method_7972());
            }
            mc.field_1761.method_2909(stack.method_7972(), creativeSlot);
            DebugLog.info("Saved spawn egg into creative slot {}", creativeSlot);
            return true;
        }
        int selected = VersionCompat.get().getSelectedSlot(mc.field_1724.method_31548());
        mc.field_1724.method_31548().method_5447(selected, stack.method_7972());
        mc.field_1761.method_2909(stack.method_7972(), 36 + selected);
        DebugLog.info("Saved spawn egg into selected slot {}", selected);
        return true;
    }

    public static String inferEntityIdFromSpawnEgg(class_1799 stack) {
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

