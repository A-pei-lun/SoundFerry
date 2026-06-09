# 声渡 SoundFerry 🎤

> 离线实时语音转写 + 中译英 Android APP

一句话：**点一下录音，边说边出字，还能翻成英文。** 全程离线，不耗流量。

---

## 功能亮点

| 功能 | 说明 |
|------|------|
| 🎙️ 实时语音转写 | 边说话边显示文字，延迟 ~200ms |
| 🌐 中译英翻译 | Google ML Kit 离线翻译，首次需联网下载模型 |
| 🔄 多引擎架构 | Vosk（已集成）+ Sherpa（预留），可切换 |
| 📦 模型导入 | 下载大模型 ZIP → APP 内一键导入 |
| 🎨 主题系统 | JSON 驱动的元素化布局，可自定义 |
| 🌍 中英文 UI | 支持中英文界面切换（设置→语言） |
| 📋 历史记录 | 识别结果本地存储，可查看/清空 |
| 🧪 实验功能 | 液化玻璃效果（Android 12+） |

---

## 大模型下载（可选）

APP 内已自带 **小模型**（`vosk-model-small-cn-0.22` ~42MB / `vosk-model-small-en-us-0.15` ~40MB），开箱即用。

如需更高准确率，下载官方大模型后通过 APP 的「模型导入」功能加载：

| 语言 | 模型 | 大小 | 下载地址 |
|------|------|------|----------|
| 🇨🇳 中文 | `vosk-model-cn-0.22` | ~1.5GB | [下载](https://alphacephei.com/vosk/models/vosk-model-cn-0.22.zip) |
| 🇺🇸 英文 | `vosk-model-en-us-0.22` | ~1.8GB | [下载](https://alphacephei.com/vosk/models/vosk-model-en-us-0.22.zip) |

**导入方法：**
1. 下载 ZIP 压缩包（**不要解压**）
2. 将 ZIP 复制到手机存储的 `Android/data/com.example.soundferry/files/` 目录
3. 打开 APP → 设置 → 模型导入 → 点击对应的 ZIP
4. 导入成功后切换模型即可

> ⚠️ **保留 ZIP 文件**：后续版本升级覆盖安装后，ZIP 可重复使用，无需重新下载。

---

## 构建指南

### 环境要求

- Android Studio Hedgehog+ (2023.1+)
- JDK 17
- Gradle 8.x+
- Android SDK 36

### 构建步骤

```bash
# 1. 克隆仓库
git clone https://github.com/A-pei-lun/SoundFerry.git

# 2. 用 Android Studio 打开项目根目录（SoundFerry/）
# 3. 等待 Gradle Sync 完成
# 4. 连接设备或启动模拟器
# 5. 点击 Run 或执行：
./gradlew assembleDebug
```

APK 生成路径：`app/build/outputs/apk/debug/app-debug.apk`

---

## 使用说明

1. 打开 APP，授予麦克风权限
2. 等待底部提示「语音模型已就绪」
3. 点击红色录音按钮开始说话
4. 文字实时出现在屏幕上
5. 再次点击按钮停止录音
6. 开启「英译」开关可自动翻译成英文

---

## 技术栈

| 层 | 技术 |
|----|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM |
| DI | Koin |
| ASR 引擎 | Vosk Android SDK 0.3.47 |
| 翻译 | Google ML Kit Translate 17.0.3 |
| 本地存储 | DataStore Preferences + JSON 文件 |
| 最低 API | 24 (Android 7.0) |
| 目标 API | 36 (Android 16) |

---

## 项目结构

```
app/src/main/java/com/example/soundferry/
├── audio/                  # 音频相关
│   ├── AudioRecorder.kt    # 录音器（AudioRecord）
│   ├── VoskRecognizer.kt   # Vosk 识别封装
│   └── engine/             # 多引擎架构
│       ├── ASREngine.kt    # 引擎接口
│       ├── VoskEngine.kt   # Vosk 实现
│       ├── SherpaEngine.kt # Sherpa（预留）
│       └── EngineManager.kt# 引擎管理器
├── data/                   # 数据层
│   ├── HistoryRepository.kt
│   ├── ModelImporter.kt
│   ├── SettingsRepository.kt
│   └── SettingsDataStore.kt
├── di/                     # 依赖注入
│   └── AppModule.kt
├── translate/              # 翻译
│   ├── Translator.kt       # 翻译接口
│   └── MLKitTranslator.kt  # ML Kit 实现
├── ui/                     # UI 层
│   ├── MainScreen.kt       # 主界面
│   ├── MainViewModel.kt    # 主 ViewModel
│   ├── SettingsScreen.kt   # 设置界面
│   ├── elements/           # UI 元素组件
│   └── theme/              # 主题系统
└── util/
    └── TextFormatter.kt
```

---

## 主题系统

主题以 JSON 文件定义，内置两个主题：
- **极简黑** — 经典深色，沉稳百搭
- **琉璃** — 青绿点缀，清新亮眼

自定义主题：将 JSON 文件放入手机存储的 `Android/data/com.example.soundferry/files/custom/` 目录即可。

---

## 版权声明

```
POWERED BY
  APIRL
```

---

## License

本项目为开源 MVP 版本，仅供学习参考。
