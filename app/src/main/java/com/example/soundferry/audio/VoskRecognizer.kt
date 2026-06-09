package com.example.soundferry.audio

import android.content.Context
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream

/**
 * Vosk 语音识别封装
 * 支持运行时切换模型
 */
class VoskRecognizer(private val appContext: Context) {

    private var model: Model? = null
    private var recognizer: Recognizer? = null

    var isInitialized = false
        private set

    /** 当前模型文件夹名（assets 中的文件夹名） */
    var currentModelFolder: String = "vosk-model-small-cn-0.22"
        private set

    var onFinalResult: ((String) -> Unit)? = null
    var onPartialResult: ((String) -> Unit)? = null

    /**
     * 扫描 assets 中可用的 Vosk 模型文件夹
     */
    fun getAvailableModels(): List<String>? {
        return try {
            appContext.assets.list("")?.filter { it.startsWith("vosk-model") }
        } catch (_: Exception) { null }
    }

    /**
     * 初始化（加载默认模型）
     */
    fun init(modelFolder: String = "vosk-model-small-cn-0.22") {
        currentModelFolder = modelFolder
        loadModel()
    }

    /**
     * 切换模型（会释放旧模型，加载新模型）
     * 调用前确保没有在录音
     */
    fun switchModel(modelFolder: String): Boolean {
        if (modelFolder == currentModelFolder && isInitialized) return true
        return try {
            release()
            currentModelFolder = modelFolder
            loadModel()
            true
        } catch (e: Exception) {
            Log.e(TAG, "模型切换失败: $modelFolder", e)
            false
        }
    }

    private fun loadModel() {
        val modelPath = copyModelFromAssets(currentModelFolder)
        model = Model(modelPath)
        recognizer = Recognizer(model, 16000f)
        isInitialized = true
        Log.d(TAG, "模型加载成功：$currentModelFolder")
    }

    private fun copyModelFromAssets(folderName: String): String {
        val modelDir = File(appContext.filesDir, "models/$folderName")
        if (modelDir.exists()) modelDir.deleteRecursively()

        copyAssetDir(folderName, modelDir)
        Log.d(TAG, "模型已解压到：${modelDir.absolutePath}")
        return modelDir.absolutePath
    }

    private fun copyAssetDir(assetPath: String, targetDir: File) {
        val assetManager = appContext.assets
        val entries = assetManager.list(assetPath)
            ?: throw IllegalStateException("assets 中找不到目录：$assetPath")
        targetDir.mkdirs()
        for (entry in entries) {
            val fullPath = "$assetPath/$entry"
            val outFile = File(targetDir, entry)
            try {
                assetManager.open(fullPath).use { input ->
                    FileOutputStream(outFile).use { output -> input.copyTo(output) }
                }
            } catch (_: java.io.IOException) {
                copyAssetDir(fullPath, outFile)
            }
        }
    }

    fun feedAudioChunk(data: ByteArray, len: Int) {
        if (!isInitialized) return
        recognizer?.acceptWaveForm(data, len)
    }

    fun getPartialResult(): String? {
        if (!isInitialized) return null
        val result = recognizer?.partialResult ?: return null
        return extractJsonField(result, "partial")
    }

    fun getFinalResult(): String? {
        if (!isInitialized) return null
        val result = recognizer?.result ?: return null
        return extractJsonField(result, "text")
    }

    fun reset() { recognizer?.reset() }

    fun release() {
        recognizer?.reset()
        recognizer = null
        model?.close()
        model = null
        isInitialized = false
        currentModelFolder = ""
        Log.d(TAG, "模型已释放")
    }

    private fun extractJsonField(json: String, field: String): String? {
        val pattern = "\"$field\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
    }

    companion object {
        private const val TAG = "VoskRecognizer"
    }
}
