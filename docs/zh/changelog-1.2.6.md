# AnkiNBT 1.2.6 更新记录

## 修复

- 修复 NeoForge 26.1、26.1.1、26.1.2 启动时的模块导出冲突。
- 26.x 构建不再把 Minecraft 自身的 `net.minecraft` 包打进模组 JAR。

## 文档

- 调整 Wiki 首页排版，避免标题在宽屏下被拆成竖排。
- 补齐中文和英文首页结构，让快速入口、功能地图和项目链接保持一致。

## 验证

- NeoForge 26.1、26.1.1、26.1.2 已使用 Java 25 完成 `clean build`。
- 已检查 26.x 修复 JAR 中不再包含 `net/minecraft` 路径。
