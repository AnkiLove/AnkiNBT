# Advanced Mode

Advanced Mode provides a full NBT tree editor for viewing and modifying all item data components. It serializes the entire `ItemStack` via `ItemStack.CODEC` into a `CompoundTag` for editing, then deserializes back on save.

---

## Layout

- **Left sidebar** — Item info panel showing item ID, stack count, and component count
- **Right panel** — NBT tree view
- **Header** — Title, mode toggle (Simple / Advanced), search bar, action buttons
- **Footer** — Status messages

---

## Tree View

The NBT tree displays all data components of the item in a hierarchical structure:

- **Compound tags** `{}` — contain named child tags (shown with braces)
- **List tags** `[]` — contain ordered child tags (shown with brackets)
- **Primitive tags** — Byte, Short, Int, Long, Float, Double, String
- **Array tags** — ByteArray, IntArray, LongArray

Each tag type is **color-coded** for easy identification:

| Tag Type | Color |
|----------|-------|
| Compound | White |
| List | Light blue |
| String | Green |
| Numbers (Byte/Short/Int/Long) | Gold/Yellow |
| Decimals (Float/Double) | Orange |
| Arrays | Cyan |

---

## Navigating the Tree

| Action | How |
|--------|-----|
| **Select a node** | Click on it |
| **Navigate** | Use arrow keys (Up/Down) |
| **Expand/Collapse** | Double-click, press `E`, or click the arrow icon |
| **Expand All** | Click the "Expand All" button in the header |
| **Collapse All** | Click the "Collapse All" button in the header |

---

## Editing Values

1. **Select** a tag node in the tree.
2. **Double-click** or press `Enter` to open the value editor.
3. The editor validates your input based on the tag type:
   - **String**: any text
   - **Byte**: integer -128 to 127
   - **Short**: integer -32768 to 32767
   - **Int**: integer -2,147,483,648 to 2,147,483,647
   - **Long**: integer -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807
   - **Float/Double**: decimal numbers
4. Press **Enter** or click **Confirm** to apply the change.
5. Press **Esc** to cancel editing.

---

## Adding New Tags

1. Select a **Compound** or **List** node.
2. Click the **"+ Add"** button in the header.
3. Choose the tag type from the dropdown.
4. Enter the tag name (for Compound children) and value.
5. Confirm to add the new tag.

!!! info
    You can add any NBT type: Byte, Short, Int, Long, Float, Double, String, ByteArray, IntArray, LongArray, List, Compound.

---

## Deleting Tags

1. Select the tag you want to remove.
2. Press `Delete` or click the delete button.
3. The tag and all its children (if any) are removed.

---

## Search / Filter

1. Click the search icon or start typing in the search bar.
2. The tree filters to show only nodes matching your query.
3. Search matches against:
   - **Key names** (tag names)
   - **Values** (tag values)
   - **Types** (tag type names)
4. Clear the search to restore the full tree.

---

## Sidebar Info

The left sidebar displays:

- **Item icon** (rendered item)
- **Item ID** (e.g., `minecraft:diamond_sword`)
- **Stack count** (e.g., `x1`)
- **Component count** — total number of data components

---

## Saving and Tools

- Press `Ctrl+S` to save changes back to the item (Creative Mode only).
- The **Export** and **Import** NBT file features are also available in Advanced Mode.
- Use **Copy NBT** to copy the entire tag tree as text to your clipboard.

---

## Tips

- If you make a mistake, close the editor without saving — the original item is preserved.
- The tree auto-collapses by default. Change this in config (`treeExpandedByDefault: true`).
- Switch to Simple Mode at any time using the toggle in the header for quick common edits.
