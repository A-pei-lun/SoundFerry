package com.example.soundferry.translate

/**
 * 翻译器接口
 * 后续可以换成其他翻译引擎（比如替换 ML Kit 为本地 LLM）
 */
interface Translator {
    /** 翻译文本，sourceLang="zh", targetLang="en" */
    suspend fun translate(text: String, sourceLang: String = "zh", targetLang: String = "en"): String
}
