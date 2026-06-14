/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_6880
 *  net.minecraft.class_6880$class_6883
 */
package com.ankinbt.util;

import com.ankinbt.config.AnkiConfig;
import java.lang.reflect.Method;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_6880;

public final class UiSound {
    private UiSound() {
    }

    public static void playClick() {
        UiSound.playClick(1.0f);
    }

    public static void playClick(float pitch) {
        float volume = AnkiConfig.getUiSoundVolume();
        if (volume <= 0.001f) {
            return;
        }
        try {
            class_310 mc = class_310.method_1551();
            if (mc == null) {
                return;
            }
            if (UiSound.playViaOfficialApi(mc, volume, pitch)) {
                return;
            }
            class_6880.class_6883 uiClick = class_3417.field_15015;
            Object value = UiSound.invokeAny(uiClick, "value");
            if (mc.field_1724 != null && UiSound.playViaPlayer(mc.field_1724, uiClick, value, volume, pitch)) {
                return;
            }
            UiSound.playViaSoundManager(mc, uiClick, value, volume, pitch);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static boolean playViaOfficialApi(class_310 mc, float volume, float pitch) {
        if (mc.method_1483() == null) {
            return false;
        }
        try {
            mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)((class_3414)class_3417.field_15015.comp_349()), (float)volume, (float)pitch));
            return true;
        }
        catch (Throwable throwable) {
            try {
                mc.method_1483().method_4873((class_1113)class_1109.method_47978((class_6880)class_3417.field_15015, (float)pitch));
                return true;
            }
            catch (Throwable throwable2) {
                return false;
            }
        }
    }

    private static boolean playViaPlayer(Object player, Object uiClick, Object value, float volume, float pitch) {
        for (Method m : player.getClass().getMethods()) {
            Class<?>[] p;
            if (!"playSound".equals(m.getName()) || m.getParameterCount() != 3 || (p = m.getParameterTypes())[1] != Float.TYPE || p[2] != Float.TYPE) continue;
            try {
                if (uiClick != null && p[0].isInstance(uiClick)) {
                    m.invoke(player, uiClick, Float.valueOf(volume), Float.valueOf(pitch));
                    return true;
                }
                if (value == null || !p[0].isInstance(value)) continue;
                m.invoke(player, value, Float.valueOf(volume), Float.valueOf(pitch));
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    private static void playViaSoundManager(class_310 mc, Object uiClick, Object value, float volume, float pitch) {
        Object manager = UiSound.invokeAny(mc, "getSoundManager");
        if (manager == null) {
            return;
        }
        Object soundInst = null;
        try {
            Class<?> simpleClass = Class.forName("net.minecraft.client.resources.sounds.SimpleSoundInstance");
            Method[] methodArray = simpleClass.getMethods();
            int n = methodArray.length;
            for (int i = 0; i < n; ++i) {
                Method m = methodArray[i];
                if (!"forUI".equals(m.getName())) continue;
                Class<?>[] p = m.getParameterTypes();
                try {
                    if (p.length == 3) {
                        if (uiClick != null && p[0].isInstance(uiClick) && p[1] == Float.TYPE && p[2] == Float.TYPE) {
                            soundInst = m.invoke(null, uiClick, Float.valueOf(volume), Float.valueOf(pitch));
                        } else {
                            if (value == null || !p[0].isInstance(value) || p[1] != Float.TYPE || p[2] != Float.TYPE) continue;
                            soundInst = m.invoke(null, value, Float.valueOf(volume), Float.valueOf(pitch));
                        }
                    } else {
                        if (p.length != 2 || p[1] != Float.TYPE) continue;
                        if (uiClick != null && p[0].isInstance(uiClick)) {
                            soundInst = m.invoke(null, uiClick, Float.valueOf(pitch));
                        } else {
                            if (value == null || !p[0].isInstance(value)) continue;
                            soundInst = m.invoke(null, value, Float.valueOf(pitch));
                        }
                    }
                    break;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (soundInst == null) {
            return;
        }
        for (Method m : manager.getClass().getMethods()) {
            if (!"play".equals(m.getName()) || m.getParameterCount() != 1 || !m.getParameterTypes()[0].isInstance(soundInst)) continue;
            try {
                m.invoke(manager, soundInst);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return;
        }
    }

    private static Object invokeAny(Object target, String ... methods) {
        if (target == null || methods == null) {
            return null;
        }
        for (String method : methods) {
            try {
                return target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
            }
            catch (Throwable throwable) {
            }
        }
        return null;
    }
}

