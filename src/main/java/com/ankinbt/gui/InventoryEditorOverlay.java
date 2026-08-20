package com.ankinbt.gui;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.util.UiSound;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

/** NeoForge-side controller for editing an item without replacing its container screen. */
public final class InventoryEditorOverlay {
    private static final Map<AbstractContainerScreen<?>, InventoryEditorOverlay> OVERLAYS =
            new IdentityHashMap<>();

    private final AbstractContainerScreen<?> container;
    private Screen editor;
    private boolean active;
    private boolean suspendedForModal;
    private float brandAnimation;
    private float settingsHoverAnimation;

    private InventoryEditorOverlay(AbstractContainerScreen<?> container) {
        this.container = container;
    }

    public static void attach(AbstractContainerScreen<?> container) {
        InventoryEditorOverlay overlay = OVERLAYS.computeIfAbsent(container, InventoryEditorOverlay::new);
        if (overlay.suspendedForModal) {
            overlay.suspendedForModal = false;
            overlay.reinitializeEditor();
        }
    }

    public static boolean handleOpenKey(Screen screen, KeyMapping openKey, int key, int scancode) {
        if (!(screen instanceof AbstractContainerScreen<?> container) || openKey == null
                || !openKey.matches(key, scancode)) return false;
        InventoryEditorOverlay overlay = OVERLAYS.computeIfAbsent(container, InventoryEditorOverlay::new);
        if (overlay.active) overlay.requestClose();
        else overlay.openHoveredItem();
        return true;
    }

    public static boolean handleKeyPressed(Screen screen, int key, int scancode, int modifiers) {
        InventoryEditorOverlay overlay = activeOverlay(screen);
        return overlay != null && overlay.editor.keyPressed(key, scancode, modifiers);
    }

    public static boolean handleCharTyped(Screen screen, char codePoint, int modifiers) {
        InventoryEditorOverlay overlay = activeOverlay(screen);
        return overlay != null && overlay.editor.charTyped(codePoint, modifiers);
    }

    public static boolean handleMouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        InventoryEditorOverlay overlay = activeOverlay(screen);
        if (overlay == null) return false;
        if (button == 0 && EditorBrandLayer.isSettingsButton(mouseX, mouseY, overlay.container.width)) {
            UiSound.playClick();
            openModal(overlay.container, new AnkiConfigScreen(overlay.container));
            return true;
        }
        if (overlay.inside(mouseX, mouseY)) {
            overlay.editor.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        Slot hovered = EditorDock.hoveredSlot(overlay.container);
        if (button == 0 && EditorDock.isPlayerInventorySlot(hovered) && hovered.hasItem()) {
            overlay.selectItem(hovered.getItem(), hovered.getContainerSlot());
            return true;
        }
        return false;
    }

    public static boolean handleMouseDragged(Screen screen, double mouseX, double mouseY, int button,
                                             double dragX, double dragY) {
        InventoryEditorOverlay overlay = activeOverlay(screen);
        if (overlay == null || (!overlay.inside(mouseX, mouseY) && !overlay.isDragging())) return false;
        overlay.editor.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return true;
    }

    public static boolean handleMouseReleased(Screen screen, double mouseX, double mouseY, int button) {
        InventoryEditorOverlay overlay = activeOverlay(screen);
        if (overlay == null || (!overlay.inside(mouseX, mouseY) && !overlay.isDragging())) return false;
        overlay.editor.mouseReleased(mouseX, mouseY, button);
        return true;
    }

    public static boolean handleMouseScrolled(Screen screen, double mouseX, double mouseY,
                                              double horizontal, double vertical) {
        InventoryEditorOverlay overlay = activeOverlay(screen);
        if (overlay == null || !overlay.inside(mouseX, mouseY)) return false;
        if (overlay.editor instanceof SimpleEditorScreen simple) {
            simple.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        } else if (overlay.editor instanceof NbtEditorScreen advanced) {
            advanced.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        }
        return true;
    }

    public static void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        InventoryEditorOverlay overlay = activeOverlay(screen);
        if (overlay == null) return;
        overlay.brandAnimation = EditorBrandLayer.approachOpen(overlay.brandAnimation);
        overlay.settingsHoverAnimation = EditorBrandLayer.approachSettingsHover(
                overlay.settingsHoverAnimation,
                EditorBrandLayer.isSettingsButton(mouseX, mouseY, overlay.container.width));
        LegacyGuiGraphics ui = new LegacyGuiGraphics(graphics);
        EditorBrandLayer.renderBackgroundLogo(ui, overlay.container.width, overlay.container.height);
        overlay.editor.render(graphics, mouseX, mouseY, partialTick);
        EditorBrandLayer.renderItemStatus(ui, Minecraft.getInstance().font,
                overlay.container.width, overlay.container.height,
                overlay.brandAnimation, overlay.editorMode());
        EditorBrandLayer.renderSettingsButton(ui, Minecraft.getInstance().font,
                overlay.container.width, mouseX, mouseY, overlay.settingsHoverAnimation);
    }

    public static void tick(Screen screen) {
        InventoryEditorOverlay overlay = activeOverlay(screen);
        if (overlay != null) overlay.editor.tick();
    }

    public static void screenClosing(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> container)) return;
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        if (overlay == null || overlay.suspendedForModal) return;
        overlay.active = false;
        overlay.disposeEditor();
        OVERLAYS.remove(container);
    }

    public static boolean isActive(Screen screen) {
        return activeOverlay(screen) != null;
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

    public static boolean isItemEditorPreviewMode() {
        Minecraft client = Minecraft.getInstance();
        return client != null && client.player != null && !client.player.isCreative();
    }

    public static String itemEditorStatusMode(String editorMode) {
        if (!isItemEditorPreviewMode()) return editorMode;
        return Component.translatable("ankinbt.editor.status.preview", editorMode).getString();
    }

    private static InventoryEditorOverlay activeOverlay(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> container)) return null;
        InventoryEditorOverlay overlay = OVERLAYS.get(container);
        return overlay != null && overlay.active && !overlay.suspendedForModal && overlay.editor != null
                ? overlay : null;
    }

    private void open(ItemStack stack, int slot) {
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
