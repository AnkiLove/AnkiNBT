# 安装指南

## 前置条件

- **Minecraft** 1.21 ~ 1.21.11、26.1 ~ 26.1.2
- Minecraft 1.21 系列使用 **Java 21**
- Minecraft 26.1 系列使用 **Java 25**
- 已安装 **NeoForge** 或 **Fabric**（Fabric 需同时安装 Fabric API）

---

## NeoForge 安装

1. 下载并安装对应 Minecraft 版本的 [NeoForge](https://neoforged.net/)。
2. 从 [Modrinth](https://modrinth.com/mod/ankinbt) 或 [GitHub Releases](https://github.com/AnkiLove/AnkiNBT/releases) 下载 **AnkiNBT NeoForge** 版本的 JAR 文件。
   - 请确保选择与你的 Minecraft 版本匹配的 JAR（例如 `AnkiNBT-neoforge-1.21.1-x.x.x.jar`）。
3. 将 JAR 文件放入 `.minecraft/mods/` 目录。
4. 使用 NeoForge 配置启动 Minecraft。

---

## Fabric 安装

1. 下载并安装对应 Minecraft 版本的 [Fabric Loader](https://fabricmc.net/)。
2. 下载并安装 [Fabric API](https://modrinth.com/mod/fabric-api) — Fabric 版本**必须**安装此前置。
3. 从 [Modrinth](https://modrinth.com/mod/ankinbt) 或 [GitHub Releases](https://github.com/AnkiLove/AnkiNBT/releases) 下载 **AnkiNBT Fabric** 版本的 JAR 文件。
   - 请确保选择与你的 Minecraft 版本匹配的 JAR（例如 `AnkiNBT-fabric-1.21.1-x.x.x.jar`）。
4. 将 Fabric API 和 AnkiNBT 的 JAR 文件一起放入 `.minecraft/mods/` 目录。
5. 使用 Fabric 配置启动 Minecraft。

---

## 版本兼容表

| Minecraft 版本 | NeoForge | Fabric (+ Fabric API) |
|---------------|----------|----------------------|
| 1.21      | 支持 | 支持 |
| 1.21.1    | 支持 | 支持 |
| 1.21.2    | 支持 | 支持 |
| 1.21.3    | 支持 | 支持 |
| 1.21.4    | 支持 | 支持 |
| 1.21.5    | 支持 | 支持 |
| 1.21.6    | 支持 | 支持 |
| 1.21.7    | 支持 | 支持 |
| 1.21.8    | 支持 | 支持 |
| 1.21.9    | 支持 | 支持 |
| 1.21.10   | 支持 | 支持 |
| 1.21.11   | 支持 | 支持 |
| 26.1      | 支持 | 支持 |
| 26.1.1    | 支持 | 支持 |
| 26.1.2    | 支持 | 支持 |

---

## 验证安装

启动游戏后：

1. 打开 **模组** 菜单（NeoForge 或 Fabric），确认模组列表中有 **AnkiNBT**。
2. 在游戏中，手持任意物品按 `N` — 编辑器应该弹出。
3. 如果没有反应，请检查：
   - 模组版本是否与 Minecraft 版本匹配。
   - Fabric 版本是否同时安装了 Fabric API。
   - 1.21 系列是否使用 Java 21，26.1 系列是否使用 Java 25。

---

## 更新模组

更新 AnkiNBT 只需将 `.minecraft/mods/` 中的旧 JAR 替换为新版本即可。配置文件（`ankinbt.json`）会自动保留。
