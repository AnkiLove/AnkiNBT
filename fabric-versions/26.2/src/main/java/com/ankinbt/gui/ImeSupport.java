package com.ankinbt.gui;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.TextInputManager;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;

/**
 * Keeps Minecraft's native text input path alive for AnkiNBT's custom editors.
 *
 * Vanilla 26.1 only requests GLFW IME input when an EditBox reports a focus
 * change. AnkiNBT draws its text fields directly, so it must participate in
 * that lifecycle explicitly. The state model mirrors IMBlocker's focus
 * ownership approach without adding a second mod or native dependency.
 */
public final class ImeSupport {
    /** GLFW_IME is intentionally not exposed by the bundled LWJGL constants. */
    private static final int GLFW_IME = 0x33007;
    private static final Set<Screen> SCREEN_USERS =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean overlayUser;
    private static boolean imeEnabled;

    private ImeSupport() {
    }

    public static boolean isAnkiScreen(Screen screen) {
        return screen != null
                && screen.getClass().getName().startsWith("com.ankinbt.gui.");
    }

    public static void screenOpened(Screen screen) {
        if (!isAnkiScreen(screen)) return;
        SCREEN_USERS.add(screen);
        updateState();
    }

    public static void screenRemoved(Screen screen) {
        if (screen == null) return;
        SCREEN_USERS.remove(screen);
        updateState();
    }

    public static void overlayOpened() {
        overlayUser = true;
        updateState();
    }

    public static void overlayClosed() {
        overlayUser = false;
        updateState();
    }

    public static void updateCursorArea(double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || !isInputRequested() || !ensureTextInputEnabled(minecraft)) return;
        int x = Math.max(0, (int) Math.round(mouseX));
        int y = Math.max(0, (int) Math.round(mouseY));
        try {
            setCursorArea(minecraft, x, y);
        } catch (Throwable ignored) {
        }
    }

    private static void updateState() {
        Minecraft minecraft;
        try {
            minecraft = Minecraft.getInstance();
        } catch (Throwable ignored) {
            return;
        }
        if (minecraft == null || minecraft.getWindow() == null) return;

        boolean shouldEnable = isInputRequested();
        try {
            TextInputManager textInput = minecraft.textInputManager();
            if (shouldEnable) {
                if (ensureTextInputEnabled(minecraft)) {
                    Screen current = minecraft.gui.screen();
                    if (current != null) {
                        setCursorArea(minecraft, current.width / 2, current.height / 2);
                    }
                }
            } else if (imeEnabled) {
                textInput.onTextInputFocusChange(false);
                WindowsIme.releaseManagedContext();
                GLFW.glfwSetInputMode(
                        minecraft.getWindow().handle(),
                        GLFW_IME,
                        GLX.glfwBool(false));
                textInput.notifyIMEChanged();
                imeEnabled = false;
            }
        } catch (Throwable ignored) {
            // Keep editor input usable if a platform window is being rebuilt.
        }
    }

    private static boolean isInputRequested() {
        return overlayUser || !SCREEN_USERS.isEmpty();
    }

    /**
     * Container widgets can change vanilla text focus after the overlay opens.
     * Reassert the native text-input state whenever an AnkiNBT field is clicked
     * so committed IME characters continue to reach the overlay callback.
     */
    private static boolean ensureTextInputEnabled(Minecraft minecraft) {
        try {
            TextInputManager textInput = minecraft.textInputManager();
            textInput.onTextInputFocusChange(true);
            GLFW.glfwSetInputMode(
                    minecraft.getWindow().handle(),
                    GLFW_IME,
                    GLX.glfwBool(true));
            textInput.notifyIMEChanged();
            WindowsIme.ensureAssociated(minecraft.getWindow().handle());
            imeEnabled = true;
            return true;
        } catch (Throwable ignored) {
            imeEnabled = false;
            return false;
        }
    }

    private static void setCursorArea(Minecraft minecraft, int x, int y) {
        minecraft.textInputManager().setTextInputArea(
                x,
                y,
                x + 2,
                y + Math.max(12, minecraft.font.lineHeight + 4));
    }

    private static final class WindowsIme {
        private static final boolean WINDOWS =
                System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        private static HWND managedWindow;
        private static HANDLE managedContext;

        private WindowsIme() {
        }

        private static void ensureAssociated(long glfwWindow) {
            if (!WINDOWS || managedContext != null) return;
            try {
                long nativeWindow = GLFWNativeWin32.glfwGetWin32Window(glfwWindow);
                if (nativeWindow == 0L) return;
                HWND hwnd = new HWND(new Pointer(nativeWindow));
                HANDLE current = Imm32.INSTANCE.ImmGetContext(hwnd);
                if (isValid(current)) {
                    Imm32.INSTANCE.ImmReleaseContext(hwnd, current);
                    return;
                }

                HANDLE created = Imm32.INSTANCE.ImmCreateContext();
                if (!isValid(created)) return;
                Imm32.INSTANCE.ImmAssociateContext(hwnd, created);
                managedWindow = hwnd;
                managedContext = created;
            } catch (Throwable ignored) {
            }
        }

        private static void releaseManagedContext() {
            if (!WINDOWS || managedWindow == null || managedContext == null) return;
            try {
                Imm32.INSTANCE.ImmAssociateContext(managedWindow, null);
                Imm32.INSTANCE.ImmDestroyContext(managedContext);
            } catch (Throwable ignored) {
            } finally {
                managedWindow = null;
                managedContext = null;
            }
        }

        private static boolean isValid(HANDLE handle) {
            return handle != null
                    && handle.getPointer() != null
                    && Pointer.nativeValue(handle.getPointer()) != 0L;
        }

        private interface Imm32 extends StdCallLibrary {
            Imm32 INSTANCE = Native.load("imm32", Imm32.class, W32APIOptions.DEFAULT_OPTIONS);

            HANDLE ImmGetContext(HWND hwnd);

            HANDLE ImmAssociateContext(HWND hwnd, HANDLE context);

            boolean ImmReleaseContext(HWND hwnd, HANDLE context);

            HANDLE ImmCreateContext();

            boolean ImmDestroyContext(HANDLE context);
        }
    }
}
