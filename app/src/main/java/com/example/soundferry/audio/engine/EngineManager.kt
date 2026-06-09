package com.example.soundferry.audio.engine

import android.util.Log

/**
 * 引擎管理器
 * 管理当前使用的 ASR 引擎，支持运行时切换
 */
class EngineManager(
    private val voskEngine: VoskEngine,
    private val sherpaEngine: SherpaEngine
) {
    private var current: ASREngine = voskEngine

    /** 当前引擎 */
    val currentEngine: ASREngine get() = current

    /** 当前引擎名称 */
    val currentEngineName: String get() = current.engineName

    /** 是否已初始化 */
    val isInitialized: Boolean get() = current.isInitialized

    /** 当前模型 */
    val currentModel: String get() = current.currentModel

    /** 切换引擎 */
    fun switchEngine(engineName: String): Boolean {
        val newEngine = when (engineName) {
            "Vosk" -> voskEngine
            "sherpa-onnx" -> sherpaEngine
            else -> return false
        }

        if (newEngine == current) return true

        // 释放旧引擎
        current.release()

        current = newEngine
        Log.d(TAG, "引擎切换到：${current.engineName}")
        return true
    }

    /** 获取所有可用引擎 */
    fun getAvailableEngines(): List<String> {
        return listOf("Vosk", "sherpa-onnx")
    }

    /** 初始化当前引擎 */
    fun init(modelFolder: String) {
        current.init(modelFolder)
    }

    fun feedAudioChunk(data: ByteArray, len: Int) = current.feedAudioChunk(data, len)
    fun getPartialResult(): String? = current.getPartialResult()
    fun getFinalResult(): String? = current.getFinalResult()
    fun reset() = current.reset()
    fun release() = current.release()
    fun switchModel(modelFolder: String): Boolean = current.switchModel(modelFolder)
    fun getAvailableModels(): List<String> = current.getAvailableModels()

    companion object {
        private const val TAG = "EngineManager"
    }
}
