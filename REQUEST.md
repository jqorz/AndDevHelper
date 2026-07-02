# AndDevHelper 需求文档

## 项目概述

**AndDevHelper** 是一款基于 Kotlin Multiplatform (Desktop JVM) + Compose Multiplatform 的 ADB 辅助开发工具。目标是集成 ADB 常用命令，为 Android 开发者提供便捷的图形化操作界面，避免与 Android Studio 功能重复（如 Logcat、布局查看等）。

---

## 一、整体布局架构

### 1.1 顶部区域 — 设备切换栏

- 显示当前已连接的所有 Android 设备列表（通过 `adb devices` 获取）
- 以横向标签或下拉选择器的形式展示，支持快速切换当前操作的目标设备
- 设备状态实时刷新（在线 / 离线）
- 显示设备序列号及设备名称（如有）
- 支持手动刷新设备列表

### 1.2 中部区域 — 功能 Tab 页

采用多 Tab 布局，每个 Tab 对应一个功能类别。每个 Tab 内部包含：

- **预置命令按钮**：常用 ADB 命令的快捷按钮，点击即执行
- **自定义命令区域**：用户可自行添加自定义命令到当前 Tab 中

### 1.3 底部区域 — 应用安装区

- 固定在界面底部
- 支持 **拖拽 APK 文件** 到该区域进行安装
- 支持 **手动选择文件**（文件选择器）
- 安装时自动使用当前选中的设备
- 显示安装进度和结果反馈

---

## 二、Tab 功能分类（预置）

### Tab 1: 设备信息

| 功能 | 预置命令 | 说明 |
|------|---------|------|
| 查看设备型号 | `adb -s {device} shell getprop ro.product.model` | 获取设备型号 |
| 查看 Android 版本 | `adb -s {device} shell getprop ro.build.version.release` | 获取系统版本 |
| 查看屏幕分辨率 | `adb -s {device} shell wm size` | 获取屏幕尺寸 |
| 查看设备序列号 | `adb -s {device} get-serialno` | 获取序列号 |
| 查看电池信息 | `adb -s {device} shell dumpsys battery` | 获取电池状态 |
| 查看设备 IP | `adb -s {device} shell ip addr show wlan0` | 获取 WiFi IP |

### Tab 2: 应用管理

| 功能 | 预置命令 | 说明 |
|------|---------|------|
| 查看顶层 Activity | `adb -s {device} shell dumpsys activity activities \| grep mResumedActivity` | 获取当前前台 Activity |
| 查看已安装应用列表 | `adb -s {device} shell pm list packages` | 列出所有包名 |
| 查看第三方应用 | `adb -s {device} shell pm list packages -3` | 仅列出第三方应用 |
| 清除应用数据 | `adb -s {device} shell pm clear {package}` | 清除指定应用数据 |
| 强制停止应用 | `adb -s {device} shell am force-stop {package}` | 强制停止应用 |
| 启动应用 | `adb -s {device} shell am start -n {package}/{activity}` | 启动指定 Activity |
| 查看应用路径 | `adb -s {device} shell pm path {package}` | 获取 APK 安装路径 |

### Tab 3: 文件操作

| 功能 | 预置命令 | 说明 |
|------|---------|------|
| 推送文件到设备 | `adb -s {device} push {local} {remote}` | 上传文件 |
| 从设备拉取文件 | `adb -s {device} pull {remote} {local}` | 下载文件 |
| 截图保存到电脑 | `adb -s {device} shell screencap -p /sdcard/screenshot.png && adb -s {device} pull /sdcard/screenshot.png` | 截屏 |
| 录屏 | `adb -s {device} shell screenrecord /sdcard/record.mp4` | 屏幕录制（Ctrl+C 停止） |

### Tab 4: 系统操作

| 功能 | 预置命令 | 说明 |
|------|---------|------|
| 重启设备 | `adb -s {device} reboot` | 重启 |
| 重启到 Recovery | `adb -s {device} reboot recovery` | 进入 Recovery 模式 |
| 重启到 Bootloader | `adb -s {device} reboot bootloader` | 进入 Bootloader |
| 查看系统属性 | `adb -s {device} shell getprop` | 列出所有系统属性 |
| 查看进程列表 | `adb -s {device} shell ps` | 查看运行中的进程 |
| 查看内存信息 | `adb -s {device} shell cat /proc/meminfo` | 获取内存使用情况 |

### Tab 5: 网络调试

| 功能 | 预置命令 | 说明 |
|------|---------|------|
| 无线连接设备 | `adb tcpip 5555 && adb connect {ip}:5555` | 切换到无线调试模式 |
| 断开无线连接 | `adb disconnect {ip}:5555` | 断开指定设备 |
| 查看端口占用 | `adb -s {device} shell netstat` | 查看网络连接 |

---

## 三、自定义命令功能

### 3.1 添加自定义命令

- 每个 Tab 底部提供 **「+ 添加命令」** 按钮
- 点击后弹出对话框，包含以下字段：
  - **命令名称**（显示在按钮上的文字）
  - **ADB 命令**（实际执行的命令，支持 `{device}` 和 `{package}` 等变量占位符）
  - **所属 Tab**（下拉选择，可将命令添加到任意 Tab）
- 支持命令分组/分类

### 3.2 命令编辑与删除

- 自定义命令支持 **长按/右键菜单** 进行编辑或删除
- 预置命令也支持二次修改（修改后标记为「已修改」，可恢复默认）

### 3.3 变量占位符

命令中支持以下变量，在执行时自动替换：

| 变量 | 说明 |
|------|------|
| `{device}` | 当前选中的设备序列号 |
| `{package}` | 用户输入的包名 |
| `{ip}` | 用户输入的 IP 地址 |
| `{local}` | 本地文件路径（通过文件选择器获取） |
| `{remote}` | 设备上的文件路径 |

### 3.4 命令执行结果

- 每个命令执行后，在按钮下方或弹出窗口中显示命令输出结果
- 支持复制输出内容
- 显示命令执行状态（成功/失败/超时）

---

## 四、设置界面

### 4.1 ADB 路径配置

- **默认行为**：启动时自动从系统环境变量 `PATH` 中查找 `adb` 可执行文件
  - 若找到，自动填入路径并显示绿色状态
  - 若未找到，显示红色警告，提示用户手动配置
- **手动配置**：提供文件选择器，允许用户手动指定 `adb` 的完整路径
- **路径验证**：配置后自动验证路径是否有效（执行 `adb version` 检测）

### 4.2 其他设置项

| 设置项 | 说明 | 默认值 |
|--------|------|--------|
| ADB 路径 | adb 可执行文件的完整路径 | 自动从环境变量查找 |
| 命令超时时间 | 单个命令的最大执行时长（秒） | 30 |
| 自动刷新设备 | 是否定时自动刷新设备列表 | 开启 |
| 刷新间隔 | 设备列表自动刷新间隔（秒） | 5 |
| 主题模式 | 亮色 / 暗色 / 跟随系统 | 跟随系统 |
| 命令历史记录 | 保存最近执行的命令记录数量 | 100 |

### 4.3 数据持久化

- 用户自定义命令、设置项、命令历史记录需持久化存储
- 推荐使用 JSON 文件存储于用户目录下（如 `~/.anddevhelper/config.json`）

---

## 五、交互与 UX 要求

1. **命令执行反馈**：所有命令执行时显示加载状态，执行完毕显示结果或错误信息
2. **错误处理**：ADB 未连接、命令超时、权限不足等情况需给出明确提示
3. **设备离线处理**：设备断开时禁用相关操作按钮，提示设备已离线
4. **拖拽安装**：底部安装区域支持文件拖拽高亮反馈，安装过程中显示进度条
5. **可调整布局**：Tab 区域支持滚动，命令按钮区域支持自适应排列
6. **窗口尺寸**：默认窗口大小适配 1280x800，支持最小尺寸限制

---

## 六、技术约束

- 平台：Desktop JVM（Windows / macOS / Linux）
- UI 框架：Compose Multiplatform
- ADB 调用：通过 `ProcessBuilder` 执行系统命令，解析标准输出
- 构建工具：Gradle (Kotlin DSL)
- 数据存储：JSON 文件（如 kotlinx.serialization + 文件 IO）
