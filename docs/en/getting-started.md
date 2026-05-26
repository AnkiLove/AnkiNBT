# Getting Started

## Opening the Editor

There are two ways to open the AnkiNBT editor:

### Method 1: Main Hand Item
1. Hold an item in your main hand.
2. Press `N` (default key, configurable in Controls > AnkiNBT).
3. The editor opens for the held item.

### Method 2: Inventory Hover
1. Open your inventory (or any container).
2. Hover your mouse over the item you want to edit.
3. Press `N`.
4. The editor opens for the hovered item.

> If you press `N` without holding or hovering over an item, a message will appear: *"No item to edit"*.

---

## Editor Modes

AnkiNBT has two editing modes:

| Mode | Best For | Description |
|------|----------|-------------|
| **Simple Mode** | Beginners | Visual editor with categories and clickable fields. No NBT knowledge required. |
| **Advanced Mode** | Power users | Full NBT tree view. Edit any data component directly. |

You can switch between modes using the toggle button at the top of the editor.

---

## Saving Changes

- Press `Ctrl+S` to save your changes to the item.
- Saving is **only available in Creative Mode**. In other game modes, the editor is view-only.
- A green status message confirms the save was successful.

---

## Closing the Editor

- Press `Esc` to close the editor.
- If you have unsaved changes, a confirmation dialog will appear asking whether to discard or continue editing.

---

## Configuration

AnkiNBT stores its config in `.minecraft/config/ankinbt.json`. Available settings:

| Setting | Default | Description |
|---------|---------|-------------|
| `openKeyCode` | `N` | Key to open the editor |
| `showAdvancedTags` | `false` | Start in Advanced Mode by default |
| `uiOpacity` | `0.85` | UI background opacity (0.3 ~ 1.0) |
| `treeExpandedByDefault` | `false` | Auto-expand all tree nodes in Advanced Mode |
| `nbtExportDir` | `ankinbt-config/save-nbt` | Directory for NBT file exports |
| `autoLoadLastNbt` | `true` | Remember last opened NBT file |
| `confirmOnClose` | `true` | Show unsaved changes dialog |

The config file is created automatically on first launch and updates when you change settings.
