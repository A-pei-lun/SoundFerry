package com.example.soundferry.util

/**
 * 文本格式化工具
 * 自动检测中英文，应用不同的格式化规则
 */
object TextFormatter {

    /**
     * 格式化识别结果（自动检测中英文）
     */
    fun formatText(raw: String): String {
        if (raw.isBlank()) return raw
        return if (containsChinese(raw)) {
            formatChinese(raw)
        } else {
            formatEnglish(raw)
        }
    }

    /**
     * 判断文本是否包含中文字符
     */
    private fun containsChinese(text: String): Boolean {
        return text.any { it in '\u4e00'..'\u9fff' }
    }

    /**
     * 格式化中文：去空格 + 加句尾标点
     */
    private fun formatChinese(raw: String): String {
        val text = raw.replace("\\s+".toRegex(), "")
        if (text.any { it in "。？！" }) return text
        return when (text.lastOrNull()) {
            '吗', '呢' -> "${text}？"
            else -> "${text}。"
        }
    }

    /**
     * 格式化英文：保留空格 + 首字母大写 + 句号
     */
    private fun formatEnglish(raw: String): String {
        val trimmed = raw.trim()
        val capitalized = trimmed.replaceFirstChar { it.uppercase() }
        return if (capitalized.last() !in ".!?") "$capitalized." else capitalized
    }
}
