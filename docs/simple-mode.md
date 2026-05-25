# Simple Mode

Simple Mode is a visual editor designed for users who want to edit items without any NBT knowledge. It features a category sidebar on the left and a property list on the right.

---

## Layout

- **Left sidebar** — Category navigation (General, Enchantments, Lore, Attributes, Visual, Tools)
- **Right panel** — Property list for the selected category
- **Header** — Item name, mode toggle (Simple / Advanced), save button
- **Footer** — Status messages

---

## Categories

### General

Edit basic item properties:

| Property | Description |
|----------|-------------|
| **Item Name** | Custom display name for the item |
| **Count** | Stack size |
| **Durability** | Current durability value |
| **Unbreakable** | Toggle unbreakable flag |
| **Max Stack Size** | Maximum stack size |
| **Repair Cost** | Anvil repair cost |
| **Fire Resistant** | Whether the item survives fire/lava |
| **Nutrition** | Food nutrition value (food items only) |
| **Saturation** | Food saturation value (food items only) |
| **Rarity** | Item rarity level (Common, Uncommon, Rare, Epic) |

### Enchantments

Manage item enchantments:

- **View** all current enchantments and their levels
- **Edit** enchantment levels by clicking the level value
- **Add** new enchantments from a searchable list (supports both English and Chinese search)
- **Remove** individual enchantments
- **Clear All** — remove all enchantments at once

!!! info
    The enchantment list includes all vanilla enchantments. You can type in the search box to filter by name.

### Lore

Edit item lore (description lines):

- **Add** new lore lines
- **Edit** existing lines by clicking on them
- **Remove** lines with the delete button
- **Reorder** lines using Move Up / Move Down buttons
- **Color codes** — use `&` color codes (e.g., `&c` for red, `&l` for bold)
- **Color palette** — built-in palette picker for easy color selection
- **Formatted color codes** — color codes are applied when editing is confirmed

#### Color Code Reference

| Code | Color | Code | Format |
|------|-------|------|--------|
| `&0` | Black | `&k` | Obfuscated |
| `&1` | Dark Blue | `&l` | **Bold** |
| `&2` | Dark Green | `&m` | ~~Strikethrough~~ |
| `&3` | Dark Aqua | `&n` | Underline |
| `&4` | Dark Red | `&o` | *Italic* |
| `&5` | Purple | `&r` | Reset |
| `&6` | Gold | | |
| `&7` | Gray | | |
| `&8` | Dark Gray | | |
| `&9` | Blue | | |
| `&a` | Green | | |
| `&b` | Aqua | | |
| `&c` | Red | | |
| `&d` | Pink | | |
| `&e` | Yellow | | |
| `&f` | White | | |

### Attributes

Add and manage attribute modifiers:

- **Add** modifiers from a searchable list of all attribute types
- **Attribute types** include: Attack Damage, Attack Speed, Armor, Armor Toughness, Max Health, Movement Speed, Knockback Resistance, Luck, and more
- **Three operations**: Add, Multiply Base, Multiply Total
- **Slot selection**: choose which equipment slot the modifier applies to (Any, Main Hand, Off Hand, Head, Chest, Legs, Feet)
- **Edit** existing modifier values
- **Remove** individual modifiers

### Visual

Customize item appearance:

| Property | Description |
|----------|-------------|
| **Custom Model Data** | Integer value for resource pack custom models |
| **Enchantment Glint** | Force enchantment glint on/off |
| **Hide Tooltip** | Hide the item tooltip entirely |
| **Dye Color** | Set leather armor dye color (RGB) |
| **Item Name Color** | Change the display name color |

### Tools

Utility functions:

| Tool | Description |
|------|-------------|
| **Copy NBT** | Copy the full NBT data to clipboard |
| **Copy /give Command** | Generate and copy a `/give` command for the item |
| **Reset Item** | Revert all changes to the original item state |
| **Export NBT** | Save NBT data to a `.nbt` file (with optional category and alias) |
| **Import NBT** | Load NBT data from a previously exported `.nbt` file |

#### NBT File Export/Import

- Exported files are saved to `.minecraft/ankinbt-config/save-nbt/` by default (configurable).
- You can organize exports into **categories** (subfolders).
- Each export can have an **alias** (friendly name) for easy identification.
- The import screen lists all available `.nbt` files with their alias, size, and modification date.

---

## Tips

- Click any editable field to modify its value.
- Changes are not applied until you press `Ctrl+S` (Creative Mode only).
- Switch to Advanced Mode at any time using the toggle in the header.
