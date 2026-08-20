package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.util.UiSound;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.IdentityHashMap;
import java.util.Map;

/** Keeps a vanilla container active while the AnkiNBT item editor is open. */
public final class InventoryEditorOverlay {
    private static final Map<AbstractContainerScreen<?>, InventoryEditorOverlay> OVERLAYS =
            new IdentityHashMap<>();

    private final AbstractContainerScreen<?> container;
    private Screen editor;
    private boolean active;
    private boolean suspendedForModal;
    private boolean eventsBound;
    private float brandAnimation;
    private float settingsHoverAnimation;
    private double lastMouseX;
    private double lastMouseY;

    private InventoryEditorOverlay(AbstractContainerScreen<?> container) {
        this.container = container;
    }

    public static InventoryEditorOverlay attach(AbstractContainerScreen<?> container, KeyMapping openKey) {
        InventoryEditorOverlay overlay = OVERLAYS.computeIfAbsent(container, InventoryEditorOverlay::new);
        overlay.bindEvents(openKey);
        if (overlay.suspendedForModal) {
            overlay.suspendedForModal = false;
            overlay.reinitializeEditor();
        }
        return overlay;
    }

    public void open(ItemStack stack, int slot) {
        if (stack == null || stack.isEmpty()) return;
        disposeEditor();
        active = true;
        suspendedForModal = false;
        brandAnimation = AnkiConfig.isUiAnimationEnabled() ? 0.0f : 1.0f;
        settingsHoverAnimation = 0.0f;
        editor = "advanced".equalsIgnoreCase(AnkiConfig.getPreferredItemEditor())
                ? new NbtEditorScreen(stack, slot, container)
                : new SimpleEditorScreen(stack, slot, container);
        reinitializeEditor();
    }

    static void close(AbstractContainerScreen<?> container) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay == null) return;
        overlay.active = false;
        overlay.disposeEditor();
    }

    static void switchToAdvanced(AbstractContainerScreen<?> container, ItemStack current,
                                 ItemStack original, int slot, boolean dirty) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay == null) return;
        overlay.disposeEditor();
        NbtEditorScreen next = new NbtEditorScreen(current, slot, container);
        next.restoreEditorState(original, dirty);
        AnkiConfig.setPreferredItemEditor("advanced");
        overlay.editor = next;
        overlay.active = true;
        overlay.reinitializeEditor();
    }

    static void switchToSimple(AbstractContainerScreen<?> container, ItemStack current,
                               ItemStack original, int slot, boolean dirty) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay == null) return;
        overlay.disposeEditor();
        SimpleEditorScreen next = new SimpleEditorScreen(current, slot, container);
        next.restoreEditorState(original, dirty);
        AnkiConfig.setPreferredItemEditor("simple");
        overlay.editor = next;
        overlay.active = true;
        overlay.reinitializeEditor();
    }

    public static void openModal(AbstractContainerScreen<?> container, Screen modal) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay != null) overlay.suspendedForModal = true;
        Minecraft.getInstance().setScreen(modal);
    }

    static void returnFromModal(AbstractContainerScreen<?> container) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay != null) overlay.suspendedForModal = true;
        Minecraft.getInstance().setScreen(container);
    }

    public static boolean handleCharTyped(Screen screen, int codePoint, int modifiers) {
        if (!(screen instanceof AbstractContainerScreen<?> container)) return false;
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay == null || !overlay.active || overlay.suspendedForModal || overlay.editor == null) {
            return false;
        }
        boolean handled = false;
        for (char unit : Character.toChars(codePoint)) {
            handled |= overlay.editor.charTyped(unit, modifiers);
        }
        return handled;
    }

    public static boolean isItemEditorPreviewMode() {
        Minecraft client = Minecraft.getInstance();
        return client != null && client.player != null && !client.player.isCreative();
    }

    public static String itemEditorStatusMode(String editorMode) {
        if (!isItemEditorPreviewMode()) return editorMode;
        return Component.translatable("ankinbt.editor.status.preview", editorMode).getString();
    }

    private void bindEvents(KeyMapping openKey) {
        if (eventsBound) return;
        eventsBound = true;

        ScreenEvents.afterRender(container).register((screen, graphics, mouseX, mouseY, tickDelta) ->
                render(graphics, mouseX, mouseY, tickDelta));
        ScreenEvents.afterTick(container).register(screen -> {
            if (active && !suspendedForModal && editor != null) editor.tick();
        });
        ScreenEvents.remove(container).register(screen -> {
            if (!suspendedForModal) {
                active = false;
                disposeEditor();
                OVERLAYS.remove(container);
            }
        });

        ScreenKeyboardEvents.allowKeyPress(container).register((screen, key, scancode, modifiers) -> {
            if (openKey.matches(key, scancode)) {
                if (active) requestClose();
                else openHoveredItem();
                return false;
            }
            if (!active || suspendedForModal || editor == null) return true;
            return !editor.keyPressed(key, scancode, modifiers);
        });
        ScreenMouseEvents.allowMouseClick(container).register((screen, mouseX, mouseY, button) ->
                handleMouseClick(mouseX, mouseY, button));
        ScreenMouseEvents.allowMouseRelease(container).register((screen, mouseX, mouseY, button) -> {
            if (!active || suspendedForModal || editor == null
                    || (!inside(mouseX, mouseY) && !isDragging())) return true;
            editor.mouseReleased(mouseX, mouseY, button);
            return false;
        });
        ScreenMouseEvents.allowMouseScroll(container).register((screen, mouseX, mouseY, horizontal, vertical) -> {
            if (!active || suspendedForModal || editor == null || !inside(mouseX, mouseY)) return true;
            if (editor instanceof SimpleEditorScreen simple) {
                simple.mouseScrolled(mouseX, mouseY, horizontal, vertical);
            } else if (editor instanceof NbtEditorScreen advanced) {
                advanced.mouseScrolled(mouseX, mouseY, horizontal, vertical);
            }
            return false;
        });
    }

    private boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (!active || suspendedForModal || editor == null) return true;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (button == 0 && EditorBrandLayer.isSettingsButton(mouseX, mouseY, container.width)) {
            UiSound.playClick();
            openModal(container, new AnkiConfigScreen(container));
            return false;
        }
        if (inside(mouseX, mouseY)) {
            editor.mouseClicked(mouseX, mouseY, button);
            return false;
        }
        Slot hovered = EditorDock.hoveredSlot(container);
        if (button == 0 && EditorDock.isPlayerInventorySlot(hovered) && hovered.hasItem()) {
            selectItem(hovered.getItem(), hovered.getContainerSlot());
            return false;
        }
        return true;
    }

    private boolean openHoveredItem() {
        Slot hovered = EditorDock.hoveredSlot(container);
        if (!EditorDock.isPlayerInventorySlot(hovered) || !hovered.hasItem()) return false;
        open(hovered.getItem(), hovered.getContainerSlot());
        return true;
    }

    private void selectItem(ItemStack stack, int slot) {
        if (editor instanceof SimpleEditorScreen simple) simple.selectInventoryItem(stack, slot);
        else if (editor instanceof NbtEditorScreen advanced) advanced.selectInventoryItem(stack, slot);
    }

    private void requestClose() {
        if (editor instanceof SimpleEditorScreen simple) simple.requestOverlayClose();
        else if (editor instanceof NbtEditorScreen advanced) advanced.requestOverlayClose();
    }

    private boolean inside(double mouseX, double mouseY) {
        if (editor instanceof SimpleEditorScreen simple) return simple.isInsideEditor(mouseX, mouseY);
        if (editor instanceof NbtEditorScreen advanced) return advanced.isInsideEditor(mouseX, mouseY);
        return false;
    }

    private boolean isDragging() {
        if (editor instanceof SimpleEditorScreen simple) return simple.isDraggingMenuBar();
        if (editor instanceof NbtEditorScreen advanced) return advanced.isDraggingMenuBar();
        return false;
    }

    private void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        if (!active || suspendedForModal || editor == null) return;
        if (isDragging() && GLFW.glfwGetMouseButton(
                Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
            editor.mouseDragged(mouseX, mouseY, GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    mouseX - lastMouseX, mouseY - lastMouseY);
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        brandAnimation = EditorBrandLayer.approachOpen(brandAnimation);
        settingsHoverAnimation = EditorBrandLayer.approachSettingsHover(settingsHoverAnimation,
                EditorBrandLayer.isSettingsButton(mouseX, mouseY, container.width));
        LegacyGuiGraphics ui = new LegacyGuiGraphics(graphics);
        EditorBrandLayer.renderBackgroundLogo(ui, container.width, container.height);
        editor.render(graphics, mouseX, mouseY, tickDelta);
        EditorBrandLayer.renderItemStatus(ui, Minecraft.getInstance().font, container.width, container.height,
                brandAnimation, editorMode());
        EditorBrandLayer.renderSettingsButton(ui, Minecraft.getInstance().font, container.width,
                mouseX, mouseY, settingsHoverAnimation);
    }

    private String editorMode() {
        String key = editor instanceof NbtEditorScreen
                ? "ankinbt.config.mode.advanced"
                : "ankinbt.config.mode.simple";
        return itemEditorStatusMode(Component.translatable(key).getString());
    }

    private void reinitializeEditor() {
        if (editor != null) {
            editor.init(Minecraft.getInstance(), container.width, container.height);
            editor.added();
        }
    }

    private void disposeEditor() {
        if (editor != null) editor.removed();
        editor = null;
    }
}
