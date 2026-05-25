# AnkiNBT 编译说明

本文档记录当前工程的全版本编译方式，适用于 Windows 环境。

## 环境

- PowerShell: `C:\pwsh\pwsh.exe`
- Java 21: `C:\Program Files\Zulu\zulu-21`
- Java 25: `C:\Program Files\Zulu\zulu-25`
- 代理端口: `127.0.0.1:7897`
- 当前发布版本: `1.2.6`

编译脚本会自动设置 `JAVA_HOME`、`HTTP_PROXY`、`HTTPS_PROXY`。1.21 系列使用 Java 21，26.1 系列使用 Java 25。

## 全版本编译

在项目目录执行：

```powershell
C:\pwsh\pwsh.exe -NoProfile -Command "cmd /c scripts\build-all.bat -AllVersions -NoPause"
```

或直接运行：

```bat
scripts\build-all.bat -AllVersions -NoPause
```

## 编译范围

NeoForge:

- 1.21
- 1.21.1
- 1.21.2
- 1.21.3
- 1.21.4
- 1.21.5
- 1.21.6
- 1.21.7
- 1.21.8
- 1.21.9
- 1.21.10
- 1.21.11
- 26.1
- 26.1.1
- 26.1.2

Fabric:

- 1.21
- 1.21.1
- 1.21.2
- 1.21.3
- 1.21.4
- 1.21.5
- 1.21.6
- 1.21.7
- 1.21.8
- 1.21.9
- 1.21.10
- 1.21.11
- 26.1
- 26.1.1
- 26.1.2

## 输出目录

脚本会在 `output` 下创建按时间命名的目录，例如：

```text
output\周一022605-043813
```

目录内会包含：

- 各加载器、各 Minecraft 版本的单版本 jar
- 顶层合并包

常用顶层合并包：

- `AnkiNBT-Fabric-mc1.21.1-1.21.8-1.2.6.jar`
- `AnkiNBT-Fabric-mc1.21.9-1.21.10-1.2.6.jar`
- `AnkiNBT-Fabric-mc1.21.11-1.2.6.jar`
- `AnkiNBT-Fabric-mc26.1-1.2.6.jar`
- `AnkiNBT-Fabric-mc26.1.1-1.2.6.jar`
- `AnkiNBT-Fabric-mc26.1.2-1.2.6.jar`
- `AnkiNBT-NeoForge-mc1.21.9-1.21.10-1.2.6.jar`
- `AnkiNBT-NeoForge-mc1.21.11-1.2.6.jar`
- `AnkiNBT-NeoForge-mc26.1-1.2.6.jar`
- `AnkiNBT-NeoForge-mc26.1.1-1.2.6.jar`
- `AnkiNBT-NeoForge-mc26.1.2-1.2.6.jar`

## 验证

编译完成后，终端应显示：

```text
Results: Total: 30 | OK: 30 | Failed: 0
```

也可以检查输出目录：

```powershell
Get-ChildItem output\<输出目录名> -File
```

## 版本号

版本号来自各目录的 `gradle.properties`：

- `gradle.properties`
- `fabric\gradle.properties`
- `versions\*\gradle.properties`
- `fabric-versions\*\gradle.properties`

修改版本号后，建议执行：

```powershell
rg -n "mod_version=" -S gradle.properties fabric/gradle.properties versions fabric-versions
```

确认所有版本一致。

## 常见问题

如果依赖下载较慢，确认本机代理端口 `7897` 可用。

如果 Java 版本不对，优先确认：

```powershell
$env:JAVA_HOME='C:\Program Files\Zulu\zulu-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

26.1 系列需要 Java 25：

```powershell
$env:JAVA_HOME='C:\Program Files\Zulu\zulu-25'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

如果只想先验证单个版本，可进入对应目录后执行：

```bat
gradlew.bat clean build -x test --no-daemon
```
