package com.ankinbt.gui;

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

/** Keeps the Windows input method attached while an AnkiNBT text editor is active. */
public final class ImeSupport {
    private static final int GLFW_IME = 0x33007;
    private static final Set<Screen> SCREEN_USERS =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean overlayUser;
    private static boolean imeEnabled;

    private ImeSupport() {
    }

    public static boolean isAnkiScreen(Screen screen) {
        return screen != null && screen.getClass().getName().startsWith("com.ankinbt.gui.");
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
        // Minecraft 1.21.9-1.21.11 has no public pre-edit rectangle API.
        // Keeping the native context associated is sufficient for committed text.
    }

    private static void updateState() {
        Minecraft minecraft;
        try {
            minecraft = Minecraft.getInstance();
        } catch (Throwable ignored) {
            return;
        }
        if (minecraft == null || minecraft.getWindow() == null) return;

        boolean shouldEnable = overlayUser || !SCREEN_USERS.isEmpty();
        long window = minecraft.getWindow().handle();
        try {
            if (shouldEnable) {
                GLFW.glfwSetInputMode(window, GLFW_IME, GLFW.GLFW_TRUE);
                WindowsIme.ensureAssociated(window);
                imeEnabled = true;
            } else if (imeEnabled) {
                WindowsIme.releaseManagedContext();
                GLFW.glfwSetInputMode(window, GLFW_IME, GLFW.GLFW_FALSE);
                imeEnabled = false;
            }
        } catch (Throwable ignored) {
            // Some GLFW builds do not expose the IME input mode. The native
            // Windows context remains available through the fallback above.
            if (shouldEnable) {
                WindowsIme.ensureAssociated(window);
                imeEnabled = true;
            }
        }
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
