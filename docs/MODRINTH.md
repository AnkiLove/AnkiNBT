A modern client-side Minecraft NBT editor. Supports NeoForge and Fabric, covering all versions from 1.21 to 1.21.11.

一个简单现代化的 Minecraft 客户端 NBT 编辑器，支持 NeoForge 和 Fabric，覆盖 1.21 ~ 1.21.11 全版本。

---

## Features / 特点

- Two editing modes: Simple Mode (visual) + Advanced Mode (NBT tree)
  双编辑模式：简易模式（可视化编辑）+ 高级模式（NBT 树编辑）
- Translucent dark UI, modern and clean
  半透明暗色 UI，现代简洁
- Edit items from main hand or by hovering in inventory
  支持主手持物品或背包悬停物品编辑
- Save changes in creative mode, view-only in other modes
  创造模式下可保存修改，非创造模式可查看
- NBT export/import to file with configurable path
  NBT 导出/导入文件，支持自定义路径
- Unsaved changes confirmation dialog
  未保存更改确认对话框
- Lore multi-line text editor with move up/down and color palette
  Lore 多行文本编辑器，支持上下移动和色板选择
- Bilingual (English / Chinese), auto-follows game language
  中英双语，自动跟随游戏语言设置

---

## Installation / 安装

1. Install NeoForge or Fabric (+ Fabric API) for your MC version.
   安装对应 MC 版本的 NeoForge 或 Fabric（Fabric 需同时安装 Fabric API）
2. Place the JAR into `.minecraft/mods/`.
   将 JAR 文件放入 `.minecraft/mods/` 目录
3. Launch the game.
   启动游戏

---

## Usage / 使用方法

1. Hold an item and press `N` to open the editor, or open inventory and hover over an item then press `N`.
   手持物品按 `N` 打开编辑器，或打开背包悬停物品按 `N`
2. Press `Ctrl+S` to save.
   编辑完成后按 `Ctrl+S` 保存
3. Press `Esc` to close.
   按 `Esc` 关闭编辑器

---

## Shortcuts / 快捷键

| Key / 快捷键 | Action / 功能 |
|:------------:|:-------------|
| `N` | Open editor (configurable in Controls > AnkiNBT) / 打开编辑器（可在 控制 > AnkiNBT 中自定义） |
| `Ctrl+S` | Save changes / 保存修改 |
| `Esc` | Close editor / 关闭编辑器 |

**Advanced Mode / 高级模式：**

| Key / 快捷键 | Action / 功能 |
|:------------:|:-------------|
| Double-click / 双击 | Edit value / expand-collapse / 编辑值 / 展开折叠 |
| `E` | Expand / collapse node / 展开 / 折叠节点 |
| `Enter` | Edit selected value / 编辑选中值 |
| `Delete` | Remove selected tag / 删除选中标签 |
| Arrow keys / 方向键 | Navigate up/down / 上下导航 |

---

## Simple Mode / 简易模式

![Simplified enchantment editing interface](https://cdn.modrinth.com/data/cached_images/6c98e9b36cbce47236b42b00959c5e9a28a079df_0.webp)

A visual editor with category sidebar on the left and property list on the right. Click to edit. No NBT knowledge needed.

可视化编辑界面，左侧分类导航，右侧属性列表，点击即可编辑。无需了解 NBT。

**General / 基础属性** - Item name, count, durability, unbreakable, max stack size, repair cost, fire resistant, nutrition/saturation, rarity
物品名称、数量、耐久、无法破坏、最大堆叠数、修复花费、防火、饱食度/饱和度、稀有度

**Enchantments / 附魔** - View, edit, add enchantments with searchable list, adjust levels, clear all
查看/修改/添加附魔，支持中文搜索，可调整等级，一键清除

**Lore / 描述** - Add, edit, remove lore lines. Color codes (&c red, &l bold, etc.), built-in palette picker, live preview. Multi-line text editor with move up/down.
添加/编辑/删除描述行，支持颜色代码（&c 红色、&l 粗体等），内置色板选择器，实时预览。多行文本编辑器，支持上下移动。

**Attributes / 属性修饰** - Add modifiers (attack damage, armor, speed, etc.), 3 operations, slot selection, searchable list
添加属性修饰（攻击力、护甲、速度等），三种运算模式，支持槽位选择，中文搜索

**Visual / 外观** - Custom model data, enchantment glint, hide tooltip, dye color, item name color
自定义模型数据、附魔光效、隐藏提示框、染色颜色、物品名称颜色

**Tools / 工具** - Copy NBT to clipboard, copy /give command, reset to original, export/import NBT to file
复制 NBT 到剪贴板、复制 Give 指令、重置为原始物品、导出/导入 NBT 文件

---

## Advanced Mode / 高级模式

![Advanced mode NBT editing interface](https://cdn.modrinth.com/data/cached_images/396fb5482afa86c8f77df3c997a0438df57ea720_0.webp)

Full NBT tree editor for viewing and modifying all item data components.

完整的 NBT 树编辑器，可查看和修改物品的所有数据组件。

- Tree view with color-coded tag types / 树形结构展示，颜色编码的标签类型
- Search by key, value, or type / 搜索过滤：按键名、值、类型搜索
- Expand all / collapse all / 全部展开 / 全部折叠
- Add new tags of any NBT type / 新建任意 NBT 类型标签
- Double-click or Enter to edit, auto type validation / 双击或 Enter 编辑值，自动校验类型
- Delete to remove nodes / Delete 删除节点
- Sidebar: item ID, count, component count / 侧边栏显示物品 ID、数量、组件数

---

## Development / 开发信息

**NeoForge (Based on 1.21.1):**
- Minecraft: 1.21.1
- NeoForge: 21.1.172
- Parchment Mappings: 2024.11.17
- Java 21

**Fabric (Based on 1.21.1):**
- Minecraft: 1.21.1
- Fabric Loader: 0.16.9
- Fabric API: 0.102.1+1.21.1
- Parchment Mappings: 2024.11.17
- Java 21
