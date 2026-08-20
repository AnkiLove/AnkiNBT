<div align="center">

<img src=".github/assets/ankinbt-banner.png" alt="AnkiNBT" width="100%">

# AnkiNBT

A modern client-side Minecraft NBT editor for Fabric and NeoForge.

一个简单现代化的 Minecraft 客户端 NBT 编辑器，支持 Fabric 与 NeoForge。

[![License](https://img.shields.io/github/license/AnkiLove/AnkiNBT?style=flat-square&color=blue)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1--1.21.11%20%7C%2026.1--26.2-38bdf8?style=flat-square)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-supported-f5c542?style=flat-square)](https://fabricmc.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-supported-ff7a45?style=flat-square)](https://neoforged.net)
[![Wiki](https://img.shields.io/badge/Wiki-GitHub%20Pages-22c55e?style=flat-square)](https://ankilove.github.io/AnkiNBT-wiki/)

[Wiki](https://ankilove.github.io/AnkiNBT-wiki/) · [Releases](https://github.com/AnkiLove/AnkiNBT/releases) · [Issues](https://github.com/AnkiLove/AnkiNBT/issues)

</div>

## 简介

AnkiNBT 用来在客户端查看和编辑物品、实体以及常见数据组件。它提供两套入口：

- 简易模式：可视化编辑物品名称、描述、附魔、属性修饰、药水效果、外观和 NBT 文件。
- 高级模式：直接查看、搜索、添加、删除和修改 NBT 树。

保存修改需要创造模式。非创造模式可以打开编辑器查看数据，但不会写回物品。

## 支持版本

| 加载器 | Minecraft | Java |
|---|---|---|
| Fabric | 1.21.1 到 1.21.11，26.1 到 26.2 | 1.21 系列用 Java 21，26.x 系列用 Java 25 |
| NeoForge | 1.21.1 到 1.21.11，26.1 到 26.2 | 1.21 系列用 Java 21，26.x 系列用 Java 25 |

Fabric 版本需要同时安装 Fabric API。

## 2.0.0

2.0.0 完成了物品、实体和村民交易编辑器的整体重制，并为不同 Minecraft
版本提供独立的兼容实现。正式发布包包含 Minecraft 1.21.1 到 1.21.11、
26.1、26.1.1、26.1.2 和 26.2 的 Fabric 与 NeoForge 构建，共 30 个 JAR。

## 主要功能

- 主手物品和背包悬停物品编辑。
- 物品基础属性、逐行描述、附魔分组、属性修饰和外观编辑。
- 药水基础类型、自定义颜色和多效果编辑。
- 实体与村民交易分页导航，支持实时保存、窗口缩放和独立尺寸调整。
- 村民交易编辑，支持买入、第二买入、卖出物品、排序和交易参数。
- 物品选择器，支持按名称和命名空间搜索。
- NBT 导入导出，支持分类、别名和自定义保存目录。
- 中英文界面与中文输入法，跟随游戏语言自动切换。

## 快速使用

1. 下载与你的加载器和 Minecraft 版本匹配的 jar。
2. Fabric 用户同时安装 Fabric API。
3. 将 jar 放入 `.minecraft/mods/`。
4. 进入游戏后，手持物品按 `N` 打开编辑器。
5. 编辑完成后，在创造模式下按 `Ctrl+S` 保存。

更多安装、版本和使用说明见 Wiki：

https://ankilove.github.io/AnkiNBT-wiki/

## License

AnkiNBT 以 [GNU General Public License v3.0 only](LICENSE)（`GPL-3.0-only`）发布。
第三方组件继续遵循其各自许可证，详情见发行包内的 `THIRD-PARTY-NOTICES.txt`。
