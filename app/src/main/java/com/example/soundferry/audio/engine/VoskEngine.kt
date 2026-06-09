package com.example.soundferry.audio.engine

import android.content.Context
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream

/**
 * Vosk 语音识别引擎
 */
class VoskEngine(private val appContext: Context) : ASREngine {

    override val engineName = "Vosk"

    private var model: Model? = null
    private var recognizer: Recognizer? = null

    override var isInitialized = false
        private set

    override var currentModel = ""
        private set

    override var onPartialResult: ((String) -> Unit)? = null

    override fun init(modelFolder: String) {
        currentModel = modelFolder
        try {
            val modelPath = resolveModelPath(modelFolder)
            model = Model(modelPath)
            recognizer = Recognizer(model, 16000f)
            isInitialized = true
            Log.d(TAG, "Vosk 引擎初始化成功：$modelFolder")
        } catch (e: Exception) {
            Log.e(TAG, "Vosk 引擎初始化失败", e)
            throw e
        }
    }

    /**
     * 解析模型路径：
     * 1. 优先看 files/models/ 下有没有（用户导入的）
     * 2. 没有再从 assets 复制（内置的）
     */
    private fun resolveModelPath(modelFolder: String): String {
        val installedDir = File(appContext.filesDir, "models/$modelFolder")
        if (installedDir.exists() && installedDir.list()?.isNotEmpty() == true) {
            Log.d(TAG, "使用已安装模型：$installedDir")
            return installedDir.absolutePath
        }
        val assetsDir = File(appContext.filesDir, "models/$modelFolder")
        if (assetsDir.exists()) assetsDir.deleteRecursively()
        copyAssetDir(modelFolder, assetsDir)
        return assetsDir.absolutePath
    }

    override fun feedAudioChunk(data: ByteArray, len: Int) {
        if (!isInitialized) return
        recognizer?.acceptWaveForm(data, len)
    }

    override fun getPartialResult(): String? {
        if (!isInitialized) return null
        val result = recognizer?.partialResult ?: return null
        return extractJsonField(result, "partial")
    }

    override fun getFinalResult(): String? {
        if (!isInitialized) return null
        val result = recognizer?.result ?: return null
        return extractJsonField(result, "text")
    }

    override fun reset() {
        recognizer?.reset()
    }

    override fun release() {
        recognizer?.reset()
        recognizer = null
        model?.close()
        model = null
        isInitialized = false
        currentModel = ""
        Log.d(TAG, "Vosk 引擎已释放")
    }

    override fun switchModel(modelFolder: String): Boolean {
        if (modelFolder == currentModel && isInitialized) return true
        return try {
            release()
            init(modelFolder)
            true
        } catch (e: Exception) {
            Log.e(TAG, "模型切换失败：$modelFolder", e)
            false
        }
    }

    override fun getAvailableModels(): List<String> {
        val models = mutableSetOf<String>()
        // assets 中的内置模型
        try {
            appContext.assets.list("")?.filter { it.startsWith("vosk-model") }?.forEach {
                models.add(it)
            }
        } catch (_: Exception) {}
        // 用户导入的已安装模型
        val installedDir = File(appContext.filesDir, "models")
        if (installedDir.exists()) {
            installedDir.listFiles { f -> f.isDirectory && f.name.startsWith("vosk-model") }?.forEach {
                models.add(it.name)
            }
        }
        return models.toList().sorted()
    }

    // ===== 内部方法 =====

    private fun copyModelFromAssets(folderName: String): String {
        val modelDir = File(appContext.filesDir, "models/$folderName")
        if (modelDir.exists()) modelDir.deleteRecursively()
        copyAssetDir(folderName, modelDir)
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

    private fun extractJsonField(json: String, field: String): String? {
        val pattern = "\"$field\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
    }

    companion object {
        private const val TAG = "VoskEngine"
    }
}
