# Installation Guide

## Prerequisites

- **Minecraft** 1.21 ~ 1.21.11, 26.1 ~ 26.1.2
- **Java 21** for Minecraft 1.21 builds
- **Java 25** for Minecraft 26.1 builds
- **NeoForge** or **Fabric** (+ Fabric API) installed for your MC version

---

## Installing on NeoForge

1. Download and install [NeoForge](https://neoforged.net/) for your Minecraft version.
2. Download the **AnkiNBT NeoForge** JAR from [Modrinth](https://modrinth.com/mod/ankinbt) or [GitHub Releases](https://github.com/AnkiLove/AnkiNBT/releases).
   - Make sure to pick the correct JAR for your Minecraft version (e.g., `AnkiNBT-neoforge-1.21.1-x.x.x.jar`).
3. Place the JAR file into your `.minecraft/mods/` folder.
4. Launch Minecraft with the NeoForge profile.

---

## Installing on Fabric

1. Download and install [Fabric Loader](https://fabricmc.net/) for your Minecraft version.
2. Download and install [Fabric API](https://modrinth.com/mod/fabric-api) — this is **required** for AnkiNBT on Fabric.
3. Download the **AnkiNBT Fabric** JAR from [Modrinth](https://modrinth.com/mod/ankinbt) or [GitHub Releases](https://github.com/AnkiLove/AnkiNBT/releases).
   - Make sure to pick the correct JAR for your Minecraft version (e.g., `AnkiNBT-fabric-1.21.1-x.x.x.jar`).
4. Place both the Fabric API JAR and the AnkiNBT JAR into your `.minecraft/mods/` folder.
5. Launch Minecraft with the Fabric profile.

---

## Version Compatibility Table

| Minecraft | NeoForge | Fabric (+ Fabric API) |
|-----------|----------|----------------------|
| 1.21      | Yes      | Yes                  |
| 1.21.1    | Yes      | Yes                  |
| 1.21.2    | Yes      | Yes                  |
| 1.21.3    | Yes      | Yes                  |
| 1.21.4    | Yes      | Yes                  |
| 1.21.5    | Yes      | Yes                  |
| 1.21.6    | Yes      | Yes                  |
| 1.21.7    | Yes      | Yes                  |
| 1.21.8    | Yes      | Yes                  |
| 1.21.9    | Yes      | Yes                  |
| 1.21.10   | Yes      | Yes                  |
| 1.21.11   | Yes      | Yes                  |
| 26.1      | Yes      | Yes                  |
| 26.1.1    | Yes      | Yes                  |
| 26.1.2    | Yes      | Yes                  |

---

## Verifying Installation

After launching the game:

1. Open the **Mods** menu (NeoForge or Fabric) and look for **AnkiNBT** in the mod list.
2. In-game, hold any item and press `N` — the editor should open.
3. If nothing happens, check that:
   - You have the correct mod version for your Minecraft version.
   - On Fabric, Fabric API is also installed.
   - You are using Java 21 for Minecraft 1.21, or Java 25 for Minecraft 26.1.

---

## Updating

To update AnkiNBT, simply replace the old JAR in `.minecraft/mods/` with the new one. Your configuration file (`ankinbt.json`) will be preserved.
