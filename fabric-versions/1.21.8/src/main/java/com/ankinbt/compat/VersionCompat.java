/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_10215
 *  net.minecraft.class_1320
 *  net.minecraft.class_1661
 *  net.minecraft.class_1799
 *  net.minecraft.class_1887
 *  net.minecraft.class_2378
 *  net.minecraft.class_2481
 *  net.minecraft.class_2487
 *  net.minecraft.class_2489
 *  net.minecraft.class_2494
 *  net.minecraft.class_2497
 *  net.minecraft.class_2503
 *  net.minecraft.class_2516
 *  net.minecraft.class_2519
 *  net.minecraft.class_2520
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_3902
 *  net.minecraft.class_4174
 *  net.minecraft.class_6880$class_6883
 *  net.minecraft.class_7924
 *  net.minecraft.class_8103
 *  net.minecraft.class_9280
 *  net.minecraft.class_9282
 *  net.minecraft.class_9285
 *  net.minecraft.class_9285$class_9287
 *  net.minecraft.class_9300
 *  net.minecraft.class_9334
 */
package com.ankinbt.compat;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10215;
import net.minecraft.class_1320;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_2378;
import net.minecraft.class_2481;
import net.minecraft.class_2487;
import net.minecraft.class_2489;
import net.minecraft.class_2494;
import net.minecraft.class_2497;
import net.minecraft.class_2503;
import net.minecraft.class_2516;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_3902;
import net.minecraft.class_4174;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import net.minecraft.class_8103;
import net.minecraft.class_9280;
import net.minecraft.class_9282;
import net.minecraft.class_9285;
import net.minecraft.class_9300;
import net.minecraft.class_9334;

public class VersionCompat {
    private static VersionCompat INSTANCE;

    public static VersionCompat get() {
        if (INSTANCE == null) {
            INSTANCE = new VersionCompat();
        }
        return INSTANCE;
    }

    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    public String getKeyDisplayName(int keyCode) {
        if (keyCode == 44) {
            return ",";
        }
        if (keyCode >= 65 && keyCode <= 90) {
            return Character.toString((char)(65 + (keyCode - 65)));
        }
        if (keyCode >= 48 && keyCode <= 57) {
            return Character.toString((char)(48 + (keyCode - 48)));
        }
        try {
            String name = class_3675.class_307.field_1668.method_1447(keyCode).method_27445().getString();
            if (name != null && !name.isBlank() && !name.startsWith("#")) {
                return name;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "KEY(" + keyCode + ")";
    }

    public List<String> getAllEnchantIds() {
        ArrayList<String> ids = new ArrayList<String>();
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 != null) {
            class_2378 reg = mc.field_1687.method_30349().method_30530(class_7924.field_41265);
            reg.method_46754().forEach(key -> ids.add(key.method_29177().toString()));
        }
        return ids;
    }

    public List<String> getAllAttributeIds() {
        ArrayList<String> ids = new ArrayList<String>();
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 != null) {
            class_2378 reg = mc.field_1687.method_30349().method_30530(class_7924.field_41251);
            reg.method_46754().forEach(key -> ids.add(key.method_29177().toString()));
        }
        return ids;
    }

    public Optional<class_6880.class_6883<class_1887>> getEnchantHolder(String id) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) {
            return Optional.empty();
        }
        class_2378 reg = mc.field_1687.method_30349().method_30530(class_7924.field_41265);
        class_2960 loc = class_2960.method_12829((String)id);
        if (loc == null) {
            return Optional.empty();
        }
        return reg.method_10223(loc);
    }

    public Optional<class_6880.class_6883<class_1320>> getAttributeHolder(String id) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) {
            return Optional.empty();
        }
        class_2378 reg = mc.field_1687.method_30349().method_30530(class_7924.field_41251);
        class_2960 loc = class_2960.method_12829((String)id);
        if (loc == null) {
            return Optional.empty();
        }
        return reg.method_10223(loc);
    }

    public boolean isFireResistant(class_1799 stack) {
        return stack.method_57826(class_9334.field_54273);
    }

    public void setFireResistant(class_1799 stack, boolean value) {
        if (value) {
            stack.method_57379(class_9334.field_54273, (Object)new class_10215(class_8103.field_42246));
        } else {
            stack.method_57381(class_9334.field_54273);
        }
    }

    public int getCustomModelData(class_1799 stack) {
        class_9280 cmd = (class_9280)stack.method_57824(class_9334.field_49637);
        return cmd != null ? cmd.comp_2382() : 0;
    }

    public void setCustomModelData(class_1799 stack, int value) {
        stack.method_57379(class_9334.field_49637, (Object)new class_9280(value));
    }

    public boolean hasFood(class_1799 stack) {
        return stack.method_57824(class_9334.field_50075) != null;
    }

    public int getFoodNutrition(class_1799 stack) {
        class_4174 food = (class_4174)stack.method_57824(class_9334.field_50075);
        return food != null ? food.comp_2491() : 0;
    }

    public float getFoodSaturation(class_1799 stack) {
        class_4174 food = (class_4174)stack.method_57824(class_9334.field_50075);
        return food != null ? food.comp_2492() : 0.0f;
    }

    public void setFoodNutrition(class_1799 stack, int nutrition) {
        class_4174 food = (class_4174)stack.method_57824(class_9334.field_50075);
        if (food != null) {
            stack.method_57379(class_9334.field_50075, (Object)new class_4174(nutrition, food.comp_2492(), food.comp_2493()));
        }
    }

    public void setFoodSaturation(class_1799 stack, float saturation) {
        class_4174 food = (class_4174)stack.method_57824(class_9334.field_50075);
        if (food != null) {
            stack.method_57379(class_9334.field_50075, (Object)new class_4174(food.comp_2491(), saturation, food.comp_2493()));
        }
    }

    public Set<String> getCompoundKeys(class_2487 tag) {
        return tag.method_10541();
    }

    public String getTagAsString(class_2520 tag) {
        return tag.method_10714();
    }

    public byte getByteValue(class_2481 tag) {
        return tag.method_10698();
    }

    public short getShortValue(class_2516 tag) {
        return tag.method_10696();
    }

    public int getIntValue(class_2497 tag) {
        return tag.method_10701();
    }

    public long getLongValue(class_2503 tag) {
        return tag.method_10699();
    }

    public float getFloatValue(class_2494 tag) {
        return tag.method_10700();
    }

    public double getDoubleValue(class_2489 tag) {
        return tag.method_10697();
    }

    public String getStringValue(class_2519 tag) {
        return tag.method_10714();
    }

    public String compoundGetString(class_2487 tag, String key) {
        return tag.method_10558(key);
    }

    public int compoundGetInt(class_2487 tag, String key) {
        return tag.method_10550(key);
    }

    public int getSelectedSlot(class_1661 inv) {
        return inv.field_7545;
    }

    public boolean isHideTooltip(class_1799 stack) {
        return stack.method_57826(class_9334.field_50074);
    }

    public void setHideTooltip(class_1799 stack, boolean value) {
        if (value) {
            stack.method_57379(class_9334.field_50074, (Object)class_3902.field_17274);
        } else {
            stack.method_57381(class_9334.field_50074);
        }
    }

    public boolean isHideAdditional(class_1799 stack) {
        return stack.method_57826(class_9334.field_49638);
    }

    public void setHideAdditional(class_1799 stack, boolean value) {
        if (value) {
            stack.method_57379(class_9334.field_49638, (Object)class_3902.field_17274);
        } else {
            stack.method_57381(class_9334.field_49638);
        }
    }

    public boolean hasHideTooltipFeature() {
        return true;
    }

    public boolean hasHideAdditionalFeature() {
        return true;
    }

    public void setUnbreakable(class_1799 stack, boolean value) {
        if (value) {
            stack.method_57379(class_9334.field_49630, (Object)new class_9300(true));
        } else {
            stack.method_57381(class_9334.field_49630);
        }
    }

    public void setDyedColor(class_1799 stack, int rgb) {
        stack.method_57379(class_9334.field_49644, (Object)new class_9282(rgb, true));
    }

    public class_9285 withEntries(List<class_9285.class_9287> entries, class_9285 old) {
        return new class_9285(entries, old.comp_2394());
    }

    public void renderTooltip(class_332 g, class_327 f, class_2561 tooltip, int mx, int my) {
        g.method_51438(f, tooltip, mx, my);
    }

    public int drawString(class_332 g, class_327 font, class_2561 text, int x, int y, int color, boolean shadow) {
        if (text == null) {
            return this.drawString(g, font, "", x, y, color, shadow);
        }
        try {
            Method m = class_332.class.getMethod("drawString", class_327.class, class_2561.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
            Object out = m.invoke((Object)g, font, text, x, y, color, shadow);
            if (out instanceof Number) {
                Number n = (Number)out;
                return n.intValue();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return this.drawString(g, font, text.getString(), x, y, color, shadow);
    }

    public int drawString(class_332 g, class_327 font, String text, int x, int y, int color, boolean shadow) {
        String resolved = text == null ? "" : text;
        try {
            Method m = class_332.class.getMethod("drawString", class_327.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
            Object out = m.invoke((Object)g, font, resolved, x, y, color, shadow);
            if (out instanceof Number) {
                Number n = (Number)out;
                return n.intValue();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        g.method_51433(font, resolved, x, y, color, shadow);
        return font.method_1727(resolved);
    }
}

