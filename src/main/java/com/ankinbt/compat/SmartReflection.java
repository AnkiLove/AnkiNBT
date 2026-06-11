/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.item.ItemStack
 */
package com.ankinbt.compat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

public class SmartReflection {
    private static boolean initialized = false;
    private static boolean isNewTooltipApi = false;
    private static String diagnostics = "";
    private static Method drawStringComponentMethod = null;
    private static Method drawStringComponentShadowMethod = null;
    private static Method drawStringStringMethod = null;
    private static Method drawStringStringShadowMethod = null;
    private static Method renderTooltipComponentMethod = null;
    private static Method renderTooltipItemStackMethod = null;
    private static Method renderTooltipListMethod = null;
    private static Method setTooltipComponentMethod = null;
    private static Method setTooltipItemStackMethod = null;
    private static Method setTooltipListMethod = null;
    private static Method setTooltipFontListMethod = null;
    private static Method renderDeferredTooltipMethod = null;

    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        StringBuilder diag = new StringBuilder();
        diag.append("SmartReflection v4.0 initializing...\n");
        Class<GuiGraphics> guiGraphicsClass = GuiGraphics.class;
        try {
            guiGraphicsClass.getMethod("setTooltipForNextFrame", Font.class, Component.class, Integer.TYPE, Integer.TYPE);
            isNewTooltipApi = true;
            diag.append("Detected: 1.21.6+ API (setTooltipForNextFrame)\n");
        }
        catch (NoSuchMethodException e) {
            isNewTooltipApi = false;
            diag.append("Detected: 1.21.0-1.21.5 API (renderTooltip)\n");
        }
        diag.append("drawString methods:\n");
        for (Method m : guiGraphicsClass.getMethods()) {
            if (!m.getName().equals("drawString")) continue;
            String sig = m.getParameterCount() + " params: ";
            for (Class<?> p : m.getParameterTypes()) {
                sig = sig + p.getSimpleName() + ", ";
            }
            diag.append("  - ").append(sig).append("\n");
        }
        if (isNewTooltipApi) {
            diag.append("Setting up NEW tooltip API (1.21.6+)\n");
            SmartReflection.setupNewTooltipApi(guiGraphicsClass, diag);
        } else {
            diag.append("Setting up OLD tooltip API (1.21.0-1.21.5)\n");
            SmartReflection.setupOldTooltipApi(guiGraphicsClass, diag);
        }
        for (Method m : guiGraphicsClass.getMethods()) {
            Class<?>[] params;
            if (m.getName().equals("drawString") && m.getParameterCount() == 5) {
                params = m.getParameterTypes();
                if (params[1].equals(Component.class) && params[4].equals(Boolean.TYPE)) {
                    drawStringComponentShadowMethod = m;
                    diag.append("Found: drawString(Component, shadow)\n");
                    continue;
                }
                if (!params[1].equals(String.class) || !params[4].equals(Boolean.TYPE)) continue;
                drawStringStringShadowMethod = m;
                diag.append("Found: drawString(String, shadow)\n");
                continue;
            }
            if (!m.getName().equals("drawString") || m.getParameterCount() != 4) continue;
            params = m.getParameterTypes();
            if (params[1].equals(Component.class)) {
                drawStringComponentMethod = m;
                diag.append("Found: drawString(Component, no shadow)\n");
                continue;
            }
            if (!params[1].equals(String.class)) continue;
            drawStringStringMethod = m;
            diag.append("Found: drawString(String, no shadow)\n");
        }
        diagnostics = diag.toString();
        System.out.println("[SmartReflection] " + diagnostics.replace("\n", " | "));
    }

    private static void setupNewTooltipApi(Class<?> ggClass, StringBuilder diag) {
        for (Method m : ggClass.getMethods()) {
            if (!m.getName().equals("setTooltipForNextFrame")) continue;
            Class<?>[] params = m.getParameterTypes();
            String sig = params.length + " params: ";
            for (Class<?> p : params) {
                sig = sig + p.getSimpleName() + ", ";
            }
            diag.append("  setTooltipForNextFrame: ").append(sig).append("\n");
            if (params.length != 4) continue;
            if (params[0].equals(Font.class) && params[1].equals(Component.class) && params[2].equals(Integer.TYPE) && params[3].equals(Integer.TYPE)) {
                setTooltipComponentMethod = m;
                diag.append("Matched: setTooltipForNextFrame(Font, Component, int, int)\n");
                continue;
            }
            if (params[0].equals(Font.class) && params[1].getName().contains("ItemStack") && params[2].equals(Integer.TYPE) && params[3].equals(Integer.TYPE)) {
                setTooltipItemStackMethod = m;
                diag.append("Matched: setTooltipForNextFrame(Font, ItemStack, int, int)\n");
                continue;
            }
            if (params[0].equals(List.class) && params[1].equals(Integer.TYPE) && params[2].equals(Integer.TYPE)) {
                setTooltipListMethod = m;
                diag.append("Matched: setTooltipForNextFrame(List, int, int)\n");
                continue;
            }
            if (!params[0].equals(Font.class) || !params[1].equals(List.class) || !params[2].equals(Integer.TYPE) || !params[3].equals(Integer.TYPE)) continue;
            setTooltipFontListMethod = m;
            diag.append("Matched: setTooltipForNextFrame(Font, List, int, int)\n");
        }
        try {
            renderDeferredTooltipMethod = ggClass.getMethod("renderDeferredTooltip", new Class[0]);
            diag.append("Found: renderDeferredTooltip()\n");
        }
        catch (NoSuchMethodException e) {
            diag.append("No renderDeferredTooltip method found\n");
        }
    }

    private static void setupOldTooltipApi(Class<?> ggClass, StringBuilder diag) {
        for (Method m : ggClass.getMethods()) {
            if (!m.getName().equals("renderTooltip")) continue;
            Class<?>[] params = m.getParameterTypes();
            String sig = params.length + " params: ";
            for (Class<?> p : params) {
                sig = sig + p.getSimpleName() + ", ";
            }
            diag.append("  renderTooltip: ").append(sig).append("\n");
            if (params.length != 4) continue;
            if (params[0].equals(Font.class) && params[1].equals(Component.class) && params[2].equals(Integer.TYPE) && params[3].equals(Integer.TYPE)) {
                renderTooltipComponentMethod = m;
                diag.append("Matched: renderTooltip(Font, Component, int, int)\n");
                continue;
            }
            if (params[0].equals(Font.class) && params[1].getName().contains("ItemStack") && params[2].equals(Integer.TYPE) && params[3].equals(Integer.TYPE)) {
                renderTooltipItemStackMethod = m;
                diag.append("Matched: renderTooltip(Font, ItemStack, int, int)\n");
                continue;
            }
            if (!params[0].equals(Font.class) || !params[1].equals(List.class) || !params[2].equals(Integer.TYPE) || !params[3].equals(Integer.TYPE)) continue;
            renderTooltipListMethod = m;
            diag.append("Matched: renderTooltip(Font, List, int, int)\n");
        }
    }

    public static int drawString(GuiGraphics g, Font font, Component text, int x, int y, int color, boolean shadow) {
        if (text == null) {
            return 0;
        }
        if (shadow && drawStringComponentShadowMethod != null) {
            try {
                return (Integer)drawStringComponentShadowMethod.invoke((Object)g, font, text, x, y, color, shadow);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (!shadow && drawStringComponentMethod != null) {
            try {
                return (Integer)drawStringComponentMethod.invoke((Object)g, font, text, x, y, color);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            return font.width((FormattedText)text);
        }
        catch (Exception e) {
            return 0;
        }
    }

    public static int drawString(GuiGraphics g, Font font, String text, int x, int y, int color, boolean shadow) {
        if (text == null) {
            return 0;
        }
        if (shadow && drawStringStringShadowMethod != null) {
            try {
                return (Integer)drawStringStringShadowMethod.invoke((Object)g, font, text, x, y, color, shadow);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (!shadow && drawStringStringMethod != null) {
            try {
                return (Integer)drawStringStringMethod.invoke((Object)g, font, text, x, y, color);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            return font.width(text);
        }
        catch (Exception e) {
            return 0;
        }
    }

    public static void renderTooltip(GuiGraphics g, Font font, Component tooltip, int x, int y) {
        if (tooltip == null) {
            return;
        }
        if (isNewTooltipApi) {
            SmartReflection.renderTooltipNew(g, font, tooltip, x, y);
        } else {
            SmartReflection.renderTooltipOld(g, font, tooltip, x, y);
        }
    }

    private static void renderTooltipNew(GuiGraphics g, Font font, Component tooltip, int x, int y) {
        if (setTooltipComponentMethod != null) {
            try {
                setTooltipComponentMethod.invoke((Object)g, font, tooltip, x, y);
                if (renderDeferredTooltipMethod != null) {
                    renderDeferredTooltipMethod.invoke((Object)g, new Object[0]);
                }
                return;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (setTooltipListMethod != null) {
            try {
                ArrayList<Object> list = new ArrayList<Object>();
                try {
                    Object visualText = Component.class.getMethod("getVisualOrderText", new Class[0]).invoke((Object)tooltip, new Object[0]);
                    list.add(visualText);
                }
                catch (Exception e) {
                    list.add(tooltip);
                }
                setTooltipListMethod.invoke((Object)g, list, x, y);
                if (renderDeferredTooltipMethod != null) {
                    renderDeferredTooltipMethod.invoke((Object)g, new Object[0]);
                }
                return;
            }
            catch (Exception list) {
                // empty catch block
            }
        }
        if (setTooltipFontListMethod != null) {
            try {
                ArrayList<Component> list = new ArrayList<Component>();
                list.add(tooltip);
                setTooltipFontListMethod.invoke((Object)g, font, list, x, y);
                if (renderDeferredTooltipMethod != null) {
                    renderDeferredTooltipMethod.invoke((Object)g, new Object[0]);
                }
                return;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        System.err.println("[SmartReflection] renderTooltip failed (new API)");
    }

    private static void renderTooltipOld(GuiGraphics g, Font font, Component tooltip, int x, int y) {
        if (renderTooltipComponentMethod != null) {
            try {
                renderTooltipComponentMethod.invoke((Object)g, font, tooltip, x, y);
                return;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (renderTooltipListMethod != null) {
            try {
                ArrayList<Component> list = new ArrayList<Component>();
                list.add(tooltip);
                renderTooltipListMethod.invoke((Object)g, font, list, x, y);
                return;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        System.err.println("[SmartReflection] renderTooltip failed (old API)");
    }

    public static void renderItemTooltip(GuiGraphics g, Font font, ItemStack itemStack, int x, int y) {
        if (itemStack == null) {
            return;
        }
        if (isNewTooltipApi) {
            SmartReflection.renderItemTooltipNew(g, font, itemStack, x, y);
        } else {
            SmartReflection.renderItemTooltipOld(g, font, itemStack, x, y);
        }
    }

    private static void renderItemTooltipNew(GuiGraphics g, Font font, ItemStack itemStack, int x, int y) {
        if (setTooltipItemStackMethod != null) {
            try {
                setTooltipItemStackMethod.invoke((Object)g, font, itemStack, x, y);
                if (renderDeferredTooltipMethod != null) {
                    renderDeferredTooltipMethod.invoke((Object)g, new Object[0]);
                }
                return;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (setTooltipComponentMethod != null) {
            try {
                Component tooltip = itemStack.getHoverName();
                setTooltipComponentMethod.invoke((Object)g, font, tooltip, x, y);
                if (renderDeferredTooltipMethod != null) {
                    renderDeferredTooltipMethod.invoke((Object)g, new Object[0]);
                }
                return;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        System.err.println("[SmartReflection] renderItemTooltip failed (new API)");
    }

    private static void renderItemTooltipOld(GuiGraphics g, Font font, ItemStack itemStack, int x, int y) {
        if (renderTooltipItemStackMethod != null) {
            try {
                renderTooltipItemStackMethod.invoke((Object)g, font, itemStack, x, y);
                return;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (renderTooltipComponentMethod != null) {
            try {
                Component tooltip = itemStack.getHoverName();
                renderTooltipComponentMethod.invoke((Object)g, font, tooltip, x, y);
                return;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        System.err.println("[SmartReflection] renderItemTooltip failed (old API)");
    }

    public static void reset() {
        initialized = false;
        isNewTooltipApi = false;
        drawStringComponentMethod = null;
        drawStringComponentShadowMethod = null;
        drawStringStringMethod = null;
        drawStringStringShadowMethod = null;
        renderTooltipComponentMethod = null;
        renderTooltipItemStackMethod = null;
        renderTooltipListMethod = null;
        setTooltipComponentMethod = null;
        setTooltipItemStackMethod = null;
        setTooltipListMethod = null;
        setTooltipFontListMethod = null;
        renderDeferredTooltipMethod = null;
        SmartReflection.initialize();
    }

    public static String getDiagnostics() {
        return diagnostics;
    }

    public static boolean isUsingNewApi() {
        return isNewTooltipApi;
    }

    static {
        SmartReflection.initialize();
    }
}

