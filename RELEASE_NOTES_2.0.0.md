# AnkiNBT 2.0.0 更新版

2.0.0 是一次重大更新。本次更新版替换了较早的 2.0.0 构建，补齐 Minecraft 1.21，并重新验证 Fabric 与 NeoForge 的全部 32 个发布 JAR。

## 主要更新

- 完整重制物品、实体和村民交易编辑器。
- 支持物品组件、附魔、Lore、属性、药水、实体状态和村民报价的可视化编辑。
- 支持真实实体与真实村民异步保存和服务端回读。
- 新增编辑器独立尺寸、缩放、实时预览和窗口重建。
- 更新游戏内 AnkiNBT 标识和 Mynaui 位图图标。
- 修复图标缺字方格、重复 `minecraft:lunge`、低版本 Fabric 背景闪烁及多项跨版本组件问题。

## 支持范围

- Minecraft 1.21 到 1.21.11。
- Minecraft 26.1、26.1.1、26.1.2、26.2。
- Fabric 与 NeoForge，共 32 个独立 JAR。
- 1.21 系列使用 Java 21；26.x 系列使用 Java 25。
- Fabric 版本需要安装对应版本的 Fabric API。

## 验证结果

- 64/64 个加载器/版本/语言组合通过。
- 3648/3648 项功能和显示检查通过。
- 测试环境为独占全屏、GUI 缩放 4、`zh_cn` 与 `en_us`。
- 1984 张最终截图，运行时日志问题为 0。
- QA 自动化代码不包含在发布 JAR 中。

请下载与加载器和 Minecraft 版本完全匹配的 JAR。`AnkiNBT-2.0.0-all-versions.zip` 包含全部构建，`SHA256SUMS.txt` 提供文件校验值。

完整使用说明和版本差异见 [AnkiNBT Wiki](https://ankilove.github.io/AnkiNBT-wiki/)。
