package com.ankinbt.util;

import com.ankinbt.config.AnkiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.lang.reflect.Method;

public final class UiSound {
    private UiSound() {}

    public static void playClick() {
        playClick(1.0f);
    }

    public static void playClick(float pitch) {
        float volume = AnkiConfig.getUiSoundVolume();
        if (volume <= 0.001f) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return;

            if (playViaOfficialApi(mc, volume, pitch)) return;
            Object uiClick = SoundEvents.UI_BUTTON_CLICK;
            Object value = invokeAny(uiClick, "value");
            if (mc.player != null && playViaPlayer(mc.player, uiClick, value, volume, pitch)) return;
            playViaSoundManager(mc, uiClick, value, volume, pitch);
        } catch (Throwable ignored) {}
    }

    private static boolean playViaOfficialApi(Minecraft mc, float volume, float pitch) {
        if (mc.getSoundManager() == null) return false;
        try {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), volume, pitch));
            return true;
        } catch (Throwable ignored) {}
        try {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
            return true;
        } catch (Throwable ignored) {}
        return false;
    }

    private static boolean playViaPlayer(Object player, Object uiClick, Object value, float volume, float pitch) {
        for (Method m : player.getClass().getMethods()) {
            if (!"playSound".equals(m.getName()) || m.getParameterCount() != 3) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p[1] != float.class || p[2] != float.class) continue;
            try {
                if (uiClick != null && p[0].isInstance(uiClick)) {
                    m.invoke(player, uiClick, volume, pitch);
                    return true;
                }
                if (value != null && p[0].isInstance(value)) {
                    m.invoke(player, value, volume, pitch);
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    private static void playViaSoundManager(Minecraft mc, Object uiClick, Object value, float volume, float pitch) {
        Object manager = invokeAny(mc, "getSoundManager");
        if (manager == null) return;

        Object soundInst = null;
        try {
            Class<?> simpleClass = Class.forName("net.minecraft.client.resources.sounds.SimpleSoundInstance");
            for (Method m : simpleClass.getMethods()) {
                if (!"forUI".equals(m.getName())) continue;
                Class<?>[] p = m.getParameterTypes();
                try {
                    if (p.length == 3) {
                        if (uiClick != null && p[0].isInstance(uiClick) && p[1] == float.class && p[2] == float.class) {
                            soundInst = m.invoke(null, uiClick, volume, pitch);
                            break;
                        }
                        if (value != null && p[0].isInstance(value) && p[1] == float.class && p[2] == float.class) {
                            soundInst = m.invoke(null, value, volume, pitch);
                            break;
                        }
                    } else if (p.length == 2 && p[1] == float.class) {
                        if (uiClick != null && p[0].isInstance(uiClick)) {
                            soundInst = m.invoke(null, uiClick, pitch);
                            break;
                        }
                        if (value != null && p[0].isInstance(value)) {
                            soundInst = m.invoke(null, value, pitch);
                            break;
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        if (soundInst == null) return;

        for (Method m : manager.getClass().getMethods()) {
            if (!"play".equals(m.getName()) || m.getParameterCount() != 1) continue;
            if (m.getParameterTypes()[0].isInstance(soundInst)) {
                try {
                    m.invoke(manager, soundInst);
                } catch (Throwable ignored) {}
                return;
            }
        }
    }

    private static Object invokeAny(Object target, String... methods) {
        if (target == null || methods == null) return null;
        for (String method : methods) {
            try {
                return target.getClass().getMethod(method).invoke(target);
            } catch (Throwable ignored) {}
        }
        return null;
    }
}
