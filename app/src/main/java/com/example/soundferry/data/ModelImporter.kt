package com.example.soundferry.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

/**
 * 模型导入器
 * 扫描指定目录中的 .zip 模型包 → 自动解压到 models/ → 可选删除压缩包
 */
class ModelImporter(private val context: Context) {

    private val modelsDir: File
        get() = File(context.filesDir, "models").also { it.mkdirs() }

    private val pendingDir: File
        get() = File(context.filesDir, "pending_models").also { it.mkdirs() }

    /**
     * 扫描待导入的模型压缩包
     * @return 找到的 zip 文件名列表
     */
    fun scanPendingZips(): List<String> {
        val result = mutableListOf<String>()

        // 扫描 filesDir 根目录
        context.filesDir.listFiles { f -> f.name.endsWith(".zip") }?.forEach {
            // 移到 pending 目录避免重复扫描
            val target = File(pendingDir, it.name)
            it.renameTo(target)
            result.add(it.name)
        }

        // 扫描 pending 目录
        pendingDir.listFiles { f -> f.name.endsWith(".zip") }?.forEach {
            result.add(it.name)
        }

        // 也扫描 custom 目录（用户从设置里打开的目录）
        val customDir = File(context.filesDir, "custom")
        if (customDir.exists()) {
            customDir.listFiles { f -> f.name.endsWith(".zip") }?.forEach {
                val target = File(pendingDir, it.name)
                if (!target.exists()) {
                    it.renameTo(target)
                    result.add(it.name)
                }
            }
        }

        return result.distinct()
    }

    /**
     * 导入（解压）模型
     * @param zipFileName pending 目录中的 zip 文件名
     * @return 解压后的模型文件夹名，失败返回 null
     */
    fun importModel(zipFileName: String): String? {
        val zipFile = File(pendingDir, zipFileName)
        if (!zipFile.exists()) {
            Log.e(TAG, "文件不存在：$zipFileName")
            return null
        }

        return try {
            val extractedName = extractZip(zipFile)
            if (extractedName != null) {
                Log.d(TAG, "模型导入成功：$extractedName")
            }
            extractedName
        } catch (e: Exception) {
            Log.e(TAG, "模型解压失败：$zipFileName", e)
            null
        }
    }

    /**
     * 解压后是否删除压缩包
     */
    fun deleteZip(zipFileName: String) {
        val zipFile = File(pendingDir, zipFileName)
        if (zipFile.exists()) zipFile.delete()
    }

    /**
     * 解压 zip 到 models 目录
     */
    private fun extractZip(zipFile: File): String? {
        val zipInputStream = ZipInputStream(FileInputStream(zipFile))
        var modelFolderName: String? = null

        try {
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                val entryName = entry.name.trim('/')
                val firstDir = entryName.substringBefore('/')

                // 记录顶层文件夹名（模型名）
                if (modelFolderName == null && firstDir.isNotBlank()) {
                    modelFolderName = firstDir
                }

                val outFile = File(modelsDir, entryName)

                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    outFile.outputStream().use { output ->
                        zipInputStream.copyTo(output, bufferSize = 8192)
                    }
                }

                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        } finally {
            zipInputStream.close()
        }

        return modelFolderName
    }

    /**
     * 获取所有已安装的模型文件夹名
     */
    fun getInstalledModels(): List<String> {
        return modelsDir.listFiles { f -> f.isDirectory && f.name.startsWith("vosk-model") }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }

    /**
     * 删除已安装的模型（非内置模型，删除 filesDir 中的）
     * @return true=删除了, false=不存在或内置模型
     */
    fun deleteInstalledModel(modelFolder: String): Boolean {
        // 只在 filesDir/models/ 中删除，不影响 assets 内置模型
        val modelDir = File(modelsDir, modelFolder)
        if (modelDir.exists() && modelDir.isDirectory) {
            modelDir.deleteRecursively()
            Log.d(TAG, "已删除模型：$modelFolder")
            return true
        }
        return false
    }

    companion object {
        private const val TAG = "ModelImporter"
    }
}
