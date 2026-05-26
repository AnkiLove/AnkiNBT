<div align="center">

<img src="src/main/resources/logo.png" alt="AnkiNBT" width="128">

# AnkiNBT

AnkiNBT 是一个现代化的 Minecraft 客户端 NBT 编辑器，支持 Fabric 和 NeoForge。

[![License](https://img.shields.io/github/license/AnkiLove/AnkiNBT?style=flat-square&color=blue)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21--1.21.11%20%7C%2026.1--26.1.2-38bdf8?style=flat-square)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-supported-f5c542?style=flat-square)](https://fabricmc.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-supported-ff7a45?style=flat-square)](https://neoforged.net)
[![Docs](https://img.shields.io/badge/Wiki-GitHub%20Pages-22c55e?style=flat-square)](https://ankilove.github.io/AnkiNBT-wiki/)

[在线文档 / Wiki](https://ankilove.github.io/AnkiNBT-wiki/) · [Releases](https://github.com/AnkiLove/AnkiNBT/releases) · [Issues](https://github.com/AnkiLove/AnkiNBT/issues)

</div>

## 简介

AnkiNBT 用来在客户端查看和编辑物品、实体以及常见数据组件。它提供简易模式和高级模式两套入口：简易模式偏向可视化操作，高级模式用于直接查看和编辑 NBT 树。

当前构建支持：

| 加载器 | Minecraft 版本 |
|---|---|
| Fabric | 1.21 ~ 1.21.11、26.1、26.1.1、26.1.2 |
| NeoForge | 1.21 ~ 1.21.11、26.1、26.1.1、26.1.2 |

## 主要功能

- 简易模式：物品名称、描述、附魔、属性修饰、外观、NBT 导入导出等常用编辑入口
- 高级模式：NBT 树查看、搜索、添加、删除、修改
- 物品选择器：支持搜索和快速切换目标物品
- 村民交易编辑：支持交易物品、数量、价格和交易属性编辑
- 药水效果编辑：支持多效果选择与单独配置
- 文本编辑：支持多行描述、颜色代码、选择文本后上色
- 中英文界面：跟随游戏语言自动切换

## 安装

1. 根据 Minecraft 版本安装 Fabric 或 NeoForge。
2. Fabric 版本需要同时安装 Fabric API。
3. 从 Releases 下载对应版本的 AnkiNBT jar，放入 `.minecraft/mods/`。
4. 进入游戏后，手持物品按 `N` 打开编辑器。

Java 要求：

| Minecraft 版本 | Java |
|---|---|
| 1.21 ~ 1.21.11 | Java 21 |
| 26.1 ~ 26.1.2 | Java 25 |

## 文档

完整使用说明、安装说明和常见问题放在 GitHub Pages：

https://ankilove.github.io/AnkiNBT-wiki/

文档站已统一迁移到独立项目 AnkiNBT-wiki，主项目不再维护内置 Wiki。

## 编译

Windows 下直接运行：

```bat
scripts\build-all.bat -AllVersions -NoPause
```

完整说明见在线文档的编译说明页面。

## 许可证

MIT
