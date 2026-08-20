<div align="center">

<img src=".github/assets/ankinbt-banner.png" alt="AnkiNBT" width="100%">

# AnkiNBT

适用于 Fabric 与 NeoForge 的 Minecraft 客户端物品、实体和村民交易编辑器。

[![Version](https://img.shields.io/badge/version-2.0.0-22c7e8?style=flat-square)](https://github.com/AnkiLove/AnkiNBT/releases/tag/2.0.0)
[![License](https://img.shields.io/github/license/AnkiLove/AnkiNBT?style=flat-square&color=334155)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21--1.21.11%20%7C%2026.1--26.2-38bdf8?style=flat-square)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-supported-f5c542?style=flat-square)](https://fabricmc.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-supported-ff7a45?style=flat-square)](https://neoforged.net)
[![Wiki](https://img.shields.io/badge/Wiki-中文%20%7C%20English-22c55e?style=flat-square)](https://ankilove.github.io/AnkiNBT-wiki/)

[下载 2.0.0](https://github.com/AnkiLove/AnkiNBT/releases/tag/2.0.0) · [使用文档](https://ankilove.github.io/AnkiNBT-wiki/) · [更新记录](CHANGELOG.md) · [问题反馈](https://github.com/AnkiLove/AnkiNBT/issues)

</div>

## 项目简介

AnkiNBT 是一个客户端编辑模组，用可视化界面编辑 Minecraft 物品、实体和村民交易数据，也可以直接查看和修改 NBT 树。模组同时维护 Fabric 与 NeoForge 构建，并针对每个 Minecraft 版本提供独立适配。

- 简易模式：按分类编辑常见属性，不需要手写 NBT。
- 高级模式：搜索、添加、删除和修改完整 NBT 树。
- 创造模式可保存修改；其他游戏模式可只读查看。
- 中文与英文界面，支持中文输入法和常用文本选择操作。

## 2.0.0 重大更新

2.0.0 重制了物品、实体和村民交易三套编辑流程，并重新核对了 Fabric 与 NeoForge 各版本的数据组件、渲染和保存接口。

### 编辑器重制

- 重制物品编辑器的基础属性、附魔、Lore、属性修饰、外观和工具页。
- 新增刷怪蛋实体与真实实体编辑，支持名称、生命值、状态、撤销和服务端回读。
- 重制村民交易编辑器，支持多项交易、两项买入、卖出、职业、等级、类型、报价和全部交易参数。
- 支持编辑器独立尺寸、缩放、实时预览、窗口重建和配置菜单。
- 使用新的 AnkiNBT 标识与 Mynaui 位图图标，避免缺字方框。

### 兼容与稳定性

- 修复附魔注册表重复项，包括 `minecraft:lunge` 重复导致的选择崩溃。
- 修复低版本 Fabric 打开实体或村民交易编辑器时的背景闪烁。
- 适配不同版本的耐火、不可破坏、自定义模型、属性和完整数据组件格式。
- 修复跨版本村民交易序列化、职业/等级/类型保存和报价同步。
- 完善中文输入、IME 焦点、鼠标选择和兼容层绘制返回值。

完整内容见 [CHANGELOG.md](CHANGELOG.md) 和 [2.0.0 Wiki 更新日志](https://ankilove.github.io/AnkiNBT-wiki/changelog-2.0.0/)。

## 主要功能

### 物品

- 编辑名称、数量、耐久、最大耐久、修复惩罚、最大堆叠、稀有度、不可破坏和耐火。
- 编辑 Lore、颜色代码、附魔、属性修饰、自定义模型、染色、附魔光效和提示显示。
- 编辑食物、药水类型、颜色和自定义效果。
- 导入导出 NBT、复制数据、撤销修改和恢复默认状态。

### 实体

- 编辑刷怪蛋保存的实体数据或玩家看向的真实实体。
- 编辑名称、生命值、静音、发光、无重力、无敌和自定义名称显示。
- 保存后通过集成服务端同步并回读确认。

### 村民交易

- 新增、复制、移动、删除和撤销交易。
- 编辑第一买入、第二买入、卖出物品和数量。
- 编辑最大交易次数、经验、价格倍率、需求、特殊价格等交易字段。
- 编辑村民职业、等级、类型并保存真实报价。

## 支持版本

| 加载器 | Minecraft | Java | 额外依赖 |
|---|---|---|---|
| Fabric | 1.21 到 1.21.11 | Java 21 | Fabric API |
| Fabric | 26.1、26.1.1、26.1.2、26.2 | Java 25 | Fabric API |
| NeoForge | 1.21 到 1.21.11 | Java 21 | 无 |
| NeoForge | 26.1、26.1.1、26.1.2、26.2 | Java 25 | 无 |

2.0.0 共提供 32 个独立 JAR。请同时匹配文件名中的加载器和 `mc` 后面的 Minecraft 版本，不要混用相邻版本。

## 安装与使用

1. 从 [GitHub Releases](https://github.com/AnkiLove/AnkiNBT/releases/tag/2.0.0) 下载与你的加载器和 Minecraft 版本完全匹配的 JAR。
2. Fabric 用户同时安装对应版本的 Fabric API。
3. 将 JAR 放入游戏实例的 `.minecraft/mods/`。
4. 进入游戏后，手持物品或在容器中悬停物品，按 `N` 打开编辑器。
5. 创造模式下按 `Ctrl+S` 保存，按 `Esc` 关闭。

实体、村民交易、配置菜单和编辑器预览快捷键可以在游戏按键设置或 AnkiNBT 配置页中调整。

## 验证状态

2.0.0 正式版已在真实 Minecraft 客户端中完成以下矩阵测试：

- Fabric 与 NeoForge 各 16 个版本。
- `zh_cn` 与 `en_us`。
- 独占全屏、GUI 缩放 4。
- 64/64 个加载器/版本/语言组合通过。
- 3648/3648 项功能与显示检查通过。
- 1984 张最终截图，运行时日志问题为 0。

自动化流程覆盖物品、刷怪蛋实体、真实实体、村民交易、全部页签渲染、连续稳定帧和服务端保存回读。QA 模组仅用于测试，不会打入发布 JAR。

## 构建

项目使用 Gradle。1.21 系列使用 Java 21，26.x 系列使用 Java 25。进入对应版本目录后执行：

```powershell
.\gradlew.bat clean build --no-daemon
```

每个版本的普通 JAR 位于对应目录的 `build/libs`。项目不会默认进行加密或混淆。

## 文档

完整安装说明、功能指南、兼容矩阵、快捷键和中英文更新日志位于：

https://ankilove.github.io/AnkiNBT-wiki/

## License

AnkiNBT 以 [GNU General Public License v3.0 only](LICENSE)（`GPL-3.0-only`）发布。第三方组件继续遵循其各自许可证。
