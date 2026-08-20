package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.util.UiSound;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

/** Keeps the container screen active while the item editor is rendered above it. */
public final class InventoryEditorOverlay {
    private static final Map<AbstractContainerScreen<?>, InventoryEditorOverlay> OVERLAYS = new IdentityHashMap<>();

    private final AbstractContainerScreen<?> container;
    private Screen editor;
    private boolean active;
    private boolean suspendedForModal;
    private boolean eventsBound;
    private float brandAnim;
    private float settingsHoverAnim;

    private InventoryEditorOverlay(AbstractContainerScreen<?> container) {
        this.container = container;
    }

    public static InventoryEditorOverlay attach(AbstractContainerScreen<?> container, KeyMapping openKey) {
        InventoryEditorOverlay overlay = OVERLAYS.computeIfAbsent(container, InventoryEditorOverlay::new);
        overlay.bindEvents(openKey);
        if (overlay.suspendedForModal) {
            overlay.suspendedForModal = false;
        }
        if (overlay.active && overlay.editor != null) {
            overlay.reinitializeEditor();
        }
        return overlay;
    }

    public void open(ItemStack stack, int slot) {
        if (stack == null || stack.isEmpty()) return;
        active = true;
        brandAnim = AnkiConfig.isUiAnimationEnabled() ? 0f : 1f;
        settingsHoverAnim = 0f;
        suspendedForModal = false;
        ImeSupport.overlayOpened();
        editor = "advanced".equalsIgnoreCase(AnkiConfig.getPreferredItemEditor())
                ? new NbtEditorScreen(stack, slot, container)
                : new SimpleEditorScreen(stack, slot, container);
        reinitializeEditor();
    }

    static void close(AbstractContainerScreen<?> container) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay == null) return;
        overlay.active = false;
        overlay.editor = null;
        ImeSupport.overlayClosed();
    }

    static void switchToAdvanced(AbstractContainerScreen<?> container, ItemStack current,
                                 ItemStack original, int slot, boolean dirty) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay == null) return;
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
        SimpleEditorScreen next = new SimpleEditorScreen(current, slot, container);
        next.restoreEditorState(original, dirty);
        AnkiConfig.setPreferredItemEditor("simple");
        overlay.editor = next;
        overlay.active = true;
        overlay.reinitializeEditor();
    }

    public static void openModal(AbstractContainerScreen<?> container, Screen modal) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay == null) {
            Minecraft.getInstance().setScreenAndShow(modal);
            return;
        }
        overlay.suspendedForModal = true;
        Minecraft.getInstance().setScreenAndShow(modal);
    }

    static void returnFromModal(AbstractContainerScreen<?> container) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay != null) overlay.suspendedForModal = true;
        Minecraft.getInstance().setScreenAndShow(container);
    }

    private void bindEvents(KeyMapping openKey) {
        // The same container instance is restored after opening a modal. Keep one
        // event set so the editor hotkey is not processed twice on the next press.
        if (eventsBound) return;
        eventsBound = true;
        ScreenEvents.afterExtract(container).register((screen, graphics, mouseX, mouseY, tickDelta) ->
                render(graphics, mouseX, mouseY, tickDelta));
        ScreenEvents.afterTick(container).register(screen -> {
            if (active && editor != null) editor.tick();
        });
        ScreenEvents.remove(container).register(screen -> {
            if (!suspendedForModal) {
                active = false;
                editor = null;
                ImeSupport.overlayClosed();
                OVERLAYS.remove(container);
            }
        });

        ScreenKeyboardEvents.allowKeyPress(container).register((screen, event) -> {
            if (openKey.matches(event)) {
                if (active) {
                    requestClose();
                    return false;
                }
                return !openHoveredItem();
            }
            if (!active || editor == null) return true;
            return !editor.keyPressed(event);
        });
        ScreenMouseEvents.allowMouseClick(container).register((screen, event) -> handleMouseClick(event));
        ScreenMouseEvents.allowMouseDrag(container).register((screen, event, dragX, dragY) -> {
            if (!active || editor == null || (!inside(event.x(), event.y()) && !isDraggingMenuBar())) return true;
            editor.mouseDragged(event, dragX, dragY);
            return false;
        });
        ScreenMouseEvents.allowMouseRelease(container).register((screen, event) -> {
            if (!active || editor == null || (!inside(event.x(), event.y()) && !isDraggingMenuBar())) return true;
            editor.mouseReleased(event);
            return false;
        });
        ScreenMouseEvents.allowMouseScroll(container).register((screen, mouseX, mouseY, horizontal, vertical) -> {
            if (!active || editor == null || !inside(mouseX, mouseY)) return true;
            if (editor instanceof SimpleEditorScreen simple) {
                simple.mouseScrolled(mouseX, mouseY, horizontal, vertical);
            } else if (editor instanceof NbtEditorScreen advanced) {
                advanced.mouseScrolled(mouseX, mouseY, horizontal, vertical);
            }
            return false;
        });
    }

    private boolean handleMouseClick(MouseButtonEvent event) {
        if (!active || editor == null) return true;
        if (event.button() == 0 && EditorBrandLayer.isSettingsButton(event.x(), event.y(), container.width)) {
            UiSound.playClick();
            openModal(container, new AnkiConfigScreen(container));
            return false;
        }
        if (inside(event.x(), event.y())) {
            ImeSupport.updateCursorArea(event.x(), event.y());
            editor.mouseClicked(event, false);
            return false;
        }

        Slot hovered = EditorDock.hoveredSlot(container);
        if (event.button() == 0 && EditorDock.isPlayerInventorySlot(hovered) && hovered.hasItem()) {
            selectItem(hovered.getItem(), hovered.getContainerSlot());
            return false;
        }
        return true;
    }

    private boolean openHoveredItem() {
        Slot hovered = EditorDock.hoveredSlot(container);
        if (EditorDock.isPlayerInventorySlot(hovered) && hovered.hasItem()) {
            open(hovered.getItem(), hovered.getContainerSlot());
            return true;
        }
        return false;
    }

    private void selectItem(ItemStack stack, int slot) {
        if (editor instanceof SimpleEditorScreen simple) {
            simple.selectInventoryItem(stack, slot);
        } else if (editor instanceof NbtEditorScreen advanced) {
            advanced.selectInventoryItem(stack, slot);
        }
    }

    private void requestClose() {
        if (editor instanceof SimpleEditorScreen simple) {
            simple.requestOverlayClose();
        } else if (editor instanceof NbtEditorScreen advanced) {
            advanced.requestOverlayClose();
        }
    }

    public static boolean handleCharTyped(Screen screen, CharacterEvent event) {
        if (!(screen instanceof AbstractContainerScreen<?> container)) return false;
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        return overlay != null
                && overlay.active
                && !overlay.suspendedForModal
                && overlay.editor != null
                && overlay.editor.charTyped(event);
    }

    /**
     * GLFW sends IME composition updates separately from committed characters.
     * Forward them to the focused editor widget while the inventory screen
     * remains the active vanilla screen.  The normal KeyboardHandler callback
     * is intentionally not cancelled, so the container keeps its own native
     * input-method state intact.
     */
    public static boolean handlePreedit(Screen screen, PreeditEvent event) {
        if (!(screen instanceof AbstractContainerScreen<?> container) || event == null) return false;
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        return overlay != null
                && overlay.active
                && !overlay.suspendedForModal
                && overlay.editor != null
                && overlay.editor.preeditUpdated(event);
    }

    private boolean inside(double mouseX, double mouseY) {
        if (editor instanceof SimpleEditorScreen simple) return simple.isInsideEditor(mouseX, mouseY);
        if (editor instanceof NbtEditorScreen advanced) return advanced.isInsideEditor(mouseX, mouseY);
        return false;
    }

    private boolean isDraggingMenuBar() {
        if (editor instanceof SimpleEditorScreen simple) return simple.isDraggingMenuBar();
        if (editor instanceof NbtEditorScreen advanced) return advanced.isDraggingMenuBar();
        return false;
    }

    private void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        if (active && !suspendedForModal && editor != null) {
            updateBrandAnimation();
            boolean settingsHovered = EditorBrandLayer.isSettingsButton(mouseX, mouseY, container.width);
            settingsHoverAnim = EditorBrandLayer.approachSettingsHover(settingsHoverAnim, settingsHovered);
            com.ankinbt.compat.GuiGraphics ui = new com.ankinbt.compat.GuiGraphics(graphics);
            EditorBrandLayer.renderBackgroundLogo(ui, container.width, container.height);
            editor.extractRenderState(graphics, mouseX, mouseY, tickDelta);
            EditorBrandLayer.renderItemStatus(ui, Minecraft.getInstance().font, container.width, container.height,
                    brandAnim, editorMode());
            EditorBrandLayer.renderSettingsButton(ui, Minecraft.getInstance().font, container.width,
                    mouseX, mouseY, settingsHoverAnim);
        }
    }

    private void updateBrandAnimation() {
        brandAnim = EditorBrandLayer.approachOpen(brandAnim);
    }

    private String editorMode() {
        String key = editor instanceof NbtEditorScreen
                ? "ankinbt.config.mode.advanced"
                : "ankinbt.config.mode.simple";
        return itemEditorStatusMode(Component.translatable(key).getString());
    }

    static boolean isItemEditorPreviewMode() {
        Minecraft client = Minecraft.getInstance();
        return client != null && client.player != null && !client.player.isCreative();
    }

    static String itemEditorStatusMode(String editorMode) {
        if (!isItemEditorPreviewMode()) return editorMode;
        return Component.translatable("ankinbt.editor.status.preview", editorMode).getString();
    }

    private void reinitializeEditor() {
        if (editor != null) editor.init(container.width, container.height);
    }
}
