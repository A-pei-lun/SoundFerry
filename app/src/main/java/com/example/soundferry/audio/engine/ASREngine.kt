package com.example.soundferry.audio.engine

/**
 * 语音识别引擎接口
 * 所有引擎（Vosk、sherpa-onnx 等）实现此接口
 */
interface ASREngine {
    /** 引擎名称（显示用） */
    val engineName: String

    /** 是否已初始化 */
    val isInitialized: Boolean

    /** 当前模型文件夹名 */
    val currentModel: String

    /** 实时识别结果回调 */
    var onPartialResult: ((String) -> Unit)?

    /** 初始化引擎并加载模型 */
    fun init(modelFolder: String)

    /** 喂音频数据给引擎识别 */
    fun feedAudioChunk(data: ByteArray, len: Int)

    /** 获取实时部分结果 */
    fun getPartialResult(): String?

    /** 获取最终完整结果 */
    fun getFinalResult(): String?

    /** 重置识别状态 */
    fun reset()

    /** 释放引擎所有资源 */
    fun release()

    /** 切换模型 */
    fun switchModel(modelFolder: String): Boolean

    /** 获取可用模型列表 */
    fun getAvailableModels(): List<String>
}
