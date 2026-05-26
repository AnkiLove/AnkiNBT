# 编译说明

## 环境

| 目标 | Java |
|---|---|
| Minecraft 1.21 到 1.21.11 | Java 21 |
| Minecraft 26.1 到 26.1.2 | Java 25 |

本仓库的 Windows 编译环境使用：

- Java 21: `C:\Program Files\Zulu\zulu-21`
- Java 25: `C:\Program Files\Zulu\zulu-25`
- 代理端口: `127.0.0.1:7897`

## 常用命令

```powershell
.\gradlew.bat clean build --no-daemon
```

多版本构建时进入对应目录，例如：

```powershell
cd versions\26.1.2
.\gradlew.bat clean build --no-daemon
```

## 输出

编译产物位于每个版本目录的 `build/libs`。发布整理后的文件会放入 `output` 目录。
