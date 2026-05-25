# 常见问题

### 问：按 N 没有反应，怎么回事？

**答：** 请检查以下几点：
- 你必须**手持物品**，或在打开的背包界面中**悬停在物品上**。
- 确认按键绑定没有被更改 — 检查 **选项 > 控制 > 按键绑定 > AnkiNBT**。
- 确认 AnkiNBT 已正确安装 — 在模组菜单中查找。

---

### 问：无法保存修改，保存按钮不起作用。

**答：** 保存功能**仅在创造模式**下可用。在生存、冒险或旁观模式下，编辑器为只读模式。请切换到创造模式（`/gamemode creative`）后再保存。

---

### 问：AnkiNBT 可以在服务器上使用吗？

**答：** AnkiNBT 是**客户端**模组，通过客户端创造模式的背包操作来修改物品。在不同服务器环境下：
- **单人游戏 / 局域网**：创造模式下完全可用。
- **支持创造模式的服务器**：如果服务器允许创造模式背包操作则可用。
- **生存服务器**：可以查看物品的 NBT 数据，但无法保存修改。

---

### 问：Fabric 版本需要安装 Fabric API 吗？

**答：** 是的。Fabric 版本需要同时安装 [Fabric API](https://modrinth.com/mod/fabric-api)。

---

### 问：导出的 NBT 文件保存在哪里？

**答：** 默认保存在 `.minecraft/ankinbt-config/save-nbt/`。可以在配置文件（`.minecraft/config/ankinbt.json`，字段 `nbtExportDir`）中修改路径。文件还可以按分类（子文件夹）组织管理。

---

### 问：可以导入其他工具导出的 NBT 文件吗？

**答：** AnkiNBT 使用 Minecraft 标准的压缩 NBT 格式（与结构文件相同）。任何该格式的 `.nbt` 文件都可以导入。导入的数据会替换当前物品的数据组件。

---

### 问：UI 太透明/太不透明，可以调整吗？

**答：** 可以。编辑 `.minecraft/config/ankinbt.json`，修改 `uiOpacity` 值。范围为 `0.3`（非常透明）到 `1.0`（完全不透明）。默认值为 `0.85`。

---

### 问：支持哪些 Minecraft 版本？

**答：** AnkiNBT 支持 Minecraft **1.21 到 1.21.11**，以及 **26.1、26.1.1、26.1.2**，同时支持 NeoForge 和 Fabric。每个版本都有专门的构建版本。

1.21 系列使用 Java 21，26.1 系列使用 Java 25。

---

### 问：如何在简易模式和高级模式之间切换？

**答：** 使用编辑器顶部的模式切换按钮。也可以在配置文件中设置 `showAdvancedTags` 为 `true`（高级模式）或 `false`（简易模式）来更改默认模式。

---

### 问：我发现了 Bug，在哪里反馈？

**答：** 请在 [GitHub Issues](https://github.com/AnkiLove/AnkiNBT/issues) 页面提交问题，包含以下信息：
- 你的 Minecraft 版本
- 模组加载器（NeoForge 或 Fabric）及版本
- 重现步骤
- `.minecraft/logs/latest.log` 中的相关错误日志
