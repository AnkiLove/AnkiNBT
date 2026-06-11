/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.ankinbt.util;

import java.util.Locale;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class ItemEditorVisuals {
    private ItemEditorVisuals() {
    }

    public static ItemStack enchantIconStack(String enchantId) {
        String id = ItemEditorVisuals.normalizeRegistryPath(enchantId);
        if (id.contains("protection") || id.contains("respiration") || id.contains("aqua_affinity") || id.contains("thorns") || id.contains("frost_walker") || id.contains("soul_speed") || id.contains("swift_sneak")) {
            return new ItemStack((ItemLike)Items.DIAMOND_CHESTPLATE);
        }
        if (id.contains("sharpness") || id.contains("smite") || id.contains("bane_of_arthropods") || id.contains("knockback") || id.contains("fire_aspect") || id.contains("looting") || id.contains("sweeping") || id.contains("density") || id.contains("breach") || id.contains("wind_burst")) {
            return new ItemStack((ItemLike)Items.DIAMOND_SWORD);
        }
        if (id.contains("efficiency") || id.contains("silk_touch") || id.contains("fortune")) {
            return new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);
        }
        if (id.contains("unbreaking") || id.contains("mending")) {
            return new ItemStack((ItemLike)Items.ANVIL);
        }
        if (id.contains("power") || id.contains("punch") || id.contains("flame") || id.contains("infinity")) {
            return new ItemStack((ItemLike)Items.BOW);
        }
        if (id.contains("multishot") || id.contains("quick_charge") || id.contains("piercing")) {
            return new ItemStack((ItemLike)Items.CROSSBOW);
        }
        if (id.contains("loyalty") || id.contains("impaling") || id.contains("riptide") || id.contains("channeling")) {
            return new ItemStack((ItemLike)Items.TRIDENT);
        }
        if (id.contains("luck_of_the_sea") || id.contains("lure")) {
            return new ItemStack((ItemLike)Items.FISHING_ROD);
        }
        if (id.contains("curse")) {
            return new ItemStack((ItemLike)Items.CRYING_OBSIDIAN);
        }
        return new ItemStack((ItemLike)Items.ENCHANTED_BOOK);
    }

    public static ItemStack attributeIconStack(String attrId) {
        String id = ItemEditorVisuals.normalizeRegistryPath(attrId);
        if (id.contains("health") || id.contains("absorption")) {
            return new ItemStack((ItemLike)Items.GOLDEN_APPLE);
        }
        if (id.contains("armor")) {
            return new ItemStack((ItemLike)Items.DIAMOND_CHESTPLATE);
        }
        if (id.contains("attack_damage") || id.contains("sweeping_damage")) {
            return new ItemStack((ItemLike)Items.DIAMOND_SWORD);
        }
        if (id.contains("attack_speed") || id.contains("movement_speed") || id.contains("flying_speed") || id.contains("sneaking_speed") || id.contains("movement_efficiency")) {
            return new ItemStack((ItemLike)Items.FEATHER);
        }
        if (id.contains("knockback")) {
            return new ItemStack((ItemLike)Items.SHIELD);
        }
        if (id.contains("luck")) {
            return new ItemStack((ItemLike)Items.RABBIT_FOOT);
        }
        if (id.contains("block") || id.contains("mining")) {
            return new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);
        }
        if (id.contains("entity_interaction") || id.contains("scale") || id.contains("range")) {
            return new ItemStack((ItemLike)Items.SPYGLASS);
        }
        if (id.contains("gravity") || id.contains("fall") || id.contains("jump")) {
            return new ItemStack((ItemLike)Items.SLIME_BALL);
        }
        if (id.contains("oxygen") || id.contains("water") || id.contains("submerged")) {
            return new ItemStack((ItemLike)Items.HEART_OF_THE_SEA);
        }
        return new ItemStack((ItemLike)Items.PAPER);
    }

    public static ItemStack potionRowIcon(String stackPath) {
        String id = ItemEditorVisuals.normalizeRegistryPath(stackPath);
        if (id.contains("splash_potion")) {
            return new ItemStack((ItemLike)Items.SPLASH_POTION);
        }
        if (id.contains("lingering_potion")) {
            return new ItemStack((ItemLike)Items.LINGERING_POTION);
        }
        if (id.contains("tipped_arrow")) {
            return new ItemStack((ItemLike)Items.TIPPED_ARROW);
        }
        return new ItemStack((ItemLike)Items.POTION);
    }

    public static ItemStack effectIconStack(String effectId) {
        String id = ItemEditorVisuals.normalizeRegistryPath(effectId);
        if (id.contains("haste")) {
            return new ItemStack((ItemLike)Items.GOLDEN_PICKAXE);
        }
        if (id.contains("fatigue")) {
            return new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);
        }
        if (id.contains("speed") || id.contains("jump")) {
            return new ItemStack((ItemLike)Items.SUGAR);
        }
        if (id.contains("slowness")) {
            return new ItemStack((ItemLike)Items.COBWEB);
        }
        if (id.contains("strength")) {
            return new ItemStack((ItemLike)Items.DIAMOND_SWORD);
        }
        if (id.contains("damage")) {
            return new ItemStack((ItemLike)Items.BLAZE_POWDER);
        }
        if (id.contains("health") || id.contains("regeneration") || id.contains("absorption")) {
            return new ItemStack((ItemLike)Items.GLISTERING_MELON_SLICE);
        }
        if (id.contains("poison")) {
            return new ItemStack((ItemLike)Items.SPIDER_EYE);
        }
        if (id.contains("wither")) {
            return new ItemStack((ItemLike)Items.WITHER_ROSE);
        }
        if (id.contains("fire")) {
            return new ItemStack((ItemLike)Items.MAGMA_CREAM);
        }
        if (id.contains("water") || id.contains("dolphins") || id.contains("conduit")) {
            return new ItemStack((ItemLike)Items.HEART_OF_THE_SEA);
        }
        if (id.contains("night_vision") || id.contains("glowing")) {
            return new ItemStack((ItemLike)Items.GLOW_BERRIES);
        }
        if (id.contains("invisibility")) {
            return new ItemStack((ItemLike)Items.GLASS_BOTTLE);
        }
        if (id.contains("luck")) {
            return new ItemStack((ItemLike)Items.RABBIT_FOOT);
        }
        if (id.contains("weakness")) {
            return new ItemStack((ItemLike)Items.FERMENTED_SPIDER_EYE);
        }
        if (id.contains("darkness") || id.contains("blindness")) {
            return new ItemStack((ItemLike)Items.INK_SAC);
        }
        if (id.contains("oozing")) {
            return new ItemStack((ItemLike)Items.SLIME_BALL);
        }
        if (id.contains("infested")) {
            return new ItemStack((ItemLike)Items.STONE);
        }
        if (id.contains("weaving")) {
            return new ItemStack((ItemLike)Items.STRING);
        }
        if (id.contains("wind")) {
            return new ItemStack((ItemLike)Items.WIND_CHARGE);
        }
        return new ItemStack((ItemLike)Items.POTION);
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

