# FAQ

### Q: Nothing happens when I press N. Why?

**A:** Check the following:
- You must be **holding an item** in your main hand, or **hovering over an item** in an open inventory screen.
- Make sure the keybinding hasn't been changed — check **Options > Controls > Key Binds > AnkiNBT**.
- Verify that AnkiNBT is installed correctly — look for it in the Mods menu.

---

### Q: I can't save my changes. The save button doesn't work.

**A:** Saving is **only available in Creative Mode**. In Survival, Adventure, or Spectator mode, the editor is view-only. Switch to Creative Mode (`/gamemode creative`) to save edits.

---

### Q: Does AnkiNBT work on servers?

**A:** AnkiNBT is a **client-side** mod. It modifies items through client-side creative mode inventory operations. On most servers:
- **Single-player / LAN**: works fully in Creative Mode.
- **Servers with Creative Mode access**: works if the server allows creative inventory manipulation.
- **Survival servers**: you can view item NBT data but cannot save changes.

---

### Q: Do I need Fabric API for the Fabric version?

**A:** Yes. The Fabric version requires [Fabric API](https://modrinth.com/mod/fabric-api) to be installed alongside AnkiNBT.

---

### Q: Where are my exported NBT files saved?

**A:** By default, exported files go to `.minecraft/ankinbt-config/save-nbt/`. You can change this path in the config file (`.minecraft/config/ankinbt.json`, field `nbtExportDir`). Files can also be organized into subcategories (subfolders).

---

### Q: Can I import NBT files from other tools?

**A:** AnkiNBT uses Minecraft's standard compressed NBT format (same as structure files). Any `.nbt` file in this format should be importable. The imported data replaces the current item's data components.

---

### Q: The UI is too transparent / too opaque. Can I adjust it?

**A:** Yes. Edit `.minecraft/config/ankinbt.json` and change the `uiOpacity` value. The range is `0.3` (very transparent) to `1.0` (fully opaque). Default is `0.85`.

---

### Q: Which Minecraft versions are supported?

**A:** AnkiNBT supports Minecraft **1.21 through 1.21.11**, plus **26.1, 26.1.1, and 26.1.2** on both NeoForge and Fabric. Each version has a dedicated build.

Minecraft 1.21 builds use Java 21. Minecraft 26.1 builds use Java 25.

---

### Q: How do I switch between Simple Mode and Advanced Mode?

**A:** Use the mode toggle button at the top of the editor. You can also set your default mode in the config file by setting `showAdvancedTags` to `true` (for Advanced Mode) or `false` (for Simple Mode).

---

### Q: I found a bug. Where can I report it?

**A:** Please open an issue on the [GitHub Issues](https://github.com/AnkiLove/AnkiNBT/issues) page with:
- Your Minecraft version
- Mod loader (NeoForge or Fabric) and version
- Steps to reproduce the bug
- Any error logs from `.minecraft/logs/latest.log`
