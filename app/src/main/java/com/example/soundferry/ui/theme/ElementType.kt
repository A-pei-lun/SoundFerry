package com.example.soundferry.ui.theme

/**
 * 元素类型枚举
 * 新增元素时只需在此添加枚举值 + 编写对应 Composable 组件
 */
enum class ElementType {
    RECORD_BUTTON,      // 录音按钮（必须）
    TEXT_DISPLAY,       // 文字显示（必须）
    MODEL_SELECTOR,     // 模型选择（必须）
    SETTINGS_BUTTON,    // 设置入口（必须）
    TRANSLATION_TOGGLE, // 翻译开关
    WAVEFORM,           // 波形动画
    STATUS_BAR,         // 状态指示
    HISTORY_BUTTON      // 历史记录
}
