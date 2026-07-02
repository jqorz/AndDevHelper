# AndDevHelper

一款基于 Kotlin Multiplatform + Compose Multiplatform 的 ADB 辅助开发工具，为 Android 开发者提供便捷的图形化操作界面。

## 功能特性

### 设备管理
- 顶部显示已连接的 Android 设备列表，支持快速切换
- 设备状态实时显示（在线 / 离线 / 未授权）
- 支持手动刷新设备列表，可配置自动刷新间隔

### 多 Tab 命令面板

内置 5 个功能分类，每个分类包含常用 ADB 命令快捷按钮：

| 分类 | 预置命令 |
|------|---------|
| **设备信息** | 查看设备型号、Android 版本、屏幕分辨率、序列号、电池信息、设备 IP |
| **应用管理** | 查看顶层 Activity、已安装应用列表、第三方应用、清除数据、强制停止、启动应用、查看应用路径 |
| **文件操作** | 推送文件、拉取文件、截图、录屏 |
| **系统操作** | 重启设备、重启到 Recovery / Bootloader、查看系统属性、进程列表、内存信息 |
| **网络调试** | 无线连接设备、断开无线连接、查看端口占用 |

### 自定义命令
- 每个 Tab 底部可添加自定义命令
- 支持变量占位符：`{device}`、`{package}`、`{ip}`、`{local}`、`{remote}`
- 预置命令支持二次编辑，修改后可恢复默认
- 自定义命令支持编辑和删除
- 命令执行结果弹窗显示，支持一键复制

### APK 安装
- 底部固定安装区域，支持手动选择 APK 文件
- 安装时自动使用当前选中的设备
- 显示安装进度和结果反馈

### 设置界面
- **ADB 路径配置**：启动时自动从环境变量查找，支持手动指定路径并验证
- **命令超时时间**：可配置单个命令最大执行时长
- **自动刷新**：可开关设备列表自动刷新，自定义刷新间隔
- **主题模式**：亮色 / 暗色 / 跟随系统
- 所有设置和自定义命令持久化存储于 `~/.anddevhelper/config.json`

## 技术栈

- **语言**：Kotlin 2.4.0
- **UI 框架**：Compose Multiplatform 1.11.1 + Material3
- **平台**：Desktop JVM（Windows / macOS / Linux）
- **构建工具**：Gradle 8.13 (Kotlin DSL)
- **ADB 调用**：ProcessBuilder
- **数据存储**：kotlinx.serialization + JSON 文件

## 运行

```bash
# 标准运行
./gradlew :desktopApp:run
```

## 项目结构

```
shared/src/
├── commonMain/kotlin/com/jqorz/anddevhelper/
│   ├── model/          # 数据模型（AdbDevice, AdbCommand, AppSettings 等）
│   ├── service/        # 业务逻辑（AdbService, ConfigManager）
│   ├── data/           # 预置命令定义
│   ├── ui/             # Compose UI 组件
│   ├── viewmodel/      # 状态管理
│   └── ui/theme/       # Material3 主题
└── jvmMain/kotlin/com/jqorz/anddevhelper/
    ├── App.kt          # 主界面组装
    └── ui/ApkInstaller.kt  # APK 安装区（含 AWT 拖拽）

desktopApp/src/main/kotlin/com/jqorz/anddevhelper/
└── main.kt             # 窗口配置（1280×800）
```
