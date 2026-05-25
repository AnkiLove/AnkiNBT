# AnkiNBT 1.2.0 Changelog

## Added
- Expanded Entity Visual Editor: richer attribute editing, live preview, one-click full heal, and state toggles.
- Expanded Villager Trade Editor: add/remove trades, profession/level controls, quick item picking, and main-hand fill.
- New tabbed Config Menu: `General`, `Keys`, `UI`, `Advanced`, `Debug`, including a live debug log panel.
- New Item Picker and visual Custom Group Editor (create/rename/delete groups, reorder groups, manage group items).
- UI click sound feedback and configurable UI sound volume.

## Improved
- Refined layout/style for Simple Editor, Entity Editor, Villager Editor, and Config screens with a modern card-based UI.
- Better button feedback, unsaved-changes confirmation flow, and critical action paths.
- Improved keybinding label rendering to avoid abnormal key names (such as `#78/#79`).
- Better scrolling and usability for picker/list-heavy screens.

## Fixed
- Fixed 1.21.11 screen rendering crashes (`Can only blur once per frame`).
- Fixed unstable health application behavior in Entity Editor and added clear upper-cap warning flow.
- Fixed Villager trade read/apply compatibility across target sources (existing entity vs. spawn egg).
- Fixed container editor entry visibility and availability checks (shown only for valid container-edit scenarios).
- Fixed multiple compile/runtime compatibility issues caused by minor-version API differences.

## Compatibility
- Supports both NeoForge and Fabric.
- Covers Minecraft `1.21` through `1.21.11` via version adapters.
