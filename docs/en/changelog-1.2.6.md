# AnkiNBT 1.2.6 Changelog

## Fixes

- Fixed the startup module export conflict on NeoForge 26.1, 26.1.1 and 26.1.2.
- The 26.x builds no longer package Minecraft-owned `net.minecraft` classes into the mod JAR.

## Documentation

- Refined the Wiki landing page so the title no longer breaks into a vertical stack on wide layouts.
- Aligned the Chinese and English home pages with the same quick links, feature map and project links.

## Verification

- NeoForge 26.1, 26.1.1 and 26.1.2 were rebuilt with Java 25 using `clean build`.
- The 26.x fix JARs were checked to ensure they do not contain `net/minecraft` entries.
