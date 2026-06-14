/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 */
package com.ankinbt.util;

import java.util.Locale;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;

public final class ItemEditorVisuals {
    private ItemEditorVisuals() {
    }

    public static class_1799 enchantIconStack(String enchantId) {
        String id = ItemEditorVisuals.normalizeRegistryPath(enchantId);
        if (id.contains("protection") || id.contains("respiration") || id.contains("aqua_affinity") || id.contains("thorns") || id.contains("frost_walker") || id.contains("soul_speed") || id.contains("swift_sneak")) {
            return new class_1799((class_1935)class_1802.field_8058);
        }
        if (id.contains("sharpness") || id.contains("smite") || id.contains("bane_of_arthropods") || id.contains("knockback") || id.contains("fire_aspect") || id.contains("looting") || id.contains("sweeping") || id.contains("density") || id.contains("breach") || id.contains("wind_burst")) {
            return new class_1799((class_1935)class_1802.field_8802);
        }
        if (id.contains("efficiency") || id.contains("silk_touch") || id.contains("fortune")) {
            return new class_1799((class_1935)class_1802.field_8377);
        }
        if (id.contains("unbreaking") || id.contains("mending")) {
            return new class_1799((class_1935)class_1802.field_8782);
        }
        if (id.contains("power") || id.contains("punch") || id.contains("flame") || id.contains("infinity")) {
            return new class_1799((class_1935)class_1802.field_8102);
        }
        if (id.contains("multishot") || id.contains("quick_charge") || id.contains("piercing")) {
            return new class_1799((class_1935)class_1802.field_8399);
        }
        if (id.contains("loyalty") || id.contains("impaling") || id.contains("riptide") || id.contains("channeling")) {
            return new class_1799((class_1935)class_1802.field_8547);
        }
        if (id.contains("luck_of_the_sea") || id.contains("lure")) {
            return new class_1799((class_1935)class_1802.field_8378);
        }
        if (id.contains("curse")) {
            return new class_1799((class_1935)class_1802.field_22421);
        }
        return new class_1799((class_1935)class_1802.field_8598);
    }

    public static class_1799 attributeIconStack(String attrId) {
        String id = ItemEditorVisuals.normalizeRegistryPath(attrId);
        if (id.contains("health") || id.contains("absorption")) {
            return new class_1799((class_1935)class_1802.field_8463);
        }
        if (id.contains("armor")) {
            return new class_1799((class_1935)class_1802.field_8058);
        }
        if (id.contains("attack_damage") || id.contains("sweeping_damage")) {
            return new class_1799((class_1935)class_1802.field_8802);
        }
        if (id.contains("attack_speed") || id.contains("movement_speed") || id.contains("flying_speed") || id.contains("sneaking_speed") || id.contains("movement_efficiency")) {
            return new class_1799((class_1935)class_1802.field_8153);
        }
        if (id.contains("knockback")) {
            return new class_1799((class_1935)class_1802.field_8255);
        }
        if (id.contains("luck")) {
            return new class_1799((class_1935)class_1802.field_8073);
        }
        if (id.contains("block") || id.contains("mining")) {
            return new class_1799((class_1935)class_1802.field_8377);
        }
        if (id.contains("entity_interaction") || id.contains("scale") || id.contains("range")) {
            return new class_1799((class_1935)class_1802.field_27070);
        }
        if (id.contains("gravity") || id.contains("fall") || id.contains("jump")) {
            return new class_1799((class_1935)class_1802.field_8777);
        }
        if (id.contains("oxygen") || id.contains("water") || id.contains("submerged")) {
            return new class_1799((class_1935)class_1802.field_8207);
        }
        return new class_1799((class_1935)class_1802.field_8407);
    }

    public static class_1799 potionRowIcon(String stackPath) {
        String id = ItemEditorVisuals.normalizeRegistryPath(stackPath);
        if (id.contains("splash_potion")) {
            return new class_1799((class_1935)class_1802.field_8436);
        }
        if (id.contains("lingering_potion")) {
            return new class_1799((class_1935)class_1802.field_8150);
        }
        if (id.contains("tipped_arrow")) {
            return new class_1799((class_1935)class_1802.field_8087);
        }
        return new class_1799((class_1935)class_1802.field_8574);
    }

    public static int effectAccentColor(String effectId) {
        String id = ItemEditorVisuals.normalizeRegistryPath(effectId);
        if (id.contains("heal") || id.contains("regeneration") || id.contains("health")) {
            return 16281969;
        }
        if (id.contains("speed") || id.contains("haste") || id.contains("jump")) {
            return 6333946;
        }
        if (id.contains("strength") || id.contains("damage") || id.contains("fire")) {
            return 16486972;
        }
        if (id.contains("poison") || id.contains("wither") || id.contains("hunger") || id.contains("weakness")) {
            return 10741301;
        }
        if (id.contains("night_vision") || id.contains("invisibility") || id.contains("glowing")) {
            return 12616956;
        }
        if (id.contains("water") || id.contains("dolphins")) {
            return 2282478;
        }
        return 9741240;
    }

    public static String compactRegistryPath(String id) {
        String path = ItemEditorVisuals.normalizeRegistryPath(id);
        return path.isBlank() ? "" : path.replace('_', ' ');
    }

    public static String normalizeRegistryPath(String id) {
        String value = id == null ? "" : id;
        int colon = value.indexOf(58);
        if (colon >= 0 && colon + 1 < value.length()) {
            value = value.substring(colon + 1);
        }
        return value.toLowerCase(Locale.ROOT);
    }
}

