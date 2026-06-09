package com.example.soundferry.audio.engine

import android.content.Context
import android.util.Log

/**
 * sherpa-onnx 引擎（占位）
 * TODO: 集成 sherpa-onnx Android SDK 后实现
 */
class SherpaEngine(private val appContext: Context) : ASREngine {

    override val engineName = "sherpa-onnx"

    override var isInitialized = false
        private set

    override var currentModel = ""
        private set

    override var onPartialResult: ((String) -> Unit)? = null

    override fun init(modelFolder: String) {
        Log.w(TAG, "sherpa-onnx 引擎尚未实现")
        // TODO: 初始化 sherpa-onnx recognizer
        currentModel = modelFolder
        isInitialized = false
    }

    override fun feedAudioChunk(data: ByteArray, len: Int) {
        // TODO: 喂音频给 sherpa-onnx
    }

    override fun getPartialResult(): String? = null

    override fun getFinalResult(): String? = null

    override fun reset() {
        // TODO: 重置
    }

    override fun release() {
        // TODO: 释放 sherpa-onnx 资源
        isInitialized = false
        currentModel = ""
    }

    override fun switchModel(modelFolder: String): Boolean {
        release()
        init(modelFolder)
        return false // 尚未实现
    }

    override fun getAvailableModels(): List<String> {
        // TODO: 扫描 sherpa-onnx 模型
        return emptyList<String>().also {
            Log.w(TAG, "sherpa-onnx 模型扫描尚未实现")
        }
    }

    companion object {
        private const val TAG = "SherpaEngine"
    }
}
