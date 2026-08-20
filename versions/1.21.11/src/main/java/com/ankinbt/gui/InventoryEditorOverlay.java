package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.util.UiSound;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    private float brandAnim;
    private float settingsHoverAnim;

    private InventoryEditorOverlay(AbstractContainerScreen<?> container) {
        this.container = container;
    }

    public static InventoryEditorOverlay attach(AbstractContainerScreen<?> container, KeyMapping openKey) {
        InventoryEditorOverlay overlay = OVERLAYS.computeIfAbsent(container, InventoryEditorOverlay::new);
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
            Minecraft.getInstance().setScreen(modal);
            return;
        }
        overlay.suspendedForModal = true;
        Minecraft.getInstance().setScreen(modal);
    }

    static void returnFromModal(AbstractContainerScreen<?> container) {
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay != null) overlay.suspendedForModal = true;
        Minecraft.getInstance().setScreen(container);
    }

    public static boolean handleKeyPressed(Screen screen, KeyEvent event, KeyMapping openKey) {
        if (!(screen instanceof AbstractContainerScreen<?> container)) return false;
        InventoryEditorOverlay overlay = attach(container, openKey);
        if (openKey != null && openKey.matches(event)) {
            if (overlay.active) {
                overlay.requestClose();
                return true;
            }
            return overlay.openHoveredItem();
        }
        return overlay.active && overlay.editor != null && overlay.editor.keyPressed(event);
    }

    public static boolean handleMouseClick(Screen screen, MouseButtonEvent event) {
        InventoryEditorOverlay overlay = find(screen);
        return overlay != null && !overlay.handleMouseClick(event);
    }

    public static boolean handleMouseDrag(Screen screen, MouseButtonEvent event, double dragX, double dragY) {
        InventoryEditorOverlay overlay = find(screen);
        if (overlay == null || !overlay.active || overlay.editor == null
                || (!overlay.inside(event.x(), event.y()) && !overlay.isDraggingMenuBar())) return false;
        overlay.editor.mouseDragged(event, dragX, dragY);
        return true;
    }

    public static boolean handleMouseRelease(Screen screen, MouseButtonEvent event) {
        InventoryEditorOverlay overlay = find(screen);
        if (overlay == null || !overlay.active || overlay.editor == null
                || (!overlay.inside(event.x(), event.y()) && !overlay.isDraggingMenuBar())) return false;
        overlay.editor.mouseReleased(event);
        return true;
    }

    public static boolean handleMouseScroll(Screen screen, double mouseX, double mouseY,
                                            double horizontal, double vertical) {
        InventoryEditorOverlay overlay = find(screen);
        if (overlay == null || !overlay.active || overlay.editor == null || !overlay.inside(mouseX, mouseY)) {
            return false;
        }
        if (overlay.editor instanceof SimpleEditorScreen simple) {
            simple.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        } else if (overlay.editor instanceof NbtEditorScreen advanced) {
            advanced.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        }
        return true;
    }

    public static void tick(Screen screen) {
        InventoryEditorOverlay overlay = find(screen);
        if (overlay != null && overlay.active && !overlay.suspendedForModal && overlay.editor != null) {
            overlay.editor.tick();
        }
    }

    public static void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY,
                              float tickDelta) {
        InventoryEditorOverlay overlay = find(screen);
        if (overlay != null) overlay.render(graphics, mouseX, mouseY, tickDelta);
    }

    public static void screenClosing(Screen screen) {
        InventoryEditorOverlay overlay = find(screen);
        if (overlay == null || overlay.suspendedForModal) return;
        overlay.active = false;
        overlay.editor = null;
        ImeSupport.overlayClosed();
        OVERLAYS.remove(overlay.container);
    }

    public static boolean isActive(Screen screen) {
        InventoryEditorOverlay overlay = find(screen);
        return overlay != null && overlay.active && !overlay.suspendedForModal && overlay.editor != null;
    }

    private static InventoryEditorOverlay find(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> container)) return null;
        return OVERLAYS.get(container);
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

    private void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        if (active && !suspendedForModal && editor != null) {
            updateBrandAnimation();
            boolean settingsHovered = EditorBrandLayer.isSettingsButton(mouseX, mouseY, container.width);
            settingsHoverAnim = EditorBrandLayer.approachSettingsHover(settingsHoverAnim, settingsHovered);
            EditorBrandLayer.renderBackgroundLogo(graphics, container.width, container.height);
            editor.render(graphics, mouseX, mouseY, tickDelta);
            EditorBrandLayer.renderItemStatus(graphics, Minecraft.getInstance().font, container.width, container.height,
                    brandAnim, editorMode());
            EditorBrandLayer.renderSettingsButton(graphics, Minecraft.getInstance().font, container.width,
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
